package com.fa.cim.newIntegration.contamination;

import com.fa.cim.MycimApplication;
import com.fa.cim.method.IContaminationMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/6/21        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/6/21 10:04
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class contaminationTest {

    @Autowired
    private IContaminationMethod contaminationMethod;

    //NP000621.00A

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void contaminationNormalTestQianDaoMode(){

    }
}