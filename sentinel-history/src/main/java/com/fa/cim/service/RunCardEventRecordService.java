package com.fa.cim.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/8/20                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/8/20 13:03
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class RunCardEventRecordService {

    @Autowired
    private RunCardHistoryService runCardHistoryService;


    public void createRunCardEventRecord(String event) {

        // insert OHRUNCARD
        runCardHistoryService.insertOHRUNCARD(event);
    }
}
