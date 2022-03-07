package com.fa.cim.newIntegration.bank.scase;

import com.fa.cim.common.support.Response;
import com.fa.cim.newIntegration.tcase.BankTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/30       ********              Nyx             create file
 *
 * @author: lightyh
 * @date: 2019/8/30 16:30
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class BankHoldCase {
    @Autowired
    private BankTestCase bankTestCase;

    public Response bankHold(){
        return null;
    }
}