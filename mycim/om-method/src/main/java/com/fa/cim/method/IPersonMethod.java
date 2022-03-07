package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * This file use to define the IPersonMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/11        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/10/11 15:16
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPersonMethod {
    /**
     * description:
     *  method:person_LogOnCheck
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/11 15:17
     * @param objCommon -
     * @param user -
     * @param subSystemID -
     * @param categoryID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.LoginCheckInqResult>
     */
    Results.LoginCheckInqResult personLogOnCheck(Infos.ObjCommon objCommon, User user, String subSystemID, String categoryID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/12 11:10
     * @param objCommon -
     * @param userID -
     * @return com.fa.cim.pojo.Outputs.ObjPersonAllowProductListGetOut
     */
    Outputs.ObjPersonAllowProductListGetOut personAllowProductListGet(Infos.ObjCommon objCommon, ObjectIdentifier userID);

    /**
     * description:
     *  method: person_userGroupList_GetDR
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/12 15:30
     * @param objCommon -
     * @param userID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjPersonUserGroupListGetOut>
     */
    List<ObjectIdentifier> personUserGroupListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier userID);

    /**
     * description:
     *  method:person_productGroupList_GetDR
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/14 15:03
     * @param objCommon -
     * @param userGroupIDList -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjPersonProductGroupListGetOut>
     */
    Outputs.ObjPersonProductGroupListGetOut personProductGroupListGetDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> userGroupIDList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/14 16:01
     * @param objCommon -
     * @param productGroupIDList -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjPersonProductListGetOut>
     */
    Outputs.ObjPersonProductListGetOut personProductListGetDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> productGroupIDList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/14 16:42
     * @param objCommon -
     * @param userID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjPersonAllowMachineRecipeListGetOut>
     */
    Outputs.ObjPersonAllowMachineRecipeListGetOut personAllowMachineRecipeListGet(Infos.ObjCommon objCommon, ObjectIdentifier userID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/14 17:15
     * @param objCommon -
     * @param userGroupIDList -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjPersonAllowMachineRecipeListGetOut>
     */
    Outputs.ObjPersonAllowMachineRecipeListGetOut personMachineRecipeListGetDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> userGroupIDList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/15 10:48
     * @param objCommon -
     * @param user -
     * @param equipmentID -
     * @param stockerID -
     * @param productIDList -
     * @param routeIDList -
     * @param lotIDList -
     * @param machineRecipeIDList -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void personPrivilegeCheckDR(Infos.ObjCommon objCommon, User user, ObjectIdentifier equipmentID
            , ObjectIdentifier stockerID, List<ObjectIdentifier> productIDList, List<ObjectIdentifier> routeIDList
            , List<ObjectIdentifier> lotIDList, List<ObjectIdentifier> machineRecipeIDList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/16 11:14
     * @param userID -
     * @param password -
     * @param newPassword -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void changePassword(String userID, String password, String newPassword);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/18 14:53
     * @param objCommon -
     * @param userID -
     * @param productID -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void personCheckProductPrivilegeDR(Infos.ObjCommon objCommon, ObjectIdentifier userID, ObjectIdentifier productID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/19 14:30
     * @param objCommon -
     * @param userID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.BasicUserInfoInqResult>
     */
    Results.BasicUserInfoInqResult personFillInTxPLQ013DR(Infos.ObjCommon objCommon, ObjectIdentifier userID);

    /**
     * description:
     * <p></p>
     * change history:
     * date defect person
     comments
     * ------------------------------------------------------------
     ---------------------------------------------------------
     *
     * @return Results.CDAInfoInqResult
     * @author Arnold
     * @since 2018/11/26 10:49:57
     */
    Outputs.ObjUserDefinedAttributeInfoGetDROut userDefinedAttributeInfoGetDR(Infos.ObjCommon objCommon, String classID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param classID classID
     * @return List<Infos.UserDefinedData>
     * @author Paladin
     * @date 2018/11/2
     */
    List<Infos.UserDefinedData> getUserDefinedAttributeDR(Infos.ObjCommon objCommon, String classID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 11:03
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    String personExistenceCheck(Infos.ObjCommon objCommon, ObjectIdentifier userId);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/20 10:16                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/10/20 10:16
     * @param  -
     * @return java.util.List<com.fa.cim.dto.Infos.UserList>
     */
    List<Infos.UsersForOMS> users();
    
    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/20 10:40                     Nyx                Create
     *       
     * @author Nyx
     * @date 2020/10/20 10:40
     * @param  -
     * @return java.util.List<com.fa.cim.dto.Infos.UserGroupList>
     */
    List<Infos.UserGroupForOMS> userGroups();
}