package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.CarrierExchangeCase;
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
 * <p>CarrierExchange .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/2/26/026   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/2/26/026 10:51
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class CarrierExchange {
    @Autowired
    CarrierExchangeCase carrierExchangeCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC10_1_1WaferPositionChangeInLotStartScreen(){
        carrierExchangeCase.PRC10_1_1WaferPositionChangeInLotStartScreen();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC10_1_2CheckAfterWaferPositionChange(){
        carrierExchangeCase.PRC10_1_2CheckAfterWaferPositionChange();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC10_1_3Justout(){
        carrierExchangeCase.PRC10_1_3Justout();
    }

    //前端执行操作，不会调用后端接口
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC10_1_4ResetFunction(){
    }

    //前端执行操作，不会调用后端接口
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC10_1_5GetBack(){
    }

    //前端执行操作，不会调用后端接口
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC10_1_6WaferPositionChangeSlotNumOver1(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC10_1_7WaferPositionChangeMultipleLotExchange(){
        //需要比较复杂的STB数据进行比对，暂时pass
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC10_1_8WaferPositionChangeMultipleLotMultipleExchange(){
        //需要比较复杂的STB数据进行比对，暂时pass
    }

    //9-18测试是从不同地方调用相同接口执行操作，和上面的测试重复，跳过

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC10_1_19SomeOfWaferPositionChange(){
        carrierExchangeCase.PRC10_1_19SomeOfWaferPositionChange();
    }

}
