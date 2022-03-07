package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotOperationEventRecordService;
import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.LotReworkEventRecordService;
import com.fa.cim.service.LotReworkHistoryService;
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
 * @date 2019/6/4 14:42
 */
@Controller
public class LotReworkEventRecordTask {

    @Autowired
    private LotOperationEventRecordService lotOperationEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Autowired
    private LotReworkHistoryService lotReworkHistoryService;

    @Autowired
    private LotReworkEventRecordService lotReworkEventRecordService;

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
     * @date 2019/6/4 14:42
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void createLotReworkEventRecord() {
        String fifoTableName="OMEVLRWK_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                Infos.LotReworkEventRecord eventRecord=lotReworkHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=lotReworkHistoryService.allUserDataSets(event);
                lotReworkEventRecordService.createLotReworkEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
            } catch (Exception e) {
                continue;
            }
        }
    }

}
