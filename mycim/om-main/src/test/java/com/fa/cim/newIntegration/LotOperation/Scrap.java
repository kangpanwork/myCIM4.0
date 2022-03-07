package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.ScrapCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/4        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/4 14:39
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
@Rollback
public class Scrap extends AbstractTransactionalTestNGSpringContextTests {
    @Autowired
    private ScrapCase scrapCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void scrap_noraml(){
        scrapCase.scrap_norml();
    }

}