package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.EqpPortStatusChangeEventRecordService;
import com.fa.cim.service.EqpPortStatusChangeHistoryService;
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
public class EqpPortStatusChangeEventRecordTask {

    @Autowired
    private EqpPortStatusChangeHistoryService eqpPortStatusChangeHistoryService;

    @Autowired
    private EqpPortStatusChangeEventRecordService eqpPortStatusChangeEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void createEqpPortStatusChangeEventRecord() {
        String fifoTableName="OMEVEQPRTSC_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                Infos.EqpPortStatusChangeEventRecord eventRecord=eqpPortStatusChangeHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=eqpPortStatusChangeHistoryService.allUserDataSets(event);
                eqpPortStatusChangeEventRecordService.createEqpPortStatusChangeEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
            } catch (Exception e) {
                continue;
            }
        }
    }

}
