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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>ReticleSubStatesChangeCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/2/21/021   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/2/21/021 09:50
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class ReticleStatusChangeCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private DurableController durableController;

    @Autowired
    private DurableInqController durableInqController;

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

    public void DRB6_2_1_ReticleStatusAvailableToNotAvailable(){
        //【Step1】 change reticle form notAvailable to Available
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step2】 change reticle form Available to notAvailable
        this.statusChange("NOTAVAILABLE",Cleaning,"AVAILABLE",normalUse);
        //【Step3】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"NOTAVAILABLE"),retCodeConfig.getError());
    }

    public void DRB6_2_2_ReticleStatusNotAvailableToAvailable(){
        //【Step1】 change reticle form notAvailable to Available
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step2】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"AVAILABLE"),retCodeConfig.getError());
    }

    public void DRB6_2_3_ReticleStatusAvailableToScrapped(){
        //【Step1】 change reticle form notAvailable to Available
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step2】 change reticle form Available to Scrapped
        this.statusChange("SCRAPPED",nomalScrap,"AVAILABLE",normalUse);
        //【Step3】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"SCRAPPED"),retCodeConfig.getError());
    }

    public void DRB6_2_4_ReticleStatusScrapedToAvailable(){
        //【Step1】 change reticle form notAvailable to Available
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step2】 change reticle form Available to Scrapped
        this.statusChange("SCRAPPED",nomalScrap,"AVAILABLE",normalUse);
        //【Step3】 change reticle form Scrapped to Available
        this.statusChange("AVAILABLE",normalUse,"SCRAPPED",nomalScrap);
        //【Step4】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"AVAILABLE"),retCodeConfig.getError());
    }

    public void DRB6_2_5_ReticleStatusScrappedToNotAvailable(){
        //【Step1】 change reticle form NOTAVAILABLE to SCRAPPED
        this.statusChange("SCRAPPED",nomalScrap,"NOTAVAILABLE",emptyID);
        //【Step2】 change reticle form SCRAPPED to NOTAVAILABLE
        this.statusChange("NOTAVAILABLE",Cleaning,"SCRAPPED",nomalScrap);
        //【Step3】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"NOTAVAILABLE"),retCodeConfig.getError());
    }

    public void DRB6_2_6_ReticleStatusNotAvailableToScrapped(){
        //【Step1】 change reticle form NOTAVAILABLE to SCRAPPED
        this.statusChange("SCRAPPED",nomalScrap,"NOTAVAILABLE",emptyID);
        //【Step2】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"SCRAPPED"),retCodeConfig.getError());
    }

    public void DRB6_2_7_ReticleStatusAvailableToInUse(){
        //【Step1】 change reticle form NOTAVAILABLE to AVAILABLE
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step2】 change reticle form AVAILABLE to InUse
        this.statusChange("INUSE",transit,"AVAILABLE",normalUse);
        //【Step3】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"INUSE"),retCodeConfig.getError());
    }

    public void DRB6_2_8_ReticleStatusInUseToAvailable(){
        //【Step1】 change reticle form NOTAVAILABLE to AVAILABLE
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step2】 change reticle form AVAILABLE to InUse
        this.statusChange("INUSE",transit,"AVAILABLE",normalUse);
        //【Step2】 change reticle form InUse to AVAILABLE
        this.statusChange("AVAILABLE",normalUse,"INUSE",transit);
        //【Step2】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"AVAILABLE"),retCodeConfig.getError());
    }

    public void DRB6_2_9_ReticleStatusNotAvailableToInUse(){
        //【Step1】 change reticle form NOTAVAILABLE to InUse
        try {
            this.statusChange("INUSE",transit,"NOTAVAILABLE",emptyID);
            throw new ServiceException(retCodeConfig.getError());
        } catch (ServiceException e) {
            //【Step2】 check the error message
            if (e.getCode() != 961){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DRB6_2_10_ReticleStatusInUseToNotAvailable(){
        //【Step1】 change reticle form NOTAVAILABLE to AVAILABLE
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step2】 change reticle form AVAILABLE to InUse
        this.statusChange("INUSE",transit,"AVAILABLE",normalUse);
        //【Step3】 change reticle form AVAILABLE to NOTAVAILABLE
        try {
            this.statusChange("NOTAVAILABLE",Cleaning,"INUSE",transit);
            throw new ServiceException(retCodeConfig.getError());
        } catch (ServiceException e) {
            //【Step4】 check the error message
            if (e.getCode() != 962){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DRB6_2_11_ReticleStatusScrappedToInUse(){
        //【Step1】 change reticle form NOTAVAILABLE to SCRAPPED
        this.statusChange("SCRAPPED",nomalScrap,"NOTAVAILABLE",emptyID);
        //【Step2】 change reticle form SCRAPPED to InUse
        try {
            this.statusChange("INUSE",transit,"SCRAPPED",nomalScrap);
            throw new ServiceException(retCodeConfig.getError());
        } catch (ServiceException e) {
            //【Step3】 check the error message
            if (e.getCode() != 961){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DRB6_2_12_ReticleStatusInUseToScrapped(){
        //【Step1】 change reticle form NOTAVAILABLE to AVAILABLE
        this.statusChange("AVAILABLE",normalUse,"NOTAVAILABLE",emptyID);
        //【Step2】 change reticle form AVAILABLE to InUse
        this.statusChange("INUSE",transit,"AVAILABLE",normalUse);
        //【Step2】 change reticle form InUse to Scrapped
        this.statusChange("SCRAPPED",nomalScrap,"INUSE",transit);
        //【Step2】 check the result
        String resultCheck = this.changeResultCheck();
        Validations.check(!CimStringUtils.equals(resultCheck,"SCRAPPED"),retCodeConfig.getError());
    }



    public void statusChange(String newDurableStatus,ObjectIdentifier newDurableSubStatus, String currentDurableStatus, ObjectIdentifier currentDurableSubStatus){
        Params.MultiDurableStatusChangeReqParams params = new Params.MultiDurableStatusChangeReqParams();
        params.setUser(getUser());
        Infos.MultiDurableStatusChangeReqInParm mulReqInParm = new Infos.MultiDurableStatusChangeReqInParm();
        params.setParm(mulReqInParm);
        mulReqInParm.setDurableCategory("Reticle");
        mulReqInParm.setDurableStatus(newDurableStatus);
        mulReqInParm.setDurableSubStatus(newDurableSubStatus);
        List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos = new ArrayList<>();
        Infos.StatusChangeDurableInfo statusChangeDurableInfo = new Infos.StatusChangeDurableInfo();
        statusChangeDurableInfos.add(statusChangeDurableInfo);
        statusChangeDurableInfo.setDurableID(testCommonData.getRETICLEID());
        statusChangeDurableInfo.setDurableStatus(currentDurableStatus);
        statusChangeDurableInfo.setDurableSubStatus(currentDurableSubStatus);
        mulReqInParm.setStatusChangeDurableInfos(statusChangeDurableInfos);
        durableController.multiDurableStatusChangeReq(params);
    }

    public String changeResultCheck(){
        Params.ReticleListInqParams reticleListInqParams= new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setWhiteDefSearchCriteria("All");
        reticleListInqParams.setMaxRetrieveCount(300);
        reticleListInqParams.setBankID(emptyID);
        reticleListInqParams.setReticleGroupID(emptyID);
        reticleListInqParams.setReticleID(testCommonData.getRETICLEID());
        reticleListInqParams.setLotID(emptyID);
        reticleListInqParams.setDurableSubStatus(emptyID);
        reticleListInqParams.setEquipmentID(emptyID);
        Response response = durableInqController.reticleListInq(reticleListInqParams);
        Results.ReticleListInqResult reticleListInqResult = (Results.ReticleListInqResult) response.getBody();
        String reticleStatus = reticleListInqResult.getStrFoundReticle().get(0).getReticleStatusInfo().getReticleStatus();
        return reticleStatus;
    }
}
