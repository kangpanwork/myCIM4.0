package com.fa.cim.controller.interfaces.fsm;

import com.fa.cim.common.support.Response;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 16:04
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IFutureSplitMergeInqController {
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/20                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/20 下午 3:40
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response fsmLotInfoInq(com.fa.cim.fsm.Params.FSMLotInfoInqParams params);

    /**
     * description:TxPSMLotDefinitionListInq
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/19                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/19 10:59
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response fsmLotDefinitionListInq(com.fa.cim.fsm.Params.FSMLotDefinitionListInqParams params);
}