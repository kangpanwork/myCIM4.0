package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.ReticleJustInOutCase;
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
 * <p>ReticleJustInOut .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/9/009   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/9/009 13:16
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class ReticleJustInOut {

    @Autowired
    private ReticleJustInOutCase reticleJustInOutCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB4_1_4OneReticleJustIn(){
        reticleJustInOutCase.DRB4_1_4OneReticleJustIn();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB4_1_6OneReticleJustOut(){
        reticleJustInOutCase.DRB4_1_6OneReticleJustOut();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB4_1_8ReticleJustOut(){
        reticleJustInOutCase.DRB4_1_8ReticleJustOut();
    }
}
