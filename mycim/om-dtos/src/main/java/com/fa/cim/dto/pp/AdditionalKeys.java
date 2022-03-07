package com.fa.cim.dto.pp;

/**
 * for adding additional entity IDs in {@link PostProcessSource}
 *
 * @author Yuri
 */
public enum AdditionalKeys {

    /**
     * Hold release lots for EDC check.
     */
    HoldReleaseLots(EntityType.Lot),

    /**
     * Partial move out action code
     * <ui>
     * <li>MoveOut</li>
     * <li>MoveOutWithHold</li>
     * <li>MoveInCancel</li>
     * <li>MoveInCancelWithHold</li>
     * </ui>
     */
    MoveOut(EntityType.Lot),
    MoveOutWithHold(EntityType.Lot),
    MoveInCancel(EntityType.Lot),
    MoveInCancelWithHold(EntityType.Lot);

    private final EntityType entityType;

    AdditionalKeys(EntityType entityType) {
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }


}
