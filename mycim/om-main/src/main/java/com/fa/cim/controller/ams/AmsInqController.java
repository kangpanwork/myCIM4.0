package com.fa.cim.controller.ams;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.ams.IAmsInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.ams.IAmsInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * description:
 * <p>AmsInpController .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/7/27/027   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/7/27/027 17:36
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/ams")
@Listenable
public class AmsInqController implements IAmsInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IAmsInqService amsInqService;

    @ResponseBody
    @RequestMapping(value = "/oms_msg_info/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.OMS_MSG_INFO_INQ)
    @Override
    public Response OmsMsgInfoInq(@RequestBody Params.OmsMsgInfoInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.OMS_MSG_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        //【step3】call txOmsMsgInfoInq(...)
        Results.OmsMsgInqResult omsMsgInqResult = amsInqService.sxOmsMsgInfoInq(objCommon, params);
        return Response.createSucc(transactionID, omsMsgInqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/users_for_oms/inq", method = RequestMethod.POST)
    @Override
    public Response usersForOMS() {
        return Response.createSucc("AMSUS001", amsInqService.usersForOMS());
    }

    @ResponseBody
    @RequestMapping(value = "/user_groups_for_oms/inq", method = RequestMethod.POST)
    @Override
    public Response userGroupsForOMS() {
        return Response.createSucc("AMSUG001", amsInqService.userGroupsForOMS());
    }

    @ResponseBody
    @RequestMapping(value = "/alarm_category/inq", method = RequestMethod.POST)
    @Override
    public Response alarmCategory() {
        return Response.createSucc("AMSAC001", amsInqService.alarmCategory());
    }


}
