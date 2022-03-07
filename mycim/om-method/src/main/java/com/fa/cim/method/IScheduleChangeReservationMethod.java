package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;

import java.util.List;

/**
 * description:
 * IScheduleChangeReservationMethod .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/8/10        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/8/10 10:22
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IScheduleChangeReservationMethod {
    /**
     * description:
     * Get ScheduleChangeReservationList by ScheduleChangeReservation's Data Members.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param inParams  inParams
     * @return RetCode scheduleChangeReservation
     * @author PlayBoy
     * @date 2018/8/10
     */
    List<Infos.SchdlChangeReservation> schdlChangeReservationGetListDR(Infos.ObjCommon objCommon, Inputs.ObjScheduleChangeReservationGetListIn inParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/18 18:19
     * @param objCommon
     * @param objSchdlChangeReservationCheckForActionDRIn -
     * @return com.fa.cim.dto.Outputs.ObjSchdlChangeReservationCheckForActionDROut
     */
    Outputs.ObjSchdlChangeReservationCheckForActionDROut schdlChangeReservationCheckForActionDR(Infos.ObjCommon objCommon, Inputs.ObjSchdlChangeReservationCheckForActionDRIn objSchdlChangeReservationCheckForActionDRIn);
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                 objCommon
     * @param scheduleChangeReservation scheduleChangeReservation
     * @return RetCode
     * @author PlayBoy
     * @date 2018/8/10
     */
    void schdlChangeReservationDeleteDR(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation scheduleChangeReservation);


    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/26 10:43
     * @param objCommon - 
     * @param lotID -
     * @param routeID -
     * @param operationNumber -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void schdlChangeReservationCheckForFutureOperation(Infos.ObjCommon objCommon, ObjectIdentifier lotID,
                                                                  String routeID, String operationNumber);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/3 16:30
     * @param objCommon
     * @param schdlChangeReservation -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void scheduleChangeReservationCreateDR(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation schdlChangeReservation);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/19 11:16
     * @param objCommon
     * @param currentSchdlChangeReservation
     * @param newSchdlChangeReservation -
     * @return void
     */
    void schdlChangeReservationChangeDR(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation currentSchdlChangeReservation, Infos.SchdlChangeReservation newSchdlChangeReservation);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/31 10:28
     * @param objCommon
     * @param schdlChangeReservation -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void schdlChangeReservationCheckForRegistration(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation schdlChangeReservation);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/10/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/10/11 13:45
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */

    void schdlChangeReservationCheckForMerge(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/10/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/10/17 15:45
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void schdlChangeReservationCheckForBranchCancelDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/3/3 9:15
     * @param objCommon
     * @param schdlChangeReservation -
     * @return void
     */
    void schdlChangeReservationApplyCountIncreaseDR(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation schdlChangeReservation);
}
