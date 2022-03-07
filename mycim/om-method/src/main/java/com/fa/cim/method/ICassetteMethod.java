package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.*;
import org.springframework.data.domain.DomainEvents;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.util.List;

/**
 * description:
 * This file use to define the ILotMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 *
 * @author Bear
 * @since 2018/6/21 10:29
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

public interface ICassetteMethod {

    Page<Infos.FoundCassette> cassetteListGetDR170(Infos.ObjCommon objCommon, Inputs.ObjCassetteListGetDRIn objCassetteListGetDRIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn -
     * @param cassetteID -
     * @return com.fa.cim.dto.RetCode<Infos.CassetteAssignedMahineGetDR>
     * @author Ho
     * @since 2019/1/9 15:55:52
     */
    Infos.CassetteAssignedMahineGetDR cassetteAssignedMahineGetDR(Infos.ObjCommon strObjCommonIn,ObjectIdentifier cassetteID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn -
     * @param equipmentID -
     * @param strStartCassette -
     * @param operation -
     * @author Ho
     * @since 2018/10/31 15:39:20
     */
    void cassetteCheckConditionForOperationForInternalBuffer(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette, String operation);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param cassetteID -
     * @return  java.lang.String
     * @author Ho
     * @since 2018/9/28 15:43:50
     */
    String cassetteInterFabXferStateGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:cassette_baseInfo_GetDR
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param cassetteID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.CarrierBasicInfoInqResult>
     * @author Ho
     * @since 2018/10/15 13:41:56
     */
    Infos.DurableAttribute cassetteBaseInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:cassette_userDataInfo_GetDR
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param cassetteID -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.UserData>>
     * @author Ho
     * @since 2018/10/15 14:46:37
     */
    List<Infos.UserData> cassetteUserDataInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description: cassette_usageInfo_Reset
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param cassetteID -
     * @author Ho
     * @since 2018/10/16 11:08:52
     */
    void cassetteUsageInfoReset(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param cassetteID -
     * @param cassetteStatus -
     * @author Ho
     * @since 2018/9/28 17:52:26
     */
    void cassetteStateChange(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, String cassetteStatus);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param stockerID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.StockerInfoInqResult>
     * @author Ho
     * @since 2018/10/8 14:09:34
     */
    Results.StockerInfoInqResult cassetteFillInTxLGQ004DR(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  - common object
     * @param cassetteID - cassette ID
     * @return com.fa.cim.pojo.obj.RetCode<Infos.LotListInCassetteInfo>
     * @author Bear
     * @since 2018/6/25
     */
    Infos.LotListInCassetteInfo cassetteGetLotList(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:cassette_GetWaferMapDR
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  - common object
     * @param cassetteID - cassette ID
     * @return com.fa.cim.pojo.obj.RetCode<Outputs.ObjCassetteGetWaferMapOut>
     * @author Panda
     * @since 2018/5/16
     */
    List<Infos.WaferMapInCassetteInfo> cassetteGetWaferMapDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * *  Update the specified cassette's MultiLotType according to the following cases.
     * Get all lots in the specified cassette.
     * For all lots, check HoldState, ProcessState and InventoryState and get each LogicalRecipeID by each,
     * then check if all of LogicalRecipeID are same or not.
     * Set MultiLotType as belows and return its value accroding to the combination of Lots in carrier.
     * <p>
     * D4000098 Add ScrapWafer Handling Routine
     * <ScrapWafer Exist>
     * If there are scrap Wafers in cassette, then MultiLotType turned to SP_Cas_MultiLotType_MultiLotSingleRecipe
     * --------------------------------------------------------------------------------------------------
     * |Case #   | Status                    | Recipe | MultiLotType                                      |
     * |=========|===========================|========|===================================================|
     * |Any lot  | There are scrap wafer     | R1     | SP_Cas_MultiLotType_MultiLotMultiRecipe  (=MS)    |
     * --------------------------------------------------------------------------------------------------
     * <p>
     * <SingleLot case>
     * --------------------------------------------------------------------------------------------------
     * |Case #   | Status                    | Recipe | MultiLotType                                      |
     * |=========|===========================|========|===================================================|
     * |1. lot-A | WAIT / HOLD / BANK /INPR  | R1     | SP_Cas_MultiLotType_MultiLotSingleRecipe (=MS)    |
     * --------------------------------------------------------------------------------------------------
     * <p>
     * <MultiLot and SingleRecipe case>
     * --------------------------------------------------------------------------------------------------
     * |Case #   | Status                    | Recipe | MultiLotType                                      |
     * |=========|===========================|========|===================================================|
     * |1. lot-A | WAIT / INPR               | R1     | SP_Cas_MultiLotType_MultiLotSingleRecipe (=MS)    |
     * |   lot-B | WAIT / INPR               | R1     |                                                   |
     * +---------+---------------------------+--------+---------------------------------------------------+
     * |2. lot-A | WAIT / INPR               | R1     | SP_Cas_MultiLotType_MultiLotMultiRecipe  (=MM)    |
     * |   lot-B | HOLD / BANK               | R1     |                                                   |
     * +---------+---------------------------+--------+---------------------------------------------------+
     * |3. lot-A | HOLD / BANK               | R1     | SP_Cas_MultiLotType_MultiLotMultiRecipe  (=MM)    |
     * |   lot-B | HOLD / BANK               | R1     |                                                   |
     * --------------------------------------------------------------------------------------------------
     * <p>
     * <MultiLot and MultiRecipe case>
     * --------------------------------------------------------------------------------------------------
     * |Case #   | Status                    | Recipe | MultiLotType                                      |
     * |=========|===========================|========|===================================================|
     * |1. lot-A | WAIT / HOLD / BANK /INPR  | R1     | SP_Cas_MultiLotType_MultiLotMultiRecipe  (=MM)    |
     * |   lot-B | WAIT / HOLD / BANK /INPR  | R2     |                                                   |
     * --------------------------------------------------------------------------------------------------
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param cassetteID -
     * @return com.fa.cim.pojo.obj.RetCode<Outputs.ObjCassetteMultiLotTypeUpdateOut>
     * @author Bear
     * @since 2018/6/25
     */
    String cassetteMultiLotTypeUpdate(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * cassette_dispatchState_Get
     *
     * @param objCommon -
     * @param cassetteID -
     * @return boolean
     */
    Boolean cassetteDispatchStateGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);


    /**
     * cassette_reservedState_Get
     *
     * @param objCommon -
     * @param carrierID -
     * @return ObjCassetteReservedStateGetOut
     * @author ho
     */
    Outputs.ObjCassetteReservedStateGetOut cassetteReservedStateGet(Infos.ObjCommon objCommon, ObjectIdentifier carrierID);


    /**
     * cassette_transferState_Change
     *
     * @param objCommon -
     * @param stockerID -
     * @param equipmentID -
     * @param carrierID -
     * @param xferCassette -
     * @param transferStatusChangeTimeStamp -
     * @author ho
     */
    void cassetteTransferStateChange(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier equipmentID,
                                     ObjectIdentifier carrierID, Infos.XferCassette xferCassette, Timestamp transferStatusChangeTimeStamp,
                                     Infos.ShelfPosition shelfPosition);

    /**
     * description:
     * This method provides search function of scraped wafers specified by in-paramter search criteria
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param cassetteIDList -
     * @return com.fa.cim.pojo.obj.Outputs.ObjCassetteScrapWaferSelectOut
     * @author Bear
     * @since 2018/5/3
     */
    List<Infos.LotWaferMap> cassetteScrapWaferSelectDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @since 11/23/2018 10:40 AM
     * @param objCommon -
     * @param cassetteID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjCassetteInPostProcessFlagGetOut>
     */
    Boolean cassetteInPostProcessFlagGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  - common object
     * @param cassetteID -
     * @return com.fa.cim.pojo.obj.RetCode<Outputs.ObjCassetteTransferStateGetOut>
     * @author Bear
     * @since 2018/6/11
     */
    String cassetteTransferStateGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * <p>Check cassette's condition for StartReservation, OpeStart, and FlowBatching.
     * The following conditions are checked for each specified operation.
     * - controlJobID
     * - multiLotType
     * - transferState
     * - transferReserved
     * - dispatchState
     * - cassetteState
     * - maxBatchSize
     * - minBatchSize
     * - cassette's loadingSequenceNumber
     * - eqp's multiRecipeCapability and recipeParameter
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         - common object
     * @param eqpID             - eqp ID
     * @param portGroupID       - port group ID
     * @param startCassetteList - start cassette list
     * @param operation         - operation
     * @author Bear
     * @since 2018/7/4 16:57
     */
    void cassetteCheckConditionForOperation(Infos.ObjCommon objCommon, ObjectIdentifier eqpID, String portGroupID,
                                                       List<Infos.StartCassette> startCassetteList, String operation);/**
     /**
      * description:
      * change history:
      * date             defect#             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2021/1/10                               Neyo                create file
      *
      * @author: Neyo
      * @date: 2021/1/10 17:00
      * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
      */
    Boolean cassetteCheckConditionForOperationForBackSideClean(Infos.ObjCommon objCommon, ObjectIdentifier eqpID, String portGroupID,
                                                       List<Infos.StartCassette> startCassetteList, String operation);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon           objCommon
     * @param cassetteID          cassetteID
     * @param dispatchReserveFlag dispatchReserveFlag
     * @author PlayBoy
     * @since 2018/7/23
     */
    void cassetteDispatchStateChange(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, boolean dispatchReserveFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn objCommonIn
     * @param cassetteID  cassetteID
     * @author Nyx
     * @since 2018/7/23
     */
    void cassetteCheckConditionForUnloading(Infos.ObjCommon objCommonIn, ObjectIdentifier cassetteID);

    /**
     * description:
     * Get controlJobID in PosCassette object and set it to returned structure.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn objCommonIn
     * @param cassetteID  cassetteID
     * @return RetCode<String> control job id
     * @author Nyx
     * @since 2018/7/23
     */
    ObjectIdentifier cassetteControlJobIDGet(Infos.ObjCommon objCommonIn, ObjectIdentifier cassetteID);

    /**
     * description:<br/>
     * If InParam's StartCassette has EmptyCarrier,It investigates whether EmptyCarrier is valid category with NextOperation of lot.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param startCassetteList startCassetteList
     * @author PlayBoy
     * @since 2018/7/30
     */
    void emptyCassetteCheckCategoryForOperation(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param cassetteID cassetteID
     * @author PlayBoy
     * @since 2018/8/14
     */
    void cassetteCheckEmpty(ObjectIdentifier cassetteID);

    /**
     * description:
     * <p>Increase the usage count of specified cassette.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon           objCommon
     * @param cassetteID          cassetteID
     * @author PlayBoy
     * @since 2018/7/30
     */
    void cassetteUsageCountIncrement(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param startCassetteList startCassetteList
     * @author PlayBoy
     * @since 2018/8/8
     */
    void cassetteCheckConditionForOpeStartCancel(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * Update Casssette's Usage Information
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  objCommon
     * @param cassetteID cassetteID
     * @author PlayBoy
     * @since 2018/8/10
     */
    void cassetteUsageCountDecrement(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:Check cassette's condition for Loading.The following conditions are checked.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param equipmentID -
     * @param portID -
     * @param cassetteID  -
     * @author Nyx
     * @since 2018/8/20 10:04
     */
    void cassetteCheckConditionForLoading(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon      objCommon
     * @param startCassettes startCassettes
     * @author Paladin
     * @since 2018/8/13
     */
    void cassetteCheckConditionForOpeComp(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassettes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  objCommon
     * @param cassetteId cassetteId
     * @return ObjCassetteUsageLimitationCheckOut
     * @author Paladin
     * @since 2018/8/13
     */
    Outputs.ObjCassetteUsageLimitationCheckOut cassetteUsageLimitationCheck(Infos.ObjCommon objCommon, ObjectIdentifier cassetteId);


    /**
     * description:
     * <p>This objmethod returns cassette location information.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param cassetteID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.LotLocationInfo>
     * @author Bear
     * @since 2018/9/19 14:50
     */
    Infos.LotLocationInfo cassetteLocationInfoGetDR(ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @since 2019/9/26 13:18
     * @param objCommon -
     * @param cassetteID -
     * @return com.fa.cim.dto.Infos.LotListInCassetteInfo
     */
    Infos.LotListInCassetteInfo cassetteLotIDListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * <p>* If 1 carrier is loaded on 1 eqp, all input carriers must be loaded on the same eqp.
     *       (This method does not check carrier-eqp relation.)<br/>
     *   * If 1 carrier is not loaded on any eqp, all input carriers must not be loaded on any eqp.<br/>
     *       (This method does not check carrier-eqp relation.)<br/>
     *   * If carrier is loaded on some eqp, this method maintains eqp's carrier-lot data
     *     and inprocessing log data. and also maintain controlJob's carrier-lot information.<br/>
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param equipmentID equipmentID
     * @param waferXferList waferXferList
     * @author PlayBoy
     * @since 2018/10/8 14:52:43
     */
    @Deprecated
    void cassetteCheckConditionForExchange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.WaferTransfer> waferXferList);

    /**
     * description:
     * <p>cassette_getStatusDR<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param cassetteID -
     * @return RetCode<Outputs.ObjCassetteStatusOut>
     * @author Sun
     * @since 2018/10/18 11:17
     */
    Outputs.ObjCassetteStatusOut cassetteGetStatusDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * <p>cassette_position_UpdateByStockerInventory <br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param stockerID stockerID
     * @param carrierInfos carrierInfos
     * @return List<Infos.InventoriedLotInfo>
     * @author Paladin
     * @since 2018/10/17 14:53:13
     */
    List<Infos.InventoriedLotInfo> cassettePositionUpdateByStockerInventory(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, Infos.ShelfPosition shelfPosition, List<Infos.InventoryLotInfo> carrierInfos);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  -
     * @param cassetteID -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Sun
     * @since 10/29/2018 11:26 AM
     */
    String cassetteMultiLotTypeGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * cassette_reservationInfo_GetDR
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn Infos.ObjCommon
     * @param cassetteID ObjectIdentifier
     * @author Yuri
     * @since 2018/10/30 10:18:25
     */
    Outputs.CassetteReservationInfoGetDROut cassetteReservationInfoGetDR(Infos.ObjCommon objCommonIn, ObjectIdentifier cassetteID);

    /**
     * description:
     * cassette_lot_reserve
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn Infos.ObjCommon
     * @param reserveLot List<Infos.ReserveLot>
     * @param claimMemo String
     * @return RetCode<Outputs.CassetteLotReserveOut>
     * @author Yuri
     * @since 2018/11/1 09:44:03
     */
    Outputs.CassetteLotReserveOut cassetteLotReserve(Infos.ObjCommon objCommonIn, List<Infos.ReserveLot> reserveLot, String claimMemo);

    /**
     * description:
     * <p>cassette_position_UpdateByStockerInventory <br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param reserveCancelLots reserveCancelLots
     * @param claimMemo claimMemo
     * @return List<Infos.ReserveCancelLot>
     * @author Paladin
     * @since 2018/11/13 14:53:13
     */
    Outputs.ObjCassetteLotReserveCancelOut cassetteLotReserveCancel(Infos.ObjCommon objCommon, List<Infos.ReserveCancelLot> reserveCancelLots, String claimMemo);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn -
     * @param cassetteDBINfoGetDRInfo -
     * @author Yuri
     * @since 2018/11/9 13:41:09
     */
    Outputs.CassetteDBInfoGetDROut cassetteDBInfoGetDR(Infos.ObjCommon objCommonIn, Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfo);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn Infos.ObjCommon
     * @param cassetteID ObjectIdentifier
     * @author Yuri
     * @since 2018/11/9 14:15:10
     */
    Outputs.CassetteZoneTypeGetOut cassetteZoneTypeGet(Infos.ObjCommon objCommonIn, ObjectIdentifier cassetteID);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/14                          Wind
      * @param objCommon -
      * @param params -
      * @author Wind
      * @since 2018/11/14 17:35
      */
    void cassetteCategoryPortCapabilityCheckForContaminationControl(Infos.ObjCommon objCommon, Params.CarrierMoveFromIBRptParams params);

    /**
     * description:TODO-NOTIMPL: cassette_lotList_GetWithPriorityOrder
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @since 2018/12/3 17:51
     * @param objCommon -
     * @param cassetteID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjCassetteLotListGetWithPriorityOrderOut>
     */
    Outputs.ObjCassetteLotListGetWithPriorityOrderOut cassetteLotListGetWithPriorityOrder(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/5                          Wind
      * @param objCommon -
      * @param equipmentID -
      * @param flowBatchID -
      * @param flowBatchByManualActionReqCassettes -
      * @param operation -
      * @author Wind
      * @since 2018/12/5 16:41
      */
    void cassetteCheckConditionForFlowBatch(Infos.ObjCommon objCommon,
                                               ObjectIdentifier equipmentID,
                                               ObjectIdentifier flowBatchID,
                                               List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassettes,
                                               String operation);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/7                          Wind
      * @param objCommon -
      * @param equipmentID -
      * @param flowBatchByManualActionReqCassettes -
      * @param operation -
      * @param claimMemo -
      * @author Wind
      * @since 2018/12/7 9:32
      */
    void cassetteCheckCountForFlowBatch(Infos.ObjCommon objCommon,
                                           ObjectIdentifier equipmentID,
                                           List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassettes,
                                           String operation,
                                           String claimMemo);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @since 2019/3/25 11:15
     * @param objCommon -
     * @param controlJobID -
     * @param strStartCassette -
     */
    void cassetteCheckConditionForStartReserveCancel(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, List<Infos.StartCassette> strStartCassette);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon Infos.ObjCommon
     * @param cassetteID ObjectIdentifier
     * @param lotID ObjectIdentifier
     * @return Results.WhereNextInterByInqResult
     * @author Yuri
     * @since 2019/4/29 09:42:09
     */
	Outputs.CassetteDestinationInfoGetOut cassetteDestinationInfoGet (Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, ObjectIdentifier lotID);

	/**
	 * description:
	 * change history:
	 * date             defect             person             comments
	 * ---------------------------------------------------------------------------------------------------------------------
	 * @author lightyh
	 * @since 2019/6/25 17:41
	 * @param objCommon -
	 * @param cassetteID -
	 * @param lotID -
	 * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Results.WhereNextStockerInqResult>
	 */
    Outputs.CassetteDestinationInfoGetForSLMOut cassetteDestinationInfoGetForSLM (Infos.ObjCommon objCommon,
                                                                            ObjectIdentifier cassetteID,
                                                                            ObjectIdentifier lotID);
    /**
     * description:
     *
     * 此方法过时请调用Sorter method相关方法
     *
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @since 2019/6/14 9:26
     * @param objCommon -
     * @param waferXferList -
     * @param equipmentID -
     */
    @Deprecated
    void cassetteCheckConditionForWaferSort(Infos.ObjCommon objCommon, List<Infos.WaferTransfer> waferXferList, ObjectIdentifier equipmentID);


	/**
	 * description:
	 * change history:
	 * date             defect#             person             comments
	 * ---------------------------------------------------------------------------------------------------------------------
	 * 2019/6/13                               Neyo                create file
	 *
	 * @author Neyo
	 * @since 2019/6/13 17:49
	 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
	 */
    void cassetteInPostProcessFlagSet(Infos.ObjCommon objCommon, Inputs.ObjCassetteInPostProcessFlagSetIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/7/4 10:34
     * @param objCommon -
     * @param cassetteID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.CassetteTransferJobRecordGetDROut>
     */
    Infos.CarrierJobResult cassetteTransferJobRecordGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description: Make Parameter Nulti Carrier Transfer for txMultipleCarrierTransferReq.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/3                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/3 16:01
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut multiCarrierXferFillInOTMSW005InParm(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/4                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/4 18:17
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjCassetteDeliveryRTDInterfaceReqOut cassetteDeliveryRTDInterfaceReq(Infos.ObjCommon objCommonIn, String kind, ObjectIdentifier keyID);

    /**
     * description: Make Parameter Single Carrier Transfer for txSingleCarrierTransferReq.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/5                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/5 10:30
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjSingleCarrierXferFillInOTMSW006InParmOut singleCarrierXferFillInOTMSW006InParm(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier unLoadPortID, ObjectIdentifier cassetteID, Results.WhereNextStockerInqResult strWhereNextStockerInqResult);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/9                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/9 14:25
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
     Outputs.ObjCassetteListEmptyAvailablePickUpOut cassetteListEmptyAvailablePickup(Infos.ObjCommon objCommonIn, List<Infos.FoundCassette> strFoundCassette);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/11                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/11 15:22
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void cassetteCheckConditionForDelivery(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, String portGroupID, List<Infos.StartCassette> strStartCassette, String operation);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/11                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/11 16:30
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut cassetteDeliverySearchEmptyCassetteAssignPort(Infos.ObjCommon objCommonIn, ObjectIdentifier lotID, List<Infos.PortID> portIDSeq, List<Infos.FoundCassette> emptyCassetteSeq, List<ObjectIdentifier> omitCassetteSeq, List<ObjectIdentifier> omitPortIDSeq);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @since 2019/8/7 17:57
     * @param objCommon -
     * @param strNPWXferCassette -
     */
    void cassetteCheckConditionForArrivalCarrierCancel(Infos.ObjCommon objCommon, List<Infos.NPWXferCassette> strNPWXferCassette);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @since 2019/8/8 10:03
     * @param objCommon -
     * @param cassetteID -
     * @param loadPurposeType -
     */
    void cassetteSetNPWLoadPurposeType(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, String loadPurposeType);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/9/17 10:26
     * @param objCommon -
     * @param cassetteID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjCassetteTransferInfoGetDROut>
     */
    @Deprecated
    Outputs.ObjCassetteTransferInfoGetDROut cassetteTransferInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param cassetteID -
     * @return com.fa.cim.pojo.RetCode<Outputs.ObjCassetteEquipmentIDGetOut>
     * @author jerry
     * @since 22018/6/4 15:25
     */
    Outputs.ObjCassetteEquipmentIDGetOut cassetteEquipmentIDGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/27 1:04
     * @param objCommon -
     * @param cassetteID -
     */
    void cassetteStatusFinalizeForPostProcess(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param cassetteID
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @exception
     * @author ho
     * @date 2020/3/17 13:44
     */
    ObjectIdentifier cassetteUTSInfoGet (Infos.ObjCommon strObjCommonIn,ObjectIdentifier cassetteID );

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @since 2019/10/25 16:08
     * @param objCommon -
     * @param cassetteID -
     * @return com.fa.cim.dto.Infos.LotListInCassetteInfo
     */
    Infos.LotListInCassetteInfo cassetteLotListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/2/4 15:47                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/2/4 15:47
     * @param objCommon
     * @param cassetteID
     * @param setFlag
     * @param actionCode -
     * @return void
     */

    void cassetteDispatchAttributeUpdate(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, Boolean setFlag, String actionCode);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/4/26 13:04
     * @param objCommon
     * @param equipmentID
     * @param strStartCassette -
     * @return java.util.List<com.fa.cim.dto.Infos.ApcBaseCassette>
     */
    List<Infos.ApcBaseCassette> cassetteAPCInformationGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette);

    void cassetteCheckConditionForBondingGroup(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDs);

    /**
     * description:
     * <p>cassette_CheckConditionForSLMDestCassette</p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/5/11/011 14:09
     */
    void cassetteCheckConditionForSLMDestCassette(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, ObjectIdentifier cassetteID, ObjectIdentifier lotID);

    ObjectIdentifier cassetteEmptyCassetteForRetrievingGet(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID, ObjectIdentifier lotID);

    void cassetteSLMReserveEquipmentSet (Infos.ObjCommon objCommon, ObjectIdentifier  cassetteID, ObjectIdentifier  slmReserveEquipmentID);

}
