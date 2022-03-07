package com.fa.cim.newIntegration.bank.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.tcase.BankTestCase;
import com.fa.cim.utils.GenerateVendorlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p>VendorLotReturnCase .<br/></p>
 * <p>
 * change history:
 * date   defect#    person  comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2019/9/9/009 ********  Decade  create file
 *
 * @author: Decade
 * @date: 2019/9/9/009 15:55
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class VendorLotReturnCase {
    @Autowired
    private BankTestCase bankTestCase;


    public Response VendorLotReturn(String bankID,String lot_ID,Integer productWaferCount) {
        //【step1】select Bank
        final String inqBank = "B";
        Response response = bankTestCase.bankListInqCase(bankID, inqBank);
        Validations.isSuccessWithException(response);
        //Validations.isSuccessWithException(, "VendorLotReceive_NotAssignSubLotType() run fail!");

        //【step2】get lots which in bank
        response = bankTestCase.lotInfoByLotListInqCase(bankID);
        Validations.isSuccessWithException(response);

        final ObjectIdentifier lotID=new ObjectIdentifier();
        lotID.setValue(lot_ID);

        //【ste3p】vendor lot return confirm
        return bankTestCase.VendorLotReturnCase(productWaferCount,lotID);
    }

    public Response VendorLotReturn(){
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        final String bankID = "BNK-0S";
        final String subLotType = "Vendor";
        final String productID = "RAW-2000.01";
        final String lotID = vendorLotID;
        //vendor lot receive
        Response response = bankTestCase.vendorLotReceiveReqCase(bankID, subLotType, lotID, productID, 25);
        String createdLotID = ((Results.VendorLotReceiveReqResult) response.getBody()).getCreatedLotID();
        return this.VendorLotReturn(bankID,createdLotID,25);
    }

}
