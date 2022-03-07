package com.fa.cim.newIntegration.doc;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.doc.scase.FPCUpdateCase;
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
 * 2019/11/28          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/11/28 17:22
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class FPCUpdate {

    @Autowired
    private FPCUpdateCase fpcUpdateCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_equipmentAndRecipe() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_1();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByWafer_equipmentAndRecipe() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_2();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_getEquipmentByOperationAndGetRecipeByOperation() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_3();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_getEquipmentByOperationAndGetRecipeByEquipment() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_4();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_equipmentAndRecipeWithSearchCondition() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_5();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_withEquipmentRestrictionFlagIsON() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_6();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_throughGetRParamByRecipe() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_7();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_throughGetRParamByEquipment() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_8();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_throughChangeDCSpec() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_12();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_InReticle() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_13();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_skipOperationWhichIsNotMandatoryOperation() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_14();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_skipOperationWhichIsMandatoryOperation() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_15();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_skipToNextProcess() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_16();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_sendEmail() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_17();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_onHold() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_18();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcRegistrationByLot_withMultipleCorrespondingOperation() {
        fpcUpdateCase.fpcRegistration_ENG_4_1_20();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcUpdate_changeBeforeDOCExecute() {
        fpcUpdateCase.fpcUpdate_ENG_4_1_21();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcUpdate_changeAfterDOCExecute() {
        fpcUpdateCase.fpcUpdate_ENG_4_1_22();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcDelete_settingBeforeDOCExecute() {
        fpcUpdateCase.fpcDelete_ENG_4_1_24();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcDelete_settingAfterDOCExecute() {
        fpcUpdateCase.fpcDelete_ENG_4_1_25();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fpcExecute_withoutStartReserve() {
        fpcUpdateCase.fpcExecute_ENG_4_1_26();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void docExecuteByLot_equipmentAndRecipe() {
        fpcUpdateCase.fpcExecute_ENG_4_1_27();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void docExecuteByWafer_equipmentAndRecipe() {
        fpcUpdateCase.fpcExecute_ENG_4_1_28();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void docExecuteByLot_getEquipmentByOperationAndGetRecipeByOperation() {
        fpcUpdateCase.fpcExecute_ENG_4_1_29();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void docExecuteByLot_getEquipmentByOperationAndGetRecipeByEquipment() {
        fpcUpdateCase.fpcExecute_ENG_4_1_30();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void docExecuteByLot_equipmentAndRecipeWithSearchCondition() {
        fpcUpdateCase.fpcExecute_ENG_4_1_31();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void docExecuteByLot_withEquipmentRestrictionFlagIsON() {
        fpcUpdateCase.fpcExecute_ENG_4_1_32();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void docExecuteByLot_throughGetRParamByRecipe() {
        fpcUpdateCase.fpcExecute_ENG_4_1_34();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void docExecuteByLot_throughGetRParamByEquipment() {
        fpcUpdateCase.fpcExecute_ENG_4_1_35();
    }
}