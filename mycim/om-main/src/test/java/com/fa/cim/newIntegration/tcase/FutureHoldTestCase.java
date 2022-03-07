package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.processcontrol.ProcessController;
import com.fa.cim.controller.processcontrol.ProcessInqController;
import com.fa.cim.controller.system.SystemInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/10       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/10 15:14
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class FutureHoldTestCase {

    @Autowired
    private SystemInqController systemInqController;

    @Autowired
    private ProcessController processController;

    @Autowired
    private ProcessInqController processInqController;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private LotHoldTestCase lotHoldTestCase;



    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public Response futureHoldRegister(ObjectIdentifier lotID, String operationNumber, boolean postFlag, boolean singleTriggerFlag,
                                       ObjectIdentifier reasonCodeID, ObjectIdentifier relatedLotID, ObjectIdentifier routeID, String holdType){
        Params.FutureHoldReqParams futureHoldReqParams = new Params.FutureHoldReqParams();
        futureHoldReqParams.setHoldType(holdType);
        futureHoldReqParams.setLotID(lotID);
        futureHoldReqParams.setOperationNumber(operationNumber);
        futureHoldReqParams.setPostFlag(postFlag);
        futureHoldReqParams.setReasonCodeID(reasonCodeID);
        futureHoldReqParams.setRelatedLotID(relatedLotID);
        futureHoldReqParams.setRouteID(routeID);
        futureHoldReqParams.setSingleTriggerFlag(singleTriggerFlag);
        futureHoldReqParams.setUser(getUser());
        return processController.futureHoldReq(futureHoldReqParams);
    }

    public Response getReasonInfo(){
        Params.ReasonCodeListByCategoryInqParams reasonCodeListByCategoryInqParams = new Params.ReasonCodeListByCategoryInqParams();
        reasonCodeListByCategoryInqParams.setUser(getUser());
        reasonCodeListByCategoryInqParams.setCodeCategory("FutureHold");
        return systemInqController.reasonCodeListByCategoryInq(reasonCodeListByCategoryInqParams);
    }

    public Response futureHoldRegisterBySpecLot(ObjectIdentifier lotID, String operationNumber, ObjectIdentifier routeID, boolean postFlag){
        //【step1】getReason
        String reasonCode = "MGHL";
        ObjectIdentifier reasonCodeID = null;
        List<Infos.ReasonCodeAttributes> reasonCodeListByCategoryInqResult = (List<Infos.ReasonCodeAttributes>) this.getReasonInfo().getBody();
        for (Infos.ReasonCodeAttributes reasonCodeResult : reasonCodeListByCategoryInqResult){
            if (reasonCodeResult.getReasonCodeID().getValue().equals(reasonCode)){
                reasonCodeID = reasonCodeResult.getReasonCodeID();
            }
        }
        //【step2】futureHoldReq
        return this.futureHoldRegister(lotID, operationNumber, postFlag, true,
                reasonCodeID, new ObjectIdentifier(), routeID, "FutureHold");
    }

    //future hold with singleTriggerFlag and postFlag
    public Response futureHoldRegisterBySpecLot(ObjectIdentifier lotID, String operationNumber, ObjectIdentifier routeID, boolean postFlag,boolean singleTriggerFlag){
        //【step1】getReason
        String reasonCode = "MGHL";
        ObjectIdentifier reasonCodeID = null;
        List<Infos.ReasonCodeAttributes> reasonCodeListByCategoryInqResult = (List<Infos.ReasonCodeAttributes>) this.getReasonInfo().getBody();
        for (Infos.ReasonCodeAttributes reasonCodeResult : reasonCodeListByCategoryInqResult){
            if (reasonCodeResult.getReasonCodeID().getValue().equals(reasonCode)){
                reasonCodeID = reasonCodeResult.getReasonCodeID();
            }
        }
        //【step2】futureHoldReq
        return this.futureHoldRegister(lotID, operationNumber, postFlag, singleTriggerFlag,
                reasonCodeID, new ObjectIdentifier(), routeID, "FutureHold");
    }

    public Response futureHoldRegisterBySpecLotWithReasonCode(ObjectIdentifier lotID, String operationNumber, ObjectIdentifier routeID, boolean postFlag,boolean singleTriggerFlag,String inputReasonCode){
        //【step1】getReason
        String reasonCode = inputReasonCode;
        ObjectIdentifier reasonCodeID = null;
        List<Infos.ReasonCodeAttributes> reasonCodeListByCategoryInqResult = (List<Infos.ReasonCodeAttributes>) this.getReasonInfo().getBody();
        for (Infos.ReasonCodeAttributes reasonCodeResult : reasonCodeListByCategoryInqResult){
            if (reasonCodeResult.getReasonCodeID().getValue().equals(reasonCode)){
                reasonCodeID = reasonCodeResult.getReasonCodeID();
            }
        }
        //【step2】futureHoldReq
        return this.futureHoldRegister(lotID, operationNumber, postFlag, singleTriggerFlag,
                reasonCodeID, new ObjectIdentifier(), routeID, "FutureHold");
    }

    public Response getFutureHoldListByKey(ObjectIdentifier lotID){
        Params.FutureHoldListInqParams futureHoldListInqParams = new Params.FutureHoldListInqParams();
        futureHoldListInqParams.setCount(9999);
        futureHoldListInqParams.setUser(getUser());
        Infos.FutureHoldSearchKey futureHoldSearchKey = new Infos.FutureHoldSearchKey();
        futureHoldListInqParams.setFutureHoldSearchKey(futureHoldSearchKey);
        futureHoldSearchKey.setLotID(lotID);
        SearchCondition searchCondition = new SearchCondition();
        futureHoldListInqParams.setSearchCondition(searchCondition);
        searchCondition.setPage(0);
        searchCondition.setSize(9999);
        searchCondition.setSortDirection(false);
        return processInqController.futureHoldListInq(futureHoldListInqParams);
    }

    public Response futureHoldCancelCase(ObjectIdentifier codeID,ObjectIdentifier lotID) {

        //【step1】call lotInfoInq to get the detail lot info
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Response response = lotGeneralTestCase.getLotInfoCase(lotIDs);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) response.getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();
        ObjectIdentifier routeID = lotOperationInfo.getRouteID();
        String operationNumber = lotOperationInfo.getOperationNumber();

        //【step2】call futureHold list function and then choose one Hold Data
        log.debug("【step2】call holdLotListInqCase function and then choose one Hold Data");
        Page<Infos.FutureHoldListAttributes> futureHoldListResult = (Page<Infos.FutureHoldListAttributes>) this.getFutureHoldListByKey(lotID).getBody();
        Infos.FutureHoldListAttributes listAttributes = futureHoldListResult.getContent().get(0);

        //【step3】call reasonCodeListByCategoryInqCase to get all release reason code and then choose one release reason code
        log.debug("【step3】call reasonCodeListByCategoryInqCase to get all release reason code and then choose one release reason code");
        String releaseReasonCode = "ReleaseHoldLot";
        Response response1 =  lotGeneralTestCase.getReasonListCase(releaseReasonCode);
        List<Infos.ReasonCodeAttributes>  reasonCodeListByCategoryInqResult = (List<Infos.ReasonCodeAttributes>)response1.getBody();
        Infos.ReasonCodeAttributes reasonCodeResult = reasonCodeListByCategoryInqResult.get(0);


        //【step4】call hold lot release function
        log.debug("【step4】call future hold cancel function");
        Params.FutureHoldCancelReqParams params = new Params.FutureHoldCancelReqParams();
        params.setUser(getUser());
        params.setLotID(lotID);
        params.setReleaseReasonCodeID(codeID);
        List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldReasonCodeID(listAttributes.getReasonCodeID());
        lotHoldReq.setHoldType(listAttributes.getHoldType());
        lotHoldReq.setHoldUserID(getUser().getUserID());
        lotHoldReq.setOperationNumber(operationNumber);
        lotHoldReq.setResponsibleOperationMark("");
        //lotHoldReq.setRelatedLotID(new ObjectIdentifier());  //【bear】the relatedLotID not equal the lotID
        lotHoldReq.setRouteID(routeID);

        holdReqList.add(lotHoldReq);
        params.setLotHoldList(holdReqList);
        Response result = processController.futureHoldCancelReq(params);
        Validations.isSuccessWithException(result);
        return result;
    }

}