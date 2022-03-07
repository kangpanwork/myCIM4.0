package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptFixture .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:45
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptFixture extends ScriptEntity<CimProcessDurable> {

    ScriptFixture(CimProcessDurable bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the Fixture.
     *
     * @return fixture id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:57
     */
    public String fixtureId() {
        return bizObject.getIdentifier();
    }
}
