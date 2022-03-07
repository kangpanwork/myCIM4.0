package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.customerDefinedAttribute.ICustomerDefinedAttributeController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 10:25
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("CustomerDefinedAttributeConfirm")
@Transactional(rollbackFor = Exception.class)
public class CustomerDefinedAttributeConfirm implements ICustomerDefinedAttributeController {
    @Override
    public Response cdaValueUpdateReq(Params.CDAValueUpdateReqParams params) {
        return null;
    }
}