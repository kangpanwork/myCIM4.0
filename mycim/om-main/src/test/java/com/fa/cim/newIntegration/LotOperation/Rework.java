package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.ReworkCase;
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
 * 2019/12/11                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/12/11 9:44
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class Rework {

    @Autowired
    private ReworkCase reworkCase;

    /********************************* Basic flow of all rework ***************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Basic_Flow_Of_All_Rework(){
        //1 Full rework at the next step of rework operation
        //2 Full rework not at the next step of  rework operation
        //3 When lot move to  rework flow,check Lot Production Status
        //6 When Future Hold is set later than rework
        reworkCase.Basic_Flow_Of_All_Rework();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Rework_With_Hold_Release(){
        //4 Rework with hold release
        //45 Lot with hold status to do Full rework
        reworkCase.Rework_With_Hold_Release();
    }

    /********************************* Rework with future hold *****************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void When_FutureHold_Is_Set_On_A_Process_Between_The_Original_Process_And_The_ReturnPoint_Post(){
        //5 When Future Hold is set on a process between the original process and the Return Point （post）
        reworkCase.When_FutureHold_Is_Set_On_A_Process_Between_The_Original_Process_And_The_ReturnPoint_Post();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void When_FutureHold_Is_Set_On_A_Process_Between_The_Original_Process_And_The_ReturnPoint_Post_Single(){
        //8 When Future Hold is set on a process between the original process and the Return Point （Per Single）
        reworkCase.When_FutureHold_Is_Set_On_A_Process_Between_The_Original_Process_And_The_ReturnPoint_Pre_Single();
    }


    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void When_FutureHold_Is_Set_On_A_Process_Between_The_Original_Process_And_The_ReturnPoint_Pre_Multiple(){
        //7 Full Rework with future hold in current step
        //9 When Future Hold is set on a process between the original process and the Return Point （Per Multiple）
        //30 PRODUCT0.01（rework count）full rework 3000.0200
        reworkCase.When_FutureHold_Is_Set_On_A_Process_Between_The_Original_Process_And_The_ReturnPoint_Pre_Multiple();
    }

    /********************************* Rework Cancel *****************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void All_Rework_Cancel(){
        //10 Rework Cancel with basic flow
        //11 Rework Cancel which lot did not locates  in first step
        //12 Rework Cancel with onhold status (page don not allow the rework cancel when lot is onHold state)
        //13 After the Lot is released, Cancel at Full Rework(don need because the 12 case)
        //15 Lot is processed in Rework Flow, Cancel it, and then perform Full Rework Cancel
        reworkCase.All_Rework_Cancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Rework_Cancel_With_Processing_Status(){
        //14 Rework Cancel with " Processing “status
        reworkCase.Rework_Cancel_With_Processing_Status();
    }

    /********************************* All Rework Cancel with future hold *****************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Rework_Cancel_With_Future_Hold_Between_Current_Operation_And_Future_Operation(){
        //16 Rework Cancel with future hold between current operation and future operation
        reworkCase.Rework_Cancel_With_Future_Hold_Between_Current_Operation_And_Future_Operation();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Rework_Cancel_With_Future_Hold_In_Current_Step(){
        //17 Rework Cancel with future hold in current step
        reworkCase.Rework_Cancel_With_Future_Hold_In_Current_Step();
    }

    /************************************** Partial Rework ********************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Partial_Rework_Normal_Case(){
        //18 partial rework normal case
        reworkCase.Partial_Rework_Normal_Case();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Partial_Rework_At_FirstStep_Of_The_Route(){
        //19 partial rework at first step of the route
        reworkCase.Partial_Rework_At_FirstStep_Of_The_Route();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void  Do_Dynamic_Partial_Rework_Again_After_Already_Did_Partial_Rework_ParentLot(){
        //20 Do Dynamic partial rework again after already did  partial rework (parent lot)
        reworkCase.Do_Dynamic_Partial_Rework_Again_After_Already_Did_Partial_Rework_ParentLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Perform_Partial_Rework_And_Skip_Parent_And_Child_Lot_To_Step_After_ReturnPoint(){
        //21 perform partial rework and skip  parent/child lot to step after return point
        reworkCase.Perform_Partial_Rework_And_Skip_Parent_And_Child_Lot_To_Step_After_ReturnPoint();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Partial_Rework_With_HoldRelease(){
        //22 Partial  Rework with Hold  Release
        //26 Partial  Rework with Hold  Release and partial Rework Cancel
        //27 Partial  Rework with Hold  Release and将子Lot skip到1000.0200工步，再进行partial Rework cancel
        reworkCase.Partial_Rework_With_HoldRelease();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Partial_Rework_WithOut_HoldRelease(){
        //23 Partial  Rework WithOut Hold Release
        //28 Partial  Rework WithOut Hold Release and partial Rework Cancel
        //29 Partial  Rework WithOut Hold Release and 将子Lot skip到1000.0200工步，再进行partial Rework cancel
        //42 Partial  Rework without  Hold  Release Positive action
        reworkCase.Partial_Rework_WithOut_HoldRelease();
    }

    /************************************** Partial Rework Cancel ********************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Partial_Rework_Cancel_Normal(){
        //24 Partial Rework Function testing
        //25 Partial Rework cancel  Function testing
        reworkCase.Partial_Rework_Cancel_Normal();
    }

    /************************************** Force rework for partail rework（rework count）********************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Reach_Max_Rework_Count_For_Partial_Rework1(){
        //33 PRODUCT0.01（rework count）partial rework 2000.0320
        reworkCase.Reach_Max_Rework_Count_For_Partial_Rework_Case1();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Reach_Max_Rework_Count_For_Partial_Rework2(){
        //31 PRODUCT0.01（rework count）partial rework 3000.0200
        reworkCase.Reach_Max_Rework_Count_For_Partial_Rework_Case2();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Reach_Max_Rework_Count_For_Partial_Rework3(){
        //35 PRODUCT1.01（rework count）partial rework 6000.0100
        reworkCase.Reach_Max_Rework_Count_For_Partial_Rework_Case3();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Reach_Max_Rework_Count_For_Full_Rework1(){
        //32 PRODUCT0.01（rework count）full rework 2000.0320
        reworkCase.Reach_Max_Rework_Count_For_Full_Rework_Case1();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Reach_Max_Rework_Count_For_Full_Rework2(){
        //34 PRODUCT1.01（rework count）full rework 6000.0100
        reworkCase.Reach_Max_Rework_Count_For_Full_Rework_Case2();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Complex_Reach_Max_Rework_Count_For_Partial_Rework_Twice_And_Then_FUll_Rework1(){
        //36 PRODUCT0.01（rework count partail rework----> full rework  = 3 merge-------->check rework count 2000.0320
        reworkCase.Complex_Reach_Max_Rework_Count_For_Partial_Rework_Twice_And_Then_FUll_Rework1();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Complex_Reach_Max_Rework_Count_For_Partial_Rework_Twice_And_Then_FUll_Rework2(){
        //37 PRODUCT0.01（rework count partail rework----> full rework  = 3 merge-------->check rework count 3000.0200
        reworkCase.Complex_Reach_Max_Rework_Count_For_Partial_Rework_Twice_And_Then_FUll_Rework2();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Complex_Reach_Max_Rework_Count_For_Partial_Rework_Twice_And_Then_FUll_Rework3(){
        //38 PRODUCT1.01（rework count partail rework----> full rework  = 3 merge-------->check rework count 6000.0100
        reworkCase.Complex_Reach_Max_Rework_Count_For_Partial_Rework_Twice_And_Then_FUll_Rework3();
    }

    //39 PRODUCT1.01 PRODUCT0.01（rework count partail rework----> pratail rework ------>merge------>check rework count 6000.0100 3000.0200 2000.0400 (tested in above)

    /************************************** Force rework for partail rework（process count）********************************************/
    /************************************** Force rework for Full rework（process count）  ********************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Force_Rework_For_Full_Rework_Or_Partial_Rework_At_Process_Count(){
        //40 PRODUCT0.01 skip---->2000.0100 move in------>move out process =3 ------>skip 2000.0400---->partial rework
        //41 PRODUCT0.01 skip---->2000.0100 move in------>move out process =3 ------>skip 2000.0400---->full rework
        reworkCase.Force_Rework_For_Full_Rework_Or_Partial_Rework_At_Process_Count();
    }

    /************************************** Partial  Rework without  Hold  Release  ********************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Do_It_Again_Partial_Rework_Without_Hold_Release_Positive_Action(){
        //43 Do it again Partial  Rework without  Hold  Release Positive action
        reworkCase.Do_It_Again_Partial_Rework_Without_Hold_Release_Positive_Action();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Partial_Rework_WithOut_Hold_Release_All_Partial_Rework(){
        //44 Partial  Rework without  Hold  Release All Partial Rework
        reworkCase.Partial_Rework_WithOut_Hold_Release_All_Partial_Rework();
    }

    /************************************** Full  Rework without  Hold  Release  ********************************************/
    /************************************************** process_count的环境变量  *****(****************************************/
    //todo 46 OM_WAFER_LEVEL_PASSCOUNT_CONTROL = 2 (the wafer level code didn't developed right now)
    //47 OM_WAFER_LEVEL_PASSCOUNT_CONTROL = 0 (now the OM_WAFER_LEVEL_PASSCOUNT_CONTROL = 0,so the above test case ia test done)

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Reach_Max_Rework_Count_For_Full_Rework_Case_OnHold_Partial_Rework_With_Hold_Release(){
        //48 在3000.0200工步上，full Rework 做了三次以上之后，将Lot hold，然后操作partial Rework(with  hold Release) ，触发Rework count
        reworkCase.Reach_Max_Rework_Count_For_Full_Rework_Case_OnHold_Partial_Rework_With_Hold_Release();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Reach_Max_Rework_Count_For_Full_Rework_Case_FutureHold_Partial_Rework_With_Hold_Release(){
        //49 在3000.0200full Rework 做了三次以上之后，在3000.0200设置一个Future hold(with  hold Release)，然后操作partial Rework ，触发Rework count ，
        reworkCase.Reach_Max_Rework_Count_For_Full_Rework_Case_FutureHold_Partial_Rework_With_Hold_Release();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Reach_Max_Rework_Count_For_Full_Rework_Case_OnHold_Partial_Rework_With_Out_Hold_Release(){
        //50 在3000.0200工步上，full Rework 做了三次以上之后，将Lot hold，然后操作partial Rework (点击without hold Release按钮)，触发Rework count ，点击confirm按钮无法进入Rework Flow
        reworkCase.Reach_Max_Rework_Count_For_Full_Rework_Case_OnHold_Partial_Rework_With_Out_Hold_Release();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Reach_Max_Rework_Count_For_Full_Rework_Case_FutureHold_Partial_Rework_With_Out_Hold_Release(){
        //51 在3000.0200full Rework 做了三次以上之后，在3000.0200设置一个Future hold，然后操作partial Rework（without hold Release） ，触发Rework count ，点击confirm按钮无法进入Rework Flow
        reworkCase.Reach_Max_Rework_Count_For_Full_Rework_Case_FutureHold_Partial_Rework_With_Out_Hold_Release();
    }




    /************************************** Dynamic All Rework  ********************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Dynamic_All_Rework(){
        //ENG-5-1-1 Dynamic all rework normal case
        //ENG-5-1-2 Dynamic all rework at first step of the route
        //ENG-5-1-3 do Dynamic rework again after already in Dynamic rework flow
        //ENG-5-1-4 return the lot not at return point or other steps after return point
        reworkCase.Dynamic_All_Rework();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void LotHold_Then_Enters_Dynamic_Full_Rework(){
        //ENG-5-1-5 LotHold then enters Dynamic Full Rework
        reworkCase.LotHold_Then_Enters_Dynamic_Full_Rework();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Trigger_The_Future_Hold_And_Then_Proceed_To_Dynamic_Full_Rework(){
        //ENG-5-1-6 Trigger the Future Hold, and then proceed to Dynamic Full Rework(pre single)
        reworkCase.Trigger_The_Future_Hold_And_Then_Proceed_To_Dynamic_Full_Rework();
    }

    /************************************** Dynamic Partial Rework  ********************************************/
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Dynamic_Partial_Rework_Return_Point_Is_After_Current_Step(){
        //ENG-5-1-7 Dynamic partial rework (return point is after current step)
        reworkCase.Dynamic_Partial_Rework_Return_Point_Is_After_Current_Step();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Dynamic_Partial_Rework_Return_Point_Is_Before_Current_Step(){
        //ENG-5-1-8 Dynamic partial rework (return point is before current step)
        reworkCase.Dynamic_Partial_Rework_Return_Point_Is_Before_Current_Step();
    }

    //ENG-5-1-9 Dynamic partial rework at first step of the route (tested at rework function : 19 partial rework at first step of the route)

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void  Do_Dynamic_Partial_Rework_Again_After_Already_Did_Dynamic_Partial_Rework_Parent_Lot(){
        //ENG-5-1-10 do Dynamic partial rework again after already did Dynamic partial rework (parent lot)
        reworkCase.Do_Dynamic_Partial_Rework_Again_After_Already_Did_Dynamic_Partial_Rework_Parent_Lot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Dynamic_Partial_Rework_And_SKip_Parent_And_Child_Lot_To_Step_After_Return_Point(){
        //ENG-5-1-11 Dynamic partial rework and skip parent/child lot to step after return point
        reworkCase.Dynamic_Partial_Rework_And_SKip_Parent_And_Child_Lot_To_Step_After_Return_Point();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void Dynamic_Partial_Rework_And_SKip_Child_Lot_To_Step_Before_Return_Point(){
        //ENG-5-1-12 do dynamic partial rework with the return point is before the current step
        reworkCase.Dynamic_Partial_Rework_And_SKip_Child_Lot_To_Step_Before_Return_Point();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void LotHold_Then_Performs_The_Dynamic_Partial_Rework_Operation_With_Hold_Release(){
        //ENG-5-1-13 Lothold then performs the Dynaminc partial Rework operation （With Hold Release）
        //ENG-5-1-14 Lothold then performs the Dynaminc partial Rework operation and select children Lot to partial Rework the Cancel（With Hold Release）
        reworkCase.LotHold_Then_Performs_The_Dynamic_Partial_Rework_Operation_With_Hold_Release();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void LotHold_Then_Performs_The_Dynamic_Partial_Rework_Operation_WithOut_Hold_Release(){
        //ENG-5-1-15 Lothold then performs the Dynaminc partial Rework operation （WithOut Hold Re lease）
        //ENG-5-1-16 Lothold then performs the Dynaminc partial Rework operation and select children Lot to partial Rework the Cancel （WithOut Hold Release）
        reworkCase.LotHold_Then_Performs_The_Dynamic_Partial_Rework_Operation_WithOut_Hold_Release();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void After_Triggering_The_FutureHold_And_Performs_Dynamic_Partial_Rework_Operation_With_Hold_Release(){
        //ENG-5-1-17 After triggering the Future hold, and performs Dynamic partial Rework operation（With Hold Release）
        //ENG-5-1-18 After triggering the Future hold, and performs Dynamic partial Rework operation（With Hold Release）partial Rework Cancel
        reworkCase.After_Triggering_The_FutureHold_And_Performs_Dynamic_Partial_Rework_Operation_With_Hold_Release();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void After_Triggering_The_FutureHold_And_Performs_Dynamic_Partial_Rework_Operation_WithOut_Hold_Release_Partial_Rework_Cancel(){
        //ENG-5-1-19 After triggering the Future hold, and performs Dynamic partial Rework operation（WithOut Hold Release）partial Rework Cancel
        reworkCase.After_Triggering_The_FutureHold_And_Performs_Dynamic_Partial_Rework_Operation_WithOut_Hold_Release_Partial_Rework_Cancel();
    }

}
