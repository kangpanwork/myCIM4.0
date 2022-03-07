package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>IDataCollectionMethod .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/12        ********             PlayBoy               create file
 *
 * @author PlayBoy
 * @since 2018/10/12 11:23
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDataCollectionMethod {

    /**
     * description:
     * <p>This object function fill the return structure's value based on po object of each lot.
     * Set return value of OEDCQ001 by following logic.<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon             objCommon
     * @param equipmentID           equipmentID
     * @param processJobControlFlag processJobControlFlag
     * @param controlJobID          controlJobID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/12 11:27:32
     */
    Results.EDCDataItemWithTransitDataInqResult dataCollectionDefinitionFillInTxDCQ002DR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, boolean processJobControlFlag, ObjectIdentifier controlJobID);

    /**
     * description: dcDef_detailInfo_GetDR
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param dcDefID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.EDCPlanInfoInqResult>
     * @author Ho
     * @date 2018/10/16 13:55:24
     */
    Results.EDCPlanInfoInqResult dcDefDetailInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier dcDefID);

    /**
     * description: dcSpec_detailInfo_GetDR__101
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param dcSpecID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.EDCSpecInfoInqResult>
     * @author Ho
     * @date 2018/10/16 16:18:51
     */
    Results.EDCSpecInfoInqResult dcSpecDetailInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier dcSpecID);

    /**
     * description:
     * <p>dataCollectionItem_FillInTxDCQ011DR: Get all of Hold Record by specified durableID.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon        objCommon
     * @param searchKeyPattern searchKeyPattern
     * @param searchKeys       searchKeys
     * @return RetCode
     * @author PlayBoy
     * @date 2018/11/21 13:23:36
     */
    Outputs.ObjDataCollectionItemFillInEDCDataItemListByKeyInqOut dataCollectionItemFillInTxDCQ011DR(Infos.ObjCommon objCommon, String searchKeyPattern, List<Infos.HashedInfo> searchKeys);
    
     /**
      * description:
      * <p>Get DC DefID List.</p>
      * change history:
      * date             defect             person             comments
      * -------------------------------------------------------------------------------------------------------------------
      * @param objCommon  objCommon
      * @param dcType  dcType
      * @param objectID  objectID
      * @param FPCCategory  FPCCategory
      * @param whiteDefSearchCriteria  whiteDefSearchCriteria
      * @param maxCount  maxCount
      * @return RetCode<List<Infos.DataCollection>>
      * @author ZQI
      * @date 2018/12/11 16:54:11
     */
    List<Infos.DataCollection> dcDefListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier objectID, String dcType, String whiteDefSearchCriteria, Long maxCount, String FPCCategory);

     /**
      * description:
      * <p>This object function gets the data collection information.</p>
      * <p>The data collection/specification Identifier is gotten from lotID and pdID, etc..</p>
      * change history:
      * date             defect             person             comments
      * -------------------------------------------------------------------------------------------------------------------
      * @param objCommon  objCommon
      * @param equipmentID  equipmentID
      * @param lotID  lotID
      * @param machineRecipeID  machineRecipeID
      * @param pdID  pdID
      * @param fpcCategory  fpcCategory
      * @param whiteDefSearchCriteria  whiteDefSearchCriteria
      * @return RetCode<List<Infos.DataCollection>>
      * @author ZQI
      * @date 2018/12/11 17:55:23
     */
     List<Infos.DataCollection> dcDefListGetFromPD(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier equipmentID, ObjectIdentifier machineRecipeID, ObjectIdentifier pdID, String whiteDefSearchCriteria, String fpcCategory);

    /**
     * description:
     * <p>Get DC SpecID List.</p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon              objCommon
     * @param objectID               objectID
     * @param dcDefID                dcDefID
     * @param fpcCategory            fpcCategory
     * @param whiteDefSearchCriteria whiteDefSearchCriteria
     * @param maxCount               maxCount
     * @return RetCode<List<Infos.DataCollection>>
     * @author ZQI
     * @date 2018/12/11 18:02:30
     */
    List<Infos.DataCollection> dcSpecListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier dcDefID, ObjectIdentifier objectID, String whiteDefSearchCriteria, Long maxCount, String fpcCategory);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param inputs    -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @author Sun
     * @date 4/11/2019 3:09 PM
     */
    void collectedDataCheckConditionForDataStore(Infos.ObjCommon objCommon, Inputs.CollectedDataCheckConditionForDataStoreIn inputs);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strProcessCollectedDataUpdateIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/20 13:55
     */
    void processCollectedDataUpdate(Infos.ObjCommon strObjCommonIn, Infos.ProcessCollectedDataUpdateIn strProcessCollectedDataUpdateIn);
}
