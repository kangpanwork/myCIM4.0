package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.*;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018-07-15                           Paladin           create file
 * <p>
 * return:
 *
 * @author Paladin
 * @date: 2018-07-15 19:23
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

public interface IDurableMethod {
    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableHistory_GetDR_in
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.DurableHistoryGetDROut>
     * @exception
     * @author Ho
     * @date 2019/4/29 15:39
     */
    Infos.DurableHistoryGetDROut durableHistoryGetDR(Infos.ObjCommon strObjCommonIn, Infos.DurableHistoryGetDRIn strDurableHistory_GetDR_in);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableID
     * @param durableCategory
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     * @author Ho
     * @date 2018/10/29 15:25:34
     */
    ObjectIdentifier durableDurableControlJobIDGet(Infos.ObjCommon objCommon, ObjectIdentifier durableID, String durableCategory);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableControlJobID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.DurableControlJobStatusGet>
     * @author Ho
     * @date 2018/10/29 15:36:37
     */
    Infos.DurableControlJobStatusGet durableControlJobStatusGet(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableCategory
     * @param durableID
     * @return com.fa.cim.dto.RetCode<java.lang.Boolean>
     * @author Ho
     * @date 2018/10/26 14:29:56
     */
    Boolean durableInPostProcessFlagGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableID
     * @param durableCategory
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.DurableHoldRecord>>
     * @author Ho
     * @date 2018/10/19 10:03:25
     */
    List<Infos.DurableHoldRecord> durableHoldRecordGetDR(Infos.ObjCommon objCommon, ObjectIdentifier durableID, String durableCategory);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.DurableStatusSelectionInqResult>
     * @author Ho
     * @date 2018/9/28 10:38:28
     */
    Results.DurableStatusSelectionInqResult durableFillInTxPDQ011DR(Infos.ObjCommon objCommon, ObjectIdentifier durableID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/29 16:00
     * @param objCommon
     * @param durableCategory
     * @param durableID -
     * @return java.util.List<com.fa.cim.dto.Infos.DurableHoldListAttributes>
     */
    List<Infos.DurableHoldListAttributes> durableFillInODRBQ019(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableCategory
     * @param durableID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.DurableSubStateGetOut>
     * @author Ho
     * @date 2018/9/28 17:18:31
     */
    Infos.DurableSubStateGetOut durableSubStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableCategory
     * @param durableID
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/9/28 16:03:18
     */
    void durableOnRouteCheck(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableCategory
     * @param durableID
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Ho
     * @date 2018/9/28 16:50:47
     */
    String durableInventoryStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * durable_CassetteCategory_CheckForContaminationControl
     * @author ho
     * @return
     */
    void durableCassetteCategoryCheckForContaminationControl(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, ObjectIdentifier equipmentID, ObjectIdentifier portID);

    /**
     * description:Check durable's condition for Durables Management Operation.
     * durable_CheckConditionForOperation.cpp
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/8/18 12:21
     * @param objCommon
     * @param in -
     * @return com.fa.cim.dto.RetCode
     */
    void durableCheckConditionForOperation(Infos.ObjCommon objCommon, Inputs.ObjDurableCheckConditionForOperationIn in);

    void durableCheckConditionForOperationForInternalBuffer(Infos.ObjCommon objCommon, Inputs.ObjDurableCheckConditionForOperationIn in);

    /**
     * description:Get durable’s logical recipe and pmcmg recipe for specified eqp.
     * durable_recipe_Get
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/8/18 15:11
     * @param objCommon
     * @param durableCategory
     * @param durableID
     * @param equipmentID -
     * @return com.fa.cim.pojo.Outputs.ObjDurableRecipeGetOut
     */
    Outputs.ObjDurableRecipeGetOut durableRecipeGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, ObjectIdentifier equipmentID);

    /**
     * description:Check durable's status for Durables Management Operation.
     * durable_status_CheckForOperation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/8/18 16:03
     * @param objCommon
     * @param operation
     * @param durableID
     * @param durableCategory -
     * @return com.fa.cim.dto.RetCode
     */
    void durableStatusCheckForOperation(Infos.ObjCommon objCommon, String operation, ObjectIdentifier durableID, String durableCategory);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param category
     * @return RetCode<List<ObjectIdentifier>>
     * @author Sun
     * @date 2018/10/16
     */
    List<ObjectIdentifier> durableCapabilityIDGetDR(Infos.ObjCommon objCommon, String category);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param category
     * @return RetCode<List<ObjectIdentifier>>
     * @author Sun
     * @date 2018/10/16
     */
    List<ObjectIdentifier> durableIDGetDR(Infos.ObjCommon objCommon, String category);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param className className
     * @param registAttribute registAttribute
     * @return Object
     * @author Paladin
     * @date 2018/11/11
     */
    void durableCheckForUpdate(Infos.ObjCommon objCommon, String className, Infos.DurableAttribute registAttribute);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param className className
     * @param registAttribute registAttribute
     * @return Object
     * @author Paladin
     * @date 2018/11/11
     */
    void durableSettingCheck(Infos.ObjCommon objCommon, String className, Infos.DurableAttribute registAttribute);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param updateFlag updateFlag
     * @param className className
     * @param registAttributeWithUdata registAttribute
     * @return registAttributeWithUdata
     * @author Paladin
     * @date 2018/11/21
     */
    void durableRegist(Infos.ObjCommon objCommon, boolean updateFlag, String className, Infos.DurableAttribute registAttributeWithUdata);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/11/27 14:28
     * @param objCommon
     * @param params -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.DurableSubStatusSelectionInqResult>
     */
    List<Infos.CandidateDurableSubStatusDetail> durableFillInTxPDQ034DR(Infos.ObjCommon objCommon, Params.DurableSubStatusSelectionInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/2/18 16:59
     * @param objCommon
     * @param in -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void durableCurrentStateCheckTransition(Infos.ObjCommon objCommon, Inputs.ObjDurableCurrentStateCheckTransitionIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/2/18 17:04
     * @param objCommon
     * @param in -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void durableCurrentStateChange(Infos.ObjCommon objCommon, Inputs.ObjDurableCurrentStateChangeIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/4 15:38
     * @param objCommon
     * @param durableCategory
     * @param durableID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjDurableHoldStateGetOut>
     */
    String durableHoldStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/4 22:16
     * @param objCommon
     * @param durableCategory
     * @param durableID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjDurableProcessStateGetOut>
     */
    String durableProcessStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/4 22:48
     * @param objCommon
     * @param durableCategory
     * @param durableID -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void durableCheckEndBankIn(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/4 23:03
     * @param objCommon
     * @param durableCategory
     * @param durableID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjDurableCurrentOperationInfoGetOut>
     */
    Outputs.ObjDurableCurrentOperationInfoGetOut durableCurrentOperationInfoGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/7/3 15:55
     * @param objCommon
     * @param durableControlJobID
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.StartDurable>>
     */
    List<Infos.StartDurable> durableControlJobDurableListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID);

    Infos.DurableSubStatusInfo durableSubStateDBInfoGetDR(Infos.ObjCommon objCommon, Inputs.ObjDurableSubStateDBInfoGetDRIn objDurableSubStateDBInfoGetDRIn);

    void durableCheckForDeletion(Infos.ObjCommon objCommon, String className, ObjectIdentifier durableID);

    Infos.DurableAttribute durableDelete(Infos.ObjCommon objCommon, String className, ObjectIdentifier durableID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strDurableOperationHistoryFillInODRBQ020DRIn
     * @return java.util.List<com.fa.cim.dto.Infos.DurableOperationHisInfo>
     * @exception
     * @author ho
     * @date 2020/6/22 13:53
     */
     List<Infos.DurableOperationHisInfo> durableOperationHistoryFillInODRBQ020DR(
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.DurableOperationHistoryFillInODRBQ020DRIn strDurableOperationHistoryFillInODRBQ020DRIn );

     /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
       * @param strObjCommonIn
      * @param strDurable_opeHisRParm_GetDR_in
      * @return java.util.List<com.fa.cim.dto.Infos.OpeHisRecipeParmInfo>
      * @exception
      * @author ho
      * @date 2020/6/22 15:08
      */
     List<Infos.OpeHisRecipeParmInfo> durableOpeHisRParmGetDR(
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.DurableOpeHisRParmGetDRIn strDurable_opeHisRParm_GetDR_in );
    /**
     * Change processState of durable to Processing
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/22 10:26
     */
    void durableProcessStateMakeProcessing(Infos.ObjCommon objCommon, String durableCategory, List<Infos.StartDurable> startDurables);

    /**
     * Create Durable PFX for Durables Management Operation.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/23 13:05
     */
    void durablePFXCreate(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, ObjectIdentifier routeID);

    /**
     * Delete Durable PFX for Durables Management Operation.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/24 13:36
     */
    void durablePFXDelete(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

     /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
       * @param strObjCommonIn
      * @param strDurableControlJobstartReserveInformationGetin
      * @return com.fa.cim.dto.Infos.DurableControlJobStartReserveInformationGetOut
      * @exception
      * @author ho
      * @date 2020/6/23 16:53
      */
    Infos.DurableControlJobStartReserveInformationGetOut durableControlJobStartReserveInformationGet(
            Infos.ObjCommon                                      strObjCommonIn,
            Infos.DurableControlJobStartReserveInformationGetIn strDurableControlJobstartReserveInformationGetin);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strDurableprocessStateMakeWaitingin
     * @return void
     * @exception
     * @author ho
     * @date 2020/6/24 14:36
     */
    void durableProcessStateMakeWaiting(
            Infos.ObjCommon                            strObjCommonIn,
            Infos.DurableProcessStateMakeWaitingIn    strDurableprocessStateMakeWaitingin );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strDurable_CheckConditionForDurablePO_in
     * @return boolean
     * @exception
     * @author ho
     * @date 2020/6/24 16:19
     */
    boolean durableCheckConditionForDurablePO (
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.DurableCheckConditionForDurablePOIn strDurable_CheckConditionForDurablePO_in );

    /**
     * Check durables' condition for DurablePO.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/24 16:49
     */
    boolean durableCheckConditionForDurablePO(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableId);

    /**
     * durable hold
     *
     * @version 1.0
     * @author Miner
     * @date 2020/6/30 16:49
     */
    List<Infos.DurableHoldHistory> durableHold(Infos.ObjCommon objCommon, Infos.DurableHoldIn durableHoldIn);


    /**
     * Set Durable’s inPostProcessFlag.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/30 13:40
     */
    void durableInPostProcessFlagSet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, boolean inPostProcessFlag);

    /**
     * Get Durables' State.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/30 15:33
     */
    String durableStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/6 17:15                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/7/6 17:15
     * @param objCommon
     * @param durableCategory
     * @param durableID
     * @param durableHoldLists
     * @param releaseReasonCodeID -
     * @return java.util.List<com.fa.cim.dto.Infos.DurableHoldHistory>
     */
    List<Infos.DurableHoldHistory> durableHoldRelease(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, List<Infos.DurableHoldList> durableHoldLists, ObjectIdentifier releaseReasonCodeID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strDurableCheckConditionForAutoBankInin
     * @return boolean
     * @exception
     * @author ho
     * @date 2020/7/8 15:37
     */
    boolean durableCheckConditionForAutoBankIn(
            Infos.ObjCommon                            strObjCommonIn,
            Infos.DurableCheckConditionForAutoBankInIn strDurableCheckConditionForAutoBankInin);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/8 17:57                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/7/8 17:57
     * @param objCommon
     * @param durableCategory
     * @param durableID -
     * @return void
     */
    void processCheckBranchCancelForDurable(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/8 17:58                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/7/8 17:58
     * @param objCommon
     * @param durableCategory
     * @param durableID -
     * @return void
     */
    void processDurableReworkCountDecrement(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/8 17:59                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/7/8 17:59
     * @param objCommon
     * @param durableCategory
     * @param durableID -
     * @return com.fa.cim.dto.Inputs.OldCurrentPOData
     */
    Inputs.OldCurrentPOData processCancelBranchRouteForDurable(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/8 18:05                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/7/8 18:05
     * @param objCommon
     * @param durableCategory
     * @param durableID -
     * @return java.lang.String
     */
    String durableProductionStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strDurableoriginalRouteListGetin
     * @return com.fa.cim.dto.Infos.DurableOriginalRouteListGetOut
     * @exception
     * @author ho
     * @date 2020/7/13 9:46
     */
     Infos.DurableOriginalRouteListGetOut durableOriginalRouteListGet(
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.DurableOriginalRouteListGetIn strDurableoriginalRouteListGetin );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strDurablerouteIDGetin
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @exception
     * @author ho
     * @date 2020/7/13 14:11
     */
     ObjectIdentifier durableRouteIDGet(
            Infos.ObjCommon                    strObjCommonIn,
            Infos.DurableRouteIDGetIn strDurablerouteIDGetin );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strDurableCheckLockHoldConditionForOperationin
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/17 15:55
     */
     void durableCheckLockHoldConditionForOperation(
            Infos.ObjCommon                                   strObjCommonIn,
            Infos.DurableCheckLockHoldConditionForOperationIn strDurableCheckLockHoldConditionForOperationin );


     String durableDurableCategoryGet(Infos.ObjCommon objCommon, ObjectIdentifier durableID);

    Results.CandidateDurableJobStatusDetail durableCandidateJobStatusGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID);

    void durableJobStateCheck(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, String jobStatus);

    void durableJobStateChange(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, String jobStatus);

    void durableCheckConditionForMachineAndChamber(Infos.ObjCommon objCommon, String equipmentID, String chamberID);

    Outputs.ObjMultiDurableXferFillInOTMSW005InParmOut multiDurableXferFillInOTMSW005InParm(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, List<Infos.StartDurable> strStartDurables,String durableCategory);

    Outputs.ObjDurableDeliveryRTDInterfaceReqOut durableDeliveryRTDInterfaceReq(Infos.ObjCommon objCommonIn, String kind, ObjectIdentifier keyID);

    Outputs.ObjSingleDurableXferFillInOTMSW006InParmOut singleDurableXferFillInOTMSW006InParm(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier unLoadPortID, ObjectIdentifier durableID, Results.DurableWhereNextStockerInqResult strWhereNextStockerInqResult, String durableCategory);

    Results.DurableWhereNextStockerInqResult durableDestinationInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier durableID);

    Outputs.ObjLotEquipmentOrderGetByLotStatusOut durableEquipmentOrderGetByDurableStatus(Infos.ObjCommon objCommon, Results.CarrierDetailInfoInqResult carrierDeatilInfo,String durableCategory);

    List<Infos.WhereNextEqpStatus> durableEquipmentStockerOrderGetByDurableStatus(Infos.ObjCommon objCommon, Results.CarrierDetailInfoInqResult carrierDeatilInfo, List<Infos.WhereNextEqpStatus> priorEqpList);

    List<ObjectIdentifier> durableQueuedMachinesGetByOperationOrder(Infos.ObjCommon objCommon, List<ObjectIdentifier> eqpIDs, ObjectIdentifier operationID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strDurableOnRouteStateGetin
     * @return java.lang.String
     * @exception
     * @author ho
     * @date 2020/9/23 15:24
     */
    String durableOnRouteStateGet (
            Infos.ObjCommon                   strObjCommonIn,
            Infos.DurableOnRouteStateGetIn   strDurableOnRouteStateGetin);

    /**
     * description:erack所需查询接口
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/19 23:11                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/10/19 23:11
     * @param objCommon
     * @param stockerID -
     * @return java.util.List<com.fa.cim.dto.Infos.PodInErack>
     */
    List<Infos.PodInErack> podInErack(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    /**
     * description: postProcessQueue_ForceDeleteDR
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2021/3/11 0011 15:29
     * @param objCommon - common
     * @param strLotCassettePostProcessForceDeleteReqInParams - params
     *
     */
    void postProcessQueueForceDeleteDR(Infos.ObjCommon objCommon, Params.StrLotCassettePostProcessForceDeleteReqInParams strLotCassettePostProcessForceDeleteReqInParams);

    /**
     * description: durableFillInTxPDQ025
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                        - common
     * @param strDurableFillInTxPDQ025InParams - params
     * @return re
     * @author YJ
     * @date 2021/3/11 0011 19:44
     */
    Results.DurableHoldListAttributesResult durableFillInTxPDQ025(Infos.ObjCommon objCommon, Params.StrDurableFillInTxPDQ025InParams strDurableFillInTxPDQ025InParams);
}