package com.fa.cim.newIntegration.durable.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
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
 * <p>ReticleStockInOutCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/16/016   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/16/016 14:31
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class ReticleStockInOutCase {
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
    public ObjectIdentifier carrierID = new ObjectIdentifier("CRUP0001","FRCAST.81057531304862583");
    public ObjectIdentifier reticleID = new ObjectIdentifier("Reticle_A01");
    public ObjectIdentifier reticlePodID = new ObjectIdentifier("RPOD25");
    public ObjectIdentifier stockerID = new ObjectIdentifier("RSHELF01","OMSTOCKER.85372818196968522");
    public ObjectIdentifier eqpID = new ObjectIdentifier("1TKI02_EXI02");

    public void DRB1_5_1_StockInWhenEIEOAndPodAssociated(){
        //【Step 1】Just In
        this.justInOut("Just-In");
        //【Step 2】EQP IN
        this.ReticlePodEIEO("EI");
        //【Step 3】Stock In
        try {
            this.StockIn("HI");
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e) {
            if (e.getCode()!=1925){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DRB1_5_2_StockInWhenEIEO(){
        //【Step 1】Eqp In
        this.reticleEqpMove("EI",eqpID);
        //【Step 2】Stock In
        this.StockIn("HI");
    }

    public void DRB1_5_3_StockInWhenHIHO(){
        //【Step 1】Eqp In
        this.reticleEqpMove("EI",eqpID);
        //【Step 2】Stock In
        this.StockIn("HI");
    }

    public void DRB1_5_4_StockInWhenHIHOAndPodAssociated(){
        //【Step 1】Just In
        this.justInOut("Just-In");
        //【Step 2】EQP IN
        this.ReticlePodEIEO("EI");
        //【Step 3】Stock In
        try {
            this.StockIn("HI");
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e) {
            if (e.getCode()!=1925){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DRB1_5_5_StockOutWhenReticleHI(){
        //【Step 1】Eqp In
        this.reticleEqpMove("EI",eqpID);
        //【Step 2】Stock In
        this.StockIn("HI");
        //【Step 3】Stock Out
        this.StockIn("HO");
    }

    public void justInOut(String moveDirection){
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

    public void StockIn(String moveDirection){
        Params.ReticleTransferStatusChangeRptParams reticleTransferStatusChangeRptParams = new Params.ReticleTransferStatusChangeRptParams();
        reticleTransferStatusChangeRptParams.setUser(getUser());
        reticleTransferStatusChangeRptParams.setStockerID(stockerID);
        reticleTransferStatusChangeRptParams.setEquipmentID(emptyID);
        List<Infos.XferReticle> xferReticles = new ArrayList<>();
        Infos.XferReticle xferReticle = new Infos.XferReticle();
        xferReticle.setReticleID(reticleID);
        xferReticle.setTransferStatus(moveDirection);
        xferReticles.add(xferReticle);
        reticleTransferStatusChangeRptParams.setStrXferReticle(xferReticles);
        durableController.reticleTransferStatusChangeRpt(reticleTransferStatusChangeRptParams);
    }

    public void ReticlePodEIEO(String transferStatus){
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

    public void reticleEqpMove(String transferStatus,ObjectIdentifier eqpID){
        Params.ReticleTransferStatusChangeRptParams reticleTransferStatusChangeRptParams = new Params.ReticleTransferStatusChangeRptParams();
        reticleTransferStatusChangeRptParams.setUser(getUser());
        reticleTransferStatusChangeRptParams.setStockerID(emptyID);
        reticleTransferStatusChangeRptParams.setEquipmentID(eqpID);
        List<Infos.XferReticle> xferReticles = new ArrayList<>();
        Infos.XferReticle xferReticle = new Infos.XferReticle();
        xferReticle.setReticleID(reticleID);
        xferReticle.setTransferStatus(transferStatus);
        xferReticles.add(xferReticle);
        reticleTransferStatusChangeRptParams.setStrXferReticle(xferReticles);
        durableController.reticleTransferStatusChangeRpt(reticleTransferStatusChangeRptParams);
    }


}

