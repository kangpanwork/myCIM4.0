package com.fa.cim.core;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 1/3/2019        ********             Sun               create file
 * 18/9/2019       原auto数据库对应的    Neko              把可查条目改为100
 *                  数据条目为size=10
 *
 * @author: Sun
 * @date: 1/3/2019 10:12 AM
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Configuration
public class TestConfiguration {
   @Bean("testCommonDataForDev")
   public TestCommonData testCommonDataForDev(){return new TestCommonData(true);}

    @Bean("testCommonDataForAuto")
    public TestCommonData testCommonDataForAuto(){return new TestCommonData(true);}

    @Bean("testCommonDataForCaptain")
    public TestCommonData testCommonDataForCaptain(){return new TestCommonData(false);}

    @Bean("testCommonDataWithData")
    public TestCommonData testCommonDataWithData(){
        ObjectIdentifier userId = new ObjectIdentifier(BizConstant.EMPTY);
        String passWord = BizConstant.EMPTY;
        ObjectIdentifier bankId = new ObjectIdentifier(BizConstant.EMPTY);
        ObjectIdentifier eqpId = new ObjectIdentifier(BizConstant.EMPTY);
        ObjectIdentifier eqpId2 = new ObjectIdentifier(BizConstant.EMPTY);
        //Mod 2: pageSize从10改成100
        int pageSize = 100;
        int pageNumber = 1;
        ObjectIdentifier endBankId = new ObjectIdentifier(BizConstant.EMPTY);
        ObjectIdentifier productSpecId = new ObjectIdentifier(BizConstant.EMPTY);
        ObjectIdentifier shipBankId = new ObjectIdentifier(BizConstant.EMPTY);
        return new TestCommonData(userId,passWord,bankId,eqpId,eqpId2,pageSize,pageNumber,endBankId,productSpecId,shipBankId);
    }

    @Value("${spring.datasource.username}")
    private String username;

    @Bean("testCommonDataWithDB")
    public TestCommonData testCommonDataWithDB(){
        if(CimStringUtils.equals(username,"mycim4_dev")){
            return testCommonDataForDev();
        }else if(CimStringUtils.equals(username,"captain")){
            return testCommonDataForCaptain();
        }else if(CimStringUtils.equals(username,"mycim4_norm")){
            return testCommonDataForDev();
        }else if(CimStringUtils.equals(username,"mycim4_auto")){
            return testCommonDataForAuto();
        }else {
            return testCommonDataWithData();
        }
    }
}