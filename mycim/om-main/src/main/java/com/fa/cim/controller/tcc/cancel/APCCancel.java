package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.apc.IAPCController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/5/7          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/5/7 12:16
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("APCCancel")
@Transactional(rollbackFor = Exception.class)
public class APCCancel implements IAPCController {
    @Override
    public Response APCInterfaceOpsReq(Params.APCIFPointReqParams apcifPointReqParams) {
        return null;
    }
}