package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.lot.LotController;
import com.fa.cim.dto.Params;
import com.fa.cim.newIntegration.common.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/5                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/12/5 11:18
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class BranchTestCase {

    @Autowired
    private LotController lotController;

    @Autowired
    private TestUtils testUtils;

    public Response subRouteBranchReqCase(Params.SubRouteBranchReqParams params){
        return lotController.subRouteBranchReq(params);
    }

    public Response branchCancelReqCase(Params.BranchCancelReqParams params){
        return lotController.branchCancelReq(params);
    }
}
