package com.fa.cim.controller.access;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.accessManagement.IAccessInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @author Nyx
 * @since 2019/7/30 13:41
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/access")
@Listenable
public class AccessInqController implements IAccessInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @ResponseBody
    @RequestMapping(value = "/basic_user_info/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.USER_DESC_INQ)
    @Override
    public Response basicUserInfoInq(@RequestBody Params.BasicUserInfoInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.USER_DESC_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call txBankListInq(...)
        Results.BasicUserInfoInqResult result = accessInqService.sxBasicUserInfoInq(objCommon, params.getUserID());
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/all_user_info/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.USER_LIST_INQ)
    @Override
    public Response allUserInfoInq(@RequestBody Params.AllUserInfoInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.USER_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        //【step3】call txBankListInq(...)
        List<Results.BasicUserInfoInqResult> allUserInfoInqResults = accessInqService.sxAllUserInfoInq();
        return Response.createSucc(allUserInfoInqResults);
    }

    @ResponseBody
    @RequestMapping(value = "/login_check/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.LOG_ON_CHECK_REQ)
    public Response loginCheckInq(@RequestBody Params.LoginCheckInqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.LOG_ON_CHECK_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】main process
        return Response.createSucc(transactionID, accessInqService.sxLoginCheckInq(objCommon, params));
    }

    @ResponseBody
    @RequestMapping(value = "/access_control_check/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PRIVILEGE_CHECK_REQ)
    @Override
    public Response accessControlCheckInq(@RequestBody Params.AccessControlCheckInqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.PRIVILEGE_CHECK_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setStockerID(params.getStockerID());
        accessControlCheckInqParams.setDepartmentSectionCheckCodes(params.getDepartmentSectionCheckCodes());
        accessControlCheckInqParams.setLotIDLists(params.getLotIDLists());
        accessControlCheckInqParams.setUser(params.getUser());
        accessControlCheckInqParams.setMachineRecipeIDList(params.getMachineRecipeIDList());
        accessControlCheckInqParams.setRouteIDList(params.getRouteIDList());
        accessControlCheckInqParams.setProductIDList(params.getProductIDList());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, null);
    }
}