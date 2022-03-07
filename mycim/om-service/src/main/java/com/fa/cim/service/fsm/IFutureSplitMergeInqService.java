package com.fa.cim.service.fsm;

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
public interface IFutureSplitMergeInqService {
    com.fa.cim.fsm.Results.FSMLotInfoInqResult sxFSMLotInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber);

    List<com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo> sxFSMLotDefinitionListInq(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, Boolean detailRequireFlag) ;
}