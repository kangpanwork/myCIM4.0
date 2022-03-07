package com.fa.cim.service.pprocess;

import com.fa.cim.common.support.ObjectIdentifier;
import lombok.Getter;
import lombok.Setter;

public class PostTaskResult {

    @Getter
    @Setter
    public static class Task {
        private String taskId;
        private String transactionId;
        private int indexNo;
        private String executorId;
        private String joinMode;
        private boolean chainedFlag;
        private String errorMode;
        private String commitMode;
        private String entityType; // Lot, Durable, Equipment
        private ObjectIdentifier entityID;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier equipmentID;
        private String status; // Reserve, Waiting, Error, Completed
        private String createTime;
        private String trxTime;
        private String trxMemo;
        private ObjectIdentifier trxUserID;
    }

    @Getter
    @Setter
    public static class TaskDetail {
        private String executorId;
        private String name;
        private String value;
    }
}
