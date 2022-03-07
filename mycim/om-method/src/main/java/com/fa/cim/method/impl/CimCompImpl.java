package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ICimComp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 *
 * @author Bear
 * @since 2018/6/21 12:25
 * Copyright 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@OmMethod
@Slf4j
public class CimCompImpl  implements ICimComp {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private RetCodeConfig retCodeConfig;

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/15                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/4/15 11:19
     * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Infos.ConfigInfo configurationInformationGetDR(Infos.ObjCommon objCommon, String objectKey, String category) {

        //Initialize
        log.info("PPTManager_i::configurationInformation_GetDR");

        //Trace input parameter
        log.info("objectKey {}",objectKey);
        log.info("category {}",category);

        String hFSCONFIGDESCRIPTION = "";
        String hFSCONFIGTYPE = "";
        String hFSCONFIGVALUE = "";

        String hFSCONFIGOBJ_KEY= objectKey;
        String hFSCONFIGCATEGORY= category;

        Object[] obj = cimJpaRepository.queryOne("SELECT DESC,\n" +
                        "                TYPE,\n" +
                        "                VALUE\n" +
                        "        FROM   OSPPRCCONFIG\n" +
                        "        WHERE  ENTITY_KEY  = ?\n" +
                        "        AND    CATEGORY = ?", hFSCONFIGOBJ_KEY,
                hFSCONFIGCATEGORY);

        Validations.check(obj==null,new OmCode(retCodeConfig.getNotFoundEntry(),"SQL SELECT FROM OSPPRCCONFIG"));

        hFSCONFIGDESCRIPTION= CimObjectUtils.toString(obj[0]);
        hFSCONFIGTYPE= CimObjectUtils.toString(obj[1]);
        hFSCONFIGVALUE= CimObjectUtils.toString(obj[2]);

        Infos.ConfigInfo strConfigInfo=new Infos.ConfigInfo();

        strConfigInfo.setObjectKey   (hFSCONFIGOBJ_KEY);
        strConfigInfo.setDescription (hFSCONFIGDESCRIPTION);
        strConfigInfo.setType        (hFSCONFIGTYPE);
        strConfigInfo.setCategory    (hFSCONFIGCATEGORY);
        strConfigInfo.setValue       (hFSCONFIGVALUE);

        log.info("description={}", hFSCONFIGDESCRIPTION);
        log.info("type       ={}", hFSCONFIGTYPE);
        log.info("value      ={}", hFSCONFIGVALUE);

        log.info("configurationInformation_GetDR");
        return strConfigInfo;
    }

}
