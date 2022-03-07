package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.FutureHoldCancelCase;
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
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/17                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/9/17 17:47
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class FutureHoldCancel {

    @Autowired
    private FutureHoldCancelCase futureHoldCancelCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void futureHoldCancel_Normal(){
        futureHoldCancelCase.futureHoldCancel_Normal();
    }
}
