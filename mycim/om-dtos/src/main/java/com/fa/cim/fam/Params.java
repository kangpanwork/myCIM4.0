package com.fa.cim.fam;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/4/27        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2021/4/27 21:36
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Params {
    @Data
    public static class AvailableCarrierInqParams {
        private User user;
        private String productRequestID;
        private String cassetteCategory;
        private ObjectIdentifier cassetteID;
    }

    @Data
    public static class SjInfoForAutoStartInqParams {
        private User user;
    }

    @Data
    public static class AutoSplitsInqParams {
        private User user;
    }

    @Data
    public static class AutoMergesInqParams {
        private User user;
    }
}