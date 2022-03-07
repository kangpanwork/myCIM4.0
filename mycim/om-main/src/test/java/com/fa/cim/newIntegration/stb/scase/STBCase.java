package com.fa.cim.newIntegration.stb.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.lotstart.LotStartController;
import com.fa.cim.controller.lotstart.LotStartInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.bank.scase.VendorLotPrepareCase;
import com.fa.cim.newIntegration.bank.scase.VendorLotReceiveCase;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.tcase.BankTestCase;
import com.fa.cim.newIntegration.tcase.StbTestCase;
import com.fa.cim.utils.GenerateVendorlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/27        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/8/27 17:46
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class STBCase {
    @Autowired
    private BankTestCase bankTestCase;

    @Autowired
    private StbTestCase stbTestCase;

    @Autowired
    private VendorLotReceiveCase vendorLotReceiveCase;

    @Autowired
    private VendorLotPrepareCase vendorLotPrepareCase;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private LotStartInqController lotStartInqController;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private LotStartController lotStartController;

    public Response STB_PreparedLot() {
        //【step1】make default setting
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        return this.STB_PreparedLot(bankID, sourceProductID, productID);
    }

    public Response STB_PreparedLot(String bankID, String sourceProductID, String productID) {
        //【step1】get one empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();

        //【step2】make prepared(common case, just one vendor lot in cassette)
        Integer waferSize = 25;
        vendorLotPrepareCase.VendorLotPrepare(bankID, sourceProductID, cassetteID, waferSize);

        //【step3】search one product to STB
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();
        Long productCount = prodReqListAttribute.getProductCount();

        //【step5】make stb
        return stbTestCase.waferLotStartReqCase(lotID, cassetteID, 0, productCount);
    }

    public Response STB_PreparedLotForFoup(String bankID, String sourceProductID, String productID) {
        //【step1】get one empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getFoupCassette();

        //【step2】make prepared(common case, just one vendor lot in cassette)
        Integer waferSize = 25;
        vendorLotPrepareCase.VendorLotPrepare(bankID, sourceProductID, cassetteID, waferSize);

        //【step3】search one product to STB
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();
        Long productCount = prodReqListAttribute.getProductCount();

        //【step5】make stb
        return stbTestCase.waferLotStartReqCase(lotID, cassetteID, 0, productCount);
    }

    public ObjectIdentifier STB_PreparedLot(TestInfos.StbInfo stbInfo) {
        //【step1】get one empty cassette
        ObjectIdentifier cassetteID = CimObjectUtils.isEmpty(stbInfo.getCassetteID()) ? testUtils.findEmptyCassette("FOUP", "AVAILABLE") : stbInfo.getCassetteID();


        //【step2】make prepared(common case, just one vendor lot in cassette)
        Integer waferSize = 25;
        if (!stbInfo.isNeedAssignWaferID()){
            vendorLotPrepareCase.VendorLotPrepare(stbInfo.getBankID(), stbInfo.getSourceProductID(), cassetteID, waferSize);
        } else {
            vendorLotPrepareCase.VendorLotPrepare(stbInfo.getBankID(), stbInfo.getSourceProductID(), cassetteID, waferSize, true);
        }

        //【step3】search one product to STB
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(stbInfo.getProductID(), stbInfo.getProductCount());
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();
        Long productCount = prodReqListAttribute.getProductCount();

        //【step5】make stb
        Response response = stbTestCase.waferLotStartReqCase(lotID, cassetteID, 0, productCount);
        return ((Results.WaferLotStartReqResult)response.getBody()).getLotID();
    }

    public ObjectIdentifier STB_Normal(TestInfos.StbInfo stbInfo) {
        ObjectIdentifier lotID = stbInfo.isNeedPrepare() ? this.STB_PreparedLot(stbInfo) : this.STB_NotPreparedLot(stbInfo);
        return lotID;
    }

    public ObjectIdentifier STB_MonitorLot(TestInfos.StbMonitorLotInfo stbMonitorLotInfo) {
        //【step1】vendor lot receive
        String vendorLotID = "VL011010.00";
        String bankID = stbMonitorLotInfo.getBankID();
        String sourceProductID = stbMonitorLotInfo.getSourceProductID();
        ObjectIdentifier productID = stbMonitorLotInfo.getProductID();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】lotstart/product_id_list/inq
        Params.ProductIdListInqInParams productIdListInqInParams = new Params.ProductIdListInqInParams();
        productIdListInqInParams.setUser(testUtils.getUser());
        productIdListInqInParams.setProductCategory("Process Monitor");
        List<Infos.ProductIDListAttributes> body = (List<Infos.ProductIDListAttributes>) lotStartInqController.productIdListInq(productIdListInqInParams).getBody();
        if (CimObjectUtils.isEmpty(productID)) {
            productID = body.get(0).getProductID();
        }

        //【step3】find one empty cassette
        String cassetteCategory = "FOUP";
        String cassetteStatus = "AVAILABLE";
        ObjectIdentifier emptyCassette = testUtils.findEmptyCassette(cassetteCategory, cassetteStatus);

        //【step3】stb monitor lot
        Params.NPWLotStartReqParams params = new Params.NPWLotStartReqParams();
        params.setUser(testUtils.getUser());
        params.setProductID(productID);
        params.setWaferCount(stbMonitorLotInfo.getProductCount());
        params.setLotType("Process Monitor");
        params.setSubLotType("Process Monitor");
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        newLotAttributes.setCassetteID(emptyCassette);
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        for (int i = 0; i < stbMonitorLotInfo.getProductCount(); i++) {
            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
            newWaferAttributes.setNewSlotNumber( i + 1);
            newWaferAttributes.setSourceLotID(new ObjectIdentifier(vendorLotID));
            newWaferAttributesList.add(newWaferAttributes);
        }
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        params.setNewLotAttributes(newLotAttributes);
        ObjectIdentifier lotID = (ObjectIdentifier)lotStartController.npwLotStartReq(params).getBody();
        return lotID;
    }

    public ObjectIdentifier STB_PRODUCT101(boolean isNeedPrepare){
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo();
        stbInfo.setBankID("BNK-0S1E");
        stbInfo.setProductCount(25L);
        stbInfo.setNeedPrepare(isNeedPrepare);
        stbInfo.setProductID("PRODUCT1.01");
        stbInfo.setSourceProductID("PRODUCT0.01");
        stbInfo.setNeedAssignWaferID(true);
        ObjectIdentifier lotID = stbInfo.isNeedPrepare() ? this.STB_PreparedLot(stbInfo) : this.STB_NotPreparedLot(stbInfo);
        return lotID;
    }

    public Response STB_NotPreparedLot() {
        //【step1】make default setting
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        return this.STB_NotPreparedLot(bankID, sourceProductID, productID);
    }

    public Response STB_NotPreparedLotWithSubLotType(String subLotType){
        //【step1】make default setting
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        return this.STB_NotPreparedLot(bankID, sourceProductID, productID, subLotType);
    }

    //for EntityInhibityCase chamber test
    public Response STB_PoC_Product01() {
        //【step1】make default setting
        String bankID = "POC-STAR";
        String sourceProductID = "PoC_Raw.01";
        String productID = "PoC_Product.01";
        return this.STB_NotPreparedLot(bankID, sourceProductID, productID);
    }


    /**
     * description:produce n stb lots
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/6 16:35
     * @param n quantity than need produce
     * @param isFirst when run a test case ,if call the mehtod firstly ,the isFirst is true, or false
     * @return java.util.List<com.fa.cim.common.support.ObjectIdentifier>
     */
    public List<ObjectIdentifier> stb_NLots_NotPreparedCase(int n, boolean isFirst){
        return this.stb_NLots_NotPreparedCase(null, n, isFirst);
    }

    public List<ObjectIdentifier> stb_NLots_NotPreparedCase(String productID, int n, boolean isFirst){
        //【step0】make default setting
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        if (CimStringUtils.isEmpty(productID)){
            productID = "PRODUCT0.01";
        }
        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        if (isFirst){
            vendorLotReceiveCase.VendorLotReceive(bankID, vendorLotID, sourceProductID, 200);
        }
        List<ObjectIdentifier> lots = new ArrayList<>();
        for (int i = 0; i < n; i++){
            //【step2】search one product to stb
            Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID);
            ObjectIdentifier lotID = prodReqListAttribute.getLotID();
            Long productCount = prodReqListAttribute.getProductCount();
            //【step3】make stb
            Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbTestCase.waferLotStartReqCase(lotID, vendorLotID, productCount).getBody();
            lots.add(waferLotStartReqResult.getLotID());
        }
        return lots;
    }

    public Response STB_NotPreparedLot(String bankID, String sourceProductID, String productID){
        return this.STB_NotPreparedLot(bankID, sourceProductID, productID, "");
    }

    public Response STB_NotPreparedLot(String bankID, String sourceProductID, String productID, String subLotType){
        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】search one product to stb
        Infos.ProdReqListAttribute prodReqListAttribute = null;
        if (CimStringUtils.isEmpty(subLotType)){
            prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID);
        } else {
            prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID, subLotType);
        }
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();
        Long productCount = prodReqListAttribute.getProductCount();

        //【step3】make stb
        return stbTestCase.waferLotStartReqCase(lotID, vendorLotID, productCount);
    }

    public ObjectIdentifier STB_NotPreparedLot(String bankID, String sourceProductID, String productID, Long productCount) {

        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】search one product to stb
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID, productCount);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();

        //【step3】make stb
        Response response = stbTestCase.waferLotStartReqCase(lotID, vendorLotID, productCount);
        Validations.isSuccessWithException(response);

        return ((Results.WaferLotStartReqResult)response.getBody()).getLotID();
    }



    public ObjectIdentifier STB_NotPreparedLot(TestInfos.StbInfo stbInfo) {
        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        if (stbInfo.getProductID().equals("PRODUCT1.01")){
            vendorLotID = GenerateVendorlot.getVendorLot(1);;
        }
        vendorLotReceiveCase.VendorLotReceive(stbInfo.getBankID(),  vendorLotID, stbInfo.getSourceProductID(), 300);

        //【step2】search one product to stb
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(stbInfo.getProductID(), stbInfo.getProductCount());
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();

        //【step3】make stb
        Response response = null;
        if (!stbInfo.isNeedAssignWaferID()){
            response = stbTestCase.waferLotStartReqCase(lotID, stbInfo.getCassetteID(), vendorLotID, prodReqListAttribute.getProductCount());
        } else {
            Map<String, Integer> sourceLotMap = new HashMap<>();
            sourceLotMap.put(vendorLotID, stbInfo.getProductCount().intValue());
            response = stbTestCase.waferLotStartReqCase(lotID, stbInfo.getCassetteID(), sourceLotMap, true);
        }

        Validations.isSuccessWithException(response);

        return ((Results.WaferLotStartReqResult)response.getBody()).getLotID();
    }

    public Response STB_WaferCount_DifferentWith_ProductCount () {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        return this.STB_WaferCount_DifferentWith_ProductCount(bankID, sourceProductID, productID);
    }

    public Response STB_WaferCount_DifferentWith_ProductCount (String bankID, String sourceProductID, String productID) {
        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID, vendorLotID, sourceProductID, 100);

        //【step2】search one product to stb
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();
        Long productCount = prodReqListAttribute.getProductCount();

        //【step3】make stb
        Response response = null;
        try {
            response = stbTestCase.waferLotStartReqCase(lotID, vendorLotID, productCount - 1);
        }catch (Exception e) {
            ServiceException serviceException = (ServiceException) e;
            Validations.assertCheck(retCodeConfig.getStbWaferCountNotEnough().getCode() ==  serviceException.getCode(), serviceException.getMessage());
            //System.out.println(e);
        }
        return response;
    }

    public Response STB_After_Merging_Wafers_Into_One_Carrier (){
        // default setting
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";

        return this.STB_After_Merging_Wafers_Into_One_Carrier(bankID, sourceProductID, productID);
    }

    public Response STB_After_Merging_Wafers_Into_One_Carrier (String bankID, String sourceProductID, String productID){
        //【step1】make receive
        String vendorLotID1 = GenerateVendorlot.getVendorLot();
        Response response = vendorLotReceiveCase.VendorLotReceive(bankID, vendorLotID1,sourceProductID, 100);

        String vendorLotID2 = "VL011002.00";
        response = vendorLotReceiveCase.VendorLotReceive(bankID, vendorLotID2, sourceProductID,100);

        //【step2】search one product to stb
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();
        Long productCount = prodReqListAttribute.getProductCount();

        //【step3】make stb
        Map<String, Integer> sourceLotMap = new HashMap<>();
        sourceLotMap.put(vendorLotID1, 2);
        sourceLotMap.put(vendorLotID2, (int)(productCount - 2));
        return stbTestCase.waferLotStartReqCase(lotID, sourceLotMap, false);
    }
    public Response STB_After_Merging_Wafers_Into_One_Prepared_Carrier() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";

        //【step1】make receive
        String vendorLotID1 = GenerateVendorlot.getVendorLot();;

        //【step2】get one empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();

        //【step3】make prepared(common case, just one vendor lot in cassette)
        Integer waferSize = 2;
        Response response = vendorLotPrepareCase.VendorLotPrepare(bankID, vendorLotID1, sourceProductID, cassetteID, waferSize);

        //【step4】search one product to stb
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();
        Long productCount = prodReqListAttribute.getProductCount();

        Map<String, Integer> sourceLotMap = new HashMap<>();
        sourceLotMap.put(vendorLotID1,  Integer.parseInt(productCount.toString()) - 2);  // add 2 wafers from vendor lot to STB

        response = stbTestCase.waferLotStartReqCase(lotID, sourceLotMap, cassetteID, 0, false);
        return response;
    }


    public Response STB_Into_One_NonSource_Carrier() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";

        //【step1】make receive
        String vendorLotID1 = GenerateVendorlot.getVendorLot();;

        //【step2】get one empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();

        //【step3】make prepared(common case, just one vendor lot in cassette) ,in order to take the 2 position
        Integer waferSize = 2;
        Response response = vendorLotPrepareCase.VendorLotPrepare(bankID, vendorLotID1, sourceProductID, cassetteID, waferSize);

        //【step4】search one product to stb, the product count must be 10.
        Long productCount = 10L;
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID, productCount);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();

        Map<String, Integer> sourceLotMap = new HashMap<>();
        sourceLotMap.put(vendorLotID1,  Integer.parseInt(productCount.toString()));  // add 10 wafers from vendor lot to STB
        response = stbTestCase.waferLotStartReqCase(lotID, sourceLotMap, cassetteID, 2, false);


        return null;
    }

    public Response STB_No_carrier_move_generating_multiple_lots(){
        //【step1】make default setting
        String bankID = "BKP-RMAT";
        String sourceProductID = "DEV-1000.01";
        String productID = "DEV-12SOIAA001.01";

        //【step2】get one empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();

        //【step3】make material prepared
        Integer waferSize = 25;
        vendorLotPrepareCase.VendorLotPrepare(bankID, sourceProductID, cassetteID, waferSize);

        //【step4】search products to stb, the product count is 10, and Product count is 5.
        Long productCount1 = 10L;
        Long productCount2 = 5L;

        //【step5】make stb to generate first Lot
        Infos.ProdReqListAttribute prodReqListAttribute1 = stbTestCase.productOrderReleasedListInqCase(productID, productCount1);
        ObjectIdentifier lotID1 = prodReqListAttribute1.getLotID();
        stbTestCase.waferLotStartReqCase(lotID1, cassetteID, 0, productCount1);

        //【step6】make stb to generate second Lot
        Infos.ProdReqListAttribute prodReqListAttribute2 = stbTestCase.productOrderReleasedListInqCase(productID, productCount1);
        ObjectIdentifier lotID2 = prodReqListAttribute2.getLotID();
        stbTestCase.waferLotStartReqCase(lotID2, cassetteID, 10, productCount1);

        //【step7】make stb to generate third Lot
        Infos.ProdReqListAttribute prodReqListAttribute3 = stbTestCase.productOrderReleasedListInqCase(productID, productCount2);
        ObjectIdentifier lotID3 = prodReqListAttribute3.getLotID();
        stbTestCase.waferLotStartReqCase(lotID3, cassetteID, 20, productCount2);
        return null;
    }


    public Response STB_One_Carrier_have_25_wafers_to_STB_into_a_same_Carrier_with_5_wafer() {
        //【step1】make default setting
        String bankID = "BKP-RMAT";
        String sourceProductID = "DEV-1000.01";
        String productID = "DEV-12SOIAA001.01";

        //【step2】get one empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();

        //【step3】make material prepared
        Integer waferSize = 25;
        vendorLotPrepareCase.VendorLotPrepare(bankID, sourceProductID, cassetteID, waferSize);

        //【step4】search products to stb, the product count is 5.
        Long productCount = 5L;
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID, productCount);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();

        // 【step5】select the last 5 slot wafers as the new Lot
        return stbTestCase.waferLotStartReqCase(lotID, cassetteID, 20, productCount);
    }
}
