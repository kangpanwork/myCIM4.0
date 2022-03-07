package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;

/**
 * <p>IEquipmentForDurableMethod .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/6/17 14:30
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface IEquipmentForDurableMethod {

    /**
     * Transaction ID and SpecialControl of Equipment Category consisytency Check.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/17 14:16
     */
    void equipmentSpecialControlVsTxIDCheckCombination(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * If equipment has any in-process durables, get default Standby machine state.
     * <p> If equipment dies not have in-process durable, get default Productive machine state.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/22 10:13
     */
    ObjectIdentifier equipmentRecoverStateGetManufacturingForDurable(Infos.ObjCommon objCommon,
                                                                     String operationType,
                                                                     ObjectIdentifier equipmentID,
                                                                     ObjectIdentifier durableControlJobID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strEquipmentdurablesWhatNextDRin
     * @return com.fa.cim.dto.Infos.EquipmentDurablesWhatNextDROut
     * @exception
     * @author ho
     * @date 2020/6/30 11:25
     */
    Infos.EquipmentDurablesWhatNextDROut equipmentDurablesWhatNextDR(
            Infos.EquipmentDurablesWhatNextDROut strEquipmentdurablesWhatNextDRout,
            Infos.ObjCommon                       strObjCommonIn,
            Infos.EquipmentDurablesWhatNextDRIn strEquipmentdurablesWhatNextDRin );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param equipmentID
     * @param portID
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/7 11:17
     */
    void machineReticlePodPortReserveCancel
            (
                    Infos.ObjCommon                           strObjCommonIn,
                    ObjectIdentifier                         equipmentID,
                    ObjectIdentifier                         portID);

    Outputs.EquipmentTargetPortPickupOut durableEquipmentTargetPortPickup(Infos.ObjCommon objCommonIn,
                                                                   Infos.EqpPortInfoOrderByGroup eqpPortInfoOrderByGroup,
                                                                   Infos.EqpBrInfo eqpBrInfo,
                                                                   Infos.EqpPortInfo eqpPortInfo);
}
