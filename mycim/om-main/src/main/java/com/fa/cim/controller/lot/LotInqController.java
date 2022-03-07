package com.fa.cim.controller.lot;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.lot.ILotInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.lot.ILotInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 15:37
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/lot")
@Listenable
public class LotInqController implements ILotInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotInqService lotInqService;

    @ResponseBody
    @RequestMapping(value = "/hold_lot_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.LOT_HOLD_LIST_INQ)
    @Override
    public Response HoldLotListInq(@RequestBody Params.HoldLotListInqParams params) {
        log.info(String.valueOf(params));
        //【step0】init params
        final String transactionID = TransactionIDEnum.LOT_HOLD_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step0】check input params
        ObjectIdentifier lotID = params.getLotID();
        SearchCondition searchCondition = params.getSearchCondition();
        Validations.check(ObjectIdentifier.isEmpty(lotID) || null == searchCondition, "the parameter is null");

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxHoldLotListInq(...)
        List<Infos.LotHoldListAttributes> lotHoldListAttributesList = null;
        try {
            lotHoldListAttributesList = lotInqService.sxHoldLotListInq(objCommon, lotID);
        } catch (ServiceException ex) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEntry(), ex.getCode())) {
            }
        }
        Results.HoldLotListInqResult holdLotListInqResult = new Results.HoldLotListInqResult();
        if (!CimObjectUtils.isEmpty(lotHoldListAttributesList)) {
            holdLotListInqResult.setLotHoldListAttributes(CimPageUtils.convertListToPage(lotHoldListAttributesList, params.getSearchCondition().getPage(), params.getSearchCondition().getSize()));
        }
        Response succ = Response.createSucc(transactionID, holdLotListInqResult);
        log.info(String.valueOf(succ));
        return succ;
    }

    @ResponseBody
    @RequestMapping(value = "/wafer_alias_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_ALIAS_NAME_INFO_INQ)
    public Response waferAliasInfoInq(@RequestBody Params.WaferAliasInfoInqParams params) {

        //init params
        final String transactionID = TransactionIDEnum.WAFER_ALIAS_NAME_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        List<ObjectIdentifier> waferIDSeq = params.getWaferIDSeq();
        Validations.check(org.springframework.util.ObjectUtils.isEmpty(waferIDSeq), "the waferIDSeq is null...");

        //【step2】call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        //【step3】call sxWaferAliasInfoInq(...)
        return Response.createSucc(transactionID, lotInqService.sxWaferAliasInfoInq(objCommon, params));
    }

    @ResponseBody
    @RequestMapping(value = "/wafer_list_in_lot_family/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_LIST_IN_LOT_FAMILY_INFO)
    public Response waferListInLotFamilyInq(@RequestBody Params.WaferListInLotFamilyInqParams params) {
        String transactionID = TransactionIDEnum.WAFER_LIST_IN_LOT_FAMILY_INFO.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step2 - txAccessControlCheckInq
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));
        //Step3 - txWaferListInLotFamilyInq
        return Response.createSucc(transactionID, lotInqService.sxWaferListInLotFamilyInq(objCommon, params.getLotFamilyID()));
    }

    @ResponseBody
    @RequestMapping(value = "/multi_path_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CONNECTED_ROUTE_LIST_INQ)
    public Response multiPathListInq(@RequestBody Params.MultiPathListInqParams multiPathListInqParams) {
        //Step-0:Initialize Parameters;
        final String transactionID = TransactionIDEnum.CONNECTED_ROUTE_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = multiPathListInqParams.getUser();
        //Step-1:Incoming Log Put;
        log.info(" Incoming = {}", transactionID);
        //Step-2:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(multiPathListInqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);
        //Step-3:txMultiPathListInq;
        log.debug("【Step-4】call-txMultiPathListInq(...)");
        List<Infos.ConnectedRouteList> result = null;
        int returnOk = CimNumberUtils.intValue(StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue());
        try {
            result = lotInqService.sxMultiPathListInq(multiPathListInqParams, objCommon);
        } catch (ServiceException ex) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEntryW(), ex.getCode()) && returnOk == 1) {

            } else {
                throw ex;
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/dynamic_path_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DYNAMIC_ROUTE_LIST_INQ)
    public Response dynamicPathListInq(@RequestBody Params.DynamicPathListInqParams params) {
        //【step0】init params
        final String txId = TransactionIDEnum.DYNAMIC_ROUTE_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        //【step1】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);
        //【step2】call dynamicPathListInq
        Page<Infos.DynamicRouteList> result = lotInqService.sxDynamicPathListInq(objCommon, params);
        // return Response.createSucc(txId, result);

        // use to paging query
        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_family/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_FAMILY_INQ)
    public Response lotFamilyInq(@RequestBody Params.LotFamilyInqParams lotFamilyInqParams) {
        //Step-0:Initialize Parameters;
        String transactionID = TransactionIDEnum.LOT_FAMILY_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(lotFamilyInqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, lotFamilyInqParams.getUser(), accessControlCheckInqParams);

        //Step-4:txLotFamilyInq;
        log.debug("【Step-4】call-txLotFamilyInq(...)");
        Results.LotFamilyInqResult result = lotInqService.sxLotFamilyInq(lotFamilyInqParams, objCommon);

        //Step-5:Post Process(Generate Output Results/event log put/Set Transaction ID);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_future_pctrl_detail_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_FUTURE_ACTION_DETAIL_INFO)
    public Response lotFuturePctrlDetailInfoInq(@RequestBody Params.FutureActionDetailInfoInqParams futureActionDetailInfoInqParams) {
        //Step-0:Initialize Parameters;
        String transactionID = TransactionIDEnum.LOT_FUTURE_ACTION_DETAIL_INFO.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(futureActionDetailInfoInqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, futureActionDetailInfoInqParams.getUser(), accessControlCheckInqParams);

        //Step-4:txLotFuturePctrlDetailInfoInq;
        log.debug("【Step-4】call-txLotFuturePctrlDetailInfoInq(...)");
        Results.LotFuturePctrlDetailInfoInqResult result = lotInqService.sxLotFuturePctrlDetailInfoInq(objCommon, futureActionDetailInfoInqParams);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_info_by_wafer/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_INFO_BY_WAFER_INQ)
    public Response lotInfoByWaferInq(@RequestBody Params.LotInfoByWaferInqParams lotInfoByWaferInqParams) {
        //Step-0:Initialize Parameters;
        String transactionID = TransactionIDEnum.LOT_INFO_BY_WAFER_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, lotInfoByWaferInqParams.getUser(), accessControlCheckInqParams);

        //Step-4:txLotInfoByWaferInq;
        log.debug("【Step-4】call-txLotInfoByWaferInq(...)");
        ObjectIdentifier result = lotInqService.sxLotInfoByWaferInq(lotInfoByWaferInqParams, objCommon);

        // Step-5:Post Process(Generate Output Results/event log put/Set Transaction ID);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_INFO_INQ)
    public Response lotInfoInq(@RequestBody Params.LotInfoInqParams lotInfoInqParams) {
        log.debug("【step0】init params");
        final String transactionID = TransactionIDEnum.LOT_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.debug("【step1】init params");
        List<ObjectIdentifier> lotIDs = lotInfoInqParams.getLotIDs();
        Validations.check(null == lotIDs, "the lotIDs is null...");

        log.debug("【step2】call txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, lotInfoInqParams.getUser(),
                accessControlCheckInqParams);

        log.debug("【step3】done: call sxLotInfoInq(...)");
        Results.LotInfoInqResult result = lotInqService.sxLotInfoInq(objCommon, lotInfoInqParams);
        Response response = Response.createSucc(transactionID, result);

        log.debug("【step4】judge whether the return code is success, if no, then TCC will rollback");
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/lot_list_by_carrier/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_LIST_IN_CASSETTE_INQ)
    public Response lotListByCarrierInq(@RequestBody Params.LotListByCarrierInqParams lotListByCarrierInqParams) {
        //init params
        final String transactionID = TransactionIDEnum.LOT_LIST_IN_CASSETTE_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        ObjectIdentifier cassetteID = lotListByCarrierInqParams.getCassetteID();

        //【step2】check user's privilege
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, lotListByCarrierInqParams.getUser(), accessControlCheckInqParams);

        //【step3】LotListByCarrierInq
        log.debug("【step3 LotListByCarrierInq service");
        Results.LotListByCarrierInqResult result = lotInqService.sxLotListByCarrierInq(objCommon, cassetteID);
        return Response.createSucc(transactionID, result);

    }

    @ResponseBody
    @RequestMapping(value = "/lot_list_by_cj/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_LIST_IN_CONTROL_JOB_INQ)
    public Response lotListByCJInq(@RequestBody Params.LotListByCJInqParams lotListByCJInqParams) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.LOT_LIST_IN_CONTROL_JOB_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());
        User user = lotListByCJInqParams.getUser();

        //Step-2:txAccessControlCheckInq;
        log.debug("【Step-2】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), lotListByCJInqParams.getUser(), accessControlCheckInqParams);

        //Step-3:txLotListByCJInq;
        log.debug("【Step-3】call-txLotListByCJInq(...)");
        List<Infos.ControlJobInfo> controlJobInfos = lotInqService.sxLotListByCJInq(lotListByCJInqParams, objCommon);
        return Response.createSucc(transactionID.getValue(), controlJobInfos);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_LIST_INQ)
    public Response lotListInq(@RequestBody @Validated Params.LotListInqParams lotListInqParams) {
        //【step0】init params
        final TransactionIDEnum transactionID = TransactionIDEnum.LOT_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //【step2】check AccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(),
                lotListInqParams.getUser(), accessControlCheckInqParams);

        //【step3】call LotListInq service
        Page<Infos.LotListAttributes> result = lotInqService.sxLotListInq(lotListInqParams, objCommon);

        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_operation_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_OPERATION_LIST_INQ)
    public Response LotOperationSelectionInq(@RequestBody Params.LotOperationSelectionInqParams params) {

        //【step0】init params
        final String transactionID = TransactionIDEnum.LOT_OPERATION_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step0】check input params
        ObjectIdentifier lotID = params.getLotID();
        SearchCondition searchCondition = params.getSearchCondition();
        Validations.check(ObjectIdentifier.isEmpty(lotID) || CimObjectUtils.isEmpty(searchCondition), "the parameter is null...");


        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //step 3
        Results.LotOperationSelectionInqResult result = lotInqService.sxLotOperationSelectionInq(objCommon, params);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/all_mfg_layer_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MFG_LAYER_LIST_INQ)
    public Response allMfgLayerListInq(@RequestBody User requestUser) {
        //Step-0:Initialize Parameters;
        final String transactionID = TransactionIDEnum.MFG_LAYER_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step-1:Incoming Log Put;
        log.info(" Incoming = {}", transactionID);


        //Step-2:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, requestUser, accessControlCheckInqParams);

        //Step-4:txAllMfgLayerListInq;
        log.debug("【Step-4】call-txAllMfgLayerListInq(...)");
        List<Infos.SubMfgLayerAttributes> subMfgLayerAttributesList = null;

        try {
            subMfgLayerAttributesList = lotInqService.sxAllMfgLayerListInq(objCommon);

        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getNotFoundCategory(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getSucc(), e.getCode())) {
                log.info("No found entry allowed. ");
            } else {
                throw e;
            }
        }

        return Response.createSucc(transactionID, subMfgLayerAttributesList);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_operation_selection_for_multiple_lots/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MULTIPLE_LOTS_OPERATION_LIST_INQ)
    public Response lotOperationSelectionForMultipleLotsInq(@RequestBody Params.LotOperationSelectionForMultipleLotsInqParams params) {

        //init params
        final String transactionID = TransactionIDEnum.MULTIPLE_LOTS_OPERATION_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        List<ObjectIdentifier> lotIDs = params.getLotIDs();
        Validations.check(CimArrayUtils.isEmpty(lotIDs), "lotIDs is null");

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(params.getLotIDs());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxLotOperationSelectionInq(...)
        Results.LotOperationSelectionForMultipleLotsInqResult result = new Results.LotOperationSelectionForMultipleLotsInqResult();
        List<Results.LotOperationSelectionInqResult> lotOperationSelectionInqResults = new ArrayList<>();
        for (ObjectIdentifier lotID : lotIDs) {
            Params.LotOperationSelectionInqParams lotOperationSelectionInqParams = new Params.LotOperationSelectionInqParams();
            lotOperationSelectionInqParams.setSearchDirection(params.isSearchDirection());
            lotOperationSelectionInqParams.setPosSearchFlag(params.isPosSearchFlag());
            lotOperationSelectionInqParams.setSearchCount(params.getSearchCount());
            lotOperationSelectionInqParams.setCurrentFlag(params.isCurrentFlag());
            lotOperationSelectionInqParams.setLotID(lotID);
            Results.LotOperationSelectionInqResult out = null;
            try {
                out = lotInqService.sxLotOperationSelectionInq(objCommon, lotOperationSelectionInqParams);
            } catch (ServiceException e) {
                break;
            }
            lotOperationSelectionInqResults.add(out);
        }
        result.setLotOperationSelectionInqResults(lotOperationSelectionInqResults);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/all_process_step_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PROCESS_DEFINITION_ID_LIST_INQ)
    public Response allProcessStepListInq(@RequestBody Params.UserParams requestedUser) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.PROCESS_DEFINITION_ID_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-1:Incoming Log Put;
        log.info(" Incoming = {}", transactionID.getValue());

        //Step-2:txCalendar_GetCurrentTimeDR;
        log.debug("【Step-2】txCalendar_GetCurrentTimeDR(...)");

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), requestedUser.getUser(), accessControlCheckInqParams);

        //Step-4:txAllProcessStepListInq;
        log.debug("【Step-4】call-txAllProcessStepListInq(...)");
        List<ObjectIdentifier> result = lotInqService.sxAllProcessStepListInq(objCommon);

        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/main_process_flow_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ROUTE_INDEX_LIST_INQ)
    public Response mainProcessFlowListInq(@RequestBody Params.MainProcessFlowListInqParams params) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ROUTE_INDEX_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());
        User user = params.getUser();
        //Step-1:Incoming Log Put;
        log.info(" Incoming = {}", transactionID.getValue());
        //Step-2:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), user, accessControlCheckInqParams);
        //Step-4:txMainProcessFlowListInq;
        log.debug("【Step-4】call-txMainProcessFlowListInq(...)");
        List<Infos.RouteIndexInformation> result = lotInqService.sxMainProcessFlowListInq(objCommon, params);
        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/process_flow_operation_list_for_lot/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ROUTE_OPERATION_LIST_FOR_LOT_INQ)
    public Response processFlowOperationListForLotInq(@RequestBody Params.ProcessFlowOperationListForLotInqParams params) {

        //【step1】init params
        final String transactionID = TransactionIDEnum.ROUTE_OPERATION_LIST_FOR_LOT_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        ObjectIdentifier lotID = params.getLotID();
        SearchCondition searchCondition = params.getSearchCondition();
        Validations.check(ObjectIdentifier.isEmpty(lotID) || null == searchCondition, "the parameter is null...");

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxProcessFlowOperationListForLotInq(...)
        List<Infos.OperationNameAttributes> operationNameResult = lotInqService.sxProcessFlowOperationListForLotInq(objCommon, lotID);

        return Response.createSucc(transactionID, new Results.ProcessFlowOperationListInqResult(CimPageUtils.convertListToPage(operationNameResult, params.getSearchCondition().getPage(), params.getSearchCondition().getSize())));
    }

    @ResponseBody
    @RequestMapping(value = "/process_flow_operation_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ROUTE_OPERATION_LIST_INQ)
    public Response processFlowOperationListInq(@RequestBody Params.ProcessFlowOperationListInqParams processFlowOperationListInqParams) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ROUTE_OPERATION_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());
        User user = processFlowOperationListInqParams.getUser();
        //Step-1:Incoming Log Put;
        log.info(transactionID.getValue(), " Incoming");
        //Step-2:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> routeIDs = new ArrayList<>();
        routeIDs.add(processFlowOperationListInqParams.getRouteID());
        accessControlCheckInqParams.setRouteIDList(routeIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), user, accessControlCheckInqParams);
        //Step-3:txProcessFlowOperationListInq;
        log.debug("【Step-4】call-txProcessFlowOperationListInq(...)");
        List<Infos.OperationNameAttributes> result = lotInqService.sxProcessFlowOperationListInq(objCommon, processFlowOperationListInqParams);
        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/all_lot_type_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ALL_LOT_TYPE_LIST_INQ)
    public Response allLotTypeListInq(@RequestBody Params.AllLotTypeListInqParams params) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ALL_LOT_TYPE_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());
        User user = params.getUser();
        //Step-1:Incoming Log Put;
        log.info(transactionID.getValue(), " Incoming");
        //Step-2:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), user, accessControlCheckInqParams);
        //Step-3:txProcessFlowOperationListInq;
        log.debug("【Step-4】call-txProcessFlowOperationListInq(...)");
        List<ObjectIdentifier> allLotType = lotInqService.getAllLotType();
        return Response.createSucc(transactionID.getValue(), allLotType);
    }

}