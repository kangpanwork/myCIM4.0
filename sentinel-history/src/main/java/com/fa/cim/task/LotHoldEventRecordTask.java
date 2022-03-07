package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotHoldEventRecordService;
import com.fa.cim.service.LotHoldHistoryService;
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
 * @exception
 * @author Ho
 * @date 2019/5/31 16:12
 */
@Controller
public class LotHoldEventRecordTask {

    @Autowired
    private LotOperationEventRecordService lotOperationEventRecordService;

    @Autowired
    private LotHoldHistoryService lotHoldHistoryService;

    @Autowired
    private LotHoldEventRecordService lotHoldEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void createLotHoldEventRecord() {
        String fifoTableName="OMEVLHOLD_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                Infos.LotHoldEventRecord eventRecord=lotHoldHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=lotHoldHistoryService.allUserDataSets(event);
                lotHoldEventRecordService.createLotHoldEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

}
