package com.fa.cim.controller.interfaces.fam;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date defect# person comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/4/21 ******** zh create file
 *
 * @author: zh
 * @date: 2021/4/21 16:47
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IFAMInqController {

    /**
     * description:
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/4/21 16:53 zh Create
     *
     * @author zh
     * @date 2021/4/21 4:53 下午
     * @param
     * @return
     */
    Response sortJobHistoryListInq(Params.SortJobHistoryParams sortJobHistoryParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/4/27 15:23                     Nyx                Create
     *
     * @author Nyx
     * @date 2021/4/27 15:23
     * @param  -
     * @return java.util.List<com.fa.cim.dto.Outputs.AutoSplitOut>
     */
    Response autoSplitsInq(com.fa.cim.fam.Params.AutoSplitsInqParams params);
    
    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/4/27 21:42                     Nyx                Create
     *       
     * @author Nyx
     * @date 2021/4/27 21:42
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response availableCarrierInq(com.fa.cim.fam.Params.AvailableCarrierInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/4/29 18:56                     Nyx                Create
     *
     * @author Nyx
     * @date 2021/4/29 18:56
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response autoMergesInq(com.fa.cim.fam.Params.AutoMergesInqParams params);
}
