package com.fa.cim.controller.interfaces.electronicInformation;

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
 * @date: 2019/7/29 17:26
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IElectronicInformationController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/2          ********            Nyx                create file
     *
     * @author: Nyx
     * @date: 2019/7/2 10:20
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eboardInfoSetReq(Params.EboardInfoSetReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/23       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/23 11:12
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotMemoAddReq(Params.LotMemoAddReqParams params);

    /**
     * description:
     * The method use to define the LotOperationNoteInfoRegisterReqController.
     * transaction ID: OINFW003
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/24        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/24 09:52
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotOpeMemoAddReq(Params.LotOperationNoteInfoRegisterReqParams params);

    /**
     * description: eqp board work zone binding req
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpBoardWorkZoneBindingParams - eqpBoardWorkZoneBindingParams
     * @return rest
     * @author YJ
     * @date 2021/2/19 0019 19:23
     */
    Response eqpBoardWorkZoneBindingReq(Params.EqpBoardWorkZoneBindingParams eqpBoardWorkZoneBindingParams);

    /**
     * description: eqp area cancel req
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpAreaCancelParams - eqp area cancel params
     * @return reset
     * @author YJ
     * @date 2021/2/20 0020 10:08
     */
    Response eqpAreaCancelReq(Params.EqpAreaCancelParams eqpAreaCancelParams);

    /**
     * description: eqp area move params
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpAreaMoveParams - eqp area move params
     * @return rest
     * @author YJ
     * @date 2021/2/20 0020 14:58
     */
    Response eqpAreaMoveReq(Params.EqpAreaMoveParams eqpAreaMoveParams);

}