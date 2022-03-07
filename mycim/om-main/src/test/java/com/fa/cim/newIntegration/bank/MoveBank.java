package com.fa.cim.newIntegration.bank;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.bank.scase.MoveBankCase;
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
 * @date: 2019/9/2 16:04
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class MoveBank {

    @Autowired
    private MoveBankCase moveBankCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveBank_NotOnHoldLot(){
        moveBankCase.moveBank_NotOnHoldLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveBank_OnHoldLot_FromNonProBank_ToNonProBank(){

    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveBank_OnHoldLot_AfterBankIn(){

    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveBank_FromProductionBank_ToProductionBank(){

    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveBank_FromProductionBank_ToNonProductionBank(){

    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveBank_FromNonProductionBank_ToNonProductionBank(){

    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveBank_NonProBankLot_FromNonProductionBank_ToProductionBank(){

    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveBank_ShippedLot(){

    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveBank_EmptiedLot(){

    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveBank_ScrappedLot(){

    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveBank_OnFloorLot(){

    }

}