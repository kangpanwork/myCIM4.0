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
 * 2019/7/26          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/26 15:51
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDurableController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-11-01                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-11-01 10:35
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response durableSetReq(Params.DurableSetReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/2/1       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2019/2/1 10:01
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response multiDurableStatusChangeReq(Params.MultiDurableStatusChangeReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-10-29                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-10-29 16:01
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticleAllInOutRpt(Params.ReticleAllInOutRptParams params);

    /**
     * description:
     * <p>MultipleReticlePodStatusChangeRptController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/29        ********             PlayBoy               create file
     *
     * @author: PlayBoy
     * @date: 2018/10/29 11:17
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response multipleReticlePodStatusChangeRpt(Params.MultipleReticlePodStatusChangeRptParam params);

    /**
     * description:
     * <p>ReticlePodMaintInfoUpdateReqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/28        ********             PlayBoy               create file
     *
     * @author: PlayBoy
     * @date: 2018/10/28 12:05
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodMaintInfoUpdateReq(Params.ReticlePodMaintInfoUpdateReqParam params);

    /**
     * description:TxReticlePodTransferStatusChangeRpt__160
     * <p>ReticlePodTransferStatusChangeRptController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/31        ********             PlayBoy               create file
     *
     * @author: PlayBoy
     * @date: 2018/10/31 17:54
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodTransferStatusChangeRpt(Params.ReticlePodTransferStatusChangeRptParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/25          ********            Nyx                create file
     *
     * @author: Nyx
     * @date: 2019/6/25 10:02
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticleSortRpt(Params.ReticleSortRptParam param);

    /**
     * description:TxCarrierStatusChangeRpt
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
    Response carrierStatusChangeRpt(Params.CarrierStatusChangeRptInParams carrierStatusChangeRptInParams);

    /**
     * description:TxMultipleCarrierStatusChangeRpt
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
    Response multipleCarrierStatusChangeRpt(Params.MultipleCarrierStatusChangeRptParms multipleCarrierStatusChangeRptParms);

    /**
     * description:TxCarrierUsageCountResetReq
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
    Response carrierUsageCountResetReq(Params.CarrierUsageCountResetReqParms carrierUsageCountResetReqParms);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/22 11:09:06
     */
    Response reticleStatusChangeRpt(Params.ReticleStatusChangeRptParams reticleStatusChangeRptParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/26 15:23:34
     */
    Response multipleReticleStatusChangeRpt(Params.MultipleReticleStatusChangeRptParams multipleReticleStatusChangeRptParams);

    /**
     * description: TxReticleUsageCountResetReq
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/26 15:23:34
     */
    Response reticleUsageCountResetReq(Params.ReticleUsageCountResetReqParams reticleUsageCountResetReqParams);

    /**
     * description: TxReticleTransferStatusChangeRpt
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/26 15:23:34
     */
    Response reticleTransferStatusChangeRpt(Params.ReticleTransferStatusChangeRptParams reticleTransferStatusChangeRptParams);

    Response reticlePodInvUpdateRpt(Params.ReticlePodInvUpdateRptParam params);

    Response drbBankInReq(Params.DurableBankInReqInParam durableBankInReqInParam);

    Response drbBankInCancelReq(Params.DurableBankInCancelReqInParam durableBankInCancelReqInParam);

    Response drbBankMoveReq(Params.DurableBankMoveReqParam durableBankMoveReqParam);

    Response drbDeleteReq(Params.DurableDeleteParam durableDeleteParam);


    /**
     * This function notifies system that Durables actual Operation has started on the Equipment.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/18 15:33
     */
    Response drbMoveInReq(Params.DurableOperationStartReqInParam durableOperationStartReqInParam);

    Response drbMoveInForIBReq(Params.DurableOperationStartReqInParam durableOperationStartReqInParam);

    /**
     * This function performs Hold/Hold release to durable based on ProcessLagTime.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/18 15:36
     */
    Response drbLagTimeActionReq(Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm);

    /**
     * The function registers the Post Processing Action Queue with the Queue Table,
     * in accordance with the Pattern Table defining the sequence of transactions.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/18 15:41
     */
    Response drbPostTaskRegistReq(Params.DurablePostProcessActionRegistReqInParam durablePostProcessActionRegistReqInParam);

    /**
     * This function deletes Durable PFX of specified Durables.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/18 15:56
     */
    Response drbPrfCxDeleteReq(Params.DurablePFXDeleteReqInParam durablePFXDeleteReqInParam);

    /**
     * This function creates Durable PFX of specified Durables.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/18 15:58
     */
    Response drbPrfCxCreateReq(Params.DurablePFXCreateReqInParam durablePFXCreateReqInParam);

    /**
     * This function cancels the Operation Start for the started Durables.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/18 15:59
     */
    Response drbMoveInCancelReq(Params.DurableOperationStartCancelReqInParam durableOperationStartCancelReqInParam);

    Response drbMoveInCancelForIBReq(Params.DurableOperationStartCancelReqInParam durableOperationStartCancelReqInParam);

    Response drbBankInByPostTaskReq(Params.DurableBankInByPostProcReqInParam durableBankInByPostProcReqInParam);

    Response drbCJStatusChangeReq(Params.DurableControlJobManageReqInParam durableControlJobManageReqInParam);

    Response drbPassThruReq(Params.DurableGatePassReqInParam durableGatePassReqInParam);

    /**
     * This function notifies system that Durables actual Operation has completed on the Equipment.
     *
     * @param durableOpeCompReqInParam {@link Params.DurableOpeCompReqInParam}
     * @return {@link Response} excludes {@link com.fa.cim.dto.Results.DurableOpeCompReqResult}
     * @version 1.0
     * @author ZQI
     * @date 2020/6/28 15:11
     */
    Response drbMoveOutReq(Params.DurableOpeCompReqInParam durableOpeCompReqInParam);

    Response drbMoveOutForIBReq(Params.DurableOpeCompReqInParam durableOpeCompReqInParam);

    Response drbSkipReq(Params.DurableOpeLocateReqInParam durableOpeLocateReqInParam);
    
    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/7 18:15                     Nyx                Create
     *       
     * @author Nyx
     * @date 2020/7/7 18:15
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response holdDrbReleaseReq(Params.HoldDurableReleaseReqParams params);
    
    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/9 16:59                     Nyx                Create
     *       
     * @author Nyx
     * @date 2020/7/9 16:59
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response drbReworkCancelReq(Params.ReworkDurableCancelReqParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strReworkDurableReqInParam
     * @return com.fa.cim.common.support.Response
     * @exception 
     * @author ho
     * @date 2020/7/10 11:01
     */
    Response drbReworkReq(@RequestBody Params.ReworkDurableReqInParam strReworkDurableReqInParam);
    
    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/13 16:25                     Nyx                Create
     *       
     * @author Nyx
     * @date 2020/7/13 16:25
     * @param holdDurableReqParams -
     * @return com.fa.cim.common.support.Response
     */
    Response holdDrbReq(Params.HoldDurableReqParams holdDurableReqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strDurableForceOpeLocateReqInParam
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/7/17 15:29
     */
    Response drbForceSkipReq(@RequestBody Params.DurableForceOpeLocateReqInParam strDurableForceOpeLocateReqInParam);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strStartDurablesReservationCancelReqInParam
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/7/6 15:01
     */
    Response drbMoveInReserveCancelReq(@RequestBody Params.StartDurablesReservationCancelReqInParam strStartDurablesReservationCancelReqInParam);

    Response drbMoveInReserveCancelForIBReq(@RequestBody Params.StartDurablesReservationCancelReqInParam strStartDurablesReservationCancelReqInParam);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strStartDurablesReservationReqInParam
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/7/2 11:05
     */
    Response drbMoveInReserveReq(@RequestBody Params.StartDurablesReservationReqInParam strStartDurablesReservationReqInParam);
    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strStartDurablesReservationReqInParam
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/7/2 11:05
     */
    Response drbMoveInReserveForIBReq(@RequestBody Params.StartDurablesReservationReqInParam strStartDurablesReservationReqInParam);

    /**
     * durableJobStatusChangeReq
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/9/10 15:06
     */
    Response durableJobStatusChangeReq(Params.DurableJobStatusChangeReqParams params);

    /**
     * durableJobStatusChangeRpt
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/9/10 17:55
     */
    Response durableJobStatusChangeRpt(Params.DurableJobStatusChangeRptParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strReworkDurableWithHoldReleaseReqInParam
     * @return com.fa.cim.common.support.Response
     * @exception 
     * @author ho
     * @date 2020/10/19 13:26
     */
    Response reworkDrbWithHoldReleaseReq(Params.ReworkDurableWithHoldReleaseReqInParam strReworkDurableWithHoldReleaseReqInParam);


    /**
     * description: This function sends request to change reticle pod port access mode.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 10:32
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodPortAccessModeChgReq(Params.EqpRSPPortAccessModeChangeReqParams params);

    /**
     * description: This function requests that reticle should be retrieved from equipment when equipment is off-line mode.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 10:40
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticleRetrieveByOffline(Params.ReticleOfflineRetrieveReqParams params);

    /**
     * description: This function is sent from EAP to report reticle pod port access mode change.
     *              OMS server will update the change to database if it is valid.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 10:54
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodPortStatusChgRpt(Params.EqpRSPPortStatusChangeRpt params);


    /**
     * description: This function requests recovery of Reticle.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 11:02
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticleRetrieveReq(Params.ReticleRetrieveReqParams params);

    /**
     * description: This function will send request to store a reticle to equipment or bare reticle stocker when offline.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 11:08
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticleStoreByOffline(Params.ReticleOfflineStoreReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 11:16
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticleRetrieveRpt(Params.ReticleRetrieveRptParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 11:16
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticleStoreReq(Params.ReticleStoreReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 11:19
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticleStoreRpt(Params.ReticleStoreRptParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 11:23
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodUnloadRpt(Params.ReticlePodUnloadingRptParams params);


    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 11:27
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodLoadRpt(Params.ReticlePodLoadingRptParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 13:08
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response bRSOnlineModeChgReq(Params.BareReticleStockerOnlineModeChangeReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 13:12
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodInvUpdateReq(Params.ReticlePodInventoryReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 13:16
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodLoadByOfflineReq(Params.ReticlePodOfflineLoadingReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 13:21
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticlePodUnloadByOfflineReq(Params.ReticlePodOfflineUnloadingReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 13:24
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reticleInvUpdateReq(Params.ReticleInventoryReqParams params);
}