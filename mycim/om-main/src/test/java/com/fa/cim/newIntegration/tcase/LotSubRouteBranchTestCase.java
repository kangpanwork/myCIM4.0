package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.controller.lot.LotController;
import com.fa.cim.dto.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/9          ********            lightyh                create file
 *
 * @author: light
 * @date: 2019/12/9 10:30
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class LotSubRouteBranchTestCase {

    @Autowired
    private LotController lotController;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public void subRouteBranchReq(ObjectIdentifier lotID, ObjectIdentifier currentRouteID, String currentOperationNumber, ObjectIdentifier subRouteID, String returnOperationNumber){
        Params.SubRouteBranchReqParams subRouteBranchReqParams = new Params.SubRouteBranchReqParams();
        subRouteBranchReqParams.setUser(getUser());
        subRouteBranchReqParams.setCurrentOperationNumber(currentOperationNumber);
        subRouteBranchReqParams.setCurrentRouteID(currentRouteID);
        subRouteBranchReqParams.setLotID(lotID);
        subRouteBranchReqParams.setReturnOperationNumber(returnOperationNumber);
        subRouteBranchReqParams.setSubRouteID(subRouteID);
        lotController.subRouteBranchReq(subRouteBranchReqParams);
    }

    public void branchCancelReq(ObjectIdentifier lotID, ObjectIdentifier currentRouteID, String currentOperationNumber){
        Params.BranchCancelReqParams branchCancelReqParams = new Params.BranchCancelReqParams();
        branchCancelReqParams.setUser(getUser());
        branchCancelReqParams.setCurrentRouteID(currentRouteID);
        branchCancelReqParams.setCurrentOperationNumber(currentOperationNumber);
        branchCancelReqParams.setLotID(lotID);
        lotController.branchCancelReq(branchCancelReqParams);
    }
}