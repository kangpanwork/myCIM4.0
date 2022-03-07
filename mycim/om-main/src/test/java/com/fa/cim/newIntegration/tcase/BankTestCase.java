package com.fa.cim.newIntegration.tcase;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.bank.BankController;
import com.fa.cim.controller.bank.BankInqController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.controller.interfaces.lot.ILotInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/12/13        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/12/13 10:50
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class BankTestCase {
    @Autowired
    private BankInqController bankInqController;

    @Autowired
    private ILotInqController electronicInformationInqController;

    @Autowired
    private BankController bankController;

    @Autowired
    private DurableInqController durableInqController;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/8/26 17:08
     * @param bankID
     * @param subLotType
     * @param lotID
     * @param productID
     * @param count -
     * @return com.fa.cim.common.support.Response
     */
    public Response vendorLotReceiveReqCase(String bankID, String subLotType, String lotID, String productID, int count) {
        // if lotID != NULL && the lotID has been existed, so skip vendor lot receive
        if (!CimStringUtils.isEmpty(lotID)) {
            try {
                Params.LotInfoInqParams lotInfoInqParams = new Params.LotInfoInqParams();
                ArrayList<ObjectIdentifier> objectIdentifiers = new ArrayList<>();
                objectIdentifiers.add(new ObjectIdentifier(lotID));
                lotInfoInqParams.setLotIDs(objectIdentifiers);
                Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
                lotInfoInqFlag.setLotBasicInfoFlag(false);
                lotInfoInqFlag.setLotBackupInfoFlag(false);
                lotInfoInqFlag.setLotControlJobInfoFlag(false);
                lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
                lotInfoInqFlag.setLotControlUseInfoFlag(false);
                lotInfoInqFlag.setLotLocationInfoFlag(false);
                lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
                lotInfoInqFlag.setLotOperationInfoFlag(false);
                lotInfoInqFlag.setLotOrderInfoFlag(false);
                lotInfoInqFlag.setLotProductInfoFlag(false);
                lotInfoInqFlag.setLotRecipeInfoFlag(false);
                lotInfoInqFlag.setLotWipOperationInfoFlag(false);
                lotInfoInqFlag.setLotWaferAttributesFlag(false);
                lotInfoInqFlag.setLotListInCassetteInfoFlag(false);
                lotInfoInqFlag.setLotWaferMapInCassetteInfoFlag(false);
                lotInfoInqParams.setLotInfoInqFlag(lotInfoInqFlag);
                lotInfoInqParams.setUser(getUser());
                Response response = electronicInformationInqController.lotInfoInq(lotInfoInqParams);
                // the lotID is already exist
                Results.VendorLotReceiveReqResult result = new Results.VendorLotReceiveReqResult();
                result.setCreatedLotID(lotID);
                return Response.createSucc("OBNKW001", result);
            }catch (ServiceException e) {
            }
        }
        Params.VendorLotReceiveParams vendorLotReceiveParams = new Params.VendorLotReceiveParams();
        vendorLotReceiveParams.setUser(getUser());
        vendorLotReceiveParams.setSubLotType(subLotType);
        vendorLotReceiveParams.setLotID(lotID);
        vendorLotReceiveParams.setBankID(new ObjectIdentifier(bankID));
        vendorLotReceiveParams.setProductID(new ObjectIdentifier(productID));
        vendorLotReceiveParams.setProductWaferCount(count);
        vendorLotReceiveParams.setSubLotType("Vendor");

        Response result = bankController.vendorLotReceiveReq(vendorLotReceiveParams);
        Validations.assertCheck(result.getCode() == 0, "vendorLotReceiveReqCase() != 0" );
        return result;
    }

    /**     
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * @author Bear
      * @date 2019/8/29 15:21
      * @param bankID
      * @param inqBank -
      * @return com.fa.cim.common.support.Response
      */
    public Response bankListInqCase(String bankID, String inqBank) {
        Params.BankListInqParams bankListInqParams = new Params.BankListInqParams();
        bankListInqParams.setUser(getUser());
        bankListInqParams.setBankID(new ObjectIdentifier(bankID));
        bankListInqParams.setInqBank(inqBank);
        //the UDO maybe not init, but sure not be null;
        bankListInqParams.setSearchCondition(new SearchCondition());

        Response result = bankInqController.bankListInq(bankListInqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response lotInfoByLotListInqCase(String bankID) {
        Params.LotListInqParams lotListInqParams = new Params.LotListInqParams();
        lotListInqParams.setUser(getUser());
        lotListInqParams.setSearchCondition(getSearchCondition());
        lotListInqParams.setDeleteLotFlag(false);
        lotListInqParams.setBankID(new ObjectIdentifier(bankID));
        Response result = electronicInformationInqController.lotListInq(lotListInqParams);
        return result;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return com.fa.cim.dto.SearchCondition
     * @author Bear
     * @date 2018/12/14 16:05
     */
    public SearchCondition getSearchCondition() {
        return testCommonData.getSEARCHCONDITION();
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return com.fa.cim.dto.Response
     * @author Bear
     * @date 2018/12/14 14:38
     */
    public Response productIDCase() {
        Params.productInqParams productInqParams = new Params.productInqParams();
        productInqParams.setUser(getUser());
        Response result = bankController.getProduct(productInqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    /**     
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * @author Bear
      * @date 2019/8/29 15:31
      * @param bankID
      * @param sourceLotMap
      * @param lotType
      * @param subLotType
      * @param cassetteID
      * @param position
      * @param isNeedAssignWaferID -
      * @return com.fa.cim.common.support.Response
      */
    public Response vendorLotPreparedReqCase(ObjectIdentifier bankID, Map<String, Integer> sourceLotMap, String lotType, String subLotType,
                                             ObjectIdentifier cassetteID, int position, boolean isNeedAssignWaferID) {
        Params.MaterialPrepareReqParams materialPrepareReqParams = new Params.MaterialPrepareReqParams();
        materialPrepareReqParams.setLotType(lotType);
        materialPrepareReqParams.setSubLotType(subLotType);
        materialPrepareReqParams.setUser(getUser());
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        newLotAttributes.setCassetteID(cassetteID);
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry: sourceLotMap.entrySet()) {
            ObjectIdentifier sourceLotID = new ObjectIdentifier(entry.getKey());
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
                newWaferAttributes.setSourceLotID(sourceLotID);
                int newSlotNumber = i + 1 + position;
                newWaferAttributes.setNewSlotNumber(newSlotNumber);
                if (CimBooleanUtils.isTrue(isNeedAssignWaferID)) {
                    String waferID = String.format("%s.%02d", sourceLotID.getValue(), newSlotNumber);
                    newWaferAttributes.setNewWaferID(new ObjectIdentifier(waferID));
                }
                newWaferAttributesList.add(newWaferAttributes);
            }
            position += count;
        }
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        materialPrepareReqParams.setNewLotAttributes(newLotAttributes);
        materialPrepareReqParams.setBankID(bankID);
        Response result = bankController.materialPrepareReq(materialPrepareReqParams);
        return result;
    }

    public ObjectIdentifier getEmptyCassette() {
        Response response = carrierListInqCase();
        ObjectIdentifier cassetteID = null;
        Results.CarrierListInq170Result carrierListInqResult = (Results.CarrierListInq170Result) response.getBody();
        List<Infos.FoundCassette> foundCassetteList = carrierListInqResult.getFoundCassette().getContent();
        for (Infos.FoundCassette foundCassette:foundCassetteList) {
            cassetteID = foundCassette.getCassetteID();
            if (cassetteID.getValue().contains("CRUP") && CimObjectUtils.isEmptyWithValue(foundCassette.getEquipmentID())) {
                break;
            }
        }
        Validations.check(null == cassetteID, "cassetteID is null");
        return cassetteID;
    }

    public ObjectIdentifier getFoupCassette() {
        Response response = carrierListInqCase();
        ObjectIdentifier cassetteID = null;
        Results.CarrierListInq170Result carrierListInqResult = (Results.CarrierListInq170Result) response.getBody();
        List<Infos.FoundCassette> foundCassetteList = carrierListInqResult.getFoundCassette().getContent();
        for (Infos.FoundCassette foundCassette:foundCassetteList) {
            if ("FOUP".equalsIgnoreCase(foundCassette.getCassetteCategory())){
                cassetteID = foundCassette.getCassetteID();
                break;
            }
        }
        Validations.check(null == cassetteID, "cassetteID is null");
        return cassetteID;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return com.fa.cim.dto.Response
     * @author Bear
     * @date 2018/12/14 16:05
     */
    public Response carrierListInqCase() {
        Params.CarrierListInqParams carrierListInqParams = new Params.CarrierListInqParams();
        carrierListInqParams.setUser(getUser());
        carrierListInqParams.setEmptyFlag(true);
        carrierListInqParams.setMaxRetrieveCount(300);
        carrierListInqParams.setSearchCondition(getSearchCondition());
        carrierListInqParams.setCassetteStatus("AVAILABLE");
        Response result = durableInqController.carrierListInq(carrierListInqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response lotListByCarrierInqCase(ObjectIdentifier cassetteID) {
        log.debug("【step2】call carrierListInq to choose one cassette, the cassetteID:%s", cassetteID);

        //【step2】call TxLotListByCarrierInq
        log.debug("【step2】call TxLotListByCarrierInq");
        Params.LotListByCarrierInqParams lotListByCarrierInqParams = new Params.LotListByCarrierInqParams();
        lotListByCarrierInqParams.setUser(getUser());
        lotListByCarrierInqParams.setCassetteID(cassetteID);
        Response result = electronicInformationInqController.lotListByCarrierInq(lotListByCarrierInqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response bankShipCase(ObjectIdentifier bankID, List<ObjectIdentifier> lotIDs){
        Params.BankMoveReqParams bankMoveReqParams = new Params.BankMoveReqParams();
        bankMoveReqParams.setUser(getUser());
        bankMoveReqParams.setBankID(bankID);
        bankMoveReqParams.setLotIDs(lotIDs);
        Response result = bankController.shipReq(bankMoveReqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response bankShipCancelCase(ObjectIdentifier bankID, List<ObjectIdentifier> lotIDs){
        Params.BankMoveReqParams bankMoveReqParams = new Params.BankMoveReqParams();
        bankMoveReqParams.setUser(getUser());
        bankMoveReqParams.setBankID(bankID);
        bankMoveReqParams.setLotIDs(lotIDs);
        Response result = bankController.unshipReq(bankMoveReqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response moveBankCase(ObjectIdentifier bankID, List<ObjectIdentifier> lotIDs){
        Params.BankMoveReqParams bankMoveReqParams = new Params.BankMoveReqParams();
        bankMoveReqParams.setUser(getUser());
        bankMoveReqParams.setBankID(bankID);
        bankMoveReqParams.setLotIDs(lotIDs);
        Response result = bankController.bankMoveReq(bankMoveReqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response bankInCase(List<ObjectIdentifier> lotIDs){
        Params.BankInReqParams bankInReqParams = new Params.BankInReqParams();
        bankInReqParams.setUser(getUser());
        bankInReqParams.setLotIDs(lotIDs);
        Response result =  bankController.bankInReq(bankInReqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response bankHoldCase(List<ObjectIdentifier> lotIDs,ObjectIdentifier reasonCodeID){
        Params.HoldLotInBankReqParams holdLotInBankReqParams=new Params.HoldLotInBankReqParams();
        holdLotInBankReqParams.setUser(getUser());
        holdLotInBankReqParams.setLotIDs(lotIDs);
        holdLotInBankReqParams.setReasonCodeID(reasonCodeID);
        Response result = bankController.holdLotInBankReq(holdLotInBankReqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response bankReleaseCase(List<ObjectIdentifier> lotIDs,String requestUserID,ObjectIdentifier reasonCodeID){
        Params.HoldLotReleaseInBankReqParams holdLotReleaseInBankReqParams=new Params.HoldLotReleaseInBankReqParams();
        holdLotReleaseInBankReqParams.setUser(getUser());
        holdLotReleaseInBankReqParams.setLotIDs(lotIDs);
        holdLotReleaseInBankReqParams.setReasonCodeID(reasonCodeID);
        Response result = bankController.holdLotReleaseInBankReq(holdLotReleaseInBankReqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response VendorLotPrepareCase(ObjectIdentifier bankID,String lotType,String subLotType,Infos.NewLotAttributes newLotAttributes){
        Params.MaterialPrepareReqParams materialPrepareReqParams=new Params.MaterialPrepareReqParams();
        materialPrepareReqParams.setUser(getUser());
        materialPrepareReqParams.setBankID(bankID);
        materialPrepareReqParams.setLotType(lotType);
        materialPrepareReqParams.setSubLotType(subLotType);
        materialPrepareReqParams.setNewLotAttributes(newLotAttributes);
        Response result = bankController.materialPrepareReq(materialPrepareReqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response VendorLotCancelCase(ObjectIdentifier preparationCancelledLotID,List<Infos.NewVendorLotInfo> newVendorLotInfoList){
        Params.MaterialPrepareCancelReqParams materialPrepareCancelReqParams=new Params.MaterialPrepareCancelReqParams();
        materialPrepareCancelReqParams.setUser(getUser());
        materialPrepareCancelReqParams.setPreparationCancelledLotID(preparationCancelledLotID);
        materialPrepareCancelReqParams.setNewVendorLotInfoList(newVendorLotInfoList);
        Response result = bankController.materialPrepareCancelReq(materialPrepareCancelReqParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response VendorLotReceiveCase(ObjectIdentifier bankID,ObjectIdentifier productID,String lotID,String subLotType,Integer productWaferCount,String vendorLotID,String vendorID){
        Params.VendorLotReceiveParams vendorLotReceiveParams=new Params.VendorLotReceiveParams();
        vendorLotReceiveParams.setUser(getUser());
        vendorLotReceiveParams.setBankID(bankID);
        vendorLotReceiveParams.setProductID(productID);
        vendorLotReceiveParams.setLotID(lotID);
        vendorLotReceiveParams.setSubLotType(subLotType);
        vendorLotReceiveParams.setProductWaferCount(productWaferCount);
        vendorLotReceiveParams.setVendorLotID(vendorLotID);
        vendorLotReceiveParams.setVendorID(vendorID);
        Response result = bankController.vendorLotReceiveReq(vendorLotReceiveParams);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response VendorLotReturnCase(Integer productWaferCount,ObjectIdentifier lotID){
        Params.VendorLotReturnParams vendorLotReturnParams=new Params.VendorLotReturnParams();
        vendorLotReturnParams.setUser(getUser());
        vendorLotReturnParams.setProductWaferCount(productWaferCount);
        vendorLotReturnParams.setLotID(lotID);
        System.out.println(JSON.toJSONString(vendorLotReturnParams));
        Response result = bankController.vendorLotReturnReq(vendorLotReturnParams);
        Validations.isSuccessWithException(result);
        return result;
    }
}