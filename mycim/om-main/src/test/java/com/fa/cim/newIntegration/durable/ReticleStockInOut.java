package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.ReticleStockInOutCase;
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
 * <p>ReticleStockInOut .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/16/016   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/16/016 14:31
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class ReticleStockInOut {

    @Autowired
    private ReticleStockInOutCase reticleStockInOutCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_5_1_StockInWhenEIEOAndPodAssociated(){
        reticleStockInOutCase.DRB1_5_1_StockInWhenEIEOAndPodAssociated();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_5_2_StockInWhenEIEO(){
        reticleStockInOutCase.DRB1_5_2_StockInWhenEIEO();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_5_3_StockInWhenHIHO(){
        reticleStockInOutCase.DRB1_5_3_StockInWhenHIHO();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_5_4_StockInWhenHIHOAndPodAssociated(){
        reticleStockInOutCase.DRB1_5_4_StockInWhenHIHOAndPodAssociated();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_5_5_StockOutWhenReticleHI(){
        reticleStockInOutCase.DRB1_5_5_StockOutWhenReticleHI();
    }
}
