package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotHoldEventRecordService;
import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.ProductRequestEventRecordService;
import com.fa.cim.service.ProductRequestHistoryService;
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
 * @date 2019/6/24 15:10
 */
@Slf4j
@Controller
public class ProductRequestEventRecordTask {

    @Autowired
    private LotHoldEventRecordService lotHoldEventRecordService;

    @Autowired
    private ProductRequestEventRecordService productRequestEventRecordService;

    @Autowired
    private ProductRequestHistoryService productRequestHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void createProductRequestEventRecord() {
        String fifoTableName="OMEVPREQ_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
                Infos.ProductRequestEventRecord eventRecord=productRequestHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=productRequestHistoryService.allUserDataSets(event);
                productRequestEventRecordService.createProductRequestEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
                log.info("end event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ",fifoTableName,event,e.getMessage());
                continue;
            }
        }
    }

}
