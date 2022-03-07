package com.fa.cim.method.impl;


import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.nonruntime.apc.CimApcRunCapaDO;
import com.fa.cim.entity.nonruntime.apc.CimApcRunCapaInstDO;
import com.fa.cim.entity.nonruntime.apc.CimApcRunCapaLotDO;
import com.fa.cim.entity.runtime.po.CimProcessOperationDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IControlJobMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.newcore.bo.CimBO;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimMachineContainerPosition;
import com.fa.cim.newcore.bo.machine.CimMaterialLocation;
import com.fa.cim.newcore.bo.machine.CimPortResource;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.prodspec.CimProductGroup;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.prodspec.CimTechnology;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.mchnmngm.BufferResource;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.newcore.standard.mchnmngm.MaterialLocation;
import com.fa.cim.newcore.standard.mtrlmngm.Material;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * ControlJobCompImpl .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/16        ********             PlayBoy               create file
 *
 * @author PlayBoy
 * @since 2018/7/16 18:51
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class ControlJobMethod  implements IControlJobMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    @Qualifier("ProductManagerCore")
    private ProductManager productManager;

    /**
     * controlJob_ProcessOperationList_GetDR
     * @author ho
     * @param objCommon -
     * @param objControlJobProcessOperationListGetDRIn -
     */
    @Override
    public List<Infos.ProcessOperationLot> controlJobProcessOperationListGetDR(Infos.ObjCommon objCommon, Inputs.ObjControlJobProcessOperationListGetDRIn objControlJobProcessOperationListGetDRIn) {
        List<Infos.ProcessOperationLot> processOperationLots =new ArrayList<>();
        boolean lotSpecified = !ObjectIdentifier.isEmpty(objControlJobProcessOperationListGetDRIn.getLotID());
        String hFRPOCTRLJOB_ID=objControlJobProcessOperationListGetDRIn.getControlJobID().getValue();
        String querySql = String.format("select ID\n" +
                "            from   OMPROPE\n" +
                "            where  CJ_ID = '%s'", hFRPOCTRLJOB_ID);
        List<CimProcessOperationDO> processOperations = cimJpaRepository.query(querySql, CimProcessOperationDO.class);
        if (!CimArrayUtils.isEmpty(processOperations)){
            for(CimProcessOperationDO processOperation : processOperations){
                String hFRPOPO_OBJ = processOperation.getId();
                querySql = "SELECT\n" +
                                "	L.LOT_ID,\n" +
                                "	L. ID\n" +
                                "FROM\n" +
                                "	OMLOT L\n" +
                                "INNER JOIN OMPRFCX P ON P.ID = L.PRFCX_RKEY\n" +
                                "INNER JOIN OMPRFCX_PROPESEQ PP ON PP.REFKEY = P.ID\n" +
                                "WHERE\n" +
                                "	PP.PROPE_RKEY = ?\n" +
                                "AND NOT EXISTS (\n" +
                                "	SELECT\n" +
                                "		1\n" +
                                "	FROM\n" +
                                "		OMLOT M\n" +
                                "	INNER JOIN OMPRFCX Q ON Q. ID = M.PRFCX_RKEY\n" +
                                "	INNER JOIN OMPRFCX_PROPESEQ QQ ON QQ.REFKEY = Q.ID\n" +
                                "	WHERE\n" +
                                "		QQ.PROPE_RKEY = PP.PROPE_RKEY\n" +
                                "	AND M .LOT_ID = L.SPLIT_LOT_ID\n" +
                                ")";

                List<Object[]> allObject = cimJpaRepository.query(querySql, hFRPOPO_OBJ);
                String hFRLOTLOT_ID=null,hFRLOTLOT_OBJ=null;
                if(!CimArrayUtils.isEmpty(allObject)){
                    hFRLOTLOT_ID=(String)allObject.get(0)[0];
                    hFRLOTLOT_OBJ=(String)allObject.get(0)[1];
                }
                if(lotSpecified&&!CimStringUtils.equals(objControlJobProcessOperationListGetDRIn.getLotID().getValue(),hFRLOTLOT_ID))continue;
                Infos.ProcessOperationLot processOperationLot=new Infos.ProcessOperationLot();
                processOperationLots.add(processOperationLot);
                processOperationLot.setPoObj(hFRPOPO_OBJ);
                processOperationLot.setLotID(ObjectIdentifier.build(hFRLOTLOT_ID, hFRLOTLOT_OBJ));
            }
        }

        Validations.check(CimArrayUtils.isEmpty(processOperationLots), retCodeConfig.getNotFoundLotInControlJob());
        return processOperationLots;
    }

    @Override
    public List<Infos.ControlJobCassette> controlJobContainedLotGet(Infos.ObjCommon objCommonIn, ObjectIdentifier controlJobID) {

        //---------------------------
        //   Get controljob Object
        //---------------------------
        CimControlJob controlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        //---------------------------
        //   Get PosStartCassetteInfoSequence Info
        //---------------------------
        List<Infos.ControlJobCassette> controlJobCassettes = new ArrayList<>();
        if (controlJob==null){
            return controlJobCassettes;
        }
        List<ProductDTO.PosStartCassetteInfo> startCassettes = controlJob.getStartCassetteInfo();

        if(CimArrayUtils.isNotEmpty(startCassettes)) {
            for (ProductDTO.PosStartCassetteInfo startCassette : startCassettes) {
                Infos.ControlJobCassette controlJobCassette = new Infos.ControlJobCassette();
                controlJobCassette.setLoadSequenceNumber(startCassette.getLoadSequenceNumber());
                controlJobCassette.setCassetteID(startCassette.getCassetteID());
                controlJobCassette.setLoadPurposeType(new ObjectIdentifier(startCassette.getLoadPurposeType()));
                controlJobCassette.setLoadPortID(startCassette.getLoadPortID());
                controlJobCassette.setUnloadPortID(startCassette.getUnloadPortID());

                List<Infos.ControlJobLot> controlJobLots = new ArrayList<>();
                List<ProductDTO.PosLotInCassetteInfo> lotInCassettes = startCassette.getLotInCassetteInfo();
                controlJobCassettes.add(controlJobCassette);
                if(CimArrayUtils.getSize(lotInCassettes) < 1){
                    continue;
                }

                for (ProductDTO.PosLotInCassetteInfo lotInCassette : lotInCassettes) {
                    Infos.ControlJobLot controlJobLot = new Infos.ControlJobLot();
                    controlJobLot.setOperationStartFlag(lotInCassette.isOperationStartFlag());
                    controlJobLot.setMonitorLotFlag(lotInCassette.isMonitorLotFlag());
                    controlJobLot.setLotID(lotInCassette.getLotID());
                    controlJobLots.add(controlJobLot);
                }
                controlJobCassette.setControlJobLotList(controlJobLots);
            }
        }

        //-----------------------------------------------------------
        //   Add lots which are stored status in SLM operation
        //-----------------------------------------------------------
        CimMachine aMachine = controlJob.getMachine();
        boolean bSLMCapabilityFlag = CimStringUtils.equals(aMachine.getSlmSwitch(),"ON");
        if (CimBooleanUtils.isTrue(bSLMCapabilityFlag)) {
            List<ProductDTO.LotInControlJobInfo> lotInControlJobInfos = controlJob.allControlJobLots();
            if(CimArrayUtils.isNotEmpty(lotInControlJobInfos)) {
                for (ProductDTO.LotInControlJobInfo lotInControlJobInfo : lotInControlJobInfos) {
                    boolean bLotFoundFlag = false;
                    if (CimArrayUtils.isNotEmpty(startCassettes)) {
                        for (ProductDTO.PosStartCassetteInfo startCassette : startCassettes) {
                            List<ProductDTO.PosLotInCassetteInfo> lotInCassetteList = startCassette.getLotInCassetteInfo();
                            if (CimArrayUtils.isNotEmpty(lotInCassetteList)) {
                                for (ProductDTO.PosLotInCassetteInfo lotInCassette : lotInCassetteList) {
                                    if (ObjectIdentifier.equalsWithValue(lotInControlJobInfo.getLotID(), lotInCassette.getLotID())) {
                                        bLotFoundFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (CimBooleanUtils.isTrue(bLotFoundFlag)) {
                                break;
                            }
                        }
                    }
                    if (!bLotFoundFlag) {
                        Infos.ControlJobCassette controlJobCassette = new Infos.ControlJobCassette();
                        controlJobCassette.setLoadSequenceNumber(0L);
                        List<Infos.ControlJobLot> controlJobLotList = new ArrayList<>();
                        Infos.ControlJobLot controlJobLot = new Infos.ControlJobLot();
                        controlJobLot.setOperationStartFlag(true);
                        controlJobLot.setMonitorLotFlag(lotInControlJobInfo.getMonitorLotFlag());
                        controlJobLot.setLotID(lotInControlJobInfo.getLotID());

                        controlJobLotList.add(controlJobLot);
                        controlJobCassette.setControlJobLotList(controlJobLotList);
                        controlJobCassettes.add(controlJobCassette);
                    }
                }
            }
        }
        return controlJobCassettes;
    }

    /**
     * @param objCommon
     * @param controlJobID
     * @return
     * @author ho
     */
    @Override
    public List<Infos.ControlJobLot> controlJobLotListGet(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID) {

        log.info("in-parm's controlJobID {}",controlJobID.getValue());
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        /*---------------------------*/
        /*   Get ControlJob Object   */
        /*---------------------------*/
        CimControlJob controlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        Validations.check(CimObjectUtils.isEmpty(controlJob),retCodeConfig.getNotFoundControlJob());

        /*---------------------------------------*/
        /*   Get Lot List In ControlJob Object   */
        /*---------------------------------------*/
        List<ProductDTO.LotInControlJobInfo> controlJobLots = controlJob.allControlJobLots();
        Validations.check(CimArrayUtils.isEmpty(controlJobLots),new OmCode(retCodeConfig.getNotFoundLotInControlJob(),controlJobID.getValue()));

        List<Infos.ControlJobLot> controlJobLotList = new ArrayList<>();
        for (ProductDTO.LotInControlJobInfo controlJobLot : controlJobLots) {
            Infos.ControlJobLot controlJobLot1 = new Infos.ControlJobLot();
            controlJobLot1.setOperationStartFlag(true);
            controlJobLot1.setMonitorLotFlag(controlJobLot.getMonitorLotFlag());
            controlJobLot1.setLotID(controlJobLot.getLotID());
            controlJobLotList.add(controlJobLot1);
        }
        return controlJobLotList;
    }

    @Override
    public List<ObjectIdentifier> controlJobLotIDListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID) {
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        Validations.check(null == controlJobID, "the controlJob is null");
        CimControlJob controlJobBO = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        Validations.check(null == controlJobBO, retCodeConfig.getNotFoundControlJob());

       // List<CimControlJobCassetteLotDO> cassetteLotList = controlJobCassetteLotDao.findAllByReferenceKeyAndOperationStartFlag(controlJobBO.getPrimaryKey(), true);
        List<ProductDTO.PosStartCassetteInfo> startCassetteInfoList = controlJobBO.getStartCassetteInfo();
        if (!CimObjectUtils.isEmpty(startCassetteInfoList)) {
            for (ProductDTO.PosStartCassetteInfo startCassette : startCassetteInfoList){
                lotIDList = startCassette.getLotInCassetteInfo().stream()
                        .filter(ProductDTO.PosLotInCassetteInfo::isOperationStartFlag)
                        .map(ProductDTO.PosLotInCassetteInfo::getLotID)
                        .collect(Collectors.toList());
            }
        }
        return lotIDList;
    }

    @Override
    public Outputs.ObjControlJobStartReserveInformationOut controlJobStartReserveInformationGet(Infos.ObjCommon objCommon,
                                                                                                ObjectIdentifier controlJobID,
                                                                                                boolean edcItemsNeedFlag) {
        Outputs.ObjControlJobStartReserveInformationOut retVal = new Outputs.ObjControlJobStartReserveInformationOut();
        /*---------------------------*/
        /*                           */
        /*   Get controljob Object   */
        /*                           */
        /*---------------------------*/
        CimControlJob controlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        Validations.check(null == controlJob, retCodeConfig.getNotFoundControlJob());
        /*---------------------------------*/
        /*                                 */
        /*   Get and Set controljob Info   */
        /*                                 */
        /*---------------------------------*/

        /*-------------------------*/
        /*   Get controljob Info   */
        /*-------------------------*/
        List<ProductDTO.PosStartCassetteInfo> startCassetteInfo = Optional.of(controlJob.getStartCassetteInfo())
                .filter(CimArrayUtils::isNotEmpty)
                .orElseGet(ArrayList::new);
        CimMachine machine = controlJob.getMachine();

        //---------------------------------------------- ----------------------------
        //  Get PortGroup from aMachine.
        //  If CJ exists but does not have EQP, it means that EQP is removed from CJ.
        //  So, getting PortGroup and allCassettes are skiped.
        //---------------------------------------------------------------------------
        boolean bCJMachineNil = null == machine ? true : false;
        List<MachineDTO.MachineCassette> strMachineCassetteSeq;
        if (CimBooleanUtils.isFalse(bCJMachineNil)) {
            String portGroup = controlJob.getPortGroup();
            strMachineCassetteSeq = machine.allCassettes();
            /*-----------------------------------------------*/
            /*   Set ControlJob Info into Return Structure   */
            /*-----------------------------------------------*/
            retVal.setPortGroupID(portGroup);
            retVal.setEquipmentID(ObjectIdentifier.build(machine.getIdentifier(), machine.getPrimaryKey()));
        } else {
            strMachineCassetteSeq = new ArrayList<>();
        }

        /* -----------------------------------------------------------------------------------------*/
        // Ceate startCassette from controlJob lot for SLM
        // By SLM function, lot is allowed to be cut relation from startCassette while processing
        // CAUTION: the startCassette could be created with blank cassetteID
        /* -----------------------------------------------------------------------------------------*/
        List<ProductDTO.LotInControlJobInfo> lotInControlJobInfos = controlJob.allControlJobLots();
        int lotInControlJobSize = CimArrayUtils.getSize(lotInControlJobInfos);
        boolean lotNotInCastFlag = false;
        for (int i = 0; i < lotInControlJobSize; i++) {
            lotNotInCastFlag = true;
            for (int j = 0, jLen = startCassetteInfo.size(); j < jLen; j++) {
                for (int k = 0, kLen = startCassetteInfo.get(j).getLotInCassetteInfo().size(); k < kLen; k++) {
                    if (ObjectIdentifier.equalsWithValue(startCassetteInfo.get(j).getLotInCassetteInfo().get(k).getLotID(),
                            lotInControlJobInfos.get(i).getLotID())) {
                        lotNotInCastFlag = false;
                        break;
                    }
                }
                if (!lotNotInCastFlag) {
                    break;
                }
            }
            ProductDTO.PosStartCassetteInfo startCassette = new ProductDTO.PosStartCassetteInfo();
            List<ProductDTO.PosLotInCassetteInfo> lotInCassettes = new ArrayList<>();
            if (lotNotInCastFlag) {
                ProductDTO.PosLotInCassetteInfo lotInCassette = new ProductDTO.PosLotInCassetteInfo();
                lotInCassette.setOperationStartFlag(true);
                lotInCassette.setLotID(lotInControlJobInfos.get(i).getLotID());
                lotInCassette.setMonitorLotFlag(lotInControlJobInfos.get(i).getMonitorLotFlag());
                lotInCassettes.add(lotInCassette);
                startCassette.setLotInCassetteInfo(lotInCassettes);
                startCassetteInfo.add(startCassette);
            }
        }
        List<Infos.StartCassette> startCassetteList = new ArrayList<>();
        int castLen = CimArrayUtils.getSize(startCassetteInfo);
        for (int i = 0; i < castLen; i++) {
            Infos.StartCassette startCassette = new Infos.StartCassette();
            final ProductDTO.PosStartCassetteInfo posStartCassetteInfo = startCassetteInfo.get(i);
            startCassette.setLoadSequenceNumber(posStartCassetteInfo.getLoadSequenceNumber());
            startCassette.setCassetteID(posStartCassetteInfo.getCassetteID());
            startCassette.setLoadPurposeType(posStartCassetteInfo.getLoadPurposeType());
            startCassette.setLoadPortID(posStartCassetteInfo.getLoadPortID());
            startCassette.setUnloadPortID(posStartCassetteInfo.getUnloadPortID());
            if (!bCJMachineNil) {
                for (MachineDTO.MachineCassette machineCassette : strMachineCassetteSeq) {
                    if (ObjectIdentifier.equalsWithValue(machineCassette.getCassetteID(),
                            posStartCassetteInfo.getCassetteID())) {
                        startCassette.setUnloadPortID(machineCassette.getUnloadPortID());
                        break;
                    }
                }
            }
            List<Infos.LotInCassette> lotInCassettes = new ArrayList<>();
            for (int j = 0, jLen = CimArrayUtils.getSize(posStartCassetteInfo.getLotInCassetteInfo()); j < jLen; j++) {
                Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
                final ProductDTO.PosLotInCassetteInfo lotInCassetteInfo = posStartCassetteInfo
                        .getLotInCassetteInfo().get(j);
                lotInCassette.setMoveInFlag(lotInCassetteInfo.isOperationStartFlag());
                lotInCassette.setMonitorLotFlag(lotInCassetteInfo.isMonitorLotFlag());
                lotInCassette.setLotID(lotInCassetteInfo.getLotID());
                lotInCassette.setFurnaceSpecificControl(lotInCassetteInfo.getProcessSpecificControl());
                List<Infos.LotWaferAttributes> pLotWaferAttributesSequence = new ArrayList<>();
                if ("OEQPW012".equals(objCommon.getTransactionID())
                        || "OEQPW024".equals(objCommon.getTransactionID())) {
                    pLotWaferAttributesSequence = lotMethod.lotMaterialsGetWafers(objCommon, lotInCassetteInfo.getLotID());
                } else {
                    Inputs.ObjLotWafersGetIn objLotWafersGetIn = new Inputs.ObjLotWafersGetIn();
                    objLotWafersGetIn.setLotID(lotInCassetteInfo.getLotID());
                    objLotWafersGetIn.setScrapCheckFlag(true);
                    List<Infos.LotWaferInfoAttributes> lotWaferInfoAttributesList =
                            lotMethod.lotWaferInfoListGetDR(objCommon, objLotWafersGetIn);
                    for (Infos.LotWaferInfoAttributes waferInfoAttributes : lotWaferInfoAttributesList) {
                        Infos.LotWaferAttributes lotWaferAttributes = new Infos.LotWaferAttributes();
                        lotWaferAttributes.setWaferID(waferInfoAttributes.getWaferID());
                        lotWaferAttributes.setCassetteID(waferInfoAttributes.getCassetteID());
                        lotWaferAttributes.setAliasWaferName(waferInfoAttributes.getAliasWaferName());
                        lotWaferAttributes.setSlotNumber(waferInfoAttributes.getSlotNumber());
                        lotWaferAttributes.setProductID(waferInfoAttributes.getProductID());
                        lotWaferAttributes.setGrossUnitCount(waferInfoAttributes.getGrossUnitCount());
                        lotWaferAttributes.setGoodUnitCount(waferInfoAttributes.getGoodUnitCount());
                        lotWaferAttributes.setRepairUnitCount(waferInfoAttributes.getRepairUnitCount());
                        lotWaferAttributes.setFailUnitCount(waferInfoAttributes.getFailUnitCount());
                        lotWaferAttributes.setControlWaferFlag(waferInfoAttributes.getControlWaferFlag());
                        lotWaferAttributes.setSTBAllocFlag(waferInfoAttributes.getSTBAllocFlag());
                        lotWaferAttributes.setReworkCount(0);
                        lotWaferAttributes.setEqpMonitorUsedCount(waferInfoAttributes.getEqpMonitorUsedCount());
                        pLotWaferAttributesSequence.add(lotWaferAttributes);
                    }
                }

                if (0 < CimArrayUtils.getSize(pLotWaferAttributesSequence)) {
                    List<Infos.LotWafer> lotWaferList = new ArrayList<>();
                    for (Infos.LotWaferAttributes lotWaferAttributes : pLotWaferAttributesSequence) {
                        Infos.LotWafer lotWafer = new Infos.LotWafer();
                        lotWafer.setWaferID(lotWaferAttributes.getWaferID());
                        lotWafer.setSlotNumber(lotWaferAttributes.getSlotNumber().longValue());
                        lotWafer.setControlWaferFlag(lotWaferAttributes.getControlWaferFlag());
                        lotWafer.setStartRecipeParameterList(new ArrayList<>());
                        lotWaferList.add(lotWafer);
                    }
                    lotInCassette.setLotWaferList(lotWaferList);
                    lotInCassette.setProductID(pLotWaferAttributesSequence.get(0).getProductID());

                    if (CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {

                        CimLot lot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());

                        //QianDao add MES-EAP Integration add maskLevel flag in startCassetteList
                        CimProcessOperation processOperation = lot.getProcessOperation();
                        Validations.check(null == processOperation, retCodeConfig.getNotFoundProcessOperation(), "****");
                        String photoLayer = processOperation.getPhotoLayer();

                        //Qiandao add MES_EAP Integration add technologyID and productGroupID start
                        CimProductSpecification productSpecification = lot.getProductSpecification();
                        Validations.check(null == productSpecification, retCodeConfig.getNotFoundProductSpec(), "******");
                        CimProductGroup productGroup = productSpecification.getProductGroup();
                        Validations.check(null == productGroup, retCodeConfig.getNotFoundProductGroup(), "******");
                        CimTechnology technology = productGroup.getTechnology();
                        Validations.check(null == technology, retCodeConfig.getNotFoundTechnology(), "******");
                        lotInCassette.setProductGroupID(ObjectIdentifier.build(productGroup.getIdentifier(), productGroup.getPrimaryKey()));
                        lotInCassette.setTechnologyID(ObjectIdentifier.build(technology.getIdentifier(), technology.getPrimaryKey()));
                        //Qiandao add MES_EAP Integration add technologyID and productGroupID end

                        Outputs.ObjLotCheckConditionForPOByControlJobOut checkConditionForPOByControlJobOut =
                                lotMethod.lotCheckConditionForPOByControlJob(objCommon, lotInCassette.getLotID(), controlJobID);

                        CimProcessOperation aPosPO = null;
                        if (CimBooleanUtils.isTrue(checkConditionForPOByControlJobOut.getCurrentPOFlag())) {
                            aPosPO = lot.getProcessOperation();
                        } else {
                            aPosPO = lot.getPreviousProcessOperation();

                        }
                        Validations.check(null == aPosPO, new OmCode(retCodeConfig.getNotFoundProcessOperation(), "", lotInCassetteInfo.getLotID().getValue()));

                        ProcessDTO.ActualStartInformationForPO actualStartInfo = aPosPO.getActualStartInfo(edcItemsNeedFlag);
                        CimProcessDefinition aMainPD = aPosPO.getMainProcessDefinition();
                        Validations.check(null == aMainPD, new OmCode(retCodeConfig.getNotFoundRoute(), ""));

                        Infos.StartOperationInfo startOperationInfo = new Infos.StartOperationInfo();
                        startOperationInfo.setProcessFlowID(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
                        CimProcessDefinition aPD = aPosPO.getProcessDefinition();
                        Validations.check(null == aPD, new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ""));

                        startOperationInfo.setOperationID(ObjectIdentifier.build(aPD.getIdentifier(), aPD.getPrimaryKey()));
                        startOperationInfo.setOperationNumber(aPosPO.getOperationNumber());
                        startOperationInfo.setPassCount(aPosPO.getPassCount().intValue());
                        startOperationInfo.setMaskLevel(photoLayer);
                        lotInCassette.setStartOperationInfo(startOperationInfo);

                        lotInCassette.setLotType(lot.getLotType());
                        lotInCassette.setSubLotType(lot.getSubLotType());
                        //------------------------------------------
                        // Get sampled waferIDs on PO
                        //------------------------------------------
                        List<String> assignedSamplingWafers = actualStartInfo.getAssignedSamplingWafers();
                        if (CimArrayUtils.isNotEmpty(assignedSamplingWafers)) {
                            for (int k = 0, kLen = CimArrayUtils.getSize(lotWaferList); k < kLen; k++) {
                                lotWaferList.get(k).setProcessJobExecFlag(false);
                            }
                            Boolean lackWaferFlag = false;
                            Boolean matchWaferFlag = false;
                            for (int k = 0, kLen = assignedSamplingWafers.size(); k < kLen; k++) {
                                matchWaferFlag = false;
                                for (int l = 0, lLen = CimArrayUtils.getSize(lotWaferList); l < lLen; l++) {
                                    if (ObjectIdentifier.equalsWithValue(assignedSamplingWafers.get(k),
                                            lotWaferList.get(l).getWaferID())) {
                                        lotWaferList.get(l).setProcessJobExecFlag(true);
                                        matchWaferFlag = true;
                                        break;
                                    }
                                }
                                if (!matchWaferFlag) {
                                    lackWaferFlag = true;
                                    break;
                                }
                            }
                            Validations.check(lackWaferFlag, retCodeConfig.getLackOfSmplWafer());
                        } else {
                            for (int k = 0, kLen = lotWaferList.size(); k < kLen; k++) {
                                lotWaferList.get(k).setProcessJobExecFlag(true);
                            }
                        }
                        lotInCassette.setRecipeParameterChangeType(actualStartInfo.getAssignedRecipeParameterChangeType());

                        Infos.StartRecipe startRecipe = new Infos.StartRecipe();
                        startRecipe.setLogicalRecipeID(actualStartInfo.getAssignedLogicalRecipe());
                        startRecipe.setMachineRecipeID(actualStartInfo.getAssignedMachineRecipe());
                        startRecipe.setPhysicalRecipeID(actualStartInfo.getAssignedPhysicalRecipe());
                        startRecipe.setDataCollectionFlag(actualStartInfo.getAssignedDataCollectionFlag());

                        List<Infos.StartReticleInfo> startReticleList = new ArrayList<>();
                        for (int rr = 0, rrLen = CimArrayUtils.getSize(actualStartInfo.getAssignedReticles()); rr < rrLen; rr++) {
                            Infos.StartReticleInfo startReticle = new Infos.StartReticleInfo();
                            startReticle.setSequenceNumber(actualStartInfo.getAssignedReticles().get(rr).getSequenceNumber());
                            startReticle.setReticleID(actualStartInfo.getAssignedReticles().get(rr).getReticleID());
                            startReticleList.add(startReticle);
                        }
                        startRecipe.setStartReticleList(startReticleList);

                        List<Infos.StartFixtureInfo> startFixtures = new ArrayList<>();
                        int actualStartInfoLength = CimArrayUtils.getSize(actualStartInfo.getAssignedFixtures());
                        for (int ff = 0; ff < actualStartInfoLength; ff++) {
                            Infos.StartFixtureInfo startFixture = new Infos.StartFixtureInfo();
                            startFixture.setFixtureCategory(actualStartInfo.getAssignedFixtures().get(ff).getFixtureCategory());
                            startFixture.setFixtureID(actualStartInfo.getAssignedFixtures().get(ff).getFixtureID());
                            startFixtures.add(startFixture);
                        }
                        startRecipe.setStartFixtureList(startFixtures);

                        List<ProcessDTO.StartRecipeParameterSetInfo> assignedRecipeParameterSetList = actualStartInfo.getAssignedRecipeParameterSets();
                        int assignedRecipeParameterSetListSize = CimArrayUtils.getSize(assignedRecipeParameterSetList);
                        if (1 == assignedRecipeParameterSetListSize) {
                            if (1 == CimArrayUtils.getSize(assignedRecipeParameterSetList.get(0).getRecipeParameterList())) {
                                actualStartInfo.setAssignedRecipeParameterSets(null);
                            }
                        }

                        if (0 < assignedRecipeParameterSetListSize) {
                            for (int w = 0, wLen = CimArrayUtils.getSize(pLotWaferAttributesSequence); w < wLen; w++) {
                                if (BizConstant.SP_RPARM_CHANGETYPE_BYLOT.equals(actualStartInfo.getAssignedRecipeParameterChangeType())) {
                                    List<Infos.StartRecipeParameter> startRecipeParameters = new ArrayList<>();
                                    final int lLen = CimArrayUtils.getSize(assignedRecipeParameterSetList.get(0).getRecipeParameterList());
                                    for (int l = 0; l < lLen; l++) {
                                        Infos.StartRecipeParameter startRecipeParameter = new Infos.StartRecipeParameter();
                                        final ProcessDTO.StartRecipeParameter recipeParameter = assignedRecipeParameterSetList.get(0)
                                                .getRecipeParameterList().get(l);
                                        startRecipeParameter.setParameterName(recipeParameter.getParameterName());
                                        startRecipeParameter.setParameterValue(recipeParameter.getParameterValue());
                                        startRecipeParameter.setTargetValue(recipeParameter.getTargetValue());
                                        startRecipeParameter.setUseCurrentSettingValueFlag(recipeParameter.getUseCurrentSettingValueFlag());
                                        startRecipeParameters.add(startRecipeParameter);
                                    }
                                    lotWaferList.get(w).setStartRecipeParameterList(startRecipeParameters);
                                } else {
                                    for (int rs = 0; rs < assignedRecipeParameterSetListSize; rs++) {
                                        final List<ProcessDTO.ParameterApplyWaferInfo> applyWaferInfoList =
                                                assignedRecipeParameterSetList.get(rs).getApplyWaferInfoList();
                                        if (CimArrayUtils.isEmpty(applyWaferInfoList)) {
                                            continue;
                                        }
                                        if (lotWaferList.get(w).getWaferID().equals(applyWaferInfoList.get(0).getWaferID())) {
                                            List<Infos.StartRecipeParameter> startRecipeParameters = new ArrayList<>();
                                            final List<ProcessDTO.StartRecipeParameter> recipeParameterList = assignedRecipeParameterSetList
                                                    .get(rs).getRecipeParameterList();
                                            for (int m = 0, mLen = recipeParameterList.size(); m < mLen; m++) {
                                                Infos.StartRecipeParameter startRecipeParameter = new Infos.StartRecipeParameter();
                                                final ProcessDTO.StartRecipeParameter recipeParameter = recipeParameterList.get(m);
                                                startRecipeParameter.setParameterName(recipeParameter.getParameterName());
                                                startRecipeParameter.setParameterValue(recipeParameter.getParameterValue());
                                                startRecipeParameter.setTargetValue(recipeParameter.getTargetValue());
                                                startRecipeParameter.setUseCurrentSettingValueFlag(recipeParameter.getUseCurrentSettingValueFlag());
                                                startRecipeParameters.add(startRecipeParameter);
                                            }
                                            lotWaferList.get(w).setStartRecipeParameterList(startRecipeParameters);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        lotInCassette.setLotWaferList(lotWaferList);

                        // zqi: EDC性能优化
                        // LotsMoveInInq 现在不需要获取EDC的信息
                        if (CimBooleanUtils.isTrue(actualStartInfo.getAssignedDataCollectionFlag())
                                && CimBooleanUtils.isTrue(edcItemsNeedFlag) // EDC性能优化
                        ) {
                            List<Infos.DataCollectionInfo> dcDefs = new ArrayList<>();
                            final int dcLen = CimArrayUtils.getSize(actualStartInfo.getAssignedDataCollections());
                            for (int dc = 0; dc < dcLen; dc++) {
                                Infos.DataCollectionInfo dcDef = new Infos.DataCollectionInfo();
                                final ProcessDTO.DataCollectionInfo dataCollectionInfo = actualStartInfo.getAssignedDataCollections().get(dc);
                                dcDef.setDataCollectionDefinitionID(dataCollectionInfo.getDataCollectionDefinitionID());
                                dcDef.setDescription(dataCollectionInfo.getDescription());
                                dcDef.setDataCollectionType(dataCollectionInfo.getDataCollectionType());
                                dcDef.setCalculationRequiredFlag(dataCollectionInfo.getCalculationRequiredFlag());
                                dcDef.setSpecCheckRequiredFlag(dataCollectionInfo.getSpecCheckRequiredFlag());
                                dcDef.setDataCollectionSpecificationID(dataCollectionInfo.getDataCollectionSpecificationID());
                                dcDef.setDcSpecDescription(dataCollectionInfo.getDcSpecDescription());
                                dcDef.setPreviousDataCollectionDefinitionID(dataCollectionInfo.getPreviousDataCollectionDefinitionID());
                                dcDef.setPreviousOperationID(dataCollectionInfo.getPreviousOperationID());
                                dcDef.setPreviousOperationNumber(dataCollectionInfo.getPreviousOperationNumber());
                                dcDef.setEdcSettingType(dataCollectionInfo.getEdcSettingType());
                                List<Infos.DataCollectionItemInfo> dcItems = new ArrayList<>();
                                final int itemLen = CimArrayUtils.getSize(dataCollectionInfo.getDcItems());
                                for (int l = 0; l < itemLen; l++) {
                                    Infos.DataCollectionItemInfo dcItem = new Infos.DataCollectionItemInfo();
                                    final ProcessDTO.DataCollectionItemInfo itemInfo = dataCollectionInfo.getDcItems().get(l);
                                    dcItem.setDataCollectionItemName(itemInfo.getDataCollectionItemName());
                                    dcItem.setDataCollectionMode(itemInfo.getDataCollectionMode());
                                    dcItem.setDataCollectionUnit(itemInfo.getDataCollectionUnit());
                                    dcItem.setDataType(itemInfo.getDataType());
                                    dcItem.setItemType(itemInfo.getItemType());
                                    dcItem.setMeasurementType(itemInfo.getMeasurementType());
                                    dcItem.setWaferID(itemInfo.getWaferID());
                                    dcItem.setWaferPosition(itemInfo.getWaferPosition());
                                    dcItem.setSitePosition(itemInfo.getSitePosition());
                                    dcItem.setHistoryRequiredFlag(itemInfo.getHistoryRequiredFlag());
                                    dcItem.setCalculationType(itemInfo.getCalculationType());
                                    dcItem.setCalculationExpression(itemInfo.getCalculationExpression());
                                    dcItem.setDataValue(itemInfo.getDataValue());
                                    dcItem.setTargetValue(itemInfo.getTargetValue());
                                    dcItem.setSpecCheckResult(itemInfo.getSpecCheckResult());

                                    dcItem.setWaferCount(itemInfo.getWaferCount());
                                    dcItem.setSiteCount(itemInfo.getSiteCount());

                                    dcItem.setActionCodes(itemInfo.getActionCodes());
                                    dcItems.add(dcItem);
                                }
                                List<Infos.DataCollectionSpecInfo> dcSpecs = new ArrayList<>();
                                final int specLen = CimArrayUtils.getSize(dataCollectionInfo.getDcSpecs());
                                for (int l = 0; l < specLen; l++) {
                                    Infos.DataCollectionSpecInfo dcSpec = new Infos.DataCollectionSpecInfo();
                                    final ProcessDTO.DataCollectionSpecInfo specInfo = dataCollectionInfo.getDcSpecs().get(l);
                                    dcSpec.setDataItemName(specInfo.getDataItemName());
                                    dcSpec.setScreenLimitLowerRequired(specInfo.getScreenLimitLowerRequired());
                                    dcSpec.setScreenLimitUpperRequired(specInfo.getScreenLimitUpperRequired());
                                    dcSpec.setScreenLimitUpper(specInfo.getScreenLimitUpper());
                                    dcSpec.setActionCodesUscrn(specInfo.getActionCodesUscrn());
                                    dcSpec.setSpecLimitLowerRequired(specInfo.getSpecLimitLowerRequired());
                                    dcSpec.setScreenLimitLower(specInfo.getScreenLimitLower());
                                    dcSpec.setActionCodesLscrn(specInfo.getActionCodesLscrn());
                                    dcSpec.setSpecLimitUpperRequired(specInfo.getSpecLimitUpperRequired());
                                    dcSpec.setSpecLimitUpper(specInfo.getSpecLimitUpper());
                                    dcSpec.setActionCodesUsl(specInfo.getActionCodesUsl());
                                    dcSpec.setSpecLimitLowerRequired(specInfo.getSpecLimitLowerRequired());
                                    dcSpec.setSpecLimitLower(specInfo.getSpecLimitLower());
                                    dcSpec.setActionCodesLsl(specInfo.getActionCodesLsl());
                                    dcSpec.setControlLimitUpperRequired(specInfo.getControlLimitUpperRequired());
                                    dcSpec.setControlLimitUpper(specInfo.getControlLimitUpper());
                                    dcSpec.setActionCodesUcl(specInfo.getActionCodesUcl());
                                    dcSpec.setControlLimitLowerRequired(specInfo.getControlLimitLowerRequired());
                                    dcSpec.setControlLimitLower(specInfo.getControlLimitLower());
                                    dcSpec.setActionCodesLcl(specInfo.getActionCodesLcl());
                                    dcSpec.setTarget(specInfo.getTarget());
                                    dcSpec.setTag(specInfo.getTag());
                                    dcSpecs.add(dcSpec);
                                }
                                dcDef.setDcSpecs(dcSpecs);
                                dcDef.setDcItems(dcItems);
                                dcDefs.add(dcDef);
                            }
                            startRecipe.setDcDefList(dcDefs);
                        }

                        lotInCassette.setStartRecipe(startRecipe);
                    } else {
                        lotInCassette.setStartRecipe(new Infos.StartRecipe());
                    }
                }
                lotInCassettes.add(lotInCassette);
            }
            startCassette.setLotInCassetteList(lotInCassettes);
            startCassetteList.add(startCassette);
        }
        if ("ODISW002".equals(objCommon.getTransactionID())
                || "ODISW004".equals(objCommon.getTransactionID())) {
            Boolean bNeedToAssignOpeStaFlag = true;
            for (int i = 0, iLen = startCassetteList.size(); i < iLen; i++) {
                for (int j = 0, jLen = startCassetteList.get(i).getLotInCassetteList().size(); j < jLen; j++) {
                    if (startCassetteList.get(i).getLotInCassetteList().get(j).getMoveInFlag()) {
                        bNeedToAssignOpeStaFlag = false;
                        break;
                    }
                }
                if (!bNeedToAssignOpeStaFlag) {
                    break;
                }
            }
            if (bNeedToAssignOpeStaFlag) {
                for (int i = 0, iLen = startCassetteList.size(); i < iLen; i++) {
                    for (int j = 0, jLen = startCassetteList.get(i).getLotInCassetteList().size(); j < jLen; j++) {
                        ObjectIdentifier lotControlJobIDResult = lotMethod.lotControlJobIDGet(objCommon,
                                startCassetteList.get(i).getLotInCassetteList().get(j).getLotID());
                        if (!ObjectIdentifier.isEmpty(lotControlJobIDResult)) {
                            startCassetteList.get(i).getLotInCassetteList().get(j).setMoveInFlag(true);
                        }
                    }
                }

            }
        }
        retVal.setStartCassetteList(startCassetteList);
        return retVal;
    }

    @Override
    public void controlJobStatusChange(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, String controlJobStatus) {
        log.debug("controlJobStatusChange(): enter controlJobStatusChange");
        CimControlJob aControlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        Validations.check(aControlJob == null, retCodeConfig.getNotFoundControlJob());
        aControlJob.setControlJobStatus(controlJobStatus);
        aControlJob.setLastClaimedUserID(objCommon.getUser().getUserID().getValue());
        aControlJob.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
    }

    @Override
    public void controlJobDelete(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, String controlJobAction) {
        log.debug("controlJobDelete(): enter controlJobDelete");
        if (CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE)){
            log.info("CJ Action Type is Delete");
            /*---------------------------*/
            /*   Get ControlJob Object   */
            /*---------------------------*/
            CimControlJob aControlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
            Validations.check(null == aControlJob, retCodeConfig.getNotFoundControlJob());
            CimMachine aMachine = aControlJob.getMachine();

            Validations.check(null == aMachine, new OmCode(retCodeConfig.getNotFoundEqp(), "****"));
            /*-------------------------------------------*/
            /*   Get PosStartCassetteInfoSequence Info   */
            /*-------------------------------------------*/
            List<ProductDTO.PosStartCassetteInfo> startCassetteInfo = aControlJob.getStartCassetteInfo();
            for (ProductDTO.PosStartCassetteInfo startCassette : startCassetteInfo){
                /*-------------------------*/
                /*   Get Cassette Object   */
                /*-------------------------*/
                com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, startCassette.getCassetteID());
                /*------------------------------------*/
                /*   Clear ControlJobID of Cassette   */
                /*------------------------------------*/
                aCassette.setControlJob(null);
                List<ProductDTO.PosLotInCassetteInfo> lotInCassetteList = startCassette.getLotInCassetteInfo();
                for (ProductDTO.PosLotInCassetteInfo lotInCassette : lotInCassetteList){
                    /*------------------------*/
                    /*   Omit Not Start Not   */
                    /*------------------------*/
                    if (!lotInCassette.isOperationStartFlag()){
                        continue;
                    }
                    /*--------------------*/
                    /*   Get Lot Object   */
                    /*--------------------*/
                    com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotInCassette.getLotID());
                    /*-------------------------------*/
                    /*   Clear ControlJobID of Lot   */
                    /*-------------------------------*/
                    aLot.setControlJob(null);

                }
                /*-------------------------------*/
                /*   Clear ControlJobID of Lot   */
                /*-------------------------------*/
                List<BufferResource> aBufferResourceList = aMachine.allBufferResources();
                if (!CimObjectUtils.isEmpty(aBufferResourceList)){
                    for (BufferResource bufferResource : aBufferResourceList){
                        Validations.check(null == bufferResource, retCodeConfig.getNotFoundBufferResource());
                        List<MaterialLocation> aMaterialLocationList = bufferResource.allMaterialLocations();
                        for (MaterialLocation materialLocation : aMaterialLocationList){
                            com.fa.cim.newcore.bo.machine.CimMaterialLocation aMaterialLocation = (com.fa.cim.newcore.bo.machine.CimMaterialLocation) materialLocation;
                            Validations.check(null == aMaterialLocation, retCodeConfig.getNotFoundMaterialLocation());
                            ObjectIdentifier containedCassetteID = null;
                            ObjectIdentifier allocatedCassetteID = null;
                            Material aMaterialLocationCassette = aMaterialLocation.getAllocatedMaterial();
                            if (aMaterialLocationCassette == null){
                                Material aContainedMaterial = aMaterialLocation.getMaterial();
                                if (aContainedMaterial != null){
                                    containedCassetteID = new ObjectIdentifier(aContainedMaterial.getIdentifier(), aContainedMaterial.getPrimaryKey());
                                }
                            } else {
                                //--- Get allocatedCassette
                                allocatedCassetteID = new ObjectIdentifier(aMaterialLocationCassette.getIdentifier(), aMaterialLocationCassette.getPrimaryKey());
                            }
                            if (!ObjectIdentifier.equalsWithValue(startCassette.getCassetteID(), containedCassetteID)
                                    && !ObjectIdentifier.equalsWithValue(startCassette.getCassetteID(), allocatedCassetteID)){
                                continue;
                            }
                            //---   Check Control Job of Material Location
                            com.fa.cim.newcore.bo.product.CimControlJob aMaterialLocationControlJob = aMaterialLocation.getControlJob();
                            if (aMaterialLocationControlJob == null){
                                continue;
                            }
                            String materialControlJobIdent = aMaterialLocationControlJob.getIdentifier();
                            if (!CimStringUtils.equals(controlJobID.getValue(), materialControlJobIdent)){
                                //---  If controlJobID is differrent, try next one
                                continue;
                            }
                            /*---------------------------------------------*/
                            /*   Delete controlJob of MaterialLocation     */
                            /*---------------------------------------------*/
                            aMaterialLocation.setControlJob(null);
                        }
                    }
                }
            }
            /*----------------------------------*/
            /*   Clear ControlJobID of Reticle  */
            /*----------------------------------*/
            List<ObjectIdentifier> reticleIDList = this.controlJobRelatedReticlesGetDR(objCommon, controlJobID);
            for (ObjectIdentifier reticleID : reticleIDList){
                CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
                com.fa.cim.newcore.bo.product.CimControlJob aRtclCJ = aReticle.findReservedControlJobNamed(controlJobID.getValue());
                if (aRtclCJ != null){
                    aReticle.removeReservedControlJob(aRtclCJ);
                }
            }
            /*-----------------------*/
            /*   Delete ControlJob   */
            /*-----------------------*/
            productManager.removeControlJob(aControlJob);
        } else if (CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_EQP)){
            log.info("CJ Action Type is Delete From EQP");
            //---------------------------//
            //   Get ControlJob Object   //
            //---------------------------//
            com.fa.cim.newcore.bo.product.CimControlJob aControlJob = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimControlJob.class, controlJobID);
            Validations.check(null == aControlJob, retCodeConfig.getNotFoundControlJob());
            //--------------------------//
            //   Get Machine Object     //
            //--------------------------//
            CimMachine aMachine = aControlJob.getMachine();
            Validations.check(null == aMachine, new OmCode(retCodeConfig.getNotFoundEqp(), "****"));
            //-------------------------------------------//
            //   Get PosStartCassetteInfoSequence Info   //
            //-------------------------------------------//
            List<ProductDTO.PosStartCassetteInfo> startCassetteInfo = aControlJob.getStartCassetteInfo();
            for (ProductDTO.PosStartCassetteInfo startCassette : startCassetteInfo){
                //-------------------------------//
                //   Clear ControlJobID of EQP   //
                //-------------------------------//
                List<BufferResource> aBufferResourceList = aMachine.allBufferResources();
                if (!CimObjectUtils.isEmpty(aBufferResourceList)){
                    for (BufferResource bufferResource : aBufferResourceList){
                        Validations.check(null == bufferResource, retCodeConfig.getNotFoundBufferResource());
                        List<MaterialLocation> aMaterialLocationList = bufferResource.allMaterialLocations();
                        for (MaterialLocation materialLocation : aMaterialLocationList){
                            com.fa.cim.newcore.bo.machine.CimMaterialLocation aMaterialLocation = (com.fa.cim.newcore.bo.machine.CimMaterialLocation) materialLocation;
                            Validations.check(null == aMaterialLocation, retCodeConfig.getNotFoundMaterialLocation());
                            //---   Check CassetteID for Material Location
                            ObjectIdentifier containedCassetteID = null;
                            ObjectIdentifier allocatedCassetteID = null;
                            Material aMaterialLocationCassette = aMaterialLocation.getAllocatedMaterial();
                            if (aMaterialLocationCassette == null){
                                //--- Get containedCassette
                                Material aContainedMaterial = aMaterialLocation.getMaterial();
                                if (aContainedMaterial != null){
                                    containedCassetteID = new ObjectIdentifier(aContainedMaterial.getIdentifier(), aContainedMaterial.getPrimaryKey());
                                }
                            } else {
                                //--- Get allocatedCassette
                                allocatedCassetteID = new ObjectIdentifier(aMaterialLocationCassette.getIdentifier(), aMaterialLocationCassette.getPrimaryKey());
                            }
                            if (!ObjectIdentifier.equalsWithValue(startCassette.getCassetteID(), containedCassetteID)
                                    && !ObjectIdentifier.equalsWithValue(startCassette.getCassetteID(), allocatedCassetteID)){
                                continue;
                            }
                            //---   Check Control Job of Material Location
                            com.fa.cim.newcore.bo.product.CimControlJob aMaterialLocationControlJob = aMaterialLocation.getControlJob();
                            if (aMaterialLocationControlJob == null){
                                continue;
                            }
                            String materialControlJobIdent = aMaterialLocationControlJob.getIdentifier();
                            if (!CimStringUtils.equals(controlJobID.getValue(), materialControlJobIdent)){
                                continue;
                            }
                            //---------------------------------------------//
                            //   Delete controlJob of MaterialLocation     //
                            //---------------------------------------------//
                            aMaterialLocation.setControlJob(null);
                        }
                    }
                }
            }
            //------------------------------------//
            //   Clear ControlJob of Equipment    //
            //------------------------------------//
            aControlJob.setMachine(null);
            /*----------------------------------*/
            /*   Clear ControlJobID of Reticle  */
            /*----------------------------------*/
            List<ObjectIdentifier> reticleIDList =this.controlJobRelatedReticlesGetDR(objCommon, controlJobID);
            for (ObjectIdentifier reticleID : reticleIDList){
                CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
                com.fa.cim.newcore.bo.product.CimControlJob aRtclCJ = aReticle.findReservedControlJobNamed(controlJobID.getValue());
                if (aRtclCJ != null){
                    aReticle.removeReservedControlJob(aRtclCJ);
                }
            }
        } else if (CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_LOTANDCASSETTE)){
            log.info("CJ Action Type is Delete From Lot And Cassette");
            //---------------------------//
            //   Get ControlJob Object   //
            //---------------------------//
            com.fa.cim.newcore.bo.product.CimControlJob aControlJob = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimControlJob.class, controlJobID);
            Validations.check(null == aControlJob, retCodeConfig.getNotFoundControlJob());
            //-------------------------------------------//
            //   Get PosStartCassetteInfoSequence Info   //
            //-------------------------------------------//
            List<ProductDTO.PosStartCassetteInfo> startCassetteInfo = aControlJob.getStartCassetteInfo();
            for (ProductDTO.PosStartCassetteInfo startCassette : startCassetteInfo){
                //-------------------------//
                //   Get Cassette Object   //
                //-------------------------//
                com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, startCassette.getCassetteID());
                //------------------------------------//
                //   Clear ControlJobID of Cassette   //
                //------------------------------------//
                aCassette.setControlJob(null);
                List<ProductDTO.PosLotInCassetteInfo> lotInCassetteList = startCassette.getLotInCassetteInfo();
                for (ProductDTO.PosLotInCassetteInfo lotInCassette : lotInCassetteList){
                    //--------------------//
                    //   Get Lot Object   //
                    //--------------------//
                    com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotInCassette.getLotID());
                    //-------------------------------//
                    //   Clear ControlJobID of Lot   //
                    //-------------------------------//
                    aLot.setControlJob(null);
                }
            }
            //-----------------------//
            //   Delete ControlJob   //
            //-----------------------//
            productManager.removeControlJob(aControlJob);
        } else {
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }
        log.debug("controlJobDelete(): exit controlJobDelete");
        return;
    }

    @Override
    public ObjectIdentifier controlJobCreate(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, List<Infos.StartCassette> startCassetteList) {
        log.debug("controlJobCreate(): enter controlJobCreate");

        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(aMachine == null, retCodeConfig.getNotFoundEqp(), objCommon.getTransactionID());
        String newControlJobId = aMachine.getNextControlJobIdentifier();
        //Create controljob
       // CimControlJobDO newControlJob = controlJobCore.controlJobCreate(newControlJobId);
        CimControlJob aControlJob = productManager.createControlJob(newControlJobId);
        Validations.check(aControlJob==null ,retCodeConfig.getNotFoundControlJob());

        //Set eqp ID and port Group to Control Job
        aControlJob.setMachine(aMachine);
        aControlJob.setOwner(baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID()));
        aControlJob.setPortGroup(portGroupID);
        //Set Start cassette to Control Job
        Validations.check(CimArrayUtils.isEmpty(startCassetteList), "startCassetteList can not be empty");

        /*-------------------------------------------------*/
        /*   Prepare Start Cassette Info for Control Job   */
        /*-------------------------------------------------*/
        List<ProductDTO.PosStartCassetteInfo> startCassetteInfoList = new ArrayList<>();
        for (Infos.StartCassette startCassette : startCassetteList){
            ProductDTO.PosStartCassetteInfo startCassetteInfo = new ProductDTO.PosStartCassetteInfo();
            startCassetteInfoList.add(startCassetteInfo);
            startCassetteInfo.setLoadSequenceNumber(startCassette.getLoadSequenceNumber());
            startCassetteInfo.setCassetteID(startCassette.getCassetteID());
            startCassetteInfo.setLoadPurposeType(startCassette.getLoadPurposeType());
            startCassetteInfo.setLoadPortID(startCassette.getLoadPortID());
            startCassetteInfo.setUnloadPortID(startCassette.getUnloadPortID());
            List<ProductDTO.PosLotInCassetteInfo> lotInCassetteInfoList = new ArrayList<>();
            startCassetteInfo.setLotInCassetteInfo(lotInCassetteInfoList);
            if (!ObjectIdentifier.isEmptyWithValue(startCassette.getUnloadPortID())){
                startCassetteInfo.setUnloadPortID(startCassette.getUnloadPortID());
            } else {
                if (!ObjectIdentifier.isEmptyWithValue(startCassette.getLoadPortID())) {
                    CimPortResource aLoadPort = (CimPortResource) aMachine.findPortResourceNamed(startCassette.getLoadPortID().getValue());
                    Validations.check(null == aLoadPort, new OmCode(retCodeConfig.getNotFoundPort(), "*****"));
                    CimPortResource anUnloadPort = aLoadPort.getAssociatedPort();
                    Validations.check(anUnloadPort == null, new OmCode(retCodeConfig.getNotFoundPort(), startCassette.getLoadPortID().getValue()), objCommon.getTransactionID());
                    startCassetteInfo .setUnloadPortID(new ObjectIdentifier(anUnloadPort.getIdentifier(), anUnloadPort.getPrimaryKey()));
                }
            }
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (!CimArrayUtils.isEmpty(lotInCassetteList)){
                for (Infos.LotInCassette lotInCassette : lotInCassetteList){
                    ProductDTO.PosLotInCassetteInfo lotInCassetteInfo = new ProductDTO.PosLotInCassetteInfo();
                    lotInCassetteInfoList.add(lotInCassetteInfo);
                    lotInCassetteInfo.setOperationStartFlag(lotInCassette.getMoveInFlag());
                    lotInCassetteInfo.setMonitorLotFlag(lotInCassette.getMonitorLotFlag());
                    lotInCassetteInfo.setLotID(lotInCassette.getLotID());
                    lotInCassetteInfo.setProcessSpecificControl(lotInCassette.getFurnaceSpecificControl());
                }
            }
        }
        /*---------------------------------------*/
        /*   Set Start Cassette to Control Job   */
        /*---------------------------------------*/
        aControlJob.setStartCassetteInfo(startCassetteInfoList);
        aControlJob.makeReservedFlagOff();
        /*--------------------------*/
        /*   Set Return Structure   */
        /*--------------------------*/
        return new ObjectIdentifier(aControlJob.getIdentifier(), aControlJob.getPrimaryKey());
    }

    @Override
    public Outputs.ControlJobAttributeInfo controlJobAttributeInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID) {
        /*---------------------------*/
        /*  Get control job object   */
        /*---------------------------*/
        CimControlJob controlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        Validations.check(null == controlJob, retCodeConfig.getNotFoundControlJob());
        /*----------------*/
        /*  Get Machine   */
        /*----------------*/
        CimMachine equipment = controlJob.getMachine();
        Validations.check(null == equipment, new OmCode(retCodeConfig.getNotFoundEqp(),controlJob.getMachine().getIdentifier()));
        /*-------------------*/
        /*  Get port group   */
        /*-------------------*/
        Outputs.ControlJobAttributeInfo controlJobAttributeInfo = new Outputs.ControlJobAttributeInfo();
        controlJobAttributeInfo.setMachineID(new ObjectIdentifier(equipment.getIdentifier(), equipment.getPrimaryKey()));
        controlJobAttributeInfo.setPortGroup(controlJob.getPortGroup());
        /*-----------------------------------*/
        /*  Get start cassette information   */
        /*-----------------------------------*/
        List<ProductDTO.PosStartCassetteInfo> startCassettes = controlJob.getStartCassetteInfo();
        List<Infos.ControlJobCassette> controlJobCassettes = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(startCassettes); i++) {
            Infos.ControlJobCassette controlJobCassette = new Infos.ControlJobCassette();
            controlJobCassette.setLoadSequenceNumber(startCassettes.get(i).getLoadSequenceNumber());
            controlJobCassette.setCassetteID(startCassettes.get(i).getCassetteID());
            controlJobCassette.setLoadPortID(startCassettes.get(i).getLoadPortID());
            controlJobCassette.setLoadPurposeType(new ObjectIdentifier(startCassettes.get(i).getLoadPurposeType()));
            controlJobCassette.setUnloadPortID(startCassettes.get(i).getUnloadPortID());
            List<Infos.ControlJobLot> controlJobLots = new ArrayList<>();
            for (int j = 0; j < CimArrayUtils.getSize(startCassettes.get(i).getLotInCassetteInfo()); j++) {
                Infos.ControlJobLot controlJobLot = new Infos.ControlJobLot();
                controlJobLot.setOperationStartFlag(startCassettes.get(i).getLotInCassetteInfo().get(j).isOperationStartFlag());
                controlJobLot.setMonitorLotFlag(startCassettes.get(i).getLotInCassetteInfo().get(j).isMonitorLotFlag());
                controlJobLot.setLotID(startCassettes.get(i).getLotInCassetteInfo().get(j).getLotID());
                controlJobLots.add(controlJobLot);
            }
            controlJobCassette.setControlJobLotList(controlJobLots);
            controlJobCassettes.add(controlJobCassette);
        }
        /*--------------*/
        /*  Get owner   */
        /*--------------*/
        CimPerson person = controlJob.getOwner();
        if (null != person) {
            controlJobAttributeInfo.setOwnerID(new ObjectIdentifier(person.getIdentifier(),person.getPrimaryKey()));
        }
        /*---------------------------*/
        /*  Get control job status   */
        /*---------------------------*/
        controlJobAttributeInfo.setControlJobStatus(controlJob.controlJobStatusGet());
        /*--------------------------*/
        /*  Get last claimed user   */
        /*--------------------------*/
        controlJobAttributeInfo.setLastClaimedUser(controlJob.getLastClaimedUserID());
        /*--------------------------------*/
        /*  Get last claimed time stamp   */
        /*--------------------------------*/
        controlJobAttributeInfo.setLastClaimedTimeStamp(controlJob.getLastClaimedTimeStamp());
        controlJobAttributeInfo.setStrControlJobCassetteSeq(controlJobCassettes);
         /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/
        return controlJobAttributeInfo;
    }

    @Override
    public void controlJobRelatedInfoUpdate(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDs) {
        int nLotIdx = 0;
        int nCastIdx = 0;
        //--------------------------------------------------------------------------------------------------
        // Get object reference from input cassette IDs
        //--------------------------------------------------------------------------------------------------
        List<CimCassette> aPosCassetteSeq = new ArrayList<>();
        int lenCasID = CimArrayUtils.getSize(cassetteIDs);
        if (0 == lenCasID) {
            return;
        }
        for (int i = 0; i < lenCasID; i++) {
            CimCassette tmpCassette = baseCoreFactory.getBO(CimCassette.class, cassetteIDs.get(i));
            if (null == tmpCassette) {
                continue;
            }
            aPosCassetteSeq.add(tmpCassette);
        }
        //--------------------------------------------------------------------------------------------------
        // Check input cassette count
        //--------------------------------------------------------------------------------------------------
        if (0 == aPosCassetteSeq.size()) {
            return;
        }
        //--------------------------------------------------------------------------------------------------
        // Get object reference of Machines
        //--------------------------------------------------------------------------------------------------
        Machine aMachine = aPosCassetteSeq.get(0).currentAssignedMachine();
        CimMachine aPosMachine = null;
        if (aMachine != null){
            boolean isStorageBool = aMachine.isStorageMachine();
            if (!isStorageBool){
                aPosMachine = (CimMachine) aMachine;
            }
        }
        //--------------------------------------------------------------------------------------------------
        // Get Carrier-lot information for all carrier
        //--------------------------------------------------------------------------------------------------
        int lenPosCasSeq = CimArrayUtils.getSize(aPosCassetteSeq);
        List<MachineDTO.MachineCassette> tmpLocalMachineCassetteSeq = new ArrayList<>();
        for (int i = 0; i < lenPosCasSeq; i++) {
            MachineDTO.MachineCassette machineCassette = new MachineDTO.MachineCassette();
            tmpLocalMachineCassetteSeq.add(machineCassette);
            machineCassette.setCassetteID(new ObjectIdentifier(aPosCassetteSeq.get(i).getIdentifier(), aPosCassetteSeq.get(i).getPrimaryKey()));
            List<Lot> aTmpLotSeq = aPosCassetteSeq.get(i).allLots();
            int lenLotSeq = CimArrayUtils.getSize(aTmpLotSeq);
            for (int j = 0; j < lenLotSeq; j++) {
                Validations.check(aTmpLotSeq.get(j) == null,
                        new OmCode(retCodeConfig.getCassetteLotRelationWrong(), tmpLocalMachineCassetteSeq.get(i).getCassetteID().getValue()));
                List<MachineDTO.MachineCassetteLot> machineCassetteLotList = tmpLocalMachineCassetteSeq.get(i).getMachineCassetteLots();
                if(CimObjectUtils.isEmpty(machineCassetteLotList)){
                    machineCassetteLotList = new ArrayList<>();
                    tmpLocalMachineCassetteSeq.get(i).setMachineCassetteLots(machineCassetteLotList);
                }
                MachineDTO.MachineCassetteLot machineCassetteLot = new MachineDTO.MachineCassetteLot();
                machineCassetteLot.setLotID(new ObjectIdentifier(aTmpLotSeq.get(j).getIdentifier(), aTmpLotSeq.get(j).getPrimaryKey()));
                machineCassetteLotList.add(machineCassetteLot);
            }
        }
        //--------------------------------------------------------------------------------------------------
        // Now update machine's carrier-lot information
        // and controljob information
        //--------------------------------------------------------------------------------------------------
        int lenMachineCasLots = 0;
        int nMachineCassetteLotLen = 0;
        int lenMachineCasSeq = 0;
        if ( null != aPosMachine) {
            //--------------------------------------------------------------------------------------------------
            // Collect Current Machine-cassette Info
            //--------------------------------------------------------------------------------------------------
            List<MachineDTO.MachineCassette> machineCassetteList = aPosMachine.allCassettes();
            lenMachineCasSeq = CimArrayUtils.getSize(machineCassetteList);
            for (int i = 0; i < lenMachineCasSeq; i++) {
                lenMachineCasLots = CimArrayUtils.getSize(machineCassetteList.get(i).getMachineCassetteLots());
                nMachineCassetteLotLen += lenMachineCasLots;
            }
            List<MachineDTO.MachineCassetteLot> machineCassetteLotList = new ArrayList<>();
            for (int i = 0; i < lenMachineCasSeq; i++){
                lenMachineCasLots = CimArrayUtils.getSize(machineCassetteList.get(i).getMachineCassetteLots());
                for (int j = 0;j < lenMachineCasLots; j++){
                    machineCassetteLotList.add(machineCassetteList.get(i).getMachineCassetteLots().get(j));
                }
            }
            //--------------------------------------------------------------------------------------------------
            // Compare Machine's cassette info and current cassette info
            //--------------------------------------------------------------------------------------------------
            lenMachineCasSeq = CimArrayUtils.getSize(machineCassetteList);
            int lenLocalMachineCasSeq = CimArrayUtils.getSize(tmpLocalMachineCassetteSeq);
            int lenTmpMachineCasLots = 0;
            boolean bMachineCassetteInfoUpdateNecessary = false;
            boolean bLotInfoFoundInMachineCassette = false;
            String lotOperationEIcheckStr = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
            int lotOperationEIcheck = CimStringUtils.isEmpty(lotOperationEIcheckStr) ? 0 : Integer.parseInt(lotOperationEIcheckStr);
            for (int i = 0; i < lenMachineCasSeq; i++) {
                bMachineCassetteInfoUpdateNecessary = false;
                CimCassette tmpCassette = null;
                MachineDTO.MachineCassette tmpLocalMachineCassette = null;
                nCastIdx = equipmentMethod.INTGetMachineCassetteSequenceIndex(tmpLocalMachineCassetteSeq, machineCassetteList.get(i).getCassetteID().getValue());
                if (-1 != nCastIdx) {
                    tmpCassette = aPosCassetteSeq.get(nCastIdx);
                    tmpLocalMachineCassette = machineCassetteList.get(i);
                    lenMachineCasLots = CimArrayUtils.getSize(machineCassetteList.get(i).getMachineCassetteLots());
                    for (int k = 0; k < lenMachineCasLots; k++) {
                        nLotIdx= equipmentMethod.INTGetMachineLotSequenceIndex(tmpLocalMachineCassetteSeq, nCastIdx, machineCassetteList.get(i).getMachineCassetteLots().get(k).getLotID().getValue());
                        if (-1 == nLotIdx) {
                            bMachineCassetteInfoUpdateNecessary = true;
                            break;
                        }
                    }
                    lenTmpMachineCasLots = CimArrayUtils.getSize(tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots());
                    for (int k = 0; k < lenTmpMachineCasLots; k++) {
                        nLotIdx = equipmentMethod.INTGetMachineLotPtrSequenceIndex(machineCassetteList, i, tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots().get(k).getLotID().getValue());
                        if (-1 == nLotIdx) {
                            bMachineCassetteInfoUpdateNecessary = true;
                            break;
                        }
                    }
                }
                if (!bMachineCassetteInfoUpdateNecessary) continue;
                //--------------------------------------------------------------------------------------------------
                // Necessary to update Machine's cassette info
                //--------------------------------------------------------------------------------------------------
                List<MachineDTO.MachineCassetteLot> tmpMachineCassetteLotSeq = new ArrayList<>();
                lenLocalMachineCasSeq = CimArrayUtils.getSize(tmpLocalMachineCassetteSeq);
                int lenMachineCasLotSeq = CimArrayUtils.getSize(machineCassetteLotList);
                nCastIdx = equipmentMethod.INTGetMachineCassetteSequenceIndex(tmpLocalMachineCassetteSeq, tmpLocalMachineCassette.getCassetteID().getValue());
                if (-1 != nCastIdx) {
                    lenTmpMachineCasLots = CimArrayUtils.getSize(tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots());
                    for (int k = 0; k < lenTmpMachineCasLots; k++) {
                        nLotIdx = equipmentMethod.INTGetMachineCassetteLotSequenceIndex(machineCassetteLotList, tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots().get(k).getLotID().getValue());
                        if (-1 != nLotIdx) {
                            tmpMachineCassetteLotSeq.add(machineCassetteLotList.get(nLotIdx));
                        } else {
                            MachineDTO.MachineCassetteLot tmpMachineCassetteLot = tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots().get(k);
                            tmpMachineCassetteLot.setMonitorLotFlag(false);
                            tmpMachineCassetteLot.setOperationStartFlag(false);
                            if (lotOperationEIcheck == 0) {
                                ObjectIdentifier tmpLotID = tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots().get(k).getLotID();
                                CimLot aNewLot = baseCoreFactory.getBO(CimLot.class, tmpLotID);
                                Validations.check(aNewLot == null, new OmCode(retCodeConfig.getNotFoundLot(), tmpLotID.getValue()));
                                //-----------------------------
                                // Get parent lot
                                //-----------------------------
                                Lot aParentPosLot = aNewLot.mostRecentlySplitFrom();
                                CimLot aParentLot = (CimLot) aParentPosLot;
                                if (null != aParentLot) {
                                    String aParentlotID = aParentPosLot.getIdentifier();
                                    //--------------------------------------------------------------
                                    // Find machine cassette lot of the parent lot
                                    //--------------------------------------------------------------
                                    nLotIdx = equipmentMethod.INTGetMachineCassetteLotSequenceIndex(machineCassetteLotList, aParentlotID);
                                    if (-1 != nLotIdx) {
                                        //--------------------------------------------------------------
                                        // Set parent lot's operationStartFlag and monitorLotFlag
                                        //--------------------------------------------------------------
                                        tmpMachineCassetteLot.setMonitorLotFlag(machineCassetteLotList.get(nLotIdx).isMonitorLotFlag());
                                        tmpMachineCassetteLot.setOperationStartFlag(machineCassetteLotList.get(nLotIdx).isOperationStartFlag());
                                    }
                                }
                            }
                            tmpMachineCassetteLotSeq.add(tmpMachineCassetteLot);
                        }
                    }
                }
                tmpLocalMachineCassette.setMachineCassetteLots(tmpMachineCassetteLotSeq);
                aPosMachine.removeCassette(tmpCassette);
                aPosMachine.addCassette(tmpLocalMachineCassette);
            }
            //--------------------------------------------------------------------------------------------------
            // Collect Current Machine-lot Info
            //--------------------------------------------------------------------------------------------------
            List<MachineDTO.MachineLot> strMachineLotSeq = aPosMachine.allProcessingLots();
            //--------------------------------------------------------------------------------------------------
            // Compare Machine's lot info and current cassette-lot info
            //--------------------------------------------------------------------------------------------------
            int lenMachineLotSeq = CimArrayUtils.getSize(strMachineLotSeq);
            lenLocalMachineCasSeq = CimArrayUtils.getSize(tmpLocalMachineCassetteSeq);
            for (int i = 0; i < lenMachineLotSeq; i++) {
                Boolean bMachineLotInfoUpdated = false;
                for (int j = 0; j < lenLocalMachineCasSeq; j++) {
                    lenMachineCasLots = CimArrayUtils.getSize(tmpLocalMachineCassetteSeq.get(j).getMachineCassetteLots());
                    nLotIdx = equipmentMethod.INTGetMachineLotSequenceIndex(tmpLocalMachineCassetteSeq, j, ObjectIdentifier.fetchValue(strMachineLotSeq.get(i).getLotID()));
                    if (-1 == nLotIdx) continue;
                    if (!ObjectIdentifier.equalsWithValue(strMachineLotSeq.get(i).getUnloadCassetteID(), tmpLocalMachineCassetteSeq.get(j).getCassetteID())) {
                        ObjectIdentifier tmpLotID = tmpLocalMachineCassetteSeq.get(j).getMachineCassetteLots().get(nLotIdx).getLotID();
                        CimLot aTmpLot = baseCoreFactory.getBO(CimLot.class, tmpLotID);
                        Validations.check(aTmpLot == null, new OmCode(retCodeConfig.getNotFoundLot(), tmpLotID.getValue()));
                        MaterialLocation aTmpMaterialLocation = aPosCassetteSeq.get(j).getLocation();
                        Validations.check(aTmpMaterialLocation == null, new OmCode(retCodeConfig.getCassetteNotInUnloader(), tmpLocalMachineCassetteSeq.get(j).getCassetteID().getValue()));

                        CimBO aTmpObject = aTmpMaterialLocation.getObjectManager();
                        CimPortResource aPosPort = aTmpObject instanceof CimPortResource ? (CimPortResource) aTmpObject : null;
                        MachineDTO.MachineLot tmpMachineLot = strMachineLotSeq.get(i);
                        if (null == aPosPort) {
                            tmpMachineLot.setUnloadCassetteID(tmpLocalMachineCassetteSeq.get(j).getCassetteID());
                            tmpMachineLot.setUnloadSequenceNumber(0L);
                        } else {
                            Long nUnloadSeqNo = aPosPort.getUnloadSeqInPortGroup();
                            tmpMachineLot.setUnloadCassetteID(tmpLocalMachineCassetteSeq.get(j).getCassetteID());
                            tmpMachineLot.setUnloadSequenceNumber(nUnloadSeqNo);
                            tmpMachineLot.setUnloadPortID(new ObjectIdentifier(aPosPort.getIdentifier(), aPosPort.getPrimaryKey()));
                        }
                        aPosMachine.removeProcessingLot(aTmpLot);
                        aPosMachine.addProcessingLot(strMachineLotSeq.get(i));
                        bMachineLotInfoUpdated = true;
                    }
                    if (bMachineLotInfoUpdated) {
                        break;
                    }
                }
            }
        }
        //--------------------------------------------------------------------------------------------------
        // Get object reference of Control Jobs
        //--------------------------------------------------------------------------------------------------
        List<CimControlJob> aPosControlJobSeq = new ArrayList<>();
        List<String> strControlJobIDSeq = new ArrayList<>();
        int lenCtrlJobIDSeq = 0;
        int lenCtrlJobSeq = 0;
        boolean bControlJobAdded = false;
        lenPosCasSeq = CimArrayUtils.getSize(aPosCassetteSeq);
        String tmpControlJobID = null;
        for (int i = 0; i < lenPosCasSeq; i++) {
            CimControlJob tmpControlJob = aPosCassetteSeq.get(i).getControlJob();
            if (tmpControlJob == null){
                continue;
            }
            tmpControlJobID = tmpControlJob.getIdentifier();
            bControlJobAdded = false;
            lenCtrlJobIDSeq = CimArrayUtils.getSize(strControlJobIDSeq);
            for (int j = 0; j < lenCtrlJobIDSeq; j++) {
                if (CimStringUtils.equals(tmpControlJobID, strControlJobIDSeq.get(j))) {
                    bControlJobAdded = true;
                    break;
                }
            }
            if (!bControlJobAdded) {
                aPosControlJobSeq.add(tmpControlJob);
                strControlJobIDSeq.add(tmpControlJobID);
            }
        }
        //--------------------------------------------------------------------------------------------------
        // Collect Start cassette Info from controljob
        //--------------------------------------------------------------------------------------------------
        List<ProductDTO.PosStartCassetteInfo> strPosStartCassetteInfoSeq = new ArrayList<>();
        int nStrStartCassetteInfoLen = 0;
        int nTmpStartCassetteInfoLen = 0;
        lenCtrlJobSeq = CimArrayUtils.getSize(aPosControlJobSeq);
        for (int i = 0; i < lenCtrlJobSeq; i++) {
            List<ProductDTO.PosStartCassetteInfo> tmpPosStartCassetteInfoSeq = aPosControlJobSeq.get(i).getStartCassetteInfo();
            if (CimArrayUtils.isNotEmpty(tmpPosStartCassetteInfoSeq)) {
                strPosStartCassetteInfoSeq.addAll(tmpPosStartCassetteInfoSeq);
            }
        }
        //--------------------------------------------------------------------------------------------------
        // Check and update local Control Job Info
        //--------------------------------------------------------------------------------------------------
        List<ProductDTO.PosLotInCassetteInfo> tmpLotInCassetteInfoSeq = new ArrayList<>();
        int lenLocalMachineCasSeq = CimArrayUtils.getSize(tmpLocalMachineCassetteSeq);
        nStrStartCassetteInfoLen = CimArrayUtils.getSize(strPosStartCassetteInfoSeq);
        Integer lenLotInCasInfo = 0;
        Boolean bLotInCassetteFound = false;
        List<ProductDTO.PosStartCassetteInfo> copyStartCassetteInfoList = new ArrayList<>(strPosStartCassetteInfoSeq);

        // strPosStartCassetteInfoSeq[i].cassetteID.identifier
        for (int i = 0; i < nStrStartCassetteInfoLen; i++) {
            nCastIdx = equipmentMethod.INTGetMachineCassetteSequenceIndex(tmpLocalMachineCassetteSeq, strPosStartCassetteInfoSeq.get(i).getCassetteID().getValue());
            if (-1 != nCastIdx ) {
                lenLotInCasInfo = CimArrayUtils.getSize(strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo());
                for (int k = 0; k < lenLotInCasInfo; k++) {
                    nLotIdx = equipmentMethod.INTGetMachineLotSequenceIndex(tmpLocalMachineCassetteSeq, nCastIdx, strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo().get(k).getLotID().getValue());
                    if (-1 == nLotIdx) {
                        ProductDTO.PosLotInCassetteInfo tmpLotInCassette = strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo().get(k);
                        tmpLotInCassetteInfoSeq.add(tmpLotInCassette);
                        tmpLotInCassette.setLotID(null);
                    }
                }
                // copy sequence buffer. copy not null lotID only
                copyStartCassetteInfoList.set(i, strPosStartCassetteInfoSeq.get(i));
                copyStartCassetteInfoList.get(i).setLotInCassetteInfo(Collections.emptyList());
                for (int k = 0; k < lenLotInCasInfo; k++) {
                    if (!CimObjectUtils.isEmpty(strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo()) && !ObjectIdentifier.isEmptyWithValue(strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo().get(k).getLotID())) {
                        ProductDTO.PosLotInCassetteInfo lotInCassette = strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo().get(k);
                        List<ProductDTO.PosLotInCassetteInfo> lotInCassetteList = copyStartCassetteInfoList.get(i).getLotInCassetteInfo();
                        lotInCassetteList.set(k, lotInCassette);
                    }
                }
            }
        }
        // copy tmp buffer
        strPosStartCassetteInfoSeq = copyStartCassetteInfoList;

        nStrStartCassetteInfoLen = CimArrayUtils.getSize(strPosStartCassetteInfoSeq);
        lenLocalMachineCasSeq    = CimArrayUtils.getSize(tmpLocalMachineCassetteSeq);
        for (int i = 0; i < nStrStartCassetteInfoLen; i++) {
            nCastIdx = equipmentMethod.INTGetMachineCassetteSequenceIndex(tmpLocalMachineCassetteSeq, strPosStartCassetteInfoSeq.get(i).getCassetteID().getValue());
            if (-1 == nCastIdx) continue;
            lenMachineCasLots = CimArrayUtils.getSize(tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots());
            for (int k = 0; k < lenMachineCasLots; k++) {
                nLotIdx = equipmentMethod.INTGetStartLotSequenceIndex(strPosStartCassetteInfoSeq, i, tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots().get(k).getLotID().getValue());
                if (-1 != nLotIdx) continue;
                // append lotID
                lenLotInCasInfo = CimArrayUtils.getSize(strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo());
                if (CimObjectUtils.isEmpty(strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo())) {
                    List<ProductDTO.PosLotInCassetteInfo> posLotInCassetteInfos = new ArrayList<>();
                    posLotInCassetteInfos.add(new ProductDTO.PosLotInCassetteInfo());
                    strPosStartCassetteInfoSeq.get(i).setLotInCassetteInfo(posLotInCassetteInfos);
                } else {
                    strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo().add(new ProductDTO.PosLotInCassetteInfo()); //strPosStartCassetteInfoSeq[i].lotInCassetteInfo.length(lenLotInCasInfo + 1);
                }
                nLotIdx = equipmentMethod.INTGetLotInCassetteSequenceIndex(tmpLotInCassetteInfoSeq, tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots().get(k).getLotID().getValue());
                if (-1 != nLotIdx) {
                    lenLotInCasInfo = CimArrayUtils.getSize(strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo());
                    List<ProductDTO.PosLotInCassetteInfo> lotInCassetteInfo = strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo();
                    lotInCassetteInfo.add(tmpLotInCassetteInfoSeq.get(nLotIdx));
                } else {
                    lenLotInCasInfo = CimArrayUtils.getSize(strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo());
                    ProductDTO.PosLotInCassetteInfo posLotInCassetteInfo = strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo().get(lenLotInCasInfo - 1);
                    posLotInCassetteInfo.setLotID(tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots().get(k).getLotID());
                    // Get lot controlJob
                    ObjectIdentifier tmpLotID = tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots().get(k).getLotID();
                    CimLot aNewLot = baseCoreFactory.getBO(CimLot.class, tmpLotID);
                    Validations.check(aNewLot == null, new OmCode(retCodeConfig.getNotFoundLot(), tmpLotID.getValue()));
                    CimControlJob aControlJob = aNewLot.getControlJob();
                    ProductDTO.PosLotInCassetteInfo lotInCassetteInfo = strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo().get(lenLotInCasInfo - 1);
                    lotInCassetteInfo.setMonitorLotFlag(false);
                    if (null != aControlJob) {
                        lotInCassetteInfo.setOperationStartFlag(true);
                        // Get monitorLotFlag from machineCassetteLot
                        List<MachineDTO.MachineCassette> strMachineCassetteSeq = aPosMachine.allCassettes();
                        boolean lotFoundFlag = false;
                        int lenCast = CimArrayUtils.getSize(strMachineCassetteSeq);
                        for (int iCnt1 = 0; iCnt1 < lenCast; iCnt1++) {
                            int lenCastLot = CimArrayUtils.getSize(strMachineCassetteSeq.get(iCnt1).getMachineCassetteLots());
                            for (int iCnt2 = 0; iCnt2 < lenCastLot; iCnt2++) {
                                if (ObjectIdentifier.equalsWithValue(tmpLocalMachineCassetteSeq.get(nCastIdx).getMachineCassetteLots().get(k).getLotID(),
                                        strMachineCassetteSeq.get(iCnt1).getMachineCassetteLots().get(iCnt2).getLotID())) {
                                    // found the lot
                                    ProductDTO.PosLotInCassetteInfo posLotInCassetteInfo2 = strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo().get(lenLotInCasInfo - 1);
                                    posLotInCassetteInfo2.setMonitorLotFlag(strMachineCassetteSeq.get(iCnt1).getMachineCassetteLots().get(iCnt2).isMonitorLotFlag());
                                    lotFoundFlag = true;
                                    break;
                                }
                            }
                            if (lotFoundFlag) {
                                break;
                            }
                        }
                    } else {
                        strPosStartCassetteInfoSeq.get(i).getLotInCassetteInfo().get(lenLotInCasInfo-1).setOperationStartFlag(false);
                    }
                }
            }
        }
        //--------------------------------------------------------------------------------------------------
        // Update Control Job Info
        //--------------------------------------------------------------------------------------------------
        lenCtrlJobSeq = CimArrayUtils.getSize(aPosControlJobSeq);
        nStrStartCassetteInfoLen = CimArrayUtils.getSize(strPosStartCassetteInfoSeq);
        for (int i = 0; i < lenCtrlJobSeq; i++) {
            List<ProductDTO.PosStartCassetteInfo> tmpPosStartCassetteInfoSeq = aPosControlJobSeq.get(i).getStartCassetteInfo();
            nTmpStartCassetteInfoLen = CimArrayUtils.getSize(tmpPosStartCassetteInfoSeq);
            for (int j = 0; j < nTmpStartCassetteInfoLen; j++) {
                nCastIdx = equipmentMethod.INTGetStartCassetteSequenceIndex(strPosStartCassetteInfoSeq, tmpPosStartCassetteInfoSeq.get(j).getCassetteID().getValue());
                if (-1 != nCastIdx) {
                    tmpPosStartCassetteInfoSeq.set(j, strPosStartCassetteInfoSeq.get(nCastIdx));
                }
            }
            aPosControlJobSeq.get(i).setStartCassetteInfo(tmpPosStartCassetteInfoSeq);
        }
    }

    @Override
    public void controlJobEmptyCassetteInfoDelete(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDList) {
        log.debug("controlJobEmptyCassetteInfoDelete(): enter here");

        //Check if there is empty cassette in input parameter or not
        List<ObjectIdentifier> emptyCassetteIDList = new ArrayList<>();
        if (!CimArrayUtils.isEmpty(cassetteIDList)) {
            for (ObjectIdentifier cassetteID : cassetteIDList) {
                CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
                Validations.check(null == cassette, retCodeConfig.getNotFoundCassette());
                //Check cassette is empty or not
                boolean cassetteEmpty = cassette.isEmpty();
                CimControlJob controlJob = cassette.getControlJob();
                boolean clearInfo = false;
                if (controlJob != null) {
                    if (cassetteEmpty) {
                        clearInfo = true;
                    } else {
                        List<ProductDTO.PosStartCassetteInfo> startCassetteInfoList = controlJob.getStartCassetteInfo();
                        boolean opeStartFlagOn = false;
                        if (!CimArrayUtils.isEmpty(startCassetteInfoList)) {
                            for (ProductDTO.PosStartCassetteInfo startCassette : startCassetteInfoList) {
                                if (!CimStringUtils.equals(cassetteID.getValue(), startCassette.getCassetteID().getValue())) {
                                    log.debug("controlJobEmptyCassetteInfoDelete(): UnMatch CarrierID. So continue...");
                                    continue;
                                }
                                List<ProductDTO.PosLotInCassetteInfo> lotInCassetteList = startCassette.getLotInCassetteInfo();
                                if (!CimArrayUtils.isEmpty(lotInCassetteList)) {
                                    for (ProductDTO.PosLotInCassetteInfo lotInCassette : lotInCassetteList) {
                                        if (lotInCassette.isOperationStartFlag()) {
                                            opeStartFlagOn = true;
                                            break;
                                        }
                                    }
                                }
                                if (opeStartFlagOn) {
                                    break;
                                }
                            }
                        }
                        if (!opeStartFlagOn) {
                            clearInfo = true;
                        }
                    }
                }
                if(clearInfo){
                    emptyCassetteIDList.add(cassetteID);
                    //The relation of Shelf to controljob is erased.
                    Machine currentEquipment = cassette.currentAssignedMachine();
                    if (currentEquipment != null) {
                        List<BufferResource> buffResourceList = currentEquipment.allBufferResources();
                        if(!CimArrayUtils.isEmpty(buffResourceList)){
                            for (BufferResource buffResource : buffResourceList) {
                                List<MaterialLocation> materialLocationList = buffResource.allMaterialLocations();
                                if (CimArrayUtils.isEmpty(buffResourceList)) {
                                    continue;
                                }
                                for (MaterialLocation materialLocation1 : materialLocationList) {
                                    CimMaterialLocation materialLocation = (CimMaterialLocation) materialLocation1;
                                    ObjectIdentifier containedCassetteID = null;
                                    ObjectIdentifier allocatedCassetteID = null;
                                    Material allocatedCassette = materialLocation.getAllocatedMaterial();
                                    if (allocatedCassette == null) {
                                        Material containedCassette = materialLocation.getMaterial();
                                        if(null != containedCassette){
                                            containedCassetteID = ObjectIdentifier.build(containedCassette.getIdentifier(), containedCassette.getPrimaryKey());
                                        }
                                    } else {
                                        allocatedCassetteID = ObjectIdentifier.build(allocatedCassette.getIdentifier(), allocatedCassette.getPrimaryKey());
                                    }

                                    if (!CimStringUtils.equals(cassetteID.getValue(), containedCassetteID == null ? null : containedCassetteID.getValue())
                                            && !CimStringUtils.equals(cassetteID.getValue(), allocatedCassetteID == null ? null : allocatedCassetteID.getValue())) {
                                        continue;
                                    }

                                    //Check Control Job of Material Location
                                    CimControlJob materialLocationControlJob = materialLocation.getControlJob();
                                    if (materialLocationControlJob == null) {
                                        continue;
                                    }

                                    if (!ObjectIdentifier.equalsWithValue(controlJob.getControlJobID(), materialLocationControlJob.getControlJobID())) {
                                        continue;
                                    }

                                    //Delete controlJob relation of MaterialLocation
                                    materialLocation.setControlJob(null);
                                }
                            }
                        }
                    }
                }
            }
        }
        //Remove empty cassette information from Control Job, controlJob_cassetteInfo_Delete
        controlJobCassetteInfoDelete(objCommon, emptyCassetteIDList);
        return ;
    }

    @Override
    public void controlJobCassetteInfoDelete(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDList) {
        //Remove input cassette from attendant control job
        if (CimArrayUtils.isEmpty(cassetteIDList)) {
            return;
        }
        for (ObjectIdentifier cassetteID : cassetteIDList) {
            CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
            Validations.check(null == cassette, retCodeConfig.getNotFoundCassette());
            //Get cassette Control Job
            CimControlJob controlJob = cassette.getControlJob();
            //cassette has Control Job !!! Remove cassette Information from Control Job !!!
            if(controlJob != null){
                cassette.setControlJob(null);

                List<ProductDTO.PosStartCassetteInfo> startCassetteInfo = controlJob.getStartCassetteInfo();
                boolean castFoundControlJob = false;
                if (CimArrayUtils.isEmpty(startCassetteInfo)) {
                    continue;
                }
                Iterator<ProductDTO.PosStartCassetteInfo> iterator = startCassetteInfo.iterator();
                while (iterator.hasNext()) {
                    ProductDTO.PosStartCassetteInfo startCassette = iterator.next();
                    if (CimStringUtils.equals(cassetteID.getValue(), startCassette.getCassetteID().getValue())) {
                        iterator.remove();
                        castFoundControlJob = true;
                        break;
                    }
                }
                if(castFoundControlJob){
                    controlJob.setStartCassetteInfo(startCassetteInfo);
                }
            }
        }
        return;
    }

    @Override
    public Outputs.ObjControlJobStatusGetOut controlJobStatusGet(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID) {
        Outputs.ObjControlJobStatusGetOut data = new Outputs.ObjControlJobStatusGetOut();

        //【Step1】Get control job status information;
        CimControlJob controlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        Validations.check(controlJob == null, new OmCode(retCodeConfig.getNotFoundControlJob(), controlJobID.getValue()));
        // Get control job status
        String tempControlStatus = controlJob.controlJobStatusGet();
        data.setControlJobStatus(tempControlStatus);
        // Get last claimed userID
        String tempLastClaimedUserID = controlJob.getLastClaimedUserID();
        data.setLastClaimedUserID(ObjectIdentifier.build(tempLastClaimedUserID, ""));
        // Get last claimed timestamp
        Timestamp tempLastClaimedTimeStamp = controlJob.getLastClaimedTimeStamp();
        data.setLastClaimedTimeStamp(CimDateUtils.convertToSpecString(tempLastClaimedTimeStamp));
        return data;
    }

    @Override
    public void controlJobProcessWafersSet(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, List<Infos.ProcessJob> processJobs) {
        List<ObjectIdentifier> lotIDs = processJobs.stream().flatMap(x -> x.getProcessWaferList().stream().map(Infos.ProcessWafer::getLotID)).distinct().collect(Collectors.toList());
        for (ObjectIdentifier lotID : lotIDs) {
            CimLot lotDO = baseCoreFactory.getBO(CimLot.class, lotID);
            Validations.check(lotDO == null, retCodeConfig.getNotFoundLot());

            // Check lot's controlJob is the same as inParameter
            CimControlJob controlJob = lotDO.getControlJob();
            ObjectIdentifier lotControlJobID = new ObjectIdentifier(controlJob.getIdentifier(), controlJob.getPrimaryKey());
            Validations.check(!ObjectIdentifier.equalsWithValue(controlJobID, lotControlJobID),
                    retCodeConfig.getSlmInvalidParameterForCj(), ObjectIdentifier.fetchValue(lotID), ObjectIdentifier.fetchValue(lotControlJobID), ObjectIdentifier.fetchValue(controlJobID));

            // Get Lot PO
            CimProcessOperation processOperationDO = lotDO.getProcessOperation();
            Validations.check(processOperationDO == null, retCodeConfig.getNotFoundProcessOperation(), "", ObjectIdentifier.fetchValue(lotID));

            // Get wafer list from PO
            List<ProcessDTO.PosProcessWafer> aOldPosProcessWaferSeq = processOperationDO.getProcessWafers();

            // Get wafer list from inParameter
            List<ProcessDTO.PosProcessWafer> processWafers = new ArrayList<>();
            if (!CimArrayUtils.isEmpty(processJobs)){
                for (Infos.ProcessJob processJob : processJobs){
                    List<Infos.ProcessWafer> processWaferList = processJob.getProcessWaferList();
                    if (!CimArrayUtils.isEmpty(processWaferList)){
                        for (Infos.ProcessWafer processWafer : processWaferList){
                            if (ObjectIdentifier.equalsWithValue(processWafer.getLotID(), lotID)){
                                ProcessDTO.PosProcessWafer posProcessWafer = new ProcessDTO.PosProcessWafer();
                                processWafers.add(posProcessWafer);
                                posProcessWafer.setWaferID(ObjectIdentifier.fetchValue(processWafer.getWaferID()));
                                posProcessWafer.setPrcsJob(processJob.getProcessJobID());
                                posProcessWafer.setPrcsJobPosition(processJob.getProcessJobPosition());
                                posProcessWafer.setSamplingWaferFlag(false);
                                if (!CimArrayUtils.isEmpty(aOldPosProcessWaferSeq)){
                                    for (ProcessDTO.PosProcessWafer oldPosProcessWafer : aOldPosProcessWaferSeq){
                                        if (CimStringUtils.equals(posProcessWafer.getWaferID(), oldPosProcessWafer.getWaferID())){
                                            posProcessWafer.setSamplingWaferFlag(oldPosProcessWafer.isSamplingWaferFlag());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            processOperationDO.setProcessWafers(processWafers);
        }
    }

    @Override
    public Infos.ControlJobInformation controlJobStartLotWaferInfoGet(Infos.ObjCommon objCommon, Inputs.ControlJobStartLotWaferInfoGetIn controlJobStartLotWaferInfoGetIn) {
        Infos.ControlJobInformation controlJobInformation = new Infos.ControlJobInformation();
        controlJobInformation.setControlJobID(controlJobStartLotWaferInfoGetIn.getControlJobID());
        /*---------------------------*/
        /*  Get control job object   */
        /*---------------------------*/
        CimControlJob aControlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobStartLotWaferInfoGetIn.getControlJobID());
        Validations.check(CimObjectUtils.isEmpty(aControlJob), retCodeConfig.getNotFoundControlJob());

        /*--------------*/
        /*  Get owner   */
        /*--------------*/
        CimPerson aPerson = aControlJob.getOwner();
        controlJobInformation.setOwnerID(CimObjectUtils.isEmpty(aPerson) ? null : new ObjectIdentifier(aPerson.getIdentifier(), aPerson.getPrimaryKey()));
        /*----------------*/
        /*  Get Machine   */
        /*----------------*/
        CimMachine aMachine = aControlJob.getMachine();
        controlJobInformation.setMachineID(CimObjectUtils.isEmpty(aMachine) ? null : new ObjectIdentifier(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
        // Get port group
        controlJobInformation.setPortGroup(aControlJob.getPortGroup());
        /*----------------------------------------*/
        /*  Get estimated completion time stamp   */
        /*----------------------------------------*/
        controlJobInformation.setEstimatedCompletionTime(CimDateUtils.convertToSpecString(aControlJob.getEstimatedProcessCompTimeStamp()));
        // Get reserve flag
        controlJobInformation.setReservedFlag(aControlJob.isReservedFlagOn());
        /*---------------------------*/
        /*  Get control job status   */
        /*---------------------------*/
        controlJobInformation.setControlJobStatus(aControlJob.controlJobStatusGet());
        /*--------------------------*/
        /*  Get last claimed user   */
        /*--------------------------*/
        controlJobInformation.setLastClaimedUser(aControlJob.getLastClaimedUserID());
        /*--------------------------------*/
        /*  Get last claimed time stamp   */
        /*--------------------------------*/
        controlJobInformation.setLastClaimedTimeStamp(CimDateUtils.convertToSpecString(aControlJob.getLastClaimedTimeStamp()));
        /*-----------------------------------*/
        /*  Get start cassette information   */
        /*-----------------------------------*/
        List<ProductDTO.PosStartCassetteInfo> startCassetteInfoList = aControlJob.getStartCassetteInfo();
        List<Infos.ControlJobCassetteInfo> controlJobCassetteList = new ArrayList<>();
        controlJobInformation.setControlJobCassetteInfoList(controlJobCassetteList);
        if (!CimObjectUtils.isEmpty(startCassetteInfoList)){
            for (ProductDTO.PosStartCassetteInfo startCassetteInfo : startCassetteInfoList){
                Infos.ControlJobCassetteInfo controlJobCassetteInfo = new Infos.ControlJobCassetteInfo();
                controlJobCassetteList.add(controlJobCassetteInfo);
                controlJobCassetteInfo.setCassetteID(startCassetteInfo.getCassetteID());
                controlJobCassetteInfo.setLoadPortID(startCassetteInfo.getLoadPortID());
                controlJobCassetteInfo.setUnloadPortID(startCassetteInfo.getUnloadPortID());
                controlJobCassetteInfo.setLoadSequenceNumber(startCassetteInfo.getLoadSequenceNumber());
                controlJobCassetteInfo.setLoadPurposeType(startCassetteInfo.getLoadPurposeType());
                List<ProductDTO.PosLotInCassetteInfo> lotInCassetteInfoList = startCassetteInfo.getLotInCassetteInfo();
                List<Infos.ControlJobCassetteLot> controlJobCassetteLotList = new ArrayList<>();
                controlJobCassetteInfo.setControlJobCassetteLotList(controlJobCassetteLotList);
                if (!CimObjectUtils.isEmpty(lotInCassetteInfoList)){
                    for (ProductDTO.PosLotInCassetteInfo lotInCassetteInfo : lotInCassetteInfoList){
                        if (controlJobStartLotWaferInfoGetIn.isStartLotOnlyFlag() && !lotInCassetteInfo.isOperationStartFlag()){
                            continue;
                        }
                        Infos.ControlJobCassetteLot controlJobCassetteLot = new Infos.ControlJobCassetteLot();
                        controlJobCassetteLotList.add(controlJobCassetteLot);
                        controlJobCassetteLot.setLotID(lotInCassetteInfo.getLotID());
                        controlJobCassetteLot.setMonitorLotFlag(lotInCassetteInfo.isMonitorLotFlag());
                        controlJobCassetteLot.setOperationStartFlag(lotInCassetteInfo.isOperationStartFlag());
                    }
                }
            }
        }
        /* -----------------------------------------------------------------------------------------*/
        // Ceate startCassette from controlJob lot for SLM
        // By SLM function, lot is allowed to be cut relation from startCassette while processing
        // CAUTION: the startCassette could be created with blank cassetteID
        /* -----------------------------------------------------------------------------------------*/
        List<ProductDTO.LotInControlJobInfo> lotInControlJobInfoList = aControlJob.allControlJobLots();
        boolean lotNotInCastFlag = false;
        if (!CimObjectUtils.isEmpty(lotInControlJobInfoList)){
            for (ProductDTO.LotInControlJobInfo lotInControlJobInfo : lotInControlJobInfoList){
                lotNotInCastFlag = true;
                List<Infos.ControlJobCassetteInfo> controlJobCassetteInfoList = controlJobInformation.getControlJobCassetteInfoList();
                if (!CimObjectUtils.isEmpty(controlJobCassetteInfoList)){
                    for (Infos.ControlJobCassetteInfo controlJobCassetteInfo : controlJobCassetteInfoList){
                        List<Infos.ControlJobCassetteLot> controlJobCassetteLotList = controlJobCassetteInfo.getControlJobCassetteLotList();
                        if (!CimObjectUtils.isEmpty(controlJobCassetteLotList)){
                            for (Infos.ControlJobCassetteLot controlJobCassetteLot : controlJobCassetteLotList){
                                if (ObjectIdentifier.equalsWithValue(controlJobCassetteLot.getLotID(), lotInControlJobInfo.getLotID())){
                                    lotNotInCastFlag = false;
                                    break;
                                }
                            }
                            if (!lotNotInCastFlag){
                                break;
                            }
                        }
                    }
                }
                if (lotNotInCastFlag){
                    // create ControlJobCassette from this lot
                    List<Infos.ControlJobCassetteInfo> tmpControlJobCassetteInfoList = new ArrayList<>();
                    controlJobInformation.setControlJobCassetteInfoList(tmpControlJobCassetteInfoList);
                    Infos.ControlJobCassetteInfo controlJobCassetteInfo = new Infos.ControlJobCassetteInfo();
                    tmpControlJobCassetteInfoList.add(controlJobCassetteInfo);
                    controlJobCassetteInfo.setLoadSequenceNumber(0);
                    controlJobCassetteInfo.setCassetteID(new ObjectIdentifier());
                    controlJobCassetteInfo.setLoadPurposeType("");
                    controlJobCassetteInfo.setLoadPortID(new ObjectIdentifier());
                    controlJobCassetteInfo.setUnloadPortID(new ObjectIdentifier());
                    List<Infos.ControlJobCassetteLot> controlJobCassetteLotList = new ArrayList<>();
                    controlJobCassetteInfo.setControlJobCassetteLotList(controlJobCassetteLotList);
                    Infos.ControlJobCassetteLot controlJobCassetteLot = new Infos.ControlJobCassetteLot();
                    controlJobCassetteLotList.add(controlJobCassetteLot);
                    controlJobCassetteLot.setOperationStartFlag(true);
                    controlJobCassetteLot.setMonitorLotFlag(lotInControlJobInfo.getMonitorLotFlag());
                    controlJobCassetteLot.setLotID(lotInControlJobInfo.getLotID());
                }
            }
        }
        List<Infos.ControlJobCassetteInfo> controlJobCassetteInfoList = controlJobInformation.getControlJobCassetteInfoList();
        if (!CimObjectUtils.isEmpty(controlJobCassetteInfoList)){
            for (Infos.ControlJobCassetteInfo controlJobCassetteInfo : controlJobCassetteInfoList){
                List<Infos.ControlJobCassetteLot> controlJobCassetteLotList = controlJobCassetteInfo.getControlJobCassetteLotList();
                if (!CimObjectUtils.isEmpty(controlJobCassetteLotList)){
                    for (Infos.ControlJobCassetteLot controlJobCassetteLot : controlJobCassetteLotList){
                        if (controlJobStartLotWaferInfoGetIn.isStartLotOnlyFlag() && !controlJobCassetteLot.isOperationStartFlag()){
                            continue;
                        }
                        CimLot aLot = baseCoreFactory.getBO(CimLot.class, controlJobCassetteLot.getLotID());
                        List<ProductDTO.WaferInfo> waferInfoList =aLot.getAllWaferInfo();
                        List<ObjectIdentifier> waferIDs = new ArrayList<>();
                        controlJobCassetteLot.setWaferIDs(waferIDs);
                        if (!CimObjectUtils.isEmpty(waferInfoList)){
                            for (ProductDTO.WaferInfo waferInfo : waferInfoList){
                                waferIDs.add(waferInfo.getWaferID());
                            }
                        }
                    }
                }
            }
        }
        return controlJobInformation;
    }

    @Override
    public void controlJobUpdateForPartialOpeComp(Infos.ObjCommon objCommon, Infos.ControlJobInformation controlJobInfo, List<Infos.PartialOpeCompLot> partialOpeCompLotSeqForOpeComp) {
        List<Infos.ControlJobCassetteInfo> controlJobCassetteList = controlJobInfo.getControlJobCassetteInfoList();
        // create temp start cassette for controlJob
        List<ProductDTO.PosStartCassetteInfo> tmpStartCassetteInfoList = new ArrayList<>();
        List<ObjectIdentifier> waferIDs = new ArrayList<>();
        for (Infos.ControlJobCassetteInfo controlJobCassetteInfo : controlJobCassetteList){
            List<Infos.ControlJobCassetteLot> controlJobCassetteLotList = controlJobCassetteInfo.getControlJobCassetteLotList();
            boolean partialCompCastFlag = false;
            for (Infos.ControlJobCassetteLot controlJobCassetteLot : controlJobCassetteLotList){
                for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotSeqForOpeComp){
                    if (ObjectIdentifier.equalsWithValue(controlJobCassetteLot.getLotID(), partialOpeCompLot.getLotID())){
                        partialCompCastFlag = true;
                        break;
                    }
                }
                if (partialCompCastFlag){
                    break;
                }
            }
            if (partialCompCastFlag){
                ProductDTO.PosStartCassetteInfo posStartCassetteInfo = new ProductDTO.PosStartCassetteInfo();
                tmpStartCassetteInfoList.add(posStartCassetteInfo);
                posStartCassetteInfo.setLoadSequenceNumber(controlJobCassetteInfo.getLoadSequenceNumber());
                posStartCassetteInfo.setCassetteID(controlJobCassetteInfo.getCassetteID());
                posStartCassetteInfo.setLoadPurposeType(controlJobCassetteInfo.getLoadPurposeType());
                posStartCassetteInfo.setLoadPortID(controlJobCassetteInfo.getLoadPortID());
                posStartCassetteInfo.setUnloadPortID(controlJobCassetteInfo.getUnloadPortID());
                List<ProductDTO.PosLotInCassetteInfo> lotInCassetteInfoList = new ArrayList<>();
                for (Infos.ControlJobCassetteLot controlJobCassetteLot : controlJobCassetteLotList){
                    ProductDTO.PosLotInCassetteInfo posLotInCassetteInfo = new ProductDTO.PosLotInCassetteInfo();
                    lotInCassetteInfoList.add(posLotInCassetteInfo);
                    posLotInCassetteInfo.setMonitorLotFlag(controlJobCassetteLot.isMonitorLotFlag());
                    posLotInCassetteInfo.setLotID(controlJobCassetteLot.getLotID());
                    posLotInCassetteInfo.setOperationStartFlag(false);
                    //------------------------------------------------------------------------//
                    //   Get lot object                                                       //
                    //------------------------------------------------------------------------//
                    CimLot aPosLot = baseCoreFactory.getBO(CimLot.class, controlJobCassetteLot.getLotID());
                    Validations.check(CimObjectUtils.isEmpty(aPosLot), retCodeConfig.getNotFoundLot());
                    CimControlJob aPosControlJob = aPosLot.getControlJob();
                    for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotSeqForOpeComp){
                        if (ObjectIdentifier.equalsWithValue(controlJobCassetteLot.getLotID(), partialOpeCompLot.getLotID())){
                            // this lot is going to perform partial opecomp
                            posLotInCassetteInfo.setOperationStartFlag(true);
                            if (aPosControlJob != null){
                                //-------------------------------
                                //  Create Target Wafer list
                                //-------------------------------
                                if (CimObjectUtils.isEmpty(waferIDs)){
                                    waferIDs.addAll(controlJobCassetteLot.getWaferIDs());
                                } else {
                                    waferIDs.addAll(controlJobCassetteLot.getWaferIDs());
                                }
                            }
                            break;
                        }
                    }
                }
                posStartCassetteInfo.setLotInCassetteInfo(lotInCassetteInfoList);
            }
        }
        //-----------------------------------------------------------
        //  Create sequence for setting controlJob cassette
        //  Lot entry whose cassetteID is blank should be removed
        //  (This mean stored lot in SLM operation)
        //-----------------------------------------------------------
        List<ProductDTO.PosStartCassetteInfo> tmpStartCassetteInfoSeqWithoutSLMStoredLot = new ArrayList<>();
        for (ProductDTO.PosStartCassetteInfo posStartCassetteInfo : tmpStartCassetteInfoList){
            if (!ObjectIdentifier.isEmptyWithValue(posStartCassetteInfo.getCassetteID())){
                tmpStartCassetteInfoSeqWithoutSLMStoredLot.add(posStartCassetteInfo);
            }
        }
        /*---------------------------*/
        /*  Get control job object   */
        /*---------------------------*/
        CimControlJob aControlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobInfo.getControlJobID());
        aControlJob.setStartCassetteInfo(tmpStartCassetteInfoSeqWithoutSLMStoredLot);
        // create control job lot
        aControlJob.removeAllControlJobLots();
        for (ProductDTO.PosStartCassetteInfo posStartCassetteInfo : tmpStartCassetteInfoList){
            for (ProductDTO.PosLotInCassetteInfo posLotInCassetteInfo : posStartCassetteInfo.getLotInCassetteInfo()){
                if (posLotInCassetteInfo.isOperationStartFlag()){
                    ProductDTO.LotInControlJobInfo lotInCJInfo = new ProductDTO.LotInControlJobInfo();
                    lotInCJInfo.setMonitorLotFlag(posLotInCassetteInfo.isMonitorLotFlag());
                    lotInCJInfo.setLotID(posLotInCassetteInfo.getLotID());
                    aControlJob.addControlJobLot(lotInCJInfo);
                }
            }
        }
        //-------------------------------
        //  Get Machine Object
        //-------------------------------
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, controlJobInfo.getMachineID());
        List<CimMachineContainerPosition> aMachineContainerPositionList = aMachine.findMachineContainerPositionForControlJob(controlJobInfo.getControlJobID().getValue());
        //------------------------------------------------
        //  Update Machine Container Position infomation
        //------------------------------------------------
        if (!CimObjectUtils.isEmpty(aMachineContainerPositionList)){
            for (CimMachineContainerPosition cimEquipmentContainerPositionDO : aMachineContainerPositionList){
                CimWafer aPosWafer = cimEquipmentContainerPositionDO.getWafer();
                if (aPosWafer == null){
                    continue;
                }
                String waferID = aPosWafer.getIdentifier();
                boolean partialCompWaferFlag = false;
                for (ObjectIdentifier waferID2 : waferIDs){
                    if (ObjectIdentifier.equalsWithValue(waferID, waferID2)){
                        // this wafer is going to perform partial opecomp
                        partialCompWaferFlag = true;
                        break;
                    }
                }
                if (partialCompWaferFlag){
                    cimEquipmentContainerPositionDO.setControlJob(aControlJob);

                } else {
                    cimEquipmentContainerPositionDO.setControlJob(null);
                }
            }
        }
        return;
    }

    @Override
    public List<ObjectIdentifier> controlJobRelatedReticlesGetDR(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID){
        /****************************************************/
        /*  Get all reticles related with input controlJob  */
        /****************************************************/
        String queySql = String.format("SELECT DISTINCT\n" +
                "                        OMPDRBL.PDRBL_ID,\n" +
                "                        OMPDRBL.ID\n" +
                "                  FROM  OMPDRBL, OMPDRBL_RSVCJ\n" +
                "                  WHERE OMPDRBL_RSVCJ.CJ_ID='%s'\n" +
                "                    AND OMPDRBL_RSVCJ.REFKEY=OMPDRBL.ID",
                ObjectIdentifier.fetchValue(controlJobID));
        List<Object[]> queryResult = cimJpaRepository.query(queySql);
        List<ObjectIdentifier> reticleIDList = new ArrayList<>();
        if (!CimObjectUtils.isEmpty(queryResult)){
            for (Object[] object : queryResult){
                reticleIDList.add(new ObjectIdentifier((String) object[0], (String) object[1]));
            }
        }
        return reticleIDList;
    }

    @Override
    public List<Infos.APCRunTimeCapabilityResponse> controlJobAPCRunTimeCapabilityGetDR(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID) {
        List<Infos.APCRunTimeCapabilityResponse> apcRunTimeCapabilityResponseList = new ArrayList<>();
        String sql;
        CimApcRunCapaDO cimApcRunCapaExam = new CimApcRunCapaDO();
        cimApcRunCapaExam.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
        List<CimApcRunCapaDO> apcRunCapaList = cimJpaRepository.findAll(Example.of(cimApcRunCapaExam));
        if (CimArrayUtils.isNotEmpty(apcRunCapaList)){
            for (CimApcRunCapaDO cimApcRunCapaDO : apcRunCapaList){
                boolean responseFlag = false;
                int response_cnt = CimArrayUtils.getSize(apcRunTimeCapabilityResponseList);
                int loop_response_cnt = 0;
                for (loop_response_cnt = 0; loop_response_cnt < response_cnt; loop_response_cnt++){
                    if (CimStringUtils.equals(cimApcRunCapaDO.getApcSystemName(), apcRunTimeCapabilityResponseList.get(loop_response_cnt).getStrAPCBaseIdentification().getSystemName())){
                        responseFlag = true;
                        break;
                    }
                }
                if (!responseFlag){
                    Infos.APCRunTimeCapabilityResponse apcRunTimeCapabilityResponse = new Infos.APCRunTimeCapabilityResponse();
                    apcRunTimeCapabilityResponseList.add(apcRunTimeCapabilityResponse);
                    Infos.APCBaseIdentification strAPCBaseIdentification = new Infos.APCBaseIdentification();
                    apcRunTimeCapabilityResponse.setStrAPCBaseIdentification(strAPCBaseIdentification);
                    strAPCBaseIdentification.setSystemName(cimApcRunCapaDO.getApcSystemName());
                }
                List<Infos.APCRunTimeCapability> strAPCRunTimeCapability = apcRunTimeCapabilityResponseList.get(loop_response_cnt).getStrAPCRunTimeCapability();
                int loop_capability_cnt = CimArrayUtils.getSize(strAPCRunTimeCapability);
                if (loop_capability_cnt == 0){
                    strAPCRunTimeCapability = new ArrayList<>();
                    apcRunTimeCapabilityResponseList.get(loop_response_cnt).setStrAPCRunTimeCapability(strAPCRunTimeCapability);
                }
                Infos.APCRunTimeCapability apcRunTimeCapability = new Infos.APCRunTimeCapability();
                strAPCRunTimeCapability.add(apcRunTimeCapability);
                List<Infos.APCLotWaferCollection> strAPCLotWaferCollection = new ArrayList<>();
                apcRunTimeCapability.setStrAPCLotWaferCollection(strAPCLotWaferCollection);

                CimApcRunCapaLotDO cimApcRunCapaLotExam = new CimApcRunCapaLotDO();
                cimApcRunCapaLotExam.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
                cimApcRunCapaLotExam.setApcSystemName(cimApcRunCapaDO.getApcSystemName());
                List<CimApcRunCapaLotDO> apcRunCapaLotList = cimJpaRepository.findAll(Example.of(cimApcRunCapaLotExam)).stream()
                        .sorted(Comparator.comparing(CimApcRunCapaLotDO::getWaferID))
                        .sorted(Comparator.comparing(CimApcRunCapaLotDO::getLotID))
                        .collect(Collectors.toList());

                if (CimArrayUtils.isNotEmpty(apcRunCapaLotList)){
                    for (CimApcRunCapaLotDO cimApcRunCapaLotDO : apcRunCapaLotList){
                        boolean lotFlag = false;
                        int lot_cnt = CimArrayUtils.getSize(strAPCLotWaferCollection);
                        int loop_lot_cnt = 0;
                        for (loop_lot_cnt = 0; loop_lot_cnt < lot_cnt; loop_lot_cnt++){
                            if (CimStringUtils.equals(cimApcRunCapaLotDO.getLotID(), strAPCLotWaferCollection.get(loop_lot_cnt).getLotID())){
                                lotFlag = true;
                                break;
                            }
                        }
                        if (!lotFlag){
                            Infos.APCLotWaferCollection apcLotWaferCollection = new Infos.APCLotWaferCollection();
                            apcLotWaferCollection.setLotID(cimApcRunCapaLotDO.getLotID());
                            strAPCLotWaferCollection.add(apcLotWaferCollection);
                        }
                        List<String> waferIDs = strAPCLotWaferCollection.get(loop_lot_cnt).getWaferID();
                        if (CimArrayUtils.isEmpty(waferIDs)){
                            waferIDs = new ArrayList<>();
                            strAPCLotWaferCollection.get(loop_lot_cnt).setWaferID(waferIDs);
                        }
                        int wafer_cnt = CimArrayUtils.getSize(waferIDs);
                        waferIDs.add(cimApcRunCapaLotDO.getWaferID());
                    }
                }
                List<Infos.APCSpecialInstruction> apcSpecialInstructionList = new ArrayList<>();
                apcRunTimeCapability.setStrAPCSpecialInstruction(apcSpecialInstructionList);

                CimApcRunCapaInstDO cimApcRunCapaInstExam = new CimApcRunCapaInstDO();
                cimApcRunCapaInstExam.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
                cimApcRunCapaInstExam.setApcSystemName(cimApcRunCapaDO.getApcSystemName());
                List<CimApcRunCapaInstDO> apcRunCapaInstList = cimJpaRepository.findAll(Example.of(cimApcRunCapaInstExam)).stream()
                        .sorted(Comparator.comparing(CimApcRunCapaInstDO::getValue))
                        .sorted(Comparator.comparing(CimApcRunCapaInstDO::getClassID))
                        .sorted(Comparator.comparing(CimApcRunCapaInstDO::getInstructionID))
                        .collect(Collectors.toList());
                if (CimArrayUtils.isNotEmpty(apcRunCapaInstList)){
                    for (CimApcRunCapaInstDO cimApcRunCapaInstDO : apcRunCapaInstList){
                        boolean instructionIDFlag = false;
                        int instruction_cnt = CimArrayUtils.getSize(apcSpecialInstructionList);
                        int loop_instruction_cnt = 0;
                        for (loop_instruction_cnt = 0; loop_instruction_cnt < instruction_cnt; loop_instruction_cnt++){
                            if (CimStringUtils.equals(cimApcRunCapaInstDO.getInstructionID(), apcSpecialInstructionList.get(loop_instruction_cnt).getInstructionID())){
                                instructionIDFlag = true;
                                break;
                            }
                        }
                        if (!instructionIDFlag){
                            Infos.APCSpecialInstruction apcSpecialInstruction = new Infos.APCSpecialInstruction();
                            apcSpecialInstructionList.add(apcSpecialInstruction);
                            apcSpecialInstruction.setInstructionID(cimApcRunCapaInstDO.getInstructionID());
                        }
                        List<Infos.APCBaseFactoryEntity> strAPCBaseFactoryEntity = apcSpecialInstructionList.get(loop_instruction_cnt).getStrAPCBaseFactoryEntity();
                        if (CimArrayUtils.isEmpty(strAPCBaseFactoryEntity)){
                            strAPCBaseFactoryEntity = new ArrayList<>();
                            apcSpecialInstructionList.get(loop_instruction_cnt).setStrAPCBaseFactoryEntity(strAPCBaseFactoryEntity);
                        }
                        Infos.APCBaseFactoryEntity apcBaseFactoryEntity = new Infos.APCBaseFactoryEntity();
                        strAPCBaseFactoryEntity.add(apcBaseFactoryEntity);
                        apcBaseFactoryEntity.setClassName(cimApcRunCapaInstDO.getClassID());
                        apcBaseFactoryEntity.setId(cimApcRunCapaInstDO.getValue());
                        apcBaseFactoryEntity.setAttrib(cimApcRunCapaInstDO.getAttribute());
                    }
                }
                boolean functionFlag = false;
                List<Infos.APCBaseAPCSystemFunction1> strAPCBaseAPCSystemFunction1 = strAPCRunTimeCapability.get(loop_capability_cnt).getStrAPCBaseAPCSystemFunction1();
                if (CimArrayUtils.isEmpty(strAPCBaseAPCSystemFunction1)){
                    strAPCBaseAPCSystemFunction1 = new ArrayList<>();
                    strAPCRunTimeCapability.get(loop_capability_cnt).setStrAPCBaseAPCSystemFunction1(strAPCBaseAPCSystemFunction1);
                }
                Infos.APCBaseAPCSystemFunction1 apcBaseAPCSystemFunction1 = new Infos.APCBaseAPCSystemFunction1();
                strAPCBaseAPCSystemFunction1.add(apcBaseAPCSystemFunction1);
                apcBaseAPCSystemFunction1.setType(cimApcRunCapaDO.getActionType());
                apcBaseAPCSystemFunction1.setControlFrequency(cimApcRunCapaDO.getCtrlFqequency());
                apcBaseAPCSystemFunction1.setDescription(cimApcRunCapaDO.getDescription());
                Infos.APCRunTimeCapabilityResponse apcRunTimeCapabilityResponse = apcRunTimeCapabilityResponseList.get(loop_response_cnt);
                Infos.APCBaseIdentification strAPCBaseIdentification = new Infos.APCBaseIdentification();
                apcRunTimeCapabilityResponse.setStrAPCBaseIdentification(strAPCBaseIdentification);
                strAPCBaseIdentification.setSystemName(cimApcRunCapaDO.getApcSystemName());
            }
        }
        return apcRunTimeCapabilityResponseList;
    }

    public void controlJobPortGroupSet(Infos.ObjCommon objCommon,ObjectIdentifier controlJobID,ObjectIdentifier portGroupID){
        //---------------------------------
        //  Get ControlJob Object
        //---------------------------------
        CimControlJob controlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        //---------------------------------
        //  Set PortGroup to ControlJob
        //---------------------------------
        controlJob.setPortGroup(portGroupID.getValue());
    }

}
