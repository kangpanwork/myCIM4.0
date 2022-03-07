package com.fa.cim.tms.method;

import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/14                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/14 16:59
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ICassetteMethod {

    List<ObjectIdentifier> cassetteAllDR(Infos.ObjCommon objCommon);

    Results.DRcassetteXfstatResult cassetteXfstatGetDR(Infos.ObjCommon objCommon, ObjectIdentifier carrierID);

    List<ObjectIdentifier> rtmsCassetteAllDR(Infos.ObjCommon objCommon);

    Infos.CarrierCurrentLocationGetDR carrierCurrentLocationGetDR(Infos.ObjCommon objCommon, ObjectIdentifier carrierID);

    Infos.ReticlePodXferStat reticlePodXferStatGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);
}
