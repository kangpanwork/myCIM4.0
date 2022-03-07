package com.fa.cim.newIntegration.equipment;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.equipment.scase.MoveInCase;
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
 * @date: 2019/9/14 20:08
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class MoveIn {
    @Autowired
    private MoveInCase moveInCase;

    /**
     * description: WIP-1-1-1   oper start
     *      WIP-1-1-2 Operation Start to Generate a Control Job which  without Start Reservation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/10 12:32
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void  moveIn_GenerateControlJob_Without_StartReservation(){
        moveInCase.moveIn_GenerateControlJob_Without_StartReservation();
    }

    /**
     * description: WIP-1-1-3 Clear the Reserved Control Job Information after Operation Start
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/10 12:40
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void  clearReservedControlJobInformationAfterMoveIn() {
        moveInCase.clearReservedControlJobInformationAfterMoveIn();
    }

    /**
     * description: WIP-1-1-4 Reporting the change event in equipment status after Operation Start
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/10 13:14
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void reportingChangeEventInEquipmentStatusAfterMoveIn(){
        moveInCase.reportingChangeEventInEquipmentStatusAfterMoveIn();
    }

    /**
     * description: WIP-1-1-5 Operation Start with onhold lot
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/10 14:05
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveInWithOnHoldLot(){
        moveInCase.moveInWithOnHoldLot();
    }

    /**
     * description: WIP-1-1-6 Increment the Operation Count of Equipment after Operation Start
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/10 14:50
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void incrementOperationCountOfEquipmentAfterMoveIn(){
        moveInCase.incrementOperationCountOfEquipmentAfterMoveIn();
    }

    /**
     * description: WIP-1-1-7 Increment the Operation Count of Employed Reticle after Operation Start
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/11 11:04
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void incrementOperationCountOfEmployedReticleAfterMoveIn(){
        moveInCase.incrementOperationCountOfEmployedReticleAfterMoveIn();
    }

    /**
     * description:WIP-1-1-8 If MultiRecipeCapability of equipment is “B(Single Recipe)” or “C(Batch),” Logical Recipes of all selected lots and Machine Recipe must be the same.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/11 14:41
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void multiRecipeCapabilityOfEquipmentIsSingleRecipeOrBatch(){
        moveInCase.multiRecipeCapabilityOfEquipmentIsSingleRecipeOrBatch();
    }

    /**
     * description: WIP-1-1-9 Durable Status is unavailable or scrapped  when operation start
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/12 14:28
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void durableStatusIsUnavailableOrScrappedWhenMoveIn(){
        moveInCase.durableStatusIsUnavailableOrScrappedWhenMoveIn();
    }

    /**
     * description: WIP-1-1-10 Assigned Process Durable(Transfer Status must be EquipmentIn) when Move in
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/12 15:12
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void assignedProcessDurableWhenMoveIn(){
        moveInCase.assignedProcessDurableWhenMoveIn();
    }

    /**
     * description: WIP-1-1-11 Assigned Process Durable(If selected reticle is under a Durable Sub-Status with operable lot limitation,
     *                         all started lot should match the defined sub-lot-type list) whenMove in
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/12 15:35
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void assignedProcessDurableSelectedReticleUunderDurableSubStatusWithOperableLotLimitationWhenMoveIn(){

    }

    /**
     * description: WIP-1-1-19 CarrierExchangeRequiredFlag of the equipment is True, the number of empty carriers and the number of carriers of Start Lots (lots to be processed in one Operation Start Claim) must match
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/16 9:27
     * @param -
     * @return
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void carrierExchangeRequiredFlagOfEquipmentIsTrue(){
        moveInCase.carrierExchangeRequiredFlagOfEquipmentIsTrue();
    }

    /**
     * description: WIP-1-1-20 Restriction EQP when move in
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/12 16:41
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void restrictionEQPWhenMoveIn(){
        moveInCase.restrictionEQPWhenMoveIn();
    }

    /**
     * description: WIP-1-1-25 Carrier’s Durable Status is unavailable when move in
     *              WIP-1-1-26 Equipment's status should be available before Move In
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/12 17:10
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void carrierDurableStatusIsUnavailableWhenMoveIn(){
        moveInCase.carrierDurableStatusIsUnavailableWhenMoveIn();
    }

    /**
     * description: WIP-1-1-18 If MonitorCreationFlag of equipment is True, empty carriers must exist in the Process Start Information.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/13 17:27
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void monitorCreationFlagOfEquipmentIsTrueAndEmptyCarriersMustExistInMoveInInformation(){
        moveInCase.monitorCreationFlagOfEquipmentIsTrueAndEmptyCarriersMustExistInMoveInInformation();
    }

    /**
     * description: WIP-1-1-22 If MultiRecipeCapability of equipment is“C(Batch)”multiple lots can be reserved at same port group and move in togather because of the same control job
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/13 15:10
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void multiRecipeCapabilityOfequipmentisBatchMultipleLotsCanBeReservedAtSamePortGroupAndMoveIn(){
        moveInCase.multiRecipeCapabilityOfequipmentisBatchMultipleLotsCanBeReservedAtSamePortGroupAndMoveIn();
    }


    /**
     * description: WIP-1-1-28  Move in a lot which product has been Restricted
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/13 9:45
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveInLotWhichProductHasBeenRestricted(){
        moveInCase.moveInLotWhichProductHasBeenRestricted();
    }

    /**
     * description: WIP-1-1-29 Move in a lot which carrier actually move out from this equipment
     *              WIP-1-1-30 Move in lot which did not unload from last equipment
     *              WIP-1-1-33 Move in which lot did not loaded to port
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/13 12:35
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveInLotWhichCarrierActuallyMoveoOutFromThisEquipment(){
        moveInCase.moveInLotWhichCarrierActuallyMoveoOutFromThisEquipment();
    }

    /**
     * description: WIP-1-1-31 Move in a lot which port is Auto-1 mode and loadComp status
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/13 13:13
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveInCarrierWhichPortIsAuto1ModeAndLoadCompStatus(){
        moveInCase.moveInCarrierWhichPortIsAuto1ModeAndLoadCompStatus();
    }

    /**
     * description: WIP-1-1-32 Move in a lot to port group:PG2 which PG1 has a lot is inprocessing
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/13 13:25
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveInLotToPortGroupPG2AndPG1HasLotIsInprocessing(){
        moveInCase.moveInLotToPortGroupPG2AndPG1HasLotIsInprocessing();
    }
}