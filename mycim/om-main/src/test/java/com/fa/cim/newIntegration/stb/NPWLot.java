package com.fa.cim.newIntegration.stb;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.stb.scase.NPWLotCase;
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
 * @date 2019/9/20 9:56
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class NPWLot {

    @Autowired
    private NPWLotCase npwLotCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void NPWLot_ProcessMonitorLotSTBBeforeProcess(){
        //PRC-13-2-1 Lot Start for NPW  lot
        npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess();
    }
}
