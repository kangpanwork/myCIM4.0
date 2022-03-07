package com.fa.cim.task;

import com.fa.cim.pr.Infos;
import com.fa.cim.service.RecipeGroupEventRecordService;
import com.fa.cim.service.RecipeGroupHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
* description:
* change history:
* date             defect             person             comments
* ---------------------------------------------------------------------------------------------------------------------
* 2021/3/4 15:42                     Aoki                Create
*
* @author Aoki
* @date 2021/3/4 15:42
* @param null
* @return
*/
@Slf4j
@Controller
public class RecipeGroupEventRecordTask {

    @Autowired
    private RecipeGroupHistoryService recipeGroupHistoryService;

    @Autowired
    private RecipeGroupEventRecordService recipeGroupEventRecordService;

/** 
* description:  
* change history:  
* date             defect             person             comments  
* ---------------------------------------------------------------------------------------------------------------------  
* 2021/3/4 16:18                     Aoki                Create
*         
* @author Aoki 
* @date 2021/3/4 16:18
* @param  
* @return void  
*/
    @Scheduled(cron = "0 0/2 * * * ? ")
    public void createRecipeGroupEventRecord() {
        String fifoTableName="OMEVRECIPEGROUP_QUE";
        List<String> events=recipeGroupHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                log.info("start event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
                Infos.RecipeGroupEventRecord eventRecord = recipeGroupHistoryService.getEventData(event);
                recipeGroupEventRecordService.createRecipeGroupEventRecord(eventRecord);
                recipeGroupHistoryService.deleteFIFO(fifoTableName,event);
                log.info("end event data: FIFO[{}], EVENT[{}] ",fifoTableName,event);
            } catch (Exception e) {
                log.error("end event data: FIFO[{}], EVENT[{}], MSG[{}] ",fifoTableName,event,e.getMessage());
                continue;
            }
        }
    }

}
