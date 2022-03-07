package com.fa.cim.controller.interfaces.autoMonitor;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.am.AllRecipeByProductSpecificationInqParam;
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
public interface IAutoMonitorInqController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/14        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2019/3/14 14:13
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response amJobListInq(Params.AMJobListInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/14        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2019/3/14 13:28
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response amListInq(Params.AMListInqParams monitorListInqParams);

    /**
     * description:
     * <p>WhatNextAMLotInqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/8        ********             Scott               create file
     *
     * @author: Scott
     * @date: 2019/1/8 10:40
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response whatNextAMLotInq(Params.WhatNextAMLotInqInParm parm);

    Response allRecipeByProductSpecificationInq(@RequestBody AllRecipeByProductSpecificationInqParam params);
}
