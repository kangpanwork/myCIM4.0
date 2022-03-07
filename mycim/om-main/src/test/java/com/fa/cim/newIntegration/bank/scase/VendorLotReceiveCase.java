package com.fa.cim.newIntegration.bank.scase;

import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.tcase.BankTestCase;
import com.fa.cim.utils.GenerateVendorlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
@Slf4j
public class VendorLotReceiveCase {
    @Autowired
    private BankTestCase bankTestCase;

    /**     
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * @author Bear
      * @date 2019/8/28 14:59
      * @param bankID
      * @param lotID
      * @param productID
      * @param count -
      * @return com.fa.cim.common.support.Response
      */
    public Response VendorLotReceive(String bankID, String lotID, String productID, int count) {
        String subLotType = "Vendor";
        return bankTestCase.vendorLotReceiveReqCase(bankID, subLotType, lotID, productID, count);
    }
    public Response VendorLotReceive(String bankID, String subLotType, String lotID, String productID, int count) {
        TestInfos.VendorLotReceiveInfo vendorLotReceiveInfo = new TestInfos.VendorLotReceiveInfo(bankID, subLotType, lotID, productID, count);
        return this.VendorLotReceive(vendorLotReceiveInfo);
    }
    public Response VendorLotReceive(TestInfos.VendorLotReceiveInfo vendorLotReceiveInfo) {
        String bankID = vendorLotReceiveInfo.getBankID();
        int count = vendorLotReceiveInfo.getCount();
        String lotID = vendorLotReceiveInfo.getLotID();
        String productID = vendorLotReceiveInfo.getProductID();
        String subLotType = vendorLotReceiveInfo.getSubLotType();

        //【step1】select Bank
        final String inqBank = "B";
        Response response = bankTestCase.bankListInqCase(bankID, inqBank);
        Validations.isSuccessWithException(response);
        //Validations.isSuccessWithException(, "VendorLotReceive_NotAssignSubLotType() run fail!");

        //【step2】get lots which in bank
        response = bankTestCase.lotInfoByLotListInqCase(bankID);
        Validations.isSuccessWithException(response);

        //【step3】get productID
        response = bankTestCase.productIDCase();
        Validations.isSuccessWithException(response);

        //【step4】vendor lot receive confirm
        return bankTestCase.vendorLotReceiveReqCase(bankID, subLotType, lotID, productID, count);
    }

    public Response VendorLotReceive_AssignLotID() {
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        final String bankID = "BNK-0S";
        final String subLotType = "Vendor";
        final String productID = "RAW-2000.01";
        final String lotID = vendorLotID;
        return this.VendorLotReceive(bankID, subLotType, lotID, productID, 100);
    }


    public Response VendorLotReceive_NotAssignLotID() {
        final String bankID = "BNK-0S";
        final String subLotType = "Vendor";
        final String productID = "RAW-2000.01";
        final String lotID = null;
        return this.VendorLotReceive(bankID, subLotType, lotID, productID, 100);
    }


    public Response VendorLotReceive_Contain25PcsWafer() {
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        final String bankID = "BNK-0S";
        final String subLotType = "Vendor";
        final String productID = "RAW-2000.01";
        final String lotID = vendorLotID;
        return this.VendorLotReceive(bankID, subLotType, lotID, productID, 25);
    }

    public Response VendorLotReceive_Contain100PcsWafer() {
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        final String bankID = "BNK-0S";
        final String subLotType = "Vendor";
        final String productID = "RAW-2000.01";
        final String lotID = vendorLotID;
        return this.VendorLotReceive(bankID, subLotType, lotID, productID, 100);
    }

    public Response VendorLotReceive_Contain0PcsWafer() {
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        final String bankID = "BNK-0S";
        final String subLotType = "Vendor";
        final String productID = "RAW-2000.01";
        final String lotID = vendorLotID;
        return this.VendorLotReceive(bankID, subLotType, lotID, productID, 0);
    }

    public Response VendorLotReceive_Contain1000000000PcsWafer() {
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        final String bankID = "BNK-0S";
        final String subLotType = "Vendor";
        final String productID = "RAW-2000.01";
        final String lotID = vendorLotID;
        return this.VendorLotReceive(bankID, subLotType, lotID, productID, 1000000000);
    }

}
