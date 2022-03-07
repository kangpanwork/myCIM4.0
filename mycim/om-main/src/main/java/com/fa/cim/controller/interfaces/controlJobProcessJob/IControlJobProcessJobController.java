package com.fa.cim.controller.interfaces.controlJobProcessJob;

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
 * @date: 2019/7/31 10:33
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IControlJobProcessJobController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/9/5        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/9/5 15:56
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response cjStatusChangeReq(Params.CJStatusChangeReqParams cjStatusChangeReqParams);

    /**
     * description:
     * // This transaction is reported from EAP after process jobs are created for a controlJob.
     * // This transaction is used for equipment OnlineRemote mode only.
     * // MMS will save the information in FRPO_SMPL table
     * // For SLM operation, this transaction will save process jobID to equipment container.
     * // For Process Job Level Control Equipment, MMS will create process job event and notify the information to DCS/APC
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params -
     * @return com.fa.cim.common.support.Response
     * @author Nyx
     * @date 2019/7/12 16:16
     */
    Response pjInfoRpt(Params.PJInfoRptParams params);

    /**
     * description: This function requests cassette unclamp when equipment is online The request will be passed to EAP to perform process job actions
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params -
     * @return com.fa.cim.common.support.Response
     * @author Nyx
     * @date 2019/7/12 16:21
     */
    Response pjStatusChangeReq(Params.PJStatusChangeReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/24       ********              Jerry             create file
     *
     * @author: Jerry
     * @date: 2019/7/24 10:06
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response pjStatusChangeRpt(Params.PJStatusChangeRptInParm params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/1/3 15:07
     * @param processJobMapInfoRptParam -
     * @return com.fa.cim.common.support.Response
     */
    Response processJobMapInfoRpt(Params.ProcessJobMapInfoRptParam processJobMapInfoRptParam);

}