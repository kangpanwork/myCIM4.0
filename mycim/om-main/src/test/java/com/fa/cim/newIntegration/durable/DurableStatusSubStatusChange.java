package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.DurableStatusSubStatusChangeCase;
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
 * <p>DurableStatusSubStatusChange .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/4/004   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/4/004 10:42
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class DurableStatusSubStatusChange {

    @Autowired
    private DurableStatusSubStatusChangeCase durableStatusSubStatusChangeCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_1DurableStatusChangeFromrAvailableToNotavailable(){
        durableStatusSubStatusChangeCase.DUR_2_3_1_CarrierStatusAvailableToNotAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_2_CarrierStatusNotAvailableToAvailable(){
        durableStatusSubStatusChangeCase.DUR_2_3_2_CarrierStatusNotAvailableToAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_3_CarrierStatusAvailableToScrapped(){
        durableStatusSubStatusChangeCase.DUR_2_3_3_CarrierStatusAvailableToScrapped();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_4_CarrierStatusScrapedToAvailable(){
        durableStatusSubStatusChangeCase.DUR_2_3_4_CarrierStatusScrapedToAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_5_CarrierStatusScrappedToNotAvailable(){
        durableStatusSubStatusChangeCase.DUR_2_3_5_CarrierStatusScrappedToNotAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_6_CarrierStatusNotAvailableToScrapped(){
        durableStatusSubStatusChangeCase.DUR_2_3_6_CarrierStatusNotAvailableToScrapped();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_7_CarrierStatusAvailableToInUse(){
        durableStatusSubStatusChangeCase.DUR_2_3_7_CarrierStatusAvailableToInUse();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_8_CarrierStatusInUseToAvailable(){
        durableStatusSubStatusChangeCase.DUR_2_3_8_CarrierStatusInUseToAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_9_CarrierStatusNotAvailableToInUse(){
        durableStatusSubStatusChangeCase.DUR_2_3_9_CarrierStatusNotAvailableToInUse();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_10_CarrierStatusInUseToNotAvailable(){
        durableStatusSubStatusChangeCase.DUR_2_3_10_CarrierStatusInUseToNotAvailable();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_11_CarrierStatusScrappedToInUse(){
        durableStatusSubStatusChangeCase.DUR_2_3_11_CarrierStatusScrappedToInUse();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_12_CarrierStatusInUseToScrapped(){
        durableStatusSubStatusChangeCase.DUR_2_3_12_CarrierStatusInUseToScrapped();
    }

    //目前没有这个参数，无法测试
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_13_CarrierStatusChangeWhenChangeFlagIsFlase(){
    }

    //和1-12测试本质相同，跳过
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_14_CarrierStatusChangeWithDifferentStatus(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_15_CarrierStatusChangeWithSameStatus(){
        durableStatusSubStatusChangeCase.DUR_2_3_15_CarrierStatusChangeWithSameStatus();
    }

    //和测试3.6相同，跳过
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_16_EmptyCarrierStatusChangeToScrapped(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR_2_3_17_NotEmptyCarrierStatusChangeToScrapped(){
        durableStatusSubStatusChangeCase.DUR_2_3_17_NotEmptyCarrierStatusChangeToScrapped();
    }
}
