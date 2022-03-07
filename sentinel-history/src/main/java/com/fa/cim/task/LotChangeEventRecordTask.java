package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotChangeEventRecordService;
import com.fa.cim.service.LotChangeHistoryService;
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
 * @exception
 * @author Ho
 * @date 2019/5/31 16:12
 */
@Controller
public class LotChangeEventRecordTask {

    @Autowired
    private LotChangeHistoryService lotChangeHistoryService;

    @Autowired
    private LotChangeEventRecordService lotChangeEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void createLotChangeEventRecord() {
        String fifoTableName="OMEVLCHG_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                Infos.LotChangeEventRecord eventRecord=lotChangeHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=lotChangeHistoryService.allUserDataSets(event);
                lotChangeEventRecordService.createLotChangeEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
            } catch (Exception e) {
                continue;
            }
        }
    }

}
