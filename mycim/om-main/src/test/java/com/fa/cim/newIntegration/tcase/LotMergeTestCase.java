package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.controller.lot.LotController;
import com.fa.cim.dto.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/12       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/12 9:47
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotMergeTestCase {

    @Autowired
    private LotController lotController;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public Response mergeLotReq(ObjectIdentifier parentLotID, ObjectIdentifier childLotID){
        Params.MergeLotReqParams mergeLotReqParams = new Params.MergeLotReqParams();
        mergeLotReqParams.setParentLotID(parentLotID);
        mergeLotReqParams.setChildLotID(childLotID);
        mergeLotReqParams.setUser(getUser());
        return lotController.mergeLotReq(mergeLotReqParams);
    }

    public Response mergeLotNotOnPfReq(ObjectIdentifier parentLotID, ObjectIdentifier childLotID) {
        Params.MergeLotNotOnPfReqParams params = new Params.MergeLotNotOnPfReqParams();
        params.setUser(getUser());
        params.setParentLotID(parentLotID);
        params.setChildLotID(childLotID);
        return lotController.mergeLotNotOnPfReq(params);
    }
}