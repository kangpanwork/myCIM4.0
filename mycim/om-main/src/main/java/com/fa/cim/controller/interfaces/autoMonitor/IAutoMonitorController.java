package com.fa.cim.controller.interfaces.autoMonitor;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/13      ********             Neko                create file
 *
 * @author Neko
 * @since 2019/11/13 18:00
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAutoMonitorController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/22        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2019/3/22 15:35
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response amJobStatusChangeRpt(@RequestBody Params.AMJobStatusChangeRptInParm param);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/9       ********              Jerry             create file
     *
     * @author: Jerry
     * @date: 2019/7/9 13:31
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response amJobLotReserveReq(@RequestBody Params.AMJobLotReserveReqInParams param);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/18        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2019/3/18 13:24
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response amStatusChangeRpt(@RequestBody Params.AMStatusChangeRptInParm param);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/20        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2019/3/20 16:55
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response amSetReq(@RequestBody Params.AMSetReqInParm param);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/2/24 14:28                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/2/24 14:28
     * @param param -
     * @return com.fa.cim.common.support.Response
     */

    Response amScheduleChgReq(@RequestBody Params.EqpMonitorScheduleUpdateInParm param);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/2/24 14:44                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/2/24 14:44
     * @param param -
     * @return com.fa.cim.common.support.Response
     */

    Response amRecoverReq(@RequestBody Params.AMRecoverReqParams param);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/5/19 12:40                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/5/19 12:40
     * @param eqpMonitorUsedCountUpdateReqInParam -
     * @return com.fa.cim.common.support.Response
     */

    Response eqpMonitorWaferUsedCountUpdateReq(@RequestBody Params.EqpMonitorUsedCountUpdateReqInParam eqpMonitorUsedCountUpdateReqInParam);
}
