package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.entity.runtime.category.CimCategoryDO;
import com.fa.cim.entity.runtime.code.CimCodeDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ICategoryMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/23       ********             Sun               create file
 *
 * @author Sun
 * @since 2018/10/23 15:11
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class CategoryMethod implements ICategoryMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private CimJpaRepository cimJpaRepository;


    @Override
    public List<Infos.SubMfgLayerAttributes> categoryFillInTxTRQ010DR(Infos.ObjCommon objCommon) {
        log.info("【Method Entry】categoryFillInTxTRQ010DR()");

        // Check Category
        CimCategoryDO category = cimJpaRepository.queryOne("select ID, CODETYPE_ID  from\n" +
                "                OMCODETYPE\n" +
                "            where  \n" +
                "                CODETYPE_ID = ?", CimCategoryDO.class, BizConstant.SP_CATEGORY_MFGLAYER);

        Validations.check(null == category,retCodeConfig.getNotFoundCategory() );

        // Get all Codes which the specified Category has
        List<CimCodeDO> codeList = cimJpaRepository.query("select\n" +
                "                ID,\n" +
                "                CODE_ID,\n" +
                "                DESCRIPTION\n" +
                "            from\n" +
                "                OMCODE\n" +
                "            where\n" +
                "                CODETYPE_ID = ?\n" +
                "            order by CODE_ID", CimCodeDO.class, category.getCategoryID());

        int codesCount = CimArrayUtils.getSize(codeList);
        List<Infos.SubMfgLayerAttributes> subMfgLayerAttributesList = new ArrayList<>();
        for (int i = 0; i < codesCount; i++) {
            CimCodeDO code = codeList.get(i);
            Infos.SubMfgLayerAttributes subMfgLayerAttributes = new Infos.SubMfgLayerAttributes();
            subMfgLayerAttributes.setMfgLayerID(new ObjectIdentifier(code.getCodeID(), code.getId()));
            subMfgLayerAttributes.setMfgLayerDescriptionIDDescription(code.getDescription());
            subMfgLayerAttributesList.add(subMfgLayerAttributes);
        }

        // Not found
        if(codesCount == 0){
            log.info(retCodeConfig.getNotFoundEntry().getMessage());

        }
        log.info("【Method Exit】categoryFillInTxTRQ010DR()");
        return subMfgLayerAttributesList;
    }
}
