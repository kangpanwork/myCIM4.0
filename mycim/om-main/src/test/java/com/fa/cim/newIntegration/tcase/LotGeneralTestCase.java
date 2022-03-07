package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.lot.LotInqController;
import com.fa.cim.controller.system.SystemInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/9/2 15:21
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotGeneralTestCase {

    @Autowired
    private ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    private SystemInqController systemInqController;

    @Autowired
    private LotInqController lotInqController;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public List<Infos.LotInfo> getLotInfos(List<ObjectIdentifier> lotIDs){
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) this.getLotInfoCase(lotIDs).getBody();
        return lotInfoInqResult.getLotInfoList();
    }

    /**
     * description: get lot Operation base info : search button
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/9/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/9/2 15:23
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    public Response getLotInfoCase(List<ObjectIdentifier> lotIDs) {
        Params.LotInfoInqParams lotInfoInqParams = new Params.LotInfoInqParams();
        lotInfoInqParams.setUser(getUser());
        lotInfoInqParams.setLotIDs(lotIDs);
        lotInfoInqParams.setLotInfoInqFlag(new Infos.LotInfoInqFlag());
        lotInfoInqParams.getLotInfoInqFlag().setLotBackupInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotBasicInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotControlJobInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotControlUseInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotFlowBatchInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotListInCassetteInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotLocationInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotNoteFlagInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotOperationInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotOrderInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotProductInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotRecipeInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotWaferAttributesFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotWaferMapInCassetteInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotWipOperationInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotListInCassetteInfoFlag(true);
        lotInfoInqParams.getLotInfoInqFlag().setLotWaferMapInCassetteInfoFlag(true);

        Response response = lotInqController.lotInfoInq(lotInfoInqParams);
        Validations.isSuccessWithException(response);
        return response;
    }
    /**
     * description: check reason list
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/9/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/9/2 15:49
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    public Response getReasonListCase(String codeCategory) {
        Params.ReasonCodeListByCategoryInqParams  params = new Params.ReasonCodeListByCategoryInqParams();
        params.setUser(getUser());
        params.setCodeCategory(codeCategory);
        Response response = systemInqController.reasonCodeListByCategoryInq(params);
        Validations.isSuccessWithException(response);
        return response;
    }

    public Response getLotOperationSelectionInq(ObjectIdentifier lotID, boolean currentFlag, boolean posSearchFlag, boolean searchDirection){
        Params.LotOperationSelectionInqParams lotOperationSelectionInqParams = new Params.LotOperationSelectionInqParams();
        lotOperationSelectionInqParams.setLotID(lotID);
        lotOperationSelectionInqParams.setCurrentFlag(currentFlag);
        lotOperationSelectionInqParams.setPosSearchFlag(posSearchFlag);
        lotOperationSelectionInqParams.setSearchCount(9999);
        lotOperationSelectionInqParams.setUser(getUser());
        lotOperationSelectionInqParams.setSearchDirection(searchDirection);
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setPage(0);
        searchCondition.setSize(9999);
        searchCondition.setSortDirection(false);
        lotOperationSelectionInqParams.setSearchCondition(searchCondition);
        return lotInqController.LotOperationSelectionInq(lotOperationSelectionInqParams);
    }

    public Response getLotFamily(ObjectIdentifier lotID){
        Params.LotFamilyInqParams lotFamilyInqParams = new Params.LotFamilyInqParams();
        lotFamilyInqParams.setLotID(lotID);
        lotFamilyInqParams.setUser(getUser());
        return lotInqController.lotFamilyInq(lotFamilyInqParams);
    }

    public Infos.OperationNameAttributes getOperationAttributesByOperationNumber(List<Infos.OperationNameAttributes> operationNameAttributesList, String operationNum) {
        Infos.OperationNameAttributes  result = null;
        if (CimArrayUtils.getSize(operationNameAttributesList) == 0){
            return result;
        }
        for (Infos.OperationNameAttributes operationNameAttributes : operationNameAttributesList) {
            if (CimStringUtils.equals(operationNameAttributes.getOperationNumber(),operationNum)){
                result = operationNameAttributes;
            }
        }
        return result;
    }

    public Infos.OperationNameAttributes getOperationAttributesByOperationNumberAndRouteID(List<Infos.OperationNameAttributes> operationNameAttributesList, String operationNum,String routeID) {
        Infos.OperationNameAttributes  result = null;
        if (CimArrayUtils.getSize(operationNameAttributesList) == 0){
            return result;
        }
        for (Infos.OperationNameAttributes operationNameAttributes : operationNameAttributesList) {
            if (CimStringUtils.equals(operationNameAttributes.getOperationNumber(),operationNum) &&
                    CimObjectUtils.equalsWithValue(operationNameAttributes.getRouteID(),routeID)){
                result = operationNameAttributes;
            }
        }
        return result;
    }

    public Response waferListInLotFamilyInfoInqCase(ObjectIdentifier familyLotID){
        Params.WaferListInLotFamilyInqParams params = new Params.WaferListInLotFamilyInqParams();
        params.setLotFamilyID(familyLotID);
        params.setUser(getUser());
        return lotInqController.waferListInLotFamilyInq(params);
    }

    public List<Infos.LotInfo> getLotInfosByCassette(ObjectIdentifier cassetteID){
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(cassetteID).getBody();
        Infos.LotListInCassetteInfo lotListInCassetteInfo = lotListByCarrierInqResult.getLotListInCassetteInfo();
        if (lotListInCassetteInfo == null){
            return null;
        }
        List<Infos.LotInfo> lotInfoList = ((Results.LotInfoInqResult) this.getLotInfoCase(lotListInCassetteInfo.getLotIDList()).getBody()).getLotInfoList();
        return lotInfoList;
    }


    public List<Infos.AliasWaferName> waferAliasInfoInq(List<ObjectIdentifier> waferIDSeq){
        Params.WaferAliasInfoInqParams params = new Params.WaferAliasInfoInqParams();
        params.setUser(getUser());
        params.setWaferIDSeq(waferIDSeq);
        List<Infos.AliasWaferName> result = (List<Infos.AliasWaferName>) lotInqController.waferAliasInfoInq(params).getBody();
        return result;
    }

    public List<Infos.LotListAttributes> lotListInq(ObjectIdentifier lotID){
        Params.LotListInqParams lotListInqParams = new Params.LotListInqParams();
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setPage(1);
        searchCondition.setSize(9999);
        searchCondition.setSortDirection(false);
        lotListInqParams.setSearchCondition(searchCondition);
        lotListInqParams.setLotID(lotID);
        lotListInqParams.setUser(getUser());
        lotListInqParams.setDeleteLotFlag(false);
        Page<Infos.LotListAttributes> page = (Page<Infos.LotListAttributes>) lotInqController.lotListInq(lotListInqParams).getBody();
        return page.getContent();
    }
}
