package com.fa.cim.enums;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/28          ********            lightyh                create file
 *
 * @author: light
 * @date: 2019/11/28 9:45
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class MethodEnums {

    public enum LotEquipmentOrderGetByLotStatusEnums {
        setQueuedMachine(0),
        setOperationDispatchedMachine(1),
        setCassetteMachine(2),
        setNoMachine(3);
        private int type;

        LotEquipmentOrderGetByLotStatusEnums(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
        public static LotEquipmentOrderGetByLotStatusEnums getByType(int type){
            for (LotEquipmentOrderGetByLotStatusEnums typeName : values()) {
                if (typeName.getType() == type) {
                    return typeName;
                }
            }
            return null;
        }
    }

    public enum CassetteLotListGetWithPriorityOrderEnums {
        waiting(0),
        hold(1),
        bankIn(2);
        private int type;
        CassetteLotListGetWithPriorityOrderEnums(int type){
            this.type = type;
        }
    }

}