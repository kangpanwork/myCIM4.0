package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.APCProcessDispositionEventRecordService;
import com.fa.cim.service.APCProcessDispositionHistoryService;
import com.fa.cim.service.LotOperationHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

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
//@Controller
public class APCProcessDispositionEventRecordTask {

    @Autowired
    private APCProcessDispositionHistoryService apcProcessDispositionHistoryService;

    @Autowired
    private APCProcessDispositionEventRecordService apcProcessDispositionEventRecordService;

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
    public void createAPCProcessDispositionEventRecord() {
        String fifoTableName="OMEVPRCDP_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
                Infos.APCProcessDispositionEventRecord eventRecord=apcProcessDispositionHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=apcProcessDispositionHistoryService.allUserDataSets(event);
                apcProcessDispositionEventRecordService.createAPCProcessDispositionEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
                log.info("end event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ",fifoTableName,event,e.getMessage());
                continue;
            }
        }
    }

}
