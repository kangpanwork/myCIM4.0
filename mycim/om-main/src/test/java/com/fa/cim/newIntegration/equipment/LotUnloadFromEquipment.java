package com.fa.cim.newIntegration.equipment;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.equipment.scase.LotUnloadFromEquipmentCase;
import com.fa.cim.newIntegration.equipment.scase.MoveOutCase;
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
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/14       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/14 11:37
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class LotUnloadFromEquipment {
    @Autowired
    private LotUnloadFromEquipmentCase lotUnloadFromEquipmentCase;

    /**
     * description:EQP8-1-2 Manual Unload a carrier which is just loaded and has not be moved in
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/5 13:13
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void manualUnload_Normal(){
        lotUnloadFromEquipmentCase.manualUnload_Normal();
    }

    /**
     * description: EQP8-1-1 Manual Unload a carrier which just did "move out" operation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/5 13:14
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void manualUnloadAferMoveOut(){
        lotUnloadFromEquipmentCase.manualUnloadAferMoveOut();
    }

    /**
     * description: EQP8-1-3 Manual Unload a carrier which just did "move in cancel" operation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/5 13:18
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void manualUnloadAferMoveInCancel(){
        lotUnloadFromEquipmentCase.manualUnloadAferMoveInCancel();
    }

    /**
     * description: EQP8-1-4 Manual Unload a carrier which is still in processing
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/5 13:37
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void manualUnloadWhenCarrierInProcessing(){
        lotUnloadFromEquipmentCase.manualUnloadWhenCarrierInProcessing();
    }

    /**
     * description: EQP8-1-5 UnLoad a carrier with Auto-1Operation Mode
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/5 14:08
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void unLoadCarrierWithAuto1OperationMode(){
        lotUnloadFromEquipmentCase.unLoadCarrierWithAuto1OperationMode();
    }

}