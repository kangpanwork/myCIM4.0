package com.fa.cim.test;

import com.fa.cim.MycimApplication;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.lot.TerminateReq;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.service.lot.ILotService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * description:
 * LotTerminateTest
 * change history:
 * date         defect#         person      comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 21-7-8       ********        Grant       create file
 *
 * @author: Grant
 * @date: 21-7-8 09:57
 * @copyright: 2021, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class LotTerminateTest {

    @Autowired
    private ILotService lotService;
    @Autowired
    private IUtilsComp utilsComp;

    @Test
    public void testSxTerminateReq() {
        User user = new User();
        user.setFunctionID("OEQPQ001");
        user.setUserID(ObjectIdentifier.buildWithValue("DEV"));
        user.setPassword("96e79218965eb72c92a549dd5a330112");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.LOT_TERMINATE_REQ.getValue(), user);

        TerminateReq.TerminateReqParams params = new TerminateReq.TerminateReqParams();
        params.setLotID(ObjectIdentifier.build("NP000126.10A", "OMLOT.464506930285053376"));
        params.setReasonCodeID(ObjectIdentifier.build("INUSE", "OMCODE.81376001289935100"));

        lotService.sxTerminateReq(objCommon, params);
    }

    @Test
    public void testSxTerminateCancelReq() {
        User user = new User();
        user.setFunctionID("OEQPQ001");
        user.setUserID(ObjectIdentifier.buildWithValue("DEV"));
        user.setPassword("96e79218965eb72c92a549dd5a330112");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.LOT_TERMINATE_REQ.getValue(), user);

        TerminateReq.TerminateCancelReqParams params = new TerminateReq.TerminateCancelReqParams();
        params.setLotID(ObjectIdentifier.build("NP000126.10A", "OMLOT.464506930285053376"));
        params.setReasonCodeID(ObjectIdentifier.build("INUSE", "OMCODE.81376001289935100"));

        lotService.sxTerminateCancelReq(objCommon, params);
    }

}
