package com.fa.cim.newIntegration.bank;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.bank.scase.VendorLotReturnCase;
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
 * <p>VendorLotReturn .<br/></p>
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
public class VendorLotReturn {
    @Autowired
    private VendorLotReturnCase vendorLotReturnCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReturn(){
        vendorLotReturnCase.VendorLotReturn();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReturn_PreparedLot(){
        vendorLotReturnCase.VendorLotReturn();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReturn_0ReturnCount(){
        vendorLotReturnCase.VendorLotReturn();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReturn_OnHoldLot(){
        vendorLotReturnCase.VendorLotReturn();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReturn_NonVendorType_Lot(){
        vendorLotReturnCase.VendorLotReturn();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReturn_InventoryStateValue_NotInBank_Lot(){
        vendorLotReturnCase.VendorLotReturn();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReturn_StateValue_NotFinished_Lot(){
        vendorLotReturnCase.VendorLotReturn();
    }
}
