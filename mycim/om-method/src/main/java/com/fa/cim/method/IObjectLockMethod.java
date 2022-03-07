package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.newcore.bo.CimBO;

import java.util.Collection;
import java.util.List;

/**
 * description:
 * This file use to define the ILotMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 *
 * @author Bear
 * @since 2018/6/21 10:29
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

public interface IObjectLockMethod {

    void objectLockForEquipmentResource(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier objectID, String className);

    void advancedObjectLock(Infos.ObjCommon strObjCommonIn, Inputs.ObjAdvanceLockIn objAdvanceLockIn);

    void objectLock(Infos.ObjCommon strObjCommonIn, Class<? extends CimBO> clazz, ObjectIdentifier objectID);

    void advancedObjectLockForEquipmentResource(Infos.ObjCommon strObjCommonIn, Inputs.ObjAdvancedObjectLockForEquipmentResourceIn objAdvancedObjectLockForEquipmentResourceIn);

    void objectSequenceLock(Infos.ObjCommon strObjCommonIn, Class<? extends CimBO> clazz, Collection<ObjectIdentifier> objectIDList);

    List<ObjectIdentifier> objectLockForEquipmentContainerPosition(Infos.ObjCommon strObjCommonIn, Inputs.ObjObjectLockForEquipmentContainerPositionIn objObjectLockForEquipmentContainerPositionIn);

    void objectLockForEqpMonitorJob(Infos.ObjCommon strObjCommonIn, ObjectIdentifier eqpMonitorID, ObjectIdentifier eqpMonitorJobID);

    /**
     * Make Write-Locking for specified object.
     * <p> This function can lock for the following objects:
     * <p> - Machine
     * <p> - StorageMachine
     * <p> - Cassette
     * <p> - Lot
     * <p> - ProductRequest
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/17 10:44
     */
    void objectLockWithoutDoubleLock(Infos.ObjCommon strObjCommonIn, List<ObjectIdentifier> objectIDList, String SPClassName);

    void advanceLockChamberForReporting(Infos.ObjCommon strCommonIn, ObjectIdentifier lotID, String waferId);
}
