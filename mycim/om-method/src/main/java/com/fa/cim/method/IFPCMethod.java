package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/3/21       ********              Nyx             create file
 *
 * @author: Nyx
 * @date: 2019/3/21 16:10
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IFPCMethod {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param in        -
     * @return com.fa.cim.common.support.RetCode<java.util.List   <   com.fa.cim.dto.Infos.FPCInfo>>
     * @author Nyx
     * @date 2019/3/22 15:01
     */
    List<Infos.FPCInfo> fpcInfoGetDR(Infos.ObjCommon objCommon, Inputs.ObjFPCInfoGetDRIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID     -
     * @return com.fa.cim.common.support.RetCode<java.util.List   <   com.fa.cim.dto.Infos.FPCDispatchEqpInfo>>
     * @author Nyx
     * @date 2019/3/27 17:03
     */
    List<Infos.FPCDispatchEqpInfo> fpcLotDispatchEquipmentsInfoCreate(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param restrictEqp
     * @param lotID
     * @param equipmentIDs -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @author Nyx
     * @date 2019/3/27 17:06
     */
    void fpcLotDispatchEquipmentsSet(Infos.ObjCommon objCommon, Boolean restrictEqp, ObjectIdentifier lotID, List<ObjectIdentifier> equipmentIDs);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param fpcInfos
     * @param recipeParmCheckFlag
     * @param dcSpecItemCheckFlag -
     * @return com.fa.cim.common.support.RetCode<java.util.List   <   com.fa.cim.dto.Infos.FPCDispatchEqpInfo>>
     * @author Nyx
     * @date 2019/3/27 18:24
     */
    List<Infos.FPCDispatchEqpInfo> fpcInfoConsistencyCheck(Infos.ObjCommon objCommon, List<Infos.FPCInfo> fpcInfos, Boolean recipeParmCheckFlag, Boolean dcSpecItemCheckFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param FPCCategory
     * @param fpcProcessConditions
     * @param FPCCategoryCheckFlag
     * @param whiteDefCheckFlag    -
     * @return com.fa.cim.common.support.RetCode<java.util.List   <   com.fa.cim.dto.Infos.FPCProcessCondition>>
     * @author Nyx
     * @date 2019/3/28 9:41
     */
    List<Infos.FPCProcessCondition> fpcProcessConditionCheck(Infos.ObjCommon objCommon, String FPCCategory, List<Infos.FPCProcessCondition> fpcProcessConditions, Boolean FPCCategoryCheckFlag, Boolean whiteDefCheckFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param FPCCategory
     * @param fpcProcessConditions -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @author Nyx
     * @date 2019/3/28 17:44
     */
    void fpcCategoryConditionCheck(Infos.ObjCommon objCommon, String FPCCategory, List<Infos.FPCProcessCondition> fpcProcessConditions);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @author Nyx
     * @date 2019/3/28 17:46
     */
    void fpcWhiteProcessConditionCheck(Infos.ObjCommon objCommon, List<Infos.FPCProcessCondition> fpcProcessConditions);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param strFPCInfoActionList -
     * @return com.fa.cim.common.support.RetCode<java.util.List   <   com.fa.cim.dto.Infos.FPCInfoAction>>
     * @author Nyx
     * @date 2019/4/4 16:40
     */
    List<Infos.FPCInfoAction> fpcInfoMerge(Infos.ObjCommon objCommon, List<Infos.FPCInfoAction> strFPCInfoActionList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param strFPCInfoActionList -
     * @return com.fa.cim.common.support.RetCode<java.util.List   <   com.fa.cim.dto.Infos.FPCInfoAction>>
     * @author Nyx
     * @date 2019/4/4 16:47
     */
    List<Infos.FPCInfoAction> fpcInfoConsistencyCheckForUpdate(Infos.ObjCommon objCommon, List<Infos.FPCInfoAction> strFPCInfoActionList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param strFPCInfoActionList -
     * @return com.fa.cim.common.support.RetCode<java.util.List   <   com.fa.cim.dto.Infos.FPCInfoAction>>
     * @author Nyx
     * @date 2019/4/8 10:58
     */
    List<Infos.FPCInfoAction> fpcInfoUpdateDR(Infos.ObjCommon objCommon, List<Infos.FPCInfoAction> strFPCInfoActionList);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/2 16:19
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void fpcInfoDeleteDR(Infos.ObjCommon objCommon, List<String> fpcIDs);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param strFPCInfo
     * @param routeID    -
     * @return com.fa.cim.common.support.RetCode<java.lang.String>
     * @author Nyx
     * @date 2019/4/9 15:12
     */
    String fpcRouteOperationConsistencyCheck(Infos.ObjCommon objCommon, Infos.FPCInfo strFPCInfo, ObjectIdentifier routeID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param recipeParmNames -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @author Nyx
     * @date 2019/4/9 16:18
     */
    void fpcRecipeParameterConsistencyCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<String> recipeParmNames);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param dcSpecID
     * @param dcSpecItems -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @author Nyx
     * @date 2019/4/9 16:39
     */
    void fpcDcSpecItemConsistencyCheck(Infos.ObjCommon objCommon, ObjectIdentifier dcSpecID, List<String> dcSpecItems);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/2 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjFPCCheckConditionForUpdateOut fpcCheckConditionForUpdate(Infos.ObjCommon objCommon, Inputs.FPCCheckConditionForUpdateIn in);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/3                             Zack               create file
     *
     * @author: Zack
     * @date: 2019/6/3 14:10
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.FPCInfo> fpcListGetDR(Infos.ObjCommon objCommon, Inputs.ObjFPCListGetDRIn fpcListGetDRIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/6/11 14:36
     * @param objCommon
     * @param exchangeType
     * @param equipmentID
     * @param startCassetteList -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.StartCassette>>
     */
    List<Infos.StartCassette> fpcStartCassetteInfoExchange(Infos.ObjCommon objCommon, String exchangeType, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/6/11 14:35
     * @param equipmentID
     * @param strStartCassette
     * @param FPCCategoryCheckFlag
     * @param whiteDefCheckFlag -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.FPCProcessCondition>>
     */
    List<Infos.FPCProcessCondition> fpcStartCassetteProcessConditionCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette, Boolean FPCCategoryCheckFlag, Boolean whiteDefCheckFlag);


    /**
     * description: EDC information write add DOC control only by moveInReserveReq edc part
     *              exchangeTpye is "StartReserveReq"
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/8/30                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/8/30 10:54
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjProcessDataCollectionDefinitionGetOut fpcDCInfoExchangeByEDCSet(Infos.ObjCommon objCommon,
                                         String exchangeType,
                                         ObjectIdentifier equipmentID,
                                         ObjectIdentifier lotID,
                                         Outputs.ObjProcessDataCollectionDefinitionGetOut edcInformation);
}
