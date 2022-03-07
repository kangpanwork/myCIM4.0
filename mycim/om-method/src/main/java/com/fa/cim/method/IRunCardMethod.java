package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.newcore.dto.product.ProductDTO;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/15                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/15 17:03
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IRunCardMethod {
    /**
     * description: get run card infomation by runCardID
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/7/2 13:35
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Infos.RunCardInfo getRunCardInfo(Infos.ObjCommon objCommon,String runCardID);


    /**
     * description: get run card approvers by userGourpID
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/8/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/8/3 10:56
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<String> getApproveUsersFromUserGroupAndFunction(String userGroupID,String functionID);

    /**
     * description: get run card information by lotID
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/7/2 13:37
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Infos.RunCardInfo getRunCardFromLotID(ObjectIdentifier lotID);

    /**
     * description: update/modify/delete run card infomation method
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/7/2 13:37
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void updateRunCardInfo(Infos.ObjCommon objCommon,Infos.RunCardInfo runCardInfo);

    /**
     * description: remove run card infomation by psmJobID and psmKey
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/7/2 13:37
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void removePsmDocRunCardInfo(Infos.ObjCommon objCommon,String psmJobID,String psmKey,String runCardID);

    /**
     * description: remove all the run card information, psm/doc information by runCardID
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/7/2 13:38
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void removeAllRunCardInfo(Infos.ObjCommon objCommon,String runCardID,Boolean removeFRRUNCARDFlag);


    /**
     * description: Check if it is a runCard lot
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/6/29 13:59                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/6/29 13:59
     * @param waferIDs -
     * @return boolean
     * @see Infos.RunCardInfo getRunCardInfoByDoc(Infos.ObjCommon objCommon,String docID)
     */
    @Deprecated
    boolean isRunCard(List<ObjectIdentifier> waferIDs);

    /**
     * description: if the lot had been splited ,query the runCard infomation by wafers
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/7/2 13:38
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     * @see Infos.RunCardInfo getRunCardInfoByPsm(Infos.ObjCommon objCommon,String psmJobID)
     */
    @Deprecated
    Infos.RunCardInfo getSplitLotRunCardInfo(List<ObjectIdentifier> waferIDs);

    /**
     * description: get runcard infomation by psm
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/10/15                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/10/15 23:41
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Infos.RunCardInfo getRunCardInfoByPsm(Infos.ObjCommon objCommon,String psmJobID);

    /**
     * description: get runcard information by doc
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/10/15                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/10/15 23:42
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Infos.RunCardInfo getRunCardInfoByDoc(Infos.ObjCommon objCommon,Infos.FPCInfo docInfo);

    /**
     * description: set RUNCARD_PSM table psmKey
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/7/2 13:40
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void setPsmKeyFromPsmInfo(ProductDTO.PlannedSplitJobInfo aPlannedSplitJobInfo);

    /**
     * description: get run card psmKey information (subrouteID/splitOperationNumber/waferList) by psmKey
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/7/2 13:42
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Infos.RunCardPsmKeyInfo getRunCardPsmKeyInfo(String splitOperationNumber, ObjectIdentifier subRouteID, List<ObjectIdentifier> waferIDs, String psmKey);


    /**
     * description: remove run card RUNCARD_PSM/RUNCARD_PSM_DOC data by psmKey
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/7/2 13:44
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void removeRunCardPsmDocFromPsmKey(Infos.ObjCommon objCommon,String changePsmKey,String removeDocPsmKey,Boolean removeDocFlag,String runCardID);
    
    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/2 16:38                     Nyx                Create
     *       
     * @author Nyx
     * @date 2020/7/2 16:38
     * @param docJobID -
     * @return void
     */
    void removeRunCardPsmDocFromDocJobID(String docJobID);

    /**
     * description: run card complete action auto by postProcess if all the split lot have return the base operation route
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/8/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/8/11 15:46
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void runCardAutoCompleteAction(Infos.ObjCommon objCommon, ObjectIdentifier lotID);



    /**
     * description: make psm history event by runcard in some delete doc sisuation case
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/8/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/8/19 17:56
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void makePsmHistoryEventByRunCard(Infos.ObjCommon objCommon,
                                      String psmJobID,
                                      ObjectIdentifier lotFamilyID,
                                      ObjectIdentifier splitRouteID,
                                      String splitOperationNumber,
                                      String originalOperationNumber,
                                      ObjectIdentifier originalRouteID,
                                      ProductDTO.PlannedSplitJobInfo plannedSplitJobInfo,
                                      String runCardID);

    /**
     * description: get run card psm info by psmKey
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/8/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/8/19 17:56
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Infos.RunCardPsmKeyInfo getRunCardPsmKeyInfoByPSMKey(String psmKey);
}
