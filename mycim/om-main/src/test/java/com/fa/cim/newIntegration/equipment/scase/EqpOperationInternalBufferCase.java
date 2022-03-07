package com.fa.cim.newIntegration.equipment.scase;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.tcase.ElectronicInformationTestCase;
import com.fa.cim.newIntegration.tcase.EqpInfoForInternalBufferTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/9       ********              Jerry             create file
 *
 * @author: Jerry
 * @date: 2019/9/9 15:44
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@Service
@Slf4j
public class EqpOperationInternalBufferCase {

    public static final String EQUIPMENTID = "1WSF01";
    public static final String PORT = "P2";

    @Autowired
    private EqpInfoForInternalBufferTestCase eqpInfoForInternalBufferTestCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    Results.EqpInfoForIBInqResult eqpInfoForIBInqResult = null;
    Results.WhatNextLotListResult whatNextLotListResult = null;
    Results.LotListByCarrierInqResult lotListByCarrierInqResult = null;
    Results.LotInfoInqResult lotInfoInqResult = null;
    Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult = null;

    public void EqpOperationInternalBuffer() {
        // eqp info -> what's Next -> startReservtion -> loading -> move to self -> ope start -> ope comp -> un loading
        //eqp info
        Response eqpInfoResponse = electronicInformationTestCase.EqpInfoForInternalBufferCase(EQUIPMENTID);
        eqpInfoForIBInqResult = (Results.EqpInfoForIBInqResult)eqpInfoResponse.getBody();

        //what's Next
        Response whatNextResponse = eqpInfoForInternalBufferTestCase.WhatNextForInternalBufferCase(EQUIPMENTID);
        whatNextLotListResult = (Results.WhatNextLotListResult)whatNextResponse.getBody();

        ObjectIdentifier cassetteID = whatNextLotListResult.getWhatNextAttributesPage().getContent().get(0).getCassetteID();

        //LotListByCarrierInq -> lot info
        Response lotListByCarrierInqResponse = eqpInfoForInternalBufferTestCase.LotListByCarrierInqCase(cassetteID);
        lotListByCarrierInqResult = (Results.LotListByCarrierInqResult)lotListByCarrierInqResponse.getBody();

         Response lotInfoInqResponse = eqpInfoForInternalBufferTestCase.LotInfoInqCase(lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList());
        lotInfoInqResult = (Results.LotInfoInqResult)lotInfoInqResponse.getBody();

        //startReservtion  入参拼接
        List<Infos.LotInCassette> lotInCassettes = new ArrayList<>();
        for (int i = 0; i < lotInfoInqResult.getLotInfoList().size(); i++) {
            Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
            lotInCassette.setMoveInFlag(true);
            lotInCassette.setMonitorLotFlag(false);

            lotInCassette.setLotID(lotInfoInqResult.getLotInfoList().get(i).getLotBasicInfo().getLotID());
            List<Infos.LotWafer> lotWafers = new ArrayList<>();
            for (int j = 0; j < CimArrayUtils.getSize(lotInfoInqResult.getLotInfoList().get(i).getLotWaferAttributesList()); j++) {
                Infos.LotWaferAttributes lotWaferAttributes = lotInfoInqResult.getLotInfoList().get(i).getLotWaferAttributesList().get(j);
                Infos.LotWafer lotWafer = new Infos.LotWafer();
                lotWafer.setWaferID(lotWaferAttributes.getWaferID());
                lotWafer.setSlotNumber(lotWaferAttributes.getSlotNumber().longValue());
                lotWafer.setControlWaferFlag(true);
                lotWafer.setProcessJobExecFlag(false);
                lotWafer.setParameterUpdateFlag(false);
                lotWafers.add(lotWafer);
            }
            lotInCassette.setLotWaferList(lotWafers);
            lotInCassette.setStartRecipe(new Infos.StartRecipe());
            lotInCassette.setStartOperationInfo(new Infos.StartOperationInfo());
            lotInCassettes.add(lotInCassette);
        }

        List<Infos.StartCassette> startReservtionStartCassettes = new ArrayList<>();
        Infos.StartCassette startReservtionStartCassette = new Infos.StartCassette();
        startReservtionStartCassette.setLotInCassetteList(lotInCassettes);
        startReservtionStartCassette.setLoadSequenceNumber(0L);
        startReservtionStartCassette.setCassetteID(cassetteID);
        startReservtionStartCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);

        startReservtionStartCassettes.add(startReservtionStartCassette);
        Response lotForStartReservtionResponse = eqpInfoForInternalBufferTestCase.lotForStartReservtionForInternalBufferCase(EQUIPMENTID, startReservtionStartCassettes);
        lotsMoveInReserveInfoInqResult = (Results.LotsMoveInReserveInfoInqResult)lotForStartReservtionResponse.getBody();


        List<Infos.StartCassette> strStartCassette = lotsMoveInReserveInfoInqResult.getStrStartCassette();
        for (Infos.StartCassette startCassette : strStartCassette) {
            startCassette.setLoadPortID(new ObjectIdentifier(PORT));
        }

        Response startReservtionResponse = eqpInfoForInternalBufferTestCase.startReservtionForInternalBufferCase(EQUIPMENTID, strStartCassette);

        //loading
        Response loadingResponse = eqpInfoForInternalBufferTestCase.loadingForInternalBufferCase(EQUIPMENTID, cassetteID, PORT);

        //move to self
        //Response moveToSelfResponse = eqpInfoForInternalBufferTestCase.moveToSelfForInternalBufferCase(EQUIPMENTID, cassetteID, PORT);

        //ope start
        List<Infos.StartCassette> opeStartStartCassettes = new ArrayList<>();

        String controlJobID = "";
        Boolean processjobPausrFlag = false;
        //Response opeStartResponse = eqpInfoForInternalBufferTestCase.opeStartForInternalBufferCase(EQUIPMENTID, opeStartStartCassettes, controlJobID, processjobPausrFlag);

        //ope comp
        //Response opeCompResponse = eqpInfoForInternalBufferTestCase.opeCompForInternalBufferCase(EQUIPMENTID, controlJobID);

        //un loading
        //Response unLoadingResponse = eqpInfoForInternalBufferTestCase.unLoadingForInternalBufferCase(EQUIPMENTID, cassetteID, PORT);

    }


}