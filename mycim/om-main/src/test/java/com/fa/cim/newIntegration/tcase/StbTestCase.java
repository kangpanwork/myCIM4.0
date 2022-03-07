package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.*;
import com.fa.cim.controller.lotstart.LotStartController;
import com.fa.cim.controller.lotstart.LotStartInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.common.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/29        ********             Bear               create file
 * 2019/9/12        N/A                  Neko               Added STB Cancel methods
 *
 * @author: Bear
 * @date: 2019/8/29 15:43
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class StbTestCase {
    @Autowired
    BankTestCase bankTestCase;

    @Autowired
    LotStartController lotStartController;

    @Autowired
    LotStartInqController lotStartInqController;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    public Infos.ProdReqListAttribute productOrderReleasedListInqCase(String productID) {
        return this.productOrderReleasedListInqCase(productID, "");
    }

    public Infos.ProdReqListAttribute productOrderReleasedListInqCase(String productID, String subLotType){
        Params.ProductOrderReleasedListInqParams productOrderReleasedListInqParams = new Params.ProductOrderReleasedListInqParams();
        productOrderReleasedListInqParams.setUser(bankTestCase.getUser());
        productOrderReleasedListInqParams.setSearchCondition(bankTestCase.getSearchCondition());
        productOrderReleasedListInqParams.setProductID(productID);
        if (!CimStringUtils.isEmpty(subLotType)){
            productOrderReleasedListInqParams.setSubLotType(subLotType);
        }
        productOrderReleasedListInqParams.setLotType("Production");
        Response result = lotStartInqController.productOrderReleasedListInq(productOrderReleasedListInqParams);
        Validations.isSuccessWithException(result);
        Results.ProductOrderReleasedListInqResult productOrderReleasedListInqResult = (Results.ProductOrderReleasedListInqResult)result.getBody();
        List<Infos.ProdReqListAttribute> prodReqListAttributeList = productOrderReleasedListInqResult.getProductReqListAttributePage().getContent();
        for (Infos.ProdReqListAttribute prodReqListAttribute : prodReqListAttributeList){
            if (prodReqListAttribute.getProductCount() == 25){
                return prodReqListAttribute;
            }
        }
        return null;
    }
    public Infos.ProdReqListAttribute productOrderReleasedListInqCase(String productID, Long productCount) {
        Params.ProductOrderReleasedListInqParams productOrderReleasedListInqParams = new Params.ProductOrderReleasedListInqParams();
        productOrderReleasedListInqParams.setUser(bankTestCase.getUser());
        productOrderReleasedListInqParams.setSearchCondition(bankTestCase.getSearchCondition());
        productOrderReleasedListInqParams.setProductID(productID);
        productOrderReleasedListInqParams.setLotType("Production");
        Response result = lotStartInqController.productOrderReleasedListInq(productOrderReleasedListInqParams);
        Validations.isSuccessWithException(result);
        Results.ProductOrderReleasedListInqResult productOrderReleasedListInqResult = (Results.ProductOrderReleasedListInqResult)result.getBody();
        List<Infos.ProdReqListAttribute> list = productOrderReleasedListInqResult.getProductReqListAttributePage().getContent();
        for (Infos.ProdReqListAttribute prodReqListAttribute : list) {
            if (null == productCount || productCount.equals(prodReqListAttribute.getProductCount())) {
                if (prodReqListAttribute.getProductID().getValue().equals("PRODUCT1.01")){
                    if (prodReqListAttribute.getManufacturingLayerID().getValue().equals("A") && prodReqListAttribute.getCustomerCode().equals("FA")){
                        return prodReqListAttribute;
                    }
                } else {
                    return prodReqListAttribute;
                }
            }
        }
        return null;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/8/28 14:18
     * @param lotID
     * @param cassetteID
     * @param position
     * @param productCount -
     * @return com.fa.cim.common.support.Response
     */
    public Response waferLotStartReqCase(ObjectIdentifier lotID, ObjectIdentifier cassetteID, int position, long productCount) {

        //【step1】get lot info which in cassette
        Response response = bankTestCase.lotListByCarrierInqCase(cassetteID);
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult)response.getBody();

        //【step2】make stb
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setCassetteID(cassetteID);
        for (int i = 0; i < productCount; i++) {
            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
            Infos.WaferMapInCassetteInfo waferMapInCassetteInfo = lotListByCarrierInqResult.getWaferMapInCassetteInfoList().get(position + i);
            ObjectIdentifier sourceLotID = waferMapInCassetteInfo.getLotID();
            ObjectIdentifier sourceWaferID = waferMapInCassetteInfo.getWaferID();
            Integer slotNumber = waferMapInCassetteInfo.getSlotNumber();
            newWaferAttributes.setNewSlotNumber(slotNumber);
            newWaferAttributes.setNewLotID(lotID);
            newWaferAttributes.setSourceLotID(sourceLotID);
            newWaferAttributes.setSourceWaferID(sourceWaferID);
            newWaferAttributesList.add(newWaferAttributes);
        }
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        Params.WaferLotStartReqParams waferLotStartReqParams = new Params.WaferLotStartReqParams();
        waferLotStartReqParams.setUser(bankTestCase.getUser());
        waferLotStartReqParams.setProductRequestID(lotID);
        waferLotStartReqParams.setNewLotAttributes(newLotAttributes);
        return lotStartController.waferLotStartReq(waferLotStartReqParams);
    }

    public Response waferLotStartReqCase(ObjectIdentifier lotID, ObjectIdentifier cassetteID, String vendorLotID, long productCount) {
        if (CimObjectUtils.isEmpty(cassetteID)){
            cassetteID = testUtils.findEmptyCassette("FOUP", "AVAILABLE");
        }
        //【step1】get lot info which in cassette
        Response response = bankTestCase.lotListByCarrierInqCase(cassetteID);
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult)response.getBody();
        List<ObjectIdentifier> waferIDList = lotListByCarrierInqResult.getWaferMapInCassetteInfoList().stream()
                .filter(waferMapInCassetteInfo -> !CimObjectUtils.isEmptyWithValue(waferMapInCassetteInfo.getWaferID()))
                .map(waferMapInCassetteInfo -> waferMapInCassetteInfo.getWaferID()).collect(Collectors.toList());
        long totalWaferSize = CimArrayUtils.getSize(waferIDList) + productCount;
        if (totalWaferSize > 25){
            Validations.check(true,"wafer size > 25 is error");
        }
        //【step2】make stb
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setCassetteID(cassetteID);
        for (int i = CimArrayUtils.getSize(waferIDList); i < totalWaferSize; i++) {
            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
            newWaferAttributes.setNewSlotNumber(i + 1);
            newWaferAttributes.setNewLotID(lotID);
            newWaferAttributes.setSourceLotID(new ObjectIdentifier(vendorLotID));
            newWaferAttributes.setSourceWaferID(new ObjectIdentifier());
            newWaferAttributesList.add(newWaferAttributes);
        }
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        Params.WaferLotStartReqParams waferLotStartReqParams = new Params.WaferLotStartReqParams();
        waferLotStartReqParams.setUser(bankTestCase.getUser());
        waferLotStartReqParams.setProductRequestID(lotID);
        waferLotStartReqParams.setNewLotAttributes(newLotAttributes);
        return lotStartController.waferLotStartReq(waferLotStartReqParams);
    }

    /**     
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * @author Bear
      * @date 2019/8/29 15:48
      * @param vendorLotID
      * @param selectedWaferSize -
      * @return com.fa.cim.common.support.Response
      */
    public Response waferLotStartReqCase(ObjectIdentifier lotID, String vendorLotID, long selectedWaferSize) {
        //【step1】search one empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();

        //【step3】make stb
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setCassetteID(cassetteID);
        for (int i = 0; i < selectedWaferSize; i++) {
            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
            newWaferAttributes.setNewSlotNumber(i + 1);
            newWaferAttributes.setNewLotID(lotID);
            newWaferAttributes.setSourceLotID(new ObjectIdentifier(vendorLotID));
            newWaferAttributes.setSourceWaferID(new ObjectIdentifier());
            newWaferAttributesList.add(newWaferAttributes);
        }
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        Params.WaferLotStartReqParams waferLotStartReqParams = new Params.WaferLotStartReqParams();
        waferLotStartReqParams.setUser(bankTestCase.getUser());
        waferLotStartReqParams.setProductRequestID(lotID);   // productRequestID = LotID = NP****.00A
        waferLotStartReqParams.setNewLotAttributes(newLotAttributes);
        return lotStartController.waferLotStartReq(waferLotStartReqParams);
    }

    public Response waferLotStartReqCase(ObjectIdentifier lotID, Map<String, Integer> sourceLotMap, boolean isNeedAssignWaferID) {
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();
        int position = 0;
        return waferLotStartReqCase(lotID, sourceLotMap, cassetteID, position, isNeedAssignWaferID);
    }

    public Response waferLotStartReqCase(ObjectIdentifier lotID, ObjectIdentifier cassetteID, Map<String, Integer> sourceLotMap, boolean isNeedAssignWaferID) {
        if (CimObjectUtils.isEmpty(cassetteID)){
            cassetteID = testUtils.findEmptyCassette("FOUP", "AVAILABLE");
        }
        int position = 0;
        return waferLotStartReqCase(lotID, sourceLotMap, cassetteID, position, isNeedAssignWaferID);
    }

    public Response waferLotStartReqCase(ObjectIdentifier lotID, Map<String, Integer> sourceLotMap,
                                          ObjectIdentifier cassetteID, int position, boolean isNeedAssignWaferID) {

        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setCassetteID(cassetteID);

        //【step2】get lot info in cassette
        //Map<Slot, WaferID>
        Map<Integer, ObjectIdentifier> slotToWaferIDMap = new HashMap<>();
        Response response = bankTestCase.lotListByCarrierInqCase(cassetteID);
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult)response.getBody();
        if (null != lotListByCarrierInqResult) {
            List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfoList = lotListByCarrierInqResult.getWaferMapInCassetteInfoList();
            for (Infos.WaferMapInCassetteInfo waferMapInCassetteInfo:waferMapInCassetteInfoList) {
                slotToWaferIDMap.put(waferMapInCassetteInfo.getSlotNumber(), waferMapInCassetteInfo.getWaferID());
                if (waferMapInCassetteInfo.getSlotNumber() >= (position + 1)) {
                    Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
                    newWaferAttributes.setNewSlotNumber(waferMapInCassetteInfo.getSlotNumber());
                    newWaferAttributes.setNewLotID(lotID);
                    newWaferAttributes.setSourceLotID(waferMapInCassetteInfo.getLotID());
                    newWaferAttributes.setSourceWaferID(waferMapInCassetteInfo.getWaferID());
                    newWaferAttributesList.add(newWaferAttributes);
                }
            }
        }

        position = slotToWaferIDMap.size();
        for (Map.Entry<String, Integer> entry : sourceLotMap.entrySet()) {
            ObjectIdentifier vendorLotID = new ObjectIdentifier(entry.getKey());
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
                int newSlotNumber = i + 1 + position;
                newWaferAttributes.setNewSlotNumber(newSlotNumber);
                newWaferAttributes.setNewLotID(lotID);
                newWaferAttributes.setSourceLotID(vendorLotID);

                // if the cassette had been prepared lot, then we can get the sourceWaferID from the slotToWaferIDMap, else set to null
                newWaferAttributes.setSourceWaferID(slotToWaferIDMap.get(newSlotNumber));

                if (CimBooleanUtils.isTrue(isNeedAssignWaferID)) {
                    String waferID = String.format("%s.%02d", lotID.getValue(), newSlotNumber);
                    newWaferAttributes.setNewWaferID(new ObjectIdentifier(waferID));
                }
                newWaferAttributesList.add(newWaferAttributes);
            }
            position += count;
        }
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        Params.WaferLotStartReqParams waferLotStartReqParams = new Params.WaferLotStartReqParams();
        waferLotStartReqParams.setUser(bankTestCase.getUser());
        waferLotStartReqParams.setProductRequestID(lotID);
        waferLotStartReqParams.setNewLotAttributes(newLotAttributes);
        return lotStartController.waferLotStartReq(waferLotStartReqParams);
    }


     //// STB Cancel methods STARTs

    /**
     *  Get the lot's detailed information
     *  send request to WaferLotStartCancelInfoInq for lot's info
     * @param  lotID is from the lot just STBed
     * @return WaferLotStartCancelReqParams
     */
    public Params.WaferLotStartCancelReqParams prepareLotWaferLotStartCancelReqParam(ObjectIdentifier lotID) {

        //【step1】get the cancel info for lot
        Params.WaferLotStartCancelInfoInqParams waferLotStartCancelInfoInqParams = new Params.WaferLotStartCancelInfoInqParams();
        waferLotStartCancelInfoInqParams.setUser(bankTestCase.getUser());
        waferLotStartCancelInfoInqParams.setStbCancelledLotID(lotID);
        Response result = lotStartInqController.WaferLotStartCancelInfoInq(waferLotStartCancelInfoInqParams);
        Validations.isSuccessWithException(result);

        Results.WaferLotStartCancelInfoInqResult waferLotStartCancelInfoInqResult =
                (Results.WaferLotStartCancelInfoInqResult) result.getBody();

        //【step2】Prepare lot parameters for requesting STB-Cancel
        Params.WaferLotStartCancelReqParams params = new Params.WaferLotStartCancelReqParams();
        Infos.NewLotAttributes lotAttributes = new Infos.NewLotAttributes();

        lotAttributes.setCassetteID(waferLotStartCancelInfoInqResult.getStbCancelledLotInfo().getCassetteID());
        List<Infos.NewWaferAttributes> waferAttributesList = new ArrayList<>();
        List<Infos.STBCancelWaferInfo> stbCancelWaferInfoList =
                waferLotStartCancelInfoInqResult.getStbCancelWaferInfoList();

        for (Infos.STBCancelWaferInfo stbCancelWaferInfo : stbCancelWaferInfoList) {
            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
            newWaferAttributes.setSourceWaferID(stbCancelWaferInfo.getWaferID());
            newWaferAttributes.setSourceLotID(new ObjectIdentifier(stbCancelWaferInfo.getStbSourceLotID()));
            newWaferAttributes.setNewLotID(stbCancelWaferInfo.getCurrentLotID());
            newWaferAttributes.setNewSlotNumber(Integer.parseInt(stbCancelWaferInfo.getSlotNo().toString()));
            newWaferAttributes.setNewWaferID(new ObjectIdentifier());
            waferAttributesList.add(newWaferAttributes);
        }
        lotAttributes.setNewWaferAttributesList(waferAttributesList);

        List<Infos.NewPreparedLotInfo> newPreparedLotInfoList = waferLotStartCancelInfoInqResult.getNewPreparedLotInfoList();

        params.setUser(bankTestCase.getUser());
        params.setStbCancelledLotID(lotID);
        params.setNewLotAttributes(lotAttributes);
        params.setNewPreparedLotInfoList(newPreparedLotInfoList);

        log.info("Successfully acquired to-be-canceled Lot's info");
        return params;

    }

    public Response stbLotCancelReqCase(ObjectIdentifier lotID){
        Params.WaferLotStartCancelReqParams params = this.prepareLotWaferLotStartCancelReqParam(lotID);
        Response result = lotStartController.waferLotStartCancelReq(params);
        Validations.isSuccessWithException(result);
        log.info("【Request result】"+result.getMessage());
        return result;
    }

    public List<Infos.ProductIDListAttributes> productIdListInqCase(String productCategory) {
        Params.ProductIdListInqInParams params = new Params.ProductIdListInqInParams();
        params.setProductCategory(productCategory);
        params.setProductGroupID("");
        params.setRouteID("");
        params.setUser(bankTestCase.getUser());
        Response productIdListInqResult = lotStartInqController.productIdListInq(params);
        Validations.isSuccessWithException(productIdListInqResult);
        List<Infos.ProductIDListAttributes> result = (List<Infos.ProductIDListAttributes>) productIdListInqResult.getBody();
        return result;
    }

    public Response npwLotStartReqCase(String subLotType, String vendorLotID, ObjectIdentifier productID, Integer waferCount) {
        //【step1】search one empty cassette
        ObjectIdentifier cassetteID = bankTestCase.getEmptyCassette();

        //【step2】make stb
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setCassetteID(cassetteID);
        for (int i = 0; i < waferCount; i++) {
            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
            newWaferAttributes.setNewSlotNumber(i + 1);
            newWaferAttributes.setNewLotID(new ObjectIdentifier());
            newWaferAttributes.setSourceLotID(new ObjectIdentifier(vendorLotID));
            newWaferAttributes.setSourceWaferID(new ObjectIdentifier());
            newWaferAttributes.setNewWaferID(new ObjectIdentifier());
            newWaferAttributesList.add(newWaferAttributes);
        }
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        Params.NPWLotStartReqParams npwLotStartReqParams = new Params.NPWLotStartReqParams();
        npwLotStartReqParams.setUser(bankTestCase.getUser());
        npwLotStartReqParams.setLotType(subLotType);
        npwLotStartReqParams.setSubLotType(subLotType);
        npwLotStartReqParams.setProductID(productID);
        npwLotStartReqParams.setWaferCount(waferCount);
        npwLotStartReqParams.setNewLotAttributes(newLotAttributes);
        return lotStartController.npwLotStartReq(npwLotStartReqParams);
    }

    //// STB Cancel methods Ends


}
