package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.controller.interfaces.lot.ILotController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/20          ********            lightyh                create file
 *
 * @author: light
 * @date: 2019/12/20 10:34
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class DynamicBranchTestCase {

    @Autowired
    private ILotController lotController;

    @Autowired
    private CommonTestCase commonTestCase;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        return user;
    }

    public void dynmicBranchReq(String currentOperationNumber, ObjectIdentifier currentRouteID, ObjectIdentifier lotID, String returnOperationNumber){
        List<Infos.DynamicRouteList> dynamicRouteLists = (List<Infos.DynamicRouteList>) commonTestCase.dynamicPathListInqCase("Branch", "%").getBody();
        Params.BranchReqParams branchReqParams = new Params.BranchReqParams();
        branchReqParams.setUser(getUser());
        Infos.BranchReq branchReq = new Infos.BranchReq();
        branchReqParams.setBranchReq(branchReq);
        branchReq.setBDynamicRoute(true);
        branchReq.setCurrentOperationNumber(currentOperationNumber);
        branchReq.setCurrentRouteID(currentRouteID);
        branchReq.setLotID(lotID);
        branchReq.setReturnOperationNumber(returnOperationNumber);
        branchReq.setSubRouteID(dynamicRouteLists.get(0).getRouteID());
        lotController.branchReq(branchReqParams);
    }
}