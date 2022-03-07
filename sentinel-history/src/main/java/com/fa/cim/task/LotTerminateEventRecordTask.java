package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.LotTerminateEventRecordService;
import com.fa.cim.service.LotTerminateHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * description:
 * CimLotTerminateEvent
 * change history:
 * date         defect#         person      comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 21-7-10       ********        Grant       create file
 *
 * @author: Grant
 * @date: 21-7-10 15:13
 * @copyright: 2021, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Controller
public class LotTerminateEventRecordTask {

    @Autowired
    private LotTerminateEventRecordService lotTerminateEventRecordService;

    @Autowired
    private LotTerminateHistoryService lotTerminateHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Scheduled(cron = "0 */2 * * * ?")
    public void createLotTerminateEventRecord() {
        String fifoTableName = "OMEVLOTTRM_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                if (log.isInfoEnabled())
                    log.info("start event data: FIFO[{}], EVENT[{}]", fifoTableName, event);

                Infos.LotTerminateEventRecord eventRecord = lotTerminateHistoryService.getEventData(event);
                if (eventRecord == null)
                    continue;

                // List<Infos.UserDataSet> userDataSets = lotTerminateHistoryService.allUserDataSets(event);
                lotTerminateEventRecordService.createEventRecord(eventRecord);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);

                if (log.isInfoEnabled())
                    log.info("end event data: FIFO[{}], EVENT[{}]", fifoTableName, event);
            } catch (Exception e) {
                if (log.isErrorEnabled())
                    log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}]", fifoTableName, event, e.getMessage());
            }
        }
    }

}
