package com.fa.cim.core;

import com.fa.cim.entity.runtime.cassette.CimCassetteDO;
import com.fa.cim.jpa.CimJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/7        *******              jerry              create file
 *
 * @author: jerry
 * @date: 2018/6/7 14:47
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@Slf4j
public class WaferCore {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Test
     public void waferTest(){
        CimCassetteDO oldCassette = cimJpaRepository.queryOne("SELECT * FROM FRCAST WHERE ID = ?1",CimCassetteDO.class,"FRCAST.96066869892461739");
        for (int i = 0; i < 15; i++) {
            //cassetteCore.removeWaferFromPosition(oldCassette, BaseStaticMethod.converLong(i+1));
        }
    }
}