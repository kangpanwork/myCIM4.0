package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.ProcessStatusEventRecordService;
import com.fa.cim.service.ProcessStatusHistoryService;
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
 * @date 2019/6/6 16:44
 */
@Slf4j
@Controller
public class ProcessStatusEventRecordTask {

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Autowired
    private ProcessStatusHistoryService processStatusHistoryService;

    @Autowired
    private ProcessStatusEventRecordService processStatusEventRecordService;

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
     * @date 2019/6/6 16:44
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void createProcessStatusEventRecord() {
        String fifoTableName="OMEVPRCST_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                Infos.ProcessStatusEventRecord eventRecord=processStatusHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=processStatusHistoryService.allUserDataSets(event);
                processStatusEventRecordService.createProcessStatusEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ",fifoTableName,event,e.getMessage());
                continue;
            }
        }
    }

}
