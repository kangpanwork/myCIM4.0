package com.fa.cim.controller.interfaces.processControl;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 15:41
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProcessController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/7/18         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/7/18 19:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response futureHoldReq(Params.FutureHoldReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/8/2         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/8/2 15:46
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response futureHoldCancelReq(Params.FutureHoldCancelReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/8       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/11/8 15:23
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response npwUsageStateModifyReq(Params.NPWUsageStateModifyReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/7        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/11/7 16:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lagTimeActionReq(Params.LagTimeActionReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/7        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/11/7 14:20
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response qtimeActionReq(Params.QtimeActionReqParam qtimeActionReqParam);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 11/14/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 11/14/2018 10:20 AM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response qtimerReq(Params.QtimerReqParams params);

    Response qtimeManageActionByPostTaskReq (
            Params.QtimeManageActionByPostTaskReqInParm strQtimeManageActionByPostTaskReqInParm );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param futureReworkCancelReqParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author Ho
     * @date 2019/8/27 15:21
     */
    Response futureReworkCancelReq(Params.FutureReworkCancelReqParams futureReworkCancelReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/31          ********            Nyx                create file
     *
     * @author: Nyx
     * @date: 2019/7/31 15:37
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response processHoldCancelReq(Params.ProcessHoldCancelReq param);

    /**
     * description:
     * <p>This function registers Hold instruction for a specified Process.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/17                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/12/17 15:01
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response processHoldReq(Params.ProcessHoldReq param);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/20 14:32
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response futureReworkReq(Params.FutureReworkReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/24 13:25
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response futureReworkActionDoReq(Params.FutureReworkActionDoReqParams params);

    /**
     * description: future hold edit department
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params - params
     * @return com.fa.cim.common.support.Response
     * @author YJ
     * @date 2021/1/27 0027 15:14
     */
    Response futureHoldDepartmentChangeReq(Params.FutureHoldReqParams params);

    /**
     * description: process hold edit department
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2021/1/27 0027 17:29
     * @param params - params
     * @return com.fa.cim.common.support.Response
     */
    Response processHoldDepartmentChangeReq(@RequestBody Params.ProcessHoldReq params);
}