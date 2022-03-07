package com.fa.cim.newIntegration.bank;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.bank.scase.VendorLotPrepareCase;
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
 * 2019/8/26        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/8/26 18:12
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class VendorLotPrepare {
    @Autowired
    private VendorLotPrepareCase vendorLotPrepareCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotPrepare_NormalLot() {
        vendorLotPrepareCase.VendorLotPrepare_EmptyCassette_SingleVendorLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotPrepare_EmptyLot() {
        vendorLotPrepareCase.VendorLotPrepare_EmptyCassette_SingleVendorLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotPrepare_OnHoldLot() {
        vendorLotPrepareCase.VendorLotPrepare_EmptyCassette_SingleVendorLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotPrepare_NonVendorType_Lot() {
        vendorLotPrepareCase.VendorLotPrepare_EmptyCassette_SingleVendorLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotPrepare_BiggestCountWafer() {
        vendorLotPrepareCase.VendorLotPrepare_EmptyCassette_SingleVendorLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotPrepare_ShippedBiggestCountWafer_Lot() {
        vendorLotPrepareCase.VendorLotPrepare_EmptyCassette_SingleVendorLot();
    }



    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotPrepare_EmptyCassette_SingleVendorLot() {
        vendorLotPrepareCase.VendorLotPrepare_EmptyCassette_SingleVendorLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotPrepare_EmptyCassette_MultipleVendorLot() {
        vendorLotPrepareCase.VendorLotPrepare_EmptyCassette_MultipleVendorLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotPrepare_NotEmptyCassette() {
        vendorLotPrepareCase.VendorLotPrepare_NotEmptyCassette();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotPrepare_AssignWaferID() {
        vendorLotPrepareCase.VendorLotPrepare_AssignWaferID();
    }

}
