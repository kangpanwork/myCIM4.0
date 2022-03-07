package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.factory.CimMESFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * <p>ScriptGlobal .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/5/18 14:12
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Component
@Lazy
public class ScriptGlobal extends ScriptEntity<CimMESFactory> {
    ScriptGlobal(CimMESFactory bizObject) {
        super(bizObject);
    }
}
