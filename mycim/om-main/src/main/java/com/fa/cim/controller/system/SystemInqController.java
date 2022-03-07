package com.fa.cim.controller.system;

import com.fa.cim.common.constant.EnvConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.systemConfig.ISystemInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.system.ISystemInqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;
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
 * @author: Nyx
 * @date: 2019/7/30 14:33
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/sys")
@Listenable
public class SystemInqController implements ISystemInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private ISystemInqService systemInqService;

    @ResponseBody
    @RequestMapping(value = "/code_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CODE_LIST_INQ)
    public Response codeSelectionInq(@RequestBody Params.CodeSelectionInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.CODE_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxCodeSelectionInq(...)
        List<Infos.CodeInfo> result = systemInqService.sxCodeSelectionInq(objCommon, params.getCategory());
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/oms_env_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENV_VARIABLE_INFO_INQ)
    public Response omsEnvInfoInq(@RequestBody Params.OMSEnvInfoInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.ENV_VARIABLE_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step2】call txOMSEnvInfoInqInq(...)
        Results.OMSEnvInfoInqResult result = systemInqService.sxOMSEnvInfoInq(objCommon);
        Infos.EnvVariableList envVariableList = new Infos.EnvVariableList();
        envVariableList.setEnvName(EnvConst.PPT_TRACE_INCOMING);
        envVariableList.setEnvValue("0");

        List<Infos.EnvVariableList> svcEnvVariableList = new ArrayList<>();
        svcEnvVariableList.add(envVariableList);
        result.setSvcEnvVariableList(svcEnvVariableList);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/reason_code_list_by_category/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.REASON_LIST_INQ)
    public Response reasonCodeListByCategoryInq(@RequestBody Params.ReasonCodeListByCategoryInqParams params) {
        final String transactionID = TransactionIDEnum.REASON_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        String codeCategory = params.getCodeCategory();
        // step0 - check input params
        Asserts.check(null != codeCategory, "the codeCategory info is null...");
        //objCommon.setUser(user);
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        List<Infos.ReasonCodeAttributes> result = systemInqService.sxReasonCodeListByCategoryInq(objCommon, codeCategory);
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, result);
    }
}