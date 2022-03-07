package com.fa.cim.fam;

import com.fa.cim.common.support.ObjectIdentifier;
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
 * @date: 2021/4/27 15:31
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Outputs {
    @Data
    public static class AutoSplitOut {
        private String cassetteID;
        private String destinationCassetteID;
        private String sorterJobCategory;
        private String sorterLotID;
        private List<String> lotIDs;
        private String holdType;
        private String holdReasonCodeID;
        private String releaseReasonCodeID;
    }

    @Data
    public static class AutoMergeOut {
        private String cassetteID;
        private String destinationCassetteID;
        private String sorterJobCategory;
        private String sorterLotID;
        private List<String> lotIDs;
        private String holdType;
        private String holdReasonCodeID;
        private String releaseReasonCodeID;
        private String lotID;
        private String childLotID;
    }
}