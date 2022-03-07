package com.fa.cim.pcs.entity;

import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.newcore.bo.code.CimCategory;
import com.fa.cim.newcore.bo.code.CimCode;
import com.fa.cim.pcs.annotations.PcsEntity;

import java.util.List;
import java.util.Optional;

/**
 * <p>ScriptCategory .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:36
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptCategory extends ScriptEntity<CimCategory> {

    ScriptCategory(CimCategory bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the Category.
     *
     * @return category id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:48
     */
    public String categoryId() {
        return bizObject.getIdentifier();
    }

    /**
     * Get all Code of the Category.
     *
     * @return array of {@link ScriptCode}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:51
     */
    public ScriptCode[] allCodes() {
        List<CimCode> cimCodes = bizObject.allCodes();
        ScriptCode[] codes = new ScriptCode[CimArrayUtils.getSize(cimCodes)];
        Optional.ofNullable(cimCodes).ifPresent(list -> {
            int count = 0;
            for (CimCode code : list) {
                codes[count++] = factory.generateScriptEntity(ScriptCode.class, code);
            }
        });
        return codes;
    }

}
