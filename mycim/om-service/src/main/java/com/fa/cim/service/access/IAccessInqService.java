package com.fa.cim.service.access;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/9        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/9 14:22
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAccessInqService {
    Results.LoginCheckInqResult sxLoginCheckInq(Infos.ObjCommon objCommon, Params.LoginCheckInqParams params);

    void sxAccessControlCheckInq(Infos.ObjCommon objCommon, Params.AccessControlCheckInqParams params);

    Infos.ObjCommon checkPrivilegeAndGetObjCommon(String transactionID, User user, Params.AccessControlCheckInqParams accessControlCheckInqParams);

    List<Results.BasicUserInfoInqResult> sxAllUserInfoInq();

    Results.BasicUserInfoInqResult sxBasicUserInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier userID);
}
