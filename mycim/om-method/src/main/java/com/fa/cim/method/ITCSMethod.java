package com.fa.cim.method;

import com.fa.cim.common.constant.TCSReqEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.*;

/**
 * description:
 * This file use to define the ITCSMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/10        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/4/10 17:54
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ITCSMethod {

    /**
     * description: General purpose EAP interface
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/10 10:31
     * @param tcsReqEnum
     * @param tcsIn -
     * @return TCSOut
     */
    Outputs.TCSOut sendTCSReq(TCSReqEnum tcsReqEnum, Inputs.TCSIn tcsIn);

    void sendCarrierOutFromIBReq(Infos.ObjCommon strObjCommonIn, User requestUserID, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, String claimMemo);

    Results.DurableOperationStartCancelReqResult sendDurableOpeStartCancelReq(Infos.ObjCommon objCommon, Infos.SendDurableOpeStartCancelReqIn sendDurableOpeStartCancelReqIn);

    void sendDurableControlJobActionReq(Infos.ObjCommon objCommon, Infos.SendDurableControlJobActionReqIn sendDurableControlJobActionReqIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strTCSMgr_SendStartDurablesReservationReq_in
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @exception
     * @author ho
     * @date 2020/7/3 14:24
     */
     ObjectIdentifier sendStartDurablesReservationReq(
            Infos.ObjCommon                                    strObjCommonIn,
            Infos.SendStartDurablesReservationReqIn strTCSMgr_SendStartDurablesReservationReq_in );

     /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
       * @param strObjCommonIn
      * @param strTCSMgrSendStartDurablesReservationCancelReqin
      * @return com.fa.cim.dto.Results.StartDurablesReservationCancelReqResult
      * @exception
      * @author ho
      * @date 2020/7/7 13:07
      */
      Results.StartDurablesReservationCancelReqResult sendStartDurablesReservationCancelReq(
            Infos.ObjCommon                                       strObjCommonIn,
            Params.StartDurablesReservationCancelReqInParam strTCSMgrSendStartDurablesReservationCancelReqin );

      /**
       * description:
       * change history:
       * date             defect#             person             comments
       * ---------------------------------------------------------------------------------------------------------------------
       * 2020/11/11                               Neyo                create file
       *
       * @author: Neyo
       * @date: 2020/11/11 14:57
       * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
       */
    void sendReticleStoreCancelReq(TCSReqEnum tcsReqEnum, Inputs.SendReticleStoreCancelReqIn sendReticleStoreCancelReqIn);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/11 15:32
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sendReticleRetrieveCancelReq(TCSReqEnum tcsReqEnum, Inputs.SendReticleRetrieveCancelReqIn sendReticleRetrieveCancelReqIn);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/11 15:50
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sendReticlePodUnclampCancelReq(TCSReqEnum tcsReqEnum, Inputs.SendReticlePodUnclampCancelReqIn sendReticlePodUnclampCancelReqIn);
}