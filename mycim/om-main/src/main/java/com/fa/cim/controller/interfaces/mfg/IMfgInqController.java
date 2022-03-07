package com.fa.cim.controller.interfaces.mfg;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import com.fa.cim.mfg.MfgInfoExportParams;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
public interface IMfgInqController {
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
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/11/10/010 14:53
     */
    Response mfgRestrictListByEqpInq( Params.MfgRestrictListByEqpInqParams mfgRestrictListByEqpInqParams) ;

    /**
     * description:This function relates ProductOrderReleasedListInq.
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Panda
     * @date: 2018/5/10
     * @return
     */
    Response subLotTypeIdListInq(@RequestBody Params.SubLotTypeListInqParams subLotTypeListInqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/11/9/009 10:56
     */
    Response recipeTimeLimitListInq(Params.RecipeTimeInqParams params);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/5/10 18:17                      Decade                Create
    *
    * @author Decade
    * @date 2021/5/10 18:17
    * @param null -
    * @return
    */
    void mfgInfoExportInq( MfgInfoExportParams mfgInfoExportParams, HttpServletResponse httpServletResponse) throws IOException;
}