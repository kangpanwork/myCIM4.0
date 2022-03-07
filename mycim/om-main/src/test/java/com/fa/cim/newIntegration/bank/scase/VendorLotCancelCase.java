package com.fa.cim.newIntegration.bank.scase;

import com.fa.cim.common.support.Response;
import com.fa.cim.newIntegration.tcase.BankTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p>VendorLotCancelCase .<br/></p>
 * <p>
 * change history:
 * date   defect#    person  comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2019/9/9/009 ********  Decade  create file
 *
 * @author: Decade
 * @date: 2019/9/9/009 15:56
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class VendorLotCancelCase {
    @Autowired
    private BankTestCase bankTestCase;

    public Response VendorLotCancel(){
        return null;
    }
}
