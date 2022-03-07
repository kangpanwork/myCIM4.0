package com.fa.cim.service.ams.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IAMSMethod;
import com.fa.cim.method.IPersonMethod;
import com.fa.cim.service.ams.IAmsInqService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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
 * @date: 2020/9/9 9:50
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
public class AmsInqServiceImpl implements IAmsInqService {
    @Autowired
    private IAMSMethod amsMethod;

    @Autowired
    private IPersonMethod personMethod;

    public Results.OmsMsgInqResult sxOmsMsgInfoInq(Infos.ObjCommon objCommon, Params.OmsMsgInfoInqParams params){
        return amsMethod.OmsMsgInfoGet();
    }

    @Override
    public List<Infos.UsersForOMS> usersForOMS() {
        return personMethod.users();
    }

    @Override
    public List<Infos.UserGroupForOMS> userGroupsForOMS() {
        return personMethod.userGroups();
    }

    @Override
    public List<Infos.AlarmCategory> alarmCategory() {
        List<Infos.AlarmCategory> alarmCategories = new ArrayList<>();
        alarmCategories.add(new Infos.AlarmCategory(BizConstant.CATEGORY_HOLD_LOT));
        alarmCategories.add(new Infos.AlarmCategory(BizConstant.CATEGORY_CONSTRAINT));
        alarmCategories.add(new Infos.AlarmCategory(BizConstant.CATEGORY_EQP_ALARM));
        alarmCategories.add(new Infos.AlarmCategory(BizConstant.CATEGORY_SYSTEM_MESSAGE));
        return alarmCategories;
    }
}
