package com.fa.cim.controller.interfaces.fmc;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * <p>IFMCController .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/21 11:01
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface IFMCController {

    /**
     * This function is SLM version of start lot reservation which should be used for SLM capable equipment.
     * <p> This function will reserve equipment container positions in addition to normal start lot reservation.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:12
     */
    Response fmcMoveInReserveReq(Params.SLMStartLotsReservationReqInParams slmStartLotsReservationReqInParams);

    /**
     * This function inserts or updates SLM definition for retrieving a process ended cassette from Equipment.
     * <p> This information is reported to TCS.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:15
     */
    Response fmcWaferRetrieveCarrierReserveReq(Params.SLMWaferRetrieveCassetteReserveReqInParams slmWaferRetrieveCassetteReserveReqInParams);

    /**
     * This function reports process job status for SLM operation
     * <p> Currently, following actionCode is supported:
     * <p> ProcessStart: process job is started (before store)
     * <p> ProcessingComp: process job is completed (before retrieve)
     * <p> Information is reported from TCS.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:17
     */
    Response fmcProcessJobStatusRpt(Params.SLMProcessJobStatusRptInParams slmProcessJobStatusRptInParams);

    /**
     * This function reports wafer store for SLM.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:20
     */
    Response fmcWaferStoreRpt(Params.SLMWaferStoreRptInParams slmWaferStoreRptInParams);

    /**
     * This function reports wafer retrieve for SLM.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:22
     */
    Response fmcWaferRetrieveRpt(Params.SLMWaferRetrieveRptInParams slmWaferRetrieveRptInParams);

    /**
     * This function reports cassette detach from control job for SLM.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:24
     */
    Response fmcCarrierRemoveFromCJReq(Params.SLMCassetteDetachFromCJReqInParams slmCassetteDetachFromCJReqInParams);


    /**
     *  This function requests cassette unclamp when equipment is online
     *  <p> The request will be passed to TCS to perform physical unclamp</p>
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/5/7 13:03
     */
    Response fmcCarrierUnclampReq (Params.SLMCassetteUnclampReqInParams slmCassetteUnclampReqInParams);

    /**
     * This function request OMS to change FMC switch at runtime
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/5/7 13:06
     */
    Response fmcModeChangeReq(Params.SLMSwitchUpdateReqInParams slmSwitchUpdateReqInParams);

    Response fmcRsvMaxCountUpdateReq(Params.FmcRsvMaxCountUpdateReqInParams fmcRsvMaxCountUpdateReqInParams);
}
