package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.lot.TerminateReq;
import com.fa.cim.layoutrecipe.LayoutRecipeParams;
import com.fa.cim.lmg.LotMonitorGroupParams;
import com.fa.cim.newcore.dto.event.Event;

import java.util.List;

/**
 * description:
 * This file use to define the IEventMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/5/29        ********             lightyh            create file
 *
 * @author: lightyh
 * @date: 2019/5/29 15:39
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEventMethod {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/5/30 10:41
     * @param objCommon
     * @param lotReworkEventMakeParams -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotReworkEventMake(Infos.ObjCommon objCommon, Inputs.LotReworkEventMakeParams lotReworkEventMakeParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/5/30 13:38
     * @param objCommon
     * @param newLotAttributes
     * @param transactionID
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotWaferMoveEventMake(Infos.ObjCommon objCommon, Infos.NewLotAttributes newLotAttributes, String transactionID, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/5/31 14:17
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotReticleSetChangeEventMake(Infos.ObjCommon objCommon, Inputs.LotReticleSetChangeEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/5/31 15:07
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotOperationMoveEventMakeOther(Infos.ObjCommon objCommon, Inputs.LotOperationMoveEventMakeOtherParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/14 16:44
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotOperationMoveEventMakeOpeComp(Infos.ObjCommon objCommon, Inputs.LotOperationMoveEventMakeOpeComp params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/8/5 10:23
     * @param objCommon
     * @param transactionID
     * @param lotID
     * @param claimMemo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotOperationMoveEventMakeGatePass(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier lotID, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/3 9:27
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void eqpMonitorWaferUsedCountUpdateEventMake(Infos.ObjCommon objCommon, Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/3 13:05
     * @param objCommon
     * @param lotHoldEventMakeParams -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotHoldEventMake(Infos.ObjCommon objCommon, Inputs.LotHoldEventMakeParams lotHoldEventMakeParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/3 14:45
     * @param objCommon
     * @param lotPartialReworkCancelEventMakeParams -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotPartialReworkCancelEventMake(Infos.ObjCommon objCommon, Inputs.LotPartialReworkCancelEventMakeParams lotPartialReworkCancelEventMakeParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/4 16:21
     * @param objCommon
     * @param lotFutureHoldEventMakeParams -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotFutureHoldEventMake(Infos.ObjCommon objCommon, Inputs.LotFutureHoldEventMakeParams lotFutureHoldEventMakeParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/4 17:28
     * @param objCommon
     * @param lotBankMoveEventMakeParams -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotBankMoveEventMake(Infos.ObjCommon objCommon, Inputs.LotBankMoveEventMakeParams lotBankMoveEventMakeParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/4 18:03
     * @param objCommon
     * @param lotOperationMoveEventMakeBranchParams -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotOperationMoveEventMakeBranch(Infos.ObjCommon objCommon, Inputs.LotOperationMoveEventMakeBranchParams lotOperationMoveEventMakeBranchParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/14 14:18
     * @param objCommon
     * @param transactionID
     * @param lotID
     * @param oldCurrentPOData
     * @param claimMemo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotOperationMoveEventMakeChangeRoute(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier lotID, Inputs.OldCurrentPOData oldCurrentPOData, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/6 9:28
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void processResourceWaferPositionEventMake(Infos.ObjCommon objCommon, Inputs.ProcessResourceWaferPositionEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/6 11:10
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotOperationMoveEventMakeLocate(Infos.ObjCommon objCommon, Inputs.LotOperationMoveEventMakeLocateParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/10 15:31
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    Infos.NewLotAttributes lotWaferMoveEventMakeMerge(Infos.ObjCommon objCommon, Inputs.LotWaferMoveEventMakeMergeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/13 14:05
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotWaferScrapEventMake(Infos.ObjCommon objCommon, Inputs.LotWaferScrapEventMakeParams params);

    /**
     * 记录Lot Terminate时的操作记录而创建的Event
     * @param objCommon
     * @param eventData
     * @param params
     * @version 0.1
     * @author Grant
     * @date 2021/7/10
     */
    void lotTerminateEventMake(Infos.ObjCommon objCommon, Event.LotEventData eventData,
                               TerminateReq.TerminateEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/13 16:45
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void waferChamberProcessEventMake(Infos.ObjCommon objCommon, Inputs.WaferChamberProcessEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/25 14:13
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void productRequestEventMakeRelease(Infos.ObjCommon objCommon, Inputs.ProductRequestEventMakeReleaseParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 2018/10/17 14:39
     * @param objCommon -
     * @param transactionID
     * @param lotID -
     * @param claimMemo
     * @return com.fa.cim.dto.RetCode
     */
    void productRequestEventMakeReleaseCancel(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier lotID, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/25 15:26
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void productRequestEventMakeUpdate(Infos.ObjCommon objCommon, Inputs.ProductRequestEventMakeUpdateParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/1 17:10
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void vendorLotEventMake(Infos.ObjCommon objCommon, Inputs.VendorLotEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/2 10:03
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void equipmentBufferResourceTypeChangeEventMake(Infos.ObjCommon objCommon, Inputs.EquipmentBufferResourceTypeChangeEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/2 11:27
     * @param objCommon
     * @param equipmentID
     * @param portOperationModeList
     * @param claimMemo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void equipmentModeChangeEventMake(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.PortOperationMode> portOperationModeList, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since  2019/7/2 13:25
     * @param objCommon -
     * @param params -
     */
    void objectNoteEventMake(Infos.ObjCommon objCommon, Inputs.ObjectNoteEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/7/2 14:02
     * @param objCommon -
     * @param params -
     */
    void equipmentPortStatusChangeEventMake(Infos.ObjCommon objCommon, Inputs.EquipmentPortStatusChangeEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/7/2 14:48
     * @param objCommon -
     * @param params -
     */
    void equipmentStatusChangeEventMake(Infos.ObjCommon objCommon, Inputs.EquipmentStatusChangeEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/2 15:54
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void eqpMonitorJobChangeEventMake(Infos.ObjCommon objCommon, Inputs.EqpMonitorJobChangeEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/3 9:52
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void eqpMonitorChangeEventMake(Infos.ObjCommon objCommon, Inputs.EqpMonitorChangeEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/3 14:01
     * @param objCommon
     * @param transactionID
     * @param equipmentID
     * @param eqpChamberStateList
     * @param claimMemo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void chamberStatusChangeEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier equipmentID, List<Infos.EqpChamberState> eqpChamberStateList, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/8 17:03
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void durableXferStatusChangeEventMake(Infos.ObjCommon objCommon, Inputs.DurableXferStatusChangeEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/14 15:20
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void durableXferJobStatusChangeEventMake(Infos.ObjCommon objCommon, Inputs.DurableXferJobStatusChangeEventMakeIn params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param equipmentID
     * @param operationMode
     * @param controlJobID
     * @param cassetteID
     * @param strLotInCassette
     * @param claimMemo
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/3/5 13:28
     */
    void lotOperationMoveEventMakeOpeStart(Infos.ObjCommon strObjCommonIn,String transactionID,ObjectIdentifier equipmentID,String operationMode,
                                                      ObjectIdentifier controlJobID, ObjectIdentifier cassetteID,Infos.LotInCassette strLotInCassette,String claimMemo);
    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param equipmentID
     * @param operationMode
     * @param controlJobID
     * @param cassetteID
     * @param strLotInCassette
     * @param claimMemo
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/7/8 17:22
     */
    void lotOperationMoveEventMakeOpeStartCancel(Infos.ObjCommon strObjCommonIn, String transactionID, ObjectIdentifier equipmentID, String operationMode, ObjectIdentifier controlJobID, ObjectIdentifier cassetteID, Infos.LotInCassette strLotInCassette, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/9 16:29
     * @param objCommon
     * @param transactionID
     * @param waferXferList
     * @param claimMemo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotWaferSortEventMake(Infos.ObjCommon objCommon, String transactionID, List<Infos.WaferTransfer> waferXferList, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/9 17:48
     * @param objCommon
     * @param transactionID
     * @param durableID
     * @param durableType
     * @param actionCode
     * @param claimMemo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void durableChangeEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier durableID, String durableType, String actionCode, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/12 10:21
     * @param objCommon
     * @param transactionID
     * @param controlJobID
     * @param controlJobStatus
     * @param startCassetteList
     * @param claimMemo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void controlJobStatusChangeEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier controlJobID, String controlJobStatus, List<Infos.StartCassette> startCassetteList, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/18 14:38
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void processJobChangeEventMake(Infos.ObjCommon objCommon, Inputs.ProcessJobChangeEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/12 11:11
     * @param objCommon
     * @param transactionID
     * @param equipmentID
     * @param actionCode
     * @param machineRecipeID
     * @param physicalRecipeID
     * @param fileLocation
     * @param fileName
     * @param formatFlag
     * @param claimMemo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void recipeBodyManageEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier equipmentID, String actionCode, ObjectIdentifier machineRecipeID, String physicalRecipeID, String fileLocation, String fileName, boolean formatFlag, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/14 10:22
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void entityInhibitEventMake(Infos.ObjCommon objCommon, Inputs.EntityInhibitEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/14 12:54
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotChangeEventMake(Infos.ObjCommon objCommon, Inputs.LotChangeEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/14 13:30
     * @param objCommon
     * @param params -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void qTimeChangeEventMake(Infos.ObjCommon objCommon, Inputs.QTimeChangeEventMakeParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableBankMoveEvent_Make_in
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/1 12:15
     */
    public void durableBankMoveEventMake( Infos.ObjCommon strObjCommonIn, Infos.DurablebankmoveeventMakeIn strDurableBankMoveEvent_Make_in );


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param strStartCassette
     * @param controlJobID
     * @param equipmentID
     * @param claimMemo
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/5/11 11:27
     */
    void collectedDataEventMake(Infos.ObjCommon strObjCommonIn, String transactionID, List<Infos.StartCassette> strStartCassette
            , ObjectIdentifier controlJobID, ObjectIdentifier equipmentID, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/18 16:26
     * @param objCommon
     * @param transactionID
     * @param lotID
     * @param flowBatchID
     * @param claimMemo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotFlowBatchEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier lotID, ObjectIdentifier flowBatchID, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/18 17:09
     * @param objCommon
     * @param transactionID
     * @param testMemo
     * @param experimentalLotRegistInfo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void experimentalLotRegistEventMake(Infos.ObjCommon objCommon, String transactionID, String testMemo, Infos.ExperimentalLotRegistInfo experimentalLotRegistInfo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/19 13:25
     * @param objCommon
     * @param transactionID
     * @param routeID
     * @param operationNumber
     * @param productID
     * @param withExecHoldFlag
     * @param holdType
     * @param reasonCodeID
     * @param entryType
     * @param claimMemo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void processHoldEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier routeID, String operationNumber, ObjectIdentifier productID, boolean withExecHoldFlag, String holdType, ObjectIdentifier reasonCodeID, String entryType, String claimMemo);
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/24 22:34
     * @param objCommon
     * @param autoDispatchControlEventMakeIn -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void autoDispatchControlEventMake(Infos.ObjCommon objCommon, Inputs.AutoDispatchControlEventMakeIn autoDispatchControlEventMakeIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * @param objCommon objCommon
     * @param objEquipmentFlowBatchMaxCountChangeEventMakeIn objEquipmentFlowBatchMaxCountChangeEventMakeIn
     * @return RetCode
     * @author ZQI
     * @date 2018/12/18 15:33:56
     */
    void equipmentFlowBatchMaxCountChangeEventMake(Infos.ObjCommon objCommon, Inputs.ObjEquipmentFlowBatchMaxCountChangeEventMakeIn objEquipmentFlowBatchMaxCountChangeEventMakeIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param equipmentID
     * @param controlJobID
     * @param lotID
     * @param actionCode
     * @param claimMemo
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/1 13:27
     */
    void processStatusEventMake(Infos.ObjCommon strObjCommonIn, String transactionID, ObjectIdentifier  equipmentID, ObjectIdentifier controlJobID,
                                ObjectIdentifier lotID, String actionCode, String claimMemo );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableControlJobStatusChangeEvent_Make_in
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/1 14:31
     */
    public void  durableControlJobStatusChangeEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableControlJobStatusChangeEventMake strDurableControlJobStatusChangeEvent_Make_in );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableHoldEvent_Make_in
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/2 10:08
     */
    public void durableHoldEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableHoldEventMakeIn strDurableHoldEvent_Make_in );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationCompleteEventMakeOpeCompIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/2 13:15
     */
    public void durableOperationCompleteEventMakeOpeComp(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOperationCompleteEventMakeOpeCompIn strDurableOperationCompleteEventMakeOpeCompIn );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationMoveEventMakeGatePassIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/2 13:36
     */
    public void durableOperationMoveEventMakeGatePass(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOperationMoveEventMakeGatePassIn strDurableOperationMoveEventMakeGatePassIn );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationMoveEventMakeLocateIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/2 14:31
     */
    public void durableOperationMoveEventMakeLocate(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOperationMoveEventMakeLocateIn strDurableOperationMoveEventMakeLocateIn );

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/2 16:34
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void fpcInfoRegistEventMake(Infos.ObjCommon objCommon, String transactionID, List<Infos.FPCInfoAction> strFPCInfoActionList, String claimMemo,String runCardID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/6 10:24
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void experimentalLotExecEventMake(Infos.ObjCommon objCommon, String txId, String testMemo, Infos.ExperimentalLotDetailResultInfo strExperimentalLotDetailResultInfo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/8/5 20:47
     * @param objCommon -
     * @param stringifiedObjectReference -
     * @param className -
     * @param hashedInfoList -
     * @param userDataActionList -
     * @param claimMemo -
     */
    void objectUserDataChangeEventMake(Infos.ObjCommon objCommon, String stringifiedObjectReference, String className, List<Infos.HashedInfo> hashedInfoList, List<Infos.UserDataAction> userDataActionList, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/8/6 10:10
     * @param objCommon -
     * @param transactionID -
     * @param parameterClass -
     * @param identifier -
     * @param userParameterValueList -
     */
    void scriptParameterChangeEventMake(Infos.ObjCommon objCommon, String transactionID, String parameterClass, String identifier, List<Infos.UserParameterValue> userParameterValueList);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationStartEventMakeOpeStartIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/2 17:52
     */
    public void durableOperationStartEventMakeOpeStart(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOperationStartEventMakeOpeStartIn strDurableOperationStartEventMakeOpeStartIn );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationStartEventMakeOpeStartCancelIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/5 10:33
     */
    public void durableOperationStartEventMakeOpeStartCancel(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOperationStartEventMakeOpeStartCancelIn strDurableOperationStartEventMakeOpeStartCancelIn );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableReworkEventMakeIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/5 10:36
     */
    public void durableReworkEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableReworkEventMakeIn strDurableReworkEventMakeIn );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param action
     * @param className
     * @param strDurableAttribute
     * @param claimMemo
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/5 11:22
     */
    public void  durableRegistEventMake(
            Infos.ObjCommon strObjCommonIn,
            String                       transactionID,
            String                       action,
            String                       className,
            Infos.DurableAttribute strDurableAttribute,
            String                       claimMemo );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurablePFXCreateEventMakeIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/5 11:35
     */
    public void durablePFXCreateEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurablePFXCreateEventMakeIn strDurablePFXCreateEventMakeIn );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurablePFXDeleteEventMakeIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/5 13:11
     */
    public void durablePFXDeleteEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurablePFXDeleteEventMakeIn strDurablePFXDeleteEventMakeIn );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param requestUserID
     * @param equipmentID
     * @param stockerID
     * @param AGVID
     * @param strEquipmentAlarm
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/5 15:21
     */
    public void equipmentAlarmEventMake(
            Infos.ObjCommon strObjCommonIn                  ,
            String                      transactionID                   ,
            User requestUserID                   ,
            ObjectIdentifier          equipmentID                     ,
            ObjectIdentifier          stockerID                       ,
            ObjectIdentifier          AGVID                           ,
            Infos.EquipmentAlarm strEquipmentAlarm                );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strEquipmentContainerMaxRsvCountUpdateEventMakeIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/5 15:25
     */
    public void equipmentContainerMaxRsvCountUpdateEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.EquipmentContainerMaxRsvCountUpdateEventMakeIn strEquipmentContainerMaxRsvCountUpdateEventMakeIn );

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/8/6 23:11
     * @param objCommonIn
     * @param systemMessageEventMakeIn -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void systemMessageEventMake(Infos.ObjCommon objCommonIn, Inputs.SystemMessageEventMakeIn systemMessageEventMakeIn);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/6 15:26
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sorterSorterJobEventMake(Infos.ObjCommon objCommon, Inputs.ObjSorterSorterJobEventMakeIn objSorterSorterJobEventMakeIn);

    /**
    * description: new sorter: 添加事件操作
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/6/23 10:41 上午 ZH Create
    *
    * @author ZH
    * @date 2021/6/23 10:41 上午
    * @param  ‐
    * @return void
    */
    void sorterSorterJobEventMakeNew(Infos.ObjCommon objCommon, com.fa.cim.sorter.Params.ObjSorterSorterJobEventMakeIn objSorterSorterJobEventMakeIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strCollectedDataChangeEventMakeIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/20 17:16
     */
    public void collectedDataChangeEventMake(
            Infos.ObjCommon strObjCommonIn,
            Infos.CollectedDataChangeEventMakeIn strCollectedDataChangeEventMakeIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param transactionID
     * @param action
     * @param lotID
     * @param routeID
     * @param operationNumber
     * @param strFutureReworkDetailInfoSeq
     * @param claimMemo
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/9/17 14:00
     */
    public void  lotFutureReworkEventMake(
            Infos.ObjCommon strObjCommonIn,
            String                               transactionID,
            String                               action,
            ObjectIdentifier                   lotID,
            ObjectIdentifier                   routeID,
            String                               operationNumber,
            List<Infos.FutureReworkDetailInfo>  strFutureReworkDetailInfoSeq,
            String                               claimMemo );

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 14:47
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void ownerChangeEventMake(Infos.ObjCommon objCommon, Inputs.ObjOwnerChangeEventMakeIn input);

    /**
     *
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/17 14:21
     */
    void bondingGroupEventMake(Infos.ObjCommon objCommon, String action, Infos.BondingGroupInfo strBondingGroupInfo, List<Infos.BondingMapInfo> strPartialReleaseSourceMapSeq);

    /**
     * This function makes Wafer Stacking change event.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/24 18:01
     */
    void waferStackingEventMake(Infos.ObjCommon objCommon, Infos.BondingGroupInfo bondingGroupInfo);

    /**
     * This function makes Wafer Stacking cancel event.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/27 15:04
     */
    void waferStackingCancelEventMake(Infos.ObjCommon objCommon, List<Infos.StackedWaferInfo> strStackedWaferInfoSeq, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/7 15:39
     * @param objCommon
     * @param transactionID
     * @param apcIf
     * @param opeCategory -
     * @return void
     */
    void APCIFPointUpdateEventMake(Infos.ObjCommon objCommon, String transactionID, Infos.APCIf apcIf, String opeCategory);

    /**
     * This function makes SLMSwitch change event.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/5/7 14:05
     */
    void equipmentSLMSwitchChangeEventMake(Infos.ObjCommon objCommon, String transactionID, ObjectIdentifier equipmentID, String fmcMode, String claimMemo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param action
     * @param season
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/28 13:06
     */
    void seasonPlanEventMake(Infos.ObjCommon objCommon, String action, Infos.Season season,String claimMemo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param action
     * @param seasonJob
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/28 13:08
     */
    void seasonJobEventMake(Infos.ObjCommon objCommon, String action, Infos.SeasonJob seasonJob,String claimMemo);

    void equipmentContainerMaxRsvCountUpdateEventMake(Infos.ObjCommon objCommon, Params.FmcRsvMaxCountUpdateReqInParams fmcRsvMaxCountUpdateReqInParams);

    /**
     * description: make runcard history
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/8/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/8/19 15:30
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void runCardEventMake(Infos.ObjCommon objCommon,String action,Infos.RunCardInfo runCardInfo);

    void durableJobStatusChangeEventMake(Infos.ObjCommon objCommon, Infos.DurableJobStatusChangeEvent event, String action);

    void reticleOperationEventMake(Infos.ObjCommon objCommon, Inputs.ReticleOperationEventMakeParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/1/18                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/1/18 14:38
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void collectedDataEventForPreviousOperationMake(Infos.ObjCommon objCommon, String transactionID, List<Infos.StartCassette> tmpStartCassette, ObjectIdentifier controlJobID, ObjectIdentifier equipmentID, String claimMemo);

    /**
     * description: layout recipe evnet make
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon - common
     * @param layoutRecipeEventParams - event params
     * @author YJ
     * @date 2021/3/6 0006 13:46
     */
    void layoutRecipeEventMake(Infos.ObjCommon objCommon,
                               LayoutRecipeParams.LayoutRecipeEventParams layoutRecipeEventParams);


    /**
     * description:  组monitor group 时生成 history
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/7/26 0026 13:14                        YJ                Create
     *
     * @author YJ
     * @date 2021/7/26 0026 13:14
     * @param objCommon - common params
     * @param lotMonitorGroupEventParams - lot 组 monitor group 信息
     */
    void lotMonitorGroupEventMake(Infos.ObjCommon objCommon,
                                  LotMonitorGroupParams.LotMonitorGroupEventParams lotMonitorGroupEventParams);

}
