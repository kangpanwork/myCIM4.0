package com.fa.cim.controller.pcs;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.processControlScript.IProcessControlScriptInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.pcs.IProcessControlScriptInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/7/31 16:15
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/pcs")
@Listenable
public class ProcessControlScriptInqController implements IProcessControlScriptInqController {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IProcessControlScriptInqService processControlScriptInqService;

    @ResponseBody
    @RequestMapping(value = "/pcs_parameter_value/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.USER_PARAMETER_VALUE_INQ)
    public Response pcsParameterValueInq(@RequestBody Params.PCSParameterValueInqParams params) {
        Validations.check(null == params, retCodeConfig.getInvalidInputParam());
        final String txId = TransactionIDEnum.USER_PARAMETER_VALUE_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step3 - txPCSParameterValueInq
        List<Infos.UserParameterValue> result = processControlScriptInqService.sxPCSParameterValueInq(objCommon, params);
        return Response.createSucc(txId, result);
    }
}