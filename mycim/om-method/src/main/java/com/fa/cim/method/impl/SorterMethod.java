package com.fa.cim.method.impl;

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
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.pos.CimProcessOperationSpecificationDO;
import com.fa.cim.entity.runtime.processflow.CimProcessFlowDO;
import com.fa.cim.entity.runtime.sortjob.CimSortJobComponentDO;
import com.fa.cim.entity.runtime.sortjob.CimSortJobDO;
import com.fa.cim.entity.runtime.sortjob.CimSortJobSlotMapDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessFlow;
import com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.bo.sorter.CimSorterJob;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.exceptions.NotFoundRecordException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.mtrlmngm.Material;
import com.fa.cim.newcore.standard.prcssdfn.ProcessFlow;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.math.BigDecimal;
import java.util.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/09/19        ********             Bear               create file
 *
 * @author Sun
 * @since 2018/09/19 09:47
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class SorterMethod  implements ISorterMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private IReticleMethod reticleMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private IConstraintMethod constraintMethod;

    @Override
    public List<Infos.SortJobListAttributes> sorterJobListGetDR(Infos.ObjCommon objCommon, Inputs.ObjSorterJobListGetDRIn objSorterJobListGetDRIn) {
        List<Infos.SortJobListAttributes> sorterJobListAttributesList = new ArrayList<>();
        boolean firstCondtionFlag = true;
        /* *****************************/
        /*   Check input parameter    */
        /* *****************************/
        if (ObjectIdentifier.isEmpty(objSorterJobListGetDRIn.getEquipmentID())
                && ObjectIdentifier.isEmpty(objSorterJobListGetDRIn.getCreateUser())
                && ObjectIdentifier.isEmpty(objSorterJobListGetDRIn.getSorterJob())
                && ObjectIdentifier.isEmpty(objSorterJobListGetDRIn.getCarrierID())
                && ObjectIdentifier.isEmpty(objSorterJobListGetDRIn.getLotID())) {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }
        /* ****************************/
        /*   Set input parameters    */
        /* ****************************/
        ObjectIdentifier equipmentID = objSorterJobListGetDRIn.getEquipmentID();
        ObjectIdentifier createUser = objSorterJobListGetDRIn.getCreateUser();
        ObjectIdentifier sorterJobID = objSorterJobListGetDRIn.getSorterJob();
        ObjectIdentifier carrierID = objSorterJobListGetDRIn.getCarrierID();
        ObjectIdentifier lotID = objSorterJobListGetDRIn.getLotID();
        Set<String> tmpSortJobIDList = new LinkedHashSet<>();
        if (!ObjectIdentifier.isEmptyWithValue(carrierID)) {
            /*------------------------------------------*/
            /*  Get carrierID From OMSORTJOB_COMP  */
            /*------------------------------------------*/
            String querySql = String.format(
                    "SELECT DISTINCT SORTER_JOB_ID FROM OMSORTJOB_COMP WHERE " +
                            "SRC_CARRIER_ID LIKE '%s' OR " +
                            "DEST_CARRIER_ID LIKE '%s'",
                    ObjectIdentifier.fetchValue(carrierID),
                    ObjectIdentifier.fetchValue(carrierID));
            List<Object[]> queryResult1 = cimJpaRepository.query(querySql);
            if(CimArrayUtils.isEmpty(queryResult1))  return sorterJobListAttributesList;
            for (Object[] object : queryResult1) {
                tmpSortJobIDList.add((String) object[0]);
            }
        }

        if (!ObjectIdentifier.isEmptyWithValue(lotID)) {
            /*------------------------------------*/
            /*  Get lotID From OMSORTJOB_SLOTMAP  */
            /*------------------------------------*/
            String querySql = String.format("SELECT DISTINCT SORTER_JOB_ID FROM OMSORTJOB_COMP_SLOTMAP WHERE " +
                    "LOT_ID LIKE '%s'", lotID.getValue());
            List<Object[]> queryResult2 = cimJpaRepository.query(querySql);
            if(CimArrayUtils.isEmpty(queryResult2)) return sorterJobListAttributesList;
            for (Object[] object : queryResult2) {
                String sortJobID = (String) object[0];
                tmpSortJobIDList.add(sortJobID);
            }
        }

        String tmpStr = "";
        if (!ObjectIdentifier.isEmptyWithValue(equipmentID)) {
            tmpStr += " WHERE";
            tmpStr += String.format(" EQP_ID LIKE '%s'", equipmentID.getValue());
            firstCondtionFlag = false;
        }
        if (!ObjectIdentifier.isEmptyWithValue(createUser)) {
            if (firstCondtionFlag) {
                firstCondtionFlag = false;
                tmpStr += " WHERE";
            } else {
                tmpStr += " AND";
            }
            tmpStr += String.format(" REQ_USER_ID LIKE '%s'", createUser.getValue());
            firstCondtionFlag = false;
        }
        if (!ObjectIdentifier.isEmptyWithValue(sorterJobID)) {
            if (firstCondtionFlag) {
                firstCondtionFlag = false;
                tmpStr += " WHERE";
            } else {
                tmpStr += " AND";
            }
            tmpStr += String.format(" SORTER_JOB_ID LIKE '%s'", sorterJobID.getValue());
        }

        //***************************/
        //*  Generate SQL sentence  */
        //***************************/
        String sql = "SELECT    SORTER_JOB_ID, " +
                "               EQP_ID, " +
                "               PORT_GROUP_ID, " +
                "               SORTER_JOB_STATUS, " +
                "               REQ_USER_ID, " +
                "               REQ_TIME, " +
                "               COMPONENT_JOB_COUNT, " +
                "               PREV_SORTER_JOB_ID, " +
                "               WAFER_ID_READ_FLAG," +
                "               SORTER_JOB_CATEGORY  " +
                "       FROM    OMSORTJOB" +
                tmpStr +
                "  ORDER BY EQP_ID, REQ_USER_ID, SORTER_JOB_ID";
        List<Object[]> queryResult = cimJpaRepository.query(sql);
        if (!CimObjectUtils.isEmpty(queryResult)) {
            for (Object[] object : queryResult) {
                String hFSSORTJOBSORTER_JOB_ID = (String) object[0];
                if (!CimObjectUtils.isEmpty(tmpSortJobIDList)) {
                    boolean breakFlag = false;
                    for (String tmpSortJobID : tmpSortJobIDList) {
                        if (CimStringUtils.equals(hFSSORTJOBSORTER_JOB_ID, tmpSortJobID)) {
                            breakFlag = true;
                            break;
                        }
                    }
                    if (!breakFlag) {
                        continue;
                    }
                }
                Infos.SortJobListAttributes sorterJobListAttributes = new Infos.SortJobListAttributes();
                sorterJobListAttributesList.add(sorterJobListAttributes);
                sorterJobListAttributes.setSorterJobID(ObjectIdentifier.buildWithValue(hFSSORTJOBSORTER_JOB_ID));
                sorterJobListAttributes.setEquipmentID(ObjectIdentifier.buildWithValue((String) object[1]));
                sorterJobListAttributes.setPortGroupID(ObjectIdentifier.buildWithValue((String) object[2]));
                sorterJobListAttributes.setSorterJobStatus((String) object[3]);
                sorterJobListAttributes.setRequestUserID(ObjectIdentifier.buildWithValue((String) object[4]));
                sorterJobListAttributes.setPreSorterJobID(ObjectIdentifier.buildWithValue((String) object[7]));
                sorterJobListAttributes.setRequestTimeStamp(String.valueOf(object[5]));
                sorterJobListAttributes.setComponentCount(CimNumberUtils.intValue((BigDecimal)object[6]));
                sorterJobListAttributes.setWaferIDReadFlag(CimNumberUtils.intValue((BigDecimal)object[8]) == 1);
                sorterJobListAttributes.setSorterJobCategory((String) object[9]);
                /*--------------------------------------------*/
                /* Get ComponentJob Infomation by sorterJobID */
                /*--------------------------------------------*/
                List<Infos.SorterComponentJobListAttributes> objSorterComponentJobInfoGetDROutRetCode = this.sorterComponentJobInfoGetDR(objCommon, sorterJobListAttributes.getSorterJobID());
                sorterJobListAttributes.setSorterComponentJobListAttributesList(objSorterComponentJobInfoGetDROutRetCode);
            }
        }
        /*--------------------------------------------------------------------------*/
        /* sorterJob information is returned in order of the priority of sorterJob. */
        /*--------------------------------------------------------------------------*/
        String tempEquipmentID = null;
        if (!ObjectIdentifier.isEmptyWithValue(equipmentID)) {
            tempEquipmentID = BaseStaticMethod.strrchr(equipmentID.getValue(), "%");
        }
        if (!ObjectIdentifier.isEmptyWithValue(equipmentID)
                && ObjectIdentifier.isEmptyWithValue(createUser)
                && ObjectIdentifier.isEmptyWithValue(sorterJobID)
                && CimStringUtils.isEmpty(tempEquipmentID)
                && ObjectIdentifier.isEmptyWithValue(lotID)
                && ObjectIdentifier.isEmptyWithValue(carrierID)) {
            int s_len = CimArrayUtils.getSize(sorterJobListAttributesList);
            List<Infos.SortJobListAttributes> tmpSortJobListAttributesList = new ArrayList<>();
            String tmpJobID = "";
            //preSorterJobID looks for what is Blank
            for (Infos.SortJobListAttributes sortJobListAttributes : sorterJobListAttributesList) {
                if (ObjectIdentifier.isEmptyWithValue(sortJobListAttributes.getPreSorterJobID())) {
                    tmpSortJobListAttributesList.add(sortJobListAttributes);
                    tmpJobID = sortJobListAttributes.getSorterJobID().getValue();
                    break;
                }
            }
            //Sort by sorter Job ID
            for (int i = 1; i < s_len; i++) {
                for (int k = 0; k < s_len; k++) {
                    if (CimStringUtils.equals(sorterJobListAttributesList.get(k).getPreSorterJobID().getValue(), tmpJobID)) {
                        tmpSortJobListAttributesList.add(sorterJobListAttributesList.get(k));
                        tmpJobID = sorterJobListAttributesList.get(k).getSorterJobID().getValue();
                        break;
                    }
                }
            }
            sorterJobListAttributesList = tmpSortJobListAttributesList;
        }
        return sorterJobListAttributesList;
    }

    @Override
    public String reqCategoryGetByLot(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        //find next pos
        CimProcessOperationSpecification currentPOS = aLot.getProcessOperation().getProcessOperationSpecification();
        if (CimObjectUtils.isEmpty(currentPOS)){
            if (log.isInfoEnabled()){
                log.info("lotContaminationLevelAndPrFlagSet->info: the processOperationSpecification is empty!");
            }
            return null;
        }
        ProcessFlow processFlow = aLot.getProcessFlow();
        CimProcessFlow pf = (CimProcessFlow)processFlow;
        CimProcessFlowDO example = new CimProcessFlowDO();
        example.setId(pf.getPrimaryKey());
        CimProcessFlowDO processFlowDO = cimJpaRepository.findOne(Example.of(example)).orElse(null);
        String sql = "SELECT\n" +
                "    pos.*\n" +
                "FROM\n" +
                "    OMPRF a,\n" +
                "    OMPRF_PRSSSEQ b,\n" +
                "    OMPRSS pos\n" +
                "WHERE\n" +
                "    a.PRP_ID = ?1 \n" +
                "AND a.PRP_LEVEL = 'Main_Ope'\n" +
                "AND a.id = b.refkey\n" +
                "AND b.PRSS_RKEY = pos.id\n" +
                "ORDER BY pos.OPE_NO " ;
        List<CimProcessOperationSpecificationDO> cimProcessOperationSpecificationDOS =
                cimJpaRepository.query(sql, CimProcessOperationSpecificationDO.class, processFlowDO.getMainProcessDefinitionID());
        Iterator<CimProcessOperationSpecificationDO> iterator = cimProcessOperationSpecificationDOS.iterator();
        while (iterator.hasNext()){
            CimProcessOperationSpecificationDO next = iterator.next();
            if (CimStringUtils.equals(next.getOperationNumber(),currentPOS.getOperationNumber())){
                //get next pos
                if (iterator.hasNext()){
                    CimProcessOperationSpecificationDO targetPOS = iterator.next();
                    return targetPOS.getRequiredCassetteCategory();
                }else {
                    //last step
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public List<Infos.SorterComponentJobListAttributes> sorterComponentJobInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterJobID) {
        String querySql = String.format("SELECT A.COMPONENT_JOB_ID,\n" +
                "                            A.JOB_SEQ,\n" +
                "                            A.COMPONENT_JOB_STATUS,\n" +
                "                            A.SRC_CARRIER_ID,\n" +
                "                            A.DEST_CARRIER_ID,\n" +
                "                            A.SRC_PORT_ID,\n" +
                "                            A.DEST_PORT_ID,\n" +
                "                            B.EQP_ID,\n" +
                "                            B.PORT_GROUP_ID,\n" +
                "                            B.REQ_TIMESTAMP\n" +
                "                            A.REFKEY\n" +
                "                     FROM   OMSORTJOB_COMP A,\n" +
                "                            OMSORTJOB B\n" +
                "                     WHERE  A.SORTER_JOB_ID     = '%s'\n" +
                "                     AND    A.REFKEY     = B.ID", sorterJobID.getValue());
        List<Object[]> queryResult = cimJpaRepository.query(querySql);
        if (CimObjectUtils.isEmpty(queryResult)) {
            return null;
        }
        List<Infos.SorterComponentJobListAttributes> sorterComponentJobListAttributesList = new ArrayList<>();
        ObjectIdentifier tmpComponentJob = null;
        for (Object[] object : queryResult) {
            Infos.SorterComponentJobListAttributes sorterComponentJobListAttributes = new Infos.SorterComponentJobListAttributes();
            sorterComponentJobListAttributesList.add(sorterComponentJobListAttributes);
            sorterComponentJobListAttributes.setSorterComponentJobID(new ObjectIdentifier((String) object[0]));
            sorterComponentJobListAttributes.setOriginalCarrierID(new ObjectIdentifier((String) object[3]));
            sorterComponentJobListAttributes.setOriginalPortID(new ObjectIdentifier((String) object[5]));
            sorterComponentJobListAttributes.setDestinationCarrierID(new ObjectIdentifier((String) object[4]));
            sorterComponentJobListAttributes.setDestinationPortID(new ObjectIdentifier((String) object[6]));
            sorterComponentJobListAttributes.setComponentSorterJobStatus((String) object[2]);
            sorterComponentJobListAttributes.setPreSorterComponentJobID(tmpComponentJob);
            tmpComponentJob = sorterComponentJobListAttributes.getSorterComponentJobID();
            sorterComponentJobListAttributes.setRequestTimeStamp(String.valueOf(object[9]));
            //----------------------------------------------------------
            //  Set Next PREV_COMPONENTJOB_ID
            //----------------------------------------------------------

            //----------------------------------------------------------
            //  Get Xfer State for original cassette
            //----------------------------------------------------------
            String hFSSORTJOB_COMPONENTSRC_CAST_ID = (String) object[3];
            Outputs.ObjCassetteTransferInfoGetDROut objCassetteTransferInfoGetDROut1 = cassetteMethod.cassetteTransferInfoGetDR(objCommon, new ObjectIdentifier(hFSSORTJOB_COMPONENTSRC_CAST_ID));
            sorterComponentJobListAttributes.setOriginalCarrierTransferState(objCassetteTransferInfoGetDROut1.getTransferStatus());
            sorterComponentJobListAttributes.setOriginalCarrierEquipmentID(objCassetteTransferInfoGetDROut1.getEquipmentID());
            sorterComponentJobListAttributes.setOriginalCarrierStockerID(objCassetteTransferInfoGetDROut1.getStockerID());
            //----------------------------------------------------------
            //  Get Xfer State for destination cassette
            //----------------------------------------------------------
            String hFSSORTJOB_COMPONENTDEST_CAST_ID = (String) object[4];
            Outputs.ObjCassetteTransferInfoGetDROut objCassetteTransferInfoGetDROut2 = cassetteMethod.cassetteTransferInfoGetDR(objCommon, new ObjectIdentifier(hFSSORTJOB_COMPONENTDEST_CAST_ID));
            sorterComponentJobListAttributes.setDestinationCarrierTranferStatus(objCassetteTransferInfoGetDROut2.getTransferStatus());
            sorterComponentJobListAttributes.setDestinationCarrierEquipmentID(objCassetteTransferInfoGetDROut2.getEquipmentID());
            sorterComponentJobListAttributes.setDestinationCarrierStockerID(objCassetteTransferInfoGetDROut2.getStockerID());
            //----------------------------------------------------------
            //  Get Sorter Component Job Detail Information
            //----------------------------------------------------------
            CimSortJobSlotMapDO cimSortJobSlotMapExam = new CimSortJobSlotMapDO();
            cimSortJobSlotMapExam.setReferenceKey((String) object[10]);
            List<CimSortJobSlotMapDO> cimSortJobSlotMapDOList = cimJpaRepository.findAll(Example.of(cimSortJobSlotMapExam));
            if (!CimObjectUtils.isEmpty(cimSortJobSlotMapDOList)) {
                List<Infos.WaferSorterSlotMap> waferSorterSlotMapList = new ArrayList<>();
                sorterComponentJobListAttributes.setWaferSorterSlotMapList(waferSorterSlotMapList);
                for (CimSortJobSlotMapDO cimSortJobSlotMapDO : cimSortJobSlotMapDOList) {
                    Infos.WaferSorterSlotMap waferSorterSlotMap = new Infos.WaferSorterSlotMap();
                    waferSorterSlotMapList.add(waferSorterSlotMap);
                    waferSorterSlotMap.setWaferID(ObjectIdentifier.buildWithValue(cimSortJobSlotMapDO.getWaferID()));
                    waferSorterSlotMap.setLotID(ObjectIdentifier.buildWithValue(cimSortJobSlotMapDO.getLotID()));
                    waferSorterSlotMap.setDestinationSlotNumber(CimNumberUtils.longValue(cimSortJobSlotMapDO.getDestPosition()));
                    waferSorterSlotMap.setOriginalSlotNumber(CimNumberUtils.longValue(cimSortJobSlotMapDO.getSourcePosition()));
                    waferSorterSlotMap.setEquipmentID(ObjectIdentifier.buildWithValue((String) object[7]));
                    waferSorterSlotMap.setPortGroup((String) object[8]);
                    waferSorterSlotMap.setRequestTime(String.valueOf(object[9]));
                    waferSorterSlotMap.setAliasName(cimSortJobSlotMapDO.getAliasName());
                    //auto sorter 情况下默认 LotTransfer
                    waferSorterSlotMap.setActionCode("LotTransfer");
                }
            }
        }
        return sorterComponentJobListAttributesList;
    }

    @Override
    public Outputs.ObjSorterWaferTransferInfoRestructureOut sorterWaferTransferInfoRestructure(Infos.ObjCommon objCommon, List<Infos.WaferTransfer> waferXferList) {
        Outputs.ObjSorterWaferTransferInfoRestructureOut data = new Outputs.ObjSorterWaferTransferInfoRestructureOut();

        //Prepare work structure for Output structure
        List<String> strLotInventoryStateList = new ArrayList<>();
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        List<Infos.PLot> pLotList = new ArrayList<>();
        int lenWaferXferSeq = CimArrayUtils.getSize(waferXferList);
        //Create output structure
        for (int i = 0; i < lenWaferXferSeq; i++) {
            Infos.WaferTransfer waferTransfer = waferXferList.get(i);
            //Prepare output structure of lot_wafer_Create() here
            com.fa.cim.newcore.bo.product.CimLot lot;
            com.fa.cim.newcore.bo.durable.CimCassette fromCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, waferTransfer.getOriginalCassetteID());

            com.fa.cim.newcore.bo.durable.CimCassette aToCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, waferTransfer.getDestinationCassetteID());
            com.fa.cim.newcore.bo.product.CimWafer wafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, waferXferList.get(i).getWaferID());
            Validations.check(CimObjectUtils.isEmpty(wafer), retCodeConfig.getNotFoundWafer());
            boolean waferIsNewlyCreatedHere = false;
            Outputs.ObjLotWaferCreateOut objLotWaferCreateOut = null;

            //input waferID is not exist case
            if (wafer == null) {
                log.info("sorterWaferTransferInfoRestructure(): wafer is null");
                //Check condition
                Validations.check(null != fromCassette, retCodeConfig.getNotFoundCassette());
                lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, waferXferList.get(i).getOriginalCassetteID());
                Validations.check(lot == null, retCodeConfig.getNotFoundLot());
                String strSourceLotType = lot.getLotType();
                Validations.check(!CimStringUtils.equals(strSourceLotType, BizConstant.SP_LOT_TYPE_VENDORLOT), retCodeConfig.getInvalidLotType());
                //Create wafer, lot_wafer_Create
                objLotWaferCreateOut = lotMethod.lotWaferCreate(objCommon, waferXferList.get(i).getOriginalCassetteID(), waferXferList.get(i).getWaferID().getValue());
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
            com.fa.cim.newcore.bo.pd.CimProcessOperation tmpPO = lot.getProcessOperation();
            if (CimStringUtils.equals(tmpLotInventoryState, BizConstant.SP_LOT_INVENTORYSTATE_INBANK) && tmpPO != null) {
                strLotInventoryState = BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR;
            } else {
                strLotInventoryState = tmpLotInventoryState;
            }
            //Prepare temporary lotID structure
            ObjectIdentifier tmpLotID;
            if (CimBooleanUtils.isTrue(waferIsNewlyCreatedHere)) {
                tmpLotID = waferXferList.get(i).getOriginalCassetteID();
            } else {
                tmpLotID = lot.getLotID();
            }
            //Create output structure
            boolean waferAdded = false;
            if (!CimArrayUtils.isEmpty(pLotList)) {
                for (int j = 0; j < pLotList.size(); j++) {
                    Infos.PLot pLot = pLotList.get(j);
                    ;
//                    List<Infos.PWafer> waferList = new ArrayList<>();
//                    pLot.setWaferList(waferList);
                    Infos.PWafer pWafer = new Infos.PWafer();
                    if (CimStringUtils.equals(pLot.getLotID().getValue(), tmpLotID.getValue())) {
                        if (!CimStringUtils.equals(cassetteIDList.get(j).getValue(), waferXferList.get(i).getDestinationCassetteID().getValue())
                                && !CimStringUtils.equals(strLotInventoryStateList.get(j), BizConstant.SP_LOT_INVENTORYSTATE_INBANK)) {
                            Validations.check(retCodeConfig.getInvalidCassetteLotRelation(), ObjectIdentifier.fetchValue(pLot.getLotID()));
                        } else if (CimStringUtils.equals(cassetteIDList.get(j).getValue(), waferXferList.get(i).getDestinationCassetteID().getValue())
                                && !CimStringUtils.equals(strLotInventoryStateList.get(j), BizConstant.SP_LOT_INVENTORYSTATE_INBANK)) {
                            pWafer.setWaferID(waferXferList.get(i).getWaferID());
                            pWafer.setSlotNumber(waferXferList.get(i).getDestinationSlotNumber());
                            waferAdded = true;
                            pLot.getWaferList().add(pWafer);
                            break;
                        } else if (CimStringUtils.equals(cassetteIDList.get(j).getValue(), waferXferList.get(i).getDestinationCassetteID().getValue())
                                && CimStringUtils.equals(strLotInventoryStateList.get(j), BizConstant.SP_LOT_INVENTORYSTATE_INBANK)) {
                            if (CimBooleanUtils.isTrue(waferIsNewlyCreatedHere)) {
                                pWafer.setWaferID(objLotWaferCreateOut == null ? null : objLotWaferCreateOut.getNewWaferID());
                                pWafer.setSlotNumber(waferXferList.get(i).getDestinationSlotNumber());
                                pLot.getWaferList().add(pWafer);
                            } else {
                                pWafer.setWaferID(waferXferList.get(i).getWaferID());
                                pWafer.setSlotNumber(waferXferList.get(i).getDestinationSlotNumber());
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
                cassetteIDList.add(waferXferList.get(i).getDestinationCassetteID());
                Infos.PLot pLot = new Infos.PLot();
                pLot.setLotID(tmpLotID);
                List<Infos.PWafer> waferList = new ArrayList<>();
                pLot.setWaferList(waferList);
                Infos.PWafer pWafer = new Infos.PWafer();
                waferList.add(pWafer);
                if (CimBooleanUtils.isTrue(waferIsNewlyCreatedHere)) {
                    pWafer.setWaferID(objLotWaferCreateOut == null ? null : objLotWaferCreateOut.getNewWaferID());
                    pWafer.setSlotNumber(waferXferList.get(i).getDestinationSlotNumber());
                } else {
                    pWafer.setWaferID(waferXferList.get(i).getWaferID());
                    pWafer.setSlotNumber(waferXferList.get(i).getDestinationSlotNumber());
                }
                pLotList.add(pLot);
            }
        }
        data.setCassetteIDList(cassetteIDList);
        data.setLotInventoryStateList(strLotInventoryStateList);
        data.setLotList(pLotList);
        return data;
    }

    @Override
    public void sorterWaferTransferInfoVerify(Infos.ObjCommon objCommon, List<Infos.WaferTransfer> waferXferList, String originalLocationVerify) {
        //--------------------------------------------------------
        // Check Current Condition of wafer - carrier - position
        //--------------------------------------------------------
        int nInputWaferLen = CimArrayUtils.getSize(waferXferList);
        List<String> cassetteIDs = new ArrayList<>();
        List<String> lotIDs = new ArrayList<>();
        List<com.fa.cim.newcore.bo.product.CimLot> aPosLotSeq = new ArrayList<>();
        ObjectIdentifier targetlotID;
        Boolean bWaferFoundFlg = false;
        Boolean scrapState = false;
        for (int i = 0; i < nInputWaferLen; i++) {
            Infos.WaferTransfer waferTransfer = waferXferList.get(i);
            if (ObjectIdentifier.equalsWithValue(waferTransfer.getDestinationCassetteID(), waferTransfer.getOriginalCassetteID())) {
                log.info("destinationCassetteID == originalCassetteID");
                continue;
            }
            //---------------------------------------------------
            // Get Target LotID
            //---------------------------------------------------
            Boolean waferFound = false;
            com.fa.cim.newcore.bo.product.CimWafer aPosWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, waferTransfer.getWaferID());
            Validations.check(null == aPosWafer, retCodeConfig.getNotFoundWafer());
            scrapState = aPosWafer.isScrap();
            if (CimBooleanUtils.isTrue(scrapState) && CimBooleanUtils.isFalse(waferTransfer.getBDestinationCassetteManagedByOM())) {
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
            if (CimBooleanUtils.isFalse(waferTransfer.getBOriginalCassetteManagedByOM())) {
                log.info("Original CassetteID is UnKnown");
                continue;
            }
            //---------------------------------------------------
            // Get Slot Map On MM Server (originalCassetteID Key)
            //---------------------------------------------------
            List<Infos.WaferMapInCassetteInfo> strCassetteGetWaferMapDROut = cassetteMethod.cassetteGetWaferMapDR(objCommon, waferTransfer.getOriginalCassetteID());

            int waferMapLen = CimArrayUtils.getSize(strCassetteGetWaferMapDROut);
            for (int j = 0; j < waferMapLen; j++) {
                Infos.WaferMapInCassetteInfo waferMapInCassetteInfo = strCassetteGetWaferMapDROut.get(j);
                if (ObjectIdentifier.equalsWithValue(waferMapInCassetteInfo.getLotID(), targetlotID)) {
                    bWaferFoundFlg = false;
                    for (int k = 0; k < nInputWaferLen; k++) {
                        if (CimBooleanUtils.isTrue(scrapState)) {
                            if (ObjectIdentifier.equalsWithValue(waferMapInCassetteInfo.getWaferID(), waferXferList.get(k).getWaferID())
                                    && CimStringUtils.equals(BizConstant.SP_SCRAPSTATE_SCRAP, waferMapInCassetteInfo.getScrapState())) {
                                log.info("bWaferFoundFlg = TRUE");
                                bWaferFoundFlg = true;
                                break;
                            }
                        } else {
                            if (ObjectIdentifier.equalsWithValue(waferMapInCassetteInfo.getWaferID(), waferXferList.get(k).getWaferID())) {
                                log.info("bWaferFoundFlg = TRUE");
                                bWaferFoundFlg = true;
                                break;
                            }
                        }
                    }
                    //---------------------------------------------------
                    // Check Exsit WaferID
                    //---------------------------------------------------
                    Validations.check(CimBooleanUtils.isFalse(bWaferFoundFlg), retCodeConfig.getInvalidCassetteLotRelation(), ObjectIdentifier.fetchValue(waferMapInCassetteInfo.getLotID()));
                }
            }
        }
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
        List<ObjectIdentifier> slotMapCassetteIDs = new ArrayList<>();
        int cassetteKind = 0;
        int sameFlag = 0;
        List<Infos.CurrentSlotMapInfo> strCurrentSlotMapInfo = new ArrayList<>();
        List<Infos.WaferSorterSlotMap> strWaferSorterSlotMapToCheck = new ArrayList<>();
        // ----------------------------------------------------------
        // Change checklogic of current wafer's position D4000135
        // Getting originalCassetteID and get each slotmap information
        // ----------------------------------------------------------
        if (CimStringUtils.equals(originalLocationVerify, BizConstant.SP_SORTER_LOCATION_CHECKBY_SLOTMAP)) {
            log.info("Check method is by slotmap");
            for (int i = 0; i < nInputWaferLen; i++) {
                if (0 == i) {
                    slotMapCassetteIDs.add(waferXferList.get(i).getOriginalCassetteID());
                    cassetteKind++;
                } else {
                    sameFlag = 0;
                    for (int j = 0; j < cassetteKind; j++) {
                        if (ObjectIdentifier.equalsWithValue(slotMapCassetteIDs.get(j), waferXferList.get(i).getOriginalCassetteID())) {
                            sameFlag = 1;
                        }
                    }
                    // ----------------------------------------------------------------
                    // Case if found any cassette that are not add to slotMapCassetteIDs
                    // then add to slotMapCassetteIDs
                    // -----------------------------------------------------------------
                    if (0 == sameFlag) {
                        slotMapCassetteIDs.add(waferXferList.get(i).getOriginalCassetteID());
                    }
                }
            }
            // ----------------------------------------------------------
            // Check slotmap is existed ior not on slotMap by CassetteIDs
            // ----------------------------------------------------------
            for (int i = 0; i < cassetteKind; i++) {
                //================================================================
                // Checking Previous Job is running or not
                //================================================================
                Infos.WaferSorterSlotMap strWaferSorterSlotMap = new Infos.WaferSorterSlotMap();
                //================================================================
                // Check destination cassette existence. If not, return error.
                //================================================================
                Validations.check(CimArrayUtils.isEmpty(slotMapCassetteIDs), retCodeConfig.getInvalidParameter());
                //------------------------------------------------------
                //   Setting Search Condition Value
                //   Condition
                //   sorterStatus = "Succeeded"
                //   direction    = "EAP"
                //   ActionCode   = "SP_Sorter_Read"
                //   This condition means Sort request was submitted,
                //   but EAP Responce is not received
                //------------------------------------------------------
                strWaferSorterSlotMap.setDirection(BizConstant.SP_SORTER_DIRECTION_TCS);
                strWaferSorterSlotMap.setSorterStatus(BizConstant.SP_SORTER_SUCCEEDED);
                //------------------------------------------------------
                //   Setting Not Search Condition Value
                //------------------------------------------------------
                strWaferSorterSlotMap.setDestinationSlotNumber(0L);
                strWaferSorterSlotMap.setOriginalSlotNumber(0L);
                strWaferSorterSlotMap.setDestinationCassetteID(slotMapCassetteIDs.get(i));
                strWaferSorterSlotMap.setDestinationCassetteManagedByOM(false);
                strWaferSorterSlotMap.setDestinationCassetteManagedByOM(false);
                //-----------------------------------------------------------------------
                //   Search From Flotmap DB
                //   => Slotmap contains latest slotmap information
                //      If there are any information in slotmap
                //      then treat current position is same as slotmap information
                //------------------------------------------------------------------------
                List<Infos.WaferSorterSlotMap> strWaferSorterSlotMapSelectDROut = null; //TODO-NOTIMPL(暂时不使用FS的表): waferMethod.waferSorterSlotMapSelectDR(objCommon, ConstEnum.SP_Sorter_SlotMap_LatestData.toString(), "", ConstEnum.SP_Sorter_Ignore_SiViewFlag.toString(),
                try {
                    strWaferSorterSlotMapSelectDROut = waferMethod.waferSorterSlotMapSelectDR(objCommon, BizConstant.SP_SORTER_SLOTMAP_LATESTDATA, "", BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG,
                            BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG, strWaferSorterSlotMap);
                } catch (ServiceException e) {
                    //------------------------------------------------------
                    // When Error Occures.// handled in the method by exception
                    //------------------------------------------------------
                    if (retCodeConfigEx.getNotFoundSlotMapRecord().getCode() != e.getCode()) {
                        throw e;
                    }
                }

                //--------------------------------------------------------
                // Check SlotMapResult is WaferIDRead or not
                //--------------------------------------------------------
                int slotMapLen = CimArrayUtils.getSize(strWaferSorterSlotMapSelectDROut);
                int actionFound = 0;
                for (int j = 0; j < slotMapLen; j++) {
                    //--------------------------------------------------------------------
                    // If there are actionCode that is not SP_Sorter_Read
                    //--------------------------------------------------------------------
                    if (CimStringUtils.equals(BizConstant.SP_SORTER_READ, strWaferSorterSlotMapSelectDROut.get(j).getActionCode())||
                            CimStringUtils.equals(BizConstant.SP_SORTER_START, strWaferSorterSlotMapSelectDROut.get(j).getActionCode())) {
                        actionFound = 1;
                        break;
                    }
                }
                //-------------------------------------------------------------------------
                // Case if there are WaferIDRead data in slotmap , then copy to
                // strCurrentSlotMapInfo[i].strWaferSorterSlotMapSequence
                // And if there is no data in slotmap then
                // length of strCurrentSlotMapInfo[i].strWaferSorterSlotMapSequence is 0
                //-------------------------------------------------------------------------
                Infos.CurrentSlotMapInfo currentSlotMapInfo = new Infos.CurrentSlotMapInfo();
                currentSlotMapInfo.setCassetteID(slotMapCassetteIDs.get(i));
                if (1 != actionFound) {
                    currentSlotMapInfo.setStrWaferSorterSlotMapSequence(strWaferSorterSlotMapSelectDROut);
                }
                strCurrentSlotMapInfo.add(currentSlotMapInfo);
            }
        }
        //--------------------------------------------------------------
        // End of  D4000135
        //--------------------------------------------------------------
        for (int i = 0; i < nInputWaferLen; i++) {
            Validations.check(ObjectIdentifier.isEmpty(waferXferList.get(i).getWaferID()), retCodeConfig.getWaferIDBlank());
            Boolean waferFound = false;
            com.fa.cim.newcore.bo.product.CimWafer aPosWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, waferXferList.get(i).getWaferID());
            if (null != aPosWafer) {
                waferFound = true;
            }
            Boolean cassetteFound = false;
            Boolean curCarrierFound = false;
            com.fa.cim.newcore.bo.durable.CimCassette anInputOrgCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, waferXferList.get(i).getOriginalCassetteID());
            if (null != anInputOrgCassette) {
                cassetteFound = true;
            }
            curCarrierFound = cassetteFound;
            cassetteFound = false;
            Boolean destCarrierFound = false;
            com.fa.cim.newcore.bo.durable.CimCassette anInputDestCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, waferXferList.get(i).getDestinationCassetteID());
            if (null != anInputDestCassette) {
                cassetteFound = true;
            }
            destCarrierFound = cassetteFound;
            Validations.check(waferXferList.get(i).getBOriginalCassetteManagedByOM() && !curCarrierFound, retCodeConfig.getNotFoundCassette());
            Validations.check(!waferXferList.get(i).getBOriginalCassetteManagedByOM() && curCarrierFound, retCodeConfig.getCassetteExist());

            Validations.check(waferXferList.get(i).getBDestinationCassetteManagedByOM() && !destCarrierFound, retCodeConfig.getNotFoundCassette());
            Validations.check(!waferXferList.get(i).getBDestinationCassetteManagedByOM() && destCarrierFound, retCodeConfig.getCassetteExist());
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
                Validations.check(CimBooleanUtils.isFalse(lotFound) || CimBooleanUtils.isTrue(waferXferList.get(i).getBOriginalCassetteManagedByOM()) || CimBooleanUtils.isTrue(curCarrierFound), retCodeConfig.getNotFoundMaterialLocation());
            } else {
                MaterialContainer aMtrlCntnr = aPosWafer.getMaterialContainer();
                com.fa.cim.newcore.bo.durable.CimCassette aCurCassette = (com.fa.cim.newcore.bo.durable.CimCassette) aMtrlCntnr;
                if (null == aCurCassette && null == anInputOrgCassette && !waferXferList.get(i).getBOriginalCassetteManagedByOM()) {
                    //ok
                } else if (null != aCurCassette && null != anInputOrgCassette && waferXferList.get(i).getBOriginalCassetteManagedByOM()) {
                    String strCurrentCassetteID = aCurCassette.getIdentifier();
                    Integer nCurPosition = aPosWafer.getPosition();
                    // --------------------------------------------------
                    // D4000135
                    // When originalLocationVerify == SP_Sorter_Location_CheckBy_SlotMap
                    // If find carrier information in slotmap, then check by slotmap
                    // Otherwise , checked by mm information
                    // --------------------------------------------------
                    int checkBySlotMap = 0;
                    if (CimStringUtils.equals(BizConstant.SP_SORTER_LOCATION_CHECKBY_SLOTMAP, originalLocationVerify)) {
                        for (int j = 0; j < cassetteKind; j++) {
                            if (ObjectIdentifier.equalsWithValue(strCurrentSlotMapInfo.get(j).getCassetteID(), waferXferList.get(i).getOriginalCassetteID())) {
                                if (0 != CimArrayUtils.getSize(strCurrentSlotMapInfo.get(j).getStrWaferSorterSlotMapSequence())) {
                                    strWaferSorterSlotMapToCheck = strCurrentSlotMapInfo.get(j).getStrWaferSorterSlotMapSequence();
                                    checkBySlotMap = 1;
                                }
                            }
                        }
                    }
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
                            if (ObjectIdentifier.equalsWithValue(strWaferSorterSlotMapToCheck.get(j).getDestinationCassetteID(), waferXferList.get(i).getOriginalCassetteID())
                                    && ObjectIdentifier.equalsWithValue(strWaferSorterSlotMapToCheck.get(j).getWaferID(), waferXferList.get(i).getWaferID())
                                    && strWaferSorterSlotMapToCheck.get(j).getDestinationSlotNumber().intValue() == waferXferList.get(i).getOriginalSlotNumber().intValue()) {
                                foundWafer = 1;
                            }
                        }
                        //-------------------------------------------------------------
                        // If no exist CassetteID/WaferID/SlotNumber, then error
                        // ------------------------------------------------------------
                        Validations.check(1 != foundWafer, retCodeConfig.getInvalidOrgWaferPosition(),
                                waferXferList.get(i).getWaferID().getValue(),
                                waferXferList.get(i).getOriginalCassetteID().getValue(),
                                waferXferList.get(i).getOriginalSlotNumber());
                    } else {
                        Validations.check(!ObjectIdentifier.equalsWithValue(strCurrentCassetteID, waferXferList.get(i).getOriginalCassetteID())
                                        || nCurPosition.intValue() != waferXferList.get(i).getOriginalSlotNumber().intValue(), retCodeConfig.getInvalidOrgWaferPosition(),
                                waferXferList.get(i).getWaferID().getValue(),
                                waferXferList.get(i).getOriginalCassetteID().getValue(),
                                waferXferList.get(i).getOriginalSlotNumber());
                    }
                } else {
                    Validations.check(true, retCodeConfig.getInvalidOrgWaferPosition(),
                            waferXferList.get(i).getWaferID().getValue(),
                            waferXferList.get(i).getOriginalCassetteID().getValue(),
                            waferXferList.get(i).getOriginalSlotNumber());
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
        //--------------------------------------------------------
        // Check condition if wafer's carrier will be changed
        //--------------------------------------------------------
        for (int i = 0; i < nInputWaferLen; i++) {
            if (waferXferList.get(i).getBOriginalCassetteManagedByOM()) {
                CimWafer anInputWafer = baseCoreFactory.getBO(CimWafer.class, waferXferList.get(i).getWaferID());
                Boolean bInWaferScrapState = anInputWafer.isScrap();
                if (CimBooleanUtils.isTrue(bInWaferScrapState)) {
                    continue;
                }
            }
            if (!ObjectIdentifier.equalsWithValue(cassetteIDs.get(i), waferXferList.get(i).getDestinationCassetteID())) {
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
                            if (ObjectIdentifier.equalsWithValue(strLotWaferID, waferXferList.get(k).getWaferID())) {
                                bLotWaferFound = true;
                                break;
                            }
                        }
                        Validations.check(CimBooleanUtils.isFalse(bLotWaferFound), retCodeConfig.getInvalidOrgWaferPosition());
                        Validations.check(!ObjectIdentifier.equalsWithValue(waferXferList.get(i).getDestinationCassetteID(), waferXferList.get(k).getDestinationCassetteID()), retCodeConfig.getInvalidOrgWaferPosition());
                    }
                }
            }
        }
        //----------------------------------------------------
        // Extract input structure that system currently cannot
        // find out destination slot is empty or not
        //----------------------------------------------------
        List<Infos.WaferTransfer> tmpWaferXferSeq = new ArrayList<>();
        int lenWaferXferSeq = CimArrayUtils.getSize(waferXferList);
        for (int i = 0; i < lenWaferXferSeq; i++) {
            Boolean bWaferExchangeFlag = false;
            for (int j = 0; j < lenWaferXferSeq; j++) {
                if (ObjectIdentifier.equalsWithValue(waferXferList.get(i).getDestinationCassetteID(), waferXferList.get(j).getOriginalCassetteID())
                        && waferXferList.get(i).getDestinationSlotNumber().intValue() == waferXferList.get(j).getOriginalSlotNumber().intValue()) {
                    bWaferExchangeFlag = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(bWaferExchangeFlag)) {
                tmpWaferXferSeq.add(waferXferList.get(i));
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
            com.fa.cim.newcore.bo.durable.CimCassette aPosCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, tmpWaferXferSeq.get(i).getDestinationCassetteID());
            if (CimObjectUtils.isEmpty(aPosCassette)) cassetteFound = false;
            if (CimBooleanUtils.isFalse(cassetteFound)) {
                bSlotCheckFlagSeq.set(i, true);
                continue;
            }
            List<Integer> nEmptyPositions = aPosCassette.emptyPositions();
            int lenEmptyPos = CimArrayUtils.getSize(nEmptyPositions);
            for (int j = 0; j < lenWaferXferSeq; j++) {
                if (ObjectIdentifier.equalsWithValue(tmpWaferXferSeq.get(i).getDestinationCassetteID(), tmpWaferXferSeq.get(j).getDestinationCassetteID())) {
                    for (int k = 0; k < lenEmptyPos; k++) {
                        if (tmpWaferXferSeq.get(j).getDestinationSlotNumber().intValue() == nEmptyPositions.get(k).intValue()) {
                            bSlotCheckFlagSeq.set(j, true);
                            break;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < lenWaferXferSeq; i++) {
            //------------------------
            // Error case
            //------------------------
            if (CimBooleanUtils.isFalse(bSlotCheckFlagSeq.get(i))) {
                com.fa.cim.newcore.bo.durable.CimCassette aPosCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, tmpWaferXferSeq.get(i).getDestinationCassetteID());
                Integer nPosTotal = aPosCassette.getPosition();
                Validations.check(true,new OmCode(retCodeConfigEx.getInvalidDestWaferPosition(), ObjectIdentifier.fetchValue(tmpWaferXferSeq.get(i).getWaferID()), nPosTotal, ObjectIdentifier.fetchValue(tmpWaferXferSeq.get(i).getDestinationCassetteID())));
//                if(tmpWaferXferSeq.get(i).getDestinationSlotNumber() > (nPosTotal.intValue() - 1)
//                        || tmpWaferXferSeq.get(i).getDestinationSlotNumber() == 0){
//                    Validations.check(true,new OmCode(retCodeConfigEx.getInvalidDestWaferPosition(), ObjectUtils.getObjectValue(tmpWaferXferSeq.get(i).getWaferID()), nPosTotal,ObjectUtils.getObjectValue(tmpWaferXferSeq.get(i).getDestinationCassetteID())));
//                }else{
//                    Validations.check(true,new OmCode(retCodeConfigEx.getInvalidDestWaferPosition(), ObjectUtils.getObjectValue(tmpWaferXferSeq.get(i).getWaferID()), nPosTotal,ObjectUtils.getObjectValue(tmpWaferXferSeq.get(i).getDestinationCassetteID())));
//                }

            }
        }
        //--------------------------------------------------------------
        // Check that Wafer must not belong to two or more Carriers.
        //--------------------------------------------------------------
        for (int i = 0; i < nInputWaferLen; i++) {
            // Check destination Cassette existance
            // If destination Cassette does not exist in SiView, the Cassette is not checked.
            Boolean cassetteFound = false;
            com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, waferXferList.get(i).getDestinationCassetteID());
            if (null != aCassette) {
                cassetteFound = true;
            }
            if (CimBooleanUtils.isFalse(waferXferList.get(i).getBDestinationCassetteManagedByOM()) || CimBooleanUtils.isFalse(cassetteFound)) {
                continue;
            }
            ObjectIdentifier destCastID = new ObjectIdentifier(aCassette.getIdentifier(), aCassette.getPrimaryKey());
            ObjectIdentifier strWaferLotGetOut = null;
            try {
                strWaferLotGetOut = waferMethod.waferLotGet(objCommon, waferXferList.get(i).getWaferID());
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
                    if (ObjectIdentifier.equalsWithValue(parentLotWaferInfoListGetDROut.get(j).getWaferID(), waferXferList.get(k).getWaferID())
                            && ObjectIdentifier.equalsWithValue(waferXferList.get(i).getDestinationCassetteID(), waferXferList.get(k).getDestinationCassetteID())
                            && waferXferList.get(i).getBDestinationCassetteManagedByOM() == waferXferList.get(k).getBDestinationCassetteManagedByOM()) {
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
        }
    }

    @Override
    public void sorterReticleTransferInfoVerify(Infos.ObjCommon objCommon, List<Infos.ReticleSortInfo> strReticleSortInfos) {
        //--------------------------------------------------------
        // Check Current Condition of wafer - carrier - position
        //--------------------------------------------------------
        log.info("Check Current Condition of reticle - reticlePod - position");

        Validations.check(CimObjectUtils.isEmpty(strReticleSortInfos), retCodeConfig.getInvalidInputParam());

        for (Infos.ReticleSortInfo strReticleSortInfo : strReticleSortInfos) {
            log.info("Check input reticle - ROUND");
            ObjectIdentifier reticleID = strReticleSortInfo.getReticleID();
            Validations.check(ObjectIdentifier.isEmpty(reticleID), retCodeConfig.getReticleIdPod());
            ObjectIdentifier originalReticlePodID = strReticleSortInfo.getOriginalReticlePodID();
            ObjectIdentifier destinationReticlePodID = strReticleSortInfo.getDestinationReticlePodID();

            CimProcessDurable aPosReticleObj = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
            Validations.check(aPosReticleObj == null, retCodeConfig.getNotFoundMaterial());

            CimReticlePod aInputOrgReticlePodObj = baseCoreFactory.getBO(CimReticlePod.class, originalReticlePodID);
            Validations.check(aInputOrgReticlePodObj == null, retCodeConfig.getNotFoundReticlePod());

            CimReticlePod aInputDestReticlePodObj = baseCoreFactory.getBO(CimReticlePod.class, destinationReticlePodID);
            Validations.check(aInputDestReticlePodObj == null, retCodeConfig.getNotFoundReticlePod());

            log.info("input originalReticlePodID, destinationReticlePodID, reticleID found");
            log.info("# originalReticlePodID {}", originalReticlePodID);
            log.info("# destinationReticlePodID {}", destinationReticlePodID);
            //----------------------------------------------------
            // check reticlePod's xferStatus
            //----------------------------------------------------
            reticleMethod.reticlePodTransferStateCheck(objCommon, originalReticlePodID, destinationReticlePodID, true, true);

            //----------------------------------------------------
            // check reticlePod's reticle slotNumber
            //----------------------------------------------------
            Material aOrgMaterialTemp = aInputOrgReticlePodObj.contentsOfPosition(strReticleSortInfo.getOriginalSlotNumber());
            Validations.check(aOrgMaterialTemp == null, retCodeConfig.getInvalidReticlePodPosition());
            Validations.check(!ObjectIdentifier.equalsWithValue(reticleID, aOrgMaterialTemp.getIdentifier()), retCodeConfig.getInvalidReticlePodPosition());
        }

        //----------------------------------------------------
        // Extract input structure that system currently cannot
        // find out destination slot is empty or not
        //   find same slot for reticlePod
        //
        //  <original>          <destination>
        //  POD1                POD1
        //  +-----              +-----
        //  |  1 ---- R1        |  1
        //  |  2 ---- R2  ->    |  2 ---- R1 <----- Non Check reticle's destination slot
        //  |  3 ---- R3        |  3 ---- R2 <----- Non Check reticle's destination slot
        //  |  4                |  4 ---- R3 <===== Check reticle's destination slot : tmpReticleSortInfoSeq[0]
        //  +-----              +-----
        //
        //  POD1                POD1
        //  +-----              +-----
        //  |  1 ---- R1        |  1 ---- R1 <----- Non Check reticle's destination slot
        //  |  2                |  2 ---- R2 <===== Check reticle's destination slot : tmpReticleSortInfoSeq[0]
        //  |  3 ---- R2  ->    |  3 ---- R3 <----- Non Check reticle's destination slot
        //  |  4                |  4
        //  |  5 ---- R3        |  5
        //  +-----              +-----
        //----------------------------------------------------
        log.info("Extract input structure that system currently cannot");

        List<Infos.ReticleSortInfo> tmpReticleSortInfoSeq = new ArrayList<>();
        for (Infos.ReticleSortInfo strReticleSortInfoI : strReticleSortInfos) {
            boolean bReticleExchangeFlag = false;
            for (Infos.ReticleSortInfo strReticleSortInfoJ : strReticleSortInfos) {
                if (ObjectIdentifier.equalsWithValue(strReticleSortInfoI.getDestinationReticlePodID(), strReticleSortInfoJ.getOriginalReticlePodID())
                        && strReticleSortInfoI.getDestinationSlotNumber() == strReticleSortInfoJ.getOriginalSlotNumber()) {
                    log.info("set!! bReticleExchangeFlag = TRUE");

                    bReticleExchangeFlag = true;
                    break;
                }
            }
            if (!bReticleExchangeFlag) {
                log.info("bReticleExchangeFlag == FALSE");
                tmpReticleSortInfoSeq.add(strReticleSortInfoI);
            }
        }

        //----------------------------------------------------
        // Check reticle's destination slot is empty or not
        //----------------------------------------------------
        log.info("Check reticle's destination slot is empty or not");
        for (Infos.ReticleSortInfo reticleSortInfo : tmpReticleSortInfoSeq) {
            CimReticlePod aPosReticlePod = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimReticlePod.class, reticleSortInfo.getDestinationReticlePodID());
            Validations.check(aPosReticlePod == null, retCodeConfig.getNotFoundReticlePod());

            int destinationSlotNumber = reticleSortInfo.getDestinationSlotNumber();
            log.info("destinationReticlePodID {}", reticleSortInfo.getDestinationReticlePodID());
            log.info("destinationSlotNumber {}", destinationSlotNumber);
            Integer nPosTotal = aPosReticlePod.getPositionTotal();
            Validations.check(destinationSlotNumber > nPosTotal || destinationSlotNumber <= 0,
                    new OmCode(retCodeConfig.getInvalidReticlePodPostionSpecified(), String.valueOf(destinationSlotNumber), reticleSortInfo.getDestinationReticlePodID().getValue()));
            Material aNewMaterialTemp = null;
            try {
                aNewMaterialTemp = aPosReticlePod.contentsOfPosition(reticleSortInfo.getDestinationSlotNumber());
            }catch (NotFoundRecordException e){
                // OK : Empty
            }

            Validations.check(aNewMaterialTemp != null, retCodeConfig.getNotEmptyReticlePodPosition());
        }
    }

    @Override
    public Outputs.SorterSorterJobStatusGetDROut sorterSorterJobStatusGetDR(Infos.ObjCommon objCommon, Inputs.SorterSorterJobStatusGetDRIn sorterSorterJobStatusGetDRIn) {
        Outputs.SorterSorterJobStatusGetDROut out = new Outputs.SorterSorterJobStatusGetDROut();
        String sql = String.format(" SELECT B.SORTER_JOB_ID, B.COMPONENT_JOB_ID\n" +
                        "   FROM OMSORTJOB A, OMSORTJOB_COMP B\n" +
                        "  WHERE A.SORTER_JOB_ID = B.SORTER_JOB_ID\n" +
                        "    AND A.EQP_ID =  '%s' \n" +
                        "    AND A.PORT_GROUP_ID =  '%s' \n" +
                        "    AND B.SRC_CARRIER_ID =  '%s' \n" +
                        "    AND B.DEST_CARRIER_ID =  '%s' \n",
                ObjectIdentifier.fetchValue(sorterSorterJobStatusGetDRIn.getEquipmentID()),
                sorterSorterJobStatusGetDRIn.getPortGroupID(),
                ObjectIdentifier.fetchValue(sorterSorterJobStatusGetDRIn.getOriginalCassetteID()),
                ObjectIdentifier.fetchValue(sorterSorterJobStatusGetDRIn.getDestinationCassetteID()));
        List<Object[]> list = cimJpaRepository.query(sql);
        if (CimArrayUtils.isNotEmpty(list)) {
            for (Object[] objects : list) {
                out.setSorterJobID(new ObjectIdentifier(objects[0].toString()));
                out.setSorterComponentJobID(new ObjectIdentifier(objects[1].toString()));
            }
        }
        return out;
    }

    @Override
    public String sorterSorterJobLockDR(Infos.ObjCommon objCommon, Inputs.SorterSorterJobLockDRIn sorterSorterJobLockDRIn) {
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
    public void sorterSorterJobStatusUpdateDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterJobID, String sorterJobStatus) {
        CimSorterJob sorterJobBO = baseCoreFactory.getBO(CimSorterJob.class, sorterJobID);
        Validations.check(sorterJobBO == null, retCodeConfigEx.getNotFoundSorterjob(), sorterJobID);
       // sorterJobBO.setSortJobStatus(sorterJobStatus);
    }

    @Override
    public Outputs.ObjSorterComponentJobInfoGetByComponentJobIDDROut sorterComponentJobInfoGetByComponentJobIDDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterComponentJobID) {

        //init
        Outputs.ObjSorterComponentJobInfoGetByComponentJobIDDROut out = new Outputs.ObjSorterComponentJobInfoGetByComponentJobIDDROut();
        //----- Initialize hostvariable -----//
        String fsSorterJobID = "";
        String fsComponentJobID = "";
        String fsComponentJobStatus = "";
        String fsSrcCastID = "";
        String fsDestCastID = "";
        String fsSrcPortID = "";
        String fsDestPortID = "";
        String fsPrevComponentJobID = "";

        //----- Set hostvariable -----//
        fsComponentJobID = sorterComponentJobID.getValue();

        //----- Select OMSORTJOB_COMP -----//
        CimSortJobComponentDO cimSortJobComponentExam = new CimSortJobComponentDO();
        cimSortJobComponentExam.setComponentJobID(ObjectIdentifier.fetchValue(sorterComponentJobID));
        List<CimSortJobComponentDO> queryResult = cimJpaRepository.findAll(Example.of(cimSortJobComponentExam));
        if (CimArrayUtils.getSize(queryResult) > 0) {
            for (CimSortJobComponentDO cimSortJobComponentDO : queryResult) {
                fsSorterJobID = cimSortJobComponentDO.getReferenceKey();
                fsComponentJobStatus = cimSortJobComponentDO.getComponentJobStatus();
                fsSrcCastID = cimSortJobComponentDO.getSourceCassetteID();
                fsDestCastID = cimSortJobComponentDO.getDestCassetteID();
                fsSrcPortID = cimSortJobComponentDO.getDestPortID();
                fsDestPortID = cimSortJobComponentDO.getDestPortID();
                fsPrevComponentJobID = cimSortJobComponentDO.getPreviousComponentJobID();
            }
        }
        //----- Set out structure -----//
        out.setSorterJobID(ObjectIdentifier.buildWithValue(fsSorterJobID));
        Infos.SorterComponentJobListAttributes strSorterComponentJobListAttributes = new Infos.SorterComponentJobListAttributes();
        strSorterComponentJobListAttributes.setSorterComponentJobID(ObjectIdentifier.buildWithValue(fsComponentJobID));
        strSorterComponentJobListAttributes.setOriginalCarrierID(ObjectIdentifier.buildWithValue(fsSrcCastID));
        strSorterComponentJobListAttributes.setOriginalPortID(ObjectIdentifier.buildWithValue(fsSrcPortID));
        strSorterComponentJobListAttributes.setDestinationCarrierID(ObjectIdentifier.buildWithValue(fsDestCastID));
        strSorterComponentJobListAttributes.setDestinationPortID(ObjectIdentifier.buildWithValue(fsDestPortID));
        strSorterComponentJobListAttributes.setComponentSorterJobStatus(fsComponentJobStatus);
        strSorterComponentJobListAttributes.setPreSorterComponentJobID(ObjectIdentifier.buildWithValue(fsPrevComponentJobID));
        out.setStrSorterComponentJobListAttributes(strSorterComponentJobListAttributes);
        //----------------------
        //  Return to Caller
        //----------------------
        return out;
    }

    @Override
    public void sorterComponentJobStatusUpdateDR(Infos.ObjCommon objCommon, ObjectIdentifier componentJobID, String sorterComponentJobStatus) {
        //----- Update OMSORTJOB_COMP -----//
        CimSortJobComponentDO cimSortJobComponentExam = new CimSortJobComponentDO();
        cimSortJobComponentExam.setComponentJobID(ObjectIdentifier.fetchValue(componentJobID));
        cimJpaRepository.findAll(Example.of(cimSortJobComponentExam)).forEach(data -> {
            data.setComponentJobStatus(sorterComponentJobStatus);
            //cimJpaRepository.save(data);
        });
    }

    @Override
    public void sorterComponentJobDeleteDR(Infos.ObjCommon objCommon, Inputs.SorterComponentJobDeleteDRIn in) {
        int nComponentLen = CimArrayUtils.getSize(in.getComponentJobIDseq());
        if (ObjectIdentifier.isEmpty(in.getSorterJobID())) {
            return;
        } else if (!ObjectIdentifier.isEmpty(in.getSorterJobID()) && 0 != nComponentLen) {
            for (int i = 0; i < nComponentLen; i++) {
                //--------------------------------
                //  Delete sorter waferSlotmap
                //--------------------------------
                this.sorterWaferSlotmapDeleteDR(objCommon, in.getComponentJobIDseq().get(i));
                //--------------------------------
                //  Delete sorter componentJob
                //--------------------------------
                CimSortJobComponentDO cimSortJobComponentExam = new CimSortJobComponentDO();
                cimSortJobComponentExam.setReferenceKey(in.getSorterJobID().getReferenceKey());
                cimSortJobComponentExam.setComponentJobID(ObjectIdentifier.fetchValue(in.getComponentJobIDseq().get(i)));
                //cimJpaRepository.removeNonRuntimeEntityForExample(cimSortJobComponentExam);
                //--------------------------------------
                //  Update sorter componentJob count
                //--------------------------------------
                CimSortJobDO cimSortJobExam = new CimSortJobDO();
                cimSortJobExam.setSorterJobID(ObjectIdentifier.fetchValue(in.getSorterJobID()));
                CimSortJobDO cimSortJobDO = cimJpaRepository.findOne(Example.of(cimSortJobExam)).orElse(null);
                int sortJobcount = Optional.ofNullable(cimSortJobDO)
                        .map(CimSortJobDO::getComponentCount).orElse(0);
                Optional.ofNullable(cimSortJobDO).ifPresent(data -> {
                    data.setComponentCount(sortJobcount - nComponentLen);
                    //cimJpaRepository.saveNonRuntimeEntity(data);
                });
            }
        } else if (!ObjectIdentifier.isEmpty(in.getSorterJobID())) {

            //--------------------------------
            //  Delete sorter ComponentJob
            //--------------------------------
            CimSortJobComponentDO cimSortJobComponentExam = new CimSortJobComponentDO();
            cimSortJobComponentExam.setReferenceKey(ObjectIdentifier.fetchReferenceKey(in.getSorterJobID()));
            cimJpaRepository.findAll(Example.of(cimSortJobComponentExam)).forEach(data -> {
                //--------------------------------
                //  Delete sorter waferSlotmap
                //--------------------------------
                this.sorterWaferSlotmapDeleteDR(objCommon, ObjectIdentifier.build(data.getComponentJobID(), data.getId()));

                //cimJpaRepository.removeNonRuntimeEntity(data);
            });

            //---------------------------
            //  Delete sorter SlotMap
            //---------------------------
            CimSortJobDO cimSortJobExam = new CimSortJobDO();
            cimSortJobExam.setSorterJobID(ObjectIdentifier.fetchValue(in.getSorterJobID()));
            //cimJpaRepository.removeNonRuntimeEntityForExample(cimSortJobExam);
        }
    }

    @Override
    public void sorterSorterJobDeleteDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterJobID) {
        CimSortJobDO cimSortJobExam = new CimSortJobDO();
        cimSortJobExam.setSorterJobID(ObjectIdentifier.fetchValue(sorterJobID));
        //cimJpaRepository.removeNonRuntimeEntityForExample(cimSortJobExam);
    }

    @Override
    public void sorterLinkedJobUpdateDR(Infos.ObjCommon objCommon, Inputs.SorterLinkedJobUpdateDRIn in) {
        Validations.check(0 == CimArrayUtils.getSize(in.getJobIDs()), retCodeConfig.getInvalidParameter());
        /*--------------------*/
        /* Job Sequence Check */
        /*--------------------*/
        if (CimStringUtils.equals(in.getJobType(), BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB)) {
            String sql = String.format("SELECT COUNT(SORTER_JOB_ID) FROM OMSORTJOB\n" +
                            " WHERE EQP_ID IN (SELECT EQP_ID FROM OMSORTJOB WHERE SORTER_JOB_ID = '%s')",
                    ObjectIdentifier.fetchValue(in.getJobIDs().get(0)));
            long hCount = cimJpaRepository.count(sql);
            Validations.check(hCount != CimArrayUtils.getSize(in.getJobIDs()), retCodeConfigEx.getSorterInvalidParameter());
        } else if (CimStringUtils.equals(in.getJobType(), BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB)) {
            String sql = String.format("SELECT COUNT(SORTER_JOB_ID) FROM OMSORTJOB_COMP\n" +
                            " WHERE SORTER_JOB_ID IN (SELECT SORTER_JOB_ID\n" +
                            " FROM OMSORTJOB_COMP WHERE COMPONENT_JOB_ID = '%s')",
                    ObjectIdentifier.fetchValue(in.getJobIDs().get(0)));
            long sortJobComponentDOS = cimJpaRepository.count(sql);
            Validations.check(sortJobComponentDOS != CimArrayUtils.getSize(in.getJobIDs()), retCodeConfigEx.getSorterInvalidParameter());
        } else {
            Validations.check(retCodeConfigEx.getInvalidSorterJobType());
        }
        /*-----------------*/
        /* Get Update Info */
        /*-----------------*/

        ObjectIdentifier tmpJobID = null;
        if (CimStringUtils.equals(in.getJobType(), BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB)) {
            /*-----------------*/
            /* Priority Update */
            /*-----------------*/
            for (ObjectIdentifier jobID : in.getJobIDs()) {
                CimSortJobDO cimSortJobExam = new CimSortJobDO();
                cimSortJobExam.setSorterJobID(ObjectIdentifier.fetchValue(jobID));
                CimSortJobDO cimSortJobDO = cimJpaRepository.findOne(Example.of(cimSortJobExam)).orElse(null);
                cimSortJobDO.setPreSorterJobID(ObjectIdentifier.fetchValue(tmpJobID));
                //cimJpaRepository.saveNonRuntimeEntity(cimSortJobDO);
                tmpJobID = jobID;
            }
        } else if (CimStringUtils.equals(in.getJobType(), BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB)) {
            /*-----------------*/
            /* Priority Update */
            /*-----------------*/
            for (ObjectIdentifier jobID : in.getJobIDs()) {
                CimSortJobComponentDO cimSortJobComponentExam = new CimSortJobComponentDO();
                cimSortJobComponentExam.setComponentJobID(ObjectIdentifier.fetchValue(jobID));
                CimSortJobComponentDO cimSortJobDO = cimJpaRepository.findOne(Example.of(cimSortJobComponentExam)).orElse(null);
                cimSortJobDO.setPreviousComponentJobID(ObjectIdentifier.fetchValue(tmpJobID));
                //cimJpaRepository.saveNonRuntimeEntity(cimSortJobDO);
                tmpJobID = jobID;
            }
        } else {
            Validations.check(retCodeConfigEx.getInvalidSorterJobType());
        }
    }

    @Override
    public void sorterWaferSlotmapDeleteDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterComponentJobID) {
//        CimSortJobSlotMapDO cimSortJobSlotMapExam = new CimSortJobSlotMapDO();
//        cimSortJobSlotMapExam.setComponentJobID(ObjectIdentifier.fetchValue(sorterComponentJobID));
//        cimJpaRepository.removeNonRuntimeEntity(cimSortJobSlotMapExam);
        List<CimSortJobSlotMapDO> sortJobSlotMapDOS = cimJpaRepository.query("select * from OMSORTJOB_COMP_SLOTMAP where COMPONENT_JOB_ID = ?1", CimSortJobSlotMapDO.class, ObjectIdentifier.fetchValue(sorterComponentJobID));
        if (CimArrayUtils.isNotEmpty(sortJobSlotMapDOS)) {
            for (CimSortJobSlotMapDO sortJobSlotMapDO : sortJobSlotMapDOS) {
                //cimJpaRepository.removeNonRuntimeEntity(sortJobSlotMapDO);
            }
        }
    }

    @Override
    public void sorterCheckConditionForJobCreate(Infos.ObjCommon objCommon, List<Infos.SorterComponentJobListAttributes> strSorterComponentJobListAttributesSequence, ObjectIdentifier equipmentID, String portGroupID) {

        //--------------------------------------------------------------
        //  Initialize
        //--------------------------------------------------------------
        log.info("in para userID : {} ", objCommon.getUser().getUserID().getValue() );
        log.info("in para equipmentID : {}", equipmentID);
        log.info("in para portGroupID : {}", portGroupID);


        //==============================================================
        //
        // Check Inparameter consistency
        //
        //==============================================================
        log.info( "##### Check Inparameter consistency.");
        int srtCmpLen  = CimArrayUtils.getSize(strSorterComponentJobListAttributesSequence);
        int srtCmpCnt  = 0;
        int srtCmpLen2 = 0;
        int srtCmpCnt2 = 0;

        int slotMapLen  = 0;
        int slotMapCnt  = 0;
        int slotMapLen2 = 0;
        int slotMapCnt2 = 0;

        //----------------------
        // Component vs Slotmap
        //----------------------
        for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++)            //Component
        {
            //1 Component
            if(ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID(), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID())
                    && ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalPortID(), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationPortID())) {
                Validations.check(retCodeConfigEx.getSorterInvalidParameter(),
                        "Some Component Jobs exist despite being Original the same as Destination");
            }
            else if(!ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID(), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID())
                    && ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalPortID(), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationPortID())) {
                log.info( "Each Port ID is the same despite being Original different from Destination." );
                Validations.check(retCodeConfigEx.getSorterInvalidParameter(),"Each Port ID is the same despite being Original different from Destination.");
            }
            else if(ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID(), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID())
                    && !ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalPortID(), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationPortID())) {
                log.info( "Each Port ID differs despite being Original the same as Destination." );
                Validations.check(retCodeConfigEx.getSorterInvalidParameter(),"Each Port ID differs despite being Original the same as Destination.");
            }


            slotMapLen = CimArrayUtils.getSize(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList());
            for(slotMapCnt = 0; slotMapCnt<slotMapLen; slotMapCnt++) {
                log.info( "Component Job info Original CarrierID   : {} ", srtCmpCnt, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID()   );
                log.info( "Component Job info Original PortID       : {}", srtCmpCnt, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalPortID()      );
                log.info( "Component Job info Destination CarrierID : {}", srtCmpCnt, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID());
                log.info( "Component Job info Destination PortID   : {} ", srtCmpCnt, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationPortID()   );
                //Component.equipment == SlotMap.equipment
                if(!ObjectIdentifier.equalsWithValue(equipmentID, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getEquipmentID())) {
                    Validations.check(retCodeConfigEx.getSorterInvalidParameter(),"EquipmentID is different.");
                }

                //Component.original/destination == SlotMap.original/destination
                if(ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID(), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getOriginalCassetteID())
                        && ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalPortID(), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getOriginalPortID())
                        && ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID(), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getDestinationCassetteID())
                        && ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationPortID(), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getDestinationPortID())) {
                    log.info( "Slot Map informaion is also same. OK." );
                } else {
                    log.info( "SlotMap Job info Original CarrierID  : {}  ", slotMapCnt, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getOriginalCassetteID()    );
                    log.info( "SlotMap Job info Original PortID     : {}  ", slotMapCnt, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getOriginalPortID()        );
                    log.info( "SlotMap Job info Destination CarrierID : {}", slotMapCnt, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getDestinationCassetteID() );
                    log.info( "SlotMap Job info Destination PortID   : {} ", slotMapCnt, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getDestinationPortID()     );
                    Validations.check(retCodeConfigEx.getInputSorterjobInformationDuplicate());
                }

                //------------------------------------------
                //SlotMap Number duplicate ?
                //------------------------------------------
                int orgSlotNum = strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getOriginalSlotNumber().intValue();
                int dstSlotNum = strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getDestinationSlotNumber().intValue();
                Boolean orgSlotDuplicateFlag = false;
                Boolean dstSlotDuplicateFlag = false;
                for( slotMapCnt2 = slotMapCnt + 1 ; slotMapCnt2 < slotMapLen; slotMapCnt2++ )
                {
                    log.info( "Original    SlotMap Number. : {}", orgSlotNum, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt2).getOriginalSlotNumber() );
                    log.info( "Destination SlotMap Number. : {}", dstSlotNum, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt2).getDestinationSlotNumber()  );
                    //original
                    if( orgSlotNum == strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt2).getOriginalSlotNumber() ) {
                        orgSlotDuplicateFlag = true;
                    }
                    //destination
                    if( dstSlotNum == strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt2).getDestinationSlotNumber() )
                    {
                        dstSlotDuplicateFlag = true;
                    }
                }
                if(CimBooleanUtils.isTrue(orgSlotDuplicateFlag) || CimBooleanUtils.isTrue(dstSlotDuplicateFlag) ) {
                    Validations.check(retCodeConfigEx.getSorterInvalidParameter(),"Slot Number is duplicated. original/destination.");
                }
            }//Loop of SlotMap
        }//Loop of Component

        //-------------------------
        // Component vs Component
        //-------------------------
        for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++)            //Component
        {
            for( srtCmpCnt2 = srtCmpCnt + 1 ; srtCmpCnt2 < srtCmpLen; srtCmpCnt2++ )    //Component2
            {
                //Component1.originalPortID - Component2.destinationPortID
                if(ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalPortID(), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationPortID())) {
                    log.info( "The Same Port is specified between OriginalPort and DestinationPort in different Component Job. Org/Dst ");
                    Validations.check(retCodeConfigEx.getSorterInvalidParameter(),"The Same Port is specified between OriginalPort and DestinationPort in different Component Job. Org/Dst ");
                }

                //Component1.originalCarrierID - Component2.destinationCarrierID
                if(ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID(),
                        strSorterComponentJobListAttributesSequence.get(srtCmpCnt2).getDestinationCarrierID())) {
                    Validations.check(retCodeConfigEx.getSorterInvalidParameter(),"The Same Carrier is specified between OriginalCarrier and DestinationCarrier in different Component Job. Org/Dst");
                }

                //Component1.SlotMap.lotID  - Component2.SlotMap.lotID
                if(CimStringUtils.equals(objCommon.getTransactionID(), "OSRTW009")) {
                    slotMapLen  = CimArrayUtils.getSize(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList());
                    slotMapLen2 = CimArrayUtils.getSize(strSorterComponentJobListAttributesSequence.get(srtCmpCnt2).getWaferSorterSlotMapList());
                    for(slotMapCnt = 0; slotMapCnt<slotMapLen; slotMapCnt++) {
                        ObjectIdentifier tmpLotID = strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getLotID();
                        Boolean lotDuplicateFlag = false;
                        for( slotMapCnt2 = 0  ; slotMapCnt2 < slotMapLen2; slotMapCnt2++ ) {
                            if(ObjectIdentifier.equalsWithValue(tmpLotID, strSorterComponentJobListAttributesSequence.get(srtCmpCnt2).getWaferSorterSlotMapList().get(slotMapCnt2).getLotID())) {
                                lotDuplicateFlag = true;
                                break;
                            }
                        }
                        if( CimBooleanUtils.isTrue(lotDuplicateFlag)) {
                            log.info( "The Same Lot is specified in different Component Job. LotID :  {}", tmpLotID);
                            Validations.check(retCodeConfigEx.getSorterInvalidParameter(),"The Same Lot is specified in different Component Job. LotID : "+ ObjectIdentifier.fetchValue(tmpLotID));
                        }
                    }
                }

                //Component1.destinationCarrierID == Component2.destinationCarrierID
                //Component1.SlotMap.destinationSlotNumber - Component2.SlotMap.destinationSlotNumber
                if(ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID(),
                        strSorterComponentJobListAttributesSequence.get(srtCmpCnt2).getDestinationCarrierID())) {
                    slotMapLen  = CimArrayUtils.getSize(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList());
                    slotMapLen2 = CimArrayUtils.getSize(strSorterComponentJobListAttributesSequence.get(srtCmpCnt2).getWaferSorterSlotMapList());
                    for(slotMapCnt = 0; slotMapCnt<slotMapLen; slotMapCnt++) {
                        int dstSlotNum = strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getDestinationSlotNumber().intValue();
                        Boolean slotMapDuplicateFlag = false;
                        for( slotMapCnt2 = 0  ; slotMapCnt2 < slotMapLen2; slotMapCnt2++ ) {
                            if( dstSlotNum == strSorterComponentJobListAttributesSequence.get(srtCmpCnt2).getWaferSorterSlotMapList().get(slotMapCnt2).getDestinationSlotNumber() ) {
                                slotMapDuplicateFlag = true;
                                break;
                            }
                        }
                        if(CimBooleanUtils.isTrue(slotMapDuplicateFlag)) {
                            log.info( "The Destination SlotMap is dupulicated. : {}", dstSlotNum);
                            Validations.check(retCodeConfigEx.getSorterInvalidParameter(),"The Destination SlotMap is dupulicated. : " + dstSlotNum);
                        }
                    }
                }
            }//Loop of Component2
        }//Loop of Component

        //------------------------------------------
        // Check MaximumWaferInALot
        //------------------------------------------
        int maxWaferInLot = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getIntValue();
        log.info( "Maximum Wafers In A Lot. : {}", maxWaferInLot );
        int waferCount = 0;
        for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {
            waferCount = 0;
            for( srtCmpCnt2 = srtCmpCnt ; srtCmpCnt2 < srtCmpLen; srtCmpCnt2++ ) {
                if(ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID(),
                        strSorterComponentJobListAttributesSequence.get(srtCmpCnt2).getDestinationCarrierID())) {
                    waferCount = waferCount + CimArrayUtils.getSize(strSorterComponentJobListAttributesSequence.get(srtCmpCnt2).getWaferSorterSlotMapList());
                }
            }
            if( waferCount > maxWaferInLot ) {
                log.info( "Some wafers were specified to the destination slot more than Maximum Wafer In A Lot. : {}", waferCount);
                Validations.check(retCodeConfigEx.getSorterInvalidParameter(), "Some wafers were specified to the destination slot more than Maximum Wafer In A Lot." + waferCount);
            }
        }

        //Number of Carrier
        List<ObjectIdentifier> orgCarrierIDs = new ArrayList<>();
        List<ObjectIdentifier> dstCarrierIDs = new ArrayList<>();
        int orgCarrierCnt = 0 ;
        int dstCarrierCnt = 0 ;
        int i = 0;
        for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++)            //Component
        {
            //originalCarrier
            Boolean orgFoundFlag = false;
            for( i = 0; i<orgCarrierCnt; i++)
            {
                if(ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID(), orgCarrierIDs.get(i))) {
                    orgFoundFlag = true;
                    break;
                }
            }
            if( CimBooleanUtils.isFalse(orgFoundFlag)) {
                orgCarrierIDs.add(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID());
                log.info( "Original Carrier   : {} ", strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID() );
                orgCarrierCnt++;
            }

            //destinationCarrier
            Boolean dstFoundFlag = false;
            for( i = 0; i<dstCarrierCnt; i++)
            {
                if(ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID(), dstCarrierIDs.get(i))) {
                    dstFoundFlag = true;
                    break;
                }
            }
            if( CimBooleanUtils.isFalse(dstFoundFlag)) {
                dstCarrierIDs.add(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID());
                log.info( "Destination Carrier: {} ", strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID() );
                dstCarrierCnt++;
            }
        }

        if( orgCarrierCnt > 1 && dstCarrierCnt > 1 ) {
            Validations.check(retCodeConfigEx.getSorterInvalidParameter(),"Can not sort in between any OriginalCarriers and any DestinationCarriers.");
        }

        //==============================================================
        //
        // Check the availability Equipment, Port, Carrier, Lot.
        //
        //==============================================================
        log.info( "##### Check Equipment, Port information.");
        //----------------------------------------
        // Equipment and Port
        //----------------------------------------
        Infos.EqpPortInfo strEquipment_portInfo_Get_out = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);


        int portLen = CimArrayUtils.getSize(strEquipment_portInfo_Get_out.getEqpPortStatuses());
        int portCnt = 0;
        if(portLen == 0) {
            log.info( "strEquipment_portInfo_Get_out.strEqpPortInfo.strEqpPortStatus.length() == 0.");
            Validations.check(retCodeConfig.getNotFoundPort());
        }

        //----------------------------------------
        // Gather Port Group no-duplicated.
        //----------------------------------------
        int    portGPCnt = 0;
        int    portGPLen = 0;
        List<String> portGPs = new ArrayList<>();
        for(portCnt =0; portCnt<portLen; portCnt++) {
            Boolean GPFoundFlag = false;
            for(i=0; i<portGPLen; i++) {
                if(CimStringUtils.equals(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortGroup(), portGPs.get(i))) {
                    GPFoundFlag = true;
                    break;
                }
            }
            if(!GPFoundFlag ) {
                log.info( "Port Group ...: {}", strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortGroup() );
                portGPs.add(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortGroup());
            }
        }

        //------------------------------------------------------------
        //Check the number of port , Inpara.PortGroup existence.
        //------------------------------------------------------------
        Boolean portGPFoundFlag = false;
        portGPLen = CimArrayUtils.getSize(portGPs);
        String operationMode = null;
        for(i=0; i<portGPLen; i++) {
            //------------------------------------------
            // PortGroup ?
            //------------------------------------------
            if(!CimStringUtils.equals( portGroupID, portGPs.get(i))) {
                log.info( "Port Group is not match. continue... : {}", portGPs.get(i) );
                continue;
            } else {
                log.info( "Port Group is found!!", portGPs.get(i));
                portGPFoundFlag = true;
            }

            int portCntInPG = 0;
            for(portCnt =0; portCnt<portLen; portCnt++) {
                if(CimStringUtils.equals(portGPs.get(i), strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortGroup())) {
                    //------------------------------------------
                    // Port Load Purpose Type
                    //------------------------------------------
                    if(!CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_OTHER, strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getLoadPurposeType())) {
                        Validations.check(retCodeConfigEx.getInvalidPurposeTypeForSorter(),strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getLoadPurposeType());
                    }
                    //------------------------------------------
                    // Auto-1, Auto-2
                    //------------------------------------------
                    operationMode = strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getOperationMode();
                    log.info( "Equipment operation mode ... : {}", strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getOperationMode() );
                    if( !CimStringUtils.equals(BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_1, operationMode )
                            && !CimStringUtils.equals( BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_2, operationMode )) {
                        log.info( "Equipment operation mode should be Auto-1 or Auto-2." );
                        Validations.check(retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID),
                                operationMode);
                    }
                    portCntInPG++;
                }
            }

            //---------------------------------------------
            //The number of Port in portGroup should be 2.
            //---------------------------------------------
            if( portCntInPG > BizConstant.SP_SORTER_PORTCOUNTINPORTGROUP) {
                Validations.check(retCodeConfigEx.getInvalidPortCountInPortgoup(), BizConstant.SP_SORTER_PORTCOUNTINPORTGROUP);
            }
        }
        if(!portGPFoundFlag ){
            Validations.check(retCodeConfigEx.getNotFoundPortgroup());
        }

        //------------------------------------------
        // Port , Port Load Sequence Number
        //------------------------------------------
        for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {

            log.info( "Original Port    : {}:", strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalPortID() );
            log.info( "Destination Port : {}:", strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationPortID() );
            Boolean orgPortFoundFlag = false;
            Boolean dstPortFoundFlag = false;
            Boolean bLoadSeqFlag = false;
            for(portCnt =0; portCnt<portLen; portCnt++) {
                //---------------
                // Original Port
                //---------------
                if(ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalPortID(), strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortID())) {
                    //Port in Inpara.PortGroup ?
                    if( !CimStringUtils.equals(portGroupID, strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortGroup())){
                        log.info( "Original Port ID is not in Port Group.");
                        Validations.check(retCodeConfigEx.getPortPortgroupUnmatch());
                    }

                    log.info( "Original Port ID is found. ： {}", strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalPortID() );
                    orgPortFoundFlag = true;

                    //LoadSequence is 1 ?
                    if(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getLoadSequenceNumber() == 1) {
                        log.info( "originalPort loadSequence is 1 " );
                        bLoadSeqFlag = true;
                    }

                    //Equipment has the Carrier Type ?
                    Params.CarrierMoveFromIBRptParams carrierMoveFromIBRptParams = new Params.CarrierMoveFromIBRptParams();
                    carrierMoveFromIBRptParams.setCarrierID(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID());
                    carrierMoveFromIBRptParams.setEquipmentID(equipmentID);
                    carrierMoveFromIBRptParams.setDestinationPortID(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalPortID());
                    cassetteMethod.cassetteCategoryPortCapabilityCheckForContaminationControl(objCommon,carrierMoveFromIBRptParams);
                }

                //-------------------
                // Destination Port
                //-------------------
                if(ObjectIdentifier.equalsWithValue(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationPortID(), strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortID())) {
                    //Port in Inpara.PortGroup ?
                    if( !CimStringUtils.equals(portGroupID, strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortGroup())) {
                        log.info( "Desination Port ID is not in Port Group.");
                        Validations.check(retCodeConfigEx.getPortPortgroupUnmatch());
                    }

                    log.info( "Original Port ID is found. : {}", strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationPortID() );
                    dstPortFoundFlag = true;
                    if(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getLoadSequenceNumber() == 1) {
                        log.info( "destinationPort loadSequence is 1 " );
                        bLoadSeqFlag = true;
                    }

                    //Equipment has the Carrier Type ?
                    Params.CarrierMoveFromIBRptParams carrierMoveFromIBRptParams = new Params.CarrierMoveFromIBRptParams();
                    carrierMoveFromIBRptParams.setCarrierID(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID());
                    carrierMoveFromIBRptParams.setEquipmentID(equipmentID);
                    carrierMoveFromIBRptParams.setDestinationPortID(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationPortID());
                    cassetteMethod.cassetteCategoryPortCapabilityCheckForContaminationControl(objCommon, carrierMoveFromIBRptParams);

                }
            }// Loop of Equipment Port

            //------------------------------------------
            // Port ?
            //------------------------------------------
            if(!orgPortFoundFlag || !dstPortFoundFlag ) {
                log.info( "Original/Destination Port is not found.");
                Validations.check(retCodeConfig.getNotFoundPort());
            }

            //------------------------------------------
            // Load Sequence is 1?
            //------------------------------------------
            if( !bLoadSeqFlag ) {
                log.info( "Port LoadSequnece is not correct.");
                Validations.check(retCodeConfig.getInvalidLoadingSequence());
            }
        }//Loop of Component

        //-------------------------------------------------------
        // Operation Mode Auto-1 (Original:Destination / 1 : 1)
        //-------------------------------------------------------
        if(CimStringUtils.equals(operationMode, BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_1)) {
            if( orgCarrierCnt > 1 || dstCarrierCnt > 1 ) {
                log.info( " Can not create Sort Job when Port Operation Mode is 'Auto-1'. : {}, {}", orgCarrierCnt, dstCarrierCnt);
                Validations.check(retCodeConfig.getInvalidParameter());
            }
        }


        //------------------------------------------
        // Equipment Category "WaferSorter" ?
        //------------------------------------------
        log.info( "Check Equipment Category" );
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        String equipmentCategory = aPosMachine.getCategory();
        log.info("aPosMachine->getCategory() : {}", equipmentCategory);

        if( !CimStringUtils.equals(equipmentCategory, BizConstant.SP_MC_CATEGORY_WAFERSORTER) ) {
            log.info( "Machine Category is not WaferSorter : {}",equipmentCategory);
            Validations.check(retCodeConfig.getMachineTypeNotSorter());
        }

        //------------------------------------------
        // Equipment is available ??
        //------------------------------------------
        log.info( "Check Equipment Availability" );
        equipmentMethod.equipmentCheckAvail( objCommon, equipmentID);

        //------------------------------------------
        // Equipment has the some inhibitions?
        //------------------------------------------
        Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
        List<Infos.EntityIdentifier> entities = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
        entityIdentifier.setObjectID(equipmentID);
        entities.add(entityIdentifier);
        entityInhibitAttributes.setEntities(entities);

        Infos.EntityInhibitCheckForEntitiesOut strEntityInhibit_CheckForEntities_out = constraintMethod.constraintCheckForEntities(objCommon, entityInhibitAttributes);


        log.info( "The number of entity inhibition .. : {}", CimArrayUtils.getSize(strEntityInhibit_CheckForEntities_out.getEntityInhibitInfo()));
        if (CimArrayUtils.getSize(strEntityInhibit_CheckForEntities_out.getEntityInhibitInfo()) > 0) {
            Validations.check(retCodeConfig.getInhibitEntity(), CimArrayUtils.getSize(strEntityInhibit_CheckForEntities_out.getEntityInhibitInfo()),equipmentID);
        }

        //-------------------------------------
        // Carrier has had already Sort Job ?
        //-------------------------------------
        log.info( "Carrier has had already Sort Job ? ");

        int orgCarrierLen = CimArrayUtils.getSize(orgCarrierIDs);
        int dstCarrierLen = CimArrayUtils.getSize(dstCarrierIDs);

        int allCarrierLen = CimArrayUtils.getSize(orgCarrierIDs) + CimArrayUtils.getSize(dstCarrierIDs);
        int allCarrierCnt = 0 ;

        List<ObjectIdentifier> allCarrierIDs = new ArrayList<>();
        for(i=0; i<orgCarrierLen; i++)    allCarrierIDs.add(orgCarrierIDs.get(i));
        for(i=0; i<dstCarrierLen; i++)    allCarrierIDs.add(dstCarrierIDs.get(i));



        for(i=0; i<allCarrierLen; i++) {
            Inputs.ObjSorterJobListGetDRIn strSorter_jobList_GetDR_in = new Inputs.ObjSorterJobListGetDRIn();
            strSorter_jobList_GetDR_in.setCarrierID(allCarrierIDs.get(i));
            log.info( "Get Sort job information by Carrier ID ..: {}", allCarrierIDs.get(i));

            List<Infos.SortJobListAttributes> strSorter_jobList_GetDR_out = this.sorterJobListGetDR(objCommon, strSorter_jobList_GetDR_in);

            if( 0 < CimArrayUtils.getSize(strSorter_jobList_GetDR_out) ) {
                Validations.check(new OmCode(retCodeConfigEx.getExistSorterjobForCassette(), ObjectIdentifier.fetchValue(allCarrierIDs.get(i)), ObjectIdentifier.fetchValue(strSorter_jobList_GetDR_out.get(0).getSorterJobID())));
            }
        }

        //-------------------------------------------------------------------
        // Check if carrier is reserved for retrieving lot on SLM operation
        //-------------------------------------------------------------------
        log.info( "Check if carrier is reserved for retrieving lot on SLM operation.");

        for( i=0; i < allCarrierLen; i++ ) {
            /*-------------------------*/
            /*   Get Cassette Object   */
            /*-------------------------*/

            CimCassette tempCast = baseCoreFactory.getBO(CimCassette.class, allCarrierIDs.get(i));

            /*-----------------------------*/
            /*   Get SLMReserved Machine   */
            /*-----------------------------*/
            log.info( "Try to get SLM reserved equipment.");

            CimMachine aSLMRsvMachine = tempCast.getSLMReservedMachine();

            if(null != aSLMRsvMachine) {
                log.info( "The specified Carrier is reserved for SLM operation. : {}", allCarrierIDs.get(i));
                Validations.check(retCodeConfig.getAlreadyReservedCassetteSlm(),allCarrierIDs.get(i));
            }
        }

        //------------------------------------------------------------------------------
        // If Operation Mode is Auto-1, the portGroup can have only one Sort Job.
        //------------------------------------------------------------------------------

        log.info( "If Operation Mode is Auto-1, the equipment can have only one Sort Job. : {}" ,operationMode );
        if(CimStringUtils.equals(operationMode, BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_1)) {
            log.info( "operationMode = Auto-1");
            Inputs.ObjSorterJobListGetDRIn strSorter_jobList_GetDR_in = new Inputs.ObjSorterJobListGetDRIn();
            strSorter_jobList_GetDR_in.setEquipmentID(equipmentID);
            log.info( "Get Sort job information by Equipment ID ..: {}", equipmentID );

            List<Infos.SortJobListAttributes> strSorter_jobList_GetDR_out = this.sorterJobListGetDR(objCommon, strSorter_jobList_GetDR_in);

            int strSortJobListAttributesSeqLen = CimArrayUtils.getSize(strSorter_jobList_GetDR_out);
            log.info( "strSortJobListAttributesSeqLen = {}", strSortJobListAttributesSeqLen);
            if (strSortJobListAttributesSeqLen > 0) {
                log.info( "strSortJobListAttributesSeqLen > 0 ");

                int nCnt = 0;
                for (nCnt = 0; nCnt < strSortJobListAttributesSeqLen; nCnt++) {
                    log.info( "SortJob on Port Group : {}, {}", strSorter_jobList_GetDR_out.get(nCnt).getSorterJobID(), strSorter_jobList_GetDR_out.get(nCnt).getPortGroupID());

                    if (ObjectIdentifier.equalsWithValue(portGroupID, strSorter_jobList_GetDR_out.get(nCnt).getPortGroupID())) {
                        log.info( "Existing sort job on the same port group.");
                        Validations.check(retCodeConfig.getObjectAlreadyExist());
                    }
                }
            } else {
                log.info( "No sort jobs for the equipment");
            }
        }

        //----------------------------------------
        // Carrier
        //----------------------------------------
        log.info( "##### Check Carrier Information. ");
        orgCarrierLen = CimArrayUtils.getSize(orgCarrierIDs);
        dstCarrierLen = CimArrayUtils.getSize(dstCarrierIDs);

        //-----------------------
        // Original Carrier
        //-----------------------
        for(i=0; i<orgCarrierLen; i++) {
            //-------------------------------------------
            // Slot Map position is correct?
            //-------------------------------------------
            //------------------------------
            // Get slot map information
            //------------------------------
            List<Infos.WaferMapInCassetteInfo> strCassette_GetWaferMapDR_out = cassetteMethod.cassetteGetWaferMapDR(objCommon, orgCarrierIDs.get(i));

            for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {
                if(ObjectIdentifier.equalsWithValue(orgCarrierIDs.get(i), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID())) {
                    log.info("Inpara.Original Carrier ID : {}", strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getOriginalCarrierID() );
                    slotMapLen = CimArrayUtils.getSize(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList());
                    for(slotMapCnt = 0; slotMapCnt<slotMapLen; slotMapCnt++) {
                        ObjectIdentifier InWaferID;
                        ObjectIdentifier InLotID;
                        InWaferID  = strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getWaferID();
                        InLotID    = strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getLotID();
                        int InSlotNum = strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getOriginalSlotNumber().intValue();

                        Boolean waferSlotMapFoundFlag = false;
                        int    mapInCassetteLen = CimArrayUtils.getSize(strCassette_GetWaferMapDR_out);
                        int    mapInCassetteCnt = 0;
                        for(mapInCassetteCnt = 0; mapInCassetteCnt < mapInCassetteLen; mapInCassetteCnt++){
                            if(ObjectIdentifier.equalsWithValue(InLotID, strCassette_GetWaferMapDR_out.get(mapInCassetteCnt).getLotID())
                                    && ObjectIdentifier.equalsWithValue(InWaferID, strCassette_GetWaferMapDR_out.get(mapInCassetteCnt).getWaferID())
                                    && (                 InSlotNum          == strCassette_GetWaferMapDR_out.get(mapInCassetteCnt).getSlotNumber())) {
                                log.info("Found in the Carrier. LotID : {}/ WaferID : {}/ SlotNumber: {}", InLotID,  InWaferID, InSlotNum);
                                waferSlotMapFoundFlag = true;
                                break;
                            }
                        }//Loop of mapInCassetteLen
                        if( !waferSlotMapFoundFlag ) {
                            Validations.check(retCodeConfig.getInvalidOrgWaferPosition());
                        }
                    }//Loop of slotMapLen
                }
            }//Loop of srtCmpLen
        }//Loop of original Carrier


        //----------------------
        // Destination Carrier
        //----------------------
        for(i=0; i<dstCarrierLen; i++) {
            //--------------------------------
            // Get Empty Slot of Carrier
            //--------------------------------
            List<Integer> nEmptyPositions = new ArrayList<>();
            List<Integer> nTmpEmptyPositions = new ArrayList<>();

            CimCassette aPosDestCassette = baseCoreFactory.getBO(CimCassette.class, dstCarrierIDs.get(i));
            nEmptyPositions = aPosDestCassette.emptyPositions();
            nTmpEmptyPositions = nEmptyPositions;

            int lenEmptyPos = CimArrayUtils.getSize(nEmptyPositions);

            for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {
                if(ObjectIdentifier.equalsWithValue(dstCarrierIDs.get(i), strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID())) {
                    log.info("Inpara.Destination Carrier ID : {}", strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getDestinationCarrierID() );
                    slotMapLen = CimArrayUtils.getSize(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList());
                    for(slotMapCnt = 0; slotMapCnt<slotMapLen; slotMapCnt++) {
                        Boolean emptySlotFindFlag = false;
                        log.info("Inpara.Destination Slot Map Number : {}", strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getDestinationSlotNumber());
                        int IndstSlotNum = strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getDestinationSlotNumber().intValue();
                        for ( int empSlotCnt = 0; empSlotCnt < lenEmptyPos; empSlotCnt++ ) {
                            log.info("# check destinationCassette Move Wafer condition ----------------------------- {}", empSlotCnt);
                            log.info("input destinationSlotNumber = {}", IndstSlotNum);
                            log.info("destCast emptySlotNumber    = {}", nEmptyPositions.get(empSlotCnt));

                            if ( IndstSlotNum == nEmptyPositions.get(empSlotCnt)){
                                log.info("input destinationSlotNumber == (*nEmptyPositions)[empSlotCnt], destinationSlot is empty.");
                                emptySlotFindFlag = true;
                                break;
                            }
                        }
                        if(!emptySlotFindFlag ) {
                            log.info("destinationCassette Move Wafer Info is invalid! error.");
                            Validations.check(retCodeConfig.getInvalidOrgWaferPosition());
                        }
                    }//Loop of slotMapLen
                }
            }//Loop of srtCmpLen


            //------------------------
            // Status is available ?
            //------------------------
            log.info( "### Check Destination Carrier Status");
            log.info( "  carrierID : {}", dstCarrierIDs.get(i));

            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, dstCarrierIDs.get(i));
            String cassetteState = aCassette.getDurableState();

            if (CimStringUtils.equals(cassetteState, BizConstant.CIMFW_DURABLE_AVAILABLE)) {
                log.info( "Carrier Status OK. ");
            } else {
                log.info( "Carrier Status is 'NOTAVAILABLE'. : {}", cassetteState);

                String cassetteIdent = aCassette.getIdentifier();
                Validations.check(new OmCode(retCodeConfig.getInvalidCassetteState(), cassetteState, cassetteIdent));
            }
        }//Loop of Destination Carrier


        //----------------------------------
        // Original/Destination Carrier
        //----------------------------------
        log.info("Original / Destination Carrier ");
        allCarrierLen = CimArrayUtils.getSize(allCarrierIDs);
        for(allCarrierCnt = 0; allCarrierCnt< allCarrierLen; allCarrierCnt++) {
            log.info("Carrier ID : {}",allCarrierIDs.get(allCarrierCnt) );
            //------------------------------------
            // Lot in Carrier has control Job ?
            //------------------------------------
            ObjectIdentifier strCassette_controlJobID_Get_out = cassetteMethod.cassetteControlJobIDGet(objCommon, allCarrierIDs.get(allCarrierCnt));

            if (ObjectIdentifier.isNotEmpty(strCassette_controlJobID_Get_out)) {
                log.info( "Carrier has controlJob : {}", allCarrierIDs.get(allCarrierCnt));
                Validations.check(retCodeConfig.getCassetteControlJobFilled());
            }
            log.info( "Carrier does NOT have controlJob. OK. : {}", allCarrierIDs.get(allCarrierCnt));

            //------------------------------------
            // Carrier Xfer status is EI ?
            //------------------------------------
            String strCassette_transferState_Get_out = cassetteMethod.cassetteTransferStateGet(objCommon, allCarrierIDs.get(allCarrierCnt));

            log.info( "Carrier Xfer Status.. : {}", strCassette_transferState_Get_out);
            if(CimStringUtils.equals(strCassette_transferState_Get_out, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                log.info( "Can not create sort job with Carrier Xfer Status EquipmentIn. :{} ", strCassette_transferState_Get_out);
                Validations.check(retCodeConfig.getInvalidCassetteTransferState());
            }
            log.info( "Carrier Xfer Status. OK. ");

            //---------------------------------------
            // Carrier is reserved for dispatching?
            //---------------------------------------
            Boolean strCassette_dispatchState_Get_out = false;
            strCassette_dispatchState_Get_out = cassetteMethod.cassetteDispatchStateGet(objCommon, allCarrierIDs.get(allCarrierCnt));

            log.info( "Carrier is reserved for dispatching ? : {}", strCassette_dispatchState_Get_out);
            if( strCassette_dispatchState_Get_out) {
                Validations.check(retCodeConfig.getAlreadyDispatchReservedCassette());
            }
            log.info( "Carrier is NOT reserved for dispatching. OK");

        }//Loop of allCarrier


        //----------------------------------------
        // Lot
        //----------------------------------------
        log.info( "##### Check Lot Information.");
        allCarrierLen = CimArrayUtils.getSize(allCarrierIDs);
        for(allCarrierCnt = 0; allCarrierCnt< allCarrierLen; allCarrierCnt++) {
            Infos.LotListInCassetteInfo strCassette_lotList_GetDR_out = cassetteMethod.cassetteLotListGetDR(objCommon, allCarrierIDs.get(allCarrierCnt));

            int lotLen = CimArrayUtils.getSize(strCassette_lotList_GetDR_out.getLotIDList());
            int lotCnt = 0;
            for(lotCnt =0; lotCnt<lotLen; lotCnt++) {
                log.info( "Check Lot condition in Carrier.: {}", strCassette_lotList_GetDR_out.getLotIDList().get(lotCnt));
                //----------------------------
                // Flow Batching ?
                //----------------------------
                log.info( "Flow Batched Lot ? " );
                try {
                    ObjectIdentifier strLot_flowBatchID_Get_out = lotMethod.lotFlowBatchIDGet(objCommon, strCassette_lotList_GetDR_out.getLotIDList().get(lotCnt));
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())) {
                        log.info( "lot_flowBatchID_GetDR() == RC_LOT_FLOW_BATCH_ID_BLANK or RC_OK, check OK!");
                    } else {
                        throw e;
                    }
                }

                //----------------------------
                // In Back Up Site ?
                //----------------------------
                log.info( "Lot locates in Back Up Site ?" );
                Infos.LotBackupInfo strLot_backupInfo_Get_out = lotMethod.lotBackupInfoGet(objCommon, strCassette_lotList_GetDR_out.getLotIDList().get(lotCnt));

                if (CimBooleanUtils.isFalse(strLot_backupInfo_Get_out.getCurrentLocationFlag()) || CimBooleanUtils.isTrue(strLot_backupInfo_Get_out.getTransferFlag())) {
                    log.info( "##### backup condition is invalid.");
                    Validations.check(retCodeConfig.getLotInOthersite());
                }
            }

            if( 0 != CimArrayUtils.getSize(strCassette_lotList_GetDR_out.getLotIDList()) ) {
                //-----------------------------
                // In PostProcessing ?
                //-----------------------------

                //------------------------------------
                // Check LOCK Hold.
                //------------------------------------
                log.info( "Check LOCK Hold. ");
                for( int count = 0 ; count < lotLen ; count++ ) {
                    //----------------------------------
                    //  Check lot InterFabXfer state
                    //----------------------------------
                    String strLot_interFabXferState_Get_out = lotMethod.lotInterFabXferStateGet(objCommon, strCassette_lotList_GetDR_out.getLotIDList().get(count));

                    if(CimStringUtils.equals(strLot_interFabXferState_Get_out, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)) {
                        log.info( " #### The Lot interFabXfer state is required... No need to check LOCK Hold. ");
                        continue;
                    }

                    List<ObjectIdentifier> ckLotIDSeq = new ArrayList<>();
                    ckLotIDSeq.add(strCassette_lotList_GetDR_out.getLotIDList().get(count));

                    lotMethod.lotCheckLockHoldConditionForOperation(objCommon, ckLotIDSeq);
                }

                //--------------------------------------
                // Equipment is available for LOT ?
                //--------------------------------------
                log.info("call equipment_CheckAvailForLot()");
                equipmentMethod.equipmentCheckAvailForLot( objCommon, equipmentID, strCassette_lotList_GetDR_out.getLotIDList());
            }

            log.info( "Check InPostProcessFlag.");
            List<ObjectIdentifier> userGroupIDs = new ArrayList<>();
            int userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);

            for( lotCnt = 0; lotCnt < lotLen; lotCnt++ ) {
                //----------------------------------
                //  Get InPostProcessFlag of Lot
                //----------------------------------

                Outputs.ObjLotInPostProcessFlagOut strLot_inPostProcessFlag_Get_out = lotMethod.lotInPostProcessFlagGet(objCommon, strCassette_lotList_GetDR_out.getLotIDList().get(lotCnt));


                //----------------------------------
                //  Check lot InterFabXfer state
                //----------------------------------
                String strLot_interFabXferState_Get_out = lotMethod.lotInterFabXferStateGet(objCommon, strCassette_lotList_GetDR_out.getLotIDList().get(lotCnt));

                //----------------------------------------------
                //  If Lot is in post process, returns error
                //----------------------------------------------
                if(CimBooleanUtils.isTrue(strLot_inPostProcessFlag_Get_out.getInPostProcessFlagOfLot())) {
                    log.info( "Lot is in post process.");
                    if(CimStringUtils.equals(strLot_interFabXferState_Get_out, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)) {
                        log.info( " #### The Lot interFabXfer state is required... No need to check post process flag. ");
                        continue;
                    }
                    if (userGroupIDsLen == 0) {
                        /*---------------------------*/
                        /* Get UserGroupID By UserID */
                        /*---------------------------*/
                        userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());

                        userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);
                        log.info( "userGroupIDsLen : {}", userGroupIDsLen);
                    }

                    int nCnt = 0;
                    for (nCnt = 0; nCnt < userGroupIDsLen; nCnt++) {
                        log.info( "# Loop({}) / userID : {}", nCnt, userGroupIDs.get(nCnt));
                    }
                    if (nCnt == userGroupIDsLen) {
                        log.info( "NOT External Post Process User!");
                        Validations.check(retCodeConfig.getLotInPostProcess());
                    }
                }   //DSIV00000201
            }
        }

        if(CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.SORT_JOB_CREATE_REQ.getValue())  //Check it when SortJobCreate.
                && (1 == dstCarrierLen && 1 == orgCarrierLen)
                && !ObjectIdentifier.equalsWithValue(dstCarrierIDs.get(0), orgCarrierIDs.get(0))
        ) {
            //-----------------------------------------
            //  Check existence of all wafer in Lot.
            //-----------------------------------------
            log.info( "##### Check existence of all wafer in Lot.(check for creation.)");
            for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {
                slotMapLen = CimArrayUtils.getSize(strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList());
                for(slotMapCnt = 0; slotMapCnt<slotMapLen; slotMapCnt++) {
                    ObjectIdentifier tmpLotID  = strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getLotID();

                    CimLot aLot = baseCoreFactory.getBO(CimLot.class, tmpLotID);
                    Validations.check(aLot == null, retCodeConfig.getNotFoundLot(), tmpLotID);
                    //------------------------------------------------------
                    // Get all wafer information of  Lot
                    //------------------------------------------------------
                    log.info("Get all wafer information of  Lot : {}", tmpLotID );
                    List<ProductDTO.WaferInfo> lotWaferInfo = aLot.getAllWaferInfo();

                    if(CimArrayUtils.isEmpty(lotWaferInfo)) {
                        log.info("lotWaferInfo == NULL");
                        Validations.check(retCodeConfig.getProductCountZero());
                    }

                    int lotWaferCount = CimArrayUtils.getSize(lotWaferInfo);
                    log.info("lotWaferInfo->length() : {}",lotWaferCount );
                    if ( 0 == lotWaferCount ) {
                        log.info("0 == lotWaferCount" );
                        Validations.check(retCodeConfig.getProductCountZero());
                    }

                    int inparaLotWaferCount = 0;
                    for( slotMapCnt2 = 0  ; slotMapCnt2 < slotMapLen; slotMapCnt2++ ) {
                        if(ObjectIdentifier.equalsWithValue(tmpLotID, strSorterComponentJobListAttributesSequence.get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt2).getLotID())) {
                            inparaLotWaferCount++;
                        }
                    }
                    log.info("Inpara Lot : {}/WaferCount : {}", tmpLotID, inparaLotWaferCount );

                    if( inparaLotWaferCount != lotWaferCount ) {
                        log.info( "All wafers do NOT exist in the requested Lot of Sort Job Information. Inpara : {}/Actual : {}", inparaLotWaferCount, lotWaferCount );
                        Validations.check(retCodeConfigEx.getSorterInvalidParameter(),"All wafers do NOT exist in the requested Lot of Sort Job Information. " );
                    }
                }
            }
        }

        //--------------------------------------------------------------
        //  Return to Caller
        //--------------------------------------------------------------
    }

    @Override
    public Infos.SortJobListAttributes sorterSorterJobCreate(Infos.ObjCommon objCommon, Inputs.SorterSorterJobCreateIn sorterSorterJobCreateIn) {

        //----------------------------------------------
        //
        // Create Sort Job Information
        //
        //----------------------------------------------
        Infos.SortJobListAttributes tmpSortJobListAttributes = new Infos.SortJobListAttributes();
        tmpSortJobListAttributes.setSorterComponentJobListAttributesList(sorterSorterJobCreateIn.getStrSorterComponentJobListAttributesSeq());
        //-----------------------------------------------------
        // Generate Sort Job ID
        //   EquipmentID + Request Time Stamp = SortJobID
        //-----------------------------------------------------
        String tmpSorterJobID = String.format("%s-%s",sorterSorterJobCreateIn.getEquipmentID().getValue(),objCommon.getTimeStamp().getReportTimeStamp());

        tmpSortJobListAttributes.setSorterJobID(ObjectIdentifier.buildWithValue(tmpSorterJobID));
        log.info("###  Ganerated Sorter Job ID : {} ", tmpSortJobListAttributes.getSorterJobID());

        tmpSortJobListAttributes.setEquipmentID(sorterSorterJobCreateIn.getEquipmentID());
        tmpSortJobListAttributes.setPortGroupID(ObjectIdentifier.buildWithValue(sorterSorterJobCreateIn.getPortGroupID()));
        tmpSortJobListAttributes.setRequestUserID(objCommon.getUser().getUserID());
        tmpSortJobListAttributes.setRequestTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        tmpSortJobListAttributes.setSorterJobCategory(sorterSorterJobCreateIn.getSorterJobCategory());
        tmpSortJobListAttributes.setComponentCount(CimArrayUtils.getSize(sorterSorterJobCreateIn.getStrSorterComponentJobListAttributesSeq()));
        tmpSortJobListAttributes.setWaferIDReadFlag(sorterSorterJobCreateIn.isWaferIDReadFlag());

        //-------------------------------
        // Set Sort Job Status
        //   Auto-1 : Executing
        //   Auto-2 : Wait To Executing
        //-------------------------------
        if( CimStringUtils.equals(sorterSorterJobCreateIn.getOperationMode(), BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_1)) {
            log.info("###  Sorter Job Status : {}", BizConstant.SP_SORTERJOBSTATUS_EXECUTING );
            tmpSortJobListAttributes.setSorterJobStatus(BizConstant.SP_SORTERJOBSTATUS_EXECUTING);
        } else if ( CimStringUtils.equals(sorterSorterJobCreateIn.getOperationMode(), BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_2)) {
            log.info("###  Sorter Job Status ： {} ", BizConstant.SP_SORTERJOBSTATUS_WAIT_TO_EXECUTING );
            tmpSortJobListAttributes.setSorterJobStatus(BizConstant.SP_SORTERJOBSTATUS_WAIT_TO_EXECUTING);
        }

        int srtCompoList = CimArrayUtils.getSize(tmpSortJobListAttributes.getSorterComponentJobListAttributesList());
        int srtCompCnt = 0;
        int jobSeq = 0;
        for( srtCompCnt =0; srtCompCnt<srtCompoList; srtCompCnt++) {
            //-----------------------------------------------------
            // Generate Component Job ID
            //   SortJobID + "-" + jobSeq = ComponentJobID
            //-----------------------------------------------------

            jobSeq++;
            String tmpCompoSorterJobID = String.format("%s-%02d", ObjectIdentifier.fetchValue(tmpSortJobListAttributes.getSorterJobID()), jobSeq);

            tmpSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCompCnt).setSorterComponentJobID(ObjectIdentifier.buildWithValue(tmpCompoSorterJobID));
            log.info("###  Ganerated Component Job ID : {} -> {}", jobSeq, tmpSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCompCnt).getSorterComponentJobID().getValue());

            //-------------------------------
            // Set Component Job Status
            //   Auto-1 : Xfer
            //   Auto-2 : Wait To Executing
            //-------------------------------
            if( CimStringUtils.equals(sorterSorterJobCreateIn.getOperationMode(), BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_1)) {
                log.info("###  Component Job Status : {}->{}", jobSeq, BizConstant.SP_SORTERCOMPONENTJOBSTATUS_XFER );
                tmpSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCompCnt).setComponentSorterJobStatus(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_XFER);
            }
            else if ( CimStringUtils.equals(sorterSorterJobCreateIn.getOperationMode(), BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_2)) {
                log.info("###  Component Job Status : {} -> {}", jobSeq, BizConstant.SP_SORTERCOMPONENTJOBSTATUS_WAIT_TO_EXECUTING );
                tmpSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCompCnt).setComponentSorterJobStatus(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_WAIT_TO_EXECUTING);
            }
        }

        //---------------------------------
        // Call sorter_sorterJob_InsertDR
        //---------------------------------
        //typedef struct objSorter_sorterJob_InsertDR_in_struct{
        //    pptSortJobListAttributes  strSortJobListAttributes;
        //    any                       siInfo;
        //}objSorter_sorterJob_InsertDR_in;
        log.info("### Call sorter_sorterJob_InsertDR ");
        this.sorterSorterJobInsertDR( objCommon, tmpSortJobListAttributes);

        //------------------------------------
        // Call sorter_componentJob_InsertDR
        //------------------------------------

        log.info("### Call sorter_componentJob_InsertDR ");
        this.sorterComponentJobInsertDR( objCommon, tmpSortJobListAttributes);

        //Set out parameter

        //--------------------------------------------------------------
        //  Return to Caller
        //--------------------------------------------------------------
        return tmpSortJobListAttributes;
    }

    @Override
    public void sorterSorterJobInsertDR(Infos.ObjCommon objCommon, Infos.SortJobListAttributes sortJobListAttributes) {
        //------------------------------
        //  Get Previous Sorter Job ID
        //------------------------------
        Object[] previousSorterJobID = cimJpaRepository.queryOne(
                " SELECT A.SORTER_JOB_ID\n" +
                        "   FROM OMSORTJOB A\n" +
                        "  WHERE A.SORTER_JOB_ID NOT IN\n" +
                        "        (SELECT PREV_SORTER_JOB_ID FROM OMSORTJOB WHERE EQP_ID = ?1)\n" +
                        "    AND A.EQP_ID = ?2",
                ObjectIdentifier.fetchValue(sortJobListAttributes.getEquipmentID()),
                ObjectIdentifier.fetchValue(sortJobListAttributes.getEquipmentID()));
        CimSortJobDO sortJob = new CimSortJobDO();
        sortJob.setSorterJobID(ObjectIdentifier.fetchValue(sortJobListAttributes.getSorterJobID()));
        sortJob.setEquipmentID(ObjectIdentifier.fetchValue(sortJobListAttributes.getEquipmentID()));
        sortJob.setPortGroupID(ObjectIdentifier.fetchValue(sortJobListAttributes.getPortGroupID()));
        sortJob.setSorterJobStatus(sortJobListAttributes.getSorterJobStatus());
        sortJob.setComponentCount(sortJobListAttributes.getComponentCount());
        sortJob.setPreSorterJobID(Optional.ofNullable(previousSorterJobID).map(o -> (String) o[0]).orElse(""));
        sortJob.setWaferIDReadFlag(sortJobListAttributes.isWaferIDReadFlag());
        sortJob.setRequestUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        sortJob.setRequestTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        //cimJpaRepository.saveNonRuntimeEntity(sortJob);
    }

    @Override
    public void sorterComponentJobInsertDR(Infos.ObjCommon objCommon, Infos.SortJobListAttributes sortJobListAttributes) {

        //--------------------------------------------------------------
        //  Create Sorter Component Job ID
        //--------------------------------------------------------------
        String previousComponentJobID = null;
        for (int i = 0; i < CimArrayUtils.getSize(sortJobListAttributes.getSorterComponentJobListAttributesList()); i++) {
            CimSortJobComponentDO sortJobComponent = new CimSortJobComponentDO();
            //todo 这儿需要改
            sortJobComponent.setReferenceKey(ObjectIdentifier.fetchValue(sortJobListAttributes.getSorterJobID()));
            sortJobComponent.setComponentJobID(ObjectIdentifier.fetchValue(sortJobListAttributes.getSorterComponentJobListAttributesList().get(i).getSorterComponentJobID()));
            sortJobComponent.setComponentJobStatus(sortJobListAttributes.getSorterComponentJobListAttributesList().get(i).getComponentSorterJobStatus());
            sortJobComponent.setSourceCassetteID(ObjectIdentifier.fetchValue(sortJobListAttributes.getSorterComponentJobListAttributesList().get(i).getOriginalCarrierID()));
            sortJobComponent.setDestCassetteID(ObjectIdentifier.fetchValue(sortJobListAttributes.getSorterComponentJobListAttributesList().get(i).getDestinationCarrierID()));
            sortJobComponent.setSourcePortID(ObjectIdentifier.fetchValue(sortJobListAttributes.getSorterComponentJobListAttributesList().get(i).getOriginalPortID()));
            sortJobComponent.setDestPortID(ObjectIdentifier.fetchValue(sortJobListAttributes.getSorterComponentJobListAttributesList().get(i).getDestinationPortID()));
            sortJobComponent.setPreviousComponentJobID(previousComponentJobID);
            //cimJpaRepository.saveNonRuntimeEntity(sortJobComponent);

            previousComponentJobID = sortJobComponent.getComponentJobID();

            Inputs.SorterWaferSlotmapInsertDRIn sorterWaferSlotmapInsertDRIn = new Inputs.SorterWaferSlotmapInsertDRIn();
            sorterWaferSlotmapInsertDRIn.setSorterJobID(sortJobListAttributes.getSorterJobID());
            sorterWaferSlotmapInsertDRIn.setSorterComponentJobID(sortJobListAttributes.getSorterComponentJobListAttributesList().get(i).getSorterComponentJobID());
            sorterWaferSlotmapInsertDRIn.setStrWaferSorterSlotMapSequence(sortJobListAttributes.getSorterComponentJobListAttributesList().get(i).getWaferSorterSlotMapList());
            this.sorterWaferSlotmapInsertDR(objCommon, sorterWaferSlotmapInsertDRIn);
        }
    }

    @Override
    public void sorterWaferSlotmapInsertDR(Infos.ObjCommon objCommon, Inputs.SorterWaferSlotmapInsertDRIn sorterWaferSlotmapInsertDRIn) {

        //--------------------------------------------------------------
        //  Create Sorter Wafer Slotmap
        //--------------------------------------------------------------
        for (int i = 0; i < CimArrayUtils.getSize(sorterWaferSlotmapInsertDRIn.getStrWaferSorterSlotMapSequence()); i++) {
            CimSortJobSlotMapDO sortJobSlotMap = new CimSortJobSlotMapDO();
            //todo 需要 tComponentJobID主键
            //    sortJobSlotMap.setReferenceKey();
            //    sortJobSlotMap.setComponentJobID(CimObjectUtils.getObjectValue(sorterWaferSlotmapInsertDRIn.getSorterComponentJobID()));
            sortJobSlotMap.setLotID(ObjectIdentifier.fetchValue(sorterWaferSlotmapInsertDRIn.getStrWaferSorterSlotMapSequence().get(i).getLotID()));
            sortJobSlotMap.setWaferID(ObjectIdentifier.fetchValue(sorterWaferSlotmapInsertDRIn.getStrWaferSorterSlotMapSequence().get(i).getWaferID()));
            sortJobSlotMap.setDestPosition(sorterWaferSlotmapInsertDRIn.getStrWaferSorterSlotMapSequence().get(i).getDestinationSlotNumber().intValue());
            sortJobSlotMap.setSourcePosition(sorterWaferSlotmapInsertDRIn.getStrWaferSorterSlotMapSequence().get(i).getOriginalSlotNumber().intValue());
            sortJobSlotMap.setAliasName(sorterWaferSlotmapInsertDRIn.getStrWaferSorterSlotMapSequence().get(i).getAliasName());
            //cimJpaRepository.saveNonRuntimeEntity(sortJobSlotMap);
        }
    }
}
