package com.fa.cim.controller.cda;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.customerDefinedAttribute.ICustomerDefinedAttributeController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.cda.ICustomerDefinedAttributeService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
 * @since 2019/7/31 10:26
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = ICustomerDefinedAttributeController.class, confirmableKey = "CustomerDefinedAttributeConfirm", cancellableKey = "CustomerDefinedAttributeCancel")
@RequestMapping("/cda")
@Listenable
public class CustomerDefinedAttributeController implements ICustomerDefinedAttributeController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private ICustomerDefinedAttributeService customerDefinedAttributeService;

    @ResponseBody
    @RequestMapping(value = "/cda_value_update/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.USER_DATA_UPDATE_REQ)
    public Response cdaValueUpdateReq(@RequestBody Params.CDAValueUpdateReqParams params) {

        final String txId = TransactionIDEnum.USER_DATA_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step3 - txCDAValueUpdateReq
        List<Results.UserDataUpdateResult> result = customerDefinedAttributeService.sxCDAValueUpdateReq(objCommon, params);

        return Response.createSucc(txId, result);
    }
}