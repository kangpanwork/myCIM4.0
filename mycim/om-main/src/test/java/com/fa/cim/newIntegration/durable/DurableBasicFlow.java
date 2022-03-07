package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.DurableBasicFlowCase;
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
 * <p>DurableBasicFlow .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/2/27/027   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/2/27/027 09:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class DurableBasicFlow {

    @Autowired
    private DurableBasicFlowCase durableBasicFlowCase;


    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRBCarrierList(){
        durableBasicFlowCase.DRBCarrierList();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB3_2_1CarrierInformation(){
        durableBasicFlowCase.DRB3_2_1CarrierInformation();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_3_1ReticleInformation(){
        durableBasicFlowCase.DRB1_3_1ReticleInformation();
    }
}
