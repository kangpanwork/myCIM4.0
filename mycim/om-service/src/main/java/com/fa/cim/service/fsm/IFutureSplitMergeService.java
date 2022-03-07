package com.fa.cim.service.fsm;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;

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
public interface IFutureSplitMergeService {
    void sxFSMLotRemoveReq(Infos.ObjCommon objCommon, com.fa.cim.fsm.Params.FSMLotRemoveReqParams params) ;

    List<ObjectIdentifier> sxFSMLotActionReq(Infos.ObjCommon objCommon, ObjectIdentifier lotId, String claimMemo);

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
    void sxFSMLotInfoSetReq(Infos.ObjCommon objCommon, com.fa.cim.fsm.Params.FSMLotInfoSetReqParams params);
}