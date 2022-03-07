package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.prodspec.CimTechnology;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptTechnology .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:39
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptTechnology extends ScriptEntity<CimTechnology> {

    ScriptTechnology(CimTechnology bizObject) {
        super(bizObject);
    }

    /**
     * Get the identifier of Technology.
     *
     * @return identifier
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:29
     */
    public String technologyId() {
        return bizObject.getIdentifier();
    }

}
