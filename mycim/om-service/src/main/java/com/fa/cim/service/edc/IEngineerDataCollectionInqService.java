package com.fa.cim.service.edc;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>IEngineerDataCollectionInqService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/8/008   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/8/008 16:30
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEngineerDataCollectionInqService {

    Results.EDCSpecCheckActionResultInqResult sxEDCSpecCheckActionResultInq(Infos.ObjCommon objCommon, Params.EDCSpecCheckActionResultInqInParms edcSpecCheckActionResultInqInParms);

    Results.EDCDataItemListByKeyInqResult sxEDCDataItemListByKeyInq(Infos.ObjCommon objCommon, String searchKeyPattern, List<Infos.HashedInfo> searchKeys);

    List<Infos.DataCollection> sxEDCConfigListInq(Infos.ObjCommon objCommon, Infos.EDCConfigListInqInParm parm);

    Results.EDCDataItemWithTransitDataInqResult sxEDCDataItemWithTransitDataInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID);

    Results.SpecCheckResultInqResult sxSpecCheckResultInq(Infos.ObjCommon objCommon, Params.SpecCheckResultInqInParms specCheckResultInqInParms);

    Results.EDCPlanInfoInqResult sxEDCPlanInfoInq(Infos.ObjCommon objCommon, Params.EDCPlanInfoInqParms edcPlanInfoInqParms);

    Results.EDCSpecInfoInqResult sxEDCSpecInfoInq(Infos.ObjCommon objCommon, Params.EDCSpecInfoInqParms edcSpecInfoInqParms);

    Results.EDCDataShowForUpdateInqResult sxEDCDataShowForUpdateInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    Results.EDCDataItemListByCJInqResult sxEDCDataItemListByCJInq(Infos.ObjCommon objCommon, Params.EDCDataItemListByCJInqParams params);
}
