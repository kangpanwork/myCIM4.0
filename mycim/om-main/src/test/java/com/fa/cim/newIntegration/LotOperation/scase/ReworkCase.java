package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/3          ********            lightyh                create file
 *
 * @author: light
 * @date: 2019/12/3 16:51
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class ReworkCase {

    @Autowired
    private CommonTestCase commonTestCase;

    @Autowired
    private ReworkTestCase reworkTestCase;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private OperationSkipCase operationSkipCase;

    @Autowired
    private LotHoldCase lotHoldCase;

    @Autowired
    private LotHoldTestCase lotHoldTestCase;

    @Autowired
    private FutureHoldTestCase futureHoldTestCase;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private EquipmentTestCase equipmentTestCase;

    @Autowired
    private LotMergeTestCase lotMergeTestCase;
    
    @Autowired
    private OperationSkipTestCase operationSkipTestCase;

    private static String REWORK_TEST_EQP = "WB201";

    private static String LOAD_PORT_ONE = "P1";

    private final static String reworkOpeNum = "2000.0320";


    public Response reworkWholeLotReq(Infos.LotInfo lotInfo){
        List<Infos.ReasonCodeAttributes> reasonCodeAttributesList = commonTestCase.getReasonCodeListByCategoryInq("Rework");
        List<Infos.ConnectedRouteList> connectedRouteList = commonTestCase.getConnectedRouteList(lotInfo.getLotBasicInfo().getLotID(), lotInfo.getLotLocationInfo().getCassetteID(), "Rework");
        Params.ReworkWholeLotReqParams reworkWholeLotReqParams = new Params.ReworkWholeLotReqParams();
        reworkWholeLotReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        reworkWholeLotReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        reworkWholeLotReqParams.setLotID(lotInfo.getLotBasicInfo().getLotID());
        reworkWholeLotReqParams.setReasonCodeID(reasonCodeAttributesList.get(0).getReasonCodeID());
        reworkWholeLotReqParams.setReturnOperationNumber(connectedRouteList.get(0).getReturnOperationNumber());
        reworkWholeLotReqParams.setSubRouteID(connectedRouteList.get(0).getRouteID());
        return reworkTestCase.reworkWholeLotReq(reworkWholeLotReqParams);
    }

    public Response reworkReq(Infos.LotInfo lotInfo,String returnPoint,Boolean dynamicFlag,Boolean forceReworkFlag){
        //【step1】get rework reason
        List<Infos.ReasonCodeAttributes> reworkReasonCodeAttributesList = commonTestCase.getReasonCodeListByCategoryInq("Rework");

        //【step2】get connected RouteList
        String returnOperationNumber = null;
        ObjectIdentifier subRouteID = null;
        if (CimBooleanUtils.isFalse(dynamicFlag)){
            List<Infos.ConnectedRouteList> connectedRouteList = commonTestCase.getConnectedRouteList(lotInfo.getLotBasicInfo().getLotID(), lotInfo.getLotLocationInfo().getCassetteID(), "Rework");
            returnOperationNumber = connectedRouteList.get(0).getReturnOperationNumber();
            subRouteID = connectedRouteList.get(0).getRouteID();
        }

        //【step3】check dynamicFlag is true or not
        if (CimBooleanUtils.isTrue(dynamicFlag) && CimStringUtils.isNotEmpty(returnPoint)){
            //【step3-1】get route operation list
            List<Infos.OperationNameAttributes> routeOperationList = (List<Infos.OperationNameAttributes>) commonTestCase.processFlowOperationListInq(lotInfo.getLotOperationInfo().getRouteID()).getBody();
            for (Infos.OperationNameAttributes operationNameAttributes : routeOperationList) {
                if (CimStringUtils.equals(returnPoint,operationNameAttributes.getOperationNumber())){
                    returnOperationNumber = operationNameAttributes.getOperationNumber();
                }
            }
            //【step3-2】get dynamic route list
            List<Infos.DynamicRouteList> dynamicRouteLists = (List<Infos.DynamicRouteList>) commonTestCase.dynamicPathListInqCase("Rework", "%").getBody();
            subRouteID = dynamicRouteLists.get(new Random().nextInt(CimArrayUtils.getSize(dynamicRouteLists))).getRouteID();
        }
        //【step4】rework req
        Params.ReworkReqParams params = new Params.ReworkReqParams();
        Infos.ReworkReq reworkReq = new Infos.ReworkReq();
        params.setReworkReq(reworkReq);
        reworkReq.setSiInfo("");
        reworkReq.setForceReworkFlag(forceReworkFlag);
        reworkReq.setSubRouteID(subRouteID);
        reworkReq.setReasonCodeID(reworkReasonCodeAttributesList.get(0).getReasonCodeID());
        reworkReq.setLotID(lotInfo.getLotBasicInfo().getLotID());
        reworkReq.setEventTxId("");
        reworkReq.setDynamicRouteFlag(dynamicFlag);
        reworkReq.setReturnOperationNumber(returnOperationNumber);
        reworkReq.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        reworkReq.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        return reworkTestCase.reworkReq(params);
    }

    public Response reworkWithHoldReleaseReq(Infos.LotInfo lotInfo, List<Infos.LotHoldReq> lotHoldReqList,String returnPoint,Boolean dynamicFlag,Boolean forceReworkFlag){
        //【step1】get lot hold release reason
        String releaseReasonCode = "ReleaseHoldLot";
        Response response1 =  lotGeneralTestCase.getReasonListCase(releaseReasonCode);
        List<Infos.ReasonCodeAttributes>  reasonCodeListByCategoryInqResult = (List<Infos.ReasonCodeAttributes>)response1.getBody();
        Infos.ReasonCodeAttributes releaseReasonCodeResult = reasonCodeListByCategoryInqResult.get(0);

        //【step2】get rework reason
        List<Infos.ReasonCodeAttributes> reworkReasonCodeAttributesList = commonTestCase.getReasonCodeListByCategoryInq("Rework");

        //【step3】get connected RouteList
        String returnOperationNumber = null;
        ObjectIdentifier subRouteID = null;
        if (CimBooleanUtils.isFalse(dynamicFlag)){
            List<Infos.ConnectedRouteList> connectedRouteList = commonTestCase.getConnectedRouteList(lotInfo.getLotBasicInfo().getLotID(), lotInfo.getLotLocationInfo().getCassetteID(), "Rework");
            returnOperationNumber = connectedRouteList.get(0).getReturnOperationNumber();
            subRouteID = connectedRouteList.get(0).getRouteID();
        }

        //【step3-1】check dynamicFlag is true or not
        if (CimBooleanUtils.isTrue(dynamicFlag) && CimStringUtils.isNotEmpty(returnPoint)){
            //【step3-2】get route operation list
            List<Infos.OperationNameAttributes> routeOperationList = (List<Infos.OperationNameAttributes>) commonTestCase.processFlowOperationListInq(lotInfo.getLotOperationInfo().getRouteID()).getBody();
            for (Infos.OperationNameAttributes operationNameAttributes : routeOperationList) {
                if (CimStringUtils.equals(returnPoint,operationNameAttributes.getOperationNumber())){
                    returnOperationNumber = operationNameAttributes.getOperationNumber();
                }
            }
            //【step3-3】get dynamic route list
            List<Infos.DynamicRouteList> dynamicRouteLists = (List<Infos.DynamicRouteList>) commonTestCase.dynamicPathListInqCase("Rework", "%").getBody();
            subRouteID = dynamicRouteLists.get(new Random().nextInt(CimArrayUtils.getSize(dynamicRouteLists))).getRouteID();
        }


        Params.ReworkWithHoldReleaseReqParams reworkWithHoldReleaseReqParams = new Params.ReworkWithHoldReleaseReqParams();
        reworkWithHoldReleaseReqParams.setHoldReqList(lotHoldReqList);
        reworkWithHoldReleaseReqParams.setReleaseReasonCodeID(releaseReasonCodeResult.getReasonCodeID());
        Infos.ReworkReq reworkReq = new Infos.ReworkReq();
        reworkWithHoldReleaseReqParams.setStrReworkReq(reworkReq);
        reworkReq.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        reworkReq.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        reworkReq.setReturnOperationNumber(returnOperationNumber);
        reworkReq.setDynamicRouteFlag(dynamicFlag);
        reworkReq.setEventTxId("");
        reworkReq.setLotID(lotInfo.getLotBasicInfo().getLotID());
        reworkReq.setReasonCodeID(reworkReasonCodeAttributesList.get(0).getReasonCodeID());
        reworkReq.setSubRouteID(subRouteID);
        reworkReq.setForceReworkFlag(forceReworkFlag);
        reworkReq.setSiInfo("");
        return reworkTestCase.reworkWithHoldReleaseReq(reworkWithHoldReleaseReqParams);
    }

    public Response partialReworkWithHoldReleaseReq(Infos.LotInfo lotInfo, List<Infos.LotHoldReq> lotHoldReqList,Boolean dynamicFlag,Boolean forceReworkFlag,String returnPoint,Integer waferSize,Integer position){
        //【step1】get lot hold release reason
        String releaseReasonCode = "ReleaseHoldLot";
        Response response1 =  lotGeneralTestCase.getReasonListCase(releaseReasonCode);
        List<Infos.ReasonCodeAttributes>  reasonCodeListByCategoryInqResult = (List<Infos.ReasonCodeAttributes>)response1.getBody();
        Infos.ReasonCodeAttributes releaseReasonCodeResult = reasonCodeListByCategoryInqResult.get(0);

        //【step2】get rework reason
        List<Infos.ReasonCodeAttributes> reworkReasonCodeAttributesList = commonTestCase.getReasonCodeListByCategoryInq("Rework");

        //【step3】get connected RouteList
        String returnOperationNumber = null;
        ObjectIdentifier subRouteID = null;
        if (CimBooleanUtils.isFalse(dynamicFlag)){
            List<Infos.ConnectedRouteList> connectedRouteList = commonTestCase.getConnectedRouteList(lotInfo.getLotBasicInfo().getLotID(), lotInfo.getLotLocationInfo().getCassetteID(), "Rework");
            returnOperationNumber = connectedRouteList.get(0).getReturnOperationNumber();
            subRouteID = connectedRouteList.get(0).getRouteID();
        }

        //【step3-1】check dynamicFlag is true or not
        if (CimBooleanUtils.isTrue(dynamicFlag) && CimStringUtils.isNotEmpty(returnPoint)){
            //【step3-2】get route operation list
            List<Infos.OperationNameAttributes> routeOperationList = (List<Infos.OperationNameAttributes>) commonTestCase.processFlowOperationListInq(lotInfo.getLotOperationInfo().getRouteID()).getBody();
            for (Infos.OperationNameAttributes operationNameAttributes : routeOperationList) {
                if (CimStringUtils.equals(returnPoint,operationNameAttributes.getOperationNumber())){
                    returnOperationNumber = operationNameAttributes.getOperationNumber();
                }
            }
            //【step3-3】get dynamic route list
            List<Infos.DynamicRouteList> dynamicRouteLists = (List<Infos.DynamicRouteList>) commonTestCase.dynamicPathListInqCase("Rework", "%").getBody();
            subRouteID = dynamicRouteLists.get(new Random().nextInt(CimArrayUtils.getSize(dynamicRouteLists))).getRouteID();
        }

        //【step4】get wafer alias name
        List<ObjectIdentifier> waferIDs = new ArrayList<>();
        List<ObjectIdentifier> choiceWaferIDs = new ArrayList<>();
        lotInfo.getLotWaferAttributesList().forEach(x->{
            waferIDs.add(x.getWaferID());
        });
        List<Infos.AliasWaferName> aliasWaferNames = lotGeneralTestCase.waferAliasInfoInq(waferIDs);
        //set wafer from wafer size
        for (Integer i = position; i < position + waferSize; i++) {
            choiceWaferIDs.add(aliasWaferNames.get(i).getWaferID());
        }
        Params.PartialReworkWithHoldReleaseReqParams params = new Params.PartialReworkWithHoldReleaseReqParams();
        params.setHoldReqList(lotHoldReqList);
        params.setReleaseReasonCodeID(releaseReasonCodeResult.getReasonCodeID());
        Infos.PartialReworkReq partialReworkReq = new Infos.PartialReworkReq();
        params.setPartialReworkReq(partialReworkReq);

        partialReworkReq.setBDynamicRoute(dynamicFlag);
        partialReworkReq.setBForceRework(forceReworkFlag);
        partialReworkReq.setChildWaferID(choiceWaferIDs);
        partialReworkReq.setEventTxId("");
        partialReworkReq.setParentLotID(lotInfo.getLotBasicInfo().getLotID());
        partialReworkReq.setReasonCodeID(reworkReasonCodeAttributesList.get(0).getReasonCodeID());
        partialReworkReq.setReturnOperationNumber(returnOperationNumber);
        partialReworkReq.setSubRouteID(subRouteID);

        return reworkTestCase.partialReworkWithHoldReleaseReq(params);
    }
    public Response partialReworkWithOutHoldReleaseReq(Infos.LotInfo lotInfo, List<Infos.LotHoldReq> lotHoldReqList,Boolean dynamicFlag,Boolean forceReworkFlag,String returnPoint,Integer waferSize,Integer position){
        //【step1】get lot hold release reason
        String releaseReasonCode = "ReleaseHoldLot";
        Response response1 =  lotGeneralTestCase.getReasonListCase(releaseReasonCode);
        List<Infos.ReasonCodeAttributes>  reasonCodeListByCategoryInqResult = (List<Infos.ReasonCodeAttributes>)response1.getBody();
        Infos.ReasonCodeAttributes releaseReasonCodeResult = reasonCodeListByCategoryInqResult.get(0);

        //【step2】get rework reason
        List<Infos.ReasonCodeAttributes> reworkReasonCodeAttributesList = commonTestCase.getReasonCodeListByCategoryInq("Rework");

        //【step3】get connected RouteList
        String returnOperationNumber = null;
        ObjectIdentifier subRouteID = null;
        if (CimBooleanUtils.isFalse(dynamicFlag)){
            List<Infos.ConnectedRouteList> connectedRouteList = commonTestCase.getConnectedRouteList(lotInfo.getLotBasicInfo().getLotID(), lotInfo.getLotLocationInfo().getCassetteID(), "Rework");
            returnOperationNumber = connectedRouteList.get(0).getReturnOperationNumber();
            subRouteID = connectedRouteList.get(0).getRouteID();
        }

        //【step3-1】check dynamicFlag is true or not
        if (CimBooleanUtils.isTrue(dynamicFlag) && CimStringUtils.isNotEmpty(returnPoint)){
            //【step3-2】get route operation list
            List<Infos.OperationNameAttributes> routeOperationList = (List<Infos.OperationNameAttributes>) commonTestCase.processFlowOperationListInq(lotInfo.getLotOperationInfo().getRouteID()).getBody();
            for (Infos.OperationNameAttributes operationNameAttributes : routeOperationList) {
                if (CimStringUtils.equals(returnPoint,operationNameAttributes.getOperationNumber())){
                    returnOperationNumber = operationNameAttributes.getOperationNumber();
                }
            }
            //【step3-3】get dynamic route list
            List<Infos.DynamicRouteList> dynamicRouteLists = (List<Infos.DynamicRouteList>) commonTestCase.dynamicPathListInqCase("Rework", "%").getBody();
            subRouteID = dynamicRouteLists.get(new Random().nextInt(CimArrayUtils.getSize(dynamicRouteLists))).getRouteID();
        }

        //【step4】get wafer alias name
        List<ObjectIdentifier> waferIDs = new ArrayList<>();
        List<ObjectIdentifier> choiceWaferIDs = new ArrayList<>();
        lotInfo.getLotWaferAttributesList().forEach(x->{
            waferIDs.add(x.getWaferID());
        });
        List<Infos.AliasWaferName> aliasWaferNames = lotGeneralTestCase.waferAliasInfoInq(waferIDs);
        //set wafer from wafer size
        for (Integer i = position; i < waferSize + position; i++) {
            choiceWaferIDs.add(aliasWaferNames.get(i).getWaferID());
        }
        Params.PartialReworkWithoutHoldReleaseReqParams params = new Params.PartialReworkWithoutHoldReleaseReqParams();
        params.setHoldReqList(lotHoldReqList);
        Infos.PartialReworkReq partialReworkReq = new Infos.PartialReworkReq();
        params.setPartialReworkReq(partialReworkReq);

        partialReworkReq.setBDynamicRoute(dynamicFlag);
        partialReworkReq.setBForceRework(forceReworkFlag);
        partialReworkReq.setChildWaferID(choiceWaferIDs);
        partialReworkReq.setEventTxId("");
        partialReworkReq.setParentLotID(lotInfo.getLotBasicInfo().getLotID());
        partialReworkReq.setReasonCodeID(reworkReasonCodeAttributesList.get(0).getReasonCodeID());
        partialReworkReq.setReturnOperationNumber(returnOperationNumber);
        partialReworkReq.setSubRouteID(subRouteID);

        return reworkTestCase.partialReworkWithOutHoldReleaseReq(params);
    }



    public void Basic_Flow_Of_All_Rework() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】next flow not in rework flow and click rework button
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        List<Infos.ReasonCodeAttributes> reasonCodeAttributesList = commonTestCase.getReasonCodeListByCategoryInq("Rework");
        List<Infos.ConnectedRouteList> connectedRouteList = commonTestCase.getConnectedRouteList(lotInfo.getLotBasicInfo().getLotID(), lotInfo.getLotLocationInfo().getCassetteID(), "Rework");

        //【ste3】check next flow is not rework flow
        Validations.assertCheck(CimArrayUtils.getSize(connectedRouteList) == 0,"this is a rework flow");

        //【step4】skip to 2000.0320
        List<Infos.LotInfo> lotInfoList2 = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), reworkOpeNum, true);

        //【step5】When Future Hold is set later than rework post single
        String futureHoldOperationNum = "3000.0300";
        Infos.LotInfo lotInfo2 = this.registerFutureHoldWithParameter(lotID, futureHoldOperationNum, true, true);

        //【step6】rework the whole lot at tne rework operationNum
        this.reworkWholeLotReq(lotInfoList2.get(0));

        //【ste7】check lot production state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotStatusList lotStatusList1 = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatusList().stream().filter(lotStatusList -> lotStatusList.getStateName().equals(BizConstant.SP_LOTSTATECAT_PRODUCTIONSTATE)).findFirst().orElse(null);
        String stateValue = null;
        if (lotStatusList1 != null) {
            stateValue = lotStatusList1.getStateValue();
        }
        Validations.assertCheck(CimStringUtils.equals(BizConstant.SP_LOT_PRODCTN_STATE_INREWORK,stateValue),"rework success the lot production state must be in rework");
    }

    public void Rework_With_Hold_Release() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】lot hold on current operation number
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);

        //【step3】click rework button
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);

        //【step3-1】get hold list
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);

        //【step3-2】rework with hold release
        this.reworkWithHoldReleaseReq(lotInfo,lotHoldReqList,null,false,false);

        //【step4】check lot hold state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Validations.assertCheck(!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus()),"the lot hold state can not be onHold");
    }

    private List<Infos.LotHoldReq> getLotHoldListAndReason(Infos.LotInfo lotInfo) {
        Infos.LotOperationInfo lotOperationInfo = lotInfo.getLotOperationInfo();
        ObjectIdentifier lotID = lotInfo.getLotBasicInfo().getLotID();
        ObjectIdentifier routeID = lotOperationInfo.getRouteID();
        String operationNumber = lotOperationInfo.getOperationNumber();

        //【step1】call holdLotListInqCase function and then choose one Hold Data
        Results.HoldLotListInqResult holdLotListInqResult = (Results.HoldLotListInqResult)lotHoldTestCase.getLotHoldListCase(lotID).getBody();
        List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
        if (null != holdLotListInqResult.getLotHoldListAttributes()){
            if (CimArrayUtils.isNotEmpty(holdLotListInqResult.getLotHoldListAttributes().getContent())){
                for (Infos.LotHoldListAttributes lotHoldListAttributes : holdLotListInqResult.getLotHoldListAttributes().getContent()) {
                    Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                    lotHoldReq.setHoldReasonCodeID(lotHoldListAttributes.getReasonCodeID());
                    lotHoldReq.setHoldType(lotHoldListAttributes.getHoldType());
                    lotHoldReq.setHoldUserID(testUtils.getUser().getUserID());
                    lotHoldReq.setOperationNumber(lotHoldListAttributes.getResponsibleOperationNumber());
                    lotHoldReq.setResponsibleOperationMark(lotHoldListAttributes.getResponsibleOperationMark());
                    lotHoldReq.setRelatedLotID(lotHoldListAttributes.getRelatedLotID());
                    lotHoldReq.setRouteID(routeID);
                    holdReqList.add(lotHoldReq);
                }
            }
        }
        return holdReqList;
    }

    public void When_FutureHold_Is_Set_On_A_Process_Between_The_Original_Process_And_The_ReturnPoint_Post() {

        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String reworkOpeNum = "3000.0200";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】register a futureHold  post ans single
        String futureHoldOperationNum = "3000.0200";
        Infos.LotInfo lotInfo = this.registerFutureHoldWithParameter(lotID, futureHoldOperationNum, true, true);

        //【step3】rework on current operationNum
        try {
            this.reworkWholeLotReq(lotInfo);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getFutureHoldInBranch(),e.getCode()),"test fail");
        }
    }

    private Infos.LotInfo registerFutureHoldWithParameter(ObjectIdentifier lotID, String futureHoldOperationNum, boolean postFlag, boolean singleTriggerFlag) {
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotGeneralTestCase.getOperationAttributesByOperationNumber(lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent(),futureHoldOperationNum);

        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();
        futureHoldTestCase.futureHoldRegisterBySpecLot(lotID, operationNumber, routeID, postFlag,singleTriggerFlag);

        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        return lotInfoCase.getLotInfoList().get(0);
    }

    public void When_FutureHold_Is_Set_On_A_Process_Between_The_Original_Process_And_The_ReturnPoint_Pre_Multiple() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "3000.0100";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        //【step2】register a futureHold  pre and multiple
        String futureHoldOperationNum = "3000.0200";
        Infos.LotInfo lotInfo = this.registerFutureHoldWithParameter(lotID, futureHoldOperationNum, false, false);

        //【step3】skip to 3000.0200
        List<Infos.LotInfo> lotInfoList2 = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), futureHoldOperationNum, true);
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfoList2.get(0).getLotBasicInfo().getLotStatus()),"the lot must be onHold");

        //【step3】rework with hold release
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfoList2.get(0));
        this.reworkWithHoldReleaseReq(lotInfoList2.get(0),lotHoldReqList,null,false,false);

        //【step4】check lot hold state
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Validations.assertCheck(!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus()),"the lot hold state can not be onHold");

        //【step5】skip to 3000.0300 and then to 3000.0200
        String reworkNextOpeNum = "3000.0300";
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,futureHoldOperationNum);

        //【step6】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,futureHoldOperationNum);

        //【step7】loop the las flow
        try {
            this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,futureHoldOperationNum);
            Assert.isTrue(false,"test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getReachMaxRework(),e.getCode()),"test fail");
        }
    }

    private void reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(ObjectIdentifier lotID, String reworkNextOpeNum, String reworkOpeNum) {
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        String currentOperationNumber = lotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getOperationNumber();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);

        if (!CimStringUtils.equals(reworkNextOpeNum,reworkOpeNum)){
            //【step1】skip to rework next operation
            List<Infos.LotInfo> lotInfos = new ArrayList<>();
            if (!CimStringUtils.equals(currentOperationNumber,reworkNextOpeNum)){
                lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), reworkNextOpeNum, true);
            }
            //【step2】back to rework operation
            lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), reworkOpeNum, false);
            lotInfo = lotInfos.get(0);
        }

        //【step3】rework with hold release or rework
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);
        if (CimArrayUtils.isNotEmpty(lotHoldReqList)){
            this.reworkWithHoldReleaseReq(lotInfo,lotHoldReqList,null,false,false);
        }else {
            this.reworkWholeLotReq(lotInfo);
            //【step3-1】full rework done skip back to rework operation when 3000.0200
            lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
            currentOperationNumber = lotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getOperationNumber();
            if (!CimStringUtils.equals(reworkNextOpeNum,reworkOpeNum)){
                //【step3-2】skip to rework next operation
                List<Infos.LotInfo> lotInfos = new ArrayList<>();
                if (!CimStringUtils.equals(currentOperationNumber,reworkNextOpeNum)){
                    lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), reworkNextOpeNum, true);
                }
                //【step3-3】back to rework operation
                lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), reworkOpeNum, false);
                lotInfo = lotInfos.get(0);
            }else {
                //【step3-4】normal skip to rework flow
                operationSkipCase.skipSpecificStep(Arrays.asList(lotID),reworkOpeNum,true);
            }
        }
    }

    public void When_FutureHold_Is_Set_On_A_Process_Between_The_Original_Process_And_The_ReturnPoint_Pre_Single() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "3000.0100";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        //【step2】register a futureHold  post ans single
        String futureHoldOperationNum = "3000.0200";
        Infos.LotInfo lotInfo = this.registerFutureHoldWithParameter(lotID, futureHoldOperationNum, false, true);

        //【step3】skip to rework operation number
        List<Infos.LotInfo> lotInfoList2 = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), futureHoldOperationNum, true);
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfoList2.get(0).getLotBasicInfo().getLotStatus()),"the lot must be onHold");

        //【step4】rework with hold release
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfoList2.get(0));
        this.reworkWithHoldReleaseReq(lotInfoList2.get(0),lotHoldReqList,null,false,false);

        //【step4】check lot hold state
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Validations.assertCheck(!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus()),"the lot hold state can not be onHold");
    }

    public void All_Rework_Cancel() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】rework the lot at rework operation
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        this.reworkWholeLotReq(lotInfo);

        //【step3】skip to the next operation on rework flow
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        List<Infos.LotInfo> lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), "1000.0200", true);

        //【step4】rework cancel
        try {
            this.reworkCancelReq(lotInfos.get(0));
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getAllAlreadyProcessed(),e.getCode()),"test fail");
        }

        //【step5】skip back to 1000.0100
        List<Infos.LotInfo> lotInfos1 = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), "1000.0100", false);

        //【step6】rework cancel
        this.reworkCancelReq(lotInfos1.get(0));
    }

    private void reworkCancelReq(Infos.LotInfo lotInfo) {
        List<Infos.ReasonCodeAttributes> reasonCodeAttributesList = commonTestCase.getReasonCodeListByCategoryInq("CancelRework");
        Params.ReworkCancelReqParams params = new Params.ReworkCancelReqParams();
        params.setClaimMemo("");
        params.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        params.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        params.setLotID(lotInfo.getLotBasicInfo().getLotID());
        params.setReasonCodeID(reasonCodeAttributesList.get(0).getReasonCodeID());
        reworkTestCase.reworkCancelReq(params);
    }

    private void partialReworkCancelReq(ObjectIdentifier childLotID,ObjectIdentifier parentLotID) {
        List<Infos.ReasonCodeAttributes> reasonCodeAttributesList = commonTestCase.getReasonCodeListByCategoryInq("CancelRework");
        Params.PartialReworkCancelReqParams params = new Params.PartialReworkCancelReqParams();
        params.setChildLotID(childLotID);
        params.setParentLotID(parentLotID);
        params.setReasonCodeID(reasonCodeAttributesList.get(0).getReasonCodeID());
        reworkTestCase.partialReworkCancelReq(params);
    }

    public void Rework_Cancel_With_Processing_Status() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "2000.0100";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        ObjectIdentifier cassetteID = lotInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step2】step to eqp WB201
        ObjectIdentifier equipmentID = new ObjectIdentifier(REWORK_TEST_EQP);
        electronicInformationTestCase.eqpInfoInqCase(equipmentID);

        //【step3】load purpose productLot
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.carrierLoadingRpt(equipmentID,cassetteID,new ObjectIdentifier(LOAD_PORT_ONE) , purposeList.get(0));

        //【step4】move in
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(cassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(equipmentID, cassetteIDs).getBody();
        Results.MoveInReqResult moveInResult = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList()).getBody();

        //【step5】back to lot Operation
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();

        //【step6】rework cancel
        try {
            this.reworkCancelReq(lotInfoCase.getLotInfoList().get(0));
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteTransferState(),e.getCode()),"test fail");
        }
    }

    public void Rework_Cancel_With_Future_Hold_Between_Current_Operation_And_Future_Operation() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】rework the lot at rework operation
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        this.reworkWholeLotReq(lotInfo);

        //【step3】register a future hold at 1000.0200 post single
        Infos.LotInfo lotInfo1 = this.registerFutureHoldWithParameter(lotID, "1000.0200", true, true);

        //【step4】rework cancel
        try {
            this.reworkCancelReq(lotInfo1);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getFutureholdOnBranch(),e.getCode()),"test fail");
        }
    }

    public void partialReworkReq(Infos.LotInfo lotInfo,Boolean bDynamicRoute,Boolean bForceRework,String returnPoint,Integer waferSize,Integer position){
        //【step1】get rework reason
        List<Infos.ReasonCodeAttributes> reworkReasonCodeAttributesList = commonTestCase.getReasonCodeListByCategoryInq("Rework");

        //【step2】get connected RouteList get normal return operationMum and routeID
        String returnOperationNumber = null;
        ObjectIdentifier subRouteID = null;
        if (CimBooleanUtils.isFalse(bDynamicRoute)){
            List<Infos.ConnectedRouteList> connectedRouteList = commonTestCase.getConnectedRouteList(lotInfo.getLotBasicInfo().getLotID(), lotInfo.getLotLocationInfo().getCassetteID(), "Rework");
            returnOperationNumber = connectedRouteList.get(0).getReturnOperationNumber();
            subRouteID = connectedRouteList.get(0).getRouteID();
        }

        //【step2-1】check dynamicFlag is true or not
        if (CimBooleanUtils.isTrue(bDynamicRoute) && CimStringUtils.isNotEmpty(returnPoint)){
            //【step2-2】get route operation list
            List<Infos.OperationNameAttributes> routeOperationList = (List<Infos.OperationNameAttributes>) commonTestCase.processFlowOperationListInq(lotInfo.getLotOperationInfo().getRouteID()).getBody();
            for (Infos.OperationNameAttributes operationNameAttributes : routeOperationList) {
                if (CimStringUtils.equals(returnPoint,operationNameAttributes.getOperationNumber())){
                    returnOperationNumber = operationNameAttributes.getOperationNumber();
                }
            }
            //【step2-3】get dynamic route list
            List<Infos.DynamicRouteList> dynamicRouteLists = (List<Infos.DynamicRouteList>) commonTestCase.dynamicPathListInqCase("Rework", "%").getBody();
            subRouteID = dynamicRouteLists.get(new Random().nextInt(CimArrayUtils.getSize(dynamicRouteLists))).getRouteID();
        }

        //【step3】get wafer alias name
        List<ObjectIdentifier> waferIDs = new ArrayList<>();
        List<ObjectIdentifier> choiceWaferIDs = new ArrayList<>();
        lotInfo.getLotWaferAttributesList().forEach(x->{
            waferIDs.add(x.getWaferID());
        });
        List<Infos.AliasWaferName> aliasWaferNames = lotGeneralTestCase.waferAliasInfoInq(waferIDs);
        //set wafer1 and wafer2
        for (Integer i = position; i < (waferSize + position); i++) {
            choiceWaferIDs.add(aliasWaferNames.get(i).getWaferID());
        }
        Params.PartialReworkReqParams params = new Params.PartialReworkReqParams();
        Infos.PartialReworkReq partialReworkReq = new Infos.PartialReworkReq();
        params.setPartialReworkReqInformation(partialReworkReq);

        partialReworkReq.setBDynamicRoute(bDynamicRoute);
        partialReworkReq.setBForceRework(bForceRework);
        partialReworkReq.setChildWaferID(choiceWaferIDs);
        partialReworkReq.setEventTxId("");
        partialReworkReq.setParentLotID(lotInfo.getLotBasicInfo().getLotID());
        partialReworkReq.setReasonCodeID(reworkReasonCodeAttributesList.get(0).getReasonCodeID());
        partialReworkReq.setReturnOperationNumber(returnOperationNumber);
        partialReworkReq.setSubRouteID(subRouteID);

        //【step4】partial rework
        reworkTestCase.partialReworkReq(params);
    }

    public void Rework_Cancel_With_Future_Hold_In_Current_Step() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "3000.0100";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        //【step2】register a futureHold  pre and multiple
        String futureHoldOperationNum = "3000.0200";
        Infos.LotInfo lotInfo = this.registerFutureHoldWithParameter(lotID, futureHoldOperationNum, false, false);

        //【step3】skip to 3000.0200
        List<Infos.LotInfo> lotInfoList2 = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), futureHoldOperationNum, true);
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfoList2.get(0).getLotBasicInfo().getLotStatus()),"the lot must be onHold");

        //【step3】rework with hold release
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfoList2.get(0));
        this.reworkWithHoldReleaseReq(lotInfoList2.get(0),lotHoldReqList,null,false,false);

        //【step4】rework cancel and check hold state
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        this.reworkCancelReq(lotInfoCase.getLotInfoList().get(0));

        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus()),"the lot hold state must be onHold");
    }

    public void Partial_Rework_Normal_Case() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】partial rework
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        this.partialReworkReq(lotInfoCase.getLotInfoList().get(0),false,false,null,2,0);

        //【step3】check parent lot is onHold and split a child lot
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        String lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot");
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"parent lot must be onHold");

        //【step4】skip the child lot to skipOpeNum
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        lotID = lotIDList.get(0);
        ObjectIdentifier childLotID = lotIDList.get(1);
        Results.LotInfoInqResult lotInfoCase1 = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(childLotID)).getBody();
        operationSkipCase.skipSpecificStep(Arrays.asList(childLotID),reworkOpeNum,true);
        lotInfoCase1 = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(childLotID)).getBody();
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfoCase1.getLotInfoList().get(0).getLotBasicInfo().getLotStatus()),"parent lot must be onHold");

        //【step5】merge the parent lot and child lot
        lotMergeTestCase.mergeLotReq(lotID,childLotID);
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(lotInfoCase.getLotListInCassetteInfo().getLotIDList()) == 1,"merge done must have 1 lot id");
    }

    public void Perform_Partial_Rework_And_Skip_Parent_And_Child_Lot_To_Step_After_ReturnPoint() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】partial rework
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        this.partialReworkReq(lotInfoCase.getLotInfoList().get(0),false,false,null,2,0);

        //【step3】check parent lot is onHold and split a child lot
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        String lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot");
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"parent lot must be onHold");

        //【step4】skip the child lot to after skipOpeNum
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        lotID = lotIDList.get(0);
        ObjectIdentifier childLotID = lotIDList.get(1);
        String afterSkipOpeNum = "2000.0400";
        try {
            operationSkipCase.skipSpecificStep(Arrays.asList(childLotID),afterSkipOpeNum,true);
            Assert.isTrue(false,"test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotFutureholdInLocate(),e.getCode()),"child lot can not skip to after opeNum");
        }
        
        //【step5】skip parent lot to after skipOpeNum success
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList,afterSkipOpeNum);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.forceOperationSkip(skipReqParams);
    }

    public void Partial_Rework_With_HoldRelease() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】lot hold the parent lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfo.getLotBasicInfo().getLotStatus()),"parent hold lot must be on hold");


        //【step3】click rework button
        //【step3-1】get hold list
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);

        //【step3-2】partial rework with hold release
        this.partialReworkWithHoldReleaseReq(lotInfo,lotHoldReqList,false,false,null,2,0);

        //【step4】check parent lot
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        String lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot");
        lotHoldReqList = this.getLotHoldListAndReason(lotInfoCase.getLotInfoList().get(0));
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus) && CimArrayUtils.getSize(lotHoldReqList) == 1,"parent hold lot must be hold release but have a rework hold");

        //【step5】get child lot and partial rework cancel
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        lotID = lotIDList.get(0);
        ObjectIdentifier childLotID = lotIDList.get(1);
        Results.LotInfoInqResult lotInfoCase1 = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(childLotID)).getBody();
        //【step5-1】child lot skip to 1000.0200
        operationSkipCase.skipSpecificStep(Arrays.asList(childLotID),"1000.0200",true);
        this.partialReworkCancelReq(childLotID,lotID);

        //【step6】check parent lot
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 1,"parent lot must merge");
        Validations.assertCheck(!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"parent lot must be hold release");
    }

    public void Partial_Rework_WithOut_HoldRelease() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】lot hold the parent lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfo.getLotBasicInfo().getLotStatus()),"parent hold lot must be on hold");

        //【step3】click rework button
        //【step3-1】get hold list
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);

        //【step3-2】partial rework with hold release
        this.partialReworkWithOutHoldReleaseReq(lotInfo,lotHoldReqList,false,false,null,2,0);

        //【step4】check parent lot
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        String lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot");
        lotHoldReqList = this.getLotHoldListAndReason(lotInfoCase.getLotInfoList().get(0));
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus) && CimArrayUtils.getSize(lotHoldReqList) == 2,"parent hold lot can not be hold release and have 2 lot hold");

        //【step5】check child lot extend a parent lot hold
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        ObjectIdentifier childLotID = lotIDList.get(1);
        Results.LotInfoInqResult childLotInfoCase = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(childLotID)).getBody();
        String childLotStatus = childLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        List<Infos.LotHoldReq> childLotHoldReqList = this.getLotHoldListAndReason(childLotInfoCase.getLotInfoList().get(0));
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,childLotStatus) && CimArrayUtils.getSize(childLotHoldReqList) == 1,"child lot must be onHold and extend 1 parent lot hold");

        //【step5】get child lot and partial rework cancel
        //【step5-1】child lot skip to 1000.0200 force
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(childLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList,"1000.0200");

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(childLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(childLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(childLotID);
        skipReqParams.setOperationID(operationNameAttributes.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.forceOperationSkip(skipReqParams);

        this.partialReworkCancelReq(childLotID,lotID);

        //【step6】check parent lot
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 1,"parent lot must merge");
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"parent lot must be on hold");
    }

    public void Partial_Rework_At_FirstStep_Of_The_Route() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】lot info
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();

        //【step3】at first step dynamic partial rework
        String returnPoint = "1000.0200";
        try {
            this.partialReworkReq(lotInfoCase.getLotInfoList().get(0),true,true,returnPoint,2,0);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidReworkOperation(),e.getCode()),"first process can't exec partial rework");
        }
    }

    public void Do_Dynamic_Partial_Rework_Again_After_Already_Did_Partial_Rework_ParentLot() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】partial rework
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        this.partialReworkReq(lotInfoCase.getLotInfoList().get(0),false,false,null,2,0);

        //【step3】check parent lot is onHold and split a child lot
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        String lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot");
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"parent lot must be onHold");

        //【step4】dynamic partial rework again and at current operaiton
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);
        this.partialReworkWithHoldReleaseReq(lotInfo,lotHoldReqList,true,true,reworkOpeNum,2,2);

        //【step5】check lot info and parent hold state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        List<ObjectIdentifier> lotIDList1 = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList1) == 3,"parent lot split 2 child lot");
        lotHoldReqList = this.getLotHoldListAndReason(lotInfoCase.getLotInfoList().get(0));
        Validations.assertCheck(CimArrayUtils.getSize(lotHoldReqList) == 1,"parent lot hold release and then hold with dynamic partial rework");
    }

    public void Partial_Rework_Cancel_Normal() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】partial rework
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        this.partialReworkReq(lotInfoCase.getLotInfoList().get(0),false,false,null,2,0);

        //【step3】check parent lot is onHold and split a child lot
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        String lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot");
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"parent lot must be onHold");

        //【step4】step parital rework cancel for child lot
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        lotID = lotIDList.get(0);
        ObjectIdentifier childLotID = lotIDList.get(1);
        Results.LotInfoInqResult lotInfoCase1 = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(childLotID)).getBody();
        this.partialReworkCancelReq(childLotID,lotID);

        //【step5】check parent lot
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 1,"parent lot must merge");
        Validations.assertCheck(!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"parent lot must be hold release");
    }

    public void Reach_Max_Rework_Count_For_Partial_Rework_Case1() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】partial rework for reach the max rework count
        String reworkNextOpeNum = "2000.0320";
        this.reachTheMaxPartialReworkCountForFullReworkOnChoiceOpeNum(lotID,reworkNextOpeNum,reworkOpeNum);
    }

    private void reachTheMaxPartialReworkCountForFullReworkOnChoiceOpeNum(ObjectIdentifier lotID, String reworkNextOpeNum, String reworkOpeNum) {
        //【step1】loop the last flow
        this.partialReworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,reworkOpeNum,2);

        //【step2】loop the last flow
        this.partialReworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,reworkOpeNum,2);

        //【step3】loop the last flow
        this.partialReworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,reworkOpeNum,2);

        //【step4】loop the las flow
        try {
            this.partialReworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,reworkOpeNum,2);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getReachMaxRework(),e.getCode()),"test fail");
        }
    }

    private void partialReworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(ObjectIdentifier lotID, String reworkNextOpeNum, String reworkOpeNum,Integer position) {
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        String currentOperationNumber = lotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getOperationNumber();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        if (!CimStringUtils.equals(reworkNextOpeNum,reworkOpeNum)){
            //【step1】skip to rework next operation
            List<Infos.LotInfo> lotInfos = new ArrayList<>();
            if (!CimStringUtils.equals(currentOperationNumber,reworkNextOpeNum)){
                lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), reworkNextOpeNum, true);
            }
            //【step2】back to rework operation
            lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), reworkOpeNum, false);
            lotInfo = lotInfos.get(0);
        }
        //【step3】partial rework
        this.partialReworkReq(lotInfo,false,false,null,2,position);

        //【step4】lot info and release the parent lot
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        String parentLotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        ObjectIdentifier childLotID = lotIDList.get(1);

        //【step5】skip the child and check child lot onHold
        List<Infos.LotInfo> childLotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(childLotID), reworkNextOpeNum, true);
        String childLotStatus = childLotInfos.get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,childLotStatus),"child lot must be on hold");

        //【step6】check parent lot onHold
        if (!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,parentLotStatus)){
            //when 3000.0200,parent lot can not be onHold so skip to reworkNextOpeNum
            List<Infos.LotInfo> parentLotInfosNew = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), reworkNextOpeNum, true);
            parentLotStatus = parentLotInfosNew.get(0).getLotBasicInfo().getLotStatus();
        }
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,parentLotStatus),"parent lot must be on hold");

        //【step7】when parent lot and child lot are both onHold,the merge them
        lotMergeTestCase.mergeLotReq(lotID,childLotID);
    }

    public void Reach_Max_Rework_Count_For_Partial_Rework_Case2() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "3000.0200";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        //【step2】partial rework for reach the max rework count
        String reworkNextOpeNum = "3000.0300";
        this.reachTheMaxPartialReworkCountForFullReworkOnChoiceOpeNum(lotID,reworkNextOpeNum,skipOpeNum);
    }

    public void Reach_Max_Rework_Count_For_Partial_Rework_Case3() {
        // 【step1】stb product 1.01
        ObjectIdentifier lotID = stbCase.STB_PRODUCT101(false);
        // 【step2】skip to the 6000.0100
        String skipOperaionNum = "6000.0100";
        List<Infos.LotInfo> lotInfoList = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), skipOperaionNum, true);

        //【step3】partial rework for reach the max rework count
        String reworkNextOpeNum = "6000.0100";
        this.reachTheMaxPartialReworkCountForFullReworkOnChoiceOpeNum(lotID,reworkNextOpeNum,reworkNextOpeNum);
    }

    public void Reach_Max_Rework_Count_For_Full_Rework_Case1() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        String reworkNextOpeNum = "2000.0350";
        this.reachTheMaxReworkCountForFullReworkOnChoiceOpeNum(lotID,reworkNextOpeNum,reworkOpeNum);
    }

    public void Reach_Max_Rework_Count_For_Full_Rework_Case2() {
        //【step1】STB a product 1.01  lot 25 wafer
        // 【step1】stb product 1.01
        ObjectIdentifier lotID = stbCase.STB_PRODUCT101(false);
        // 【step2】skip to the 6000.0100
        String skipOperaionNum = "6000.0100";
        List<Infos.LotInfo> lotInfoList = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), skipOperaionNum, true);
        //【step3】partial rework for reach the max rework count
        String reworkNextOpeNum = "6000.0100";
        this.reachTheMaxReworkCountForFullReworkOnChoiceOpeNum(lotID,reworkNextOpeNum,reworkNextOpeNum);
    }

    private void reachTheMaxReworkCountForFullReworkOnChoiceOpeNum(ObjectIdentifier lotID,String reworkNextOpeNum,String reworkOpeNum) {

        //【step1】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,reworkOpeNum);

        //【step2】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,reworkOpeNum);

        //【step3】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,reworkOpeNum);

        //【step4】loop the las flow
        this.reachTheMaxReworkCountForPartialReworkOrFullRework(lotID,reworkNextOpeNum,reworkOpeNum);
    }

    public void Complex_Reach_Max_Rework_Count_For_Partial_Rework_Twice_And_Then_FUll_Rework1() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        String reworkNextOpeNum = "2000.0320";
        //【step2】complex reach the max rework count
        this.reachTheMaxReworkCountForComplexReworkOnChoiceOpeNum(lotID,reworkNextOpeNum,reworkOpeNum);
    }

    private void reachTheMaxReworkCountForComplexReworkOnChoiceOpeNum(ObjectIdentifier lotID, String reworkNextOpeNum, String skipOpeNum) {
        //【step1】partial rework
        this.partialReworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum,2);

        //【step2】loop the last flow
        this.partialReworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum,2);

        //【step3】full rework
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step5】reach the max rework count for partial rework or full rework
        this.reachTheMaxReworkCountForPartialReworkOrFullRework(lotID,reworkNextOpeNum,skipOpeNum);
    }

    private void reachTheMaxReworkCountForPartialReworkOrFullRework(ObjectIdentifier lotID, String reworkNextOpeNum, String skipOpeNum) {
        Integer random = new Random().nextInt(2);
        switch (random){
            case 0:{
                try {
                    this.partialReworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum,2);
                    Assert.isTrue(false,"test error");
                } catch (ServiceException e) {
                    Assert.isTrue(Validations.isEquals(retCodeConfig.getReachMaxRework(),e.getCode()),"test fail");
                    break;
                }
            }
            case 1:{
                try {
                    this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);
                    Assert.isTrue(false,"test error");
                } catch (ServiceException e) {
                    Assert.isTrue(Validations.isEquals(retCodeConfig.getReachMaxRework(),e.getCode()),"test fail");
                    break;
                }
            }
        }
    }

    public void Complex_Reach_Max_Rework_Count_For_Partial_Rework_Twice_And_Then_FUll_Rework2() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "3000.0200";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        String reworkNextOpeNum = "3000.0300";
        this.reachTheMaxReworkCountForComplexReworkOnChoiceOpeNum(lotID,reworkNextOpeNum,skipOpeNum);
    }
    public void Complex_Reach_Max_Rework_Count_For_Partial_Rework_Twice_And_Then_FUll_Rework3() {
        // 【step1】stb product 1.01
        ObjectIdentifier lotID = stbCase.STB_PRODUCT101(false);
        // 【step2】skip to the 6000.0100
        String skipOperaionNum = "6000.0100";
        List<Infos.LotInfo> lotInfoList = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), skipOperaionNum, true);
        //【step3】partial rework for reach the max rework count
        String reworkNextOpeNum = "6000.0100";
        this.reachTheMaxReworkCountForComplexReworkOnChoiceOpeNum(lotID,reworkNextOpeNum,reworkNextOpeNum);
    }

    public void Force_Rework_For_Full_Rework_Or_Partial_Rework_At_Process_Count() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "2000.0100";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        //【step2】step to REWORK_TEST_EQP WB201 to  ( load -> move in - move out - unload ) loop 3 times
        for (int i = 0; i < 4; i++) {
            this.loadMoveInMoveOutUnloadSkip(lotID,skipOpeNum);
        }

        String reworkNextOpeNum = "2000.0320";

        //【step3】skip to reworkOpeNum
        operationSkipCase.skipSpecificStep(Arrays.asList(lotID),reworkOpeNum,true);

        //【step4】complex reach the max rework count
        this.reachTheMaxReworkCountForPartialReworkOrFullRework(lotID,reworkNextOpeNum,reworkOpeNum);
    }

    private void loadMoveInMoveOutUnloadSkip(ObjectIdentifier lotID, String skipOpeNum) {
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        ObjectIdentifier cassetteID = lotInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step1】step to eqp WB201
        ObjectIdentifier equipmentID = new ObjectIdentifier(REWORK_TEST_EQP);
        electronicInformationTestCase.eqpInfoInqCase(equipmentID);

        //【step2】load purpose productLot
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.carrierLoadingRpt(equipmentID,cassetteID,new ObjectIdentifier(LOAD_PORT_ONE) , purposeList.get(0));

        //【step3】move in
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(cassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(equipmentID, cassetteIDs).getBody();
        Results.MoveInReqResult moveInResult = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList()).getBody();

        //【step4】move out
        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInResult.getControlJobID());
        opeComWithDataReqParams.setUser(testUtils.getUser());
        opeComWithDataReqParams.setEquipmentID(equipmentID);
        equipmentTestCase.moveOut(opeComWithDataReqParams);

        //【step5】unLoad to the eqp
        equipmentTestCase.uncarrierLoadingRpt(equipmentID, cassetteID, new ObjectIdentifier(LOAD_PORT_ONE), purposeList.get(0));

        //【step6】step back to skipOpeNum
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        operationSkipCase.skipSpecificStep(Arrays.asList(lotID),skipOpeNum,false);
    }

    public void Partial_Rework_WithOut_Hold_Release_All_Partial_Rework() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】lot hold the parent lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfo.getLotBasicInfo().getLotStatus()),"parent hold lot must be on hold");

        //【step3】click rework button
        //【step3-1】get hold list
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);

        //【step3-2】partial rework with out hold release
        this.partialReworkWithOutHoldReleaseReq(lotInfo,lotHoldReqList,false,false,null,2,0);

        //【step4】check parent lot
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        String lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot");
        lotHoldReqList = this.getLotHoldListAndReason(lotInfoCase.getLotInfoList().get(0));
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus) && CimArrayUtils.getSize(lotHoldReqList) == 2,"parent hold lot can not be hold release and have 2 lot hold");

        //【step5】check child lot extend a parent lot hold
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        ObjectIdentifier childLotID = lotIDList.get(1);
        Results.LotInfoInqResult childLotInfoCase = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(childLotID)).getBody();
        String childLotStatus = childLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        List<Infos.LotHoldReq> childLotHoldReqList = this.getLotHoldListAndReason(childLotInfoCase.getLotInfoList().get(0));
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,childLotStatus) && CimArrayUtils.getSize(childLotHoldReqList) == 1,"child lot must be onHold and extend 1 parent lot hold");

        //【step6】parent lot do partial rework again and lotHoldList can not choice rework hold list
        lotHoldReqList = this.getLotHoldListAndReason(lotInfoCase.getLotInfoList().get(0));
        Validations.assertCheck(CimArrayUtils.getSize(lotHoldReqList) == 2,"the parent have 2 lot hold");
        //【step6-1】filter the rework Hold record out
        lotHoldReqList = lotHoldReqList.stream().filter(x -> x.getHoldType().equals(BizConstant.SP_HOLDTYPE_LOTHOLD)).collect(Collectors.toList());

        //【step7】partial rework with out hold release again
        this.partialReworkWithOutHoldReleaseReq(lotInfoCase.getLotInfoList().get(0),lotHoldReqList,false,false,null,2,0);
    }

    public void Do_It_Again_Partial_Rework_Without_Hold_Release_Positive_Action() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "2000.0320";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        //【step2】lot hold the parent lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfo.getLotBasicInfo().getLotStatus()),"parent hold lot must be on hold");

        //【step3】click rework button
        //【step3-1】get hold list
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);

        //【step3-2】partial rework with out hold release
        int waferSize = CimArrayUtils.getSize(lotInfo.getLotWaferAttributesList());
        try {
            this.partialReworkWithOutHoldReleaseReq(lotInfo,lotHoldReqList,false,false,null,waferSize,0);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getPlotEmptySplit(),e.getCode()),"test fail");
        }
    }

    public void Reach_Max_Rework_Count_For_Full_Rework_Case_OnHold_Partial_Rework_With_Hold_Release() {
        //【step0】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "3000.0200";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        String reworkNextOpeNum = "3000.0300";

        //【step1】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step2】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step3】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step4】on hold the lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);

        //【step5】partial rework with  hold release
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);

        //【step6】partial rework with hold release
        try {
            this.partialReworkWithHoldReleaseReq(lotInfo,lotHoldReqList,false,false,null,2,0);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getReachMaxRework(),e.getCode()),"test fail");
        }
    }

    public void Reach_Max_Rework_Count_For_Full_Rework_Case_FutureHold_Partial_Rework_With_Hold_Release() {
        //【step0】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "3000.0200";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        String reworkNextOpeNum = "3000.0300";

        //【step1】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step2】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step3】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step4】set Future Hold pre muilty and skip trigger the futureHold
        operationSkipCase.skipSpecificStep(Arrays.asList(lotID),"3000.0100",false);
        Infos.LotInfo lotInfo = this.registerFutureHoldWithParameter(lotID, skipOpeNum, false, false);
        List<Infos.LotInfo> lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), skipOpeNum, true);
        lotInfo = lotInfos.get(0);

        //【step5】partial rework with  hold release
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);

        //【step6】partial rework with hold release
        try {
            this.partialReworkWithHoldReleaseReq(lotInfo,lotHoldReqList,false,false,null,2,0);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getReachMaxRework(),e.getCode()),"test fail");
        }
    }

    public void Reach_Max_Rework_Count_For_Full_Rework_Case_OnHold_Partial_Rework_With_Out_Hold_Release() {
        //【step0】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "3000.0200";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        String reworkNextOpeNum = "3000.0300";

        //【step1】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step2】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step3】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step4】on hold the lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);

        //【step5】partial rework with  hold release
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);

        //【step6】partial rework with out hold release
        try {
            this.partialReworkWithOutHoldReleaseReq(lotInfo,lotHoldReqList,false,false,null,2,0);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getReachMaxRework(),e.getCode()),"test fail");
        }
    }

    public void Reach_Max_Rework_Count_For_Full_Rework_Case_FutureHold_Partial_Rework_With_Out_Hold_Release() {
        //【step0】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOpeNum = "3000.0200";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOpeNum);

        String reworkNextOpeNum = "3000.0300";

        //【step1】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step2】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step3】loop the last flow
        this.reworkFlowSkipToReworkNextOpeNumAndThenBackAndRework(lotID,reworkNextOpeNum,skipOpeNum);

        //【step4】set Future Hold  pre muilty and skip back and trigger the futureHold
        operationSkipCase.skipSpecificStep(Arrays.asList(lotID),"3000.0100",false);
        Infos.LotInfo lotInfo = this.registerFutureHoldWithParameter(lotID, skipOpeNum, false, false);
        List<Infos.LotInfo> lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), skipOpeNum, true);
        lotInfo = lotInfos.get(0);

        //【step5】partial rework with  hold release
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);

        //【step6】partial rework with hold release
        try {
            this.partialReworkWithOutHoldReleaseReq(lotInfo,lotHoldReqList,false,false,null,2,0);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getReachMaxRework(),e.getCode()),"test fail");
        }
    }

    public void Dynamic_All_Rework() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】dynamic full rework function  at first step
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        String returnPoint = "1000.0200";
        try {
            this.reworkReq(lotInfo,returnPoint,true,true);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidReworkOperation(),e.getCode()),"test fail");
        }

        //【step3】skip to next operation to do dynamic rework
        List<Infos.LotInfo> lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), returnPoint, true);
        lotInfo = lotInfos.get(0);

        //【step4】dynamic rework again
        this.reworkReq(lotInfo,returnPoint,true,true);

        //【step5】dynamic rework again in rework flow
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotInfo = lotInfoCase.getLotInfoList().get(0);
        try {
            this.reworkReq(lotInfo,returnPoint,true,true);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidLotProductionState(),e.getCode()),"test fail");
        }

        //【step6】from rework flow skip to return point
        //rework 1000.0200
        List<Infos.LotInfo> lotInfos1 = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), returnPoint, true);
        //main 1000.0200
        List<Infos.LotInfo> lotInfos2 = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), returnPoint, true);
    }

    public void Dynamic_Partial_Rework_Return_Point_Is_After_Current_Step() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOperation = "1000.0200";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOperation);

        //【step2】set return point after current
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        String returnPoint = "2000.0100";

        //【step3】dynamic partial rework and skip return point and merge parent lot and child lot
        this.dynamicPartialReworkOnReturnPointAndMergeParentLotAndChildLot(returnPoint,null,lotInfo,true);
    }

    private void dynamicPartialReworkOnReturnPointAndMergeParentLotAndChildLot(String returnPoint, String originalPoint,Infos.LotInfo lotInfo,Boolean stepFlag) {
        ObjectIdentifier parentLotId = lotInfo.getLotBasicInfo().getLotID();
        //【step1】exec partial rework set return point
        this.partialReworkReq(lotInfo,true,true,returnPoint,2,0);

        //【step2】check split child lot
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(parentLotId)).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        String parentHoldState = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"the parent lot must split a child lot");
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        ObjectIdentifier childLotId = lotIDList.get(1);

        //【step3】skip the parent lot and child lot to return point to trigger future hold
        if (!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,parentHoldState)){
            List<Infos.LotInfo> parentLotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(parentLotId), returnPoint, stepFlag);
            parentHoldState = parentLotInfos.get(0).getLotBasicInfo().getLotStatus();
        }
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,parentHoldState),"parent lot must on hold");

        List<Infos.LotInfo> childLotInfos = null;
        if (CimStringUtils.isNotEmpty(originalPoint)){
            childLotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(childLotId), originalPoint, true);
        }else {
            childLotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(childLotId), returnPoint, true);
        }
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,childLotInfos.get(0).getLotBasicInfo().getLotStatus()),"child lot must on hold");

        //【step4】merge parent lot and child lot
        lotMergeTestCase.mergeLotReq(parentLotId,childLotId);
    }


    public void Dynamic_Partial_Rework_Return_Point_Is_Before_Current_Step() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOperation = "2000.0100";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOperation);

        //【step2】set return point before current
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        String returnPoint = "1000.0200";

        //【step3】dynamic partial rework and skip return point and merge parent lot and child lot
        this.dynamicPartialReworkOnReturnPointAndMergeParentLotAndChildLot(returnPoint,skipOperation,lotInfo,false);
    }

    public void Do_Dynamic_Partial_Rework_Again_After_Already_Did_Dynamic_Partial_Rework_Parent_Lot() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOperation = "1000.0200";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOperation);

        //【step2】set return point before current
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        String returnPoint = "2000.0100";

        //【step3】dynamic partial rework for first time
        this.partialReworkReq(lotInfo,true,true,returnPoint,2,0);
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotInfo = lotInfoCase.getLotInfoList().get(0);
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"the parent lot must split a child lot");

        //【step4】dynamic partial rework for second time
        this.partialReworkReq(lotInfo,true,true,returnPoint,2,0);
    }

    public void Dynamic_Partial_Rework_And_SKip_Parent_And_Child_Lot_To_Step_After_Return_Point() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOperation = "2000.0100";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOperation);

        //【step2】set return point after current
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        String returnPoint = "2000.0400";
        String skipNextOpeNum = "2000.0500";
        this.dynamicPartialReworkOnReturnPointSkipOtherOpeNum(returnPoint,skipNextOpeNum,lotInfo,true);

    }

    private void dynamicPartialReworkOnReturnPointSkipOtherOpeNum(String returnPoint, String skipOpeNum,Infos.LotInfo lotInfo,Boolean stepFlag) {
        ObjectIdentifier parentLotId = lotInfo.getLotBasicInfo().getLotID();
        //【step1】exec partial rework set return point
        this.partialReworkReq(lotInfo,true,true,returnPoint,2,0);

        //【step2】check split child lot
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(parentLotId)).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        String parentHoldState = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"the parent lot must split a child lot");
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        ObjectIdentifier childLotId = lotIDList.get(1);

        //【step3】skip the parent lot and child lot to return point to trigger future hold
        if (CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_NOTONHOLD,parentHoldState)){
            try {
                List<Infos.LotInfo> parentLotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(parentLotId), skipOpeNum, stepFlag);
                Assert.isTrue(false,"test error");
            } catch (ServiceException e) {
                Assert.isTrue(Validations.isEquals(retCodeConfig.getNotFutureholdInLocate(),e.getCode()),"test fail");
            }
        }

        try {
            List<Infos.LotInfo> childLotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(childLotId), skipOpeNum, true);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotFutureholdInLocate(),e.getCode()),"test fail");
        }

    }

    public void Dynamic_Partial_Rework_And_SKip_Child_Lot_To_Step_Before_Return_Point() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOperation = "2000.0100";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOperation);

        //【step2】set return point after current
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        String returnPoint = "1000.0200";
        String skipNextOpeNum = "2000.0200";
        this.dynamicPartialReworkOnReturnPointSkipOtherOpeNum(returnPoint,skipNextOpeNum,lotInfo,true);
    }

    public void LotHold_Then_Enters_Dynamic_Full_Rework() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String reworkOpeNum = "2000.0350";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】lot hold on current operation number
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);

        //【step3】click rework button
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);

        //【step3-1】get hold list
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);

        //【step3-2】rework with hold release
        this.reworkWithHoldReleaseReq(lotInfo,lotHoldReqList,reworkOpeNum,true,true);

        //【step4】check lot hold state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Validations.assertCheck(!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus()),"the lot hold state can not be onHold");
    }

    public void Trigger_The_Future_Hold_And_Then_Proceed_To_Dynamic_Full_Rework() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】set a future hold pre single
        String futureHoldOperationNum = "2000.0350";
        this.registerFutureHoldWithParameter(lotID, futureHoldOperationNum, false, true);

        //【step3】skip to 2000.0350
        List<Infos.LotInfo> lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), futureHoldOperationNum, true);

        //【step4】click rework button
        Infos.LotInfo lotInfo = lotInfos.get(0);

        //【step4-1】get hold list
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);

        //【step4-2】rework with hold release
        this.reworkWithHoldReleaseReq(lotInfo,lotHoldReqList,futureHoldOperationNum,true,true);

        //【step5】check lot hold state
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Validations.assertCheck(!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus()),"the lot hold state can not be onHold");

        //【step6】rework cancel
        this.reworkCancelReq(lotInfoCase.getLotInfoList().get(0));

        //【step7】check lot hold state single ,so future hold did not trigger
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Validations.assertCheck(!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus()),"the lot hold state can not be onHold");
    }

    public void LotHold_Then_Performs_The_Dynamic_Partial_Rework_Operation_With_Hold_Release() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String reworkOpeNum = "2000.0350";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】lot hold on current operation number
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);


        //【step3】dynamic partial rework with hold release
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        String lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must be onHold");

        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);
        String returnPoint = "2000.0100";
        this.partialReworkWithHoldReleaseReq(lotInfo,lotHoldReqList,true,true,returnPoint,2,0);


        //【step4】check split and parent lot hold state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        lotHoldReqList = this.getLotHoldListAndReason(lotInfo);
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must be onHold");
        Validations.assertCheck(CimArrayUtils.getSize(lotHoldReqList) == 1,"parent lot hold must be replace by rework hold");
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot");
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        ObjectIdentifier childLotID = lotIDList.get(1);

        //【step5】partial rework cancel
        this.partialReworkCancelReq(childLotID,lotID);

        //【step5】check parent lot state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must not be release");
    }

    public void LotHold_Then_Performs_The_Dynamic_Partial_Rework_Operation_WithOut_Hold_Release() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String reworkOpeNum = "2000.0350";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,reworkOpeNum);

        //【step2】lot hold on current operation number
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);


        //【step3】dynamic partial rework with hold release
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        String lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must be onHold");

        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);
        String returnPoint = "2000.0100";
        this.partialReworkWithOutHoldReleaseReq(lotInfo,lotHoldReqList,true,true,returnPoint,2,0);

        //【step4】check split and parent lot hold state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        lotHoldReqList = this.getLotHoldListAndReason(lotInfo);
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must be onHold");
        Validations.assertCheck(CimArrayUtils.getSize(lotHoldReqList) == 2,"parent lot hold must be replace by rework hold");
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot");
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        ObjectIdentifier childLotID = lotIDList.get(1);
        Results.LotInfoInqResult childLotInfoCase = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(childLotID)).getBody();
        String childLotState = childLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,childLotState),"the child lot hold state must be onHold");

        //【step5】partial rework cancel
        this.partialReworkCancelReq(childLotID,lotID);

        //【step5】check parent lot state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must be onhold");
    }

    public void After_Triggering_The_FutureHold_And_Performs_Dynamic_Partial_Rework_Operation_With_Hold_Release() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】set a future hold pre single
        String futureHoldOperationNum = "2000.0350";
        this.registerFutureHoldWithParameter(lotID, futureHoldOperationNum, false, true);

        //【step3】skip to 2000.0350
        List<Infos.LotInfo> lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), futureHoldOperationNum, true);

        //【step3】dynamic partial rework with hold release
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        String lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must be onHold");

        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);
        String returnPoint = "2000.0100";
        this.partialReworkWithHoldReleaseReq(lotInfo,lotHoldReqList,true,true,returnPoint,2,0);


        //【step4】check split and parent lot hold state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        lotHoldReqList = this.getLotHoldListAndReason(lotInfo);
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must be onHold");
        Validations.assertCheck(CimArrayUtils.getSize(lotHoldReqList) == 1,"parent lot hold must be replace by rework hold");
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot");
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        ObjectIdentifier childLotID = lotIDList.get(1);

        //【step5】partial rework cancel
        this.partialReworkCancelReq(childLotID,lotID);

        //【step5】check parent lot state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(!CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must not be release");
    }

    public void After_Triggering_The_FutureHold_And_Performs_Dynamic_Partial_Rework_Operation_WithOut_Hold_Release_Partial_Rework_Cancel() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】set a future hold pre single
        String futureHoldOperationNum = "2000.0350";
        this.registerFutureHoldWithParameter(lotID, futureHoldOperationNum, false, true);

        //【step3】skip to 2000.0350
        List<Infos.LotInfo> lotInfos = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), futureHoldOperationNum, true);

        //【step3】dynamic partial rework with hold release
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        String lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must be onHold");

        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        List<Infos.LotHoldReq> lotHoldReqList = this.getLotHoldListAndReason(lotInfo);
        String returnPoint = "2000.0100";
        this.partialReworkWithOutHoldReleaseReq(lotInfo,lotHoldReqList,true,true,returnPoint,2,0);


        //【step4】check split and parent lot hold state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        lotHoldReqList = this.getLotHoldListAndReason(lotInfo);
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must be onHold");
        Validations.assertCheck(CimArrayUtils.getSize(lotHoldReqList) == 2,"parent lot hold must be replace by rework hold");
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot");
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        ObjectIdentifier childLotID = lotIDList.get(1);
        Results.LotInfoInqResult childLotInfoCase = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(childLotID)).getBody();
        String childLotState = childLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,childLotState),"the child lot hold state must be onHold");

        //【step5】partial rework cancel
        this.partialReworkCancelReq(childLotID,lotID);

        //【step5】check parent lot state
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        lotStatus = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,lotStatus),"the lot hold state must be onhold");
    }
}