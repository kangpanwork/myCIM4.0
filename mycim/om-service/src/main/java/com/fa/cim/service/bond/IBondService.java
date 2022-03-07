package com.fa.cim.service.bond;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

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
 * @date: 2020/9/8 17:19
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IBondService {
    List<Infos.BondingGroupReleasedLot> sxBondingGroupPartialReleaseReq(Infos.ObjCommon objCommon, Params.BondingGroupPartialReleaseReqInParam param);

    Results.BondingGroupUpdateReqResult sxBondingGroupUpdateReq(Infos.ObjCommon objCommon, Params.BondingGroupUpdateReqInParams strBondingGroupUpdateReqInParm);

    void sxBondingMapResultRpt(Infos.ObjCommon objCommon, Params.BondingMapResultRptInParams bondingMapResultRptInParams);

    void sxWaferStackingCancelReq(Infos.ObjCommon objCommon, Params.WaferStackingCancelReqInParams waferStackingCancelReqInParams);

    void sxWaferStackingReq(Infos.ObjCommon objCommon, Params.WaferStackingReqInParams waferStackingReqInParams);

    void lotWaferStackMake(Infos.ObjCommon objCommon, List<ObjectIdentifier> topLotIDSeq);

}
