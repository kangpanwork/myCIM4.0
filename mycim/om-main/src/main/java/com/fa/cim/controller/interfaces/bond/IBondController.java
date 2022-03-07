package com.fa.cim.controller.interfaces.bond;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * <p>IBondingController .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/16 10:21
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface IBondController {

    /**
     * This function manages specified Bonding Group and Bonding Map Information based on update mode.
     * <p> Create : Create Bonding Group and its Bonding Map Information based on input.
     * <p> Update : Update Target Equipment.
     * <p> Delete : Delete Bonding Group and its Bonding Map Information.
     *
     * @param bondingGroupUpdateReqInParams {@link Params.BondingGroupUpdateReqInParams}
     * @return {@link Response}
     * @version 1.0
     * @author ZQI
     * @date 2020/4/16 10:49
     */
    Response bondingGroupModifyReq(Params.BondingGroupUpdateReqInParams bondingGroupUpdateReqInParams);

    /**
     * This function reports bonding process result, and update Bonding Map Information.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 10:20
     */
    Response bondingMapRpt(Params.BondingMapResultRptInParams bondingMapResultRptInParams);

    /**
     * This function claims wafer stacking requests for specified Bonding Group.
     * <p> It works as follows based on Bonding Map Information, which is defined in Bonding Group.
     * <p> + Change the State of Top Lot to be CIMFW_Lot_State_Finished
     * <p> + Change the Finished State of Top Lot to be SP_LOT_FINISHED_STATE_STACKED
     * <p> + Change the Scrap State of Top Wafer to be SP_ScrapState_Stacked
     * <p> + Detach Top Lot / Wafer from FOUP
     * <p> + Maintain Stacked Wafer Information of Base Wafer
     * <p> + Maintain Alias Wafer Name of Base Wafer
     * <p> + Delete Bonding Group
     * <p> + Make History
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 17:55
     */
    Response waferBondReq(Params.WaferStackingReqInParams waferStackingReqInParams);

    /**
     * This function claims wafer stacking cancel requests for specified Top Lot (s).
     * <p> It works as follows:
     * <p> + Change the State of Top Lot to be previous one
     * <p> + Change the Finished State of Top Lot to be previous one
     * <p> + Change the Scrap State of Top Wafer to be SP_ScrapState_Active
     * <p> + Maintain Stacked Wafer Information of Base Wafer
     * <p> + Maintain Alias Wafer Name of Base Wafer
     * <p> + Make History
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 18:05
     */
    Response waferBondCancelReq(Params.WaferStackingCancelReqInParams waferStackingCancelReqInParams);

    /**
     * This function release specified Base Wafers and Top Wafers from Bonding Group.
     * <p> If partial wafers in lot are target, this function performs lot split.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/21 10:48
     */
    Response bondingGroupPartialRemoveReq(Params.BondingGroupPartialReleaseReqInParam bondingGroupPartialReleaseReqInParams);
}
