package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IPortMethod;
import com.fa.cim.newcore.bo.code.CimMachineOperationMode;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimPortResource;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.code.CodeDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.mchnmngm.MaterialLocation;
import com.fa.cim.newcore.standard.mchnmngm.PortResource;
import com.fa.cim.newcore.standard.mtrlmngm.Material;
import com.fa.cim.newcore.standard.prsnmngm.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * PortCompImpl .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/11        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/7/11 16:57
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class PortMethod  implements IPortMethod {

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Override
    public Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationModeGet(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier portID) {
        Outputs.ObjPortResourceCurrentOperationModeGetOut retVal = new Outputs.ObjPortResourceCurrentOperationModeGetOut();
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(aPosMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), "*****"));

        PortResource aPort = aPosMachine.findPortResourceNamed(portID.getValue());
        Validations.check(aPort == null, new OmCode(retCodeConfig.getNotFoundPort(), portID.getValue()));
        CimPortResource aPosPort = (CimPortResource) aPort;
        //-------------------------------------
        // Set output structure
        //-------------------------------------
        retVal.setEquipmentID(ObjectIdentifier.build(aPosMachine.getIdentifier(), aPosMachine.getPrimaryKey()));
        retVal.setPortID(ObjectIdentifier.build(aPosPort.getIdentifier(), aPosPort.getPrimaryKey()));
        //-------------------------------------
        // Get object reference of Port Operatoin Mode
        // object reference must be retrieved
        //-------------------------------------
        CimMachineOperationMode aCurrentOpeMode = aPosPort.getMachineOperationMode();
        Validations.check(aCurrentOpeMode == null, retCodeConfig.getNotFoundMachineOperationMode());
        //-------------------------------------
        // Get detail info of PosMachineOperation
        //-------------------------------------
        CodeDTO.OperationModeInfo aCurrentOpeModeInfo = aCurrentOpeMode.getOperationModeInfo();
        //0.01 add start
        Validations.check(aCurrentOpeModeInfo == null, retCodeConfig.getNotFoundMachineOperationMode());
        //-------------------------------------
        // Set output structure
        //-------------------------------------
        Infos.OperationMode operationMode = new Infos.OperationMode();
        operationMode.setOperationMode(ObjectIdentifier.build(aCurrentOpeMode.getIdentifier(), aCurrentOpeMode.getPrimaryKey()));
        operationMode.setOnlineMode(aCurrentOpeModeInfo.getOnlineMode());
        operationMode.setDispatchMode(aCurrentOpeModeInfo.getDispatchMode());
        operationMode.setAccessMode(aCurrentOpeModeInfo.getAccessMode());
        operationMode.setMoveInMode(aCurrentOpeModeInfo.getOperationStartMode());
        operationMode.setMoveOutMode(aCurrentOpeModeInfo.getOperationCompMode());
        operationMode.setDescription(aCurrentOpeMode.getDescription());
        retVal.setOperationMode(operationMode);
        return retVal;
    }

    @Override
    public Outputs.ObjEquipmentPortGroupIDGetOut equipmentPortGroupIDGet(Infos.ObjCommon objCommon,
                                                                         Inputs.ObjEquipmentPortGroupIDGetIn objEquipmentPortGroupIDGetIn) {
        Outputs.ObjEquipmentPortGroupIDGetOut result = new Outputs.ObjEquipmentPortGroupIDGetOut();
        ObjectIdentifier equipmentId = objEquipmentPortGroupIDGetIn.getEquipmentId();
        ObjectIdentifier portId = objEquipmentPortGroupIDGetIn.getPortId();
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentId);
        Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentId.getValue()));
        PortResource aBasePort = aMachine.findPortResourceNamed(portId.getValue());
        Validations.check(aBasePort == null, new OmCode(retCodeConfig.getNotFoundPort(), portId.getValue()));
        com.fa.cim.newcore.bo.machine.CimPortResource aPosPort = (com.fa.cim.newcore.bo.machine.CimPortResource) aBasePort;
        Validations.check(aPosPort == null, new OmCode(retCodeConfig.getNotFoundPort(), portId.getValue()));
        result.setPortGroupId(aPosPort.getPortGroup());
        return result;
    }

    @Override
    public void equipmentPortStateChangeForTakeOutInMode(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, String portStatus, ObjectIdentifier cassetteID) {
        /*----------------------------------------------*/
        /*   Get eqp & port Resource Object       */
        /*----------------------------------------------*/
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(CimObjectUtils.isEmpty(aMachine), new OmCode(retCodeConfig.getNotFoundEquipment(), equipmentID.getValue()));

        // Get Port Resource Object
        PortResource aTmpPortResource = aMachine.findPortResourceNamed(portID.getValue());
        Validations.check(CimObjectUtils.isEmpty(aTmpPortResource),retCodeConfig.getNotFoundPort());
        com.fa.cim.newcore.bo.machine.CimPortResource aPosPortResource = (com.fa.cim.newcore.bo.machine.CimPortResource) aTmpPortResource;

        /*---------------------------*/
        /*   Change port State       */
        /*---------------------------*/
        switch (portStatus) {
            case BizConstant.SP_PORTRSC_PORTSTATE_LOADAVAIL:
                aPosPortResource.makeLoadAvailable(); break;
            case BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ:
                aPosPortResource.makeLoadRequired(); break;
            case BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP:
                aPosPortResource.makeLoadComp(); break;
            case BizConstant.SP_PORTRSC_PORTSTATE_UNLOADAVAIL:
                aPosPortResource.makeUnloadAvailable(); break;
            case BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ:
                aPosPortResource.makeUnloadRequired(); break;
            case BizConstant.SP_PORTRSC_PORTSTATE_UNLOADCOMP:
                aPosPortResource.makeUnLoadComp(); break;
            case BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN:
                aPosPortResource.makeUnknown(); break;
            case BizConstant.SP_PORTRSC_PORTSTATE_DOWN:
                aPosPortResource.makeDown();
                break;
            default:
                throw new ServiceException(new OmCode(retCodeConfig.getUndefinedPortState(), portID.getValue(), portStatus));
        }

        /*----------------------------------------------*/
        /*       Update TimeStamp and person Info       */
        /*----------------------------------------------*/
        aPosPortResource.setLastStatusChangeTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());

       com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(CimObjectUtils.isEmpty(aPerson),new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        aPosPortResource.setLastStatusChangePerson(aPerson);

        aPosPortResource.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aPosPortResource.setLastClaimedPerson(aPerson);

        /*----------------------------------------------*/
        /*   change dispatchInfo for TakeOutIn mode     */
        /*----------------------------------------------*/
        if (BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ.equals(portStatus)
                || BizConstant.SP_PORTRSC_PORTSTATE_LOADAVAIL.equals(portStatus)
                || BizConstant.SP_PORTRSC_PORTSTATE_UNLOADCOMP.equals(portStatus)) {
            /*--------------------------------------------*/
            /*   clear Disp.unload Cast/lot information   */
            /*--------------------------------------------*/
            aPosPortResource.setDispatchUnloadLot(null);
            aPosPortResource.setDispatchUnloadCassette(null);
        } else if(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ.equals(portStatus)) {
            // change to Required
            equipmentMethod.equipmentDispatchStateChange(objCommon, equipmentID, portID,
                    BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED, null, null, null, null);
        } else if(BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP.equals(portStatus)
                || BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN.equals(portStatus)) {
            /*--------------------------------------------*/
            /*   clear Disp.unload Cast/Lot information   */
            /*   clear Disp.load Cast/Lot information     */
            /*--------------------------------------------*/
            aPosPortResource.setDispatchLoadLot(null);
            aPosPortResource.setDispatchLoadCassette(null);
            aPosPortResource.setDispatchUnloadLot(null);
            aPosPortResource.setDispatchUnloadCassette(null);
        }

        /*---------------------------------*/
        /*   Add cassette to Unload port   */
        /*---------------------------------*/
        if (BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ.equals(portStatus)) {
            /*---------------------------------------------------------*/
            /*   Get Associated port's (Load port's) LoadPurposeType   */
            /*---------------------------------------------------------*/
            com.fa.cim.newcore.bo.machine.CimPortResource aLoadPortResource = aPosPortResource.getAssociatedPort();
            Validations.check(CimObjectUtils.isEmpty(aLoadPortResource),retCodeConfig.getNotFoundPort());

            String loadPurposeType = aLoadPortResource.getLoadPurposeType();

            /*---------------------------------*/
            /*   Check Operation port or Not   */
            /*---------------------------------*/
            if (BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT.equals(loadPurposeType)
                    || BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT.equals(loadPurposeType)
                    || BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE.equals(loadPurposeType)) {
                /*-----------------------------------*/
                /*   Get Material Location of port   */
                /*-----------------------------------*/
                List<MaterialLocation> materialLocations = aPosPortResource.allMaterialLocations();
                Validations.check(CimArrayUtils.isEmpty(materialLocations),retCodeConfig.getMaterialLocationNotAvailable());
                Validations.check(CimObjectUtils.isEmpty(materialLocations.get(0)), retCodeConfig.getMaterialLocationNotAvailable());
            }
        }

        /*--------------------------------------*/
        /*   Remove cassette from Unload port   */
        /*--------------------------------------*/
        if (BizConstant.SP_PORTRSC_PORTSTATE_UNLOADCOMP.equals(portStatus)) {
            /*-----------------------------------*/
            /*   Get Material Location of port   */
            /*-----------------------------------*/
            List<MaterialLocation> materialLocations = aPosPortResource.allMaterialLocations();
            Validations.check(CimArrayUtils.isEmpty(materialLocations),retCodeConfig.getMaterialLocationNotAvailable());
            Validations.check(CimObjectUtils.isEmpty(materialLocations.get(0)), retCodeConfig.getMaterialLocationNotAvailable());

            /*-------------------------------------------*/
            /*   Check Material Location is Occupied     */
            /*-------------------------------------------*/
            boolean occupiedFlag =materialLocations.get(0).isOccupied();
            if (occupiedFlag) {
                materialLocations.get(0).materialSent();
            }
        }

    }

    @Override
    public void equipmentPortStateChange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, String portStatus, ObjectIdentifier cassetteID) {
        /*----------------------------------------------*/
        /*   Get eqp & port Resource Object       */
        /*----------------------------------------------*/
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(CimObjectUtils.isEmpty(aPosMachine),retCodeConfig.getNotFoundEquipment());

        /*--------------------------------------*/
        /*       Get Port Resource Object       */
        /*--------------------------------------*/
        PortResource aTmpPortResource = aPosMachine.findPortResourceNamed(portID.getValue());
        Validations.check(CimObjectUtils.isEmpty(aTmpPortResource),retCodeConfig.getNotFoundPort());
        com.fa.cim.newcore.bo.machine.CimPortResource aPosPortResource = (com.fa.cim.newcore.bo.machine.CimPortResource) aTmpPortResource;

        /*---------------------------*/
        /*   Change port State       */
        /*---------------------------*/
        if (BizConstant.SP_PORTRSC_PORTSTATE_LOADAVAIL.equals(portStatus)) {
            aPosPortResource.makeLoadAvailable();
        } else if (BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ.equals(portStatus)) {
            aPosPortResource.makeLoadRequired();
        } else if (BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP.equals(portStatus)) {
            aPosPortResource.makeLoadComp();
        } else if (BizConstant.SP_PORTRSC_PORTSTATE_UNLOADAVAIL.equals(portStatus)) {
            aPosPortResource.makeUnloadAvailable();
        } else if (BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ.equals(portStatus)) {
            aPosPortResource.makeUnloadRequired();
        } else if (BizConstant.SP_PORTRSC_PORTSTATE_UNLOADCOMP.equals(portStatus)) {
            aPosPortResource.makeUnLoadComp();
        } else if (BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN.equals(portStatus)) {
            aPosPortResource.makeUnknown();
        } else if (BizConstant.SP_PORTRSC_PORTSTATE_DOWN.equals(portStatus)) {
            aPosPortResource.makeDown();
        } else {
            throw new ServiceException(new OmCode(retCodeConfig.getUndefinedPortState(), portID.getValue(), portStatus));
        }

        /*----------------------------------------------*/
        /*       Update TimeStamp and person Info       */
        /*----------------------------------------------*/
        aPosPortResource.setLastStatusChangeTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());

        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(CimObjectUtils.isEmpty(aPerson),new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        aPosPortResource.setLastStatusChangePerson(aPerson);

        aPosPortResource.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aPosPortResource.setLastClaimedPerson(aPerson);

        /*--------------------------------------------------------*/
        /*   Update dispatch Status and Load/Unload lot/cassette  */
        /*--------------------------------------------------------*/
        boolean furnaceFlag;
        boolean swapFlag = false;
        boolean portAssignedFlag = false;
        boolean otherPortAssignedFlag = false;

        furnaceFlag = aPosMachine.isFurnace();

        if (furnaceFlag) {
            /*-------------------------------------*/
            /*   change dispatchInfo for Furnace   */
            /*-------------------------------------*/
            if (BizConstant.SP_PORTRSC_PORTSTATE_LOADAVAIL.equals(portStatus)
                    || BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ.equals(portStatus)) {
                /*------------------------*/
                /*   change to Required   */
                /*------------------------*/
                equipmentMethod.equipmentDispatchStateChange(objCommon, equipmentID, portID,
                        BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED, null, null, null, null);
            } else if (BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP.equals(portStatus)
                    || BizConstant.SP_PORTRSC_PORTSTATE_UNLOADCOMP.equals(portStatus)
                    || BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN.equals(portStatus)) {
                aPosPortResource.setDispatchLoadLot(null);
                aPosPortResource.setDispatchLoadCassette(null);
                aPosPortResource.setDispatchUnloadLot(null);
                aPosPortResource.setDispatchUnloadCassette(null);
            }
        } else {
            /*----------------------------------------------*/
            /*   change dispatchInfo for normal equipment   */
            /*----------------------------------------------*/
            if (BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ.equals(portStatus)
                    || BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ.equals(portStatus)) {
                /*------------------------*/
                /*   change to Required   */
                /*------------------------*/
                equipmentMethod.equipmentDispatchStateChange(objCommon, equipmentID, portID,
                        BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED, null, null, null, null);
            } else if (BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP.equals(portStatus)
                    || BizConstant.SP_PORTRSC_PORTSTATE_UNLOADCOMP.equals(portStatus)
                    ||  BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN.equals(portStatus)) {
                com.fa.cim.newcore.bo.durable.CimCassette aDisPatchReservedCassette = aPosPortResource.getDispatchLoadCassette();
                com.fa.cim.newcore.bo.product.CimControlJob aPosControlJob = null;
                if (!CimObjectUtils.isEmpty(aDisPatchReservedCassette)) {
                    aPosControlJob = aDisPatchReservedCassette.getControlJob();

                    if (!CimObjectUtils.isEmpty(aPosControlJob) || BizConstant.SP_PORTRSC_PORTSTATE_UNLOADCOMP.equals(portStatus)) {
                        aPosPortResource.setDispatchLoadLot(null);
                        aPosPortResource.setDispatchLoadCassette(null);
                        aPosPortResource.setDispatchUnloadLot(null);
                        aPosPortResource.setDispatchUnloadCassette(null);
                    }
                }
            }
        }

        /*---------------------------------*/
        /*   Add cassette to Unload port   */
        /*---------------------------------*/
        if (BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ.equals(portStatus)) {
            /*---------------------------------------------------------*/
            /*   Get Associated port's (Load port's) LoadPurposeType   */
            /*---------------------------------------------------------*/
            com.fa.cim.newcore.bo.machine.CimPortResource aLoadPortResource = aPosPortResource.getAssociatedPort();
            Validations.check(CimObjectUtils.isEmpty(aLoadPortResource),retCodeConfig.getNotFoundPort());

            String loadPurposeType = aLoadPortResource.getLoadPurposeType();

            /*---------------------------------*/
            /*   Check Operation port or Not   */
            /*---------------------------------*/
            if (BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT.equals(loadPurposeType)
                    || BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT.equals(loadPurposeType)
                    || BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE.equals(loadPurposeType)) {
                /*-----------------------------------*/
                /*   Get Material Location of port   */
                /*-----------------------------------*/
                List<MaterialLocation> materialLocations = aPosPortResource.allMaterialLocations();
                Validations.check(CimArrayUtils.isEmpty(materialLocations),retCodeConfig.getMaterialLocationNotAvailable());
                Validations.check(CimObjectUtils.isEmpty(materialLocations.get(0)), retCodeConfig.getMaterialLocationNotAvailable());
            }
        }

        /*--------------------------------------*/
        /*   Remove cassette from Unload port   */
        /*--------------------------------------*/
        if (BizConstant.SP_PORTRSC_PORTSTATE_UNLOADCOMP.equals(portStatus)) {
            /*-----------------------------------*/
            /*   Get Material Location of port   */
            /*-----------------------------------*/
            List<MaterialLocation> materialLocations = aPosPortResource.allMaterialLocations();
            Validations.check(CimArrayUtils.isEmpty(materialLocations),retCodeConfig.getMaterialLocationNotAvailable());
            Validations.check(CimObjectUtils.isEmpty(materialLocations.get(0)), retCodeConfig.getMaterialLocationNotAvailable());

            /*-------------------------------------------*/
            /*   Check Material Location is Occupied     */
            /*-------------------------------------------*/
            boolean occupiedFlag =materialLocations.get(0).isOccupied();
            if (occupiedFlag) {
                /*----------------------------------------------------*/
                /*   Clear and Re-Set Cassette to Material Location   */
                /*----------------------------------------------------*/
                materialLocations.get(0).materialSent();
            }
        }

    }

    @Override
    public Outputs.ObjEquipmentPortGroupInfoGetOut equipmentPortGroupInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, ObjectIdentifier portID) {
        List<Infos.EqpPortAttributes> initeEpPortAttributes = new ArrayList<>();
        Outputs.ObjEquipmentPortGroupInfoGetOut portGroupInfoGetOut = new Outputs.ObjEquipmentPortGroupInfoGetOut();
        portGroupInfoGetOut.setEqpPortAttributes(initeEpPortAttributes);

        /*-------------------------------------------*/
        /*              Check input value            */
        /*-------------------------------------------*/
        Validations.check(CimObjectUtils.isEmpty(equipmentID.getValue()) || CimObjectUtils.isEmpty(portGroupID) && CimObjectUtils.isEmpty(portID.getValue()),retCodeConfig.getInvalidInputParam());

        /*----------------------------------------------*/
        /*                  Get eqp               */
        /*----------------------------------------------*/
        CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(CimObjectUtils.isEmpty(equipment),retCodeConfig.getNotFoundEquipment());

        /*----------------------------------------------*/
        /*                  Check port group            */
        /*----------------------------------------------*/
        if (CimStringUtils.isEmpty(portGroupID)) {
            PortResource portResource = equipment.findPortResourceNamed(portID.getValue());
            com.fa.cim.newcore.bo.machine.CimPortResource port = null;
            if (portResource != null){
                port = (com.fa.cim.newcore.bo.machine.CimPortResource)portResource;
            }
            Validations.check(CimObjectUtils.isEmpty(port),retCodeConfig.getNotFoundPort());
            portGroupID = port.getPortGroup();
        }

        /*----------------------------------------------*/
        /*           Get each port information          */
        /*----------------------------------------------*/
        List<PortResource> allPorts = equipment.allPortResources();
        List<Infos.EqpPortAttributes> eqpPortAttributesList = portGroupInfoGetOut.getEqpPortAttributes();

        for (int i = 0; i < CimArrayUtils.getSize(allPorts); i++) {
            Infos.EqpPortAttributes eqpPortAttributes = new Infos.EqpPortAttributes();
            com.fa.cim.newcore.bo.machine.CimPortResource port = (com.fa.cim.newcore.bo.machine.CimPortResource)allPorts.get(i);

            Validations.check(CimObjectUtils.isEmpty(port),retCodeConfig.getNotFoundPort());

            /*----------------------------------------------*/
            /*           Check port's port group.           */
            /*If the port belongs other port group, omit it.*/
            /*----------------------------------------------*/
            if (!portGroupID.equals(port.getPortGroup())) {
                continue;
            }

            /*----------------------------------------------*/
            /*           Set port attribute information.    */
            /*----------------------------------------------*/
            eqpPortAttributes.setEquipmentID(equipmentID);
            eqpPortAttributes.setPortGroup(port.getPortGroup());
            eqpPortAttributes.setPortID(ObjectIdentifier.build(port.getIdentifier(),port.getPrimaryKey()));
            eqpPortAttributes.setPortName(port.getName());

            Person person = port.getOwner();
            if (!CimObjectUtils.isEmpty(person)) {
                ObjectIdentifier ownerId = new ObjectIdentifier(person.getIdentifier());
                eqpPortAttributes.setOwnerID(ownerId);
            }

            eqpPortAttributes.setLoadSequenceNumber(port.getLoadSeqInPortGroup().intValue());
            eqpPortAttributes.setUnloadSequenceNumber(port.getUnloadSeqInPortGroup().intValue());
            eqpPortAttributes.setLoadPurposeType(port.getLoadPurposeType());
            Boolean statusBool = false;
            statusBool = port.isStandBy();
            if (CimBooleanUtils.isTrue(statusBool)){
                eqpPortAttributes.setE10Status(CIMStateConst.CIM_E10_STANDBY);
            }
            statusBool = port.isNonScheduled();
            if (CimBooleanUtils.isTrue(statusBool)){
                eqpPortAttributes.setE10Status(CIMStateConst.CIM_E10_NON_SCHEDULED);
            }
            statusBool = port.isUnscheduledDowntime();
            if (CimBooleanUtils.isTrue(statusBool)){
                eqpPortAttributes.setE10Status(CIMStateConst.CIM_E10_UN_SCHEDULED_DOWN_TIME);
            }
            statusBool = port.isScheduledDowntime();
            if (CimBooleanUtils.isTrue(statusBool)){
                eqpPortAttributes.setE10Status(CIMStateConst.CIM_E10_SCHEDULED_DOWN_TIME);
            }
            statusBool = port.isEngineering();
            if (CimBooleanUtils.isTrue(statusBool)){
                eqpPortAttributes.setE10Status(CIMStateConst.CIM_E10_ENGINEERING);
            }
            statusBool = port.isProductive();
            if (CimBooleanUtils.isTrue(statusBool)) {
                eqpPortAttributes.setE10Status(CIMStateConst.CIM_E10_PRODUCTIVE);
            }

            /*switch (CIMStateEnum.get(port.getETenState())) {
                case CIMFW_E10_STANDBY:
                case CIMFW_E10_NONSCHEDULED:
                case CIMFW_E10_UNSCHEDULEDDOWNTIME:
                case CIMFW_E10_SCHEDULEDDOWNTIME:
                case CIMFW_E10_PRODUCTIVE:
                    eqpPortAttributes.setE10Status(port.getETenState());
                    break;
                default:
                        eqpPortAttributes.setE10Status(null);
            }*/

            eqpPortAttributes.setPortUsage(port.getPortUsage());
            eqpPortAttributes.setPortUsageType(port.getUsageType());

            com.fa.cim.newcore.bo.machine.CimPortResource associatedPort = port.getAssociatedPort();
            if (!CimObjectUtils.isEmpty(associatedPort)) {
                ObjectIdentifier associatedPortObject = new ObjectIdentifier(port.getAssociatedPort().getIdentifier());
                eqpPortAttributes.setAssociatedPortID(associatedPortObject);
            }

            eqpPortAttributes.setPortState(port.getPortState());
            eqpPortAttributes.setLastClaimedTimeStamp(CimDateUtils.getTimestampAsString(port.getLastClaimedTimeStamp()));
            eqpPortAttributes.setLastClaimedUserID(new ObjectIdentifier(port.getLastClaimedPersonID()));
            eqpPortAttributes.setLastMaintTimeStamp(CimDateUtils.getTimestampAsString(port.getLastMaintenanceTimeStamp()));
            eqpPortAttributes.setLastMaintUserID(new ObjectIdentifier(port.getLastMaintenancePersonID()));
            eqpPortAttributes.setStatusChangeTimeStamp(CimDateUtils.getTimestampAsString(port.getLastStatusChangeTimeStamp()));
            eqpPortAttributes.setStatusChangeUserID(new ObjectIdentifier(port.getLastStatusChangePersonID()));
            eqpPortAttributes.setPortClaimMode(port.getClaimMode());
            eqpPortAttributes.setPortTransferMode(port.getTransferMode());
            eqpPortAttributes.setPortLotSelMode(port.getLotSelectionMode());
            eqpPortAttributes.setPortQueueMode(port.getQueuingMode());
            eqpPortAttributes.setDispatchState(port.getDispatchState());
            eqpPortAttributes.setPortDispatchTimeStamp(CimDateUtils.getTimestampAsString(port.getLastDispatchRequiredTimeStamp()));

            CimLot dispatchLot = port.getDispatchLoadLot();
            if (!CimObjectUtils.isEmpty(dispatchLot)) {
                eqpPortAttributes.setLoadDispatchLotID(new ObjectIdentifier(dispatchLot.getIdentifier()));
            }

            com.fa.cim.newcore.bo.durable.CimCassette loadDispatchCassette = port.getDispatchLoadCassette();
            if (!CimObjectUtils.isEmpty(loadDispatchCassette)) {
                eqpPortAttributes.setLoadDispatchCassetteID(new ObjectIdentifier(loadDispatchCassette.getIdentifier()));
            }

            CimLot undispatchLot = port.getDispatchUnloadLot();
            if (!CimObjectUtils.isEmpty(undispatchLot)) {
                eqpPortAttributes.setUnloadDispatchLotID(new ObjectIdentifier(undispatchLot.getIdentifier()));
            }

            com.fa.cim.newcore.bo.durable.CimCassette unloadDispatchCassette = port.getDispatchUnloadCassette();
            if (!CimObjectUtils.isEmpty(unloadDispatchCassette)) {
                eqpPortAttributes.setUnloadDispatchCassetteID(new ObjectIdentifier(unloadDispatchCassette.getIdentifier()));
            }

            com.fa.cim.newcore.bo.code.CimMachineOperationMode currentOperationMode = port.getMachineOperationMode();
            if (!CimObjectUtils.isEmpty(currentOperationMode)) {
                eqpPortAttributes.setCurrentOperationModeID(new ObjectIdentifier(currentOperationMode.getIdentifier()));
            }

            Outputs.ObjPortResourceCurrentOperationModeGetOut currentOperationModeOut = portResourceCurrentOperationModeGet(objCommon, equipmentID, ObjectIdentifier.build(port.getIdentifier(), port.getPrimaryKey()));

            Infos.OperationMode operationMode = currentOperationModeOut.getOperationMode();
            eqpPortAttributes.setOperationModeDescription(operationMode.getDescription());
            eqpPortAttributes.setOnlineMode(operationMode.getOnlineMode());
            eqpPortAttributes.setDispatchMode(operationMode.getDispatchMode());
            eqpPortAttributes.setCurrentAccessMode(operationMode.getAccessMode());
            eqpPortAttributes.setOperationStartMode(operationMode.getMoveInMode());
            eqpPortAttributes.setOperationCompMode(operationMode.getMoveOutMode());

            List<MaterialLocation> materialLocations = port.allMaterialLocations();
            if (!CimArrayUtils.isEmpty(materialLocations)) {
                Material aContainedCast = materialLocations.get(0).getMaterial();
                if (!CimObjectUtils.isEmpty(aContainedCast)) {
                    eqpPortAttributes.setLoadedCassetteID(new ObjectIdentifier(aContainedCast.getIdentifier()));
                }
            }

            com.fa.cim.newcore.bo.durable.CimCassette m3CompleteCassette = port.getLastM3CompCassette();
            if (!CimObjectUtils.isEmpty(m3CompleteCassette)) {
                eqpPortAttributes.setLastM3CompCassetteID(new ObjectIdentifier(m3CompleteCassette.getIdentifier()));
            }

            List<String> portAccessModes = port.getAccessModes();
            List<String> accessModes = new ArrayList<>(portAccessModes);
            eqpPortAttributes.setAccessModes(accessModes);

            List<String> portCapableOperationModes = port.getCapableOperationModes();
            List<String> capableOperationModes = new ArrayList<>(portCapableOperationModes);
            eqpPortAttributes.setCapableOperationModes(capableOperationModes);

            List<String> portCassetteCapabilities = port.getCassetteCategoryCapability();
            List<String> cassetteCapabilities = new ArrayList<>(portCassetteCapabilities);
            eqpPortAttributes.setCassetteCategoryCapability(capableOperationModes);

            List<String> portSpecialControls = port.getSpecialPortControls();
            List<String> specialPortControls = new ArrayList<>(portSpecialControls);
            eqpPortAttributes.setSpecialPortControls(specialPortControls);

            eqpPortAttributesList.add(eqpPortAttributes);
        }
        return portGroupInfoGetOut;
    }

    @Override
    public Outputs.ObjEquipmentPortCombinationCheck equipmentPortCombinationCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<ObjectIdentifier> portIDs) {
        Outputs.ObjEquipmentPortCombinationCheck combinationCheck = new Outputs.ObjEquipmentPortCombinationCheck();
        CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, equipmentID);

        //Check input portID length
        Validations.check(CimArrayUtils.isEmpty(portIDs), retCodeConfig.getNoPortInput());

        // Get object reference of port
        // and retrieve current mode information from
        // object reference must be retrieved

        List<String> portGroups = new ArrayList<>();
        List<PortResource> ports = equipment.allPortResources();
        List<Infos.EqpPortStatus> eqpPortStatuses = new ArrayList<>();

        for (PortResource portResource : ports) {
            Infos.EqpPortStatus eqpPortStatus = new Infos.EqpPortStatus();
            CimPortResource port = (CimPortResource) portResource;
            CimPortResource associatedPort = port.getAssociatedPort();

            eqpPortStatus.setPortID(new ObjectIdentifier(port.getIdentifier(), port.getPrimaryKey()));
            eqpPortStatus.setPortGroup(port.getPortGroup());
            eqpPortStatus.setAssociatedPortID(new ObjectIdentifier(associatedPort.getIdentifier(), associatedPort.getPrimaryKey()));
            eqpPortStatuses.add(eqpPortStatus);

            boolean portGroupAdded = false;
            for (int j=0; j<portGroups.size(); j++) {
                if (eqpPortStatus.getPortGroup().equals(portGroups.get(j))) {
                    portGroupAdded = true;
                    break;
                }
            }

            if (!portGroupAdded) {
                portGroups.add(eqpPortStatus.getPortGroup());
            }
        }

        for (int i=0; i<portIDs.size(); i++) {
            boolean associatedPortExist = false;

            ObjectIdentifier portID = portIDs.get(i);

            for (int j=0; j<eqpPortStatuses.size(); j++) {
                Infos.EqpPortStatus eqpPortStatus = eqpPortStatuses.get(j);

                if (portID.getValue().equals(eqpPortStatus.getPortID().getValue())) {
                    for (int k=0; k<portIDs.size(); k++) {
                        if (portIDs.get(k).getValue().equals(eqpPortStatus.getAssociatedPortID().getValue())) {
                            associatedPortExist = true;
                            break;
                        }
                    }
                }

                if (associatedPortExist) {
                    break;
                }
            }

            Validations.check(!associatedPortExist, retCodeConfig.getInvalidPortCombination());
        }

        // All ports in 1 port group must be specified in input portID
        List<String> inputPortGroups = new ArrayList<>();
        for (int i=0; i<portIDs.size(); i++) {
            int inputPortFoundPosition = 0;
            boolean inputPortFoundFlag = false;

            ObjectIdentifier portID = portIDs.get(i);

            for (int j=0; j<eqpPortStatuses.size(); j++) {
                if (portID.getValue().equals(eqpPortStatuses.get(j).getPortID().getValue())) {
                    inputPortFoundPosition = j;
                    inputPortFoundFlag = true;
                    break;
                }
            }

            if (inputPortFoundFlag) {
                boolean portGroupAdded = false;

                for (int k=0; k<inputPortGroups.size(); k++) {
                    if (inputPortGroups.get(k).equals(eqpPortStatuses.get(inputPortFoundPosition).getPortGroup())) {
                        portGroupAdded = true;
                        break;
                    }
                }

                if (!portGroupAdded) {
                    inputPortGroups.add(eqpPortStatuses.get(inputPortFoundPosition).getPortGroup());
                }

                for (int j=inputPortFoundPosition; j<eqpPortStatuses.size()-1; j++) {
                    eqpPortStatuses.set(j, eqpPortStatuses.get(j+1));
                }
                eqpPortStatuses.remove(eqpPortStatuses.size()-1);
            } else {
                Validations.check(true,retCodeConfig.getInvalidPortCombination());
            }
        }

        // Check all port in port group is involved in Input structure or not
        for (int i=0; i<inputPortGroups.size(); i++) {
            for (int j=0; j<eqpPortStatuses.size(); j++) {
                Validations.check(inputPortGroups.get(i).equals(eqpPortStatuses.get(j).getPortGroup()), retCodeConfig.getInvalidPortCombination());
            }
        }

        combinationCheck.setEquipmentPortGroup(portGroups);
        combinationCheck.setInputPortGroup(inputPortGroups);
        return combinationCheck;
    }

    @Override
    public Infos.CandidatePortMode portResourceCandidateOperationModeGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String modeChangeType, ObjectIdentifier portID) {
        Infos.CandidatePortMode candidatePortMode = new Infos.CandidatePortMode();
        List<Infos.OperationMode> operationModes = new ArrayList<>();
        candidatePortMode.setStrOperationMode(operationModes);

        CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(CimObjectUtils.isEmpty(equipment), retCodeConfig.getNotFoundEqp());
        PortResource portResource = equipment.findPortResourceNamed(ObjectIdentifier.fetchValue(portID));
        Validations.check(CimObjectUtils.isEmpty(portResource), retCodeConfig.getNotFoundPort());
        com.fa.cim.newcore.bo.machine.CimPortResource port = (com.fa.cim.newcore.bo.machine.CimPortResource) portResource;

        candidatePortMode.setPortID(portID);
        candidatePortMode.setPortGroup(port.getPortGroup());
        candidatePortMode.setPortUsage(port.getPortUsage());

        // Get object reference of Port Operatoin Mode object reference must be retrieved
        CimMachineOperationMode currentOperationMode = port.getMachineOperationMode();
        Validations.check(CimObjectUtils.isEmpty(currentOperationMode), retCodeConfig.getNotFoundMachineOperationMode());

        //Get detail info of PosMachineOperation
        CodeDTO.OperationModeInfo aCurrentOpeModeInfo = currentOperationMode.getOperationModeInfo();

        //Get All allowed operation mode for port
        List<CimMachineOperationMode> allAllowedOpeModes = port.getAllAllowedOperationMode();

        // Get detail info of PosMachineOperation from all allowed operation mode
        for (int i = 0; i < allAllowedOpeModes.size(); i++) {
            CodeDTO.OperationModeInfo operationModeInfo = allAllowedOpeModes.get(i).getOperationModeInfo();
            // If modeChangeType is 'OnlineModeChange', operationModes whose onlineMode is different from current operationModes is added into return structure
            if (BizConstant.SP_EQP_OPERATION_ONLINEMODE_CHANGE.equals(modeChangeType)) {
                if (!currentOperationMode.getOnlineMode().equals(operationModeInfo.getOnlineMode())) {
                    addOperationModes(operationModes, operationModeInfo, allAllowedOpeModes.get(i));
                }
            } else if (BizConstant.SP_EQP_OPERATION_ACCESSMODE_CHANGE.equals(modeChangeType)) {
                // If modeChangeType is 'AccessModeChange',
                // operationModes whose onlineMode is th same as current operationModes and
                // accessMode is different from current accessMode is added into return structure
                if (aCurrentOpeModeInfo.getOnlineMode().equals(operationModeInfo.getOnlineMode())
                        && !aCurrentOpeModeInfo.getAccessMode().equals(operationModeInfo.getAccessMode())) {
                    addOperationModes(operationModes, operationModeInfo, allAllowedOpeModes.get(i));
                }
            } else if (BizConstant.SP_EQP_OPERATION_OTHERMODE_CHANGE.equals(modeChangeType)) {
                if (aCurrentOpeModeInfo.getOnlineMode().equals(operationModeInfo.getOnlineMode())
                        && aCurrentOpeModeInfo.getAccessMode().equals(operationModeInfo.getAccessMode())) {
                    if (!currentOperationMode.getOperationModeID().equals(allAllowedOpeModes.get(i).getIdentifier())) {
                        addOperationModes(operationModes, operationModeInfo, allAllowedOpeModes.get(i));
                    }
                }
            } else {
                Validations.check(retCodeConfig.getInvalidModeChangeType());
            }
        }
        return candidatePortMode;
    }

    /**
     * description: addOperationModes for portResourceCandidateOperationModeGet
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/10/30 10:22
     * @param operationModes
     * @param operationModeInfo
     * @param machineOperationMode -
     * @return void
     */
    private void addOperationModes(List<Infos.OperationMode> operationModes, CodeDTO.OperationModeInfo operationModeInfo, com.fa.cim.newcore.bo.code.CimMachineOperationMode machineOperationMode){
        Infos.OperationMode operationMode = new Infos.OperationMode();
        operationMode.setOperationMode(new ObjectIdentifier(machineOperationMode.getIdentifier(), machineOperationMode.getPrimaryKey()));
        operationMode.setOnlineMode(operationModeInfo.getOnlineMode());
        operationMode.setDispatchMode(operationModeInfo.getDispatchMode());
        operationMode.setAccessMode(operationModeInfo.getAccessMode());
        operationMode.setMoveInMode(operationModeInfo.getOperationStartMode());
        operationMode.setMoveOutMode(operationModeInfo.getOperationCompMode());
        operationMode.setDescription(machineOperationMode.getDescription());
        operationModes.add(operationMode);
    }

    @Override
    public Infos.EqpPortInfo portResourceAllPortsInSameGroupGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID) {
        Infos.EqpPortInfo eqpPortInfo = new Infos.EqpPortInfo();
        List<Infos.EqpPortStatus> eqpPortStatuses = new ArrayList<>();
        /*-------------------------------------------*/
        /*   Check the existence of in-paramater     */
        /*-------------------------------------------*/
        Validations.check( ObjectIdentifier.isEmpty(portID) || ObjectIdentifier.isEmpty(equipmentID),retCodeConfig.getInvalidParameter());

        /*----------------------------------*/
        /*   Get All Port Information       */
        /*----------------------------------*/
        Infos.EqpPortInfo equipmentPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID );
        String tmpPortGroup = null;
        int lenPortResSeq = CimArrayUtils.getSize(equipmentPortInfo.getEqpPortStatuses());
        /*----------------------------------------------------------------------------------------------*/
        /* Specified portID is compared with aquired portID. Search!! PortGroup of specified portID.    */
        /*----------------------------------------------------------------------------------------------*/
        for (int i=0 ; i < lenPortResSeq ; i++ ) {
            if(ObjectIdentifier.equalsWithValue(equipmentPortInfo.getEqpPortStatuses().get(i).getPortID(), portID)) {
                tmpPortGroup = equipmentPortInfo.getEqpPortStatuses().get(i).getPortGroup();
                break;
            }
        }

        Validations.check(CimStringUtils.isEmpty(tmpPortGroup),retCodeConfig.getNotFoundPort());

        /*-----------------------------------------------------------*/
        /* Set Port Info of the same PortGroup as specified port.    */
        /*-----------------------------------------------------------*/
        for (int i=0 ; i < lenPortResSeq ; i++ ) {
            if(CimStringUtils.equals(equipmentPortInfo.getEqpPortStatuses().get(i).getPortGroup(), tmpPortGroup) ) {
                Infos.EqpPortStatus  eqpPortStatus =equipmentPortInfo.getEqpPortStatuses().get(i);
                eqpPortStatuses.add(eqpPortStatus);
            }
        }
        // Resize of OutPut Paramater's length.
        eqpPortInfo.setEqpPortStatuses(eqpPortStatuses);
        return eqpPortInfo;
    }

}
