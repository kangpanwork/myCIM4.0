package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.CarrierStockInOutCase;
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
 * <p>CarrierStockInOut .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/2/25/025   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/2/25/025 12:40
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class CarrierStockInOut {
    @Autowired
    private CarrierStockInOutCase carrierStockInOutCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB2_4_1_Stockin(){
        carrierStockInOutCase.DRB2_4_1_Stockin();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB2_4_2_CarrierNoTransferStatusStockin(){
        carrierStockInOutCase.DRB2_4_2_CarrierNoTransferStatusStockin();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB2_4_3_CarrierMIStatusStockin(){
        carrierStockInOutCase.DRB2_4_3_CarrierMIStatusStockin();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB2_4_4_CarrierMoStatusStockin(){
        carrierStockInOutCase.DRB2_4_4_CarrierMoStatusStockin();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB2_4_5_CarrierEOStatusStockin(){
        carrierStockInOutCase.DRB2_4_5_CarrierEOStatusStockin();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB2_4_6_CarrierEIStatusStockin(){
        carrierStockInOutCase.DRB2_4_6_CarrierEIStatusStockin();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB2_4_7_StockOut(){
        carrierStockInOutCase.DRB2_4_7_StockOut();
    }
}
