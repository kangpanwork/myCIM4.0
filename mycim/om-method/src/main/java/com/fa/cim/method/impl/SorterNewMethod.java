package com.fa.cim.method.impl;

import cn.hutool.core.util.StrUtil;
import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.runtime.actionrecipe.CimActionRecipeDO;
import com.fa.cim.entity.runtime.cassette.CimCassetteDO;
import com.fa.cim.entity.runtime.sortjob.CimSortJobComponentDO;
import com.fa.cim.entity.runtime.sortjob.CimSortJobSlotMapDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.bo.sorter.CimActionCodeManager;
import com.fa.cim.newcore.bo.sorter.CimActionRecipe;
import com.fa.cim.newcore.bo.sorter.CimSorterJob;
import com.fa.cim.newcore.bo.sorter.CimSorterJobManager;
import com.fa.cim.newcore.dto.sorter.ActionRecipeDTO;
import com.fa.cim.newcore.dto.sorter.SorterJobDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.newcore.standard.mtrlmngm.Material;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import com.fa.cim.sorter.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/06/22       开发NewSorter        Bear               create file
 *
 * @author Jerry
 * @since 2021/06/22 04:22
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class SorterNewMethod implements ISorterNewMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IAutoDispatchControlMethod autoDispatchControlMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IConstraintMethod constraintMethod;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IEventMethod eventMakeMethod;

    @Autowired
    @Qualifier("ActionCodeManagerCore")
    private CimActionCodeManager actionCodeManager;

    @Autowired
    @Qualifier("SorterJobManagerCore")
    private CimSorterJobManager sorterJobManager;

    private final static String maxNumber = "99999";

    @Override
    public Boolean checkVendorLotID(String vendorLotID) {
        String sql ="SELECT DISTINCT slot.LOT_ID from OMSORTJOB_COMP_SLOTMAP slot where slot.LOT_ID=?1";
        List<Object[]> query = cimJpaRepository.query(sql, vendorLotID);
        if(CimArrayUtils.getSize(query)>0){
            return  true;
        }else {
            return false;
        }
    }

    @Override
    public List<Info.SortActionAttributes> waferSorterActionListSelectDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        if (log.isDebugEnabled()) {
            log.debug("Get object reference of Equipment");
        }
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(CimObjectUtils.isEmpty(aPosMachine), retCodeConfig.getNotFoundEqp());

        if (log.isDebugEnabled()) {
            log.debug("Get Equipment attributes");
        }
        String strMachineCategory = aPosMachine.getCategory();
        Validations.check(CimStringUtils.isEmpty(strMachineCategory), retCodeConfig.getMachineTypeNotSorter(),strMachineCategory);

        if (log.isDebugEnabled()) {
            log.debug("Check Equipment Type");
        }
        Validations.check(!CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_WAFERSORTER, strMachineCategory),
                retCodeConfig.getMachineTypeNotSorter(),strMachineCategory);

        if (log.isDebugEnabled()) {
            log.debug("query action code");
        }
        String sql = "SELECT\n" +
                "\t* \n" +
                "FROM\n" +
                "\tOMSORTACT \n" +
                "WHERE\n" +
                "\tEQP_ID = ?1";
        List<CimActionRecipeDO> cimActionRecipeDOList = cimJpaRepository.query(sql, CimActionRecipeDO.class, ObjectIdentifier.fetchValue(equipmentID));
        return cimActionRecipeDOList.stream()
                .map(cimActionRecipeDO -> {
                    Info.SortActionAttributes waferSorterActionList = new Info.SortActionAttributes();
                    waferSorterActionList.setEquipmentID(ObjectIdentifier.buildWithValue(cimActionRecipeDO.getEqpId()));
                    waferSorterActionList.setActionCode(cimActionRecipeDO.getActionCode());
                    waferSorterActionList.setPhysicalRecipeID(cimActionRecipeDO.getPhysicalRecipeId());
                    waferSorterActionList.setUserID(ObjectIdentifier.buildWithValue(cimActionRecipeDO.getUserId()));
                    waferSorterActionList.setStoreTime(CimDateUtils.convertToSpecString(cimActionRecipeDO.getStoreTime()));
                    return waferSorterActionList;
                }).collect(Collectors.toList());
    }

    @Override
    public List<Info.SortJobListAttributes> sorterJobListGetDR(Infos.ObjCommon objCommon, Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn) {
        if (log.isDebugEnabled()) {
            log.debug("step - Check input parameter");
        }
        Validations.check(ObjectIdentifier.isEmpty(objSorterJobListGetDRIn.getEquipmentID())
                        && ObjectIdentifier.isEmpty(objSorterJobListGetDRIn.getCreateUser())
                        && ObjectIdentifier.isEmpty(objSorterJobListGetDRIn.getSorterJob())
                        && ObjectIdentifier.isEmpty(objSorterJobListGetDRIn.getCarrierID())
                        && ObjectIdentifier.isEmpty(objSorterJobListGetDRIn.getLotID()),
                retCodeConfig.getInvalidInputParam());

        ObjectIdentifier equipmentID = objSorterJobListGetDRIn.getEquipmentID();
        ObjectIdentifier createUser = objSorterJobListGetDRIn.getCreateUser();
        ObjectIdentifier sorterJobID = objSorterJobListGetDRIn.getSorterJob();
        ObjectIdentifier carrierID = objSorterJobListGetDRIn.getCarrierID();
        ObjectIdentifier lotID = objSorterJobListGetDRIn.getLotID();
        ObjectIdentifier controlJob = objSorterJobListGetDRIn.getControlJob();
        String portGroup = objSorterJobListGetDRIn.getPortGroup();
        String actionCode = objSorterJobListGetDRIn.getActionCode();
        String sql = "SELECT DISTINCT\n" +
                "\tA.SORTER_JOB_ID,\n" +
                "\tA.EQP_ID,\n" +
                "\tA.PORT_GROUP_ID,\n" +
                "\tA.SORTER_JOB_STATUS,\n" +
                "\tA.REQ_USER_ID,\n" +
                "\tA.REQ_TIMESTAMP,\n" +
                "\tA.COMPONENT_JOB_COUNT,\n" +
                "\tA.PREV_SORTER_JOB_ID,\n" +
                "\tA.WAFER_ID_READ_FLAG, \n" +
                "\tA.CTRLJOB_ID \n" +
                "FROM\n" +
                "\tOMSORTJOB A\n" +
                "\tLEFT JOIN OMSORTJOB_COMP B ON A.ID = B.REFKEY \n" +
                "\tLEFT JOIN OMSORTJOB_COMP_SLOTMAP C ON A.ID = C.REFKEY ";
        boolean isFirstCondition = true;
        if (ObjectIdentifier.isNotEmpty(equipmentID)) {
            sql += isFirstCondition ? " WHERE" : " AND";
            sql += String.format(" A.EQP_ID LIKE '%s'", ObjectIdentifier.fetchValue(equipmentID));
            isFirstCondition = false;
        }
        if (ObjectIdentifier.isNotEmpty(carrierID)) {
            sql += isFirstCondition ? " WHERE" : " AND";
            String fetchValueCarrier = ObjectIdentifier.fetchValue(carrierID);
            sql += String.format(" (B.DEST_CARRIER_ID LIKE '%s' OR B.SRC_CARRIER_ID LIKE '%s')",
                    fetchValueCarrier, fetchValueCarrier);
            isFirstCondition = false;
        }
        if (ObjectIdentifier.isNotEmpty(createUser)) {
            sql += isFirstCondition ? " WHERE" : " AND";
            sql += String.format(" A.REQ_USER_ID LIKE '%s'", ObjectIdentifier.fetchValue(createUser));
            isFirstCondition = false;
        }
        if (ObjectIdentifier.isNotEmpty(sorterJobID)) {
            sql += isFirstCondition ? " WHERE" : " AND";
            sql += String.format(" A.SORTER_JOB_ID LIKE '%s'", ObjectIdentifier.fetchValue(sorterJobID));
            isFirstCondition = false;
        }
        if (ObjectIdentifier.isNotEmpty(lotID)) {
            sql += isFirstCondition ? " WHERE" : " AND";
            sql += String.format(" C.LOT_ID LIKE '%s'", ObjectIdentifier.fetchValue(lotID));
            isFirstCondition = false;
        }
        if (ObjectIdentifier.isNotEmpty(controlJob)) {
            sql += isFirstCondition ? " WHERE" : " AND";
            sql += String.format(" A.CTRLJOB_ID = '%s'", ObjectIdentifier.fetchValue(controlJob));
            isFirstCondition = false;
        }
        if (CimStringUtils.isNotEmpty(portGroup)) {
            sql += isFirstCondition ? " WHERE" : " AND";
            sql += String.format(" A.PORT_GROUP_ID = '%s'", portGroup);
            isFirstCondition = false;
        }
        if (CimStringUtils.isNotEmpty(actionCode)) {
            sql += isFirstCondition ? " WHERE" : " AND";
            sql += String.format(" B.ACTION_CODE = '%s'", actionCode);
            isFirstCondition = false;
        }
        List<Object[]> queryResult = cimJpaRepository.query(sql);
        List<Info.SortJobListAttributes> sorterJobListAttributesList = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(queryResult)) {
            for (Object[] object : queryResult) {
                Info.SortJobListAttributes sorterJobListAttributes = new Info.SortJobListAttributes();
                sorterJobListAttributesList.add(sorterJobListAttributes);
                sorterJobListAttributes.setSorterJobID(ObjectIdentifier.buildWithValue((String) object[0]));
                sorterJobListAttributes.setEquipmentID(ObjectIdentifier.buildWithValue((String) object[1]));
                sorterJobListAttributes.setPortGroupID(ObjectIdentifier.buildWithValue((String) object[2]));
                sorterJobListAttributes.setSorterJobStatus((String) object[3]);
                sorterJobListAttributes.setRequestUserID(ObjectIdentifier.buildWithValue((String) object[4]));
                sorterJobListAttributes.setRequestTimeStamp(String.valueOf(object[5]));
                sorterJobListAttributes.setComponentCount(CimNumberUtils.intValue((BigDecimal)object[6]));
                sorterJobListAttributes.setPreSorterJobID(ObjectIdentifier.buildWithValue((String) object[7]));
                sorterJobListAttributes.setWaferIDReadFlag(CimNumberUtils.intValue((BigDecimal)object[8]) == 1);
                sorterJobListAttributes.setCtrljobId((String) object[9]);
                if (log.isDebugEnabled()) {
                    log.debug("Get ComponentJob Infomation by sorterJobID");
                }
                List<Info.SorterComponentJobListAttributes> objSorterComponentJobInfoGetDROutRetCode = this.sorterComponentJobInfoGetDR(objCommon, sorterJobListAttributes.getSorterJobID(), false);
                sorterJobListAttributes.setSorterComponentJobListAttributesList(objSorterComponentJobInfoGetDROutRetCode);

                if (log.isDebugEnabled()) {
                    log.debug("get postAct informaiton by sortJobID");
                }
                if (CimStringUtils.equals(SorterType.Action.WaferStart.getValue(),
                        objSorterComponentJobInfoGetDROutRetCode.get(0).getActionCode())
                        || CimStringUtils.equals(SorterType.Action.Combine.getValue(),
                        objSorterComponentJobInfoGetDROutRetCode.get(0).getActionCode())
                        || CimStringUtils.equals(SorterType.Action.Separate.getValue(),
                        objSorterComponentJobInfoGetDROutRetCode.get(0).getActionCode())) {
                    Info.SortJobPostAct postAct = this.getPostAct((String) object[0]);
                    sorterJobListAttributes.setPostAct(postAct);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("sorterJob information is returned in order of the priority of sorterJob.");
        }
        String tempEquipmentID = null;
        if (!ObjectIdentifier.isEmptyWithValue(equipmentID)) {
            tempEquipmentID = BaseStaticMethod.strrchr(equipmentID.getValue(), "%");
        }
        //此处原逻辑是只有eqp一个条件时，才对sortJob进行排序
        if (//!CimObjectUtils.isEmptyWithValue(equipmentID) &&      //千岛二期 存在查询所有设备的sj的情况
                ObjectIdentifier.isEmptyWithValue(createUser)
                        && ObjectIdentifier.isEmptyWithValue(sorterJobID)
                        && CimStringUtils.isEmpty(tempEquipmentID)
                        && ObjectIdentifier.isEmptyWithValue(lotID)
                        && ObjectIdentifier.isEmptyWithValue(carrierID)) {
            List<Info.SortJobListAttributes> tmpSortJobListAttributesList = new ArrayList<>();

            if (log.isDebugEnabled()) {
                log.debug("after grouping by Equipment, sort by preSortJobID");
            }
            Map<ObjectIdentifier, List<Info.SortJobListAttributes>> sortJobMap = sorterJobListAttributesList.stream()
                    .collect(Collectors.groupingBy(Info.SortJobListAttributes::getEquipmentID));
            sortJobMap.forEach((key, sortJobs) -> {
                String tmpJobID = "";
                for (Info.SortJobListAttributes sortJobListAttributes : sortJobs) {
                    if (ObjectIdentifier.isEmptyWithValue(sortJobListAttributes.getPreSorterJobID())) {
                        tmpSortJobListAttributesList.add(sortJobListAttributes);
                        tmpJobID = sortJobListAttributes.getSorterJobID().getValue();
                        break;
                    }
                }
                int s_len = CimArrayUtils.getSize(sortJobs);
                for (int i = 1; i < s_len; i++) {
                    for (int k = 0; k < s_len; k++) {
                        if (CimStringUtils.equals(sortJobs.get(k).getPreSorterJobID().getValue(), tmpJobID)) {
                            tmpSortJobListAttributesList.add(sortJobs.get(k));
                            tmpJobID = sortJobs.get(k).getSorterJobID().getValue();
                            break;
                        }
                    }
                }
            });
            sorterJobListAttributesList = tmpSortJobListAttributesList;
        }
        return sorterJobListAttributesList;
    }

    public List<Info.SorterComponentJobListAttributes> sorterComponentJobInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterJobID, Boolean completeFlag) {
        if (log.isDebugEnabled()) {
            log.debug("step - check input param");
        }
        CimSorterJob sorterJob = baseCoreFactory.getBO(CimSorterJob.class, sorterJobID);
        Validations.check(sorterJob == null, retCodeConfigEx.getNotFoundSorterjob(), sorterJobID);

        if (log.isDebugEnabled()) {
            log.debug("step - get all componentJob from sorterJob");
        }
        SorterJobDTO.SortJobInfo sortJobInfo = sorterJob.getSortJobInfo();

        if (log.isDebugEnabled()) {
            log.debug("step - the assembly data");
        }
        List<SorterJobDTO.SorterjobComponent> sorterjobComponents = sortJobInfo.getSorterjobComponents();
        List<Info.SorterComponentJobListAttributes> sorterComponentJobListAttributesList = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(sorterjobComponents)) {
            for (SorterJobDTO.SorterjobComponent component : sorterjobComponents) {
                Info.SorterComponentJobListAttributes sorterComponentJobListAttributes = new Info.SorterComponentJobListAttributes();
                if (completeFlag && BizConstant.RUNCARD_INSTRUCTTION_COMPLETE.equals(component.getComponentJobStatus())) {
                    continue;
                } else {
                    sorterComponentJobListAttributesList.add(sorterComponentJobListAttributes);
                    sorterComponentJobListAttributes.setSorterComponentJobID(component.getComponentJobID());
                    sorterComponentJobListAttributes.setComponentSorterJobStatus(component.getComponentJobStatus());
                    sorterComponentJobListAttributes.setOriginalCarrierID(component.getSourceCassetteID());
                    sorterComponentJobListAttributes.setDestinationCarrierID(component.getDestCassetteID());
                    sorterComponentJobListAttributes.setOriginalPortID(component.getSourcePortID());
                    sorterComponentJobListAttributes.setDestinationPortID(component.getDestPortID());
                    sorterComponentJobListAttributes.setActionCode(component.getActionCode());
                    sorterComponentJobListAttributes.setPreSorterComponentJobID(ObjectIdentifier
                            .buildWithValue(component.getPreviousComponentJobID()));
                    sorterComponentJobListAttributes.setRequestUserID(component.getReqUserID());
                    sorterComponentJobListAttributes.setRequestTimeStamp(String.valueOf(component.getReqTimeStamp()));
                    sorterComponentJobListAttributes.setReplyTimeStamp(component.getReplyTimeStamp() == null ? null
                            : component.getReplyTimeStamp().toString());

                    if (log.isDebugEnabled()) {
                        log.debug("Get Xfer State for original cassette");
                    }
                    String originalCarrierID = ObjectIdentifier.fetchValue(component.getSourceCassetteID());
                    if (!SorterHandler.containsFOSB(component.getSourceCassetteID())) {
                        Outputs.ObjCassetteTransferInfoGetDROut objCassetteTransferInfoGetDROut1 = this.cassetteTransferInfoGetDR(objCommon, ObjectIdentifier.buildWithValue(originalCarrierID));
                        sorterComponentJobListAttributes.setOriginalCarrierTransferState(objCassetteTransferInfoGetDROut1.getTransferStatus());
                        sorterComponentJobListAttributes.setOriginalCarrierEquipmentID(objCassetteTransferInfoGetDROut1.getEquipmentID());
                        sorterComponentJobListAttributes.setOriginalCarrierStockerID(objCassetteTransferInfoGetDROut1.getStockerID());
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Get Xfer State for destination cassette");
                    }
                    String destinationCarrierID = ObjectIdentifier.fetchValue(component.getDestCassetteID());
                    if (!SorterHandler.containsFOSB(component.getDestCassetteID())) {
                        Outputs.ObjCassetteTransferInfoGetDROut objCassetteTransferInfoGetDROut2 = this.cassetteTransferInfoGetDR(objCommon, ObjectIdentifier.buildWithValue(destinationCarrierID));
                        sorterComponentJobListAttributes.setDestinationCarrierTranferStatus(objCassetteTransferInfoGetDROut2.getTransferStatus());
                        sorterComponentJobListAttributes.setDestinationCarrierEquipmentID(objCassetteTransferInfoGetDROut2.getEquipmentID());
                        sorterComponentJobListAttributes.setDestinationCarrierStockerID(objCassetteTransferInfoGetDROut2.getStockerID());
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("query all sort job slot map");
                    }
                    List<SorterJobDTO.SlotMapInfo> cimSortJobSlotMapDOList = component.getSlotMapInfoList();
                    if (!CimObjectUtils.isEmpty(cimSortJobSlotMapDOList)) {
                        List<Info.WaferSorterSlotMap> waferSorterSlotMapList = new ArrayList<>();
                        sorterComponentJobListAttributes.setWaferSorterSlotMapList(waferSorterSlotMapList);
                        for (SorterJobDTO.SlotMapInfo slotMapInfo : cimSortJobSlotMapDOList) {
                            Info.WaferSorterSlotMap waferSorterSlotMap = new Info.WaferSorterSlotMap();
                            if (completeFlag && !slotMapInfo.getDirection().equals(BizConstant.SP_USERDATA_ORIG_MM)) {
                                continue;
                            } else {
                                waferSorterSlotMapList.add(waferSorterSlotMap);
                            }
                            waferSorterSlotMap.setWaferID(slotMapInfo.getWaferID());
                            waferSorterSlotMap.setLotID(slotMapInfo.getLotID());
                            waferSorterSlotMap.setDestinationSlotNumber(CimNumberUtils.intValue(slotMapInfo.getDestPosition()));
                            waferSorterSlotMap.setOriginalSlotNumber(CimNumberUtils.intValue(slotMapInfo.getSourcePosition()));
                            waferSorterSlotMap.setDirection(slotMapInfo.getDirection());
                            waferSorterSlotMap.setAliasName(slotMapInfo.getAliasName());
                        }
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("step - priority sort");
        }
        List<Info.SorterComponentJobListAttributes> tempSorterComponent = new ArrayList();
        String tempComponentID = "";
        for (Info.SorterComponentJobListAttributes componentJobListAttributes : sorterComponentJobListAttributesList) {
            if (ObjectIdentifier.isEmptyWithValue(componentJobListAttributes.getPreSorterComponentJobID())) {
                tempSorterComponent.add(componentJobListAttributes);
                tempComponentID = componentJobListAttributes.getSorterComponentJobID().getValue();
                break;
            }
        }
        int s_len = CimArrayUtils.getSize(sorterComponentJobListAttributesList);
        for (int i = 1; i < s_len; i++) {
            for (int k = 0; k < s_len; k++) {
                if (CimStringUtils.equals(sorterComponentJobListAttributesList.get(k).getPreSorterComponentJobID().getValue(), tempComponentID)) {
                    tempSorterComponent.add(sorterComponentJobListAttributesList.get(k));
                    tempComponentID = sorterComponentJobListAttributesList.get(k).getSorterComponentJobID().getValue();
                    break;
                }
            }
        }
        return tempSorterComponent;
    }

    public Outputs.ObjCassetteTransferInfoGetDROut cassetteTransferInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        Outputs.ObjCassetteTransferInfoGetDROut objCassetteTransferInfoGetDROut = new Outputs.ObjCassetteTransferInfoGetDROut();
        if (!SorterHandler.containsFOSB(cassetteID)) {
            CimCassetteDO cimCassetteExample = new CimCassetteDO();
            cimCassetteExample.setCassetteID(ObjectIdentifier.fetchValue(cassetteID));
            List<CimCassetteDO> cassetteList = cimJpaRepository.findAll(Example.of(cimCassetteExample));
            Validations.check(CimObjectUtils.isEmpty(cassetteList), retCodeConfig.getNotFoundCassette());
            CimCassetteDO cimCassetteDO = cassetteList.get(0);
            //----- Set out structure -----//
            String transferState = cimCassetteDO.getTransferState();
            objCassetteTransferInfoGetDROut.setTransferStatus(transferState);
            if (transferState.startsWith("E")) {
                objCassetteTransferInfoGetDROut.setEquipmentID(new ObjectIdentifier(cimCassetteDO.getEquipmentID()));
            } else {
                objCassetteTransferInfoGetDROut.setStockerID(new ObjectIdentifier(cimCassetteDO.getEquipmentID()));
            }
        }
        return objCassetteTransferInfoGetDROut;
    }

    @Override
    public void waferSorterActionListInsertDR(Infos.ObjCommon objCommon, List<Info.SortActionAttributes> strWaferSorterActionListSequence, ObjectIdentifier equipmentID) {
        if (log.isDebugEnabled()) {
            log.debug("step1 - Get object reference of Equipment");
        }
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check (CimObjectUtils.isEmpty(aPosMachine), new OmCode(retCodeConfig.getNotFoundEqp(),
                ObjectIdentifier.fetchValue(equipmentID)));

        if (log.isDebugEnabled()) {
            log.debug("step2 - Get Equipment attributes");
        }
        String strMachineCategory = aPosMachine.getCategory();
        Validations.check(CimStringUtils.isEmpty(strMachineCategory), retCodeConfig.getNotFoundCategory());

        if (log.isDebugEnabled()) {
            log.debug("step3 - Check Equipment Type");
        }
        Validations.check(!CimObjectUtils.equals(BizConstant.SP_MC_CATEGORY_WAFERSORTER,strMachineCategory),
                retCodeConfig.getMachineTypeNotSorter(),strMachineCategory);

        if (log.isDebugEnabled()) {
            log.debug("step4 - RECORD DELETE");
        }
        String fsEquipmentID = ObjectIdentifier.fetchValue(equipmentID);
        actionCodeManager.removeActionByEqpID(fsEquipmentID);

        if (log.isDebugEnabled()) {
            log.debug("step5 - RECORD INSERT");
        }
        if (CimArrayUtils.isNotEmpty(strWaferSorterActionListSequence)) {
            for (Info.SortActionAttributes waferSorterAction : strWaferSorterActionListSequence) {
                if (CimStringUtils.equals(SorterType.Action.getSpecifiedValue(waferSorterAction
                        .getActionCode()),waferSorterAction.getActionCode())) {
                    try {
                        Integer integer = Integer.valueOf(waferSorterAction.getPhysicalRecipeID());
                    }catch (Exception e){
                        Validations.check(retCodeConfigEx.getActionCodeRotateAngle());
                    }
                }
                CimActionRecipe actionCode = actionCodeManager.createActionCode();
                ActionRecipeDTO.ActionRecipe actionRecipe = new ActionRecipeDTO.ActionRecipe();
                actionRecipe.setEqpId(waferSorterAction.getEquipmentID());
                actionRecipe.setActionCode(waferSorterAction.getActionCode());
                actionRecipe.setPhysicalRecipeId(waferSorterAction.getPhysicalRecipeID());
                actionRecipe.setUserId(waferSorterAction.getUserID());
                actionCode.registeredActionCode(actionRecipe);
            }
        }
    }

    @Override
    public String sorterSorterJobLockDR(Infos.ObjCommon objCommon, Info.SorterSorterJobLockDRIn sorterSorterJobLockDRIn) {
        //---------------------------
        //  Check input parameter
        //---------------------------
        Validations.check(sorterSorterJobLockDRIn.getLockType() != BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ
                && sorterSorterJobLockDRIn.getLockType() != BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, retCodeConfig.getInvalidParameter());

        Validations.check(ObjectIdentifier.isEmpty(sorterSorterJobLockDRIn.getSorterJobID())
                && ObjectIdentifier.isEmpty(sorterSorterJobLockDRIn.getSorterComponentJobID())
                && ObjectIdentifier.isEmpty(sorterSorterJobLockDRIn.getCassetteID()), retCodeConfig.getInvalidParameter());


        //---------------------------
        //  Lock OMSORTJOB
        //---------------------------
        /*
        String sorterJobID;
        // Get sorterJobID
        if (ObjectUtils.isEmpty(sorterSorterJobLockDRIn.getSorterJobID()) ) {
            log.info("sorterJobID is blank");

            if ( ObjectUtils.isEmpty(sorterSorterJobLockDRIn.getSorterComponentJobID())) {
                log.info("sorterComponentJobID is not blank");

                // Get sorterJobID from sorterComponentJobID
                EXEC SQL SELECT SORTER_JOB_ID
                INTO   :hFSSORTJOBSORTER_JOB_ID
                FROM   OMSORTJOB_COMP
                WHERE  COMPONENT_JOB_ID = :hFSSORTJOB_COMPONENTCOMPONENT_JOB_ID
                FETCH FIRST ROWS ONLY;
            }
            else
            {
                log.info("sorterComponentJobID is blank");

                // Get sorterJobID from cassetteID
                EXEC SQL SELECT SORTER_JOB_ID
                INTO   :hFSSORTJOBSORTER_JOB_ID
                FROM   OMSORTJOB_COMP
                WHERE  SRC_CAST_ID  = :hFSSORTJOB_COMPONENTSRC_CAST_ID
                OR  DEST_CAST_ID = :hFSSORTJOB_COMPONENTDEST_CAST_ID
                FETCH FIRST ROWS ONLY;
            }
            PPT_DR_CHECK_SQL_ERROR_KEY( strSorter_sorterJob_LockDR_out,
                    sorter_sorterJob_LockDR,
                    MSG_NOT_FOUND_FSSORTJOB, RC_NOT_FOUND_FSSORTJOB,
                    SQL SELECT (OMSORTJOB_COMP), "*" );
        }


        log.info("sorterJob_Lock: Lock Start...");
        // READ Lock Record
        if ( sorterSorterJobLockDRIn.getLockType() == BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ ) {
            log.info("lockType = SP_ObjectLock_LockType_READ");

            EXEC SQL SELECT SORTER_JOB_ID INTO :hFSSORTJOBSORTER_JOB_ID FROM OMSORTJOB
            WHERE SORTER_JOB_ID = :hFSSORTJOBSORTER_JOB_ID FOR READ ONLY WITH RS USE AND KEEP SHARE LOCKS;
        }
        // WRITE Lock Record
        else
        {
            log.info("lockType = SP_ObjectLock_LockType_WRITE");

            EXEC SQL SELECT SORTER_JOB_ID INTO :hFSSORTJOBSORTER_JOB_ID FROM OMSORTJOB
            WHERE SORTER_JOB_ID = :hFSSORTJOBSORTER_JOB_ID FOR UPDATE WITH RS USE AND KEEP EXCLUSIVE LOCKS;
        }
        PPT_DR_CHECK_SQL_ERROR_KEY( strSorter_sorterJob_LockDR_out,
                sorter_sorterJob_LockDR,
                MSG_NOT_FOUND_FSSORTJOB, RC_NOT_FOUND_FSSORTJOB,
                SQL SELECT (OMSORTJOB), hFSSORTJOBSORTER_JOB_ID );*/
        log.info("sorterJob_Lock: Lock Completed...");
        //----------------------
        //  Return to Caller
        //----------------------
        return null;
    }

    @Override
    public Info.ObjSorterComponentJobInfoGetByComponentJobIDDROut sorterComponentJobInfoGetByComponentJobIDDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterComponentJobID) {
        if (log.isDebugEnabled()) {
            log.debug("get sortJob and componentJob params");
        }
        CimSortJobComponentDO cimSortJobComponentExam = new CimSortJobComponentDO();
        cimSortJobComponentExam.setComponentJobID(ObjectIdentifier.fetchValue(sorterComponentJobID));
        CimSortJobComponentDO componentDO = cimJpaRepository.findOne(Example.of(cimSortJobComponentExam)).orElse(null);
        Validations.check(componentDO == null, retCodeConfigEx.getNotFoundSorterjobComponent(), sorterComponentJobID);
        CimSorterJob sorterJob = baseCoreFactory.getBO(CimSorterJob.class, componentDO.getReferenceKey());
        Validations.check(sorterJob == null, retCodeConfigEx.getNotFoundSorterjob(),componentDO.getReferenceKey());

        if (log.isDebugEnabled()) {
            log.debug("assemble data");
        }
        Info.ObjSorterComponentJobInfoGetByComponentJobIDDROut out = new Info.ObjSorterComponentJobInfoGetByComponentJobIDDROut();
        out.setSorterJobID(ObjectIdentifier.buildWithValue(sorterJob.getIdentifier()));

        Info.ComponentJob sorterComponentJob = new Info.ComponentJob();
        out.setComponentJob(sorterComponentJob);

        sorterComponentJob.setComponentJobID(componentDO.getComponentJobID());
        sorterComponentJob.setOriginalCassetteID(ObjectIdentifier.buildWithValue(componentDO.getSourceCassetteID()));
        sorterComponentJob.setOriginalPortID(ObjectIdentifier.buildWithValue(componentDO.getSourcePortID()));
        sorterComponentJob.setDestinationCassetteID(ObjectIdentifier.buildWithValue(componentDO.getDestCassetteID()));
        sorterComponentJob.setDestinationPortID(ObjectIdentifier.buildWithValue(componentDO.getDestPortID()));
        sorterComponentJob.setComponentJobStatus(componentDO.getComponentJobStatus());
        sorterComponentJob.setPreSorterComponentJobID(ObjectIdentifier.buildWithValue(componentDO.getPreviousComponentJobID()));
        sorterComponentJob.setReplyTimeStamp(componentDO.getReplyTimeStamp() == null ? null : componentDO.getReplyTimeStamp().toString());
        sorterComponentJob.setActionCode(componentDO.getActionCode());
        return out;
    }

    @Override
    public void sorterLinkedJobUpdateDR(Infos.ObjCommon objCommon, Info.SorterLinkedJobUpdateDRIn in) {
        Validations.check(0 == CimArrayUtils.getSize(in.getJobIDs()), retCodeConfig.getInvalidParameter());

        String tmpJobID = null;
        if (log.isDebugEnabled()) {
            log.debug("Job Sequence Check");
        }
        if (CimStringUtils.equals(in.getJobType(), BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB)) {
            List<SorterJobDTO.SortJob> sortJobs = sorterJobManager.allSortJobInfos(ObjectIdentifier.fetchValue(in.getJobIDs().get(0)));
            Validations.check(CimArrayUtils.getSize(sortJobs) != CimArrayUtils.getSize(in.getJobIDs()),
                    retCodeConfigEx.getSorterInvalidParameter());

            for (ObjectIdentifier jobID : in.getJobIDs()) {
                CimSorterJob sj = baseCoreFactory.getBO(CimSorterJob.class, jobID);
                sj.setPreSortJob(tmpJobID);
                tmpJobID = ObjectIdentifier.fetchValue(jobID);
            }
        } else if (CimStringUtils.equals(in.getJobType(), BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB)) {
            CimSorterJob cimSortJobByComponentJobID = sorterJobManager
                    .getCimSortJobByComponentJobID(ObjectIdentifier.fetchValue(in.getJobIDs().get(0)));

            List<SorterJobDTO.SorterjobComponent> sorterjobComponents = cimSortJobByComponentJobID.getSortJobInfo().getSorterjobComponents();
            Validations.check(CimArrayUtils.getSize(sorterjobComponents) != CimArrayUtils.getSize(in.getJobIDs()),
                    retCodeConfigEx.getSorterInvalidParameter());

            for (ObjectIdentifier jobID : in.getJobIDs()) {
                CimSorterJob sj = sorterJobManager.getCimSortJobByComponentJobID(ObjectIdentifier.fetchValue(jobID));
                sj.setPreComponentJob(tmpJobID, ObjectIdentifier.fetchValue(jobID));
                tmpJobID = ObjectIdentifier.fetchValue(jobID);
            }
        } else {
            Validations.check(retCodeConfigEx.getInvalidSorterJobType());
        }
    }

    @Override
    public void sorterAndCompnentJobStatusUpdateDR(Infos.ObjCommon objCommon, Info.SorterComponentJobType sorterComponentJobType) {
        String jobType = sorterComponentJobType.getJobType();
        String jobStatus = sorterComponentJobType.getJobStatus();
        ObjectIdentifier sortJobID = sorterComponentJobType.getSortJobID();
        ObjectIdentifier componentJobID = sorterComponentJobType.getComponentJobID();
        CimSorterJob sorterJob = baseCoreFactory.getBO(CimSorterJob.class, sortJobID);
        Validations.check(sorterJob == null, retCodeConfigEx.getNotFoundSorterjob(), sortJobID);
        SorterJobDTO.ModifyJobStatus modifyJobStatus =new SorterJobDTO.ModifyJobStatus();
        modifyJobStatus.setStatus(jobStatus);
        if (CimStringUtils.equals(SorterType.JobType.SorterJob.getValue(), jobType)) {
            modifyJobStatus.setModifyTime(new Timestamp(System.currentTimeMillis()));
            modifyJobStatus.setModifyUserId(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
            modifyJobStatus.setModifyUserObj(ObjectIdentifier.fetchReferenceKey(objCommon.getUser().getUserID()));
            sorterJob.setSortJobStatus(modifyJobStatus);
        }else if (CimStringUtils.equals(SorterType.JobType.ComponentJob.getValue(), jobType)) {
            modifyJobStatus.setJobId(ObjectIdentifier.fetchValue(componentJobID));
            sorterJob.setComponentJobStaus(modifyJobStatus);
        }else {
            Validations.check(retCodeConfig.getInvalidParameter());
        }
    }

    @Override
    public void sorterJobInfoDeleteDR(Infos.ObjCommon objCommon, Info.SorterComponentJobDeleteDRIn sjDeleteParam) {
        if (log.isDebugEnabled()) {
            log.debug("check sortJob id exits");
        }
        CimSorterJob sorterJob = baseCoreFactory.getBO(CimSorterJob.class, sjDeleteParam.getSorterJobID());
        Validations.check(sorterJob == null, retCodeConfigEx.getNotFoundSorterjob(), sjDeleteParam.getSorterJobID());
        List<ObjectIdentifier> componentJobIDseq = sjDeleteParam.getComponentJobIDseq();
        if (CimArrayUtils.isEmpty(componentJobIDseq)) {
            sorterJob.remove();
        }else {
            for (ObjectIdentifier componentJob : componentJobIDseq) {
                sorterJob.removeComponentJobAndSlotMap(ObjectIdentifier.fetchValue(componentJob));
            }
        }
    }

    @Override
    public void waferSorterCheckConditionForAction(Infos.ObjCommon objCommon, Info.SortJobInfo sortJobInfo) {
        //---------------------
        // Initialize
        //---------------------

        //----------------------------
        // No Parameters are specified
        //--------------------------------------------------
        // If ActionCode is not SP_Sorter_End, then error
        //--------------------------------------------------
        Info.ComponentJob componentJob = sortJobInfo.getComponentJob();
        String actionCode = componentJob.getActionCode();
        List<Info.WaferSorterSlotMap> waferList = componentJob.getWaferList();
        if (!actionCode.equals(SorterType.Action.RFIDRead.getValue())
                && !actionCode.equals(SorterType.Action.RFIDWrite.getValue())) {
            Validations.check(CimArrayUtils.isEmpty(waferList), retCodeConfig.getInvalidParameter());
        }
        //=======================================================
        // In-Parameter check routine
        // --------------------------
        // These Parameters are verified
        //     actionCode/equipmentID/equipment category/portGroup
        //=======================================================
        //----------------------------------------------------------------------------------
        //    Check ActionCode is valid
        //    => SP_Sorter_Read/SP_Sorter_MiniRead/SP_Sorter_PositionChange/SP_Sorter_JustIn
        //       SP_Sorter_JustOut/SP_Sorter_Scrap/SP_Sorter_End is only valid
        //----------------------------------------------------------------------------------
        ObjectIdentifier equipmentID = sortJobInfo.getEquipmentID();
        log.info("waferSorter_CheckConditionForAction Specified ActionCode is : {}",actionCode);

        //----------------------------------------------------------------------------------
        //    Check Equipment Category
        //    => SP_Mc_Category_WaferSorter is only valid
        //----------------------------------------------------------------------------------
        log.info("Getting Equipment Category Information ");

        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aPosMachine, retCodeConfig.getNotFoundEqp(), objCommon.getTransactionID());

        String strMachneCategory = aPosMachine.getCategory();

        log.info("Check Equipment Category");
        Validations.check( !CimStringUtils.equals(strMachneCategory,BizConstant.SP_MC_CATEGORY_WAFERSORTER),retCodeConfig.getMachineTypeNotSorter());

        //-------------------------------------------------------
        //    Check PortGroup
        //    => PortGroupame should be same as inpara
        //-------------------------------------------------------
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        int lenPortInfo = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());

        ObjectIdentifier operationModeID = null;
        ObjectIdentifier portID = null;
        String portGroup = sortJobInfo.getPortGroup();
        for (int i=0; i < lenPortInfo ; i++ ) {
            // ---------------------------------------------
            // Check On-Line Mode of Port
            // ---------------------------------------------
            Validations.check(i == 0 && !CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(i).getOnlineMode(),
                    BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE), retCodeConfig.getInvalidPortState());

            // ---------------------------------------------
            // Check In-Para Port Group is on EQP Port Group
            // ---------------------------------------------
            if(CimStringUtils.equals( eqpPortInfo.getEqpPortStatuses().get(i).getPortGroup(),portGroup)) {
                operationModeID =  eqpPortInfo.getEqpPortStatuses().get(i).getOperationModeID(); //PSIV00000956
                portID =  eqpPortInfo.getEqpPortStatuses().get(i).getPortID();                   //PSIV00000956
                break;
            }

            //--------------------
            // Case in Final Loop
            //--------------------
            Validations.check(i == (lenPortInfo - 1), retCodeConfigEx.getPortPortgroupUnmatch());
        }

        Validations.check ( !ObjectIdentifier.equalsWithValue(operationModeID, SorterType.OperationMode.Auto_1.getValue())
                && !ObjectIdentifier.equalsWithValue(operationModeID, SorterType.OperationMode.Auto_2.getValue())
                  && !ObjectIdentifier.equalsWithValue(operationModeID, SorterType.OperationMode.Semi_1.getValue()),
                retCodeConfigEx.getInvalidPortOperationMode(),portID,operationModeID);

        //-------------------------------------------------------
        // Check PhysicalRecipeID
        // => physicalRecipeID should be input except SP_Sorter_End
        //-------------------------------------------------------
        //------------------------------------
        //   Check equipment availability
        //------------------------------------
        equipmentMethod.equipmentCheckAvail(objCommon, equipmentID);

        //======================================================
        //    Each Check of Structure by ActionCode
        //======================================================

        //---------------------------------------------------------------------------------------------
        // Destination SlotNumber Check
        //---------------------------------------------------------------------------------------------
        boolean destinationCassetteFound = true;
        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, componentJob.getDestinationCassetteID());
        if (null == aCassette) {
            destinationCassetteFound = false;
        }

        //---------------------------------------
        // Getting Original Cassette Information
        //---------------------------------------
        boolean originalCassetteFound = true;
        aCassette = baseCoreFactory.getBO(CimCassette.class,componentJob.getOriginalCassetteID());
        if (null == aCassette) {
            originalCassetteFound = false;
        }
        //---------------------------------------------------------------------------------------------
        // wafer start
        //---------------------------------------------------------------------------------------------
        //    Checking Items :
        //        bDestinationCassetteManagedBySiView must be TRUE
        //        bOriginalCassetteManagedBySiView    must be false
        //                         | Original         |     Destination
        //             ------------+------------------+-------------------
        //             SiView Flag |     false        |        TRUE
        //             Cassette    | MM Un-Registered |    MM Registered
        //---------------------------------------------------------------------------------------------
        if (CimStringUtils.equals(actionCode,SorterType.Action.WaferStart.getValue())) {
            Validations.check(   !destinationCassetteFound || originalCassetteFound ,
                    retCodeConfigEx.getInvalidSiviewflagForJustin());
        } else {
            Validations.check(!destinationCassetteFound || !originalCassetteFound, retCodeConfigEx.getInvalidSiviewflagForWaferidposchange());
        }

        // --------------------------------------------------------------
        // Check All Data in Structure Sequence
        // --------------------------------------------------------------
        if (!actionCode.equals(SorterType.Action.RFIDRead.getValue())
                && !actionCode.equals(SorterType.Action.RFIDWrite.getValue())) {
            for (int i = 0; i < CimArrayUtils.getSize(waferList); i++) {
                if (!CimStringUtils.equals(actionCode,SorterType.Action.WaferStart.getValue())) {
                    // -------------------------------------------------------------
                    // WaferID Existence Check
                    //   => WaferID Should be existed
                    // -------------------------------------------------------------
                    CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class,waferList.get(i).getWaferID());
                    Validations.check(null == aPosWafer, retCodeConfig.getNotFoundWafer());

                    // -------------------------------------------------------------
                    // Getting Wafers
                    // => Then Error
                    // -------------------------------------------------------------
                    if (log.isDebugEnabled()) {
                        log.debug( "Checking Current Carrier Information");
                    }
                    MaterialContainer aMtrlCntnr = aPosWafer.getMaterialContainer();
                    CimCassette aCurCassette = (CimCassette) aMtrlCntnr;

                    //--------------------------------------------------------------
                    // If Carrier information is not found then Error except Just-IN
                    //--------------------------------------------------------------
                    if (log.isDebugEnabled()) {
                        log.debug( "Carrier Information Copy");
                    }
                    if(null == aCurCassette){
                        Validations.check(!SorterType.Action.WaferStart.equals(actionCode), retCodeConfig.getNotFoundCassette());
                    }

                    // -------------------------------------------------------------
                    // GetLotID for Future Check
                    // -------------------------------------------------------------
                    CimLot aTmpLot = (CimLot)aPosWafer.getLot();
                    Validations.check(null == aTmpLot,retCodeConfig.getNotFoundLot());
                }

                //---------------------------------------------------------------------------------------------
                // Destination SlotNumber Check
                //---------------------------------------------------------------------------------------------
                int maximumWafersInALot = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getIntValue();
                Validations.check( (0 > waferList.get(i).getDestinationSlotNumber())||
                                (waferList.get(i).getDestinationSlotNumber() > maximumWafersInALot),
                        retCodeConfigEx.getInvalidWaferPosition());

                Validations.check((CimStringUtils.equals( actionCode , BizConstant.SP_SORTER_AUTOSORTING)||
                                CimStringUtils.equals( actionCode , BizConstant.SP_SORTER_LOTTRANSFER)||
                                CimStringUtils.equals( actionCode , BizConstant.SP_SORTER_WAFEREND)) && ( (1 > waferList.get(i).getDestinationSlotNumber())||
                                (waferList.get(i).getDestinationSlotNumber() > maximumWafersInALot)),
                        retCodeConfigEx.getInvalidWaferPosition());


                // ------------------------------------------------------------------------
                // Carrier/Port/Slot Duplicate Check
                // Carrier/Port/Slot should be unique because each slot can have only one wafer
                // ------------------------------------------------------------------------
                for(int j=0;j<CimArrayUtils.getSize(waferList);j++) {
                    // No Check Same Data
                    if( i==j ) {
                        continue;
                    }
                    //---------------------------------------------------------
                    // Check Destination Information
                    // ActionCode : ALL
                    //---------------------------------------------------------
                    // -------------------
                    // Case PortID is Same
                    // -------------------
                    if(ObjectIdentifier.equalsWithValue(componentJob.getDestinationPortID(),
                            componentJob.getDestinationPortID())) {
                        // --------------------
                        // Case Carrier is same
                        // --------------------
                        if(ObjectIdentifier.equals(componentJob.getDestinationCassetteID(),
                                componentJob.getDestinationCassetteID())) {
                            // -----------------------------------
                            // If Slot Number is same then Error
                            // -----------------------------------
                            Validations.check( waferList.get(i).getDestinationSlotNumber().intValue() ==
                                    waferList.get(j).getDestinationSlotNumber().intValue(), retCodeConfigEx.getDuplicateLocation());
                        }
                        // --------------------------------------------
                        // Carrier is not same on same port then error
                        // --------------------------------------------
                        else {
                            log.info(" waferSorter_CheckConditionForAction, Different Destination CARRIER  specified in same Port ");
                            Validations.check(retCodeConfigEx.getDifferentCarrierInPort());
                        }
                    }
                    // -------------------------
                    // Case Port is not Same
                    // -------------------------
                    else {
                        // -------------------------
                        // Case Carrier is not Same
                        // -------------------------
                        Validations.check(ObjectIdentifier.equalsWithValue(componentJob.getDestinationCassetteID(),
                                componentJob.getDestinationCassetteID()), retCodeConfigEx.getDuplicateCarrier());
                    }

                    if (!SorterType.Action.WaferStart.equals(actionCode)) {
                        //---------------------------------------------------------
                        // Check Original Information
                        // ActionCode : PositionChange/JustIn/JustOut/Scrap
                        //---------------------------------------------------------
                        if(ObjectIdentifier.equalsWithValue(componentJob.getOriginalPortID(),
                                componentJob.getOriginalPortID())) {
                            // --------------------
                            // Case Carrier is same
                            // --------------------
                            if(ObjectIdentifier.equalsWithValue(componentJob.getOriginalCassetteID(), componentJob.getOriginalCassetteID())) {
                                // -----------------------------------
                                // If Slot Number is same then Error
                                // -----------------------------------
                                Validations.check( waferList.get(i).getOriginalSlotNumber().intValue() ==
                                        waferList.get(j).getOriginalSlotNumber().intValue() , retCodeConfigEx.getDuplicateLocation());
                            }
                            // --------------------------------------------
                            // Carrier is not same on same port then error
                            // --------------------------------------------
                            else {
                                log.info(" waferSorter_CheckConditionForAction, Different Original CARRIER  specified in same Port ");
                                throw new ServiceException(retCodeConfigEx.getDuplicateCarrier());
                            }
                        }
                        // -------------------------
                        // Case Carrier is not Same
                        // -------------------------
                        else {
                            Validations.check(ObjectIdentifier.equalsWithValue(componentJob.getOriginalCassetteID(),
                                    componentJob.getOriginalCassetteID()), retCodeConfigEx.getDifferentCarrierInPort());
                        }
                    }
                }
            }
        }

        //=========================================================================
        // Prepare AnyData for sorter_waferTransferInfo_Verify
        // because it needs all waferdata in specified lot
        // So this routine adds waferinformation to strWaferTransferSequence
        // Invoke it only case PositionChange/JustIn/JustOut/Scrap
        //=========================================================================
        //---------------------------------------------------------------------------------------------
        // PositionChange/JustIn/JustOut/Scrap
        //---------------------------------------------------------------------------------------------
        //    Checking Items :
        //        Send Parameters to sorter_waferTransferInfo_Verify and get return code
        //---------------------------------------------------------------------------------------------
        //-----------------------------------------------------
        // Preparation for Sorter transfer buffer
        //-----------------------------------------------------

        log.info(" waferSorter_CheckConditionForAction, Check Parameters using sorter_waferTransferInfo_Verify" );

        //---------------------------------------------------------------
        // Check Strusture data for wafer transfer (Sorter operation)
        //---------------------------------------------------------------
        log.info( " Executing sorter_waferTransferInfo_Verify" );
        if (!SorterType.Action.WaferStart.getValue().equals(actionCode)
                && !SorterType.Action.RFIDWrite.getValue().equals(actionCode)
                && !SorterType.Action.RFIDRead.getValue().equals(actionCode)) {
            this.sorterWaferTransferInfoVerify(objCommon, sortJobInfo, SorterType.Action.AdjustByMES.getValue());
        }
        // --------------------------------------------------------------
        // If Error Occures
        // --------------------------------------------------------------
        log.info( "check for sorter_waferTransferInfo_Verify OK");
        //---------------------------------------
        // Check input parameter and
        // Server data condition
        //---------------------------------------
        this.cassetteCheckConditionForWaferSort(objCommon, sortJobInfo);

        log.info( "check for cassette_CheckConditionForWaferSort OK");
    }

    @Override
    public void sorterWaferTransferInfoVerify(Infos.ObjCommon objCommon, Info.SortJobInfo sortJobInfo, String action) {
        //--------------------------------------------------------
        // Check Current Condition of wafer - carrier - position
        //--------------------------------------------------------
        Info.ComponentJob componentJob = sortJobInfo.getComponentJob();
        List<Info.WaferSorterSlotMap> waferList = componentJob.getWaferList();
        int nInputWaferLen = CimArrayUtils.getSize(componentJob.getWaferList());
        List<String> cassetteIDs = new ArrayList<>();
        List<String> lotIDs = new ArrayList<>();
        List<CimLot> aPosLotSeq = new ArrayList<>();
        ObjectIdentifier targetlotID;
        Boolean bWaferFoundFlg ;
        Boolean scrapState;
        for (Info.WaferSorterSlotMap waferSorterSlotMap : waferList) {
            if (ObjectIdentifier.equalsWithValue(componentJob.getDestinationCassetteID(), componentJob.getOriginalCassetteID())) {
                log.info("destinationCassetteID == originalCassetteID");
                continue;
            }
            //---------------------------------------------------
            // Get Target LotID
            //---------------------------------------------------
            CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, waferSorterSlotMap.getWaferID());
            Validations.check(null == aPosWafer, retCodeConfig.getNotFoundWafer());
            scrapState = aPosWafer.isScrap();
            if (CimBooleanUtils.isTrue(scrapState)) {
                log.info("Input wafer is scrapped. Skip check logic.");
                continue;
            }
            //-----------------------------------------------------------
            // Check whether wafer is stacked on other wafer
            // If it is stacked, should not allow wafer sorter operation
            //-----------------------------------------------------------
            boolean bStacked = aPosWafer.isStacked();
            Validations.check(bStacked, retCodeConfig.getInvalidWaferState());
            Lot aTmpLot = aPosWafer.getLot();
            Validations.check(null == aTmpLot, retCodeConfig.getNotFoundLot());
            targetlotID = new ObjectIdentifier(aTmpLot.getIdentifier(), aTmpLot.getPrimaryKey());

            //---------------------------------------------------
            // Get Slot Map On MM Server (originalCassetteID Key)
            //---------------------------------------------------
            List<Infos.WaferMapInCassetteInfo> strCassetteGetWaferMapDROut = cassetteMethod
                    .cassetteGetWaferMapDR(objCommon, componentJob.getOriginalCassetteID());

            if (CimStringUtils.unEqual(SorterType.Action.Separate.getValue(), sortJobInfo.getComponentJob().getActionCode())) {
                int waferMapLen = CimArrayUtils.getSize(strCassetteGetWaferMapDROut);
                for (int j = 0; j < waferMapLen; j++) {
                    Infos.WaferMapInCassetteInfo waferMapInCassetteInfo = strCassetteGetWaferMapDROut.get(j);
                    if (ObjectIdentifier.equalsWithValue(waferMapInCassetteInfo.getLotID(), targetlotID)) {
                        bWaferFoundFlg = false;
                        for (Info.WaferSorterSlotMap sorterSlotMap : waferList) {
                            if (CimBooleanUtils.isTrue(scrapState)) {
                                if (ObjectIdentifier.equalsWithValue(waferMapInCassetteInfo.getWaferID(), sorterSlotMap.getWaferID())
                                        && CimStringUtils.equals(BizConstant.SP_SCRAPSTATE_SCRAP, waferMapInCassetteInfo.getScrapState())) {
                                    log.info("bWaferFoundFlg = TRUE");
                                    bWaferFoundFlg = true;
                                    break;
                                }
                            } else {
                                if (ObjectIdentifier.equalsWithValue(waferMapInCassetteInfo.getWaferID(), sorterSlotMap.getWaferID())) {
                                    log.info("bWaferFoundFlg = TRUE");
                                    bWaferFoundFlg = true;
                                    break;
                                }
                            }
                        }
                        //---------------------------------------------------
                        // Check Exsit WaferID
                        //---------------------------------------------------
                        Validations.check(CimBooleanUtils.isFalse(bWaferFoundFlg), retCodeConfig.getInvalidCassetteLotRelation(),
                                ObjectIdentifier.fetchValue(waferMapInCassetteInfo.getLotID()));
                    }
                }
            }
        }
//            if (nInputWaferLen > 0 && waferMapLen > 0) {
//                for (Info.WaferSorterSlotMap sorterSlotMap : waferList) {
//                    bWaferFoundFlg = false;
//                    for (int j = 0; j < waferMapLen; j++) {
//                        Infos.WaferMapInCassetteInfo waferMapInCassetteInfo = strCassetteGetWaferMapDROut.get(j);
//                        if (CimObjectUtils.equalsWithValue(waferMapInCassetteInfo.getLotID(),targetlotID)) {
//                            if (CimBooleanUtils.isTrue(scrapState)) {
//                                if (CimObjectUtils.equalsWithValue(waferMapInCassetteInfo.getWaferID(), sorterSlotMap.getWaferID())
//                                        && CimStringUtils.equals(BizConstant.SP_SCRAPSTATE_SCRAP, waferMapInCassetteInfo.getScrapState())) {
//                                    log.info("bWaferFoundFlg = TRUE");
//                                    bWaferFoundFlg = true;
//                                    break;
//                                }
//                            } else {
//                                if (CimObjectUtils.equalsWithValue(waferMapInCassetteInfo.getWaferID(), sorterSlotMap.getWaferID())) {
//                                    log.info("bWaferFoundFlg = TRUE");
//                                    bWaferFoundFlg = true;
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                    //---------------------------------------------------
//                    // Check Exsit WaferID
//                    //---------------------------------------------------
//                    Validations.check(CimBooleanUtils.isFalse(bWaferFoundFlg), retCodeConfig.getInvalidCassetteLotRelation(),
//                            ObjectIdentifier.fetchValue(targetlotID));
//                }
//            }

        // ----------------------------------------------------------
        // D4000056
        // ----------------------------------------------------------
        // Original Location Validation method
        // 1. originalLocationVerify is SP_Sorter_Location_CheckBy_SlotMap
        //                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //   There are specified carrier's data in slotmap
        //      => Checked original slot location by SlotMap
        //   OTHER case
        //      => Checked original slot location by MM information
        //
        // 2. originalLocationVerify is SP_Sorter_Location_CheckBy_MM
        //                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //      => Checked original slot location by MM information
        //
        // ----------------------------------------------------------
        List<Infos.WaferSorterSlotMap> strWaferSorterSlotMapToCheck = new ArrayList<>();
        // ----------------------------------------------------------
        // Change checklogic of current wafer's position D4000135
        // Getting originalCassetteID and get each slotmap information
        // ----------------------------------------------------------
        if (CimStringUtils.equals(SorterType.Action.AdjustByMES.getValue(), action)) {
            for (int i = 0; i < CimArrayUtils.getSize(waferList); i++) {
                Validations.check(ObjectIdentifier.isEmpty(waferList.get(i).getWaferID()),
                        retCodeConfig.getWaferIDBlank());
                Boolean waferFound = false;
                CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, waferList.get(i).getWaferID());
                if (null != aPosWafer) {
                    waferFound = true;
                }
                Boolean cassetteFound = false;
                Boolean curCarrierFound ;
                CimCassette anInputOrgCassette = baseCoreFactory.getBO(CimCassette.class, componentJob.getOriginalCassetteID());
                if (null != anInputOrgCassette) {
                    cassetteFound = true;
                }
                curCarrierFound = cassetteFound;
                cassetteFound = false;
                //  Boolean destCarrierFound ;
                CimCassette anInputDestCassette = baseCoreFactory.getBO(CimCassette.class, componentJob.getDestinationCassetteID());
                if (null != anInputDestCassette) {
                    cassetteFound = true;
                }
                //  destCarrierFound = cassetteFound;
                if (CimBooleanUtils.isFalse(waferFound)) {
                    Boolean lotFound = true;
                    if (null != anInputOrgCassette) {
                        List<Lot> lots = anInputOrgCassette.allLots();
                        int orgCasseetteLen = CimArrayUtils.getSize(lots);
                        for (int j = 0; j < orgCasseetteLen; j++) {
                            aPosLotSeq.add((com.fa.cim.newcore.bo.product.CimLot) lots.get(j));
                        }
                    }
                    if (CimArrayUtils.isEmpty(aPosLotSeq) || null == aPosLotSeq.get(i)) {
                        lotFound = false;
                    }
                    Validations.check(CimBooleanUtils.isFalse(lotFound) || CimBooleanUtils.isTrue(curCarrierFound), retCodeConfig.getNotFoundMaterialLocation());
                } else {
                    MaterialContainer aMtrlCntnr = aPosWafer.getMaterialContainer();
                    CimCassette aCurCassette = (CimCassette) aMtrlCntnr;
                    if (null == aCurCassette && null == anInputOrgCassette) {
                        //ok
                    } else if (null != aCurCassette && null != anInputOrgCassette) {
                        String strCurrentCassetteID = aCurCassette.getIdentifier();
                        Integer nCurPosition = aPosWafer.getPosition();
                        // --------------------------------------------------
                        // D4000135
                        // When originalLocationVerify == SP_Sorter_Location_CheckBy_SlotMap
                        // If find carrier information in slotmap, then check by slotmap
                        // Otherwise , checked by mm information
                        // --------------------------------------------------
                        int checkBySlotMap = 0;
                        // --------------------------------------------------------------
                        // D4000135
                        // If there are information in SlotMap and originalLocationVerify ==
                        // then check by SP_Sorter_Location_CheckBy_SlotMap
                        // Otherwise, use current mm data
                        // --------------------------------------------------------------
                        if (1 == checkBySlotMap) {
                            int slotMapLen = CimArrayUtils.getSize(strWaferSorterSlotMapToCheck);
                            int foundWafer = 0;
                            for (int j = 0; j < slotMapLen; j++) {
                                //--------------------------------------------------------------
                                // If exactly same CassetteID/WaferID/SlotNumber exists, then OK
                                // --------------------------------------------------------------
                                if (ObjectIdentifier.equalsWithValue(strWaferSorterSlotMapToCheck.get(j).getDestinationCassetteID(),
                                        componentJob.getOriginalCassetteID())
                                        && ObjectIdentifier.equalsWithValue(strWaferSorterSlotMapToCheck.get(j).getWaferID(),
                                        waferList.get(i).getWaferID())
                                        && strWaferSorterSlotMapToCheck.get(j).getDestinationSlotNumber().intValue() ==
                                        waferList.get(i).getOriginalSlotNumber().intValue()) {
                                    foundWafer = 1;
                                }
                            }
                            //-------------------------------------------------------------
                            // If no exist CassetteID/WaferID/SlotNumber, then error
                            // ------------------------------------------------------------
                            Validations.check(1 != foundWafer, retCodeConfig.getInvalidOrgWaferPosition(),
                                    waferList.get(i).getWaferID().getValue(),
                                    componentJob.getOriginalCassetteID().getValue(),
                                    waferList.get(i).getOriginalSlotNumber());
                        } else {
                            Validations.check(!ObjectIdentifier.equalsWithValue(strCurrentCassetteID, componentJob.getOriginalCassetteID())
                                            || nCurPosition.intValue() != waferList.get(i).getOriginalSlotNumber().intValue(),
                                    retCodeConfig.getInvalidOrgWaferPosition(),
                                    waferList.get(i).getWaferID().getValue(),
                                    componentJob.getOriginalCassetteID().getValue(),
                                    waferList.get(i).getOriginalSlotNumber());
                        }
                    } else {
                        Validations.check(true, retCodeConfig.getInvalidOrgWaferPosition(),
                                waferList.get(i).getWaferID().getValue(),
                                componentJob.getOriginalCassetteID().getValue(),
                                waferList.get(i).getOriginalSlotNumber());
                    }
                    Lot aTmpLot = aPosWafer.getLot();
                    Validations.check(null == aTmpLot, retCodeConfig.getNotFoundLot());
                    aPosLotSeq.add((com.fa.cim.newcore.bo.product.CimLot) aTmpLot);
                    lotIDs.add(aPosLotSeq.get(i).getIdentifier());
                    List<MaterialContainer> aMtrlCntnrSeq = aPosLotSeq.get(i).materialContainers();
                    if (CimArrayUtils.isNotEmpty(aMtrlCntnrSeq)) {
                        cassetteIDs.add(aMtrlCntnrSeq.get(0).getIdentifier());
                    } else {
                        cassetteIDs.add("");
                    }
                }
            }
        }

        //--------------------------------------------------------
        // Check condition if wafer's carrier will be changed
        //--------------------------------------------------------

        for (int i = 0; i < nInputWaferLen; i++) {
            CimWafer anInputWafer = baseCoreFactory.getBO(CimWafer.class, waferList.get(i).getWaferID());
            Boolean bInWaferScrapState = anInputWafer.isScrap();
            if (CimBooleanUtils.isTrue(bInWaferScrapState)) {
                continue;
            }
            if (CimStringUtils.equals(SorterType.Action.AdjustByMES.getValue(), action)
                    && CimStringUtils.unEqual(SorterType.Action.Separate.getValue(),
                    sortJobInfo.getComponentJob().getActionCode())) {
                if (!ObjectIdentifier.equalsWithValue(cassetteIDs.get(i), componentJob.getDestinationCassetteID())) {
                    Boolean bLotWaferAlreadyChecked = false;
                    for (int j = 0; j < i; j++) {
                        if (CimStringUtils.equals(lotIDs.get(i), lotIDs.get(j))) {
                            bLotWaferAlreadyChecked = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isFalse(bLotWaferAlreadyChecked)) {
                        List<Material> aWafers = aPosLotSeq.get(i).allMaterial();
                        int nLotWaferLen = CimArrayUtils.getSize(aWafers);
                        for (int j = 0; j < nLotWaferLen; j++) {
                            Boolean bLotWaferFound = false;
                            String strLotWaferID = aWafers.get(j).getIdentifier();
                            Boolean bWaferScrapState = ((com.fa.cim.newcore.bo.product.CimWafer) aWafers.get(j)).isScrap();
                            if (CimBooleanUtils.isTrue(bWaferScrapState)) {
                                continue;
                            }
                            int k;
                            for (k = 0; k < nInputWaferLen; k++) {
                                if (ObjectIdentifier.equalsWithValue(strLotWaferID, waferList.get(k).getWaferID())) {
                                    bLotWaferFound = true;
                                    break;
                                }
                            }
                            Validations.check(CimBooleanUtils.isFalse(bLotWaferFound), retCodeConfig.getInvalidOrgWaferPosition());
                        }
                    }
//                    if (CimBooleanUtils.isFalse(bLotWaferAlreadyChecked)) {
//                        List<Material> aWafers = aPosLotSeq.get(i).allMaterial();
//                        int nLotWaferLen = CimArrayUtils.getSize(aWafers);
//                        for (int k = 0; k < nInputWaferLen; k++) {
//                            Boolean bLotWaferFound = false;
//                            for (int j = 0; j < nLotWaferLen; j++) {
//                                String strLotWaferID = aWafers.get(j).getIdentifier();
//                                Boolean bWaferScrapState = ((com.fa.cim.newcore.bo.product.CimWafer) aWafers.get(j)).isScrap();
//                                if (CimBooleanUtils.isTrue(bWaferScrapState)) {
//                                    continue;
//                                }
//                                if (CimObjectUtils.equalsWithValue(strLotWaferID, waferList.get(k).getWaferID())) {
//                                    bLotWaferFound = true;
//                                    break;
//                                }
//                            }
//                            Validations.check(CimBooleanUtils.isFalse(bLotWaferFound), retCodeConfig.getInvalidOrgWaferPosition());
//                            Validations.check(!CimObjectUtils.equalsWithValue(componentJob.getDestinationCassetteID(),
//                                    componentJob.getDestinationCassetteID()), retCodeConfig.getInvalidOrgWaferPosition());
//
//                        }
//                    }
                }
            }
        }
        //----------------------------------------------------
        // Extract input structure that system currently cannot
        // find out destination slot is empty or not
        //----------------------------------------------------
        List<Info.WaferSorterSlotMap> tmpWaferXferSeq = new ArrayList<>();
        int lenWaferXferSeq = CimArrayUtils.getSize(componentJob.getWaferList());
        for (int i = 0; i < lenWaferXferSeq; i++) {
            Boolean bWaferExchangeFlag = false;
            for (int j = 0; j < lenWaferXferSeq; j++) {
                if (ObjectIdentifier.equalsWithValue(componentJob.getDestinationCassetteID(), componentJob.getOriginalCassetteID())
                        && waferList.get(i).getDestinationSlotNumber().intValue()
                        == waferList.get(j).getOriginalSlotNumber().intValue()) {
                    bWaferExchangeFlag = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(bWaferExchangeFlag)) {
                tmpWaferXferSeq = waferList;
            }
        }

        //----------------------------------------------------
        // Check wafer's destination slot is empty or not
        //----------------------------------------------------
        lenWaferXferSeq = CimArrayUtils.getSize(tmpWaferXferSeq);
        List<Boolean> bSlotCheckFlagSeq = new ArrayList<>();
        for (int i = 0; i < lenWaferXferSeq; i++) {
            bSlotCheckFlagSeq.add(false);
        }
        for (int i = 0; i < lenWaferXferSeq; i++) {
            if (bSlotCheckFlagSeq.get(i)) {
                continue;
            }
            Boolean cassetteFound = true;
            CimCassette aPosCassette = baseCoreFactory.getBO(CimCassette.class, componentJob.getDestinationCassetteID());
            if (CimObjectUtils.isEmpty(aPosCassette)) cassetteFound = false;
            if (CimBooleanUtils.isFalse(cassetteFound)) {
                bSlotCheckFlagSeq.set(i, true);
                continue;
            }
            List<Integer> nEmptyPositions = aPosCassette.emptyPositions();
            int lenEmptyPos = CimArrayUtils.getSize(nEmptyPositions);
            for (int j = 0; j < lenWaferXferSeq; j++) {
                for (int k = 0; k < lenEmptyPos; k++) {
                    if (tmpWaferXferSeq.get(j).getDestinationSlotNumber().intValue() == nEmptyPositions.get(k).intValue()) {
                        bSlotCheckFlagSeq.set(j, true);
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < lenWaferXferSeq; i++) {
            //------------------------
            // Error case
            //------------------------
            if (CimBooleanUtils.isFalse(bSlotCheckFlagSeq.get(i))) {
                CimCassette aPosCassette = baseCoreFactory.getBO(CimCassette.class, componentJob.getDestinationCassetteID());
                Integer nPosTotal = aPosCassette.getPosition();
                Validations.check(retCodeConfigEx.getInvalidDestWaferPosition(), ObjectIdentifier.fetchValue(tmpWaferXferSeq.get(i).getWaferID()),
                        nPosTotal, ObjectIdentifier.fetchValue(componentJob.getDestinationCassetteID()));
                if(tmpWaferXferSeq.get(i).getDestinationSlotNumber() > (nPosTotal.intValue() - 1)
                        || tmpWaferXferSeq.get(i).getDestinationSlotNumber() == 0){
                    Validations.check(retCodeConfigEx.getInvalidDestWaferPosition(), ObjectIdentifier.fetchValue(tmpWaferXferSeq.get(i).getWaferID()),
                            nPosTotal,ObjectIdentifier.fetchValue(componentJob.getDestinationCassetteID()));
                }else{
                    Validations.check(retCodeConfigEx.getInvalidDestWaferPosition(), ObjectIdentifier.fetchValue(tmpWaferXferSeq.get(i).getWaferID()),
                            nPosTotal,ObjectIdentifier.fetchValue(componentJob.getDestinationCassetteID()));
                }

            }
        }
        //--------------------------------------------------------------
        // Check that Wafer must not belong to two or more Carriers.
        //--------------------------------------------------------------
        if (CimStringUtils.unEqual(SorterType.Action.Separate.getValue(), sortJobInfo.getComponentJob().getActionCode())) {
            for (int i = 0; i < nInputWaferLen; i++) {
                // Check destination Cassette existance
                // If destination Cassette does not exist in SiView, the Cassette is not checked.
                Boolean cassetteFound = false;
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, componentJob.getDestinationCassetteID());
                if (null != aCassette) {
                    cassetteFound = true;
                }
                if (CimBooleanUtils.isFalse(cassetteFound)) {
                    continue;
                }
                ObjectIdentifier destCastID = ObjectIdentifier.build(aCassette.getIdentifier(), aCassette.getPrimaryKey());
                ObjectIdentifier strWaferLotGetOut = null;
                try {
                    strWaferLotGetOut = waferMethod.waferLotGet(objCommon, waferList.get(i).getWaferID());
                } catch (ServiceException e) {
                    continue;
                }
                Inputs.ObjLotWafersGetIn objLotWafersGetIn = new Inputs.ObjLotWafersGetIn();
                objLotWafersGetIn.setLotID(strWaferLotGetOut);
                objLotWafersGetIn.setScrapCheckFlag(false);
                List<Infos.LotWaferInfoAttributes> parentLotWaferInfoListGetDROut = lotMethod.lotWaferInfoListGetDR(objCommon, objLotWafersGetIn);
                int nAllWafer = CimArrayUtils.getSize(parentLotWaferInfoListGetDROut);
                for (int j = 0; j < nAllWafer; j++) {
                    if (ObjectIdentifier.isEmpty(parentLotWaferInfoListGetDROut.get(j).getCassetteID())) {
                        continue;
                    }
                    Boolean bNoCheck = false;
                    for (int k = 0; k < nInputWaferLen; k++) {
                        if (ObjectIdentifier.equalsWithValue(parentLotWaferInfoListGetDROut.get(j).getWaferID(),
                                waferList.get(k).getWaferID())) {
                            bNoCheck = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bNoCheck)) {
                        continue;
                    }
                    cassetteFound = false;
                    com.fa.cim.newcore.bo.durable.CimCassette aChkCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, parentLotWaferInfoListGetDROut.get(j).getCassetteID());
                    if (null != aChkCassette) {
                        cassetteFound = true;
                    }
                    if (CimBooleanUtils.isFalse(cassetteFound)) {
                        continue;
                    }
                    Validations.check(!ObjectIdentifier.equalsWithValue(parentLotWaferInfoListGetDROut.get(j).getCassetteID(), destCastID), new OmCode(retCodeConfig.getInvalidCassetteLotRelation(), ObjectIdentifier.fetchValue(strWaferLotGetOut)));
                }
//            for (int k = 0; k < nInputWaferLen; k++) {
//                Boolean bNoCheck = false;
//                for (int j = 0; j < nAllWafer; j++) {
//                    if (CimObjectUtils.isEmpty(parentLotWaferInfoListGetDROut.get(j).getCassetteID())) {
//                        continue;
//                    }
//                    if (CimObjectUtils.equalsWithValue(parentLotWaferInfoListGetDROut.get(j).getWaferID(),
//                            waferList.get(k).getWaferID())
//                            && CimObjectUtils.equalsWithValue(componentJob.getDestinationCassetteID(),
//                            componentJob.getDestinationCassetteID())) {
//                        bNoCheck = true;
//                        break;
//                    }
//                    if (CimBooleanUtils.isTrue(bNoCheck)) {
//                        continue;
//                    }
//                    cassetteFound = false;
//                    CimCassette aChkCassette = baseCoreFactory.getBO(CimCassette.class, parentLotWaferInfoListGetDROut.get(j).getCassetteID());
//                    if (null != aChkCassette) {
//                        cassetteFound = true;
//                    }
//                    if (CimBooleanUtils.isFalse(cassetteFound)) {
//                        continue;
//                    }
//                }
//                Validations.check(!bNoCheck,retCodeConfig.getInvalidCassetteLotRelation(), ObjectIdentifier.fetchValue(strWaferLotGetOut));
//            }
            }
        }
    }

    @Override
    public void cassetteCheckConditionForWaferSort(Infos.ObjCommon objCommon, Info.SortJobInfo sortJobInfo) {
        //--------------------------------------------------
        // Collect cassette ID of input parameter
        //--------------------------------------------------
        Info.ComponentJob componentJob = sortJobInfo.getComponentJob();
        ObjectIdentifier equipmentID = sortJobInfo.getEquipmentID();
        List<ObjectIdentifier> cassetteIDSeq = SorterHandler.getduplicateRemovalCarrierIDs(componentJob);
        //--------------------------------------------------
        // Collect cassette's equipment ID / Cassette ObjRef
        //--------------------------------------------------
        int lenCasIDSeq = CimArrayUtils.getSize(cassetteIDSeq);
        List<CimCassette> aPosCassetteSeq = new ArrayList<>();
        List<String> strLoadedEquipments = new ArrayList<>();
        if (lenCasIDSeq > 0) {
            for (ObjectIdentifier cassetteID : cassetteIDSeq) {
                CimCassette aPosCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
                if (null == aPosCassette) {
                    continue;
                }
                String tmpCassetteTransportState = aPosCassette.getTransportState();
                Machine tmpMachine = aPosCassette.currentAssignedMachine();
                aPosCassetteSeq.add(aPosCassette);
                if (null != tmpMachine && CimStringUtils.equals(tmpCassetteTransportState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    strLoadedEquipments.add(tmpMachine.getIdentifier());
                }
            }
        }

        //-----------------------------
        //  Check InPostProcessFlag
        //-----------------------------
        if (lenCasIDSeq > 0) {
            for (ObjectIdentifier cassetteID : cassetteIDSeq) {
                //---------------------------------------
                //  Get InPostProcessFlag of Cassette
                //---------------------------------------
                Boolean strCassettInPostProcessFlagGetOut = cassetteMethod.cassetteInPostProcessFlagGet(objCommon, cassetteID);
                String strCassetteInterFabXferStateGetOut = cassetteMethod.cassetteInterFabXferStateGet(objCommon, cassetteID);
                Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, strCassetteInterFabXferStateGetOut), retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest());
                Infos.LotListInCassetteInfo strCassetteLotListGetDROut = cassetteMethod.cassetteLotIDListGetDR(objCommon, cassetteID);
                int lotLen = CimArrayUtils.getSize(strCassetteLotListGetDROut.getLotIDList());
                if (lotLen > 0) {
                    for (ObjectIdentifier lotId : strCassetteLotListGetDROut.getLotIDList()) {
                        String lotInterFabXferStateResultOut = lotMethod.lotInterFabXferStateGet(objCommon, lotId);
                        if (CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_ORIGINDELETING, lotInterFabXferStateResultOut)
                                || CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, lotInterFabXferStateResultOut)) {
                            throw new ServiceException(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest());
                        }
                    }
                }
                //---------------------------------------------------
                //  If Cassette is in post process, returns error
                //---------------------------------------------------
                Validations.check(CimBooleanUtils.isTrue(strCassettInPostProcessFlagGetOut), retCodeConfig.getCassetteInPostProcess());
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
                Machine aSLMReservedMachine = aCassette.getSLMReservedMachine();
                Validations.check(null != aSLMReservedMachine, retCodeConfig.getAlreadyReservedCassetteSlm(), aCassette.getIdentifier());
            }
        }
        //--------------------------------------------------
        // Check all cassette is on the same equipment
        //--------------------------------------------------
        String tmpLoadedEquipment = null;
        int lenLoadedEqp = CimArrayUtils.getSize(strLoadedEquipments);
        for (int i = 0; i < lenLoadedEqp; i++) {
            if (0 == i) {
                tmpLoadedEquipment = strLoadedEquipments.get(0);
            } else {
                Validations.check(!CimStringUtils.equals(tmpLoadedEquipment, strLoadedEquipments.get(i)), retCodeConfig.getEquipmentOfCassetteNotSame());
            }
        }
        //--------------------------------------------------
        // Check equipment of cassette and input equipment is same or not
        //--------------------------------------------------

        Validations.check(!CimStringUtils.equals(TransactionIDEnum.WAFER_SORTER_POSITION_ADJUST_REQ.getValue(), objCommon.getTransactionID())
                && !ObjectIdentifier.equalsWithValue(equipmentID, tmpLoadedEquipment), retCodeConfig.getCassetteEquipmentDifferent());

        //--------------------------------------------------
        // If equipmetnID is not input, return OK
        //--------------------------------------------------
        if (ObjectIdentifier.isEmpty(equipmentID) && CimStringUtils.isEmpty(tmpLoadedEquipment)) {
            //ok
            return;
        }

        //--------------------------------------------------
        // Get object reference of PosMachine
        //--------------------------------------------------
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        //--------------------------------------------------
        // Get Equipment attributes
        //--------------------------------------------------
        String strMachineCategory = aPosMachine.getCategory();

        //--------------------------------------------------
        // Check Equipment Type
        //--------------------------------------------------
        Validations.check(!CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_WAFERSORTER, strMachineCategory), retCodeConfig.getMachineTypeNotSorter());

        //--------------------------------------------------
        // Check Control Job Info
        //--------------------------------------------------
        String strCassetteControlJob = null;
        String tmpCassetteControlJob;

        int lenCasSeq = CimArrayUtils.getSize(aPosCassetteSeq);
        if (!StringUtils.isEmpty(ObjectIdentifier.fetchValue(sortJobInfo.getControlJobID()))) {
            for (int i = 0; i < lenCasSeq; i++) {
                CimControlJob aPosCtrlJob;
                if (0 == i) {
                    aPosCtrlJob = aPosCassetteSeq.get(i).getControlJob();

                    Validations.check(null == aPosCtrlJob, retCodeConfig.getNotSameControlJobId());
                    strCassetteControlJob = aPosCtrlJob.getIdentifier();

                } else {
                    aPosCtrlJob = aPosCassetteSeq.get(i).getControlJob();
                    Validations.check(null == aPosCtrlJob, retCodeConfig.getNotSameControlJobId());

                    tmpCassetteControlJob = aPosCtrlJob.getIdentifier();
                    Validations.check(!CimStringUtils.equals(strCassetteControlJob, tmpCassetteControlJob), retCodeConfig.getNotSameControlJobId());
                }
            }
        }

    }



    @Override
    public Results.ObjSorterWaferTransferInfoRestructureOut sorterWaferTransferInfoRestructure(Infos.ObjCommon objCommon, Info.SortJobInfo sortJobInfo) {
        Results.ObjSorterWaferTransferInfoRestructureOut data = new Results.ObjSorterWaferTransferInfoRestructureOut();

        Info.ComponentJob componentJob = sortJobInfo.getComponentJob();
        List<Info.WaferSorterSlotMap> waferList = componentJob.getWaferList();
        //Prepare work structure for Output structure
        List<String> strLotInventoryStateList = new ArrayList<>();
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        List<Infos.PLot> pLotList = new ArrayList<>();
        //Create output structure
        CimCassette fromCassette = baseCoreFactory.getBO(CimCassette.class, componentJob.getOriginalCassetteID());
        CimCassette aToCassette = baseCoreFactory.getBO(CimCassette.class, componentJob.getDestinationCassetteID());
        for (Info.WaferSorterSlotMap waferSorterSlotMap : waferList) {
            //Prepare output structure of lot_wafer_Create() here
            CimLot lot;
            CimWafer wafer = baseCoreFactory.getBO(CimWafer.class, waferSorterSlotMap.getWaferID());
            boolean waferIsNewlyCreatedHere = false;
            Outputs.ObjLotWaferCreateOut objLotWaferCreateOut = null;

            //input waferID is not exist case
            if (wafer == null) {
                log.info("sorterWaferTransferInfoRestructure(): wafer is null");
                //Check condition
                Validations.check(null != fromCassette, retCodeConfig.getNotFoundCassette());
                lot = baseCoreFactory.getBO(CimLot.class, waferSorterSlotMap.getLotID());
                Validations.check(lot == null, retCodeConfig.getNotFoundLot());
                String strSourceLotType = lot.getLotType();
                Validations.check(!CimStringUtils.equals(strSourceLotType, BizConstant.SP_LOT_TYPE_VENDORLOT), retCodeConfig.getInvalidLotType());
                //Create wafer, lot_wafer_Create
                objLotWaferCreateOut = lotMethod.lotWaferCreate(objCommon, componentJob.getOriginalCassetteID(),
                        ObjectIdentifier.fetchValue(waferSorterSlotMap.getWaferID()));
                waferIsNewlyCreatedHere = true;
            } else {
                //input waferID is exist case
                lot = (com.fa.cim.newcore.bo.product.CimLot) wafer.getLot();
                Validations.check(lot == null, retCodeConfig.getNotFoundLot());
            }
            /*
             Get lot's inventory status
             If lot is InBank and has po,
             lot inventory status here should be treated as 'OnFloor'
             */
            String strLotInventoryState;
            String tmpLotInventoryState = lot.getLotInventoryState();
            CimProcessOperation tmpPO = lot.getProcessOperation();
            if (CimStringUtils.equals(tmpLotInventoryState, BizConstant.SP_LOT_INVENTORYSTATE_INBANK) && tmpPO != null) {
                strLotInventoryState = BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR;
            } else {
                strLotInventoryState = tmpLotInventoryState;
            }
            //Prepare temporary lotID structure
            ObjectIdentifier tmpLotID;
            if (CimBooleanUtils.isTrue(waferIsNewlyCreatedHere)) {
                tmpLotID = componentJob.getOriginalCassetteID();
            } else {
                tmpLotID = lot.getLotID();
            }
            //Create output structure
            boolean waferAdded = false;
            if (!CimArrayUtils.isEmpty(pLotList)) {
                for (int j = 0; j < pLotList.size(); j++) {
                    Infos.PLot pLot = pLotList.get(j);
                    Infos.PWafer pWafer = new Infos.PWafer();
                    if (ObjectIdentifier.equals(pLot.getLotID(), tmpLotID)) {
                        if (!CimObjectUtils.equals(cassetteIDList.get(j), componentJob.getDestinationCassetteID())
                                && !CimStringUtils.equals(strLotInventoryStateList.get(j), BizConstant.SP_LOT_INVENTORYSTATE_INBANK)) {
                            Validations.check(retCodeConfig.getInvalidCassetteLotRelation(), ObjectIdentifier.fetchValue(pLot.getLotID()));
                        } else if (CimObjectUtils.equals(cassetteIDList.get(j), componentJob.getDestinationCassetteID())
                                && !CimStringUtils.equals(strLotInventoryStateList.get(j), BizConstant.SP_LOT_INVENTORYSTATE_INBANK)) {
                            pWafer.setWaferID(waferSorterSlotMap.getWaferID());
                            pWafer.setSlotNumber(waferSorterSlotMap.getDestinationSlotNumber());
                            pWafer.setAliasName(waferSorterSlotMap.getAliasName());
                            waferAdded = true;
                            pLot.getWaferList().add(pWafer);
                            break;
                        } else if (CimObjectUtils.equals(cassetteIDList.get(j), componentJob.getDestinationCassetteID())
                                && CimStringUtils.equals(strLotInventoryStateList.get(j), BizConstant.SP_LOT_INVENTORYSTATE_INBANK)) {
                            if (CimBooleanUtils.isTrue(waferIsNewlyCreatedHere)) {
                                pWafer.setWaferID(objLotWaferCreateOut == null ? null : objLotWaferCreateOut.getNewWaferID());
                                pWafer.setSlotNumber(waferSorterSlotMap.getDestinationSlotNumber());
                                pWafer.setAliasName(waferSorterSlotMap.getAliasName());
                                pLot.getWaferList().add(pWafer);
                            } else {
                                pWafer.setWaferID(waferSorterSlotMap.getWaferID());
                                pWafer.setSlotNumber(waferSorterSlotMap.getDestinationSlotNumber());
                                pWafer.setAliasName(waferSorterSlotMap.getAliasName());
                                pLot.getWaferList().add(pWafer);
                            }
                            waferAdded = true;
                            break;
                        }
                    }
                }
            }
            if (CimBooleanUtils.isFalse(waferAdded)) {
                strLotInventoryStateList.add(strLotInventoryState);
                cassetteIDList.add(componentJob.getDestinationCassetteID());
                Infos.PLot pLot = new Infos.PLot();
                pLot.setLotID(tmpLotID);
                List<Infos.PWafer> wafers = new ArrayList<>();
                Infos.PWafer pWafer = new Infos.PWafer();
                if (CimBooleanUtils.isTrue(waferIsNewlyCreatedHere)) {
                    pWafer.setWaferID(objLotWaferCreateOut == null ? null : objLotWaferCreateOut.getNewWaferID());
                    pWafer.setSlotNumber(waferSorterSlotMap.getDestinationSlotNumber());
                } else {
                    pWafer.setWaferID(waferSorterSlotMap.getWaferID());
                    pWafer.setSlotNumber(waferSorterSlotMap.getDestinationSlotNumber());
                }
                pWafer.setAliasName(waferSorterSlotMap.getAliasName());
                wafers.add(pWafer);
                pLot.setWaferList(wafers);
                pLotList.add(pLot);
            }
        }

        data.setCassetteIDList(cassetteIDList);
        data.setLotInventoryStateList(strLotInventoryStateList);
        data.setLotList(pLotList);
        return data;
    }

    @Override
    public Info.SortJobInfo sorterActionInq(Infos.ObjCommon objCommon, Params.SorterActionInqParams params,String jobStatus) {
        if (log.isDebugEnabled()) {
            log.debug("Find the sortJob with the highest priority");
        }
        //--------------------------------------------------
        // get sortJobList,对于一个carrierId只能存在一个sortJob
        //--------------------------------------------------
        //为了满足EAP,满足以下构建对象
        Info.SortJobInfo infoResult = new Info.SortJobInfo();
        infoResult.setSorterJobID(ObjectIdentifier.buildWithValue(""));
        Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new Params.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRIn.setCarrierID(params.getCassetteID());
        objSorterJobListGetDRIn.setEquipmentID(params.getEquipmentID());
        List<Info.SortJobListAttributes> sortJobListAttributes = this.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
        if(CimArrayUtils.getSize(sortJobListAttributes) == 0){
            return infoResult;
        }
        Info.SortJobListAttributes sortJob = null;
        for (Info.SortJobListAttributes sortJobListAttribute : sortJobListAttributes) {
            if (CimStringUtils.equals(SorterType.Status.Created.getValue(), sortJobListAttribute.getSorterJobStatus())
                    || CimStringUtils.equals(SorterType.Status.Executing.getValue(), sortJobListAttribute.getSorterJobStatus())) {
                sortJob = sortJobListAttribute;
                break;
            }
        }
        if(null == sortJob){
            return infoResult;
        }
        infoResult.setSorterJobID(sortJob.getSorterJobID());
        infoResult.setEquipmentID(sortJob.getEquipmentID());
        infoResult.setPortGroup(ObjectIdentifier.fetchValue(sortJob.getPortGroupID()));
        infoResult.setSorterJobStatus(sortJob.getSorterJobStatus());
        infoResult.setRequestUserID(sortJob.getRequestUserID());
        infoResult.setWaferIDReadFlag(sortJob.isWaferIDReadFlag());
        infoResult.setControlJobID(ObjectIdentifier.buildWithValue(sortJob.getCtrljobId()));
        infoResult.setUser(params.getUser());
        if (log.isDebugEnabled()) {
            log.debug("check compoentJob");
        }
        List<Info.SorterComponentJobListAttributes> componentJobListAttributes = sortJob.getSorterComponentJobListAttributesList();
        boolean foundFlag = false;
        Info.SorterComponentJobListAttributes attributes = null;
        if (CimArrayUtils.getSize(componentJobListAttributes) > 0) {
            for (Info.SorterComponentJobListAttributes jobListAttributes : componentJobListAttributes) {
                String componentJobStatus = jobListAttributes.getComponentSorterJobStatus();
                if (!Objects.equals(jobStatus, componentJobStatus)) {
                    continue;
                }
                if (Objects.equals(jobStatus, componentJobStatus)) {
                    foundFlag = true;
                    attributes = jobListAttributes;
                    break;
                }
            }
        }

        if(!foundFlag){
            infoResult.setSorterJobID(ObjectIdentifier.buildWithValue(""));
            return infoResult;
        }
        if (!ObjectIdentifier.isEmpty(params.getPortID())) {
            String actionCode = attributes.getActionCode();
            if (actionCode.equals(SorterType.Action.LotTransfer.getValue())
                    || actionCode.equals(SorterType.Action.WaferEnd.getValue())) {
                ObjectIdentifier srcCarrierID = attributes.getOriginalCarrierID();
                ObjectIdentifier desCarrierID = attributes.getDestinationCarrierID();
                if (!ObjectIdentifier.equalsWithValue(srcCarrierID, params.getCassetteID())
                        && !ObjectIdentifier.equalsWithValue(desCarrierID, params.getCassetteID())) {
                    Info.SortJobInfo jobInfo = new Info.SortJobInfo();
                    jobInfo.setSorterJobID(ObjectIdentifier.buildWithValue(""));
                    return jobInfo;
                }
            }
        }
        //--------------------------------------------------
        // get getComponentJob
        //--------------------------------------------------
        getComponentJob(true, attributes, infoResult);
        return infoResult;
    }

    private void getComponentJob(boolean foundFlag, Info.SorterComponentJobListAttributes attributes, Info.SortJobInfo infoResult) {
        if (foundFlag && null != attributes) {
            Info.ComponentJob componentJob = new Info.ComponentJob();
            componentJob.setComponentJobID(ObjectIdentifier.fetchValue(attributes.getSorterComponentJobID()));
            componentJob.setComponentJobStatus(attributes.getComponentSorterJobStatus());
            //--------------------------------------------------
            // ActionCode=RFIDRead时carrierID=FOSB11111处理成空
            //--------------------------------------------------
            if (CimStringUtils.equals(SorterType.Action.RFIDRead.getValue(), attributes.getActionCode())) {
                componentJob.setOriginalCassetteID(ObjectIdentifier.buildWithValue(""));
                componentJob.setDestinationCassetteID(ObjectIdentifier.buildWithValue(""));
            } else {
                componentJob.setDestinationCassetteID(attributes.getDestinationCarrierID());
                componentJob.setOriginalCassetteID(attributes.getOriginalCarrierID());
            }
            componentJob.setOriginalPortID(attributes.getOriginalPortID());
            componentJob.setDestinationPortID(attributes.getDestinationPortID());
            componentJob.setRequestTimeStamp(attributes.getRequestTimeStamp());
            componentJob.setActionCode(attributes.getActionCode());
            infoResult.setComponentJob(componentJob);
            List<Info.WaferSorterSlotMap> waferList = attributes.getWaferSorterSlotMapList();
            int waferSize = CimArrayUtils.getSize(waferList);
            if (waferSize > 0) {
                List<Info.WaferSorterSlotMap> infos = new ArrayList<>(waferSize);
                waferList.stream().forEach(slotMap -> {
                    Info.WaferSorterSlotMap slotMapInfo = new Info.WaferSorterSlotMap();
                    if (CimStringUtils.equals(SorterType.Action.WaferStart.getValue(), attributes.getActionCode())) {
                        slotMapInfo.setWaferID(ObjectIdentifier.buildWithValue(""));
                    } else {
                        slotMapInfo.setWaferID(slotMap.getWaferID());
                        slotMapInfo.setAliasName(slotMap.getAliasName());
                        slotMapInfo.setStatus(true);
                    }
                    slotMapInfo.setOriginalSlotNumber(slotMap.getOriginalSlotNumber());
                    slotMapInfo.setDestinationSlotNumber(slotMap.getDestinationSlotNumber());
                    slotMapInfo.setLotID(slotMap.getLotID());
                    infos.add(slotMapInfo);
                });
                componentJob.setWaferList(infos);
            }
        }
    }

    @Override
    public void sorterCheckConditionForJobCreate(Infos.ObjCommon objCommon, List<Info.ComponentJob> componentJobList,
                                                 ObjectIdentifier equipmentID, String portGroupID, ObjectIdentifier controlJobID) {
        Validations.check(CimArrayUtils.isEmpty(componentJobList), retCodeConfigEx.getNotFoundSorterjobComponent());
        //==============================================================
        // Check Inparameter consistency
        //==============================================================
        int srtCmpLen  = CimArrayUtils.getSize(componentJobList);
        int srtCmpCnt;
        int srtCmpCnt2;

        int slotMapLen;
        int slotMapCnt;
        int slotMapLen2;
        int slotMapCnt2;

        String actionCode = componentJobList.get(0).getActionCode();
        if(CimStringUtils.unEqual(SorterType.Action.RFIDRead.getValue(), actionCode)
                && CimStringUtils.unEqual(SorterType.Action.RFIDWrite.getValue(), actionCode)){
            //----------------------
            // Component vs Slotmap
            //----------------------
            if (log.isDebugEnabled()) {
                log.debug("Component vs Slotmap");
            }
            for (Info.ComponentJob componentJob : componentJobList) {
                //--------------------------------------------------------------
                //  只要不做transfer的action,来源跟目的地的carrier,port相同
                //--------------------------------------------------------------
                Validations.check(!this.checkSpecifiedAction(actionCode)
                                && !actionCode.equals(SorterType.Action.LotTransfer.getValue())
                        && ObjectIdentifier.equalsWithValue(componentJob.getOriginalCassetteID(), componentJob.getDestinationCassetteID())
                        && ObjectIdentifier.equalsWithValue(componentJob.getOriginalPortID(), componentJob.getDestinationPortID()),
                        retCodeConfigEx.getSorterInvalidParameter(), "The port or carrier of the source and destination should be different");

                //--------------------------------------------------------------
                //  port相同时，Carrier一定不同
                //--------------------------------------------------------------
                Validations.check(!ObjectIdentifier.equalsWithValue(componentJob.getOriginalCassetteID(), componentJob.getDestinationCassetteID())
                                && ObjectIdentifier.equalsWithValue(componentJob.getOriginalPortID(), componentJob.getDestinationPortID()),
                        retCodeConfigEx.getSorterInvalidParameter(), "Each Port ID is the same despite being Original different from Destination.");

                //--------------------------------------------------------------
                //  Carrier相同时，port一定不同
                //--------------------------------------------------------------
                Validations.check(ObjectIdentifier.equalsWithValue(componentJob.getOriginalCassetteID(), componentJob.getDestinationCassetteID())
                                && !ObjectIdentifier.equalsWithValue(componentJob.getOriginalPortID(), componentJob.getDestinationPortID()),
                        retCodeConfigEx.getSorterInvalidParameter(), "Each Port ID differs despite being Original the same as Destination.");

                //--------------------------------------------------------------
                //  验证source和destination不能使用相同的slotNumber
                //--------------------------------------------------------------
                List<Info.WaferSorterSlotMap> waferList = componentJob.getWaferList();
                long oriCount = Optional.ofNullable(waferList).orElseGet(Collections::emptyList).stream()
                        .map(Info.WaferSorterSlotMap::getOriginalSlotNumber)
                        .distinct()
                        .count();
                long destCount = Optional.ofNullable(waferList).orElseGet(Collections::emptyList).stream()
                        .map(Info.WaferSorterSlotMap::getDestinationSlotNumber)
                        .distinct()
                        .count();
                Validations.check(oriCount < CimArrayUtils.getSize(waferList) || destCount < CimArrayUtils.getSize(waferList),
                        retCodeConfigEx.getSorterInvalidParameter(), "Slot Number is duplicated. original/destination.");
            }

            //-------------------------
            // Component vs Component
            //-------------------------
            if (log.isDebugEnabled()) {
                log.debug("Component vs Component");
            }
            for (srtCmpCnt = 0; srtCmpCnt < srtCmpLen; srtCmpCnt++){
                for (srtCmpCnt2 = srtCmpCnt + 1; srtCmpCnt2 < srtCmpLen; srtCmpCnt2++){
                    //--------------------------------------------------------------
                    //  Component1.originalPortID - Component2.destinationPortID
                    //--------------------------------------------------------------
                    if (ObjectIdentifier.equalsWithValue(componentJobList.get(srtCmpCnt).getOriginalPortID(),
                            componentJobList.get(srtCmpCnt2).getDestinationPortID())) {
                        Validations.check(retCodeConfigEx.getSorterInvalidParameter(),
                                "The Same Port is specified between OriginalPort and DestinationPort in different Component Job. Org/Dst ");
                    }

                    //--------------------------------------------------------------
                    //  Component1.originalCarrierID - Component2.destinationCarrierID
                    //--------------------------------------------------------------
                    if (ObjectIdentifier.equalsWithValue(componentJobList.get(srtCmpCnt).getOriginalCassetteID(),
                            componentJobList.get(srtCmpCnt2).getDestinationCassetteID())) {
                        Validations.check(retCodeConfigEx.getSorterInvalidParameter(),
                                "The Same Carrier is specified between OriginalCarrier and DestinationCarrier in different Component Job. Org/Dst");
                    }

                    //--------------------------------------------------------------
                    //  Component1.SlotMap.lotID  - Component2.SlotMap.lotID
                    //--------------------------------------------------------------
                    if (CimStringUtils.equals(TransactionIDEnum.SORT_JOB_CREATE_REQ.getValue(), objCommon.getTransactionID())
                            && CimStringUtils.unEqual(SorterType.Action.WaferStart.getValue(), actionCode)) {
                        slotMapLen = CimArrayUtils.getSize(componentJobList.get(srtCmpCnt).getWaferList());
                        slotMapLen2 = CimArrayUtils.getSize(componentJobList.get(srtCmpCnt2).getWaferList());
                        for (slotMapCnt = 0; slotMapCnt < slotMapLen; slotMapCnt++) {
                            ObjectIdentifier tmpLotID = componentJobList.get(srtCmpCnt).getWaferList().get(slotMapCnt).getLotID();
                            for (slotMapCnt2 = 0; slotMapCnt2 < slotMapLen2; slotMapCnt2++) {
                                ObjectIdentifier currentLotID = componentJobList.get(srtCmpCnt2).getWaferList().get(slotMapCnt2).getLotID();
                                Validations.check(ObjectIdentifier.equalsWithValue(tmpLotID, currentLotID), retCodeConfigEx.getSorterInvalidParameter()
                                        , "The Same Lot is specified in different Component Job. LotID : " + ObjectIdentifier.fetchValue(tmpLotID));
                            }
                        }
                    }


                    //--------------------------------------------------------------
                    //  Component1.destinationCarrierID == Component2.destinationCarrierID
                    //  Component1.SlotMap.destinationSlotNumber - Component2.SlotMap.destinationSlotNumber
                    //--------------------------------------------------------------
                    if (ObjectIdentifier.equalsWithValue(componentJobList.get(srtCmpCnt).getDestinationCassetteID(),
                            componentJobList.get(srtCmpCnt2).getDestinationCassetteID())) {
                        slotMapLen = CimArrayUtils.getSize(componentJobList.get(srtCmpCnt).getWaferList());
                        slotMapLen2 = CimArrayUtils.getSize(componentJobList.get(srtCmpCnt2).getWaferList());
                        for (slotMapCnt = 0; slotMapCnt < slotMapLen; slotMapCnt++) {
                            Integer tmpDestSlotNum = componentJobList.get(srtCmpCnt).getWaferList().get(slotMapCnt).getDestinationSlotNumber();
                            for (slotMapCnt2 = 0; slotMapCnt2 < slotMapLen2; slotMapCnt2++) {
                                Integer curDestSlotNum = componentJobList.get(srtCmpCnt2).getWaferList().get(slotMapCnt2).getDestinationSlotNumber();
                                Validations.check(CimNumberUtils.eq(tmpDestSlotNum, curDestSlotNum), retCodeConfigEx.getSorterInvalidParameter(),
                                        "The Destination SlotMap is dupulicated. : " + tmpDestSlotNum);
                            }
                        }
                    }
                }
            }

            //--------------------------------------------------------------
            //  Check MaximumWaferInALot
            //--------------------------------------------------------------
            if (log.isDebugEnabled()) {
                log.debug("Check MaximumWaferInALot");
            }
            int maxWaferInLot = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getIntValue();
            for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {
                int  waferCount = 0;
                for( srtCmpCnt2 = srtCmpCnt ; srtCmpCnt2 < srtCmpLen; srtCmpCnt2++ ) {
                    if(ObjectIdentifier.equalsWithValue(componentJobList.get(srtCmpCnt).getDestinationCassetteID(),
                            componentJobList.get(srtCmpCnt2).getDestinationCassetteID())) {
                        waferCount = waferCount + CimArrayUtils.getSize(componentJobList
                                .get(srtCmpCnt2).getWaferList());
                    }
                }
                if( waferCount > maxWaferInLot ) {
                    Validations.check(retCodeConfigEx.getSorterInvalidParameter(),
                            "Some wafers were specified to the destination slot more than Maximum Wafer In A Lot." + waferCount);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("check carrier number");
        }
        Set<ObjectIdentifier> orgCarrierIDs = new HashSet<>();
        Set<ObjectIdentifier> dstCarrierIDs = new HashSet<>();
        for (Info.ComponentJob componentJob : componentJobList) {
            if(!SorterHandler.containsFOSB(componentJob.getOriginalCassetteID())){
                orgCarrierIDs.add(componentJob.getOriginalCassetteID());
            }
            if(!SorterHandler.containsFOSB(componentJob.getDestinationCassetteID())){
                dstCarrierIDs.add(componentJob.getDestinationCassetteID());
            }
        }
        Validations.check(orgCarrierIDs.size() > 1 && dstCarrierIDs.size() > 1,
                retCodeConfigEx.getSorterInvalidParameter(),
                "Can not sort in between any OriginalCarriers and any DestinationCarriers.");

        //------------------------------------------------------------------
        //在创建Reset的Sorter Job时，如果一个FOUP有多个Lot，系统需要Reject
        //------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("check lot");
        }
        for (ObjectIdentifier dstCarrierID : dstCarrierIDs) {
            if (SorterType.Action.Reset.getValue().equals(actionCode)) {
                Infos.LotListInCassetteInfo objCassetteGetLotListOut = cassetteMethod.cassetteGetLotList(objCommon, dstCarrierID);
                List<ObjectIdentifier> lotIDList = objCassetteGetLotListOut.getLotIDList();
                Validations.check(CimArrayUtils.getSize(lotIDList) > 1, retCodeConfigEx.getLotInCassetteNumExcessive());
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("check destination carrier exist");
        }
        if (CimStringUtils.equals(SorterType.Action.RFIDWrite.getValue(), actionCode)) {
            Infos.LotListInCassetteInfo objCassetteGetLotListOut= null;
            try {
                ObjectIdentifier cassetteID = componentJobList.get(0).getDestinationCassetteID();
                objCassetteGetLotListOut = cassetteMethod.cassetteGetLotList(objCommon, cassetteID);
            } catch (ServiceException e) {
                if(e.getCode().equals(retCodeConfig.getNotFoundLot())){
                    if (log.isErrorEnabled()) {
                        log.error(e.getMessage());
                    }
                }
            }
            Validations.check(!StringUtils.isEmpty(objCassetteGetLotListOut)
                            && CimArrayUtils.getSize(objCassetteGetLotListOut.getLotIDList()) > 0, retCodeConfigEx.getNotFoundEmptyCast());
        }


        //==============================================================
        //
        // Check the availability Equipment, Port, Carrier, Lot.
        //
        //==============================================================


        //----------------------------------------
        // Equipment and Port
        //----------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("check port");
        }
        Infos.EqpPortInfo strEquipmentPortInfoGetOut = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = strEquipmentPortInfoGetOut.getEqpPortStatuses();
        int portLen = CimArrayUtils.getSize(eqpPortStatuses);
        Validations.check(CimArrayUtils.isEmpty(eqpPortStatuses), retCodeConfig.getNotFoundPort());

        if (log.isDebugEnabled()) {
            log.debug("get all portGroup");
        }
        Set<String> portGPSet = new HashSet<>();
        for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses) {
            portGPSet.add(eqpPortStatus.getPortGroup());
        }
        List<String> portGPs = new ArrayList<>(portGPSet);

        //------------------------------------------------------------
        //Check the number of port , Inpara.PortGroup existence.
        //------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("check load purPoseType and operation mode");
        }
        boolean portGPFoundFlag = false;
        String operationMode = null;
        for (String portGP : portGPs) {
            int portCntInPG = 0;
            if(CimStringUtils.unEqual(portGroupID, portGP)) {
                continue;
            } else {
                portGPFoundFlag = true;
            }
            for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses) {
                if (CimStringUtils.equals(portGP, eqpPortStatus.getPortGroup())) {
                    Validations.check(CimStringUtils.unEqual(BizConstant.SP_LOADPURPOSETYPE_OTHER, eqpPortStatus.getLoadPurposeType()),
                            retCodeConfigEx.getInvalidPurposeTypeForSorter(),eqpPortStatus.getLoadPurposeType());
                    operationMode = eqpPortStatus.getOperationMode();
                    String onlineMode = eqpPortStatus.getOnlineMode();
                    if (CimStringUtils.equals(SorterType.OperationMode.Auto_1.getValue(), operationMode)) {
                        if (ObjectIdentifier.isEmpty(controlJobID)) {
                            Validations.check(CimStringUtils.unEqual(SorterType.Action.T7CodeRead.getValue(), actionCode)
                                            && CimStringUtils.unEqual(SorterType.Action.WaferStart.getValue(), actionCode)
                                            && CimStringUtils.unEqual(SorterType.Action.LotTransfer.getValue(), actionCode)
                                            && CimStringUtils.unEqual(SorterType.Action.Separate.getValue(), actionCode)
                                            && CimStringUtils.unEqual(SorterType.Action.Combine.getValue(), actionCode)
                                            && CimStringUtils.unEqual(SorterType.Action.Reset.getValue(), actionCode)
                                            && CimStringUtils.unEqual(SorterType.Action.Flip.getValue(), actionCode)
                                            && CimStringUtils.unEqual(SorterType.Action.AdjustByTool.getValue(), actionCode)
                                            && CimStringUtils.unEqual(SorterType.Action.AdjustByMES.getValue(), actionCode)
                                            && !StrUtil.contains(actionCode, SorterType.Action.Rotate_.getValue()),
                                    retCodeConfigEx.getSorterActionCodeError());
                        }else {
                            Validations.check(CimStringUtils.unEqual(SorterType.Action.LotTransfer.getValue(), actionCode)
                                            && CimStringUtils.unEqual(SorterType.Action.WaferEnd.getValue(), actionCode)
                                            && CimStringUtils.unEqual(SorterType.Action.Flip.getValue(), actionCode)
                                            && !StrUtil.contains(actionCode, SorterType.Action.Rotate_.getValue()),
                                    retCodeConfigEx.getSorterActionCodeError());
                        }
                    }else if (CimStringUtils.equals(SorterType.OperationMode.Semi_1.getValue(), operationMode)) {
                        Validations.check(CimStringUtils.unEqual(SorterType.Action.RFIDRead.getValue(), actionCode)
                                        && CimStringUtils.unEqual(SorterType.Action.RFIDWrite.getValue(), actionCode),
                                retCodeConfigEx.getSorterActionCodeError());
                    }
                    Validations.check(!CimStringUtils.equals(SorterType.OperationMode.Auto_1.getValue(), operationMode)
                                    && !CimStringUtils.equals(SorterType.OperationMode.Auto_2.getValue(), operationMode)
                                    && !CimStringUtils.equals( SorterType.OperationMode.Semi_1.getValue(), operationMode),
                            retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID), operationMode);
                    portCntInPG++;
                }
            }
            //---------------------------------------------
            //The number of Port in portGroup should be 2.
            //---------------------------------------------
            if (log.isDebugEnabled()) {
                log.debug("The number of Port in portGroup should be 2.");
            }
            if(portCntInPG > BizConstant.SP_SORTER_PORTCOUNTINPORTGROUP) {
                Validations.check(retCodeConfigEx.getInvalidPortCountInPortgoup(), BizConstant.SP_SORTER_PORTCOUNTINPORTGROUP);
            }
        }
        Validations.check(CimBooleanUtils.isFalse(portGPFoundFlag), retCodeConfigEx.getNotFoundPortgroup());

        //------------------------------------------
        // Port , Port Load Sequence Number
        //------------------------------------------
        for (Info.ComponentJob componentJob : componentJobList) {
            boolean orgPortFoundFlag = false;
            boolean dstPortFoundFlag = false;
            boolean bLoadSeqFlag = false;
            for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses) {
                //---------------
                // Original Port
                //---------------
                if (log.isDebugEnabled()) {
                    log.debug("Original Port");
                }
                if(ObjectIdentifier.equalsWithValue(componentJob.getOriginalPortID(), eqpPortStatus.getPortID())) {
                    Validations.check(!CimStringUtils.equals(portGroupID, eqpPortStatus.getPortGroup()),
                            retCodeConfigEx.getPortPortgroupUnmatch());
                    orgPortFoundFlag = true;

                    if(eqpPortStatus.getLoadSequenceNumber() == 1) {
                        bLoadSeqFlag = true;
                    }


                    //Equipment has the Carrier Type ?
                    // wafer start时 OriginalCarrierID=FOSB时自动忽视
                    if (!SorterHandler.containsFOSB(componentJob.getOriginalCassetteID())) {
                        com.fa.cim.dto.Params.CarrierMoveFromIBRptParams carrierMoveFromIBRptParams = new com.fa.cim.dto.Params.CarrierMoveFromIBRptParams();
                        carrierMoveFromIBRptParams.setCarrierID(componentJob.getOriginalCassetteID());
                        carrierMoveFromIBRptParams.setEquipmentID(equipmentID);
                        carrierMoveFromIBRptParams.setDestinationPortID(componentJob.getOriginalPortID());
                        cassetteMethod.cassetteCategoryPortCapabilityCheckForContaminationControl(objCommon, carrierMoveFromIBRptParams);
                    }
                }

                //-------------------
                // Destination Port
                //-------------------
                if (log.isDebugEnabled()) {
                    log.debug("Destination Port");
                }
                if(ObjectIdentifier.equalsWithValue(componentJob.getDestinationPortID(), eqpPortStatus.getPortID())) {
                    Validations.check(!CimStringUtils.equals(portGroupID, eqpPortStatus.getPortGroup()),
                            retCodeConfigEx.getPortPortgroupUnmatch());
                    dstPortFoundFlag = true;

                    if(eqpPortStatus.getLoadSequenceNumber() == 1) {
                        bLoadSeqFlag = true;
                    }

                    //Equipment has the Carrier Type ?
                    // 当OriginalCarrierID action code =wafer start时 只有多对一的情况 OriginalCarrierID=FOSB11111时自动忽视
                    if (!SorterHandler.containsFOSB(componentJob.getDestinationCassetteID())) {
                        com.fa.cim.dto.Params.CarrierMoveFromIBRptParams carrierMoveFromIBRptParams = new com.fa.cim.dto.Params.CarrierMoveFromIBRptParams();
                        carrierMoveFromIBRptParams.setCarrierID(componentJob.getDestinationCassetteID());
                        carrierMoveFromIBRptParams.setEquipmentID(equipmentID);
                        carrierMoveFromIBRptParams.setDestinationPortID(componentJob.getDestinationPortID());
                        cassetteMethod.cassetteCategoryPortCapabilityCheckForContaminationControl(objCommon, carrierMoveFromIBRptParams);
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("check port exist");
            }
            Validations.check(!orgPortFoundFlag || !dstPortFoundFlag, retCodeConfig.getNotFoundPort());

            if (log.isDebugEnabled()) {
                log.debug("check load flag");
            }
            Validations.check(!bLoadSeqFlag, retCodeConfig.getInvalidLoadingSequence());
        }

        //-------------------------------------------------------
        // Operation Mode Semi-1 --> source and destination can't be greater than 1
        //-------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("check source and destination carrier size");
        }
        Validations.check(CimStringUtils.equals(SorterType.OperationMode.Semi_1.getValue(), operationMode)
                && (orgCarrierIDs.size() > 1 || dstCarrierIDs.size() > 1), retCodeConfig.getInvalidParameter());

        //------------------------------------------
        // Equipment Category "WaferSorter" ?
        //------------------------------------------
        if (log.isErrorEnabled()) {
            log.debug("check equipment category");
        }
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(CimStringUtils.unEqual(BizConstant.SP_MC_CATEGORY_WAFERSORTER, aPosMachine.getCategory()),
                retCodeConfig.getMachineTypeNotSorter());

        //------------------------------------------
        // Equipment is available ??
        //------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("check Equipment is available");
        }
        equipmentMethod.equipmentCheckAvail(objCommon, equipmentID);

        //------------------------------------------
        // Equipment has the some inhibitions?
        //------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Equipment has the some inhibitions");
        }
        Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
        List<Infos.EntityIdentifier> entities = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
        entityIdentifier.setObjectID(equipmentID);
        entities.add(entityIdentifier);
        entityInhibitAttributes.setEntities(entities);
        Infos.EntityInhibitCheckForEntitiesOut strEntityInhibitCheckForEntitiesOut = constraintMethod
                .constraintCheckForEntities(objCommon, entityInhibitAttributes);
        Validations.check(CimArrayUtils.isNotEmpty(strEntityInhibitCheckForEntitiesOut.getEntityInhibitInfo()),
                retCodeConfig.getInhibitEntity(),
                CimArrayUtils.getSize(strEntityInhibitCheckForEntitiesOut.getEntityInhibitInfo()),
                equipmentID);

        if (log.isDebugEnabled()) {
            log.debug("get all carrier");
        }
        List<ObjectIdentifier> allCarrierIDs = new ArrayList<>();
        allCarrierIDs.addAll(orgCarrierIDs);
        allCarrierIDs.addAll(dstCarrierIDs);
        allCarrierIDs.stream().distinct().collect(Collectors.toList());


        //-------------------------------------------------------------------
        // Check if carrier is reserved for retrieving lot on SLM operation
        //-------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Check if carrier is reserved for retrieving lot on SLM operation");
        }
        Optional.ofNullable(allCarrierIDs).orElseGet(Collections::emptyList).stream().forEach(carrierId -> {
            CimCassette tempCast = baseCoreFactory.getBO(CimCassette.class, carrierId);
            /*-----------------------------*/
            /*   Get SLMReserved Machine   */
            /*-----------------------------*/
            CimMachine aSLMRsvMachine = tempCast.getSLMReservedMachine();
            Validations.check(null != aSLMRsvMachine, retCodeConfig.getAlreadyReservedCassetteSlm(), carrierId);
        });

        //------------------------------------------------------------------------------
        // If Operation Mode is Semi-1 或者 Auto_1, the portGroup can have only one Sort Job.
        //------------------------------------------------------------------------------
        if (CimStringUtils.equals(operationMode, SorterType.OperationMode.Semi_1.getValue())
                || CimStringUtils.equals(operationMode, SorterType.OperationMode.Auto_1.getValue())) {
            if (log.isDebugEnabled()) {
                log.debug("Verify that portGroup A under the equipment does not exist soter job");
            }
            com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobs = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
            objSorterJobs.setEquipmentID(equipmentID);
            objSorterJobs.setPortGroup(portGroupID);
            List<Info.SortJobListAttributes> sorterJobListGetDR = this.sorterJobListGetDR(objCommon, objSorterJobs);
            if (CimArrayUtils.getSize(sorterJobListGetDR) > 0) {
                Validations.check(true, retCodeConfigEx.getExistSorterJobForEquipment(),ObjectIdentifier.fetchValue(equipmentID),
                        ObjectIdentifier.fetchValue(sorterJobListGetDR.get(0).getSorterJobID()));
            }
            if (log.isDebugEnabled()) {
                log.debug("Verify that only one carrier can exist and only one sort job");
            }
            Optional.ofNullable(allCarrierIDs).orElseGet(Collections::emptyList).stream().forEach(carrier -> {
                com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
                objSorterJobListGetDRIn.setCarrierID(carrier);
                List<Info.SortJobListAttributes> sortJobListAttributes = this.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
                if (CimArrayUtils.getSize(sortJobListAttributes) > 0) {
                    Validations.check(true, retCodeConfigEx.getExistSorterjobForCassette(),ObjectIdentifier.fetchValue(carrier),
                            ObjectIdentifier.fetchValue(sortJobListAttributes.get(0).getSorterJobID()));
                }
            });

            //------------------------------------------------------------------------------
            // 如果是FOSB的情况下，slotMap表的lot不能重复
            //------------------------------------------------------------------------------
            Optional.ofNullable(componentJobList).orElseGet(Collections::emptyList).stream()
                    .filter(cassette ->
                            SorterHandler.containsFOSB(cassette.getOriginalCassetteID())
                                    || SorterHandler.containsFOSB(cassette.getDestinationCassetteID()))
                    .forEach(data -> {
                        if (log.isDebugEnabled()) {
                            log.debug("Verify that componentJob can only have one lotID");
                        }
                        List<ObjectIdentifier> lotIDs = Optional.ofNullable(data.getWaferList()).orElseGet(Collections::emptyList)
                                .stream()
                                .map(Info.WaferSorterSlotMap::getLotID)
                                .distinct()
                                .collect(Collectors.toList());
                        Validations.check(CimArrayUtils.getSize(lotIDs) > 1, retCodeConfigEx.getFoundSlotmap());

                        if (log.isDebugEnabled()) {
                            log.debug("Query if lot exists sortJob");
                        }
                        CimSortJobSlotMapDO slotMapDO = new CimSortJobSlotMapDO();
                        slotMapDO.setLotID(ObjectIdentifier.fetchValue(lotIDs.get(0)));
                        long slotMapCount = cimJpaRepository.count(Example.of(slotMapDO));
                        Validations.check(slotMapCount > 0, retCodeConfigEx.getFoundSlotmap());
                    });

            //------------------------------------------------------------------------------
            //  T7CodeRead只能针对单个批次进行读取 / 如果lot处于翻转状态，或者存在Flip的sorter，那么不能继续创建T7CodeRead sorter
            //------------------------------------------------------------------------------
            Optional.ofNullable(componentJobList).orElseGet(Collections::emptyList).stream()
                    .filter(comp -> CimStringUtils.equals(SorterType.Action.T7CodeRead.getValue(), comp.getActionCode()))
                    .forEach(cassette -> {
                        List<ObjectIdentifier> lotIDs = Optional.ofNullable(cassette.getWaferList())
                                .orElseGet(Collections::emptyList)
                                .stream()
                                .map(Info.WaferSorterSlotMap::getLotID)
                                .distinct()
                                .collect(Collectors.toList());
                        Validations.check(CimArrayUtils.getSize(lotIDs) != 1, retCodeConfigEx.getFoundSlotmap());

                        CimLot lot = baseCoreFactory.getBO(CimLot.class, lotIDs.get(0));
                        boolean flip = lot.getFlip();
                        Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new Params.ObjSorterJobListGetDRIn();
                        objSorterJobListGetDRIn.setEquipmentID(equipmentID);
                        objSorterJobListGetDRIn.setLotID(lotIDs.get(0));
                        List<Info.SortJobListAttributes> sortJobListAttributes = this.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
                        Validations.check(flip || CimArrayUtils.isNotEmpty(sortJobListAttributes),
                                retCodeConfigEx.getLotFlipAndFlipSorter(),ObjectIdentifier.fetchValue(lotIDs.get(0)));
                    });

            //------------------------------------------------------------------------------
            //  WaferStart不能存在进行一对多
            //------------------------------------------------------------------------------
            long destCassetCount = Optional.ofNullable(componentJobList).orElseGet(Collections::emptyList).stream()
                    .filter(comp -> CimStringUtils.equals(SorterType.Action.WaferStart.getValue(), comp.getActionCode()))
                    .map(Info.ComponentJob::getDestinationCassetteID)
                    .distinct()
                    .count();
            Validations.check(destCassetCount > 1, retCodeConfigEx.getCassetteNotAvailable());
        }

        //-----------------------------------------------------------------------------------------
        //  将传入的source carrier的lotID/waferID/slotNum信息与数据库中信息进行对比
        //-----------------------------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Slot Map position is correct");
        }
        if (CimArrayUtils.isNotEmpty(orgCarrierIDs) && !actionCode.equals(SorterType.Action.RFIDRead.getValue())
                && !actionCode.equals(SorterType.Action.RFIDWrite.getValue())) {
            Set<ObjectIdentifier> lotIDSet = new HashSet<>();
            for (ObjectIdentifier orgCarrierID : orgCarrierIDs) {
                if (log.isDebugEnabled()) {
                    log.debug("Get slot map information");
                }
                List<Infos.WaferMapInCassetteInfo> strCassetteGetWaferMapDROut = cassetteMethod
                        .cassetteGetWaferMapDR(objCommon, orgCarrierID);

                if (log.isDebugEnabled()) {
                    log.debug("compare carrier lot/wafer/slotNum information");
                }
                for (Info.ComponentJob componentJob : componentJobList) {
                    if (ObjectIdentifier.equalsWithValue(orgCarrierID, componentJob.getOriginalCassetteID())) {
                        if (CimArrayUtils.isNotEmpty(componentJob.getWaferList())) {
                            for (Info.WaferSorterSlotMap sorterSlotMap : componentJob.getWaferList()) {
                                ObjectIdentifier inWaferID = sorterSlotMap.getWaferID();
                                ObjectIdentifier inLotID = sorterSlotMap.getLotID();
                                lotIDSet.add(inLotID);
                                int inSlotNum = sorterSlotMap.getOriginalSlotNumber().intValue();
                                boolean waferSlotMapFoundFlag = false;
                                if (CimArrayUtils.isNotEmpty(strCassetteGetWaferMapDROut)) {
                                    for (Infos.WaferMapInCassetteInfo cassetteInfo : strCassetteGetWaferMapDROut) {
                                        if (ObjectIdentifier.equalsWithValue(inLotID, cassetteInfo.getLotID())
                                                && ObjectIdentifier.equalsWithValue(inWaferID, cassetteInfo.getWaferID())
                                                && (inSlotNum == cassetteInfo.getSlotNumber())) {
                                            waferSlotMapFoundFlag = true;
                                            break;
                                        }
                                    }
                                    Validations.check(CimBooleanUtils.isFalse(waferSlotMapFoundFlag), retCodeConfig.getInvalidOrgWaferPosition());
                                }
                            }
                        }
                    }
                }
            }
            //chck 入参的lot是否被hold,如果被hold不能创建sortjob
            if (log.isDebugEnabled()) {
                log.debug("check lot whether hold");
            }
            if(lotIDSet.size()>0){
                lotIDSet.forEach(lotID->{
                    String lotHoldState = lotMethod.lotHoldStateGet(objCommon, lotID);
                    Validations.check(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD, lotHoldState),
                            retCodeConfig.getInvalidLotHoldStat());
                });
            }

        }

        //----------------------
        // Destination Carrier,sortJob不做transfer时
        //----------------------
        if (log.isDebugEnabled()) {
            log.debug("check destination carrier position");
        }
        if(!this.checkSpecifiedAction(actionCode)) {
            if (CimArrayUtils.isNotEmpty(dstCarrierIDs)) {
                for (ObjectIdentifier dstCarrierID : dstCarrierIDs) {
                    if (log.isDebugEnabled()) {
                        log.debug("Get Empty Slot of Carrier");
                    }
                    CimCassette aPosDestCassette = baseCoreFactory.getBO(CimCassette.class, dstCarrierID);
                    List<Integer> nEmptyPositions = aPosDestCassette.emptyPositions();
                    for (Info.ComponentJob componentJob : componentJobList) {
                        if (ObjectIdentifier.equalsWithValue(dstCarrierID, componentJob.getDestinationCassetteID())) {
                            if (log.isDebugEnabled()) {
                                log.debug("compare position");
                            }
                            List<Info.WaferSorterSlotMap> waferList = componentJob.getWaferList();
                            if (CimArrayUtils.isNotEmpty(waferList)) {
                                for (Info.WaferSorterSlotMap sorterSlotMap : waferList) {
                                    Boolean emptySlotFindFlag = false;
                                    int IndstSlotNum = sorterSlotMap.getDestinationSlotNumber().intValue();
                                    if (CimArrayUtils.isNotEmpty(nEmptyPositions)) {
                                        for (Integer nEmptyPosition : nEmptyPositions) {
                                            if (IndstSlotNum == nEmptyPosition) {
                                                emptySlotFindFlag = true;
                                                break;
                                            }
                                        }
                                        Validations.check(CimBooleanUtils.isFalse(emptySlotFindFlag), retCodeConfig.getInvalidOrgWaferPosition());
                                    }
                                }
                            }
                        }
                    }
                    //------------------------
                    // Status is available ?
                    //------------------------
                    if (log.isDebugEnabled()) {
                        log.debug("check carrier durable state");
                    }
                    CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, dstCarrierID);
                    Validations.check(CimStringUtils.unEqual(BizConstant.CIMFW_DURABLE_AVAILABLE, aCassette.getDurableState()),
                            retCodeConfig.getInvalidCassetteState(), aCassette.getDurableState(), aCassette.getIdentifier());
                }
            }
        }

        //----------------------------------
        // Original/Destination Carrier
        //----------------------------------
        if (log.isDebugEnabled()) {
            log.debug("check controlJob/XferState/dispatcherState");
        }
        if (CimArrayUtils.isNotEmpty(allCarrierIDs)) {
            for (ObjectIdentifier allCarrierID : allCarrierIDs) {
                //------------------------------------
                // Lot in Carrier has control Job ?
                //------------------------------------
                if (log.isDebugEnabled()) {
                    log.debug("check Lot in Carrier has control Job");
                }
                ObjectIdentifier strCassetteControlJobIDGetOut = cassetteMethod.cassetteControlJobIDGet(objCommon, allCarrierID);
                Validations.check(ObjectIdentifier.isNotEmpty(strCassetteControlJobIDGetOut), retCodeConfig.getCassetteControlJobFilled());

                //------------------------------------
                // Carrier Xfer status is EI ?,排除半自动的
                //------------------------------------
                if (log.isDebugEnabled()) {
                    log.debug("check carrier Xfer status");
                }
                if(CimStringUtils.unEqual(SorterType.OperationMode.Semi_1.getValue(), operationMode)){
                    String strCassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, allCarrierID);
                    Validations.check(CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, strCassetteTransferStateGetOut),
                            retCodeConfig.getInvalidCassetteTransferState());
                }

                //---------------------------------------
                // Carrier is reserved for dispatching?
                //---------------------------------------
                if (log.isDebugEnabled()) {
                    log.debug("check dispatch state");
                }
                if (ObjectIdentifier.isEmpty(controlJobID)) {
                    Boolean strCassetteDispatchStateGetOut = cassetteMethod.cassetteDispatchStateGet(objCommon, allCarrierID);
                    Validations.check(CimBooleanUtils.isTrue(strCassetteDispatchStateGetOut),
                            retCodeConfig.getAlreadyDispatchReservedCassette());
                }
            }
        }

        //----------------------------------------
        // Lot
        //----------------------------------------
        log.info( "##### Check Lot Information.");
        if(CimStringUtils.unEqual(SorterType.Action.RFIDRead.getValue(), actionCode)
                && CimStringUtils.unEqual(SorterType.Action.RFIDWrite.getValue(), actionCode)) {
            for (ObjectIdentifier allCarrierID : allCarrierIDs) {
                Infos.LotListInCassetteInfo lotList = cassetteMethod.cassetteLotListGetDR(objCommon, allCarrierID);
                List<ObjectIdentifier> lotIDList = lotList.getLotIDList();
                if (CimArrayUtils.isNotEmpty(lotIDList)) {
                    for (ObjectIdentifier lotID : lotIDList) {
                        if (log.isDebugEnabled()) {
                            log.debug("get lot flow batch");
                        }
                        try {
                            lotMethod.lotFlowBatchIDGet(objCommon, lotID);
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())) {
                                if (log.isDebugEnabled()) {
                                    log.debug( "lot_flowBatchID_GetDR() == RC_LOT_FLOW_BATCH_ID_BLANK or RC_OK, check OK!");
                                }
                            } else {
                                if (log.isErrorEnabled()) {
                                    log.error(e.getMessage());
                                }
                                throw e;
                            }
                        }

                        //----------------------------
                        // In Back Up Site ?
                        //----------------------------
                        if (log.isDebugEnabled()) {
                            log.debug("get lot backup info");
                        }
                        Infos.LotBackupInfo strLotBackupInfoGetOut = lotMethod.lotBackupInfoGet(objCommon, lotID);
                        Validations.check(CimBooleanUtils.isFalse(strLotBackupInfoGetOut.getCurrentLocationFlag())
                                || CimBooleanUtils.isTrue(strLotBackupInfoGetOut.getTransferFlag()), retCodeConfig.getLotInOthersite());

                        //----------------------------------
                        //  Check lot InterFabXfer state
                        //----------------------------------
                        if (log.isErrorEnabled()) {
                            log.debug("check lot Xfer state");
                        }
                        String strLotInterFabXferStateGetOut = lotMethod.lotInterFabXferStateGet(objCommon,lotID);
                        if(CimStringUtils.equals(strLotInterFabXferStateGetOut, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)) {
                            continue;
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("check lot hold condition for operation");
                        }
                        List<ObjectIdentifier> ckLotIDSeq = new ArrayList<>();
                        ckLotIDSeq.add(lotID);
                        lotMethod.lotCheckLockHoldConditionForOperation(objCommon, ckLotIDSeq);
                    }
                    //--------------------------------------
                    // Equipment is available for LOT ?
                    //--------------------------------------
                    if (log.isDebugEnabled()) {
                        log.debug("check avail for lot");
                    }
                    equipmentMethod.equipmentCheckAvailForLot(objCommon, equipmentID, lotList.getLotIDList());
                }


                List<ObjectIdentifier> userGroupIDs = new ArrayList<>();
                int userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);
                if (CimArrayUtils.isNotEmpty(lotIDList)) {
                    for (ObjectIdentifier lotID : lotIDList) {
                        //----------------------------------
                        //  Get InPostProcessFlag of Lot
                        //----------------------------------
                        if (log.isDebugEnabled()) {
                            log.debug("get post process flag");
                        }
                        Outputs.ObjLotInPostProcessFlagOut strLotInPostProcessFlagGetOut = lotMethod
                                .lotInPostProcessFlagGet(objCommon,lotID);

                        //----------------------------------
                        //  Check lot InterFabXfer state
                        //----------------------------------
                        if (log.isErrorEnabled()) {
                            log.debug("check lot Xfer state");
                        }
                        String strLotInterFabXferStateGetOut = lotMethod.lotInterFabXferStateGet(objCommon, lotID);

                        //----------------------------------------------
                        //  If Lot is in post process, returns error
                        //----------------------------------------------
                        if(CimBooleanUtils.isTrue(strLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot())) {
                            if(CimStringUtils.equals(strLotInterFabXferStateGetOut, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)) {
                                continue;
                            }
                            if (userGroupIDsLen == 0) {
                                /*---------------------------*/
                                /* Get UserGroupID By UserID */
                                /*---------------------------*/
                                if (log.isDebugEnabled()) {
                                    log.debug("Get UserGroupID By UserID");
                                }
                                userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                                userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);
                            }

                            int nCnt ;
                            for (nCnt = 0; nCnt < userGroupIDsLen; nCnt++) {
                                if (log.isDebugEnabled()) {
                                    log.debug( "# Loop({}) / userID : {}", nCnt, userGroupIDs.get(nCnt));
                                }
                            }
                            Validations.check(nCnt == userGroupIDsLen, retCodeConfig.getLotInPostProcess());
                        }
                    }
                }
            }
        }
    }

    @Override
    public Params.SJCreateReqParams sorterJobCreate(Infos.ObjCommon objCommon, Params.SJCreateReqParams sortJobListAttributes) {
        //----------------------------------------------
        // Create Sort Job Information
        //----------------------------------------------
        String tmpSorterJobID = String.format("%s-%s", sortJobListAttributes.getEquipmentID().getValue(), objCommon.getTimeStamp().getReportTimeStamp());
        sortJobListAttributes.setSorterJobID(ObjectIdentifier.buildWithValue(tmpSorterJobID));
        //-------------------------------
        // Set Sort Job Status
        //   Semi-1 : Create
        //   Auto-1 : Create
        //   Auto-2 : Wait To Executing
        //-------------------------------

        sortJobListAttributes.setSorterJobStatus(SorterType.Status.Created.getValue());

        int jobSeq = 0;
        for (Info.ComponentJob componentJob: sortJobListAttributes.getStrSorterComponentJobListAttributesSequence()) {
            //-----------------------------------------------------
            // Generate Component Job ID
            //   SortJobID + "-" + jobSeq = ComponentJobID
            //-----------------------------------------------------
            jobSeq++;
            String tmpCompoSorterJobID = String.format("%s-%02d",
                    ObjectIdentifier.fetchValue(sortJobListAttributes.getSorterJobID()), jobSeq);
            componentJob.setComponentJobID(tmpCompoSorterJobID);
            if (log.isDebugEnabled()) {
                log.debug("###  Ganerated Component Job ID : {} -> {}", jobSeq,
                        componentJob.getComponentJobID());
            }

            //-------------------------------
            // Set Component Job Status
            //   Auto-1 : Xfer
            //-------------------------------
            componentJob.setComponentJobStatus(SorterType.Status.Created.getValue());

        }
        sortJobListAttributes.setComponentCount(CimArrayUtils.getSize(sortJobListAttributes.getStrSorterComponentJobListAttributesSequence()));
        //---------------------------------
        // Call sorterJobInsertDR
        //---------------------------------
        sorterJobInsertDR(objCommon, sortJobListAttributes);
        //--------------------------------------------------------------
        //  Return to Caller
        //--------------------------------------------------------------
        return sortJobListAttributes;
    }

    @Override
    public CimSorterJob getCimSortJobByComponentJobID(String componentJobID) {
        return sorterJobManager.getCimSortJobByComponentJobID(componentJobID);
    }

    @Override
    public Info.SortJobPostAct getPostAct(String sortJobID) {
        CimSorterJob sorterJob = baseCoreFactory.getBOByIdentifier(CimSorterJob.class, sortJobID);
        Validations.check(null==sorterJob,retCodeConfig.getInvalidInputParam());
        SorterJobDTO.SortJobPostAct dbPostAct = sorterJob.getPostAct();
        Info.SortJobPostAct postAct =new Info.SortJobPostAct();
        BeanUtils.copyProperties(dbPostAct,postAct);
         return postAct;
    }


    @Override
    public List<Info.SlotmapHisRecord> getSorterjobSlotmapHisRecord(String sortJobId) {
        String sql ="SELECT\n" +
                "\tsj.ACTION,\n" +
                "\tslotMap.WAFER_ID,\n" +
                "\tslotMap.DEST_POSITION \n" +
                "FROM\n" +
                "\tOHSORTJOB sj,\n" +
                "\tOHSORTJOB_COMP_SLOTMAP slotMap \n" +
                "WHERE\n" +
                "\tsj.ID = slotMap.REFKEY \n" +
                "\tAND sj.SORTER_JOB_CATEGORY = 'Completed' \n" +
                "\tAND sj.SORTER_JOB_ID = ?1";
        return cimJpaRepository.query(sql,Info.SlotmapHisRecord.class, sortJobId);
    }


    public void sorterJobInsertDR(Infos.ObjCommon objCommon,Params.SJCreateReqParams jobListAttributes ){
        String actionCode =jobListAttributes.getStrSorterComponentJobListAttributesSequence().get(0).getActionCode();
        jobListAttributes.setComponentCount(CimArrayUtils.getSize(jobListAttributes.getStrSorterComponentJobListAttributesSequence()));

        if (log.isDebugEnabled()) {
            log.debug("create sortJob bo");
        }
        CimSorterJob cimSorterJob = sorterJobManager.createCimSorterJob(ObjectIdentifier
                .fetchValue(jobListAttributes.getSorterJobID()));

        if (log.isDebugEnabled()) {
            log.debug("Get Previous Sorter Job ID");
        }
        CimSorterJob previousSorterJobFor = sorterJobManager.findPreviousSorterJobFor(ObjectIdentifier.fetchValue(jobListAttributes.getEquipmentID()));
        String prevSorterJobIdForEqpId = previousSorterJobFor == null ? null : previousSorterJobFor.getIdentifier();

        if (log.isDebugEnabled()) {
            log.debug("insert sortJob");
        }
        SorterJobDTO.SortJob sortJob = new SorterJobDTO.SortJob();
        sortJob.setSorterJobID(jobListAttributes.getSorterJobID());
        sortJob.setEquipmentID(jobListAttributes.getEquipmentID());
        sortJob.setPortGroupID(jobListAttributes.getPortGroupID());
        sortJob.setSorterJobStatus(jobListAttributes.getSorterJobStatus());
        sortJob.setComponentCount(jobListAttributes.getComponentCount());
        sortJob.setPreSorterJobID(prevSorterJobIdForEqpId == null ? ObjectIdentifier.buildWithValue("") :
                ObjectIdentifier.buildWithValue(prevSorterJobIdForEqpId));
        sortJob.setWaferIDReadFlag(jobListAttributes.getWaferIDReadFlag());
        sortJob.setRequestUserID(objCommon.getUser().getUserID());
        sortJob.setRequestTimeStamp(new Timestamp(System.currentTimeMillis()));
        sortJob.setCtrlJobID(ObjectIdentifier.fetchValue(jobListAttributes.getControlJobID()));
        sortJob.setCreateUserId(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        sortJob.setCreateTimeStamp(new Timestamp(System.currentTimeMillis()));
        sortJob.setCreateUserObj(ObjectIdentifier.fetchReferenceKey(objCommon.getUser().getUserID()));
        cimSorterJob.setSorterJobInfo(sortJob);

        if (log.isDebugEnabled()) {
            log.debug("insert postact");
        }
        if (SorterType.Action.WaferStart.getValue().equals(actionCode)
                || SorterType.Action.Combine.getValue().equals(actionCode)
                || SorterType.Action.Separate.getValue().equals(actionCode) ) {
            Info.SortJobPostAct temPostAct = jobListAttributes.getPostAct();
            Validations.check(temPostAct == null, retCodeConfigEx.getNotFoundSorterPostAct());
            temPostAct.setSorterJobID(ObjectIdentifier.fetchValue(jobListAttributes.getSorterJobID()));
            SorterJobDTO.SortJobPostAct postAct = new SorterJobDTO.SortJobPostAct();
            postAct.setActionCode(actionCode);
            postAct.setSorterJobID(jobListAttributes.getSorterJobID());
            postAct.setVendorId(temPostAct.getVendorId());
            postAct.setProductOrderId(temPostAct.getProductOrderId());
            postAct.setWaferCount(temPostAct.getWaferCount());
            postAct.setSourceProductId(temPostAct.getSourceProductId());
            postAct.setChildLotId(temPostAct.getChildLotId());
            postAct.setParentLotId(temPostAct.getParentLotId());
            cimSorterJob.setSorterPostActInfos(postAct);
        }

        if (log.isDebugEnabled()) {
            log.debug("insert componentJob and slotMap");
        }
        String previousComponentJobID = null;
        for (Info.ComponentJob componentJob : jobListAttributes
                .getStrSorterComponentJobListAttributesSequence()) {
            SorterJobDTO.SorterjobComponent sorterjobComponent = new SorterJobDTO.SorterjobComponent();
            sorterjobComponent.setComponentJobID(ObjectIdentifier.buildWithValue(componentJob.getComponentJobID()));
            sorterjobComponent.setComponentJobStatus(componentJob.getComponentJobStatus());
           //actionCode =waferStart 来源carrier=FOSB
            if (SorterHandler.containsFOSB(componentJob.getOriginalCassetteID())
                    && !SorterHandler.containsFOSB(componentJob.getDestinationCassetteID())) {
                sorterjobComponent.setSourceCassetteID(ObjectIdentifier
                        .buildWithValue(componentJob.getOriginalCassetteID().getValue() + SorterHandler.generateSixDigits()));
                sorterjobComponent.setDestCassetteID(componentJob.getDestinationCassetteID());
            }  else {
                sorterjobComponent.setSourceCassetteID(componentJob.getOriginalCassetteID());
                sorterjobComponent.setDestCassetteID(componentJob.getDestinationCassetteID());
            }
            sorterjobComponent.setSourcePortID(componentJob.getOriginalPortID());
            sorterjobComponent.setDestPortID(componentJob.getDestinationPortID());
            sorterjobComponent.setPreviousComponentJobID(previousComponentJobID);
            sorterjobComponent.setReqTimeStamp(new Timestamp(System.currentTimeMillis()));
            sorterjobComponent.setReqUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
            sorterjobComponent.setActionCode(componentJob.getActionCode());

            List<SorterJobDTO.SlotMapInfo> slotMapInfoList = new ArrayList<>();
            sorterjobComponent.setSlotMapInfoList(slotMapInfoList);
            if (CimArrayUtils.isNotEmpty(componentJob.getWaferList())) {
                for (Info.WaferSorterSlotMap waferSorterSlotMap : componentJob.getWaferList()) {
                    SorterJobDTO.SlotMapInfo slotMapInfo = new SorterJobDTO.SlotMapInfo();
                    slotMapInfoList.add(slotMapInfo);
                    slotMapInfo.setComponentJobID(ObjectIdentifier.fetchValue(sorterjobComponent.getComponentJobID()));
                    slotMapInfo.setAliasName(waferSorterSlotMap.getAliasName());
                    slotMapInfo.setLotID(waferSorterSlotMap.getLotID());
                    slotMapInfo.setWaferID(waferSorterSlotMap.getWaferID());
                    slotMapInfo.setDestPosition(waferSorterSlotMap.getDestinationSlotNumber());
                    slotMapInfo.setSourcePosition(waferSorterSlotMap.getOriginalSlotNumber());
                    slotMapInfo.setReqTimeStamp(new Timestamp(System.currentTimeMillis()));
                    slotMapInfo.setReqUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("insert component job and slotmap");
            }
            cimSorterJob.setSorterComponentJobInfos(sorterjobComponent);
            previousComponentJobID = ObjectIdentifier.fetchValue(sorterjobComponent.getComponentJobID());
        }
    }

    @Override
    public Info.SortJobInfo getSortJobInfoResultBySortJob(Infos.ObjCommon objCommon,ObjectIdentifier sortJobId, String componentJobId) {
        Info.SortJobInfo infoResult = new Info.SortJobInfo();
        List<Info.SorterComponentJobListAttributes> componentJobListAttributes = this
                .sorterComponentJobInfoGetDR(objCommon, sortJobId, false);

        Validations.check(CimArrayUtils.getSize(componentJobListAttributes) == 0,
                retCodeConfig.getInvalidInputParam());
        boolean foundFlag = false;
        Info.SorterComponentJobListAttributes attributes = null;
        for (Info.SorterComponentJobListAttributes jobListAttributes : componentJobListAttributes) {
            if (Objects.equals(ObjectIdentifier.fetchValue(jobListAttributes.getSorterComponentJobID()),
                    componentJobId)) {
                attributes = jobListAttributes;
                foundFlag=true;
                break;
            }

        }
        getComponentJob(foundFlag,attributes,infoResult);

        return infoResult;
    }


    @Override
    public Page<Results.SortJobHistoryResult> getSortJobHistory(Infos.ObjCommon objCommon, Params.SortJobHistoryParams params) {
        StringBuffer buffer =new StringBuffer();
        String sql ="SELECT\n" +
                "\this.EQP_ID,his.PORT_GROUP_ID,his.TRX_USER_ID,his.SORTER_JOB_STATUS,his.SORTER_JOB_ID," +
                "his.STORE_TIME,his.ID,his.COMPONENT_JOB_COUNT,his.CTRLJOB_ID\n" +
                "FROM\n" +
                "\tOHSORTJOB his \n" +
                "WHERE\n" +
                "\this.EQP_ID = '%s' \n" +
                "\tAND EVENT_CREATE_TIME ";
        String eqpIdSql = String.format(sql, ObjectIdentifier.fetchValue(params.getEquipmentID()));
        String startTimeStampSql = String.format("BETWEEN TO_TIMESTAMP('%s', 'yyyy-mm-dd hh24:mi:ss.ff')", params.getStartTimeStamp());
        String endTimeStampSql = String.format("AND TO_TIMESTAMP( '%s', 'yyyy-mm-dd hh24:mi:ss.ff' ) " +
                "ORDER BY EVENT_CREATE_TIME DESC ", params.getEndTimeStamp());

        buffer.append(eqpIdSql).append(startTimeStampSql).append(endTimeStampSql);
        Page<Object[]> queryPage = cimJpaRepository.query(buffer.toString(), params.getSearchCondition());
        List<Object[]> content = queryPage.getContent();
        List<Results.SortJobHistoryResult> list =new ArrayList<>();
        if(CimArrayUtils.getSize(content)>0) {
            content.forEach(data->{
                Results.SortJobHistoryResult historyResult =new  Results.SortJobHistoryResult();
                historyResult.setSorterJobID(ObjectIdentifier.build((String) data[4],(String) data[6]));
                historyResult.setRequestUserID(ObjectIdentifier.buildWithValue((String) data[2]));
                historyResult.setControlJobID(ObjectIdentifier.buildWithValue((String) data[8]));
                historyResult.setEquipmentID(ObjectIdentifier.buildWithValue((String) data[0]));
                historyResult.setPortGroup((String) data[1]);
                historyResult.setComponentJobCount(((BigDecimal) data[7]).intValue());
                historyResult.setReqTime((Timestamp) data[5]);
                historyResult.setSorterJobStatus((String) data[3]);
                historyResult.setComponentJobs(getComponentJobHistoryRecordReListByRefkey(objCommon,(String) data[6]));
                historyResult.setPostAct(getPostActHistoryRecordByRefkey(objCommon, (String)data[6]));
                list.add(historyResult);
            });
        }
        return new PageImpl<>(list, queryPage.getPageable(), queryPage.getTotalElements());
    }

    private Results.SortJobPostAct getPostActHistoryRecordByRefkey(Infos.ObjCommon objCommon, String sortJobHistoryID) {
        Results.SortJobPostAct sortJobPostAct = new Results.SortJobPostAct();
        String sql = "SELECT\n" +
                "\tSORTER_JOB_ID,\n" +
                "\tACTION_CODE,\n" +
                "\tPRODUCT_ORDER_ID,\n" +
                "\tVENDOR_ID,\n" +
                "\tWAFER_COUNT,\n" +
                "\tSOURCE_PRODUCT_ID \n" +
                "FROM\n" +
                "\tOHSORTJOB_POSTACT \n" +
                "WHERE\n" +
                "\tREFKEY = ?";
        Object[] postAct = cimJpaRepository.queryOne(sql, sortJobHistoryID);
        if (postAct == null || CimObjectUtils.isEmpty((postAct[0]))) return null;
        sortJobPostAct.setSorterJobID(((String) postAct[0]));
        sortJobPostAct.setActionCode(((String) postAct[1]));
        sortJobPostAct.setProductOrderId(((String) postAct[2]));
        sortJobPostAct.setVendorId(((String) postAct[3]));
        sortJobPostAct.setWaferCount(CimNumberUtils.intValue((BigDecimal)postAct[4]));
        sortJobPostAct.setSourceProductId(((String) postAct[5]));
        return sortJobPostAct;
    }


    public List<Info.SorterComponentJobListAttributes> getComponentJobHistoryRecordReListByRefkey(Infos.ObjCommon objCommon,String refkey ){
        String queryCompSql = "SELECT A.COMPONENT_JOB_ID,A.COMPONENT_JOB_STATUS,A.SRC_CARRIER_ID,A.DEST_CARRIER_ID,A.SRC_PORT_ID," +
                "A.DEST_PORT_ID,A.ACTION_CODE FROM OHSORTJOB_COMP A WHERE  A.REFKEY =?1";
        List<Object[]> queryResult = cimJpaRepository.query(queryCompSql,refkey);
        if (CimObjectUtils.isEmpty(queryResult)) {
            return null;
        }
        List<Info.SorterComponentJobListAttributes> componentJobs = new ArrayList<>();
        for (Object[] object : queryResult) {
            Info.SorterComponentJobListAttributes componentJob = new Info.SorterComponentJobListAttributes();
            componentJobs.add(componentJob);
            String srcCastID = (String) object[2];
            String destCastID = (String) object[3];
            componentJob.setSorterComponentJobID( ObjectIdentifier.buildWithValue((String) object[0]));
            componentJob.setOriginalCarrierID( ObjectIdentifier.buildWithValue(srcCastID));
            componentJob.setOriginalPortID( ObjectIdentifier.buildWithValue((String) object[4]));
            componentJob.setDestinationCarrierID( ObjectIdentifier.buildWithValue(destCastID));
            componentJob.setDestinationPortID( ObjectIdentifier.buildWithValue((String) object[5]));
            componentJob.setComponentSorterJobStatus((String) object[1]);
            componentJob.setActionCode((String) object[6]);
            //----------------------------------------------------------
            //  Get Xfer State for original cassette
            //----------------------------------------------------------
            if(!SorterHandler.containsFOSB(ObjectIdentifier.buildWithValue(srcCastID))){
                Outputs.ObjCassetteTransferInfoGetDROut objCassetteTransferInfoGetDROut1 = this.
                        cassetteTransferInfoGetDR(objCommon, componentJob.getOriginalCarrierID());
                componentJob.setOriginalCarrierTransferState(objCassetteTransferInfoGetDROut1.getTransferStatus());
                componentJob.setOriginalCarrierEquipmentID(objCassetteTransferInfoGetDROut1.getEquipmentID());
                componentJob.setOriginalCarrierStockerID(objCassetteTransferInfoGetDROut1.getStockerID());
            }

            //----------------------------------------------------------
            //  Get Xfer State for destination cassette
            //----------------------------------------------------------
            if(!SorterHandler.containsFOSB(ObjectIdentifier.buildWithValue(destCastID))){
                Outputs.ObjCassetteTransferInfoGetDROut objCassetteTransferInfoGetDROut2 = this
                        .cassetteTransferInfoGetDR(objCommon, componentJob.getDestinationCarrierID());
                componentJob.setDestinationCarrierTranferStatus(objCassetteTransferInfoGetDROut2.getTransferStatus());
                componentJob.setDestinationCarrierEquipmentID(objCassetteTransferInfoGetDROut2.getEquipmentID());
                componentJob.setDestinationCarrierStockerID(objCassetteTransferInfoGetDROut2.getStockerID());
            }
            //----------------------------------------------------------
            //  Get Sorter Component Job Detail Information
            //----------------------------------------------------------
            String querySlotMapSql = "SELECT\n" +
                    "\tslotMap.LOT_ID,\n" +
                    "\tslotMap.WAFER_ID,\n" +
                    "\tslotMap.DEST_POSITION,\n" +
                    "\tslotMap.SRC_POSITION,\n" +
                    "\tslotMap.ALIAS_NAME,\n" +
                    "\tslotMap.SORTER_STATUS\n" +
                    "FROM\n" +
                    "\tOHSORTJOB_COMP_SLOTMAP slotMap WHERE  slotMap.REFKEY=?1";
            List<Object[]> dbSlotMap = cimJpaRepository.query(querySlotMapSql,refkey);
            if (!CimObjectUtils.isEmpty(dbSlotMap)) {
                List<Info.WaferSorterSlotMap> waferSorterSlotMapList = new ArrayList<>();
                componentJob.setWaferSorterSlotMapList(waferSorterSlotMapList);
                for (Object[] obj : dbSlotMap) {
                    Info.WaferSorterSlotMap waferSorterSlotMap = new Info.WaferSorterSlotMap();
                    waferSorterSlotMapList.add(waferSorterSlotMap);
                    waferSorterSlotMap.setWaferID(ObjectIdentifier.buildWithValue((String)obj[1]));
                    waferSorterSlotMap.setLotID(ObjectIdentifier.buildWithValue((String)obj[0]));
                    waferSorterSlotMap.setDestinationSlotNumber(((BigDecimal)obj[2]).intValue());
                    waferSorterSlotMap.setOriginalSlotNumber(((BigDecimal)obj[3]).intValue());
                    waferSorterSlotMap.setAliasName((String)obj[4]);
                    waferSorterSlotMap.setStatus(((BigDecimal)obj[5]).intValue() != 0);

                }
            }
        }
        return componentJobs;
    }





    @Override
    public Info.LotAliasName checkLotAndAliasNameRelationship(ObjectIdentifier lotId, String aliasName) {
        Info.LotAliasName info =new Info.LotAliasName();
        if (StringUtils.isEmpty(aliasName)) {
            info.setCompareResults(false);
            info.setReservaLotID(lotId);
            return info;
        }
        String sql = "SELECT LOT_ID FROM OMWAFER WHERE OMWAFER.ALIAS_WAFER_NAME =?1";
        Object[] objects = cimJpaRepository.queryOne(sql, aliasName);
        if (null != objects) {
            if (!CimStringUtils.equals(lotId,(String) objects[0])) {
                info.setCompareResults(false);
                info.setReservaLotID(lotId);
                info.setRelationLotID(ObjectIdentifier.buildWithValue((String) objects[0]));
            }else {
                info.setCompareResults(true);
                info.setReservaLotID(lotId);
                info.setRelationLotID(ObjectIdentifier.buildWithValue((String) objects[0]));
            }
        } else {
            info.setCompareResults(false);
            info.setReservaLotID(lotId);
        }
        return info;
    }

    @Override
    public List<String> getVendorlotIDs(ObjectIdentifier sortJobId) {
        CimSorterJob sorterJob = baseCoreFactory.getBOByIdentifier(CimSorterJob.class, ObjectIdentifier.fetchValue(sortJobId));
        Validations.check(null == sorterJob,retCodeConfig.getInvalidInputParam());
        Set<String> vendorlotIDs = sorterJob.findVendorlotIDs();

        List<String> resultVendorlotIDs =new ArrayList<>();
        if(!CimArrayUtils.isEmpty(vendorlotIDs)){
            for (String vendorlotID :vendorlotIDs) {
                com.fa.cim.newcore.bo.product.CimLot cimLot = baseCoreFactory.getBOByIdentifier(com.fa.cim.newcore.bo.product.CimLot.class,
                        vendorlotID);
                Validations.check(cimLot == null, retCodeConfig.getNotFoundLot());
                //数量cimLot.getQuantity()数量为0就注册
                if(cimLot.getQuantity()==0){
                    resultVendorlotIDs.add(vendorlotID);
                }
            }
        }
        return resultVendorlotIDs;
    }

    private boolean checkSpecifiedAction(String actionCode) {
        if (SorterType.Action.T7CodeRead.getValue().equals(actionCode)
                || SorterType.Action.Reset.getValue().equals(actionCode)
                || StrUtil.contains(actionCode, SorterType.Action.Rotate_.getValue())
                || SorterType.Action.Flip.getValue().equals(actionCode)
                || SorterType.Action.RFIDRead.getValue().equals(actionCode)
                || SorterType.Action.RFIDWrite.getValue().equals(actionCode)
                || SorterType.Action.AdjustByMES.getValue().equals(actionCode)
                || SorterType.Action.AdjustByTool.getValue().equals(actionCode)) {
            return true;
        }
        return false;
    }

    @Override
    public void setAliasNameForComponentJobID(List<String> aliasNames, String componentJobID,String sortJobId) {
        CimSorterJob sorterJob = baseCoreFactory.getBOByIdentifier(CimSorterJob.class, sortJobId);
        Validations.check(StringUtils.isEmpty(sorterJob),retCodeConfigEx.getNotFoundSorterjob());
        sorterJob.setAliasNameForComponentJobID(aliasNames,componentJobID);
    }

    @Override
    public String getCarrierCategory(String carrierID) {
        return baseCoreFactory.getBOByIdentifier(CimCassette.class, carrierID).getCassetteCategory();
    }

    @Override
    public String generateFosbID() {
        String sql = "SELECT DISTINCT CARRIER_ID from OMCARRIER WHERE  regexp_like (CARRIER_ID,'JFS[0-9]{5}+$') ORDER by CARRIER_ID asc";
        String tempCarrierID = "JFS";
        int tempCode = 0;
        List<Object[]> dbCarrierIds = cimJpaRepository.query(sql);
        int dbSize = CimArrayUtils.getSize(dbCarrierIds);
        if (dbSize > 0) {
            Object[] objects = dbCarrierIds.get(dbSize - 1);
            String carrierID = (String) objects[0];
            carrierID = carrierID.substring(carrierID.length() - 5, carrierID.length());
            Validations.check(carrierID.equals(maxNumber),retCodeConfigEx.getFosbIdQuantityExceedingLimit());
            tempCode = Integer.valueOf(carrierID);
        }
        return tempCarrierID+autoGenericCode(tempCode, 5);
    }
    private String autoGenericCode(int code, int num) {
        return String.format("%0" + num + "d", code + 1);
    }

    @Override
    public void updateLotFilpStatus(String lotID) {
        CimLot cimLot = baseCoreFactory.getBOByIdentifier(CimLot.class, lotID);
        Validations.check(null == cimLot, retCodeConfig.getNotFoundLot());
        if (cimLot.getFlip()) {
            cimLot.setFilp(false);
        } else if (!cimLot.getFlip()) {
            cimLot.setFilp(true);
        }
    }

    @Override
    public void updateRelationFoupFlag(ObjectIdentifier srcCarrierID, ObjectIdentifier desCarrierID) {
        CimCassette desCassette = baseCoreFactory
                .getBOByIdentifier(CimCassette.class, ObjectIdentifier.fetchValue(desCarrierID));
        CimCassette srcCassette = baseCoreFactory
                .getBOByIdentifier(CimCassette.class, ObjectIdentifier.fetchValue(srcCarrierID));
        Validations.check(null == srcCassette || null == desCassette, retCodeConfig.getNotFoundCassette());
        //目的地carrier改成true
       /* if (CimObjectUtils.equalsWithValue(srcCarrierID, desCarrierID)) {
                desCassette.setRelationFoupFlag(true);
        } else {
            //目的地carrier改成true,来源的carrier改成false
                srcCassette.setRelationFoupFlag(false);
                desCassette.setRelationFoupFlag(true);
        }*/
        desCassette.setRelationFoupFlag(true);
    }

    @Override
    public Info.SortJobInfo getRecentlyHistory(Infos.ObjCommon objCommon, Params.SJListInqParams params, String actionCode) {
        String equipmentID = ObjectIdentifier.fetchValue(params.getEquipmentID());
        String carrierID = ObjectIdentifier.fetchValue(params.getCarrierID());
        String lotID = ObjectIdentifier.fetchValue(params.getLotID());
        String sql = "SELECT\n" +
                "\tsj.ID sjID,\n" +
                "\tcj.ID cjID,\n" +
                "\tsm.LOT_ID,\n" +
                "\tsm.WAFER_ID,\n" +
                "\tsm.DEST_POSITION,\n" +
                "\tsm.SRC_POSITION,\n" +
                "\tsm.ALIAS_NAME,\n" +
                "\tsj.EVENT_CREATE_TIME \n" +
                "FROM\n" +
                "\tOHSORTJOB sj,\n" +
                "\tOHSORTJOB_COMP cj,\n" +
                "\tOHSORTJOB_COMP_SLOTMAP sm \n" +
                "WHERE\n" +
                "\tsj.ID = cj.REFKEY \n" +
                "\tAND sj.ID = sm.REFKEY \n" +
                "\tAND sm.COMPONENT_JOB_ID = cj.COMPONENT_JOB_ID \n" +
                "\tAND cj.OPERATION = 'Component Job Comp'";
//        if (CimObjectUtils.isNotEmpty(params.getEquipmentID())) {
//            sql += String.format(" AND sj.EQP_ID = '%s'", equipmentID);
//        }
        if (ObjectIdentifier.isNotEmpty(params.getCarrierID())) {
            sql += String.format(" AND ( cj.DEST_CARRIER_ID = '%s' OR cj.SRC_CARRIER_ID = '%s' ) ", carrierID, carrierID);
        }
        if (ObjectIdentifier.isNotEmpty(params.getLotID())) {
            sql += String.format(" AND sm.LOT_ID = '%s'", lotID);
        }
        if (CimStringUtils.isNotEmpty(actionCode)) {
            sql += String.format(" AND cj.ACTION_CODE = '%s'", actionCode);
        }
        List<Info.SlotMapHistory> slotMapHistories = new ArrayList<>();
        List<Object[]> slotMaps = cimJpaRepository.query(sql);
        if (CimArrayUtils.isNotEmpty(slotMaps)) {
            for (Object[] slotMap : slotMaps) {
                Info.SlotMapHistory slotMapHistory = new Info.SlotMapHistory();
                slotMapHistory.setSortJobHisID(((String) slotMap[0]));
                slotMapHistory.setComponentJobHisID(((String) slotMap[1]));
                slotMapHistory.setLotID(((String) slotMap[2]));
                slotMapHistory.setWaferID(((String) slotMap[3]));
                slotMapHistory.setDestPosition(CimNumberUtils.intValue((BigDecimal)slotMap[4]));
                slotMapHistory.setSrcPosition(CimNumberUtils.intValue((BigDecimal)slotMap[5]));
                slotMapHistory.setAliasName(((String) slotMap[6]));
                slotMapHistory.setEventCreateTime(((Timestamp) slotMap[7]));
                slotMapHistories.add(slotMapHistory);
            }
        }
        Info.SlotMapHistory firstSlotMapHistory = slotMapHistories.stream()
                .sorted((a, b) -> b.getEventCreateTime().compareTo(a.getEventCreateTime()))
                .findFirst()
                .orElse(null);

        Info.SortJobInfo sortJobInfo = null;
        if (firstSlotMapHistory != null) {
            sortJobInfo = new Info.SortJobInfo();
            String sortJobHisID = firstSlotMapHistory.getSortJobHisID();
            String sjSql = "SELECT\n" +
                    "\tSORTER_JOB_ID,\n" +
                    "\tTRX_USER_ID,\n" +
                    "\tCTRLJOB_ID,\n" +
                    "\tEQP_ID,\n" +
                    "\tPORT_GROUP_ID,\n" +
                    "\tWAFER_ID_READ_FLAG \n" +
                    "FROM\n" +
                    "\tOHSORTJOB \n" +
                    "WHERE\n" +
                    "\tID = ?1";
            Object[] sortJobHis = cimJpaRepository.queryOne(sjSql, sortJobHisID);
            sortJobInfo.setSorterJobID(ObjectIdentifier.buildWithValue(((String) sortJobHis[0])));
            sortJobInfo.setRequestUserID(ObjectIdentifier.buildWithValue(((String) sortJobHis[1])));
            sortJobInfo.setControlJobID(ObjectIdentifier.buildWithValue(((String) sortJobHis[2])));
            sortJobInfo.setEquipmentID(ObjectIdentifier.buildWithValue(((String) sortJobHis[3])));
            sortJobInfo.setPortGroup(((String) sortJobHis[4]));
            sortJobInfo.setWaferIDReadFlag(CimNumberUtils.intValue((BigDecimal)sortJobHis[5]) == 1);

            String componentJobHisID = firstSlotMapHistory.getComponentJobHisID();
            String cjSql = "SELECT\n" +
                    "\tCOMPONENT_JOB_ID,\n" +
                    "\tCOMPONENT_JOB_STATUS,\n" +
                    "\tSRC_CARRIER_ID,\n" +
                    "\tDEST_CARRIER_ID,\n" +
                    "\tSRC_PORT_ID,\n" +
                    "\tDEST_PORT_ID,\n" +
                    "\tACTION_CODE,\n" +
                    "\tOPERATION \n" +
                    "FROM\n" +
                    "\tOHSORTJOB_COMP \n" +
                    "WHERE\n" +
                    "\tID = ?1";
            Object[] componentJobHis = cimJpaRepository.queryOne(cjSql, componentJobHisID);
            Info.ComponentJob componentJob = new Info.ComponentJob();
            sortJobInfo.setComponentJob(componentJob);
            componentJob.setComponentJobID(((String) componentJobHis[0]));
            componentJob.setComponentJobStatus(((String) componentJobHis[1]));
            componentJob.setOriginalCassetteID(ObjectIdentifier.buildWithValue(((String) componentJobHis[2])));
            componentJob.setDestinationCassetteID(ObjectIdentifier.buildWithValue(((String) componentJobHis[3])));
            componentJob.setOriginalPortID(ObjectIdentifier.buildWithValue(((String) componentJobHis[4])));
            componentJob.setDestinationPortID(ObjectIdentifier.buildWithValue(((String) componentJobHis[5])));
            componentJob.setActionCode(((String) componentJobHis[6]));
            componentJob.setOperation(((String) componentJobHis[7]));

            List<Info.WaferSorterSlotMap> slotMapList = new ArrayList<>();
            componentJob.setWaferList(slotMapList);
            List<Info.SlotMapHistory> slotMapHis = slotMapHistories.stream()
                    .filter(data -> CimStringUtils.equals(componentJobHisID, data.getComponentJobHisID()))
                    .collect(Collectors.toList());
            if (CimArrayUtils.isNotEmpty(slotMapHis)) {
                for (Info.SlotMapHistory slotMapHi : slotMapHis) {
                    Info.WaferSorterSlotMap slotMap = new Info.WaferSorterSlotMap();
                    slotMap.setLotID(ObjectIdentifier.buildWithValue(slotMapHi.getLotID()));
                    slotMap.setWaferID(ObjectIdentifier.buildWithValue(slotMapHi.getWaferID()));
                    slotMap.setDestinationSlotNumber(slotMapHi.getDestPosition());
                    slotMap.setOriginalSlotNumber(slotMapHi.getSrcPosition());
                    slotMap.setAliasName(slotMapHi.getAliasName());
                    slotMapList.add(slotMap);
                }
            }
        }
        return sortJobInfo;
    }

    @Override
    public List<ObjectIdentifier> getLotIDByEquipment(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        Validations.check(ObjectIdentifier.isEmpty(equipmentID), retCodeConfig.getNotFoundEqp());
        String sql = "SELECT DISTINCT\n" +
                "\tsm.LOT_ID \n" +
                "FROM\n" +
                "\tOHSORTJOB os,\n" +
                "\tOHSORTJOB_COMP_SLOTMAP sm \n" +
                "WHERE\n" +
                "\tos.ID = sm.REFKEY \n" +
                "\tAND os.EQP_ID = ?1";
        List<Object[]> objects = cimJpaRepository.query(sql, ObjectIdentifier.fetchValue(equipmentID));
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(objects)) {
            for (Object[] object : objects) {
                lotIDs.add(ObjectIdentifier.buildWithValue(((String) object[0])));
            }
        }
        return lotIDs;
    }

    @Override
    public List<ObjectIdentifier> getCarrierIDByEquipment(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        String sql = "SELECT\n" +
                "\toc.DEST_CARRIER_ID \n" +
                "FROM\n" +
                "\tOHSORTJOB os,\n" +
                "\tOHSORTJOB_COMP oc \n" +
                "WHERE\n" +
                "\tos.ID = oc.REFKEY \n" +
                "\tAND os.EQP_ID = ?1 UNION\n" +
                "SELECT\n" +
                "\toc.SRC_CARRIER_ID \n" +
                "FROM\n" +
                "\tOHSORTJOB os,\n" +
                "\tOHSORTJOB_COMP oc \n" +
                "WHERE\n" +
                "\tos.ID = oc.REFKEY \n" +
                "\tAND os.EQP_ID = ?2";
        List<Object[]> objects = cimJpaRepository.query(sql, ObjectIdentifier.fetchValue(equipmentID), ObjectIdentifier.fetchValue(equipmentID));
        List<ObjectIdentifier> carrierIDs = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(objects)) {
            for (Object[] object : objects) {
                carrierIDs.add(ObjectIdentifier.buildWithValue(((String) object[0])));
            }
        }
        return carrierIDs;
    }

    @Override
    public void updatePostActAndSoltMapLot(ObjectIdentifier sortJobID, ObjectIdentifier childLotID) {
        Validations.check(ObjectIdentifier.isEmpty(sortJobID) || ObjectIdentifier.isEmpty(childLotID),
                retCodeConfigEx.getNotFoundSorterjob());
        CimSorterJob sorterJob = baseCoreFactory.getBO(CimSorterJob.class, sortJobID);
        sorterJob.setPostActChildLotForSortJobID(ObjectIdentifier.fetchValue(childLotID));
        sorterJob.setSlotMapLotForSortJobID(ObjectIdentifier.fetchValue(childLotID));
    }

    @Override
    public void cassetteCheckConditionForExchange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.WaferTransfer> waferXferList) {
        log.debug("cassetteCheckConditionForExchange(): enter in");
        //Collect cassette ID of input parameter
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        collectCassetteIDsOfParams(waferXferList, cassetteIDList);
        //Collect cassette's eqp ID / cassette ObjRef
        List<com.fa.cim.newcore.bo.durable.CimCassette> cassetteList = new ArrayList<>();
        List<String> strLoadedEquipments = new ArrayList<>();
        if (!CimArrayUtils.isEmpty(cassetteIDList)) {
            for (ObjectIdentifier cassetteID : cassetteIDList) {
                com.fa.cim.newcore.bo.durable.CimCassette cassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
                Validations.check(null == cassette, retCodeConfig.getNotFoundCassette());
                String tmpCassetteTransportState = cassette.getTransportState();
                Machine tmpEquipment = cassette.currentAssignedMachine();
                cassetteList.add(cassette);
                if (tmpEquipment != null && CimStringUtils.equals(tmpCassetteTransportState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    log.debug("cassetteCheckConditionForExchange(): tmpMachine is not nil");
                    strLoadedEquipments.add(tmpEquipment.getIdentifier());
                }
            }
        }

        //Check all cassette is on the same eqp
        String tmpLoadedEquipmentID = BizConstant.EMPTY;
        if (!CimArrayUtils.isEmpty(strLoadedEquipments)) {
            tmpLoadedEquipmentID = strLoadedEquipments.get(0);
            for (int i = 1; i < strLoadedEquipments.size(); i++) {
                if (!CimStringUtils.equals(tmpLoadedEquipmentID, strLoadedEquipments.get(i))) {
                    throw new ServiceException(retCodeConfig.getEquipmentOfCassetteNotSame());
                }
            }
        }
        //Check Carriers are loaded to eqp or not
        if (CimStringUtils.isEmpty(equipmentID.getValue()) && CimStringUtils.isEmpty(tmpLoadedEquipmentID)) {
            throw new ServiceException(retCodeConfig.getCassetteNotOnEquipment());
        }
        //Check eqp of cassette and input eqp is same or not
        Validations.check(!CimStringUtils.equals(equipmentID.getValue(), tmpLoadedEquipmentID), retCodeConfig.getCassetteEquipmentDifferent());
        //Get object reference of PosMachine
        CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == equipment, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
        //Get eqp attributes
        Boolean cassetteExchangeType = equipment.isCassetteChangeRequired();
        //Check eqp type
        Validations.check(CimBooleanUtils.isFalse(cassetteExchangeType), retCodeConfig.getInvalidMachineId());
        //Check cassette control jobs
        List<ObjectIdentifier> cassetteControlJobIDs = new ArrayList<>();
        cassetteList.forEach(cassette -> cassetteControlJobIDs.add(cassette.getControlJobID()));
        for (int i = 1; i < cassetteList.size(); i++) {
            Validations.check(!ObjectIdentifier.equalsWithValue(cassetteControlJobIDs.get(0), cassetteControlJobIDs.get(i)), retCodeConfig.getNotSameControlJobId());
        }
    }

    @Override
    public void updateComponentJob(Infos.ObjCommon objCommon, Info.ComponentJob componentJob) {
        CimSorterJob sorterJobBO = sorterJobManager.getCimSortJobByComponentJobID(componentJob.getComponentJobID());
        SorterJobDTO.SorterjobComponent component = new SorterJobDTO.SorterjobComponent();
        SorterJobDTO.SortJobInfo sortJobInfo = sorterJobBO.getSortJobInfo();
        List<SorterJobDTO.SorterjobComponent> sorterjobComponents = sortJobInfo.getSorterjobComponents();
        SorterJobDTO.SorterjobComponent componentJobInfo = Optional.ofNullable(sortJobInfo.getSorterjobComponents())
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(comJob -> ObjectIdentifier.equalsWithValue(componentJob.getComponentJobID(), comJob.getComponentJobID()))
                .findFirst()
                .get();

        List<SorterJobDTO.SlotMapInfo> slotMapInfos = new ArrayList<>();
        Optional.ofNullable(componentJobInfo.getSlotMapInfoList()).ifPresent(waferInfos -> waferInfos.forEach(waferInfo -> {
            Info.WaferSorterSlotMap slotMap = Optional.ofNullable(componentJob.getWaferList())
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .filter(comp -> comp.getDestinationSlotNumber().equals(waferInfo.getDestPosition()))
                    .findFirst()
                    .get();

            SorterJobDTO.SlotMapInfo slotMapInfo = new SorterJobDTO.SlotMapInfo();
            slotMapInfo.setDestPosition(slotMap.getDestinationSlotNumber());
          //  slotMapInfo.setWaferID(slotMap.getWaferID());
            slotMapInfo.setAliasName(slotMap.getAliasName());
            slotMapInfo.setDirection(SorterType.JobDirection.EAP.getValue());
            slotMapInfos.add(slotMapInfo);
        }));
        sorterJobBO.setSlotMapInfoForComponentJobID(componentJob.getComponentJobID(), slotMapInfos);
    }

    public static void collectCassetteIDsOfParams(List<Infos.WaferTransfer> waferXferList, List<ObjectIdentifier> cassetteIDList) {
        if (!CimArrayUtils.isEmpty(waferXferList)) {
            waferXferList.forEach(waferTransfer -> {
                boolean destCassetteAdded = false;
                boolean origCassetteAdded = false;
                for (ObjectIdentifier cassetteID : cassetteIDList) {
                    if (CimStringUtils.equals(waferTransfer.getDestinationCassetteID().getValue(), cassetteID.getValue())) {
                        destCassetteAdded = true;
                    }
                    if (CimStringUtils.equals(waferTransfer.getOriginalCassetteID().getValue(), cassetteID.getValue())) {
                        origCassetteAdded = true;
                    }
                    if (destCassetteAdded && origCassetteAdded) {
                        break;
                    }
                }
                if (!destCassetteAdded) {
                    cassetteIDList.add(waferTransfer.getDestinationCassetteID());
                }
                if (!origCassetteAdded) {
                    cassetteIDList.add(waferTransfer.getOriginalCassetteID());
                }
            });
        }
    }

    @Override
    public ObjectIdentifier getEquipmentIDBySortJobID(Infos.ObjCommon objCommon, ObjectIdentifier sortJobID) {
        CimSorterJob cimSortJob = baseCoreFactory.getBOByIdentifier(CimSorterJob.class, ObjectIdentifier.fetchValue(sortJobID));
        Validations.check(null == cimSortJob, retCodeConfigEx.getNotFoundSorterjob());
        return ObjectIdentifier.buildWithValue(cimSortJob.getEqpID());
    }
}
