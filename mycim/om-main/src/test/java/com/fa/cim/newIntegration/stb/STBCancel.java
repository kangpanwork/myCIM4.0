package com.fa.cim.newIntegration.stb;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.stb.scase.STBCancelCase;
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
 * This script is created based on the Test Cases provided in SVN.
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Neko
 * @date 2019/9/11 15:30
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class STBCancel {
    @Autowired
    private STBCancelCase stbCancelCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STBCancel_NormalCase(){
        stbCancelCase.STBCancel_NormalCase();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STBCancel_OnHoldLot(){
        stbCancelCase.STBCancel_OnHoldLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STBCancel_ProcessingLot(){
        stbCancelCase.STBCancel_ProcessingLot();
    }


}
