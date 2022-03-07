package com.fa.cim.service.einfo;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.lmg.LotMonitorGroupParams;

import java.util.List;

/**
 * description:
 * <p>IElectronicInformationInqService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/8/008   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/8/008 16:21
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IElectronicInformationInqService {

    Results.EboardInfoInqResult sxEboardInfoInq(Infos.ObjCommon objCommon, User reqestUserID);

    Results.OpeGuideInqResult sxEqpOperationManualInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    Results.HistoryInformationInqResult sxHistoryInformationInq(Infos.ObjCommon strObjCommonIn, Params.HistoryInformationInqParams strHistoryInformationInqInParm);

    Results.LotAnnotationInqResult sxLotAnnotationInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    Results.LotMemoInfoInqResult sxLotMemoInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    Results.LotOperationSelectionFromHistoryInqResult sxLotOperationSelectionFromHistoryInq(Infos.ObjCommon objCommon, Params.LotOperationSelectionFromHistoryInqParams params);

    Results.LotOpeMemoInfoInqResult sxLotOpeMemoInfoInq(Params.LotOpeMemoInfoInqParams lotOpeMemoInfoInqParams, Infos.ObjCommon objCommon);

    Results.LotOpeMemoListInqResult sxLotOpeMemoListInq(Params.LotOpeMemoListInqParams params, Infos.ObjCommon objCommon);

    Results.LotOperationHistoryInqResult sxLotOperationHistoryInq(
            Infos.ObjCommon strObjCommonIn,
            ObjectIdentifier lotID,
            ObjectIdentifier routeID,
            ObjectIdentifier operationID,
            String operationNumber,
            String operationPass,
            String operationCategory,
            Boolean pinPointFlag,
            String fromTimeStamp,
            String toTimeStamp
    );

    Results.SubLotTypeListInqResult sxSubLotTypeListInq(Infos.ObjCommon objCommon, Params.SubLotTypeListInqParams subLotTypeListInqParams);

    List<String> sxEqpSpecialControlsGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    Infos.EqpPortInfo sxPortInfoInq(Infos.ObjCommon objCommon, Params.PortInfoInqParams portInfoInqParams);

    List<Infos.ScrapHistories> sxWaferScrappedHistoryInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier cassetteID);

    List<Infos.EquipmentAlarm> sxEqpAlarmHistInq(Infos.ObjCommon objCommon, Params.EqpAlarmHistInqParams params);

    Results.EDCHistoryInqResult sxEDCHistoryInq(Infos.ObjCommon objCommon, Params.EDCHistoryInqParams params);

    /**
     * description: eqp search for setting eqp board
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2021/2/19 0019 11:32
     * @param objCommon - common
     * @param eqpSearchForSettingEqpBoardParams - eqp search params
     * @return result eqp info
     */
    List<Results.EqpSearchForSettingEqpBoardResult> sxEqpSearchForSettingEqpBoardInq(Infos.ObjCommon objCommon, Params.EqpSearchForSettingEqpBoardParams eqpSearchForSettingEqpBoardParams);

    /**
     * description: eqp area board list
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2021/2/19 0019 11:04
     * @param objCommon - objCommon
     * @param eqpAreaBoardListParams - eqpAreaBoardListParams
     * @return rest
     */
    List<Results.EqpAreaBoardListResult> sxEqpAreaBoardListInq(Infos.ObjCommon objCommon, Params.EqpAreaBoardListParams eqpAreaBoardListParams);

    /**
     * description: eqp work zone list
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2021/2/19 0019 11:04
     * @param eqpWorkZoneListParams - eqpWorkZoneListParams
     * @return rest
     */
    List<String> sxEqpWorkZoneListInq(Infos.ObjCommon objCommon, Params.EqpWorkZoneListParams eqpWorkZoneListParams);

    /**
     * description:  通过monitor group 查询monitor lot 的 edc 数据集
     * change history:
     * date             defect             person             comments
     * ----------------------------------------------------------------------------------------------------------------
     * 2021/7/28 0028 14:48                        YJ                Create
     *
     * @author YJ
     * @date 2021/7/28 0028 14:48
     * @param  objCommon - common
     * @param  monitorDataCollectionParams - monitor data collection params
     * @return edc history
     */
    Results.EDCHistoryInqResult sxMonitorLotDataCollectionInq(
            Infos.ObjCommon objCommon, LotMonitorGroupParams.MonitorDataCollectionParams monitorDataCollectionParams);

}
