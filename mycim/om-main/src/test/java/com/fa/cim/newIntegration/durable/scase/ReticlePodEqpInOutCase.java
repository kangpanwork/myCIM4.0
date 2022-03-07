package com.fa.cim.newIntegration.durable.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.durable.DurableController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>ReticlePodEqpInOutCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/2/24/024   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/2/24/024 12:47
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class ReticlePodEqpInOutCase {

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
    public ObjectIdentifier eqpID = new ObjectIdentifier("1TKI02_EXI02");
    public ObjectIdentifier reticlePodID = new ObjectIdentifier("RPOD25");


    public void DRB1_6_1_ReticlePodStatusEOEqpIn(){
        //【Step 1】EqpOut the reticlePod
        this.ReticlePodStatusChange("EO");
        //【Step 2 】EqpIn the reticlePod
        this.ReticlePodStatusChange("EI");
        //【Step 3】Check the result
        String statusCheck = this.statusCheck();
        if (!CimStringUtils.equals(statusCheck,"EI")){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void DRB1_6_3_ReticlePodNoStatusEqpIn(){
        //【Step 1】EqpIn the reticlePod
        this.ReticlePodStatusChange("EI");
        //【Step 2】Check the result
        String statusCheck = this.statusCheck();
        if (!CimStringUtils.equals(statusCheck,"EI")){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void DRB1_6_4_ReticlePodNeedNoReticleEqpIn(){
        //【Step 1】EqpIn the reticlePod
        Params.ReticlePodTransferStatusChangeRptParams params = new Params.ReticlePodTransferStatusChangeRptParams();
        params.setUser(getUser());
        params.setStockerID(emptyID);
        params.setEquipmentID(ObjectIdentifier.buildWithValue("1ASH01"));
        List<Infos.XferReticlePod> xferReticlePodList = new ArrayList<>();
        Infos.XferReticlePod xferReticlePod = new Infos.XferReticlePod();
        xferReticlePodList.add(xferReticlePod);
        xferReticlePod.setReticlePodID(reticlePodID);
        xferReticlePod.setTransferStatus("EI");
        xferReticlePod.setTransferStatusChangeTimeStamp("");
        params.setXferReticlePodList(xferReticlePodList);
        try {
            durableController.reticlePodTransferStatusChangeRpt(params);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e) {
            if (e.getCode()!= 510 ){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DRB1_6_6_ReticlePodStatusEIEqpOut(){
        //【Step 1】EqpOut the reticlePod
        this.ReticlePodStatusChange("EI");
        //【Step 2 】EqpIn the reticlePod
        this.ReticlePodStatusChange("EO");
        //【Step 3】Check the result
        String statusCheck = this.statusCheck();
        if (!CimStringUtils.equals(statusCheck,"EO")){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void ReticlePodStatusChange(String transferStatus){
        Params.ReticlePodTransferStatusChangeRptParams params = new Params.ReticlePodTransferStatusChangeRptParams();
        params.setUser(getUser());
        params.setStockerID(emptyID);
        params.setEquipmentID(eqpID);
        List<Infos.XferReticlePod> xferReticlePodList = new ArrayList<>();
        Infos.XferReticlePod xferReticlePod = new Infos.XferReticlePod();
        xferReticlePodList.add(xferReticlePod);
        xferReticlePod.setReticlePodID(reticlePodID);
        xferReticlePod.setTransferStatus(transferStatus);
        xferReticlePod.setTransferStatusChangeTimeStamp("");
        params.setXferReticlePodList(xferReticlePodList);
        durableController.reticlePodTransferStatusChangeRpt(params);
    }

    public String statusCheck (){
        Params.ReticlePodListInqParams params = new Params.ReticlePodListInqParams();
        params.setEmptyFlag(false);
        params.setMaxRetrieveCount(300L);
        params.setBankID(emptyID);
        params.setReticleGroupID(emptyID);
        params.setReticlePodID(reticlePodID);
        params.setReticleID(emptyID);
        params.setDurableSubStatus(emptyID);
        params.setEquipmentID(emptyID);
        params.setStockerID(emptyID);
        params.setUser(getUser());
        Response response = durableInqController.reticlePodListInq(params);
        List<Infos.ReticlePodListInfo> reticlePodListInfos= (List<Infos.ReticlePodListInfo>) response.getBody();
        String transferStatus = reticlePodListInfos.get(0).getReticlePodStatusInfo().getTransferStatus();
        return transferStatus;
    }

}
