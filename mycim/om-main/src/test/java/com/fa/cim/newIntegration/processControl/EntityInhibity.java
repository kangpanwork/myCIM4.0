package com.fa.cim.newIntegration.processControl;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.processControl.scase.EntityInhibityCase;
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
 * <p>EntityInhibity .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2019/11/26/026   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2019/11/26/026 16:54
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class EntityInhibity {
    @Autowired
    private EntityInhibityCase entityInhibityCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_1_Inhibited_Equipment_Chamber_EnvironmentVariableIs0_ToLoad(){
        entityInhibityCase.Inhibited_Equipment_Chamber_EnvironmentVariableIs0_ToLoad();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_2_Inhibited_Equipment_Chamber_EnvironmentVariableIs1_ToLoad(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_3_Inhibited_Equipment_Chamber_EnvironmentVariableIs0_ToMoveIn(){
        entityInhibityCase.Inhibited_Equipment_Chamber_EnvironmentVariableIs0_MoveIn();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_4_Inhibited_Equipment_Chamber_EnvironmentVariableIs1_ToMoveIn(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_5_Inhibited_Equipment_MachineRecipe_EnvironmentVariableIs0_ToMoveIn(){
        entityInhibityCase.Inhibited_Equipment_MachineRecipe_EnvironmentVariableIs0_ToMoveIn();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_6_Inhibited_Equipment_MachineRecipe_EnvironmentVariableIs1_ToMoveIn(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_7_Inhibited_Equipment_Chamber_MachineRecipe_EnvironmentVariableIs0_ToMoveIn(){
        entityInhibityCase.Inhibited_Equipment_Chamber_MachineRecipe_EnvironmentVariableIs0_ToMoveIn();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_8_Inhibited_Equipment_Chamber_MachineRecipe_EnvironmentVariableIs1_ToMoveIn(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_9_Inhibited_Product_EnvironmentVariableIs1_CheckRestrictionFlag(){
        entityInhibityCase.Inhibited_Product_EnvironmentVariableIs1_CheckRestrictionFlag();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_10_Inhibited_Product_EnvironmentVariableIs1_CheckRestrictionFlag(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_11_Inhibited_ProcessFlow_EnvironmentVariableIs1_CheckRestrictionFlag(){
        entityInhibityCase.Inhibited_ProcessFlow_EnvironmentVariableIs1_CheckRestrictionFlag();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_12_Inhibited_ProcessFlow_EnvironmentVariableIs1_CheckRestrictionFlag(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_13_Inhibited_StartOperation_EnvironmentVariableIs1_CheckRestrictionFlag(){
        entityInhibityCase.Inhibited_StartOperation_EnvironmentVariableIs1_CheckRestrictionFlag();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_14_Inhibited_StartOperation_EnvironmentVariableIs1_CheckRestrictionFlag(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_15_Inhibited_StartOperation_Product_EnvironmentVariableIs1_CheckRestrictionFlag(){
        entityInhibityCase.Inhibited_StartOperation_Product_EnvironmentVariableIs1_CheckRestrictionFlag();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_16_Inhibited_StartOperation_EnvironmentVariableIs1_Product_CheckRestrictionFlag(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_17_Inhibited_StartOperation_Product_Equipment_EnvironmentVariableIs1_CheckRestrictionFlag(){
        entityInhibityCase.Inhibited_StartOperation_Product_Equipment_EnvironmentVariableIs1_CheckRestrictionFlag();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_18_Inhibited_StartOperation_EnvironmentVariableIs1_Product_Equipment_CheckRestrictionFlag(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_19_Inhibited_Recipe_EnvironmentVariableIs1_CheckRestrictionFlag(){
        entityInhibityCase.Inhibited_Recipe_EnvironmentVariableIs1_CheckRestrictionFlag();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_20_Inhibited_Recipe_EnvironmentVariableIs1_CheckRestrictionFlag(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_21_Inhibited_Reticle_EnvironmentVariableIs1_CheckRestrictionFlag(){
        entityInhibityCase.Inhibited_Reticle_EnvironmentVariableIs1_CheckRestrictionFlag();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_22_Inhibited_Reticle_EnvironmentVariableIs1_CheckRestrictionFlag(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_23_Inhibited_ReticleGroup_EnvironmentVariableIs1_CheckRestrictionFlag(){
        entityInhibityCase.Inhibited_ReticleGroup_EnvironmentVariableIs1_CheckRestrictionFlag();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_24_Inhibited_ReticleGroup_EnvironmentVariableIs1_CheckRestrictionFlag(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_25_Inhibited_ProcessDefinition_Equipment_EnvironmentVariableIs1_CheckRestrictionFlag(){
        entityInhibityCase.Inhibited_ProcessDefinition_Equipment_EnvironmentVariableIs1_CheckRestrictionFlag();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_26_Inhibited_ProcessDefinition_Equipment_EnvironmentVariableIs1_CheckRestrictionFlag(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_27_Inhibited_Module_Process_Definition_Equipment_EnvironmentVariableIs1_CheckRestrictionFlag(){
        entityInhibityCase.Inhibited_Module_Process_Definition_Equipment_EnvironmentVariableIs1_CheckRestrictionFlag();
    }

    //NOT USED
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_28_Inhibited_Module_Process_Definition_Equipment_EnvironmentVariableIs1_CheckRestrictionFlag(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_29_Inhibited_Product_EnvironmentVariableIs1_Which_LotIn_FlowBatch(){
        entityInhibityCase.PRC_5_1_29_Inhibited_Product_EnvironmentVariableIs1_Which_LotIn_FlowBatch();
    }

    
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void PRC_5_1_30_Inhibited_Product_EnvironmentVariableIs1_Which_LotIn_FlowBatch(){
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void MFG_1000_Regist(){
        entityInhibityCase.MFG_1000_Regist();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void MFG_1000_Cancle(){
        entityInhibityCase.MFG_1000_Cancle();
    }
}
