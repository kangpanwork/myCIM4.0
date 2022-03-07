package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.*;
import com.fa.cim.eqp.IBFurnaceEQPBatchInfo;
import com.fa.cim.eqp.carrierout.CarrierOutPortInfo;
import com.fa.cim.eqp.carrierout.CarrierOutPortResults;
import com.fa.cim.layoutrecipe.LayoutRecipeParams;
import com.fa.cim.layoutrecipe.LayoutRecipeResults;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;

import java.util.List;

/**
 * description:
 * This file use to define the IEquipmentMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/6/21 10:30
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEquipmentMethod {

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strEquipmentListInfoGetDRIn
     * @return com.fa.cim.dto.RetCode<Infos.EquipmentListInfoGetDROut>
     * @author Ho
     * @date 2019/2/13 15:19:25
     */
    Infos.EquipmentListInfoGetDROut equipmentListInfoGetDR(Infos.ObjCommon strObjCommonIn,Infos.EquipmentListInfoGetDRIn strEquipmentListInfoGetDRIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strEquipmentHistory_GetDR_in
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.EquipmentHistoryGetDROut>
     * @exception
     * @author Ho
     * @date 2019/4/29 15:55
     */
    Infos.EquipmentHistoryGetDROut equipmentHistoryGetDR(Infos.ObjCommon strObjCommonIn,
                                                                  Infos.EquipmentHistoryGetDRIn strEquipmentHistory_GetDR_in);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strEquipmentMonitorJobLotHistory_GetDR_in
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.EquipmentMonitorJobLotHistoryGetDROut>
     * @exception
     * @author Ho
     * @date 2019/4/29 17:23
     */
    Infos.EquipmentMonitorJobLotHistoryGetDROut equipmentMonitorJobLotHistoryGetDR(Infos.ObjCommon strObjCommonIn, Infos.EquipmentMonitorJobLotHistoryGetDRIn strEquipmentMonitorJobLotHistory_GetDR_in);

    /**
     * description:
     * 1)Retrieve Online Mode of eqp
     * 2)Retrieve 1st post resource from eqp and retrieve operation mode from it.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon - objCommon
     * @param equipmentID  - equipmentID
     * @return com.fa.cim.pojo.obj.Outputs.ObjEquipmentOnlineModeOut
     * @author Bear
     * @date 2018/6/22
     */
    String equipmentOnlineModeGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @return com.fa.cim.dto.RetCode<Results.FlowBatchLotSelectionInqResult>
     * @author Ho
     * @date 2019/1/7 13:26:29
     */
    Results.FlowBatchLotSelectionInqResult equipmentFlowBatchWaitLotsGetDR(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @return com.fa.cim.dto.RetCode<java.util.List<Infos.FlowBatchCandidateLot>>
     * @author Ho
     * @date 2019/1/7 15:41:26
     */
    List<Infos.FlowBatchCandidateLot> equipmentFlowBatchCandidateLotsGetDR(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param equipmentID
     * @param portID
     * @param cassetteID
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/11/14 14:35:17
     */
    void equipmentLoadLotDeleteForInternalBuffer(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier portID, ObjectIdentifier cassetteID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param equipmentID
     * @param cassetteID
     * @param portID
     * @param loadPurposeType
     * @param controlJobID
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/11/12 18:07:52
     */
    void equipmentAllocatedMaterialAdd(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier portID, String loadPurposeType, ObjectIdentifier controlJobID);

    void equipmentAllocatedMaterialAddForDrbIB(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier portID, String loadPurposeType, ObjectIdentifier controlJobID);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param equipmentID
     * @param bEqpInternalBufferInfo
     * @param strEqpInternalBufferInfoSeq
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.EquipmentShelfSpaceForInternalBufferGet>
     * @author Ho
     * @date 2018/11/2 15:50:48
     */
    Infos.EquipmentShelfSpaceForInternalBufferGet equipmentShelfSpaceForInternalBufferGet(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, Boolean bEqpInternalBufferInfo, List<Infos.EqpInternalBufferInfo> strEqpInternalBufferInfoSeq);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.EqpInternalBufferInfo>>
     * @author Ho
     * @date 2018/11/1 17:41:34
     */
    List<Infos.EqpInternalBufferInfo> equipmentInternalBufferInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param operation
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/11/1 11:07:35
     */
    void eqpMonitorJobLotUpdate(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String operation);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param equipmentID
     * @param controlJobID
     * @param strStartCassette
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/11/1 10:13:12
     */
    void equipmentProcessingLotAddForInternalBuffer(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> strStartCassette);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param eqpMonitorID
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.EqpMonitorDetailInfo>>
     * @author Ho
     * @date 2018/11/1 10:40:18
     */
    List<Infos.EqpMonitorDetailInfo> eqpMonitorInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier eqpMonitorID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/24 9:44
     * @param objCommon
     * @param eqpMonitorNextExecutionTimeCalculateIn -
     * @return com.fa.cim.dto.Results.EqpMonitorNextExecutionTimeCalculateResult
     */
    Results.EqpMonitorNextExecutionTimeCalculateResult eqpMonitorNextExecutionTimeCalculate(Infos.ObjCommon objCommon, Infos.EqpMonitorNextExecutionTimeCalculateIn eqpMonitorNextExecutionTimeCalculateIn);

    /**
     * availableEquipment_GetByModeAndStatusDR
     *
     * @param objCommon
     * @return
     * @author ho
     */
    List<ObjectIdentifier> availableEquipmentGetByModeAndStatusDR(Infos.ObjCommon objCommon);

    /**
     * equipment_FillInTxEQQ003DR
     *
     * @param objCommon
     * @param eqpListByBayInqInParm
     * @return
     * @author ho
     */
    Results.EqpListByBayInqResult equipmentFillInTxEQQ003DR(Infos.ObjCommon objCommon, Params.EqpListByBayInqInParm eqpListByBayInqInParm);

    /**
     * equipment_state_GetDR
     *
     * @param objCommon
     * @param equipmentID
     * @return
     * @author Ho
     */
    ObjectIdentifier equipmentStateGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:equipmentContainer_info_GetDR
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   -
     * @param equipmentID -
     * @return com.fa.cim.pojo.Outputs.ObjEquipmentContainerInfoGetDROut
     * @author Panda
     * @date 2018/4/19
     * @see IEquipmentContainerMethod#equipmentContainerInfoGet(com.fa.cim.dto.Infos.ObjCommon, com.fa.cim.common.support.ObjectIdentifier)
     */
    @Deprecated
    Outputs.ObjEquipmentContainerInfoGetOut equipmentContainerInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common object
     * @param equipmentID - eqp ID
     * @return Outputs.ObjEquipmentContainerInfoGetDROut
     * @author Paladin
     * @since 2018/7/4 13:33
     */
    Infos.EqpContainerInfo equipmentContainerInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p> get equipmentStatusInfo by eqp id </p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param equipmentID -
     * @return retCode
     * @author PlayBoy
     * @since 2018/6/26
     */
    Infos.EqpStatusInfo equipmentStatusInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p> This function get all in processing jobs in eqp </p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentId
     * @return RetCode
     * @author PlayBoy
     * @date 2018/6/26
     */
    List<ObjectIdentifier> equipmentInProcessingControlJobGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentId);

    /**
     * description:
     * <p> update backup state of eqp </p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param equipmentId -
     * @param equipmentStatusCode -
     * @author PlayBoy
     * @since 2018/6/26
     */
    void equipmentBackupStateUpdate(Infos.ObjCommon objCommon, ObjectIdentifier equipmentId, ObjectIdentifier equipmentStatusCode);

    /**
     * description:
     * <p>get eqp Backup State</p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param equipmentId -
     * @return Infos.EqpBackupState
     * @author PlayBoy
     * @since 2018/6/26
     */
    Infos.EqpBackupState equipmentBackupStateGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentId);

    /**
     * description:
     * <p>get eqp Backup State</p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @return RetCode
     * @author Paladin
     * @date 2018/7/09
     */
    Infos.EqpBrInfo equipmentBRInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p>convert eqp state</p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn -
     * @param equipmentId -
     * @param equipmentStatusCode -
     * @return RetCode
     * @author PlayBoy
     * @since 2018/6/26
     */
    Outputs.ObjEquipmentStateConvertOut equipmentStateConvert(Infos.ObjCommon objCommonIn,
                                                              ObjectIdentifier equipmentId,
                                                              ObjectIdentifier equipmentStatusCode);

    /**
     * 查询指定状态在指定设备的加工Lot中是否有符合条件的其他状态转换
     * @param equipmentID 设备ID
     * @param eqpStatusCode 状态
     * @return {@link Outputs.ObjEquipmentStateConvertOut}
     * @version 0.1
     * @author Grant
     * @date 2021/7/20
     */
    Outputs.ObjEquipmentStateConvertOut equipmentStateConvertV2(ObjectIdentifier equipmentID,
                                                                ObjectIdentifier eqpStatusCode);

    /**
     * description:
     * <p>check Transition for eqp Current State</p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn -
     * @param equipmentId -
     * @param convertedStatusCode -
     * @param checkManufacturingFlag -
     * @author PlayBoy
     * @since 2018/6/26
     */
    void equipmentCurrentStateCheckTransition(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentId,
                                              ObjectIdentifier convertedStatusCode, boolean checkManufacturingFlag);

    /**
     * 检查设备将要修改的转换状态是否有转换规则配置
     * @param equipmentID 设备ID
     * @param eqpToState 将要转换的E10子状态
     * @param checkManufacturingFlag 是否验证加工的标记
     * @version 0.1
     * @author Grant
     * @date 2021/7/21
     */
    void equipmentCurrentStateCheckTransitionV2(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
                                                ObjectIdentifier eqpToState, boolean checkManufacturingFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/7/21                            Grant               优化实现内容
     * @param objCommon -
     * @param equipmentID -
     * @param eqpToState -
     * @return RetCode
     * @author PlayBoy
     * @since 2018/6/26
     */
    Outputs.ObjEquipmentCurrentStateChangeOut equipmentCurrentStateChange(Infos.ObjCommon objCommon,
                                                                          ObjectIdentifier equipmentID,
                                                                          ObjectIdentifier eqpToState);

    /**
     * description:
     * <p>Transaction ID and eqp Category Consistency Check</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common object
     * @param equipmentID - eqp ID
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/7/4 13:33
     */
    void equipmentCategoryVsTxIDCheckCombination(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p>The data will be filled by "DartyRead"</p>
     * the workflow:
     * step1) find sequence of PortID by equipmentID
     * step2) equipmentID -> List<controljob>
     * step3) controlJobID -> List<ControlJobToCassette>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common object
     * @param equipmentID -
     * @return com.fa.cim.pojo.RetCode<Outputs.ObjEquipmentPortInfoGetDROut>
     * @author Bear
     * @date 2018/7/10 11:11
     */
    Infos.EqpPortInfo equipmentPortInfoForInternalBufferGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param equipmentID
     * @param loadedPortID
     * @param cassetteID
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/11/16 12:10:14
     */
    void equipmentContainedMaterialAdd(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier loadedPortID, ObjectIdentifier cassetteID);


    /**
     * description:
     * <p>query equipment for status related data</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common object
     * @param equipmentID - eqp ID
     * @return Outputs.ObjEquipmentStatusInfoGetDROut
     * @author Paladin
     * @date 2018/7/4 13:33
     */
    Infos.EqpStatusInfo equipmentStatusInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p>query PM info after check equipment current state and e10 state</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common object
     * @param equipmentID - eqp ID
     * @return Outputs.ObjEquipmentPMInfoGetDROut
     * @author Paladin
     * @date 2018/7/4 13:33
     */
    Infos.EqpPMInfo equipmentPMInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param strEqpChamberStatus
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     * @author Ho
     * @date 2018/10/10 10:40:29
     */
    ObjectIdentifier equipmentCurrentStateBecomeWhat(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.EqpChamberStatus> strEqpChamberStatus);

    /**
     * 通过所有腔体chamber的规则来获取指定设备当前应变更的状态
     * @param equipmentID 设备标识
     * @param chamberStatuses 腔体当前状态集合
     * @return 设备应变更的新E10子状态标识
     * @version 0.1
     * @author Grant
     * @date 2021/7/24
     */
    ObjectIdentifier getEquipmentStateByChambers(ObjectIdentifier equipmentID,
                                                 List<Infos.EqpChamberStatus> chamberStatuses);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param lotID
     * @param cassetteID
     * @param durableID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.EqpForAutoTransferInqResult>
     * @author Ho
     * @date 2018/10/11 14:24:19
     */
    List<ObjectIdentifier> availableEquipmentGetForDeliveryReqDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier lotID, ObjectIdentifier cassetteID, ObjectIdentifier durableID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param strEqpPortStatus
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.EqpPortInfoOrderByGroup>
     * @author Ho
     * @date 2018/10/12 10:36:03
     */
    Infos.EqpPortInfoOrderByGroup equipmentPortInfoSortByGroup(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.EqpPortStatus> strEqpPortStatus);

    /**
     * description: equipment_operationMode_GetDR
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Ho
     * @date 2018/10/11 15:05:40
     */
    String equipmentOperationModeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/10/11 15:43:40
     */
    void equipmentCheckAvailDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon       - common object
     * @param equipmentID     - eqp ID
     * @return RetCode<Outputs.ObjEquipmentPortInfoGetDROut>
     * @author Paladin
     * @since 2018/7/4 13:33
     */
    Infos.EqpPortInfo equipmentPortInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.PortID>>
     * @author Ho
     * @date 2018/11/14 14:00:42
     */
    List<Infos.PortID> equipmentAllCassetteOnPortForInternalBufferGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p>query chamber data from equipment</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common object
     * @param equipmentID - eqp ID
     * @return Outputs.ObjEquipmentChamberInfoGetDROut
     * @author Paladin
     * @date 2018/7/4 13:33
     */
    Infos.EqpChamberInfo equipmentChamberInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common object
     * @param equipmentID - eqp ID
     * @return Outputs.ObjEquipmentStockerInfoGetDROut
     * @author Paladin
     * @date 2018/7/4 13:33
     */
    Infos.EqpStockerInfo equipmentStockerInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common object
     * @param equipmentID - eqp ID
     * @return Outputs.ObjEquipmentInprocessingControlJobInfoGetDROut
     * @author Paladin
     * @since 2018/7/4 13:33
     */
    List<Infos.EqpInprocessingControlJob> equipmentInprocessingControlJobInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common object
     * @param equipmentID - eqp ID
     * @return Outputs.ObjEquipmentInprocessingControlJobInfoGetDROut
     * @author Paladin
     * @date 2018/7/4 13:33
     */
    List<Infos.StartReservedControlJobInfo> equipmentReservedControlJobIDGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/14 17:17
     * @param objCommon
     * @param equipmentID -
     * @return java.util.List<com.fa.cim.dto.Infos.StartReservedControlJobInfo>
     */
    List<Infos.StartReservedControlJobInfo> equipmentReservedControlJobIDGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common object
     * @param equipmentID - eqp ID
     * @param chamberIDs  - chamber ID
     * @return Outputs.ObjEquipmentFillInTxEQQ004DROut
     * @author Paladin
     * @date 2018/7/4 13:33
     */
    List<Infos.EntityInhibitAttributes> equipmentFillInTxEQQ004(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<ObjectIdentifier> chamberIDs);

    /**
     * description: eqpMonitor_list_GetDR
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param objCommon -
     * @return com.fa.cim.pojo.Outputs.ObjEqpMonitorListGetDROut
     * @author panda
     * @date 2018/7/12 13:33
     */
    List<Infos.EqpMonitorDetailInfo> eqpMonitorListGetDR(Inputs.ObjEqpMonitorListGetDRIn objEqpMonitorListGetDRIn, Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/23 22:58
     * @param objCommon
     * @param eqpID -
     * @return com.fa.cim.dto.Outputs.ObjEquipmentBankIDGetOut
     */
    Outputs.ObjEquipmentBankIDGetOut equipmentBankIDGet(Infos.ObjCommon objCommon, ObjectIdentifier eqpID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param eqpID     -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/7/19 16:24
     */
    void equipmentCheckAvail(Infos.ObjCommon objCommon, ObjectIdentifier eqpID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param eqpID     -
     * @return com.fa.cim.pojo.Outputs.ObjEquipmentProcessBatchConditionGetOut
     * @author Bear
     * @date 2018/7/19 17:47
     */
    Outputs.ObjEquipmentProcessBatchConditionGetOut equipmentProcessBatchConditionGet(Infos.ObjCommon objCommon, ObjectIdentifier eqpID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param cassetteID
     * @param portID
     * @param loadPurposeType
     * @param loadingVerifiedLots
     * @return
     * @author PlayBoy
     * @date 2018/7/20
     */
    void equipmentLoadLotAdd(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier portID,
                                                       String loadPurposeType, List<Infos.LoadingVerifiedLot> loadingVerifiedLots);

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
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/10/31 15:52:40
     */
    void equipmentPortStateCheckForOpeStartForInternalBuffer(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon        objCommon
     * @param equipmentID      equipmentID
     * @param portID           portID
     * @param dispatchState    dispatchState
     * @param loadLotID        loadLotID
     * @param loadCassetteID   loadCassetteID
     * @param unloadLotID      unloadLotID
     * @param unloadCassetteID unloadCassetteID
     * @author PlayBoy
     * @since 2018/7/23
     */
    void equipmentDispatchStateChange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, String dispatchState,
                                                 ObjectIdentifier loadLotID, ObjectIdentifier loadCassetteID, ObjectIdentifier unloadLotID, ObjectIdentifier unloadCassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon    objCommon
     * @param equipmentID  equipmentID
     * @param controlJobID controlJobID
     * @param cassetteID   cassetteID
     * @param portID       portID
     * @author PlayBoy
     * @date 2018/7/23
     */
    void equipmentContainerPositionInfoUpdateForLoading(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, ObjectIdentifier cassetteID, ObjectIdentifier portID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn objCommonIn
     * @param equipmentID equipmentID
     * @param portID      portID
     * @param cassetteID  cassetteID
     * @author Nyx
     * @date 2018/7/23
     */
    void equipmentPortStateCheckForUnloading(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier portID, ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn objCommonIn
     * @param equipmentID equipmentID
     * @param portID      portID
     * @param cassetteID  cassetteID
     * @author Nyx
     * @date 2018/7/23
     */
    void equipmentLoadLotDelete(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier portID, ObjectIdentifier cassetteID);

    /**
     * description:
     * <p>Check eqp's availability for specified lot.
     * 1. Get eqp's current state code.
     * 2. Check state code as follws.
     * - equipmentAvailableFlag
     * - conditionalAvailableFlag
     * ||  conditionFlag
     * ||------------------
     * availFlag  ||  TRUE  |  FALSE
     * ============++========+=========
     * TRUE     ||   *3   |   *1
     * ------------++--------+---------
     * FALSE    ||   *2   |   *2
     * <p>
     * *1 : return RC_OK.
     * *2 : return RC_EQP_NOT_AVAILSTAT.
     * *3 : if subLotType of specified lot is contained into the conditional list, return RC_OK.
     * if it is not contained, return RC_EQP_NOT_AVAILSTAT_FOR_LOT.
     *
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param lotIDList   -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/7/24 15:06
     */
    void equipmentCheckAvailForLot(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<ObjectIdentifier> lotIDList);

    /**
     * description:
     * Check eqp port for OpeStart
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param equipmentID       equipmentID
     * @param portGroupID       portGroupID
     * @param startCassetteList startCassetteList
     * @author PlayBoy
     * @date 2018/7/27
     */
    void equipmentPortStateCheckForOpeStart(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param lotIDList
     * @param startCassetteList -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/7/30 17:12
     */
    void equipmentCheckInhibitForLotWithMachineRecipe(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<ObjectIdentifier> lotIDList, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * <p>
     * Check Process for Process durable
     * The following conditions are checked by this object
     * <p>
     * 1. Whether eqp requires process durable or not
     * If no-need, return OK;
     * <p>
     * 2. At least one of reticle / fixture for each reticleGroup /
     * fixtureGroup is in the eqp or not.
     * Even if required reticle is in the eqp, its status must
     * be _Available or _InUse.
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/7/27
     */
    void equipmentProcessDurableRequiredFlagGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p>equipment_reservedControlJobID_Clear<p/>
     * <p>Clear specified reservedControlJobID of eqp.</p>
     * <p>If eqp does not have specified process group ID, return "RC_EQP_NOT_RESVED_FOR_CTRLJOB".</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon    objCommon
     * @param equipmentID  equipmentID
     * @param controlJobID controlJobID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/7/30
     */
    void equipmentReservedControlJobIDClear(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID);

    /**
     * description:
     * <p>equipment_processingLot_Add</p>
     * <p>Add specified lots into eqp's processing-lot</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param equipmentID       equipmentID
     * @param controlJobID      controlJobID
     * @param startCassetteList startCassetteList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/7/30
     */
    void equipmentProcessingLotAdd(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * equipment_currentState_CheckToManufacturing
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   objCommon
     * @param equipmentID equipmentID
     * @return boolean manufacturingStateChangeableFlag
     * @author PlayBoy
     * @date 2018/7/30
     */
    Boolean equipmentCurrentStateCheckToManufacturing(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p>
     * If eqp has any in-process lots, get default Standby pmcmg state and return success.<br/>
     * If eqp dies not have in-process lot, get default Productive pmcmg state and return success.
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   objCommon
     * @param equipmentID equipmentID
     * @return String equipmentRecoverStateCode
     * @author PlayBoy
     * @since 2018/7/30
     */
    ObjectIdentifier equipmentRecoverStateGetManufacturing(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p>equipment_usageCount_Increment<br/>
     * Increase the usage count of specified eqp.<br/>
     * If loadPurposeType is empty cassette (SP_LoadPurposeType_EmptyCassette),it is not required to do anything.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param equipmentID       equipmentID
     * @param startCassetteList startCassetteList
     * @author PlayBoy
     * @date 2018/7/30
     */
    void equipmentUsageCountIncrement(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon      objCommon
     * @param equipmentID    equipmentID
     * @param allInquiryFlag allInquiryFlag
     * @return EqpStatusSelectionInqResult
     * @author PlayBoy
     * @since 2018/8/2
     */
    Results.EqpStatusSelectionInqResult equipmentFillInTxEQQ002DR(Infos.ObjCommon objCommon,
                                                                  ObjectIdentifier equipmentID,
                                                                  boolean allInquiryFlag);

    Results.EqpStatusSelectionInqResult equipmentFillInTxEQQ002DRV2(Infos.ObjCommon objCommon,
                                                                    ObjectIdentifier equipmentID,
                                                                    boolean allInquiryFlag);

    /**
     * description:
     * <p>Check eqp and cassette's flow batching status StartReservation and OpeStart.
     * The following conditions are checked.
     * 1. whether in-parm's eqp has reserved flowBatchID or not
     * fill  -> all of flowBatch member and in-parm's lot must be same perfectly.
     * blank -> no check
     * <p>
     * 2. whether lot is in flowBatch section or not
     * in    -> lot must have flowBatchID, and flowBatch must have reserved equipmentID.
     * if lot is on target operation, flowBatch's reserved equipmentID and in-parm's equipmentID must be same.
     * out   -> no check
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param operationStartIn -
     * @return com.fa.cim.pojo.RetCode<Outputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartOut>
     * @author Bear
     * @date 2018/8/6 11:08
     */
    ObjectIdentifier equipmentLotCheckFlowBatchConditionForOpeStart(Infos.ObjCommon objCommon, Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn operationStartIn);


    /**
     * description:
     * <p>
     * Set specified controlJob to the eqp as startReservedControlJob.
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param controlJobID -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/8/6 17:19
     */
    void equipmentReservedControlJobIDSet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID);

    /**
     * description:
     * Get equipmentContainer position objects by controlJobID
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon    objCommon
     * @param equipmentID  equipmentID
     * @param controlJobID controlJobID
     * @param keyCategory  keyCategory
     * @return RetCode<Infos.EqpContainerPositionInfo>
     * @author PlayBoy
     * @date 2018/8/8
     * @see IEquipmentContainerPositionMethod#equipmentContainerPositionInfoGet(com.fa.cim.dto.Infos.ObjCommon, com.fa.cim.dto.Inputs.ObjEquipmentContainerPositionInfoGetIn)
     */
    @Deprecated
    Infos.EqpContainerPositionInfo equipmentContainerPositionInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, String keyCategory);

    /**
     * equipmentContainer_wafer_Retrieve
     *
     * @param objCommon
     * @param equipmentID
     * @param controlJobID
     * @param processJobID
     * @param strSlmSlotMapSeq
     * @return
     * @author ho
     */
    void equipmentContainerWaferRetrieve(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, ObjectIdentifier processJobID, List<Infos.SlmSlotMap> strSlmSlotMapSeq);


    /**
     * description:
     * Update eqp container position for operationCancel
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon    objCommon
     * @param equipmentID  equipmentID
     * @param controlJobID controlJobID
     * @return RetCode<Object>
     * @author PlayBoy
     * @date 2018/8/8
     */
    void equipmentContainerPositionUpdateForOpeStartCancel(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID);

    /**
     * description:
     * check eqp condition for ope start cancel
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param equipmentID       equipmentID
     * @param startCassetteList startCassetteList
     * @return RetCode<Object>
     * @author PlayBoy
     * @date 2018/8/8
     */
    void equipmentCheckConditionForOpeStartCancel(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * Check eqp port for OpeStartCancel
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param equipmentID       equipmentID
     * @param startCassetteList startCassetteList
     * @return RetCode<Object>
     * @author PlayBoy
     * @date 2018/8/8
     */
    void equipmentPortStateCheckForOpeStartCancel(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * Delete specified lots from eqp's processing-lot
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param equipmentID       equipmentID
     * @param startCassetteList startCassetteList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/8/10
     */
    void equipmentProcessingLotDelete(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param equipmentID       equipmentID
     * @param startCassetteList startCassetteList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/8/10
     */
    void equipmentUsageCountDecrement(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param portID
     * @param cassetteID
     * @param loadPurposeType -
     * @return com.fa.cim.dto.RetCode
     * @author Jerry
     * @date 2018/8/19 10:13
     */
    void equipmenCheckConditionForLoading(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, ObjectIdentifier cassetteID, String loadPurposeType);


    /**
     * description:Get EqpMonitor job information from object. If eqpMonitorJobID isn't specified, return all EqpMonitor job for EqpMonitor.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpMonitorID
     * @param eqpMonitorJobID -
     * @return com.fa.cim.pojo.Outputs.ObjEqpMonitorJobInfoGetOut
     * @author Nyx
     * @date 2018/8/16 17:07
     */
    List<Infos.EqpMonitorJobInfo> eqpMonitorJobInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier eqpMonitorID, ObjectIdentifier eqpMonitorJobID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon      objCommon
     * @param equipmentId    equipmentID
     * @param startCassettes startCassettes
     * @return String control job id
     * @author Paladin
     * @date 2018/8/13
     */
    void equipmentPortStateCheckForOpeComp(Infos.ObjCommon objCommon, ObjectIdentifier equipmentId, List<Infos.StartCassette> startCassettes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   objCommon
     * @param equipmentId equipmentID
     * @return ObjEquipmentUsageLimitationCheckOut
     * @author Paladin
     * @date 2018/8/13
     */
    Outputs.ObjEquipmentUsageLimitationCheckOut equipmentUsageLimitationCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentId);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/10/29 15:17                       Jerry               Create
     *
     * @author Jerry
     * @date 2019/10/29 15:17
     * @param objCommon
     * @param equipmentID -
     * @return java.util.List<com.fa.cim.dto.Infos.CandidateChamberStatusInfo>
     */

    List<Infos.CandidateChamberStatusInfo> equipmentFillInTxEQQ015DR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    List<Infos.CandidateChamberStatusInfo> equipmentFillInTxEQQ015DRV2(Infos.ObjCommon objCommon,
                                                                       ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.pojo.Outputs.ObjEquipmentContainerPositionInfoGetByLotDROut
     * @author Jerry
     * @date 2018/9/26 10:13
     */
    List<Infos.EqpContainerPosition> equipmentContainerPositionInfoGetByLotDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentRelatedInfoUpdateForLotSplitOnEqpIn -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Jerry
     * @date 2018/9/29 9:24
     */
    void equipmentRelatedInfoUpdateForLotSplitOnEqp(Infos.ObjCommon objCommon, Inputs.EquipmentRelatedInfoUpdateForLotSplitOnEqpIn equipmentRelatedInfoUpdateForLotSplitOnEqpIn);

    /**
     * description:
     * <p> Check eqp TakeOutIn transfer enable.
     * 1. OM_XFER_TOTI_ENABLE_FLAG == 1 or not
     * 2. isTakeOutInTransferFlagOn == TRUE or not
     * equipment_TakeOutInMode_Check
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.dto.RetCode<java.lang.Long>
     * @author Bear
     * @date 2018/9/25 18:22
     */
    Long equipmentTakeOutInModeCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);


    /**
     * description:
     * <p> Check eqp condition for OpeStartCancel
     * The following conditions are checked.
     * - In-parm's controlJobID must be existing in eqp's reservedControlJob.
     * method: equipment_CheckConditionForStartReserveCancel()
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param controlJobID -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/9/26 18:47
     */
    void equipmentCheckConditionForStartReserveCancel(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param controlJobID
     * @param processJobID
     * @param strSlmSlotMapSeq -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Jerry
     * @date 2018/10/9 10:10
     * @see IEquipmentContainerMethod#equipmentContainerWaferStore(com.fa.cim.dto.Infos.ObjCommon, com.fa.cim.dto.Inputs.ObjEquipmentContainerWaferStoreIn)
     */
    @Deprecated
    void equipmentContainerWaferStore(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, String processJobID, List<Infos.SlmSlotMap> strSlmSlotMapSeq);

    /**
     * description:
     * <p>equipment_CheckConditionForEmptyCassetteEarlyOut<br/>
     * If empty cassette was in input cassette sequence, and if that cassette
     * attendended to Control Job, then this method delete cassette from Control Job</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   objCommon
     * @param equipmentID equipmentID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/8 12:28:42
     */
    void equipmentCheckConditionForEmptyCassetteEarlyOut(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param machineCassettes
     * @param nCastIdx
     * @param lotID              -
     * @return java.lang.Integer
     * @author Jerry
     * @date 2018/10/9 10:09
     */
    Integer INTGetMachineLotSequenceIndex(List<MachineDTO.MachineCassette> machineCassettes, Integer nCastIdx, String lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param machineCassettes
     * @param nCastIdx
     * @param lotID              -
     * @return java.lang.Integer
     * @author Jerry
     * @date 2018/10/9 10:09
     */
    Integer INTGetMachineLotPtrSequenceIndex(List<MachineDTO.MachineCassette> machineCassettes, Integer nCastIdx, String lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param cassetteID         -
     * @return java.lang.Integer
     * @author Jerry
     * @date 2018/10/9 10:09
     */
    Integer INTGetMachineCassetteSequenceIndex(List<MachineDTO.MachineCassette> machineCassettes, String cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param machineCassetteLots
     * @param lotID              -
     * @return java.lang.Integer
     * @author Jerry
     * @date 2018/10/9 10:09
     */
    Integer INTGetMachineCassetteLotSequenceIndex(List<MachineDTO.MachineCassetteLot> machineCassetteLots, String lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strPosStartCassetteInfoSeq
     * @param nCastIdx
     * @param lotID                      -
     * @return java.lang.Integer
     * @author Jerry
     * @date 2018/10/9 15:20
     */
    Integer INTGetStartLotSequenceIndex(List<ProductDTO.PosStartCassetteInfo> strPosStartCassetteInfoSeq, Integer nCastIdx, String lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param tmpLotInCassetteInfoSeq
     * @param lotID                   -
     * @return java.lang.Integer
     * @author Jerry
     * @date 2018/10/9 15:28
     */
    Integer INTGetLotInCassetteSequenceIndex(List<ProductDTO.PosLotInCassetteInfo> tmpLotInCassetteInfoSeq, String lotID);

    Integer INTGetStartCassetteSequenceIndex(List<ProductDTO.PosStartCassetteInfo> strPosStartCassetteInfoSeq, String cassetteID);

    /**
     * description:
     * <p>This function checks eqp process job level control related attributes<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon              objCommon
     * @param equipmentID            equipmentID
     * @param pjLevelCtrlFlagCheck   pjLevelCtrlFlagCheck
     * @param equipmentCategoryCheck equipmentCategoryCheck
     * @param onlineModeCheck        onlineModeCheck
     * @param multipleRecipeCheck    multipleRecipeCheck
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/12 10:48:07
     */
    void equipmentProcessJobLevelControlCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, boolean pjLevelCtrlFlagCheck, boolean equipmentCategoryCheck, boolean onlineModeCheck, boolean multipleRecipeCheck);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return RetCode<Outputs.ObjEquipmentIDGetDROut>
     * @author Wind
     * @date 2018/10/17 09:31
     */
    List<ObjectIdentifier> equipmentIDGetDR(Infos.ObjCommon objCommon);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/19                           Wind
     *
     * @param objCommon
     * @param equipmentID
     * @return RetCode<Outputs.ObjEquipmentNoteGetByEqpIDDROut>
     * @author Wind
     * @date 2018/10/19 18:17
     */
    List<Infos.EqpNote> equipmentNoteGetByEqpIDDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/23                              Wind
     *
     * @param objCommon
     * @param
     * @return RetCode<EquipmentNote>
     * @author Wind
     * @date 2018/10/23 11:05
     */
    void equipmentNoteMake(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String noteTitle, String equipmentNote);

    /**
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Paladin
     * @since 2018/10/24 16:10
     * @param objCommon -
     * @param equipmentID -
     */
    void equipmentUsageInfoReset(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Paladin
     * @date 2018/10/25 16:10
     * @param objCommon
     * @param equipmentID
     * @return String
     */
    String equipmentCategoryGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p>This function checks eqp process job level control related attributes<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon              objCommon
     * @param equipmentID            equipmentID
     * @return Infos.EqpBrInfoForInternalBuffer
     * @author Paladin
     * @since 2018/10/23 10:48:07
     */
    Outputs.ObjRawEquipmentStateTranslateOut rawEquipmentStateTranslate(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier rawEquipmentStatusCode);

    /**
     * description:
     * <p>This function checks eqp process job level control related attributes<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon              objCommon
     * @param equipmentID                  equipmentID
     * @param actualStatusCode       actualStatusCode
     * @param equipmentStatusCode    equipmentStatusCode
     * @param updateCurrentState    updateCurrentState
     * @return Infos.EqpBrInfoForInternalBuffer
     * @author Paladin
     * @since 2018/10/23 10:48:07
     */
    Outputs.ObjEquipmentCurrentStateChangeByAutoOut equipmentCurrentStateChangeByAuto(
            Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier actualStatusCode,
            ObjectIdentifier equipmentStatusCode, boolean updateCurrentState);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2018/10/30 10:11
     * @param objCommon
     * @param equipmentID
     * @param productLotIDs
     * @param productRequestID
     * @param strNewLotAttributes -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void equipmentLotSTBCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<ObjectIdentifier> productLotIDs, ObjectIdentifier productRequestID, Infos.NewLotAttributes strNewLotAttributes);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn Infos.ObjCommon
     * @param machineID ObjectIdentifier
     * @return Outputs.EquipmentMachineTypeCheckOut
     * @author Yuri
     * @date 2018/10/29 17:39:15
     */
    Boolean equipmentMachineTypeCheckDR(Infos.ObjCommon objCommonIn, ObjectIdentifier machineID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param startCassetteList startCassetteList
     * @param spcCheckLot       spcCheckLot
     * @return RetCode
     * @author PlayBoy
     * @date 2018/11/7 10:14:05
     */
    Results.MoveOutReqResult equipmentFillInTxTRC004(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList, List<Infos.SpcCheckLot> spcCheckLot);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/18 14:26
     * @param objCommonIn
     * @param eqpPortInfoOrderByGroup
     * @param eqpBrInfo
     * @param eqpPortInfo -
     * @return com.fa.cim.dto.Outputs.EquipmentTargetPortPickupOut
     */
	Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickup(Infos.ObjCommon objCommonIn,
                                                                            Infos.EqpPortInfoOrderByGroup eqpPortInfoOrderByGroup,
                                                                            Infos.EqpBrInfo eqpBrInfo,
                                                                            Infos.EqpPortInfo eqpPortInfo);

	/**
	 * description:
	 * change history:
	 * date             defect             person             comments
	 * ---------------------------------------------------------------------------------------------------------------------
	 * @author lightyh
	 * @date 2019/10/19 20:59
	 * @param objCommonIn
	 * @param equipmentIDs -
	 * @return java.util.List<com.fa.cim.dto.Infos.EqpAuto3SettingInfo>
	 */
    List<Infos.EqpAuto3SettingInfo> equipmentAuto3DispatchSettingListGetDR(Infos.ObjCommon objCommonIn, List<ObjectIdentifier> equipmentIDs);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon            objCommon
     * @param equipmentID equipmentID
     * @param startCassetteList          startCassetteList
     * @return RetCode
     * @author Paladin
     * @date 2018/11/12 10:14:05
     */
    void equipmentPortStateCheckForOpeStartCancelForInternalBuffer(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/13                          Wind
      * @param objCommon
      * @param equipmentID
      * @return RetCode<List<Infos.BufferResourceInfo>>s
      * @author Wind
      * @date 2018/11/13 13:05
      */
    List<Infos.BufferResourceInfo> equipmentBufferResourceInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/13                          Wind
      * @param objCommon
      * @param strEquipmentBufferResourceTypeChangeIn
      * @return RetCode<Object>
      * @author Wind
      * @date 2018/11/13 18:04
      */
    void equipmentBufferResourceTypeChange(Infos.ObjCommon objCommon, Inputs.ObjEquipmentBufferResourceTypeChangeIn strEquipmentBufferResourceTypeChangeIn);


    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/15                          Wind
      * @param objCommon
      * @param params
      * @return RetCode<Object>
      * @author Wind
      * @date 2018/11/15 9:11
      */
    void equipmentUnLoadCarrierAdd(Infos.ObjCommon objCommon, Params.CarrierMoveFromIBRptParams params);


    /**
     * description:
     * <p>get equipment and controlLotBank data from equipment</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return RetCode<List<Infos.EqpBrInfoForInternalBuffer>>
     * @author Paladin
     * @date 2018/11/14 19:07:05
     */
    Infos.EqpBrInfoForInternalBuffer equipmentBrInfoForInternalBufferGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return RetCode<Outputs.ObjEquipmentReticlePodPortInfoGetDROut>
     * @author Paladin
     * @date 2018/11/14 19:07:05
     */
    Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/2/13        TASK-1813          Nyx
     * @author Lin
     * @date 2018/11/28 15:19
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.dto.RetCode<java.util.List<Infos.MandPRecipeInfo>>
     */
    List<Infos.MandPRecipeInfo> equipmentMachineAndPhysicalRecipeIDGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/5                          Wind
      * @param objCommon
      * @param equipmentID
      * @return RetCode<Outputs.objEquipmentReserveFlowBatchIDGetOut>
      * @author Wind
      * @date 2018/12/5 15:22
      */
    Outputs.ObjEquipmentReserveFlowBatchIDGetOut equipmentReserveFlowBatchIDGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/7                          Wind
      * @param objCommon
      * @param equipmentID
      * @param lotIDs
      * @return RetCode<Object>
      * @author Wind
      * @date 2018/12/7 13:48
      */
    void equipmentCheckInhibitForLot(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<ObjectIdentifier> lotIDs);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         -
     * @param equipmentID       -
     * @param startCassetteList -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 12/17/2018 3:00 PM
     */
    void equipmentPortStateCheckForOpeCompForInternalBuffer(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         -
     * @param equipmentID       -
     * @param startCassetteList -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 12/17/2018 3:27 PM
     */
    void equipmentCheckConditionForOpeComp(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Lin
     * @date 2018/12/18 13:40
     * @param  * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjEquipmentinprocessingControlJobInfoGetout>
     */

    Infos.EqpInprocessingControlJobInfo equipmentInprocessingControlJobInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

     /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * -------------------------------------------------------------------------------------------------------------------
      * @param objCommon  objCommon
      * @param objEquipmentFlowBatchMaxCountChangeIn  objEquipmentFlowBatchMaxCountChangeIn
      * @return RetCode
      * @author ZQI
      * @date 2018/12/18 14:28:32
     */
    void equipmentFlowBatchMaxCountChange(Infos.ObjCommon objCommon, Inputs.ObjEquipmentFlowBatchMaxCountChangeIn objEquipmentFlowBatchMaxCountChangeIn);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/19                          Wind
      * @param objCommon
      * @param equipmentID
      * @param flowBatchID
      * @param operation
      * @return RetCode<Object>
      * @author Wind
      * @date 2018/12/19 15:29
      */
    void equipmentCheckConditionForFlowBatch(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier flowBatchID, String operation);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/21 15:30
     * @param objCommon
     * @param equipmentID
     * @param portID
     * @param cassetteID
     * @param loadPurposeType -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void equipmentCheckConditionForLoadingForInternalBuffer(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, ObjectIdentifier cassetteID, String loadPurposeType);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/26                          Wind
      * @param objCommon
      * @param objEqpMonitorWaferUsedCountUpdateIn
      * @return RetCode<List<Infos.EqpMonitorWaferUsedCount>>
      * @author Wind
      * @date 2018/12/26 16:42
      */
    List<Infos.EqpMonitorWaferUsedCount> eqpMonitorWaferUsedCountUpdate(Infos.ObjCommon objCommon, Inputs.ObjEqpMonitorWaferUsedCountUpdateIn objEqpMonitorWaferUsedCountUpdateIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param  objCommon
     * @param  equipmentID
     * @param  controlJobID
     * @return com.fa.cim.dto.RetCode
     * @author Scott
     * @date 2019/1/2 13:45:18
     */
    void equipmentStartReserveCancelForInternalBuffer(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID);
    void equipmentStartReserveCancelForDurableInternalBuffer(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID);
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Lin
     * @date 2019/1/2 15:00
     * @param  * @param objCommon
     * @param equipmentID
     * @param strPortOperationMode -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.ObjectIdentifier>
     */
    void equipmentPortOperationModeChange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.PortOperationMode> strPortOperationMode);
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Lin
     * @since 2019/1/7 17:19
     * @param  objCommon -
     * @param equipmentID -
     * @param strEqpInBuffer -
     * @param strEqpOutBuffer -
     * @return Results.ObjEquipmentportStateFillInTxEQR002InParmResult
     */
    List<Infos.EqpPortEventOnTCS> equipmentportStateFillInTxEQR002InParm(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,List<Infos.EqpInBuffer> strEqpInBuffer,List<Infos.EqpOutBuffer> strEqpOutBuffer);
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/2/20 15:16
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     */
    String equipmentMultiRecipeCapabilityGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/3/1 15:24
     * @param objCommon
     * @param equipmentID
     * @param startCassettes -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void equipmentPortStateCheckForStartReservationForInternalBuffer(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassettes);
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/3/14 15:42
     * @param objCommon
     * @param eqpMonitorJobListGetDRIn -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.bean.extension.Infos.EqpMonitorJobDetailInfo>>
     */
    List<Infos.EqpMonitorJobDetailInfo> eqpMonitorJobListGetDR(Infos.ObjCommon objCommon, Infos.EqpMonitorJobListGetDRIn eqpMonitorJobListGetDRIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/3/19 15:10
     * @param objCommon
     * @param eqpMonitorScheduleUpdateIn -
     * @return com.fa.cim.common.support.RetCode<java.lang.String>
     */
    String eqpMonitorScheduleUpdate(Infos.ObjCommon objCommon, Infos.EqpMonitorScheduleUpdateIn eqpMonitorScheduleUpdateIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/3/19 15:33
     * @param objCommon
     * @param eqpMonitorID -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.common.support.ObjectIdentifier>>
     */
    List<ObjectIdentifier> eqpMonitorEqpMonitorJobIDsGet(Infos.ObjCommon objCommon, ObjectIdentifier eqpMonitorID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/3/19 15:56
     * @param objCommon
     * @param eqpMonitorID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.common.support.ObjectIdentifier>
     */
    ObjectIdentifier eqpMonitorJobCreate(Infos.ObjCommon objCommon, ObjectIdentifier eqpMonitorID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/3/19 17:34
     * @param objCommon
     * @param eqpMonitorStatusChangeIn -
     * @return com.fa.cim.common.support.RetCode<java.lang.String>
     */
    String eqpMonitorStatusChange(Infos.ObjCommon objCommon, Infos.EqpMonitorStatusChangeIn eqpMonitorStatusChangeIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/3/20 9:53
     * @param objCommon
     * @param eqpMonitorActionExecuteIn -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void eqpMonitorActionExecute(Infos.ObjCommon objCommon, Infos.EqpMonitorActionExecuteIn eqpMonitorActionExecuteIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/3/20 14:09
     * @param objCommon
     * @param eqpMonitorJobStatusChangeIn -
     * @return com.fa.cim.common.support.RetCode<java.lang.String>
     */
    String eqpMonitorJobStatusChange(Infos.ObjCommon objCommon, Infos.EqpMonitorJobStatusChangeIn eqpMonitorJobStatusChangeIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/3/20 15:18
     * @param objCommon
     * @param eqpMonitorJobCompletedIn -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void eqpMonitorJobCompleted(Infos.ObjCommon objCommon, Infos.EqpMonitorJobCompletedIn eqpMonitorJobCompletedIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/3/21 13:13
     * @param objCommon
     * @param eqpMonitorInfoUpdateIn -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.common.support.ObjectIdentifier>
     */
    ObjectIdentifier eqpMonitorInfoUpdate(Infos.ObjCommon objCommon, Infos.EqpMonitorInfoUpdateIn eqpMonitorInfoUpdateIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/31 16:12
     * @param objCommon
     * @param machineID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjMachineTypeGetOut>
     */
    Outputs.ObjMachineTypeGetOut machineTypeGet(Infos.ObjCommon objCommon, ObjectIdentifier machineID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/6/13 17:37
     * @param objCommon -
     * @param equipmentID -
     * @param startCassette -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void equipmentLotCheckFlowBatchConditionForLoading(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, Infos.StartCassette startCassette);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/27 0:09
     * @param objCommon
     * @param equipmentIDs
     * @param lotID
     * @param checkInhibitFlag
     * @param checkMachineAvailabilityFlag -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjEquipmentPriorityOrderGetByLotAvailabilityOut>
     */
    Outputs.ObjEquipmentPriorityOrderGetByLotAvailabilityOut equipmentPriorityOrderGetByLotAvailability(Infos.ObjCommon objCommon, List<ObjectIdentifier> equipmentIDs,
                                                                                                                 ObjectIdentifier lotID, Boolean checkInhibitFlag, Boolean checkMachineAvailabilityFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/27 10:02
     * @param objCommon
     * @param machineStateID
     * @param lotID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjMachineStateAvailabilityCheckOut>
     */
    Boolean machineStateAvailabilityCheck(Infos.ObjCommon objCommon, ObjectIdentifier machineStateID, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/27 11:06
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjEquipmentPortOperationModeGetDROut>
     */
    List<Infos.PortOperationMode> equipmentPortOperationModeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/27 13:36
     * @param objCommon
     * @param cassetteLocationInfo
     * @param lotStatusInfo
     * @param eqpStatusList -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjEquipmentStockerOrderGetByLotStatusOut>
     */
    List<Infos.WhereNextEqpStatus> equipmentStockerOrderGetByLotStatus(Infos.ObjCommon objCommon, Infos.LotLocationInfo cassetteLocationInfo,
                                                                                                   Infos.LotStatusInfo lotStatusInfo, List<Infos.WhereNextEqpStatus> eqpStatusList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/27 14:10
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjEquipmentAllStockerGetByUTSPriorityDROut>
     */
    Outputs.ObjEquipmentAllStockerGetByUTSPriorityDROut equipmentAllStockerGetByUTSPriorityDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/27 22:27
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjEquipmentUTSInfoGetDROut>
     */
    Infos.EqpStockerInfo equipmentUTSInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Zack
     * @date 2019/06/28 10:10
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Results.OpeGuideInqResult>
     */
    Results.OpeGuideInqResult equipmentOperationProcedureFillInTxPLQ007DR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/7/1 15:53
     * @param objCommon
     * @param in
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void equipmentAndPortStateCheckForDurableOperation(Infos.ObjCommon objCommon, Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn in);

    void equipmentAndPortStateCheckForDurableOperationForInternalBuffer(Infos.ObjCommon objCommon, Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn in);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/3 22:18
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void equipmentCheckAvailForDurable(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/4 13:23
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjEquipmentReservedDurableControlJobIDGetDROut>
     */
    List<ObjectIdentifier> equipmentReservedDurableControlJobIDGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/4 15:10
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjEquipmentStoredReticleGetDROut>
     */
    List<Infos.StoredReticle> equipmentStoredReticleGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/5 15:23
     * @param objCommon
     * @param objEquipmentCheckInhibitForDurableWithMachineRecipeIn -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void equipmentCheckInhibitForDurableWithMachineRecipe(Infos.ObjCommon objCommon, Inputs.ObjEquipmentCheckInhibitForDurableWithMachineRecipeIn objEquipmentCheckInhibitForDurableWithMachineRecipeIn);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/3 14:33
     * @param objCommon
     * @param equipmentID -
     * @return boolean
     */
    boolean equipmentRecipeBodyManageFlagGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/7/10 13:05
     * @param objCommon
     * @param params
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.common.support.ObjectIdentifier>
     */
    void eqpMonitorJobLotReserve(Infos.ObjCommon objCommon, Params.AMJobLotReserveReqInParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * 2021/3/11        portStatus         Neyo               qiandao autoMoveInReserve Requirement: autoMoveInReserveFlag
     *                                                        1.when autoMoveInReserve autoMoveInRerserveFlag is True
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/8 15:06
     * @param objCommon
     * @param equipmentID
     * @param portGroupID
     * @param strStartCassette -
     * @return void
     */
    void equipmentPortStateCheckForStartReservation(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, List<Infos.StartCassette> strStartCassette,Boolean autoMoveInReserveFlag);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/12                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/12 16:45
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void equipmentMonitorCreationFlagGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);
	/**
     * description: Check Equipment Port for deleting Dis.Unload FOUPID.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/5 11:16
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void equipmentPortStateCheckForCassetteDelivery(Infos.ObjCommon objCommonIn, Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn deliveryIn);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/8                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/8 18:27
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Infos.EqpTargetPortInfo equipmentTargetPortPickupForTakeOutIn(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, Infos.EqpTargetPortInfo strEqpTargetPortInfo);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/18                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/18 15:51
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void equipmentPortStateCheckForTakeOutIn(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, List<Infos.StartCassette> strStartCassette);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/22 15:04
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickupForInternalBuffer(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, List<Infos.EqpInternalBufferInfo> strEqpInternalBufferInfos, List<Infos.EqpPortStatus> strPortInfos);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/7/24 15:00
     * @param objCommon
     * @param in
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void equipmentContainerPositionProcessJobStatusSet(Infos.ObjCommon objCommon, Inputs.EquipmentContainerPositionProcessJobStatusSetIn in);


    void equipmentLotInCassetteAdjust(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/9/16 16:47
     * @param equipmentID -
     * @return com.fa.cim.dto.Infos.EqpStockerInfo
     */
    Infos.EqpStockerInfo equipmentSLMUTSInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/9/16 16:58
     * @param objCommon
     * @param equipmentID -
     * @return java.util.List<com.fa.cim.common.support.ObjectIdentifier>
     */
    List<ObjectIdentifier> equipmentInprocessingDurableControlJobIDGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Lin
     * @date 2018/12/29 10:16
     * @param  * @param objCommon
     * @param equipmentID
     * @param strPortOperationMode -
     * @return com.fa.cim.dto.RetCode<Outputs.ObjEquipmentOperationModeCombinationCheck>
     */
    Outputs.ObjEquipmentOperationModeCombinationCheck equipmentOperationModeCombinationCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.PortOperationMode> strPortOperationMode);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/8 11:24
     * @param objCommon
     * @param eqipmentID -
     * @return java.lang.String
     */
    String equipmentGetTypeDR(Infos.ObjCommon objCommon, ObjectIdentifier eqipmentID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/10/30 13:50
     * @param objCommon -
     * @param in -
     */
    void equipmentUsageCountUpdateForPostProc(Infos.ObjCommon objCommon, Inputs.ObjEquipmentUsageCountUpdateForPostProcIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Neko
     * @since 2019/10/31 10:00
     * @param objCommon -
     * @param equipmentID -
     */
    List<Outputs.NPWReserveInfo> equipmentNPWReserveInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 11:01
     * @param objCommon
     * @param eqpMonitorID
     * @param eqpMonitorJobID -
     * @return java.lang.Long
     */
    Long eqpMonitorJobretryCountIncrement(Infos.ObjCommon objCommon, ObjectIdentifier eqpMonitorID, ObjectIdentifier eqpMonitorJobID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 11:00
     * @param objCommon
     * @param eqpMonitorID
     * @param eqpMonitorJobID -
     * @return java.util.List<com.fa.cim.common.support.ObjectIdentifier>
     */
    List<ObjectIdentifier> eqpMonitorJobLotRemove(Infos.ObjCommon objCommon, ObjectIdentifier eqpMonitorID, ObjectIdentifier eqpMonitorJobID);

    /**
     * description: Clear material locations.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/11/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/11/5 17:15
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void equipmentArrivalCarrierCancelForInternalBuffer(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.NPWXferCassette> strNPWXferCassette);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/13 17:20
     * @param objCommon
     * @param equipmentID
     * @param strProcessJobMapInfoSeq -
     * @return void
     */
    void equipmentContainerPositionProcessJobSet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.ProcessJobMapInfo> strProcessJobMapInfoSeq);

    /**
     * description:equipment_lots_WhatNextDR__140
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objEquipmentLotsWhatNextDRIn
     * @param objCommon                    -
     * @return com.fa.cim.pojo.Outputs.ObjEquipmentLotsWhatNextDROut
     * @author panda
     * @date 2018/7/12 13:34
     * @see com.fa.cim.method.impl.equipment.EquipmentWhatNextMethod#equipmentLotsWhatNextDR(Inputs.ObjEquipmentLotsWhatNextDRIn, Infos.ObjCommon)
     */
    @Deprecated
    Results.WhatNextLotListResult equipmentLotsWhatNextDR(Inputs.ObjEquipmentLotsWhatNextDRIn objEquipmentLotsWhatNextDRIn, Infos.ObjCommon objCommon);


    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2019/12/31 10:27
     */
    void equipmentAuto3DispatchSettingUpdateDR(Infos.ObjCommon objCommon,Infos.EqpAuto3SettingInfo strEqpAuto3SettingInfo,String updateMode);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/1/2 13:36
     * @param objCommon
     * @param eqpInternalBufferInfos
     * @param equipmentID
     * @param cassetteID
     * @param unloadReservePortID -
     * @return void
     */
    void equipmentCheckConditionForUnloadingForInternalBuffer(Infos.ObjCommon objCommon, List<Infos.EqpInternalBufferInfo> eqpInternalBufferInfos, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier unloadReservePortID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/1/2 13:37
     * @param objCommon
     * @param equipmentID
     * @param cassetteID
     * @param unloadReservePortID -
     * @return void
     */
    void equipmentUnloadingLotsReservationForInternalBuffer(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier unloadReservePortID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/1/6 15:16
     * @param objCommon
     * @param equipmentID
     * @param cassetteID -
     * @return void
     */
    void equipmentCheckConditionForUnloadReserveCancelForInternalBuffer(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/1/6 15:18
     * @param objCommon
     * @param equipmentID
     * @param cassetteID -
     * @return void
     */
    void equipmentUnloadingLotsReservationCancelForInternalBuffer(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/2/19 16:54                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/2/19 16:54
     * @param objCommon
     * @param objectID
     * @param type
     * @param fromTimeStamp
     * @param toTimeStamp -
     * @return java.util.List<com.fa.cim.dto.Infos.EquipmentAlarm>
     */

    List<Infos.EquipmentAlarm> equipmentAlarmHistoryFillInTxEQQ011DR(Infos.ObjCommon objCommon, ObjectIdentifier objectID, String type, String fromTimeStamp, String toTimeStamp);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/2/24 15:17                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/2/24 15:17
     * @param objCommon
     * @param equipmentID
     * @param eqpMonitorID -
     * @return java.lang.String
     */

    String eqpMonitorConditionReset(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier eqpMonitorID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param equipmentID
     * @param reticlePodPortID
     * @param reticlePodID
     * @param claimMemo
     * @return void
     * @exception
     * @author ho
     * @date 2020/3/19 14:42
     */
    void equipmentReticlePodUnload (
            Infos.ObjCommon strObjCommonIn,
            ObjectIdentifier equipmentID,
            ObjectIdentifier reticlePodPortID,
            ObjectIdentifier reticlePodID,
            String claimMemo);

    /**
     * This function inquires product Lot list of specified equipments and products.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/23 14:17
     */
    List<Infos.LotListAttributes> equipmentsProductLotListGetDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> equipmentIDs, List<ObjectIdentifier> productIDs);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/7 11:00
     * @param objCommon -
     * @return java.util.List<com.fa.cim.dto.Infos.EntityValue>
     */
    List<Infos.EntityValue> equipmentTypeListGetDR(Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/7 11:11
     * @param objCommon
     * @param searchKeyName
     * @param searchKeyValue
     * @param option -
     * @return java.util.List<com.fa.cim.dto.Infos.EntityValue>
     */
    List<Infos.EntityValue> equipmentListGetDR(Infos.ObjCommon objCommon, String searchKeyName, String searchKeyValue, String option);


    /**
     * find equipment availability of chambers
     *
     * @param objCommon
     * @param equipmentID
     * @return {@link Outputs.EquipmentAvailableInfoGetDROut}
     * @author Yuri
     * @date 2020/4/27 18:17
     */
    Outputs.EquipmentAvailableInfoGetDROut equipmentAvailableInfoGetDR (Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * This function sets SLMSwitch to equipment.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/5/7 13:59
     */
    void equipmentSLMSwitchSet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String slmSwitch);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/12 13:46
     * @param objCommon
     * @param equipmentID
     * @param controlJobID -
     * @return void
     */
    void equipmentReservedControlJobClearForSLM(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID);

    List<String> getEquipmentSpecialControls(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/4 10:41
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.StoredReticle> equipmentRetrieveReticleListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/4 13:22
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    ObjectIdentifier machineWorkAreaGet(Infos.ObjCommon objCommon, ObjectIdentifier machineID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/4 18:09
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    String machineStateAvailabilityCheckForReticlePodXfer(Infos.ObjCommon objCommon, ObjectIdentifier machineID);


    void equipmentReticlePodPortAccessModeChange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier reticlePodPortID, String newAccessMode);

    void equipmentConditionCheckForReticleRetrieve(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier reticlePodPortID, ObjectIdentifier  reticlePodID, List<Infos.MoveReticles> strMoveReticlesSeq);



    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/6 16:03
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void equipmentReticlePodStateChange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, String portStatus);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/9 17:53
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void machineCapacityCheckForReticleStore(Infos.ObjCommon objCommon, ObjectIdentifier machineID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/9 18:06
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.ReticleComponentJob> machineStoreComponentJobGetDR(Infos.ObjCommon objCommon, ObjectIdentifier machineID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/16                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/16 13:24
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void machineReticlePodPortReserve(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, ObjectIdentifier reticlePodID);

    /**
     * description:This method load reticle pod to a port of equipment.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/17 14:24                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/17 14:24
     * @param objCommon
     * @param equipmentID
     * @param reticlePodPortID
     * @param reticlePodID
     * @param claimMemo -
     * @return void
     */
    void equipmentReticlePodLoad(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier reticlePodPortID, ObjectIdentifier reticlePodID, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/24 10:53                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/24 10:53
     * @param objCommon
     * @param equipmentID -
     * @return boolean
     */
    boolean equipmentReticleRequiredFlagGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/4 14:33                      Decade                Create
    *
    * @author Decade
    * @date 2021/3/4 14:33
    * @param null -
    * @return
    */
    void capabilityCheck(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotIdList, ObjectIdentifier equipmentID);

    /**
     * description: Qiandao Project
     *              Check Equipment Auto MoveInReserve Condition
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/26                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/26 14:00
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     * @param objCommon
     * @param carrierID
     * @param equipmentID
     * @param portID
     */
    Infos.EqpPortStatus equipmentAutoMoveInReserveConditionCheck(Infos.ObjCommon objCommon, ObjectIdentifier carrierID, ObjectIdentifier equipmentID, ObjectIdentifier portID);

    /**
     * description:Qiandao Project
     *             1.filter none current equipment processing lots
     *             2.get carrier lots moveInReserveInfo inputParam
     *             3.regard all suitable lots belongs one Foup moveInFlag is true
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/26                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/26 16:51
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.LotsMoveInReserveInfoInqResult equipmentAutoMoveInReserveInfoGet(Infos.ObjCommon objCommon, Results.LotInfoInqResult lotInfoInqResult, ObjectIdentifier equipmentID);
    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/4 14:33                      Decade                Create
    *
    * @author Decade
    * @date 2021/3/4 14:33
    * @param null -
    * @return
    */
    void ibFurnaceEQPBatchinqCheck(Infos.ObjCommon objCommon, ObjectIdentifier eqpID);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/4 16:50                      Decade                Create
    *
    * @author Decade
    * @date 2021/3/4 16:50
    * @param null -
    * @return
    */
    IBFurnaceEQPBatchInfo ibFurnaceEQPBatchInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier eqpID);

    /**
     * description: equipment furnace search
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param equipmentFurnaceSearchParams - eqp search params
     * @param objCommon - obj common
     * @return result
     * @author YJ
     * @date 2021/3/2 0002 16:03
     */
    List<LayoutRecipeResults.EquipmentFurnaceResult> equipmentFurnaceSearch(Infos.ObjCommon objCommon, LayoutRecipeParams.EquipmentFurnaceSearchParams equipmentFurnaceSearchParams);

    /**
     * description: Task-461
     *              CarrierOutPortReq
     *                  check carrierPort inputParam condition
     *                      1.check carrier is no processing
     *                      2.check portID port usage type is INPUT_OUTPUT or OUTPUT
     *                      3.check portID in buffer exist moveInReserve carrier
     *                      4.check portID loadCassetteID is empty
     *                      5.check portID dispatchUnloadCasseteID is empty when accessMode is "Auto"
     *                      6.check portID onlineMode is not offline
     * change history:
     * date             defect#             person             comments
     * ----------------------------------------------------------------------------------------------------------------
     * 2021/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/7/29 15:09
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void equipmentCarierOutPortInfoCheck(Infos.ObjCommon objCommon,
                                         ObjectIdentifier equipmentID,
                                         List<CarrierOutPortInfo> carrierOutPortInfoList);

    /**
     * description:Task-461
     *             CarrierOutPortReq
     *                   equipmentCarierOutPortInfoGet
     *                      1.forEach all eqpPortInfo check condition
     *                      2.filter portID in buffer exist moveInReserve carrier
     *                      3.filter port useage is INPUT_OUTPUT or OUTPUT
     *                      4.filter port loadCassetteID is empty
     *                      5.filter port dispatchUnloadCassetteID is empty when accessMode is "AUTO"
     *                      6.collect all portList upon 1-5 and order by dispatchStateTime
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/8/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/8/2 14:52
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<CarrierOutPortResults> equipmentCarierOutPortInfoGet(Infos.ObjCommon objCommon,
                                                              ObjectIdentifier equipmentID,
                                                              List<ObjectIdentifier> carrierList);
}
