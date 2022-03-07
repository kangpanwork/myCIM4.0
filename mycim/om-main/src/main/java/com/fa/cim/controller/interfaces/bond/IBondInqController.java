package com.fa.cim.controller.interfaces.bond;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * <p>IBondingInqController .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/16 10:22
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface IBondInqController {

    /**
     * This function obtains List of Bonding Group that fulfills specified condition.
     * <p> The following search conditions can be specified by "AND".
     * <p> -Bonding Group ID
     * <p> -Bonding Group State
     * <p> -Target Equipment ID
     * <p> -Control Job ID
     * <p> -User ID
     * <p> -Lot ID in Bonding Map Information
     * <p> -Base Product ID in Bonding Map Information
     * <p> -Top Product ID in Bonding Map Information
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 16:52
     */
    Response bondingGroupListInq(Params.BondingGroupListInqInParams bondingGroupListInqInParams);

    /**
     * This function obtains the relation map between Base Wafer and Top Wafer based on input.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 16:55
     */
    Response bondedWaferListInq(Params.StackedWaferListInqInParams stackedWaferListInqInParams);

    /**
     * This function returns Bonding Flow Name List.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 16:58
     */
    Response bondingFLowListInq(Params.BondingFLowListInqInParams bondingFLowListInqInParams);

    /**
     * This function obtains Lot List which belongs to specified Bonding Flow Section.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 17:06
     */
    Response lotListByBondingFlowInq(Params.LotListInBondingFlowInqInParams lotListInBondingFlowInqInParams);

    /**
     * This function obtains Top Lot List which can be target of specified Base Product,
     * specified bonding process, and specified Target Equipment (if specified).
     * <p> If bonding process is defined as target operation of bonding flow section,
     * return Top Lot List which belongs to the same bonding flow section.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 17:08
     */
    Response bondingLotListInq(Params.BondingLotListInqInParams bondingLotListInqInParams);

    /**
     * This function obtains Equipment List for specified Bonding Group(s).
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/5/6 12:50
     */
    Response eqpBondingCandidateInq(Params.EqpCandidateForBondingInqInParams eqpCandidateForBondingInqInParams);
}
