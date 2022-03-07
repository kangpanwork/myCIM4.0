package com.fa.cim.service.durable;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 17:05
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDurableService {

    Results.StartDurablesReservationCancelReqResult sxDrbMoveInReserveCancelReq(Infos.ObjCommon strObjCommonIn, Params.StartDurablesReservationCancelReqInParam strStartDurablesReservationCancelReqInParam, String claimMemo);

    Results.StartDurablesReservationCancelReqResult sxDrbMoveInReserveCancelForIBReq(Infos.ObjCommon strObjCommonIn, Params.StartDurablesReservationCancelReqInParam strStartDurablesReservationCancelReqInParam, String claimMemo);

    ObjectIdentifier sxDrbMoveInReserveReq(Infos.ObjCommon strObjCommonIn, Params.StartDurablesReservationReqInParam strStartDurablesReservationReqInParam, String claimMemo);

    ObjectIdentifier sxDrbMoveInReserveForIBReq(Infos.ObjCommon strObjCommonIn, Params.StartDurablesReservationReqInParam strStartDurablesReservationReqInParam, String claimMemo);

    boolean sxDrbBankInByPostTaskReq(Infos.ObjCommon strObjCommonIn, Params.DurableBankInByPostProcReqInParam strDurableBankInByPostProcReqInParam, String claimMemo);

    void sxDrbBankInCancelReq(Infos.ObjCommon objCommon, Params.DurableBankInCancelReqInParam durableBankInCancelReqInParam, String claimMemo);

    Results.DurableBankMoveReqResult sxDrbBankMoveReq(Results.DurableBankMoveReqResult durableBankMoveReqResult, Infos.ObjCommon objCommon, int seqIndex, Params.DurableBankMoveReqParam strInParm, String claimMemo);

    ObjectIdentifier sxDrbCJStatusChangeReq(Infos.ObjCommon objCommon, Params.DurableControlJobManageReqInParam paramIn);

    Results.DurableDeleteReqResult sxDrbDeleteReq(Results.DurableDeleteReqResult durableDeleteReqResult, Infos.ObjCommon objCommon, ObjectIdentifier durableID, String className, String claimMemo);

    void sxDrbForceSkipReq(Infos.ObjCommon strObjCommonIn, Params.DurableForceOpeLocateReqInParam strDurableForceOpeLocateReqInParam, String claimMemo);

    Results.DurableProcessLagTimeUpdateReqResult sxDrbLagTimeActionReq(Infos.ObjCommon objCommonIn, User user, Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm);

    Results.DurableOperationStartCancelReqResult sxDrbMoveInCancelForIBReq(Infos.ObjCommon strObjCommonIn, Params.DurableOperationStartCancelReqInParam strDurableOperationStartCancelReqInParam, String claimMemo);

    Results.DurableOperationStartCancelReqResult sxDrbMoveInCancelReq(Infos.ObjCommon strObjCommonIn, Params.DurableOperationStartCancelReqInParam strDurableOperationStartCancelReqInParam, String claimMemo);

    Results.DurableOperationStartReqResult sxDrbMoveInForIBReq(Infos.ObjCommon objCommon, Params.DurableOperationStartReqInParam param);

    Results.DurableOpeCompReqResult sxDrbMoveOutForIBReq(Infos.ObjCommon objCommon, Params.DurableOpeCompReqInParam paramIn);

    Results.DurableOpeCompReqResult sxDrbMoveOutReq(Infos.ObjCommon objCommon, Params.DurableOpeCompReqInParam paramIn);

    Results.DurableGatePassResult sxDrbPassThruReq(Results.DurableGatePassResult strDurableGatePassReqResult, Infos.ObjCommon strObjCommonIn, int seqIndex, Params.DurableGatePassReqInParam strDurableGatePassReqInParam);

    Results.DurablePostProcessActionRegistReqResult sxDrbPostTaskRegistReq(Infos.ObjCommon objCommon, Params.DurablePostProcessActionRegistReqInParam paramIn);

    void sxDrbPrfCxCreateReq(Infos.ObjCommon objCommon, Params.DurablePFXCreateReqInParam paramIn);

    void sxDrbPrfCxDeleteReq(Infos.ObjCommon objCommon, Params.DurablePFXDeleteReqInParam paramIn);

    void sxDrbReworkCancelReq(Infos.ObjCommon objCommon, Infos.ReworkDurableCancelReqInParam strInParm, String claimMemo);

    void sxDrbReworkReq(Infos.ObjCommon strObjCommonIn, Params.ReworkDurableReqInParam strReworkDurableReqInParam, String claimMemo);

    void sxDrbSkipReq(Infos.ObjCommon strObjCommonIn, Params.DurableOpeLocateReqInParam strDurableOpeLocateReqInParam, String claimMemo);

    void sxDurableSetReq(Infos.ObjCommon objCommon, boolean updateFlag, String className, Infos.DurableAttribute strDurableAttribute, String claimMemo);

    void sxDurableTransferJobStatusRpt(Infos.ObjCommon objCommon, Params.DurableTransferJobStatusRptParams params);

    void sxHoldDrbReleaseReq(Infos.ObjCommon objCommon, Infos.HoldDurableReleaseReqInParam paramsIn, String claimMemo);

    void sxHoldDrbReq(Infos.ObjCommon objCommon, Params.HoldDurableReqInParam holdDurableReqInParam, String claimMemo);

    void sxMultiDurableStatusChangeReq(Infos.ObjCommon objCommon, Params.MultiDurableStatusChangeReqParams params);

    void sxCarrierDispatchAttrChgReq(Infos.ObjCommon objCommon, Params.CarrierDispatchAttrChgReqParm carrierTransferJobEndRptParams);

    void sxMultipleReticlePodStatusChangeRpt(Infos.ObjCommon objCommon, User user, String reticlePodStatus, List<ObjectIdentifier> reticlePodIDList);

    void sxReticleAllInOutRpt(Infos.ObjCommon objCommon, Params.ReticleAllInOutRptParams params);

    Results.ReticlePodInvUpdateRptResult sxReticlePodInvUpdateRpt(Infos.ObjCommon strObjCommonIn, User requestUserID, ObjectIdentifier stockerID, ObjectIdentifier equipmentID, List<Infos.InventoryReticlePodInfo> strInventoryReticlePodInfo, String claimMemo, Infos.ShelfPosition shelfPosition);

    void sxReticlePodMaintInfoUpdateReq(Infos.ObjCommon objCommon, User user, ObjectIdentifier reticlePodID);

    Results.ReticlePodTransferStatusChangeRptResult sxReticlePodTransferStatusChangeRpt(Infos.ObjCommon objCommon, Params.ReticlePodTransferStatusChangeRptParams params, String claimMemo);

    void sxReticleSortRpt(Infos.ObjCommon objCommon, List<Infos.ReticleSortInfo> strReticleSortInfos, String claimMemo);

    void sxCarrierStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, String cassetteStatus, String claimMemo);

    ObjectIdentifier sxCarrierUsageCountResetReq(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, String claimMemo);

    void sxMultipleCarrierStatusChangeRpt(Infos.ObjCommon objCommon, String cassetteStatus, List<ObjectIdentifier> cassetteID, String claimMemo);

    void sxMultipleReticleStatusChangeRpt(Infos.ObjCommon objCommon, String reticleStatus, List<ObjectIdentifier> reticleID, String claimMemo);

    Results.ReticleStatusChangeRptResult sxReticleStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String reticleStatus, String claimMemo);

    Results.ReticleTransferStatusChangeRptResult sxReticleTransferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier equipmentID, List<Infos.XferReticle> strXferReticle, String claimMemo,boolean byUrl);

    ObjectIdentifier sxReticleUsageCountResetReq(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String claimMemo);

    Results.DurableBankInReqResult sxDrbBankInReq(Results.DurableBankInReqResult strDurableBankInReqResult, Infos.ObjCommon objCommon, int seqIndex, Params.DurableBankInReqInParam strInParm, String claimMemo);

    void sxDurableJobStatusChangeReq(Infos.ObjCommon objCommon, Params.DurableJobStatusChangeReqParams params);

    void sxDurableJobStatusChangeRpt(Infos.ObjCommon objCommon, Params.DurableJobStatusChangeRptParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strReworkDurableWithHoldReleaseReqInParam
     * @return void
     * @exception 
     * @author ho
     * @date 2020/10/19 12:53
     */
    void sxReworkDurableWithHoldReleaseReq (
            Infos.ObjCommon                            strObjCommonIn,
            Params.ReworkDurableWithHoldReleaseReqInParam strReworkDurableWithHoldReleaseReqInParam);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param cassetteID
     * @param claimMemo
     * @return void
     * @exception 
     * @author ho
     * @date 2020/10/21 15:43
     */
    void sxAutoClean(Infos.ObjCommon objCommon,ObjectIdentifier cassetteID,String claimMemo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param cassetteID
     * @param claimMemo
     * @return void
     * @exception 
     * @author ho
     * @date 2020/10/21 15:46
     */
    void sxAutoAvailableForBankIn(Infos.ObjCommon objCommon,ObjectIdentifier cassetteID,String claimMemo);


    /**
     * description: This function sends request to change reticle pod port access mode.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 10:36
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxEqpRSPPortAccessModeChangeReq(Infos.ObjCommon objCommon, Params.EqpRSPPortAccessModeChangeReqParams params);

    /**
     * description: This function requests that reticle should be retrieved from equipment when equipment is off-line mode.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 10:43
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticleOfflineRetrieveReq(Infos.ObjCommon objCommon, Params.ReticleOfflineRetrieveReqParams params);

    /**
     * description: This function is sent from EAP to report reticle pod port access mode change.
     *              OMS server will update the change to database if it is valid.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/6 15:30
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxEqpRSPPortStatusChangeRpt(Infos.ObjCommon objCommon, Params.EqpRSPPortStatusChangeRpt params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/5 16:09                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/5 16:09
     * @param objCommon
     * @param params -
     * @return void
     */
    void sxReticleRetrieveReq(Infos.ObjCommon objCommon, Params.ReticleRetrieveReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/12 14:07                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/11/12 14:07
     * @param objCommon
     * @param params -
     * @return void
     */

    void sxReticleOfflineStoreReq(Infos.ObjCommon objCommon, Params.ReticleOfflineStoreReqParams params);

    /**
     * Description:
     * verify input parameter
     * update FQRTCLJOBREQ table (Remove record)
     * update following information based on input paramter
     * Equipment
     *   - reticle - equipment relation will be canceled.
     * Reticle
     *   - reticle - equipment relation will be canceled.
     *   - reticle - reticle pod relation will be created.
     * Reticle pod
     *   - reticle - reticle pod relation will be created.
     *   - if slot-reticle reserve information is same as input parameter,
     *     that slot reserve information will also be removed.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/12 11:08                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/12 11:08
     * @param objCommon
     * @param params -
     * @return void
     */
    void sxReticleRetrieveRpt(Infos.ObjCommon objCommon, Params.ReticleRetrieveRptParams params);

    /**
     * description:verify input parameter
     * update FQRTCLJOBREQ table
     * notify job information to TCS
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/13 16:57                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/13 16:57
     * @param objCommon
     * @param params -
     * @return void
     */
    void sxReticleStoreReq(Infos.ObjCommon objCommon, Params.ReticleStoreReqParams params);

    /**
     * description:verify  input parameter
     * update  FQRTCLJOBREQ table (Remove record)
     * update following information based on input parameter
     * Equipment
     *     - reticle - equipment reialtion will be created.
     * Reticle
     *     - reticle - equipment reialtion will be created.
     *     - reticle - reticle pod relation will be canceled.
     * Reticle pod
     *     - reticle - reticle pod relation will be canceled.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/16 10:13                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/16 10:13
     * @param objCommon
     * @param params -
     * @return void
     */
    void sxReticleStoreRpt(Infos.ObjCommon objCommon, Params.ReticleStoreRptParams params);

    /**
     * description: This function receives report of unloading ReticlePod from the port of equipment.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/17 15:10
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticlePodUnloadingRpt(Infos.ObjCommon objCommon, Params.ReticlePodUnloadingRptParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/16 16:48                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/16 16:48
     * @param objCommon
     * @param params -
     * @return void
     */
    void sxReticlePodLoadingRpt(Infos.ObjCommon objCommon, Params.ReticlePodLoadingRptParams params);

    /**
     * description:This function changes the online mode for a bare reticle stocker.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/20 17:21
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxBareReticleStockerOnlineModeChangeReq(Infos.ObjCommon objCommon, Params.BareReticleStockerOnlineModeChangeReqParams params);

    /**
     * description: This function updates Inventory information of ReticlePod related to specified stocker or equipment.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/20 19:53
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.ReticlePodInventoryReqResult sxReticlePodInventoryReq(Infos.ObjCommon objCommon, Params.ReticlePodInventoryReqParams params);

    /**
     * description: This function will send request to load a reticle pod to a particular port offline.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/21                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/21 11:04
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticlePodOfflineLoadingReq(Infos.ObjCommon objCommon, Params.ReticlePodOfflineLoadingReqParams params);


    /**
     * description: This function requests that ReticlePod should be unloaded from the port of equipment.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/23                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/23 10:42
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticlePodOfflineUnloadingReq(Infos.ObjCommon objCommon, Params.ReticlePodOfflineUnloadingReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/23 10:51                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/23 10:51
     * @param objCommon
     * @param params -
     * @return com.fa.cim.dto.Results.ReticleInventoryReqResult
     */
    Results.ReticleInventoryReqResult sxReticleInventoryReq(Infos.ObjCommon objCommon, Params.ReticleInventoryReqParams params);

    /**
     * description: lot cassette post process force delete req
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                                       - common
     * @param strLotCassettePostProcessForceDeleteReqInParams - req
     * @author YJ
     * @date 2021/3/11 0011 14:55
     */
    void sxLotCassettePostProcessForceDeleteReq(Infos.ObjCommon objCommon, Params.StrLotCassettePostProcessForceDeleteReqInParams strLotCassettePostProcessForceDeleteReqInParams);

}
