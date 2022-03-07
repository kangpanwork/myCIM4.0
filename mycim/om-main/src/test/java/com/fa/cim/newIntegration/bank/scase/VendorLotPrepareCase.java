package com.fa.cim.newIntegration.bank.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.newIntegration.tcase.BankTestCase;
import com.fa.cim.utils.GenerateVendorlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
@Service
@Slf4j
public class VendorLotPrepareCase {
    @Autowired
    private BankTestCase bankTestCase;

    @Autowired
    private VendorLotReceiveCase vendorLotReceiveCase;

    public Response VendorLotPrepare(String bankID, String productID, ObjectIdentifier cassetteID, Integer waferSize) {
        return this.VendorLotPrepare(bankID, productID, cassetteID, waferSize, false);
    }

    public Response VendorLotPrepare(String bankID, String productID, ObjectIdentifier cassetteID, Integer waferSize, boolean isNeedAssignWaferID){
        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();
        if (productID.equals("PRODUCT0.01")){
            vendorLotID = GenerateVendorlot.getVendorLot(1);
        }
        return this.VendorLotPrepare(bankID, vendorLotID, productID, cassetteID, waferSize, isNeedAssignWaferID);
    }

    public Response VendorLotPrepare(String bankID, String vendorLotID, String productID, ObjectIdentifier cassetteID, Integer waferSize) {
        return this.VendorLotPrepare(bankID, vendorLotID, productID, cassetteID, waferSize, false);
    }
    public Response VendorLotPrepare(String bankID, String vendorLotID, String productID, ObjectIdentifier cassetteID, Integer waferSize, boolean isNeedAssignWaferID){
        //【step1】make receive
        vendorLotReceiveCase.VendorLotReceive(bankID, "Vendor", vendorLotID, productID, 100);
        //【step2】make prepare
        Map<String, Integer> sourceLotMap = new HashMap<>();
        sourceLotMap.put(vendorLotID, waferSize);
        return bankTestCase.vendorLotPreparedReqCase(new ObjectIdentifier(bankID), sourceLotMap, "Vendor", "Vendor", cassetteID, 0, isNeedAssignWaferID);

    }

    public Response VendorLotPrepare(String bankID, String vendorLotID, String productID, ObjectIdentifier cassetteID, Integer waferSize, boolean isNeedAssignWaferID,String lotType,String subLotType){
        //【step1】make receive
        vendorLotReceiveCase.VendorLotReceive(bankID, "Vendor", vendorLotID, productID, 100);
        //【step2】make prepare
        Map<String, Integer> sourceLotMap = new HashMap<>();
        sourceLotMap.put(vendorLotID, waferSize);
        return bankTestCase.vendorLotPreparedReqCase(new ObjectIdentifier(bankID), sourceLotMap, lotType, subLotType, cassetteID, 0, isNeedAssignWaferID);

    }

    public Response VendorLotPrepare_EmptyCassette_SingleVendorLot() {
        String vendorLotID = GenerateVendorlot.getVendorLot();
        //【step1】vendor lot receive one vendor lot
        final String bankID = "BNK-0S";
        final String subLotType = "Vendor";
        final String productID = "RAW-2000.01";
        final String lotID = vendorLotID;
        vendorLotReceiveCase.VendorLotReceive(bankID, subLotType, lotID, productID, 100);

        //【step2】search empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();

        //【step3】vendor lot prepared
        Map<String, Integer> sourceLotMap = new HashMap<>();
        sourceLotMap.put(lotID, 5);
        return bankTestCase.vendorLotPreparedReqCase(new ObjectIdentifier(bankID), sourceLotMap, "Vendor", "Vendor", cassetteID, 0, false);
    }

    public Response VendorLotPrepare_EmptyCassette_MultipleVendorLot() {
        String vendorLotID = GenerateVendorlot.getVendorLot();
        //【step1】vendor lot receive one vendor lot
        final String bankID = "BNK-0S";
        final String subLotType = "Vendor";
        final String productID = "RAW-2000.01";
        final String lotID1 = GenerateVendorlot.getVendorLot();
        final String lotID2 = GenerateVendorlot.getVendorLot(1);
        vendorLotReceiveCase.VendorLotReceive(bankID, subLotType, lotID1, productID, 100);
        vendorLotReceiveCase.VendorLotReceive(bankID, subLotType, lotID2, productID, 100);

        //【step2】search empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();

        //【step3】vendor lot prepared
        Map<String, Integer> sourceLotMap = new HashMap<>();
        sourceLotMap.put(lotID1, 5);
        sourceLotMap.put(lotID2, 10);
        return bankTestCase.vendorLotPreparedReqCase(new ObjectIdentifier(bankID), sourceLotMap, "Vendor", "Vendor", cassetteID, 0, false);
    }

    public Response VendorLotPrepare_NotEmptyCassette() {
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        //【step1】vendor lot receive one vendor lot
        final String bankID = "BNK-0S";
        final String subLotType = "Vendor";
        final String productID = "RAW-2000.01";
        final String lotID1 = vendorLotID;
        vendorLotReceiveCase.VendorLotReceive(bankID, subLotType, lotID1, productID, 100);

        //【step2】search empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();

        //【step3】vendor lot prepared once
        Map<String, Integer> sourceLotMap = new HashMap<>();
        sourceLotMap.put(lotID1, 5);
        bankTestCase.vendorLotPreparedReqCase(new ObjectIdentifier(bankID), sourceLotMap, "Vendor", "Vendor", cassetteID, 0, false);

        //【step4】vendor lot prepared two
        Map<String, Integer> sourceLotMap2 = new HashMap<>();
        sourceLotMap2.put(lotID1, 10);
        return bankTestCase.vendorLotPreparedReqCase(new ObjectIdentifier(bankID), sourceLotMap2, "Vendor", "Vendor", cassetteID, 5, false);
    }

    public Response VendorLotPrepare_AssignWaferID() {
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        //【step1】vendor lot receive one vendor lot
        final String bankID = "BNK-0S";
        final String subLotType = "Vendor";
        final String productID = "RAW-2000.01";
        final String lotID1 = vendorLotID;
        vendorLotReceiveCase.VendorLotReceive(bankID, subLotType, lotID1, productID, 100);

        //【step4】search empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();

        //【step4】vendor lot prepared
        Map<String, Integer> sourceLotMap = new HashMap<>();
        sourceLotMap.put(lotID1, 5);
        return bankTestCase.vendorLotPreparedReqCase(new ObjectIdentifier(bankID), sourceLotMap, "Vendor", "Vendor", cassetteID, 0, true);
    }

}
