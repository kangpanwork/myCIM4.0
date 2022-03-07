package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotOperationEventRecordService;
import com.fa.cim.service.LotOperationHistoryService;
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
 * @date 2019/3/11 16:52
 */
@Controller
public class LotOperationEventRecordTask {

    @Autowired
    private LotOperationEventRecordService lotOperationEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void createLotOperationStartEventRecord() {
        String fifoTableName = "OMEVMVI_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                Infos.LotOperationStartEventRecord eventRecord = lotOperationHistoryService.getLotOperationStartEventRecord(event);
                List<Infos.UserDataSet> userDataSets = lotOperationHistoryService.getLotOperationStartEventUserDataSets(event);
                lotOperationEventRecordService.createLotOperationStartEventRecord(eventRecord, userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
            } catch (Exception e) {
                continue;
            }
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void createLotOperationCompleteEventRecord() {
        String fifoTableName = "OMEVMVO_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                Infos.LotOperationCompleteEventRecord eventRecord = lotOperationHistoryService.getLotOperationCompleteEventRecord(event);
                List<Infos.UserDataSet> userDataSets = lotOperationHistoryService.getLotOperationCompleteEventUserDataSets(event);
                lotOperationEventRecordService.createLotOperationCompleteEventRecord(eventRecord, userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
            } catch (Exception e) {
                continue;
            }
        }
    }

}
