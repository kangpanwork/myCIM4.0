package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.bank.scase.VendorLotReceiveCase;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import com.fa.cim.newIntegration.tcase.LotHoldTestCase;
import com.fa.cim.newcore.exceptions.CoreFrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/9/2 13:46
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotHoldCase {

    @Autowired
    private VendorLotReceiveCase vendorLotReceiveCase;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private LotHoldTestCase lotHoldTestCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private RetCodeConfig retCodeConfig;

    public Response VenderLot_Todo_LotHold() {
        //【step1】vendor lot receive to get new lot id
        Response response = vendorLotReceiveCase.VendorLotReceive_AssignLotID();

        Results.VendorLotReceiveReqResult body = (Results.VendorLotReceiveReqResult) response.getBody();
        String lotID = body.getCreatedLotID();//lotID
        final String reasonCode = "SOHL";
        final String reasonableOperation = "P";
        //【step2】lotHold
        return this.lotHold(lotID,reasonCode,reasonableOperation);
    }

    public Response lotHold(String lotID,String reasonCode, String reasonableOperation){
        return this.lotHold(new ObjectIdentifier(lotID), reasonCode, reasonableOperation);
    }
    public Response lotHold(ObjectIdentifier lotID,String reasonCode, String reasonableOperation){
        TestInfos.LotHoldInfo lotHoldInfo = new TestInfos.LotHoldInfo();
        lotHoldInfo.setLotID(lotID);
        lotHoldInfo.setReasonCode(reasonCode);
        lotHoldInfo.setReasonableOperation(reasonableOperation);
        return this.lotHold(lotHoldInfo);
    }
    public Response lotHold(TestInfos.LotHoldInfo lotHoldInfo) {
        ObjectIdentifier lotID = lotHoldInfo.getLotID();
        String reasonCode = lotHoldInfo.getReasonCode();
        String reasonableOperation = lotHoldInfo.getReasonableOperation();
        //【step1】enter the "lot ID" search the lot info
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Response response = lotGeneralTestCase.getLotInfoCase(lotIDs);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult)response.getBody();
        //just get the first one
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();
        ObjectIdentifier routeID = lotOperationInfo.getRouteID();
        String operationNumber = lotOperationInfo.getOperationNumber();


        //【step2】check reason list
        final String codeCategory = "HoldLot";
        lotGeneralTestCase.getReasonListCase(codeCategory);
        //【step3】select reasonCode and resonableOperation and confirm
        Response result = null;
        try {
            result = lotHoldTestCase.holdLotReqCase(lotID.getValue(), reasonCode, reasonableOperation, routeID, operationNumber, codeCategory);
        }catch (Exception exception){
            Boolean experssion = false;
            if (exception instanceof ServiceException){
                ServiceException exception1 = (ServiceException) exception;
                if (exception1.getCode() == retCodeConfig.getInvalidLotStat().getCode()){
                    experssion = true;
                }
            }
            if (exception instanceof CoreFrameworkException){
                CoreFrameworkException exception1 = (CoreFrameworkException) exception;
                if (exception1.getCoreCode().getCode() == retCodeConfig.getDuplicateRecord().getCode()){
                    experssion = true;
                }
            }
            if (!experssion){
                throw exception;
            }
            Validations.assertCheck(experssion, exception.getMessage());
        }
        return result;
    }


    public Infos.LotInfo STB_After_LotHold_On_Current() {
        //【step1】stb
        Response response = stbCase.STB_PreparedLot();
        Results.WaferLotStartReqResult body = (Results.WaferLotStartReqResult) response.getBody();
        ObjectIdentifier lotID = body.getLotID();

        final String reasonCode = "SOHL";
        final String reasonableOperation = "C";
        //【step2】lotHold
        this.lotHold(lotID.getValue(),reasonCode,reasonableOperation);
        //【step3】lot info
        Infos.LotInfo lotInfo = ((Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody()).getLotInfoList().get(0);
        return lotInfo;
    }

    public Infos.LotInfo lotHoldOnCurrent(ObjectIdentifier cassetteID){
        //【step1】get lot info by cassetteID
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfosByCassette(cassetteID);
        final String reasonCode = "SOHL";
        final String reasonableOperation = "C";
        //【step2】lotHold
        ObjectIdentifier lotID = lotInfoList.get(0).getLotBasicInfo().getLotID();
        this.lotHold(lotID.getValue(),reasonCode,reasonableOperation);
        //【step3】lot info
        Infos.LotInfo lotInfo = ((Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody()).getLotInfoList().get(0);
        return lotInfo;
    }

    public void SameLot_Do_Hold_ForTwice_Using_Same_ReasonCode() {
        //【step1】stb
        Response response = stbCase.STB_PreparedLot();
        Results.WaferLotStartReqResult body = (Results.WaferLotStartReqResult) response.getBody();
        ObjectIdentifier lotID = body.getLotID();

        final String reasonCode = "SOHL";
        final String reasonableOperation = "C";
        //【step2】lotHold
        this.lotHold(lotID.getValue(),reasonCode,reasonableOperation);
        try {
            this.lotHold(lotID.getValue(),reasonCode,reasonableOperation);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getExistSameHold(), e.getCode())){
                Assert.isTrue(true, "test success");
            }
        }
    }

    public Response SameLot_Do_Hold_FowTwice_Using_Different_ReasonCode() {
        //【step1】stb
        Response response = stbCase.STB_PreparedLot();
        Results.WaferLotStartReqResult body = (Results.WaferLotStartReqResult) response.getBody();
        ObjectIdentifier lotID = body.getLotID();

        final String reasonCode = "SOHL";
        final String reasonableOperation = "C";
        //【step2】lotHold
        this.lotHold(lotID.getValue(),reasonCode,reasonableOperation);

        final String reasonCode1 = "SOOR";
        final String reasonableOperation1 = "C";
        return this.lotHold(lotID.getValue(),reasonCode1,reasonableOperation1);
    }
}
