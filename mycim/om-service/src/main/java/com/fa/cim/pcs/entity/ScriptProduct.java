package com.fa.cim.pcs.entity;

import com.fa.cim.common.utils.Validations;
import com.fa.cim.newcore.bo.prodspec.CimProductGroup;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.pcs.annotations.PcsEntity;

@PcsEntity
public class ScriptProduct extends ScriptEntity<CimProductSpecification> {

    public ScriptProduct(CimProductSpecification bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the product specification.
     *
     * @return product id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 17:49
     */
    public String productId() {
        return bizObject.getIdentifier();
    }

    /**
     * Get identifier of product group.
     *
     * @return product group
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 17:49
     */
    public String productGroupId() {
        CimProductGroup productGroup = bizObject.getProductGroup();
        Validations.check(null == productGroup, retCodeConfig.getNotFoundProductGroup());
        return productGroup.getIdentifier();
    }
}
