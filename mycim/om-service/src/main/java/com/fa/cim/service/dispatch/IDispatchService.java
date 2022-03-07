package com.fa.cim.service.dispatch;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * description:
 * This file use to define the IDispatchService interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 16:50
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDispatchService {

    Results.AutoDispatchConfigModifyReqResult sxAutoDispatchConfigModifyReq(Infos.ObjCommon objCommon, Params.AutoDispatchConfigModifyReqParams autoDispatchConfigModifyReqParams);

    void sxCarrierDispatchAttrChgReq(Infos.ObjCommon objCommon, Params.CarrierDispatchAttrChgReqParm carrierTransferJobEndRptParams);

    Results.MoveInReserveCancelForIBReqResult sxMoveInReserveCancelForIBReqService(Infos.ObjCommon objCommon, Params.MoveInReserveCancelForIBReqParams params);

    Results.MoveInReserveCancelReqResult sxMoveInReserveCancelReqService(Infos.ObjCommon objCommon, Params.MoveInReserveCancelReqParams params);

    ObjectIdentifier sxMoveInReserveForIBReq(Infos.ObjCommon objCommon, Params.MoveInReserveForIBReqParams params, String apcifControlStatus);

    Results.MoveInReserveForTOTIReqResult sxMoveInReserveForTOTIReq(Infos.ObjCommon objCommon, Params.MoveInReserveForTOTIReqInParam params, String claimMemo);
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/11                               Neyo                create file
     * 2021/3/11                               Neyo                change outPut to Object
     *                                                              1. moveInReserve return MoveInReserveReqResult
     *                                                              2. autoMoveInReserve return EqpAutoMoveInReserveReqResult
     *
     * @author: Neyo
     * @date: 2021/3/11 14:42
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Object sxMoveInReserveReq(Infos.ObjCommon objCommon, Params.MoveInReserveReqParams moveInReserveReqParams);

    Results.EqpFullAutoConfigChgReqResult sxEqpFullAutoConfigChgReq(Infos.ObjCommon objCommon, Params.EqpFullAutoConfigChgReqInParm eqpFullAutoConfigChgReqInParm);

    Results.EqpAutoMoveInReserveReqResult sxEqpAutoMoveInReserveReq(Infos.ObjCommon objCommon, Params.EqpAutoMoveInReserveReqParams params);
}
