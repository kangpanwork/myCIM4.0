package com.fa.cim.controller.interfaces.ams;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>IAmsInqController .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/7/27/027   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/7/27/027 17:38
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAmsInqController {

    Response OmsMsgInfoInq(@RequestBody Params.OmsMsgInfoInqParams params);

    Response usersForOMS();

    Response userGroupsForOMS();

    Response alarmCategory();
}
