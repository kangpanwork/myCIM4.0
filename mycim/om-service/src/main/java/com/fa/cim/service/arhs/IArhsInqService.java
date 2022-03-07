package com.fa.cim.service.arhs;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/3 16:16
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IArhsInqService {
    /**
     * description: This function inquires of RTD and acquires the newest action list.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/4 10:47
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.WhatReticleActionListInqResult sxWhatReticleActionListInq(Infos.ObjCommon objCommon, Params.WhatReticleActionListInqParams params);

    /**
     * description: This function will list the reticles that are to available to be retrieved from equipment buffer..
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/4 10:47
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.StoredReticle> sxWhatReticleRetrieveInq(Infos.ObjCommon objCommon, Params.WhatReticleRetrieveInqParams params);

    /**
     * description: This function will give the destination (equipment, bare reticle stocker,
     * reticle pod stocker) which the reticle pod should be tranfered.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/4 10:47
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.WhereNextForReticlePodInqResult sxWhereNextForReticlePodInq(Infos.ObjCommon objCommon, Params.WhereNextForReticlePodInqParams params);


    /**
     * description: This function will list all associated reticle component jobs in sequence for a given reticle dispatch job.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/20 19:20
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.ReticleComponentJob> sxReticleComponentJobListInq(Infos.ObjCommon objCommon, Params.ReticleComponentJobListInqParams params);

    /**
     * description: This function list all reticle dispatch jobs.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/20 19:29
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.ReticleDispatchJob> sxReticleDispatchJobListInq(Infos.ObjCommon objCommon);

    /**
     * description:-ReticlePod selection rule-
     * //    1. ReticlePod should be empty.(if input reticle is in reticle pod, it's exceptional case.)
     * //    2. ReticlePod should be on "Auto" accessMode and "LoadComp" status port, or reticlePod should be in reticlePodStocker.
     * //    3. ReticlePod should not be reserved by any RDJ.
     * //       (though that RDJ is not released yet, that kind of reticlePod is not listed by this transaction.)
     * //
     * //  -ReticlePod selection priority-
     * //    1. If reticle already has reserved RDJ job, and if that job information has reticlePod association infomation,
     * //       MM selects that reticlePod.
     * //    2. If reticle is in reticlePod, MM selects that reticlePod.
     * //    3. If input reticle is in equipment ot bareReticleStocker, and if that machine has empty reticlePod on port,
     * //       MM selects that reticlePod.
     * //    4. If input reticle is in equipment, and if multiple reticlePodStocker is associated to the equipment,
     * //       MM selects reticlePod which is in those reticlePodStockers.
     * //    5. If no empty reticlePod is listed by above selection logic, MM gets reticle current location,
     * //       and gets reticle current work area, and MM selection logic, MM gets empty reticlePod in the same work area.
     * //    6. If no empty reticlePod is listed by above selection logic, MM gets empty reticlePod which is on equipment port and selects
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/24 12:55                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/24 12:55
     * @param objCommon
     * @param params -
     * @return com.fa.cim.dto.Results.WhatReticlePodForReticleXferInqResult
     */
    Results.WhatReticlePodForReticleXferInqResult sxWhatReticlePodForReticleXferInq(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    /**
     * description: This function will list reticle pod transfer jobs according to given search conditions.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/24 13:14
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.ReticlePodXferJobListInqResult sxReticlePodXferJobListInq(Infos.ObjCommon objCommon, Params.ReticlePodXferJobListInqParams params);
}
