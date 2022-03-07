package com.fa.cim.controller.interfaces.constraint;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 10:00
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IConstraintInqController {
    /**
     * description:
     * The method use to define the MfgRestrictListInqController.
     * transaction ID: OCONQ001
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/09/28        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/09/28 10:18
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response mfgRestrictListInq(Params.MfgRestrictListInqParams mfgRestrictListInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 11/27/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 11/27/2018 9:45 AM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response mfgRestrictExclusionLotListInq(Params.MfgRestrictExclusionLotListInqParams params);

    /**
     * description:
     * The method use to define the RouteListInqController.
     * transaction ID: OCONQ003
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/24        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/24 11:26
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response moduleAllProcessStepListInq(Params.RouteListInqParams params);

    /**
     * description:
     * The method use to define the StageListInqController.
     * transaction ID: OCONQ004
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/24        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/24 16:01
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response stageListInq(Params.StageListInqParams params);

    /**
     * description: constraint list
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/8 16:19                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/8 16:19
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response constraintListByEqpInq(Params.ConstraintListByEqpInqParams params);

    /**
    * description: for search tool constraint history
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/6/11 13:06                       AOKI              Create
    * @author AOKI
    * @date 2021/6/11 13:06
    * @param params
    * @return com.fa.cim.common.support.Response
    */
    Response constraintHistoryListInq(Params.ConstraintHistoryListInqParams params);
}