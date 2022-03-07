package com.fa.cim.service.psm;

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
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 16:30
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPlannedSplitMergeInqService {
    Results.PSMLotInfoInqResult sxPSMLotInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber);

    List<Infos.ExperimentalLotInfo> sxPSMLotDefinitionListInq(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, Boolean detailRequireFlag) ;
}