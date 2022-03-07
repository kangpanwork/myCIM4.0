package com.fa.cim.dto.lot;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;

/**
 * description:
 * TerminateReq
 * change history:
 * date         defect#         person      comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 21-7-7       ********        Grant       create file
 *
 * @author: Grant
 * @date: 21-7-7 19:34
 * @copyright: 2021, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
public class TerminateReq {

    @Data
    public static class TerminateReqParams {

        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier bankID;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;

    }

    @Data
    public static class TerminateCancelReqParams {

        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;

    }

    @Data
    public static class TerminateEventMakeParams {

        private String transactionID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;

    }

}
