package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.CarrierListbyCategoryCase;
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
 * <p>CarrierListbyCategory .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/3/003   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/3/003 13:12
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class CarrierListbyCategory {
    @Autowired
    CarrierListbyCategoryCase carrierListbyCategoryCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR2_1_1NormalUseOfCarrierListByCategory(){
        carrierListbyCategoryCase.DUR2_1_1NormalUseOfCarrierListByCategory();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR2_1_2CarrierListSearchByCarrierID(){
        carrierListbyCategoryCase.DUR2_1_2CarrierListSearchByCarrierID();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR2_1_3CarrierListSearchByCarrierType(){
        carrierListbyCategoryCase.DUR2_1_3CarrierListSearchByCarrierType();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR2_1_4CarrierListSearchByStationID(){
        carrierListbyCategoryCase.DUR2_1_4CarrierListSearchByStationID();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DUR2_1_5CarrierListSearchByWrongInfo(){
        carrierListbyCategoryCase.DUR2_1_5CarrierListSearchByWrongInfo();
    }
}
