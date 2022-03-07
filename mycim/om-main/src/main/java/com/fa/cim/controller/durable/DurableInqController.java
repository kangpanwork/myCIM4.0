package com.fa.cim.controller.durable;

import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ErrorCode;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.durable.IDurableInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.bank.IBankInqService;
import com.fa.cim.service.durable.IDurableInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 10:57
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/drb")
@Listenable
public class DurableInqController implements IDurableInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IBankInqService carrierListInqService;

    @Autowired
    private IBankInqService carrierDetailInfoInqService;

    @Autowired
    private IDurableInqService durableInqService;

    @ResponseBody
    @RequestMapping(value = "/reticle_pod_list_with_basic_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_POD_BASE_INFO_LIST_INQ)
    public Response reticlePodListWithBasicInfoInq(@RequestBody Params.ReticlePodListWithBasicInfoInqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_BASE_INFO_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        Validations.check(user == null, "userID can not be null");

        //Step1 - calendar_GetCurrentTimeDR
//        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, privilegeCheckParams);

        //Step3 - txReticlePodListWithBasicInfoInq
        List<Infos.DurableAttribute> result = durableInqService.sxReticlePodListWithBasicInfoInq(objCommon, params.getReticlePodIDList());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/reticle_pod_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_POD_LIST_INQ)
    public Response reticlePodListInq(@RequestBody Params.ReticlePodListInqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = params.getUser();
        Validations.check(user == null, "userID can not be null");

        //step1 - calendar_GetCurrentTimeDR

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, privilegeCheckParams);

        //step3 - txReticlePodListInq__170
        Page<Infos.ReticlePodListInfo> result = null;
        String returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
        try {
            result = durableInqService.sxPageReticlePodListInq(objCommon, params);
        } catch (ServiceException ex) {
            if (Validations.isEquals(retCodeConfig.getNotFoundReticlePod(), ex.getCode()) && CimStringUtils.equals(returnOK, CIMStateConst.SP_FUNCTION_AVAILABLE_TRUE)) {
                return Response.createSucc(txId, result);
            }
            throw ex;
        }

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/reticle_pod_detail_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_POD_STATUS_QIN)
    public Response reticlePodDetailInfoInq(@RequestBody Params.ReticlePodDetailInfoInqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_STATUS_QIN.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = params.getUser();
        Validations.check(user == null, "userID can not be null !");


        //Step1 - calendar_GetCurrentTimeDR

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, privilegeCheckParams);

        //Step3 - txReticlePodDetailInfoInq__170
        Results.ReticlePodDetailInfoInqResult result = durableInqService.sxReticlePodDetailInfoInq(objCommon, params);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/reticle_stocker_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_STOCKER_INFO_INQ)
    public Response reticleStocInfoInq(@RequestBody Params.ReticleStocInfoInqParams params) {
        String txId = TransactionIDEnum.RETICLE_STOCKER_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = params.getUser();
        Validations.check(user == null, "userID can not be null");

        //Step1 - calendar_GetCurrentTimeDR

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, privilegeCheckParams);

        //Step3 - txCarrierMoveFromIBRpt
        Results.ReticleStocInfoInqResult result = durableInqService.sxReticleStocInfoInq(objCommon, params.getStockerID());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/durable_status_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CANDIDATE_DURABLE_STATUS_INQ)
    public Response durableStatusSelectionInq(@RequestBody Params.DurableStatusSelectionInqInParms durableStatusSelectionInqInParms) {
        String txId = TransactionIDEnum.CANDIDATE_DURABLE_STATUS_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = durableStatusSelectionInqInParms.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txId);
        }
        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        //step3 - txDurableStatusSelectionInq
        Results.DurableStatusSelectionInqResult result = durableInqService.sxDurableStatusSelectionInq(objCommon, durableStatusSelectionInqInParms.getDurableID());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/durable_sub_status_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CANDIDATE_DURABLE_SUB_STATUS_INQ)
    public Response durableSubStatusSelectionInq(@RequestBody Params.DurableSubStatusSelectionInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.CANDIDATE_DURABLE_SUB_STATUS_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        User user = params.getUser();
        Validations.check(null == user, "the user info is null...");

        //【step2】call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, new Params.AccessControlCheckInqParams(true));

        //【step3】call sxDurableSubStatusSelectionInq(...)
        List<Infos.CandidateDurableSubStatusDetail> result = durableInqService.sxDurableSubStatusSelectionInq(objCommon, params);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/reticle_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_LIST_INQ)
    public Response reticleListInq(@RequestBody Params.ReticleListInqParams reticleListInqParams) {
        final String transactionID = TransactionIDEnum.RETICLE_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar
        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(reticleListInqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, reticleListInqParams.getUser(), accessControlCheckInqParams);

        //step3 - txReticleListInq__170
        Results.PageReticleListInqResult result = null;
        String returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
        try {
            result = durableInqService.sxPageReticleListInq(objCommon, reticleListInqParams);
        } catch (ServiceException ex) {
            if (retCodeConfig.getNotFoundReticle().getCode() == ex.getCode() &&
                    CimStringUtils.equals(returnOK, "1")) {
            } else {
                throw ex;
            }
        }

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/reticle_detail_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_STATUS_INQ)
    public Response reticleDetailInfoInq(@RequestBody Params.ReticleDetailInfoInqParams reticleDetailInfoInqParams) {
        String txID = TransactionIDEnum.RETICLE_STATUS_INQ.getValue();
        ThreadContextHolder.setTransactionId(txID);

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txID, reticleDetailInfoInqParams.getUser(), accessControlCheckInqParams);

        //step3 - txReticleDetailInfoInq__170
        Results.ReticleDetailInfoInqResult result = durableInqService.sxReticleDetailInfoInq(objCommon, reticleDetailInfoInqParams.getReticleID(), reticleDetailInfoInqParams.isDurableOperationInfoFlag(),
                reticleDetailInfoInqParams.isDurableWipOperationInfoFlag());

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CASSETTE_LIST_INQ)
    public Response carrierListInq(@RequestBody Params.CarrierListInqParams carrierListInqParams) {
        //init params
        final String transactionID = TransactionIDEnum.CASSETTE_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = carrierListInqParams.getUser();

        //check input params
        Validations.check(null == user, "the user info is null...");

        //【step1】get schedule from calendar
        log.debug("【step1】get schedule from calendar");

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessControlCheckInqParams.setStockerID(carrierListInqParams.getStockerID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //【step3】call txBankListInq(...)
        log.debug("【step3】call txBankListInq(...)");
        //【jerry】 page
        //RetCode<Results.CarrierListInqResult> result =  carrierListInqService.sxCarrierListInq(objCommon, carrierListInqParams);
        Results.CarrierListInq170Result carrierListInq170Result = carrierListInqService.sxCarrierListInq(objCommon, carrierListInqParams);
        return Response.createSucc(transactionID, carrierListInq170Result);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_detail_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CASSETTE_STATUS_INQ)
    public Response carrierDetailInfoInq(@RequestBody Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams) {
        final String transactionId = TransactionIDEnum.CASSETTE_STATUS_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionId);
        User user = carrierDetailInfoInqParams.getUser();
        //step0 - check input params
        Validations.check(null == user, "the user info is null...");
        //step2  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId, user, accessControlCheckInqParams);
        //step-3: call txCarrierDetailInfoInq;
        Results.CarrierDetailInfoInqResult result = carrierDetailInfoInqService.sxCarrierDetailInfoInq(objCommon, carrierDetailInfoInqParams);
        return Response.createSucc(transactionId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_basic_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CASSETTE_BASE_INFO_LIST_INQ)
    public Response carrierBasicInfoInq(@RequestBody Params.CarrierBasicInfoInqParms carrierBasicInfoInqParms) {

        String txId = TransactionIDEnum.CASSETTE_BASE_INFO_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, carrierBasicInfoInqParms.getUser(), accessControlCheckInqParams);

        //step3 - txCarrierBasicInfoInq
        List<Infos.DurableAttribute> result = durableInqService.sxCarrierBasicInfoInq(objCommon, carrierBasicInfoInqParms.getCassetteIDSeq());
        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/all_reticle_group_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_GROUP_ID_LIST_INQ)
    public Response allReticleGroupListInq(@RequestBody Params.UserParams allReticleGroupListInqParams) {
        //Step-0:Initialize Parameters;
        String transactionID = TransactionIDEnum.RETICLE_GROUP_ID_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, allReticleGroupListInqParams.getUser(), accessControlCheckInqParams);

        //Step-4:txAllReticleGroupListInq;
        log.debug("【Step-4】call-txAllReticleGroupListInq(...)");
        List<ObjectIdentifier> result = durableInqService.sxAllReticleGroupListInq(objCommon);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/all_reticle_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_ID_LIST_INQ)
    public Response allReticleListInq(@RequestBody Params.UserParams userParams) {
        //Step-0:Initialize Parameters;
        String transactionID = TransactionIDEnum.RETICLE_ID_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, userParams.getUser(), accessControlCheckInqParams);

        //Step-4:txAllReticleListInq;
        log.debug("【Step-4】call-txAllReticleListInq(...)");
        List<ObjectIdentifier> result = durableInqService.sxAllReticleListInq(objCommon);

        // Step-5:Post Process(Generate Output Results/event log put/Set Transaction ID);
        return Response.createSucc(transactionID, result);
    }


    //-----------------------------
    // For DMS API
    //-----------------------------
    @ResponseBody
    @PostMapping(value = "/drb_info_for_move_in_reserve/inq")
    @Override
    @CimMapping(TransactionIDEnum.DURABLES_INFO_FOR_START_RESERVATION_INQ)
    public Response drbInfoForMoveInReserveInq(@RequestBody Params.DurablesInfoForStartReservationInqInParam param) {
        Validations.check(null == param, retCodeConfig.getInvalidInputParam());
        String transactionID = TransactionIDEnum.DURABLES_INFO_FOR_START_RESERVATION_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, param.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurablesInfoForStartReservationInqResult result = durableInqService.sxDrbInfoForMoveInReserveInq(objCommon, param);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @PostMapping(value = "/drb_info_for_move_in/inq")
    @Override
    @CimMapping(TransactionIDEnum.DURABLES_INFO_FOR_OPE_START_INQ)
    public Response drbInfoForMoveInInq(@RequestBody Params.DurablesInfoForOpeStartInqInParam paramIn) {
        String transactionID = TransactionIDEnum.DURABLES_INFO_FOR_OPE_START_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(paramIn.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, paramIn.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurablesInfoForOpeStartInqResult infoForOpeStartInqResult = durableInqService.sxDrbInfoForMoveInInq(objCommon, paramIn);
        return Response.createSucc(transactionID, infoForOpeStartInqResult);
    }

    @ResponseBody
    @PostMapping(value = "/drb_step_list/inq")
    @Override
    @CimMapping(TransactionIDEnum.DURABLE_OPERATION_LIST_INQ)
    public Response drbStepListInq(@RequestBody Params.DurableOperationListInqInParam param) {
        String transactionID = TransactionIDEnum.DURABLE_OPERATION_LIST_INQ.getValue();
        User requestUserID = param.getUser();
        ThreadContextHolder.setTransactionId(transactionID);
        ObjectIdentifier dummy = null;
        List<ObjectIdentifier> dummyIDs = new ArrayList<>();

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(dummy);
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        Infos.ObjCommon objCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, requestUserID, accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurableOperationListInqResult durableOperationListInqResult = new Results.DurableOperationListInqResult();
        try {
            durableOperationListInqResult = durableInqService.sxDrbStepListInq(objCommonIn, param);
        } catch (ServiceException e) {
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getSomeopelistDataError())) {
                throw e;
            }
        }

        return Response.createSucc(transactionID, durableOperationListInqResult);
    }

    @ResponseBody
    @PostMapping(value = "/drb_step_list_from_history/inq")
    @Override
    @CimMapping(TransactionIDEnum.DURABLE_OPERATION_LIST_FROM_HISTORY_INQ)
    public Response drbStepListFromHistoryInq(@RequestBody Params.DurableOperationListFromHistoryInqInParam strDurableOperationListFromHistoryInqInParam) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Results.DurableOperationListFromHistoryInqResult retVal = new Results.DurableOperationListFromHistoryInqResult();
        Infos.ObjCommon strObjCommonIn;

        String transactionID = TransactionIDEnum.DURABLE_OPERATION_LIST_FROM_HISTORY_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        // Initialising strObjCommonIn's first two parameters
        User requestUserID = strDurableOperationListFromHistoryInqInParam.getUser();

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummy = null;
        List<ObjectIdentifier> dummyIDs = new ArrayList<>();

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(dummy);
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, requestUserID, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        retVal = durableInqService.sxDrbStepListFromHistoryInq(strObjCommonIn, strDurableOperationListFromHistoryInqInParam);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/drb_step_history/inq")
    @Override
    @CimMapping(TransactionIDEnum.DURABLE_OPERATION_HISTORY_INQ)
    public Response drbStepHistoryInq(
            @RequestBody Params.DurableOperationHistoryInqInParam strDurableOperationHistoryInqInParam) {
        User requestUserID = strDurableOperationHistoryInqInParam.getUser();

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        List<Infos.DurableOperationHisInfo> retVal = new ArrayList<>();

        // Initialising strObjCommonIn's first two parameters
        String transactionID = TransactionIDEnum.DURABLE_OPERATION_HISTORY_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Infos.ObjCommon strObjCommonIn;
        ObjectIdentifier dummy = null;
        List<ObjectIdentifier> dummyIDs = new ArrayList<>();

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(dummy);
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(
                transactionID,
                requestUserID,
                accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        try {
            retVal = durableInqService.sxDrbStepHistoryInq(strObjCommonIn, strDurableOperationHistoryInqInParam);
        } catch (ServiceException ex) {
            if (!Validations.isEquals(ex.getCode(), retCodeConfig.getSomeopehisDataError())) {
                throw ex;
            }
        }

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/drb_control_job_job_list/inq")
    @Override
    @CimMapping(TransactionIDEnum.DURABLE_CONTROL_JOB_LIST_INQ)
    public Response drbControlJobListInq(@RequestBody Params.DurableControlJobListInqParams params) {
        User user = params.getUser();
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        List<Infos.DurableControlJobListInfo> retVal = new ArrayList<>();

        // Initialising strObjCommonIn's first two parameters
        String transactionID = TransactionIDEnum.DURABLE_CONTROL_JOB_LIST_INQ.getValue();

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        try {
            retVal = durableInqService.sxDrbControlJobListInq(objCommonIn, params);
        } catch (ServiceException e) {
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getNotFoundEntry())) {
                throw e;
            }
        }
        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/drb_hold_list/inq")
    @Override
    @CimMapping(TransactionIDEnum.DURABLE_HOLD_LIST_INQ)
    public Response drbHoldListInq(@RequestBody Params.DurableHoldListInqInParam params) {
        User user = params.getUser();
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        List<Infos.DurableHoldListAttributes> retVal = new ArrayList<>();

        // Initialising strObjCommonIn's first two parameters
        String transactionID = TransactionIDEnum.DURABLE_HOLD_LIST_INQ.getValue();

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Infos.ObjCommon objCommonIn;
        ObjectIdentifier dummy = null;
        List<ObjectIdentifier> dummyIDs = new ArrayList<>();

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(dummy);
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        objCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        try {
            retVal = durableInqService.sxDrbHoldListInq(objCommonIn, params);
        } catch (ServiceException e) {
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getNotFoundEntry())) {
                throw e;
            }
        }
        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/process_flow_operation_list_for_drb/inq")
    @Override
    @CimMapping(TransactionIDEnum.ROUTE_OPERATION_LIST_FOR_DURABLE_INQ)
    public Response processFlowOperationListForDrbInq(@RequestBody Params.RouteOperationListForDurableInqInParam strRouteOperationListForDurableInqInParam) {
        // Initializing strObjCommonIn's first two parameters
        String transactionID = TransactionIDEnum.ROUTE_OPERATION_LIST_FOR_DURABLE_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummyID;
        dummyID = ObjectIdentifier.buildWithValue("");
        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>(0);

        Infos.ObjCommon strObjCommonIn;
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(dummyID);
        accessControlCheckInqParams.setStockerID(dummyID);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, strRouteOperationListForDurableInqInParam.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<Infos.OperationNameAttributes> retVal = durableInqService.sxProcessFlowOperationListForDrbInq(strObjCommonIn, strRouteOperationListForDurableInqInParam);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        log.info("PPTServiceManager_i::TxRouteOperationListForDurableInq");
        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/multi_drb_path_list/inq")
    @CimMapping(TransactionIDEnum.CONNECTED_DURABLE_ROUTE_LIST_INQ)
    @Override
    public Response multiDrbPathListInq(@RequestBody Params.ConnectedDurableRouteListInqParams params) {
        String transactionID = TransactionIDEnum.CONNECTED_DURABLE_ROUTE_LIST_INQ.getValue();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        List<Infos.ConnectedRouteList> connectedRouteLists = new ArrayList<>();
        try {
            connectedRouteLists = durableInqService.sxConnectedDurableRouteListInq(objCommon, params.getConnectedDurableRouteListInqInParam());
        } catch (ServiceException e) {
            String returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getNotFoundEntryW()) || !CimBooleanUtils.isTrue(returnOK)) {
                throw e;
            }
        }
        return Response.createSucc(transactionID, connectedRouteLists);
    }

    @ResponseBody
    @PostMapping(value = "/ava_carrier/inq")
    @Override
    public Response availableCarrierInq() {
        Infos.ObjCommon objCommon = new Infos.ObjCommon();
        List<Infos.AvailableCarrierOut> strings = durableInqService.sxAvailableCarrierListForLotStartInq(objCommon);
        return Response.createSucc(null, strings);
    }

    @ResponseBody
    @RequestMapping(value = "/drb_what_next_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WHAT_NEXT_DURABLE_LIST_INQ)
    public Response drbWhatNextListInq(@RequestBody Params.WhatNextDurableListInqInParam strWhatNextDurableListInqInParam) {

        String txID = TransactionIDEnum.WHAT_NEXT_DURABLE_LIST_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txID);


        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(strWhatNextDurableListInqInParam.getEquipmentID());
        ObjectIdentifier dummy = null;
        accessControlCheckInqParams.setStockerID(dummy);
        List<ObjectIdentifier> dummyIDs = new ArrayList<>(0);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txID, strWhatNextDurableListInqInParam.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Results.WhatNextDurableListInqResult retVal = new Results.WhatNextDurableListInqResult();

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        retVal = durableInqService.sxDrbWhatNextListInq(objCommon, strWhatNextDurableListInqInParam);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(txID, retVal);
    }


    @ResponseBody
    @PostMapping(value = "/durable_job_status_selection/inq")
    @Override
    @CimMapping(TransactionIDEnum.DURABLE_JOB_STATUS_SELECTION_INQ)
    public Response durableJobStatusSelectionInq(@RequestBody Params.DurableJobStatusSelectionInqParams params) {
        Validations.check(null == params, retCodeConfig.getInvalidInputParam());

        String transactionID = TransactionIDEnum.DURABLE_JOB_STATUS_SELECTION_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        Results.CandidateDurableJobStatusDetail retVal = durableInqService.sxDurableJobStatusSelectionInq(objCommon, params);
        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/erack_pod_info/inq")
    @Override
    @CimMapping(TransactionIDEnum.ER_POD_INFO_INQ)
    public Response ErackPodInfoInq(@RequestBody Params.ErackPodInfoInqParams params) {
        String transactionID = TransactionIDEnum.ER_POD_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));
        return Response.createSucc(transactionID, durableInqService.sxErackPodInfoInq(objCommon, params.getStockerID()));
    }

    @RequestMapping(value = "/reticle_pod_stocker_info/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_STOCKER_INFO_INQ)
    @Override
    public Response reticlePodStockerInfoInq(@RequestBody Params.ReticlePodStockerInfoInqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_STOCKER_INFO_INQ.getValue();

        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setStockerID(params.getStockerID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        Results.ReticlePodStockerInfoInqResult result = durableInqService.sxReticlePodStockerInfoInq(objCommon, params);
        return Response.createSucc(txId, result);
    }

    @RequestMapping(value = "/brs_info/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.BARE_RETICLE_STOCKER_INFO_INQ)
    @Override
    public Response bRSInfoInq(@RequestBody Params.BareReticleStockerInfoInqParams params) {
        String txId = TransactionIDEnum.BARE_RETICLE_STOCKER_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setStockerID(params.getStockerID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        Results.BareReticleStockerInfoInqResult result = durableInqService.sxBareReticleStockerInfoInq(objCommon, params);
        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/drb_bank_in_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DURABLE_BANK_IN_LIST_INQ)
    public Response drbBankInListInq(@RequestBody Params.BankListInqParams params) {
        final String txId = TransactionIDEnum.DURABLE_BANK_IN_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.BankListInqResult bankListInqResult = durableInqService.sxDrbBankInListInq(objCommon, params);
        return Response.createSucc(txId,bankListInqResult);
    }

}
