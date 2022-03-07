package com.fa.cim.core;

import com.fa.cim.MycimApplication;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.newcore.bo.abstractgroup.CimPcsUserDefVariables;
import com.fa.cim.newcore.bo.code.CimScript;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.global.GlobalDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.pcs.attribute.property.StringMap;
import com.fa.cim.pcs.attribute.property.StringProperty;
import com.fa.cim.pcs.engine.CimPcsEngine;
import com.fa.cim.pcs.engine.ScriptEntityFactory;
import com.fa.cim.pcs.engine.ScriptParam;
import com.fa.cim.pcs.entity.ScriptLot;
import com.fa.cim.pcs.entity.utils.Date;
import com.fa.cim.service.access.IAccessInqService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * description:
 * <p>PCSTest .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/11/30         ********             ZQI               create file. PCS Test.
 *
 * @author ZQI
 * @date 2019/11/30 22:01
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class PCSTest {

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private ScriptEntityFactory scriptEntityFactory;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private CimPcsEngine engine;

    private static final String LOT_ID = "NP000255.00A";

    @Test
    @Rollback(false)
    @Transactional(rollbackFor = Exception.class)
    public void userDefVariablesTest(){
        CimPcsUserDefVariables cimPcsUserDefVariables = baseCoreFactory.newBO(CimPcsUserDefVariables.class);
        GlobalDTO.BrPcsUserDefVariables userDefVariables = new GlobalDTO.BrPcsUserDefVariables();
        userDefVariables.setKeyName("Lot;TestMap");
        userDefVariables.setTheTableMarker("Pcs_User_Variables");
        userDefVariables.setVariableDefaultValue("AAA");
        userDefVariables.setVariableDescription("Eric Test");
        userDefVariables.setVariableName("TestMap");
        userDefVariables.setVariableOwner("Eric");
        userDefVariables.setVariableOwnerClass("Lot");
        userDefVariables.setVariableScope("ALL");
        userDefVariables.setVariableType("TABLESS");

        cimPcsUserDefVariables.setPcsUserDefVariablesInfo(userDefVariables);
    }

    @Test
    @Rollback(false)
    @Transactional(rollbackFor = Exception.class)
    public void scriptParameterStringTest(){
        CimLot lot = baseCoreFactory.getBOByIdentifier(CimLot.class, LOT_ID);
        ScriptLot scriptLot = scriptEntityFactory.generateScriptEntity(ScriptLot.class, lot);
        StringProperty temp = scriptLot.stringValue("Temp");
        System.out.println("value : " + temp.getValue());
    }

    @Test
    @Rollback(false)
    @Transactional(rollbackFor = Exception.class)
    public void scriptParameterStringMapTest() {
        CimLot lot = baseCoreFactory.getBOByIdentifier(CimLot.class, LOT_ID);
        ScriptLot scriptLot = scriptEntityFactory.generateScriptEntity(ScriptLot.class, lot);
        StringMap testMap = scriptLot.stringMap("TestMap");
        System.out.println("key :  TestMap " + "value : " + testMap.getValueNamed("TestMap"));
    }

    @Test
    @Rollback(false)
    @Transactional(rollbackFor = Exception.class)
    public void createScript(){
        CimScript cimScript = baseCoreFactory.newBO(CimScript.class);
        cimScript.setDescription("PCS Flow Test By Eric.");
        cimScript.setIdentifier("ERIC_TEST.01");
        cimScript.setIntermediateCode("var factory = $Factory;\n" +
                "var system = $System;\n" +
                "var lot = factory.lot($LotId);\n" +
                "if (lot.productId() == \"PRODUCTPCS.01\" && lot.holdState() == \"NOTONHOLD\") {\n" +
                "\tlot.skipTo(\"1000.0400\");\n" +
                "\tsystem.log(lot.lotId());\n" +
                "\tlot.setOpeNote(\"1000.0400\",\"Eric PCS Test\",\"This a simple test of PSC.\");\n" +
                "\tsystem.log(\"Eric Test >>>>>>\");\n" +
                "\tsystem.log(\"Eric Test >>>>>> \" + factory.lot(lot));\n" +
                "}");

        CimScript cimScript2 = baseCoreFactory.newBO(CimScript.class);
        cimScript2.setDescription("PCS Flow Test By Eric.");
        cimScript2.setIdentifier("ERIC_TEST.02");
        cimScript2.setIntermediateCode("var lot = $Factory.lot($LotId);\n" +
                "if(lot.operationNumber() == \"1000.0500\"){\n" +
                "\tlot.skipTo(\"1000.0100\");\n" +
                "}");
    }

    @Test
    @Rollback(false)
    @Transactional(rollbackFor = Exception.class)
    public void updateScript(){
        String primaryKey = "FRSCRIPT.262263152727556096";
        CimScript cimScript = baseCoreFactory.getBO(CimScript.class, primaryKey);
        cimScript.setIntermediateCode("var factory = $Factory;\n" +
                "var lot = factory.lot($LotId);\n" +
                "var date = $Date;\n" +
                "if (lot.productId() == \"PRODUCTPCS.01\" && lot.holdState() == \"NOTONHOLD\") {\n" +
                "\tlot.skipTo(\"1000.0400\");\n" +
                "\tlot.setOpeNote(\"1000.0400\",\"Eric PCS Test\",\"This a simple test of PSC.\");\n" +
                "\n" +
                "\t$System.log(\"Eric Test >>>>>> Lot ID: \" + lot.lotId());\n" +
                "\t$System.log(\"Eric Test >>>>>> 上次修改时间：\" + lot.lastTimestamp());\n" +
                "\t$System.log(\"Eric Test >>>>>> 上次修改User：\" + lot.lastUser());\n" +
                "\t$System.log(\"Eric Test >>>>>> orderNum:\" + lot.orderNum());\n" +
                "\t$System.log(\"Eric Test >>>>>> priority: \" + lot.priorityClass());\n" +
                "\n" +
                "\t$System.log(\"Eric Test >>>>>> 1. Product Group ID : \" + lot.product().productGroupId());\n" +
                "\t$System.log(\"Eric Test >>>>>> 2. Product Group ID : \" + lot.productGroupId());\n" +
                "\n" +
                "\tLog.info(\"Eric Test >>>>>>\");\n" +
                "\tLog.info(\"Eric Test >>>>>>\" + date.currentTimeStamp());\n" +
                "\n" +
                "\t$System.log(\"Eric Test >>>>>> Current Date : \" + date.currentDate());\n" +
                "\t$System.log(\"Eric Test >>>>>> Current Time : \" + date.currentTime());\n" +
                "\t$System.log(\"Eric Test >>>>>> Current Day Of Week : \" + date.currentDayOfWeek());\n" +
                "\t$System.log(\"Eric Test >>>>>> Current TimeStamp : \" + date.currentTimeStamp());\n" +
                "\n" +
                "\n" +
                "\tLog.info(\"Eric Test >>>>>> Current Date : \" + date.currentDate());\n" +
                "\tLog.info(\"Eric Test >>>>>> Current Time : \" + date.currentTime());\n" +
                "\tLog.info(\"Eric Test >>>>>> Current Day Of Week : \" + date.currentDayOfWeek());\n" +
                "\tLog.info(\"Eric Test >>>>>> Current TimeStamp : \" + date.currentTimeStamp());\n" +
                "\n" +
                "\tLog.debug(\"Eric Test >>>>>> Current TimeStamp : \" + date.currentTimeStamp());\n" +
                "\tLog.trace(\"Eric Test >>>>>> Current TimeStamp : \" + date.currentTimeStamp());\n" +
                "}");
    }

    @Autowired
    public Date date;

    @Test
    public void DateTest() {
        System.out.println("Current Date : " + date.currentDate());
        System.out.println("Current Time : " + date.currentTime());
        System.out.println("Current Day Of week : " + date.currentDayOfWeek());
        System.out.println("Current TimeStamp : " + date.currentTimeStamp());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Rollback(false)
    public void skipTest(){
        String lotId = "NP000243.00A";
        ScriptLot scriptLot = scriptEntityFactory.lot(lotId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(ObjectIdentifier.buildWithValue(scriptLot.lotId())));

        User user = new User();
        user.setUserID(ObjectIdentifier.buildWithValue("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        user.setFunctionID("OLOTW003");
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon("OLOTW003", user, accessControlCheckInqParams);

        ScriptParam scriptParam = new ScriptParam();
        scriptParam.setLotId(ObjectIdentifier.buildWithValue(lotId));
        scriptParam.setPhase(BizConstant.SP_BRSCRIPT_PRE1);
        /*String script = "$ScriptEntityFactory.lot($LotId).skipTo('2000.0200');";*/
        String script = "$ScriptEntityFactory.lot($LotId).skip();";
        scriptParam.setScript(script);
        engine.runScript(objCommon, scriptParam);

        // ScriptThreadHolder.init(BizConstant.SP_BRSCRIPT_PRE1,objCommon);
        //scriptLot.skip();
        /*scriptLot.skipTo("5000.0100");*/
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Rollback(false)
    public void returnFromSubRouteTest(){
        String lotId = "NP000243.00A";
        ScriptLot scriptLot = scriptEntityFactory.lot(lotId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(ObjectIdentifier.buildWithValue(scriptLot.lotId())));

        User user = new User();
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        user.setUserID(ObjectIdentifier.buildWithValue("ADMIN"));
        user.setFunctionID("OLOTW003");
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon("OLOTW003", user, accessControlCheckInqParams);

        ScriptParam scriptParam = new ScriptParam();
        scriptParam.setLotId(ObjectIdentifier.buildWithValue(lotId));
        scriptParam.setPhase(BizConstant.SP_BRSCRIPT_PRE1);
        String script = "$ScriptEntityFactory.lot($LotId).returnFromSubRoute();";
        scriptParam.setScript(script);
        engine.runScript(objCommon, scriptParam);
    }
}
