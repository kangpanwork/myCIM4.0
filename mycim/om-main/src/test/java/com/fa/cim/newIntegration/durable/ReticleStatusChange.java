package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.ReticleStatusChangeCase;
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
 * <p>ReticleSubStatesChange .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/2/21/021   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/2/21/021 09:50
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class ReticleStatusChange {

    @Autowired
    private ReticleStatusChangeCase reticleStatusChangeCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_1_ReticleStatusAvailableToNotAvailable(){
        reticleStatusChangeCase.DRB6_2_1_ReticleStatusAvailableToNotAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_2_ReticleStatusNotAvailableToAvailable(){
        reticleStatusChangeCase.DRB6_2_2_ReticleStatusNotAvailableToAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_3_ReticleStatusAvailableToScrapped(){
        reticleStatusChangeCase.DRB6_2_3_ReticleStatusAvailableToScrapped();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_4_ReticleStatusScrapedToAvailable(){
        reticleStatusChangeCase.DRB6_2_4_ReticleStatusScrapedToAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_5_ReticleStatusScrappedToNotAvailable(){
        reticleStatusChangeCase.DRB6_2_5_ReticleStatusScrappedToNotAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_6_ReticleStatusNotAvailableToScrapped(){
        reticleStatusChangeCase.DRB6_2_6_ReticleStatusNotAvailableToScrapped();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_7_ReticleStatusAvailableToInUse(){
        reticleStatusChangeCase.DRB6_2_7_ReticleStatusAvailableToInUse();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_8_ReticleStatusInUseToAvailable(){
        reticleStatusChangeCase.DRB6_2_8_ReticleStatusInUseToAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_9_ReticleStatusNotAvailableToInUse(){
        reticleStatusChangeCase.DRB6_2_9_ReticleStatusNotAvailableToInUse();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_10_ReticleStatusInUseToNotAvailable(){
        reticleStatusChangeCase.DRB6_2_10_ReticleStatusInUseToNotAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_11_ReticleStatusScrappedToInUse(){
        reticleStatusChangeCase.DRB6_2_11_ReticleStatusScrappedToInUse();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB6_2_12_ReticleStatusInUseToScrapped(){
        reticleStatusChangeCase.DRB6_2_12_ReticleStatusInUseToScrapped();
    }

}
