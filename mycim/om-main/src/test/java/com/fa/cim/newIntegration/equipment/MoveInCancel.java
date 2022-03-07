package com.fa.cim.newIntegration.equipment;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.equipment.scase.MoveInCancelCase;
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
 * 2019/9/15       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/15 9:05
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class MoveInCancel {
    @Autowired
    private MoveInCancelCase moveInCancelCase;

    /**
     * description: TRK4-1-1  Delete control job in lot info after oper start cancel
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/16 14:19
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void  moveInCancel_Normal(){
        moveInCancelCase.moveInCancel_Normal();
    }

    /**
     * description: TRK4-1-5 Check EQP E10 status after oper start cancel
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Ho
     * @date 2019/12/16 14:26
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void  checkEQPE10StatusAfterOperStartCancel(){
        moveInCancelCase.checkEQPE10StatusAfterOperStartCancel();
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Ho
     * @date 2019/12/16 14:32
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void  checkProcessStatusAfterOperStartCancel(){
        moveInCancelCase.checkProcessStatusAfterOperStartCancel();
    }

    /**
     * description: TRK4-1-6 Check Usage Count of Equipment after oper start cancel
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/16 14:33
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void  checkUsageCountOfEquipmentAfterOperStartCancel(){
        moveInCancelCase.checkUsageCountOfEquipmentAfterOperStartCancel();
    }

    /**
     * description: TRK4-1-7 Check Usage Count of Employed Reticle after oper start cancel
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Ho
     * @date 2019/12/16 14:34
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void  checkUsageCountOfEmployedReticleAfterOperStartCancel(){
        moveInCancelCase.checkUsageCountOfEmployedReticleAfterOperStartCancel();
    }

    /**
     * description: TRK4-1-9 Check Monitor Group after oper start cancel if it was generated previously
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/17 13:35
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void checkMonitorGroupAfterMoveInCancel(){
        moveInCancelCase.checkMonitorGroupAfterMoveInCancel();
    }

    /**
     * description: TRK4-1-11 Move in cancel a lot which equipment has two different control jobs
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/17 16:32
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveInCancelLotWhichEquipmentHasTwoDifferentControlJobs(){
        moveInCancelCase.moveInCancelLotWhichEquipmentHasTwoDifferentControlJobs();
    }
}