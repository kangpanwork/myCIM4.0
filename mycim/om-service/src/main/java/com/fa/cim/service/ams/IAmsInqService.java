package com.fa.cim.service.ams;

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
 * @date: 2020/9/9 9:49
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAmsInqService {
    Results.OmsMsgInqResult sxOmsMsgInfoInq(Infos.ObjCommon objCommon, Params.OmsMsgInfoInqParams params);

    List<Infos.UsersForOMS> usersForOMS();

    List<Infos.UserGroupForOMS> userGroupsForOMS();

    List<Infos.AlarmCategory> alarmCategory();
}
