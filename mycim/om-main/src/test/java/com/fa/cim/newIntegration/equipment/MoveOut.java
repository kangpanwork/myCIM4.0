package com.fa.cim.newIntegration.equipment;

import com.fa.cim.MycimApplication;
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
 * 2019/9/15       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/15 8:51
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class MoveOut {

    @Autowired
    private MoveOutCase moveOutCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveOut_Normal(){
        moveOutCase.moveOut_Normal();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveOut_WithSingleDataCollection(){
        moveOutCase.moveOut_withSingleDataCollection();
    }

    /**
     *
     * description: TRK4-2 Clear any Future Hold already triggered after Operation Completion
     *              TRK4-3 The lot will Held here, if Future Hold of “PRE” type was registered for the process of Operation Complete
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/10 16:09
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void clearAnyFutureHoldAlreadyTriggeredAfterOperationCompletion(){
        moveOutCase.clearAnyFutureHoldAlreadyTriggeredAfterOperationCompletion();
    }

    /**
     *
     * description: TRK4-4 Future Hold of “POST” type was registered for the process  of Operation Complete
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/10 16:09
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void FutureHoldOfPOSTTypeWasRegisteredForTheProcessOfOperationComplete(){
        moveOutCase.FutureHoldOfPOSTTypeWasRegisteredForTheProcessOfOperationComplete();
    }

    /**
     *
     * description: TRK4-5 Clear the Lot Information from In-Process Lot Information after Operation Complete
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/10 16:09
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void clearTheLotInformationFromInProcessLotInformationAfterOperationComplete(){
        moveOutCase.clearTheLotInformationFromInProcessLotInformationAfterOperationComplete();
    }

    /**
     *
     * description: TRK4-6 "If the equipment is OffLine, the equipment status is changed from In Process (PRD) to Waiting (SBY)
     * automatically within Operation Comp Logic "
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/10 16:09
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void ifTheequipmentIsOffLineTheEquipmentStatusIsChangedFromInProcessPRDToWaitingSBYAutomaticallyWithinOperationCompLogic(){
        moveOutCase.ifTheequipmentIsOffLineTheEquipmentStatusIsChangedFromInProcessPRDToWaitingSBYAutomaticallyWithinOperationCompLogic();
    }


}