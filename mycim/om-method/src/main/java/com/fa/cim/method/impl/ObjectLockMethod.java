package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.method.IObjectLockMethod;
import com.fa.cim.newcore.bo.CimBO;
import com.fa.cim.newcore.bo.dispatch.CimDispatcher;
import com.fa.cim.newcore.bo.dispatch.CimFlowBatch;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimMachineContainer;
import com.fa.cim.newcore.bo.machine.CimMaterialLocation;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.planning.CimProductRequest;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimMonitorGroup;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import com.fa.cim.newcore.lock.definition.StdAttLockDefinitionEnum;
import com.fa.cim.newcore.lock.manager.ObjectLockManager;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.mchnmngm.BufferResource;
import com.fa.cim.newcore.standard.mtrlmngm.Material;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 *
 * @author Bear
 * @since 2018/6/21 10:31
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class ObjectLockMethod implements IObjectLockMethod {

    @Autowired
    private ObjectLockManager lockManager;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private GenericCoreFactory genericCoreFactory;

    @Override
    public void objectLock(Infos.ObjCommon strObjCommonIn,
                           Class<? extends CimBO> clazz,
                           ObjectIdentifier objectID) {
        if (ObjectIdentifier.isEmpty(objectID)) {
            return;
        }
        if (null == clazz || !Modifier.isInterface(clazz.getModifiers())) {
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }

        if (clazz.equals(CimDispatcher.class)) {
            CimDispatcher dispatcher = null;
            if (!ObjectIdentifier.isEmptyWithRefKey(objectID) && ObjectIdentifier.isEmptyWithValue(objectID)) {
                dispatcher = baseCoreFactory.getBO(CimDispatcher.class, ObjectIdentifier.fetchReferenceKey(objectID));
            }
            if (dispatcher != null) {
                dispatcher.lock();
            } else {
                CimMachine anObj = baseCoreFactory.getBO(CimMachine.class, objectID);
                if (null != anObj) {
                    Optional.ofNullable(anObj.getDispatcher())
                            .orElseThrow(() -> new ServiceException(retCodeConfig.getNotFoundEqpDispatcher())).lock();
                } else {
                    throw new ServiceException(retCodeConfig.getNotFoundEqp());
                }
            }
        } else {
            lockManager.lock(clazz, objectID);
        }
    }

    @Override
    public void objectSequenceLock(Infos.ObjCommon strObjCommonIn,
                                   Class<? extends CimBO> clazz,
                                   Collection<ObjectIdentifier> objectIDList) {
        if (null == clazz || !Modifier.isInterface(clazz.getModifiers())) {
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }
        lockManager.lockAll(clazz, objectIDList);
    }

    @Override
    public void objectLockForEquipmentResource(Infos.ObjCommon strObjCommonIn,
                                               ObjectIdentifier equipmentID,
                                               ObjectIdentifier objectID,
                                               String className) {
        Validations.check(ObjectIdentifier.isEmpty(equipmentID), retCodeConfig.getInvalidInputParam());
        Validations.check(ObjectIdentifier.isEmpty(objectID), retCodeConfig.getInvalidInputParam());

        StdAttLockDefinitionEnum lockStrategy;
        switch (className) {
            case BizConstant.SP_CLASSNAME_POSPORTRESOURCE:
                lockStrategy = StdAttLockDefinitionEnum.LOCK_PORT_RESOURCE_FOR_MACHINE;
                break;
            case BizConstant.SP_CLASSNAME_POSMATERIALLOCATION:
                lockStrategy = StdAttLockDefinitionEnum.LOCK_MATERIAL_LOCATIONS_FOR_MACHINE;
                break;
            case BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE:
                lockStrategy = StdAttLockDefinitionEnum.LOCK_RETICLE_POD_PORT_RESOURCE_FOR_MACHINE;
                break;
            case BizConstant.SP_CLASSNAME_POSPROCESSRESOURCE:
                lockStrategy = StdAttLockDefinitionEnum.LOCK_PROCESS_RESOURCE_FOR_MACHINE;
                break;
            case BizConstant.SP_CLASSNAME_POSMACHINECONTAINER:
                lockStrategy = StdAttLockDefinitionEnum.LOCK_MACHINE_CONTAINER_FOR_MACHINE;
                break;
            default:
                throw new ServiceException(String.format("Invalid Class Name [%s]", className));
        }
        String resourceID = ObjectIdentifier.fetchValue(objectID);
        lockManager.advanceLock(lockStrategy, equipmentID, resourceID);
    }

    @Override
    public void advancedObjectLock(Infos.ObjCommon strObjCommonIn,
                                   Inputs.ObjAdvanceLockIn objAdvanceLockIn) {
        ObjectIdentifier objectID = objAdvanceLockIn.getObjectID();
        String className = objAdvanceLockIn.getClassName();
        if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSMACHINE, className)) {
            String elementType = objAdvanceLockIn.getObjectType();
            StdAttLockDefinitionEnum lockStrategy;
            switch (elementType) {
                case BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT:
                    lockStrategy = StdAttLockDefinitionEnum.LOCK_PROCESSING_LOT_FOR_MACHINE;
                    break;
                case BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE:
                    lockStrategy = StdAttLockDefinitionEnum.LOCK_LOAD_CARRIER_FOR_MACHINE;
                    break;
                case BizConstant.SP_OBJECTLOCK_OBJECTTYPE_USERDEFINEDDATA:
                    lockStrategy = StdAttLockDefinitionEnum.LOCK_USER_DATA_FOR_MACHINE;
                    break;
                case BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT:
                    this.objectLock(strObjCommonIn, CimMachine.class, objectID);
                    lockStrategy = StdAttLockDefinitionEnum.LOCK_MAIN_ENTITY_FOR_MACHINE;
                    objAdvanceLockIn.setKeyList(Collections.singletonList(ObjectIdentifier.fetchValue(objectID)));
                    break;
                default:
                    throw new ServiceException(String.format("Invalid Class Name [%s]", className));
            }
            objAdvanceLockIn.getKeyList().forEach(key -> lockManager.advanceLock(lockStrategy, objectID, key));
        } else if (CimStringUtils.equals(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT, objAdvanceLockIn.getObjectType())) {
            switch (className) {
                case BizConstant.SP_CLASSNAME_POSLOT:
                    this.objectLock(strObjCommonIn, CimLot.class, objectID);
                    break;
                case BizConstant.SP_CLASSNAME_POSCASSETTE:
                    this.objectLock(strObjCommonIn, CimCassette.class, objectID);
                    break;
                case BizConstant.SP_CLASSNAME_POSMONITORGROUP:
                    this.objectLock(strObjCommonIn, CimMonitorGroup.class, objectID);
                    break;
                case BizConstant.SP_CLASSNAME_POSFLOWBATCH:
                    this.objectLock(strObjCommonIn, CimFlowBatch.class, objectID);
                    break;
                default:
                    throw new ServiceException(String.format("Invalid Class Name [%s]", className));
            }
        }
    }

    @Override
    public void advancedObjectLockForEquipmentResource(Infos.ObjCommon strObjCommonIn,
                                                       Inputs.ObjAdvancedObjectLockForEquipmentResourceIn in) {
        String className = in.getClassName();
        CimMachine cimMachine = genericCoreFactory.getBO(CimMachine.class, in.getEquipmentID());
        List<String> paramList;
        String objectId = ObjectIdentifier.fetchValue(in.getObjectID());
        switch (className) {
            case BizConstant.SP_CLASSNAME_POSMATERIALLOCATION:
                paramList = Collections.singletonList(objectId);
                break;
            case BizConstant.SP_CLASSNAME_POSMATERIALLOCATION_BYCJ:
                paramList = cimMachine.allBufferResources().stream().filter(buffer ->
                        buffer.allMaterialLocations().stream().map(ml -> (CimMaterialLocation) ml).anyMatch(ml -> {
                            CimControlJob controlJob = ml.getControlJob();
                            return controlJob != null && CimStringUtils.equals(controlJob.getIdentifier(), objectId);
                        })
                ).map(BufferResource::getIdentifier).collect(Collectors.toList());
                break;
            case BizConstant.SP_CLASSNAME_POSMATERIALLOCATION_BYCASTID:
                paramList = cimMachine.allBufferResources().stream().filter(buffer ->
                        buffer.allMaterialLocations().stream().map(ml -> (CimMaterialLocation) ml).anyMatch(ml -> {
                            Material material = Optional.ofNullable(ml.getAllocatedMaterial())
                                    .orElseGet(ml::getMaterial);
                            return material != null && material.getMaterialContainer() == null;
                        })
                ).map(BufferResource::getIdentifier).collect(Collectors.toList());
                break;
            case BizConstant.SP_CLASSNAME_POSMATERIALLOCATION_EMPTYML:
                paramList = cimMachine.allBufferResources().stream().filter(buffer ->
                        buffer.allMaterialLocations().stream().map(ml -> (CimMaterialLocation) ml).anyMatch(ml -> {
                            Material material = Optional.ofNullable(ml.getAllocatedMaterial())
                                    .orElseGet(ml::getMaterial);
                            if (material == null) return false;
                            MaterialContainer materialContainer = material.getMaterialContainer();
                            return materialContainer != null && CimStringUtils.equals(materialContainer.getIdentifier(), objectId);
                        })
                ).map(BufferResource::getIdentifier).collect(Collectors.toList());
                break;
            default:
                throw new ServiceException(String.format("Invalid Class Name [%s]", className));
        }
        paramList.forEach(param -> lockManager.advanceLock(StdAttLockDefinitionEnum.LOCK_MATERIAL_LOCATIONS_FOR_MACHINE, in.getEquipmentID(), param));
    }

    @Override
    public List<ObjectIdentifier> objectLockForEquipmentContainerPosition(Infos.ObjCommon strObjCommonIn,
                                                                          Inputs.ObjObjectLockForEquipmentContainerPositionIn in) {
        List<ObjectIdentifier> retVal;
        ObjectIdentifier equipmentID = in.getEquipmentID();
        List<Infos.StartCassette> strStartCassette = in.getStrStartCassette();
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundMachine(), equipmentID.getValue());
        assert aMachine != null;
        if (CimArrayUtils.isNotEmpty(strStartCassette)) {
            int startWaferCount = (int) strStartCassette.stream()
                    .filter(data -> CimArrayUtils.isNotEmpty(data.getLotInCassetteList()))
                    .flatMap(data -> data.getLotInCassetteList().stream())
                    .filter(data -> CimBooleanUtils.isTrue(data.getMoveInFlag()))
                    .count();

            List<CimMachineContainer> aEqpContSeq = aMachine.allMachineContainers();
            Validations.check(CimArrayUtils.isEmpty(aEqpContSeq), retCodeConfigEx.getNotFoundContainers(), equipmentID.getValue());
            retVal = aEqpContSeq.stream()
                    .filter(mc -> startWaferCount <= CimArrayUtils.getSize(mc.getVacantMachineContainerPositions()))
                    .flatMap(mc -> mc.getVacantMachineContainerPositions().stream())
                    .map(mcp -> ObjectIdentifier.build(mcp.getIdentifier(), mcp.getPrimaryKey()))
                    .collect(Collectors.toList());

        } else if (!ObjectIdentifier.isEmpty(in.getControlJobID())) {
            String controlJobId = ObjectIdentifier.fetchValue(in.getControlJobID());
            retVal = aMachine.findMachineContainerPositionForControlJob(controlJobId)
                    .stream().map(mcp -> ObjectIdentifier.build(mcp.getIdentifier(), mcp.getPrimaryKey()))
                    .collect(Collectors.toList());
        } else if (!ObjectIdentifier.isEmpty(in.getDestCassetteID())) {
            List<CimMachineContainer> aEqpContSeq = aMachine.allMachineContainers();
            Validations.check(CimArrayUtils.isEmpty(aEqpContSeq), retCodeConfigEx.getNotFoundContainers(), equipmentID.getValue());
            retVal = aEqpContSeq.stream().filter(mc -> CimArrayUtils.isNotEmpty(mc.allMachineContainerPositions()))
                    .flatMap(mc -> mc.allMachineContainerPositions().stream())
                    .filter(mcp -> {
                        CimCassette destCassette = mcp.getDestCassette();
                        return destCassette != null && in.getDestCassetteID().equals(destCassette.getIdentifier());
                    }).map(mcp -> ObjectIdentifier.build(mcp.getIdentifier(), mcp.getPrimaryKey()))
                    .collect(Collectors.toList());
        } else if (!ObjectIdentifier.isEmpty(in.getWaferID())) {
            String waferId = ObjectIdentifier.fetchValue(in.getWaferID());
            retVal = Optional.ofNullable(aMachine.findMachineContainerPositionForWafer(waferId))
                    .map(mcp -> ObjectIdentifier.build(mcp.getIdentifier(), mcp.getPrimaryKey()))
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);
        } else {
            retVal = Collections.emptyList();
        }
        retVal.stream().map(ObjectIdentifier::fetchValue)
                .forEach(id -> lockManager.advanceLock(StdAttLockDefinitionEnum.LOCK_MACHINE_CONTAINER_POSITION_FOR_MACHINE,
                        in.getEquipmentID(), id));
        return retVal;
    }

    @Override
    public void objectLockForEqpMonitorJob(Infos.ObjCommon strObjCommonIn,
                                           ObjectIdentifier eqpMonitorID,
                                           ObjectIdentifier eqpMonitorJobID) {
        Validations.check(ObjectIdentifier.isEmpty(eqpMonitorID), retCodeConfig.getInvalidInputParam());
        Validations.check(ObjectIdentifier.isEmpty(eqpMonitorJobID), retCodeConfig.getInvalidInputParam());

        lockManager.advanceLock(StdAttLockDefinitionEnum.LOCK_EQP_MONITOR_JOB_FOR_EQP_MONITOR, eqpMonitorID,
                ObjectIdentifier.fetchValue(eqpMonitorJobID));
    }

    @Override
    public void objectLockWithoutDoubleLock(Infos.ObjCommon strObjCommonIn, List<ObjectIdentifier> objectIDList, String SPClassName) {
        log.info("Object count: " + CimArrayUtils.getSize(objectIDList));
        log.info("Object class: " + SPClassName);
        Optional.ofNullable(objectIDList).ifPresent(list -> {
            for (ObjectIdentifier objectIdentifier : list) {
                if (ObjectIdentifier.isEmpty(objectIdentifier)) {
                    continue;
                }
                CimBO boBusiness = null;
                switch (SPClassName) {
                    case BizConstant.SP_CLASSNAME_POSMACHINE:
                        boBusiness = baseCoreFactory.getBO(CimMachine.class, objectIdentifier);
                        break;
                    case BizConstant.SP_CLASSNAME_POSSTORAGEMACHINE:
                        boBusiness = baseCoreFactory.getBO(CimStorageMachine.class, objectIdentifier);
                        break;
                    case BizConstant.SP_CLASSNAME_POSCASSETTE:
                        boBusiness = baseCoreFactory.getBO(CimCassette.class, objectIdentifier);
                        break;
                    case BizConstant.SP_CLASSNAME_POSLOT:
                        boBusiness = baseCoreFactory.getBO(CimLot.class, objectIdentifier);
                        break;
                    case BizConstant.SP_CLASSNAME_POSPRODUCTREQUEST:
                        boBusiness = baseCoreFactory.getBO(CimProductRequest.class, objectIdentifier);
                        break;
                    case BizConstant.SP_CLASSNAME_POSRETICLEPOD:
                        boBusiness = baseCoreFactory.getBO(CimReticlePod.class, objectIdentifier);
                        break;
                    default:
                        log.info("The specified class name is invalid.");
                        break;

                }
                if (null != boBusiness) boBusiness.lock();
            }
        });
    }

    @Override
    public void advanceLockChamberForReporting(Infos.ObjCommon strCommonIn, ObjectIdentifier lotID, String waferId) {
        Optional.ofNullable(genericCoreFactory.getBO(CimLot.class, lotID))
                .map(CimLot::getProcessOperation)
                .map(CimProcessOperation::getPrimaryKey)
                .ifPresent(primaryKey -> lockManager.advanceLock(StdAttLockDefinitionEnum.LOCK_WAFER_CHAMBER_FOR_REPORT, primaryKey, waferId));
    }
}
