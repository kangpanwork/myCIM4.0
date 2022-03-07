package com.fa.cim.common.constant;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/27        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/11/27 14:01
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public enum ConstraintClassEnum {
    INHIBITCLASSID_PRODUCT("Product Specification"),
    INHIBITCLASSID_ROUTE("Route"),
    INHIBITCLASSID_OPERATION("Operation"),
    INHIBITCLASSID_PROCESS("Process Definition"),
    INHIBITCLASSID_MACHINERECIPE("Machine Recipe"),
    INHIBITCLASSID_EQUIPMENT("Equipment"),
    INHIBITCLASSID_RETICLE("Reticle"),
    INHIBITCLASSID_RETICLEGROUP("Reticle Group"),
    INHIBITCLASSID_FIXTURE("Fixture"),
    INHIBITCLASSID_FIXTUREGROUP("Fixture Group"),
    INHIBITCLASSID_STAGE("Stage"),
    INHIBITCLASSID_MODULEPD("Module Process Definition"),
    INHIBITCLASSID_CHAMBER("Chamber"),
    INHIBITCLASSID_LOT("Lot");

    private String value;

    ConstraintClassEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ConstraintClassEnum get(String value) {
        for (ConstraintClassEnum constraintClassEnum : values()) {
            if (constraintClassEnum.getValue().equals(value)) {
                return constraintClassEnum;
            }
        }
        return null;
    }
}