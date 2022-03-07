package com.fa.cim.controller.cda;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.customerDefinedAttribute.ICustomerDefinedAttributeInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.cda.ICustomerDefinedAttributeInqService;
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
 * @since 2019/7/31 10:29
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/cda")
@Listenable
public class CustomerDefinedAttributeInqController implements ICustomerDefinedAttributeInqController {
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private ICustomerDefinedAttributeInqService customerDefinedAttributeInqService;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @ResponseBody
    @RequestMapping(value = "/cda_value/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.USER_DATA_INQ)
    public Response cdaValueInq(@RequestBody Params.CDAValueInqParams params) {

        final String txId = TransactionIDEnum.USER_DATA_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);


        //step3 - txCDAValueInq__101
        List<Infos.UserData> result = null;
        String returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
        try {
            result = customerDefinedAttributeInqService.sxCDAValueInq(objCommon, params);
        } catch (ServiceException e) {
            if (!(Validations.isEquals(retCodeConfig.getNotFoundUData(), e.getCode() == null ? 0 : e.getCode()) && CimStringUtils.equals(returnOK, "1"))) {
                log.info("No data found. ");
                throw e;
            }
        }
        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/cda_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.USER_DEFINED_ATTRIBUTE_INFO_INQ)
    public Response cdaInfoInq(@RequestBody Params.CDAInfoInqParams params) {
        // init params
        final String txID = TransactionIDEnum.USER_DEFINED_ATTRIBUTE_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(txID);

        // Step1 privilege check
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txID, params.getUser(), accessControlCheckInqParams);


        // Step3 Step3 call txCDAInfoInq(...)
        Results.CDAInfoInqResult result = customerDefinedAttributeInqService.sxCDAInfoInq(objCommon, params);

        return Response.createSucc(txID, result);
    }
}