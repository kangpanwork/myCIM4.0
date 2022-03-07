package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.EntityInhibitEventRecordService;
import com.fa.cim.service.EntityInhibitHistoryService;
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
public class EntityInhibitEventRecordTask {

    @Autowired
    private EntityInhibitHistoryService entityInhibitHistoryService;

    @Autowired
    private EntityInhibitEventRecordService entityInhibitEventRecordService;

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
     * @date 2019/7/1 10:48
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void createEntityInhibitEventRecord() {
        String fifoTableName = "OMEVRESTRICT_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                if(log.isDebugEnabled()){
                    log.debug("start event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
                }
                Infos.EntityInhibitEventRecord eventRecord = entityInhibitHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets = entityInhibitHistoryService.allUserDataSets(event);
                entityInhibitEventRecordService.createEntityInhibitEventRecord(eventRecord, userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
                if (log.isDebugEnabled()) {
                    log.debug("end event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
                }
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ", fifoTableName, event, e.getMessage());
                continue;
            }
        }
    }

}
