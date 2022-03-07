package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.factory.CimBank;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptBank .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/1/3 14:03
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptBank extends ScriptEntity<CimBank> {
    ScriptBank(CimBank bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the Bank.
     *
     * @return bank id
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 14:06
     */
    public String bankId() {
        return bizObject.getIdentifier();
    }
}
