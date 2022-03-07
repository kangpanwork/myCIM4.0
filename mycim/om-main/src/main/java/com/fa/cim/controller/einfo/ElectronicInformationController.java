package com.fa.cim.controller.einfo;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.interfaces.electronicInformation.IElectronicInformationController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.einfo.IElectronicInformationService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 17:39
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IElectronicInformationController.class, confirmableKey = "ElectronicInformationConfirm", cancellableKey = "ElectronicInformationCancel")
@RequestMapping("/einfo")
@Listenable
public class ElectronicInformationController implements IElectronicInformationController {

    @Autowired
    private IElectronicInformationService electronicInformationService;
    @Autowired
    private IAccessInqService accessInqService;

    @ResponseBody
    @RequestMapping(value = "/eboard_info_set/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.BULLETIN_BOARD_INFO_REGIST)
    public Response eboardInfoSetReq(@RequestBody Params.EboardInfoSetReqParams params) {
        final String transactionID = TransactionIDEnum.BULLETIN_BOARD_INFO_REGIST.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        log.info("【step2】call txAccessControlCheckInq(...)");
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        log.info("【step3】call sxEboardInfoSetReq");
        electronicInformationService.sxEboardInfoSetReq(objCommon, params.getNoticeTitle(), params.getNoticeDescription());

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_memo_add/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_NOTE_INFO_REGIST_REQ)
    public Response lotMemoAddReq(@RequestBody Params.LotMemoAddReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.LOT_NOTE_INFO_REGIST_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step0】check input params

        ObjectIdentifier lotID = params.getLotID();
        Validations.check(ObjectIdentifier.isEmpty(lotID), "lotID is null");


        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxLotMemoAddReq(...)
        ObjectIdentifier result = electronicInformationService.sxLotMemoAddReq(objCommon, params);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_ope_memo_add/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_OPERATION_NOTE_INFO_REGISTER_REQ)
    public Response lotOpeMemoAddReq(@RequestBody Params.LotOperationNoteInfoRegisterReqParams params) {
        System.out.println(JSONObject.toJSONString(params));
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.LOT_OPERATION_NOTE_INFO_REGISTER_REQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());


        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), params.getUser(), accessControlCheckInqParams);


        //Step-4:txLotOperationNoteInfoRegisterReq;
        log.debug("【Step-4】call-txLotOperationNoteInfoRegisterReq(...)");
        ObjectIdentifier result = electronicInformationService.sxLotOperationNoteInfoRegisterReq(params, objCommon);

        // Step-5:Post Process(Generate Output Results/event log put/Set Transaction ID);
        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_board_work_zone_binding/req", method = RequestMethod.POST)
    @Override
    @CimMapping({TransactionIDEnum.EQP_BOARD_WORK_ZONE_BINDING_REQ, TransactionIDEnum.EQP_BOARD_USER_BINDING_REQ})
    public Response eqpBoardWorkZoneBindingReq(@RequestBody Params.EqpBoardWorkZoneBindingParams eqpBoardWorkZoneBindingParams) {

        // step1 - set current tx id
        User user = eqpBoardWorkZoneBindingParams.getUser();
        Validations.check(Objects.isNull(user), "the user info is null...");

        String functionID = user.getFunctionID();
        String txId;
        if (CimStringUtils.equals(TransactionIDEnum.EQP_BOARD_WORK_ZONE_BINDING_REQ.getValue(), functionID)) {
            txId = TransactionIDEnum.EQP_BOARD_WORK_ZONE_BINDING_REQ.getValue();
        } else {
            txId = TransactionIDEnum.EQP_BOARD_USER_BINDING_REQ.getValue();
        }
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, eqpBoardWorkZoneBindingParams.getUser(), privilegeCheckParams);

        //step3 - call sxEqpBoardWorkZoneBindingReq
        electronicInformationService.sxEqpBoardWorkZoneBindingReq(objCommon, eqpBoardWorkZoneBindingParams);

        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_area_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping({TransactionIDEnum.EQP_AREA_CANCEL_USER_REQ, TransactionIDEnum.EQP_AREA_CANCEL_WORK_ZONE_REQ})
    public Response eqpAreaCancelReq(@RequestBody Params.EqpAreaCancelParams eqpAreaCancelParams) {
        // step1 - set current tx id
        User user = eqpAreaCancelParams.getUser();
        Validations.check(Objects.isNull(user), "the user info is null...");

        String functionID = user.getFunctionID();
        String txId;
        if (CimStringUtils.equals(TransactionIDEnum.EQP_AREA_CANCEL_USER_REQ.getValue(), functionID)) {
            txId = TransactionIDEnum.EQP_AREA_CANCEL_USER_REQ.getValue();
        } else {
            txId = TransactionIDEnum.EQP_AREA_CANCEL_WORK_ZONE_REQ.getValue();
        }
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, eqpAreaCancelParams.getUser(), privilegeCheckParams);

        //step3 - call sxEqpBoardWorkZoneBindingReq
        electronicInformationService.sxEqpAreaCancelReq(objCommon, eqpAreaCancelParams);

        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_area_move/req", method = RequestMethod.POST)
    @Override
    @CimMapping({TransactionIDEnum.EQP_AREA_MOVE_USER_REQ, TransactionIDEnum.EQP_AREA_MOVE_WORK_ZONE_REQ})
    public Response eqpAreaMoveReq(@RequestBody Params.EqpAreaMoveParams eqpAreaMoveParams) {
        // step1 - set current tx id
        User user = eqpAreaMoveParams.getUser();
        Validations.check(Objects.isNull(user), "the user info is null...");

        String functionID = user.getFunctionID();
        String txId;
        if (CimStringUtils.equals(TransactionIDEnum.EQP_AREA_MOVE_USER_REQ.getValue(), functionID)) {
            txId = TransactionIDEnum.EQP_AREA_MOVE_USER_REQ.getValue();
        } else {
            txId = TransactionIDEnum.EQP_AREA_MOVE_WORK_ZONE_REQ.getValue();
        }
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, eqpAreaMoveParams.getUser(), privilegeCheckParams);

        //step3 - call sxEqpAreaMoveReq
        electronicInformationService.sxEqpAreaMoveReq(objCommon, eqpAreaMoveParams);

        return Response.createSucc(txId);
    }


}