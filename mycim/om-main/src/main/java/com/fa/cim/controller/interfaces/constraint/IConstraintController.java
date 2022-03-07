package com.fa.cim.controller.interfaces.constraint;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 9:56
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IConstraintController {
    /**
     * description:
     * The method use to define the MfgRestrictCancelReqController.
     * transaction ID: OCONW002
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/15        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/15 16:10
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response mfgRestrictCancelReq(Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams);

    /**
     * description:
     * The method use to define the MfgRestrictReqController.
     * transaction ID: OCONW001
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/11        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/11 16:54
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response mfgRestrictReq(Params.MfgRestrictReqParams mfgRestrictReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 11/28/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 11/28/2018 4:17 PM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response mfgRestrictExclusionLotCancelReq(Params.MfgRestrictExclusionLotReqParams cancelParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 11/27/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 11/27/2018 5:43 PM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response mfgRestrictExclusionLotReq(Params.MfgRestrictExclusionLotReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/8 19:37                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/8 19:37
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response constraintEqpAddReq(Params.ConstraintEqpAddReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/8 19:37                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/8 19:37
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response constraintEqpModifyReq(Params.ConstraintEqpModifyReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/8 19:37                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/8 19:37
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response constraintEqpCancelReq(Params.ConstraintEqpCancelReqParams params);

}