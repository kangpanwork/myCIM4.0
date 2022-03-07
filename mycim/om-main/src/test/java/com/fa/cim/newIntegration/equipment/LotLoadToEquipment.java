package com.fa.cim.newIntegration.equipment;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.equipment.scase.LotLoadToEquipmentCase;
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
 * 2019/9/12       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/12 14:57
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class LotLoadToEquipment {

    @Autowired
    private LotLoadToEquipmentCase lotLoadToEquipmentCase;

    /**
     * description: EQP6-1-1  Manual Loading after StartLotsReservation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/6 15:25
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void manual_Loading_After_StartLotsReservation(){
        lotLoadToEquipmentCase.manual_Loading_After_StartLotsReservation();
    }

    /**
     * description:EQP6-1-2   Manual Loading without StartLotsReservation in advance
     *         EQP6-1-13 Equipment's status should be available before Move In
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/6 15:26
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void manual_Loading_Without_StartLotsReservation(){
        lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation();
    }

    /**
     * description: EQP6-1-3 Manual Loading a carrier which is already reserved in another Equipment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/6 15:26
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void manualLoadingCarrierAlreadyReservedInAnotherEquipment(){
        lotLoadToEquipmentCase.manualLoadingCarrierAlreadyReservedInAnotherEquipment();
    }

    /**
     * description:EQP6-1-4  Manual Loading a carrier whose transfer status is "EI"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/6 15:32
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void manualLoadingCarrierThatTransferStatusIsEI(){
        lotLoadToEquipmentCase.manualLoadingCarrierThatTransferStatusIsEI();
    }

    /**
     * description: EQP6-1-5 Manual Loading a carrier which is not supposed to be processed in current selected equipment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/6 16:41
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void manualLoadingCarrierNotSupposedToBeProcessedInCurrentSelectedEquipment(){
        lotLoadToEquipmentCase.manualLoadingCarrierNotSupposedToBeProcessedInCurrentSelectedEquipment();
    }

    /**
     * description: EQP6-1-6 Load a reserved process lot carrier to a port whose purpose type is "Process Monitor Lot"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/6 16:47
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadReservedProcessLotCarrierToPortThatPurposeTypeIsProcessMonitorLot(){
        lotLoadToEquipmentCase.loadReservedProcessLotCarrierToPortThatPurposeTypeIsProcessMonitorLot();
    }

    /**
     * description:EQP6-1-7 Load a carrier with Carrier Type "FOUP" to a port whose compatible carrier cagetory does not include "FOUP"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/9 9:58
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadCarrierWithCategoryFOUPToPortThatCagetoryDoesNotIncludeFOUP(){
        lotLoadToEquipmentCase.loadCarrierWithCategoryFOUPToPortThatCagetoryDoesNotIncludeFOUP();
    }

    /**
     * description: EQP6-1-8 Load a carrier with Auto-1Operation Mode
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/9 11:20
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadCarrierWithAuto1OperationMode(){
        lotLoadToEquipmentCase.loadCarrierWithAuto1OperationMode();
    }

    /**
     * description: EQP6-1-9  Load a carrier with Auto-2Operation Mode
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/9 11:27
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadCarrierWithAuto2OperationMode(){
        lotLoadToEquipmentCase.loadCarrierWithAuto2OperationMode();
    }

    /**
     * description: EQP6-1-11 When Loading Carrier contained Process Lot to Equipment with MultiRecipeCapability: “Batch”
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/9 13:47
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadingCarrierContainedProcessLotToEquipmentWithMultiRecipeCapabilityBatch(){
        lotLoadToEquipmentCase.loadingCarrierContainedProcessLotToEquipmentWithMultiRecipeCapabilityBatch();
    }

    /**
     * description: EQP6-1-12 When Loading Carrier contained Process Lot to Equipment with MultiRecipeCapability: “Multi Recipe”
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/9 13:47
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadingCarrierContainedProcessLotToEquipmentWithMultiRecipeCapabilityMultiRecipe(){
        lotLoadToEquipmentCase.loadingCarrierContainedProcessLotToEquipmentWithMultiRecipeCapabilityMultiRecipe();
    }

    /**
     * description: EQP6-1-14 Current equipment status is “Conditional Available”， and sub lot type is Engineering
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/9 14:47
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void currentEquipmentStatusIsConditionalAvailableAndSubLotTypeIsEngineering(){
        lotLoadToEquipmentCase.currentEquipmentStatusIsConditionalAvailableAndSubLotTypeIsEngineering();
    }

    /**
     * description: EQP6-1-18 Load carrier which Carrier’s Transfer State is ManualIn
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/10 8:49
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadCarrierWhichTransferStateIsManualIn(){
        lotLoadToEquipmentCase.loadCarrierWhichTransferStateIsManualIn();
    }

    /**
     * description: EQP8-1-22 Load carrier which Carrier’s status is NotAvailable
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/10 10:02
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadCarrierWhichStatusIsNotAvailable(){
        lotLoadToEquipmentCase.loadCarrierWhichStatusIsNotAvailable();
    }

    /**
     * description: EQP8-1-23 Load carrier which Carrier’s status is Inuse
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/10 10:15
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadCarrierWhichStatusIsInuse(){
        lotLoadToEquipmentCase.loadCarrierWhichStatusIsInuse();
    }

    /**
     * description: EQP8-1-24 Load a carrier which Lot Hold State is onhold
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/10 10:40
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadCarrierWhichLotHoldStateIsOnhold(){
        lotLoadToEquipmentCase.loadCarrierWhichLotHoldStateIsOnHold();
    }

    /**
     * description: EQP8-1-25 Restriction EQP when move in
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/10 10:55
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void restrictionEQPWhenMoveIn(){
        lotLoadToEquipmentCase.restrictionEQPWhenMoveIn();
    }

    /**
     * description: EQP8-1-26 Load a carrier which exist Scrap Wafer in lots
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/10 10:57
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadCarrierWhichExistScrapWaferInLots(){
        lotLoadToEquipmentCase.loadCarrierWhichExistScrapWaferInLots();
    }

    /**
     * description: EQP8-1-27  When Loading Carrier contained Process Lot to Equipment with MultiRecipeCapability: Single-recipe
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/11 13:33
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadingCarrierContainedProcessLotToEquipmentWithMultiRecipeCapabilitySingleRecipe(){
        lotLoadToEquipmentCase.loadingCarrierContainedProcessLotToEquipmentWithMultiRecipeCapabilitySingleRecipe();
    }

    /**
     * description: EQP8-1-28 Load a carrier to equipment's port which need reticle（ reticle is unavailable）
     *             EQP8-1-29 Load a carrier to equipment's port which need reticle（ one of the reticle is unavailable, anther is available）
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/12 10:23
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void loadCarrierToEquipmentWhichNeedReticle(){
        lotLoadToEquipmentCase.loadCarrierToEquipmentWhichNeedReticle();
    }

}