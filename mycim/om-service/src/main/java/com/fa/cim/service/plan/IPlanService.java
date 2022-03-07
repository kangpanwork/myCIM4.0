package com.fa.cim.service.plan;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 16:01
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPlanService {

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strProdOrderChangeReqResult
     * @param objCommon
     * @param seqIx
     * @param strChangedLotAttributes
     * @param claimMemo
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.ChangeLotReturn>>
     * @author Ho
     * @date 2018/11/23 13:35:46
     */
    public List<Infos.ChangeLotReturn> sxProdOrderChangeReq(List<Infos.ChangeLotReturn> strProdOrderChangeReqResult, Infos.ObjCommon objCommon, Integer seqIx, List<Infos.ChangedLotAttributes> strChangedLotAttributes, String claimMemo);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotExternalPriorityList lotExternalPriorityList
     * @param commitByLotFlag         commitByLotFlag
     * @param seqIx                   seqIx
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/25 13:35:19
     */
    //@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    List<Results.LotExternalPriorityResult> sxLotExtPriorityModifyReq(Infos.ObjCommon objCommon, List<Infos.LotExternalPriority> lotExternalPriorityList, Boolean commitByLotFlag, int seqIx) ;

    /**
     * description:
     * <p>Due to only lotExternalPriorityChangeByAll has Transaction,
     * so if sxLotExtPriorityModifyReq throw exception, only rollback for every lot<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon               objCommon
     * @param lotExternalPriorityList lotExternalPriorityList
     * @param errorLotCount           errorLotCount
     * @param resultList              resultList
     * @author PlayBoy
     * @date 2018/10/26 11:11:22
     */
    void lotExternalPriorityChangeByLot(Infos.ObjCommon objCommon, List<Infos.LotExternalPriority> lotExternalPriorityList, AtomicInteger errorLotCount, List<Results.LotExternalPriorityResult> resultList) ;

    /**
     * description:
     * <p>Due to sxLotExtPriorityModifyReq and lotExternalPriorityChangeByAll share same Transaction,
     * so if sxLotExtPriorityModifyReq throw exception, the whole will rollback<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon               objCommon
     * @param lotExternalPriorityList lotExternalPriorityList
     * @param errorLotCount           errorLotCount
     * @param resultList              resultList
     * @author PlayBoy
     * @date 2018/10/26 11:11:22
     */
    void lotExternalPriorityChangeByAll(Infos.ObjCommon objCommon, List<Infos.LotExternalPriority> lotExternalPriorityList, AtomicInteger errorLotCount, List<Results.LotExternalPriorityResult> resultList) ;

    Results.LotCurrentQueueReactivateReqResult sxLotRequeueReq(Infos.ObjCommon objCommon,
                                                               List<Infos.LotReQueueAttributes> lotReQueueAttributesList);

    List<Infos.ChangeLotScheduleReturn> sxLotScheduleChangeReq(Infos.ObjCommon objCommon, Params.LotScheduleChangeReqParams params) ;

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param newProdOrderCancelReqParams
     * @param objCommon
     * @return RetCode<List<Infos.ReleaseCancelLotReturn>>
     * @author Sun
     * @date 2018/10/17
     */

    List<Infos.ReleaseCancelLotReturn> sxNewProdOrderCancelReq(Params.NewProdOrderCancelReqParams newProdOrderCancelReqParams, Infos.ObjCommon objCommon) ;

    Results.NewProdOrderCreateReqResult sxNewProdOrderCreateReq(Infos.ObjCommon objCommon, Infos.ReleaseLotAttributes releaseLotAttributes) ;

    Infos.ReleasedLotReturn sxNewProdOrderModifyReq(Infos.ObjCommon objCommon, Infos.UpdateLotAttributes updateLotAttributes) ;

    void sxLotPlanChangeReserveCreateReq(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation schdlChangeReservation, String claimMemo);

    void sxLotPlanChangeReserveModifyReq(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation strCurrentSchdlChangeReservation, Infos.SchdlChangeReservation strNewSchdlChangeReservation, String claimMemo);

    List<Infos.SchdlChangeReservation> sxLotPlanChangeReserveCancelReq(Infos.ObjCommon objCommon, List<Infos.SchdlChangeReservation> strSchdlChangeReservations, String claimMemo);

    List<Infos.WIPLotResetResult> sxStepContentResetByLotReq(Infos.ObjCommon objCommon, Infos.StepContentResetByLotReqInParm stepContentResetByLotReqInParm, String claimMemo);

    List<Infos.ChangeLotSchdlReturn> sxLotPlanChangeReserveDoActionReq(Infos.ObjCommon objCommon, List<Infos.ReScheduledLotAttributes> reScheduledLotAttributesList, String claimMemo);

    /**
     * description:  Modify the lot information, such as the product step process, according to the SPS
     * lot change Plan configuration (in OMS called SchduleChangeReservation)
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/8/14 13:19                       AOKI              Create
     * @author AOKI
     * @date 2021/8/14 13:19
     * @param objCommon user information
     * @param lotID  lot identifier
     * @return void
     */
    void sxSchdlChangeReservationExecuteByPostProcReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID);
}