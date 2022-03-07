package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.DurableOperationMoveEventRecordService;
import com.fa.cim.service.DurableOperationMoveHistoryService;
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
public class DurableOperationMoveEventRecordTask {

    @Autowired
    private DurableOperationMoveHistoryService durableOperationMoveHistoryService;

    @Autowired
    private DurableOperationMoveEventRecordService durableOperationMoveEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @throws
     * @author Ho
     * @date 2019/7/23 10:42
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void createDurableOperationMoveEventRecord() {
        String fifoTableName = "OMEVDURMVOP_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
                Infos.DurableOperationMoveEventRecord eventRecord = durableOperationMoveHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets = durableOperationMoveHistoryService.allUserDataSets(event);
                durableOperationMoveEventRecordService.createDurableOperationMoveEventRecord(eventRecord, userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
                log.info("end event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ", fifoTableName, event, e.getMessage());
                continue;
            }
        }
    }

}
