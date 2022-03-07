package com.fa.cim.newIntegration.durable.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.durable.DurableController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>DurableStatusSubStatusChangeCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/4/004   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/4/004 10:42
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class DurableStatusSubStatusChangeCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private DurableController durableController;

    @Autowired
    private DurableInqController durableInqController;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

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
    public ObjectIdentifier normalUse = ObjectIdentifier.build("NORMAL_USE",null);
    public ObjectIdentifier Cleaning = ObjectIdentifier.build("Cleaning",null);
    public ObjectIdentifier nomalScrap = ObjectIdentifier.build("Nomal_Scrap",null);
    public ObjectIdentifier transit = ObjectIdentifier.build("Transit",null);
    public ObjectIdentifier carrierID = ObjectIdentifier.buildWithValue("CRUPJOJO");

    public void DUR_2_3_1_CarrierStatusAvailableToNotAvailable(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form notAvailable to Available
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step3】 change Carrier form Available to notAvailable
        this.statusChange("NOTAVAILABLE",Cleaning,"AVAILABLE",normalUse);
        //【Step4】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"NOTAVAILABLE"),retCodeConfig.getError());
    }

    public void DUR_2_3_2_CarrierStatusNotAvailableToAvailable(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form notAvailable to Available
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step3】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"AVAILABLE"),retCodeConfig.getError());
    }

    public void DUR_2_3_3_CarrierStatusAvailableToScrapped(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form notAvailable to Available
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step3】 change Carrier form Available to Scrapped
        this.statusChange("SCRAPPED",nomalScrap,"AVAILABLE",normalUse);
        //【Step4】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"SCRAPPED"),retCodeConfig.getError());
    }

    public void DUR_2_3_4_CarrierStatusScrapedToAvailable(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form notAvailable to Available
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step3】 change Carrier form Available to Scrapped
        this.statusChange("SCRAPPED",nomalScrap,"AVAILABLE",normalUse);
        //【Step4】 change Carrier form Scrapped to Available
        this.statusChange("AVAILABLE",normalUse,"SCRAPPED",nomalScrap);
        //【Step5】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"AVAILABLE"),retCodeConfig.getError());
    }

    public void DUR_2_3_5_CarrierStatusScrappedToNotAvailable(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form NOTAVAILABLE to SCRAPPED
        this.statusChange("SCRAPPED",nomalScrap,"NOTAVAILABLE",emptyID);
        //【Step3】 change Carrier form SCRAPPED to NOTAVAILABLE
        this.statusChange("NOTAVAILABLE",Cleaning,"SCRAPPED",nomalScrap);
        //【Step4】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"NOTAVAILABLE"),retCodeConfig.getError());
    }

    public void DUR_2_3_6_CarrierStatusNotAvailableToScrapped(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form NOTAVAILABLE to SCRAPPED
        this.statusChange("SCRAPPED",nomalScrap,"NOTAVAILABLE",emptyID);
        //【Step3】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"SCRAPPED"),retCodeConfig.getError());
    }

    public void DUR_2_3_7_CarrierStatusAvailableToInUse(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form NOTAVAILABLE to AVAILABLE
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step3】 change Carrier form AVAILABLE to InUse
        this.statusChange("INUSE",transit,"AVAILABLE",normalUse);
        //【Step4】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"INUSE"),retCodeConfig.getError());
    }

    public void DUR_2_3_8_CarrierStatusInUseToAvailable(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form NOTAVAILABLE to AVAILABLE
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step3】 change Carrier form AVAILABLE to InUse
        this.statusChange("INUSE",transit,"AVAILABLE",normalUse);
        //【Step4】 change Carrier form InUse to AVAILABLE
        this.statusChange("AVAILABLE",normalUse,"INUSE",transit);
        //【Step5】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"AVAILABLE"),retCodeConfig.getError());
    }

    public void DUR_2_3_9_CarrierStatusNotAvailableToInUse(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form NOTAVAILABLE to InUse
        try {
            this.statusChange("INUSE",transit,"NOTAVAILABLE",emptyID);
            throw new ServiceException(retCodeConfig.getError());
        } catch (ServiceException e) {
            //【Step3】 check the error message
            if (e.getCode() != 961){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DUR_2_3_10_CarrierStatusInUseToNotAvailable(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form NOTAVAILABLE to AVAILABLE
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step3】 change Carrier form AVAILABLE to InUse
        this.statusChange("INUSE",transit,"AVAILABLE",normalUse);
        //【Step4】 change Carrier form AVAILABLE to NOTAVAILABLE
        try {
            this.statusChange("NOTAVAILABLE",Cleaning,"INUSE",transit);
            throw new ServiceException(retCodeConfig.getError());
        } catch (ServiceException e) {
            //【Step5】 check the error message
            if (e.getCode() != 962){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DUR_2_3_11_CarrierStatusScrappedToInUse(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form NOTAVAILABLE to SCRAPPED
        this.statusChange("SCRAPPED",nomalScrap,"NOTAVAILABLE",emptyID);
        //【Step3】 change Carrier form SCRAPPED to InUse
        try {
            this.statusChange("INUSE",transit,"SCRAPPED",nomalScrap);
            throw new ServiceException(retCodeConfig.getError());
        } catch (ServiceException e) {
            //【Step4】 check the error message
            if (e.getCode() != 961){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DUR_2_3_12_CarrierStatusInUseToScrapped(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form NOTAVAILABLE to AVAILABLE
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step3】 change Carrier form AVAILABLE to InUse
        this.statusChange("INUSE",transit,"AVAILABLE",normalUse);
        //【Step4】 change Carrier form InUse to Scrapped
        this.statusChange("SCRAPPED",nomalScrap,"INUSE",transit);
        //【Step5】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"SCRAPPED"),retCodeConfig.getError());
    }

    public void DUR_2_3_15_CarrierStatusChangeWithSameStatus(){
        //【Step1】create carrier for test
        this.createCarrier();
        //【Step2】 change Carrier form NOTAVAILABLE to AVAILABLE
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        try {
            //【Step3】 change Carrier form AVAILABLE to InUse
            this.statusChange("AVAILABLE",normalUse,"AVAILABLE",normalUse);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e) {
            //【Step4】 check the error message
            if (e.getCode() != 2500){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DUR_2_3_17_NotEmptyCarrierStatusChangeToScrapped(){
        //【STEP1】STB
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        //【Step2】 change Carrier form NOTAVAILABLE to Scrapped
        Params.MultiDurableStatusChangeReqParams params = new Params.MultiDurableStatusChangeReqParams();
        params.setUser(getUser());
        Infos.MultiDurableStatusChangeReqInParm mulReqInParm = new Infos.MultiDurableStatusChangeReqInParm();
        params.setParm(mulReqInParm);
        mulReqInParm.setDurableCategory("Cassette");
        mulReqInParm.setDurableStatus("SCRAPPED");
        mulReqInParm.setDurableSubStatus(nomalScrap);
        List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos = new ArrayList<>();
        Infos.StatusChangeDurableInfo statusChangeDurableInfo = new Infos.StatusChangeDurableInfo();
        statusChangeDurableInfos.add(statusChangeDurableInfo);
        statusChangeDurableInfo.setDurableID(cassetteID);
        statusChangeDurableInfo.setDurableStatus("NOTAVAILABLE");
        statusChangeDurableInfo.setDurableSubStatus(emptyID);
        mulReqInParm.setStatusChangeDurableInfos(statusChangeDurableInfos);
        try {
            //【Step3】 change Carrier form AVAILABLE to InUse
            durableController.multiDurableStatusChangeReq(params);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e) {
            //【Step4】 check the error message
            if (e.getCode() != 308){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }



    public void statusChange(String newDurableStatus,ObjectIdentifier newDurableSubStatus, String currentDurableStatus, ObjectIdentifier currentDurableSubStatus){
        Params.MultiDurableStatusChangeReqParams params = new Params.MultiDurableStatusChangeReqParams();
        params.setUser(getUser());
        Infos.MultiDurableStatusChangeReqInParm mulReqInParm = new Infos.MultiDurableStatusChangeReqInParm();
        params.setParm(mulReqInParm);
        mulReqInParm.setDurableCategory("Cassette");
        mulReqInParm.setDurableStatus(newDurableStatus);
        mulReqInParm.setDurableSubStatus(newDurableSubStatus);
        List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos = new ArrayList<>();
        Infos.StatusChangeDurableInfo statusChangeDurableInfo = new Infos.StatusChangeDurableInfo();
        statusChangeDurableInfos.add(statusChangeDurableInfo);
        statusChangeDurableInfo.setDurableID(carrierID);
        statusChangeDurableInfo.setDurableStatus(currentDurableStatus);
        statusChangeDurableInfo.setDurableSubStatus(currentDurableSubStatus);
        mulReqInParm.setStatusChangeDurableInfos(statusChangeDurableInfos);
        durableController.multiDurableStatusChangeReq(params);
    }

    public String changeResultCheck(){
        Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams= new Params.CarrierDetailInfoInqParams();
        carrierDetailInfoInqParams.setUser(getUser());
        carrierDetailInfoInqParams.setDurableWipOperationInfoFlag(true);
        carrierDetailInfoInqParams.setDurableOperationInfoFlag(true);
        carrierDetailInfoInqParams.setCassetteID(carrierID);
        Response response = durableInqController.carrierDetailInfoInq(carrierDetailInfoInqParams);
        Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult = (Results.CarrierDetailInfoInqResult) response.getBody();
        String cassetteStatus = carrierDetailInfoInqResult.getCassetteStatusInfo().getCassetteStatus();
        return cassetteStatus;
    }

    public void createCarrier(){
        Params.DurableSetReqParams params = new Params.DurableSetReqParams();
        params.setUser(getUser());
        Infos.DurableRegistInfo durableRegistInfo =new Infos.DurableRegistInfo();
        params.setDurableRegistInfo(durableRegistInfo);
        durableRegistInfo.setClassName("Cassette");
        durableRegistInfo.setUpdateFlag(false);
        List<Infos.DurableAttribute> durableAttributes =new ArrayList<>();
        Infos.DurableAttribute durableAttribute = new Infos.DurableAttribute();
        durableAttributes.add(durableAttribute);
        durableAttribute.setCapacity(25);
        durableAttribute.setDescription("test carrier");
        durableAttribute.setCategory("FOUP");
        durableAttribute.setDurableID(carrierID);
        durableAttribute.setInstanceName("");
        durableAttribute.setIntervalBetweenPM(0);
        durableAttribute.setMaximumOperationStartCount(0d);
        durableAttribute.setMaximumRunTime("");
        durableAttribute.setNominalSize(12);
        durableAttribute.setUsageCheckFlag(true);
        durableRegistInfo.setDurableAttributeList(durableAttributes);
        durableController.durableSetReq(params);
    }
}
