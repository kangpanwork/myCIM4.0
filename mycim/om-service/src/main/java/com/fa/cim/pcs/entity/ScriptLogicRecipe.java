package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptLogicRecipe .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/12/24 16:35         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:35
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptLogicRecipe extends ScriptEntity<CimLogicalRecipe> {

    ScriptLogicRecipe(CimLogicalRecipe bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the LogicRecipe.
     *
     * @return logic recipe ID
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:43
     */
    public String logicRecipeId() {
        return bizObject.getIdentifier();
    }
}
