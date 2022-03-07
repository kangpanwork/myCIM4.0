package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.pcs.annotations.PcsEntity;

@PcsEntity
public class ScriptEquipment extends ScriptEntity<CimMachine> {

    public ScriptEquipment(CimMachine bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the Equip.
     *
     * @return equipment id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:34
     */
    public String equipmentId() {
        return bizObject.getIdentifier();
    }

    /**
     * Get type of the Equip.
     *
     * @return equipment type.
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:35
     */
    public String equipmentType() {
        return bizObject.getMachineType();
    }

}
