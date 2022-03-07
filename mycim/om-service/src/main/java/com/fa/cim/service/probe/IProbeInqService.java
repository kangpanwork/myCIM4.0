package com.fa.cim.service.probe;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/5        ********             Jerry_Huang              create file
 *
 * @author: Jerry_Huang
 * @date: 2020/11/5
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProbeInqService {
    Results.FixtureListInqResult sxProbeListInq(Infos.ObjCommon objCommon, Params.ProbeListInqParams probeListInqParams);

    Results.FixtureIDListInqResult sxProbeIDListInq(Infos.ObjCommon objCommon, Params.ProbeIDListInqParams probeIDListInqParams);

    Results.FixtureStatusInqResult sxProbeStatusInq(Infos.ObjCommon objCommon, ObjectIdentifier objectIdentifier);

    Results.FixtureStockerInfoInqResult sxProbeStockerInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier objectIdentifier);

    Results.FixtureGroupIDListInqResult sxProbeGroupIDListInq(Infos.ObjCommon objCommon);
}
