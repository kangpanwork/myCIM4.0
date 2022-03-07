package com.fa.cim.newIntegration.PlanSplitMerge;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.PlanSplitMerge.scase.PlanSplitMergeCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * description: PSM test case
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/4                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/12/4 14:04
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class PlanSplitMerge {

    @Autowired
    private PlanSplitMergeCase planSplitMergeCase;

    /*************************************** Planned Split(Sub-route) ************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedSplit_With_DynamicRouteID_And_NonAction_OrAction(){
        //EWR3-1-1 Planned split with DynamicRouteID&Non-Action
        //EWR3-1-2 Planned split with DynamicRouteID&Action
        //EWR3-1-21 Planned Merge with Onhold Child-Lot and Parent Lot
        //EWR3-1-52 Execute PSM at split point and merge,then skip back to split point again,check if PSM will spli again
        planSplitMergeCase.PlannedSplit_With_DynamicRouteID_And_NonAction_OrAction();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedSplit_With_SubRouteID_And_NonAction_OrAction(){
        //EWR3-1-3 Planned split with SubRouteID&Non-Action
        //EWR3-1-4 Planned split with SubRouteID&Action
        planSplitMergeCase.PlannedSplit_With_SubRouteID_And_NonAction_OrAction();
    }

    //EWR3-1-5 Planned split at Rework operation ignore
    //EWR3-1-6 Planned split with Rework Cancel ignore
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedSplit_With_Branch_Cancel(){
        //EWR3-1-7 Planned split with Branch Cancel
        planSplitMergeCase.PlannedSplit_With_Branch_Cancel();
    }
    //【todo】EWR3-1-8 Planned split with Wafer Sorter (no testing)


    /*************************************** Planned Split(scrapped wafers) ******************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedSplit_With_Scrapped_Wafers_Reserved_For_ChildLot(){
        //EWR3-1-9 Planned split with scrapped wafers(scrapped wafer reserved for a Child Lot)
        planSplitMergeCase.PlannedSplit_With_Scrapped_Wafers_Reserved_For_ChildLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedSplit_With_Scrapped_Wafers_Reserved_For_ParentLot(){
        //EWR3-1-10 Planned split with scrapped wafers(scrapped wafer reserved for a Parent Lot)
        planSplitMergeCase.PlannedSplit_With_Scrapped_Wafers_Reserved_For_ParentLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedSplit_With_Scrapped_Wafers_Reserved_Except_ScrappedWafer(){
        //EWR3-1-11 Planned split with scrapped wafers(scrapped wafer researved for a Parent Lot but just one of the wafers)
        planSplitMergeCase.PlannedSplit_With_Scrapped_Wafers_Reserved_Except_ScrappedWafer();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedSplit_With_Scrapped_Wafers_Reserved_Only_ScrappedWafer(){
        //EWR3-1-12 Planned split with scrapped wafers(scrapped wafer researved for a Child Lot but just one of the wafers)
        planSplitMergeCase.PlannedSplit_With_Scrapped_Wafers_Reserved_Only_ScrappedWafer();
    }

    /*************************************** Planned Split(future hold) **********************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Register_PSM_With_FutureHold_Between_SplitPoint_And_MergePoint(){
        //EWR3-1-13 Registrate a PSM with future hold(between split point and merge point)
        planSplitMergeCase.Register_PSM_With_FutureHold_Between_SplitPoint_And_MergePoint();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedSplit_With_FutureHold_At_SplitPoint_Pos(){
        //EWR3-1-14 Planned split with future hold at split point(pos)
        planSplitMergeCase.PlannedSplit_With_FutureHold_At_SplitPoint_Pos();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedSplit_With_FutureHold_At_SplitPoint_Pre(){
        //EWR3-1-15 Planned split with future hold at split point(pre)
        planSplitMergeCase.PlannedSplit_With_FutureHold_At_SplitPoint_Pre();
    }

    /*************************************** Planned Split(other case) ***********************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedSplit_With_Operation_LocateTo_NextStep_Then_BackwardTo_Current_SplitStep(){
        //EWR3-1-16 Planned split with operation locate to next step(current panned split did not operate) then backward to current split step
        planSplitMergeCase.PlannedSplit_With_Operation_LocateTo_NextStep_Then_BackwardTo_Current_SplitStep();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Specify_SplitPoints_Or_MergePoints_On_The_Operations_In_FlowBatch_Section(){
        //EWR3-1-17 Users can’t specify split points or merge points on the operations in a flow batch section except its entry point
        planSplitMergeCase.Specify_SplitPoints_Or_MergePoints_On_The_Operations_In_FlowBatch_Section();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedSplit_After_MoveOut(){
        //EWR3-1-18 Planned split after Operation Comp
        planSplitMergeCase.PlannedSplit_After_MoveOut();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Specify_SplitPoints_On_The_EntryPoint_Of_A_FlowBatch_Section(){
        //EWR3-1-19 Specify split points on the entry point of a flow batch section
        planSplitMergeCase.Specify_SplitPoints_On_The_EntryPoint_Of_A_FlowBatch_Section();

    }
    //【todo】EWR3-1-20 Planned split when Lot ReQueue ignore (no coding and no testing)
    /*************************************** Planned Merge(Lot hold) *************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedMerge_With_OnHold_With_ChildLot_Or_ParentLot(){
        //EWR3-1-22 Planned Merge with OHold Child-Lot but not OnHold with Parent-Lot
        //EWR3-1-24 Planned Merge with not OnHold Child-Lot and Parent-Lot
        planSplitMergeCase.PlannedMerge_With_OnHold_With_ChildLot_Or_ParentLot();
    }
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PlannedMerge_With_OnHold_With_ChildLot_NotOnHold_And_ParentLot_OnHold(){
        //EWR3-1-23 Planned Merge with not OnHold Child-Lot but OnHold with Parent-Lot
        planSplitMergeCase.PlannedMerge_With_OnHold_With_ChildLot_NotOnHold_And_ParentLot_OnHold();
    }
    /*************************************** Planned Merge(merge point) **********************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void The_MergePoint_IsBefore_ReturnPoint_Of_SubRoute(){
        //EWR3-1-25 The Merge-Point is before Return-Point of a sub-route.
        //EWR3-1-26 The Merge Point is at the Split Point.
        planSplitMergeCase.The_MergePoint_IsBefore_ReturnPoint_Of_SubRoute();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void The_MergePoint_Is_At_Or_After_The_Next_Already_Defined_SplitPoint(){
        //EWR3-1-27 The Merge Point is at or after the next already-defined Split Point.
        planSplitMergeCase.The_MergePoint_Is_At_Or_After_The_Next_Already_Defined_SplitPoint();
    }
    /*************************************** Planned Split & Merge ***************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Add_SplitRecord_At_SubBranch_Which_Wafer_Did_Owned_By_Branch_Or_Not(){
        //EWR3-1-28 Add a split record at a sub-branch which wafer did not owned by branch
        //EWR3-1-29 Add a split record at a sub-branch which wafer owned by branch
        //EWR3-1-31 Add split record at two different branch(图4)
        planSplitMergeCase.Add_SplitRecord_At_SubBranch_Which_Wafer_Did_Owned_By_Branch_Or_Not();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Add_splitRecord_At_SubBranch_Which_WaferOwned_By_Branch_All_Wafers_Are_Branch_To_Sub_Route(){
        //EWR3-1-30 Add a split record at a sub-branch(图3)which wafer owned by branch(all wafers are branch to sub sub-route)
        planSplitMergeCase.Add_splitRecord_At_SubBranch_Which_WaferOwned_By_Branch_All_Wafers_Are_Branch_To_Sub_Route();
    }

    /*************************************** Delete PSM record *******************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Delete_A_PSM_Record(){
        //EWR3-1-32 Delete a PSM record which did not done
        //EWR3-1-35 Delete a PSM record in PSM List
        planSplitMergeCase.Delete_A_PSM_Record();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Delete_A_PSM_Record_After_It_Executed_Or_In_PSM_List(){
        //EWR3-1-33 Delete a PSM record which had been done in PSM list
        //EWR3-1-34 Delete a PSM record after it executed
        planSplitMergeCase.Delete_A_PSM_Record_After_It_Executed_Or_In_PSM_List();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Delete_PSM_In_Detail_Which_Has_Two_Records_With_Same_Split_And_MergePoint_But_Different_SubRoute_And_Wafers(){
        //EWR3-1-36 Delete PSM in detail info page which has two records with same split and merge point ,but different sub-route and wafers
        planSplitMergeCase.Delete_PSM_In_Detail_Which_Has_Two_Records_With_Same_Split_And_MergePoint_But_Different_SubRoute_And_Wafers();
    }
    /*************************************** Update PSM record *******************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Modify_PSM_Record(){
        //EWR3-1-37 Modify wafer id in a PSM which is branch sub-route
        //EWR3-1-38 Modify wafer id in a PSM which is dynamic sub-route
        //EWR3-1-39 Update a PSM change return point(Dynamic) and merge point
        //EWR3-1-40 Update a PSM change Actions
        planSplitMergeCase.Modify_PSM_Record();
    }
    /***************************************** PSM other case *******************************************/
    //【todo】EWR3-1-41 After Move Out, there is supposed to be a PSM in the next step but a PCS script is executed during Move Out and the lot is located to another step ignore (no testing)
    //【todo】EWR3-1-42 PSM is created during lot processing in equipment. If Move In Cancel is done，PSM will not execute (no need to write the case)

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void After_Successful_GatePass_On_CurrentStep_And_The_Lot_MoveTo_The_NextStep_Which_Has_A_PSM_And_PSM_Will_Execute(){
        //EWR3-1-43 After successful Gate Pass on current step and the lot move to the next step which has a PSM.  PSM will execute
        planSplitMergeCase.After_Successful_GatePass_On_CurrentStep_And_The_Lot_MoveTo_The_NextStep_Which_Has_A_PSM_And_PSM_Will_Execute();
    }

    //【todo】EWR3-1-44 There is a Rework set in PCS script for the current step. In this case, during Move Out, though the lot on the current process joins the WIP queue of the next process (the Split-Point), the PSM will not be carried out because the script is executed first (no testing)
    //【todo】EWR3-1-45 Based on the last case,if delete rework script,then execute rework cancel (no testing)
    //【todo】EWR3-1-46 There is a Branch set in PCS script for the current step. In this case, during Move Out, though the lot on the current process joins the WIP queue of the next process (the Split-Point), the PSM will not be carried out because the script is executed first (no testing)
    //【todo】EWR3-1-47 Based on the last case,if delete branch script,then execute branch cancel (no testing)

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void During_TheLot_Is_OnHold_Register_A_PSM_Record_In_CurrentOperation_Then_HoldRelease(){
        //EWR3-1-48 During the lot is on hold,registrate a PSM record in current operation,then hold release
        planSplitMergeCase.During_TheLot_Is_OnHold_Register_A_PSM_Record_In_CurrentOperation_Then_HoldRelease();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void One_SplitPoint_Linked_To_Multiple_SubRoutes(){
        //EWR3-1-49 One split point linked to multiple sub-routes
        planSplitMergeCase.One_SplitPoint_Linked_To_Multiple_SubRoutes();
    }

    //【todo】EWR3-1-50 There is a Skip set in PCS script for the current step. In this case, during Move Out, though the lot on the current process joins the WIP queue of the next process (the Split-Point), the PSM will not be carried out because the script is executed first (can not psc test)

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void A_Lot_Was_Moved_To_A_Step_Which_Has_PSM_Using_Schedule_Change_SPS(){
        //EWR3-1-51 A lot was moved to a step which has PSM using schedule change (no testing) SPS
        planSplitMergeCase.A_Lot_Was_Moved_To_A_Step_Which_Has_PSM_Using_Schedule_Change_SPS();
    }
}
