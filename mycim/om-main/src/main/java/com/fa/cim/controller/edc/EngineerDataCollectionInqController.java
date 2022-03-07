package com.fa.cim.controller.edc;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ErrorCode;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.interfaces.engineerDataCollection.IEngineerDataCollectionInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.edc.IEngineerDataCollectionInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
 * @date: 2019/7/30 14:17
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/edc")
@Listenable
public class EngineerDataCollectionInqController implements IEngineerDataCollectionInqController {
    @Autowired
    private IEngineerDataCollectionInqService engineerDataCollectionInqService;
    @Autowired
    private IAccessInqService IAccessControlCheckInqService;

    @ResponseBody
    @RequestMapping(value = "/edc_spec_check_action_result/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.COLLECTED_DATA_ACTION_INQ)
    public Response edcSpecCheckActionResultInq(@RequestBody Params.EDCSpecCheckActionResultInqInParms edcSpecCheckActionResultInqInParms) {

        User user = edcSpecCheckActionResultInqInParms.getUser();
        String txID = TransactionIDEnum.COLLECTED_DATA_ACTION_INQ.getValue();
        ThreadContextHolder.setTransactionId(txID);
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txID);
        }

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = IAccessControlCheckInqService.checkPrivilegeAndGetObjCommon(txID, user, accessControlCheckInqParams);

        //step3 - txEDCSpecCheckActionResultInq
        Results.EDCSpecCheckActionResultInqResult result = engineerDataCollectionInqService.sxEDCSpecCheckActionResultInq(objCommon, edcSpecCheckActionResultInqInParms);

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/edc_data_item_list_by_key/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DATA_COLLECTION_ITEM_LIST_INQ)
    public Response edcDataItemListByKeyInq(@RequestBody Params.EDCDataItemListByKeyInqParams params) {
        //======Pre Process======
        String txId = TransactionIDEnum.DATA_COLLECTION_ITEM_LIST_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        Validations.check(user == null, "userID can not be null");

        //Step1 - calendar_GetCurrentTimeDR
        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = IAccessControlCheckInqService.checkPrivilegeAndGetObjCommon(txId, user, privilegeCheckParams);

        //Step3 - txEDCDataItemListByKeyInq
        Results.EDCDataItemListByKeyInqResult result = engineerDataCollectionInqService.sxEDCDataItemListByKeyInq(objCommon, params.getSearchKeyPattern(), params.getSearchKeys());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/edc_config_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DATA_COLLECTION_LIST_INQ)
    public Response edcConfigListInq(@RequestBody Params.EDCConfigListInqParams params) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        log.info("EDCConfigListInqParams: " + params);
        final String transactionID = TransactionIDEnum.DATA_COLLECTION_LIST_INQ.getValue();
        // set current tx id.
        ThreadContextHolder.setTransactionId(transactionID);
        // check input params.
        User user = params.getUser();
        Validations.check(null == user, "the user info is null.");
        Infos.EDCConfigListInqInParm edcConfigListInqInParm = params.getStrEDCConfigListInqInParm();
        Validations.check(null == edcConfigListInqInParm, "the params of edcConfigListInqInParm is null.");

        // 【Step1】  calendar_GetCurrentTimeDR(...)

        // 【Step2】  txAccessControlCheckInq(...)
        //--- Privilege Check for lot -----//
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = IAccessControlCheckInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        // 【Step3】 txEDCConfigListInq(...)
        List<Infos.DataCollection> listRetCode = engineerDataCollectionInqService.sxEDCConfigListInq(objCommon, edcConfigListInqInParm);

        return Response.createSucc(transactionID, listRetCode);
    }

    @ResponseBody
    @RequestMapping(value = "/edc_data_item_with_transit_data/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DATA_ITEM_WITH_TEMP_DATA_INQ)
    public Response edcDataItemWithTransitDataInq(@RequestBody Params.EDCDataItemWithTransitDataInqParams params) {
        log.debug("carrierExchangeReq(): invoke carrierExchangeReq");
        String txId = TransactionIDEnum.DATA_ITEM_WITH_TEMP_DATA_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("userID can not be null"), txId);
        }
        //Step1 - calendar_GetCurrentTimeDR

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = IAccessControlCheckInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        Results.EDCDataItemWithTransitDataInqResult result = engineerDataCollectionInqService.sxEDCDataItemWithTransitDataInq(objCommon, params.getEquipmentID(), params.getControlJobID());
        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/spec_check_result/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DATA_SPEC_CHECK_RESULT_INQ)
    public Response specCheckResultInq(@RequestBody Params.SpecCheckResultInqInParms specCheckResultInqInParms) {

        User user = specCheckResultInqInParms.getUser();
        String txID = TransactionIDEnum.DATA_SPEC_CHECK_RESULT_INQ.getValue();

        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txID);
        }


        ThreadContextHolder.setTransactionId(txID);

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar
        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = IAccessControlCheckInqService.checkPrivilegeAndGetObjCommon(txID, user, accessControlCheckInqParams);

        //step3 - txSpecCheckResultInq
        Results.SpecCheckResultInqResult result = engineerDataCollectionInqService.sxSpecCheckResultInq(objCommon, specCheckResultInqInParms);

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/edc_plan_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DC_DEF_DETAIL_INFO_INQ)
    public Response edcPlanInfoInq(@RequestBody Params.EDCPlanInfoInqParms edcPlanInfoInqParms) {

        User user = edcPlanInfoInqParms.getUser();
        String txID = TransactionIDEnum.DC_DEF_DETAIL_INFO_INQ.getValue();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txID);
        }

        ThreadContextHolder.setTransactionId(txID);

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = IAccessControlCheckInqService.checkPrivilegeAndGetObjCommon(txID, user, accessControlCheckInqParams);

        //step3 - txEDCPlanInfoInq
        Results.EDCPlanInfoInqResult result = engineerDataCollectionInqService.sxEDCPlanInfoInq(objCommon, edcPlanInfoInqParms);

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/edc_spec_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DC_SPEC_DETAIL_INFO_INQ)
    public Response edcSpecInfoInq(@RequestBody Params.EDCSpecInfoInqParms edcSpecInfoInqParms) {

        User user = edcSpecInfoInqParms.getUser();
        String txID = TransactionIDEnum.DC_SPEC_DETAIL_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(txID);
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txID);
        }

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = IAccessControlCheckInqService.checkPrivilegeAndGetObjCommon(txID, user, accessControlCheckInqParams);

        //step3 - txEDCSpecInfoInq__101
        Results.EDCSpecInfoInqResult result = engineerDataCollectionInqService.sxEDCSpecInfoInq(objCommon, edcSpecInfoInqParms);

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/edc_data_show_for_update/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.COLLECTED_DATA_INFO_FOR_UPDATE_INQ)
    public Response edcDataShowForUpdateInq(@RequestBody Params.EDCDataShowForUpdateInqParams params) {
        final String transactionID = TransactionIDEnum.COLLECTED_DATA_INFO_FOR_UPDATE_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        ObjectIdentifier lotID = params.getLotID();
        Validations.check(ObjectIdentifier.isEmpty(lotID), "lotID is null");

        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = IAccessControlCheckInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("call sxEDCDataShowForUpdateInq");
        return Response.createSucc(transactionID, engineerDataCollectionInqService.sxEDCDataShowForUpdateInq(objCommon, lotID));
    }

    @ResponseBody
    @RequestMapping(value = "/edc_data_item_list_by_cj/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.COLLECTED_DATA_ITEM_INQ)
    public Response edcDataItemListByCJInq(@RequestBody Params.EDCDataItemListByCJInqParams params) {
        //======Pre Process======
        String txId = TransactionIDEnum.COLLECTED_DATA_ITEM_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        Validations.check(user == null, "userID can not be null");

        //Step1 - calendar_GetCurrentTimeDR

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = IAccessControlCheckInqService.checkPrivilegeAndGetObjCommon(txId, user, privilegeCheckParams);

        //Step3 - txEDCDataItemListByKeyInq
        Results.EDCDataItemListByCJInqResult result = engineerDataCollectionInqService.sxEDCDataItemListByCJInq(objCommon, params);

        return Response.createSucc(txId, result);
    }
}