package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.lot.LotController;
import com.fa.cim.controller.lot.LotInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/9/2 13:48
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotHoldTestCase {

    @Autowired
    private LotController lotController;

    @Autowired
    private LotInqController lotInqController;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }


    public Response holdLotReqCase(String lotID, String reasonCode, String reasonableOperation,ObjectIdentifier routeID,String operationNumber,String codeCategory) {
        Params.HoldLotReqParams holdLotReqParams = new Params.HoldLotReqParams();
        holdLotReqParams.setUser(getUser());
        holdLotReqParams.setLotID(new ObjectIdentifier(lotID));

        List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(reasonCode));
        lotHoldReq.setResponsibleOperationMark(reasonableOperation);
        lotHoldReq.setHoldUserID(getUser().getUserID());
        lotHoldReq.setHoldType(codeCategory);

        lotHoldReq.setOperationNumber(operationNumber);
        lotHoldReq.setRouteID(routeID);

        holdReqList.add(lotHoldReq);
        holdLotReqParams.setHoldReqList(holdReqList);

        Response result = lotController.holdLotReq(holdLotReqParams);
        Validations.assertCheck(result.getCode() == 0, "vendorLotReceiveReqCase() != 0" );
        return result;
    }


    public Response getLotHoldListCase(ObjectIdentifier lotID) {
        Params.HoldLotListInqParams params = new Params.HoldLotListInqParams();
        params.setUser(getUser());
        params.setLotID(lotID);
        params.setSearchCondition(new SearchCondition());
        Response result = lotInqController.HoldLotListInq(params);
        Validations.isSuccessWithException(result);
        return result;
    }

    public Response lotHoldReleaseReqCase(ObjectIdentifier lotID) {
        //【step1】call lotInfoInq to get the detail lot info
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Response response = lotGeneralTestCase.getLotInfoCase(lotIDs);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) response.getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();
        ObjectIdentifier routeID = lotOperationInfo.getRouteID();
        String operationNumber = lotOperationInfo.getOperationNumber();

        //【step2】call holdLotListInqCase function and then choose one Hold Data
        //【bear】the holdLotListInqCase include lotHoldReq, so we can't call more once again.
        log.debug("【step2】call holdLotListInqCase function and then choose one Hold Data");
        response = this.getLotHoldListCase(lotID);
        Results.HoldLotListInqResult holdLotListInqResult = (Results.HoldLotListInqResult)response.getBody();
        Infos.LotHoldListAttributes lotHoldListAttributes = holdLotListInqResult.getLotHoldListAttributes().getContent().get(0);


        //【step3】call reasonCodeListByCategoryInqCase to get all release reason code and then choose one release reason code
        log.debug("【step3】call reasonCodeListByCategoryInqCase to get all release reason code and then choose one release reason code");
        String releaseReasonCode = "ReleaseHoldLot";
        Response response1 =  lotGeneralTestCase.getReasonListCase(releaseReasonCode);
        List<Infos.ReasonCodeAttributes>  reasonCodeListByCategoryInqResult = (List<Infos.ReasonCodeAttributes>)response1.getBody();
        Infos.ReasonCodeAttributes reasonCodeResult = reasonCodeListByCategoryInqResult.get(0);


        //【step4】call hold lot release function
        log.debug("【step4】call hold lot release function");
        Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
        holdLotReleaseReqParams.setUser(getUser());
        holdLotReleaseReqParams.setLotID(lotID);
        holdLotReleaseReqParams.setReleaseReasonCodeID(new ObjectIdentifier("LOHR"));
        List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldReasonCodeID(lotHoldListAttributes.getReasonCodeID());
        lotHoldReq.setHoldType(lotHoldListAttributes.getHoldType());
        lotHoldReq.setHoldUserID(getUser().getUserID());
        lotHoldReq.setOperationNumber(operationNumber);
        lotHoldReq.setResponsibleOperationMark("C");
        lotHoldReq.setRelatedLotID(lotHoldListAttributes.getRelatedLotID());

        //lotHoldReq.setRelatedLotID(new ObjectIdentifier());  //【bear】the relatedLotID not equal the lotID
        lotHoldReq.setRouteID(routeID);

        holdReqList.add(lotHoldReq);
        holdLotReleaseReqParams.setHoldReqList(holdReqList);
        Response result = lotController.holdLotReleaseReq(holdLotReleaseReqParams);
        Validations.isSuccessWithException(result);
        return result;
    }
}
