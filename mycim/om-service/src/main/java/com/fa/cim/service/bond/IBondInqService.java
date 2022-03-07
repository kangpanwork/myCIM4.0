package com.fa.cim.service.bond;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 17:18
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IBondInqService {
    List<String> sxBondingFlowSectionListInq(Infos.ObjCommon objCommon);
    List<Infos.BondingGroupInfo> sxBondingGroupListInq(Infos.ObjCommon objCommon, Params.BondingGroupListInqInParams bondingGroupListInqInParams);
    List<Infos.BondingLotAttributes> sxBondingLotListInq(Infos.ObjCommon objCommon, Params.BondingLotListInqInParams bondingLotListInqInParams);
    List<Infos.AreaEqp> sxEqpCandidateForBondingInq(Infos.ObjCommon objCommon, Params.EqpCandidateForBondingInqInParams eqpCandidateForBondingInqInParams);
    List<Infos.LotInBondingFlowInfo> sxLotListInBondingFlowInq(Infos.ObjCommon objCommon, Params.LotListInBondingFlowInqInParams params);
    List<Infos.StackedWaferInfo> sxStackedWaferListInq(Infos.ObjCommon objCommon, Params.StackedWaferListInqInParams stackedWaferListInqInParams);
}
