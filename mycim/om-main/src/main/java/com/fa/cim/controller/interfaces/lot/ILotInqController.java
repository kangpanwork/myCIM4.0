package com.fa.cim.controller.interfaces.lot;

import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 15:27
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ILotInqController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/6/26         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/6/26 17:41
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response HoldLotListInq(Params.HoldLotListInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/19         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/19 11:05
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response waferAliasInfoInq(Params.WaferAliasInfoInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-11-23                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-11-23 16:18
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response waferListInLotFamilyInq(Params.WaferListInLotFamilyInqParams params);

    /**
     * description:
     * The method use to define the MultiPathListInq controller.
     * transaction ID: OLOTQ013
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/10        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/10 10:16
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response multiPathListInq(Params.MultiPathListInqParams multiPathListInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/6        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/11/6 15:24
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response dynamicPathListInq(Params.DynamicPathListInqParams params);

    /**
     * description:
     * The method use to define the LotFamilyInq Controller.
     * transaction ID: OLOTQ004
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/10        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/10 10:16
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotFamilyInq(Params.LotFamilyInqParams lotFamilyInqParams);

    /**
     * description:
     * <p>LotFuturePctrlDetailInfoInq .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/4        ********             Scott               create file
     *
     * @author: Scott
     * @date: 2018/12/4 13:37
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotFuturePctrlDetailInfoInq(Params.FutureActionDetailInfoInqParams futureActionDetailInfoInqParams);

    /**
     * description:
     * The method use to define the LotInfoByWaferInqController.
     * transaction ID: OLOTQ005
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/15       ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/15 11:26
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotInfoByWaferInq(Params.LotInfoByWaferInqParams lotInfoByWaferInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/3         OLOTQ001              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/5/3 15:21
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotInfoInq(Params.LotInfoInqParams lotInfoInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/30        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/5/30 13:14
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotListByCarrierInq(Params.LotListByCarrierInqParams lotListByCarrierInqParams);

    /**
     * description:
     * The method use to define the LotListByCJInqController.
     * transaction ID: OLOTQ007
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/22        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/22 17:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotListByCJInq(Params.LotListByCJInqParams lotListByCJInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/8        ********            Bear         create file
     *
     * @author: Bear
     * @date: 2018/4/8 10:19
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotListInq(Params.LotListInqParams lotListInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/8/27         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/8/27 10:34
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response LotOperationSelectionInq(Params.LotOperationSelectionInqParams params);

    /**
     * description:
     * The method use to define the AllMfgLayerListInqController.
     * transaction ID: OLOTQ022
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/23        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/23 15:40
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response allMfgLayerListInq(User requestUser);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/23       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/23 17:56
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotOperationSelectionForMultipleLotsInq(Params.LotOperationSelectionForMultipleLotsInqParams params);

    /**
     * description:
     * The method use to define the AllProcessStepListInqController.
     * transaction ID: OCONQ003
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/24        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/24 11:26
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response allProcessStepListInq(Params.UserParams requestedUser);

    /**
     * description:
     * The method use to define the MainProcessFlowListInqController.
     * transaction ID: OLOTQ026
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/24        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/24 11:26
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response mainProcessFlowListInq(Params.MainProcessFlowListInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/25       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/25 14:22
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response processFlowOperationListForLotInq(Params.ProcessFlowOperationListForLotInqParams params);

    /**
     * description:
     * The method use to define the ProcessFlowOperationListInqController.
     * transaction ID: ODRBQ013
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/16        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/16 15:38
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response processFlowOperationListInq(Params.ProcessFlowOperationListInqParams processFlowOperationListInqParams);

    Response allLotTypeListInq(Params.AllLotTypeListInqParams params);
}