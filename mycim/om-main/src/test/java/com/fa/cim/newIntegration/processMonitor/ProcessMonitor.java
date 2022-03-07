package com.fa.cim.newIntegration.processMonitor;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.processMonitor.scase.ProcessMonitorCase;
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
 * 2019/11/27                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/11/27 15:06
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class ProcessMonitor {

    @Autowired
    private ProcessMonitorCase processMonitorCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Bind_MonitorLot_To_The_ProductLot(){
        //PRC-13-6-1 one Monitor Lot group with one Production Lot (PM主---NP)
        //PRC-13-6-3  one product lot grouped by multiple monitoring lot (n different PM主---NP)
        processMonitorCase.Bind_MonitorLot_To_The_ProductLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Put_MonitorLot_Grouping_Into_MonitorLot(){
        //PRC-13-6-2 one Monitor Lot group with one Monitor Lot (PM---PM)
        processMonitorCase.Put_MonitorLot_Grouping_Into_MonitorLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void One_MonitorLot_Binds_MultipleLots(){
        //PRC-13-6-4  one product lot grouped by multiple monitoring lot (n PM主---NP)
        processMonitorCase.One_MonitorLot_Binds_MultipleLots();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void One_MonitoringLot_Can_Only_Have_One_Grouping(){
        //PRC-13-6-5  one monitoring lot group with multiple product lot (PM主---n NP)
        processMonitorCase.One_MonitoringLot_Can_Only_Have_One_Grouping();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void One_ProcessLot_CanNot_Have_MoreThan_One_Grouping(){
        //PRC-13-6-6 one process lot group ultiple different monitor lot (NP主---n PM)
        processMonitorCase.One_ProcessLot_CanNot_Have_MoreThan_One_Grouping();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void LotA_Bound_LotB_And_LotB_Bound_LotC(){
        //PRC-13-6-7 Multiple grouping (A grouping B,B grouping C)
        processMonitorCase.LotA_Bound_LotB_And_LotB_Bound_LotC();
    }

    //todo PRC-13-6-8 Monitor grouping after lots selection in What's Next (no testing)
    //todo PRC-13-6-9 Monitor grouping after lot start in What's Next (no testing)

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void GroupingCancel_OneMonitorLot_Grouping_OneMonitorLot(){
        //PRC-13-7-1 Grouping Cancel 1 MONITOR lot grouping 1 monitor lot
        processMonitorCase.GroupingCancel_OneMonitorLot_Grouping_OneMonitorLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void GroupingCancel_OneMonitorLot_Grouping_OneProcessLot(){
        //PRC-13-7-2 Grouping Cancel 1 MONITOR lot grouping 1 process lot (Bind a Monitor Lot to the product Lot)
        processMonitorCase.GroupingCancel_OneMonitorLot_Grouping_OneProcessLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void GroupingCancel_OneMonitorLot_Grouping_MultipleProcessLots(){
        //PRC-13-7-3 Grouping Cancel 1 MONITOR lot grouping multiple process lots
        processMonitorCase.GroupingCancel_OneMonitorLot_Grouping_MultipleProcessLots();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void GroupingCancel_OneProcessLot_Can_Have_MoreThanOne_Grouping(){
        processMonitorCase.GroupingCancel_OneProcessLot_Can_Have_MoreThanOne_Grouping();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void GroupingCancel_A_GroupingB_And_BGroupingC(){
        //PRC-13-7-4 Grouping Cancel A grouping B,B grouping C
        processMonitorCase.GroupingCancel_A_GroupingB_And_BGroupingC();
    }

    //todo PRC-13-7-5 Auto Group cancel after move out with greoup cancel flag is setted in MDS (no testing)
    //todo PRC-13-7-6 Monitor group cancel one of the monitored lot (no testing)

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Reserve_Tow_ProductionLots_And_One_LoadPurpose_As_ProcessLot_And_AnotherOne_LoadPurpose_As_ProcessMonitorLot(){
        //PRC-13-6-10 Monitor lot and Process lot auto grouping after move in with move in reserve
        //PRC-13-6-14 Reserve 2 prouduction lots ,one load purpose as Process Lot,another one load purpose as Process Monitor Lot
        processMonitorCase.Reserve_Tow_ProductionLots_And_One_LoadPurpose_As_ProcessLot_And_AnotherOne_LoadPurpose_As_ProcessMonitorLot();
    }


    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Load_Two_ProductionLots_WithOut_Reserve_And_One_LoadPurpose_As_ProcessLot_And_AnotherOne_LoadPurpose_As_ProcessMonitorLot_And_Check_ProcessLot_OnHold(){
        //PRC-13-6-11 Monitor lot and Process lot auto grouping after move in without move in reserve
        //PRC-13-6-15 Load 2  prouduction lots without reserve,one load purpose as Process Lot,another one load purpose as Process Monitor Lot
        processMonitorCase.Load_Two_ProductionLots_WithOut_Reserve_And_One_LoadPurpose_As_ProcessLot_And_AnotherOne_LoadPurpose_As_ProcessMonitorLot_And_Check_ProcessLot_OnHold();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void MonitorLot_And_ProcessLot_AutoGrouping_After_MoveIn_With_MoveIn_Reserve(){
        //PRC-13-6-12 Monitor lot and Process lot auto grouping after move in with move in reserve
        processMonitorCase.MonitorLot_And_ProcessLot_AutoGrouping_After_MoveIn_With_MoveIn_Reserve();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Load_Two_ProductionLots_WithOut_Reserve_And_One_LoadPurpose_As_ProcessLot_And_AnotherOne_LoadPurpose_As_ProcessMonitorLot_And_Check_ProcessLot_OnHold_For_InternalBuffer(){
        //PRC-13-6-13 Monitor lot and Process lot auto grouping after move in without move in reserve
        //PRC-13-6-15 Load 2  prouduction lots without reserve,one load purpose as Process Lot,another one load purpose as Process Monitor Lot
        processMonitorCase.Load_Two_ProductionLots_WithOut_Reserve_And_One_LoadPurpose_As_ProcessLot_And_AnotherOne_LoadPurpose_As_ProcessMonitorLot_And_Check_ProcessLot_OnHold_For_InternalBuffer();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void LotHold_For_MonitorGrouping(){
        //PRC-13-6-16 Monitor lot group a process lot which has onhold status
        processMonitorCase.LotHold_For_MonitorGrouping();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void MonitorLot_Did_Split_First_AndThen_Merge(){
        //PRC-13-6-17 Merge a splited monitor lot which has been monitoring two process lots seperately
        processMonitorCase.MonitorLot_Did_Split_First_AndThen_Merge();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Grouped_MonitorLot_Split_And_TheChildLot_DoesNot_Have_TheGroupInfo_But_TheMotherLot_Have_TheGroupInfo(){
        //PRC-13-6-18 Split monitor lot before group, then group process lots with parent lot and child lot
        processMonitorCase.Grouped_MonitorLot_Split_And_TheChildLot_DoesNot_Have_TheGroupInfo_But_TheMotherLot_Have_TheGroupInfo();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Grouped_MonitorLot_ShouldNot_Skip(){
        //PRC-13-6-19 Grouped Monitor Lot to skip to other operation
        processMonitorCase.Grouped_MonitorLot_ShouldNot_Skip();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Grouped_ProcessLot_Can_Skip_If_NotOnHold(){
        //PRC-13-6-20 Grouped Process Lot can  skip (locate) if not ONHOLD
        processMonitorCase.Grouped_ProcessLot_Can_Skip_If_NotOnHold();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Grouped_Monitor_Lot_To_Split(){
        //PRC-13-6-21 Grouped Monitor Lot to split
        processMonitorCase.Grouped_Monitor_Lot_To_Split();
    }
    //todo PRC-13-6-22 Grouping step by step (no testing)
    //todo PRC-13-6-23 Monitor lot monitoring a process lot with scrapped wafer in monitored lot (no testing)
    //todo PRC-13-6-24 Monitor lot monitoring a process lot with scrapped wafer in monitoring lot (no testing)
    //todo PRC-13-6-25 Monitor lot monitoring a process lot with empty carrier exist in monitoring lot/monitored lot (no testing)
    //todo PRC-13-6-26 Monitor lot monitoring a process lot which monitoring lot/monitored lot lotProcessState is Processing (no testing)

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void AutoGrouping_By_EqpMoveOut_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OK(){
        //PRC-13-2-1-2 Auto grouping(monitor lot and process lot) after move out
        processMonitorCase.AutoGrouping_By_EqpMoveOut_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OK();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void AutoGrouping_By_EqpMoveOut_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OverLimit(){
        //PRC-13-2-1-3 monitor lot run at measurment step if result is good,monitored lot will be released,else will onhold with reason code : uppeer/lower limit
        processMonitorCase.AutoGrouping_By_EqpMoveOut_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OverLimit();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void ManuelGrouping_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OK(){
        //PRC-13-2-4 A process lot and a monitor lot group togather and use monitor lot to processing measurement step(case1)
        processMonitorCase.ManuelGrouping_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OK();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void ManuelGrouping_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OverLimit(){
        //PRC-13-2-4 A process lot and a monitor lot group togather and use monitor lot to processing measurement step(case2)
        processMonitorCase.ManuelGrouping_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OverLimit();
    }

    //todo PRC-13-2-5 Monitor grouping STB after processing in internal buffer EQP (need configuration data)

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Monitor_Grouping_STB_After_Processing_In_Normal_EQP_And_Then_Use_Monitor_Lot_Processing_Measurement_Step(){
        //PRC-13-2-6 Monitor grouping STB after processing in normal EQP
        processMonitorCase.Monitor_Grouping_STB_After_Processing_In_Normal_EQP_And_Then_Use_Monitor_Lot_Processing_Measurement_Step();
    }

    //todo PRC-13-2-8 Monitor grouping STB after processing in normal EQP, then use monitor lot processing measurement step
    //todo PRC-13-2-7 Monitor grouping STB after processing in internal buffer EQP, then use monitor lot processing measurement step (need configuration data)
    //todo PRC-13-2-9 Gate Pass is performed to measurement operation of Lot B when Monitor Group and Monitor Hold is released (no testing)
}
