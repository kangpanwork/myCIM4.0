package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.VendorLotEventRecordService;
import com.fa.cim.service.VendorLotHistoryService;
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
 * @author Ho
 * @exception
 * @date 2019/6/28 13:02
 */
@Slf4j
@Controller
public class VendorLotEventRecordTask {

    @Autowired
    private VendorLotHistoryService vendorLotHistoryService;

    @Autowired
    private VendorLotEventRecordService vendorLotEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void createVendorLotEventRecord() {
        String fifoTableName = "OMEVVENDLOT_QUE";
        List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event : events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
                Infos.VendorLotEventRecord eventRecord = vendorLotHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets = vendorLotHistoryService.allUserDataSets(event);
                vendorLotEventRecordService.createVendorLotEventRecord(eventRecord, userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
                log.info("end event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ", fifoTableName, event, e.getMessage());
                continue;
            }
        }
    }

}
