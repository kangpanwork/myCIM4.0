package com.fa.cim.dto.pp;

/**
 * the working type of the target entity
 *
 * @author Yuri
 */
public enum EntityType {

    /**
     * target type is a lot
     */
    Lot,

    /**
     * target type is a durable
     */
    Durable,

    /**
     * target type is a piece of equipment
     */
    Equipment,

    /**
     * target type is a piece of equipment with a control job
     */
    EquipmentWithCj // do we still need it?
}
