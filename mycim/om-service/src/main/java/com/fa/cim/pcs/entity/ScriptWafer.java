package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.pcs.annotations.PcsEntity;

@PcsEntity
public class ScriptWafer extends ScriptEntity<CimWafer> {

    public ScriptWafer(CimWafer bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of this Wafer.
     *
     * @return wafer id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:39
     */
    public String waferId() {
        return bizObject.getIdentifier();
    }

    public String collectedDataWaferId() {
        return "0";
    }
}
