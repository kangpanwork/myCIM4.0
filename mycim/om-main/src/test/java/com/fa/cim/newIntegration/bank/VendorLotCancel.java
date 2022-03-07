package com.fa.cim.newIntegration.bank;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.bank.scase.VendorLotCancelCase;
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
 * <p>VendorLotCancel .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2019/9/9/009   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2019/9/9/009 16:03
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class VendorLotCancel {
    @Autowired
    private VendorLotCancelCase vendorLotCancelCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_Prepare_Lot(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_WithoutPrepare_Lot(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_ScrappedWafer_Lot(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_HasRouteInformation_Lot(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_LotsWafer_StrartedBuild_ServalTimes(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_AfterCancle_CheckFrlotTableData_ForCancledLot(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_AfterCancle_CheckFrlotTableData_ForNewlyGenerateLot(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_ForLot_MadeWith_MultipleVendorLots(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_ForOnHoldLot(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_ForPMLot_AssignedNoCarrier(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_ForPMLot_InBank_AssignedToCarrier(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_VendorLot_AssignedToCarrier(){
        vendorLotCancelCase.VendorLotCancel();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotCancel_VendorLot_NotAssignedToCarrier(){
        vendorLotCancelCase.VendorLotCancel();
    }
}
