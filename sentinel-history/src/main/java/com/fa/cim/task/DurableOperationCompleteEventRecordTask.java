package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.DurableOperationCompleteEventRecordService;
import com.fa.cim.service.DurableOperationCompleteHistoryService;
import com.fa.cim.service.LotOperationHistoryService;
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
 * @author Ho
 * @exception
 * @date 2019/5/31 16:12
 */
@Slf4j
@Controller
public class DurableOperationCompleteEventRecordTask {

    @Autowired
    private DurableOperationCompleteHistoryService durableOperationCompleteHistoryService;

    @Autowired
    private DurableOperationCompleteEventRecordService durableOperationCompleteEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @throws
     * @author Ho
     * @date 2019/7/11 14:02
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void createDurableOperationCompleteEventRecord() {
        String fifoTableName = "OMEVDURMVO_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
                Infos.DurableOperationCompleteEventRecord eventRecord = durableOperationCompleteHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets = durableOperationCompleteHistoryService.allUserDataSets(event);
                durableOperationCompleteEventRecordService.createDurableOperationCompleteEventRecord(eventRecord, userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
                log.info("end event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ", fifoTableName, event, e.getMessage());
                continue;
            }
        }
    }

}
