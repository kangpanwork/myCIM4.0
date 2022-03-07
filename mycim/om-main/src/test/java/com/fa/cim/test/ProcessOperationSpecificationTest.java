package com.fa.cim.test;

import com.fa.cim.MycimApplication;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
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

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
@Slf4j
public class ProcessOperationSpecificationTest {

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Test
    @Transactional
    @Rollback(false)
    public void testSetProcessOperationSpecificationInfo() {
        ProcessDTO.BrPOSData posData = new ProcessDTO.BrPOSData();
        List<ProcessDTO.BrDefaultTimeRestrictionData> list = new ArrayList<>();
        ProcessDTO.BrDefaultTimeRestrictionData dataMax = new ProcessDTO.BrDefaultTimeRestrictionData();
        ProcessDTO.BrDefaultTimeRestrictionData dataMin = new ProcessDTO.BrDefaultTimeRestrictionData();
        dataMax.setDuration((long) 55);
        dataMax.setTargetOperationNumber("1000.3");
        dataMax.setAction("DispatchPrecede");
        dataMax.setQTimeType("By Lot");
        dataMax.setQType("Max QTime");
        dataMin.setDuration((long) 5);
        dataMin.setTargetOperationNumber("1000.3");
        dataMin.setQTimeType("By Lot");
        dataMin.setQType("Min QTime");
        list.add(dataMax);
        list.add(dataMin);
        posData.setOperationNumber("1000.2");
        ObjectIdentifier objectIdentifier = new ObjectIdentifier();
        objectIdentifier.setValue("P_003.1");
        posData.setProcessDefinition(objectIdentifier);
        posData.setDefaultTimeRestrictions(list);
        CimProcessOperationSpecification specificationBO = baseCoreFactory.getBO(
                CimProcessOperationSpecification.class, "OMPRSS.464377692319974848");
        specificationBO.setProcessOperationSpecificationInfo(posData);
        log.info("Over");
    }

}
