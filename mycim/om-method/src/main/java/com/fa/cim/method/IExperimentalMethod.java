package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/22          ********            light                create file
 *
 * @author: light
 * @date: 2019/11/22 13:15
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IExperimentalMethod {

    /**
     * description:
     * change history: add run card
     *                 add return psmJobID for history
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/22 下午 12:23
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    String experimentalLotInfoDelete(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber,String originalPsmKey,String runCardID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/26                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/26 10:44
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void experimentalLotInfoCheck(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber, List<Infos.ExperimentalLotDetailInfo> strExperimentalLotDetailInfoSeq);
    /**
     * description:
     *   2020/09/01   add Input param actionSeparateHold/actionCombineHold support for auto separate and combine  - jerry
     *
     *
     * change history: add change psmkey
     *                 add return param pamJobID for history
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/26                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/26 15:42
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    String experimentalLotInfoUpdate(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber, Boolean actionEMail, Boolean actionHold, String testMemo, Boolean execFlag,
                                   String actionTimeStamp, String modifyTimeStamp, String modifyUserID, List<Infos.ExperimentalLotDetailInfo> strExperimentalLotDetailInfoSeq,List<String> originalPsmKeys,String modifyPsmKey,String removePsmKey, Boolean actionSeparateHold, Boolean actionCombineHold);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/30                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/30 13:28
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void experimentalLotStateCheck(Infos.ObjCommon objCommon, ObjectIdentifier lotId);

    /**
     * description:
     * 2020/09/01 add support for auto separate and combine  - jerry
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/30                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/30 14:03
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjExperimentalLotInfoGetOut experimentalLotInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotId);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/5 15:18
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjExperimentalLotActualInfoCreateOut experimentalLotActualInfoCreate(Infos.ObjCommon objCommon, ObjectIdentifier lotId, Infos.ExperimentalLotInfo strExperimentalLotInfo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param lotFamily
     * @param splitRoute
     * @param splitOperationNumber
     * @param originalRoute
     * @param originalOperationNumber
     * @param execCheckFlag
     * @param detailRequireFlag
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.ExperimentalLotInfo>>
     * @author Ho
     * @date 2018/12/5 11:25:35
     */
    List<Infos.ExperimentalLotInfo> experimentalLotListGetDR(Infos.ObjCommon strObjCommonIn, String lotFamily, String splitRoute, String splitOperationNumber, String originalRoute, String originalOperationNumber, Boolean execCheckFlag, Boolean detailRequireFlag);

}