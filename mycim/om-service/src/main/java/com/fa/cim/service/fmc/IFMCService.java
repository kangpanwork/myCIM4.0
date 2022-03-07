package com.fa.cim.service.fmc;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>IFMCController .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/21 11:01
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface IFMCService {

    /**
     * This function is SLM version of start lot reservation which should be used for SLM capable equipment.
     * <p> This function will reserve equipment container positions in addition to normal start lot reservation.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:12
     */
    ObjectIdentifier sxSLMStartLotsReservationReq(Infos.ObjCommon objCommon,
                                                  Params.SLMStartLotsReservationReqInParams slmStartLotsReservationReqInParams,
                                                  AtomicReference<String> APCIFControlStatus);

    /**
     * This function inserts or updates SLM definition for retrieving a process ended cassette from Equipment.
     * <p> This information is reported to TCS.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:15
     */
    Results.SLMWaferRetrieveCassetteReserveReqResult sxSLMWaferRetrieveCassetteReserveReq(Infos.ObjCommon objCommon, Params.SLMWaferRetrieveCassetteReserveReqInParams slmWaferRetrieveCassetteReserveReqInParams);
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
    void sxSLMProcessJobStatusRpt(Infos.ObjCommon objCommon, Params.SLMProcessJobStatusRptInParams slmProcessJobStatusRptInParams);

    /**
     * This function reports wafer store for SLM.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:20
     */
    void sxFMCWaferStoreRpt(Infos.ObjCommon objCommon, Params.SLMWaferStoreRptInParams slmWaferStoreRptInParams);

    /**
     * This function reports wafer retrieve for SLM.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:22
     */
    void sxFMCWaferRetrieveRpt(Infos.ObjCommon objCommon, Params.SLMWaferRetrieveRptInParams params) ;

    /**
     * This function reports cassette detach from control job for SLM.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:24
     */
    void sxSLMCassetteDetachFromCJReq(Infos.ObjCommon objCommon, Params.SLMCassetteDetachFromCJReqInParams slmCassetteDetachFromCJReqInParams);


    /**
     *  This function requests cassette unclamp when equipment is online
     *  <p> The request will be passed to TCS to perform physical unclamp</p>
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/5/7 13:03
     */
    void sxFMCCassetteUnclampReq(Infos.ObjCommon objCommon, Params.SLMCassetteUnclampReqInParams params);

    /**
     * This function request OMS to change FMC switch at runtime
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/5/7 13:06
     */
    void sxFMCModeChangeReq(Infos.ObjCommon objCommon, Params.SLMSwitchUpdateReqInParams slmSwitchUpdateReqInParams) ;


    void sxFMCRsvMaxCountUpdateReq(Infos.ObjCommon objCommon,
                                   Params.FmcRsvMaxCountUpdateReqInParams fmcRsvMaxCountUpdateReqInParams);
}
