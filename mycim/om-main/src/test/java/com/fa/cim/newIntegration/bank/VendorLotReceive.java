package com.fa.cim.newIntegration.bank;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.bank.scase.VendorLotReceiveCase;
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
 * @date: 2019/8/26 16:39
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class VendorLotReceive {

    @Autowired
    private VendorLotReceiveCase vendorLotReceiveCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReceive_NotAssignLotID() {
        vendorLotReceiveCase.VendorLotReceive_NotAssignLotID();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReceive_AssignLotID() {
        vendorLotReceiveCase.VendorLotReceive_AssignLotID();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReceive_Contain25PcsWafer() {
        vendorLotReceiveCase.VendorLotReceive_AssignLotID();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReceive_Contain100PcsWafer() {
        vendorLotReceiveCase.VendorLotReceive_AssignLotID();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReceive_Contain0PcsWafer() {
        vendorLotReceiveCase.VendorLotReceive_AssignLotID();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void VendorLotReceive_Contain100000000000PcsWafer() {
        vendorLotReceiveCase.VendorLotReceive_AssignLotID();
    }


}
