package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptReticle .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:42
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptReticle extends ScriptEntity<CimProcessDurable> {

    ScriptReticle(CimProcessDurable bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the Reticle.
     *
     * @return reticle id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:56
     */
    public String reticleId() {
        return bizObject.getIdentifier();
    }
}
