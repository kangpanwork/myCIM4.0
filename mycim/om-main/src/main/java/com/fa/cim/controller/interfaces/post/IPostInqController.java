package com.fa.cim.controller.interfaces.post;

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
 * @date: 2019/7/31 9:48
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPostInqController {
    /**
     * description:
     * <p>CimObjectListInqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * 2018/12/4        ********               Lin            create file
     *
     * @author: Lin
     * @date: 2018/12/4 10:11
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response cimObjectListInq(Params.CimObjectListInqInParms params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/10 14:24
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response postActionListInq(Params.PostActionListInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/16 18:37
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response postFilterListForExtInq(Params.PostFilterListForExtInqParams params);
}