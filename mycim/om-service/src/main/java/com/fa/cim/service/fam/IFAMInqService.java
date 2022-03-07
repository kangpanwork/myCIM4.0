package com.fa.cim.service.fam;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.fam.Outputs;

import java.util.List;

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
public interface IFAMInqService {

    /**
     * description: query sort job history
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/4/21 5:44 下午 zh Create
     *
     * @author zh
     * @date 2021/4/21 5:44 下午
     * @param
     * @return void
     */
    Boolean sxHasSJHistoryListInq(Params.SortJobHistoryParams sortJobHistoryParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/4/27 15:26                     Nyx                Create
     *
     * @author Nyx
     * @date 2021/4/27 15:26
     * @param  -
     * @return java.util.List<com.fa.cim.dto.Outputs.AutoSplitOut>
     */
    List<Outputs.AutoSplitOut> autoSplits();

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/4/27 21:42                     Nyx                Create
     *
     * @author Nyx
     * @date 2021/4/27 21:42
     * @param objCommon
     * @param params -
     * @return java.util.List<com.fa.cim.dto.Infos.AvailableCarrierOut>
     */
    List<Infos.AvailableCarrierOut> sxAvailableCarrierListForLotStartInq(Infos.ObjCommon objCommon, com.fa.cim.fam.Params.AvailableCarrierInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/4/29 18:54                     Nyx                Create
     *
     * @author Nyx
     * @date 2021/4/29 18:54
     * @param  -
     * @return java.util.List<com.fa.cim.fam.Outputs.AutoMergeOut>
     */
    List<Outputs.AutoMergeOut> autoMerges();
}
