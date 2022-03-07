package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
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
 * 2019/12/3          ********            lightyh                create file
 *
 * @author: light
 * @date: 2019/12/3 16:53
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class ReworkTestCase {

    @Autowired
    private LotController lotController;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        return user;
    }

    public Response reworkWholeLotReq(Params.ReworkWholeLotReqParams reworkWholeLotReqParams){
        reworkWholeLotReqParams.setUser(getUser());
        return lotController.reworkWholeLotReq(reworkWholeLotReqParams);
    }

    public Response reworkWithHoldReleaseReq(Params.ReworkWithHoldReleaseReqParams params){
        params.setUser(getUser());
        return lotController.reworkWithHoldReleaseReq(params);
    }

    public Response reworkCancelReq(Params.ReworkCancelReqParams params) {
        params.setUser(getUser());
        return lotController.reworkCancelReq(params);
    }

    public Response partialReworkReq(Params.PartialReworkReqParams params){
        params.setUser(getUser());
        params.setClaimMemo("");
        return lotController.partialReworkReq(params);
    }

    public Response partialReworkWithHoldReleaseReq(Params.PartialReworkWithHoldReleaseReqParams params){
        params.setUser(getUser());
        return lotController.partialReworkWithHoldReleaseReq(params);
    }

    public Response partialReworkWithOutHoldReleaseReq(Params.PartialReworkWithoutHoldReleaseReqParams params){
        params.setUser(getUser());
        return lotController.partialReworkWithoutHoldReleaseReq(params);
    }

    public Response partialReworkCancelReq(Params.PartialReworkCancelReqParams params){
        params.setUser(getUser());
        params.setClaimMemo("");
        return lotController.partialReworkCancelReq(params);
    }

    public Response reworkReq(Params.ReworkReqParams params){
        params.setUser(getUser());
        return lotController.reworkReq(params);
    }
}