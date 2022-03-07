package com.fa.cim.service.psm;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

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
 * @date: 2020/9/8 16:29
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPlannedSplitMergeService {
    void sxPSMLotRemoveReq(Infos.ObjCommon objCommon, Params.PSMLotRemoveReqParams params) ;

    List<ObjectIdentifier> sxPSMLotActionReq(Infos.ObjCommon objCommon, ObjectIdentifier lotId, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/25                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/25 14:26
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxPSMLotInfoSetReq(Infos.ObjCommon objCommon, Params.PSMLotInfoSetReqParams params);
}