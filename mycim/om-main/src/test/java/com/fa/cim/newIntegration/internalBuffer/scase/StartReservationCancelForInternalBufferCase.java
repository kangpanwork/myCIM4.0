package com.fa.cim.newIntegration.internalBuffer.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.dispatch.DispatchController;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.interfaces.lot.ILotInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
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
 * 2019/11/26        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/11/26 16:18
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class StartReservationCancelForInternalBufferCase {
    @Autowired
    private StartReservationForInternalBufferCase startReservationForInternalBufferCase;

    @Autowired
    ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    private DispatchController dispatchController;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private LoadForInternalBufferCase loadForInternalBufferCase;

    @Autowired
    private ILotInqController lotInqController;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public Response startReservationCancel_SingleLot() {
        //[step1]make one product lot and skip to internal buffer eqp
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");
        ObjectIdentifier loadPortID = new ObjectIdentifier("P1");
        ObjectIdentifier unloadPortID = new ObjectIdentifier("P1");
        String loadPurposeType = "Process Lot";

        Response response = startReservationForInternalBufferCase.startReservation_SingleLot();
        Validations.isSuccessWithException(response);
        ObjectIdentifier controlJobID = (ObjectIdentifier) response.getBody();

        //[step2]einfo/lot_list_in_control_job/inq
        Params.LotListByCJInqParams lotListByCJInqParams = new Params.LotListByCJInqParams();
        lotListByCJInqParams.setUser(getUser());
        List<ObjectIdentifier> controlJobIDs = new ArrayList<>();
        controlJobIDs.add(controlJobID);
        lotListByCJInqParams.setControlJobIDs(controlJobIDs);
        Response response1 = lotInqController.lotListByCJInq(lotListByCJInqParams);
        Validations.isSuccessWithException(response1);
        List<Infos.ControlJobInfo> controlJobInfoList = (List<Infos.ControlJobInfo>) response1.getBody();
        Validations.check(CimArrayUtils.isEmpty(controlJobInfoList), "controlJobInfoList must be not null!");

        Params.MoveInReserveCancelForIBReqParams moveInReserveCancelForIBReqParams = new Params.MoveInReserveCancelForIBReqParams();
        moveInReserveCancelForIBReqParams.setUser(getUser());
        moveInReserveCancelForIBReqParams.setControlJobID(controlJobID);
        moveInReserveCancelForIBReqParams.setEquipmentID(equipmentID);
        Response response2 = dispatchController.moveInReserveCancelForIBReq(moveInReserveCancelForIBReqParams);
        Validations.isSuccessWithException(response2);
        return response2;
    }

    public Response startReservationCancel_AfterLoading() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");
        //【step1】make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step3】loading
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(equipmentID);
        loadForInternalBufferInfo.setLoadPurposeType("Process Lot");
        loadForInternalBufferInfo.setLotID(lotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        loadForInternalBufferInfo.setNeedStartReserved(true);
        loadForInternalBufferCase.load(loadForInternalBufferInfo);

        //【ste4】get the control job ID
        ObjectIdentifier controlJobID = testUtils.getControlJobIDByLotID(lotID);
        Validations.check(CimObjectUtils.isEmpty(controlJobID), "there is no control job when after start reservation...");

        //【step5】make start reservation cancel
        //【step5-1】einfo/lot_list_in_control_job/inq
        Params.LotListByCJInqParams lotListByCJInqParams = new Params.LotListByCJInqParams();
        lotListByCJInqParams.setUser(getUser());
        List<ObjectIdentifier> controlJobIDs = new ArrayList<>();
        controlJobIDs.add(controlJobID);
        lotListByCJInqParams.setControlJobIDs(controlJobIDs);
        Response response1 = lotInqController.lotListByCJInq(lotListByCJInqParams);
        Validations.isSuccessWithException(response1);
        List<Infos.ControlJobInfo> controlJobInfoList = (List<Infos.ControlJobInfo>) response1.getBody();
        Validations.check(CimArrayUtils.isEmpty(controlJobInfoList), "controlJobInfoList must be not null!");

        //【step5-2】start reservation cancel
        Params.MoveInReserveCancelForIBReqParams moveInReserveCancelForIBReqParams = new Params.MoveInReserveCancelForIBReqParams();
        moveInReserveCancelForIBReqParams.setUser(getUser());
        moveInReserveCancelForIBReqParams.setControlJobID(controlJobID);
        moveInReserveCancelForIBReqParams.setEquipmentID(equipmentID);
        Response response2 = dispatchController.moveInReserveCancelForIBReq(moveInReserveCancelForIBReqParams);
        return response2;
    }
}