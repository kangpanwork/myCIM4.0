package com.fa.cim.newIntegration.durable;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.durable.scase.ReticleListCase;
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
 * <p>ReticleList .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/9/009   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/9/009 16:30
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class ReticleList {

    @Autowired
    private ReticleListCase reticleListCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_1_1ReticleListInquiryForAllRticles(){
        reticleListCase.DRB1_1_1ReticleListInquiryForAllRticles();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_1_2ReticleListInquiryViaLotID(){
        reticleListCase.DRB1_1_2ReticleListInquiryViaLotID();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_1_3ReticleListInquiryViaDurableStatus(){
        reticleListCase.DRB1_1_3ReticleListInquiryViaDurableStatus();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_1_4ReticleListInquiryViaEquipmentID(){
        reticleListCase.DRB1_1_4ReticleListInquiryViaEquipmentID();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_1_5ReticleListInquiryViaEquipmentIDAndLotID(){
        reticleListCase.DRB1_1_5ReticleListInquiryViaEquipmentIDAndLotID();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_1_6ReticleListInquiryViaReticleID(){
        reticleListCase.DRB1_1_6ReticleListInquiryViaReticleID();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_1_7ReticleListInquiryViaReticlePartNO(){
        reticleListCase.DRB1_1_7ReticleListInquiryViaReticlePartNO();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void DRB1_1_7ReticleListInquiryViaReticleGroupID(){
        reticleListCase.DRB1_1_7ReticleListInquiryViaReticleGroupID();
    }
}
