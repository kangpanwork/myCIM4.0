package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import java.util.List;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2018/11/1 13:23:10
 */
public interface IFlowBatchMethod {

   /**
    * description:
    * <p></p>
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    *
    * @param objCommon
    * @param equipmentID
    * @param flowBatchID
    * @return com.fa.cim.dto.RetCode<java.lang.Object>
    * @author Ho
    * @date 2018/11/1 13:27:47
    */
   void flowBatchInformationUpdateByOpeStart(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier flowBatchID);

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
     * @date 2018/12/26 11:17:02
     */
   void flowBatchInformationUpdateByOpeStartCancel(Infos.ObjCommon strObjCommonIn,ObjectIdentifier equipmentID,List<Infos.StartCassette> strStartCassette);

   /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/5                          Wind
     * @param objCommon
     * @param batchID
     * @return RetCode<List<Infos.ContainedCassettesInFlowBatch>>
     * @author Wind
     * @date 2018/12/5 11:22
     */
   List<Infos.ContainedCassettesInFlowBatch> flowBatchCassetteGet(Infos.ObjCommon objCommon, ObjectIdentifier batchID);

   /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/5                          Wind
     * @param objCommon
     * @param batchingReqLots
     * @return RetCode<Outputs.ObjFlowBatchMakeOut>
     * @author Wind
     * @date 2018/12/5 19:23
     */
   Outputs.ObjFlowBatchMakeOut flowBatchMake(Infos.ObjCommon objCommon, List<Infos.BatchingReqLot> batchingReqLots);

   /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/6                          Wind
     * @param objCommon
     * @param flowBatchID
     * @return RetCode<ObjectIdentifier>
     * @author Wind
     * @date 2018/12/6 15:25
     */
   ObjectIdentifier flowBatchReserveEquipmentIDGet(Infos.ObjCommon objCommon, ObjectIdentifier flowBatchID);

   /**
    * description:
    * <p></p>
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * @param   objCommon
    * @return com.fa.cim.pojo.Outputs.ObjFlowBatchLostLotsListGetDRout
    * @author Scott
    * @date 2018/12/12 11:13:39
    */ 
   Outputs.ObjFlowBatchLostLotsListGetDRout flowBatchLostLotsListGetDR(Infos.ObjCommon objCommon);

   /**
    *
    * @param objCommon
    * @param flowBatchInformation
    * @return com.fa.cim.pojo.Outputs.ObjFlowBatchInformationGetOut
    * @author Scott
    * @date 2018/12/13 14:01:29
    */
   Outputs.ObjFlowBatchInformationGetOut flowBatchInformationGet(Infos.ObjCommon objCommon, Infos.FlowBatchInformation flowBatchInformation);

   /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * @author light
    * @date 2019/11/22 11:24
    * @param objCommon
    * @param strFlowBatchByManualActionReqCassette -
    * @return com.fa.cim.dto.Outputs.ObjFlowBatchInfoSortByCassetteOut
    */
   Outputs.ObjFlowBatchInfoSortByCassetteOut flowBatchInfoSortByCassette(Infos.ObjCommon objCommon, List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette);

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
    * @date 12/17/2018 4:05 PM
    */
   void flowBatchInformationUpdateByOpeComp(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);
    
   /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * @author lightyh
    * @date 2019/10/22 13:54
    * @param objCommon
    * @param equipmentID -
    * @return com.fa.cim.dto.Results.FloatingBatchListInqResult
    */
    Results.FloatingBatchListInqResult flowBatchFillInTxDSQ002DR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * @param objCommon objCommon
     * @param equipmentID equipmentID
     * @param flowBatchID  flowBatchID
     * @return RetCode
     * @author ZQI
     * @date 2018/12/20 10:58:47
    */
    void flowBatchReserveEquipmentIDClear(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier flowBatchID);


     /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * -------------------------------------------------------------------------------------------------------------------
      * @param objCommon
      * @param batchID
      * @return RetCode<List<Infos.ContainedLotsInFlowBatch>>
      * @author ZQI
      * @date 2018/12/21 14:01:05
     */
   List<Infos.ContainedLotsInFlowBatch> flowBatchLotGet(Infos.ObjCommon objCommon, ObjectIdentifier batchID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * @param objCommon  objCommon
     * @param equipmentID  equipmentID
     * @param batchID  flowBatchID
     * @return RetCode<Object>
     * @author ZQI
     * @date 2018/12/21 17:27:07
    */
    void flowBatchReserveEquipmentIDSet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier batchID);

     /**
      * description:
      * <p>Set return value of OFLWW003.
      * To set return values, get PosFlowBatch object.</p>
      * change history:
      * date             defect             person             comments
      * -------------------------------------------------------------------------------------------------------------------
      * @param objCommon  objCommon
      * @param equipmentID  equipmentID
      * @param batchID  flowBatchID
      * @return RetCode<List<Infos.FlowBatchedLot>>
      * @author ZQI
      * @date 2018/12/22 16:16:58
     */
    List<Infos.FlowBatchedLot> flowBatchFillInTxDSC003(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier batchID);

   /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/20                          Wind
     * @param objCommon
     * @param flowBatchID
     * @param strRemoveLot
     * @return RetCode<Object>
     * @author Wind
     * @date 2018/12/20 13:49
     */
   void flowBatchLotRemove(Infos.ObjCommon objCommon, ObjectIdentifier flowBatchID, List<Infos.RemoveLot> strRemoveLot);
    
   /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * @author lightyh
    * @date 2019/10/10 13:33
    * @param objCommon
    * @param flowBatchID -
    * @return com.fa.cim.dto.Results.FlowBatchLotRemoveReqResult
    */
   Results.FlowBatchLotRemoveReqResult flowBatchFillInTxDSC005(Infos.ObjCommon objCommon, ObjectIdentifier flowBatchID);

   /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/3                          Wind
     * @param objCommon
     * @param equipmentID
     * @param floatingBatchListInqResult
     * @return RetCode<ObjectIdentifier>
     * @author Wind
     * @date 2019/1/3 17:25
     */
   ObjectIdentifier flowBatchSelectForEquipmentDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, Results.FloatingBatchListInqResult floatingBatchListInqResult);

   /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * @author light
    * @date 2019/11/22 11:25
    * @param objCommon
    * @param tempFlowBatch -
    * @return java.util.List<com.fa.cim.dto.Infos.FlowBatchByManualActionReqCassette>
    */
   List<Infos.FlowBatchByManualActionReqCassette> tempFlowBatchSelectForEquipmentDR(Infos.ObjCommon objCommon, Inputs.ObjTempFlowBatchSelectForEquipmentDRIn tempFlowBatch);

   /**     
    * description:
    * change history:
    * date             defect             person             comments
    * -----------------------------------------------------------------------------------------------------------------
    * @author Bear
    * @date 2019/6/18 17:53
    * @param  -  
    * @return com.fa.cim.common.support.RetCode<com.fa.cim.common.support.ObjectIdentifier>
    */
   ObjectIdentifier lotFlowBatchIDGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

   /**
    * description:
    * change history:
    * date             defect#             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2019/7/9                               Neyo                create file
    *
    * @author: Neyo
    * @date: 2019/7/9 14:32
    * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
    */
    Outputs.ObjFlowBatchCheckConditionForCassetteDeliveryOut flowBatchCheckConditionForCassetteDelivery(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, Results.WhatNextLotListResult strWhatNextInqResult);
}
