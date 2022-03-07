package com.fa.cim.service.newSorter.Impl;

import cn.hutool.core.util.StrUtil;
import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
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
import com.fa.cim.dto.Results;
import com.fa.cim.method.*;
import com.fa.cim.method.impl.CassetteMethod;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.sorter.CimSorterJob;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.remote.IEAPRemoteManager;
import com.fa.cim.service.bank.IBankInqService;
import com.fa.cim.service.bank.IBankService;
import com.fa.cim.service.dispatch.IDispatchService;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.equipment.IEquipmentInqService;
import com.fa.cim.service.equipment.IEquipmentProcessOperation;
import com.fa.cim.service.equipment.IEquipmentService;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.lot.ILotProcessOperationService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.lotstart.ILotStartService;
import com.fa.cim.service.newSorter.ISortNewInqService;
import com.fa.cim.service.newSorter.ISortNewService;
import com.fa.cim.service.system.ISystemInqService;
import com.fa.cim.service.tms.ITransferManagementSystemService;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.Params;
import com.fa.cim.sorter.SorterHandler;
import com.fa.cim.sorter.SorterType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:58
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class SortNewServiceImpl implements ISortNewService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ISorterNewMethod sorterNewMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private ITransferManagementSystemService transferManagementSystemService;

    @Autowired
    private IBankInqService bankInqService;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private ILotStartService lotStartService;

    @Autowired
    private IDurableService durableService;

    @Autowired
    private ILotInqService lotInqService;

    @Autowired
    private ILotService lotService;

    @Autowired
    private ISystemInqService systemInqService;

    @Autowired
    private IEAPMethod eapMethod;

    @Autowired
    private ISortNewInqService sortInqService;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IContaminationMethod contaminationMethod;

    @Autowired
    private IEquipmentInqService equipmentInqService;

    @Autowired
    private IEquipmentService equipmentService;

    @Autowired
    private IEquipmentProcessOperation equipmentProcessOperation;

    @Autowired
    private ILotProcessOperationService processOperationService;

    @Autowired
    private IDispatchService dispatchService;

    @Autowired
    private IBankService bankService;

    private final static String rotate = "Rotate";

    @Override
    public void sxWaferSorterActionRegisterReq(Infos.ObjCommon objCommon, com.fa.cim.sorter.Params.WaferSorterActionRegisterReqParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        List<Info.SortActionAttributes> strWaferSorterActionListSequence = params.getStrWaferSorterActionListSequence();

        if (log.isDebugEnabled()) {
            log.debug("Check Transaction ID and equipment Category combination.");
        }
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        int nListLen = CimArrayUtils.getSize(strWaferSorterActionListSequence);
        for (int i = 0; i < nListLen; i++) {
            if (log.isDebugEnabled()) {
                log.debug("Check Action Code exist");
            }
            String actionCode = strWaferSorterActionListSequence.get(i).getActionCode();
            Validations.check(CimStringUtils.unEqual(SorterType.Action.T7CodeRead.getValue(), actionCode)
                            && CimStringUtils.unEqual(SorterType.Action.WaferStart.getValue(), actionCode)
                            && CimStringUtils.unEqual(SorterType.Action.LotTransfer.getValue(), actionCode)
                            && CimStringUtils.unEqual(SorterType.Action.Reset.getValue(), actionCode)
                            && CimStringUtils.unEqual(SorterType.Action.Flip.getValue(), actionCode)
                            && CimStringUtils.unEqual(SorterType.Action.WaferSlotMapAdjust.getValue(), actionCode)
                            && !StrUtil.contains(SorterType.Action.Rotate_.getValue(), actionCode),
                    new OmCode(retCodeConfig.getInvalidActionCode(), strWaferSorterActionListSequence.get(i).getActionCode()));
            if (log.isDebugEnabled()) {
                log.debug("Check Equipment ID ( Comp First Record and other record )");
            }
            Validations.check(!ObjectIdentifier.equalsWithValue(strWaferSorterActionListSequence.get(i).getEquipmentID(), equipmentID),
                    new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(strWaferSorterActionListSequence.get(i).getEquipmentID())));
        }
        if (log.isDebugEnabled()) {
            log.debug("Insert to DB");
        }
        sorterNewMethod.waferSorterActionListInsertDR(objCommon, strWaferSorterActionListSequence, equipmentID);
    }



    @Override
    public void sortActionStart(Infos.ObjCommon objCommon, Params.SJCreateReqParams params) {

        //------------------------------------------------------------------
        //  Object Lock
        //------------------------------------------------------------------
        Outputs.ObjLockModeOut strObjectLockModeGetOut = null;

       // int sorterJobLockFlag = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getIntValue();
        int lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE.intValue();
        if (StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()) {
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(params.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);
            strObjectLockModeGetOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = strObjectLockModeGetOut.getLockMode().intValue();
        }
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn strAdvancedobjectLockIn = new Inputs.ObjAdvanceLockIn();
            strAdvancedobjectLockIn.setObjectID(params.getEquipmentID());
            strAdvancedobjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjectLockIn.setLockType(strObjectLockModeGetOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjectLockIn);
        } else {
            //--------------------------------------------------------
            //  Object Lock for Equipment
            //--------------------------------------------------------
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }

        //-------------------------------------------------------------------
        // Call equipment_portInfo_Get
        //-------------------------------------------------------------------
        Infos.EqpPortInfo strEquipmentPortInfoGetOut = equipmentMethod.equipmentPortInfoGet(objCommon, params.getEquipmentID());
        int portLen = CimArrayUtils.getSize(strEquipmentPortInfoGetOut.getEqpPortStatuses());
        String operationMode = "";
        if (portLen > 0) {
            for (Infos.EqpPortStatus eqpPortStatus : strEquipmentPortInfoGetOut.getEqpPortStatuses()) {
                if (log.isDebugEnabled()) {
                    log.debug("portGroup : {}  ", eqpPortStatus.getPortGroup());
                }
                if (CimStringUtils.equals(eqpPortStatus.getPortGroup(), params.getPortGroupID())) {
                    objectLockMethod.objectLockForEquipmentResource(objCommon, params.getEquipmentID(),
                            eqpPortStatus.getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                }
            }
            for (Infos.EqpPortStatus eqpPortStatus : strEquipmentPortInfoGetOut.getEqpPortStatuses()) {
                if (CimStringUtils.equals(eqpPortStatus.getPortGroup(), params.getPortGroupID())) {
                    if (eqpPortStatus.getOperationMode().equals(SorterType.OperationMode.Semi_1.getValue())) {
                        operationMode = eqpPortStatus.getOperationMode();
                        break;
                    }
                }
            }
        }

        Info.ComponentJob componentJob = params.getStrSorterComponentJobListAttributesSequence().get(0);
        String actionCode = componentJob.getActionCode();
        //--------------------------------------------------------
        //  check actionCode
        //--------------------------------------------------------

        Validations.check(StringUtils.isEmpty(operationMode)
                        || !operationMode.equals(SorterType.OperationMode.Semi_1.getValue()),
                retCodeConfig.getNotFoundMachineOperationMode());
        Validations.check(!SorterType.Action.RFIDRead.getValue().equals(actionCode)
                        && !SorterType.Action.RFIDWrite.getValue().equals(actionCode)
                        && !SorterType.Action.T7CodeRead.getValue().equals(actionCode), retCodeConfig.getInvalidActionCode());

        //--------------------------------------------------------
        //  Object Lock for Cassette
        //--------------------------------------------------------
        Set<ObjectIdentifier> sortCassetteIDs = new HashSet<>();
        //RFIDRead carrier没有数据，默认为FOSB+6位数
        ObjectIdentifier source = ObjectIdentifier.buildWithValue(SorterType.CarrierType.FOSB.getValue() + SorterHandler.generateSixDigits());
        ObjectIdentifier target = ObjectIdentifier.buildWithValue(SorterType.CarrierType.FOSB.getValue() + SorterHandler.generateSixDigits());
        if (SorterType.Action.RFIDRead.getValue().equals(actionCode)
                && ObjectIdentifier.isEmpty(componentJob.getOriginalCassetteID())
                && ObjectIdentifier.isEmpty(componentJob.getDestinationCassetteID())) {
            componentJob.setOriginalCassetteID(source);
            componentJob.setDestinationCassetteID(target);
        }
        sortCassetteIDs.add(componentJob.getOriginalCassetteID());
        sortCassetteIDs.add(componentJob.getDestinationCassetteID());
        for (ObjectIdentifier carrierID : sortCassetteIDs) {
            if(SorterHandler.containsFOSB(carrierID)){
                continue;
            }
            objectLockMethod.objectLock(objCommon, CimCassette.class, carrierID);
        }


        //--------------------------------------------------------
        //  Object Lock for Lot
        //--------------------------------------------------------
        Set<ObjectIdentifier> sortLotIDs = new HashSet<>();
        if (CimArrayUtils.isNotEmpty(componentJob.getWaferList())) {
            for (Info.WaferSorterSlotMap waferSorterSlotMap : componentJob.getWaferList()) {
                sortLotIDs.add(waferSorterSlotMap.getLotID());
            }
        }
        if (sortLotIDs.size() > 0) {
            for (ObjectIdentifier sortLotID : sortLotIDs) {
                objectLockMethod.objectLock(objCommon, CimLot.class, sortLotID);
            }
        }

        this.sxSJCreateReq(objCommon, params);
        Params.SorterActionInqParams inqParams = new Params.SorterActionInqParams();
        inqParams.setUser(params.getUser());
        inqParams.setEquipmentID(params.getEquipmentID());
        inqParams.setCassetteID(source);
        Info.SortJobInfo sortJobInfo = sortInqService.sorterActionInq(objCommon, inqParams, SorterType.Status.Created.getValue());
        Object result = this.sendEap(objCommon, sortJobInfo, SorterType.OperationMode.Semi_1.getValue());
        //通知EAP
        if (null != result) {
            //更新状态
            this.sxAssembleStatusChg(objCommon, ObjectIdentifier.buildWithValue(componentJob.getComponentJobID()),
                    SorterType.JobType.ComponentJob.getValue(), SorterType.Status.Executing.getValue(), false);
        }
    }

    @Override
    public void sxSortJobPriorityChangeReq(Infos.ObjCommon objCommon, com.fa.cim.sorter.Params.SortJobPriorityChangeReqParam param) {
        List<ObjectIdentifier> jobIDs = param.getJobIDs();
        if (log.isDebugEnabled()) {
            log.debug("check input params");
        }
        if (0 == CimArrayUtils.getSize(jobIDs) || CimStringUtils.isEmpty(param.getJobType())) {
            Validations.check(retCodeConfig.getInvalidParameter());
        }
        Validations.check(!CimStringUtils.equals(param.getJobType(), SorterType.JobType.SorterJob.getValue())
                        && !CimStringUtils.equals(param.getJobType(), SorterType.JobType.ComponentJob.getValue()),
                new OmCode(retCodeConfigEx.getInvalidSorterJobType(), param.getJobType()));

        if (log.isDebugEnabled()) {
            log.debug("Check jobID duplication");
        }
        long count = jobIDs.stream().map(ObjectIdentifier::fetchValue).distinct().count();
        Validations.check(CimArrayUtils.getSize(jobIDs) != count, retCodeConfig.getInvalidParameter());
        int jobLen = CimArrayUtils.getSize(jobIDs);
        if (StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()) {
            if (log.isDebugEnabled()) {
                log.debug("Lock Sort Jobs");
            }
            //jobType=ComponentJob,jobIDs存储的是等待排序的ComponentJobId,而且是一个sj下的
            if (CimStringUtils.equals(param.getJobType(), SorterType.JobType.ComponentJob.getValue())) {
                Info.ObjSorterComponentJobInfoGetByComponentJobIDDROut out = sorterNewMethod
                        .sorterComponentJobInfoGetByComponentJobIDDR(objCommon, jobIDs.get(0));
                objectLockMethod.objectLock(objCommon, CimSorterJob.class, out.getSorterJobID());
            } else if (CimStringUtils.equals(param.getJobType(), SorterType.JobType.SorterJob.getValue())) {
                for (ObjectIdentifier jobID : jobIDs) {
                    objectLockMethod.objectLock(objCommon, CimSorterJob.class, jobID);
                }
            }
        }

        Outputs.ObjLockModeOut strObjectLockModeGetOut = null;
        ObjectIdentifier tmpEqpID = null;
        if (CimStringUtils.equals(param.getJobType(), SorterType.JobType.SorterJob.getValue())) {
            if (log.isDebugEnabled()) {
                log.debug("Get Sort job information");
            }
            for (int i = 0; i < jobLen; i++) {
                boolean isSucess = false;
                com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
                objSorterJobListGetDRIn.setSorterJob(jobIDs.get(i));
                List<Info.SortJobListAttributes> sortJobListAttributes = sorterNewMethod.sorterJobListGetDR(objCommon,
                        objSorterJobListGetDRIn);
                Validations.check(CimArrayUtils.isEmpty(sortJobListAttributes),
                        new OmCode(retCodeConfigEx.getNotFoundSorterjob(), ObjectIdentifier.fetchValue(jobIDs.get(i))));

                if (0 == i) {
                    Long lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE;
                    if (StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()) {
                        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
                        objLockModeIn.setObjectID(sortJobListAttributes.get(0).getEquipmentID());
                        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                        objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
                        objLockModeIn.setUserDataUpdateFlag(false);
                        strObjectLockModeGetOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
                        lockMode = strObjectLockModeGetOut.getLockMode();
                    }
                    if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
                        Inputs.ObjAdvanceLockIn strAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
                        strAdvancedObjectLockIn.setObjectID(sortJobListAttributes.get(0).getEquipmentID());
                        strAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                        strAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
                        strAdvancedObjectLockIn.setLockType(strObjectLockModeGetOut.getRequiredLockForMainObject());
                        log.info("calling advanced_object_Lock() : {} ", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
                        objectLockMethod.advancedObjectLock(objCommon, strAdvancedObjectLockIn);

                    } else {
                        objectLockMethod.objectLock(objCommon, CimMachine.class, sortJobListAttributes.get(0).getEquipmentID());
                    }
                } else {
                    Validations.check(!ObjectIdentifier.equalsWithValue(tmpEqpID, sortJobListAttributes.get(0).getEquipmentID()),
                            new OmCode(retCodeConfigEx.getCannotPriorityChange(), SorterType.JobType.SorterJob.getValue()));
                }
                tmpEqpID = sortJobListAttributes.get(0).getEquipmentID();

                if (log.isDebugEnabled()) {
                    log.debug("Sort Job Status is WaitToExecuting after the second");
                }
                if (CimStringUtils.equals(SorterType.Status.Created.getValue(), sortJobListAttributes.get(0).getSorterJobStatus())) {
                    isSucess = true;
                }
                if (!isSucess) {
                    Validations.check(new OmCode(retCodeConfigEx.getCannotPriorityChange(),
                            SorterType.JobType.SorterJob.getValue()));
                }
            }
        } else if (CimStringUtils.equals(param.getJobType(), SorterType.JobType.ComponentJob.getValue())) {
            if (log.isDebugEnabled()) {
                log.debug("check component job belongs to sort job");
            }
            ObjectIdentifier tempSortJob = null;
            for (ObjectIdentifier jobID : jobIDs) {
                boolean isSuccess = false;
                Info.ObjSorterComponentJobInfoGetByComponentJobIDDROut componentJobInfo = sorterNewMethod
                        .sorterComponentJobInfoGetByComponentJobIDDR(objCommon, jobID);

                if (tempSortJob != null && !ObjectIdentifier.equalsWithValue(componentJobInfo.getSorterJobID(), tempSortJob)) {
                    Validations.check(new OmCode(retCodeConfigEx.getCannotPriorityChange(),
                            SorterType.JobType.ComponentJob.getValue()));
                }
                tempSortJob = componentJobInfo.getSorterJobID();

                //验证下一个SortJob是否一致
                if (ObjectIdentifier.isEmpty(tempSortJob)) {
                    Validations.check(new OmCode(retCodeConfigEx.getNotFoundSorterjob(), ObjectIdentifier.fetchValue(tempSortJob)));
                }

                if (CimStringUtils.equals(componentJobInfo.getComponentJob().getComponentJobStatus()
                        , SorterType.Status.Created.getValue())) {
                    isSuccess = true;
                }
                if (!isSuccess ) {
                    Validations.check(new OmCode(retCodeConfigEx.getCannotPriorityChange(),
                            SorterType.JobType.ComponentJob.getValue()));
                }
            }
            ObjectIdentifier equipmentID = sorterNewMethod.getEquipmentIDBySortJobID(objCommon, tempSortJob);
            if (log.isDebugEnabled()) {
                log.debug("add lock");
            }
            long lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE;
            if (StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()) {
                Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
                objLockModeIn.setObjectID(equipmentID);
                objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objLockModeIn.setFunctionCategory(ThreadContextHolder.getTransactionId());
                objLockModeIn.setUserDataUpdateFlag(false);
                strObjectLockModeGetOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
                lockMode = strObjectLockModeGetOut.getLockMode();
            }
            if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
                Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
                objAdvancedObjectLockIn.setObjectID(equipmentID);
                objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
                objAdvancedObjectLockIn.setLockType(strObjectLockModeGetOut.getRequiredLockForMainObject());
                objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);
            } else {
                objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Priority Change");
        }
        Info.SorterLinkedJobUpdateDRIn sorterLinkedJobUpdateDRIn = new Info.SorterLinkedJobUpdateDRIn();
        sorterLinkedJobUpdateDRIn.setJobIDs(jobIDs);
        sorterLinkedJobUpdateDRIn.setJobType(param.getJobType());
        sorterNewMethod.sorterLinkedJobUpdateDR(objCommon, sorterLinkedJobUpdateDRIn);
    }

    public void sxAssembleStatusChg(Infos.ObjCommon objCommon, ObjectIdentifier componentJob, String jobType,
                                    String jobStatus, boolean resEAPFlag) {
        com.fa.cim.sorter.Params.SJStatusChgRptParams sjStatusChgRptParams = new com.fa.cim.sorter.Params.SJStatusChgRptParams();
        sjStatusChgRptParams.setJobID(componentJob);
        sjStatusChgRptParams.setJobType(jobType);
        sjStatusChgRptParams.setJobStatus(jobStatus);
        this.sxSJStatusChgRpt(objCommon, sjStatusChgRptParams, resEAPFlag);
    }

    public void sxAssembleEvent(Infos.ObjCommon objCommon, Info.SortJobListAttributes strSortJobListAttributes,
                                String claimMemo, boolean isPartComp, List<ObjectIdentifier> sorterComponentJobIDseq,
                                String sjOperation, String cjOperation, String jobStatus, boolean resEAPFlag) {
        List<Info.SorterComponentJobListAttributes> componentJobs = strSortJobListAttributes.getSorterComponentJobListAttributesList();

        Params.ObjSorterSorterJobEventMakeIn eventMakeIn = new Params.ObjSorterSorterJobEventMakeIn();
        if (CimArrayUtils.isNotEmpty(componentJobs)) {
            eventMakeIn.setActionCode(componentJobs.get(0).getActionCode());
        }
        eventMakeIn.setTransactionID(objCommon.getTransactionID());

        //assemble sort job
        Params.SJCreateReqParams sortJobEvent = new Params.SJCreateReqParams();
        eventMakeIn.setStrSortJobListAttributes(sortJobEvent);
        sortJobEvent.setEquipmentID(strSortJobListAttributes.getEquipmentID());
        sortJobEvent.setControlJobID(ObjectIdentifier.buildWithValue(strSortJobListAttributes.getCtrljobId()));
        sortJobEvent.setPortGroupID(ObjectIdentifier.fetchValue(strSortJobListAttributes.getPortGroupID()));
        sortJobEvent.setWaferIDReadFlag(strSortJobListAttributes.isWaferIDReadFlag());
        sortJobEvent.setClaimMemo(claimMemo);
        sortJobEvent.setSorterJobID(strSortJobListAttributes.getSorterJobID());
        sortJobEvent.setSorterJobStatus(CimStringUtils.isNotEmpty(jobStatus) ? jobStatus : strSortJobListAttributes.getSorterJobStatus());
        sortJobEvent.setComponentCount(strSortJobListAttributes.getComponentCount());
        sortJobEvent.setOperation(sjOperation);

        List<Info.ComponentJob> componentEvents = new ArrayList<>();
        sortJobEvent.setStrSorterComponentJobListAttributesSequence(componentEvents);
        //assemble component job
        if (CimArrayUtils.isNotEmpty(componentJobs)) {
            for (Info.SorterComponentJobListAttributes componentJob : componentJobs) {
                //当删除部分component job时，只添加部分component job的事件信息
                if (isPartComp && CimArrayUtils.isNotEmpty(sorterComponentJobIDseq)) {
                    List<String> compJobIDs = sorterComponentJobIDseq.stream()
                            .map(e -> ObjectIdentifier.fetchValue(e)).collect(Collectors.toList());
                    if (!compJobIDs.contains(ObjectIdentifier.fetchValue(componentJob.getSorterComponentJobID()))) {
                        continue;
                    }
                }

                Info.ComponentJob componentEvent = new Info.ComponentJob();
                componentEvents.add(componentEvent);
                componentEvent.setComponentJobID(ObjectIdentifier.fetchValue(componentJob.getSorterComponentJobID()));
                componentEvent.setComponentJobStatus(CimStringUtils.isNotEmpty(jobStatus) ? jobStatus : componentJob.getComponentSorterJobStatus());
                componentEvent.setOriginalCassetteID(componentJob.getOriginalCarrierID());
                componentEvent.setDestinationCassetteID(componentJob.getDestinationCarrierID());
                componentEvent.setDestinationPortID(componentJob.getDestinationPortID());
                componentEvent.setOriginalPortID(componentJob.getOriginalPortID());
                if (resEAPFlag) {
                    componentEvent.setReplyTimeStamp(componentJob.getReplyTimeStamp());
                }
                componentEvent.setRequestTimeStamp(componentJob.getRequestTimeStamp());
                componentEvent.setActionCode(componentJob.getActionCode());
                componentEvent.setOperation(cjOperation);

                //assemble slotmap
                List<Info.WaferSorterSlotMap> slotMapEvents = componentJob.getWaferSorterSlotMapList();
                Optional.ofNullable(slotMapEvents).orElseGet(Collections::emptyList).stream().forEach(data -> {
                    data.setDirection(resEAPFlag ? SorterType.JobDirection.EAP.getValue() : SorterType.JobDirection.OMS.getValue());
                });
                componentEvent.setWaferList(slotMapEvents);
            }
        }

        //assemble postAct
        if (CimStringUtils.equals(SorterType.Action.WaferStart.getValue(),componentJobs.get(0).getActionCode())
                || CimStringUtils.equals(SorterType.Action.Combine.getValue(),componentJobs.get(0).getActionCode())
                || CimStringUtils.equals(SorterType.Action.Separate.getValue(),componentJobs.get(0).getActionCode())) {
            sortJobEvent.setPostAct(strSortJobListAttributes.getPostAct());
        }
        eventMethod.sorterSorterJobEventMakeNew(objCommon, eventMakeIn);
    }

    @Override
    public String sxSJCreateByMoveInReserve(Infos.ObjCommon objCommon, com.fa.cim.dto.Params
            .MoveInReserveReqParams moveInReserveReqParams, ObjectIdentifier controlJobID) {
        if (log.isDebugEnabled()) {
            log.debug("check input params");
        }
        List<Infos.StartCassette> startCassetteList = moveInReserveReqParams.getStartCassetteList();
        Validations.check(CimArrayUtils.isEmpty(startCassetteList), retCodeConfig.getNotFoundCassette());

        if (log.isDebugEnabled()) {
            log.debug("assembly sorter job");
        }
        Params.SJCreateReqParams sjCreateReqParams = new Params.SJCreateReqParams();
        sjCreateReqParams.setUser(objCommon.getUser());
        sjCreateReqParams.setEquipmentID(moveInReserveReqParams.getEquipmentID());
        sjCreateReqParams.setControlJobID(controlJobID);
        sjCreateReqParams.setPortGroupID(moveInReserveReqParams.getPortGroupID());

        List<Info.ComponentJob> componentJobList = new ArrayList<>();
        sjCreateReqParams.setStrSorterComponentJobListAttributesSequence(componentJobList);
        for (Infos.StartCassette startCassette : startCassetteList) {
            String loadPurposeType = startCassette.getLoadPurposeType();

            if (log.isDebugEnabled()) {
                log.debug("source carrier parsing");
            }
            if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT, loadPurposeType)
                    || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT, loadPurposeType)
                    || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_OTHER, loadPurposeType)) {

                List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                Validations.check(CimArrayUtils.isEmpty(lotInCassetteList), retCodeConfig.getNotFoundLot());

                List<Info.WaferSorterSlotMap> slotMaps = new ArrayList<>();
                String actionCode = null;
                String eqActionCode = null;
                for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                    //当moveInFlag为true的时，才对这些lot创建sorter
                    if (CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {
                        if (log.isDebugEnabled()) {
                            log.debug("parsing actionCode");
                        }
                        ObjectIdentifier machineRecipeID = lotInCassette.getStartRecipe().getMachineRecipeID();
                        String recipeID = ObjectIdentifier.fetchValue(machineRecipeID).replaceAll("\\.", "_");
                        String[] split = StrUtil.split(recipeID, "_");
                        Validations.check(split.length <= 1, retCodeConfig.getNotFoundLogicRecipe());
                        if (split[1].contains(rotate)) {
                            String orientation = StrUtil.subAfter(split[1], rotate, true);
                            int orientationNum = CimNumberUtils.intValue(orientation);
                            Validations.check(orientationNum <= 0 || orientationNum >= 360, retCodeConfigEx.getNotValidAct());
                            actionCode = rotate + "_" + orientation;
                            eqActionCode = rotate + "_";
                        } else {
                            actionCode = split[1];
                            eqActionCode = split[1];
                        }

                        contaminationMethod.lotSorterCJCreateCheck(lotInCassette.getLotID(), moveInReserveReqParams.getEquipmentID(),actionCode);

                        if (log.isDebugEnabled()) {
                            log.debug("assembly slotMap");
                        }
                        List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
                        if (CimArrayUtils.isNotEmpty(lotWaferList)) {
                            for (Infos.LotWafer lotWafer : lotWaferList) {
                                Info.WaferSorterSlotMap slotMap = new Info.WaferSorterSlotMap();
                                slotMap.setOriginalSlotNumber(CimNumberUtils.intValue(lotWafer.getSlotNumber()));
                                slotMap.setDestinationSlotNumber(CimNumberUtils.intValue(lotWafer.getSlotNumber()));
                                slotMap.setLotID(lotInCassette.getLotID());
                                slotMap.setWaferID(lotWafer.getWaferID());
                                slotMap.setAliasName(lotWafer.getAliasName());
                                slotMap.setDirection(SorterType.JobDirection.OMS.getValue());
                                slotMaps.add(slotMap);
                            }
                        }
                    }
                }

                //目前只考虑：单Carrier操作时，不能添加空Carrier；多Carrier时只能添加一个空carrier的情况
                List<Infos.StartCassette> emptyCarriers = startCassetteList.stream().filter(carrier ->
                        CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, carrier.getLoadPurposeType()))
                        .collect(Collectors.toList());
                Info.ComponentJob componentJob = new Info.ComponentJob();
                if (CimStringUtils.equals(SorterType.Action.Rotate_.getValue(), eqActionCode)
                        || CimStringUtils.equals(SorterType.Action.Flip.getValue(), eqActionCode)) {
                    componentJob.setDestinationCassetteID(startCassette.getCassetteID());
                    componentJob.setDestinationPortID(startCassette.getLoadPortID());
                    Validations.check(CimArrayUtils.getSize(emptyCarriers) != 0, retCodeConfig.getNotFoundCassette());
                } else if (CimStringUtils.equals(SorterType.Action.LotTransfer.getValue(), eqActionCode)
                        || CimStringUtils.equals(SorterType.Action.WaferEnd.getValue(), eqActionCode)) {
                    Validations.check(CimArrayUtils.getSize(emptyCarriers) != 1, retCodeConfig.getNotFoundCassette());
                    Infos.StartCassette emptyCarrier = emptyCarriers.get(0);
                    componentJob.setDestinationCassetteID(emptyCarrier.getCassetteID());
                    componentJob.setDestinationPortID(emptyCarrier.getLoadPortID());
                } else {
                    Validations.check(retCodeConfigEx.getNotValidAct());
                }
                componentJob.setOriginalCassetteID(startCassette.getCassetteID());
                componentJob.setOriginalPortID(startCassette.getLoadPortID());
                componentJob.setActionCode(actionCode);
                componentJob.setWaferList(slotMaps);
                componentJobList.add(componentJob);
            }
        }
        if(sjCreateReqParams.getStrSorterComponentJobListAttributesSequence()
                .get(0).getActionCode().equals(SorterType.Action.Flip.getValue())){
            //on-route Flip--> WaferIDReadFlag默认为false
            sjCreateReqParams.setWaferIDReadFlag(false);
        }else {
            sjCreateReqParams.setWaferIDReadFlag(moveInReserveReqParams.isWaferIDReadFlag());
        }

        return this.sxSJCreateReq(objCommon, sjCreateReqParams);
    }

    @Override
    public void sxSJStatusChgRpt(Infos.ObjCommon objCommon, com.fa.cim.sorter.Params.SJStatusChgRptParams params, boolean resEAPFlag) {
        //init
        ObjectIdentifier jobID = params.getJobID();
        String jobStatus = params.getJobStatus();
        String jobType = params.getJobType();
        String claimMemo = params.getOpeMemo();

        if (log.isDebugEnabled()) {
            log.debug("Check Input parameter");
        }
        Validations.check(ObjectIdentifier.isEmpty(jobID), retCodeConfig.getInvalidParameter());

        if (log.isDebugEnabled()) {
            log.debug("Check JogType contents");
        }
        Validations.check(!CimStringUtils.equals(SorterType.JobType.SorterJob.getValue(), jobType)
                        && !CimStringUtils.equals(SorterType.JobType.ComponentJob.getValue(), jobType),
                retCodeConfig.getInvalidParameter());

        if (log.isDebugEnabled()) {
            log.debug("Lock Sort Jobs");
        }
        int sorterJobLockFlag = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getIntValue();
        if (1 == sorterJobLockFlag) {
            ObjectIdentifier sorterJobID = new ObjectIdentifier();
            if (CimStringUtils.equals(SorterType.JobType.SorterJob.getValue(), jobType)) {
                sorterJobID = jobID;
            } else {
                sorterJobID = sorterNewMethod.sorterComponentJobInfoGetByComponentJobIDDR(objCommon, jobID).getSorterJobID();
            }
            objectLockMethod.objectLock(objCommon,CimSorterJob.class,sorterJobID);
        }

        List<Info.SortJobListAttributes> sorterJobListGetDROut = null;
        //---------------------------------------------------
        //
        // Sort Job Status
        //
        //---------------------------------------------------
        if (CimStringUtils.equals(SorterType.JobType.SorterJob.getValue(), jobType)) {

            if (log.isDebugEnabled()) {
                log.debug("Change Sort Job Status:{},{}", jobType, jobStatus);
            }
            //--------------------------------------------------
            // Check sortJob status
            //       Error
            //       Aborted
            //       ForceCompleted
            //--------------------------------------------------
            Validations.check(!CimStringUtils.equals(SorterType.Status.Error.getValue(), jobStatus)
                            && !CimStringUtils.equals(SorterType.Status.Aborted.getValue(), jobStatus)
                            && !CimStringUtils.equals(SorterType.Status.ForceCompleted.getValue(), jobStatus),
                    retCodeConfigEx.getInvalidSorterJobStatus(), jobID, jobStatus);

            if (log.isDebugEnabled()) {
                log.debug("Get Sort job information");
            }
            com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn sorterJobListGetDRIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
            sorterJobListGetDRIn.setSorterJob(jobID);
            sorterJobListGetDROut = sorterNewMethod.sorterJobListGetDR(objCommon,
                    sorterJobListGetDRIn);
            Validations.check(CimArrayUtils.isEmpty(sorterJobListGetDROut),
                    new OmCode(retCodeConfigEx.getNotFoundSorterjob(), ObjectIdentifier.fetchValue(sorterJobListGetDRIn.getSorterJob())));
            //-------------------------------------------------
            // Current Sort Job Status is Created
            //     -> Executing   OK      //不需要，在修改componentJob状态为Executing时修改sortJob
            //     -> Error       OK
            // The OTHER status changes are NG.
            //-------------------------------------------------
            Info.SortJobListAttributes sortJobListAttributes = sorterJobListGetDROut.get(0);
            String dbSorterJobStatus = sortJobListAttributes.getSorterJobStatus();
            if (log.isDebugEnabled()) {
                log.debug("Current Sort job status..{}", dbSorterJobStatus);
            }
            if (CimStringUtils.equals(SorterType.Status.Created.getValue(), dbSorterJobStatus)) {
                Validations.check(!CimStringUtils.equals(SorterType.Status.Error.getValue(), jobStatus),
                        retCodeConfigEx.getCannotSortJobStatusChange(), dbSorterJobStatus, jobStatus);
            }
            //-----------------------------------------
            // Current Sort Job Status is Executing
            // Executing
            //     -> Completed       OK    //不需要，在修改componentJob状态为Completed时修改sortJob
            //     -> Error           OK    //不需要，在修改componentJob状态为Error时修改sortJob
            //     -> Aborted         OK
            //     -> ForceCompleted  OK
            // The OTHER status changes are NG.
            //-----------------------------------------
            else if (CimStringUtils.equals(SorterType.Status.Executing.getValue(), dbSorterJobStatus)) {
                Validations.check(!CimStringUtils.equals(SorterType.Status.Aborted.getValue(), jobStatus)
                                && !CimStringUtils.equals(SorterType.Status.ForceCompleted.getValue(), jobStatus),
                        retCodeConfigEx.getCannotSortJobStatusChange(), dbSorterJobStatus, jobStatus);
            } else {
                //Sort Job status is Other：The OTHER status changes are NG.
                Validations.check(retCodeConfigEx.getCannotSortJobStatusChange(), dbSorterJobStatus, jobStatus);
            }

            if (log.isDebugEnabled()) {
                log.debug("Change Sort Job Status");
            }
            Info.SorterComponentJobType sortType = new Info.SorterComponentJobType();
            sortType.setSortJobID(jobID);
            sortType.setJobType(SorterType.JobType.SorterJob.getValue());
            sortType.setJobStatus(jobStatus);
            sorterNewMethod.sorterAndCompnentJobStatusUpdateDR(objCommon, sortType);

            if (log.isDebugEnabled()) {
                log.debug("Change Component Job Status");
            }
            if (CimArrayUtils.isNotEmpty(sortJobListAttributes.getSorterComponentJobListAttributesList())) {
                for (Info.SorterComponentJobListAttributes sjAttribute : sortJobListAttributes.getSorterComponentJobListAttributesList()) {
                    Info.SorterComponentJobType componentJobType = new Info.SorterComponentJobType();
                    componentJobType.setSortJobID(jobID);
                    componentJobType.setComponentJobID(sjAttribute.getSorterComponentJobID());
                    componentJobType.setJobType(SorterType.JobType.ComponentJob.getValue());
                    componentJobType.setJobStatus(jobStatus);
                    sorterNewMethod.sorterAndCompnentJobStatusUpdateDR(objCommon, componentJobType);
                }
            }
        }
        //---------------------------------------------------
        //
        // Component Job Status
        //
        //---------------------------------------------------
        else if (CimStringUtils.equals(BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB, jobType)) {
            //--------------------------------------------------------
            // Check Inpara Sorter Component Job
            //       Created
            //       Executing
            //       Completed
            //       Error
            //--------------------------------------------------------
            Validations.check(!CimStringUtils.equals(SorterType.Status.Created.getValue(), jobStatus)
                            && !CimStringUtils.equals(SorterType.Status.Executing.getValue(), jobStatus)
                            && !CimStringUtils.equals(SorterType.Status.Completed.getValue(), jobStatus)
                            && !CimStringUtils.equals(SorterType.Status.Error.getValue(), jobStatus),
                    retCodeConfigEx.getInvalidSorterComponentJobStatus(), jobID, jobStatus);

            if (log.isDebugEnabled()) {
                log.debug("Get Sort Component Job");
            }
            Info.ObjSorterComponentJobInfoGetByComponentJobIDDROut componentJobIDDR = sorterNewMethod.sorterComponentJobInfoGetByComponentJobIDDR(objCommon,jobID);

            if (log.isDebugEnabled()) {
                log.debug("Get Sort job information");
            }
            com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn sorterJobListGetDRIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
            sorterJobListGetDRIn.setSorterJob(componentJobIDDR.getSorterJobID());
            sorterJobListGetDROut = sorterNewMethod.sorterJobListGetDR(objCommon, sorterJobListGetDRIn);
            Validations.check(CimArrayUtils.isEmpty(sorterJobListGetDROut), retCodeConfigEx.getNotFoundSorterjob(), sorterJobListGetDRIn.getSorterJob().getValue());

            Info.SortJobListAttributes sortJobListAttributes = sorterJobListGetDROut.get(0);
            Info.SorterComponentJobListAttributes componentJob = Optional.ofNullable(sortJobListAttributes.getSorterComponentJobListAttributesList()).
                    orElseGet(Collections::emptyList)
                    .stream()
                    .filter(comp -> ObjectIdentifier.equalsWithValue(jobID, comp.getSorterComponentJobID()))
                    .findFirst()
                    .get();
            String componentJobStatus = componentJob.getComponentSorterJobStatus();
            //-----------------------------------------
            // Current Sort Component Job Status is Create
            // Create
            //     -> Executing       OK
            //     -> Error           OK
            // The OTHER status changes are NG.
            //-----------------------------------------
            if (CimStringUtils.equals(SorterType.Status.Created.getValue(), componentJobStatus)) {
                Validations.check(!CimStringUtils.equals(SorterType.Status.Executing.getValue(), jobStatus)
                                && !CimStringUtils.equals(SorterType.Status.Error.getValue(), jobStatus),
                        retCodeConfigEx.getCannotSortComponentJobStatusChange(), componentJobStatus, jobStatus);
            }
            //-----------------------------------------
            // Current Sort Component Job Status is Executing
            // Executing
            //     -> Completed       OK
            //     -> Error       OK
            // The OTHER status changes are NG.
            //-----------------------------------------
            else if (CimStringUtils.equals(SorterType.Status.Executing.getValue(), componentJobStatus)) {
                Validations.check(!CimStringUtils.equals(SorterType.Status.Completed.getValue(), jobStatus)
                                && !CimStringUtils.equals(SorterType.Status.Error.getValue(), jobStatus)
                                && !CimStringUtils.equals(SorterType.Status.Aborted.getValue(), jobStatus)
                                && !CimStringUtils.equals(SorterType.Status.ForceCompleted.getValue(), jobStatus),
                        retCodeConfigEx.getCannotSortComponentJobStatusChange(), componentJobStatus, jobStatus);
            } else {
                //Sort Component Job status is Other：The OTHER status changes are NG.
                Validations.check(retCodeConfigEx.getCannotSortComponentJobStatusChange(), componentJobStatus, jobStatus);
            }

            if (log.isDebugEnabled()) {
                log.debug("get need update status Sort Job");
            }
            String chgSortJobStatus = null;
            if (CimStringUtils.equals(SorterType.Status.Error.getValue(), jobStatus)) {
                chgSortJobStatus = SorterType.Status.Error.getValue();
            }
            if (CimStringUtils.equals(SorterType.Status.Completed.getValue(), jobStatus)) {
                long count = Optional.ofNullable(sortJobListAttributes.getSorterComponentJobListAttributesList()).orElseGet(Collections::emptyList)
                        .stream()
                        .map(e -> e.getComponentSorterJobStatus())
                        .filter(e -> !CimStringUtils.equals(SorterType.Status.Completed.getValue(), e))
                        .count();
                if (count == 1) {
                    chgSortJobStatus = SorterType.Status.Completed.getValue();
                }
            }

            if (CimStringUtils.equals(SorterType.Status.Executing.getValue(), jobStatus)
                    && CimStringUtils.equals(SorterType.Status.Created.getValue(), sortJobListAttributes.getSorterJobStatus())) {
                chgSortJobStatus = SorterType.Status.Executing.getValue();
            }


            if (log.isDebugEnabled()) {
                log.debug("udpate Component Job");
            }
            Info.SorterComponentJobType sorterComponentJobType = new Info.SorterComponentJobType();
            sorterComponentJobType.setSortJobID(componentJobIDDR.getSorterJobID());
            sorterComponentJobType.setComponentJobID(jobID);
            sorterComponentJobType.setJobType(SorterType.JobType.ComponentJob.getValue());
            sorterComponentJobType.setJobStatus(jobStatus);
            sorterNewMethod.sorterAndCompnentJobStatusUpdateDR(objCommon, sorterComponentJobType);

            if (log.isDebugEnabled()) {
                log.debug("update Sort Job");
            }
            if (CimStringUtils.isNotEmpty(chgSortJobStatus)) {
                sorterComponentJobType.setJobType(SorterType.JobType.SorterJob.getValue());
                sorterComponentJobType.setJobStatus(jobStatus);
                sorterNewMethod.sorterAndCompnentJobStatusUpdateDR(objCommon, sorterComponentJobType);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("add event");
        }
        Info.SortJobListAttributes sortJobListAttributes = sorterJobListGetDROut.get(0);
        boolean isPartComp = CimStringUtils.equals(jobType, SorterType.JobType.ComponentJob.getValue());
        List<ObjectIdentifier> sorterComponentJobIDseq = new ArrayList<>();
        if (isPartComp) {
            sorterComponentJobIDseq.add(jobID);
        }
        String sjOperation = null;
        String cjOperation = null;
        if (CimStringUtils.equals(SorterType.Status.Executing.getValue(), jobStatus)) {
            sjOperation = SorterType.JobOperation.SortJobStart.getValue();
            cjOperation = SorterType.JobOperation.ComponentJobStart.getValue();
        } else if (CimStringUtils.equals(SorterType.Status.Completed.getValue(), jobStatus)) {
            sjOperation = SorterType.JobOperation.SortJobComp.getValue();
            cjOperation = SorterType.JobOperation.ComponentJobComp.getValue();
        } else if (CimStringUtils.equals(SorterType.Status.Error.getValue(), jobStatus)) {
            sjOperation = SorterType.JobOperation.SortJobError.getValue();
            cjOperation = SorterType.JobOperation.ComponentJobError.getValue();
        } else if (CimStringUtils.equals(SorterType.Status.Canceled.getValue(), jobStatus)) {
            sjOperation = SorterType.JobOperation.SortJobCanceled.getValue();
            cjOperation = SorterType.JobOperation.ComponentJobCanceled.getValue();
        } else if (CimStringUtils.equals(SorterType.Status.Aborted.getValue(), jobStatus)) {
            sjOperation = SorterType.JobOperation.SortJobAborted.getValue();
            cjOperation = SorterType.JobOperation.ComponentJobAborted.getValue();
        } else if (CimStringUtils.equals(SorterType.Status.ForceCompleted.getValue(), jobStatus)) {
            sjOperation = SorterType.JobOperation.SortJobForceCompleted.getValue();
            cjOperation = SorterType.JobOperation.ComponentJobForceCompleted.getValue();
        }

        this.sxAssembleEvent(objCommon, sortJobListAttributes, claimMemo, isPartComp, sorterComponentJobIDseq,
                sjOperation, cjOperation, jobStatus, resEAPFlag);
    }

    @Override
    public void sxSorterActionReq(Infos.ObjCommon objCommon, Info.SortJobInfo params) {
        //-----------------------------------------
        // collect SiView managed casettes only.
        //-----------------------------------------
        Info.ComponentJob componentJob = params.getComponentJob();
        Validations.check(null == componentJob, retCodeConfig.getInvalidParameter());
        Validations.check(ObjectIdentifier.isEmpty(params.getEquipmentID())
                        || CimObjectUtils.isEmpty(componentJob) || ObjectIdentifier.isEmpty(params.getSorterJobID()),
                retCodeConfig.getInvalidParameter());

        //----------------------------------------------------
        // 验证EAP此时加工的componentJobID是不是优先级最高的哪一个?
        //-----------------------------------------------------
        com.fa.cim.sorter.Params.SorterActionInqParams inqParams = new com.fa.cim.sorter.Params.SorterActionInqParams();
        inqParams.setEquipmentID(params.getEquipmentID());
        inqParams.setCassetteID(params.getComponentJob().getOriginalCassetteID());
        Info.SortJobInfo sortJobInfo = sortInqService.sorterActionInq(objCommon, inqParams, SorterType.Status.Created.getValue());
        this.checkProcessingInformation(params, sortJobInfo);

        //-----------------------------------------------------
        // Carrier 去重
        //-----------------------------------------------------
        List<ObjectIdentifier> cassetteIDs = SorterHandler.getduplicateRemovalCarrierIDs(componentJob);

        int sorterJobLockFlag = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getIntValue();
        if (log.isDebugEnabled()) {
            log.debug("sorterJobLockFlag : {}", sorterJobLockFlag);
        }
        int lockMode = 0;

        Outputs.ObjLockModeOut objLockModeOut = new Outputs.ObjLockModeOut();

        //1 == sorterJobLockFlag替换成
        // StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()当environmentVariableValue=1为TRUE
        if (StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()) {
            if (log.isDebugEnabled()) {
                log.debug("sorterJobLockFlag = 1");
            }
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(params.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);

            objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode().intValue();
        }

        //--------------------------------------------------------------
        //  Check inpurt parameters length
        //--------------------------------------------------------------

        if (StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()) {
            //---------------------------------
            // Lock Sort Jobs
            //---------------------------------
            objectLockMethod.objectLock(objCommon,CimSorterJob.class,params.getSorterJobID());

        }
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

            int cassetteIDsLen = CimArrayUtils.getSize(cassetteIDs);
            if (0 < cassetteIDsLen) {
                // Lock Equipment LoadCassette Element (Read)
                List<String> loadCastSeq = new ArrayList<>();
                for (int loadCastNo = 0; loadCastNo < cassetteIDsLen; loadCastNo++) {
                    loadCastSeq.add(ObjectIdentifier.fetchValue(cassetteIDs.get(loadCastNo)));
                }

                Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
                objAdvanceLockIn.setObjectID(params.getEquipmentID());
                objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                objAdvanceLockIn.setLockType(CimNumberUtils.longValue(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ));
                objAdvanceLockIn.setKeyList(loadCastSeq);
                objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
            }
        } else {
            /*---------------------------------------------------*/
            /*   Lock Macihne object for check condition         */
            /*---------------------------------------------------*/
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }

        //----------------------------------------------
        // Lock cassette objects for check condition.
        //----------------------------------------------
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);


        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //   Check Process
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        if (log.isDebugEnabled()) {
            log.debug("Check Transaction ID and equipment Category combination.");
        }

        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, params.getEquipmentID());

        //---------------------------------------
        // Check lot InterFabXfer State
        //---------------------------------------
        for (ObjectIdentifier cassetteID : cassetteIDs) {
            /*-------------------------------*/
            /*  Get Lot list in carrier      */
            /*-------------------------------*/
            Infos.LotListInCassetteInfo cassetteLotListOut = cassetteMethod.cassetteLotIDListGetDR(objCommon, cassetteID);

            int lotLen = CimArrayUtils.getSize(cassetteLotListOut.getLotIDList());
            if (lotLen > 0) {
                for (ObjectIdentifier cassetteInfo : cassetteLotListOut.getLotIDList()) {
                    String strLotInterFabXferStateGetOut = lotMethod.lotInterFabXferStateGet(objCommon, cassetteInfo);
                    if (CimStringUtils.equals(strLotInterFabXferStateGetOut, BizConstant.SP_INTERFAB_XFERSTATE_ORIGINDELETING)) {
                        Validations.check(retCodeConfigEx.getInterfabInvalidXferstateDeleting());
                    }
                }
            }
        }

        //====================================================================
        // Checking Cassette Status ,DestinationCassetteID=FOSB 不check
        //====================================================================
        String actionCode = params.getComponentJob().getActionCode();
        if (!SorterType.Action.equalsActionCode(actionCode)) {
            Validations.check(retCodeConfig.getInvalidActionCode(), actionCode);
        }

        if (!SorterHandler.containsFOSB(componentJob.getDestinationCassetteID())) {
            Outputs.ObjCassetteStatusOut cassetteStatusDR;
            try {
                cassetteStatusDR = cassetteMethod.cassetteGetStatusDR(objCommon, componentJob.getDestinationCassetteID());
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {
                }
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage());
                }
                throw e;
            }
            if (log.isDebugEnabled()) {
                log.debug("strCassette_getStatusDR_out.drbl_state---> : {}", cassetteStatusDR.getDurableState());
            }
            Validations.check(CimStringUtils.equals(cassetteStatusDR.getDurableState(), CIMStateConst.CIM_DURABLE_NOTAVAILABLE),
                    retCodeConfigEx.getCassetteNotAvailable());
        }

        //======================================================
        //   Check WaferSorter Operation Data Readiness
        //======================================================
        sorterNewMethod.waferSorterCheckConditionForAction(objCommon, params);

        //======================================================
        //  On-Route Sorter 结合 move in
        //======================================================
        if (!ObjectIdentifier.isEmpty(params.getControlJobID())
                && sortJobInfo.getSorterJobStatus().equals(SorterType.Status.Created.getValue())) {
            com.fa.cim.dto.Params.LotsMoveInInfoInqParams inInfoInqParams = new com.fa.cim.dto.Params.LotsMoveInInfoInqParams();
            inInfoInqParams.setUser(objCommon.getUser());
            inInfoInqParams.setEquipmentID(params.getEquipmentID());
            inInfoInqParams.setCassetteIDs(cassetteIDs);
            Results.LotsMoveInInfoInqResult infoInqResult = equipmentInqService.sxLotsMoveInInfoInq(objCommon, inInfoInqParams);
            List<Infos.StartCassette> startCassetteList = infoInqResult.getStartCassetteList();
            //2716 【污染管理】on route Lot transfer sorter，预约生成sorter job后，删除OSCONTMNTSORT表中的carrier category关系数据，仍然能sorter成功
            contaminationMethod.contaminationSorterCheckForQiandao(params.getComponentJob(),null,
                    params.getEquipmentID());
            equipmentService.sxMoveInReq(objCommon, params.getEquipmentID(), params.getPortGroup(),
                    params.getControlJobID(), startCassetteList,
                    false,
                    null,
                    null,
                    params.getOpeMemo());
        }

        //----------------------------------------------------------------------
        //  Sorter component Job Status Update (Xfer -> Executing)
        //----------------------------------------------------------------------
        log.info("actionCode == SP_Sorter_AutoSorting ");

        this.sxAssembleStatusChg(objCommon, ObjectIdentifier.buildWithValue(componentJob.getComponentJobID()),
                SorterType.JobType.ComponentJob.getValue(), SorterType.Status.Executing.getValue(), false);
    }

    private Infos.WaferTransfer getWaferTransfer(Info.ComponentJob componentJob, Info.WaferSorterSlotMap waferSorterSlotMap) {
        Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
        waferTransfer.setWaferID(waferSorterSlotMap.getWaferID());
        waferTransfer.setBDestinationCassetteManagedByOM(true);
        waferTransfer.setBOriginalCassetteManagedByOM(true);
        waferTransfer.setDestinationCassetteID(componentJob.getDestinationCassetteID());
        waferTransfer.setDestinationSlotNumber(waferSorterSlotMap.getDestinationSlotNumber());
        waferTransfer.setOriginalCassetteID(componentJob.getOriginalCassetteID());
        waferTransfer.setOriginalSlotNumber(waferSorterSlotMap.getOriginalSlotNumber());
        return waferTransfer;
    }

    @Override
    public List<ObjectIdentifier> sxSorterActionRpt(Infos.ObjCommon objCommon, Info.SortJobInfo params) {
        OmCode rcParamChk = new OmCode();
        //----------------------------------------
        //  check EAP报告的componentJobID是不是优先级最高的
        //----------------------------------------
        Validations.check(ObjectIdentifier.isEmpty(params.getEquipmentID()),retCodeConfigEx.getEqpIdParamNull());
        Validations.check(ObjectIdentifier.isEmpty(params.getSorterJobID()),retCodeConfigEx.getNotFoundSorterjob());
        Validations.check(StringUtils.isEmpty(params.getComponentJob().getComponentJobID()),
                retCodeConfigEx.getNotFoundSorterjobComponent());
        //---------------------------------------------------------------------------------
        // Collect participant cassettes (original/destination) from WaferSorterSlotMapSeq.
        //---------------------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Collect participant cassettes (original/destination) from WaferSorterSlotMapSeq.");
        }
        Info.ComponentJob tcsComponentJob = params.getComponentJob();
        List<ObjectIdentifier> cassetteIDs = SorterHandler.getduplicateRemovalCarrierIDs(tcsComponentJob);
        //1 == sorterJobLockFlag替换成
        // StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()当environmentVariableValue=1为TRUE
        if (StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()) {
            //---------------------------------
            // Lock Sort Jobs
            //---------------------------------
            objectLockMethod.objectLock(objCommon, CimSorterJob.class, params.getSorterJobID());

        }

        Outputs.ObjLockModeOut objLockModeOut = new Outputs.ObjLockModeOut();
        int lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE.intValue();
        if (StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()) {
            // Get required equipment lock mode
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(params.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);
            objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode().intValue();
        }
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

            // Lock Equipment LoadCassette Element (Write)
            List<String> loadCastSeq = new ArrayList<>();
            cassetteIDs.forEach(id -> {
                loadCastSeq.add(ObjectIdentifier.fetchValue(id));
            });
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(params.getEquipmentID());
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
            //0L是WRITE锁,1L是READ锁
            objAdvanceLockIn.setLockType(0L);
            objAdvanceLockIn.setKeyList(loadCastSeq);
            objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
        } else {
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }

        //---------------------------------------
        // Lock cassette objects for update.
        //---------------------------------------
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

        com.fa.cim.sorter.Params.SorterActionInqParams inqParams = new com.fa.cim.sorter.Params.SorterActionInqParams();
        inqParams.setEquipmentID(params.getEquipmentID());
        inqParams.setCassetteID(params.getComponentJob().getOriginalCassetteID());
        this.checkProcessingInformation(params, sortInqService
                .sorterActionInq(objCommon, inqParams, SorterType.Status.Executing.getValue()));
        //----------------------------------------
        //  equipmentCategoryVsTxID
        //----------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("equipment Category and Tx Check EquipmentID:{}", ObjectIdentifier.fetchValue(params.getEquipmentID()));
        }
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, params.getEquipmentID());
        //-------------------------------------------------------------------------
        //    Check INPUT PARAMETER: TCS Reply Sequence Length Check
        //-------------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Check input Wafer sLotMap");
        }
        List<Info.WaferSorterSlotMap> waferList = params.getComponentJob().getWaferList();
        String actionCode = params.getComponentJob().getActionCode();
        if (log.isTraceEnabled()) {
            log.trace("actionCode,{}",actionCode);
        }
        //-------------------------------------------------------------------------
        //    Check INPUT PARAMETER: Action Code exist And Same Data And Sorter Status
        //-------------------------------------------------------------------------
        if (!SorterType.Action.equalsActionCode(actionCode)) {
            rcParamChk = retCodeConfig.getInvalidActionCode();
        }
        Set<ObjectIdentifier> needHoldLotList = new HashSet<>();
        List<Info.LotAliasName> list = new ArrayList<>();
        if (!actionCode.equals(SorterType.Action.RFIDRead.getValue())
                && !actionCode.equals(SorterType.Action.RFIDWrite.getValue())
                && params.getComponentJob().getResultCode() != 0) {
            Validations.check(CimArrayUtils.isEmpty(waferList)
                    || 0 == CimArrayUtils.getSize(waferList), retCodeConfig.getInvalidParameter());
            boolean foundError = false;
            //-------------------------------
            //  Check INPUT PARAMETER: sorterStatus
            //-------------------------------
            if (log.isDebugEnabled()) {
                log.debug("Check INPUT PARAMETER: sorterStatus ");
            }

            for (Info.WaferSorterSlotMap slotMapInfo : waferList) {
                if (!slotMapInfo.getStatus()) {
                    foundError = true;
                    needHoldLotList.add(slotMapInfo.getLotID());
                }
            }
            if (foundError) {
                rcParamChk = retCodeConfigEx.getInvalidSorterstatus();
            }
        }
        //因为是在sortActionReq已经进行的逻辑分批,在创建的sortjob的时候发送给EAP是母lot
        // 此时验证EAP报告回来的是母lot，所以要子lot替换母lot
        if(actionCode.equals(SorterType.Action.Separate.getValue())){
            Info.SortJobPostAct postAct = sorterNewMethod.getPostAct(ObjectIdentifier.fetchValue(params.getSorterJobID()));
            for (Info.WaferSorterSlotMap slotMapInfo : waferList) {
                if(ObjectIdentifier.equalsWithValue(slotMapInfo.getLotID(), postAct.getParentLotId())){
                    slotMapInfo.setLotID(postAct.getChildLotId());
                }
            }
        }
        //T7code默认不重复
        boolean waferRepeat = false;
        if (!actionCode.equals(SorterType.Action.RFIDRead.getValue())
                && !actionCode.equals(SorterType.Action.RFIDWrite.getValue())) {

            for (Info.WaferSorterSlotMap slotMapInfo : waferList) {
                //获取lot与AliasName关系,验证T7code是否存在其他lotID中，或者是否为空，或者在DB中不存在
                Info.LotAliasName info = sorterNewMethod
                        .checkLotAndAliasNameRelationship(slotMapInfo.getLotID(), slotMapInfo.getAliasName());
                if (!info.getCompareResults() && !ObjectIdentifier.isEmpty(info.getRelationLotID())) {
                    list.add(info);
                    needHoldLotList.add(slotMapInfo.getLotID());
                    waferRepeat = true;
                }
            }
        }
        //----------------------------------
        // GET Requesting omsortjob_comp_Slotmap
        //----------------------------------
        if (log.isDebugEnabled()) {
            log.debug("GET Requesting dbSortJobInfoResult.");
        }
        Info.SortJobInfo dbSortJobInfoResult = sorterNewMethod.getSortJobInfoResultBySortJob(objCommon,
                params.getSorterJobID(), params.getComponentJob().getComponentJobID());
        Info.ComponentJob omsComponentJob = dbSortJobInfoResult.getComponentJob();

        if (log.isDebugEnabled()) {
            log.debug("Check Whether the inbound carrier and DB parameters are the same ");
        }
        if (!actionCode.equals(SorterType.Action.RFIDRead.getValue())
                && !actionCode.equals(SorterType.Action.RFIDWrite.getValue())) {
            Validations.check(!CimObjectUtils.equals(omsComponentJob.getDestinationCassetteID(),
                    tcsComponentJob.getDestinationCassetteID()), retCodeConfigEx.getEapReportedError());
            Validations.check(!CimObjectUtils.equals(omsComponentJob.getOriginalCassetteID(),
                    tcsComponentJob.getOriginalCassetteID()), retCodeConfigEx.getEapReportedError());
        }

        //当isComplete=true可以将componentJob的状态改成Complete
        boolean isComplete = true;

        //-------------------------
        //  When TCS RC is ERROR
        //-------------------------

        //将EAP传入过来的数据修改到历史表
        if (!CimStringUtils.equals(actionCode, SorterType.Action.RFIDRead.getValue())
                && !CimStringUtils.equals(actionCode, SorterType.Action.RFIDWrite.getValue())) {
            sorterNewMethod.updateComponentJob(objCommon, tcsComponentJob);
        }

        if (log.isTraceEnabled()) {
            log.trace("EAP Result Code : {} and rcParamChk : {}", params.getComponentJob().getResultCode(), rcParamChk.getCode());
        }
        if (params.getComponentJob().getResultCode() != 0 || !Validations.isEquals(rcParamChk, retCodeConfig.getSucc())) {
            //--------------------------------------------------------------------
            //  Sorter Job Status Update (Executing -> Errored)
            //--------------------------------------------------------------------
            if (log.isDebugEnabled()) {
                log.debug("Sorter Job Status Update (Executing -> Errored)");
            }
            this.sxAssembleStatusChg(objCommon, ObjectIdentifier.buildWithValue(tcsComponentJob.getComponentJobID()),
                    SorterType.JobType.ComponentJob.getValue(), SorterType.Status.Error.getValue(), true);
            //----------------------------------------
            //  MKAE A CASSETTE NOT AVAILABLE
            //----------------------------------------
            this.holdLotAndMultiDurableStatusChangeReq(objCommon, params, needHoldLotList,list);
            rcParamChk = retCodeConfigEx.getEapReportedError();
        } else {
            //----------------------------------------
            //   CASE Action Code is Read
            //----------------------------------------
            ObjectIdentifier destinationCassetteID = tcsComponentJob.getDestinationCassetteID();
            if (CimStringUtils.equals(actionCode, SorterType.Action.T7CodeRead.getValue())) {
                boolean ckeckSTATUS = true;
                boolean bRecCompStat;
                boolean bFosb = false;
                //---------------------------------
                //   Get a object length
                //---------------------------------
                List<Infos.WaferMapInCassetteInfo> strCassetteGetWaferMapOut = new ArrayList<>();
                int nSlotMap = CimArrayUtils.getSize(waferList);
                if (log.isDebugEnabled()) {
                    log.debug("SlotMap Count = {}", nSlotMap);
                }
                for (ObjectIdentifier mmcassetteID : cassetteIDs) {
                    //---------------------------------------------------
                    // Get Slot Map On MM Server (SlotMap Key)
                    //---------------------------------------------------
                    try {
                        strCassetteGetWaferMapOut = cassetteMethod.cassetteGetWaferMapDR(objCommon, mmcassetteID);
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {
                            bFosb = true;
                        } else {
                            throw e;
                        }
                    }
                    if (CimBooleanUtils.isFalse(bFosb)) {
                        for (Info.WaferSorterSlotMap slotMapInfo : waferList) {
                            bRecCompStat = false;
                            for (Infos.WaferMapInCassetteInfo cassetteInfo : strCassetteGetWaferMapOut) {
                                ObjectIdentifier tmpWaferID = cassetteInfo.getWaferID();
                                String tmpAliasWaferName = cassetteInfo.getAliasWaferName();
                                if (log.isTraceEnabled()) {
                                    log.trace("cassetteInfo-->tmpWaferID,{};tmpAliasWaferName,{}", tmpWaferID, tmpAliasWaferName);
                                }

                                if (ObjectIdentifier.isEmpty(tmpWaferID)) {
                                    continue;
                                }
                                //---------------------------------------------------
                                // MM Exist Sequence VS TCS Repry Sequence
                                //---------------------------------------------------
                                if (ObjectIdentifier.equalsWithValue(mmcassetteID, destinationCassetteID)
                                        && (cassetteInfo.getSlotNumber().equals(slotMapInfo.getDestinationSlotNumber()))) {
                                    ObjectIdentifier waferID = slotMapInfo.getWaferID();
                                    if (log.isTraceEnabled()) {
                                        log.trace("slotMapInfo-->waferID,{};AliasName,{}", waferID, slotMapInfo.getAliasName());
                                    }
                                    if (ObjectIdentifier.isEmptyWithValue(waferID) || ObjectIdentifier.equalsWithValue(tmpWaferID, waferID)) {
                                        slotMapInfo.setWaferID(tmpWaferID);
                                        //如果EAP报告的resultCode=0并且status=true但是DB中AliasWaferName为空,
                                        // EAP报告给OMS的AliasName也为空这种情况，视为一种异常 但是job成功
                                        if (CimStringUtils.equals(slotMapInfo.getAliasName(), tmpAliasWaferName)
                                                && !StringUtils.isEmpty(slotMapInfo.getAliasName())
                                                && !StringUtils.isEmpty(tmpAliasWaferName)) {
                                            bRecCompStat = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (CimBooleanUtils.isFalse(bRecCompStat)) {
                                needHoldLotList.add(slotMapInfo.getLotID());
                                ckeckSTATUS = false;
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Cassette Is Fosb");
                        }
                    }
                }

                //----------------------------------------
                //  MAKE A CASSETTE IS NOT AVAILABLE
                //----------------------------------------
                if (log.isTraceEnabled()) {
                    log.trace("ckeckSTATUS,{}",ckeckSTATUS);
                }

                if (CimBooleanUtils.isFalse(ckeckSTATUS)) {
                    //此时的T7code存在与其他Lot中
                    if(waferRepeat){
                        isComplete = false;
                    }else {
                        isComplete = true;
                    }
                    this.holdLotAndMultiDurableStatusChangeReq(objCommon, params, needHoldLotList,list);
                    rcParamChk = retCodeConfigEx.getSlotNoMismatchSlotmapEqpctnpst();
                }
            } else {
                if (!actionCode.equals(SorterType.Action.RFIDRead.getValue())
                        && !actionCode.equals(SorterType.Action.RFIDWrite.getValue())) {
                    if (log.isDebugEnabled()) {
                        log.debug("params.getActionCode()() != RFIDRead or RFIDWrite");
                    }

                    //----------------------------------------
                    //  TCS Reply Data -> Request Data Compare
                    //----------------------------------------
                    int nSlotMapWriteLen = CimArrayUtils.getSize(waferList);
                    List<Info.WaferSorterSlotMap> tmpSlotSeq = omsComponentJob.getWaferList();
                    int nGetSlotMapDRLen = CimArrayUtils.getSize(tmpSlotSeq);
                    boolean ckeckSTATUS = true;
                    boolean bRecCompStat;
                    if (log.isTraceEnabled()) {
                        log.trace("nSlotMapWriteLen,{};nGetSlotMapDRLen,{}",nSlotMapWriteLen,nGetSlotMapDRLen);
                    }
                    if (nSlotMapWriteLen != nGetSlotMapDRLen) {
                        ckeckSTATUS = false;
                    } else {
                        //----------------------------------------
                        //  Data Compare
                        //----------------------------------------
                        for (Info.WaferSorterSlotMap tcsWaferMap : waferList) {
                            bRecCompStat = false;
                            for (Info.WaferSorterSlotMap omsWaferMap : tmpSlotSeq) {
                                if (ObjectIdentifier.equalsWithValue(tcsWaferMap.getWaferID(), omsWaferMap.getWaferID())
                                        && ObjectIdentifier.equalsWithValue(tcsComponentJob.getDestinationCassetteID(), omsComponentJob.getDestinationCassetteID())
                                        && ObjectIdentifier.equalsWithValue(tcsComponentJob.getDestinationPortID(), omsComponentJob.getDestinationPortID())
                                        && tcsWaferMap.getDestinationSlotNumber().equals(omsWaferMap.getDestinationSlotNumber())
                                        && ObjectIdentifier.equalsWithValue(tcsComponentJob.getOriginalCassetteID(), omsComponentJob.getOriginalCassetteID())
                                        && ObjectIdentifier.equalsWithValue(tcsComponentJob.getOriginalPortID(), omsComponentJob.getOriginalPortID())) {
                                    if (!SorterType.Action.WaferStart.getValue().equals(actionCode)) {
                                        if (tcsWaferMap.getOriginalSlotNumber().equals(omsWaferMap.getOriginalSlotNumber())
                                                //如果EAP报告的resultCode=0并且status=true但是DB中AliasWaferName为空,
                                                // EAP报告给OMS的AliasName也为空这种情况，视为一种异常,此时sj为Error
                                                && CimStringUtils.equals(tcsWaferMap.getAliasName(), omsWaferMap.getAliasName())
                                                && !StringUtils.isEmpty(tcsWaferMap.getAliasName())
                                                && !StringUtils.isEmpty(omsWaferMap.getAliasName())) {
                                            bRecCompStat = true;
                                            break;
                                        }
                                    } else {
                                        if (!StringUtils.isEmpty(tcsWaferMap.getAliasName())) {
                                            bRecCompStat = true;
                                            break;
                                        }
                                    }

                                }
                            }

                            if (CimBooleanUtils.isFalse(bRecCompStat)) {
                                needHoldLotList.add(tcsWaferMap.getLotID());
                                ckeckSTATUS = false;
                            }
                        }
                    }

                    if (log.isTraceEnabled()) {
                        log.trace("ckeckSTATUS,{}",ckeckSTATUS);
                    }
                    //----------------------------------------
                    //  MKAE A CASSETTE NOT AVAILABLE
                    //----------------------------------------
                    if (CimBooleanUtils.isFalse(ckeckSTATUS) || waferRepeat) {
                        isComplete = false;
                        this.holdLotAndMultiDurableStatusChangeReq(objCommon, params, needHoldLotList, list);
                        rcParamChk = retCodeConfigEx.getWafersorterSlotmapCompareError();
                    }
                    if (CimBooleanUtils.isTrue(ckeckSTATUS)) {
                        if (CimStringUtils.equals(actionCode, SorterType.Action.LotTransfer.getValue())
                                || CimStringUtils.equals(actionCode, SorterType.Action.WaferEnd.getValue())
                                || CimStringUtils.equals(actionCode, SorterType.Action.Combine.getValue())
                                || CimStringUtils.equals(actionCode, SorterType.Action.Separate.getValue())) {
                            if (log.isDebugEnabled()) {
                                log.debug("sxWaferSlotmapChangeRpt");
                            }
                            this.sxWaferSlotmapChangeRpt(objCommon, params);
                            for (ObjectIdentifier cassetteID : cassetteIDs) {
                                equipmentMethod.equipmentLotInCassetteAdjust(objCommon, params.getEquipmentID(), cassetteID);
                            }

                        }
                    }
                }
            }
            if(!isComplete){
                this.sxAssembleStatusChg(objCommon, ObjectIdentifier.buildWithValue(tcsComponentJob.getComponentJobID()),
                        SorterType.JobType.ComponentJob.getValue(), SorterType.Status.Error.getValue(), true);
            }
            if (Validations.isEquals(rcParamChk, retCodeConfig.getSucc()) ||  isComplete) {
                //----------------------------------------------------------
                //  Sorter Job Status Update (Update -> Completed)
                //----------------------------------------------------------
                this.sxAssembleStatusChg(objCommon, ObjectIdentifier.buildWithValue(tcsComponentJob.getComponentJobID()),
                        SorterType.JobType.ComponentJob.getValue(), SorterType.Status.Completed.getValue(), true);

                //----------------------------------------------------------
                //  更改lot--->filp状态
                //----------------------------------------------------------
                Set<ObjectIdentifier> lotIDs = tcsComponentJob.getWaferList().stream()
                        .map(Info.WaferSorterSlotMap::getLotID).collect(Collectors.toSet());
                if (CimStringUtils.equals(actionCode, SorterType.Action.Flip.getValue())) {
                    if (!CimArrayUtils.isEmpty(lotIDs)) {
                        lotIDs.forEach(lotID -> {
                            sorterNewMethod.updateLotFilpStatus(ObjectIdentifier.fetchValue(lotID));
                        });
                    }
                }
                //----------------------------------------------------------
                //  更改lot--->RELATION_FOUP_FLAG状态
                //----------------------------------------------------------
                if (CimStringUtils.equals(actionCode, SorterType.Action.RFIDWrite.getValue())) {
                    ObjectIdentifier des = tcsComponentJob.getDestinationCassetteID();
                    ObjectIdentifier src = tcsComponentJob.getOriginalCassetteID();
                    sorterNewMethod.updateRelationFoupFlag(src, des);
                }

                //----------------------------------------------------------
                //  Delete soter job, if Sorter Job Status is all compeleted
                //----------------------------------------------------------
                com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.
                        Params.ObjSorterJobListGetDRIn();
                objSorterJobListGetDRIn.setSorterJob(params.getSorterJobID());
                List<Info.SortJobListAttributes> jobListGetDROut = sorterNewMethod
                        .sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
                int srtJobLen = CimArrayUtils.getSize(jobListGetDROut);
                if (srtJobLen > 0) {
                    int compStatusLen = 0;
                    int compoJobLen = CimArrayUtils.getSize(jobListGetDROut.get(0).getSorterComponentJobListAttributesList());
                    for (Info.SorterComponentJobListAttributes componentJob :
                            jobListGetDROut.get(0).getSorterComponentJobListAttributesList()) {
                        if (CimStringUtils.equals(SorterType.Status.Completed.getValue(),componentJob.getComponentSorterJobStatus())) {
                            compStatusLen++;
                        } else {
                            break;
                        }
                    }
                    //----------------------------------------------------------
                    //  Delete sorter job
                    //----------------------------------------------------------
                    if (compStatusLen == compoJobLen) {
                        if (log.isDebugEnabled()) {
                            log.debug("compStatusLen == compoJobLen");
                        }
                        //on route 目前只是一对一，比如lot transfer,转角
                        if (!ObjectIdentifier.isEmpty(params.getControlJobID())) {
                            com.fa.cim.dto.Params.OpeComWithDataReqParams opeComWithDataReqParams =
                                    new com.fa.cim.dto.Params.OpeComWithDataReqParams();
                            opeComWithDataReqParams.setControlJobID(params.getControlJobID());
                            opeComWithDataReqParams.setEquipmentID(params.getEquipmentID());
                            opeComWithDataReqParams.setUser(params.getUser());
                            opeComWithDataReqParams.setSpcResultRequiredFlag(false);
                            objCommon.setTransactionID(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue());
                            equipmentProcessOperation.sxMoveOutReq(objCommon, opeComWithDataReqParams);
                            //跳站
                            return new ArrayList<>(lotIDs);
                        }
                    }
                } else {
                    Validations.check(retCodeConfigEx.getInvalidSorterJobid(), ObjectIdentifier.fetchValue(params.getSorterJobID()));
                }
            }
        }
        if (!Validations.isEquals(rcParamChk, retCodeConfig.getSucc())) {
            Validations.check(rcParamChk);
        }
        return new ArrayList<>();
    }


    @Override
    public void sxOnlineSorterSlotmapAdjustReq(Infos.ObjCommon objCommon, Params.OnlineSorterSlotmapAdjustReqParam params) {
        //init
        ObjectIdentifier equipmentID = params.getEquipmentID();
        String adjustDirection = params.getAdjustDirection();
        Validations.check(CimStringUtils.unEqual(SorterType.Action.AdjustByMES.getValue(), adjustDirection)
                        && CimStringUtils.unEqual(SorterType.Action.AdjustByTool.getValue(), adjustDirection),
                retCodeConfigEx.getSorterAdjustDirectionError());

        // Get required equipment lock mode
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(params.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

        //验证传入的lot是否只有一个
        List<ObjectIdentifier> lotIDs = Optional.ofNullable(params.getStrWaferXferSeq().getWaferList())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Info.WaferSorterSlotMap::getLotID)
                .distinct()
                .collect(Collectors.toList());
        Validations.check(CimArrayUtils.getSize(lotIDs) != 1, retCodeConfigEx.getSorterT7CodeReadLotNumberError());

        //验证waferIDRead结果是否和MES中数据一致，不一致再进行后续操作
        Info.SortJobInfo sortJobInfo = new Info.SortJobInfo();
        sortJobInfo.setComponentJob(params.getStrWaferXferSeq());
        sortJobInfo.setOpeMemo(params.getClaimMemo());
        sortJobInfo.setEquipmentID(params.getEquipmentID());
        sortJobInfo.setPortGroup(params.getPortGroup());

        //验证对比结果中是否有不匹配的
        Params.SJListInqParams sjListInqParams = new Params.SJListInqParams();
        sjListInqParams.setCarrierID(params.getStrWaferXferSeq().getOriginalCassetteID());
        sjListInqParams.setLotID(lotIDs.get(0));
        sjListInqParams.setEquipmentID(params.getEquipmentID());
        Info.WaferSorterCompareCassette waferSorterCompareCassette = this.sxOnlineSorterSlotmapCompareReq(objCommon, sjListInqParams);
        boolean isAllMatch = Optional.ofNullable(waferSorterCompareCassette.getStrWaferSorterCompareSlotMapSequence())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Info.WaferSorterCompareSlotMap::getCompareStatus)
                .allMatch(status -> CimStringUtils.equals(SorterType.CompareResult.Match.getValue(), status));
        Validations.check(isAllMatch, retCodeConfigEx.getSorterDataConsistent());

        Long lockMode = objLockModeOut.getLockMode();
        if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

            // Advanced Mode
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);
        }

        //================================
        // Check equipment availability
        //================================
        //【step3】 - equipment_CheckAvail
        equipmentMethod.equipmentCheckAvail(objCommon, equipmentID);

        //================================================================
        // Checking Action one's personal history (Look for WaferIDRead)
        //================================================================
        //【step4】 - waferSorter_CheckCondition_AfterWaferIdReadDR
        Info.ComponentJob strWaferSorterSlotMapSequence = params.getStrWaferXferSeq();
        Validations.check(!SorterType.Action.T7CodeRead.equals(strWaferSorterSlotMapSequence.getActionCode()),
                retCodeConfigEx.getInvalidSorterJobType());
        //--------------------------------------------------------
        // Case adjustDirection is SP_Sorter_Adjust_To_MM
        //--------------------------------------------------------
        if (SorterType.Action.AdjustByMES.equals(adjustDirection)) {
            //---------------------------------------------------------------------------------
            // Collect participant cassettes (original/destination) from WaferSorterSlotMapSeq.
            //---------------------------------------------------------------------------------

            List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
            cassetteIDs.add(strWaferSorterSlotMapSequence.getOriginalCassetteID());
            cassetteIDs.add(strWaferSorterSlotMapSequence.getDestinationCassetteID());

            if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
                log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

                // Advanced Mode
                // Lock Equipment Main Object
                Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
                objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
                objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                objAdvancedObjectLockIn.setLockType(Long.valueOf(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE));
                objAdvancedObjectLockIn.setKeyList(new ArrayList<>());
                objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

            }
            objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
            log.info("adjust to MM");
            //【step7】 - sorter job create
            com.fa.cim.sorter.Params.SJCreateReqParams sjCreateReqParams = new com.fa.cim.sorter.Params.SJCreateReqParams();
            sjCreateReqParams.setPortGroupID(params.getPortGroup());
            sjCreateReqParams.setClaimMemo(params.getClaimMemo());
            sjCreateReqParams.setUser(params.getUser());
            sjCreateReqParams.setEquipmentID(params.getEquipmentID());
            sjCreateReqParams.setWaferIDReadFlag(false);
            List<Info.ComponentJob> componentAttributes = new ArrayList<>();
            strWaferSorterSlotMapSequence.setActionCode(SorterType.Action.AdjustByMES.getValue());
            componentAttributes.add(strWaferSorterSlotMapSequence);
            sjCreateReqParams.setStrSorterComponentJobListAttributesSequence(componentAttributes);
            this.sxSJCreateReq(objCommon, sjCreateReqParams);
        }
        //--------------------------------------------------------
        // Case adjustDirection is SP_Sorter_Adjust_To_WaferSorter
        //--------------------------------------------------------
        else {
            //================================================================
            // Checking Previous Job is running or not
            //================================================================
            //【step8】 - waferSorter_CheckRunningJobs

            //---------------------------------------------------------------------------------
            // Collect participant cassettes (original/destination) from WaferTransferSequence.
            //---------------------------------------------------------------------------------
            Info.ComponentJob strWaferXferSeq = params.getStrWaferXferSeq();

            List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
            cassetteIDs.add(strWaferXferSeq.getOriginalCassetteID());
            cassetteIDs.add(strWaferXferSeq.getDestinationCassetteID());

            if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
                log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

                // Advanced Mode
                // Lock Equipment Main Object
                Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
                objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
                objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                objAdvancedObjectLockIn.setLockType(Long.valueOf(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE));
                objAdvancedObjectLockIn.setKeyList(new ArrayList<>());
                objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);
            }

            objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

            //---------------------------------------
            // 当执行AdjustByTool时候，不能存在AdjustByMES的Sorter
            //---------------------------------------
            Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new Params.ObjSorterJobListGetDRIn();
            objSorterJobListGetDRIn.setCarrierID(params.getStrWaferXferSeq().getOriginalCassetteID());
            objSorterJobListGetDRIn.setLotID(lotIDs.get(0));
            objSorterJobListGetDRIn.setActionCode(SorterType.Action.AdjustByMES.getValue());
            List<Info.SortJobListAttributes> sortJobListAttributes = sorterNewMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
            Validations.check(CimArrayUtils.isNotEmpty(sortJobListAttributes), retCodeConfigEx.getSorterAdjustExist(), lotIDs.get(0));

            //---------------------------------------
            // Check input parameter for
            // wafer transfer (Sorter operation)
            //---------------------------------------
            //【step11】- sorter_waferTransferInfo_Verify
            sorterNewMethod.sorterWaferTransferInfoVerify(objCommon, sortJobInfo, SorterType.Action.AdjustByTool.getValue());

            //---------------------------------------
            // Check input parameter and
            // Server data condition
            //---------------------------------------
            //【step12】 - cassette_CheckConditionForWaferSort
            sorterNewMethod.cassetteCheckConditionForWaferSort(objCommon, sortJobInfo);

            //------------------------------------------------------------
            // Retrieve Equipment's onlineMode
            //------------------------------------------------------------
            //【step13】 - equipment_onlineMode_Get

            log.info("bNotifyToTCS != TRUE  && onlineMode == SP_Eqp_OnlineMode_OnlineRemote");

            //【step14】 - sorter_waferTransferInfo_Restructure
            com.fa.cim.sorter.Results.ObjSorterWaferTransferInfoRestructureOut restructureSorterWaferTransferInfoOut =
                    sorterNewMethod.sorterWaferTransferInfoRestructure(objCommon, sortJobInfo);
            //------------------------------------------------------------
            // Check output parameter of sorter_waferTransferInfo_Restructure()
            //------------------------------------------------------------
            log.info("Check output parameter of sorter_waferTransferInfo_Restructure()");
            for (int i = 0; i < CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotInventoryStateList()); i++) {
                log.info("Lot ID : {}", restructureSorterWaferTransferInfoOut.getLotInventoryStateList().get(i));
                int nOutputWaferLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList());
                for (int j = 0; j < nOutputWaferLen; j++) {
                    log.info("Wafer ID : {}", restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getWaferID().getValue());
                    log.info("Slot Number : {}", restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getSlotNumber());
                }
            }
            //---------------------------------------
            // At first, all relation between
            // carrier-wafer should be canceled
            // If bNotifyToTCS is true, Only Check Logic
            // works inside of wafer_materialContainer_Change()
            // or lot_materialContainer_Change()
            //---------------------------------------
            ObjectIdentifier newCassetteID = new ObjectIdentifier();
            int nILen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotInventoryStateList());
            for (int i = 0; i < nILen; i++) {
                log.info("strLotInventoryStateSeq == SP_Lot_InventoryState_InBank,{}", i);
                int nJLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList());
                for (int j = 0; j < nJLen; j++) {
                    //【step15】 - wafer_materialContainer_Change
                    Infos.Wafer strWafer = new Infos.Wafer();
                    strWafer.setWaferID(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getWaferID());
                    strWafer.setSlotNumber(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getSlotNumber());
                    waferMethod.waferMaterialContainerChange(objCommon, newCassetteID, strWafer);
                    // Input parameter
                    //  - equipmentID = input equipmentID
                    //  - newCassetteID = null
                    //  - strWafer = strSorter_waferTransferInfo_Restructure_out.strLotSeq[i].strWafer[j]
                    //  - bNotifyToTCS = input bNotifyToTCS
                }
            }
            //---------------------------------------
            // Next, new relation between
            // carrier-wafer should be created
            //---------------------------------------
            nILen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotInventoryStateList());
            for (int i = 0; i < nILen; i++) {
                log.info("strLotInventoryStateSeq == SP_Lot_InventoryState_InBank = {}", i);
                int nJLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList());
                for (int j = 0; j < nJLen; j++) {
                    //【step16】 - wafer_materialContainer_Change
                    Infos.Wafer strWafer = new Infos.Wafer();
                    strWafer.setWaferID(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getWaferID());
                    strWafer.setSlotNumber(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getSlotNumber());
                    waferMethod.waferMaterialContainerChange(objCommon,
                            restructureSorterWaferTransferInfoOut.getCassetteIDList().get(i), strWafer);
                    // Input parameter
                    //  - equipmentID = input equipmentID
                    //  - newCassetteID = strSorter_waferTransferInfo_Restructure_out.cassetteIDSeq[i]
                    //  - strWafer = strSorter_waferTransferInfo_Restructure_out.strLotSeq[i].strWafer[j]
                    //修改AliasName
                    Infos.WaferSorter waferSorter = new Infos.WaferSorter();
                    waferSorter.setWaferID(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getWaferID());
                    waferSorter.setAliasName(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getAliasName());
                    waferMethod.waferSorterAliasNameUpdate(objCommon, waferSorter);
                }
            }
            //---------------------------------------
            // Collect Cassette IDs of input parameter
            //---------------------------------------
            log.info("Collect Cassette IDs of input parameter");

            //---------------------------------------
            // Update Machine/ControlJob related information
            //---------------------------------------
            log.info("Call controlJob_relatedInfo_Update");
            //【step17】 - controlJob_relatedInfo_Update
            controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDs);
            //---------------------------------------
            // Update Carrier Multi Lot Type
            //---------------------------------------
            log.info("Update Carrier Multi Lot Type");
            for (ObjectIdentifier cassetteID : cassetteIDs) {
                //【step18】 - cassette_multiLotType_Update
                try {
                    cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
                } catch (ServiceException e) {
                    boolean checkFlag = e.getCode() != retCodeConfig.getSucc().getCode() && e.getCode() != retCodeConfig.getNotFoundCassette().getCode();
                    Validations.check(checkFlag, new OmCode(e.getCode(), e.getMessage()));
                }
            }

            //---------------------------------------
            // Update WaferLotHistoryPointer of Lot
            //---------------------------------------
            log.info("Update WaferLotHistoryPointer of Lot");
            for (int i = 0; i < CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList()); i++) {
                log.info(" ##### Update lotID = {}", restructureSorterWaferTransferInfoOut.getLotList().get(i).getLotID().getValue());
                //【step19】 - lot_waferLotHistoryPointer_Update
                lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, restructureSorterWaferTransferInfoOut.getLotList().get(i).getLotID());
            }
            //---------------------------------------
            // Prepare input parameter of lotWaferSortEvent_Make()
            //---------------------------------------
            log.info("Prepare input parameter of lotWaferSortEvent_Make()");
            for (int i = 0; i < CimArrayUtils.getSize(strWaferXferSeq.getWaferList()); i++) {
                Boolean bStringifiedObjRefFilled = false;
                for (int j = 0; j < CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList()); j++) {
                    for (int k = 0; k < CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(j).getWaferList()); k++) {
                        if (CimObjectUtils.equals(strWaferXferSeq.getWaferList().get(i).getWaferID(),
                                restructureSorterWaferTransferInfoOut.getLotList().get(j).getWaferList().get(k).getWaferID())) {
                            bStringifiedObjRefFilled = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bStringifiedObjRefFilled)) {
                        break;
                    }
                }
            }
            //---------------------------------------
            // Create Wafer Sort Event
            //---------------------------------------
            log.info("Create Wafer Sort Event");
            //【step20】 - lotWaferSortEvent_Make
            // todo jerry 2021/6/29 2:52 下午 lotWaferSortEventMake 需要完善
            //  eventMethod.lotWaferSortEventMake(objCommon, TransactionIDEnum.WAFER_SORTER_POSITION_ADJUST_REQ.getValue(),strWaferXferSeq.getWaferList() , claimMemo);
        }
    }

    @Override
    public void sxSJConditionCheckReq(Infos.ObjCommon objCommon, Params.SortJobCheckConditionReqInParam params) {
        sorterNewMethod.sorterCheckConditionForJobCreate(objCommon, params.getStrSorterComponentJobListAttributesSequence(),
                params.getEquipmentID(), params.getPortGroupID(), null);
    }

    @Override
    public void sxWaferSlotmapChangeReq(Infos.ObjCommon objCommon, Info.SortJobInfo params, boolean notifyToTCS) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        //------------------------------------------------------------
        // Retrieve Equipment's onlineMode
        //------------------------------------------------------------
        Long lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE;
        String onlineMode = BizConstant.SP_EQP_ONLINEMODE_OFFLINE;
        if (!ObjectIdentifier.isEmptyWithValue(equipmentID)) {
            // object_lockMode_Get
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(equipmentID);
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.WAFER_SORT_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                // advanced_object_Lock
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                        objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
            }
            // equipment_onlineMode_Get
            onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
        }

        //-----------------------
        // Cassette ID Collection
        //-----------------------
        Boolean bCassetteFind = false;
        List<ObjectIdentifier> casts = new ArrayList<>();
        List<String> castIDSeq = new ArrayList<>();
        Info.ComponentJob componentJob = params.getComponentJob();
        if (!SorterHandler.containsFOSB(componentJob.getDestinationCassetteID())) {
            casts.add(componentJob.getDestinationCassetteID());
            castIDSeq.add(ObjectIdentifier.fetchValue(componentJob.getDestinationCassetteID()));
        }
        if (!SorterHandler.containsFOSB(componentJob.getOriginalCassetteID())) {
            casts.add(componentJob.getOriginalCassetteID());
            castIDSeq.add(ObjectIdentifier.fetchValue(componentJob.getOriginalCassetteID()));
        }

        // advanced_object_Lock
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                    (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, castIDSeq));
        }
        //--------------------------------------------------
        // Collect cassette's equipment ID / Cassette ObjRef
        //--------------------------------------------------
        for (ObjectIdentifier carrierID : casts) {
            ObjectIdentifier controlJobID = null;
            //---------------------------------------------------
            // Error Judgement
            // If rc was RC_NOT_FOUND_CASSETTE, It is OK for FOSB
            //---------------------------------------------------
            int retCode = 0;
            try {
                controlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, carrierID);
            } catch (ServiceException e) {
                retCode = e.getCode();
                if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {
                    throw e;
                }
            }
            if (!ObjectIdentifier.isEmptyWithValue(controlJobID)) {
                throw new ServiceException(retCodeConfig.getNotClearedControlJob());
            }
            if (retCode == 0) {
                /*-------------------------------*/
                /*   Check SorterJob existence   */
                /*-------------------------------*/
                List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
                cassetteIDs.add(carrierID);
                Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
                objWaferSorterJobCheckForOperation.setCassetteIDList(cassetteIDs);
                objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_CAST);
                waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
            }
        }

        //contamination check
        List<Info.WaferSorterSlotMap> waferList = componentJob.getWaferList();
        if (CimArrayUtils.isNotEmpty(waferList)) {
            for (Info.WaferSorterSlotMap slotMap : waferList) {
                Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
                waferTransfer.setWaferID(slotMap.getWaferID());
                waferTransfer.setDestinationCassetteID(componentJob.getDestinationCassetteID());
                waferTransfer.setDestinationSlotNumber(slotMap.getDestinationSlotNumber());
                waferTransfer.setOriginalCassetteID(componentJob.getOriginalCassetteID());
                waferTransfer.setOriginalSlotNumber(slotMap.getOriginalSlotNumber());
                contaminationMethod.lotWaferSlotMapChangeCheck(objCommon,waferTransfer);
            }
        }
        //------------------------------------------------------------
        // If bNotifyToTCSFlag is true, Verify request inside of MM
        // and notify to EAP
        //------------------------------------------------------------
        if (CimBooleanUtils.isTrue(notifyToTCS)) {
            //sorter_waferTransferInfo_Verify
            sorterNewMethod.sorterWaferTransferInfoVerify(objCommon, params, SorterType.Action.AdjustByMES.getValue());

            //---------------------------------------
            // Check input parameter and
            // Server data condition
            //---------------------------------------
            sorterNewMethod.cassetteCheckConditionForWaferSort(objCommon, params);
            //【TODO】【TODO - NOTIMPL】- TCSMgr_SendWaferSlotmapChangeReq
        }
        //------------------------------------------------------------
        // If bNotifyToTCSFlag is false, Update MM Data and
        // does not notify to EAP
        //------------------------------------------------------------
        else if (CimBooleanUtils.isFalse(notifyToTCS)) {
            this.sxWaferSlotmapChangeRpt(objCommon, params);
        } else {
            Validations.check(retCodeConfig.getInvalidSorterOperation());
        }
    }

    @Override
    public void sxCarrierExchangeReq(Infos.ObjCommon objCommon, Info.SortJobInfo sortJobInfo) {
        ObjectIdentifier equipmentID = sortJobInfo.getEquipmentID();
        Validations.check(equipmentID == null, "equipmentID can not be null");

        Long lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE;
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            // Step1 - object_lockMode_Get
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(equipmentID);
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.CASSETTE_EXCHANGE_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                // Lock Equipment Main Object
                // Step2 - advanced_object_Lock
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                        objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
            }
        }
        //	Step3 - equipment_CheckConditionForEmptyCassetteEarlyOut, Check eqp allow early carrier out or not
        boolean earlyOutFlag = true;
        try {
            equipmentMethod.equipmentCheckConditionForEmptyCassetteEarlyOut(objCommon, equipmentID);
        } catch (ServiceException e) {
            earlyOutFlag = false;
        }

        log.debug("sxCarrierExchangeReq(): Check whether eqp is Internal Buffer or not.");
        if (earlyOutFlag){
            String equipmentCategory = null;
            if (lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_ELEMENT)) {
                //	Step4 - equipment_brInfo_GetDR__120
                Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
                equipmentCategory = eqpBrInfo.getEquipmentCategory();
                if (CimStringUtils.equals(equipmentCategory, BizConstant.SP_MC_CATEGORY_INTERNALBUFFER)) {
                    Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, equipmentID);
                    List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
                    int lenPortInfo = CimArrayUtils.getSize(eqpPortStatuses);
                    // Step6 - object_LockForEquipmentResource
                    for (int i = 0; i < lenPortInfo; i++) {
                        objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, eqpPortStatuses.get(i).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                    }
                }
            }
        }
        //Collect participant cassettes (original/destination) from WaferTransferSequence.
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        Info.ComponentJob componentJob = sortJobInfo.getComponentJob();
        if (!SorterHandler.containsFOSB(componentJob.getDestinationCassetteID())) {
            cassetteIDs.add(componentJob.getDestinationCassetteID());
        }
        if (!SorterHandler.containsFOSB(componentJob.getOriginalCassetteID())) {
            cassetteIDs.add(componentJob.getOriginalCassetteID());
        }
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
            //	Step7 - cassette_controlJobID_Get,Get cassette's ControlJobID
            ObjectIdentifier cassetteControlJobIDOut = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteIDs.get(0));
            if (!ObjectIdentifier.isEmpty(cassetteControlJobIDOut)) {
                log.debug("sxCarrierExchangeReq(): Get controljob Info");
                //	Step8 - controlJob_containedLot_Get
                List<String> processLotList = new ArrayList<>();
                List<Infos.ControlJobCassette> controlJobCassettes = controlJobMethod.controlJobContainedLotGet(objCommon, cassetteControlJobIDOut);
                if (!CimArrayUtils.isEmpty(controlJobCassettes)) {
                    for (Infos.ControlJobCassette controlJobCassette : controlJobCassettes) {
                        if (CimArrayUtils.isNotEmpty(controlJobCassette.getControlJobLotList())) {
                            controlJobCassette.getControlJobLotList().forEach(controlJobLot -> {
                                if (controlJobLot.getOperationStartFlag()) {
                                    processLotList.add(controlJobLot.getLotID().getValue());
                                }
                            });
                        }
                    }
                }
                if (!CimArrayUtils.isEmpty(processLotList)) {
                    // Step9 - advanced_object_Lock
                    // Lock Equipment ProcLot Element (Write)
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT,
                            (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, processLotList));
                }
                // Lock eqp LoadCassette Element (Write)
                // Step10 - advanced_object_Lock
                List<String> loadCastSeq = cassetteIDs.stream().map(ObjectIdentifier::getValue).collect(Collectors.toList());
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                        (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, loadCastSeq));
                //Lock controljob Object
                // Step11 - object_Lock
                objectLockMethod.objectLock(objCommon, CimControlJob.class, cassetteControlJobIDOut);
            }
        }
        // Step12 - objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

        /*-------------------------------*/
        /*   Check SorterJob existence   */
        /*-------------------------------*/
        Inputs.ObjWaferSorterJobCheckForOperation checkForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        checkForOperation.setCassetteIDList(cassetteIDs);
        checkForOperation.setOperation(BizConstant.SP_OPERATION_FOR_CAST);
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, checkForOperation);

        //---------------------------------------
        // Check input parameter for
        // wafer transfer (Sorter operation)
        //---------------------------------------
        sorterNewMethod.sorterWaferTransferInfoVerify(objCommon, sortJobInfo, SorterType.Action.AdjustByMES.getValue());

        //contamination check
        for (Info.WaferSorterSlotMap waferSorterSlotMap : componentJob.getWaferList()) {
            contaminationMethod.carrierProductUsageTypeCheck(ObjectIdentifier.emptyIdentifier(),
                    waferSorterSlotMap.getLotID(),componentJob.getDestinationCassetteID());
            if (StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOn()) {
                //qiandao mode
                contaminationMethod.carrierExchangeCheckQiandaoMode(objCommon, componentJob.getDestinationCassetteID(),
                        waferSorterSlotMap.getLotID(),"");
            } else {
                //OMS mode
                Infos.WaferTransfer waferTransfer = getWaferTransfer(componentJob, waferSorterSlotMap);
                contaminationMethod.lotWaferCarrierExchangeChangeCheck(objCommon, waferTransfer,"");
            }
        }

        //	Step15 - cassette_CheckConditionForExchange
        // todo jerry 2021/7/2 9:18 上午 类型转换
        List<Infos.WaferTransfer> waferXferList = new ArrayList<>();
        for (Info.WaferSorterSlotMap waferSorterSlotMap : componentJob.getWaferList()) {
            Infos.WaferTransfer waferTransfer = getWaferTransfer(componentJob, waferSorterSlotMap);
            waferXferList.add(waferTransfer);
        }
        sorterNewMethod.cassetteCheckConditionForExchange(objCommon, equipmentID, waferXferList);
        if (!CimArrayUtils.isEmpty(cassetteIDs)) {
            for (ObjectIdentifier cassetteID : cassetteIDs) {
                String interFabXferState = cassetteMethod.cassetteInterFabXferStateGet(objCommon, cassetteID);
                Validations.check(CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING),
                        retCodeConfig.getInterfabInvalidLotXferstateForReq(), cassetteID, interFabXferState);
                /*-------------------------------*/
                /*  Get lot list in carrier      */
                /*-------------------------------*/
                //	Step17 - cassette_lotList_GetDR
                Infos.LotListInCassetteInfo cassetteLotListOut = cassetteMethod.cassetteLotListGetDR(objCommon, cassetteID);
                List<ObjectIdentifier> lotIDList = cassetteLotListOut.getLotIDList();
                if (!CimObjectUtils.isEmpty(lotIDList)) {
                    for (ObjectIdentifier lotID : lotIDList) {
                        String lotInterFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
                        Validations.check(CimStringUtils.equals(lotInterFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_ORIGINDELETING),
                                retCodeConfigEx.getInterfabInvalidXferstateDeleting(), lotID);
                    }
                }
            }
        }
        //	Step19 - sorter_waferTransferInfo_Restructure
        com.fa.cim.sorter.Results.ObjSorterWaferTransferInfoRestructureOut restructureSorterWaferTransferInfoOut =
                sorterNewMethod.sorterWaferTransferInfoRestructure(objCommon, sortJobInfo);
        //Reject if request lot is backup processing.
        List<Infos.PLot> lotList = restructureSorterWaferTransferInfoOut.getLotList();
        if (!CimArrayUtils.isEmpty(lotList)) {
            for (Infos.PLot pLot : lotList) {
                //	Step20 - lot_backupInfo_Get, Check Backup Info
                Infos.LotBackupInfo lotBackupInfoOut = lotMethod.lotBackupInfoGet(objCommon, pLot.getLotID());

                Validations.check(Boolean.FALSE.equals(lotBackupInfoOut.getCurrentLocationFlag())
                        || Boolean.TRUE.equals(lotBackupInfoOut.getTransferFlag()), retCodeConfig.getLotInOthersite());
            }
        }
        List<String> lotInventoryStateList = restructureSorterWaferTransferInfoOut.getLotInventoryStateList();
        if (!CimArrayUtils.isEmpty(lotInventoryStateList)) {
            ObjectIdentifier newCassetteID = new ObjectIdentifier();
            for (int i = 0; i < lotInventoryStateList.size(); i++) {
                if (CimStringUtils.equals(lotInventoryStateList.get(i), CIMStateConst.CIM_LOT_INVENTORY_STATE_INBANK)) {
                    List<Infos.PWafer> waferList = lotList.get(i).getWaferList();
                    for (Infos.PWafer pWafer : waferList) {
                        //	Step21 - wafer_materialContainer_Change
                        Infos.Wafer strWafer = new Infos.Wafer();
                        strWafer.setWaferID(pWafer.getWaferID());
                        strWafer.setSlotNumber(pWafer.getSlotNumber());
                        waferMethod.waferMaterialContainerChange(objCommon, newCassetteID, strWafer);
                        //Next, new relation between carrier-wafer should be created
                        //	Step23 - wafer_materialContainer_Change
                        Infos.Wafer waferInfo = new Infos.Wafer();
                        waferInfo.setWaferID(pWafer.getWaferID());
                        waferInfo.setSlotNumber(pWafer.getSlotNumber());
                        waferMethod.waferMaterialContainerChange(objCommon, restructureSorterWaferTransferInfoOut.getCassetteIDList().get(i), waferInfo);
                    }
                } else {
                    //	Step22 - lot_materialContainer_Change
                    lotMethod.lotMaterialContainerChange(newCassetteID, lotList.get(i));

                    //Next, new relation between carrier-wafer should be created
                    //	Step24 - lot_materialContainer_Change
                    lotMethod.lotMaterialContainerChange(restructureSorterWaferTransferInfoOut.getCassetteIDList().get(i), lotList.get(i));
                }
            }
        }
        //Collect cassette IDs of input parameter
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        CassetteMethod.collectCassetteIDsOfParams(waferXferList, cassetteIDList);
        //	Step25 - controlJob_relatedInfo_Update, Update Machine/controljob information
        List<ObjectIdentifier> cassetteIDStrList = new ArrayList<>(cassetteIDList);
        controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDStrList);
        if (earlyOutFlag){
            controlJobMethod.controlJobEmptyCassetteInfoDelete(objCommon, cassetteIDList);
        }
        //	Step26 - controlJob_emptyCassetteInfo_Delete, Remove empty cassette from Control Job if eqp configuration allow

        //	Step27 - cassette_multiLotType_Update, Update MultiLotType of Carrier
        for (ObjectIdentifier cassetteID : cassetteIDList) {
            try {
                cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
            } catch (ServiceException e) {
                boolean checkFlag = e.getCode() != retCodeConfig.getSucc().getCode() && e.getCode() !=
                        retCodeConfig.getNotFoundCassette().getCode();
                Validations.check(checkFlag, new OmCode(e.getCode(), e.getMessage()));
            }
        }
        //	Step28 - lot_waferLotHistoryPointer_Update, Update WaferLotHistoryPointer of lot
        for (Infos.PLot pLot : lotList) {
            lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, pLot.getLotID());
        }
        // Step29 - lotWaferSortEvent_Make
        //---------------------------------------
        // Prepare input parameter of lotWaferSortEvent_Make()
        //---------------------------------------
        List<Infos.WaferTransfer> tmpWaferXferList = waferXferList;
        for (Infos.WaferTransfer waferTransfer : tmpWaferXferList) {
            boolean bStringifiedObjRefFilled = false;
            List<Infos.PLot> pLotList = restructureSorterWaferTransferInfoOut.getLotList();
            for (Infos.PLot pLot : pLotList) {
                List<Infos.PWafer> pWaferList = pLot.getWaferList();
                for (Infos.PWafer pWafer : pWaferList) {
                    if (ObjectIdentifier.equalsWithValue(waferTransfer.getWaferID(), pWafer.getWaferID())) {
                        ObjectIdentifier waferID = waferTransfer.getWaferID();
                        waferID.setReferenceKey(pWafer.getWaferID().getReferenceKey());
                        bStringifiedObjRefFilled = true;
                        break;
                    }
                }
                if (bStringifiedObjRefFilled) {
                    break;
                }
            }
        }
        //---------------------------------------
        // Create Wafer Sort Event
        //---------------------------------------
        eventMethod.lotWaferSortEventMake(objCommon, TransactionIDEnum.CASSETTE_EXCHANGE_REQ.getValue(), tmpWaferXferList,
                sortJobInfo.getOpeMemo());
    }

    @Override
    public void sxWaferSlotmapChangeRpt(Infos.ObjCommon objCommon, Info.SortJobInfo params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        //---------------------------------------------------------------------------------
        // Collect participant cassettes (original/destination) from WaferTransferSequence.
        //---------------------------------------------------------------------------------
        Info.ComponentJob componentJob = params.getComponentJob();
        List<ObjectIdentifier> cassetteIDs = SorterHandler.getduplicateRemovalCarrierIDs(componentJob);
        if (log.isDebugEnabled()) {
            log.debug("cassetteIDs :{}", cassetteIDs);
        }
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            // object_lockMode_Get
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(equipmentID);
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.WAFER_SORTER_RPT.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            Long lockMode = objLockModeOut.getLockMode();
            if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                // advanced_object_Lock
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                        objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                // advanced_object_Lock
                List<String> loadCastSeq = cassetteIDs.stream().map(ObjectIdentifier::getValue).collect(Collectors.toList());
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                        (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, loadCastSeq));
            } else {
                objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
            }
        }
        //-----------------------------------
        // Lock cassette objects for update.
        //-----------------------------------
        // objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
        //---------------------------------------
        // Check input parameter for
        // wafer transfer (Sorter operation)
        //---------------------------------------

        sorterNewMethod.sorterWaferTransferInfoVerify(objCommon, params, SorterType.Action.AdjustByMES.getValue());
        //---------------------------------------
        // Check input parameter and
        // Server data condition
        //---------------------------------------
        sorterNewMethod.cassetteCheckConditionForWaferSort(objCommon, params);

        //------------------------------------------------------------
        // Retrieve Equipment's onlineMode
        //------------------------------------------------------------
        if (!ObjectIdentifier.isEmpty(params.getEquipmentID())) {
            equipmentMethod.equipmentOnlineModeGet(objCommon, params.getEquipmentID());
        }

        com.fa.cim.sorter.Results.ObjSorterWaferTransferInfoRestructureOut restructureSorterWaferTransferInfoOut =
                sorterNewMethod.sorterWaferTransferInfoRestructure(objCommon, params);
        //------------------------------------------------------------
        //   Reject if request lot is backup processing.
        //------------------------------------------------------------
        int lotLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList());
        /*-----------------------------------*/
        /*   Check Backup Info               */
        /*-----------------------------------*/
        List<Infos.PLot> pLots = restructureSorterWaferTransferInfoOut.getLotList();
        for (int i = 0; i < lotLen; i++) {
            Infos.LotBackupInfo lotBackupInfoOut = lotMethod.lotBackupInfoGet(objCommon, pLots.get(i).getLotID());
            if (CimBooleanUtils.isFalse(lotBackupInfoOut.getCurrentLocationFlag())
                    || CimBooleanUtils.isTrue(lotBackupInfoOut.getTransferFlag())) {
                Validations.check(retCodeConfig.getLotInOthersite());
            }
        }
        //---------------------------------------
        // At first, all relation between
        // carrier-wafer should be canceled
        // If bNotifyToTCS is true, Only Check Logic
        // works inside of wafer_materialContainer_Change()
        // or lot_materialContainer_Change()
        //---------------------------------------
        int nILen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotInventoryStateList());
        log.info("nILen : {}", nILen);
        for (int i = 0; i < nILen; i++) {
            int nJLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList());
            for (int j = 0; j < nJLen; j++) {
                Infos.PWafer pWafer = restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j);
                Infos.Wafer strWafer = new Infos.Wafer();
                strWafer.setWaferID(pWafer.getWaferID());
                strWafer.setSlotNumber(pWafer.getSlotNumber());
                waferMethod.waferMaterialContainerChange(objCommon, null, strWafer);
            }
        }
        //---------------------------------------
        // Next, new relation between
        // carrier-wafer should be created
        //---------------------------------------
        nILen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotInventoryStateList());
        for (int i = 0; i < nILen; i++) {
            int nJLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList());
            for (int j = 0; j < nJLen; j++) {
                Infos.PWafer pWafer = restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j);
                Infos.Wafer strWafer = new Infos.Wafer();
                strWafer.setWaferID(pWafer.getWaferID());
                strWafer.setSlotNumber(pWafer.getSlotNumber());
                waferMethod.waferMaterialContainerChange(objCommon, restructureSorterWaferTransferInfoOut.getCassetteIDList().get(i), strWafer);
            }
        }
        //---------------------------------------
        // Collect Cassette IDs of input parameter
        //---------------------------------------

        //---------------------------------------
        // Update Machine/ControlJob related information
        //---------------------------------------
        controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDs);
        //---------------------------------------
        // Update Carrier Multi Lot Type
        //---------------------------------------
        for (ObjectIdentifier cassetteID : cassetteIDs) {
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
        }

        //---------------------------------------
        // Update WaferLotHistoryPointer of Lot
        //---------------------------------------
        // lot_waferLotHistoryPointer_Update
        for (int i = 0; i < lotLen; i++) {
            lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, pLots.get(i).getLotID());
        }
        //---------------------------------------
        // Prepare input parameter of lotWaferSortEvent_Make()
        //---------------------------------------
        for (Info.WaferSorterSlotMap waferSorterSlotMap : componentJob.getWaferList()) {
            boolean bStringifiedObjRefFilled = false;
            for (Infos.PLot pLot : pLots) {
                List<Infos.PWafer> waferList = pLot.getWaferList();
                for (Infos.PWafer pWafer : waferList) {
                    if (ObjectIdentifier.equalsWithValue(waferSorterSlotMap.getWaferID(), pWafer.getWaferID())) {
                        waferSorterSlotMap.getWaferID().setReferenceKey(pWafer.getWaferID().getReferenceKey());
                        bStringifiedObjRefFilled = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isTrue(bStringifiedObjRefFilled)) {
                    break;
                }
            }
        }

        //---------------------------------------
        // Create Wafer Sort Event
        //---------------------------------------
        // lotWaferSortEvent_Make

        List<Infos.WaferTransfer> waferXferList = new ArrayList<>();
        for (Info.WaferSorterSlotMap waferSorterSlotMap : componentJob.getWaferList()) {
            Infos.WaferTransfer waferTransfer = getWaferTransfer(componentJob, waferSorterSlotMap);
            waferXferList.add(waferTransfer);
        }
        eventMethod.lotWaferSortEventMake(objCommon, TransactionIDEnum.WAFER_SORTER_RPT.getValue(), waferXferList, params.getOpeMemo());
    }

    @Override
    public Info.WaferSorterCompareCassette sxOnlineSorterSlotmapCompareReq(Infos.ObjCommon objCommon, Params.SJListInqParams params) {
        if (log.isDebugEnabled()) {
            log.debug("get Recently History");
        }
        Info.SortJobInfo sortJobInfo = sorterNewMethod.getRecentlyHistory(objCommon, params, SorterType.Action.T7CodeRead.getValue());
        Validations.check(sortJobInfo == null, retCodeConfigEx.getNotFoundWaferIdRead());

        if (log.isDebugEnabled()) {
            log.debug("get input params");
        }
        Info.ComponentJob componentJob = sortJobInfo.getComponentJob();
        ObjectIdentifier destinationCassetteID = componentJob.getDestinationCassetteID();
        ObjectIdentifier destinationPortID = componentJob.getDestinationPortID();

        if (log.isDebugEnabled()) {
            log.debug("Check Transaction ID and equipment Category combination.");
        }
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, sortJobInfo.getEquipmentID());

        if (log.isDebugEnabled()) {
            log.debug("check that the actionCode can only be WaferIDRead");
        }
        Validations.check(!CimStringUtils.equals(SorterType.Action.T7CodeRead.getValue(), componentJob.getActionCode()),
                retCodeConfigEx.getNotFoundWaferIdRead());

        Long maxWaferLen = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getLongValue();
        Info.WaferSorterCompareCassette waferSorterCompareCassette = new Info.WaferSorterCompareCassette();
        if (ObjectIdentifier.isNotEmptyWithValue(destinationCassetteID)) {
            waferSorterCompareCassette.setCassetteID(destinationCassetteID);
            waferSorterCompareCassette.setPortID(destinationPortID);
        }

//        if (log.isDebugEnabled()) {
//            log.debug("Verify that wafer belongs to the same lot");
//        }
//        long waferLot = Optional.ofNullable(componentJob.getWaferList()).orElseGet(Collections::emptyList).stream()
//                .map(data -> waferMethod.waferLotGet(objCommon, data.getWaferID()))
//                .distinct()
//                .count();
//        Validations.check(waferLot > 1, retCodeConfigEx.getWaferNotBelongLot());

        if (log.isDebugEnabled()) {
            log.debug("SET TCS'S SLOTMAP INFORMATION & GET destCastID");
        }
        List<Info.WaferSorterCompareSlotMap> strWaferSorterCompareSlotMapSequence = new ArrayList<>();
        Optional.ofNullable(componentJob.getWaferList()).orElseGet(Collections::emptyList).stream().forEach(slotMap -> {
            long destPosition = CimNumberUtils.longValue(slotMap.getDestinationSlotNumber());
            Validations.check(destPosition <= 0 || destPosition > maxWaferLen, retCodeConfigEx.getInvalidTcsResult());
            Info.WaferSorterCompareSlotMap waferSorterCompareSlotMap = new Info.WaferSorterCompareSlotMap();
            waferSorterCompareSlotMap.setTcsDestinationLotID(slotMap.getLotID());
            waferSorterCompareSlotMap.setTcsDestinationPosition(slotMap.getDestinationSlotNumber());
            waferSorterCompareSlotMap.setTcsDestinationWaferID(slotMap.getWaferID());
            waferSorterCompareSlotMap.setTcsDestinationAliasName(slotMap.getAliasName());
            strWaferSorterCompareSlotMapSequence.add(waferSorterCompareSlotMap);
        });
        waferSorterCompareCassette.setStrWaferSorterCompareSlotMapSequence(strWaferSorterCompareSlotMapSequence);

        if (log.isDebugEnabled()) {
            log.debug("Read a Slot Map (MM)");
        }
        List<Infos.WaferMapInCassetteInfo> cassetteGetWaferMapOutRetCode = null;
        try {
            cassetteGetWaferMapOutRetCode = cassetteMethod.cassetteGetWaferMapDR(objCommon, waferSorterCompareCassette.getCassetteID());
        } catch (ServiceException exception) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(), exception.getCode())) {
                if (log.isErrorEnabled()) {
                    log.error("throw Exception: {}", exception);
                }
                throw exception;
            }
        }
        Optional.ofNullable(cassetteGetWaferMapOutRetCode).orElseGet(Collections::emptyList).stream().forEach(data -> {
            long slotNumber = CimNumberUtils.longValue(data.getSlotNumber());
            Validations.check(slotNumber <= 0 || slotNumber > maxWaferLen, retCodeConfigEx.getInvalidMMResult());
            waferSorterCompareCassette.getStrWaferSorterCompareSlotMapSequence().stream().filter(slotMap ->
                    CimStringUtils.equals(ObjectIdentifier.fetchValue(data.getWaferID()), ObjectIdentifier.fetchValue(slotMap.getTcsDestinationWaferID()))
            ).forEach(slotMap -> {
                slotMap.setMmDestinationPosition(data.getSlotNumber());
                slotMap.setMmDestinationLotID(data.getLotID());
                slotMap.setMmDestinationWaferID(data.getWaferID());
                slotMap.setMmDestinationAliasName(data.getAliasWaferName());
            });
        });

        if (log.isDebugEnabled()) {
            log.debug("compare mm/tcs");
        }
        Optional.ofNullable(waferSorterCompareCassette.getStrWaferSorterCompareSlotMapSequence())
                .orElseGet(Collections::emptyList)
                .stream()
                .forEach(data -> {
                    ObjectIdentifier mmDestWaferID = data.getMmDestinationWaferID();
                    String mmDestAliasName = data.getMmDestinationAliasName();
                    long mmDestPosition = CimNumberUtils.longValue(data.getMmDestinationPosition());
                    ObjectIdentifier tcsDestWaferID = data.getTcsDestinationWaferID();
                    String tcsDestAliasName = data.getTcsDestinationAliasName();
                    long tcsDestPosition = CimNumberUtils.longValue(data.getTcsDestinationPosition());
                    if (ObjectIdentifier.isEmpty(mmDestWaferID) && CimStringUtils.isEmpty(mmDestAliasName)
                            && ObjectIdentifier.isEmpty(tcsDestWaferID) && CimStringUtils.isEmpty(tcsDestAliasName)) {
                        data.setCompareStatus(SorterType.CompareResult.Match.getValue());
                    } else if (ObjectIdentifier.equalsWithValue(mmDestWaferID, tcsDestWaferID)
                            && CimStringUtils.equals(mmDestAliasName, tcsDestAliasName)) {
                        if (mmDestPosition == tcsDestPosition) {
                            data.setCompareStatus(SorterType.CompareResult.Match.getValue());
                        } else {
                            data.setCompareStatus(SorterType.CompareResult.UnMatch.getValue());
                        }
                    } else {
                        data.setCompareStatus(SorterType.CompareResult.UnMatch.getValue());
                    }
                    if (ObjectIdentifier.isNotEmpty(tcsDestWaferID)) {
                        //【step6】 - wafer_lot_Get EAP不报Wafer,故无此段逻辑
                        try {
                            ObjectIdentifier waferLotOut = waferMethod.waferLotGet(objCommon, tcsDestWaferID);
                        } catch (ServiceException e) {
                            data.setCompareStatus(SorterType.CompareResult.Unknown.getValue());
                        }
                    }
                });

        return waferSorterCompareCassette;
    }


    @Override
    public String sxSJCreateReq(Infos.ObjCommon objCommon, Params.SJCreateReqParams params) {
        //------------------------------------------------------------------
        //  Object Lock
        //------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Object Lock");
        }
        Outputs.ObjLockModeOut strObjectLockModeGetOut = null;

        int lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE.intValue();

        if (!StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()) {
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(params.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);
            strObjectLockModeGetOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = strObjectLockModeGetOut.getLockMode().intValue();
        }
        if (null != strObjectLockModeGetOut && lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE){
            if (log.isDebugEnabled()) {
                log.debug("Lock Equipment Main Object");
            }
            Inputs.ObjAdvanceLockIn strAdvancedobjectLockIn = new Inputs.ObjAdvanceLockIn();
            strAdvancedobjectLockIn.setObjectID(params.getEquipmentID());
            strAdvancedobjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjectLockIn.setLockType(strObjectLockModeGetOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjectLockIn);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Object Lock for Equipment");
            }
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }
        //-------------------------------------------------------------------
        // Call equipment_portInfo_Get
        //-------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("get port by equipment");
        }
        Infos.EqpPortInfo strEquipmentPortInfoGetOut = equipmentMethod.equipmentPortInfoGet(objCommon, params.getEquipmentID());
        List<Infos.EqpPortStatus> eqpPortStatuses = strEquipmentPortInfoGetOut.getEqpPortStatuses();
        Validations.check(CimArrayUtils.isEmpty(eqpPortStatuses), retCodeConfig.getNotFoundPort());

        if (log.isDebugEnabled()) {
            log.debug("lock port");
        }
        for (Infos.EqpPortStatus eqpPortStatus : strEquipmentPortInfoGetOut.getEqpPortStatuses()) {
            if (CimStringUtils.equals(eqpPortStatus.getPortGroup(), params.getPortGroupID())) {
                objectLockMethod.objectLockForEquipmentResource(objCommon, params.getEquipmentID(),
                        eqpPortStatus.getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        }
        //--------------------------------------------------------
        //  Object Lock for Cassette
        //--------------------------------------------------------
        Set<ObjectIdentifier> sortCassetteIDs = new HashSet<>();
        Set<ObjectIdentifier> sortLotIDs = new HashSet<>();
        if (log.isDebugEnabled()) {
            log.debug("get all carrier and lot");
        }
        if (CimArrayUtils.isNotEmpty(params.getStrSorterComponentJobListAttributesSequence())) {
            List<Info.ComponentJob> componentJobList = params.getStrSorterComponentJobListAttributesSequence();
            for (Info.ComponentJob componentJob : componentJobList) {
                if (!SorterHandler.containsFOSB(componentJob.getOriginalCassetteID())) {
                    sortCassetteIDs.add(componentJob.getOriginalCassetteID());
                }
                if (!SorterHandler.containsFOSB(componentJob.getDestinationCassetteID())) {
                    sortCassetteIDs.add(componentJob.getDestinationCassetteID());
                }
            }

            for (Info.ComponentJob componentJob : componentJobList) {
                if (CimArrayUtils.isNotEmpty(componentJob.getWaferList())) {
                    for (Info.WaferSorterSlotMap waferSorterSlotMap : componentJob.getWaferList()) {
                        sortLotIDs.add(waferSorterSlotMap.getLotID());
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Object Lock for Cassette");
        }
        if(CimArrayUtils.isNotEmpty(sortCassetteIDs)){
            for (ObjectIdentifier carrierID : sortCassetteIDs) {
                objectLockMethod.objectLock(objCommon, CimCassette.class, carrierID);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Object Lock for Lot");
        }
        if (CimArrayUtils.isNotEmpty(sortLotIDs)) {
            for (ObjectIdentifier sortLotID : sortLotIDs) {
                objectLockMethod.objectLock(objCommon, CimLot.class, sortLotID);
            }
        }

        //check contamination
        ObjectIdentifier controlJobID = params.getControlJobID();
        boolean onRoute = true;
        if (ObjectIdentifier.isEmpty(controlJobID)){
            onRoute = false;
        }
        List<Info.ComponentJob> strSorterComponentJobListAttributesSequence = params.getStrSorterComponentJobListAttributesSequence();
        String productOrderId = "";
        if (!CimObjectUtils.isEmpty(params.getPostAct())){
            productOrderId = params.getPostAct().getProductOrderId();
        }
            for (Info.ComponentJob componentJob : strSorterComponentJobListAttributesSequence){
                if(!componentJob.getActionCode().equals(SorterType.Action.RFIDWrite.getValue())
                        && !componentJob.getActionCode().equals(SorterType.Action.RFIDRead.getValue())){
                    if (CimStringUtils.equals(componentJob.getActionCode(),SorterType.Action.WaferStart.getValue())){
                        onRoute = true;
                    }
                    if ((StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOn())){
                        //qiandao mode
                        //check exchange first
                        List<Info.WaferSorterSlotMap> waferSorterSlotMapList = componentJob.getWaferList();
                        for (Info.WaferSorterSlotMap waferSorterSlotMap : waferSorterSlotMapList) {
                            contaminationMethod.carrierProductUsageTypeCheck(ObjectIdentifier.emptyIdentifier(),
                                    waferSorterSlotMap.getLotID(),componentJob.getDestinationCassetteID());
                            contaminationMethod.carrierExchangeCheckQiandaoMode(objCommon,
                                    componentJob.getDestinationCassetteID(), waferSorterSlotMap.getLotID(),componentJob.getActionCode());
                        }
                        //check sorter
                        if (onRoute){
                            contaminationMethod.contaminationSorterCheckForQiandao(componentJob,productOrderId,params.getEquipmentID());
                        }else {
                            for (Info.WaferSorterSlotMap waferSorterSlotMap : waferSorterSlotMapList) {
                                Infos.WaferTransfer waferTransfer = getWaferTransfer(componentJob, waferSorterSlotMap);
                                contaminationMethod.lotOffRouteSorterCheckForQiandao(objCommon,
                                        waferTransfer);
                            }
                        }
                    } else if (StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOff()) {
                        List<Info.WaferSorterSlotMap> waferSorterSlotMapList = componentJob.getWaferList();
                        for (Info.WaferSorterSlotMap waferSorterSlotMap : waferSorterSlotMapList) {
                            contaminationMethod.carrierProductUsageTypeCheck(ObjectIdentifier.emptyIdentifier(),
                                    waferSorterSlotMap.getLotID(),componentJob.getDestinationCassetteID());
                            Infos.WaferTransfer waferTransfer = getWaferTransfer(componentJob, waferSorterSlotMap);
                            contaminationMethod.lotWaferCarrierExchangeChangeCheck(objCommon,
                                    waferTransfer,componentJob.getActionCode());
                        }
                        //OMS mode
                        contaminationMethod.contaminationSorterCheck(componentJob,productOrderId,params.getEquipmentID(),controlJobID);
                    }
                }
            }




        //------------------------------------------------------------------
        //  Check the validity of Sorter Job Information.
        //------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Check the validity of Sorter Job Information");
        }
        String operationMode = null;
        for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses) {
            if (CimStringUtils.equals(eqpPortStatus.getPortGroup(), params.getPortGroupID())
                    && (ObjectIdentifier.equalsWithValue(eqpPortStatus.getOperationModeID(),
                    SorterType.OperationMode.Semi_1.getValue())
                    || ObjectIdentifier.equalsWithValue(eqpPortStatus.getOperationModeID(),
                    SorterType.OperationMode.Auto_1.getValue())
                    || ObjectIdentifier.equalsWithValue(eqpPortStatus.getOperationModeID(),
                    SorterType.OperationMode.Auto_2.getValue()))) {
                operationMode = ObjectIdentifier.fetchValue(eqpPortStatus.getOperationModeID());
                break;
            }
        }
        Validations.check(null == operationMode, retCodeConfigEx.getInvalidPortOperationMode());

        //------------------------------------------------------------------
        // Call sorter_CheckConditionForJobCreate
        //------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("create sort job check");
        }
        sorterNewMethod.sorterCheckConditionForJobCreate(objCommon, params.getStrSorterComponentJobListAttributesSequence(),
                params.getEquipmentID(), params.getPortGroupID(), params.getControlJobID());


        //Combine --> 进行合批检查   Separate --> 进行分批
        if (log.isDebugEnabled()) {
            log.debug("check Combine and Separate");
        }
        String actionCode = params.getStrSorterComponentJobListAttributesSequence().get(0).getActionCode();
        if (log.isTraceEnabled()) {
            log.trace("actionCode,{}",actionCode);
        }
        if (SorterType.Action.Combine.getValue().equals(actionCode)) {
            com.fa.cim.dto.Params.MergeLotReqParams mergeLotReqParams = new com.fa.cim.dto.Params.MergeLotReqParams();
            mergeLotReqParams.setChildLotID(params.getPostAct().getChildLotId());
            mergeLotReqParams.setParentLotID(params.getPostAct().getParentLotId());
            mergeLotReqParams.setClaimMemo(params.getClaimMemo());
            lotService.lotMergeCheck(objCommon, mergeLotReqParams, false);
        } else if (SorterType.Action.Separate.getValue().equals(actionCode)) {
            Info.ComponentJob componentJob = params.getStrSorterComponentJobListAttributesSequence().get(0);
            List<Info.WaferSorterSlotMap> waferList = componentJob.getWaferList();
            com.fa.cim.dto.Params.SplitLotReqParams splitLotReqParams = new com.fa.cim.dto.Params.SplitLotReqParams();
            List<ObjectIdentifier> waferIDs = new ArrayList<>();
            if (CimArrayUtils.isNotEmpty(waferList)) {
                for (Info.WaferSorterSlotMap slotMap : waferList) {
                    waferIDs.add(slotMap.getWaferID());
                    splitLotReqParams.setParentLotID(slotMap.getLotID());
                }
            }
            splitLotReqParams.setChildWaferIDs(waferIDs);
            splitLotReqParams.setBranchingRouteSpecifyFlag(false);
            splitLotReqParams.setFutureMergeFlag(false);
            lotService.lotSplitCheck(objCommon, splitLotReqParams);
        } else if (SorterType.Action.WaferStart.getValue().equals(actionCode)) {
            List<Info.SortJobListAttributes> jobListGetDROut = new ArrayList<>();
            Info.SortJobListAttributes sortJobListAttributes = new Info.SortJobListAttributes();
            List<Info.SorterComponentJobListAttributes> jobListAttributes = new ArrayList<>();
            for (Info.ComponentJob componentJob : params.getStrSorterComponentJobListAttributesSequence()) {
                Info.SorterComponentJobListAttributes cjAttributes = new Info.SorterComponentJobListAttributes();
                cjAttributes.setDestinationCarrierID(componentJob.getDestinationCassetteID());
                cjAttributes.setActionCode(componentJob.getActionCode());
                cjAttributes.setWaferSorterSlotMapList(componentJob.getWaferList());
                jobListAttributes.add(cjAttributes);
                sortJobListAttributes.setSorterComponentJobListAttributesList(jobListAttributes);
            }
            jobListGetDROut.add(sortJobListAttributes);
            ObjectIdentifier productRequestID = ObjectIdentifier.buildWithValue(params.getPostAct().getProductOrderId());
            Infos.NewLotAttributes newLotAttributes = SorterHandler.makeNewlotAttributes(productRequestID, jobListGetDROut);
            lotStartService.lotStartCheck(objCommon, productRequestID, newLotAttributes, params.getClaimMemo());
        }

        //------------------------------------------------------------------
        //  Create Sorter Job Information
        //------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("create sort job");
        }
        params.setOperationMode(operationMode);
        Params.SJCreateReqParams jobListAttributes = sorterNewMethod.sorterJobCreate(objCommon, params);

        //如果是非Semi_1模式，需要向EAP发送消息
        if (!SorterType.OperationMode.Semi_1.getValue().equals(operationMode)) {
            if (log.isDebugEnabled()) {
                log.debug("send EAP message");
            }
            Params.SorterActionInqParams inqParams = new Params.SorterActionInqParams();
            inqParams.setUser(params.getUser());
            inqParams.setEquipmentID(jobListAttributes.getEquipmentID());
            inqParams.setCassetteID(jobListAttributes.getStrSorterComponentJobListAttributesSequence().get(0).getDestinationCassetteID());
            Info.SortJobInfo sortJobInfo = sortInqService.sorterActionInq(objCommon, inqParams, SorterType.Status.Created.getValue());
            this.sendEap(objCommon, sortJobInfo, operationMode);
        }

        if (log.isDebugEnabled()) {
            log.debug("create event");
        }
        Params.ObjSorterSorterJobEventMakeIn eventMakeIn = new Params.ObjSorterSorterJobEventMakeIn();
        jobListAttributes.setOperation(SorterType.JobOperation.SortJobCreate.getValue());
        for (Info.ComponentJob componentJob : jobListAttributes.getStrSorterComponentJobListAttributesSequence()) {
            componentJob.setOperation(SorterType.JobOperation.ComponentJobCreate.getValue());
            if (CimArrayUtils.isNotEmpty(componentJob.getWaferList())) {
                for (Info.WaferSorterSlotMap slotMap : componentJob.getWaferList()) {
                    slotMap.setDirection(SorterType.JobDirection.OMS.getValue());
                }
            }
        }
        eventMakeIn.setActionCode(jobListAttributes.getStrSorterComponentJobListAttributesSequence().get(0).getActionCode());
        eventMakeIn.setStrSortJobListAttributes(jobListAttributes);
        eventMakeIn.setTransactionID(objCommon.getTransactionID());
        eventMethod.sorterSorterJobEventMakeNew(objCommon, eventMakeIn);
        return ObjectIdentifier.fetchValue(jobListAttributes.getSorterJobID());
    }


    private void holdLotAndMultiDurableStatusChangeReq(Infos.ObjCommon objCommon,
                Info.SortJobInfo sortJobInfo, Set<ObjectIdentifier> needHoldSets,List<Info.LotAliasName> list) {
        Info.ComponentJob componentJob = sortJobInfo.getComponentJob();
        multiDurableStatusChange(objCommon, componentJob);
        String memo = "Duplicated wafer alias name exist in ";
        StringBuilder tempLotID= new StringBuilder();
        int size = CimArrayUtils.getSize(list);
        if (needHoldSets.size() > 0) {
            //onroute用running hold
            if (!ObjectIdentifier.isEmpty(sortJobInfo.getControlJobID())) {
                for (ObjectIdentifier lotID : needHoldSets) {
                    if (size > 0) {
                        for (Info.LotAliasName lotAliasName : list) {
                            if (ObjectIdentifier.equalsWithValue(lotID, lotAliasName.getReservaLotID())) {
                                tempLotID.append(ObjectIdentifier.fetchValue(lotAliasName.getRelationLotID())).append(",");
                                break;
                            }
                        }
                    }
                }
                if (!StringUtils.isEmpty(tempLotID.toString())) {
                    tempLotID = new StringBuilder(tempLotID.substring(0, tempLotID.length() - 1));
                    memo = memo + tempLotID;
                } else {
                    memo = "";
                }
                sxRunningHoldReq(objCommon, sortJobInfo.getEquipmentID(), sortJobInfo.getControlJobID(),
                        ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_RUNNINGHOLD), memo);
            } else {
                for (ObjectIdentifier lotID : needHoldSets) {
                    if (size > 0) {
                        for (Info.LotAliasName lotAliasName : list) {
                            if (ObjectIdentifier.equalsWithValue(lotID, lotAliasName.getReservaLotID())) {
                                memo = memo + ObjectIdentifier.fetchValue(lotAliasName.getRelationLotID());
                                holdLot(objCommon, lotID, componentJob.getResultCode(), memo);
                                break;
                            }
                        }
                    } else {
                        holdLot(objCommon, lotID, componentJob.getResultCode(), "");
                    }
                }
            }
        }
    }

    private void multiDurableStatusChange(Infos.ObjCommon objCommon, Info.ComponentJob componentJob) {
        Set<ObjectIdentifier> carrierSets = new HashSet<>();
        if (!SorterHandler.containsFOSB(componentJob.getDestinationCassetteID())) {
            carrierSets.add(componentJob.getDestinationCassetteID());
        }
        if (!SorterHandler.containsFOSB(componentJob.getOriginalCassetteID())) {
            carrierSets.add(componentJob.getOriginalCassetteID());
        }
        for (ObjectIdentifier tcsCassetteID : carrierSets) {
            Outputs.ObjCassetteStatusOut strCassetteGetStatusDROut = cassetteMethod
                    .cassetteGetStatusDR(objCommon, tcsCassetteID);

            if (CimStringUtils.equals(strCassetteGetStatusDROut.getDurableState(), CIMStateConst.CIM_DURABLE_NOTAVAILABLE)) {
                if (log.isDebugEnabled()) {
                    log.debug("Cassette Status == Not Available");
                }
                continue;
            }
            // MKAE A CASSETTE NOT AVAILABLE,sxMultiDurableStatusChangeReq 还是sxCarrierStatusChangeRpt待定
          //  durableService.sxCarrierStatusChangeRpt(objCommon,tcsCassetteID,CIMStateConst.CIM_DURABLE_NOTAVAILABLE,"");
            com.fa.cim.dto.Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams = new com.fa.cim.dto.Params.CarrierDetailInfoInqParams();
            carrierDetailInfoInqParams.setCassetteID(tcsCassetteID);
            carrierDetailInfoInqParams.setDurableOperationInfoFlag(false);
            carrierDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
            carrierDetailInfoInqParams.setUser(objCommon.getUser());
            Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult = bankInqService.sxCarrierDetailInfoInq(objCommon, carrierDetailInfoInqParams);
            Infos.CassetteStatusInfo cassetteStatusInfo = carrierDetailInfoInqResult.getCassetteStatusInfo();

            com.fa.cim.dto.Params.MultiDurableStatusChangeReqParams multiDurableStatusChangeReqParams = new com.fa.cim.dto.Params.MultiDurableStatusChangeReqParams();
            Infos.MultiDurableStatusChangeReqInParm multiDurableStatusChangeReqInParm = new Infos.MultiDurableStatusChangeReqInParm();
            multiDurableStatusChangeReqInParm.setDurableStatus(CIMStateConst.CIM_DURABLE_NOTAVAILABLE);
            //CIMFW_DURABLE_SUB_STATE_DEFAULT
            multiDurableStatusChangeReqInParm.setDurableSubStatus(ObjectIdentifier.buildWithValue(BizConstant.CIMFW_DURABLE_SUB_STATE_SORTER));
            multiDurableStatusChangeReqInParm.setDurableCategory(BizConstant.SP_DURABLECAT_CASSETTE);
            //组装当前DurableSubStatus
            List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos = new ArrayList<>();
            Infos.StatusChangeDurableInfo statusChangeDurableInfo = new Infos.StatusChangeDurableInfo();
            statusChangeDurableInfo.setDurableID(tcsCassetteID);
            statusChangeDurableInfo.setDurableStatus(cassetteStatusInfo.getCassetteStatus());
            statusChangeDurableInfo.setDurableSubStatus(cassetteStatusInfo.getDurableSubStatus());
            statusChangeDurableInfos.add(statusChangeDurableInfo);
            multiDurableStatusChangeReqInParm.setStatusChangeDurableInfos(statusChangeDurableInfos);
            multiDurableStatusChangeReqParams.setParm(multiDurableStatusChangeReqInParm);
            durableService.sxMultiDurableStatusChangeReq(objCommon, multiDurableStatusChangeReqParams);
        }
    }

    private void sxRunningHoldReq(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID,
                                  ObjectIdentifier controlJobId, ObjectIdentifier holdReasonCodeId, String claimMemo){
        equipmentService.sxRunningHoldReq(objCommon, equipmentID, controlJobId,holdReasonCodeId, claimMemo);
    }

    private void holdLot(Infos.ObjCommon objCommon, ObjectIdentifier lotID, int errorCode,String memo) {
        com.fa.cim.dto.Params.LotInfoInqParams lotInfoInqParams = new com.fa.cim.dto.Params.LotInfoInqParams();
        lotInfoInqParams.setLotIDs(CimArrayUtils.generateList(lotID));
        Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
        lotInfoInqFlag.setLotOperationInfoFlag(true);
        lotInfoInqFlag.setLotBasicInfoFlag(true);
        lotInfoInqFlag.setLotBasicInfoFlag(true);
        lotInfoInqFlag.setLotOperationInfoFlag(true);
        lotInfoInqFlag.setLotProductInfoFlag(true);
        lotInfoInqFlag.setLotListInCassetteInfoFlag(true);
        lotInfoInqFlag.setLotWaferMapInCassetteInfoFlag(true);
        lotInfoInqFlag.setLotWaferAttributesFlag(true);
        lotInfoInqParams.setLotInfoInqFlag(lotInfoInqFlag);
        Results.LotInfoInqResult lotInfoInqResult = lotInqService.sxLotInfoInq(objCommon, lotInfoInqParams);
        Validations.check(lotInfoInqResult == null || CimArrayUtils.getSize(lotInfoInqResult
                .getLotInfoList()) != 1, retCodeConfig.getInvalidParameter());
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldType(BizConstant.SP_REASONCAT_LOTHOLD);

        List<Infos.ReasonCodeAttributes> reasonCodeAttributes = systemInqService
                .sxReasonCodeListByCategoryInq(objCommon, BizConstant.CATEGORY_HOLD_LOT);
        Validations.check(CimArrayUtils.isEmpty(reasonCodeAttributes), retCodeConfig.getNotFoundSystemObj());
        String reasonCode;

        switch (errorCode) {
            case 1:
                reasonCode = BizConstant.SP_REASON_WAITINGTRANSFERERRORONHOLD;
                break;
            case 2:
                reasonCode = BizConstant.SP_REASON_WAITINGTRANSFERFAILONHOLD;
                break;
            case 3:
                reasonCode = BizConstant.SP_REASON_WAITINGTRANSFERPARTIALFAILONHOLD;
                break;
            default:
                reasonCode = BizConstant.SP_REASON_WAITINGTRANSFERFAILONHOLD;
                break;
        }

        String finalReasonCode = reasonCode;
        List<ObjectIdentifier> reasonCodes = reasonCodeAttributes.stream().filter(
                reasonCodeAttribute -> CimStringUtils.equals(reasonCodeAttribute.getReasonCodeID(), finalReasonCode)).map(
                reasonCodeAttribute -> reasonCodeAttribute.getReasonCodeID()).collect(Collectors.toList());
        Validations.check(CimArrayUtils.isEmpty(reasonCodes), retCodeConfig.getNotFoundSystemObj());

        lotHoldReq.setHoldReasonCodeID(reasonCodes.get(0));

        lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
        lotHoldReq.setOperationNumber(lotOperationInfo.getOperationNumber());
        lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
        lotHoldReq.setRouteID(lotOperationInfo.getRouteID());
        lotHoldReq.setClaimMemo(memo);
        List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
        holdReqList.add(lotHoldReq);
        lotService.sxHoldLotReq(objCommon, lotID, holdReqList);
    }

    private void lotStartAction(Infos.ObjCommon objCommon, List<Info.SortJobListAttributes> jobListGetDROut, String opeMemo) {
        if (log.isDebugEnabled()) {
            log.debug("lotStart");
        }
        Info.SortJobPostAct postAct = jobListGetDROut.get(0).getPostAct();
        ObjectIdentifier productRequestID = ObjectIdentifier.buildWithValue(postAct.getProductOrderId());
        //----------------------------------------------------------
        // lotStart
        //---------------------------------------------------------

        Infos.NewLotAttributes newLotAttributes = SorterHandler.makeNewlotAttributes(productRequestID, jobListGetDROut);
        Results.WaferLotStartReqResult waferLotStartReqResult = lotStartService.sxWaferLotStartReq(objCommon,
                productRequestID, newLotAttributes, opeMemo);
        ObjectIdentifier lotID = waferLotStartReqResult.getLotID();


        //----------------------------------------------------------
        // 补齐AliasNames
        //---------------------------------------------------------

        Results.LotListByCarrierInqResult lotListByCarrierInqResult = lotInqService.sxLotListByCarrierInq(objCommon,
                jobListGetDROut.get(0).getSorterComponentJobListAttributesList().get(0).getDestinationCarrierID());

        com.fa.cim.dto.Params.WaferAliasSetReqParams waferAliasSetReqParams = new com.fa.cim.dto.Params.WaferAliasSetReqParams();
        waferAliasSetReqParams.setLotID(lotID);
        waferAliasSetReqParams.setClaimMemo(opeMemo);
        List<Infos.AliasWaferName> aliasWaferNames = new ArrayList<>();
        waferAliasSetReqParams.setAliasWaferNames(aliasWaferNames);

        Optional.ofNullable(lotListByCarrierInqResult.getWaferMapInCassetteInfoList())
                .ifPresent(waferMapInCassetteInfos -> waferMapInCassetteInfos.forEach(waferMapInCassetteInfo -> {
                    if (ObjectIdentifier.isEmptyWithValue(waferMapInCassetteInfo.getLotID())) {
                        return;
                    }
                    Integer slotNumber = waferMapInCassetteInfo.getSlotNumber();
                    Infos.AliasWaferName aliasWaferName = new Infos.AliasWaferName();
                    aliasWaferNames.add(aliasWaferName);
                    aliasWaferName.setWaferID(waferMapInCassetteInfo.getWaferID());
                    for (Infos.NewWaferAttributes attributes : newLotAttributes.getNewWaferAttributesList()) {
                        if (CimNumberUtils.intValue(slotNumber) == CimNumberUtils.intValue(attributes.getNewSlotNumber())) {
                            aliasWaferName.setAliasWaferName(attributes.getWaferAliasName());
                            break;
                        }
                    }
                }));

        lotStartService.sxWaferAliasSetReq(objCommon, waferAliasSetReqParams);

    }

    private void checkProcessingInformation(Info.SortJobInfo soruceJobInfo, Info.SortJobInfo targetJobInfo) {
        Validations.check(ObjectIdentifier.isEmpty(targetJobInfo.getSorterJobID()), retCodeConfigEx.getNotFoundSorterjob());
        Validations.check(CimObjectUtils.isEmpty(targetJobInfo.getComponentJob().getComponentJobID()),
                retCodeConfigEx.getNotFoundSorterjobComponent());
        Validations.check(!soruceJobInfo.getComponentJob().getComponentJobID().
                        equals(targetJobInfo.getComponentJob().getComponentJobID()), retCodeConfigEx.getNotFoundSorterjobComponent(),
                soruceJobInfo.getComponentJob().getComponentJobID());
    }


    private Object sendEap(Infos.ObjCommon objCommon, Info.SortJobInfo jobInfo, String operationMode) {
        Object result = null;
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        Long sleepTimeValue = 0L;
        Long retryCountValue = 0L;

        if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            sleepTimeValue = CimNumberUtils.longValue(tmpSleepTimeValue);
        }

        if (0 == CimStringUtils.length(tmpRetryCountValue)) {
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            retryCountValue = CimNumberUtils.longValue(tmpRetryCountValue);
        }

        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            log.info("{} {}", "loop to retryCountValue + 1", retryNum);
            /*--------------------------*/
            /*    Send Request to EAP   */
            /*--------------------------*/
            IEAPRemoteManager eapRemoteManager = eapMethod
                    .eapRemoteManager(objCommon, objCommon.getUser(), jobInfo.getEquipmentID(), null, true);
            if (null == eapRemoteManager) {
                log.info("MES not configure EAP host");
                break;
            }
            try {
                if (operationMode.equals(SorterType.OperationMode.Semi_1.getValue())) {
                    result = eapRemoteManager.sendSortActionStartReq(jobInfo);
                } else {
                    result = eapRemoteManager.sendSorterActionReserveReq(jobInfo);
                }
                log.info("Now EAP subSystem is alive!! Go ahead");
                break;
            } catch (CimIntegrationException ex) {
                if (Validations.isEquals((int) ex.getCode(), retCodeConfig.getExtServiceBindFail())
                        || Validations.isEquals((int) ex.getCode(), retCodeConfig.getExtServiceNilObj())
                        || Validations.isEquals((int) ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    log.info("{} {}", "EAP subsystem has return NO_RESPONSE!! just retry now!!  now count...", retryNum);
                    log.info("{} {}", "now sleeping... ", sleepTimeValue);
                    if (retryNum != retryCountValue) {
                        try {
                            Thread.sleep(sleepTimeValue);
                            continue;
                        } catch (InterruptedException e) {
                            ex.addSuppressed(e);
                            Thread.currentThread().interrupt();
                            throw ex;
                        }
                    } else {
                        Validations.check(true, retCodeConfig.getTcsNoResponse());
                    }
                } else {
                    Validations.check(true, new OmCode((int) ex.getCode(), ex.getMessage()));
                }
            }
        }
        return result;
    }

    @Override
    public void sxPostAct(Infos.ObjCommon objCommon,Info.PostActDRIn postActDrIn ) {
        com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.
                Params.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRIn.setSorterJob(postActDrIn.getSortJobID());
        List<Info.SortJobListAttributes> jobListGetDROut = sorterNewMethod
                .sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
        String actionCode = postActDrIn.getActionCode();
        if (log.isDebugEnabled()) {
            log.debug("actionCode,{}",actionCode);
        }
        if (SorterType.Action.Combine.getValue().equals(actionCode)) {
            Info.SortJobPostAct postAct = jobListGetDROut.get(0).getPostAct();
            com.fa.cim.dto.Params.MergeLotReqParams mergeLotReqParams = new com.fa.cim.dto.Params.MergeLotReqParams();
            mergeLotReqParams.setChildLotID(postAct.getChildLotId());
            mergeLotReqParams.setParentLotID(postAct.getParentLotId());
            mergeLotReqParams.setClaimMemo(postActDrIn.getOpeMemo());
            ObjectIdentifier originalCarrierID = jobListGetDROut.get(0).getSorterComponentJobListAttributesList().get(0).getOriginalCarrierID();
            this.lotService.lotMerge(objCommon, mergeLotReqParams,originalCarrierID);
        }
        //WaferStart只有n:1
        if (CimStringUtils.equals(actionCode, SorterType.Action.WaferStart.getValue())) {
            ObjectIdentifier destinationCassetteID = jobListGetDROut.get(0)
                    .getSorterComponentJobListAttributesList().get(0).getDestinationCarrierID();
            //lot start
            this.lotStartAction(objCommon, jobListGetDROut, postActDrIn.getOpeMemo());
            //判断vendor lot qty
            List<String> vendorlotIDs = sorterNewMethod.getVendorlotIDs(postActDrIn.getSortJobID());
            if (CimArrayUtils.isNotEmpty(vendorlotIDs)) {
                vendorlotIDs.forEach(vendorlotID -> {
                    Infos.DurableAttribute strDurableAttribute = new Infos.DurableAttribute();
                    String fosbID = sorterNewMethod.generateFosbID();
                    strDurableAttribute.setDurableID(ObjectIdentifier.buildWithValue(fosbID));
                    strDurableAttribute.setCategory(sorterNewMethod
                            .getCarrierCategory(ObjectIdentifier.fetchValue(destinationCassetteID)));
                    strDurableAttribute.setUsageCheckFlag(true);
                    strDurableAttribute.setCapacity(25);
                    strDurableAttribute.setNominalSize(12);
                    strDurableAttribute.setMaximumOperationStartCount(0D);
                    strDurableAttribute.setIntervalBetweenPM(0);
                    strDurableAttribute.setCarrierType(SorterType.CarrierType.FOSB.getValue());
                    if (log.isDebugEnabled()) {
                        log.debug("carrierId:{}", fosbID);
                    }
                    durableService.sxDurableSetReq(objCommon, false, BizConstant.SP_DURABLECAT_CASSETTE, strDurableAttribute, "");
                });
            }
        }

    }

    @Override
    public void sxSJCancelReq(Infos.ObjCommon objCommon, com.fa.cim.sorter.Params.SJCancelReqParm params, boolean isDelete) {
        if (log.isDebugEnabled()) {
            log.debug("step - check input param");
        }
        Validations.check(ObjectIdentifier.isEmpty(params.getJobID()) || CimStringUtils.isEmpty(params.getJobType()),
                retCodeConfigEx.getNotFoundSorterjob(), params.getJobID());

        com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRIn.setSorterJob(params.getJobID());
        List<Info.SortJobListAttributes> strSorterJobListGetDROut = sorterNewMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
        Validations.check(CimArrayUtils.isEmpty(strSorterJobListGetDROut), retCodeConfigEx.getNotFoundSorterjob());
        Info.SortJobListAttributes strSortJobListAttributes = strSorterJobListGetDROut.get(0);
        ObjectIdentifier equipmentID = strSortJobListAttributes.getEquipmentID();
        ObjectIdentifier portGroupID = strSortJobListAttributes.getPortGroupID();
        if (log.isDebugEnabled()) {
            log.debug("step - add lock");
        }
        /*String sorterJobLockFlagStr = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getValue();
        int sorterJobLockFlag = BaseStaticMethod.parseToInteger(sorterJobLockFlagStr);*/

        if (StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()) {
            if (log.isDebugEnabled()) {
                log.debug("step - Lock Sort Jobs");
            }
            objectLockMethod.objectLock(objCommon,CimSorterJob.class,params.getJobID());
        }

        Outputs.ObjLockModeOut objLockModeOut = new Outputs.ObjLockModeOut();
        int lockMode = CimNumberUtils.intValue("0");
        if (StandardProperties.OM_SORTER_LOCK_JOB_FLAG.isTrue()) {
            if (log.isDebugEnabled()) {
                log.debug("step - lock equipment");
            }
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(equipmentID);
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);
            objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode().intValue();
        }

        if (lockMode != CimNumberUtils.intValue("0")) {
            if (log.isDebugEnabled()) {
                log.debug("step - Lock Equipment Main Object");
            }

            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(strSortJobListAttributes.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

            if (!CimStringUtils.equals(SorterType.Status.Created.getValue(), strSortJobListAttributes.getSorterJobStatus())) {
                if (log.isDebugEnabled()) {
                    log.debug("get equipment port group form equipment");
                }
                Outputs.ObjEquipmentPortGroupInfoGetOut objEquipmentPortGroupInfoGetOut = portMethod
                        .equipmentPortGroupInfoGet(objCommon,equipmentID, ObjectIdentifier.fetchValue(portGroupID), null);

                if (objEquipmentPortGroupInfoGetOut != null
                        && CimArrayUtils.isNotEmpty(objEquipmentPortGroupInfoGetOut.getEqpPortAttributes())) {
                    List<Infos.EqpPortAttributes> eqpPortAttributes = objEquipmentPortGroupInfoGetOut.getEqpPortAttributes();
                    for (Infos.EqpPortAttributes eqpPortAttribute : eqpPortAttributes) {
                        objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID,
                                eqpPortAttribute.getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                        if (log.isDebugEnabled()) {
                            log.debug("lock port {}", eqpPortAttribute.getPortID());
                        }
                    }
                }
            }
        } else {
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }

        if (log.isDebugEnabled()) {
            log.debug("step - Remove duplicate for orgCarrier/dstCarrier/lot");
        }
        Set<ObjectIdentifier> lotIDs = new HashSet<>();
        List<Info.SorterComponentJobListAttributes> sorterComponentJobListAttributesList = strSortJobListAttributes
                .getSorterComponentJobListAttributesList();
        Validations.check(CimArrayUtils.isEmpty(sorterComponentJobListAttributesList), retCodeConfigEx.getNotFoundSorterjobComponent());

        List<ObjectIdentifier> orgCarrierIDs = sorterComponentJobListAttributesList
                .stream()
                .map(Info.SorterComponentJobListAttributes::getOriginalCarrierID)
                .distinct()
                .filter(orgCarrier -> !SorterHandler.containsFOSB(orgCarrier))
                .collect(Collectors.toList());
        List<ObjectIdentifier> dstCarrierIDs = sorterComponentJobListAttributesList
                .stream()
                .map(Info.SorterComponentJobListAttributes::getDestinationCarrierID)
                .distinct()
                .filter(detCarrier -> !SorterHandler.containsFOSB(detCarrier))
                .collect(Collectors.toList());
        sorterComponentJobListAttributesList.stream()
                .map(Info.SorterComponentJobListAttributes::getWaferSorterSlotMapList)
                .forEach(slotMaps -> {
                    Optional.ofNullable(slotMaps).orElseGet(Collections::emptyList).stream().forEach(slotMap -> lotIDs.add(slotMap.getLotID()));
                });


        if (log.isDebugEnabled()) {
            log.debug("step - lock carrier/lot");
        }
        orgCarrierIDs.stream().forEach(carrier -> objectLockMethod.objectLock(objCommon, CimCassette.class, carrier));
        dstCarrierIDs.stream().forEach(carrier -> objectLockMethod.objectLock(objCommon, CimCassette.class, carrier));
        lotIDs.stream().forEach(lotID -> objectLockMethod.objectLock(objCommon, CimLot.class, lotID));

        if (log.isDebugEnabled()) {
            log.debug("check current sorter job status");
        }
        String sorterJobStatus = strSortJobListAttributes.getSorterJobStatus();
        if (!CimStringUtils.equals(SorterType.Status.Error.getValue(), sorterJobStatus)
                && !CimStringUtils.equals(SorterType.Status.Completed.getValue(), sorterJobStatus)
                && !CimStringUtils.equals(SorterType.Status.Aborted.getValue(), sorterJobStatus)
                && !CimStringUtils.equals(SorterType.Status.Created.getValue(), sorterJobStatus)
                && !CimStringUtils.equals(SorterType.Status.ForceCompleted.getValue(), sorterJobStatus)) {
            Validations.check(retCodeConfigEx.getInvalidSorterJobStatus(),
                    ObjectIdentifier.fetchValue(strSortJobListAttributes.getSorterJobID()), sorterJobStatus);
        }

        if (log.isDebugEnabled()) {
            log.debug("step - Get Sort Job by the Equipment");
        }
        com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRInByTheEquipment =
                new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRInByTheEquipment.setEquipmentID(equipmentID);
        objSorterJobListGetDRInByTheEquipment.setPortGroup(ObjectIdentifier.fetchValue(portGroupID));
        List<Info.SortJobListAttributes> strSortJobListAttributesSeqByEQP = sorterNewMethod
                .sorterJobListGetDR(objCommon, objSorterJobListGetDRInByTheEquipment);

        if (CimStringUtils.isNotEmpty(strSortJobListAttributes.getCtrljobId())) {
            // on-route
            //如果是通过本接口调用过来，不管是EAP还是页面，都需要进行Move In Reserve Cancel/Move In Cancel/delete Sorter Job
            if (CimStringUtils.equals(SorterType.Status.Created.getValue(), strSortJobListAttributes.getSorterJobStatus())) {
                com.fa.cim.dto.Params.MoveInReserveCancelReqParams moveInReserveCancelReqParams = new com.fa.cim.dto.Params.MoveInReserveCancelReqParams();
                moveInReserveCancelReqParams.setUser(objCommon.getUser());
                moveInReserveCancelReqParams.setEquipmentID(strSortJobListAttributes.getEquipmentID());
                moveInReserveCancelReqParams.setControlJobID(ObjectIdentifier.buildWithValue(strSortJobListAttributes.getCtrljobId()));
                moveInReserveCancelReqParams.setAlreadySendMsgFlag(true);
                dispatchService.sxMoveInReserveCancelReqService(objCommon, moveInReserveCancelReqParams);
            }
        }

        if (isDelete){  //页面操作，删除Sorter Job
            if (log.isDebugEnabled()) {
                log.debug("step - delete sortJob/componentJob/slotMap");
            }
            Info.SorterComponentJobDeleteDRIn sorterComponentJobDeleteDRIn =
                    new Info.SorterComponentJobDeleteDRIn();
            sorterComponentJobDeleteDRIn.setSorterJobID(params.getJobID());
            sorterNewMethod.sorterJobInfoDeleteDR(objCommon, sorterComponentJobDeleteDRIn);

            if (log.isDebugEnabled()) {
                log.debug("step - Adjust order sequence of remained Sort Job");
            }
            List<ObjectIdentifier> remainSortJobIDs = new ArrayList<>();
            for (Info.SortJobListAttributes sortJobListAttributes : strSortJobListAttributesSeqByEQP) {
                if (!ObjectIdentifier.equalsWithValue(params.getJobID(), sortJobListAttributes.getSorterJobID())) {
                    remainSortJobIDs.add(sortJobListAttributes.getSorterJobID());
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("step - Change priority of remained Sort Job.");
            }
            if (CimArrayUtils.isNotEmpty(remainSortJobIDs)) {
                Info.SorterLinkedJobUpdateDRIn sorterLinkedJobUpdateDRIn = new Info.SorterLinkedJobUpdateDRIn();
                sorterLinkedJobUpdateDRIn.setJobIDs(remainSortJobIDs);
                sorterLinkedJobUpdateDRIn.setJobType(BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB);
                sorterNewMethod.sorterLinkedJobUpdateDR(objCommon, sorterLinkedJobUpdateDRIn);
            }
        }else {  //EAP调用，修改Sorter Job Status
            for (Info.SorterComponentJobListAttributes componentJob : sorterComponentJobListAttributesList) {
                this.sxAssembleStatusChg(objCommon, componentJob.getSorterComponentJobID(),
                        SorterType.JobType.ComponentJob.getValue(), SorterType.Status.Error.getValue(), true);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("step - Call TxSendSortJobCancelNotificationReq");
        }
        if (CimBooleanUtils.isTrue(params.getNotifyEAPFlag())) {
            String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
            String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
            int sleepTimeValue = 0;
            int retryCountValue = 0;

            if (CimStringUtils.isEmpty(tmpSleepTimeValue)) {
                sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS.intValue();
            } else {
                sleepTimeValue = CimNumberUtils.intValue(tmpSleepTimeValue);
            }

            if (CimStringUtils.isEmpty(tmpRetryCountValue)) {
                retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS.intValue();
            } else {
                retryCountValue = CimNumberUtils.intValue(tmpRetryCountValue);
            }

            if (log.isDebugEnabled()) {
                log.debug("step - Send Request to TCS");
            }

            Params.SJCancelRptParam sjCancelRptParam = new Params.SJCancelRptParam();
            sjCancelRptParam.setJobType(SorterType.JobType.SorterJob.getValue());
            sjCancelRptParam.setUser(objCommon.getUser());
            sjCancelRptParam.setOpeMemo(params.getOpeMemo());
            sjCancelRptParam.setNotifyEAPFlag(true);
            sjCancelRptParam.setJobID(params.getJobID());

            // 更改为EAPMethood
            IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon, objCommon.getUser(), strSortJobListAttributes.getEquipmentID(), null, true);
            if (eapRemoteManager != null) {
                for (int i = 0; i < (retryCountValue + 1); i++) {
                    try {
                        eapRemoteManager.sendSortActionCancelReq(sjCancelRptParam);
                    } catch (ServiceException ex) {
                        if (Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceBindFail()) ||
                                Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceNilObj()) ||
                                Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                            if (i < retryCountValue) {
                                try {
                                    Thread.sleep(sleepTimeValue);
                                    continue;
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    ex.addSuppressed(e);
                                    throw ex;
                                }
                            }
                        }
                        throw ex;
                    } catch (Exception ex) {
                        if (i < retryCountValue) {
                            try {
                                Thread.sleep(sleepTimeValue);
                                continue;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                ex.addSuppressed(e);
                                throw ex;
                            }
                        }
                        throw ex;
                    }
                    break;
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("step - NPWReserve Cancel");
        }
        List<ObjectIdentifier> allCarrierIDs = new ArrayList<>(orgCarrierIDs);
        allCarrierIDs.addAll(dstCarrierIDs);

        int allCarrierLen = CimArrayUtils.getSize(allCarrierIDs);
        List<ObjectIdentifier> NPWRsvCanCarrierIDs = new ArrayList<>();
        List<ObjectIdentifier> NPWRsvLoadPortIDs = new ArrayList<>();
        int allCarrierCnt = 0;
        int srtCmpCnt = 0;
        int srtCmpLen = CimArrayUtils.getSize(strSortJobListAttributes.getSorterComponentJobListAttributesList());
        for (allCarrierCnt = 0; allCarrierCnt < allCarrierLen; allCarrierCnt++) {
            ObjectIdentifier loadPortID = null;
            for (srtCmpCnt = 0; srtCmpCnt < srtCmpLen; srtCmpCnt++) {
                if (ObjectIdentifier.equalsWithValue(allCarrierIDs.get(allCarrierCnt), strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getOriginalCarrierID())) {
                    loadPortID = strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getOriginalPortID();
                    break;
                } else if (ObjectIdentifier.equalsWithValue(allCarrierIDs.get(allCarrierCnt), strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getDestinationCarrierID())) {
                    loadPortID = strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getDestinationPortID();
                    break;
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("step - Carrier is reserved for dispatching");
            }
            Boolean dispatchStateGet = cassetteMethod.cassetteDispatchStateGet(objCommon, allCarrierIDs.get(allCarrierCnt));
            if (log.isTraceEnabled()) {
                log.trace("cassette 【{}】 dispatch state is【{}】", allCarrierIDs.get(allCarrierCnt), dispatchStateGet);
            }
            if (CimBooleanUtils.isTrue(dispatchStateGet)) {
                NPWRsvCanCarrierIDs.add(allCarrierIDs.get(allCarrierCnt));
                NPWRsvLoadPortIDs.add(loadPortID);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("step - NPW Reserve Cancel");
        }
        int NPWRsvLen = CimArrayUtils.getSize(NPWRsvCanCarrierIDs);
        log.info("NPWRsvLen,长度：{}",NPWRsvLen);
        if (CimArrayUtils.isNotEmpty(NPWRsvCanCarrierIDs)) {
            List<Infos.NPWXferCassette> strNPWXferCassette = new ArrayList<>();
            for (int NPWRsvCnt = 0; NPWRsvCnt < NPWRsvLen; NPWRsvCnt++) {
                Infos.NPWXferCassette npwXferCassette = new Infos.NPWXferCassette();
                npwXferCassette.setCassetteID(NPWRsvCanCarrierIDs.get(NPWRsvCnt));
                npwXferCassette.setLoadPortID(NPWRsvLoadPortIDs.get(NPWRsvCnt));
                strNPWXferCassette.add(npwXferCassette);
            }

            transferManagementSystemService.sxNPWCarrierReserveCancelReq(objCommon,
                    strSortJobListAttributes.getEquipmentID(),
                    ObjectIdentifier.fetchValue(strSortJobListAttributes.getPortGroupID()),
                    strNPWXferCassette,
                    params.getNotifyEAPFlag(),
                    params.getOpeMemo());

        }

        if (log.isDebugEnabled()) {
            log.debug("step - add sorter event");
        }
        //当需要通知EAP的时候，是页面上进行的操作，需要记录删除Sorter Job事件
        if (params.getNotifyEAPFlag()) {
            Optional.ofNullable(strSortJobListAttributes.getSorterComponentJobListAttributesList())
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .forEach(data -> {
                        Optional.ofNullable(data.getWaferSorterSlotMapList()).orElseGet(Collections::emptyList).stream()
                                .forEach(ele -> ele.setDirection(SorterType.JobDirection.OMS.getValue()));
                    });
            this.sxAssembleEvent(objCommon, strSortJobListAttributes, params.getOpeMemo(), false, null,
                    SorterType.JobOperation.SortJobDeleted.getValue(),
                    SorterType.JobOperation.ComponentJobDeleted.getValue(), null, !params.getNotifyEAPFlag());
        }
    }
}
