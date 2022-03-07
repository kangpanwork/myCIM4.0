package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.ReticlePodEqpInOutCase;
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
 * <p>ReticlePodEqpInOut .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/2/24/024   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/2/24/024 12:47
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class ReticlePodEqpInOut {

    @Autowired
    private ReticlePodEqpInOutCase reticlePodEqpInOutCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_1_ReticlePodStatusEOEqpIn(){
        reticlePodEqpInOutCase.DRB1_6_1_ReticlePodStatusEOEqpIn();
    }

    //此操作会被前端拦截，跳过test
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_2_ReticlePodStatusEIEqpIn(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_3_ReticlePodNoStatusEqpIn(){
        reticlePodEqpInOutCase.DRB1_6_3_ReticlePodNoStatusEqpIn();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_4_ReticlePodNeedNoReticleEqpIn(){
        reticlePodEqpInOutCase.DRB1_6_4_ReticlePodNeedNoReticleEqpIn();
    }

    //此操作会被前端拦截，跳过test
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_5_ReticlePodStatusEOEqpOut(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_6_ReticlePodStatusEIEqpOut(){
        reticlePodEqpInOutCase.DRB1_6_6_ReticlePodStatusEIEqpOut();
    }

    //此操作会被前端拦截，跳过test
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_6_7_ReticlePodNoStatusEqpOut(){
    }

    //DRB1_6_8,DRB1_6_9,DRB1_6_10都在ReticleEqpInOut里有相关测试

}
