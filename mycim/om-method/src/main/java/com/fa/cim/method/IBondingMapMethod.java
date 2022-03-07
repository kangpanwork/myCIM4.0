package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/10/9          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/10/9 14:54
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IBondingMapMethod {
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @since 2019/10/9 14:56
     * @param objCommon -
     * @param strBondingMapInfoSeq -
     * @param targetEquipmentID -
     * @param actualTopFlag -
     */
    void bondingMapInfoConsistencyCheck(Infos.ObjCommon objCommon, List<Infos.BondingMapInfo> strBondingMapInfoSeq, ObjectIdentifier targetEquipmentID, boolean actualTopFlag);

    /**
     * This function inquires information of specified bonding group.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 10:43
     */
    Outputs.ObjBondingMapFillInTxPCR003DROut bondingMapFillInTxPCR003DR(Infos.ObjCommon objCommon, List<Infos.BondingMapInfo> strBondingMapInfoSeq);

    /**
     * This function inquires information of specified bonding group.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 13:11
     */
    List<Infos.BondingMapInfo> bondingMapResultMerge(Infos.ObjCommon objCommon, List<Infos.BondingMapInfo> strBondingMapInfoSeq, List<Infos.BondingGroupInfo> strBondingGroupInfoSeq);

    /**
     * Update the Bonding Group and related child tables.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 13:40
     */
    void bondingMapResultUpdateDR(Infos.ObjCommon objCommon, List<Infos.BondingMapInfo> strBondingMapInfoSeq);

    /**
     * This function makes wafers in the Bonding Map Stacked state.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/26 10:46
     */
    void bondingMapWaferStackMake(Infos.ObjCommon objCommon, List<Infos.BondingMapInfo> bondingMapInfoSeq);

    /**
     *
     *
     * @param objCommon
     * @param strBondingMapInfoSeq
     * @param bondingGroupReleaseLotWaferInfos
     * @author Yuri
     */
    Outputs.BondingMapInfoConsistencyCheckForPartialReleaseOut bondingMapInfoConsistencyCheckForPartialRelease(Infos.ObjCommon objCommon,
                                                                                                               List<Infos.BondingMapInfo> strBondingMapInfoSeq,
                                                                                                               List<Infos.BondingGroupReleaseLotWafer> bondingGroupReleaseLotWaferInfos);

}