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
import com.fa.cim.dto.Results;
import com.fa.cim.entity.runtime.cassette.CimCassetteDO;
import com.fa.cim.entity.runtime.flowbatch.CimFlowBatchDO;
import com.fa.cim.entity.runtime.flowbatchdis.CimFlowDispatcherLotDO;
import com.fa.cim.entity.runtime.lot.CimLotEquipmentDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.dispatch.CimFlowBatch;
import com.fa.cim.newcore.bo.dispatch.DispatchingManager;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.prcssdfn.ProcessDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2018/11/1 13:24:33
 */
@Slf4j
@OmMethod
public class FlowBatchMethod  implements IFlowBatchMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    @Qualifier("DispatchingManagerCore")
    private DispatchingManager dispatchingManager;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private FlowBatchMethod flowBatchMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param flowBatchID
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/11/1 13:27:47
     */
    @Override
    public void flowBatchInformationUpdateByOpeStart(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier flowBatchID) {
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));

        com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = baseCoreFactory.getBO(com.fa.cim.newcore.bo.dispatch.CimFlowBatch.class, flowBatchID);
        Validations.check(null == aFlowBatch, retCodeConfig.getNotFoundFlowBatch());

        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == aPerson, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));

        aFlowBatch.makeTargetOperationStarted();

        String envFlowBatchClear = StandardProperties.OM_FLOWB_RSRV_REMOVE_BY_MOVEIN.getValue();
        if (CimStringUtils.equals(String.valueOf(BizConstant.SP_FLOWBATCH_CLEAR), envFlowBatchClear)) {
            aMachine.removeFlowBatch(aFlowBatch);
        }
        aFlowBatch.setLastClaimedPerson(aPerson);
        aFlowBatch.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aMachine.setLatestOperatedUser(aPerson);
        aMachine.setLatestOperatedTimestamp(objCommon.getTimeStamp().getReportTimeStamp());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param equipmentID
     * @param strStartCassette
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/12/26 11:17:02
     */
    @Override
    public void flowBatchInformationUpdateByOpeStartCancel(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette) {


        CimFlowBatch aFlowBatch=null;

        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(aMachine == null, retCodeConfig.getNotFoundEqp());

        Boolean targetFlag = false;
        int sclength;
        sclength = CimArrayUtils.getSize(strStartCassette);
        for (int i=0 ; i<sclength ; i++) {
            List<Infos.LotInCassette> strLotInCassette = strStartCassette.get(i).getLotInCassetteList();
            int LICLength;
            LICLength = CimArrayUtils.getSize(strLotInCassette);
            for (int j=0 ; j<LICLength ; j++) {
                Infos.LotInCassette lotInCassette = strLotInCassette.get(j);
                Boolean operationStartFlag;
                operationStartFlag = lotInCassette.getMoveInFlag();
                if (!CimBooleanUtils.isTrue(operationStartFlag)) {
                    continue;
                }

                com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotInCassette.getLotID());
                Validations.check(CimObjectUtils.isEmpty(aLot), retCodeConfig.getNotFoundLot());

                aFlowBatch = aLot.getFlowBatch();
                if (aFlowBatch==null) {
                    continue;
                }

                com.fa.cim.newcore.bo.pd.CimProcessOperation aPO = aLot.getProcessOperation();
                Validations.check(aPO==null,retCodeConfig.getNotFoundProcessOperation());

                ProcessDTO.PosFlowBatchControl flowBatch = aPO.getFlowBatchControl();
                if ( flowBatch !=null ) {
                    Boolean isFlowBatchTargetOperation = aPO.isFlowBatchTargetOperation();

                    if ( CimBooleanUtils.isTrue(isFlowBatchTargetOperation) ) {
                        targetFlag = true;
                        break;
                    }
                }
            }

            if (CimBooleanUtils.isTrue(targetFlag)) {
                break;
            }
        }

        if (CimBooleanUtils.isTrue(targetFlag)) {

            List<com.fa.cim.newcore.bo.dispatch.CimFlowBatch> reservedFlowBatches = aMachine.allFlowBatches();
            int maxCountForFlowBatch = CimNumberUtils.intValue(aMachine.getFlowBatchMaxCount());

            ObjectIdentifier reservedFlowBatchID;
            ObjectIdentifier flowBatchID;
            String envFlowBatchClear = StandardProperties.OM_FLOWB_RSRV_REMOVE_BY_MOVEIN_CANCEL.getValue();

            if ( aFlowBatch==null ) {
            } else {
                flowBatchID= ObjectIdentifier.build(aFlowBatch.getIdentifier(), aFlowBatch.getPrimaryKey());

                Boolean sameFlowBatchFound = false;
                Boolean removeEqpQueue = false;
                int reservedFlowBatchLen = CimArrayUtils.getSize(reservedFlowBatches);
                for ( int m = 0; m < reservedFlowBatchLen; m++ ) {
                    com.fa.cim.newcore.bo.dispatch.CimFlowBatch reservedFlowBatch = reservedFlowBatches.get(m);
                    reservedFlowBatchID= ObjectIdentifier.build(reservedFlowBatch.getIdentifier(), reservedFlowBatch.getPrimaryKey());

                    if ( CimStringUtils.equals( flowBatchID.getValue(), reservedFlowBatchID.getValue()) ) {
                        sameFlowBatchFound = true;
                        if ( CimStringUtils.equals(envFlowBatchClear,BizConstant.SP_FLOWBATCH_CLEAR.toString())) {
                            aMachine.removeFlowBatch(aFlowBatch);
                            removeEqpQueue = true;
                        }
                        break;
                    }
                }

                if ( !CimBooleanUtils.isTrue(sameFlowBatchFound) ) {
                    if ( CimStringUtils.equals(envFlowBatchClear,BizConstant.SP_FLOWBATCH_RECOVER.toString())) {
                        if( 0 != reservedFlowBatchLen && 0 == maxCountForFlowBatch ) {
                            throw new ServiceException(retCodeConfig.getEqpAlreadyReserved());
                        }

                        aMachine.addFlowBatch(aFlowBatch);
                    } else if( CimStringUtils.equals(envFlowBatchClear,BizConstant.SP_FLOWBATCH_CLEAR.toString()) ) {
                        removeEqpQueue = true;
                    }
                }

                if( CimBooleanUtils.isTrue(removeEqpQueue) ) {
                    List<com.fa.cim.newcore.bo.product.CimLot> aLotSequence = aFlowBatch.allLots();

                    int i, nLen = CimArrayUtils.getSize(aLotSequence);

                    for( i = 0; i < nLen; i++ ) {
                        dispatchingManager.removeFromQueue(aLotSequence.get(i));
                    }
                }
            }

            com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, strObjCommonIn.getUser().getUserID());

            assert aFlowBatch != null;
            aFlowBatch.makeTargetOperationNotStarted();
            aFlowBatch.setLastClaimedPerson(aPerson);
            aFlowBatch.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());
            aMachine.setLatestOperatedUser(aPerson);
            aMachine.setLatestOperatedTimestamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());
        }

    }

    @Override
    public List<Infos.ContainedCassettesInFlowBatch> flowBatchCassetteGet(Infos.ObjCommon objCommon, ObjectIdentifier batchID) {
        com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = baseCoreFactory.getBO(com.fa.cim.newcore.bo.dispatch.CimFlowBatch.class, batchID);
        Validations.check(aFlowBatch == null, new OmCode(retCodeConfig.getNotFoundFlowBatch(), batchID.getValue()));
        List<com.fa.cim.newcore.bo.product.CimLot> aLotList = aFlowBatch.allLots();
        int lenLotInFB = CimArrayUtils.getSize(aLotList);
        Validations.check(lenLotInFB == 0, new OmCode(retCodeConfig.getNotFoundLot(), "*****"));
        List<Infos.ContainedCassettesInFlowBatch> strCantainedCassette = new ArrayList<>();
        if (lenLotInFB > 0) {
            for (com.fa.cim.newcore.bo.product.CimLot lot : aLotList) {
                List<MaterialContainer> aMaterialContainers = lot.materialContainers();
                com.fa.cim.newcore.bo.durable.CimCassette aCassette = null;
                if (!CimArrayUtils.isEmpty(aMaterialContainers)) {
                    aCassette = (com.fa.cim.newcore.bo.durable.CimCassette)aMaterialContainers.get(0);
                }
                Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), ""));
                ObjectIdentifier lotInCassetteID = ObjectIdentifier.build(aCassette.getIdentifier(), aCassette.getPrimaryKey());
                //Found or Add cassette Info
                int nCassetteIdx = -1;
                int lenCassette = CimArrayUtils.getSize(strCantainedCassette);
                for (int j = 0; j < lenCassette; j++) {
                    if (ObjectIdentifier.equalsWithValue(lotInCassetteID, strCantainedCassette.get(j).getCassetteID())) {
                        nCassetteIdx = j;
                        break;
                    }
                }
                Infos.ContainedCassettesInFlowBatch containedCassettesInFlowBatch = new Infos.ContainedCassettesInFlowBatch();
                if (nCassetteIdx < 0) {
                    strCantainedCassette.add(containedCassettesInFlowBatch);
                    containedCassettesInFlowBatch.setCassetteID(lotInCassetteID);
                    nCassetteIdx = lenCassette;
                }
                /*------------------*/
                /*   Add Lot Info   */
                /*------------------*/
                ObjectIdentifier lotID = ObjectIdentifier.build(lot.getIdentifier(), lot.getPrimaryKey());
                Infos.ContainedCassettesInFlowBatch tmpContainedCassettesInFlowBatch = strCantainedCassette.get(nCassetteIdx);
                List<Infos.ContainedLotInCassetteInFlowBatch> strContainedLotInCassetteInFlowBatch = tmpContainedCassettesInFlowBatch.getStrContainedLotInCassetteInFlowBatch();
                if (CimArrayUtils.isEmpty(strContainedLotInCassetteInFlowBatch)){
                    strContainedLotInCassetteInFlowBatch = new ArrayList<>();
                    tmpContainedCassettesInFlowBatch.setStrContainedLotInCassetteInFlowBatch(strContainedLotInCassetteInFlowBatch);
                }
                Infos.ContainedLotInCassetteInFlowBatch containedLotInCassetteInFlowBatch = new Infos.ContainedLotInCassetteInFlowBatch();
                containedLotInCassetteInFlowBatch.setLotID(lotID);
                strContainedLotInCassetteInFlowBatch.add(containedLotInCassetteInFlowBatch);
                //Add wafer Info
                //step1 - lot_materials_GetWafers
                List<Infos.LotWaferAttributes> lotMaterialsGetWafersList = lotMethod.lotMaterialsGetWafers(objCommon, lotID);
                int lenWafer = CimArrayUtils.getSize(lotMaterialsGetWafersList);
                List<Infos.LotWafer> lotWaferList = new ArrayList<>();
                containedLotInCassetteInFlowBatch.setStrLotWafer(lotWaferList);
                for (int k = 0; k < lenWafer; k++) {
                    log.debug("Add Wafer Info");
                    Infos.LotWafer lotWafer = new Infos.LotWafer();
                    lotWafer.setWaferID(lotMaterialsGetWafersList.get(k).getWaferID());
                    lotWafer.setSlotNumber(lotMaterialsGetWafersList.get(k).getSlotNumber().longValue());
                    lotWafer.setControlWaferFlag(lotMaterialsGetWafersList.get(k).getControlWaferFlag());
                    lotWaferList.add(lotWafer);
                }
            }
        }
        return strCantainedCassette;
    }

    @Override
    public Outputs.ObjFlowBatchMakeOut flowBatchMake(Infos.ObjCommon objCommon, List<Infos.BatchingReqLot> batchingReqLots) {
        Outputs.ObjFlowBatchMakeOut objFlowBatchMakeOut = new Outputs.ObjFlowBatchMakeOut();
        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        int lenLot = CimArrayUtils.getSize(batchingReqLots);
        List<com.fa.cim.newcore.bo.product.CimLot> aLotList = new ArrayList<>();
        com.fa.cim.newcore.bo.product.CimLot aLot = null;
        List<Infos.BatchedLot> strBatchedLot = new ArrayList<>();
        objFlowBatchMakeOut.setStrBatchedLot(strBatchedLot);
        for (int i = 0; i < lenLot; i++){
            aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, batchingReqLots.get(i).getLotID());
            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), batchingReqLots.get(i).getLotID().getValue()));
            aLotList.add(aLot);
            Infos.BatchedLot batchedLot = new Infos.BatchedLot();
            strBatchedLot.add(batchedLot);
            batchedLot.setLotID(batchingReqLots.get(i).getLotID());
            batchedLot.setCassetteID(batchingReqLots.get(i).getCassetteID());
            batchedLot.setProcessSequenceNumber(i + 1L);
            dispatchingManager.removeFromQueue(aLot);
            aLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aLot.setLastClaimedPerson(aPerson);
        }
        com.fa.cim.newcore.bo.dispatch.CimFlowBatchDispatcher aFlowBatchDispatcher = dispatchingManager.getFlowBatchDispatcher();
        Validations.check(aFlowBatchDispatcher == null, new OmCode(retCodeConfig.getNotFoundSystemObj(), "FlowBatchDispatcher"));
        String id = aFlowBatchDispatcher.getNextFlowBatchIdentifier();
        com.fa.cim.newcore.bo.dispatch.CimFlowBatch aNewFlowBatch = dispatchingManager.createFlowBatch(id);
        Validations.check(aNewFlowBatch == null, new OmCode(retCodeConfig.getNotFoundFlowBatch(), ""));
        boolean reFlowBatchingFlag = false;
        if (CimStringUtils.equals(objCommon.getTransactionID(), "OFLWW006")){
            reFlowBatchingFlag = true;
        }
        aNewFlowBatch.setLots(aLotList, reFlowBatchingFlag);
        objFlowBatchMakeOut.setBatchID(new ObjectIdentifier(aNewFlowBatch.getIdentifier(), aNewFlowBatch.getPrimaryKey()));
        aNewFlowBatch.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aNewFlowBatch.setLastClaimedPerson(aPerson);
        return objFlowBatchMakeOut;
    }

    @Override
    public ObjectIdentifier flowBatchReserveEquipmentIDGet(Infos.ObjCommon objCommon, ObjectIdentifier flowBatchID) {
        com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = baseCoreFactory.getBO(com.fa.cim.newcore.bo.dispatch.CimFlowBatch.class, flowBatchID);
        Validations.check(aFlowBatch == null, new OmCode(retCodeConfig.getNotFoundFlowBatch(), flowBatchID.getValue()));
        CimMachine aMachine = aFlowBatch.getMachine();
        if (aMachine != null){
            ObjectIdentifier equipmentID = new ObjectIdentifier(aMachine.getIdentifier(), aMachine.getPrimaryKey());
            throw new ServiceException(retCodeConfig.getFlowBatchReservedEqpIdFilled(), equipmentID);
        }
        throw new ServiceException(retCodeConfig.getFlowBatchReservedEqpIdBlank());
    }

    @Override
    public Outputs.ObjFlowBatchLostLotsListGetDRout flowBatchLostLotsListGetDR(Infos.ObjCommon objCommon) {
        Outputs.ObjFlowBatchLostLotsListGetDRout out = new Outputs.ObjFlowBatchLostLotsListGetDRout();
        List<Infos.FlowBatchLostLotInfo> flowBatchedCassetteInfoList = new ArrayList<Infos.FlowBatchLostLotInfo>();
        //----------------------------------------------------
        //    step1 Get lot List for flow batch dispatch Queue
        //----------------------------------------------------
        String sql = "SELECT LOT_KEY\n" +
                "               FROM OMFLOWBDISP_LOT\n" +
                "               ORDER BY LOT_KEY";
        List<CimFlowDispatcherLotDO> flowDispatcherLots = cimJpaRepository.query(sql, CimFlowDispatcherLotDO.class);
        // SPDynamicTableS< char*, char*, char*, char*, stringSequence, stringSequence > flowBatchDPLotIDList;
        Map<String, String> flowBatchDPLotIDMap = new HashMap<>();
        if (flowDispatcherLots != null && flowDispatcherLots.size() > 0) {
            for (CimFlowDispatcherLotDO flowDispatcherLot : flowDispatcherLots) {
                if (!flowBatchDPLotIDMap.containsKey(flowDispatcherLot.getLotKey())) {
                    flowBatchDPLotIDMap.put(flowDispatcherLot.getLotKey(), flowDispatcherLot.getLotKey());
                }
            }
        }
        //----------------------------------------------------
        //    step2 Get lot List for Waiting lot List
        //----------------------------------------------------
        sql = "SELECT A.ID,\n" +
                "                    A.LOT_ID,\n" +
                "                    A.PROPE_RKEY,\n" +
                "                    A.FLOWB_ID,\n" +
                "                    B.ROUTE_PRSS_RKEY\n" +
                "               FROM OMLOT A, OMPROPE B\n" +
                "              WHERE A.LOT_FINISHED_STATE is NULL\n" +
                "                AND A.LOT_HOLD_STATE     = 'NOTONHOLD'\n" +
                "                AND A.LOT_INV_STATE      = 'OnFloor'\n" +
                "                AND A.LOT_PROCESS_STATE  = 'Waiting'\n" +
                "                AND A.PROPE_RKEY         = B.ID\n" +
                "              ORDER BY A.LOT_ID";
        List<Object[]> waitingLotList = cimJpaRepository.query(sql);
        if (!CimArrayUtils.isEmpty(waitingLotList)) {
            for (Object[] objects : waitingLotList) {
                String lotObj = (null == objects[0]) ? null : String.valueOf(objects[0]);
                String lotId = (null == objects[1]) ? null : String.valueOf(objects[1]);
                String poObj = (null == objects[2]) ? null : String.valueOf(objects[2]);
                String flowBatchId = (null == objects[3]) ? null : String.valueOf(objects[3]);
                String modPosObj = (null == objects[4]) ? null : String.valueOf(objects[4]);
                Boolean bLostLot = false;
                //----------------------------------------------------
                //    step3 Get ProcessOperation Object
                //----------------------------------------------------
                Outputs.ObjProcessFlowBatchDefinitionGetDROut flowBatchOut = processMethod.processFlowBatchDefinitionGetDR(objCommon, modPosObj);
                Infos.FlowBatchControlInfo flowBatchControlInfo = new Infos.FlowBatchControlInfo();
                flowBatchControlInfo.setName(flowBatchOut.getFlowBatchControl().getName());
                flowBatchControlInfo.setTargetOperation(flowBatchOut.getFlowBatchSection().getTargetOperationFlag());
                Boolean bEntryPoint = flowBatchOut.getFlowBatchSection().getEntryOperationFlag();
                if (CimStringUtils.isEmpty(flowBatchId)) {
                    if (!CimBooleanUtils.isTrue(bEntryPoint) && !CimStringUtils.isEmpty(flowBatchControlInfo.getName())) {
                        bLostLot = true;
                    }
                }
                if (CimBooleanUtils.isFalse(bLostLot)) {
                    if (flowBatchDPLotIDMap.containsKey(lotId)) {
                        if (CimStringUtils.isEmpty(flowBatchControlInfo.getName()) && !CimBooleanUtils.isTrue(bEntryPoint)) {
                            bLostLot = true;
                        }
                    }
                }
                if (CimBooleanUtils.isFalse(bLostLot) && !CimStringUtils.isEmpty(flowBatchId)) {
                    sql = String.format("SELECT FLOWB_ID,\n" +
                            "                                    EQP_ID\n" +
                            "                               FROM OMFLOWB\n" +
                            "                              WHERE FLOWB_ID = '%s'", flowBatchId);
                    CimFlowBatchDO flowBatch = cimJpaRepository.queryOne(sql, CimFlowBatchDO.class);
                    if (CimObjectUtils.isEmpty(flowBatch) || CimStringUtils.isEmpty(flowBatch.getEquipmentID())) {
                        log.info("No eqp Reservation, skipped");
                    } else if (!CimStringUtils.isEmpty(flowBatchControlInfo.getName())) {
                        sql = String.format("SELECT EQP_ID\n" +
                                "                                       FROM OMLOT_EQP\n" +
                                "                                      WHERE REFKEY = '%s'\n" +
                                "                                        AND EQP_ID = '%s'", lotObj, flowBatch.getEquipmentID());
                        CimLotEquipmentDO lotEquipment = cimJpaRepository.queryOne(sql, CimLotEquipmentDO.class);
                        if (lotEquipment != null){
                            if (CimStringUtils.equals(flowBatchId, lotEquipment.getEquipmentID()) && !CimBooleanUtils.isTrue(flowBatchControlInfo.getTargetOperation())) {
                                bLostLot = true;
                            }
                        }
                    }
                }
                if (CimBooleanUtils.isFalse(bLostLot)) {
                    if (!CimStringUtils.isEmpty(flowBatchId) && CimStringUtils.isEmpty(flowBatchControlInfo.getName())) {
                        bLostLot = true;
                    }
                }

                if (CimBooleanUtils.isTrue(bLostLot)) {
                    //------------------------
                    //   Add Lost Lots List
                    //------------------------
                    sql = String.format("SELECT CARRIER_ID,\n" +
                            "                           ID\n" +
                            "                    FROM   OMCARRIER\n" +
                            "                    WHERE  ID\n" +
                            "                           IN (\n" +
                            "                              SELECT REFKEY\n" +
                            "                              FROM   OMCARRIER_LOT\n" +
                            "                              WHERE  LOT_ID = '%s'\n" +
                            "                               )", lotId);
                    CimCassetteDO cassette = cimJpaRepository.queryOne(sql, CimCassetteDO.class);
                    if (cassette == null) {
                        Infos.FlowBatchLostLotInfo flowBatchLostLotInfo = new Infos.FlowBatchLostLotInfo();
                        flowBatchLostLotInfo.setLotID(ObjectIdentifier.build(lotId, lotObj));
                        flowBatchLostLotInfo.setCassetteID(ObjectIdentifier.emptyIdentifier());
                        flowBatchedCassetteInfoList.add(flowBatchLostLotInfo);
                    } else {
                        Infos.FlowBatchLostLotInfo flowBatchLostLotInfo = new Infos.FlowBatchLostLotInfo();
                        flowBatchLostLotInfo.setLotID(ObjectIdentifier.build(lotId, lotObj));
                        flowBatchLostLotInfo.setCassetteID(ObjectIdentifier.build(cassette.getCassetteID(), cassette.getId()));
                        flowBatchedCassetteInfoList.add(flowBatchLostLotInfo);
                    }
                }
            }
        }

        out.setFlowBatchedCassetteInfoList(flowBatchedCassetteInfoList);
        return out;
    }

    public Outputs.ObjFlowBatchInformationGetOut flowBatchInformationGet(Infos.ObjCommon objCommon, Infos.FlowBatchInformation flowBatchInformation) {
        Outputs.ObjFlowBatchInformationGetOut objFlowBatchInformationGetOut = new Outputs.ObjFlowBatchInformationGetOut();
        objFlowBatchInformationGetOut.setMaxCountForFlowBatch(0L);
        List<com.fa.cim.newcore.bo.dispatch.CimFlowBatch> flowBatches = new ArrayList<>();
        // Get lot's flowbatch;
        if (!ObjectIdentifier.isEmptyWithValue(flowBatchInformation.getLotID())) {
            com.fa.cim.newcore.bo.product.CimLot aFlwBatchLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, flowBatchInformation.getLotID());
            Validations.check(aFlwBatchLot == null, new OmCode(retCodeConfig.getNotFoundLot(), flowBatchInformation.getLotID().getValue()));
            com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = aFlwBatchLot.getFlowBatch();
            if (aFlowBatch != null){
                flowBatches.add(aFlowBatch);
            }
            //-------------------------------------------------
            // Get Equipment's FlowBatch
            //------------------------------------------------- 
        } else if (!ObjectIdentifier.isEmptyWithValue(flowBatchInformation.getEquipmentID())) {
            CimMachine aFlwBatchMachine = baseCoreFactory.getBO(CimMachine.class, flowBatchInformation.getEquipmentID());
            Validations.check(aFlwBatchMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), flowBatchInformation.getEquipmentID().getValue()));
            flowBatches = aFlwBatchMachine.allFlowBatches();
        } else {
            // Get flowbatch;
            com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = dispatchingManager.findFlowBatchNamed(flowBatchInformation.getFlowBatchID().getValue());
            if (aFlowBatch != null){
                flowBatches.add(aFlowBatch);
            }
        }
        int flowBatchesCount = CimArrayUtils.getSize(flowBatches);
        Validations.check(flowBatchesCount == 0, new OmCode(retCodeConfig.getNotFoundFlowBatch(), "*****"));
        List<Infos.FlowBatchInfo> flowBatchInfoList = new ArrayList<>();
        //Set each flowbatch;
        for (int m = 0; m < flowBatchesCount; m++) {
            com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = flowBatches.get(m);
            Integer operationCount = aFlowBatch.getOperationCount();
            Integer flowBatchCount = aFlowBatch.getFlowBatchSize();
            List<com.fa.cim.newcore.bo.product.CimLot> lotList = aFlowBatch.allLots();
            int nLen = CimArrayUtils.getSize(lotList);
            List<Infos.FlowBatchedCassetteInfoExtend> flowBatchedCassetteInfoExtendList = new ArrayList<>();
            int castLen = 0;
            for (int n = 0; n < nLen; n++) {
                List<MaterialContainer> aMaterialContainerSeq = lotList.get(n).materialContainers();
                com.fa.cim.newcore.bo.durable.CimCassette aCassette = null;
                if (!CimArrayUtils.isEmpty(aMaterialContainerSeq)){
                    aCassette = (com.fa.cim.newcore.bo.durable.CimCassette) aMaterialContainerSeq.get(0);
                }
                Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), ""));
                Infos.FlowBatchedCassetteInfoExtend flowBatchedCassetteInfo = new Infos.FlowBatchedCassetteInfoExtend();
                flowBatchedCassetteInfoExtendList.add(flowBatchedCassetteInfo);
                //Step01 make all sequence by lot (cassette count is same as lots.)
                String cassetteIdentifier = aCassette.getIdentifier();
                flowBatchedCassetteInfo.setCassetteID(new ObjectIdentifier(cassetteIdentifier));
                String lotID = lotList.get(n).getIdentifier();
                ObjectIdentifier lotIDObj = ObjectIdentifier.build(lotID, null);
                Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
                lotInfoInqFlag.setLotBasicInfoFlag(true);
                lotInfoInqFlag.setLotControlUseInfoFlag(false);
                lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
                lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
                lotInfoInqFlag.setLotOperationInfoFlag(false);
                lotInfoInqFlag.setLotOrderInfoFlag(false);
                lotInfoInqFlag.setLotControlJobInfoFlag(false);
                lotInfoInqFlag.setLotProductInfoFlag(true);
                lotInfoInqFlag.setLotRecipeInfoFlag(false);
                lotInfoInqFlag.setLotLocationInfoFlag(true);
                lotInfoInqFlag.setLotWipOperationInfoFlag(false);
                lotInfoInqFlag.setLotWaferAttributesFlag(false);
                lotInfoInqFlag.setLotBackupInfoFlag(false);
                Infos.LotInfo lotInfoRetCode = lotMethod.lotDetailInfoGetDR(objCommon, lotInfoInqFlag, lotIDObj);
                List<Infos.FlowBatchedLotInfoExtend> flowBatchedLotInfoList = new ArrayList<>();
                Infos.FlowBatchedLotInfoExtend flowBatchedLotInfoExtend = new Infos.FlowBatchedLotInfoExtend();

                String lotStatus = (lotInfoRetCode.getLotBasicInfo() == null) ? "" :
                        lotInfoRetCode.getLotBasicInfo().getLotStatus();
                flowBatchedLotInfoExtend.setLotStatus(lotStatus);

                String transferStatus = (lotInfoRetCode.getLotLocationInfo() == null) ? "" :
                        lotInfoRetCode.getLotLocationInfo().getTransferStatus();
                flowBatchedLotInfoExtend.setTransferStatus(transferStatus);

                ObjectIdentifier stockerID = (lotInfoRetCode.getLotLocationInfo() == null) ? new ObjectIdentifier(null) :
                        lotInfoRetCode.getLotLocationInfo().getStockerID();
                flowBatchedLotInfoExtend.setStockerID(stockerID);

                ObjectIdentifier equipmentID = (lotInfoRetCode.getLotLocationInfo() == null) ? new ObjectIdentifier(null) :
                        lotInfoRetCode.getLotLocationInfo().getEquipmentID();
                flowBatchedLotInfoExtend.setEquipmentID(equipmentID);

                String priorityClass = (lotInfoRetCode.getLotBasicInfo() == null) ? "" :
                        ((lotInfoRetCode.getLotBasicInfo().getPriorityClass() != null) ? lotInfoRetCode.getLotBasicInfo().getPriorityClass().toString() : BizConstant.EMPTY);
                flowBatchedLotInfoExtend.setPriorityClass(priorityClass);

                ObjectIdentifier productID = (lotInfoRetCode.getLotProductInfo() == null) ? new ObjectIdentifier(null) :
                        lotInfoRetCode.getLotProductInfo().getProductID();
                flowBatchedLotInfoExtend.setProductID(productID);

                flowBatchedLotInfoExtend.setFlowBatchLotSize(flowBatchCount.longValue());
                flowBatchedLotInfoExtend.setFlowBatchOperationCount(operationCount.longValue());
                List<Infos.EntityInhibitAttributes> entityInhibitionsList = new ArrayList<>();
                int lotEntityInhibitionsCount = CimArrayUtils.getSize(lotInfoRetCode.getEntityInhibitAttributesList());
                if (lotEntityInhibitionsCount > 0) {
                    entityInhibitionsList.addAll(lotInfoRetCode.getEntityInhibitAttributesList());
                }
                flowBatchedLotInfoExtend.setEntityInhibitionsList(entityInhibitionsList);
                flowBatchedLotInfoExtend.setLotID(lotIDObj);
                flowBatchedLotInfoList.add(flowBatchedLotInfoExtend);
                flowBatchedCassetteInfo.setFlowBatchedLotInfoList(flowBatchedLotInfoList);
            }

            //Step02 re-make lot's sequence(cassette count is same as lots.)
            List<Infos.FlowBatchedCassetteInfoExtend> newFlowBatchedCassetteInfoList = new ArrayList<>();
            for (int p = 0; p < nLen; p++) {
                Infos.FlowBatchedCassetteInfoExtend flowBatchedCassetteInfoExtend = flowBatchedCassetteInfoExtendList.get(p);
                String crrCastID = flowBatchedCassetteInfoExtend.getCassetteID().getValue();
                Boolean sameCastFlag = false;
                int targetArrayNo = 0;
                for (int q = 0; q < p; q++) {
                    Infos.FlowBatchedCassetteInfoExtend preFlowBatchedCassetteInfoExtend = flowBatchedCassetteInfoExtendList.get(q);
                    if (ObjectIdentifier.equalsWithValue(crrCastID, preFlowBatchedCassetteInfoExtend.getCassetteID())) {
                        int lotLenOld = flowBatchedCassetteInfoExtendList.get(q).getFlowBatchedLotInfoList().size();
                        int lotLenCur = flowBatchedCassetteInfoExtendList.get(p).getFlowBatchedLotInfoList().size();
                        int lotLenNew = lotLenOld + lotLenCur;
                        targetArrayNo = q;
                        sameCastFlag = true;
                        for (int r = 0; r < lotLenCur; r++) {
                            flowBatchedCassetteInfoExtendList.get(q).getFlowBatchedLotInfoList().add(flowBatchedCassetteInfoExtendList.get(p).getFlowBatchedLotInfoList().get(r));
                        }
                        for (int x = 0; x < castLen; x++) {
                            if (ObjectIdentifier.equalsWithValue(flowBatchedCassetteInfoExtendList.get(q).getCassetteID(), newFlowBatchedCassetteInfoList.get(x).getCassetteID())) {
                                newFlowBatchedCassetteInfoList.set(x, flowBatchedCassetteInfoExtendList.get(q));
                            }
                        }
                        break;
                    }
                }
                if (!sameCastFlag) {
                    newFlowBatchedCassetteInfoList.add(flowBatchedCassetteInfoExtendList.get(p));
                    castLen++;
                }
            }
            CimMachine aMachine = aFlowBatch.getMachine();
            if (aMachine != null) {
                objFlowBatchInformationGetOut.setReservedEquipmentID(ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
                Integer maxCountForFlowBatch = aMachine.getFlowBatchMaxCount();
                objFlowBatchInformationGetOut.setMaxCountForFlowBatch(CimNumberUtils.longValue(maxCountForFlowBatch));
            }
            Infos.FlowBatchInfo flowBatchInfo = new Infos.FlowBatchInfo();
            flowBatchInfo.setFlowBatchID(ObjectIdentifier.build(aFlowBatch.getIdentifier(), aFlowBatch.getPrimaryKey()));
            flowBatchInfo.setFlowBatchedCassetteInfoList(newFlowBatchedCassetteInfoList);
            flowBatchInfoList.add(flowBatchInfo);
        }
        objFlowBatchInformationGetOut.setFlowBatchInfoList(flowBatchInfoList);
        return objFlowBatchInformationGetOut;
    }

    @Override
    public Outputs.ObjFlowBatchInfoSortByCassetteOut flowBatchInfoSortByCassette(Infos.ObjCommon objCommon, List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette) {
        List<Infos.FlowBatchByManualActionReqCassette> tempFlowBatchByManualActionReqCassettes = new ArrayList<>();
        Outputs.ObjFlowBatchInfoSortByCassetteOut out = new Outputs.ObjFlowBatchInfoSortByCassetteOut();
        out.setFlowBatchByManualActionReqCassetteList(tempFlowBatchByManualActionReqCassettes);

        //----------------
        //   Initialize
        //----------------
        int i, j;
        for (i = 0; i < strFlowBatchByManualActionReqCassette.size(); i++) {
            // Check exist same cassetteID
            Boolean bFound = false;
            List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassetteList = tempFlowBatchByManualActionReqCassettes;
            for (j = 0; j < flowBatchByManualActionReqCassetteList.size(); j++) {
                if (ObjectIdentifier.equalsWithValue(strFlowBatchByManualActionReqCassette.get(i).getCassetteID(), flowBatchByManualActionReqCassetteList.get(j).getCassetteID())) {
                    // Add lot info

                    if (null == flowBatchByManualActionReqCassetteList.get(j).getLotID()) {
                        flowBatchByManualActionReqCassetteList.get(j).setLotID(new ArrayList<>());
                    }
                    flowBatchByManualActionReqCassetteList.get(j).getLotID().addAll(strFlowBatchByManualActionReqCassette.get(i).getLotID());
                    // found same cassetteID
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                log.info("Not found same cassetteID ---> Add cassetteID to OutStruct");
                // Add cassetteID
                Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = new Infos.FlowBatchByManualActionReqCassette();
                flowBatchByManualActionReqCassette.setCassetteID(strFlowBatchByManualActionReqCassette.get(i).getCassetteID());
                // Add lot info
                flowBatchByManualActionReqCassette.setLotID(strFlowBatchByManualActionReqCassette.get(i).getLotID());
                flowBatchByManualActionReqCassetteList.add(flowBatchByManualActionReqCassette);
            }
        }
        return out;
    }

    @Override
    public void flowBatchInformationUpdateByOpeComp(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList) {

        com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = null;
        // Get Machine Object  ;
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);

        //Check lot is on the target operation of flowbatch ;
        Boolean targetFlag = false;
        int scLength = CimArrayUtils.getSize(startCassetteList);
        for (int i = 0; i < scLength; i++) {
            Infos.StartCassette startCassette = startCassetteList.get(i);
            int licLength = CimArrayUtils.getSize(startCassette.getLotInCassetteList());
            for (int j = 0; j < licLength; j++) {
                Infos.LotInCassette lotInCassette = startCassette.getLotInCassetteList().get(j);
                // Omit Not-Start lot;
                Boolean operationStartFlag = lotInCassette.getMoveInFlag();
                if (!operationStartFlag) {
                    continue;
                }

                //Get lot Object;
                com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotInCassette.getLotID());

                // Get Flow Batch Object from lot ;
                aFlowBatch = aLot.getFlowBatch();
                if (aFlowBatch == null) {
                    continue;
                }

                //Check Target or Not ;
                com.fa.cim.newcore.bo.pd.CimProcessOperation aPosPO = aLot.getProcessOperation();
                Validations.check(aPosPO == null, new OmCode(retCodeConfig.getNotFoundProcessOperation(), "", lotInCassette.getLotID().getValue()));

                ProcessDTO.PosFlowBatchControl flowBatch = aPosPO.getFlowBatchControl();
                if (flowBatch != null) {
                    Boolean isFlowBatchTargetOperation = aPosPO.isFlowBatchTargetOperation();
                    if (isFlowBatchTargetOperation) {
                        targetFlag = TRUE;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isTrue(targetFlag)) {
                break;
            }
        }

        //Update Flow Batch Information ;
        if (CimBooleanUtils.isTrue(targetFlag)) {
            // Get person Object ;
            com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
            Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
            //Set TargetOperationStartFlag to FALSE ;
            if (null != aFlowBatch) {
                aFlowBatch.makeTargetOperationNotStarted();
            }

            //Delete the Flow Batch Reservation;
            aMachine.removeFlowBatch(aFlowBatch);
        }

    }

    @Override
    public Results.FloatingBatchListInqResult flowBatchFillInTxDSQ002DR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        Results.FloatingBatchListInqResult data = new Results.FloatingBatchListInqResult();
        data.setEquipmentID(equipmentID);
        List<Infos.FloatBatch> floatBatches = new ArrayList<>();
        data.setFloatBatches(floatBatches);
        List<com.fa.cim.newcore.bo.dispatch.CimFlowBatch> flowBatches = dispatchingManager.allFloatingFlowBatches();
        int lenFB = CimArrayUtils.getSize(flowBatches);
        Validations.check(lenFB == 0, new OmCode(retCodeConfig.getNotFoundFlowBatch(), "******"));

        int flowBatchSize = 0;
        boolean isBatchedLotInfoEmpty = true;
        int processSeqNo = 1;

        for (int i=0; i < lenFB; i++) {
            com.fa.cim.newcore.bo.dispatch.CimFlowBatch flowBatch = flowBatches.get(i);
            ObjectIdentifier flowBatchID = ObjectIdentifier.build(flowBatch.getIdentifier(), flowBatch.getPrimaryKey());
            int flowBatchOperationCount = flowBatch.getOperationCount();
            List<com.fa.cim.newcore.bo.product.CimLot> lots = flowBatch.allLots();
            int lotSeqLen = CimArrayUtils.getSize(lots);
            Validations.check(lotSeqLen == 0, new OmCode(retCodeConfig.getNotFoundLot(), "*****"));

            com.fa.cim.newcore.bo.pd.CimProcessOperation curPO = lots.get(0).getProcessOperation();
            Validations.check(null == curPO, new OmCode(retCodeConfig.getNotFoundProcessOperation(), ""));

            CimProductSpecification productSpecification = lots.get(0).getProductSpecification();
            Validations.check(null == productSpecification, new OmCode(retCodeConfig.getNotFoundOperation(), ""));

            boolean isTargetOperationFlag = curPO.isFlowBatchTargetOperation();
            List<CimMachine>  aMachineSequence = null;

            if (CimBooleanUtils.isTrue(isTargetOperationFlag)) {
                aMachineSequence = curPO.findMachinesFor(productSpecification);
            } else {
                com.fa.cim.newcore.bo.pd.CimProcessFlowContext processFlowContext = lots.get(0).getProcessFlowContext();
                Validations.check(CimObjectUtils.isEmpty(processFlowContext), new OmCode(retCodeConfig.getNotFoundPfx(), ""));

                com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification processOperationSpecification = processFlowContext.findFlowBatchTargetOperationSpecification();
                if (processOperationSpecification == null){
                    ObjectIdentifier tmpLotID = new ObjectIdentifier(lots.get(0).getIdentifier(), lots.get(0).getPrimaryKey());
                    throw new ServiceException(new OmCode(retCodeConfig.getNotFoundPos(), "*****", tmpLotID.getValue()));
                }

                List<ProcessDefinition> processDefinitions = processOperationSpecification.getProcessDefinitions();
                if (CimArrayUtils.isNotEmpty(processDefinitions)) {
                    CimProcessDefinition processDefinitionBO = (CimProcessDefinition)processDefinitions.get(0);
                    Validations.check(null == processDefinitionBO, new OmCode(retCodeConfig.getNotFoundSystemObj(), ""), retCodeConfig.getNotFoundProcessDefinition().getMessage());
                    aMachineSequence = processDefinitionBO.findMachinesFor(productSpecification);
                }
            }
            int machineSeqLength = CimArrayUtils.getSize(aMachineSequence);
            for (int count = 0; count < machineSeqLength; count++) {
                CimMachine cimMachine = aMachineSequence.get(count);
                String aMachineID = cimMachine.getIdentifier();
                if (ObjectIdentifier.equalsWithValue(equipmentID, aMachineID)) {
                    flowBatchSize = flowBatches.get(i).getFlowBatchSize();
                    for (int j = 0; j < CimArrayUtils.getSize(lots); j++) {
                        com.fa.cim.newcore.bo.product.CimLot lot = lots.get(j);
                        ObjectIdentifier lotID = ObjectIdentifier.build(lot.getIdentifier(), lot.getPrimaryKey());
                        isBatchedLotInfoEmpty = false;
                        List<MaterialContainer> aMaterialContainerSeq = lot.materialContainers();
                        /*--------------------*/
                        /*   Set CassetteID   */
                        /*--------------------*/
                        int lenLotInCassette = CimArrayUtils.getSize(aMaterialContainerSeq);
                        ObjectIdentifier cassetteID = null;
                        CimCassette cassette = null;
                        if (lenLotInCassette > 0) {
                            cassette = (CimCassette)aMaterialContainerSeq.get(0);
                            cassetteID = ObjectIdentifier.build(cassette.getIdentifier(), cassette.getPrimaryKey());
                        }
                        int cassetteIndex = -1;
                        int lenFlowBatch = CimArrayUtils.getSize(data.getFloatBatches());
                        for (int k = 0; k < lenFlowBatch; k++) {
                            Infos.FloatBatch floatBatch = floatBatches.get(k);
                            if (ObjectIdentifier.equalsWithValue(floatBatch.getCassetteID(), cassetteID)) {
                                cassetteIndex = k;
                                break;
                            }
                        }
                        Infos.FloatBatch tmpFloatBatch = new Infos.FloatBatch();
                        if (cassetteIndex < 0) {
                            cassetteIndex = lenFlowBatch;
                            tmpFloatBatch.setCassetteID(cassetteID);
                            tmpFloatBatch.setFlowBatchID(flowBatchID);
                            tmpFloatBatch.setFlowBatchOperationCount(flowBatchOperationCount);
                            floatBatches.add(tmpFloatBatch);
                        }
                        /*---------------*/
                        /*   Set LotID   */
                        /*---------------*/
                        int lotIndex = -1;
                        List<Infos.FlowBatchedLotInfo> flowBatchedLotInfos = floatBatches.get(cassetteIndex).getFlowBatchedLotInfos();
                        int lenFBLot = CimArrayUtils.getSize(flowBatchedLotInfos);
                        for (int k = 0; k < lenFBLot; k++) {
                            Infos.FlowBatchedLotInfo flowBatchedLotInfo = flowBatchedLotInfos.get(k);
                            if (ObjectIdentifier.equalsWithValue(flowBatchedLotInfo.getLotID(), lotID)) {
                                lotIndex = k;
                                break;
                            }
                        }
                        if (lotIndex < 0) {
                            if (flowBatchedLotInfos == null){
                                flowBatchedLotInfos = new ArrayList<>();
                            }
                            Infos.FlowBatchedLotInfo tmpFlowBatchedLotInfo = new Infos.FlowBatchedLotInfo();
                            tmpFlowBatchedLotInfo.setLotID(lotID);
                            tmpFlowBatchedLotInfo.setProcessSequenceNumber(processSeqNo);
                            tmpFlowBatchedLotInfo.setFlowBatchOperationCount(flowBatchOperationCount);
                            tmpFlowBatchedLotInfo.setFlowBatchLotSize(flowBatchSize);
                            flowBatchedLotInfos.add(tmpFlowBatchedLotInfo);
                            tmpFloatBatch.setFlowBatchedLotInfos(flowBatchedLotInfos);
                        }

                    }
                }
            }
        }

        Validations.check(isBatchedLotInfoEmpty, retCodeConfig.getNotFoundFlowBatch());

        return data;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   objCommon
     * @param equipmentID equipmentID
     * @param flowBatchID flowBatchID
     * @return RetCode
     * @author ZQI
     * @date 2018/12/20 10:59:22
     */
    @Override
    public void flowBatchReserveEquipmentIDClear(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier flowBatchID) {
        log.info("flowBatchReserveEquipmentIDClear():come in the flowBatchReserveEquipmentIDClear method...");
        com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = baseCoreFactory.getBO(com.fa.cim.newcore.bo.dispatch.CimFlowBatch.class, flowBatchID);
        Validations.check(aFlowBatch == null, new OmCode(retCodeConfig.getNotFoundFlowBatch(), flowBatchID.getValue()));
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
        // set the flow Batch eqp info.
        aFlowBatch.setMachine(null);
        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        aFlowBatch.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aFlowBatch.setLastClaimedPerson(aPerson);
        aMachine.setLatestOperatedTimestamp(objCommon.getTimeStamp().getReportTimeStamp());
        aMachine.setLatestOperatedUser(aPerson);
        aMachine.removeFlowBatch(aFlowBatch);
        // Remove lots from waiting lots queue for eqp DCR743.
        List<com.fa.cim.newcore.bo.product.CimLot> aLotList = aFlowBatch.allLots();
        if (!CimArrayUtils.isEmpty(aLotList)) {
            for (com.fa.cim.newcore.bo.product.CimLot lot : aLotList) {
                dispatchingManager.removeFromQueue(lot);
            }
        }
        log.info("flowBatchReserveEquipmentIDClear():exit flowBatchReserveEquipmentIDClear...");
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param batchID
     * @return RetCode<List                                                                                                                                                                                                                                                               <                                                                                                                                                                                                                                                               Infos.ContainedLotsInFlowBatch>>
     * @author ZQI
     * @date 2018/12/21 14:01:42
     */
    @Override
    public List<Infos.ContainedLotsInFlowBatch> flowBatchLotGet(Infos.ObjCommon objCommon, ObjectIdentifier batchID) {
        log.info("getFlowBatchLot():come in the getFlowBatchLot method...");
        // get the flow batch by the flowBatchID;
        com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = baseCoreFactory.getBO(com.fa.cim.newcore.bo.dispatch.CimFlowBatch.class, batchID);
        Validations.check(aFlowBatch == null, new OmCode(retCodeConfig.getNotFoundFlowBatch(), batchID.getValue()));
        // get the all lots info by the flow batch.
        List<com.fa.cim.newcore.bo.product.CimLot> strLots = aFlowBatch.allLots();
        int lenLot = CimArrayUtils.getSize(strLots);
        if (lenLot == 0) {
            log.info("getFlowBatchLot():not found lots info by the flowbatch.", batchID);
            throw new ServiceException(new OmCode(retCodeConfig.getNotFoundLot(), "*****"));
        }
        List<Infos.ContainedLotsInFlowBatch> containedLotsInFlowBatchList = new ArrayList<>();
        long count = 0;
        for (com.fa.cim.newcore.bo.product.CimLot lot : strLots) {
            log.info("getFlowBatchLot(): lot : " + lot.getIdentifier());
            Infos.ContainedLotsInFlowBatch containedLotsInFlowBatch = new Infos.ContainedLotsInFlowBatch();
            containedLotsInFlowBatchList.add(containedLotsInFlowBatch);
            containedLotsInFlowBatch.setLotID(new ObjectIdentifier(lot.getIdentifier(), lot.getPrimaryKey()));
            List<MaterialContainer> strMaterialContainers = lot.materialContainers();
            int mcLen = CimArrayUtils.getSize(strMaterialContainers);
            int j;
            for (j = 0; j < mcLen; j++){
                if (strMaterialContainers.get(j) == null){
                    continue;
                } else {
                    break;
                }
            }
            if (j < mcLen){
                containedLotsInFlowBatch.setCassetteID(new ObjectIdentifier(strMaterialContainers.get(j).getIdentifier(), strMaterialContainers.get(j).getPrimaryKey()));
            }
            containedLotsInFlowBatch.setProcessSequenceNumber(++count);
        }
        log.info("getFlowBatchLot():exit the getFlowBatchLot method...");
        return containedLotsInFlowBatchList;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   objCommon
     * @param equipmentID equipmentID
     * @param batchID flowBatchID
     * @return RetCode<Object>
     * @author ZQI
     * @date 2018/12/21 17:28:22
     */
    @Override
    public void flowBatchReserveEquipmentIDSet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier batchID) {
        log.debug("setFlowBatchReserveEquipmentID():come in the setFlowBatchReserveEquipmentID method...");
        // get flow batch info by the flowBatchID.
        com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = baseCoreFactory.getBO(com.fa.cim.newcore.bo.dispatch.CimFlowBatch.class, batchID);
        Validations.check(aFlowBatch == null, new OmCode(retCodeConfig.getNotFoundFlowBatch(), batchID.getValue()));
        // get the eqp info by the equipmentID.
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
        // set the eqp info for the flowBatch.
        aFlowBatch.setMachine(aMachine);
        // add flow batch info to EQP dispatch list.
     //   aMachine.addFlowBatch(aFlowBatch);
        // get the person info by the userID.
        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        // set the claim time for the flowBatch.
        aFlowBatch.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        // set the person info for the flowBatch.
        aFlowBatch.setLastClaimedPerson(aPerson);
        // set the claim time for the eqp.
        aMachine.setLatestOperatedTimestamp(objCommon.getTimeStamp().getReportTimeStamp());
        // set the person info for the eqp.
        aMachine.setLatestOperatedUser(aPerson);
        // Move from flowBatch_Make DCR743.
        List<com.fa.cim.newcore.bo.product.CimLot> aLotList = aFlowBatch.allLots();
        int nLen = CimArrayUtils.getSize(aLotList);
        for (int i = 0; i < nLen; i++) {
            dispatchingManager.addToQueue(aLotList.get(i));
        }
        log.debug("setFlowBatchReserveEquipmentID():exit the setFlowBatchReserveEquipmentID method...");
    }

    /**
     * description:
     * <p>Set return value of OFLWW003.
     * To set return values, get PosFlowBatch object.</p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   objCommon
     * @param equipmentID equipmentID
     * @return RetCode<List   <   Infos.FlowBatchedLot>>
     * @author ZQI
     * @date 2018/12/22 16:16:58
     */
    @Override
    public List<Infos.FlowBatchedLot> flowBatchFillInTxDSC003(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier batchID) {
        log.info("flowBatchFillInTxDSC003(): come in the flowBatchFillInTxDSC003 method...");
        List<Infos.FlowBatchedLot> resultList = new ArrayList<>();
        com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = baseCoreFactory.getBO(com.fa.cim.newcore.bo.dispatch.CimFlowBatch.class, batchID);
        Validations.check(aFlowBatch == null, new OmCode(retCodeConfig.getNotFoundFlowBatch(), batchID.getValue()));
        List<com.fa.cim.newcore.bo.product.CimLot> aLotList = aFlowBatch.allLots();
        Long count = 0L;
        if (!CimArrayUtils.isEmpty(aLotList)) {
            for (com.fa.cim.newcore.bo.product.CimLot aLot : aLotList) {
                count++;
                Infos.FlowBatchedLot flowBatchedLot = new Infos.FlowBatchedLot();
                flowBatchedLot.setLotID(new ObjectIdentifier(aLot.getIdentifier(), aLot.getPrimaryKey()));
                flowBatchedLot.setProcessSequenceNumber(count);
                List<MaterialContainer> aMaterialContainerList = aLot.materialContainers();
                if (!CimArrayUtils.isEmpty(aMaterialContainerList)) {
                    flowBatchedLot.setCassetteID(new ObjectIdentifier(aMaterialContainerList.get(0).getIdentifier(),aMaterialContainerList.get(0).getPrimaryKey()));
                }
                resultList.add(flowBatchedLot);
            }
        }
        log.info("flowBatchFillInTxDSC003(): exit the flowBatchFillInTxDSC003 method...");
        return resultList;
    }


    @Override
    public void flowBatchLotRemove(Infos.ObjCommon objCommon, ObjectIdentifier flowBatchID, List<Infos.RemoveLot> strRemoveLot) {
        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = baseCoreFactory.getBO(com.fa.cim.newcore.bo.dispatch.CimFlowBatch.class, flowBatchID);
        Validations.check(aFlowBatch == null, new OmCode(retCodeConfig.getNotFoundFlowBatch(), flowBatchID.getValue()));
        int nLen = CimArrayUtils.getSize(strRemoveLot);
        Validations.check(nLen == 0, retCodeConfig.getInvalidParameter());
        com.fa.cim.newcore.bo.product.CimLot aLot = null;
        for (Infos.RemoveLot removeLot : strRemoveLot){
            aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, removeLot.getLotID());
            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), removeLot.getLotID().getValue()));
            dispatchingManager.removeFromQueue(aLot);
            aFlowBatch.removeLot(aLot);
            aLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aLot.setLastClaimedPerson(aPerson);
            dispatchingManager.addToQueue(aLot);
        }
        List<com.fa.cim.newcore.bo.product.CimLot> aLotList = aFlowBatch.allLots();
        nLen = CimArrayUtils.getSize(aLotList);
        CimMachine aMachine = null;
        if (nLen == 0){
            aMachine = aFlowBatch.getMachine();
            if (aMachine != null){
                // equipment's flow batch clear
                aMachine.removeFlowBatch(aFlowBatch);
                aMachine.setLatestOperatedTimestamp(objCommon.getTimeStamp().getReportTimeStamp());
                aMachine.setLatestOperatedUser(aPerson);
            }
            // Delete flow batch
            dispatchingManager.removeFlowBatch(aFlowBatch);
            throw new ServiceException(retCodeConfig.getFlowBatchRemoved());
        } else {  // Some lots remain in flow batch
            aFlowBatch.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aFlowBatch.setLastClaimedPerson(aPerson);
            aLotList = aFlowBatch.allLots();
            int lenLotInFB = CimArrayUtils.getSize(aLotList);
            List<String> buffCassettes = new ArrayList<>();
            int buff_cassetteLen = 0;
            for (int j = 0; j < lenLotInFB; j++){
                List<MaterialContainer> aMaterialContainers = aLotList.get(j).materialContainers();
                int lenMaterial = CimArrayUtils.getSize(aMaterialContainers);
                com.fa.cim.newcore.bo.durable.CimCassette aCassette = null;
                if (lenMaterial > 0){
                    aCassette = (com.fa.cim.newcore.bo.durable.CimCassette)aMaterialContainers.get(0);
                }
                Validations.check(aCassette == null, retCodeConfig.getNotFoundCst());
                ObjectIdentifier lotInCassetteID = new ObjectIdentifier(aCassette.getIdentifier(), aCassette.getPrimaryKey());
                boolean duplicateFlag = false;
                for (int k = 0; k < buff_cassetteLen; k++){
                    if (ObjectIdentifier.equalsWithValue(lotInCassetteID, buffCassettes.get(k))){
                        duplicateFlag = true;
                        break;
                    }
                }
                if (!duplicateFlag){
                    buff_cassetteLen++;
                    buffCassettes.add(lotInCassetteID.getValue());
                }
            }
            aFlowBatch.setFlowBatchSize(buff_cassetteLen);
        }
    }

    @Override
    public Results.FlowBatchLotRemoveReqResult flowBatchFillInTxDSC005(Infos.ObjCommon objCommon, ObjectIdentifier flowBatchID) {
        Results.FlowBatchLotRemoveReqResult flowBatchLotRemoveReqResult = new Results.FlowBatchLotRemoveReqResult();
        com.fa.cim.newcore.bo.dispatch.CimFlowBatch aFlowBatch = baseCoreFactory.getBO(com.fa.cim.newcore.bo.dispatch.CimFlowBatch.class, flowBatchID);
        Validations.check(aFlowBatch == null, new OmCode(retCodeConfig.getNotFoundFlowBatch(), flowBatchID.getValue()));
        int nOperationCount = aFlowBatch.getOperationCount();
        int nFlowBatchSize = aFlowBatch.getFlowBatchSize();
        CimMachine aMachine = aFlowBatch.getMachine();
        flowBatchLotRemoveReqResult.setFlowBatchID(flowBatchID);
        if (aMachine != null){
            flowBatchLotRemoveReqResult.setReserveEquipmentID(new ObjectIdentifier(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
        }
        List<Infos.FlowBatchedCassetteInfo> strFlowBatchedCassetteInfoList = new ArrayList<>();
        List<com.fa.cim.newcore.bo.product.CimLot> aLotList = aFlowBatch.allLots();
        List<MaterialContainer> aMaterialContainerList = null;
        com.fa.cim.newcore.bo.durable.CimCassette aCassette = null;
        int lenLot = CimArrayUtils.getSize(aLotList);
        for (int i = 0; i < lenLot; i++){
            ObjectIdentifier lotID = new ObjectIdentifier(aLotList.get(i).getIdentifier(), aLotList.get(i).getPrimaryKey());
            aMaterialContainerList = aLotList.get(i).materialContainers();
            int lenLotInCassette = CimArrayUtils.getSize(aMaterialContainerList);
            /*--------------------*/
            /*   Set CassetteID   */
            /*--------------------*/
            ObjectIdentifier cassetteID = null;
            if (lenLotInCassette > 0){
                aCassette = (com.fa.cim.newcore.bo.durable.CimCassette) aMaterialContainerList.get(0);
                cassetteID = new ObjectIdentifier(aCassette.getIdentifier(), aCassette.getPrimaryKey());
            } else {
                log.info("{},!!!!! Lot doesn't have Cassette.", lotID.getValue());
            }
            int nCasIdx = -1;
            int lenFBCassette = CimArrayUtils.getSize(strFlowBatchedCassetteInfoList);
            for (int j = 0; j < lenFBCassette; j++){
                if (ObjectIdentifier.equalsWithValue(strFlowBatchedCassetteInfoList.get(j).getCassetteID(), cassetteID)){
                    nCasIdx = j;   // Found same cassetteID sequence
                    break;
                }
            }
            Infos.FlowBatchedCassetteInfo flowBatchedCassetteInfo = new Infos.FlowBatchedCassetteInfo();
            if (nCasIdx < 0){
                // CassetteID not found
                nCasIdx = lenFBCassette;
                flowBatchedCassetteInfo.setCassetteID(cassetteID);
            }
            /*---------------*/
            /*   Set LotID   */
            /*---------------*/
            int nLotIdx = -1;
            List<Infos.FlowBatchedLotInfo> strFlowBatchedLotInfo = flowBatchedCassetteInfo.getStrFlowBatchedLotInfo();
            int lenFBLot = CimArrayUtils.getSize(strFlowBatchedLotInfo);
            for (int j = 0; j < lenFBLot; j++){
                if (ObjectIdentifier.equalsWithValue(strFlowBatchedLotInfo.get(j).getLotID(), lotID)){
                    nLotIdx = j;
                    break;
                }
            }
            if (nLotIdx < 0){
                // LotID not found
                nLotIdx = lenFBLot;
                Infos.FlowBatchedLotInfo flowBatchedLotInfo = new Infos.FlowBatchedLotInfo();
                flowBatchedLotInfo.setLotID(lotID);
                List<Infos.FlowBatchedLotInfo> tmpFlowBatchedLotInfoList = flowBatchedCassetteInfo.getStrFlowBatchedLotInfo();
                if (CimArrayUtils.isEmpty(tmpFlowBatchedLotInfoList)){
                    tmpFlowBatchedLotInfoList = new ArrayList<>();
                    flowBatchedCassetteInfo.setStrFlowBatchedLotInfo(tmpFlowBatchedLotInfoList);
                }
                tmpFlowBatchedLotInfoList.add(flowBatchedLotInfo);
            }
            if (flowBatchedCassetteInfo != null){
                strFlowBatchedCassetteInfoList.add(flowBatchedCassetteInfo);
            }
        }
        /*-----------------------*/
        /*   Set Output Struct   */
        /*-----------------------*/
        flowBatchLotRemoveReqResult.setStrFlowBatchedCassetteInfo(strFlowBatchedCassetteInfoList);
        return flowBatchLotRemoveReqResult;
    }

    @Override
    public ObjectIdentifier flowBatchSelectForEquipmentDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, Results.FloatingBatchListInqResult floatingBatchListInqResult) {
        List<Infos.FloatBatch> floatBatches = floatingBatchListInqResult.getFloatBatches();
        Validations.check(CimArrayUtils.isEmpty(floatBatches), retCodeConfig.getNotFoundFlowBatch());

        List<ObjectIdentifier> candidateFloatingIDSeq = new ArrayList<>();
        List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchingInfo = new ArrayList<>();
        int nCandidateFloatingIdx = 0;

        int lenFloatingBatch = CimArrayUtils.getSize(floatBatches);
        for (int i = 0; i < lenFloatingBatch; i++) {
            /*--------------------------------------------------------*/
            /*   Add pptFloatingBatchListInqResult to FloatingBatchInfo   */
            /*--------------------------------------------------------*/
            Infos.FloatBatch floatBatch = floatBatches.get(i);
            Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = new Infos.FlowBatchByManualActionReqCassette();

            flowBatchByManualActionReqCassette.setCassetteID(floatBatch.getCassetteID());
            List<Infos.FlowBatchedLotInfo> flowBatchedLotInfos = floatBatch.getFlowBatchedLotInfos();
            if(CimArrayUtils.isNotEmpty(flowBatchedLotInfos)){
                List<ObjectIdentifier> lotID = new ArrayList<>();
                for (Infos.FlowBatchedLotInfo flowBatchedLotInfo : flowBatchedLotInfos) {
                    lotID.add(flowBatchedLotInfo.getLotID());
                }
                flowBatchByManualActionReqCassette.setLotID(lotID);
            }
            strFlowBatchingInfo.add(flowBatchByManualActionReqCassette);

            /*-------------------------------------------------------------------------*/
            /*   If next FlowBatchID is changed or last of loop then check condition   */
            /*-------------------------------------------------------------------------*/
            if ((i < lenFloatingBatch - 1 &&
                    !ObjectIdentifier.equalsWithValue(floatBatch.getFlowBatchID(), floatBatches.get(i + 1).getFlowBatchID())) ||
                    i == lenFloatingBatch - 1) {
                boolean bCondition = false;
                //step1 - cassette_CheckCountForFlowBatch
                try{
                    cassetteMethod.cassetteCheckCountForFlowBatch(objCommon, equipmentID, strFlowBatchingInfo, BizConstant.SP_OPERATION_FLOWBATCHING, "");
                    bCondition = true;
                }catch (ServiceException e) {
                    // DO NOTHING
                }
                if (CimBooleanUtils.isTrue(bCondition)) {
                    candidateFloatingIDSeq.add(floatBatch.getFlowBatchID());
                    nCandidateFloatingIdx++;
                }
            }
        }

        Validations.check(nCandidateFloatingIdx == 0, retCodeConfig.getNotFoundFlowBatch());

        ObjectIdentifier targetFlowBatchID = candidateFloatingIDSeq.get(0);
        long nExtremePriorityQtime = 0;
        Boolean bFirstSetFlowBatch = false;
        CimLot lot = null;
        for (int i = 0; i < lenFloatingBatch; i++) {
            Infos.FloatBatch floatBatch = floatBatches.get(i);
            Boolean bNotCheck = true;

            if(CimArrayUtils.isNotEmpty(candidateFloatingIDSeq)){
                for (ObjectIdentifier objectIdentifier : candidateFloatingIDSeq) {
                    if(ObjectIdentifier.equalsWithValue(floatBatch.getFlowBatchID(), objectIdentifier)){
                        bNotCheck = false;
                        break;
                    }
                }
            }
            if(CimBooleanUtils.isTrue(bNotCheck)){
                continue;
            }
            Boolean bLotHold = false;
            Boolean bExistQtime = false;
            Boolean bInhibit = false;
            long nExtremePriorityQtimeInOneBatch = 0;
            List<Infos.FlowBatchedLotInfo> flowBatchedLotInfos = floatBatch.getFlowBatchedLotInfos();
            int lenFlowBatchedLot = CimArrayUtils.getSize(flowBatchedLotInfos);
            for (int j = 0; j < lenFlowBatchedLot; j++) {
                Infos.FlowBatchedLotInfo flowBatchedLotInfo = flowBatchedLotInfos.get(j);

                List<Infos.EntityInhibitAttributes> entityInhibitions = flowBatchedLotInfo.getEntityInhibitions();
                if(CimArrayUtils.isNotEmpty(entityInhibitions)){
                    bInhibit = true;
                    break;
                }

                lot = baseCoreFactory.getBO(CimLot.class, flowBatchedLotInfo.getLotID());
                ObjectIdentifier lotIDObj = ObjectIdentifier.build(lot.getIdentifier(), lot.getPrimaryKey());

                Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
                lotInfoInqFlag.setLotBackupInfoFlag(false);
                lotInfoInqFlag.setLotBasicInfoFlag(true);
                lotInfoInqFlag.setLotControlUseInfoFlag(false);
                lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
                lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
                lotInfoInqFlag.setLotOperationInfoFlag(false);
                lotInfoInqFlag.setLotOrderInfoFlag(false);
                lotInfoInqFlag.setLotControlJobInfoFlag(false);
                lotInfoInqFlag.setLotProductInfoFlag(false);
                lotInfoInqFlag.setLotRecipeInfoFlag(false);
                lotInfoInqFlag.setLotLocationInfoFlag(false);
                lotInfoInqFlag.setLotWipOperationInfoFlag(false);
                lotInfoInqFlag.setLotWaferAttributesFlag(false);

                //step2 - lot_detailInfo_GetDR__160
                Infos.LotInfo lotDetailInfo = lotMethod.lotDetailInfoGetDR(objCommon, lotInfoInqFlag, lotIDObj);

                if(CimStringUtils.equals(BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD, lotDetailInfo.getLotBasicInfo().getLotStatus())){
                    bLotHold = true;
                    break;
                }
                if(CimBooleanUtils.isFalse(lotDetailInfo.getLotBasicInfo().getQtimeFlag())){
                    continue;
                }

                //step3 - lot_qTime_GetDR
                Outputs.ObjLotQTimeGetDROut objLotQTimeGetDROut = lotMethod.lotQTimeGetDR(objCommon, flowBatchedLotInfo.getLotID());
                List<Infos.LotQtimeInfo> StrLotQTimeInfo = objLotQTimeGetDROut.getStrLotQtimeInfoList();

                int lenLotQtime = CimArrayUtils.getSize(StrLotQTimeInfo);
                for (int k = 0; k < lenLotQtime; k++) {
                    Infos.LotQtimeInfo qTimeInformation = StrLotQTimeInfo.get(k);

                    if(CimBooleanUtils.isFalse(CimStringUtils.equals("Y",qTimeInformation.getActionDoneFlag())) &&
                            CimBooleanUtils.isTrue(CimStringUtils.equals("Y",qTimeInformation.getWatchDogRequired())) &&
                            !CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, qTimeInformation.getQrestrictionTargetTimeStamp())){
                        long nQtime = Long.valueOf(qTimeInformation.getQrestrictionRemainTime());
                        if(CimBooleanUtils.isFalse(bExistQtime)){
                            nExtremePriorityQtimeInOneBatch = nQtime;
                        } else if (nExtremePriorityQtimeInOneBatch > nQtime) {
                            nExtremePriorityQtimeInOneBatch = nQtime;
                        }
                        bExistQtime = true;
                    }

                }
            }
            if(CimBooleanUtils.isFalse(bInhibit) && CimBooleanUtils.isFalse(bLotHold) && CimBooleanUtils.isTrue(bExistQtime)){
                if(CimBooleanUtils.isFalse(bFirstSetFlowBatch) || nExtremePriorityQtime > nExtremePriorityQtimeInOneBatch){
                    targetFlowBatchID = floatBatch.getFlowBatchID();
                    nExtremePriorityQtime = nExtremePriorityQtimeInOneBatch;
                    bFirstSetFlowBatch = true;
                }
            }

        }


        return targetFlowBatchID;
    }

    @Override
    public List<Infos.FlowBatchByManualActionReqCassette> tempFlowBatchSelectForEquipmentDR(Infos.ObjCommon objCommon, Inputs.ObjTempFlowBatchSelectForEquipmentDRIn tempFlowBatch) {

        List<Infos.FlowBatchByManualActionReqCassette> result = new ArrayList<>();

        ObjectIdentifier equipmentID = tempFlowBatch.getEquipmentID();
        Results.FlowBatchLotSelectionInqResult strFlowBatchLotSelectionInqResult = tempFlowBatch.getStrFlowBatchLotSelectionInqResult();

        //step1 - equipment_processBatchCondition_Get
        Outputs.ObjEquipmentProcessBatchConditionGetOut equipmentProcessBatchCondition = equipmentMethod.equipmentProcessBatchConditionGet(objCommon, equipmentID);
        List<Infos.TempFlowBatch> strTempFlowBatch = strFlowBatchLotSelectionInqResult.getStrTempFlowBatch();

        List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette = new ArrayList<>();
        long nBatchingSize = 0;
        Boolean bCheckReserveFlowBatchSet = false;
        Boolean bFlowBatchingCondition = false;
        ObjectIdentifier nowFlowBatchTempID = new ObjectIdentifier();
        ObjectIdentifier cassetteID = null;
        if(CimArrayUtils.isNotEmpty(strTempFlowBatch)){
            for (Infos.TempFlowBatch flowBatch : strTempFlowBatch) {
                if(CimArrayUtils.isEmpty(flowBatch.getStrTempFlowBatchLot())){
                    continue;
                }
                if(!ObjectIdentifier.equalsWithValue(flowBatch.getTemporaryFlowBatchID(), nowFlowBatchTempID)){
                    bCheckReserveFlowBatchSet = false;
                    strFlowBatchByManualActionReqCassette.clear();
                    nBatchingSize = 0;
                }
                nowFlowBatchTempID = flowBatch.getTemporaryFlowBatchID();
                cassetteID = flowBatch.getCassetteID();
                long nFlowBatchAreaMaxSize = flowBatch.getStrTempFlowBatchLot().get(0).getFlowBatchLotSize();
                if(0 >= nFlowBatchAreaMaxSize){
                    continue;
                }
                if(nFlowBatchAreaMaxSize >= equipmentProcessBatchCondition.getMaxBatchSize()){
                    nFlowBatchAreaMaxSize = equipmentProcessBatchCondition.getMaxBatchSize();
                }
                Boolean bCassetteAlreadyExist = false;
                if(CimArrayUtils.isNotEmpty(strFlowBatchByManualActionReqCassette)){
                    for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassette) {
                        if(ObjectIdentifier.equalsWithValue(flowBatchByManualActionReqCassette.getCassetteID(), cassetteID)){
                            bCassetteAlreadyExist = true;
                            break;
                        }
                    }
                }
                if(CimBooleanUtils.isTrue(bCassetteAlreadyExist)){
                    continue;
                }

                //step2 - cassette_GetLotList
                Infos.LotListInCassetteInfo objCassetteLotListGetDROut = null;
                try {
                    objCassetteLotListGetDROut = cassetteMethod.cassetteGetLotList(objCommon, cassetteID);
                }catch (ServiceException e) {
                    continue;
                }
                List<ObjectIdentifier> lotIDList = objCassetteLotListGetDROut.getLotIDList();
                Boolean bFound = false;
                if(CimArrayUtils.isNotEmpty(lotIDList)){
                    for (ObjectIdentifier lotId : lotIDList) {
                        bFound = false;
                        for (Infos.TempFlowBatch tempFlowBatchs : strTempFlowBatch) {
                            if(CimStringUtils.equals(tempFlowBatchs.getTemporaryFlowBatchID().getValue(), nowFlowBatchTempID.getValue())){
                                List<Infos.TempFlowBatchLot> strTempFlowBatchLot = tempFlowBatchs.getStrTempFlowBatchLot();
                                if(CimArrayUtils.isNotEmpty(strTempFlowBatchLot)){
                                    for (Infos.TempFlowBatchLot tempFlowBatchLot : strTempFlowBatchLot) {
                                        if(ObjectIdentifier.equalsWithValue(tempFlowBatchLot.getLotID(), lotId)){
                                            bFound = true;
                                            break;
                                        }
                                    }
                                }
                                if(CimBooleanUtils.isTrue(bFound)){
                                    break;
                                }
                            }
                        }
                        if(CimBooleanUtils.isFalse(bFound)){
                            break;
                        }
                    }
                }
                if(CimBooleanUtils.isFalse(bFound)){
                    continue;
                }
                List<Infos.FlowBatchByManualActionReqCassette> strCassetteCheckSeq = new ArrayList<>();
                Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = new Infos.FlowBatchByManualActionReqCassette();
                flowBatchByManualActionReqCassette.setCassetteID(cassetteID);
                List<ObjectIdentifier> lotID = new ArrayList<>(lotIDList.size());
                flowBatchByManualActionReqCassette.setLotID(lotID);
                strCassetteCheckSeq.add(flowBatchByManualActionReqCassette);

                lotID.addAll(lotIDList);
                ObjectIdentifier flowBatchID = new ObjectIdentifier();

                //step3 - cassette_CheckConditionForFlowBatch
                try {
                    cassetteMethod.cassetteCheckConditionForFlowBatch(objCommon, equipmentID, flowBatchID, strCassetteCheckSeq, BizConstant.SP_OPERATION_FLOWBATCHINGAUTO);
                } catch (ServiceException e) {
                    continue;
                }
                strFlowBatchByManualActionReqCassette.add(strCassetteCheckSeq.get(0));
                nBatchingSize++;
                if(nFlowBatchAreaMaxSize == nBatchingSize){
                    bCheckReserveFlowBatchSet = true;
                }
                if(CimBooleanUtils.isTrue(bCheckReserveFlowBatchSet)){
                    for (int n = 0; n < 1; n++) {
                        flowBatchID = new ObjectIdentifier();

                        //step4 - cassette_CheckConditionForFlowBatch
                        try{
                            cassetteMethod.cassetteCheckConditionForFlowBatch(objCommon, equipmentID, flowBatchID, strFlowBatchByManualActionReqCassette, BizConstant.SP_OPERATION_FLOWBATCHINGAUTO);
                        }catch (ServiceException e) {
                            break;
                        }
                        //step5 - cassette_CheckCountForFlowBatch
                        try{
                            cassetteMethod.cassetteCheckCountForFlowBatch(objCommon, equipmentID, strFlowBatchByManualActionReqCassette, BizConstant.SP_OPERATION_FLOWBATCHINGAUTO, "");
                        }catch (ServiceException e) {
                            break;
                        }
                        bFlowBatchingCondition = true;
                    }
                    if(CimBooleanUtils.isTrue(bFlowBatchingCondition)){
                        break;
                    }
                    bCheckReserveFlowBatchSet = false;
                    strFlowBatchByManualActionReqCassette.clear();
                    nBatchingSize = 0;
                }
            }
        }
        if(CimBooleanUtils.isTrue(bFlowBatchingCondition)){
            result = strFlowBatchByManualActionReqCassette;
        } else {
            throw new ServiceException(retCodeConfig.getNotEnoughLotForFlowBatch());
        }

        return result;
    }

    @Override
    public ObjectIdentifier lotFlowBatchIDGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {

        com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
        Validations.check(CimObjectUtils.isEmpty(lot),retCodeConfig.getNotFoundLot());
        com.fa.cim.newcore.bo.dispatch.CimFlowBatch flowBatch = lot.getFlowBatch();
        Validations.check(CimObjectUtils.isEmpty(flowBatch),retCodeConfig.getLotFlowBatchIdBlank());

        //result.setObject(new ObjectIdentifier(flowBatchDO.getFlowBatchID(), flowBatchDO.getId()));
        throw new ServiceException(retCodeConfig.getLotFlowBatchIdFilled());
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/9 14:32
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Outputs.ObjFlowBatchCheckConditionForCassetteDeliveryOut flowBatchCheckConditionForCassetteDelivery(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, Results.WhatNextLotListResult strWhatNextInqResult) {
        //init
        Outputs.ObjFlowBatchCheckConditionForCassetteDeliveryOut out = new Outputs.ObjFlowBatchCheckConditionForCassetteDeliveryOut();


        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class,equipmentID);
        ObjectIdentifier reservedFlowBatchID = new ObjectIdentifier();
        List<Infos.ContainedLotsInFlowBatch> strFlowBatchLotGetOut = new ArrayList<>();
        List<CimFlowBatch> reserveFlowBatches = aMachine.allFlowBatches();
        int reservedFBLen = CimArrayUtils.getSize(reserveFlowBatches);
        if (0 < reservedFBLen){
            log.info("FlowBatch is Exist!!");
            //---------------------------------------------------------------------------//
            //  Get flowBatchID from strWhatNextInqResult.strWhatNextAttributes.  //
            //  Put them into whatNextFlowBatchIDs by the priority of WhatsNext.         //
            //---------------------------------------------------------------------------//
            ObjectIdentifier tempFlowBatchID = new ObjectIdentifier();
            List<ObjectIdentifier> whatNextFlowBatchIDs = new ArrayList<>();
            int whatNextFlowBatchLen = 0;
            int whatNextLotLen = CimArrayUtils.getSize(strWhatNextInqResult.getStrWhatNextAttributes());
            for (int m = 0; m < whatNextLotLen; m++) {
                tempFlowBatchID = strWhatNextInqResult.getStrWhatNextAttributes().get(m).getFlowBatchID();
                if (!ObjectIdentifier.isEmpty(tempFlowBatchID)){
                    log.info("FlowBatched lot is found: lotID -> {}",strWhatNextInqResult.getStrWhatNextAttributes().get(m).getLotID().getValue());
                    if (CimArrayUtils.getSize(whatNextFlowBatchIDs) == 0){
                        log.info("New FloBatch Found: flowBatchID -> {}",tempFlowBatchID.getValue());
                        whatNextFlowBatchIDs.add(tempFlowBatchID);
                        whatNextFlowBatchLen ++;
                    }
                    for (int n = 0; n < CimArrayUtils.getSize(whatNextFlowBatchIDs); n++) {
                        if (!whatNextFlowBatchIDs.contains(tempFlowBatchID)){
                            log.info("New FloBatch Found: flowBatchID -> {}",tempFlowBatchID.getValue());
                            whatNextFlowBatchIDs.add(tempFlowBatchID);
                            whatNextFlowBatchLen ++;
                            break;
                        }
                    }
                }
            }
            //Check strWhatNextAttributes contain FlowBatched lots or Not.
            if (whatNextFlowBatchLen == 0){
                log.info("There is no flowBatched lot in whatNextLotList.");
                throw new ServiceException(retCodeConfig.getNotFoundFlowbatchCandLot());
            }
            // Check strWhatNextAttributes contain all the lots of a reserved flowBatch.
            Boolean readyFlowBatchFoundFlag = false;
            whatNextFlowBatchLen = CimArrayUtils.getSize(whatNextFlowBatchIDs);
            for (int mm = 0; mm < whatNextFlowBatchLen; mm++) {
                for (int nn = 0; nn < reservedFBLen; nn++) {
                    if (!CimObjectUtils.isEmpty(reserveFlowBatches.get(nn))){
                        reservedFlowBatchID  = ObjectIdentifier.build(reserveFlowBatches.get(nn).getIdentifier(), reserveFlowBatches.get(nn).getPrimaryKey());
                    }else {
                        reservedFlowBatchID = ObjectIdentifier.build("", "");
                    }
                    if (!CimStringUtils.equals(whatNextFlowBatchIDs.get(mm).getValue(), reservedFlowBatchID.getValue())){
                        log.info("whatNextFlowBatchIDs != reservedFlowBatchID. Go to the next reservedFlowBatchID.");
                        continue;
                    }else {
                        log.info("whatNextFlowBatchIDs == reservedFlowBatchID");
                        readyFlowBatchFoundFlag = true;
                        //Get FlowBatching Lots
                        //【step1】 - flowBatch_lot_Get
                        strFlowBatchLotGetOut = flowBatchMethod.flowBatchLotGet(objCommonIn, reservedFlowBatchID);

                        //Check FlowBatching All Lots Exist in WIPLots
                        log.info("----- FlowBatching Lots --------------------");
                        int lenFolwBatchLots = CimArrayUtils.getSize(strFlowBatchLotGetOut);
                        for (int i = 0; i < lenFolwBatchLots; i++) {
                            Boolean bFound = false;
                            int lenWipLots = CimArrayUtils.getSize(strWhatNextInqResult.getStrWhatNextAttributes());
                            for (int j = 0; j < lenWipLots; j++) {
                                if (CimStringUtils.equals(strFlowBatchLotGetOut.get(i).getLotID().getValue(),
                                        strWhatNextInqResult.getStrWhatNextAttributes().get(j).getLotID().getValue())){
                                    bFound = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isFalse(bFound)){
                                readyFlowBatchFoundFlag = false;
                                break;
                            }
                        }
                        break;
                    }
                }
                if (CimBooleanUtils.isTrue(readyFlowBatchFoundFlag)){
                    log.info("This FlowBatch is ready. flowBatchID -> {}",whatNextFlowBatchIDs.get(mm).getValue());
                    break;
                }else {
                    log.info("This FlowBatch is NOT ready. Check the next whatNextFlowBatch. flowBatchID -> {}",whatNextFlowBatchIDs.get(mm).getValue());
                    continue;
                }
            }
            if (CimBooleanUtils.isFalse(readyFlowBatchFoundFlag)){
                //Not need to Set strFlowBatch_CheckConditionForCassetteDelivery_out
                throw new ServiceException(retCodeConfig.getNotFoundFlowbatchCandLot());

            }
        }
        //Set output structure member
        out.setFlowBatchID(reservedFlowBatchID);
        out.setStrContainedLotsInFlowBatch(strFlowBatchLotGetOut);
        return out;
    }
}
