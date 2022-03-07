package com.fa.cim.service.flowbatch;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>IFlowBatchService .<br/></p>
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
public interface IFlowBatchService {
    Results.FlowBatchByManualActionReqResult sxFlowBatchByManualActionReq(Infos.ObjCommon objCommon, Params.FlowBatchByManualActionReqParam param) ;

    Results.FlowBatchLotRemoveReqResult sxFlowBatchLotRemoveReq(Infos.ObjCommon objCommon, Params.FlowBatchLotRemoveReq param);

    Results.FlowBatchByAutoActionReqResult sxFlowBatchByAutoActionReq(Infos.ObjCommon objCommon, Params.FlowBatchByAutoActionReqParams params);

    void sxEqpMaxFlowbCountModifyReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, Integer flowBatchMaxCount, String claimMemo) ;

    void sxEqpReserveCancelForflowBatchReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier flowBatchID, String claimMemo);

    List<Infos.FlowBatchedLot> sxEqpReserveForFlowBatchReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier flowBatchID, String claimMemo) ;

    void sxFlowBatchCheckForLotSkipReq(Infos.ObjCommon objCommon, Params.FlowBatchCheckForLotSkipReqParams params);

    Results.ReFlowBatchByManualActionReqResult sxReFlowBatchByManualActionReq(Infos.ObjCommon objCommon, Params.ReFlowBatchByManualActionReqParam param);


}
