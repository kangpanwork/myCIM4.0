package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.LotHoldReleaseCase;
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
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/9/3 10:27
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
@Rollback
public class LotHoldRelease extends AbstractTransactionalTestNGSpringContextTests {

    @Autowired
    private LotHoldReleaseCase lotHoldReleaseCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Multi_HoldList_ToDo_HoldRelease(){
        lotHoldReleaseCase.Multi_HoldList_ToDo_HoldRelease();
    }
}
