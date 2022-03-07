package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotBankMoveEventRecordService;
import com.fa.cim.service.LotBankMoveHistoryService;
import com.fa.cim.service.LotOperationHistoryService;
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
 * @exception
 * @author Ho
 * @date 2019/5/31 16:12
 */
@Slf4j
@Controller
public class LotBankMoveEventRecordTask {

    @Autowired
    private LotBankMoveHistoryService lotBankMoveHistoryService;

    @Autowired
    private LotBankMoveEventRecordService lotBankMoveEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void createLotBankMoveEventRecord() {
        String fifoTableName="OMEVBNKMOV_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
                Infos.LotBankMoveEventRecord eventRecord=lotBankMoveHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=lotBankMoveHistoryService.allUserDataSets(event);
                lotBankMoveEventRecordService.createLotBankMoveEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
                log.info("end event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ",fifoTableName,event,e.getMessage());
                continue;
            }
        }
    }

}
