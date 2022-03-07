package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.LotHoldCase;
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
 * 2019/9/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/9/2 13:45
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class LotHold {

    @Autowired
    private LotHoldCase lotHoldCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VenderLot_Todo_LotHold(){
        lotHoldCase.VenderLot_Todo_LotHold();
    }


    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STB_After_LotHold_On_Current(){
        lotHoldCase.STB_After_LotHold_On_Current();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void SameLot_Do_Hold_FowTwice_Using_Same_ReasonCode(){
        lotHoldCase.SameLot_Do_Hold_ForTwice_Using_Same_ReasonCode();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void SameLot_Do_Hold_FowTwice_Using_Different_ReasonCode(){
        lotHoldCase.SameLot_Do_Hold_FowTwice_Using_Different_ReasonCode();
    }

}
