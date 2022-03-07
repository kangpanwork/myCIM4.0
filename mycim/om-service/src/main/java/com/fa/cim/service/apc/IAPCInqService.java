package com.fa.cim.service.apc;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
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
 * @date: 2020/9/8 12:48
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAPCInqService {

    List<Infos.APCIf> sxAPCInterfaceListInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    List<Infos.StartCassette> sxAPCRecipeParameterAdjustInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette, List<Infos.APCRunTimeCapabilityResponse> strAPCRunTimeCapabilityResponse, boolean finalBoolean);

    List<Infos.APCRunTimeCapabilityResponse> sxAPCRunTimeCapabilityInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> strStartCassette, boolean sendTxFlag);

    Results.EntityListInqResult sxEntityListInq(Infos.ObjCommon objCommon, String entityClass, String searchKeyName, String searchKeyValue, String option);
}
