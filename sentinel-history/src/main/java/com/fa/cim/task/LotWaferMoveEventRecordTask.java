package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotOperationEventRecordService;
import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.LotWaferMoveEventRecordService;
import com.fa.cim.service.LotWaferMoveHistoryService;
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
public class LotWaferMoveEventRecordTask {

    @Autowired
    private LotOperationEventRecordService lotOperationEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Autowired
    private LotWaferMoveEventRecordService lotWaferMoveEventRecordService;

    @Autowired
    private LotWaferMoveHistoryService lotWaferMoveHistoryService;

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
    public void createLotWaferMoveEventRecord() {
        String fifoTableName = "OMEVLWFMV_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                Infos.LotWaferMoveEventRecord eventRecord = lotWaferMoveHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets = lotWaferMoveHistoryService.allUserDataSets(event);
                lotWaferMoveEventRecordService.createLotWaferMoveEventRecord(eventRecord, userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
            } catch (Exception e) {
                continue;
            }
        }
    }

}
