package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.FutureHoldTestCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
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
 * 2019/9/17                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/9/17 17:44
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class FutureHoldCancelCase {

    @Autowired
    private STBCase stbCase;

    @Autowired
    private FutureHoldTestCase futureHoldTestCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;


    public void futureHoldCancel_Normal() {
        //input param
        final Boolean postFlag = true;
        final Boolean singleTriggerFlag = true;

        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();

        //【step2】get lot info
        List<ObjectIdentifier> lotIds = new ArrayList<>();
        lotIds.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfo1 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIds).getBody();

        //【step3】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(0);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        //【step3】future hold
        Response response = futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, postFlag, singleTriggerFlag);

        //【step4】get reason code
        final String codeCategory = "FutureHoldCancel";
        List<Infos.ReasonCodeAttributes> reasonCodeListByCategoryInqResult = (List<Infos.ReasonCodeAttributes>)lotGeneralTestCase.getReasonListCase(codeCategory).getBody();

        ObjectIdentifier codeID = reasonCodeListByCategoryInqResult.get(0).getReasonCodeID();

        //【step5】future hold cancel
        this.futureHoldCancel(codeID,waferLotStartReqResult.getLotID());
    }

    private void futureHoldCancel(ObjectIdentifier codeID,ObjectIdentifier lotID) {
        futureHoldTestCase.futureHoldCancelCase(codeID,lotID);
    }
}
