package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import com.fa.cim.newIntegration.tcase.LotHoldTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/9/3 10:29
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotHoldReleaseCase {

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private LotHoldCase lotHoldCase;

    @Autowired
    private LotHoldTestCase lotHoldTestCase;


    public Response Multi_HoldList_ToDo_HoldRelease() {
        //【step1】stb
        Response response = stbCase.STB_PreparedLot();
        Results.WaferLotStartReqResult body = (Results.WaferLotStartReqResult) response.getBody();
        ObjectIdentifier lotID = body.getLotID();

        final String reasonCode = "SOHL";
        final String reasonableOperation = "C";
        //【step2】lotHold
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);

        //【step3】lotHoldRelease
        return this.LotHoldRelease(lotID);
    }

    private Response LotHoldRelease(ObjectIdentifier lotID) {
        //【step1】 check lot hold list
        lotHoldTestCase.getLotHoldListCase(lotID);
        //【step2】 exec lot hold release
        return lotHoldTestCase.lotHoldReleaseReqCase(lotID);
    }
}
