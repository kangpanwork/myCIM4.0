package com.fa.cim.task;

import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.RunCardEventRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/8/20                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/8/20 13:00
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Controller
public class RunCardEventRecordTask {

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Autowired
    private RunCardEventRecordService runCardEventRecordService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void createRunCardEventRecord() {
        String fifoTableName = "OMEVRUNCARD_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
                runCardEventRecordService.createRunCardEventRecord(event);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
                log.info("end event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ", fifoTableName, event, e.getMessage());
                continue;
            }
        }
    }
}
