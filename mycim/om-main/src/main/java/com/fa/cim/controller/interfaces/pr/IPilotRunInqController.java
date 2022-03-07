package com.fa.cim.controller.interfaces.pr;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/17          ********            jerry              create file
 *
 * @author: Jerry
 * @date: 2020/12/17 14:43
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPilotRunInqController {

    Response pilotRunList(Params.PilotRunInqParams params);

    Response pilotRunJobInfo(Params.PilotRunJobInfoParams params);

    Response pilotJobListInq(com.fa.cim.pr.Params.PilotJobInfoParams params);

    Response pilotRecipeGroupListInq(com.fa.cim.pr.Params.PilotRunRecipeGroupParams params);

    Response pilotRecipeListInq(com.fa.cim.pr.Params.EquipmentParams params);

}
