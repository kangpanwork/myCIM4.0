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
 * <p>ReticleEqpInOutCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2019/12/23/023   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2019/12/23/023 17:08
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class ReticleEqpInOutCase {

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

    public ObjectIdentifier noNeedReticleEqpID = ObjectIdentifier.build("1ASH01",null);

    public void DRB1_6_1_ReticleXferStatusEOEqpIn(){
        //【STEP1】eqp out reticle to prepare
        ObjectIdentifier NormalEqpID = testCommonData.getEQUIPMENTID3();
        this.reticleEqpMove("EO",NormalEqpID);
        //【STEP2】eqp in
        this.reticleEqpMove("EI",NormalEqpID);
        //【STEP3】check the XferStatus
        String reticleXferStatus = this.getReticleXferStatus();
        Validations.check(!CimStringUtils.equals(reticleXferStatus,"EI"),retCodeConfig.getError());
    }

    public void DRB1_6_2_ReticleXferStatusEIEqpOut(){
        //【STEP1】eqp out reticle to prepare
        ObjectIdentifier NormalEqpID = testCommonData.getEQUIPMENTID3();
        this.reticleEqpMove("EO",NormalEqpID);
        //【STEP2】eqp in
        this.reticleEqpMove("EI",NormalEqpID);
        //【STEP3】eqp out
        this.reticleEqpMove("EO",NormalEqpID);
        //【STEP4】check the XferStatus
        String reticleXferStatus = this.getReticleXferStatus();
        Validations.check(!CimStringUtils.equals(reticleXferStatus,"EO"),retCodeConfig.getError());
    }

    public void DRB1_6_3_ReticleEqpNotNeedReticleEqpIn(){
        //【STEP1】eqp out reticle to prepare
        ObjectIdentifier NormalEqpID = testCommonData.getEQUIPMENTID3();
        this.reticleEqpMove("EO",NormalEqpID);
        //【STEP2】try eqp in
        try {
            this.reticleEqpMove("EI",noNeedReticleEqpID);
            throw new ServiceException(retCodeConfig.getError());
        } catch (ServiceException e) {
            if (e.getCode()!=510){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DRB1_6_4_ReticleRelatedToReticlePodEqpIn(){
        //【STEP1】eqp out reticle to prepare
        ObjectIdentifier NormalEqpID = testCommonData.getEQUIPMENTID3();
        this.reticleEqpMove("EO",NormalEqpID);
        //【STEP2】eqp out reticlePod to prepare
        this.reticlePodEqpMove("EO",NormalEqpID);
        //【STEP3】Just in Reticle Pod
        this.justInOut("Just-In");
        //【STEP4】try eqp in
        try {
            this.reticleEqpMove("EI",NormalEqpID);
            throw new ServiceException(retCodeConfig.getError());
        } catch (ServiceException e) {
            if (e.getCode()!=1925){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DRB1_6_5_ReticleRelatedToReticlePodEqpOut(){
        //【STEP1】eqp out reticle to prepare
        ObjectIdentifier NormalEqpID = testCommonData.getEQUIPMENTID3();
        this.reticleEqpMove("EO",NormalEqpID);
        //【STEP2】eqp out reticlePod to prepare
        this.reticlePodEqpMove("EO",NormalEqpID);
        //【STEP3】Just in Reticle Pod
        this.justInOut("Just-In");
        //【STEP4】eqp in the reticle pod
        this.reticlePodEqpMove("EI",NormalEqpID);
        //【STEP5】try eqp out
        try {
            this.reticleEqpMove("EO",NormalEqpID);
            throw new ServiceException(retCodeConfig.getError());
        } catch (ServiceException e) {
            if (e.getCode()!=1925){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DRB1_6_8_ReticleXferStatusEOEqpInFromReticlePodMenu(){
        //【STEP1】eqp out reticle to prepare
        ObjectIdentifier NormalEqpID = testCommonData.getEQUIPMENTID3();
        this.reticleEqpMove("EO",NormalEqpID);
        //【STEP2】eqp out reticlePod to prepare
        this.reticlePodEqpMove("EO",NormalEqpID);
        //【STEP3】Just in ReticlePod
        this.justInOut("Just-In");
        //【STEP4】eqp in the reticlePod to Prepare
        this.reticlePodEqpMove("EI",NormalEqpID);
        //【STEP5】Check the Xferstatus
        String reticleXferStatus = this.getReticleXferStatus();
        Validations.check(!CimStringUtils.equals(reticleXferStatus,"EI"),retCodeConfig.getError());
    }

    public void DRB1_6_9_ReticleXferStatusEOEqpInFromReticlePodMenu(){
        //【STEP1】eqp out reticle to prepare
        ObjectIdentifier NormalEqpID = testCommonData.getEQUIPMENTID3();
        this.reticleEqpMove("EO",NormalEqpID);
        //【STEP2】eqp out reticlePod to prepare
        this.reticlePodEqpMove("EO",NormalEqpID);
        //【STEP3】Just in Reticle Pod
        this.justInOut("Just-In");
        //【STEP4】eqp in the reticlePod to prepare
        this.reticlePodEqpMove("EI",NormalEqpID);
        //【STEP5】eqp out reticlePod
        this.reticlePodEqpMove("EO",NormalEqpID);
        //【STEP6】Check the Xferstatus
        String reticleXferStatus = this.getReticleXferStatus();
        Validations.check(!CimStringUtils.equals(reticleXferStatus,"EO"),retCodeConfig.getError());
    }

    public void DRB1_6_10_Add_Reticle_to_Equipment(){
        //【STEP1】eqp out reticle to prepare
        ObjectIdentifier NormalEqpID = testCommonData.getEQUIPMENTID3();
        this.reticleEqpMove("EO",NormalEqpID);
        //【STEP2】eqp out reticlePod to prepare
        this.reticlePodEqpMove("EO",NormalEqpID);
        //【STEP3】Just in Reticle Pod
        this.justInOut("Just-In");
        //【STEP4】eqp in the reticlePod to prepare
        this.reticlePodEqpMove("EI",NormalEqpID);
        //【STEP5】just out
        this.justInOut("Just-Out");
        //【STEP6】Check the Xferstatus
        String reticleXferStatus = this.getReticleXferStatus();
        Validations.check(!CimStringUtils.equals(reticleXferStatus,"EI"),retCodeConfig.getError());
    }

    public void DRB1_6_11_Reticle_Processing_After_EqpIn(){
        //【STEP1】eqp out reticle to prepare
        ObjectIdentifier NormalEqpID = testCommonData.getEQUIPMENTID3();
        this.reticleEqpMove("EO",NormalEqpID);
        //【STEP2】eqp in
        this.reticleEqpMove("EI",NormalEqpID);
        //【STEP3】eqp out
        this.reticleEqpMove("EO",NormalEqpID);
        //【STEP4】check the XferStatus
        String reticleXferStatus = this.getReticleXferStatus();
        Validations.check(!CimStringUtils.equals(reticleXferStatus,"EO"),retCodeConfig.getError());
    }

    public void reticleEqpMove(String transferStatus,ObjectIdentifier eqpID){
        Params.ReticleTransferStatusChangeRptParams reticleTransferStatusChangeRptParams = new Params.ReticleTransferStatusChangeRptParams();
        reticleTransferStatusChangeRptParams.setUser(getUser());
        reticleTransferStatusChangeRptParams.setStockerID(emptyID);
        reticleTransferStatusChangeRptParams.setEquipmentID(eqpID);
        List<Infos.XferReticle> xferReticles = new ArrayList<>();
        Infos.XferReticle xferReticle = new Infos.XferReticle();
        xferReticle.setReticleID(testCommonData.getRETICLEID());
        xferReticle.setTransferStatus(transferStatus);
        xferReticles.add(xferReticle);
        reticleTransferStatusChangeRptParams.setStrXferReticle(xferReticles);
        durableController.reticleTransferStatusChangeRpt(reticleTransferStatusChangeRptParams);
    }

    public void reticlePodEqpMove(String transferStatus,ObjectIdentifier eqpID){
        Params.ReticlePodTransferStatusChangeRptParams reticleTransferStatusChangeRptParams = new Params.ReticlePodTransferStatusChangeRptParams();
        reticleTransferStatusChangeRptParams.setUser(getUser());
        reticleTransferStatusChangeRptParams.setStockerID(emptyID);
        reticleTransferStatusChangeRptParams.setEquipmentID(eqpID);
        List<Infos.XferReticlePod> xferReticlePods = new ArrayList<>();
        Infos.XferReticlePod xferReticlePod = new Infos.XferReticlePod();
        xferReticlePod.setReticlePodID(testCommonData.getRETICLEPODID());
        xferReticlePod.setTransferStatus(transferStatus);
        xferReticlePods.add(xferReticlePod);
        reticleTransferStatusChangeRptParams.setXferReticlePodList(xferReticlePods);
        durableController.reticlePodTransferStatusChangeRpt(reticleTransferStatusChangeRptParams);
    }

    public String getReticleXferStatus(){
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setEquipmentID(emptyID);
        reticleListInqParams.setBankID(emptyID);
        reticleListInqParams.setWhiteDefSearchCriteria("ALL");
        reticleListInqParams.setMaxRetrieveCount(300);
        reticleListInqParams.setReticleGroupID(emptyID);
        reticleListInqParams.setReticleID(testCommonData.getRETICLEID());
        reticleListInqParams.setLotID(emptyID);
        reticleListInqParams.setDurableSubStatus(emptyID);
        Response response = durableInqController.reticleListInq(reticleListInqParams);
        Results.ReticleListInqResult result = (Results.ReticleListInqResult) response.getBody();
        String xferStatus = result.getStrFoundReticle().get(0).getReticleStatusInfo().getTransferStatus();
        return xferStatus;
    }

    public void justInOut(String moveDirection){
        ObjectIdentifier reticlePodID = testCommonData.getRETICLEPODID();
        ObjectIdentifier reticleID = testCommonData.getRETICLEID();
        Params.ReticleAllInOutRptParams reticleAllInOutRptParams = new Params.ReticleAllInOutRptParams();
        reticleAllInOutRptParams.setUser(getUser());
        reticleAllInOutRptParams.setMoveDirection(moveDirection);
        reticleAllInOutRptParams.setReticlePodID(reticlePodID);
        List<Infos.MoveReticles> moveReticles = new ArrayList<>();
        Infos.MoveReticles moveReticle = new Infos.MoveReticles();
        moveReticle.setReticleID(reticleID);
        moveReticle.setSlotNumber(1);
        moveReticles.add(moveReticle);
        reticleAllInOutRptParams.setMoveReticles(moveReticles);
        durableController.reticleAllInOutRpt(reticleAllInOutRptParams);
    }
}
