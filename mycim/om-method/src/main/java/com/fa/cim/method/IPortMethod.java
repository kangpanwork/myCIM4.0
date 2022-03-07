package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.common.support.ObjectIdentifier;

import java.util.List;

/**
 * description:
 * IPortComp .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/11        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/7/11 16:50
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPortMethod {

    /**
     * description:
     * This method returns current pmcmg operation mode of port resource.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn objCommonIn
     * @param equipmentID equipmentID
     * @param portID      portID
     * @return OperationMode
     * @author PlayBoy
     * @since 2018/7/12
     */
    Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationModeGet(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier portID);

    /**
     * description:
     * This method returns current pmcmg operation mode of port resource.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param objEquipmentPortGroupIDGetIn objEquipmentPortGroupIDGetIn
     * @return ObjEquipmentPortGroupIDGetOut
     * @author Paladin
     * @date 2018/8/17
     */
    Outputs.ObjEquipmentPortGroupIDGetOut equipmentPortGroupIDGet(Infos.ObjCommon objCommon, Inputs.ObjEquipmentPortGroupIDGetIn objEquipmentPortGroupIDGetIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param equipmentID equipmentID
     * @param portID portID
     * @param portStatus portStatus
     * @param cassetteID cassetteID
     * @author Paladin
     * @since 2018/10/14
     */
    void equipmentPortStateChangeForTakeOutInMode(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, String portStatus, ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param equipmentID equipmentID
     * @param portID portID
     * @param portStatus portStatus
     * @param cassetteID cassetteID
     * @author Paladin
     * @since 2018/10/14
     */
    void equipmentPortStateChange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, String portStatus, ObjectIdentifier cassetteID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param equipmentID equipmentID
     * @param portGroupID portGroupID
     * @param portID portID
     * @return Outputs.ObjEquipmentPortGroupInfoGetOut
     * @author Paladin
     * @since 2018/10/14
     */
    Outputs.ObjEquipmentPortGroupInfoGetOut equipmentPortGroupInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, ObjectIdentifier portID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param equipmentID equipmentID
     * @param portID portID
     * @return Outputs.ObjEquipmentPortCombinationCheck
     * @author Paladin
     * @date 2018/11/5
     */
    Outputs.ObjEquipmentPortCombinationCheck equipmentPortCombinationCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<ObjectIdentifier> portID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param equipmentID equipmentID
     * @param modeChangeType modeChangeType
     * @param portID portID
     * @return Infos.CandidatePortMode
     * @author Paladin
     * @date 2018/11/5
     */
    Infos.CandidatePortMode portResourceCandidateOperationModeGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String modeChangeType, ObjectIdentifier portID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/8 10:34
     * @param objCommon
     * @param equipmentID
     * @param portID
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.EqpPortInfo>
     */
    Infos.EqpPortInfo portResourceAllPortsInSameGroupGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID);
}
