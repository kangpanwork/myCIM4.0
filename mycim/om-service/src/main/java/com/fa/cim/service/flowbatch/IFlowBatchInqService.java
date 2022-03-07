package com.fa.cim.service.flowbatch;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;

/**
 * description:
 * <p>IFlowBatchInqService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/8/008   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/8/008 16:46
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IFlowBatchInqService {

    Results.FloatingBatchListInqResult sxFloatingBatchListInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) ;

    Results.FlowBatchLotSelectionInqResult sxFlowBatchLotSelectionInq(Infos.ObjCommon strObjCommonIn,ObjectIdentifier equipmentID);

    Results.FlowBatchInfoInqResult sxFlowBatchInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier flowBatchID, ObjectIdentifier lotID, ObjectIdentifier equipmentID) ;

    Results.FlowBatchStrayLotsListInqResult sxFlowBatchStrayLotsListInq(Infos.ObjCommon objCommon);
}
