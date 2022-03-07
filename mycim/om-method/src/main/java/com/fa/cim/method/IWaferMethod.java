package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.lot.LotUsageRecycleCountParams;
import com.fa.cim.sorter.Info;

import java.util.List;

/**
 * description:
 * This file use to define the IWaferMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/9/19        ********             Bear               create file
 * 2019/9/23        ######              Neko                Refactor: change retCode to exception
 *
 * @author Bear
 * @since 2018/9/19 15:40
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IWaferMethod {

    /**
     * description:
     * Cancel current relation between carrier and wafer Position
     * Create new relation between input carrier ID and input wafer ID
     * Set wafer position as input parameter specifiing
     * if newCassetteID is not specified,
     * wafer-carrier / lot-carrier relation is not created
     * by using this method, "1 lot - multi carrier" relation can be created.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param newCassetteID
     * @param strWafer
     * @return com.fa.cim.dto.other.RetCode
     * @author Bear
     * @date 2018/4/26
     */
    void waferMaterialContainerChange(Infos.ObjCommon objCommon, ObjectIdentifier newCassetteID, Infos.Wafer strWafer);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/8 16:21
     * @param objCommon
     * @param newWaferAttributesList -
     * @return void
     */
    void waferAssignedLotChangeForSTBCancel(Infos.ObjCommon objCommon, List<Infos.NewWaferAttributes> newWaferAttributesList);

    /**
     * description:
     * By this method, lot - multi carrier relation can be created.
     * If sourceWaferID and newWaferID is different, this method re-assign new wafer ID for wafer
     * all of strNewWaferAttributesSeq.strNewWaferAttributes[i].newLotID must be the same
     * input parameter's strNewWaferAttributesSeq[i].newSlotNumber won't be used.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param newWaferAttributesList -
     * @return com.fa.cim.dto.other.RetCode
     * @author Bear
     * @date 2018/4/27
     */
    void waferAssignedLotChange(Infos.ObjCommon objCommon, List<Infos.NewWaferAttributes> newWaferAttributesList);

    /**
     * description:Get cassetteID and LotID which wafreID of in-parameter is belong to.
     * Set both of them to out parameter if value were found.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/9/27 17:22
     * @param objCommon
     * @param waferID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjWaferLotCassetteGetOut>
     */
    Outputs.ObjWaferLotCassetteGetOut waferLotCassetteGet(Infos.ObjCommon objCommon, ObjectIdentifier waferID);

    /**
     * change history:
     *
     * 此方法过时，不再用此方法，删除FSSLOTMAP表相关逻辑，采用Component Job方式
     *
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 2018/10/18 13:37
     * @param objCommon
     * @param equipmentID
     * @param portGroup
     * @return com.fa.cim.dto.RetCode
     * @deprecated
     */
    @Deprecated
    void waferSorterCheckRunningJobs(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroup);


    /**
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 2018/10/18 14:10
     * @param objCommon
     * @param requiredData
     * @param sortPattern
     * @param destinationCassetteManagedBySiViewFlag
     * @param strWaferSorterSlotMap
     * @return com.fa.cim.dto.RetCode<List<Infos.WaferSorterSlotMap180>>
     */
    List<Infos.WaferSorterSlotMap> waferSorterSlotMapSelectDR(Infos.ObjCommon objCommon, String requiredData,
                                                              String sortPattern, String destinationCassetteManagedBySiViewFlag,
                                                              String originalCassetteManagedBySiViewFlag, Infos.WaferSorterSlotMap strWaferSorterSlotMap);

    /**
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 2018/10/19 16:00
     * @param objCommon
     * @param waferID
     * @return com.fa.cim.dto.RetCode<List<ObjectIdentifier>>
     */
    ObjectIdentifier waferLotGet(Infos.ObjCommon objCommon, ObjectIdentifier waferID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @since 2018/10/19 14:13
     * @param objCommon -
     * @param waferIDs -
     * @return java.util.List<com.fa.cim.pojo.Infos.AliasWaferName>
     */
    List<Infos.AliasWaferName> waferAliasNameGetDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> waferIDs);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 10/25/2018 4:09 PM
     * @param objCommon -
     * @param newWaferAttributesList -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void waferAssignedLotChangeForWaferSorter(Infos.ObjCommon objCommon, List<Infos.NewWaferAttributes> newWaferAttributesList);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/7                          Wind
     * @param objCommon
     * @param objWaferSorterJobCheckForOperation
     * @return RetCode<Object>
     * @author Wind
     * @date 2018/12/7 11:32
     */
    void waferSorterSorterJobCheckForOperation(Infos.ObjCommon objCommon, Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation);

    /**
     * description:
     *
     * 此方法已过时，调用newSorterMethod方法
     *
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/23        标记已过时           jerry              标记过时
     * @author Jerry
     * @date 2019/7/29 17:49
     * @param objCommon
     * @param equipmentID
     * @param actionCode
     * @param waferSorterSlotMaps
     * @param portGroup
     * @param physicalRecipeID
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.WaferSorterSlotMap>>
     */
    @Deprecated
    List<Infos.WaferSorterSlotMap> waferSorterCheckConditionForAction(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String actionCode, List<Infos.WaferSorterSlotMap> waferSorterSlotMaps, String portGroup, String physicalRecipeID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/7/29 17:55
     * @param objCommon
     * @param portGroup
     * @param equipmentID
     * @param cassetteIDs
     * @param requestTimeStamp
     * @param sorterStatus
     * @param direction
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void waferSorterSlotMapDeleteDR(Infos.ObjCommon objCommon, String portGroup, ObjectIdentifier equipmentID, List<ObjectIdentifier> cassetteIDs, String requestTimeStamp, String sorterStatus, String direction);

    /**
     * description: 删除slotmap临时参数信息
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/7/29 17:55
     * @param objCommon
     * @param onlineSorterRptParams
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void waferSorterSlotMapSTInfoDeleteDR(Infos.ObjCommon objCommon, Params.OnlineSorterRptParams onlineSorterRptParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/1 16:18
     * @param objCommon
     * @param strWaferSorterSlotMapSequence
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void waferSorterSlotMapInsertDR(Infos.ObjCommon objCommon, List<Infos.WaferSorterSlotMap> strWaferSorterSlotMapSequence);

    /**
     * description: 保存receiveAndPrepare->lotStart->setAliasWafer参数,以供eap报告结束后自动完成下线工作
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author ho
     * @date 2019/8/1 16:18
     * @param objCommon
     * @param onlineSorterActionExecuteReqParams
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void waferSorterSlotMapSTInfoInsertDR(Infos.ObjCommon objCommon, Params.OnlineSorterActionExecuteReqParams onlineSorterActionExecuteReqParams);

    /**
     * description: 保存receiveAndPrepare->lotStart->setAliasWafer参数,以供eap报告结束后自动完成下线工作
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author ho
     * @date 2019/8/1 16:18
     * @param objCommon
     * @param waferSorterSlotMapList
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void waferSorterCheckAliasName(Infos.ObjCommon objCommon, List<Infos.WaferSorterSlotMap> waferSorterSlotMapList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/1 14:30
     * @param objCommon
     * @param strWaferSorterSlotMap
     * @param updateStatus
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void waferSorterSlotMapStatusUpdateDR(Infos.ObjCommon objCommon, Infos.WaferSorterSlotMap strWaferSorterSlotMap, String updateStatus);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/1 16:20
     * @param objCommon
     * @param waferSorterSlotMaps
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void waferSorterLotMaterialsScrap(Infos.ObjCommon objCommon, List<Infos.WaferSorterSlotMap>  waferSorterSlotMaps);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/1 16:22
     * @param objCommon
     * @param waferSorterSlotMaps
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void waferSorterSlotMapWaferIdReadUpdateDR(Infos.ObjCommon objCommon, List<Infos.WaferSorterSlotMap>  waferSorterSlotMaps);



    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 10:36
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void waferSorterCheckConditionAfterWaferIdReadDR(Infos.ObjCommon objCommon, String portGroup, ObjectIdentifier equipmentID);

    /**
     * description: 使用SortNewMethod中的waferSorterActionListSelectDR方法替换该方法
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 15:25
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    List<Infos.WaferSorterActionList> waferSorterActionListSelectDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history: 使用SortNewMethod中的waferSorterActionListInsertDR方法替换该方法
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 17:05
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    void waferSorterActionListInsertDR(Infos.ObjCommon objCommon, List<Infos.WaferSorterActionList> strWaferSorterActionListSequence, ObjectIdentifier equipmentID);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/2/24 19:21                      Decade                Create
    *
    * @author Decade
    * @date 2021/2/24 19:21
    * @param null -
    * @return
    */
    void recycleCountCheck(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID, Infos.NewLotAttributes newLotAttributes, String LotType);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/2/24 20:14                      Decade                Create
    *
    * @author Decade
    * @date 2021/2/24 20:14
    * @param null -
    * @return
    */
    void bankInCancelCheckByUsageCount(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/2/24 20:21                      Decade                Create
    *
    * @author Decade
    * @date 2021/2/24 20:21
    * @param null -
    * @return
    */
    LotUsageRecycleCountParams getUsageRecycleCountByLot(Infos.ObjCommon objCommon, String lotObj);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/2/25 18:31                      Decade                Create
    *
    * @author Decade
    * @date 2021/2/25 18:31
    * @param null -
    * @return
    */
    void lotRecycleWaferCountUpdate(Infos.ObjCommon objCommon, String lotObj);

    /**
    * description: 修改Wafer的AliasName
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/8/6 1:47 下午 ZH Create
    *
    * @author ZH
    * @date 2021/8/6 1:47 下午
    * @param  ‐
    * @return void
    */
    void waferSorterAliasNameUpdate(Infos.ObjCommon objCommon, Infos.WaferSorter waferSorter);
}
