package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;

import java.util.List;

/**
 * <p>IEquipmentContainerPositionMethod .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/28 17:14
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface IEquipmentContainerPositionMethod {


    /**
     * Equipment container position information is written in for SLMStartReservation.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/28 17:18
     */
    void equipmentContainerPositionReservationCreate(Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerPositionReservationCreateIn reservationCreateIn);

    /**
     * description:
     * <p>equipmentContainerPosition_info_GetByDestCassette</p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/4/30/030 13:05
     */
    List<Infos.EqpContainerPosition> equipmentContainerPositionInfoGetByDestCassette(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID);

    /**
     * Get equipment container position info.
     * <p> equipmentContainerPosition_info_Get</p>
     * @version 1.0
     * @author ZQI
     * @date 2020/5/8 14:48
     */
    Infos.EqpContainerPositionInfo equipmentContainerPositionInfoGet(Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerPositionInfoGetIn positionInfoGetIn);

    List<ObjectIdentifier> equipmentContainerPositionInfoClear(Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerPositionInfoClearIn in);
}
