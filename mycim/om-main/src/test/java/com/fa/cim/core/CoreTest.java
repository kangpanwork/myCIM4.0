package com.fa.cim.core;

import com.fa.cim.MycimApplication;
import com.fa.cim.common.utils.SpringContextUtil;
import com.fa.cim.entity.runtime.area.CimAreaDO;
import com.fa.cim.entity.runtime.stocker.CimStockerDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.pcs.entity.ScriptGlobal;
import com.fa.cim.repository.standard.lot.LotDao;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>CoreTest .</p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/23        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2019/4/23 10:07
 * @copyright: 2019, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
@Slf4j
public class CoreTest {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void boCimLotTest(){
        /*CimLotDO cimLotDO = new CimLotDO();
        cimLotDO.setLotID("NP000325.00A");
        cimLotDO.setId("FRLOT.20998542066561857");
        cimLotDO.setProcessOperationObj("FRPO.14670956278272000");
        cimLotDO.setProcessFlowContextObj("FRPFX.80900313052665326");


        cimLot.beginNextProcessOperationFor(cimLotDO, null);
        log.info("end");*/

        CimAreaDO cimAreaDO = new CimAreaDO();
        cimAreaDO.setId("1");
        CimStockerDO cimStockerDO = new CimStockerDO();
        cimStockerDO.setId("1");

        //cimArea.removeStorageMachine(cimAreaDO, cimStockerDO);
    }

    @Test
    public void queryOneTest(){
        String sql7 =  "SELECT\n" +
                "	DESCRIPTION\n" +
                "FROM\n" +
                "	FRDRBLGRP\n" +
                "WHERE\n" +
                "	DRBLGRP_ID = ?";

        Object[] DESCRIPTION = cimJpaRepository.queryOne(sql7,"RTCL_3");

        System.out.println("*********************************--------------------***********************");
        System.out.println("*********************************--------------------***********************");
        System.out.println("*********************************--------------------***********************");
        System.out.println("*********************************--------------------***********************");
        System.out.println("*********************************--------------------***********************");
        System.out.println(DESCRIPTION[0]);
        System.out.println("*********************************--------------------***********************");
        System.out.println("*********************************--------------------***********************");
        System.out.println("*********************************--------------------***********************");
        System.out.println("*********************************--------------------***********************");

    }

    @Test
    public void test() {
        final ScriptGlobal bean = SpringContextUtil.getSingletonBean(ScriptGlobal.class);
        System.out.println(bean);

        final LotDao bean1 = SpringContextUtil.getSingletonBean(LotDao.class);
        System.out.println(bean1);

        System.out.println("======");
    }

}
