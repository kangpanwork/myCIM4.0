package com.fa.cim.controller.interfaces.flowBatch;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 13:49
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IFlowBatchController {
    /**
     * description: FlowBatchByManualActionReqController
     * <p></p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/14                            Scott                create file
     *
     * @author: Scott
     * @date: 2018/12/14 15:08
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response flowBatchByManualActionReq(Params.FlowBatchByManualActionReqParam param);

    /**
     * description:
     * <p>FlowBatchLotRemoveReqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/3        ********             Scott               create file
     *
     * @author: Scott
     * @date: 2019/1/3 17:12
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response flowBatchLotRemoveReq(Params.FlowBatchLotRemoveReq params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/3                            Wind                create file
     *
     * @author: Wind
     * @date: 2019/1/3 16:15
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response autoFlowBatchByManualActionReq(Params.FlowBatchByAutoActionReqParams params);

    /**
     * description:
     * <p>EqpMaxFlowbCountModifyReqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * 2018/12/18         ********             ZQI               create file
     *
     * @author: ZQI
     * @date: 2018/12/18 11:24
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpMaxFlowbCountModifyReq(Params.EqpMaxFlowbCountModifyReqParams params);

    /**
     * description:
     * <p>EqpReserveCancelForflowBatchReqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * 2018/12/19         ********             ZQI               create file
     *
     * @author ZQI
     * @date 2018/12/19 17:20
     * @copyright 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpReserveCancelForflowBatchReq(Params.EqpReserveCancelForflowBatchReqParams params);

    /**
     * description:
     * <p>EqpReserveForFlowBatchReqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * 2018/12/21         ********             ZQI               create file
     *
     * @author ZQI
     * @date 2018/12/21 13:16
     * @copyright 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpReserveForFlowBatchReq(Params.EqpReserveForFlowBatchReqParam params);

    /**
     * description:
     * <p>This function checks whether Lots existing in FlowBatch section can be located or not.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/3                            Wind                create file
     *
     * @author: Wind
     * @date: 2019/1/3 10:12
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response flowBatchCheckForLotSkipReq(Params.FlowBatchCheckForLotSkipReqParams params);

    /**
     * description:
     * <p>This function generates a new flowbatch group from FloatingBatch Lots and reserves it to eqp.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/5                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/12/5 09:49
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reFlowBatchByManualActionReq(Params.ReFlowBatchByManualActionReqParam param);
}