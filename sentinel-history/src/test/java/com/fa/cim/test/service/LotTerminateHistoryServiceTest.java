package com.fa.cim.test.service;

import com.fa.cim.SentinelHistoryApplication;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.service.LotTerminateEventRecordService;
import com.fa.cim.service.LotTerminateHistoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static com.fa.cim.utils.BaseUtils.generateID;

/**
 * description:
 * LotTerminateHistoryServiceTest
 * change history:
 * date         defect#         person      comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 21-7-10       ********        Grant       create file
 *
 * @author: Grant
 * @date: 21-7-10 17:52
 * @copyright: 2021, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SentinelHistoryApplication.class)
@WebAppConfiguration
public class LotTerminateHistoryServiceTest {

    @Autowired
    private LotTerminateHistoryService historyService;
    @Autowired
    private LotTerminateEventRecordService recordService;

    @Test
    public void testGetEventData() {
        Infos.LotTerminateEventRecord record = historyService.getEventData(generateID(Infos.Ohtrmhs.class));
        assert record != null;
    }

    @Test
    public void testAllUserDataSets() {
        List<Infos.UserDataSet> list = historyService.allUserDataSets(generateID(Infos.Ohtrmhs.class));
        assert list != null && list.size() > 0;
    }

    @Test
    public void testCreateEventRecord() {
        Response response = recordService.createEventRecord(
                historyService.getEventData(generateID(Infos.Ohtrmhs.class)));
        System.out.println(response.getCode());
    }

}
