package com.fa.cim.newIntegration.bank;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.bank.scase.BankHoldCase;
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
 * <p>HoldLotInBank .<br/></p>
 * <p>
 * change history:
 * date   defect#    person  comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2019/9/9/009 ********  Decade  create file
 *
 * @author: Decade
 * @date: 2019/9/9/009 15:48
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class BankHold {
    @Autowired
    private BankHoldCase bankHoldCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void bankHold(){
        bankHoldCase.bankHold();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void bankHold_WithoutReason(){
        bankHoldCase.bankHold();
    }
}
