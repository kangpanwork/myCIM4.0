package com.fa.cim.newIntegration.equipment;

import com.fa.cim.MycimApplication;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.impl.EventMakeMethod;
import com.fa.cim.newcore.bo.code.CimMachineState;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.dto.code.CodeDTO;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
@Slf4j
public class EquipmentStateTest {
    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Test
    @Transactional
    @Rollback(false)
    public void testNext() {
        String eqpModel = "Measurement";
        String nextEqpStIdent = "ENG";
        CimMachineState cimMachineState = baseCoreFactory.getBO(CimMachineState.class,
                "OMEQPST.26276413982881837");
        List<CodeDTO.BrEquipmentStateUserGroupData> userGroupData = cimMachineState.
                findUserGroupsForNextEquipmentState(eqpModel, nextEqpStIdent);
        if(CimArrayUtils.isEmpty(userGroupData)) {
            log.info("null");
        }
        userGroupData.forEach(data -> {
            String userGroupId = data.getUserGroupId();
            log.info(userGroupId);
        });
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testProcessResource() {
        MachineDTO.EquipmentInfo equipmentInfo = new MachineDTO.EquipmentInfo();
        List<MachineDTO.ProcessResourceCapabilityInfo> prCapabilities = new ArrayList<>();
        MachineDTO.ProcessResourceCapabilityInfo capabilityInfo = new MachineDTO.ProcessResourceCapabilityInfo();
        List<String> capabilities = new ArrayList<>();
        capabilities.add("Capability.1");
        capabilityInfo.setProcessResourceName("1");
        capabilityInfo.setCapabilities(capabilities);
        capabilityInfo.setGroupName("niuniu");
        capabilityInfo.setRelationship("paralar");
        capabilityInfo.setGroupRelationship("paralar");
        prCapabilities.add(capabilityInfo);
        equipmentInfo.setPrCapabilities(prCapabilities);
        List<String> eqpCapabilities = new ArrayList<>();
        equipmentInfo.setEqpCapabilities(eqpCapabilities);
        CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class,"OMEQP.449525049751832000");
        cimMachine.setEquipmentInfo(equipmentInfo);
        log.info("Yes");
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testConv() {
        CodeDTO.BrEquipmentStateInfo anEquipmentStateInfo = new CodeDTO.BrEquipmentStateInfo();
        List<CodeDTO.BrEquipmentStateConvertingConditionData> conditionData = new ArrayList<>();
        CodeDTO.BrEquipmentStateConvertingConditionData data = new CodeDTO.BrEquipmentStateConvertingConditionData();
        data.setCheckSequence(3);
        data.setAttributeValue("Production");
        data.setConvertingLogic("STLT");
        ObjectIdentifier objectIdentifier = new ObjectIdentifier();
        objectIdentifier.setValue("ENG");
        data.setToEquipmentStateCode(objectIdentifier);
        data.setEqpModel("Process");
        conditionData.add(data);
        anEquipmentStateInfo.setConvertingConditions(conditionData);
        CimMachineState cimMachineState = baseCoreFactory.getBO(CimMachineState.class,
                "OMEQPST.26276413982881837");
        cimMachineState.setEquipmentStateInfo(anEquipmentStateInfo);
    }

    @Autowired
    private EventMakeMethod eventMakeMethod;

    @Test
    @Transactional
    @Rollback(false)
    public void lotTerminate() {
        Infos.ObjCommon objCommon = new Infos.ObjCommon();
        objCommon.setTransactionID("OLOTW031");
        User user = new User();
        ObjectIdentifier objectIdentifier = new ObjectIdentifier();
        objectIdentifier.setValue("1999");
        user.setUserID(objectIdentifier);
        objCommon.setUser(user);
        Infos.TimeStamp timeStamp = new Infos.TimeStamp();
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        timeStamp.setReportTimeStamp(stamp);
        timeStamp.setReportShopDate(11.11);
        objCommon.setTimeStamp(timeStamp);
        ObjectIdentifier lotID = new ObjectIdentifier();
        lotID.setValue("AA");
        ObjectIdentifier bankID = new ObjectIdentifier();
        bankID.setValue("BB");
        String lotFinishedState = "Finish";
//        eventMakeMethod.eventTerminate(objCommon, lotID, bankID, lotFinishedState);
    }

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Test
    @Transactional
    @Rollback(false)
    public void testNumberUtils() {
        String sql = "SELECT DURATION FROM OMPRSS_MINQT WHERE ID = ?";
        String systemKey = "OMPRSS_MINQT.467332173675747392";
        Object[] objects = cimJpaRepository.queryOne(sql, systemKey);
        log.info("{}",CimNumberUtils.doubleValue((Number) objects[0]));
    }
}
