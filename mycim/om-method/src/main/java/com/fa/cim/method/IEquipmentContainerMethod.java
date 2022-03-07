package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;

/**
 * <p>IEquipmentContainerMethod .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/5/8 15:53
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface IEquipmentContainerMethod {

    /**
     * @version 1.0
     * @author ZQI
     * @date 2020/5/8 15:57
     */
    void equipmentContainerWaferStore(Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerWaferStoreIn objEquipmentContainerWaferStoreIn);

    /**
     * Get the specified equipment's Equipment Container Information .
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/5/8 16:02
     */
    Infos.EqpContainerInfo equipmentContainerInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    void equipmentContainerWaferRetrieve (Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerWaferRetrieveIn objEquipmentContainerWaferRetrieveIn);

    /**
     * description:
     * <p>equipmentContainer_reservation_Update</p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/5/11/011 16:14
     */
    void equipmentContainerReservationUpdate(Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerReservationUpdateIn objEquipmentContainerReservationUpdateIn);

    void equipmentContainerMaxRsvCountUpdate(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID, ObjectIdentifier equipmentContainerID , Integer maxRsvCount);
}
