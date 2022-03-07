package com.fa.cim.controller.interfaces.probe;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/15         ********            Jerry_Hunag                create file
 *
 * @author: Jerry_Huang
 * @date 2020/11/15
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProbeInqController {
    /**
     * description:
     * The method use to define the BankListInq controller.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/5       ********            Jerry_Huang         create file
     *
     * @author: Jerry_Huang
     * @date: 2020/11/5
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response probeListInq(Params.ProbeListInqParams probeListInqParams);

    /**
     * description:
     * The method use to define the BankListInq controller.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6       ********            Jerry_Huang         create file
     *
     * @author: Jerry_Huang
     * @date: 2020/11/6
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response probeIDListInq(Params.ProbeIDListInqParams probeIDListInqParams);


    /**
     * description:
     * The method use to define the BankListInq controller.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6       ********            Jerry_Huang         create file
     *
     * @author: Jerry_Huang
     * @date: 2020/11/6
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response probeStatusInq(Params.ProbeStatusParams probeStatusParams);

    /**
     * description:
     * The method use to define the BankListInq controller.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6       ********            Jerry_Huang         create file
     *
     * @author: Jerry_Huang
     * @date: 2020/11/6
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response probeStockerInfoInq(Params.ProbeStockerInfoInq ProbeStockerInfoInq);

    /**
     * description:
     * The method use to define the BankListInq controller.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6       ********            Jerry_Huang         create file
     *
     * @author: Jerry_Huang
     * @date: 2020/11/6
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response probeGroupIDListInq(Params.probeGroupIdListParams probeGroupIdListParams);

}
