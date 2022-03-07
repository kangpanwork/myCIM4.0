package com.fa.cim.controller.interfaces.equipment;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import com.fa.cim.eqp.carrierout.CarrierOutPortReqParams;
import com.fa.cim.eqp.carrierout.CarrierOutReqParams;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 10:17
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEquipmentController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-19                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-09-19 15:43
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response chamberStatusChangeReq(Params.ChamberStatusChangeReqPrams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-20                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-09-20 17:17
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response chamberStatusChangeRpt(Params.ChamberStatusChangeRptPrams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/25       ********              Lin             create file
     *
     * @author: Lin
     * @date: 2018/12/25 10:56
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response EqpModeChangeReq(Params.EqpModeChangeReqPrams params);

    /**
     * description:TxEqpMemoAddReq__160
     * <p>This function registers the eqp notes that are claimed by an operator.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/23                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/10/23 10:25
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpMemoAddReq(Params.EqpMemoAddReqParams eqpMemoAddReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-10-14                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-10-14 16:13
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response portStatusChangeRpt(Params.PortStatusChangeRptParam params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/4       ********              Lin             create file
     *
     * @author: Lin
     * @date: 2019/1/4 15:14
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response EqpEAPStatusSyncReq(Params.EqpEAPStatusSyncReqPrams params);

    /**
     * description:
     * eqp status change request controller.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/6/26        ********             PlayBoy               create file
     *
     * @author: PlayBoy
     * @date: 2018/6/26 16:52
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpStatusChangeReq(Params.EqpStatusChangeReqParams eqpStatusChangeReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/20        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/11/20 13:30
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpStatusChangeRpt(Params.EqpStatusChangeRptParams eqpStatusChangeRptParams);

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
     * @date: 2018-10-29 14:16
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpStatusResetReq(Params.EqpStatusResetReqParams params);

    /**
     * description:This function checks the propriety of the data based on the reported measured value.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params -
     * @return com.fa.cim.common.support.Response
     * @author Paladin
     * @date 2018/10/18 10:25:41
     */
    Response eqpUsageCountResetReq(Params.EqpUsageCountResetReqParam params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param param -
     * @return com.fa.cim.common.support.Response
     * @author PlayBoy
     * @date 2018/7/13 15:16
     */
    Response carrierLoadingRpt(Params.loadOrUnloadLotRptParams param);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-07-11                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-07-11 20:58
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierLoadingVerifyReq(Params.CarrierLoadingVerifyReqParams carrierLoadingVerifyReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/7/19        ********             Fly               create file
     *
     * @author: Paladin
     * @date: 2018/7/19 14:15
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response moveOutReq(Params.OpeComWithDataReqParams opeComWithDataReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-11-07                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-11-07 14:05
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response moveInCancelForIBReq(Params.MoveInCancelForIBReqParams params);

    /**
     * description:
     * This function cancels the Operation Start for the started Lots.
     * This function reports to system that Operation Start is cancelled for started Lots on the eqp.
     * Operation Start cancel can be claimed by Control Job unit. By using this function, the information inside a system is rolled back to the situation which is just before Operation Start.
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params MoveInCancelReqParams
     * @return Response
     * @author PlayBoy
     * @date 2018/7/31
     */
    Response moveInCancelReq(Params.MoveInCancelReqParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/30 11:31:29
     */
    Response moveInForIBReq(Params.MoveInForIBReqParams moveInForIBReqParams);

    /**
     * description:
     * <p>
     * This function notifies system that Lots' actual Operation has started on the eqp.
     * This function reports to system that Lots in the loaded Carriers have been started on the eqp.
     * Based on the number of physical Ports and the setting of eqp's MultiRecipeCapability, two or more Lots can be reported at one time.
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params MoveInReqParams
     * @return Response
     * @author PlayBoy
     * @date 2018/7/24
     */
    Response moveInReq(Params.MoveInReqParams params);

    /**
     * description:
     * UnCarrierLoadingRptController .
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/7/11        ********             PlayBoy               create file
     *
     * @author: PlayBoy
     * @date: 2018/7/11 14:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response uncarrierLoadingRpt(Params.loadOrUnloadLotRptParams param);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 14:54
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eapRecoveryReq(Params.EAPRecoveryReqParam params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/12                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/12 15:51
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response forceMoveOutReq(Params.ForceMoveOutReqParams params);

    /**
     * description:TxCarrierMoveFromIBRpt
     * <p></p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/14                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/11/14 13:46
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierMoveFromIBRpt(Params.CarrierMoveFromIBRptParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/10                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/10 15:16
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response runningHoldReq(Params.RunningHoldReqParams params);

    /**
     * description: TxCarrierLoadingForIBRpt
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/11/12 11:31:51
     */
    Response carrierLoadingForIBRpt(Params.CarrierLoadingForIBRptParams carrierLoadingForIBRptParams);

    /**
     * description: TxCarrierMoveToIBRpt
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/11/16 10:04:11
     */
    Response carrierMoveToIBRpt(Params.CarrierMoveToIBRptParams carrierMoveToIBRptParams);

    /**
     * description: TxCarrierUnloadingForIBRpt
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/11/12 11:31:51
     */
    Response uncarrierLoadingForIBRpt(Params.CarrierUnloadingForIBRptParams uncarrierLoadingForIBRptParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/20       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/12/20 17:36
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierLoadingVerifyForIBReq(Params.CarrierLoadingVerifyForIBReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 12/14/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 12/14/2018 1:42 PM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response moveOutForIBReq(Params.MoveOutForIBReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29       ********              lightyh             create file
     *
     * @author: lightyh
     * @date: 2019/7/29 10:44
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response partialMoveOutReq(Params.PartialMoveOutReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/2/20 14:23                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/2/20 14:23
     * @param params -
     * @return com.fa.cim.common.support.Response
     */

    Response moveOutWithRunningSplitForIBReq(Params.PartialMoveOutReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/18       ********              Lin             create file
     *
     * @author: Lin
     * @date: 2018/12/18 10:38
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response chamberWithProcessWaferRpt(@RequestBody Params.ChamberWithProcessWaferRptInParams params);

    /**
     * description:
     * <p>This function reports wafer position in processed process resource.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/18                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/12/18 15:55
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response waferPositionWithProcessResourceRpt(@RequestBody Params.WaferPositionWithProcessResourceRptParam param);

    public Response carrierOutFromIBReq(@RequestBody Params.CarrierOutFromIBReqParam param);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/13                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/11/13 16:41
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpBufferTypeModifyReq(Params.EqpBufferTypeModifyReqInParm params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/1/2 10:25
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response reserveUnloadingLotsForIBRpt(Params.ReserveUnloadingLotsForIBRptParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/1/6 14:23
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response ReserveCancelUnloadingLotsForIBReq(Params.ReserveCancelUnloadingLotsForIBReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/2/20 16:51                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/2/20 16:51
     * @param params -
     * @return com.fa.cim.common.support.Response
     */

    Response ForceMoveOutForIBReq(Params.ForceMoveOutForIBReqParams params);

    Response cxEqpAlarmRpt(@RequestBody Params.EqpAlarmRptParams eqpAlarmRptParams);

    /**
     * description:
     * <p>This function reports lot Process Status.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/4                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/12/4 16:23
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response processStatusRpt(Params.ProcessStatusRptParam param);


    /**
     * description: loading carrier时增加查询当前eqp,port,carrier是否还有加工计划，没有sj则不允许load
     * change history:
     *  date      time           person          comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/6/7    19:32          Ly              Create
     *
     * @author Ly
     * @date 2021/6/7 19:32
     * @param
     * @return
     */
    Response carrierLoadingForSRTRpt(@RequestBody Params.CarrierLoadingForSORRptParams param);


    /**
     * description: 重sorter设备取下carrier
     * change history:
     *  date      time           person          comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/6/7    19:32          Ly              Create
     *
     * @author Ly
     * @date 2021/6/7 19:32
     * @param
     * @return
     */
    Response carrierUnloadingForSRTRpt(@RequestBody Params.CarrierUnloadingForSORRptParams param);

    /**
     * description: Task-461
     *              CarrierOutPortReq Controller
     *              1.EAP -> OMS CarrierOutPortReq MES校验并返回EAP 选择的portID 是否可用unloadReserve
     *              2.EAP -> OMS MES 没有找到可用的LP,存入Event数据，Sentinel 回调EAP 该carrierList 选择的portID
  * change history:
     * date             defect#             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/7/28                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/7/28 16:58
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierOutPortReq(@RequestBody CarrierOutPortReqParams params);

    /**
     * description: Task-461
     *              Sentinel 找到carrierList 对应的port 后异步返回给EAP
     * change history:
     * date             defect#             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/7/28                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/7/28 17:24
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierOutReq(@RequestBody CarrierOutReqParams params);

}