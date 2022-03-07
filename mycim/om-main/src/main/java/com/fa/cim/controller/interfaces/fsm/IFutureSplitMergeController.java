package com.fa.cim.controller.interfaces.fsm;

import com.fa.cim.common.support.Response;

public interface IFutureSplitMergeController {

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/21                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/21 下午 4:11
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response fsmLotRemoveReq(com.fa.cim.fsm.Params.FSMLotRemoveReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/30                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/30 11:21
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response fsmLotActionReq(com.fa.cim.fsm.Params.FSMLotActionReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/25                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/25 13:52
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response fsmLotInfoSetReq(com.fa.cim.fsm.Params.FSMLotInfoSetReqParams params);
}
