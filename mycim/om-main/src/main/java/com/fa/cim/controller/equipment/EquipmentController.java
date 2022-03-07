package com.fa.cim.controller.equipment;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.*;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.equipment.IEquipmentController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.*;
import com.fa.cim.eqp.carrierout.CarrierOutPortReqParams;
import com.fa.cim.eqp.carrierout.CarrierOutPortResults;
import com.fa.cim.eqp.carrierout.CarrierOutReqParams;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.frameworks.pprocess.proxy.impl.*;
import com.fa.cim.method.*;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.constraint.IConstraintService;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.equipment.IEquipmentProcessOperation;
import com.fa.cim.service.equipment.IEquipmentService;
import com.fa.cim.service.newSorter.ISortNewInqService;
import com.fa.cim.service.post.IPostService;
import com.fa.cim.service.pr.IPilotRunService;
import com.fa.cim.sorter.SorterHandler;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/7/29 10:49
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IEquipmentController.class, confirmableKey = "EquipmentConfirm", cancellableKey = "EquipmentCancel")
@RequestMapping("/eqp")
@Listenable
public class EquipmentController implements IEquipmentController {

    @Autowired
    private IEquipmentService equipmentService;
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IControlJobMethod controlJobMethod;
    @Autowired
    private ICassetteMethod cassetteMethod;
    @Autowired
    private ILotMethod lotMethod;
    @Autowired
    private IPostProcessMethod postProcessMethod;
    @Autowired
    private IMessageMethod messageMethod;
    @Autowired
    private PostController postController;
    @Autowired
    private IPostService postService;
    @Autowired
    private PostController postTaskExecuteReqController;
    @Autowired
    private IConstraintService constraintService;
    @Autowired
    private IDurableService durableService;
    @Autowired
    private IPilotRunService pilotRunService;
    @Autowired
    private BaseCoreFactory baseCoreFactory;
    @Autowired
    private ISortNewInqService sortNewInqService;


    @ResponseBody
    @RequestMapping(value = "/chamber_status_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CHAMBER_STATUS_CHANGE_REQ)
    public Response chamberStatusChangeReq(@RequestBody Params.ChamberStatusChangeReqPrams params) {
        String transactionID = TransactionIDEnum.CHAMBER_STATUS_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams =
                new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(),
                accessControlCheckInqParams);

        //step3 - chamberStatusChangeReqService
        Results.ChamberStatusChangeReqResult result = equipmentService.sxChamberStatusChangeReq(objCommon,
                params.getEquipmentID(), params.getEqpChamberStatuses(), params.getReasonCodeID(),
                params.getClaimMemo());

        // step4 - messageQueue_Put
        Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
        msgQueuePut.setEquipmentID(params.getEquipmentID());
        msgQueuePut.setCassetteDispatchReserveFlag(false);
        msgQueuePut.setCassetteTransferReserveFlag(false);
        messageMethod.messageQueuePut(objCommon, msgQueuePut);
        return Response.createSucc(transactionID, result);

    }

    @ResponseBody
    @RequestMapping(value = "/chamber_status_change/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CHAMBER_STATUS_CHANGE_RPT)
    public Response chamberStatusChangeRpt(@RequestBody Params.ChamberStatusChangeRptPrams params) {
        String txId = TransactionIDEnum.CHAMBER_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);


        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step3 - chamberStatusChangeRptService
        ObjectIdentifier result = equipmentService.sxChamberStatusChangeRpt(objCommon, params.getEquipmentID(), params.getEqpChamberStatuses());

        // step4 - messageQueue_Put
        Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
        msgQueuePut.setEquipmentID(params.getEquipmentID());
        msgQueuePut.setCassetteDispatchReserveFlag(false);
        msgQueuePut.setCassetteTransferReserveFlag(false);
        messageMethod.messageQueuePut(objCommon, msgQueuePut);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_mode_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_MODE_CHANGE_REQ)
    public Response EqpModeChangeReq(@RequestBody Params.EqpModeChangeReqPrams params) {
        String txId = TransactionIDEnum.EQP_MODE_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);


        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step3 - chamberStatusChangeReqService
        equipmentService.sxEqpModeChangeReq(objCommon, params);

        //step4 - messageQueue_Put
        Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
        msgQueuePut.setEquipmentID(params.getEquipmentID());
        msgQueuePut.setStrPortOperationMode(params.getPortOperationModeList());
        msgQueuePut.setCassetteDispatchReserveFlag(false);
        msgQueuePut.setCassetteTransferReserveFlag(false);
        messageMethod.messageQueuePut(objCommon, msgQueuePut);

        return Response.createSucc(TransactionIDEnum.EQP_MODE_CHANGE_REQ.getValue(), null);
    }


    @ResponseBody
    @RequestMapping(value = "/eqp_memo_add/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_NOTE_REGIST_REQ)
    public Response eqpMemoAddReq(@RequestBody Params.EqpMemoAddReqParams eqpMemoAddReqParams) {
        String txId = TransactionIDEnum.EQP_NOTE_REGIST_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);


        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(eqpMemoAddReqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, eqpMemoAddReqParams.getUser(), accessControlCheckInqParams);

        //step3 - txEqpMemoAddReq__160
        return Response.createSucc(txId, equipmentService.sxEqpMemoAddReq(objCommon, eqpMemoAddReqParams));
    }

    @ResponseBody
    @RequestMapping(value = "/port_status_change/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_PORT_STATUS_CHANGE_RPT)
    public Response portStatusChangeRpt(@RequestBody Params.PortStatusChangeRptParam params) {

        String txId = TransactionIDEnum.EQP_PORT_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);


        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step3 - portStatusChangeRptService
        ObjectIdentifier result = equipmentService.sxPortStatusChangeRpt(objCommon, params.getEquipmentID(), params.getEqpPortEventOnEAPesList(), params.getOpeMemo());

        //step4 - messageQueue_Put
        Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
        msgQueuePut.setEquipmentID(result);
        msgQueuePut.setCassetteDispatchReserveFlag(false);
        msgQueuePut.setCassetteTransferReserveFlag(false);
        messageMethod.messageQueuePut(objCommon, msgQueuePut);

        return Response.createSucc(TransactionIDEnum.EQP_PORT_STATUS_CHANGE_RPT.getValue(), result);
    }


    @ResponseBody
    @RequestMapping(value = "/eqp_eap_status_sync/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_STATUS_ADJUST_REQ)
    public Response EqpEAPStatusSyncReq(@RequestBody Params.EqpEAPStatusSyncReqPrams params) {

        String txId = TransactionIDEnum.EQP_STATUS_ADJUST_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step2 - call AccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step3 - chamberStatusChangeReqService
        equipmentService.sxEqpEAPStatusSyncReq(objCommon, params);

        // step4 - messageQueue_Put
        Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
        msgQueuePut.setEquipmentID(params.getEquipmentID());
        msgQueuePut.setCassetteDispatchReserveFlag(false);
        msgQueuePut.setCassetteTransferReserveFlag(false);
        messageMethod.messageQueuePut(objCommon, msgQueuePut);

        return Response.createSucc(TransactionIDEnum.EQP_STATUS_ADJUST_REQ.getValue(), null);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_status_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_STATUS_CHANGE_REQ)
    public Response eqpStatusChangeReq(@RequestBody Params.EqpStatusChangeReqParams eqpStatusChangeReqParams) {
        String txId = TransactionIDEnum.EQP_STATUS_CHANGE_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams =
                new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(eqpStatusChangeReqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId,
                eqpStatusChangeReqParams.getUser(), accessControlCheckInqParams);

        //get the current state of eqp
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class,eqpStatusChangeReqParams.getEquipmentID());
        String e10AndCurState = null;
        if (aMachine != null) {
            String curE10State = aMachine.getCurE10State();
            String curState = ObjectIdentifier.fetchValue(aMachine.getCurrentMachineStateID());
            e10AndCurState = new StringJoiner(".").add(curE10State).add(curState).toString();
        }

        //Step3 - txEqpStatusChangeReq
        Results.EqpStatusChangeReqResult result = equipmentService.sxEqpStatusChangeReq(objCommon,
                eqpStatusChangeReqParams.getEquipmentID(), eqpStatusChangeReqParams.getEquipmentStatusCode(),
                eqpStatusChangeReqParams.getReasonCodeID(), eqpStatusChangeReqParams.getClaimMemo());

        // Step4 - messageQueue_Put, call message to EI
        Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
        msgQueuePut.setEquipmentID(result.getEquipmentID());
        msgQueuePut.setEquipmentStatusCode(result.getEquipmentStatusCode());
        msgQueuePut.setCassetteDispatchReserveFlag(false);
        msgQueuePut.setCassetteTransferReserveFlag(false);
        messageMethod.messageQueuePut(objCommon, msgQueuePut);

        //Step5 - create recipe job
        pilotRunService.sxCreateRecipeJobByEqpStatusCut(objCommon,eqpStatusChangeReqParams,e10AndCurState);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_status_change/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_STATUS_CHANGE_RPT)
    public Response eqpStatusChangeRpt(@RequestBody Params.EqpStatusChangeRptParams eqpStatusChangeRptParams) {
        String txId = TransactionIDEnum.EQP_STATUS_CHANGE_RPT.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(eqpStatusChangeRptParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, eqpStatusChangeRptParams.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.EqpStatusChangeRptResult tmpResult = equipmentService.sxEqpStatusChangeRpt(objCommon, eqpStatusChangeRptParams);

        // Step4 - messageQueue_Put
        Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
        msgQueuePut.setEquipmentID(eqpStatusChangeRptParams.getEquipmentID());
        msgQueuePut.setEquipmentStatusCode(eqpStatusChangeRptParams.getEquipmentStatusCode());
        msgQueuePut.setCassetteDispatchReserveFlag(false);
        msgQueuePut.setCassetteTransferReserveFlag(false);
        messageMethod.messageQueuePut(objCommon, msgQueuePut);

        //【step5】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(txId, tmpResult);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_status_reset/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_STATUS_RECOVER_REQ)
    public Response eqpStatusResetReq(@RequestBody Params.EqpStatusResetReqParams params) {
        String txId = TransactionIDEnum.EQP_STATUS_RECOVER_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //Step3 - txEqpStatusResetReq
        Results.EqpStatusResetReqResult result = equipmentService.sxEqpStatusResetReq(objCommon, params.getEquipmentID(), params.getChangeType(), params.getClaimMemo());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_usage_count_reset/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_USAGE_COUNT_RESET_REQ)
    public Response eqpUsageCountResetReq(@RequestBody Params.EqpUsageCountResetReqParam params) {
        String transactionID = TransactionIDEnum.EQP_USAGE_COUNT_RESET_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        Validations.check(user == null, "user can not be null");

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //Step3 - txEqpUsageCountResetReq
        ObjectIdentifier result = equipmentService.sxEqpUsageCountResetReq(objCommon, params.getEquipmentID(), params.getClaimMemo());
        Response response = Response.createSucc(transactionID, result);
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_loading/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOADING_LOT_RPT)
    public Response carrierLoadingRpt(@RequestBody Params.loadOrUnloadLotRptParams param) {
        //===========Pre process============
        String txId = TransactionIDEnum.LOADING_LOT_RPT.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = param.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txId);
        }
        //Step1 calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommonIn = utilsComp.setObjCommon(txId, user);

        //Step2 - cassette_lotIDList_GetDR，cassetteLotIDList will used in txAccessControlCheckInq
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1"))) {
            // step2 - cassette_lotIDList_GetDR
            Infos.LotListInCassetteInfo cassetteLotIDList = cassetteMethod.cassetteLotIDListGetDR(objCommonIn, param.getCassetteID());
            if (CimObjectUtils.isEmpty(cassetteLotIDList)) {
                return Response.createError(objCommonIn.getTransactionID(), "not found");
            }
            lotIDs = cassetteLotIDList.getLotIDList();
        }

        //Step3 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        // note: need to split the method of checkPrivilegeAndGetObjCommon
        accessInqService.sxAccessControlCheckInq(objCommonIn, accessControlCheckInqParams);
        //===========Main process============
        //Step4 - txCarrierLoadingRpt
        List<Infos.LoadingVerifiedLot> loadingVerifiedLots = null;
        try {
            loadingVerifiedLots = equipmentService.sxCarrierLoadingRpt(objCommonIn, param.getEquipmentID(), param.getCassetteID(), param.getPortID(), param.getLoadPurposeType());
        } catch (ServiceException e) {
            loadingVerifiedLots = e.getData(List.class);
            if (Validations.isEquals(retCodeConfig.getCastForceLoaded(), e.getCode())) {
                return Response.createSuccWithOmCode(txId, retCodeConfig.getCastForceLoaded(), loadingVerifiedLots);
            } else {
                throw e;
            }
        }
        //===========Post process============
        return Response.createSucc(txId, loadingVerifiedLots);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_loading_verify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_VERIFY_FOR_LOADING_REQ)
    public Response carrierLoadingVerifyReq(@RequestBody Params.CarrierLoadingVerifyReqParams carrierLoadingVerifyReqParams) {

        String txId = TransactionIDEnum.LOT_VERIFY_FOR_LOADING_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        //step 1 - Pre Process
        String transactionID = TransactionIDEnum.LOT_VERIFY_FOR_LOADING_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = carrierLoadingVerifyReqParams.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        //Step2 - call cassette_lotIDList_GetDR for LotIDs which will be used in Step3
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue) && (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1"))) {
            // step2 - cassette_lotIDList_GetDR
            Infos.LotListInCassetteInfo cassetteLotIDList = cassetteMethod.cassetteLotIDListGetDR(objCommon, carrierLoadingVerifyReqParams.getCassetteId());
            lotIDs = cassetteLotIDList.getLotIDList();
        }

        //Step3 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(carrierLoadingVerifyReqParams.getEquipmentId());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //Step4 - Main Process
        Results.CarrierLoadingVerifyReqResult result = equipmentService.sxCarrierLoadingVerifyReq(objCommon, carrierLoadingVerifyReqParams);
        //Step5 - Post Process
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/old/move_out/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ)
    public Response moveOutReqOld(@RequestBody Params.OpeComWithDataReqParams opeComWithDataReqParams) {
        List<Infos.EventParameter> strEventParameter;

        Infos.ObjCommon strObjCommonIn;

        // Initialising strObjCommonIn's first two parameters

        String txID = TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue();
        User user = opeComWithDataReqParams.getUser();

        ThreadContextHolder.setTransactionId(txID);
        // clearThreadSpecificDataString ();
        ThreadContextHolder.clearThreadSpecificDataString();

        strObjCommonIn = utilsComp.setObjCommon(txID, user);

        ObjectIdentifier dummy = null;
        List<ObjectIdentifier> dummyIDs = null;
        dummyIDs = new ArrayList<>();

        List<ObjectIdentifier> lotIDs;
        lotIDs = new ArrayList<>(0);
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();

        if ((0 != CimStringUtils.length(tmpPrivilegeCheckCJValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1"))) {
            List<ObjectIdentifier> strControlJobLotIDListGetDROut;
            strControlJobLotIDListGetDROut = controlJobMethod.controlJobLotIDListGetDR(strObjCommonIn, opeComWithDataReqParams.getControlJobID());

            lotIDs = strControlJobLotIDListGetDROut;
        } else {
        }

        ObjectIdentifier equipmentID = opeComWithDataReqParams.getEquipmentID();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        accessInqService.sxAccessControlCheckInq(strObjCommonIn, accessControlCheckInqParams);

        //-----------------------------------------------------------------------
        //   Main Process
        //-----------------------------------------------------------------------
        List<ObjectIdentifier> holdReleasedLotIDs;
        String APCIFControlStatus;
        String DCSIFControlStatus;

        List<Infos.ApcBaseCassette> strAPCBaseCassetteListForOpeComp;
        Results.MoveOutReqResult retVal = equipmentService.sxMoveOutReq(
                strObjCommonIn,
                opeComWithDataReqParams);

        holdReleasedLotIDs = opeComWithDataReqParams.getHoldReleasedLotIDs();
        APCIFControlStatus = opeComWithDataReqParams.getApcifControlStatus();
        DCSIFControlStatus = opeComWithDataReqParams.getDcsifControlStatus();
        strAPCBaseCassetteListForOpeComp = opeComWithDataReqParams.getApcBaseCassetteListForOpeComp();

        Results.PostTaskRegisterReqResult strPostTaskRegisterReqResult;
        Results.PostTaskRegisterReqResult strPostTaskRegisterReqResult2 = new Results.PostTaskRegisterReqResult();
        int PostProcForLotFlag = StandardProperties.OM_PP_FOR_LOT_MOVEOUT_FLAG.getIntValue();
        String opeCompedTxID;
        if (-1 == PostProcForLotFlag) {
            String strPostProcForLotFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCFORLOTFLAG);
            if (CimStringUtils.equals(strPostProcForLotFlag, "1")) {
                PostProcForLotFlag = 1;
            } else {
                PostProcForLotFlag = 0;
            }
        }

        if (PostProcForLotFlag != 1) {
            opeCompedTxID = "OEQPW006";
        } else {
            opeCompedTxID = "TXTRC_04";
        }

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        ObjectIdentifier controlJobID = opeComWithDataReqParams.getControlJobID();

        int i = 0;
        int opeCompedLotlen = CimArrayUtils.getSize(retVal.getMoveOutLot());
        List<ObjectIdentifier> opeCompedLotIDs;

        opeCompedLotIDs = new ArrayList<>(opeCompedLotlen);

        //Collection of OpeComped LotID
        for (i = 0; i < opeCompedLotlen; i++) {
            opeCompedLotIDs.add(retVal.getMoveOutLot().get(i).getLotID());
        }


        Infos.PostProcessRegistrationParam strPostProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
        strPostProcessRegistrationParm.setLotIDs(opeCompedLotIDs);
        strPostProcessRegistrationParm.setEquipmentID(equipmentID);
        strPostProcessRegistrationParm.setControlJobID(controlJobID);

        String claimMemo = opeComWithDataReqParams.getOpeMemo();
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
        postTaskRegisterReqParams.setTransactionID(opeCompedTxID);
        postTaskRegisterReqParams.setPatternID(null);
        postTaskRegisterReqParams.setKey(null);
        postTaskRegisterReqParams.setSequenceNumber(-1);
        postTaskRegisterReqParams.setPostProcessRegistrationParm(strPostProcessRegistrationParm);
        postTaskRegisterReqParams.setClaimMemo(claimMemo);

        strPostTaskRegisterReqResult = postService.sxPostTaskRegisterReq(strObjCommonIn, postTaskRegisterReqParams);

        //--------------------------------------------------
        // Post-Processing for OpeCompedLot Update Section
        //--------------------------------------------------
        int nActionLen = CimArrayUtils.getSize(strPostTaskRegisterReqResult.getPostProcessActionInfoList());
        Infos.PostProcessAdditionalInfo[] strPostProcessAdditionalInfoSeq;
        strPostProcessAdditionalInfoSeq = new Infos.PostProcessAdditionalInfo[nActionLen * 3];
        int nAdditionalCnt = 0;

        for (int nActionIdx = 0; nActionIdx < nActionLen; nActionIdx++) {
            if (CimStringUtils.equals(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_QTIME)) {
                if (1 == PostProcForLotFlag) {

                    Outputs.ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout strLotPostProcessPreviousBranchAndReturnInfoGetDROut;
                    strLotPostProcessPreviousBranchAndReturnInfoGetDROut = lotMethod.lotPostProcessPreviousBranchAndReturnInfoGetDR(
                            strObjCommonIn,
                            strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getPostProcessTargetObject().getLotID());


                    if (ObjectIdentifier.isNotEmpty(strLotPostProcessPreviousBranchAndReturnInfoGetDROut.getBranchRouteID())) {

                        String[] strings;
                        strings = new String[2];
                        strings[0] = strLotPostProcessPreviousBranchAndReturnInfoGetDROut.getBranchRouteID().getValue();
                        strings[1] = strLotPostProcessPreviousBranchAndReturnInfoGetDROut.getBranchOperationNumber();
                        String previousBranchInfo = CimArrayUtils.mergeStringIntoTokens(CimArrayUtils.generateList(strings), BizConstant.SP_KEY_SEPARATOR_DOT);

                        strPostProcessAdditionalInfoSeq[nAdditionalCnt] = new Infos.PostProcessAdditionalInfo();
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setDKey(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getDKey());
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setSequenceNumber(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getSequenceNumber());
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setValue(previousBranchInfo);
                        nAdditionalCnt++;
                    }

                    if (ObjectIdentifier.isNotEmpty(strLotPostProcessPreviousBranchAndReturnInfoGetDROut.getReturnRouteID())) {

                        String[] strings;
                        strings = new String[2];
                        strings[0] = strLotPostProcessPreviousBranchAndReturnInfoGetDROut.getReturnRouteID().getValue();
                        strings[1] = strLotPostProcessPreviousBranchAndReturnInfoGetDROut.getReturnOperationNumber();
                        String previousReturnInfo = CimArrayUtils.mergeStringIntoTokens(CimArrayUtils.generateList(strings), BizConstant.SP_KEY_SEPARATOR_DOT);

                        strPostProcessAdditionalInfoSeq[nAdditionalCnt] = new Infos.PostProcessAdditionalInfo();
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setDKey(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getDKey());
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setSequenceNumber(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getSequenceNumber());
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setValue(previousReturnInfo);
                        nAdditionalCnt++;
                    }

                    if (0 < CimStringUtils.length(strLotPostProcessPreviousBranchAndReturnInfoGetDROut.getReworkOutKey())) {
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt] = new Infos.PostProcessAdditionalInfo();
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setDKey(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getDKey());
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setSequenceNumber(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getSequenceNumber());
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);
                        strPostProcessAdditionalInfoSeq[nAdditionalCnt].setValue(strLotPostProcessPreviousBranchAndReturnInfoGetDROut.getReworkOutKey());
                        nAdditionalCnt++;
                    }
                }
            }
        }

        if (0 < nAdditionalCnt) {
            postService.sxPostActionModifyReq(strObjCommonIn, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                    strPostTaskRegisterReqResult.getPostProcessActionInfoList(), Arrays.asList(strPostProcessAdditionalInfoSeq), opeComWithDataReqParams.getOpeMemo());
        }

        int holdReleasedLotLen;
        holdReleasedLotLen = CimArrayUtils.getSize(holdReleasedLotIDs);

        if (holdReleasedLotLen != 0) {

            //HoldReleased LotID Trace
            for (i = 0; i < holdReleasedLotLen; i++) {
                log.info("HoldRelesedLotIDs : {}", holdReleasedLotIDs.get(i).getValue());
            }

            strPostProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
            strPostProcessRegistrationParm.setLotIDs(holdReleasedLotIDs);
            strPostProcessRegistrationParm.setEquipmentID(equipmentID);
            strPostProcessRegistrationParm.setControlJobID(controlJobID);

            Params.PostTaskRegisterReqParams postTaskRegisterReqParams1 = new Params.PostTaskRegisterReqParams();
            postTaskRegisterReqParams1.setTransactionID("ThTRC004");
            postTaskRegisterReqParams1.setPatternID(null);
            postTaskRegisterReqParams1.setKey(null);
            postTaskRegisterReqParams1.setSequenceNumber(-1);
            postTaskRegisterReqParams1.setPostProcessRegistrationParm(strPostProcessRegistrationParm);
            postTaskRegisterReqParams1.setClaimMemo(claimMemo);
            strPostTaskRegisterReqResult2 = postService.sxPostTaskRegisterReq(
                    strObjCommonIn,
                    postTaskRegisterReqParams1);

        }

        // TODO: NOTIMPL
        // if (rc == RC_OK) {
        // } else {
        //     if( CIMFWStrLen(APCIFControlStatus) != 0 && CIMFWStrCmp(getenv(SP_Rollback_Notify_To_APC), "1") == 0 )
        //     {
        //         log.info("", "Resend ControlJobInfomation to APC. notify status ->", APCIFControlStatus);
        //         int iRc = RC_OK;
        //         pptStartCassetteSequence strStartCassette;
        //         pptAPCCJRptResult strAPCCJRptResult;
        //         TX_BEGIN(txAPCCJRpt);
        //         try
        //         {
        //             iRc = thePPTManager->txAPCCJRpt(strAPCCJRptResult,
        //                     strObjCommonIn,
        //                     equipmentID,
        //                     controlJobID,
        //                     strStartCassette,
        //                     APCIFControlStatus);
        //         }
        //         CATCH_TX_TIMEOUT_EXCEPTIONS(txAPCCJRpt);
        //         if( iRc == RC_OK || iRc == RC_OK_NO_IF )
        //         {
        //             log.info("", "txAPCCJRpt() == RC_OK or RC_OK_NO_IF", iRc);
        //             TX_COMMIT(txAPCCJRpt);
        //         }
        //         else
        //         {
        //             log.info("", "txAPCCJRpt() != RC_OK", iRc);
        //             TX_ROLLBACK(txAPCCJRpt);
        //         }
        //     }
        // }

        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------

        Results.PostTaskExecuteReqResult result = null;
        Results.PostTaskExecuteReqResult result2;
        OmCode tmpResult = null;
        OmCode tmpResult2 = null;

        int PostProcParallelFlag = StandardProperties.OM_PP_PARALLEL_ENABLE.getIntValue();
        if (0 == PostProcParallelFlag) {
            Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
            postTaskExecuteReqParams.setUser(user);
            postTaskExecuteReqParams.setKey(strPostTaskRegisterReqResult.getDKey());
            postTaskExecuteReqParams.setSyncFlag(1);
            postTaskExecuteReqParams.setPreviousSequenceNumber(0);
            postTaskExecuteReqParams.setKeyTimeStamp(null);
            postTaskExecuteReqParams.setClaimMemo("");
            Response postTaskExecuteReqResult;
            try {
                postTaskExecuteReqResult = postController.postTaskExecuteReq(postTaskExecuteReqParams);
                result = (Results.PostTaskExecuteReqResult) postTaskExecuteReqResult.getBody();
                tmpResult = new OmCode(postTaskExecuteReqResult.getCode(), postTaskExecuteReqResult.getMessage());
            } catch (ServiceException ex) {
                tmpResult = new OmCode(ex.getCode(), ex.getMessage());
            }

            //Post Process Execution for Released Lot
            if (PostProcForLotFlag != 1) {
                postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams.setUser(user);
                postTaskExecuteReqParams.setKey(strPostTaskRegisterReqResult2.getDKey());
                postTaskExecuteReqParams.setSyncFlag(1);
                postTaskExecuteReqParams.setPreviousSequenceNumber(0);
                postTaskExecuteReqParams.setKeyTimeStamp(null);
                postTaskExecuteReqParams.setClaimMemo("");
                try {
                    postTaskExecuteReqResult = postController.postTaskExecuteReq(postTaskExecuteReqParams);
                    result2 = (Results.PostTaskExecuteReqResult) postTaskExecuteReqResult.getBody();
                    tmpResult2 = new OmCode(postTaskExecuteReqResult.getCode(), postTaskExecuteReqResult.getMessage());
                } catch (ServiceException ex) {
                    tmpResult2 = new OmCode(ex.getCode(), ex.getMessage());
                }
            } else {
                // Set strLotSpecSPCCheckResultSeq (for OpeCompedLot)
                if (CimArrayUtils.isNotEmpty(result.getStrLotSpecSPCCheckResultSeq())){
                    retVal.setMoveOutLot(result.getStrLotSpecSPCCheckResultSeq());// strLotSpecSPCCheckResultSeq;
                }


                postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams.setUser(user);
                postTaskExecuteReqParams.setKey(result.getRelatedQueueKey());
                postTaskExecuteReqParams.setSyncFlag(1);
                postTaskExecuteReqParams.setPreviousSequenceNumber(0);
                postTaskExecuteReqParams.setKeyTimeStamp(null);
                postTaskExecuteReqParams.setClaimMemo("");
                try {
                    postTaskExecuteReqResult = postController.postTaskExecuteReq(postTaskExecuteReqParams);
                    result2 = (Results.PostTaskExecuteReqResult) postTaskExecuteReqResult.getBody();
                    tmpResult2 = new OmCode(postTaskExecuteReqResult.getCode(), postTaskExecuteReqResult.getMessage());
                } catch (ServiceException ex) {
                    tmpResult2 = new OmCode(ex.getCode(), ex.getMessage());
                }
            }
        } else {
            boolean bPrallelExecFlag = false;
            boolean bOpeCompedLotParallelExec = false;
            Infos.PostConcurrentTaskExecuteReqResult result_p = new Infos.PostConcurrentTaskExecuteReqResult();

            Outputs.ObjPostProcessQueueParallelExecCheckOut strPostProcessQueueParallelExecCheckOut;
            Inputs.PostProcessQueueParallelExecCheckIn strPostProcessQueueParallelExecCheckIn = new Inputs.PostProcessQueueParallelExecCheckIn();
            strPostProcessQueueParallelExecCheckIn.setDKey("");
            strPostProcessQueueParallelExecCheckIn.setPostProcessActionInfoList(strPostTaskRegisterReqResult.getPostProcessActionInfoList());
            strPostProcessQueueParallelExecCheckIn.setLotCountCheckFlag(true);

            try {
                strPostProcessQueueParallelExecCheckOut = postProcessMethod.postProcessQueueParallelExecCheck(
                        strObjCommonIn,
                        strPostProcessQueueParallelExecCheckIn);
                bPrallelExecFlag = strPostProcessQueueParallelExecCheckOut.isParallelExecFlag();
            } catch (ServiceException ex) {
                bPrallelExecFlag = false;
            }

            if (CimBooleanUtils.isTrue(bPrallelExecFlag)) {
                /* TODO:
                pptPostConcurrentTaskExecuteReqInParm strPostConcurrentTaskExecuteReqInParm;
                strPostConcurrentTaskExecuteReqInParm.key      = strPostTaskRegisterReqResult.dKey;
                strPostConcurrentTaskExecuteReqInParm.syncFlag = SP_PostProcess_SyncFlag_Sync_Parallel;
                result_p = TxPostConcurrentTaskExecuteReq( requestUserID, strPostConcurrentTaskExecuteReqInParm, "" );
                tmpResult = result_p->strResult;
                tmpResult2.returnCode = CIMFWStrDup(RC_OK); // Error in Post Process for released lot is already merged in result of TxPostConcurrentTaskExecuteReq
                bOpeCompedLotParallelExec = TRUE;*/
            } else {
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams.setUser(user);
                postTaskExecuteReqParams.setKey(strPostTaskRegisterReqResult.getDKey());
                postTaskExecuteReqParams.setSyncFlag(BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL);
                postTaskExecuteReqParams.setPreviousSequenceNumber(0);
                postTaskExecuteReqParams.setKeyTimeStamp(null);
                postTaskExecuteReqParams.setClaimMemo("");
                Response postTaskExecuteReqResult;
                try {
                    postTaskExecuteReqResult = postController.postTaskExecuteReq(postTaskExecuteReqParams);
                    result = (Results.PostTaskExecuteReqResult) postTaskExecuteReqResult.getBody();
                    tmpResult = new OmCode(postTaskExecuteReqResult.getCode(), postTaskExecuteReqResult.getMessage());
                } catch (ServiceException ex) {
                    tmpResult = new OmCode(ex.getCode(), ex.getMessage());
                }
            }

            //Post Process Execution for Released Lot
            if (PostProcForLotFlag != 1) {
                if (0 < CimStringUtils.length(strPostTaskRegisterReqResult2.getDKey())) {
                    Outputs.ObjPostProcessQueueParallelExecCheckOut strPostProcessQueue_parallelExec_Check_out2;
                    Inputs.PostProcessQueueParallelExecCheckIn strPostProcessQueueParallelExecCheckIn2 = new Inputs.PostProcessQueueParallelExecCheckIn();
                    strPostProcessQueueParallelExecCheckIn2.setDKey("");
                    strPostProcessQueueParallelExecCheckIn2.setPostProcessActionInfoList(strPostTaskRegisterReqResult2.getPostProcessActionInfoList());
                    strPostProcessQueueParallelExecCheckIn2.setLotCountCheckFlag(true);
                    try {
                        strPostProcessQueue_parallelExec_Check_out2 = postProcessMethod.postProcessQueueParallelExecCheck(
                                strObjCommonIn,
                                strPostProcessQueueParallelExecCheckIn2);
                        bPrallelExecFlag = strPostProcessQueue_parallelExec_Check_out2.isParallelExecFlag();
                    } catch (ServiceException ex) {
                        bPrallelExecFlag = false;
                    }

                    if (TRUE == bPrallelExecFlag) {
                        /* TODO: NOTIMPL
                        pptPostConcurrentTaskExecuteReqResult_var result_p2;
                        pptPostConcurrentTaskExecuteReqInParm     strPostConcurrentTaskExecuteReqInParm2;
                        strPostConcurrentTaskExecuteReqInParm2.key      = strPostTaskRegisterReqResult2.dKey;
                        strPostConcurrentTaskExecuteReqInParm2.syncFlag = SP_PostProcess_SyncFlag_Sync_Parallel;
                        result_p2 = TxPostConcurrentTaskExecuteReq( requestUserID, strPostConcurrentTaskExecuteReqInParm2, "" );
                        tmpResult2 = result_p2->strResult;*/
                    } else {
                        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
                        postTaskExecuteReqParams.setUser(user);
                        postTaskExecuteReqParams.setKey(strPostTaskRegisterReqResult2.getDKey());
                        postTaskExecuteReqParams.setSyncFlag(BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL);
                        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
                        postTaskExecuteReqParams.setKeyTimeStamp(null);
                        postTaskExecuteReqParams.setClaimMemo("");
                        Response postTaskExecuteReqResult;
                        try {
                            postTaskExecuteReqResult = postController.postTaskExecuteReq(postTaskExecuteReqParams);
                            result2 = (Results.PostTaskExecuteReqResult) postTaskExecuteReqResult.getBody();
                            tmpResult2 = new OmCode(postTaskExecuteReqResult.getCode(), postTaskExecuteReqResult.getMessage());
                        } catch (ServiceException ex) {
                            tmpResult2 = new OmCode(ex.getCode(), ex.getMessage());
                        }
                    }
                } else {
                    tmpResult2 = retCodeConfig.getSucc();
                }
            } else {
                if (TRUE == bOpeCompedLotParallelExec) {
                    // Set strLotSpecSPCCheckResultSeq (for OpeCompedLot)
                    retVal.setMoveOutLot(result_p.getStrLotSpecSPCCheckResultSeq());
                } else {
                    // Set strLotSpecSPCCheckResultSeq (for OpeCompedLot)
                    retVal.setMoveOutLot(result==null?null:result.getStrLotSpecSPCCheckResultSeq());//strLotSpecSPCCheckResultSeq;

                    if (result!=null&&0 < CimStringUtils.length(result.getRelatedQueueKey())) {
                        Outputs.ObjPostProcessQueueParallelExecCheckOut strPostProcessQueueParallelExecCheckOut2;
                        Inputs.PostProcessQueueParallelExecCheckIn strPostProcessQueueParallelExecCheckIn2 = new Inputs.PostProcessQueueParallelExecCheckIn();
                        strPostProcessQueueParallelExecCheckIn2.setDKey(result.getRelatedQueueKey());
                        strPostProcessQueueParallelExecCheckIn2.setLotCountCheckFlag(TRUE);

                        try {
                            strPostProcessQueueParallelExecCheckOut2 = postProcessMethod.postProcessQueueParallelExecCheck(
                                    strObjCommonIn,
                                    strPostProcessQueueParallelExecCheckIn2);
                            bPrallelExecFlag = strPostProcessQueueParallelExecCheckOut2.isParallelExecFlag();
                        } catch (ServiceException ex) {
                            bPrallelExecFlag = FALSE;
                        }

                        if (TRUE == bPrallelExecFlag) {
                            /* TODO: NOTIMPL
                            pptPostConcurrentTaskExecuteReqResult_var result_p2;
                            pptPostConcurrentTaskExecuteReqInParm     strPostConcurrentTaskExecuteReqInParm2;
                            strPostConcurrentTaskExecuteReqInParm2.key      = result->relatedQueuekey;
                            strPostConcurrentTaskExecuteReqInParm2.syncFlag = SP_PostProcess_SyncFlag_Sync_Parallel;
                            result_p2 = TxPostConcurrentTaskExecuteReq( requestUserID, strPostConcurrentTaskExecuteReqInParm2, "" );
                            tmpResult2 = result_p2->strResult;*/
                        } else {
                            Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
                            postTaskExecuteReqParams.setUser(user);
                            postTaskExecuteReqParams.setKey(result.getRelatedQueueKey());
                            postTaskExecuteReqParams.setSyncFlag(BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL);
                            postTaskExecuteReqParams.setPreviousSequenceNumber(0);
                            postTaskExecuteReqParams.setKeyTimeStamp(null);
                            postTaskExecuteReqParams.setClaimMemo("");
                            Response postTaskExecuteReqResult;
                            try {
                                postTaskExecuteReqResult = postController.postTaskExecuteReq(postTaskExecuteReqParams);
                                result2 = (Results.PostTaskExecuteReqResult) postTaskExecuteReqResult.getBody();
                                tmpResult2 = new OmCode(postTaskExecuteReqResult.getCode(), postTaskExecuteReqResult.getMessage());
                            } catch (ServiceException ex) {
                                tmpResult2 = new OmCode(ex.getCode(), ex.getMessage());
                            }
                        }
                    } else {
                        tmpResult2 = retCodeConfig.getSucc();
                    }
                }
            }
        }
        OmCode tmp_rc = retCodeConfig.getSucc();

        // OpeComped Lot
        OmCode rc = tmpResult;
        OmCode retValResult = null;
        if (!Validations.isEquals(rc, retCodeConfig.getSucc())) {
            retValResult = tmpResult;
        }

        // Released Lot
        if (Validations.isEquals(rc, retCodeConfig.getSucc())) {
            if (tmpResult2 != null) {
                tmp_rc = tmpResult2;
                if (!Validations.isEquals(tmp_rc, retCodeConfig.getSucc())) {
                    retValResult = tmpResult2;
                    rc = tmp_rc;
                }
            }
        }

        Validations.check(retValResult != null, retValResult);

        //--------------------------------------
        // Calculate length of lotID in cassette
        //--------------------------------------
        int opeCompLotRecLen = CimArrayUtils.getSize(retVal.getMoveOutLot());
        int strLotIDOpeCompLen = 0;
        int i_Lot = 0;

        for (i_Lot = 0; i_Lot < opeCompLotRecLen; i_Lot++) {
            strLotIDOpeCompLen += CimStringUtils.length(retVal.getMoveOutLot().get(i_Lot).getLotID().getValue()) + 1;
        }

        StringBuilder strLotIDOpeComp;
        strLotIDOpeComp = new StringBuilder();

        for (i_Lot = 0; i_Lot < opeCompLotRecLen; i_Lot++) {
            strLotIDOpeComp.append(retVal.getMoveOutLot().get(i_Lot).getLotID().getValue());
            strLotIDOpeComp.append(",");
        }

        // clearThreadSpecificDataString ();
        ThreadContextHolder.clearThreadSpecificDataString();

        return Response.createSucc("OEQPW006", retVal);
    }

    @Autowired
    private IEquipmentProcessOperation equipmentProcessOperation;

    @Override
    @ResponseBody
    @PostMapping(value = "/move_out/req")
    @CimMapping(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ)
    @EnablePostProcess(proxy = MoveOutReqPostProcessProxy.class)
    public Response moveOutReq(@RequestBody Params.OpeComWithDataReqParams opeComWithDataReqParams) {
        //---------------------------------------
        // Step1 : Initialising transactionID
        //---------------------------------------
        if (log.isDebugEnabled()) log.debug("step1 : Initialising transactionID");
        String txID = TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue();
        ThreadContextHolder.setTransactionId(txID);

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txID, opeComWithDataReqParams.getUser());
        List<ObjectIdentifier> lotIDs = Collections.emptyList();
        int accessCheckFlag = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getIntValue();
        if (accessCheckFlag == 1) {
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, opeComWithDataReqParams.getControlJobID());
        }

        //------------------------------------------
        // Step2 : Privilege check
        //------------------------------------------
        if (log.isDebugEnabled()) log.debug("step2 : Privilege check");
        ObjectIdentifier equipmentID = opeComWithDataReqParams.getEquipmentID();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(ObjectIdentifier.emptyIdentifier());
        accessControlCheckInqParams.setProductIDList(Collections.emptyList());
        accessControlCheckInqParams.setRouteIDList(Collections.emptyList());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(Collections.emptyList());
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //----------------------------------
        //   Main Process
        //----------------------------------
        if (log.isDebugEnabled()) log.debug("step3 : Main Process. call sxMoveOutReq(...)");
//        Results.MoveOutReqResult retVal = equipmentService.sxMoveOutReq(
//                objCommon,
//                opeComWithDataReqParams);
        Results.MoveOutReqResult retVal = equipmentProcessOperation.sxMoveOutReq(objCommon, opeComWithDataReqParams);
        return Response.createSucc(txID, retVal);

        //----------------------------------
        //   Post Process Enabled
        //----------------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/move_in_cancel_for_ib/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ)
    public Response moveInCancelForIBReqOld(@RequestBody Params.MoveInCancelForIBReqParams params) {
        //set current tx id
        String txId = TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txId);
        }
        //Step1 calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1"))) {
            // step2 - cassette_lotIDList_GetDR
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }

        //Step3 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //Step4 - txMoveInCancelForIBReq__120
        Results.MoveInCancelReqResult result = equipmentService.sxMoveInCancelForIBReq(objCommon,
                params.getEquipmentID(),
                params.getControlJobID(),
                null,
                null,
                null);

        Response response = Response.createSucc(txId, result.getStartCassetteList());
        Validations.isSuccessWithException(response);
        //TODO: Step5 - txPostTaskRegisterReq__100
        //TODO: Step6 -  txPostActionModifyReq__130
        //TODO: Step7 -  TxPostTaskExecuteReq__100
        //TODO: Step8 - txAPCCJRpt
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return response;
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/move_in_cancel_for_ib/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ)
    @EnablePostProcess
    public Response moveInCancelForIBReq(@RequestBody Params.MoveInCancelForIBReqParams params) {
        //set current tx id
        String txId = TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = params.getUser();
        Validations.check(null == user, retCodeConfig.getInvalidParameter());

        //Step1 calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        int accessCheckForCjIntValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getIntValue();
        List<ObjectIdentifier> lotIDs = Collections.emptyList();
        if (accessCheckForCjIntValue == 1) {
            // step2 - cassette_lotIDList_GetDR
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }

        //Step3 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //Step4 - txMoveInCancelForIBReq__120
        Results.MoveInCancelReqResult result = equipmentService.sxMoveInCancelForIBReq(objCommon,
                params.getEquipmentID(),
                params.getControlJobID(),
                null,
                null,
                null);
        return Response.createSucc(txId, result);

        //-------------------------
        // Post Process
        //-------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/move_in_cancel/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.OPERATION_START_CANCEL_REQ)
    public Response moveInCancelReqOld(@RequestBody Params.MoveInCancelReqParams params) {
        //===========Pre process============
        String txId = TransactionIDEnum.OPERATION_START_CANCEL_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txId);
        }
        //Step1 calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        //Step2 - controlJob_lotIDList_GetDR
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1"))) {
            // step2 - cassette_lotIDList_GetDR
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }


        //Step-3:txAccessControlCheckInq;
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //===========Main process============
        //Step4 - txMoveInCancelReq__120
        Results.MoveInCancelReqResult result = equipmentService.sxMoveInCancelReq(objCommon, params.getEquipmentID(),
                params.getControlJobID(), params.getOpeMemo(), null, null, null);
        //TODO: Step5 - txAPCCJRpt

        //===========Post process============

        return Response.createSucc(txId, result.getStartCassetteList());
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/move_in_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.OPERATION_START_CANCEL_REQ)
    @EnablePostProcess
    public Response moveInCancelReq(@RequestBody Params.MoveInCancelReqParams params) {
        //===========Pre process============
        String txId = TransactionIDEnum.OPERATION_START_CANCEL_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        Validations.check(null == user, retCodeConfig.getInvalidParameter());

        //Step1 calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);
        //Step2 - controlJob_lotIDList_GetDR
        int accessCheckForCjIntValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getIntValue();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        if (accessCheckForCjIntValue == 1) {
            // step2 - cassette_lotIDList_GetDR
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }


        //Step-3:txAccessControlCheckInq;
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //===========Main process============
        //Step4 - txMoveInCancelReq__120
        Results.MoveInCancelReqResult result = equipmentService.sxMoveInCancelReq(objCommon,
                params.getEquipmentID(),
                params.getControlJobID(),
                params.getOpeMemo(),
                null,
                null,
                null);
        //TODO: Step5 - txAPCCJRpt
        return Response.createSucc(txId, result);

        //===========Post process============
    }

    @ResponseBody
    @RequestMapping(value = "/old/move_in_for_ib/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.OPERATION_START_FOR_INTERNAL_BUFFER_REQ)
    public Response moveInForIBReqOld(@RequestBody Params.MoveInForIBReqParams moveInForIBReqParams) {
        String txId = TransactionIDEnum.OPERATION_START_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        User user = moveInForIBReqParams.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txId);
        }

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        List<ObjectIdentifier> lotIDs = new ArrayList<>();

        int lenStartCassette = CimArrayUtils.getSize(moveInForIBReqParams.getStartCassetteList());

        for (int i = 0; i < lenStartCassette; i++) {
            Infos.StartCassette startCassette = moveInForIBReqParams.getStartCassetteList().get(i);
            int lenLotInCassette = CimArrayUtils.getSize(startCassette.getLotInCassetteList());

            for (int j = 0; j < lenLotInCassette; j++) {
                Infos.LotInCassette lotInCassette = startCassette.getLotInCassetteList().get(j);
                if (lotInCassette.getMoveInFlag()) {
                    lotIDs.add(lotInCassette.getLotID());
                }
            }
        }


        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(moveInForIBReqParams.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);

        // step2 - txAccessControlCheckInq
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        String apcIFControlStatus = null;
        String dcsIFControlStatus = null;
        // step3 - txMoveInForIBReq
        Results.MoveInReqResult retVal = null;
        try {
            retVal = equipmentService.sxMoveInForIBReq(objCommon, moveInForIBReqParams, apcIFControlStatus, dcsIFControlStatus);
            // Auto Clean(Auto block Dirty FOUP), by ho
            List<Infos.StartCassette> startCassetteList = moveInForIBReqParams.getStartCassetteList();
            if (CimArrayUtils.isNotEmpty(startCassetteList)){
                startCassetteList.forEach(startCassette -> {
                    durableService.sxAutoClean(objCommon,startCassette.getCassetteID(),moveInForIBReqParams.getOpeMemo());
                });
            }
            Results.PostTaskRegisterReqResult strPostTaskRegisterReqResult = null;
            String runWaferUpdateByPostProc1 = StandardProperties.OM_PP_UPDATE_FOR_EQP_ATTR.getValue();

            runWaferUpdateByPostProc1 = null == runWaferUpdateByPostProc1 ? "0" : runWaferUpdateByPostProc1;
            int runWaferUpdateByPostProc = Integer.parseInt(runWaferUpdateByPostProc1);

            Params.PostTaskRegisterReqParams strPostProcessRegistrationParm = new Params.PostTaskRegisterReqParams();
            strPostProcessRegistrationParm.setTransactionID(objCommon.getTransactionID());
            strPostProcessRegistrationParm.setSequenceNumber(-1);
            strPostProcessRegistrationParm.setClaimMemo(moveInForIBReqParams.getOpeMemo());

            Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
            postProcessRegistrationParam.setEquipmentID(moveInForIBReqParams.getEquipmentID());
            postProcessRegistrationParam.setLotIDs(lotIDs);
            postProcessRegistrationParam.setControlJobID(retVal.getControlJobID());
            strPostProcessRegistrationParm.setPostProcessRegistrationParm(postProcessRegistrationParam);


            // step4 - txPostTaskRegisterReq__100
            strPostTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, strPostProcessRegistrationParm);
            //----------------------------------
            // Post-Processing Update Section
            //----------------------------------

            if (runWaferUpdateByPostProc == 1) {
                String strRunWaferCnt = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT);
                List<Infos.PostProcessAdditionalInfo> strPostProcessAdditionalInfoSeq = new ArrayList<>();
                boolean additionalInfoFlag = false;
                List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq = strPostTaskRegisterReqResult.getPostProcessActionInfoList();
                for (int i = 0; i < CimArrayUtils.getSize(strPostProcessActionInfoSeq); i++) {
                    Infos.PostProcessActionInfo strPostProcessActionInfo = strPostProcessActionInfoSeq.get(i);
                    if (CimStringUtils.equals(strPostProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_RUNWAFERINFOUPDATE)) {
                        Infos.PostProcessAdditionalInfo strPostProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                        strPostProcessAdditionalInfoSeq.add(strPostProcessAdditionalInfo);
                        strPostProcessAdditionalInfo.setDKey(strPostProcessActionInfo.getDKey());
                        strPostProcessAdditionalInfo.setSequenceNumber(strPostProcessActionInfo.getSequenceNumber());
                        strPostProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT);
                        strPostProcessAdditionalInfo.setValue(strRunWaferCnt);

                        strPostProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                        strPostProcessAdditionalInfoSeq.add(strPostProcessAdditionalInfo);
                        strPostProcessAdditionalInfo.setDKey(strPostProcessActionInfo.getDKey());
                        strPostProcessAdditionalInfo.setSequenceNumber(strPostProcessActionInfo.getSequenceNumber());
                        strPostProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_OPESTARTCNT);
                        strPostProcessAdditionalInfo.setValue("1");
                        additionalInfoFlag = true;
                        break;
                    }
                }
                if (additionalInfoFlag) {
                    // step5 - TODO-NOTIMPL call txPostActionModifyReq__130
                }
            }
            //retVal.setReturnCode(retCodeConfig.getSucc());

            Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
            postTaskExecuteReqParams.setKey(strPostTaskRegisterReqResult.getDKey());
            postTaskExecuteReqParams.setUser(user);
            postTaskExecuteReqParams.setSyncFlag(1);
            postTaskExecuteReqParams.setPreviousSequenceNumber(0);

            // step6 - TxPostTaskExecuteReq__100
            /*
            Response result = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams);
            if( !Validations.isSuccess(result.getCode())) {
                retVal.setReturnCode(new OmCode(result.getCode(),result.getMessage()));
            }
            */
//        if (Validations.isSuccess(retVal)) {
//
//        } else {
//            // TX_ROLLBACK(txMoveInForIBReq) ;
//            if (!StringUtils.isEmpty(apcIFControlStatus) && StringUtils.equals(cimEnvironmentVariable.getEnvironmentValue(EnvEnum.SP_ROLL_BACK_NOTIFY_TO_APC), BizConstant.VALUE_ONE)) {
//                // step7 - TODO-NOTIMPL txAPCCJRpt
//            }
//            if (!StringUtils.isEmpty(dcsIFControlStatus)) {
//                // step8 - TODO-NOTIMPL txOpeStartCancelForDCSRpt
//            }
//        }
        } catch (ServiceException e) {
            //--------------------------------
            // Entity Inhibit
            //--------------------------------
            String envInhibitWhenAPC = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getValue();
            if (BizConstant.VALUE_ONE.equals(envInhibitWhenAPC)) {
                if (Validations.isEquals(retCodeConfig.getNoResponseApc(), e.getCode()) ||
                        Validations.isEquals(retCodeConfig.getApcServerBindFail(), e.getCode()) ||
                        Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), e.getCode()) ||
                        Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), e.getCode()) ||
                        Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), e.getCode())) {

                    String strClaimMemo = "APC IF Error.";
//                int saveRc = rc;

                    Infos.EntityInhibitDetailAttributes entityInhibitAttributes = new Infos.EntityInhibitDetailAttributes();
                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                    Infos.EntityIdentifier entity = new Infos.EntityIdentifier();
                    entity.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                    entity.setObjectID(moveInForIBReqParams.getEquipmentID());
                    entity.setAttribution("");
                    entities.add(entity);
                    entityInhibitAttributes.setEntities(entities);
                    List<String> subLotTypes = new ArrayList<>();
                    entityInhibitAttributes.setSubLotTypes(subLotTypes);
                    entityInhibitAttributes.setStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());

                    if (Validations.isEquals(retCodeConfig.getNoResponseApc(), e.getCode()) ||
                            Validations.isEquals(retCodeConfig.getApcServerBindFail(), e.getCode())) {
                        entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                    } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), e.getCode()) ||
                            Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), e.getCode())) {
                        entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                    } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), e.getCode())) {
                        entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                    }

//                 注意   strEntityInhibitions.ownerID          = strObjCommonIn.strUser.userID;
                    entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                    entityInhibitAttributes.setClaimedTimeStamp(null);

                    Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                    mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitAttributes);
                    mfgRestrictReqParams.setClaimMemo(strClaimMemo);
//                    // step9 - txMfgRestrictReq__101
//                    Infos.EntityInhibitDetailInfo strMfgRestrictReqResult = constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                    //Step5 - txMfgRestrictReq__110
                    Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                    List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                    entityInhibitDetailAttributeList.add(entityInhibitAttributes);
                    mfgRestrictReq_110Params.setClaimMemo(strClaimMemo);
                    mfgRestrictReq_110Params.setUser(objCommon.getUser());
                    mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                    constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);

                }
            } else {
                throw e;
            }
        }
        return Response.createSucc(objCommon.getTransactionID(), retVal);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/move_in_for_ib/req", method = RequestMethod.POST)
    @EnablePostProcess(proxy = MoveInPostProcessProxy.class)
    @CimMapping(TransactionIDEnum.OPERATION_START_FOR_INTERNAL_BUFFER_REQ)
    public Response moveInForIBReq(@RequestBody Params.MoveInForIBReqParams moveInForIBReqParams) {
        // transaction id
        String txId = TransactionIDEnum.OPERATION_START_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        // check input param
        User user = moveInForIBReqParams.getUser();
        List<Infos.StartCassette> startCassetteList = moveInForIBReqParams.getStartCassetteList();
        ObjectIdentifier equipmentID = moveInForIBReqParams.getEquipmentID();
        Validations.check(null == user || CimArrayUtils.isEmpty(startCassetteList) || null == equipmentID,
                retCodeConfig.getInvalidParameter());

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        moveInForIBReqParams.getStartCassetteList().forEach(startCassette ->
                startCassette.getLotInCassetteList().forEach(lotInCassette -> {
                    if (lotInCassette.getMoveInFlag()) {
                        lotIDs.add(lotInCassette.getLotID());
                    }
                }));
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(moveInForIBReqParams.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        // step2 - txAccessControlCheckInq
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        String apcIFControlStatus = null;
        String dcsIFControlStatus = null;

        //---------------------------------------
        // Main Process
        //---------------------------------------
        // step3 - txMoveInForIBReq
        Results.MoveInReqResult retVal = null;
        try {
            // call sxMoveInForIBReq...
            retVal = equipmentService.sxMoveInForIBReq(objCommon, moveInForIBReqParams, apcIFControlStatus, dcsIFControlStatus);

            // Auto Clean(Auto block Dirty FOUP), by ho
            startCassetteList.forEach(startCassette -> {
                durableService.sxAutoClean(objCommon, startCassette.getCassetteID(), moveInForIBReqParams.getOpeMemo());
            });

        } catch (ServiceException e) {

            // todo txAPCCJRpt

            //-----------------------------------------
            // Send to OpeStartCancel to DCS.
            //-----------------------------------------
            // todo txOpeStartCancelForDCSRpt

            //--------------------------------
            // Entity Inhibit
            //--------------------------------
            int envInhibitWhenAPC = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getIntValue();
            //------------------------------------------
            //  Entity Inhibit enable check.
            //  OM_CONSTRAINT_APC_RPARM_CHG_ERROR
            //       0 --> Inhibit disable
            //       1 --> Inhibit enable
            //------------------------------------------
            if (envInhibitWhenAPC == 1) {
                final Integer retCode = e.getCode();
                if (Validations.isEquals(retCodeConfig.getNoResponseApc(), retCode)
                        || Validations.isEquals(retCodeConfig.getApcServerBindFail(), retCode)
                        || Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), retCode)
                        || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), retCode)
                        || Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), retCode)) {
                    if (log.isDebugEnabled())
                        log.debug("OM_CONSTRAINT_APC_RPARM_CHG_ERROR = 1, and APC error, so create Constraint for Equipment[{}]",
                                ObjectIdentifier.fetchValue(equipmentID));

                    Infos.EntityInhibitDetailAttributes detailAttributes = new Infos.EntityInhibitDetailAttributes();
                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                    Infos.EntityIdentifier entity = new Infos.EntityIdentifier();
                    entity.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                    entity.setObjectID(equipmentID);
                    entity.setAttribution("");
                    entities.add(entity);
                    detailAttributes.setEntities(entities);
                    List<String> subLotTypes = new ArrayList<>();
                    detailAttributes.setSubLotTypes(subLotTypes);
                    detailAttributes.setStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());

                    if (Validations.isEquals(retCodeConfig.getNoResponseApc(), retCode)
                            || Validations.isEquals(retCodeConfig.getApcServerBindFail(), retCode)) {
                        detailAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                    } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), retCode)
                            || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), retCode)) {
                        detailAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                    } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), retCode)) {
                        detailAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                    }

                    detailAttributes.setOwnerID(objCommon.getUser().getUserID());

                    //Step5 - txMfgRestrictReq__110
                    Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                    List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                    entityInhibitDetailAttributeList.add(detailAttributes);
                    mfgRestrictReq_110Params.setClaimMemo("APC IF Error.");
                    mfgRestrictReq_110Params.setUser(objCommon.getUser());
                    mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                    constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params, objCommon);
                }
            } else {
                throw e;
            }
        }

        return Response.createSucc(objCommon.getTransactionID(), retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/old/move_in/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.OPERATION_START_REQ)
    public Response moveInReqOld(@RequestBody Params.MoveInReqParams params) {
        //===========Pre process============
        String txId = TransactionIDEnum.OPERATION_START_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        User user = params.getUser();
        List<Infos.StartCassette> startCassetteList = params.getStartCassetteList();
        Validations.check(user == null || CimObjectUtils.isEmpty(startCassetteList), "parameter is null");

        //Step1 calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        //Step2 - call txAccessControlCheckInq(...)
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (Infos.StartCassette startCassette : startCassetteList) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            lotInCassetteList.forEach(x -> lotIDs.add(x.getLotID()));
        }
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //===========Main process============
        //Step3 - txMoveInReq
        Results.MoveInReqResult moveInReqResult = null;
        try {
            moveInReqResult = equipmentService.sxMoveInReq(objCommon, params.getEquipmentID(), params.getPortGroupID(),
                    params.getControlJobID(), startCassetteList, params.isProcessJobPauseFlag(), null,
                    null, params.getOpeMemo());

            // Auto Clean(Auto block Dirty FOUP), by ho
            if (CimArrayUtils.isNotEmpty(startCassetteList)){
                startCassetteList.forEach(startCassette -> {
                    durableService.sxAutoClean(objCommon,startCassette.getCassetteID(),params.getOpeMemo());
                });
            }

            //done: Step4 - txPostTaskRegisterReq__100
            log.info("Registration of Post-Processng for OpeCompedLot");
            Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam(params.getEquipmentID(),
                    moveInReqResult.getControlJobID(), lotIDs, null);
            Results.PostTaskRegisterReqResult sxPostTaskRegisterReq = postService.sxPostTaskRegisterReq(objCommon,
                    new Params.PostTaskRegisterReqParams(objCommon.getTransactionID(), null, null, -1,
                            postProcessRegistrationParam, params.getOpeMemo()));
            //----------------------------------
            // Post-Processing Update Section
            //----------------------------------
            //TODO: txPostActionModifyReq__130

            //done: Step5 - TxPostTaskExecuteReq__100
            try {
                postTaskExecuteReqController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(user,
                        sxPostTaskRegisterReq.getDKey(), 1, 0, null, ""));
            } catch (Exception ex) {
                throw ex;
                //TODO: Step6 - txAPCCJRpt
                //TODO: Step7 - txOpeStartCancelForDCSRpt
            }
        } catch (ServiceException e) {
            //--------------------------------
            // Entity Inhibit
            //--------------------------------
            Long envInhibitWhenAPC = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getLongValue();
            if (envInhibitWhenAPC == 1) {
                if (Validations.isEquals(retCodeConfig.getNoResponseApc(), e.getCode())
                        || Validations.isEquals(retCodeConfig.getApcServerBindFail(), e.getCode())
                        || Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), e.getCode())
                        || Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), e.getCode())) {
                    Params.MfgRestrictReqParams strEntityInhibitions = new Params.MfgRestrictReqParams();

                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                    entityInhibitDetailAttributes.setEntities(entities);
                    strEntityInhibitions.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
                    Infos.EntityIdentifier entity = new Infos.EntityIdentifier();
                    entities.add(entity);
                    entity.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                    entity.setObjectID(params.getEquipmentID());
                    entity.setAttribution("");
                    List<String> subLotTypes = new ArrayList<>();
                    entityInhibitDetailAttributes.setSubLotTypes(subLotTypes);
                    entityInhibitDetailAttributes.setStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());

                    if (Validations.isEquals(retCodeConfig.getNoResponseApc(), e.getCode()) ||
                            Validations.isEquals(retCodeConfig.getApcServerBindFail(), e.getCode())) {
                        entityInhibitDetailAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                    } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), e.getCode()) ||
                            Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), e.getCode())) {
                        entityInhibitDetailAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                    } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), e.getCode())) {
                        entityInhibitDetailAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCERROR);
                    }

                    entityInhibitDetailAttributes.setOwnerID(objCommon.getUser().getUserID());
                    entityInhibitDetailAttributes.setClaimedTimeStamp("");

//                    // Step8 - txMfgRestrictReq__101
//                    constraintService.sxMfgRestrictReq(strEntityInhibitions, objCommon);

                    //Step8 - txMfgRestrictReq__110
                    Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                    List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                    entityInhibitDetailAttributeList.add(entityInhibitDetailAttributes);
                    mfgRestrictReq_110Params.setClaimMemo(params.getOpeMemo());
                    mfgRestrictReq_110Params.setUser(objCommon.getUser());
                    mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                    constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);

                }
            } else {
                throw e;
            }
        }

        //===========Post process============
        return Response.createSucc(txId, moveInReqResult);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/move_in/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.OPERATION_START_REQ)
    @EnablePostProcess(proxy = MoveInPostProcessProxy.class)
    public Response moveInReq(@RequestBody Params.MoveInReqParams params) {
        //===========Pre process============
        String txId = TransactionIDEnum.OPERATION_START_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        User user = params.getUser();
        List<Infos.StartCassette> startCassetteList = params.getStartCassetteList();
        Validations.check(user == null || CimObjectUtils.isEmpty(startCassetteList),
                retCodeConfig.getInvalidParameter());

        //Step1 calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        //Step2 - call txAccessControlCheckInq(...)
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        startCassetteList.forEach(startCassette -> startCassette.getLotInCassetteList()
                .forEach(lotInCassette -> lotIDs.add(lotInCassette.getLotID())));

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //---------------------------------------
        //===========Main process============
        //---------------------------------------
        Results.MoveInReqResult moveInReqResult = null;
        try {
            // call sxMoveInReq
            moveInReqResult = equipmentService.sxMoveInReq(
                    objCommon,
                    params.getEquipmentID(),
                    params.getPortGroupID(),
                    params.getControlJobID(),
                    startCassetteList,
                    params.isProcessJobPauseFlag(),
                    null,
                    null,
                    params.getOpeMemo());

            //--------------------------------------------------
            // Auto Clean(Auto block Dirty FOUP), by ho
            //--------------------------------------------------
            startCassetteList.forEach(startCassette -> {
                durableService.sxAutoClean(objCommon, startCassette.getCassetteID(), params.getOpeMemo());
            });
        } catch (ServiceException e) {

            //TODO: Step6 - txAPCCJRpt

            //-----------------------------------------
            // Send to OpeStartCancel to DCS.
            //-----------------------------------------
            //TODO: Step7 - txOpeStartCancelForDCSRpt

            //--------------------------------
            // Entity Inhibit
            //--------------------------------
            Long envInhibitWhenAPC = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getLongValue();
            if (log.isDebugEnabled()) log.debug("ENV OM_CONSTRAINT_APC_RPARM_CHG_ERROR = {} ", envInhibitWhenAPC);
            //------------------------------------------
            //  Entity Inhibit enable check.
            //  OM_CONSTRAINT_APC_RPARM_CHG_ERROR
            //       0 --> Inhibit disable
            //       1 --> Inhibit enable
            //------------------------------------------
            if (envInhibitWhenAPC == 1) {
                if (Validations.isEquals(retCodeConfig.getNoResponseApc(), e.getCode())
                        || Validations.isEquals(retCodeConfig.getApcServerBindFail(), e.getCode())
                        || Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), e.getCode())
                        || Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), e.getCode())) {
                    if (log.isDebugEnabled())
                        log.debug("OM_CONSTRAINT_APC_RPARM_CHG_ERROR = 1 , Create Constraint for Equipment[{}].",
                                ObjectIdentifier.fetchValue(params.getEquipmentID()));

                    Params.MfgRestrictReqParams strEntityInhibitions = new Params.MfgRestrictReqParams();
                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                    Infos.EntityInhibitDetailAttributes detailAttributes = new Infos.EntityInhibitDetailAttributes();
                    detailAttributes.setEntities(entities);
                    strEntityInhibitions.setEntityInhibitDetailAttributes(detailAttributes);
                    Infos.EntityIdentifier entity = new Infos.EntityIdentifier();
                    entities.add(entity);
                    entity.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                    entity.setObjectID(params.getEquipmentID());
                    entity.setAttribution("");
                    List<String> subLotTypes = new ArrayList<>();
                    detailAttributes.setSubLotTypes(subLotTypes);
                    detailAttributes.setStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());

                    if (Validations.isEquals(retCodeConfig.getNoResponseApc(), e.getCode())
                            || Validations.isEquals(retCodeConfig.getApcServerBindFail(), e.getCode())) {
                        detailAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                    } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), e.getCode())
                            || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), e.getCode())) {
                        detailAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                    } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), e.getCode())) {
                        detailAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCERROR);
                    }

                    detailAttributes.setOwnerID(objCommon.getUser().getUserID());
                    detailAttributes.setClaimedTimeStamp("");

                    //Step8 - sxMfgRestrictReq_110
                    Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                    List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                    entityInhibitDetailAttributeList.add(detailAttributes);
                    mfgRestrictReq_110Params.setClaimMemo(params.getOpeMemo());
                    mfgRestrictReq_110Params.setUser(objCommon.getUser());
                    mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                    //-----------------------------
                    // call sxMfgRestrictReq_110
                    //-----------------------------
                    constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params, objCommon);
                }
            } else {
                throw e;
            }
        }

        //===========Post process============
        return Response.createSucc(txId, moveInReqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_unloading/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.UN_LOADING_LOT_RPT)
    public Response uncarrierLoadingRpt(@RequestBody Params.loadOrUnloadLotRptParams param) {
        //===========Pre process============
        String txId = TransactionIDEnum.UN_LOADING_LOT_RPT.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, param.getUser(), accessControlCheckInqParams);

        //===========Main process============
        //Step3 - txEqpStatusChangeReq
        equipmentService.sxCarrierUnloadingRpt(objCommon, param.getEquipmentID(), param.getCassetteID(), param.getPortID());
        //===========Post process============

        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/eap_recovery/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.TCS_RECOVERY_REQ)
    public Response eapRecoveryReq(@RequestBody Params.EAPRecoveryReqParam params) {
        //【Step-1】:Initialize Parameters;
        if (log.isDebugEnabled()){
            log.debug("【Step-1】initialize Parameters");
        }
        final TransactionIDEnum transactionID = TransactionIDEnum.TCS_RECOVERY_REQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //【Step-2】:txAccessControlCheckInq;
        if (log.isDebugEnabled()){
            log.debug("【Step-2】checkPrivilegeAndGetObjCommon(...)");
        }
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(),
                params.getUser(), accessControlCheckInqParams);

        //【Step-3】:txEAPRecoveryReq
        if (log.isDebugEnabled()){
            log.debug("【Step-3】sxEAPRecoveryReq(...)");
        }
        Results.EAPRecoveryReqResult result = equipmentService.sxEAPRecoveryReq(objCommon, params);
        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/old/force_move_out/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.FORCE__OPE_COMP_REQ)
    public Response forceMoveOutReqOld(@RequestBody Params.ForceMoveOutReqParams params) {

        //【step0】init params
        final String transactionID = TransactionIDEnum.FORCE__OPE_COMP_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        Validations.check(null == user, "the user info is null");

        //【step0】Incoming Log Put
        log.info(" Incoming = {}", transactionID);

        //【step1】calendar_GetCurrentTimeDR
        log.info("【step1】calendar_GetCurrentTimeDR");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //【step2】 controlJob_lotIDList_GetDR
        log.info("【step2】controlJob_lotIDList_GetDR");
        List<ObjectIdentifier> lotIds = new ArrayList<>();
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        log.info("env value of OM_ACCESS_CHECK_FOR_CJ  = {}", tmpPrivilegeCheckCJValue);
        if ((CimStringUtils.isNotEmpty(tmpPrivilegeCheckCJValue)) && CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1")) {
            //Step2 - controlJob_lotIDList_GetDR
            log.info("Call controlJob_lotIDList_GetDR");
            lotIds = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobId());
        } else {
            log.info("OM_ACCESS_CHECK_FOR_CJ OFF Do Nothing.");
        }

        //【step3】txAccessControlCheckInq
        log.info("【step3】 txAccessControlCheckInq");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIds);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentId());
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //Main Process
        List<ObjectIdentifier> holdReleaseLotIds = new ArrayList<>();
        //【step4】call  txForceMoveOutReq
        log.info("call txForceMoveOutReq");
        Results.ForceMoveOutReqResult result = equipmentProcessOperation.sxForceMoveOutReq(objCommon, params.getEquipmentId(),
                params.getControlJobId(), params.getSpcResultRequiredFlag(), params.getClaimMemo());

        int PostProcForLotFlag = StandardProperties.OM_PP_FOR_LOT_MOVEOUT_FLAG.getIntValue();
        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        List<Infos.OpeCompLot> partialOpeCompLotList = result.getStrOpeCompLot();
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult1 = new Results.PostTaskRegisterReqResult();
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult2 = new Results.PostTaskRegisterReqResult();
        if (-1 == PostProcForLotFlag) {
            log.info("Get PostProcForLotFlag used in OpeComp process");
            String strPostProcForLotFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCFORLOTFLAG);
            PostProcForLotFlag = strPostProcForLotFlag.equals("1") ? 1 : 0;
        }
        String tmpTxID = PostProcForLotFlag != 1 ? "OEQPW014" : "TXEWC_10";
        //For OperationCompletion lot (PostProcess)
        List<ObjectIdentifier> opeCompedLotIDs = new ArrayList<>();
        for (Infos.OpeCompLot partialOpeCompLot : partialOpeCompLotList) {
            opeCompedLotIDs.add(partialOpeCompLot.getLotID());
        }
        if (!CimObjectUtils.isEmpty(opeCompedLotIDs)) {

            try {
                Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                postProcessRegistrationParam.setLotIDs(opeCompedLotIDs);
                postProcessRegistrationParam.setEquipmentID(params.getEquipmentId());
                postProcessRegistrationParam.setControlJobID(params.getControlJobId());
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
                postTaskRegisterReqParams.setTransactionID(tmpTxID);
                postTaskRegisterReqParams.setPatternID(null);
                postTaskRegisterReqParams.setKey(null);
                postTaskRegisterReqParams.setSequenceNumber(-1);
                //Post-Processing Registration for OpeComped Lot
                postTaskRegisterReqResult1 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);

                //--------------------------------------------------
                // Post-Processing for OpeCompWithHoldLot Update Section
                //--------------------------------------------------
                List<Infos.PostProcessActionInfo> postProcessActionInfoList = postTaskRegisterReqResult1.getPostProcessActionInfoList();
                List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList = new ArrayList<>();
                for (Infos.PostProcessActionInfo postProcessActionInfo : postProcessActionInfoList) {
                    if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_QTIME)) {
                        if (1 == PostProcForLotFlag) {
                            Outputs.ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout getDRoutRetCode = lotMethod
                                    .lotPostProcessPreviousBranchAndReturnInfoGetDR(objCommon, postProcessActionInfo.getPostProcessTargetObject().getLotID());

                            if (!ObjectIdentifier.isEmptyWithValue(getDRoutRetCode.getBranchRouteID())) {
                                List<String> strings = new ArrayList<>();
                                strings.add(getDRoutRetCode.getBranchRouteID().getValue());
                                strings.add(getDRoutRetCode.getBranchOperationNumber());
                                String previousBranchInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);
                                postProcessAdditionalInfo.setValue(previousBranchInfo);
                            }
                            if (!ObjectIdentifier.isEmptyWithValue(getDRoutRetCode.getReturnRouteID())) {
                                List<String> strings = new ArrayList<>();
                                strings.add(getDRoutRetCode.getReturnRouteID().getValue());
                                strings.add(getDRoutRetCode.getReturnOperationNumber());
                                String previousReturnInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);
                                postProcessAdditionalInfo.setValue(previousReturnInfo);
                            }
                            if (!CimStringUtils.isEmpty(getDRoutRetCode.getReworkOutKey())) {
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);
                                postProcessAdditionalInfo.setValue(getDRoutRetCode.getReworkOutKey());
                            }
                        }
                    }
                }

                if (!CimObjectUtils.isEmpty(postProcessAdditionalInfoList)) {
                    postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                            postTaskRegisterReqResult1.getPostProcessActionInfoList(), postProcessAdditionalInfoList, params.getClaimMemo());
                }

                if (!CimObjectUtils.isEmpty(holdReleaseLotIds)) {
                    log.info("Registration of Post-Processng for HoldReleasedLot");
                    postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                    postProcessRegistrationParam.setLotIDs(holdReleaseLotIds);
                    postProcessRegistrationParam.setEquipmentID(params.getEquipmentId());
                    postProcessRegistrationParam.setControlJobID(params.getControlJobId());
                    postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                    postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
                    postTaskRegisterReqParams.setTransactionID("ThEWC010");
                    postTaskRegisterReqParams.setPatternID(null);
                    postTaskRegisterReqParams.setKey(null);
                    postTaskRegisterReqParams.setSequenceNumber(-1);
                    //Post-Processing Registration for OpeComped Lot
                    postTaskRegisterReqResult2 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                }
                log.info("ForceOpeComp and Post Process Registration :rc == RC_OK");
                Response postTaskExecuteReqResult1;    // for OpeComped Lot
                Response postTaskExecuteReqResult2;   // for Released Lot

                int PostProcParallelFlag = StandardProperties.OM_PP_PARALLEL_ENABLE.getIntValue();
                if (0 == PostProcParallelFlag) {
                    if (PostProcForLotFlag != 1) {
                        postTaskExecuteReqResult1 = postController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(user,
                                postTaskRegisterReqResult1.getDKey(), 1, 0, null, ""));
                        postTaskExecuteReqResult2 = postController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(user,
                                postTaskRegisterReqResult2.getDKey(), 1, 0, null, ""));
                    } else {
                        postTaskExecuteReqResult1 = postController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(user,
                                postTaskRegisterReqResult1.getDKey(), 1, 0, null, ""));

                        // Set strLotSpecSPCCheckResultSeq
                        Results.PostTaskExecuteReqResult postTaskExecuteReqRes = (Results.PostTaskExecuteReqResult) postTaskExecuteReqResult1.getBody();
                        result.setStrOpeCompLot(postTaskExecuteReqRes.getStrLotSpecSPCCheckResultSeq());
                        //Post Process Execution for Released Lot
                        postTaskExecuteReqResult1= postController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(user,
                                postTaskExecuteReqRes.getRelatedQueueKey(), 1, 0, null, ""));
                    }
                } else {
                    //TODO-NOTIMPL: postProcessQueue_parallelExec_Check
                    //TODO-NOTIMPL: TxPostProcessParallelExecReq
                }
            } catch (ServiceException e) {
                // TODO :txAPCControlJobInfoRpt
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/force_move_out/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.FORCE__OPE_COMP_REQ)
    @EnablePostProcess(proxy = ForceMoveOutPostProcessProxy.class)
    public Response forceMoveOutReq(@RequestBody Params.ForceMoveOutReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.FORCE__OPE_COMP_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        Validations.check(null == user, "The user info is null");

        //【step1】calendar_GetCurrentTimeDR
        if (log.isInfoEnabled()) log.info("【step1】calendar_GetCurrentTimeDR");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //【step2】 controlJob_lotIDList_GetDR
        if (log.isInfoEnabled()) log.info("【step2】controlJobLotIDListGetDR");
        List<ObjectIdentifier> lotIds = Collections.emptyList();
        int accessCheckForCjIntValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getIntValue();
        if (log.isInfoEnabled()) log.info("Env OM_ACCESS_CHECK_FOR_CJ = {}", accessCheckForCjIntValue);
        if (accessCheckForCjIntValue == 1) {
            //Step2 - controlJob_lotIDList_GetDR
            if (log.isInfoEnabled()) log.info("Call controlJobLotIDListGetDR(...)");
            lotIds = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobId());
        } else {
            if (log.isInfoEnabled()) log.info("OM_ACCESS_CHECK_FOR_CJ OFF. Do Nothing...");
        }

        //【step3】txAccessControlCheckInq
        if (log.isInfoEnabled()) log.info("【step3】 sxAccessControlCheckInq");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIds);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentId());
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //-----------------------
        // Main Process
        //-----------------------
        //【step4】call txForceMoveOutReq
        if (log.isInfoEnabled()) log.info("【step4】 sxForceMoveOutReq()...");
        Results.ForceMoveOutReqResult result = equipmentProcessOperation.sxForceMoveOutReq(objCommon,
                params.getEquipmentId(),
                params.getControlJobId(),
                params.getSpcResultRequiredFlag(),
                params.getClaimMemo());

        // todo:: txAPCControlJobInfoRpt

        return Response.createSucc(transactionID, result);
        //-------------------------------------
        // Post-Processing Section
        //-------------------------------------
    }


    @ResponseBody
    @RequestMapping(value = "/carrier_move_from_ib/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MOVE_CASSETTE_FROM_INTERNAL_BUFFER_RPT)
    public Response carrierMoveFromIBRpt(@RequestBody Params.CarrierMoveFromIBRptParams params) {
        String txId = TransactionIDEnum.MOVE_CASSETTE_FROM_INTERNAL_BUFFER_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = params.getUser();
        Validations.check(null == user, new ErrorCode("user can not be null"));

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        Infos.LotListInCassetteInfo lotIDs = null;
        String tmpPrivilegeCheckCASTValue =StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();

        if (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1")) {
            // step2 - cassette_lotIDList_GetDR
            lotIDs = cassetteMethod.cassetteLotIDListGetDR(objCommon, params.getCarrierID());
        }

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams();
        privilegeCheckParams.setEquipmentID(params.getEquipmentID());
        privilegeCheckParams.setLotIDLists(lotIDs==null?null:lotIDs.getLotIDList());
        accessInqService.sxAccessControlCheckInq(objCommon, privilegeCheckParams);

        //Step3 - txCarrierMoveFromIBRpt
        equipmentService.sxCarrierMoveFromIBRpt(objCommon, params);

        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/running_hold/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RUNNING_HOLD_REQ)
    public Response runningHoldReq(@RequestBody Params.RunningHoldReqParams params) {

        //【step0】init params
        final String transactionID = TransactionIDEnum.RUNNING_HOLD_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        Validations.check(null == user, "the user info is null");
        RetCode<Object> result = new RetCode<>();

        //【step0】Incoming Log Put
        log.info(" Incoming = {}", transactionID);

        //【step1】calendar_GetCurrentTimeDR
        log.info("【step1】calendar_GetCurrentTimeDR");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //【step2】 controlJob_lotIDList_GetDR
        log.info("【step2】controlJob_lotIDList_GetDR");
        List<ObjectIdentifier> lotIds = new ArrayList<>();
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        log.info("env value of OM_ACCESS_CHECK_FOR_CJ  = ", tmpPrivilegeCheckCJValue);
        if ((CimStringUtils.isNotEmpty(tmpPrivilegeCheckCJValue)) && CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1")) {
            //Step2 - controlJob_lotIDList_GetDR
            log.info("Call controlJob_lotIDList_GetDR");
            lotIds = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        } else {
            log.info("OM_ACCESS_CHECK_FOR_CJ OFF Do Nothing.");
        }

        //【step3】txAccessControlCheckInq
        log.info("【step3】 txAccessControlCheckInq");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIds);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //【step4】 call txRunningHoldReq
        log.info("【step4】 call txRunningHoldReq");
        equipmentService.sxRunningHoldReq(objCommon, params.getEquipmentID(), params.getControlJobID(), params.getHoldReasonCodeID(), params.getOpeMemo());
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_loading_for_ib/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOADING_LOT_FOR_INTERNAL_BUFFER_RPT)
    public Response carrierLoadingForIBRpt(@RequestBody Params.CarrierLoadingForIBRptParams carrierLoadingForIBRptParams) {

        final String transactionID = TransactionIDEnum.LOADING_LOT_FOR_INTERNAL_BUFFER_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = carrierLoadingForIBRptParams.getUser();
        Validations.check(null == user, new ErrorCode("user can not be null"));

        List<Infos.LoadingVerifiedLot> retVal = new ArrayList<>();

        log.debug("step1 - calendar_GetCurrentTimeDR get schedule from calendar");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        Infos.LotListInCassetteInfo lotIDs = null;
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        if (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1")) {
            log.debug("step2 - cassette lot ID List GetDR");
            lotIDs = cassetteMethod.cassetteLotIDListGetDR(objCommon, carrierLoadingForIBRptParams.getCassetteID());
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(carrierLoadingForIBRptParams.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs==null?null:lotIDs.getLotIDList());
        log.debug("step3 - access control check");
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        log.debug("step4 - sxCarrierLoadingForIBRpt");
        try {
            retVal = equipmentService.sxCarrierLoadingForIBRpt(objCommon, carrierLoadingForIBRptParams.getEquipmentID(), carrierLoadingForIBRptParams.getCassetteID(),
                    carrierLoadingForIBRptParams.getPortID(), carrierLoadingForIBRptParams.getLoadPurposeType(), carrierLoadingForIBRptParams.getOpeMemo());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getCastForceLoaded(), e.getCode())) {
                return Response.createSucc(transactionID, retVal);
            } else {
                throw e;
            }
        }

        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_move_to_ib/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MOVE_CASSETTE_TO_INTERNAL_BUFFER_RPT)
    public Response carrierMoveToIBRpt(@RequestBody Params.CarrierMoveToIBRptParams carrierMoveToIBRptParams) {

        final String transactionID = TransactionIDEnum.MOVE_CASSETTE_TO_INTERNAL_BUFFER_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        User user = carrierMoveToIBRptParams.getUser();
        Validations.check(null == user, new ErrorCode("user can not be null"));

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        Infos.LotListInCassetteInfo lotIDs = null;
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();

        if (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1")) {
            // step2 - cassette_lotIDList_GetDR
            lotIDs = cassetteMethod.cassetteLotIDListGetDR(objCommon, carrierMoveToIBRptParams.getCarrierID());
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIDs==null?null:lotIDs.getLotIDList());
        accessControlCheckInqParams.setEquipmentID(carrierMoveToIBRptParams.getEquipmentID());
        // step3 - txAccessControlCheckInq
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        // step4 - txCarrierMoveToIBRpt
        equipmentService.sxCarrierMoveToIBRpt(objCommon, carrierMoveToIBRptParams.getEquipmentID(),
                carrierMoveToIBRptParams.getLoadedPortID(), carrierMoveToIBRptParams.getCarrierID(), carrierMoveToIBRptParams.getOpeMemo());

        return Response.createSucc(transactionID, null);

    }

    @ResponseBody
    @RequestMapping(value = "/carrier_unloading_for_ib/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.UN_LOADING_LOT_FOR_INTERNAL_BUFFER_RPT)
    public Response uncarrierLoadingForIBRpt(@RequestBody Params.CarrierUnloadingForIBRptParams uncarrierLoadingForIBRptParams) {
        final String transactionID = TransactionIDEnum.UN_LOADING_LOT_FOR_INTERNAL_BUFFER_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        // step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(uncarrierLoadingForIBRptParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, uncarrierLoadingForIBRptParams.getUser(),
                accessControlCheckInqParams);

        // step3 - txCarrierUnloadingForIBRpt
        equipmentService.sxCarrierUnloadingForIBRpt(objCommon, uncarrierLoadingForIBRptParams.getEquipmentID(), uncarrierLoadingForIBRptParams.getCassetteID(),
                uncarrierLoadingForIBRptParams.getPortID(), uncarrierLoadingForIBRptParams.getOpeMemo());

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_loading_verify_for_ib/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_VERIFY_FOR_LOADING_FOR_INTERNAL_BUFFER_REQ)
    public Response carrierLoadingVerifyForIBReq(@RequestBody Params.CarrierLoadingVerifyForIBReqParams params) {
        log.info("CarrierLoadingVerifyForIBReqController::carrierLoadingVerifyForIBReq()");
        //init params
        final String transactionID = TransactionIDEnum.LOT_VERIFY_FOR_LOADING_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.info("【step0】check input params");
        User user = params.getUser();
        Validations.check(null == user, "the user info is null...");
        ObjectIdentifier cassetteID = params.getCassetteID();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        Validations.check(ObjectIdentifier.isEmpty(cassetteID) || ObjectIdentifier.isEmpty(equipmentID), "invalid input parameter");

        log.info("【step1】get schedule from calendar");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step2】 cassetteLotIDListGetDR");
        Infos.LotListInCassetteInfo lotIDs = null;
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue) && CimStringUtils.equals("1", tmpPrivilegeCheckCASTValue)) {
            lotIDs = cassetteMethod.cassetteLotIDListGetDR(null, cassetteID);
        }
        Validations.check(CimObjectUtils.isEmpty(lotIDs), "cassetteLotIDs is null");

        log.info("【step3】call txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIDs.getLotIDList());
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        log.info("【step4】call sxCarrierLoadingVerifyForIBReq(...)");
        Results.CarrierLoadingVerifyReqResult result = equipmentService.sxCarrierLoadingVerifyForIBReq(objCommon, equipmentID,
                params.getPortID(), cassetteID, params.getLoadPurposeType());
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/old/move_out_for_ib/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.OPE_COMP_FOR_INTERNAL_BUFFER_REQ)
    public Response moveOutForIBReqOld(@RequestBody Params.MoveOutForIBReqParams params) {
        TransactionIDEnum transactionID = TransactionIDEnum.OPE_COMP_FOR_INTERNAL_BUFFER_REQ;

        ThreadContextHolder.setTransactionId(transactionID.getValue());
        User user = params.getUser();

        //check input params
        Validations.check(null == user, "the user info is null...");

        log.debug("step1 - get schedule from calendar");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.debug("step2 - get lot id list from control job");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue) && CimStringUtils.equals(BizConstant.CONSTANT_QUANTITY_ONE, tmpPrivilegeCheckCJValue)) {
            lotIDLists = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);
        //lotIDLists.stream().forEach(lotID -> qTimeMethod.checkMMQTime(lotID, true));

        log.debug("step3 - main process");
        Params.MoveOutForIBReqExtraParams extraParams = new Params.MoveOutForIBReqExtraParams();
        final Results.MoveOutReqResult result = equipmentProcessOperation.sxMoveOutForIBReq(objCommon, params);

        Results.PostTaskRegisterReqResult strPostTaskRegisterReqResult = null;
        Results.PostTaskRegisterReqResult strPostTaskRegisterReqResult2 = null;
        String postProcessForLotFlag = StandardProperties.OM_PP_FOR_LOT_MOVEOUT_FLAG.getValue();
        int postProcForLotFlag = Integer.parseInt(postProcessForLotFlag);
        String opeCompedTxID;
        if (postProcForLotFlag == -1) {
            String strPostProcForLotFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCFORLOTFLAG);
            if (CimStringUtils.equals(strPostProcForLotFlag, "1")) {
                postProcForLotFlag = 1;
            } else {
                postProcForLotFlag = 0;
            }
        }
        if (postProcForLotFlag != 1) {
            opeCompedTxID = "OEQPW008";
        } else {
            opeCompedTxID = "TXTRC_55";
        }
        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        ArrayList<ObjectIdentifier> opeCompedLotIDs = new ArrayList<>();
        result.getMoveOutLot().forEach(opeCompLot -> opeCompedLotIDs.add(opeCompLot.getLotID()));

        Infos.PostProcessRegistrationParam strPostProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
        strPostProcessRegistrationParm.setLotIDs(opeCompedLotIDs);
        strPostProcessRegistrationParm.setEquipmentID(params.getEquipmentID());
        strPostProcessRegistrationParm.setControlJobID(params.getControlJobID());
        Params.PostTaskRegisterReqParams postProcessRegistrationParam = new Params.PostTaskRegisterReqParams();
        postProcessRegistrationParam.setPostProcessRegistrationParm(strPostProcessRegistrationParm);
        postProcessRegistrationParam.setTransactionID(opeCompedTxID);
        postProcessRegistrationParam.setPatternID(null);
        postProcessRegistrationParam.setKey(null);
        postProcessRegistrationParam.setSequenceNumber(-1);
        strPostTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postProcessRegistrationParam);
        //--------------------------------------------------
        // Post-Processing for OpeCompedLot Update Section
        //--------------------------------------------------
        List<Infos.PostProcessAdditionalInfo> strPostProcessAdditionalInfoSeq = new ArrayList<>();
        for (Infos.PostProcessActionInfo postProcessActionInfo : strPostTaskRegisterReqResult.getPostProcessActionInfoList()) {
            if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_QTIME)) {
                if (postProcForLotFlag == 1) {
                    Outputs.ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout objLotPostProcessPreviousBranchAndReturnInfoGetDRout = null;
                    try {
                        objLotPostProcessPreviousBranchAndReturnInfoGetDRout =
                                lotMethod.lotPostProcessPreviousBranchAndReturnInfoGetDR(objCommon, postProcessActionInfo.getPostProcessTargetObject().getLotID());
                    } catch (Exception e) {
                        break;
                    }
                    if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(objLotPostProcessPreviousBranchAndReturnInfoGetDRout.getBranchRouteID()))) {
                        List<String> strings = new ArrayList<>();
                        strings.add(objLotPostProcessPreviousBranchAndReturnInfoGetDRout.getBranchRouteID().getValue());
                        strings.add(objLotPostProcessPreviousBranchAndReturnInfoGetDRout.getBranchOperationNumber());
                        String previousBranchInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                        Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                        postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                        postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                        postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);
                        postProcessAdditionalInfo.setValue(previousBranchInfo);
                        strPostProcessAdditionalInfoSeq.add(postProcessAdditionalInfo);
                    }
                    if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(objLotPostProcessPreviousBranchAndReturnInfoGetDRout.getReturnRouteID()))) {
                        List<String> strings = new ArrayList<>();
                        strings.add(objLotPostProcessPreviousBranchAndReturnInfoGetDRout.getReturnRouteID().getValue());
                        strings.add(objLotPostProcessPreviousBranchAndReturnInfoGetDRout.getReturnOperationNumber());
                        String previousReturnInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                        Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                        postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                        postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                        postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);
                        postProcessAdditionalInfo.setValue(previousReturnInfo);
                        strPostProcessAdditionalInfoSeq.add(postProcessAdditionalInfo);
                    }
                    if (CimStringUtils.isNotEmpty(objLotPostProcessPreviousBranchAndReturnInfoGetDRout.getReworkOutKey())) {
                        Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                        postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                        postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                        postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);
                        postProcessAdditionalInfo.setValue(objLotPostProcessPreviousBranchAndReturnInfoGetDRout.getReworkOutKey());
                        strPostProcessAdditionalInfoSeq.add(postProcessAdditionalInfo);
                    }
                }
            }
        }
        if (CimArrayUtils.getSize(strPostProcessAdditionalInfoSeq) > 0) {
            // txPostActionModifyReq__130
            List<Infos.PostProcessActionInfo> postProcessActionUpdateInfoList = postService.sxPostActionModifyReq(objCommon,
                    BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                    strPostTaskRegisterReqResult.getPostProcessActionInfoList(), strPostProcessAdditionalInfoSeq, params.getOpeMemo());
        }
        if (CimArrayUtils.isNotEmpty(extraParams.getHoldReleasedLotIDs())) {
            Infos.PostProcessRegistrationParam strPostProcessRegistrationParm2 = new Infos.PostProcessRegistrationParam();
            strPostProcessRegistrationParm2.setLotIDs(extraParams.getHoldReleasedLotIDs());
            strPostProcessRegistrationParm2.setEquipmentID(params.getEquipmentID());
            strPostProcessRegistrationParm2.setControlJobID(params.getControlJobID());
            Params.PostTaskRegisterReqParams postProcessRegistrationParam2 = new Params.PostTaskRegisterReqParams();
            postProcessRegistrationParam2.setPostProcessRegistrationParm(strPostProcessRegistrationParm2);
            postProcessRegistrationParam2.setTransactionID("ThTRC055");
            postProcessRegistrationParam2.setPatternID(null);
            postProcessRegistrationParam2.setKey(null);
            postProcessRegistrationParam2.setSequenceNumber(-1);
            strPostTaskRegisterReqResult2 = postService.sxPostTaskRegisterReq(objCommon, postProcessRegistrationParam2);
        }
        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        int postProcParallelFlag = StandardProperties.OM_PP_PARALLEL_ENABLE.getIntValue();
        if (postProcParallelFlag == 0) {
            Params.PostTaskExecuteReqParams postTaskExecuteReqParams1 = new Params.PostTaskExecuteReqParams();
            postTaskExecuteReqParams1.setUser(params.getUser());
            postTaskExecuteReqParams1.setKey(strPostTaskRegisterReqResult.getDKey());
            postTaskExecuteReqParams1.setSyncFlag(1);
            postTaskExecuteReqParams1.setPreviousSequenceNumber(0);
            Response postTaskExecuteReqResponse1 = postController.postTaskExecuteReq(postTaskExecuteReqParams1);
            //Post Process Execution for Released Lot
            if (postProcForLotFlag != 1) {
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams2 = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams2.setUser(params.getUser());
                postTaskExecuteReqParams2.setKey(strPostTaskRegisterReqResult2 == null ? null : strPostTaskRegisterReqResult2.getDKey());
                postTaskExecuteReqParams2.setSyncFlag(1);
                postTaskExecuteReqParams2.setPreviousSequenceNumber(0);
                postController.postTaskExecuteReq(postTaskExecuteReqParams2);
            } else {
                // Set strLotSpecSPCCheckResultSeq (for OpeCompedLot)
                Results.PostTaskExecuteReqResult postTaskExecuteReqResult = (Results.PostTaskExecuteReqResult) postTaskExecuteReqResponse1.getBody();
                result.setMoveOutLot(postTaskExecuteReqResult.getStrLotSpecSPCCheckResultSeq());
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams3 = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams3.setUser(params.getUser());
                postTaskExecuteReqParams3.setKey(postTaskExecuteReqResult.getRelatedQueueKey());
                postTaskExecuteReqParams3.setSyncFlag(1);
                postTaskExecuteReqParams3.setPreviousSequenceNumber(0);
                postController.postTaskExecuteReq(postTaskExecuteReqParams3);
            }
        } else {
            // TODO parallelExec相关
        }

        return Response.createSucc(transactionID.getValue(), result.getMoveOutLot());
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/move_out_for_ib/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.OPE_COMP_FOR_INTERNAL_BUFFER_REQ)
    @EnablePostProcess(proxy = MoveOutForIBPostProcessProxy.class)
    public Response moveOutForIBReq(@RequestBody Params.MoveOutForIBReqParams params) {
        // transaction id
        TransactionIDEnum transactionID = TransactionIDEnum.OPE_COMP_FOR_INTERNAL_BUFFER_REQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //check input params
        User user = params.getUser();
        Validations.check(null == user, "the user info is null...");

        log.debug("step1 - get schedule from calendar");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.debug("step2 - get lot id list from control job");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        if (CimStringUtils.isNotEmpty(tmpPrivilegeCheckCJValue)
                && CimStringUtils.equals(BizConstant.CONSTANT_QUANTITY_ONE, tmpPrivilegeCheckCJValue)) {
            lotIDLists = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //-------------------------------------
        // Main Process
        //-------------------------------------
        log.debug("step3 - main process");
        Results.MoveOutReqResult result = equipmentProcessOperation.sxMoveOutForIBReq(objCommon, params);

        return Response.createSucc(transactionID.getValue(), result);
        //--------------------------------------------------
        // Post-Processing
        //--------------------------------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/move_out_with_running_split/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ)
    public Response partialMoveOutReqOld(@RequestBody Params.PartialMoveOutReqParams partialMoveOutReqParams) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step 1 get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, partialMoveOutReqParams.getUser());

        //step 2
        // ----------------------------------------------------------------
        // ---- PrivilegeCheck for equipmentID
        // ----------------------------------------------------------------
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        List<ObjectIdentifier> lotIDs = null;
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1"))) {
            // step2 - cassette_lotIDList_GetDR
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, partialMoveOutReqParams.getControlJobID());
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(partialMoveOutReqParams.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //step 3
        String APCIFControlStatus = null;
        String DCSIFControlStatus = null;
        Results.PartialMoveOutReqResult result = equipmentProcessOperation.sxPartialMoveOutReq(objCommon, partialMoveOutReqParams,
                 APCIFControlStatus, DCSIFControlStatus);
        int PostProcForLotFlag = StandardProperties.OM_PP_FOR_LOT_MOVEOUT_FLAG.getIntValue();
        if (-1 == PostProcForLotFlag) {
            String strPostProcForLotFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCFORLOTFLAG);
            PostProcForLotFlag = CimStringUtils.equals(strPostProcForLotFlag, "1") ? 1 : 0;
        }
        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        List<Infos.PartialOpeCompLot> partialOpeCompLotList = result.getPartialOpeCompLotList();
        boolean bSucceedFlag = false;
        Results.PostTaskRegisterReqResult postTaskRegisterReqResultRetCode1 = null;
        Results.PostTaskRegisterReqResult postTaskRegisterReqResultRetCode2 = null;
        Results.PostTaskRegisterReqResult postTaskRegisterReqResultRetCode3 = null;
        Results.PostTaskRegisterReqResult postTaskRegisterReqResultRetCode4 = null;
        Results.PostTaskRegisterReqResult postTaskRegisterReqResultRetCode5 = null;
        bSucceedFlag = true;
        String tmpTxID = null;
        //For OperationCompletion lot (PostProcess)
        List<ObjectIdentifier> opeCompedLotIDs = new ArrayList<>();
        for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
            if (CimStringUtils.equals(partialOpeCompLot.getActionCode(), BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP)) {
                opeCompedLotIDs.add(partialOpeCompLot.getLotID());
            }
        }
        if (!CimObjectUtils.isEmpty(opeCompedLotIDs)) {
            if (PostProcForLotFlag != 1) {
                tmpTxID = TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue();
            } else {
                tmpTxID = "TXEWC_15";
            }
            Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
            postProcessRegistrationParam.setLotIDs(opeCompedLotIDs);
            postProcessRegistrationParam.setEquipmentID(partialMoveOutReqParams.getEquipmentID());
            postProcessRegistrationParam.setControlJobID(partialMoveOutReqParams.getControlJobID());
            Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
            postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
            postTaskRegisterReqParams.setTransactionID(tmpTxID);
            postTaskRegisterReqParams.setPatternID(null);
            postTaskRegisterReqParams.setKey(null);
            postTaskRegisterReqParams.setSequenceNumber(-1);
            //Post-Processing Registration for OpeComped Lot
            try {
                postTaskRegisterReqResultRetCode1 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
            } catch (Exception e) {
                bSucceedFlag = false;
            }

            //--------------------------------------------------
            // Post-Processing for OpeCompWithHoldLot Update Section
            //--------------------------------------------------
            List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList = new ArrayList<>();
            if (!CimObjectUtils.isEmpty(postTaskRegisterReqResultRetCode1)) {
                List<Infos.PostProcessActionInfo> postProcessActionInfoList = postTaskRegisterReqResultRetCode1.getPostProcessActionInfoList();
                for (Infos.PostProcessActionInfo postProcessActionInfo : postProcessActionInfoList) {
                    if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_QTIME)) {
                        if (1 == PostProcForLotFlag) {
                            Outputs.ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout getDRoutRetCode = null;
                            try {
                                getDRoutRetCode = lotMethod.lotPostProcessPreviousBranchAndReturnInfoGetDR(objCommon,
                                        postProcessActionInfo.getPostProcessTargetObject().getLotID());
                            } catch (ServiceException e) {
                                bSucceedFlag = false;
                                break;
                            }

                            if (!ObjectIdentifier.isEmptyWithValue(getDRoutRetCode.getBranchRouteID())) {
                                List<String> strings = new ArrayList<>();
                                strings.add(getDRoutRetCode.getBranchRouteID().getValue());
                                strings.add(getDRoutRetCode.getBranchOperationNumber());
                                String previousBranchInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);
                                postProcessAdditionalInfo.setValue(previousBranchInfo);
                            }
                            if (!ObjectIdentifier.isEmptyWithValue(getDRoutRetCode.getReturnRouteID())) {
                                List<String> strings = new ArrayList<>();
                                strings.add(getDRoutRetCode.getReturnRouteID().getValue());
                                strings.add(getDRoutRetCode.getReturnOperationNumber());
                                String previousReturnInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);
                                postProcessAdditionalInfo.setValue(previousReturnInfo);
                            }
                            if (!CimStringUtils.isEmpty(getDRoutRetCode.getReworkOutKey())) {
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);
                                postProcessAdditionalInfo.setValue(getDRoutRetCode.getReworkOutKey());
                            }
                        }
                    }
                }
            }


            if (!CimObjectUtils.isEmpty(postProcessAdditionalInfoList)) {
                postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                        postTaskRegisterReqResultRetCode1.getPostProcessActionInfoList(), postProcessAdditionalInfoList, partialMoveOutReqParams.getClaimMemo());
            }


        }
        if (bSucceedFlag) {
            //For OperationCompletionWithHold lot (PostProcess)
            List<ObjectIdentifier> opeCompedWithHoldLotIDs = new ArrayList<>();
            for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                if (CimStringUtils.equals(partialOpeCompLot.getActionCode(), BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD)) {
                    opeCompedWithHoldLotIDs.add(partialOpeCompLot.getLotID());
                }
            }
            if (!CimObjectUtils.isEmpty(opeCompedWithHoldLotIDs)) {
                if (PostProcForLotFlag != 1) {
                    tmpTxID = "T1EWC015";
                } else {
                    tmpTxID = "T1EWC_15";
                }
                Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                postProcessRegistrationParam.setLotIDs(opeCompedWithHoldLotIDs);
                postProcessRegistrationParam.setEquipmentID(partialMoveOutReqParams.getEquipmentID());
                postProcessRegistrationParam.setControlJobID(partialMoveOutReqParams.getControlJobID());
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
                postTaskRegisterReqParams.setTransactionID(tmpTxID);
                postTaskRegisterReqParams.setPatternID(null);
                postTaskRegisterReqParams.setKey(null);
                postTaskRegisterReqParams.setSequenceNumber(-1);
                postTaskRegisterReqParams.setClaimMemo(partialMoveOutReqParams.getClaimMemo());
                try {
                    postTaskRegisterReqResultRetCode2 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                } catch (Exception e) {
                    bSucceedFlag = false;
                }

                //--------------------------------------------------
                // Post-Processing for OpeCompWithHoldLot Update Section
                //--------------------------------------------------
                List<Infos.PostProcessActionInfo> postProcessActionInfoList = postTaskRegisterReqResultRetCode2.getPostProcessActionInfoList();
                List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList = new ArrayList<>();
                for (Infos.PostProcessActionInfo postProcessActionInfo : postProcessActionInfoList) {
                    if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_QTIME)) {
                        if (1 == PostProcForLotFlag) {
                            log.info("lot_postProcessPreviousBranchAndReturnInfo_GetDR__150");
                            Outputs.ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout getDRoutRetCode = null;
                            try {
                                getDRoutRetCode = lotMethod.lotPostProcessPreviousBranchAndReturnInfoGetDR(objCommon, postProcessActionInfo.getPostProcessTargetObject().getLotID());
                            } catch (Exception e) {
                                bSucceedFlag = false;
                                break;
                            }
                            if (!ObjectIdentifier.isEmptyWithValue(getDRoutRetCode.getBranchRouteID())) {
                                List<String> strings = new ArrayList<>();
                                strings.add(getDRoutRetCode.getBranchRouteID().getValue());
                                strings.add(getDRoutRetCode.getBranchOperationNumber());
                                String previousBranchInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);
                                postProcessAdditionalInfo.setValue(previousBranchInfo);
                            }
                            if (!ObjectIdentifier.isEmptyWithValue(getDRoutRetCode.getReturnRouteID())) {
                                List<String> strings = new ArrayList<>();
                                strings.add(getDRoutRetCode.getReturnRouteID().getValue());
                                strings.add(getDRoutRetCode.getReturnOperationNumber());
                                String previousReturnInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);
                                postProcessAdditionalInfo.setValue(previousReturnInfo);
                            }
                            if (!CimStringUtils.isEmpty(getDRoutRetCode.getReworkOutKey())) {
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);
                                postProcessAdditionalInfo.setValue(getDRoutRetCode.getReworkOutKey());
                            }
                        }
                    }
                }
                if (!CimObjectUtils.isEmpty(postProcessAdditionalInfoList)) {
                    postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                            postTaskRegisterReqResultRetCode2.getPostProcessActionInfoList(), postProcessAdditionalInfoList, partialMoveOutReqParams.getClaimMemo());
                }

            }
        }

        int opeStartCancelLotCnt = 0;
        int runWaferUpdateByPostProc = StandardProperties.OM_PP_UPDATE_FOR_EQP_ATTR.getIntValue();

        if (bSucceedFlag) {
            List<ObjectIdentifier> opeStartCancelLotIDs = new ArrayList<>();
            for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                if (CimStringUtils.equals(partialOpeCompLot.getActionCode(), BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCEL)) {
                    opeStartCancelLotIDs.add(partialOpeCompLot.getLotID());
                    opeStartCancelLotCnt++;
                }
            }
            if (!CimObjectUtils.isEmpty(opeStartCancelLotIDs)) {
                tmpTxID = "T2EWC015";
                Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                postProcessRegistrationParam.setLotIDs(opeStartCancelLotIDs);
                postProcessRegistrationParam.setEquipmentID(partialMoveOutReqParams.getEquipmentID());
                postProcessRegistrationParam.setControlJobID(partialMoveOutReqParams.getControlJobID());
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
                postTaskRegisterReqParams.setTransactionID(tmpTxID);
                postTaskRegisterReqParams.setKey(null);
                postTaskRegisterReqParams.setPatternID(null);
                postTaskRegisterReqParams.setSequenceNumber(-1);
                try {
                    postTaskRegisterReqResultRetCode3 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                } catch (Exception e) {
                    bSucceedFlag = false;
                }
            }
        }
        if (bSucceedFlag) {
            List<ObjectIdentifier> opeStartCancelWithHoldLotIDs = new ArrayList<>();
            for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                if (CimStringUtils.equals(partialOpeCompLot.getActionCode(), BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCELWITHHOLD)) {
                    opeStartCancelWithHoldLotIDs.add(partialOpeCompLot.getLotID());
                    opeStartCancelLotCnt++;
                }
            }
            if (!CimObjectUtils.isEmpty(opeStartCancelWithHoldLotIDs)) {
                tmpTxID = "T3EWC015";
                Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                postProcessRegistrationParam.setLotIDs(opeStartCancelWithHoldLotIDs);
                postProcessRegistrationParam.setEquipmentID(partialMoveOutReqParams.getEquipmentID());
                postProcessRegistrationParam.setControlJobID(partialMoveOutReqParams.getControlJobID());
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
                postTaskRegisterReqParams.setTransactionID(tmpTxID);
                postTaskRegisterReqParams.setKey(null);
                postTaskRegisterReqParams.setPatternID(null);
                postTaskRegisterReqParams.setSequenceNumber(-1);
                try {
                    postTaskRegisterReqResultRetCode4 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                } catch (ServiceException e) {
                    bSucceedFlag = false;
                }
            }
        }
        if (bSucceedFlag) {
            //----------------------------------
            // Post-Processing Update Section
            //----------------------------------
            if (runWaferUpdateByPostProc == 1 && opeStartCancelLotCnt > 0) {
                String strRunWaferCnt = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT);
                boolean additionalInfoFlag = false;
                //  Update postProcessQueue
                List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList = new ArrayList<>();
                // opeStartCancelLotCnt > 0, so either opeStartCancel or opeStartCancelWithHold, or both are claimed. We only update once.
                List<Infos.PostProcessActionInfo> postProcessActionInfoList = null;
                if (!CimObjectUtils.isEmpty(postTaskRegisterReqResultRetCode3.getPostProcessActionInfoList())) {
                    postProcessActionInfoList = postTaskRegisterReqResultRetCode3.getPostProcessActionInfoList();
                } else {
                    // Operatoin Start Cancel with hold lot
                    postProcessActionInfoList = postTaskRegisterReqResultRetCode4.getPostProcessActionInfoList();
                }
                for (Infos.PostProcessActionInfo postProcessActionInfo : postProcessActionInfoList) {
                    if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_RUNWAFERINFOUPDATE)) {
                        Infos.PostProcessAdditionalInfo postProcessAdditionalInfo1 = new Infos.PostProcessAdditionalInfo();
                        postProcessAdditionalInfoList.add(postProcessAdditionalInfo1);
                        postProcessAdditionalInfo1.setDKey(postProcessActionInfo.getDKey());
                        postProcessAdditionalInfo1.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                        postProcessAdditionalInfo1.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT);
                        postProcessAdditionalInfo1.setValue(strRunWaferCnt);
                        if (opeStartCancelLotCnt == CimArrayUtils.getSize(partialOpeCompLotList)) {
                            Infos.PostProcessAdditionalInfo postProcessAdditionalInfo2 = new Infos.PostProcessAdditionalInfo();
                            postProcessAdditionalInfoList.add(postProcessAdditionalInfo2);
                            postProcessAdditionalInfo2.setDKey(postProcessActionInfo.getDKey());
                            postProcessAdditionalInfo2.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                            postProcessAdditionalInfo2.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_OPESTARTCNT);
                            postProcessAdditionalInfo2.setValue("1");
                        }
                        additionalInfoFlag = true;
                        break;
                    }
                }
                if (additionalInfoFlag) {
                    postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                            postTaskRegisterReqResultRetCode3.getPostProcessActionInfoList(), postProcessAdditionalInfoList,
                            partialMoveOutReqParams.getClaimMemo());
                }
            }
        }
        if (bSucceedFlag) {
            if (PostProcForLotFlag != 1) {
                tmpTxID = "ThEWC015";
                if (!CimObjectUtils.isEmpty(result.getHoldReleasedLotIDs())) {
                    Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                    postProcessRegistrationParam.setLotIDs(result.getHoldReleasedLotIDs());
                    postProcessRegistrationParam.setEquipmentID(partialMoveOutReqParams.getEquipmentID());
                    postProcessRegistrationParam.setControlJobID(partialMoveOutReqParams.getControlJobID());
                    Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                    postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
                    postTaskRegisterReqParams.setTransactionID(tmpTxID);
                    postTaskRegisterReqParams.setKey(null);
                    postTaskRegisterReqParams.setPatternID(null);
                    postTaskRegisterReqParams.setSequenceNumber(-1);
                    try {
                        postTaskRegisterReqResultRetCode5 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                    } catch (ServiceException e) {
                        bSucceedFlag = false;
                    }
                }
            }
        }

        List<Params.PostTaskExecuteReqParams> params = new ArrayList<>();

        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        if (bSucceedFlag) {
            Integer tmpResult1 = null;
            Integer tmpResult2 = null;
            Integer tmpResult3 = null;
            Integer tmpResult4 = null;
            Integer tmpResult5 = null;
            Integer tmpResult6 = null;
            int PostProcParallelFlag = StandardProperties.OM_PP_PARALLEL_ENABLE.getIntValue();
            if (PostProcParallelFlag == 0) {
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams1 = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams1.setUser(partialMoveOutReqParams.getUser());
                postTaskExecuteReqParams1.setKey(postTaskRegisterReqResultRetCode1 == null ? null : postTaskRegisterReqResultRetCode1.getDKey());
                postTaskExecuteReqParams1.setSyncFlag(1);
                postTaskExecuteReqParams1.setPreviousSequenceNumber(0);
                postTaskExecuteReqParams1.setKeyTimeStamp(null);
                postTaskExecuteReqParams1.setClaimMemo("");
                Response response1 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams1);
                tmpResult1 = response1.getCode();
                if (PostProcForLotFlag != 1) {
                    Params.PostTaskExecuteReqParams postTaskExecuteReqParams5 = new Params.PostTaskExecuteReqParams();
                    postTaskExecuteReqParams5.setUser(partialMoveOutReqParams.getUser());
                    postTaskExecuteReqParams5.setKey(postTaskRegisterReqResultRetCode2 == null ? null : postTaskRegisterReqResultRetCode2.getDKey());
                    postTaskExecuteReqParams5.setSyncFlag(1);
                    postTaskExecuteReqParams5.setPreviousSequenceNumber(0);
                    postTaskExecuteReqParams5.setKeyTimeStamp(null);
                    postTaskExecuteReqParams5.setClaimMemo("");
                    Response response5 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams5);
                    tmpResult5 = response5.getCode();
                } else {
                    List<Infos.OpeCompLot> opeCompLotList = ((Results.PostTaskExecuteReqResult) response1.getBody()).getStrLotSpecSPCCheckResultSeq();
                    for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                        if (!CimArrayUtils.isEmpty(opeCompLotList)){
                            for (Infos.OpeCompLot opeCompLot : opeCompLotList) {
                                if (ObjectIdentifier.equalsWithValue(partialOpeCompLot.getLotID(), opeCompLot.getLotID())) {
                                    // found the lot
                                    partialOpeCompLot.setSpcCheckResult(opeCompLot.getSpecificationCheckResult());
                                    partialOpeCompLot.setSpcCheckResult(opeCompLot.getSpcCheckResult());
                                    partialOpeCompLot.setSpcResult(opeCompLot.getSpcResult());
                                    break;
                                }
                            }
                        }
                    }
                    // Call TxPostTaskExecuteReq__100 for hold released lot
                    Params.PostTaskExecuteReqParams postTaskExecuteReqParams5 = new Params.PostTaskExecuteReqParams();
                    postTaskExecuteReqParams5.setUser(partialMoveOutReqParams.getUser());
                    postTaskExecuteReqParams5.setKey(((Results.PostTaskExecuteReqResult) response1.getBody()).getRelatedQueueKey());
                    postTaskExecuteReqParams5.setSyncFlag(1);
                    postTaskExecuteReqParams5.setPreviousSequenceNumber(0);
                    postTaskExecuteReqParams5.setKeyTimeStamp(null);
                    postTaskExecuteReqParams5.setClaimMemo("");
                    Response response5 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams5);
                    tmpResult5 = response5.getCode();
                }
                //Post Process Execution for OperationCompletionWithHold Lot
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams2 = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams2.setUser(partialMoveOutReqParams.getUser());
                postTaskExecuteReqParams2.setKey(postTaskRegisterReqResultRetCode2 == null ? null : postTaskRegisterReqResultRetCode2.getDKey());
                postTaskExecuteReqParams2.setSyncFlag(1);
                postTaskExecuteReqParams2.setPreviousSequenceNumber(0);
                postTaskExecuteReqParams2.setKeyTimeStamp(null);
                postTaskExecuteReqParams2.setClaimMemo("");
                Response response2 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams2);
                tmpResult2 = response2.getCode();
                if (PostProcForLotFlag == 1) {
                    // Set strLotSpecSPCCheckResultSeq (for OpeCompedLot PostProc)
                    // Create pptOpeCompLotSequence from retVal->strPartialOpeCompLotSeq
                    List<Infos.OpeCompLot> opeCompLotList = ((Results.PostTaskExecuteReqResult) response2.getBody()).getStrLotSpecSPCCheckResultSeq();
                    for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                        if (!CimArrayUtils.isEmpty(opeCompLotList)){
                            for (Infos.OpeCompLot opeCompLot : opeCompLotList) {
                                if (ObjectIdentifier.equalsWithValue(partialOpeCompLot.getLotID(), opeCompLot.getLotID())) {
                                    // found the lot
                                    partialOpeCompLot.setSpcCheckResult(opeCompLot.getSpecificationCheckResult());
                                    partialOpeCompLot.setSpcCheckResult(opeCompLot.getSpcCheckResult());
                                    partialOpeCompLot.setSpcResult(opeCompLot.getSpcResult());
                                    break;
                                }
                            }
                        }

                    }
                    // Call TxPostTaskExecuteReq__100 for hold released lot
                    Params.PostTaskExecuteReqParams postTaskExecuteReqParams6 = new Params.PostTaskExecuteReqParams();
                    postTaskExecuteReqParams6.setUser(partialMoveOutReqParams.getUser());
                    postTaskExecuteReqParams6.setKey(((Results.PostTaskExecuteReqResult) response2.getBody()).getRelatedQueueKey());
                    postTaskExecuteReqParams6.setSyncFlag(1);
                    postTaskExecuteReqParams6.setPreviousSequenceNumber(0);
                    postTaskExecuteReqParams6.setKeyTimeStamp(null);
                    postTaskExecuteReqParams6.setClaimMemo("");
                    Response response6 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams6);
                    tmpResult6 = response6.getCode();
                }
                //Post Process Execution for OperationStartCancel Lot
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams3 = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams3.setUser(partialMoveOutReqParams.getUser());
                postTaskExecuteReqParams3.setKey(postTaskRegisterReqResultRetCode3 == null ? null : postTaskRegisterReqResultRetCode3.getDKey());
                postTaskExecuteReqParams3.setSyncFlag(1);
                postTaskExecuteReqParams3.setPreviousSequenceNumber(0);
                postTaskExecuteReqParams3.setKeyTimeStamp(null);
                postTaskExecuteReqParams3.setClaimMemo("");
                Response response3 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams3);
                tmpResult3 = response3.getCode();
                //Post Process Execution for OperationStartCancelWithHold Lot
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams4 = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams4.setUser(partialMoveOutReqParams.getUser());
                postTaskExecuteReqParams4.setKey(postTaskRegisterReqResultRetCode4 == null ? null : postTaskRegisterReqResultRetCode4.getDKey());
                postTaskExecuteReqParams4.setSyncFlag(1);
                postTaskExecuteReqParams4.setPreviousSequenceNumber(0);
                postTaskExecuteReqParams4.setKeyTimeStamp(null);
                postTaskExecuteReqParams4.setClaimMemo("");
                Response response4 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams4);
                tmpResult4 = response4.getCode();
            } else {
                boolean bPrallelExecFlag = false;
                boolean bOpeCompedLotParallelExec1 = false;
                //----------------------------------
                // OperationCompletion lot
                //----------------------------------
                if (postTaskRegisterReqResultRetCode1 != null && !CimStringUtils.isEmpty(postTaskRegisterReqResultRetCode1.getDKey())) {
                    Inputs.PostProcessQueueParallelExecCheckIn postProcessQueueParallelExecCheckIn = new Inputs.PostProcessQueueParallelExecCheckIn();
                    postProcessQueueParallelExecCheckIn.setDKey("");
                    postProcessQueueParallelExecCheckIn.setPostProcessActionInfoList(postTaskRegisterReqResultRetCode1.getPostProcessActionInfoList());
                    postProcessQueueParallelExecCheckIn.setLotCountCheckFlag(true);
                    log.info("");
                    Outputs.ObjPostProcessQueueParallelExecCheckOut objPostProcessQueueParallelExecCheckOutRetCode1 = postProcessMethod
                            .postProcessQueueParallelExecCheck(objCommon, postProcessQueueParallelExecCheckIn);
                    bPrallelExecFlag = objPostProcessQueueParallelExecCheckOutRetCode1.isParallelExecFlag();
                    if (bPrallelExecFlag) {
                        // TODO  TxPostConcurrentTaskExecuteReq
                    }
                }
            }
        } else {
            // TODO :txAPCCJRpt
        }
        return Response.createSucc(transactionID, result);
    }

    @Override
    @ResponseBody
    @PostMapping(value = "/move_out_with_running_split/req")
    @CimMapping(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ)
    @EnablePostProcess(proxy = PartialMoveOutRequestProxy.class)
    public Response partialMoveOutReq(@RequestBody Params.PartialMoveOutReqParams partialMoveOutReqParams) {
        //-----------------------------------
        // Initialising transactionID
        //-----------------------------------
        if (log.isDebugEnabled()) log.debug("step1 : Initialising transactionID");
        final String transactionID = TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) log.debug("step2 : Privilege check");
        // ------------------------------------
        // Privilege Check
        // ------------------------------------
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, partialMoveOutReqParams.getUser());
        int accessCheckForCjIntValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getIntValue();
        List<ObjectIdentifier> lotIDs = Collections.emptyList();
        if (accessCheckForCjIntValue == 1) {
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, partialMoveOutReqParams.getControlJobID());
        }

        Params.AccessControlCheckInqParams controlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        controlCheckInqParams.setEquipmentID(partialMoveOutReqParams.getEquipmentID());
        controlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, controlCheckInqParams);

        //---------------------------
        // Main Process
        //---------------------------
        if (log.isDebugEnabled()) log.debug("step3 : Main process. call sxPartialMoveOutReq(...)");
        String APCIFControlStatus = null;
        String DCSIFControlStatus = null;
        Results.PartialMoveOutReqResult result = equipmentProcessOperation.sxPartialMoveOutReq(objCommon,
                partialMoveOutReqParams,
                APCIFControlStatus,
                DCSIFControlStatus);

        // todo:: txAPCCJRpt
        return Response.createSucc(transactionID, result);

        //----------------------------------
        // Post-Processing Section
        //----------------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/move_out_with_running_split_for_ib/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ)
    public Response moveOutWithRunningSplitForIBReqOld(@RequestBody Params.PartialMoveOutReqParams params) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step 1 get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, params.getUser());

        //step 2
        // ----------------------------------------------------------------
        // ---- PrivilegeCheck for equipmentID
        // ----------------------------------------------------------------
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        List<ObjectIdentifier> lotIDs = null;
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1"))) {
            // step2 - cassette_lotIDList_GetDR
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //step 3
        String APCIFControlStatus = null;
        String DCSIFControlStatus = null;
        Results.PartialMoveOutReqResult result = equipmentProcessOperation.sxMoveOutWithRunningSplitForIBReq(objCommon, params,
                APCIFControlStatus, DCSIFControlStatus);

        int PostProcForLotFlag = StandardProperties.OM_PP_FOR_LOT_MOVEOUT_FLAG.getIntValue();
        if (-1 == PostProcForLotFlag) {
            String strPostProcForLotFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCFORLOTFLAG);
            PostProcForLotFlag = CimStringUtils.equals(strPostProcForLotFlag, "1") ? 1 : 0;
        }
        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        List<Infos.PartialOpeCompLot> partialOpeCompLotList = result.getPartialOpeCompLotList();
        boolean bSucceedFlag = true;
        Results.PostTaskRegisterReqResult postTaskRegisterReqResultRetCode1 = null;
        Results.PostTaskRegisterReqResult postTaskRegisterReqResultRetCode2 = null;
        Results.PostTaskRegisterReqResult postTaskRegisterReqResultRetCode3 = null;
        Results.PostTaskRegisterReqResult postTaskRegisterReqResultRetCode4 = null;
        Results.PostTaskRegisterReqResult postTaskRegisterReqResultRetCode5 = null;

        String tmpTxID = null;
        //For OperationCompletion lot (PostProcess)
        List<ObjectIdentifier> opeCompedLotIDs = new ArrayList<>();
        for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
            if (CimStringUtils.equals(partialOpeCompLot.getActionCode(), BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP)) {
                opeCompedLotIDs.add(partialOpeCompLot.getLotID());
            }
        }
        if (!CimObjectUtils.isEmpty(opeCompedLotIDs)) {
            if (PostProcForLotFlag != 1) {
                tmpTxID = TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue();
            } else {
                tmpTxID = "TXEWC_16";
            }
            Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
            postProcessRegistrationParam.setLotIDs(opeCompedLotIDs);
            postProcessRegistrationParam.setEquipmentID(params.getEquipmentID());
            postProcessRegistrationParam.setControlJobID(params.getControlJobID());
            Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
            postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
            postTaskRegisterReqParams.setTransactionID(tmpTxID);
            postTaskRegisterReqParams.setPatternID(null);
            postTaskRegisterReqParams.setKey(null);
            postTaskRegisterReqParams.setSequenceNumber(-1);
            //Post-Processing Registration for OpeComped Lot
            try {
                postTaskRegisterReqResultRetCode1 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
            } catch (Exception e) {
                bSucceedFlag = false;
            }

            //--------------------------------------------------
            // Post-Processing for OpeCompWithHoldLot Update Section
            //--------------------------------------------------
            List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList = new ArrayList<>();
            if (!CimObjectUtils.isEmpty(postTaskRegisterReqResultRetCode1)) {
                List<Infos.PostProcessActionInfo> postProcessActionInfoList = postTaskRegisterReqResultRetCode1.getPostProcessActionInfoList();
                for (Infos.PostProcessActionInfo postProcessActionInfo : postProcessActionInfoList) {
                    if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_QTIME)) {
                        if (1 == PostProcForLotFlag) {
                            Outputs.ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout getDRoutRetCode = null;
                            try {
                                getDRoutRetCode = lotMethod.lotPostProcessPreviousBranchAndReturnInfoGetDR(objCommon,
                                        postProcessActionInfo.getPostProcessTargetObject().getLotID());
                            } catch (ServiceException e) {
                                bSucceedFlag = false;
                                break;
                            }

                            if (!ObjectIdentifier.isEmptyWithValue(getDRoutRetCode.getBranchRouteID())) {
                                List<String> strings = new ArrayList<>();
                                strings.add(getDRoutRetCode.getBranchRouteID().getValue());
                                strings.add(getDRoutRetCode.getBranchOperationNumber());
                                String previousBranchInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);
                                postProcessAdditionalInfo.setValue(previousBranchInfo);
                            }
                            if (!ObjectIdentifier.isEmptyWithValue(getDRoutRetCode.getReturnRouteID())) {
                                List<String> strings = new ArrayList<>();
                                strings.add(getDRoutRetCode.getReturnRouteID().getValue());
                                strings.add(getDRoutRetCode.getReturnOperationNumber());
                                String previousReturnInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);
                                postProcessAdditionalInfo.setValue(previousReturnInfo);
                            }
                            if (!CimStringUtils.isEmpty(getDRoutRetCode.getReworkOutKey())) {
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);
                                postProcessAdditionalInfo.setValue(getDRoutRetCode.getReworkOutKey());
                            }
                        }
                    }
                }
            }


            if (!CimObjectUtils.isEmpty(postProcessAdditionalInfoList)) {
                postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                        postTaskRegisterReqResultRetCode1.getPostProcessActionInfoList(), postProcessAdditionalInfoList, params.getClaimMemo());
            }


        }
        if (bSucceedFlag) {
            //For OperationCompletionWithHold lot (PostProcess)
            List<ObjectIdentifier> opeCompedWithHoldLotIDs = new ArrayList<>();
            for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                if (CimStringUtils.equals(partialOpeCompLot.getActionCode(), BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD)) {
                    opeCompedWithHoldLotIDs.add(partialOpeCompLot.getLotID());
                }
            }
            if (!CimObjectUtils.isEmpty(opeCompedWithHoldLotIDs)) {
                if (PostProcForLotFlag != 1) {
                    tmpTxID = "T1EWC015";
                } else {
                    tmpTxID = "T1EWC_15";
                }
                Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                postProcessRegistrationParam.setLotIDs(opeCompedWithHoldLotIDs);
                postProcessRegistrationParam.setEquipmentID(params.getEquipmentID());
                postProcessRegistrationParam.setControlJobID(params.getControlJobID());
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
                postTaskRegisterReqParams.setTransactionID(tmpTxID);
                postTaskRegisterReqParams.setPatternID(null);
                postTaskRegisterReqParams.setKey(null);
                postTaskRegisterReqParams.setSequenceNumber(-1);
                postTaskRegisterReqParams.setClaimMemo(params.getClaimMemo());
                try {
                    postTaskRegisterReqResultRetCode2 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                } catch (Exception e) {
                    bSucceedFlag = false;
                }

                //--------------------------------------------------
                // Post-Processing for OpeCompWithHoldLot Update Section
                //--------------------------------------------------
                List<Infos.PostProcessActionInfo> postProcessActionInfoList = postTaskRegisterReqResultRetCode2.getPostProcessActionInfoList();
                List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList = new ArrayList<>();
                for (Infos.PostProcessActionInfo postProcessActionInfo : postProcessActionInfoList) {
                    if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_QTIME)) {
                        if (1 == PostProcForLotFlag) {
                            log.info("lot_postProcessPreviousBranchAndReturnInfo_GetDR__150");
                            Outputs.ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout getDRoutRetCode = null;
                            try {
                                getDRoutRetCode = lotMethod.lotPostProcessPreviousBranchAndReturnInfoGetDR(objCommon,
                                        postProcessActionInfo.getPostProcessTargetObject().getLotID());
                            } catch (Exception e) {
                                bSucceedFlag = false;
                                break;
                            }
                            if (!ObjectIdentifier.isEmptyWithValue(getDRoutRetCode.getBranchRouteID())) {
                                List<String> strings = new ArrayList<>();
                                strings.add(getDRoutRetCode.getBranchRouteID().getValue());
                                strings.add(getDRoutRetCode.getBranchOperationNumber());
                                String previousBranchInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);
                                postProcessAdditionalInfo.setValue(previousBranchInfo);
                            }
                            if (!ObjectIdentifier.isEmptyWithValue(getDRoutRetCode.getReturnRouteID())) {
                                List<String> strings = new ArrayList<>();
                                strings.add(getDRoutRetCode.getReturnRouteID().getValue());
                                strings.add(getDRoutRetCode.getReturnOperationNumber());
                                String previousReturnInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);
                                postProcessAdditionalInfo.setValue(previousReturnInfo);
                            }
                            if (!CimStringUtils.isEmpty(getDRoutRetCode.getReworkOutKey())) {
                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfoList.add(postProcessAdditionalInfo);
                                postProcessAdditionalInfo.setDKey(postProcessActionInfo.getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);
                                postProcessAdditionalInfo.setValue(getDRoutRetCode.getReworkOutKey());
                            }
                        }
                    }
                }
                if (!CimObjectUtils.isEmpty(postProcessAdditionalInfoList)) {
                    postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                            postTaskRegisterReqResultRetCode2.getPostProcessActionInfoList(), postProcessAdditionalInfoList, params.getClaimMemo());
                }

            }
        }

        int opeStartCancelLotCnt = 0;
        int runWaferUpdateByPostProc = StandardProperties.OM_PP_UPDATE_FOR_EQP_ATTR.getIntValue();

        if (bSucceedFlag) {
            List<ObjectIdentifier> opeStartCancelLotIDs = new ArrayList<>();
            for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                if (CimStringUtils.equals(partialOpeCompLot.getActionCode(), BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCEL)) {
                    opeStartCancelLotIDs.add(partialOpeCompLot.getLotID());
                    opeStartCancelLotCnt++;
                }
            }
            if (!CimObjectUtils.isEmpty(opeStartCancelLotIDs)) {
                tmpTxID = "T2EWC016";
                Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                postProcessRegistrationParam.setLotIDs(opeStartCancelLotIDs);
                postProcessRegistrationParam.setEquipmentID(params.getEquipmentID());
                postProcessRegistrationParam.setControlJobID(params.getControlJobID());
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
                postTaskRegisterReqParams.setTransactionID(tmpTxID);
                postTaskRegisterReqParams.setKey(null);
                postTaskRegisterReqParams.setPatternID(null);
                postTaskRegisterReqParams.setSequenceNumber(-1);
                try {
                    postTaskRegisterReqResultRetCode3 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                } catch (Exception e) {
                    bSucceedFlag = false;
                }
            }
        }
        if (bSucceedFlag) {
            List<ObjectIdentifier> opeStartCancelWithHoldLotIDs = new ArrayList<>();
            for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                if (CimStringUtils.equals(partialOpeCompLot.getActionCode(), BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCELWITHHOLD)) {
                    opeStartCancelWithHoldLotIDs.add(partialOpeCompLot.getLotID());
                    opeStartCancelLotCnt++;
                }
            }
            if (!CimObjectUtils.isEmpty(opeStartCancelWithHoldLotIDs)) {
                tmpTxID = "T3EWC015";
                Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                postProcessRegistrationParam.setLotIDs(opeStartCancelWithHoldLotIDs);
                postProcessRegistrationParam.setEquipmentID(params.getEquipmentID());
                postProcessRegistrationParam.setControlJobID(params.getControlJobID());
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
                postTaskRegisterReqParams.setTransactionID(tmpTxID);
                postTaskRegisterReqParams.setKey(null);
                postTaskRegisterReqParams.setPatternID(null);
                postTaskRegisterReqParams.setSequenceNumber(-1);
                try {
                    postTaskRegisterReqResultRetCode4 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                } catch (ServiceException e) {
                    bSucceedFlag = false;
                }
            }
        }
        if (bSucceedFlag) {
            //----------------------------------
            // Post-Processing Update Section
            //----------------------------------
            if (runWaferUpdateByPostProc == 1 && opeStartCancelLotCnt > 0) {
                String strRunWaferCnt = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT);

                boolean additionalInfoFlag = false;
                //  Update postProcessQueue
                List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList = new ArrayList<>();
                // opeStartCancelLotCnt > 0, so either opeStartCancel or opeStartCancelWithHold, or both are claimed. We only update once.
                List<Infos.PostProcessActionInfo> postProcessActionInfoList = null;
                if (!CimObjectUtils.isEmpty(postTaskRegisterReqResultRetCode3.getPostProcessActionInfoList())) {
                    postProcessActionInfoList = postTaskRegisterReqResultRetCode3.getPostProcessActionInfoList();
                } else {
                    // Operatoin Start Cancel with hold lot
                    postProcessActionInfoList = postTaskRegisterReqResultRetCode4.getPostProcessActionInfoList();
                }
                for (Infos.PostProcessActionInfo postProcessActionInfo : postProcessActionInfoList) {
                    if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_RUNWAFERINFOUPDATE)) {
                        Infos.PostProcessAdditionalInfo postProcessAdditionalInfo1 = new Infos.PostProcessAdditionalInfo();
                        postProcessAdditionalInfoList.add(postProcessAdditionalInfo1);
                        postProcessAdditionalInfo1.setDKey(postProcessActionInfo.getDKey());
                        postProcessAdditionalInfo1.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                        postProcessAdditionalInfo1.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT);
                        postProcessAdditionalInfo1.setValue(strRunWaferCnt);
                        if (opeStartCancelLotCnt == CimArrayUtils.getSize(partialOpeCompLotList)) {
                            Infos.PostProcessAdditionalInfo postProcessAdditionalInfo2 = new Infos.PostProcessAdditionalInfo();
                            postProcessAdditionalInfoList.add(postProcessAdditionalInfo2);
                            postProcessAdditionalInfo2.setDKey(postProcessActionInfo.getDKey());
                            postProcessAdditionalInfo2.setSequenceNumber(postProcessActionInfo.getSequenceNumber());
                            postProcessAdditionalInfo2.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_OPESTARTCNT);
                            postProcessAdditionalInfo2.setValue("1");
                        }
                        additionalInfoFlag = true;
                        break;
                    }
                }
                if (additionalInfoFlag) {
                    postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                            postTaskRegisterReqResultRetCode3.getPostProcessActionInfoList(), postProcessAdditionalInfoList, params.getClaimMemo());
                }
            }
        }
        if (bSucceedFlag) {
            if (PostProcForLotFlag != 1) {
                tmpTxID = "ThEWC015";
                if (!CimObjectUtils.isEmpty(result.getHoldReleasedLotIDs())) {
                    Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                    postProcessRegistrationParam.setLotIDs(result.getHoldReleasedLotIDs());
                    postProcessRegistrationParam.setEquipmentID(params.getEquipmentID());
                    postProcessRegistrationParam.setControlJobID(params.getControlJobID());
                    Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                    postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
                    postTaskRegisterReqParams.setTransactionID(tmpTxID);
                    postTaskRegisterReqParams.setKey(null);
                    postTaskRegisterReqParams.setPatternID(null);
                    postTaskRegisterReqParams.setSequenceNumber(-1);
                    try {
                        postTaskRegisterReqResultRetCode5 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                    } catch (ServiceException e) {
                        bSucceedFlag = false;
                    }
                }
            }
        }

        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        if (bSucceedFlag) {
            Integer tmpResult1 = null;
            Integer tmpResult2 = null;
            Integer tmpResult3 = null;
            Integer tmpResult4 = null;
            Integer tmpResult5 = null;
            Integer tmpResult6 = null;
            int PostProcParallelFlag = StandardProperties.OM_PP_PARALLEL_ENABLE.getIntValue();
            if (PostProcParallelFlag == 0) {
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams1 = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams1.setUser(params.getUser());
                postTaskExecuteReqParams1.setKey(postTaskRegisterReqResultRetCode1 == null ? null : postTaskRegisterReqResultRetCode1.getDKey());
                postTaskExecuteReqParams1.setSyncFlag(1);
                postTaskExecuteReqParams1.setPreviousSequenceNumber(0);
                postTaskExecuteReqParams1.setKeyTimeStamp(null);
                postTaskExecuteReqParams1.setClaimMemo("");
                Response response1 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams1);
                tmpResult1 = response1.getCode();
                if (PostProcForLotFlag != 1) {
                    Params.PostTaskExecuteReqParams postTaskExecuteReqParams5 = new Params.PostTaskExecuteReqParams();
                    postTaskExecuteReqParams5.setUser(params.getUser());
                    postTaskExecuteReqParams5.setKey(postTaskRegisterReqResultRetCode2 == null ? null : postTaskRegisterReqResultRetCode2.getDKey());
                    postTaskExecuteReqParams5.setSyncFlag(1);
                    postTaskExecuteReqParams5.setPreviousSequenceNumber(0);
                    postTaskExecuteReqParams5.setKeyTimeStamp(null);
                    postTaskExecuteReqParams5.setClaimMemo("");
                    Response response5 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams5);
                    tmpResult5 = response5.getCode();
                } else {
                    List<Infos.OpeCompLot> opeCompLotList = ((Results.PostTaskExecuteReqResult) response1.getBody()).getStrLotSpecSPCCheckResultSeq();
                    for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                        for (Infos.OpeCompLot opeCompLot : opeCompLotList) {
                            if (ObjectIdentifier.equalsWithValue(partialOpeCompLot.getLotID(), opeCompLot.getLotID())) {
                                // found the lot
                                partialOpeCompLot.setSpcCheckResult(opeCompLot.getSpecificationCheckResult());
                                partialOpeCompLot.setSpcCheckResult(opeCompLot.getSpcCheckResult());
                                partialOpeCompLot.setSpcResult(opeCompLot.getSpcResult());
                                break;
                            }
                        }
                    }
                    // Call TxPostTaskExecuteReq__100 for hold released lot
                    Params.PostTaskExecuteReqParams postTaskExecuteReqParams5 = new Params.PostTaskExecuteReqParams();
                    postTaskExecuteReqParams5.setUser(params.getUser());
                    postTaskExecuteReqParams5.setKey(((Results.PostTaskExecuteReqResult) response1.getBody()).getRelatedQueueKey());
                    postTaskExecuteReqParams5.setSyncFlag(1);
                    postTaskExecuteReqParams5.setPreviousSequenceNumber(0);
                    postTaskExecuteReqParams5.setKeyTimeStamp(null);
                    postTaskExecuteReqParams5.setClaimMemo("");
                    Response response5 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams5);
                    tmpResult5 = response5.getCode();
                }
                //Post Process Execution for OperationCompletionWithHold Lot
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams2 = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams2.setUser(params.getUser());
                postTaskExecuteReqParams2.setKey(postTaskRegisterReqResultRetCode2 == null ? null : postTaskRegisterReqResultRetCode2.getDKey());
                postTaskExecuteReqParams2.setSyncFlag(1);
                postTaskExecuteReqParams2.setPreviousSequenceNumber(0);
                postTaskExecuteReqParams2.setKeyTimeStamp(null);
                postTaskExecuteReqParams2.setClaimMemo("");
                Response response2 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams2);
                tmpResult2 = response2.getCode();
                if (PostProcForLotFlag == 1) {
                    // Set strLotSpecSPCCheckResultSeq (for OpeCompedLot PostProc)
                    // Create pptOpeCompLotSequence from retVal->strPartialOpeCompLotSeq
                    List<Infos.OpeCompLot> opeCompLotList = ((Results.PostTaskExecuteReqResult) response2.getBody()).getStrLotSpecSPCCheckResultSeq();
                    for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                        for (Infos.OpeCompLot opeCompLot : opeCompLotList) {
                            if (ObjectIdentifier.equalsWithValue(partialOpeCompLot.getLotID(), opeCompLot.getLotID())) {
                                // found the lot
                                partialOpeCompLot.setSpcCheckResult(opeCompLot.getSpecificationCheckResult());
                                partialOpeCompLot.setSpcCheckResult(opeCompLot.getSpcCheckResult());
                                partialOpeCompLot.setSpcResult(opeCompLot.getSpcResult());
                                break;
                            }
                        }
                    }
                    // Call TxPostTaskExecuteReq__100 for hold released lot
                    Params.PostTaskExecuteReqParams postTaskExecuteReqParams6 = new Params.PostTaskExecuteReqParams();
                    postTaskExecuteReqParams6.setUser(params.getUser());
                    postTaskExecuteReqParams6.setKey(((Results.PostTaskExecuteReqResult) response2.getBody()).getRelatedQueueKey());
                    postTaskExecuteReqParams6.setSyncFlag(1);
                    postTaskExecuteReqParams6.setPreviousSequenceNumber(0);
                    postTaskExecuteReqParams6.setKeyTimeStamp(null);
                    postTaskExecuteReqParams6.setClaimMemo("");
                    Response response6 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams6);
                    tmpResult6 = response6.getCode();
                }
                //Post Process Execution for OperationStartCancel Lot
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams3 = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams3.setUser(params.getUser());
                postTaskExecuteReqParams3.setKey(postTaskRegisterReqResultRetCode3 == null ? null : postTaskRegisterReqResultRetCode3.getDKey());
                postTaskExecuteReqParams3.setSyncFlag(1);
                postTaskExecuteReqParams3.setPreviousSequenceNumber(0);
                postTaskExecuteReqParams3.setKeyTimeStamp(null);
                postTaskExecuteReqParams3.setClaimMemo("");
                Response response3 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams3);
                tmpResult3 = response3.getCode();
                //Post Process Execution for OperationStartCancelWithHold Lot
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams4 = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams4.setUser(params.getUser());
                postTaskExecuteReqParams4.setKey(postTaskRegisterReqResultRetCode4 == null ? null : postTaskRegisterReqResultRetCode4.getDKey());
                postTaskExecuteReqParams4.setSyncFlag(1);
                postTaskExecuteReqParams4.setPreviousSequenceNumber(0);
                postTaskExecuteReqParams4.setKeyTimeStamp(null);
                postTaskExecuteReqParams4.setClaimMemo("");
                Response response4 = postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams4);
                tmpResult4 = response4.getCode();
            } else {
                boolean bPrallelExecFlag = false;
                boolean bOpeCompedLotParallelExec1 = false;
                //----------------------------------
                // OperationCompletion lot
                //----------------------------------
                if (postTaskRegisterReqResultRetCode1 != null && !CimStringUtils.isEmpty(postTaskRegisterReqResultRetCode1.getDKey())) {
                    Inputs.PostProcessQueueParallelExecCheckIn postProcessQueueParallelExecCheckIn = new Inputs.PostProcessQueueParallelExecCheckIn();
                    postProcessQueueParallelExecCheckIn.setDKey("");
                    postProcessQueueParallelExecCheckIn.setPostProcessActionInfoList(postTaskRegisterReqResultRetCode1.getPostProcessActionInfoList());
                    postProcessQueueParallelExecCheckIn.setLotCountCheckFlag(true);
                    log.info("");
                    Outputs.ObjPostProcessQueueParallelExecCheckOut objPostProcessQueueParallelExecCheckOutRetCode1 = postProcessMethod
                            .postProcessQueueParallelExecCheck(objCommon, postProcessQueueParallelExecCheckIn);
                    bPrallelExecFlag = objPostProcessQueueParallelExecCheckOutRetCode1.isParallelExecFlag();
                    if (bPrallelExecFlag) {
                        // TODO  TxPostConcurrentTaskExecuteReq
                    }
                }
            }
        } else {
            // TODO :txAPCCJRpt
        }
        return Response.createSucc(transactionID, result);
    }

    @Override
    @ResponseBody
    @PostMapping(value = "/move_out_with_running_split_for_ib/req")
    @CimMapping(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ)
    @EnablePostProcess(proxy = PartialMoveOutRequestProxy.class)
    public Response moveOutWithRunningSplitForIBReq(@RequestBody Params.PartialMoveOutReqParams params) {
        //init params
        if (log.isDebugEnabled()) log.debug("step1 : Initialising transactionID");
        final String transactionID = TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, params.getUser());

        // ----------------------------------------------------------------
        // ---- PrivilegeCheck for equipmentID
        // ----------------------------------------------------------------
        if (log.isDebugEnabled()) log.debug("step2 : Privilege Check");
        int accessCheckForCjIntValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getIntValue();
        List<ObjectIdentifier> lotIDs = Collections.emptyList();
        if (accessCheckForCjIntValue == 1) {
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }
        Params.AccessControlCheckInqParams controlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        controlCheckInqParams.setEquipmentID(params.getEquipmentID());
        controlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, controlCheckInqParams);

        //--------------------------------------
        // Main Process
        //--------------------------------------
        String APCIFControlStatus = null;
        String DCSIFControlStatus = null;
        if (log.isDebugEnabled()) log.debug("step3 : Main Process. call sxMoveOutWithRunningSplitForIBReq(...)");
        Results.PartialMoveOutReqResult result = equipmentProcessOperation.sxMoveOutWithRunningSplitForIBReq(objCommon,
                params,
                APCIFControlStatus,
                DCSIFControlStatus);

        // todo:: txAPCCJRpt
        return Response.createSucc(transactionID, result);

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_buffer_type_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_BUFFER_RRESOURCE_TYPE_CHANGE_REQ)
    public Response eqpBufferTypeModifyReq(@RequestBody Params.EqpBufferTypeModifyReqInParm params) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.EQP_BUFFER_RRESOURCE_TYPE_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //Step3 - txEqpBufferTypeModifyReq
        equipmentService.sxEqpBufferTypeModifyReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/reserve_unloading_lots_for_ib/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.UN_LOADING_LOTS_RESERVATION_FOR_INTERNAL_BUFFER_RPT)
    public Response reserveUnloadingLotsForIBRpt(@RequestBody Params.ReserveUnloadingLotsForIBRptParams params) {
        String transactionID = TransactionIDEnum.UN_LOADING_LOTS_RESERVATION_FOR_INTERNAL_BUFFER_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(),
                new Params.AccessControlCheckInqParams(true));

        equipmentService.sxReserveUnloadingLotsForIBRpt(objCommon,
                params.getEquipmentID(),
                params.getCassetteID(),
                params.getUnloadReservePortID(),
                params.getOpeMemo());
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/reserve_cancel_unloading_lots_for_ib/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.UN_LOADING_LOTS_RESERVATION_CANCEL_FOR_INTERNAL_BUFFER_REQ)
    public Response ReserveCancelUnloadingLotsForIBReq(@RequestBody Params.ReserveCancelUnloadingLotsForIBReqParams
                                                                   params) {
        String transactionID = TransactionIDEnum.UN_LOADING_LOTS_RESERVATION_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        Validations.check(null == user, "the user info is null...");

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue))
                && (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1"))) {
            Infos.LotListInCassetteInfo lotListInCassetteInfo =
                    cassetteMethod.cassetteLotIDListGetDR(objCommon, params.getCarrierID());
            lotIDs = lotListInCassetteInfo.getLotIDList();
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams =
                new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //call cancel
        equipmentService.sxReserveCancelUnloadingLotsForIBReq(objCommon,
                params.getEquipmentID(),
                params.getCarrierID());
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/chamber_with_process_wafer/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CHAMBER_PROCESS_WAFER_RPT)
    public Response chamberWithProcessWaferRpt(@RequestBody Params.ChamberWithProcessWaferRptInParams params) {

        final String transactionID = TransactionIDEnum.CHAMBER_PROCESS_WAFER_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(params.getChamberProcessLotInfos()); i++) {
            if (!ObjectIdentifier.isEmpty(params.getChamberProcessLotInfos().get(i).getLotID())) {
                lotIDs.add(params.getChamberProcessLotInfos().get(i).getLotID());
            }
        }
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //step3 - txCarrierStatusChangeRpt
        equipmentService.sxChamberWithProcessWaferRpt(objCommon, params);
        return Response.createSucc(objCommon.getTransactionID());
    }

    @ResponseBody
    @RequestMapping(value = "/wafer_position_with_process_resource/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PROCESS_RESOURCE_WAFER_POSITION_RPT)
    public Response waferPositionWithProcessResourceRpt(@RequestBody Params.WaferPositionWithProcessResourceRptParam param) {

        log.debug("waferPositionWithProcessResourceRptParam: {}", JSONObject.toJSONString(param));
        final String txId = TransactionIDEnum.PROCESS_RESOURCE_WAFER_POSITION_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step1 - calendar_GetCurrentTimeDR

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, param.getUser(), accessControlCheckInqParams);

        //step3 - txWaferPositionWithProcessResourceRpt
        equipmentService.sxWaferPositionWithProcessResourceRpt(objCommon, param);
        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_out_from_ib/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CARRIER_OUT_FROM_IB_REQ)
    public Response carrierOutFromIBReq(@RequestBody Params.CarrierOutFromIBReqParam param) {

        log.debug("waferPositionWithProcessResourceRptParam:%s", JSONObject.toJSONString(param));
        final String txId = TransactionIDEnum.CARRIER_OUT_FROM_IB_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);


        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        List<Infos.EventParameter>        strEventParameter;

        //===== Initialising strObjCommonIn's first two parameters =====//

        //===== Incoming Log Put =====//

        strEventParameter=new ArrayList<>();
        int nLen= CimArrayUtils.getSize(strEventParameter);
        strEventParameter.add(new Infos.EventParameter());
        strEventParameter.get(nLen  ).setParameterName  ("EQP_ID");
        strEventParameter.get(nLen++).setParameterValue ( param.getEquipmentID().getValue() );
        strEventParameter.add(new Infos.EventParameter());
        strEventParameter.get(nLen  ).setParameterName  ("CAST_ID") ;
        strEventParameter.get(nLen++).setParameterValue ( param.getCassetteID().getValue() ) ;

        //【step1】calendar_GetCurrentTimeDR
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, param.getUser());

        ObjectIdentifier dummy=null;
        List<ObjectIdentifier> dummyIDs;
        dummyIDs=new ArrayList<>();

        List<ObjectIdentifier> lotIDs;
        lotIDs=new ArrayList<>();
        String tmpPrivilegeCheckCASTValue  = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();

        if( (0 != CimStringUtils.length(tmpPrivilegeCheckCASTValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1")) ) {
            // step2 - cassette_lotIDList_GetDR
            Infos.LotListInCassetteInfo strCassetteLotIDListGetDROut = cassetteMethod.cassetteLotIDListGetDR(objCommon, param.getCassetteID());



            lotIDs = strCassetteLotIDListGetDROut.getLotIDList();
        } else {
            log.info("OM_ACCESS_CHECK_FOR_CARRIER  OFF Do Nothing." );
        }

        //step3 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);

        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        equipmentService.sxCarrierOutFromIBReq( objCommon, param.getEquipmentID(),
                param.getCassetteID(),
                param.getClaimMemo());

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/

        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/old/force_move_out_for_ib/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.FORCE_OPE_COMP_FOR_INTERNAL_BUFFER_REQ)
    public Response ForceMoveOutForIBReqOld(@RequestBody Params.ForceMoveOutForIBReqParams params) {

        log.debug("ForceMoveOutForIBReqParams:{}", params);
        final String txId = TransactionIDEnum.FORCE_OPE_COMP_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();

        //step1 - calendar_GetCurrentTimeDR
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue) && CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1")) {
            //Step2 - controlJob_lotIDList_GetDR
            log.info("Call controlJob_lotIDList_GetDR");
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }

        //step3 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        //step4 - txForceMoveOutForIBReq
        List<ObjectIdentifier> holdReleasedLotIDs = new ArrayList<>();
        String APCIFControlStatus = null;
        Boolean contextFlag = true;
        Results.ForceMoveOutReqResult strOpeCompLot = equipmentProcessOperation.sxForceMoveOutForIBReq(objCommon,
                params.getEquipmentID(),
                params.getControlJobID(),
                params.getSpcResultRequiredFlag(),
                params.getClaimMemo());

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        int PostProcForLotFlag = StandardProperties.OM_PP_FOR_LOT_MOVEOUT_FLAG.getIntValue();
        Results.PostTaskRegisterReqResult strPostTaskRegisterReqResult = new Results.PostTaskRegisterReqResult();    // for OpeComped Lot
        Results.PostTaskRegisterReqResult strPostTaskRegisterReqResult2 = new Results.PostTaskRegisterReqResult();   // for Released Lot
        String opeCompedTxID;
        if (-1 == PostProcForLotFlag) {
            log.info("Get PostProcForLotFlag used in OpeComp process");
            String strPostProcForLotFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCFORLOTFLAG);
            log.info("strPostProcForLotFlag = {}", strPostProcForLotFlag);

            if (CimStringUtils.equals(strPostProcForLotFlag, "1")) {
                log.info("strPostProcForLotFlag = 1");
                PostProcForLotFlag = 1;
            } else {
                log.info("strPostProcForLotFlag = 0");
                PostProcForLotFlag = 0;
            }
        }
        if (PostProcForLotFlag != 1) {
            opeCompedTxID = "OEQPW023"; //DSIV00002270
        } else {
            opeCompedTxID = "TXEWC_11";
        }
        log.info("PostProcForLotFlag : {}", PostProcForLotFlag);
        log.info("opeCompedTxID : {}", opeCompedTxID);


        log.info("txForceMoveOutForIBReq :rc == RC_OK");
        if (contextFlag) {
            try {
                Infos.PostProcessRegistrationParam strPostProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
                int opeCompedLotlen = CimArrayUtils.getSize(strOpeCompLot.getStrOpeCompLot());
                List<ObjectIdentifier> opeCompedLotIDs = new ArrayList<>(opeCompedLotlen);
                log.info("Registration of Post-Processng for OpeCompedLot ： {}", opeCompedLotlen);

                //Collection of OpeComped LotID
                for (int i = 0; i < opeCompedLotlen; i++) {
                    opeCompedLotIDs.add(strOpeCompLot.getStrOpeCompLot().get(i).getLotID());
                    log.info("Collecting OpeComped LotID : {}", opeCompedLotIDs.get(i));
                }

                log.info("Registration of Post-Processng for OpeCompedLot");

                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                postTaskRegisterReqParams.setUser(objCommon.getUser());
                postTaskRegisterReqParams.setTransactionID(opeCompedTxID);
                postTaskRegisterReqParams.setSequenceNumber(-1);
                postTaskRegisterReqParams.setClaimMemo(params.getClaimMemo());

                strPostProcessRegistrationParm.setLotIDs(opeCompedLotIDs);
                strPostProcessRegistrationParm.setEquipmentID(params.getEquipmentID());
                strPostProcessRegistrationParm.setControlJobID(params.getControlJobID());
                postTaskRegisterReqParams.setPostProcessRegistrationParm(strPostProcessRegistrationParm);
                strPostTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);

                //--------------------------------------------------
                // Post-Processing for OpeCompedLot Update Section
                //--------------------------------------------------
                int nActionLen = CimArrayUtils.getSize(strPostTaskRegisterReqResult.getPostProcessActionInfoList());
                List<Infos.PostProcessAdditionalInfo> strPostProcessAdditionalInfoSeq = new ArrayList<>();
                int nAdditionalCnt = 0;

                for (int nActionIdx = 0; nActionIdx < nActionLen; nActionIdx++) {
                    log.info("For-Loop of regist actions... : {}", nActionIdx);
                    if (CimStringUtils.equals(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_QTIME)) {
                        log.info("QTime exists");
                        if (1 == PostProcForLotFlag) {
                            log.info("PostProcForLotFlag = 1");

                            Outputs.ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out = null;
                            try {
                                strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out = lotMethod.lotPostProcessPreviousBranchAndReturnInfoGetDR(objCommon,
                                        strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getPostProcessTargetObject().getLotID());
                            } catch (ServiceException e) {
                                log.info("lot_postProcessPreviousBranchAndReturnInfo_GetDR() != RC_OK");
                                break;
                            }

                            if (ObjectIdentifier.isNotEmpty(strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getBranchRouteID())) {
                                log.info("branchRouteID : {} ", strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getBranchRouteID());
                                log.info("branchOperationNumber : {}", strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getBranchOperationNumber());

                                List<String> strings = new ArrayList<>();
                                strings.add(ObjectIdentifier.fetchValue(strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getBranchRouteID()));
                                strings.add(strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getBranchOperationNumber());
                                String previousBranchInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);

                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfo.setDKey(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);
                                postProcessAdditionalInfo.setValue(previousBranchInfo);
                                strPostProcessAdditionalInfoSeq.add(nAdditionalCnt, postProcessAdditionalInfo);
                                nAdditionalCnt++;
                            }

                            if (ObjectIdentifier.isNotEmpty(strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getReturnRouteID())) {
                                log.info("returnRouteID    : {}", strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getReturnRouteID());
                                log.info("returnOperationNumber : {}", strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getReturnOperationNumber());

                                List<String> strings = new ArrayList<>();
                                strings.add(ObjectIdentifier.fetchValue(strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getReturnRouteID()));
                                strings.add(strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getReturnOperationNumber());
                                String previousReturnInfo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);

                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfo.setDKey(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);
                                postProcessAdditionalInfo.setValue(previousReturnInfo);
                                strPostProcessAdditionalInfoSeq.add(nAdditionalCnt, postProcessAdditionalInfo);
                                nAdditionalCnt++;
                            }

                            if (CimStringUtils.isNotEmpty(strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getReworkOutKey())) {
                                log.info("reworkOutKey : {}", strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getReworkOutKey());

                                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = new Infos.PostProcessAdditionalInfo();
                                postProcessAdditionalInfo.setDKey(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getDKey());
                                postProcessAdditionalInfo.setSequenceNumber(strPostTaskRegisterReqResult.getPostProcessActionInfoList().get(nActionIdx).getSequenceNumber());
                                postProcessAdditionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);
                                postProcessAdditionalInfo.setValue(strLot_postProcessPreviousBranchAndReturnInfo_GetDR_out.getReworkOutKey());
                                strPostProcessAdditionalInfoSeq.add(nAdditionalCnt, postProcessAdditionalInfo);
                                nAdditionalCnt++;
                            }
                        }
                    }
                }


                log.info("rc = RC_OK");

                if (0 < nAdditionalCnt) {
                    log.info("nAdditionalCnt : {}", nAdditionalCnt);
                    postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                            strPostTaskRegisterReqResult.getPostProcessActionInfoList(), strPostProcessAdditionalInfoSeq, params.getClaimMemo());
                }


                int holdReleasedLotLen = CimArrayUtils.getSize(holdReleasedLotIDs);
                if (holdReleasedLotLen != 0) {
                    log.info("Registration of Post-Processng for HoldReleasedLot ： {}", holdReleasedLotLen);

                    //HoldReleased LotID Trace
                    for (int i = 0; i < holdReleasedLotLen; i++) {
                        log.info("HoldRelesedLotIDs ：{}", holdReleasedLotIDs.get(i));
                    }

                    strPostProcessRegistrationParm.setLotIDs(holdReleasedLotIDs);
                    strPostProcessRegistrationParm.setEquipmentID(params.getEquipmentID());
                    strPostProcessRegistrationParm.setControlJobID(params.getControlJobID());

                    //Post-Processing Registration for Released Lot
                    Params.PostTaskRegisterReqParams postTaskRegisterReqParams1 = new Params.PostTaskRegisterReqParams();
                    postTaskRegisterReqParams1.setTransactionID("ThEWC011");
                    postTaskRegisterReqParams1.setSequenceNumber(-1);
                    postTaskRegisterReqParams1.setPostProcessRegistrationParm(strPostProcessRegistrationParm);
                    postTaskRegisterReqParams1.setClaimMemo(params.getClaimMemo());
                    strPostTaskRegisterReqResult2 = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams1);
                }
            } catch (ServiceException e) {
                contextFlag = false;
            }
        }


        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        if (contextFlag) {
            log.info("ForceOpeCompForIB and Post Process Registration :rc == RC_OK");

            Results.PostTaskExecuteReqResult result = new Results.PostTaskExecuteReqResult();    // for OpeComped Lot
            Results.PostTaskExecuteReqResult result2;   // for Released Lot

            int PostProcParallelFlag = StandardProperties.OM_PP_PARALLEL_ENABLE.getValue() == null ?
                    0 : StandardProperties.OM_PP_PARALLEL_ENABLE.getIntValue();
            if (0 == PostProcParallelFlag) {
                log.info("OM_PP_PARALLEL_ENABLE=0(Sequential)");
                //Post Process Execution
                if (PostProcForLotFlag != 1) {
                    log.info("strPostTaskRegisterReqResult.dKey : {}", strPostTaskRegisterReqResult.getDKey());
                    log.info("strPostTaskRegisterReqResult2.dKey : {}", strPostTaskRegisterReqResult2.getDKey());

                    //Post Process Execution for OpeComped Lot
                    Response response = postTaskExecuteReqController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(objCommon.getUser(),
                            strPostTaskRegisterReqResult.getDKey(), 1, 0, null, params.getClaimMemo()));
                    result = (Results.PostTaskExecuteReqResult)response.getBody();
                    //Post Process Execution for Released Lot
                    Response response2 = postTaskExecuteReqController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(objCommon.getUser(),
                            strPostTaskRegisterReqResult2.getDKey(), 1, 0, null, params.getClaimMemo() ));
                    result2 = (Results.PostTaskExecuteReqResult)response2.getBody();

                } else {
                    log.info("strPostTaskRegisterReqResult.dKey : {}", strPostTaskRegisterReqResult.getDKey());
                    //Post Process Execution for OpeComped Lot
                    Response response  = postTaskExecuteReqController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(objCommon.getUser(),
                            strPostTaskRegisterReqResult.getDKey(),  1, 0, null, params.getClaimMemo() ));
                    result = (Results.PostTaskExecuteReqResult)response.getBody();

                    // Set strLotSpecSPCCheckResultSeq

                    //Post Process Execution for Released Lot
                    Response response2 = postTaskExecuteReqController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(objCommon.getUser(),
                            result.getRelatedQueueKey(), 1, 0, null, params.getClaimMemo() ));
                    result2 = (Results.PostTaskExecuteReqResult)response2.getBody();
                }
            } else {
                log.info("OM_PP_PARALLEL_ENABLE=1(Parallel)");
                Boolean bPrallelExecFlag = false;
                Boolean bOpeCompedLotParallelExec = false;

                log.info("call postProcessQueue_parallelExec_Check()");
                Outputs.ObjPostProcessQueueParallelExecCheckOut strPostProcessQueue_parallelExec_Check_out;
                Inputs.PostProcessQueueParallelExecCheckIn strPostProcessQueueParallelExecCheckIn = new Inputs.PostProcessQueueParallelExecCheckIn();
                strPostProcessQueueParallelExecCheckIn.setDKey("");
                strPostProcessQueueParallelExecCheckIn.setPostProcessActionInfoList(strPostTaskRegisterReqResult.getPostProcessActionInfoList());
                strPostProcessQueueParallelExecCheckIn.setLotCountCheckFlag(true);
                try {
                    strPostProcessQueue_parallelExec_Check_out = postProcessMethod.postProcessQueueParallelExecCheck(objCommon, strPostProcessQueueParallelExecCheckIn);
                    bPrallelExecFlag = strPostProcessQueue_parallelExec_Check_out.isParallelExecFlag();
                } catch (ServiceException ex) {
                    bPrallelExecFlag = false;
                }

                if (CimBooleanUtils.isTrue(bPrallelExecFlag)) {
                    log.info("Post Process parallel execution for OpeComped lot");
                    /*
                    PostConcurrentTaskExecuteReqInParm strPostConcurrentTaskExecuteReqInParm;
                    strPostConcurrentTaskExecuteReqInParm.key      = strPostTaskRegisterReqResult.getDKey();
                    strPostConcurrentTaskExecuteReqInParm.syncFlag = BizConstant.SP_PostProcess_SyncFlag_Sync_Parallel;
                    result_p = TxPostConcurrentTaskExecuteReq( requestUserID, strPostConcurrentTaskExecuteReqInParm, "" );
                    tmpResult = result_p->strResult;
                    tmpResult2.returnCode = CIMFWStrDup(RC_OK); // Error in Post Process for released lot is already merged in result of TxPostConcurrentTaskExecuteReq
                    bOpeCompedLotParallelExec = true;
                    */
                } else {
                    log.info("Post Process sequential execution for OpeComped lot");
                    Response response = postTaskExecuteReqController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams( objCommon.getUser(),
                            strPostTaskRegisterReqResult.getDKey(), BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL,
                            0, null, params.getClaimMemo() ));
                    result = (Results.PostTaskExecuteReqResult)response.getBody();
                }

                //Post Process Execution for Released Lot
                if (PostProcForLotFlag != 1) {
                    log.info("strPostTaskRegisterReqResult2.dKey : {}", strPostTaskRegisterReqResult2.getDKey());
                    if (CimStringUtils.isNotEmpty(strPostTaskRegisterReqResult2.getDKey())) {
                        log.info("call postProcessQueue_parallelExec_Check()");

                        Outputs.ObjPostProcessQueueParallelExecCheckOut strPostProcessQueue_parallelExec_Check_out2;
                        Inputs.PostProcessQueueParallelExecCheckIn strPostProcessQueueParallelExecCheckIn2 = new Inputs.PostProcessQueueParallelExecCheckIn();
                        strPostProcessQueueParallelExecCheckIn2.setDKey("");
                        strPostProcessQueueParallelExecCheckIn2.setPostProcessActionInfoList(strPostTaskRegisterReqResult2.getPostProcessActionInfoList());
                        strPostProcessQueueParallelExecCheckIn2.setLotCountCheckFlag(true);
                        try {
                            strPostProcessQueue_parallelExec_Check_out2 = postProcessMethod.postProcessQueueParallelExecCheck(objCommon, strPostProcessQueueParallelExecCheckIn2);
                            bPrallelExecFlag = strPostProcessQueue_parallelExec_Check_out2.isParallelExecFlag();
                        } catch (ServiceException ex) {
                            bPrallelExecFlag = false;
                        }

                        if (CimBooleanUtils.isTrue(bPrallelExecFlag)) {
                            /*
                            log.info("Post Process parallel execution for released lots");
                            pptPostConcurrentTaskExecuteReqResult_var result_p2;
                            pptPostConcurrentTaskExecuteReqInParm     strPostConcurrentTaskExecuteReqInParm2;
                            strPostConcurrentTaskExecuteReqInParm2.key      = strPostTaskRegisterReqResult2.dKey;
                            strPostConcurrentTaskExecuteReqInParm2.syncFlag = SP_PostProcess_SyncFlag_Sync_Parallel;
                            result_p2 = TxPostConcurrentTaskExecuteReq( requestUserID, strPostConcurrentTaskExecuteReqInParm2, "" );
                            tmpResult2 = result_p2->strResult;
                            */
                        } else {
                            log.info("Post Process sequential execution for released lots");
                            Response response2 = postTaskExecuteReqController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(objCommon.getUser(),
                                    strPostTaskRegisterReqResult2.getDKey(), BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL,
                                    0, null, params.getClaimMemo() ));
                            result2 = (Results.PostTaskExecuteReqResult)response2.getBody();
                        }
                    } else {
                        log.info("Released lot not exist.");

                    }
                } else {
                    if (CimBooleanUtils.isTrue(bOpeCompedLotParallelExec)) {
                        // Set strLotSpecSPCCheckResultSeq (for OpeCompedLot)
                        log.info("Post Process is already executed for released lot.");
                    } else {
                        // Set strLotSpecSPCCheckResultSeq (for OpeCompedLot)
                        log.info("result->relatedQueuekey ； {}", result.getRelatedQueueKey());
                        if (CimStringUtils.isNotEmpty(result.getRelatedQueueKey())) {
                            log.info("call postProcessQueue_parallelExec_Check()");

                            Outputs.ObjPostProcessQueueParallelExecCheckOut strPostProcessQueue_parallelExec_Check_out2;
                            Inputs.PostProcessQueueParallelExecCheckIn strPostProcessQueueParallelExecCheckIn2 = new Inputs.PostProcessQueueParallelExecCheckIn();
                            strPostProcessQueueParallelExecCheckIn2.setDKey(result.getRelatedQueueKey());
                            strPostProcessQueueParallelExecCheckIn2.setLotCountCheckFlag(true);
                            try {
                                strPostProcessQueue_parallelExec_Check_out2 = postProcessMethod.postProcessQueueParallelExecCheck(objCommon, strPostProcessQueueParallelExecCheckIn2);
                                bPrallelExecFlag = strPostProcessQueue_parallelExec_Check_out2.isParallelExecFlag();
                            } catch (ServiceException ex) {
                                bPrallelExecFlag = false;
                            }

                            if (CimBooleanUtils.isTrue(bPrallelExecFlag)) {
                                log.info("Post Process parallel execution for released lots");
                                /*
                                pptPostConcurrentTaskExecuteReqResult_var result_p2;
                                pptPostConcurrentTaskExecuteReqInParm     strPostConcurrentTaskExecuteReqInParm2;
                                strPostConcurrentTaskExecuteReqInParm2.key      = result->relatedQueuekey;
                                strPostConcurrentTaskExecuteReqInParm2.syncFlag = SP_PostProcess_SyncFlag_Sync_Parallel;
                                result_p2 = TxPostConcurrentTaskExecuteReq( requestUserID, strPostConcurrentTaskExecuteReqInParm2, "" );
                                tmpResult2 = result_p2->strResult;
                                */
                            } else {
                                log.info("Post Process sequential execution for released lots");
                                Response response2 = postTaskExecuteReqController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(objCommon.getUser(),
                                        result.getRelatedQueueKey(), BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL,
                                        0, null, params.getClaimMemo() ));
                                result2 = (Results.PostTaskExecuteReqResult)response2.getBody();
                            }
                        } else {
                            log.info("Released lot not exist.");
                        }
                    }
                }
            }

        } else {
            log.info("txForceMoveOutForIBReq or txPostTaskRegisterReq__100 are failed:rc != RC_OK");
            //【TODO】【TODO - NOTIMPL】- txAPCCJRpt
        }

        log.info("Call clearThreadSpecificDataString");
        ThreadContextHolder.clearThreadSpecificDataString();
        return Response.createSucc(txId, strOpeCompLot.getStrOpeCompLot());
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/force_move_out_for_ib/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.FORCE_OPE_COMP_FOR_INTERNAL_BUFFER_REQ)
    @EnablePostProcess(proxy = ForceMoveOutForIBRequestProxy.class)
    public Response ForceMoveOutForIBReq(@RequestBody Params.ForceMoveOutForIBReqParams params) {
        final String txId = TransactionIDEnum.FORCE_OPE_COMP_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        int accessCheckForCjIntValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getIntValue();

        //step1 - calendar_GetCurrentTimeDR
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());
        List<ObjectIdentifier> lotIDs = Collections.emptyList();
        if (accessCheckForCjIntValue == 1) {
            //Step2 - controlJob_lotIDList_GetDR
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }

        //step3 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.ForceMoveOutReqResult strOpeCompLot = equipmentProcessOperation.sxForceMoveOutForIBReq(objCommon,
                params.getEquipmentID(),
                params.getControlJobID(),
                params.getSpcResultRequiredFlag(),
                params.getClaimMemo());

        // todo:: txAPCControlJobInfoRpt

        return Response.createSucc(txId, strOpeCompLot);
        //----------------------------------
        // Post-Processing
        //----------------------------------
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param eqpAlarmRptParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/3/13 10:30
     */
    @ResponseBody
    @RequestMapping(value = "/eqp_alarm/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_ALARM_RPT)
    public Response cxEqpAlarmRpt(@RequestBody Params.EqpAlarmRptParams eqpAlarmRptParams) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Results.EqpAlarmRptResult retVal;

        Infos.ObjCommon strObjCommonIn ;

        int rc = 0 ;

        // Initialising strObjCommonIn's first two parameters

        // step-1 calendar_GetCurrentTimeDR
        String txID=TransactionIDEnum.EQP_ALARM_RPT.getValue();

        List<ObjectIdentifier> dummyIDs;
        dummyIDs=new ArrayList<>();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        ObjectIdentifier equipmentID=eqpAlarmRptParams.getEquipmentID();
        ObjectIdentifier stockerID=eqpAlarmRptParams.getStockerID();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);

        User user=eqpAlarmRptParams.getUser();
        // step-2 txAccessControlCheckInq
        strObjCommonIn= accessInqService.checkPrivilegeAndGetObjCommon(txID,user,accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        ObjectIdentifier AGVID=eqpAlarmRptParams.getAGVID();
        Infos.EquipmentAlarm strEquipmentAlarm=eqpAlarmRptParams.getStrEquipmentAlarm();
        // step-3 txEqpAlarmRpt
        String claimMemo=eqpAlarmRptParams.getClaimMemo();
        retVal = equipmentService.sxEqpAlarmRpt(
                strObjCommonIn     ,
                equipmentID        ,
                stockerID          ,
                AGVID              ,
                strEquipmentAlarm  ,
                claimMemo          );

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/

        return Response.createSucc(txID,retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/process_status/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PROCESS_STATUS_RPT)
    public Response processStatusRpt(@RequestBody Params.ProcessStatusRptParam param) {
        String txId = TransactionIDEnum.PROCESS_STATUS_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);
        User user = param.getUser();
        //step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(param.getLotIDList());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);
        //step2 - txProcessStatusRpt
        equipmentService.sxProcessStatusRpt(objCommon, param);
        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_loading_for_sorter/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOADING_LOT_FOR_SORTER_RPT)
    public Response carrierLoadingForSRTRpt(@RequestBody Params.CarrierLoadingForSORRptParams param) {
        //===========Pre process============
        String txId = TransactionIDEnum.LOADING_LOT_FOR_SORTER_RPT.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = param.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txId);
        }
        //Step1 calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommonIn = utilsComp.setObjCommon(txId, user);

        //Step2 - cassette_lotIDList_GetDR，cassetteLotIDList will used in txAccessControlCheckInq
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue)
                && CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1")) {
            // step2 - cassette_lotIDList_GetDR
            Infos.LotListInCassetteInfo cassetteLotIDList = cassetteMethod.
                    cassetteLotIDListGetDR(objCommonIn, param.getCassetteID());
            if (CimObjectUtils.isEmpty(cassetteLotIDList)) {
                return Response.createError(objCommonIn.getTransactionID(), "not found");
            }
            lotIDs = cassetteLotIDList.getLotIDList();
        }

        //Step3 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        // note: need to split the method of checkPrivilegeAndGetObjCommon

        accessInqService.sxAccessControlCheckInq(objCommonIn, accessControlCheckInqParams);

        //===========Main process============

        //Step5 - txCarrierLoadingRpt
        List<Infos.LoadingVerifiedLot> loadingVerifiedLots;
        String loadPurposeType="Empty Cassette";
        Infos.LotListInCassetteInfo lotListInCassetteInfo = null;
        try {
            if(!SorterHandler.containsFOSB(param.getCassetteID())){
                lotListInCassetteInfo = cassetteMethod
                        .cassetteGetLotList(objCommonIn, param.getCassetteID());
            }
        }catch (ServiceException ex){
            if (log.isErrorEnabled()) {
                log.error(ex.getMessage());
            }
        }
        if (!StringUtils.isEmpty(lotListInCassetteInfo)) {
            loadPurposeType = CimArrayUtils.getSize(lotListInCassetteInfo.getLotIDList()) > 0 ?
                    BizConstant.SP_LOADPURPOSETYPE_OTHER : "Empty Cassette";
        }
        try {
            loadingVerifiedLots = equipmentService.sxCarrierLoadingForSORRpt(objCommonIn,"",
                    param.getEquipmentID(),param.getCassetteID(), param.getPortID(), loadPurposeType);
        } catch (ServiceException e) {
            loadingVerifiedLots = e.getData(List.class);
            if (Validations.isEquals(retCodeConfig.getCastForceLoaded(), e.getCode())) {
                return Response.createSuccWithOmCode(txId, retCodeConfig.getCastForceLoaded(), loadingVerifiedLots);
            } else {
                throw e;
            }
        }
        //===========Post process============
        return Response.createSucc(txId, loadingVerifiedLots);
    }


    @ResponseBody
    @RequestMapping(value = "/carrier_unloading_for_sorter/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.UN_LOADING_LOT_FOR_SORTER_RPT)
    public Response carrierUnloadingForSRTRpt(@RequestBody Params.CarrierUnloadingForSORRptParams params) {
        final String transactionID = TransactionIDEnum.UN_LOADING_LOT_FOR_SORTER_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        // step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(),
                accessControlCheckInqParams);
        equipmentService.sxCarrierUnloadingForSORRpt(objCommon,"", params.getEquipmentID(), params.getCassetteID(),
                params.getPortID());

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_out_port/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CARRIER_OUT_PORT_REQ)
    public Response carrierOutPortReq(@RequestBody CarrierOutPortReqParams params) {
        if (log.isDebugEnabled()) {
            log.debug("step1 : Initialising transactionID");
        }
        String txID = TransactionIDEnum.CARRIER_OUT_PORT_REQ.getValue();
        ThreadContextHolder.setTransactionId(txID);

        //step2 - call checkPrivilegeAndGetObjCommon
        if (log.isDebugEnabled()) {
            log.debug("step2 : Privilege check");
        }
        Params.AccessControlCheckInqParams accessParam = new Params.AccessControlCheckInqParams(true);
        accessParam.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txID,
                params.getUser(),
                accessParam);

        //step3 - sxCarrierOutPortReq
        if (log.isDebugEnabled()) {
            log.debug("step3 : Main Process. call sxCarrierOutPortReq");
        }
        List<CarrierOutPortResults> result = equipmentService.sxCarrierOutPortReq(objCommon, params);
        return Response.createSucc(TransactionIDEnum.CARRIER_OUT_PORT_REQ.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_out/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CARRIER_OUT_REQ)
    public Response carrierOutReq(@RequestBody CarrierOutReqParams params) {
        if (log.isDebugEnabled()) {
            log.debug("step1 : Initialising transactionID");
        }
        String txID = TransactionIDEnum.CARRIER_OUT_REQ.getValue();
        ThreadContextHolder.setTransactionId(txID);

        //step2 - call checkPrivilegeAndGetObjCommon
        if (log.isDebugEnabled()) {
            log.debug("step2 : Privilege check");
        }
        Params.AccessControlCheckInqParams accessParam = new Params.AccessControlCheckInqParams(true);
        accessParam.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txID,
                params.getUser(),
                accessParam);

        //step3 - sxCarrierOutPortReturnReq
        if (log.isDebugEnabled()) {
            log.debug("step3 : Main Process. call sxCarrierOutPortReturnReq");
        }
        CarrierOutPortResults result = equipmentService.sxCarrierOutReq(objCommon, params);
        return Response.createSucc(TransactionIDEnum.CARRIER_OUT_REQ.getValue(), result);
    }
}
