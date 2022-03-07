package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.code.CimCode;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptCode .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:35
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptCode extends ScriptEntity<CimCode> {

    ScriptCode(CimCode bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the Code.
     *
     * @return code id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:44
     */
    public String codeId() {
        return bizObject.getIdentifier();
    }

    /**
     * Get the Category of the Code.
     *
     * @return {@link ScriptCategory}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:46
     */
    public ScriptCategory category() {
        String category = bizObject.getCategory();
        return factory.category(category);
    }

}
