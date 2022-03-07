package com.fa.cim.controller.interfaces.flowBatch;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 11:12
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IFlowBatchInqController {
    /**
     * description:
     * <p>FloatingBatchListInqController .<br/></p>
     * <p>
     * change history:
     * date  defect  person  comments
     * ------------------------------------------------------------
     * 2018/12/17 ******** Paladin
     * <p>
     * create file
     *
     * @author: Paladin
     * @date: 2018/12/17 16:34
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All
     * Rights Reserved.
     */
    Response floatingBatchListInq(Params.FloatingBatchListInqParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/1/4 13:48:24
     */
    Response flowBatchLotSelectionInq(Params.FlowBatchLotSelectionInqParam params);

    /**
     * description:
     * <p>FlowBatchInfoInqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/13        ********             Scott               create file
     *
     * @author: Scott
     * @date: 2018/12/13 10:51
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response flowBatchInfoInq(Params.FlowBatchInfoInqParams params);

    /**
     * description:
     * <p>FlowBatchLostLotsController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/11        ********             Scott               create file
     *
     * @author: Scott
     * @date: 2018/12/11 17:07
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response flowBatchStrayLotsListInq(Params.FlowBatchStrayLotsListInqParams params);
}