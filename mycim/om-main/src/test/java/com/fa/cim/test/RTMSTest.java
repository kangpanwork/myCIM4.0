package com.fa.cim.test;

import com.fa.cim.MycimApplication;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import com.fa.cim.entity.runtime.lot.CimLotMaterialContainerDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.remote.IRTMSRemoteManager;
import com.fa.cim.rtms.ReticleRTMSEqpOutReqParams;
import com.fa.cim.rtms.ReticleRTMSStockerInReqParams;
import com.fa.cim.rtms.ReticleRTMSStockerOutReqParams;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/7/22        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/7/22 14:41
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
@Slf4j
public class RTMSTest {

    @Autowired
    private IRTMSRemoteManager rtmsRemoteManager;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IUtilsComp utilsComp;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Test
    @Transactional
    @Rollback(false)
    public void rtmsReticleEqpInTest(){
        User user = new User();
        user.setFunctionID("OEQPW005");
        user.setUserID(ObjectIdentifier.buildWithValue("decade"));
        String txId = TransactionIDEnum.OPERATION_START_REQ.getValue();
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);
        ObjectIdentifier equipmentID = ObjectIdentifier.buildWithValue("ACVD201-CH");
        CimLot boByIdentifier = baseCoreFactory.getBOByIdentifier(CimLot.class, "NP000581.040");
        CimLotMaterialContainerDO cimLotMaterialContainerExample = new CimLotMaterialContainerDO();
        cimLotMaterialContainerExample.setSequenceNumber(0);
        cimLotMaterialContainerExample.setReferenceKey(ObjectIdentifier.fetchReferenceKey(boByIdentifier.getLotID()));
        CimLotMaterialContainerDO lotMaterialContainers = cimJpaRepository.findOne(Example.of(cimLotMaterialContainerExample)).orElse(null);
        CimCassette aCast = baseCoreFactory.getBO(CimCassette.class, lotMaterialContainers.getMaterialContainerObj());
        List<ObjectIdentifier> cassetteID = new ArrayList<>();
        cassetteID.add(ObjectIdentifier.buildWithValue(aCast.getIdentifier()));
        List<Infos.StartCassette> startCassettes = processMethod.processStartReserveInformationGetByCassette(
                objCommon,equipmentID,cassetteID,false
        );
    }

    @Test
    @Transactional
    @Rollback(false)
    public void rtmsReticleEqpOutTest(){
        ReticleRTMSEqpOutReqParams params = new ReticleRTMSEqpOutReqParams();
        params.setReticleId(43681);
        params.setReticleName("Retice13216546798");
        params.setPodName("Pod00000000000014");
        User user = new User();
        params.setUser(user);
        user.setUserID(ObjectIdentifier.buildWithValue("xiaohan"));
        user.setFunctionID("test_requestEquipmentIn_function");
        user.setPassword("96e79218965eb72c92a549dd5a330112");
        Object reticleRTMSEqpInReq = rtmsRemoteManager.reticleRTMSEqpOutReq(params);
    }

    @Test
    @Transactional
    @Rollback(false)
    public void rtmsReticleStockerInTest(){
        ReticleRTMSStockerInReqParams params = new ReticleRTMSStockerInReqParams();
        params.setReticleId(43681);
        params.setReticleName("Retice13216546798");
        params.setStockerName("Stocker1");
        User user = new User();
        params.setUser(user);
        user.setUserID(ObjectIdentifier.buildWithValue("xiaohan"));
        user.setFunctionID("test_requestEquipmentIn_function");
        user.setPassword("96e79218965eb72c92a549dd5a330112");
        Object reticleRTMSEqpInReq = rtmsRemoteManager.reticleRTMSStockerInReq(params);
    }

    @Test
    @Transactional
    @Rollback(false)
    public void rtmsReticleStocerOutTest(){
        ReticleRTMSStockerOutReqParams params = new ReticleRTMSStockerOutReqParams();
        params.setReticleId(43681);
        params.setReticleName("Retice13216546798");
        params.setStockerName("Stocker1");
        User user = new User();
        params.setUser(user);
        user.setUserID(ObjectIdentifier.buildWithValue("xiaohan"));
        user.setFunctionID("test_requestEquipmentIn_function");
        user.setPassword("96e79218965eb72c92a549dd5a330112");
        Object reticleRTMSEqpInReq = rtmsRemoteManager.requestRTMSStockerOut(params);
    }
}