package com.fa.cim.controller.interfaces.durable;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 10:52
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDurableInqController {
    /**
     * description:
     * <p>ReticlePodListWithBasicInfoInqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/31        ********             PlayBoy               create file
     *
     * @author: PlayBoy
     * @date: 2018/10/31 10:55
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodListWithBasicInfoInq(Params.ReticlePodListWithBasicInfoInqParams params);

    /**
     * description:TxReticlePodListInq__170
     * <p></p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/6                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/11/6 10:50
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodListInq(Params.ReticlePodListInqParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/7                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/11/7 17:21
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodDetailInfoInq(Params.ReticlePodDetailInfoInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/14       ********              lightyh             create file
     *
     * @author: lightyh
     * @date: 2019/6/14 13:08
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticleStocInfoInq(Params.ReticleStocInfoInqParams params);

    /**
     * description:TxDurableStatusSelectionInq
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-18                           ho             create file
     * <p>
     * return:
     *
     * @author ho
     * @date: 2018-09-18 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response durableStatusSelectionInq(Params.DurableStatusSelectionInqInParms durableStatusSelectionInqInParms);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/27       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/11/27 14:04
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response durableSubStatusSelectionInq(Params.DurableSubStatusSelectionInqParams params);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/17 10:15:29
     */
    Response reticleListInq(Params.ReticleListInqParams reticleListInqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/26 14:13:12
     */
    Response reticleDetailInfoInq(Params.ReticleDetailInfoInqParams reticleDetailInfoInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/25        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/5/25 9:50
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierListInq(Params.CarrierListInqParams carrierListInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Panda
     * @date: 2018/5/11
     */
    Response carrierDetailInfoInq(Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams);

    /**
     * description:TxCarrierBasicInfoInq
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-18                           ho             create file
     * <p>
     * return:
     *
     * @author ho
     * @date: 2018-09-18 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierBasicInfoInq(Params.CarrierBasicInfoInqParms carrierBasicInfoInqParms);

    /**
     * description:
     * The method use to define the AllReticleGroupListInqController.
     * transaction ID: ODRBQ012
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/15        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/16 13:56
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response allReticleGroupListInq(@RequestBody Params.UserParams allReticleGroupListInqParams);

    /**
     * description:
     * The method use to define the AllReticleListInqController.
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
    Response allReticleListInq(@RequestBody Params.UserParams userParams);

    /**
     * This function returns Information of Start Carrier to Start Reservation for Durable.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/17 11:29
     */
    Response drbInfoForMoveInReserveInq(@RequestBody Params.DurablesInfoForStartReservationInqInParam param);

    /**
     * This function returns Information of Start Durable.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/17 12:40
     */
    Response drbInfoForMoveInInq(@RequestBody Params.DurablesInfoForOpeStartInqInParam param);

    /**
     * This function returns the list of Operation Numbers for Durable
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/17 12:42
     */
    Response drbStepListInq(@RequestBody Params.DurableOperationListInqInParam param);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param param
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/6/22 10:20
     */
    Response drbStepListFromHistoryInq(Params.DurableOperationListFromHistoryInqInParam param);

    Response drbStepHistoryInq(
            @RequestBody Params.DurableOperationHistoryInqInParam         strDurableOperationHistoryInqInParam);

    Response drbControlJobListInq(Params.DurableControlJobListInqParams params);

    Response drbHoldListInq(Params.DurableHoldListInqInParam params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strRouteOperationListForDurableInqInParam
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/7/14 11:10
     */
    Response processFlowOperationListForDrbInq(@RequestBody Params.RouteOperationListForDurableInqInParam strRouteOperationListForDurableInqInParam);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/16 15:02                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/7/16 15:02
     * @param connectedDurableRouteListInqParams -
     * @return com.fa.cim.common.support.Response
     */
    Response multiDrbPathListInq(Params.ConnectedDurableRouteListInqParams connectedDurableRouteListInqParams);


    /**
     * description:
     * 2020/07/21  Auto lot start 需要call 的接口 临时自定义
     *
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/21 17:46                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/7/21 17:46
     * @param  -
     * @return com.fa.cim.common.support.Response
     */

    Response availableCarrierInq();

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strWhatNextDurableListInqInParam
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/6/30 11:17
     */
    Response drbWhatNextListInq(@RequestBody Params.WhatNextDurableListInqInParam strWhatNextDurableListInqInParam);


    /**
     * durableJobStatusSelectionInq
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/9/10 10:55
     */
    Response durableJobStatusSelectionInq(@RequestBody Params.DurableJobStatusSelectionInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/19 23:30                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/10/19 23:30
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response ErackPodInfoInq(Params.ErackPodInfoInqParams params);

    /**
     * description: This function will retrieve reticle pod stocker information.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 10:49
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodStockerInfoInq(Params.ReticlePodStockerInfoInqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 11:31
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response bRSInfoInq(Params.BareReticleStockerInfoInqParams params);

    /**
     * description: Select bank from durable where bank in can be performed
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/19 11:27                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/19 11:27
     * @param bankListInqParams -
     * @return com.fa.cim.common.support.Response
     */
    Response drbBankInListInq(Params.BankListInqParams bankListInqParams);
}
