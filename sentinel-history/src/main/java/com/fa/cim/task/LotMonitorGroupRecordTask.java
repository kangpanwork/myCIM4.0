package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.service.LotMonitorGroupRecordService;
import com.fa.cim.service.LotMonitorGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fa.cim.utils.BaseUtils.isOk;

/**
 * description:  make monitor group task
 *
 * change history:  
 * date             defect#             person             comments  
 * ---------------------------------------------------------------------------------------------------------------------  
 * 2021/7/26 0026          ********            Decade            create file  
 * @author: YJ
 * @date: 2021/7/26 0026 15:51  
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.    
 */
@Component
@Slf4j
public class LotMonitorGroupRecordTask {

    @Autowired
    private LotMonitorGroupRecordService lotMonitorGroupRecordService;

    @Autowired
    private LotMonitorGroupService lotMonitorGroupService;

    @Scheduled(cron = "0/10 * * * * ?")
    public void createLotMonitorGroupRecord() {
		String fifoTableName = "OMEVLMG_QUE";
		List<String> eventFIFO = lotMonitorGroupRecordService.getEventFIFO(fifoTableName);
		eventFIFO.forEach(event -> {
			Infos.LotMonitorGroupEventRecord eventData = lotMonitorGroupService.getEventData(event);
			List<Infos.UserDataSet> userDataSets = lotMonitorGroupService.allUserDataSets(event);
			Response response = lotMonitorGroupService.createLayoutHistory(eventData, userDataSets);
			if (!isOk(response)) {
				log.info(
						"HistoryWatchDogServer::createLotMonitorGroupRecord(): createLayoutRecipeRecord SQL Error ccured");
				log.info("HistoryWatchDogServer::createLotMonitorGroupRecord Function");
			}
			log.info("HistoryWatchDogServer::createLotMonitorGroupRecord Function");
			lotMonitorGroupService.deleteFIFO(fifoTableName, event);
		});


    }
}
