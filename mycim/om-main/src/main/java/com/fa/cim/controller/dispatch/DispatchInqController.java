package com.fa.cim.controller.dispatch;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.dispatch.IDispatchInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.dispatch.IDispatchInqService;
import com.fa.cim.service.lot.ILotInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
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
 * @date: 2019/7/30 10:47
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/dispatch")
@Listenable
public class DispatchInqController implements IDispatchInqController {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private ILotInqService lotInqService;

    @Autowired
    private IDispatchInqService dispatchInqService;

    @ResponseBody
    @RequestMapping(value = "/lots_move_in_reserve_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOTS_INFO_FOR_START_RESERVATION_INQ)
    public Response lotsMoveInReserveInfoInq(@RequestBody Params.LotsMoveInReserveInfoInqParams lotsMoveInReserveInfoInqParams) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.LOTS_INFO_FOR_START_RESERVATION_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        User user = lotsMoveInReserveInfoInqParams.getUser();

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        if (CimArrayUtils.isEmpty(lotsMoveInReserveInfoInqParams.getStartCassettes())) {
            return Response.createError(retCodeConfig.getInvalidParameter(), transactionID.getValue());
        }
        for (Infos.StartCassette startCassette : lotsMoveInReserveInfoInqParams.getStartCassettes()) {
            for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                lotIDLists.add(lotInCassette.getLotID());
            }
        }
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessControlCheckInqParams.setEquipmentID(lotsMoveInReserveInfoInqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), user, accessControlCheckInqParams);

        //step 3
        Results.LotsMoveInReserveInfoInqResult result = lotInqService.sxLotsMoveInReserveInfoInq(objCommon, lotsMoveInReserveInfoInqParams);
        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/what_next_npw_standby_lot/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WHAT_NEXT_STANDBY_LOT_INQ)
    public Response whatNextNPWStandbyLotInq(@RequestBody Params.WhatNextNPWStandbyLotInqParams params) {
        //Step-0:Initialize Parameters;
        final String transactionID = TransactionIDEnum.WHAT_NEXT_STANDBY_LOT_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();

        //check input params
        Validations.check(null == user, "the user info is null...");

        //Step-1:Incoming Log Put;
        log.info(" Incoming = {}", transactionID);

        //Step-2:txCalendar_GetCurrentTimeDR;
        log.debug("【Step-2】txCalendar_GetCurrentTimeDR(...)");

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //Step-4:txWhatNextNPWStandbyLotInq;
        log.debug("【Step-4】call-txWhatNextNPWStandbyLotInq(...)");
        //RetCode<Results.WhatNextNPWStandbyLotInqResult> result =  new RetCode<>();
        List<Infos.WhatNextStandbyAttributes> whatNextStandbyAttributesList = dispatchInqService.sxWhatNextNPWStandbyLotInq(objCommon, params);
        Results.WhatNextNPWStandbyLotInqResult whatNextNPWStandbyLotInqResult = new Results.WhatNextNPWStandbyLotInqResult();
        whatNextNPWStandbyLotInqResult.setWhatNextStandbyAttributesList(whatNextStandbyAttributesList);
        //result.setObject(whatNextNPWStandbyLotInqResult);
        // Step-5:Post Process(Generate Output Results/event log put/Set Transaction ID);
        //result.setTransactionID(transactionID);
        return Response.createSucc(transactionID, whatNextNPWStandbyLotInqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/what_next/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WHATNEXT_LOT_LIST_INQ)
    public Response whatNextInq(@RequestBody Params.WhatNextLotListParams whatNextLotListParams) {
        StopWatch stopWatch = new StopWatch();
        log.debug("Performance Test : whatNextInq in");
        //try {
        log.debug("whatNextLotListParams: {}", JSONObject.toJSONString(whatNextLotListParams));

        //step1 - check input params and get schedule from calendar
        User user = whatNextLotListParams.getUser();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(whatNextLotListParams.getEquipmentID());
        stopWatch.start("What's Next - checkPrivilegeAndGetObjCommon");
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(TransactionIDEnum.WHATNEXT_LOT_LIST_INQ.getValue(), user, accessControlCheckInqParams);
        stopWatch.stop();

        stopWatch.start("What's Next - sxWhatNextLotListInfo");
        Results.WhatNextLotListResult result = dispatchInqService.sxWhatNextLotListInfo(objCommon, whatNextLotListParams);
        stopWatch.stop();
        log.debug("Performance Test : whatNextInq out");
        log.debug(stopWatch.prettyPrint());
        return Response.createSucc(TransactionIDEnum.WHATNEXT_LOT_LIST_INQ.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/lots_move_in_reserve_info_for_ib/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOTS_INFO_FOR_START_RESERVATION_FOR_INTERNAL_BUFFER_INQ)
    public Response lotsMoveInReserveInfoForIBInq(@RequestBody Params.LotsMoveInReserveInfoForIBInqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.LOTS_INFO_FOR_START_RESERVATION_FOR_INTERNAL_BUFFER_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step0】check input params
        User user = params.getUser();
        Validations.check(null == user, "the user info is null...");
        ObjectIdentifier equipmentID = params.getEquipmentID();
        Validations.check(ObjectIdentifier.isEmpty(equipmentID), "the equipmentID is null...");

        //step1 - done: get schedule from calendar

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(params.getStartCassettes()); i++) {
            Infos.StartCassette startCassette = params.getStartCassettes().get(i);
            for (int j = 0; j < CimArrayUtils.getSize(startCassette.getLotInCassetteList()); j++) {
                Infos.LotInCassette lotInCassette = startCassette.getLotInCassetteList().get(j);
                if (CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {
                    lotIDList.add(startCassette.getLotInCassetteList().get(j).getLotID());
                }
            }
        }
        accessControlCheckInqParams.setLotIDLists(lotIDList);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);
        //step3 - done: call sxLotInfoInq(...)
        Results.LotsMoveInReserveInfoInqResult result = dispatchInqService.sxLotsMoveInReserveInfoForIBInq(objCommon, equipmentID, params.getStartCassettes());
        Response response = Response.createSucc(transactionID, result);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/auto_dispatch_config/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_AUTO_DISPATCH_CONTROL_INFO_INQ)
    public Response AutoDispatchConfigInq(@RequestBody Params.AutoDispatchConfigInqParams autoDispatchConfigInqParams) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.LOT_AUTO_DISPATCH_CONTROL_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step0】check input params
        User user = autoDispatchConfigInqParams.getUser();
        Validations.check(null == user, "the user info is null...");

        //step1 - done: get schedule from calendar
        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        ObjectIdentifier lotID = autoDispatchConfigInqParams.getLotID();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        if (!ObjectIdentifier.isEmptyWithValue(lotID) && lotID.getValue().indexOf("%") == -1) {
            lotIDs.add(lotID);
        }
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step3 - done: call sxAutoDispatchConfigInq(...)
        Results.AutoDispatchConfigInqResult result = dispatchInqService.sxAutoDispatchConfigInq(objCommon, autoDispatchConfigInqParams);
        Response response = Response.createSucc(transactionID, result);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/virtual_operation_wip_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.VIRTUAL_OPERATION_LOT_LIST_INQ)
    public Response virtualOperationWipListInq(@RequestBody Params.VirtualOperationWipListInqParams params) {
        log.debug("VirtualOperationWipListInqParams: {}", JSONObject.toJSONString(params));
        //init params
        final String transactionID = TransactionIDEnum.VIRTUAL_OPERATION_LOT_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //check input params
        User user = params.getUser();
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);
        //【step3】call sxVirtualOperationWipListInq(...)
        Results.VirtualOperationWipListInqResult result = dispatchInqService.sxVirtualOperationWipListInq(objCommon, params);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_full_auto_config_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_AUTO_3SETTING_LIST_INQ)
    public Response eqpFullAutoConfigListInq(@RequestBody Params.EqpFullAutoConfigListInqInParm eqpFullAutoConfigListInqInParm) {

        String txID = TransactionIDEnum.EQP_AUTO_3SETTING_LIST_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txID);


        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txID, eqpFullAutoConfigListInqInParm.getUser(), accessControlCheckInqParams);

        //step3 - txEqpFullAutoConfigListInq
        Results.EqpFullAutoConfigListInqResult result = dispatchInqService.sxEqpFullAutoConfigListInq(objCommon, eqpFullAutoConfigListInqInParm);

        return Response.createSucc(txID, result);
    }
}