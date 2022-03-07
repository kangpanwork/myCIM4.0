package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotOperationEventRecordService;
import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.LotOperationMoveEventRecordService;
import com.fa.cim.service.LotOperationMoveHistoryService;
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
 * @date 2019/6/4 14:42
 */
@Controller
public class LotOperationMoveEventRecordTask {

    @Autowired
    private LotOperationEventRecordService lotOperationEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Autowired
    private LotOperationMoveHistoryService lotOperationMoveHistoryService;

    @Autowired
    private LotOperationMoveEventRecordService lotOperationMoveEventRecordService;

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
     * @date 2019/6/4 14:42
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void createLotOperationMoveEventRecord() {
        String fifoTableName = "OMEVMVOP_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                Infos.LotOperationMoveEventRecord eventRecord = lotOperationMoveHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets = lotOperationMoveHistoryService.allUserDataSets(event);
                lotOperationMoveEventRecordService.createLotOperationMoveEventRecord(eventRecord, userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
            } catch (Exception e) {
                continue;
            }
        }
    }

}
