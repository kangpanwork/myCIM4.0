package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.BaseStaticMethod;
import com.fa.cim.dto.Infos;
import com.fa.cim.entity.runtime.productgroup.CimProductGroupDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IProductGroupMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/12/4                            Wind                create file
 *
 * @author Wind
 * @since 2018/12/4 22:11
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class ProductGroupMethod implements IProductGroupMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Override
    public List<Infos.ProductGroupIDListAttributes> productGroupListAttributesGetDR(Infos.ObjCommon objCommon) {

        List<Infos.ProductGroupIDListAttributes> productGroupIDListAttributes = new ArrayList<>();


        List<CimProductGroupDO> allProductGroup = cimJpaRepository.query(" SELECT  ID    ,\n" +
                "                         PRODFMLY_ID     ,\n" +
                "                         DESCRIPTION    ,\n" +
                "                         TECH_ID        ,\n" +
                "                         TECH_RKEY       ,\n" +
                "                         OWNER_ID       ,\n" +
                "                         OWNER_RKEY      ,\n" +
                "                         CHIP_SIZE_X    ,\n" +
                "                         CHIP_SIZE_Y    ,\n" +
                "                         CYCLE_TIME_PLAN,\n" +
                "                         YIELD_PLAN     ,\n" +
                "                         GROSS_DIE_COUNT\n" +
                "                 FROM    OMPRODFMLY\n" +
                "                 ORDER BY PRODFMLY_ID ", CimProductGroupDO.class);
        for (CimProductGroupDO productGroup : allProductGroup) {
            Infos.ProductGroupIDListAttributes productGroupIDList = new Infos.ProductGroupIDListAttributes();
            productGroupIDList.setProductGroupID(ObjectIdentifier.build(productGroup.getProductGroupID(), productGroup.getId()));
            productGroupIDList.setProductGroupName(productGroup.getProductGroupID());
            productGroupIDList.setDescription(productGroup.getDescription());
            productGroupIDList.setTechnologyID(ObjectIdentifier.build(productGroup.getTechnologyID(), productGroup.getTechnologyObj()));
            productGroupIDList.setOwnerID(ObjectIdentifier.build(productGroup.getOwnerID(), productGroup.getOwnerObj()));
            productGroupIDList.setXChipSize(productGroup.getXChipSize());
            productGroupIDList.setYChipSize(productGroup.getYChipSize());
            productGroupIDList.setPlanCycleTime(productGroup.getPlanCycleTime());
            productGroupIDList.setPlanYield(productGroup.getPlanYield());
            productGroupIDList.setGrossDieCount(productGroup.getDieCount() != null ? productGroup.getDieCount().longValue() : null);

            productGroupIDListAttributes.add(productGroupIDList);
        }

        return productGroupIDListAttributes;
    }
}
