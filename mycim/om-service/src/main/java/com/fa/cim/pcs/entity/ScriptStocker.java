package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptStocker .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:40
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptStocker extends ScriptEntity<CimStorageMachine> {

    ScriptStocker(CimStorageMachine bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the Stocker.
     *
     * @return stocker id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:54
     */
    public String stockerId() {
        return bizObject.getIdentifier();
    }

}
