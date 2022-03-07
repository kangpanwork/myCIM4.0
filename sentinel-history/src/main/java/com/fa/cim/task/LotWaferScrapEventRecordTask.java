package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.LotWaferScrapEventRecordService;
import com.fa.cim.service.LotWaferScrapHistoryService;
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
public class LotWaferScrapEventRecordTask {

    @Autowired
    private LotWaferScrapHistoryService lotWaferScrapHistoryService;

    @Autowired
    private LotWaferScrapEventRecordService lotWaferScrapEventRecordService;

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
    public void createLotWaferScrapEventRecord() {
        String fifoTableName = "OMEVWFSCR_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
                Infos.LotWaferScrapEventRecord eventRecord = lotWaferScrapHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets = lotWaferScrapHistoryService.allUserDataSets(event);
                lotWaferScrapEventRecordService.createLotWaferScrapEventRecord(eventRecord, userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
                log.info("end event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ", fifoTableName, event, e.getMessage());
                continue;
            }
        }
    }

}
