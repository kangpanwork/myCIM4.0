package com.fa.cim.newIntegration.durable.scase;

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
 * <p>ReticleJustInOutCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/9/009   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/9/009 13:16
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class ReticleJustInOutCase {

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

    public void DRB4_1_4OneReticleJustIn(){
        //【STEP1】Just in Reticle Pod
        this.justInOut("Just-In");
    }

    public void DRB4_1_6OneReticleJustOut(){
        //【STEP1】Just in Reticle Pod
        this.justInOut("Just-In");
        //【STEP2】Just in Reticle Pod
        this.justInOut("Just-Out");
    }

    public void DRB4_1_8ReticleJustOut(){
        //【STEP1】Just in Reticle Pod
        ObjectIdentifier reticlePodID = testCommonData.getRETICLEPODID();
        ObjectIdentifier reticleID = testCommonData.getRETICLEID();
        Params.ReticleAllInOutRptParams reticleAllInOutRptParams = new Params.ReticleAllInOutRptParams();
        reticleAllInOutRptParams.setUser(getUser());
        reticleAllInOutRptParams.setMoveDirection("Just-In");
        reticleAllInOutRptParams.setReticlePodID(reticlePodID);
        List<Infos.MoveReticles> moveReticles = new ArrayList<>();
        Infos.MoveReticles moveReticle = new Infos.MoveReticles();
        moveReticle.setReticleID(reticleID);
        moveReticle.setSlotNumber(3);
        moveReticles.add(moveReticle);
        reticleAllInOutRptParams.setMoveReticles(moveReticles);
        durableController.reticleAllInOutRpt(reticleAllInOutRptParams);
        //【STEP2】Just in Reticle Pod
        Params.ReticleAllInOutRptParams params = new Params.ReticleAllInOutRptParams();
        params.setUser(getUser());
        params.setMoveDirection("Just-Out");
        params.setReticlePodID(reticlePodID);
        List<Infos.MoveReticles> moveReticle2 = new ArrayList<>();
        Infos.MoveReticles moveReticle1 = new Infos.MoveReticles();
        moveReticle1.setReticleID(reticleID);
        moveReticle1.setSlotNumber(3);
        moveReticles.add(moveReticle1);
        params.setMoveReticles(moveReticle2);
        durableController.reticleAllInOutRpt(params);
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
