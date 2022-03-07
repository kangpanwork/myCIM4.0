package com.fa.cim.controller.interfaces.electronicInformation;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import com.fa.cim.lmg.LotMonitorGroupParams;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 11:23
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IElectronicInformationInqController {

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/29       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/11/29 10:01
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eboardInfoInq(Params.EboardInfoInqParams params);

    /**
     * description:TxBayListInq
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019-06-28                           Zack             create file
     * <p>
     * return:
     *
     * @author Zack
     * @date: 2018--06-28 10:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpOperationManualInq(Params.OpeGuideInq opeGuideInqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @throws
     * @author Ho
     * @date 2019/4/29 13:33
     */
    Response historyInformationInq(Params.HistoryInformationInqParams strHistoryInformationInqInParm);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/22       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/22 10:19
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotAnnotationInq(Params.LotAnnotationInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/22       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/22 14:41
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotMemoInfoInq(Params.LotMemoInfoInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 12/11/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 12/11/2018 5:08 PM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotOperationSelectionFromHistoryInq(Params.LotOperationSelectionFromHistoryInqParams params);

    /**
     * description:
     * The method use to define the LotOpeMemoInfoInqController.
     * transaction ID: OINFQ011
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/22        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/22 17:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotOpeMemoInfoInq(Params.LotOpeMemoInfoInqParams lotOpeMemoInfoInqParams);

    /**
     * description:
     * The method use to define the LotOpeMemoListInqController.
     * transaction ID: OINFQ012
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/23        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/23 14:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotOpeMemoListInq(Params.LotOpeMemoListInqParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @throws
     * @author Ho
     * @date 2019/4/25 13:34
     */
    Response lotOperationHistoryInq(Params.LotOperationHistoryInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 12/24/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 12/24/2018 10:19 AM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response edcHistoryInq(Params.EDCHistoryInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/10         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/10 10:33
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response waferScrappedHistoryInq(Params.WaferScrappedHistoryInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/2/19 16:37                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/2/19 16:37
     * @param params -
     * @return com.fa.cim.common.support.Response
     */

    Response EqpAlarmHistInq(Params.EqpAlarmHistInqParams params);

    /**
     * description: eqp search for setting eqp board
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2021/2/19 0019 11:04
     * @param eqpSearchForSettingEqpBoardParams - eqpSearchForSettingEqpBoardParams
     * @return rest
     */
    Response eqpSearchForSettingEqpBoardInq(Params.EqpSearchForSettingEqpBoardParams eqpSearchForSettingEqpBoardParams);

    /**
     * description: eqp area board list
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2021/2/19 0019 11:04
     * @param eqpAreaBoardListParams - eqpAreaBoardListParams
     * @return rest
     */
    Response eqpAreaBoardListInq(Params.EqpAreaBoardListParams eqpAreaBoardListParams);

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
    Response eqpWorkZoneListInq(Params.EqpWorkZoneListParams eqpWorkZoneListParams);

    /**
     * description:  通过monitor group 查询monitor lot 的 edc 数据集
     * change history:
     * date             defect             person             comments
     * ----------------------------------------------------------------------------------------------------------------
     * 2021/7/28 0028 14:48                        YJ                Create
     *
     * @author YJ
     * @date 2021/7/28 0028 14:48
     * @param  monitorDataCollectionParams - monitor data collection params
     * @return edc history
     */
    Response monitorLotDataCollectionInq(
            LotMonitorGroupParams.MonitorDataCollectionParams monitorDataCollectionParams);


}