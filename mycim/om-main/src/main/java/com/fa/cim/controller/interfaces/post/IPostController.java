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
 * @date: 2019/7/31 9:37
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPostController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/7        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/5/7 17:07
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response postTaskExecuteReq(Params.PostTaskExecuteReqParams postTaskExecuteReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/7        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/5/7 15:05
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response postTaskRegisterReq(Params.PostTaskRegisterReqParams postTaskRegisterReqParams);
    
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/28 11:04
     * @param postActionModifyReqParams -
     * @return com.fa.cim.common.support.Response
     */
    Response postActionModifyReq(Params.PostActionModifyReqParams postActionModifyReqParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/16 13:50
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response postFilterCreateForExtReq(Params.PostFilterCreateForExtReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/16 13:50
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response postFilterRemoveForExtReq(Params.PostFilterRemoveForExtReqParams params);

    /**
     * description: sxLotCassettePostProcessForceDeleteReq
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params - params
     * @return result
     * @author YJ
     * @date 2021/3/11 0011 20:51
     */
    Response lotCassettePostProcessForceDeleteReq(Params.StrLotCassettePostProcessForceDeleteReqInParams params);
}