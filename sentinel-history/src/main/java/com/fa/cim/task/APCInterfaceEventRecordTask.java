package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.APCInterfaceEventRecordService;
import com.fa.cim.service.APCInterfaceHistoryService;
import com.fa.cim.service.LotOperationHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

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
//@Controller
public class APCInterfaceEventRecordTask {

    @Autowired
    private APCInterfaceHistoryService apcInterfaceHistoryService;

    @Autowired
    private APCInterfaceEventRecordService apcInterfaceEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

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
     * @date 2019/7/23 10:42
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void createAPCInterfaceEventRecord() {
        String fifoTableName="OMEVDOCIF_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
                Infos.APCInterfaceEventRecord eventRecord=apcInterfaceHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=apcInterfaceHistoryService.allUserDataSets(event);
                apcInterfaceEventRecordService.createAPCInterfaceEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
                log.info("end event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ",fifoTableName,event,e.getMessage());
                continue;
            }
        }
    }

}
