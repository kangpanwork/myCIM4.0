package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.ReticleEqpInOutCase;
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
 * <p>ReticleEqpInOut .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2019/12/23/023   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2019/12/23/023 17:08
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class ReticleEqpInOut {

    @Autowired
    private ReticleEqpInOutCase reticleEqpInOutCase;

    //AUTO缺少对应配置，请在DEV环境进行测试

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_1_ReticleXferStatusEOEqpIn(){
        reticleEqpInOutCase.DRB1_6_1_ReticleXferStatusEOEqpIn();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_2_ReticleXferStatusEIEqpOut(){
        reticleEqpInOutCase.DRB1_6_2_ReticleXferStatusEIEqpOut();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_3_ReticleEqpNotNeedReticleEqpIn(){
        reticleEqpInOutCase.DRB1_6_3_ReticleEqpNotNeedReticleEqpIn();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_4_ReticleRelatedToReticlePodEqpIn(){
        reticleEqpInOutCase.DRB1_6_4_ReticleRelatedToReticlePodEqpIn();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_5_ReticleRelatedToReticlePodEqpOut(){
        reticleEqpInOutCase.DRB1_6_5_ReticleRelatedToReticlePodEqpOut();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_6_ReticleXferStatusEOEqpIn(){
        reticleEqpInOutCase.DRB1_6_1_ReticleXferStatusEOEqpIn();
    }

    /**
     * description:
     * <p>the same as DRB1_6_12_Eqp_Out_Status_Check</p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2019/12/31/031 9:17
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_7_ReticleXferStatusEIEqpOut(){
        reticleEqpInOutCase.DRB1_6_2_ReticleXferStatusEIEqpOut();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_8_ReticleXferStatusEOEqpInFromReticlePodMenu(){
        reticleEqpInOutCase.DRB1_6_8_ReticleXferStatusEOEqpInFromReticlePodMenu();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_9_ReticleXferStatusEOEqpInFromReticlePodMenu(){
        reticleEqpInOutCase.DRB1_6_9_ReticleXferStatusEOEqpInFromReticlePodMenu();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_10_Add_Reticle_to_Equipment(){
        reticleEqpInOutCase.DRB1_6_10_Add_Reticle_to_Equipment();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_11_Reticle_Processing_After_EqpIn(){
        reticleEqpInOutCase.DRB1_6_11_Reticle_Processing_After_EqpIn();
    }
}
