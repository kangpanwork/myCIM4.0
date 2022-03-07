package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.OperationSkipCase;
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
 * 2019/9/10       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/10 13:43
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class OperationSkip {

    @Autowired
    private OperationSkipCase operationSkipCase;

    /**
     * description: normal case
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 11:03
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void operationSkip_Forward_Backward_Normal(){
        operationSkipCase.operationSkip_Forward_Backward_Normal();
    }

    /**
     * description: WIP-3-1-1 Skip(Forward) with a future hold registration in  current operation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 11:03
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void operationSkip_Forward_With_FutureHoldRegistration_In_CurrentOperation(){
        operationSkipCase.operationSkip_Forward_With_FutureHoldRegistration_In_CurrentOperation();
    }

    /**
     * description: WIP-3-1-2 Skip(Forward) with a future hold registration（pre） in  destination operation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 11:05
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void operationSkip_Forward_With_FutureHoldRegistrationPre_In_DestinationOperation(){
        operationSkipCase.operationSkip_Forward_With_FutureHoldRegistrationPre_In_DestinationOperation();
    }

    /**
     * description: WIP-3-1-3 Skip(Backward) with a future hold registration（pre） in  current operation which lot 's status is onhold
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 11:06
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void operationSkip_Backward_With_FutureHoldRegistrationPre_In_CurrentOperation_LotOnhold(){
        operationSkipCase.operationSkip_Backward_With_FutureHoldRegistrationPre_In_CurrentOperation_LotOnhold();
    }

    /**
     * description: WIP-3-1-4 Skip(Backward) with a future hold registration（post） in  current operation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 11:06
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void operationSkip_Backward_With_FutureHoldRegistrationPost_In_CurrentOperation(){
        operationSkipCase.operationSkip_Backward_With_FutureHoldRegistrationPost_In_CurrentOperation();
    }

    /**
     * description: WIP-3-1-5 Skip(Backward) with a future hold registration（pre） in  current operation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 11:07
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void operationSkip_Backward_With_FutureHoldRegistrationPre_In_CurrentOperation(){
        operationSkipCase.operationSkip_Backward_With_FutureHoldRegistrationPre_In_CurrentOperation();
    }

    /**
     * description: WIP-3-1-6 Skip(Backward) with a future hold registration（pre） in  current operation，then Skip(Forward)  to see if the   the future hold request   will become effective again
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 11:07
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void operationSkip_Backward_With_FutureHoldRegistrationPre_In_CurrentOperation_Forward_FutureHold(){
        operationSkipCase.operationSkip_Backward_With_FutureHoldRegistrationPre_In_CurrentOperation_Forward_FutureHold();
    }

    /**
     * description:WIP-3-1-8 Skip with carrier's transfer status is EquipmentIn
     *            WIP-3-1-7 Skip can not executed  in equipment when OM_CARRIER_CHK_EI_FOR_LOT_OPERATION is 1
     *            TRK5-1-15 Skip lot when lot xfer status is EI
     *            TRK5-1-16 Skip lot when lot xfer status is EI(when OM_CARRIER_CHK_EI_FOR_LOT_OPERATION is 0)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 13:02
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void operationSkip_With_CarrierTransferStatus(){
        operationSkipCase.operationSkip_With_CarrierTransferStatus();
    }

    /**
     * description: WIP-3-1-9 Force Skip when lot is held by Process Hold instruction
     *              TRK5-1-13  A lot is held by Process Hold on current operation.
     *              Move lot from current operation to a forward destination operation.
     *              You can force a skip operation and when lot is moved away from the operation for which the Process Hold is registered,
     *              the Lot Hold is not released by Process Hold Registration Cancel.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 11:08
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void forceOperationSkip_When_LotHeldByProcessHoldInstruction(){
        operationSkipCase.forceOperationSkip_When_LotHeldByProcessHoldInstruction();
    }

    /**
     * description: WIP-3-1-10 Skip a lot which InventoryState is InBank
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 11:09
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void operationSkip_LotInBank(){
        operationSkipCase.operationSkip_LotInBank();
    }

    /**
     * description: TRK5-1-11 Move lot from current operation in rework flow back to an operation in the Main Process Flow. The lot production state will be changed from In-Rework to In-Production
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 14:04
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveLotFromCurrentOperationInReworkFlowBackToMainProcessFlow(){
        operationSkipCase.moveLotFromCurrentOperationInReworkFlowBackToMainProcessFlow();
    }

    /**
     * description: TRK5-1-14 Skip lot when lot Inventory State is InBank
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 15:09
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void skipLotWhenLotInventoryStateIsInBank(){
        operationSkipCase.skipLotWhenLotInventoryStateIsInBank();
    }

    /**
     * description: TRK5-1-17 When skip,With a forward destination operations is the same route as the current operation or on the original route
     *              TRK5-1-18 Skip with a backward destination, only operations on the same route as the current operation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 16:26
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void skipWithDestinationoperationsIsTheSameRoute(){
        operationSkipCase.skipWithDestinationoperationsIsTheSameRoute();
    }

    /**
     * description: TRK5-1-19  If the destination is a forward operation, no future hold requests should exist during the current operation and previous operation of the destination
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/18 17:58
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void skipWithFutureHoldBetweenTargetOperationAndCurrentOperation(){
        operationSkipCase.skipWithFutureHoldBetweenTargetOperationAndCurrentOperation();
    }


    /**
     * description: TRK5-1-21 Lot skip when lot had been reserved to current equipment
     *              TRK5-1-22 Lot skip when lot had been reserved and load to current equipment
     *              TRK5-1-23 Lot skip when lot had been load without reserve to current equipment
     *              TRK5-1-24 Lot skip when lot had been move in to current equipment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/19 9:38
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void lotSkipWhenLotReservedToCurrentEquipment(){
        operationSkipCase.lotSkipWhenLotReservedToCurrentEquipment();
    }

    /**
     * description: TRK5-1-25 Skip to the entry point of flow batch
     *          TRK5-1-26 Skip to the flow batch section
     *          TRK5-1-27 Skip over the target point of flow batch
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/19 10:42
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void skipFlowBatch(){
        operationSkipCase.skipFlowBatch();
    }


}