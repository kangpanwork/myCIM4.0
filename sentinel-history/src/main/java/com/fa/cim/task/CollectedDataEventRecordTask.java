package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.CollectedDataEventRecordService;
import com.fa.cim.service.CollectedDataHistoryService;
import com.fa.cim.service.LotOperationEventRecordService;
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
 * @author Ho
 * @date 2019/3/11 16:52
 */
@Controller
@Slf4j
public class CollectedDataEventRecordTask {

    @Autowired
    private LotOperationEventRecordService lotOperationEventRecordService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Autowired
    private CollectedDataHistoryService collectedDataHistoryService;

    @Autowired
    private CollectedDataEventRecordService collectedDataEventRecordService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void createCollectedDataEventRecord() {
		String fifoTableName = "OMEVEDC_QUE";
		List<String> events = lotOperationHistoryService.getEventFIFO(fifoTableName);
		for (String event : events) {
			try {
				Infos.CollectedDataEventRecord eventRecord = collectedDataHistoryService.getEventData(event);
				List<Infos.UserDataSet> userDataSets = collectedDataHistoryService.allUserDataSets(event);
				collectedDataEventRecordService.createCollectedDataEventRecord(eventRecord, userDataSets);
				lotOperationHistoryService.deleteFIFO(fifoTableName, event);
			} catch (Exception e) {
				log.error("createCollectedDataEventRecord()->error: {}", e.getMessage());
			}
		}
    }

}
