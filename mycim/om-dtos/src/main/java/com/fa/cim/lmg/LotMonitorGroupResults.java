package com.fa.cim.lmg;

import com.fa.cim.common.support.ObjectIdentifier;
import lombok.Data;

import java.util.List;

/**
 * description: monitor group results
 *
 * change history:  
 * date             defect#             person             comments  
 * ---------------------------------------------------------------------------------------------------------------------  
 * 2021/7/26 0026          ********            Decade            create file  
 * @author: YJ
 * @date: 2021/7/26 0026 13:29  
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.    
 */
public class LotMonitorGroupResults {

    @Data
    public static class LotMonitorGroupHistoryResults {
        private String monitorGroupId; // monitor group id
        private ObjectIdentifier monitorLotId; // monitor group id
        private List<MonitorGroupLotDetailsResults> lotResult;
    }

    @Data
    public static class MonitorGroupLotDetailsResults {
        private ObjectIdentifier lotId; // lot id
        private ObjectIdentifier operationId; // 工步id
        private String operationNumber; // 工步编号
        private String processFlowId; // flow id
        private Integer operationPassCount; // pass count
        private String operationType;  // 工步类型
        private String lotType; // lot 类型
        private String subLotType; // lot 子类型
        private String carrierId; // carrier Id
        private String carrierCategory; // carrier 类型
        private String operationName; // 工步名称
        private String holdState; // 是否hold
        private String productType; // 产品类型
        private String productId; // 产品 id
        private String technologyId; // technology id
        private Integer lotPriority; // lot 优先级
        private String mfgLayer; // mfg
        private String productFmlyId; // group id
        private String stageId; // stage id
        private String stageGroupId; // stage group
        private String lotOwnerId; // user

    }

    @Data
    public static class MonitorLotDataCollectionQueryResults {
        private ObjectIdentifier monitorLotId;
        private String operationNumber;
        private String operationPassCount;
        private String routeId;
    }

}
