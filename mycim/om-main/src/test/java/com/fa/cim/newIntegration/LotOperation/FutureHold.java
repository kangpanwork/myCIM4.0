package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.FutureHoldCase;
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
 * 2019/9/10       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/10 14:30
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class FutureHold {

    @Autowired
    private FutureHoldCase futureHoldCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void futureHold_Normal(){
        futureHoldCase.futureHold_Normal();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void futureHold_With_HoldTimingPre_And_HoldTriggerSingle(){
        futureHoldCase.futureHold_With_HoldTimingPre_And_HoldTriggerSingle();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void futureHold_With_HoldTimingPre_And_HoldTriggerMultiple(){
        futureHoldCase.futureHold_With_HoldTimingPre_And_HoldTriggerMultiple();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void futureHold_With_HoldTimingPost(){
        futureHoldCase.futureHold_With_HoldTimingPost();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void trigger_FutureHold_By_OperationLocate_When_HoldTimingPre(){
        futureHoldCase.trigger_FutureHold_By_OperationLocate_When_HoldTimingPre();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void vender_Lot_Todo_FutureHold(){
        futureHoldCase.vender_Lot_Todo_FutureHold();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void same_Lot_FutureHold_Using_Different_ReasonCode_For_Twice(){
        futureHoldCase.same_Lot_FutureHold_Using_Different_ReasonCode_For_Twice();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void same_Lot_FutureHold_Using_Same_ReasonCode_For_Twice(){
        futureHoldCase.same_Lot_FutureHold_Using_Same_ReasonCode_For_Twice();
    }

    /*@Test
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void trigger_FutureHold_By_Operation_Complete_When_The_HoldTimingPre(){
        futureHoldCase.trigger_FutureHold_By_Operation_Complete_When_The_HoldTimingPre();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void trigger_TwiceFutureHold_By_Operation_Complete_When_Register_TwoStep(){
        futureHoldCase.trigger_TwiceFutureHold_By_Operation_Complete_When_Register_TwoStep();
    }*/

}