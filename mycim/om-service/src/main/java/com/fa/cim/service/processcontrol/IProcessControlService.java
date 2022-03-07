package com.fa.cim.service.processcontrol;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 16:54
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProcessControlService {
    void sxFutureHoldReq(Infos.ObjCommon objCommon, Params.FutureHoldReqParams params) ;

    void sxFutureHoldCancelReq(Infos.ObjCommon objCommon, Params.FutureHoldCancelReqParams params) ;

    void sxFutureHoldCancelReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier releaseReasonCodeID, String entityType, List<Infos.LotHoldReq> futureHoldCancelReqList) ;

    Results.NPWUsageStateModifyReqResult sxNPWUsageStateModifyReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String controlUseState, int usageCount);

    /**
     * description:
     *     Update lot's process lag time information.
     *     (Case-1) action : SP_ProcessLagTime_Action_Set
     *        This action is specified to set lot's processLagTime information.
     *        When OpeComp is claimed for trigger operation of processLagTime,
     *        this tx is called with SP_ProcessLagTime_Action_Set. in this case,
     *        txHoldLotReq() is also called with "PLTH" (ProcessLagTimeHold).
     *
     *     (Case-2) action : SP_ProcessLagTime_Action_Clear
     *        This action is specified to clear lot's processLagTime information.
     *        When process lag time is expired, this tx is called by Watchdog with
     *        SP_ProcessLagTime_Action_Clear. In this case, txHoldLotReleaseReq()
     *        is also called with "PLTR" (ProcLagTimeHoldRelease).
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/11/8 11:07
     * @param objCommon -
     * @param params -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void sxProcessLagTimeUpdate(Infos.ObjCommon objCommon, Params.LagTimeActionReqParams params) ;

    RetCode<String> sxQtimeActionReq(Infos.ObjCommon objCommon, Inputs.QtimeActionReqIn qtimeActionReqIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params    -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 11/14/2018 10:42 AM
     */
    void sxQtimerReq(Infos.ObjCommon objCommon, Params.QtimerReqParams params) ;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strQtimeManageActionByPostTaskReqInParm
     * @param claimMemo
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/27 14:39
     */
    public void sxQtimeManageActionByPostTaskReq(Infos.ObjCommon strObjCommonIn, Params.QtimeManageActionByPostTaskReqInParm strQtimeManageActionByPostTaskReqInParm, String claimMemo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param lotID
     * @param routeID
     * @param operationNumber
     * @param strFutureReworkDetailInfoSeq
     * @param claimMemo
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/8/27 15:40
     */
    void sxFutureReworkCancelReq(
            Infos.ObjCommon strObjCommonIn,
            ObjectIdentifier lotID,
            ObjectIdentifier routeID,
            String operationNumber,
            List<Infos.FutureReworkDetailInfo> strFutureReworkDetailInfoSeq,
            String claimMemo) ;

    List<Infos.ProcessHoldLot> sxProcessHoldCancelReq(Infos.ObjCommon objCommon, Params.ProcessHoldCancelReq param);

    List<Infos.ProcessHoldLot> sxProcessHoldReq(Infos.ObjCommon objCommon, Params.ProcessHoldReq param);

    void sxFutureReworkReq(Infos.ObjCommon objCommon, Params.FutureReworkReqParams params) ;

    void sxFutureReworkActionDoReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String claimMemo);

    void sxProcessHoldDoActionReq(Infos.ObjCommon objCommon, ObjectIdentifier createLotID, String claimMemo);

    /**
     * description: future hold edit department
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params - params
     * @author YJ
     * @date 2021/1/27 0027 15:14
     */
    void sxFutureHoldDepartmentChangeReq(Infos.ObjCommon objCommon, Params.FutureHoldReqParams params);

    /**
     * description: process hold edit department
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2021/1/27 0027 17:38
     * @param objCommon - common
     * @param params - params
     */
    void sxProcessHoldDepartmentChangeReq(Infos.ObjCommon objCommon, Params.ProcessHoldReq params);

    void sxFutureHoldPreByPostProcessReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String claimMemo);

    void sxFutureHoldPostByPostProcessReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String claimMemo);
}