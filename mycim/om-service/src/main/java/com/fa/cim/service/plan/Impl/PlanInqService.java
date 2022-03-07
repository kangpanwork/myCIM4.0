package com.fa.cim.service.plan.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IScheduleChangeReservationMethod;
import com.fa.cim.service.plan.IPlanInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
 * @date: 2020/9/8 17:37
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class PlanInqService implements IPlanInqService {
    @Autowired
    private IScheduleChangeReservationMethod scheduleChangeReservationMethod;

    @Override
    public Results.LotPlanChangeReserveListInqResult sxLotPlanChangeReserveListInq(Infos.ObjCommon objCommon, Params.LotPlanChangeReserveListInqParams params){
        Results.LotPlanChangeReserveListInqResult lotPlanChangeReserveListInqResult = new Results.LotPlanChangeReserveListInqResult();
        /*-----------------------------------------------*/
        /*   Call Obj schdlChangeReservation_GetListDR   */
        /*-----------------------------------------------*/
        Inputs.ObjScheduleChangeReservationGetListIn inParams = new Inputs.ObjScheduleChangeReservationGetListIn();
        inParams.setEventID(params.getEventID());
        inParams.setObjectID(params.getObjectID());
        inParams.setObjectType(params.getObjectType());
        inParams.setTargetRouteID(params.getTargetRouteID());
        inParams.setTargetOperationNumber(params.getTargetOperationNumber());
        inParams.setProductID(params.getProductID());
        inParams.setRouteID(params.getRouteID());
        inParams.setOperationNumber(params.getOperationNumber());
        inParams.setSubLotType(params.getSubLotType());
        inParams.setStartDate(params.getStartDate());
        inParams.setEndDate(params.getEndDate());
        inParams.setStatus(params.getStatus());
        inParams.setLotInfoChangeFlag(params.getLotInfoChangeFlag());
        log.info("Step1 - schdlChangeReservation_GetListDR__110");
        List<Infos.SchdlChangeReservation> scheduleChangeReservationList = scheduleChangeReservationMethod.schdlChangeReservationGetListDR(objCommon, inParams);

        /*-----------------------------------------------*/
        /*   Set Out Structure                           */
        /*-----------------------------------------------*/
        lotPlanChangeReserveListInqResult.setSchdlChangeReservations(scheduleChangeReservationList);
        return lotPlanChangeReserveListInqResult;
    }
}