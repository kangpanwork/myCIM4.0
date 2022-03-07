package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptProcess .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/1/3 14:39         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/1/3 14:39
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptProcess extends ScriptEntity<CimProcessDefinition> {

    ScriptProcess(CimProcessDefinition bizObject) {
        super(bizObject);
    }

    /**
     * Get the identifier of the process.
     *
     * @return process id
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 14:40
     */
    public String processId() {
        return bizObject.getIdentifier();
    }
}
