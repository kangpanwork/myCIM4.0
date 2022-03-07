package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.*;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.lmg.LotMonitorGroupResults;
import com.fa.cim.lot.LotNpwUsageRecycleCountUpdateParams;
import com.fa.cim.lot.LotNpwUsageRecycleLimitUpdateParams;
import com.fa.cim.lot.LotStbUsageRecycleLimitParams;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.product.DispatchReadinessState;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * description:
 * This file use to define the ILotMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/6/21 10:29
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

public interface ILotMethod {

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.LotInterFabXferStateResult>
     * @author Ho
     * @since 2018/11/6 10:39:17
     */
    String lotInterFabXferStateGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param cassetteID
     * @param strLotInCassette
     * @return com.fa.cim.dto.RetCode<com.fa.cim.bean.extension.Infos.LotRecipeParameterEventStructOut>
     * @exception
     * @author Ho
     * @date 2019/3/5 16:50
     */
    Infos.LotRecipeParameterEventStructOut lotRecipeParameterEventStruct(
            Infos.ObjCommon strObjCommonIn,
            ObjectIdentifier cassetteID,
            Infos.LotInCassette strLotInCassette
    );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param lotID
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Ho
     * @date 2019/1/9 16:42:58
     */
    String lotSubLotTypeGetDR(Infos.ObjCommon strObjCommonIn,ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param lotID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.ObjectIdentifier>
     * @author Ho
     * @date 2018/12/5 13:53:00
     */
    ObjectIdentifier lotMainRouteIDGet(Infos.ObjCommon strObjCommonIn, ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strOperationNameAttributes
     * @param strRequestActionFlagSeq
     * @param lotID
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.OperationFutureActionAttributes>>
     * @author Ho
     * @date 2018/12/4 14:54:06
     */
    List<Infos.OperationFutureActionAttributes> lotFutureActionInfoGetDR(Infos.ObjCommon strObjCommonIn, List<Infos.OperationNameAttributes> strOperationNameAttributes, List<Infos.HashedInfo> strRequestActionFlagSeq, ObjectIdentifier lotID);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param equipmentID
     * @param strStartCassette
     * @param operation
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/10/31 15:46:32
     */
    void lotCheckConditionForOperationForInternalBuffer(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette, String operation);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param changedLotAttributes
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/11/23 17:39:27
     */
    void lotChangeOrder(Infos.ObjCommon objCommon, Infos.ChangedLotAttributes changedLotAttributes);

    /**
     * description:lot_STB.cpp
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param productID
     * @param newLotAttributes -
     * @return com.fa.cim.pojo.obj.Outputs.ObjLotSTBOut
     * @author Jerry
     * @date 2018/5/7
     */
    Outputs.ObjLotSTBOut lotSTB(Infos.ObjCommon objCommon, ObjectIdentifier productID, Infos.NewLotAttributes newLotAttributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param holdReqList -
     * @return com.fa.cim.pojo.Outputs.ObjLotHoldOut
     * @author jerry
     * @date 2018/5/18 14:33
     */
    List<Infos.HoldHistory> lotHold(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.LotHoldReq> holdReqList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.pojo.RetCode<Outputs.ObjLotCurrentOperationInfoGetDROut>
     * @author jerry
     * @date 2018/6/29
     */
    Outputs.ObjLotCurrentOperationInfoGetDROut lotCurrentOperationInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.pojo.RetCode<Outputs.ObjLotCurrentOperationInfoGetOut>
     * @author Bear
     * @date 2018/7/27 11:15 lotCurrentOperationInfoGet
     */
    Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.pojo.Outputs.ObjLotFlowBatchSectionInfoGetDROut
     * @author Nyx
     * @date 2018/6/5
     */
    Infos.FlowBatchLotInfo lotFlowBatchSectionInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                 -
     * @param preparationCancelledLotID -
     * @return com.fa.cim.pojo.obj.RetCode<Outputs.ObjLotPreparationCancelInfoGetDROut>
     * @author Bear
     * @since 2018/5/22
     */
    Outputs.ObjLotPreparationCancelInfoGetDROut lotPreparationCancelInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier preparationCancelledLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                 -
     * @param objLotPreparationCancelIn -
     * @return com.fa.cim.dto.other.RetCode
     * @author Bear
     * @since 2018/5/23
     */
    List<Infos.ReceivedLotInfo> lotPreparationCancel(Infos.ObjCommon objCommon, Inputs.ObjLotPreparationCancelIn objLotPreparationCancelIn);


    /**
     * description:
     *
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/26                          nyx                modify context
     * @author Jerry
     * @date  10:49
     * @param objCommon
     * @param locateDirection
     * @param lotID
     * @param processRef -
     * @return com.fa.cim.dto.RetCode
     */
    void lotFutureHoldRequestsCheckLocate(Infos.ObjCommon objCommon, Boolean locateDirection, ObjectIdentifier lotID, Infos.ProcessRef processRef);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param mainProcessFlow
     * @param moduleProcessFlow
     * @param operationNumber   -
     * @return com.fa.cim.pojo.Outputs.ObjLotQtimeGetForRouteDROut
     * @author Jerry
     * @date 17:29
     */
    List<Infos.LotQtimeInfo> lotQtimeGetForRouteDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String mainProcessFlow, String moduleProcessFlow, String operationNumber);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 12:38
     * @param objCommon
     * @param lotID
     * @param eqpIDForGetReticleGroup
     * @param dummyPhotoLayer -
     * @return java.util.List<com.fa.cim.common.support.ObjectIdentifier>
     */
    List<ObjectIdentifier> lotReticleGroupsGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier eqpIDForGetReticleGroup, String dummyPhotoLayer);

    /**
     * description:lot_GetSourceLotsDR__180
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param sourceLotListInqParams -
     * @return com.fa.cim.pojo.obj.RetCode<Outputs.ObjLotGetSourceLotsOut>
     * @author Panda
     * @since 2018/5/4
     */
    Outputs.ObjLotGetSourceLotsOut lotGetSourceLotsDR(Infos.ObjCommon objCommon, Params.SourceLotListInqParams sourceLotListInqParams);


    /**
     * description:
     * method:lot_controlJobID_Get
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjLotControlJobIDGetOut>
     * @author Bear
     * @date 2018/10/23 11:30
     */
    ObjectIdentifier lotControlJobIDGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * 获取Lot的Control Job ID
     * @param objCommon
     * @param lot
     * @return
     * @version 0.1
     * @author Grant
     * @date 2021/7/8
     */
    ObjectIdentifier lotControlJobIDGet(Infos.ObjCommon objCommon, CimLot lot);

    /**
     * description:
     * check input parameter validity
     * [1] combination of input parameter pattern can be thought
     * (1) - All  New wafer ID is filled and All  Source wafer ID is filled.
     * (2) - Some New wafer ID is filled and All  Source wafer ID is filled.
     * (3) - No   New wafer ID is filled and All  Source wafer ID is filled.
     * (4) - All  New wafer ID is filled and Some Source wafer ID is filled.
     * (5) - Some New wafer ID is filled and Some Source wafer ID is filled.
     * (6) - No   New wafer ID is filled and Some Source wafer ID is filled.
     * (7) - All  New wafer ID is filled and No   Source wafer ID is filled.
     * (8) - Some New wafer ID is filled and No   Source wafer ID is filled.
     * (9) - No   New wafer ID is filled and No   Source wafer ID is filled.
     * <p>
     * [2] pattern of bank property
     * (a) - wafer ID automatic generation YES
     * (b) - wafer ID automatic generation NO
     * <p>
     * (1)&(a) ===> OK
     * (2)&(a) ===> OK
     * (3)&(a) ===> OK (bWaferIDAssignRequred = true)
     * (4)&(a) ===> OK
     * (5)&(a) ===> OK
     * (6)&(a) ===> OK (bWaferIDAssignRequred = true)
     * (7)&(a) ===> OK
     * (8)&(a) ===> Error
     * (9)&(a) ===> OK (bWaferIDAssignRequred = true)
     * <p>
     * (1)&(b) ===> OK
     * (2)&(b) ===> OK
     * (3)&(b) ===> OK
     * (4)&(b) ===> OK
     * (5)&(b) ===> OK
     * (6)&(b) ===> Error
     * (7)&(b) ===> OK
     * (8)&(b) ===> Error
     * (9)&(b) ===> Error
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param bankID
     * @param newLotAttributes -
     * @return com.fa.cim.pojo.obj.RetCode<Outputs.ObjLotParameterForLotGenerationCheckOut>
     * @author Bear
     * @date 2018/4/25
     */
    Outputs.ObjLotParameterForLotGenerationCheckOut lotParameterForLotGenerationCheck(Infos.ObjCommon objCommon, ObjectIdentifier bankID, Infos.NewLotAttributes newLotAttributes);

    /**
     * description:
     * generate wafer ID for new lot
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param newLotAttributes -
     * @return com.fa.cim.pojo.ObjLotWaferIDGenerate
     * @author Bear
     * @date 2018/4/25
     */
    Outputs.ObjLotWaferIDGenerateOut lotWaferIDGenerate(Infos.ObjCommon objCommon, Infos.NewLotAttributes newLotAttributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/6/13 13:45
     * @param objCommon
     * @param exChangeType
     * @param equipmentID
     * @param lotID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjLotEffectiveFPCInfoGetOut>
     */
    Outputs.ObjLotEffectiveFPCInfoGetOut lotEffectiveFPCInfoGet(Infos.ObjCommon objCommon, String exChangeType, ObjectIdentifier equipmentID, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 12:38
     * @param objCommon
     * @param lotID -
     * @return java.lang.String
     */
    String lotBondingGroupIDGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/9/29 15:05
     * @param objCommon -
     * @param lotTypeId -
     * @return java.util.List<com.fa.cim.dto.Infos.LotTypeInfo>
     */
    List<Infos.LotTypeInfo> lotTypeSubLotTypeInfoGet(Infos.ObjCommon objCommon, String lotTypeId);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/28 15:50
     * @param objCommon
     * @param stbCancelledLotID -
     * @return com.fa.cim.dto.Outputs.ObjLotSTBCancelInfoOut
     */
    Outputs.ObjLotSTBCancelInfoOut lotSTBCancelInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier stbCancelledLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 12:38
     * @param objCommon
     * @param objLotWafersIn -
     * @return java.util.List<com.fa.cim.dto.Infos.LotWaferAttributes>
     */
    List<Infos.LotWaferAttributes> lotWafersGetDR(Infos.ObjCommon objCommon, Inputs.ObjLotWafersGetIn objLotWafersIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/29 20:11
     * @param objCommon
     * @param lotSTBCancelIn -
     * @return com.fa.cim.dto.Outputs.ObjLotSTBCancelOut
     */
    Outputs.ObjLotSTBCancelOut lotSTBCancel(Infos.ObjCommon objCommon, Inputs.ObjLotSTBCancelIn lotSTBCancelIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param vendorLotReturnParams -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.LotQuantitySubtractResult>
     * @author jerry
     * @date 2018/4/26 10:43
     */
    Results.LotQuantitySubtractResult lotQuantitySubtract(Infos.ObjCommon objCommon, Params.VendorLotReturnParams vendorLotReturnParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param vendorLotReceiveParams -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.VendorLotReceiveReqResult>
     * @author jerry
     * @date 2018/4/26 10:42
     */
    Results.VendorLotReceiveReqResult lotMakeVendorLot(Infos.ObjCommon objCommon, Params.VendorLotReceiveParams vendorLotReceiveParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 12:39
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.dto.Results.LotFillInTxBKC007Result
     */
    Results.LotFillInTxBKC007Result lotFillInTxBKC007(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 12:39
     * @param objCommon
     * @param lotID
     * @param reasonCodeID
     * @param claimMemo -
     * @return com.fa.cim.dto.Results.LotBankHoldResult
     */
    Results.LotBankHoldResult lotBankHold(Infos.ObjCommon objCommon, ObjectIdentifier lotID,ObjectIdentifier reasonCodeID,String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 12:39
     * @param objCommon
     * @param lotID
     * @param reasonCodeID
     * @param ClaimMemo -
     * @return java.util.List<com.fa.cim.dto.Infos.HoldHistory>
     */
    List<Infos.HoldHistory> lotBankHoldRelease(Infos.ObjCommon objCommon, ObjectIdentifier lotID,ObjectIdentifier reasonCodeID,String ClaimMemo);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.dto.RetCode
     * @author Jerry
     * @date 2018/7/4 10:12
     */
    void lotCassetteCategoryUpdateForContaminationControl(Infos.ObjCommon objCommon, ObjectIdentifier lotID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/7/12                             Nyx
     * @param objCommon
     * @param lotInfoInqFlag
     * @param lotID          -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.LotInfo>
     * @author Jerry
     * @date 2018/4/19 10:11
     */
    Infos.LotInfo lotDetailInfoGetDR(Infos.ObjCommon objCommon, Infos.LotInfoInqFlag lotInfoInqFlag, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotInfoInqFlag
     * @param lotID          -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.LotInfo>
     * @author Jerry
     * @date 2018/5/24 11:00
     */
    Infos.LotInfo lotDBInfoGetDR(Infos.ObjCommon objCommon, Infos.LotInfoInqFlag lotInfoInqFlag, ObjectIdentifier lotID);

    /**
     * description:Check lot's duration for lot deletion and check relation to carrier and product request.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID       -
     * @return com.fa.cim.dto.RetCode<java.lang.Boolean>
     * @author Nyx
     * @date 2018/4/16
     */
    Boolean lotCheckDurationForOperation(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID       -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/4/16
     */
    void lotStateChangeShipped(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @since 2019/1/31 13:17
     * @param objCommon -
     * @param in -
     * @param searchCondition -
     * @return com.fa.cim.dto.RetCode<Results.LotListInqResult>
     */
    Page<Infos.LotListAttributes> lotListForDeletionGetDR(Infos.ObjCommon objCommon, Inputs.ObjLotListForDeletionGetDRIn in, SearchCondition searchCondition);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/7/2 9:26
     * @param objCommon
     * @param lotID -
     * @return void
     */
    void lotEventRecordCheckForDeletionDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/21 22:54
     * @param objCommon
     * @param lotType
     * @param productID
     * @param subLotType -
     * @return java.lang.String
     */
    String lotTypeLotIDAssign(Infos.ObjCommon objCommon, String lotType, ObjectIdentifier productID, String subLotType);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/8 16:03
     * @param objCommon
     * @param checkIn -
     * @return void
     */
    void lotSTBCancelCheck(Infos.ObjCommon objCommon, Inputs.ObjLotSTBCancelCheckIn checkIn);


    /**
     * description:
     * Check for preparation cancelled lot
     * 1.  Source lot Type
     * 2.  lot inventory state
     * 3.  Input bankID
     * 4.  Hold State
     * 5.  If lot state is Finished or not
     * 6.  lot finish state
     * 7.  lot process state
     * 8.  lot family's lot state(if preparation cancelled lot is original lot)
     * 9.  Existence check of ProdReq using cancelled lot
     * 10. If lot isn't in the cassette
     * 11. Check if lot doesn't have route information
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                      -
     * @param objLotPreparationCancelCheckIn -
     * @author Bear
     * @since 2018/5/24
     */
    void lotPreparationCancelCheck(Infos.ObjCommon objCommon, Inputs.ObjLotPreparationCancelCheckIn objLotPreparationCancelCheckIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param releaseReasonCodeID
     * @param holdReqList         -
     * @return com.fa.cim.pojo.Outputs.ObjLotHoldReleaseOut
     * @author Jerry
     * @date 2018/6/29
     */
    Outputs.ObjLotHoldReleaseOut lotHoldRelease(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier releaseReasonCodeID, List<Infos.LotHoldReq> holdReqList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.LotHoldListAttributes>>
     * @author Nyx
     * @date 2018/7/2 10:09
     */
    List<Infos.LotHoldListAttributes> lotFillInTxTRQ005DR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p>Check if all lot(operationStartFlag = TRUE) in cassette have one or more wafer(processJobExecFlag = TRUE).</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param startCassetteList -
     * @return com.fa.cim.pojo.Outputs.ObjLotProcessJobExecFlagValidCheckOut
     * @author Bear
     * @date 2018/7/4 15:34
     */
    Outputs.ObjLotProcessJobExecFlagValidCheckOut lotProcessJobExecFlagValidCheck(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * <p>Check lot's condition for StartReservation, OpeStart, and FlowBatching.
     * The following conditions are checked for each specified operation.
     * - controlJobID
     * - lot's equipmentID
     * - lot's current operation (must not last bank-In operation)
     * - lotHoldState
     * - lotProcessState
     * - lotInventoryState
     * - entityInhibition
     * - minWaferCount
     * - eqp's availability for specified lot
     * - lot's current operation (must not last bank-In operation)
     * - lot's recipe vs eqp's multiRecipeCapability (*3)
     * - in-parm's route/operationNumber/logicalrecipe & lot's current route/operationNumber/logicalrecipe.
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param eqpID
     * @param portGroupID
     * @param startCassetteList
     * @param operation         -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/7/4 17:15
     */
    void lotCheckConditionForOperation(Infos.ObjCommon objCommon, ObjectIdentifier eqpID, String portGroupID,
                                                  List<Infos.StartCassette> startCassetteList, String operation);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                -
     * @param objLotWaferIDListGetDRIn
     * @return com.fa.cim.pojo.Outputs.ObjLotWaferIDListGetDROut
     * @author panda
     * @date 2018/7/9 15:34
     */
    List<ObjectIdentifier> lotWaferIDListGetDR(Infos.ObjCommon objCommon, Inputs.ObjLotWaferIDListGetDRIn objLotWaferIDListGetDRIn);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param effectCondition -
     * @return com.fa.cim.pojo.Outputs.objLotFutureHoldRequestsDeleteEffectedByConditionOut
     * @author Jerry
     * @date 17:16
     */
    Outputs.ObjLotFutureHoldRequestsDeleteEffectedByConditionOut lotFutureHoldRequestsDeleteEffectedByCondition(Infos.ObjCommon objCommon, ObjectIdentifier lotID, Infos.EffectCondition effectCondition);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param effectCondition -
     * @return com.fa.cim.pojo.Outputs.objLotFutureHoldRequestsEffectByConditionOut
     * @author Jerry
     * @date 17:16
     */
    Outputs.ObjLotFutureHoldRequestsEffectByConditionOut lotFutureHoldRequestsEffectByCondition(Infos.ObjCommon objCommon,
                                                                                                ObjectIdentifier lotID,
                                                                                                Infos.EffectCondition effectCondition);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.pojo.Outputs.ObjLotMonitorRouteFlagGetOut
     * @author Bear
     * @date 2018/7/18 16:42
     */
    boolean lotMonitorRouteFlagGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param lotId     lotID
     * @return ObjLotCurrentOperationNumberGetOut
     * @author Paladin
     * @date 2018/8/13
     */
    String lotCurrentOpeNoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotId);



    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param lotId     lotID
     * @return ObjLotCurrentOperationNumberGetOut
     * @author Paladin
     * @date 2018/8/13
     */
    String lotHoldStateGet(Infos.ObjCommon objCommon, ObjectIdentifier lotId);



    /**
     * description:
     * <p>Get lot's logical recipe and pmcmg recipe for specified eqp.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param lotID       -
     * @return com.fa.cim.pojo.RetCode<Outputs.ObjLotRecipeGetOut>
     * @author Bear
     * @date 2018/7/27 13:44
     */
    Outputs.ObjLotRecipeGetOut lotRecipeGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier lotID);

    /**
     * description:
     * <p>lot_CheckConditionForPO : Check lot's condition for PO.
     * Check lot should use CURRENT PO or PREVIOUS PO
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.pojo.Outputs.ObjLotCheckConditionForProcessOperation
     * @author Sun
     * @date 2018/10/10 16:21
     */
    Boolean lotCheckConditionForPO(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn
     * @param lotListInCassetteInfo
     * @author PlayBoy
     * @date 2018/7/23
     */
    void lotCheckConditionForUnloading(Infos.ObjCommon objCommonIn, Infos.LotListInCassetteInfo lotListInCassetteInfo);

    /**
     * description:
     * lot_processJobExecFlag_ValidCheckForOpeStart
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param startCassetteList
     * @param processJobPauseFlag
     * @author PlayBoy
     * @date 2018/7/26
     */
    void lotProcessJobExecFlagValidCheckForOpeStart(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList, boolean processJobPauseFlag);

    /**
    /**     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   objCommon
     * @param lotID       lotID
     * @param cassetteID  cassetteID
     * @param equipmentID equipmentID
     * @param loadPortID  loadPortID
     * @return
     * @author PlayBoy
     * @date 2018/7/30
     */
    void lotCassetteCategoryCheckForContaminationControl(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier cassetteID, ObjectIdentifier equipmentID, ObjectIdentifier loadPortID);

    /**
     * description:
     * <p>Change processState of lot to SP_Lot_ProcState_Processing.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param startCassetteList startCassetteList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/7/30
     */
    void lotProcessStateMakeProcessing(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @return String testTypeID
     * @author PlayBoy
     * @date 2018/7/30
     */
    ObjectIdentifier lotTestTypeIDGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param params    -
     * @return com.fa.cim.pojo.Outputs.ObjLotFutureHoldRequestsMakeEntryOut
     * @author Nyx
     * @date 2018/7/19 15:02
     */
    Infos.FutureHoldHistory lotFutureHoldRequestsMakeEntry(Infos.ObjCommon objCommon, Params.FutureHoldReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/8/2 16:31
     * @param objCommon
     * @param params -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.FutureHoldHistory>>
     */
    List<Infos.FutureHoldHistory> lotFutureHoldRequestsDeleteEntry(Infos.ObjCommon objCommon, Params.FutureHoldCancelReqParams params);

    /**
     * description:
     * Check Process for lot
     * The following conditions are checked by this object
     * - lotProcessState
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param startCassetteList startCassetteList
     * @return RetCode<Object>
     * @author PlayBoy
     * @date 2018/8/8
     */
    void lotCheckConditionForOpeStartCancel(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);


    /**
     * description:
     * Get of the FutureHold information
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param lotID     lotID
     * @return RetCode ObjLotFutureHoldRequestsEffectForOpeStartCancelOut
     * @author PlayBoy
     * @date 2018/8/10
     */
    Outputs.ObjLotFutureHoldRequestsEffectForOpeStartCancelOut lotFutureHoldRequestsEffectForOpeStartCancel(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p>RequiredCassetteCategory of the process is acquired one after another.<p/>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param lotID     lotID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/8/14
     */
    String lotRequiredCassetteCategoryGetForNextOperation(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param startCassettes
     * @param loadPurposeType -
     * @return com.fa.cim.dto.RetCode
     * @author Jerry
     * @date 2018/8/16 16:37
     */
    void lotRecipeCombinationCheckForLoading(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassettes, String loadPurposeType);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.LotWaferAttributes>>
     * @author Jerry
     * @date 2018/8/16 16:39
     */
    List<Infos.LotWaferAttributes> lotMaterialsGetWafers(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param objLotWafersGetIn
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.LotWaferInfoAttributes>
     * @author Jerry
     * @date 2018/8/16 16:58
     */
    List<Infos.LotWaferInfoAttributes> lotWaferInfoListGetDR(Infos.ObjCommon objCommon, Inputs.ObjLotWafersGetIn objLotWafersGetIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param controlJobID -
     * @return com.fa.cim.pojo.Outputs.ObjLotCheckConditionForPOByControlJobOut
     * @author Jerry
     * @date 2018/8/16 17:30
     */
    Outputs.ObjLotCheckConditionForPOByControlJobOut lotCheckConditionForPOByControlJob(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier controlJobID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param reworkIn  -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/10/29 11:24
     */
    void lotCheckFlowBatchConditionForRework(Infos.ObjCommon objCommon, Inputs.ObjLotCheckFlowBatchConditionForReworkIn reworkIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param reworkReq -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/10/29 16:05
     */
    void lotCheckBondingFlowSectionForRework(Infos.ObjCommon objCommon, Infos.ReworkReq reworkReq);

    /**
     * description:Check lot's condition for Loading. The following conditions are checked for each specified operation.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param lotID       -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/8/14 16:51
     */
    void lotCheckConditionForLoading(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon      objCommon
     * @param startCassettes startCassettes
     * @return String control job id
     * @author Paladin
     * @date 2018/8/13
     */
    void lotCheckConditionForOpeComp(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassettes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param lotId     lotID
     * @return ObjLotCheckConditionForAutoBankInOut
     * @author Paladin
     * @date 2018/8/13
     */
    boolean  lotCheckConditionForAutoBankIn(Infos.ObjCommon objCommon, ObjectIdentifier lotId);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon              objCommon
     * @param lotId                  lotID
     * @param lotWaferAttributesList lotWaferAttributesList
     * @return ObjLotCheckConditionForAutoBankInOut
     * @author Paladin
     * @date 2018/8/16
     */
    void lotWaferChangeDie(Infos.ObjCommon objCommon, ObjectIdentifier lotId, List<Infos.LotWaferAttributes> lotWaferAttributesList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon      objCommon
     * @param lotId          lotID
     * @param lotHoldReqList lotHoldReqList
     * @return ObjLotFutureHoldEffectedProcessConversionOut
     * @author Paladin
     * @date 2018/8/16
     */
    Outputs.ObjLotFutureHoldEffectedProcessConversionOut lotFutureHoldEffectedProcessConversion(Infos.ObjCommon objCommon, ObjectIdentifier lotId, List<Infos.LotHoldReq> lotHoldReqList);


    /**
     * description:TODO-NOTIMPL: lot_RemoveFromMonitorGroup
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/9/17 14:55
     */
    void lotRemoveFromMonitorGroup(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * remove Lot from the monitor group
     * @param objCommon
     * @param lot
     * @version 0.1
     * @author Grant
     * @date 2021/7/8
     */
    void lotRemoveFromMonitorGroup(Infos.ObjCommon objCommon, CimLot lot);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Jerry
     * @date 2018/9/18 11:13
     */
    ObjectIdentifier lotCurrentRouteIDGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.pojo.RetCode<Outputs.ObjLotOriginalRouteListGetOut>
     * @author Jerry
     * @date 2018/9/18 11:13
     */
    Outputs.ObjLotOriginalRouteListGetOut lotOriginalRouteListGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * Create new wafer
     * If lot's total quantity and product quantity is the same, this method fails.
     * The target lot's total quantity > product quantity
     * This means that target lot just has quantity count and not have wafer object
     * Increment lot's product quantity
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param newWaferID -
     * @return com.fa.cim.pojo.obj.RetCode<Outputs.ObjLotWaferCreateOut>
     * @author Bear
     * @date 2018/4/25
     */
    Outputs.ObjLotWaferCreateOut lotWaferCreate(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String newWaferID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Nyx
     * @date 2018/9/25 15:24
     */
    String lotTransferStateGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * 获取Lot的Transfer State
     * @param objCommon
     * @param lot
     * @return
     * @version 0.1
     * @author Grant
     * @date 2021/7/8
     */
    String lotTransferStateGet(Infos.ObjCommon objCommon, CimLot lot);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param scrapWafersList -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/9/25 15:37
     */
    void lotMaterialsCheckExistance(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.ScrapWafers> scrapWafersList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param scrapWafersList -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/9/25 16:18
     */
    void lotMaterialsScrapByWafer(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.ScrapWafers> scrapWafersList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/9/25 16:31
     */
    void lotWaferLotHistoryPointerUpdate(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * 更新Lot的历史操作信息
     * @param objCommon
     * @param lot
     * @version 0.1
     * @author Grant
     * @date 2021/7/8
     */
    void lotWaferLotHistoryPointerUpdate(Infos.ObjCommon objCommon, CimLot lot);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 13:25
     * @param objCommon
     * @param lotID
     * @param mergedOperationNumber
     * @param returnOperationNumber -
     * @return void
     */
    void lotFutureHoldRequestsCheckSplit(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String mergedOperationNumber, String returnOperationNumber);

    /**
     * description:
     * <p>Sun: update return type (RetCode) and input parameter from String to ObjectIdentifier on 2018/10/31 11:13.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param parentLotID
     * @param childWaferID -
     * @return com.fa.cim.pojo.Outputs.ObjLotSplitWaferLotOut
     * @author Jerry
     * @date 2018/10/9 10:14
     */
    ObjectIdentifier lotSplitWaferLot(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, List<ObjectIdentifier> childWaferID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param cassetteID
     * @param scrapCancelWafersList -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/9/28 15:53
     */
    void lotMaterialsScrapCancelByWafer(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier cassetteID, List<Infos.ScrapCancelWafers> scrapCancelWafersList);


    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param controlUseState
     * @param usageCount      -
     * @return com.fa.cim.pojo.Outputs.ObjLotControlUseInfoChangeOut
     * @author Jerry
     * @date 2018/9/27 14:30
     */
    Outputs.ObjLotControlUseInfoChangeOut lotControlUseInfoChange(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String controlUseState, Integer usageCount);

    /**
     * description:
     * <p>lot_backupInfo_Get<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param lotID     lotID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/8 15:35:37
     */
    Infos.LotBackupInfo lotBackupInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * 查询Lot的Backup数据
     * @param objCommon
     * @param lot
     * @return
     * @version 0.1
     * @author Grant
     * @date 2021/7/8
     */
    Infos.LotBackupInfo lotBackupInfoGet(Infos.ObjCommon objCommon, CimLot lot);

    /**
     * description:
     * <p>lot_materialContainer_Change<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param newCassetteID newCassetteID
     * @param pLot          pLot
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/8 16:09:20
     */
    void lotMaterialContainerChange(ObjectIdentifier newCassetteID, Infos.PLot pLot);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param waferCount -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/10/8 15:02
     */
    void lotMaterialsScrapWaferNotOnRoute(Infos.ObjCommon objCommon, ObjectIdentifier lotID, Integer waferCount);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param waferCount -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/10/9 10:38
     */
    void lotMaterialsScrapWaferNotOnRouteCancel(Infos.ObjCommon objCommon, ObjectIdentifier lotID, Integer waferCount);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param parentLotID -
     * @return com.fa.cim.pojo.Outputs.ObjLotIDAssignOut
     * @author Jerry
     * @date 2018/10/10 14:52
     */
    ObjectIdentifier lotIDAssign(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID);

    /**
     * description:
     * LotID生成规则，将来会重写
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.pojo.Outputs.ObjLotFamilySplitNoCreateOut
     * @author Jerry
     * @date 2018/10/10 15:44
     */
    String lotFamilySplitNoCreate(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/10/10 15:30
     */
    void lotBankIn(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID       -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/10/11 10:58
     */
    void lotCheckBankInCancel(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/10/11 11:06
     */
    void lotBankInCancel(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param subLotType
     * @return com.fa.cim.pojo.Outputs.LotSubLotTypeGetDetailInfoDR
     * @author Sun
     * @date 2018/10/11 17:21
     */
    Outputs.LotSubLotTypeGetDetailInfoDR lotSubLotTypeGetDetailInfoDR(Infos.ObjCommon objCommon, String subLotType);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonm
     * @param lotID      -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjLotAllStateGetOut>
     * @author Jerry
     * @date 2018/10/16 14:16
     */
    Outputs.ObjLotAllStateGetOut lotAllStateGet(Infos.ObjCommon objCommonm, ObjectIdentifier lotID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param splitLotNotOnPfReqParams -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     * @author Jerry
     * @date 2018/10/16 14:15
     */
    ObjectIdentifier lotSplitWaferLotNotOnRoute(Infos.ObjCommon objCommon, Params.SplitLotNotOnPfReqParams splitLotNotOnPfReqParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param parentLotID
     * @param childLotID  -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjLotAllStateCheckSame>
     * @author Jerry
     * @date 2018/10/17 10:59
     */
    Outputs.ObjLotAllStateCheckSame lotAllStateCheckSame(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param
     * @param   -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Jerry
     * @date 2018/10/17 11:28
     */
    String lotBankCheckSame(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lot
     * @param bankID    -
     * @return com.fa.cim.dto.RetCode
     * @author Nyx
     * @date 2018/10/16 14:25
     */
    void lotNonProBankIn(Infos.ObjCommon objCommon, ObjectIdentifier lot, ObjectIdentifier bankID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Jerry
     * @date 2018/10/17 16:10
     */
    String lotHoldListCheckMerge(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param
     * @param   -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.LotHoldReq>>
     * @author Jerry
     * @date 2018/10/18 10:07
     */
    List<Infos.LotHoldReq> lotFutureHoldRequestsCheckMerge(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon             -
     * @param lotID                 -
     * @param returnOperationNumber -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/10/25 18:17
     */
    void lotFutureHoldRequestsCheckBranch(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String returnOperationNumber);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param parentLotID
     * @param childLotID  -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Jerry
     * @date 2018/10/18 10:08
     */
    String lotMergeWaferLot(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param lotID
     * @param routeID
     * @param operationNumber
     * @param operationPassCount
     * @param claimTime
     * @param operationCategory
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.LotOpeHisRParmGetDROut>
     * @exception
     * @author Ho
     * @date 2019/4/26 10:38
     */
    Infos.LotOpeHisRParmGetDROut lotOpeHisRParmGetDR(Infos.ObjCommon strObjCommonIn, String lotID, String routeID, String operationNumber, Long operationPassCount, String claimTime, String operationCategory);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lot       -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Nyx
     * @date 2018/10/18 11:22
     */
    String lotNonProBankOut(Infos.ObjCommon objCommon, ObjectIdentifier lot);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param aliasWaferNames -
     * @author Nyx
     * @since 2018/10/19 13:30
     */
    void lotAliasWaferNameUpdate(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.AliasWaferName> aliasWaferNames);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.LotAnnotationInqResult>
     * @author Nyx
     * @date 2018/10/22 10:48
     */
    Results.LotAnnotationInqResult lotCommentFillInTxPLQ002DR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param parentLotID
     * @param childLotID  -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Jerry
     * @date 2018/10/23 18:32
     */
    void lotMergeWaferLotNotOnRoute(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.LotMemoInfoInqResult>
     * @author Nyx
     * @date 2018/10/22 15:19
     */
    Results.LotMemoInfoInqResult lotNoteFillInTxPLQ003DR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotOpeMemoInfoInqParams
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.LotOpeMemoInfoInqResult>
     * @author Sun
     * @date 2018/10/23 10:08
     */
    Results.LotOpeMemoInfoInqResult lotOperationNoteFillInTxPLQ005DR(Infos.ObjCommon objCommon, Params.LotOpeMemoInfoInqParams lotOpeMemoInfoInqParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.LotOpeMemoListInqResult>
     * @author Sun
     * @since 2018/10/23 14:13
     */
    Results.LotOpeMemoListInqResult lotOperationNoteFillInTxPLQ004DR(Infos.ObjCommon objCommon, Params.LotOpeMemoListInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotID
     * @param lotNoteTitle
     * @param lotNoteDescription -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     * @author Nyx
     * @date 2018/10/23 16:12
     */
    ObjectIdentifier lotNoteMake(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String lotNoteTitle, String lotNoteDescription);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     * @author Sun
     * @since 2018/10/24 10:00
     */
    ObjectIdentifier lotOperationNoteMake(Infos.ObjCommon objCommon, Params.LotOperationNoteInfoRegisterReqParams params);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon        objCommon
     * @param lotID            lotID
     * @param externalPriority externalPriority
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/26 13:42:33
     */
    void lotExternalPriorityUpdate(Infos.ObjCommon objCommon, ObjectIdentifier lotID, int externalPriority);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param futureHoldSearchKey
     * @param count
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.FutureHoldListAttributes>>
     * @author Nyx
     * @date 2018/10/24 15:45
     */
    List<Infos.FutureHoldListAttributes> lotFutureHoldListbyKeyDR(Infos.ObjCommon objCommon, Infos.FutureHoldSearchKey futureHoldSearchKey, Integer count);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     * @author Nyx
     * @date 2018/10/25 16:35
     */
    ObjectIdentifier lotRouteIdGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/11/6 11:04
     */
    void lotFutureHoldRequestsCheckBranchCancel(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon       -
     * @param locateDirection -
     * @param lotID           -
     * @param processRef      -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/10/29 14:40
     */
    void lotFlowBatchCheckLocate(Infos.ObjCommon objCommon, Boolean locateDirection, ObjectIdentifier lotID, Infos.ProcessRef processRef);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param lotID -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Sun
     * @date 10/30/2018 3:50 PM
     */
    String lotContentsGet(ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Sun
     * @date 11/5/2018 10:23 AM
     */
    String lotProductionStateGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Sun
     * @date 11/5/2018 11:07 AM
     */
    String lotFinishedStateGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Sun
     * @date 11/5/2018 1:11 PM
     */
    String lotProcessStateGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.LotWaferMap>>
     * @author Sun
     * @date 11/5/2018 4:25 PM
     */
    List<Infos.LotWaferMap> lotWaferMapGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjGetLotCassetteOut>
     * @author Bear
     * @since 2018/10/22 18:04
     */
    ObjectIdentifier lotCassetteGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * 获取指定Lot的Cassette信息
     * @param objCommon
     * @param lot
     * @return
     * @version 0.1
     * @author Grant
     * @date 2021/7/8
     */
    ObjectIdentifier lotCassetteGet(Infos.ObjCommon objCommon, CimLot lot);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Bear
     * @date 2018/11/5 17:21
     */
    String lotStateGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotIDs    -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 11/6/2018 10:09 AM
     */
    void lotCheckLockHoldConditionForOperation(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotIDs);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   -
     * @param parentLotID -
     * @param childLotID  -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 11/6/2018 2:22 PM
     */
    void lotHoldListCheckReworkCancel(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     * @author Sun
     * @date 11/6/2018 2:52 PM
     */
    ObjectIdentifier lotFlowBatchIDGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * 获取Lot的FlowBatchID
     * @param objCommon
     * @param lot
     * @return
     * @version 0.1
     * @author Grant
     * @date 2021/7/8
     */
    ObjectIdentifier lotFlowBatchIDGet(Infos.ObjCommon objCommon, CimLot lot);

    /**
     * description:
     * <p>Check if actions of each strDCDef related to strLotInCassette.lotID included in strStartCassette.
     * Then return the out-parameter data sequence as bellows;
     * If, the lot which is required to do action of SPEC check result is Monitor-lot,
     * and, some production lots are connected to its monitor, all of production lots are
     * effected the same action.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param outData                            outData
     * @param objCommon                          objCommon
     * @param startCassetteList                  startCassetteList
     * @param interFabMonitorGroupActionInfoList interFabMonitorGroupActionInfoList
     * @param dcActionLotResultList              dcActionLotResultList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/11/6 11:18:38
     */
    Outputs.ObjLotHoldRecordEffectSpecCheckResultOut lotHoldRecordEffectSpecCheckResult(Outputs.ObjLotHoldRecordEffectSpecCheckResultOut outData, Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList, List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoList, List<Results.DCActionLotResult> dcActionLotResultList);



    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Bear
     * @date 2018/11/5 18:12
     */
    String lotInventoryStateGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjLotInPostProcessFlagOut>
     * @author Bear
     * @date 2018/11/5 18:24
     */
    Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/27 11:18
     * @param objCommon
     * @param lotID
     * @param inPostProcessFlag -
     * @return void
     */
    void lotInPostProcessFlagSet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, boolean inPostProcessFlag);

    /**
     * description:
     * <p>Check if actions of each strSpcCheckLot.
     * Then return the out-parameter data sequence as bellows;
     * If, the lot which is required to do action of SPC check result is Monitor-lot,
     * and, some production lots are connected to its monitor, all of production lots are
     * effected the same action.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                          objCommon
     * @param spcCheckLotList                    spcCheckLotList
     * @param interFabMonitorGroupActionInfoList interFabMonitorGroupActionInfoList
     * @param dcActionLotResultList              dcActionLotResultList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/11/6 16:52:43
     */
    Outputs.ObjLotHoldRecordEffectSPCCheckResultOut lotHoldRecordEffectSPCCheckResult(Infos.ObjCommon objCommon, List<Infos.SpcCheckLot> spcCheckLotList, List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoList, List<Results.DCActionLotResult> dcActionLotResultList);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param lotID     lotID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/11/7 10:28:51
     */
    String lotTypeGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/11/7 14:48
     * @param objCommon
     * @param lotID
     * @param toBankID -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void lotBankMove(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier toBankID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/11/7 15:39
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void lotStateCancelShipped(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     *   set lot's processLagTime information.
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/11/8 11:19
     * @param objCommon -
     * @param lotID -
     * @param processLagTime -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void lotProcessLagTimeSet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String processLagTime);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/11/8 17:28
     * @param objCommon -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.NPWUsageStateSelectionInqResult>
     */
    List<Infos.LotCtrlStatus> lotFillInTxPCQ017DR(Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * 2018/12/25       Bug-148            Sun                filter lot for Move Bank Screen, only display 'InBank' Lot;
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param lotListInqParams -
     * @param searchCondition  -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.LotListInqResult>
     * @author Sun
     * @since 11/20/2018 4:42 PM
     */
    Page<Infos.LotListAttributes> lotListGetDR(Infos.ObjCommon objCommon, Params.LotListInqParams lotListInqParams, SearchCondition searchCondition);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/11/21 16:36
     * @param objCommon -
 * @param sourceLotID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjSourceLotProductRequestGetDROut>
     */
    ObjectIdentifier sourceLotProductRequestGetDR(Infos.ObjCommon objCommon, ObjectIdentifier sourceLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Paladin
     * @since 2018/11/23 16:36
     * @param objCommon objCommon
     * @param lotFamilyID lotFamilyID
     * @return List<Infos.WaferListInLotFamilyInfo>
     */
    List<Infos.WaferListInLotFamilyInfo> lotWafersStatusListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID);

    /**
     * description:lot_ChangeSchedule__110
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/3 11:02
     * @param objCommon
     * @param attributes -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjLotChangeScheduleOut>
     */
    Outputs.ObjLotChangeScheduleOut lotChangeSchedule(Infos.ObjCommon objCommon, Infos.ReScheduledLotAttributes attributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/2/21 9:43
     * @param objCommon
     * @param lotID -
     * @return java.util.List<com.fa.cim.dto.Infos.DataCollectionInfo>
     */
    List<Infos.DataCollectionInfo> lotCurrentOperationDataCollectionInformationGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);


    Infos.StartRecipe lotActualOperationStartRecipeGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/5                          Wind
      * @param objCommon
      * @param lotID
      * @return RetCode<Infos.EqpMonitorJobLotInfo>
      * @author Wind
      * @date 2018/12/5 17:28
      */
    Infos.EqpMonitorJobLotInfo lotEqpMonitorJobGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/6                          Wind
      * @param objCommon
      * @param equipmentID
      * @param portGroupID
      * @param strStartCassette
      * @param operation
      * @return RetCode<Object>
      * @author Wind
      * @date 2018/12/6 16:30
      */
    void lotCheckConditionForFlowBatch(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID,
                                       List<Infos.StartCassette> strStartCassette, String operation);
    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/6                          Wind
      * @param objCommon
      * @param lotIDs
      * @return RetCode<ObjectIdentifier>
      * @author Wind
      * @date 2018/12/6 17:31
      */
    ObjectIdentifier lotCassetteCheckSame(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotIDs);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/7                          Wind
      * @param objCommon
      * @param batchingReqLot
      * @return RetCode<Object>
      * @author Wind
      * @date 2018/12/7 10:28
      */
    void lotNextOperationCheckEntryPointOfFlowBatch(Infos.ObjCommon objCommon, Infos.BatchingReqLot batchingReqLot);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param   * @param null
     * @return java.util.List
     * @author Scott
     * @date 2018/12/12 14:42:5
     */
    List<Object[]> getWaitingLotList();

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param routeID
     * @param operationNumber -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.FutureReworkInfo>>
     * @author Nyx
     * @date 2018/12/12 15:39
     */
    List<Infos.FutureReworkInfo> lotFutureReworkListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier routeID, String operationNumber);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         -
     * @param startCassetteList -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 12/18/2018 1:41 PM
     */
    void lotProcessStateMakeWaiting(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         -
     * @param startCassetteList -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.LotHoldEffectList>>
     * @author Sun
     * @date 12/18/2018 2:59 PM
     */
    List<Infos.LotHoldEffectList> lotHoldRecordEffectMonitorIssue(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
     * description:lot_processJobExecFlag_GetRecycleSampling
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/18 10:39
     * @param objCommon
     * @param lotInCassette
     * @param lotID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.LotInCassette>
     */
    Infos.LotInCassette lotProcessJobExecFlagGetRecycleSampling(Infos.ObjCommon objCommon, Infos.LotInCassette lotInCassette, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/18 13:43
     * @param objCommon
     * @param lotID
     * @param targetOperationNo -
     * @return com.fa.cim.dto.RetCode<java.util.List<java.lang.String>>
     */
    List<String> lotProcessedWaferIDsGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String targetOperationNo);

    /**
     * description:lot_samplingMessage_Create
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/18 14:44
     * @param objCommon
     * @param lotID
     * @param messageType
     * @param messageText -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     */
    String lotSamplingMessageCreate(Infos.ObjCommon objCommon, ObjectIdentifier lotID, Integer messageType, String messageText);

    /**
     * description:lot_processJobExecFlag_GetPolicySampling
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/18 16:05
     * @param objCommon
     * @param lotID
     * @param equipmentID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.LotInCassette>
     */
    Infos.LotInCassette lotProcessJobExecFlagGetPolicySampling(Infos.ObjCommon objCommon, Infos.LotInCassette lotInCassette, ObjectIdentifier lotID, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param  objCommon
     * @param  equipmentID
     * @param  strBatchingReqLot
     * @return com.fa.cim.dto.RetCode
     * @author Scott
     * @date 2018/12/20 09:50:39
     */
    void lotOperationCheckSameForFlowBatch(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.BatchingReqLot> strBatchingReqLot);
    /**
     * description:lot_dataCollectionInformation_GetDR__120
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params    -
     * @return com.fa.cim.dto.RetCode<Results.EDCHistoryInqResult>
     * @author Sun
     * @date 12/24/2018 11:12 AM
     */
    Results.EDCHistoryInqResult lotDataCollectionInformationGetDR(Infos.ObjCommon objCommon, Params.EDCHistoryInqParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param  objCommon
     * @param  objLotCheckConditionForWhatNextEqpMonitorLotIn
     * @return com.fa.cim.dto.RetCode<java.util.List<Infos.WhatNextAttributes>>
     * @author Scott
     * @date 2019/1/8 13:02:46
     */
    List<Infos.WhatNextAttributes> lotCheckConditionForWhatNextEqpMonitorLot(Infos.ObjCommon objCommon, Inputs.ObjLotCheckConditionForWhatNextEqpMonitorLotIn objLotCheckConditionForWhatNextEqpMonitorLotIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Scott
     * @date 2019/1/8 17:02:32
     */
    void lotCheckConditionForEqpMonitor(Infos.ObjCommon objCommon, Inputs.strLotCheckConditionForEqpMonitorIn strLotCheckConditionForEqpMonitorIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Scott
     * @date 2019/1/8 17:02:24
     */
    List<Infos.WaferListInLotFamilyInfo> lotScrapWaferSelectDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> strLoScrapWaferSelectDrIn);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/3/19 14:43
     * @param objCommon
     * @param lotID -
     * @return java.lang.Boolean
     */
    Boolean lotFPCAvailFlagGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/3/25 14:10
     * @param objCommon
     * @param controlJobID
     * @param strStartCassette -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotCheckConditionForStartReserveCancel(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, List<Infos.StartCassette> strStartCassette);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/3/27 14:12
     * @param objCommon
     * @param equipmentID
     * @param waferIDInfoGetFlag
     * @param recipeParmInfoGetFlag
     * @param reticleInfoGetFlag
     * @param dcSpecItemInfoGetFlag -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.FPCInfo>>
     */
    List<Infos.FPCInfo> lotCurrentFPCInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier equipmentID,
                                                      Boolean waferIDInfoGetFlag, Boolean recipeParmInfoGetFlag, Boolean reticleInfoGetFlag, Boolean dcSpecItemInfoGetFlag);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/6/26 15:39
     * @param objCommon -
     * @param branchReq -
     */
    void lotCheckFlowBatchConditionForBranch(Infos.ObjCommon objCommon, Infos.BranchReq branchReq);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/6/26 16:30
     * @param objCommon -
     * @param branchReq -
     */
    void lotCheckBondingFlowSectionForBranch(Infos.ObjCommon objCommon, Infos.BranchReq branchReq);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @since 2019/6/26 16:37
     * @param objCommon -
 * @param lotID -
 * @param processRef -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotBondingFlowSectionCheckLocate(Infos.ObjCommon objCommon, ObjectIdentifier lotID, Infos.ProcessRef processRef);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/26 16:24
     * @param objCommon
     * @param cassetteLocationInfo
     * @param lotStatus -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjLotEquipmentOrderGetByLotStatusOut>
     */
    Outputs.ObjLotEquipmentOrderGetByLotStatusOut lotEquipmentOrderGetByLotStatus(Infos.ObjCommon objCommon, Infos.LotLocationInfo cassetteLocationInfo, Infos.LotStatusInfo lotStatus);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/26 17:57
     * @param objCommon
     * @param lotID
     * @param productID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjLotQueuedMachinesGetByOperationOrderOut>
     */
    Outputs.ObjLotQueuedMachinesGetByOperationOrderOut lotQueuedMachinesGetByOperationOrder(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier productID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/26 18:06
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjLotQueuedMachinesGetDROut>
     */
    Outputs.ObjLotQueuedMachinesGetDROut lotQueuedMachinesGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/7/10 11:03
     * @param objCommon
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotCheckConditionForEqpMonitorLotReserve(Infos.ObjCommon objCommon, Inputs.LotCheckConditionForEqpMonitorLotReserveIn lotCheckConditionForEqpMonitorLotReserveIn);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/15                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/15 21:29
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.LotHoldEffectList> lotHoldRecordEffectMonitorIssueForPostProc(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/15                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/15 22:26
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjLotPreviousOperationInfoGetOut lotPreviousOperationInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

	/**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/9 17:02
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjLotProductIDGetOut lotProductIDGet(Infos.ObjCommon objCommonIn, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/30 9:49
     * @param objCommon
     * @param sourceLotID
     * @param destLotID -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void lotProcessOperationDataTransferInProcessing(Infos.ObjCommon objCommon, ObjectIdentifier sourceLotID, ObjectIdentifier destLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/31 18:15
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout>
     */
    Outputs.ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout lotPostProcessPreviousBranchAndReturnInfoGetDR(Infos.ObjCommon objCommon,ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 13:11
     * @param objCommon
     * @param controlJobID
     * @param strStartCassette -
     * @return void
     */
    void lotCheckConditionForArrivalCarrierCancel(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, List<Infos.StartCassette> strStartCassette);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/8/15 17:11
     * @param objCommon
     * @param equipmentID
     * @param lotIDs -
     * @return com.fa.cim.dto.Outputs.ObjLotPreviousOperationDataCollectionInformationGetOut
     */
    Outputs.ObjLotPreviousOperationDataCollectionInformationGetOut lotPreviousOperationDataCollectionInformationGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<ObjectIdentifier> lotIDs);


    Outputs.ObjLotCurrentOperationDataCollectionInformationGetOut lotCurrentOperationDataCollectionInformationGet(Infos.ObjCommon objCommon,
                                                                                                                   ObjectIdentifier equipmentID,
                                                                                                                   List<ObjectIdentifier> lotIDs);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param lotID
     * @param routeID
     * @param operationNumber
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/9/17 13:36
     */
    void lotFutureReworkRequestDelete(
            Infos.ObjCommon strObjCommonIn,
            ObjectIdentifier                 lotID,
            ObjectIdentifier                 routeID,
            String                             operationNumber );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param updateFlag
     * @param lotID
     * @param routeID
     * @param operationNumber
     * @param strFutureReworkDetailInfoSeq
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/9/17 13:40
     */
    void lotFutureReworkRequestRegist(
            Infos.ObjCommon strObjCommonIn,
            Boolean updateFlag,
            ObjectIdentifier lotID,
            ObjectIdentifier routeID,
            String operationNumber,
            List<Infos.FutureReworkDetailInfo> strFutureReworkDetailInfoSeq);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param monitorGroupID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.InterFabMonitorGroups>
     * @author Jerry
     * @date 2018/11/16 14:26
     */
    Infos.InterFabMonitorGroups lotMonitoredLotsGetDR(Infos.ObjCommon objCommon, ObjectIdentifier monitorGroupID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/23 21:11
     * @param objCommon
     * @param lotID
     * @param eqpID -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.EntityIdentifier>>
     */
    List<Infos.EntityIdentifier> lotEntityIDListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier eqpID);

    /**
     * description:
     * <p>get eqp monitor section information</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.pojo.Outputs.ObjEquipmentMonitorSectionInfoGetForJobOut
     * @author Bear
     * @date 2018/7/20 13:30
     */
    Outputs.ObjEquipmentMonitorSectionInfoGetForJobOut lotEqpMonitorSectionInfoGetForJob(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.pojo.Outputs.ObjEquipmentMonitorOperationLabelGetOut
     * @author Bear
     * @date 2018/7/20 16:04
     */
    List<Infos.EqpMonitorLabelInfo> lotEqpMonitorOperationLabelGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/2/21 13:25
     * @param objCommon
     * @param code
     * @param verifiedLots
     * @param equipmentID
     * @param cassetteID
     * @param portID
     * @param lotPurposeType -
     * @return com.fa.cim.dto.RetCode<java.util.List<Infos.LoadingVerifiedLot>>
     */
    List<Infos.LoadingVerifiedLot> loadedLotVerifiedInfoCheckConditionForForceLoad(Infos.ObjCommon objCommon, int code, List<Infos.LoadingVerifiedLot> verifiedLots,
                                                                                            ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier portID, String lotPurposeType);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/2/21 13:48
     * @param objCommon
     * @param equipmentID
     * @param cassetteID
     * @param portID
     * @param lotPurposeType
     * @param verifiedLots -
     * @return com.fa.cim.dto.RetCode<java.util.List<Infos.LoadingVerifiedLot>>
     */
    List<Infos.LoadingVerifiedLot> loadedLotVerifiedInfoChangeForForceLoad(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
                                                                           ObjectIdentifier cassetteID, ObjectIdentifier portID, String lotPurposeType, List<Infos.LoadingVerifiedLot> verifiedLots);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/29 21:19
     * @param objCommon
     * @param lotID -
     * @return void
     */
    void lotQtimeDeleteDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param   objCommon
     * @param   futureActionDetailInfoInqParams
     * @return  ObjAutoDispatchControlInfoGetDROut
     * @author Scott
     * @date 2018/12/7 17:59:40
     */
    Outputs.lotFutureReworkListGetDROut lotFutureReworkListGetDR(Infos.ObjCommon objCommon, Params.FutureActionDetailInfoInqParams futureActionDetailInfoInqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strLotProcessHistory_GetDR_in
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.LotProcessHistoryGetDROut>
     * @exception
     * @author Ho
     * @date 2019/4/29 16:39
     */
    Infos.LotProcessHistoryGetDROut lotProcessHistoryGetDR(Infos.ObjCommon strObjCommonIn,
                                                           Infos.LotProcessHistoryGetDRIn strLotProcessHistory_GetDR_in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/30 17:42
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.dto.Outputs.ObjLotQTimeGetDROut
     */
    Outputs.ObjLotQTimeGetDROut lotQTimeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/10/8 16:15
     * @param objCommon
     * @param equipmentID
     * @param controlJobID
     * @param strStartCassette
     * @param operation -
     * @return void
     */
    void lotBondingGroupUpdateByOperation(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> strStartCassette, String operation);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/10/14 14:40
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.dto.Outputs.ObjLotBondingOperationInfoGetDROut
     */
    Outputs.ObjLotBondingOperationInfoGetDROut lotBondingOperationInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/10/14 16:23
     * @param objCommon
     * @param exchangeType
     * @param equipmentID
     * @param lotID
     * @param routeID
     * @param operationNumber -
     * @return com.fa.cim.dto.Outputs.ObjLotEffectiveFPCInfoForOperationGetOut
     */
    Outputs.ObjLotEffectiveFPCInfoForOperationGetOut lotEffectiveFPCInfoForOperationGet(Infos.ObjCommon objCommon, String exchangeType, ObjectIdentifier equipmentID, ObjectIdentifier lotID, ObjectIdentifier routeID, String operationNumber);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/10/14 16:28
     * @param objCommon
     * @param lotID
     * @param routeID
     * @param operationNumber -
     * @return com.fa.cim.dto.Infos.FPCDispatchEqpInfo
     */
    Infos.FPCDispatchEqpInfo lotFPCdispatchEqpInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier routeID, String operationNumber);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/10/15 13:41
     * @param objCommon
     * @param objLotFPCInfoForOperationGetIn -
     * @return java.util.List<com.fa.cim.dto.Infos.FPCInfo>
     */
    List<Infos.FPCInfo> lotFPCInfoForOperationGet(Infos.ObjCommon objCommon, Inputs.ObjLotFPCInfoForOperationGetIn objLotFPCInfoForOperationGetIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 13:10
     * @param objCommon
     * @param preparationCancelledLotID
     * @param deleteWaferIDList -
     * @return void
     */
    void lotWafersDelete(Infos.ObjCommon objCommon, ObjectIdentifier preparationCancelledLotID, List<ObjectIdentifier> deleteWaferIDList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotIDs -
     * @return ObjectIdentifier
     * @author Nyx
     * @since 2018/5/31
     */
    ObjectIdentifier lotCassetteSameCheckDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotIDs);

    /**
     * description:
     * <p>Sun: update return data type and lotID type from String to ObjectIdentifier by 2018/10/30 13:25 </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon - common object
     * @param lotID     - lot id
     * @return com.fa.cim.pojo.Outputs.ObjLotCassetteListGetDROut
     * @author Nyx
     * @since 2018/6/7
     */
    ObjectIdentifier lotCassetteListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strLotControlJobHistory_GetDR_in
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.LotControlJobHistoryGetDROut>
     * @exception
     * @author Ho
     * @since 2019/4/29 16:27
     */
    Infos.LotControlJobHistoryGetDROut lotControlJobHistoryGetDR(Infos.ObjCommon strObjCommonIn, Infos.LotControlJobHistoryGetDRIn strLotControlJobHistory_GetDR_in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/22 14:00
     * @param flowBatchData
     * @param objCommon -
     * @return com.fa.cim.dto.Results.FloatingBatchListInqResult
     */
    Results.FloatingBatchListInqResult lotFillInTxDSQ002DR(Results.FloatingBatchListInqResult flowBatchData, Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjMonitorLotSTBInfoGetOut>
     * @author Jerry
     * @date 2018/10/29 16:06
     */
    Outputs.ObjMonitorLotSTBInfoGetOut monitorLotSTBInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/1/16 15:06
     * @param objCommon
     * @param objLotWIPInfoResetIn -
     * @return com.fa.cim.dto.Outputs.ObjLotWIPInfoResetOut
     */
    Outputs.ObjLotWIPInfoResetOut lotWIPInfoReset(Infos.ObjCommon objCommon, Inputs.ObjLotWIPInfoResetIn objLotWIPInfoResetIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/20 17:58
     * @param objCommon
     * @param lotID
     * @param routeID
     * @param operationNumber
     * @param strFutureReworkDetailInfo -
     * @return com.fa.cim.dto.Outputs.ObjLotFutureReworkInfoCheckOut
     */
    Outputs.ObjLotFutureReworkInfoCheckOut lotFutureReworkInfoCheck(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier routeID, String operationNumber, Infos.FutureReworkDetailInfo strFutureReworkDetailInfo);

    /**
     * Do Check of Lot related to BondingGroup.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/17 13:17
     */
    void lotCheckConditionForBondingGroup(Infos.ObjCommon objCommon, String action, List<ObjectIdentifier> bondingLotIDSeq);


    /**
     * This function inquires information of specified bonding group.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/21 16:52
     */
    Outputs.ObjLotStackedWaferInfoGetDROut lotStackedWaferInfoGetDR(Infos.ObjCommon objCommon, Inputs.ObjLotStackedWaferInfoGetDRIn lotStackedWaferInfoGetDRIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/4/26 15:01
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.dto.Outputs.ObjLotProductProcessOperationInfoGetOut
     */
    Outputs.ObjLotProductProcessOperationInfoGetOut lotProductProcessOperationInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * Get all of Hold Record by specified LotID.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/23 16:24
     */
    List<Infos.LotHoldRecordInfo> lotHoldRecordGetDR(String lotID);

    /**
     * Do Check of Lot related to WaferStacking.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/24 17:11
     */
    void lotCheckConditionForWaferStacking(Infos.ObjCommon objCommon, List<ObjectIdentifier> stackingLotIDSeq);

    /**
     * get the list of cassette of the lot
     *
     * @param objCommon
     * @param lotID
     * @author Yuri
     * @return {@link List} {@link ObjectIdentifier}
     * @date 2020/4/20 13:53
     */
    List<ObjectIdentifier> lotCassetteListGet (Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * Do Check of Lot related to cancel WaferStacking.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/27 14:06
     */
    void lotCheckConditionForWaferStackingCancel(Infos.ObjCommon objCommon, List<ObjectIdentifier> baseLotIDSeq, List<ObjectIdentifier> topLotIDSeq);

    /**
     * This function claims wafer stacking cancel requests for specified Top Lot(s).
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/27 14:08
     */
    void lotWaferStackMake(Infos.ObjCommon objCommon, List<ObjectIdentifier> topLotIDSeq);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/4/27 14:21
     * @param objCommon
     * @param strStartCassette
     * @param strAPCRecipeParameterResponse -
     * @return java.util.List<com.fa.cim.dto.Infos.StartCassette>
     */
    List<Infos.StartCassette> lotAPCRecipeParameterResponse(Infos.ObjCommon objCommon, List<Infos.StartCassette> strStartCassette, List<Infos.APCRecipeParameterResponse> strAPCRecipeParameterResponse);

    void lotWaferStackCancel(Infos.ObjCommon objCommon, List<ObjectIdentifier> baseLotIDSeq, List<ObjectIdentifier> topLotIDSeq, List<Infos.StackedWaferInfo> strStackedWaferInfoSeq);

    /**
     * description: get RunCard/Psm/Doc history infomation
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/8/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/8/20 16:24
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Infos.RunCardHistoryGetDROut runCardPsmDocHisotryGetDR(Infos.ObjCommon strObjCommonIn, Infos.RunCardHistoryGetDRIn runCardHistoryGetDRIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/11/30/030 16:05
     */
    List<ObjectIdentifier> allLotTypeGet();

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Jerry_Huang
     * @date 2021/01/14/030 16:05
     */
    ObjectIdentifier lotIDAssignByDef(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Jerry_Huang
     * @date 2021/01/14/030 16:05
     */
    ObjectIdentifier lotIDAssignbyEnv(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID);

    /**
     * description: update lot hold
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common
     * @param lotID       - lot Id
     * @param holdReqList - hold lot
     * @author YJ
     * @date 2021/1/22 0022 15:08
     */
    List<Infos.HoldHistory> lotHoldDepartmentChange(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.LotHoldReq> holdReqList);

    /**
     * description: lot future hold change
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon - common params
     * @param params    - future hold params
     * @return com.fa.cim.dto.Infos.FutureHoldHistory
     * @author YJ
     * @date 2021/1/27 0022 15:08
     */
    Infos.FutureHoldHistory lotFutureHoldChangeMakeEntry(Infos.ObjCommon objCommon, Params.FutureHoldReqParams params);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/2/22 20:02                      Decade                Create
    *
    * @author Nyx
    * @date 2021/2/22 20:02
    * @param null -
    * @return
    */
    void lotNpwUsageRecycleLimitUpdate(LotNpwUsageRecycleLimitUpdateParams params, Infos.ObjCommon objCommon);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/2/24 15:54                      Decade                Create
    *
    * @author Decade
    * @date 2021/2/24 15:54
    * @param null -
    * @return
    */
    void lotNpwUsageRecycleCountUpdate(LotNpwUsageRecycleCountUpdateParams params, Infos.ObjCommon objCommon);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/2/27 17:46                      Decade                Create
    *
    * @author Decade
    * @date 2021/2/27 17:46
    * @param null -
    * @return
    */
    void lotNpwStbUpdate(Infos.ObjCommon objCommon, ObjectIdentifier createdLotID, List<LotStbUsageRecycleLimitParams> lotStbUsageRecycleLimitParamsList, ObjectIdentifier productID);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/1 17:07                      Decade                Create
    *
    * @author Decade
    * @date 2021/3/1 17:07
    * @param null -
    * @return
    */
    boolean checkNPWLotSkipNeedReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    boolean checkLotMoveNextRequired(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    void setLotMoveNextRequired(Infos.ObjCommon objCommon, ObjectIdentifier lotID, boolean requiredFlag);

    /**
     * 检查是否存在Terminated状态的Lot
     * @param lotIDs
     * @version 0.1
     * @author Grant
     * @date 2021/7/9
     */
    void checkIsTerminated(List<ObjectIdentifier> lotIDs);

    /**
     * 执行Lot Terminate数据处理
     * @param objCommon
     * @param lot
     * @version 0.1
     * @author Grant
     * @date 2021/7/9
     */
    void lotTerminate(Infos.ObjCommon objCommon, CimLot lot);

    /**
     * 执行Lot Terminate Cancel数据处理
     * @param objCommon
     * @param lot
     * @version 0.1
     * @author Grant
     * @date 2021/7/9
     */
    void lotTerminateCancel(Infos.ObjCommon objCommon, CimLot lot);

    /**
     * 获取Lot Dispatch Readiness状态获取 {@link DispatchReadinessState}
     *
     * @param objCommon objCommon
     * @param lotID lotID
     * @return {@link DispatchReadinessState}
     */
    DispatchReadinessState lotDispatchReadinessGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    void lotDispatchReadinessSet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, DispatchReadinessState state);

    /**
    * description: 通过lot统计wafer数量
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/14 18:07 Ly Create
    *
    * @author Ly
    * @date 2021/7/14 18:07
    * @param  ‐
    * @return
    */
    int countQuantityWaferBylot(Infos.ObjCommon objCommon, String lotID,ObjectIdentifier aChildCassetteID);


    void lotSTBCheck(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID,
                     Infos.NewLotAttributes newLotAttributes);

    /**
     * description:  通过monitor group id 查询monitor lot id
     * change history:
     * date             defect             person             comments
     * ----------------------------------------------------------------------------------------------------------------
     * 2021/7/28 0028 15:11                        YJ                Create
     *
     * @author YJ
     * @date 2021/7/28 0028 15:11
     * @param monitorGroupId - monitor group id
     * @param objCommon - common
     * @return monitor lot results
     */
    List<LotMonitorGroupResults.MonitorLotDataCollectionQueryResults> monitorGroupLotGet(Infos.ObjCommon objCommon,
                                                                                   String monitorGroupId);

    /**
    * description:获取lotStatus
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/10/14 18:56 Ly Create
    *
    * @author Ly
    * @date 2021/10/14 18:56
    * @param  ‐
    * @return
    */
    String getLotStatus(Infos.ObjCommon objCommon,ObjectIdentifier lotID);

}
