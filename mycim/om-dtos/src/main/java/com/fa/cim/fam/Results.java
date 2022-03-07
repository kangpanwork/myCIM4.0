package com.fa.cim.fam;

import lombok.Data;

import java.util.List;


/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/4/27        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2021/4/27 15:29
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Results {

    @Data
    public static class AutoSplitResult {
        private List<Outputs.AutoSplitOut> autoSplitOuts;
    }

    @Data
    public static class AutoMergesResult {
        private List<Outputs.AutoMergeOut> autoSplitOuts;
    }
}