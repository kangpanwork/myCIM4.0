package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.dynamicOperationControl.IDynamicOperationController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 16:39
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("DynamicOperationControlCancel")
@Transactional(rollbackFor = Exception.class)
public class DynamicOperationControlCancel implements IDynamicOperationController {
    @Override
    public Response docLotRemoveReq(Params.DOCLotRemoveReqParams params) {
        return null;
    }

    @Override
    public Response docLotActionReq(Params.DOCLotActionReqParams params) {
        return null;
    }

    @Override
    public Response docLotInfoSetReq(Params.DOCLotInfoSetReqParams params) {
        return null;
    }
}