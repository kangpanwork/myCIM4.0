package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.*;

import java.util.List;

/**
 * <p>IProcessForDurableMethod .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/6/17 14:26         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/6/17 14:26
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface IProcessForDurableMethod {

    /**
     * This object function fill the return structure's value.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/17 14:21
     */
    Outputs.ProcessStartDurablesReserveInformationGetBaseInfoForClientOut processStartDurablesReserveInformationGetBaseInfoForClient(Infos.ObjCommon objCommon,
                                                                                                                                     ObjectIdentifier equipmentID,
                                                                                                                                     String durableCategory,
                                                                                                                                     List<ObjectIdentifier> durableIDs);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strProcess_operationListForDurableFromHistoryDR_in
     * @return java.util.List<com.fa.cim.dto.Infos.OperationNameAttributesFromHistory>
     * @exception 
     * @author ho
     * @date 2020/6/22 13:50
     */
    public List<Infos.OperationNameAttributesFromHistory> processOperationListForDurableFromHistoryDR(Infos.ObjCommon strObjCommonIn, Infos.ProcessOperationListForDurableFromHistoryDRIn strProcess_operationListForDurableFromHistoryDR_in );

    /**
     * Set Durable Control Job ID and start information.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/22 10:05
     */
    void processStartDurablesReserveInformationSet(Infos.ObjCommon objCommon, Inputs.ProcessStartDurablesReserveInformationSetIn reserveInformationSetIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strProcessStartDurablesReserveInformationClearin
     * @return void
     * @exception
     * @author ho
     * @date 2020/6/24 15:06
     */
    void processStartDurablesReserveInformationClear(
            Infos.ObjCommon                                        strObjCommonIn,
            Infos.ProcessStartDurablesReserveInformationClearIn strProcessStartDurablesReserveInformationClearin );

    /**
     * Set the value of ActualCompTimeStamp for actual comp information.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/28 16:49
     */
    void durableProcessActualCompInformationSet(Infos.ObjCommon objCommon, String durableCategory, List<Infos.StartDurable> startDurables);

    /**
     *  Move process operation of Durable.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/28 16:55
     */
    Boolean durableProcessMove(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableId);
    /**
     * description:
     * <p>
     * method:process_durableProcessLagTime_Get
     * Get ProcessLagTime of lot's previous operation.
     * - If processLagTime is defined, the information, which includes expiredTimeDuration
     * - If processLagTime is not defined, "0" and "1901-01-01-00.00.00.000000" are returned.</p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param durableProcessLagTimeUpdateReqInParm     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjMonitorLotSTBInfoGetOut>
     * @author Bear
     * @date 2018/11/7 16:27
     */
    Outputs.ObjProcessLagTimeGetOut processDurableProcessLagTimeGet(Infos.ObjCommon objCommon, Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm);



    /**
     * description:
     * <p>
     * method:durable_processLagTime_Set
     * Get ProcessLagTime of lot's previous operation.
     * - If processLagTime is defined, the information, which includes expiredTimeDuration
     * - If processLagTime is not defined, "0" and "1901-01-01-00.00.00.000000" are returned.</p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param durableProcessLagTimeUpdateReqInParm     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjMonitorLotSTBInfoGetOut>
     * @author Bear
     * @date 2018/11/7 16:27
     */
    void DurableProcessLagTimeSet(Infos.ObjCommon objCommon, Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm,Outputs.ObjProcessLagTimeGetOut lagTimeGetOutRetCode);





    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strProcessGetTargetOperationForDurablein
     * @return com.fa.cim.dto.Infos.ProcessRef
     * @exception
     * @author ho
     * @date 2020/6/28 15:16
     */
     Infos.ProcessRef processGetTargetOperationForDurable(
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.ProcessGetTargetOperationForDurableIn strProcessGetTargetOperationForDurablein );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strProcessLocateForDurablein
     * @return com.fa.cim.dto.Infos.ProcessLocateForDurableOut
     * @exception
     * @author ho
     * @date 2020/6/28 15:38
     */
     Infos.ProcessLocateForDurableOut processLocateForDurable(
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.ProcessLocateForDurableIn strProcessLocateForDurablein );

     /**
      * description:
      * <p></p>
      * change history:
      * date   defect   person   comments
      * ------------------------------------------------------------------------------------------------------------------
      *
      * @return
      * @author Decade
      * @date 2020/7/6/006 13:03
      */
     Results.DurableOperationListInqResult processOperationListForDurableDR(Infos.ObjCommon objCommon, Params.ProcessOperationListForDurableDRInParam param);

    void processOperationListForDurableHelperDR(Infos.ObjCommon objCommon, Infos.DurableOperationNameAttributes objProcessOperationListForDurableHelperDROut);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strProcessCheckGatePassForDurablein
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/8 15:16
     */
    void processCheckGatePassForDurable(
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.ProcessCheckGatePassForDurableIn strProcessCheckGatePassForDurablein );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strProcessGetReturnOperationForDurablein
     * @return com.fa.cim.dto.Infos.ProcessGetReturnOperationForDurableOut
     * @exception
     * @author ho
     * @date 2020/7/10 15:38
     */
    Infos.ProcessGetReturnOperationForDurableOut processGetReturnOperationForDurable(
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.ProcessGetReturnOperationForDurableIn strProcessGetReturnOperationForDurablein );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strProcessdurableReworkCountCheckin
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/13 9:55
     */
     void processDurableReworkCountCheck(
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.ProcessDurableReworkCountCheckIn strProcessdurableReworkCountCheckin );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strProcessdurableReworkCountIncrementin
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/13 10:03
     */
     void processDurableReworkCountIncrement(
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.ProcessDurableReworkCountIncrementIn strProcessdurableReworkCountIncrementin );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strProcessBranchRouteForDurablein
     * @return com.fa.cim.dto.Inputs.OldCurrentPOData
     * @exception
     * @author ho
     * @date 2020/7/13 10:17
     */
     Inputs.OldCurrentPOData processBranchRouteForDurable(
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.ProcessBranchRouteForDurableIn strProcessBranchRouteForDurablein );
}
