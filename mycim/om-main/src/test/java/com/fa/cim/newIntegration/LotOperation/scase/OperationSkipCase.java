package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.equipment.scase.LotLoadToEquipmentCase;
import com.fa.cim.newIntegration.equipment.scase.MoveInCase;
import com.fa.cim.newIntegration.equipment.scase.StartLotsReservationCancelCase;
import com.fa.cim.newIntegration.equipment.scase.StartLotsReservationCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.*;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/10       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/10 16:31
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class OperationSkipCase {
    @Autowired
    private STBCase stbCase;

    @Autowired
    private FutureHoldTestCase futureHoldTestCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private OperationSkipTestCase operationSkipTestCase;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private LotHoldTestCase lotHoldTestCase;

    @Autowired
    private BankTestCase bankTestCase;

    @Autowired
    private LotLoadToEquipmentCase lotLoadToEquipmentCase;

    @Autowired
    private ReworkCase reworkCase;

    @Autowired
    private StartLotsReservationCase startLotsReservationCase;

    @Autowired
    private StartLotsReservationCancelCase startLotsReservationCancelCase;

    @Autowired
    private MoveInCase moveInCase;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    public Response operationSkip_Forward_Backward_Normal(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        //【step3】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes1 = operationNameAttributesList1.get(operationNameAttributesList1.size()/2);
        //【step4】skip
        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);
        //【step5】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult3 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, false).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList3 = lotOperationSelectionInqResult3.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes3 = operationNameAttributesList3.get(new Random().nextInt(operationNameAttributesList3.size()));
        //【step6】skip backward
        Params.SkipReqParams skipReqParams2 = new Params.SkipReqParams();
        skipReqParams2.setCurrentOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams2.setCurrentRouteID(operationNameAttributes1.getRouteID());
        skipReqParams2.setLocateDirection(false);
        skipReqParams2.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams2.setOperationID(operationNameAttributes3.getOperationID());
        skipReqParams2.setOperationNumber(operationNameAttributes3.getOperationNumber());
        skipReqParams2.setProcessRef(operationNameAttributes3.getProcessRef());
        skipReqParams2.setRouteID(operationNameAttributes3.getRouteID());
        skipReqParams2.setSeqno(-1);
        skipReqParams2.setSequenceNumber(0);
        return operationSkipTestCase.operationSkip(skipReqParams2);
    }

    public void operationSkip_Forward_With_FutureHoldRegistration_In_CurrentOperation(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        //【step3】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent().get(0);
        String operationNumber = operationNameAttributes1.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes1.getRouteID();
        //【step4】future hold register
        futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, true);
        //【step5】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes2 = operationNameAttributesList2.get(new Random().nextInt(operationNameAttributesList2.size()));
        //【step6】skip
        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        Response response = null;
        try {
            response = operationSkipTestCase.operationSkip(skipReqParams);
        } catch (ServiceException e) {
            Assert.isTrue(e.getCode() == retCodeConfig.getNotFutureholdInLocate().getCode(), e.getMessage());
        }
    }

    public void operationSkip_Forward_With_FutureHoldRegistrationPre_In_DestinationOperation(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        //【step3】getOperationNumber and RouteID that futurehold
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes1 = operationNameAttributesList1.get(operationNameAttributesList1.size() - 1);
        String operationNumber = operationNameAttributes1.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes1.getRouteID();
        //【step4】future hold register
        futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, false);
        //【step5】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes2 = operationNameAttributesList2.get(operationNameAttributesList2.size() - 1);
        //【step6】skip
        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);
        //【step6】hold list
        Response response = lotHoldTestCase.getLotHoldListCase(waferLotStartReqResult.getLotID());
        Results.HoldLotListInqResult holdListInqResult = (Results.HoldLotListInqResult) response.getBody();
        Assert.isTrue(!CimObjectUtils.isEmpty(holdListInqResult.getLotHoldListAttributes().getContent()), "hold list must not be null");
        //【step7】get lot info again
        response = lotGeneralTestCase.getLotInfoCase(lotIDList);
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) response.getBody();
        Infos.LotInfo lotInfo2 = lotInfoInqResult2.getLotInfoList().get(0);
        Assert.isTrue(lotInfo2.getLotBasicInfo().getLotStatus().equals("ONHOLD"), "the lot status must be onhold");
    }

    public void operationSkip_Backward_With_FutureHoldRegistrationPre_In_CurrentOperation_LotOnhold(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        //【step3】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes1 = operationNameAttributesList1.get(operationNameAttributesList1.size()/2);
        //【step4】skip
        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);
        //【step5】getOperationNumber and RouteID that futurehold
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes2 = operationNameAttributesList2.get(operationNameAttributesList2.size()/2);
        String operationNumber = operationNameAttributes2.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes2.getRouteID();
        //【step6】future hold register
        futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, false);

        //【step7】lotHold
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        String codeCategory = "HoldLot";
        lotHoldTestCase.holdLotReqCase(waferLotStartReqResult.getLotID().getValue(), reasonCode, reasonableOperation, operationNameAttributes1.getRouteID(), operationNameAttributes1.getOperationNumber(), codeCategory);

        //【step8】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult3 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, false).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList3 = lotOperationSelectionInqResult3.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes3 = operationNameAttributesList3.get(new Random().nextInt(operationNameAttributesList3.size()));
        //【step9】skip
        Params.SkipReqParams skipReqParams2 = new Params.SkipReqParams();
        skipReqParams2.setCurrentOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams2.setCurrentRouteID(operationNameAttributes1.getRouteID());
        skipReqParams2.setLocateDirection(false);
        skipReqParams2.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams2.setOperationID(operationNameAttributes3.getOperationID());
        skipReqParams2.setOperationNumber(operationNameAttributes3.getOperationNumber());
        skipReqParams2.setProcessRef(operationNameAttributes3.getProcessRef());
        skipReqParams2.setRouteID(operationNameAttributes3.getRouteID());
        skipReqParams2.setSeqno(-1);
        skipReqParams2.setSequenceNumber(0);
        operationSkipTestCase.forceOperationSkip(skipReqParams2);
        //【step10】get lot info again
        Response response = lotGeneralTestCase.getLotInfoCase(lotIDList);
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) response.getBody();
        Infos.LotInfo lotInfo2 = lotInfoInqResult2.getLotInfoList().get(0);
        Assert.isTrue(lotInfo2.getLotBasicInfo().getLotStatus().equals("ONHOLD"), "the lot status must be onhold");
    }

    public void operationSkip_Backward_With_FutureHoldRegistrationPost_In_CurrentOperation(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        //【step3】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes1 = operationNameAttributesList1.get(operationNameAttributesList1.size()/2);
        //【step4】skip
        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);
        //【step5】getOperationNumber and RouteID that futurehold
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes2 = operationNameAttributesList2.get(operationNameAttributesList2.size()/2);
        String operationNumber = operationNameAttributes2.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes2.getRouteID();
        //【step6】future hold register
        futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, true);

        //【step7】lotHold
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        String codeCategory = "HoldLot";
        lotHoldTestCase.holdLotReqCase(waferLotStartReqResult.getLotID().getValue(), reasonCode, reasonableOperation, operationNameAttributes1.getRouteID(), operationNameAttributes1.getOperationNumber(), codeCategory);

        //【step8】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult3 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, false).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList3 = lotOperationSelectionInqResult3.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes3 = operationNameAttributesList3.get(new Random().nextInt(operationNameAttributesList3.size()));
        //【step9】skip
        Params.SkipReqParams skipReqParams2 = new Params.SkipReqParams();
        skipReqParams2.setCurrentOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams2.setCurrentRouteID(operationNameAttributes1.getRouteID());
        skipReqParams2.setLocateDirection(false);
        skipReqParams2.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams2.setOperationID(operationNameAttributes3.getOperationID());
        skipReqParams2.setOperationNumber(operationNameAttributes3.getOperationNumber());
        skipReqParams2.setProcessRef(operationNameAttributes3.getProcessRef());
        skipReqParams2.setRouteID(operationNameAttributes3.getRouteID());
        skipReqParams2.setSeqno(-1);
        skipReqParams2.setSequenceNumber(0);
        operationSkipTestCase.forceOperationSkip(skipReqParams2);
        //【step10】get lot info again
        Response response = lotGeneralTestCase.getLotInfoCase(lotIDList);
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) response.getBody();
        Infos.LotInfo lotInfo2 = lotInfoInqResult2.getLotInfoList().get(0);
        Assert.isTrue(lotInfo2.getLotBasicInfo().getLotStatus().equals("ONHOLD"), "the lot status must be onhold");
    }

    public void operationSkip_Backward_With_FutureHoldRegistrationPre_In_CurrentOperation(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        //【step3】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes1 = operationNameAttributesList1.get(operationNameAttributesList1.size()/2);
        //【step4】skip
        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);
        //【step5】getOperationNumber and RouteID that futurehold
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes2 = operationNameAttributesList2.get(operationNameAttributesList2.size()/2);
        String operationNumber = operationNameAttributes2.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes2.getRouteID();
        //【step6】future hold register
        futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, false);
        //【step7】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult3 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, false).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList3 = lotOperationSelectionInqResult3.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes3 = operationNameAttributesList3.get(new Random().nextInt(operationNameAttributesList3.size()));
        //【step8】skip
        Params.SkipReqParams skipReqParams2 = new Params.SkipReqParams();
        skipReqParams2.setCurrentOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams2.setCurrentRouteID(operationNameAttributes1.getRouteID());
        skipReqParams2.setLocateDirection(false);
        skipReqParams2.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams2.setOperationID(operationNameAttributes3.getOperationID());
        skipReqParams2.setOperationNumber(operationNameAttributes3.getOperationNumber());
        skipReqParams2.setProcessRef(operationNameAttributes3.getProcessRef());
        skipReqParams2.setRouteID(operationNameAttributes3.getRouteID());
        skipReqParams2.setSeqno(-1);
        skipReqParams2.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams2);
        //【step9】get lot info again
        Response response = lotGeneralTestCase.getLotInfoCase(lotIDList);
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) response.getBody();
        Infos.LotInfo lotInfo2 = lotInfoInqResult2.getLotInfoList().get(0);
        Assert.isTrue(lotInfo2.getLotBasicInfo().getLotStatus().equals("Waiting"), "the lot status must be waiting");
    }

    public void operationSkip_Backward_With_FutureHoldRegistrationPre_In_CurrentOperation_Forward_FutureHold(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        //【step3】getOperationNumber and RouteID that futurehold
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes1 = operationNameAttributesList1.get(operationNameAttributesList1.size() - 1);
        String operationNumber = operationNameAttributes1.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes1.getRouteID();
        //【step4】future hold register
        futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, false);
        //【step5】skip
        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);
        //【step6】future hold list
        Response response = lotHoldTestCase.getLotHoldListCase(waferLotStartReqResult.getLotID());
        Results.HoldLotListInqResult holdListInqResult = (Results.HoldLotListInqResult) response.getBody();
        List<Infos.LotHoldListAttributes> content = holdListInqResult.getLotHoldListAttributes().getContent();
        Assert.isTrue(!CimObjectUtils.isEmpty(content), "hold list must not be null");
        //【step7】get lot info again
        response = lotGeneralTestCase.getLotInfoCase(lotIDList);
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) response.getBody();
        Infos.LotInfo lotInfo2 = lotInfoInqResult2.getLotInfoList().get(0);
        Assert.isTrue(lotInfo2.getLotBasicInfo().getLotStatus().equals("ONHOLD"), "the lot status must be onhold");
        //【step8】lot hold release
        lotHoldTestCase.lotHoldReleaseReqCase(waferLotStartReqResult.getLotID());
        //【step9】get lot info again
        response = lotGeneralTestCase.getLotInfoCase(lotIDList);
        Results.LotInfoInqResult lotInfoInqResult3 = (Results.LotInfoInqResult) response.getBody();
        Infos.LotInfo lotInfo3 = lotInfoInqResult3.getLotInfoList().get(0);
        Assert.isTrue(lotInfo3.getLotBasicInfo().getLotStatus().equals("Waiting"), "the lot status must be onhold");
        //【step10】getOperationNumber and RouteID that futurehold
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes2 = operationNameAttributesList2.get(operationNameAttributesList2.size() - 1);
        String operationNumber2 = operationNameAttributes2.getOperationNumber();
        ObjectIdentifier routeID2 = operationNameAttributes2.getRouteID();
        //【step11】future hold register
        futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber2, routeID2, true);
        //【step12】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult3 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, false).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList3 = lotOperationSelectionInqResult3.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes3 = operationNameAttributesList3.get(new Random().nextInt(operationNameAttributesList3.size()));
        //【step13】skip
        Params.SkipReqParams skipReqParams2 = new Params.SkipReqParams();
        skipReqParams2.setCurrentOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams2.setCurrentRouteID(operationNameAttributes1.getRouteID());
        skipReqParams2.setLocateDirection(false);
        skipReqParams2.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams2.setOperationID(operationNameAttributes3.getOperationID());
        skipReqParams2.setOperationNumber(operationNameAttributes3.getOperationNumber());
        skipReqParams2.setProcessRef(operationNameAttributes3.getProcessRef());
        skipReqParams2.setRouteID(operationNameAttributes3.getRouteID());
        skipReqParams2.setSeqno(-1);
        skipReqParams2.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams2);
        //【step14】get lot info again
        response = lotGeneralTestCase.getLotInfoCase(lotIDList);
        Results.LotInfoInqResult lotInfoInqResult4 = (Results.LotInfoInqResult) response.getBody();
        Infos.LotInfo lotInfo4 = lotInfoInqResult4.getLotInfoList().get(0);
        Assert.isTrue(lotInfo4.getLotBasicInfo().getLotStatus().equals("Waiting"), "the lot status must be waiting");
    }

    public void operationSkip_With_CarrierTransferStatus(){
        //【step1】assert the envValue is 1
        int envValue = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getIntValue();
        if (envValue !=1 ){
            //environmentVariableManager.updateValue(StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getName(), "1");
        }
        //【step2】stb and skip
        List<Infos.LotInfo> lotInfoList = this.stbNLotsAndSkipSpecificStep(1, true, "1000.0200", true);
        Infos.LotInfo lotInfo = lotInfoList.get(0);
        List<Infos.LotEquipmentList> lotEquipmentList = lotInfo.getLotOperationInfo().getLotEquipmentList();
        //【step3】load
        lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotInfo.getLotBasicInfo().getLotID(), lotEquipmentList.get(0).getEquipmentID());
        //【step4】lot info
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(Arrays.asList(lotInfo.getLotBasicInfo().getLotID()));
        String transferStatus = lotInfoList2.get(0).getLotLocationInfo().getTransferStatus();
        Assert.isTrue(CimStringUtils.equals(transferStatus, "EI"), "xfer status is not correct");
        //【step5】skip backward
        try {
            this.skipSpecificStep(Arrays.asList(lotInfo.getLotBasicInfo().getLotID()), "1000.0100", false);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteTransferState(), e.getCode()), e.getMessage());
        }
        //【step6】change the envValue to 0
        //environmentVariableManager.updateValue(StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getName(),"0");
        //【step7】skip backward ,will success
        this.skipSpecificStep(Arrays.asList(lotInfo.getLotBasicInfo().getLotID()), "1000.0100", false);
    }

    public void forceOperationSkip_When_LotHeldByProcessHoldInstruction(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        //【step3】lotHold
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        String codeCategory = "HoldLot";
        lotHoldTestCase.holdLotReqCase(waferLotStartReqResult.getLotID().getValue(), reasonCode, reasonableOperation, lotInfo.getLotOperationInfo().getRouteID(), lotInfo.getLotOperationInfo().getOperationNumber(), codeCategory);

        //【step4】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes = operationNameAttributesList.get(new Random().nextInt(operationNameAttributesList.size()));
        //【step5】skip
        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(operationNameAttributes.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.forceOperationSkip(skipReqParams);
        //【step6】lot info
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo2 = lotInfoInqResult2.getLotInfoList().get(0);
        Assert.isTrue(lotInfo2.getLotBasicInfo().getLotStatus().equals("ONHOLD"), "the lot status must be onhold");
    }

    public void operationSkip_LotInBank(){
        //【step1】stb and bank in
        List<Infos.LotInfo> lotInfoList = this.skipAndBankIn();
        //【step2】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotInfoList.get(0).getLotBasicInfo().getLotID(), false, true, false).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes2 = operationNameAttributesList2.get(new Random().nextInt(operationNameAttributesList2.size()));
        //【step3】skip backword
        Params.SkipReqParams skipReqParams2 = new Params.SkipReqParams();
        skipReqParams2.setCurrentOperationNumber(lotInfoList.get(0).getLotOperationInfo().getOperationNumber());
        skipReqParams2.setCurrentRouteID(lotInfoList.get(0).getLotOperationInfo().getRouteID());
        skipReqParams2.setLocateDirection(false);
        skipReqParams2.setLotID(lotInfoList.get(0).getLotBasicInfo().getLotID());
        skipReqParams2.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams2.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams2.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams2.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams2.setSeqno(-1);
        skipReqParams2.setSequenceNumber(0);
        Response response = null;
        try {
            response = operationSkipTestCase.operationSkip(skipReqParams2);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(e.getCode() == retCodeConfig.getInvalidLotStat().getCode(), e.getMessage());
        }
    }

    public void moveLotFromCurrentOperationInReworkFlowBackToMainProcessFlow(){
        //【step1】stb and skip
        List<Infos.LotInfo> lotInfoList = this.stbNLotsAndSkipSpecificStep(1, true, "3000.0200", true);
        //【step2】full rework
        reworkCase.reworkWholeLotReq(lotInfoList.get(0));
        //【step3】get lot info
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()));
        String lotProductionStateName = "Lot Production State";
        Map<String, List<Infos.LotStatusList>> map = lotInfoList2.get(0).getLotBasicInfo().getLotStatusList().stream().collect(Collectors.groupingBy(Infos.LotStatusList::getStateName));
        String lotProductionStateValue = map.get(lotProductionStateName).get(0).getStateValue();
        Assert.isTrue(CimStringUtils.equals(lotProductionStateValue, "INREWORK"), " the Lot Production State is not correct");
        //【step4】skip forward
        this.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), "3000.0300", true);
        //【step5】get lot info
        List<Infos.LotInfo> lotInfoList3 = lotGeneralTestCase.getLotInfos(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()));
        Map<String, List<Infos.LotStatusList>> map2 = lotInfoList3.get(0).getLotBasicInfo().getLotStatusList().stream().collect(Collectors.groupingBy(Infos.LotStatusList::getStateName));
        String lotProductionStateValue2 = map2.get(lotProductionStateName).get(0).getStateValue();
        Assert.isTrue(CimStringUtils.equals(lotProductionStateValue2, "INPRODUCTION"), " the Lot Production State is not correct");
    }

    public void skipLotWhenLotInventoryStateIsInBank(){
        //【step1】stb and bankin
        List<Infos.LotInfo> lotInfoList = this.skipAndBankIn();
        String lotProductionStateName = "Lot Inventory State";
        Map<String, List<Infos.LotStatusList>> map = lotInfoList.get(0).getLotBasicInfo().getLotStatusList().stream().collect(Collectors.groupingBy(Infos.LotStatusList::getStateName));
        String lotProductionStateValue = map.get(lotProductionStateName).get(0).getStateValue();
        Assert.isTrue(CimStringUtils.equals(lotProductionStateValue, "InBank"), " the Lot Inventory State is not correct");
        //【step2】skip
        try {
            this.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), "1000.0100", false);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode()), e.getMessage());
        }
    }

    public void skipWithDestinationoperationsIsTheSameRoute(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot operation list
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lots.get(0), false, true, false).getBody();
        Assert.isTrue(CimArrayUtils.isEmpty(lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent()), "test fail");
        //【step3】skip
        List<Infos.LotInfo> lotInfoList = this.skipSpecificStep(lots, "3000.0200", true);
        //【step4】full rework
        reworkCase.reworkWholeLotReq(lotInfoList.get(0));
        //【step5】get Lot info
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()));
        //【step6】get lot operation list backward
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotInfoList.get(0).getLotBasicInfo().getLotID(), false, true, false).getBody();
        Assert.isTrue(!CimArrayUtils.isEmpty(lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent()), "test fail");
        List<ObjectIdentifier> routeIDList = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent().stream().map(Infos.OperationNameAttributes::getRouteID).collect(Collectors.toList());
        Assert.isTrue(!routeIDList.contains(lotInfoList2.get(0).getLotOperationInfo().getRouteID()), "test fail") ;
        //【step7】get lot operation list forward
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult3 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotInfoList.get(0).getLotBasicInfo().getLotID(), false, true, true).getBody();
        Assert.isTrue(!CimArrayUtils.isEmpty(lotOperationSelectionInqResult3.getOperationNameAttributesAttributes().getContent()), "test fail");
        List<Infos.OperationNameAttributes> operationNameAttributesList = lotOperationSelectionInqResult3.getOperationNameAttributesAttributes().getContent().stream().
                filter(operationNameAttributes -> CimObjectUtils.equalsWithValue(operationNameAttributes.getRouteID(), lotInfoList2.get(0).getLotOperationInfo().getRouteID())).collect(Collectors.toList());
        //【step8】skip forward
        this.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), operationNameAttributesList.get(0).getOperationNumber(), true);
        //【step9】skip backward
        this.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), lotInfoList2.get(0).getLotOperationInfo().getOperationNumber(), false);
        //【step10】skip over the rework route
        this.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), "3000.0300", true);
        //【step11】then skip to the reword route will fail
        try {
            this.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), lotInfoList2.get(0).getLotOperationInfo().getOperationNumber(), false);
            Assert.isTrue(false, "test fail");
        } catch (Exception e) {

        }
    }

    public void skipWithFutureHoldBetweenTargetOperationAndCurrentOperation(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        //【step3】register future hold
        futureHoldTestCase.futureHoldRegisterBySpecLot(lotInfoList.get(0).getLotBasicInfo().getLotID(), "1000.0200", lotInfoList.get(0).getLotOperationInfo().getRouteID(), true);
        //【step4】skip
        try {
            this.skipSpecificStep(lots, "2000.0100", true);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotFutureholdInLocate(), e.getCode()), e.getMessage());
        }

    }

    public void lotSkipWhenLotReservedToCurrentEquipment(){
        //【step1】reserve
        ObjectIdentifier cassetteID = startLotsReservationCase.startLotsReservation();
        //【step2】lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfosByCassette(cassetteID);
        //【step3】skip
        try {
            this.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), "2000.0100", true);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getLotControlJobidFilled(), e.getCode()), e.getMessage());
        }
        //【step4】load
        lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotInfoList.get(0).getLotBasicInfo().getLotID(), testCommonData.getEQUIPMENTID());
        //【step5】skip
        try {
            this.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), "2000.0100", true);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteTransferState(), e.getCode()), e.getMessage());
        }
        //【step6】reserve cancel
        startLotsReservationCancelCase.startLotsReservationCancel_normal(testCommonData.getEQUIPMENTID());
        //【step7】skip
        try {
            this.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), "2000.0100", true);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteTransferState(), e.getCode()), e.getMessage());
        }
        //【step8】move in
        moveInCase.onlyMoveIn(Arrays.asList(cassetteID), testCommonData.getEQUIPMENTID());
        //【step9】skip
        try {
            this.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), "2000.0100", true);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidLotProcessState(), e.getCode()), e.getMessage());
        }

    }

    public void skipFlowBatch(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        //【step3】Skip to the entry point of flow batch
        this.skipSpecificStep(lots, "4000.0100", true);
        //【step4】Skip to the flow batch section
        try {
            this.skipSpecificStep(lots, "4000.0200", true);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode()), e.getMessage());
        }
        //【step5】Skip over the target point of flow batch, will success
        this.skipSpecificStep(lots, "5000.0100", true);
    }

    public List<Infos.LotInfo> skipSpecificStep(List<ObjectIdentifier> lots, String step, boolean locateDirection){
        boolean searchDirection = locateDirection;
        List<Infos.LotInfo> lotInfoList = new ArrayList<>();
        for (ObjectIdentifier lot : lots) {
            List<ObjectIdentifier> lotIDList = new ArrayList<>();
            lotIDList.add(lot);
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
            Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lot, false, true, searchDirection).getBody();
            List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent();
            Infos.OperationNameAttributes operationNameAttributes1 = null;
            for (Infos.OperationNameAttributes tmpOperationNameAttributes : operationNameAttributesList1) {
                if (CimStringUtils.equals(tmpOperationNameAttributes.getOperationNumber(), step)) {
                    operationNameAttributes1 = tmpOperationNameAttributes;
                    break;
                }
            }
            Assert.isTrue(operationNameAttributes1 != null, "not find operationnumber");
            Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
            skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
            skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
            skipReqParams.setLocateDirection(locateDirection);
            skipReqParams.setLotID(lot);
            skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
            skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
            skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
            skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
            skipReqParams.setSeqno(-1);
            skipReqParams.setSequenceNumber(0);
            operationSkipTestCase.operationSkip(skipReqParams);
            Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotInfo lotInfo2 = lotInfoInqResult2.getLotInfoList().get(0);
            lotInfoList.add(lotInfo2);
        }
        return lotInfoList;
    }

    /**
     * description: produce n lots and skip
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/9 10:13
     * @param number
     * @param isFirst
     * @param step
     * @param locateDirection -
     * @return java.util.List<com.fa.cim.dto.Infos.LotInfo>
     */
    public List<Infos.LotInfo> stbNLotsAndSkipSpecificStep(int number, boolean isFirst, String step, boolean locateDirection){
        return this.stbNLotsAndSkipSpecificStep(null, number, isFirst, step, locateDirection);
    }

    public List<Infos.LotInfo> stbNLotsAndSkipSpecificStep(String productID, int number, boolean isFirst, String step, boolean locateDirection){
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(productID, number, isFirst);
        return this.skipSpecificStep(lots, step, locateDirection);
    }

    public List<Infos.LotInfo> skipAndBankIn(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);

        //【step4】get lot operation
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes = operationNameAttributesList.get(operationNameAttributesList.size()-1);
        //【step5】skip
        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(operationNameAttributes.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);
        //【step5】bank in
        bankTestCase.bankInCase(lotIDList);
        List<Infos.LotInfo> lotInfos = lotGeneralTestCase.getLotInfos(Arrays.asList(waferLotStartReqResult.getLotID()));
        return lotInfos;
    }
}