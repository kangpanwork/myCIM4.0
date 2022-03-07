package com.fa.cim.newIntegration.bank;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.bank.scase.BankShipCancelCase;
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
 * 2019/9/2       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/2 15:11
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class BankShipCancel {
    @Autowired
    private BankShipCancelCase bankShipCancelCase;
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void bankShip(){
        bankShipCancelCase.bankShipCancel();
    }
}