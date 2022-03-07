package com.fa.cim.controller.interfaces.probe;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/11       ********            Jerry_Huang                 create file
 *
 * @author: Jerry_Huang
 * @date: 2020/11/11
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProbeController {

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/11       *******             Jerry_Huang        create file
     *
     * @author: Jerry_Huang
     * @date:  2020/11/11
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response ProbeStatusChangeRpt(Params.ProbeStatusChangeParams probeStatusChangeParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param probeUsageCountResetReqParams
     * @return com.fa.cim.common.support.Response
     * @exception 
     * @author ho
     * @date 2020/11/11 18:16
     */
    Response probeUsageCountResetReq(@RequestBody Params.ProbeUsageCountResetReqParams probeUsageCountResetReqParams);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param probeStatusMultiChangeRptParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author Jerry_Huang
     * @date 2020/11/11 18:16
     */
    Response probeStatusMultiChangeRpt(@RequestBody Params.ProbeStatusMultiChangeRptParams probeStatusMultiChangeRptParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param probeXferStatusChangeRptParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author Jerry_Huang
     * @date 2020/11/15 18:16
     */
    Response probeXferStatusChangeRpt(@RequestBody Params.probeXferStatusChangeRptParams probeXferStatusChangeRptParams);
}
