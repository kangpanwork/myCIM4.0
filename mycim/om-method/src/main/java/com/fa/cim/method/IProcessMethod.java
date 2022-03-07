package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.*;
import com.fa.cim.entity.runtime.eqp.CimEquipmentDO;
import com.fa.cim.entity.runtime.logicalrecipe.CimLogicalRecipeDO;
import com.fa.cim.entity.runtime.mrecipe.CimMachineRecipeDO;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationDO;
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
public interface IProcessMethod {
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param modulePOS -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjProcessFlowBatchDefinitionGetDROut>
     * @author Nyx
     * @date 2018/6/7 14:47
     */
    Outputs.ObjProcessFlowBatchDefinitionGetDROut processFlowBatchDefinitionGetDR(Infos.ObjCommon objCommon, String modulePOS);

    Outputs.ObjProcessNextOperationInModuleGetDROut processNextOperationInModuleGetDR(Infos.ObjCommon objCommon,
                                                                                      String modulePF, String moduleOpeNo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/9/18 20:28
     * @param objCommon -
     * @param lotID -
     */
    void poDelQueuePutDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/26 18:41
     * @param objCommon
     * @param lotID
     * @param processRef
     * @param sequenceNumber -
     * @return com.fa.cim.dto.RetCode<Outputs.ObjProcessLocateOut>
     */
    Outputs.ObjProcessLocateOut processLocate(Infos.ObjCommon objCommon, ObjectIdentifier lotID, Infos.ProcessRef processRef, Integer sequenceNumber);


    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param operationID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     * @author Ho
     * @date 2018/10/24 17:08:38
     */
    ObjectIdentifier processDefaultLogicalRecipeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier operationID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/11/29 15:21
     * @param objCommon
     * @param processOperationListForDurableDRIn -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.DurableOperationNameAttributes>>
     */
    List<Infos.DurableOperationNameAttributes> processOperationListForDurableDR(Infos.ObjCommon objCommon, Params.ProcessOperationListForDurableDRParams processOperationListForDurableDRIn);

    /**
     * processOperation_dcActionInfo_GetDR
     *
     * @param objCommon
     * @param poObj
     * @param lotID
     * @return
     * @author ho
     */
    Results.EDCSpecCheckActionResultInqResult processOperationDCActionInfoGetDR(Infos.ObjCommon objCommon, String poObj, ObjectIdentifier lotID);

    /**
     * description: processOperation_dataCollectionInfo_GetDR
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param specFlag
     * @param poObj
     * @return
     * @author Ho
     * @date 2018/9/26 14:44:04
     */
    List<Infos.DCDef> processOperationDataCollectionInfoGetDR(Infos.ObjCommon objCommon, Boolean specFlag, String poObj);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param portGroupID
     * @param controlJobID
     * @param startCassetteList
     * @param processJobPauseFlag
     * @return
     * @author PlayBoy
     * @date 2018/7/30
     */
    void processStartReserveInformationSet(Infos.ObjCommon objCommon,
                                           ObjectIdentifier equipmentID,
                                           String portGroupID,
                                           ObjectIdentifier controlJobID,
                                           List<Infos.StartCassette> startCassetteList,
                                           boolean processJobPauseFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param in        -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.OperationProcessRefListAttributes>>
     * @author Nyx
     * @date 2018/7/20 16:22
     */
    List<Infos.OperationProcessRefListAttributes> processOperationProcessRefListForLot(Infos.ObjCommon objCommon, Inputs.ObjProcessOperationProcessRefListForLotIn in);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param operationNumber
     * @param processFlow     -
     * @return com.fa.cim.pojo.Outputs.ObjProcessOperationProcessRefListForLotHelperOut
     * @author Nyx
     * @date 2018/7/24 17:27
     */
    String processOperationProcessRefListForLotHelper(Infos.ObjCommon objCommon, String operationNumber, String processFlow);

    /**
     * description:
     * <p>
     * Check the following items.
     * <p>
     * 1. Whether eqp requires process durable or not. If no-need, return OK;
     * <p>
     * 2. At least one of reticle / fixture for each reticleGroup / fixtureGroup is in the eqp or not.
     * Even if required reticle is in the eqp, its status must be _Available or _InUse.
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param logicalRecipeID
     * @param machineRecipeID
     * @param lotID           -
     * @return com.fa.cim.pojo.RetCode<Outputs.ObjProcessDurableCheckConditionForOperationStartOut>
     * @author Bear
     * @date 2018/8/6 14:47
     */
    Outputs.ObjProcessDurableCheckConditionForOperationStartOut processDurableCheckConditionForOpeStart(
            Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier logicalRecipeID, ObjectIdentifier machineRecipeID, ObjectIdentifier lotID);

    /**
     * Obtain the actual start information for Lot.
     * <ui>
     *     <li>Assigned Machine</li>
     *     <li>Assigned PortGroup</li>
     *     <li>Assigned LogicalRecipe</li>
     *     <li>Assigned MachineRecipe</li>
     *     <li>Assigned PhysicalRecipe</li>
     *     <li>Assigned Reticles</li>
     *     <li>Assigned Fixtures</li>
     *     <li>Assigned Fixtures</li>
     *     <li>Assigned Recipe Parameter Change Type</li>
     *     <li>Assigned Recipe Parameter Sets</li>
     *     <li>Assigned DataCollection Flag</li>
     *     <li>Assigned DataCollection Flag</li>
     *     <li>Assigned DataCollections</li>
     *     <li>Assigned Sampling Wafers</li>
     *     <li>...</li>
     * </ui>
     *
     * @param objCommon  objCommon
     * @param startCassetteList startCassetteList
     * @param equipmentID equipmentID
     * @param edcItemsNeedFlag edcItemsNeedFlag
     * @return actual start information like {@link Outputs.ObjProcessStartReserveInformationGetOut}
     * @author zqi
     */
    Outputs.ObjProcessStartReserveInformationGetOut processStartReserveInformationGet(Infos.ObjCommon objCommon,
                                                                                      List<Infos.StartCassette> startCassetteList,
                                                                                      ObjectIdentifier equipmentID,
                                                                                      boolean edcItemsNeedFlag);

    /**
     * This object function gets the data collection information.
     *
     * @param objCommon       objCommon
     * @param equipmentID     equipmentID
     * @param lotID           lotID
     * @param logicalRecipeID logicalRecipeID
     * @param machineRecipeID machineRecipeID
     * @return EDC Information like {@link Outputs.ObjProcessDataCollectionDefinitionGetOut}
     * @author zqi
     */
    Outputs.ObjProcessDataCollectionDefinitionGetOut processDataCollectionDefinitionGet(Infos.ObjCommon objCommon,
                                                                                        ObjectIdentifier equipmentID,
                                                                                        ObjectIdentifier lotID,
                                                                                        ObjectIdentifier logicalRecipeID,
                                                                                        ObjectIdentifier machineRecipeID);

    /**
     * @param objCommon       objCommon
     * @param equipmentID     equipmentID
     * @param lotID           lotID
     * @param logicalRecipeID logicalRecipeID
     * @param machineRecipeID machineRecipeID
     * @return Return true if the specific Lot need detail EDC information(e.x. item,spec...)
     * for assigned equipment and assigned recipe.
     * @author zqi
     */
    boolean edcDetailInformationGetFlag(Infos.ObjCommon objCommon,
                                        ObjectIdentifier equipmentID,
                                        ObjectIdentifier lotID,
                                        ObjectIdentifier logicalRecipeID,
                                        ObjectIdentifier machineRecipeID);

    /**
     * @param objCommon objCommon
     * @param lotID     lotID
     * @return Return true if the specific Lot has exist EDC item information.
     * @author zqi
     */
    boolean edcItemsInformationExist(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * Get Used Reticles for lot
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @return RetCode<List<Infos.StartReticle>>
     * @author PlayBoy
     * @date 2018/8/8
     */
    List<Infos.StartReticle> processAssignedReticleGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param lotID     lotID
     * @return RetCode<List<Infos.StartFixture>>
     * @author PlayBoy
     * @date 2018/8/8
     */
    List<Infos.StartFixture> processAssignedFixtureGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * Gets the process operation start information for Normal EQP when ControlJob is empty.
     *
     * @param objCommon objCommon
     * @param equipmentID equipmentID
     * @param cassetteID cassetteID
     * @param edcItemsNeedFlag edcItemsNeedFlag EDC性能优化添加，是否需要EDC的信息，看业务代码需要
     * @return Actual operation start information list {@link Infos.StartCassette}
     * @author zqi
     */
    List<Infos.StartCassette> processStartReserveInformationGetByCassette(Infos.ObjCommon objCommon,
                                                                          ObjectIdentifier equipmentID,
                                                                          List<ObjectIdentifier> cassetteID,
                                                                          boolean edcItemsNeedFlag);

    /**
     * Gets the process operation start information for IntelBuffer EQP when ControlJob is empty.
     *
     * @param objCommon objCommon
     * @param equipmentID equipmentID
     * @param cassetteID cassetteID
     * @param edcItemsNeedFlag edcItemsNeedFlag EDC性能优化添加，是否需要EDC的信息，看业务代码需要
     * @return Actual operation start information list {@link Infos.StartCassette}
     * @author zqi
     */
    Outputs.ObjProcessStartReserveInformationGetByCassetteOut processStartReserveInformationGetByCassetteForInternalBuffer(Infos.ObjCommon objCommon,
                                                                                                                           ObjectIdentifier equipmentID,
                                                                                                                           List<ObjectIdentifier> cassetteID,
                                                                                                                           boolean edcItemsNeedFlag);

    /**
     * description:
     * Clear ActualStart Info in Each Lots' PO<br/>
     * - Clear controlJobID of each cassette.<br/>
     * - Clear controlJobID of each lot.<br/>
     * - Clear control job info of each lot's cunrrent PO.<br/>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param controlJobID      controlJobID
     * @param startCassetteList startCassetteList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/8/10
     */
    void processStartReserveInformationClear(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * Clear wafer-Chamber Information in PO<br/>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param startCassetteList startCassetteList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/8/10
     */
    void processWaferChamberInformationClear(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * Clear Process resource wafer position information in PO<br/>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param startCassetteList startCassetteList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/8/10
     */
    void processWaferPositionInProcessResourceInformationClear(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param startCassettes -
     * @return com.fa.cim.pojo.Outputs.ObjProcessStartReserveInformationGetBaseInfoForClientOut
     * @author Jerry
     * @date 2018/8/15 16:08
     */
    Outputs.ObjProcessStartReserveInformationGetBaseInfoForClientOut processStartReserveInformationGetBaseInfoForClient(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassettes);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon      objCommon
     * @param startCassettes startCassettes
     * @return ObjLotCheckConditionForAutoBankInOut
     * @author Paladin
     * @date 2018/8/16
     */
    void processActualCompInformationSet(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassettes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param lotId     lotID
     * @return ObjProcessMoveOut
     * @author Paladin
     * @date 2018/8/26
     */
    Outputs.ObjProcessMoveOut processMove(Infos.ObjCommon objCommon, ObjectIdentifier lotId);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param mergedRouteID
     * @param mergedOperationNumber
     * @param returnOperationNumber -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Jerry
     * @date 2018/9/26 10:14
     */
    void processCheckSplit(Infos.ObjCommon objCommon, ObjectIdentifier mergedRouteID, String mergedOperationNumber, String returnOperationNumber);

    Outputs.ObjProcessCompareCurrentOut processCompareCurrent(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier mergedRouteID, String mergedOperationNumber);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/24 12:55
     * @param objCommon
     * @param routeType
     * @param lotID -
     * @return java.util.List<com.fa.cim.dto.Infos.ConnectedRouteList>
     */
    List<Infos.ConnectedRouteList> processConnectedRouteList(Infos.ObjCommon objCommon, String routeType, ObjectIdentifier lotID);

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
     * @date 2018/10/10 15:05
     */
    void processCheckBankIn(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon       objCommon
     * @param equipmentID     equipmentID
     * @param chamberStatuses chamberStatuses
     * @return ObjProcessResourceStateConvertOut
     * @author Paladin
     * @date 2018/8/26
     */
    Outputs.ObjProcessResourceStateConvertOut processResourceStateConvert(Infos.ObjCommon objCommon,
                                                                          ObjectIdentifier equipmentID,
                                                                          List<Infos.EqpChamberStatus> chamberStatuses);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                    objCommon
     * @param equipmentID                  equipmentID
     * @param chamberStatuses              eqpChamberStatusCheckResults
     * @param checkManufacturingFlag       checkManufacturingFlag
     * @return List<Infos.EqpChamberStatus>
     * @author Paladin
     * @date 2018/8/26
     */
    List<Infos.EqpChamberStatusCheckResult> processResourceCurrentStateCheckTransition(
            Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.EqpChamberStatus> chamberStatuses,
            boolean checkManufacturingFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                    objCommon
     * @param equipmentID                  equipmentID
     * @param checkResults                 eqpChamberStatusCheckResults
     * @return Results.ObjProcessResourceCurrentStateChangeOut
     * @author Paladin
     * @date 2018/9/20
     */
    Outputs.ObjProcessResourceCurrentStateChangeOut processResourceCurrentStateChange(
            Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
            List<Infos.EqpChamberStatusCheckResult> checkResults);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon             objCommon
     * @param equipmentID           equipmentID
     * @param actualChamberStatuses actualChamberStatuses
     * @return Outputs.ObjProcessResourceCurrentStateChangeByAutoOut
     * @author Paladin
     * @date 2018/9/20
     */
    Outputs.ObjProcessResourceCurrentStateChangeByAutoOut processResourceCurrentStateChangeByAuto(
            Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
            List<Infos.ActualChamberStatus> actualChamberStatuses);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objcommon
     * @param strProcessOperationListForRouteIn
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.OperationNameAttributes160>
     * @author Sun
     * @date 2018/10/12 14:07
     */
    List<Infos.OperationNameAttributes> processOperationListForRoute(Infos.ObjCommon objcommon, Inputs.ProcessOperationListForRoute strProcessOperationListForRouteIn);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param lotID     lotID
     * @param category  category
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/16 10:35:59
     */
    Outputs.ObjProcessOperationDataConditionGetDrOut processOperationDataConditionGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String category);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param parentLot
     * @param childLot  -
     * @return com.fa.cim.dto.RetCode
     * @author Jerry
     * @date 2018/10/17 13:40
     */
    void processCheckMerge(Infos.ObjCommon objCommon, ObjectIdentifier parentLot, ObjectIdentifier childLot);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param pdID
     * @return RetCode<ObjectIdentifier>
     * @author Sun
     * @date 2018/10/16 17:28
     */
    ObjectIdentifier processActiveIDGet(Infos.ObjCommon objCommon, ObjectIdentifier pdID);

    /**
     * description:
     * method: process_OperationListForLot__160
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param params
     * @return com.fa.cim.pojo.RetCode<Outputs.ObjProcessOperationListForLotOut>
     * @author Nyx
     * @date 2018/8/29 11:06
     */
    List<Infos.OperationNameAttributes> processOperationListForLot(Infos.ObjCommon objCommon, Inputs.ObjProcessOperationListForLotIn params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param lotID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.ProcessOriginalOperationGetOut>
     * @author Ho
     * @date 2018/12/5 14:06:47
     */
    Infos.ProcessOriginalOperationGetOut processOriginalOperationGet(Infos.ObjCommon strObjCommonIn, ObjectIdentifier lotID);

    /**
     * description:
     * <p>Store reported raw data value as temporarily data with control job ID.
     * The key of data to be stored is specified controlJobID.
     * Usually, reported raw data is newly added with controlJobID.
     * If data to be stored is already existing with same key (controlJobID),
     * that must be updated by inputted raw data.<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param controlJobID      controlJobID
     * @param startCassetteList startCassetteList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/18 18:18:56
     */
    void processOperationTempDataSet(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, List<Infos.StartCassette> startCassetteList);


    /**
     * Just only set the EDC items information.
     *
     * @param objCommon         objCommon
     * @param controlJobID      controlJobID
     * @param startCassetteList startCassetteList
     */
    void processOperationTempDataEDCItemsSet(Infos.ObjCommon objCommon,
                                             ObjectIdentifier controlJobID,
                                             List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * <p>Confirm whether PO for started lot should be current or previous.
     * Get PO's collected data information.
     * If length of DC Spec ID is not 0,
     * Get DC Spec's detailed information.
     * Store collected data information with DC Spec's detailed information into PO.
     * Return collected data information with DC Spec's detailed information.<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param startCassetteList startCassetteList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/18 18:26:25
     */
    List<Infos.StartCassette> processDataCollectionSpecificationSet(Infos.ObjCommon objCommon,
                                                                    List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * method:process_checkForDynamicRoute
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  -
     * @param subRouteID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjProcessCheckForDynamicRouteOut>
     * @author Bear
     * @date 2018/10/23 16:38
     */
    Outputs.ObjProcessCheckForDynamicRouteOut processCheckForDynamicRoute(Infos.ObjCommon objCommon, ObjectIdentifier subRouteID);

    /**
     * description:
     * method:process_GetReturnOperation
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  -
     * @param lotID      -
     * @param subRouteID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjProcessGetReturnOperationOut>
     * @author Bear
     * @date 2018/10/23 17:12
     */
    Outputs.ObjProcessGetReturnOperationOut processGetReturnOperation(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier subRouteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param params
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.ProcessDefinitionIndexListInqResult>
     * @author Sun
     * @date 2018/10/24 13:35
     */
    List<Infos.ProcessDefinitionIndexList> processDefinitionIDList(Infos.ObjCommon objCommon, Params.RouteListInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.ProcessDefinitionIndexListInqResult>
     * @author Sun
     * @date 2018/10/24 14:26
     */
    List<ObjectIdentifier> processDefinitionProcessDefinitionIDGetDR(Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param params
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.MainProcessFlowListInqResult>
     * @author Sun
     * @date 2018/10/24 14:15
     */
    List<Infos.RouteIndexInformation> processRouteList(Infos.ObjCommon objCommon, Params.MainProcessFlowListInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.StageListInqResult>
     * @author Sun
     * @since 2018/10/24 16:11
     */
    Results.StageListInqResult processStageIDGetDR(Infos.ObjCommon objCommon, Params.StageListInqParams params);

    /**
     * description:
     * method:process_reworkCount_Check
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/10/25 10:07
     */
    void processReworkCountCheck(Infos.ObjCommon objCommon, ObjectIdentifier lotID);


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
     * @date 2018/10/29 16:19
     */
    void processReworkCountIncrement(Infos.ObjCommon objCommon, ObjectIdentifier lotID);


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
     * @date 2018/11/6 10:52
     */
    void processCheckBranchCancel(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Inputs.OldCurrentPOData>
     * @author Bear
     * @date 2018/11/6 13:10
     */
    Inputs.OldCurrentPOData processCancelBranchRoute(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                  objCommon
     * @param lotID                      lotID
     * @param dataCollectionDefinitionID dataCollectionDefinitionID
     * @return DCItems
     * @author PlayBoy
     * @date 2018/10/30 15:17:46
     */
    List<Infos.DataCollectionItemInfo> processOperationDCItemsGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier dataCollectionDefinitionID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon             -
     * @param lotID                 -
     * @param subRouteID            -
     * @param returnOperationNumber -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Inputs.OldCurrentPOData>
     * @author Bear
     * @date 2018/10/30 15:17
     */
    Inputs.OldCurrentPOData processBranchRoute(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier subRouteID, String returnOperationNumber);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 11/5/2018 5:20 PM
     */
    void processReworkCountDecrement(Infos.ObjCommon objCommon, ObjectIdentifier lotID);


    /**
     * description:
     * method:process_dynamicRouteListDR
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params    -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.DynamicPathListInqResult>
     * @author Bear
     * @date 2018/11/6 16:39
     */
    Page<Infos.DynamicRouteList> processDynamicRouteListDR(Infos.ObjCommon objCommon, Params.DynamicPathListInqParams params);

    /**
     * description:
     * <p>Confirm whether PO for started lot should be current or previous.
     * Get PO's collected data information.
     * Change dataValue from empty to *. (P4200535)
     * Change specCheckResult to empty when targetValue is empty. (D7000100)
     * Omit * dataValue when OM_EDC_ASTERISK_VALUE_SAVE is 0. (P4200535)
     * Store collected data information into PO.
     * Store action data information into PO.(DSIV00001021)</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon             objCommon
     * @param startCassetteList     startCassetteList
     * @param dcActionLotResultList dcActionLotResultList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/11/6 15:48:32
     */
    void processDataCollectionInformationUpdate(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList, List<Results.DCActionLotResult> dcActionLotResultList);


    /**
     * description:
     * <p>
     * method:process_processLagTime_Get
     * Get ProcessLagTime of lot's previous operation.
     * - If processLagTime is defined, the information, which includes expiredTimeDuration
     * - If processLagTime is not defined, "0" and "1901-01-01-00.00.00.000000" are returned.</p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjMonitorLotSTBInfoGetOut>
     * @author Bear
     * @date 2018/11/7 16:27
     */
    Outputs.ObjProcessLagTimeGetOut processProcessLagTimeGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p>It is purpose of this function that get process operation information by specified process.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param param     param
     * @return RetCode
     * @author PlayBoy
     * @date 2018/11/13 18:01:00
     */
    Infos.ProcessOperationInfo processOperationInfoGetDr(Infos.ObjCommon objCommon, Inputs.ObjProcessOperationInfoGetDrIn param);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2018/11/17                            Nyx             add content
     *
     * @param objCommon -
     * @param params    -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjProcessOperationNumberListForLotOut>
     * @author Nyx
     * @date 11/14/2018 4:23 PM
     */
    List<Infos.OperationNumberListAttributes> processOperationNumberListForLot(Infos.ObjCommon objCommon, Inputs.ObjProcessOperationProcessRefListForLotIn params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param operationNameAttributes -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.DurableOperationNameAttributes>
     * @author Nyx
     * @date 2018/11/12 16:15
     */
    Infos.DurableOperationNameAttributes processOperationListForDurableHelperDR(Infos.ObjCommon objCommon, Infos.DurableOperationNameAttributes operationNameAttributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param operationID -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.ObjectIdentifier>>
     * @author Nyx
     * @date 2018/11/17 21:55
     */
    List<ObjectIdentifier> processDispatchEquipmentsForDurableGetDR(Infos.ObjCommon objCommon, ObjectIdentifier operationID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon           -
     * @param processDefinitionID -
     * @param PDLevel             -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Sun
     * @date 11/16/2018 1:56 PM
     */
    String processExistenceCheck(Infos.ObjCommon objCommon, ObjectIdentifier processDefinitionID, String PDLevel);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  -
     * @param processRef -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.objProcessPreviousProcessReferenceOut>
     * @author Sun
     * @date 11/16/2018 2:22 PM
     */
    Outputs.objProcessPreviousProcessReferenceOut processPreviousProcessReferenceGet(Infos.ObjCommon objCommon, Infos.ProcessRef processRef);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                                 -
     * @param mainProcessFlowObjRef                     -
     * @param operationNumber                           -
     * @param moduleProcessOperationSpecificationObjRef -
     * @param branchCheckFlag                           -
     * @param reworkCheckFlag                           -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.ConnectedRoute>>
     * @author Sun
     * @date 11/16/2018 2:58 PM
     */
    List<Infos.ConnectedRoute> processConnectedRouteGetDR(Infos.ObjCommon objCommon, String mainProcessFlowObjRef, String operationNumber,
                                                          String moduleProcessOperationSpecificationObjRef, Boolean branchCheckFlag, Boolean reworkCheckFlag);

    /**
     * description:process_OperationListForLot_Helper__160
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotScheduleObj
     * @param backupInfoFlag
     * @param productID         -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.OperationNameAttributes>
     * @author Nyx
     * @date 2018/11/17 21:57
     */
    Infos.OperationNameAttributes processOperationListForLotHelper(Infos.OperationNameAttributes attributes, Infos.ObjCommon objCommon, String lotScheduleObj, Boolean backupInfoFlag, ObjectIdentifier productID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/5                          Wind
     * @param objCommon
     * @param lotID
     * @return RetCode<Outputs.ObjProcessGetFlowBatchDefinitionOut>
     * @author Wind
     * @date 2018/12/5 13:32
     */
    Outputs.ObjProcessGetFlowBatchDefinitionOut processGetFlowBatchDefinition(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   -
     * @param inputParams -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.CollectedDataItem>>
     * @author Sun
     * @date 12/10/2018 5:00 PM
     */
    List<Infos.CollectedDataItem> processOperationRawDCItemsGetDR(Infos.ObjCommon objCommon, Inputs.ObjProcessOperationRawDCItemsGetDR inputParams);



    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  -
     * @param controlJobID -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.ProcessJob>>
     * @author Sun
     * @date 12/10/2018 5:21 PM
     */
    List<Infos.ProcessJob> processOperationProcessWafersGet(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param   objCommon
     * @param   futureActionDetailInfoInqParams
     * @return  ObjProcessFutureQrestTimeInfoGetDROut
     * @author Scott
     * @date 2018/12/5 10:11:17
     */
    Outputs.ObjProcessFutureQrestTimeInfoGetDROut processFutureQrestTimeInfoGetDR(
            Infos.ObjCommon objCommon, Params.FutureActionDetailInfoInqParams futureActionDetailInfoInqParams);


    /**
     * description:
     * 未实现
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date  10:25
     * @param objCommon
     * @param objProcessGetTargetOperationIn -
     * @return com.fa.cim.pojo.Outputs.ObjProcessGetTargetOperationOut
     */
    Outputs.ObjProcessGetTargetOperationOut processGetTargetOperation(Infos.ObjCommon objCommon, Inputs.ObjProcessGetTargetOperationIn objProcessGetTargetOperationIn);


    /**
     * description:
     *
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018.12.25       nyx-modify         nyx                modify operationNumber return value
     * @author Jerry
     * @date  10:40
     * @param objCommon
     * @param processRef -
     * @return com.fa.cim.pojo.Outputs.ObjProcessGetOperationByProcessRefOut
     */
    Outputs.ObjProcessGetOperationByProcessRefOut processGetOperationByProcessRef(Infos.ObjCommon objCommon, Infos.ProcessRef processRef);

    List<CimEquipmentDO> getEquipmentsByprocessDefinitionAndEquipment(CimProcessDefinitionDO processDefinition, CimProductSpecificationDO productSpecification);

    List<CimEquipmentDO> getAllMachinesBySpecificRecipeSetting (CimLogicalRecipeDO logicalRecipe);

    List<CimEquipmentDO> getAllMachines(CimMachineRecipeDO mRecipe);

    List<CimMachineRecipeDO> getAllMachineRecipe(CimLogicalRecipeDO logicalRecipe);

    /**
     * description:
     * change history: add fromTimeStamp and toTimeStamp for runcard history
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   -
     * @param searchCount -
     * @param lotID       -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.OperationNameAttributesFromHistory>>
     * @author Sun
     * @date 12/11/2018 5:33 PM
     */
    List<Infos.OperationNameAttributesFromHistory> processOperationListFromHistoryDR(Infos.ObjCommon objCommon, Long searchCount, ObjectIdentifier lotID, String fromTimeStamp, String toTimeStamp);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param
     * @return
     * @author Scott
     * @date 2018/12/13 09:59:48
     */
    List<Infos.ProcHoldListAttributes> processHoldHoldListGetDR(Infos.ObjCommon objCommon, Infos.ProcessHoldSearchKey processHoldSearchKey, ObjectIdentifier reasonCodeID, Long count, boolean countLimitter, boolean validAst);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/14                          Wind
     * @param objCommon
     * @param objProcessOperationNestListGetDRIn
     * @return RetCode<Infos.RouteInfo>
     * @author Wind
     * @date 2018/12/14 13:42
     */
    Infos.RouteInfo processOperationNestListGetDR(Infos.ObjCommon objCommon, Inputs.ObjProcessOperationNestListGetDRIn objProcessOperationNestListGetDRIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/17                          Wind
     * @param objCommon
     * @param param
     * @return RetCode<Infos.ProcessHoldHistory>
     * @author Wind
     * @date 2018/12/17 17:30
     */
    void processHoldRequestsMakeEntry(Infos.ObjCommon objCommon, Params.ProcessHoldReq param);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/23 15:45
     * @param objCommon
     * @param param -
     * @return java.util.List<com.fa.cim.common.support.ObjectIdentifier>
     */
    List<ObjectIdentifier> processHoldGetLotListForHoldDR(Infos.ObjCommon objCommon, Params.ProcessHoldReq param);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/23 16:53
     * @param objCommon
     * @param param -
     * @return com.fa.cim.dto.Infos.ProcessHoldHistory
     */
    void processHoldRequestsDeleteEntry(Infos.ObjCommon objCommon, Params.ProcessHoldCancelReq param);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/23 16:59
     * @param objCommon
     * @param param -
     * @return java.util.List<com.fa.cim.common.support.ObjectIdentifier>
     */
    List<ObjectIdentifier> processHoldGetLotListForHoldReleaseDR(Infos.ObjCommon objCommon, Params.ProcessHoldCancelReq param);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/8/20 14:04
     * @param objCommon
     * @param processResourcePositionInfo -
     * @return void
     */
    void processWaferPositionInProcessResourceInformationSet(Infos.ObjCommon objCommon, Infos.ProcessResourcePositionInfo processResourcePositionInfo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/4/2 14:06
     * @param objCommon
     * @param strChamberProcessLotInfos -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void processWaferChamberInformationSet(Infos.ObjCommon objCommon, List<Infos.ChamberProcessLotInfo> strChamberProcessLotInfos);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/26                          Wind
     * @param objCommon
     * @param lotID
     * @return RetCode<Object>
     * @author Wind
     * @date 2018/12/26 13:46
     */
    void processCheckGatePass(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/10/23 14:06
     * @param objCommon
     * @param lotID -
     * @return void
     */
    void processCheckGatePassForBondingFlowSection(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param  objCommon
     * @param  aLotID
     * @param  productLotID
     * @return java.lang.Boolean
     * @author Scott
     * @date 2018/12/27 11:09:00
     */
    Boolean repeatGatePassCheckCondition(Infos.ObjCommon objCommon, ObjectIdentifier aLotID, ObjectIdentifier productLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/3/19 14:57
     * @param objCommon
     * @param in -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.ConnectedSubRouteOperationInfo>>
     */
    List<Infos.ConnectedSubRouteOperationInfo> processOperationListInRouteForFPCGetDR(Infos.ObjCommon objCommon, Inputs.ObjProcessOperationListInRouteForFpcGetDRIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/3/20 10:26
     * @param objCommon
     * @param routeID
     * @param operationNumber
     * @param routeRequirePattern -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.OperationInformation>>
     */
    List<Infos.OperationInfo> processOperationListInRouteGetDR(Infos.ObjCommon objCommon, ObjectIdentifier routeID, String operationNumber, String routeRequirePattern);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/3/26 10:32
     * @param objCommon
     * @param dKey
     * @param seqNo -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.PostProcessAdditionalInfo>>
     */
    List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoGetDR(Infos.ObjCommon objCommon, String dKey, Integer seqNo);
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/26                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/26 11:31
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjProcessBondingFlowDefinitionGetDROut processBondingFlowDefinitionGetDR(Infos.ObjCommon objCommon, String modulePOS);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/26                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/26 13:08
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void processCheckInterFabXferPlanSkip(Infos.ObjCommon objCommon, Inputs.ObjProcessCheckInterFabXferPlanSkipIn objProcessCheckInterFabXferPlanSkipIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param dcItems   -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @author Sun
     * @date 4/11/2019 3:31 PM
     */
    void processOperationRawDCItemsSet(Infos.ObjCommon objCommon, List<Infos.CollectedDataItemStruct> dcItems);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/25                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/25 13:04
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void processOperationRecipeParametersSet(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, List<Infos.LotStartRecipeParameter> strLotStartRecipeParameterSeq);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/10/14 14:48
     * @param objCommon
     * @param productID
     * @param operationID -
     * @return java.util.List<com.fa.cim.dto.Infos.BOMPartsInfo>
     */
    List<Infos.BOMPartsInfo> processBOMPartsInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier productID, ObjectIdentifier operationID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/28 15:51
     * @param objCommon -
     * @return void
     */
    void processOperationActionResultSet(Infos.ObjCommon objCommon, Inputs.ObjProcessOperationActionResultSetIn objProcessOperationActionResultSetIn);

    Outputs.RawProcessResourceStateTranslateOut rawProcessResourceStateTranslate(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.EqpChamberStatus> strEqpChamberStatus);

    /**
     * description:
     * process_dispatchEquipments_GetDR
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param productID
     * @param operationID -
     * @return com.fa.cim.pojo.Outputs.ObjProcessDispatchEquipmentsOut
     * @author panda
     * @date 2018/5/29 18:33
     */
    List<ObjectIdentifier> processDispatchEquipmentsGetDR(Infos.ObjCommon objCommon, ObjectIdentifier productID, ObjectIdentifier operationID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/5 10:54
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjProcessOperationSequenceGetDROut processOperationSequenceGetDR(Infos.ObjCommon objCommon, Inputs.ProcessOperationSequenceGetDRIn processOperationSequenceGetDRIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/20 17:44
     * @param objCommon
     * @param lotID
     * @param routeID
     * @param operationNumber
     * @param processRef
     * @param referentialOperationNumber -
     * @return void
     */
    void processFutureHoldRequestsCheck(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier routeID, String operationNumber, Infos.ProcessRef processRef, String referentialOperationNumber);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/20 17:54
     * @param objCommon
     * @param lotID
     * @param routeID
     * @param operationNumber
     * @param operationReference -
     * @return void
     */
    void processMaxCountCheck(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier routeID, String operationNumber, Infos.ProcessRef operationReference);

    ObjectIdentifier processlogicalRecipeGetDR (Infos.ObjCommon objCommon, ObjectIdentifier productID, ObjectIdentifier operationID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/15 14:53                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/7/15 14:53
     * @param objCommon
     * @param durableCategory
     * @param durableID
     * @param routeType -
     * @return java.util.List<com.fa.cim.dto.Infos.ConnectedRouteList>
     */
    List<Infos.ConnectedRouteList> processConnectedRouteListForDurable(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, String routeType);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/31 15:19                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/7/31 15:19
     * @param objCommon
     * @param routeID
     * @param operationID
     * @param operationNumber
     * @param mainPOS
     * @param modulePOS -
     * @return com.fa.cim.dto.Infos.ScriptInfo
     */
    Infos.ScriptInfo processScriptInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier routeID, ObjectIdentifier operationID, String operationNumber, String mainPOS, String modulePOS);

    /**
     * description: process hold change
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon - common
     * @param param     - change param
     * @author YJ
     * @date 2021/1/27 0027 18:37
     */
    void processHoldRequestsChangeEntry(Infos.ObjCommon objCommon, Params.ProcessHoldReq param);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/2 10:25                      Decade                Create
    *
    * @author Decade
    * @date 2021/3/2 10:25
    * @param null -
    * @return
    */
    Params.SkipReqParams prepareForSkip(Infos.ObjCommon objCommon,String operationNumber, ObjectIdentifier lotID);

    /*
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/8 15:27                      Decade                Create
     *
     * @author Decade
     * @date 2021/3/8 15:27
     * @param null -
     * @return
     */
    String getFisrtStepOpeNumByLot(Infos.ObjCommon objCommon,ObjectIdentifier lotID);

    /*
     * description: Check whether the PDType of routeID and the incoming PDType are equal
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/11 12:27                     Nyx                Create
     *
     * @author Nyx
     * @date 2021/6/11 12:27
     * @param routeID
     * @param processDefinitionType
     * @return
     */
    void checkProcessDefinitionType(ObjectIdentifier routeID, String processDefinitionType);
}
