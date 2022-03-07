package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.*;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.rtms.ReticleUpdateParamsInfo;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.util.List;

/**
 * description:
 * <p><br/></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2018/10/17 10:48:23
 */
public interface IReticleMethod {

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param reticleListInqParams
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.ReticleListInqResult>
     * @author Ho
     * @date 2018/10/17 10:54:39
     */
    Results.ReticleListInqResult reticleListGetDR(Infos.ObjCommon objCommon, Params.ReticleListInqParams reticleListInqParams);

    /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/26 10:22                     Aoki                Create
    *
    * @author Aoki
    * @date 2021/3/26 10:22
    * @param objCommon
 * @param reticleListInqParams
    * @return com.fa.cim.dto.Results.ReticleListInqResult
    */
    Results.PageReticleListInqResult reticleListGetDRForPage(Infos.ObjCommon objCommon, Params.ReticleListInqParams reticleListInqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param reticleID
     * @param reticlePodID
     * @param toEquipmentID
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.ReticleDispatchJob>>
     * @author Ho
     * @date 2018/10/31 17:49:10
     */
    List<Infos.ReticleDispatchJob> reticleDispatchJobCheckExistenceDR(Infos.ObjCommon strObjCommonIn, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID, ObjectIdentifier toEquipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param stockerID
     * @param equipmentID
     * @param strXferReticle
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.ReticleChangeTransportState>
     * @author Ho
     * @date 2018/10/29 14:07:00
     */
    Infos.ReticleChangeTransportState reticleChangeTransportState(Infos.ObjCommon strObjCommonIn, ObjectIdentifier stockerID, ObjectIdentifier equipmentID, List<Infos.XferReticle> strXferReticle);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param reticleID
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/10/26 16:01:06
     */
    void reticleUsageInfoReset(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param reticleID
     * @param durableOperationInfoFlag
     * @param durableWipOperationInfoFlag
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.ReticleDetailInfoInqResult>
     * @author Ho
     * @date 2018/10/22 13:23:11
     */
    Results.ReticleDetailInfoInqResult reticleDetailInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Boolean durableOperationInfoFlag, Boolean durableWipOperationInfoFlag);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param reticleID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.ReticleAdditionalAttribute>
     * @author Ho
     * @date 2018/10/25 11:20:43
     */
    Infos.ReticleAdditionalAttribute reticleReservationInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/19 12:23
     * @param objCommon
     * @param equipmentID
     * @param startReticleList
     * @param lotID -
     * @return void
     */
    void reticleStateCheck170(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartReticleInfo> startReticleList, ObjectIdentifier lotID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param reticleID
     * @param reticleStatus
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/10/22 16:38:20
     */
    void reticleStateChange(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String reticleStatus);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param reticleID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.ReticleStatusChangeRptResult>
     * @author Ho
     * @date 2018/10/22 16:43:13
     */
    Results.ReticleStatusChangeRptResult reticleFillInTxPDR005(Infos.ObjCommon strObjCommonIn, ObjectIdentifier reticleID);

    /**
     * description:
     * <p>Set TimeStamp and User.<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon    objCommon
     * @param reticlePodID reticlePodID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/28 21:46:27
     */
    void reticlePodTimeStampSet(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);

    /**
     * description:
     * <p>This function gets the Reticle Pod base information.<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon    objCommon
     * @param reticlePodID reticlePodID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/31 13:51:32
     */
    Infos.DurableAttribute reticlePodBaseInfoGetDr(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon    objCommon
     * @param reticlePodID reticlePodID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/31 14:39:18
     */
    List<Infos.UserData> reticlePodUserDataInfoGetDr(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/6                          Wind
     *
     * @param objCommon
     * @param params
     * @return RetCode<List<Infos.ReticlePodListInfo>>
     * @author Wind
     * @date 2018/11/6 13:34
     */
    List<Infos.ReticlePodListInfo> reticlePodFillInTxPDQ012DR(Infos.ObjCommon objCommon, Params.ReticlePodListInqParams params);

    /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/26 11:05                     Aoki                Create
    *
    * @author Aoki
    * @date 2021/3/26 11:05
    * @param objCommon
     * @param params
    * @return java.util.List<com.fa.cim.dto.Infos.ReticlePodListInfo>
    */
    Page<Infos.ReticlePodListInfo> pageReticlePodList(Infos.ObjCommon objCommon, Params.ReticlePodListInqParams params);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/7                            Wind
      * @param objCommon
      * @param params
      * @return RetCode<Outputs.ObjReticlePodFillInTxPDQ013DROut>
      * @author Wind
      * @date 2018/11/7 18:08
      */
    Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DR(Infos.ObjCommon objCommon, Params.ReticlePodDetailInfoInqParams params);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/7                            Wind
      * @param objCommon
      * @param reticlePodID
      * @return RetCode<Infos.ReticlePodAdditionalAttribute>
      * @author Wind
      * @date 2018/11/7 18:18
      */
    Infos.ReticlePodAdditionalAttribute reticlePodReservedReticleInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/8                          Wind
      * @param objCommon
      * @param reticlePodID
      * @return RetCode<Outputs.ObjReticlePodReticleListGetDROut>
      * @author Wind
      * @date 2018/11/8 19:47
      */
    Outputs.ObjReticlePodReticleListGetDROut reticlePodReticleListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);


    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/9                          Wind
      * @param objCommon
      * @param stockerID
      * @return RetCode<Object>
      * @author Wind
      * @date 2018/11/9 16:04
      */
    void objectLock(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/9                          Wind
      * @param objCommon
      * @param strObjectLockModeGetIn
      * @return RetCode<Outputs.ObjectLockModeGetOut>
      * @author Wind
      * @date 2018/11/9 16:21
      */
    Outputs.ObjectLockModeGetOut objectLockModeGet(Infos.ObjCommon objCommon, Inputs.ObjectLockModeGetIn strObjectLockModeGetIn);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/9                          Wind
      * @param objCommon
      * @param stockerID
      * @param equipmentID
      * @param reticlePodID
      * @param transferStatusChangeTimeStamp
      * @param claimMemo
      * @return RetCode<Object>
      * @author Wind
      * @date 2018/11/9 16:35
      */
    void reticlePodTransferStateChange(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier equipmentID, ObjectIdentifier reticlePodID, String transferStatus, String transferStatusChangeTimeStamp, String claimMemo, Infos.ShelfPosition shelfPosition);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/6/25 10:32
     * @param objCommon
     * @param strReticleSortInfo
     * @param bAddFlag
     * @param bRemoveFlag -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void reticleMaterialContainerChange(Infos.ObjCommon objCommon, Infos.ReticleSortInfo strReticleSortInfo, boolean bAddFlag, boolean bRemoveFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/6/25 11:15
     * @param objCommon
     * @param fromReticlePodID
     * @param toReticlePodID
     * @param fromReticlePodFlag
     * @param toReticlePodFlag -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void reticlePodTransferStateCheck(Infos.ObjCommon objCommon, ObjectIdentifier fromReticlePodID, ObjectIdentifier toReticlePodID, boolean fromReticlePodFlag, boolean toReticlePodFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/5 9:11
     * @param objCommon
     * @param equipmentID
     * @param reticlePodPortID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut>
     */
    Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut reticlePodPortResourceCurrentAccessModeGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier reticlePodPortID);

    /**
     * description:
     * <p>reticlePodWhereConditionMake</p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2019/10/30/030 10:13
     */
    String reticlePodWhereConditionMake(Infos.ObjCommon objCommon, String conditionString);

    /**
     * description:
     * <p>reticlePodWildCardStringCheck</p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2019/10/30/030 15:53
     */
    Outputs.ObjReticlePodWildCardStringCheckOut reticlePodWildCardStringCheck(Infos.ObjCommon objCommon, String conditionString, String dataString);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param reticleID
     * @author PlayBoy
     * @date 2018/7/30
     */
    void reticleUsageCountIncrement(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param reticleID reticleID
     * @return RetCode<Object>
     * @author PlayBoy
     * @date 2018/8/8
     */
    void reticleUsageCountDecrement(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param reticleId reticleId
     * @return Outputs.ObjReticleUsageLimitationCheckOut
     * @author Paladin
     * @date 2018/8/14
     */
    Outputs.ObjReticleUsageLimitationCheckOut reticleUsageLimitationCheck(Infos.ObjCommon objCommon, ObjectIdentifier reticleId);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param reticleId reticleId
     * @param lastUsedTime lastUsedTime
     * @return RetCode
     * @author Paladin
     * @date 2018/8/15
     */
    void reticleLastUsedTimeSet(ObjectIdentifier reticleId, Timestamp lastUsedTime);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param startCassettes startCassettes
     * @return RetCode
     * @author Paladin
     * @date 2018/8/26
     */
    void reticleLastUsedTimeStampUpdate(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassettes);


    /**
     * description:
     * <p>Change Reticle Pod and Reticle Status.<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon        objCommon
     * @param reticlePodStatus reticlePodStatus
     * @param reticlePodID     reticlePodID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/29 13:00:31
     */
    void reticlePodStatusChange(Infos.ObjCommon objCommon, String reticlePodStatus, ObjectIdentifier reticlePodID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param moveDirection moveDirection
     * @param reticlePodID reticlePodID
     * @param moveReticles moveReticles
     * @return Object
     * @author Paladin
     * @date 2018/8/17
     */
    void justInOutReticleTransferInfoVerify(Infos.ObjCommon objCommon, String moveDirection, ObjectIdentifier reticlePodID, List<Infos.MoveReticles> moveReticles);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param reticlePodID reticlePodID
     * @param reticleID reticleID
     * @param slotNumber slotNumber
     * @return Object
     * @author Paladin
     * @date 2018/10/30
     */
    void reticleMaterialContainerJustIn(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, ObjectIdentifier reticleID, int slotNumber);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param reticlePodID reticlePodID
     * @param reticleID reticleID
     * @param slotNumber slotNumber
     * @return Object
     * @author Paladin
     * @date 2018/10/30
     */
    void reticleMaterialContainerJustOut(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, ObjectIdentifier reticleID, int slotNumber);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param reticlePodID
     * @return com.fa.cim.dto.Infos.ReticlePodCurrentMachineGetOut
     * @exception
     * @author ho
     * @date 2020/3/19 13:54
     */
    public Infos.ReticlePodCurrentMachineGetOut reticlePodCurrentMachineGet(Infos.ObjCommon strObjCommonIn, ObjectIdentifier reticlePodID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param stockerID
     * @param strInventoryReticlePodInfo
     * @return java.util.List<com.fa.cim.dto.Infos.InventoriedReticlePodInfo>
     * @exception
     * @author ho
     * @date 2020/3/20 10:40
     */
    public List<Results.InventoriedReticlePodInfo> reticlePodPositionUpdateByStockerInventoryDR(
            Infos.ObjCommon                                       strObjCommonIn,
            ObjectIdentifier                                     stockerID,
            List<Infos.InventoryReticlePodInfo>                   strInventoryReticlePodInfo,
            Infos.ShelfPosition shelfPosition);


    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/4 10:40
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.ReticleDispatchJob> reticleDispatchJobListGetDR(Infos.ObjCommon objCommon, String reticleDispatchJobID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/4 11:16
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.ReticleComponentJob> reticleComponentJobListGetDR(Infos.ObjCommon objCommon, String reticleDispatchJobID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/4 14:34
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ReticleDispatchJobCreateOut reticleDispatchJobCreate(Infos.ObjCommon objCommon, String reticleDispatchJobID, ObjectIdentifier rDJRequestUserID, Long priority, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID, ObjectIdentifier toMachineID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/4 14:39
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ReticleAvailabilityCheckForXferOut reticleAvailabilityCheckForXfer(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/5 10:23
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.ReticleComponentJob> reticleComponentJobCheckExistenceDR(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID, ObjectIdentifier fromMachineID, ObjectIdentifier fromPortID, ObjectIdentifier toMachineID, ObjectIdentifier toPortID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/5 15:14
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ReticlePodAvailabilityCheckForXferOut reticlePodAvailabilityCheckForXfer(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, String reticleDispatchJobID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/6 13:30
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticleDispatchJobInsertDR(Infos.ObjCommon objCommon, List<Infos.ReticleDispatchJob> strReticleDispatchJobList);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/6 14:06
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticleReticlePodReserve(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID, ObjectIdentifier toMachineID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/6 14:57
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticleEventQueuePutDR(Infos.ObjCommon objCommon, Infos.ReticleEventRecord strReticleEventRecord);

    void reticleReservationCheck(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    void reticlePodVacantSlotPositionCheck(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, List<Infos.MoveReticles> moveReticlesList);

    void reticleReticlePodReserveCancel(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Boolean bCheckToMachineFlag);

    /**
     * description:Get Previous transferStatus.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/5 15:16                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/5 15:16
     * @param objCommon
     * @param reticlePodID -
     * @return java.lang.String
     */
    String reticlePodTransferStateGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6 16:05                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/6 16:05
     * @param objCommon
     * @param reticlePodID -
     * @return void
     */
    void reticlePodAvailabilityCheck(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6 17:56                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/6 17:56
     * @param objCommon
     * @param reticleDispatchJobID
     * @param reticleComponentJobID
     * @param jobSuccessFlag -
     * @return void
     */
    void reticleJobStatusUpdateByRequestDR(Infos.ObjCommon objCommon, String reticleDispatchJobID, String reticleComponentJobID, boolean jobSuccessFlag);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/9 10:03
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticleDispatchJobUpdateDR(Infos.ObjCommon objCommon, Infos.ReticleDispatchJob strReticleDispatchJob);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/9 12:43
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.ReticleDispatchJob> reticleDispatchJobListGetForUpdateDR(Infos.ObjCommon objCommon, String reticleDispatchJobID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/9 13:06
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ReticleReticlePodGetForXferOut reticleReticlePodGetForXfer(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/9 13:16
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.CandidateReticlePod> reticlePodCandidateInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, Boolean reservedCheckFlag);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/9 16:08
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ReticleComponentJobCreateOut reticleComponentJobCreate(Infos.ObjCommon objCommon, String reticleDispatchJobID, ObjectIdentifier RDJRequestUserID, Long priority, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID, ObjectIdentifier toMachineID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/9 17:06
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<ObjectIdentifier> reticlePodPortAvailabilityCheckForReticlePodXfer(Infos.ObjCommon objCommon, ObjectIdentifier machineID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/11 11:07
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticleComponentJobInsertDR(Infos.ObjCommon objCommon, List<Infos.ReticleComponentJob> strReticleComponentJobList);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/11 14:14
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticleDispatchJobDeleteDR(Infos.ObjCommon objCommon, String reticleDispatchJobID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/11 14:20
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticleComponentJobDeleteDR(Infos.ObjCommon objCommon, String reticleDispatchJobID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/11 15:11
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticleComponentJobUpdateDR(Infos.ObjCommon objCommon, Infos.ReticleComponentJob strReticleComponentJob);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/11 17:25
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticlePodTransferReserveCancel(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/12                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/12 14:43
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticleDispatchJobDeleteByStatusDR(Infos.ObjCommon objCommon, String reticleDispatchJobID);

    Outputs.ReticleReservationDetailInfoGetOut reticleReservationDetailInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    void reticleStoreTimeSet(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String toString);

    Outputs.ReticleControlJobInfoGetOut reticlecontrolJobInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/12                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/12 17:10
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ReticleArhsJobCreateOut reticleArhsJobCreate(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Integer slotNumber, ObjectIdentifier reticlePodID, ObjectIdentifier toMachineID, ObjectIdentifier toPortID, ObjectIdentifier fromMachineID, ObjectIdentifier fromPortID, String jobName);

    /**
     * description:Find RCJ with jobName and (reticleID and/or reticlePodID) this is dynamic sql function.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/12 17:54                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/12 17:54
     * @param objCommon
     * @param jobName
     * @param reticleID
     * @param reticlePodID -
     * @return com.fa.cim.dto.Outputs.ReticleComponentJobGetByJobNameDROut
     */
    Outputs.ReticleComponentJobGetByJobNameDROut reticleComponentJobGetByJobNameDR(Infos.ObjCommon objCommon, String jobName, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID);

    /**
     * description:this is direct sql function.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/12 18:18                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/12 18:18
     * @param objCommon
     * @param reticleComponentJobID
     * @param jobSuccessFlag -
     * @return void
     */
    void reticleJobStatusUpdateByReportDR(Infos.ObjCommon objCommon, String reticleComponentJobID, boolean jobSuccessFlag);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/16                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/16 14:05
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticlePodTransferReserve(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, ObjectIdentifier reticlePodID);
    
    /**
     * description:This method gets reticlePod transfer status.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/17 14:21                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/17 14:21
     * @param objCommon
     * @param reticlePodID -
     * @return java.lang.String
     */
    String reticlePodTransferStateGet(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/21                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/21 13:15
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void reticlePodTransferReservationCheck(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);

    void reticleStateCheckForAction(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String action);

    void reticleInspectionRequest(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String inspectionType);

    void reticleInspectionIn(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    String reticleInspectionOut(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, boolean operationFlag,String claimMemo);

    boolean reticleRequestRepair(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    void reticleRepairIn(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    void reticleLocationTrxUpdate(Infos.ObjCommon objCommon, CimProcessDurable processDurableBO, String location);

    void reticleRepairOut(Infos.ObjCommon objCommon, ObjectIdentifier reticleID,String claimMemo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * {@link Infos.ObjCommon}
     * {@link ObjectIdentifier}
     * @author salt
     * @date 2021/1/4 10:04
     * @param objCommon -
     * @param reticleID -
     * @return
     */
    void confirmMaskQuality(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    /**
     * description:  update hold or HoldRelease State
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/4 11:02
     *
     * {@link Infos.ObjCommon}
     * {@link ObjectIdentifier}
     * @param objCommon -
     * @param reticleID -
     * @param action -
     * @return
     */
    void changeRecticleState(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String action);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/4 11:02
     *
     * {@link Params.ReticleHoldReqParams}
     * {@link Infos.ObjCommon}
     * {@link ObjectIdentifier}
     * @param objCommon -
     * @param reticleID -
     * @param params -
     * @return
     */
    void reticleHold(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleHoldReqParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/4 11:01
     *
     * {@link Params.ReticleHoldReleaseReqParams}
     * {@link Infos.ObjCommon}
     * {@link ObjectIdentifier}
     * @param objCommon -
     * @param reticleID -
     * @param params -
     * @return
     */
    void reticleHoldRelease(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleHoldReleaseReqParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/4 14:59
     * {@link Infos.ObjCommon}
     * {@link ObjectIdentifier}
     * @param objCommon -
     * @param reticleID -
     * @return
     */
    List<Infos.ReticleHoldListAttributes> findHoldReticleListInq(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    /*      get the fabID,department, DOC Category, User ID for RTMS to update the reticle
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/8/20 14:52                      Decade                Create
    *
    * @author Decade
    * @date 2021/8/20 14:52
    * @param null -
    * @return
    */
    ReticleUpdateParamsInfo reticleUpdateParamsGet(Infos.ObjCommon objCommon);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/4 14:59
     * {@link Infos.ObjCommon}
     * {@link ObjectIdentifier}
     * {@link Params.ReticleTerminateReqParams}
     * @param objCommon -
     * @param reticleID -
     * @param params -
     * @return
     */
    void reticleTerminate(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleTerminateReqParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/4 14:59
     * {@link Infos.ObjCommon}
     * {@link ObjectIdentifier}
     * {@link Params.ReticleTerminateReqParams}
     * @param objCommon -
     * @param reticleID -
     * @param params -
     * @return
     */
    void reticleTerminateCancel(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleTerminateReqParams params);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/6 10:00
     * {@link Infos.ObjCommon}
     * {@link ObjectIdentifier}
     * {@link Params.ReticleScrapReqParams}
     * @param objCommon -
     * @param reticleID -
     * @param params -
     * @return
     */
    void reticleScrap(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleScrapReqParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date  2021/1/6 10:00
     * {@link Infos.ObjCommon}
     * {@link ObjectIdentifier}
     * {@link Params.ReticleScrapReqParams}
     * @param objCommon -
     * @param reticleID -
     * @param params -
     * @return
     */
    void reticleScrapCancel(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleScrapReqParams params);

    /*
    * description:  reticle scan request
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/8/18 17:17                      Decade                Create
    *
    * @author Decade
    * @date 2021/8/18 17:17
    * @param null -
    * @return
    */
    void reticleScanRequest(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    /*       scan complete success,release all the hold
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/8/19 10:59                      Decade                Create
    *
    * @author Decade
    * @date 2021/8/19 10:59
    * @param null -
    * @return
    */
    void reticleScanCompleteSuccess(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, ObjectIdentifier releaseCodeId);

    /*       scan complete fail,hold all the reticle
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/8/19 14:30                      Decade                Create
    *
    * @author Decade
    * @date 2021/8/19 14:30
    * @param null -
    * @return
    */
    void reticleScanCompleteFail(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Infos.ReticleReasonReq reticleHoldReason);

    /*       change the reticle inspection type
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/8/19 14:31                      Decade                Create
    *
    * @author Decade
    * @date 2021/8/19 14:31
    * @param null -
    * @return
    */
    void reticleInspectionTypeChange(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String inspectionType);

    Infos.ReticleHistoryGetDROut reticleHistoryGetDR(Infos.ObjCommon objCommon, Infos.ReticleHistoryGetDRIn reticleHistoryGetDRIn);

    void reticleEventInfoSet(Inputs.ReticleOperationEventMakeParams params , ObjectIdentifier reticleID);

    String timeStampGetDR();

    /*
    * description:  check reticle usage by RTMS
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/9/2 18:07                      Decade                Create
    *
    * @author Decade
    * @date 2021/9/2 18:07
    * @param null -
    * @return
    */
    void reticleUsageCheckByRTMS(Infos.ObjCommon objCommon, String action, List<String> reticlelist, ObjectIdentifier lotID, ObjectIdentifier eqpID);

    /*       report to RTMS when move in cancel/reserve  cancel/ move out
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/9/3 11:13                      Decade                Create
    *
    * @author Decade
    * @date 2021/9/3 11:13
    * @param null -
    * @return
    */
    void reticleOpeCancelCompleteRptRTMS(Infos.ObjCommon objCommon, String action, ObjectIdentifier lotID);
}
