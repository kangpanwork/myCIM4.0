package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptMachineRecipe .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:34
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptMachineRecipe extends ScriptEntity<CimMachineRecipe> {

    ScriptMachineRecipe(CimMachineRecipe bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the MachineRecipe.
     *
     * @return recipe id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:41
     */
    public String recipeId() {
        return bizObject.getIdentifier();
    }
}
