package com.fa.cim.newIntegration.sps;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.sps.scase.LotScheduleCase;
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
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/16                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/12/16 14:12
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class LotSchedule {

    @Autowired
    private LotScheduleCase lotScheduleCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Lot_Creation_By_Volume_Schedule(){
        //SPS1-1-1 -> SPS1-1-6  Lot Creation(By Volume)
        //SPS1-3-1 -> SPS1-3-6  Lot Creation(By SSL) : same as By Volume so skip the case
        lotScheduleCase.Lot_Creation_By_Volume_Schedule_Daily();
    }


    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Lot_Creation_By_Source_Lot_Inheriting(){
        //SPS1-2-1 -> SPS1-2-5 Lot Creation(By Source Lot)
        lotScheduleCase.Lot_Creation_By_Source_Lot_Inheriting();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Lot_Creation_By_Source_Lot_Next(){
        //SPS1-2-1 -> SPS1-2-5 Lot Creation(By Source Lot)
        lotScheduleCase.Lot_Creation_By_Source_Lot_Next();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Lot_Change(){
        //SPS2-1-1 -> SPS2-1-3 Lot Change
        lotScheduleCase.Lot_Change();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Lot_Change_For_Pre_LotStart(){
        //SPS2-1-1 -> SPS2-1-3 Lot Change
        lotScheduleCase.Lot_Change_For_Pre_LotStart();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Lot_Change_For_Bank_In(){
        //SPS2-1-1 -> SPS2-1-3 Lot Change
        lotScheduleCase.Lot_Change_For_Bank_In();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Pre_Lot_Start_Cancel(){
        //SPS3-1-1 -> SPS3-1-4 Pre-Lot Start Cancel
        lotScheduleCase.Pre_Lot_Start_Cancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Pre_Lot_Start_Cancel_By_Source_Lot(){
        //SPS3-2-1 -> SPS3-2-3 Pre-Lot Start Cancel(By Source Lot)
        lotScheduleCase.Pre_Lot_Start_Cancel_By_Source_Lot();
    }

}
