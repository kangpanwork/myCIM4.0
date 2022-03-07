package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptCarrier .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:33
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptCarrier extends ScriptEntity<CimCassette> {

    ScriptCarrier(CimCassette bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the Carrier.
     *
     * @return carrier id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:36
     */
    public String carrierId() {
        return bizObject.getIdentifier();
    }

    /**
     * Get category of the Carrier.
     *
     * @return category
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:37
     */
    public String carrierCategory() {
        return bizObject.getCassetteCategory();
    }

}
