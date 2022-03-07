package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.ProcessResourceWaferPositionEventRecordService;
import com.fa.cim.service.ProcessResourceWaferPositionHistoryService;
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
 * @date 2019/6/6 16:44
 */
@Controller
public class ProcessResourceWaferPositionEventRecordTask {

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Autowired
    private ProcessResourceWaferPositionHistoryService processResourceWaferPositionHistoryService;

    @Autowired
    private ProcessResourceWaferPositionEventRecordService processResourceWaferPositionEventRecordService;

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
     * @date 2019/6/6 16:44
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void createProcessResourceWaferPositionEventRecord() {
        String fifoTableName = "OMEVPRCRESWP_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                Infos.ProcessResourceWaferPositionEventRecord eventRecord = processResourceWaferPositionHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets = processResourceWaferPositionHistoryService.allUserDataSets(event);
                processResourceWaferPositionEventRecordService.createProcessResourceWaferPositionEventRecord(eventRecord, userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
            } catch (Exception e) {
                continue;
            }
        }
    }

}
