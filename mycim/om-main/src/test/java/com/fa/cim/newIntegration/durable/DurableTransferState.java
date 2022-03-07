package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.DurableTransferStateCase;
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
 * <p>DurableTransferState .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/4/004   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/4/004 17:07
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class DurableTransferState {
    @Autowired
    private DurableTransferStateCase durableTransferStateCase;

    //暂时无法测试
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_1_1ChangeXferStateFromSIToMO(){
    }

    //暂时无法测试
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_1_2ChangeXferStateFromSOToMO(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_1_3ChangeXferStateFromMIToMO(){
        durableTransferStateCase.ChangeXferStateToMO();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_1_4ChangeXferStateFromMOToMO(){
        durableTransferStateCase.ChangeXferStateToMO();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_1_5ChangeXferStateFromEOToMO(){
        durableTransferStateCase.ChangeXferStateToMO();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_1_6ChangeXferStateFromHIToMO(){
        durableTransferStateCase.ChangeXferStateToMO();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_1_7ChangeXferStateFromHOToMO(){
        durableTransferStateCase.ChangeXferStateToMO();
    }

    //暂时无法测试
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_1_8ChangeXferStateFromAOToMO(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_1_9ChangeXferStateFromEIToMO(){
        durableTransferStateCase.ChangeXferStateToMO();
    }

    //暂时无法测试
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_1ChangeXferStateFromSIToMI(){
    }

    //暂时无法测试
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_2ChangeXferStateFromSOToMI(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3ChangeXferStateFromMIToMI(){
        durableTransferStateCase.ChangeXferStateToMI();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_4ChangeXferStateFromMOToMI(){
        durableTransferStateCase.ChangeXferStateToMI();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_5ChangeXferStateFromEOToMI(){
        durableTransferStateCase.ChangeXferStateToMI();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_61ChangeXferStateFromHIToMI(){
        durableTransferStateCase.ChangeXferStateToMI();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_7ChangeXferStateFromHOToMI(){
        durableTransferStateCase.ChangeXferStateToMI();
    }

    //暂时无法测试
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_8ChangeXferStateFromAOToMI(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_1ChangeXferStateFromEIToMI(){
        durableTransferStateCase.ChangeXferStateToMI();
    }

    //3.1-3.7,和CarrierStockInOut相同测试

    //4.1 4.2,ReticleEqpInOut,ReticlePodEqpInOut相同测试

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_5_1ChangeXferStateFromEOToEI(){
    }

    //暂时无法测试
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_5_2ChangeXferStateFromSIToEI(){
    }

    //暂时无法测试
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_5_3ChangeXferStateFromSOToEI(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_5_4ChangeXferStateFromMIToEI(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_5_5ChangeXferStateFromMOToEI(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_5_6ChangeXferStateFromHIToEI(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_5_7ChangeXferStateFromHOToEI(){
    }

    //暂时无法测试
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_5_8ChangeXferStateFromAOToEI(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_6_1ChangeXferStateFromEIToEO(){
    }

}
