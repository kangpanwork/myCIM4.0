package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.lot.LotController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/4        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/4 14:40
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class ScrapCase {
    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private LotController lotController;

    @Autowired
    private ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    private STBCase stbCase;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public Infos.LotInfo scrap_norml() {
        //【step1】stb one process lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);
        return this.scrap_normal(lotID, null);
    }

    public Infos.LotInfo scrap_normal(ObjectIdentifier lotID, String reasonOperationNumber) {
        //【step2】scrap the wafer
        ObjectIdentifier reasonRouteID = new ObjectIdentifier("LAYER0MA.01");
        ObjectIdentifier reasonCodeID = new ObjectIdentifier("MISC");
        if (CimStringUtils.isEmpty(reasonOperationNumber)) {
            reasonOperationNumber = "1000.0100";
        }
        int scrapWaferCount = 2;
        TestInfos.ScrapInfo scrapInfo = new TestInfos.ScrapInfo(lotID, reasonRouteID, reasonCodeID, reasonOperationNumber, scrapWaferCount);
        this.scrap(scrapInfo);
        //【step3】lot Info
        Infos.LotInfo lotInfo = ((Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody()).getLotInfoList().get(0);
        return lotInfo;
    }

    public Response scrap(TestInfos.ScrapInfo scrapInfo) {
        ObjectIdentifier lotID = scrapInfo.getLotID();

        //【step1】get lot info
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Response response = lotGeneralTestCase.getLotInfoCase(lotIDs);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) response.getBody();
        ObjectIdentifier cassetteID = lotInfoInqResult.getLotListInCassetteInfo().getCassetteID();


        //【step2】get reason list
        final String codeCategory = "ScrapWafer";
        lotGeneralTestCase.getReasonListCase(codeCategory);

        //【step3】scrap wafer
        Params.ScrapWaferReqParams params = new Params.ScrapWaferReqParams();
        params.setUser(getUser());
        params.setLotID(lotID);
        params.setReasonOperationNumber(scrapInfo.getReasonOperationNumber());
        params.setReasonRouteID(scrapInfo.getReasonRouteID());
        params.setCassetteID(cassetteID);
        //params.setReasonOperationID(new ObjectIdentifier());
        List<Infos.ScrapWafers> scrapWafersList = new ArrayList<>();
        for (int i = 0, j = 0; i < scrapInfo.getScrapWaferCount() && j < CimArrayUtils.getSize(lotInfoInqResult.getWaferMapInCassetteInfoList()); j++) {
            Infos.WaferMapInCassetteInfo waferMapInCassetteInfo = lotInfoInqResult.getWaferMapInCassetteInfoList().get(j);
            if (!CimStringUtils.equals(waferMapInCassetteInfo.getScrapState(), "Active")) {
                continue;
            }
            Infos.ScrapWafers scrapWafers = new Infos.ScrapWafers();
            scrapWafers.setWaferID(waferMapInCassetteInfo.getWaferID());
            scrapWafers.setReasonCodeID(scrapInfo.getReasonCodeID());
            scrapWafersList.add(scrapWafers);
            i++;
        }
        params.setScrapWafers(scrapWafersList);
        return lotController.scrapWaferReq(params);
    }

    public Response waferScrappedHistoryInqCase(Params.WaferScrappedHistoryInqParams params) {
        return electronicInformationInqController.waferScrappedHistoryInq(params);
    }

}