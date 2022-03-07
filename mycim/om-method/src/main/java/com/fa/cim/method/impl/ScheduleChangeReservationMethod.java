package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.nonruntime.CimScheduleChangeReservationDO;
import com.fa.cim.entity.runtime.po.CimProcessOperationDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.method.IScheduleChangeReservationMethod;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlow;
import com.fa.cim.newcore.bo.pd.CimProcessFlowContext;
import com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.prcssdfn.ProcessDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * description:
 * ScheduleChangeReservationCompImpl .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/8/10        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/8/10 10:38
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class ScheduleChangeReservationMethod implements IScheduleChangeReservationMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    
    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Override
    public List<Infos.SchdlChangeReservation> schdlChangeReservationGetListDR(Infos.ObjCommon objCommon, Inputs.ObjScheduleChangeReservationGetListIn inParams) {
        CimScheduleChangeReservationDO example = new CimScheduleChangeReservationDO();
        example.setEventId(CimStringUtils.getValueOrNullString(inParams.getEventID()));
        example.setObjectID(CimStringUtils.getValueOrNullString(inParams.getObjectID()));
        example.setObjectType(CimStringUtils.getValueOrNullString(inParams.getObjectType()));
        example.setTargetMainProcessDefinitionId(CimStringUtils.getValueOrNullString(inParams.getTargetRouteID()));
        example.setTargetOperationNumber(CimStringUtils.getValueOrNullString(inParams.getTargetOperationNumber()));
        example.setProcessDefinitionSpecId(CimStringUtils.getValueOrNullString(inParams.getProductID()));
        example.setMainProcessDefinitionId(CimStringUtils.getValueOrNullString(inParams.getRouteID()));
        example.setOperationNumber(CimStringUtils.getValueOrNullString(inParams.getOperationNumber()));
        example.setSubLotType(CimStringUtils.getValueOrNullString(inParams.getSubLotType()));
        example.setStartDate(CimStringUtils.getValueOrNullString(inParams.getStartDate()));
        example.setEndDate(CimStringUtils.getValueOrNullString(inParams.getEndDate()));
        example.setStatus(CimStringUtils.getValueOrNullString(inParams.getStatus()));
        example.setLotInfoChangeFlag(CimBooleanUtils.isTrue(inParams.getLotInfoChangeFlag()));

        return cimJpaRepository.findAll(Example.of(example)).stream()
                .map(data -> {
                    Infos.SchdlChangeReservation info = new Infos.SchdlChangeReservation();
                    info.setEventID(data.getEventId());
                    info.setObjectID(ObjectIdentifier.buildWithValue(data.getObjectID()));
                    info.setObjectType(data.getObjectType());
                    info.setTargetRouteID(ObjectIdentifier.buildWithValue(data.getTargetMainProcessDefinitionId()));
                    info.setTargetOperationNumber(data.getTargetOperationNumber());
                    info.setProductID(ObjectIdentifier.buildWithValue(data.getProcessDefinitionSpecId()));
                    info.setRouteID(ObjectIdentifier.buildWithValue(data.getMainProcessDefinitionId()));
                    info.setOperationNumber(data.getOperationNumber());
                    info.setSubLotType(data.getSubLotType());
                    info.setStartDate(data.getStartDate());
                    info.setStartTime(data.getStartTime());
                    info.setEndDate(data.getEndDate());
                    info.setEndTime(data.getEndTime());
                    info.setEraseAfterUsedFlag(data.getAutoEraseFlag());
                    info.setMaxLotCnt(CimNumberUtils.longValue(data.getMaxLotCnt()));
                    info.setApplyLotCnt(CimNumberUtils.longValue(data.getAppltLotCount()));
                    info.setStatus(data.getStatus());
                    info.setActionCode(data.getActionCode());
                    info.setLotInfoChangeFlag(data.getLotInfoChangeFlag());
                    return info;
                }).collect(Collectors.toList());
    }


    @Override
    public Outputs.ObjSchdlChangeReservationCheckForActionDROut schdlChangeReservationCheckForActionDR(Infos.ObjCommon objCommon, Inputs.ObjSchdlChangeReservationCheckForActionDRIn objSchdlChangeReservationCheckForActionDRIn) {
        Outputs.ObjSchdlChangeReservationCheckForActionDROut objSchdlChangeReservationCheckForActionDROut =
                new Outputs.ObjSchdlChangeReservationCheckForActionDROut();
        Infos.SchdlChangeReservation strSchdlChangeReservation = new Infos.SchdlChangeReservation();
        objSchdlChangeReservationCheckForActionDROut.setStrSchdlChangeReservation(strSchdlChangeReservation);

        ObjectIdentifier lotID = objSchdlChangeReservationCheckForActionDRIn.getLotID();
        String routeID = objSchdlChangeReservationCheckForActionDRIn.getRouteID();
        String operationNumber = objSchdlChangeReservationCheckForActionDRIn.getOperationNumber();

        String currentDate = null;
        String currentTime = null;
        StringBuffer hFRLOTLOTID = new StringBuffer();

        hFRLOTLOTID.append(lotID.getValue());
        objSchdlChangeReservationCheckForActionDROut.setExistFlag(false);
        long strLen = objCommon.getTimeStamp().getReportTimeStamp().getTime();
        String aCurrentTimeStamp = String.valueOf(objCommon.getTimeStamp().getReportTimeStamp().toString());
        currentDate = aCurrentTimeStamp.substring(0, 10);
        currentTime = aCurrentTimeStamp.substring(10, aCurrentTimeStamp.length());
        currentTime = currentTime.substring(0,currentTime.indexOf("."));
        String getWildCard = StandardProperties.OM_CONSTRAINT_WILDCARD_VALUE.getValue();

        CimLot lot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(null == lot, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(lotID)));

        com.fa.cim.newcore.bo.prodspec.CimProductSpecification productSpecification = lot.getProductSpecification();
        Validations.check(null == productSpecification, retCodeConfig.getNotFoundProductSpec());
        String prodSpecID = ObjectIdentifier.fetchValue(productSpecification.getProductSpecID());
        String mainPD = null;
        String modulePDID = null;
        if (CimStringUtils.isNotEmpty(routeID) && CimStringUtils.isNotEmpty(operationNumber)) {
            long checkCnt = 0;
            while (CimStringUtils.isEmpty(modulePDID)) {
                checkCnt++;
                com.fa.cim.newcore.bo.pd.CimProcessOperation processOperation = null;
                if (checkCnt == 1) {
                    processOperation = lot.getProcessOperation();
                } else if (checkCnt == 2) {
                    processOperation = lot.getPreviousProcessOperation();
                } else {
                    break;
                }
                if (processOperation == null){
                    break;
                }
                com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainPD = processOperation.getMainProcessDefinition(); // OMPRP
                Validations.check(null == aMainPD, retCodeConfig.getNotFoundMainRoute());
                mainPD = aMainPD.getIdentifier(); // PRP_ID
                String opeNumber = processOperation.getOperationNumber();
                String modNumberIn = BaseStaticMethod.convertOpeNoToModuleNo(operationNumber);
                String modNumberPO = BaseStaticMethod.convertOpeNoToModuleNo(opeNumber);
                if (CimStringUtils.equals(routeID, mainPD) && CimStringUtils.equals(modNumberIn, modNumberPO)) {
                    com.fa.cim.newcore.bo.pd.CimProcessDefinition aModuleProcessDefinition = processOperation.getModuleProcessDefinition();
                    Validations.check(null == aModuleProcessDefinition, new OmCode(retCodeConfig.getNotFoundModule(), "*****"));
                    modulePDID = aModuleProcessDefinition.getIdentifier();
                }
            }
            if (modulePDID == null) {
                com.fa.cim.newcore.bo.pd.CimProcessFlowContext processFlowContext = lot.getProcessFlowContext();
                if(!CimObjectUtils.isEmpty(processFlowContext)){
                    List<ProcessDTO.ReturnOperation> aReturnOperationSequence = processFlowContext.allReturnOperations();
                    if (CimArrayUtils.isNotEmpty(aReturnOperationSequence)) {
                        mainPD = routeID;
                        ObjectIdentifier searchRouteID = new ObjectIdentifier(routeID);

                        Inputs.ObjProcessOperationProcessRefListForLotIn refListForLotIn = new Inputs.ObjProcessOperationProcessRefListForLotIn();
                        refListForLotIn.setSearchDirection(true);
                        refListForLotIn.setPosSearchFlag(true);
                        refListForLotIn.setSearchCount(Integer.parseInt(BizConstant.SP_SEARCH_COUNT_MAX));
                        refListForLotIn.setCurrentFlag(true);
                        refListForLotIn.setSearchRouteID(searchRouteID);
                        refListForLotIn.setLotID(lotID);
                        refListForLotIn.setSearchOperationNumber(operationNumber);

                        //Step1 - process_OperationProcessRefListForLot
                        List<Infos.OperationProcessRefListAttributes> operationProcessRefListAttributesList = processMethod.processOperationProcessRefListForLot(objCommon, refListForLotIn);
                        if(CimArrayUtils.isNotEmpty(operationProcessRefListAttributesList)){
                            int i = operationProcessRefListAttributesList.size();
                            if(ObjectIdentifier.equalsWithValue(operationProcessRefListAttributesList.get(i - 1).getRouteID(), searchRouteID)
                            && CimStringUtils.equals(operationProcessRefListAttributesList.get(i-1).getOperationNumber(), operationNumber)){
                                CimProcessFlow processFlow = baseCoreFactory.getBO(CimProcessFlow.class, operationProcessRefListAttributesList.get(i-1).getProcessRef().getModuleProcessFlow());
                                if(CimBooleanUtils.isFalse(processFlow == null)){
                                    ProcessDefinition aModuleProcessDefinition = processFlow.getRootProcessDefinition();
                                    Validations.check(aModuleProcessDefinition == null, retCodeConfig.getNotFoundModule());
                                    modulePDID = aModuleProcessDefinition.getIdentifier();
                                }
                            }
                        }
                    }
                }
            }
        }

        if(modulePDID == null){
            ObjectIdentifier aRouteID = new ObjectIdentifier(routeID);
            CimProcessDefinition aMainProcessDefinition = baseCoreFactory.getBO(CimProcessDefinition.class, aRouteID);

            Validations.check(aMainProcessDefinition == null, new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ObjectIdentifier.fetchValue(aRouteID)));
            mainPD = aMainProcessDefinition.getIdentifier();
            com.fa.cim.newcore.bo.pd.CimProcessFlow aMainProcessFlow = aMainProcessDefinition.getActiveMainProcessFlow();
            Validations.check(aMainProcessFlow == null, retCodeConfig.getNotFoundProcessFlowForProcessDefinition(),aMainProcessDefinition.getIdentifier(),aMainProcessDefinition.getProcessDefinitionLevel());
            String moduleNumber = BaseStaticMethod.convertOpeNoToModuleNo(operationNumber);
            AtomicReference<CimProcessFlow> aProcessFlow = new AtomicReference<>();
            com.fa.cim.newcore.bo.pd.CimProcessDefinition aModuleProcessDefinition = aMainProcessFlow.findProcessDefinitionFor(moduleNumber, aProcessFlow);
            if(aModuleProcessDefinition != null) {
                modulePDID = aModuleProcessDefinition.getIdentifier();
            }
        }
        String sql =
                "SELECT     COUNT(LOT_PLAN_CHG_ID) " +
                        "FROM OSLOTPLANCHGRSV " +
                        "WHERE (TO_DATE(START_DATE,'yyyy-MM-dd') < TO_DATE(?1,'yyyy-MM-dd') " +
                        "OR (START_DATE = ?1 " +
                        "   AND TO_DATE(START_TIME,'HH24-mi-ss') <= TO_DATE(?2,'HH24-mi-ss'))) " +
                        "AND (TO_DATE(END_DATE,'yyyy-MM-dd') > TO_DATE(?1,'yyyy-MM-dd') " +
                        "OR (END_DATE = ?1 " +
                        "   AND TO_DATE(END_TIME,'HH24-mi-ss') >= TO_DATE(?2,'HH24-mi-ss'))) " +
                        "AND MAX_CNT_FOR_APPLY > CUR_CNT_FOR_APPLY " +
                        "AND TARGET_PROCESS_ID = ?3 " +
                        "AND TARGET_OPE_NO = ?4 ";
        List<Object> countEVENTID = cimJpaRepository.queryOneColumn(sql, currentDate,currentTime, mainPD, operationNumber);

        if(CimNumberUtils.longValue((Number) countEVENTID.get(0)) == 0){
            //result.setReturnCode(retCodeConfig.getSucc());
            return objSchdlChangeReservationCheckForActionDROut;
        }

        //Get And Check OBJECT_ID For Lot_ID
        sql = "     SELECT   * " +
                "   FROM   OSLOTPLANCHGRSV " +
                "   WHERE  TARGET_OBJECT_TYPE = ?1 " +
                "   AND (TO_DATE(START_DATE,'yyyy-MM-dd') < TO_DATE(?2,'yyyy-MM-dd') " +
                "   OR (START_DATE = ?2 AND TO_DATE(START_TIME,'HH24-mi-ss') <= TO_DATE(?3,'HH24-mi-ss'))) " +
                "   AND (TO_DATE(END_DATE,'yyyy-MM-dd') > TO_DATE(?2,'yyyy-MM-dd') " +
                "       OR (END_DATE = ?2 AND TO_DATE(END_TIME,'HH24-mi-ss') >= TO_DATE(?3,'HH24-mi-ss'))) " +
                "   AND MAX_CNT_FOR_APPLY > CUR_CNT_FOR_APPLY " +
                "   AND TARGET_PROCESS_ID = ?4 " +
                "   AND TARGET_OPE_NO = ?5 " +
                "   ORDER BY LENGTH(TARGET_OBJECT_ID) DESC";

        List<CimScheduleChangeReservationDO> scheduleChangeReservationList = cimJpaRepository.query(sql, CimScheduleChangeReservationDO.class
                , BizConstant.SP_SCHDL_CHG_OBJTYPE_LOT, currentDate, currentTime, mainPD, operationNumber);
        if (CimArrayUtils.isNotEmpty(scheduleChangeReservationList)) {
            for (CimScheduleChangeReservationDO scheduleChangeReservation : scheduleChangeReservationList) {
                char[] chars = getWildCard.toCharArray();
                String objectID = scheduleChangeReservation.getObjectID();
                if(objectID.indexOf(chars[0]) == -1){
                    if(CimStringUtils.equals(objectID, lotID.getValue())){
                        strSchdlChangeReservation.setEventID(scheduleChangeReservation.getEventId());
                        strSchdlChangeReservation.setObjectID(lotID);
                        strSchdlChangeReservation.setObjectType(scheduleChangeReservation.getObjectType());
                        strSchdlChangeReservation.setProductID(ObjectIdentifier.buildWithValue(scheduleChangeReservation.getProcessDefinitionSpecId()));
                        strSchdlChangeReservation.setRouteID(ObjectIdentifier.buildWithValue(scheduleChangeReservation.getMainProcessDefinitionId()));
                        strSchdlChangeReservation.setOperationNumber(scheduleChangeReservation.getOperationNumber());
                        strSchdlChangeReservation.setSubLotType(scheduleChangeReservation.getSubLotType());
                        strSchdlChangeReservation.setTargetRouteID(ObjectIdentifier.buildWithValue(scheduleChangeReservation.getTargetMainProcessDefinitionId()));
                        strSchdlChangeReservation.setTargetOperationNumber(scheduleChangeReservation.getTargetOperationNumber());
                        strSchdlChangeReservation.setStartDate(scheduleChangeReservation.getStartDate());
                        strSchdlChangeReservation.setStartTime(scheduleChangeReservation.getStartTime());
                        strSchdlChangeReservation.setEndDate(scheduleChangeReservation.getEndDate());
                        strSchdlChangeReservation.setEndTime(scheduleChangeReservation.getEndTime());
                        strSchdlChangeReservation.setEraseAfterUsedFlag(scheduleChangeReservation.getAutoEraseFlag());
                        strSchdlChangeReservation.setMaxLotCnt(scheduleChangeReservation.getMaxLotCnt().longValue());
                        strSchdlChangeReservation.setApplyLotCnt(scheduleChangeReservation.getAppltLotCount().longValue());
                        strSchdlChangeReservation.setStatus(scheduleChangeReservation.getStatus());
                        strSchdlChangeReservation.setActionCode(scheduleChangeReservation.getActionCode());
                        strSchdlChangeReservation.setLotInfoChangeFlag(scheduleChangeReservation.getLotInfoChangeFlag());
                        objSchdlChangeReservationCheckForActionDROut.setExistFlag(true);

                        //result.setReturnCode(retCodeConfig.getSucc());
                        return objSchdlChangeReservationCheckForActionDROut;
                    }
                } else {
                    //Check LotID For WildCard String
                    long nLen = objectID.length();
                    StringBuilder TmpParam = new StringBuilder();
                    String TempParameter;
                    for (int iCnt = 0; iCnt < nLen; iCnt++) {
                        TempParameter = objectID.substring(0, 1);
                        if(CimStringUtils.equals(TempParameter, getWildCard)){
                            break;
                        } else {
                            TmpParam.append(TempParameter);
                        }
                    }
                    if(TmpParam.length() > lotID.getValue().length()){
                        continue;
                    }
                    int wLen = TmpParam.length();
                    String tmpLotID = lotID.getValue().substring(0, wLen);
                    if(CimStringUtils.equals(TmpParam.toString(), tmpLotID)){
                        strSchdlChangeReservation.setEventID(scheduleChangeReservation.getEventId());
                        strSchdlChangeReservation.setObjectID(ObjectIdentifier.buildWithValue(scheduleChangeReservation.getObjectID()));
                        strSchdlChangeReservation.setObjectType(scheduleChangeReservation.getObjectType());
                        strSchdlChangeReservation.setProductID(ObjectIdentifier.buildWithValue(scheduleChangeReservation.getProcessDefinitionSpecId()));
                        strSchdlChangeReservation.setRouteID(ObjectIdentifier.buildWithValue(scheduleChangeReservation.getMainProcessDefinitionId()));
                        strSchdlChangeReservation.setOperationNumber(scheduleChangeReservation.getOperationNumber());
                        strSchdlChangeReservation.setSubLotType(scheduleChangeReservation.getSubLotType());
                        strSchdlChangeReservation.setTargetRouteID(ObjectIdentifier.buildWithValue(scheduleChangeReservation.getTargetMainProcessDefinitionId()));
                        strSchdlChangeReservation.setTargetOperationNumber(scheduleChangeReservation.getTargetOperationNumber());
                        strSchdlChangeReservation.setStartDate(scheduleChangeReservation.getStartDate());
                        strSchdlChangeReservation.setStartTime(scheduleChangeReservation.getStartTime());
                        strSchdlChangeReservation.setEndDate(scheduleChangeReservation.getEndDate());
                        strSchdlChangeReservation.setEndTime(scheduleChangeReservation.getEndTime());
                        strSchdlChangeReservation.setEraseAfterUsedFlag(scheduleChangeReservation.getAutoEraseFlag());
                        strSchdlChangeReservation.setMaxLotCnt(scheduleChangeReservation.getMaxLotCnt().longValue());
                        strSchdlChangeReservation.setApplyLotCnt(scheduleChangeReservation.getAppltLotCount().longValue());
                        strSchdlChangeReservation.setStatus(scheduleChangeReservation.getStatus());
                        strSchdlChangeReservation.setActionCode(scheduleChangeReservation.getActionCode());
                        strSchdlChangeReservation.setLotInfoChangeFlag(scheduleChangeReservation.getLotInfoChangeFlag());
                        objSchdlChangeReservationCheckForActionDROut.setExistFlag(true);

                        //result.setReturnCode(retCodeConfig.getSucc());
                        return objSchdlChangeReservationCheckForActionDROut;
                    }
                }
            }
        }
        //product
        scheduleChangeReservationList = cimJpaRepository.query(sql, CimScheduleChangeReservationDO.class
                , BizConstant.SP_SCHDL_CHG_OBJTYPE_PRD, currentDate, currentTime, mainPD, operationNumber);
        if(CimArrayUtils.isNotEmpty(scheduleChangeReservationList)){
            for (CimScheduleChangeReservationDO scheduleChangeReservation : scheduleChangeReservationList) {
                char[] chars = getWildCard.toCharArray();
                String objectID = scheduleChangeReservation.getObjectID();
                if(objectID.indexOf(chars[0]) == -1){
                    if(CimStringUtils.equals(objectID, prodSpecID)){
                        strSchdlChangeReservation.setEventID(scheduleChangeReservation.getEventId());
                        strSchdlChangeReservation.setObjectID(new ObjectIdentifier(scheduleChangeReservation.getObjectID(), ""));
                        strSchdlChangeReservation.setObjectType(scheduleChangeReservation.getObjectType());
                        strSchdlChangeReservation.setProductID(new ObjectIdentifier(scheduleChangeReservation.getProcessDefinitionSpecId(),""));
                        strSchdlChangeReservation.setRouteID(new ObjectIdentifier(scheduleChangeReservation.getMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setOperationNumber(scheduleChangeReservation.getOperationNumber());
                        strSchdlChangeReservation.setSubLotType(scheduleChangeReservation.getSubLotType());
                        strSchdlChangeReservation.setTargetRouteID(new ObjectIdentifier(scheduleChangeReservation.getTargetMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setTargetOperationNumber(scheduleChangeReservation.getTargetOperationNumber());
                        strSchdlChangeReservation.setStartDate(scheduleChangeReservation.getStartDate());
                        strSchdlChangeReservation.setStartTime(scheduleChangeReservation.getStartTime());
                        strSchdlChangeReservation.setEndDate(scheduleChangeReservation.getEndDate());
                        strSchdlChangeReservation.setEndTime(scheduleChangeReservation.getEndTime());
                        strSchdlChangeReservation.setEraseAfterUsedFlag(scheduleChangeReservation.getAutoEraseFlag());
                        strSchdlChangeReservation.setMaxLotCnt(scheduleChangeReservation.getMaxLotCnt().longValue());
                        strSchdlChangeReservation.setApplyLotCnt(scheduleChangeReservation.getAppltLotCount().longValue());
                        strSchdlChangeReservation.setStatus(scheduleChangeReservation.getStatus());
                        strSchdlChangeReservation.setActionCode(scheduleChangeReservation.getActionCode());
                        strSchdlChangeReservation.setLotInfoChangeFlag(scheduleChangeReservation.getLotInfoChangeFlag());
                        objSchdlChangeReservationCheckForActionDROut.setExistFlag(true);

                        //result.setReturnCode(retCodeConfig.getSucc());
                        return objSchdlChangeReservationCheckForActionDROut;
                    }
                } else {
                    //Check Product_ID For WildCard String
                    long nLen = objectID.length();
                    String ptmpDataValue = objectID;
                    StringBuilder TmpParam = new StringBuilder("");
                    String TempParameter;
                    for (int iCnt = 0; iCnt < nLen; iCnt++) {
                        TempParameter = ptmpDataValue.substring(0, 1);
                        if(CimStringUtils.equals(TempParameter, getWildCard)){
                            break;
                        } else {
                            TmpParam.append(TempParameter);
                        }
                    }
                    if(TmpParam.length() > prodSpecID.length()){
                        continue;
                    }
                    int wLen = TmpParam.length();
                    String tmpProdSpecID = prodSpecID.substring(0, wLen);
                    if(CimStringUtils.equals(TmpParam.toString(), tmpProdSpecID)){
                        strSchdlChangeReservation.setEventID(scheduleChangeReservation.getEventId());
                        strSchdlChangeReservation.setObjectID(new ObjectIdentifier(scheduleChangeReservation.getObjectID(), ""));
                        strSchdlChangeReservation.setObjectType(scheduleChangeReservation.getObjectType());
                        strSchdlChangeReservation.setProductID(new ObjectIdentifier(scheduleChangeReservation.getProcessDefinitionSpecId(),""));
                        strSchdlChangeReservation.setRouteID(new ObjectIdentifier(scheduleChangeReservation.getMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setOperationNumber(scheduleChangeReservation.getOperationNumber());
                        strSchdlChangeReservation.setSubLotType(scheduleChangeReservation.getSubLotType());
                        strSchdlChangeReservation.setTargetRouteID(new ObjectIdentifier(scheduleChangeReservation.getTargetMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setTargetOperationNumber(scheduleChangeReservation.getTargetOperationNumber());
                        strSchdlChangeReservation.setStartDate(scheduleChangeReservation.getStartDate());
                        strSchdlChangeReservation.setStartTime(scheduleChangeReservation.getStartTime());
                        strSchdlChangeReservation.setEndDate(scheduleChangeReservation.getEndDate());
                        strSchdlChangeReservation.setEndTime(scheduleChangeReservation.getEndTime());
                        strSchdlChangeReservation.setEraseAfterUsedFlag(scheduleChangeReservation.getAutoEraseFlag());
                        strSchdlChangeReservation.setMaxLotCnt(scheduleChangeReservation.getMaxLotCnt().longValue());
                        strSchdlChangeReservation.setApplyLotCnt(scheduleChangeReservation.getAppltLotCount().longValue());
                        strSchdlChangeReservation.setStatus(scheduleChangeReservation.getStatus());
                        strSchdlChangeReservation.setActionCode(scheduleChangeReservation.getActionCode());
                        strSchdlChangeReservation.setLotInfoChangeFlag(scheduleChangeReservation.getLotInfoChangeFlag());
                        objSchdlChangeReservationCheckForActionDROut.setExistFlag(true);

                        //result.setReturnCode(retCodeConfig.getSucc());
                        return objSchdlChangeReservationCheckForActionDROut;
                    }
                }
            }
        }
        // mainPD
        scheduleChangeReservationList = cimJpaRepository.query(sql, CimScheduleChangeReservationDO.class
                , BizConstant.SP_SCHDL_CHG_OBJTYPE_MPD, currentDate, currentTime, mainPD, operationNumber);
        if(CimArrayUtils.isNotEmpty(scheduleChangeReservationList)){
            for (CimScheduleChangeReservationDO scheduleChangeReservation : scheduleChangeReservationList) {
                char[] chars = getWildCard.toCharArray();
                String objectID = scheduleChangeReservation.getObjectID();
                if(objectID.indexOf(chars[0]) == -1){
                    if(CimStringUtils.equals(objectID, mainPD)){
                        strSchdlChangeReservation.setEventID(scheduleChangeReservation.getEventId());
                        strSchdlChangeReservation.setObjectID(new ObjectIdentifier(scheduleChangeReservation.getObjectID(), ""));
                        strSchdlChangeReservation.setObjectType(scheduleChangeReservation.getObjectType());
                        strSchdlChangeReservation.setProductID(new ObjectIdentifier(scheduleChangeReservation.getProcessDefinitionSpecId(),""));
                        strSchdlChangeReservation.setRouteID(new ObjectIdentifier(scheduleChangeReservation.getMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setOperationNumber(scheduleChangeReservation.getOperationNumber());
                        strSchdlChangeReservation.setSubLotType(scheduleChangeReservation.getSubLotType());
                        strSchdlChangeReservation.setTargetRouteID(new ObjectIdentifier(scheduleChangeReservation.getTargetMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setTargetOperationNumber(scheduleChangeReservation.getTargetOperationNumber());
                        strSchdlChangeReservation.setStartDate(scheduleChangeReservation.getStartDate());
                        strSchdlChangeReservation.setStartTime(scheduleChangeReservation.getStartTime());
                        strSchdlChangeReservation.setEndDate(scheduleChangeReservation.getEndDate());
                        strSchdlChangeReservation.setEndTime(scheduleChangeReservation.getEndTime());
                        strSchdlChangeReservation.setEraseAfterUsedFlag(scheduleChangeReservation.getAutoEraseFlag());
                        strSchdlChangeReservation.setMaxLotCnt(scheduleChangeReservation.getMaxLotCnt().longValue());
                        strSchdlChangeReservation.setApplyLotCnt(scheduleChangeReservation.getAppltLotCount().longValue());
                        strSchdlChangeReservation.setStatus(scheduleChangeReservation.getStatus());
                        strSchdlChangeReservation.setActionCode(scheduleChangeReservation.getActionCode());
                        strSchdlChangeReservation.setLotInfoChangeFlag(scheduleChangeReservation.getLotInfoChangeFlag());
                        objSchdlChangeReservationCheckForActionDROut.setExistFlag(true);

                        //result.setReturnCode(retCodeConfig.getSucc());
                        return objSchdlChangeReservationCheckForActionDROut;
                    }
                } else {
                    //Check Route_ID For WildCard String
                    long nLen = objectID.length();
                    String ptmpDataValue = objectID;
                    StringBuilder TmpParam = new StringBuilder("");
                    String TempParameter;
                    for (int iCnt = 0; iCnt < nLen; iCnt++) {
                        TempParameter = ptmpDataValue.substring(0, 1);
                        if(CimStringUtils.equals(TempParameter, getWildCard)){
                            break;
                        } else {
                            TmpParam.append(TempParameter);
                        }
                    }
                    if(TmpParam.length() > mainPD.length()){
                        continue;
                    }
                    int wLen = TmpParam.length();
                    String tmpMainID = mainPD.substring(0, wLen);
                    if(CimStringUtils.equals(TmpParam.toString(), tmpMainID)){
                        strSchdlChangeReservation.setEventID(scheduleChangeReservation.getEventId());
                        strSchdlChangeReservation.setObjectID(new ObjectIdentifier(scheduleChangeReservation.getObjectID(), ""));
                        strSchdlChangeReservation.setObjectType(scheduleChangeReservation.getObjectType());
                        strSchdlChangeReservation.setProductID(new ObjectIdentifier(scheduleChangeReservation.getProcessDefinitionSpecId(),""));
                        strSchdlChangeReservation.setRouteID(new ObjectIdentifier(scheduleChangeReservation.getMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setOperationNumber(scheduleChangeReservation.getOperationNumber());
                        strSchdlChangeReservation.setSubLotType(scheduleChangeReservation.getSubLotType());
                        strSchdlChangeReservation.setTargetRouteID(new ObjectIdentifier(scheduleChangeReservation.getTargetMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setTargetOperationNumber(scheduleChangeReservation.getTargetOperationNumber());
                        strSchdlChangeReservation.setStartDate(scheduleChangeReservation.getStartDate());
                        strSchdlChangeReservation.setStartTime(scheduleChangeReservation.getStartTime());
                        strSchdlChangeReservation.setEndDate(scheduleChangeReservation.getEndDate());
                        strSchdlChangeReservation.setEndTime(scheduleChangeReservation.getEndTime());
                        strSchdlChangeReservation.setEraseAfterUsedFlag(scheduleChangeReservation.getAutoEraseFlag());
                        strSchdlChangeReservation.setMaxLotCnt(scheduleChangeReservation.getMaxLotCnt().longValue());
                        strSchdlChangeReservation.setApplyLotCnt(scheduleChangeReservation.getAppltLotCount().longValue());
                        strSchdlChangeReservation.setStatus(scheduleChangeReservation.getStatus());
                        strSchdlChangeReservation.setActionCode(scheduleChangeReservation.getActionCode());
                        strSchdlChangeReservation.setLotInfoChangeFlag(scheduleChangeReservation.getLotInfoChangeFlag());
                        objSchdlChangeReservationCheckForActionDROut.setExistFlag(true);

                        //result.setReturnCode(retCodeConfig.getSucc());
                        return objSchdlChangeReservationCheckForActionDROut;
                    }
                }
            }
        }
        //Get And Check OBJECT_ID For Module_ID
        scheduleChangeReservationList = cimJpaRepository.query(sql, CimScheduleChangeReservationDO.class
                , BizConstant.SP_SCHDL_CHG_OBJTYPE_MDP, currentDate, currentTime, mainPD, operationNumber);
        if(CimArrayUtils.isNotEmpty(scheduleChangeReservationList)){
            for (CimScheduleChangeReservationDO scheduleChangeReservation : scheduleChangeReservationList) {
                char[] chars = getWildCard.toCharArray();
                String objectID = scheduleChangeReservation.getObjectID();
                if(objectID.indexOf(chars[0]) == -1){
                    if(CimStringUtils.equals(objectID, modulePDID)){
                        strSchdlChangeReservation.setEventID(scheduleChangeReservation.getEventId());
                        strSchdlChangeReservation.setObjectID(new ObjectIdentifier(scheduleChangeReservation.getObjectID(), ""));
                        strSchdlChangeReservation.setObjectType(scheduleChangeReservation.getObjectType());
                        strSchdlChangeReservation.setProductID(new ObjectIdentifier(scheduleChangeReservation.getProcessDefinitionSpecId(),""));
                        strSchdlChangeReservation.setRouteID(new ObjectIdentifier(scheduleChangeReservation.getMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setOperationNumber(scheduleChangeReservation.getOperationNumber());
                        strSchdlChangeReservation.setSubLotType(scheduleChangeReservation.getSubLotType());
                        strSchdlChangeReservation.setTargetRouteID(new ObjectIdentifier(scheduleChangeReservation.getTargetMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setTargetOperationNumber(scheduleChangeReservation.getTargetOperationNumber());
                        strSchdlChangeReservation.setStartDate(scheduleChangeReservation.getStartDate());
                        strSchdlChangeReservation.setStartTime(scheduleChangeReservation.getStartTime());
                        strSchdlChangeReservation.setEndDate(scheduleChangeReservation.getEndDate());
                        strSchdlChangeReservation.setEndTime(scheduleChangeReservation.getEndTime());
                        strSchdlChangeReservation.setEraseAfterUsedFlag(scheduleChangeReservation.getAutoEraseFlag());
                        strSchdlChangeReservation.setMaxLotCnt(scheduleChangeReservation.getMaxLotCnt().longValue());
                        strSchdlChangeReservation.setApplyLotCnt(scheduleChangeReservation.getAppltLotCount().longValue());
                        strSchdlChangeReservation.setStatus(scheduleChangeReservation.getStatus());
                        strSchdlChangeReservation.setActionCode(scheduleChangeReservation.getActionCode());
                        strSchdlChangeReservation.setLotInfoChangeFlag(scheduleChangeReservation.getLotInfoChangeFlag());
                        objSchdlChangeReservationCheckForActionDROut.setExistFlag(true);

                        //result.setReturnCode(retCodeConfig.getSucc());
                        return objSchdlChangeReservationCheckForActionDROut;
                    }
                } else {
                    //Check Module_ID For WildCard String
                    long nLen = objectID.length();
                    String ptmpDataValue = objectID;
                    StringBuilder TmpParam = new StringBuilder("");
                    String TempParameter;
                    for (int iCnt = 0; iCnt < nLen; iCnt++) {
                        TempParameter = ptmpDataValue.substring(0, 1);
                        if(CimStringUtils.equals(TempParameter, getWildCard)){
                            break;
                        } else {
                            TmpParam.append(TempParameter);
                        }
                    }
                    if(TmpParam.length() > modulePDID.length()){
                        continue;
                    }
                    int wLen = TmpParam.length();
                    String tmpModuleID = modulePDID.substring(0, wLen);
                    if(CimStringUtils.equals(TmpParam.toString(), tmpModuleID)){
                        strSchdlChangeReservation.setEventID(scheduleChangeReservation.getEventId());
                        strSchdlChangeReservation.setObjectID(new ObjectIdentifier(scheduleChangeReservation.getObjectID(), ""));
                        strSchdlChangeReservation.setObjectType(scheduleChangeReservation.getObjectType());
                        strSchdlChangeReservation.setProductID(new ObjectIdentifier(scheduleChangeReservation.getProcessDefinitionSpecId(),""));
                        strSchdlChangeReservation.setRouteID(new ObjectIdentifier(scheduleChangeReservation.getMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setOperationNumber(scheduleChangeReservation.getOperationNumber());
                        strSchdlChangeReservation.setSubLotType(scheduleChangeReservation.getSubLotType());
                        strSchdlChangeReservation.setTargetRouteID(new ObjectIdentifier(scheduleChangeReservation.getTargetMainProcessDefinitionId(), ""));
                        strSchdlChangeReservation.setTargetOperationNumber(scheduleChangeReservation.getTargetOperationNumber());
                        strSchdlChangeReservation.setStartDate(scheduleChangeReservation.getStartDate());
                        strSchdlChangeReservation.setStartTime(scheduleChangeReservation.getStartTime());
                        strSchdlChangeReservation.setEndDate(scheduleChangeReservation.getEndDate());
                        strSchdlChangeReservation.setEndTime(scheduleChangeReservation.getEndTime());
                        strSchdlChangeReservation.setEraseAfterUsedFlag(scheduleChangeReservation.getAutoEraseFlag());
                        strSchdlChangeReservation.setMaxLotCnt(scheduleChangeReservation.getMaxLotCnt().longValue());
                        strSchdlChangeReservation.setApplyLotCnt(scheduleChangeReservation.getAppltLotCount().longValue());
                        strSchdlChangeReservation.setStatus(scheduleChangeReservation.getStatus());
                        strSchdlChangeReservation.setActionCode(scheduleChangeReservation.getActionCode());
                        strSchdlChangeReservation.setLotInfoChangeFlag(scheduleChangeReservation.getLotInfoChangeFlag());
                        objSchdlChangeReservationCheckForActionDROut.setExistFlag(true);

                        //result.setReturnCode(retCodeConfig.getSucc());
                        return objSchdlChangeReservationCheckForActionDROut;
                    }
                }
            }
        }

        return objSchdlChangeReservationCheckForActionDROut;
    }

    @Override
    public void schdlChangeReservationDeleteDR(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation scheduleChangeReservation) {
        String hfsschchgrsvactionCode;
        if (CimStringUtils.isEmpty(scheduleChangeReservation.getActionCode())) {
            hfsschchgrsvactionCode = BizConstant.SP_ACTION_CODE_SCHEDULE_CHANGE;
        } else {
            hfsschchgrsvactionCode = scheduleChangeReservation.getActionCode();
        }
        CimScheduleChangeReservationDO example = new CimScheduleChangeReservationDO();
        example.setObjectID(ObjectIdentifier.fetchValue(scheduleChangeReservation.getObjectID()));
        example.setObjectType(CimStringUtils.getValueOrEmptyString(scheduleChangeReservation.getObjectType()));
        example.setTargetMainProcessDefinitionId(ObjectIdentifier.fetchValue(scheduleChangeReservation.getTargetRouteID()));
        example.setTargetOperationNumber(CimStringUtils.getValueOrEmptyString(scheduleChangeReservation.getTargetOperationNumber()));
        example.setProcessDefinitionSpecId(ObjectIdentifier.fetchValue(scheduleChangeReservation.getProductID()));
        example.setMainProcessDefinitionId(ObjectIdentifier.fetchValue(scheduleChangeReservation.getRouteID()));
        example.setOperationNumber(CimStringUtils.getValueOrEmptyString(scheduleChangeReservation.getOperationNumber()));
        example.setSubLotType(CimStringUtils.getValueOrEmptyString(scheduleChangeReservation.getSubLotType()));
        example.setStartDate(CimStringUtils.getValueOrEmptyString(scheduleChangeReservation.getStartDate()));
        example.setEndDate(CimStringUtils.getValueOrEmptyString(scheduleChangeReservation.getEndDate()));
        example.setActionCode(CimStringUtils.getValueOrEmptyString(hfsschchgrsvactionCode));
        example.setLotInfoChangeFlag(CimBooleanUtils.isTrue(scheduleChangeReservation.getLotInfoChangeFlag()));
        cimJpaRepository.delete(Example.of(example));
    }

    @Override
    public void schdlChangeReservationCheckForFutureOperation(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String routeID, String operationNumber) {

        Boolean upToEndFlag = false ;
        if( CimObjectUtils.isEmpty(routeID) && CimStringUtils.isEmpty(operationNumber) ) {
            upToEndFlag = true ;
        }
        CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);


        //Check whether locateDirecion is Forward or Backward.
        CimProcessDefinition aPosProcessDefinition = aLot.getMainProcessDefinition();

        Validations.check(null == aPosProcessDefinition, retCodeConfig.getNotFoundProcessDefinition());

        String currentRoute = aPosProcessDefinition.getIdentifier();

        String curOperationNumber = aLot.getOperationNumber();

        CimProcessFlowContext aFlowContext = aLot.getProcessFlowContext();
        Validations.check(null == aFlowContext, retCodeConfig.getNotFoundPfx());

        Boolean locateDirection = false ;
        if( CimBooleanUtils.isFalse(upToEndFlag )) {
            Boolean isAfterOperationNumber = aFlowContext.isAfterOperationNumber(operationNumber, curOperationNumber) ;

            if(CimBooleanUtils.isTrue(isAfterOperationNumber)) {
                locateDirection = true;
            } else {
                return;
            }
        } else {
            locateDirection = true ;
        }
        Inputs.ObjSchdlChangeReservationCheckForActionDRIn strTmpSchdlChangeReservation_CheckForActionDR_in = new Inputs.ObjSchdlChangeReservationCheckForActionDRIn();
        strTmpSchdlChangeReservation_CheckForActionDR_in.setLotID(lotID);
        strTmpSchdlChangeReservation_CheckForActionDR_in.setRouteID(currentRoute);
        strTmpSchdlChangeReservation_CheckForActionDR_in.setOperationNumber(curOperationNumber);
        Outputs.ObjSchdlChangeReservationCheckForActionDROut strTmpSchdlChangeReservationCheckForActionOut = this.schdlChangeReservationCheckForActionDR(objCommon, strTmpSchdlChangeReservation_CheckForActionDR_in);
        Validations.check(strTmpSchdlChangeReservationCheckForActionOut.isExistFlag(), retCodeConfig.getSchdresvExistedFutureOperation(), lotID);


        AtomicReference<CimProcessFlow> outMainPF = new AtomicReference<>();
        AtomicReference<CimProcessFlow> outModulePF = new AtomicReference<>();
        AtomicReference<String> outModuleNo = new AtomicReference<>();
        CimProcessOperationSpecification aModulePOS = aFlowContext.getNextProcessOperationSpecification(outMainPF,outModuleNo,outModulePF);

        Integer inSeqNo = 0;
        AtomicReference<Integer> outSeqNo = new AtomicReference<>();
        outSeqNo.set(0);
        AtomicReference<CimProcessFlow> mainPF = outMainPF;
        AtomicReference<CimProcessFlow> modulePF = outModulePF;
        String  moduleNo;

        String  varOutModuleNo = outModuleNo.get();
        moduleNo = varOutModuleNo;
        while(true) {
            outModuleNo = new AtomicReference<>();
            if(null == aModulePOS) {
                break;
            }
            Validations.check(null == mainPF, retCodeConfig.getNotFoundProcessFlow());

            ProcessDefinition outMainPD = mainPF.get().getRootProcessDefinition();
            Validations.check(null == outMainPD, retCodeConfig.getNotFoundPos());

            String outMainPDId = outMainPD.getIdentifier() ;

            String outModuleOpeNo = aModulePOS.getOperationNumber() ;
            String outOpeNo = moduleNo + "." + outModuleOpeNo;

            if( CimBooleanUtils.isFalse(upToEndFlag )) {
                if (CimStringUtils.equals(outMainPDId,routeID) && CimStringUtils.equals(outOpeNo,operationNumber)){
                    break;
                }
            }


            Outputs.ObjSchdlChangeReservationCheckForActionDROut  strSchdlChangeReservation_CheckForAction_out;
            Inputs.ObjSchdlChangeReservationCheckForActionDRIn strSchdlChangeReservation_CheckForActionDR_in = new Inputs.ObjSchdlChangeReservationCheckForActionDRIn();
            strSchdlChangeReservation_CheckForActionDR_in.setLotID(lotID);
            strSchdlChangeReservation_CheckForActionDR_in.setRouteID(outMainPDId);
            strSchdlChangeReservation_CheckForActionDR_in.setOperationNumber(outOpeNo);
            strSchdlChangeReservation_CheckForAction_out = this.schdlChangeReservationCheckForActionDR(objCommon, strSchdlChangeReservation_CheckForActionDR_in );

            Validations.check(strSchdlChangeReservation_CheckForAction_out.isExistFlag(), retCodeConfig.getSchdresvExistedFutureOperation(), lotID);

            inSeqNo  = outSeqNo.get();

            aModulePOS = aFlowContext.getNextProcessOperationSpecificationFor(mainPF.get(),moduleNo,modulePF.get(),aModulePOS,outMainPF,outModuleNo,outModulePF,inSeqNo,outSeqNo);
            moduleNo = outModuleNo.get();
            mainPF   = outMainPF;
            modulePF = outModulePF;
        }

    }

    @Override
    public void scheduleChangeReservationCreateDR(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation scheduleChangeReservation) {
        /*--------------------------------------------*/
        /*   Check of the use situation of Wildcard   */
        /*--------------------------------------------*/
        String getWildCard = StandardProperties.OM_CONSTRAINT_WILDCARD_VALUE.getValue();

        String wildcardPosition = BaseStaticMethod.strrchr(ObjectIdentifier.fetchValue(scheduleChangeReservation.getObjectID()), getWildCard);
        if (!CimStringUtils.isEmpty(wildcardPosition)){
            int nLen = scheduleChangeReservation.getObjectID().getValue().length();
            String ptmpObjectID = scheduleChangeReservation.getObjectID().getValue();
            int fCnt = 0;
            for (int i  = 0;i < nLen ; i++){
                String tempParameter = String.valueOf(ptmpObjectID.charAt(i));
                if (CimStringUtils.equals(tempParameter, getWildCard)){
                    fCnt++;
                    if (fCnt>1){
                        throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidUseWildCard(), scheduleChangeReservation.getObjectID().getValue()));
                    }
                    if (i + 1 != nLen){
                        throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidUseWildCard(), scheduleChangeReservation.getObjectID().getValue()));
                    }
                }
            }
        }
        /*-----------------------------------*/
        /*   Check existence of Input Parm   */
        /*-----------------------------------*/
        CimScheduleChangeReservationDO cimScheduleChangeReservationExam = new CimScheduleChangeReservationDO();
        cimScheduleChangeReservationExam.setObjectID(ObjectIdentifier.fetchValue(scheduleChangeReservation.getObjectID()));
        cimScheduleChangeReservationExam.setObjectType(scheduleChangeReservation.getObjectType());
        if (CimNumberUtils.intValue(cimJpaRepository.count(Example.of(cimScheduleChangeReservationExam))) > 0){
        }

        long count = cimJpaRepository.count("SELECT\n" +
                        "\tCOUNT(TARGET_OBJECT_ID)\n" +
                        "FROM\n" +
                        "\tOSLOTPLANCHGRSV\n" +
                        "WHERE\n" +
                        "\tTARGET_OBJECT_TYPE = ?\n" +
                        "\tAND TARGET_OBJECT_ID = ?\n" +
                        "\tAND ((TO_DATE(START_DATE, 'yyyy-MM-dd') < TO_DATE(?, 'yyyy-MM-dd')\n" +
                        "\tOR (TO_DATE(START_DATE, 'yyyy-MM-dd') = TO_DATE(?, 'yyyy-MM-dd')\n" +
                        "\tAND TO_DATE(START_TIME, 'HH24-mi-ss') <= TO_DATE(?, 'HH24-mi-ss')))\n" +
                        "\tAND (TO_DATE(END_DATE, 'yyyy-MM-dd') > TO_DATE(?, 'yyyy-MM-dd')\n" +
                        "\tOR (TO_DATE(END_DATE, 'yyyy-MM-dd') = TO_DATE(?, 'yyyy-MM-dd')\n" +
                        "\tAND TO_DATE(END_TIME, 'HH24-mi-ss') >= TO_DATE(?, 'HH24-mi-ss'))))",

                scheduleChangeReservation.getObjectType(),
                ObjectIdentifier.fetchValue(scheduleChangeReservation.getObjectID()),
                scheduleChangeReservation.getStartDate(),
                scheduleChangeReservation.getStartDate(),
                scheduleChangeReservation.getStartTime(),
                scheduleChangeReservation.getEndDate(),
                scheduleChangeReservation.getEndDate(),
                scheduleChangeReservation.getEndTime()
                );
        Validations.check(count > 0, retCodeConfigEx.getDuplicationOfSchedule(), scheduleChangeReservation.getObjectID());

        /*-------------------*/
        /*   Decide Status   */
        /*-------------------*/
        String status = null;
        String currentTime = objCommon.getTimeStamp().getReportTimeStamp().toString();
        // .replace(".", ":")
        String startTime = scheduleChangeReservation.getStartDate() + " " + scheduleChangeReservation.getStartTime();
        String endTime = scheduleChangeReservation.getEndDate() + " " + scheduleChangeReservation.getEndTime();
        //startTime = BaseStaticMethod.getTimeByAmOrPm(startTime);
        //endTime = BaseStaticMethod.getTimeByAmOrPm(endTime);
        String splitStartTime = startTime.substring(startTime.indexOf(" ")+1,startTime.length());
        String splitEndTime = endTime.substring(endTime.indexOf(" ")+1,endTime.length());
        if (CimDateUtils.compare(startTime, endTime) >= 0){
            throw new ServiceException(new OmCode(retCodeConfigEx.getDuplicationOfSchedule(), scheduleChangeReservation.getObjectID().getValue()));
        }
        if (CimDateUtils.compare(startTime, currentTime) > 0){
            status = BizConstant.SP_SCHDL_CHG_STATUS_WAITING;
        } else if (CimDateUtils.compare(startTime, currentTime) <= 0 && CimDateUtils.compare(currentTime, endTime) < 0){
            status = BizConstant.SP_SCHDL_CHG_STATUS_ONGOING;
        } else {
            status = BizConstant.SP_SCHDL_CHG_STATUS_COMP;
        }
        /*---------------------*/
        /*   Set Insert Data   */
        /*---------------------*/
        int hFSSCHCHGRSVMAX_LOT_CNT = 0;
        int hFSSCHCHGRSVAPPLY_LOT_COUNT = 0;
        boolean hFSSCHCHGRSVAUTO_ERASE_FLAG = false;
        boolean hFSSCHCHGRSVLOTINFO_CHANGE_FLAG = false;
        String hFSSCHCHGRSVOBJECT_TYPE = scheduleChangeReservation.getObjectType();
        String hFSSCHCHGRSVOBJECT_ID = ObjectIdentifier.fetchValue(scheduleChangeReservation.getObjectID());
        String hFSSCHCHGRSVTARGET_MAINPD_ID = ObjectIdentifier.fetchValue(scheduleChangeReservation.getTargetRouteID());
        String hFSSCHCHGRSVTARGET_OPE_NO = scheduleChangeReservation.getTargetOperationNumber();
        String hFSSCHCHGRSVPRODSPEC_ID = ObjectIdentifier.fetchValue(scheduleChangeReservation.getProductID());
        String hFSSCHCHGRSVMAINPD_ID = ObjectIdentifier.fetchValue(scheduleChangeReservation.getRouteID());
        String hFSSCHCHGRSVOPE_NO = scheduleChangeReservation.getOperationNumber();
        String hFSSCHCHGRSVSUB_LOT_TYPE = scheduleChangeReservation.getSubLotType();
        String hFSSCHCHGRSVSTART_DATE = scheduleChangeReservation.getStartDate();
        String hFSSCHCHGRSVSTART_TIME = scheduleChangeReservation.getStartTime();
        String hFSSCHCHGRSVEND_DATE = scheduleChangeReservation.getEndDate();
        String hFSSCHCHGRSVEND_TIME = scheduleChangeReservation.getEndTime();
        String hFSSCHCHGRSVEVENT_ID = scheduleChangeReservation.getEventID();
        hFSSCHCHGRSVMAX_LOT_CNT = CimNumberUtils.intValue(scheduleChangeReservation.getMaxLotCnt());
        String hFSSCHCHGRSVSTATUS = status;
        hFSSCHCHGRSVAUTO_ERASE_FLAG = scheduleChangeReservation.getEraseAfterUsedFlag();
        hFSSCHCHGRSVLOTINFO_CHANGE_FLAG = scheduleChangeReservation.getLotInfoChangeFlag();
        String hFSSCHCHGRSVACTION_CODE = null;
        if (CimStringUtils.isEmpty(scheduleChangeReservation.getActionCode())){
            hFSSCHCHGRSVACTION_CODE = BizConstant.SP_ACTION_CODE_SCHEDULE_CHANGE;
        } else {
            hFSSCHCHGRSVACTION_CODE = scheduleChangeReservation.getActionCode();
        }
        Timestamp hFSSCHCHGRSVCLAIM_TIME = objCommon.getTimeStamp().getReportTimeStamp();
        String hFSSCHCHGRSVCLAIM_USER_ID = ObjectIdentifier.fetchValue(objCommon.getUser().getUserID());
        CimScheduleChangeReservationDO cimScheduleChangeReservationDO = new CimScheduleChangeReservationDO();
        cimScheduleChangeReservationDO.setObjectType(hFSSCHCHGRSVOBJECT_TYPE);
        cimScheduleChangeReservationDO.setObjectID(hFSSCHCHGRSVOBJECT_ID);
        cimScheduleChangeReservationDO.setTargetMainProcessDefinitionId(hFSSCHCHGRSVTARGET_MAINPD_ID);
        cimScheduleChangeReservationDO.setTargetOperationNumber(hFSSCHCHGRSVTARGET_OPE_NO);
        cimScheduleChangeReservationDO.setProcessDefinitionSpecId(hFSSCHCHGRSVPRODSPEC_ID);
        cimScheduleChangeReservationDO.setMainProcessDefinitionId(hFSSCHCHGRSVMAINPD_ID);
        cimScheduleChangeReservationDO.setOperationNumber(hFSSCHCHGRSVOPE_NO);
        cimScheduleChangeReservationDO.setSubLotType(hFSSCHCHGRSVSUB_LOT_TYPE);
        cimScheduleChangeReservationDO.setStartDate(hFSSCHCHGRSVSTART_DATE);
        cimScheduleChangeReservationDO.setStartTime(splitStartTime);
        cimScheduleChangeReservationDO.setEndDate(hFSSCHCHGRSVEND_DATE);
        cimScheduleChangeReservationDO.setEndTime(splitEndTime);
        cimScheduleChangeReservationDO.setEventId(hFSSCHCHGRSVEVENT_ID);
        cimScheduleChangeReservationDO.setMaxLotCnt(hFSSCHCHGRSVMAX_LOT_CNT);
        cimScheduleChangeReservationDO.setAppltLotCount(hFSSCHCHGRSVAPPLY_LOT_COUNT);
        cimScheduleChangeReservationDO.setStatus(hFSSCHCHGRSVSTATUS);
        cimScheduleChangeReservationDO.setAutoEraseFlag(hFSSCHCHGRSVAUTO_ERASE_FLAG);
        cimScheduleChangeReservationDO.setActionCode(hFSSCHCHGRSVACTION_CODE);
        cimScheduleChangeReservationDO.setLotInfoChangeFlag(hFSSCHCHGRSVLOTINFO_CHANGE_FLAG);
        cimScheduleChangeReservationDO.setClaimTime(hFSSCHCHGRSVCLAIM_TIME);
        cimScheduleChangeReservationDO.setClaimUserId(hFSSCHCHGRSVCLAIM_USER_ID);

        cimJpaRepository.save(cimScheduleChangeReservationDO);

    }

    @Override
    public void schdlChangeReservationChangeDR(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation currentSchdlChangeReservation, Infos.SchdlChangeReservation newSchdlChangeReservation) {
        /*--------------------------------------------*/
        /*   Check of the use situation of Wildcard   */
        /*--------------------------------------------*/
        String getWildCard = StandardProperties.OM_CONSTRAINT_WILDCARD_VALUE.getValue();

        String wildcardPosition = BaseStaticMethod.strrchr(ObjectIdentifier.fetchValue(newSchdlChangeReservation.getObjectID()), getWildCard);
        if (!CimStringUtils.isEmpty(wildcardPosition)) {
            int nLen = newSchdlChangeReservation.getObjectID().getValue().length();
            String ptmpObjectID = newSchdlChangeReservation.getObjectID().getValue();
            int fCnt = 0;
            for (int i = 0; i < nLen; i++) {
                String tempParameter = String.valueOf(ptmpObjectID.charAt(i));
                if (CimStringUtils.equals(tempParameter, getWildCard)) {
                    fCnt++;
                    if (fCnt > 1) {
                        throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidUseWildCard(), newSchdlChangeReservation.getObjectID().getValue()));
                    }
                    if (i + 1 != nLen) {
                        throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidUseWildCard(), newSchdlChangeReservation.getObjectID().getValue()));
                    }
                }
            }
        }
        /*-------------------*/
        /*   Decide Status   */
        /*-------------------*/
        String status = null;
        String currentTime = objCommon.getTimeStamp().getReportTimeStamp().toString();
        String startTime = newSchdlChangeReservation.getStartDate() + " " + newSchdlChangeReservation.getStartTime().replace(".", ":");
        String endTime = newSchdlChangeReservation.getEndDate() + " " + newSchdlChangeReservation.getEndTime().replace(".", ":");
        //startTime = BaseStaticMethod.getTimeByAmOrPm(startTime);
        //endTime = BaseStaticMethod.getTimeByAmOrPm(endTime);
        String splitStartTime = startTime.substring(startTime.indexOf(" ") + 1, startTime.length());
        String splitEndTime = endTime.substring(endTime.indexOf(" ") + 1, endTime.length());
        if (CimDateUtils.compare(startTime, currentTime) > 0) {
            status = BizConstant.SP_SCHDL_CHG_STATUS_WAITING;
        } else if (CimDateUtils.compare(startTime, currentTime) <= 0 && CimDateUtils.compare(currentTime, endTime) < 0) {
            status = BizConstant.SP_SCHDL_CHG_STATUS_ONGOING;
        } else {
            status = BizConstant.SP_SCHDL_CHG_STATUS_COMP;
        }
        /*------------------------------------------------------------------------*/
        /*                            Set   Data                                  */
        /*------------------------------------------------------------------------*/
        /*-----   Set host variables of current data   -----*/
        String hFSSCHCHGRSVOBJECT_TYPE = currentSchdlChangeReservation.getObjectType();
        String hFSSCHCHGRSVOBJECT_ID = ObjectIdentifier.fetchValue(currentSchdlChangeReservation.getObjectID());
        String hFSSCHCHGRSVTARGET_MAINPD_ID = ObjectIdentifier.fetchValue(currentSchdlChangeReservation.getTargetRouteID());
        String hFSSCHCHGRSVTARGET_OPE_NO = currentSchdlChangeReservation.getTargetOperationNumber();
        String hFSSCHCHGRSVPRODSPEC_ID = ObjectIdentifier.fetchValue(currentSchdlChangeReservation.getProductID());
        String hFSSCHCHGRSVMAINPD_ID = ObjectIdentifier.fetchValue(currentSchdlChangeReservation.getRouteID());
        String hFSSCHCHGRSVOPE_NO = currentSchdlChangeReservation.getOperationNumber();
        String hFSSCHCHGRSVSUB_LOT_TYPE = currentSchdlChangeReservation.getSubLotType();
        String hFSSCHCHGRSVSTART_DATE = currentSchdlChangeReservation.getStartDate();
        String hFSSCHCHGRSVSTART_TIME = currentSchdlChangeReservation.getStartTime();
        String hFSSCHCHGRSVEND_DATE = currentSchdlChangeReservation.getEndDate();
        String hFSSCHCHGRSVEND_TIME = currentSchdlChangeReservation.getEndTime();
        String hFSSCHCHGRSVACTION_CODE = null;
        if (CimStringUtils.isEmpty(currentSchdlChangeReservation.getActionCode())) {
            hFSSCHCHGRSVACTION_CODE = BizConstant.SP_ACTION_CODE_SCHEDULE_CHANGE;
        } else {
            hFSSCHCHGRSVACTION_CODE = currentSchdlChangeReservation.getActionCode();
        }
        Boolean hFSSCHCHGRSVLOTINFO_CHANGE_FLAG = currentSchdlChangeReservation.getLotInfoChangeFlag();
        /*-----   Set host variables of update data   -----*/
        String hFSSCHCHGRSV_UPD_OBJECT_TYPE = newSchdlChangeReservation.getObjectType();
        String hFSSCHCHGRSV_UPD_OBJECT_ID = ObjectIdentifier.fetchValue(newSchdlChangeReservation.getObjectID());
        String hFSSCHCHGRSV_UPD_TARGET_MAINPD_ID = ObjectIdentifier.fetchValue(newSchdlChangeReservation.getTargetRouteID());
        String hFSSCHCHGRSV_UPD_TARGET_OPE_NO = newSchdlChangeReservation.getTargetOperationNumber();
        String hFSSCHCHGRSV_UPD_PRODSPEC_ID = ObjectIdentifier.fetchValue(newSchdlChangeReservation.getProductID());
        String hFSSCHCHGRSV_UPD_MAINPD_ID = ObjectIdentifier.fetchValue(newSchdlChangeReservation.getRouteID());
        String hFSSCHCHGRSV_UPD_OPE_NO = newSchdlChangeReservation.getOperationNumber();
        String hFSSCHCHGRSV_UPD_SUB_LOT_TYPE = newSchdlChangeReservation.getSubLotType();
        String hFSSCHCHGRSV_UPD_START_DATE = newSchdlChangeReservation.getStartDate();
        String hFSSCHCHGRSV_UPD_START_TIME = splitStartTime;
        String hFSSCHCHGRSV_UPD_END_DATE = newSchdlChangeReservation.getEndDate();
        String hFSSCHCHGRSV_UPD_END_TIME = splitEndTime;
        String hFSSCHCHGRSV_UPD_EVENT_ID = newSchdlChangeReservation.getEventID();
        Long hFSSCHCHGRSV_UPD_MAX_LOT_CNT = newSchdlChangeReservation.getMaxLotCnt();
        Long hFSSCHCHGRSV_UPD_APPLY_LOT_COUNT = newSchdlChangeReservation.getApplyLotCnt();
        String hFSSCHCHGRSV_UPD_STATUS = status;
        Boolean hFSSCHCHGRSV_UPD_AUTO_ERASE_FLAG = newSchdlChangeReservation.getEraseAfterUsedFlag();
        String hFSSCHCHGRSV_UPD_ACTION_CODE = null;
        if (CimStringUtils.isEmpty(newSchdlChangeReservation.getActionCode())) {
            hFSSCHCHGRSV_UPD_ACTION_CODE = BizConstant.SP_ACTION_CODE_SCHEDULE_CHANGE;
        } else {
            hFSSCHCHGRSV_UPD_ACTION_CODE = newSchdlChangeReservation.getActionCode();
        }
        Boolean hFSSCHCHGRSV_UPD_LOTINFO_CHANGE_FLAG = newSchdlChangeReservation.getLotInfoChangeFlag();
        String hFSSCHCHGRSV_UPD_CLAIM_TIME = CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp());
        String hFSSCHCHGRSV_UPD_CLAIM_USER_ID = objCommon.getUser().getUserID().getValue();
        /*------------------------------*/
        /*   Check of a Schedule Term   */
        /*------------------------------*/
        String sql = "SELECT COUNT(TARGET_OBJECT_ID)\n" +
                "                   FROM OSLOTPLANCHGRSV\n" +
                "                  WHERE TARGET_OBJECT_TYPE = ?\n" +
                "                    AND TARGET_OBJECT_ID   = ?\n" +
                "                    AND ((TO_DATE(START_DATE, 'yyyy-MM-dd') < TO_DATE(?, 'yyyy-MM-dd')\n" +
                "                    OR (TO_DATE(START_DATE, 'yyyy-MM-dd') = TO_DATE(?, 'yyyy-MM-dd')\n" +
                "                    AND TO_DATE(START_TIME, 'HH24-mi-ss') <= TO_DATE(?, 'HH24-mi-ss')))\n" +
                "                    AND (TO_DATE(END_DATE, 'yyyy-MM-dd') > TO_DATE(?, 'yyyy-MM-dd')\n" +
                "                    OR (TO_DATE(END_DATE, 'yyyy-MM-dd') = TO_DATE(?, 'yyyy-MM-dd')\n" +
                "                    AND TO_DATE(END_TIME, 'HH24-mi-ss') >= TO_DATE(?, 'HH24-mi-ss'))))\n" +
                "                    AND (TARGET_PROCESS_ID   <> ?\n" +
                "                    OR  TARGET_OPE_NO       <> ?\n" +
                "                    OR  PROD_ID         <> ?\n" +
                "                    OR  PROCESS_ID           <> ?\n" +
                "                    OR  OPE_NO              <> ?\n" +
                "                    OR  SUB_LOT_TYPE        <> ?\n" +
                "                    OR  START_DATE          <> ?\n" +
                "                    OR  START_TIME          <> ?\n" +
                "                    OR  END_DATE            <> ?\n" +
                "                    OR  END_TIME            <> ?\n" +
                "                    OR  ACTION_CODE         <> ?\n" +
                "                    OR  LOT_INFO_CHG_FLAG <> ?)";
        long totalcount = cimJpaRepository.count(sql, hFSSCHCHGRSV_UPD_OBJECT_TYPE, hFSSCHCHGRSV_UPD_OBJECT_ID, hFSSCHCHGRSV_UPD_END_DATE, hFSSCHCHGRSV_UPD_END_DATE,
                hFSSCHCHGRSV_UPD_END_TIME, hFSSCHCHGRSV_UPD_START_DATE, hFSSCHCHGRSV_UPD_START_DATE, hFSSCHCHGRSV_UPD_START_TIME, hFSSCHCHGRSVTARGET_MAINPD_ID,
                hFSSCHCHGRSVTARGET_OPE_NO, hFSSCHCHGRSVPRODSPEC_ID, hFSSCHCHGRSVMAINPD_ID, hFSSCHCHGRSVOPE_NO, hFSSCHCHGRSVSUB_LOT_TYPE, hFSSCHCHGRSVSTART_DATE,
                hFSSCHCHGRSVSTART_TIME, hFSSCHCHGRSVEND_DATE, hFSSCHCHGRSVEND_TIME, hFSSCHCHGRSVACTION_CODE, CimBooleanUtils.isTrue(hFSSCHCHGRSVLOTINFO_CHANGE_FLAG) ? 1 : 0);
        Validations.check(totalcount > 0, new OmCode(retCodeConfigEx.getDuplicationOfSchedule(), newSchdlChangeReservation.getObjectID().getValue()));
        if (hFSSCHCHGRSV_UPD_START_DATE.compareTo(hFSSCHCHGRSV_UPD_END_DATE) > 0
                || (hFSSCHCHGRSV_UPD_START_DATE.compareTo(hFSSCHCHGRSV_UPD_END_DATE) == 0
                && hFSSCHCHGRSV_UPD_START_TIME.compareTo(hFSSCHCHGRSV_UPD_END_TIME) >= 0)) {
            throw new ServiceException(new OmCode(retCodeConfigEx.getDuplicationOfSchedule(), newSchdlChangeReservation.getObjectID().getValue()));
        }
        /*------------------------------------------------------------------------*/
        /*                           Update   Data                                */
        /*------------------------------------------------------------------------*/
        sql = "SELECT * FROM OSLOTPLANCHGRSV\n" +
                "                  WHERE nvl(TARGET_OBJECT_TYPE         ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(TARGET_OBJECT_ID           ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(TARGET_PROCESS_ID    ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(TARGET_OPE_NO       ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(PROD_ID         ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(PROCESS_ID           ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(OPE_NO              ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(SUB_LOT_TYPE        ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(START_DATE          ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(START_TIME          ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(END_DATE            ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(END_TIME            ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(ACTION_CODE         ,'N') = nvl(?,'N')\n" +
                "                    AND nvl(LOT_INFO_CHG_FLAG , 0) = nvl(?,0)";
        List<CimScheduleChangeReservationDO> cimScheduleChangeReservationDOList = cimJpaRepository.query(sql, CimScheduleChangeReservationDO.class, hFSSCHCHGRSVOBJECT_TYPE, hFSSCHCHGRSVOBJECT_ID, hFSSCHCHGRSVTARGET_MAINPD_ID,
                hFSSCHCHGRSVTARGET_OPE_NO, hFSSCHCHGRSVPRODSPEC_ID, hFSSCHCHGRSVMAINPD_ID, hFSSCHCHGRSVOPE_NO, hFSSCHCHGRSVSUB_LOT_TYPE, hFSSCHCHGRSVSTART_DATE,
                hFSSCHCHGRSVSTART_TIME, hFSSCHCHGRSVEND_DATE, hFSSCHCHGRSVEND_TIME, hFSSCHCHGRSVACTION_CODE, (CimBooleanUtils.isTrue(hFSSCHCHGRSVLOTINFO_CHANGE_FLAG) ? 1 : 0));
        if (!CimArrayUtils.isEmpty(cimScheduleChangeReservationDOList)) {
            // firt delete
//            cimScheduleChangeReservationDOList.forEach(cimScheduleChangeReservationDO -> cimJpaRepository.removeNonRuntimeEntity(cimScheduleChangeReservationDO));
            // then add
            String finalHFSSCHCHGRSV_UPD_ACTION_CODE = hFSSCHCHGRSV_UPD_ACTION_CODE;
            cimScheduleChangeReservationDOList.forEach(newCimScheduleChangeReservationDO -> {
                newCimScheduleChangeReservationDO.setObjectType(hFSSCHCHGRSV_UPD_OBJECT_TYPE);
                newCimScheduleChangeReservationDO.setObjectID(hFSSCHCHGRSV_UPD_OBJECT_ID);
                newCimScheduleChangeReservationDO.setTargetMainProcessDefinitionId(hFSSCHCHGRSV_UPD_TARGET_MAINPD_ID);
                newCimScheduleChangeReservationDO.setTargetOperationNumber(hFSSCHCHGRSV_UPD_TARGET_OPE_NO);
                newCimScheduleChangeReservationDO.setProcessDefinitionSpecId(hFSSCHCHGRSV_UPD_PRODSPEC_ID);
                newCimScheduleChangeReservationDO.setMainProcessDefinitionId(hFSSCHCHGRSV_UPD_MAINPD_ID);
                newCimScheduleChangeReservationDO.setOperationNumber(hFSSCHCHGRSV_UPD_OPE_NO);
                newCimScheduleChangeReservationDO.setSubLotType(hFSSCHCHGRSV_UPD_SUB_LOT_TYPE);
                newCimScheduleChangeReservationDO.setStartDate(hFSSCHCHGRSV_UPD_START_DATE);
                newCimScheduleChangeReservationDO.setStartTime(hFSSCHCHGRSV_UPD_START_TIME);
                newCimScheduleChangeReservationDO.setEndDate(hFSSCHCHGRSV_UPD_END_DATE);
                newCimScheduleChangeReservationDO.setEndTime(hFSSCHCHGRSV_UPD_END_TIME);
                newCimScheduleChangeReservationDO.setEventId(hFSSCHCHGRSV_UPD_EVENT_ID);
                newCimScheduleChangeReservationDO.setMaxLotCnt(hFSSCHCHGRSV_UPD_MAX_LOT_CNT.intValue());
                newCimScheduleChangeReservationDO.setAppltLotCount(hFSSCHCHGRSV_UPD_APPLY_LOT_COUNT.intValue());
                newCimScheduleChangeReservationDO.setStatus(hFSSCHCHGRSV_UPD_STATUS);
                newCimScheduleChangeReservationDO.setAutoEraseFlag(hFSSCHCHGRSV_UPD_AUTO_ERASE_FLAG);
                newCimScheduleChangeReservationDO.setActionCode(finalHFSSCHCHGRSV_UPD_ACTION_CODE);
                newCimScheduleChangeReservationDO.setLotInfoChangeFlag(hFSSCHCHGRSV_UPD_LOTINFO_CHANGE_FLAG);
                newCimScheduleChangeReservationDO.setClaimTime(CimDateUtils.convertToOrInitialTime(hFSSCHCHGRSV_UPD_CLAIM_TIME));
                newCimScheduleChangeReservationDO.setClaimUserId(hFSSCHCHGRSV_UPD_CLAIM_USER_ID);
                cimJpaRepository.save(newCimScheduleChangeReservationDO);
            });
        }

    }

    @Override
    public void schdlChangeReservationCheckForRegistration(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation schdlChangeReservation){
        // Check objectType
        Validations.check (!CimStringUtils.equals(schdlChangeReservation.getObjectType(), BizConstant.SP_SCHDL_CHG_OBJTYPE_LOT)
                && !CimStringUtils.equals(schdlChangeReservation.getObjectType(), BizConstant.SP_SCHDL_CHG_OBJTYPE_PRD)
                && !CimStringUtils.equals(schdlChangeReservation.getObjectType(), BizConstant.SP_SCHDL_CHG_OBJTYPE_MPD)
                && !CimStringUtils.equals(schdlChangeReservation.getObjectType(), BizConstant.SP_SCHDL_CHG_OBJTYPE_MDP), retCodeConfig.getInvalidInputParam());
        // Check Target RouteID / OpeNo
        Inputs.ProcessOperationListForRoute strProcessOperationListForRouteIn = new Inputs.ProcessOperationListForRoute();
        strProcessOperationListForRouteIn.setRouteID(schdlChangeReservation.getTargetRouteID());
        strProcessOperationListForRouteIn.setOperationID(new ObjectIdentifier());
        strProcessOperationListForRouteIn.setOperationNumber(schdlChangeReservation.getTargetOperationNumber());
        strProcessOperationListForRouteIn.setPdType("");
        strProcessOperationListForRouteIn.setSearchCount(1);
        List<Infos.OperationNameAttributes> processOperationListForRouteResult = null;
        try{
            processOperationListForRouteResult = processMethod.processOperationListForRoute(objCommon, strProcessOperationListForRouteIn);
        }catch (ServiceException ex){
            Validations.check(true,new OmCode(retCodeConfig.getNotFoundRouteOpe(), schdlChangeReservation.getTargetRouteID().getValue(), schdlChangeReservation.getTargetOperationNumber()));
        }
        if (!ObjectIdentifier.isEmptyWithValue(schdlChangeReservation.getProductID())){
            CimProductSpecification aPosProdSpec = baseCoreFactory.getBO(CimProductSpecification.class, schdlChangeReservation.getProductID());
        }
        if (!ObjectIdentifier.isEmptyWithValue(schdlChangeReservation.getRouteID()) && !CimStringUtils.isEmpty(schdlChangeReservation.getOperationNumber())){
            // Check RouteID / OpeNo
            Inputs.ProcessOperationListForRoute processOperationListForRouteIn = new Inputs.ProcessOperationListForRoute();
            processOperationListForRouteIn.setRouteID(schdlChangeReservation.getRouteID());
            processOperationListForRouteIn.setOperationNumber(schdlChangeReservation.getOperationNumber());
            processOperationListForRouteIn.setSearchCount(1);
            List<Infos.OperationNameAttributes> processoperationListForRouteResult = null;
            try{
                processoperationListForRouteResult = processMethod.processOperationListForRoute(objCommon, processOperationListForRouteIn);
            } catch (ServiceException ex){
                Validations.check(new OmCode(retCodeConfig.getNotFoundRouteOpe(), schdlChangeReservation.getRouteID().getValue(), schdlChangeReservation.getOperationNumber()));
            }
        }
        // Check eraseAfterUsedFlag   no need
        // Check existence for subLotType
        if (!CimStringUtils.isEmpty(schdlChangeReservation.getSubLotType())){
            Outputs.LotSubLotTypeGetDetailInfoDR lotSubLotTypeGetDetailInfoDRRetCode = lotMethod.lotSubLotTypeGetDetailInfoDR(objCommon, schdlChangeReservation.getSubLotType());
        }
    }

    @Override
    public void schdlChangeReservationCheckForMerge(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        String objectID = parentLotID.getValue();
        String objectType = "Lot";
        String tmpString = "";

        /*-----------------------------------------*/
        /*   Get Reservation List By parentLotID   */
        /*-----------------------------------------*/
        log.info("Get Reservation List By parentLotID");
        log.info("【step1】- schdlChangeReservation_GetListDR__110");
        Inputs.ObjScheduleChangeReservationGetListIn inParams = new Inputs.ObjScheduleChangeReservationGetListIn();
        inParams.setEventID(tmpString);
        inParams.setObjectID(objectID);
        inParams.setObjectType(objectType);
        inParams.setTargetRouteID(tmpString);
        inParams.setTargetOperationNumber(tmpString);
        inParams.setProductID(tmpString);
        inParams.setRouteID(tmpString);
        inParams.setOperationNumber(tmpString);
        inParams.setSubLotType(tmpString);
        inParams.setStartDate(tmpString);
        inParams.setEndDate(tmpString);
        inParams.setStatus(tmpString);
        inParams.setLotInfoChangeFlag(0L);
        List<Infos.SchdlChangeReservation> schdlChangeReservationsForParent = this.schdlChangeReservationGetListDR(objCommon, inParams);

        /*-----------------------------------------*/
        /*   Get Reservation List By childLotID   */
        /*-----------------------------------------*/
        objectID = childLotID.getValue();
        log.info("Get Reservation List By childLotID");
        log.info("【step2】- schdlChangeReservation_GetListDR__110");
        Inputs.ObjScheduleChangeReservationGetListIn inParamsChild = new Inputs.ObjScheduleChangeReservationGetListIn();
        inParamsChild.setEventID(tmpString);
        inParamsChild.setObjectID(objectID);
        inParamsChild.setObjectType(objectType);
        inParamsChild.setTargetRouteID(tmpString);
        inParamsChild.setTargetOperationNumber(tmpString);
        inParamsChild.setProductID(tmpString);
        inParamsChild.setRouteID(tmpString);
        inParamsChild.setOperationNumber(tmpString);
        inParamsChild.setSubLotType(tmpString);
        inParamsChild.setStartDate(tmpString);
        inParamsChild.setEndDate(tmpString);
        inParamsChild.setStatus(tmpString);
        inParamsChild.setLotInfoChangeFlag(0L);
        List<Infos.SchdlChangeReservation> schdlChangeReservationsForChild = this.schdlChangeReservationGetListDR(objCommon, inParamsChild);

        //Get Current_Date todo confirm the currentDate characters from DB
        log.info("【get Current Date】");
        String currentDate = CimDateUtils.convertToSpecString(new Timestamp(System.currentTimeMillis()));
        //Get Current_Time todo confirm the currentTime characters from DB
        log.info("【get Current Time】");
        String currentTime = CimDateUtils.convertToSpecString(new Timestamp(System.currentTimeMillis()));
        /*---------------------------------------------------------------*/
        /*   Check Reservation List Compare parentLotID And childLotID   */
        /*---------------------------------------------------------------*/
        log.info("Check Reservation List Compare parentLotID And childLotID");
        int nLen = CimArrayUtils.getSize(schdlChangeReservationsForChild);
        for (int i = 0; i < nLen; i++) {
            if (Objects.equals(schdlChangeReservationsForChild.get(i).getMaxLotCnt(), schdlChangeReservationsForChild.get(i).getApplyLotCnt())){
                log.info("maxLotCnt == applyLotCnt");
                continue;
            }
            if (!CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getEndDate(),currentDate)
                    || CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getEndDate(),currentDate)
                    && !CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getEndTime(),currentTime)){
                log.info("Expiration","[childLotID]continue");
                continue;
            }
            int oLen = CimArrayUtils.getSize(schdlChangeReservationsForParent);
            if (oLen == 0){
                log.info("Existed","oLen == 0");
                throw new ServiceException(new OmCode(retCodeConfigEx.getSchdresvUnMatch(),parentLotID.getValue(),childLotID.getValue()));
            }
            for (int j = 0; j <= oLen; j++) {
                if (oLen == j){
                    log.info("Existed","oLen == j");
                    throw new ServiceException(new OmCode(retCodeConfigEx.getSchdresvUnMatch(),parentLotID.getValue(),childLotID.getValue()));
                }
                if (!CimStringUtils.equals(schdlChangeReservationsForParent.get(i).getEndDate(),currentDate)
                        || CimStringUtils.equals(schdlChangeReservationsForParent.get(i).getEndDate(),currentDate)
                        && !CimStringUtils.equals(schdlChangeReservationsForParent.get(i).getEndTime(),currentTime)){
                    log.info("Expiration","[parentLotID]continue");
                    continue;
                }
                /*------------------------------------------------*/
                /*   Check Reservation List Data Is Same OR Not   */
                /*------------------------------------------------*/
                if (CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getTargetRouteID().getValue(), schdlChangeReservationsForParent.get(j).getTargetRouteID().getValue())
                        && CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getTargetOperationNumber(), schdlChangeReservationsForParent.get(j).getTargetOperationNumber())
                        && CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getProductID().getValue(), schdlChangeReservationsForParent.get(j).getProductID().getValue())
                        && CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getRouteID().getValue(), schdlChangeReservationsForParent.get(j).getRouteID().getValue())
                        && CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getOperationNumber(), schdlChangeReservationsForParent.get(j).getOperationNumber())
                        && CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getSubLotType(), schdlChangeReservationsForParent.get(j).getSubLotType())
                        && CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getStartDate(), schdlChangeReservationsForParent.get(j).getStartDate())
                        && CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getStartTime(), schdlChangeReservationsForParent.get(j).getStartTime())
                        && CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getEndDate(), schdlChangeReservationsForParent.get(j).getEndDate())
                        && CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getEndTime(), schdlChangeReservationsForParent.get(j).getEndTime())
                        && (Objects.equals(schdlChangeReservationsForChild.get(i).getLotInfoChangeFlag(), schdlChangeReservationsForParent.get(j).getLotInfoChangeFlag()))
                        && CimStringUtils.equals(schdlChangeReservationsForChild.get(i).getActionCode(), schdlChangeReservationsForParent.get(j).getActionCode())){
                    log.info("Same Data");
                    break;
                }
            }
        }
        for (int k = 0; k < nLen; k++) {
            log.info("schdlChangeReservation_DeleteDR For Child");
            Infos.SchdlChangeReservation scheduleChangeReservation = schdlChangeReservationsForChild.get(k);
            log.info("【step3】schdlChangeReservation_DeleteDR__110");
            this.schdlChangeReservationDeleteDR(objCommon,scheduleChangeReservation);
        }
    }

    @Override
    public void schdlChangeReservationCheckForBranchCancelDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        /*---------------------------------------*/
        /*   Get MainPD_ID And OperationNumber   */
        /*---------------------------------------*/
        String sql  = String.format("SELECT\n" +
                "                     OMPROPE.MAIN_PROCESS_ID,\n" +
                "                     OMPROPE.OPE_NO\n" +
                "                 FROM\n" +
                "                     OMLOT,OMPROPE\n" +
                "                 WHERE\n" +
                "                     OMLOT.PROPE_RKEY = OMPROPE.ID\n" +
                "                 AND\n" +
                "                     OMLOT.LOT_ID = '%s'", lotID.getValue());
        CimProcessOperationDO cimProcessOperationDO = cimJpaRepository.queryOne(sql, CimProcessOperationDO.class);
        /*---------------------------------------*/
        /*   Get MainPD_ID And OperationNumber   */
        /*---------------------------------------*/
        String routeID = cimProcessOperationDO.getMainProcessDefinitionID();
        String operationNumber = cimProcessOperationDO.getOperationNumber();
        Inputs.ObjSchdlChangeReservationCheckForActionDRIn objSchdlChangeReservationCheckForActionDRIn = new Inputs.ObjSchdlChangeReservationCheckForActionDRIn();
        objSchdlChangeReservationCheckForActionDRIn.setLotID(lotID);
        objSchdlChangeReservationCheckForActionDRIn.setRouteID(routeID);
        objSchdlChangeReservationCheckForActionDRIn.setOperationNumber(operationNumber);
        Outputs.ObjSchdlChangeReservationCheckForActionDROut objSchdlChangeReservationCheckForActionDROut = this.schdlChangeReservationCheckForActionDR(objCommon, objSchdlChangeReservationCheckForActionDRIn);
        if (CimBooleanUtils.isTrue(objSchdlChangeReservationCheckForActionDROut.isExistFlag())){
            throw new ServiceException(new OmCode(retCodeConfig.getSchdresvExistedFutureOperation(), lotID.getValue()));
        }
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
        CimProcessFlowContext aProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(aProcessFlowContext == null, new OmCode(retCodeConfig.getNotFoundPfx(), ""));
        AtomicReference<CimProcessFlow> outModulePF = new AtomicReference<>();
        AtomicReference<CimProcessFlow> outMainPF = new AtomicReference<>();
        AtomicReference<String> outModuleNo = new AtomicReference<>();
        CimProcessOperationSpecification aModulePOS = aProcessFlowContext.getNextProcessOperationSpecificationOnCurrentRoute(outMainPF, outModuleNo, outModulePF);
        String aModuleNo = outModuleNo.get();
        CimProcessFlow aMainPF = outMainPF.get();
        CimProcessFlow aModulePF = outModulePF.get();
        while (true){
            AtomicReference<CimProcessFlow> outModulePFInner = new AtomicReference<>();
            AtomicReference<CimProcessFlow> outMainPFInner = new AtomicReference<>();
            AtomicReference<String> outModuleNoInnner = new AtomicReference<>();
            if (aModulePOS == null){
                break;
            }
            Validations.check(aMainPF == null, new OmCode(retCodeConfig.getNotFoundProcessFlow(), ""));
            CimProcessDefinition outMainPD = aMainPF.getRootProcessDefinition();
            Validations.check(outMainPD == null, new OmCode(retCodeConfig.getNotFoundPos(), lotID.getValue()));
            String outMainPDId = outMainPD.getIdentifier();
            String outModuleOpeNo = aModulePOS.getOperationNumber();
            String outOpeNo = BaseStaticMethod.convertModuleOpeNoToOpeNo(aModuleNo, outModuleOpeNo);
            Inputs.ObjSchdlChangeReservationCheckForActionDRIn objSchdlChangeReservationCheckForActionDRIn2 = new Inputs.ObjSchdlChangeReservationCheckForActionDRIn();
            objSchdlChangeReservationCheckForActionDRIn2.setLotID(lotID);
            objSchdlChangeReservationCheckForActionDRIn2.setRouteID(outMainPDId);
            objSchdlChangeReservationCheckForActionDRIn2.setOperationNumber(outOpeNo);
            Outputs.ObjSchdlChangeReservationCheckForActionDROut objSchdlChangeReservationCheckForActionDROut2 = this.schdlChangeReservationCheckForActionDR(objCommon, objSchdlChangeReservationCheckForActionDRIn2);
            if (CimBooleanUtils.isTrue(objSchdlChangeReservationCheckForActionDROut2.isExistFlag())){
                throw new ServiceException(new OmCode(retCodeConfig.getSchdresvExistedFutureOperation(), lotID.getValue()));
            }
            aModulePOS = aProcessFlowContext.getNextProcessOperationSpecificationOnCurrentRouteFor(aMainPF, aModuleNo, aModulePF, aModulePOS,
                    outMainPFInner, outModuleNoInnner, outModulePFInner);
            aModuleNo = outModuleNoInnner.get();
            aMainPF = outMainPFInner.get();
            aModulePF = outModulePFInner.get();
        }
    }

    @Override
    public void  schdlChangeReservationApplyCountIncreaseDR(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation schdlChangeReservation) {
        long applyLotCount = schdlChangeReservation.getApplyLotCnt() + 1;
        boolean autoEraseFlag = true; //The default setting is true
        boolean updateFlag = false;
        String actionCode = CimStringUtils.isEmpty(schdlChangeReservation.getActionCode()) ?
                BizConstant.SP_ACTION_CODE_SCHEDULE_CHANGE :
                schdlChangeReservation.getActionCode();

        String sql = "SELECT * from OSLOTPLANCHGRSV  WHERE TARGET_OBJECT_TYPE      =  ?\n" +
                "                    AND TARGET_OBJECT_ID        =  ?\n" +
                "                    AND TARGET_PROCESS_ID =  ?\n" +
                "                    AND TARGET_OPE_NO    =  ?\n" +
                "                    AND PROD_ID      =  ?\n" +
                "                    AND PROCESS_ID        =  ?\n" +
                "                    AND OPE_NO           =  ?\n" +
                "                    AND SUB_LOT_TYPE     =  ?\n" +
                "                    AND START_DATE       =  ?\n" +
                "                    AND START_TIME       =  ?\n" +
                "                    AND END_DATE         =  ?\n" +
                "                    AND END_TIME         =  ?\n" +
                "                    AND MAX_CNT_FOR_APPLY      <= ?\n" +
                "                    AND AUTO_REMOVE_FLAG  =  ?\n" +
                "                    AND ACTION_CODE      =  ?\n" +
                "                    AND LOT_INFO_CHG_FLAG = ?";
        CimScheduleChangeReservationDO cimScheduleChangeReservationDO = cimJpaRepository.queryOne(sql, CimScheduleChangeReservationDO.class,
                schdlChangeReservation.getObjectType(), schdlChangeReservation.getObjectID().getValue(),
                schdlChangeReservation.getTargetRouteID().getValue(), schdlChangeReservation.getTargetOperationNumber(),
                schdlChangeReservation.getProductID().getValue(),
                schdlChangeReservation.getRouteID().getValue(), schdlChangeReservation.getOperationNumber(),
                schdlChangeReservation.getSubLotType(), schdlChangeReservation.getStartDate(), schdlChangeReservation.getStartTime(),
                schdlChangeReservation.getEndDate(), schdlChangeReservation.getEndTime(), applyLotCount,
                autoEraseFlag, actionCode, schdlChangeReservation.getLotInfoChangeFlag());
        if (cimScheduleChangeReservationDO == null) {
            updateFlag = true;
        } else {
            cimJpaRepository.delete(cimScheduleChangeReservationDO);
        }
        if (updateFlag) {
            CimScheduleChangeReservationDO cimScheduleChangeReservationExam = new CimScheduleChangeReservationDO();
            cimScheduleChangeReservationExam.setObjectType(CimStringUtils.getValueOrEmptyString(schdlChangeReservation.getObjectType()));
            cimScheduleChangeReservationExam.setObjectID(ObjectIdentifier.fetchValue(schdlChangeReservation.getObjectID()));
            cimScheduleChangeReservationExam.setTargetMainProcessDefinitionId(ObjectIdentifier.fetchValue(schdlChangeReservation.getTargetRouteID()));
            cimScheduleChangeReservationExam.setTargetOperationNumber(CimStringUtils.getValueOrEmptyString(schdlChangeReservation.getTargetOperationNumber()));
            cimScheduleChangeReservationExam.setProcessDefinitionSpecId(ObjectIdentifier.fetchValue(schdlChangeReservation.getProductID()));
            cimScheduleChangeReservationExam.setMainProcessDefinitionId(ObjectIdentifier.fetchValue(schdlChangeReservation.getRouteID()));
            cimScheduleChangeReservationExam.setOperationNumber(CimStringUtils.getValueOrEmptyString(schdlChangeReservation.getOperationNumber()));
            cimScheduleChangeReservationExam.setSubLotType(CimStringUtils.getValueOrEmptyString(schdlChangeReservation.getSubLotType()));
            cimScheduleChangeReservationExam.setStartDate(CimStringUtils.getValueOrEmptyString(schdlChangeReservation.getStartDate()));
            cimScheduleChangeReservationExam.setStartTime(CimStringUtils.getValueOrEmptyString(schdlChangeReservation.getStartTime()));
            cimScheduleChangeReservationExam.setEndDate(CimStringUtils.getValueOrEmptyString(schdlChangeReservation.getEndDate()));
            cimScheduleChangeReservationExam.setEndTime(CimStringUtils.getValueOrEmptyString(schdlChangeReservation.getEndTime()));
            cimScheduleChangeReservationExam.setActionCode(CimStringUtils.getValueOrEmptyString(actionCode));
            cimScheduleChangeReservationExam.setLotInfoChangeFlag(CimBooleanUtils.isTrue(schdlChangeReservation.getLotInfoChangeFlag()));
            cimJpaRepository.findOne(Example.of(cimScheduleChangeReservationExam))
                    .ifPresent(data -> {
                        data.setAppltLotCount(CimNumberUtils.intValue(applyLotCount));
                        cimJpaRepository.save(data);
                    });
        }
    }
}
