package com.fa.cim.service.sort;

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
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:34
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISortInqService {

    Results.OnlineSorterActionSelectionInqResult sxOnlineSorterActionSelectionInq(Infos.ObjCommon objCommon, Params.OnlineSorterActionSelectionInqParams params) ;

    List<Infos.WaferSorterSlotMap> sxOnlineSorterActionStatusInq (Infos.ObjCommon objCommon, Params.OnlineSorterActionStatusInqParm params) ;

    List<Infos.LotWaferMap> sxOnlineSorterScrapWaferInq(Infos.ObjCommon objCommon, Params.OnlineSorterScrapWaferInqParams params) ;

    Results.SJInfoForAutoLotStartInqResult sxSJInfoForAutoLotStartInq(Infos.ObjCommon objCommon) ;

    List<Infos.SortJobListAttributes> sxSJListInq(Infos.ObjCommon objCommon, Params.SJListInqParams params) ;

    Results.SJStatusInqResult sxSJStatusInq(Infos.ObjCommon objCommon, ObjectIdentifier sorterJobID);

    String reqCategoryGetByLot(Infos.ObjCommon objCommon, ObjectIdentifier lotID);


}
