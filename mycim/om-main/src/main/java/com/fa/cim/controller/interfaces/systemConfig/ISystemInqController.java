package com.fa.cim.controller.interfaces.systemConfig;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 14:31
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISystemInqController {
    /**
     * description:
     * <p>CodeSelectionInqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * 2018/11/21        ********               Lin            create file
     *
     * @author: Lin
     * @date: 2018/11/21 16:31
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response codeSelectionInq(Params.CodeSelectionInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/8        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/10/8 14:10
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response omsEnvInfoInq(Params.OMSEnvInfoInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/3        *******              jerry              create file
     *
     * @author: jerry
     * @date: 2018/5/3 15:06
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reasonCodeListByCategoryInq(Params.ReasonCodeListByCategoryInqParams reasonCodeListByCategoryInqParams);
}