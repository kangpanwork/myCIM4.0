package com.fa.cim.task;

import com.fa.cim.dto.Infos;
import com.fa.cim.service.LotOperationHistoryService;
import com.fa.cim.service.WaferChamberProcessEventRecordService;
import com.fa.cim.service.WaferChamberProcessHistoryService;
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
@Controller
public class WaferChamberProcessEventRecordTask {

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Autowired
    private WaferChamberProcessHistoryService waferChamberProcessHistoryService;

    @Autowired
    private WaferChamberProcessEventRecordService waferChamberProcessEventRecordService;

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
    public void createWaferChamberProcessEventRecord() {
        String fifoTableName="OMEVWFCMB_QUE";
        List<String> events=lotOperationHistoryService.getEventFIFO(fifoTableName);
        for (String event:events) {
            try {
                Infos.WaferChamberProcessEventRecord eventRecord=waferChamberProcessHistoryService.getEventData(event);
                List<Infos.UserDataSet> userDataSets=waferChamberProcessHistoryService.allUserDataSets(event);
                waferChamberProcessEventRecordService.createWaferChamberProcessEventRecord(eventRecord,userDataSets);
                lotOperationHistoryService.deleteFIFO(fifoTableName,event);
            } catch (Exception e) {
                continue;
            }
        }
    }

}
