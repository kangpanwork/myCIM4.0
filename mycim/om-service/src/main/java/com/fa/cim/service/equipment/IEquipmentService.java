package com.fa.cim.service.equipment;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.eqp.carrierout.CarrierOutPortReqParams;
import com.fa.cim.eqp.carrierout.CarrierOutPortResults;
import com.fa.cim.eqp.carrierout.CarrierOutReqParams;

import java.util.List;

/**
 * description:
 * <p>IEquipmentService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/9/009   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/9/009 15:26
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEquipmentService {

    /**
     * description: change chamber status(旧版本v1)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/23 9:28                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/23 9:28
     * @param objCommon
     * @param equipmentID
     * @param chamberStatusList
     * @param claimMemo -
     * @return com.fa.cim.dto.Results.ChamberStatusChangeReqResult
     */
    Results.ChamberStatusChangeReqResult sxChamberStatusChangeReqV1(Infos.ObjCommon objCommon,
                                                                    ObjectIdentifier equipmentID,
                                                                    List<Infos.EqpChamberStatus> chamberStatusList,
                                                                    String claimMemo);

    /**
     * Chamber腔体状态修改
     * @param objCommon 通用参数对象
     * @param equipmentID 设备标识
     * @param chamberStatuses 腔体状态列表
     * @param claimMemo 操作备注
     * @return {@link Results.ChamberStatusChangeReqResult}
     * @version 0.1
     * @author Grant
     * @date 2021/7/19
     */
    Results.ChamberStatusChangeReqResult sxChamberStatusChangeReq(Infos.ObjCommon objCommon,
                                                                  ObjectIdentifier equipmentID,
                                                                  List<Infos.EqpChamberStatus> chamberStatuses,
                                                                  String claimMemo);

    /**
     * Chamber腔体状态修改
     * @param objCommon 通用参数对象
     * @param equipmentID 设备标识
     * @param chamberStatuses 腔体状态列表
     * @param reasonCodeID ReasonCode ObjectIdentifier
     * @param claimMemo 操作备注
     * @return {@link Results.ChamberStatusChangeReqResult}
     * @version 0.1
     * @author Grant
     * @date 2021/7/19
     */
    Results.ChamberStatusChangeReqResult sxChamberStatusChangeReq(Infos.ObjCommon objCommon,
                                                                  ObjectIdentifier equipmentID,
                                                                  List<Infos.EqpChamberStatus> chamberStatuses,
                                                                  ObjectIdentifier reasonCodeID,
                                                                  String claimMemo);

    /**
     * 执行设备状态修改请求(旧版本v1)
     * @param objCommonIn 通用参数对象
     * @param equipmentId 设备标识
     * @param equipmentStatusCode 将要修改的新E10子状态
     * @param claimMemo 操作备注
     * @return {@link Results.EqpStatusChangeReqResult}
     */
    Results.EqpStatusChangeReqResult sxEqpStatusChangeReqV1(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentId,
                                                            ObjectIdentifier equipmentStatusCode, String claimMemo);

    /**
     * 执行设备状态修改请求
     * @param objCommon 通用参数对象
     * @param equipmentID 设备标识
     * @param eqpToState 将要修改的新E10子状态
     * @param claimMemo 操作备注
     * @return {@link Results.EqpStatusChangeReqResult}
     * @version 0.1
     * @author Grant
     * @date 2021/7/19
     */
    Results.EqpStatusChangeReqResult sxEqpStatusChangeReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
                                                          ObjectIdentifier eqpToState, String claimMemo);

    /**
     * 执行设备状态修改请求
     * @param objCommon 通用参数对象
     * @param equipmentID 设备标识
     * @param eqpToState 将要修改的新E10子状态
     * @param reasonCodeID ReasonCode ObjectIdentifier
     * @param claimMemo 操作备注
     * @return {@link Results.EqpStatusChangeReqResult}
     * @version 0.1
     * @author Grant
     * @date 2021/7/19
     */
    Results.EqpStatusChangeReqResult sxEqpStatusChangeReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
                                                          ObjectIdentifier eqpToState, ObjectIdentifier reasonCodeID,
                                                          String claimMemo);

    Results.EqpStatusChangeRptResult sxEqpStatusChangeRpt(Infos.ObjCommon objCommon,
                                                          Params.EqpStatusChangeRptParams rptParams);

    Results.EqpStatusResetReqResult sxEqpStatusResetReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String changeType, String claimMemo);

    ObjectIdentifier sxEqpUsageCountResetReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String claimMemo);

    List<Infos.LoadingVerifiedLot> sxCarrierLoadingRpt(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier portID, String lotPurposeType);

    Results.CarrierLoadingVerifyReqResult sxCarrierLoadingVerifyReq(Infos.ObjCommon objCommon, Params.CarrierLoadingVerifyReqParams carrierLoadingVerifyReqParams);

    @Deprecated
    Results.MoveOutReqResult sxMoveOutReq(Infos.ObjCommon objCommon, Params.OpeComWithDataReqParams opeComWithDataReqParams);

    Results.MoveInCancelReqResult sxMoveInCancelForIBReq(Infos.ObjCommon objCommon,
                                                         ObjectIdentifier equipmentID,
                                                         ObjectIdentifier controlJobID,
                                                         List<Infos.ApcBaseCassette> apcBaseCassetteListForOpeStartCancel,
                                                         String pAPCIFControlStatus,
                                                         String pDCSIFControlStatus);

    Results.MoveInCancelReqResult sxMoveInCancelReq(Infos.ObjCommon objCommon,
                                                    ObjectIdentifier equipmentID,
                                                    ObjectIdentifier controlJobID,
                                                    String claimMemo,
                                                    List<Infos.ApcBaseCassette> strAPCBaseCassetteListForOpeStartCancel,
                                                    String APCIFControlStatus,
                                                    String DCSIFControlStatus);

    Results.MoveInReqResult sxMoveInForIBReq(Infos.ObjCommon objCommon, Params.MoveInForIBReqParams moveInForIBReqParams, String APCIFControlStatus, String DCSIFControlStatus);

    Results.MoveInReqResult sxMoveInReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, ObjectIdentifier controlJobID, List<Infos.StartCassette> startCassetteList, boolean processJobPauseFlag, String APCIFControlStatus, String DCSIFControlStatus, String claimMemo);

    void sxCarrierUnloadingRpt(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier portID);

    Results.EAPRecoveryReqResult sxEAPRecoveryReq(Infos.ObjCommon objCommon, Params.EAPRecoveryReqParam params);

    /**
     * @see IEquipmentProcessOperation#sxForceMoveOutReq(Infos.ObjCommon, ObjectIdentifier, ObjectIdentifier, Boolean, String)
     * @author zqi
     */
    @Deprecated
    Results.ForceMoveOutReqResult sxForceMoveOutReq(Infos.ObjCommon objCommon,
                                                    ObjectIdentifier equipmentID,
                                                    ObjectIdentifier controlJobID,
                                                    Boolean spcResultRequiredFlag,
                                                    String claimMemo);

    void sxCarrierMoveFromIBRpt(Infos.ObjCommon objCommon, Params.CarrierMoveFromIBRptParams params);

    void sxRunningHoldReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentId, ObjectIdentifier controlJobId, ObjectIdentifier holdReasonCodeId, String claimMemo);

    /**
     * description: equipment port loading carrier
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/22 14:38                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/22 14:38
     * @param objCommon
     * @param equipmentID
     * @param cassetteID
     * @param portID
     * @param loadPurposeType
     * @param claimMemo -
     * @return java.util.List<com.fa.cim.dto.Infos.LoadingVerifiedLot>
     */
    List<Infos.LoadingVerifiedLot> sxCarrierLoadingForIBRpt(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier portID, String loadPurposeType, String claimMemo);

    void sxCarrierMoveToIBRpt(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier loadedPortID, ObjectIdentifier carrierID, String claimMemo);

    void sxCarrierUnloadingForIBRpt(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier portID, String claimMemo);

    Results.CarrierLoadingVerifyReqResult sxCarrierLoadingVerifyForIBReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, ObjectIdentifier cassetteID, String loadPurposeType);

    /**
     * @see IEquipmentProcessOperation#sxMoveOutForIBReq(Infos.ObjCommon, Params.MoveOutForIBReqParams)
     */
    @Deprecated
    Results.MoveOutReqResult sxMoveOutForIBReq(Infos.ObjCommon objCommon, Params.MoveOutForIBReqParams params);

    /**
     * @see IEquipmentProcessOperation#sxPartialMoveOutReq(Infos.ObjCommon, Params.PartialMoveOutReqParams, String, String)
     * @author zqi
     */
    @Deprecated
    Results.PartialMoveOutReqResult sxPartialMoveOutReq(Infos.ObjCommon objCommon,
                                                        Params.PartialMoveOutReqParams partialMoveOutReqParams,
                                                        String APCIFControlStatus,
                                                        String DCSIFControlStatus);

    /**
     * @see IEquipmentProcessOperation#sxMoveOutWithRunningSplitForIBReq(Infos.ObjCommon, Params.PartialMoveOutReqParams, String, String)
     * @author zqi
     */
    @Deprecated
    Results.PartialMoveOutReqResult sxMoveOutWithRunningSplitForIBReq(Infos.ObjCommon objCommon,
                                                                      Params.PartialMoveOutReqParams partialMoveOutReqParams,
                                                                      String APCIFControlStatus,
                                                                      String DCSIFControlStatus);

    void sxEqpBufferTypeModifyReq(Infos.ObjCommon objCommon, Params.EqpBufferTypeModifyReqInParm params);

    void sxReserveUnloadingLotsForIBRpt(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier unloadReservePortID, String claimMemo);

    void sxReserveCancelUnloadingLotsForIBReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID);

    void sxChamberWithProcessWaferRpt(Infos.ObjCommon objCommon, Params.ChamberWithProcessWaferRptInParams params);

    void sxWaferPositionWithProcessResourceRpt(Infos.ObjCommon objCommon, Params.WaferPositionWithProcessResourceRptParam param);

    void sxCarrierOutFromIBReq(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, String claimMemo);

    /**
     * @see IEquipmentProcessOperation#sxForceMoveOutForIBReq(Infos.ObjCommon, ObjectIdentifier, ObjectIdentifier, Boolean, String)
     * @author zqi
     */
    @Deprecated
    Results.ForceMoveOutReqResult sxForceMoveOutForIBReq(Infos.ObjCommon objCommon,
                                                  ObjectIdentifier equipmentID,
                                                  ObjectIdentifier controlJobID,
                                                  Boolean spcResultRequiredFlag,
                                                  String claimMemo);

    Results.EqpAlarmRptResult sxEqpAlarmRpt(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier stockerID, ObjectIdentifier AGVID, Infos.EquipmentAlarm strEquipmentAlarm, String claimMemo);

    void sxProcessStatusRpt(Infos.ObjCommon objCommon, Params.ProcessStatusRptParam param);

    void sxEqpEAPStatusSyncReq(Infos.ObjCommon objCommon, Params.EqpEAPStatusSyncReqPrams params);

    ObjectIdentifier sxChamberStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.EqpChamberStatus> eqpChamberStatuses);

    /**
     * description: change equipment mode
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/22 15:16                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/22 15:16
     * @param objCommon
     * @param params -
     * @return void
     */
    void sxEqpModeChangeReq(Infos.ObjCommon objCommon, Params.EqpModeChangeReqPrams params);

    ObjectIdentifier sxEqpMemoAddReq(Infos.ObjCommon objCommon, Params.EqpMemoAddReqParams params);

    /**
     * description: change port status of equipment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/22 11:21                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/22 11:21
     * @param objCommon
     * @param equipmentID
     * @param eqpPortEventOnTCSes
     * @param claimMemo -
     * @return com.fa.cim.common.support.ObjectIdentifier
     */
    ObjectIdentifier sxPortStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.EqpPortEventOnTCS> eqpPortEventOnTCSes, String claimMemo);

    /**
     * description: Sorter equipment port loading carrier
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
    List<Infos.LoadingVerifiedLot> sxCarrierLoadingForSORRpt(Infos.ObjCommon objCommon,String claimMemo,
                                                             ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier portID, String loadPurposeType);

    /**
     * description: Sorter equipment port unsxCarrierUnloadingRptloading carrier
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
    void sxCarrierUnloadingForSORRpt(Infos.ObjCommon objCommon,String claimMemo,ObjectIdentifier equipmentID,
                                     ObjectIdentifier cassetteID, ObjectIdentifier portID);


    /**
    * description: 获取EqpPortInfo
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/8 16:17 Ly Create
    *
    * @author Ly
    * @date 2021/7/8 16:17
    * @param  ‐
    * @return
    */
    Infos.EqpPortInfo equipmentPortInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier eqpID);

    /**
     * description: Task-461
     *              CarrierOutPortReq service
     * change history:
     * date             defect#             person             comments
     * ----------------------------------------------------------------------------------------------------------------
     * 2021/7/28                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/7/28 17:14
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<CarrierOutPortResults> sxCarrierOutPortReq(Infos.ObjCommon objCommon, CarrierOutPortReqParams params);

    /**
     * description:Task-461
     *             CarrierOutPortReturnReq service
     * change history:
     * date             defect#             person             comments
     * ----------------------------------------------------------------------------------------------------------------
     * 2021/7/28                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/7/28 17:23
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    CarrierOutPortResults sxCarrierOutReq(Infos.ObjCommon objCommon, CarrierOutReqParams params);
}
