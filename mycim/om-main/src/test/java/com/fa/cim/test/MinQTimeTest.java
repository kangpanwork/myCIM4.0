package com.fa.cim.test;

import com.alibaba.fastjson.JSON;
import com.fa.cim.MycimApplication;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Infos.ObjCommon;
import com.fa.cim.dto.Outputs;
import com.fa.cim.method.IMinQTimeMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.newcore.bo.qtime.CimMinQTimeRestriction;
import com.fa.cim.newcore.bo.qtime.MinQTimeRestrictionManager;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import com.fa.cim.jpa.SearchCondition;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

/**
 * description:
 * MinQTimeTest
 * change history:
 * date         defect#         person      comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 21-6-15       ********        Grant       create file
 *
 * @author: Grant
 * @date: 21-6-15 15:27
 * @copyright: 2021, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class MinQTimeTest {

    @Autowired
    private GenericCoreFactory genericCoreFactory;
    @Autowired
    private IMinQTimeMethod minQTimeMethod;
    @Autowired
    private MinQTimeRestrictionManager restrictionManager;
    @Autowired
    private IUtilsComp utilsComp;

    @Test
    public void testCheckAndSetRestrictions() {
        User user = new User();
        user.setFunctionID("OEQPQ001");
        user.setUserID(ObjectIdentifier.buildWithValue("DEV"));
        user.setPassword("96e79218965eb72c92a549dd5a330112");
        ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue(), user);

        List<Infos.StartCassette> cassettes = Lists.newArrayList();
        Infos.StartCassette cassette = new Infos.StartCassette();
        List<Infos.LotInCassette> lotIns = Lists.newArrayList();
        Infos.LotInCassette lotIn = new Infos.LotInCassette();
        lotIn.setLotID(ObjectIdentifier.build("NP000246.00A", "OMLOT.453598988769756608"));
        lotIn.setMoveInFlag(true);
        lotIns.add(lotIn);
        cassette.setLotInCassetteList(lotIns);
        cassettes.add(cassette);

        minQTimeMethod.checkAndSetRestrictions(objCommon, cassettes);
    }

    @Test
    public void testCheckIsRejectByRestriction() {
        User user = new User();
        user.setFunctionID("OEQPQ001");
        user.setUserID(ObjectIdentifier.buildWithValue("DEV"));
        user.setPassword("96e79218965eb72c92a549dd5a330112");
        ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue(), user);

        List<Infos.StartCassette> cassettes = Lists.newArrayList();
        Infos.StartCassette cassette = new Infos.StartCassette();
        List<Infos.LotInCassette> lotIns = Lists.newArrayList();
        Infos.LotInCassette lotIn = new Infos.LotInCassette();
        lotIn.setLotID(ObjectIdentifier.build("NP000246.00A", "OMLOT.453598988769756608"));
        lotIn.setMoveInFlag(true);
        lotIns.add(lotIn);
        cassette.setLotInCassetteList(lotIns);
        cassettes.add(cassette);

        minQTimeMethod.checkIsRejectByRestriction(objCommon, cassettes);
        System.out.println("Lot Reserve / Move In success");
    }

    @Test
    public void testLotSplit() {
        ObjectIdentifier parentLotID = ObjectIdentifier.build("NP000246.00A",
                "OMLOT.453598988769756608");
        ObjectIdentifier childLotID = ObjectIdentifier.build("NP000242.00A",
                "OMLOT.452105035314693568");
        minQTimeMethod.lotSplit(parentLotID, childLotID);
    }

    @Test
    public void testLotMerge() {
        ObjectIdentifier parentLotID = ObjectIdentifier.build("NP000246.00A",
                "OMLOT.453598988769756608");
        ObjectIdentifier childLotID = ObjectIdentifier.build("NP000242.00A",
                "OMLOT.452105035314693568");
        minQTimeMethod.lotMerge(parentLotID, childLotID);
    }

    @Test
    public void testClearInvalid() {
        minQTimeMethod.clearInvalid();
    }

    @Test
    public void testCheckIsInRestriction() {
        ObjectIdentifier lotID = ObjectIdentifier.buildWithValue("NP000246.00A");
        String mainProcessFlow = "OMPRF.455314662999262656";
        String processFlowContextObj = "OMPRFCX.450006791751271872";
        boolean result = minQTimeMethod.checkIsInRestriction(lotID, mainProcessFlow, processFlowContextObj);
        System.out.println(result);
    }

    @Test
    public void testGetRestrictInProcessArea() {
        ObjectIdentifier lotID = ObjectIdentifier.buildWithValue("NP000246.00A");
        String mainProcessFlow = "OMPRF.455314662999262656";
        List<Infos.LotQtimeInfo> result = minQTimeMethod.getRestrictInProcessArea(lotID, mainProcessFlow);
        System.out.println(CimArrayUtils.getSize(result));
    }

    @Test
    public void testGetRestrictionsByLot() {
        User user = new User();
        user.setFunctionID("OEQPQ001");
        user.setUserID(ObjectIdentifier.buildWithValue("DEV"));
        user.setPassword("96e79218965eb72c92a549dd5a330112");
        ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue(), user);

        ObjectIdentifier lotID = ObjectIdentifier.buildWithValue("NP000246.00A");

        List<Outputs.QrestLotInfo> result = minQTimeMethod.getRestrictionsByLot(objCommon, lotID);
        System.out.println(CimArrayUtils.getSize(result));
    }

    @Test
    public void testGetRestrictionsByPage() {
        User user = new User();
        user.setFunctionID("OEQPQ001");
        user.setUserID(ObjectIdentifier.buildWithValue("DEV"));
        user.setPassword("96e79218965eb72c92a549dd5a330112");
        ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue(), user);

        Infos.QtimePageListInqInfo params = new Infos.QtimePageListInqInfo();
        params.setLotID(ObjectIdentifier.buildWithValue("NP000246.00A"));
        params.setType("min");
        SearchCondition condition = new SearchCondition();
        condition.setPage(1);
        condition.setSize(20);
        condition.setConditions(Collections.emptyList());
        params.setSearchCondition(condition);

        Page<Outputs.QrestLotInfo> result = minQTimeMethod.getRestrictionsByPage(objCommon, params);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void testRemoveRestriction() {
        CimMinQTimeRestriction restriction = genericCoreFactory.getBO(CimMinQTimeRestriction.class,
                "OMMINQT.457859550683349056");
        restrictionManager.removeRestriction(restriction);
    }

    @Test
    public void testFindRestrictions() {
//        final String findRestrictByLotAndOpeNo = "SELECT ID, TIMER_TYPE, LOT_ID, LOT_RKEY, WAFER_ID, WAFER_RKEY, " +
//                "PRP_LEVEL, TRIGGER_PRP_ID, TRIGGER_PRP_RKEY, TRIGGER_OPE_NO, TRIGGER_TIME, TARGET_PRP_ID, " +
//                "TARGET_PRP_RKEY, TARGET_OPE_NO, TARGET_TIME, ENTITY_MGR FROM OMMINQT WHERE " +
//                "LOT_RKEY = ? AND TARGET_OPE_NO = ?";
//        List<CimMinQTimeRestriction> restricts = genericCoreFactory.getBOListByCustom(CimMinQTimeRestriction.class,
//                findRestrictByLotAndOpeNo, "OMLOT.453598988769756608", "1000.400");
//        System.out.println(restricts.size());

        final String findInvalid = "SELECT * FROM OMMINQT WHERE TARGET_TIME < ?";
        List<CimMinQTimeRestriction> restricts = genericCoreFactory.getBOListByCustom(CimMinQTimeRestriction.class,
                findInvalid, new Timestamp(System.currentTimeMillis()));
        System.out.println(restricts.size());
    }

    @Test
    public void testUpdateRestriction() {
        CimMinQTimeRestriction restriction = genericCoreFactory.getBO(CimMinQTimeRestriction.class,
                "OMMINQT.458008201447620672");
        final Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
        restriction.setTargetTime(currentTimeStamp);
    }

}
