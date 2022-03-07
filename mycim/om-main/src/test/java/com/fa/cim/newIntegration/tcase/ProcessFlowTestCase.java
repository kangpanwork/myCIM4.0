package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.lot.ILotController;
import com.fa.cim.controller.interfaces.lot.ILotInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.newIntegration.common.TestUtils;
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
 * 2019/12/12          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/12/12 16:14
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class ProcessFlowTestCase {
    @Autowired
    private ILotInqController lotInqController;
    @Autowired
    private ILotController lotController;
    @Autowired
    private TestUtils testUtils;

    public List<Infos.ConnectedRouteList> multiPathListInqCase(ObjectIdentifier cassetteID, ObjectIdentifier lotID) {
        Params.MultiPathListInqParams multiPathListInqParams = new Params.MultiPathListInqParams();
        multiPathListInqParams.setUser(testUtils.getUser());
        multiPathListInqParams.setCassetteID(cassetteID);
        multiPathListInqParams.setLotID(lotID);
        return (List<Infos.ConnectedRouteList>) lotInqController.multiPathListInq(multiPathListInqParams).getBody();
    }

    public Response branchWithHoldReleaseReqCase(Infos.BranchReq branchReq, ObjectIdentifier releaseReasonCodeID, List<Infos.LotHoldReq> lotHoldReqs) {
        Params.BranchWithHoldReleaseReqParams params = new Params.BranchWithHoldReleaseReqParams();
        params.setUser(testUtils.getUser());
        params.setBranchReq(branchReq);
        params.setReleaseReasonCodeID(releaseReasonCodeID);
        params.setStrLotHoldReleaseReqList(lotHoldReqs);
        return lotController.branchWithHoldReleaseReq(params);
    }
}