package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptReticlePod .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:41
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptReticlePod extends ScriptEntity<CimReticlePod> {

    ScriptReticlePod(CimReticlePod bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the ReticlePod.
     *
     * @return reticle pod id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:55
     */
    public String reticlePodId() {
        return bizObject.getIdentifier();
    }
}
