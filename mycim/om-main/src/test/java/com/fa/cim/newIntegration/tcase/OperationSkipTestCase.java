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
 * 2019/9/10       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/10 16:54
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class OperationSkipTestCase {
    @Autowired
    private LotController lotController;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public Response operationSkip(Params.SkipReqParams skipReqParams){
        skipReqParams.setUser(getUser());
        return lotController.skipReq(skipReqParams);
    }

    public Response forceOperationSkip(Params.SkipReqParams skipReqParams){
        skipReqParams.setUser(getUser());
        return lotController.forceSkipReq(skipReqParams);
    }
}