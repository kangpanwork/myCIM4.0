package com.fa.cim.tms.service.impl;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 13:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class AccessControlCheckInqService implements IAccessControlCheckInqService {
    @Autowired
    private IOMSManager omsManager;

    public Results.AccessControlCheckInqResult sxAccessControlCheckInq(Infos.ObjCommon objCommon, Params.AccessControlCheckInqParam priorityChangeReqParam) {
        Results.AccessControlCheckInqResult result = new Results.AccessControlCheckInqResult();
        log.info("txAccessControlCheckInq Request Json" + JSON.toJSONString(priorityChangeReqParam));
        String subSystemID = priorityChangeReqParam.getSubSystemID();
        String categoryID = priorityChangeReqParam.getCategory();
        log.info("【step1】 - omsManager.sendLoginCheckInq");
        Results.LoginCheckInqResult loginCheckInqResult = omsManager.sendLoginCheckInq(objCommon, subSystemID, categoryID);

        Optional.ofNullable(loginCheckInqResult.getSubSystemFuncLists()).ifPresent(funcLists -> funcLists.forEach(funcList -> {
            Results.PrivilegeCheck tempPrivilegeCheck = new Results.PrivilegeCheck();
            tempPrivilegeCheck.setSubSystemID(funcList.getSubSystemID());
            Optional.ofNullable(funcList.getStrFuncIDs()).ifPresent(funcIDS -> funcIDS.forEach(funcID -> {
                Results.PrivilegeCategory tempPrivilegeCategory = new Results.PrivilegeCategory();
                tempPrivilegeCategory.setCategoryID(funcID.getCategoryID());
                tempPrivilegeCategory.setFunctionID(funcID.getFunctionID());
                tempPrivilegeCategory.setPermission(funcID.getPermission());
                tempPrivilegeCheck.getPrivilegeCategoryData().add(tempPrivilegeCategory);
            }));
            result.getPrivilegeCheckData().add(tempPrivilegeCheck);
        }));
        return result;
    }
}
