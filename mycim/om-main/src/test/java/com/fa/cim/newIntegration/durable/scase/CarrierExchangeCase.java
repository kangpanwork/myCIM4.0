package com.fa.cim.newIntegration.durable.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.lot.LotInqController;
import com.fa.cim.controller.sort.SortController;
import com.fa.cim.controller.sort.SortInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.common.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>CarrierExchangeCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/2/26/026   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/2/26/026 10:51
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class CarrierExchangeCase {
    @Autowired
    private TestUtils testUtils;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private SortController sortController;

    @Autowired
    private LotInqController lotInqController;

    @Autowired
    private SortInqController sortInqController;

    @Autowired
    private RetCodeConfig retCodeConfig;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public ObjectIdentifier emptyID = ObjectIdentifier.build(null,null);

    public void PRC10_1_1WaferPositionChangeInLotStartScreen(){
        //【Step 1】Change the wafer of the carrier
        Params.WaferSlotmapChangeReqParams params = new Params.WaferSlotmapChangeReqParams();
        params.setUser(getUser());
        params.setEquipmentID(emptyID);
        params.setBNotifyToTCS(false);
        List<Infos.WaferTransfer> strWaferXferSeq = new ArrayList<>();
        Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
        strWaferXferSeq.add(waferTransfer);
        waferTransfer.setOriginalSlotNumber(1);
        waferTransfer.setDestinationSlotNumber(1);
        waferTransfer.setBDestinationCassetteManagedByOM(true);
        waferTransfer.setBOriginalCassetteManagedByOM(true);
        waferTransfer.setOriginalCassetteID(ObjectIdentifier.buildWithValue("CRUP0208"));
        waferTransfer.setDestinationCassetteID(ObjectIdentifier.buildWithValue("CRUP0255"));
        waferTransfer.setWaferID(ObjectIdentifier.build("RL000002.00.01","FRWAFER.66391876795456719"));
        params.setStrWaferXferSeq(strWaferXferSeq);
        sortController.waferSlotmapChangeReq(params);
        //【step 2】Check the result
        Params.LotListByCarrierInqParams lotparams = new Params.LotListByCarrierInqParams();
        lotparams.setUser(getUser());
        lotparams.setCassetteID(ObjectIdentifier.buildWithValue("CRUP0255"));
        Response response = lotInqController.lotListByCarrierInq(lotparams);
        Results.LotListByCarrierInqResult result = (Results.LotListByCarrierInqResult) response.getBody();
        String waferID = result.getWaferMapInCassetteInfoList().get(0).getWaferID().getValue();
        if (!CimStringUtils.equals(waferID,"RL000002.00.01")){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void PRC10_1_2CheckAfterWaferPositionChange(){
        //【Step 1】Change the wafer of the carrier
        Params.WaferSlotmapChangeReqParams params = new Params.WaferSlotmapChangeReqParams();
        params.setUser(getUser());
        params.setEquipmentID(emptyID);
        params.setBNotifyToTCS(false);
        List<Infos.WaferTransfer> strWaferXferSeq = new ArrayList<>();
        Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
        strWaferXferSeq.add(waferTransfer);
        waferTransfer.setOriginalSlotNumber(1);
        waferTransfer.setDestinationSlotNumber(1);
        waferTransfer.setBDestinationCassetteManagedByOM(true);
        waferTransfer.setBOriginalCassetteManagedByOM(true);
        waferTransfer.setOriginalCassetteID(ObjectIdentifier.buildWithValue("CRUP0208"));
        waferTransfer.setDestinationCassetteID(ObjectIdentifier.buildWithValue("CRUP0255"));
        waferTransfer.setWaferID(ObjectIdentifier.build("RL000002.00.01","FRWAFER.66391876795456719"));
        params.setStrWaferXferSeq(strWaferXferSeq);
        sortController.waferSlotmapChangeReq(params);
        //【step 2】Check the result
        Params.LotListByCarrierInqParams lotparams = new Params.LotListByCarrierInqParams();
        lotparams.setUser(getUser());
        lotparams.setCassetteID(ObjectIdentifier.buildWithValue("CRUP0208"));
        Response response = lotInqController.lotListByCarrierInq(lotparams);
        Results.LotListByCarrierInqResult result = (Results.LotListByCarrierInqResult) response.getBody();
        List<Infos.WaferMapInCassetteInfo> infoList = result.getWaferMapInCassetteInfoList();
        if (!CimObjectUtils.isEmpty(infoList)){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void PRC10_1_3Justout(){
        //【Step 1】just out
        Params.WaferSlotmapChangeReqParams params = new Params.WaferSlotmapChangeReqParams();
        params.setUser(getUser());
        params.setEquipmentID(emptyID);
        params.setBNotifyToTCS(false);
        List<Infos.WaferTransfer> strWaferXferSeq = new ArrayList<>();
        Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
        strWaferXferSeq.add(waferTransfer);
        waferTransfer.setOriginalSlotNumber(1);
        waferTransfer.setDestinationSlotNumber(0);
        waferTransfer.setBDestinationCassetteManagedByOM(false);
        waferTransfer.setBOriginalCassetteManagedByOM(true);
        waferTransfer.setOriginalCassetteID(ObjectIdentifier.buildWithValue("CRUP0208"));
        waferTransfer.setDestinationCassetteID(ObjectIdentifier.buildWithValue("JustOut"));
        waferTransfer.setWaferID(ObjectIdentifier.build("RL000002.00.01","FRWAFER.66391876795456719"));
        params.setStrWaferXferSeq(strWaferXferSeq);
        sortController.waferSlotmapChangeReq(params);
        //【step 2】Check the result

        Params.LotListByCarrierInqParams lotparams = new Params.LotListByCarrierInqParams();
        lotparams.setUser(getUser());
        lotparams.setCassetteID(ObjectIdentifier.buildWithValue("CRUP0208"));
        Response response = lotInqController.lotListByCarrierInq(lotparams);
        Results.LotListByCarrierInqResult result = (Results.LotListByCarrierInqResult) response.getBody();
        List<Infos.WaferMapInCassetteInfo> infoList = result.getWaferMapInCassetteInfoList();
        if (!CimObjectUtils.isEmpty(infoList)){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void PRC10_1_19SomeOfWaferPositionChange(){
        //【Step 1】just out
        Params.WaferSlotmapChangeReqParams params = new Params.WaferSlotmapChangeReqParams();
        params.setUser(getUser());
        params.setEquipmentID(emptyID);
        params.setBNotifyToTCS(false);
        List<Infos.WaferTransfer> strWaferXferSeq = new ArrayList<>();
        Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
        strWaferXferSeq.add(waferTransfer);
        waferTransfer.setOriginalSlotNumber(1);
        waferTransfer.setDestinationSlotNumber(1);
        waferTransfer.setBDestinationCassetteManagedByOM(true);
        waferTransfer.setBOriginalCassetteManagedByOM(true);
        waferTransfer.setOriginalCassetteID(ObjectIdentifier.buildWithValue("CRUP0095"));
        waferTransfer.setDestinationCassetteID(ObjectIdentifier.buildWithValue("CRUP0255"));
        waferTransfer.setWaferID(ObjectIdentifier.buildWithValue("NP000407.00A.01"));
        params.setStrWaferXferSeq(strWaferXferSeq);
        try {
            sortController.waferSlotmapChangeReq(params);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=2916){
                throw new ServiceException(retCodeConfig.getError());
            }
        }


    }
}
