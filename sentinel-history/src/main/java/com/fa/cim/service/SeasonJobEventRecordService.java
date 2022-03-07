package com.fa.cim.service;

import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/6/30 15:06
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class SeasonJobEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private SeasonJobHistoryService seasonJobHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param event
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/31 12:58
     */
    public void createSeasonJobEventRecord(String event) {
        // insert OHSEASONJOB
        String id = seasonJobHistoryService.insertOHSEASONJOB(event);

        // inset OHSEASON_UDATA
        seasonJobHistoryService.insertOHSEASONJOBUDATA(event,id);
    }
}
