package com.fa.cim.service.lot.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.dto.lot.TerminateReq;
import com.fa.cim.lot.LotNpwUsageRecycleCountUpdateParams;
import com.fa.cim.lot.LotNpwUsageRecycleLimitUpdateParams;
import com.fa.cim.method.*;
import com.fa.cim.method.impl.MonitorGroupMethod;
import com.fa.cim.method.impl.ScheduleChangeReservationMethod;
import com.fa.cim.method.impl.WaferMethod;
import com.fa.cim.newcore.bo.dispatch.CimFlowBatch;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimMonitorGroup;
import com.fa.cim.newcore.bo.restrict.CimRestriction;
import com.fa.cim.newcore.bo.restrict.RestrictionManager;
import com.fa.cim.newcore.dto.restriction.Constrain;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.bank.IBankService;
import com.fa.cim.service.constraint.IConstraintService;
import com.fa.cim.service.flowbatch.IFlowBatchService;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.plan.IPlanService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import com.fa.cim.service.sampling.ISamplingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8          ********            ho                create file
 *
 * @author: ho
 * @date: 2020/9/8 17:01
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class LotServiceImpl implements ILotService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IProcessControlService processControlService;

    @Autowired
    private ICodeMethod codeMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ILotFamilyMethod lotFamilyMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private WaferMethod waferMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IQTimeMethod qTimeMethod;

    @Autowired
    private IMinQTimeMethod minQTimeMethod;

    @Autowired
    private IConstraintService constraintService;

//    @Autowired
//    @Qualifier("EntityInhibitManagerCore")
//    private EntityInhibitManager entityInhibitManager;
    @Autowired
    private RestrictionManager entityInhibitManager;

    @Autowired
    private ScheduleChangeReservationMethod scheduleChangeReservationMethod;

    @Autowired
    private IPlanService planService;

    @Autowired
    private IFlowBatchMethod flowBatchMethod;

    @Autowired
    private MonitorGroupMethod monitorGroupMethod;

    @Autowired
    private IFlowBatchService flowBatchService;

    @Autowired
    private IBankService bankService;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IRunCardMethod runCardMethod;

    @Autowired
    private ILotInqService lotInqService;

    @Autowired
    private IQTimeMethod iqTimeMethod;

    @Autowired
    private IQTimeMethod qtimeMethod;

    @Autowired
    private IContaminationMethod contaminationMethod;

    @Autowired
    private ISamplingService samplingService;

    @Autowired
    private ISorterNewMethod sorterNewMethod;

    public void sxHoldLotReleaseReq(Infos.ObjCommon objCommon, Params.HoldLotReleaseReqParams holdLotReleaseReqParams) {
        Validations.check(0 >= CimArrayUtils.getSize(holdLotReleaseReqParams.getHoldReqList()), retCodeConfig.getInvalidParameter());
        Boolean backupProcessingFlag = false;
        log.trace("\"TXBOC003\".equals(objCommon.getTransactionID())\n" +
                "                || \"TXBOC006\".equals(objCommon.getTransactionID())\n" +
                "                || \"TXBOC009\".equals(objCommon.getTransactionID()) : {}","TXBOC003".
                equals(objCommon.getTransactionID())
                || "TXBOC006".equals(objCommon.getTransactionID())
                || "TXBOC009".equals(objCommon.getTransactionID()));
        if ("TXBOC003".equals(objCommon.getTransactionID())
                || "TXBOC006".equals(objCommon.getTransactionID())
                || "TXBOC009".equals(objCommon.getTransactionID())) {
            backupProcessingFlag = true;
        }
        /*-----------------------------------*/
        /*   Get cassette / lot connection   */
        /*-----------------------------------*/
        log.debug("Get cassette / lot connection");
        ObjectIdentifier cassetteOut = null;
        try {
            cassetteOut = lotMethod.lotCassetteGet(objCommon, holdLotReleaseReqParams.getLotID());
        } catch (ServiceException e) {
            log.trace("!Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode()));
            if (!Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {
                if (!backupProcessingFlag) {
                    throw e;
                }
            }
        }

        /*--------------------------------*/
        /*   Lock objects to be updated   */
        /*--------------------------------*/
        // lock
        log.debug("Lock objects to be updated");
        String strParallelPostProcFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG);
        //----------------------------------------------------------//
        //   Skip cassette lock to increase parallel availability   //
        //   under PostProcess parallel execution                   //
        //----------------------------------------------------------//
        log.debug("Skip cassette lock to increase parallel availability");
        log.trace("!StringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON) : {}",!CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON));
        if (!CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON)){
            //PostProcess sequential execution
            log.trace("!backupProcessingFlag : {}",!backupProcessingFlag);
            if (!backupProcessingFlag){
                objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteOut);
            }
        }
        objectLockMethod.objectLock(objCommon, CimLot.class, holdLotReleaseReqParams.getLotID());
        /*-----------------------------------------------------------*/
        /* Check Condition                                           */
        /*-----------------------------------------------------------*/

        log.trace("Check Condition ObjectUtils.equalsWithValue(BizConstant.SP_REASON_LOTLOCKRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue()) : {}",
                CimStringUtils.equals(BizConstant.SP_REASON_LOTLOCKRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue()));
        if (CimStringUtils.equals(BizConstant.SP_REASON_LOTLOCKRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue())) {
            for (int i = 0; i < CimArrayUtils.getSize(holdLotReleaseReqParams.getHoldReqList()); i++) {
                Validations.check(!ObjectIdentifier.equalsWithValue(holdLotReleaseReqParams.getHoldReqList().get(i).getHoldReasonCodeID(), BizConstant.SP_REASON_LOTLOCK), retCodeConfig.getCannotHoldReleaseWithLocr());
            }
        } else {
            for (int i = 0; i < CimArrayUtils.getSize(holdLotReleaseReqParams.getHoldReqList()); i++) {
                Validations.check(CimStringUtils.equals(holdLotReleaseReqParams.getHoldReqList().get(i).getHoldReasonCodeID().getValue(), BizConstant.SP_REASON_LOTLOCK), retCodeConfig.getCannotHoldReleaseWithLocr());
            }
        }
        for (int i = 0; i < CimArrayUtils.getSize(holdLotReleaseReqParams.getHoldReqList()); i++) {
            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_REASON_NONPROBANKHOLD, holdLotReleaseReqParams.getHoldReqList().get(i).getHoldReasonCodeID().getValue()) : {}",
                    CimStringUtils.equals(BizConstant.SP_REASON_NONPROBANKHOLD, holdLotReleaseReqParams.getHoldReqList().get(i).getHoldReasonCodeID().getValue()));
            if (CimStringUtils.equals(BizConstant.SP_REASON_NONPROBANKHOLD, holdLotReleaseReqParams.getHoldReqList().get(i).getHoldReasonCodeID().getValue())) {
                Validations.check(!"OBNKW014".equals(objCommon.getTransactionID())
                        && !"TXBOC006".equals(objCommon.getTransactionID())
                        && !"TXBOC009".equals(objCommon.getTransactionID()), retCodeConfig.getCannotHoldreleaseWithNpbh());
            }
        }
        //Qiandao add OCAP release reason code start
        log.debug("Qiandao add OCAP release reason code start");
        log.trace("ArrayUtils.isNotEmpty(holdLotReleaseReqParams.getHoldReqList()) : {}", CimArrayUtils.isNotEmpty(holdLotReleaseReqParams.getHoldReqList()));
        if (CimArrayUtils.isNotEmpty(holdLotReleaseReqParams.getHoldReqList())){
            log.trace("BooleanUtils.isTrue(holdLotReleaseReqParams.getHoldReqList().parallelStream().anyMatch(lotHoldReq ->\n" +
                    "                    StringUtils.equals(BizConstant.OCAP_HOLD_LOT_REWORK,ObjectIdentifier.fetchValue(lotHoldReq.getHoldReasonCodeID())) ||\n" +
                    "                            StringUtils.equals(BizConstant.OCAP_HOLD_LOT,ObjectIdentifier.fetchValue(lotHoldReq.getHoldReasonCodeID())))) : {}",
                    CimBooleanUtils.isTrue(holdLotReleaseReqParams.getHoldReqList().parallelStream().anyMatch(lotHoldReq ->
                            CimStringUtils.equals(BizConstant.OCAP_HOLD_LOT, ObjectIdentifier.fetchValue(lotHoldReq.getHoldReasonCodeID()))
                                    || CimStringUtils.equals(BizConstant.OCAP_ADD_MEASURE_HOLD_LOT, ObjectIdentifier.fetchValue(lotHoldReq.getHoldReasonCodeID()))
                                    || CimStringUtils.equals(BizConstant.OCAP_RE_MEASURE_HOLD_LOT, ObjectIdentifier.fetchValue(lotHoldReq.getHoldReasonCodeID()))

                    )));

            if (CimBooleanUtils.isTrue(holdLotReleaseReqParams.getHoldReqList().parallelStream().anyMatch(lotHoldReq ->
                    CimStringUtils.equals(BizConstant.OCAP_HOLD_LOT, ObjectIdentifier.fetchValue(lotHoldReq.getHoldReasonCodeID()))
                            || CimStringUtils.equals(BizConstant.OCAP_ADD_MEASURE_HOLD_LOT, ObjectIdentifier.fetchValue(lotHoldReq.getHoldReasonCodeID()))
                            || CimStringUtils.equals(BizConstant.OCAP_RE_MEASURE_HOLD_LOT, ObjectIdentifier.fetchValue(lotHoldReq.getHoldReasonCodeID()))))) {
                Validations.check(!CimStringUtils.equals(BizConstant.OCAP_HOLD_LOT_RELEASE, ObjectIdentifier.fetchValue(holdLotReleaseReqParams.getReleaseReasonCodeID())), retCodeConfigEx.getCanNotReleaseWithOutOrlc());
            }
        }
        //Qiandao add OCAP release reason code end

        log.debug("Qiandao add OCAP release reason code end");
        log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_REASON_NONPROBANKHOLDRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue()) : {}",
                CimStringUtils.equals(BizConstant.SP_REASON_NONPROBANKHOLDRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue()));

        if (CimStringUtils.equals(BizConstant.SP_REASON_NONPROBANKHOLDRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue())) {
            Validations.check(!"OBNKW014".equals(objCommon.getTransactionID())
                    && !"TXBOC006".equals(objCommon.getTransactionID())
                    && !"TXBOC009".equals(objCommon.getTransactionID()), retCodeConfig.getCannotHoldreleaseWithNpbr());
        }
        log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_REASON_BACKUPOPERATION_HOLD, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue()) : {}",
                CimStringUtils.equals(BizConstant.SP_REASON_BACKUPOPERATION_HOLD, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue()));

        if (CimStringUtils.equals(BizConstant.SP_REASON_BACKUPOPERATION_HOLD, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue())) {
            for (int i = 0; i < holdLotReleaseReqParams.getHoldReqList().size(); i++) {
                Validations.check(!"TXBOC003".equals(objCommon.getTransactionID())
                        && !"TXBOC006".equals(objCommon.getTransactionID())
                        && !"TXBOC009".equals(objCommon.getTransactionID()), retCodeConfig.getCannotHoldreleaseWithBohl());
            }
        }
        log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_REASON_BACKUPOPERATION_HOLDRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue()) : {}",
                CimStringUtils.equals(BizConstant.SP_REASON_BACKUPOPERATION_HOLDRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue()));

        if (CimStringUtils.equals(BizConstant.SP_REASON_BACKUPOPERATION_HOLDRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue())) {
            Validations.check(!"TXBOC003".equals(objCommon.getTransactionID())
                    && !"TXBOC006".equals(objCommon.getTransactionID())
                    && !"TXBOC009".equals(objCommon.getTransactionID()), retCodeConfig.getCannotHoldreleaseWithBohr());
        }
        String lotStateGet = lotMethod.lotStateGet(objCommon, holdLotReleaseReqParams.getLotID());
        log.trace("StringUtils.isEmpty(lotStateGet) || !ObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotStateGet) : {}",
                CimStringUtils.isEmpty(lotStateGet) || !CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotStateGet));

        if (CimStringUtils.isEmpty(lotStateGet) || !CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotStateGet)) {
            log.trace("!backupProcessingFlag : {}",!backupProcessingFlag);
            if (!backupProcessingFlag) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotStat(), lotStateGet));
            }
        }
        String holdStateGet = lotMethod.lotHoldStateGet(objCommon, holdLotReleaseReqParams.getLotID());
        log.trace("StringUtils.isEmpty(holdStateGet) || ObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD, holdStateGet) : {}",
                CimStringUtils.isEmpty(holdStateGet) || CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD, holdStateGet));

        if (CimStringUtils.isEmpty(holdStateGet) || CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD, holdStateGet)){
            throw new ServiceException(new OmCode(retCodeConfig.getLotNotHeld(), ObjectIdentifier.fetchValue(holdLotReleaseReqParams.getLotID())));
        }
        //----------------------------------
        //  Get InPostProcessFlag of lot
        //----------------------------------
        log.debug("Get InPostProcessFlag of lot");
        Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, holdLotReleaseReqParams.getLotID());
        //-----------------------------------------------------------
        // Check lot interFabXferState
        //-----------------------------------------------------------
        log.debug("Check lot interFabXferState");
        String interFabXferStateGet = lotMethod.lotInterFabXferStateGet(objCommon, holdLotReleaseReqParams.getLotID());

        //----------------------------------------------
        //  If lot is in post process, returns error
        //----------------------------------------------
        log.debug("If lot is in post process, returns error");
        log.trace("BaseStaticMethod.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot()) : {}",CimBooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot()));
        if (CimBooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot())) {
            log.trace("!ObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED, interFabXferStateGet) : {}",
                    !CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED, interFabXferStateGet));

            if (!CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED, interFabXferStateGet)) {
                /*---------------------------*/
                /* Get UserGroupID By UserID */
                /*---------------------------*/
                log.debug("Get UserGroupID By UserID");
                List<ObjectIdentifier> objGetPersonUserGroupListDROut = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                int i = 0;
                for (i = 0; i < CimArrayUtils.getSize(objGetPersonUserGroupListDROut); i++) {
                }
                Validations.check(i == CimArrayUtils.getSize(objGetPersonUserGroupListDROut), retCodeConfig.getLotInPostProcess());
            }
        }
        /*-----------------------------------------------------------*/
        /*   Check PosCode                                           */
        /*-----------------------------------------------------------*/
        log.debug("Check PosCode");
        List<ObjectIdentifier> strCheckedCodes = new ArrayList<>();
        strCheckedCodes.add(holdLotReleaseReqParams.getReleaseReasonCodeID());
        log.trace("\"OPRCW007\".equals(objCommon.getTransactionID()) : {}","OPRCW007".equals(objCommon.getTransactionID()));
        log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_REASON_NONPROBANKHOLDRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue()) : {}",
                CimStringUtils.equals(BizConstant.SP_REASON_NONPROBANKHOLDRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue()));
        if ("OPRCW007".equals(objCommon.getTransactionID())) {
            codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_PROCESSHOLDCANCEL, strCheckedCodes);
        } else if (CimStringUtils.equals(BizConstant.SP_REASON_NONPROBANKHOLDRELEASE, holdLotReleaseReqParams.getReleaseReasonCodeID().getValue())) {
            // Do Nothing.
        } else {
            codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_LOTHOLDRELEASE, strCheckedCodes);
        }
        /*------------------------------------------------------------------------*/
        /*   Change State                                                         */
        /*------------------------------------------------------------------------*/
        log.debug("Change State ");
        Outputs.ObjLotHoldReleaseOut objLotHoldReleaseOut = lotMethod.lotHoldRelease(objCommon, holdLotReleaseReqParams.getLotID(), holdLotReleaseReqParams.getReleaseReasonCodeID(), holdLotReleaseReqParams.getHoldReqList());

        /*---------------------------------------*/
        /*   Update cassette's MultiLotType      */
        /*---------------------------------------*/
        log.debug("Update cassette's MultiLotType");
        log.trace("!backupProcessingFlag : {}",!backupProcessingFlag);
        if (!backupProcessingFlag) {
            log.trace("!StringUtils.isEmpty(cassetteOut) : {}",!ObjectIdentifier.isEmpty(cassetteOut));
            if (!ObjectIdentifier.isEmpty(cassetteOut)) {
                cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteOut);
            }
        }

        /*------------------------------------------------------------------------*/
        /*   Make lot Hold Release History                                        */
        /*------------------------------------------------------------------------*/
        log.debug("Make lot Hold Release History");
        log.trace("!ObjectUtils.equalsWithValue(holdLotReleaseReqParams.getReleaseReasonCodeID().getValue(), BizConstant.SP_REASON_LOTLOCKRELEASE) : {}",
                !CimStringUtils.equals(holdLotReleaseReqParams.getReleaseReasonCodeID().getValue(), BizConstant.SP_REASON_LOTLOCKRELEASE));

        if (!CimStringUtils.equals(holdLotReleaseReqParams.getReleaseReasonCodeID().getValue(), BizConstant.SP_REASON_LOTLOCKRELEASE)) {
            Inputs.LotHoldEventMakeParams lotHoldEventMakeParams = new Inputs.LotHoldEventMakeParams();
            lotHoldEventMakeParams.setHoldHistoryList(objLotHoldReleaseOut.getHoldHistoryList());
            lotHoldEventMakeParams.setLotID(holdLotReleaseReqParams.getLotID());
            lotHoldEventMakeParams.setTransactionID(TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue());
            eventMethod.lotHoldEventMake(objCommon, lotHoldEventMakeParams);
        }

        /*------------------------------------------------------------------------*/
        /*   Future Hold Cancel                                                   */
        /*------------------------------------------------------------------------*/
        log.debug("Future Hold Cancel");
        log.trace("ArrayUtils.getSize(objLotHoldReleaseOut.getHoldList()) > 0 : {}", CimArrayUtils.getSize(objLotHoldReleaseOut.getHoldList()) > 0);
        if (CimArrayUtils.getSize(objLotHoldReleaseOut.getHoldList()) > 0) {
            int foundCnt = 0;
            int cancelLen = CimArrayUtils.getSize(objLotHoldReleaseOut.getHoldList());
            List<Infos.LotHoldReq> futureHoldCancelList = new ArrayList<>();
            for (int cancelCnt = 0; cancelCnt < cancelLen; cancelCnt++) {
                Infos.FutureHoldSearchKey fhSearchKey = new Infos.FutureHoldSearchKey();
                fhSearchKey.setLotID(holdLotReleaseReqParams.getLotID());
                fhSearchKey.setHoldType(objLotHoldReleaseOut.getHoldList().get(cancelCnt).getHoldType());
                fhSearchKey.setReasonCodeID(objLotHoldReleaseOut.getHoldList().get(cancelCnt).getHoldReasonCodeID());
                fhSearchKey.setUserID(objLotHoldReleaseOut.getHoldList().get(cancelCnt).getHoldUserID());
                fhSearchKey.setRouteID(objLotHoldReleaseOut.getHoldList().get(cancelCnt).getRouteID());
                fhSearchKey.setOperationNumber(objLotHoldReleaseOut.getHoldList().get(cancelCnt).getOperationNumber());
                fhSearchKey.setRelatedLotID(objLotHoldReleaseOut.getHoldList().get(cancelCnt).getRelatedLotID());
                try {
                    lotMethod.lotFutureHoldListbyKeyDR(objCommon, fhSearchKey, 1);
                } catch (ServiceException e) {
                    log.trace("Validations.isEquals(retCodeConfig.getNotFoundFtholdEntW(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotFoundFtholdEntW(), e.getCode()));
                    if (Validations.isEquals(retCodeConfig.getNotFoundFtholdEntW(), e.getCode())) {
                        log.error("The target future hold request is not found. It may have been cancelled.");
                    } else {
                        throw e;
                    }
                }
                futureHoldCancelList.add(foundCnt, objLotHoldReleaseOut.getHoldList().get(cancelCnt));
                foundCnt++;
            }
            log.trace("foundCnt > 0 : {}",foundCnt > 0);
            if (foundCnt > 0) {
                Params.FutureHoldCancelReqParams futureCancelReqParams = new Params.FutureHoldCancelReqParams();
                futureCancelReqParams.setUser(objCommon.getUser());
                futureCancelReqParams.setLotID(holdLotReleaseReqParams.getLotID());
                futureCancelReqParams.setReleaseReasonCodeID(holdLotReleaseReqParams.getReleaseReasonCodeID());
                futureCancelReqParams.setEntryType(BizConstant.SP_ENTRYTYPE_REMOVE);
                futureCancelReqParams.setLotHoldList(futureHoldCancelList);

                processControlService.sxFutureHoldCancelReq(objCommon, futureCancelReqParams);
            }
        }

        // check contamination level after hold lot release
        log.debug("check contamination level after hold lot release");
        boolean isExistContaminationReleaseHold = holdLotReleaseReqParams.getHoldReqList()
                .parallelStream()
                .anyMatch(lotHoldReq ->
                        CimStringUtils.equals(BizConstant.SP_REASON_CONTAMINATION_HOLD, lotHoldReq.getHoldReasonCodeID().getValue())
                     || CimStringUtils.equals(BizConstant.SP_REASON_PR_HOLD, lotHoldReq.getHoldReasonCodeID().getValue())
                     || CimStringUtils.equals(BizConstant.SP_REASON_CARRIER_CATEGORY_HOLD, lotHoldReq.getHoldReasonCodeID().getValue())
                );
        log.trace("isExistContaminationReleaseHold : {}",isExistContaminationReleaseHold);
        if (isExistContaminationReleaseHold) {
            boolean holdFlag = contaminationMethod.lotCheckContaminationLevelForHold(objCommon, holdLotReleaseReqParams.getLotID());
            Validations.check(holdFlag,retCodeConfigEx.getContaminationLevelMatchState());
        }
    }

    @SuppressWarnings("deprecation")
    public void sxHoldLotReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.LotHoldReq> holdReqList) {
        Validations.check (CimArrayUtils.isEmpty(holdReqList), retCodeConfig.getInvalidParameter());

        ObjectIdentifier cassetteID = null;
        boolean backupProcessingFlag = false;
        log.trace("StringUtils.equals(\"TXBOC003\" , objCommon.getTransactionID()) : {}", CimStringUtils.equals("TXBOC003" , objCommon.getTransactionID()));
        if (CimStringUtils.equals("TXBOC003" , objCommon.getTransactionID())) {
            backupProcessingFlag = true;
        }

        log.trace("!backupProcessingFlag : {}",!backupProcessingFlag);
        if (!backupProcessingFlag) {
            ObjectIdentifier objGetLotCassetteOut = null;
            try {
                objGetLotCassetteOut = lotMethod.lotCassetteGet(objCommon, lotID);
            } catch (ServiceException e) {
                log.trace("Validations.isEquals(retCodeConfig.getNotFoundCst(),e.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotFoundCst(),e.getCode()));
                if (Validations.isEquals(retCodeConfig.getNotFoundCst(),e.getCode())) {
                    int casstteForHold = StandardProperties.OM_CARRIER_CHK_RELATION_FOR_HOLD.getIntValue();
                    if (casstteForHold == 0) {

                    } else {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }

            log.trace("null != objGetLotCassetteOut : {}",null != objGetLotCassetteOut);
            if(null != objGetLotCassetteOut){
                cassetteID = objGetLotCassetteOut;
            }
        }

        //step 1 - Lock objects to be updated
        log.debug("[step-1] Lock objects to be updated");
        String strParallelPostProcFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG);
        //step 2 - Skip cassette lock to increase parallel availability under PostProcess parallel execution
        log.debug("!StringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON) : {}",
                !CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON));

        if (!CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON)) {
            log.trace("!backupProcessingFlag : {}",!backupProcessingFlag);
            if (!backupProcessingFlag) {
                log.trace("null != cassetteID : {}",null != cassetteID);
                if (null != cassetteID) {
                    //  object_Lock
                    objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
                }
            }
        }
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);

        //step 3 - Check Condition
        log.debug("[step-3] Check Condition");
        Boolean bLotLockFlag = false;
        for (int i = 0; i < holdReqList.size(); i++) {
            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_REASON_LOTLOCK, holdReqList.get(i).getHoldReasonCodeID()) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_REASON_LOTLOCK, holdReqList.get(i).getHoldReasonCodeID()));
            log.trace("BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            log.trace("BizConstant.SP_REASON_RUNNINGHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_RUNNINGHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            log.trace("BizConstant.SP_REASON_RUNNINGHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_RUNNINGHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            log.trace("BizConstant.SP_REASON_FORCECOMPHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_FORCECOMPHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            log.trace("BizConstant.SP_REASON_BACKUPOPERATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_BACKUPOPERATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            log.trace("BizConstant.SP_REASON_CONTAMINATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_CONTAMINATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            if (CimObjectUtils.equalsWithValue(BizConstant.SP_REASON_LOTLOCK, holdReqList.get(i).getHoldReasonCodeID())) {
                bLotLockFlag = true;
                Validations.check (!TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.POST_PROCESS_ACTION_REGIST_REQ.getValue().equals(objCommon.getTransactionID())
                        //wafer bonding根据逻辑添加，无源码
                        && !TransactionIDEnum.BONDING_GROUP_MODIFY_REQ.getValue().equals(objCommon.getTransactionID())
                        && !"OPOSW008".equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.OPE_LOCATE_REQ.getValue().equals(objCommon.getTransactionID()), retCodeConfig.getInvalidReasonCodeFromClient(), BizConstant.SP_REASON_LOTLOCK);
            } else if (BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                Validations.check (!TransactionIDEnum.NON_PRO_BANK_IN_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.SPLIT_WAFER_LOT_WITH_OUT_HOLD_RELEASE_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.FORCE_OPERATION_COMP_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.SPC_ACTION_EXECUTE_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.LOT_TERMINATE_REQ.getValue().equals(objCommon.getTransactionID()), retCodeConfig.getCannotHoldWithNpbh());
            } else if (BizConstant.SP_REASON_RUNNINGHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                Validations.check(true, retCodeConfig.getInvalidReasonCodeFromClient(), BizConstant.SP_REASON_RUNNINGHOLD);
            } else if (BizConstant.SP_REASON_FORCECOMPHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                Validations.check (!TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.FORCE_OPERATION_COMP_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.SPLIT_WAFER_LOT_WITH_OUT_HOLD_RELEASE_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.BONDING_GROUP_PARTIAL_REMOVE_REQ.getValue().equals(objCommon.getTransactionID())
                        && !"TXPCC059".equals(objCommon.getTransactionID()), retCodeConfig.getInvalidReasonCodeFromClient(), BizConstant.SP_REASON_FORCECOMPHOLD);
            } else if (BizConstant.SP_REASON_BACKUPOPERATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                Validations.check (!"TXBOC003".equals(objCommon.getTransactionID()), retCodeConfig.getCannotHoldWithBohl(), BizConstant.SP_REASON_BACKUPOPERATION_HOLD);
            }else if (BizConstant.SP_REASON_CONTAMINATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                Validations.check ("OLOTW001".equals(objCommon.getTransactionID()), retCodeConfigEx.getCannotHoldWithCcmh());
            }
        }

        Infos.LotBackupInfo lotBackupInfo = lotMethod.lotBackupInfoGet(objCommon, lotID);
        log.trace("null != lotBackupInfo : {}",null != lotBackupInfo);
        if (null != lotBackupInfo) {
            Validations.check (!lotBackupInfo.getCurrentLocationFlag() || lotBackupInfo.getTransferFlag(), retCodeConfig.getLotInOthersite());
        }

        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        log.trace("!BizConstant.CIMFW_LOT_STATE_ACTIVE.equals(lotState) : {}",!BizConstant.CIMFW_LOT_STATE_ACTIVE.equals(lotState));
        if (!BizConstant.CIMFW_LOT_STATE_ACTIVE.equals(lotState)) {
            Validations.check (!backupProcessingFlag, new OmCode(retCodeConfig.getInvalidLotStat(), lotState));
        }

        String lotProcessStateGet = lotMethod.lotProcessStateGet(objCommon, lotID);
        //step 4 - ProcessState should not be Processing
        log.debug("[step-4]  ProcessState should not be Processing");
        log.trace("BizConstant.SP_LOT_PROCSTATE_PROCESSING.equals(lotProcessStateGet) : {}",BizConstant.SP_LOT_PROCSTATE_PROCESSING.equals(lotProcessStateGet));
        if (BizConstant.SP_LOT_PROCSTATE_PROCESSING.equals(lotProcessStateGet)) {
            log.trace("TransactionIDEnum.COLLECTED_DATA_BY_PJ_RPT.getValue().equals(objCommon.getTransactionID())\n" +
                    "                    || TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue().equals(objCommon.getTransactionID())\n" +
                    "                    || TransactionIDEnum.POST_PROCESS_ACTION_REGIST_REQ.getValue().equals(objCommon.getTransactionID())\n" +
                    "                    || TransactionIDEnum.POST_PROCESS_ACTION_UPDATE.getValue().equals(objCommon.getTransactionID())\n" +
                    "                    || \"OPOSW008\".equals(objCommon.getTransactionID())\n" +
                    "                    || bLotLockFlag : {}",
                    TransactionIDEnum.COLLECTED_DATA_BY_PJ_RPT.getValue().equals(objCommon.getTransactionID())
                            || TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue().equals(objCommon.getTransactionID())
                            || TransactionIDEnum.POST_PROCESS_ACTION_REGIST_REQ.getValue().equals(objCommon.getTransactionID())
                            || TransactionIDEnum.POST_PROCESS_ACTION_UPDATE.getValue().equals(objCommon.getTransactionID())
                            || "OPOSW008".equals(objCommon.getTransactionID())
                            || bLotLockFlag);

            if (TransactionIDEnum.COLLECTED_DATA_BY_PJ_RPT.getValue().equals(objCommon.getTransactionID())
                    || TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue().equals(objCommon.getTransactionID())
                    || TransactionIDEnum.POST_PROCESS_ACTION_REGIST_REQ.getValue().equals(objCommon.getTransactionID())
                    || TransactionIDEnum.POST_PROCESS_ACTION_UPDATE.getValue().equals(objCommon.getTransactionID())
                    || "OPOSW008".equals(objCommon.getTransactionID())
                    || bLotLockFlag) {
                //OK
            } else {
                Validations.check(true, retCodeConfig.getInvalidLotProcstat(),ObjectIdentifier.fetchValue(lotID),lotProcessStateGet);
            }
        }

        //step 5 -  Get InPostProcessFlag of lot
        log.debug("[step-5] Get InPostProcessFlag of lot");
        Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);

        //step 6 -  Check lot interFabXferState
        log.debug("[step-6] Check lot interFabXferState");
        String lotInterFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);

        //step 7 - If lot is in post process, returns error
        log.debug("[step-7] If lot is in post process, returns error");
        if (BaseStaticMethod.isTrue(lotInPostProcessFlagGetOut.getInPostProcessFlagOfLot())) {
            int ppChainMode = StandardProperties.OM_PP_CHAIN_FLAG.getIntValue();
            log.trace("ppChainMode == 1 : {}",ppChainMode == 1);
            if (ppChainMode == 1) {
                String strTriggerDKey = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
                log.trace("StringUtils.isEmpty(strTriggerDKey) : {}", CimStringUtils.isEmpty(strTriggerDKey));
                if (CimStringUtils.isEmpty(strTriggerDKey)) {
                    ppChainMode = 0;
                }
            }

            Boolean bSkipPPLotCheck = false;
            log.trace("ppChainMode == 1 && ArrayUtils.getSize(holdReqList) == 1 &&\n" +
                    "                    (ObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_PSM_HOLD )||\n" +
                    "                            ObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_SPR_HOLD)) : {}",
                    ppChainMode == 1 && CimArrayUtils.getSize(holdReqList) == 1 &&
                            (CimObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_PSM_HOLD )||
                                    CimObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_SPR_HOLD)));

            if (ppChainMode == 1 && CimArrayUtils.getSize(holdReqList) == 1 &&
                    (CimObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_PSM_HOLD )||
                            CimObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_SPR_HOLD))) {
                log.info("Hold reason is PSWH or SPRH and called from PostProc");
                bSkipPPLotCheck = true;
            }

            log.trace("!bSkipPPLotCheck && !BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED.equals(lotInterFabXferState) : {}",!bSkipPPLotCheck && !BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED.equals(lotInterFabXferState));
            if (!bSkipPPLotCheck && !BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED.equals(lotInterFabXferState)) {
                //step 8 - Get UserGroupID By UserID
                log.debug("[step-8] Get UserGroupID By UserID");
                List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());

                int i;
                for (i = 0; i < CimArrayUtils.getSize(userGroupIDs); i++) {
                }
                Validations.check (CimArrayUtils.getSize(userGroupIDs) == i, retCodeConfig.getLotInPostProcess());
            }
        }

        //step 9 - Check PosCode
        log.debug("[step-9] Check PosCode");
        List<ObjectIdentifier> futureHolds = new ArrayList<>();
        List<ObjectIdentifier> processHolds = new ArrayList<>();
        List<ObjectIdentifier> lotHolds = new ArrayList<>();
        for (int i = 0; i < holdReqList.size(); i++) {
            log.trace("BizConstant.SP_REASONCAT_FUTUREHOLD.equals(holdReqList.get(i).getHoldType()) : {}",BizConstant.SP_REASONCAT_FUTUREHOLD.equals(holdReqList.get(i).getHoldType()));
            log.trace("BizConstant.SP_REASONCAT_PROCESSHOLD.equals(holdReqList.get(i).getHoldType()) : {}",BizConstant.SP_REASONCAT_PROCESSHOLD.equals(holdReqList.get(i).getHoldType()));
            log.trace("BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            if (BizConstant.SP_REASONCAT_FUTUREHOLD.equals(holdReqList.get(i).getHoldType())) {
                futureHolds.add(holdReqList.get(i).getHoldReasonCodeID());
            } else if (BizConstant.SP_REASONCAT_PROCESSHOLD.equals(holdReqList.get(i).getHoldType())) {
                processHolds.add(holdReqList.get(i).getHoldReasonCodeID());
            } else if (BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                continue;
            } else {
                lotHolds.add(holdReqList.get(i).getHoldReasonCodeID());
            }
        }
        log.trace("!ArrayUtils.isEmpty(futureHolds) : {}",!CimArrayUtils.isEmpty(futureHolds));
        if (!CimArrayUtils.isEmpty(futureHolds)) {
            codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_FUTUREHOLD, futureHolds);
        }
        log.trace("!ArrayUtils.isEmpty(processHolds) : {}",!CimArrayUtils.isEmpty(processHolds));
        if (!CimArrayUtils.isEmpty(processHolds)) {
            codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_PROCESSHOLD, processHolds);
        }
        log.trace("!ArrayUtils.isEmpty(lotHolds) : {}",!CimArrayUtils.isEmpty(lotHolds));
        if (!CimArrayUtils.isEmpty(lotHolds)) {
            List<ObjectIdentifier> tmp = new ArrayList<>();
            for (int i = 0; i < lotHolds.size(); i++) {
                tmp.add(lotHolds.get(i));
                try {
                    codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_LOTHOLD, tmp);
                } catch (ServiceException e) {
                    log.trace("TransactionIDEnum.HOLD_LOT_REQ.getValue().equals(objCommon.getTransactionID()) : {}",TransactionIDEnum.HOLD_LOT_REQ.getValue().equals(objCommon.getTransactionID()));
                    if (TransactionIDEnum.HOLD_LOT_REQ.getValue().equals(objCommon.getTransactionID())) {
                        throw e;
                    } else {
                        codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_FUTUREHOLD, tmp);
                    }
                }

            }
        }

        //step 10 - Change State
        log.debug("[step-10] Change State");
        List<Infos.HoldHistory> objLotHoldOut = lotMethod.lotHold(objCommon, lotID, holdReqList);

        //step 11 -  Update cassette's MultiLotType
        log.debug("[step-11] Update cassette's MultiLotType");
        if (!backupProcessingFlag) {
            log.trace("!ObjectUtils.isEmpty(cassetteID) : {}",!CimObjectUtils.isEmpty(cassetteID));
            if (!CimObjectUtils.isEmpty(cassetteID)) {
                cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
            }
        }

        /*------------------------------------------------------------------------*/
        /*   Make History                                                         */
        /*------------------------------------------------------------------------*/
        log.debug("Make History ");
        Boolean bReasonLotLockFlag = false;
        for (int i = 0 ; i< holdReqList.size(); i++){
            log.debug("ObjectUtils.equalsWithValue(holdReqList.get(i).getHoldReasonCodeID(),BizConstant.SP_REASON_LOTLOCK) : {}",
                    CimObjectUtils.equalsWithValue(holdReqList.get(i).getHoldReasonCodeID(),BizConstant.SP_REASON_LOTLOCK));

            if (CimObjectUtils.equalsWithValue(holdReqList.get(i).getHoldReasonCodeID(),BizConstant.SP_REASON_LOTLOCK)){
                bReasonLotLockFlag = true;
                break;
            }
        }
        log.trace("!bReasonLotLockFlag : {}",!bReasonLotLockFlag);
        if (!bReasonLotLockFlag){
            Inputs.LotHoldEventMakeParams lotHoldEventMakeParams = new Inputs.LotHoldEventMakeParams();
            lotHoldEventMakeParams.setTransactionID(TransactionIDEnum.HOLD_LOT_REQ.getValue());
            lotHoldEventMakeParams.setLotID(lotID);
            lotHoldEventMakeParams.setHoldHistoryList(objLotHoldOut);
            eventMethod.lotHoldEventMake(objCommon, lotHoldEventMakeParams);
        }
    }

    @SuppressWarnings("deprecation")
    public void sxMergeLotNotOnPfReq(Infos.ObjCommon objCommon, Params.MergeLotNotOnPfReqParams mergeLotNotOnPfReqParams) {
        Boolean bParentLotHasWaferID = true;
        Boolean bChildLotHasWaferID  = true;
        Boolean bHasWaferID          = true;
        //-------------------------------------------------------------
        // Get cassetteID which parentLot is in for object_Lock().
        //-------------------------------------------------------------
        log.debug("Get cassetteID which parentLot is in for object_Lock().");
        String tmpLotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        Boolean updateControlJobFlag = false;
        Long lockMode = 0L;
        //-----------------------------
        // set parentLotID of in-parm.
        //-----------------------------
        log.debug("set parentLotID of in-parm.");
        ObjectIdentifier objGetLotCassetteOut = null;
        ObjectIdentifier aParentCassetteID = new ObjectIdentifier();
        tmpLotOperationEIcheck = null == tmpLotOperationEIcheck ? BizConstant.CONSTANT_QUANTITY_ZERO : tmpLotOperationEIcheck;
        Integer lotOperationEIcheck = Integer.valueOf(tmpLotOperationEIcheck);
        String objCassetteTransferStateGetOut = null;
        Outputs.ObjCassetteEquipmentIDGetOut cassetteEqpOut = null;
        try {
            objGetLotCassetteOut = lotMethod.lotCassetteGet(objCommon, mergeLotNotOnPfReqParams.getParentLotID());
            // When parentLot is in parentCassetteID.
            aParentCassetteID.setValue(objGetLotCassetteOut.getValue());
            log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) : {}",
                    CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()));
            if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString())) {
                //-------------------------------
                // Get carrier transfer status
                //-------------------------------
                log.debug("Get carrier transfer status");
                objCassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
                /*------------------------------------*/
                /*   Get eqp ID in cassette     */
                /*------------------------------------*/
                log.debug("Get eqp ID in cassette");
                cassetteEqpOut  = cassetteMethod.cassetteEquipmentIDGet(objCommon, aParentCassetteID);
                //-------------------------------
                // Get required eqp lock mode
                //-------------------------------
                // object_lockMode_Get
                log.debug("Get required eqp lock mode");
                Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
                objLockModeIn.setObjectID(cassetteEqpOut.getEquipmentID());
                objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objLockModeIn.setFunctionCategory(TransactionIDEnum.MERGE_WAFER_LOT_NOT_ON_ROUTE_REQ.getValue());
                objLockModeIn.setUserDataUpdateFlag(false);
                Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
                lockMode = objLockModeOut.getLockMode();
                log.debug("StringUtils.equals(objCassetteTransferStateGetOut, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) : {}", CimStringUtils.equals(objCassetteTransferStateGetOut, BizConstant.SP_TRANSSTATE_EQUIPMENTIN));
                if (CimStringUtils.equals(objCassetteTransferStateGetOut, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                    updateControlJobFlag = true;
                    log.debug("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                    if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                        // advanced_object_Lock
                        objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(cassetteEqpOut.getEquipmentID(),
                                BizConstant.SP_CLASSNAME_POSMACHINE,
                                BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                                objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                        // advanced_object_Lock
                        objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(cassetteEqpOut.getEquipmentID(),
                                BizConstant.SP_CLASSNAME_POSMACHINE,
                                BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                                (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE,
                                Collections.singletonList(aParentCassetteID.getValue())));
                    } else {
                        // object_Lock
                        objectLockMethod.objectLock(objCommon, CimMachine.class, cassetteEqpOut.getEquipmentID());
                    }
                }
            }
            // object_Lock
            log.debug(" object_Lock");
            objectLockMethod.objectLock(objCommon, CimCassette.class, aParentCassetteID);
            log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()));
            if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString())) {
                log.trace("BooleanUtils.isFalse(updateControlJobFlag) || !BizConstant.SP_EQP_LOCK_MODE_WRITE.equals(lockMode) : {}", CimBooleanUtils.isFalse(updateControlJobFlag) || !BizConstant.SP_EQP_LOCK_MODE_WRITE.equals(lockMode));
                if (CimBooleanUtils.isFalse(updateControlJobFlag) || !BizConstant.SP_EQP_LOCK_MODE_WRITE.equals(lockMode)) {
                    //---------------------------------
                    //   Get cassette's ControlJobID
                    //---------------------------------
                    log.debug("Get cassette's ControlJobID");
                    ObjectIdentifier strCassetteControlJobIDGetOut = cassetteMethod.cassetteControlJobIDGet(objCommon, aParentCassetteID);
                    // object_Lock
                    log.trace("(!ObjectUtils.isEmptyWithValue(strCassetteControlJobIDGetOut) : {}",!CimObjectUtils.isEmptyWithValue(strCassetteControlJobIDGetOut));
                    if (!CimObjectUtils.isEmptyWithValue(strCassetteControlJobIDGetOut)){
                        updateControlJobFlag = true;
                        log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                            /*------------------------------*/
                            /*   Lock ControlJob Object     */
                            /*------------------------------*/
                            objectLockMethod.objectLock(objCommon, CimControlJob.class, strCassetteControlJobIDGetOut);
                        }
                    }
                }
            }
        } catch (ServiceException e) {
            log.trace("!Validations.isEquals(retCodeConfig.getNotFoundCst(),e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotFoundCst(),e.getCode()));
            if (!Validations.isEquals(retCodeConfig.getNotFoundCst(),e.getCode())) {
                throw e;
            }
        }


        //-------------------------------------------------------------
        // Get cassetteID which childLot is in for object_Lock().
        //-------------------------------------------------------------
        //---------------------------
        // set childLotID of in-parm
        //---------------------------
        log.debug("Get cassetteID which childLot is in for object_Lock().");
        ObjectIdentifier getLotCassetteOut = null;
        int retCode0 = 0;
        try {
            getLotCassetteOut = lotMethod.lotCassetteGet(objCommon, mergeLotNotOnPfReqParams.getChildLotID());
        } catch (ServiceException e) {
            retCode0 = e.getCode();
            log.trace("!Validations.isEquals(retCodeConfig.getNotFoundCst() ,e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotFoundCst() ,e.getCode()));
            if(!Validations.isEquals(retCodeConfig.getNotFoundCst() ,e.getCode())){
                throw e;
            }
        }
        ObjectIdentifier aChildCassetteID = null;
        log.trace("retCode0 == 0 : {}",retCode0 == 0);
        if (retCode0 == 0){
            aChildCassetteID = getLotCassetteOut;
            objectLockMethod.objectLock(objCommon, CimCassette.class, aChildCassetteID);
        }

        //---------------------------------------------------------//
        //   Check CassetteIDs of the Parent lot and the Child lot //
        //---------------------------------------------------------//
        log.debug("Check CassetteIDs of the Parent lot and the Child lot");
        log.trace("!ObjectUtils.isEmpty(aParentCassetteID) && !ObjectUtils.isEmpty(aChildCassetteID) : {}",!CimObjectUtils.isEmpty(aParentCassetteID) && !CimObjectUtils.isEmpty(aChildCassetteID));
        if (!CimObjectUtils.isEmpty(aParentCassetteID) && !CimObjectUtils.isEmpty(aChildCassetteID)) {
            Validations.check (!CimObjectUtils.equalsWithValue(aParentCassetteID.getValue(),aChildCassetteID.getValue()), retCodeConfig.getCassetteNotSame());
        }
        /*--------------------------------*/
        /*   Lock objects to be updated   */
        /*--------------------------------*/
        // object_Lock
        log.debug("Lock objects to be updated : {}",mergeLotNotOnPfReqParams.getParentLotID());
        objectLockMethod.objectLock(objCommon, CimLot.class, mergeLotNotOnPfReqParams.getParentLotID());

        // object_Lock
        log.debug("Lock objects to be updated : {}",mergeLotNotOnPfReqParams.getChildLotID());
        objectLockMethod.objectLock(objCommon, CimLot.class, mergeLotNotOnPfReqParams.getChildLotID());

        /*-------------------------------*/
        /*   Check SorterJob existence   */
        /*-------------------------------*/
        //waferSorter_sorterJob_CheckForOperation
        log.debug("Check SorterJob existence");
        List<ObjectIdentifier> dummyCastIDs = new ArrayList<>();
        List<ObjectIdentifier> lotSortIDs = new ArrayList<>();

        lotSortIDs.add(mergeLotNotOnPfReqParams.getParentLotID());
        lotSortIDs.add(mergeLotNotOnPfReqParams.getChildLotID());
        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(new Infos.EquipmentLoadPortAttribute());
        objWaferSorterJobCheckForOperation.setCassetteIDList(dummyCastIDs);
        objWaferSorterJobCheckForOperation.setLotIDList(lotSortIDs);
        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);

        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);

        //---------------------------------
        // Check Contents for parent lot
        //---------------------------------
        log.debug("Check Contents for parent lot");
        String lotContentsGet = lotMethod.lotContentsGet(mergeLotNotOnPfReqParams.getParentLotID());

        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER,lotContentsGet)
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE,lotContentsGet), new OmCode(retCodeConfig.getInvalidLotContents(),mergeLotNotOnPfReqParams.getParentLotID().getValue()));
        //---------------------------------
        // Check Contents for child lot
        //---------------------------------
        log.debug("Check Contents for child lot");
        String lotContentsGetForChild = lotMethod.lotContentsGet(mergeLotNotOnPfReqParams.getChildLotID());
        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER,lotContentsGetForChild)
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE,lotContentsGetForChild), new OmCode(retCodeConfig.getInvalidLotContents(),mergeLotNotOnPfReqParams.getChildLotID().getValue()));
        //------------------------------------
        // Check Bonding Group for parent lot
        //------------------------------------
        log.debug("Check Bonding Group for parent lot");
        String groupIDGetDROutForParent = lotMethod.lotBondingGroupIDGetDR(objCommon, mergeLotNotOnPfReqParams.getParentLotID());
        Validations.check(!CimObjectUtils.isEmpty(groupIDGetDROutForParent),new OmCode(retCodeConfig.getLotHasBondingGroup(),mergeLotNotOnPfReqParams.getParentLotID().getValue(),groupIDGetDROutForParent));
        //------------------------------------
        // Check Bonding Group for child lot
        //------------------------------------
        log.debug("Check Bonding Group for child lot");
        String groupIDGetDROutForChild = lotMethod.lotBondingGroupIDGetDR(objCommon, mergeLotNotOnPfReqParams.getChildLotID());
        Validations.check(!CimObjectUtils.isEmpty(groupIDGetDROutForChild),new OmCode(retCodeConfig.getLotHasBondingGroup(),mergeLotNotOnPfReqParams.getChildLotID().getValue(),groupIDGetDROutForChild));

        //------------------------------------------------------
        // Check parent lot and child lot is same state or not
        //------------------------------------------------------
        log.debug("Check parent lot and child lot is same state or not");
        Outputs.ObjLotAllStateCheckSame objLotAllStateCheckSameRetCode = lotMethod.lotAllStateCheckSame(objCommon, mergeLotNotOnPfReqParams.getParentLotID(), mergeLotNotOnPfReqParams.getChildLotID());
        //------------------------------------------------------
        // Check lot state
        //------------------------------------------------------
        log.debug("Check lot state");
        Validations.check (!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_STATE_FINISHED,objLotAllStateCheckSameRetCode.getLotState()), new OmCode(retCodeConfig.getInvalidLotStat(),objLotAllStateCheckSameRetCode.getLotState()));
        //------------------------------------------------------
        // Check lot hold state
        //------------------------------------------------------
        log.debug("Check lot hold state");
        Validations.check (!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD,objLotAllStateCheckSameRetCode.getLotHoldState()), retCodeConfig.getCannotMergeHeldlotInbank());
        //------------------------------------------------------
        // Check lot finished state
        //------------------------------------------------------
        log.debug("Check lot finished state");
        Validations.check (!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED,objLotAllStateCheckSameRetCode.getLotFinishedState()), new OmCode(retCodeConfig.getInvalidLotFinishStat(),objLotAllStateCheckSameRetCode.getLotFinishedState()));
        //------------------------------------------------------
        // Check lot process state
        //------------------------------------------------------
        log.debug("Check lot process state");
        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_PROCSTATE_PROCESSED,objLotAllStateCheckSameRetCode.getLotProcessState()), new OmCode(retCodeConfig.getInvalidLotProcessState(),mergeLotNotOnPfReqParams.getParentLotID().getValue(),objLotAllStateCheckSameRetCode.getLotProcessState()));

        //------------------------------------------------------
        // Check lot inventory state
        //------------------------------------------------------
        log.debug("Check lot inventory state");
        log.trace("!ObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,objLotAllStateCheckSameRetCode.getLotInventoryState()) : {}",
                !CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,objLotAllStateCheckSameRetCode.getLotInventoryState()));
        if (!CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,objLotAllStateCheckSameRetCode.getLotInventoryState())) {
            log.debug("lot_allState_CheckSame() returned lotInventoryState is not SP_Lot_InventoryState_InBank");
            Validations.check(retCodeConfig.getInvalidLotInventoryStat(),ObjectIdentifier.fetchValue(mergeLotNotOnPfReqParams.getParentLotID()),
                    objLotAllStateCheckSameRetCode.getLotProcessState());
        } else {
            String retCode = lotMethod.lotBankCheckSame(objCommon,mergeLotNotOnPfReqParams.getParentLotID(),mergeLotNotOnPfReqParams.getChildLotID());
        }
        //-----------------------------
        //  Check InPostProcessFlag
        //-----------------------------
        log.debug("Check InPostProcessFlag.");
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> userGroupIDs = new ArrayList<>();
        lotIDs.add(mergeLotNotOnPfReqParams.getParentLotID());
        lotIDs.add(mergeLotNotOnPfReqParams.getChildLotID());
        for (int i = 0; i < CimArrayUtils.getSize(lotIDs); i++) {
            Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotIDs.get(i));
            //----------------------------------------------
            //  If lot is in post process, returns error
            //----------------------------------------------
            log.debug("If lot is in post process, returns error BooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot()) : {}",
                    CimBooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot()));
            if (CimBooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot())) {
                log.trace("ArrayUtils.isEmpty(userGroupIDs)");
                if (CimArrayUtils.isEmpty(userGroupIDs)) {
                    /*---------------------------*/
                    /* Get UserGroupID By UserID */
                    /*---------------------------*/
                    List<ObjectIdentifier> userGroupListGetOutRetCode =  personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                    userGroupIDs = userGroupListGetOutRetCode;
                }
                int j ;
                for (j = 0; j < userGroupIDs.size(); j++) {
                }
                Validations.check (j == CimArrayUtils.getSize(userGroupIDs), new OmCode(retCodeConfig.getLotInPostProcess(),lotIDs.get(i).getValue()));
            }
        }
        /*------------------------------------------------------------------------*/
        /*   Check if the wafers in lot don't have machine container position     */
        /*------------------------------------------------------------------------*/
        log.debug("call equipmentContainerPosition_info_GetByLotDR( parentLot )");
        // for parentLot
        List<Infos.EqpContainerPosition> parentLotEquipmentContainerPositionInfoGetByLotDROut = equipmentMethod.equipmentContainerPositionInfoGetByLotDR(objCommon, mergeLotNotOnPfReqParams.getParentLotID());
        Validations.check (0 < CimArrayUtils.getSize(parentLotEquipmentContainerPositionInfoGetByLotDROut), new OmCode(retCodeConfig.getWaferInLotHaveContainerPosition(),mergeLotNotOnPfReqParams.getParentLotID().getValue()));

        // for childLotID
        List<Infos.EqpContainerPosition> childLotEquipmentContainerPositionInfoGetByLotDROut = equipmentMethod.equipmentContainerPositionInfoGetByLotDR(objCommon, mergeLotNotOnPfReqParams.getChildLotID());
        Validations.check (0 < CimArrayUtils.getSize(childLotEquipmentContainerPositionInfoGetByLotDROut), new OmCode(retCodeConfig.getWaferInLotHaveContainerPosition(),mergeLotNotOnPfReqParams.getChildLotID().getValue()));

        /*----------------------------------*/
        /*   Check lot's Control Job ID     */
        /*----------------------------------*/
        log.debug("Check lot's Control Job ID");
        // for parentLot
        ObjectIdentifier parentLotControlJobIDOut = lotMethod.lotControlJobIDGet(objCommon, mergeLotNotOnPfReqParams.getParentLotID());
        log.trace("StringUtils.isEmpty(parentLotControlJobIDOut) : {}", CimStringUtils.isEmpty(parentLotControlJobIDOut));
        if (CimStringUtils.isEmpty(parentLotControlJobIDOut)) {
            // do nothing
        } else {
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),mergeLotNotOnPfReqParams.getParentLotID().getValue(),parentLotControlJobIDOut.getValue()));
        }
        // for childLot
        ObjectIdentifier childLotControlJobIDOut = lotMethod.lotControlJobIDGet(objCommon, mergeLotNotOnPfReqParams.getChildLotID());
        log.trace("StringUtils.isEmpty(childLotControlJobIDOut) : {}", CimStringUtils.isEmpty(childLotControlJobIDOut));
        if (CimStringUtils.isEmpty(childLotControlJobIDOut)) {
            // do nothing
        } else {
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),mergeLotNotOnPfReqParams.getChildLotID().getValue(),childLotControlJobIDOut.getValue()));
        }
        String cassetteTransferState = null;
        log.trace("!ObjectUtils.isEmpty(aParentCassetteID) : {}",!CimObjectUtils.isEmpty(aParentCassetteID));
        if (!CimObjectUtils.isEmpty(aParentCassetteID)) {
            log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString()));
            if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString())) {
                log.debug("lotOperationEIcheck = 1");
                //-------------------------
                // Check carrier dispatch status
                //-------------------------
                log.debug("Check carrier dispatch status");
                Boolean castDisStateOut = cassetteMethod.cassetteDispatchStateGet(objCommon, aParentCassetteID);
                Validations.check (castDisStateOut, retCodeConfig.getNotFoundCst());
            }
            log.trace("(ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString())\n" +
                    "                    || (ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString())\n" +
                    "                    && !ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,objCassetteTransferStateGetOut)) : {}",
                    CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString())
                            || (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString())
                            && !CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,objCassetteTransferStateGetOut)));

            if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString())
                    || (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString())
                    && !CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,objCassetteTransferStateGetOut))) {
                //-------------------------
                // Check carrier transfer status
                //-------------------------
                log.debug("Check carrier transfer status");
                cassetteTransferState = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
                log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()));
                if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString())) {
                    log.debug("lotOperationEIcheck = 0");
                    Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferState), new OmCode(retCodeConfig.getChangedToEiByOtherOperation(),aParentCassetteID.getValue()));
                } else if (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferState)
                        || CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT,cassetteTransferState)){
                    log.debug("XferState is invalid...");
                    Validations.check(true, new OmCode(retCodeConfig.getInvalidCassetteTransferState(),
                            cassetteTransferState,aParentCassetteID.getValue()));
                }
            }
            log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()));
            if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString())) {
                log.debug("lotOperationEIcheck = 0");
                cassetteTransferState = objCassetteTransferStateGetOut;
                log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferState) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferState));
                if (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferState)) {
                    /*------------------------------------*/
                    /*   Get eqp port Info          */
                    /*------------------------------------*/
                    log.debug("Get eqp port Info");
                    Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, cassetteEqpOut.getEquipmentID());
                    int i;
                    for (i = 0; i < CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses()); i++) {
                        log.trace("ObjectUtils.equalsWithValue(aParentCassetteID.getValue(),eqpPortInfo.getEqpPortStatuses().get(i).getLoadedCassetteID().getValue()) : {}", CimObjectUtils.equalsWithValue(aParentCassetteID.getValue(),eqpPortInfo.getEqpPortStatuses().get(i).getLoadedCassetteID().getValue()));
                        if (CimObjectUtils.equalsWithValue(aParentCassetteID.getValue(),eqpPortInfo.getEqpPortStatuses().get(i).getLoadedCassetteID().getValue())) {
                            log.debug("parentCassetteID == loadedCassetteID");
                            break;
                        }
                    }
                    //-----------------------------------------------------------------
                    // Check parent lot and child lot is same operationStartFlag or not
                    //-----------------------------------------------------------------
                    log.debug("Check parent lot and child lot is same operationStartFlag or not");
                    Boolean bParentLotOpeStartFlg = false;
                    Boolean bChildLotOpeStartFlg  = false;
                    if (i < CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses())) {
                        for (int j = 0; j < CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses().get(i).getLotOnPorts()); j++) {
                            Infos.LotOnPort strLotOnPort = eqpPortInfo.getEqpPortStatuses().get(i).getLotOnPorts().get(j);
                            log.trace("ObjectUtils.equalsWithValue(mergeLotNotOnPfReqParams.getParentLotID().getValue(),strLotOnPort.getLotID().getValue())) : {}", CimObjectUtils.equalsWithValue(mergeLotNotOnPfReqParams.getParentLotID().getValue(),strLotOnPort.getLotID().getValue()));
                            log.trace("ObjectUtils.equalsWithValue(mergeLotNotOnPfReqParams.getChildLotID().getValue(),strLotOnPort.getLotID().getValue()) : {}", CimObjectUtils.equalsWithValue(mergeLotNotOnPfReqParams.getChildLotID().getValue(),strLotOnPort.getLotID().getValue()));
                            if (CimObjectUtils.equalsWithValue(mergeLotNotOnPfReqParams.getParentLotID().getValue(),strLotOnPort.getLotID().getValue())) {
                                bParentLotOpeStartFlg = strLotOnPort.isMoveInFlag();
                            } else if (CimObjectUtils.equalsWithValue(mergeLotNotOnPfReqParams.getChildLotID().getValue(),strLotOnPort.getLotID().getValue())) {
                                bChildLotOpeStartFlg = strLotOnPort.isMoveInFlag();
                            }
                        }
                    }
                    Validations.check (CimBooleanUtils.isTrue(bParentLotOpeStartFlg) || CimBooleanUtils.isFalse(bChildLotOpeStartFlg), new OmCode(retCodeConfig.getAttributeDifferentForMerge(),"operationStartFlag",(CimBooleanUtils.isTrue(bParentLotOpeStartFlg) ? "True":"False"),(CimBooleanUtils.isTrue(bChildLotOpeStartFlg) ? "True":"False")));
                }
            }
        }
        log.debug("!ObjectUtils.isEmpty(aChildCassetteID) : {}",!CimObjectUtils.isEmpty(aChildCassetteID));
        if (!CimObjectUtils.isEmpty(aChildCassetteID)) {
            log.debug("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString()));
            if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString())) {
                log.debug("lotOperationEIcheck = 1");
                //-------------------------
                // Check carrier dispatch status
                //-------------------------
                log.debug("Check carrier dispatch status");
                Boolean castDisStateOut = cassetteMethod.cassetteDispatchStateGet(objCommon, aChildCassetteID);
                Validations.check (CimBooleanUtils.isTrue(castDisStateOut), retCodeConfig.getNotFoundCst());
                //-------------------------
                // Check carrier transfer status
                //-------------------------
                log.debug("Check carrier transfer status");
                String cassetteTransferStateGet = cassetteMethod.cassetteTransferStateGet(objCommon, aChildCassetteID);
                Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateGet)
                        || CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT,cassetteTransferStateGet), new OmCode(retCodeConfig.getInvalidCassetteTransferState(),
                        cassetteTransferStateGet,aChildCassetteID.getValue()));
            }
        }

        //-------------------------
        // Check allocated quantities for parent lot
        //-------------------------
        log.debug("Check allocated quantities for parent lot");
        Inputs.ObjLotWafersGetIn objLotWafersGetIn = new Inputs.ObjLotWafersGetIn();
        objLotWafersGetIn.setLotID(mergeLotNotOnPfReqParams.getParentLotID());
        objLotWafersGetIn.setScrapCheckFlag(true);
        List<Infos.LotWaferInfoAttributes>  parentLotWaferInfoListGetDROut = lotMethod.lotWaferInfoListGetDR(objCommon, objLotWafersGetIn);

        Boolean bSTBAllocatedFlag = false;
        ObjectIdentifier aWaferID = new ObjectIdentifier();
        for (int i = 0; i < CimArrayUtils.getSize(parentLotWaferInfoListGetDROut); i++) {
            log.debug("BooleanUtils.isTrue(parentLotWaferInfoListGetDROut.get(i).getSTBAllocFlag()) : {}", CimBooleanUtils.isTrue(parentLotWaferInfoListGetDROut.get(i).getSTBAllocFlag()));
            if (CimBooleanUtils.isTrue(parentLotWaferInfoListGetDROut.get(i).getSTBAllocFlag())) {
                bSTBAllocatedFlag = true;
                aWaferID = parentLotWaferInfoListGetDROut.get(i).getWaferID();
                break;
            }
        }
        Validations.check (CimBooleanUtils.isTrue(bSTBAllocatedFlag), new OmCode(retCodeConfig.getWaferAllocated(),aWaferID.getValue()));
        //--------------------------------
        // Check hasWafer for parent lot
        //--------------------------------
        log.debug("Check hasWafer for parent lot");
        log.trace("ObjectUtils.isEmpty(parentLotWaferInfoListGetDROut.get(0).getWaferID().getValue()) : {}", CimObjectUtils.isEmpty(parentLotWaferInfoListGetDROut.get(0).getWaferID().getValue()));
        if (CimObjectUtils.isEmpty(parentLotWaferInfoListGetDROut.get(0).getWaferID().getValue())) {
            log.debug("bParentLotHasWaferID = FALSE");
            bParentLotHasWaferID = false;
        }else {
            log.debug("bParentLotHasWaferID = TRUE");
        }
        //-------------------------
        // Check allocated quantities for child lot
        //-------------------------
        log.debug("Check allocated quantities for child lot");
        Inputs.ObjLotWafersGetIn objLotWafersGetIn2 = new Inputs.ObjLotWafersGetIn();
        objLotWafersGetIn2.setLotID(mergeLotNotOnPfReqParams.getChildLotID());
        objLotWafersGetIn2.setScrapCheckFlag(true);
        List<Infos.LotWaferInfoAttributes> childLotWaferInfoListGetDROut = lotMethod.lotWaferInfoListGetDR(objCommon, objLotWafersGetIn2);

        bSTBAllocatedFlag  = false;
        for (int i = 0; i < CimArrayUtils.getSize(childLotWaferInfoListGetDROut); i++) {
            log.trace("BooleanUtils.isTrue(childLotWaferInfoListGetDROut.get(i).getSTBAllocFlag()) : {}", CimBooleanUtils.isTrue(childLotWaferInfoListGetDROut.get(i).getSTBAllocFlag()));
            if (CimBooleanUtils.isTrue(childLotWaferInfoListGetDROut.get(i).getSTBAllocFlag())) {
                bSTBAllocatedFlag = true;
                aWaferID = childLotWaferInfoListGetDROut.get(i).getWaferID();
                break;
            }
        }
        Validations.check (CimBooleanUtils.isTrue(bSTBAllocatedFlag), new OmCode(retCodeConfig.getWaferAllocated(),aWaferID.getValue()));
        //--------------------------------
        // Check hasWafer for child lot
        //--------------------------------
        log.debug("Check hasWafer for child lot");
        log.trace("ObjectUtils.isEmpty(childLotWaferInfoListGetDROut.get(0).getWaferID().getValue()) : {}", CimObjectUtils.isEmpty(childLotWaferInfoListGetDROut.get(0).getWaferID().getValue()));
        if (CimObjectUtils.isEmpty(childLotWaferInfoListGetDROut.get(0).getWaferID().getValue())) {
            log.debug("bChildLotHasWaferID = FALSE");
            bChildLotHasWaferID = false;
        }else {
            log.debug("bParentLotHasWaferID = TRUE");
        }
        //----------------------------------------------------------------------------------------
        // Check whether both ChildLot and ParentLot are in the cassette or not in the cassette.
        //----------------------------------------------------------------------------------------
        log.debug("Check whether both ChildLot and ParentLot are in the cassette or not in the cassette.");
        log.trace("BooleanUtils.isTrue(bParentLotHasWaferID) && BooleanUtils.isTrue(bChildLotHasWaferID) : {}", CimBooleanUtils.isTrue(bParentLotHasWaferID) && CimBooleanUtils.isTrue(bChildLotHasWaferID));
        log.trace("BooleanUtils.isFalse(bParentLotHasWaferID) && BooleanUtils.isFalse(bChildLotHasWaferID) : {}", CimBooleanUtils.isFalse(bParentLotHasWaferID) && CimBooleanUtils.isFalse(bChildLotHasWaferID));
        if (CimBooleanUtils.isTrue(bParentLotHasWaferID) && CimBooleanUtils.isTrue(bChildLotHasWaferID)) {
            log.debug("Both ParentLot and ChildLot are in the cassette. bHasWaferID = TRUE");
        } else if (CimBooleanUtils.isFalse(bParentLotHasWaferID) && CimBooleanUtils.isFalse(bChildLotHasWaferID)) {
            log.debug("Both ParentLot and ChildLot are not in the cassette. bHasWaferID = FALSE");
            bHasWaferID = false;
        } else {
            log.debug("Either ParentLot or ChildLot is not in the cassette and another lot is in the cassette.");
            Validations.check(true, retCodeConfig.getError());
        }
        //---------------------------------
        // Get Parent lot's Route ID (Main PD ID)
        //---------------------------------
        log.debug("Get Parent Lot's Route ID (Main PD ID)");
        ObjectIdentifier parentRouteID = lotMethod.lotRouteIdGet(objCommon, mergeLotNotOnPfReqParams.getParentLotID());
        Validations.check (!CimObjectUtils.isEmpty(parentRouteID), retCodeConfig.getLotOnRoute());
        //---------------------------------
        // Get Child lot's Route ID (Main PD ID)
        //---------------------------------
        log.debug("Get Child Lot's Route ID (Main PD ID)");
        ObjectIdentifier childRouteID = lotMethod.lotRouteIdGet(objCommon, mergeLotNotOnPfReqParams.getChildLotID());
        Validations.check(!CimObjectUtils.isEmpty(childRouteID), retCodeConfig.getLotOnRoute());
        //--------------------------------------------------------
        // Check input parent lot and child lot is family or not.
        //--------------------------------------------------------
        log.debug("Check input parent lot and child lot is family or not.");
        lotFamilyMethod.lotFamilyCheckMerge(objCommon, mergeLotNotOnPfReqParams.getParentLotID(),mergeLotNotOnPfReqParams.getChildLotID());

        //------------------------------------------------------------------------
        //   Create History Event before child lot's wafer become parent lot's wafer
        //------------------------------------------------------------------------
        //lotWaferMoveEvent_MakeMerge
        log.debug("Create History Event before child lot's wafer become parent lot's wafer");
        Infos.NewLotAttributes  lotWaferMoveEventMakeMergeResult = new Infos.NewLotAttributes();
        if (CimBooleanUtils.isTrue(bHasWaferID)){
            log.debug("Create History Event before child lot's wafer become parent lot's wafer");
            Inputs.LotWaferMoveEventMakeMergeParams lotWaferMoveEventMakeMergeParams = new Inputs.LotWaferMoveEventMakeMergeParams();
            lotWaferMoveEventMakeMergeParams.setDestinationLotID(mergeLotNotOnPfReqParams.getParentLotID());
            lotWaferMoveEventMakeMergeParams.setSourceLotID(mergeLotNotOnPfReqParams.getChildLotID());
            lotWaferMoveEventMakeMergeParams.setTransactionID(objCommon.getTransactionID());
            lotWaferMoveEventMakeMergeResult = eventMethod.lotWaferMoveEventMakeMerge(objCommon, lotWaferMoveEventMakeMergeParams);
        }

        /*------------------------------------------------------------------------*/
        /*  Check Lot Split Or Merge                                              */
        /*------------------------------------------------------------------------*/
        log.debug("Check Lot Split Or Merge");
        List<ObjectIdentifier> lotIDSeq = new ArrayList<>();
        lotIDSeq.add(mergeLotNotOnPfReqParams.getParentLotID());
        lotIDSeq.add(mergeLotNotOnPfReqParams.getChildLotID());
        this.checkLotSplitOrMerge(objCommon,lotIDSeq, BizConstant.CHECK_LOT_ACTION_MERGE);

        //--------------------
        //   Change State
        //--------------------
        log.debug("Change State");
        lotMethod.lotMergeWaferLotNotOnRoute(objCommon, mergeLotNotOnPfReqParams.getParentLotID(), mergeLotNotOnPfReqParams.getChildLotID());

        log.trace("!ObjectUtils.isEmpty(aParentCassetteID.getValue())\n" +
                "                && !ObjectUtils.isEmpty(aChildCassetteID.getValue()) : {}",
                !CimObjectUtils.isEmpty(aParentCassetteID.getValue())
                        && !CimObjectUtils.isEmpty(aChildCassetteID.getValue()));

        if (!CimObjectUtils.isEmpty(aParentCassetteID.getValue())
                && !CimObjectUtils.isEmpty(aChildCassetteID.getValue())) {
            log.trace("BooleanUtils.isTrue(updateControlJobFlag) : {}", CimBooleanUtils.isTrue(updateControlJobFlag));
            if (CimBooleanUtils.isTrue(updateControlJobFlag)) {
                List<ObjectIdentifier> cassettes = new ArrayList<>();
                cassettes.add(aParentCassetteID);
                controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassettes);
            }
            /*---------------------------------------*/
            /*   Update cassette's MultiLotType      */
            /*---------------------------------------*/
            log.debug("Update Cassette's MultiLotType");
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, aParentCassetteID);
        }
        log.debug("BooleanUtils.isTrue(bHasWaferID) : {}", CimBooleanUtils.isTrue(bHasWaferID));
        if (CimBooleanUtils.isTrue(bHasWaferID)) {
            //lotWaferMoveEvent_Make
            log.debug("make event for lot wafer move event merge result");
            Infos.NewLotAttributes newLotAttributes = lotWaferMoveEventMakeMergeResult;
            eventMethod.lotWaferMoveEventMake(objCommon, newLotAttributes, objCommon.getTransactionID(), mergeLotNotOnPfReqParams.getClaimMemo());
        }

        // check child and parent the contamination level and pr flag
        log.debug("check child and parent the contamination level and pr flag : {}");
        contaminationMethod.lotCheckContaminationLevelAndPrFlagMatchError(objCommon, mergeLotNotOnPfReqParams.getParentLotID(), mergeLotNotOnPfReqParams.getChildLotID());
    }

    @SuppressWarnings("deprecation")
    public void sxMergeLotReq(Infos.ObjCommon objCommon, Params.MergeLotReqParams mergeLotReqParams) {
        //------------------------------------------------------------------------
        //   Get cassette / lot connection
        //------------------------------------------------------------------------
        log.debug("Get cassette / lot connection");
        ObjectIdentifier aParentCassetteID = lotMethod.lotCassetteGet(objCommon,mergeLotReqParams.getParentLotID());

        ObjectIdentifier aChildCassetteID = lotMethod.lotCassetteGet(objCommon,mergeLotReqParams.getChildLotID());

        //--------------------------------
        //   CassetteID should be same!
        //--------------------------------
        log.debug("CassetteID should be same!");
        Validations.check (!CimStringUtils.equals(aChildCassetteID.getValue(), aParentCassetteID.getValue()), retCodeConfig.getCassetteNotSame());

        //--------------------------------
        //   Lock objects to be updated
        //--------------------------------
        log.debug(" Lock objects to be updated");
        String tmpLotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        tmpLotOperationEIcheck = null == tmpLotOperationEIcheck ? "0" : tmpLotOperationEIcheck;
        Integer lotOperationEIcheck = Integer.valueOf(tmpLotOperationEIcheck);
        Boolean updateControlJobFlag = false;
        Long lockMode = 0L;
        String cassetteTransferStateRetCode = null;
        Outputs.ObjCassetteEquipmentIDGetOut objCassetteEquipmentIDGetOut = null;
        log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !StringUtils.isEmpty(aParentCassetteID) : {},",
                CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !CimStringUtils.isEmpty(aParentCassetteID));
        if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !CimStringUtils.isEmpty(aParentCassetteID)) {
            //-------------------------------
            // Get carrier transfer status
            //-------------------------------
            log.debug("Get carrier transfer status");
            cassetteTransferStateRetCode = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
            /*------------------------------------*/
            /*   Get eqp ID in cassette     */
            /*------------------------------------*/
            log.debug("Get eqp ID in cassette");
            objCassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, aParentCassetteID);
            //-------------------------------
            // Get required eqp lock mode
            //-------------------------------
            // object_lockMode_Get
            log.debug(" Get required eqp lock mode");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(objCassetteEquipmentIDGetOut.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.MERGE_WAFER_LOT_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            log.trace("StringUtils.equals(cassetteTransferStateRetCode, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) : {}", CimStringUtils.equals(cassetteTransferStateRetCode, BizConstant.SP_TRANSSTATE_EQUIPMENTIN));
            if (CimStringUtils.equals(cassetteTransferStateRetCode, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                updateControlJobFlag = true;
                // advanced_object_Lock
                log.debug("advanced_object_Lock");
                log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                    //  advanced_object_Lock
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                            (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, Collections.singletonList(aParentCassetteID.getValue())));
                } else {
                    // object_Lock
                    objectLockMethod.objectLock(objCommon, CimMachine.class, objCassetteEquipmentIDGetOut.getEquipmentID());
                }
            }
        }
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimCassette.class, aParentCassetteID);
        log.trace("StringUtils.equals(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !StringUtils.isEmpty(aParentCassetteID) : {}",
                CimStringUtils.equals(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !CimStringUtils.isEmpty(aParentCassetteID));
        if (CimStringUtils.equals(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !CimStringUtils.isEmpty(aParentCassetteID)) {
            // object_Lock
            log.trace("!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
            if (!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                //---------------------------------
                //   Get Cassette's ControlJobID
                //---------------------------------
                log.debug("Get Cassette's ControlJobID");
                ObjectIdentifier controlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, aParentCassetteID);
                log.trace("!ObjectUtils.isEmptyWithValue(controlJobID) : {}",!CimObjectUtils.isEmptyWithValue(controlJobID));
                if (!CimObjectUtils.isEmptyWithValue(controlJobID)){
                    updateControlJobFlag = true;
                    log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                    if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                        /*------------------------------*/
                        /*   Lock ControlJob Object     */
                        /*------------------------------*/
                        log.debug(" Lock ControlJob Object");
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
                    }
                }
            }
        }
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimCassette.class, aChildCassetteID);
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimLot.class, mergeLotReqParams.getParentLotID());
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimLot.class, mergeLotReqParams.getChildLotID());
        //------------------------------------
        // Check LOCK Hold.
        //------------------------------------
        log.debug("Check LOCK Hold. ");
        List<ObjectIdentifier> lotIDSeq = new ArrayList<>();
        lotIDSeq.add(mergeLotReqParams.getParentLotID());
        lotIDSeq.add(mergeLotReqParams.getChildLotID());
        for (int i = 0; i < CimArrayUtils.getSize(lotIDSeq); i++) {
            //----------------------------------
            //  Check lot InterFabXfer state
            //----------------------------------
            String interFabXferStateGet = lotMethod.lotInterFabXferStateGet(objCommon, lotIDSeq.get(i));
            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferStateGet) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferStateGet));
            if (CimObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferStateGet)) {
                log.debug(" #### The Lot interFabXfer state is required... No need to check LOCK Hold. ");
                continue;
            }
            //lot_CheckLockHoldConditionForOperation
            log.debug("lot_CheckLockHoldConditionForOperation");
            lotMethod.lotCheckLockHoldConditionForOperation(objCommon,lotIDSeq);
        }

        /*------------------------------------------------------------------------*/
        /*  Check Lot Split Or Merge                                              */
        /*------------------------------------------------------------------------*/
        log.debug("Check Lot Split Or Merge");
        this.checkLotSplitOrMerge(objCommon,lotIDSeq, BizConstant.CHECK_LOT_ACTION_MERGE);

        //-----------------------------
        //  Check InPostProcessFlag
        //-----------------------------
        log.debug("Check InPostProcessFlag.");
        List<ObjectIdentifier> userGroupIDs = new ArrayList<>();
        for (ObjectIdentifier s : lotIDSeq) {
            //----------------------------------
            //  Get InPostProcessFlag of lot
            //----------------------------------
            log.debug("Get InPostProcessFlag of lot");
            Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, s);
            //----------------------------------
            //  Check lot InterFabXfer state
            //----------------------------------
            log.debug("Check lot InterFabXfer state");
            String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, s);
            //----------------------------------------------
            //  If lot is in post process, returns error
            //----------------------------------------------
            log.debug("If lot is in post process, returns error");
            log.trace("lotInPostProcessFlagOut.getInPostProcessFlagOfLot() : {}",lotInPostProcessFlagOut.getInPostProcessFlagOfLot());
            if (lotInPostProcessFlagOut.getInPostProcessFlagOfLot()) {
                log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferState) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferState));
                if (CimObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferState)) {
                    log.debug(" #### The Lot interFabXfer state is required... No need to check post process flag. ");
                    continue;
                }
                log.trace("0 == ArrayUtils.getSize(userGroupIDs) : {}",0 == CimArrayUtils.getSize(userGroupIDs));
                if (0 == CimArrayUtils.getSize(userGroupIDs)) {
                    /*---------------------------*/
                    /* Get UserGroupID By UserID */
                    /*---------------------------*/
                    log.debug("Get UserGroupID By UserID");
                    userGroupIDs =  personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                }
                int j = 0;
                for (j = 0; j < CimArrayUtils.getSize(userGroupIDs); j++) {
                }
                Validations.check (j == CimArrayUtils.getSize(userGroupIDs), retCodeConfig.getLotInPostProcess());
            }
        }

        /*-------------------------------*/
        /*   Check SorterJob existence   */
        /*-------------------------------*/
        log.debug("Check SorterJob existence");
        List<ObjectIdentifier> dummyCastIDs = new ArrayList<>();
        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(new Infos.EquipmentLoadPortAttribute());
        objWaferSorterJobCheckForOperation.setCassetteIDList(dummyCastIDs);
        objWaferSorterJobCheckForOperation.setLotIDList(lotIDSeq);
        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);

        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);

        //---------------------------------
        // Check carrier dispatch status
        //---------------------------------
        log.debug("Check carrier dispatch status");
        String transferStateGetOutRetCode = null;
        log.trace("!StringUtils.isEmpty(aParentCassetteID) : {}",!CimStringUtils.isEmpty(aParentCassetteID));
        if (!CimStringUtils.isEmpty(aParentCassetteID)) {
            log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString()));
            if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString())) {
                Boolean castDisStateOut = cassetteMethod.cassetteDispatchStateGet(objCommon, aParentCassetteID);
                Validations.check (castDisStateOut, retCodeConfig.getNotFoundCst());
            }
            //---------------------------------
            // Check carrier transfer status
            //---------------------------------
            log.debug("Check carrier transfer status");
            log.trace("!StringUtils.isEmpty(aParentCassetteID)\n" +
                    "                    || (StringUtils.isEmpty(aParentCassetteID))\n" +
                    "                    && !ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateRetCode) : {}",!CimStringUtils.isEmpty(aParentCassetteID)
                    || (CimStringUtils.isEmpty(aParentCassetteID))
                    && !CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateRetCode));
            if (!CimStringUtils.isEmpty(aParentCassetteID)
                    || (CimStringUtils.isEmpty(aParentCassetteID))
                    && !CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateRetCode)) {
                transferStateGetOutRetCode = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
                log.trace("StringUtils.isEmpty(aParentCassetteID) : {}", CimStringUtils.isEmpty(aParentCassetteID));
                if (CimStringUtils.isEmpty(aParentCassetteID)) {
                    log.debug("Changed to EI by other operation");
                    Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateRetCode), retCodeConfig.getChangedToEiByOtherOperation());
                } else if (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT,transferStateGetOutRetCode)
                        || CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,transferStateGetOutRetCode)) {
                    log.debug("XferState is invalid...");
                    Validations.check(true, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferStateGetOutRetCode, CimObjectUtils.getObjectValue(aParentCassetteID)));
                }
            }
            log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()));
            if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString())) {
                transferStateGetOutRetCode = cassetteTransferStateRetCode;
            }
        }
        //------------------------------------------------------------------------
        //   Check Condition
        //------------------------------------------------------------------------
        //---------------------------------
        // Check Contents for parent lot
        //---------------------------------
        log.debug("Check Contents for parent lot");
        String lotContents = lotMethod.lotContentsGet(mergeLotReqParams.getParentLotID());

        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER,lotContents)
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE,lotContents) , retCodeConfig.getInvalidLotContents());
        //---------------------------------
        // Check Contents for child lot
        //---------------------------------
        log.debug("Check Contents for child lot");
        String lotContentsChild = lotMethod.lotContentsGet(mergeLotReqParams.getChildLotID());
        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER,lotContentsChild)
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE,lotContentsChild) , retCodeConfig.getInvalidLotContents());
        //---------------------------------------
        // Check Finished State for parent lot
        //---------------------------------------
        log.debug("Check Finished State for parent lot");
        String lotFinishedStateParent = lotMethod.lotFinishedStateGet(objCommon, mergeLotReqParams.getParentLotID());
        Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_FINISHED_STATE_STACKED,lotFinishedStateParent), retCodeConfig.getInvalidLotFinishStat());
        //---------------------------------------
        // Check Finished State for child lot
        //---------------------------------------
        log.debug("Check Finished State for child lot");
        String lotFinishedStateChild = lotMethod.lotFinishedStateGet(objCommon, mergeLotReqParams.getChildLotID());
        Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_FINISHED_STATE_STACKED,lotFinishedStateChild), retCodeConfig.getInvalidLotFinishStat());

        //---------------------------------------
        // Check Bonding Group for parent lot
        //---------------------------------------
        log.debug("Check Bonding Group for parent lot");
        String groupIDGetDROut = lotMethod.lotBondingGroupIDGetDR(objCommon, mergeLotReqParams.getParentLotID());

        log.trace("!StringUtils.isEmpty(groupIDGetDROut) : {}",!CimStringUtils.isEmpty(groupIDGetDROut));
        if (!CimStringUtils.isEmpty(groupIDGetDROut)) {
            throw new ServiceException(new OmCode(retCodeConfig.getLotHasBondingGroup(),mergeLotReqParams.getParentLotID().getValue(),groupIDGetDROut));
        }
        //---------------------------------------
        // Check Bonding Group for child lot
        //---------------------------------------
        log.debug("Check Bonding Group for child lot");
        String groupIDGetDROut1 = lotMethod.lotBondingGroupIDGetDR(objCommon, mergeLotReqParams.getChildLotID());
        log.trace("!StringUtils.isEmpty(groupIDGetDROut1) : {}",!CimStringUtils.isEmpty(groupIDGetDROut1));
        if (!CimStringUtils.isEmpty(groupIDGetDROut1)) {
            throw new ServiceException(new OmCode(retCodeConfig.getLotHasBondingGroup(),mergeLotReqParams.getChildLotID().getValue(),groupIDGetDROut));
        }
        //------------------------------------------------------
        // Check parent lot and child lot is same state or not
        //------------------------------------------------------
        log.debug("Check parent lot and child lot is same state or not");
        Outputs.ObjLotAllStateCheckSame objLotAllStateCheckSameRetCode = null;
        try {
            objLotAllStateCheckSameRetCode = lotMethod.lotAllStateCheckSame(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());
        } catch (ServiceException e) {
            objLotAllStateCheckSameRetCode = (Outputs.ObjLotAllStateCheckSame)e.getData();
            log.trace("!\"HOLDSTATE\".equals(objLotAllStateCheckSameRetCode.getLotHoldState()) : {}",!"HOLDSTATE".equals(objLotAllStateCheckSameRetCode.getLotHoldState()));
            if (!"HOLDSTATE".equals(objLotAllStateCheckSameRetCode.getLotHoldState())) {
                throw e;
            }
        }

        //------------------------------------------------------
        // Check lot state
        //------------------------------------------------------
        log.debug("Check lot state");
        log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,objLotAllStateCheckSameRetCode.getLotInventoryState())\n" +
                "                || ObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK,objLotAllStateCheckSameRetCode.getLotInventoryState()) : {}",
                CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,objLotAllStateCheckSameRetCode.getLotInventoryState())
                        || CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK,objLotAllStateCheckSameRetCode.getLotInventoryState()));
        if (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,objLotAllStateCheckSameRetCode.getLotInventoryState())
                || CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK,objLotAllStateCheckSameRetCode.getLotInventoryState())) {
            log.debug("Check BankID.");
            //------------------------------------------------------
            // Check BankID.
            //------------------------------------------------------
            String retCode = lotMethod.lotBankCheckSame(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());
        }
        //--- Check for lot State -----//
        log.debug("Check for lot State");
        Validations.check (CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_STATE_SHIPPED,objLotAllStateCheckSameRetCode.getLotState()), retCodeConfig.getInvalidLotStat());
        //--- Check for lot Process State -----//
        log.debug("Check for lot Process State");
        Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_PROCSTATE_PROCESSING,objLotAllStateCheckSameRetCode.getLotProcessState()), retCodeConfig.getInvalidLotProcstat());
        /*-------------------------------*/
        /*   Check flowbatch Condition   */
        /*-------------------------------*/
        //lot_flowBatchID_Get
        log.debug("Check FlowBatch Condition  [ParentLot]");
        try {
            ObjectIdentifier flowBatchIDParentLot = lotMethod.lotFlowBatchIDGet(objCommon, mergeLotReqParams.getParentLotID());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(),e.getCode())){
                log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
                throw new ServiceException(retCodeConfig.getFlowBatchLimitation());
            }else if(Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(),e.getCode())){
                log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
            }else{
                log.debug("lot_flowBatchID_Get() != RC_OK");
                throw e;
            }
        }

        //lot_flowBatchID_Get
        log.debug("Check FlowBatch Condition  [ChildLot]");
        try {
            ObjectIdentifier flowBatchIDChildLot = lotMethod.lotFlowBatchIDGet(objCommon, mergeLotReqParams.getChildLotID());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(),e.getCode())){
                log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
                throw new ServiceException(retCodeConfig.getFlowBatchLimitation());
            }else if(Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(),e.getCode())){
                log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
            }else{
                log.debug("lot_flowBatchID_Get() != RC_OK");
                throw e;
            }
        }
        /*------------------------------------------------------------------------*/
        /*   Check if the wafers in lot don't have machine container position     */
        /*------------------------------------------------------------------------*/
        log.debug("call equipmentContainerPosition_info_GetByLotDR( parentLot )");
        List<Infos.EqpContainerPosition> eqpContainerPositionRetCode = equipmentMethod.equipmentContainerPositionInfoGetByLotDR(objCommon, mergeLotReqParams.getParentLotID());
        Validations.check (0 < CimArrayUtils.getSize(eqpContainerPositionRetCode), retCodeConfig.getWaferInLotHaveContainerPosition());
        // for childLotID
        log.debug("call equipmentContainerPosition_info_GetByLotDR( childLot )");
        List<Infos.EqpContainerPosition> eqpContainerPositionRetCode1 = equipmentMethod.equipmentContainerPositionInfoGetByLotDR(objCommon, mergeLotReqParams.getChildLotID());
        Validations.check (0 < CimArrayUtils.getSize(eqpContainerPositionRetCode1), retCodeConfig.getWaferInLotHaveContainerPosition());
        //----------------------------------
        //   Check lot's Control Job ID
        //----------------------------------
        // for parentLot
        log.debug("Check lot's Control Job ID");
        ObjectIdentifier controlJobIDOut = lotMethod.lotControlJobIDGet(objCommon, mergeLotReqParams.getParentLotID());
        log.trace("!ObjectUtils.isEmpty(controlJobIDOut) : {}",!CimObjectUtils.isEmpty(controlJobIDOut));
        if (!CimObjectUtils.isEmpty(controlJobIDOut)){
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),controlJobIDOut.getValue()));
        }
        // for childLotID
        ObjectIdentifier controlJobIDOut1 = lotMethod.lotControlJobIDGet(objCommon,mergeLotReqParams.getChildLotID());
        log.trace("!ObjectUtils.isEmpty(controlJobIDOut1) : {}",!CimObjectUtils.isEmpty(controlJobIDOut1));
        if (!CimObjectUtils.isEmpty(controlJobIDOut1)){
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),controlJobIDOut1.getValue()));
        }
        log.trace("!StringUtils.isEmpty(aParentCassetteID)");
        if (!CimStringUtils.isEmpty(aParentCassetteID)) {
            log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()));
            if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString())) {
                log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,transferStateGetOutRetCode) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,transferStateGetOutRetCode));
                if (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,transferStateGetOutRetCode)) {
                    /*------------------------------------*/
                    /*   Get eqp port Info          */
                    /*------------------------------------*/
                    log.debug("Get eqp port Info ");
                    Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, objCassetteEquipmentIDGetOut.getEquipmentID());
                    Integer portLen = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                    int i = 0;
                    for (i = 0; i < portLen; i++) {
                        log.trace("ObjectUtils.equalsWithValue(aParentCassetteID.getValue(),eqpPortInfo.getEqpPortStatuses().get(i).getLoadedCassetteID()) : {}", CimObjectUtils.equalsWithValue(aParentCassetteID.getValue(),eqpPortInfo.getEqpPortStatuses().get(i).getLoadedCassetteID()));
                        if (CimObjectUtils.equalsWithValue(aParentCassetteID.getValue(),eqpPortInfo.getEqpPortStatuses().get(i).getLoadedCassetteID())) {
                            log.debug("parentCassetteID == loadedCassetteID");
                            break;
                        }
                    }
                    //-----------------------------------------------------------------
                    // Check parent lot and child lot is same operationStartFlag or not
                    //-----------------------------------------------------------------
                    log.debug("Check parent lot and child lot is same operationStartFlag or not");
                    boolean bParentLotOpeStartFlg = false;
                    boolean bChildLotOpeStartFlg = false;
                    if (i < portLen) {
                        for (int j = 0; j < CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses().get(i).getLotOnPorts()); j++) {
                            Infos.LotOnPort lotOnPort = eqpPortInfo.getEqpPortStatuses().get(i).getLotOnPorts().get(j);
                            log.trace("ObjectUtils.equalsWithValue(mergeLotReqParams.getParentLotID(),lotOnPort.getLotID()) : {}", CimObjectUtils.equalsWithValue(mergeLotReqParams.getParentLotID(),lotOnPort.getLotID()));
                            if (CimObjectUtils.equalsWithValue(mergeLotReqParams.getParentLotID(),lotOnPort.getLotID())) {
                                log.debug("parentLotID == lotID");
                                bParentLotOpeStartFlg = lotOnPort.isMoveInFlag();
                            } else if (CimObjectUtils.equalsWithValue(mergeLotReqParams.getChildLotID(),lotOnPort.getLotID())) {
                                log.debug("childLotID == lotID");
                                bChildLotOpeStartFlg = lotOnPort.isMoveInFlag();
                            }
                        }
                    }
                    Validations.check (bChildLotOpeStartFlg != bParentLotOpeStartFlg , retCodeConfig.getAttributeDifferentForMerge());
                }
            }
        }
        lotFamilyMethod.lotFamilyCheckMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

        processMethod.processCheckMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());
        String holdListCheckMerge = lotMethod.lotHoldListCheckMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

        List<Infos.LotHoldReq>  lotFutureHoldRequestsCheckMerge = lotMethod.lotFutureHoldRequestsCheckMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

        log.trace("!ArrayUtils.isEmpty(lotFutureHoldRequestsCheckMerge) : {}",!CimArrayUtils.isEmpty(lotFutureHoldRequestsCheckMerge));
        if (!CimArrayUtils.isEmpty(lotFutureHoldRequestsCheckMerge)) {
            Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
            holdLotReleaseReqParams.setReleaseReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGE));
            holdLotReleaseReqParams.setLotID(mergeLotReqParams.getChildLotID());
            holdLotReleaseReqParams.setHoldReqList(lotFutureHoldRequestsCheckMerge);
            holdLotReleaseReqParams.setUser(objCommon.getUser());
            sxHoldLotReleaseReq(objCommon,holdLotReleaseReqParams);

            // sampling , checking sampling after hold release
            log.debug("sampling , checking sampling after hold release");
            samplingService.sxLotSamplingCheckThenSkipReq(objCommon, mergeLotReqParams.getChildLotID(), TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue(), BizConstant.LS_CHILD_LOT_EXECUTE);

            for (int i = 0; i < CimArrayUtils.getSize(lotFutureHoldRequestsCheckMerge); i++) {
                lotFutureHoldRequestsCheckMerge.get(i).setRelatedLotID(mergeLotReqParams.getChildLotID());
            }
            Params.HoldLotReleaseReqParams holdLotReleaseReqParams1 = new Params.HoldLotReleaseReqParams();
            holdLotReleaseReqParams1.setReleaseReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGE));
            holdLotReleaseReqParams1.setLotID(mergeLotReqParams.getParentLotID());
            holdLotReleaseReqParams1.setHoldReqList(lotFutureHoldRequestsCheckMerge);
            holdLotReleaseReqParams1.setUser(objCommon.getUser());
            sxHoldLotReleaseReq(objCommon,holdLotReleaseReqParams1);

            // sampling , checking sampling after hold release
            log.debug(" sampling , checking sampling after hold release");
            samplingService.sxLotSamplingCheckThenSkipReq(objCommon, mergeLotReqParams.getParentLotID(), TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue(), BizConstant.LS_BASIC_LOT_EXECUTE);

        }
        /*-----------------------------------*/
        /*   Check Future Action Procedure   */
        /*-----------------------------------*/
        // schdlChangeReservation_CheckForMerge
        log.debug("Check Future Action Procedure :: schdlChangeReservation_CheckForMerge");
        scheduleChangeReservationMethod.schdlChangeReservationCheckForMerge(objCommon,mergeLotReqParams.getParentLotID(),mergeLotReqParams.getChildLotID());

        /*----------------------------------------*/
        /*   Check Q-Time information condition   */
        /*----------------------------------------*/
        // qTime_CheckForMerge
        log.debug("Check Q-Time information condition :: qTime_CheckForMerge");
        qTimeMethod.qTimeCheckForMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

        //------------------------------------------------------------------------
        //   //DCR4000125 Create History Event before child lot's wafer become parent lot's wafer
        //   lotWaferMoveEvent_MakeMerge() only prepare merge event information.
        //------------------------------------------------------------------------
        // lotWaferMoveEvent_MakeMerge
        log.debug("lotWaferMoveEvent_MakeMerge");
        Inputs.LotWaferMoveEventMakeMergeParams lotWaferMoveEventMakeMergeParams = new Inputs.LotWaferMoveEventMakeMergeParams();
        lotWaferMoveEventMakeMergeParams.setTransactionID(objCommon.getTransactionID());
        lotWaferMoveEventMakeMergeParams.setSourceLotID(mergeLotReqParams.getChildLotID());
        lotWaferMoveEventMakeMergeParams.setDestinationLotID(mergeLotReqParams.getParentLotID());
        Infos.NewLotAttributes newLotAttributesMergeOut = eventMethod.lotWaferMoveEventMakeMerge(objCommon, lotWaferMoveEventMakeMergeParams);

        //------------------------------------------------------------------------
        //   Change State
        //------------------------------------------------------------------------
        //P5100296 Add Start
        //---------------------------------------------------------------------------------
        //  If Child lot is member of Monitor Group,
        //  the following action is performed according to the rule.
        //
        //           |                | Child lot is Monitored lot
        //           | Child lot is   +----------------------+---------------------------
        //    Rule   | Monitoring lot | only 1 Monitored lot | more than 2 Monitored lot
        //   --------+----------------+----------------------+---------------------------
        //    Action | return ERROR   | return ERROR         | remove from Monitor Group
        //---------------------------------------------------------------------------------
        //lot_RemoveFromMonitorGroup
        lotMethod.lotRemoveFromMonitorGroup(objCommon, mergeLotReqParams.getChildLotID());

        String lotMergeWaferLot = lotMethod.lotMergeWaferLot(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

        /*----------------------------------------*/
        /*   Merge Q-Time information             */
        /*----------------------------------------*/
        // qTime_infoMerge
        log.debug("Merge Q-Time information");
        qTimeMethod.qTimeInfoMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

        // Min q-time of merging sub Lot and parent Lot
        minQTimeMethod.lotMerge(mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

        //---------------------------------------
        //   Update cassette's MultiLotType
        //---------------------------------------
        log.debug("Update cassette's MultiLotType");
        log.trace("!StringUtils.isEmpty(aParentCassetteID) : {}",!CimStringUtils.isEmpty(aParentCassetteID));
        if (!CimStringUtils.isEmpty(aParentCassetteID)) {
            log.trace("updateControlJobFlag : {}",updateControlJobFlag);
            if (updateControlJobFlag) {
                //----------------------
                // Update control Job Info and
                // Machine cassette info if information exist
                //----------------------
                List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
                cassetteIDs.add(aParentCassetteID);
                controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDs);
            }
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, aParentCassetteID);
        }
        //------------------------------------------------------------------------
        //   Make History
        //------------------------------------------------------------------------
        log.debug("Make History");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, mergeLotReqParams.getParentLotID());

        // Get Entity Inhibition Info
        log.debug("Get Entity Inhibition Info");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon,mergeLotReqParams.getParentLotID());
        //for Parent lot, for later generating OPEHS

        // lotWaferMoveEvent_Make
        log.debug("create Event");
        Infos.NewLotAttributes newLotAttributes = newLotAttributesMergeOut;
        eventMethod.lotWaferMoveEventMake(objCommon, newLotAttributes, objCommon.getTransactionID(), mergeLotReqParams.getClaimMemo());
        //-- Entity Inhibit Exception Lot Data --//
        // TODO: 2019/10/10
        List<CimRestriction> entityInhibitList = entityInhibitManager.getEntityInhibitsWithExceptionLotByLot(mergeLotReqParams.getChildLotID());
        if (!CimObjectUtils.isEmpty(entityInhibitList)){
            Params.MfgRestrictExclusionLotReqParams cancelParams = new Params.MfgRestrictExclusionLotReqParams();
            List<Infos.EntityInhibitExceptionLot> entityInhibitExceptionLotList = new ArrayList<>();
            cancelParams.setEntityInhibitExceptionLots(entityInhibitExceptionLotList);
            for (CimRestriction cimEntityInhibit : entityInhibitList){
                Infos.EntityInhibitExceptionLot entityInhibitExceptionLot = new Infos.EntityInhibitExceptionLot();
                entityInhibitExceptionLot.setEntityInhibitID(new ObjectIdentifier(cimEntityInhibit.getIdentifier(),cimEntityInhibit.getPrimaryKey()));
                entityInhibitExceptionLot.setLotID(mergeLotReqParams.getChildLotID());
                entityInhibitExceptionLotList.add(entityInhibitExceptionLot);
            }
            String claimMemo = "Delete for MergeWaferLot";
            cancelParams.setClaimMemo(claimMemo);
            cancelParams.setUser(mergeLotReqParams.getUser());
            constraintService.sxMfgRestrictExclusionLotCancelReq(cancelParams, objCommon);

        }

        // check child and parent the contamination level and pr flag
        log.debug("check child and parent the contamination level and pr flag");
        contaminationMethod.lotCheckContaminationLevelAndPrFlagMatchError(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

    }

    @SuppressWarnings("deprecation")
    public void sxSkipReq(Infos.ObjCommon objCommon, Params.SkipReqParams skipReqParams) {
        ObjectIdentifier lotID = skipReqParams.getLotID();

        CimLot lot= baseCoreFactory.getBO(CimLot.class, lotID);

        Validations.check (null == lot, new OmCode(retCodeConfig.getNotFoundLot(),lotID.getValue()));
        Boolean cassetteCheckFlag = true;
        Boolean backupOperationFlag = false;
        ObjectIdentifier aCassetteID = null;
        Infos.ProcessRef processRef = new Infos.ProcessRef();
        processRef.setMainProcessFlow(skipReqParams.getProcessRef().getMainProcessFlow());
        processRef.setModuleNumber(skipReqParams.getProcessRef().getModuleNumber());
        processRef.setModulePOS(skipReqParams.getProcessRef().getModulePOS());
        processRef.setModuleProcessFlow(skipReqParams.getProcessRef().getModuleProcessFlow());
        processRef.setProcessFlow(skipReqParams.getProcessRef().getProcessFlow());
        processRef.setProcessOperationSpecification(skipReqParams.getProcessRef().getProcessOperationSpecification());
        log.trace("\"TXBOC009\".equals(objCommon.getTransactionID()) : {}","TXBOC009".equals(objCommon.getTransactionID()));
        if ("TXBOC009".equals(objCommon.getTransactionID())) {
            backupOperationFlag = true;
            log.trace("CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED.equals(lot.getLotFinishedState()) : {}",CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED.equals(lot.getLotFinishedState()));
            if (CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED.equals(lot.getLotFinishedState())) {
                cassetteCheckFlag = false;
            }
        }
        Boolean bParallelPostProcFlag = false;
        String strParallelPostProcFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG);
        log.trace("BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON.equals(strParallelPostProcFlag) : {}",BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON.equals(strParallelPostProcFlag));
        if (BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON.equals(strParallelPostProcFlag)) {
            bParallelPostProcFlag = true;
        }
        log.trace("cassetteCheckFlag : {}",cassetteCheckFlag);
        if (cassetteCheckFlag) {
            /*------------------------------------------*/
            /* step 1 - Get cassette / lot connection   */
            /*------------------------------------------*/
            log.debug("step 1 - Get cassette / lot connection");
            aCassetteID = lotMethod.lotCassetteGet(objCommon, lotID);

            /*--------------------------------*/
            /* step 2 - Lock objects to be updated   */
            /*--------------------------------*/
            log.debug("step 2 - Lock objects to be updated");
            //----------------------------------------------------------//
            //  step 3 - Skip cassette lock to increase parallel availability   //
            //   under PostProcess parallel execution                   //
            //----------------------------------------------------------//
            log.debug(" step 3 - Skip cassette lock to increase parallel availability ");
            if (!bParallelPostProcFlag) {
                // lock cassette
                objectLockMethod.objectLock(objCommon, CimCassette.class, aCassetteID);
            }
        }

        // lock lot
        log.debug(" lock lot");
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);
        log.trace("bParallelPostProcFlag : {}",bParallelPostProcFlag);
        if (bParallelPostProcFlag) {
            //-----------------------------------------------------------------------------------//
            //  step 4 -  Lock flowbatch to keep data inconsistency under PostProcess parallel execution  //
            //-----------------------------------------------------------------------------------//
            // lock FlowBatchID
            log.debug("step 4 -  Lock flowbatch to keep data inconsistency under PostProcess parallel execution");
            ObjectIdentifier flowBatchID = null;
            try {
                flowBatchID = lotMethod.lotFlowBatchIDGet(objCommon, lotID);
            } catch (ServiceException e) {
                log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()));
                if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())){
                    flowBatchID = e.getData(ObjectIdentifier.class);
                    objectLockMethod.objectLock(objCommon, CimFlowBatch.class, flowBatchID);
                } else if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())){
                    log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
                } else {
                    throw e;
                }
            }

        }
        //--------------------------------------------------------------------------
        //  step 5 - Check whether lot is on the specified Route/Operation or Not
        //--------------------------------------------------------------------------
        log.debug("step 5 - Check whether lot is on the specified Route/Operation or Not");
        String tmpCurrentRouteID = CimObjectUtils.getObjectValue(skipReqParams.getCurrentRouteID());
        String tmpCurrentOperationNumber = skipReqParams.getCurrentOperationNumber();
        log.debug("!StringUtils.isEmpty(tmpCurrentOperationNumber) && !StringUtils.isEmpty(tmpCurrentRouteID) : {}",!CimStringUtils.isEmpty(tmpCurrentOperationNumber) && !CimStringUtils.isEmpty(tmpCurrentRouteID));
        if (!CimStringUtils.isEmpty(tmpCurrentOperationNumber) && !CimStringUtils.isEmpty(tmpCurrentRouteID)) {
            Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoResultRetCode = lotMethod.lotCurrentOperationInfoGet(objCommon,lotID);
            log.debug("ObjectUtils.equalsWithValue(tmpCurrentRouteID, lotCurrentOperationInfoResultRetCode.getRouteID())\n" +
                    "                    && StringUtils.equals(tmpCurrentOperationNumber, lotCurrentOperationInfoResultRetCode.getOperationNumber()) : {}",
                    CimObjectUtils.equalsWithValue(tmpCurrentRouteID, lotCurrentOperationInfoResultRetCode.getRouteID())
                            && CimStringUtils.equals(tmpCurrentOperationNumber, lotCurrentOperationInfoResultRetCode.getOperationNumber()));
            if (CimObjectUtils.equalsWithValue(tmpCurrentRouteID, lotCurrentOperationInfoResultRetCode.getRouteID())
                    && CimStringUtils.equals(tmpCurrentOperationNumber, lotCurrentOperationInfoResultRetCode.getOperationNumber())){
                log.debug("Route/Operation check OK. Go ahead...");
            } else {
                log.debug("Route/Operation check NG.");
                throw new ServiceException(new OmCode(retCodeConfig.getNotSameRoute(),
                        "Input parameter's currentRouteID/currentOperationNumber",
                        "lot's current currentRouteID/currentOperationNumber"));
            }
        }
        /*--------------------------------*/
        /*  step 6 -  Check Condition     */
        /*--------------------------------*/
        /*-------------------------------*/
        /*   Get and Check lot's State   */
        /*-------------------------------*/
        log.debug("step 6 -  Check Condition");
        log.trace("cassetteCheckFlag : {}",cassetteCheckFlag);
        if (cassetteCheckFlag) {
            Validations.check (!CIMStateConst.CIM_LOT_STATE_ACTIVE.equals(lot.getLotState()), new OmCode(retCodeConfig.getInvalidLotStat(),lot.getLotState()));
        }
        log.trace("!CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD.equals(lot.getLotHoldState()) : {}",!CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD.equals(lot.getLotHoldState()));
        if (!CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD.equals(lot.getLotHoldState())) {
            Validations.check (!TransactionIDEnum.FORCE_OPE_LOCATE_REQ.getValue().equals(objCommon.getTransactionID()),new OmCode(retCodeConfig.getInvalidLotHoldStat(),lot.getIdentifier(),lot.getLotHoldState()));
        }
        log.trace("cassetteCheckFlag : {}",cassetteCheckFlag);
        if (cassetteCheckFlag) {
            Validations.check (!CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_WAITING, lot.getLotProcessState()), new OmCode(retCodeConfig.getInvalidLotProcessState(), lot.getIdentifier(), lot.getLotProcessState()));
        }
        Validations.check (!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, lot.getLotInventoryState()),
                retCodeConfig.getInvalidLotInventoryStat(),ObjectIdentifier.fetchValue(lotID),lot.getLotInventoryState());
        //----------------------------------
        // step 7 -  Get InPostProcessFlag of lot
        //----------------------------------
        log.debug(" step 7 -  Get InPostProcessFlag of lot");
        Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);

        //----------------------------------------------
        // step 8 - If lot is in post process, returns error
        //----------------------------------------------
        log.debug("step 8 - If lot is in post process, returns error");
        if (objLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot()) {
            /*-----------------------------------*/
            /* step 9 - Get UserGroupID By UserID */
            /*------------------------------------*/
            log.debug("step 9 - Get UserGroupID By UserID");
            List<ObjectIdentifier>  userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());

            /*String extPostProc = environmentVariableCore.getEnvironmentValue(EnvEnum.EXTERNAL_POST_PROC_USER_GRP);*/
            int i;
            for (i = 0; i < userGroupIDs.size(); i++) {

            }
            Validations.check(i == userGroupIDs.size(), retCodeConfig.getLotInPostProcess());
        }

        /*---------------------------------------------------*/
        /* step 10 -   Get and Check cassette's Xfer State   */
        /*---------------------------------------------------*/
        log.debug("step 10 -   Get and Check cassette's Xfer State");
        int lotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getIntValue();
        log.trace("1 == lotOperationEIcheck : {}",1 == lotOperationEIcheck);
        if (1 == lotOperationEIcheck) {
            log.trace("!TransactionIDEnum.OPERATION_START_CANCEL_REQ.getValue().equals(objCommon.getTransactionID() : {}",!TransactionIDEnum.OPERATION_START_CANCEL_REQ.getValue().equals(objCommon.getTransactionID()));
            if (!TransactionIDEnum.OPERATION_START_CANCEL_REQ.getValue().equals(objCommon.getTransactionID())
                    && !TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue().equals(objCommon.getTransactionID())
                    && !TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue().equals(objCommon.getTransactionID())
                    && !TransactionIDEnum.GATE_PASS_REQ.getValue().equals(objCommon.getTransactionID())
                    && !TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                    && !TransactionIDEnum.FORCE_OPERATION_COMP_REQ.getValue().equals(objCommon.getTransactionID())
                    && !TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                    && !TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                    && !TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                    && !TransactionIDEnum.TXBOC009.getValue().equals(objCommon.getTransactionID())
                    && !TransactionIDEnum.MONITOR_LOT_STB_AFTER_PROCESS_REQ.getValue().equals(objCommon.getTransactionID())
                    && !TransactionIDEnum.LOT_SAMPLING_RULE_CHECK.getValue().equals(objCommon.getTransactionID())
            ) {
                CimCassette cassette=baseCoreFactory.getBO(CimCassette.class,aCassetteID);
                Validations.check (CimStringUtils.isEmpty(cassette), retCodeConfig.getNotFoundCassette());
                Validations.check (BizConstant.SP_TRANSSTATE_EQUIPMENTIN.equals(cassette.getTransportState()), new OmCode(retCodeConfig.getInvalidCassetteTransferState(),
                        cassette.getTransportState(),cassette.getIdentifier()));
            }
        }
        log.trace("StringUtils.isEmpty(processRef.getModulePOS()) || processRef.getModulePOS().equals(\"*\") : {}", CimStringUtils.isEmpty(processRef.getModulePOS()) || processRef.getModulePOS().equals("*"));
        if (CimStringUtils.isEmpty(processRef.getModulePOS()) || processRef.getModulePOS().equals("*")) {
            Inputs.ObjProcessGetTargetOperationIn objProcessGetTargetOperationIn = new Inputs.ObjProcessGetTargetOperationIn();
            objProcessGetTargetOperationIn.setLocateDirection(skipReqParams.getLocateDirection());
            objProcessGetTargetOperationIn.setLotID(lotID);
            objProcessGetTargetOperationIn.setRouteID(skipReqParams.getRouteID());
            objProcessGetTargetOperationIn.setOperationNumber(skipReqParams.getOperationNumber());
            Outputs.ObjProcessGetTargetOperationOut objProcessGetTargetOperationOut = processMethod.processGetTargetOperation(objCommon, objProcessGetTargetOperationIn);
            processRef = objProcessGetTargetOperationOut.getProcessRef();
        } else {
            /*-------------------------------------------------------*/
            /* step 11 - Check Target Operation same as processRef   */
            /*-------------------------------------------------------*/
            log.debug("step 11 - Check Target Operation same as processRef");
            Outputs.ObjProcessGetOperationByProcessRefOut objProcessGetOperationByProcessRefOut = processMethod.processGetOperationByProcessRef(objCommon, processRef);
            log.trace("!StringUtils.isEmpty(skipReqParams.getRouteID()) && !StringUtils.isEmpty(skipReqParams.getOperationNumber()) : {}",!CimStringUtils.isEmpty(skipReqParams.getRouteID()) && !CimStringUtils.isEmpty(skipReqParams.getOperationNumber()));
            if (!CimStringUtils.isEmpty(skipReqParams.getRouteID()) && !CimStringUtils.isEmpty(skipReqParams.getOperationNumber())) {
                Validations.check (!CimObjectUtils.equalsWithValue(skipReqParams.getRouteID(), objProcessGetOperationByProcessRefOut.getRouteID())
                        || !skipReqParams.getOperationNumber().equals(objProcessGetOperationByProcessRefOut.getOperationNumber()), new OmCode(retCodeConfig.getNotSameRoute(),skipReqParams.getRouteID().getValue(),objProcessGetOperationByProcessRefOut.getRouteID().getValue()));
            }
        }
        lotMethod.lotFutureHoldRequestsCheckLocate(objCommon, skipReqParams.getLocateDirection(), lotID, processRef);

        log.trace("!backupOperationFlag : {}",!backupOperationFlag);
        if (!backupOperationFlag) {
            try {
                lotMethod.lotFlowBatchCheckLocate(objCommon, skipReqParams.getLocateDirection(), skipReqParams.getLotID(), processRef);
            } catch (ServiceException e) {
                /*---------------------------------------*/
                /* Step 12 - Remove lot from flowbatch   */
                /*---------------------------------------*/
                log.debug("Step 12 - Remove lot from flowbatch");
                log.trace("Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode()));
                if (Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode())) {
                    ObjectIdentifier lotFlowBatchIDGet = null;
                    try {
                        lotFlowBatchIDGet = lotMethod.lotFlowBatchIDGet(objCommon, lotID);
                    } catch (ServiceException ex) {
                        log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), ex.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), ex.getCode()));
                        if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), ex.getCode())){
                            lotFlowBatchIDGet = ex.getData(ObjectIdentifier.class);
                            log.debug("bParallelPostProcFlag : {}",bParallelPostProcFlag);
                            if (bParallelPostProcFlag) {
                                List<Infos.ContainedLotsInFlowBatch> containedLotsInFlowBatches = flowBatchMethod.flowBatchLotGet(objCommon, lotFlowBatchIDGet);
                                log.trace("!ArrayUtils.isEmpty(containedLotsInFlowBatches) : {}",!CimArrayUtils.isEmpty(containedLotsInFlowBatches));
                                if (!CimArrayUtils.isEmpty(containedLotsInFlowBatches)) {
                                    for (Infos.ContainedLotsInFlowBatch containedLotsInFlowBatch : containedLotsInFlowBatches) {
                                        // advanced_object_Lock
                                        objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(containedLotsInFlowBatch.getLotID(),
                                                BizConstant.SP_CLASSNAME_POSLOT,
                                                BizConstant.SP_OBJECTLOCK_OBJECTTYPE_OBJECT,
                                                (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ, new ArrayList<>()));
                                    }
                                }
                            }
                            // flowBatchLotRemoveReqService
                            Params.FlowBatchLotRemoveReq flowBatchLotRemoveReq = new Params.FlowBatchLotRemoveReq();
                            flowBatchLotRemoveReq.setFlowBatchID(lotFlowBatchIDGet);
                            List<Infos.RemoveCassette> removeCassettes = new ArrayList<>();
                            Infos.RemoveCassette removeCassette = new Infos.RemoveCassette();
                            removeCassette.setLotID(Collections.singletonList(lotID));
                            removeCassette.setCassetteID(aCassetteID);
                            removeCassettes.add(removeCassette);
                            flowBatchLotRemoveReq.setStrRemoveCassette(removeCassettes);
                            flowBatchService.sxFlowBatchLotRemoveReq(objCommon, flowBatchLotRemoveReq);
                        } else {
                            throw ex;
                        }
                    }
                } else {
                    throw e;
                }
            }

            // lot_bondingFlowSection_CheckLocate
            log.debug("check lot bonding flow section locate");
            lotMethod.lotBondingFlowSectionCheckLocate(objCommon, skipReqParams.getLotID(), processRef);
        }

        /*------------------------------------------*/
        /* Step 14 - Check lot's Control Job ID     */
        /*------------------------------------------*/
        log.debug("Step 14 - Check lot's Control Job ID");
        Validations.check (!CimStringUtils.isEmpty(lot.getControlJob()), new OmCode(retCodeConfig.getLotControlJobidFilled()
                , lot.getIdentifier(), CimObjectUtils.getObjectValue(lot.getControlJobID())));
        /*------------------------------------------------------------------------*/
        /* Step 15 - Change State                                                 */
        /*------------------------------------------------------------------------*/
        log.debug("Step 15 - Change State");
        monitorGroupMethod.monitorGroupCheckExistance(objCommon, lotID);
        log.trace("skipReqParams.getLocateDirection() : {}",skipReqParams.getLocateDirection());
        if (skipReqParams.getLocateDirection()) {
            futureHoldCancel(objCommon, new Infos.EffectCondition(BizConstant.SP_FUTUREHOLD_ALL, BizConstant.SP_FUTUREHOLD_ALL), lotID);

            //---------------------------------------
            // step 16 - Check interFabXferPlan existence
            //---------------------------------------
//            Inputs.objProcessCheckInterFabXferPlanSkipIn objProcessCheckInterFabXferPlanSkipIn = new Inputs.objProcessCheckInterFabXferPlanSkipIn();
//            objProcessCheckInterFabXferPlanSkipIn.setLotID(skipReqParams.getLotID().getValue());
//            objProcessCheckInterFabXferPlanSkipIn.setCurrentRouteID(skipReqParams.getCurrentRouteID().getValue());
//            objProcessCheckInterFabXferPlanSkipIn.setCurrentOpeNo(skipReqParams.getCurrentOperationNumber());
//            objProcessCheckInterFabXferPlanSkipIn.setJumpingRouteID(skipReqParams.getRouteID().getValue());
//            objProcessCheckInterFabXferPlanSkipIn.setJumpingOpeNo(skipReqParams.getOperationNumber());
            //TODO-NOTIMPL: process_CheckInterFabXferPlanSkip
//            RetCode retCode1 = cimComp.processCheckInterFabXferPlanSkip(objCommon, objProcessCheckInterFabXferPlanSkipIn);
//            if (retCodeConfig.getSucc() != retCode1.getReturnCode()) {
//                result.setReturnCode(retCode1.getReturnCode());
//                return result;
//            }
            /*--------------------------------------------*/
            /*  step 17 - Check Future Action Procedure   */
            /*--------------------------------------------*/
            // schdlChangeReservation_CheckForFutureOperation
            log.debug("step 17 - Check Future Action Procedure");
            String tmpRouteID = ObjectIdentifier.fetchValue(skipReqParams.getRouteID());
            scheduleChangeReservationMethod.schdlChangeReservationCheckForFutureOperation(objCommon, lotID, tmpRouteID, skipReqParams.getOperationNumber());
        }

        Outputs.ObjProcessLocateOut objProcessLocateOut = processMethod.processLocate(objCommon, lotID, processRef, skipReqParams.getSequenceNumber());
        log.trace("cassetteCheckFlag : {}",cassetteCheckFlag);
        if (cassetteCheckFlag) {
            Infos.EffectCondition effectCondition = new Infos.EffectCondition(BizConstant.SP_FUTUREHOLD_PRE, BizConstant.SP_FUTUREHOLD_ALL);
            Outputs.ObjLotFutureHoldRequestsEffectByConditionOut conditionOutObj = lotMethod.lotFutureHoldRequestsEffectByCondition(objCommon, lotID, effectCondition);

            log.trace("!ObjectUtils.isEmpty(conditionOutObj) && !ObjectUtils.isEmpty(conditionOutObj.getStrLotHoldReqList()) : {}",!CimObjectUtils.isEmpty(conditionOutObj) && !CimObjectUtils.isEmpty(conditionOutObj.getStrLotHoldReqList()));
            if (!CimObjectUtils.isEmpty(conditionOutObj) && !CimObjectUtils.isEmpty(conditionOutObj.getStrLotHoldReqList())) {
                sxHoldLotReq(objCommon, lotID, conditionOutObj.getStrLotHoldReqList());
            }
            //add run card auto complete start
            //-----------------------------------------------------------------
            // Call runCardAutoCompleteAction
            //-----------------------------------------------------------------
            log.debug("Call runCardAutoCompleteAction");
            try {
                runCardMethod.runCardAutoCompleteAction(objCommon,lotID);
            } catch (Exception e) {
                //do nothing
                log.error("RunCard Auto Complete Fail: {}",e.getMessage());
            }
            //add run card auto complete end
        }

        futureHoldCancel(objCommon, new Infos.EffectCondition(BizConstant.SP_FUTUREHOLD_PRE, BizConstant.SP_FUTUREHOLD_SINGLE), lotID);

        /*------------------------------------------------*/
        /*  step 18 - Update cassette's MultiLotType      */
        /*------------------------------------------------*/
        log.debug("step 18 - Update cassette's MultiLotType");
        log.trace("cassetteCheckFlag : {}",cassetteCheckFlag);
        if (cassetteCheckFlag) {
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, aCassetteID);
        }

        //--------------------------------------------------------------------------------------------------
        // UpDate RequiredCassetteCategory
        //--------------------------------------------------------------------------------------------------
        log.debug("UpDate RequiredCassetteCategory");
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotID);

        //-----------------------------//
        //  step 19 - Process Hold    //
        //----------------------------//

        //step 19 :txProcessHoldDoActionReq
        log.debug("step 19 :txProcessHoldDoActionReq");
        processControlService.sxProcessHoldDoActionReq(objCommon, lotID, skipReqParams.getClaimMemo());

        /*------------------------------------------------------------------------*/
        /*  step 20 - Make History                                                */
        /*------------------------------------------------------------------------*/
        log.debug("step 20 - Make History ");
        Inputs.LotOperationMoveEventMakeLocateParams lotOperationMoveEventMakeLocateParams = new Inputs.LotOperationMoveEventMakeLocateParams();
        lotOperationMoveEventMakeLocateParams.setClaimMemo(skipReqParams.getClaimMemo());
        lotOperationMoveEventMakeLocateParams.setALotID(skipReqParams.getLotID());
        lotOperationMoveEventMakeLocateParams.setLotcateDirection(skipReqParams.getLocateDirection());
        lotOperationMoveEventMakeLocateParams.setOldCurrentPOData(objProcessLocateOut.getOldCurrentPOData());
        log.trace("ObjectUtils.equalsWithValue(objCommon.getTransactionID(), TransactionIDEnum.FORCE_OPE_LOCATE_REQ.getValue()) : {}", CimObjectUtils.equalsWithValue(objCommon.getTransactionID(), TransactionIDEnum.FORCE_OPE_LOCATE_REQ.getValue()));
        if (CimObjectUtils.equalsWithValue(objCommon.getTransactionID(), TransactionIDEnum.FORCE_OPE_LOCATE_REQ.getValue())){
            lotOperationMoveEventMakeLocateParams.setTransactionID(TransactionIDEnum.FORCE_OPE_LOCATE_REQ.getValue());
        } else {
            lotOperationMoveEventMakeLocateParams.setTransactionID(TransactionIDEnum.OPE_LOCATE_REQ.getValue());
        }
        eventMethod.lotOperationMoveEventMakeLocate(objCommon, lotOperationMoveEventMakeLocateParams);
        boolean objLotCheckConditionForAutoBankInOut = lotMethod.lotCheckConditionForAutoBankIn(objCommon, lotID);
        log.trace("objLotCheckConditionForAutoBankInOut : {}",objLotCheckConditionForAutoBankInOut);
        if (objLotCheckConditionForAutoBankInOut){
            List<ObjectIdentifier> lotIDx = new ArrayList<>();
            lotIDx.add(skipReqParams.getLotID());
            /*-----------------------------------------*/
            /*   Call txBankInReq() for Auto-Bank-In   */
            /*-----------------------------------------*/
            log.debug("Call txBankInReq() for Auto-Bank-In ");
            bankService.sxBankInReq(objCommon, 0, lotIDx, skipReqParams.getClaimMemo());
        }

        // 【step 21】 check contamination after skipping
        log.debug("【step 21】 check contamination after skipping");
        contaminationMethod.lotCheckContaminationLevelStepOut(objCommon, lotID);

    }

    /**
     * %s
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Nyx
     * @date 2018/12/28 14:28
     */
    private void futureHoldCancel(Infos.ObjCommon objCommon, Infos.EffectCondition effectCondition, ObjectIdentifier lotID) {
        Outputs.ObjLotFutureHoldRequestsDeleteEffectedByConditionOut conditionOutObj = lotMethod.lotFutureHoldRequestsDeleteEffectedByCondition(objCommon, lotID, effectCondition);
        if (!CimObjectUtils.isEmpty(conditionOutObj) && !CimObjectUtils.isEmpty(conditionOutObj.getStrFutureHoldReleaseReqList())) {
            Params.FutureHoldCancelReqParams params = new Params.FutureHoldCancelReqParams();
            params.setLotID(lotID);
            params.setEntryType(CIMStateConst.CIM_ENTRY_TYPE_REMOVE);
            params.setLotHoldList(conditionOutObj.getStrFutureHoldReleaseReqList());
            processControlService.sxFutureHoldCancelReq(objCommon, params);
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params    -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     * @author Sun
     * @date 10/30/2018 11:14 AM
     */
    @SuppressWarnings("deprecation")
    public Results.PartialReworkReqResult sxPartialReworkReq(Infos.ObjCommon objCommon, Params.PartialReworkReqParams params) {
        Results.PartialReworkReqResult retVal = new Results.PartialReworkReqResult();
        log.debug("PartialReworkReqParams ; {}", params);
        log.debug("【Method Entry】sxPartialReworkReq()");
        Infos.PartialReworkReq partialReworkReq = params.getPartialReworkReqInformation();
        //【Step1】Lock objects to be updated;
        log.debug("【Step1】Lock objects to be updated;");
        ObjectIdentifier outLotCassetteGet = lotMethod.lotCassetteListGetDR(objCommon,partialReworkReq.getParentLotID());
        int lotOperationCheckEI = 0;
        String configOperationEICheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        log.trace("!StringUtils.isEmpty(configOperationEICheck) : {}",!CimStringUtils.isEmpty(configOperationEICheck));
        if(!CimStringUtils.isEmpty(configOperationEICheck)){
            lotOperationCheckEI = Integer.parseInt(configOperationEICheck);
        }
        String txID = null;
        Long lockMode = 0L;
        String cassetteTransferStateGetOutRetCode = null;
        Boolean updateControlJobFlag = false;
        log.trace("0 == lotOperationCheckEI : {}",0 == lotOperationCheckEI);
        if ( 0 == lotOperationCheckEI ) {
            //【Step2】 Get carrier transfer status;
            log.debug("【Step2】 Get carrier transfer status;");
            cassetteTransferStateGetOutRetCode = cassetteMethod.cassetteTransferStateGet(objCommon,outLotCassetteGet);

            //【Step3】Get eqp ID in cassette;
            log.debug("【Step3】Get eqp ID in cassette;");
            Outputs.ObjCassetteEquipmentIDGetOut outEquipmentID = cassetteMethod.cassetteEquipmentIDGet(objCommon,outLotCassetteGet);
            // lock
            // 【Step4】Get required eqp lock mode;
            log.debug("【Step4】Get required eqp lock mode;");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(outEquipmentID.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            log.trace("params.getPartialReworkReqInformation().getBForceRework() : {}",params.getPartialReworkReqInformation().getBForceRework());
            if (params.getPartialReworkReqInformation().getBForceRework()){
                log.trace("params.getPartialReworkReqInformation().getBDynamicRoute() : {}",params.getPartialReworkReqInformation().getBDynamicRoute());
                if (params.getPartialReworkReqInformation().getBDynamicRoute()){
                    txID = TransactionIDEnum.PARTIAL_REWORK_FORCE_AND_DYNAMIC_ROUTE.getValue();
                } else {
                    txID = TransactionIDEnum.PARTIAL_REWORK_FORCE.getValue();
                }
            } else {
                log.trace("params.getPartialReworkReqInformation().getBDynamicRoute() : {}",params.getPartialReworkReqInformation().getBDynamicRoute());
                if (params.getPartialReworkReqInformation().getBDynamicRoute()){
                    txID = TransactionIDEnum.PARTIAL_REWORK_DYNAMIC_ROUTE.getValue();
                } else {
                    txID = TransactionIDEnum.PARTIAL_REWORK.getValue();
                }
            }
            objLockModeIn.setFunctionCategory(txID);
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            log.debug( "lockMode = {}", lockMode );
            log.trace("StringUtils.equals(cassetteTransferStateGetOutRetCode, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) : {}", CimStringUtils.equals(cassetteTransferStateGetOutRetCode, BizConstant.SP_TRANSSTATE_EQUIPMENTIN));
            if (CimStringUtils.equals(cassetteTransferStateGetOutRetCode, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                updateControlJobFlag = true;
                log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                    // 【Step4-1-1】Lock eqp Main Object;
                    log.debug("【Step4-1-1】Lock eqp Main Object;");
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(outEquipmentID.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                    // 【Step4-1-2】Lock eqp LoadCassette Element (Write);
                    log.debug("【Step4-1-2】Lock eqp LoadCassette Element (Write)");
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(outEquipmentID.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                            (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, Collections.singletonList(outLotCassetteGet.getValue())));
                }
                else {
                    //【Step4-2】Lock Machine object  ;
                    log.debug("【Step4-2】Lock Machine object  ;");
                    objectLockMethod.objectLock(objCommon, CimMachine.class, outEquipmentID.getEquipmentID());
                }
            }
        }
        // lock
        //【Step5】 lock cassette
        log.debug("lock cassette");
        objectLockMethod.objectLock(objCommon, CimCassette.class, outLotCassetteGet);
        log.trace("0 == lotOperationCheckEI : {}",0 == lotOperationCheckEI);
        if ( 0 == lotOperationCheckEI ) {
            log.trace(" !updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}", !updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
            if( !updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                //【Step6】Get cassette's ControlJobID;
                //【Step6-1】
                log.debug("【Step6】Get cassette's ControlJobID;");
                ObjectIdentifier cassetteControlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, outLotCassetteGet);
                log.trace("!ObjectUtils.isEmptyWithValue(cassetteControlJobID) : {}",!CimObjectUtils.isEmptyWithValue(cassetteControlJobID));
                if (!CimObjectUtils.isEmptyWithValue(cassetteControlJobID)) {
                    log.debug( "cassette's controlJobID isn't blank." );
                    updateControlJobFlag = true;
                    log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                    if(!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                        //【Step6-2】Lock controljob Object;
                        log.debug("【Step6-2】Lock controljob Object;");
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, cassetteControlJobID);
                    }
                }
            }
        }
        //【Step7】 lock parent lot;
        log.debug("【Step7】 lock parent lot;");
        objectLockMethod.objectLock(objCommon, CimLot.class, partialReworkReq.getParentLotID());
        //【Step8】 Check lot transfer status;
        log.debug("【Step8】 Check lot transfer status;");
        String lotTransferState;
        log.trace("lotOperationCheckEI == 1 || (lotOperationCheckEI == 0 &&\n" +
                "                !ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,\n" +
                "                        cassetteTransferStateGetOutRetCode)) : {}",lotOperationCheckEI == 1 || (lotOperationCheckEI == 0 &&
                !CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,
                        cassetteTransferStateGetOutRetCode)));
        if (lotOperationCheckEI == 1 || (lotOperationCheckEI == 0 &&
                !CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,
                        cassetteTransferStateGetOutRetCode))){
            lotTransferState = lotMethod.lotTransferStateGet(objCommon,partialReworkReq.getParentLotID());
            log.trace("lotOperationCheckEI == 0 : {}",lotOperationCheckEI == 0);
            if(lotOperationCheckEI == 0) {
                log.debug("lotOperationCheckEI = 0");
                log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,lotTransferState) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,lotTransferState));
                if (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,lotTransferState)) {
                    log.debug("Changed to EI by other operation");
                    Validations.check(true,retCodeConfig.getChangedToEiByOtherOperation(),outLotCassetteGet);
                }
                else if (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT,lotTransferState) ||
                        CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,lotTransferState)) {
                    log.debug("Transfer State is INVALID...");
                    Validations.check(true,retCodeConfig.getInvalidLotXferstat(),lotTransferState);
                }
            }
        }
        log.debug("0 == lotOperationCheckEI : {}",0 == lotOperationCheckEI);
        if ( 0 == lotOperationCheckEI ) {
            lotTransferState = cassetteTransferStateGetOutRetCode;
        }

        //【Step9】Check Bonding Group for parentLot;
        log.debug("【Step9】Check Bonding Group for parentLot;");
        String lotBondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon,
                partialReworkReq.getParentLotID());
        log.trace(" !ObjectUtils.isEmpty(lotBondingGroupID) : {}", !CimObjectUtils.isEmpty(lotBondingGroupID));
        if( !CimObjectUtils.isEmpty(lotBondingGroupID)) {
            log.debug("lotBondingGroupIDGetDROutRetCode.getObject().getBondingGroupID() is not empty string.");
            Validations.check(true,retCodeConfig.getLotHasBondingGroup(),partialReworkReq.getParentLotID().getValue(),lotBondingGroupID);
        }
        // 【Step10】Retrieve all state for parentLot;
        log.debug("【Step10】Retrieve all state for parentLot;");
        Outputs.ObjLotAllStateGetOut lotAllStateGetOutRetCode = lotMethod.lotAllStateGet(objCommon,partialReworkReq.getParentLotID());


        //【Step11】Check lot condition;
        log.debug("【Step11】Check lot condition;");
        Validations.check (CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD,lotAllStateGetOutRetCode.getHoldState()), retCodeConfig.getConnotSplitHeldlot());
        Validations.check (!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_STATE_ACTIVE,lotAllStateGetOutRetCode.getLotState()), retCodeConfig.getInvalidLotStat(),lotAllStateGetOutRetCode.getLotState());
        Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_PROCSTATE_PROCESSING,lotAllStateGetOutRetCode.getProcessState()), retCodeConfig.getInvalidLotProcessState(),partialReworkReq.getParentLotID(),lotAllStateGetOutRetCode.getProcessState());
        Validations.check (CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_PRODUCTION_STATE_INREWORK,lotAllStateGetOutRetCode.getProductionState()), retCodeConfig.getInvalidLotProductionState(),partialReworkReq.getParentLotID(),lotAllStateGetOutRetCode.getProductionState());
        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR,lotAllStateGetOutRetCode.getInventoryState()),
                retCodeConfig.getInvalidLotInventoryStat(),partialReworkReq.getParentLotID(),lotAllStateGetOutRetCode.getInventoryState());

        log.debug( "【Step12】Get InPostProcessFlag of lot");
        //【Step12】Get InPostProcessFlag of lot
        Outputs.ObjLotInPostProcessFlagOut strLotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, partialReworkReq.getParentLotID());
        //【Step13】If lot is in post process, returns error:Get UserGroupID By UserID;
        log.debug("【Step13】If lot is in post process, returns error:Get UserGroupID By UserID;");
        log.trace("BooleanUtils.isTrue(strLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot()) : {}", CimBooleanUtils.isTrue(strLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot()));
        if (CimBooleanUtils.isTrue(strLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot())) {
            /*---------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            log.debug("Get UserGroupID By UserID");
            List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
            int userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);
            log.debug("userGroupIDsLen : {}", userGroupIDsLen);
            int i = 0;
            for ( i = 0; i < userGroupIDsLen; i++) {
            }
            Validations.check ( i == userGroupIDsLen, retCodeConfig.getLotInPostProcess(),partialReworkReq.getParentLotID());
        }
        //【Step14】for parentLot;
        log.debug("【Step14】for parentLot;");
        String lotContentsRetCode = lotMethod.lotContentsGet(partialReworkReq.getParentLotID());

        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER,lotContentsRetCode)
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE,lotContentsRetCode), retCodeConfig.getInvalidLotContents(),partialReworkReq.getParentLotID());

        //【Step15】Check lot's Control Job ID;
        log.debug("【Step15】Check lot's Control Job ID;");
        ObjectIdentifier lotControlJobIDGetOutRetCode = lotMethod.lotControlJobIDGet(objCommon,partialReworkReq.getParentLotID());
        log.trace("ObjectUtils.isEmpty(lotControlJobIDGetOutRetCode) : {}", CimObjectUtils.isEmpty(lotControlJobIDGetOutRetCode));
        if (CimObjectUtils.isEmpty(lotControlJobIDGetOutRetCode)) {
            log.debug("lotControlJobIDGetOutRetCode.getObject().getControlJobID().getValue()) is empty string.");
        } else {
            log.debug( "lotControlJobIDGetOutRetCode.getObject().getControlJobID().getValue()) is not empty string.");
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),partialReworkReq.getParentLotID().getValue(),lotControlJobIDGetOutRetCode.getValue()));
        }

        log.trace("1 == lotOperationCheckEI : {}",1 == lotOperationCheckEI);
        if ( 1 == lotOperationCheckEI ){
            log.debug( "lotOperationCheckEI = 1");
            //【Step16】 Check carrier dispatch status;
            log.debug("【Step16】 Check carrier dispatch status;");
            Boolean cassetteDispatchStateOut = cassetteMethod.cassetteDispatchStateGet(objCommon,outLotCassetteGet);
            Validations.check (cassetteDispatchStateOut, retCodeConfig.getAlreadyDispatchReservedCassette());
        }

        //【Step17】Check SorterJob existence;
        log.debug("【Step17】Check SorterJob existence;");
        Inputs.ObjWaferSorterJobCheckForOperation operation = new Inputs.ObjWaferSorterJobCheckForOperation();
        List<ObjectIdentifier> lotIDs= new ArrayList<>();
        lotIDs.add(partialReworkReq.getParentLotID());
        operation.setLotIDList(lotIDs);
        operation.setOperation(BizConstant.SP_OPERATION_FOR_LOT.toString());
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon,operation);
        //【Step18】Converts input strPartialReworkReq.subrouteID if it has the version ##;
        log.debug("【Step18】Converts input strPartialReworkReq.subrouteID if it has the version");
        ObjectIdentifier processActiveIDOut = processMethod.processActiveIDGet(objCommon,partialReworkReq.getSubRouteID());
        log.debug( "active subRouteID = ", processActiveIDOut.getValue()); ;

        ObjectIdentifier aSubRouteID = processActiveIDOut;
        //【Step19】Check first operation or not;
        log.debug("【Step19】Check first operation or not"); ;
        Inputs.ObjProcessOperationProcessRefListForLotIn in = new Inputs.ObjProcessOperationProcessRefListForLotIn();
        in.setSearchDirection(false);
        in.setPosSearchFlag(true);
        in.setSearchCount(Integer.parseInt(BizConstant.SEARCH_COUNT.toString()));
        in.setCurrentFlag(true);
        in.setSearchRouteID(new ObjectIdentifier(BizConstant.EMPTY.toString()));
        in.setLotID(partialReworkReq.getParentLotID());
        List<Infos.OperationProcessRefListAttributes> refListForLotOutRetCode = processMethod.processOperationProcessRefListForLot(objCommon,in);

        int lenNameAttributes = CimArrayUtils.getSize(refListForLotOutRetCode);
        log.trace("lenNameAttributes < 2\n" +
                "                || ( lenNameAttributes >= 2\n" +
                "                && !ObjectUtils.equals(refListForLotOutRetCode.get(0).getRouteID(),\n" +
                "                refListForLotOutRetCode.get(1).getRouteID())) : {}",lenNameAttributes < 2
                || ( lenNameAttributes >= 2
                && !CimObjectUtils.equals(refListForLotOutRetCode.get(0).getRouteID(),
                refListForLotOutRetCode.get(1).getRouteID())));
        if (lenNameAttributes < 2
                || ( lenNameAttributes >= 2
                && !CimObjectUtils.equals(refListForLotOutRetCode.get(0).getRouteID(),
                refListForLotOutRetCode.get(1).getRouteID())) ) {
            log.debug("processOperationProcessRefListForLot(): lenNameAttributes == {}", lenNameAttributes);
            Validations.check(retCodeConfig.getInvalidReworkOperation());
        }


        //【Step20】Check Route is Dynamic or Not
        log.debug("【Step20】Check Route is Dynamic or Not");
        log.trace("BooleanUtils.isTrue(partialReworkReq.getBDynamicRoute()) : {}", CimBooleanUtils.isTrue(partialReworkReq.getBDynamicRoute()));
        if ( CimBooleanUtils.isTrue(partialReworkReq.getBDynamicRoute())) {
            Outputs.ObjProcessCheckForDynamicRouteOut checkForDynamicRouteOutRetCode = processMethod.processCheckForDynamicRoute(objCommon, aSubRouteID);
            Validations.check (CimBooleanUtils.isFalse(checkForDynamicRouteOutRetCode.getDynamicRouteFlag()), retCodeConfig.getNotDynamicRoute());
        }

        //【Step21】Check Input return operation number
        log.debug("【Step21】Check Input return operation number");
        Boolean inputReturnOperationFlag = false;
        log.trace("!ObjectUtils.isEmpty(partialReworkReq.getReturnOperationNumber()) : {}",!CimObjectUtils.isEmpty(partialReworkReq.getReturnOperationNumber()));
        if(!CimObjectUtils.isEmpty(partialReworkReq.getReturnOperationNumber())) {
            log.debug( "Set inputReturnOperationFlag = TRUE" ); ;
            inputReturnOperationFlag = true ;
        }

        //【Step22】Check Input return operation number is exist in connected route list;
        log.debug("【Step22】Check Input return operation number is exist in connected route list;");
        Boolean connectedRouteReturnOperationFlag = false;
        String returnOperationNumber = null;
        Boolean sameReturnOperationExistFlag = false;

        //【Step22-1】
        Outputs.ObjProcessCheckForDynamicRouteOut checkForDynamicRouteOutRetCode = processMethod.processCheckForDynamicRoute(objCommon, aSubRouteID);
        log.debug("bDynamicRoute = {}", checkForDynamicRouteOutRetCode.getDynamicRouteFlag());

        //【Step22-2】
        Outputs.ObjProcessGetReturnOperationOut processGetReturnOperationOutRetCode = null;
        try{
            processGetReturnOperationOutRetCode = processMethod.processGetReturnOperation(objCommon,
                    partialReworkReq.getParentLotID(), aSubRouteID);
            //ok
            log.debug("Set connectedRouteReturnOperationFlag = true");
            connectedRouteReturnOperationFlag = true;
            log.trace("ObjectUtils.equalsWithValue(processGetReturnOperationOutRetCode.getOperationNumber(),partialReworkReq.getReturnOperationNumber()) : {}", CimObjectUtils.equalsWithValue(processGetReturnOperationOutRetCode.getOperationNumber(),partialReworkReq.getReturnOperationNumber()));
            log.trace("BooleanUtils.isFalse(checkForDynamicRouteOutRetCode.getDynamicRouteFlag()) &&\n" +
                    "                    !ObjectUtils.isEmpty(partialReworkReq.getReturnOperationNumber()) : {}", CimBooleanUtils.isFalse(checkForDynamicRouteOutRetCode.getDynamicRouteFlag()) &&
                    !CimObjectUtils.isEmpty(partialReworkReq.getReturnOperationNumber()));
            if (CimObjectUtils.equalsWithValue(processGetReturnOperationOutRetCode.getOperationNumber(),partialReworkReq.getReturnOperationNumber())) {
                log.debug("Set sameReturnOperationExistFlag = true");
                sameReturnOperationExistFlag = true;
            } else if (CimBooleanUtils.isFalse(checkForDynamicRouteOutRetCode.getDynamicRouteFlag()) &&
                    !CimObjectUtils.isEmpty(partialReworkReq.getReturnOperationNumber())){
                log.debug("input invalid parameters.");
                Validations.check(retCodeConfig.getInvalidParameter());
            }
        }catch (ServiceException ex){
            if (Validations.isEquals(retCodeConfig.getNotFoundSubRoute(),ex.getCode())) {
                log.debug("process_GetReturnOperation() == RC_NOT_FOUND_SUBROUTE");
                processGetReturnOperationOutRetCode = (Outputs.ObjProcessGetReturnOperationOut) ex.getData();
            }else {
                throw ex;
            }
        }

        //【Step23】 Get Current Route ID and Operation Number;
        log.debug("【Step23】 Get Current Route ID and Operation Number;");
        Outputs.ObjLotCurrentOperationInfoGetDROut  lotCurrentOperationInfoRetCode = lotMethod.lotCurrentOperationInfoGetDR(objCommon,partialReworkReq.getParentLotID());


        log.debug("currentRouteID         : {} ", lotCurrentOperationInfoRetCode.getOperationID() ); ;
        log.debug("currentOperationNumber : {} ", lotCurrentOperationInfoRetCode.getOpeNo()); ;

        //【Step24】Decide return operation number using all flags;
        log.debug("【Step24】Decide return operation number using all flags;");
        log.trace("BooleanUtils.isTrue(partialReworkReq.getBDynamicRoute()) : {}", CimBooleanUtils.isTrue(partialReworkReq.getBDynamicRoute()));
        if ( CimBooleanUtils.isTrue(partialReworkReq.getBDynamicRoute())) {
            log.debug("partialReworkReq.getBDynamicRoute() == true"); ;
            log.trace("BooleanUtils.isTrue(inputReturnOperationFlag ) : {}", CimBooleanUtils.isTrue(inputReturnOperationFlag ));
            if ( CimBooleanUtils.isTrue(inputReturnOperationFlag )) {
                log.debug("inputReturnOperationFlag == true"); ;
                returnOperationNumber = partialReworkReq.getReturnOperationNumber();
            } else {
                log.debug( "inputReturnOperationFlag == false"); ;
                log.trace("BooleanUtils.isTrue(connectedRouteReturnOperationFlag) : {}", CimBooleanUtils.isTrue(connectedRouteReturnOperationFlag));
                if ( CimBooleanUtils.isTrue(connectedRouteReturnOperationFlag)) {
                    log.debug("connectedRouteReturnOperationFlag == true"); ;
                    returnOperationNumber =processGetReturnOperationOutRetCode.getOperationNumber();
                } else {
                    log.debug("connectedRouteReturnOperationFlag == true"); ;
                    returnOperationNumber = lotCurrentOperationInfoRetCode.getOpeNo();
                }
            }
        }
        else  //strPartialReworkReq.bDynamicRoute == false ;
        {
            log.debug( "partialReworkReq.getBDynamicRoute() == false"); ;
            log.trace("connectedRouteReturnOperationFlag : {}",connectedRouteReturnOperationFlag);
            if ( connectedRouteReturnOperationFlag ) {
                log.debug( "connectedRouteReturnOperationFlag == true"); ;
                log.trace("inputReturnOperationFlag : {}",inputReturnOperationFlag);
                if ( inputReturnOperationFlag ) {
                    log.debug( "inputReturnOperationFlag == true"); ;
                    returnOperationNumber = partialReworkReq.getReturnOperationNumber();
                } else {
                    log.debug( "inputReturnOperationFlag == false" ); ;
                    returnOperationNumber = processGetReturnOperationOutRetCode.getOperationNumber();
                }
            } else {
                log.debug( "connectedRouteReturnOperationFlag == false" ); ;
                Validations.check(retCodeConfig.getInvalidRouteId());
            }
        }

        //【Step25】Check interFabXferPlan existence;
        log.debug("【Step25】call process_CheckInterFabXferPlanSkip");
        Inputs.ObjProcessCheckInterFabXferPlanSkipIn inPutParam = new Inputs.ObjProcessCheckInterFabXferPlanSkipIn();
        inPutParam.setLotID(partialReworkReq.getParentLotID());
        inPutParam.setCurrentRouteID(lotCurrentOperationInfoRetCode.getMainPDID());
        inPutParam.setCurrentOpeNo(lotCurrentOperationInfoRetCode.getOpeNo());
        inPutParam.setJumpingRouteID(lotCurrentOperationInfoRetCode.getMainPDID());
        inPutParam.setJumpingOpeNo(partialReworkReq.getReturnOperationNumber());
        // TODO: 2019/10/15 tobe confirm
        processMethod.processCheckInterFabXferPlanSkip(objCommon, inPutParam);

        //【Step26】Check ProcessDefinitionType is 'REWORK' or not;
        log.debug( "【Step26】Check ProcessDefinitionType is 'REWORK' or not."); ;
        Validations.check(!BizConstant.equalsIgnoreCase(BizConstant.SP_MAINPDTYPE_REWORK,
                processGetReturnOperationOutRetCode.getProcessDefinitionType()), retCodeConfig.getInvalidRouteType());

        //【Step27】Check decided return operation is exist on current route;
        log.debug( "【Step27】Check decided return operation is exist on current route."); ;
        List<Infos.OperationNameAttributes> processFlowOperationListInqResult = lotInqService.sxProcessFlowOperationListForLotInq(objCommon, partialReworkReq.getParentLotID());


        log.debug("returnOperationNumber : {}", returnOperationNumber );
        int opeLen = CimArrayUtils.getSize(processFlowOperationListInqResult);
        for (int opeCnt=0; opeCnt < opeLen; opeCnt++ ) {
            log.debug("opeCnt : {}", opeCnt ); ;
            Infos.OperationNameAttributes operationNameAttributes = processFlowOperationListInqResult.get(opeCnt);
            log.trace("StringUtils.equals(returnOperationNumber,operationNameAttributes.getOperationNumber()) : {}", CimStringUtils.equals(returnOperationNumber,operationNameAttributes.getOperationNumber()));
            if(CimStringUtils.equals(returnOperationNumber,operationNameAttributes.getOperationNumber())) {
                log.debug( "return operation is exist on current route"); ;
                break;
            } else if ( opeCnt == opeLen - 1 ) {
                log.debug( "opeCnt == opeLen - 1"); ;
                Validations.check(true,retCodeConfig.getNotFoundOperation(),returnOperationNumber);
            }
        }

        //【Step28】Check routeID conflict return RC_INVALID_BRANCH_ROUTED,  when the same routeID is used in the following case   ex) Subroute --> The same SubRoute in the course;
        log.debug("【Step28】Check routeID conflict return RC_INVALID_BRANCH_ROUTED,  when the same routeID is used in the following case   ex) Subroute --> The same SubRoute in the course;");
        //【Step28-1】
        log.debug( "【Step28-1】lotOriginalRouteListGet() IN ");
        Outputs.ObjLotOriginalRouteListGetOut lotOriginalRouteListGetRetCode = lotMethod.lotOriginalRouteListGet(objCommon,partialReworkReq.getParentLotID());

        int kLen = CimArrayUtils.getSize(lotOriginalRouteListGetRetCode.getOriginalRouteID());
        log.info( "the length of lotOriginalRouteListGetRetCode.getObject().getOriginalRouteID() = {}", kLen);

        //【Step28-2】Check CurrentRoute VS SubRoute
        log.debug("【Step28-2】Check CurrentRoute VS SubRoute");
        Validations.check(CimObjectUtils.equalsWithValue(aSubRouteID, lotCurrentOperationInfoRetCode.getOperationID()), retCodeConfig.getInvalidBranchRouteId());

        //【Step28-3】Check After Route VS SubRoute;
        log.debug("【Step28-3】Check After Route VS SubRoute;");
        for (int iLoop = 0; iLoop < kLen; iLoop++) {
            ObjectIdentifier originalRouteID = lotOriginalRouteListGetRetCode.getOriginalRouteID().get(iLoop);
            log.debug( "lotOriginalRouteListGetRetCode() : originalRouteID = {}", originalRouteID.getValue());
            Validations.check (CimObjectUtils.equalsWithValue(originalRouteID, partialReworkReq.getSubRouteID()), retCodeConfig.getInvalidBranchRouteId());
        }

        //【Step29】 Check for Future Hold
        log.debug("【Step29】 Check for Future Hold");
        log.debug("checkForLCFRRetCode != OK");
        lotMethod.lotFutureHoldRequestsCheckSplit(objCommon, partialReworkReq.getParentLotID(),
                "", partialReworkReq.getReturnOperationNumber());

        //【Step30】Split lot;
        log.debug("【Step30】Split lot;");
        ObjectIdentifier childWafer = lotMethod.lotSplitWaferLot(objCommon, partialReworkReq.getParentLotID(), partialReworkReq.getChildWaferID());

        //inherit the contamination flag from parent lot
        log.debug("inherit the contamination flag from parent lot");
        contaminationMethod.inheritContaminationFlagFromParentLot(partialReworkReq.getParentLotID(),childWafer);

        //【Step31】 Check Rework Count if necessary;
        log.debug("【Step31】 Check Rework Count if necessary;");
        log.trace("BooleanUtils.isFalse(partialReworkReq.getBForceRework()) : {}", CimBooleanUtils.isFalse(partialReworkReq.getBForceRework()));
        if (CimBooleanUtils.isFalse(partialReworkReq.getBForceRework())) {
            log.debug( "partialReworkReq.getBForceRework() == false");
            processMethod.processReworkCountCheck(objCommon,childWafer);
        }

        //【Step32】increment rework count of child lot;
        log.debug("【Step32】increment rework count of child lot;");
        processMethod.processReworkCountIncrement(objCommon,childWafer);
        //【Step32-2】This function check whether the lot's current operation is inside qtime section or not;
        qTimeMethod.qTimeCheckConditionForReplaceTarget(objCommon,childWafer);

        //【Step33】Branch child lot to sub route;
        log.debug("【Step33】Branch child lot to sub route;");
        Inputs.OldCurrentPOData oldCurrentPODataRetCode = processMethod.processBranchRoute(objCommon,childWafer,
                aSubRouteID,returnOperationNumber);

        //【Step34】Check parent lot's situation with Route Process Flow point of view;
        log.debug("【Step34】Check parent lot's situation with Route Process Flow point of view;");
        Boolean flag=false;
        Boolean flag1=false;
        Outputs.ObjProcessCompareCurrentOut processCompareCurrentOutRetCode = null;
        try{
            processCompareCurrentOutRetCode = processMethod.processCompareCurrent(objCommon,partialReworkReq.getParentLotID(),aSubRouteID,returnOperationNumber);
        }catch (ServiceException ex){
            processCompareCurrentOutRetCode = ex.getData(Outputs.ObjProcessCompareCurrentOut.class);
            log.trace("Validations.isEquals(retCodeConfig.getCurrentToperationSame(),ex.getCode()) ||\n" +
                    "                    Validations.isEquals(retCodeConfig.getCurrentToperationLate(),ex.getCode()) :{}",
                    Validations.isEquals(retCodeConfig.getCurrentToperationSame(),ex.getCode()) ||
                            Validations.isEquals(retCodeConfig.getCurrentToperationLate(),ex.getCode()));
            log.trace("Validations.isEquals(retCodeConfig.getCurrentToperationEarly(),ex.getCode()) : {}",Validations.isEquals(retCodeConfig.getCurrentToperationEarly(),ex.getCode()));
            if (Validations.isEquals(retCodeConfig.getCurrentToperationSame(),ex.getCode()) ||
                    Validations.isEquals(retCodeConfig.getCurrentToperationLate(),ex.getCode())){
                flag=true;
            }else if (Validations.isEquals(retCodeConfig.getCurrentToperationEarly(),ex.getCode())){
                flag1=true;
            }else {
                throw ex;
            }
        }

        String extendedCurrentOperationNumber ;
        String extendedReturnOperationNumber ;
        //【Step34-1】;
        log.trace("flag : {}",flag);
        if (flag) {
            log.debug( "processCompareCurrent() return CURRENT_OPERATION_SAME or CURRENT_OPERATION_LATE.");

            //【Step1】 Hold parent lot;
            log.debug("【Step1】 Hold parent lot;");
            List<Infos.LotHoldReq> lotHoldReqList = new ArrayList<>();
            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_REWORKHOLD);
            lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_REWORKHOLD,null));
            lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            lotHoldReq.setRouteID(aSubRouteID);
            lotHoldReq.setOperationNumber(returnOperationNumber);
            lotHoldReq.setRelatedLotID(childWafer);
            lotHoldReqList.add(lotHoldReq);
            sxHoldLotReq(objCommon, partialReworkReq.getParentLotID(),lotHoldReqList);

            //【Step2】Register Future Hold Record for parent lot
            log.debug("【Step2】Register Future Hold Record for parent lot");
            extendedCurrentOperationNumber = processCompareCurrentOutRetCode.getCurrentOperationNumber();

            Params.FutureHoldReqParams futureHoldReqParams = new Params.FutureHoldReqParams();
            futureHoldReqParams.setHoldType(BizConstant.SP_HOLDTYPE_REWORKHOLD);
            futureHoldReqParams.setLotID(partialReworkReq.getParentLotID());
            futureHoldReqParams.setRouteID(processCompareCurrentOutRetCode.getCurrentRouteID());
            futureHoldReqParams.setOperationNumber(extendedCurrentOperationNumber);
            futureHoldReqParams.setReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_REWORKHOLD));
            futureHoldReqParams.setRelatedLotID(childWafer);
            futureHoldReqParams.setPostFlag(false);
            futureHoldReqParams.setSingleTriggerFlag(false);

            try {
                processControlService.sxFutureHoldReq(objCommon,futureHoldReqParams);
            } catch (ServiceException e) {
                log.trace("!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()));
                if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode())) {
                    throw e;
                }
            }
            Params.FutureHoldReqParams futureHoldReqRepeat = new Params.FutureHoldReqParams();
            futureHoldReqRepeat.setHoldType(BizConstant.SP_HOLDTYPE_REWORKHOLD);
            futureHoldReqRepeat.setLotID(childWafer);
            futureHoldReqRepeat.setRouteID(processCompareCurrentOutRetCode.getCurrentRouteID());
            futureHoldReqRepeat.setOperationNumber(extendedCurrentOperationNumber);
            futureHoldReqRepeat.setReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_REWORKHOLD));
            futureHoldReqRepeat.setRelatedLotID(partialReworkReq.getParentLotID());
            futureHoldReqRepeat.setPostFlag(false);
            futureHoldReqRepeat.setSingleTriggerFlag(false);
            try {
                processControlService.sxFutureHoldReq(objCommon,futureHoldReqRepeat);
            } catch (ServiceException e) {
                log.trace("!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()));
                if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode())){
                    throw e;
                }
            }
        } else if (flag1) {
            //【Step34-2】;
            log.debug( "processCompareCurrent() return CURRENT_OPERATION_EARLY.");
            extendedReturnOperationNumber  = returnOperationNumber;
            log.debug( "extendedReturnOperationNumber = {} ", extendedReturnOperationNumber);

            //【Step1】;
            log.debug("set futureHoldReqParams");
            Params.FutureHoldReqParams futureHoldReqParams = new Params.FutureHoldReqParams();
            futureHoldReqParams.setHoldType(BizConstant.SP_HOLDTYPE_REWORKHOLD);
            futureHoldReqParams.setLotID(partialReworkReq.getParentLotID());
            futureHoldReqParams.setRouteID(processCompareCurrentOutRetCode.getCurrentRouteID());
            futureHoldReqParams.setOperationNumber(extendedReturnOperationNumber);
            futureHoldReqParams.setReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_REWORKHOLD));
            futureHoldReqParams.setRelatedLotID(childWafer);
            futureHoldReqParams.setPostFlag(false);
            futureHoldReqParams.setSingleTriggerFlag(false);

            try {
                processControlService.sxFutureHoldReq(objCommon,futureHoldReqParams);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode())){
                    throw e;
                }
            }
            //【Step2】;
            log.debug("set futureHoldReqRepeat param");
            Params.FutureHoldReqParams futureHoldReqRepeat = new Params.FutureHoldReqParams();
            futureHoldReqRepeat.setHoldType(BizConstant.SP_HOLDTYPE_REWORKHOLD);
            futureHoldReqRepeat.setLotID(childWafer);
            futureHoldReqRepeat.setRouteID(processCompareCurrentOutRetCode.getCurrentRouteID());
            futureHoldReqRepeat.setOperationNumber(extendedReturnOperationNumber);
            futureHoldReqRepeat.setReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_REWORKHOLD));
            futureHoldReqRepeat.setRelatedLotID(partialReworkReq.getParentLotID());
            futureHoldReqRepeat.setPostFlag(false);
            futureHoldReqRepeat.setSingleTriggerFlag(false);
            try {
                processControlService.sxFutureHoldReq(objCommon,futureHoldReqRepeat);
            } catch (ServiceException e) {
                log.trace("!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()));
                if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode())){
                    throw e;
                }
            }
        }

        log.trace("BooleanUtils.isTrue(updateControlJobFlag ) : {}", CimBooleanUtils.isTrue(updateControlJobFlag ));
        if (CimBooleanUtils.isTrue(updateControlJobFlag )) {
            //【Step35】Update control Job Info and Machine cassette info if information exist;
            log.debug("【Step35】Update control Job Info and Machine cassette info if information exist;");
            List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
            cassetteIDs.add(outLotCassetteGet);
            controlJobMethod.controlJobRelatedInfoUpdate(objCommon,cassetteIDs);

        }

        //【Step36】Check Bonding Group for childLot;
        log.debug("【Step36】Check Bonding Group for childLot;");
        log.debug( "processCompareCurrentOutRetCode.currentRouteID = {}", processCompareCurrentOutRetCode.getCurrentRouteID().getValue());
        log.debug( "returnOperationNumber = {}", returnOperationNumber);

        //【Step1】process_OperationProcessRefListForLot;
        log.debug("【Step36-1】process_OperationProcessRefListForLot;");
        Inputs.ObjProcessOperationProcessRefListForLotIn refListForLotIn = new Inputs.ObjProcessOperationProcessRefListForLotIn();
        refListForLotIn.setSearchDirection(true);
        refListForLotIn.setPosSearchFlag(true);
        refListForLotIn.setSearchCount(Integer.parseInt(BizConstant.SEARCH_COUNT_MAX.toString()));
        refListForLotIn.setCurrentFlag(true);
        refListForLotIn.setSearchRouteID(processCompareCurrentOutRetCode.getCurrentRouteID());
        refListForLotIn.setLotID(partialReworkReq.getParentLotID());
        refListForLotIn.setSearchOperationNumber(returnOperationNumber);
        List<Infos.OperationProcessRefListAttributes> refListForLotRepeatRetCode = processMethod.processOperationProcessRefListForLot(objCommon,refListForLotIn);

        //【Step2】lot_bondingFlowSection_CheckLocate;
        log.debug("【Step36-2】lot_bondingFlowSection_CheckLocate;");
        int operationCountForLot = CimArrayUtils.getSize(refListForLotRepeatRetCode);
        log.trace("operationCountForLot >= 1 : {}",operationCountForLot >= 1);
        if( operationCountForLot >= 1 ) {
            lotMethod.lotBondingFlowSectionCheckLocate(objCommon,
                    childWafer,
                    refListForLotRepeatRetCode.get(operationCountForLot - 1).getProcessRef());
        }

        //【Step37】 Update cassette's MultiLotType ;
        log.debug("【Step37】 Update cassette's MultiLotType ;");
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon,outLotCassetteGet);

        //【Step38】UpDate RequiredCassetteCategory;
        log.debug("【Step38】UpDate RequiredCassetteCategory;");
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon,childWafer);

        // 【Step39】Process Hold for Child lot;
        log.debug("【Step39】Process Hold for Child lot; ,Call sxProcessHoldDoActionReq() for Child lot");
        processControlService.sxProcessHoldDoActionReq(objCommon,childWafer,params.getClaimMemo());

        //【Step40】 Replace Target Operation for rework route;
        log.debug("【Step40】 Replace Target Operation for rework route;");
        Infos.QTimeTargetOpeReplaceIn qTimeReplaceParams = new Infos.QTimeTargetOpeReplaceIn();
        qTimeReplaceParams.setLotID(childWafer);
        qTimeReplaceParams.setSpecificControlFlag(false);
        qTimeMethod.qTimeTargetOpeReplace(objCommon,qTimeReplaceParams);

        minQTimeMethod.checkTargetOpeReplace(objCommon, childWafer);

        //【Step41】Make History;
        log.debug("Make History.");
        //【Step41-1】lot_waferLotHistoryPointer_Update;
        log.debug("【Step41-1】lot_waferLotHistoryPointer_Update;");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon,partialReworkReq.getParentLotID());

        //【Step41-2】lot_waferLotHistoryPointer_Update for created lot;
        log.debug("【Step41-2】lot_waferLotHistoryPointer_Update for created lot;");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon,childWafer);

        //Create History Data;
        //--------------------------------
        //   Decide TX_ID
        //--------------------------------
        log.debug("Create History Data;");
        String strTransactionID;
        log.trace("BooleanUtils.isTrue(params.getPartialReworkReqInformation().getBForceRework()) : {}", CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBForceRework()));
        if (CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBForceRework())){
            log.trace("BooleanUtils.isTrue(params.getPartialReworkReqInformation().getBDynamicRoute()) : {}", CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBDynamicRoute()));
            if (CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBDynamicRoute())){
                strTransactionID = TransactionIDEnum.PARTIAL_REWORK_FORCE_AND_DYNAMIC_ROUTE.getValue();
            } else {
                strTransactionID = TransactionIDEnum.PARTIAL_REWORK_FORCE.getValue();
            }
        } else {
            log.trace("BooleanUtils.isTrue(params.getPartialReworkReqInformation().getBDynamicRoute()) : {}", CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBDynamicRoute()));
            if (CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBDynamicRoute())){
                strTransactionID = TransactionIDEnum.PARTIAL_REWORK_DYNAMIC_ROUTE.getValue();
            } else {
                strTransactionID = TransactionIDEnum.PARTIAL_REWORK.getValue();
            }
        }
        log.debug("get lot wafer by parent lotID");
        List<Infos.LotWaferMap> lotWaferMapGetResult = lotMethod.lotWaferMapGet(objCommon, partialReworkReq.getParentLotID());

        List<Infos.LotWaferMap> lotWaferMapList = lotWaferMapGetResult;
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        newLotAttributes.setCassetteID(outLotCassetteGet);
        log.trace("!ObjectUtils.isEmpty(lotWaferMapList) : {}",!CimObjectUtils.isEmpty(lotWaferMapList));
        if (!CimObjectUtils.isEmpty(lotWaferMapList)){
            for (Infos.LotWaferMap lotWaferMap : lotWaferMapList){
                Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
                newWaferAttributes.setNewLotID(childWafer);
                newWaferAttributes.setNewWaferID(lotWaferMap.getWaferID());
                newWaferAttributes.setNewSlotNumber((int)lotWaferMap.getSlotNumber());
                newWaferAttributes.setSourceLotID(partialReworkReq.getParentLotID());
                newWaferAttributes.setSourceWaferID(lotWaferMap.getWaferID());
                newWaferAttributesList.add(newWaferAttributes);
            }
        }
        log.debug("create lot rework event");
        eventMethod.lotWaferMoveEventMake(objCommon, newLotAttributes, strTransactionID, params.getClaimMemo());
        Inputs.LotReworkEventMakeParams lotReworkEventMakeParams = new Inputs.LotReworkEventMakeParams();
        lotReworkEventMakeParams.setTransactionID(strTransactionID);
        lotReworkEventMakeParams.setLotID(childWafer);
        lotReworkEventMakeParams.setReasonCodeID(partialReworkReq.getReasonCodeID());
        lotReworkEventMakeParams.setOldCurrentPOData(oldCurrentPODataRetCode);
        lotReworkEventMakeParams.setClaimMemo(params.getClaimMemo());
        eventMethod.lotReworkEventMake(objCommon, lotReworkEventMakeParams);

        // check contamination level ，trigger contamination hold
        log.debug("check contamination level ，trigger contamination hold");
        contaminationMethod.lotCheckContaminationLevelStepOut(objCommon, childWafer);

        log.debug("【Method Exit】sxPartialReworkReq()");
        retVal.setCreatedLotID(childWafer);
        return retVal;
    }

    public Results.PartialReworkReqResult sxPartialReworkWithHoldReleaseReq(Infos.ObjCommon objCommon, Params.PartialReworkWithHoldReleaseReqParams partialReworkWithHoldReleaseReqParams) {
        /*----------------------------------------*/
        /*    Call txHoldLotReleaseReq            */
        /*----------------------------------------*/
        log.debug("Call txHoldLotReleaseReq");
        Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
        holdLotReleaseReqParams.setReleaseReasonCodeID(partialReworkWithHoldReleaseReqParams.getReleaseReasonCodeID());
        holdLotReleaseReqParams.setLotID(partialReworkWithHoldReleaseReqParams.getPartialReworkReq().getParentLotID());
        holdLotReleaseReqParams.setHoldReqList(partialReworkWithHoldReleaseReqParams.getHoldReqList());
        holdLotReleaseReqParams.setUser(objCommon.getUser());
        sxHoldLotReleaseReq(objCommon,holdLotReleaseReqParams);

        /*-----------------------------------------*/
        /*    Call txPartialReworkReq              */
        /*-----------------------------------------*/
        log.debug("Call txPartialReworkReq ");
        Params.PartialReworkReqParams params = new Params.PartialReworkReqParams();
        params.setPartialReworkReqInformation(partialReworkWithHoldReleaseReqParams.getPartialReworkReq());
        params.setClaimMemo(partialReworkWithHoldReleaseReqParams.getClaimMemo());


        return sxPartialReworkReq(objCommon, params);
    }

    @SuppressWarnings("deprecation")
    public Results.PartialReworkReqResult sxPartialReworkWithoutHoldReleaseReq(Infos.ObjCommon objCommon,
                                                                               Infos.PartialReworkReq partialReworkReq,
                                                                               List<Infos.LotHoldReq> lotHoldReqs,
                                                                               String claimMemo) {
        Validations.check(null == partialReworkReq, retCodeConfig.getInvalidInputParam(), objCommon.getTransactionID());
        Results.PartialReworkReqResult retVal = new Results.PartialReworkReqResult();

        ObjectIdentifier parentLotID = partialReworkReq.getParentLotID();
        String tmpRtnOpeNum = partialReworkReq.getReturnOperationNumber();
        ObjectIdentifier subRouteID = partialReworkReq.getSubRouteID();

        //===========================================================================
        // Lock for objects
        //===========================================================================
        //----- Gets Parent lot's cassette. -------//
        log.debug("step1 - Gets Parent lot's cassette.");
        ObjectIdentifier lotCassetteOut = lotMethod.lotCassetteGet(objCommon, parentLotID);

        ObjectIdentifier cassetteID = lotCassetteOut;

        String lotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        Boolean updateControlJobFlag = false;
        Long lockMode = 0L;
        String transferState = null;
        Long equipmentLockModeWrite = BizConstant.SP_EQP_LOCK_MODE_WRITE;
        log.trace("ObjectUtils.equalsWithValue(\"0\",lotOperationEIcheck) : {}", CimObjectUtils.equalsWithValue("0",lotOperationEIcheck));
        if (CimObjectUtils.equalsWithValue("0",lotOperationEIcheck)) {
            //-------------------------------
            // Get carrier transfer status
            //-------------------------------
            log.debug("step2 - Get carrier transfer status");
            String cassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
            transferState = cassetteTransferStateGetOut;

            /*------------------------------------*/
            /*   Get eqp ID in cassette     */
            /*------------------------------------*/
            log.debug("step3 - Get eqp ID in cassette");
            Outputs.ObjCassetteEquipmentIDGetOut cassetteEquipmentIDOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, cassetteID);

            //-------------------------------
            // Get required eqp lock mode
            //-------------------------------
            // object_lockMode_Get
            log.debug("step4 - Get required eqp lock mode");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(cassetteEquipmentIDOut.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            log.trace("StringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState) : {}", CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState));
            if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) {
                updateControlJobFlag = true;
                log.trace("!lockMode.equals(equipmentLockModeWrite) : {}",!lockMode.equals(equipmentLockModeWrite));
                if (!lockMode.equals(equipmentLockModeWrite)) {
                    // advanced_object_Lock
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(cassetteEquipmentIDOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                    // advanced_object_Lock
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(cassetteEquipmentIDOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                            (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, Collections.singletonList(cassetteID.getValue())));
                } else {
                    // object_Lock
                    objectLockMethod.objectLock(objCommon, CimMachine.class, cassetteEquipmentIDOut.getEquipmentID());
                }
            }
        }

        //----- Lock for cassette -------//
        // object_Lock
        log.debug("step5 -  Lock for cassette");
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
        log.debug("ObjectUtils.equalsWithValue(\"0\", lotOperationEIcheck) : {}", CimObjectUtils.equalsWithValue("0", lotOperationEIcheck));
        if (CimObjectUtils.equalsWithValue("0", lotOperationEIcheck)) {
            log.trace("BooleanUtils.isFalse(updateControlJobFlag) || !lockMode.equals(equipmentLockModeWrite) : {}", CimBooleanUtils.isFalse(updateControlJobFlag) || !lockMode.equals(equipmentLockModeWrite));
            if (CimBooleanUtils.isFalse(updateControlJobFlag) || !lockMode.equals(equipmentLockModeWrite)) {
                //---------------------------------
                //   Get cassette's ControlJobID
                //---------------------------------
                log.debug("step6 - Get cassette's ControlJobID");
                ObjectIdentifier cassetteControlJobIDOut = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
                log.trace("!ObjectUtils.isEmpty(cassetteControlJobIDOut) : {}",!CimObjectUtils.isEmpty(cassetteControlJobIDOut));
                if (!CimObjectUtils.isEmpty(cassetteControlJobIDOut)) {
                    updateControlJobFlag = true;
                    log.trace("!lockMode.equals(equipmentLockModeWrite) : {}",!lockMode.equals(equipmentLockModeWrite));
                    if (!lockMode.equals(equipmentLockModeWrite)) {
                        /*------------------------------*/
                        /*   Lock controljob Object     */
                        /*------------------------------*/
                        // object_Lock
                        log.debug("Lock controljob Object");
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, cassetteControlJobIDOut);
                    }
                }
            }
        }

        //----- Lock for Parent lot -------//
        // object_Lock
        log.debug("Lock for Parent lot");
        objectLockMethod.objectLock(objCommon, CimLot.class, parentLotID);

        /*-------------------------------*/
        /*   Check SorterJob existence   */
        /*-------------------------------*/
        //waferSorter_sorterJob_CheckForOperation
        log.debug("step7 - Check SorterJob existence");
        List<ObjectIdentifier> dummyCastIDs = new ArrayList<>();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();

        lotIDs.add(partialReworkReq.getParentLotID());
        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(new Infos.EquipmentLoadPortAttribute());
        objWaferSorterJobCheckForOperation.setCassetteIDList(dummyCastIDs);
        objWaferSorterJobCheckForOperation.setLotIDList(lotIDs);
        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);
        log.debug("wafer Sorter sorterJob Check For Operation");
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);

        //===========================================================================
        // Check for conditions
        //===========================================================================
        //-----------------------------------------------------------
        // Check for consistency of Sub Route and Return Operation
        //-----------------------------------------------------------
        //----- Converts Sub Route which is specified by input-parameter into the active one. -------//
        log.debug("Converts Sub Route which is specified by input-parameter into the active");
        log.debug("step8 - processActiveIDGet");
        subRouteID = processMethod.processActiveIDGet(objCommon, subRouteID);

        //----- Gets Parent lot's Current Operation. -------//
        log.debug("Gets Parent lot's Current Operation.");
        log.debug("step9 - lotCurrentOperationInfoGet");
        Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, parentLotID);

        //----- Check for Current Route : It should not be same as Sub Route. -------//
        log.debug("Check for Current Route : It should not be same as Sub Route.");
        Outputs.ObjLotCurrentOperationInfoGetOut operationInfoGetDROut = lotCurrentOperationInfoGetOut;
        Validations.check (CimObjectUtils.equalsWithValue(operationInfoGetDROut.getRouteID(),subRouteID), retCodeConfig.getInvalidBranchRouteId());

        //----- Gets Parent lot's Original Route. -------//
        log.debug("Gets Parent lot's Original Route.");
        log.debug("step10 - lotOriginalRouteListGet");
        Outputs.ObjLotOriginalRouteListGetOut originalRouteListGetOut = lotMethod.lotOriginalRouteListGet(objCommon, parentLotID);

        //----- Check for Original Route : It should not be same as Sub Route. -------//
        log.debug("Check for Original Route : It should not be same as Sub Route.");
        Outputs.ObjLotOriginalRouteListGetOut objLotOriginalRouteListGetOut = originalRouteListGetOut;
        List<ObjectIdentifier> originalRouteIDs = objLotOriginalRouteListGetOut.getOriginalRouteID();
        for (ObjectIdentifier originalRouteID : originalRouteIDs) {
            Validations.check (CimObjectUtils.equalsWithValue(originalRouteID,subRouteID), retCodeConfig.getInvalidBranchRouteId());
        }

        //----- Gets Sub Route's Dynamic Route Flag. -------//
        log.debug("Gets Sub Route's Dynamic Route Flag.");
        log.debug("step11 - processCheckForDynamicRoute");
        Outputs.ObjProcessCheckForDynamicRouteOut dynamicRouteOut = processMethod.processCheckForDynamicRoute(objCommon, subRouteID);
        //----- Check for Dynamic Route Flag -------//
        log.debug("Check for Dynamic Route Flag");
        Validations.check (BaseStaticMethod.isTrue(partialReworkReq.getBDynamicRoute()) && !BaseStaticMethod.isTrue(dynamicRouteOut.getDynamicRouteFlag()), retCodeConfig.getNotDynamicRoute());

        //----- Gets Sub Route's Return Operation. -------//
        log.debug("Gets Sub Route's Return Operation.");
        Outputs.ObjProcessGetReturnOperationOut returnOperationOut = null;
        Boolean returnOperationCodeFlag = false;
        try{
            log.debug("processGetReturnOperation");
            returnOperationOut = processMethod.processGetReturnOperation(objCommon, parentLotID, subRouteID);
            returnOperationCodeFlag = true;
        }catch (ServiceException ex){
            log.trace("!Validations.isEquals(retCodeConfig.getNotFoundSubRoute(),ex.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotFoundSubRoute(),ex.getCode()));
            if (!Validations.isEquals(retCodeConfig.getNotFoundSubRoute(),ex.getCode())) {
                throw ex;
            }
            returnOperationOut = (Outputs.ObjProcessGetReturnOperationOut) ex.getData();
        }

        //----- Check for ProcessDefinitionType of Sub Route -------//
        log.debug("step12 Check for ProcessDefinitionType of Sub Route");
        Outputs.ObjProcessGetReturnOperationOut objProcessGetReturnOperationOut = returnOperationOut;
        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_MAINPDTYPE_REWORK, objProcessGetReturnOperationOut.getProcessDefinitionType()), retCodeConfig.getInvalidRouteType());

        //----- Check for Return Operation -------//
        Boolean connectedRouteReturnOperationFlag = false;
        log.trace("BooleanUtils.isTrue(returnOperationCodeFlag) : {}", CimBooleanUtils.isTrue(returnOperationCodeFlag));
        if (CimBooleanUtils.isTrue(returnOperationCodeFlag)) {
            connectedRouteReturnOperationFlag = true;
            log.trace("ObjectUtils.equalsWithValue(objProcessGetReturnOperationOut.getOperationNumber(),tmpRtnOpeNum) : {}", CimObjectUtils.equalsWithValue(objProcessGetReturnOperationOut.getOperationNumber(),tmpRtnOpeNum));
            log.trace("!BaseStaticMethod.isTrue(partialReworkReq.getBDynamicRoute()) : {}",!BaseStaticMethod.isTrue(partialReworkReq.getBDynamicRoute()));
            if (CimObjectUtils.equalsWithValue(objProcessGetReturnOperationOut.getOperationNumber(),tmpRtnOpeNum)) {
                log.debug("Return Operation which is specified by input-parameter is correct.");
            } else if (!BaseStaticMethod.isTrue(dynamicRouteOut.getDynamicRouteFlag()) && CimObjectUtils.isEmpty(tmpRtnOpeNum)) {
                log.debug("Return Operation is Invalid !!! This is not same as Sub Route's Return Operation.");
                Validations.check(retCodeConfig.getInvalidInputParam());
            }
        } else if (!BaseStaticMethod.isTrue(partialReworkReq.getBDynamicRoute())) {
            log.debug("Dynamic Route Flag is Invalid !!! Sub Route doesn't have Return Operation.");
            Validations.check(retCodeConfig.getInvalidRouteId());
        }
        log.debug("This is Sub Route which is used at after process.{}",subRouteID.getValue());
        //---------------------------------------
        // Check interFabXferPlan existence
        //---------------------------------------
        log.debug("Check interFabXferPlan existence");
        log.debug("TODO-NOTIMPL: step - call process_CheckInterFabXferPlanSkip");
        //TODO-NOTIMPL: call process_CheckInterFabXferPlanSkip
        Inputs.ObjProcessCheckInterFabXferPlanSkipIn inPut = new Inputs.ObjProcessCheckInterFabXferPlanSkipIn();
        inPut.setLotID(parentLotID);
        inPut.setCurrentRouteID(lotCurrentOperationInfoGetOut.getRouteID());
        inPut.setCurrentOpeNo(lotCurrentOperationInfoGetOut.getOperationNumber());
        inPut.setJumpingRouteID(lotCurrentOperationInfoGetOut.getRouteID());
        inPut.setJumpingOpeNo(partialReworkReq.getReturnOperationNumber());
        // TODO: 2019/10/15 tobe confirm
        processMethod.processCheckInterFabXferPlanSkip(objCommon, inPut);

        //-----------------------------------------------------------
        // Check for Bonding Group of Parent lot
        //-----------------------------------------------------------
        log.debug("Check for Bonding Group of Parent lot");
        log.debug("step13 - lotBondingGroupIDGetDR");
        String bondingGroupIDGetDROut = lotMethod.lotBondingGroupIDGetDR(objCommon, parentLotID);
        Validations.check(!CimObjectUtils.isEmpty(bondingGroupIDGetDROut),retCodeConfig.getLotHasBondingGroup(),parentLotID.getValue(),bondingGroupIDGetDROut);
        //-----------------------------------------------------------
        // Check for contents of Parent lot
        //-----------------------------------------------------------
        log.debug("Check for contents of Parent lot");
        //----- Gets Parent lot's contents. -------//
        log.debug("Gets Parent lot's contents.");
        log.debug("step14 - lotContentsGet");
        String lotContentsGet = lotMethod.lotContentsGet(parentLotID);
        //----- Check for contents : It should be "wafer" or "Die". -------//
        log.debug("Check for contents : It should be 'wafer' or 'Die'.");
        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER, lotContentsGet) && !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE, lotContentsGet), retCodeConfig.getInvalidLotContents(),parentLotID);

        //-----------------------------------------------------------
        // Check for each state of Parent lot
        //-----------------------------------------------------------
        log.debug("Check for each state of Parent lot");
        //----- Gets Parent lot's all state. -------//
        log.debug("Gets Parent lot's all state.");
        log.debug("step15 - lotAllStateGet");
        Outputs.ObjLotAllStateGetOut lotAllStateGetOutRetCode = lotMethod.lotAllStateGet(objCommon, parentLotID);

        Outputs.ObjLotAllStateGetOut objLotAllStateGetOut = lotAllStateGetOutRetCode;
        //----- Check for lot State : It should be "ACTIVE". -------//
        log.debug("Check for lot State : It should be 'ACTIVE'.");
        Validations.check (!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_STATE_ACTIVE, objLotAllStateGetOut.getLotState()), retCodeConfig.getInvalidLotStat(),objLotAllStateGetOut.getLotState());
        //----- Check for lot Hold State : It should be "ONHOLD". -------//
        log.debug("Check for lot Hold State : It should be 'ONHOLD'.");
        Validations.check (!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, objLotAllStateGetOut.getHoldState()), retCodeConfig.getInvalidLotHoldStat(),parentLotID.getValue(),objLotAllStateGetOut.getHoldState());
        //---- But, the lot should not be held by "LOCK". -----//
        log.debug("But, the lot should not be held by 'LOCK'.");
        //lot_CheckLockHoldConditionForOperation
        log.debug("step16 - lot_CheckLockHoldConditionForOperation");
        lotMethod.lotCheckLockHoldConditionForOperation(objCommon, lotIDs);
        //----- Check for lot Process State : It should not be "Processing". -------//
        log.debug("Check for lot Process State : It should not be 'Processing'");
        Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_PROCSTATE_PROCESSING, objLotAllStateGetOut.getProcessState()),
                retCodeConfig.getInvalidLotProcessState(),parentLotID,objLotAllStateGetOut.getProcessState());
        //----- Check for lot Production State : It should not be "INREWORK". -------//
        log.debug("Check for lot Production State : It should not be 'INREWORK'");
        Validations.check (CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_PRODUCTION_STATE_INREWORK, objLotAllStateGetOut.getProductionState()),
                retCodeConfig.getInvalidLotProductionState(),parentLotID.getValue(),objLotAllStateGetOut.getProductionState());
        //----- Check for lot Inventory State : It should be "OnFloor". -------//
        log.debug("Check for lot Inventory State : It should be 'OnFloor'");
        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, objLotAllStateGetOut.getInventoryState()),
                retCodeConfig.getInvalidLotInventoryStat(),parentLotID,objLotAllStateGetOut.getInventoryState());

        //-----------------------------------------------------------
        // Check for Control Job of Parent lot
        //-----------------------------------------------------------
        log.debug("Check for Control Job of Parent lot");
        //----- Gets Parent lot's Control Job. -------//
        log.debug("Gets Parent lot's Control Job.");
        log.debug("step17 - lotControlJobIDGet");
        ObjectIdentifier controlJobIDGetOut = lotMethod.lotControlJobIDGet(objCommon, parentLotID);
        //----- Check for Control Job : It should not exist. -------//
        log.debug("Check for Control Job : It should not exist.");

        Validations.check(!CimObjectUtils.isEmpty(controlJobIDGetOut),
                new OmCode(retCodeConfig.getLotControlJobidFilled()
                        , CimObjectUtils.getObjectValue(parentLotID), CimObjectUtils.getObjectValue(controlJobIDGetOut)));

        log.trace("ObjectUtils.equalsWithValue(\"1\", lotOperationEIcheck) : {}", CimObjectUtils.equalsWithValue("1", lotOperationEIcheck));
        if (CimObjectUtils.equalsWithValue("1", lotOperationEIcheck)) {
            //-------------------------------
            // Check carrier dispatch status
            //-------------------------------
            log.debug("Check carrier dispatch status");
            Boolean cassetteDispatchStateOut = cassetteMethod.cassetteDispatchStateGet(objCommon, cassetteID);
            Validations.check (BaseStaticMethod.isTrue(cassetteDispatchStateOut), retCodeConfig.getAlreadyDispatchReservedCassette());
        }

        //-----------------------------------------------------------
        // Check for current Operation of Parent lot
        //-----------------------------------------------------------
        log.debug("Check for current Operation of Parent lot");
        //----- Gets Parent lot's current and previous Operation. -------//
        log.debug("Gets Parent lot's current and previous Operation.");
        Inputs.ObjProcessOperationProcessRefListForLotIn in = new Inputs.ObjProcessOperationProcessRefListForLotIn();
        in.setSearchDirection(false);
        in.setPosSearchFlag(true);
        in.setSearchCount(2);
        in.setCurrentFlag(true);
        in.setLotID(parentLotID);
        log.debug("step - processOperationProcessRefListForLot");
        List<Infos.OperationProcessRefListAttributes> processOut = processMethod.processOperationProcessRefListForLot(new Infos.ObjCommon(), in);
        //----- Check for Operaions : Current Operation should be the first one or be same as the previous one. -------//
        log.debug("Check for Operaions : Current Operation should be the first one or be same as the previous one.");
        List<Infos.OperationProcessRefListAttributes> operationProcessRefListAttributesList = processOut;
        Integer opeLen = CimArrayUtils.getSize(operationProcessRefListAttributesList);
        int searchCount = Integer.parseInt(BizConstant.SEARCH_COUNT);
        Validations.check (opeLen < searchCount
                        || (opeLen >= searchCount
                        && !CimObjectUtils.equalsWithValue(operationProcessRefListAttributesList.get(0).getRouteID(),operationProcessRefListAttributesList.get(1).getRouteID())),
                retCodeConfig.getInvalidReworkOperation());

        //-----------------------------------------------------------
        // Check for Future Hold Request of Parent lot
        //-----------------------------------------------------------
        log.debug("Check for Future Hold Request of Parent lot");
        log.debug("step - lotFutureHoldRequestsCheckSplit");
        lotMethod.lotFutureHoldRequestsCheckSplit(objCommon, parentLotID,
                "", partialReworkReq.getReturnOperationNumber());

        //-----------------------------------------------------------
        // Check for Xfer State of cassette
        //-----------------------------------------------------------
        log.debug("Check for Xfer State of cassette");
        //----- Gets cassette's Xfer State. -------//
        log.debug("Gets cassette's Xfer State. ");
        String xferTransferState = null;
        log.trace("ObjectUtils.equalsWithValue(\"1\", lotOperationEIcheck) || (ObjectUtils.equalsWithValue(\"0\", lotOperationEIcheck)\n" +
                "                && !ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) : {}", CimObjectUtils.equalsWithValue("1", lotOperationEIcheck) || (CimObjectUtils.equalsWithValue("0", lotOperationEIcheck)
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)));
        if (CimObjectUtils.equalsWithValue("1", lotOperationEIcheck) || (CimObjectUtils.equalsWithValue("0", lotOperationEIcheck)
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState))) {
            log.debug("step - cassetteTransferStateGet");
            String cassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
            xferTransferState = cassetteTransferStateGetOut;

            log.trace("ObjectUtils.equalsWithValue(\"0\", lotOperationEIcheck) : {}", CimObjectUtils.equalsWithValue("0", lotOperationEIcheck));
            if (CimObjectUtils.equalsWithValue("0", lotOperationEIcheck)) {
                log.debug("lotOperationEIcheck = 0");
                Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, xferTransferState),
                        retCodeConfig.getChangedToEiByOtherOperation(),cassetteID);
            } else {
                //----- Check for Xfer State : It should not be "BO" and "EI". -------//
                log.debug("lotOperationEIcheck = 1");
                log.debug("Check for Xfer State : It should not be 'BO' and 'EI'.");
                Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT, xferTransferState)
                                || CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, xferTransferState),
                        retCodeConfig.getInvalidCassetteTransferState(),
                        xferTransferState,cassetteID);
            }
        }
        log.trace("ObjectUtils.equalsWithValue(\"0\", lotOperationEIcheck) : {}", CimObjectUtils.equalsWithValue("0", lotOperationEIcheck));
        if (CimObjectUtils.equalsWithValue("0", lotOperationEIcheck)) {
            xferTransferState = transferState;
        }

        //-----------------------------------------------------------
        // Check for consistency of Hold Requests
        //-----------------------------------------------------------
        log.debug("Check for consistency of Hold Requests");
        Integer processHoldAllowLotMovement = StandardProperties.OM_PROCESS_HOLD_ENABLE_WAFER_MOVE.getIntValue();
        Boolean inheritProcessHold = false;
        List<Infos.LotHoldReq> procHoldReq = new ArrayList<>();
        List<Infos.LotHoldReq> holdReqWithoutProcHold = new ArrayList<>();
        for (Infos.LotHoldReq lotHoldReq : lotHoldReqs) {
            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_HOLDTYPE_PROCESSHOLD, lotHoldReq.getHoldType()) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_HOLDTYPE_PROCESSHOLD, lotHoldReq.getHoldType()));
            log.trace("!ObjectUtils.isEmpty(lotHoldReq.getRelatedLotID()) : {}",!CimObjectUtils.isEmpty(lotHoldReq.getRelatedLotID()));
            if (CimObjectUtils.equalsWithValue(BizConstant.SP_HOLDTYPE_PROCESSHOLD, lotHoldReq.getHoldType())) {
                log.debug("Process Hold cannot be inherited.");
                Validations.check (processHoldAllowLotMovement == 0,
                        retCodeConfig.getHoldRecordCannotInherit(),
                        lotHoldReq.getHoldType(),lotHoldReq.getHoldReasonCodeID(),
                        lotHoldReq.getHoldUserID().getValue(),lotHoldReq.getRelatedLotID());
                inheritProcessHold = true;
                procHoldReq.add(lotHoldReq);
            } else if (!CimObjectUtils.isEmpty(lotHoldReq.getRelatedLotID())) {
                log.debug("Merge Hold, Rework Hold and so on cannot be inherited.");
                Validations.check(true,retCodeConfig.getHoldRecordCannotInherit(),
                        lotHoldReq.getHoldType(),lotHoldReq.getHoldReasonCodeID(),
                        lotHoldReq.getHoldUserID(),lotHoldReq.getRelatedLotID());
            } else {
                log.debug("Inherited Hold Request");
                holdReqWithoutProcHold.add(lotHoldReq);
            }
        }

        //---------------------------
        // Get the lot's hold list.
        //---------------------------
        log.debug("Get the lot's hold list.{}",parentLotID.getValue());
        List<Infos.LotHoldListAttributes> lotFillInTxTRQ005DROut = null;
        try {
            log.debug("step - lotFillInTxTRQ005DR");
            lotFillInTxTRQ005DROut = lotMethod.lotFillInTxTRQ005DR(objCommon, parentLotID);
            //ok
            //---------------------------
            // Check Inherit Hold Record
            //---------------------------
            log.debug("Check Inherit Hold Record");
            Boolean existFlag;
            for (Infos.LotHoldReq lotHoldReq : holdReqWithoutProcHold) {
                existFlag = false;
                for (Infos.LotHoldListAttributes lotHoldListAttributes : lotFillInTxTRQ005DROut) {
                    log.trace("ObjectUtils.equalsWithValue(lotHoldReq.getHoldType(),lotHoldListAttributes.getHoldType())\n" +
                            "                            && ObjectUtils.equalsWithValue(lotHoldReq.getHoldReasonCodeID(),lotHoldListAttributes.getReasonCodeID())\n" +
                            "                            && ObjectUtils.equalsWithValue(lotHoldReq.getHoldUserID(), lotHoldListAttributes.getUserID())\n" +
                            "                            && ObjectUtils.equalsWithValue(lotHoldReq.getRelatedLotID(),lotHoldListAttributes.getRelatedLotID()) : {}", CimObjectUtils.equalsWithValue(lotHoldReq.getHoldType(),lotHoldListAttributes.getHoldType())
                            && CimObjectUtils.equalsWithValue(lotHoldReq.getHoldReasonCodeID(),lotHoldListAttributes.getReasonCodeID())
                            && CimObjectUtils.equalsWithValue(lotHoldReq.getHoldUserID(), lotHoldListAttributes.getUserID())
                            && CimObjectUtils.equalsWithValue(lotHoldReq.getRelatedLotID(),lotHoldListAttributes.getRelatedLotID()));
                    if (CimObjectUtils.equalsWithValue(lotHoldReq.getHoldType(),lotHoldListAttributes.getHoldType())
                            && CimObjectUtils.equalsWithValue(lotHoldReq.getHoldReasonCodeID(),lotHoldListAttributes.getReasonCodeID())
                            && CimObjectUtils.equalsWithValue(lotHoldReq.getHoldUserID(), lotHoldListAttributes.getUserID())
                            && CimObjectUtils.equalsWithValue(lotHoldReq.getRelatedLotID(),lotHoldListAttributes.getRelatedLotID())) {
                        log.debug("Hold Record Exist!");
                        existFlag = true;
                        break;
                    }
                }
                Validations.check (CimBooleanUtils.isFalse(existFlag), retCodeConfig.getHoldRecordCannotInherit(),
                        lotHoldReq.getHoldType(),lotHoldReq.getHoldReasonCodeID(),
                        lotHoldReq.getHoldUserID(),lotHoldReq.getRelatedLotID());
            }
        } catch (ServiceException e) {
            log.trace("!Validations.isEquals(retCodeConfig.getNotFoundEntry(),e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotFoundEntry(),e.getCode()));
            if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(),e.getCode())) {
                throw e;
            }
            log.trace("!ObjectUtils.isEmpty(holdReqWithoutProcHold) : {}",!CimObjectUtils.isEmpty(holdReqWithoutProcHold));
            if (!CimObjectUtils.isEmpty(holdReqWithoutProcHold)){
                throw new ServiceException(new OmCode(retCodeConfig.getHoldRecordCannotInherit(),
                        holdReqWithoutProcHold.get(0).getHoldType(),holdReqWithoutProcHold.get(0).getHoldReasonCodeID().getValue(),
                        holdReqWithoutProcHold.get(0).getHoldUserID().getValue(),holdReqWithoutProcHold.get(0).getRelatedLotID().getValue()));
            }
        }
        //===========================================================================
        // Preparation for change
        //===========================================================================
        log.debug("Preparation for change");
        //----- Decides Return Operation. -------//
        log.debug("Decides Return Operation.");
        String returnOperationNumber = null;
        log.trace("!ObjectUtils.isEmpty(tmpRtnOpeNum) : {}",!CimObjectUtils.isEmpty(tmpRtnOpeNum));
        if (!CimObjectUtils.isEmpty(tmpRtnOpeNum)) {
            returnOperationNumber = tmpRtnOpeNum;
        } else if (BaseStaticMethod.isTrue(partialReworkReq.getBDynamicRoute())) {
            log.trace("BooleanUtils.isTrue(connectedRouteReturnOperationFlag) : {}", CimBooleanUtils.isTrue(connectedRouteReturnOperationFlag));
            if (CimBooleanUtils.isTrue(connectedRouteReturnOperationFlag)) {
                returnOperationNumber = objProcessGetReturnOperationOut.getOperationNumber();
            } else {
                returnOperationNumber = operationInfoGetDROut.getOperationNumber();
            }
        } else {
            returnOperationNumber = objProcessGetReturnOperationOut.getOperationNumber();
        }

        //----- Gets all Operations on Parent lot's Route. -------//
        log.debug("Gets all Operations on Parent lot's Route.");
        log.debug("step - sxProcessFlowOperationListForLotInq");
        List<Infos.OperationNameAttributes> processFlowOperationListInqResultOut = lotInqService.sxProcessFlowOperationListForLotInq(objCommon, parentLotID);

        //----- Check for the decided Return Operation : It should be exist. -------//
        log.debug("Check for the decided Return Operation : It should be exist.");
        List<Infos.OperationNameAttributes> operationNameAttributesList = processFlowOperationListInqResultOut;
        for (int i = 0, j = CimArrayUtils.getSize(operationNameAttributesList); i < j; i++) {
            log.trace("ObjectUtils.equalsWithValue(operationNameAttributesList.get(i).getOperationNumber(),returnOperationNumber) : {}", CimObjectUtils.equalsWithValue(operationNameAttributesList.get(i).getOperationNumber(),returnOperationNumber));
            if (CimObjectUtils.equalsWithValue(operationNameAttributesList.get(i).getOperationNumber(),returnOperationNumber)) {
                log.debug("Return Operation is found.");
                break;
            } else if (i == j - 1) {
                log.error("Return Operation {} is Invelid!!! This is not on Parent Lot's Route.",returnOperationNumber);
                Validations.check(true,retCodeConfig.getNotFoundOperation(),returnOperationNumber);
            }
        }
        log.debug("This is Return Operation {} which is used at after process.",returnOperationNumber);

        //===========================================================================
        // Change for objects
        //===========================================================================
        log.debug("Change for objects");
        //-----------------------------------------------------------
        // Creates Child lot
        //-----------------------------------------------------------
        log.debug("Creates Child lot");
        log.debug("step - lotSplitWaferLot");
        ObjectIdentifier objectIdentifierRetCode = lotMethod.lotSplitWaferLot(objCommon, parentLotID, partialReworkReq.getChildWaferID());

        //inherit the contamination flag from parent lot
        log.debug("inherit the contamination flag from parent lot");
        contaminationMethod.inheritContaminationFlagFromParentLot(parentLotID,objectIdentifierRetCode);

        Results.PartialReworkWithoutHoldReleaseReqResult partialReworkWithoutHoldReleaseReqResult = new Results.PartialReworkWithoutHoldReleaseReqResult();
        ObjectIdentifier childLotID = objectIdentifierRetCode;
        partialReworkWithoutHoldReleaseReqResult.setChildLotID(childLotID);

        //----- Updates History Time Stamp to Parent lot : Those data are used in Event. -------//
        log.debug("Updates History Time Stamp to Parent lot : Those data are used in Event.");
        log.debug("step - lotWaferLotHistoryPointerUpdate");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, parentLotID);
        //----- Updates History Time Stamp to Child lot : Those data are used in Event. -------//
        log.debug("Updates History Time Stamp to Child lot : Those data are used in Event.");
        log.debug("step - lotWaferLotHistoryPointerUpdate");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, childLotID);
        //----- Prepares data of Split Event. -------//
        log.debug("Prepares data of Split Event.");
        //lot_waferMap_Get
        log.debug("step - lot_waferMap_Get");
        List<Infos.LotWaferMap> lotWaferMapGetResult = lotMethod.lotWaferMapGet(objCommon, childLotID);

        //----- Makes Split Event. -------//
        log.debug("Makes Split Event.");
        //lotWaferMoveEvent_Make
        log.debug("step - lotWaferMoveEvent_Make");
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        newLotAttributes.setCassetteID(cassetteID);
        List<Infos.LotWaferMap> lotWaferMapList = lotWaferMapGetResult;
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        log.trace("!ObjectUtils.isEmpty(lotWaferMapList) : {}",!CimObjectUtils.isEmpty(lotWaferMapList));
        if (!CimObjectUtils.isEmpty(lotWaferMapList)){
            for (Infos.LotWaferMap lotWaferMap : lotWaferMapList){
                Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
                newWaferAttributes.setNewLotID(childLotID);
                newWaferAttributes.setNewWaferID(lotWaferMap.getWaferID());
                newWaferAttributes.setNewSlotNumber((int)lotWaferMap.getSlotNumber());
                newWaferAttributes.setSourceLotID(parentLotID);
                newWaferAttributes.setSourceWaferID(lotWaferMap.getWaferID());
                newWaferAttributesList.add(newWaferAttributes);
            }
        }
        eventMethod.lotWaferMoveEventMake(objCommon, newLotAttributes, TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ.getValue(), claimMemo);
        log.trace("BooleanUtils.isTrue(inheritProcessHold) : {}", CimBooleanUtils.isTrue(inheritProcessHold));
        if (CimBooleanUtils.isTrue(inheritProcessHold)) {
            //-----------------------------------------------------------
            // Performs Process Hold to Child lot
            //-----------------------------------------------------------
            log.debug("Performs Process Hold to Child lot");
            log.debug("step - sxHoldLotReq");
            sxHoldLotReq(objCommon, childLotID, procHoldReq);
        }

        //-----------------------------------------------------------
        // Performs Rework to Child lot
        //-----------------------------------------------------------
        log.debug("Performs Rework to Child lot");
        //----- Check for Rework Count of Child lot -------//
        log.debug("Check for Rework Count of Child lot");
        log.trace("!BaseStaticMethod.isTrue(partialReworkReq.getBForceRework()) : {}",!BaseStaticMethod.isTrue(partialReworkReq.getBForceRework()));
        if (!BaseStaticMethod.isTrue(partialReworkReq.getBForceRework())) {
            log.debug("step - processReworkCountCheck");
            processMethod.processReworkCountCheck(objCommon, childLotID);
        }
        //----- Increments Rework Count of Child lot. -------//
        log.debug("Increments Rework Count of Child lot.");
        log.debug("step - processReworkCountIncrement");
        processMethod.processReworkCountIncrement(objCommon, childLotID);
        log.debug("step - qTimeCheckConditionForReplaceTarget");
        qTimeMethod.qTimeCheckConditionForReplaceTarget(objCommon, childLotID);
        //----- Moves Child lot to Sub Route. -------//
        log.debug("Moves Child lot to Sub Route.");
        log.debug("step - processBranchRoute");
        Inputs.OldCurrentPOData oldCurrentPODataRetCode = processMethod.processBranchRoute(objCommon, childLotID, subRouteID, returnOperationNumber);
        //----- Makes Branch Event. -------//
        log.debug("Makes Branch Event. ");
        //lotReworkEvent_Make
        log.debug("step - lotReworkEvent_Make");
        Inputs.LotReworkEventMakeParams lotReworkEventMakeParams = new Inputs.LotReworkEventMakeParams();
        lotReworkEventMakeParams.setClaimMemo(claimMemo);
        lotReworkEventMakeParams.setLotID(childLotID);
        lotReworkEventMakeParams.setReasonCodeID(partialReworkReq.getReasonCodeID());
        lotReworkEventMakeParams.setOldCurrentPOData(oldCurrentPODataRetCode);
        lotReworkEventMakeParams.setTransactionID(TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ.getValue());
        eventMethod.lotReworkEventMake(objCommon, lotReworkEventMakeParams);
        //-----------------------------------------------------------
        // Performs Rework Hold
        //-----------------------------------------------------------
        log.debug("Performs Rework Hold");
        ObjectIdentifier reasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_REWORKHOLD);
        String operationNumber = returnOperationNumber;
        Outputs.ObjProcessCompareCurrentOut processCompareCurrentOut = null;
        //----- Performs Rework Hold Request to Lot, which is on Return Point or more forward Operation. -------//
        log.debug("Performs Rework Hold Request to Lot, which is on Return Point or more forward Operation.");
        try{
            log.debug("step - processCompareCurrent");
            processCompareCurrentOut = processMethod.processCompareCurrent(objCommon, parentLotID, subRouteID, returnOperationNumber);
        }catch (ServiceException ex){
            processCompareCurrentOut = ex.getData(Outputs.ObjProcessCompareCurrentOut.class);
            log.debug("Validations.isEquals(retCodeConfig.getCurrentToperationSame(),ex.getCode())\n" +
                    "                    || Validations.isEquals(retCodeConfig.getCurrentToperationLate(),ex.getCode()) : {}",Validations.isEquals(retCodeConfig.getCurrentToperationSame(),ex.getCode())
                    || Validations.isEquals(retCodeConfig.getCurrentToperationLate(),ex.getCode()));
            if (Validations.isEquals(retCodeConfig.getCurrentToperationSame(),ex.getCode())
                    || Validations.isEquals(retCodeConfig.getCurrentToperationLate(),ex.getCode())) {
                operationNumber = processCompareCurrentOut.getCurrentOperationNumber();
                //----- Performs Rework Hold Request to Parent Lot -------//
                log.debug("Performs Rework Hold Request to Parent Lot");
                List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_REWORKHOLD);
                lotHoldReq.setHoldReasonCodeID(reasonCodeID);
                lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq.setRouteID(subRouteID);
                lotHoldReq.setOperationNumber(returnOperationNumber);
                lotHoldReq.setRelatedLotID(childLotID);
                holdReqList.add(lotHoldReq);
                log.debug("step - sxHoldLotReq");
                sxHoldLotReq(objCommon, parentLotID, holdReqList);
            } else if (Validations.isEquals(retCodeConfig.getCurrentToperationEarly(),ex.getCode())) {
                log.debug("Parent Lot is on more backward Operation of Return Point.");
            } else {
                throw ex;
            }
        }

        ObjectIdentifier currentRouteID = processCompareCurrentOut.getCurrentRouteID();
        log.debug("currentRouteID ; {}",currentRouteID.getValue());
        log.debug("returnOperationNumber ; {}",returnOperationNumber);
        //-------------------------
        // Check Bonding Group for childLot
        //-------------------------
        log.debug("Check Bonding Group for childLot");
        Inputs.ObjProcessOperationProcessRefListForLotIn refListForLotIn = new Inputs.ObjProcessOperationProcessRefListForLotIn();
        refListForLotIn.setSearchDirection(true);
        refListForLotIn.setPosSearchFlag(true);
        refListForLotIn.setSearchCount(Integer.parseInt(BizConstant.SEARCH_COUNT_MAX));
        refListForLotIn.setCurrentFlag(true);
        refListForLotIn.setSearchRouteID(currentRouteID);
        refListForLotIn.setLotID(parentLotID);
        refListForLotIn.setSearchOperationNumber(returnOperationNumber);
        log.debug("step - processOperationProcessRefListForLot");
        List<Infos.OperationProcessRefListAttributes> refListForLotRepeatRetCode = processMethod.processOperationProcessRefListForLot(objCommon, refListForLotIn);
        //checkLocateLotBondingFlowSection
        log.debug("step - checkLocateLotBondingFlowSection");
        int operationCountForLot = CimArrayUtils.getSize(refListForLotRepeatRetCode);
        log.trace("operationCountForLot >= 1 : {}",operationCountForLot >= 1);
        if (operationCountForLot >= 1) {
            lotMethod.lotBondingFlowSectionCheckLocate(objCommon,
                    childLotID, refListForLotRepeatRetCode.get(operationCountForLot - 1).getProcessRef());
        }
        //----- Creates Rework Hold Request of Parent lot -------//
        log.debug("Creates Rework Hold Request of Parent lot");
        Params.FutureHoldReqParams futureHoldReqParams = new Params.FutureHoldReqParams();
        futureHoldReqParams.setHoldType(BizConstant.SP_HOLDTYPE_REWORKHOLD);
        futureHoldReqParams.setLotID(parentLotID);
        futureHoldReqParams.setRouteID(currentRouteID);
        futureHoldReqParams.setOperationNumber(operationNumber);
        futureHoldReqParams.setReasonCodeID(reasonCodeID);
        futureHoldReqParams.setRelatedLotID(childLotID);
        futureHoldReqParams.setPostFlag(false);
        futureHoldReqParams.setSingleTriggerFlag(false);
        try {
            log.debug("step - sxFutureHoldReq");
            processControlService.sxFutureHoldReq(objCommon, futureHoldReqParams);
        } catch (ServiceException e) {
            log.trace("!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()));
            if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode())){
                throw e;
            }
        }

        //----- Creates Rework Hold Request of Child lot -------//
        log.debug("Creates Rework Hold Request of Child lot");
        log.debug("step - sxFutureHoldReq");
        futureHoldReqParams.setLotID(childLotID);
        futureHoldReqParams.setRelatedLotID(parentLotID);
        try {
            processControlService.sxFutureHoldReq(objCommon, futureHoldReqParams);
        } catch (ServiceException e) {
            log.trace("!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()));
            if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode())){
                throw e;
            }
        }

        //-----------------------------------------------------------
        // Performs Process Hold to Child lot
        //-----------------------------------------------------------
        log.debug("Performs Process Hold to Child lot");
        log.debug("step - sxProcessHoldDoActionReq");
        processControlService.sxProcessHoldDoActionReq(objCommon, childLotID, claimMemo);

        //-----------------------------------------------------------
        // Inherits Hold Records to Child lot
        //-----------------------------------------------------------
        log.debug("Inherits Hold Records to Child lot");
        log.trace("!ObjectUtils.isEmpty(holdReqWithoutProcHold) : {}",!CimObjectUtils.isEmpty(holdReqWithoutProcHold));
        if (!CimObjectUtils.isEmpty(holdReqWithoutProcHold)) {
            log.debug("step - sxHoldLotReq");
            sxHoldLotReq(objCommon, childLotID, holdReqWithoutProcHold);
        }
        log.trace("BooleanUtils.isTrue(updateControlJobFlag) : {}", CimBooleanUtils.isTrue(updateControlJobFlag));
        if (CimBooleanUtils.isTrue(updateControlJobFlag)) {
            //----------------------
            // Update control Job Info and
            // Machine cassette info if information exist
            //----------------------
            log.debug("Update control Job Info and Machine cassette info if information exist");
            log.debug("step - controlJobRelatedInfoUpdate");
            controlJobMethod.controlJobRelatedInfoUpdate(objCommon, Arrays.asList(cassetteID));
        }

        //-----------------------------------------------------------
        // Updates cassette's Multi lot Type
        //-----------------------------------------------------------
        log.debug("Updates cassette's Multi lot Type");
        log.debug("step - cassetteMultiLotTypeUpdate");
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);

        //-----------------------------------------------------------
        // Updates Child lot's Required cassette Category
        //-----------------------------------------------------------
        log.debug("Updates Child lot's Required cassette Category");
        log.debug("step - lotCassetteCategoryUpdateForContaminationControl");
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, childLotID);

        //-----------------------------------------------------------
        // Replace Target Operation for rework route
        //-----------------------------------------------------------
        log.debug("Replace Target Operation for rework route");
        Infos.QTimeTargetOpeReplaceIn inputParams = new Infos.QTimeTargetOpeReplaceIn();
        inputParams.setLotID(childLotID);
        inputParams.setSpecificControlFlag(false);
        log.debug("step - qTimeTargetOperationReplace");
        qTimeMethod.qTimeTargetOpeReplace(objCommon, inputParams);

        minQTimeMethod.checkTargetOpeReplace(objCommon, childLotID);

        // check contamination level ，trigger contamination hold
        log.debug("check contamination level ，trigger contamination hold");
        contaminationMethod.lotCheckContaminationLevelStepOut(objCommon, childLotID);
        retVal.setCreatedLotID(childLotID);
        return retVal;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params    -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     * @author Sun
     * @date 10/30/2018 11:14 AM
     */
    @SuppressWarnings("deprecation")
    public void sxPartialReworkCancelReq(Infos.ObjCommon objCommon, Params.PartialReworkCancelReqParams params) {
        log.info("【Method Entry】sxPartialReworkCancelReq()");

        //【Step1】Get cassette ID of parent lot;
        log.debug("[step-1] Get cassette ID of parent lot;");
        ObjectIdentifier lotCassetteGetRetCode = lotMethod.lotCassetteListGetDR(objCommon, params.getParentLotID());

        //【Step2】Lock objects to be updated;
        log.debug("[Step-2] Lock objects to be updated;");
        String configOperationEICheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        int isCheckLotOperationEI = CimStringUtils.isEmpty(configOperationEICheck) ? 0 : Integer.parseInt(configOperationEICheck);
        Long lockMode = 0L;
        Boolean updateControlJobFlag = false;
        String cassetteTransferStateGetRetCode = null;

        log.trace("isCheckLotOperationEI == 0 : {}",isCheckLotOperationEI == 0);
        if (isCheckLotOperationEI == 0) {
            //【Step3】 Get carrier transfer status;
            log.debug("[step-3] Get carrier transfer status;");
            cassetteTransferStateGetRetCode = cassetteMethod.cassetteTransferStateGet(objCommon, lotCassetteGetRetCode);

            //【Step4】Get eqp ID in cassette;
            log.debug("[step-4] Get eqp ID in cassette;");
            Outputs.ObjCassetteEquipmentIDGetOut objCassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, lotCassetteGetRetCode);
            // 【Step5】Get required eqp lock mode;
            log.debug("[step-5] Get required eqp lock mode");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(objCassetteEquipmentIDGetOut.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.REWORK_PARTIAL_WAFER_LOT_CANCEL_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            log.debug("lockMode = {}", lockMode);
            log.trace("StringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGetRetCode) : {}", CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGetRetCode));
            if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGetRetCode)) {
                updateControlJobFlag = true;
                log.debug("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                    // 【Step5-1-1】Lock eqp Main Object;
                    log.debug("[step-5-1] Lock eqp Main Object;");
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                    // 【Step5-1-2】Lock eqp LoadCassette Element (Write);
                    log.debug("[step-5-1-2] Lock eqp LoadCassette Element (Write)");
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                            (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, Collections.singletonList(lotCassetteGetRetCode.getValue())));
                } else {
                    //【Step5-2】Lock Machine object  ;
                    log.debug("[step-5-2] Lock Machine object");
                    objectLockMethod.objectLock(objCommon, CimMachine.class, objCassetteEquipmentIDGetOut.getEquipmentID());
                }
            }
        }

        //【Step6】
        log.debug("[step-6] lock cassette");
        objectLockMethod.objectLock(objCommon, CimCassette.class, lotCassetteGetRetCode);
        log.trace("0 == isCheckLotOperationEI : {}",0 == isCheckLotOperationEI);
        if (0 == isCheckLotOperationEI) {
            log.trace("!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
            if (!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                //【Step7】Get cassette's ControlJobID;
                //【Step7-1】
                log.debug("[step-7] Get cassette's ControlJobID");
                ObjectIdentifier controlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, lotCassetteGetRetCode);
                log.trace("!ObjectUtils.isEmptyWithValue(controlJobID) : {}",!CimObjectUtils.isEmptyWithValue(controlJobID));
                if (!CimObjectUtils.isEmptyWithValue(controlJobID)) {
                    log.debug("【Step7-1】: cassette's controlJobID isn't blank.");
                    updateControlJobFlag = true;
                    log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                    if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                        //【Step7-2】Lock controljob Object;
                        log.debug("[step-7-2] Lock controljob Object");
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
                    }
                }
            }
        }
        //【Step8】 lock parent lot;
        log.debug("[step-8] lock parent lot");
        objectLockMethod.objectLock(objCommon, CimLot.class, params.getParentLotID());

        //【Step9】 lock child lot;
        log.debug("[step-9] lock child lot");
        objectLockMethod.objectLock(objCommon, CimLot.class, params.getChildLotID());

        //【Step10】Check Lock Hold;
        log.debug("[step-10] Check LOCK Hold.");
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(params.getParentLotID());
        lotIDs.add(params.getChildLotID());
        lotMethod.lotCheckLockHoldConditionForOperation(objCommon, lotIDs);

        //【Step11】Check InPostProcessFlag;
        log.debug("[step-11] Check InPostProcessFlag.");
        final List<ObjectIdentifier> userGroupIDs = new ArrayList<>();
        int userGroupIDsLen = 0;
        for (int loopCnt = 0; loopCnt < CimArrayUtils.getSize(lotIDs); loopCnt++) {
            //  Get InPostProcessFlag of lot;
            log.debug("Get InPostProcessFlag of lot");
            Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagGetResult = lotMethod.lotInPostProcessFlagGet(objCommon, lotIDs.get(loopCnt));

            //  If lot is in post process, returns error
            log.trace("BooleanUtils.isTrue(lotInPostProcessFlagGetResult.getInPostProcessFlagOfLot()) : {}", CimBooleanUtils.isTrue(lotInPostProcessFlagGetResult.getInPostProcessFlagOfLot()));
            if (CimBooleanUtils.isTrue(lotInPostProcessFlagGetResult.getInPostProcessFlagOfLot())) {
                log.debug("lot is in post process.");
                log.trace("ArrayUtils.getSize(userGroupIDs) == 0 ; {}", CimArrayUtils.getSize(userGroupIDs) == 0);
                if (CimArrayUtils.getSize(userGroupIDs) == 0) {
                    // Get UserGroupID By UserID ;

                    log.debug("Get UserGroupID By UserID");
                    List<ObjectIdentifier> personUserGroupListRetCode = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());

                    userGroupIDs.addAll(personUserGroupListRetCode);
                    userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);
                    log.debug("userGroupIDsLen = {}", userGroupIDsLen);
                }


                int nCnt = 0;
                for (nCnt = 0; nCnt < userGroupIDsLen; nCnt++) {
                    log.debug("# Loop[nCnt = {}]/userID = {}", nCnt, userGroupIDs.get(nCnt).getValue());

                }
                Validations.check (nCnt == userGroupIDsLen, retCodeConfig.getLotInPostProcess());
            }
        }

        //【Step12】Check interFab TransferPlan existence;
        log.debug("[step-12] Check interFab TransferPlan existence.");

        //【Step12-1】lot_currentRouteID_Get;
        log.debug("[step-12-1] ot_currentRouteID_Get;");
        ObjectIdentifier lotCurrentRouteIDRetCode = lotMethod.lotCurrentRouteIDGet(objCommon, params.getChildLotID());


        //【Step12-2】Todo-NOTIMPL:interFab_xferPlanList_GetDR;

        //【Step13】Check SorterJob existence;
        log.debug("[step-13] Check SorterJob existence");
        List<ObjectIdentifier> dummyCastIDs = new ArrayList<>();
        Inputs.ObjWaferSorterJobCheckForOperation operation = new Inputs.ObjWaferSorterJobCheckForOperation();
        operation.setLotIDList(lotIDs);
        operation.setCassetteIDList(dummyCastIDs);
        operation.setEquipmentLoadPortAttribute(new Infos.EquipmentLoadPortAttribute());
        operation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, operation);

        String lotProductionStateRetCode = lotMethod.lotProductionStateGet(objCommon, params.getChildLotID());
        Validations.check (!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_PRODUCTION_STATE_INREWORK, lotProductionStateRetCode),
                retCodeConfig.getInvalidLotProductionState(),params.getChildLotID(),lotCassetteGetRetCode);

        //【Step14】 Check parent lot transfer status;
        log.debug("[step-14] Check parent lot transfer status");
        String lotTransferStateRetCode = null;
        log.trace("(1 == isCheckLotOperationEI) ||\n" +
                "                (0 == isCheckLotOperationEI)\n" +
                "                        && (!ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGetRetCode)) : {}",
                (1 == isCheckLotOperationEI) ||
                        (0 == isCheckLotOperationEI)
                                && (!CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGetRetCode)));
        if ((1 == isCheckLotOperationEI) ||
                (0 == isCheckLotOperationEI)
                        && (!CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGetRetCode))) {
            lotTransferStateRetCode = lotMethod.lotTransferStateGet(objCommon, params.getParentLotID());

            log.trace("0 == isCheckLotOperationEI : {}",0 == isCheckLotOperationEI);
            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT, lotTransferStateRetCode) ||\n" +
                    "                    ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, lotTransferStateRetCode) : {}",
                    CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT, lotTransferStateRetCode) ||
                            CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, lotTransferStateRetCode));

            if (0 == isCheckLotOperationEI) {
                log.debug("isCheckLotOperationEI = 0");
                Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, lotTransferStateRetCode),
                        retCodeConfig.getChangedToEiByOtherOperation(),lotCassetteGetRetCode);
            } else if (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT, lotTransferStateRetCode) ||
                    CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, lotTransferStateRetCode)) {
                log.debug("TRANSFER STATE is INVALID...");
                Validations.check(true,retCodeConfig.getInvalidLotXferstat(),params.getParentLotID(),lotTransferStateRetCode);
            }
        }
        log.trace("0 == isCheckLotOperationEI : {}",0 == isCheckLotOperationEI);
        if (0 == isCheckLotOperationEI) {
            lotTransferStateRetCode = cassetteTransferStateGetRetCode;
        }

        log.trace("1 == isCheckLotOperationEI : {}",1 == isCheckLotOperationEI);
        if (1 == isCheckLotOperationEI) {
            //【Step15】 Check child lot transfer status;
            lotTransferStateRetCode = lotMethod.lotTransferStateGet(objCommon, params.getChildLotID());
            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT, lotTransferStateRetCode) ||\n" +
                    "                    ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, lotTransferStateRetCode) : {}",
                    CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT, lotTransferStateRetCode) ||
                            CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, lotTransferStateRetCode));

            if (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT, lotTransferStateRetCode) ||
                    CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, lotTransferStateRetCode)) {
                log.debug("TRANSFER STATE is INVALID...");
                Validations.check(true,retCodeConfig.getInvalidLotXferstat(),params.getChildLotID(),lotTransferStateRetCode);
            }
        }

        //【Step16】Check lot Contents for Parent lot;
        log.debug("[step-16] Check lot Contents for Parent lot");
        String lotContentsRetCode = lotMethod.lotContentsGet(params.getParentLotID());

        log.trace("!ObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER, lotContentsRetCode) &&\n" +
                "                !ObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE, lotContentsRetCode) : {}",
                !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER, lotContentsRetCode) &&
                        !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE, lotContentsRetCode));

        if (!CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER, lotContentsRetCode) &&
                !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE, lotContentsRetCode)) {
            log.debug("The parent lot {} contents != 'wafer' and 'Die'", params.getParentLotID().getValue());
            Validations.check(true,retCodeConfig.getInvalidLotContents(),params.getParentLotID());
        }

        //【Step17】Check lot Contents for Child lot;
        log.debug("[step-17] Check lot Contents for Child lot");
        lotContentsRetCode = lotMethod.lotContentsGet(params.getChildLotID());
        log.trace("!ObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER, lotContentsRetCode) &&\n" +
                "                !ObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE, lotContentsRetCode) : {}",
                !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER, lotContentsRetCode) &&
                        !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE, lotContentsRetCode));

        if (!CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER, lotContentsRetCode) &&
                !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE, lotContentsRetCode)) {
            log.debug("The child lot {} contents != 'wafer' and 'Die'", params.getChildLotID().getValue());
            Validations.check(true,retCodeConfig.getInvalidLotContents(),params.getChildLotID());
        }

        //【Step18】Check Finished State of parent lot;
        log.debug("[step -18] Check Finished State of parent lot");
        String lotFinishedStateRetCode = lotMethod.lotFinishedStateGet(objCommon, params.getParentLotID());
        log.debug("ObjectUtils.equalsWithValue(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedStateRetCode) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedStateRetCode));
        if (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedStateRetCode)) {
            log.debug("【Parent lot】:LotFinishedState is 'STACKED'.");
            Validations.check(true,retCodeConfig.getInvalidLotFinishStat(),lotFinishedStateRetCode);
        }

        //【Step19】Check Finished State of child lot ;
        log.debug("[step-19] Check Finished State of child lot");
        lotFinishedStateRetCode = lotMethod.lotFinishedStateGet(objCommon, params.getChildLotID());
        log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedStateRetCode) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedStateRetCode));
        if (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedStateRetCode)) {
            log.debug("【Child lot】: LotFinishedState is 'STACKED'.");
            Validations.check(true,retCodeConfig.getInvalidLotFinishStat(),params.getChildLotID());
        }
        //【Step20】Check Bonding Group for parent lot ;
        log.debug("[step-20] Check Bonding Group for parent lot");
        String lotBondingGroupIDRetCode = lotMethod.lotBondingGroupIDGetDR(objCommon, params.getParentLotID());
        log.trace("!ObjectUtils.isEmpty(lotBondingGroupIDRetCode) : {}",!CimObjectUtils.isEmpty(lotBondingGroupIDRetCode));
        if (!CimObjectUtils.isEmpty(lotBondingGroupIDRetCode)) {
            log.debug("【Parent lot】: BondingGroupID is not empty. ");
            Validations.check(true,retCodeConfig.getLotHasBondingGroup(),params.getParentLotID().getValue(),lotBondingGroupIDRetCode);
        }

        //【Step21】Check Bonding Group for child lot ;
        log.debug("[step-21] Check Bonding Group for child lot");
        lotBondingGroupIDRetCode = lotMethod.lotBondingGroupIDGetDR(objCommon, params.getChildLotID());
        log.trace("!ObjectUtils.isEmpty(lotBondingGroupIDRetCode) : {}",!CimObjectUtils.isEmpty(lotBondingGroupIDRetCode));
        if (!CimObjectUtils.isEmpty(lotBondingGroupIDRetCode)) {
            log.debug("【Child lot】: BondingGroupID is not empty. ");
            Validations.check(true,retCodeConfig.getLotHasBondingGroup(),params.getChildLotID().getValue(),lotBondingGroupIDRetCode);
        }
        //【Step22】Check lot Process State for Parent lot;
        log.debug("[step-22] Check lot Process State for Parent lot");
        String lotProcessStateRetCode = lotMethod.lotProcessStateGet(objCommon, params.getParentLotID());
        Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotProcessStateRetCode),
                retCodeConfig.getInvalidLotProcessState(),params.getParentLotID(),lotProcessStateRetCode);

        //【Step23】Collect all lot state for Child lot;
        log.debug("[step-23] Collect all lot state for Child lot");
        Outputs.ObjLotAllStateGetOut lotAllStateGetRetCode = lotMethod.lotAllStateGet(objCommon, params.getChildLotID());

        //【Step24】Check lot Process State for Child lot;
        log.debug("[step-24] Check lot Process State for Child lot");
        Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotAllStateGetRetCode.getProcessState()),
                retCodeConfig.getInvalidLotProcessState(),params.getChildLotID(),lotAllStateGetRetCode.getProcessState());

        //【Step25】Check lot Inventory State for Child lot;
        log.debug("[step-25] Check lot Inventory State for Child lot");
        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, lotAllStateGetRetCode.getInventoryState()),
                retCodeConfig.getInvalidLotInventoryStat(),params.getChildLotID(),lotAllStateGetRetCode.getInventoryState());

        //【Step26】Check Rework Cancel can be done or not with lot Family ;
        log.debug("[step-26] Check Rework Cancel can be done or not with lot Family");
        lotFamilyMethod.lotFamilyCheckReworkCancel(objCommon, params.getParentLotID(), params.getChildLotID());


        //【Step27】Check lot Merge can be done or not;
        log.debug("[step-27] Check lot Merge can be done or not");
        try{
            processMethod.processCheckMerge(objCommon, params.getParentLotID(),params.getChildLotID());
        }catch (ServiceException ex){
            log.trace("!Validations.isEquals(retCodeConfig.getSamePreOperation(), ex.getCode()) : {}",!Validations.isEquals(retCodeConfig.getSamePreOperation(), ex.getCode()));
            if (!Validations.isEquals(retCodeConfig.getSamePreOperation(), ex.getCode())){
                throw ex;
            }
        }


        //【Step28】 Check lot Hold List for Rework Cancel;
        log.debug("[step-28] Check lot Hold List for Rework Cancel");
        lotMethod.lotHoldListCheckReworkCancel(objCommon, params.getParentLotID(), params.getChildLotID());


        //【Step29】Check lot Future Hold for lot Merge & 【Step1】;
        log.debug("[step-29] Check lot Future Hold for lot Merge & [Step-29-1]");
        List<Infos.LotHoldReq> lotFutureHoldCheckMergeRetCode = lotMethod.lotFutureHoldRequestsCheckMerge(objCommon, params.getParentLotID(),params.getChildLotID());

        ObjectIdentifier aReasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_MERGE);
        int holdReqListSize = CimArrayUtils.getSize(lotFutureHoldCheckMergeRetCode);
        log.trace("holdReqListSize > 0 : {}",holdReqListSize > 0);
        if (holdReqListSize > 0) {
            log.debug("lotFutureHoldRequestsCheckMerge() return valid the list of Infos.LotHoldReq.");
            //【Step2】Cancel Future Hold Registration of Child lot;
            log.debug("[step-29-2] Cancel Future Hold Registration of Child lot");
            Params.FutureHoldCancelReqParams futureHoldCancelReqParams = new Params.FutureHoldCancelReqParams();
            futureHoldCancelReqParams.setUser(objCommon.getUser());
            futureHoldCancelReqParams.setLotID(params.getChildLotID());
            futureHoldCancelReqParams.setReleaseReasonCodeID(aReasonCodeID);
            futureHoldCancelReqParams.setEntryType(BizConstant.SP_ENTRYTYPE_REMOVE);
            futureHoldCancelReqParams.setLotHoldList(lotFutureHoldCheckMergeRetCode);

            processControlService.sxFutureHoldCancelReq(objCommon, futureHoldCancelReqParams);

            //【Step3】Prepare data for Parent lot(set child lot id as relatedLotID;);
            log.debug("[step-29-3] Prepare data for Parent lot(set child lot id as relatedLotID");
            lotFutureHoldCheckMergeRetCode.get(0).setRelatedLotID(params.getChildLotID());
            //【Step4】Get Parent lot's Hold Records;
            log.debug("[step-29-4] Get Parent lot's Hold Records");
            List<Infos.LotHoldReq> strReleaseList = new ArrayList<>();
            List<Infos.LotHoldReq> strCancelList = new ArrayList<>();

            List<Infos.LotHoldListAttributes> lotFillInTxTRQ005DRRetCode = null;
            try {
                lotFillInTxTRQ005DRRetCode = lotMethod.lotFillInTxTRQ005DR(objCommon, params.getParentLotID());

                //【Step4-1】Sort out Parent lot's Hold Record
                log.debug("[step-29-4-1] Sort out Parent lot's Hold Record");
                log.debug("Parent lot has some Hold Records. The one by Merge/Rework Hold Request is sorted out.");

                int requestLen = CimArrayUtils.getSize(lotFutureHoldCheckMergeRetCode);
                int recordLen = CimArrayUtils.getSize(lotFillInTxTRQ005DRRetCode);

                for (int requestCnt = 0; requestCnt < requestLen; requestCnt++) {
                    Infos.LotHoldReq lotHoldReq = lotFutureHoldCheckMergeRetCode.get(requestCnt);
                    Boolean foundRecord = false;

                    log.debug("holdType = {} ,holdReasonCodeID = {} ,holdUserID = {}, relatedLotID = {}",lotHoldReq.getHoldType(),lotHoldReq.getHoldReasonCodeID(),lotHoldReq.getHoldUserID().getValue(),lotHoldReq.getRelatedLotID());

                    for (int recordCnt = 0; recordCnt < recordLen; recordCnt++) {
                        Infos.LotHoldListAttributes lotHoldListAttributes = lotFillInTxTRQ005DRRetCode.get(recordCnt);

                        log.debug("StringUtils.equals(lotHoldReq.getHoldType(), lotHoldListAttributes.getHoldType()) &&\n" +
                                "                                ObjectUtils.equalsWithValue(lotHoldReq.getHoldReasonCodeID(), lotHoldListAttributes.getReasonCodeID()) &&\n" +
                                "                                ObjectUtils.equalsWithValue(lotHoldReq.getHoldUserID(), lotHoldListAttributes.getUserID()) &&\n" +
                                "                                ObjectUtils.equalsWithValue(lotHoldReq.getRelatedLotID(), lotHoldListAttributes.getRelatedLotID()) : {}",
                                CimStringUtils.equals(lotHoldReq.getHoldType(), lotHoldListAttributes.getHoldType()) &&
                                        CimObjectUtils.equalsWithValue(lotHoldReq.getHoldReasonCodeID(), lotHoldListAttributes.getReasonCodeID()) &&
                                        CimObjectUtils.equalsWithValue(lotHoldReq.getHoldUserID(), lotHoldListAttributes.getUserID()) &&
                                        CimObjectUtils.equalsWithValue(lotHoldReq.getRelatedLotID(), lotHoldListAttributes.getRelatedLotID()));

                        if (CimStringUtils.equals(lotHoldReq.getHoldType(), lotHoldListAttributes.getHoldType()) &&
                                CimObjectUtils.equalsWithValue(lotHoldReq.getHoldReasonCodeID(), lotHoldListAttributes.getReasonCodeID()) &&
                                CimObjectUtils.equalsWithValue(lotHoldReq.getHoldUserID(), lotHoldListAttributes.getUserID()) &&
                                CimObjectUtils.equalsWithValue(lotHoldReq.getRelatedLotID(), lotHoldListAttributes.getRelatedLotID())) {
                            foundRecord = true;
                            break;
                        }
                    }

                    log.debug("foundRecord : {}",foundRecord);
                    if (foundRecord) {
                        log.debug("Hold Record by Merge/Rework Hold Request is found.");
                        strReleaseList.add(lotHoldReq);
                    } else {
                        log.debug("Hold Record by Merge/Rework Hold Request is not found.");
                        strCancelList.add(lotHoldReq);
                    }
                }
            } catch (ServiceException e) {
                log.trace("!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode()));
                if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                    log.debug("lotFillInTxTRQ005DR() return {}", e.getCode());
                    throw e;
                }
                log.debug("Parent lot has no Hold Record.");
                int requestLen = CimArrayUtils.getSize(lotFutureHoldCheckMergeRetCode);
                for (int requestCnt = 0; requestCnt < requestLen; requestCnt++) {
                    strCancelList.add(lotFutureHoldCheckMergeRetCode.get(requestCnt));
                }
            }

            //【Step5】If Parent lot is held by ReworkMerge reason,Cancel lot Hold of Parent lot;
            log.debug("[step-29-5] If Parent lot is held by ReworkMerge reason,Cancel lot Hold of Parent lot");
            log.trace("ArrayUtils.getSize(strReleaseList) > 0 : {}", CimArrayUtils.getSize(strReleaseList) > 0);
            if (CimArrayUtils.getSize(strReleaseList) > 0) {
                log.debug("Hold Records of Merge/Rework Hold Requests are released.");
                Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                holdLotReleaseReqParams.setUser(objCommon.getUser());
                holdLotReleaseReqParams.setLotID(params.getParentLotID());
                holdLotReleaseReqParams.setReleaseReasonCodeID(aReasonCodeID);
                holdLotReleaseReqParams.setHoldReqList(strReleaseList);
                log.debug("sent lot hold release request :: sxHoldLotReleaseReq");
                sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
            }

            //【Step6】If lot has future hold registration for rework Merge,Cancel Future Hold registration of Parent lot;
            log.debug("[step-29-6] If lot has future hold registration for rework Merge,Cancel Future Hold registration of Parent lot");
            log.trace("ArrayUtils.getSize(strCancelList) > 0 : {}", CimArrayUtils.getSize(strCancelList) > 0);
            if (CimArrayUtils.getSize(strCancelList) > 0) {
                log.debug("Merge/Rework Hold Requests are cancelled.");

                Params.FutureHoldCancelReqParams cancelListCancelReqParams = new Params.FutureHoldCancelReqParams();
                cancelListCancelReqParams.setUser(objCommon.getUser());
                cancelListCancelReqParams.setLotID(params.getParentLotID());
                cancelListCancelReqParams.setReleaseReasonCodeID(aReasonCodeID);
                cancelListCancelReqParams.setEntryType(BizConstant.SP_ENTRYTYPE_REMOVE);
                cancelListCancelReqParams.setLotHoldList(strCancelList);

                processControlService.sxFutureHoldCancelReq(objCommon, cancelListCancelReqParams);
            }
        }
        //【Step30】Check flowbatch Condition;
        log.debug("【Parent lot】Check flowbatch Condition");
        ObjectIdentifier parentLotFlowBatchIdRetCode = null;
        try {
            parentLotFlowBatchIdRetCode = lotMethod.lotFlowBatchIDGet(objCommon, params.getParentLotID());
        } catch (ServiceException e) {
            log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(),e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(),e.getCode()));
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(),e.getCode())){
                log.debug("【Parent lot】:lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
                throw new ServiceException(retCodeConfig.getFlowBatchLimitation());
            } else if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())) {
                log.debug("【Parent lot】:lotFlowBatchIDGet() return {}", e.getCode());
            } else {
                log.debug("【Parent lot】:lotFlowBatchIDGet() return {}", e.getCode());
                throw e;
            }

        }


        //【Step31】Check flowbatch Condition;
        log.debug("[step-31]【Child lot】Check flowbatch Condition");
        ObjectIdentifier childLotFlowBatchIdRetCode = null;
        try {
            childLotFlowBatchIdRetCode = lotMethod.lotFlowBatchIDGet(objCommon, params.getChildLotID());
        } catch (ServiceException e) {
            log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()));
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())) {
                log.debug("【Child lot】: lotFlowBatchIDGet() return {}", e.getCode());
                throw new ServiceException(retCodeConfig.getFlowBatchLimitation());
            } else if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())) {
                log.debug("【Child lot】:lotFlowBatchIDGet() return {}", e.getCode());
            } else {
                log.debug("【Child lot】:lotFlowBatchIDGet() return {}", e.getCode());
                throw e;
            }

        }

        //【Step32】Check lot's Control Job ID;
        log.debug("[step-32] Check lot's Control Job ID");
        ObjectIdentifier lotControlJobIDGetRetCode = lotMethod.lotControlJobIDGet(objCommon, params.getParentLotID());
        log.trace("ObjectUtils.isEmpty(lotControlJobIDGetRetCode) : {}", CimObjectUtils.isEmpty(lotControlJobIDGetRetCode));
        if (CimObjectUtils.isEmpty(lotControlJobIDGetRetCode)) {
            log.debug("【Parent lot】: lotControlJobIDGet() return empty control job id.");
        } else {
            log.debug("【Parent lot】: lotControlJobIDGet() return valid control job id.");
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),params.getParentLotID().getValue(),lotControlJobIDGetRetCode.getValue()));
        }

        //【Step33】Check lot's Control Job ID for childLot;
        log.debug("[step-33] Check lot's Control Job ID for childLot");
        lotControlJobIDGetRetCode = lotMethod.lotControlJobIDGet(objCommon, params.getChildLotID());
        if (CimObjectUtils.isEmpty(lotControlJobIDGetRetCode)) {
            log.debug("【Child lot】: lotControlJobIDGet() return empty control job id.");
        } else {
            log.debug("【Child lot】: lotControlJobIDGet() return valid control job id.");
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),params.getChildLotID().getValue(),lotControlJobIDGetRetCode.getValue()));
        }

        log.trace("1 == isCheckLotOperationEI : {}",1 == isCheckLotOperationEI);
        if (1 == isCheckLotOperationEI) {
            log.debug("isCheckLotOperationEI = 1");
            //【Step34】Check carrier dispatch status;
            log.debug("[step-34] Check carrier dispatch status");
            Boolean cassetteDispathStateRetCode = cassetteMethod.cassetteDispatchStateGet(objCommon, lotCassetteGetRetCode);
            Validations.check (CimBooleanUtils.isTrue(cassetteDispathStateRetCode), retCodeConfig.getAlreadyDispatchReservedCassette());
        }

        //【Step35】Collect Child lot Wafers;
        log.debug("[step-35] Collect Child lot Wafers");
        List<Infos.LotWaferMap> lotWaferMapListRetCode = lotMethod.lotWaferMapGet(objCommon, params.getChildLotID());

        int nLen = CimArrayUtils.getSize(lotWaferMapListRetCode);
        log.debug("nLen = {}", nLen);

        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        for (int i = 0; i < nLen; i++) {
            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();

            newWaferAttributes.setNewLotID(params.getParentLotID());
            newWaferAttributes.setNewWaferID(lotWaferMapListRetCode.get(i).getWaferID());
            Integer position = Integer.parseInt(String.format("%d", lotWaferMapListRetCode.get(i).getSlotNumber()));
            newWaferAttributes.setNewSlotNumber(position);
            newWaferAttributes.setSourceLotID(params.getChildLotID());
            newWaferAttributes.setSourceWaferID(lotWaferMapListRetCode.get(i).getWaferID());

            newWaferAttributesList.add(newWaferAttributes);
        }
        Infos.NewLotAttributes strNewLotAttributes = new Infos.NewLotAttributes();
        strNewLotAttributes.setNewWaferAttributesList(newWaferAttributesList);

        //【Step36】Make History;
        log.debug("[step-37] Make History the first tme.");
        //【Step36-1】lot_waferLotHistoryPointer_Update;
        log.debug("[step-36-1] update lot wafer history pointer");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, params.getParentLotID());
        //【Step36-2】lotPartialReworkCancelEvent_Make;
        log.debug("[step-36-2] make lot partial rework cancel event");
        Inputs.LotPartialReworkCancelEventMakeParams lotPartialReworkCancelEventMakeParams = new Inputs.LotPartialReworkCancelEventMakeParams();
        lotPartialReworkCancelEventMakeParams.setTransactionID(TransactionIDEnum.REWORK_PARTIAL_WAFER_LOT_CANCEL_REQ.getValue());
        lotPartialReworkCancelEventMakeParams.setChildLotID(params.getChildLotID());
        lotPartialReworkCancelEventMakeParams.setParentLotID(params.getParentLotID());
        lotPartialReworkCancelEventMakeParams.setClaimMemo(params.getClaimMemo());
        lotPartialReworkCancelEventMakeParams.setReasonCodeID(params.getReasonCodeID());
        eventMethod.lotPartialReworkCancelEventMake(objCommon, lotPartialReworkCancelEventMakeParams);
        //【Step37】Check Future Action Procedure;
        log.debug("[step-37] Check Future Action Procedure");
        scheduleChangeReservationMethod.schdlChangeReservationCheckForMerge(objCommon,params.getParentLotID(),params.getChildLotID());

        //【Step38】Roll back Target Operation for rework route ;
        log.debug("[step-38] Roll back Target Operation for rework route");
        iqTimeMethod.qTimeTargetOpeCancelReplace(objCommon, params.getChildLotID());
        //【Step39】Check Q-Time information condition ;
        log.debug("[step-39] Check Q-Time information condition");
        iqTimeMethod.qTimeCheckForMerge(objCommon, params.getParentLotID(), params.getChildLotID());

        //【Step40】Decrement Rework Count of Child lot before they are merged;
        log.debug("[step-40] Decrement Rework Count of Child lot before they are merged");
        processMethod.processReworkCountDecrement(objCommon, params.getChildLotID());
        //【Step41】Remove the child lot from monitor groups ;
        log.debug("[step-41] Remove the child lot from monitor groups");
        lotMethod.lotRemoveFromMonitorGroup(objCommon, params.getChildLotID());

        //【Step42】Change ChildLot State ;
        log.debug("[step-42] Change ChildLot State");
        Inputs.OldCurrentPOData cancelBranchRouteRetCode = processMethod.processCancelBranchRoute(objCommon, params.getChildLotID());
        //【Step43】Merge Child lot to Parent lot ;
        log.debug("[step-43] Merge Child lot to Parent lot");
        String lotMergeWaferLotRetCode = lotMethod.lotMergeWaferLot(objCommon, params.getParentLotID(),params.getChildLotID());

        //【Step44】Merge Q-Time information ;
        log.debug("[step-44] Merge Q-Time information");
        iqTimeMethod.qTimeInfoMerge(objCommon, params.getParentLotID(), params.getChildLotID());

        //【Step45】TRUE == updateControlJobFlag ;
        log.trace("[step-45] BooleanUtils.isTrue(updateControlJobFlag) : {}", CimBooleanUtils.isTrue(updateControlJobFlag));
        if (CimBooleanUtils.isTrue(updateControlJobFlag)) {
            //【Step1】Update control Job Info and Machine cassette info if information exist;
            log.debug("[step-46-1] Update control Job Info and Machine cassette info if information exist");
            List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
            cassetteIDList.add(lotCassetteGetRetCode);
            controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDList);
        }

        //【Step2】Update cassette multi lot type
        log.debug("[step-46-2] Update cassette multi lot type");
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, lotCassetteGetRetCode);

        //【Step46】Make History 2;
        log.debug("[step-46] Make wafer move History");
        eventMethod.lotWaferMoveEventMake(objCommon, strNewLotAttributes, TransactionIDEnum.REWORK_PARTIAL_WAFER_LOT_CANCEL_REQ.getValue(), params.getClaimMemo());
        //-- Entity Inhibit Exception Lot Data --//
        // Get Entity Inhibition Info
        // TODO: 2019/10/18 core
        log.debug("Get Entity Inhibition Info");
        List<CimRestriction> aEntityInhibitList = entityInhibitManager.getEntityInhibitsWithExceptionLotByLot(params.getChildLotID());
        log.trace("!ObjectUtils.isEmpty(aEntityInhibitList) : {}",!CimObjectUtils.isEmpty(aEntityInhibitList));
        if (!CimObjectUtils.isEmpty(aEntityInhibitList)){
            Params.MfgRestrictExclusionLotReqParams cancelReqData = new Params.MfgRestrictExclusionLotReqParams();
            List<Infos.EntityInhibitExceptionLot> entityInhibitExceptionLots = new ArrayList<>();
            cancelReqData.setEntityInhibitExceptionLots(entityInhibitExceptionLots);
            cancelReqData.setClaimMemo(params.getClaimMemo());
            for (CimRestriction cimEntityInhibit : aEntityInhibitList){
                Infos.EntityInhibitExceptionLot entityInhibitExceptionLot = new Infos.EntityInhibitExceptionLot();
                Constrain.EntityInhibitRecord entityInhibitRecord = cimEntityInhibit.getInhibitRecord();
                entityInhibitExceptionLot.setEntityInhibitID(new ObjectIdentifier(entityInhibitRecord.getId(),entityInhibitRecord.getReferenceKey()));
                entityInhibitExceptionLot.setLotID(params.getChildLotID());
                entityInhibitExceptionLots.add(entityInhibitExceptionLot);
            }
            log.debug("sent mfg resrtrict ecxclusion lot cancel request");
            constraintService.sxMfgRestrictExclusionLotCancelReq(cancelReqData, objCommon);

        }

        // 【step 47】 check contamination level and pr flag
        log.debug("[step-47] check contamination level and pr flag");
        contaminationMethod.lotPartialReworkCancelContaminationCheck(objCommon, params.getParentLotID(), params.getChildLotID());
        log.debug("【Method Exit】sxPartialReworkCancelReq()");
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/22 17:24
     * @param objCommon -
     * @param reworkReq -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    public void sxReworkReq(Infos.ObjCommon objCommon, Infos.ReworkReq reworkReq, String claimMemo) {
        log.debug("inputParam lotID                 : {}",reworkReq.getLotID().getValue());
        log.debug("inputParam CurrentOperationNum   : {}",reworkReq.getCurrentOperationNumber());
        log.debug("inputParam CurrentRouteID        : {}",reworkReq.getCurrentRouteID().getValue());
        log.debug("inputParam subRouteID            : {}",reworkReq.getSubRouteID().getValue());
        log.debug("inputParam returnOperationNumber : {}",reworkReq.getReturnOperationNumber());
        log.debug("inputParam reasonCodeID          : {}",reworkReq.getReasonCodeID().getValue());
        log.debug("inputParam eventTxId             : {}",reworkReq.getEventTxId());
        log.debug("inputParam bForceRework          : {}",reworkReq.getForceReworkFlag().toString());
        log.debug("inputParam bDynamicRoute         : {}",reworkReq.getDynamicRouteFlag().toString());

        // decide TX_ID
        String txID = null;

        log.trace("!ObjectUtils.isEmpty(reworkReq.getEventTxId()) : {}",!CimObjectUtils.isEmpty(reworkReq.getEventTxId()));
        if (!CimObjectUtils.isEmpty(reworkReq.getEventTxId())) {
            txID = reworkReq.getEventTxId();
            log.debug("TX ID (Use Input TX ID )   : {}",txID);
        } else {
            //Special Rework case
            TransactionIDEnum REWORK_REQ = null;
            log.trace("BooleanUtils.isTrue(reworkReq.getForceReworkFlag()) : {}", CimBooleanUtils.isTrue(reworkReq.getForceReworkFlag()));
            if (CimBooleanUtils.isTrue(reworkReq.getForceReworkFlag())) {
                REWORK_REQ = CimBooleanUtils.isTrue(reworkReq.getDynamicRouteFlag()) ? TransactionIDEnum.FORCE_DYNAMIC_REWORK_REQ : TransactionIDEnum.FORCE_REWORK_REQ;
            } else {
                REWORK_REQ = CimBooleanUtils.isTrue(reworkReq.getDynamicRouteFlag()) ? TransactionIDEnum.DYNAMIC_REWORK_REQ : TransactionIDEnum.REWORK_REQ;
            }
            txID = REWORK_REQ.getValue();
        }

        //【step1】get cassette / lot connection
        log.debug("【step1】get cassette / lot connection");
        ObjectIdentifier cassetteID = lotMethod.lotCassetteGet(objCommon, reworkReq.getLotID());
        // 【step2】lock object
        log.debug("【step2】lock object");
        String strParallelPostProcFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG);
        log.trace("!StringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON) : {}",!CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON));
        if (!CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON)){
            objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
        }
        // get lotOperationEICheck from environment.
        log.debug("get lotOperationEICheck from environment");
        Long lotOperationEICheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getLongValue();
        log.trace("1 == lotOperationEICheck : {}",1 == lotOperationEICheck);
        if (1 == lotOperationEICheck) {
            // transferState should not be EI except when called from BRScript with OpeComp/OpeStartCancel.
            log.debug("transferState should not be EI except when called from BRScript with OpeComp/OpeStartCancel.");

            log.trace("!TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID() : {}",
                    !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID())
                            && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID())
                            && !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID())
                            && !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID())
                            && !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_REQ, objCommon.getTransactionID())
                            && !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID())
                            && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_REQ, objCommon.getTransactionID())
                            && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()));

            if (!TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID())
                    && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID())
                    && !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID())
                    && !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID())
                    && !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_REQ, objCommon.getTransactionID())
                    && !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID())
                    && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_REQ, objCommon.getTransactionID())
                    && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID())) {
                String transferStateGetOutRetCode = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
                Validations.check (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferStateGetOutRetCode), new OmCode(retCodeConfig.getInvalidCassetteTransferState(),
                        transferStateGetOutRetCode,cassetteID.getValue()));
            }
        }

        // step4】lock object to be updated.
        log.debug("【step4】 lock object to be updated.");
        objectLockMethod.objectLock(objCommon, CimLot.class, reworkReq.getLotID());

        //【step5】check whether lot is on the specified route/operation or not.
        log.debug("【step5】check whether lot is on the specified route/operation or not.");

        ObjectIdentifier tmpCurrentRouteID = reworkReq.getCurrentRouteID();
        String tmpCurrentOperationNumber = reworkReq.getCurrentOperationNumber();
        ObjectIdentifier lotID = reworkReq.getLotID();

        //【Step17】Check SorterJob existence;
        log.debug("【Step17】Check SorterJob existence;");
        Inputs.ObjWaferSorterJobCheckForOperation operation = new Inputs.ObjWaferSorterJobCheckForOperation();
        List<ObjectIdentifier> lotIDs= new ArrayList<>();
        lotIDs.add(lotID);
        operation.setLotIDList(lotIDs);
        operation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon,operation);

        log.trace("!ObjectUtils.isEmpty(tmpCurrentRouteID) && !ObjectUtils.isEmpty(tmpCurrentOperationNumber) : {}",!ObjectIdentifier.isEmpty(tmpCurrentRouteID) && !CimObjectUtils.isEmpty(tmpCurrentOperationNumber));
        if (!ObjectIdentifier.isEmpty(tmpCurrentRouteID) && !CimObjectUtils.isEmpty(tmpCurrentOperationNumber)) {
            log.debug("in-para current info is not null. begin to check!");
            Outputs.ObjLotCurrentOperationInfoGetOut currentOperationInfoGetOutRetCode = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);

            log.trace("ObjectUtils.equalsWithValue(tmpCurrentRouteID, currentOperationInfoGetOutRetCode.getRouteID())\n" +
                    "                    && StringUtils.equals(tmpCurrentOperationNumber, currentOperationInfoGetOutRetCode.getOperationNumber()) : {}",
                    ObjectIdentifier.equalsWithValue(tmpCurrentRouteID, currentOperationInfoGetOutRetCode.getRouteID())
                            && CimStringUtils.equals(tmpCurrentOperationNumber, currentOperationInfoGetOutRetCode.getOperationNumber()));

            if (ObjectIdentifier.equalsWithValue(tmpCurrentRouteID, currentOperationInfoGetOutRetCode.getRouteID())
                    && CimStringUtils.equals(tmpCurrentOperationNumber, currentOperationInfoGetOutRetCode.getOperationNumber())) {
                log.debug("route/operation check ok, go ahead...");
            } else {
                log.error("not same route.");
                Validations.check(true,new OmCode(retCodeConfig.getNotSameRoute(),"Input parameter's currentRouteID/currentOperationNumber","lot's current currentRouteID/currentOperationNumber"));
            }
        }else {
            log.debug("InPara Current Info is Null. No Check!!");
        }

        //【step6】get all lot state
        log.debug("【step6】get all lot state");

        Outputs.ObjLotAllStateGetOut lotAllStateGetOut = lotMethod.lotAllStateGet(objCommon, lotID);


        //check lot condition
        log.debug("check lot condition");
        Validations.check (CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, lotAllStateGetOut.getHoldState()), new OmCode(retCodeConfig.getInvalidLotHoldStat(),reworkReq.getLotID().getValue(),lotAllStateGetOut.getHoldState()));
        Validations.check (!CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotAllStateGetOut.getLotState()), new OmCode(retCodeConfig.getInvalidLotStat(),lotAllStateGetOut.getLotState()));
        Validations.check (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotAllStateGetOut.getProcessState()), new OmCode(retCodeConfig.getInvalidLotProcessState(),reworkReq.getLotID().getValue(),lotAllStateGetOut.getProcessState()));
        Validations.check (CimStringUtils.equals(CIMStateConst.CIM_LOT_PRODUCTION_STATE_INREWORK, lotAllStateGetOut.getProductionState()), new OmCode(retCodeConfig.getInvalidLotProductionState(),reworkReq.getLotID().getValue(),lotAllStateGetOut.getProductionState()));
        Validations.check (!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, lotAllStateGetOut.getInventoryState()),
                retCodeConfig.getInvalidLotInventoryStat(),ObjectIdentifier.fetchValue(reworkReq.getLotID()),lotAllStateGetOut.getInventoryState());

        log.debug("【step7】call lot_inPostProcessFlag_Get");
        Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);
        //----------------------------------------------
        //  If Lot is in post process, returns error
        //----------------------------------------------
        log.trace("BooleanUtils.isTrue(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot()) : {}",
                CimBooleanUtils.isTrue(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot()));

        if (CimBooleanUtils.isTrue(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot())) {
            /*---------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            log.debug("Lot is in post process.");
            log.debug("【step8】call person_userGroupList_GetDR");
            List<ObjectIdentifier> strPersonUserGroupListGetDROut = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());

            int userGroupIDsLen = CimArrayUtils.getSize(strPersonUserGroupListGetDROut);
            int i;
            for (i = 0; i < userGroupIDsLen; i++) {
            }
            Validations.check( i == userGroupIDsLen, new OmCode(retCodeConfig.getLotInPostProcess(),reworkReq.getLotID().getValue()));
        }

        log.debug("【step9】call process_CheckInterFabXferPlanSkip");
        Inputs.ObjProcessCheckInterFabXferPlanSkipIn in = new Inputs.ObjProcessCheckInterFabXferPlanSkipIn();
        in.setLotID(lotID);
        in.setCurrentRouteID(reworkReq.getCurrentRouteID());
        in.setCurrentOpeNo(reworkReq.getCurrentOperationNumber());
        in.setJumpingRouteID(reworkReq.getCurrentRouteID());
        in.setJumpingOpeNo(reworkReq.getReturnOperationNumber());
        // TODO: 2019/10/15 tobe confirm
        processMethod.processCheckInterFabXferPlanSkip(objCommon, in);

        //【step10】check lot's controlJobID
        log.debug("【step10】check lot's controlJobID");
        ObjectIdentifier controlJobID = lotMethod.lotControlJobIDGet(objCommon, lotID);
        String controlJobId = ObjectIdentifier.fetchValue(controlJobID);
        Validations.check(!CimObjectUtils.isEmpty(controlJobId), new OmCode(retCodeConfig.getLotControlJobidFilled(),reworkReq.getLotID().getValue(),controlJobId));


        //【step11】converts input reworkReq.subRouteID if it has version.
        log.debug("【step11】converts input reworkReq.subRouteID if it has version.");
        ObjectIdentifier subRouteID = processMethod.processActiveIDGet(objCommon, reworkReq.getSubRouteID());
        log.debug("active subRouteID = {}",subRouteID);
        //check first operation or not.
        log.debug("check first opeartion or not.");
        Boolean searchDirectionFlag = false;
        Boolean posSearchFlag = true;
        Integer searchCount = 2;
        String searchOperationNumber = null;
        Boolean currentFlag = true;

        //【bear】the processOperationListForLot named is nonconforming, it will be corrected later.
        Inputs.ObjProcessOperationListForLotIn objProcessOperationListForLotIn = new Inputs.ObjProcessOperationListForLotIn();
        objProcessOperationListForLotIn.setSearchDirectionFlag(false);
        objProcessOperationListForLotIn.setPosSearchFlag(true);
        objProcessOperationListForLotIn.setSearchCount(2);
        objProcessOperationListForLotIn.setSearchOperationNumber(null);
        objProcessOperationListForLotIn.setCurrentFlag(true);
        objProcessOperationListForLotIn.setLotID(reworkReq.getLotID());
        // TODO: 2019/10/15 don not need refacor Temporarily
        List<Infos.OperationNameAttributes> operationNameAttributesAttributesList = processMethod.processOperationListForLot(objCommon, objProcessOperationListForLotIn);

        int lengthNameAttributes = (CimArrayUtils.isEmpty(operationNameAttributesAttributesList)) ? 0 : CimArrayUtils.getSize(operationNameAttributesAttributesList);
        log.debug("lengthNameAttributes < 2 : {}",lengthNameAttributes < 2);
        if (lengthNameAttributes < 2) {
            log.error("invalid rework operation");
            Validations.check(retCodeConfig.getInvalidReworkOperation());
        } else {
            log.debug("!StringUtils.equals(operationNameAttributesAttributesList.get(0).getRouteID().getValue(), operationNameAttributesAttributesList.get(1).getRouteID().getValue()) : {}",
                    !CimStringUtils.equals(operationNameAttributesAttributesList.get(0).getRouteID().getValue(), operationNameAttributesAttributesList.get(1).getRouteID().getValue()));

            if (!CimStringUtils.equals(operationNameAttributesAttributesList.get(0).getRouteID().getValue(), operationNameAttributesAttributesList.get(1).getRouteID().getValue())) {
                log.error("invalid rework operation");
                Validations.check(retCodeConfig.getInvalidReworkOperation());
            }
        }

        //【step13】check route is dynamic or not.
        log.debug("【step13】check route is dynamic or not.");
        log.trace("BooleanUtils.isTrue(reworkReq.getDynamicRouteFlag()) : {}", CimBooleanUtils.isTrue(reworkReq.getDynamicRouteFlag()));
        if (CimBooleanUtils.isTrue(reworkReq.getDynamicRouteFlag())) {
            Outputs.ObjProcessCheckForDynamicRouteOut checkForDynamicRouteOutRetCode = processMethod.processCheckForDynamicRoute(objCommon, subRouteID);
            Validations.check (CimBooleanUtils.isFalse(checkForDynamicRouteOutRetCode.getDynamicRouteFlag()), retCodeConfig.getNotDynamicRoute());
        }

        //【step14】check input return operation nubmer
        log.debug("【step14】check input return operation nubmer");
        Boolean inputReturnOperationFlag = false;
        String returnOperationNumber = null;
        log.trace("!ObjectUtils.isEmpty(reworkReq.getReturnOperationNumber()) : {}",!CimObjectUtils.isEmpty(reworkReq.getReturnOperationNumber()));
        if (!CimObjectUtils.isEmpty(reworkReq.getReturnOperationNumber())) {
            inputReturnOperationFlag = true;
        }

        /**************************************************************************************************************/
        /*【step15】check input return operation number is exist in connected route list                              */
        /*    - check process dynamic route                                                                           */
        /*    - get process return operation                                                                          */
        /**************************************************************************************************************/
        log.debug("【step15】check input return operation number is exist in connected route list");
        Boolean connectedRouteReturnOperationFlag = false;
        Boolean sameReturnOperationFlag = false;

        //【step15-1】check process dynamic route
        log.debug("【step15-1】check process dynamic route");
        Outputs.ObjProcessCheckForDynamicRouteOut checkForDynamicRouteOutRetCode = processMethod.processCheckForDynamicRoute(objCommon, subRouteID);

        //【step15-2】get process return operations
        log.debug("【step15-2】get process return operations");
        Outputs.ObjProcessGetReturnOperationOut returnOperationOutRetCode = null;
        try{
            returnOperationOutRetCode = processMethod.processGetReturnOperation(objCommon, lotID, subRouteID);
            connectedRouteReturnOperationFlag = true;
            log.trace("ObjectUtils.equalsWithValue(reworkReq.getReturnOperationNumber(), returnOperationOutRetCode.getOperationNumber()) : {}",
                    CimStringUtils.equals(reworkReq.getReturnOperationNumber(), returnOperationOutRetCode.getOperationNumber()));

            log.trace("!BooleanUtils.isTrue(checkForDynamicRouteOutRetCode.getDynamicRouteFlag() : {}",!CimBooleanUtils.isTrue(checkForDynamicRouteOutRetCode.getDynamicRouteFlag()));

            if (CimStringUtils.equals(reworkReq.getReturnOperationNumber(), returnOperationOutRetCode.getOperationNumber())) {
                sameReturnOperationFlag = true;
            } else if (!CimBooleanUtils.isTrue(checkForDynamicRouteOutRetCode.getDynamicRouteFlag())
                    && !CimObjectUtils.isEmpty(reworkReq.getReturnOperationNumber())) {
                log.error("invalid input param");
                Validations.check(retCodeConfig.getInvalidInputParam());
            }
        }catch (ServiceException ex){
            log.trace("Validations.isEquals(retCodeConfig.getNotFoundSubRoute(), ex.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotFoundSubRoute(), ex.getCode()));
            if (Validations.isEquals(retCodeConfig.getNotFoundSubRoute(), ex.getCode())) {
                log.error("process_GetReturnOperation() == RC_NOT_FOUND_SUBROUTE");
                returnOperationOutRetCode = (Outputs.ObjProcessGetReturnOperationOut) ex.getData();
            } else {
                throw ex;
            }
        }

        //【step16】get current routeID and operation number
        log.debug("【step16】get current routeID and operation number");
        Outputs.ObjLotCurrentOperationInfoGetOut currentOperationInfoGetOutRetCode = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);

        log.debug("currentRouteID:%s", currentOperationInfoGetOutRetCode.getRouteID());
        log.debug("currentOperationNumber:%s", currentOperationInfoGetOutRetCode.getOperationNumber());

        //【step17】decide return operation number using all flag.
        log.debug("【step17】decide return operation number using all flag.");
        log.trace("BooleanUtils.isTrue(reworkReq.getDynamicRouteFlag()) : {}", CimBooleanUtils.isTrue(reworkReq.getDynamicRouteFlag()));
        if (CimBooleanUtils.isTrue(reworkReq.getDynamicRouteFlag())) {
            log.debug("strReworkReq.bDynamicRoute = TRUE");
            log.trace("BooleanUtils.isTrue(inputReturnOperationFlag) : {}", CimBooleanUtils.isTrue(inputReturnOperationFlag));
            if (CimBooleanUtils.isTrue(inputReturnOperationFlag)) {
                returnOperationNumber = reworkReq.getReturnOperationNumber();
            } else {
                log.debug("inputReturnOperationFlag == FALSE");
                returnOperationNumber = CimBooleanUtils.isTrue(connectedRouteReturnOperationFlag) ? returnOperationOutRetCode.getOperationNumber()
                        : currentOperationInfoGetOutRetCode.getOperationNumber();
            }
        } else {
            log.trace("BooleanUtils.isTrue(connectedRouteReturnOperationFlag) : {}", CimBooleanUtils.isTrue(connectedRouteReturnOperationFlag));
            if (CimBooleanUtils.isTrue(connectedRouteReturnOperationFlag)) {
                log.debug("connectedRouteReturnOperationFlag == TRUE");
                returnOperationNumber = CimBooleanUtils.isTrue(inputReturnOperationFlag) ? reworkReq.getReturnOperationNumber() :
                        returnOperationOutRetCode.getOperationNumber();
            } else {
                log.error("connectedRouteReturnOperationFlag ==FALSE");
                Validations.check(retCodeConfig.getInvalidRouteId());
            }
        }

        //【step18】check processDefinitionType is 'REWORK' or not
        log.debug("【step18】check processDefinitionType is 'REWORK' or not");
        Validations.check (!CimStringUtils.equals(BizConstant.SP_MAINPDTYPE_REWORK, returnOperationOutRetCode.getProcessDefinitionType()), retCodeConfig.getInvalidRouteType());

        //【step19】check decided return operation is exist on current route
        log.debug("【step19】check decided return operation is exist on current route");
        List<Infos.OperationNameAttributes> listRetCode = lotInqService.sxProcessFlowOperationListForLotInq(objCommon, lotID);

        int opeLen = CimArrayUtils.getSize(listRetCode);
        for (int opeCnt=0; opeCnt < opeLen; opeCnt++ ) {
            Infos.OperationNameAttributes operationNameAttributes = listRetCode.get(opeCnt);
            log.trace("ObjectUtils.equalsWithValue(operationNameAttributes.getOperationNumber(),returnOperationNumber) : {}", CimStringUtils.equals(operationNameAttributes.getOperationNumber(), returnOperationNumber));
            log.trace(" opeCnt == opeLen - 1 : {}", opeCnt == opeLen - 1);
            if(CimStringUtils.equals(operationNameAttributes.getOperationNumber(), returnOperationNumber)) {
                log.debug("return operation is exist on current route");
                break;
            }
            else if ( opeCnt == opeLen - 1 ) {
                Validations.check(new OmCode(retCodeConfig.getNotFoundOperation(),returnOperationNumber));
            }
        }

        /**************************************************************************************************************/
        /*【step20】check routeID confilication                                                                       */
        /*    - lot_originalRouteList_Get                                                                             */
        /*    - check currentRoute vs SubRoute                                                                        */
        /*    - check return Route ID vs SubRoute                                                                     */
        /**************************************************************************************************************/
        //【step20-1】get original route list by lot
        log.debug("【step20-1】get original route list by lot");
        Outputs.ObjLotOriginalRouteListGetOut lotOriginalRouteListGetOutRetCode = lotMethod.lotOriginalRouteListGet(objCommon, reworkReq.getLotID());

        //【step20-2】check current route vs sub route
        log.debug("【step20-2】check current route vs sub route");
        Validations.check (CimStringUtils.equals(currentOperationInfoGetOutRetCode.getRouteID().getValue(), subRouteID.getValue()), retCodeConfig.getInvalidBranchRouteId());

        //【step20-3】check return route id vs sub route
        log.debug("【step20-3】check return route id vs sub route");
        int routeIDSize = CimArrayUtils.getSize(lotOriginalRouteListGetOutRetCode.getOriginalRouteID());
        for (int i = 0; i < routeIDSize; i++) {
            ObjectIdentifier originalRouteID = lotOriginalRouteListGetOutRetCode.getOriginalRouteID().get(i);
            Validations.check (CimStringUtils.equals(originalRouteID.getValue(), subRouteID.getValue()), retCodeConfig.getInvalidBranchRouteId());
        }

        //【step21】check max rework count
        log.debug("【step21】check max rework count");
        if (CimBooleanUtils.isFalse(reworkReq.getForceReworkFlag())) {
            processMethod.processReworkCountCheck(objCommon, reworkReq.getLotID());
        }

        //【step22】check future hold
        log.debug("【step22】check future hold");
        lotMethod.lotFutureHoldRequestsCheckBranch(objCommon, lotID, returnOperationNumber);

        //【step23】check future action procedure
        log.debug("【step23】check future action procedure");
        String tmpRouteID = currentOperationInfoGetOutRetCode.getRouteID().getValue();
        scheduleChangeReservationMethod.schdlChangeReservationCheckForFutureOperation(objCommon, reworkReq.getLotID(), tmpRouteID, returnOperationNumber);

        /**************************************************************************************************************/
        /*【step24】check flow batch condition.                                                                       */
        /*    FlowBatched lot :                                                                                       */
        /*      - Can NOT Branch on a Branch Route which returns outside the flow batch section.                      */
        /*      - Can NOT Branch on a Branch Route which returns to the operation beyond a target process             */
        /*    Not FlowBatched lot :                                                                                   */
        /*      - Can NOT Branch on a Branch Route which returns to the flow batch section.                           */
        /**************************************************************************************************************/
        log.debug("【step24】check flow batch condition.");

        Inputs.ObjLotCheckFlowBatchConditionForReworkIn lotCheckFlowBatchConditionForReworkIn = new Inputs.ObjLotCheckFlowBatchConditionForReworkIn();
        lotCheckFlowBatchConditionForReworkIn.setReworkReq(reworkReq);

        //【step24-1】check input parameters are empty or not.
        log.debug("【step24-1】check input parameters are empty or not.");
        log.trace("ObjectUtils.isEmpty(reworkReq.getCurrentRouteID()) : {}", ObjectIdentifier.isEmpty(reworkReq.getCurrentRouteID()));
        if (ObjectIdentifier.isEmpty(reworkReq.getCurrentRouteID())) {
            reworkReq.setCurrentRouteID(currentOperationInfoGetOutRetCode.getRouteID());  // the getObject() must not be null.
        }

        log.trace("ObjectUtils.isEmpty(reworkReq.getCurrentOperationNumber()) : {}", CimObjectUtils.isEmpty(reworkReq.getCurrentOperationNumber()));
        if (CimObjectUtils.isEmpty(reworkReq.getCurrentOperationNumber())) {
            reworkReq.setCurrentOperationNumber(currentOperationInfoGetOutRetCode.getOperationNumber()); //the getObject() must not be null.
        }

        log.trace("ObjectUtils.isEmpty(reworkReq.getReturnOperationNumber()) : {}", CimObjectUtils.isEmpty(reworkReq.getReturnOperationNumber()));
        if (CimObjectUtils.isEmpty(reworkReq.getReturnOperationNumber())) {
            reworkReq.setReturnOperationNumber(returnOperationOutRetCode.getOperationNumber());  // the getObject() must not be null.
        }
        try {
            lotMethod.lotCheckFlowBatchConditionForRework(objCommon, lotCheckFlowBatchConditionForReworkIn);
        } catch (ServiceException e) {
            log.trace("Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode())\n" +
                    "                    || Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode())\n" +
                    "                    || Validations.isEquals(retCodeConfig.getNotLocateToOverTarget(), e.getCode()) : {}",
                    Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode())
                            || Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode())
                            || Validations.isEquals(retCodeConfig.getNotLocateToOverTarget(), e.getCode()));

            if (Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getNotLocateToOverTarget(), e.getCode())) {
                log.error("lotCheckFlowBatchConditionForRework() = rc.not_rework_batch_ope");
                StringBuilder errorMsg = new StringBuilder();
                log.trace("Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode()));
                if (Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode())){
                    errorMsg.append("Return point of the rework operation is in a FlowBatch Section.");
                } else if (Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode())){
                    errorMsg.append("FlowBatched lots cannot go out of the FlowBatch Section.");
                } else if (Validations.isEquals(retCodeConfig.getNotLocateToOverTarget(), e.getCode())){
                    errorMsg.append("FlowBatched lots cannot step over the target operation in the FlowBatch Section.");
                }
                throw new ServiceException(new OmCode(retCodeConfig.getNotReworkBatchOpe(), errorMsg.toString()));
            } else {
                log.error("lotCheckFlowBatchConditionForRework() != ok");
                throw e;
            }
        }

        //【step25】check bonding flow condition.
        log.debug("【step25】check bonding flow condition.");
        log.trace("ObjectUtils.isEmpty(reworkReq.getCurrentRouteID()) : {}", ObjectIdentifier.isEmpty(reworkReq.getCurrentRouteID()));
        if (ObjectIdentifier.isEmpty(reworkReq.getCurrentRouteID())) {
            reworkReq.setCurrentRouteID(currentOperationInfoGetOutRetCode.getRouteID());
        }
        log.trace("ObjectUtils.isEmpty(reworkReq.getCurrentOperationNumber()) : {}", CimObjectUtils.isEmpty(reworkReq.getCurrentOperationNumber()));
        if (CimObjectUtils.isEmpty(reworkReq.getCurrentOperationNumber())) {
            reworkReq.setCurrentOperationNumber(currentOperationInfoGetOutRetCode.getOperationNumber());
        }
        log.trace("ObjectUtils.isEmpty(reworkReq.getReturnOperationNumber()) : {}", CimObjectUtils.isEmpty(reworkReq.getReturnOperationNumber()));
        if (CimObjectUtils.isEmpty(reworkReq.getReturnOperationNumber())) {
            reworkReq.setReturnOperationNumber(returnOperationOutRetCode.getOperationNumber());
        }
        try {
            lotMethod.lotCheckBondingFlowSectionForRework(objCommon, reworkReq);
        } catch (ServiceException e) {
            log.trace("Validations.isEquals(retCodeConfig.getNotLocatetoBondingflowsection(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotLocatetoBondingflowsection(), e.getCode()));
            if (Validations.isEquals(retCodeConfig.getNotLocatetoBondingflowsection(), e.getCode())) {
                Validations.check(retCodeConfig.getNotReworkBondingFlow());
            } else {
                throw e;
            }
        }


        //【step26】increment process rework count
        log.debug("【step26】increment process rework count, the lotID:%s", lotID);
        processMethod.processReworkCountIncrement(objCommon, lotID);

        //【step27】check q-time condition for replace target
        log.debug("【step27】check q-time condition for replace target");
        qTimeMethod.qTimeCheckConditionForReplaceTarget(objCommon, lotID);
        //【step28】process branch route
        log.debug("【step28】process branch route");
        Inputs.OldCurrentPOData oldCurrentPODataRetCode = processMethod.processBranchRoute(objCommon, lotID, subRouteID, returnOperationNumber);

        //【step29】update cassette's multiLotType
        log.debug("【step29】update cassette's multiLotType");
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);

        //【step30】update RequiredCassetteCategory
        log.debug("【step30】upDate RequiredCassetteCategory");
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, reworkReq.getLotID());

        //【step31】process hold
        log.debug("【step31】process hold");
        processControlService.sxProcessHoldDoActionReq(objCommon, reworkReq.getLotID(), claimMemo);

        //【step32】qTime_targetOpe_Replace
        log.debug("【step32】qTime_targetOpe_Replace");
        Infos.QTimeTargetOpeReplaceIn inputParams = new Infos.QTimeTargetOpeReplaceIn();
        inputParams.setLotID(lotID);
        inputParams.setSpecificControlFlag(false);
        qTimeMethod.qTimeTargetOpeReplace(objCommon, inputParams);

        minQTimeMethod.checkTargetOpeReplace(objCommon, lotID);

        //【step33】make history
        log.debug("【step33】make history");
        Inputs.LotReworkEventMakeParams lotReworkEventMakeParams = new Inputs.LotReworkEventMakeParams();
        lotReworkEventMakeParams.setLotID(lotID);
        lotReworkEventMakeParams.setReasonCodeID(reworkReq.getReasonCodeID());
        lotReworkEventMakeParams.setOldCurrentPOData(oldCurrentPODataRetCode);
        lotReworkEventMakeParams.setClaimMemo(claimMemo);
        lotReworkEventMakeParams.setTransactionID(TransactionIDEnum.DYNAMIC_REWORK_REQ.getValue());
        eventMethod.lotReworkEventMake(objCommon, lotReworkEventMakeParams);

        // 【step 34】 check contamination level ，trigger contamination hold
        log.debug("【step33】check contamination level ，trigger contamination hold");
        contaminationMethod.lotCheckContaminationLevelStepOut(objCommon, lotID);

    }


    @Override
    public void sxReworkCancelReq(Infos.ObjCommon objCommon, Params.ReworkCancelReqParams params) {

        ObjectIdentifier lotID = params.getLotID();
        ObjectIdentifier currentRouteID = params.getCurrentRouteID();   // current route id
        String currentOperationNumber = params.getCurrentOperationNumber();             // current operation number
        ObjectIdentifier reasonCodeID = params.getReasonCodeID();

        //【step1】get lot cassette
        log.debug("【step1】get lot cassette");
        ObjectIdentifier lotCassetteOutRetCode = lotMethod.lotCassetteGet(objCommon, params.getLotID());

        ObjectIdentifier cassetteID = lotCassetteOutRetCode;

        // 【step2】object lock for cassette
        log.debug("【step2】object lock for cassette");
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);

        String strLotOperationEICheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE, strLotOperationEICheck) : {}", CimStringUtils.equals(BizConstant.CONSTANT_QUANTITY_ONE, strLotOperationEICheck));
        if (CimStringUtils.equals(BizConstant.CONSTANT_QUANTITY_ONE, strLotOperationEICheck)) {
            //【step3】the transferState should not be EI.
            log.debug("【step3】the transferState should not be EI.");
            String transferStateGetOutRetCode
                    = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
            Validations.check(CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferStateGetOutRetCode),
                    retCodeConfig.getInvalidCassetteTransferState(), transferStateGetOutRetCode,cassetteID);
        }

        //【step4】lock object tobe updated
        log.debug("【step4】lock object tobe updated");
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);

        //【step5】check whether lot is on the specified route/ operation or not.
        log.debug("【step5】check whether lot is on the specified route/ operation or not.");
        ObjectIdentifier tmpCurrentRouteID = currentRouteID;
        String tmpCurrentOperationNumber = new String(currentOperationNumber);

        log.trace("!ObjectUtils.isEmpty(tmpCurrentRouteID.getValue()) && !ObjectUtils.isEmpty(tmpCurrentOperationNumber) : {}",!CimObjectUtils.isEmpty(tmpCurrentRouteID.getValue()) && !CimObjectUtils.isEmpty(tmpCurrentOperationNumber));
        if (!CimObjectUtils.isEmpty(tmpCurrentRouteID.getValue()) && !CimObjectUtils.isEmpty(tmpCurrentOperationNumber)) {
            Outputs.ObjLotCurrentOperationInfoGetOut getOutRetCode = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);
            log.trace("ObjectUtils.equalsWithValue(tmpCurrentRouteID, getOutRetCode.getRouteID())\n" +
                    "                    && ObjectUtils.equalsWithValue(tmpCurrentOperationNumber, getOutRetCode.getOperationNumber()) : {}",
                    ObjectIdentifier.equalsWithValue(tmpCurrentRouteID, getOutRetCode.getRouteID())
                            && CimStringUtils.equals(tmpCurrentOperationNumber, getOutRetCode.getOperationNumber()));

            if (ObjectIdentifier.equalsWithValue(tmpCurrentRouteID, getOutRetCode.getRouteID())
                    && CimStringUtils.equals(tmpCurrentOperationNumber, getOutRetCode.getOperationNumber())) {
                log.debug("route/operation check ok. go ahead...");
            } else {
                log.error("route/operation check NG, not same route.");
                throw new ServiceException(new OmCode(retCodeConfig.getNotSameRoute(), "Input parameter's currentRouteID/currentOperationNumber", "lot's current currentRouteID/currentOperationNumber"));
            }
        }else {
            log.debug("InPara Current Info is Null. No Check!!");
        }

        //【step6】check condition
        log.debug("【step6】check condition");

        //【step6-1】get lot hold state
        log.debug("【step6-1】get lot hold state");
        String lotHoldStateGetOut = lotMethod.lotHoldStateGet(objCommon, lotID);
        Validations.check (CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, lotHoldStateGetOut),
                retCodeConfig.getInvalidLotHoldStat(),lotID,lotHoldStateGetOut);

        //【step6-2】get lot state
        log.debug("【step6-2】get lot state");
        String lotStateOut = lotMethod.lotStateGet(objCommon, lotID);
        String lotState = lotStateOut;
        Validations.check (!CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotState),
                retCodeConfig.getInvalidLotStat(),lotState);

        //【step6-3】get process state
        log.debug("【step6-3】get process state");
        String lotProcesssState = lotMethod.lotProcessStateGet(objCommon, lotID);
        Validations.check (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotProcesssState),
                retCodeConfig.getInvalidLotProcessState(),lotID,lotProcesssState);

        //【step6-4】get product state
        log.debug("【step6-4】get product state");
        String lotProductionState = lotMethod.lotProductionStateGet(objCommon, lotID);
        Validations.check (!CimStringUtils.equals(CIMStateConst.CIM_LOT_PRODUCTION_STATE_INREWORK, lotProductionState),
                retCodeConfig.getInvalidLotProductionState(), lotID, lotProductionState);

        //【step6-5】get inventory state
        log.debug("【step6-5】get inventory state");
        String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, lotID);
        Validations.check (!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, lotInventoryState),
                retCodeConfig.getInvalidLotInventoryStat(),lotID,lotInventoryState);

        //【step6-6】get lot inPostProcessFlag
        log.debug("【step6-6】get lot inPostProcessFlag");
        Outputs.ObjLotInPostProcessFlagOut postProcessFlagOutRetCode = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);
        if (CimBooleanUtils.isTrue(postProcessFlagOutRetCode.getInPostProcessFlagOfLot())) {
            log.debug("lot is in post process.");

            //【step6-7】get user group id bu user id
            log.debug("【step6-7】get user group id bu user id");
            List<ObjectIdentifier> userGroupListGetOutRetCode = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());

            List<ObjectIdentifier> userGroupIDList = userGroupListGetOutRetCode;
            int size = CimArrayUtils.getSize(userGroupIDList);
            log.debug("the size of user group id list, %d", size);
            boolean externalPostProcesUserFlag = false;
            for (int i = 0; i < size; i++) {
            }

            Validations.check (CimBooleanUtils.isFalse(externalPostProcesUserFlag), retCodeConfig.getLotInPostProcess(),lotID);
        }

        //【TODO-NOTIMPL】【step6-8】get interFab transfer plan list (line:437 - 460)
        log.debug("【TODO-NOTIMPL】【step6-8】get interFab transfer plan list");

        //【step6-9】check lot's control job id
        log.debug("【step6-9】check lot's control job id");
        ObjectIdentifier lotControlJobIDGetOutRetCode = lotMethod.lotControlJobIDGet(objCommon, lotID);
        if (!ObjectIdentifier.isEmpty(lotControlJobIDGetOutRetCode)) {
            log.error("lot control job id filled.");
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),lotID.getValue(),lotControlJobIDGetOutRetCode.getValue()));
        }

        //【step6-10】check process branch cancel
        log.debug("【step6-10】check process branch cancel");
        processMethod.processCheckBranchCancel(objCommon, lotID);

        //【step6-11】check lot future hold request branch cancel
        log.debug("【step6-11】check lot future hold request branch cancel");
        lotMethod.lotFutureHoldRequestsCheckBranchCancel(objCommon, lotID);

        //【step6-12】check future action procedure
        log.debug("【TODO-NOTIMPL】【step6-12】check future action procedure");
        scheduleChangeReservationMethod.schdlChangeReservationCheckForBranchCancelDR(objCommon,lotID);

        log.debug("【step7】roll back target operation for rework route");
        qtimeMethod.qTimeTargetOpeCancelReplace(objCommon, lotID);
        //【step8】change state
        log.debug("【step8】change state");
        processMethod.processReworkCountDecrement(objCommon, lotID);

        //【step9】cancel process branch route
        log.debug("【step9】cancel process branch route");
        Inputs.OldCurrentPOData oldCurrentPODataRetCode = processMethod.processCancelBranchRoute(objCommon, lotID);

        //【step10】call lotFutureHoldRequestsEffectByCondition
        log.debug("【step10】call lotFutureHoldRequestsEffectByCondition");
        Infos.EffectCondition effectCondition  = new Infos.EffectCondition();
        effectCondition.setPhase(BizConstant.SP_FUTUREHOLD_PRE);
        effectCondition.setTriggerLevel(BizConstant.SP_FUTUREHOLD_ALL);
        Outputs.ObjLotFutureHoldRequestsEffectByConditionOut conditionOutRetCode
                = lotMethod.lotFutureHoldRequestsEffectByCondition(objCommon, lotID, effectCondition);

        log.trace("!ArrayUtils.isEmpty(conditionOutRetCode.getStrLotHoldReqList()) : {}",!CimArrayUtils.isEmpty(conditionOutRetCode.getStrLotHoldReqList()));
        if (!CimArrayUtils.isEmpty(conditionOutRetCode.getStrLotHoldReqList())) {
            sxHoldLotReq(objCommon, lotID, conditionOutRetCode.getStrLotHoldReqList());
        }

        //【step11】call lotFutureHoldRequestsDeleteEffectedByCondition
        log.debug("【step11】call lotFutureHoldRequestsDeleteEffectedByCondition");
        effectCondition.setPhase(BizConstant.SP_FUTUREHOLD_PRE);
        effectCondition.setTriggerLevel(BizConstant.SP_FUTUREHOLD_SINGLE);
        Outputs.ObjLotFutureHoldRequestsDeleteEffectedByConditionOut deleteEffectedByConditionOutRetCode
                = lotMethod.lotFutureHoldRequestsDeleteEffectedByCondition(objCommon, lotID, effectCondition);

        log.trace("!ArrayUtils.isEmpty(deleteEffectedByConditionOutRetCode.getStrFutureHoldReleaseReqList()) : {}",!CimArrayUtils.isEmpty(deleteEffectedByConditionOutRetCode.getStrFutureHoldReleaseReqList()));
        if (!CimArrayUtils.isEmpty(deleteEffectedByConditionOutRetCode.getStrFutureHoldReleaseReqList())) {
            String releaseReasonCodeID = "";
            processControlService.sxFutureHoldCancelReq(objCommon, lotID, new ObjectIdentifier(releaseReasonCodeID),
                    BizConstant.SP_ENTRYTYPE_REMOVE, deleteEffectedByConditionOutRetCode.getStrFutureHoldReleaseReqList());
        }

        //【step12】update cassette's multi lot type
        log.debug("【step12】update cassette's multi lot type");
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);

        //【step13】update required cassette category
        log.debug("【step13】update required cassette category");
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotID);

        //【step14】process hold
        log.debug("【step14】process hold");
        processControlService.sxProcessHoldDoActionReq(objCommon,lotID, params.getClaimMemo());

        //【step15】make history
        log.debug("【step15】make history");
        Inputs.LotReworkEventMakeParams lotReworkEventMakeParams = new Inputs.LotReworkEventMakeParams();
        lotReworkEventMakeParams.setTransactionID(TransactionIDEnum.REWORK_WHOLE_LOT_CANCEL_REQ.getValue());
        lotReworkEventMakeParams.setLotID(params.getLotID());
        lotReworkEventMakeParams.setReasonCodeID(params.getReasonCodeID());
        lotReworkEventMakeParams.setOldCurrentPOData(oldCurrentPODataRetCode);
        lotReworkEventMakeParams.setClaimMemo(params.getClaimMemo());
        eventMethod.lotReworkEventMake(objCommon, lotReworkEventMakeParams);

        // 【step 16】 check contamination level ，trigger contamination hold
        log.debug("【step16】check contamination level ，trigger contamination hold");
        contaminationMethod.lotCheckContaminationLevelStepOut(objCommon, lotID);
    }


    public void sxReworkWithHoldReleaseReq(Infos.ObjCommon objCommon, Params.ReworkWithHoldReleaseReqParams reworkWithHoldReleaseReqParams){
        /*----------------------------------------*/
        /*    Call txHoldLotReleaseReq            */
        /*----------------------------------------*/
        log.debug("[step-1] Call txHoldLotReleaseReq");
        Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
        holdLotReleaseReqParams.setReleaseReasonCodeID(reworkWithHoldReleaseReqParams.getReleaseReasonCodeID());
        holdLotReleaseReqParams.setLotID(reworkWithHoldReleaseReqParams.getStrReworkReq().getLotID());
        holdLotReleaseReqParams.setHoldReqList(reworkWithHoldReleaseReqParams.getHoldReqList());
        holdLotReleaseReqParams.setUser(objCommon.getUser());
        sxHoldLotReleaseReq(objCommon,holdLotReleaseReqParams);


        /*-----------------------------------------*/
        /*    Call txReworkReq                     */
        /*-----------------------------------------*/
        log.debug("[step-2] Call txReworkReq");
        sxReworkReq(objCommon,reworkWithHoldReleaseReqParams.getStrReworkReq(), reworkWithHoldReleaseReqParams.getClaimMemo());
    }


    public ObjectIdentifier sxSplitLotNotOnPfReq(Infos.ObjCommon objCommon, Params.SplitLotNotOnPfReqParams splitLotNotOnPfReqParams) {
        //-------------------------------------
        // Prepare objectIdentifier structure
        // for local lot
        //-------------------------------------

        log.debug("Prepare objectIdentifier structure  for local lot");
        ObjectIdentifier aParentCassetteID = null;
        ObjectIdentifier objectIdentifier = new ObjectIdentifier();
        log.trace("(!ArrayUtils.isEmpty(splitLotNotOnPfReqParams.getChildWaferIDs()) : {}",!CimArrayUtils.isEmpty(splitLotNotOnPfReqParams.getChildWaferIDs()));
        if (!CimArrayUtils.isEmpty(splitLotNotOnPfReqParams.getChildWaferIDs())) {
            log.debug("childWaferID.length() > 0");
            //-------------------------------------------------------------
            // Get cassetteID which childWaferID[0] is in for object_Lock().
            //-------------------------------------------------------------
            log.debug("Get cassetteID which childWaferID[0] is in for object_Lock()");
            Outputs.ObjWaferLotCassetteGetOut waferLotCassetteGetOut = waferMethod.waferLotCassetteGet(objCommon, splitLotNotOnPfReqParams.getChildWaferIDs().get(0));

            log.trace("!StringUtils.isEmpty(waferLotCassetteGetOut.getCassetteID()) : {}",!ObjectIdentifier.isEmpty(waferLotCassetteGetOut.getCassetteID()));
            if (!ObjectIdentifier.isEmpty(waferLotCassetteGetOut.getCassetteID())) {
                objectIdentifier = waferLotCassetteGetOut.getCassetteID();
                aParentCassetteID = objectIdentifier;
            }
            Validations.check (!ObjectIdentifier.equalsWithValue(splitLotNotOnPfReqParams.getParentLotID(), waferLotCassetteGetOut.getLotID()), retCodeConfig.getInvalidLotContents());
        } else {
            log.debug("childWaferID.length() <= 0");
            //-------------------------------------------------------------
            // Get cassetteID which parentLot is in for object_Lock().
            //-------------------------------------------------------------

            //----------------------------
            // set parentLotID of in-parm.
            //----------------------------
            log.debug("Get cassetteID which parentLot is in for object_Lock().");
            ObjectIdentifier lotCassetteOut = null;
            try {
                lotCassetteOut = lotMethod.lotCassetteGet(objCommon, splitLotNotOnPfReqParams.getParentLotID());
            } catch (ServiceException e) {
                log.trace("!Validations.isEquals(retCodeConfig.getNotFoundCst() ,e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotFoundCst() ,e.getCode()));
                if (!Validations.isEquals(retCodeConfig.getNotFoundCst() ,e.getCode())) {
                    throw e;
                }
            }

            log.debug("null == lotCassetteOut : {}",null == lotCassetteOut);
            if (null == lotCassetteOut) {
                aParentCassetteID = null;
            } else {
                aParentCassetteID = lotCassetteOut;
            }
        }
        String tmpLotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        tmpLotOperationEIcheck = null == tmpLotOperationEIcheck ? "0" : tmpLotOperationEIcheck;
        Integer lotOperationEIcheck = Integer.valueOf(tmpLotOperationEIcheck);
        Boolean updateControlJobFlag = false;
        Long lockMode = 0L;
        //-------------------------------
        // If parentCassetteID is filled
        //-------------------------------
        String cassetteTransferStateOut = null;
        log.trace("!ObjectUtils.isEmpty(aParentCassetteID) : {}",!ObjectIdentifier.isEmpty(aParentCassetteID));
        if (!ObjectIdentifier.isEmpty(aParentCassetteID)) {
            log.debug("(aParentCassetteID.identifier) != 0");
            log.trace("StringUtils.equals(lotOperationEIcheck.toString(),\"0\") : {}", CimStringUtils.equals(lotOperationEIcheck.toString(),"0"));
            if (CimStringUtils.equals(lotOperationEIcheck.toString(),"0")) {
                //-------------------------------
                // Get carrier transfer status
                //-------------------------------
                log.debug("Get carrier transfer status");
                cassetteTransferStateOut = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
                /*------------------------------------*/
                /*   Get eqp ID in cassette     */
                /*------------------------------------*/
                log.debug("Get eqp ID in cassette");
                Outputs.ObjCassetteEquipmentIDGetOut objCassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, aParentCassetteID);
                //-------------------------------
                // Get required eqp lock mode
                //-------------------------------
                // object_lockMode_Get
                log.debug("Get required eqp lock mode");
                Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
                objLockModeIn.setObjectID(objCassetteEquipmentIDGetOut.getEquipmentID());
                objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objLockModeIn.setFunctionCategory(TransactionIDEnum.SPLIT_WAFER_LOT_NOT_ON_ROUTE_REQ.getValue());
                objLockModeIn.setUserDataUpdateFlag(false);
                Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
                lockMode = objLockModeOut.getLockMode();
                log.trace("StringUtils.equals(cassetteTransferStateOut, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) : {}", CimStringUtils.equals(cassetteTransferStateOut, BizConstant.SP_TRANSSTATE_EQUIPMENTIN));
                if (CimStringUtils.equals(cassetteTransferStateOut, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                    updateControlJobFlag = true;
                    log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                    if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                        // advanced_object_Lock
                        objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                                BizConstant.SP_CLASSNAME_POSMACHINE,
                                BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                                objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                        //  advanced_object_Lock
                        objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                                BizConstant.SP_CLASSNAME_POSMACHINE,
                                BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                                (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, Collections.singletonList(aParentCassetteID.getValue())));
                    } else {
                        /*--------------------------------*/
                        /*   Lock Macihne object          */
                        /*--------------------------------*/
                        log.debug("Lock Macihne object");
                        objectLockMethod.objectLock(objCommon, CimMachine.class, objCassetteEquipmentIDGetOut.getEquipmentID());
                    }
                }
            }
            //-------------------------------
            // Lock objects to be updated
            //-------------------------------
            //  object_Lock
            log.debug("Lock objects to be updated");
            objectLockMethod.objectLock(objCommon, CimCassette.class, aParentCassetteID);

            // object_Lock
            log.trace("lotOperationEIcheck == 0 : {}",lotOperationEIcheck == 0);
            if (lotOperationEIcheck == 0){
                log.trace("!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                if (!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                    //---------------------------------
                    //   Get Cassette's ControlJobID
                    //--------------------------------
                    log.debug("Get Cassette's ControlJobID");
                    ObjectIdentifier controlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, aParentCassetteID);
                    log.trace("!ObjectUtils.isEmptyWithValue(controlJobID) : {}",!ObjectIdentifier.isEmptyWithValue(controlJobID));
                    if (!ObjectIdentifier.isEmptyWithValue(controlJobID)){
                        updateControlJobFlag = true;
                        log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                            /*------------------------------*/
                            /*   Lock ControlJob Object     */
                            /*------------------------------*/
                            log.debug("Lock ControlJob Object");
                            objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
                        }
                    }
                }
            }

            // object_Lock
            log.debug("lock lotObject");
            objectLockMethod.objectLock(objCommon, CimLot.class, splitLotNotOnPfReqParams.getParentLotID());

            //-------------------------------
            // Check carrier transfer status
            //-------------------------------
            log.debug("Check carrier transfer status");
            log.trace("1 == lotOperationEIcheck\n" +
                    "                    || (0 == lotOperationEIcheck\n" +
                    "                    && !StringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateOut)) : {}",
                    1 == lotOperationEIcheck
                            || (0 == lotOperationEIcheck
                            && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateOut)));

            if (1 == lotOperationEIcheck
                    || (0 == lotOperationEIcheck
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateOut))) {
                cassetteTransferStateOut = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
                log.trace("StringUtils.equals(lotOperationEIcheck.toString(),\"0\") : {}", CimStringUtils.equals(lotOperationEIcheck.toString(),"0"));
                log.trace("StringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT,cassetteTransferStateOut)\n" +
                        "                        || StringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateOut) : {}",
                        CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT,cassetteTransferStateOut)
                                || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateOut));

                if (CimStringUtils.equals(lotOperationEIcheck.toString(),"0")) {
                    log.debug("lotOperationEIcheck = 0");
                    Validations.check (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateOut), retCodeConfig.getChangedToEiByOtherOperation());
                } else if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT,cassetteTransferStateOut)
                        || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateOut)){
                    log.debug("XferState is invalid...");
                    Validations.check(new OmCode(retCodeConfig.getInvalidCassetteTransferState(),
                            cassetteTransferStateOut,aParentCassetteID.getValue()));
                }
            }



            log.trace("StringUtils.equals(lotOperationEIcheck.toString(),\"1\") : {}", CimStringUtils.equals(lotOperationEIcheck.toString(),"1"));
            if (CimStringUtils.equals(lotOperationEIcheck.toString(),"1")) {
                //-------------------------------
                // Check carrier dispatch status
                //-------------------------------
                log.debug("Check carrier dispatch status");
                Boolean cassetteDispatchStateRetCode = cassetteMethod.cassetteDispatchStateGet(objCommon, aParentCassetteID);
                Validations.check (cassetteDispatchStateRetCode, retCodeConfig.getAlreadyDispatchReservedCassette());
            }
            //-------------------------------
            // Check carrier reserved flag
            //-------------------------------
            // cassette_reservedState_Get
            log.debug(" Check carrier reserved flag");
            Outputs.ObjCassetteReservedStateGetOut cassetteReservedStateRetCode = cassetteMethod.cassetteReservedStateGet(objCommon, aParentCassetteID);
            Validations.check (cassetteReservedStateRetCode.isTransferReserved(), retCodeConfig.getAlreadyDispatchReservedCassette());
        } else {
            // object_Lock
            log.debug("lock lotObject");
            objectLockMethod.objectLock(objCommon, CimLot.class, splitLotNotOnPfReqParams.getParentLotID());
        }
        /*-------------------------------*/
        /*   Check SorterJob existence   */
        /*-------------------------------*/
        //waferSorter_sorterJob_CheckForOperation
        log.debug("Check SorterJob existence");
        List<ObjectIdentifier> dummyCastIDs = new ArrayList<>();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();

        lotIDs.add(splitLotNotOnPfReqParams.getParentLotID());
        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(new Infos.EquipmentLoadPortAttribute());
        objWaferSorterJobCheckForOperation.setCassetteIDList(dummyCastIDs);
        objWaferSorterJobCheckForOperation.setLotIDList(lotIDs);
        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);

        log.debug("wafer sorter Job check for operation");
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);

        /*------------------------------------------------------------------------*/
        /*   Check if the wafers in lot don't have machine container position     */
        /*------------------------------------------------------------------------*/
        log.debug("Check if the wafers in lot don't have machine container position");
        List<Infos.EqpContainerPosition> eqpContainerPositionRetCode = equipmentMethod.equipmentContainerPositionInfoGetByLotDR(objCommon, splitLotNotOnPfReqParams.getParentLotID());
        Validations.check (0 < CimArrayUtils.getSize(eqpContainerPositionRetCode), retCodeConfig.getWaferInLotHaveContainerPosition());
        //----------------------------------------------
        // Check lot contents die, chip, wafer, etc...
        //----------------------------------------------
        log.debug("Check lot contents die, chip, wafer, etc...");
        String lotContents = lotMethod.lotContentsGet(splitLotNotOnPfReqParams.getParentLotID());
        Validations.check (!CimStringUtils.equals(BizConstant.SP_PRODTYPE_WAFER,lotContents)
                && !CimStringUtils.equals(BizConstant.SP_PRODTYPE_DIE,lotContents), retCodeConfig.getInvalidLotContents());

        /*------------------------------------------------------------------------*/
        /*  Check Lot Split Or Merge                                              */
        /*------------------------------------------------------------------------*/
        log.debug("Check Lot Split Or Merge ");
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(splitLotNotOnPfReqParams.getParentLotID());
        this.checkLotSplitOrMerge(objCommon,lotIDLists, BizConstant.CHECK_LOT_ACTION_SPLIT);

        //-------------------------
        // Get lot's all state
        //-------------------------
        log.debug("Get lot's all state");
        Outputs.ObjLotAllStateGetOut lotAllStateGetOutRetCode = lotMethod.lotAllStateGet(objCommon, splitLotNotOnPfReqParams.getParentLotID());
        //-------------------------
        // Check lot hold state
        //-------------------------
        log.debug("Check lot hold state");
        Validations.check (CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotAllStateGetOutRetCode.getHoldState()), retCodeConfig.getConnotSplitHeldlot());
        //-------------------------
        // Check lot finish state
        //-------------------------
        log.debug("Check lot finish state");
        Validations.check (CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_SCRAPPED,lotAllStateGetOutRetCode.getFinishedState())
                || CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_EMPTIED,lotAllStateGetOutRetCode.getFinishedState())
                || CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_STACKED,lotAllStateGetOutRetCode.getFinishedState()), retCodeConfig.getInvalidLotStat());

        //-------------------------
        // Check Bonding Group
        //-------------------------
        //Bonding Group

        log.debug("Check Bonding Group");
        String lotBondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon, splitLotNotOnPfReqParams.getParentLotID());
        log.trace("!StringUtils.isEmpty(lotBondingGroupID) : {}",!CimStringUtils.isEmpty(lotBondingGroupID));
        if (!CimStringUtils.isEmpty(lotBondingGroupID)) {
            throw new ServiceException(new OmCode(retCodeConfig.getLotHasBondingGroup(),splitLotNotOnPfReqParams.getParentLotID().getValue(),lotBondingGroupID));
        }

        //-------------------------
        // Check lot process state
        //-------------------------
        log.debug("Check lot process state");
        Validations.check (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING,lotAllStateGetOutRetCode.getProcessState()), retCodeConfig.getInvalidLotProcessState()
                , ObjectIdentifier.fetchValue(splitLotNotOnPfReqParams.getParentLotID()),lotAllStateGetOutRetCode.getProcessState());
        //-------------------------
        // Check lot Inventory state
        //-------------------------
        log.debug("Check lot Inventory state");
        Validations.check (!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotAllStateGetOutRetCode.getInventoryState()),
                retCodeConfig.getInvalidLotInventoryStat(),splitLotNotOnPfReqParams.getParentLotID(),lotAllStateGetOutRetCode.getInventoryState());
        //----------------------------------
        //  Get InPostProcessFlag of lot
        //----------------------------------
        log.debug("Get InPostProcessFlag of lot");
        Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, splitLotNotOnPfReqParams.getParentLotID());
        //----------------------------------------------
        //  If lot is in post process, returns error
        //----------------------------------------------
        log.debug(" If lot is in post process, returns error");
        List<ObjectIdentifier> userGroupListGetOutRetCode = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
        int i = 0;
        for (i = 0; i < CimArrayUtils.getSize(userGroupListGetOutRetCode); i++) {
        }
        // TODO: 2019/9/27  skip this check
        //Validations.check (i == ArrayUtils.getSize(userGroupListGetOutRetCode.getUserGroupIDList()), retCodeConfig.getLotInPostProcess());
        /*----------------------------------*/
        /*   Check lot's Control Job ID     */
        /*----------------------------------*/
        log.debug("Check lot's Control Job ID");
        ObjectIdentifier jobIDGetOut = lotMethod.lotControlJobIDGet(objCommon, splitLotNotOnPfReqParams.getParentLotID());
        //---------------------------------
        // Get lot's Route ID (Main PD ID)
        //---------------------------------
        log.debug("Get lot's Route ID (Main PD ID)");
        ObjectIdentifier routeID = lotMethod.lotRouteIdGet(objCommon, splitLotNotOnPfReqParams.getParentLotID());
        Validations.check (!ObjectIdentifier.isEmpty(routeID), retCodeConfig.getLotOnRoute());


        //-----------------
        //   Change State
        //-----------------
        log.debug("Change lot split wafer State");
        ObjectIdentifier lotSplitWaferLotNotOnRoute = lotMethod.lotSplitWaferLotNotOnRoute(objCommon, splitLotNotOnPfReqParams);

        //inherit the contamination flag from parent lot
        log.debug("inherit the contamination flag from parent lot");
        contaminationMethod.inheritContaminationFlagFromParentLot(splitLotNotOnPfReqParams.getParentLotID(),lotSplitWaferLotNotOnRoute);

        /*---------------------------------------*/
        /*   Update cassette's MultiLotType      */
        /*---------------------------------------*/
        log.debug("Update cassette's MultiLotType");
        log.trace("!StringUtils.isEmpty(aParentCassetteID) : {}",!ObjectIdentifier.isEmpty(aParentCassetteID));
        if (!ObjectIdentifier.isEmpty(aParentCassetteID)) {
            //----------------------
            // Update control Job Info and
            // Machine cassette info if information exist
            //----------------------
            log.debug("Update control Job Info and Machine cassette info if information exist");
            log.trace("BooleanUtils.isTrue(updateControlJobFlag) : {}", CimBooleanUtils.isTrue(updateControlJobFlag));
            if (CimBooleanUtils.isTrue(updateControlJobFlag)) {
                List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
                cassetteIDs.add(aParentCassetteID);
                controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDs);
            }
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, aParentCassetteID);
        }

        /*------------------------------------------------------------------------*/
        /*   Make History                                                         */
        /*------------------------------------------------------------------------*/
        log.debug(" Make History ");
        ObjectIdentifier aLotID = lotSplitWaferLotNotOnRoute;
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, splitLotNotOnPfReqParams.getParentLotID());
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, aLotID);
        List<Infos.LotWaferAttributes> listRetCode = lotMethod.lotMaterialsGetWafers(objCommon, lotSplitWaferLotNotOnRoute);
        List<Infos.LotWaferAttributes> lotWaferAttributesList = listRetCode;
        log.trace("!ObjectUtils.isEmpty(lotWaferAttributesList) && !ObjectUtils.isEmpty(splitLotNotOnPfReqParams.getChildWaferIDs()) : {}",
                !CimObjectUtils.isEmpty(lotWaferAttributesList) && !CimObjectUtils.isEmpty(splitLotNotOnPfReqParams.getChildWaferIDs()));

        if (!CimObjectUtils.isEmpty(lotWaferAttributesList) && !CimObjectUtils.isEmpty(splitLotNotOnPfReqParams.getChildWaferIDs())){
            Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
            newLotAttributes.setCassetteID(aParentCassetteID);
            List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
            newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
            for (Infos.LotWaferAttributes lotWaferAttribute : lotWaferAttributesList){
                Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
                newWaferAttributes.setNewLotID(lotSplitWaferLotNotOnRoute);
                newWaferAttributes.setNewWaferID(lotWaferAttribute.getWaferID());
                newWaferAttributes.setNewSlotNumber(lotWaferAttribute.getSlotNumber());
                newWaferAttributes.setSourceLotID(splitLotNotOnPfReqParams.getParentLotID());
                newWaferAttributes.setSourceWaferID(lotWaferAttribute.getWaferID());
                newWaferAttributesList.add(newWaferAttributes);
            }
            log.debug("lot wafer move event create");
            eventMethod.lotWaferMoveEventMake(objCommon, newLotAttributes, TransactionIDEnum.SPLIT_WAFER_LOT_NOT_ON_ROUTE_REQ.getValue(), splitLotNotOnPfReqParams.getClaimMemo());
        }
        return lotSplitWaferLotNotOnRoute;
    }


    public Results.SplitLotReqResult sxSplitLotReq(Infos.ObjCommon objCommon, Params.SplitLotReqParams splitLotReqParams) {
        Validations.check (CimArrayUtils.isEmpty(splitLotReqParams.getChildWaferIDs()), retCodeConfig.getInvalidParameter());

        Results.SplitLotReqResult retVal = new Results.SplitLotReqResult();
        //入参校验
        //add je rry 如果传入的wafer有重复返回错误
        List<String> list = new ArrayList<>();
        for (ObjectIdentifier childWaferID :  splitLotReqParams.getChildWaferIDs()) {
            list.add(childWaferID.getValue());
        }
        //利用set不重复判断是否值重复
        boolean isRepeat = list.size() != new HashSet<>(list).size();
        Validations.check (isRepeat, retCodeConfig.getInvalidParameter());
        Validations.check (ObjectIdentifier.isEmptyWithValue(splitLotReqParams.getParentLotID()), retCodeConfig.getInvalidParameter());

        /*------------------------------------------------------------------------*/
        /*   Check Condition                                                      */
        /*------------------------------------------------------------------------*/
        log.debug(" Check Condition  ");
        ObjectIdentifier cassetteOut = null;
        try {
            // not found carrier, not dispose
            cassetteOut = lotMethod.lotCassetteGet(objCommon, splitLotReqParams.getParentLotID());
        } catch (ServiceException e) {
            log.trace("!NumberUtils.eq(e.getCode(), retCodeConfig.getNotFoundCst().getCode()) : {}",!CimNumberUtils.eq(e.getCode(), retCodeConfig.getNotFoundCst().getCode()));
            if (!CimNumberUtils.eq(e.getCode(), retCodeConfig.getNotFoundCst().getCode())) {
                throw e;
            }
        }

        ObjectIdentifier aParentCassetteID = cassetteOut;
        String transferState = null;
        String lotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        boolean updateControlJobFlag = false;
        Long lockMode = 0L;
        log.trace("StringUtils.equals(lotOperationEIcheck,\"0\") && !ObjectUtils.isEmpty(aParentCassetteID) : {}", CimStringUtils.equals(lotOperationEIcheck,"0") && !ObjectIdentifier.isEmpty(aParentCassetteID));
        if (CimStringUtils.equals(lotOperationEIcheck,"0") && !ObjectIdentifier.isEmpty(aParentCassetteID)) {
            //-------------------------------
            // Get carrier transfer status
            //-------------------------------
            log.debug("Get carrier transfer status");
            transferState = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
            /*------------------------------------*/
            /*   Get equipment ID in cassette     */
            /*------------------------------------*/
            log.debug("Get equipment ID in cassette");
            Outputs.ObjCassetteEquipmentIDGetOut objCassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, aParentCassetteID);
            //object_lockMode_Get
            log.debug("get lock for model in object");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(objCassetteEquipmentIDGetOut.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.SPLIT_WAFER_LOT_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            //-------------------------------
            // Get required eqp lock mode
            //-------------------------------
            log.debug("Get required eqp lock mode");
            log.trace("StringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) : {}", CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN));
            if (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                updateControlJobFlag = true;
                log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                    // advanced_object_Lock
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(),
                            new ArrayList<>()));
                }
            }else {
                // object_Lock
                log.debug("lot equipment object");
                objectLockMethod.objectLock(objCommon, CimMachine.class, objCassetteEquipmentIDGetOut.getEquipmentID());
            }
        }
        /*--------------------------------*/
        /*   Lock objects to be updated   */
        /*--------------------------------*/
        // object_Lock
        log.debug("Lock objects to be updated");
        objectLockMethod.objectLock(objCommon, CimCassette.class, aParentCassetteID);
        log.trace("StringUtils.equals(lotOperationEIcheck,\"0\") && !ObjectUtils.isEmpty(aParentCassetteID) : {}", CimStringUtils.equals(lotOperationEIcheck,"0") && !ObjectIdentifier.isEmpty(aParentCassetteID));
        if (CimStringUtils.equals(lotOperationEIcheck,"0") && !ObjectIdentifier.isEmpty(aParentCassetteID)) {
            log.trace("!updateControlJobFlag || lockMode.longValue() != Long.valueOf(\"0\") : {}",!updateControlJobFlag || lockMode.longValue() != Long.valueOf("0"));
            if (!updateControlJobFlag || lockMode.longValue() != Long.valueOf("0")) {
                //---------------------------------
                //   Get cassette's ControlJobID
                //---------------------------------
                log.debug("Get cassette's ControlJobID");
                ObjectIdentifier cassetteControlJobResult = cassetteMethod.cassetteControlJobIDGet(objCommon, aParentCassetteID);
                log.trace("null != cassetteControlJobResult : {}",null != cassetteControlJobResult);
                if (null != cassetteControlJobResult) {
                    updateControlJobFlag = true;
                    log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                    if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                        /*------------------------------*/
                        /*   Lock controljob Object     */
                        /*------------------------------*/
                        // object_Lock
                        log.debug("Lock for controljob Object");
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, cassetteControlJobResult);
                    }
                }
            }
        }
        // object_Lock
        log.debug("lock for lot object");
        objectLockMethod.objectLock(objCommon, CimLot.class, splitLotReqParams.getParentLotID());
        log.trace("!ObjectUtils.equalsWithValue(objCommon.getUser().getUserID(),BizConstant.SP_SORTERWATCHDOG_PERSON) : {}",
                !ObjectIdentifier.equalsWithValue(objCommon.getUser().getUserID(), BizConstant.SP_SORTERWATCHDOG_PERSON));

        if (!ObjectIdentifier.equalsWithValue(objCommon.getUser().getUserID(), BizConstant.SP_SORTERWATCHDOG_PERSON)) {
            /*-------------------------------*/
            /*   Check SorterJob existence   */
            /*-------------------------------*/
            //waferSorter_sorterJob_CheckForOperation
            log.debug("Check SorterJob existence");
            List<ObjectIdentifier> dummyCastIDs = new ArrayList<>();
            List<ObjectIdentifier> lotIDs = new ArrayList<>();

            lotIDs.add(splitLotReqParams.getParentLotID());
            Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
            objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(new Infos.EquipmentLoadPortAttribute());
            objWaferSorterJobCheckForOperation.setCassetteIDList(dummyCastIDs);
            objWaferSorterJobCheckForOperation.setLotIDList(lotIDs);
            objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);

            log.debug("ckeck wafer sorter job for operation");
            if (CimStringUtils.unEqual(TransactionIDEnum.SORT_ACTION_REQ.getValue(), objCommon.getTransactionID())) {
                waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
            }
        }
        /*----------------------------------------------------*/
        /*Currrent Route ID Get For routeID confliction-Check */
        /*----------------------------------------------------*/
        log.debug("Currrent Route ID Get For routeID confliction-Check ");
        ObjectIdentifier  lotCurrentRouteIDGet = lotMethod.lotCurrentRouteIDGet(objCommon, splitLotReqParams.getParentLotID());

        ObjectIdentifier currentRouteID = lotCurrentRouteIDGet;
        /* ------------------------------------------------------------*/
        /* Check routeID confliction                                   */
        /*   return RC_INVALID_BRANCH_ROUTEID,                         */
        /*   when the same routeID is used in the following case       */
        /*       ex) Subroute --> The same SubRoute in the course      */
        /* ------------------------------------------------------------*/
        log.debug("lot_originalRouteList_Get IN ");
        Outputs.ObjLotOriginalRouteListGetOut originalRouteListGetOut = lotMethod.lotOriginalRouteListGet(objCommon, splitLotReqParams.getParentLotID());

        //Check CurrentRoute VS SubRoute
        log.debug("Check CurrentRoute VS SubRoute");
        Validations.check (ObjectIdentifier.equalsWithValue(splitLotReqParams.getSubRouteID(), currentRouteID), retCodeConfig.getInvalidBranchRouteId());
        //Check Afetr Route VS SubRoute
        log.debug("Check Afetr Route VS SubRoute");
        log.trace("!ArrayUtils.isEmpty(originalRouteListGetOut.getOriginalRouteID()) : {}",!CimArrayUtils.isEmpty(originalRouteListGetOut.getOriginalRouteID()));
        if (!CimArrayUtils.isEmpty(originalRouteListGetOut.getOriginalRouteID())) {
            for (int i = 0; i < CimArrayUtils.getSize(originalRouteListGetOut.getOriginalRouteID()); i++) {
                Validations.check (originalRouteListGetOut.getOriginalRouteID().get(i).equals(splitLotReqParams.getSubRouteID()), retCodeConfig.getInvalidBranchRouteId());
            }
        }

        //========================================
        // LotStatusList should be Not Shipped
        //========================================
        log.debug("LotStatusList should be Not Shipped");
        String lotState = lotMethod.lotStateGet(objCommon, splitLotReqParams.getParentLotID());
        Validations.check (CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_EMPTIED,lotState), retCodeConfig.getInvalidLotStat());
        /*------------------------------------------------------------------------*/
        /*   Check Condition                                                      */
        /*------------------------------------------------------------------------*/
        log.debug("Check Condition");
        String lotContents = lotMethod.lotContentsGet(splitLotReqParams.getParentLotID());
        Validations.check (CimStringUtils.equals(BizConstant.SP_PRODTYPE_WAFER,lotContents)
                && CimStringUtils.equals(BizConstant.SP_PRODTYPE_DIE,lotContents), retCodeConfig.getInvalidLotContents());

        String lotHoldState = lotMethod.lotHoldStateGet(objCommon, splitLotReqParams.getParentLotID());

        Validations.check (CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotHoldState), retCodeConfig.getConnotSplitHeldlot());

        String finishState = lotMethod.lotFinishedStateGet(objCommon, splitLotReqParams.getParentLotID());
        Validations.check (CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_SCRAPPED,finishState)
                || CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_EMPTIED,finishState)
                || CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_STACKED,finishState), retCodeConfig.getInvalidLotStat());

        //-------------------------
        // Check Bonding Group
        //-------------------------
        //lot_bondingGroupID_GetDR
        log.debug("Check Bonding Group");
        String lotBondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon, splitLotReqParams.getParentLotID());
        Validations.check(!CimObjectUtils.isEmpty(lotBondingGroupID),retCodeConfig.getLotHasBondingGroup(),splitLotReqParams.getParentLotID().getValue(),lotBondingGroupID);

        String processState = lotMethod.lotProcessStateGet(objCommon, splitLotReqParams.getParentLotID());
        log.trace("StringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING,processState) : {}", CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING,processState));
        if (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING,processState)) {
            Validations.check (!"OEQPW012".equals(objCommon.getTransactionID())
                    && !"OEQPW024".equals(objCommon.getTransactionID()), new OmCode(retCodeConfig.getInvalidLotProcessState(), ObjectIdentifier.fetchValue(splitLotReqParams.getParentLotID()), processState));
        }
        log.debug("Check InPostProcessFlag.");
        //----------------------------------
        //  Get InPostProcessFlag of lot
        //----------------------------------
        log.debug("Get InPostProcessFlag of lot");
        Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, splitLotReqParams.getParentLotID());
        //----------------------------------------------
        //  If lot is in post process, returns error
        //----------------------------------------------
        log.trace("BooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot()) : {}", CimBooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot()));
        if (CimBooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot())) {
            /*---------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            log.debug("Get UserGroupID By UserID");
            List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
            int i ;
            for (i = 0; i < CimArrayUtils.getSize(userGroupIDs); i++) {
            }
            Validations.check (CimArrayUtils.getSize(userGroupIDs) == i, retCodeConfig.getLotInPostProcess());
        }

        /*------------------------------------------------------------------------*/
        /*   Check SubRouteID and return operation                                */
        /*------------------------------------------------------------------------*/
        log.debug("Check SubRouteID and return operation");
        log.trace("!ObjectUtils.isEmpty(splitLotReqParams.getSubRouteID()) : {}",!ObjectIdentifier.isEmpty(splitLotReqParams.getSubRouteID()));
        if (!ObjectIdentifier.isEmpty(splitLotReqParams.getSubRouteID())) {
            //----------------------------------------//
            //    Call lot_inventoryState_Get         //
            //----------------------------------------//
            log.debug("get lot inventory state by lot parent lotID");
            String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, splitLotReqParams.getParentLotID());
            Validations.check (CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotInventoryState),
                    retCodeConfig.getInvalidLotInventoryStat(),ObjectIdentifier.fetchValue(splitLotReqParams.getParentLotID()),
                    lotInventoryState);
            //【step5】process_checkForDynamicRoute
            log.debug("【step5】process_checkForDynamicRoute");
            Outputs.ObjProcessCheckForDynamicRouteOut processCheckForDynamicRouteOut = processMethod.processCheckForDynamicRoute(objCommon, splitLotReqParams.getSubRouteID());
            log.trace("BooleanUtils.isFalse(processCheckForDynamicRouteOut.getDynamicRouteFlag()) : {}", CimBooleanUtils.isFalse(processCheckForDynamicRouteOut.getDynamicRouteFlag()));
            if (CimBooleanUtils.isFalse(processCheckForDynamicRouteOut.getDynamicRouteFlag())) {
                Outputs.ObjProcessGetReturnOperationOut processReturnOperation = processMethod.processGetReturnOperation(objCommon, splitLotReqParams.getParentLotID(), splitLotReqParams.getSubRouteID());
                Validations.check (CimStringUtils.isNotEmpty(splitLotReqParams.getReturnOperationNumber())
                        && !CimStringUtils.equals(processReturnOperation.getOperationNumber(), splitLotReqParams.getReturnOperationNumber()), retCodeConfig.getInvalidParameter());
            }
        }

        log.debug("process check split ");
        processMethod.processCheckSplit(objCommon, splitLotReqParams.getMergedRouteID(), splitLotReqParams.getMergedOperationNumber(), splitLotReqParams.getReturnOperationNumber());

        log.debug("lot future split  hold  check request ");
        lotMethod.lotFutureHoldRequestsCheckSplit(objCommon, splitLotReqParams.getParentLotID(), splitLotReqParams.getMergedOperationNumber(), splitLotReqParams.getReturnOperationNumber());
        /*-------------------------------*/
        /*   Check flowbatch Condition   */
        /*-------------------------------*/
        //lot_flowBatchID_Get
        log.debug("Check FlowBatch Condition  [ParentLot]");
        try {
            ObjectIdentifier retCodeObjectIdentifier = flowBatchMethod.lotFlowBatchIDGet(objCommon, splitLotReqParams.getParentLotID());
        } catch (ServiceException e) {
            log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()));
            log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode()));
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())){
                throw new ServiceException(retCodeConfig.getFlowBatchLimitation());
            }else if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())){
                log.debug("flowBatchMethod.getFlowBatchID() == RC_LOT_BATCH_ID_BLANK");
            }
        }

        log.trace("!\"OEQPW012\".equals(objCommon.getTransactionID())\n" +
                "                && !\"OEQPW024\".equals(objCommon.getTransactionID()) : {}",
                !"OEQPW012".equals(objCommon.getTransactionID())
                        && !"OEQPW024".equals(objCommon.getTransactionID()));

        if (!"OEQPW012".equals(objCommon.getTransactionID())
                && !"OEQPW024".equals(objCommon.getTransactionID())) {
            /*------------------------------------------------------------------------*/
            /*   Check if the wafers in lot don't have machine container position     */
            /*------------------------------------------------------------------------*/
            log.debug("Check if the wafers in lot don't have machine container position");
            log.debug("call equipmentContainerPosition_info_GetByLotDR()");
            List<Infos.EqpContainerPosition> ObjEquipmentContainerPositionInfoGetByLotDROut = equipmentMethod.equipmentContainerPositionInfoGetByLotDR(objCommon, splitLotReqParams.getParentLotID());
            int lenEqpContPos = CimArrayUtils.getSize(ObjEquipmentContainerPositionInfoGetByLotDROut);
            Validations.check (0 < lenEqpContPos, retCodeConfig.getWaferInLotHaveContainerPosition());
        }
        /*----------------------------------*/
        /*   Check lot's Control Job ID     */
        /*----------------------------------*/
        log.debug("Check lot's Control Job ID");
        ObjectIdentifier jobIDGetOut = lotMethod.lotControlJobIDGet(objCommon, splitLotReqParams.getParentLotID());
        log.trace("ObjectUtils.isEmpty(jobIDGetOut) : {}", ObjectIdentifier.isEmpty(jobIDGetOut));
        if (ObjectIdentifier.isEmpty(jobIDGetOut)) {
        } else {
            log.trace("!\"OEQPW012\".equals(objCommon.getTransactionID())\n" +
                    "                    && !\"OEQPW024\".equals(objCommon.getTransactionID()) : {}",
                    !"OEQPW012".equals(objCommon.getTransactionID())
                            && !"OEQPW024".equals(objCommon.getTransactionID()));

            if (!"OEQPW012".equals(objCommon.getTransactionID())
                    && !"OEQPW024".equals(objCommon.getTransactionID())) {
                throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(), ObjectIdentifier.fetchValue(splitLotReqParams.getParentLotID()), ObjectIdentifier.fetchValue(jobIDGetOut)));
            }
        }
        //-------------------------------
        // Check carrier transfer status
        //-------------------------------
        log.debug(" Check carrier transfer status");
        String cassetteTransferStateGetOut = null;
        log.trace("!ObjectUtils.isEmpty(aParentCassetteID) : {}",!ObjectIdentifier.isEmpty(aParentCassetteID));
        if (!ObjectIdentifier.isEmpty(aParentCassetteID)) {
            log.trace("StringUtils.equals(lotOperationEIcheck,\"1\")\n" +
                    "                    || (StringUtils.equals(lotOperationEIcheck,\"0\") && !StringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,transferState)): {}",
                    CimStringUtils.equals(lotOperationEIcheck,"1")
                            || (CimStringUtils.equals(lotOperationEIcheck,"0") && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,transferState)));

            if (CimStringUtils.equals(lotOperationEIcheck,"1")
                    || (CimStringUtils.equals(lotOperationEIcheck,"0") && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,transferState))) {
                cassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
                log.trace("StringUtils.equals(lotOperationEIcheck,\"0\") : {}", CimStringUtils.equals(lotOperationEIcheck,"0"));
                if (CimStringUtils.equals(lotOperationEIcheck,"0")) {
                    Validations.check (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateGetOut), retCodeConfig.getChangedToEiByOtherOperation());
                } else {
                    Validations.check ((CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT,cassetteTransferStateGetOut))
                                    || (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateGetOut)
                                    && !"OEQPW006".equals(objCommon.getTransactionID())
                                    && !"OEQPW012".equals(objCommon.getTransactionID())
                                    && !"OEQPW024".equals(objCommon.getTransactionID())
                                    && !"OEQPW008".equals(objCommon.getTransactionID())
                                    //TODO: huzi 2021/7/16 6:51 下午 actionReq的txID不进行检查
                                    && CimStringUtils.unEqual(TransactionIDEnum.SORT_ACTION_REQ.getValue()
                            , objCommon.getTransactionID())), retCodeConfig.getInvalidCassetteTransferState(),
                            cassetteTransferStateGetOut,aParentCassetteID);
                }
            }
            log.trace("StringUtils.equals(lotOperationEIcheck,\"0\") : {}", CimStringUtils.equals(lotOperationEIcheck,"0"));
            if (CimStringUtils.equals(lotOperationEIcheck,"0")) {
                cassetteTransferStateGetOut = transferState;
            }
        }

        log.trace("StringUtils.equals(lotOperationEIcheck,\"1\" : {}", CimStringUtils.equals(lotOperationEIcheck,"1"));
        if (CimStringUtils.equals(lotOperationEIcheck,"1")) {
            //-------------------------------
            // Check carrier dispatch status
            //-------------------------------
            log.debug("Check carrier dispatch status");
            log.trace("!ObjectUtils.isEmpty(aParentCassetteID) : {}",!ObjectIdentifier.isEmpty(aParentCassetteID));
            if (!ObjectIdentifier.isEmpty(aParentCassetteID)) {
                Boolean cassetteDispatchStateRetCode = cassetteMethod.cassetteDispatchStateGet(objCommon, aParentCassetteID);
                Validations.check (cassetteDispatchStateRetCode, retCodeConfig.getAlreadyDispatchReservedCassette());
            }
        }
        //-------------------------------
        // Check carrier reserved flag
        //-------------------------------
        log.debug("Check carrier reserved flag");
        log.trace("!ObjectUtils.isEmpty(aParentCassetteID) : {}",!ObjectIdentifier.isEmpty(aParentCassetteID));
        if (!ObjectIdentifier.isEmpty(aParentCassetteID)) {
            Outputs.ObjCassetteReservedStateGetOut cassetteReservedStateRetCode = cassetteMethod.cassetteReservedStateGet(objCommon, aParentCassetteID);
            Validations.check (cassetteReservedStateRetCode.isTransferReserved(), retCodeConfig.getAlreadyDispatchReservedCassette());
        }

        /*------------------------------------------------------------------------*/
        /*  Check Lot Split Or Merge                                  */
        /*------------------------------------------------------------------------*/
        log.debug("Check Lot Split Or Merge");
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(splitLotReqParams.getParentLotID());
        this.checkLotSplitOrMerge(objCommon,lotIDLists, BizConstant.CHECK_LOT_ACTION_SPLIT);

        /*------------------------------------------------------------------------*/
        /*   Change State                                                         */
        /*------------------------------------------------------------------------*/
        log.debug("Change State ");
        ObjectIdentifier lotSplitWaferLotOut = lotMethod.lotSplitWaferLot(objCommon, splitLotReqParams.getParentLotID(), splitLotReqParams.getChildWaferIDs());

        //inherit the contamination flag from parent lot
        log.debug("inherit the contamination flag from parent lot");
        contaminationMethod.inheritContaminationFlagFromParentLot(splitLotReqParams.getParentLotID(),lotSplitWaferLotOut);

        ObjectIdentifier lotID = lotSplitWaferLotOut;
        Inputs.OldCurrentPOData processBranchRouteResult = null;
        log.trace("BooleanUtils.isTrue(splitLotReqParams.getBranchingRouteSpecifyFlag()) : {}", CimBooleanUtils.isTrue(splitLotReqParams.getBranchingRouteSpecifyFlag()));
        if (CimBooleanUtils.isTrue(splitLotReqParams.getBranchingRouteSpecifyFlag())) {
            //  qTime_CheckConditionForReplaceTarget
            log.debug("ckeck for condition for repalce target lot");
            qTimeMethod.qTimeCheckConditionForReplaceTarget(objCommon, lotID);

            //  process_BranchRoute
            log.debug("process branch route");
            processBranchRouteResult = processMethod.processBranchRoute(objCommon, lotID, splitLotReqParams.getSubRouteID(), splitLotReqParams.getReturnOperationNumber());
            //check if the child lot match the step contamination requirement
            log.debug("check if the child lot match the step contamination requirement");
            contaminationMethod.lotCheckContaminationLevelStepOut(objCommon,lotID);
            //--------------------------------------------------------------------------------------------------
            // Replace Target Operation for sub route
            //--------------------------------------------------------------------------------------------------
            // qTime_targetOpe_Replace
            log.debug("Replace Target Operation for sub route");
            Infos.QTimeTargetOpeReplaceIn inputParams = new Infos.QTimeTargetOpeReplaceIn();
            inputParams.setLotID(lotID);
            inputParams.setSpecificControlFlag(false);
            qTimeMethod.qTimeTargetOpeReplace(objCommon, inputParams);

            minQTimeMethod.checkTargetOpeReplace(objCommon, lotID);
        }

        log.trace("BooleanUtils.isTrue(splitLotReqParams.getFutureMergeFlag()) : {}", CimBooleanUtils.isTrue(splitLotReqParams.getFutureMergeFlag()));
        if (CimBooleanUtils.isTrue(splitLotReqParams.getFutureMergeFlag())) {
            Params.FutureHoldReqParams params = new Params.FutureHoldReqParams();
            params.setHoldType(BizConstant.SP_HOLDTYPE_MERGEHOLD);
            params.setLotID(splitLotReqParams.getParentLotID());params.setRouteID(splitLotReqParams.getMergedRouteID());
            params.setOperationNumber(splitLotReqParams.getMergedOperationNumber());
            params.setReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGEHOLD));
            log.debug("BooleanUtils.isTrue(splitLotReqParams.getCombineHold()) : {}", CimBooleanUtils.isTrue(splitLotReqParams.getCombineHold()));
            if (CimBooleanUtils.isTrue(splitLotReqParams.getCombineHold())) {
                params.setReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_COMBINEHOLD));
            }
            params.setRelatedLotID(lotID);
            params.setPostFlag(false);
            params.setSingleTriggerFlag(false);
            try {
                processControlService.sxFutureHoldReq(objCommon, params);
            } catch (ServiceException e) {
                log.trace("!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()));
                if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode())){
                    throw e;
                }
            }
            Params.FutureHoldReqParams params1 = new Params.FutureHoldReqParams();
            params1.setHoldType(BizConstant.SP_HOLDTYPE_MERGEHOLD);
            params1.setLotID(lotID);
            params1.setRouteID(splitLotReqParams.getMergedRouteID());
            params1.setOperationNumber(splitLotReqParams.getMergedOperationNumber());
            params1.setReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGEHOLD));
            log.trace("BooleanUtils.isTrue(splitLotReqParams.getCombineHold()) : {}", CimBooleanUtils.isTrue(splitLotReqParams.getCombineHold()));
            if (CimBooleanUtils.isTrue(splitLotReqParams.getCombineHold())) {
                params1.setReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_COMBINEHOLD));
            }
            params1.setRelatedLotID(splitLotReqParams.getParentLotID());
            params1.setPostFlag(false);
            params1.setSingleTriggerFlag(false);
            try {
                processControlService.sxFutureHoldReq(objCommon, params1);
            } catch (ServiceException e) {
                log.trace("!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()));
                if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode())){
                    throw e;
                }
            }
        }
        Outputs.ObjProcessCompareCurrentOut objProcessCompareCurrentOut = null;
        Boolean CurrentToperationSame=false;
        try{
            objProcessCompareCurrentOut = processMethod.processCompareCurrent(objCommon, splitLotReqParams.getParentLotID(),
                    splitLotReqParams.getMergedRouteID(),
                    splitLotReqParams.getMergedOperationNumber());
        }catch (ServiceException ex){
            log.trace("Validations.isEquals(retCodeConfig.getCurrentToperationSame(),ex.getCode()) : {}",Validations.isEquals(retCodeConfig.getCurrentToperationSame(),ex.getCode()));
            if (Validations.isEquals(retCodeConfig.getCurrentToperationSame(),ex.getCode())){
                CurrentToperationSame=true;
            }
        }

        log.trace("BooleanUtils.isTrue(CurrentToperationSame) : {}", CimBooleanUtils.isTrue(CurrentToperationSame));
        if (CimBooleanUtils.isTrue(CurrentToperationSame)) {
            List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_MERGEHOLD);
            lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGEHOLD));
            lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            lotHoldReq.setRouteID(splitLotReqParams.getMergedRouteID());
            lotHoldReq.setOperationNumber(splitLotReqParams.getMergedOperationNumber());
            lotHoldReq.setRelatedLotID(lotID);
            holdReqList.add(lotHoldReq);
            sxHoldLotReq(objCommon,
                    splitLotReqParams.getParentLotID(), holdReqList);
            log.trace("BooleanUtils.isFalse(splitLotReqParams.getBranchingRouteSpecifyFlag()) : {}", CimBooleanUtils.isFalse(splitLotReqParams.getBranchingRouteSpecifyFlag()));
            if (CimBooleanUtils.isFalse(splitLotReqParams.getBranchingRouteSpecifyFlag())) {
                List<Infos.LotHoldReq> holdReqList1 = new ArrayList<>();
                Infos.LotHoldReq lotHoldReq1 = new Infos.LotHoldReq();
                lotHoldReq1.setHoldType(BizConstant.SP_HOLDTYPE_MERGEHOLD);
                lotHoldReq1.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGEHOLD));
                lotHoldReq1.setHoldUserID(objCommon.getUser().getUserID());
                lotHoldReq1.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq1.setRouteID(splitLotReqParams.getMergedRouteID());
                lotHoldReq1.setOperationNumber(splitLotReqParams.getMergedOperationNumber());
                lotHoldReq1.setRelatedLotID(splitLotReqParams.getParentLotID());
                holdReqList1.add(lotHoldReq1);
                sxHoldLotReq(objCommon, lotID, holdReqList1);
            }
        }
        /*---------------------------------------*/
        /*   Update cassette's MultiLotType      */
        /*---------------------------------------*/
        log.debug("Update cassette's MultiLotType");
        log.trace("!ObjectIdentifier.isEmpty(aParentCassetteID) : {}",!ObjectIdentifier.isEmpty(aParentCassetteID));
        if (!ObjectIdentifier.isEmpty(aParentCassetteID)) {
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, aParentCassetteID);
        }
        //--------------------------------------------------------------------------------------------------
        // UpDate RequiredCassetteCategory
        //--------------------------------------------------------------------------------------------------
        log.debug("UpDate RequiredCassetteCategory");
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotID);
        //--------------------------------------------------------
        //   Maintain Loaded cassette on Eqp Information
        //--------------------------------------------------------
        log.debug("Maintain Loaded cassette on Eqp Information");
        log.trace("!ObjectUtils.isEmpty(jobIDGetOut) : {}",!ObjectIdentifier.isEmpty(jobIDGetOut));
        if (!ObjectIdentifier.isEmpty(jobIDGetOut)) {
            // ------------ WARNING --------------------
            // it is assumed that this is called from Partial Operation Completion.
            // So the eqp object is considered locked before this method is called
            // ------------ WARNING --------------------
            log.debug("get control job attribute info by jobID");
            Outputs.ControlJobAttributeInfo objControlJobAttributeInfoGetOut = controlJobMethod.controlJobAttributeInfoGet(objCommon,jobIDGetOut);

            // Update eqp related information
            log.debug("Update eqp related information");
            Inputs.EquipmentRelatedInfoUpdateForLotSplitOnEqpIn equipmentRelatedInfoUpdateForLotSplitOnEqpIn = new Inputs.EquipmentRelatedInfoUpdateForLotSplitOnEqpIn();
            equipmentRelatedInfoUpdateForLotSplitOnEqpIn.setEquipmentID(objControlJobAttributeInfoGetOut.getMachineID());
            equipmentRelatedInfoUpdateForLotSplitOnEqpIn.setParentLotID(splitLotReqParams.getParentLotID());
            equipmentRelatedInfoUpdateForLotSplitOnEqpIn.setChildLotID(lotID);
            equipmentMethod.equipmentRelatedInfoUpdateForLotSplitOnEqp(objCommon, equipmentRelatedInfoUpdateForLotSplitOnEqpIn);

        }
        log.trace("!ObjectUtils.isEmpty(aParentCassetteID) : {}",!ObjectIdentifier.isEmpty(aParentCassetteID));
        if (!ObjectIdentifier.isEmpty(aParentCassetteID)) {
            log.trace("updateControlJobFlag || StringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateGetOut) : {}",updateControlJobFlag || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateGetOut));
            if (updateControlJobFlag || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateGetOut)) {
                List<ObjectIdentifier> cassettes = new ArrayList<>();
                cassettes.add(aParentCassetteID);
                controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassettes);
            }
        }

        //----------------------------------------//
        //    Call lot_inventoryState_Get         //
        //----------------------------------------//
        log.debug("get lot inventory state");
        String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, lotID);
        log.trace("StringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotInventoryState) : {}", CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotInventoryState));
        if (CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotInventoryState)){
            log.debug("Lot's inventory state is 'InBank'.");
        }else  {
            log.debug("Lot's inventory state is not 'InBank'. So, txProcessHoldDoActionReq() is called.");
            //------------------------------------//
            //     Process Hold for Child lot     //
            //------------------------------------//
            log.debug(" Process Hold for Child lot");
            log.debug("Call txProcessHoldDoActionReq()");
            //txProcessHoldDoActionReq
            processControlService.sxProcessHoldDoActionReq(objCommon, lotID, splitLotReqParams.getClaimMemo());
        }

        /*------------------------------------------------------------------------*/
        /*   当actioCode=separate此时的TransactionID由替OSRTW015换成Split->SPLIT_WAFER_LOT_REQ("OLOTW030")
        /*   为了增加Split的操作记录
        /*------------------------------------------------------------------------*/
        if(objCommon.getTransactionID().equals(TransactionIDEnum.SORT_ACTION_REQ.getValue())) {
            objCommon.setTransactionID(TransactionIDEnum.SPLIT_WAFER_LOT_REQ.getValue());
        }

        /*------------------------------------------------------------------------*/
        /*   Make History                                                         */
        /*------------------------------------------------------------------------*/
        log.debug("Make History ");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, splitLotReqParams.getParentLotID());
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, lotID);
        List<Infos.LotWaferAttributes> lotMaterialsGetWafers = lotMethod.lotMaterialsGetWafers(objCommon, lotID);
        //lotWaferMoveEvent_Make
        log.debug("make event info for lot wafer move");
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        newLotAttributes.setCassetteID(cassetteOut);
        List<Infos.LotWaferAttributes> lotWaferAttributesList = lotMaterialsGetWafers;
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        log.trace("!ObjectUtils.isEmpty(lotWaferAttributesList) : {}",!CimObjectUtils.isEmpty(lotWaferAttributesList));
        if (!CimObjectUtils.isEmpty(lotWaferAttributesList)){
            for (Infos.LotWaferAttributes lotWaferAttributes : lotWaferAttributesList){
                Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
                newWaferAttributes.setNewLotID(lotSplitWaferLotOut);
                newWaferAttributes.setNewWaferID(lotWaferAttributes.getWaferID());
                newWaferAttributes.setNewSlotNumber(lotWaferAttributes.getSlotNumber());
                newWaferAttributes.setSourceLotID(splitLotReqParams.getParentLotID());
                newWaferAttributes.setSourceWaferID(lotWaferAttributes.getWaferID());
                newWaferAttributesList.add(newWaferAttributes);
            }
            log.debug("lot wafer move event create ");
            eventMethod.lotWaferMoveEventMake(objCommon, newLotAttributes, TransactionIDEnum.SPLIT_WAFER_LOT_REQ.getValue(), splitLotReqParams.getClaimMemo());
        }

        //lotOperationMoveEvent_MakeBranch
        log.debug("lot operateion move envet make branch");
        log.trace("BooleanUtils.isTrue(splitLotReqParams.getBranchingRouteSpecifyFlag()) : {}", CimBooleanUtils.isTrue(splitLotReqParams.getBranchingRouteSpecifyFlag()));
        if (CimBooleanUtils.isTrue(splitLotReqParams.getBranchingRouteSpecifyFlag())){
            Inputs.LotOperationMoveEventMakeBranchParams lotOperationMoveEventMakeBranchParams = new Inputs.LotOperationMoveEventMakeBranchParams();
            lotOperationMoveEventMakeBranchParams.setTransactionID(TransactionIDEnum.SPLIT_WAFER_LOT_REQ.getValue());
            lotOperationMoveEventMakeBranchParams.setLotID(lotID);
            lotOperationMoveEventMakeBranchParams.setOldCurrentPOData(processBranchRouteResult);
            lotOperationMoveEventMakeBranchParams.setClaimMemo(splitLotReqParams.getClaimMemo());
            eventMethod.lotOperationMoveEventMakeBranch(objCommon, lotOperationMoveEventMakeBranchParams);
        }
        retVal.setChildLotID(lotID);
        return retVal;
    }


    public Results.SplitLotReqResult sxSplitLotWithHoldReleaseReq(Infos.ObjCommon objCommon, Params.SplitLotWithHoldReleaseReqParams splitLotWithHoldReleaseReqParams) {
        /*------------------------------------------------------------------------*/
        /*   Get lot / cassette connection                                        */
        /*------------------------------------------------------------------------*/
        log.debug("Get lot / cassette connection ");
        ObjectIdentifier objGetLotCassetteOut = null;
        try {
            objGetLotCassetteOut = lotMethod.lotCassetteGet(objCommon, splitLotWithHoldReleaseReqParams.getParentLotID());
        } catch (ServiceException e) {
            log.trace("!Validations.isEquals(retCodeConfig.getNotFoundCassette() ,e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotFoundCassette() ,e.getCode()));
            if (!Validations.isEquals(retCodeConfig.getNotFoundCassette() ,e.getCode())) {
                throw e;
            }
        }

        ObjectIdentifier aCassetteID = objGetLotCassetteOut;
        /*--------------------------------*/
        /*   Lock objects to be updated   */
        /*--------------------------------*/
        // object_Lock
        log.debug("Lock objects to be updated");
        objectLockMethod.objectLock(objCommon, CimCassette.class, aCassetteID);

        // object_Lock
        objectLockMethod.objectLock(objCommon, CimLot.class, splitLotWithHoldReleaseReqParams.getParentLotID());
        /*-------------------------------*/
        /*   Check SorterJob existence   */
        /*-------------------------------*/
        //waferSorter_sorterJob_CheckForOperation
        log.debug("Check SorterJob existence");
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> dummyCastIDs = new ArrayList<>();
        lotIDs.add(splitLotWithHoldReleaseReqParams.getParentLotID());
        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        objWaferSorterJobCheckForOperation.setLotIDList(lotIDs);
        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);
        objWaferSorterJobCheckForOperation.setCassetteIDList(dummyCastIDs);

        //step1 - waferSorter_sorterJob_CheckForOperation
        log.debug("step1 - waferSorter_sorterJob_CheckForOperation");
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
        /*------------------------------------------------------------------------*/
        /*   Check Finished State of lot                                          */
        /*------------------------------------------------------------------------*/
        log.debug("Check Finished State of lot");
        String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon, splitLotWithHoldReleaseReqParams.getParentLotID());
        Validations.check (CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_STACKED,lotFinishedState), retCodeConfig.getInvalidLotFinishStat());
        //-------------------------
        // Check Bonding Group
        //-------------------------
        //Bonding Group
        log.debug("Check Bonding Group");
        String groupIDGetDROut = lotMethod.lotBondingGroupIDGetDR(objCommon, splitLotWithHoldReleaseReqParams.getParentLotID());
        log.trace("StringUtils.length(groupIDGetDROut) > 0 : {}", CimStringUtils.length(groupIDGetDROut) > 0);
        if (CimStringUtils.length(groupIDGetDROut) > 0){
            throw new ServiceException(new OmCode(retCodeConfig.getLotHasBondingGroup(),splitLotWithHoldReleaseReqParams.getParentLotID().getValue(),groupIDGetDROut));
        }
        /*------------------------------------------------------------------------*/
        /*   Check if the wafers in lot don't have machine container position     */
        /*------------------------------------------------------------------------*/
        log.debug("Check if the wafers in lot don't have machine container position");
        List<Infos.EqpContainerPosition> parentLotEquipmentContainerPositionInfoGetByLotDROut = equipmentMethod.equipmentContainerPositionInfoGetByLotDR(objCommon, splitLotWithHoldReleaseReqParams.getParentLotID());
        Validations.check (0 < parentLotEquipmentContainerPositionInfoGetByLotDROut.size(), retCodeConfig.getWaferInLotHaveContainerPosition());

        /*------------------------------------------------------------------------*/
        /*  Check Lot Split Or Merge                                              */
        /*------------------------------------------------------------------------*/
        log.debug("Check Lot Split Or Merge");
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(splitLotWithHoldReleaseReqParams.getParentLotID());
        this.checkLotSplitOrMerge(objCommon,lotIDLists, BizConstant.CHECK_LOT_ACTION_SPLIT);

        /*----------------------------------------*/
        /*    Call lot_inventoryState_Get         */
        /*----------------------------------------*/
        log.debug("get lot inventory state");
        String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, splitLotWithHoldReleaseReqParams.getParentLotID());
        /*----------------------------------------*/
        /*    Call lot_inventoryState_Get         */
        /*----------------------------------------*/
        log.debug("StringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotInventoryState) : {}", CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotInventoryState));
        if (CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotInventoryState)) {
            /*----------------------------------------*/
            /*    Call txHoldLotReleaseInBankReq        */
            /*----------------------------------------*/
            log.debug("Call txHoldLotReleaseInBankReq ");
            List<ObjectIdentifier> lots = new ArrayList<>();
            lots.add(splitLotWithHoldReleaseReqParams.getParentLotID());
            Params.HoldLotReleaseInBankReqParams holdLotReleaseInBankReqParams = new Params.HoldLotReleaseInBankReqParams();
            holdLotReleaseInBankReqParams.setLotIDs(lots);
            holdLotReleaseInBankReqParams.setReasonCodeID(splitLotWithHoldReleaseReqParams.getReleaseReasonCodeID());
            holdLotReleaseInBankReqParams.setClaimMemo(splitLotWithHoldReleaseReqParams.getClaimMemo());
            Integer lotSeqLen = 0;
            log.debug("sent lot hold release in bank request");
            List<Infos.HoldHistory> releaseBankLotReqResultRetCode = bankService.sxHoldLotReleaseInBankReq(objCommon, lotSeqLen, holdLotReleaseInBankReqParams);

        } else {
            Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
            holdLotReleaseReqParams.setUser(splitLotWithHoldReleaseReqParams.getUser());
            holdLotReleaseReqParams.setLotID(splitLotWithHoldReleaseReqParams.getParentLotID());
            holdLotReleaseReqParams.setReleaseReasonCodeID(splitLotWithHoldReleaseReqParams.getReleaseReasonCodeID());
            holdLotReleaseReqParams.setHoldReqList(splitLotWithHoldReleaseReqParams.getStrLotHoldReleaseReqList());
            log.debug("sent lot hold release reuqest");
            sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
        }
        /*-----------------------------------------*/
        /*    Call txSplitLotReq              */
        /*-----------------------------------------*/
        log.debug("Call txSplitLotReq");
        Params.SplitLotReqParams splitLotReqParams = new Params.SplitLotReqParams();
        splitLotReqParams.setUser(splitLotWithHoldReleaseReqParams.getUser());
        splitLotReqParams.setParentLotID(splitLotWithHoldReleaseReqParams.getParentLotID());
        splitLotReqParams.setChildWaferIDs(splitLotWithHoldReleaseReqParams.getChildWaferIDs());
        splitLotReqParams.setFutureMergeFlag(splitLotWithHoldReleaseReqParams.getFutureMergeFlag());
        splitLotReqParams.setMergedRouteID(splitLotWithHoldReleaseReqParams.getMergedRouteID());
        splitLotReqParams.setMergedOperationNumber(splitLotWithHoldReleaseReqParams.getMergedOperationNumber());
        splitLotReqParams.setBranchingRouteSpecifyFlag(splitLotWithHoldReleaseReqParams.getBranchingRouteSpecifyFlag());
        splitLotReqParams.setSubRouteID(splitLotWithHoldReleaseReqParams.getSubRouteID());
        splitLotReqParams.setReturnOperationNumber(splitLotWithHoldReleaseReqParams.getReturnOperationNumber());
        splitLotReqParams.setClaimMemo(splitLotWithHoldReleaseReqParams.getClaimMemo());
        return sxSplitLotReq(objCommon, splitLotReqParams);
    }

    @SuppressWarnings("deprecation")
    public Results.SplitLotReqResult sxSplitLotWithoutHoldReleaseReq(Infos.ObjCommon objCommon, Params.SplitLotWithoutHoldReleaseReqParams splitLotWithoutHoldReleaseReqParams) {
        /*------------------------------------------------------------------------*/
        /*   Check lot's Backup State                                             */
        /*------------------------------------------------------------------------*/
        Results.SplitLotReqResult retVal = new Results.SplitLotReqResult();
        log.debug("Check lot's Backup State");
        Infos.LotBackupInfo lotBackupInfoOut = lotMethod.lotBackupInfoGet(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID());

        Infos.LotBackupInfo lotBackupInfo = lotBackupInfoOut;
        Validations.check(null != lotBackupInfo && CimBooleanUtils.isTrue(lotBackupInfo.getBackupProcessingFlag()), retCodeConfig.getLotInBackupoperation());
        //===========================================================================
        // Lock for objects
        //===========================================================================
        log.debug(" Lock cassette objects ");
        ObjectIdentifier objGetLotCassetteOut = lotMethod.lotCassetteGet(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID());

        ObjectIdentifier cassetteID = objGetLotCassetteOut;
        String tmpLotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        tmpLotOperationEIcheck = null == tmpLotOperationEIcheck ? "0" : tmpLotOperationEIcheck;
        Integer lotOperationEIcheck = Integer.valueOf(tmpLotOperationEIcheck);
        Boolean updateControlJobFlag = false;
        Long lockMode = 0L;
        String objCassetteTransferStateGetOut = null;
        log.trace("StringUtils.equals(\"0\",lotOperationEIcheck.toString())\n" +
                "                && StringUtils.isEmpty(cassetteID.getValue()) : {}",
                CimStringUtils.equals("0",lotOperationEIcheck.toString())
                        && CimStringUtils.isEmpty(cassetteID.getValue()));

        if (CimStringUtils.equals("0",lotOperationEIcheck.toString())
                && CimStringUtils.isEmpty(cassetteID.getValue())) {
            //-------------------------------
            // Get carrier transfer status
            //-------------------------------
            log.debug("Get carrier transfer status");
            objCassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
            /*------------------------------------*/
            /*   Get eqp ID in cassette     */
            /*------------------------------------*/
            log.debug("Get eqp ID in cassette");
            Outputs.ObjCassetteEquipmentIDGetOut cassetteEqpOut  = cassetteMethod.cassetteEquipmentIDGet(objCommon, cassetteID);
            //-------------------------------
            // Get required eqp lock mode
            //-------------------------------
            // object_lockMode_Get
            log.debug("Get required eqp lock mode");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(cassetteEqpOut.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.SPLIT_WAFER_LOT_WITH_OUT_HOLD_RELEASE_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            log.debug("StringUtils.equals(objCassetteTransferStateGetOut, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) : {}", CimStringUtils.equals(objCassetteTransferStateGetOut, BizConstant.SP_TRANSSTATE_EQUIPMENTIN));
            if (CimStringUtils.equals(objCassetteTransferStateGetOut, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                updateControlJobFlag = true;
                log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                    // advanced_object_Lock
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(cassetteEqpOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                    // advanced_object_Lock
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(cassetteEqpOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                            (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, Collections.singletonList(cassetteID.getValue())));
                } else {
                    objectLockMethod.objectLock(objCommon, CimMachine.class, cassetteEqpOut.getEquipmentID());
                }
            }
        }
        // object_Lock
        log.debug("lock cassette object");
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
        //----- Lock for cassette -------//
        // object_Lock
        log.trace("StringUtils.equals(\"0\",lotOperationEIcheck.toString()) && !StringUtils.isEmpty(cassetteID.getValue()) : {}", CimStringUtils.equals("0",lotOperationEIcheck.toString()) && !CimStringUtils.isEmpty(cassetteID.getValue()));
        if (CimStringUtils.equals("0",lotOperationEIcheck.toString()) && !CimStringUtils.isEmpty(cassetteID.getValue())) {
            log.trace("!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
            if (!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                //---------------------------------
                //   Get cassette's ControlJobID
                //---------------------------------
                log.debug("Get cassette's ControlJobID");
                ObjectIdentifier controlJobRetCode = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
                log.trace("!ObjectUtils.isEmptyWithValue(controlJobRetCode) : {}",!CimObjectUtils.isEmptyWithValue(controlJobRetCode));
                if (!CimObjectUtils.isEmptyWithValue(controlJobRetCode)) {
                    updateControlJobFlag = true;
                    log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                    if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                        // object_Lock
                        log.debug("lcok control job objecgt");
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobRetCode);
                    }
                }
            }
        }
        // object_Lock
        log.debug("lock lot object by parent lot ID");
        objectLockMethod.objectLock(objCommon, CimLot.class, splitLotWithoutHoldReleaseReqParams.getParentLotID());
        //waferSorter_sorterJob_CheckForOperation
        log.trace("!ObjectUtils.equalsWithValue(BizConstant.SP_SORTERWATCHDOG_PERSON,objCommon.getUser().getUserID().getValue()) : {}",
                !CimObjectUtils.equalsWithValue(BizConstant.SP_SORTERWATCHDOG_PERSON,objCommon.getUser().getUserID().getValue()));

        if (!CimObjectUtils.equalsWithValue(BizConstant.SP_SORTERWATCHDOG_PERSON,objCommon.getUser().getUserID().getValue())){
            /*-------------------------------*/
            /*   Check SorterJob existence   */
            /*-------------------------------*/
            log.debug("Check SorterJob existence");
            List<ObjectIdentifier> lotIDs = new ArrayList<>();
            List<ObjectIdentifier> dummyCastIDs = new ArrayList<>();
            lotIDs.add(splitLotWithoutHoldReleaseReqParams.getParentLotID());
            Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
            objWaferSorterJobCheckForOperation.setLotIDList(lotIDs);
            objWaferSorterJobCheckForOperation.setCassetteIDList(dummyCastIDs);
            objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);
            objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(new Infos.EquipmentLoadPortAttribute());
            log.debug("Check wafer Sorter SorterJob For Operation");
            waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
        }

        //===========================================================================
        // Check for conditions
        //===========================================================================
        /*------------------------------------------------------------------------*/
        /*   Check SubRouteID and return operation                                */
        /*------------------------------------------------------------------------*/
        log.debug("Check for conditions");
        log.debug("Check SubRouteID and return operation");
        ObjectIdentifier subRouteID = splitLotWithoutHoldReleaseReqParams.getSubRouteID();
        log.trace("!ObjectUtils.isEmpty(subRouteID) : {}",!CimObjectUtils.isEmpty(subRouteID));
        if (!CimObjectUtils.isEmpty(subRouteID)) {
            Outputs.ObjProcessCheckForDynamicRouteOut checkForDynamicRouteOutRetCode = processMethod.processCheckForDynamicRoute(objCommon, subRouteID);
            log.trace("BooleanUtils.isFalse(checkForDynamicRouteOutRetCode.getDynamicRouteFlag()) : {}", CimBooleanUtils.isFalse(checkForDynamicRouteOutRetCode.getDynamicRouteFlag()));
            if (CimBooleanUtils.isFalse(checkForDynamicRouteOutRetCode.getDynamicRouteFlag())) {
                Outputs.ObjProcessGetReturnOperationOut returnOperationOutRetCode = processMethod.processGetReturnOperation(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID(), splitLotWithoutHoldReleaseReqParams.getSubRouteID());
                Validations.check (!CimStringUtils.isEmpty(splitLotWithoutHoldReleaseReqParams.getReturnOperationNumber()) && !returnOperationOutRetCode.getOperationNumber().equals(splitLotWithoutHoldReleaseReqParams.getReturnOperationNumber()), retCodeConfig.getInvalidInputParam());
            }
        }
        //-----------------------------------------------------------
        // Check for combination of Return Point and Merge Point
        //-----------------------------------------------------------
        log.debug("Check for combination of Return Point and Merge Point");
        processMethod.processCheckSplit(objCommon, splitLotWithoutHoldReleaseReqParams.getMergedRouteID(), splitLotWithoutHoldReleaseReqParams.getMergedOperationNumber(), splitLotWithoutHoldReleaseReqParams.getReturnOperationNumber());
        //-----------------------------------------------------------
        // Check for consistency of Sub Route
        //-----------------------------------------------------------
        log.debug("Check for consistency of Sub Route");
        ObjectIdentifier  lotCurrentRouteIDGet = lotMethod.lotCurrentRouteIDGet(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID());

        //----- Check for Current Route : It should not be same as Sub Route. -------//
        log.debug("Check for Current Route : It should not be same as Sub Route");
        Validations.check (CimObjectUtils.equalsWithValue(lotCurrentRouteIDGet,subRouteID), retCodeConfig.getInvalidBranchRouteId());
        //----- Gets Parent lot's Original Route. -------//
        log.debug("Gets Parent lot's Original Route");
        Outputs.ObjLotOriginalRouteListGetOut originalRouteListGetOut = lotMethod.lotOriginalRouteListGet(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID());

        for (int i = 0; i < CimArrayUtils.getSize(originalRouteListGetOut.getOriginalRouteID()); i++) {
            Validations.check (CimObjectUtils.equalsWithValue(originalRouteListGetOut.getOriginalRouteID().get(i).getValue(),subRouteID.getValue()), retCodeConfig.getInvalidBranchRouteId());
        }
        //-----------------------------------------------------------
        // Check for contents of Parent lot
        //-----------------------------------------------------------
        //----- Gets Parent lot's contents. -------//
        log.debug("Check for contents of Parent lot");
        ObjectIdentifier parentLotID = splitLotWithoutHoldReleaseReqParams.getParentLotID();
        String theLotContents = lotMethod.lotContentsGet(parentLotID);
        //----- Check for contents : It should be "wafer" or "Die". -------//
        log.debug("Check for contents : It should be \"wafer\" or \"Die\"");
        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER,theLotContents)
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE,theLotContents), retCodeConfig.getInvalidLotContents());
        //-----------------------------------------------------------
        // Check for each state of Parent lot
        //-----------------------------------------------------------
        //----- Gets Parent lot's all state. -------//
        log.debug("Check for each state of Parent lot");
        log.debug("Gets Parent lot's all state");
        Outputs.ObjLotAllStateGetOut lotAllStateGetOutRetCode = lotMethod.lotAllStateGet(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID());

        //----- Check for lot State : It should not be "SHIPPED". -------//
        log.debug("Check for lot State : It should not be \"SHIPPED\".");
        Validations.check (CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_STATE_SHIPPED,lotAllStateGetOutRetCode.getLotState()), retCodeConfig.getInvalidLotStat());
        //----- Check for lot Hold State : It should be "ONHOLD". -------//
        log.debug("Check for lot Hold State : It should be \"ONHOLD\".");
        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotAllStateGetOutRetCode.getHoldState())
                , new OmCode(retCodeConfig.getLotNotHeld(), CimObjectUtils.getObjectValue(parentLotID)));
        //---- But, the lot should not be held by "LOCK". -----//
        //lot_CheckLockHoldConditionForOperation
        log.debug("But, the lot should not be held by \"LOCK\".");
        List<ObjectIdentifier> lotIDSeq = new ArrayList<>();
        lotIDSeq.add(parentLotID);
        log.debug("check lot lock hold condition for operation");
        lotMethod.lotCheckLockHoldConditionForOperation(objCommon,lotIDSeq);

        //----- Check for lot Finished State : It should not be "SCRAPPED" or "EMPTIED". -------//
        log.debug("Check for lot Finished State : It should not be \"SCRAPPED\" or \"EMPTIED\".");
        Validations.check (CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED,lotAllStateGetOutRetCode.getFinishedState())
                || CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_FINISHED_STATE_STACKED,lotAllStateGetOutRetCode.getFinishedState())
                || CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_FINISHED_STATE_EMPTIED,lotAllStateGetOutRetCode.getFinishedState()), retCodeConfig.getInvalidLotFinishStat());
        //----- Check for lot Process State : It should not be "Processing". -------//
        log.debug("Check for lot Process State : It should not be \"Processing\"");
        Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_PROCSTATE_PROCESSING,lotAllStateGetOutRetCode.getProcessState()), retCodeConfig.getInvalidLotProcstat());
        //-----------------------------------------------------------
        // Check for hold record inheritance from Parent lot
        //-----------------------------------------------------------
        //----- Check for lot Inventory State : If it is "NonProBank", the hold record that the reason code is "NPBH" should be inherited. -------//
        log.debug("Check for hold record inheritance from Parent lot");
        log.debug("Check for lot Inventory State : If it is \"NonProBank\", the hold record that the reason code is \"NPBH\" should be inherited.");
        log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK,lotAllStateGetOutRetCode.getInventoryState()) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK,lotAllStateGetOutRetCode.getInventoryState()));
        if (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK,lotAllStateGetOutRetCode.getInventoryState())) {
            log.debug("Lot's inventory state is 'NonProBank'. So, hold record inheritance is checked.");
            int i = 0;
            for (i = 0; i < CimArrayUtils.getSize(splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList()); i++) {
                log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_REASON_NONPROBANKHOLD,splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList().get(i).getHoldReasonCodeID().getValue()) : {}",
                        CimObjectUtils.equalsWithValue(BizConstant.SP_REASON_NONPROBANKHOLD,splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList().get(i).getHoldReasonCodeID().getValue()));

                if (CimObjectUtils.equalsWithValue(BizConstant.SP_REASON_NONPROBANKHOLD,splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList().get(i).getHoldReasonCodeID().getValue())) {
                    log.debug("Hold record that the reason code is 'NPBH' is inherited from Parent Lot.");
                    break;
                }
            }
            Validations.check (i == CimArrayUtils.getSize(splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList()), retCodeConfig.getInvalidInputParam());
        }
        //------------------------------------
        //  Check InPostProcessFlag of lot
        //------------------------------------
        log.debug("Check InPostProcessFlag.");
        Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID());
        //----- Check for InPostProcessFlag : It should be OFF. -------//
        log.debug("Check for InPostProcessFlag : It should be OFF");
        log.trace("objLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot() : {}",objLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot());
        if (objLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot()) {
            log.debug("Lot is in post process.");
            /*---------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            log.debug("Get UserGroupID By UserID");
            List<ObjectIdentifier>  userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
            int i;
            for (i = 0; i < CimArrayUtils.getSize(userGroupIDs); i++) {
            }
            Validations.check (i == CimArrayUtils.getSize(userGroupIDs), new OmCode(retCodeConfig.getLotInPostProcess(),parentLotID.getValue()));
        }
        //-----------------------------------------------------------
        // Check for Flow Batch condition of Parent lot
        //-----------------------------------------------------------
        //----- Gets Parent lot's Flow Batch ID. -------//
        log.debug("Check for Flow Batch condition of Parent lot");
        log.debug("Gets Parent lot's Flow Batch ID");
        ObjectIdentifier objectIdentifierRetCode = null;
        try {
            objectIdentifierRetCode = flowBatchMethod.lotFlowBatchIDGet(objCommon, parentLotID);
            log.debug("lot doesn't belong to flow batch");
        } catch (ServiceException e) {
            log.debug("Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()));
            log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode()));
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())){
                throw e;
            }else if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())){
                log.debug("lot doesn't belong to flow batch");
            }else {
                throw e;
            }
        }

        //-------------------------
        // Check Bonding Group
        //-------------------------
        //Bonding Group
        log.debug("Check Bonding Group");
        String groupIDGetDROut = lotMethod.lotBondingGroupIDGetDR(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID());
        Validations.check(!CimStringUtils.isEmpty(groupIDGetDROut),new OmCode(retCodeConfig.getLotHasBondingGroup(),splitLotWithoutHoldReleaseReqParams.getParentLotID().getValue(),groupIDGetDROut));

        /*------------------------------------------------------------------------*/
        /*   Check if the wafers in lot don't have machine container position     */
        /*------------------------------------------------------------------------*/
        log.debug("Check if the wafers in lot don't have machine container position");
        log.info("call equipmentContainerPosition_info_GetByLotDR()");
        List<Infos.EqpContainerPosition> parentLotEquipmentContainerPositionInfoGetByLotDROut = equipmentMethod.equipmentContainerPositionInfoGetByLotDR(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID());
        Validations.check (0 < CimArrayUtils.getSize(parentLotEquipmentContainerPositionInfoGetByLotDROut), new OmCode(retCodeConfig.getWaferInLotHaveContainerPosition(),parentLotID.getValue()));
        //-----------------------------------------------------------
        // Check for Control Job of Parent lot
        //-----------------------------------------------------------
        //----- Gets Parent lot's Control Job. -------//
        log.debug("Check for Control Job of Parent lot");
        log.debug(" Gets Parent lot's Control Job");
        ObjectIdentifier controlJobIDGetOutObj = lotMethod.lotControlJobIDGet(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID());
        //----- Check for Control Job : It should not exist. -------//
        log.debug("Check for Control Job : It should not exist");
        log.trace("!ObjectUtils.isEmpty(controlJobIDGetOutObj) : {}",!CimObjectUtils.isEmpty(controlJobIDGetOutObj));
        if(!CimObjectUtils.isEmpty(controlJobIDGetOutObj)) {
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),parentLotID.getValue(),controlJobIDGetOutObj.getValue()));
        }
        //-----------------------------------------------------------
        // Check for Future Hold Request of Parent lot
        //-----------------------------------------------------------
        log.debug("Check for Future Hold Request of Parent lot");
        lotMethod.lotFutureHoldRequestsCheckSplit(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID(), splitLotWithoutHoldReleaseReqParams.getMergedOperationNumber(), splitLotWithoutHoldReleaseReqParams.getReturnOperationNumber());
        //-----------------------------------------------------------
        // Check for Xfer State of cassette
        //-----------------------------------------------------------
        //----- Gets cassette's Xfer State. -------//
        log.debug("Check for Xfer State of cassette");
        log.debug(" Gets cassette's Xfer State");
        log.trace("!StringUtils.isEmpty(cassetteID) && null != objCassetteTransferStateGetOut : {}",!CimStringUtils.isEmpty(cassetteID) && null != objCassetteTransferStateGetOut);
        if (!CimStringUtils.isEmpty(cassetteID) && null != objCassetteTransferStateGetOut) {
            String cassetteTransferStateRetCode = null;
            log.trace("ObjectUtils.equalsWithValue(\"1\",lotOperationEIcheck.toString())\n" +
                    "                    || (ObjectUtils.equalsWithValue(\"0\",lotOperationEIcheck.toString()))\n" +
                    "                    && ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,objCassetteTransferStateGetOut) : {}",
                    CimObjectUtils.equalsWithValue("1",lotOperationEIcheck.toString())
                            || (CimObjectUtils.equalsWithValue("0",lotOperationEIcheck.toString()))
                            && CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,objCassetteTransferStateGetOut));

            if (CimObjectUtils.equalsWithValue("1",lotOperationEIcheck.toString())
                    || (CimObjectUtils.equalsWithValue("0",lotOperationEIcheck.toString()))
                    && CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,objCassetteTransferStateGetOut)) {
                cassetteTransferStateRetCode = cassetteMethod.cassetteTransferStateGet(objCommon,cassetteID);
                log.trace("ObjectUtils.equalsWithValue(\"0\",lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue("0",lotOperationEIcheck.toString()));
                if (CimObjectUtils.equalsWithValue("0",lotOperationEIcheck.toString())) {
                    Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateRetCode), new OmCode(retCodeConfig.getChangedToEiByOtherOperation(),cassetteID.getValue()));
                } else {
                    Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT,cassetteTransferStateRetCode)
                            || (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateRetCode)
                            && !"OEQPW006".equals(objCommon.getTransactionID())
                            && !"OEQPW008".equals(objCommon.getTransactionID())), new OmCode(retCodeConfig.getInvalidCassetteTransferState(),
                            cassetteTransferStateRetCode,cassetteID.getValue()));
                }
            }
            log.trace("ObjectUtils.equalsWithValue(\"0\",lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue("0",lotOperationEIcheck.toString()));
            if (CimObjectUtils.equalsWithValue("0",lotOperationEIcheck.toString())) {
                cassetteTransferStateRetCode = objCassetteTransferStateGetOut;
            }
        }
        log.trace("ObjectUtils.equalsWithValue(\"1\",lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue("1",lotOperationEIcheck.toString()));
        if (CimObjectUtils.equalsWithValue("1",lotOperationEIcheck.toString())) {
            log.debug("lotOperationEIcheck = 1");
            //-----------------------------------------------------------
            // Check for dispatch State of cassette
            //-----------------------------------------------------------
            //----- Gets cassette's dispatch State. -------//
            log.debug(" Check for dispatch State of cassette");
            log.debug("Gets cassette's dispatch State");
            log.trace("!StringUtils.isEmpty(cassetteID) : {}",!CimStringUtils.isEmpty(cassetteID));
            if (!CimStringUtils.isEmpty(cassetteID)) {
                Boolean cassetteDispatchStateRetCode = cassetteMethod.cassetteDispatchStateGet(objCommon, cassetteID);
                Validations.check (cassetteDispatchStateRetCode, retCodeConfig.getAlreadyDispatchReservedCassette());
            }
        }
        //-----------------------------------------------------------
        // Check for Reserve State of cassette
        //-----------------------------------------------------------
        //----- Gets cassette's Reserve State. -------//
        log.debug("Check for Reserve State of cassette");
        log.debug("Gets cassette's Reserve State");
        log.trace("!StringUtils.isEmpty(cassetteID) : {}",!CimStringUtils.isEmpty(cassetteID));
        if (!CimStringUtils.isEmpty(cassetteID)) {
            Outputs.ObjCassetteReservedStateGetOut cassetteReservedStateRetCode = cassetteMethod.cassetteReservedStateGet(objCommon, cassetteID);
            Validations.check (cassetteReservedStateRetCode.isTransferReserved(), retCodeConfig.getAlreadyDispatchReservedCassette());
        }
        //-----------------------------------------------------------
        // Check for consistency of Hold Requests
        //-----------------------------------------------------------
        log.debug("Check for consistency of Hold Requests");
        String tmpProcessHoldAllowLotMovement = StandardProperties.OM_PROCESS_HOLD_ENABLE_WAFER_MOVE.getValue();
        tmpProcessHoldAllowLotMovement = null == tmpProcessHoldAllowLotMovement ? "0" : tmpProcessHoldAllowLotMovement;
        Integer processHoldAllowLotMovement = Integer.valueOf(tmpProcessHoldAllowLotMovement);
        Boolean inheritProcessHold = false;
        List<Infos.LotHoldReq> procHoldReq = new ArrayList<>();
        List<Infos.LotHoldReq> holdReqWithoutProcHold = new ArrayList<>();
        List<Infos.LotHoldReq> strLotHoldRequests = splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList();
        for (int i = 0; i < CimArrayUtils.getSize(strLotHoldRequests); i++) {
            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_HOLDTYPE_PROCESSHOLD, strLotHoldRequests.get(i).getHoldType()) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_HOLDTYPE_PROCESSHOLD, strLotHoldRequests.get(i).getHoldType()));
            log.trace("!StringUtils.isEmpty(splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList().get(i).getRelatedLotID()) : {}",!CimStringUtils.isEmpty(splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList().get(i).getRelatedLotID()));
            if (CimObjectUtils.equalsWithValue(BizConstant.SP_HOLDTYPE_PROCESSHOLD, strLotHoldRequests.get(i).getHoldType())) {
                Validations.check (CimStringUtils.equals("0",processHoldAllowLotMovement.toString()),
                        new OmCode(retCodeConfig.getHoldRecordCannotInherit(), strLotHoldRequests.get(i).getHoldType(), ObjectIdentifier.fetchValue(strLotHoldRequests.get(i).getHoldReasonCodeID()),
                                ObjectIdentifier.fetchValue(strLotHoldRequests.get(i).getHoldUserID()), ObjectIdentifier.fetchValue(strLotHoldRequests.get(i).getRelatedLotID())));
                inheritProcessHold = true;
                procHoldReq.add(splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList().get(i));
            } else if (!CimStringUtils.isEmpty(splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList().get(i).getRelatedLotID())) {
                Validations.check(retCodeConfig.getHoldRecordCannotInherit());
            } else {
                holdReqWithoutProcHold.add(splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList().get(i));
            }
        }
        //---------------------------
        // Get the lot's hold list.
        //---------------------------
        log.debug("Get the lot's hold list.");
        List<Infos.LotHoldListAttributes> out = null;
        try {
            out = lotMethod.lotFillInTxTRQ005DR(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID());
        } catch (ServiceException e) {
            log.trace("!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode()));
            if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                throw e;
            }
        }

        //---------------------------
        // Check Inherit Hold Record
        //---------------------------
        log.debug("Check Inherit Hold Record");
        Boolean existFlag = false;
        List<Infos.LotHoldReq> strLotHoldReleaseReqList = splitLotWithoutHoldReleaseReqParams.getStrLotHoldReleaseReqList();
        List<Infos.LotHoldListAttributes> lotHoldListAttributesList = out;
        log.trace("!ObjectUtils.isEmpty(strLotHoldReleaseReqList) && !ObjectUtils.isEmpty(lotHoldListAttributesList) : {}",!CimObjectUtils.isEmpty(strLotHoldReleaseReqList) && !CimObjectUtils.isEmpty(lotHoldListAttributesList));
        if (!CimObjectUtils.isEmpty(strLotHoldReleaseReqList) && !CimObjectUtils.isEmpty(lotHoldListAttributesList)) {
            for (Infos.LotHoldReq lotHoldReq : strLotHoldReleaseReqList) {
                for (Infos.LotHoldListAttributes lotHoldListAttributes : lotHoldListAttributesList) {
                    log.trace("lotHoldReq.getHoldType().equals(lotHoldListAttributes.getHoldType()) : {}",lotHoldReq.getHoldType().equals(lotHoldListAttributes.getHoldType()));
                    if (lotHoldReq.getHoldType().equals(lotHoldListAttributes.getHoldType())
                            && CimObjectUtils.equalsWithValue(lotHoldReq.getHoldReasonCodeID(), lotHoldListAttributes.getReasonCodeID())
                            && CimObjectUtils.equalsWithValue(lotHoldReq.getHoldUserID(), lotHoldListAttributes.getUserID())
                            && CimObjectUtils.equalsWithValue(lotHoldReq.getRelatedLotID(), lotHoldListAttributes.getRelatedLotID())) {
                        log.debug("Hold Record Exist!");
                        existFlag = true;
                        break;
                    }
                }
                Validations.check (!existFlag, new OmCode(retCodeConfig.getHoldRecordCannotInherit()
                        , CimObjectUtils.getObjectValue(lotHoldReq.getHoldReasonCodeID())
                        , CimObjectUtils.getObjectValue(lotHoldReq.getHoldUserID())
                        , CimObjectUtils.getObjectValue(lotHoldReq.getRelatedLotID())));
            }
        }

        /*------------------------------------------------------------------------*/
        /*  Check Lot Split Or Merge                                              */
        /*------------------------------------------------------------------------*/
        log.debug(" Check Lot Split Or Merge  ");
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(splitLotWithoutHoldReleaseReqParams.getParentLotID());
        this.checkLotSplitOrMerge(objCommon,lotIDLists, BizConstant.CHECK_LOT_ACTION_SPLIT);

        //===========================================================================
        // Change for objects
        //===========================================================================
        //-----------------------------------------------------------
        // Creates Child lot
        //-----------------------------------------------------------
        log.debug("Change for objects");
            log.debug("Creates Child lo");
        ObjectIdentifier objectIdentifierRetCode1 = lotMethod.lotSplitWaferLot(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID(), splitLotWithoutHoldReleaseReqParams.getChildWaferIDs());

        //inherit the contamination flag from parent lot
        log.debug("inherit the contamination flag from parent lot");
        contaminationMethod.inheritContaminationFlagFromParentLot(splitLotWithoutHoldReleaseReqParams.getParentLotID(),objectIdentifierRetCode1);

        ObjectIdentifier childLotID = objectIdentifierRetCode1;

        //----- Updates History Time Stamp to Parent lot : Those data are used in Event. -------//
        //lot_waferLotHistoryPointer_Update
        log.debug("Updates History Time Stamp to Parent lot : Those data are used in Event");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, parentLotID);
        //----- Updates History Time Stamp to Child lot : Those data are used in Event. -------//
        //lot_waferLotHistoryPointer_Update
        log.debug("Updates History Time Stamp to Child lot : Those data are used in Event");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, childLotID);
        //----- Prepares data of Split Event. -------//
        //lot_materials_GetWafers
        log.debug("Prepares data of Split Event");
        List<Infos.LotWaferAttributes> lotMaterialsGetWafersResult = lotMethod.lotMaterialsGetWafers(objCommon, childLotID);
        //----- Makes Split Event. -------//
        log.debug("Makes Split Event");
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        newLotAttributes.setCassetteID(cassetteID);
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        for(Infos.LotWaferAttributes lotWaferAttributes : lotMaterialsGetWafersResult){
            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
            newWaferAttributes.setNewLotID(childLotID);
            newWaferAttributes.setNewWaferID(lotWaferAttributes.getWaferID());
            newWaferAttributes.setNewSlotNumber(lotWaferAttributes.getSlotNumber());
            newWaferAttributes.setSourceLotID(parentLotID);
            newWaferAttributes.setSourceWaferID(lotWaferAttributes.getWaferID());
            newWaferAttributesList.add(newWaferAttributes);
        }
        //lotWaferMoveEvent_Make
        log.debug("lot wafer move envent create");
        eventMethod.lotWaferMoveEventMake(objCommon, newLotAttributes, TransactionIDEnum.SPLIT_WAFER_LOT_WITH_OUT_HOLD_RELEASE_REQ.getValue(), splitLotWithoutHoldReleaseReqParams.getClaimMemo());
        //-----------------------------------------------------------
        // Performs Process Hold to Child lot
        //-----------------------------------------------------------
        log.trace("Performs Process Hold to Child lot");
        log.trace("inheritProcessHold : {}",inheritProcessHold);
        if (inheritProcessHold) {
            sxHoldLotReq(objCommon,childLotID, procHoldReq);
        }
        //-----------------------------------------------------------
        // Performs Branch to Child lot
        //-----------------------------------------------------------
        log.debug("Performs Branch to Child lot");
        log.trace("splitLotWithoutHoldReleaseReqParams.getBranchingRouteSpecifyFlag() : {}",splitLotWithoutHoldReleaseReqParams.getBranchingRouteSpecifyFlag());
        if (splitLotWithoutHoldReleaseReqParams.getBranchingRouteSpecifyFlag()) {
            qTimeMethod.qTimeCheckConditionForReplaceTarget(objCommon,childLotID);
            Inputs.OldCurrentPOData oldCurrentPODataOut = processMethod.processBranchRoute(objCommon, childLotID, splitLotWithoutHoldReleaseReqParams.getSubRouteID(), splitLotWithoutHoldReleaseReqParams.getReturnOperationNumber());
            //--------------------------------------------------------------------------------------------------
            // Replace Target Operation for sub route
            //--------------------------------------------------------------------------------------------------
            log.debug("Replace Target Operation for sub route");
            Infos.QTimeTargetOpeReplaceIn inputParams = new Infos.QTimeTargetOpeReplaceIn();
            inputParams.setLotID(childLotID);
            inputParams.setSpecificControlFlag(false);
            qTimeMethod.qTimeTargetOpeReplace(objCommon, inputParams);

            minQTimeMethod.checkTargetOpeReplace(objCommon, childLotID);

            //----- Makes Branch Event. -------//
            //lotOperationMoveEvent_MakeBranch
            log.debug("set Branch Event operation params");
            Inputs.LotOperationMoveEventMakeBranchParams lotOperationMoveEventMakeBranchParams = new Inputs.LotOperationMoveEventMakeBranchParams();
            lotOperationMoveEventMakeBranchParams.setClaimMemo(splitLotWithoutHoldReleaseReqParams.getClaimMemo());
            lotOperationMoveEventMakeBranchParams.setOldCurrentPOData(oldCurrentPODataOut);
            lotOperationMoveEventMakeBranchParams.setLotID(childLotID);
            lotOperationMoveEventMakeBranchParams.setTransactionID(TransactionIDEnum.SPLIT_WAFER_LOT_WITH_OUT_HOLD_RELEASE_REQ.getValue());
            log.debug("branch event create");
            eventMethod.lotOperationMoveEventMakeBranch(objCommon, lotOperationMoveEventMakeBranchParams);

        }
        //-----------------------------------------------------------
        // Performs Merge Hold
        //-----------------------------------------------------------
        log.debug("Performs Merge Hold");
        log.trace("BooleanUtils.isTrue(splitLotWithoutHoldReleaseReqParams.getFutureMergeFlag()) : {}", CimBooleanUtils.isTrue(splitLotWithoutHoldReleaseReqParams.getFutureMergeFlag()));
        if (CimBooleanUtils.isTrue(splitLotWithoutHoldReleaseReqParams.getFutureMergeFlag())) {
            //----- Creates Merge Hold Request of Parent lot -------//
            log.debug("Creates Merge Hold Request of Parent lot");
            Params.FutureHoldReqParams params = new Params.FutureHoldReqParams();
            params.setHoldType(BizConstant.SP_HOLDTYPE_MERGEHOLD);
            params.setLotID(splitLotWithoutHoldReleaseReqParams.getParentLotID());
            params.setRouteID(splitLotWithoutHoldReleaseReqParams.getMergedRouteID());
            params.setOperationNumber(splitLotWithoutHoldReleaseReqParams.getMergedOperationNumber());
            params.setReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGEHOLD));
            params.setRelatedLotID(childLotID);
            params.setSingleTriggerFlag(false);
            params.setPostFlag(false);
            try {
                processControlService.sxFutureHoldReq(objCommon,params);
            } catch (ServiceException e) {
                log.trace("!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()));
                if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode())){
                    throw e;
                }
            }

            //----- Creates Merge Hold Request of Child lot -------//
            log.debug("Creates Merge Hold Request of Child lot");
            Params.FutureHoldReqParams futureHoldReqParams = new Params.FutureHoldReqParams();
            futureHoldReqParams.setHoldType(BizConstant.SP_HOLDTYPE_MERGEHOLD);
            futureHoldReqParams.setLotID(childLotID);
            futureHoldReqParams.setRouteID(splitLotWithoutHoldReleaseReqParams.getMergedRouteID());
            futureHoldReqParams.setOperationNumber(splitLotWithoutHoldReleaseReqParams.getMergedOperationNumber());
            futureHoldReqParams.setReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGEHOLD));
            futureHoldReqParams.setRelatedLotID(splitLotWithoutHoldReleaseReqParams.getParentLotID());
            futureHoldReqParams.setSingleTriggerFlag(false);
            futureHoldReqParams.setPostFlag(false);
            try {
                processControlService.sxFutureHoldReq(objCommon,futureHoldReqParams);
            } catch (ServiceException e) {
                log.debug("!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode()));
                if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(),e.getCode())){
                    throw e;
                }
            }
        }
        //----- Performs Merge Hold Request to lot on Merge Point. -------//
        log.debug("Performs Merge Hold Request to lot on Merge Point");
        Outputs.ObjProcessCompareCurrentOut processCompareCurrentOut = null;
        try{
            processCompareCurrentOut = processMethod.processCompareCurrent(objCommon, splitLotWithoutHoldReleaseReqParams.getParentLotID(), splitLotWithoutHoldReleaseReqParams.getMergedRouteID(), splitLotWithoutHoldReleaseReqParams.getMergedOperationNumber());
        }catch (ServiceException ex){
            log.trace("retCodeConfig.getCurrentToperationSame().getCode() == ex.getCode() : {}",retCodeConfig.getCurrentToperationSame().getCode() == ex.getCode());
            log.trace("Validations.isEquals(retCodeConfig.getSucc(), ex.getCode() : {}",Validations.isEquals(retCodeConfig.getSucc(), ex.getCode()));
            if (retCodeConfig.getCurrentToperationSame().getCode() == ex.getCode()) {
                List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_MERGEHOLD);
                lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGEHOLD));
                lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq.setRouteID(splitLotWithoutHoldReleaseReqParams.getMergedRouteID());
                lotHoldReq.setOperationNumber(splitLotWithoutHoldReleaseReqParams.getMergedOperationNumber());
                lotHoldReq.setRelatedLotID(childLotID);
                holdReqList.add(lotHoldReq);
                sxHoldLotReq(objCommon,splitLotWithoutHoldReleaseReqParams.getParentLotID(), holdReqList);
                log.trace("!BooleanUtils.isTrue(splitLotWithoutHoldReleaseReqParams.getBranchingRouteSpecifyFlag()) : {}",!CimBooleanUtils.isTrue(splitLotWithoutHoldReleaseReqParams.getBranchingRouteSpecifyFlag()));
                if(!CimBooleanUtils.isTrue(splitLotWithoutHoldReleaseReqParams.getBranchingRouteSpecifyFlag())){
                    List<Infos.LotHoldReq> holdReqList1 = new ArrayList<>();
                    Infos.LotHoldReq lotHoldReq1 = new Infos.LotHoldReq();
                    lotHoldReq1.setHoldType(BizConstant.SP_HOLDTYPE_MERGEHOLD);
                    lotHoldReq1.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGEHOLD));
                    lotHoldReq1.setHoldUserID(objCommon.getUser().getUserID());
                    lotHoldReq1.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    lotHoldReq1.setRouteID(splitLotWithoutHoldReleaseReqParams.getMergedRouteID());
                    lotHoldReq1.setOperationNumber(splitLotWithoutHoldReleaseReqParams.getMergedOperationNumber());
                    lotHoldReq1.setRelatedLotID(splitLotWithoutHoldReleaseReqParams.getParentLotID());
                    holdReqList1.add(lotHoldReq1);
                    sxHoldLotReq(objCommon,childLotID, holdReqList1);
                }
            } else if (Validations.isEquals(retCodeConfig.getSucc(), ex.getCode())
                    || Validations.isEquals(retCodeConfig.getCurrentToperationEarly(), ex.getCode())
                    || Validations.isEquals(retCodeConfig.getCurrentToperationLate(), ex.getCode())) {
            } else {
                throw ex;
            }
        }

        //-----------------------------------------------------------
        // Performs Process Hold to Child lot
        //-----------------------------------------------------------
        log.debug("Performs Process Hold to Child lot");
        log.trace("splitLotWithoutHoldReleaseReqParams.getBranchingRouteSpecifyFlag() : {}",splitLotWithoutHoldReleaseReqParams.getBranchingRouteSpecifyFlag());
        if (splitLotWithoutHoldReleaseReqParams.getBranchingRouteSpecifyFlag()) {
            processControlService.sxProcessHoldDoActionReq(objCommon,childLotID, splitLotWithoutHoldReleaseReqParams.getClaimMemo());
        }
        //-----------------------------------------------------------
        // Inherits Hold Records to Child lot
        //-----------------------------------------------------------
        log.debug("Inherits Hold Records to Child lot");
        log.trace("!ArrayUtils.isEmpty(holdReqWithoutProcHold) : {}",!CimArrayUtils.isEmpty(holdReqWithoutProcHold));
        if (!CimArrayUtils.isEmpty(holdReqWithoutProcHold)) {
            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotAllStateGetOutRetCode.getInventoryState()) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotAllStateGetOutRetCode.getInventoryState()));
            if (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotAllStateGetOutRetCode.getInventoryState())) {
                Params.HoldLotInBankReqParams holdLotInBankReqParams = new Params.HoldLotInBankReqParams();
                List<ObjectIdentifier> lots = new ArrayList<>();
                lots.add(childLotID);
                holdLotInBankReqParams.setLotIDs(lots);
                holdLotInBankReqParams.setReasonCodeID(holdReqWithoutProcHold.get(0).getHoldReasonCodeID());
                holdLotInBankReqParams.setClaimMemo(holdReqWithoutProcHold.get(0).getClaimMemo());
                Results.HoldLotInBankReqResult holdLotInBankReqResult = bankService.sxHoldLotInBankReqReq(objCommon, 0, holdLotInBankReqParams);
            } else {
                log.debug("Lot's inventory state is not 'InBank'. So, txHoldLotReq() is called.");
                sxHoldLotReq(objCommon, childLotID, holdReqWithoutProcHold);
            }
        }
        //-----------------------------------------------------------
        // Updates cassette's Multi lot Type
        //-----------------------------------------------------------
        log.debug("Updates cassette's Multi lot Type");
        log.trace("StringUtils.isNotEmpty(cassetteID.getValue()) : {}", CimStringUtils.isNotEmpty(cassetteID.getValue()));
        if (CimStringUtils.isNotEmpty(cassetteID.getValue())){
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
        }
        //-----------------------------------------------------------
        // Updates Child lot's Required cassette Category
        //-----------------------------------------------------------
        log.debug("Updates Child lot's Required cassette Category");
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, childLotID);
        //-----------------------------------------------------------
        // Updates eqp's cassette/lot data etc.
        //-----------------------------------------------------------
        log.debug("Updates eqp's cassette/lot data etc.");
        log.trace("StringUtils.isNotEmpty(cassetteID.getValue()) : {}", CimStringUtils.isNotEmpty(cassetteID.getValue()));
        if (CimStringUtils.isNotEmpty(cassetteID.getValue())){
            List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
            cassetteIDList.add(cassetteID);
            controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDList);
        }
        retVal.setChildLotID(childLotID);
        return retVal;
    }

    @SuppressWarnings("deprecation")
    public void sxBranchCancelReq(Infos.ObjCommon objCommon, Params.BranchCancelReqParams branchCancelReqParams) {
        ObjectIdentifier aLotID = branchCancelReqParams.getLotID();
        /*------------------------------------------------------------------------*/
        /*   Get cassette / lot connection                                        */
        /*------------------------------------------------------------------------*/
        log.debug("step1 - lot_cassette_Get");
        ObjectIdentifier cassetteID = lotMethod.lotCassetteGet(objCommon, aLotID);
        /*--------------------------------*/
        /*   Lock objects to be updated   */
        /*--------------------------------*/
        // object_Lock
        log.debug("Lock objects to be updated");
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
        // object_Lock
        log.debug("lock lot object");
        objectLockMethod.objectLock(objCommon, CimLot.class, aLotID);
        //--------------------------------------------------------------------------
        //   Check whether lot is on the specified Route/Operation or Not
        //--------------------------------------------------------------------------
        log.debug("Check whether lot is on the specified Route/Operation or Not");
        ObjectIdentifier tmpCurrentRouteID = branchCancelReqParams.getCurrentRouteID();
        String tmpCurrentOperationNumber = branchCancelReqParams.getCurrentOperationNumber();
        log.trace("!ObjectUtils.isEmptyWithValue(tmpCurrentRouteID) && !StringUtils.isEmpty(tmpCurrentOperationNumber) : {}",!CimObjectUtils.isEmptyWithValue(tmpCurrentRouteID) && !CimStringUtils.isEmpty(tmpCurrentOperationNumber));
        if (!CimObjectUtils.isEmptyWithValue(tmpCurrentRouteID) && !CimStringUtils.isEmpty(tmpCurrentOperationNumber)) {
            log.debug("step4 - lot_currentOperationInfo_Get");
            Outputs.ObjLotCurrentOperationInfoGetDROut objLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGetDR(objCommon, branchCancelReqParams.getLotID());
            log.trace("ObjectUtils.equalsWithValue(tmpCurrentRouteID, objLotCurrentOperationInfoGetOut.getMainPDID()) && tmpCurrentOperationNumber.equals(objLotCurrentOperationInfoGetOut.getOpeNo()) : {}",
                    CimObjectUtils.equalsWithValue(tmpCurrentRouteID, objLotCurrentOperationInfoGetOut.getMainPDID()) && tmpCurrentOperationNumber.equals(objLotCurrentOperationInfoGetOut.getOpeNo()));

            if (CimObjectUtils.equalsWithValue(tmpCurrentRouteID, objLotCurrentOperationInfoGetOut.getMainPDID()) && tmpCurrentOperationNumber.equals(objLotCurrentOperationInfoGetOut.getOpeNo())) {
                log.debug("Route/Operation check OK. Go ahead...");
            } else {
                throw new ServiceException(new OmCode(retCodeConfig.getNotSameRoute(), "Input parameter's currentRouteID/currentOperationNumber", "lot's current currentRouteID/currentOperationNumber"));
            }
        }
        /*------------------------------------------------------------------------*/
        /*   Check Condition                                                      */
        /*------------------------------------------------------------------------*/
        log.debug("step5 - Check Condition.");
        String lotHoldState = lotMethod.lotHoldStateGet(objCommon, aLotID);
        Validations.check (CimStringUtils.equals(lotHoldState, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD),
                new OmCode(retCodeConfig.getInvalidLotHoldStat(), aLotID.getValue(), lotHoldState));
        String lotState = lotMethod.lotStateGet(objCommon, aLotID);
        Validations.check (!CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE), new OmCode(retCodeConfig.getInvalidLotStat(), lotState));
        String theLotProcessState = lotMethod.lotProcessStateGet(objCommon, aLotID);
        Validations.check (CimStringUtils.equals(theLotProcessState, BizConstant.SP_LOT_PROCSTATE_PROCESSING),
                new OmCode(retCodeConfig.getInvalidLotProcessState(), aLotID.getValue(), theLotProcessState));
        String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, aLotID);
        Validations.check (!CimStringUtils.equals(lotInventoryState, BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR),
                retCodeConfig.getInvalidLotInventoryStat(), aLotID.getValue(), lotInventoryState);
        String lotProductionState = lotMethod.lotProductionStateGet(objCommon, aLotID);
        Validations.check (!CimStringUtils.equals(lotProductionState, CIMStateConst.CIM_LOT_PRODUCTION_STATE_INPRODUCTION),
                new OmCode(retCodeConfig.getInvalidLotProductionState(), aLotID.getValue(), lotProductionState));
        //----------------------------------
        //  Get InPostProcessFlag of lot
        //----------------------------------
        log.debug("step6 -Get InPostProcessFlag of lot");
        Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, aLotID);
        //----------------------------------------------
        // If lot is in post process, returns error
        //----------------------------------------------
        log.trace("objLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot() : {}",objLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot());
        if (objLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot()) {
            /*--------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            log.debug("step7 - Get UserGroupID By UserID");
            List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
            int i;
            for (i = 0; i < userGroupIDs.size(); i++) {
            }
            Validations.check (i == userGroupIDs.size(), new OmCode(retCodeConfig.getLotInPostProcess(), aLotID.getValue()));
        }
        //---------------------------------------
        //   Check interFabXferPlan existence
        //---------------------------------------
        //【TODO】【STEP 8】- interFab_xferPlanList_GetDR

        log.debug("Check interFabXferPlan existence");
        Integer lotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getIntValue();
        log.trace("lotOperationEIcheck == 1 : {}",lotOperationEIcheck == 1);
        if (lotOperationEIcheck == 1){
            //==================================
            // TransferState should be Not EI
            //==================================
            log.debug("step9 - cassette transferState Get.");
            String transferState = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
            Validations.check(CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN),
                    new OmCode(retCodeConfig.getInvalidLotXferstat(), cassetteID.getValue(), transferState));
        }
        /*----------------------------------*/
        /*   Check lot's Control Job ID     */
        /*----------------------------------*/
        log.debug("step10 - Check lot's Control Job ID");
        ObjectIdentifier controlJobID = lotMethod.lotControlJobIDGet(objCommon, branchCancelReqParams.getLotID());
        log.trace("!ObjectUtils.isEmptyWithValue(controlJobID) : {}",!CimObjectUtils.isEmptyWithValue(controlJobID));
        if (!CimObjectUtils.isEmptyWithValue(controlJobID)){
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(), aLotID.getValue(), controlJobID.getValue()));
        }
        log.debug("step11 - process_CheckBranchCancel.");
        processMethod.processCheckBranchCancel(objCommon, aLotID);
        log.debug("step12 - lot_futureHoldRequests_CheckBranchCancel.");
        lotMethod.lotFutureHoldRequestsCheckBranchCancel(objCommon, aLotID);

        /*-----------------------------------*/
        /*   Check Future Action Procedure   */
        /*-----------------------------------*/
        log.debug("step13 -Check Future Action Procedure");
        scheduleChangeReservationMethod.schdlChangeReservationCheckForBranchCancelDR(objCommon, aLotID);
        /*-----------------------------------------------------------*/
        /* Roll back Target Operation for sub route                  */
        /*-----------------------------------------------------------*/
        log.debug("step14 -Roll back Target Operation for sub route qTime_targetOpe_cancelReplace.");
        qTimeMethod.qTimeTargetOpeCancelReplace(objCommon, aLotID);
        /*-----------------------------------------------------------*/
        /* Route change request                                      */
        /*-----------------------------------------------------------*/
        log.debug("step15 - Route change reques");
        Inputs.OldCurrentPOData oldCurrentPODataRetCode = processMethod.processCancelBranchRoute(objCommon, aLotID);
        Infos.EffectCondition effectCondition = new Infos.EffectCondition();
        effectCondition.setPhase(BizConstant.SP_FUTUREHOLD_PRE);
        effectCondition.setTriggerLevel(BizConstant.SP_FUTUREHOLD_ALL);
        log.debug("step16 - lot_futureHoldRequests_EffectByCondition.");
        Outputs.ObjLotFutureHoldRequestsEffectByConditionOut objLotFutureHoldRequestsEffectByConditionOut = lotMethod.lotFutureHoldRequestsEffectByCondition(objCommon, aLotID, effectCondition);
        List<Infos.LotHoldReq> strLotHoldReqList = objLotFutureHoldRequestsEffectByConditionOut.getStrLotHoldReqList();
        log.trace("!ArrayUtils.isEmpty(strLotHoldReqList) : {}",!CimArrayUtils.isEmpty(strLotHoldReqList));
        if (!CimArrayUtils.isEmpty(strLotHoldReqList)) {
            log.debug("step17 - Hold Lot Req.");
            sxHoldLotReq(objCommon, aLotID, strLotHoldReqList);
        }
        effectCondition.setPhase(BizConstant.SP_FUTUREHOLD_PRE);
        effectCondition.setTriggerLevel(BizConstant.SP_FUTUREHOLD_SINGLE);
        log.debug("step18 - lot_futureHoldRequests_DeleteEffectedByCondition.");
        Outputs.ObjLotFutureHoldRequestsDeleteEffectedByConditionOut objLotFutureHoldRequestsDeleteEffectedByConditionOut = lotMethod.lotFutureHoldRequestsDeleteEffectedByCondition(objCommon, aLotID, effectCondition);
        List<Infos.LotHoldReq> strFutureHoldReleaseReqList = objLotFutureHoldRequestsDeleteEffectedByConditionOut.getStrFutureHoldReleaseReqList();
        log.trace("!ArrayUtils.isEmpty(strFutureHoldReleaseReqList) : {}",!CimArrayUtils.isEmpty(strFutureHoldReleaseReqList));
        if (!CimArrayUtils.isEmpty(strFutureHoldReleaseReqList)) {
            log.debug("step19 - txFutureHoldCancelReq.");
            processControlService.sxFutureHoldCancelReq(objCommon, aLotID, null, BizConstant.SP_ENTRYTYPE_REMOVE, strFutureHoldReleaseReqList);
        }
        /*---------------------------------------*/
        /*   Update cassette's MultiLotType      */
        /*---------------------------------------*/
        log.debug("step20 -  Update cassette's MultiLotType");
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
        //--------------------------------------------------------------------------------------------------
        // UpDate RequiredCassetteCategory
        //--------------------------------------------------------------------------------------------------
        log.debug("step21 - UpDate RequiredCassetteCategory");
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, branchCancelReqParams.getLotID());
        //-----------------------//
        //     Process Hold      //
        //-----------------------//
        log.debug("step22 -Process Hold");
        processControlService.sxProcessHoldDoActionReq(objCommon, aLotID, branchCancelReqParams.getClaimMemo());
        /*-----------------------------------------------------------*/
        /*   Make History                                            */
        /*-----------------------------------------------------------*/
        //lotOperationMoveEvent_MakeBranch
        log.debug("step23 -Make History");
        Inputs.LotOperationMoveEventMakeBranchParams lotOperationMoveEventMakeBranchParams = new Inputs.LotOperationMoveEventMakeBranchParams();
        lotOperationMoveEventMakeBranchParams.setTransactionID(TransactionIDEnum.SUBROUT_BRABCH_CANCEL_REQ.getValue());
        lotOperationMoveEventMakeBranchParams.setLotID(branchCancelReqParams.getLotID());
        lotOperationMoveEventMakeBranchParams.setOldCurrentPOData(oldCurrentPODataRetCode);
        lotOperationMoveEventMakeBranchParams.setClaimMemo(branchCancelReqParams.getClaimMemo());
        eventMethod.lotOperationMoveEventMakeBranch(objCommon, lotOperationMoveEventMakeBranchParams);

        // 【step 24】check contamination level ，trigger contamination hold
        log.debug("step24 - check contamination level ，trigger contamination hold.");
        contaminationMethod.lotCheckContaminationLevelStepOut(objCommon, aLotID);
    }

    public void sxSubRouteBranchReqService(Infos.ObjCommon objCommon, Params.SubRouteBranchReqParams params){
        Infos.BranchReq branchReq = new Infos.BranchReq();
        branchReq.setLotID(params.getLotID());
        branchReq.setBDynamicRoute(false);
        branchReq.setSubRouteID(params.getSubRouteID());
        branchReq.setReturnOperationNumber(params.getReturnOperationNumber());
        branchReq.setCurrentRouteID(params.getCurrentRouteID());
        branchReq.setCurrentOperationNumber(params.getCurrentOperationNumber());
        branchReq.setEventTxId(objCommon.getUser().getFunctionID());
        log.debug("sent brach request");
        sxBranchReq(objCommon,branchReq, params.getClaimMemo());
    }

    public void sxForceSkipReq(Infos.ObjCommon objCommon, Params.SkipReqParams params) {

        //-----------------------------------------------------------
        // Check for Hold State
        //-----------------------------------------------------------
        //----- Get hold state. -------//

        //step1 - lot_holdState_Get
        log.debug("[step-1] Check for Hold State");
        String objLotHoldStateGetOut = lotMethod.lotHoldStateGet(objCommon,params.getLotID());
        //----- Check for lot Hold State : It should be "ONHOLD". -------//
        log.debug("Check for lot Hold State : It should be \"ONHOLD\".");
        Validations.check(!CimStringUtils.equals(objLotHoldStateGetOut, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD), new OmCode(retCodeConfig.getLotNotHeld(),params.getLotID().getValue()) );
        //---- But, the lot should not be held by "LOCK". -----//
        log.debug("But, the lot should not be held by \"LOCK\"");
        List<ObjectIdentifier> lotIDSeq = new ArrayList<>();
        lotIDSeq.add(params.getLotID());

        //step2 - lot_CheckLockHoldConditionForOperation
        log.debug("[step-2] ckeck lot lock hold condition for operation");
        lotMethod.lotCheckLockHoldConditionForOperation(objCommon,lotIDSeq);


        //-----------------------------------------------------------
        // Operation
        //-----------------------------------------------------------

        //step3 - txSkipReq
        log.debug("[step-3] force skip request");
        sxSkipReq(objCommon,params);

        //step4 - TX There is no post process, so manual sampling operation is needed. Should we consider adding post process later
        log.debug("[step-4] TX There is no post process, so manual sampling operation is needed. Should we consider adding post process later");
        samplingService.sxLotSamplingCheckThenSkipReq(objCommon, params.getLotID(), ThreadContextHolder.getTransactionId(), BizConstant.LS_BASIC_LOT_EXECUTE);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 12/10/2018 1:19 PM
     * @param objCommon -
     * @param params -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    public void sxReworkWholeLotReq(Infos.ObjCommon objCommon, Params.ReworkWholeLotReqParams params) {
        Infos.ReworkReq reworkReqInfos = new Infos.ReworkReq();
        reworkReqInfos.setLotID(params.getLotID());
        reworkReqInfos.setSubRouteID(params.getSubRouteID());
        reworkReqInfos.setReturnOperationNumber(params.getReturnOperationNumber());
        reworkReqInfos.setReasonCodeID(params.getReasonCodeID());
        reworkReqInfos.setEventTxId(objCommon.getUser().getFunctionID());
        reworkReqInfos.setDynamicRouteFlag(false);
        reworkReqInfos.setForceReworkFlag(false);
        reworkReqInfos.setCurrentRouteID(params.getCurrentRouteID());
        reworkReqInfos.setCurrentOperationNumber(params.getCurrentOperationNumber());

        log.debug("rework request");
        sxReworkReq(objCommon,reworkReqInfos, params.getClaimMemo());
    }

    @SuppressWarnings("deprecation")
    public Results.PassThruReqResult sxPassThruReq(Infos.ObjCommon objCommon,
                                                   Infos.GatePassLotInfo strGatePassLotInfo,
                                                   String claimMemo) {
        Results.PassThruReqResult retVal =  new Results.PassThruReqResult();

        //init
        List<Results.GatePassLotsResult> strGatePassLotsResult = new ArrayList<>();
        retVal.setStrGatePassLotsResult(strGatePassLotsResult);

        List<ObjectIdentifier> holdReleasedLotIDs = new ArrayList<>();
        retVal.setHoldReleasedLotIDs(holdReleasedLotIDs);

        ObjectIdentifier aLotID = strGatePassLotInfo.getLotID();
        ObjectIdentifier aCassetteID;
        ObjectIdentifier routeID = strGatePassLotInfo.getCurrentRouteID();
        String operationNumber = strGatePassLotInfo.getCurrentOperationNumber();

        //Get lot / cassette connection
        log.debug("[step-1] Get lot / cassette connection");
        Results.GatePassLotsResult gatePassLotsResult = new Results.GatePassLotsResult();
        strGatePassLotsResult.add(gatePassLotsResult);

        //Step1 - lot_cassette_Get
        log.debug("[step-1]  get cassette for lot");
        ObjectIdentifier strLotCassetteGetOut = null;
        try {
            strLotCassetteGetOut = lotMethod.lotCassetteGet(objCommon, aLotID);
        } catch (ServiceException e) {

            gatePassLotsResult.setReturnCode(String.valueOf(e.getCode()));
            gatePassLotsResult.setLotID(aLotID);
            throw e;
        }
        log.debug("lot_cassette_Get");

        aCassetteID = strLotCassetteGetOut;
        boolean bParallelPostProcFlag = false;
        String strParallelPostProcFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG);

        log.trace("StringUtils.equals(BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON, strParallelPostProcFlag) : {}", CimStringUtils.equals(BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON, strParallelPostProcFlag));
        if (CimStringUtils.equals(BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON, strParallelPostProcFlag)) {
            bParallelPostProcFlag = true;
        }

        // Step2 - object_Lock
        log.debug("[step-2] lock cassette object by cassetteID");
        log.trace("bParallelPostProcFlag : {}",bParallelPostProcFlag);
        if (!bParallelPostProcFlag){
            objectLockMethod.objectLock(objCommon, CimCassette.class, aCassetteID);
        }
        String environmentVariable = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        long lotOperationEIcheck = Long.valueOf(environmentVariable);
        log.trace("1 == lotOperationEIcheck : {}",1 == lotOperationEIcheck);
        if (1 == lotOperationEIcheck) {
            log.trace("!TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) &&\n" +
                    "                    !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) &&\n" +
                    "                    !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) &&\n" +
                    "                    !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) &&\n" +
                    "                    !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_REQ, objCommon.getTransactionID()) &&\n" +
                    "                    !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) &&\n" +
                    "                    !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_REQ, objCommon.getTransactionID()) &&\n" +
                    "                    !TransactionIDEnum.equals(TransactionIDEnum.WAFER_SORTER_ON_EQP_RPT, objCommon.getTransactionID()) &&\n" +
                    "                    !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) : {}",
                    !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) &&
                            !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) &&
                            !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) &&
                            !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) &&
                            !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_REQ, objCommon.getTransactionID()) &&
                            !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) &&
                            !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_REQ, objCommon.getTransactionID()) &&
                            !TransactionIDEnum.equals(TransactionIDEnum.WAFER_SORTER_ON_EQP_RPT, objCommon.getTransactionID()) &&
                            !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()));

            if (!TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) &&
                    !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) &&
                    !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) &&
                    !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) &&
                    !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_REQ, objCommon.getTransactionID()) &&
                    !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) &&
                    !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_REQ, objCommon.getTransactionID()) &&
                    !TransactionIDEnum.equals(TransactionIDEnum.WAFER_SORTER_ON_EQP_RPT, objCommon.getTransactionID()) &&
                    !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID())) {

                //Step3 - cassette_transferState_Get
                log.debug("[step-3] get cassette transferstate ");
                String strCassetteTransferStateGetOut = null;
                try {
                    strCassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, aCassetteID);
                } catch (ServiceException e) {
                    gatePassLotsResult.setReturnCode(String.valueOf(e.getCode()));
                    gatePassLotsResult.setLotID(aLotID);
                    throw e;
                }
                Validations.check(CimStringUtils.equals(CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN, strCassetteTransferStateGetOut), new OmCode(retCodeConfig.getInvalidCassetteTransferState(),
                        strCassetteTransferStateGetOut, aCassetteID.getValue()));
            }
        }
        //Lock objects to be updated
        // Step4 - object_Lock
        log.debug("l[step-4] lock lot object by lotID");
        objectLockMethod.objectLock(objCommon, CimLot.class, aLotID);

        log.trace("BooleanUtils.isTrue(bParallelPostProcFlag) : {}", CimBooleanUtils.isTrue(bParallelPostProcFlag));
        if (CimBooleanUtils.isTrue(bParallelPostProcFlag)) {
            //Lock MonitorGroup to reactivate object in Post Process parallel execution

            //Step5 - monitorGroup_GetDR
            log.debug("[step-5] get monitor groups");
            List<Infos.MonitorGroups> monitorGroups = monitorGroupMethod.monitorGroupGetDR(objCommon, aLotID);
            int monGrpLen = CimArrayUtils.getSize(monitorGroups);
            for (int i = 0; i < monGrpLen; i++){
                // Step6 - object_Lock
                log.debug("[step-6] lock monitor group object");
                objectLockMethod.objectLock(objCommon, CimMonitorGroup.class, monitorGroups.get(i).getMonitorGroupID());
            }
        }
        gatePassLotsResult.setLotID(aLotID);

        //Check whether Lot is on the specified Route/Operation or Not

        //Step7 - lot_currentOperationInfo_Get
        log.debug("[step-7] Check whether Lot is on the specified Route/Operation or Not");
        Outputs.ObjLotCurrentOperationInfoGetOut strLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, aLotID);
        log.trace("StringUtils.equals(routeID.getValue(), strLotCurrentOperationInfoGetOut.getRouteID().getValue()) &&\n" +
                "                StringUtils.equals(operationNumber, strLotCurrentOperationInfoGetOut.getOperationNumber()) : {}", CimStringUtils.equals(routeID.getValue(), strLotCurrentOperationInfoGetOut.getRouteID().getValue()) &&
                CimStringUtils.equals(operationNumber, strLotCurrentOperationInfoGetOut.getOperationNumber()));
        if (CimStringUtils.equals(routeID.getValue(), strLotCurrentOperationInfoGetOut.getRouteID().getValue()) &&
                CimStringUtils.equals(operationNumber, strLotCurrentOperationInfoGetOut.getOperationNumber())) {
            // do nothing
        } else {
            gatePassLotsResult.setReturnCode(String.valueOf(retCodeConfig.getNotSameRoute().getCode()));
            gatePassLotsResult.setLotID(aLotID);
            Validations.check(retCodeConfig.getNotSameRoute(),routeID,operationNumber);
        }
        //Check Condition
        // check contamination pr flag
        log.debug("Check Condition pilot run flag");
        contaminationMethod.lotCheckPrFlagStepIn(aLotID);

        //Step8 - process_CheckGatePass
        log.debug("[step-8] check process gate pass by lotID");
        processMethod.processCheckGatePass(objCommon, aLotID);

        //Step9 - process_CheckGatePassForBondingFlowSection
        log.debug("[step-9] check process gate pass for bonding flow section by lotID");
        processMethod.processCheckGatePassForBondingFlowSection(objCommon, aLotID);

        //Step10 - lot_state_Get
        log.debug("get lot state");
        Outputs.ObjLotAllStateGetOut objLotAllStateGetOut = lotMethod.lotAllStateGet(objCommon, aLotID);
        log.trace("!ObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_STATE_ACTIVE, objLotAllStateGetOut.getLotState()) : {}",!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_STATE_ACTIVE, objLotAllStateGetOut.getLotState()));
        if (!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_STATE_ACTIVE, objLotAllStateGetOut.getLotState())) {
            gatePassLotsResult.setReturnCode(String.valueOf(retCodeConfig.getInvalidLotStat().getCode()));
            Validations.check(retCodeConfig.getInvalidLotStat());
        }

        //Step11 - lot_holdState_Get
        log.debug("[step-11] get lot hold state");
        String objLotHoldStateGetOut = lotMethod.lotHoldStateGet(objCommon, aLotID);
        log.trace("!ObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD, objLotHoldStateGetOut) : {}",!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD, objLotHoldStateGetOut));
        if (!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD, objLotHoldStateGetOut)) {
            gatePassLotsResult.setReturnCode(String.valueOf(retCodeConfig.getInvalidLotHoldStat().getCode()));
            Validations.check(retCodeConfig.getInvalidLotHoldStat());
        }

        //Step12 - lot_processState_Get
        log.debug("[step-12] get lot process state");
        String lotProcessStateGet = lotMethod.lotProcessStateGet(objCommon, aLotID);
        log.trace("!ObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_PROCESS_STATE_WAITING, lotProcessStateGet) : {}",!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_PROCESS_STATE_WAITING, lotProcessStateGet));
        if (!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_PROCESS_STATE_WAITING, lotProcessStateGet)) {
            gatePassLotsResult.setReturnCode(String.valueOf(retCodeConfig.getInvalidLotProcstat().getCode()));
            throw new ServiceException(retCodeConfig.getInvalidLotProcstat());
        }

        //Step13 - lot_inventoryState_Get
        log.debug("[step-13] get lot inventory state ");
        String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, aLotID);
        Validations.check(!CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_INVENTORY_STATE_ONFLOOR, lotInventoryState),
                retCodeConfig.getInvalidLotInventoryStat(),ObjectIdentifier.fetchValue(aLotID), lotInventoryState);

        //Step14 - lot_inPostProcessFlag_Get
        log.debug("[step-14] get lot in post process flag");
        Outputs.ObjLotInPostProcessFlagOut strLotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, aLotID);
        //  If Lot is in post process, returns error
        log.trace("BooleanUtils.isTrue(strLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot()) : {}", CimBooleanUtils.isTrue(strLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot()));
        if (CimBooleanUtils.isTrue(strLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot())) {
            //Get UserGroupID By UserID

            //Step15 - person_userGroupList_GetDR
            log.debug("[step-15] get user group by userID");
            List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
            long nCnt = 0;
            log.trace("ArrayUtils.isNotEmpty(userGroupIDs) : {}", CimArrayUtils.isNotEmpty(userGroupIDs));
            if (CimArrayUtils.isNotEmpty(userGroupIDs)) {
                for (ObjectIdentifier userGroupID : userGroupIDs) {
                }
            }
            Validations.check(nCnt == userGroupIDs.size(), retCodeConfig.getLotInPostProcess());
        }

        //TODO-NOTIMPL:Step16 - interFab_xferPlanList_GetDR
        log.debug("[step-16] get interfab transfer plan list");
        Inputs.ObjInterFabXferPlanListGetDRIn objInterFabXferPlanListGetDRIn = new Inputs.ObjInterFabXferPlanListGetDRIn();
        Infos.InterFabLotXferPlanInfo interFabLotXferPlanInfo = new Infos.InterFabLotXferPlanInfo();
        interFabLotXferPlanInfo.setLotID(aLotID);
        interFabLotXferPlanInfo.setOriginalFabID(StandardProperties.OM_SITE_ID.getValue());
        interFabLotXferPlanInfo.setOriginalRouteID(routeID);
        interFabLotXferPlanInfo.setOriginalOpeNumber(operationNumber);
        objInterFabXferPlanListGetDRIn.setStrInterFabLotXferPlanInfo(interFabLotXferPlanInfo);

        //TODO-NOTIMPL 暂时不需要FS的表
//        try {
//            Outputs.ObjInterFabXferPlanListGetDROut objInterFabXferPlanListGetDROut = cimComp.interFabXferPlanListGetDR(objCommon, objInterFabXferPlanListGetDRIn);
//            List<Infos.InterFabLotXferPlanInfo> strInterFabLotXferPlanInfoSeq = objInterFabXferPlanListGetDROut.getStrInterFabLotXferPlanInfoSeq();
//            for (Infos.InterFabLotXferPlanInfo fabLotXferPlanInfo : strInterFabLotXferPlanInfoSeq) {
//                Validations.check(ObjectUtils.equalsWithValue(fabLotXferPlanInfo.getState(), BizConstant.SP_INTERFAB_XFERPLANSTATE_CREATED) ||
//                                ObjectUtils.equalsWithValue(fabLotXferPlanInfo.getState(), BizConstant.SP_INTERFAB_XFERPLANSTATE_CANCELED),
//                        retCodeConfig.getInterfabProcessSkipError(), strGatePassLotInfo.get(seqIndex).getCurrentOperationNumber());
//            }
//        } catch (ServiceException e) {
//            if (!Validations.isEquals(retCodeConfig.getInterfabNotFoundXferPlan(), e.getCode())) {
//                throw e;
//            }
//        }


        // Check Lot's Control Job ID
        //Step17 - lot_flowBatch_CheckLocate
        //Step18 - lot_flowBatchID_Get
        //Step19 - txFlowBatchLotRemoveReq

        //Step20 - lot_controlJobID_Get
        log.debug("[step-20] get lot control job by lotID");
        ObjectIdentifier lotControlJobIDOut = lotMethod.lotControlJobIDGet(objCommon, aLotID);
        Validations.check(!CimObjectUtils.isEmpty(lotControlJobIDOut), new OmCode(retCodeConfig.getLotControlJobidFilled()
                , CimObjectUtils.getObjectValue(aLotID), CimObjectUtils.getObjectValue(lotControlJobIDOut)));

        //Step21 - process_CheckBankIn
        log.debug("[step-21] check process bank in ");
        try {
            processMethod.processCheckBankIn(objCommon, aLotID);
        } catch (ServiceException e) {
            log.trace("!Validations.isEquals(retCodeConfig.getNotBankInOperation(), e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotBankInOperation(), e.getCode()));
            if (!Validations.isEquals(retCodeConfig.getNotBankInOperation(), e.getCode())) {
                throw e;
            }
        }
        Inputs.ObjSchdlChangeReservationCheckForActionDRIn objSchdlChangeReservationCheckForActionDRIn = new Inputs.ObjSchdlChangeReservationCheckForActionDRIn();
        objSchdlChangeReservationCheckForActionDRIn.setLotID(aLotID);
        objSchdlChangeReservationCheckForActionDRIn.setRouteID(strLotCurrentOperationInfoGetOut.getRouteID().getValue());
        objSchdlChangeReservationCheckForActionDRIn.setOperationNumber(strLotCurrentOperationInfoGetOut.getOperationNumber());

        //Step22 - schdlChangeReservation_CheckForActionDR__110
        log.debug("check schedule change reservatuion for action ");
        Outputs.ObjSchdlChangeReservationCheckForActionDROut schdlChangeReservationCheckForActionDR = scheduleChangeReservationMethod.schdlChangeReservationCheckForActionDR(objCommon, objSchdlChangeReservationCheckForActionDRIn);

        // Check Min Q-Time restriction
        minQTimeMethod.checkIsRejectByRestriction(objCommon, aLotID);

        //Step23 - qtime_SetClearByOperationComp
        log.debug("[step-23] set qtime clear by operationComp");
        Outputs.ObjQtimeSetClearByOperationCompOut objQtimeSetClearByOperationCompOut = qTimeMethod.qtimeSetClearByOperationComp(objCommon, aLotID);

        // 检查并设置Min Q-Time最小时间限制
        minQTimeMethod.checkAndSetRestrictions(objCommon, aLotID);

        ObjectIdentifier resetReasonCodeID = new ObjectIdentifier();
        resetReasonCodeID.setValue(BizConstant.SP_REASON_QTIMECLEAR);
        log.trace("ArrayUtils.isNotEmpty(objQtimeSetClearByOperationCompOut.getStrLotHoldReleaseList()) : {}", CimArrayUtils.isNotEmpty(objQtimeSetClearByOperationCompOut.getStrLotHoldReleaseList()));
        if (CimArrayUtils.isNotEmpty(objQtimeSetClearByOperationCompOut.getStrLotHoldReleaseList())) {
            Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
            holdLotReleaseReqParams.setHoldReqList(objQtimeSetClearByOperationCompOut.getStrLotHoldReleaseList());
            holdLotReleaseReqParams.setUser(objCommon.getUser());
            holdLotReleaseReqParams.setLotID(aLotID);
            holdLotReleaseReqParams.setReleaseReasonCodeID(resetReasonCodeID);

            //Step24 - txHoldLotReleaseReq
            log.debug("[step-24] sent lot hold release request");
            sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
        }
        log.trace("ArrayUtils.isNotEmpty(objQtimeSetClearByOperationCompOut.getStrFutureHoldCancelList()) : {}", CimArrayUtils.isNotEmpty(objQtimeSetClearByOperationCompOut.getStrFutureHoldCancelList()));
        if (CimArrayUtils.isNotEmpty(objQtimeSetClearByOperationCompOut.getStrFutureHoldCancelList())) {
            Params.FutureHoldCancelReqParams futureHoldCancelReqParams = new Params.FutureHoldCancelReqParams();
            futureHoldCancelReqParams.setLotID(aLotID);
            futureHoldCancelReqParams.setReleaseReasonCodeID(resetReasonCodeID);
            futureHoldCancelReqParams.setUser(objCommon.getUser());
            futureHoldCancelReqParams.setLotHoldList(objQtimeSetClearByOperationCompOut.getStrFutureHoldCancelList());
            futureHoldCancelReqParams.setEntryType(BizConstant.SP_ENTRYTYPE_CANCEL);

            //Step25 - txFutureHoldCancelReq
            log.debug("[step-25] sent fututre hold cancel request");
            processControlService.sxFutureHoldCancelReq(objCommon, futureHoldCancelReqParams);
        }
        //Future Rework Actions
        log.debug("future rework actions");
        List<Infos.FutureReworkInfo> strFutureReworkCancelList = objQtimeSetClearByOperationCompOut.getStrFutureReworkCancelList();
        log.trace("ArrayUtils.isNotEmpty(strFutureReworkCancelList) : {}", CimArrayUtils.isNotEmpty(strFutureReworkCancelList));
        if (CimArrayUtils.isNotEmpty(strFutureReworkCancelList)) {
            for (Infos.FutureReworkInfo futureReworkInfo : strFutureReworkCancelList) {
                //Step26 - txFutureReworkCancelReq
                log.debug("[step-26] future rework canceel request");
                processControlService.sxFutureReworkCancelReq(objCommon, futureReworkInfo.getLotID(), futureReworkInfo.getRouteID(), futureReworkInfo.getOperationNumber(), futureReworkInfo.getFutureReworkDetailInfoList(), "");
            }
        }
        Infos.EffectCondition effectCondition = new Infos.EffectCondition();
        effectCondition.setPhase(BizConstant.SP_FUTUREHOLD_POST);
        effectCondition.setTriggerLevel(BizConstant.SP_FUTUREHOLD_ALL);

        //Step27 - lot_futureHoldRequests_EffectByCondition
        log.debug("[step-27] lot future hold requets effect by condition out");
        Outputs.ObjLotFutureHoldRequestsEffectByConditionOut objLotFutureHoldRequestsEffectByConditionOut = lotMethod.lotFutureHoldRequestsEffectByCondition(objCommon, aLotID, effectCondition);

        // Delete Effected Future Hold Direction
        log.debug("delete Effected Future Hold Direction");
        effectCondition.setPhase(BizConstant.SP_FUTUREHOLD_ALL);
        effectCondition.setTriggerLevel(BizConstant.SP_FUTUREHOLD_ALL);

        //Step28 - lot_futureHoldRequests_DeleteEffectedByCondition
        log.debug("[step-28] delete effect by conditon out");
        Outputs.ObjLotFutureHoldRequestsDeleteEffectedByConditionOut deleteEffectedByConditionOut = lotMethod.lotFutureHoldRequestsDeleteEffectedByCondition(objCommon, aLotID, effectCondition);

        ObjectIdentifier releaseReasonCodeID = new ObjectIdentifier();
        log.trace("ArrayUtils.isNotEmpty(deleteEffectedByConditionOut.getStrFutureHoldReleaseReqList()) : {}", CimArrayUtils.isNotEmpty(deleteEffectedByConditionOut.getStrFutureHoldReleaseReqList()));
        if (CimArrayUtils.isNotEmpty(deleteEffectedByConditionOut.getStrFutureHoldReleaseReqList())) {

            //Step29 - txFutureHoldCancelReq
            log.debug("[step-29] future hold cancle request");
            processControlService.sxFutureHoldCancelReq(objCommon, aLotID, releaseReasonCodeID, BizConstant.SP_ENTRYTYPE_REMOVE, deleteEffectedByConditionOut.getStrFutureHoldReleaseReqList());
        }
        // Update EqpMonitor job information
        log.debug("Update EqpMonitor job information");
        Boolean bOpeCompFlag = false;
        long eqpMonitorSwitch =  StandardProperties.OM_AUTOMON_FLAG.getLongValue();
        Outputs.ObjEquipmentMonitorSectionInfoGetForJobOut objLotEqpMonitorSectionInfoGetForJobOut = new Outputs.ObjEquipmentMonitorSectionInfoGetForJobOut();
        log.trace("1 == eqpMonitorSwitch : {}",1 == eqpMonitorSwitch);
        if (1 == eqpMonitorSwitch) {

            //Step30 - lot_lotType_Get
            log.debug("[step-30] get lot type");
            String lotLotTypeOut = lotMethod.lotTypeGet(objCommon, aLotID);

            log.debug("StringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotLotTypeOut) ||\n" +
                    "                    StringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, lotLotTypeOut) : {}",
                    CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotLotTypeOut) ||
                            CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, lotLotTypeOut));

            if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotLotTypeOut) ||
                    CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, lotLotTypeOut)) {
                Infos.ObjCommon tmpObjCommonIn = objCommon;
                log.trace("TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) ||\n" +
                        "                        TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) ||\n" +
                        "                        TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_REQ, objCommon.getTransactionID()) ||\n" +
                        "                        TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) ||\n" +
                        "                        TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) ||\n" +
                        "                        TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) : {}",
                        TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) ||
                                TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) ||
                                TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_REQ, objCommon.getTransactionID()) ||
                                TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) ||
                                TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) ||
                                TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()));

                if (TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) ||
                        TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) ||
                        TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_REQ, objCommon.getTransactionID()) ||
                        TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID()) ||
                        TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ, objCommon.getTransactionID()) ||
                        TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, objCommon.getTransactionID())) {
                    bOpeCompFlag = true;
                    tmpObjCommonIn.setTransactionID(TransactionIDEnum.GATE_PASS_REQ.getValue());
                }

                //Step31 - lot_eqpMonitorOperationLabel_Get
                log.debug("[step-31] get equipemnt monitor operation lable by lotID");
                List<Infos.EqpMonitorLabelInfo> eqpMonitorLabelInfoList = lotMethod.lotEqpMonitorOperationLabelGet(objCommon, aLotID);
                boolean bMonitorLabel = false;
                log.trace("ArrayUtils.isNotEmpty(eqpMonitorLabelInfoList) : {}", CimArrayUtils.isNotEmpty(eqpMonitorLabelInfoList));
                if (CimArrayUtils.isNotEmpty(eqpMonitorLabelInfoList)) {
                    for (Infos.EqpMonitorLabelInfo eqpMonitorLabelInfo : eqpMonitorLabelInfoList) {
                        log.trace("StringUtils.equals(BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR, eqpMonitorLabelInfo.getOperationLabel()) : {}", CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR, eqpMonitorLabelInfo.getOperationLabel()));
                        if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR, eqpMonitorLabelInfo.getOperationLabel())) {
                            bMonitorLabel = true;
                            break;
                        }
                    }
                }
                log.trace("BooleanUtils.isTrue(bMonitorLabel) : {}", CimBooleanUtils.isTrue(bMonitorLabel));
                if (CimBooleanUtils.isTrue(bMonitorLabel)) {
                    Inputs.ObjEqpMonitorWaferUsedCountUpdateIn objEqpMonitorWaferUsedCountUpdateIn = new Inputs.ObjEqpMonitorWaferUsedCountUpdateIn();
                    objEqpMonitorWaferUsedCountUpdateIn.setLotID(aLotID);
                    objEqpMonitorWaferUsedCountUpdateIn.setAction(BizConstant.SP_EQPMONUSEDCNT_ACTION_INCREMENT);

                    //Step32 - eqpMonitorWaferUsedCountUpdate
                    log.debug("[step-32] update equipment monitor wafer used count");
                    List<Infos.EqpMonitorWaferUsedCount> eqpMonitorWaferUsedCountUpdate = equipmentMethod.eqpMonitorWaferUsedCountUpdate(objCommon, objEqpMonitorWaferUsedCountUpdateIn);

                    // Step33 - eqpMonitorWaferUsedCountUpdateEvent_Make
                    log.debug("[step-33] make event for equipment monitor used count ");
                    Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams objEqpMonitorWaferUsedCountUpdateEventMakeParams = new Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams();
                    objEqpMonitorWaferUsedCountUpdateEventMakeParams.setLotID(aLotID);
                    objEqpMonitorWaferUsedCountUpdateEventMakeParams.setTransactionID("OLOTW005");
                    objEqpMonitorWaferUsedCountUpdateEventMakeParams.setStrEqpMonitorWaferUsedCountList(eqpMonitorWaferUsedCountUpdate);
                    objEqpMonitorWaferUsedCountUpdateEventMakeParams.setClaimMemo(claimMemo);
                    eventMethod.eqpMonitorWaferUsedCountUpdateEventMake(objCommon, objEqpMonitorWaferUsedCountUpdateEventMakeParams);
                }

                //Step34 - lot_eqpMonitorSectionInfo_GetForJob
                log.debug("[step-33] get equipment monitor section info for job by lotID");
                Outputs.ObjEquipmentMonitorSectionInfoGetForJobOut equipmentMonitorSectionInfo = lotMethod.lotEqpMonitorSectionInfoGetForJob(objCommon, aLotID);
                log.debug("ObjectUtils.isNotEmpty(equipmentMonitorSectionInfo.getEquipmentMonitorJobID()) && objLotEqpMonitorSectionInfoGetForJobOut.isExitFlag() : {}",
                        CimObjectUtils.isNotEmpty(equipmentMonitorSectionInfo.getEquipmentMonitorJobID()) && objLotEqpMonitorSectionInfoGetForJobOut.isExitFlag());

                if (CimObjectUtils.isNotEmpty(equipmentMonitorSectionInfo.getEquipmentMonitorJobID()) && objLotEqpMonitorSectionInfoGetForJobOut.isExitFlag()) {

                    //Step35 - eqpMonitorJob_lot_Update
                    log.debug("[step-35] update equipment monitor job");
                    equipmentMethod.eqpMonitorJobLotUpdate(objCommon, aLotID, BizConstant.SP_EQPMONITORJOB_OPECATEGORY_OPESTARTCANCEL);
                    log.trace("BooleanUtils.isFalse(bOpeCompFlag) : {}", CimBooleanUtils.isFalse(bOpeCompFlag));
                    if (CimBooleanUtils.isFalse(bOpeCompFlag)) {
                        //TODO-NOTIMPL:Step36 - txAMJObLotDeleteReq
                    }
                }
            }
        }

        //update contanmination flag
        log.debug("update contanmination flag");
        contaminationMethod.lotContaminationLevelAndPrFlagSet(aLotID);

        //Step37 - process_Move
        log.debug("[step-37] process move");
        processMethod.processMove(objCommon, aLotID);
        log.trace("!ObjectUtils.isEmpty(objLotEqpMonitorSectionInfoGetForJobOut.getEquipmentMonitorJobID()) && objLotEqpMonitorSectionInfoGetForJobOut.isExitFlag() && BooleanUtils.isTrue(bOpeCompFlag) : {}",
                !CimObjectUtils.isEmpty(objLotEqpMonitorSectionInfoGetForJobOut.getEquipmentMonitorJobID()) && objLotEqpMonitorSectionInfoGetForJobOut.isExitFlag() && CimBooleanUtils.isTrue(bOpeCompFlag));

        if (!CimObjectUtils.isEmpty(objLotEqpMonitorSectionInfoGetForJobOut.getEquipmentMonitorJobID()) && objLotEqpMonitorSectionInfoGetForJobOut.isExitFlag() && CimBooleanUtils.isTrue(bOpeCompFlag)) {
            //TODO:Step38 - txAMJObLotDeleteReq
        }

        //Step39 - monitorGroup_DeleteComp
        log.debug("[step-39] delete monitor group comp");
        Outputs.ObjMonitorGroupDeleteCompOut monitorGroupDeleteComp = monitorGroupMethod.monitorGroupDeleteComp(objCommon, aLotID);

        ObjectIdentifier aReasonCodeID = new ObjectIdentifier();
        aReasonCodeID.setValue(BizConstant.SP_REASON_WAITINGMONITORHOLDRELEASE);
        List<Infos.MonitoredCompLots> monitoredCompLotsList = monitorGroupDeleteComp.getMonitoredCompLotsList();
        long holdReleasedLotCount = 0;
        log.trace("ArrayUtils.isNotEmpty(monitoredCompLotsList) : {}", CimArrayUtils.isNotEmpty(monitoredCompLotsList));
        if (CimArrayUtils.isNotEmpty(monitoredCompLotsList)) {
            for (Infos.MonitoredCompLots monitoredCompLots : monitoredCompLotsList) {
                log.trace("ArrayUtils.isNotEmpty(monitoredCompLots.getStrLotHoldReleaseReqList()) : {}", CimArrayUtils.isNotEmpty(monitoredCompLots.getStrLotHoldReleaseReqList()));
                if (CimArrayUtils.isNotEmpty(monitoredCompLots.getStrLotHoldReleaseReqList())) {
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(monitoredCompLots.getProductLotID());
                    holdLotReleaseReqParams.setReleaseReasonCodeID(aReasonCodeID);
                    holdLotReleaseReqParams.setUser(objCommon.getUser());
                    holdLotReleaseReqParams.setHoldReqList(monitoredCompLots.getStrLotHoldReleaseReqList());

                    //Step40 - txHoldLotReleaseReq
                    log.debug("lot hold release reuqest");
                    sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);

                    //Return Hold Released LotID for Caller
                    log.debug("return hold release lotID for caller");
                    holdReleasedLotIDs.add(monitoredCompLots.getProductLotID());
                    //  Get Lot Type of Monitoring Lot

                    //Step41 - lot_type_Get
                    log.debug("Get Lot Type of Monitoring Lot");
                    String lotType = null;
                    try {
                        lotType = lotMethod.lotTypeGet(objCommon, aLotID);
                    } catch (ServiceException e) {
                        continue;
                    }

                    log.trace("StringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONLOT, lotType) : {}", CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONLOT, lotType));
                    if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONLOT, lotType)) {
                        while (true) {

                            //Step42 - repeatGatePass_CheckCondition
                            log.debug("[step-42] check repeat gate pass condition");
                            Boolean repeatGatePassCheckCondition = processMethod.repeatGatePassCheckCondition(objCommon, aLotID, monitoredCompLots.getProductLotID());
                            log.trace("BooleanUtils.isFalse(repeatGatePassCheckCondition) : {}", CimBooleanUtils.isFalse(repeatGatePassCheckCondition));
                            if (CimBooleanUtils.isFalse(repeatGatePassCheckCondition)) {
                                break;
                            }
                            //  Get Monitored Lot's Info

                            //Step43 - lot_currentOperationInfo_Get

                            log.debug("[step-43] get lot current operation info");
                            Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoOut = lotMethod.lotCurrentOperationInfoGet(objCommon, monitoredCompLots.getProductLotID());

                            Infos.GatePassLotInfo gatePassLotInfo = new Infos.GatePassLotInfo();
                            gatePassLotInfo.setLotID(monitoredCompLots.getProductLotID());
                            gatePassLotInfo.setCurrentRouteID(lotCurrentOperationInfoOut.getRouteID());
                            gatePassLotInfo.setCurrentOperationNumber(lotCurrentOperationInfoOut.getOperationNumber());
                            //  Call txPassThruReq()

                            //Step44 - txPassThruReq
                            log.debug("[step-44] pass thru reuqest");
                            sxPassThruReq(objCommon, gatePassLotInfo, claimMemo);
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        //Lot FutureHold Post By Previous Operation Effect after Operation Move
        log.debug("Lot FutureHold Post By Previous Operation Effect after Operation Move");
        log.trace("ArrayUtils.isNotEmpty(objLotFutureHoldRequestsEffectByConditionOut.getStrLotHoldReqList()) : {}", CimArrayUtils.isNotEmpty(objLotFutureHoldRequestsEffectByConditionOut.getStrLotHoldReqList()));
        if (CimArrayUtils.isNotEmpty(objLotFutureHoldRequestsEffectByConditionOut.getStrLotHoldReqList())) {

            //Step45 - lot_futureHold_EffectedProcessConversion
            log.debug("[step-45] lot future hold effected process conversion");
            Outputs.ObjLotFutureHoldEffectedProcessConversionOut objLotFutureHoldEffectedProcessConversionOut = lotMethod.lotFutureHoldEffectedProcessConversion(objCommon, objLotFutureHoldRequestsEffectByConditionOut.getLotID(), objLotFutureHoldRequestsEffectByConditionOut.getStrLotHoldReqList());


            //Step46 - txHoldLotReq
            log.debug("[step-46] lot hold request");
            sxHoldLotReq(objCommon, objLotFutureHoldRequestsEffectByConditionOut.getLotID(), objLotFutureHoldEffectedProcessConversionOut.getLotHoldReqList());
        }
        effectCondition.setPhase(BizConstant.SP_FUTUREHOLD_PRE);
        effectCondition.setTriggerLevel(BizConstant.SP_FUTUREHOLD_ALL);

        //Step47 - lot_futureHoldRequests_EffectByCondition
        log.debug("[step-47] lot future hold request effect by conditon");
        objLotFutureHoldRequestsEffectByConditionOut = lotMethod.lotFutureHoldRequestsEffectByCondition(objCommon, aLotID, effectCondition);
        log.trace("ArrayUtils.isNotEmpty(objLotFutureHoldRequestsEffectByConditionOut.getStrLotHoldReqList()) : {}", CimArrayUtils.isNotEmpty(objLotFutureHoldRequestsEffectByConditionOut.getStrLotHoldReqList()));
        if (CimArrayUtils.isNotEmpty(objLotFutureHoldRequestsEffectByConditionOut.getStrLotHoldReqList())) {

            //Step48 - txHoldLotReq
            log.debug("[step-48] lot future hold request effect by conditon out");
            sxHoldLotReq(objCommon, aLotID, objLotFutureHoldRequestsEffectByConditionOut.getStrLotHoldReqList());
        }

        effectCondition.setPhase(BizConstant.SP_FUTUREHOLD_PRE);
        effectCondition.setTriggerLevel(BizConstant.SP_FUTUREHOLD_SINGLE);

        //Step49 - lot_futureHoldRequests_DeleteEffectedByCondition
        log.debug("[step-49] lot future request delete effected by condition out");
        Outputs.ObjLotFutureHoldRequestsDeleteEffectedByConditionOut objLotFutureHoldRequestsDeleteEffectedByConditionOut = lotMethod.lotFutureHoldRequestsDeleteEffectedByCondition(objCommon, aLotID, effectCondition);

        log.trace("ArrayUtils.isNotEmpty(objLotFutureHoldRequestsDeleteEffectedByConditionOut.getStrFutureHoldReleaseReqList()) : {}",
                CimArrayUtils.isNotEmpty(objLotFutureHoldRequestsDeleteEffectedByConditionOut.getStrFutureHoldReleaseReqList()));

        if (CimArrayUtils.isNotEmpty(objLotFutureHoldRequestsDeleteEffectedByConditionOut.getStrFutureHoldReleaseReqList())) {

            //Step50 - txFutureHoldCancelReq
            log.debug("[step-50] future hold cancel request");
            processControlService.sxFutureHoldCancelReq(objCommon, aLotID, releaseReasonCodeID, BizConstant.SP_ENTRYTYPE_REMOVE, objLotFutureHoldRequestsDeleteEffectedByConditionOut.getStrFutureHoldReleaseReqList());
        }
        //Update Cassette's MultiLotType

        //Step51 - cassette_multiLotType_Update
        log.debug("[step-51] Update Cassette's MultiLotType");
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, aCassetteID);

        // UpDate RequiredCassetteCategory

        //Step52 - lot_CassetteCategory_UpdateForContaminationControl
        log.debug("[step-52] UpDate Required lot CassetteCategory for contaminaltion control");
        try {
            lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, aLotID);
        } catch (ServiceException e) {
            gatePassLotsResult.setReturnCode(String.valueOf(e.getCode()));
            throw e;
        }

        Params.LagTimeActionReqParams lagTimeActionReqParams = new Params.LagTimeActionReqParams();
        lagTimeActionReqParams.setLotID(aLotID);
        lagTimeActionReqParams.setClaimMemo(claimMemo);
        lagTimeActionReqParams.setUser(objCommon.getUser());
        lagTimeActionReqParams.setAction(BizConstant.SP_PROCESSLAGTIME_ACTION_SET);

        //Step53 - txLagTimeActionReq
        log.debug("[step-53] update process log time");
        processControlService.sxProcessLagTimeUpdate(objCommon, lagTimeActionReqParams);
        //Process Hold

        //Step54 - txProcessHoldDoActionReq
        log.debug("[step-54] process hold do action request");
        processControlService.sxProcessHoldDoActionReq(objCommon, aLotID, claimMemo);

        //Step55 - lotOperationMoveEvent_MakeGatePass
        log.debug("[step-55] make event lot operation move gate pass");
        eventMethod.lotOperationMoveEventMakeGatePass(objCommon, "OLOTW005", aLotID, claimMemo);

        //Execute Future Action Procedure
        log.debug("Execute Future Action Procedure");
        log.trace("schdlChangeReservationCheckForActionDR.isExistFlag() : {}",schdlChangeReservationCheckForActionDR.isExistFlag());
        if (schdlChangeReservationCheckForActionDR.isExistFlag()) {
            RetCode<List<Infos.ChangeLotSchdlReturn>> strChangeLotSchdlReturn = new RetCode<>();

            List<Infos.ReScheduledLotAttributes> strRescheduledLotAttributes = new ArrayList<>();
            Infos.ReScheduledLotAttributes rescheduledLotAttributes = new Infos.ReScheduledLotAttributes();
            rescheduledLotAttributes.setLotID(aLotID);
            rescheduledLotAttributes.setProductID(schdlChangeReservationCheckForActionDR.getStrSchdlChangeReservation().getProductID());
            rescheduledLotAttributes.setRouteID(schdlChangeReservationCheckForActionDR.getStrSchdlChangeReservation().getRouteID());
            rescheduledLotAttributes.setCurrentOperationNumber(schdlChangeReservationCheckForActionDR.getStrSchdlChangeReservation().getOperationNumber());
            rescheduledLotAttributes.setSubLotType(schdlChangeReservationCheckForActionDR.getStrSchdlChangeReservation().getSubLotType());
            strRescheduledLotAttributes.add(rescheduledLotAttributes);

            // Get current RouteID by lotID

            //Step56 - lot_currentRouteID_Get
            log.debug("[step-56] get lot current routeID");
            ObjectIdentifier strLotCurrentRouteIDGetOut = lotMethod.lotCurrentRouteIDGet(objCommon, aLotID);

            rescheduledLotAttributes.setOriginalRouteID(strLotCurrentRouteIDGetOut.getValue());

            // Get current oparation No. by lotID

            //Step57 - lot_currentOpeNo_Get
            log.debug("[step-57] get lot curretn operation no");
            String lotCurrentOperationNumber = lotMethod.lotCurrentOpeNoGet(objCommon, aLotID);

            rescheduledLotAttributes.setOriginalOperationNumber(lotCurrentOperationNumber);

            // Step58 - txLotPlanChangeReserveDoActionReq__110
            log.debug("[step-58] lot plan change reserce do action request");
            planService.sxLotPlanChangeReserveDoActionReq(objCommon, strRescheduledLotAttributes, schdlChangeReservationCheckForActionDR.getStrSchdlChangeReservation().getEventID());

            // Step59 - schdlChangeReservation_applyCount_IncreaseDR__110
            log.debug("[step-59] schedule change reservation apply count in crease ");
            scheduleChangeReservationMethod.schdlChangeReservationApplyCountIncreaseDR(objCommon, schdlChangeReservationCheckForActionDR.getStrSchdlChangeReservation());

        }

        //Step60 - lot_CheckConditionForAutoBankIn
        log.debug("[step-60] check lot conditon for auto bank in");
        boolean objLotCheckConditionForAutoBankInOut = lotMethod.lotCheckConditionForAutoBankIn(objCommon, aLotID);
        log.trace("objLotCheckConditionForAutoBankInOut : {}",objLotCheckConditionForAutoBankInOut);
        if (objLotCheckConditionForAutoBankInOut) {
            List<ObjectIdentifier> lotID = new ArrayList<>();
            lotID.add(strGatePassLotInfo.getLotID());

            //Step61 - txBankInReq
            log.debug("[step-61] bank in request");
            try {
                bankService.sxBankInReq(objCommon, 0, lotID, claimMemo);
            } catch (ServiceException e) {
                log.trace("!Validations.isEquals(retCodeConfig.getInvalidLotHoldStat(), e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getInvalidLotHoldStat(), e.getCode()));
                if (!Validations.isEquals(retCodeConfig.getInvalidLotHoldStat(), e.getCode())) {
                    throw e;
                }
            }
        }

        // step 62 check contamination level , lot hold
        log.debug("[step-62] check contamination level , lot hold ");
        contaminationMethod.lotCheckContaminationLevelAndPrFlagStepOut(objCommon, aLotID);

        return retVal;
    }

    public void sxBranchReq(Infos.ObjCommon objCommon, Infos.BranchReq branchReq, String claimMemo) {
        log.info("BranchReq = {}", branchReq);
        //--------------------------------
        //   Decide TX_ID
        //--------------------------------
        Boolean bDynamicRoute = branchReq.getBDynamicRoute();
        String eventTxId = branchReq.getEventTxId();
        String txID = !CimStringUtils.isEmpty(eventTxId) ? eventTxId : (CimBooleanUtils.isTrue(bDynamicRoute) ? TransactionIDEnum.BRANCH_REQ.getValue() : TransactionIDEnum.SUB_ROUTE_BRANCH_REQ.getValue());
        log.debug("Decide TX_ID = {}", txID);
        //------------------------------------------------------------------------
        //   Get cassette / lot connection
        //------------------------------------------------------------------------
        log.debug(" Get cassette / lot connection");
        ObjectIdentifier aCassetteID = lotMethod.lotCassetteGet(objCommon, branchReq.getLotID());
        log.debug("getLotCassette().cassetteID = {}", aCassetteID);
        //--------------------------------
        //   Lock objects to be updated
        //--------------------------------
        log.debug("Lock objects to be updated");
        String strParallelPostProcFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG);
        //----------------------------------------------------------//
        //   Skip cassette lock to increase parallel availability   //
        //   under PostProcess parallel execution                   //
        //----------------------------------------------------------//
        log.debug("Skip cassette lock to increase parallel availability");
        log.trace("!StringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON) : {}",!CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON));
        if (!CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON)){
            // object_Lock
            objectLockMethod.objectLock(objCommon, CimCassette.class, aCassetteID);
        }

        objectLockMethod.objectLock(objCommon, CimLot.class, branchReq.getLotID());

        //Check whether the PDType of route matches with Process
        ObjectIdentifier subRouteID = branchReq.getSubRouteID();
        processMethod.checkProcessDefinitionType(subRouteID, BizConstant.SP_MAINPDTYPE_BRANCH);

        //--------------------------------------------------------------------------
        //   Check whether lot is on the specified Route/Operation or Not
        //--------------------------------------------------------------------------
        log.debug("Check whether lot is on the specified Route/Operation or Not");
        ObjectIdentifier tmpCurrentRouteID = branchReq.getCurrentRouteID();
        String tmpCurrentOperationNumber = branchReq.getCurrentOperationNumber();
        ObjectIdentifier lotID = branchReq.getLotID();
        log.trace("!ObjectUtils.isEmptyWithValue(tmpCurrentRouteID) && !StringUtils.isEmpty(tmpCurrentOperationNumber) : {}",!ObjectIdentifier.isEmptyWithValue(tmpCurrentRouteID) && !CimStringUtils.isEmpty(tmpCurrentOperationNumber));
        if (!ObjectIdentifier.isEmptyWithValue(tmpCurrentRouteID) && !CimStringUtils.isEmpty(tmpCurrentOperationNumber)) {
            Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);
            Validations.check (!ObjectIdentifier.equalsWithValue(tmpCurrentRouteID, lotCurrentOperationInfoGetOut.getRouteID())
                            || !tmpCurrentOperationNumber.equals(lotCurrentOperationInfoGetOut.getOperationNumber()),
                    new OmCode(retCodeConfig.getNotSameRoute(), "Input parameter's currentRouteID/currentOperationNumber", "lot's current currentRouteID/currentOperationNumber"));

        }
        //------------------------------------------------------------------------
        //   Check Condition
        //------------------------------------------------------------------------
        log.debug("Check Condition");
        String lotHoldState = lotMethod.lotHoldStateGet(objCommon, branchReq.getLotID());
        Validations.check (CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, lotHoldState),
                new OmCode(retCodeConfig.getInvalidLotHoldStat(), branchReq.getLotID().getValue(), lotHoldState));
        String lotState = lotMethod.lotStateGet(objCommon, branchReq.getLotID());
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotState), new OmCode(retCodeConfig.getInvalidLotStat(), lotState));
        String theLotProcessState = lotMethod.lotProcessStateGet(objCommon, branchReq.getLotID());
        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_WAITING, theLotProcessState),
                new OmCode(retCodeConfig.getInvalidLotProcessState(), branchReq.getLotID().getValue(), theLotProcessState));
        String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, branchReq.getLotID());
        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, lotInventoryState),
                retCodeConfig.getInvalidLotInventoryStat(), ObjectIdentifier.fetchValue(branchReq.getLotID()), lotInventoryState);

        //----------------------------------
        //  Get InPostProcessFlag of lot
        //----------------------------------
        // lot_inPostProcessFlag_Get
        log.debug("Get InPostProcessFlag of lot");
        Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, branchReq.getLotID());
        //----------------------------------------------
        //  If Lot is in post process, returns error
        //----------------------------------------------
        log.debug("If Lot is in post process, returns error");
        log.trace("objLotInPostProcessFlagOut.getInPostProcessFlagOfLot() : {}",objLotInPostProcessFlagOut.getInPostProcessFlagOfLot());
        if (objLotInPostProcessFlagOut.getInPostProcessFlagOfLot()){
            /*---------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            log.debug("Get UserGroupID By UserID");
            List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
            int userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);
            int nCnt = 0;
            for (nCnt = 0; nCnt < userGroupIDsLen; nCnt++){
            }
            Validations.check(nCnt == userGroupIDsLen, new OmCode(retCodeConfig.getLotInPostProcess(), branchReq.getLotID().getValue()));
        }
        //---------------------------------------
        // Check interFabXferPlan existence
        //---------------------------------------
        //TODO-NOTIMPL: process_CheckInterFabXferPlanSkip

        //----------------------------------
        //   Check lot's Control Job ID
        //----------------------------------
        log.debug("Check lot's Control Job ID");
        ObjectIdentifier controlJobID = lotMethod.lotControlJobIDGet(objCommon, lotID);
        log.trace("!ObjectUtils.isEmptyWithValue(controlJobID) : {}",!ObjectIdentifier.isEmptyWithValue(controlJobID));
        if (!ObjectIdentifier.isEmptyWithValue(controlJobID)){
            Validations.check(retCodeConfig.getLotControlJobidFilled(), ObjectIdentifier.fetchValue(branchReq.getLotID()),
                    ObjectIdentifier.fetchValue(controlJobID));
        }
        //----------------------------------------------------------
        //   Convers input subrouteID if it has the version ##
        //----------------------------------------------------------
        log.debug("Convers input subrouteID if it has the version ##");
        ObjectIdentifier aSubRouteID = processMethod.processActiveIDGet(objCommon, branchReq.getSubRouteID());
        log.debug("processActiveIDOut().aSubRouteID = {}", aSubRouteID);

        //-----------------------------------
        //   Check Route is Dynamic or Not
        //-----------------------------------
        log.debug("Check Route is Dynamic or Not");
        if (CimBooleanUtils.isTrue(bDynamicRoute)) {
            Outputs.ObjProcessCheckForDynamicRouteOut dynamicRouteOut = processMethod.processCheckForDynamicRoute(objCommon, aSubRouteID);
            Validations.check (CimBooleanUtils.isFalse(dynamicRouteOut.getDynamicRouteFlag()), retCodeConfig.getNotDynamicRoute());
        }
        //-------------------------------------------------
        //   Check Input return operation number
        //-------------------------------------------------
        log.debug("Check Input return operation number");
        boolean inputReturnOperationFlag = false;
        String returnOperationNumberVar = null;
        String returnOperationNumber = branchReq.getReturnOperationNumber();
        log.trace("!StringUtils.isEmpty(returnOperationNumber) : {}",!CimStringUtils.isEmpty(returnOperationNumber));
        if (!CimStringUtils.isEmpty(returnOperationNumber)) {
            inputReturnOperationFlag = true;
        }
        //------------------------------------------------------------------------
        //   Check Input return operation number is exist in connected route list
        //------------------------------------------------------------------------
        log.debug("Check Input return operation number is exist in connected route list");
        boolean connectedRouteReturnOperationFlag = true;
        boolean sameReturnOperationExistFlag = false;
        Outputs.ObjProcessCheckForDynamicRouteOut dynamicRouteOut = processMethod.processCheckForDynamicRoute(objCommon, aSubRouteID);
        Outputs.ObjProcessGetReturnOperationOut processGetReturnOperationOut = null;
        try{
            processGetReturnOperationOut = processMethod.processGetReturnOperation(objCommon, lotID, aSubRouteID);
        }catch (ServiceException ex){
            log.trace("Validations.isEquals(retCodeConfig.getNotFoundSubRoute(), ex.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotFoundSubRoute(), ex.getCode()));
            if (Validations.isEquals(retCodeConfig.getNotFoundSubRoute(), ex.getCode())) {
                connectedRouteReturnOperationFlag = false;
                processGetReturnOperationOut = (Outputs.ObjProcessGetReturnOperationOut) ex.getData();
            }else {
                throw ex;
            }
        }
        log.trace("StringUtils.equals(processGetReturnOperationOut.getOperationNumber(), returnOperationNumber) : {}", CimStringUtils.equals(processGetReturnOperationOut.getOperationNumber(), returnOperationNumber));
        log.trace("BooleanUtils.isFalse(dynamicRouteOut.getDynamicRouteFlag()) && !StringUtils.isEmpty(returnOperationNumber) : {}", CimBooleanUtils.isFalse(dynamicRouteOut.getDynamicRouteFlag()) && !CimStringUtils.isEmpty(returnOperationNumber));
        if (CimStringUtils.equals(processGetReturnOperationOut.getOperationNumber(), returnOperationNumber)) {
            sameReturnOperationExistFlag = true;
        } else if (CimBooleanUtils.isFalse(dynamicRouteOut.getDynamicRouteFlag()) && !CimStringUtils.isEmpty(returnOperationNumber)) {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }
        //----------------------------------------------
        // Get Currrent Route ID and Operation Number
        //----------------------------------------------
        log.debug("Get Currrent Route ID and Operation Number");
        Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);
        //-------------------------------------------------
        // Decide return operation number using all flags
        //-------------------------------------------------
        log.debug("decide return operation number using all flags ");
        log.trace("BooleanUtils.isTrue(bDynamicRoute) : {}", CimBooleanUtils.isTrue(bDynamicRoute));
        if (CimBooleanUtils.isTrue(bDynamicRoute)) {
            log.debug("bDynamicRoute == true");
            log.trace("inputReturnOperationFlag : {}",inputReturnOperationFlag);
            if (inputReturnOperationFlag) {
                log.debug("inputReturnOperationFlag == true");
                returnOperationNumberVar = returnOperationNumber;
            } else {
                log.debug("inputReturnOperationFlag == false");
                log.trace("connectedRouteReturnOperationFlag : {}",connectedRouteReturnOperationFlag);
                if (connectedRouteReturnOperationFlag) {
                    log.debug("connectedRouteReturnOperationFlag == true");
                    returnOperationNumberVar = processGetReturnOperationOut.getOperationNumber();
                } else {
                    log.debug("connectedRouteReturnOperationFlag == false");
                    returnOperationNumberVar = lotCurrentOperationInfoGetOut.getOperationNumber();
                }
            }
        } else {
            log.debug("bDynamicRoute == false");
            log.trace("connectedRouteReturnOperationFlag : {}",connectedRouteReturnOperationFlag);
            if (connectedRouteReturnOperationFlag) {
                log.info("connectedRouteReturnOperationFlag == true");
                log.trace("inputReturnOperationFlag : {}",inputReturnOperationFlag);
                if (inputReturnOperationFlag) {
                    log.debug("inputReturnOperationFlag == true");
                    returnOperationNumberVar = returnOperationNumber;
                } else {
                    log.debug("inputReturnOperationFlag == false");
                    returnOperationNumberVar = processGetReturnOperationOut.getOperationNumber();
                }
            } else {
                log.error("connectedRouteReturnOperationFlag == false");
                Validations.check(retCodeConfig.getInvalidRouteId());
            }
        }
        log.debug("returnOperationNumberVar = {}", returnOperationNumberVar);
        //----------------------------------------------------
        //   Check ProcessDefinitionType is 'BRANCH' or not
        //----------------------------------------------------
        log.debug("Check ProcessDefinitionType is 'BRANCH' or not");
        Validations.check (!CimStringUtils.equals(BizConstant.SP_MAINPDTYPE_BRANCH, processGetReturnOperationOut.getProcessDefinitionType()),
                retCodeConfig.getInvalidRouteType());

        //-----------------------------------------------------------
        //   Check decided return operation is exist on current route
        //-----------------------------------------------------------
        log.debug("Check decided return operation is exist on current route");
        List<Infos.OperationNameAttributes> operationNameAttributesList = lotInqService.sxProcessFlowOperationListForLotInq(objCommon, lotID);
        int opeLen = CimArrayUtils.getSize(operationNameAttributesList);
        for (int i = 0; i < opeLen; i++) {
            log.trace("operationNameAttributesList.get(i).getOperationNumber().equals(returnOperationNumberVar) : {}",operationNameAttributesList.get(i).getOperationNumber().equals(returnOperationNumberVar));
            log.trace("i == opeLen - 1 : {}",i == opeLen - 1);
            if (operationNameAttributesList.get(i).getOperationNumber().equals(returnOperationNumberVar)) {
                break;
            } else if (i == opeLen - 1) {
                log.error("not found operation");
                Validations.check(retCodeConfig.getNotFoundOperation(), returnOperationNumberVar);
            }
        }
        // ------------------------------------------------------------
        // Check routeID confliction
        //   return RC_INVALID_BRANCH_ROUTEID,
        //   when the same routeID is used in the following case
        //       ex) Subroute --> The same SubRoute in the course
        // ------------------------------------------------------------
        log.debug("Check routeID confliction");
        Outputs.ObjLotOriginalRouteListGetOut lotOriginalRouteListGetOut = lotMethod.lotOriginalRouteListGet(objCommon, lotID);

        //Check CurrentRoute VS SubRoute
        log.debug("Check CurrentRoute VS SubRoute");
        Validations.check(ObjectIdentifier.equalsWithValue(lotCurrentOperationInfoGetOut.getRouteID(), aSubRouteID), retCodeConfig.getInvalidBranchRouteId());

        //Check Afetr Route VS SubRoute
        log.debug("Check Afetr Route VS SubRoute");
        List<ObjectIdentifier> originalRouteIDs = lotOriginalRouteListGetOut.getOriginalRouteID();
        for (ObjectIdentifier originalRouteID : originalRouteIDs) {
            Validations.check(ObjectIdentifier.equalsWithValue(originalRouteID, aSubRouteID), retCodeConfig.getInvalidBranchRouteId());
        }
        //---------------------------------
        //   Check Future Hold
        //---------------------------------
        log.debug("Check Future Hold");
        lotMethod.lotFutureHoldRequestsCheckBranch(objCommon, lotID, returnOperationNumberVar);

        /*-----------------------------------*/
        /*   Check Future Action Procedure   */
        /*-----------------------------------*/
        // schdlChangeReservation_CheckForFutureOperation
        log.debug("Check Future Action Procedure");
        String tmpRouteID = lotCurrentOperationInfoGetOut.getRouteID().getValue();
        scheduleChangeReservationMethod.schdlChangeReservationCheckForFutureOperation(objCommon, lotID, tmpRouteID, returnOperationNumberVar);
        int lotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getIntValue();
        log.trace("lotOperationEIcheck == 1 : {}",lotOperationEIcheck == 1);
        if (lotOperationEIcheck == 1) {
            //-------------------------------------
            // Check cassette Xfer Stat
            // Does not Check OpeComp, ForceOpeComp
            //  and OpeStartCancel -- D7000213
            //-------------------------------------
            log.debug("Check cassette Xfer Stat Does not Check OpeComp, ForceOpeComp");
            String transactionID = objCommon.getTransactionID();
            log.trace("!TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, transactionID)\n" +
                    "                    && !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_REQ, transactionID)\n" +
                    "                    && !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, transactionID)\n" +
                    "                    && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, transactionID)\n" +
                    "                    && !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ, transactionID)\n" +
                    "                    && !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, transactionID)\n" +
                    "                    && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_REQ, transactionID)\n" +
                    "                    && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ, transactionID) : {}",
                    !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, transactionID)
                            && !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_REQ, transactionID)
                            && !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, transactionID)
                            && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, transactionID)
                            && !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ, transactionID)
                            && !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, transactionID)
                            && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_REQ, transactionID)
                            && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ, transactionID));

            if (!TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ, transactionID)
                    && !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_REQ, transactionID)
                    && !TransactionIDEnum.equals(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, transactionID)
                    && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, transactionID)
                    && !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ, transactionID)
                    && !TransactionIDEnum.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ, transactionID)
                    && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_REQ, transactionID)
                    && !TransactionIDEnum.equals(TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ, transactionID)) {

                String transferState = cassetteMethod.cassetteTransferStateGet(objCommon, aCassetteID);
                Validations.check(CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState),
                        retCodeConfig.getInvalidCassetteTransferState(), transferState, aCassetteID.getValue());
            }
        }
        //--------------------------------------------------------------------------------------------------
        // Check Flow Batch Condition.
        //
        // FlowBatched lot :
        //   Can NOT Branch on a Branch Route which returns outside the flow batch section.
        //   Can NOT Branch on a Branch Route which returns to the operation beyond a target process.
        // Not FlowBatched lot :
        //   Can NOT Branch on a Branch Route which returns to the flow batch section.
        //--------------------------------------------------------------------------------------------------
        //lot_CheckFlowBatchConditionForBranch
        log.debug("Check Flow Batch Condition.");
        log.trace("ObjectUtils.isEmptyWithValue(branchReq.getCurrentRouteID()) : {}", ObjectIdentifier.isEmptyWithValue(branchReq.getCurrentRouteID()));
        if (ObjectIdentifier.isEmptyWithValue(branchReq.getCurrentRouteID())) {
            branchReq.setCurrentRouteID(lotCurrentOperationInfoGetOut.getRouteID());
        }
        log.trace("StringUtils.isEmpty(branchReq.getCurrentOperationNumber()) : {}", CimStringUtils.isEmpty(branchReq.getCurrentOperationNumber()));
        if (CimStringUtils.isEmpty(branchReq.getCurrentOperationNumber())) {
            branchReq.setCurrentOperationNumber(lotCurrentOperationInfoGetOut.getOperationNumber());
        }
        log.trace("StringUtils.isEmpty(branchReq.getReturnOperationNumber()) : {}", CimStringUtils.isEmpty(branchReq.getReturnOperationNumber()));
        if (CimStringUtils.isEmpty(branchReq.getReturnOperationNumber())) {
            branchReq.setReturnOperationNumber(processGetReturnOperationOut.getOperationNumber());
        }
        try {
            log.debug("check lot flow batch condtion for banch");
            lotMethod.lotCheckFlowBatchConditionForBranch(objCommon, branchReq);
        } catch (ServiceException e) {
            log.trace("Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode())\n" +
                    "                    || Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode())\n" +
                    "                    || Validations.isEquals(retCodeConfig.getNotLocateToOverTarget(), e.getCode()) : {}",
                    Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode())
                            || Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode())
                            || Validations.isEquals(retCodeConfig.getNotLocateToOverTarget(), e.getCode()));

            if (Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getNotLocateToOverTarget(), e.getCode())){
                StringBuilder msgSb = new StringBuilder();
                log.trace("Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode()));
                log.trace("Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode()));
                log.trace("Validations.isEquals(retCodeConfig.getNotLocateToOverTarget(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotLocateToOverTarget(), e.getCode()));

                if (Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode())){
                    msgSb.append("Return point of the branch route is in a FlowBatch Section.");
                } else if (Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode())){
                    msgSb.append("FlowBatched lots cannot go out of the FlowBatch Section.");
                } else if (Validations.isEquals(retCodeConfig.getNotLocateToOverTarget(), e.getCode())){
                    msgSb.append("FlowBatched lots cannot jump over the target operation in the FlowBatch Section.");
                }
                Validations.check(retCodeConfig.getNotBranchBatchOpe(), msgSb.toString());
            } else {
                throw e;
            }
        }
        //--------------------------------------------------------------------------------------------------
        // Check Bonding Flow Condition.
        //--------------------------------------------------------------------------------------------------
        log.debug("Check Bonding Flow Condition");
        Infos.BranchReq strBranchReq = branchReq;
        /* ------------------------------------------ */
        /*  Check input parameters are empty or not.  */
        /* ------------------------------------------ */
        log.debug("Check input parameters are empty or not");
        log.trace("ObjectUtils.isEmptyWithValue(branchReq.getCurrentRouteID()) : {}", ObjectIdentifier.isEmptyWithValue(branchReq.getCurrentRouteID()));
        if (ObjectIdentifier.isEmptyWithValue(branchReq.getCurrentRouteID())){
            strBranchReq.setCurrentRouteID(lotCurrentOperationInfoGetOut.getRouteID());
        }
        log.trace("StringUtils.isEmpty(branchReq.getCurrentOperationNumber()) : {}", CimStringUtils.isEmpty(branchReq.getCurrentOperationNumber()));
        if (CimStringUtils.isEmpty(branchReq.getCurrentOperationNumber())){
            strBranchReq.setCurrentOperationNumber(lotCurrentOperationInfoGetOut.getOperationNumber());
        }
        log.trace("StringUtils.isEmpty(branchReq.getReturnOperationNumber()) : {}", CimStringUtils.isEmpty(branchReq.getReturnOperationNumber()));
        if (CimStringUtils.isEmpty(branchReq.getReturnOperationNumber())){
            strBranchReq.setReturnOperationNumber(processGetReturnOperationOut.getOperationNumber());
        }
        //lot_CheckBondingFlowSectionForBranch
        log.debug("check bonding flow section for branch");
        try {
            lotMethod.lotCheckBondingFlowSectionForBranch(objCommon, branchReq);
        } catch (ServiceException e) {
            log.trace("Validations.isEquals(retCodeConfig.getNotLocatetoBondingflowsection(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotLocatetoBondingflowsection(), e.getCode()));
            if (Validations.isEquals(retCodeConfig.getNotLocatetoBondingflowsection(), e.getCode())){
                Validations.check(retCodeConfig.getNotBranchBondingFlow(), "Return point of the branch route is not in a Bonding Flow Section.");
            } else {
                throw e;
            }
        }
        log.debug("check qtime condition for relace target");
        qTimeMethod.qTimeCheckConditionForReplaceTarget(objCommon, lotID);

        //-----------------------------------------------------------
        // Route change request
        //-----------------------------------------------------------
        log.debug("Route change request");
        Inputs.OldCurrentPOData oldCurrentPODataOut = processMethod.processBranchRoute(objCommon, lotID, aSubRouteID, returnOperationNumberVar);

        //---------------------------------------
        //   Update cassette's MultiLotType
        //---------------------------------------
        log.debug("Update cassette's MultiLotType");
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, aCassetteID);

        //--------------------------------------------------------------------------------------------------
        // UpDate RequiredCassetteCategory
        //--------------------------------------------------------------------------------------------------
        log.debug("UpDate RequiredCassetteCategory");
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotID);

        //-----------------------//
        //     Process Hold      //
        //-----------------------//
        //txProcessHoldDoActionReq
        log.debug("process hold do action request");
        processControlService.sxProcessHoldDoActionReq(objCommon, lotID, claimMemo);

        //--------------------------------------------------------------------------------------------------
        // Replace Target Operation for sub route
        //--------------------------------------------------------------------------------------------------
        //qTime_targetOpe_Replace
        log.debug("Replace Target Operation for sub route");
        Infos.QTimeTargetOpeReplaceIn inputParams = new Infos.QTimeTargetOpeReplaceIn();
        inputParams.setLotID(branchReq.getLotID());
        inputParams.setSpecificControlFlag(false);
        qTimeMethod.qTimeTargetOpeReplace(objCommon, inputParams);

        minQTimeMethod.checkTargetOpeReplace(objCommon, branchReq.getLotID());

        //-----------------------------------------------------------
        //   Make History
        //-----------------------------------------------------------
        //lotOperationMoveEvent_MakeBranch
        log.debug("make event for lot operation move branch");
        Inputs.LotOperationMoveEventMakeBranchParams lotOperationMoveEventMakeBranchParams = new Inputs.LotOperationMoveEventMakeBranchParams();
        lotOperationMoveEventMakeBranchParams.setTransactionID(TransactionIDEnum.BRANCH_REQ.getValue());
        lotOperationMoveEventMakeBranchParams.setLotID(branchReq.getLotID());
        lotOperationMoveEventMakeBranchParams.setOldCurrentPOData(oldCurrentPODataOut);
        lotOperationMoveEventMakeBranchParams.setClaimMemo(branchReq.getClaimMemo());
        eventMethod.lotOperationMoveEventMakeBranch(objCommon, lotOperationMoveEventMakeBranchParams);

        // check contamination level ，trigger contamination hold
        log.debug("check contamination level ，trigger contamination hold");
        contaminationMethod.lotCheckContaminationLevelStepOut(objCommon, lotID);
    }

    public void sxBranchWithHoldReleaseReq(Infos.ObjCommon objCommon, Infos.BranchReq branchReq, ObjectIdentifier releaseReasonCodeID, List<Infos.LotHoldReq> strLotHoldReleaseReqList, String claimMemo) {
        /*----------------------------------------*/
        /*    Call txHoldLotReleaseReq            */
        /*----------------------------------------*/
        log.debug("Call txHoldLotReleaseReq");
        Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
        holdLotReleaseReqParams.setLotID(branchReq.getLotID());
        holdLotReleaseReqParams.setReleaseReasonCodeID(releaseReasonCodeID);
        holdLotReleaseReqParams.setHoldReqList(strLotHoldReleaseReqList);
        holdLotReleaseReqParams.setClaimMemo(claimMemo);
        sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);

        /*-----------------------------------------*/
        /*    Call txBranchReq                     */
        /*-----------------------------------------*/
        log.debug("Call txBranchReq");
        sxBranchReq(objCommon, branchReq, claimMemo);
    }

    @SuppressWarnings("deprecation")
    public void sxUnscrapWaferReq(Infos.ObjCommon objCommon, Params.UnscrapWaferReqParams params) {
        log.debug("ObjCommon = {}, UnscrapWaferReqParams = {}", objCommon, params);

        ObjectIdentifier lotID = params.getLotID();
        ObjectIdentifier inCassetteID = params.getCassetteID();
        List<Infos.ScrapCancelWafers> scrapCancelWafersList = params.getScrapCancelWafers();
        boolean bMrgLotInCast = true;

        //---------------------------------------
        // step1 - Check Finished State of lot
        //---------------------------------------
        log.debug("[step-1] Check Finished State of lot");
        String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon, lotID);
        log.trace("StringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedState) : {}", CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedState));
        log.trace("StringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_SCRAPPED, lotFinishedState) : {}", CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_SCRAPPED, lotFinishedState));
        if (CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedState)) {
            throw new ServiceException(retCodeConfig.getInvalidLotFinishStat());
        } else if (CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_SCRAPPED, lotFinishedState)) {
            Validations.check(CimBooleanUtils.isFalse(lotMethod.lotCheckDurationForOperation(objCommon, lotID)), retCodeConfig.getReservedByDeletionProgram());
        }

        //------------------------------
        // step3 - Check Bonding Group
        //------------------------------
        log.debug("[step-3] Check Bonding Group");
        String bondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon, lotID);
        Validations.check(!CimStringUtils.isEmpty(bondingGroupID), retCodeConfig.getLotHasBondingGroup(),lotID.getValue(),bondingGroupID);

        //----------------------------
        //   step4 - Check lot is in cassette
        //----------------------------
        log.debug("[step-4] Check lot is in cassette");
        ObjectIdentifier cassetteID = null;
        try {
            cassetteID = lotMethod.lotCassetteGet(objCommon, lotID);
        } catch (ServiceException e) {
            log.trace("!Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode()) );
            if (!Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {
                log.error("getLotCassette() != ok");
                throw e;
            } else {
                Validations.check(!CimObjectUtils.isEmpty(inCassetteID), retCodeConfig.getInvalidInputCassetteId());
            }
        }
        Validations.check(!CimObjectUtils.equalsWithValue(inCassetteID, cassetteID), retCodeConfig.getInvalidInputCassetteId());

        String lotOperationEIcheckStr = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        int lotOperationEIcheck = CimObjectUtils.isEmpty(lotOperationEIcheckStr) ? 0 : Integer.valueOf(lotOperationEIcheckStr);
        boolean updateControlJobFlag = false;
        Long lockMode = 0L;

        String transferState = null;
        log.trace("0 == lotOperationEIcheck && bMrgLotInCast : {}",0 == lotOperationEIcheck && bMrgLotInCast);
        if (0 == lotOperationEIcheck && bMrgLotInCast) {
            updateControlJobFlag = true;
            //-------------------------------
            // step5 - Get carrier transfer status
            //-------------------------------
            log.debug("[step-5] Get carrier transfer status");
            transferState = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);

            //-------------------------------
            // step6 - Get eqp ID in cassette
            //-------------------------------
            log.debug("[step-6]  Get eqp ID in cassette");
            Outputs.ObjCassetteEquipmentIDGetOut objCassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, cassetteID);

            //-------------------------------
            // Get required equipment lock mode
            //-------------------------------
            log.debug("Get required equipment lock mode");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(objCassetteEquipmentIDGetOut.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.SCRAP_WAFER_CANCEL_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            //-------------------------------
            // step7 - lock
            //-------------------------------
            log.debug("[step-7] lock object");
            log.trace("!Objects.equals(lockMode, BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!Objects.equals(lockMode, BizConstant.SP_EQP_LOCK_MODE_WRITE));
            if (!Objects.equals(lockMode, BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                // Lock Equipment Main Object
                log.debug(" Lock Equipment Main Object");
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                        objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                // Lock Equipment LoadCassette Element (Write)
                log.debug("Lock Equipment LoadCassette Element (Write)");
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                        (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, Arrays.asList(cassetteID.getValue())));
            } else {
                /*--------------------------------*/
                /*   Lock Macihne object          */
                /*--------------------------------*/
                log.debug("Lock Macihne object");
                objectLockMethod.objectLock(objCommon, CimMachine.class, objCassetteEquipmentIDGetOut.getEquipmentID());
            }
        }
        // object_Lock
        log.debug("lock cassette object by cassetteID");
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
        log.trace("0 == lotOperationEIcheck && bMrgLotInCast : {}",0 == lotOperationEIcheck && bMrgLotInCast);
        if (0 == lotOperationEIcheck && bMrgLotInCast) {
            log.trace("!updateControlJobFlag || !Objects.equals(lockMode, BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!updateControlJobFlag || !Objects.equals(lockMode, BizConstant.SP_EQP_LOCK_MODE_WRITE));
            if (!updateControlJobFlag || !Objects.equals(lockMode, BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                ObjectIdentifier cassetteControlJobOut = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
                log.trace("!ObjectUtils.isEmptyWithValue(cassetteControlJobOut) : {}",!CimObjectUtils.isEmptyWithValue(cassetteControlJobOut));
                if (!CimObjectUtils.isEmptyWithValue(cassetteControlJobOut)) {
                    updateControlJobFlag = true;
                    log.trace("!Objects.equals(lockMode, BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!Objects.equals(lockMode, BizConstant.SP_EQP_LOCK_MODE_WRITE));
                    if (!Objects.equals(lockMode, BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                        log.debug("lock contorl job object");
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, cassetteControlJobOut);
                    }
                }
            }
        }

        //----------------------------------------
        // step11 - Check wafer is in cassette
        //----------------------------------------
        log.debug("[step-11] Check wafer is in cassette");
        boolean bValidCastComb = true;
        boolean bScrapInCast = true;

        for (Infos.ScrapCancelWafers scrapCancelWafers : scrapCancelWafersList) {
            //-----------------------------------------
            // Get cassetteID strScrapCancelWafers[jj]
            // For Check of wafer is in cassette
            //-----------------------------------------
            log.debug("Get cassetteID scrapCancelWafersList[] For Check of wafer is in cassette");
            Outputs.ObjWaferLotCassetteGetOut objWaferLotCassetteGetOut = waferMethod.waferLotCassetteGet(objCommon, scrapCancelWafers.getWaferID());
            log.trace("ObjectUtils.isEmpty(objWaferLotCassetteGetOut) || ObjectUtils.isEmpty(objWaferLotCassetteGetOut.getCassetteID()) : {}", CimObjectUtils.isEmpty(objWaferLotCassetteGetOut) || CimObjectUtils.isEmpty(objWaferLotCassetteGetOut.getCassetteID()));
            if (CimObjectUtils.isEmpty(objWaferLotCassetteGetOut) || CimObjectUtils.isEmpty(objWaferLotCassetteGetOut.getCassetteID())) {
                bScrapInCast = false;
            }
            //-----------------------------------------------------------------------
            // Check the cassette combination of scrap-cancel wafers and merged lot.
            //-----------------------------------------------------------------------
            log.debug("Check the cassette combination of scrap-cancel wafers and merged lot.");
            log.trace("bMrgLotInCast : {}",bMrgLotInCast);
            if (bMrgLotInCast) {
                log.trace("!bScrapInCast : {}",!bScrapInCast);
                log.trace("!ObjectUtils.equalsWithValue(cassetteID, objWaferLotCassetteGetOut.getCassetteID()) : {}",!CimObjectUtils.equalsWithValue(cassetteID, objWaferLotCassetteGetOut.getCassetteID()));
                if (!bScrapInCast) {
                    bValidCastComb = false;
                } else if (!CimObjectUtils.equalsWithValue(cassetteID, objWaferLotCassetteGetOut.getCassetteID())) {
                    bValidCastComb = false;
                }
            } else {
                log.trace("bScrapInCast : {}",bScrapInCast);
                if (bScrapInCast) {
                    bValidCastComb = false;
                }
            }

            log.trace("!bValidCastComb : {}",!bValidCastComb);
            if (!bValidCastComb) {
                throw new ServiceException(retCodeConfig.getInvalidCassetteCombination());
            }
        }

        //----------------------------------
        //  step13 - Get InPostProcessFlag of lot
        //----------------------------------
        log.debug("[step-13] Get InPostProcessFlag of lot");
        Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);

        //If lot is in post process, returns error
        log.trace("BaseStaticMethod.isTrue(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot()) : {}",BaseStaticMethod.isTrue(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot()));
        if (BaseStaticMethod.isTrue(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot())) {
            log.debug("Lot is in post process.");
            /*---------------------------*/
            /* Get UserGroupID By UserID */

            log.debug("Get UserGroupID By UserID");
            List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());

            int nCnt = 0;
            for (ObjectIdentifier userGroupID : userGroupIDs) {
            }
            Validations.check(nCnt == CimArrayUtils.getSize(userGroupIDs), retCodeConfig.getLotInPostProcess());
        }

        /*------------------------------------------------------------------------*/
        /*   Check conditions of the lot                                          */
        /*                                                                        */
        /*      strScrapCancelWafers.lotID are same in the input array.           */
        /*      Use first array's strScrapCancelWafers.lotID as an input of the   */
        /*      following ObjectMethod call ... (A),(B),(C),(D),(E),(F)           */
        /*------------------------------------------------------------------------*/
        /**=================(A)====================**/
        /** LotStatusList should be Active or Finished **/
        /**========================================**/
        log.debug("Check conditions of the lot");
        log.debug("A LotStatusList should be Active or Finished");
        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        Validations.check(!(CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, lotState) || CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_ACTIVE, lotState)), retCodeConfig.getInvalidLotStat());

        /**=================(C)===================**/
        /** ProcessState should not be Processing **/
        /**=======================================**/
        log.debug("C ProcessState should not be Processing");
        String lotProcessState = lotMethod.lotProcessStateGet(objCommon, lotID);
        Validations.check(CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotProcessState), retCodeConfig.getInvalidLotProcessState(), CimObjectUtils.getObjectValue(lotID),lotProcessState);

        //----------------------------------
        //  step17 - Check Backup Info
        //----------------------------------
        log.debug("[step-17] Check Backup Info ");
        Infos.LotBackupInfo lotBackupInfo = lotMethod.lotBackupInfoGet(objCommon, lotID);
        Validations.check(CimBooleanUtils.isFalse(lotBackupInfo.getCurrentLocationFlag()) || CimBooleanUtils.isTrue(lotBackupInfo.getTransferFlag()), retCodeConfig.getLotInOthersite(), lotID);

        log.trace("1 == lotOperationEIcheck : {}",1 == lotOperationEIcheck);
        if (1 == lotOperationEIcheck) {
            /**=================(D)===================**/
            /** DispatchState should be NO            **/
            /**=======================================**/
            log.debug("D DispatchState should be NO");
            Validations.check(bMrgLotInCast && BaseStaticMethod.isTrue(cassetteMethod.cassetteDispatchStateGet(objCommon, cassetteID)), retCodeConfig.getInvalidCastDispatchStat());
        }

        String cassetteTransferStateGet = null;
        /**=================(E)====================**/
        /** TransferState should be EO/MO/IO/HO/AO **/
        /**========================================**/
        log.debug("E TransferState should be EO/MO/IO/HO/AO ");
        log.trace("bMrgLotInCast : {}",bMrgLotInCast);
        if (bMrgLotInCast) {
            log.trace("1 == lotOperationEIcheck || (0 == lotOperationEIcheck && !ObjectUtils.isEmpty(transferState)\n" +
                    "                    && StringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) : {}",
                    1 == lotOperationEIcheck || (0 == lotOperationEIcheck && !CimObjectUtils.isEmpty(transferState)
                            && CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)));

            if (1 == lotOperationEIcheck || (0 == lotOperationEIcheck && !CimObjectUtils.isEmpty(transferState)
                    && CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState))) {

                cassetteTransferStateGet = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
                log.trace("0 == lotOperationEIcheck : {}",0 == lotOperationEIcheck);
                log.trace("StringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT, cassetteTransferStateGet) || StringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGet) : {}",
                        CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT, cassetteTransferStateGet) || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGet));

                if (0 == lotOperationEIcheck) {
                    if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGet)) {
                        throw new ServiceException(retCodeConfig.getChangedToEiByOtherOperation());
                    }
                } else if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT, cassetteTransferStateGet) || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGet)) {
                    throw new ServiceException(retCodeConfig.getInvalidLotXferstat());
                }
            }
            log.trace("0 == lotOperationEIcheck : {}",0 == lotOperationEIcheck);
            if (0 == lotOperationEIcheck) cassetteTransferStateGet = transferState;
        }

        /**==================(F)==============================**/
        /** Check this lot contents are 'wafer style' product **/
        /**===================================================**/
        log.debug("Check this lot contents are 'wafer style' product");
        String materialContents = lotMethod.lotContentsGet(lotID);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_PRODTYPE_WAFER, materialContents), retCodeConfig.getInvalidLotContents());

        //--------------------------------------------------
        // Check flowbatch Condition
        //--------------------------------------------------
        //lot_flowBatchID_Get
        log.debug("Check flowbatch Condition");
        try {
            lotMethod.lotFlowBatchIDGet(objCommon, lotID);
        } catch (ServiceException e) {
            log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode()));
            log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()));
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())) {
                log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
                // common case,
            } else if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())) {
                log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_Filled");
                throw new ServiceException(retCodeConfig.getFlowBatchLimitation());
            } else {
                throw e;
            }
        }


        //--------------------------------------------------
        // Check lot's Control Job ID
        //--------------------------------------------------
        log.debug("Check lot's Control Job ID");
        ObjectIdentifier objLotControlJobIDGetOut = lotMethod.lotControlJobIDGet(objCommon, lotID);
        Validations.check(!CimObjectUtils.isEmpty(objLotControlJobIDGetOut), new OmCode(retCodeConfig.getLotControlJobidFilled()
                , CimObjectUtils.getObjectValue(lotID), CimObjectUtils.getObjectValue(objLotControlJobIDGetOut)));

        //--------------------------------------------------
        // Check if all claimed reasons are correct
        //--------------------------------------------------
        log.debug("Check if all claimed reasons are correct");
        List<ObjectIdentifier> ceckedCodes = new ArrayList<>();
        scrapCancelWafersList.forEach(scrapCancelWafers -> ceckedCodes.add(scrapCancelWafers.getReasonCodeID()));
        codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_OPERATIONCATEGORY_WAFERSCRAPCANCEL, ceckedCodes);

        //---------------------------
        // Do wafer Scrap Cancel
        //---------------------------
        log.debug("Do wafer Scrap Cancel");
        lotMethod.lotMaterialsScrapCancelByWafer(objCommon, lotID, cassetteID, scrapCancelWafersList);

        //---------------------------
        // Update cassette's MultiLotType
        //---------------------------
        log.debug("Update cassette's MultiLotType");
        log.trace("bMrgLotInCast : {}",bMrgLotInCast);
        if (bMrgLotInCast) {
            log.trace("updateControlJobFlag : {}",updateControlJobFlag);
            if (updateControlJobFlag) {
                List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
                cassetteIDs.add(cassetteID);
                controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDs);
            }
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
        }

        //-----------------------------------------
        // PosLot WaferHis pointer update
        //-----------------------------------------
        log.debug("PosLot WaferHis pointer update");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, lotID);

        //-----------------------------------------
        // Create Operation History
        //-----------------------------------------
        log.debug("Create Operation History");
        Inputs.LotWaferScrapEventMakeParams lotWaferScrapEventMakeParams = new Inputs.LotWaferScrapEventMakeParams();
        List<Infos.ScrapWafers> scrapWafersList = params.getScrapCancelWafers().stream().map(scrapCancelWafers -> {
            Infos.ScrapWafers scrapWafers = new Infos.ScrapWafers();
            scrapWafers.setReasonCodeID(scrapCancelWafers.getReasonCodeID());
            scrapWafers.setWaferID(scrapCancelWafers.getWaferID());
            return scrapWafers;
        }).collect(Collectors.toList());
        lotWaferScrapEventMakeParams.setScrapWafers(scrapWafersList);
        lotWaferScrapEventMakeParams.setTransactionID(TransactionIDEnum.SCRAP_WAFER_CANCEL_REQ.getValue());
        lotWaferScrapEventMakeParams.setClaimMemo(params.getClaimMemo());
        lotWaferScrapEventMakeParams.setLotID(params.getLotID());
        lotWaferScrapEventMakeParams.setCassetteID(cassetteID);
        lotWaferScrapEventMakeParams.setReasonRouteID(new ObjectIdentifier());
        lotWaferScrapEventMakeParams.setReasonOperationID(new ObjectIdentifier());
        lotWaferScrapEventMakeParams.setReasonOperationNumber(null);
        lotWaferScrapEventMakeParams.setReasonOperationPass(null);
        eventMethod.lotWaferScrapEventMake(objCommon, lotWaferScrapEventMakeParams);
    }

    @SuppressWarnings("deprecation")
    public void sxUnscrapWaferNotOnPfReq(Infos.ObjCommon objCommon, Params.ScrapWaferNotOnPfReqParams params) {
        log.debug("ObjCommon = {}, ScrapWaferNotOnPfReqParams = {}", objCommon, params);

        ObjectIdentifier lotID = params.getLotID();
        ObjectIdentifier reasonCode = params.getReasonCode();
        int waferCount = params.getWaferCount();

        /*------------------------------------------------------------------------*/
        /*   Check Finished State of lot                                          */
        /*------------------------------------------------------------------------*/
        log.debug("Check Finished State of lot ");
        String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon, lotID);
        log.trace("StringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedState) : {}", CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedState));
        if (CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinishedState)) {
            throw new ServiceException(retCodeConfig.getInvalidLotFinishStat());
        } else if (CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_SCRAPPED, lotFinishedState)) {
            Validations.check(CimBooleanUtils.isFalse(lotMethod.lotCheckDurationForOperation(objCommon, lotID)), retCodeConfig.getReservedByDeletionProgram());
        }

        //---------------------------------------
        // Check Bonding Group
        //---------------------------------------
        log.debug("Check Bonding Group");
        String bondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon, lotID);
        Validations.check(!CimStringUtils.isEmpty(bondingGroupID), retCodeConfig.getLotHasBondingGroup(),lotID.getValue(),bondingGroupID);

        /*------------------------------------------------------------------------*/
        /*   Check conditions of the lot                                          */
        /*------------------------------------------------------------------------*/
        /**=================(A)====================**/
        /** LotStatusList should be Active or Finished **/
        /**========================================**/
        log.debug("Check conditions of the lot");
        log.debug("LotStatusList should be Active or Finished");
        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        Validations.check(!(CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, lotState) || CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_ACTIVE, lotState)), retCodeConfig.getInvalidLotStat());

        /**=================(B)===================**/
        /** ProcessState should not be Processing **/
        /**=======================================**/
        log.debug("ProcessState should not be Processing");
        String lotProcessState = lotMethod.lotProcessStateGet(objCommon, lotID);
        Validations.check(CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotProcessState), retCodeConfig.getInvalidLotProcessState(), CimObjectUtils.getObjectValue(lotID),lotProcessState);

        /**==================(C)==============================**/
        /** Check this lot contents are 'wafer style' product **/
        /**===================================================**/
        log.debug("Check this lot contents are 'wafer style' product");
        String materialContents = lotMethod.lotContentsGet(lotID);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_PRODTYPE_WAFER, materialContents), retCodeConfig.getInvalidLotContents());

        /*----------------------------------*/
        /*   Check lot's Control Job ID     */
        /*----------------------------------*/
        log.debug("Check lot's Control Job ID");
        ObjectIdentifier controlJobID = lotMethod.lotControlJobIDGet(objCommon, lotID);
        Validations.check(!org.springframework.util.ObjectUtils.isEmpty(controlJobID), new OmCode(retCodeConfig.getLotControlJobidFilled()
                , CimObjectUtils.getObjectValue(lotID), CimObjectUtils.getObjectValue(controlJobID)));

        /*------------------------------------------------------------------------*/
        /*   Check if all claimed reasons are correct                             */
        /*------------------------------------------------------------------------*/
        log.debug("Check if all claimed reasons are correct");
        codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_OPERATIONCATEGORY_WAFERSCRAPCANCEL, Arrays.asList(reasonCode));

        /*------------------------------------------------------------------------*/
        /*   lot inventory state should be "InBank"                               */
        /*------------------------------------------------------------------------*/
        log.debug("lot inventory state should be \"InBank\"");
        String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, lotID);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK, lotInventoryState), retCodeConfig.getInvalidLotInventoryStat(),
                ObjectIdentifier.fetchValue(lotID), lotInventoryState);

        /*------------------------------------------------------------------------*/
        /*   Do wafer Scrap Cancel                                                */
        /*------------------------------------------------------------------------*/
        log.debug("Do wafer Scrap Cancel");
        lotMethod.lotMaterialsScrapWaferNotOnRouteCancel(objCommon, lotID, waferCount);

        /**========================================**/
        /** PosLot WaferHis pointer update         **/
        /**========================================**/
        log.debug("PosLot WaferHis pointer update");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, lotID);

        /**========================================**/
        /** Create Operation History               **/
        /**========================================**/
        log.debug("Create Operation History ");
        Inputs.LotWaferScrapEventMakeParams lotWaferScrapEventMakeParams = new Inputs.LotWaferScrapEventMakeParams();
        lotWaferScrapEventMakeParams.setCassetteID(new ObjectIdentifier());
        lotWaferScrapEventMakeParams.setReasonRouteID(new ObjectIdentifier());
        lotWaferScrapEventMakeParams.setReasonOperationID(new ObjectIdentifier());
        lotWaferScrapEventMakeParams.setReasonOperationNumber(null);
        lotWaferScrapEventMakeParams.setReasonOperationPass(null);
        List<Infos.ScrapWafers> scrapWafersList = new ArrayList<>();
        scrapWafersList.add(new Infos.ScrapWafers(reasonCode));
        lotWaferScrapEventMakeParams.setScrapWafers(scrapWafersList);
        lotWaferScrapEventMakeParams.setLotID(params.getLotID());
        lotWaferScrapEventMakeParams.setClaimMemo(params.getClaimMemo());
        lotWaferScrapEventMakeParams.setTransactionID(TransactionIDEnum.SCRAP_WAFER_NOT_ON_ROUTE_CANCEL_REQ.getValue());
        eventMethod.lotWaferScrapEventMake(objCommon, lotWaferScrapEventMakeParams);

    }

    @SuppressWarnings("deprecation")
    public void sxScrapWaferNotOnPfReq(Infos.ObjCommon objCommon, Params.ScrapWaferNotOnPfReqParams params) {
        log.debug("ObjCommon = {}, ScrapWaferNotOnPfReqParams = {}", objCommon, params);

        ObjectIdentifier lotID = params.getLotID();
        ObjectIdentifier reasonCode = params.getReasonCode();
        int waferCount = params.getWaferCount();

        /**========================================**/
        /** LotStatusList should be Active or Finished **/
        /**========================================**/
        log.debug("LotStatusList should be Active or Finished");
        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        Validations.check(!(CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, lotState) || CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_ACTIVE, lotState)), retCodeConfig.getInvalidLotStat());

        /**=======================================**/
        /** FinishedStatus should not be Scrapped **/
        /**  or Emptied  (should be Completed),   **/
        /**  in the case of "lotState = Finished" **/
        /**=======================================**/
        log.debug("FinishedStatus should not be Scrapped or Emptied  (should be Completed)  in the case of \"lotState = Finished\"");
        log.trace("StringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, lotState) : {}", CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, lotState));
        if (CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, lotState)) {
            String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon, lotID);
            Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_COMPLETED, lotFinishedState), retCodeConfig.getInvalidLotFinishStat());
        }

        //---------------------------------------
        // Check Bonding Group
        //---------------------------------------
        log.debug("Check Bonding Group");
        String bondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon, lotID);
        Validations.check(!CimStringUtils.isEmpty(bondingGroupID), retCodeConfig.getLotHasBondingGroup(),lotID.getValue(),bondingGroupID);

        /**=======================================**/
        /** ProcessState should not be Processing **/
        /**=======================================**/
        log.debug("ProcessState should not be Processing");
        String lotProcessState = lotMethod.lotProcessStateGet(objCommon, lotID);
        Validations.check(CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotProcessState), retCodeConfig.getInvalidLotProcessState(), CimObjectUtils.getObjectValue(lotID),lotProcessState);

        /*----------------------------------*/
        /*   Check lot's Control Job ID     */
        /*----------------------------------*/
        log.debug("Check lot's Control Job ID");
        ObjectIdentifier controlJobID = lotMethod.lotControlJobIDGet(objCommon, lotID);
        Validations.check(!org.springframework.util.ObjectUtils.isEmpty(controlJobID), new OmCode(retCodeConfig.getLotControlJobidFilled(),
                CimObjectUtils.getObjectValue(lotID), CimObjectUtils.getObjectValue(controlJobID)));

        /*------------------------------------------------------------------------*/
        /*   Check if claimed reasons are correct                                 */
        /*------------------------------------------------------------------------*/
        log.debug("Check if claimed reasons are correct");
        codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_OPERATIONCATEGORY_WAFERSCRAP, Arrays.asList(reasonCode));

        /*------------------------------------------------------------------------*/
        /*   Check this lot contents are 'wafer style' product                    */
        /*------------------------------------------------------------------------*/
        log.debug("Check this lot contents are 'wafer style' product");
        String materialContents = lotMethod.lotContentsGet(lotID);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_PRODTYPE_WAFER, materialContents), retCodeConfig.getInvalidLotContents());

        /*------------------------------------------------------------------------*/
        /*   Do wafer Scrap                                                       */
        /*------------------------------------------------------------------------*/
        log.debug("Do wafer Scrap");
        try {
            lotMethod.lotMaterialsScrapWaferNotOnRoute(objCommon, lotID, waferCount);
        } catch (ServiceException e) {
            log.trace("Validations.isEquals(retCodeConfig.getAllScraped(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getAllScraped(), e.getCode()));
            if (Validations.isEquals(retCodeConfig.getAllScraped(), e.getCode())) {
                String lotType = lotMethod.lotTypeGet(objCommon, lotID);
                log.debug("StringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotType) || StringUtils.equals(BizConstant.SP_LOT_TYPE_CORRELATIONLOT, lotType) : {}",
                        CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotType) || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_CORRELATIONLOT, lotType));

                if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotType) || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_CORRELATIONLOT, lotType)) {
                }
            } else {
                throw e;
            }
        }

        /**========================================**/
        /** PosLot WaferHis pointer update         **/
        /**========================================**/
        log.debug("PosLot WaferHis pointer update");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, lotID);

        /**========================================**/
        /** Create Operation History               **/
        /**========================================**/
        log.debug("Create Operation History ");
        Inputs.LotWaferScrapEventMakeParams lotWaferScrapEventMakeParams = new Inputs.LotWaferScrapEventMakeParams();
        lotWaferScrapEventMakeParams.setCassetteID(new ObjectIdentifier());
        lotWaferScrapEventMakeParams.setReasonRouteID(new ObjectIdentifier());
        lotWaferScrapEventMakeParams.setReasonOperationID(new ObjectIdentifier());
        lotWaferScrapEventMakeParams.setReasonOperationNumber(null);
        lotWaferScrapEventMakeParams.setReasonOperationPass(null);
        List<Infos.ScrapWafers> scrapWafersList = new ArrayList<>();
        scrapWafersList.add(new Infos.ScrapWafers(reasonCode));
        lotWaferScrapEventMakeParams.setScrapWafers(scrapWafersList);
        lotWaferScrapEventMakeParams.setLotID(params.getLotID());
        lotWaferScrapEventMakeParams.setClaimMemo(params.getClaimMemo());
        lotWaferScrapEventMakeParams.setTransactionID(TransactionIDEnum.SCRAP_WAFER_NOT_ON_ROUTE_REQ.getValue());
        eventMethod.lotWaferScrapEventMake(objCommon, lotWaferScrapEventMakeParams);

    }

    @SuppressWarnings("deprecation")
    public void sxScrapWaferReq(Infos.ObjCommon objCommon, Params.ScrapWaferReqParams params) {
        log.debug("ObjCommon = {}, ScrapWaferReqParams = {}", objCommon, params);

        ObjectIdentifier lotID = params.getLotID();
        List<Infos.ScrapWafers> strScrapWafers = params.getScrapWafers();

        //-------------
        //   step1 -
        //-------------
        log.debug("[step-1] lot reomve from monitor group");
        lotMethod.lotRemoveFromMonitorGroup(objCommon, lotID);

        //----------------------------
        //   step2 - Check lot is in cassette
        //----------------------------
        log.debug("[step-2] Check lot is in cassette");
        ObjectIdentifier cassetteID = lotMethod.lotCassetteGet(objCommon, lotID);

        //---------------------------------------
        //   step3 - Lock objects to be updated
        //---------------------------------------
        // object_Lock
        log.debug("[step-3] Lock objects to be updated ");
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);

        //-------------------------------
        //   step4 - Check LOCK Hold
        //-------------------------------
        // lot_CheckLockHoldConditionForOperation
        log.debug("[step-4] Check LOCK Hold");
        lotMethod.lotCheckLockHoldConditionForOperation(objCommon, Collections.singletonList(lotID));

        //----------------------------------------
        //  step5 - Get InPostProcessFlag of lot
        //----------------------------------------
        log.debug("[step-5] get in post process flag lot");
        Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);

        //If lot is in post process, returns error
        log.trace("BaseStaticMethod.isTrue(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot()) : {}",BaseStaticMethod.isTrue(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot()));
        if (BaseStaticMethod.isTrue(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot())) {
            log.debug("Lot is in post process.");
            /*---------------------------*/
            /* Get UserGroupID By UserID */

            log.debug(" Get UserGroupID By UserID");
            List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());

            int nCnt = 0;
            for (ObjectIdentifier userGroupID : userGroupIDs) {
                nCnt++;
            }
            Validations.check(nCnt == CimArrayUtils.getSize(userGroupIDs), retCodeConfig.getLotInPostProcess());
        }

        //--------------------------------------------------
        //  step7 - LotStatusList should be Active or Finished
        //--------------------------------------------------
        log.debug("[step-7] LotStatusList should be Active or Finished");
        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        Validations.check(!(CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_FINISHED, lotState) || CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotState)), new OmCode(retCodeConfig.getInvalidLotStat(),lotState));

        //------------------------------------------------------------------------------------------------------------------------
        //  step8 - FinishedStatus should not be Scrapped or Emptied  (should be Completed),in the case of "lotState = Finished"
        //------------------------------------------------------------------------------------------------------------------------
        log.debug("[step-8] FinishedStatus should not be Scrapped or Emptied  (should be Completed),in the case of 'lotState = Finished'");
        if (CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_FINISHED, lotState)) {
            String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon, lotID);
            Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_COMPLETED, lotFinishedState), retCodeConfig.getInvalidLotFinishStat());
        }

        //---------------------------------------
        // step9 - Check Bonding Group
        //---------------------------------------
        log.debug("[step-9] Check Bonding Group");
        String bondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon, lotID);
        Validations.check(!CimStringUtils.isEmpty(bondingGroupID), retCodeConfig.getLotHasBondingGroup(),lotID.getValue(),bondingGroupID);

        //-----------------------------------------------
        // step10 - ProcessState should not be Processing
        //-----------------------------------------------
        log.debug("[step-10] ProcessState should not be Processing");
        String lotProcessState = lotMethod.lotProcessStateGet(objCommon, lotID);
        Validations.check(CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotProcessState), retCodeConfig.getInvalidLotProcessState(), CimObjectUtils.getObjectValue(lotID),lotProcessState);

        //---------------------------------------
        // step11 - Check Backup Info
        //---------------------------------------
        log.debug("[step-11] Check Backup Info");
        Infos.LotBackupInfo lotBackupInfo = lotMethod.lotBackupInfoGet(objCommon, lotID);
        Validations.check(CimBooleanUtils.isFalse(lotBackupInfo.getCurrentLocationFlag()) || CimBooleanUtils.isTrue(lotBackupInfo.getTransferFlag()), retCodeConfig.getLotInOthersite(), lotID);

        String lotOperationEIcheckStr = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        int lotOperationEIcheck = org.springframework.util.ObjectUtils.isEmpty(lotOperationEIcheckStr) ? 0 : Integer.valueOf(lotOperationEIcheckStr);
        log.trace("1 == lotOperationEIcheck : {}",1 == lotOperationEIcheck);
        if (1 == lotOperationEIcheck) {
            //---------------------------------------
            // step12 - DispatchState should be NO
            //---------------------------------------
            log.debug("[step-12] DispatchState should be NO");
            log.trace("!org.springframework.util.ObjectUtils.isEmpty(cassetteID) : {}",!org.springframework.util.ObjectUtils.isEmpty(cassetteID));
            if (!org.springframework.util.ObjectUtils.isEmpty(cassetteID)) {
                Validations.check(BaseStaticMethod.isTrue(cassetteMethod.cassetteDispatchStateGet(objCommon, cassetteID)), retCodeConfig.getInvalidCastDispatchStat());
            }
        }

        //--------------------------------------------------
        // step13 - TransferState should be EO/MO/IO/HO/AO
        //--------------------------------------------------
        log.debug("[step-13] TransferState should be EO/MO/IO/HO/AO");
        log.trace("!org.springframework.util.ObjectUtils.isEmpty(cassetteID) : {}",!org.springframework.util.ObjectUtils.isEmpty(cassetteID));
        if (!org.springframework.util.ObjectUtils.isEmpty(cassetteID)) {
            log.trace("1 == lotOperationEIcheck : {}",1 == lotOperationEIcheck);
            if (1 == lotOperationEIcheck) {
                String transferState = lotMethod.lotTransferStateGet(objCommon, lotID);
                Validations.check(CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT, transferState) || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState), new OmCode(retCodeConfig.getInvalidLotXferstat(),lotID.getValue(),transferState));
            }
        }

        //--------------------------------------------------
        // step14 - Check flowbatch Condition
        //--------------------------------------------------
        //lot_flowBatchID_Get
        log.debug("[step-14] Check flowbatch Condition");
        try {
            lotMethod.lotFlowBatchIDGet(objCommon, lotID);
        } catch (ServiceException e) {
            log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode()));
            log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()));
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())) {
                log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
                // common case,
            } else if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())) {
                log.info("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_Filled");
                throw new ServiceException(retCodeConfig.getFlowBatchLimitation());
            } else {
                throw e;
            }
        }

        //--------------------------------------------------
        // step15 - Check lot's Control Job ID
        //--------------------------------------------------
        log.debug("[step-15] Check lot's Control Job ID");
        ObjectIdentifier objLotControlJobIDGetOut = lotMethod.lotControlJobIDGet(objCommon, lotID);
        Validations.check(!org.springframework.util.ObjectUtils.isEmpty(objLotControlJobIDGetOut), retCodeConfig.getLotControlJobidFilled());

        //--------------------------------------------------
        // step16 - Check all claimed wafer existance
        //--------------------------------------------------
        log.debug("[step-16] Check all claimed wafer existance");
        lotMethod.lotMaterialsCheckExistance(objCommon, lotID, strScrapWafers);


        //--------------------------------------------------
        // step17 - Check if all claimed reasons are correct
        //--------------------------------------------------
        log.debug("[step-17] Check if all claimed reasons are correct");
        List<ObjectIdentifier> ceckedCodes = new ArrayList<>();
        strScrapWafers.forEach(scrapWafers -> ceckedCodes.add(scrapWafers.getReasonCodeID()));
        codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_OPERATIONCATEGORY_WAFERSCRAP, ceckedCodes);

        //-----------------------------------------------------------------
        // step18 - Check this lot contents are 'wafer style' product
        //-----------------------------------------------------------------
        log.debug("[step-18] Check this lot contents are 'wafer style' product");
        String materialContents = lotMethod.lotContentsGet(lotID);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_PRODTYPE_WAFER, materialContents), retCodeConfig.getInvalidLotContents());

        //---------------------------
        // Do wafer Scrap
        //---------------------------
        log.debug("Do wafer Scrap");
        try {
            lotMethod.lotMaterialsScrapByWafer(objCommon, lotID, strScrapWafers);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getAllScraped(), e.getCode())) {
                String lotType = lotMethod.lotStateGet(objCommon, lotID);
                log.trace("StringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotType) || StringUtils.equals(BizConstant.SP_LOT_TYPE_CORRELATIONLOT, lotType) : {}",
                        CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotType) || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_CORRELATIONLOT, lotType));

                if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotType) || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_CORRELATIONLOT, lotType)) {
                }
            } else {
                throw e;
            }

        }
        log.trace("!org.springframework.util.ObjectUtils.isEmpty(cassetteID) : {}",!org.springframework.util.ObjectUtils.isEmpty(cassetteID));
        if (!org.springframework.util.ObjectUtils.isEmpty(cassetteID)) {
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
        }

        //-----------------------------------------
        // step20 - PosLot WaferHis pointer update
        //-----------------------------------------
        log.debug("[step-20] PosLot WaferHis pointer update");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, lotID);

        //-----------------------------------------
        // step21 - Create Operation History
        //-----------------------------------------
        log.debug("[step-21] Create Operation History");
        Inputs.LotWaferScrapEventMakeParams lotWaferScrapEventMakeParams = new Inputs.LotWaferScrapEventMakeParams();
        lotWaferScrapEventMakeParams.setTransactionID(TransactionIDEnum.SCRAP_WAFER_REQ.getValue());
        lotWaferScrapEventMakeParams.setLotID(params.getLotID());
        lotWaferScrapEventMakeParams.setCassetteID(cassetteID);
        lotWaferScrapEventMakeParams.setReasonRouteID(params.getReasonRouteID());
        lotWaferScrapEventMakeParams.setReasonOperationID(params.getReasonOperationID());
        lotWaferScrapEventMakeParams.setReasonOperationNumber(params.getReasonOperationNumber());
        lotWaferScrapEventMakeParams.setReasonOperationPass(params.getReasonOperationPass());
        lotWaferScrapEventMakeParams.setScrapWafers(params.getScrapWafers());
        lotWaferScrapEventMakeParams.setClaimMemo(params.getClaimMemo());
        eventMethod.lotWaferScrapEventMake(objCommon, lotWaferScrapEventMakeParams);

    }

    @Override
    public void sxLotContaminationUpdateReq(Params.LotContaminationParams params, Infos.ObjCommon objCommon) {
        objectLockMethod.objectLock(objCommon, CimLot.class, params.getLotId());
        contaminationMethod.lotContaminationUpdate(params, objCommon);
    }

    @Override
    public void checkLotSplitOrMerge(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotIDLists,String action) {
        log.trace("CollectionUtils.isEmpty(lotIDLists) : {}",CollectionUtils.isEmpty(lotIDLists));
        if (CollectionUtils.isEmpty(lotIDLists)) {
            log.error("not found lots, the lotIDs is Empty");
            return;
        }

        for (ObjectIdentifier lotID : lotIDLists) {
            log.debug("!ObjectUtils.isEmptyWithValue(lotID) : {}",!ObjectIdentifier.isEmptyWithValue(lotID));
            if (!ObjectIdentifier.isEmptyWithValue(lotID)){
                CimLot cimLotBO = baseCoreFactory.getBO(CimLot.class,lotID);
                log.trace("null == cimLotBO : {}",null == cimLotBO);
                if (null == cimLotBO) {
                    log.error("not found lot, the lotID: {}", lotID.getValue());
                    Validations.check(true, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
                }

                // When the Lot Type of Lot is Production and the property of Lot Inventory State is InBank, Split and Merge cannot be used
                log.debug("【step9-3-1】When the Lot Type of Lot is Production and the property of Lot Inventory State is InBank, Split and Merge cannot be used");
                String inventoryState = cimLotBO.getLotInventoryState();
                String lotType = cimLotBO.getLotType();
                boolean inventoryStateBool = BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK.equals(inventoryState) ||
                        BizConstant.SP_LOT_INVENTORYSTATE_INBANK.equals(inventoryState);

                log.trace("BizConstant.SP_LOT_TYPE_PRODUCTION.equals(lotType) && inventoryStateBool : {}",BizConstant.SP_LOT_TYPE_PRODUCTION.equals(lotType) && inventoryStateBool);
                if (BizConstant.SP_LOT_TYPE_PRODUCTION.equals(lotType) && inventoryStateBool) {
                    log.error("{} Lot is not allowed to do Split/Merge in {}", lotID.getValue(), inventoryState);
                    Validations.check(true, new OmCode(retCodeConfigEx.getLotNotSplitMerge(), lotID.getValue(),action, inventoryState));
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sxHoldDepartmentChangeReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.LotHoldReq> holdReqList) {
        Validations.check (CimArrayUtils.isEmpty(holdReqList), retCodeConfig.getInvalidParameter());
        Validations.check (CimObjectUtils.isEmpty(lotID), retCodeConfig.getInvalidParameter());

        ObjectIdentifier cassetteID = null;
        boolean backupProcessingFlag = false;
        log.trace("StringUtils.equals(\"TXBOC003\" , objCommon.getTransactionID()) : {}", CimStringUtils.equals("TXBOC003" , objCommon.getTransactionID()));
        if (CimStringUtils.equals("TXBOC003" , objCommon.getTransactionID())) {
            backupProcessingFlag = true;
        }

        log.trace("!backupProcessingFlag : {}",!backupProcessingFlag);
        if (!backupProcessingFlag) {
            ObjectIdentifier objGetLotCassetteOut = null;
            try {
                objGetLotCassetteOut = lotMethod.lotCassetteGet(objCommon, lotID);
            } catch (ServiceException e) {
                log.trace("Validations.isEquals(retCodeConfig.getNotFoundCst(),e.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotFoundCst(),e.getCode()));
                if (Validations.isEquals(retCodeConfig.getNotFoundCst(),e.getCode())) {
                    int casstteForHold = StandardProperties.OM_CARRIER_CHK_RELATION_FOR_HOLD.getIntValue();
                    log.trace("casstteForHold == 0 : {}",casstteForHold == 0);
                    if (casstteForHold == 0) {

                    } else {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }

            log.trace("null != objGetLotCassetteOut : {}",null != objGetLotCassetteOut);
            if(null != objGetLotCassetteOut){
                cassetteID = objGetLotCassetteOut;
            }
        }

        //step 1 - Lock objects to be updated
        log.trace("[step-1] Lock objects to be updated");
        String strParallelPostProcFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG);
        //step 2 - Skip cassette lock to increase parallel availability under PostProcess parallel execution
        log.debug("[step-2] Skip cassette lock to increase parallel availability under PostProcess parallel execution");
        log.trace("!StringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON) : {}",!CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON));
        if (!CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON)) {
            log.trace("!backupProcessingFlag : {}",!backupProcessingFlag);
            if (!backupProcessingFlag) {
                log.trace("null != cassetteID : {}",null != cassetteID);
                if (null != cassetteID) {
                    //  object_Lock
                    objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
                }
            }
        }
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);

        //step 3 - Check Condition
        log.debug("[step-3] Check Condition");
        Boolean bLotLockFlag = false;
        for (int i = 0; i < holdReqList.size(); i++) {
            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_REASON_LOTLOCK, holdReqList.get(i).getHoldReasonCodeID()) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_REASON_LOTLOCK, holdReqList.get(i).getHoldReasonCodeID()));
            log.trace("BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            log.trace("BizConstant.SP_REASON_RUNNINGHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_RUNNINGHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            log.trace("BizConstant.SP_REASON_FORCECOMPHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_FORCECOMPHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            log.trace("BizConstant.SP_REASON_BACKUPOPERATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_BACKUPOPERATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            log.trace("BizConstant.SP_REASON_CONTAMINATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_CONTAMINATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            if (CimObjectUtils.equalsWithValue(BizConstant.SP_REASON_LOTLOCK, holdReqList.get(i).getHoldReasonCodeID())) {
                bLotLockFlag = true;
                Validations.check (!TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.POST_PROCESS_ACTION_REGIST_REQ.getValue().equals(objCommon.getTransactionID())
                        //wafer bonding根据逻辑添加，无源码
                        && !TransactionIDEnum.BONDING_GROUP_MODIFY_REQ.getValue().equals(objCommon.getTransactionID())
                        && !"OPOSW008".equals(objCommon.getTransactionID()), retCodeConfig.getInvalidReasonCodeFromClient(), BizConstant.SP_REASON_LOTLOCK);
            } else if (BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                Validations.check (!TransactionIDEnum.NON_PRO_BANK_IN_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.SPLIT_WAFER_LOT_WITH_OUT_HOLD_RELEASE_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.FORCE_OPERATION_COMP_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.SPC_ACTION_EXECUTE_REQ.getValue().equals(objCommon.getTransactionID()), retCodeConfig.getCannotHoldWithNpbh());
            } else if (BizConstant.SP_REASON_RUNNINGHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                Validations.check(true, retCodeConfig.getInvalidReasonCodeFromClient(), BizConstant.SP_REASON_RUNNINGHOLD);
            } else if (BizConstant.SP_REASON_FORCECOMPHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                Validations.check (!TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.FORCE_OPERATION_COMP_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.SPLIT_WAFER_LOT_WITH_OUT_HOLD_RELEASE_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ.getValue().equals(objCommon.getTransactionID())
                        && !TransactionIDEnum.BONDING_GROUP_PARTIAL_REMOVE_REQ.getValue().equals(objCommon.getTransactionID())
                        && !"TXPCC059".equals(objCommon.getTransactionID()), retCodeConfig.getInvalidReasonCodeFromClient(), BizConstant.SP_REASON_FORCECOMPHOLD);
            } else if (BizConstant.SP_REASON_BACKUPOPERATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                Validations.check (!"TXBOC003".equals(objCommon.getTransactionID()), retCodeConfig.getCannotHoldWithBohl(), BizConstant.SP_REASON_BACKUPOPERATION_HOLD);
            }else if (BizConstant.SP_REASON_CONTAMINATION_HOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                Validations.check ("OLOTW001".equals(objCommon.getTransactionID()), retCodeConfigEx.getCannotHoldWithCcmh());
            }
        }

        Infos.LotBackupInfo lotBackupInfo = lotMethod.lotBackupInfoGet(objCommon, lotID);
        log.trace("null != lotBackupInfo : {}",null != lotBackupInfo);
        if (null != lotBackupInfo) {
            Validations.check (!lotBackupInfo.getCurrentLocationFlag() || lotBackupInfo.getTransferFlag(), retCodeConfig.getLotInOthersite());
        }

        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        log.trace("!BizConstant.CIMFW_LOT_STATE_ACTIVE.equals(lotState) : {}",!BizConstant.CIMFW_LOT_STATE_ACTIVE.equals(lotState));
        if (!BizConstant.CIMFW_LOT_STATE_ACTIVE.equals(lotState)) {
            Validations.check (!backupProcessingFlag, new OmCode(retCodeConfig.getInvalidLotStat(), lotState));
        }

        String lotProcessStateGet = lotMethod.lotProcessStateGet(objCommon, lotID);
        //step 4 - ProcessState should not be Processing
        log.debug("[step-4] ProcessState should not be Processing");
        log.trace("BizConstant.SP_LOT_PROCSTATE_PROCESSING.equals(lotProcessStateGet) : {}",BizConstant.SP_LOT_PROCSTATE_PROCESSING.equals(lotProcessStateGet));
        if (BizConstant.SP_LOT_PROCSTATE_PROCESSING.equals(lotProcessStateGet)) {
            log.trace("TransactionIDEnum.COLLECTED_DATA_BY_PJ_RPT.getValue().equals(objCommon.getTransactionID() : {}",TransactionIDEnum.COLLECTED_DATA_BY_PJ_RPT.getValue().equals(objCommon.getTransactionID()));
            if (TransactionIDEnum.COLLECTED_DATA_BY_PJ_RPT.getValue().equals(objCommon.getTransactionID())
                    || TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue().equals(objCommon.getTransactionID())
                    || TransactionIDEnum.POST_PROCESS_ACTION_REGIST_REQ.getValue().equals(objCommon.getTransactionID())
                    || TransactionIDEnum.POST_PROCESS_ACTION_UPDATE.getValue().equals(objCommon.getTransactionID())
                    || "OPOSW008".equals(objCommon.getTransactionID())
                    || bLotLockFlag) {
                //OK
            } else {
                Validations.check(true, retCodeConfig.getInvalidLotProcstat(),ObjectIdentifier.fetchValue(lotID),lotProcessStateGet);
            }
        }

        //step 5 -  Get InPostProcessFlag of lot
        log.debug("[step-5] get in post process flag lot");
        Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);

        //step 6 -  Check lot interFabXferState
        log.debug("[step-6] Check lot interFabXferState");
        String lotInterFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);

        //step 7 - If lot is in post process, returns error
        log.debug("[step-7] If lot is in post process, returns error");
        log.trace("BaseStaticMethod.isTrue(lotInPostProcessFlagGetOut.getInPostProcessFlagOfLot()) : {}",BaseStaticMethod.isTrue(lotInPostProcessFlagGetOut.getInPostProcessFlagOfLot()));
        if (BaseStaticMethod.isTrue(lotInPostProcessFlagGetOut.getInPostProcessFlagOfLot())) {
            int ppChainMode = StandardProperties.OM_PP_CHAIN_FLAG.getIntValue();
            log.trace("ppChainMode == 1 : {}",ppChainMode == 1);
            if (ppChainMode == 1) {
                String strTriggerDKey = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
                log.trace("StringUtils.isEmpty(strTriggerDKey) : {}", CimStringUtils.isEmpty(strTriggerDKey));
                if (CimStringUtils.isEmpty(strTriggerDKey)) {
                    ppChainMode = 0;
                }
            }

            Boolean bSkipPPLotCheck = false;
            log.trace("ppChainMode == 1 && ArrayUtils.getSize(holdReqList) == 1 &&\n" +
                    "                    (ObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_PSM_HOLD )||\n" +
                    "                            ObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_SPR_HOLD)) : {}",
                    ppChainMode == 1 && CimArrayUtils.getSize(holdReqList) == 1 &&
                            (CimObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_PSM_HOLD )||
                                    CimObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_SPR_HOLD)));

            if (ppChainMode == 1 && CimArrayUtils.getSize(holdReqList) == 1 &&
                    (CimObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_PSM_HOLD )||
                            CimObjectUtils.equalsWithValue(holdReqList.get(0).getHoldReasonCodeID(), BizConstant.SP_REASON_SPR_HOLD))) {
                log.debug("Hold reason is PSWH or SPRH and called from PostProc");
                bSkipPPLotCheck = true;
            }

            log.trace("!bSkipPPLotCheck && !BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED.equals(lotInterFabXferState) : {}",!bSkipPPLotCheck && !BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED.equals(lotInterFabXferState));
            if (!bSkipPPLotCheck && !BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED.equals(lotInterFabXferState)) {
                //step 8 - Get UserGroupID By UserID
                log.debug("[step-8]  Get UserGroupID By UserID");
                List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                int i;
                for (i = 0; i < CimArrayUtils.getSize(userGroupIDs); i++) {
                }
                Validations.check (CimArrayUtils.getSize(userGroupIDs) == i, retCodeConfig.getLotInPostProcess());
            }
        }

        //step 9 - Check PosCode
        log.debug("[step-9]  Check PosCode");
        List<ObjectIdentifier> futureHolds = new ArrayList<>();
        List<ObjectIdentifier> processHolds = new ArrayList<>();
        List<ObjectIdentifier> lotHolds = new ArrayList<>();
        for (int i = 0; i < holdReqList.size(); i++) {
            log.trace("BizConstant.SP_REASONCAT_FUTUREHOLD.equals(holdReqList.get(i).getHoldType()) : {}",BizConstant.SP_REASONCAT_FUTUREHOLD.equals(holdReqList.get(i).getHoldType()));
            log.trace("BizConstant.SP_REASONCAT_PROCESSHOLD.equals(holdReqList.get(i).getHoldType()) : {}",BizConstant.SP_REASONCAT_PROCESSHOLD.equals(holdReqList.get(i).getHoldType()));
            log.trace("BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()) : {}",BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue()));
            if (BizConstant.SP_REASONCAT_FUTUREHOLD.equals(holdReqList.get(i).getHoldType())) {
                futureHolds.add(holdReqList.get(i).getHoldReasonCodeID());
            } else if (BizConstant.SP_REASONCAT_PROCESSHOLD.equals(holdReqList.get(i).getHoldType())) {
                processHolds.add(holdReqList.get(i).getHoldReasonCodeID());
            } else if (BizConstant.SP_REASON_NONPROBANKHOLD.equals(holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                continue;
            } else {
                lotHolds.add(holdReqList.get(i).getHoldReasonCodeID());
            }
        }
        log.trace("!ArrayUtils.isEmpty(futureHolds) : {}",!CimArrayUtils.isEmpty(futureHolds));
        if (!CimArrayUtils.isEmpty(futureHolds)) {
            codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_FUTUREHOLD, futureHolds);
        }
        log.trace("!ArrayUtils.isEmpty(processHolds) : {}",!CimArrayUtils.isEmpty(processHolds));
        if (!CimArrayUtils.isEmpty(processHolds)) {
            codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_PROCESSHOLD, processHolds);
        }
        log.trace("!ArrayUtils.isEmpty(lotHolds) : {}",!CimArrayUtils.isEmpty(lotHolds));
        if (!CimArrayUtils.isEmpty(lotHolds)) {
            List<ObjectIdentifier> tmp = new ArrayList<>();
            for (int i = 0; i < lotHolds.size(); i++) {
                tmp.add(lotHolds.get(i));
                try {
                    codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_LOTHOLD, tmp);
                } catch (ServiceException e) {
                    log.trace("TransactionIDEnum.HOLD_LOT_REQ.getValue().equals(objCommon.getTransactionID()) : {}",TransactionIDEnum.HOLD_LOT_REQ.getValue().equals(objCommon.getTransactionID()));
                    if (TransactionIDEnum.HOLD_LOT_REQ.getValue().equals(objCommon.getTransactionID())) {
                        throw e;
                    } else {
                        codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_FUTUREHOLD, tmp);
                    }
                }

            }
        }

        //step 10 - lot edit
        log.debug("[step-10] lot edit");
        List<Infos.HoldHistory> holdHistories = lotMethod.lotHoldDepartmentChange(objCommon, lotID, holdReqList);

        /*------------------------------------------------------------------------*/
        /*   Make History                                                         */
        /*------------------------------------------------------------------------*/
        log.debug("Make History ");
        boolean bReasonLotLockFlag = false;
        for (Infos.LotHoldReq lotHoldReq : holdReqList) {
            log.trace("ObjectUtils.equalsWithValue(lotHoldReq.getHoldReasonCodeID(), BizConstant.SP_REASON_LOTLOCK) : {}", CimObjectUtils.equalsWithValue(lotHoldReq.getHoldReasonCodeID(), BizConstant.SP_REASON_LOTLOCK));
            if (CimObjectUtils.equalsWithValue(lotHoldReq.getHoldReasonCodeID(), BizConstant.SP_REASON_LOTLOCK)) {
                bReasonLotLockFlag = true;
                break;
            }
        }
        log.trace("!bReasonLotLockFlag : {}",!bReasonLotLockFlag);
        if (!bReasonLotLockFlag){
            Inputs.LotHoldEventMakeParams lotHoldEventMakeParams = new Inputs.LotHoldEventMakeParams();
            lotHoldEventMakeParams.setTransactionID(TransactionIDEnum.HOLD_LOT_REQ.getValue());
            lotHoldEventMakeParams.setLotID(lotID);
            lotHoldEventMakeParams.setHoldHistoryList(holdHistories);
            eventMethod.lotHoldEventMake(objCommon, lotHoldEventMakeParams);
        }
    }

    @Override
    public void sxLotNpwUsageRecycleLimitUpdateReq(Infos.ObjCommon objCommon, LotNpwUsageRecycleLimitUpdateParams params) {
        //lock the lot
        log.debug("lock lot object");
        objectLockMethod.objectLock(objCommon, CimLot.class, params.getLotID());
        //call lotNpwUsageRecycleUpdate
        log.debug("call lotNpwUsageRecycleUpdate");
        lotMethod.lotNpwUsageRecycleLimitUpdate(params, objCommon);
    }

    @Override
    public void sxLotNpwUsageRecycleCountUpdateReq(Infos.ObjCommon objCommon, LotNpwUsageRecycleCountUpdateParams params) {
        //lock the lot
        log.debug("lock lot object");
        objectLockMethod.objectLock(objCommon, CimLot.class, params.getLotID());
        //call lotNpwUsageRecycleUpdate
        log.debug("call lotNpwUsageRecycleUpdate");
        lotMethod.lotNpwUsageRecycleCountUpdate(params, objCommon);
    }

    @Override
    public void sxNPWLotAutoSkipCheckAndExcute(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        //lock the lot
        log.debug("log lot object");
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);
        //check the lot state if need to skip
        log.debug("check the lot state if need to skip");
        boolean skipFlag = lotMethod.checkNPWLotSkipNeedReq(objCommon,lotID);
        //skip the lot to the first step if need
        log.debug("skip the lot to the first step if need");
        log.trace("skipFlag : {}",skipFlag);
        if (skipFlag){
            //get the first opeNum
            log.debug("get the first opeNum");
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
            String fisrtStepOpeNumByLot = processMethod.getFisrtStepOpeNumByLot(objCommon, lotID);
            Params.SkipReqParams skipReqParams = processMethod.prepareForSkip(objCommon,fisrtStepOpeNumByLot, lotID);
            log.debug("sent skip request");
            this.sxSkipReq(objCommon, skipReqParams);
        }
    }

    @Override
    public void sxTerminateReq(Infos.ObjCommon objCommon, TerminateReq.TerminateReqParams params) {
        if (log.isDebugEnabled())
            log.debug("ObjCommon = {}, ReqParams = {}", objCommon, params);

        CimLot lot = checkParamAndGetLotForTerminate(objCommon, params.getLotID(), params.getReasonCodeID());

        ObjectIdentifier cassetteID = lotMethod.lotCassetteGet(objCommon, lot);
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);

        // check the Lot is in Post Process
        Validations.check(lot.isPostProcessFlagOn(), retCodeConfig.getLotInPostProcess());

        // 只有Engineer Lot和NPW Lot可以执行Terminate操作（体现为Lot Type限制, 有开关控制）
        // 开关OM_LOT_TERMINATE_CHK_TYPE: 0.关 1.开
        if (nullToEmpty(StandardProperties.OM_LOT_TERMINATE_CHK_TYPE.getValue()).trim().equals("1")) {
            // Engineer Lot: Engineering
            // NPW Lot: Auto Monitor, Dummy, Process Monitor, Recycle
            Validations.check(CimStringUtils.unEqualIn(lot.getLotType(),
                    BizConstant.SP_LOT_TYPE_ENGINEERINGLOT,
                    BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT,
                    BizConstant.SP_LOT_TYPE_DUMMYLOT,
                    BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT,
                    BizConstant.SP_LOT_TYPE_RECYCLELOT),
                    retCodeConfig.getInvalidLotType(), lot.getLotType(), lot.getLotID());
        }

        // 当只有Lot的主状态为 Waiting/OnHold/NonProBank/Completed 时才可以执行Terminate操作
        // 要求与现有Scrap操作限制条件相同, 所以屏蔽主状态检查
        // Validations.check(!BizConstant.SP_LOT_PROCSTATE_WAITING.equalsIgnoreCase(lot.getLotProcessState())
        //         && !BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD.equalsIgnoreCase(lot.getLotHoldState())
        //         && !BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK.equalsIgnoreCase(lot.getLotInventoryState())
        //         && !CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED.equalsIgnoreCase(lot.getLotFinishedState()),
        //         retCodeConfig.getInvalidStateTransition());

        // Lot state must be Active or Finished
        Validations.check(!CIMStateConst.CIM_LOT_STATE_FINISHED.equalsIgnoreCase(lot.getLotState())
                && !CIMStateConst.CIM_LOT_STATE_ACTIVE.equalsIgnoreCase(lot.getLotState()),
                retCodeConfig.getInvalidLotStat(), lot.getLotState());

        // check if the state of Lot is Terminated
        Validations.check(BizConstant.SP_LOT_FINISHED_STATE_TERMINATED.equalsIgnoreCase(lot.getLotFinishedState()),
                retCodeConfig.getInvalidLotFinishStat(), lot.getLotFinishedState());

        // FinishedStatus should Completed, in the case of "lotState = Finished"
        Validations.check(CIMStateConst.CIM_LOT_STATE_FINISHED.equalsIgnoreCase(lot.getLotState())
                && !BizConstant.SP_LOT_FINISHED_STATE_COMPLETED.equalsIgnoreCase(lot.getLotFinishedState()),
                retCodeConfig.getInvalidLotFinishStat(), lot.getLotFinishedState());

        // ProcessState should not be Processing
        Validations.check(BizConstant.SP_LOT_PROCSTATE_PROCESSING.equalsIgnoreCase(lot.getLotProcessState()),
                retCodeConfig.getInvalidLotProcessState(), lot.getLotID(), lot.getLotProcessState());

        // check bank param
        Validations.check(
                !BizConstant.SP_LOT_INVENTORYSTATE_INBANK.equalsIgnoreCase(lot.getLotInventoryState())
                && !BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK.equalsIgnoreCase(lot.getLotInventoryState())
                && ObjectIdentifier.isEmptyWithValue(params.getBankID()), retCodeConfig.getInvalidInputParam());

        // check Lock Hold
        lotMethod.lotCheckLockHoldConditionForOperation(objCommon, Collections.singletonList(lot.getLotID()));

        // check bonding group
        String bondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon, lot.getLotID());
        Validations.check(CimStringUtils.isNotEmpty(bondingGroupID), retCodeConfig.getLotHasBondingGroup(),
                lot.getIdentifier(), bondingGroupID);

        // remove the Lot from the monitor group
        lotMethod.lotRemoveFromMonitorGroup(objCommon, lot);

        // check Backup Info
        Infos.LotBackupInfo lotBackupInfo = lotMethod.lotBackupInfoGet(objCommon, lot);
        Validations.check(CimBooleanUtils.isFalse(lotBackupInfo.getCurrentLocationFlag())
                        || CimBooleanUtils.isTrue(lotBackupInfo.getTransferFlag()),
                retCodeConfig.getLotInOthersite(), lot.getLotID());

        // 检查Lot的EI状态(有开关控制)
        // 开关OM_CARRIER_CHK_EI_FOR_LOT_OPERATION: 0.关 1.开
        if (nullToEmpty(StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue()).trim().equals("1")) {
            if (ObjectIdentifier.isNotEmptyWithValue(cassetteID)) {
                // DispatchState should be No
                Validations.check(CimBooleanUtils.isTrue(cassetteMethod.cassetteDispatchStateGet(objCommon,
                        cassetteID)), retCodeConfig.getInvalidCastDispatchStat());

                // TransferState should be EO/MO/IO/HO/AO
                String transferState = lotMethod.lotTransferStateGet(objCommon, lot);
                Validations.check(CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT, transferState)
                                || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState),
                        retCodeConfig.getInvalidLotXferstat(), lot.getLotID(), transferState);
            }
        }

        // check FlowBatch Condition
        try {
            lotMethod.lotFlowBatchIDGet(objCommon, lot);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())) {
                throw new ServiceException(retCodeConfig.getFlowBatchLimitation());
            } else if (!Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())) {
                throw e;
            }
        }

        // check the Lot's control job ID
        Validations.check(ObjectIdentifier.isNotEmptyWithValue(lotMethod.lotControlJobIDGet(objCommon, lot)),
                retCodeConfig.getLotControlJobidFilled());

        // exec Lot in NonProdBank, if the Lot is not in bank
        if (!BizConstant.SP_LOT_INVENTORYSTATE_INBANK.equalsIgnoreCase(lot.getLotInventoryState())
                && !BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK.equalsIgnoreCase(lot.getLotInventoryState())) {
            Params.NonProdBankStoreReqParams nonProdBankStoreReqParams = new Params.NonProdBankStoreReqParams();
            nonProdBankStoreReqParams.setUser(objCommon.getUser());
            nonProdBankStoreReqParams.setLotID(lot.getLotID());
            nonProdBankStoreReqParams.setBankID(params.getBankID());
            bankService.sxNonProdBankStoreReq(objCommon, nonProdBankStoreReqParams);
        }

        // exec Lot Terminate
        lotMethod.lotTerminate(objCommon, lot);

        terminateFinal(objCommon, lot, cassetteID, params.getReasonCodeID(), params.getClaimMemo());
    }

    @Override
    public void sxTerminateCancelReq(Infos.ObjCommon objCommon, TerminateReq.TerminateCancelReqParams params) {
        if (log.isDebugEnabled())
            log.debug("ObjCommon = {}, ReqParams = {}", objCommon, params);

        CimLot lot = checkParamAndGetLotForTerminate(objCommon, params.getLotID(), params.getReasonCodeID());

        // check if the state of Lot is Terminated
        Validations.check(!BizConstant.SP_LOT_FINISHED_STATE_TERMINATED.equalsIgnoreCase(lot.getLotFinishedState()),
                retCodeConfig.getInvalidLotFinishStat(), lot.getLotFinishedState());

        ObjectIdentifier cassetteID = lotMethod.lotCassetteGet(objCommon, lot);
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);

        // exec Lot Terminate Cancel
        lotMethod.lotTerminateCancel(objCommon, lot);

        terminateFinal(objCommon, lot, cassetteID, params.getReasonCodeID(), params.getClaimMemo());
    }

    @SuppressWarnings("deprecation")
    private CimLot checkParamAndGetLotForTerminate(Infos.ObjCommon objCommon, ObjectIdentifier lotID,
                                                   ObjectIdentifier reasonCodeID) {
        Validations.check(CimObjectUtils.isEmptyWithValue(lotID)
                        || CimObjectUtils.isEmptyWithValue(reasonCodeID), retCodeConfig.getInvalidInputParam());

        // check reasonCode is correct
        codeMethod.codeCheckExistanceDR(objCommon, objCommon.getTransactionID().equals(
                TransactionIDEnum.LOT_TERMINATE_REQ.getValue()) ? BizConstant.SP_OPERATIONCATEGORY_LOTTERMINATE
                        : BizConstant.SP_OPERATIONCATEGORY_LOTTERMINATECANCEL,
                Collections.singletonList(reasonCodeID));

        CimLot lot = baseCoreFactory.getBO(CimLot.class, lotID);
        if (lot == null)
            throw new ServiceException(new OmCode(retCodeConfig.getNotFoundLot(), lotID));

        return lot;
    }

    private void terminateFinal(Infos.ObjCommon objCommon, CimLot lot, ObjectIdentifier cassetteID,
                                ObjectIdentifier reasonCodeID, String claimMemo) {
        // update the specified cassette's MultiLotType
        if (ObjectIdentifier.isNotEmptyWithValue(cassetteID)) {
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
        }

        // update lot history pointer
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, lot);

        // create operation history
        TerminateReq.TerminateEventMakeParams eventMakeParams = new TerminateReq.TerminateEventMakeParams();
        eventMakeParams.setTransactionID(objCommon.getTransactionID());
        eventMakeParams.setLotID(lot.getLotID());
        eventMakeParams.setReasonCodeID(reasonCodeID);
        eventMakeParams.setClaimMemo(claimMemo);
        eventMethod.lotTerminateEventMake(objCommon, lot.getEventData(), eventMakeParams);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Boolean lotMergeCheck(Infos.ObjCommon objCommon, Params.MergeLotReqParams mergeLotReqParams,
                                             Boolean updateControlJobFlag ) {
        //------------------------------------------------------------------------
        //   Get cassette / lot connection
        //------------------------------------------------------------------------
        log.debug("Get cassette / lot connection");
        ObjectIdentifier aParentCassetteID = lotMethod.lotCassetteGet(objCommon,mergeLotReqParams.getParentLotID());

        ObjectIdentifier aChildCassetteID = lotMethod.lotCassetteGet(objCommon,mergeLotReqParams.getChildLotID());

        //--------------------------------
        //   CassetteID should be same!
        //--------------------------------
        log.debug("CassetteID should be same!");
        int nums = lotMethod.countQuantityWaferBylot(objCommon,
                ObjectIdentifier.fetchValue(mergeLotReqParams.getChildLotID()),aChildCassetteID);
        if (nums == 0) {
            Validations.check(!CimStringUtils.equals(aChildCassetteID.getValue(), aParentCassetteID.getValue()), retCodeConfig.getCassetteNotSame());
        }
        //--------------------------------
        //   Lock objects to be updated
        //--------------------------------
        log.debug(" Lock objects to be updated");
        String tmpLotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        tmpLotOperationEIcheck = null == tmpLotOperationEIcheck ? "0" : tmpLotOperationEIcheck;
        Integer lotOperationEIcheck = Integer.valueOf(tmpLotOperationEIcheck);
        //Boolean updateControlJobFlag = false;
        Long lockMode = 0L;
        String cassetteTransferStateRetCode = null;
        Outputs.ObjCassetteEquipmentIDGetOut objCassetteEquipmentIDGetOut = null;
        log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !StringUtils.isEmpty(aParentCassetteID) : {},",
                CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !CimStringUtils.isEmpty(aParentCassetteID));
        if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !CimStringUtils.isEmpty(aParentCassetteID)) {
            //-------------------------------
            // Get carrier transfer status
            //-------------------------------
            log.debug("Get carrier transfer status");
            cassetteTransferStateRetCode = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
            /*------------------------------------*/
            /*   Get eqp ID in cassette     */
            /*------------------------------------*/
            log.debug("Get eqp ID in cassette");
            objCassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, aParentCassetteID);
            //-------------------------------
            // Get required eqp lock mode
            //-------------------------------
            // object_lockMode_Get
            log.debug(" Get required eqp lock mode");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(objCassetteEquipmentIDGetOut.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.MERGE_WAFER_LOT_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            log.trace("StringUtils.equals(cassetteTransferStateRetCode, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) : {}", CimStringUtils.equals(cassetteTransferStateRetCode, BizConstant.SP_TRANSSTATE_EQUIPMENTIN));
            if (CimStringUtils.equals(cassetteTransferStateRetCode, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                updateControlJobFlag = true;
                // advanced_object_Lock
                log.debug("advanced_object_Lock");
                log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                    //  advanced_object_Lock
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                            (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, Collections.singletonList(aParentCassetteID.getValue())));
                } else {
                    // object_Lock
                    objectLockMethod.objectLock(objCommon, CimMachine.class, objCassetteEquipmentIDGetOut.getEquipmentID());
                }
            }
        }
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimCassette.class, aParentCassetteID);
        log.trace("StringUtils.equals(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !StringUtils.isEmpty(aParentCassetteID) : {}",
                CimStringUtils.equals(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !CimStringUtils.isEmpty(aParentCassetteID));
        if (CimStringUtils.equals(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) && !CimStringUtils.isEmpty(aParentCassetteID)) {
            // object_Lock
            log.trace("!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
            if (!updateControlJobFlag || !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                //---------------------------------
                //   Get Cassette's ControlJobID
                //---------------------------------
                log.debug("Get Cassette's ControlJobID");
                ObjectIdentifier controlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, aParentCassetteID);
                log.trace("!ObjectUtils.isEmptyWithValue(controlJobID) : {}",!CimObjectUtils.isEmptyWithValue(controlJobID));
                if (!CimObjectUtils.isEmptyWithValue(controlJobID)){
                    updateControlJobFlag = true;
                    log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                    if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                        /*------------------------------*/
                        /*   Lock ControlJob Object     */
                        /*------------------------------*/
                        log.debug(" Lock ControlJob Object");
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
                    }
                }
            }
        }
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimCassette.class, aChildCassetteID);
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimLot.class, mergeLotReqParams.getParentLotID());
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimLot.class, mergeLotReqParams.getChildLotID());
        //------------------------------------
        // Check LOCK Hold.
        //------------------------------------
        log.debug("Check LOCK Hold. ");
        List<ObjectIdentifier> lotIDSeq = new ArrayList<>();
        lotIDSeq.add(mergeLotReqParams.getParentLotID());
        lotIDSeq.add(mergeLotReqParams.getChildLotID());
        for (int i = 0; i < CimArrayUtils.getSize(lotIDSeq); i++) {
            //----------------------------------
            //  Check lot InterFabXfer state
            //----------------------------------
            String interFabXferStateGet = lotMethod.lotInterFabXferStateGet(objCommon, lotIDSeq.get(i));
            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferStateGet) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferStateGet));
            if (CimObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferStateGet)) {
                log.debug(" #### The Lot interFabXfer state is required... No need to check LOCK Hold. ");
                continue;
            }
            //lot_CheckLockHoldConditionForOperation
            log.debug("lot_CheckLockHoldConditionForOperation");
            lotMethod.lotCheckLockHoldConditionForOperation(objCommon,lotIDSeq);
        }

        /*------------------------------------------------------------------------*/
        /*  Check Lot Split Or Merge                                              */
        /*------------------------------------------------------------------------*/
        log.debug("Check Lot Split Or Merge");
        this.checkLotSplitOrMerge(objCommon,lotIDSeq, BizConstant.CHECK_LOT_ACTION_MERGE);

        //-----------------------------
        //  Check InPostProcessFlag
        //-----------------------------
        log.debug("Check InPostProcessFlag.");
        List<ObjectIdentifier> userGroupIDs = new ArrayList<>();
        for (ObjectIdentifier s : lotIDSeq) {
            //----------------------------------
            //  Get InPostProcessFlag of lot
            //----------------------------------
            log.debug("Get InPostProcessFlag of lot");
            Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, s);
            //----------------------------------
            //  Check lot InterFabXfer state
            //----------------------------------
            log.debug("Check lot InterFabXfer state");
            String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, s);
            //----------------------------------------------
            //  If lot is in post process, returns error
            //----------------------------------------------
            log.debug("If lot is in post process, returns error");
            log.trace("lotInPostProcessFlagOut.getInPostProcessFlagOfLot() : {}",lotInPostProcessFlagOut.getInPostProcessFlagOfLot());
            if (lotInPostProcessFlagOut.getInPostProcessFlagOfLot()) {
                log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferState) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferState));
                if (CimObjectUtils.equalsWithValue(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED,interFabXferState)) {
                    log.debug(" #### The Lot interFabXfer state is required... No need to check post process flag. ");
                    continue;
                }
                log.trace("0 == ArrayUtils.getSize(userGroupIDs) : {}",0 == CimArrayUtils.getSize(userGroupIDs));
                if (0 == CimArrayUtils.getSize(userGroupIDs)) {
                    /*---------------------------*/
                    /* Get UserGroupID By UserID */
                    /*---------------------------*/
                    log.debug("Get UserGroupID By UserID");
                    userGroupIDs =  personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                }
                int j = 0;
                for (j = 0; j < CimArrayUtils.getSize(userGroupIDs); j++) {
                }
                Validations.check (j == CimArrayUtils.getSize(userGroupIDs), retCodeConfig.getLotInPostProcess());
            }
        }

        /*-------------------------------*/
        /*   Check SorterJob existence   */
        /*-------------------------------*/
        log.debug("Check SorterJob existence");
        List<ObjectIdentifier> dummyCastIDs = new ArrayList<>();
        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(new Infos.EquipmentLoadPortAttribute());
        objWaferSorterJobCheckForOperation.setCassetteIDList(dummyCastIDs);
        objWaferSorterJobCheckForOperation.setLotIDList(lotIDSeq);
        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);

        //actioCode=Combine跳过此检查，此时lot已经存在sortJob了
        if(!objCommon.getTransactionID().equals(TransactionIDEnum.SORT_ACTION_RPT.getValue())) {
            waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
        //---------------------------------
        // Check carrier dispatch status
        //---------------------------------
        log.debug("Check carrier dispatch status");
        String transferStateGetOutRetCode = null;
        log.trace("!StringUtils.isEmpty(aParentCassetteID) : {}",!CimStringUtils.isEmpty(aParentCassetteID));
            if (!CimStringUtils.isEmpty(aParentCassetteID)) {
                log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE,lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE, lotOperationEIcheck.toString()));
                if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ONE, lotOperationEIcheck.toString())) {
                    Boolean castDisStateOut = cassetteMethod.cassetteDispatchStateGet(objCommon, aParentCassetteID);
                    Validations.check(castDisStateOut, retCodeConfig.getNotFoundCst());
                }
                //---------------------------------
                // Check carrier transfer status
                //---------------------------------
                log.debug("Check carrier transfer status");
                log.trace("!StringUtils.isEmpty(aParentCassetteID)\n" +
                        "                    || (StringUtils.isEmpty(aParentCassetteID))\n" +
                        "                    && !ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateRetCode) : {}", !CimStringUtils.isEmpty(aParentCassetteID)
                        || (CimStringUtils.isEmpty(aParentCassetteID))
                        && !CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateRetCode));
                if (!CimStringUtils.isEmpty(aParentCassetteID)
                        || (CimStringUtils.isEmpty(aParentCassetteID))
                        && !CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateRetCode)) {
                    transferStateGetOutRetCode = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
                    log.trace("StringUtils.isEmpty(aParentCassetteID) : {}", CimStringUtils.isEmpty(aParentCassetteID));
                    if (CimStringUtils.isEmpty(aParentCassetteID)) {
                        log.debug("Changed to EI by other operation");
                        Validations.check(CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateRetCode), retCodeConfig.getChangedToEiByOtherOperation());
                    } else if (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_BAYOUT, transferStateGetOutRetCode)
                            || CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferStateGetOutRetCode)) {
                        log.debug("XferState is invalid...");
                        Validations.check(true, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferStateGetOutRetCode, CimObjectUtils.getObjectValue(aParentCassetteID)));
                    }
                }
                log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO, lotOperationEIcheck.toString()));
                if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO, lotOperationEIcheck.toString())) {
                    transferStateGetOutRetCode = cassetteTransferStateRetCode;
                }
            }
            log.trace("!StringUtils.isEmpty(aParentCassetteID)");

            if (!CimStringUtils.isEmpty(aParentCassetteID)) {
                log.trace("ObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO,lotOperationEIcheck.toString()) : {}", CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO, lotOperationEIcheck.toString()));
                if (CimObjectUtils.equalsWithValue(BizConstant.CONSTANT_QUANTITY_ZERO, lotOperationEIcheck.toString())) {
                    log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,transferStateGetOutRetCode) : {}", CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferStateGetOutRetCode));
                    if (CimObjectUtils.equalsWithValue(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferStateGetOutRetCode)) {
                        /*------------------------------------*/
                        /*   Get eqp port Info          */
                        /*------------------------------------*/
                        log.debug("Get eqp port Info ");
                        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, objCassetteEquipmentIDGetOut.getEquipmentID());
                        Integer portLen = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                        int i = 0;
                        for (i = 0; i < portLen; i++) {
                            log.trace("ObjectUtils.equalsWithValue(aParentCassetteID.getValue(),eqpPortInfo.getEqpPortStatuses().get(i).getLoadedCassetteID()) : {}", CimObjectUtils.equalsWithValue(aParentCassetteID.getValue(), eqpPortInfo.getEqpPortStatuses().get(i).getLoadedCassetteID()));
                            if (CimObjectUtils.equalsWithValue(aParentCassetteID.getValue(), eqpPortInfo.getEqpPortStatuses().get(i).getLoadedCassetteID())) {
                                log.debug("parentCassetteID == loadedCassetteID");
                                break;
                            }
                        }
                        //-----------------------------------------------------------------
                        // Check parent lot and child lot is same operationStartFlag or not
                        //-----------------------------------------------------------------
                        log.debug("Check parent lot and child lot is same operationStartFlag or not");
                        boolean bParentLotOpeStartFlg = false;
                        boolean bChildLotOpeStartFlg = false;
                        if (i < portLen) {
                            for (int j = 0; j < CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses().get(i).getLotOnPorts()); j++) {
                                Infos.LotOnPort lotOnPort = eqpPortInfo.getEqpPortStatuses().get(i).getLotOnPorts().get(j);
                                log.trace("ObjectUtils.equalsWithValue(mergeLotReqParams.getParentLotID(),lotOnPort.getLotID()) : {}", CimObjectUtils.equalsWithValue(mergeLotReqParams.getParentLotID(), lotOnPort.getLotID()));
                                if (CimObjectUtils.equalsWithValue(mergeLotReqParams.getParentLotID(), lotOnPort.getLotID())) {
                                    log.debug("parentLotID == lotID");
                                    bParentLotOpeStartFlg = lotOnPort.isMoveInFlag();
                                } else if (CimObjectUtils.equalsWithValue(mergeLotReqParams.getChildLotID(), lotOnPort.getLotID())) {
                                    log.debug("childLotID == lotID");
                                    bChildLotOpeStartFlg = lotOnPort.isMoveInFlag();
                                }
                            }
                        }
                        Validations.check(bChildLotOpeStartFlg != bParentLotOpeStartFlg, retCodeConfig.getAttributeDifferentForMerge());
                    }
                }
            }

        }
        //当actioCode=Combine此时的TransactionID由替OSRTR005换成merge->MERGE_WAFER_LOT_REQ("OLOTW033")为了增加merge的操作记录
        if(objCommon.getTransactionID().equals(TransactionIDEnum.SORT_ACTION_RPT.getValue())) {
            objCommon.setTransactionID(TransactionIDEnum.MERGE_WAFER_LOT_REQ.getValue());
        }
        //------------------------------------------------------------------------
        //   Check Condition
        //------------------------------------------------------------------------
        //---------------------------------
        // Check Contents for parent lot
        //---------------------------------
        log.debug("Check Contents for parent lot");
        String lotContents = lotMethod.lotContentsGet(mergeLotReqParams.getParentLotID());

        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER,lotContents)
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE,lotContents) , retCodeConfig.getInvalidLotContents());
        //---------------------------------
        // Check Contents for child lot
        //---------------------------------
        log.debug("Check Contents for child lot");
        String lotContentsChild = lotMethod.lotContentsGet(mergeLotReqParams.getChildLotID());
        Validations.check (!CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_WAFER,lotContentsChild)
                && !CimObjectUtils.equalsWithValue(BizConstant.SP_PRODTYPE_DIE,lotContentsChild) , retCodeConfig.getInvalidLotContents());
        //---------------------------------------
        // Check Finished State for parent lot
        //---------------------------------------
        log.debug("Check Finished State for parent lot");
        String lotFinishedStateParent = lotMethod.lotFinishedStateGet(objCommon, mergeLotReqParams.getParentLotID());
        Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_FINISHED_STATE_STACKED,lotFinishedStateParent), retCodeConfig.getInvalidLotFinishStat());
        //---------------------------------------
        // Check Finished State for child lot
        //---------------------------------------
        log.debug("Check Finished State for child lot");
        String lotFinishedStateChild = lotMethod.lotFinishedStateGet(objCommon, mergeLotReqParams.getChildLotID());
        Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_FINISHED_STATE_STACKED,lotFinishedStateChild), retCodeConfig.getInvalidLotFinishStat());

        //---------------------------------------
        // Check Bonding Group for parent lot
        //---------------------------------------
        log.debug("Check Bonding Group for parent lot");
        String groupIDGetDROut = lotMethod.lotBondingGroupIDGetDR(objCommon, mergeLotReqParams.getParentLotID());

        log.trace("!StringUtils.isEmpty(groupIDGetDROut) : {}",!CimStringUtils.isEmpty(groupIDGetDROut));
        if (!CimStringUtils.isEmpty(groupIDGetDROut)) {
            throw new ServiceException(new OmCode(retCodeConfig.getLotHasBondingGroup(),mergeLotReqParams.getParentLotID().getValue(),groupIDGetDROut));
        }
        //---------------------------------------
        // Check Bonding Group for child lot
        //---------------------------------------
        log.debug("Check Bonding Group for child lot");
        String groupIDGetDROut1 = lotMethod.lotBondingGroupIDGetDR(objCommon, mergeLotReqParams.getChildLotID());
        log.trace("!StringUtils.isEmpty(groupIDGetDROut1) : {}",!CimStringUtils.isEmpty(groupIDGetDROut1));
        if (!CimStringUtils.isEmpty(groupIDGetDROut1)) {
            throw new ServiceException(new OmCode(retCodeConfig.getLotHasBondingGroup(),mergeLotReqParams.getChildLotID().getValue(),groupIDGetDROut));
        }
        //------------------------------------------------------
        // Check parent lot and child lot is same state or not
        //------------------------------------------------------
        log.debug("Check parent lot and child lot is same state or not");
        Outputs.ObjLotAllStateCheckSame objLotAllStateCheckSameRetCode = null;
        try {
            objLotAllStateCheckSameRetCode = lotMethod.lotAllStateCheckSame(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());
        } catch (ServiceException e) {
            objLotAllStateCheckSameRetCode = (Outputs.ObjLotAllStateCheckSame)e.getData();
            log.trace("!\"HOLDSTATE\".equals(objLotAllStateCheckSameRetCode.getLotHoldState()) : {}",!"HOLDSTATE".equals(objLotAllStateCheckSameRetCode.getLotHoldState()));
            if (!"HOLDSTATE".equals(objLotAllStateCheckSameRetCode.getLotHoldState())) {
                throw e;
            }
        }

        //------------------------------------------------------
        // Check lot state
        //------------------------------------------------------
        log.debug("Check lot state");
        log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,objLotAllStateCheckSameRetCode.getLotInventoryState())\n" +
                        "                || ObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK,objLotAllStateCheckSameRetCode.getLotInventoryState()) : {}",
                CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,objLotAllStateCheckSameRetCode.getLotInventoryState())
                        || CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK,objLotAllStateCheckSameRetCode.getLotInventoryState()));
        if (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,objLotAllStateCheckSameRetCode.getLotInventoryState())
                || CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK,objLotAllStateCheckSameRetCode.getLotInventoryState())) {
            log.debug("Check BankID.");
            //------------------------------------------------------
            // Check BankID.
            //------------------------------------------------------
            String retCode = lotMethod.lotBankCheckSame(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());
        }
        //--- Check for lot State -----//
        log.debug("Check for lot State");
        Validations.check (CimObjectUtils.equalsWithValue(CIMStateConst.CIM_LOT_STATE_SHIPPED,objLotAllStateCheckSameRetCode.getLotState()), retCodeConfig.getInvalidLotStat());
        //--- Check for lot Process State -----//
        log.debug("Check for lot Process State");
        Validations.check (CimObjectUtils.equalsWithValue(BizConstant.SP_LOT_PROCSTATE_PROCESSING,objLotAllStateCheckSameRetCode.getLotProcessState()), retCodeConfig.getInvalidLotProcstat());
        /*-------------------------------*/
        /*   Check flowbatch Condition   */
        /*-------------------------------*/
        //lot_flowBatchID_Get
        log.debug("Check FlowBatch Condition  [ParentLot]");
        try {
            ObjectIdentifier flowBatchIDParentLot = lotMethod.lotFlowBatchIDGet(objCommon, mergeLotReqParams.getParentLotID());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(),e.getCode())){
                log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
                throw new ServiceException(retCodeConfig.getFlowBatchLimitation());
            }else if(Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(),e.getCode())){
                log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
            }else{
                log.debug("lot_flowBatchID_Get() != RC_OK");
                throw e;
            }
        }

        //lot_flowBatchID_Get
        log.debug("Check FlowBatch Condition  [ChildLot]");
        try {
            ObjectIdentifier flowBatchIDChildLot = lotMethod.lotFlowBatchIDGet(objCommon, mergeLotReqParams.getChildLotID());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(),e.getCode())){
                log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
                throw new ServiceException(retCodeConfig.getFlowBatchLimitation());
            }else if(Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(),e.getCode())){
                log.debug("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
            }else{
                log.debug("lot_flowBatchID_Get() != RC_OK");
                throw e;
            }
        }
        /*------------------------------------------------------------------------*/
        /*   Check if the wafers in lot don't have machine container position     */
        /*------------------------------------------------------------------------*/
        log.debug("call equipmentContainerPosition_info_GetByLotDR( parentLot )");
        List<Infos.EqpContainerPosition> eqpContainerPositionRetCode = equipmentMethod.equipmentContainerPositionInfoGetByLotDR(objCommon, mergeLotReqParams.getParentLotID());
        Validations.check (0 < CimArrayUtils.getSize(eqpContainerPositionRetCode), retCodeConfig.getWaferInLotHaveContainerPosition());
        // for childLotID
        log.debug("call equipmentContainerPosition_info_GetByLotDR( childLot )");
        List<Infos.EqpContainerPosition> eqpContainerPositionRetCode1 = equipmentMethod.equipmentContainerPositionInfoGetByLotDR(objCommon, mergeLotReqParams.getChildLotID());
        Validations.check (0 < CimArrayUtils.getSize(eqpContainerPositionRetCode1), retCodeConfig.getWaferInLotHaveContainerPosition());
        //----------------------------------
        //   Check lot's Control Job ID
        //----------------------------------
        // for parentLot
        log.debug("Check lot's Control Job ID");
        ObjectIdentifier controlJobIDOut = lotMethod.lotControlJobIDGet(objCommon, mergeLotReqParams.getParentLotID());
        log.trace("!ObjectUtils.isEmpty(controlJobIDOut) : {}",!CimObjectUtils.isEmpty(controlJobIDOut));
        if (!CimObjectUtils.isEmpty(controlJobIDOut)){
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),controlJobIDOut.getValue()));
        }
        // for childLotID
        ObjectIdentifier controlJobIDOut1 = lotMethod.lotControlJobIDGet(objCommon,mergeLotReqParams.getChildLotID());
        log.trace("!ObjectUtils.isEmpty(controlJobIDOut1) : {}",!CimObjectUtils.isEmpty(controlJobIDOut1));
        if (!CimObjectUtils.isEmpty(controlJobIDOut1)){
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),controlJobIDOut1.getValue()));
        }

        lotFamilyMethod.lotFamilyCheckMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

        processMethod.processCheckMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());
        String holdListCheckMerge = lotMethod.lotHoldListCheckMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

        List<Infos.LotHoldReq>  lotFutureHoldRequestsCheckMerge = lotMethod.lotFutureHoldRequestsCheckMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

        log.trace("!ArrayUtils.isEmpty(lotFutureHoldRequestsCheckMerge) : {}",!CimArrayUtils.isEmpty(lotFutureHoldRequestsCheckMerge));
        if (!CimArrayUtils.isEmpty(lotFutureHoldRequestsCheckMerge)) {
            Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
            holdLotReleaseReqParams.setReleaseReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGE));
            holdLotReleaseReqParams.setLotID(mergeLotReqParams.getChildLotID());
            holdLotReleaseReqParams.setHoldReqList(lotFutureHoldRequestsCheckMerge);
            holdLotReleaseReqParams.setUser(objCommon.getUser());
            sxHoldLotReleaseReq(objCommon,holdLotReleaseReqParams);

            // sampling , checking sampling after hold release
            log.debug("sampling , checking sampling after hold release");
            samplingService.sxLotSamplingCheckThenSkipReq(objCommon, mergeLotReqParams.getChildLotID(), TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue(), BizConstant.LS_CHILD_LOT_EXECUTE);

            for (int i = 0; i < CimArrayUtils.getSize(lotFutureHoldRequestsCheckMerge); i++) {
                lotFutureHoldRequestsCheckMerge.get(i).setRelatedLotID(mergeLotReqParams.getChildLotID());
            }
            Params.HoldLotReleaseReqParams holdLotReleaseReqParams1 = new Params.HoldLotReleaseReqParams();
            holdLotReleaseReqParams1.setReleaseReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_MERGE));
            holdLotReleaseReqParams1.setLotID(mergeLotReqParams.getParentLotID());
            holdLotReleaseReqParams1.setHoldReqList(lotFutureHoldRequestsCheckMerge);
            holdLotReleaseReqParams1.setUser(objCommon.getUser());
            sxHoldLotReleaseReq(objCommon,holdLotReleaseReqParams1);

            // sampling , checking sampling after hold release
            log.debug(" sampling , checking sampling after hold release");
            samplingService.sxLotSamplingCheckThenSkipReq(objCommon, mergeLotReqParams.getParentLotID(), TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue(), BizConstant.LS_BASIC_LOT_EXECUTE);

        }
        /*-----------------------------------*/
        /*   Check Future Action Procedure   */
        /*-----------------------------------*/
        // schdlChangeReservation_CheckForMerge
        log.debug("Check Future Action Procedure :: schdlChangeReservation_CheckForMerge");
        scheduleChangeReservationMethod.schdlChangeReservationCheckForMerge(objCommon,mergeLotReqParams.getParentLotID(),mergeLotReqParams.getChildLotID());

        /*----------------------------------------*/
        /*   Check Q-Time information condition   */
        /*----------------------------------------*/
        // qTime_CheckForMerge
        log.debug("Check Q-Time information condition :: qTime_CheckForMerge");
        qTimeMethod.qTimeCheckForMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());


        // check child and parent the contamination level and pr flag
        log.debug("check child and parent the contamination level and pr flag");
        contaminationMethod.lotCheckContaminationLevelAndPrFlagMatchError(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

        return updateControlJobFlag;
    }

    @Override
    public void lotMerge(Infos.ObjCommon objCommon, Params.MergeLotReqParams mergeLotReqParams,ObjectIdentifier originalCarrierID) {
        //------------------------------------------------------------------------
        //   Get cassette / lot connection
        //------------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Get cassette / lot connection");
        }
        ObjectIdentifier aParentCassetteID = lotMethod.lotCassetteGet(objCommon,mergeLotReqParams.getParentLotID());
        int nums = lotMethod.countQuantityWaferBylot(objCommon, ObjectIdentifier.fetchValue(mergeLotReqParams.getChildLotID()),originalCarrierID);
        //------------------------------------------------------------------------
        //  nums==0时子批跟母批在同一个carrier,此时合批
        //------------------------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("nums,",nums);
        }
        if (nums == 0) {
            Boolean updateControlJobFlag = this.lotMergeCheck(objCommon,mergeLotReqParams,false);
            //------------------------------------------------------------------------
            //   //DCR4000125 Create History Event before child lot's wafer become parent lot's wafer
            //   lotWaferMoveEvent_MakeMerge() only prepare merge event information.
            //------------------------------------------------------------------------
            // lotWaferMoveEvent_MakeMerge
            log.debug("lotWaferMoveEvent_MakeMerge");
            Inputs.LotWaferMoveEventMakeMergeParams lotWaferMoveEventMakeMergeParams = new Inputs.LotWaferMoveEventMakeMergeParams();
            lotWaferMoveEventMakeMergeParams.setTransactionID(objCommon.getTransactionID());
            lotWaferMoveEventMakeMergeParams.setSourceLotID(mergeLotReqParams.getChildLotID());
            lotWaferMoveEventMakeMergeParams.setDestinationLotID(mergeLotReqParams.getParentLotID());
            Infos.NewLotAttributes newLotAttributesMergeOut = eventMethod.lotWaferMoveEventMakeMerge(objCommon, lotWaferMoveEventMakeMergeParams);

            //------------------------------------------------------------------------
            //   Change State
            //------------------------------------------------------------------------
            //P5100296 Add Start
            //---------------------------------------------------------------------------------
            //  If Child lot is member of Monitor Group,
            //  the following action is performed according to the rule.
            //
            //           |                | Child lot is Monitored lot
            //           | Child lot is   +----------------------+---------------------------
            //    Rule   | Monitoring lot | only 1 Monitored lot | more than 2 Monitored lot
            //   --------+----------------+----------------------+---------------------------
            //    Action | return ERROR   | return ERROR         | remove from Monitor Group
            //---------------------------------------------------------------------------------
            //lot_RemoveFromMonitorGroup
            lotMethod.lotRemoveFromMonitorGroup(objCommon, mergeLotReqParams.getChildLotID());

            String lotMergeWaferLot = lotMethod.lotMergeWaferLot(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());

            /*----------------------------------------*/
            /*   Merge Q-Time information             */
            /*----------------------------------------*/
            // qTime_infoMerge
            log.debug("Merge Q-Time information");
            qTimeMethod.qTimeInfoMerge(objCommon, mergeLotReqParams.getParentLotID(), mergeLotReqParams.getChildLotID());
            //---------------------------------------
            //   Update cassette's MultiLotType
            //---------------------------------------
            log.debug("Update cassette's MultiLotType");
            log.trace("!StringUtils.isEmpty(aParentCassetteID) : {}",!ObjectIdentifier.isEmpty(aParentCassetteID));
            if (!ObjectIdentifier.isEmpty(aParentCassetteID)) {
                log.trace("updateControlJobFlag : {}",updateControlJobFlag);
                if (updateControlJobFlag) {
                    //----------------------
                    // Update control Job Info and
                    // Machine cassette info if information exist
                    //----------------------
                    List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
                    cassetteIDs.add(aParentCassetteID);
                    controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDs);
                }
                cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, aParentCassetteID);
            }
            //------------------------------------------------------------------------
            //   Make History
            //------------------------------------------------------------------------
            if (log.isDebugEnabled()) {
                log.debug("Make History");
            }

            lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, mergeLotReqParams.getParentLotID());

            // Get Entity Inhibition Info
            if (log.isDebugEnabled()) {
                log.debug("Get Entity Inhibition Info");
            }
            lotMethod.lotWaferLotHistoryPointerUpdate(objCommon,mergeLotReqParams.getParentLotID());
            //for Parent lot, for later generating OPEHS

            // lotWaferMoveEvent_Make

            if (log.isDebugEnabled()) {
                log.debug("create Event");
            }
            Infos.NewLotAttributes newLotAttributes = newLotAttributesMergeOut;
            eventMethod.lotWaferMoveEventMake(objCommon, newLotAttributes, objCommon.getTransactionID(), mergeLotReqParams.getClaimMemo());
            //-- Entity Inhibit Exception Lot Data --//
            // TODO: 2019/10/10
            List<CimRestriction> entityInhibitList = entityInhibitManager.getEntityInhibitsWithExceptionLotByLot(mergeLotReqParams.getChildLotID());
            if (!CimObjectUtils.isEmpty(entityInhibitList)){
                Params.MfgRestrictExclusionLotReqParams cancelParams = new Params.MfgRestrictExclusionLotReqParams();
                List<Infos.EntityInhibitExceptionLot> entityInhibitExceptionLotList = new ArrayList<>();
                cancelParams.setEntityInhibitExceptionLots(entityInhibitExceptionLotList);
                for (CimRestriction cimEntityInhibit : entityInhibitList){
                    Infos.EntityInhibitExceptionLot entityInhibitExceptionLot = new Infos.EntityInhibitExceptionLot();
                    entityInhibitExceptionLot.setEntityInhibitID(new ObjectIdentifier(cimEntityInhibit.getIdentifier(),cimEntityInhibit.getPrimaryKey()));
                    entityInhibitExceptionLot.setLotID(mergeLotReqParams.getChildLotID());
                    entityInhibitExceptionLotList.add(entityInhibitExceptionLot);
                }
                String claimMemo = "Delete for MergeWaferLot";
                cancelParams.setClaimMemo(claimMemo);
                cancelParams.setUser(mergeLotReqParams.getUser());
                constraintService.sxMfgRestrictExclusionLotCancelReq(cancelParams, objCommon);

            }
        }
    }

    @Override
    public void lotSplitCheck(Infos.ObjCommon objCommon, Params.SplitLotReqParams splitLotReqParams) {
        Validations.check (CimArrayUtils.isEmpty(splitLotReqParams.getChildWaferIDs()), retCodeConfig.getInvalidParameter());

        Results.SplitLotReqResult retVal = new Results.SplitLotReqResult();
        //入参校验
        //add je rry 如果传入的wafer有重复返回错误
        List<String> list = new ArrayList<>();
        for (ObjectIdentifier childWaferID :  splitLotReqParams.getChildWaferIDs()) {
            list.add(childWaferID.getValue());
        }
        //利用set不重复判断是否值重复
        boolean isRepeat = list.size() != new HashSet<>(list).size();
        Validations.check (isRepeat, retCodeConfig.getInvalidParameter());
        Validations.check (ObjectIdentifier.isEmptyWithValue(splitLotReqParams.getParentLotID()), retCodeConfig.getInvalidParameter());

        /*------------------------------------------------------------------------*/
        /*   Check Condition                                                      */
        /*------------------------------------------------------------------------*/
        log.debug(" Check Condition  ");
        ObjectIdentifier cassetteOut = null;
        try {
            // not found carrier, not dispose
            cassetteOut = lotMethod.lotCassetteGet(objCommon, splitLotReqParams.getParentLotID());
        } catch (ServiceException e) {
            log.trace("!NumberUtils.eq(e.getCode(), retCodeConfig.getNotFoundCst().getCode()) : {}",!CimNumberUtils.eq(e.getCode(), retCodeConfig.getNotFoundCst().getCode()));
            if (!CimNumberUtils.eq(e.getCode(), retCodeConfig.getNotFoundCst().getCode())) {
                throw e;
            }
        }

        ObjectIdentifier aParentCassetteID = cassetteOut;
        String transferState = null;
        String lotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        boolean updateControlJobFlag = false;
        Long lockMode = 0L;
        log.trace("StringUtils.equals(lotOperationEIcheck,\"0\") && !ObjectUtils.isEmpty(aParentCassetteID) : {}", CimStringUtils.equals(lotOperationEIcheck,"0") && !ObjectIdentifier.isEmpty(aParentCassetteID));
        if (CimStringUtils.equals(lotOperationEIcheck,"0") && !ObjectIdentifier.isEmpty(aParentCassetteID)) {
            //-------------------------------
            // Get carrier transfer status
            //-------------------------------
            log.debug("Get carrier transfer status");
            transferState = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
            /*------------------------------------*/
            /*   Get equipment ID in cassette     */
            /*------------------------------------*/
            log.debug("Get equipment ID in cassette");
            Outputs.ObjCassetteEquipmentIDGetOut objCassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, aParentCassetteID);
            //object_lockMode_Get
            log.debug("get lock for model in object");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(objCassetteEquipmentIDGetOut.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.SPLIT_WAFER_LOT_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            //-------------------------------
            // Get required eqp lock mode
            //-------------------------------
            log.debug("Get required eqp lock mode");
            log.trace("StringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) : {}", CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN));
            if (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                updateControlJobFlag = true;
                log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                    // advanced_object_Lock
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(),
                            new ArrayList<>()));
                }
            }else {
                // object_Lock
                log.debug("lot equipment object");
                objectLockMethod.objectLock(objCommon, CimMachine.class, objCassetteEquipmentIDGetOut.getEquipmentID());
            }
        }
        /*--------------------------------*/
        /*   Lock objects to be updated   */
        /*--------------------------------*/
        // object_Lock
        log.debug("Lock objects to be updated");
        objectLockMethod.objectLock(objCommon, CimCassette.class, aParentCassetteID);
        log.trace("StringUtils.equals(lotOperationEIcheck,\"0\") && !ObjectUtils.isEmpty(aParentCassetteID) : {}", CimStringUtils.equals(lotOperationEIcheck,"0") && !ObjectIdentifier.isEmpty(aParentCassetteID));
        if (CimStringUtils.equals(lotOperationEIcheck,"0") && !ObjectIdentifier.isEmpty(aParentCassetteID)) {
            log.trace("!updateControlJobFlag || lockMode.longValue() != Long.valueOf(\"0\") : {}",!updateControlJobFlag || lockMode.longValue() != Long.valueOf("0"));
            if (!updateControlJobFlag || lockMode.longValue() != Long.valueOf("0")) {
                //---------------------------------
                //   Get cassette's ControlJobID
                //---------------------------------
                log.debug("Get cassette's ControlJobID");
                ObjectIdentifier cassetteControlJobResult = cassetteMethod.cassetteControlJobIDGet(objCommon, aParentCassetteID);
                log.trace("null != cassetteControlJobResult : {}",null != cassetteControlJobResult);
                if (null != cassetteControlJobResult) {
                    updateControlJobFlag = true;
                    log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) : {}",!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
                    if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                        /*------------------------------*/
                        /*   Lock controljob Object     */
                        /*------------------------------*/
                        // object_Lock
                        log.debug("Lock for controljob Object");
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, cassetteControlJobResult);
                    }
                }
            }
        }
        // object_Lock
        log.debug("lock for lot object");
        objectLockMethod.objectLock(objCommon, CimLot.class, splitLotReqParams.getParentLotID());
        log.trace("!ObjectUtils.equalsWithValue(objCommon.getUser().getUserID(),BizConstant.SP_SORTERWATCHDOG_PERSON) : {}",
                !ObjectIdentifier.equalsWithValue(objCommon.getUser().getUserID(), BizConstant.SP_SORTERWATCHDOG_PERSON));

        if (!ObjectIdentifier.equalsWithValue(objCommon.getUser().getUserID(), BizConstant.SP_SORTERWATCHDOG_PERSON)) {
            /*-------------------------------*/
            /*   Check SorterJob existence   */
            /*-------------------------------*/
            //waferSorter_sorterJob_CheckForOperation
            log.debug("Check SorterJob existence");
            List<ObjectIdentifier> dummyCastIDs = new ArrayList<>();
            List<ObjectIdentifier> lotIDs = new ArrayList<>();

            lotIDs.add(splitLotReqParams.getParentLotID());
            Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
            objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(new Infos.EquipmentLoadPortAttribute());
            objWaferSorterJobCheckForOperation.setCassetteIDList(dummyCastIDs);
            objWaferSorterJobCheckForOperation.setLotIDList(lotIDs);
            objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);

            log.debug("ckeck wafer sorter job for operation");
            waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
        }
        /*----------------------------------------------------*/
        /*Currrent Route ID Get For routeID confliction-Check */
        /*----------------------------------------------------*/
        log.debug("Currrent Route ID Get For routeID confliction-Check ");
        ObjectIdentifier  lotCurrentRouteIDGet = lotMethod.lotCurrentRouteIDGet(objCommon, splitLotReqParams.getParentLotID());

        ObjectIdentifier currentRouteID = lotCurrentRouteIDGet;
        /* ------------------------------------------------------------*/
        /* Check routeID confliction                                   */
        /*   return RC_INVALID_BRANCH_ROUTEID,                         */
        /*   when the same routeID is used in the following case       */
        /*       ex) Subroute --> The same SubRoute in the course      */
        /* ------------------------------------------------------------*/
        log.debug("lot_originalRouteList_Get IN ");
        Outputs.ObjLotOriginalRouteListGetOut originalRouteListGetOut = lotMethod.lotOriginalRouteListGet(objCommon, splitLotReqParams.getParentLotID());

        //Check CurrentRoute VS SubRoute
        log.debug("Check CurrentRoute VS SubRoute");
        Validations.check (ObjectIdentifier.equalsWithValue(splitLotReqParams.getSubRouteID(), currentRouteID), retCodeConfig.getInvalidBranchRouteId());
        //Check Afetr Route VS SubRoute
        log.debug("Check Afetr Route VS SubRoute");
        log.trace("!ArrayUtils.isEmpty(originalRouteListGetOut.getOriginalRouteID()) : {}",!CimArrayUtils.isEmpty(originalRouteListGetOut.getOriginalRouteID()));
        if (!CimArrayUtils.isEmpty(originalRouteListGetOut.getOriginalRouteID())) {
            for (int i = 0; i < CimArrayUtils.getSize(originalRouteListGetOut.getOriginalRouteID()); i++) {
                Validations.check (originalRouteListGetOut.getOriginalRouteID().get(i).equals(splitLotReqParams.getSubRouteID()), retCodeConfig.getInvalidBranchRouteId());
            }
        }

        //========================================
        // LotStatusList should be Not Shipped
        //========================================
        log.debug("LotStatusList should be Not Shipped");
        String lotState = lotMethod.lotStateGet(objCommon, splitLotReqParams.getParentLotID());
        Validations.check (CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_EMPTIED,lotState), retCodeConfig.getInvalidLotStat());
        /*------------------------------------------------------------------------*/
        /*   Check Condition                                                      */
        /*------------------------------------------------------------------------*/
        log.debug("Check Condition");
        String lotContents = lotMethod.lotContentsGet(splitLotReqParams.getParentLotID());
        Validations.check (CimStringUtils.equals(BizConstant.SP_PRODTYPE_WAFER,lotContents)
                && CimStringUtils.equals(BizConstant.SP_PRODTYPE_DIE,lotContents), retCodeConfig.getInvalidLotContents());

        String lotHoldState = lotMethod.lotHoldStateGet(objCommon, splitLotReqParams.getParentLotID());

        Validations.check (CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotHoldState), retCodeConfig.getConnotSplitHeldlot());

        String finishState = lotMethod.lotFinishedStateGet(objCommon, splitLotReqParams.getParentLotID());
        Validations.check (CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_SCRAPPED,finishState)
                || CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_EMPTIED,finishState)
                || CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_STACKED,finishState), retCodeConfig.getInvalidLotStat());

        //-------------------------
        // Check Bonding Group
        //-------------------------
        //lot_bondingGroupID_GetDR
        log.debug("Check Bonding Group");
        String lotBondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon, splitLotReqParams.getParentLotID());
        Validations.check(!CimObjectUtils.isEmpty(lotBondingGroupID),retCodeConfig.getLotHasBondingGroup(),splitLotReqParams.getParentLotID().getValue(),lotBondingGroupID);

        String processState = lotMethod.lotProcessStateGet(objCommon, splitLotReqParams.getParentLotID());
        log.trace("StringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING,processState) : {}", CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING,processState));
        if (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING,processState)) {
            Validations.check (!"OEQPW012".equals(objCommon.getTransactionID())
                    && !"OEQPW024".equals(objCommon.getTransactionID()), new OmCode(retCodeConfig.getInvalidLotProcessState(), ObjectIdentifier.fetchValue(splitLotReqParams.getParentLotID()), processState));
        }
        log.debug("Check InPostProcessFlag.");
        //----------------------------------
        //  Get InPostProcessFlag of lot
        //----------------------------------
        log.debug("Get InPostProcessFlag of lot");
        Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, splitLotReqParams.getParentLotID());
        //----------------------------------------------
        //  If lot is in post process, returns error
        //----------------------------------------------
        log.trace("BooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot()) : {}", CimBooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot()));
        if (CimBooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot())) {
            /*---------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            log.debug("Get UserGroupID By UserID");
            List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
            int i ;
            for (i = 0; i < CimArrayUtils.getSize(userGroupIDs); i++) {
            }
            Validations.check (CimArrayUtils.getSize(userGroupIDs) == i, retCodeConfig.getLotInPostProcess());
        }

        /*------------------------------------------------------------------------*/
        /*   Check SubRouteID and return operation                                */
        /*------------------------------------------------------------------------*/
        log.debug("Check SubRouteID and return operation");
        log.trace("!ObjectUtils.isEmpty(splitLotReqParams.getSubRouteID()) : {}",!ObjectIdentifier.isEmpty(splitLotReqParams.getSubRouteID()));
        if (!ObjectIdentifier.isEmpty(splitLotReqParams.getSubRouteID())) {
            //----------------------------------------//
            //    Call lot_inventoryState_Get         //
            //----------------------------------------//
            log.debug("get lot inventory state by lot parent lotID");
            String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, splitLotReqParams.getParentLotID());
            Validations.check (CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK,lotInventoryState), retCodeConfig.getInvalidLotInventoryStat());
            //【step5】process_checkForDynamicRoute
            log.debug("【step5】process_checkForDynamicRoute");
            Outputs.ObjProcessCheckForDynamicRouteOut processCheckForDynamicRouteOut = processMethod.processCheckForDynamicRoute(objCommon, splitLotReqParams.getSubRouteID());
            log.trace("BooleanUtils.isFalse(processCheckForDynamicRouteOut.getDynamicRouteFlag()) : {}", CimBooleanUtils.isFalse(processCheckForDynamicRouteOut.getDynamicRouteFlag()));
            if (CimBooleanUtils.isFalse(processCheckForDynamicRouteOut.getDynamicRouteFlag())) {
                Outputs.ObjProcessGetReturnOperationOut processReturnOperation = processMethod.processGetReturnOperation(objCommon, splitLotReqParams.getParentLotID(), splitLotReqParams.getSubRouteID());
                Validations.check (CimStringUtils.isNotEmpty(splitLotReqParams.getReturnOperationNumber())
                        && !CimStringUtils.equals(processReturnOperation.getOperationNumber(), splitLotReqParams.getReturnOperationNumber()), retCodeConfig.getInvalidParameter());
            }
        }

        log.debug("process check split ");
        processMethod.processCheckSplit(objCommon, splitLotReqParams.getMergedRouteID(), splitLotReqParams.getMergedOperationNumber(), splitLotReqParams.getReturnOperationNumber());

        log.debug("lot future split  hold  check request ");
        lotMethod.lotFutureHoldRequestsCheckSplit(objCommon, splitLotReqParams.getParentLotID(), splitLotReqParams.getMergedOperationNumber(), splitLotReqParams.getReturnOperationNumber());
        /*-------------------------------*/
        /*   Check flowbatch Condition   */
        /*-------------------------------*/
        //lot_flowBatchID_Get
        log.debug("Check FlowBatch Condition  [ParentLot]");
        try {
            ObjectIdentifier retCodeObjectIdentifier = flowBatchMethod.lotFlowBatchIDGet(objCommon, splitLotReqParams.getParentLotID());
        } catch (ServiceException e) {
            log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode()));
            log.trace("Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode()));
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())){
                throw new ServiceException(retCodeConfig.getFlowBatchLimitation());
            }else if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())){
                log.debug("flowBatchMethod.getFlowBatchID() == RC_LOT_BATCH_ID_BLANK");
            }
        }

        log.trace("!\"OEQPW012\".equals(objCommon.getTransactionID())\n" +
                        "                && !\"OEQPW024\".equals(objCommon.getTransactionID()) : {}",
                !"OEQPW012".equals(objCommon.getTransactionID())
                        && !"OEQPW024".equals(objCommon.getTransactionID()));

        if (!"OEQPW012".equals(objCommon.getTransactionID())
                && !"OEQPW024".equals(objCommon.getTransactionID())) {
            /*------------------------------------------------------------------------*/
            /*   Check if the wafers in lot don't have machine container position     */
            /*------------------------------------------------------------------------*/
            log.debug("Check if the wafers in lot don't have machine container position");
            log.debug("call equipmentContainerPosition_info_GetByLotDR()");
            List<Infos.EqpContainerPosition> ObjEquipmentContainerPositionInfoGetByLotDROut = equipmentMethod.equipmentContainerPositionInfoGetByLotDR(objCommon, splitLotReqParams.getParentLotID());
            int lenEqpContPos = CimArrayUtils.getSize(ObjEquipmentContainerPositionInfoGetByLotDROut);
            Validations.check (0 < lenEqpContPos, retCodeConfig.getWaferInLotHaveContainerPosition());
        }
        /*----------------------------------*/
        /*   Check lot's Control Job ID     */
        /*----------------------------------*/
        log.debug("Check lot's Control Job ID");
        ObjectIdentifier jobIDGetOut = lotMethod.lotControlJobIDGet(objCommon, splitLotReqParams.getParentLotID());
        log.trace("ObjectUtils.isEmpty(jobIDGetOut) : {}", ObjectIdentifier.isEmpty(jobIDGetOut));
        if (ObjectIdentifier.isEmpty(jobIDGetOut)) {
        } else {
            log.trace("!\"OEQPW012\".equals(objCommon.getTransactionID())\n" +
                            "                    && !\"OEQPW024\".equals(objCommon.getTransactionID()) : {}",
                    !"OEQPW012".equals(objCommon.getTransactionID())
                            && !"OEQPW024".equals(objCommon.getTransactionID()));

            if (!"OEQPW012".equals(objCommon.getTransactionID())
                    && !"OEQPW024".equals(objCommon.getTransactionID())) {
                throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(), ObjectIdentifier.fetchValue(splitLotReqParams.getParentLotID()), ObjectIdentifier.fetchValue(jobIDGetOut)));
            }
        }
        //-------------------------------
        // Check carrier transfer status
        //-------------------------------
        log.debug(" Check carrier transfer status");
        String cassetteTransferStateGetOut = null;
        log.trace("!ObjectUtils.isEmpty(aParentCassetteID) : {}",!ObjectIdentifier.isEmpty(aParentCassetteID));
        if (!ObjectIdentifier.isEmpty(aParentCassetteID)) {
            log.trace("StringUtils.equals(lotOperationEIcheck,\"1\")\n" +
                            "                    || (StringUtils.equals(lotOperationEIcheck,\"0\") && !StringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,transferState)): {}",
                    CimStringUtils.equals(lotOperationEIcheck,"1")
                            || (CimStringUtils.equals(lotOperationEIcheck,"0") && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,transferState)));

            if (CimStringUtils.equals(lotOperationEIcheck,"1")
                    || (CimStringUtils.equals(lotOperationEIcheck,"0") && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,transferState))) {
                cassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, aParentCassetteID);
                log.trace("StringUtils.equals(lotOperationEIcheck,\"0\") : {}", CimStringUtils.equals(lotOperationEIcheck,"0"));
                if (CimStringUtils.equals(lotOperationEIcheck,"0")) {
                    Validations.check (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateGetOut), retCodeConfig.getChangedToEiByOtherOperation());
                } else {
                    Validations.check ((CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT,cassetteTransferStateGetOut))
                                    || (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,cassetteTransferStateGetOut)
                                    && !"OEQPW006".equals(objCommon.getTransactionID())
                                    && !"OEQPW012".equals(objCommon.getTransactionID())
                                    && !"OEQPW024".equals(objCommon.getTransactionID())
                                    && !"OEQPW008".equals(objCommon.getTransactionID())), retCodeConfig.getInvalidCassetteTransferState(),
                            cassetteTransferStateGetOut,aParentCassetteID);
                }
            }
            log.trace("StringUtils.equals(lotOperationEIcheck,\"0\") : {}", CimStringUtils.equals(lotOperationEIcheck,"0"));
            if (CimStringUtils.equals(lotOperationEIcheck,"0")) {
                cassetteTransferStateGetOut = transferState;
            }
        }

        log.trace("StringUtils.equals(lotOperationEIcheck,\"1\" : {}", CimStringUtils.equals(lotOperationEIcheck,"1"));
        if (CimStringUtils.equals(lotOperationEIcheck,"1")) {
            //-------------------------------
            // Check carrier dispatch status
            //-------------------------------
            log.debug("Check carrier dispatch status");
            log.trace("!ObjectUtils.isEmpty(aParentCassetteID) : {}",!ObjectIdentifier.isEmpty(aParentCassetteID));
            if (!ObjectIdentifier.isEmpty(aParentCassetteID)) {
                Boolean cassetteDispatchStateRetCode = cassetteMethod.cassetteDispatchStateGet(objCommon, aParentCassetteID);
                Validations.check (cassetteDispatchStateRetCode, retCodeConfig.getAlreadyDispatchReservedCassette());
            }
        }
        //-------------------------------
        // Check carrier reserved flag
        //-------------------------------
        log.debug("Check carrier reserved flag");
        log.trace("!ObjectUtils.isEmpty(aParentCassetteID) : {}",!ObjectIdentifier.isEmpty(aParentCassetteID));
        if (!ObjectIdentifier.isEmpty(aParentCassetteID)) {
            Outputs.ObjCassetteReservedStateGetOut cassetteReservedStateRetCode = cassetteMethod.cassetteReservedStateGet(objCommon, aParentCassetteID);
            Validations.check (cassetteReservedStateRetCode.isTransferReserved(), retCodeConfig.getAlreadyDispatchReservedCassette());
        }

        /*------------------------------------------------------------------------*/
        /*  Check Lot Split Or Merge                                  */
        /*------------------------------------------------------------------------*/
        log.debug("Check Lot Split Or Merge");
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(splitLotReqParams.getParentLotID());
        this.checkLotSplitOrMerge(objCommon,lotIDLists, BizConstant.CHECK_LOT_ACTION_SPLIT);
    }

}
