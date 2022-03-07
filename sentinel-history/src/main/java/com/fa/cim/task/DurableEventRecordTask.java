package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.DurableEventRecordService;
import com.fa.cim.service.DurableHistoryService;
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
 * @exception
 * @author Ho
 * @date 2019/5/31 16:12
 */
@Slf4j
@Controller
public class DurableEventRecordTask {

    @Autowired
    private DurableHistoryService durableHistoryService;

    @Autowired
    private DurableEventRecordService durableEventRecordService;

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
     * @exception
     * @author Ho
     * @date 2019/7/11 14:02
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void createDurableEventRecord() {
        String fifoTableName="OMEVDUR_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
                Infos.DurableEventRecord eventRecord=durableHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=durableHistoryService.allUserDataSets(event);
                durableEventRecordService.createDurableEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
                log.info("end event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ",fifoTableName,event,e.getMessage());
                continue;
            }
        }
    }

}
