package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

import static com.fa.cim.utils.BaseUtils.isOk;

/**
 * description:
 * This file use to define the ReticleEventRecord class.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/1/9        ********             salt               create file
 *
 * @author: salt
 * @date: 2021/1/9 14:37
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Controller
@Slf4j
public class ReticleEventRecordTask {
    @Autowired
    private DurableHistoryService durableHistoryService;

    @Autowired
    private DurableEventRecordService durableEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;


    @Autowired
    private ReticleOperationHistoryService reticleOperationHistoryService;

    @Autowired
    private ReticleHistoryService reticleHistoryService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void createReticleEventRecord() {
        String fifoTableName = "OMEVRTLOPE_QUE";
        List<String> events = reticleOperationHistoryService.getEventFIFO(fifoTableName);

        for (String event : events) {
            Infos.ReticleEventOperationalData eventData = reticleHistoryService.getEventData(event);
            List<Infos.UserDataSet> userDataSetList = reticleHistoryService.allUserDataSets(event);
            Infos.ReticleHistory reticleHistory = reticleHistoryService.eventHistoryConversionReticleHistory(eventData);
            Response response = reticleHistoryService.createReticleHistory(reticleHistory);
            if( !isOk(response) ) {
                log.info("HistoryWatchDogServer::createReticleEventRecord(): createReticleHistory SQL Error Occured");
                log.info("HistoryWatchDogServer::createReticleEventRecord Function");
            }

            log.info("HistoryWatchDogServer::createReticleEventRecord Function");

            reticleHistoryService.createUserData(userDataSetList);

            reticleHistoryService.deleteFIFO(fifoTableName, event);
        }



        for (String event : events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
                Infos.DurableEventRecord eventRecord = durableHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets = durableHistoryService.allUserDataSets(event);
                durableEventRecordService.createDurableEventRecord(eventRecord, userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName, event);
                log.info("end event data: FIFO[{}], EVENT[{}] ", fifoTableName, event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ", fifoTableName, event, e.getMessage());
                continue;
            }
        }
    }
}