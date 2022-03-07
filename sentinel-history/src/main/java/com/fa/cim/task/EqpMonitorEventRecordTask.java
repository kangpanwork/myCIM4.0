package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.EqpMonitorEventRecordService;
import com.fa.cim.service.EqpMonitorHistoryService;
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
public class EqpMonitorEventRecordTask {

    @Autowired
    private EqpMonitorHistoryService eqpMonitorHistoryService;

    @Autowired
    private EqpMonitorEventRecordService eqpMonitorEventRecordService;

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
     * @exception
     * @author Ho
     * @date 2019/7/1 10:48
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void createEqpMonitorEventRecord() {
        String fifoTableName="OMEVAMON_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
                Infos.EqpMonitorEventRecord eventRecord=eqpMonitorHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=eqpMonitorHistoryService.allUserDataSets(event);
                eqpMonitorEventRecordService.createEqpMonitorEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
                log.info("end event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ",fifoTableName,event,e.getMessage());
                continue;
            }
        }
    }

}
