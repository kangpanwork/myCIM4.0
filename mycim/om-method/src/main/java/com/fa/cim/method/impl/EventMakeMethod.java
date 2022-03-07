package com.fa.cim.method.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.dto.lot.TerminateReq;
import com.fa.cim.entitysuper.EventBaseDO;
import com.fa.cim.entitysuper.MachineDO;
import com.fa.cim.event.eo.BaseEvent;
import com.fa.cim.layoutrecipe.LayoutRecipeParams;
import com.fa.cim.lmg.LotMonitorGroupParams;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.code.*;
import com.fa.cim.newcore.bo.dispatch.CimFlowBatch;
import com.fa.cim.newcore.bo.durable.*;
import com.fa.cim.newcore.bo.event.*;
import com.fa.cim.newcore.bo.factory.CimBank;
import com.fa.cim.newcore.bo.factory.CimStage;
import com.fa.cim.newcore.bo.machine.CimEqpMonitor;
import com.fa.cim.newcore.bo.machine.CimEqpMonitorJob;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimProcessResource;
import com.fa.cim.newcore.bo.pd.*;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.planning.CimProductRequest;
import com.fa.cim.newcore.bo.planning.PlanManager;
import com.fa.cim.newcore.bo.prodspec.CimCustomer;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.*;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.event.Event;
import com.fa.cim.newcore.dto.event.SortEvent;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.dto.restriction.Constrain;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.Durable;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.newcore.standard.mtrlmngm.Material;
import com.fa.cim.newcore.standard.prcssdfn.ProcessDefinition;
import com.fa.cim.newcore.standard.prcssdfn.ProcessOperation;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import com.fa.cim.newcore.standard.prdctspc.ProductSpecification;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.SorterType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.fa.cim.common.constant.BizConstant.*;
import static com.fa.cim.common.constant.TransactionIDEnum.POST_PROCESS_ACTION_REGIST_REQ;
import static com.fa.cim.common.constant.TransactionIDEnum.POST_PROCESS_EXEC_REQ;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/5/29        ********             lightyh            create file
 *
 * @author: lightyh
 * @date: 2019/5/29 11:31
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class EventMakeMethod implements IEventMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ISorterNewMethod sorterNewMethod;

    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private PlanManager newPlanManager;

    @Autowired
    private ProductManager newProductManager;

    @Autowired
    private EventManager eventManager;

    @Autowired
    @Qualifier("CodeManagerCore")
    private CodeManager newCodeManager;

    @Autowired
    private ExtendableEventManager extendableEventManager;

    @Override
    public void lotReworkEventMake(Infos.ObjCommon objCommon, Inputs.LotReworkEventMakeParams lotReworkEventMakeParams) {
        CimLot cimLot = baseCoreFactory.getBO(CimLot.class, lotReworkEventMakeParams.getLotID());
        Validations.check(cimLot == null, retCodeConfig.getNotFoundLot());

        Event.LotEventData eventData = cimLot.getEventData();

        Event.LotReworkEventRecord anEventRecord = new Event.LotReworkEventRecord();
        Event.LotEventData lotEventData = new Event.LotEventData();
        BeanUtils.copyProperties(eventData, lotEventData);
        anEventRecord.setLotData(lotEventData);
        anEventRecord.setReworkCount(0L);

        CimProcessFlowContext aPosProcessFlowContext = cimLot.getProcessFlowContext();
        Validations.check(aPosProcessFlowContext == null, retCodeConfig.getNotFoundPfx());

        CimProcessOperation aPrevProcessOperation = aPosProcessFlowContext.getPreviousProcessOperation();
        anEventRecord.setPreviousOperationPassCount(0L);
        if (aPrevProcessOperation != null) {
            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevProcessOperation.getEventData();
            if (prevPOEventData != null) {
                anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
                anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
                anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
                anEventRecord.setPreviousOperationPassCount((long) prevPOEventData.getOperationPassCount());
                anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
                anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
                anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
            }

        }
        //----------------------------------------------
        // Rework Count Default Setteing
        // Case of Rework Cancel , Rework Count is Zero
        //----------------------------------------------
        anEventRecord.setReworkCount(0L);
        String key = null;
        //------------------------------------------------------------
        // Rework & Rework Cancel Case (Normal Case):
        //     Getting rework count from rework out operation.
        //------------------------------------------------------------
        Inputs.OldCurrentPOData oldCurrentPOData = lotReworkEventMakeParams.getOldCurrentPOData();
        if (!CimObjectUtils.isEmpty(oldCurrentPOData.getReworkOutOperation())) {
            key = oldCurrentPOData.getReworkOutOperation();
        } else {
            //------------------------------------------------------------
            // Rework & Rework Cancel Case (Exceptional Case):
            //     Getting rework count from original key.
            //------------------------------------------------------------
            AtomicReference<CimProcessFlow> aMainProcessFlow = new AtomicReference<>();
            AtomicReference<CimProcessFlow> aModuleProcessFlow = new AtomicReference<>();
            AtomicReference<String> moduleNumber = new AtomicReference<>();
            AtomicReference<CimProcessFlow> outMainProcessFlow = new AtomicReference<>();
            AtomicReference<CimProcessFlow> outModuleProcessFlow = new AtomicReference<>();
            AtomicReference<String> outModuleNumber = new AtomicReference<>();
            CimProcessOperationSpecification aBackupPOS = aPosProcessFlowContext.getPreviousProcessOperationSpecification(aMainProcessFlow, moduleNumber, aModuleProcessFlow);
            Integer inseqno = 0;
            AtomicReference<Integer> outseqno = new AtomicReference<>(0);
            Validations.check(aBackupPOS == null, retCodeConfig.getNotFoundPos());

            //-------------------------------------------------------------
            // Rework Cancel Case : Getting Rework Count from before 1 POS.
            // Because Lot was moved to Back Operation already.
            //------------------------------------------------------------
            String strTergetRouteID;
            String strOpeNumber;
            if (CimStringUtils.equals(lotReworkEventMakeParams.getTransactionID(), TransactionIDEnum.REWORK_WHOLE_LOT_CANCEL_REQ.getValue())) {
                strOpeNumber = aBackupPOS.getOperationNumber();
                outModuleNumber = moduleNumber;
                Validations.check(aMainProcessFlow.get() == null, new OmCode(retCodeConfig.getNotFoundProcessFlow(), lotReworkEventMakeParams.getLotID().getValue()));

                ProcessDefinition aMainPD = aMainProcessFlow.get().getRootProcessDefinition();
                Validations.check(aMainPD == null, retCodeConfig.getNotFoundMainRoute());

                strTergetRouteID = aMainPD.getIdentifier();
            }
            //-------------------------------------------------------
            // Rework case (Not Rework Cancel):
            //               Getting rework count from before 2 POSs.
            //-------------------------------------------------------
            else {
                CimProcessOperationSpecification aPrevPOS = aPosProcessFlowContext.getPreviousProcessOperationSpecificationFor(
                        aMainProcessFlow.get(), moduleNumber.get(), aModuleProcessFlow.get(),
                        aBackupPOS, outMainProcessFlow, outModuleNumber,
                        outModuleProcessFlow, inseqno, outseqno);
                Validations.check(aPrevPOS == null, new OmCode(retCodeConfig.getNotFoundPos()));

                strOpeNumber = aPrevPOS.getOperationNumber();
                Validations.check(outMainProcessFlow.get() == null, new OmCode(retCodeConfig.getNotFoundProcessFlow()));

                ProcessDefinition aMainPD = outMainProcessFlow.get().getRootProcessDefinition();
                Validations.check(aMainPD == null, new OmCode(retCodeConfig.getNotFoundMainRoute()));

                strTergetRouteID = aMainPD.getIdentifier();
            }
            key = strTergetRouteID + "." + outModuleNumber.get() + "." + strOpeNumber;

        }
        String passCountWaferLevel = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_CONTROL.getValue();
        String passCountWaferLevelEventCreation = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_EVENT_CREATE.getValue();
        List<ProductDTO.WaferInfo> waferInfoSeq = cimLot.getAllWaferInfo();
        Validations.check(CimObjectUtils.isEmpty(waferInfoSeq), new OmCode(retCodeConfig.getProductCountZero()));

        String currentKey_var = null;
        String prevKey_var = null;
        if (!CimStringUtils.equals(passCountWaferLevel, "0")
                && CimStringUtils.equals(passCountWaferLevelEventCreation, "1")) {
            currentKey_var = anEventRecord.getLotData().getRouteID() + "." + anEventRecord.getLotData().getOperationNumber();
            prevKey_var = oldCurrentPOData.getRouteID() + "." + oldCurrentPOData.getOperationNumber();
        }
        Long maxReworkCount = 0L;
        List<Event.WaferReworkCountEventData> waferReworkCountEventDatas = new ArrayList<>();
        List<Event.WaferPassCountEventData> waferPassCountEventDatas = new ArrayList<>();
        for (ProductDTO.WaferInfo waferInfo : waferInfoSeq) {
            Event.WaferReworkCountEventData waferReworkCountEventData = new Event.WaferReworkCountEventData();
            Event.WaferPassCountEventData waferPassCountEventData = new Event.WaferPassCountEventData();
            waferReworkCountEventDatas.add(waferReworkCountEventData);
            CimWafer cimWafer = baseCoreFactory.getBO(CimWafer.class, waferInfo.getWaferID());
            Validations.check(CimObjectUtils.isEmpty(cimWafer),retCodeConfig.getNotFoundWafer());
            Long reworkCount = cimWafer.getReworkCount(key);
            waferReworkCountEventData.setReworkCount(reworkCount);
            waferReworkCountEventData.setWaferID(waferInfo.getWaferID().getValue());
            if (!CimStringUtils.equals(passCountWaferLevel, "0")
                    && CimStringUtils.equals(passCountWaferLevelEventCreation, "1")) {
                waferPassCountEventDatas.add(waferPassCountEventData);
                Long previousPassCount = cimWafer.getPassCount(prevKey_var);
                Long passCount = cimWafer.getPassCount(currentKey_var);
                waferPassCountEventData.setPreviousPassCount(previousPassCount.intValue());
                waferPassCountEventData.setPassCount(passCount.intValue());
                waferPassCountEventData.setWaferID(waferInfo.getWaferID().getValue());
            }
            if (waferReworkCountEventData.getReworkCount() > maxReworkCount) {
                maxReworkCount = waferReworkCountEventData.getReworkCount();
            }
        }
        anEventRecord.setReworkWafers(waferReworkCountEventDatas);
        anEventRecord.setProcessWafers(waferPassCountEventDatas);
        anEventRecord.setReworkCount(maxReworkCount);

        Event.ProcessOperationEventData processOperationEventData = new Event.ProcessOperationEventData();
        BeanUtils.copyProperties(oldCurrentPOData, processOperationEventData);
        anEventRecord.setOldCurrentPOData(processOperationEventData);
        anEventRecord.setReasonCodeID(lotReworkEventMakeParams.getReasonCodeID());

        anEventRecord.setEventCommon(setEventData(objCommon, lotReworkEventMakeParams.getClaimMemo()));
        eventManager.createEvent(anEventRecord, CimLotReworkEvent.class);
    }

    @Override
    public void lotWaferMoveEventMake(Infos.ObjCommon objCommon, Infos.NewLotAttributes newLotAttributes, String transactionID, String claimMemo) {
        List<Infos.NewWaferAttributes> newWaferAttributesList = newLotAttributes.getNewWaferAttributesList();
        Validations.check(CimObjectUtils.isEmpty(newWaferAttributesList), retCodeConfig.getInvalidInputParam());
        Event.LotWaferMoveEventRecord anEventRecord = new Event.LotWaferMoveEventRecord();
        CimLot aNewLot = baseCoreFactory.getBO(CimLot.class, newWaferAttributesList.get(0).getNewLotID());
        Validations.check(aNewLot == null, new OmCode(retCodeConfig.getNotFoundLot(), newWaferAttributesList.get(0).getNewLotID().getValue()));
        //-------------------------------
        // New lot Information
        //-------------------------------
        Event.LotEventData newLotEventData = aNewLot.getEventData();
        anEventRecord.setDestinationLotData(newLotEventData);
        /*------------------------------------------------------------------------------------*/
        /*   Get EndBankID of SourceLot                                                       */
        /*   If inheritingSTB case, sourceLot is changed to destinationLot.                   */
        /*   So, EndBank of SourceLot and destinationLot(NewLot)'s route->startBank is same   */
        /*------------------------------------------------------------------------------------*/
        Boolean bInheritLotID = false;
        ObjectIdentifier sourceLotEndBankID = new ObjectIdentifier();
        if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.STB_RELEASED_LOT_REQ.getValue())
                && CimStringUtils.equals(ObjectIdentifier.fetchValue(newWaferAttributesList.get(0).getNewLotID()),
                ObjectIdentifier.fetchValue(newWaferAttributesList.get(0).getSourceLotID()))) {
            bInheritLotID = true;
            CimLot aSourceLotTmp = baseCoreFactory.getBO(CimLot.class, newWaferAttributesList.get(0).getSourceLotID());
            Validations.check(aSourceLotTmp == null, new OmCode(retCodeConfig.getNotFoundLot(), newWaferAttributesList.get(0).getSourceLotID().getValue()));
            CimProcessOperation aPO = aSourceLotTmp.getProcessOperation();
            Validations.check(aPO == null, new OmCode(retCodeConfig.getNotFoundProcessOperation(), ""));
            CimProcessDefinition aMainPD = aPO.getMainProcessDefinition();
            Validations.check(aMainPD == null, new OmCode(retCodeConfig.getNotFoundProcessDefinition(),""));
            CimBank aStartBank = aMainPD.getStartBank();
            Validations.check(aStartBank == null, new OmCode(retCodeConfig.getNotFoundBank(), ""));
            sourceLotEndBankID = new ObjectIdentifier(aStartBank.getIdentifier(), aStartBank.getPrimaryKey());
        }
        //-------------------------------
        // New lot Wafer Information
        //-------------------------------
        List<Event.NewWaferEventData> currentWafersList = new ArrayList<>();
        anEventRecord.setCurrentWafers(currentWafersList);
        for (Infos.NewWaferAttributes newWaferAttributes : newWaferAttributesList) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(newWaferAttributes.getSourceWaferID()), new OmCode(retCodeConfig.getSourceWaferObjrefBlank(), newWaferAttributes.getSourceWaferID().getValue()));
            Event.NewWaferEventData currentWaferEventData = new Event.NewWaferEventData();
            currentWafersList.add(currentWaferEventData);
            CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, newWaferAttributes.getSourceWaferID());
        //    Validations.check(aPosWafer == null, retCodeConfig.getNotFoundWafer());
            currentWaferEventData.setWaferID(newWaferAttributes.getNewWaferID().getValue());
            if (aPosWafer != null) {
                currentWaferEventData.setControlWaferFlag(aPosWafer.isControlWafer());
            }
            currentWaferEventData.setSlotNumber(CimNumberUtils.longValue(newWaferAttributes.getNewSlotNumber()));
            currentWaferEventData.setOriginalWaferID(newWaferAttributes.getSourceWaferID().getValue());
        }
        if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.MERGE_WAFER_LOT_REQ.getValue())
                && CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.REWORK_PARTIAL_WAFER_LOT_CANCEL_REQ.getValue())
                && CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.MERGE_WAFER_LOT_NOT_ON_ROUTE_REQ.getValue())) {
            //-----------------------------------------
            // New lot not moved Wafer Information
            //-----------------------------------------
            List<Infos.LotWaferAttributes> lotMaterialsGetWafersResult = lotMethod.lotMaterialsGetWafers(objCommon, newWaferAttributesList.get(0).getNewLotID());
            List<Event.NewWaferEventData> sourceWafersList = new ArrayList<>();
            for (Infos.LotWaferAttributes lotWaferAttributes : lotMaterialsGetWafersResult) {
                Boolean bNewWaferIDFound = false;
                Event.NewWaferEventData sourceWaferEventData = new Event.NewWaferEventData();
                sourceWafersList.add(sourceWaferEventData);
                for (Infos.NewWaferAttributes newWaferAttributes : newWaferAttributesList) {
                    if (CimStringUtils.equals(newWaferAttributes.getNewWaferID().getValue(), lotWaferAttributes.getWaferID().getValue())) {
                        bNewWaferIDFound = true;
                        break;
                    }
                }
                if (!bNewWaferIDFound) {
                    sourceWaferEventData.setWaferID(lotWaferAttributes.getWaferID().getValue());
                    sourceWaferEventData.setControlWaferFlag(lotWaferAttributes.getControlWaferFlag());
                    sourceWaferEventData.setSlotNumber(lotWaferAttributes.getSlotNumber().longValue());
                }
            }
            if (!CimObjectUtils.isEmpty(sourceWafersList)) {
                anEventRecord.setSourceWafers(sourceWafersList);
            }
        }
        //-------------------------------
        // Check and collect Source lot IDs
        //-------------------------------
        Long nSourceLotIDLen = 0L;
        List<ObjectIdentifier> strSourceLotIDSeq = new ArrayList<>();
        for (int i = 0; i < newWaferAttributesList.size(); i++) {
            if (i == 0) {
                nSourceLotIDLen++;
                strSourceLotIDSeq.add(newWaferAttributesList.get(i).getSourceLotID());
            } else {
                Boolean bSourceLotIDAlreadyAdded = false;
                for (int j = 0; j < nSourceLotIDLen; j++) {
                    if (ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(i).getSourceLotID(), strSourceLotIDSeq.get(j))) {
                        bSourceLotIDAlreadyAdded = true;
                        break;
                    }
                }
                if (!bSourceLotIDAlreadyAdded) {
                    nSourceLotIDLen++;
                    strSourceLotIDSeq.add(newWaferAttributesList.get(i).getSourceLotID());
                }
            }
        }
        //-------------------------------
        // Source lot Information
        //-------------------------------
        List<Event.SourceLotEventData> sourceLotsList = new ArrayList<>();
        for (int i = 0; i < nSourceLotIDLen; i++) {
            Event.SourceLotEventData sourceLotEventData = new Event.SourceLotEventData();
            sourceLotsList.add(sourceLotEventData);
            CimLot aSourceLot = baseCoreFactory.getBO(CimLot.class, strSourceLotIDSeq.get(i));
            Validations.check(aSourceLot == null, new OmCode(retCodeConfig.getNotFoundLot(), strSourceLotIDSeq.get(i).getValue()));
            Event.LotEventData lotEventData = aSourceLot.getEventData();
            sourceLotEventData.setSourceLotData(lotEventData);
            if (bInheritLotID) {
                lotEventData.setBankID(sourceLotEndBankID.getValue());
            }
            //-------------------------------
            // Source lot Wafer Information
            //-------------------------------
            List<Event.WaferEventData> currentWafers = new ArrayList<>();
            for (Infos.NewWaferAttributes newWaferAttributes : newWaferAttributesList) {
                if (CimStringUtils.equals(newWaferAttributes.getSourceLotID().getValue(), strSourceLotIDSeq.get(i).getValue())) {
                    Event.WaferEventData currentWafer = new Event.WaferEventData();
                    currentWafers.add(currentWafer);
                    CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, newWaferAttributes.getSourceWaferID());
              //      Validations.check(aPosWafer == null, retCodeConfig.getNotFoundWafer());
                    currentWafer.setWaferID(newWaferAttributes.getSourceWaferID().getValue());
                    if (aPosWafer != null) {
                        currentWafer.setControlWaferFlag(aPosWafer.isControlWafer());
                    }
                    currentWafer.setDestinationSlotNumber(CimNumberUtils.longValue(newWaferAttributes.getNewSlotNumber()));
                    currentWafer.setOriginalSlotNumber(CimNumberUtils.longValue(newWaferAttributes.getNewSlotNumber()));
                    currentWafer.setOriginalWaferID(newWaferAttributes.getSourceWaferID().getValue());
                }
            }
            if (!CimObjectUtils.isEmpty(currentWafers)) {
                sourceLotEventData.setCurrentWafers(currentWafers);
            }
            List<Event.WaferEventData> sourceWafersList = new ArrayList<>();
            if (!CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.MERGE_WAFER_LOT_REQ.getValue())
                    && !CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.REWORK_PARTIAL_WAFER_LOT_CANCEL_REQ.getValue())
                    && !CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.MERGE_WAFER_LOT_NOT_ON_ROUTE_REQ.getValue())) {
                //--------------------------------------------
                // Source lot not moved Wafer Information
                //--------------------------------------------
                List<Infos.LotWaferAttributes> listRetCode = lotMethod.lotMaterialsGetWafers(objCommon, strSourceLotIDSeq.get(i));
                for (Infos.LotWaferAttributes lotWaferAttributes : listRetCode) {
                    Event.WaferEventData sourceWafers = new Event.WaferEventData();
                    sourceWafersList.add(sourceWafers);
                    if (!ObjectIdentifier.isEmpty(lotWaferAttributes.getWaferID())) {
                        sourceWafers.setWaferID(lotWaferAttributes.getWaferID().getValue());
                    }
                    sourceWafers.setControlWaferFlag(lotWaferAttributes.getControlWaferFlag());
                    if (!CimObjectUtils.isEmpty(lotWaferAttributes.getSlotNumber())) {
                        sourceWafers.setOriginalSlotNumber(lotWaferAttributes.getSlotNumber().longValue());
                    }
                }
            }
            if (!CimObjectUtils.isEmpty(sourceWafersList)) {
                sourceLotEventData.setSourceWafers(sourceWafersList);
            }
        }
        if (!CimObjectUtils.isEmpty(sourceLotsList)) {
            anEventRecord.setSourceLots(sourceLotsList);
        }
        //-------------------------------
        // Common information
        //-------------------------------
        anEventRecord.setEventCommon(setEventData(objCommon, claimMemo));

        //-------------------------------
        // Send event data to Event Manager
        //-------------------------------
        eventManager.createEvent(anEventRecord, CimLotWaferMoveEvent.class);
    }

    @Override
    public void lotReticleSetChangeEventMake(Infos.ObjCommon objCommon, Inputs.LotReticleSetChangeEventMakeParams params) {
        Event.LotReticleSetChangeEventRecord anEventRecord = new Event.LotReticleSetChangeEventRecord();
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, params.getLotID());
        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), params.getLotID().getValue()));
        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotData(lotEventData);
        CimReticleSet aReticleSet = aLot.getReticleSet();
        if (aReticleSet != null) {
            anEventRecord.setReticleSetID(aReticleSet.getIdentifier());
        }
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        // Put into the queue of PosEventManager
        eventManager.createEvent(anEventRecord, CimLotReticleSetChangeEvent.class);
    }

    @Override
    public void lotOperationMoveEventMakeOther(Infos.ObjCommon objCommon, Inputs.LotOperationMoveEventMakeOtherParams params) {
        Event.LotOperationMoveEventRecord anEventRecord = new Event.LotOperationMoveEventRecord();
        anEventRecord.setTestCriteriaResult(true);
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, params.getCreatedID());
        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), "*****"));
        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotData(lotEventData);

        CimProcessFlowContext aPosProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(aPosProcessFlowContext == null, new OmCode(retCodeConfig.getNotFoundPfx(), ""));
        CimProcessOperation aPrevProcessOperation = aPosProcessFlowContext.getPreviousProcessOperation();
        anEventRecord.setPreviousOperationPassCount(0);
        if (aPrevProcessOperation != null) {
            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevProcessOperation.getEventData();
            anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
            anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount(prevPOEventData.getOperationPassCount());
            anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
            anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
        }
        /*===== set reticles, fixtures info =====*/
        List<String> reticleIDs = new ArrayList<>();
        List<String> fixtureIDs = new ArrayList<>();
        /*===== set recipe parameter info =====*/
        List<Event.RecipeParmEventData> recipeParameters = new ArrayList<>();
        anEventRecord.setLocateBackFlag(false);
        String passCountWaferLevel = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_CONTROL.getValue();
        String passCountWaferLevelEventCreation = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_EVENT_CREATE.getValue();
        if (!CimStringUtils.equals(passCountWaferLevel, "0") && CimStringUtils.equals(passCountWaferLevelEventCreation, "1")) {
            String key_var = lotEventData.getRouteID() + "." + lotEventData.getOperationNumber();
            List<ProductDTO.WaferInfo> waferInfoSeq = aLot.getAllWaferInfo();
            Validations.check(CimObjectUtils.isEmpty(waferInfoSeq), retCodeConfig.getProductCountZero());
            List<Event.WaferPassCountEventData> processWafersList = new ArrayList<>();
            anEventRecord.setProcessWafers(processWafersList);
            for (ProductDTO.WaferInfo waferInfo : waferInfoSeq) {
                CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, waferInfo.getWaferID());
                Validations.check(aPosWafer == null, retCodeConfig.getNotFoundWafer());
                Event.WaferPassCountEventData processWafers = new Event.WaferPassCountEventData();
                processWafers.setPassCount(aPosWafer.getPassCount(key_var).intValue());
                processWafers.setWaferID(waferInfo.getWaferID().getValue());
            }
        }
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        eventManager.createEvent(anEventRecord, CimLotOperationMoveEvent.class);
    }

    @Override
    public void lotOperationMoveEventMakeOpeComp(Infos.ObjCommon objCommon, Inputs.LotOperationMoveEventMakeOpeComp params) {
        /*--------------------------*/
        /*   Prepare Event Record   */
        /*--------------------------*/
        ObjectIdentifier cassetteID = params.getCassetteID();
        Infos.LotInCassette lotInCassette = params.getLotInCassette();
        Event.LotOperationCompleteEventRecord anEventRecord = new Event.LotOperationCompleteEventRecord();
        anEventRecord.setTestCriteriaResult(true);
        /*--------------------*/
        /*   Get Lot Object   */
        /*--------------------*/
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());
        /*------------------------------*/
        /*   Set Data to Event Record   */
        /*------------------------------*/
        Event.LotEventData lotEventData = aLot.getEventData();
        Event.LotEventData lotData = new Event.LotEventData();
        BeanUtils.copyProperties(lotEventData, lotData);
        lotData.setLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
        lotData.setLotType(lotInCassette.getLotType());
        lotData.setCassetteID(ObjectIdentifier.fetchValue(cassetteID));
        anEventRecord.setLotData(lotData);
        Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
        anEventRecord.setLogicalRecipeID(ObjectIdentifier.fetchValue(startRecipe.getLogicalRecipeID()));
        anEventRecord.setMachineRecipeID(ObjectIdentifier.fetchValue(startRecipe.getMachineRecipeID()));
        anEventRecord.setPhysicalRecipeID(startRecipe.getPhysicalRecipeID());
        CimProcessFlowContext pfx = aLot.getProcessFlowContext();
        Validations.check(pfx == null, retCodeConfig.getNotFoundPfx());

        Boolean pendingMoveNext = aLot.isPendingMoveNext();
        CimProcessOperation po = pendingMoveNext? pfx.getCurrentProcessOperation() : pfx.getPreviousProcessOperation();
        anEventRecord.setPreviousOperationPassCount(0);

        ProcessDTO.PosProcessOperationEventData poEventData = Optional.ofNullable(po)
                .map(ProcessOperation::getEventData).orElse(null);
        if (null != poEventData) {
            anEventRecord.setPreviousRouteID(poEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(poEventData.getOperationNumber());
            anEventRecord.setPreviousOperationID(poEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount(poEventData.getOperationPassCount());
            anEventRecord.setPreviousObjrefPOS(poEventData.getObjrefPOS());
            anEventRecord.setPreviousObjrefMainPF(poEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(poEventData.getObjrefModulePOS());
        }

        List<String> reticleIDs = new ArrayList<>();
        anEventRecord.setReticleIDs(reticleIDs);
        List<Infos.StartReticleInfo> startReticleList = startRecipe.getStartReticleList();
        if (CimArrayUtils.isNotEmpty(startReticleList)) {
            for (Infos.StartReticleInfo startReticleInfo : startReticleList) {
                reticleIDs.add(ObjectIdentifier.fetchValue(startReticleInfo.getReticleID()));
            }
        }
        List<String> fixtureIDs = startRecipe.getStartFixtureList().stream()
                .map(Infos.StartFixtureInfo::getFixtureID)
                .map(ObjectIdentifier::fetchValue)
                .collect(Collectors.toList());
        anEventRecord.setFixtureIDs(fixtureIDs);
        /*===== set recipe parameter info =====*/
        if (CimStringUtils.equals(lotInCassette.getRecipeParameterChangeType(),
                BizConstant.SP_RPARM_CHANGETYPE_BYLOT)) {
            List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
            if (!CimObjectUtils.isEmpty(lotWaferList)) {
                List<Event.RecipeParmEventData> recipeParameters = new ArrayList<>();
                anEventRecord.setRecipeParameters(recipeParameters);
                List<Infos.StartRecipeParameter> startRecipeParameterList = lotWaferList.get(0).getStartRecipeParameterList();
                if (startRecipeParameterList != null){
                    for (Infos.StartRecipeParameter startRecipeParameter : startRecipeParameterList) {
                        Event.RecipeParmEventData recipeParmEventData = new Event.RecipeParmEventData();
                        recipeParameters.add(recipeParmEventData);
                        recipeParmEventData.setParameterName(startRecipeParameter.getParameterName());
                        recipeParmEventData.setParameterValue(startRecipeParameter.getParameterValue());
                    }
                }
            }
        } else {
            // Set RecipeParameter for every Wafer
            Infos.LotRecipeParameterEventStructOut rcpParamEventData = lotMethod.lotRecipeParameterEventStruct(objCommon,
                    cassetteID, lotInCassette);

            List<Infos.OpeHisRecipeParmInfo> opeHisRecipeParmInfoList = rcpParamEventData.getStrOpeHisRecipeParmInfo();
            List<Event.RecipeParmEventData> recipeParameters = new ArrayList<>();
            anEventRecord.setRecipeParameters(recipeParameters);
            for (Infos.OpeHisRecipeParmInfo opeHisRecipeParmInfo : opeHisRecipeParmInfoList) {
                Event.RecipeParmEventData recipeParmEventData = new Event.RecipeParmEventData();
                recipeParameters.add(recipeParmEventData);
                recipeParmEventData.setParameterName(opeHisRecipeParmInfo.getRecipeParameterName());
                recipeParmEventData.setParameterValue(opeHisRecipeParmInfo.getRecipeParameterValue());
            }
            List<Infos.OpeHisRecipeParmWaferInfo> opeHisRecipeParmWaferInfoList = rcpParamEventData.getStrOpeHisRecipeParmWaferInfo();
            List<Event.WaferLevelRecipeEventData> waferLevelRecipes = new ArrayList<>();
            anEventRecord.setWaferLevelRecipe(waferLevelRecipes);
            for (Infos.OpeHisRecipeParmWaferInfo opeHisRecipeParmWaferInfo : opeHisRecipeParmWaferInfoList) {
                Event.WaferLevelRecipeEventData waferLevelRecipeEventData = new Event.WaferLevelRecipeEventData();
                waferLevelRecipes.add(waferLevelRecipeEventData);
                waferLevelRecipeEventData.setWaferID(ObjectIdentifier.fetchValue(opeHisRecipeParmWaferInfo.getWaferID()));
                waferLevelRecipeEventData.setMachineRecipeID(ObjectIdentifier.fetchValue(opeHisRecipeParmWaferInfo.getMachineRecipeID()));
                List<Infos.OpeHisRecipeParmInfo> opeHisRecipeParmInfoList2 = opeHisRecipeParmWaferInfo.getStrOpeHisRecipeParmInfo();
                List<Event.WaferRecipeParmEventData> waferRecipeParameters = new ArrayList<>();
                waferLevelRecipeEventData.setWaferRecipeParameters(waferRecipeParameters);
                for (Infos.OpeHisRecipeParmInfo opeHisRecipeParmInfo2 : opeHisRecipeParmInfoList2) {
                    Event.WaferRecipeParmEventData waferRecipeParmEventData = new Event.WaferRecipeParmEventData();
                    waferRecipeParameters.add(waferRecipeParmEventData);
                    waferRecipeParmEventData.setParameterName(opeHisRecipeParmInfo2.getRecipeParameterName());
                    waferRecipeParmEventData.setParameterValue(opeHisRecipeParmInfo2.getRecipeParameterValue());
                }
            }
        }
        /*===== set equipment info =====*/
        anEventRecord.setEquipmentID(params.getEquipmentID().getValue());
        anEventRecord.setOperationMode(params.getOperationMode());
        /*===== set flow batch info =====*/
        CimFlowBatch aFlowBatch = aLot.getFlowBatch();
        if (aFlowBatch != null) {
            anEventRecord.setBatchID(aFlowBatch.getIdentifier());
        }
        /*===== set controlJob info =====*/
        anEventRecord.setControlJobID(params.getControlJobID().getValue());
        anEventRecord.setLocateBackFlag(false);
        int passCountWaferLevel = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_CONTROL.getIntValue();
        int passCountWaferLevelEventCreation = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_EVENT_CREATE.getIntValue();
        if (passCountWaferLevel != 0 && passCountWaferLevelEventCreation == 1) {
            List<ProductDTO.WaferInfo> waferInfoList = aLot.getAllWaferInfo();
            Validations.check(CimObjectUtils.isEmpty(waferInfoList), retCodeConfig.getProductCountZero());

            if (po != null) {
                if (!CimStringUtils.isEmpty(poEventData.getRouteID()) && !CimStringUtils.isEmpty(poEventData.getOperationNumber())) {
                    // Get AssignedSamplingWafers
                    List<String> assignedSamplingWafers = po.getAssignedSamplingWafers();
                    if (!CimObjectUtils.isEmpty(assignedSamplingWafers)) {
                        String key_var = poEventData.getRouteID() + "." + poEventData.getOperationNumber();
                        List<Event.WaferPassCountNoPreviousEventData> processWafers = new ArrayList<>();
                        anEventRecord.setProcessWafers(processWafers);
                        for (ProductDTO.WaferInfo waferInfo : waferInfoList) {
                            CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class,waferInfo.getWaferID());
                            if (!assignedSamplingWafers.contains(waferInfo.getWaferID().getValue())) {
                                Event.WaferPassCountNoPreviousEventData waferPassCountEventData = new Event.WaferPassCountNoPreviousEventData();
                                processWafers.add(waferPassCountEventData);
                                waferPassCountEventData.setPassCount(CimNumberUtils.intValue(aPosWafer.getPassCount(key_var)));
                                waferPassCountEventData.setWaferID(ObjectIdentifier.fetchValue(waferInfo.getWaferID()));
                                waferPassCountEventData.setCurrentOperationFlag(false);
                            }
                        }
                    }
                }
            }

            String key_var = lotEventData.getRouteID() + "." + lotEventData.getOperationNumber();
            List<Event.WaferPassCountNoPreviousEventData> processWafers = anEventRecord.getProcessWafers();
            for (ProductDTO.WaferInfo waferInfo : waferInfoList) {
                CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, waferInfo.getWaferID());
                Event.WaferPassCountNoPreviousEventData waferPassCountNoPreviousEventData = new Event.WaferPassCountNoPreviousEventData();
                processWafers.add(waferPassCountNoPreviousEventData);
                waferPassCountNoPreviousEventData.setPassCount(CimNumberUtils.intValue(aPosWafer.getPassCount(key_var)));
                waferPassCountNoPreviousEventData.setWaferID(ObjectIdentifier.fetchValue(waferInfo.getWaferID()));
                waferPassCountNoPreviousEventData.setCurrentOperationFlag(true);
            }
        }
        /*===== set common info =====*/
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));

        eventManager.createEvent(anEventRecord, CimLotOperationCompleteEvent.class);

    }

    @Override
    public void lotOperationMoveEventMakeGatePass(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier lotID, String claimMemo) {
        Event.LotOperationMoveEventRecord anEventRecord = new Event.LotOperationMoveEventRecord();
        anEventRecord.setTestCriteriaResult(true);
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotData(lotEventData);
        CimProcessFlowContext aPosProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(aPosProcessFlowContext == null,  retCodeConfig.getNotFoundPfx());

        CimProcessOperation aPrevProcessOperation = aPosProcessFlowContext.getPreviousProcessOperation();
        anEventRecord.setPreviousOperationPassCount(0);
        if (aPrevProcessOperation != null) {
            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevProcessOperation.getEventData();
            anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
            anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount(prevPOEventData.getOperationPassCount());
            anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
            anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
        }
        anEventRecord.setLogicalRecipeID("");
        anEventRecord.setMachineRecipeID("");
        anEventRecord.setPhysicalRecipeID("");
        anEventRecord.setEquipmentID("");
        anEventRecord.setOperationMode("");
        anEventRecord.setBatchID("");
        anEventRecord.setControlJobID("");
        anEventRecord.setLocateBackFlag(false);
        int passCountWaferLevel = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_CONTROL.getIntValue();
        int passCountWaferLevelEventCreation = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_EVENT_CREATE.getIntValue();
        if (passCountWaferLevel != 0 && passCountWaferLevelEventCreation == 1) {
            String key_var = lotEventData.getRouteID() + "." + lotEventData.getOperationNumber();
            List<ProductDTO.WaferInfo> waferInfoList = aLot.getAllWaferInfo();
            Validations.check(!CimObjectUtils.isEmpty(waferInfoList), retCodeConfig.getProductCountZero());

            List<Event.WaferPassCountEventData> processWafers = new ArrayList<>();
            anEventRecord.setProcessWafers(processWafers);
            for (ProductDTO.WaferInfo waferInfo : waferInfoList) {
                CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, waferInfo.getWaferID());
                Event.WaferPassCountEventData waferPassCountEventData = new Event.WaferPassCountEventData();
                processWafers.add(waferPassCountEventData);
                waferPassCountEventData.setWaferID(waferInfo.getWaferID().getValue());
                waferPassCountEventData.setPassCount(aPosWafer.getPassCount(key_var).intValue());
            }
        }
        anEventRecord.setEventCommon(setEventData(objCommon, claimMemo));

        eventManager.createEvent(anEventRecord, CimLotOperationMoveEvent.class);

    }

    @Override
    public void eqpMonitorWaferUsedCountUpdateEventMake(Infos.ObjCommon objCommon, Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams params) {
        //--------------//
        //  Initialize  //
        //--------------//
        Event.EqpMonitorCountEventRecord anEventRecord = new Event.EqpMonitorCountEventRecord();
        //-----------------//
        //  Set Event Data //
        //-----------------//
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, params.getLotID());
        Validations.check(aLot == null, retCodeConfig.getNotFoundLot());

        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotData(lotEventData);
        if (!ObjectIdentifier.isEmpty(params.getControlJobID())) {
            //判断当前lot是否做了process move 操作，因此需要判断，这个添加这个 Flag 的逻辑判断
            final boolean checkConditionForPO = lotMethod.lotCheckConditionForPO(objCommon, params.getLotID());
            CimProcessOperation aPrePO;
            if (checkConditionForPO) {
                aPrePO = aLot.getProcessOperation();
            } else {
                aPrePO = aLot.getPreviousProcessOperation();
            }
            Validations.check(aPrePO == null, retCodeConfig.getNotFoundProcessOperation());

            ProcessDTO.PosProcessOperationEventData aPOEventData = aPrePO.getEventData();
            Event.LotEventData lotData = new Event.LotEventData();
            lotData.setRouteID(aPOEventData.getRouteID());
            lotData.setOperationNumber(aPOEventData.getOperationNumber());
            lotData.setOperationID(aPOEventData.getOperationID());
            lotData.setOperationPassCount(CimLongUtils.longValue(aPOEventData.getOperationPassCount()));
            lotData.setObjrefPOS(aPOEventData.getObjrefPOS());
            lotData.setObjrefPO(aPOEventData.getObjrefPO());
            lotData.setObjrefMainPF(aPOEventData.getObjrefMainPF());
            lotData.setObjrefModulePOS(aPOEventData.getObjrefModulePOS());
            anEventRecord.setLotData(lotData);
            anEventRecord.setEquipmentID(params.getEquipmentID().getValue());
            anEventRecord.setControlJobID(params.getControlJobID().getValue());
        }
        List<Infos.EqpMonitorWaferUsedCount> strEqpMonitorWaferUsedCountList = params.getStrEqpMonitorWaferUsedCountList();
        List<Event.EqpMonitorWaferCountEventData> eqpMonitorWaferCountEventDataList = new ArrayList<>();
        anEventRecord.setWafers(eqpMonitorWaferCountEventDataList);
        for (Infos.EqpMonitorWaferUsedCount eqpMonitorWaferUsedCount : strEqpMonitorWaferUsedCountList) {
            Event.EqpMonitorWaferCountEventData eqpMonitorWaferCountEventData = new Event.EqpMonitorWaferCountEventData();
            eqpMonitorWaferCountEventData.setWaferID(eqpMonitorWaferUsedCount.getWaferID().getValue());
            eqpMonitorWaferCountEventData.setEqpMonitorUsedCount(eqpMonitorWaferUsedCount.getEqpMonitorUsedCount());
            eqpMonitorWaferCountEventDataList.add(eqpMonitorWaferCountEventData);
        }

        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));

        //-----------------//
        //  Put Event Data //
        //-----------------//
        eventManager.createEvent(anEventRecord, CimEqpMonitorCountEvent.class);

    }

    @Override
    public void lotHoldEventMake(Infos.ObjCommon objCommon, Inputs.LotHoldEventMakeParams lotHoldEventMakeParams) {
        Event.LotHoldEventRecord anEventRecord = new Event.LotHoldEventRecord();
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotHoldEventMakeParams.getLotID());
        Validations.check(null == aLot, retCodeConfig.getNotFoundLot());

        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotData(lotEventData);
        CimProcessFlowContext aPosProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(null == aPosProcessFlowContext, retCodeConfig.getNotFoundPfx());

        CimProcessOperation aPrevProcessOperation = aPosProcessFlowContext.getPreviousProcessOperation();
        anEventRecord.setPreviousOperationPassCount(0L);
        if (aPrevProcessOperation != null) {
            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevProcessOperation.getEventData();
            anEventRecord.setPreviousRouteId(prevPOEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
            anEventRecord.setPreviousOperationId(prevPOEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount(prevPOEventData.getOperationPassCount());
            anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefModulePOS());
            anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
        }
        List<Infos.HoldHistory> holdHistoryList = lotHoldEventMakeParams.getHoldHistoryList();
        List<Event.LotHoldEventData> holdRecordList = new ArrayList<>();
        anEventRecord.setHoldRecords(holdRecordList);
        if (!CimObjectUtils.isEmpty(holdHistoryList)) {
            for (Infos.HoldHistory holdHistory : holdHistoryList) {
                Event.LotHoldEventData lotHoldEventData = new Event.LotHoldEventData();
                lotHoldEventData.setMovementFlag(holdHistory.getMovementFlag());
                lotHoldEventData.setChangeStateFlag(holdHistory.getChangeStateFlag());
                lotHoldEventData.setHoldType(holdHistory.getHoldType());
                lotHoldEventData.setHoldReasonCodeId(holdHistory.getHoldReasonCode());
                lotHoldEventData.setHoldUserId(holdHistory.getHoldPerson().getValue());
                lotHoldEventData.setHoldTimeStamp(holdHistory.getHoldTime());
                lotHoldEventData.setResponsibleOperationFlag(holdHistory.getResponsibleOperationFlag());
                lotHoldEventData.setResponsibleOperationExistFlag(holdHistory.getResponsibleOperationExistFlag());
                if (!CimObjectUtils.isEmpty(holdHistory.getReleaseClaimMemo())) {
                    lotHoldEventData.setHoldClaimMemo(holdHistory.getReleaseClaimMemo());
                } else {
                    lotHoldEventData.setHoldClaimMemo(holdHistory.getHoldClaimMemo());
                }
                // DepartmentNamePlate
                lotHoldEventData.setDepartmentNamePlate(holdHistory.getDepartmentNamePlate());
                holdRecordList.add(lotHoldEventData);
            }
            anEventRecord.setReleaseReasonCodeId(holdHistoryList.get(0).getReleaseReasonCode());
        }
        anEventRecord.setEventCommon(setEventData(objCommon, "",lotHoldEventMakeParams.getTransactionID()));
        if (!CimObjectUtils.isEmpty(holdHistoryList)) {
            if (!CimObjectUtils.isEmpty(holdHistoryList.get(0).getReleaseClaimMemo())) {
                anEventRecord.getEventCommon().setEventMemo(holdHistoryList.get(0).getReleaseClaimMemo());
            } else {
                anEventRecord.getEventCommon().setEventMemo(holdHistoryList.get(0).getHoldClaimMemo());
            }
        }
        CimEventBase event = eventManager.createEvent(anEventRecord, CimLotHoldEvent.class);

        //-------------------------------//
        // Extendable Event              //
        //-------------------------------//
        if (null != event) {
            String transactionID = objCommon.getTransactionID();
            // When the PostProcess exec "HoldLot" Operation. Don't create a AMS Event.
            if (TransactionIDEnum.equals(transactionID, POST_PROCESS_ACTION_REGIST_REQ)
                    || TransactionIDEnum.equals(transactionID, POST_PROCESS_EXEC_REQ)) {
                return;
            }
            if (!CimStringUtils.equals(anEventRecord.getLotData().getLotStatus(), BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD)) {
                return;
            }
            // Make Event
            BaseEvent baseEvent = new BaseEvent(this);
            baseEvent.setTxID(transactionID);
            baseEvent.setUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
            baseEvent.setEventID(event.getPrimaryKey());
            baseEvent.setEventInfo(JSONObject.toJSONString(anEventRecord));
            extendableEventManager.makeEvent(BizConstant.CATEGORY_HOLD_LOT, baseEvent);
        }

    }

    @Override
    public void lotPartialReworkCancelEventMake(Infos.ObjCommon objCommon, Inputs.LotPartialReworkCancelEventMakeParams lotPartialReworkCancelEventMakeParams) {
        Event.LotReworkEventRecord anEventRecord = new Event.LotReworkEventRecord();
        ObjectIdentifier parentLotID = lotPartialReworkCancelEventMakeParams.getParentLotID();
        CimLot aParentLot = baseCoreFactory.getBO(CimLot.class, parentLotID);
        Validations.check(aParentLot == null, new OmCode(retCodeConfig.getNotFoundLot(), parentLotID.getValue()));

        ObjectIdentifier childLotID = lotPartialReworkCancelEventMakeParams.getChildLotID();
        CimLot aChildLot = baseCoreFactory.getBO(CimLot.class, childLotID);
        Validations.check(aChildLot == null, new OmCode(retCodeConfig.getNotFoundLot(), childLotID.getValue()));

        Event.LotEventData childLotEventData = aChildLot.getEventData();
        Event.LotEventData lotData = new Event.LotEventData();
        lotData.setLotID(childLotEventData.getLotID());
        lotData.setLotType(childLotEventData.getLotType());
        lotData.setCassetteID(childLotEventData.getCassetteID());
        lotData.setLotStatus(childLotEventData.getLotStatus());
        lotData.setCustomerID(childLotEventData.getCustomerID());
        lotData.setPriorityClass(childLotEventData.getPriorityClass());
        lotData.setProductID(childLotEventData.getProductID());
        lotData.setOriginalWaferQuantity(childLotEventData.getOriginalWaferQuantity());
        lotData.setCurrentWaferQuantity(childLotEventData.getCurrentWaferQuantity());
        lotData.setProductWaferQuantity(childLotEventData.getProductWaferQuantity());
        lotData.setControlWaferQuantity(childLotEventData.getControlWaferQuantity());
        lotData.setHoldState(childLotEventData.getHoldState());
        lotData.setBankID(childLotEventData.getBankID());
        lotData.setWaferHistoryTimeStamp(childLotEventData.getWaferHistoryTimeStamp());
        anEventRecord.setLotData(lotData);

        Event.ProcessOperationEventData oldCurrentPOData = new Event.ProcessOperationEventData();
        oldCurrentPOData.setRouteID(childLotEventData.getRouteID());
        oldCurrentPOData.setOperationNumber(childLotEventData.getOperationNumber());
        oldCurrentPOData.setOperationID(childLotEventData.getOperationID());
        oldCurrentPOData.setOperationPassCount(childLotEventData.getOperationPassCount().intValue());
        oldCurrentPOData.setObjrefPO(childLotEventData.getObjrefPO());
        oldCurrentPOData.setObjrefPOS(childLotEventData.getObjrefPOS());
        oldCurrentPOData.setObjrefMainPF(childLotEventData.getObjrefMainPF());
        oldCurrentPOData.setObjrefModulePOS(childLotEventData.getObjrefModulePOS());
        anEventRecord.setOldCurrentPOData(oldCurrentPOData);

        Event.LotEventData parentLotEventData = aParentLot.getEventData();
        anEventRecord.getLotData().setRouteID(parentLotEventData.getRouteID());
        anEventRecord.getLotData().setOperationNumber(parentLotEventData.getOperationNumber());
        anEventRecord.getLotData().setOperationID(parentLotEventData.getOperationID());
        anEventRecord.getLotData().setOperationPassCount(parentLotEventData.getOperationPassCount());
        anEventRecord.getLotData().setObjrefPOS(parentLotEventData.getObjrefPOS());
        anEventRecord.getLotData().setObjrefMainPF(parentLotEventData.getObjrefMainPF());
        anEventRecord.getLotData().setObjrefModulePOS(parentLotEventData.getObjrefModulePOS());

        CimProcessFlowContext aPosProcessFlowContext = aParentLot.getProcessFlowContext();
        Validations.check(aPosProcessFlowContext == null, retCodeConfig.getNotFoundPfx());

        //------------------------------------------------------------
        // Get rework count from rework out operation. (Normal Case)
        //------------------------------------------------------------
        CimProcessFlowContext aChildLotPosPFX = aChildLot.getProcessFlowContext();
        Validations.check(aChildLotPosPFX == null, retCodeConfig.getNotFoundPfx());

        ProcessDTO.BackupOperation backupOperation = aChildLotPosPFX.getBackupOperation();
        String routeIDOpeNumber = null;
        if (!CimObjectUtils.isEmpty(backupOperation.getReworkOutKey())) {
            routeIDOpeNumber = backupOperation.getReworkOutKey();
        } else {
            //-------------------------------------
            // Get Rework Count from before 2 POSs.
            //-------------------------------------
            Integer inseqno = 0;
            AtomicReference<Integer> outseqno = new AtomicReference<>();
            AtomicReference<CimProcessFlow> aMainProcessFlow = new AtomicReference<>();
            AtomicReference<CimProcessFlow> aModuleProcessFlow = new AtomicReference<>();
            AtomicReference<CimProcessFlow> outMainProcessFlow = new AtomicReference<>();
            AtomicReference<CimProcessFlow> outModuleProcessFlow = new AtomicReference<>();
            AtomicReference<String> moduleNumber = new AtomicReference<>();
            AtomicReference<String> outModuleNumber = new AtomicReference<>();
            CimProcessOperationSpecification aBackupPOS = aChildLotPosPFX.getBackupProcessOperationSpecificationFor(inseqno, outseqno,
                    aMainProcessFlow, moduleNumber, aModuleProcessFlow);
            inseqno = 0;
            outseqno.set(0);
            Validations.check(aBackupPOS == null, retCodeConfig.getNotFoundPos());

            CimProcessOperationSpecification aPrevPOS = aChildLotPosPFX.getPreviousProcessOperationSpecificationFor(aMainProcessFlow.get(),
                    moduleNumber.get(), aModuleProcessFlow.get(), aBackupPOS, outMainProcessFlow,
                    outModuleNumber, outModuleProcessFlow, inseqno, outseqno);
            Validations.check(aPrevPOS == null, retCodeConfig.getNotFoundPos());

            String strOpeNumber = aPrevPOS.getOperationNumber();
            Validations.check(CimObjectUtils.isEmpty(outMainProcessFlow.get()), retCodeConfig.getNotFoundProcessFlow());

            ProcessDefinition aPosMainPD = outMainProcessFlow.get().getRootProcessDefinition();
            Validations.check(aPosMainPD == null, retCodeConfig.getNotFoundMainRoute());

            String strTergetRouteID = aPosMainPD.getIdentifier();
            routeIDOpeNumber = strTergetRouteID + "." + outModuleNumber.get() + "." + strOpeNumber;
        }
        //-----------------------------------------------
        // Make a Rework Event Data by Child Lot's Wafer
        //-----------------------------------------------
        List<ProductDTO.WaferInfo> childWaferInfoList = aChildLot.getAllWaferInfo();
        Validations.check(CimObjectUtils.isEmpty(childWaferInfoList), retCodeConfig.getProductCountZero());

        Long maxReworkCount = 0L;
        List<Event.WaferReworkCountEventData> reworkWafers = new ArrayList<>();
        anEventRecord.setReworkWafers(reworkWafers);
        for (ProductDTO.WaferInfo waferInfo : childWaferInfoList) {
            Event.WaferReworkCountEventData waferReworkCountEventData = new Event.WaferReworkCountEventData();
            CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, waferInfo.getWaferID());
            Long tmpReworkCount = aPosWafer.getReworkCount(routeIDOpeNumber);
            if (tmpReworkCount > 0) {
                waferReworkCountEventData.setReworkCount(tmpReworkCount - 1);
            } else {
                waferReworkCountEventData.setReworkCount(0L);
            }
            waferReworkCountEventData.setWaferID(waferInfo.getWaferID().getValue());
            if (waferReworkCountEventData.getReworkCount() > maxReworkCount) {
                maxReworkCount = waferReworkCountEventData.getReworkCount();
            }
            reworkWafers.add(waferReworkCountEventData);
        }
        anEventRecord.setReworkCount(maxReworkCount);
        CimProcessOperation aPrevProcessOperation = aPosProcessFlowContext.getPreviousProcessOperation();
        anEventRecord.setPreviousOperationPassCount(0L);
        if (aPrevProcessOperation != null) {
            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevProcessOperation.getEventData();
            anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
            anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount((long) prevPOEventData.getOperationPassCount());
            anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
            anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
        }
        String passCountWaferLevel = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_CONTROL.getValue();
        String passCountWaferLevelEventCreation = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_EVENT_CREATE.getValue();
        if (!CimStringUtils.equals(passCountWaferLevel, "0")
                && CimStringUtils.equals(passCountWaferLevelEventCreation, "1")) {
            String currentKey_var = anEventRecord.getLotData().getRouteID() + "." + anEventRecord.getLotData().getOperationNumber();
            String prevKey_var = anEventRecord.getOldCurrentPOData().getRouteID() + "." + anEventRecord.getOldCurrentPOData().getOperationNumber();
            List<ProductDTO.WaferInfo> waferInfoList = aChildLot.getAllWaferInfo();
            Validations.check(CimObjectUtils.isEmpty(waferInfoList), retCodeConfig.getProductCountZero());

            List<Event.WaferPassCountEventData> processWafers = new ArrayList<>();
            anEventRecord.setProcessWafers(processWafers);
            for (ProductDTO.WaferInfo waferInfo : waferInfoList) {
                CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, waferInfo.getWaferID());
                Event.WaferPassCountEventData processWafer = new Event.WaferPassCountEventData();
                processWafer.setPreviousPassCount(aPosWafer.getPassCount(prevKey_var).intValue());
                processWafer.setPassCount(aPosWafer.getPassCount(currentKey_var).intValue());
                processWafer.setWaferID(waferInfo.getWaferID().getValue());
            }
        }
        anEventRecord.setReasonCodeID(lotPartialReworkCancelEventMakeParams.getReasonCodeID());
        anEventRecord.setEventCommon(setEventData(objCommon, lotPartialReworkCancelEventMakeParams.getClaimMemo()));
        eventManager.createEvent(anEventRecord, CimLotReworkEvent.class);
    }

    @Override
    public void lotFutureHoldEventMake(Infos.ObjCommon objCommon, Inputs.LotFutureHoldEventMakeParams lotFutureHoldEventMakeParams) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Event.LotFutureHoldEventRecord alotFutureHoldEventData = new Event.LotFutureHoldEventRecord();
        /*---------------------------------------*/
        /*   Set Lot Future Hold Event Data   */
        /*---------------------------------------*/
        alotFutureHoldEventData.setLotID(lotFutureHoldEventMakeParams.getLotID().getValue());
        if (CimStringUtils.equals(lotFutureHoldEventMakeParams.getEntryType(), BizConstant.SP_ENTRYTYPE_REMOVE)
                || CimStringUtils.equals(lotFutureHoldEventMakeParams.getEntryType(), BizConstant.SP_ENTRYTYPE_ENTRY)
                || CimStringUtils.equals(lotFutureHoldEventMakeParams.getEntryType(), BizConstant.SP_ENTRYTYPE_CANCEL)) {
            alotFutureHoldEventData.setEntryType(lotFutureHoldEventMakeParams.getEntryType());
        } else {
            alotFutureHoldEventData.setEntryType(BizConstant.SP_ENTRYTYPE_CANCEL);
        }
        Infos.FutureHoldHistory futureHoldHistory = lotFutureHoldEventMakeParams.getFutureHoldHistory();
        alotFutureHoldEventData.setHoldType(futureHoldHistory.getHoldType());
        alotFutureHoldEventData.setRegisterReasonCode(futureHoldHistory.getReasonCode().getValue());
        alotFutureHoldEventData.setRegisterPerson(futureHoldHistory.getPerson().getValue());
        alotFutureHoldEventData.setRouteID(futureHoldHistory.getRouteID().getValue());
        alotFutureHoldEventData.setOpeNo(futureHoldHistory.getOperationNumber());
        alotFutureHoldEventData.setPostFlag(futureHoldHistory.getPostFlag());
        alotFutureHoldEventData.setSingleTriggerFlag(futureHoldHistory.getSingleTriggerFlag());
        alotFutureHoldEventData.setRelatedLotID(futureHoldHistory.getRelatedLotID() == null ? null :
                futureHoldHistory.getRelatedLotID().getValue());
        if (CimStringUtils.equals(lotFutureHoldEventMakeParams.getEntryType(), BizConstant.SP_ENTRYTYPE_CANCEL)) {
            alotFutureHoldEventData.setReleaseReasonCode(lotFutureHoldEventMakeParams.getReleaseReasonCode().getValue());
        } else {
            alotFutureHoldEventData.setReleaseReasonCode("");
        }
        alotFutureHoldEventData.setEventCommon(setEventData(objCommon, ""));
        if (!CimObjectUtils.isEmpty(futureHoldHistory.getClaimMemo())) {
            alotFutureHoldEventData.getEventCommon().setEventMemo(futureHoldHistory.getClaimMemo());
        } else {
            alotFutureHoldEventData.getEventCommon().setEventMemo(lotFutureHoldEventMakeParams.getClaimMemo());
        }
        /*--------------------*/
        /*   Put Event Data   */
        /*--------------------*/
        eventManager.createEvent(alotFutureHoldEventData, CimLotFutureHoldEvent.class);
    }

    @Override
    public void lotBankMoveEventMake(Infos.ObjCommon objCommon, Inputs.LotBankMoveEventMakeParams lotBankMoveEventMakeParams) {
        Event.LotBankMoveEventRecord anEventRecord = new Event.LotBankMoveEventRecord();
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotBankMoveEventMakeParams.getLotID());
        Validations.check(null== aLot, new OmCode(retCodeConfig.getNotFoundLot(), lotBankMoveEventMakeParams.getLotID().getValue()));

        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotData(lotEventData);
        CimBank aPrevBank = aLot.getPreviousBank();
        if (aPrevBank != null) {
            anEventRecord.setPreviousBankID(aPrevBank.getIdentifier());
        }
        anEventRecord.setEventCommon(setEventData(objCommon, lotBankMoveEventMakeParams.getClaimMemo()));

        eventManager.createEvent(anEventRecord, CimLotBankMoveEvent.class);
    }

    @Override
    public void lotOperationMoveEventMakeBranch(Infos.ObjCommon objCommon, Inputs.LotOperationMoveEventMakeBranchParams params) {
        Event.LotOperationMoveEventRecord anEventRecord = new Event.LotOperationMoveEventRecord();
        anEventRecord.setTestCriteriaResult(true);
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, params.getLotID());
        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), params.getLotID().getValue()));

        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotData(lotEventData);
        CimProcessFlowContext aPosProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(aPosProcessFlowContext == null, retCodeConfig.getNotFoundPfx());

        CimProcessOperation aPrevProcessOperation = aPosProcessFlowContext.getPreviousProcessOperation();
        anEventRecord.setPreviousOperationPassCount(0);
        if (aPrevProcessOperation != null) {
            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevProcessOperation.getEventData();
            anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
            anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount(prevPOEventData.getOperationPassCount());
            anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
            anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
        }
        anEventRecord.setLogicalRecipeID("");
        anEventRecord.setMachineRecipeID("");
        anEventRecord.setPhysicalRecipeID("");
        anEventRecord.setEquipmentID("");
        anEventRecord.setOperationMode("");
        anEventRecord.setBatchID("");
        anEventRecord.setControlJobID("");
        anEventRecord.setLocateBackFlag(false);
        Event.ProcessOperationEventData oldCurrentPOData = new Event.ProcessOperationEventData();
        anEventRecord.setOldCurrentPOData(oldCurrentPOData);
        BeanUtils.copyProperties(params.getOldCurrentPOData(), oldCurrentPOData);
        String passCountWaferLevel = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_CONTROL.getValue();
        String passCountWaferLevelEventCreation = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_EVENT_CREATE.getValue();
        if (!CimStringUtils.equals(passCountWaferLevel, "0")
                && CimStringUtils.equals(passCountWaferLevelEventCreation, "1")) {
            String key_var = lotEventData.getRouteID() + "." + lotEventData.getOperationNumber();
            String prevKey_var = params.getOldCurrentPOData().getRouteID() + "." + params.getOldCurrentPOData().getOperationNumber();
            List<ProductDTO.WaferInfo> waferInfoList = aLot.getAllWaferInfo();
            Validations.check(CimObjectUtils.isEmpty(waferInfoList), retCodeConfig.getProductCountZero());

            List<Event.WaferPassCountEventData> processWaferList = new ArrayList<>();
            anEventRecord.setProcessWafers(processWaferList);
            for (ProductDTO.WaferInfo waferInfo : waferInfoList) {
                Event.WaferPassCountEventData processWafer = new Event.WaferPassCountEventData();
                CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, waferInfo.getWaferID());
                processWafer.setPreviousPassCount(aPosWafer.getPassCount(prevKey_var).intValue());
                processWafer.setPassCount(aPosWafer.getPassCount(key_var).intValue());
                processWafer.setWaferID(waferInfo.getWaferID().getValue());
                processWaferList.add(processWafer);
            }
        }
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        eventManager.createEvent(anEventRecord, CimLotOperationMoveEvent.class);
    }

    @Override
    public void lotOperationMoveEventMakeChangeRoute(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier lotID, Inputs.OldCurrentPOData oldCurrentPOData, String claimMemo) {
        Event.LotOperationMoveEventRecord anEventRecord = new Event.LotOperationMoveEventRecord();
        anEventRecord.setTestCriteriaResult(true);
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotData(lotEventData);
        anEventRecord.setEquipmentID("");
        anEventRecord.setOperationMode("");
        anEventRecord.setLogicalRecipeID("");
        anEventRecord.setMachineRecipeID("");
        anEventRecord.setPhysicalRecipeID("");
        CimProcessFlowContext aPosProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(aPosProcessFlowContext == null, retCodeConfig.getNotFoundPfx());

        CimProcessOperation aPrevProcessOperation = aPosProcessFlowContext.getPreviousProcessOperation();

        anEventRecord.setPreviousOperationPassCount(0);
        if (aPrevProcessOperation != null) {
            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevProcessOperation.getEventData();
            anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
            anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount(prevPOEventData.getOperationPassCount());
            anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
            anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
        }
        Event.ProcessOperationEventData processOperationEventData = new Event.ProcessOperationEventData();
        if (oldCurrentPOData != null) {
            BeanUtils.copyProperties(oldCurrentPOData, processOperationEventData);
        } else {
            oldCurrentPOData = new Inputs.OldCurrentPOData();
        }
        anEventRecord.setOldCurrentPOData(processOperationEventData);
        anEventRecord.setBatchID("");
        CimControlJob aControlJob = aLot.getControlJob();
        if (aControlJob != null) {
            anEventRecord.setControlJobID(ObjectIdentifier.fetchValue(aControlJob.getControlJobID()));
        }
        anEventRecord.setLocateBackFlag(false);
        int passCountWaferLevel = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_CONTROL.getIntValue();
        int passCountWaferLevelEventCreation = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_EVENT_CREATE.getIntValue();
        if (passCountWaferLevel != 0 && passCountWaferLevelEventCreation == 1) {
            String key_var = lotEventData.getRouteID() + "." + lotEventData.getOperationNumber();
            String prevKey_var = oldCurrentPOData.getRouteID() + "." + oldCurrentPOData.getOperationNumber();
            List<ProductDTO.WaferInfo> waferInfoList = aLot.getAllWaferInfo();
            Validations.check(CimObjectUtils.isEmpty(waferInfoList), retCodeConfig.getProductCountZero());

            List<Event.WaferPassCountEventData> processWafers = new ArrayList<>();
            anEventRecord.setProcessWafers(processWafers);
            for (ProductDTO.WaferInfo waferInfo : waferInfoList) {
                CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, waferInfo.getWaferID());
                Event.WaferPassCountEventData waferPassCountEventData = new Event.WaferPassCountEventData();
                processWafers.add(waferPassCountEventData);
                waferPassCountEventData.setPreviousPassCount(CimNumberUtils.intValue(aPosWafer.getPassCount(prevKey_var)));
                waferPassCountEventData.setPassCount(CimNumberUtils.intValue(aPosWafer.getPassCount(key_var)));
                waferPassCountEventData.setWaferID(waferInfo.getWaferID().getValue());
            }
        }
        Infos.ObjCommon duplicate = objCommon.duplicate();
        duplicate.setTransactionID(transactionID);
        anEventRecord.setEventCommon(setEventData(duplicate, claimMemo));

        eventManager.createEvent(anEventRecord, CimLotOperationMoveEvent.class);
    }

    @Override
    public void processResourceWaferPositionEventMake(Infos.ObjCommon objCommon, Inputs.ProcessResourceWaferPositionEventMakeParams params) {
        /*-----------------------------*/
        /*  Localize input parameter   */
        /*-----------------------------*/
        String equipmentID = params.getEquipmentID().getValue();
        String controlJobjID = params.getControlJobID().getValue();
        String processResourceID = ObjectIdentifier.fetchValue(params.getProcessResourcePositionInfo().getProcessResourceID());
        /*-------------------------------------------------------*/
        /*   Set Wafer position in Process Resource Event Data   */
        /*-------------------------------------------------------*/
        Event.ProcessResourceWaferPositionEventRecord anEventRecord = new Event.ProcessResourceWaferPositionEventRecord();
        /*===== equipmentID, controlJobID, processResourceID =====*/
        anEventRecord.setEquipmentID(equipmentID);
        anEventRecord.setControlJobID(controlJobjID);
        anEventRecord.setProcessResourceID(processResourceID);

        /*===== set Event Common Data =====*/
        Event.EventData eventCommon = setEventData(objCommon, params.getClaimMemo());
        anEventRecord.setEventCommon(eventCommon);
        List<Infos.WaferPositionInProcessResourceInfo> waferPositionInProcessResourceInfoList = params.getProcessResourcePositionInfo().getWaferPositionInProcessResourceInfoList();
        if (!CimObjectUtils.isEmpty(waferPositionInProcessResourceInfoList)) {
            long i = 0;
            for (Infos.WaferPositionInProcessResourceInfo waferPositionInProcessResourceInfo : waferPositionInProcessResourceInfoList) {
                /*--------------------*/
                /*   Get Lot Object   */
                /*--------------------*/
                ObjectIdentifier lotID = waferPositionInProcessResourceInfo.getLotID();
                CimLot aPosLot = baseCoreFactory.getBO(CimLot.class, lotID);
                Validations.check(aPosLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(lotID)));

                /*-----------------------*/
                /*   Get Lot Event Data  */
                /*-----------------------*/
                Event.LotEventData lotEventData = aPosLot.getEventData();
                anEventRecord.setLotID(lotEventData.getLotID());
                anEventRecord.setMainPDID(lotEventData.getRouteID());
                anEventRecord.setOpeNo(lotEventData.getOperationNumber());
                anEventRecord.setOpePassCount(lotEventData.getOperationPassCount());
                anEventRecord.setWaferID(waferPositionInProcessResourceInfo.getWaferID().getValue());
                anEventRecord.setWaferPosition(waferPositionInProcessResourceInfo.getPosition());
                anEventRecord.setProcessTime(waferPositionInProcessResourceInfo.getProcessReportedTimeStamp());
                eventCommon.setEventTimeStamp(eventCommon.getEventTimeStamp());

                /*--------------------*/
                /*   Put Event Data   */
                /*--------------------*/
                eventManager.createEvent(anEventRecord, CimProcessResourceWaferPositionEvent.class);
            }
        }

    }

    @Override
    public void lotOperationMoveEventMakeLocate(Infos.ObjCommon objCommon, Inputs.LotOperationMoveEventMakeLocateParams params) {
        RetCode<Object> result = new RetCode<>();
        result.setReturnCode(retCodeConfig.getSucc());
        Event.LotOperationMoveEventRecord anEventRecord = new Event.LotOperationMoveEventRecord();
        anEventRecord.setTestCriteriaResult(true);
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, params.getALotID());
        Validations.check(null == aLot, retCodeConfig.getNotFoundLot());

        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotData(lotEventData);
        CimProcessFlowContext aPosProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(aPosProcessFlowContext == null, retCodeConfig.getNotFoundPfx());

        CimProcessOperation aPrevProcessOperation = aPosProcessFlowContext.getPreviousProcessOperation();
        anEventRecord.setPreviousOperationPassCount(0);
        if (aPrevProcessOperation != null) {
            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevProcessOperation.getEventData();
            anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
            anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount(prevPOEventData.getOperationPassCount());
            anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
            anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
        }
        anEventRecord.setLogicalRecipeID("");
        anEventRecord.setMachineRecipeID("");
        anEventRecord.setPhysicalRecipeID("");
        anEventRecord.setEquipmentID("");
        anEventRecord.setOperationMode("");
        anEventRecord.setBatchID("");
        anEventRecord.setControlJobID("");
        anEventRecord.setLocateBackFlag(!params.getLotcateDirection());
        Event.ProcessOperationEventData oldCurrentPOData = new Event.ProcessOperationEventData();
        BeanUtils.copyProperties(params.getOldCurrentPOData(), oldCurrentPOData);
        anEventRecord.setOldCurrentPOData(oldCurrentPOData);
        String passCountWaferLevel = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_CONTROL.getValue();
        String passCountWaferLevelEventCreation = StandardProperties.OM_WAFER_LEVEL_PASSCOUNT_EVENT_CREATE.getValue();
        if (!CimStringUtils.equals(passCountWaferLevel, "0")
                && CimStringUtils.equals(passCountWaferLevelEventCreation, "1")) {
            String key_var = lotEventData.getRouteID() + "." + lotEventData.getOperationNumber();
            String prevKey_var = params.getOldCurrentPOData().getRouteID() + "." + params.getOldCurrentPOData().getOperationNumber();
            List<ProductDTO.WaferInfo> waferInfoList = aLot.getAllWaferInfo();
            Validations.check(CimObjectUtils.isEmpty(waferInfoList), retCodeConfig.getProductCountZero());

            List<Event.WaferPassCountEventData> waferPassCountEventDataList = new ArrayList<>();
            anEventRecord.setProcessWafers(waferPassCountEventDataList);
            for (ProductDTO.WaferInfo waferInfo : waferInfoList) {
                Event.WaferPassCountEventData processWafer = new Event.WaferPassCountEventData();
                CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, waferInfo.getWaferID());
                processWafer.setPreviousPassCount(aPosWafer.getPassCount(prevKey_var).intValue());
                processWafer.setPassCount(aPosWafer.getPassCount(key_var).intValue());
                processWafer.setWaferID(waferInfo.getWaferID().getValue());
                waferPassCountEventDataList.add(processWafer);
            }
        }
        // -----------------------------------------------------------------------------------------------------------
        // Doc calls skip to save logs using the txID of the skip interface                                           |
        // rather than the TxID  of the moveOut interface,                                                            |
        // which would cause lotOperation History to fail to match the txID when generating a category,               |
        // making the category null                                                                                   |
        // -----------------------------------------------------------------------------------------------------------
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        if (CimStringUtils.isNotEmpty(params.getTransactionID())) {
            anEventRecord.getEventCommon().setTransactionID(params.getTransactionID());
        }

        eventManager.createEvent(anEventRecord, CimLotOperationMoveEvent.class);
    }

    @Override
    public Infos.NewLotAttributes lotWaferMoveEventMakeMerge(Infos.ObjCommon objCommon, Inputs.LotWaferMoveEventMakeMergeParams params) {
        Boolean bInCast = true;
        CimLot aSourceLot = baseCoreFactory.getBO(CimLot.class, params.getSourceLotID());
        Validations.check(null == aSourceLot, new OmCode(retCodeConfig.getNotFoundLot(), params.getSourceLotID().getValue()));

        List<Material> aWafers = aSourceLot.allMaterial();
        //-------------------------------------
        // Prepare input parameter of lotWaferMoveEvent_Make
        // for each wafer
        //-------------------------------------
        ObjectIdentifier lotCassetteResult = null;
        try {
            lotCassetteResult = lotMethod.lotCassetteGet(objCommon, params.getDestinationLotID());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {
                bInCast = false;
            } else {
                throw e;
            }

        }
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        if (CimBooleanUtils.isFalse(bInCast)) {
            newLotAttributes.setCassetteID(new ObjectIdentifier(""));
        } else {
            newLotAttributes.setCassetteID(lotCassetteResult);
        }
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        if (!CimObjectUtils.isEmpty(aWafers)) {
            for (Material materialDO : aWafers) {
                Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
                newWaferAttributes.setSourceWaferID(new ObjectIdentifier(materialDO.getIdentifier(), materialDO.getPrimaryKey()));
                newWaferAttributes.setNewWaferID(newWaferAttributes.getSourceWaferID());
                newWaferAttributes.setNewLotID(params.getDestinationLotID());
                if (CimBooleanUtils.isTrue(bInCast)) {
                    newWaferAttributes.setNewSlotNumber(materialDO.getPosition());
                } else {
                    newWaferAttributes.setNewSlotNumber(0);
                }
                newWaferAttributes.setSourceLotID(params.getSourceLotID());
                newWaferAttributesList.add(newWaferAttributes);
            }
        }
        return newLotAttributes;
    }

    @Override
    public void lotWaferScrapEventMake(Infos.ObjCommon objCommon, Inputs.LotWaferScrapEventMakeParams params) {
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, params.getLotID());
        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), params.getLotID().getValue()));

        Event.LotWaferScrapEventRecord anEventRecord = new Event.LotWaferScrapEventRecord();
        // Set Event Record data
        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotData(lotEventData);
        List<Material> aMaterialList = aLot.allMaterial();
        List<Event.WaferEventData> currentWafersList = new ArrayList<>();
        anEventRecord.setCurrentWafers(currentWafersList);
        if (!CimObjectUtils.isEmpty(aMaterialList)) {
            for (Material materialDO : aMaterialList) {
                CimWafer aWafer = (CimWafer) materialDO;
                Event.WaferEventData waferEventData = new Event.WaferEventData();
                waferEventData.setWaferID(aWafer.getIdentifier());
                waferEventData.setOriginalWaferID(waferEventData.getWaferID());
                waferEventData.setControlWaferFlag(aWafer.isControlWafer());
                waferEventData.setOriginalSlotNumber(Long.valueOf(aWafer.getPosition()));
                waferEventData.setDestinationSlotNumber(waferEventData.getOriginalSlotNumber());
                if (waferEventData.getOriginalSlotNumber() != 0) {
                    waferEventData.setOriginalCassetteID(params.getCassetteID().getValue());
                    waferEventData.setDestinationCassetteID(params.getCassetteID().getValue());
                } else {
                    waferEventData.setOriginalCassetteID("");
                    waferEventData.setDestinationCassetteID("");
                }
                currentWafersList.add(waferEventData);
            }
        }
        List<Infos.ScrapWafers> scrapWafersList = params.getScrapWafers();
        List<Event.WaferScrapEventData> waferScrapEventDataList = new ArrayList<>();
        anEventRecord.setScrapWafers(waferScrapEventDataList);
        if (!CimObjectUtils.isEmpty(scrapWafersList)) {
            for (Infos.ScrapWafers scrapWafers : scrapWafersList) {
                Event.WaferScrapEventData waferScrapEventData = new Event.WaferScrapEventData();
                CimWafer aPosWafer = null;
                ObjectIdentifier waferID = scrapWafers.getWaferID();
                if (!ObjectIdentifier.isEmpty(waferID)) {
                    aPosWafer = (CimWafer) newProductManager.findProductNamed(ObjectIdentifier.fetchValue(scrapWafers.getWaferID()));
                }
                if (aPosWafer != null) {
                    waferScrapEventData.setWaferID(scrapWafers.getWaferID().getValue());
                    waferScrapEventData.setControlWaferFlag(aPosWafer.isControlWafer());
                } else {
                    waferScrapEventData.setWaferID("");
                    waferScrapEventData.setControlWaferFlag(false);
                }
                waferScrapEventData.setReasonCodeID(scrapWafers.getReasonCodeID());
                waferScrapEventDataList.add(waferScrapEventData);
            }
        }
        anEventRecord.setReasonRouteID(params.getReasonRouteID().getValue());
        anEventRecord.setReasonOperationID(ObjectIdentifier.fetchValue(params.getReasonOperationID()));
        anEventRecord.setReasonOperationNumber(params.getReasonOperationNumber());
        if (!CimObjectUtils.isEmpty(params.getReasonOperationPass())) {
            anEventRecord.setReasonOperationPassCount(Long.parseLong(params.getReasonOperationPass()));
        } else {
            anEventRecord.setReasonOperationPassCount(anEventRecord.getLotData().getOperationPassCount());
        }
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        eventManager.createEvent(anEventRecord, CimLotWaferScrapEvent.class);
    }

    @Override
    public void lotTerminateEventMake(Infos.ObjCommon objCommon, Event.LotEventData eventData,
                                      TerminateReq.TerminateEventMakeParams params) {
        Event.LotTerminateEventRecord eventRecord = new Event.LotTerminateEventRecord();
        eventRecord.setLotData(eventData);
        eventRecord.setReasonCodeID(params.getReasonCodeID());
        eventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        eventManager.createEvent(eventRecord, CimLotTerminateEvent.class);
    }

    @Override
    public void waferChamberProcessEventMake(Infos.ObjCommon objCommon, Inputs.WaferChamberProcessEventMakeParams params) {
        /*------------------------------------------*/
        /*   Set Wafer Chamber Process Event Data   */
        /*------------------------------------------*/
        Event.WaferChamberProcessEventRecord anEventRecord = new Event.WaferChamberProcessEventRecord();
        anEventRecord.setEquipmentID(params.getEquipmentID().getValue());
        anEventRecord.setActionCode(params.getChamberProcessLotInfos().get(0).getActionCode());

        /*===== set Event Common Data =====*/
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));

        List<Infos.ChamberProcessLotInfo> chamberProcessLotInfos = params.getChamberProcessLotInfos();
        if (!CimObjectUtils.isEmpty(chamberProcessLotInfos)) {
            for (Infos.ChamberProcessLotInfo chamberProcessLotInfo : chamberProcessLotInfos) {
                /*===== get PosLot =====*/
                CimLot aLot = baseCoreFactory.getBO(CimLot.class, chamberProcessLotInfo.getLotID());
                Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), chamberProcessLotInfo.getLotID().getValue()));

                Event.LotEventData lotEventData = aLot.getEventData();
                /*===== set Lot Event Data =====*/
                anEventRecord.setLotID(lotEventData.getLotID());
                ObjectIdentifier lotControlJobIDGet = lotMethod.lotControlJobIDGet(objCommon, chamberProcessLotInfo.getLotID());
                anEventRecord.setControlJobID(ObjectIdentifier.fetchValue(lotControlJobIDGet));
                anEventRecord.setRouteID(lotEventData.getRouteID());
                anEventRecord.setOpeNo(lotEventData.getOperationNumber());
                anEventRecord.setPassCount(lotEventData.getOperationPassCount());
                List<Infos.ChamberProcessWaferInfo> strChamberProcessWaferInfos = chamberProcessLotInfo.getChamberProcessWaferInfos();
                if (!CimObjectUtils.isEmpty(strChamberProcessWaferInfos)) {
                    for (Infos.ChamberProcessWaferInfo chamberProcessWaferInfo : strChamberProcessWaferInfos) {
                        /*===== waferID =====*/
                        anEventRecord.setWaferID(chamberProcessWaferInfo.getWaferID().getValue());
                        List<Infos.ProcessedChamberInfo> strProcessedChamberInfos = chamberProcessWaferInfo.getProcessedChamberInfos();
                        if (!CimObjectUtils.isEmpty(chamberProcessWaferInfo)) {
                            for (Infos.ProcessedChamberInfo processedChamberInfo : strProcessedChamberInfos) {
                                /*===== chamberID =====*/
                                anEventRecord.setProcessResourceID(processedChamberInfo.getChamberID().getValue());
                                /*===== processReportedTimeStamp =====*/
                                anEventRecord.setProcessTime(processedChamberInfo.getProcessReportedTimeStamp());
                                /*--------------------*/
                                /*   Put Event Data   */
                                /*--------------------*/
                                eventManager.createEvent(anEventRecord, CimWaferChamberProcessEvent.class);

                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void productRequestEventMakeRelease(Infos.ObjCommon objCommon, Inputs.ProductRequestEventMakeReleaseParams params) {
        Event.ProductRequestEventRecord anEventRecord = new Event.ProductRequestEventRecord();
        anEventRecord.setLotID(params.getReleaseLotAttributes().getLotID().getValue());
        anEventRecord.setLotType(params.getReleaseLotAttributes().getLotType());
        anEventRecord.setSubLotType(params.getReleaseLotAttributes().getSubLotType());
        anEventRecord.setRouteID(params.getReleaseLotAttributes().getRouteID().getValue());
        anEventRecord.setProductQuantity(params.getReleaseLotAttributes().getProductQuantity().longValue());
        anEventRecord.setPlanStartTime(params.getReleaseLotAttributes().getPlannedStartTime());
        anEventRecord.setPlanCompTime(params.getReleaseLotAttributes().getPlannedFinishTime());
        anEventRecord.setLotGenerationType(params.getReleaseLotAttributes().getLotGenerationType());
        anEventRecord.setLotScheduleMode(params.getReleaseLotAttributes().getSchedulingMode());
        anEventRecord.setLotIDGenerationMode(params.getReleaseLotAttributes().getLotIDGenerationMode());
        anEventRecord.setProductDefinitionMode(params.getReleaseLotAttributes().getProductDefinitionMode());
        anEventRecord.setExternalPriority(Long.parseLong(params.getReleaseLotAttributes().getExternalPriority()));
        anEventRecord.setPriorityClass(Long.parseLong(params.getReleaseLotAttributes().getPriorityClass()));
        anEventRecord.setProductID(params.getReleaseLotAttributes().getProductID().getValue());
        anEventRecord.setLotOwnerID(params.getReleaseLotAttributes().getLotOwner());
        anEventRecord.setOrderNumber(params.getReleaseLotAttributes().getManufacturingOrderNumber());
        anEventRecord.setCustomerID(params.getReleaseLotAttributes().getCustomerCode());
        anEventRecord.setLotComment(params.getReleaseLotAttributes().getLotComment());

        // Set Event Common data
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));

        // Put into the queue of PosEventManager
        eventManager.createEvent(anEventRecord, CimProductRequestEvent.class);
    }

    @Override
    public void productRequestEventMakeReleaseCancel(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier lotID, String claimMemo) {
        log.info("【Method Entry】productRequestEventMakeReleaseCancel");
        // Get object reference of product request(= lot)
        CimProductRequest aPosProductRequest = newPlanManager.findProductRequestNamed(lotID.getValue());
        Validations.check(aPosProductRequest == null, new OmCode(retCodeConfig.getNotFoundProductRequest(), ""));

        Event.ProductRequestEventRecord anEventRecord = new Event.ProductRequestEventRecord();
        anEventRecord.setLotID(lotID.getValue());
        anEventRecord.setLotType(aPosProductRequest.getLotType());
        anEventRecord.setSubLotType(aPosProductRequest.getSubLotType());

        CimProcessDefinition aPosProcessDefinition = aPosProductRequest.getMainProcessDefinition();
        anEventRecord.setRouteID(aPosProcessDefinition.getIdentifier());
        anEventRecord.setProductQuantity(CimNumberUtils.longValue(aPosProductRequest.getProductQuantity()));
        anEventRecord.setPlanStartTime(CimDateUtils.getTimestampAsString(aPosProductRequest.getPlanReleaseDateTime()));
        anEventRecord.setPlanCompTime(CimDateUtils.getTimestampAsString(aPosProductRequest.getDeliveryDateTime()));
        anEventRecord.setLotGenerationType(aPosProductRequest.getLotGenerationType());
        anEventRecord.setLotScheduleMode(aPosProductRequest.getScheduleMode());
        anEventRecord.setLotIDGenerationMode(aPosProductRequest.getLotIdGenerationMode());
        anEventRecord.setProductDefinitionMode(aPosProductRequest.getProductMode());
        anEventRecord.setExternalPriority(CimNumberUtils.longValue(aPosProductRequest.getSchedulePriority()));
        anEventRecord.setPriorityClass(aPosProductRequest.getPriorityClass());

        ProductSpecification aProductSpecification = aPosProductRequest.getProductSpecification();
        Validations.check(aProductSpecification == null, retCodeConfig.getNotFoundProductSpec());

        anEventRecord.setProductID(aProductSpecification.getIdentifier());

        CimPerson aPosPerson = aPosProductRequest.getLotOwner();
        if (aPosPerson != null) {
            anEventRecord.setLotOwnerID(aPosPerson.getIdentifier());
        } else {
            anEventRecord.setLotOwnerID("");
        }

        anEventRecord.setOrderNumber(aPosProductRequest.getOrderNumber());
        CimCustomer aCustomer = aPosProductRequest.getCustomer();
        Validations.check(null == aCustomer, retCodeConfigEx.getNotFoundCustomer());

        anEventRecord.setCustomerID(aCustomer.getIdentifier());
        anEventRecord.setLotComment(aPosProductRequest.getLotComment());

        anEventRecord.setEventCommon(setEventData(objCommon, claimMemo));
        eventManager.createEvent(anEventRecord, CimProductRequestEvent.class);

    }

    @Override
    public void productRequestEventMakeUpdate(Infos.ObjCommon objCommon, Inputs.ProductRequestEventMakeUpdateParams params) {
        Event.ProductRequestEventRecord anEventRecord = new Event.ProductRequestEventRecord();
        /*------------------------------------------------------------------------*/
        /*   Get object reference of product request(= lot)                       */
        /*------------------------------------------------------------------------*/
        CimProductRequest aPosProductRequest = baseCoreFactory.getBO(CimProductRequest.class, params.getUpdateLotAttributes().getLotID());
        /*------------------------------------------------------------------------*/
        /*   Set Event Record data from input parameter                           */
        /*------------------------------------------------------------------------*/
        Infos.UpdateLotAttributes updateLotAttributes = params.getUpdateLotAttributes();
        anEventRecord.setLotID(updateLotAttributes.getLotID().getValue());
        anEventRecord.setSubLotType(updateLotAttributes.getSubLotType());
        anEventRecord.setRouteID(updateLotAttributes.getRouteID().getValue());
        anEventRecord.setProductQuantity(updateLotAttributes.getProductQuantity().longValue());
        anEventRecord.setPlanStartTime(updateLotAttributes.getPlannedStartTime());
        anEventRecord.setPlanCompTime(updateLotAttributes.getPlannedFinishTime());
        anEventRecord.setLotGenerationType(updateLotAttributes.getLotGenerationType());
        anEventRecord.setLotScheduleMode(updateLotAttributes.getSchedulingMode());
        anEventRecord.setProductDefinitionMode(updateLotAttributes.getProductDefinitionMode());
        anEventRecord.setProductID(updateLotAttributes.getProductID().getValue());
        anEventRecord.setLotOwnerID(updateLotAttributes.getLotOwner());
        anEventRecord.setOrderNumber(updateLotAttributes.getManufacturingOrderNumber());
        anEventRecord.setCustomerID(updateLotAttributes.getCustomerCode());
        anEventRecord.setLotComment(updateLotAttributes.getLotComment());
        anEventRecord.setExternalPriority(Long.parseLong(updateLotAttributes.getExternalPriority()));
        anEventRecord.setPriorityClass(Long.parseLong(updateLotAttributes.getPriorityClass()));

        /*------------------------------------------------------------------------*/
        /*   Set Event Record data from object                                    */
        /*------------------------------------------------------------------------*/
        anEventRecord.setLotType(aPosProductRequest.getLotType());
        anEventRecord.setLotIDGenerationMode(aPosProductRequest.getLotIdGenerationMode());

        /*------------------------------------------------------------------------*/
        /*   Set Event Common data                                                */
        /*------------------------------------------------------------------------*/
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        /*------------------------------------------------------------------------*/
        /*   Put into the queue of PosEventManager                                */
        /*------------------------------------------------------------------------*/
        eventManager.createEvent(anEventRecord, CimProductRequestEvent.class);
    }

    @Override
    public void vendorLotEventMake(Infos.ObjCommon objCommon, Inputs.VendorLotEventMakeParams params) {
        Event.VendorLotEventRecord anEventRecord = new Event.VendorLotEventRecord();
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, params.getLotID());
        Validations.check(CimObjectUtils.isEmpty(aLot),retCodeConfig.getNotFoundLot());
        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotDate(lotEventData);
        anEventRecord.setVendorLotID(params.getVendorLotID());
        anEventRecord.setClaimQuantity(params.getClaimQuantity().longValue());

        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));

        // Put into the queue of PosEventManager
        eventManager.createEvent(anEventRecord, CimVendorLotEvent.class);
    }

    @Override
    public void equipmentBufferResourceTypeChangeEventMake(Infos.ObjCommon objCommon, Inputs.EquipmentBufferResourceTypeChangeEventMakeParams params) {
        //--------------------------------------------------
        //   Get Machine Object & state code
        //--------------------------------------------------
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, params.getEquipmentID());
        CimMachineState aPosMachineState = aPosMachine.getCurrentMachineState();
        Validations.check(aPosMachineState == null, retCodeConfig.getNotFoundEqpState());

        CimE10State aPosE10State = aPosMachineState.getE10State();
        Validations.check(aPosE10State == null, retCodeConfig.getNotFoundE10State());

        ObjectIdentifier equipmentState = new ObjectIdentifier(aPosMachineState.getIdentifier(), aPosMachineState.getPrimaryKey());
        ObjectIdentifier E10State = new ObjectIdentifier(aPosE10State.getIdentifier(), aPosE10State.getPrimaryKey());
        //--------------------------------------------------
        // Check capacity of each buffer category
        //--------------------------------------------------
        List<Infos.BufferResourceUpdateInfo> tmpBufferResourceUpdateInfoList = params.getBufferResourceUpdateInfoList();
        if (!CimObjectUtils.isEmpty(tmpBufferResourceUpdateInfoList)) {
            for (Infos.BufferResourceUpdateInfo bufferResourceUpdateInfo : tmpBufferResourceUpdateInfoList) {
                if (CimStringUtils.equals(bufferResourceUpdateInfo.getBufferCategory(), SP_BUFFERCATEGORY_ANYPROCESSLOT)) {
                    continue;
                }
                Long newCapacity = bufferResourceUpdateInfo.getNewCapacity();
                Long smCapacity = bufferResourceUpdateInfo.getSmCapacity();
                Long dynCapacity = bufferResourceUpdateInfo.getDynamicCapacity();
                if (newCapacity.equals(dynCapacity)) {
                    continue;
                }
                //--------------------------------------------------
                // Create event for this buffer category
                //--------------------------------------------------
                Event.DynamicBufferResourceChangeEventRecord buffRscChgEvtData = new Event.DynamicBufferResourceChangeEventRecord();
                buffRscChgEvtData.setEquipmentID(params.getEquipmentID().getValue());
                buffRscChgEvtData.setEquipmentState(equipmentState.getValue());
                buffRscChgEvtData.setE10State(E10State.getValue());
                buffRscChgEvtData.setBufferCategory(bufferResourceUpdateInfo.getBufferCategory());
                buffRscChgEvtData.setSmCapacity(smCapacity);
                buffRscChgEvtData.setDynamicCapacity(newCapacity);

                buffRscChgEvtData.setEventCommon(setEventData(objCommon, params.getClaimMemo()));

                //--------------------------------------------------
                //   Put Event Data
                //--------------------------------------------------
                eventManager.createEvent(buffRscChgEvtData, CimDynamicBufferResourceChangeEvent.class);

            }
        }
    }

    @Override
    public void equipmentModeChangeEventMake(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.PortOperationMode> portOperationModeList, String claimMemo) {
        if (!CimObjectUtils.isEmpty(portOperationModeList)) {
            for (Infos.PortOperationMode portOperationMode : portOperationModeList) {
                Event.EquipmentModeChangeEventRecord eqpModeChgEvtRecord = new Event.EquipmentModeChangeEventRecord();
                eqpModeChgEvtRecord.setEquipmentID(equipmentID.getValue());
                eqpModeChgEvtRecord.setPortID(portOperationMode.getPortID().getValue());
                eqpModeChgEvtRecord.setOperationMode(portOperationMode.getOperationMode().getOperationMode().getValue());
                eqpModeChgEvtRecord.setOnlineMode(portOperationMode.getOperationMode().getOnlineMode());
                eqpModeChgEvtRecord.setDispatchMode(portOperationMode.getOperationMode().getDispatchMode());
                eqpModeChgEvtRecord.setAccessMode(portOperationMode.getOperationMode().getAccessMode());
                eqpModeChgEvtRecord.setOperationStartMode(portOperationMode.getOperationMode().getMoveInMode());
                eqpModeChgEvtRecord.setOperationCompMode(portOperationMode.getOperationMode().getMoveOutMode());
                eqpModeChgEvtRecord.setDescription(portOperationMode.getOperationMode().getDescription());

                eqpModeChgEvtRecord.setEventCommon(setEventData(objCommon, claimMemo));

                eventManager.createEvent(eqpModeChgEvtRecord, CimEquipmentModeChangeEvent.class);
            }
        }
    }

    @Override
    public void objectNoteEventMake(Infos.ObjCommon objCommon, Inputs.ObjectNoteEventMakeParams params) {
        Event.NoteChangeEventRecord noteChgEvtData = new Event.NoteChangeEventRecord();
        noteChgEvtData.setObjectID(ObjectIdentifier.fetchValue(params.getObjectID()));
        noteChgEvtData.setNoteType(params.getNoteType());
        noteChgEvtData.setAction(params.getAction());
        noteChgEvtData.setRouteID(ObjectIdentifier.fetchValue(params.getRouteID()));
        noteChgEvtData.setOperationID(ObjectIdentifier.fetchValue(params.getOperationID()));
        noteChgEvtData.setOperationNumber(params.getOperationNumber());
        noteChgEvtData.setNoteTitle(params.getNoteTitle());
        noteChgEvtData.setNoteContents(params.getNoteContents());
        noteChgEvtData.setOwnerID(ObjectIdentifier.fetchValue(params.getOwnerID()));

        Event.EventData eventCommon = new Event.EventData();
        if (!CimObjectUtils.isEmpty(params.getTransactionID())) {
            eventCommon.setTransactionID(params.getTransactionID());
        } else {
            eventCommon.setTransactionID(objCommon.getTransactionID());
        }
        eventCommon.setUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        eventCommon.setEventTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        eventCommon.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
        eventCommon.setEventMemo(params.getClaimMemo());
        noteChgEvtData.setEventCommon(eventCommon);
        //--------------------------------------------------
        //   Put Event Data
        //--------------------------------------------------
        eventManager.createEvent(noteChgEvtData, CimNoteChangeEvent.class);
    }

    @Override
    public void equipmentPortStatusChangeEventMake(Infos.ObjCommon objCommon, Inputs.EquipmentPortStatusChangeEventMakeParams params) {
        Event.EqpPortStatusChangeEventRecord anEventRecord = new Event.EqpPortStatusChangeEventRecord();
        anEventRecord.setEquipmentID(ObjectIdentifier.fetchValue(params.getEquipmentID()));
        anEventRecord.setPortType(params.getPortType());
        anEventRecord.setPortID(ObjectIdentifier.fetchValue(params.getPortID()));
        anEventRecord.setPortUsage(params.getPortUsage());
        anEventRecord.setPortStatus(params.getPortStatus());
        anEventRecord.setAccessMode(params.getAccessMode());
        anEventRecord.setDispatchState(params.getDispatchState());
        anEventRecord.setDispatchTime(params.getDispatchTime());
        anEventRecord.setDispatchDurableID(params.getDispatchDurableID());

        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        eventManager.createEvent(anEventRecord, CimEqpPortStatusChangeEvent.class);
    }

    @Override
    public void equipmentStatusChangeEventMake(Infos.ObjCommon objCommon, Inputs.EquipmentStatusChangeEventMakeParams params) {
        Event.EquipmentStatusChangeEventRecord eqpStatChgEvtData = new Event.EquipmentStatusChangeEventRecord();
        eqpStatChgEvtData.setEquipmentID(ObjectIdentifier.fetchValue(params.getEquipmentID()));
        eqpStatChgEvtData.setStockerID(ObjectIdentifier.fetchValue(params.getStockerID()));
        eqpStatChgEvtData.setEquipmentState(ObjectIdentifier.fetchValue(params.getPreviousStatus()));
        eqpStatChgEvtData.setE10State(ObjectIdentifier.fetchValue(params.getPreviousE10Status()));
        eqpStatChgEvtData.setActualEquipmentState(ObjectIdentifier.fetchValue(params.getPreviousActualStatus()));
        eqpStatChgEvtData.setActualE10State(ObjectIdentifier.fetchValue(params.getPreviousActualE10Status()));
        eqpStatChgEvtData.setOperationMode(params.getPreviousOpeMode());
        eqpStatChgEvtData.setStartTimeStamp(params.getPrevStateStartTime());
        eqpStatChgEvtData.setNewEquipmentState(ObjectIdentifier.fetchValue(params.getNewEquipmentStatus()));
        eqpStatChgEvtData.setNewE10State(ObjectIdentifier.fetchValue(params.getNewE10Status()));
        eqpStatChgEvtData.setNewOperationMode(params.getNewOperationMode());
        eqpStatChgEvtData.setReasonCodeID(params.getReasonCodeID());

        eqpStatChgEvtData.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        /*--------------------*/
        /*   Put Event Data   */
        /*--------------------*/
        eventManager.createEvent(eqpStatChgEvtData, CimEquipmentStatusChangeEvent.class);
    }

    @Override
    public void eqpMonitorJobChangeEventMake(Infos.ObjCommon objCommon, Inputs.EqpMonitorJobChangeEventMakeParams params) {
        Event.EqpMonitorJobEventRecord anEventRecord = new Event.EqpMonitorJobEventRecord();
        //--------------------------------//
        //  Get Auto Monitor Object  //
        //--------------------------------//
        CimEqpMonitor anEqpMonitor = baseCoreFactory.getBO(CimEqpMonitor.class, params.getEqpMonitorID());
        CimEqpMonitorJob anEqpMonitorJob = baseCoreFactory.getBO(CimEqpMonitorJob.class, params.getEqpMonitorJobID());
        MachineDTO.EqpMonitorJobInfo anEqpMonitorJobInfo = anEqpMonitorJob.getEqpMonitorJobInfo();
        CimProcessResource aChamber = anEqpMonitorJob.getChamber();
        //-----------------//
        //  Set Event Data //
        //-----------------//
        anEventRecord.setOpeCategory(params.getOpeCategory());
        anEventRecord.setEquipmentID(ObjectIdentifier.fetchValue(anEqpMonitorJobInfo.getEquipmentID()));
        if (aChamber == null) {
            anEventRecord.setChamberID("");
        } else {
            anEventRecord.setChamberID(aChamber.getIdentifier());
        }
        anEventRecord.setEqpMonitorID(params.getEqpMonitorID().getValue());
        anEventRecord.setEqpMonitorJobID(params.getEqpMonitorJobID().getValue());
        anEventRecord.setMonitorJobStatus(anEqpMonitorJobInfo.getMonitorJobStatus());
        anEventRecord.setRetryCount(anEqpMonitorJobInfo.getRetryCount().longValue());
        if (CimStringUtils.equals(params.getOpeCategory(), BizConstant.SP_EQPMONITORJOB_OPECATEGORY_STATUSCHANGE)) {
            anEventRecord.setPrevMonitorJobStatus(params.getPreviousMonitorJobStatus());
        }
        if (!CimObjectUtils.isEmpty(params.getEqpMonitorLotInfoList())) {
            List<Event.EqpMonitorLotEventData> eqpMonitorLotEventDataList = new ArrayList<>();
            anEventRecord.setMonitorLots(eqpMonitorLotEventDataList);
            for (Infos.EqpMonitorLotInfo eqpMonitorLotInfo : params.getEqpMonitorLotInfoList()) {
                Event.EqpMonitorLotEventData eqpMonitorLotEventData = new Event.EqpMonitorLotEventData();
                eqpMonitorLotEventDataList.add(eqpMonitorLotEventData);
                eqpMonitorLotEventData.setLotID(eqpMonitorLotInfo.getLotID().getValue());
                CimProcessOperation aPO = null;
                CimLot aLot = baseCoreFactory.getBO(CimLot.class, eqpMonitorLotInfo.getLotID());
                CimProductSpecification aProductSpecification = aLot.getProductSpecification();
                ObjectIdentifier productID = aProductSpecification.getProductSpecID();
                eqpMonitorLotEventData.setProductSpecificationID(productID.getValue());
                eqpMonitorLotEventData.setStartSeqNo(eqpMonitorLotInfo.getStartSeqNo().longValue());
                if (CimStringUtils.equals(anEventRecord.getOpeCategory(), BizConstant.SP_EQPMONITORJOB_OPECATEGORY_EQPMONSTART)
                        || CimStringUtils.equals(anEventRecord.getOpeCategory(), BizConstant.SP_EQPMONITORJOB_OPECATEGORY_EQPMONCOMP)) {
                    Boolean bPreviousPOFlag = false;
                    if (CimStringUtils.equals(anEventRecord.getOpeCategory(), BizConstant.SP_EQPMONITORJOB_OPECATEGORY_EQPMONSTART)) {
                        log.info("OpeCategory is EQPMonStart");
                        log.info("Get from current PO");
                    } else {
                        if (eqpMonitorLotInfo.getResult() == 1) {
                            bPreviousPOFlag = true;
                        } else if (eqpMonitorLotInfo.getExitFlag()) {
                            bPreviousPOFlag = true;
                        }
                    }
                    if (bPreviousPOFlag) {
                        aPO = aLot.getPreviousProcessOperation();

                    } else {
                        aPO = aLot.getProcessOperation();
                    }
                    Validations.check(null == aPO, retCodeConfig.getNotFoundProcessOperation());

                    CimProcessDefinition aMainPD = aPO.getMainProcessDefinition();
                    String operationNo = aPO.getOperationNumber();
                    eqpMonitorLotEventData.setOpeNo(operationNo);
                    ObjectIdentifier routeID = new ObjectIdentifier(aMainPD.getIdentifier(), aMainPD.getPrimaryKey());
                    eqpMonitorLotEventData.setMainPDID(routeID.getValue());
                    Long nPassCount = aPO.getPassCount();
                    eqpMonitorLotEventData.setOpePassCount(nPassCount);
                    CimProcessDefinition aPD = aPO.getProcessDefinition();
                    Validations.check(null == aPD, retCodeConfig.getNotFoundProcessDefinition());

                    ObjectIdentifier thePDID = new ObjectIdentifier(aPD.getIdentifier(), aPD.getPrimaryKey());
                    eqpMonitorLotEventData.setPdID(thePDID.getValue());
                }
            }
        }
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));

        //-----------------//
        //  Put Event Data //
        //-----------------//
        eventManager.createEvent(anEventRecord, CimEqpMonitorJobEvent.class);
    }

    @Override
    public void eqpMonitorChangeEventMake(Infos.ObjCommon objCommon, Inputs.EqpMonitorChangeEventMakeParams params) {
        Event.EqpMonitorEventRecord anEventRecord = new Event.EqpMonitorEventRecord();
        //--------------------------------//
        //  Get Auto Monitor Object  //
        //--------------------------------//
        CimEqpMonitor anEqpMonitor = baseCoreFactory.getBO(CimEqpMonitor.class, params.getEqpMonitorID());
        // BUG-2024 throw exception or return when not found eqpMonitor entity, use return just for now
        if (null == anEqpMonitor) {
            return;
        }
        MachineDTO.EqpMonitorInfo anEqpMonitorInfo = anEqpMonitor.getEqpMonitorInfo();
        anEventRecord.setOpeCategory(params.getOpeCategory());
        anEventRecord.setEquipmentID(ObjectIdentifier.fetchValue(anEqpMonitorInfo.getMachineID()));
        anEventRecord.setChamberID(ObjectIdentifier.fetchValue(anEqpMonitorInfo.getChamberID()));
        anEventRecord.setEqpMonitorID(ObjectIdentifier.fetchValue(params.getEqpMonitorID()));
        anEventRecord.setMonitorType(anEqpMonitorInfo.getMonitorType());
        anEventRecord.setMonitorStatus(anEqpMonitorInfo.getMonitorStatus());

        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        if (CimStringUtils.equals(params.getOpeCategory(), SP_EQPMONITOR_OPECATEGORY_CREATE)
                || CimStringUtils.equals(params.getOpeCategory(), SP_EQPMONITOR_OPECATEGORY_UPDATE)) {
            List<Event.EqpMonitorDefEventData> monitorDefList = new ArrayList<>();
            anEventRecord.setMonitorDefs(monitorDefList);
            Event.EqpMonitorDefEventData eqpMonitorDefEventData = new Event.EqpMonitorDefEventData();
            monitorDefList.add(eqpMonitorDefEventData);
            eqpMonitorDefEventData.setDescription(anEqpMonitorInfo.getDescription());
            eqpMonitorDefEventData.setScheduleType(anEqpMonitorInfo.getScheduleType());
            eqpMonitorDefEventData.setStartTimeStamp(CimDateUtils.convertToSpecString(anEqpMonitorInfo.getStartTimeStamp()));
            eqpMonitorDefEventData.setExecutionInterval(Long.valueOf(anEqpMonitorInfo.getExecutionInterval()));
            eqpMonitorDefEventData.setWarningInterval(Long.valueOf(anEqpMonitorInfo.getWarningInterval()));
            eqpMonitorDefEventData.setExpirationInterval(Long.valueOf(anEqpMonitorInfo.getExpirationInterval()));
            eqpMonitorDefEventData.setStandAloneFlag(anEqpMonitorInfo.isStandAloneFlag());
            eqpMonitorDefEventData.setKitFlag(anEqpMonitorInfo.isKitFlag());
            eqpMonitorDefEventData.setMaxRetryCount(Long.valueOf(anEqpMonitorInfo.getMaxRetryCount()));
            eqpMonitorDefEventData.setMachineStateAtStart(ObjectIdentifier.fetchValue(anEqpMonitorInfo.getMachineStateAtStart()));
            eqpMonitorDefEventData.setMachineStateAtPassed(ObjectIdentifier.fetchValue(anEqpMonitorInfo.getMachineStateAtPassed()));
            eqpMonitorDefEventData.setMachineStateAtFailed(ObjectIdentifier.fetchValue(anEqpMonitorInfo.getMachineStateAtFailed()));
            List<MachineDTO.EqpMonitorProductSpecificationInfo> eqpMonitorProdSpecs = anEqpMonitorInfo.getEqpMonitorProdSpecs();
            if (!CimObjectUtils.isEmpty(eqpMonitorProdSpecs)) {
                List<Event.EqpMonitorDefprodEventData> monitorDefprodList = new ArrayList<>();
                anEventRecord.setMonitorDefprods(monitorDefprodList);
                for (MachineDTO.EqpMonitorProductSpecificationInfo eqpMonitorProductSpecificationInfo : eqpMonitorProdSpecs) {
                    Event.EqpMonitorDefprodEventData eqpMonitorDefprodEventData = new Event.EqpMonitorDefprodEventData();
                    monitorDefprodList.add(eqpMonitorDefprodEventData);
                    eqpMonitorDefprodEventData.setProductSpecificationID(ObjectIdentifier.fetchValue(eqpMonitorProductSpecificationInfo.getProductSpecificationID()));
                    eqpMonitorDefprodEventData.setWaferCount(Long.valueOf(eqpMonitorProductSpecificationInfo.getWaferCount()));
                    eqpMonitorDefprodEventData.setStartSeqNo(Long.valueOf(eqpMonitorProductSpecificationInfo.getStartSeqNo()));
                }
            }
            List<MachineDTO.EqpMonitorActionInfo> eqpMonitorActions = anEqpMonitorInfo.getEqpMonitorActions();
            if (!CimObjectUtils.isEmpty(eqpMonitorActions)) {
                List<Event.EqpMonitorDefactionEventData> monitorDefactionList = new ArrayList<>();
                anEventRecord.setMonitorDefactions(monitorDefactionList);
                for (MachineDTO.EqpMonitorActionInfo eqpMonitorActionInfo : eqpMonitorActions) {
                    Event.EqpMonitorDefactionEventData eqpMonitorDefactionEventData = new Event.EqpMonitorDefactionEventData();
                    monitorDefactionList.add(eqpMonitorDefactionEventData);
                    eqpMonitorDefactionEventData.setEventType(eqpMonitorActionInfo.getEventType());
                    eqpMonitorDefactionEventData.setAction(eqpMonitorActionInfo.getAction());
                    eqpMonitorDefactionEventData.setReasonCode(ObjectIdentifier.fetchValue(eqpMonitorActionInfo.getReasonCodeID()));
                    eqpMonitorDefactionEventData.setSysMessageID(ObjectIdentifier.fetchValue(eqpMonitorActionInfo.getSysMessageCodeID()));
                    eqpMonitorDefactionEventData.setCustomField(eqpMonitorActionInfo.getCustomField());
                }
            }
        }
        if (CimStringUtils.equals(params.getOpeCategory(), SP_EQPMONITOR_OPECATEGORY_RESET)
                || CimStringUtils.equals(params.getOpeCategory(), SP_EQPMONITOR_OPECATEGORY_SKIP)
                || CimStringUtils.equals(params.getOpeCategory(), SP_EQPMONITOR_OPECATEGORY_POSTPONE)
                || CimStringUtils.equals(params.getOpeCategory(), SP_EQPMONITOR_OPECATEGORY_FORCERUN)) {
            List<Event.EqpMonitorSchchgEventData> monitorSchchgList = new ArrayList<>();
            Event.EqpMonitorSchchgEventData eqpMonitorSchchgEventData = new Event.EqpMonitorSchchgEventData();
            Infos.EqpMonitorNextExecutionTimeCalculateIn eqpMonitorNextExecutionTimeCalculateIn = new Infos.EqpMonitorNextExecutionTimeCalculateIn();
            eqpMonitorNextExecutionTimeCalculateIn.setCurrentScheduleBaseTime(anEqpMonitorInfo.getScheduleBaseTimeStamp());
            eqpMonitorNextExecutionTimeCalculateIn.setExecutionInterval(anEqpMonitorInfo.getExecutionInterval());
            eqpMonitorNextExecutionTimeCalculateIn.setScheduleAdjustment(anEqpMonitorInfo.getScheduleAdjustment());
            eqpMonitorNextExecutionTimeCalculateIn.setLastMonitorPassedTime(null);
            eqpMonitorNextExecutionTimeCalculateIn.setExpirationInterval(anEqpMonitorInfo.getExpirationInterval());
            eqpMonitorNextExecutionTimeCalculateIn.setFutureTimeRequireFlag(false);
            Results.EqpMonitorNextExecutionTimeCalculateResult eqpMonitorNextExecutionTimeCalculateResultRetCode = equipmentMethod.eqpMonitorNextExecutionTimeCalculate(objCommon, eqpMonitorNextExecutionTimeCalculateIn);

            eqpMonitorSchchgEventData.setNextExecutionTime(eqpMonitorNextExecutionTimeCalculateResultRetCode.getNextExecutionTime());
            eqpMonitorSchchgEventData.setPrevNextExecutionTime(params.getPreviousNextExecutionTime());
            monitorSchchgList.add(eqpMonitorSchchgEventData);
            anEventRecord.setMonitorSchchgs(monitorSchchgList);
        }
        if (CimStringUtils.equals(params.getOpeCategory(), SP_EQPMONITORJOB_OPECATEGORY_STATUSCHANGE)) {
            anEventRecord.setPrevMonitorStatus(params.getPreviousMonitorStatus());
        }
        //-----------------//
        //  Put Event Data //
        //-----------------//
        eventManager.createEvent(anEventRecord, CimEqpMonitorEvent.class);
    }

    @Override
    public void chamberStatusChangeEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier equipmentID, List<Infos.EqpChamberState> eqpChamberStateList, String claimMemo) {
        Event.ChamberStatusChangeEventRecord chmbStatChgEvtData = new Event.ChamberStatusChangeEventRecord();
        if (!CimObjectUtils.isEmpty(eqpChamberStateList)) {
            for (Infos.EqpChamberState eqpChamberState : eqpChamberStateList) {
                /*------------------------------------------*/
                /*   Set Chamber Status Change Event Data   */
                /*------------------------------------------*/
                chmbStatChgEvtData.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
                chmbStatChgEvtData.setProcessResourceID(ObjectIdentifier.fetchValue(eqpChamberState.getChamberID()));
                chmbStatChgEvtData.setProcessResourceState(ObjectIdentifier.fetchValue(eqpChamberState.getPreviousStatus()));
                chmbStatChgEvtData.setProcessResourceE10State(ObjectIdentifier.fetchValue(eqpChamberState.getPreviousE10Status()));
                chmbStatChgEvtData.setActualProcessResourceState(ObjectIdentifier.fetchValue(eqpChamberState.getPreviousActualStatus()));
                chmbStatChgEvtData.setActualProcessResourceE10State(ObjectIdentifier.fetchValue(eqpChamberState.getPreviousActualE10Status()));
                chmbStatChgEvtData.setStartTimeStamp(eqpChamberState.getPrevStateStartTime());
                chmbStatChgEvtData.setNewProcessResourceState(ObjectIdentifier.fetchValue(eqpChamberState.getChamberStatusCode()));
                chmbStatChgEvtData.setNewProcessResourceE10State(ObjectIdentifier.fetchValue(eqpChamberState.getChamberE10Status()));
                chmbStatChgEvtData.setNewActualProcessResourceState(ObjectIdentifier.fetchValue(eqpChamberState.getActualStatus()));
                chmbStatChgEvtData.setNewActualProcessResourceE10State(ObjectIdentifier.fetchValue(eqpChamberState.getActualE10Status()));

                chmbStatChgEvtData.setEventCommon(setEventData(objCommon, claimMemo));

                /*--------------------*/
                /*   Put Event Data   */
                /*--------------------*/
                eventManager.createEvent(chmbStatChgEvtData, CimChamberStatusChangeEvent.class);
            }
        }
    }

    @Override
    public void durableXferStatusChangeEventMake(Infos.ObjCommon objCommon, Inputs.DurableXferStatusChangeEventMakeParams params) {
        /*---------------------------------------------*/
        /* Check Environment variable for xfer history */
        /*---------------------------------------------*/
        if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE)) {
            if (CimStringUtils.equals(params.getDurableType(), BizConstant.SP_DURABLECAT_CASSETTE)) {
                String theCassetteXferHistory = StandardProperties.OM_CARRIER_XFER_HISTORY_STORE.getValue();
                if (CimStringUtils.equals(theCassetteXferHistory, "OFF")) {
                    // By Pass this Event Make
                }
            } else if (CimStringUtils.equals(params.getDurableType(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
                String theReticlePodXferHistory = StandardProperties.OM_RTCLPOD_XFER_HISTORY_STORE.getValue();
                if (CimStringUtils.equals(theReticlePodXferHistory, "OFF")) {
                }
            } else if (CimStringUtils.equals(params.getDurableType(), BizConstant.SP_DURABLECAT_RETICLE)) {
                String theReticleXferHistory = StandardProperties.OM_RETICLE_TRANSFER_HISTORY.getValue();
                if (CimStringUtils.equals(theReticleXferHistory, "OFF")) {
                }
            } else if (CimStringUtils.equals(params.getDurableType(), BizConstant.SP_DURABLECAT_FIXTURE)) {
                String theFixtureXferHistory = StandardProperties.OM_FIXTURE_TRANSFER_HISTORY.getValue();
                if (CimStringUtils.equals(theFixtureXferHistory, "OFF")) {
                }
            }
        }
        /*-----------------------------------*/
        /*   Set Durable Change Event Data   */
        /*-----------------------------------*/
        Event.DurableChangeEventRecord durableChangeEventRecord = new Event.DurableChangeEventRecord();
        durableChangeEventRecord.setDurableID(ObjectIdentifier.fetchValue(params.getDurableID()));
        durableChangeEventRecord.setDurableType(params.getDurableType());
        durableChangeEventRecord.setAction(params.getActionCode());

        Event.EventData eventCommon = new Event.EventData();
        if (!CimStringUtils.isEmpty(params.getTransactionID())) {
            eventCommon.setTransactionID(params.getTransactionID());
        } else {
            eventCommon.setTransactionID(objCommon.getTransactionID());
        }
        eventCommon.setEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        eventCommon.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
        eventCommon.setUserID(objCommon.getUser().getUserID().getValue());
        eventCommon.setEventMemo(params.getClaimMemo());
        durableChangeEventRecord.setEventCommon(eventCommon);

        /*---- Set specified data by durable category ----*/
        if (CimStringUtils.equals(params.getDurableType(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class,params.getDurableID());
            durableChangeEventRecord.setDurableStatus(aCassette.getDurableState());
            String substatus = null;
            if (!CimObjectUtils.isEmpty(aCassette.getDurableSubState())) {
                substatus = aCassette.getDurableSubState().getIdentifier();
            }
            durableChangeEventRecord.setDurableSubStatus(substatus);
            durableChangeEventRecord.setXferStatus(params.getXferStatus());
            if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE)) {
                durableChangeEventRecord.setXferStatChgTimeStamp(params.getTransferStatusChangeTimeStamp());
            } else {
                durableChangeEventRecord.setXferStatChgTimeStamp("");
            }
            durableChangeEventRecord.setLocation(params.getLocation());
        } else if (CimStringUtils.equals(params.getDurableType(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod =baseCoreFactory.getBO(CimReticlePod.class,params.getDurableID());
            Boolean isAvailableFlag = aReticlePod.isAvailable();
            Boolean isInUseFlag = aReticlePod.isInUse();
            Boolean isNotAvailableFlag = aReticlePod.isNotAvailable();
            Boolean isScrappedFlag = aReticlePod.isScrapped();
            if (isAvailableFlag) {
                durableChangeEventRecord.setDurableStatus(CIMFW_DURABLE_AVAILABLE);
            } else if (isInUseFlag) {
                durableChangeEventRecord.setDurableStatus(CIMFW_DURABLE_INUSE);
            } else if (isNotAvailableFlag) {
                durableChangeEventRecord.setDurableStatus(CIMFW_DURABLE_NOTAVAILABLE);
            } else if (isScrappedFlag) {
                durableChangeEventRecord.setDurableStatus(CIMFW_DURABLE_SCRAPPED);
            } else {
                durableChangeEventRecord.setDurableStatus(CIMFW_DURABLE_UNDEFINED);
            }
            durableChangeEventRecord.setXferStatus(params.getXferStatus());
            String substatus = null;
            if (!CimObjectUtils.isEmpty(aReticlePod.getDurableSubState())) {
                substatus = aReticlePod.getDurableSubState().getDurableSubStateName();
            }
            durableChangeEventRecord.setDurableSubStatus(substatus);
            if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE)) {
                durableChangeEventRecord.setXferStatChgTimeStamp(params.getTransferStatusChangeTimeStamp());
            } else {
                durableChangeEventRecord.setXferStatChgTimeStamp("");
            }
            durableChangeEventRecord.setLocation(params.getLocation());
        } else if (CimStringUtils.equals(params.getDurableType(), BizConstant.SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aReticle =baseCoreFactory.getBO(CimProcessDurable.class,params.getDurableID());
            durableChangeEventRecord.setDurableStatus(aReticle.getDurableState());
            String substatus = null;
            if (!CimObjectUtils.isEmpty(aReticle.getDurableSubState())) {
                substatus = aReticle.getDurableSubState().getIdentifier();
            }
            durableChangeEventRecord.setDurableSubStatus(substatus);
            durableChangeEventRecord.setXferStatus(params.getXferStatus());
            if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE)) {
                durableChangeEventRecord.setXferStatChgTimeStamp(params.getTransferStatusChangeTimeStamp());
            } else {
                durableChangeEventRecord.setXferStatChgTimeStamp("");
            }
            durableChangeEventRecord.setLocation(params.getLocation());
        } else {
            CimProcessDurable aFixture =baseCoreFactory.getBO(CimProcessDurable.class,params.getDurableID());
            durableChangeEventRecord.setDurableStatus(aFixture.getDurableState());
            CimDurableSubState durableSubState = aFixture.getDurableSubState();
            if (!CimObjectUtils.isEmpty(durableSubState)) {
                durableChangeEventRecord.setDurableSubStatus(aFixture.getDurableSubState().getIdentifier());
            }
            durableChangeEventRecord.setXferStatus(params.getXferStatus());
            if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE)) {
                durableChangeEventRecord.setXferStatChgTimeStamp(params.getTransferStatusChangeTimeStamp());
            } else {
                durableChangeEventRecord.setXferStatChgTimeStamp("");
            }
            durableChangeEventRecord.setLocation(params.getLocation());
        }
        /*--------------------*/
        /*   Put Event Data   */
        /*--------------------*/
        eventManager.createEvent(durableChangeEventRecord, CimDurableChangeEvent.class);
    }

    @Override
    public void durableXferJobStatusChangeEventMake(Infos.ObjCommon objCommon, Inputs.DurableXferJobStatusChangeEventMakeIn params) {
        /*-----------------------------------*/
        /*   Set Durable Change Event Data   */
        /*-----------------------------------*/
        List<Infos.CarrierJobResult> carrierJobResultList = params.getStrCarrierJobResult();
        for (Infos.CarrierJobResult carrierJobResult : carrierJobResultList) {
            Event.DurableXferJobStatusChangeEventRecord durableXferJobStatusChangeEventRecord = new Event.DurableXferJobStatusChangeEventRecord();
            durableXferJobStatusChangeEventRecord.setDurableType(params.getDurableType());
            durableXferJobStatusChangeEventRecord.setOperationCategory(params.getOperationCategory());
            durableXferJobStatusChangeEventRecord.setCarrierID(ObjectIdentifier.fetchValue(carrierJobResult.getCarrierID()));
            durableXferJobStatusChangeEventRecord.setJobID(params.getJobID());
            durableXferJobStatusChangeEventRecord.setCarrierJobID(carrierJobResult.getCarrierJobID());
            durableXferJobStatusChangeEventRecord.setTransportType(params.getTransportType());
            durableXferJobStatusChangeEventRecord.setZoneType(carrierJobResult.getZoneType());
            durableXferJobStatusChangeEventRecord.setN2purgeFlag((long) (carrierJobResult.isN2PurgeFlag() ? 1 : 0));
            durableXferJobStatusChangeEventRecord.setFromMachineID(ObjectIdentifier.fetchValue(carrierJobResult.getFromMachineID()));
            durableXferJobStatusChangeEventRecord.setFromPortID(ObjectIdentifier.fetchValue(carrierJobResult.getFromPortID()));
            durableXferJobStatusChangeEventRecord.setToStockerGroup(carrierJobResult.getToStockerGroup());
            durableXferJobStatusChangeEventRecord.setToMachineID(ObjectIdentifier.fetchValue(carrierJobResult.getToMachine()));
            durableXferJobStatusChangeEventRecord.setToPortID(ObjectIdentifier.fetchValue(carrierJobResult.getToPortID()));
            durableXferJobStatusChangeEventRecord.setExpectedStrtTime(carrierJobResult.getExpectedStartTime());
            durableXferJobStatusChangeEventRecord.setExpectedEndTime(carrierJobResult.getExpectedEndTime());
            durableXferJobStatusChangeEventRecord.setEstimateStrtTime(carrierJobResult.getEstimatedStartTime());
            durableXferJobStatusChangeEventRecord.setEstimateEndTime(carrierJobResult.getEstimatedEndTime());
            durableXferJobStatusChangeEventRecord.setMandatoryFlag((long) (carrierJobResult.isMandatoryFlag() ? 1 : 0));
            durableXferJobStatusChangeEventRecord.setPriority(carrierJobResult.getPriority());
            durableXferJobStatusChangeEventRecord.setJobStatus(params.getJobStatus());
            durableXferJobStatusChangeEventRecord.setCarrierJobStatus(carrierJobResult.getCarrierJobStatus());
            /*---- Set Common Data for event ----*/
            Event.EventData eventCommon = new Event.EventData();
            eventCommon.setTransactionID(objCommon.getTransactionID());
            if (!CimStringUtils.isEmpty(params.getClaimUserID())) {
                eventCommon.setUserID(params.getClaimUserID());
            } else {
                eventCommon.setUserID(objCommon.getUser().getUserID().getValue());
            }
            if (!CimStringUtils.isEmpty(params.getEventTime())) {
                eventCommon.setEventTimeStamp(params.getEventTime());
            } else {
                eventCommon.setEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
            }
            eventCommon.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
            eventCommon.setEventMemo(params.getClaimMemo());
            durableXferJobStatusChangeEventRecord.setEventCommon(eventCommon);

            eventManager.createEvent(durableXferJobStatusChangeEventRecord, CimDurableXferJobStatusChangeEvent.class);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param equipmentID
     * @param operationMode
     * @param controlJobID
     * @param cassetteID
     * @param strLotInCassette
     * @param claimMemo
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/3/5 13:28
     */
    @Override
    public void lotOperationMoveEventMakeOpeStart(Infos.ObjCommon strObjCommonIn, String transactionID, ObjectIdentifier equipmentID, String operationMode, ObjectIdentifier controlJobID, ObjectIdentifier cassetteID, Infos.LotInCassette strLotInCassette, String claimMemo) {
        Event.LotOperationStartEventRecord anEventRecord = new Event.LotOperationStartEventRecord();

        anEventRecord.setTestCriteriaResult(true);

        CimLot aLot = baseCoreFactory.getBO(CimLot.class, strLotInCassette.getLotID());
        Event.LotEventData lotData = new Event.LotEventData();
        anEventRecord.setLotData(lotData);
        lotData.setLotID(strLotInCassette.getLotID().getValue());
        lotData.setLotType(strLotInCassette.getLotType());
        lotData.setCassetteID(cassetteID.getValue());

        Event.LotEventData lotEventData = aLot.getEventData();

        lotData.setLotStatus(lotEventData.getLotStatus());
        lotData.setCustomerID(lotEventData.getCustomerID());
        lotData.setPriorityClass(lotEventData.getPriorityClass());
        lotData.setProductID(lotEventData.getProductID());
        lotData.setOriginalWaferQuantity(lotEventData.getOriginalWaferQuantity());
        lotData.setCurrentWaferQuantity(lotEventData.getCurrentWaferQuantity());
        lotData.setProductWaferQuantity(lotEventData.getProductWaferQuantity());
        lotData.setControlWaferQuantity(lotEventData.getControlWaferQuantity());
        lotData.setHoldState(lotEventData.getHoldState());
        lotData.setBankID(lotEventData.getBankID());
        lotData.setRouteID(lotEventData.getRouteID());
        lotData.setOperationNumber(lotEventData.getOperationNumber());
        lotData.setOperationID(lotEventData.getOperationID());
        lotData.setOperationPassCount(lotEventData.getOperationPassCount());
        lotData.setObjrefPOS(lotEventData.getObjrefPOS());
        lotData.setWaferHistoryTimeStamp(lotEventData.getWaferHistoryTimeStamp());
        lotData.setObjrefMainPF(lotEventData.getObjrefMainPF());
        lotData.setObjrefModulePOS(lotEventData.getObjrefModulePOS());
        anEventRecord.setSamplingWafers(lotEventData.getSamplingWafers());

        anEventRecord.setLogicalRecipeID(strLotInCassette.getStartRecipe().getLogicalRecipeID().getValue());
        anEventRecord.setMachineRecipeID(strLotInCassette.getStartRecipe().getMachineRecipeID().getValue());
        anEventRecord.setPhysicalRecipeID(strLotInCassette.getStartRecipe().getPhysicalRecipeID());

        Integer rtclLen;
        List<Infos.StartReticleInfo> strStartReticle = strLotInCassette.getStartRecipe().getStartReticleList();
        rtclLen = CimArrayUtils.getSize(strStartReticle);
        List<String> reticleIDs = new ArrayList<>();
        anEventRecord.setReticleIDs(reticleIDs);
        for (int j = 0; j < rtclLen; j++) {
            reticleIDs.add(strStartReticle.get(j).getReticleID().getValue());
        }

        int fixtLen;
        List<Infos.StartFixtureInfo> strStartFixture = strLotInCassette.getStartRecipe().getStartFixtureList();
        fixtLen = CimArrayUtils.getSize(strStartFixture);
        List<String> fixtureIDs = new ArrayList<>();
        anEventRecord.setFixtureIDs(fixtureIDs);
        for (int j = 0; j < fixtLen; j++) {
            fixtureIDs.add(strStartFixture.get(j).getFixtureID().getValue());
        }

        String recipeParameterChangeType;
        recipeParameterChangeType = strLotInCassette.getRecipeParameterChangeType();
        if (CimStringUtils.equals(recipeParameterChangeType, SP_RPARM_CHANGETYPE_BYLOT)) {
            List<Infos.LotWafer> strLotWafer = strLotInCassette.getLotWaferList();
            if (CimArrayUtils.getSize(strLotWafer) > 0) {
                int rpLen;
                List<Infos.StartRecipeParameter> strStartRecipeParameter = strLotWafer.get(0).getStartRecipeParameterList();
                rpLen = CimArrayUtils.getSize(strStartRecipeParameter);
                List<Event.RecipeParmEventData> recipeParameters = new ArrayList<>();
                anEventRecord.setRecipeParameters(recipeParameters);
                for (int j = 0; j < rpLen; j++) {
                    Event.RecipeParmEventData recipeParameter = new Event.RecipeParmEventData();
                    recipeParameters.add(recipeParameter);
                    recipeParameter.setParameterName(strStartRecipeParameter.get(j).getParameterName());
                    recipeParameter.setParameterValue(strStartRecipeParameter.get(j).getParameterValue());
                }
            }
        } else {

            Infos.LotRecipeParameterEventStructOut strLotRecipeParameterEventStructOut;
            strLotRecipeParameterEventStructOut = lotMethod.lotRecipeParameterEventStruct(strObjCommonIn, cassetteID, strLotInCassette);


            List<Infos.OpeHisRecipeParmInfo> strOpeHisRecipeParmInfo = strLotRecipeParameterEventStructOut.getStrOpeHisRecipeParmInfo();
            int lenRParm = CimArrayUtils.getSize(strOpeHisRecipeParmInfo);
            List<Event.RecipeParmEventData> recipeParameters = new ArrayList<>();
            anEventRecord.setRecipeParameters(recipeParameters);
            for (int j = 0; j < lenRParm; j++) {
                Event.RecipeParmEventData recipeParameter = new Event.RecipeParmEventData();
                recipeParameters.add(recipeParameter);
                recipeParameter.setParameterName(strOpeHisRecipeParmInfo.get(j).getRecipeParameterName());
                recipeParameter.setParameterValue(strOpeHisRecipeParmInfo.get(j).getRecipeParameterValue());
            }

            List<Infos.OpeHisRecipeParmWaferInfo> strOpeHisRecipeParmWaferInfo = strLotRecipeParameterEventStructOut.getStrOpeHisRecipeParmWaferInfo();
            int lenRParmWafer = CimArrayUtils.getSize(strOpeHisRecipeParmWaferInfo);
            List<Event.WaferLevelRecipeEventData> waferLevelRecipe = new ArrayList<>();
            anEventRecord.setWaferLevelRecipe(waferLevelRecipe);

            for (int j = 0; j < lenRParmWafer; j++) {
                Event.WaferLevelRecipeEventData waferLevelRecipej = new Event.WaferLevelRecipeEventData();
                waferLevelRecipe.add(waferLevelRecipej);
                waferLevelRecipej.setWaferID(strOpeHisRecipeParmWaferInfo.get(j).getWaferID().getValue());
                waferLevelRecipej.setMachineRecipeID(strOpeHisRecipeParmWaferInfo.get(j).getMachineRecipeID().getValue());

                lenRParm = CimArrayUtils.getSize(strOpeHisRecipeParmWaferInfo.get(j).getStrOpeHisRecipeParmInfo());
                List<Event.WaferRecipeParmEventData> waferRecipeParameters = new ArrayList<>();
                waferLevelRecipej.setWaferRecipeParameters(waferRecipeParameters);

                for (int k = 0; k < lenRParm; k++) {
                    Event.WaferRecipeParmEventData waferRecipeParameter = new Event.WaferRecipeParmEventData();
                    waferRecipeParameters.add(waferRecipeParameter);
                    waferRecipeParameter.setParameterName(
                            strOpeHisRecipeParmWaferInfo.get(j).getStrOpeHisRecipeParmInfo().get(k).getRecipeParameterName());
                    waferRecipeParameter.setParameterValue(
                            strOpeHisRecipeParmWaferInfo.get(j).getStrOpeHisRecipeParmInfo().get(k).getRecipeParameterValue());

                }
            }
        }

        anEventRecord.setEquipmentID(equipmentID.getValue());
        anEventRecord.setOperationMode(operationMode);

        anEventRecord.setPreviousRouteID("");
        anEventRecord.setPreviousOperationID("");
        anEventRecord.setPreviousOperationNumber("");
        anEventRecord.setPreviousOperationPassCount(0L);
        anEventRecord.setPreviousObjrefPOS("");

        CimFlowBatch aFlowBatch = aLot.getFlowBatch();

        if (aFlowBatch != null) {
            anEventRecord.setBatchID(aFlowBatch.getIdentifier());
        }

        anEventRecord.setControlJobID(controlJobID.getValue());
        anEventRecord.setLocateBackFlag(false);

        anEventRecord.setEventCommon(setEventData(strObjCommonIn, claimMemo));

        eventManager.createEvent(anEventRecord, CimLotOperationStartEvent.class);

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param equipmentID
     * @param operationMode
     * @param controlJobID
     * @param cassetteID
     * @param strLotInCassette
     * @param claimMemo
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/7/8 17:22
     */
    @Override
    public void lotOperationMoveEventMakeOpeStartCancel(Infos.ObjCommon strObjCommonIn, String transactionID, ObjectIdentifier equipmentID, String operationMode, ObjectIdentifier controlJobID, ObjectIdentifier cassetteID, Infos.LotInCassette strLotInCassette, String claimMemo) {
        /*--------------------------*/
        /*   Prepare Event Record   */
        /*--------------------------*/
        Event.LotOperationStartEventRecord anEventRecord = new Event.LotOperationStartEventRecord();

        anEventRecord.setTestCriteriaResult(true);

        /*--------------------*/
        /*   Get Lot Object   */
        /*--------------------*/
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, strLotInCassette.getLotID());

        /*------------------------------*/
        /*   Set Data to Event Record   */
        /*------------------------------*/

        /*===== set lotID, lotType, cassetteID =====*/
        anEventRecord.setLotData(new Event.LotEventData());
        anEventRecord.getLotData().setLotID(ObjectIdentifier.fetchValue(strLotInCassette.getLotID()));
        anEventRecord.getLotData().setLotType(aLot.getLotType());
        anEventRecord.getLotData().setCassetteID(ObjectIdentifier.fetchValue(cassetteID));

        Event.LotEventData lotEventData = aLot.getEventData();

        anEventRecord.getLotData().setLotStatus(lotEventData.getLotStatus());
        anEventRecord.getLotData().setCustomerID(lotEventData.getCustomerID());
        anEventRecord.getLotData().setPriorityClass(lotEventData.getPriorityClass());
        anEventRecord.getLotData().setProductID(lotEventData.getProductID());
        anEventRecord.getLotData().setOriginalWaferQuantity(lotEventData.getOriginalWaferQuantity());
        anEventRecord.getLotData().setCurrentWaferQuantity(lotEventData.getCurrentWaferQuantity());
        anEventRecord.getLotData().setProductWaferQuantity(lotEventData.getProductWaferQuantity());
        anEventRecord.getLotData().setControlWaferQuantity(lotEventData.getControlWaferQuantity());
        anEventRecord.getLotData().setHoldState(lotEventData.getHoldState());
        anEventRecord.getLotData().setBankID(lotEventData.getBankID());
        anEventRecord.getLotData().setRouteID(lotEventData.getRouteID());
        anEventRecord.getLotData().setOperationNumber(lotEventData.getOperationNumber());
        anEventRecord.getLotData().setOperationID(lotEventData.getOperationID());
        anEventRecord.getLotData().setOperationPassCount(lotEventData.getOperationPassCount());
        anEventRecord.getLotData().setObjrefPOS(lotEventData.getObjrefPOS());
        anEventRecord.getLotData().setWaferHistoryTimeStamp(lotEventData.getWaferHistoryTimeStamp());
        anEventRecord.getLotData().setObjrefMainPF(lotEventData.getObjrefMainPF());
        anEventRecord.getLotData().setObjrefModulePOS(lotEventData.getObjrefModulePOS());

        anEventRecord.setLogicalRecipeID("");
        anEventRecord.setMachineRecipeID("");
        anEventRecord.setPhysicalRecipeID("");
        anEventRecord.setReticleIDs(new ArrayList<>());
        anEventRecord.setFixtureIDs(new ArrayList<>());

        /*===== set recipe parameter info =====*/
        anEventRecord.setRecipeParameters(new ArrayList<>());
        anEventRecord.setWaferLevelRecipe(new ArrayList<>());

        /*===== set equipment info =====*/
        anEventRecord.setEquipmentID(equipmentID.getValue());
        anEventRecord.setOperationMode(operationMode);

        /*===== set previousXxxxx info =====*/
        anEventRecord.setPreviousRouteID("");
        anEventRecord.setPreviousOperationID("");
        anEventRecord.setPreviousOperationNumber("");
        anEventRecord.setPreviousOperationPassCount(0L);
        anEventRecord.setPreviousObjrefPOS("");

        /*===== set flow batch info =====*/
        CimFlowBatch aFlowBatch = aLot.getFlowBatch();

        if (aFlowBatch != null) {
            anEventRecord.setBatchID(aFlowBatch.getIdentifier());
        }

        /*===== set controlJob info =====*/
        anEventRecord.setControlJobID(controlJobID.getValue());
        anEventRecord.setLocateBackFlag(false);

        /*===== set common info =====*/
        anEventRecord.setEventCommon(new Event.EventData());
        anEventRecord.getEventCommon().setTransactionID(transactionID);
        anEventRecord.getEventCommon().setEventTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp().toString());
        anEventRecord.getEventCommon().setEventShopDate(strObjCommonIn.getTimeStamp().getReportShopDate());
        anEventRecord.getEventCommon().setUserID(strObjCommonIn.getUser().getUserID().getValue());
        anEventRecord.getEventCommon().setEventMemo(claimMemo);

        /*------------------*/
        /*   Create Event   */
        /*------------------*/

        eventManager.createEvent(anEventRecord, CimLotOperationStartEvent.class);
    }

    @Override
    public void lotWaferSortEventMake(Infos.ObjCommon objCommon, String transactionID, List<Infos.WaferTransfer> waferXferList, String claimMemo) {
        //--------------------------------------
        // Get object reference for all Wafer
        //--------------------------------------
        List<CimWafer> aPosWaferList = new ArrayList<>();
        for (Infos.WaferTransfer waferTransfer : waferXferList) {
            aPosWaferList.add(baseCoreFactory.getBO(CimWafer.class, waferTransfer.getWaferID()));
        }
        //--------------------------------------
        // Get Lot ID for all Wafer and
        // Get Lot ID List
        //--------------------------------------
        List<String> waferLotIDList = new ArrayList<>();
        List<String> lotIDList = new ArrayList<>();
        for (CimWafer aPosWafer : aPosWaferList) {
            Lot aPosLot = aPosWafer.getLot();
            Validations.check(null == aPosLot, retCodeConfig.getNotFoundLot());
            String waferLotID = aPosLot.getIdentifier();
            waferLotIDList.add(waferLotID);
            boolean bLotIDAdded = false;
            for (String lotID : lotIDList) {
                if (CimStringUtils.equals(waferLotID, lotID)) {
                    bLotIDAdded = true;
                    break;
                }
            }
            if (!bLotIDAdded) {
                lotIDList.add(waferLotID);
            }
        }
        //----------------------------------
        // Create Event Data for each Lot
        //----------------------------------
        for (String lotID : lotIDList) {
            Event.LotWaferSortEventRecord anEventRecord = new Event.LotWaferSortEventRecord();
            //----------------------------------
            // Prepare Wafer Sort Data
            //----------------------------------
            anEventRecord.setLotID(lotID);
            com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.Params .ObjSorterJobListGetDRIn();
            objSorterJobListGetDRIn.setLotID(new ObjectIdentifier(lotID));
            List<Info.SortJobListAttributes> objSorterJobListGetDROut = sorterNewMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
            List<Info.SortJobListAttributes> sortJobListAttributesList = objSorterJobListGetDROut;
            if (!CimObjectUtils.isEmpty(sortJobListAttributesList)) {
                boolean setFlag = false;
                for (Info.SortJobListAttributes sortJobListAttributes : sortJobListAttributesList) {
                    List<Info.SorterComponentJobListAttributes> sorterComponentJobListAttributesList = sortJobListAttributes.getSorterComponentJobListAttributesList();
                    for (Info.SorterComponentJobListAttributes sorterComponentJobListAttributes : sorterComponentJobListAttributesList) {
                        if (CimStringUtils.equals(sorterComponentJobListAttributes.getComponentSorterJobStatus(), SP_SORTERCOMPONENTJOBSTATUS_EXECUTING)) {
                            anEventRecord.setEquipmentID(ObjectIdentifier.fetchValue(sortJobListAttributes.getEquipmentID()));
                            anEventRecord.setSorterJobID(ObjectIdentifier.fetchValue(sortJobListAttributes.getSorterJobID()));
                            anEventRecord.setComponentJobID(ObjectIdentifier.fetchValue(sorterComponentJobListAttributes.getSorterComponentJobID()));
                            setFlag = true;
                            break;
                        }
                        if (setFlag) {
                            break;
                        }
                    }
                    if (setFlag) {
                        break;
                    }
                }
            }
            List<Event.WaferEventData> currentWafers = new ArrayList<>();
            anEventRecord.setCurrentWafers(currentWafers);
            int lenWaferLotID = CimArrayUtils.getSize(waferLotIDList);
            for (int j = 0; j < lenWaferLotID; j++) {
                if (CimStringUtils.equals(lotID, waferLotIDList.get(j))) {
                    Event.WaferEventData currentEventData = new Event.WaferEventData();
                    currentEventData.setWaferID(waferXferList.get(j).getWaferID().getValue());
                    currentEventData.setOriginalCassetteID(waferXferList.get(j).getOriginalCassetteID().getValue());
                    currentEventData.setOriginalSlotNumber(CimNumberUtils.longValue(waferXferList.get(j).getOriginalSlotNumber()));
                    currentEventData.setDestinationCassetteID(waferXferList.get(j).getDestinationCassetteID().getValue());
                    currentEventData.setDestinationSlotNumber(CimNumberUtils.longValue(waferXferList.get(j).getDestinationSlotNumber()));
                    currentEventData.setControlWaferFlag(aPosWaferList.get(j).isControlWafer());
                    currentWafers.add(currentEventData);
                }
            }
            //---------------------------------
            // Prepare Wafer Not Sort Data
            //---------------------------------
            List<Infos.LotWaferMap> lotWaferMapGetResult = lotMethod.lotWaferMapGet(objCommon, new ObjectIdentifier(lotID));
            List<Event.WaferEventData> sourceWafers = new ArrayList<>();
            anEventRecord.setSourceWafers(sourceWafers);
            List<Infos.LotWaferMap> lotWaferMapList = lotWaferMapGetResult;
            for (Infos.LotWaferMap lotWaferMap : lotWaferMapList) {
                boolean bWaferIDFound = false;
                for (int n = 0; n < lenWaferLotID; n++) {
                    if (CimStringUtils.equals(lotID, waferLotIDList.get(n))
                            && ObjectIdentifier.equalsWithValue(lotWaferMap.getWaferID(), waferXferList.get(n).getWaferID())) {
                        bWaferIDFound = true;
                        break;
                    }
                }
                if (!bWaferIDFound) {
                    Event.WaferEventData sourceEventData = new Event.WaferEventData();
                    sourceEventData.setWaferID(lotWaferMap.getWaferID().getValue());
                    sourceEventData.setOriginalCassetteID(lotWaferMap.getCassetteID().getValue());
                    sourceEventData.setOriginalSlotNumber(CimNumberUtils.longValue(lotWaferMap.getSlotNumber()));
                    sourceEventData.setControlWaferFlag(lotWaferMap.isControlWaferFlag());
                }
            }
            //----------------------------------
            // Prepare eventCommon Data
            //----------------------------------
            anEventRecord.setEventCommon(setEventData(objCommon, claimMemo));
            eventManager.createEvent(anEventRecord, CimLotWaferSortEvent.class);
        }
    }

    @Override
    public void durableChangeEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier durableID, String durableType, String actionCode, String claimMemo) {
        /*---------------------------------------------*/
        /* Check Environment variable for xfer history */
        /*---------------------------------------------*/
        if (CimStringUtils.equals(actionCode, BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE)) {
            if (CimStringUtils.equals(durableType, BizConstant.SP_DURABLECAT_CASSETTE)) {
                String theCassetteXferHistory = StandardProperties.OM_CARRIER_XFER_HISTORY_STORE.getValue();
                if (CimStringUtils.equals(theCassetteXferHistory, "OFF")) {
                }
            } else if (CimStringUtils.equals(durableType, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
                String theReticlePodXferHistory = StandardProperties.OM_RTCLPOD_XFER_HISTORY_STORE.getValue();
                if (CimStringUtils.equals(theReticlePodXferHistory, "OFF")) {
                }
            } else if (CimStringUtils.equals(durableType, BizConstant.SP_DURABLECAT_RETICLE)) {
                String theReticleXferHistory = StandardProperties.OM_RETICLE_TRANSFER_HISTORY.getValue();
                if (CimStringUtils.equals(theReticleXferHistory, "OFF")) {
                }
            } else if (CimStringUtils.equals(durableType, BizConstant.SP_DURABLECAT_FIXTURE)) {
                String theFixtureXferHistory = StandardProperties.OM_FIXTURE_TRANSFER_HISTORY.getValue();
                if (CimStringUtils.equals(theFixtureXferHistory, "OFF")) {
                }
            }
        }
        /*-----------------------------------*/
        /*   Set Durable Change Event Data   */
        /*-----------------------------------*/
        Event.DurableChangeEventRecord durableChangeEventRecord = new Event.DurableChangeEventRecord();
        durableChangeEventRecord.setDurableID(durableID.getValue());
        if (CimStringUtils.equals(durableType,BizConstant.SP_DURABLECAT_CASSETTE)){
            durableChangeEventRecord.setDurableType(SP_SCRIPTPARM_CLASS_CARRIER);
        }
        durableChangeEventRecord.setDurableType(durableType);
        durableChangeEventRecord.setAction(actionCode);
        durableChangeEventRecord.setEventCommon(setEventData(objCommon, claimMemo));
        if (!CimStringUtils.isEmpty(transactionID)) {
            durableChangeEventRecord.getEventCommon().setTransactionID(transactionID);
        } else {
            durableChangeEventRecord.getEventCommon().setTransactionID(objCommon.getTransactionID());
        }

        /*---- Set specified data by durable category ----*/
        if (CimStringUtils.equals(durableType, BizConstant.SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class,durableID);
            durableChangeEventRecord.setDurableStatus(aCassette.getDurableState());
            CimDurableSubState durableSubState = aCassette.getDurableSubState();

            if (durableSubState != null) {
                durableChangeEventRecord.setDurableSubStatus(durableSubState.getIdentifier());
            } else {
                durableChangeEventRecord.setDurableSubStatus("");
            }
            durableChangeEventRecord.setXferStatus(aCassette.getTransportState());
            if (CimStringUtils.equals(actionCode, BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE)) {
                durableChangeEventRecord.setXferStatChgTimeStamp(CimDateUtils.convertToSpecString(aCassette.getTransferStatusChangedTimeStamp()));
            } else {
                durableChangeEventRecord.setXferStatChgTimeStamp("");
            }
            Machine aMachine = aCassette.currentAssignedMachine();
            durableChangeEventRecord.setLocation("");
            if (aMachine != null) {
                boolean isStorageMachine = aMachine.isStorageMachine();
                if (!isStorageMachine) {
                    durableChangeEventRecord.setLocation(aMachine.getIdentifier());
                }
            }
        } else if (CimStringUtils.equals(durableType, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,durableID);

            boolean isAvailableFlag = aReticlePod.isAvailable();
            boolean isInUseFlag = aReticlePod.isInUse();
            boolean isNotAvailableFlag = aReticlePod.isNotAvailable();
            boolean isScrappedFlag = aReticlePod.isScrapped();
            if (isAvailableFlag) {
                durableChangeEventRecord.setDurableStatus(BizConstant.CIMFW_DURABLE_AVAILABLE);
            } else if (isInUseFlag) {
                durableChangeEventRecord.setDurableStatus(BizConstant.CIMFW_DURABLE_INUSE);
            } else if (isNotAvailableFlag) {
                durableChangeEventRecord.setDurableStatus(BizConstant.CIMFW_DURABLE_NOTAVAILABLE);
            } else if (isScrappedFlag) {
                durableChangeEventRecord.setDurableStatus(BizConstant.CIMFW_DURABLE_SCRAPPED);
            } else {
                durableChangeEventRecord.setDurableStatus(BizConstant.CIMFW_DURABLE_UNDEFINED);
            }
            CimDurableSubState durableSubState = aReticlePod.getDurableSubState();
            if (durableSubState != null) {
                durableChangeEventRecord.setDurableSubStatus(durableSubState.getIdentifier());
            } else {
                durableChangeEventRecord.setDurableSubStatus("");
            }

            durableChangeEventRecord.setXferStatus(aReticlePod.getTransferStatus());
            if (CimStringUtils.equals(actionCode, BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE)) {
                durableChangeEventRecord.setXferStatChgTimeStamp(CimDateUtils.convertToSpecString(aReticlePod.getTransferStatusChangedTimeStamp()));
            } else {
                durableChangeEventRecord.setXferStatChgTimeStamp("");
            }
            Machine aMachine = aReticlePod.currentAssignedMachine();
            durableChangeEventRecord.setLocation("");
            if (aMachine != null) {
                boolean isStorageMachine = aMachine.isStorageMachine();
                if (!isStorageMachine) {
                    durableChangeEventRecord.setLocation(aMachine.getIdentifier());
                }
            }
        } else if (CimStringUtils.equals(durableType, BizConstant.SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class,durableID);

            durableChangeEventRecord.setDurableStatus(aReticle.getDurableState());
            CimDurableSubState cimDurableSubState = aReticle.getDurableSubState();
            if (cimDurableSubState != null) {
                durableChangeEventRecord.setDurableSubStatus(cimDurableSubState.getIdentifier());
            } else {
                durableChangeEventRecord.setDurableSubStatus("");
            }
            durableChangeEventRecord.setXferStatus(aReticle.getTransportState());
            if (CimStringUtils.equals(actionCode, BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE)) {
                durableChangeEventRecord.setXferStatChgTimeStamp(CimDateUtils.convertToSpecString(aReticle.getTransferStatusChangedTimeStamp()));
            } else {
                durableChangeEventRecord.setXferStatChgTimeStamp("");
            }
            Machine aMachine = aReticle.currentAssignedMachine();
            durableChangeEventRecord.setLocation("");
            if (aMachine != null) {
                boolean isStorageMachine = aMachine.isStorageMachine();
                if (!isStorageMachine) {
                    durableChangeEventRecord.setLocation(aMachine.getIdentifier());
                }
            }
        } else {
            CimProcessDurable aFixture = baseCoreFactory.getBO(CimProcessDurable.class,durableID);

            durableChangeEventRecord.setDurableStatus(aFixture.getDurableState());
            durableChangeEventRecord.setXferStatus(aFixture.getTransportState());
            if (CimStringUtils.equals(actionCode, BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE)) {
                durableChangeEventRecord.setXferStatChgTimeStamp(CimDateUtils.convertToSpecString(aFixture.getTransferStatusChangedTimeStamp()));
            } else {
                durableChangeEventRecord.setXferStatChgTimeStamp("");
            }
            Machine aMachine = aFixture.currentAssignedMachine();
            durableChangeEventRecord.setLocation("");
            if (aMachine != null) {
                boolean isStorageMachine = aMachine.isStorageMachine();
                if (!isStorageMachine) {
                    durableChangeEventRecord.setLocation(aMachine.getIdentifier());
                }
            }
        }
        /*--------------------*/
        /*   Put Event Data   */
        /*--------------------*/
        eventManager.createEvent(durableChangeEventRecord, CimDurableChangeEvent.class);
    }

    @Override
    public void controlJobStatusChangeEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier controlJobID, String controlJobStatus, List<Infos.StartCassette> startCassetteList, String claimMemo) {
        Event.ControlJobStatusChangeEventRecord anEventRecord = new Event.ControlJobStatusChangeEventRecord();
        //------------------------------------------------------------------------
        // Set to OMEVCJSC
        //------------------------------------------------------------------------
        anEventRecord.setCtrlJob(ObjectIdentifier.fetchValue(controlJobID));
        anEventRecord.setCtrlJobState(controlJobStatus);
        //--------------------------------
        //   Get ControlJob Object
        //--------------------------------
        CimControlJob aControlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        //--------------------------------
        //   Get EquipmentID
        //--------------------------------
        CimMachine aMachine = aControlJob.getMachine();
        Validations.check(aMachine == null, retCodeConfig.getNotFoundEqp());

        //---------------------------------------------------------
        //   Get EquipmentID and Set EquipmentID
        //---------------------------------------------------------
        ObjectIdentifier equipmentID = new ObjectIdentifier(aMachine.getIdentifier(), aMachine.getPrimaryKey());
        anEventRecord.setEqpID(ObjectIdentifier.fetchValue(equipmentID));
        //---------------------------------------------------------
        //   Get EquipmentID Description and Set eqpDescription
        //---------------------------------------------------------
        anEventRecord.setEqpDescription(aMachine.getDescription());

        //---------------------------------------------------------
        //   Get Lot Information and and Set lots
        //---------------------------------------------------------
        List<Event.ControlJobStatusChangeLotEventData> lots = new ArrayList<>();
        anEventRecord.setLots(lots);
        for (Infos.StartCassette startCassette : startCassetteList) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                Event.ControlJobStatusChangeLotEventData eventData = new Event.ControlJobStatusChangeLotEventData();
                lots.add(eventData);
                //------------------------------------------------------------------
                //   Omit Not-OpeStart Lot
                //------------------------------------------------------------------
                if (!lotInCassette.getMoveInFlag()) {
                    continue;
                }
                //------------------------------------------------------------------
                //   Get Lot ojbect
                //------------------------------------------------------------------
                CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());
                //--------------------------------------------------------------------
                // Get Object ProductSpecification_var
                //--------------------------------------------------------------------
                CimProductSpecification aLotProdSpec = aLot.getProductSpecification();
                Validations.check(aLotProdSpec == null, retCodeConfig.getNotFoundProductSpec());

                //------------------------------------------------------------------------
                // Get productID and Set ProductSpecification
                //------------------------------------------------------------------------
                eventData.setProdSpecID(ObjectIdentifier.fetchValue(aLotProdSpec.getProductSpecID()));
                //------------------------------------------------------------------------
                // Set to OMEVCJSC_LOTS
                //------------------------------------------------------------------------
                eventData.setLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                eventData.setCastID(ObjectIdentifier.fetchValue(startCassette.getCassetteID()));
                eventData.setLotType(lotInCassette.getLotType());
                eventData.setSubLotType(lotInCassette.getSubLotType());
                eventData.setMainPDID(ObjectIdentifier.fetchValue(lotInCassette.getStartOperationInfo().getProcessFlowID()));
                eventData.setOpeNo(lotInCassette.getStartOperationInfo().getOperationNumber());
                eventData.setPdID(ObjectIdentifier.fetchValue(lotInCassette.getStartOperationInfo().getOperationID()));
                eventData.setOpePassCount(CimNumberUtils.longValue(lotInCassette.getStartOperationInfo().getPassCount()));
                CimProcessDefinition aProcessDefinition = baseCoreFactory.getBO(CimProcessDefinition.class, lotInCassette.getStartOperationInfo().getOperationID());
                boolean processDefinitionFound = aProcessDefinition != null;
                if (!processDefinitionFound) {
                    eventData.setPdName("");
                } else {
                    eventData.setPdName(aProcessDefinition.getProcessDefinitionName());
                }
            }
        }
        //----------------------------------
        // Prepare eventCommon Data
        //----------------------------------
        anEventRecord.setEventCommon(setEventData(objCommon, claimMemo));
        eventManager.createEvent(anEventRecord, CimControlJobStatusChangeEvent.class);
    }

    @Override
    public void processJobChangeEventMake(Infos.ObjCommon objCommon, Inputs.ProcessJobChangeEventMakeParams params) {
        Event.ProcessJobChangeEventRecord anEventRecord = new Event.ProcessJobChangeEventRecord();
        anEventRecord.setCtrlJob(ObjectIdentifier.fetchValue(params.getControlJobID()));
        anEventRecord.setPrcsJob(params.getProcessJobID());
        anEventRecord.setOpeCategory(params.getOpeCategory());
        anEventRecord.setProcessStart(params.getProcessStart());
        anEventRecord.setCurrentState(params.getCurrentState());
        List<Infos.ProcessWafer> processWaferList = params.getProcessWaferList();
        List<Event.ProcessJobChangeWaferEventData> wafers = new ArrayList<>();
        anEventRecord.setWafers(wafers);
        if (!CimArrayUtils.isEmpty(processWaferList)) {
            processWaferList.forEach(processWafer -> {
                Event.ProcessJobChangeWaferEventData processJobChangeWaferEventData = new Event.ProcessJobChangeWaferEventData();
                processJobChangeWaferEventData.setLotID(ObjectIdentifier.fetchValue(processWafer.getLotID()));
                processJobChangeWaferEventData.setWaferID(ObjectIdentifier.fetchValue(processWafer.getWaferID()));
                wafers.add(processJobChangeWaferEventData);
            });
        }
        List<Infos.ProcessJobChangeRecipeParameter> processJobChangeRecipeParameterList = params.getProcessJobChangeRecipeParameterList();
        List<Event.ProcessJobChangeRecipeParameterEventData> recipeParameters = new ArrayList<>();
        anEventRecord.setRecipeParameters(recipeParameters);
        if (!CimArrayUtils.isEmpty(processJobChangeRecipeParameterList)) {
            processJobChangeRecipeParameterList.forEach(processJobChangeRecipeParameter -> {
                Event.ProcessJobChangeRecipeParameterEventData processJobChangeRecipeParameterEventData = new Event.ProcessJobChangeRecipeParameterEventData();
                processJobChangeRecipeParameterEventData.setParameterName(processJobChangeRecipeParameter.getParameterName());
                processJobChangeRecipeParameterEventData.setPreviousParameterValue(processJobChangeRecipeParameter.getPreParameterValue());
                processJobChangeRecipeParameterEventData.setParameterValue(processJobChangeRecipeParameter.getParameterValue());
                recipeParameters.add(processJobChangeRecipeParameterEventData);
            });
        }
        /*---------------------------*/
        /*   Set Event Common Info   */
        /*---------------------------*/
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        eventManager.createEvent(anEventRecord, CimProcessJobChangeEvent.class);
    }

    @Override
    public void recipeBodyManageEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier equipmentID, String actionCode, ObjectIdentifier machineRecipeID, String physicalRecipeID, String fileLocation, String fileName, boolean formatFlag, String claimMemo) {
        /*--------------------------------------------*/
        /*   Set Equipment Status Change Event Data   */
        /*--------------------------------------------*/
        Event.RecipeBodyManageEventRecord rcpBodyMngEvtRecord = new Event.RecipeBodyManageEventRecord();
        rcpBodyMngEvtRecord.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
        rcpBodyMngEvtRecord.setActionCode(actionCode);
        rcpBodyMngEvtRecord.setMachineRecipeID(ObjectIdentifier.fetchValue(machineRecipeID));
        rcpBodyMngEvtRecord.setPhysicalRecipeID(physicalRecipeID);
        rcpBodyMngEvtRecord.setFileLocation(fileLocation);
        rcpBodyMngEvtRecord.setFileName(fileName);
        rcpBodyMngEvtRecord.setFormatFlag(formatFlag);

        rcpBodyMngEvtRecord.setEventCommon(setEventData(objCommon, claimMemo));

        eventManager.createEvent(rcpBodyMngEvtRecord, CimRecipeBodyManageEvent.class);
    }

    @Override
    public void entityInhibitEventMake(Infos.ObjCommon objCommon, Inputs.EntityInhibitEventMakeParams params) {
        Infos.EntityInhibitDetailInfo entityInhibitInfo = params.getEntityInhibitDetailInfo();
        ObjectIdentifier reasonCode = params.getReasonCode();
        Event.EntityInhibitEventRecord anEventRecord = new Event.EntityInhibitEventRecord();
        anEventRecord.setFunctionRule(entityInhibitInfo.getEntityInhibitDetailAttributes().getFunctionRule());
        anEventRecord.setSpecTool(entityInhibitInfo.getEntityInhibitDetailAttributes().isSpecTool());
        anEventRecord.setInhibitID(ObjectIdentifier.fetchValue(entityInhibitInfo.getEntityInhibitID()));
        anEventRecord.setStartTimeStamp(entityInhibitInfo.getEntityInhibitDetailAttributes().getStartTimeStamp());
        anEventRecord.setEndTimeStamp(entityInhibitInfo.getEntityInhibitDetailAttributes().getEndTimeStamp());
        anEventRecord.setClaimMemo(entityInhibitInfo.getEntityInhibitDetailAttributes().getMemo());
        List<Constrain.EntityIdentifier> entities = entityInhibitInfo.getEntityInhibitDetailAttributes().getEntities()
                .stream()
                .map(detial -> new Constrain.EntityIdentifier(detial.getClassName(), detial.getObjectID().getValue(), detial.getAttribution()))
                .collect(Collectors.toList());
        anEventRecord.setEntities(entities);
        anEventRecord.setSubLotTypes(entityInhibitInfo.getEntityInhibitDetailAttributes().getSubLotTypes());

        List<Constrain.EntityIdentifier> expEntities = Optional.
                ofNullable(entityInhibitInfo.getEntityInhibitDetailAttributes().getExceptionEntities())
                .map(entityList -> entityList
                        .parallelStream()
                        .map(detail -> new Constrain.EntityIdentifier(detail.getClassName(), detail.getObjectID().getValue(),
                                Optional.ofNullable(detail.getAttribution()).orElse(BizConstant.EMPTY)))
                        .collect(Collectors.toList())).orElse(Collections.emptyList());
        anEventRecord.setExpEntities(expEntities);

        if (CimStringUtils.equals(params.getTransactionID(),
                TransactionIDEnum.ENTITY_INHIBIT_CANCEL_REQ.getValue())) {
            anEventRecord.setReasonCode(ObjectIdentifier.fetchValue(reasonCode));
            CimCategory aCategory = newCodeManager
                    .findCategoryNamed(BizConstant.SP_REASONCAT_ENTITYINHIBITCANCEL);
            if (aCategory != null) {
                CimCode aReasonCode = null;
                String aCategoryIdentifier = aCategory.getIdentifier();
                if (CimObjectUtils.isEmpty(reasonCode.getReferenceKey())) {
                    Validations.check(CimObjectUtils.isEmpty(reasonCode.getValue()),
                            retCodeConfig.getNotFoundCode(), aCategoryIdentifier, "*****");
                    aReasonCode = aCategory.findCodeNamed(reasonCode.getValue());
                } else {
                    aReasonCode = baseCoreFactory.getBO(CimCode.class,
                            reasonCode.getReferenceKey());
                }
                Validations.check(aReasonCode == null, retCodeConfig.getNotFoundCode(), aCategoryIdentifier,
                        reasonCode);

                anEventRecord.setDescription(aReasonCode.getDescription());
            } else {
                anEventRecord.setDescription("");
            }
        } else {
            anEventRecord.setReasonCode(entityInhibitInfo.getEntityInhibitDetailAttributes().getReasonCode());
            anEventRecord.setReasonDesc(entityInhibitInfo.getEntityInhibitDetailAttributes().getReasonDesc());
        }
        anEventRecord.setOwnerID(ObjectIdentifier.fetchValue(entityInhibitInfo.getEntityInhibitDetailAttributes().getOwnerID()));
        List<Constrain.EntityInhibitReasonDetailInfo> detailInfos = new ArrayList<>();
        List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos = entityInhibitInfo.getEntityInhibitDetailAttributes().getEntityInhibitReasonDetailInfos();
        if (CimArrayUtils.isNotEmpty(entityInhibitReasonDetailInfos)) {
            for (Infos.EntityInhibitReasonDetailInfo entityInhibitReasonDetailInfo : entityInhibitReasonDetailInfos) {
                Constrain.EntityInhibitReasonDetailInfo detailInfo = new Constrain.EntityInhibitReasonDetailInfo();
                detailInfos.add(detailInfo);
                detailInfo.setRelatedLotID(entityInhibitReasonDetailInfo.getRelatedLotID());
                detailInfo.setRelatedControlJobID(entityInhibitReasonDetailInfo.getRelatedControlJobID());
                detailInfo.setRelatedFabID(entityInhibitReasonDetailInfo.getRelatedFabID());
                detailInfo.setRelatedRouteID(entityInhibitReasonDetailInfo.getRelatedRouteID());
                detailInfo.setRelatedProcessDefinitionID(entityInhibitReasonDetailInfo.getRelatedProcessDefinitionID());
                detailInfo.setRelatedOperationNumber(entityInhibitReasonDetailInfo.getRelatedOperationNumber());
                detailInfo.setRelatedOperationPassCount(entityInhibitReasonDetailInfo.getRelatedOperationPassCount());

                List<Constrain.EntityInhibitSpcChartInfo> infos = new ArrayList<>();
                List<Infos.EntityInhibitSpcChartInfo> strEntityInhibitSpcChartInfos = entityInhibitReasonDetailInfo.getStrEntityInhibitSpcChartInfos();
                if (!CimObjectUtils.isEmpty(strEntityInhibitSpcChartInfos)) {
                    for (Infos.EntityInhibitSpcChartInfo strEntityInhibitSpcChartInfo : strEntityInhibitSpcChartInfos) {
                        Constrain.EntityInhibitSpcChartInfo info = new Constrain.EntityInhibitSpcChartInfo();
                        infos.add(info);
                        info.setRelatedSpcDcType(strEntityInhibitSpcChartInfo.getRelatedSpcDcType());
                        info.setRelatedSpcChartGroupID(strEntityInhibitSpcChartInfo.getRelatedSpcChartGroupID());
                        info.setRelatedSpcChartID(strEntityInhibitSpcChartInfo.getRelatedSpcChartID());
                        info.setRelatedSpcChartType(strEntityInhibitSpcChartInfo.getRelatedSpcChartType());
                        info.setRelatedSpcChartUrl(strEntityInhibitSpcChartInfo.getRelatedSpcChartUrl());
                    }
                }
                detailInfo.setStrEntityInhibitSpcChartInfos(infos);
            }
        }
        anEventRecord.setReasonDetailInfos(detailInfos);

        anEventRecord.setAppliedContext(ObjectIdentifier.fetchValue(params.getControlJobID()));
        List<Infos.EntityInhibitExceptionLotInfo> entityInhibitExceptionLotInfos = entityInhibitInfo.getEntityInhibitDetailAttributes().getEntityInhibitExceptionLotInfos();
        if (!CimObjectUtils.isEmpty(entityInhibitExceptionLotInfos)) {
            List<Constrain.ExceptionLotRecord> exceptionLots = entityInhibitExceptionLotInfos.stream().map(entityInhibitExceptionLotInfo -> {
                Constrain.ExceptionLotRecord exceptionLotRecord = new Constrain.ExceptionLotRecord();
                exceptionLotRecord.setLotID(entityInhibitExceptionLotInfo.getLotID());
                exceptionLotRecord.setSingleTriggerFlag(entityInhibitExceptionLotInfo.getSingleTriggerFlag());
                return exceptionLotRecord;
            }).collect(Collectors.toList());
            anEventRecord.setExceptionLots(exceptionLots);
        }
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));

        CimEventBase event = eventManager.createEvent(anEventRecord, CimEntityInhibitEvent.class);

        //-------------------------------//
        // Extendable Event              //
        //-------------------------------//
        if (null != event) {
            if (CimStringUtils.equals("APBE", anEventRecord.getReasonCode())
                    || CimStringUtils.equals("EXPD", anEventRecord.getReasonCode())
                    || CimStringUtils.equals("ICWS", anEventRecord.getReasonCode())) {
                return;
            }
            // Make Event
            BaseEvent baseEvent = new BaseEvent(this);
            baseEvent.setTxID(objCommon.getTransactionID());
            baseEvent.setUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
            baseEvent.setEventID(event.getPrimaryKey());
            baseEvent.setEventInfo(JSONObject.toJSONString(anEventRecord));
            extendableEventManager.makeEvent(BizConstant.CATEGORY_CONSTRAINT, baseEvent);
        }

    }

    @Override
    public void lotChangeEventMake(Infos.ObjCommon objCommon, Inputs.LotChangeEventMakeParams params) {
        Event.LotChangeEventRecord anEventRecord = new Event.LotChangeEventRecord();
        anEventRecord.setLotID(params.getLotID());
        anEventRecord.setExternalPriority(CimNumberUtils.longValue(params.getExternalPriority()));
        anEventRecord.setLotOwnerID(params.getLotOwnerID());
        anEventRecord.setLotComment(params.getLotComment());
        anEventRecord.setOrderNumber(params.getOrderNumber());
        anEventRecord.setCustomerID(params.getCustomerCodeID());
        anEventRecord.setPriorityClass(CimNumberUtils.longValue(params.getPriorityClass()));
        anEventRecord.setProductID(params.getProductID());
        anEventRecord.setPreviousProductID(params.getPreviousProductID());
        anEventRecord.setPlanStartTime(params.getPlanStartTime());
        anEventRecord.setPlanCompTime(params.getPlanCompTime());////////////////////////////////////////////
        //   0.01 (DCR9900166) (R20b) add start   //
        ////////////////////////////////////////////
        //----- Get Lot Object -----//
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, new ObjectIdentifier(params.getLotID()));
        Validations.check(null == aLot, new OmCode(retCodeConfig.getNotFoundLot(), params.getLotID()));

        anEventRecord.setLotStatus(aLot.getState());
        anEventRecord.setStageID("");
        boolean isOnFloor = CimBooleanUtils.isTrue(aLot.isOnFloor());
        boolean isNonProBank = CimBooleanUtils.isTrue(aLot.isNonProBank());
        if (isOnFloor || isNonProBank) {
            CimProcessOperation aPO = aLot.getProcessOperation();
            if (aPO != null) {
                CimStage aStage = aPO.getStage();
                if (aStage != null) {
                    anEventRecord.setStageID(aStage.getIdentifier());
                }
            }
        }
        Infos.ObjCommon duplicate = objCommon.duplicate();
        duplicate.setTransactionID(params.getTransactionID());
        anEventRecord.setEventCommon(setEventData(duplicate, params.getClaimMemo()));
        eventManager.createEvent(anEventRecord, CimLotChangeEvent.class);
    }

    @Override
    public void qTimeChangeEventMake(Infos.ObjCommon objCommon, Inputs.QTimeChangeEventMakeParams params) {
        ObjectIdentifier lotID = params.getLotID();
        ObjectIdentifier waferID = params.getQtimeInfo().getWaferID();
        Infos.QtimeInfo qtimeInfo = params.getQtimeInfo();

        Event.QTimeEventRecord anEventRecord = new Event.QTimeEventRecord();
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));

        anEventRecord.setQTimeType(qtimeInfo.getQTimeType());
        anEventRecord.setLotID(ObjectIdentifier.fetchValue(lotID));
        anEventRecord.setWaferID(ObjectIdentifier.fetchValue(waferID));
        anEventRecord.setOriginalQTime(qtimeInfo.getOriginalQTime());
        anEventRecord.setProcessDefinitionLevel(qtimeInfo.getProcessDefinitionLevel());
        anEventRecord.setOpeCategory(params.getUpdateMode());
        anEventRecord.setTriggerMainProcessDefinitionID(ObjectIdentifier.fetchValue(qtimeInfo.getRestrictionTriggerRouteID()));
        anEventRecord.setTriggerOperationNumber(qtimeInfo.getRestrictionTriggerOperationNumber());
        anEventRecord.setTriggerBranchInfo(qtimeInfo.getRestrictionTriggerBranchInfo());
        anEventRecord.setTriggerReturnInfo(qtimeInfo.getRestrictionTriggerReturnInfo());
        anEventRecord.setTriggerTimeStamp(qtimeInfo.getRestrictionTriggerTimeStamp());
        anEventRecord.setTargetMainProcessDefinitionID(ObjectIdentifier.fetchValue(qtimeInfo.getRestrictionTargetRouteID()));
        anEventRecord.setTargetOperationNumber(qtimeInfo.getRestrictionTargetOperationNumber());
        anEventRecord.setTargetBranchInfo(qtimeInfo.getRestrictionTargetBranchInfo());
        anEventRecord.setTargetReturnInfo(qtimeInfo.getRestrictionTargetReturnInfo());
        anEventRecord.setTargetTimeStamp(qtimeInfo.getRestrictionTargetTimeStamp());
        anEventRecord.setPreviousTargetInfo(qtimeInfo.getPreviousTargetInfo());
        anEventRecord.setControl(qtimeInfo.getSpecificControl());
        if (CimStringUtils.equals(qtimeInfo.getWatchDogRequired(), "Y")) {
            anEventRecord.setWatchdogRequired(true);
        } else if (CimStringUtils.equals(qtimeInfo.getWatchDogRequired(), "N")) {
            anEventRecord.setWatchdogRequired(false);
        }
        if (CimStringUtils.equals(qtimeInfo.getActionDoneFlag(), "Y")) {
            anEventRecord.setActionDone(true);
        } else if (CimStringUtils.equals(qtimeInfo.getActionDoneFlag(), "N")) {
            anEventRecord.setActionDone(false);
        }
        anEventRecord.setManualCreated(qtimeInfo.getManualCreated());
        anEventRecord.setPreTrigger(qtimeInfo.getPreTrigger());

        List<Infos.QTimeActionInfo> qtimeActionInfoList = qtimeInfo.getStrQtimeActionInfoList();
        List<Event.QTimeActionEventData> actions = qtimeActionInfoList == null ? null : qtimeActionInfoList.stream().map(qTimeActionInfo -> {
            Event.QTimeActionEventData qTimeActionEventData = new Event.QTimeActionEventData();
            qTimeActionEventData.setTargetTimeStamp(qTimeActionInfo.getQrestrictionTargetTimeStamp());
            qTimeActionEventData.setAction(qTimeActionInfo.getQrestrictionAction());
            qTimeActionEventData.setReasonCode(ObjectIdentifier.fetchValue(qTimeActionInfo.getReasonCodeID()));
            qTimeActionEventData.setActionRouteID(ObjectIdentifier.fetchValue(qTimeActionInfo.getActionRouteID()));
            qTimeActionEventData.setOperationNumber(qTimeActionInfo.getActionOperationNumber());
            qTimeActionEventData.setTiming(qTimeActionInfo.getFutureHoldTiming());
            qTimeActionEventData.setMainProcessDefinitionID(ObjectIdentifier.fetchValue(qTimeActionInfo.getReworkRouteID()));
            qTimeActionEventData.setMessageDefinitionID(ObjectIdentifier.fetchValue(qTimeActionInfo.getMessageID()));
            qTimeActionEventData.setCustomField(qTimeActionInfo.getCustomField());
            if (CimStringUtils.equals(qTimeActionInfo.getWatchDogRequired(), "Y")) {
                qTimeActionEventData.setWatchdogRequired(true);
            } else if (CimStringUtils.equals(qTimeActionInfo.getWatchDogRequired(), "N")) {
                qTimeActionEventData.setWatchdogRequired(false);
            }
            if (CimStringUtils.equals(qTimeActionInfo.getActionDoneFlag(), "Y")) {
                qTimeActionEventData.setActionDone(true);
            } else if (CimStringUtils.equals(qTimeActionInfo.getActionDoneFlag(), "N")) {
                qTimeActionEventData.setActionDone(false);
            }
            return qTimeActionEventData;
        }).collect(Collectors.toList());
        anEventRecord.setActions(actions);
        //---------------------------------------------
        // Create QTime event
        //---------------------------------------------
        eventManager.createEvent(anEventRecord, CimQTimeEvent.class);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param strStartCassette
     * @param controlJobID
     * @param equipmentID
     * @param claimMemo
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/5/11 11:27
     */
    @Override
    public void collectedDataEventMake(Infos.ObjCommon strObjCommonIn,
                                       String transactionID,
                                       List<Infos.StartCassette> strStartCassette,
                                       ObjectIdentifier controlJobID,
                                       ObjectIdentifier equipmentID,
                                       String claimMemo) {
        Event.CollectedDataEventRecord anEventRecord = new Event.CollectedDataEventRecord();
        anEventRecord.setProcessedLots(new ArrayList<>());
        int scLen = CimArrayUtils.getSize(strStartCassette);

        int i, j, k, l;

        for (i = 0; i < scLen; i++) {
            List<Infos.LotInCassette> strLotInCassette = strStartCassette.get(i).getLotInCassetteList();
            int lenLotInCassette = CimArrayUtils.getSize(strLotInCassette);

            for (j = 0; j < lenLotInCassette; j++) {
                Infos.LotInCassette lotInCassette = strLotInCassette.get(j);
                if (!CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {
                    continue;
                }

                List<Infos.DataCollectionInfo> strDCDef = lotInCassette.getStartRecipe().getDcDefList();
                int lenDCDef = CimArrayUtils.getSize(strDCDef);
                if (0 == lenDCDef) {
                    continue;
                }

                CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());

                boolean skipFlag = false;

                for (k = 0; k < lenDCDef; k++) {
                    if (ObjectIdentifier.isEmpty(strDCDef.get(k).getDataCollectionDefinitionID())) {
                        skipFlag = true;
                        break;
                    }
                }
                if (skipFlag) {
                    continue;
                }

                Event.LotEventData measuredLotData = new Event.LotEventData();
                anEventRecord.setMeasuredLotData(measuredLotData);
                measuredLotData.setLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                measuredLotData.setLotType(lotInCassette.getLotType());
                measuredLotData.setCassetteID(ObjectIdentifier.fetchValue(strStartCassette.get(i).getCassetteID()));


                Event.LotEventData lotEventData = aLot.getEventData();

                measuredLotData.setLotStatus(lotEventData.getLotStatus());
                measuredLotData.setCustomerID(lotEventData.getCustomerID());
                measuredLotData.setPriorityClass(lotEventData.getPriorityClass());
                measuredLotData.setProductID(lotEventData.getProductID());
                measuredLotData.setOriginalWaferQuantity(lotEventData.getOriginalWaferQuantity());
                measuredLotData.setCurrentWaferQuantity(lotEventData.getCurrentWaferQuantity());
                measuredLotData.setProductWaferQuantity(lotEventData.getProductWaferQuantity());
                measuredLotData.setControlWaferQuantity(lotEventData.getControlWaferQuantity());
                measuredLotData.setHoldState(lotEventData.getHoldState());
                measuredLotData.setBankID(lotEventData.getBankID());
                measuredLotData.setRouteID(lotEventData.getRouteID());
                measuredLotData.setOperationNumber(lotEventData.getOperationNumber());
                measuredLotData.setOperationID(lotEventData.getOperationID());
                measuredLotData.setOperationPassCount(lotEventData.getOperationPassCount());
                measuredLotData.setObjrefPOS(lotEventData.getObjrefPOS());
                measuredLotData.setWaferHistoryTimeStamp(lotEventData.getWaferHistoryTimeStamp());
                measuredLotData.setObjrefPO(lotEventData.getObjrefPO());
                measuredLotData.setObjrefMainPF(lotEventData.getObjrefMainPF());
                measuredLotData.setObjrefModulePOS(lotEventData.getObjrefModulePOS());

                anEventRecord.setEquipmentID(equipmentID.getValue());
                anEventRecord.setLogicalRecipeID(
                        ObjectIdentifier.fetchValue(lotInCassette.getStartRecipe().getLogicalRecipeID()));
                anEventRecord.setMachineRecipeID(
                        ObjectIdentifier.fetchValue(lotInCassette.getStartRecipe().getMachineRecipeID()));
                anEventRecord.setPhysicalRecipeID(lotInCassette.getStartRecipe().getPhysicalRecipeID());


                CimMonitorGroup aMonGrp = aLot.getControlMonitorGroup();

                int mLen = 0;
                if (aMonGrp == null) {
                    anEventRecord.setMonitorGroupID("");
                } else {
                    anEventRecord.setMonitorGroupID(aMonGrp.getIdentifier());

                    List<ProductDTO.MonitoredLot> mLotSeq = aMonGrp.allLots();

                    mLen = CimArrayUtils.getSize(mLotSeq);
                    List<Event.ProcessedLotEventData> processedLots = new ArrayList<>();
                    anEventRecord.setProcessedLots(processedLots);


                    for (l = 0; l < mLen; l++) {

                        Event.ProcessedLotEventData processedLot = new Event.ProcessedLotEventData();
                        processedLots.add(processedLot);
                        processedLot.setProcessLotID(ObjectIdentifier.fetchValue((mLotSeq).get(l).getLotID()));

                        CimProcessOperation mPO = baseCoreFactory.getBO(CimProcessOperation.class, mLotSeq.get(l).getProcessOperation());

                        if (mPO != null) {
                            ProcessDTO.PosProcessOperationEventData monitorGRP_POEventData = mPO.getEventData();

                            processedLot.setProcessRouteID(monitorGRP_POEventData.getRouteID());
                            processedLot.setProcessOperationNumber(monitorGRP_POEventData.getOperationNumber());
                            processedLot.setProcessOperationPassCount(CimNumberUtils.longValue(monitorGRP_POEventData.getOperationPassCount()));
                            processedLot.setProcessObjrefPO(monitorGRP_POEventData.getObjrefPO());
                        } else {
                            processedLot.setProcessRouteID("");
                            processedLot.setProcessOperationNumber("");
                            processedLot.setProcessOperationPassCount(0L);
                            processedLot.setProcessObjrefPO("");
                        }

                    }

                }

                CimProcessFlowContext aPFX = aLot.getProcessFlowContext();

                Validations.check(null == aPFX, retCodeConfig.getNotFoundPfx());

                int fpcCnt = 0;
                int numFPC = 0;
                int foundFPCCnt = 0;
                int waferCntFromStartCassette = 0;
                int corresOpeLenforFPC = 0;
                String singleCorrOpeFPC = null;
                List<String> dummyFPC_IDs;
                dummyFPC_IDs = new ArrayList<>();
                ObjectIdentifier dummyID = null;
                ObjectIdentifier mainPDID = null;
                mainPDID = ObjectIdentifier.buildWithValue(lotEventData.getRouteID());
                String dummy = null;
                Inputs.ObjFPCInfoGetDRIn objFPCInfoGetDRIn = new Inputs.ObjFPCInfoGetDRIn();
                objFPCInfoGetDRIn.setLotID(lotInCassette.getLotID());
                objFPCInfoGetDRIn.setFPCIDs(dummyFPC_IDs);
                objFPCInfoGetDRIn.setMainPDID(mainPDID);
                objFPCInfoGetDRIn.setMainOperNo(measuredLotData.getOperationNumber());
                objFPCInfoGetDRIn.setEquipmentID(equipmentID);
                objFPCInfoGetDRIn.setWaferIDInfoGetFlag(true);
                objFPCInfoGetDRIn.setDcSpecItemInfoGetFlag(true);
                objFPCInfoGetDRIn.setRecipeParmInfoGetFlag(false);
                objFPCInfoGetDRIn.setReticleInfoGetFlag(false);
                // step1 - FPC_info_GetDR__101
                List<Infos.FPCInfo> strFPCInfoList = fpcMethod.fpcInfoGetDR(strObjCommonIn, objFPCInfoGetDRIn);
                fpcCnt = CimArrayUtils.getSize(strFPCInfoList);
                List<Infos.LotWafer> strLotWafer = lotInCassette.getLotWaferList();
                waferCntFromStartCassette = CimArrayUtils.getSize(strLotWafer);
                boolean foundFPCFlag = false;
                for (numFPC = 0; numFPC < fpcCnt; numFPC++) {
                    Infos.FPCInfo strFPCInfo = strFPCInfoList.get(numFPC);
                    for (int inParaWCnt = 0; inParaWCnt < waferCntFromStartCassette; inParaWCnt++) {
                        if (CimStringUtils.equals(strFPCInfo.getLotWaferInfoList().get(0).getWaferID().getValue(),
                                lotInCassette.getLotWaferList().get(inParaWCnt).getWaferID().getValue())) {
                            corresOpeLenforFPC = CimArrayUtils.getSize(strFPCInfo.getCorrespondingOperationInfoList());
                            singleCorrOpeFPC = (strFPCInfo.getCorrespondOperationNumber());
                            foundFPCCnt = numFPC;
                            foundFPCFlag = true;
                            break;
                        }
                    }
                    if (foundFPCFlag) {
                        break;
                    }
                }
                Long envMultiCorrOpe = CimLongUtils.longValue(StandardProperties.OM_EDC_MULTI_CORRESPOND_FLAG.getValue());
                if (corresOpeLenforFPC > 0 || CimStringUtils.length(singleCorrOpeFPC) > 0) {
                    if (0 < envMultiCorrOpe) {
                        List<Event.ProcessedLotEventData> processedLots = anEventRecord.getProcessedLots();
                        anEventRecord.setProcessedLots(processedLots);
                        CimProcessOperation aFPCMultiCorrPO;
                        ProcessDTO.PosProcessOperationEventData multiFPCcorrespondingPOEventData;

                        for (l = 0; l < corresOpeLenforFPC; l++) {
                            aFPCMultiCorrPO = aPFX.findProcessOperationForOperationNumberBefore(strFPCInfoList.get(foundFPCCnt).getCorrespondingOperationInfoList().get(l).getCorrespondingOperationNumber());
                            Validations.check(null == aFPCMultiCorrPO, retCodeConfig.getNotFoundProcessOperation());

                            multiFPCcorrespondingPOEventData = aFPCMultiCorrPO.getEventData();
                            Event.ProcessedLotEventData processedLot = new Event.ProcessedLotEventData();
                            processedLots.add(processedLot);
                            processedLot.setProcessLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                            processedLot.setProcessRouteID(multiFPCcorrespondingPOEventData.getRouteID());
                            processedLot.setProcessOperationNumber(multiFPCcorrespondingPOEventData.getOperationNumber());
                            processedLot.setProcessOperationPassCount(multiFPCcorrespondingPOEventData.getOperationPassCount() * 1L);
                            processedLot.setProcessObjrefPO(multiFPCcorrespondingPOEventData.getObjrefPO());

                        }
                    } else {
                        List<Event.ProcessedLotEventData> processedLots = anEventRecord.getProcessedLots();
                        CimProcessOperation aFPCSingleCorrPO;
                        ProcessDTO.PosProcessOperationEventData singleFPCcorrespondingPOEventData;
                        aFPCSingleCorrPO = aPFX.findProcessOperationForOperationNumberBefore(singleCorrOpeFPC);
                        Validations.check(null == aFPCSingleCorrPO, retCodeConfig.getNotFoundProcessOperation());

                        singleFPCcorrespondingPOEventData = aFPCSingleCorrPO.getEventData();

                        Event.ProcessedLotEventData processedLot = new Event.ProcessedLotEventData();
                        processedLots.add(processedLot);
                        processedLot.setProcessLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                        processedLot.setProcessRouteID(singleFPCcorrespondingPOEventData.getRouteID());
                        processedLot.setProcessOperationNumber(singleFPCcorrespondingPOEventData.getOperationNumber());
                        processedLot.setProcessOperationPassCount(singleFPCcorrespondingPOEventData.getOperationPassCount() * 1L);
                        processedLot.setProcessObjrefPO(singleFPCcorrespondingPOEventData.getObjrefPO());

                    }
                } else {
                    List<CimProcessOperation> aCorresOpeList = aPFX.getCorrespondingProcessOperations();
                    int corresOpeLen = CimArrayUtils.getSize(aCorresOpeList);
                    if (corresOpeLen > 0) {

                        List<Event.ProcessedLotEventData> processedLots = new ArrayList<>();
                        ProcessDTO.PosProcessOperationEventData correspondingPOEventData;
                        anEventRecord.setProcessedLots(processedLots);
                        for (l = 0; l < corresOpeLen; l++) {
                            correspondingPOEventData = (aCorresOpeList).get(l).getEventData();

                            Event.ProcessedLotEventData processedLot = new Event.ProcessedLotEventData();
                            processedLots.add(processedLot);
                            processedLot.setProcessLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                            processedLot.setProcessRouteID(correspondingPOEventData.getRouteID());
                            processedLot.setProcessOperationNumber(correspondingPOEventData.getOperationNumber());
                            processedLot.setProcessOperationPassCount(correspondingPOEventData.getOperationPassCount() * 1L);
                            processedLot.setProcessObjrefPO(correspondingPOEventData.getObjrefPO());

                        }
                    } else {
						List<Event.ProcessedLotEventData> processedLots = new ArrayList<>();
						Event.ProcessedLotEventData processedLot = new Event.ProcessedLotEventData();
						processedLots.add(processedLot);
						processedLot.setProcessLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
						processedLot.setProcessRouteID(measuredLotData.getRouteID());
						processedLot.setProcessOperationNumber(measuredLotData.getOperationNumber());
						processedLot.setProcessOperationPassCount(measuredLotData.getOperationPassCount());
						processedLot.setProcessObjrefPO(measuredLotData.getObjrefPO());
						if (CollectionUtil.isNotEmpty(processedLots)) {
                            // 当前lot + product Lot
							anEventRecord.getProcessedLots().addAll(processedLots);
						} else {
							anEventRecord.setProcessedLots(processedLots);
						}
                    }
                }

                anEventRecord.setEventCommon(setEventData(strObjCommonIn, claimMemo));

                eventManager.createEvent(anEventRecord, CimCollectedDataEvent.class);
            }
        }
    }

    @Override
    public void lotFlowBatchEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier lotID, ObjectIdentifier flowBatchID, String claimMemo) {
        Event.LotFlowBatchEventRecord anEventRecord = new Event.LotFlowBatchEventRecord();
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
        Event.LotEventData lotEventData = aLot.getEventData();
        anEventRecord.setLotData(lotEventData);
        anEventRecord.setFlowBatchID(ObjectIdentifier.fetchValue(flowBatchID));
        anEventRecord.setEventCommon(setEventData(objCommon, claimMemo));
        // Put into the queue of PosEventManager
        CimProcessOperation curPO = aLot.getProcessOperation();
        Validations.check(null == curPO, new OmCode(retCodeConfig.getNotFoundOperation(), ""));

        CimFlowBatch aLotFlowBatch = aLot.getFlowBatch();
        if (aLotFlowBatch != null) {
            CimProcessFlowContext aPFX = aLot.getProcessFlowContext();
            Validations.check(null == aPFX, new OmCode(retCodeConfig.getNotFoundPfx(), "*****"));

            CimProcessOperationSpecification aPOS = aPFX.findFlowBatchTargetOperationSpecification();
            if (aPOS != null) {
                String moduleNo = curPO.getModuleNumber();
                String moduleOpeNo = aPOS.getOperationNumber();
                if (!CimStringUtils.isEmpty(moduleNo) && !CimStringUtils.isEmpty(moduleOpeNo)) {
                    anEventRecord.setTargetOperationNumber(moduleNo + "." + moduleOpeNo);
                }
            }
            CimMachine aReservedMachine = aLotFlowBatch.getMachine();
            if (aReservedMachine != null) {
                ObjectIdentifier equipmentID = new ObjectIdentifier(aReservedMachine.getIdentifier(), aReservedMachine.getPrimaryKey());
                anEventRecord.setTargetEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
            }
        }
        eventManager.createEvent(anEventRecord, CimLotFlowBatchEvent.class);
    }

    @Override
    public void experimentalLotRegistEventMake(Infos.ObjCommon objCommon, String transactionID, String testMemo, Infos.ExperimentalLotRegistInfo experimentalLotRegistInfo) {
        Event.PlannedSplitEventRecord aEventRecord = new Event.PlannedSplitEventRecord();
        aEventRecord.setEventCommon(setEventData(objCommon, testMemo));
        //add psmJobID for history
        aEventRecord.setPsmJobID(experimentalLotRegistInfo.getPsmJobID());
        aEventRecord.setRunCardID(experimentalLotRegistInfo.getRunCardID());
        aEventRecord.setAction(experimentalLotRegistInfo.getAction());
        aEventRecord.setLotFamilyID(ObjectIdentifier.fetchValue(experimentalLotRegistInfo.getLotFamilyID()));
        aEventRecord.setSplitRouteID(ObjectIdentifier.fetchValue(experimentalLotRegistInfo.getSplitRouteID()));
        aEventRecord.setSplitOperationNumber(experimentalLotRegistInfo.getSplitOperationNumber());
        aEventRecord.setOriginalRouteID(ObjectIdentifier.fetchValue(experimentalLotRegistInfo.getOriginalRouteID()));
        aEventRecord.setOriginalOperationNumber(experimentalLotRegistInfo.getOriginalOperationNumber());
        aEventRecord.setActionEMail(experimentalLotRegistInfo.isActionEMail());
        aEventRecord.setActionHold(experimentalLotRegistInfo.isActionHold());
        List<Infos.ExperimentalLotRegist> experimentalLotRegistList = experimentalLotRegistInfo.getStrExperimentalLotRegistSeq();
        List<Event.SplitSubRouteEventData> subRoutes = new ArrayList<>();
        aEventRecord.setSubRoutes(subRoutes);
        for (Infos.ExperimentalLotRegist experimentalLotRegist : experimentalLotRegistList) {
            Event.SplitSubRouteEventData splitSubRouteEventData = new Event.SplitSubRouteEventData();
            subRoutes.add(splitSubRouteEventData);
            splitSubRouteEventData.setSubRouteID(ObjectIdentifier.fetchValue(experimentalLotRegist.getSubRouteID()));
            splitSubRouteEventData.setReturnOperationNumber(experimentalLotRegist.getReturnOperationNumber());
            splitSubRouteEventData.setMergeOperationNumber(experimentalLotRegist.getMergeOperationNumber());
            splitSubRouteEventData.setParentLotID("");
            splitSubRouteEventData.setChildLotID("");
            splitSubRouteEventData.setMemo(experimentalLotRegist.getMemo());
            List<Event.SplitedWaferEventData> wafers = new ArrayList<>();
            splitSubRouteEventData.setWafers(wafers);
            List<ObjectIdentifier> waferIDs = experimentalLotRegist.getWaferIDs();
            for (ObjectIdentifier waferID : waferIDs) {
                Event.SplitedWaferEventData splitedWaferEventData = new Event.SplitedWaferEventData();
                wafers.add(splitedWaferEventData);
                splitedWaferEventData.setWaferID(ObjectIdentifier.fetchValue(waferID));
                splitedWaferEventData.setSuccessFlag("");
            }
        }
        //------------------------------------------------------------------------//
        // Put into the queue of PosEventManager                                  //
        //------------------------------------------------------------------------//
        eventManager.createEvent(aEventRecord, CimPlannedSplitEvent.class);
    }

    @Override
    public void processHoldEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier routeID, String operationNumber, ObjectIdentifier productID, boolean withExecHoldFlag, String holdType, ObjectIdentifier reasonCodeID, String entryType, String claimMemo) {
        Event.ProcessHoldEventRecord anEventRecord = new Event.ProcessHoldEventRecord();
        /*---------------------------------------*/
        /*   Set Lot Process Hold Event Data     */
        /*---------------------------------------*/
        if (CimStringUtils.equals(entryType, BizConstant.SP_ENTRYTYPE_ENTRY) || CimStringUtils.equals(entryType, BizConstant.SP_ENTRYTYPE_CANCEL)) {
            anEventRecord.setEntryType(entryType);
        }
        anEventRecord.setRouteID(ObjectIdentifier.fetchValue(routeID));
        anEventRecord.setOperationNumber(operationNumber);
        anEventRecord.setProductID(ObjectIdentifier.fetchValue(productID));
        anEventRecord.setWithExecHoldFlag(withExecHoldFlag);
        anEventRecord.setHoldType(holdType);
        anEventRecord.setReasonCodeID(reasonCodeID);
        //Set OperationID
        Inputs.ProcessOperationListForRoute strProcessOperationListForRouteIn = new Inputs.ProcessOperationListForRoute();
        strProcessOperationListForRouteIn.setRouteID(routeID);
        strProcessOperationListForRouteIn.setOperationID(new ObjectIdentifier(""));
        strProcessOperationListForRouteIn.setOperationNumber(operationNumber);
        strProcessOperationListForRouteIn.setPdType("");
        strProcessOperationListForRouteIn.setSearchCount(1);
        List<Infos.OperationNameAttributes> processOperationListForRouteResult = null;
        try {
            processOperationListForRouteResult = processMethod.processOperationListForRoute(objCommon, strProcessOperationListForRouteIn);
            anEventRecord.setOperationID(ObjectIdentifier.fetchValue(processOperationListForRouteResult.get(0).getOperationID()));
        } catch (ServiceException ex) {
            anEventRecord.setOperationID(BizConstant.SP_DEFAULT_CHAR);
        }
        anEventRecord.setEventCommon(setEventData(objCommon, claimMemo));
        eventManager.createEvent(anEventRecord, CimProcessHoldEvent.class);
    }

    @Override
    public void autoDispatchControlEventMake(Infos.ObjCommon objCommon, Inputs.AutoDispatchControlEventMakeIn autoDispatchControlEventMakeIn) {
        ObjectIdentifier lotID = autoDispatchControlEventMakeIn.getLotID();
        Infos.AutoDispatchControlUpdateInfo autoDispatchControlUpdateInfo = autoDispatchControlEventMakeIn.getAutoDispatchControlUpdateInfo();
        //---------------------------------------------
        // Prepare AutoDispatchControl event
        //---------------------------------------------
        Event.AutoDispatchControlEventRecord anEventRecord = new Event.AutoDispatchControlEventRecord();
        anEventRecord.setEventCommon(setEventData(objCommon, autoDispatchControlEventMakeIn.getClaimMemo()));

        anEventRecord.setLotID(ObjectIdentifier.fetchValue(lotID));
        anEventRecord.setAction(autoDispatchControlUpdateInfo.getUpdateMode());
        anEventRecord.setRouteID(ObjectIdentifier.fetchValue(autoDispatchControlUpdateInfo.getRouteID()));
        anEventRecord.setOperationNumber(autoDispatchControlUpdateInfo.getOperationNumber());
        anEventRecord.setSingleTriggerFlag(autoDispatchControlUpdateInfo.isSingleTriggerFlag());
        anEventRecord.setDescription(autoDispatchControlUpdateInfo.getDescription());
        //---------------------------------------------
        // Create AutoDispatchControl event
        //--------------------------------------------
        eventManager.createEvent(anEventRecord, CimAutoDispatchControlEvent.class);
    }

    @Override
    public void equipmentFlowBatchMaxCountChangeEventMake(Infos.ObjCommon objCommon, Inputs.ObjEquipmentFlowBatchMaxCountChangeEventMakeIn eventMakeIn) {
        //--------------//
        //  Initialize  //
        //--------------//
        Event.EquipmentFlowBatchMaxCountChangeEventRecord eventRecord = new Event.EquipmentFlowBatchMaxCountChangeEventRecord();
        eventRecord.setEquipmentID(eventMakeIn.getEquipmentID().getValue());
        eventRecord.setNewFlowBatchMaxCount(CimNumberUtils.longValue(eventMakeIn.getFlowBatchMaxCount()));
        eventRecord.setEventCommon(setEventData(objCommon, eventMakeIn.getClaimMemo()));

        //-----------------//
        //  Put Event Data //
        //-----------------//
        eventManager.createEvent(eventRecord, CimEquipmentFlowBatchMaxCountChangeEvent.class);
    }

    /**
     * description: durableBankMoveEvent_Make
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableBankMoveEvent_Make_in
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/1 12:15
     */
    @Override
    public void durableBankMoveEventMake(Infos.ObjCommon strObjCommonIn, Infos.DurablebankmoveeventMakeIn strDurableBankMoveEvent_Make_in) {
        log.info("durableBankMoveEvent_Make");
        Event.DurableBankMoveEventRecord anEventRecord;
        Event.DurableEventData durableEventData = new Event.DurableEventData();
        CimBank aPrevBank = null;

        if (CimStringUtils.equals(strDurableBankMoveEvent_Make_in.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
            log.info("" + "durableCategory is Cassette");
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, strDurableBankMoveEvent_Make_in.getDurableID());
            durableEventData = aCassette.getEventData();
            aPrevBank = aCassette.getPreviousBank();

        } else if (CimStringUtils.equals(strDurableBankMoveEvent_Make_in.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
            log.info("" + "durableCategory is ReticlePod");
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, strDurableBankMoveEvent_Make_in.getDurableID());

            durableEventData = aReticlePod.getEventData();

            aPrevBank = aReticlePod.getPreviousBank();

        } else if (CimStringUtils.equals(strDurableBankMoveEvent_Make_in.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
            log.info("" + "durableCategory is Reticle");
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, strDurableBankMoveEvent_Make_in.getDurableID());

            durableEventData = aReticle.getEventData();

            aPrevBank = aReticle.getPreviousBank();

        }

        anEventRecord = new Event.DurableBankMoveEventRecord();
        anEventRecord.setDurableData(new Event.DurableEventData());
        anEventRecord.getDurableData().setDurableID(durableEventData.getDurableID());
        anEventRecord.getDurableData().setDurableCategory(durableEventData.getDurableCategory());
        anEventRecord.getDurableData().setDurableStatus(durableEventData.getDurableStatus());
        anEventRecord.getDurableData().setHoldState(durableEventData.getHoldState());
        anEventRecord.getDurableData().setBankID(durableEventData.getBankID());
        anEventRecord.getDurableData().setRouteID(durableEventData.getRouteID());
        anEventRecord.getDurableData().setOperationNumber(durableEventData.getOperationNumber());
        anEventRecord.getDurableData().setOperationID(durableEventData.getOperationID());
        anEventRecord.getDurableData().setOperationPassCount(durableEventData.getOperationPassCount());
        anEventRecord.getDurableData().setObjrefPOS(durableEventData.getObjrefPOS());
        anEventRecord.getDurableData().setObjrefPO(durableEventData.getObjrefPO());
        anEventRecord.getDurableData().setObjrefMainPF(durableEventData.getObjrefMainPF());
        anEventRecord.getDurableData().setObjrefModulePOS(durableEventData.getObjrefModulePOS());

        if (aPrevBank != null) {
            log.info("" + "aPrevBank is not nil");
            anEventRecord.setPreviousBankID(aPrevBank.getIdentifier());

        }

        anEventRecord.setEventCommon(setEventData(strObjCommonIn, strDurableBankMoveEvent_Make_in.getClaimMemo()));

        log.info("" + "call eventManager.createEvent");
        eventManager.createEvent(anEventRecord, CimDurableBankMoveEvent.class);
    }

    @Override
    public void processStatusEventMake(Infos.ObjCommon strObjCommonIn, String transactionID, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, ObjectIdentifier lotID,
                                       String actionCode, String claimMemo) {
        Event.ProcessStatusEventRecord procStatusEventRecord = new Event.ProcessStatusEventRecord();
        /*------------------------------------------*/
        /*   Set Process Status Change Event Data   */
        /*------------------------------------------*/
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
        /*===== get aPO =====*/
        CimProcessOperation aProcessOperation = aLot.getProcessOperation();
        Event.LotEventData lotEventData = aLot.getEventData();
        procStatusEventRecord.setLotData(lotEventData);
        procStatusEventRecord.setActionCode(actionCode);
        procStatusEventRecord.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
        /*===== operationMode =====*/
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(strObjCommonIn, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        int lenEqpPort = CimArrayUtils.getSize(eqpPortStatuses);
        for (int i = 0; i < lenEqpPort; i++) {
            Infos.EqpPortStatus eqpPortStatus = eqpPortStatuses.get(i);
            if (ObjectIdentifier.equalsWithValue(lotEventData.getCassetteID(), eqpPortStatus.getLoadedCassetteID())) {
                procStatusEventRecord.setOperationMode(eqpPortStatus.getOperationMode());
                break;
            }
        }
        /*===== set Recipe Info =====*/
        if (aProcessOperation != null) {
            /*===== logicalRecipe =====*/
            CimLogicalRecipe aPosLogicalRecipe = aProcessOperation.getAssignedLogicalRecipe();
            Validations.check(aPosLogicalRecipe == null, retCodeConfig.getNotFoundLogicalRecipe());
            procStatusEventRecord.setLogicalRecipeID(aPosLogicalRecipe.getIdentifier());
            /*===== machineRecipe =====*/
            CimMachineRecipe aPosMachineRecipe = aProcessOperation.getAssignedMachineRecipe();
            Validations.check(aPosMachineRecipe == null, retCodeConfig.getNotFoundMachineRecipe());
            procStatusEventRecord.setMachineRecipeID(aPosMachineRecipe.getIdentifier());
            /*===== physicalRecipe =====*/
            procStatusEventRecord.setPhysicalRecipeID(aProcessOperation.getAssignedPhysicalRecipe());
        }
        /*===== batchID =====*/
        CimFlowBatch aFlowBatch = aLot.getFlowBatch();
        if (aFlowBatch != null) {
            procStatusEventRecord.setBatchID(aFlowBatch.getIdentifier());
        }
        /*===== controlJobID =====*/
        procStatusEventRecord.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
        procStatusEventRecord.setEventCommon(setEventData(strObjCommonIn, claimMemo));
        eventManager.createEvent(procStatusEventRecord, CimProcessStatusEvent.class);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableControlJobStatusChangeEvent_Make_in
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/1 14:31
     */
    public void durableControlJobStatusChangeEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableControlJobStatusChangeEventMake strDurableControlJobStatusChangeEvent_Make_in) {
        String methodName = null;

        log.info("durableControlJobStatusChangeEvent_Make");

        Event.DurableControlJobStatusChangeEventRecord anEventRecord;

        anEventRecord = new Event.DurableControlJobStatusChangeEventRecord();
        anEventRecord.setDurableCtrlJobID(strDurableControlJobStatusChangeEvent_Make_in.getDurableControlJobID().getValue());
        anEventRecord.setDurableCtrlJobState(strDurableControlJobStatusChangeEvent_Make_in.getDurableControlJobStatus());
        log.info("" + "durableCtrlJobID     . " + anEventRecord.getDurableCtrlJobID());
        log.info("" + "durableCtrlJobState  . " + anEventRecord.getDurableCtrlJobState());

        CimDurableControlJob aDurableControlJob = baseCoreFactory.getBO(CimDurableControlJob.class, strDurableControlJobStatusChangeEvent_Make_in.getDurableControlJobID());

        MachineDO aMachine = null;
//        aMachine = cimDurableControlJob.getMachine(aDurableControlJob);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp());

        ObjectIdentifier equipmentID;
        equipmentID = ObjectIdentifier.build(aMachine.getMachineID(), aMachine.getId());

        anEventRecord.setEqpID(equipmentID.getValue());

        anEventRecord.setEqpDescription(aMachine.getMachineDescription());

        int durableLen = CimArrayUtils.getSize(strDurableControlJobStatusChangeEvent_Make_in.getStrStartDurables());
        anEventRecord.setDurables(new ArrayList<>());
        for (int i = 0; i < durableLen; i++) {
            anEventRecord.getDurables().add(new Event.DurableControlJobStatusChangeDurableEventData());
            anEventRecord.getDurables().get(i).setDurableCategory(strDurableControlJobStatusChangeEvent_Make_in.getDurableCategory());
            anEventRecord.getDurables().get(i).setDurableID(strDurableControlJobStatusChangeEvent_Make_in.getStrStartDurables().get(i).getDurableId().getValue());

            anEventRecord.getDurables().get(i).setMainPDID(strDurableControlJobStatusChangeEvent_Make_in.getStrStartDurables().get(i).getStartOperationInfo().getProcessFlowID().getValue());
            anEventRecord.getDurables().get(i).setOpeNo(strDurableControlJobStatusChangeEvent_Make_in.getStrStartDurables().get(i).getStartOperationInfo().getOperationNumber());
            anEventRecord.getDurables().get(i).setPdID(strDurableControlJobStatusChangeEvent_Make_in.getStrStartDurables().get(i).getStartOperationInfo().getOperationID().getValue());
            anEventRecord.getDurables().get(i).setOpePassCount(CimLongUtils.longValue(
                    strDurableControlJobStatusChangeEvent_Make_in.getStrStartDurables().get(i).getStartOperationInfo().getPassCount()
            ));
            Boolean processDefinitionFound = true;
            CimProcessDefinition aProcessDefinition = baseCoreFactory.getBO(CimProcessDefinition.class, strDurableControlJobStatusChangeEvent_Make_in.getStrStartDurables().get(i).getStartOperationInfo().getOperationID());
            if (!processDefinitionFound) {
                log.info("" + "PDObj is not found");
                anEventRecord.getDurables().get(i).setPdName("");
            } else {
                log.info("" + "Found PDObj");
                anEventRecord.getDurables().get(i).setPdName(aProcessDefinition.getProcessDefinitionName());

            }

        }

        anEventRecord.getEventCommon().setTransactionID(strDurableControlJobStatusChangeEvent_Make_in.getTransactionID());
        anEventRecord.getEventCommon().setEventTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp().toString());
        anEventRecord.getEventCommon().setEventShopDate(strObjCommonIn.getTimeStamp().getReportShopDate());
        anEventRecord.getEventCommon().setUserID(strObjCommonIn.getUser().getUserID().getValue());
        anEventRecord.getEventCommon().setEventMemo(strDurableControlJobStatusChangeEvent_Make_in.getClaimMemo());

        log.info("" + "transactionID   . " + anEventRecord.getEventCommon().getTransactionID());
        log.info("" + "eventTimeStamp  . " + anEventRecord.getEventCommon().getEventTimeStamp());
        log.info("" + "eventShopDate   . " + anEventRecord.getEventCommon().getEventShopDate());
        log.info("" + "userID          . " + anEventRecord.getEventCommon().getUserID());
        log.info("" + "eventMemo       . " + anEventRecord.getEventCommon().getEventMemo());

        eventManager.createEvent(anEventRecord, CimDurableControlJobStatusChangeEvent.class);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableHoldEvent_Make_in
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/2 10:08
     */
    public void durableHoldEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableHoldEventMakeIn strDurableHoldEvent_Make_in) {
        String methodName = null;

        log.info("durableHoldEvent_Make");

        Event.DurableHoldEventRecord anEventRecord;
        Event.DurableEventData durableEventData = new Event.DurableEventData();
        CimDurableProcessFlowContext durablePFX = null;
        int hldHstLen = CimArrayUtils.getSize(strDurableHoldEvent_Make_in.getStrHoldHistoryList());

        if (CimStringUtils.equals(strDurableHoldEvent_Make_in.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
            log.info("" + "durableCategory is Cassette");
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, strDurableHoldEvent_Make_in.getDurableID());
            durableEventData = aCassette.getEventData();
            durablePFX = aCassette.getDurableProcessFlowContext();

        } else if (CimStringUtils.equals(strDurableHoldEvent_Make_in.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
            log.info("" + "durableCategory is ReticlePod");
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, strDurableHoldEvent_Make_in.getDurableID());
            durableEventData = aReticlePod.getEventData();

            durablePFX = aReticlePod.getDurableProcessFlowContext();

        } else if (CimStringUtils.equals(strDurableHoldEvent_Make_in.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
            log.info("" + "durableCategory is ReticlePod");
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, strDurableHoldEvent_Make_in.getDurableID());
            durableEventData = aReticle.getEventData();

            durablePFX = aReticle.getDurableProcessFlowContext();

        }

        Validations.check(null == durablePFX, retCodeConfig.getNotFoundPfx());
        anEventRecord = new Event.DurableHoldEventRecord();
        anEventRecord.setEventCommon(new Event.EventData());
        anEventRecord.getEventCommon().setTransactionID(strDurableHoldEvent_Make_in.getTransactionID());
        anEventRecord.getEventCommon().setEventTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp().toString());
        anEventRecord.getEventCommon().setEventShopDate(strObjCommonIn.getTimeStamp().getReportShopDate());
        anEventRecord.getEventCommon().setUserID(strObjCommonIn.getUser().getUserID().getValue());

        anEventRecord.setDurableData(new Event.DurableEventData());
        anEventRecord.getDurableData().setDurableID(durableEventData.getDurableID());
        anEventRecord.getDurableData().setDurableCategory(durableEventData.getDurableCategory());
        anEventRecord.getDurableData().setDurableStatus(durableEventData.getDurableStatus());
        anEventRecord.getDurableData().setHoldState(durableEventData.getHoldState());
        anEventRecord.getDurableData().setBankID(durableEventData.getBankID());
        anEventRecord.getDurableData().setRouteID(durableEventData.getRouteID());
        anEventRecord.getDurableData().setOperationNumber(durableEventData.getOperationNumber());
        anEventRecord.getDurableData().setOperationID(durableEventData.getOperationID());
        anEventRecord.getDurableData().setOperationPassCount(durableEventData.getOperationPassCount());
        anEventRecord.getDurableData().setObjrefPOS(durableEventData.getObjrefPOS());
        anEventRecord.getDurableData().setObjrefPO(durableEventData.getObjrefPO());
        anEventRecord.getDurableData().setObjrefMainPF(durableEventData.getObjrefMainPF());
        anEventRecord.getDurableData().setObjrefModulePOS(durableEventData.getObjrefModulePOS());

        CimDurableProcessOperation aPrevProcessOperation = durablePFX.getPreviousProcessOperation();
        anEventRecord.setPreviousOperationPassCount(0L);
        if (aPrevProcessOperation != null) {
            log.info("durableHoldEvent_Make" + "aPrevProcessOperation != null ");

            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevProcessOperation.getEventData();

            anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
            anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount(CimLongUtils.longValue(prevPOEventData.getOperationPassCount()));
            anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
            anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
        }

        log.info("durableHoldEvent_Make" + "ArrayUtils.getSize(strDurableHoldEvent_Make_in.getStrHoldHistoryList()) = " + hldHstLen);
        anEventRecord.setHoldRecords(new ArrayList<>());

        for (int hldHstSeq = 0; hldHstSeq < hldHstLen; hldHstSeq++) {
            anEventRecord.getHoldRecords().add(new Event.DurableHoldEventData());
            anEventRecord.getHoldRecords().get(hldHstSeq).setMovementFlag(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getMovementFlag());
            anEventRecord.getHoldRecords().get(hldHstSeq).setChangeStateFlag(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getChangeStateFlag());
            anEventRecord.getHoldRecords().get(hldHstSeq).setHoldType(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getHoldType());
            anEventRecord.getHoldRecords().get(hldHstSeq).setHoldReasonCodeID(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getHoldReasonCode());
            anEventRecord.getHoldRecords().get(hldHstSeq).setHoldUserID(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getHoldPerson().getValue());
            anEventRecord.getHoldRecords().get(hldHstSeq).setHoldTimeStamp(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getHoldTime());
            anEventRecord.getHoldRecords().get(hldHstSeq).setResponsibleOperationFlag(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getResponsibleOperationFlag());
            anEventRecord.getHoldRecords().get(hldHstSeq).setResponsibleOperationExistFlag(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getResponsibleOperationExistFlag());
            anEventRecord.getHoldRecords().get(hldHstSeq).setResponsibleRouteID(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getResponsibleRouteID());
            anEventRecord.getHoldRecords().get(hldHstSeq).setResponsibleOperationNumber(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getResponsibleOperationNumber());
            anEventRecord.getHoldRecords().get(hldHstSeq).setResponsibleOperationName(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getResponsibleOperationName());

            if (CimStringUtils.length(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getReleaseClaimMemo()) > 0) {
                anEventRecord.getHoldRecords().get(hldHstSeq).setHoldClaimMemo(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getReleaseClaimMemo());
            } else {
                anEventRecord.getHoldRecords().get(hldHstSeq).setHoldClaimMemo(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(hldHstSeq).getHoldClaimMemo());
            }
        }

        if (hldHstLen > 0) {
            anEventRecord.setReleaseReasonCodeID(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(0).getReleaseReasonCode());

            if (CimStringUtils.length(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(0).getReleaseClaimMemo()) > 0) {
                anEventRecord.getEventCommon().setEventMemo(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(0).getReleaseClaimMemo());
            } else {
                anEventRecord.getEventCommon().setEventMemo(strDurableHoldEvent_Make_in.getStrHoldHistoryList().get(0).getHoldClaimMemo());
            }
        }

        eventManager.createEvent(anEventRecord, CimDurableHoldEvent.class);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationCompleteEventMakeOpeCompIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/2 13:15
     */
    public void durableOperationCompleteEventMakeOpeComp(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOperationCompleteEventMakeOpeCompIn strDurableOperationCompleteEventMakeOpeCompIn) {
        String methodName = null;

        log.info("durableOperationCompleteEventMakeOpeComp");

        Infos.DurableOperationCompleteEventMakeOpeCompIn strInParm = strDurableOperationCompleteEventMakeOpeCompIn;

        Event.DurableOperationCompleteEventRecord anEventRecord;

        anEventRecord = new Event.DurableOperationCompleteEventRecord();
        anEventRecord.setEventCommon(new Event.EventData());
        anEventRecord.getEventCommon().setTransactionID(strObjCommonIn.getTransactionID());
        anEventRecord.getEventCommon().setEventTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp().toString());
        anEventRecord.getEventCommon().setEventShopDate(strObjCommonIn.getTimeStamp().getReportShopDate());
        anEventRecord.getEventCommon().setUserID(strObjCommonIn.getUser().getUserID().getValue());
        anEventRecord.getEventCommon().setEventMemo(strInParm.getClaimMemo());

        anEventRecord.setEquipmentID(strInParm.getEquipmentID().getValue());
        anEventRecord.setOperationMode(strInParm.getOperationMode());

        anEventRecord.setDurableControlJobID(strInParm.getDurableControlJobID().getValue());

        anEventRecord.setLogicalRecipeID(strInParm.getStrDurableStartRecipe().getLogicalRecipeId().getValue());
        anEventRecord.setMachineRecipeID(strInParm.getStrDurableStartRecipe().getMachineRecipeId().getValue());
        anEventRecord.setPhysicalRecipeID(strInParm.getStrDurableStartRecipe().getPhysicalRecipeId());

        int paramLen = CimArrayUtils.getSize(strInParm.getStrDurableStartRecipe().getStartRecipeParameterS());
        anEventRecord.setRecipeParameters(new ArrayList<>());
        for (int paramCnt = 0; paramCnt < paramLen; paramCnt++) {
            log.info("" + "ArrayUtils.getSize(loop to strInParm.getStrDurableStartRecipe().getStrStartRecipeParameter())" + paramCnt);
            anEventRecord.getRecipeParameters().add(new Event.RecipeParmEventData());
            anEventRecord.getRecipeParameters().get(paramCnt).setParameterName(strInParm.getStrDurableStartRecipe().getStartRecipeParameterS().get(paramCnt).getParameterName());
            anEventRecord.getRecipeParameters().get(paramCnt).setParameterValue(strInParm.getStrDurableStartRecipe().getStartRecipeParameterS().get(paramCnt).getParameterValue());
        }

        anEventRecord.setDurableData(new Event.DurableEventData());
        for (int durableCnt = 0; durableCnt < CimArrayUtils.getSize(strInParm.getStrStartDurables()); durableCnt++) {
            log.info("" + "ArrayUtils.getSize(loop to strInParm.getStrStartDurables())" + durableCnt);

            anEventRecord.getDurableData().setDurableID(strInParm.getStrStartDurables().get(durableCnt).getDurableId().getValue());
            anEventRecord.getDurableData().setDurableCategory(strInParm.getDurableCategory());

            CimCassette aCassette;
            CimReticlePod aReticlePod;
            CimProcessDurable aReticle;

            Event.DurableEventData durableEventData = null;
            CimDurableProcessFlowContext durablePFX = null;
            CimDurableProcessOperation aPrevProcessOperation = null;
            if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
                log.info("" + "durableCategory == SP_DURABLECAT_CASSETTE");
                aCassette = baseCoreFactory.getBO(CimCassette.class, strInParm.getStrStartDurables().get(durableCnt).getDurableId());

                durableEventData = aCassette.getEventData();

                durablePFX = aCassette.getDurableProcessFlowContext();

            } else if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
                log.info("" + "durableCategory == SP_DURABLECAT_RETICLEPOD");
                aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, strInParm.getStrStartDurables().get(durableCnt).getDurableId());
                durableEventData = aReticlePod.getEventData();

                durablePFX = aReticlePod.getDurableProcessFlowContext();

            } else if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
                log.info("" + "durableCategory == SP_DURABLECAT_RETICLE");
                aReticle = baseCoreFactory.getBO(CimProcessDurable.class, strInParm.getStrStartDurables().get(durableCnt).getDurableId());
                durableEventData = aReticle.getEventData();

                durablePFX = aReticle.getDurableProcessFlowContext();

            } else {
                log.info("" + "durableCategory is invalid" + strInParm.getDurableCategory());
                Validations.check(true, retCodeConfig.getInvalidDurableCategory());
            }

            Validations.check(null == durablePFX, retCodeConfig.getNotFoundPfx());

            if (durableEventData != null) {
                anEventRecord.getDurableData().setDurableID(durableEventData.getDurableID());
                anEventRecord.getDurableData().setDurableCategory(durableEventData.getDurableCategory());
                anEventRecord.getDurableData().setDurableStatus(durableEventData.getDurableStatus());
                anEventRecord.getDurableData().setHoldState(durableEventData.getHoldState());
                anEventRecord.getDurableData().setBankID(durableEventData.getBankID());
                anEventRecord.getDurableData().setRouteID(durableEventData.getRouteID());
                anEventRecord.getDurableData().setOperationNumber(durableEventData.getOperationNumber());
                anEventRecord.getDurableData().setOperationID(durableEventData.getOperationID());
                anEventRecord.getDurableData().setOperationPassCount(durableEventData.getOperationPassCount());
                anEventRecord.getDurableData().setObjrefPOS(durableEventData.getObjrefPOS());
                anEventRecord.getDurableData().setObjrefPO(durableEventData.getObjrefPO());
                anEventRecord.getDurableData().setObjrefMainPF(durableEventData.getObjrefMainPF());
                anEventRecord.getDurableData().setObjrefModulePOS(durableEventData.getObjrefModulePOS());
            }

            aPrevProcessOperation = durablePFX.getPreviousProcessOperation();

            if (aPrevProcessOperation != null) {
                log.info("durableHoldEventMake" + "aPrevProcessOperation != null ");

                ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevProcessOperation.getEventData();

                anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
                anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
                anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
                anEventRecord.setPreviousOperationPassCount(CimLongUtils.longValue(prevPOEventData.getOperationPassCount()));
                anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
                anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
                anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
            }
            eventManager.createEvent(anEventRecord, CimDurableOperationCompleteEvent.class);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param paramIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/2 13:36
     */
    public void durableOperationMoveEventMakeGatePass(Infos.ObjCommon strObjCommonIn,
                                                      Infos.DurableOperationMoveEventMakeGatePassIn paramIn) {
        Validations.check(null == strObjCommonIn || null == paramIn, retCodeConfig.getInvalidInputParam());
        ObjectIdentifier durableID = paramIn.getDurableID();

        log.info("durableOperationMoveEventMakeGatePass");

        Event.DurableOperationMoveEventRecord anEventRecord;
        Event.DurableEventData durableEventData = new Event.DurableEventData();
        CimDurableProcessFlowContext aDurablePFX = null;

        if (CimStringUtils.equals(paramIn.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
            log.info("" + "durableCategory is Cassette");
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            durableEventData = aCassette.getEventData();
            aDurablePFX = aCassette.getDurableProcessFlowContext();

        } else if (CimStringUtils.equals(paramIn.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
            log.info("" + "durableCategory is ReticlePod" + durableID);
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            durableEventData = aReticlePod.getEventData();
            aDurablePFX = aReticlePod.getDurableProcessFlowContext();

        } else if (CimStringUtils.equals(paramIn.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
            log.info("" + "durableCategory is Reticle");
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            durableEventData = aReticle.getEventData();
            aDurablePFX = aReticle.getDurableProcessFlowContext();
        }

        anEventRecord = new Event.DurableOperationMoveEventRecord();
        anEventRecord.setEventCommon(new Event.EventData());
        anEventRecord.getEventCommon().setTransactionID(paramIn.getTransactionID());
        anEventRecord.getEventCommon().setEventTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp().toString());
        anEventRecord.getEventCommon().setEventShopDate(strObjCommonIn.getTimeStamp().getReportShopDate());
        anEventRecord.getEventCommon().setUserID(strObjCommonIn.getUser().getUserID().getValue());
        anEventRecord.getEventCommon().setEventMemo(paramIn.getClaimMemo());

        anEventRecord.setDurableData(new Event.DurableEventData());
        anEventRecord.getDurableData().setDurableID(durableEventData.getDurableID());
        anEventRecord.getDurableData().setDurableCategory(durableEventData.getDurableCategory());
        anEventRecord.getDurableData().setDurableStatus(durableEventData.getDurableStatus());
        anEventRecord.getDurableData().setHoldState(durableEventData.getHoldState());
        anEventRecord.getDurableData().setBankID(durableEventData.getBankID());
        anEventRecord.getDurableData().setRouteID(durableEventData.getRouteID());
        anEventRecord.getDurableData().setOperationNumber(durableEventData.getOperationNumber());
        anEventRecord.getDurableData().setOperationID(durableEventData.getOperationID());
        anEventRecord.getDurableData().setOperationPassCount(durableEventData.getOperationPassCount());
        anEventRecord.getDurableData().setObjrefPOS(durableEventData.getObjrefPOS());
        anEventRecord.getDurableData().setObjrefPO(durableEventData.getObjrefPO());
        anEventRecord.getDurableData().setObjrefMainPF(durableEventData.getObjrefMainPF());
        anEventRecord.getDurableData().setObjrefModulePOS(durableEventData.getObjrefModulePOS());
        Validations.check(null == aDurablePFX, retCodeConfig.getNotFoundPfx());

        CimDurableProcessOperation aPrevDurableProcessOperation = aDurablePFX.getPreviousProcessOperation();
        if (aPrevDurableProcessOperation != null) {
            log.info("" + "aPrevDurableProcessOperation is not nil");
            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevDurableProcessOperation.getEventData();
            anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
            anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount(CimLongUtils.longValue(prevPOEventData.getOperationPassCount()));
            anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
            anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
        }

        anEventRecord.setRecipeParameters(new ArrayList<>());

        anEventRecord.setLogicalRecipeID("");
        anEventRecord.setMachineRecipeID("");
        anEventRecord.setPhysicalRecipeID("");
        anEventRecord.setEquipmentID("");
        anEventRecord.setOperationMode("");
        anEventRecord.setDurableControlJobID("");
        anEventRecord.setLocateBackFlag(false);

        eventManager.createEvent(anEventRecord, CimDurableOperationMoveEvent.class);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationMoveEventMakeLocateIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/2 14:31
     */
    public void durableOperationMoveEventMakeLocate(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOperationMoveEventMakeLocateIn strDurableOperationMoveEventMakeLocateIn) {
        String methodName = null;

        log.info("durableOperationMoveEventMakeLocate");

        Event.DurableOperationMoveEventRecord anEventRecord;
        Event.DurableEventData durableEventData = new Event.DurableEventData();
        CimDurableProcessFlowContext aDurablePFX = null;
        CimDurableProcessOperation aPrevProcessOperation = null;

        if (CimStringUtils.equals(strDurableOperationMoveEventMakeLocateIn.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
            log.info("" + "durableCategory is Cassette");
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, strDurableOperationMoveEventMakeLocateIn.getDurableID());
            durableEventData = aCassette.getEventData();

            aDurablePFX = aCassette.getDurableProcessFlowContext();

        } else if (CimStringUtils.equals(strDurableOperationMoveEventMakeLocateIn.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
            log.info("" + "durableCategory is ReticlePod");
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, strDurableOperationMoveEventMakeLocateIn.getDurableID());
            aDurablePFX = aReticlePod.getDurableProcessFlowContext();
        } else if (CimStringUtils.equals(strDurableOperationMoveEventMakeLocateIn.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
            log.info("" + "durableCategory is Reticle");
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, strDurableOperationMoveEventMakeLocateIn.getDurableID());
            durableEventData = aReticle.getEventData();

            aDurablePFX = aReticle.getDurableProcessFlowContext();

        }

        anEventRecord = new Event.DurableOperationMoveEventRecord();
        anEventRecord.setDurableData(new Event.DurableEventData());
        anEventRecord.getDurableData().setDurableID(durableEventData.getDurableID());
        anEventRecord.getDurableData().setDurableCategory(durableEventData.getDurableCategory());
        anEventRecord.getDurableData().setDurableStatus(durableEventData.getDurableStatus());
        anEventRecord.getDurableData().setHoldState(durableEventData.getHoldState());
        anEventRecord.getDurableData().setBankID(durableEventData.getBankID());
        anEventRecord.getDurableData().setRouteID(durableEventData.getRouteID());
        anEventRecord.getDurableData().setOperationNumber(durableEventData.getOperationNumber());
        anEventRecord.getDurableData().setOperationID(durableEventData.getOperationID());
        anEventRecord.getDurableData().setOperationPassCount(durableEventData.getOperationPassCount());
        anEventRecord.getDurableData().setObjrefPOS(durableEventData.getObjrefPOS());
        anEventRecord.getDurableData().setObjrefPO(durableEventData.getObjrefPO());
        anEventRecord.getDurableData().setObjrefMainPF(durableEventData.getObjrefMainPF());
        anEventRecord.getDurableData().setObjrefModulePOS(durableEventData.getObjrefModulePOS());

        Validations.check(null == aDurablePFX, retCodeConfig.getNotFoundPfx());

        aPrevProcessOperation = aDurablePFX.getPreviousProcessOperation();
        anEventRecord.setPreviousOperationPassCount(0L);

        if (aPrevProcessOperation != null) {
            log.info("" + "aPrevProcessOperation is not nil");
            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevProcessOperation.getEventData();

            anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
            anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount(CimLongUtils.longValue(prevPOEventData.getOperationPassCount()));
            anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
            anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
        }

        anEventRecord.setRecipeParameters(new ArrayList<>());
        anEventRecord.setLogicalRecipeID("");
        anEventRecord.setMachineRecipeID("");
        anEventRecord.setPhysicalRecipeID("");
        anEventRecord.setEquipmentID("");
        anEventRecord.setOperationMode("");
        anEventRecord.setDurableControlJobID("");
        anEventRecord.setLocateBackFlag(!(strDurableOperationMoveEventMakeLocateIn.getLocateDirection()));

        anEventRecord.setOldCurrentDurablePOData(new Event.DurableProcessOperationEventData());
        anEventRecord.getOldCurrentDurablePOData().setRouteID(strDurableOperationMoveEventMakeLocateIn.getStrOldCurrentPOData().getRouteID());
        anEventRecord.getOldCurrentDurablePOData().setOperationNumber(strDurableOperationMoveEventMakeLocateIn.getStrOldCurrentPOData().getOperationNumber());
        anEventRecord.getOldCurrentDurablePOData().setOperationID(strDurableOperationMoveEventMakeLocateIn.getStrOldCurrentPOData().getOperationID());
        anEventRecord.getOldCurrentDurablePOData().setOperationPassCount(CimLongUtils.longValue(
                strDurableOperationMoveEventMakeLocateIn.getStrOldCurrentPOData().getOperationPassCount()
        ));
        anEventRecord.getOldCurrentDurablePOData().setObjrefPOS(strDurableOperationMoveEventMakeLocateIn.getStrOldCurrentPOData().getObjrefPOS());
        anEventRecord.getOldCurrentDurablePOData().setObjrefMainPF(strDurableOperationMoveEventMakeLocateIn.getStrOldCurrentPOData().getObjrefMainPF());
        anEventRecord.getOldCurrentDurablePOData().setObjrefModulePOS(strDurableOperationMoveEventMakeLocateIn.getStrOldCurrentPOData().getObjrefModulePOS());

        anEventRecord.setEventCommon(new Event.EventData());
        anEventRecord.getEventCommon().setTransactionID(strDurableOperationMoveEventMakeLocateIn.getTransactionID());
        anEventRecord.getEventCommon().setEventTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp().toString());
        anEventRecord.getEventCommon().setEventShopDate(strObjCommonIn.getTimeStamp().getReportShopDate());
        anEventRecord.getEventCommon().setUserID(strObjCommonIn.getUser().getUserID().getValue());
        anEventRecord.getEventCommon().setEventMemo(strDurableOperationMoveEventMakeLocateIn.getClaimMemo());

        Event.EventRecord eventRecord;
        eventRecord = anEventRecord;
        eventManager.createEvent(eventRecord, CimDurableOperationMoveEvent.class);

    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/2 16:35
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public void fpcInfoRegistEventMake(Infos.ObjCommon objCommon, String transactionID, List<Infos.FPCInfoAction> strFPCInfoActionList, String claimMemo, String runCardID) {
        //init
        int FPCActionLen = CimArrayUtils.getSize(strFPCInfoActionList);
        int FPCActionCnt = 0;
        log.info("## Make event for DOC information registration. Base info Len {}", FPCActionLen);
        for (FPCActionCnt = 0; FPCActionCnt < FPCActionLen; FPCActionCnt++) {
            //Prepare DOC information Registration event
            Event.FPCEventRecord anEventRecord = new Event.FPCEventRecord();
            Event.EventData eventCommon = new Event.EventData();
            eventCommon.setTransactionID(transactionID);
            eventCommon.setEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
            eventCommon.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
            eventCommon.setUserID(objCommon.getUser().getUserID().getValue());
            eventCommon.setEventMemo(claimMemo);
            anEventRecord.setEventCommon(eventCommon);

            //check actionType
            log.info("## Check Actiont Type {}.", strFPCInfoActionList.get(FPCActionCnt).getActionType());
            if (!CimStringUtils.equals(SP_FPCINFO_CREATE, strFPCInfoActionList.get(FPCActionCnt).getActionType()) &&
                    !CimStringUtils.equals(SP_FPCINFO_UPDATE, strFPCInfoActionList.get(FPCActionCnt).getActionType()) &&
                    !CimStringUtils.equals(SP_FPCINFO_DELETE, strFPCInfoActionList.get(FPCActionCnt).getActionType())) {
                continue;
            }
            anEventRecord.setAction(strFPCInfoActionList.get(FPCActionCnt).getActionType());

            //DOC base info
            log.info("## FPC_ID  {}  ", strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getFpcID());
            log.info("## lotFamilyID  {} ", strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotFamilyID());
            //add runCardID for history
            anEventRecord.setRunCardID(runCardID);
            anEventRecord.setFPCID(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getFpcID());
            anEventRecord.setLotFamilyID(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotFamilyID().getValue());
            anEventRecord.setMainPDID(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getMainProcessDefinitionID().getValue());
            anEventRecord.setOperationNumber(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getOperationNumber());
            anEventRecord.setOriginalMainPDID(ObjectIdentifier.fetchValue(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getOriginalMainProcessDefinitionID()));
            anEventRecord.setOriginalOperationNumber(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getOriginalOperationNumber());
            anEventRecord.setSubMainPDID(ObjectIdentifier.fetchValue(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getSubMainProcessDefinitionID()));
            anEventRecord.setSubOperationNumber(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getSubOperationNumber());
            anEventRecord.setFPCGroupNo(String.valueOf(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getFpcGroupNumber()));
            anEventRecord.setFPCType(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getFpcType());
            anEventRecord.setMergeMainPDID(ObjectIdentifier.fetchValue(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getMergeMainProcessDefinitionID()));
            anEventRecord.setMergeOperationNumber(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getMergeOperationNumber());
            anEventRecord.setFPCCategory(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getFpcCategory());
            anEventRecord.setPdID(ObjectIdentifier.fetchValue(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getProcessDefinitionID()));
            anEventRecord.setPdType(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getProcessDefinitionType());
            anEventRecord.setCorrespondingOperNo(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getCorrespondOperationNumber());
            anEventRecord.setSkipFlag(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().isSkipFalg());
            anEventRecord.setRestrictEqpFlag(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().isRestrictEquipmentFlag());
            anEventRecord.setEquipmentID(ObjectIdentifier.fetchValue(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getEquipmentID()));
            anEventRecord.setMachineRecipeID(ObjectIdentifier.fetchValue(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getMachineRecipeID()));
            anEventRecord.setDcDefID(ObjectIdentifier.fetchValue(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcDefineID()));
            anEventRecord.setDcSpecID(ObjectIdentifier.fetchValue(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecID()));
            anEventRecord.setRecipeParameterChangeType(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getRecipeParameterChangeType());
            anEventRecord.setDescription(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDescription());
            anEventRecord.setCreateTime(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getCreateTime());
            anEventRecord.setUpdateTime(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getUpdateTime());
            anEventRecord.setSendEmailFlag(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().isSendEmailFlag());
            anEventRecord.setHoldLotFlag(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().isHoldLotFlag());

            //DOC Wafer info
            int waferLen = CimArrayUtils.getSize(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList());
            int waferCnt = 0;
            log.info("## Wafer info Len  {} ", waferLen);
            for (waferCnt = 0; waferCnt < waferLen; waferCnt++) {
                List<Event.WaferRecipeParameterEventData> wafers = new ArrayList<>();
                Event.WaferRecipeParameterEventData waferRecipeParameterEventData = new Event.WaferRecipeParameterEventData();
                waferRecipeParameterEventData.setWaferID(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList().get(waferCnt).getWaferID().getValue());

                //DOC Recipe Parm info
                int recipeParmLen = CimArrayUtils.getSize(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList().get(waferCnt).getRecipeParameterInfoList());
                int recipeParmCnt = 0;
                log.info("## RParm info Len   {} ", recipeParmLen);
                List<Event.RecipeParameterEventData> recipeParameters = new ArrayList<>();
                for (recipeParmCnt = 0; recipeParmCnt < recipeParmLen; recipeParmCnt++) {
                    Event.RecipeParameterEventData posRecipeParameterEventData = new Event.RecipeParameterEventData();
                    posRecipeParameterEventData.setSeq_No(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList().get(waferCnt).getRecipeParameterInfoList().get(recipeParmCnt).getSequenceNumber());
                    posRecipeParameterEventData.setParameterName(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList().get(waferCnt).getRecipeParameterInfoList().get(recipeParmCnt).getParameterName());
                    posRecipeParameterEventData.setParameterValue(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList().get(waferCnt).getRecipeParameterInfoList().get(recipeParmCnt).getParameterValue());
                    posRecipeParameterEventData.setUseCurrentSettingValueFlag(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList().get(waferCnt).getRecipeParameterInfoList().get(recipeParmCnt).isUseCurrentSettingValueFlag());
                    posRecipeParameterEventData.setParameterUnit(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList().get(waferCnt).getRecipeParameterInfoList().get(recipeParmCnt).getParameterUnit());
                    posRecipeParameterEventData.setParameterDataType(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList().get(waferCnt).getRecipeParameterInfoList().get(recipeParmCnt).getParameterDataType());
                    posRecipeParameterEventData.setParameterLowerLimit(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList().get(waferCnt).getRecipeParameterInfoList().get(recipeParmCnt).getParameterLowerLimit());
                    posRecipeParameterEventData.setParameterUpperLimit(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList().get(waferCnt).getRecipeParameterInfoList().get(recipeParmCnt).getParameterUpperLimit());
                    posRecipeParameterEventData.setParameterTargetValue(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getLotWaferInfoList().get(waferCnt).getRecipeParameterInfoList().get(recipeParmCnt).getParameterTargetValue());
                    recipeParameters.add(recipeParmCnt, posRecipeParameterEventData);
                    waferRecipeParameterEventData.setRecipeParameters(recipeParameters);
                }
                wafers.add(waferRecipeParameterEventData);
                anEventRecord.setWafers(wafers);
            }

            //DOC DCSpecs info
            int dcSpecLen = CimArrayUtils.getSize(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList());
            int dcSpecCnt = 0;
            log.info("## DCSpecItem info Len {}", dcSpecLen);
            List<Event.DCSpecItemEventData> dcSpecItems = new ArrayList<>();
            for (dcSpecCnt = 0; dcSpecCnt < dcSpecLen; dcSpecCnt++) {
                Event.DCSpecItemEventData dcSpecItemEventData = new Event.DCSpecItemEventData();
                dcSpecItemEventData.setDcItemName(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getDataItemName());
                dcSpecItemEventData.setScreenUpperRequired(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getScreenLimitUpperRequired());
                dcSpecItemEventData.setScreenUpperLimit(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getScreenLimitUpper());
                dcSpecItemEventData.setScreenUpperActions(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getActionCodes_uscrn());
                dcSpecItemEventData.setScreenLowerRequired(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getScreenLimitLowerRequired());
                dcSpecItemEventData.setScreenLowerLimit(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getScreenLimitLower());
                dcSpecItemEventData.setScreenLowerActions(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getActionCodes_lscrn());
                dcSpecItemEventData.setSpecUpperRequired(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getSpecLimitUpperRequired());
                dcSpecItemEventData.setSpecUpperLimit(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getSpecLimitUpper());
                dcSpecItemEventData.setSpecLowerRequired(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getSpecLimitLowerRequired());
                dcSpecItemEventData.setSpecLowerLimit(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getSpecLimitLower());
                dcSpecItemEventData.setSpecLowerActions(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getActionCodes_lsl());
                dcSpecItemEventData.setControlUpperRequired(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getControlLimitUpperRequired());
                dcSpecItemEventData.setControlUpeerLimit(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getControlLimitUpper());
                dcSpecItemEventData.setControlUpperActions(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getActionCodes_ucl());
                dcSpecItemEventData.setControlLowerRequired(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getControlLimitLowerRequired());
                dcSpecItemEventData.setControlLowerLimit(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getControlLimitLower());
                dcSpecItemEventData.setControlLowerActions(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getActionCodes_lcl());
                dcSpecItemEventData.setDcItemTargetValue(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getTarget());
                dcSpecItemEventData.setDcItemTag(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getTag());
                dcSpecItemEventData.setDcSpecGroup(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getDcSpecList().get(dcSpecCnt).getDcSpecGroup());
                dcSpecItems.add(dcSpecCnt, dcSpecItemEventData);
                anEventRecord.setDcSpecItems(dcSpecItems);
            }

            //DOC Reticle info
            int reticleLen = CimArrayUtils.getSize(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getReticleInfoList());
            int reticleCnt = 0;
            log.info("## Reticle info Len  {} ", reticleLen);
            List<Event.ReticleEventData> reticles = new ArrayList<>();
            for (reticleCnt = 0; reticleCnt < reticleLen; reticleCnt++) {
                Event.ReticleEventData reticleEventData = new Event.ReticleEventData();
                reticleEventData.setSeq_No(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getReticleInfoList().get(reticleCnt).getSequenceNumber());
                reticleEventData.setReticleID(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getReticleInfoList().get(reticleCnt).getReticleID().getValue());
                reticleEventData.setReticleGroupID(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getReticleInfoList().get(reticleCnt).getReticleGroup().getValue());
                reticles.add(reticleCnt, reticleEventData);
                anEventRecord.setReticles(reticles);
            }

            //DOC Corresponding Operation info
            int corrOpeLen = CimArrayUtils.getSize(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getCorrespondingOperationInfoList());
            int corrOpeCnt = 0;
            log.info("## Corresponding Operation info Len {} ", corrOpeLen);
            List<Event.CorrespondingOperationEventData> correspondingOperations = new ArrayList<>();
            for (corrOpeCnt = 0; corrOpeCnt < corrOpeLen; corrOpeCnt++) {
                Event.CorrespondingOperationEventData posCorrespondingOperationEventData = new Event.CorrespondingOperationEventData();
                posCorrespondingOperationEventData.setCorrespondingOperationNumber(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getCorrespondingOperationInfoList().get(corrOpeCnt).getCorrespondingOperationNumber());
                posCorrespondingOperationEventData.setDcSpecGroup(strFPCInfoActionList.get(FPCActionCnt).getStrFPCInfo().getCorrespondingOperationInfoList().get(corrOpeCnt).getDcSpecGroup());
                correspondingOperations.add(corrOpeCnt, posCorrespondingOperationEventData);
                anEventRecord.setCorrespondingOperations(correspondingOperations);
            }

            //Create FutureReworkRequest event
            eventManager.createEvent(anEventRecord, CimFPCEvent.class);
        }
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/6 10:24
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public void experimentalLotExecEventMake(Infos.ObjCommon objCommon, String txId, String testMemo, Infos.ExperimentalLotDetailResultInfo strExperimentalLotDetailResultInfo) {
        //initial
        log.info("in para transactionID,{}", txId);
        log.info("in para testMemo,{}", testMemo);
        int i = 0;
        int j = 0;
        int lenDetail = 0;
        int lenWafer = 0;
        Event.PlannedSplitEventRecord aEventRecord = new Event.PlannedSplitEventRecord();
        Event.EventData eventCommon = new Event.EventData();
        aEventRecord.setEventCommon(eventCommon);
        eventCommon.setTransactionID(txId);
        eventCommon.setEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        eventCommon.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
        eventCommon.setUserID(objCommon.getUser().getUserID().getValue());
        eventCommon.setEventMemo(testMemo);

        //add psmJobID and runCardID for hisory
        aEventRecord.setRunCardID(strExperimentalLotDetailResultInfo.getRunCardID());
        aEventRecord.setPsmJobID(strExperimentalLotDetailResultInfo.getPsmJobID());

        aEventRecord.setAction(strExperimentalLotDetailResultInfo.getAction());
        aEventRecord.setLotFamilyID(strExperimentalLotDetailResultInfo.getLotFamilyID().getValue());
        aEventRecord.setSplitRouteID(strExperimentalLotDetailResultInfo.getSplitOperationNumber());
        aEventRecord.setSplitOperationNumber(strExperimentalLotDetailResultInfo.getSplitOperationNumber());
        aEventRecord.setOriginalRouteID(strExperimentalLotDetailResultInfo.getOriginalRouteID().getValue());
        aEventRecord.setOriginalOperationNumber(strExperimentalLotDetailResultInfo.getOriginalOperationNumber());
        aEventRecord.setActionEMail(strExperimentalLotDetailResultInfo.isActionEMail());
        aEventRecord.setActionHold(strExperimentalLotDetailResultInfo.isActionHold());

        lenDetail = CimArrayUtils.getSize(strExperimentalLotDetailResultInfo.getStrExperimentalLotDetailSeq());
        List<Event.SplitSubRouteEventData> subRoutes = new ArrayList<>();
        aEventRecord.setSubRoutes(subRoutes);
        for (i = 0; i < lenDetail; i++) {
            Event.SplitSubRouteEventData splitSubRouteEventData = new Event.SplitSubRouteEventData();
            subRoutes.add(splitSubRouteEventData);
            splitSubRouteEventData.setSubRouteID(strExperimentalLotDetailResultInfo.getStrExperimentalLotDetailSeq().get(i).getSubRouteID().getValue());
            splitSubRouteEventData.setReturnOperationNumber(strExperimentalLotDetailResultInfo.getStrExperimentalLotDetailSeq().get(i).getReturnOperationNumber());
            splitSubRouteEventData.setMergeOperationNumber(strExperimentalLotDetailResultInfo.getStrExperimentalLotDetailSeq().get(i).getMergeOperationNumber());
            // bug 7267  If there is no batch, there is no information about parentLotID and childLotID
            ObjectIdentifier strParentLotID = strExperimentalLotDetailResultInfo.getStrExperimentalLotDetailSeq().get(i).getParentLotID();
            ObjectIdentifier strChildLotID = strExperimentalLotDetailResultInfo.getStrExperimentalLotDetailSeq().get(i).getChildLotID();
            splitSubRouteEventData.setParentLotID(strParentLotID == null ? null : strParentLotID.getValue());
            splitSubRouteEventData.setChildLotID(strChildLotID == null ? null : strChildLotID.getValue());
            splitSubRouteEventData.setMemo(strExperimentalLotDetailResultInfo.getStrExperimentalLotDetailSeq().get(i).getMemo());

            lenWafer = CimArrayUtils.getSize(strExperimentalLotDetailResultInfo.getStrExperimentalLotDetailSeq().get(i).getStrExperimentalLotWaferSeq());
            List<Event.SplitedWaferEventData> wafers = new ArrayList<>();
            splitSubRouteEventData.setWafers(wafers);

            for (j = 0; j < lenWafer; j++) {
                Event.SplitedWaferEventData splitedWaferEventData = new Event.SplitedWaferEventData();
                wafers.add(splitedWaferEventData);
                splitedWaferEventData.setWaferID(strExperimentalLotDetailResultInfo.getStrExperimentalLotDetailSeq().get(i).getStrExperimentalLotWaferSeq().get(j).getWaferId().getValue());
                splitedWaferEventData.setSuccessFlag(strExperimentalLotDetailResultInfo.getStrExperimentalLotDetailSeq().get(i).getStrExperimentalLotWaferSeq().get(j).getStatus());
            }
        }
        //Put into the queue of PosEventManager
        //Create FutureReworkRequest event
        eventManager.createEvent(aEventRecord, CimPlannedSplitEvent.class);

    }

    @Override
    public void objectUserDataChangeEventMake(Infos.ObjCommon objCommon, String stringifiedObjectReference, String className, List<Infos.HashedInfo> hashedInfoList, List<Infos.UserDataAction> userDataActionList, String claimMemo) {
        //---------------------------------------------------
        // Set to Event structure
        //---------------------------------------------------
        Event.UserDataChangeEventRecord anEvent = new Event.UserDataChangeEventRecord();
        StringBuilder keyString = new StringBuilder();
        for (int inputKeyIdx = 0; inputKeyIdx < CimArrayUtils.getSize(hashedInfoList); inputKeyIdx++) {
            if (inputKeyIdx > 0) {
                keyString.append(".");
            }
            keyString.append(hashedInfoList.get(inputKeyIdx).getHashKey());
            keyString.append(":");
            keyString.append(hashedInfoList.get(inputKeyIdx).getHashData());
        }
        anEvent.setClassName(className);
        anEvent.setHashedInfo(keyString.toString());
        anEvent.setEventCommon(setEventData(objCommon, claimMemo));

        //--------------------------------------------
        // Set Action Information
        //--------------------------------------------
        List<Event.UserDataChangeActionEventData> actions = new ArrayList<>();
        anEvent.setActions(actions);
        for (Infos.UserDataAction userDataAction : userDataActionList) {
            Validations.check(!CimObjectUtils.equals(userDataAction.getActionCode(), SP_USERDATAUPDATERESULT_ACTIONCODE_INSERT)
                    && !CimObjectUtils.equals(userDataAction.getActionCode(), SP_USERDATAUPDATERESULT_ACTIONCODE_UPDATE)
                    && !CimObjectUtils.equals(userDataAction.getActionCode(), SP_USERDATAUPDATERESULT_ACTIONCODE_DELETE), new OmCode(retCodeConfig.getInvalidActionCode(), userDataAction.getActionCode()));
            //---------------------------------------------------
            // Set action information
            //---------------------------------------------------
            Event.UserDataChangeActionEventData userDataChangeActionEventData = new Event.UserDataChangeActionEventData();
            actions.add(userDataChangeActionEventData);
            userDataChangeActionEventData.setName(userDataAction.getUserDataName());
            userDataChangeActionEventData.setOrig(userDataAction.getOriginator());
            userDataChangeActionEventData.setActionCode(userDataAction.getActionCode());
            userDataChangeActionEventData.setFromType(userDataAction.getFromType());
            userDataChangeActionEventData.setFromValue(userDataAction.getFromValue());
            userDataChangeActionEventData.setToType(userDataAction.getToType());
            userDataChangeActionEventData.setToValue(userDataAction.getToValue());
        }
        //---------------------------------------------------
        // Create User Data Change Event
        //---------------------------------------------------
        eventManager.createEvent(anEvent, CimUserDataChangeEvent.class);
    }

    @Override
    public void scriptParameterChangeEventMake(Infos.ObjCommon objCommon, String transactionID, String parameterClass, String identifier, List<Infos.UserParameterValue> userParameterValueList) {

        Event.EventData eventCommon = new Event.EventData();
        if (!CimObjectUtils.isEmpty(transactionID)) {
            eventCommon.setTransactionID(transactionID);
        } else {
            eventCommon.setTransactionID(objCommon.getTransactionID());
        }
        eventCommon.setUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        eventCommon.setEventTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        eventCommon.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());

        List<Event.UserParameterValue> eventUserParamValList = new ArrayList<>(userParameterValueList.size());
        // TODO: Neko double check this params
        for (Infos.UserParameterValue tmp : userParameterValueList) {
            Event.UserParameterValue tmpEvent = new Event.UserParameterValue();
            tmpEvent.setChangeType(tmp.getChangeType());
            tmpEvent.setDataType(tmp.getDataType());
            tmpEvent.setDescription(tmp.getDescription());
            tmpEvent.setKeyValue(tmp.getKeyValue());
            tmpEvent.setParameterName(tmp.getParameterName());
            tmpEvent.setValue(tmp.getValue());
            tmpEvent.setValueFlag(tmp.getValueFlag());

            eventUserParamValList.add(tmpEvent);
        }


        Event.ScriptParameterChangeEventRecord aEventRecord = new Event.ScriptParameterChangeEventRecord();
        aEventRecord.setEventCommon(eventCommon);
        aEventRecord.setParameterClass(parameterClass);
        aEventRecord.setIdentifier(identifier);
        aEventRecord.setUserParameterValues(eventUserParamValList.stream().filter(userParameterValue -> !userParameterValue.getChangeType().equals(BizConstant.SP_PAR_VAL_NO_CHANGE)).collect(Collectors.toList()));
        //------------------------------------------------------------------------//
        // Put into the queue of PosEventManager                                  //
        //------------------------------------------------------------------------//
        eventManager.createEvent(aEventRecord, CimScriptParameterChangeEvent.class);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationStartEventMakeOpeStartIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/2 17:52
     */
    public void durableOperationStartEventMakeOpeStart(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOperationStartEventMakeOpeStartIn strDurableOperationStartEventMakeOpeStartIn) {
        String methodName = null;

        log.info("durableOperationStartEventMakeOpeStart");

        Infos.DurableOperationStartEventMakeOpeStartIn strInParm = strDurableOperationStartEventMakeOpeStartIn;

        Event.DurableOperationStartEventRecord anEventRecord;
        Event.DurableEventData durableEventData = null;

        anEventRecord = new Event.DurableOperationStartEventRecord();
        anEventRecord.setEventCommon(new Event.EventData());
        anEventRecord.getEventCommon().setTransactionID(strObjCommonIn.getTransactionID());
        anEventRecord.getEventCommon().setEventTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp().toString());
        anEventRecord.getEventCommon().setEventShopDate(strObjCommonIn.getTimeStamp().getReportShopDate());
        anEventRecord.getEventCommon().setUserID(strObjCommonIn.getUser().getUserID().getValue());
        anEventRecord.getEventCommon().setEventMemo(strInParm.getClaimMemo());

        anEventRecord.setEquipmentID(strInParm.getEquipmentID().getValue());
        anEventRecord.setOperationMode(strInParm.getOperationMode());

        anEventRecord.setDurableControlJobID(strInParm.getDurableControlJobID().getValue());

        anEventRecord.setLogicalRecipeID(strInParm.getStrDurableStartRecipe().getLogicalRecipeId().getValue());
        anEventRecord.setMachineRecipeID(strInParm.getStrDurableStartRecipe().getMachineRecipeId().getValue());
        anEventRecord.setPhysicalRecipeID(strInParm.getStrDurableStartRecipe().getPhysicalRecipeId());

        int paramLen = CimArrayUtils.getSize(strInParm.getStrDurableStartRecipe().getStartRecipeParameterS());
        anEventRecord.setRecipeParameters(new ArrayList<>());
        for (int paramCnt = 0; paramCnt < paramLen; paramCnt++) {
            log.info("" + "ArrayUtils.getSize(loop to strInParm.getStrDurableStartRecipe().getStrStartRecipeParameter())" + paramCnt);
            anEventRecord.getRecipeParameters().add(new Event.RecipeParmEventData());
            anEventRecord.getRecipeParameters().get(paramCnt).setParameterName(strInParm.getStrDurableStartRecipe().getStartRecipeParameterS().get(paramCnt).getParameterName());
            anEventRecord.getRecipeParameters().get(paramCnt).setParameterValue(strInParm.getStrDurableStartRecipe().getStartRecipeParameterS().get(paramCnt).getParameterValue());
        }

        for (int durableCnt = 0; durableCnt < CimArrayUtils.getSize(strInParm.getStrStartDurables()); durableCnt++) {
            log.info("" + "ArrayUtils.getSize(loop to strInParm.getStrStartDurables())" + durableCnt);

            anEventRecord.setDurableData(new Event.DurableEventData());
            anEventRecord.getDurableData().setDurableID(strInParm.getStrStartDurables().get(durableCnt).getDurableId().getValue());
            anEventRecord.getDurableData().setDurableCategory(strInParm.getDurableCategory());

            CimCassette aCassette;
            CimReticlePod aReticlePod;
            CimProcessDurable aReticle;
            CimDurableProcessOperation aDurablePO;
            String durableStatus;
            if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
                log.info("" + "durableCategory == SP_DURABLECAT_CASSETTE");
                aCassette = baseCoreFactory.getBO(CimCassette.class, strInParm.getStrStartDurables().get(durableCnt).getDurableId());
                durableEventData = aCassette.getEventData();

            } else if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
                log.info("" + "durableCategory == SP_DURABLECAT_RETICLEPOD");
                aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, strInParm.getStrStartDurables().get(durableCnt).getDurableId());
                durableEventData = aReticlePod.getEventData();

            } else if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
                log.info("" + "durableCategory == SP_DURABLECAT_RETICLE");
                aReticle = baseCoreFactory.getBO(CimProcessDurable.class, strInParm.getStrStartDurables().get(durableCnt).getDurableId());
                durableEventData = aReticle.getEventData();

            } else {
                log.info("" + "durableCategory is invalid" + strInParm.getDurableCategory());
                Validations.check(true, retCodeConfig.getInvalidDurableCategory());
            }

            anEventRecord.setPreviousRouteID("");
            anEventRecord.setPreviousOperationID("");
            anEventRecord.setPreviousOperationNumber("");
            anEventRecord.setPreviousOperationPassCount(0L);
            anEventRecord.setPreviousObjrefPOS("");

            if (null != durableEventData) {
                anEventRecord.getDurableData().setDurableID(durableEventData.getDurableID());
                anEventRecord.getDurableData().setDurableCategory(durableEventData.getDurableCategory());
                anEventRecord.getDurableData().setDurableStatus(durableEventData.getDurableStatus());
                anEventRecord.getDurableData().setHoldState(durableEventData.getHoldState());
                anEventRecord.getDurableData().setBankID(durableEventData.getBankID());
                anEventRecord.getDurableData().setRouteID(durableEventData.getRouteID());
                anEventRecord.getDurableData().setOperationNumber(durableEventData.getOperationNumber());
                anEventRecord.getDurableData().setOperationID(durableEventData.getOperationID());
                anEventRecord.getDurableData().setOperationPassCount(durableEventData.getOperationPassCount());
                anEventRecord.getDurableData().setObjrefPOS(durableEventData.getObjrefPOS());
                anEventRecord.getDurableData().setObjrefPO(durableEventData.getObjrefPO());
                anEventRecord.getDurableData().setObjrefMainPF(durableEventData.getObjrefMainPF());
                anEventRecord.getDurableData().setObjrefModulePOS(durableEventData.getObjrefModulePOS());
            }

            eventManager.createEvent(anEventRecord, CimDurableOperationStartEvent.class);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationStartEventMakeOpeStartCancelIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/5 10:33
     */
    public void durableOperationStartEventMakeOpeStartCancel(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOperationStartEventMakeOpeStartCancelIn strDurableOperationStartEventMakeOpeStartCancelIn) {
        String methodName = null;

        log.info("durableOperationStartEventMakeOpeStartCancel");

        Infos.DurableOperationStartEventMakeOpeStartCancelIn strInParm = strDurableOperationStartEventMakeOpeStartCancelIn;

        Event.DurableOperationStartEventRecord anEventRecord;
        Event.DurableEventData durableEventData = null;

        for (int durableCnt = 0; durableCnt < CimArrayUtils.getSize(strInParm.getStrStartDurables()); durableCnt++) {
            log.info("" + "ArrayUtils.getSize(loop to strInParm.getStrStartDurables())" + durableCnt);

            anEventRecord = new Event.DurableOperationStartEventRecord();
            anEventRecord.setEventCommon(new Event.EventData());
            anEventRecord.getEventCommon().setTransactionID(strObjCommonIn.getTransactionID());
            anEventRecord.getEventCommon().setEventTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp().toString());
            anEventRecord.getEventCommon().setEventShopDate(strObjCommonIn.getTimeStamp().getReportShopDate());
            anEventRecord.getEventCommon().setUserID(strObjCommonIn.getUser().getUserID().getValue());
            anEventRecord.getEventCommon().setEventMemo(strInParm.getClaimMemo());

            anEventRecord.setDurableData(new Event.DurableEventData());
            anEventRecord.getDurableData().setDurableID(strInParm.getStrStartDurables().get(durableCnt).getDurableId().getValue());
            anEventRecord.getDurableData().setDurableCategory(strInParm.getDurableCategory());

            CimCassette aCassette;
            CimReticlePod aReticlePod;
            CimProcessDurable aReticle;
            CimDurableProcessOperation aDurablePO;
            String durableStatus;
            if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
                log.info("" + "durableCategory == SP_DURABLECAT_CASSETTE");
                aCassette = baseCoreFactory.getBO(CimCassette.class, strInParm.getStrStartDurables().get(durableCnt).getDurableId());
                durableEventData = aCassette.getEventData();

            } else if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
                log.info("" + "durableCategory == SP_DURABLECAT_RETICLEPOD");
                aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, strInParm.getStrStartDurables().get(durableCnt).getDurableId());
                durableEventData = aReticlePod.getEventData();

            } else if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
                log.info("" + "durableCategory == SP_DURABLECAT_RETICLE");
                aReticle = baseCoreFactory.getBO(CimProcessDurable.class, strInParm.getStrStartDurables().get(durableCnt).getDurableId());
                durableEventData = aReticle.getEventData();

            } else {

                log.info("" + "durableCategory is invalid" + strInParm.getDurableCategory());
                Validations.check(true, retCodeConfig.getInvalidDurableCategory());
            }

            if (null != durableEventData) {
                anEventRecord.getDurableData().setDurableID(durableEventData.getDurableID());
                anEventRecord.getDurableData().setDurableCategory(durableEventData.getDurableCategory());
                anEventRecord.getDurableData().setDurableStatus(durableEventData.getDurableStatus());
                anEventRecord.getDurableData().setHoldState(durableEventData.getHoldState());
                anEventRecord.getDurableData().setBankID(durableEventData.getBankID());
                anEventRecord.getDurableData().setRouteID(durableEventData.getRouteID());
                anEventRecord.getDurableData().setOperationNumber(durableEventData.getOperationNumber());
                anEventRecord.getDurableData().setOperationID(durableEventData.getOperationID());
                anEventRecord.getDurableData().setOperationPassCount(durableEventData.getOperationPassCount());
                anEventRecord.getDurableData().setObjrefPOS(durableEventData.getObjrefPOS());
                anEventRecord.getDurableData().setObjrefPO(durableEventData.getObjrefPO());
                anEventRecord.getDurableData().setObjrefMainPF(durableEventData.getObjrefMainPF());
                anEventRecord.getDurableData().setObjrefModulePOS(durableEventData.getObjrefModulePOS());
            }

            anEventRecord.setEquipmentID(strInParm.getEquipmentID().getValue());
            anEventRecord.setOperationMode(strInParm.getOperationMode());
            anEventRecord.setDurableControlJobID(strInParm.getDurableControlJobID().getValue());
            anEventRecord.setLogicalRecipeID("");
            anEventRecord.setMachineRecipeID("");
            anEventRecord.setPhysicalRecipeID("");
            anEventRecord.setRecipeParameters(new ArrayList<>());
            anEventRecord.setPreviousRouteID("");
            anEventRecord.setPreviousOperationID("");
            anEventRecord.setPreviousOperationNumber("");
            anEventRecord.setPreviousOperationPassCount(0L);
            anEventRecord.setPreviousObjrefPOS("");

            eventManager.createEvent(anEventRecord, CimDurableOperationStartEvent.class);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableReworkEventMakeIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/5 10:36
     */
    public void durableReworkEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableReworkEventMakeIn strDurableReworkEventMakeIn) {
        String methodName = null;

        log.info("durableReworkEventMake");

        Event.DurableReworkEventRecord anEventRecord;
        Event.DurableEventData durableEventData = new Event.DurableEventData();
        CimDurableProcessFlowContext aDurablePFX = null;
        CimDurableProcessOperation aPrevProcessOperation;
        CimDurableProcessOperation aPrevDurableProcessOperation = null;
        CimCassette aCassette = null;
        CimReticlePod aReticlePod = null;
        CimProcessDurable aReticle = null;

        if (CimStringUtils.equals(strDurableReworkEventMakeIn.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
            log.info("" + "durableCategory is Cassette");
            aCassette = baseCoreFactory.getBO(CimCassette.class, strDurableReworkEventMakeIn.getDurableID());
            durableEventData = aCassette.getEventData();

            aDurablePFX = aCassette.getDurableProcessFlowContext();

        } else if (CimStringUtils.equals(strDurableReworkEventMakeIn.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
            log.info("" + "durableCategory is ReticlePod");
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, strDurableReworkEventMakeIn.getDurableID());
            durableEventData = aReticlePod.getEventData();

            aDurablePFX = aReticlePod.getDurableProcessFlowContext();

        } else if (CimStringUtils.equals(strDurableReworkEventMakeIn.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
            log.info("" + "durableCategory is Reticle");
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class, strDurableReworkEventMakeIn.getDurableID());
            durableEventData = aReticle.getEventData();

            aDurablePFX = aReticle.getDurableProcessFlowContext();
        }

        anEventRecord = new Event.DurableReworkEventRecord();
        anEventRecord.setDurableData(new Event.DurableEventData());
        anEventRecord.getDurableData().setDurableID(durableEventData.getDurableID());
        anEventRecord.getDurableData().setDurableCategory(durableEventData.getDurableCategory());
        anEventRecord.getDurableData().setDurableStatus(durableEventData.getDurableStatus());
        anEventRecord.getDurableData().setHoldState(durableEventData.getHoldState());
        anEventRecord.getDurableData().setBankID(durableEventData.getBankID());
        anEventRecord.getDurableData().setRouteID(durableEventData.getRouteID());
        anEventRecord.getDurableData().setOperationNumber(durableEventData.getOperationNumber());
        anEventRecord.getDurableData().setOperationID(durableEventData.getOperationID());
        anEventRecord.getDurableData().setOperationPassCount(durableEventData.getOperationPassCount());
        anEventRecord.getDurableData().setObjrefPOS(durableEventData.getObjrefPOS());
        anEventRecord.getDurableData().setObjrefPO(durableEventData.getObjrefPO());
        anEventRecord.getDurableData().setObjrefMainPF(durableEventData.getObjrefMainPF());
        anEventRecord.getDurableData().setObjrefModulePOS(durableEventData.getObjrefModulePOS());
        anEventRecord.setPreviousOperationPassCount(0L);

        Validations.check(null == aDurablePFX, retCodeConfig.getNotFoundPfx());
        aPrevProcessOperation = aDurablePFX.getPreviousProcessOperation();
        aPrevDurableProcessOperation = aPrevProcessOperation;

        if (aPrevDurableProcessOperation != null) {
            log.info("" + "aPrevDurableProcessOperation is not nil");

            ProcessDTO.PosProcessOperationEventData prevPOEventData = aPrevDurableProcessOperation.getEventData();

            anEventRecord.setPreviousRouteID(prevPOEventData.getRouteID());
            anEventRecord.setPreviousOperationNumber(prevPOEventData.getOperationNumber());
            anEventRecord.setPreviousOperationID(prevPOEventData.getOperationID());
            anEventRecord.setPreviousOperationPassCount(CimLongUtils.longValue(prevPOEventData.getOperationPassCount()));
            anEventRecord.setPreviousObjrefPOS(prevPOEventData.getObjrefPOS());
            anEventRecord.setPreviousObjrefMainPF(prevPOEventData.getObjrefMainPF());
            anEventRecord.setPreviousObjrefModulePOS(prevPOEventData.getObjrefModulePOS());
        }

        anEventRecord.setReworkCount(0L);
        String key = null;

        if (CimStringUtils.length(strDurableReworkEventMakeIn.getStrOldCurrentPOData().getReworkOutOperation()) != 0) {
            log.info("" + "** Rework out operation is  " + strDurableReworkEventMakeIn.getStrOldCurrentPOData().getReworkOutOperation());
            key = strDurableReworkEventMakeIn.getStrOldCurrentPOData().getReworkOutOperation();
            log.info("" + "** Got key is  " + key);
        } else {

            log.info("" + "********* Terget Trace Start!!  *********");
            log.info("" + "** Claimed Transaction is ReworkReq" + strObjCommonIn.getTransactionID());

            AtomicReference<CimProcessFlow> aMainProcessFlow = new AtomicReference<>();
            AtomicReference<CimProcessFlow> aModuleProcessFlow = new AtomicReference<>();
            AtomicReference<CimProcessFlow> outMainProcessFlow = new AtomicReference<>();
            AtomicReference<CimProcessFlow> outModuleProcessFlow = new AtomicReference<>();
            String varOutModuleNumber, varModuleNumber;
            AtomicReference<String> moduleNumber = new AtomicReference<>();
            AtomicReference<String> outModuleNumber = new AtomicReference<>();
            int inseqno = 0;
            AtomicReference<Integer> outseqno = new AtomicReference<>();

            log.info("" + "** Getting PrevPOS");

            CimProcessOperationSpecification aBackupPOS = null;
            aBackupPOS = aDurablePFX.getPreviousProcessOperationSpecification(aMainProcessFlow, moduleNumber, aModuleProcessFlow);

            varModuleNumber = moduleNumber.get();

            String strTergetRouteID;
            String strOpeNumber;

            if (CimStringUtils.equals(strDurableReworkEventMakeIn.getTransactionID(), "ODRBW037")) {
                log.info("" + "transactionID is ODRBW037");

                strOpeNumber = aBackupPOS.getOperationNumber();

                outModuleNumber = moduleNumber;

                log.info("" + "** outModuleNumber  " + outModuleNumber);
                log.info("" + "** strOpeNumber     " + strOpeNumber);
                log.info("" + "** Getting aMainPD");

                Validations.check(null == aMainProcessFlow, retCodeConfig.getNotFoundPfForDurable());

                CimProcessDefinition aMainPD = aMainProcessFlow.get().getRootProcessDefinition();
                Validations.check(null == aMainPD, retCodeConfig.getNotFoundMainRoute());

                CimProcessDefinition aPosMainPD;
                aPosMainPD = aMainPD;

                Validations.check(null == aPosMainPD, retCodeConfig.getNotFoundMainRoute());

                log.info("" + "** Getting TergetRouteID");

                strTergetRouteID = aPosMainPD.getIdentifier();

                log.info("" + "** strTergetRouteID" + strTergetRouteID);
            } else {
                log.info("" + "** Getting PrevPOSFor");

                CimProcessOperationSpecification aPrevPOS = null;
                aPrevPOS = aDurablePFX.getPreviousProcessOperationSpecificationFor(aMainProcessFlow.get(), moduleNumber.get(), aModuleProcessFlow.get(),
                        aBackupPOS,
                        outMainProcessFlow, outModuleNumber, outModuleProcessFlow,
                        inseqno, outseqno);

                varOutModuleNumber = outModuleNumber.get();

                strOpeNumber = aPrevPOS.getOperationNumber();

                log.info("" + "** outModuleNumber  " + outModuleNumber);
                log.info("" + "** strOpeNumber     " + strOpeNumber);
                log.info("" + "** Getting aMainPD");

                Validations.check(outMainProcessFlow == null, retCodeConfig.getNotFoundPfForDurable());

                CimProcessDefinition aMainPD = outMainProcessFlow.get().getRootProcessDefinition();
                Validations.check(aMainPD == null, retCodeConfig.getNotFoundMainRoute());

                CimProcessDefinition aPosMainPD;
                aPosMainPD = aMainPD;

                Validations.check(aPosMainPD == null, retCodeConfig.getNotFoundMainRoute());
                log.info("" + "** Getting TergetRouteID");

                strTergetRouteID = aPosMainPD.getIdentifier();

                log.info("" + "** strTergetRouteID" + strTergetRouteID);
            }

            key = CimStringUtils.length(strTergetRouteID) + 1 + CimStringUtils.length(outModuleNumber.get()) + 1 + CimStringUtils.length(strOpeNumber) + 1 + "";

            if (key != null) {

                key = "";
            }

            key = strTergetRouteID;
            key += ".";
            key += outModuleNumber;
            key += ".";
            key += strOpeNumber;

            log.info("" + "** Made key is  " + key);
        }

        String keyVar = key;
        Long maxReworkCount = 0L;

        if (CimStringUtils.equals(strDurableReworkEventMakeIn.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
            log.info("" + "durableCategory is Cassette");
            if (null != aCassette) {
                maxReworkCount = aCassette.getReworkCount(key);
            }
        } else if (CimStringUtils.equals(strDurableReworkEventMakeIn.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
            log.info("" + "durableCategory is ReticlePod");
            if (null != aReticlePod) {
                maxReworkCount = aReticlePod.getReworkCount(key);
            }
        } else if (CimStringUtils.equals(strDurableReworkEventMakeIn.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
            log.info("" + "durableCategory is Reticle");
            if (null != aReticle) {
                maxReworkCount = aReticle.getReworkCount(key);
            }
        }
        log.info("" + "#### Max reworkCount = " + maxReworkCount);
        anEventRecord.setReworkCount(maxReworkCount);

        anEventRecord.setOldCurrentDurablePOData(new Event.DurableProcessOperationEventData());
        anEventRecord.getOldCurrentDurablePOData().setRouteID(strDurableReworkEventMakeIn.getStrOldCurrentPOData().getRouteID());
        anEventRecord.getOldCurrentDurablePOData().setOperationNumber(strDurableReworkEventMakeIn.getStrOldCurrentPOData().getOperationNumber());
        anEventRecord.getOldCurrentDurablePOData().setOperationID(strDurableReworkEventMakeIn.getStrOldCurrentPOData().getOperationID());
        anEventRecord.getOldCurrentDurablePOData().setOperationPassCount(CimLongUtils.longValue(
                strDurableReworkEventMakeIn.getStrOldCurrentPOData().getOperationPassCount()
        ));
        anEventRecord.getOldCurrentDurablePOData().setObjrefPOS(strDurableReworkEventMakeIn.getStrOldCurrentPOData().getObjrefPOS());
        anEventRecord.getOldCurrentDurablePOData().setObjrefMainPF(strDurableReworkEventMakeIn.getStrOldCurrentPOData().getObjrefMainPF());
        anEventRecord.getOldCurrentDurablePOData().setObjrefModulePOS(strDurableReworkEventMakeIn.getStrOldCurrentPOData().getObjrefModulePOS());
        anEventRecord.setReasonCodeID(strDurableReworkEventMakeIn.getReasonCodeID());

        anEventRecord.setEventCommon(setEventData(strObjCommonIn, strDurableReworkEventMakeIn.getClaimMemo()));

        eventManager.createEvent(anEventRecord, CimDurableReworkEvent.class);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param action
     * @param className
     * @param strDurableAttribute
     * @param claimMemo
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/5 11:22
     */
    public void durableRegistEventMake(
            Infos.ObjCommon strObjCommonIn,
            String transactionID,
            String action,
            String className,
            Infos.DurableAttribute strDurableAttribute,
            String claimMemo) {
        String methodName = null;

        log.info("durableRegistEventMake");

        Event.DurableEventRecord anEventRecord;

        anEventRecord = new Event.DurableEventRecord();
        anEventRecord.setEventCommon(setEventData(strObjCommonIn, claimMemo));

        double maximumRunTime = 0.0;
        if (CimStringUtils.length(strDurableAttribute.getMaximumRunTime()) > 0) {
            maximumRunTime = CimDoubleUtils.doubleValue(strDurableAttribute.getMaximumRunTime());
        }

        anEventRecord.setAction(action);
        anEventRecord.setDurableType(className);
        anEventRecord.setDurableID(strDurableAttribute.getDurableID().getValue());
        anEventRecord.setDescription(strDurableAttribute.getDescription());
        anEventRecord.setCategoryID(strDurableAttribute.getCategory());
        anEventRecord.setUsageCheckRequiredFlag(strDurableAttribute.getUsageCheckFlag());
        anEventRecord.setContents(strDurableAttribute.getContents());
        anEventRecord.setContentsSize(CimLongUtils.longValue(strDurableAttribute.getNominalSize()));
        anEventRecord.setCapacity(CimLongUtils.longValue(strDurableAttribute.getCapacity()));
        anEventRecord.setDurationLimit(maximumRunTime);
        anEventRecord.setTimeUsedLimit(CimLongUtils.longValue(strDurableAttribute.getMaximumOperationStartCount()));
        anEventRecord.setIntervalBetweenPM(CimLongUtils.longValue(strDurableAttribute.getIntervalBetweenPM()));
        anEventRecord.setInstanceName(strDurableAttribute.getInstanceName());

        CimEventBase anEvent;
        Event.EventRecord eventRecord;
        eventRecord = anEventRecord;

        anEvent = eventManager.createEvent(eventRecord, CimDurableEvent.class);


    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurablePFXCreateEventMakeIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/5 11:35
     */
    public void durablePFXCreateEventMake(Infos.ObjCommon strObjCommonIn, Infos.DurablePFXCreateEventMakeIn strDurablePFXCreateEventMakeIn) {
        log.info("durablePFXCreateEventMake");

        Event.DurablePFXEventRecord anEventRecord = new Event.DurablePFXEventRecord();

        anEventRecord.setEventCommon(setEventData(strObjCommonIn, strDurablePFXCreateEventMakeIn.getClaimMemo()));

        Durable aDurable = null;
        if (CimStringUtils.equals(strDurablePFXCreateEventMakeIn.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
            log.info("" + "durableCategory == SP_DURABLECAT_CASSETTE");
            aDurable = baseCoreFactory.getBO(CimCassette.class, strDurablePFXCreateEventMakeIn.getDurableID());
        } else if (CimStringUtils.equals(strDurablePFXCreateEventMakeIn.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
            log.info("" + "durableCategory == SP_DURABLECAT_RETICLEPOD");
            aDurable = baseCoreFactory.getBO(CimReticlePod.class, strDurablePFXCreateEventMakeIn.getDurableID());
        } else if (CimStringUtils.equals(strDurablePFXCreateEventMakeIn.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
            log.info("" + "durableCategory == SP_DURABLECAT_RETICLE");
            aDurable = baseCoreFactory.getBO(CimProcessDurable.class, strDurablePFXCreateEventMakeIn.getDurableID());
        } else {
            Validations.check(retCodeConfig.getInvalidDurableCategory());
        }
        Validations.check(null == aDurable, retCodeConfig.getNotFoundDurable());

        Event.DurableEventData durableEventData = aDurable.getEventData();
        anEventRecord.setDurableData(new Event.DurableEventData());
        anEventRecord.getDurableData().setDurableID(durableEventData.getDurableID());
        anEventRecord.getDurableData().setDurableCategory(strDurablePFXCreateEventMakeIn.getDurableCategory());
        anEventRecord.getDurableData().setDurableStatus(durableEventData.getDurableStatus());
        anEventRecord.getDurableData().setHoldState(durableEventData.getHoldState());
        anEventRecord.getDurableData().setBankID(durableEventData.getBankID());
        anEventRecord.getDurableData().setRouteID(durableEventData.getRouteID());
        anEventRecord.getDurableData().setOperationNumber(durableEventData.getOperationNumber());
        anEventRecord.getDurableData().setOperationID(durableEventData.getOperationID());
        anEventRecord.getDurableData().setOperationPassCount(durableEventData.getOperationPassCount());
        anEventRecord.getDurableData().setObjrefPOS(durableEventData.getObjrefPOS());
        anEventRecord.getDurableData().setObjrefPO(durableEventData.getObjrefPO());
        anEventRecord.getDurableData().setObjrefMainPF(durableEventData.getObjrefMainPF());
        anEventRecord.getDurableData().setObjrefModulePOS(durableEventData.getObjrefModulePOS());

        eventManager.createEvent(anEventRecord, CimDurablePFXEvent.class);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurablePFXDeleteEventMakeIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/5 13:11
     */
    public void durablePFXDeleteEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurablePFXDeleteEventMakeIn strDurablePFXDeleteEventMakeIn) {
        String methodName = null;

        log.info("durablePFXDeleteEventMake");

        Infos.DurablePFXDeleteEventMakeIn strInParm = strDurablePFXDeleteEventMakeIn;
        Event.DurableEventData durableEventData = null;

        Event.DurablePFXEventRecord anEventRecord;

        anEventRecord = new Event.DurablePFXEventRecord();
        anEventRecord.setEventCommon(setEventData(strObjCommonIn, strInParm.getClaimMemo()));

        CimCassette aCassette;
        CimReticlePod aReticlePod;
        CimProcessDurable aReticle;
        String durableStatus;
        if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
            log.info("" + "durableCategory == SP_DURABLECAT_CASSETTE");
            aCassette = baseCoreFactory.getBO(CimCassette.class, strInParm.getDurableID());
            durableEventData = aCassette.getEventData();

        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
            log.info("" + "durableCategory == SP_DURABLECAT_RETICLEPOD");
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, strInParm.getDurableID());
            durableEventData = aReticlePod.getEventData();

        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
            log.info("" + "durableCategory == SP_DURABLECAT_RETICLE");
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class, strInParm.getDurableID());
            durableEventData = aReticle.getEventData();

        } else {
            log.info("" + "Invalid durable category" + strInParm.getDurableCategory());
            Validations.check(true, retCodeConfig.getInvalidDurableCategory());
        }

        anEventRecord.setDurableData(new Event.DurableEventData());
        if (null != durableEventData) {
            anEventRecord.getDurableData().setDurableID(durableEventData.getDurableID());
            anEventRecord.getDurableData().setDurableCategory(durableEventData.getDurableCategory());
            anEventRecord.getDurableData().setDurableStatus(durableEventData.getDurableStatus());
            anEventRecord.getDurableData().setHoldState(durableEventData.getHoldState());
            anEventRecord.getDurableData().setBankID(durableEventData.getBankID());
            anEventRecord.getDurableData().setRouteID(durableEventData.getRouteID());
            anEventRecord.getDurableData().setOperationNumber(durableEventData.getOperationNumber());
            anEventRecord.getDurableData().setOperationID(durableEventData.getOperationID());
            anEventRecord.getDurableData().setOperationPassCount(durableEventData.getOperationPassCount());
            anEventRecord.getDurableData().setObjrefPOS(durableEventData.getObjrefPOS());
            anEventRecord.getDurableData().setObjrefPO(durableEventData.getObjrefPO());
            anEventRecord.getDurableData().setObjrefMainPF(durableEventData.getObjrefMainPF());
            anEventRecord.getDurableData().setObjrefModulePOS(durableEventData.getObjrefModulePOS());
        }

        eventManager.createEvent(anEventRecord, CimDurablePFXEvent.class);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param requestUserID
     * @param equipmentID
     * @param stockerID
     * @param AGVID
     * @param strEquipmentAlarm
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/5 15:21
     */
    public void equipmentAlarmEventMake(Infos.ObjCommon strObjCommonIn, String transactionID, User requestUserID,
                                        ObjectIdentifier equipmentID, ObjectIdentifier stockerID, ObjectIdentifier AGVID,
                                        Infos.EquipmentAlarm strEquipmentAlarm){

        String methodName = null;
        log.info("equipmentAlarmEventMake");
        Event.EquipmentAlarmEventRecord eqpAlmEvtData;
        eqpAlmEvtData = new Event.EquipmentAlarmEventRecord();
        eqpAlmEvtData.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
        eqpAlmEvtData.setStockerID(ObjectIdentifier.fetchValue(stockerID));
        eqpAlmEvtData.setAGVID(ObjectIdentifier.fetchValue(AGVID));
        eqpAlmEvtData.setAlarmCategory(strEquipmentAlarm.getAlarmCategory());
        eqpAlmEvtData.setAlarmCode(strEquipmentAlarm.getAlarmCode());
        eqpAlmEvtData.setAlarmID(strEquipmentAlarm.getAlarmID());
        eqpAlmEvtData.setAlarmText(strEquipmentAlarm.getAlarmText());
        eqpAlmEvtData.setEventCommon(new Event.EventData());
        eqpAlmEvtData.getEventCommon().setTransactionID(transactionID);
        eqpAlmEvtData.getEventCommon().setUserID(requestUserID.getUserID().getValue());
        eqpAlmEvtData.getEventCommon().setEventTimeStamp(strEquipmentAlarm.getAlarmTimeStamp());
        eqpAlmEvtData.getEventCommon().setEventShopDate(null);

        eqpAlmEvtData.getEventCommon().setEventMemo("");

        CimEventBase event = eventManager.createEvent(eqpAlmEvtData, CimEquipmentAlarmEvent.class);
        //-------------------------------//
        // Extendable Event              //
        //-------------------------------//
        if (null != event) {
            // Make Event
            BaseEvent baseEvent = new BaseEvent(this);
            baseEvent.setTxID(strObjCommonIn.getTransactionID());
            baseEvent.setUserID(ObjectIdentifier.fetchValue(strObjCommonIn.getUser().getUserID()));
            baseEvent.setEventID(event.getPrimaryKey());
            baseEvent.setEventInfo(JSONObject.toJSONString(eqpAlmEvtData));
            extendableEventManager.makeEvent(BizConstant.CATEGORY_EQP_ALARM, baseEvent);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strEquipmentContainerMaxRsvCountUpdateEventMakeIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/5 15:25
     */
    public void equipmentContainerMaxRsvCountUpdateEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.EquipmentContainerMaxRsvCountUpdateEventMakeIn strEquipmentContainerMaxRsvCountUpdateEventMakeIn) {
        String methodName = null;

        log.info("equipmentContainerMaxRsvCountUpdateEventMake");

        Event.SLMMaxReserveCountEventRecord anEventRecord;

        anEventRecord = new Event.SLMMaxReserveCountEventRecord();
        anEventRecord.setMachineID(strEquipmentContainerMaxRsvCountUpdateEventMakeIn.getEquipmentID().getValue());
        anEventRecord.setMachineContainerID(strEquipmentContainerMaxRsvCountUpdateEventMakeIn.getEquipmentContainerID().getValue());
        anEventRecord.setMaxReserveCount(strEquipmentContainerMaxRsvCountUpdateEventMakeIn.getMaxRsvCount());
        anEventRecord = new Event.SLMMaxReserveCountEventRecord();
        anEventRecord.setEventCommon(setEventData(strObjCommonIn,
                strEquipmentContainerMaxRsvCountUpdateEventMakeIn.getClaimMemo()));

        Event.EventRecord eventRecord;
        eventRecord = anEventRecord;

        EventBaseDO anEvent;
//            anEvent = eventManager.createEvent( eventRecord ,CimEquipmentContainerMaxRsvCountUpdateEvent.class);


    }

    @Override
    public void systemMessageEventMake(Infos.ObjCommon objCommonIn, Inputs.SystemMessageEventMakeIn systemMessageEventMakeIn) {
        Event.SystemMessageEventRecord sysMsgEvtRecord = new Event.SystemMessageEventRecord();
        int messageLen = CimStringUtils.length(systemMessageEventMakeIn.getSystemMessageText());
        Validations.check(messageLen > 2048, retCodeConfig.getInvalidInputParam());
        /*------------------------------------------*/
        /*   Set Chamber Status Change Event Data   */
        /*------------------------------------------*/
        sysMsgEvtRecord.setSubSystemID(systemMessageEventMakeIn.getSubSystemID());
        sysMsgEvtRecord.setSystemMessageCode(systemMessageEventMakeIn.getSystemMessageCode());
        sysMsgEvtRecord.setSystemMessageText(systemMessageEventMakeIn.getSystemMessageText());
        sysMsgEvtRecord.setNotifyFlag(systemMessageEventMakeIn.isNotifyFlag());
        sysMsgEvtRecord.setEquipmentID(ObjectIdentifier.fetchValue(systemMessageEventMakeIn.getEquipmentID()));
        sysMsgEvtRecord.setEquipmentState(systemMessageEventMakeIn.getEquipmentStatus());
        sysMsgEvtRecord.setStockerID(ObjectIdentifier.fetchValue(systemMessageEventMakeIn.getStockerID()));
        sysMsgEvtRecord.setStockerState(systemMessageEventMakeIn.getStockerStatus());
        sysMsgEvtRecord.setAGVID(ObjectIdentifier.fetchValue(systemMessageEventMakeIn.getAGVID()));
        sysMsgEvtRecord.setAGVState(systemMessageEventMakeIn.getAGVStatus());
        sysMsgEvtRecord.setLotID(ObjectIdentifier.fetchValue(systemMessageEventMakeIn.getLotID()));
        sysMsgEvtRecord.setLotState(systemMessageEventMakeIn.getLotStatus());
        sysMsgEvtRecord.setRouteID(ObjectIdentifier.fetchValue(systemMessageEventMakeIn.getRouteID()));
        sysMsgEvtRecord.setOperationNumber(systemMessageEventMakeIn.getOperationNumber());
        sysMsgEvtRecord.setOperationID(ObjectIdentifier.fetchValue(systemMessageEventMakeIn.getOperationID()));
        sysMsgEvtRecord.setEventCommon(setEventData(objCommonIn, systemMessageEventMakeIn.getClaimMemo()));
        CimEventBase event = eventManager.createEvent(sysMsgEvtRecord, CimSystemMessageEvent.class);
        //-------------------------------//
        // Extendable Event              //
        //-------------------------------//
        if (null != event) {
            // Make Event
            BaseEvent baseEvent = new BaseEvent(this);
            baseEvent.setTxID(objCommonIn.getTransactionID());
            baseEvent.setUserID(ObjectIdentifier.fetchValue(objCommonIn.getUser().getUserID()));
            baseEvent.setEventID(event.getPrimaryKey());
            baseEvent.setEventInfo(JSONObject.toJSONString(sysMsgEvtRecord));
            extendableEventManager.makeEvent(BizConstant.CATEGORY_SYSTEM_MESSAGE, baseEvent);
        }

    }

    @Override
    public void sorterSorterJobEventMake(Infos.ObjCommon objCommon, Inputs.ObjSorterSorterJobEventMakeIn param) {

       /* //---------------------------------------------------
        // Check Input parameter
        //---------------------------------------------------
        log.info("Sort Job Event action : {}",param.getAction());
        if (!CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBCREATE,param.getAction())
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBSTART,param.getAction())
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_COMPONENTJOBXFER,param.getAction())
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_COMPONENTJOBSTART,param.getAction())
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_COMPONENTJOBCOMP,param.getAction())
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBCOMP,param.getAction())
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBERROR,param.getAction())
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBDELETED,param.getAction())){
            log.error("Specified action is invalid.{}",param.getAction());
            Validations.check(true, new OmCode(retCodeConfig.getInvalidActionCode(),param.getAction()));
        }
        //---------------------------------------------------
        // Set to Event structure
        //---------------------------------------------------
        log.info("Set to Event structure {}",param.getStrSortJobListAttributes().getSorterJobID().getValue());
        Event.WaferSortJobEventRecord anEvent = new Event.WaferSortJobEventRecord();
        anEvent.setEquipmentID(param.getStrSortJobListAttributes().getEquipmentID().getValue());
        anEvent.setPortGroupID(param.getStrSortJobListAttributes().getPortGroupID().getValue());
        anEvent.setSorterJobID(param.getStrSortJobListAttributes().getSorterJobID().getValue());
        anEvent.setAction(param.getAction());
        anEvent.setSorterJobCategory(param.getSorterJobCategory());
        anEvent.setWaferIDReadFlag(param.getStrSortJobListAttributes().getWaferIDReadFlag());

        anEvent.setEventCommon(setEventData(objCommon, param.getClaimMemo()));

        //-----------------------
        // Set sorter job status
        //-----------------------
        log.info("Set Sort Job information{}",param.getAction());
        // Sort Job Create
        String action = param.getAction();
        if (CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBCREATE,action)){
            log.info("Set sorter Job Status {}",BizConstant.SP_SORTERJOBSTATUS_WAIT_TO_EXECUTING);
            anEvent.setSorterJobStatus(BizConstant.SP_SORTERJOBSTATUS_WAIT_TO_EXECUTING);
        }
        // Sort Job Start
        else if (CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBSTART,action)){
            log.info("Set sorter Job Status {}",BizConstant.SP_SORTERJOBSTATUS_EXECUTING);
            anEvent.setSorterJobStatus(BizConstant.SP_SORTERJOBSTATUS_EXECUTING);
        }
        // Component Job Xfer
        else if (CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_COMPONENTJOBXFER,action)){
            log.info("Set sorter Job Status {}",BizConstant.SP_SORTERJOBSTATUS_EXECUTING);
            anEvent.setSorterJobStatus(BizConstant.SP_SORTERJOBSTATUS_EXECUTING);
        }
        //Component Job Start
        else if (CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_COMPONENTJOBSTART,action)){
            log.info("Set sorter Job Status {}",BizConstant.SP_SORTERJOBSTATUS_EXECUTING);
            anEvent.setSorterJobStatus(BizConstant.SP_SORTERJOBSTATUS_EXECUTING);
        }
        // Component Job Comp
        else if (CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_COMPONENTJOBCOMP,action)){
            log.info("Set sorter Job Status {}",BizConstant.SP_SORTERJOBSTATUS_EXECUTING);
            anEvent.setSorterJobStatus(BizConstant.SP_SORTERJOBSTATUS_EXECUTING);
        }
        //Sort Job Comp
        else if (CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBCOMP,action)){
            log.info("Set sorter Job Status {}",BizConstant.SP_SORTERJOBSTATUS_COMPLETED);
            anEvent.setSorterJobStatus(BizConstant.SP_SORTERJOBSTATUS_COMPLETED);
        }
        // Sort Job Error
        else if (CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBERROR,action)){
            log.info("Set sorter Job Status {}",BizConstant.SP_SORTERJOBSTATUS_ERROR);
            anEvent.setSorterJobStatus(BizConstant.SP_SORTERJOBSTATUS_ERROR);
        }
        //Sort Job Deleted
        else if (CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBDELETED,action)){
            log.info("Set sorter Job Status {}"," is Blank.");
            anEvent.setSorterJobStatus("");
        }
        //--------------------------------------------
        // Set Component Job Information
        //--------------------------------------------
        int srtCompoLen = CimArrayUtils.getSize(param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList());
        int srtCompoCnt = 0;
        int eventSlotMapCnt = 0;
        anEvent.setComponentJobs(new ArrayList<Event.SortJobComponentEventData>());
        for (int i = 0; i < srtCompoLen; i++) {
            //---------------------------------------------------
            // Set component job id / component job status
            //---------------------------------------------------
            if (CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBCREATE,param.getAction())){
                log.info("Component Job ID / Component Job Status is blank .{}",param.getAction());
                anEvent.setComponentJobID("");
                anEvent.setComponentJobStatus("");
            }else if (CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBSTART,param.getAction())
                    || CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBCOMP,param.getAction())
                    || CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBDELETED,param.getAction())){
                log.info("Component Job ID / Component Job Status is blank .{}",param.getAction());
                anEvent.setComponentJobID("");
                anEvent.setComponentJobStatus("");
                anEvent.setComponentJobs(new ArrayList<Event.SortJobComponentEventData>());
                anEvent.setSlotMaps(new ArrayList<Event.SortJobSlotMapEventData>());
                break;
            }else {
                log.info("Component Job ID / Component Job Status {},{}",param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getSorterComponentJobID().getValue(),param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getComponentSorterJobStatus());
                anEvent.setComponentJobID(param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getSorterComponentJobID().getValue());
                anEvent.setComponentJobStatus(param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getComponentSorterJobStatus());
                anEvent.setComponentJobs(new ArrayList<Event.SortJobComponentEventData>());
                anEvent.setSlotMaps(new ArrayList<Event.SortJobSlotMapEventData>());
                break;
            }
            //---------------------------------------------------
            // Set component job information (only Creation)
            //---------------------------------------------------
            log.info("Set Component Job Information. {}",param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getSorterComponentJobID().getValue());
            Event.SortJobComponentEventData sortJobComponentEventData = new Event.SortJobComponentEventData();
            anEvent.getComponentJobs().add(i,sortJobComponentEventData);
            sortJobComponentEventData.setSorterJobID(param.getStrSortJobListAttributes().getSorterJobID().getValue());
            sortJobComponentEventData.setComponentJobID(param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getSorterComponentJobID().getValue());
            sortJobComponentEventData.setJobSeq(CimLongUtils.getLongValue(i+1));
            sortJobComponentEventData.setSourceCassetteID(param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getOriginalCarrierID().getValue());
            sortJobComponentEventData.setDestinationCassetteID(param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getDestinationCarrierID().getValue());
            sortJobComponentEventData.setSourcePortID(param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getOriginalPortID().getValue());
            sortJobComponentEventData.setDestinationPortID(param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getDestinationPortID().getValue());

            //---------------------------------------------------
            // Set SlotMap information (only Creation)
            //---------------------------------------------------
            int slotMapLen = CimArrayUtils.getSize(param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getWaferSorterSlotMapList());
            int eventSlotMapLen = CimArrayUtils.getSize(anEvent.getSlotMaps());
            List<Event.SortJobSlotMapEventData> sortJobSlotMapEventDataList = new ArrayList<>();
            for (int j = 0; j < slotMapLen; j++) {
                Event.SortJobSlotMapEventData sortJobSlotMapEventData = new Event.SortJobSlotMapEventData();
                Infos.WaferSorterSlotMap waferSorterSlotMap = param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getWaferSorterSlotMapList().get(j);
                sortJobSlotMapEventData.setSorterJobID(param.getStrSortJobListAttributes().getSorterJobID().getValue());
                sortJobSlotMapEventData.setComponentJobID(param.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(i).getSorterComponentJobID().getValue());
                sortJobSlotMapEventData.setLotID(waferSorterSlotMap.getLotID().getValue());
                sortJobSlotMapEventData.setWaferID(waferSorterSlotMap.getWaferID().getValue());
                sortJobSlotMapEventData.setDestinationCassetteID(waferSorterSlotMap.getDestinationCassetteID().getValue());
                sortJobSlotMapEventData.setDestinationPortID(waferSorterSlotMap.getDestinationPortID().getValue());
                sortJobSlotMapEventData.setDestinationManagedByMyCim(waferSorterSlotMap.getDestinationCassetteManagedByOM());
                sortJobSlotMapEventData.setDestinationSlotPosition(waferSorterSlotMap.getDestinationSlotNumber());
                sortJobSlotMapEventData.setSourceCassetteID(waferSorterSlotMap.getOriginalCassetteID().getValue());
                sortJobSlotMapEventData.setSourcePortID(waferSorterSlotMap.getOriginalPortID().getValue());
                sortJobSlotMapEventData.setSourceManagedByMyCim(waferSorterSlotMap.getOriginalCassetteManagedByOM());
                sortJobSlotMapEventData.setSourceSlotPosition(waferSorterSlotMap.getOriginalSlotNumber());
                sortJobSlotMapEventDataList.add(sortJobSlotMapEventData);
            }
            anEvent.setSlotMaps(sortJobSlotMapEventDataList);
        }
        //---------------------------------------------------
        // Create Wafer Sorter Event
        //---------------------------------------------------
        log.info("Create Wafer Sorter Event ");
        eventManager.createEvent( anEvent ,CimWaferSortJobEvent.class);*/
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strCollectedDataChangeEventMakeIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/20 17:16
     */
    public void collectedDataChangeEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.CollectedDataChangeEventMakeIn strCollectedDataChangeEventMakeIn) {
        log.info("collectedDataChangeEventMake");

        List<Infos.DCItem> strPreviousRawDCItem = strCollectedDataChangeEventMakeIn.getStrPreviousRawDCItem();
        List<Infos.DCItem> strCurrentRawDCItem = strCollectedDataChangeEventMakeIn.getStrCurrentRawDCItem();
        int strPreviousRawDCItemLen = CimArrayUtils.getSize(strPreviousRawDCItem);
        int strCurrentRawDCItemLen = CimArrayUtils.getSize(strCurrentRawDCItem);

        log.info("" + "in Parameter transactionID              " + strCollectedDataChangeEventMakeIn.getTransactionID());
        log.info("" + "in Parameter lotID                      " + strCollectedDataChangeEventMakeIn.getLotID().getValue());
        log.info("" + "in Parameter controlJobID               " + strCollectedDataChangeEventMakeIn.getControlJobID().getValue());
        log.info("" + "in Parameter dataCollectionDefinitionID " + strCollectedDataChangeEventMakeIn.getDataCollectionDefinitionID().getValue());
        log.info("" + "in Parameter strPreviousRawDCItemLen    " + strPreviousRawDCItemLen);
        log.info("" + "in Parameter strCurrentRawDCItemLen     " + strCurrentRawDCItemLen);

        Validations.check(strPreviousRawDCItemLen != strCurrentRawDCItemLen, retCodeConfig.getInvalidParameterWithMsg());

        if (strPreviousRawDCItemLen > 0) {
            log.info("" + "strPreviousRawDCItemLen > 0 ");
            Event.CollectedDataChangeEventRecord anEventRecord = new Event.CollectedDataChangeEventRecord();

            int nCnt1 = 0;

            anEventRecord.setLotID(strCollectedDataChangeEventMakeIn.getLotID().getValue());
            anEventRecord.setControlJobID(strCollectedDataChangeEventMakeIn.getControlJobID().getValue());
            anEventRecord.setDataCollectionDefinitionID(strCollectedDataChangeEventMakeIn.getDataCollectionDefinitionID().getValue());

            anEventRecord.setChangedDCDataSeq(new ArrayList<>());

            for (nCnt1 = 0; nCnt1 < strPreviousRawDCItemLen; nCnt1++) {
                log.info("" + "# changedDCDataSeq.get(nCnt1).getDataCollectionItemName() " + nCnt1 + strPreviousRawDCItem.get(nCnt1).getDataCollectionItemName());
                anEventRecord.getChangedDCDataSeq().get(nCnt1).setDataCollectionItemName(strPreviousRawDCItem.get(nCnt1).getDataCollectionItemName());
                anEventRecord.getChangedDCDataSeq().get(nCnt1).setPreviousDataValue(strPreviousRawDCItem.get(nCnt1).getDataValue());
                anEventRecord.getChangedDCDataSeq().get(nCnt1).setCurrentDataValue(strCurrentRawDCItem.get(nCnt1).getDataValue());
            }

            anEventRecord.setEventCommon(setEventData(strObjCommonIn,
                    strCollectedDataChangeEventMakeIn.getClaimMemo()));

            log.info("" + "anEventRecord.getEventCommon().getTransactionID()     " + anEventRecord.getEventCommon().getTransactionID());
            log.info("" + "anEventRecord.getEventCommon().getEventTimeStamp()    " + anEventRecord.getEventCommon().getEventTimeStamp());
            log.info("" + "anEventRecord.getEventCommon().getEventShopDate()     " + anEventRecord.getEventCommon().getEventShopDate());
            log.info("" + "anEventRecord.getEventCommon().getUserID()            " + anEventRecord.getEventCommon().getUserID());
            log.info("" + "anEventRecord.getEventCommon().getEventMemo()         " + anEventRecord.getEventCommon().getEventMemo());

            eventManager.createEvent(anEventRecord, CimCollectedDataChangeEvent.class);
        }
        log.info("collectedDataChangeEventMake");

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param action
     * @param lotID
     * @param routeID
     * @param operationNumber
     * @param strFutureReworkDetailInfoSeq
     * @param claimMemo
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/9/17 14:00
     */
    public void lotFutureReworkEventMake(
            Infos.ObjCommon strObjCommonIn,
            String transactionID,
            String action,
            ObjectIdentifier lotID,
            ObjectIdentifier routeID,
            String operationNumber,
            List<Infos.FutureReworkDetailInfo> strFutureReworkDetailInfoSeq,
            String claimMemo) {
        String methodName = null;

        log.info("lotFutureReworkEventMake");

        Event.FutureReworkEventRecord anEventRecord;

        anEventRecord = new Event.FutureReworkEventRecord();
        anEventRecord.setEventCommon(new Event.EventData());
        anEventRecord.getEventCommon().setTransactionID(transactionID);
        anEventRecord.getEventCommon().setEventTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp().toString());
        anEventRecord.getEventCommon().setEventShopDate(strObjCommonIn.getTimeStamp().getReportShopDate());
        anEventRecord.getEventCommon().setUserID(strObjCommonIn.getUser().getUserID().getValue());
        anEventRecord.getEventCommon().setEventMemo(claimMemo);

        anEventRecord.setAction(action);
        anEventRecord.setLotID(lotID.getValue());
        anEventRecord.setRouteID(routeID.getValue());
        anEventRecord.setOperationNumber(operationNumber);

        int detailLen = CimArrayUtils.getSize(strFutureReworkDetailInfoSeq);
        anEventRecord.setReworkRoutes(new ArrayList<>());
        for (int i = 0; i < detailLen; i++) {
            anEventRecord.getReworkRoutes().add(new Event.FutureReworkRouteEventData());
            anEventRecord.getReworkRoutes().get(i).setTrigger(strFutureReworkDetailInfoSeq.get(i).getTrigger());
            anEventRecord.getReworkRoutes().get(i).setReworkRouteID(strFutureReworkDetailInfoSeq.get(i).getReworkRouteID().getValue());
            anEventRecord.getReworkRoutes().get(i).setReturnOperationNumber(strFutureReworkDetailInfoSeq.get(i).getReturnOperationNumber());
            anEventRecord.getReworkRoutes().get(i).setReasonCodeID(strFutureReworkDetailInfoSeq.get(i).getReasonCodeID());
        }

        CimEventBase anEvent;
        Event.EventRecord eventRecord;
        eventRecord = anEventRecord;
        anEvent = eventManager.createEvent(eventRecord, CimFutureReworkEvent.class);

    }

    @Override
    public void ownerChangeEventMake(Infos.ObjCommon objCommon, Inputs.ObjOwnerChangeEventMakeIn input) {

        //---------------------------------------------------
        // Set to Event structure
        //---------------------------------------------------

        Event.OwnerChangeEventRecord anEvent = new Event.OwnerChangeEventRecord();
        anEvent.setFromOwnerID(input.getFromOwnerID().getValue());
        anEvent.setToOwnerID(input.getToOwnerID().getValue());
        anEvent.setEventCommon(setEventData(objCommon, input.getClaimMemo()));

        //--------------------------------------------
        // Set change object Information
        //--------------------------------------------
        int srtChgObjLen = CimArrayUtils.getSize(input.getStrOwnerChangeObjectSeq());
        List<Event.OwnerChangeObjectEventData> changeObjects = new ArrayList<>();
        anEvent.setChangeObjects(changeObjects);
        for (int i = 0; i < srtChgObjLen; i++) {
            //---------------------------------------------------
            // Set change object information
            //---------------------------------------------------
            Event.OwnerChangeObjectEventData ownerChangeObjectEventData = new Event.OwnerChangeObjectEventData();
            changeObjects.add(i, ownerChangeObjectEventData);
            ownerChangeObjectEventData.setObjectName(input.getStrOwnerChangeObjectSeq().get(i).getObjectName());
            ownerChangeObjectEventData.setHashedInfo(input.getStrOwnerChangeObjectSeq().get(i).getHashedInfo());
        }

        //---------------------------------------------------
        // Create Owner Change Event
        //---------------------------------------------------
        log.info("Create Owner Change Event ");
        Event.OwnerChangeEventRecord eventRecord;
        eventRecord = anEvent;
        eventManager.createEvent(eventRecord, CimOwnerChangeEvent.class);
    }

    @Override
    public void bondingGroupEventMake(Infos.ObjCommon objCommon, String action, Infos.BondingGroupInfo strBondingGroupInfo, List<Infos.BondingMapInfo> strPartialReleaseSourceMapSeq) {
        //-----------------//
        //  Set Event Data //
        //-----------------//
        Event.BondingGroupEventRecord anEventRecord = new Event.BondingGroupEventRecord();
        anEventRecord.setEventCommon(setEventData(objCommon, strBondingGroupInfo.getClaimMemo()));
        anEventRecord.setAction(action);
        anEventRecord.setBondingGroupID(strBondingGroupInfo.getBondingGroupID());
        anEventRecord.setBondingGroupStatus(strBondingGroupInfo.getBondingGroupState());
        anEventRecord.setEquipmentID(ObjectIdentifier.fetchValue(strBondingGroupInfo.getTargetEquipmentID()));
        anEventRecord.setControlJobID(ObjectIdentifier.fetchValue(strBondingGroupInfo.getControlJobID()));

        List<Event.BondingMapEventData> bondingMapInfos = new ArrayList<>();
        anEventRecord.setBondingMapInfos(bondingMapInfos);
        boolean isPartialRelease = CimStringUtils.equals(action, SP_BONDINGGROUPACTION_PARTIALRELEASE);

        int index = 0;
        for (Infos.BondingMapInfo data : strBondingGroupInfo.getBondingMapInfoList()) {
            Event.BondingMapEventData mapEventData = new Event.BondingMapEventData();
            if (isPartialRelease) {
                mapEventData.setAction(BizConstant.SP_BONDINGGROUPACTION_PARTIALRELEASEDESTINATION);
            } else {
                mapEventData.setAction(action);
            }

            mapEventData.setBondingGroupID(strBondingGroupInfo.getBondingGroupID());
            mapEventData.setBondingSeqNo(CimNumberUtils.longValue(index++));
            mapEventData.setBaseLotID(ObjectIdentifier.fetchValue(data.getBaseLotID()));
            mapEventData.setBaseProductID(ObjectIdentifier.fetchValue(data.getBaseProductID()));
            mapEventData.setBaseWaferID(ObjectIdentifier.fetchValue(data.getBaseWaferID()));
            mapEventData.setBaseBondingSide(data.getBaseBondingSide());

            if (ObjectIdentifier.isNotEmpty(data.getActualTopLotID())) {
                mapEventData.setTopLotID(ObjectIdentifier.fetchValue(data.getActualTopLotID()));
            } else {
                mapEventData.setTopLotID(ObjectIdentifier.fetchValue(data.getPlanTopLotID()));
            }

            if (ObjectIdentifier.isNotEmpty(data.getActualTopProductID())) {
                mapEventData.setTopProductID(ObjectIdentifier.fetchValue(data.getActualTopProductID()));
            } else {
                mapEventData.setTopProductID(ObjectIdentifier.fetchValue(data.getPlanTopProductID()));
            }

            if (ObjectIdentifier.isNotEmpty(data.getActualTopWaferID())) {
                mapEventData.setTopWaferID(ObjectIdentifier.fetchValue(data.getActualTopWaferID()));
            } else {
                mapEventData.setTopWaferID(ObjectIdentifier.fetchValue(data.getPlanTopLotID()));
            }

            if (CimStringUtils.isNotEmpty(data.getActualTopBondingSide())) {
                mapEventData.setTopBondingSide(data.getActualTopBondingSide());
            } else {
                mapEventData.setTopBondingSide(data.getPlanTopBondingSide());
            }
            bondingMapInfos.add(mapEventData);
        }

        if (isPartialRelease && null != strPartialReleaseSourceMapSeq) {
            index = 0;
            for (Infos.BondingMapInfo data : strPartialReleaseSourceMapSeq) {
                Event.BondingMapEventData mapEventData = new Event.BondingMapEventData();
                mapEventData.setAction(BizConstant.SP_BONDINGGROUPACTION_PARTIALRELEASESOURCE);

                mapEventData.setBondingGroupID(strBondingGroupInfo.getBondingGroupID());
                mapEventData.setBondingSeqNo(CimNumberUtils.longValue(index++));
                mapEventData.setBaseLotID(ObjectIdentifier.fetchValue(data.getBaseLotID()));
                mapEventData.setBaseProductID(ObjectIdentifier.fetchValue(data.getBaseProductID()));
                mapEventData.setBaseWaferID(ObjectIdentifier.fetchValue(data.getBaseWaferID()));
                mapEventData.setBaseBondingSide(data.getBaseBondingSide());

                if (ObjectIdentifier.isNotEmpty(data.getActualTopLotID())) {
                    mapEventData.setTopLotID(ObjectIdentifier.fetchValue(data.getActualTopLotID()));
                } else {
                    mapEventData.setTopLotID(ObjectIdentifier.fetchValue(data.getPlanTopLotID()));
                }

                if (ObjectIdentifier.isNotEmpty(data.getActualTopProductID())) {
                    mapEventData.setTopProductID(ObjectIdentifier.fetchValue(data.getActualTopProductID()));
                } else {
                    mapEventData.setTopProductID(ObjectIdentifier.fetchValue(data.getPlanTopProductID()));
                }

                if (ObjectIdentifier.isNotEmpty(data.getActualTopWaferID())) {
                    mapEventData.setTopWaferID(ObjectIdentifier.fetchValue(data.getActualTopWaferID()));
                } else {
                    mapEventData.setTopWaferID(ObjectIdentifier.fetchValue(data.getPlanTopLotID()));
                }

                if (CimStringUtils.isNotEmpty(data.getActualTopBondingSide())) {
                    mapEventData.setTopBondingSide(data.getActualTopBondingSide());
                } else {
                    mapEventData.setTopBondingSide(data.getPlanTopBondingSide());
                }
                bondingMapInfos.add(mapEventData);
            }
        }

        //-----------------//
        //  Put Event Data //
        //-----------------//
        eventManager.createEvent(anEventRecord, CimBondingGroupEvent.class);
    }

    @Override
    public void waferStackingEventMake(Infos.ObjCommon objCommon, Infos.BondingGroupInfo bondingGroupInfo) {
        Optional.ofNullable(bondingGroupInfo).ifPresent(bondGroupInfo -> {
            //--------------//
            //  Initialize  //
            //--------------//
            Event.LotWaferStackEventRecord aBaseEventRecord = new Event.LotWaferStackEventRecord();
            Event.LotWaferStackEventRecord aTopEventRecord = new Event.LotWaferStackEventRecord();

            //-----------------//
            //  Set Event Data //
            //-----------------//
            aBaseEventRecord.setEventCommon(setEventData(objCommon, bondingGroupInfo.getClaimMemo()));
            aTopEventRecord.setEventCommon(setEventData(objCommon, bondingGroupInfo.getClaimMemo()));

            aBaseEventRecord.setBondingGroupID(bondGroupInfo.getBondingGroupID());
            aTopEventRecord.setBondingGroupID(bondGroupInfo.getBondingGroupID());

            aBaseEventRecord.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_BASE);
            aTopEventRecord.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_TOP);

            aBaseEventRecord.setEquipmentID(bondGroupInfo.getTargetEquipmentID().getValue());
            aTopEventRecord.setEquipmentID(bondGroupInfo.getTargetEquipmentID().getValue());

            aBaseEventRecord.setControlJobID(bondGroupInfo.getControlJobID().getValue());
            aTopEventRecord.setControlJobID(bondGroupInfo.getControlJobID().getValue());

            //----------------------------------------------------------------------
            // Collect uniquely pairs of BaseLot and TopLot
            //----------------------------------------------------------------------
            List<ObjectIdentifier> baseLotIDs = new ArrayList<>();
            List<ObjectIdentifier> topLotIDs = new ArrayList<>();
            List<Infos.BondingMapInfo> bondingMapInfoList = bondGroupInfo.getBondingMapInfoList();
            ObjectIdentifier tmpbaseLotID = ObjectIdentifier.emptyIdentifier();
            ObjectIdentifier tmptopLotID = ObjectIdentifier.emptyIdentifier();

            for (Infos.BondingMapInfo bondingMapInfo : bondingMapInfoList) {
                if (!CimObjectUtils.equals(tmpbaseLotID, bondingMapInfo.getBaseLotID())
                        || !CimObjectUtils.equals(tmptopLotID, bondingMapInfo.getActualTopLotID())) {
                    baseLotIDs.add(bondingMapInfo.getBaseLotID());
                    topLotIDs.add(bondingMapInfo.getActualTopLotID());
                    tmpbaseLotID = bondingMapInfo.getBaseLotID();
                    tmptopLotID = bondingMapInfo.getActualTopLotID();
                }
            }

            int size = baseLotIDs.size();
            for (int i = 0; i < size; i++) {
                ObjectIdentifier baseLotId = baseLotIDs.get(i);
                ObjectIdentifier topLotId = topLotIDs.get(i);

                CimLot aBaseLot = baseCoreFactory.getBO(CimLot.class, baseLotId);
                CimLot aTopLot = baseCoreFactory.getBO(CimLot.class, topLotId);

                log.info("Get Lot Event Data.");
                Event.LotEventData baseLotEventData = aBaseLot.getEventData();
                Event.LotEventData topLotEventData = aTopLot.getEventData();

                CimProcessOperation aBasePrePO = Optional.ofNullable(aBaseLot.getResponsibleProcessOperation()).orElseGet(aBaseLot::getProcessOperation);
                CimProcessOperation aTopPrePO = Optional.ofNullable(aTopLot.getResponsibleProcessOperation()).orElseGet(aTopLot::getProcessOperation);
                log.info("Get PO from the previous Operation.");


                Validations.check(null == aBasePrePO, retCodeConfig.getNotFoundPoForLot());
                Validations.check(null == aTopPrePO, retCodeConfig.getNotFoundPoForLot());

                log.info("Get PO Event Data.");
                ProcessDTO.PosProcessOperationEventData basePOEventData = aBasePrePO.getEventData();
                ProcessDTO.PosProcessOperationEventData topPOEventData = aTopPrePO.getEventData();

                Event.LotEventData lotDataForBase = new Event.LotEventData();
                lotDataForBase.setLotID(baseLotId.getValue());
                lotDataForBase.setLotType(baseLotEventData.getLotType());
                lotDataForBase.setCassetteID(baseLotEventData.getCassetteID());
                lotDataForBase.setLotStatus(baseLotEventData.getLotStatus());
                lotDataForBase.setCustomerID(baseLotEventData.getCustomerID());
                lotDataForBase.setPriorityClass(baseLotEventData.getPriorityClass());
                lotDataForBase.setProductID(baseLotEventData.getProductID());
                lotDataForBase.setOriginalWaferQuantity(baseLotEventData.getOriginalWaferQuantity());
                lotDataForBase.setCurrentWaferQuantity(baseLotEventData.getCurrentWaferQuantity());
                lotDataForBase.setProductWaferQuantity(baseLotEventData.getProductWaferQuantity());
                lotDataForBase.setControlWaferQuantity(baseLotEventData.getControlWaferQuantity());
                lotDataForBase.setHoldState(baseLotEventData.getHoldState());
                lotDataForBase.setBankID(baseLotEventData.getBankID());
                lotDataForBase.setRouteID(basePOEventData.getRouteID());
                lotDataForBase.setOperationNumber(basePOEventData.getOperationNumber());
                lotDataForBase.setOperationID(basePOEventData.getOperationID());
                lotDataForBase.setOperationPassCount(CimNumberUtils.longValue(basePOEventData.getOperationPassCount()));
                lotDataForBase.setObjrefPOS(basePOEventData.getObjrefPOS());
                lotDataForBase.setWaferHistoryTimeStamp(baseLotEventData.getWaferHistoryTimeStamp());
                lotDataForBase.setObjrefPO(basePOEventData.getObjrefPO());
                lotDataForBase.setObjrefMainPF(basePOEventData.getObjrefMainPF());
                lotDataForBase.setObjrefModulePOS(basePOEventData.getObjrefModulePOS());
                lotDataForBase.setSamplingWafers(baseLotEventData.getSamplingWafers());
                aBaseEventRecord.setLotData(lotDataForBase);
                aBaseEventRecord.setRelatedLotID(topLotId.getValue());

                Event.LotEventData lotDataForTop = new Event.LotEventData();
                lotDataForTop.setLotID(topLotId.getValue());
                lotDataForTop.setLotType(topLotEventData.getLotType());
                lotDataForTop.setCassetteID(topLotEventData.getCassetteID());
                lotDataForTop.setLotStatus(topLotEventData.getLotStatus());
                lotDataForTop.setCustomerID(topLotEventData.getCustomerID());
                lotDataForTop.setPriorityClass(topLotEventData.getPriorityClass());
                lotDataForTop.setProductID(topLotEventData.getProductID());
                lotDataForTop.setOriginalWaferQuantity(topLotEventData.getOriginalWaferQuantity());
                lotDataForTop.setCurrentWaferQuantity(topLotEventData.getCurrentWaferQuantity());
                lotDataForTop.setProductWaferQuantity(topLotEventData.getProductWaferQuantity());
                lotDataForTop.setControlWaferQuantity(topLotEventData.getControlWaferQuantity());
                lotDataForTop.setHoldState(topLotEventData.getHoldState());
                lotDataForTop.setBankID(topLotEventData.getBankID());
                lotDataForTop.setRouteID(topPOEventData.getRouteID());
                lotDataForTop.setOperationNumber(topPOEventData.getOperationNumber());
                lotDataForTop.setOperationID(topPOEventData.getOperationID());
                lotDataForTop.setOperationPassCount(CimNumberUtils.longValue(topPOEventData.getOperationPassCount()));
                lotDataForTop.setObjrefPOS(topPOEventData.getObjrefPOS());
                lotDataForTop.setWaferHistoryTimeStamp(topLotEventData.getWaferHistoryTimeStamp());
                lotDataForTop.setObjrefPO(topPOEventData.getObjrefPO());
                lotDataForTop.setObjrefMainPF(topPOEventData.getObjrefMainPF());
                lotDataForTop.setObjrefModulePOS(topPOEventData.getObjrefModulePOS());
                lotDataForTop.setSamplingWafers(topLotEventData.getSamplingWafers());
                aTopEventRecord.setLotData(lotDataForTop);
                aTopEventRecord.setRelatedLotID(baseLotId.getValue());

                List<Event.StackWaferEventData> stackWaferEventDatasForBase = new ArrayList<>();
                aBaseEventRecord.setWafers(stackWaferEventDatasForBase);

                List<Event.StackWaferEventData> stackWaferEventDatasForTop = new ArrayList<>();
                aTopEventRecord.setWafers(stackWaferEventDatasForTop);

                log.info("Get Wafer Event Data.");
                Optional.ofNullable(bondingMapInfoList).ifPresent(maps -> {
                    for (Infos.BondingMapInfo mapInfo : maps) {
                        if (ObjectIdentifier.equalsWithValue(mapInfo.getBaseLotID(), baseLotId)
                                && ObjectIdentifier.equalsWithValue(mapInfo.getActualTopLotID(), topLotId)) {
                            CimWafer aBaseWafer = baseCoreFactory.getBO(CimWafer.class, mapInfo.getBaseWaferID());
                            CimWafer aTopWafer = baseCoreFactory.getBO(CimWafer.class, mapInfo.getActualTopWaferID());

                            String baseAliasWaferName = aBaseWafer.getAliasWaferName();
                            String topAliasWaferName = aTopWafer.getAliasWaferName();

                            log.info("baseAliasWaferName : " + baseAliasWaferName);
                            log.info("topAliasWaferName : " + topAliasWaferName);

                            boolean baseIsControlWafer = aBaseWafer.isControlWafer();
                            boolean topIsControlWafer = aTopWafer.isControlWafer();

                            CimCassette aBaseCassette = null;
                            MaterialContainer aMtrlCntr = aBaseWafer.getMaterialContainer();

                            String baseCassetteID = aMtrlCntr.getIdentifier();
                            log.info("baseCassetteID : " + baseCassetteID);

                            Integer baseSlotNo = aBaseWafer.getPosition();
                            log.info("baseSlotNo : " + baseSlotNo);

                            Event.StackWaferEventData waferForBase = new Event.StackWaferEventData();
                            stackWaferEventDatasForBase.add(waferForBase);
                            waferForBase.setOriginalWaferID(mapInfo.getBaseWaferID().getValue());
                            waferForBase.setWaferID(mapInfo.getBaseWaferID().getValue());
                            waferForBase.setAliasWaferName(baseAliasWaferName);
                            waferForBase.setControlWaferFlag(baseIsControlWafer);
                            waferForBase.setRelatedWaferID(mapInfo.getActualTopWaferID().getValue());
                            waferForBase.setOriginalCassetteID(baseCassetteID);
                            waferForBase.setOriginalSlotNumber(CimNumberUtils.longValue(baseSlotNo));
                            waferForBase.setDestinationCassetteID(baseCassetteID);
                            waferForBase.setDestinationSlotNumber(CimNumberUtils.longValue(baseSlotNo));

                            if (CimStringUtils.equals(mapInfo.getActualTopBondingSide(), BizConstant.SP_BONDINGSIDE_TOP)) {
                                log.info("baseBondingSide == SP_BondingSide_Bottom");
                                waferForBase.setOriginalAliasWaferName(baseAliasWaferName);
                            } else {
                                log.info("baseBondingSide == SP_BondingSide_Top");
                                ProductDTO.StackedWafer in_PosStackedWafer = new ProductDTO.StackedWafer();
                                in_PosStackedWafer.setTopLotID(mapInfo.getActualTopLotID());
                                in_PosStackedWafer.setTopWaferID(mapInfo.getActualTopWaferID());
                                ProductDTO.StackedWafer outPosStackedWafer = aBaseWafer.findStackedWafer(in_PosStackedWafer);
                                Validations.check(null == outPosStackedWafer, retCodeConfigEx.getNotFoundStackedWafer());
                                waferForBase.setOriginalAliasWaferName(outPosStackedWafer.getPreviousAliasWaferName());
                            }

                            Event.StackWaferEventData waferForTop = new Event.StackWaferEventData();
                            stackWaferEventDatasForTop.add(waferForTop);
                            waferForTop.setOriginalWaferID(mapInfo.getActualTopWaferID().getValue());
                            waferForTop.setWaferID(mapInfo.getActualTopWaferID().getValue());
                            waferForTop.setAliasWaferName(topAliasWaferName);
                            waferForTop.setOriginalAliasWaferName(topAliasWaferName);
                            waferForTop.setControlWaferFlag(topIsControlWafer);
                            waferForTop.setRelatedWaferID(mapInfo.getBaseWaferID().getValue());
                            waferForTop.setOriginalCassetteID(mapInfo.getActualTopCarrierID().getValue());
                            waferForTop.setOriginalSlotNumber(mapInfo.getActualTopSlotNo());
                            waferForTop.setDestinationCassetteID(baseCassetteID);
                            waferForTop.setDestinationSlotNumber(CimNumberUtils.longValue(baseSlotNo));
                        }
                    }
                });

                // Put into the queue of PosEventManager
                eventManager.createEvent(aBaseEventRecord, CimLotWaferStackEvent.class);
                eventManager.createEvent(aTopEventRecord, CimLotWaferStackEvent.class);
            }
        });
    }

    @Override
    public void waferStackingCancelEventMake(Infos.ObjCommon objCommon, List<Infos.StackedWaferInfo> strStackedWaferInfoSeq, String claimMemo) {
        Validations.check(null == strStackedWaferInfoSeq, retCodeConfig.getInvalidParameter());
        Event.LotWaferStackEventRecord aBaseEventRecord = new Event.LotWaferStackEventRecord();
        Event.LotWaferStackEventRecord aTopEventRecord = new Event.LotWaferStackEventRecord();

        aBaseEventRecord.setEventCommon(setEventData(objCommon, claimMemo));
        aTopEventRecord.setEventCommon(setEventData(objCommon, claimMemo));

        aBaseEventRecord.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_BASECANCEL);
        aTopEventRecord.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_TOPCANCEL);

        //----------------------------------------------------------------------
        // Collect uniquely pairs of BaseLot and TopLot
        //----------------------------------------------------------------------
        //----------------------------------------------------------------------
        // Collect uniquely pairs of BaseLot and TopLot
        //----------------------------------------------------------------------
        List<ObjectIdentifier> baseLotIDs = new ArrayList<>();
        List<ObjectIdentifier> topLotIDs = new ArrayList<>();
        ObjectIdentifier tmpbaseLotID = ObjectIdentifier.emptyIdentifier();
        ObjectIdentifier tmptopLotID = ObjectIdentifier.emptyIdentifier();

        for (Infos.StackedWaferInfo stackedWaferInfo : strStackedWaferInfoSeq) {
            if (!CimObjectUtils.equals(tmpbaseLotID, stackedWaferInfo.getBaseLotID())
                    || !CimObjectUtils.equals(tmptopLotID, stackedWaferInfo.getTopLotID())) {
                baseLotIDs.add(stackedWaferInfo.getBaseLotID());
                topLotIDs.add(stackedWaferInfo.getTopLotID());
                tmpbaseLotID = stackedWaferInfo.getBaseLotID();
                tmptopLotID = stackedWaferInfo.getTopLotID();
            }
        }


        int size = baseLotIDs.size();
        for (int i = 0; i < size; i++) {
            ObjectIdentifier baseLotId = baseLotIDs.get(i);
            ObjectIdentifier topLotId = topLotIDs.get(i);

            CimLot aBaseLot = baseCoreFactory.getBO(CimLot.class, baseLotId);
            Validations.check(null == aBaseLot, retCodeConfig.getNotFoundLot());
            CimLot aTopLot = baseCoreFactory.getBO(CimLot.class, topLotId);
            Validations.check(null == aTopLot, retCodeConfig.getNotFoundLot());

            Event.LotEventData baseLotEventData = aBaseLot.getEventData();
            Event.LotEventData topLotEventData = aTopLot.getEventData();

            aBaseEventRecord.setLotData(baseLotEventData);
            aTopEventRecord.setLotData(topLotEventData);

            aBaseEventRecord.setRelatedLotID(topLotId.getValue());
            aTopEventRecord.setRelatedLotID(baseLotId.getValue());

            List<Event.StackWaferEventData> wafersForBase = new ArrayList<>();
            aBaseEventRecord.setWafers(wafersForBase);

            List<Event.StackWaferEventData> wafersForTop = new ArrayList<>();
            aTopEventRecord.setWafers(wafersForTop);


            Optional.of(strStackedWaferInfoSeq).ifPresent(list -> list.forEach(wafer -> {
                if (ObjectIdentifier.equalsWithValue(wafer.getBaseLotID(), baseLotId)
                        && ObjectIdentifier.equalsWithValue(wafer.getTopLotID(), topLotId)) {
                    CimWafer aBaseWafer = baseCoreFactory.getBO(CimWafer.class, wafer.getBaseWaferID());
                    Validations.check(null == aBaseWafer, retCodeConfig.getNotFoundWafer());
                    CimWafer aTopWafer = baseCoreFactory.getBO(CimWafer.class, wafer.getTopWaferID());
                    Validations.check(null == aTopWafer, retCodeConfig.getNotFoundWafer());

                    boolean baseIsControlWafer = aBaseWafer.isControlWafer();
                    boolean topIsControlWafer = aTopWafer.isControlWafer();

                    Event.StackWaferEventData waferForBase = new Event.StackWaferEventData();
                    wafersForBase.add(waferForBase);
                    waferForBase.setOriginalWaferID(wafer.getBaseWaferID().getValue());
                    waferForBase.setWaferID(wafer.getBaseWaferID().getValue());
                    waferForBase.setAliasWaferName(wafer.getMaterialOffset() > 0 ? wafer.getBaseAliasWaferName() : wafer.getBasePreviousAliasWaferName());
                    waferForBase.setOriginalAliasWaferName(wafer.getBaseAliasWaferName());
                    waferForBase.setControlWaferFlag(baseIsControlWafer);
                    waferForBase.setRelatedWaferID(wafer.getTopWaferID().getValue());
                    waferForBase.setOriginalCassetteID(wafer.getCassetteID().getValue());
                    waferForBase.setOriginalSlotNumber(wafer.getSlotNo());
                    waferForBase.setDestinationCassetteID(wafer.getCassetteID().getValue());
                    waferForBase.setDestinationSlotNumber(wafer.getSlotNo());

                    Event.StackWaferEventData waferForTop = new Event.StackWaferEventData();
                    wafersForTop.add(waferForTop);
                    waferForTop.setOriginalWaferID(wafer.getTopWaferID().getValue());
                    waferForTop.setWaferID(wafer.getTopWaferID().getValue());
                    waferForTop.setAliasWaferName(wafer.getTopAliasWaferName());
                    waferForTop.setOriginalAliasWaferName(wafer.getTopAliasWaferName());
                    waferForTop.setControlWaferFlag(topIsControlWafer);
                    waferForTop.setRelatedWaferID(wafer.getBaseWaferID().getValue());
                    waferForTop.setOriginalCassetteID(wafer.getCassetteID().getValue());
                    waferForTop.setOriginalSlotNumber(wafer.getSlotNo());
                    waferForTop.setDestinationSlotNumber(0L);
                }
            }));

            // Put into the queue of PosEventManager
            eventManager.createEvent(aBaseEventRecord, CimLotWaferStackEvent.class);
            eventManager.createEvent(aTopEventRecord, CimLotWaferStackEvent.class);
        }
    }

    @Override
    public void APCIFPointUpdateEventMake(Infos.ObjCommon objCommon, String transactionID, Infos.APCIf apcIf, String opeCategory) {
        Event.APCInterfaceEventRecord anAPCIFEventRecord = new Event.APCInterfaceEventRecord();
        anAPCIFEventRecord.setEquipmentID(ObjectIdentifier.fetchValue(apcIf.getEquipmentID()));
        anAPCIFEventRecord.setAPC_systemName(apcIf.getAPCSystemName());
        anAPCIFEventRecord.setOperationCategory(opeCategory);
        anAPCIFEventRecord.setEquipmentDescription(apcIf.getEqpDescription());
        anAPCIFEventRecord.setIgnoreAbleFlag(apcIf.isAPCIgnoreable());
        anAPCIFEventRecord.setAPC_responsibleUserID(ObjectIdentifier.fetchValue(apcIf.getAPCRep1UserID()));
        anAPCIFEventRecord.setAPC_subResponsibleUserID(ObjectIdentifier.fetchValue(apcIf.getAPCRep2UserID()));
        anAPCIFEventRecord.setAPC_configState(apcIf.getAPCConfigStatus());
        anAPCIFEventRecord.setAPC_registeredUserID(ObjectIdentifier.fetchValue(apcIf.getRegisteredUserID()));
        anAPCIFEventRecord.setRegisteredTime(apcIf.getRegisteredTimeStamp());
        anAPCIFEventRecord.setRegisteredMemo(apcIf.getRegisteredMemo());
        anAPCIFEventRecord.setApprovedUserID(ObjectIdentifier.fetchValue(apcIf.getApprovedUserID()));
        anAPCIFEventRecord.setApprovedTime(apcIf.getApprovedTimeStamp());
        anAPCIFEventRecord.setApprovedMemo(apcIf.getApprovedMemo());

        anAPCIFEventRecord.setEventCommon(setEventData(objCommon, null));
        eventManager.createEvent(anAPCIFEventRecord, CimAPCInterfaceEvent.class);
    }

    @Override
    public void equipmentSLMSwitchChangeEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier equipmentID, String fmcMode, String claimMemo) {
        Event.SLMSwitchEventRecord anEventRecord = new Event.SLMSwitchEventRecord();
        anEventRecord.setEventCommon(setEventData(objCommon, claimMemo));
        //-----------------//
        //  Set Event Data //
        //-----------------//
        anEventRecord.setMachineID(ObjectIdentifier.fetchValue(equipmentID));
        anEventRecord.setFmcMode(fmcMode);
        eventManager.createEvent(anEventRecord, CimSLMSwitchEvent.class);
    }

    @Override
    public void seasonPlanEventMake(Infos.ObjCommon objCommon, String action, Infos.Season season, String claimMemo) {
        Event.SeasonPlanEventRecord anEventRecord = new Event.SeasonPlanEventRecord();
        anEventRecord.setEventCommon(setEventData(objCommon, claimMemo));
        //-----------------//
        //  Set Event Data //
        //-----------------//
        anEventRecord.setAction(action);
        anEventRecord.setActionTime(objCommon.getTimeStamp().getReportTimeStamp());
        anEventRecord.setCreateTime(season.getCreateTime());
        anEventRecord.setSeasonID(season.getSeasonID());
        anEventRecord.setCondType(season.getCondType());
        anEventRecord.setSeasonType(season.getSeasonType());
        anEventRecord.setProductID(season.getProductID());
        anEventRecord.setEqpID(season.getEqpID());
        anEventRecord.setLastSeasonTime(season.getLastSeasonTime());
        anEventRecord.setUserID(season.getUserID());
        anEventRecord.setStatus(season.getStatus());
        List<Infos.SeasonChamber> chambers = season.getChambers();
        List<Event.SeasonChamberEventData> chamberEventRecords = new ArrayList<>();
        Optional.ofNullable(chambers).ifPresent(list -> list.forEach(data -> {
            Event.SeasonChamberEventData eventRecord = new Event.SeasonChamberEventData();
            eventRecord.setChamberID(data.getChamberID());
            chamberEventRecords.add(eventRecord);
        }));
        anEventRecord.setChambers(chamberEventRecords);

        List<Event.SeasonProdRecipeEventData> prodRecipeEventRecords = new ArrayList<>();
        Optional.ofNullable(season.getRecipes()).ifPresent(list -> list.forEach(data -> {
            Event.SeasonProdRecipeEventData record = new Event.SeasonProdRecipeEventData();
            record.setRecipeID(data.getRecipeID());
            prodRecipeEventRecords.add(record);
        }));
        anEventRecord.setRecipes(prodRecipeEventRecords);

        List<Event.SeasonProductEventData> productEventRecords = new ArrayList<>();
        Optional.ofNullable(season.getSeasonProducts()).ifPresent(list -> list.forEach(data -> {
            Event.SeasonProductEventData record = new Event.SeasonProductEventData();
            record.setProductID(data.getProductID());
            record.setQuantity(data.getQuantity());
            productEventRecords.add(record);
        }));
        anEventRecord.setSeasonProducts(productEventRecords);

        Event.SeasonParamEventData paramEventRecord = null;
        Infos.SeasonParam param = season.getParam();
        if (null != param) {
            paramEventRecord = new Event.SeasonParamEventData();
            paramEventRecord.setFromRecipeGroup(param.getFromRecipeGroup());
            paramEventRecord.setIntervalBetweenSeason(param.getIntervalBetweenSeason());
            paramEventRecord.setMaxIdleTime(param.getMaxIdleTime());
//            paramEventRecord.setNoIdleFlag(param.getNoIdleFlag());
            paramEventRecord.setSeasonGroupFlag(param.getSeasonGroupFlag());
            paramEventRecord.setWaitFlag(param.getWaitFlag());
            paramEventRecord.setToRecipeGroup(param.getToRecipeGroup());
        }
        anEventRecord.setParam(paramEventRecord);
        anEventRecord.setPriority(season.getPriority());

        eventManager.createEvent(anEventRecord, CimSeasonEvent.class);
    }

    @Override
    public void seasonJobEventMake(Infos.ObjCommon objCommon, String action, Infos.SeasonJob seasonJob, String claimMemo) {
        Event.SeasonJobEventRecord anEventRecord = new Event.SeasonJobEventRecord();
        anEventRecord.setEventCommon(setEventData(objCommon, claimMemo));
        //-----------------//
        //  Set Event Data //
        //-----------------//
        anEventRecord.setAction(action);
        anEventRecord.setActionTime(objCommon.getTimeStamp().getReportTimeStamp());
        anEventRecord.setCreateTime(seasonJob.getCreateTime());
        anEventRecord.setCondType(seasonJob.getCondType());
        anEventRecord.setSeasonType(seasonJob.getSeasonType());
        anEventRecord.setSeasonProductID(seasonJob.getSeasonProductID());
        anEventRecord.setEqpID(seasonJob.getEqpID());
        anEventRecord.setUserID(seasonJob.getUserID());
        anEventRecord.setPriority(seasonJob.getPriority());
        anEventRecord.setSeasonJobID(seasonJob.getSeasonJobID());
        anEventRecord.setSeasonID(seasonJob.getSeasonID());
        anEventRecord.setChamber(seasonJob.getChamber());
        anEventRecord.setSeasonJobStatus(seasonJob.getSeasonJobStatus());
        anEventRecord.setSeasonLotID(seasonJob.getSeasonLotID());
        anEventRecord.setSeasonCarrierID(seasonJob.getSeasonCarrierID());
        anEventRecord.setLotID(seasonJob.getLotID());
        anEventRecord.setSeasonRcpID(seasonJob.getSeasonRcpID());
        anEventRecord.setCarrierID(seasonJob.getCarrierID());
        anEventRecord.setWaferQty(seasonJob.getWaferQty());
        anEventRecord.setMinSeasonWaferCount(seasonJob.getMinSeasonWaferCount());
        anEventRecord.setMaxIdleTime(seasonJob.getMaxIdleTime());
        anEventRecord.setIntervalBetweenSeason(seasonJob.getIntervalBetweenSeason());
        anEventRecord.setSeasonGroupFlag(seasonJob.getSeasonGroupFlag());
//        anEventRecord.setNoIdleFlag(seasonJob.getNoIdleFlag());
        anEventRecord.setWaitFlag(seasonJob.getWaitFlag());
        anEventRecord.setFromRecipe(seasonJob.getFromRecipe());
        anEventRecord.setToRecipe(seasonJob.getToRecipe());
        anEventRecord.setMoveInTime(seasonJob.getMoveInTime());
        anEventRecord.setMoveOutTime(seasonJob.getMoveOutTime());
        anEventRecord.setOpeMemo(seasonJob.getOpeMemo());

        eventManager.createEvent(anEventRecord, CimSeasonJobEvent.class);
    }

    @Override
    public void equipmentContainerMaxRsvCountUpdateEventMake(Infos.ObjCommon objCommon, Params.FmcRsvMaxCountUpdateReqInParams fmcRsvMaxCountUpdateReqInParams) {
        //--------------//
        //  Initialize  //
        //--------------//
        Event.SLMMaxReserveCountEventRecord anEventRecord = new Event.SLMMaxReserveCountEventRecord();
        anEventRecord.setEventCommon(setEventData(objCommon, fmcRsvMaxCountUpdateReqInParams.getClaimMemo()));
        anEventRecord.setMachineID(fmcRsvMaxCountUpdateReqInParams.getEquipmentID().getValue());
        anEventRecord.setMachineContainerID(fmcRsvMaxCountUpdateReqInParams.getEquipmentContainerID().getValue());
        anEventRecord.setMaxReserveCount(fmcRsvMaxCountUpdateReqInParams.getMaxRsvCount().longValue());

        eventManager.createEvent(anEventRecord, CimSLMMaxReserveCountEvent.class);
    }

    @Override
    public void runCardEventMake(Infos.ObjCommon objCommon, String action, Infos.RunCardInfo runCardInfo) {
        Event.RunCardEventRecord anEventRecord = new Event.RunCardEventRecord();
        anEventRecord.setEventCommon(setEventData(objCommon, null));
        anEventRecord.setAction(action);
        anEventRecord.setRunCardID(runCardInfo.getRunCardID());
        anEventRecord.setLotID(runCardInfo.getLotID());
        anEventRecord.setRunCardState(runCardInfo.getRunCardState());
        anEventRecord.setOwner(runCardInfo.getOwner());
        anEventRecord.setExtApprovalFlag(runCardInfo.getExtApprovalFlag());
        anEventRecord.setCreateTime(runCardInfo.getCreateTime());
        anEventRecord.setUpdateTime(runCardInfo.getUpdateTime());
        anEventRecord.setApprovers(runCardInfo.getApprovers());
        anEventRecord.setClaimMemo(runCardInfo.getClaimMemo());
        anEventRecord.setAutoCompleteFlag(runCardInfo.getAutoCompleteFlag());

        eventManager.createEvent(anEventRecord, CimRunCardEvent.class);

    }

    @Override
    public void durableJobStatusChangeEventMake(Infos.ObjCommon objCommon, Infos.DurableJobStatusChangeEvent params, String action) {
        Validations.check(null == objCommon || null == params, retCodeConfig.getInvalidInputParam());

        String durableCategory = params.getDurableCategory();
        ObjectIdentifier durableID = params.getDurableID();

        Event.DurableJobStatusChangeEventRecord anEventRecord = new Event.DurableJobStatusChangeEventRecord();
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        anEventRecord.setAction(action);
        anEventRecord.setDurableType(durableCategory);
        anEventRecord.setDurableID(ObjectIdentifier.fetchValue(durableID));
        anEventRecord.setJobStatus(params.getJobStatus());


        Durable aDurable = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                aDurable = baseCoreFactory.getBO(CimCassette.class, durableID);
                break;
            case SP_DURABLECAT_RETICLEPOD:
                aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                break;
            case SP_DURABLECAT_RETICLE:
                aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                break;
        }
        Validations.check(null == aDurable, retCodeConfig.getNotFoundDurable());
        CimDurableProcessOperation durablePO = aDurable.getDurableProcessOperation();
        Validations.check(null == durablePO, retCodeConfig.getNotFoundDurablePo());

        anEventRecord.setProcess(durablePO.getMainProcessDefinition().getIdentifier());
        anEventRecord.setRoute(durablePO.getModuleProcessDefinition().getIdentifier());
        anEventRecord.setStep(durablePO.getProcessDefinition().getIdentifier());
        anEventRecord.setOperationNo(durablePO.getOperationNumber());
        anEventRecord.setStatusChangeTime(aDurable.getJobStatusChangeTime().toString());

        anEventRecord.setEquipmentID(params.getEquipmentID());
        anEventRecord.setChamberID(params.getChamberID());

        eventManager.createEvent(anEventRecord, CimDurableJobStatusChangeEvent.class);
    }

    @Override
    public void reticleOperationEventMake(Infos.ObjCommon objCommon, Inputs.ReticleOperationEventMakeParams params) {
        Event.ReticleOperationEventRecord anEventRecord = new Event.ReticleOperationEventRecord();
        anEventRecord.setInspectionType(params.getInspectionType());
        anEventRecord.setOpeCategory(params.getOpeCategory());
        anEventRecord.setReasonCode(params.getReasonCode());
        anEventRecord.setReticleGrade(params.getReticleGrade());
        anEventRecord.setReticleID(params.getReticleID());
        anEventRecord.setReticleLocation(params.getReticleLocation());
        anEventRecord.setReticleObj(params.getReticleObj());
        anEventRecord.setReticlePodID(params.getReticlePodID());
        anEventRecord.setReticleStatus(params.getReticleStatus());
        anEventRecord.setReticleSubStatus(params.getReticleSubStatus());
        anEventRecord.setReticleType(params.getReticleType());
        anEventRecord.setInspectionType(params.getInspectionType());
        anEventRecord.setEqpID(params.getEqpID());
        anEventRecord.setStockerID(params.getStockerID());
        anEventRecord.setTransferStatus(params.getTransferStatus());
        anEventRecord.setClaimMemo(params.getClaimMemo());
        anEventRecord.setEventCommon(setEventData(objCommon, params.getClaimMemo()));
        eventManager.createEvent(anEventRecord, CimReticleOperationEvent.class);
    }

    @Override
    public void collectedDataEventForPreviousOperationMake(Infos.ObjCommon objCommon,
                                                           String transactionID,
                                                           List<Infos.StartCassette> tmpStartCassette,
                                                           ObjectIdentifier controlJobID,
                                                           ObjectIdentifier equipmentID,
                                                           String claimMemo) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Event.CollectedDataEventRecord anEventRecord = new Event.CollectedDataEventRecord();

        /*-------------------------------*/
        /*   Loop for strStartCassette   */
        /*-------------------------------*/
        if (CimArrayUtils.isNotEmpty(tmpStartCassette)) {
            for (Infos.StartCassette startCassette : tmpStartCassette) {
                if (CimArrayUtils.isNotEmpty(startCassette.getLotInCassetteList())) {
                    for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                        /*---------------------------*/
                        /*   Omit Not-OpeStart Lot   */
                        /*---------------------------*/
                        if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                            continue;
                        }

                        /*-----------------------------*/
                        /*   Omit Non-DataCollection   */
                        /*-----------------------------*/
                        if (CimArrayUtils.isEmpty(lotInCassette.getStartRecipe().getDcDefList())) {
                            continue;
                        }

                        /*--------------------*/
                        /*   Get Lot ojbect   */
                        /*--------------------*/
                        log.info("Get Lot ojbect");
                        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());
                        Validations.check(null == aLot, retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(lotInCassette.getLotID()));

                        /*------------------------------------*/
                        /*   Get PO from Previous Operation   */
                        /*------------------------------------*/
                        log.info("Get PO from the previous Operation.");
                        CimProcessOperation aPrePO = aLot.getPreviousProcessOperation();
                        Validations.check(null == aPrePO, retCodeConfig.getNotFoundProcessOperation(), ObjectIdentifier.fetchValue(lotInCassette.getLotID()));

                        /*--------------------------------*/
                        /*   Check Validity of strDCDef   */
                        /*--------------------------------*/
                        log.info("Check Validity of DCDef");
                        Boolean skipFlag = false;
                        for (Infos.DataCollectionInfo dataCollectionInfo : lotInCassette.getStartRecipe().getDcDefList()) {
                            if (ObjectIdentifier.isEmptyWithValue(dataCollectionInfo.getDataCollectionDefinitionID())) {
                                log.info("set skipFlag = TRUE");
                                skipFlag = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isTrue(skipFlag)) {
                            log.info("skipFlag == TRUE");
                            continue;
                        }

                        //--------------------------------------------------------------
                        //  Set Event Record Data
                        //
                        //  typedef struct posCollectedDataEventRecord_struct {
                        //      posLotEventData                     measuredLotData;
                        //      string                              monitorGroupID;
                        //      posProcessedLotEventDataSequence    processedLots;
                        //      string                              equipmentID;
                        //      string                              logicalRecipeID;
                        //      string                              machineRecipeID;
                        //      string                              physicalRecipeID;
                        //      posEventData                        eventCommon;
                        //  } posCollectedDataEventRecord;
                        //
                        //  typedef struct posLotEventData_struct {
                        //      string                    * lotID;
                        //      string                    * lotType;
                        //      string                    * cassetteID;
                        //      string                    * lotStatus;
                        //      string                    : customerID;
                        //      long                      * priorityClass;
                        //      string                    : productID;
                        //      long                      * originalWaferQuantity;
                        //      long                      * currentWaferQuantity;
                        //      long                      * productWaferQuantity;
                        //      long                      * controlWaferQuantity;
                        //      string                    * holdState;
                        //      string                    : bankID;
                        //      string                    + routeID;
                        //      string                    + operationNumber;
                        //      string                    + operationID;
                        //      long                      + operationPassCount;
                        //      string                    + objrefPOS;
                        //      string                    + objrefPO;
                        //      string                    + objrefMainPF;
                        //      string                    + objrefModulePOS;
                        //      string                    * waferHistoryTimeStamp;
                        //      stringSequence              samplingWafers;
                        //  } posLotEventData;
                        //
                        //  typedef struct posProcessedLotEventData_struct {
                        //      string                      processLotID;
                        //      string                      processRouteID;
                        //      string                      processOperationNumber;
                        //      long                        processOperationPassCount;
                        //      string                      processObjrefPO;
                        //  } posProcessedLotEventData;
                        //
                        //  typedef struct posEventData_struct {
                        //      string                      transactionID;
                        //      string                      eventTimeStamp;
                        //      double                      eventShopDate;
                        //      string                      userID;
                        //      string                      eventMemo;
                        //      string                      eventCreationTimeStamp;
                        //  } posEventData;
                        //
                        //--------------------------------------------------------------

                        /*------------------------*/
                        /*   Set Lot Event Data   */
                        /*------------------------*/
                        /*=== lot related info ===*/
                        anEventRecord.setMeasuredLotData(new Event.LotEventData());
                        anEventRecord.getMeasuredLotData().setLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                        anEventRecord.getMeasuredLotData().setLotType(lotInCassette.getLotType());

                        log.info("anEventRecord.measuredLotData.lotID: {}", anEventRecord.getMeasuredLotData().getLotID());
                        log.info("anEventRecord.measuredLotData.lotType: {}", anEventRecord.getMeasuredLotData().getLotType());

                        Event.LotEventData lotEventData = aLot.getEventData();

                        anEventRecord.getMeasuredLotData().setCassetteID(lotEventData.getCassetteID());
                        anEventRecord.getMeasuredLotData().setLotStatus(lotEventData.getLotStatus());
                        anEventRecord.getMeasuredLotData().setCustomerID(lotEventData.getCustomerID());
                        anEventRecord.getMeasuredLotData().setPriorityClass(lotEventData.getPriorityClass());
                        anEventRecord.getMeasuredLotData().setProductID(lotEventData.getProductID());
                        anEventRecord.getMeasuredLotData().setOriginalWaferQuantity(lotEventData.getOriginalWaferQuantity());
                        anEventRecord.getMeasuredLotData().setCurrentWaferQuantity(lotEventData.getCurrentWaferQuantity());
                        anEventRecord.getMeasuredLotData().setProductWaferQuantity(lotEventData.getProductWaferQuantity());
                        anEventRecord.getMeasuredLotData().setControlWaferQuantity(lotEventData.getControlWaferQuantity());
                        anEventRecord.getMeasuredLotData().setHoldState(lotEventData.getHoldState());
                        anEventRecord.getMeasuredLotData().setBankID(lotEventData.getBankID());
                        anEventRecord.getMeasuredLotData().setWaferHistoryTimeStamp(lotEventData.getWaferHistoryTimeStamp());

                        ProcessDTO.PosProcessOperationEventData poEventData = aPrePO.getEventData();

                        anEventRecord.getMeasuredLotData().setRouteID(poEventData.getRouteID());
                        anEventRecord.getMeasuredLotData().setOperationNumber(poEventData.getOperationNumber());
                        anEventRecord.getMeasuredLotData().setOperationID(poEventData.getOperationID());
                        anEventRecord.getMeasuredLotData().setOperationPassCount(CimNumberUtils.longValue(poEventData.getOperationPassCount()));
                        anEventRecord.getMeasuredLotData().setObjrefPOS(poEventData.getObjrefPOS());
                        anEventRecord.getMeasuredLotData().setObjrefPO(poEventData.getObjrefPO());
                        anEventRecord.getMeasuredLotData().setObjrefMainPF(poEventData.getObjrefMainPF());
                        anEventRecord.getMeasuredLotData().setObjrefModulePOS(poEventData.getObjrefModulePOS());

                        /*------------------------------*/
                        /*   Set Equipment and Recipe   */
                        /*------------------------------*/
                        anEventRecord.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
                        anEventRecord.setLogicalRecipeID(ObjectIdentifier.fetchValue(lotInCassette.getStartRecipe().getLogicalRecipeID()));
                        anEventRecord.setMachineRecipeID(ObjectIdentifier.fetchValue(lotInCassette.getStartRecipe().getMachineRecipeID()));
                        anEventRecord.setPhysicalRecipeID(lotInCassette.getStartRecipe().getPhysicalRecipeID());

                        log.info("anEventRecord.equipmentID: {}", anEventRecord.getEquipmentID());
                        log.info("anEventRecord.logicalRecipeID: {}", anEventRecord.getLogicalRecipeID());
                        log.info("anEventRecord.machineRecipeID: {}", anEventRecord.getMachineRecipeID());
                        log.info("anEventRecord.physicalRecipeID: {}", anEventRecord.getPhysicalRecipeID());

                        /*------------------------------------*/
                        /*   Set Monitor Group Related Info   */
                        /*------------------------------------*/
                        CimMonitorGroup aMonGrp = aLot.getControlMonitorGroup();
                        List<Event.ProcessedLotEventData> processedLots = new ArrayList<>();
                        anEventRecord.setProcessedLots(processedLots);
                        if (null == aMonGrp) {
                            log.info("aMonGrp is null");
                            anEventRecord.setMonitorGroupID("");
                        } else {
                            anEventRecord.setMonitorGroupID(aMonGrp.getIdentifier());
                            List<ProductDTO.MonitoredLot> mLotSeq = aMonGrp.allLots();

                            /*-----------------------------------*/
                            /*   Set for Monitor-Grouping Lots   */
                            /*-----------------------------------*/
                            log.info("Set for Monitor-Grouping Lots");
                            if (CimArrayUtils.isNotEmpty(mLotSeq)) {
                                for (ProductDTO.MonitoredLot monitoredLot : mLotSeq) {
                                    Event.ProcessedLotEventData processedLotEventData = new Event.ProcessedLotEventData();
                                    processedLots.add(processedLotEventData);
                                    processedLotEventData.setProcessLotID(ObjectIdentifier.fetchValue(monitoredLot.getLotID()));

                                    CimProcessOperation mPO = baseCoreFactory.getBO(CimProcessOperation.class, monitoredLot.getProcessOperation());
                                    if (null != mPO) {
                                        log.info("mPO is not null");
                                        ProcessDTO.PosProcessOperationEventData monitorGRPPOEventData = mPO.getEventData();

                                        processedLotEventData.setProcessRouteID(monitorGRPPOEventData.getRouteID());
                                        processedLotEventData.setProcessOperationNumber(monitorGRPPOEventData.getOperationNumber());
                                        processedLotEventData.setProcessOperationPassCount(CimNumberUtils.longValue(monitorGRPPOEventData.getOperationPassCount()));
                                        processedLotEventData.setProcessObjrefPO(monitorGRPPOEventData.getObjrefPO());
                                    } else {
                                        processedLotEventData.setProcessRouteID("");
                                        processedLotEventData.setProcessOperationNumber("");
                                        processedLotEventData.setProcessOperationPassCount(0L);
                                        processedLotEventData.setProcessObjrefPO("");
                                    }

                                    log.info("processedLotEventData.processRouteID: {}", processedLotEventData.getProcessRouteID());
                                    log.info("processedLotEventData.processOperationNumber: {}", processedLotEventData.getProcessOperationNumber());
                                    log.info("processedLotEventData.processOperationPassCount: {}", processedLotEventData.getProcessOperationPassCount());
                                    log.info("processedLotEventData.processObjrefPO: {}", processedLotEventData.getProcessObjrefPO());
                                }
                            }
                        }
                        CimProcessFlowContext aPFX = aLot.getProcessFlowContext();
                        Validations.check(null == aPFX, retCodeConfig.getNotFoundPfx(), "");

                        CimProcessDefinition aMainPD = aPrePO.getMainProcessDefinition();
                        Validations.check(null == aMainPD, retCodeConfig.getNotFoundProcessDefinition(), "");

                        ObjectIdentifier mainPDID = ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey());
                        log.info("mainPDID: {}", ObjectIdentifier.fetchValue(mainPDID));

                        int corresOpeLenforFPC = 0;
                        String singleCorrOpeFPC = null;
                        List<String> dummyFPCIDs = new ArrayList<>();
                        ObjectIdentifier dummyID = null;
                        String dummy = null;

                        Inputs.ObjFPCInfoGetDRIn objFPCInfoGetDRIn = new Inputs.ObjFPCInfoGetDRIn();
                        objFPCInfoGetDRIn.setLotID(lotInCassette.getLotID());
                        objFPCInfoGetDRIn.setLotFamilyID(dummyID);
                        objFPCInfoGetDRIn.setMainPDID(mainPDID);
                        objFPCInfoGetDRIn.setMainOperNo(anEventRecord.getMeasuredLotData().getOperationNumber());
                        objFPCInfoGetDRIn.setOrgMainPDID(dummyID);
                        objFPCInfoGetDRIn.setOrgOperNo(dummy);
                        objFPCInfoGetDRIn.setSubMainPDID(dummyID);
                        objFPCInfoGetDRIn.setSubOperNo(dummy);
                        objFPCInfoGetDRIn.setFPCIDs(dummyFPCIDs);
                        objFPCInfoGetDRIn.setEquipmentID(equipmentID);
                        objFPCInfoGetDRIn.setWaferIDInfoGetFlag(true);
                        objFPCInfoGetDRIn.setRecipeParmInfoGetFlag(false);
                        objFPCInfoGetDRIn.setReticleInfoGetFlag(false);
                        objFPCInfoGetDRIn.setDcSpecItemInfoGetFlag(true);
                        List<Infos.FPCInfo> fpcInfoGetDROut = fpcMethod.fpcInfoGetDR(objCommon, objFPCInfoGetDRIn);

                        Boolean foundFPCFlag = false;
                        Infos.FPCInfo foundFPCInfo = new Infos.FPCInfo();
                        if (CimArrayUtils.isNotEmpty(fpcInfoGetDROut)) {
                            for (Infos.FPCInfo fpcInfo : fpcInfoGetDROut) {
                                log.info("wafer key for FPC {}", ObjectIdentifier.fetchValue(fpcInfo.getLotWaferInfoList().get(0).getWaferID()));
                                if (CimArrayUtils.isNotEmpty(lotInCassette.getLotWaferList())) {
                                    for (Infos.LotWafer lotWafer : lotInCassette.getLotWaferList()) {
                                        if (ObjectIdentifier.equalsWithValue(fpcInfo.getLotWaferInfoList().get(0).getWaferID(), lotWafer.getWaferID())) {
                                            foundFPCInfo = fpcInfo;
                                            foundFPCFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if (CimBooleanUtils.isTrue(foundFPCFlag)) {
                                    break;
                                }
                            }
                        }
                        singleCorrOpeFPC = foundFPCInfo.getCorrespondOperationNumber();
                        corresOpeLenforFPC = CimArrayUtils.getSize(foundFPCInfo.getCorrespondingOperationInfoList());
                        log.info("correspondingOperationInfo->length() from FPC {}", corresOpeLenforFPC);

                        Long envMultiCorrOpe = StandardProperties.OM_EDC_MULTI_CORRESPOND_FLAG.getLongValue();
                        if (corresOpeLenforFPC > 0 || CimStringUtils.isNotEmpty(singleCorrOpeFPC)) {
                            log.info("Previous PO has Corresponding Operation in FPC.");
                            if (0 < envMultiCorrOpe) {
                                Event.ProcessedLotEventData processedLotEventData = new Event.ProcessedLotEventData();
                                processedLots.add(processedLotEventData);

                                if (CimArrayUtils.isNotEmpty(foundFPCInfo.getCorrespondingOperationInfoList())) {
                                    for (Infos.CorrespondingOperationInfo correspondingOperationInfo : foundFPCInfo.getCorrespondingOperationInfoList()) {
                                        CimProcessOperation aFPCMultiCorrPO = aPFX.findProcessOperationForOperationNumberBefore(correspondingOperationInfo.getCorrespondingOperationNumber());
                                        Validations.check(null == aFPCMultiCorrPO, retCodeConfig.getNotFoundProcessOperation(),
                                                ObjectIdentifier.fetchValue(lotInCassette.getLotID()));

                                        ProcessDTO.PosProcessOperationEventData multiFPCcorrespondingPOEventData = aFPCMultiCorrPO.getEventData();
                                        processedLotEventData.setProcessLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                                        processedLotEventData.setProcessRouteID(multiFPCcorrespondingPOEventData.getRouteID());
                                        processedLotEventData.setProcessOperationNumber(multiFPCcorrespondingPOEventData.getOperationNumber());
                                        processedLotEventData.setProcessOperationPassCount(CimNumberUtils.longValue(multiFPCcorrespondingPOEventData.getOperationPassCount()));
                                        processedLotEventData.setProcessObjrefPO(multiFPCcorrespondingPOEventData.getObjrefPO());

                                        log.info("processedLotEventData.processLotID: {}", processedLotEventData.getProcessLotID());
                                        log.info("processedLotEventData.processRouteID: {}", processedLotEventData.getProcessRouteID());
                                        log.info("processedLotEventData.processOperationNumber: {}", processedLotEventData.getProcessOperationNumber());
                                        log.info("processedLotEventData.processOperationPassCount: {}", processedLotEventData.getProcessOperationPassCount());
                                        log.info("processedLotEventData.processObjrefPO: {}", processedLotEventData.getProcessObjrefPO());
                                    }
                                }
                            } else {
                                //Single Corresponding Operation in FPC
                                Event.ProcessedLotEventData processedLotEventData = new Event.ProcessedLotEventData();
                                processedLots.add(processedLotEventData);

                                CimProcessOperation aFPCSingleCorrPO = aPFX.findProcessOperationForOperationNumberBefore(singleCorrOpeFPC);
                                Validations.check(null == aFPCSingleCorrPO, retCodeConfig.getNotFoundProcessOperation(), ObjectIdentifier.fetchValue(lotInCassette.getLotID()));

                                ProcessDTO.PosProcessOperationEventData singleFPCcorrespondingPOEventData = aFPCSingleCorrPO.getEventData();

                                processedLotEventData.setProcessLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                                processedLotEventData.setProcessRouteID(singleFPCcorrespondingPOEventData.getRouteID());
                                processedLotEventData.setProcessOperationNumber(singleFPCcorrespondingPOEventData.getOperationNumber());
                                processedLotEventData.setProcessOperationPassCount(CimNumberUtils.longValue(singleFPCcorrespondingPOEventData.getOperationPassCount()));
                                processedLotEventData.setProcessObjrefPO(singleFPCcorrespondingPOEventData.getObjrefPO());

                                log.info("processedLotEventData.processLotID: {}", processedLotEventData.getProcessLotID());
                                log.info("processedLotEventData.processRouteID: {}", processedLotEventData.getProcessRouteID());
                                log.info("processedLotEventData.processOperationNumber: {}", processedLotEventData.getProcessOperationNumber());
                                log.info("processedLotEventData.processOperationPassCount: {}", processedLotEventData.getProcessOperationPassCount());
                                log.info("processedLotEventData.processObjrefPO: {}", processedLotEventData.getProcessObjrefPO());
                            }
                        } else {
                            List<CimProcessOperation> aCorresOpeList = aPFX.getCorrespondingProcessOperationsFor(aPrePO);
                            if (CimArrayUtils.isNotEmpty(aCorresOpeList)) {
                                log.info("Previous PO has Corresponding Operation.");
                                Event.ProcessedLotEventData processedLotEventData = new Event.ProcessedLotEventData();
                                processedLots.add(processedLotEventData);

                                for (CimProcessOperation cimProcessOperation : aCorresOpeList) {
                                    ProcessDTO.PosProcessOperationEventData correspondingPOEventData = cimProcessOperation.getEventData();

                                    processedLotEventData.setProcessLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                                    processedLotEventData.setProcessRouteID(correspondingPOEventData.getRouteID());
                                    processedLotEventData.setProcessOperationNumber(correspondingPOEventData.getOperationNumber());
                                    processedLotEventData.setProcessOperationPassCount(CimNumberUtils.longValue(correspondingPOEventData.getOperationPassCount()));
                                    processedLotEventData.setProcessObjrefPO(correspondingPOEventData.getObjrefPO());

                                    log.info("processedLotEventData.processLotID: {}", processedLotEventData.getProcessLotID());
                                    log.info("processedLotEventData.processRouteID: {}", processedLotEventData.getProcessRouteID());
                                    log.info("processedLotEventData.processOperationNumber: {}", processedLotEventData.getProcessOperationNumber());
                                    log.info("processedLotEventData.processOperationPassCount: {}", processedLotEventData.getProcessOperationPassCount());
                                    log.info("processedLotEventData.processObjrefPO: {}", processedLotEventData.getProcessObjrefPO());
                                }
                            } else {
                                Event.ProcessedLotEventData processedLotEventData = new Event.ProcessedLotEventData();
                                processedLots.add(processedLotEventData);
                                processedLotEventData.setProcessLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                                processedLotEventData.setProcessRouteID(anEventRecord.getMeasuredLotData().getRouteID());
                                processedLotEventData.setProcessOperationNumber(anEventRecord.getMeasuredLotData().getOperationNumber());
                                processedLotEventData.setProcessOperationPassCount(anEventRecord.getMeasuredLotData().getOperationPassCount());
                                processedLotEventData.setProcessObjrefPO(anEventRecord.getMeasuredLotData().getObjrefPO());

                                log.info("processedLotEventData.processLotID: {}", processedLotEventData.getProcessLotID());
                                log.info("processedLotEventData.processRouteID: {}", processedLotEventData.getProcessRouteID());
                                log.info("processedLotEventData.processOperationNumber: {}", processedLotEventData.getProcessOperationNumber());
                                log.info("processedLotEventData.processOperationPassCount: {}", processedLotEventData.getProcessOperationPassCount());
                                log.info("processedLotEventData.processObjrefPO: {}", anEventRecord.getMeasuredLotData().getObjrefPO());
                            }
                        }

                        /*---------------------------*/
                        /*   Set Event Common Info   */
                        /*---------------------------*/
                        anEventRecord.setEventCommon(new Event.EventData());
                        anEventRecord.getEventCommon().setTransactionID(transactionID);
                        anEventRecord.getEventCommon().setEventTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                        anEventRecord.getEventCommon().setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
                        anEventRecord.getEventCommon().setUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
                        anEventRecord.getEventCommon().setEventMemo(claimMemo);

                        log.info("anEventRecord.eventCommon.transactionID: {}", anEventRecord.getEventCommon().getTransactionID());
                        log.info("anEventRecord.eventCommon.eventTimeStamp: {}", anEventRecord.getEventCommon().getEventTimeStamp());
                        log.info("anEventRecord.eventCommon.eventShopDate: {}", anEventRecord.getEventCommon().getEventShopDate());
                        log.info("anEventRecord.eventCommon.userID: {}", anEventRecord.getEventCommon().getUserID());
                        log.info("anEventRecord.eventCommon.eventMemo: {}", anEventRecord.getEventCommon().getEventMemo());

                        eventManager.createEvent(anEventRecord, CimCollectedDataEvent.class);
                    }
                }
            }
        }
    }

    @Override
    public void layoutRecipeEventMake(Infos.ObjCommon objCommon, LayoutRecipeParams.LayoutRecipeEventParams layoutRecipeEventParams) {
        log.info("layoutRecipeEventMake()->info: params : {}", layoutRecipeEventParams.toString());
        Event.LayoutRecipeEventRecord layoutRecipeEventRecord = new Event.LayoutRecipeEventRecord();
        layoutRecipeEventRecord.setOperationMemo(layoutRecipeEventParams.getClaimMemo());
        BeanUtils.copyProperties(layoutRecipeEventParams, layoutRecipeEventRecord);
        layoutRecipeEventRecord.setEventCommon(setEventData(objCommon, layoutRecipeEventParams.getClaimMemo()));
        eventManager.createEvent(layoutRecipeEventRecord, CimLayoutRecipeEvent.class);
    }

    private Event.EventData setEventData(Infos.ObjCommon objCommon, String claimMemo) {
        Event.EventData eventData = new Event.EventData();
        eventData.setTransactionID(objCommon.getTransactionID());
        eventData.setEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        eventData.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
        eventData.setUserID(objCommon.getUser().getUserID().getValue());
        eventData.setEventMemo(claimMemo);
        eventData.setEventCreationTimeStamp(CimDateUtils.getCurrentDateTimeWithDefault());
        return eventData;
    }

    private Event.EventData setEventData(Infos.ObjCommon objCommon, String claimMemo, String transactionID) {
        Event.EventData eventData = new Event.EventData();
        if (CimStringUtils.isNotEmpty(transactionID)
                && !CimStringUtils.equals(TransactionIDEnum.FORCE__OPE_COMP_REQ.getValue(),
                objCommon.getTransactionID())) {
            eventData.setTransactionID(transactionID);
        } else {
            eventData.setTransactionID(objCommon.getTransactionID());
        }
        eventData.setEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        eventData.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
        eventData.setUserID(objCommon.getUser().getUserID().getValue());
        eventData.setEventMemo(claimMemo);
        eventData.setEventCreationTimeStamp(CimDateUtils.getCurrentDateTimeWithDefault());
        return eventData;
    }


    @Override
    public void sorterSorterJobEventMakeNew(Infos.ObjCommon objCommon, com.fa.cim.sorter.Params.ObjSorterSorterJobEventMakeIn objSorterSorterJobEventMakeIn) {
        if (log.isDebugEnabled()) {
            log.debug("check input action code");
        }
        Validations.check(!SorterType.Action.equalsActionCode(objSorterSorterJobEventMakeIn.getActionCode()),
                retCodeConfig.getInvalidActionCode(), objSorterSorterJobEventMakeIn.getActionCode());

        com.fa.cim.sorter.Params.SJCreateReqParams sortJobAttribute = objSorterSorterJobEventMakeIn.getStrSortJobListAttributes();
        if (log.isDebugEnabled()) {
            log.debug("Set to Event Record");
        }
        SortEvent.WaferSortJobEventRecord anEvent = new SortEvent.WaferSortJobEventRecord();
        anEvent.setOperation(objSorterSorterJobEventMakeIn.getStrSortJobListAttributes().getOperation());
        anEvent.setEquipmentID(ObjectIdentifier.fetchValue(sortJobAttribute.getEquipmentID()));
        anEvent.setPortGroupID(sortJobAttribute.getPortGroupID());
        anEvent.setSorterJobID(ObjectIdentifier.fetchValue(sortJobAttribute.getSorterJobID()));
        anEvent.setSorterJobStatus(sortJobAttribute.getSorterJobStatus());
        anEvent.setWaferIDReadFlag(sortJobAttribute.getWaferIDReadFlag());
        anEvent.setComponentJobCount(sortJobAttribute.getComponentCount());
        anEvent.setCtrlJobID(ObjectIdentifier.fetchValue(sortJobAttribute.getControlJobID()));

        if (log.isDebugEnabled()) {
            log.debug("set event common");
        }
        Event.EventData eventData = new Event.EventData();
        anEvent.setEventCommon(eventData);
        eventData.setTransactionID(objSorterSorterJobEventMakeIn.getTransactionID());
        eventData.setEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        eventData.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
        eventData.setUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        eventData.setEventMemo(objSorterSorterJobEventMakeIn.getClaimMemo());

        if (log.isDebugEnabled()) {
            log.debug("set postAct");
        }
        Info.SortJobPostAct postAct = sortJobAttribute.getPostAct();
        if (postAct != null) {
            SortEvent.WaferSortJobPostActRecord postActRecord = new SortEvent.WaferSortJobPostActRecord();
            anEvent.setPostActRecord(postActRecord);
            postActRecord.setSorterJobID(ObjectIdentifier.fetchValue(sortJobAttribute.getSorterJobID()));
            postActRecord.setActionCode(postAct.getActionCode());
            postActRecord.setProductOrderID(postAct.getProductOrderId());
            postActRecord.setVendorID(postAct.getVendorId());
            postActRecord.setWaferCount(postAct.getWaferCount());
            postActRecord.setSourceProductID(postAct.getSourceProductId());
            postActRecord.setChildLotId(postAct.getChildLotId());
            postActRecord.setParentLotId(postAct.getParentLotId());
        }

        if (log.isDebugEnabled()) {
            log.debug("Set Component Job Information");
        }
        List<Info.ComponentJob> compJobAttributes = objSorterSorterJobEventMakeIn
                .getStrSortJobListAttributes().getStrSorterComponentJobListAttributesSequence();
        List<SortEvent.SortJobComponentEventData> compEvents = new ArrayList<>();
        anEvent.setComponentJobs(compEvents);
        if (CimArrayUtils.isNotEmpty(compJobAttributes)) {
            for (Info.ComponentJob componentJob : compJobAttributes) {
                SortEvent.SortJobComponentEventData compEvent = new SortEvent.SortJobComponentEventData();
                compEvents.add(compEvent);
                compEvent.setComponentJobID(componentJob.getComponentJobID());
                compEvent.setDestinationCarrierID(ObjectIdentifier.fetchValue(componentJob.getDestinationCassetteID()));
                compEvent.setDestinationPortID(ObjectIdentifier.fetchValue(componentJob.getDestinationPortID()));
                compEvent.setSourceCarrierID(ObjectIdentifier.fetchValue(componentJob.getOriginalCassetteID()));
                compEvent.setSourcePortID(ObjectIdentifier.fetchValue(componentJob.getOriginalPortID()));
                compEvent.setComponentJobStatus(componentJob.getComponentJobStatus());
                compEvent.setActionCode(componentJob.getActionCode());
                compEvent.setOperation(componentJob.getOperation());
                String replyTimeStamp = componentJob.getReplyTimeStamp();
                if (log.isDebugEnabled()) {
                    log.debug("Set ComponentJob Slot Map");
                }
                List<Info.WaferSorterSlotMap> slotMapAttributes = componentJob.getWaferList();
                List<SortEvent.SortJobSlotMapEventData> slotMapEvents = new ArrayList<>();
                compEvent.setSlotMaps(slotMapEvents);
                if (CimArrayUtils.isNotEmpty(slotMapAttributes)) {
                    for (Info.WaferSorterSlotMap slotMapAttribute : slotMapAttributes) {
                        SortEvent.SortJobSlotMapEventData slotMapEvent = new SortEvent.SortJobSlotMapEventData();
                        slotMapEvents.add(slotMapEvent);
                        slotMapEvent.setComponentJobID(componentJob.getComponentJobID());
                        slotMapEvent.setLotID(ObjectIdentifier.fetchValue(slotMapAttribute.getLotID()));
                        slotMapEvent.setWaferID(ObjectIdentifier.fetchValue(slotMapAttribute.getWaferID()));
                        slotMapEvent.setDestinationPosition(slotMapAttribute.getDestinationSlotNumber());
                        slotMapEvent.setSourcePosition(slotMapAttribute.getOriginalSlotNumber());
                        slotMapEvent.setAliasName(slotMapAttribute.getAliasName());
                        slotMapEvent.setDirection(slotMapAttribute.getDirection());
                        slotMapEvent.setSortStatus(slotMapAttribute.getStatus());
                        slotMapEvent.setReplyTimestamp(replyTimeStamp == null ? null :
                                Timestamp.valueOf(replyTimeStamp));
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Create Wafer Sorter Event");
        }
        eventManager.createEvent(anEvent, CimWaferSortJobEvent.class);
    }


    @Override
    public void lotMonitorGroupEventMake(Infos.ObjCommon objCommon,
                                         LotMonitorGroupParams.LotMonitorGroupEventParams lotMonitorGroupEventParams) {
        log.info("lotMonitorGroupEventMake()->info: params : {}", lotMonitorGroupEventParams.toString());
        Event.LotMonitorGroupEventRecord lotMonitorGroupEventRecord = new Event.LotMonitorGroupEventRecord();
        BeanUtils.copyProperties(lotMonitorGroupEventParams, lotMonitorGroupEventRecord);
        lotMonitorGroupEventRecord.setEventCommon(setEventData(objCommon, ""));
        eventManager.createEvent(lotMonitorGroupEventRecord, CimLotMonitorGroupEvent.class);
    }

}
