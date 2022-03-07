package com.fa.cim.service.sort;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

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
public interface ISortService {

    void sxOnlineSorterActionCancelReq(Infos.ObjCommon objCommon, Params.OnlineSorterActionCancelReqParm params);

    void sxOnlineSorterActionExecuteReq(Infos.ObjCommon objCommon, Params.OnlineSorterActionExecuteReqParams params) ;

    void sxOnlineSorterRpt(Infos.ObjCommon objCommon, Params.OnlineSorterRptParams params);

    void sxOnlineSorterSlotmapAdjustReq(Infos.ObjCommon objCommon, Params.OnlineSorterSlotmapAdjustReqParam params);

    List<Infos.WaferSorterCompareCassette> sxOnlineSorterSlotmapCompareReq(Infos.ObjCommon objCommon, String portGroup, ObjectIdentifier equipmentID,String actionCode) ;

    void sxSJCancelReq(Infos.ObjCommon objCommon, Params.SJCancelReqParm params);

    void sxSJConditionCheckReq(Infos.ObjCommon objCommon, Params.SortJobCheckConditionReqInParam params);

    ObjectIdentifier sxSJCreateReq(Infos.ObjCommon objCommon, Params.SJCreateReqParams params) ;

    void sxSJStartReq(Infos.ObjCommon objCommon, Infos.SJStartReqInParm params, String claimMemo) ;

    void sxSJStatusChgRpt(Infos.ObjCommon objCommon, Params.SJStatusChgRptParams params);

    void sxSortJobPriorityChangeReq(Infos.ObjCommon objCommon, Params.SortJobPriorityChangeReqParam param);

    void sxWaferSorterActionRegisterReq(Infos.ObjCommon objCommon, Params.WaferSorterActionRegisterReqParams params) ;

    void sxWaferSlotmapChangeReq(Infos.ObjCommon objCommon, Params.WaferSlotmapChangeReqParams params);

    void sxCarrierExchangeReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.WaferTransfer> waferXferList, String claimMemo);

    void sxWaferSlotmapChangeRpt(Infos.ObjCommon objCommon, Params.WaferSlotmapChangeRptParams params);


}
