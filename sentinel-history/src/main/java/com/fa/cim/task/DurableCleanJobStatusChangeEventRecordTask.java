package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.DurableCleanJobStatusChangeHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Controller
public class DurableCleanJobStatusChangeEventRecordTask {

    @Autowired
    private DurableCleanJobStatusChangeHistoryService durableCleanJobStatusChangeHistoryService;

    @Scheduled(cron = "0 0/1 * * * *")
    @Transactional(rollbackFor = Exception.class)
    public void createDurableCleanJobStatusChangeEventRecord() {
        String fifoTableName = "OMEVJOBSTCH_QUE";
        List<String> events = durableCleanJobStatusChangeHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);

                //----------------------------------------------//
                // Step1: Get Job Status Event Info             //
                //----------------------------------------------//
                Infos.DurableCleanJobStatusChangeEventRecord eventRecord = durableCleanJobStatusChangeHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets = durableCleanJobStatusChangeHistoryService.allUserDataSets(event);

                //----------------------------------------------//
                // Step2: Create History
                //----------------------------------------------//
                durableCleanJobStatusChangeHistoryService.createHistory(eventRecord, userDataSets);

                //----------------------------------------------//
                // Step3: Deletion FIFO info                    //
                //----------------------------------------------//
                durableCleanJobStatusChangeHistoryService.deleteFIFO(fifoTableName, event);
                log.info("end event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ", fifoTableName, event, e.getMessage());
            }
        }
    }

}
