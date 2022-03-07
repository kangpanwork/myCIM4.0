package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.controller.lot.LotController;
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
 * 2019/9/11       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/11 18:00
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotSplitTestCase {

    @Autowired
    private LotController lotController;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public Response splitLotReq(List<ObjectIdentifier> childWaferIDs, boolean branchingRouteSpecifyFlag,
                                     boolean futureMergeFlag, String mergedOperationNumber, ObjectIdentifier mergedRouteID,
                                     ObjectIdentifier parentLotID, ObjectIdentifier subRouteID){
        Params.SplitLotReqParams splitLotReqParams = new Params.SplitLotReqParams();
        splitLotReqParams.setBranchingRouteSpecifyFlag(false);
        splitLotReqParams.setChildWaferIDs(childWaferIDs);
        splitLotReqParams.setFutureMergeFlag(futureMergeFlag);
        splitLotReqParams.setMergedOperationNumber(mergedOperationNumber);
        splitLotReqParams.setMergedRouteID(mergedRouteID);
        splitLotReqParams.setParentLotID(parentLotID);
        splitLotReqParams.setSubRouteID(subRouteID);
        splitLotReqParams.setUser(getUser());
        return lotController.splitLotReq(splitLotReqParams);
    }
}