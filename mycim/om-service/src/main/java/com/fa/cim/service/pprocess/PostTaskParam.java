package com.fa.cim.service.pprocess;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.jpa.SearchCondition;
import lombok.Getter;
import lombok.Setter;

public class PostTaskParam {

    @Getter
    @Setter
    public static class TaskListInq {
        private User user;
        private Integer indexNo;
        private String taskId;
        private String transactionId;
        private String executorId;
        private ObjectIdentifier lotID;         // wild card * enabled
        private ObjectIdentifier carrierID;     // wild card * enabled
        private ObjectIdentifier equipmentID;   // wild card * enabled
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier trxUserID;
        private Integer maxRecordCount;
        private SearchCondition searchCondition;
    }

    @Getter
    @Setter
    public static class TaskExecReq {
        private User user;
        private String taskId;
        private String execAction; // ExecuteTask_force, ExecuteTask_passThrough, ExecuteTask_retry
    }

    @Getter
    @Setter
    public static class TaskDetailsInq {
        private User user;
        private String taskId;
        private Integer indexNo;
    }

    @Getter
    @Setter
    public static class TaskRemoveReq {
        private User user;
        private String taskId;
        private String removeAction; // RemoveTask_Top, RemoveTask_byTaskId, RemoveTask_byChained
    }
}
