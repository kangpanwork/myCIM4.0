package com.fa.cim.task;

import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.PilotRecipeJobEventRecordService;
import com.fa.cim.service.PilotRecipeJobHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author zh
 * @exception
 * @date 2021/3/4 11:23
 */
@Slf4j
@Controller
public class PilotRecipeJobEventRecordTask {

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Autowired
    private PilotRecipeJobHistoryService pilotRecipeJobHistoryService;

    @Autowired
    private PilotRecipeJobEventRecordService pilotRecipeJobEventRecordService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author zh
     * @date 2021/3/4 10:42
     */
    @Scheduled(cron = "0 0/2 * * * ? ")
    public void createPilotRecipeJobEventRecord() {
        String fifoTableName = "OMEVRECIPEPRJOB_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
                com.fa.cim.fsm.Infos.PilotEventRecord eventRecord = pilotRecipeJobHistoryService.getEventData(event);
                pilotRecipeJobEventRecordService.createPilotRecipeJobEventRecord(eventRecord);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
                log.info("end event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ",fifoTableName,event,e.getMessage());
                continue;
            }
        }
    }

}
