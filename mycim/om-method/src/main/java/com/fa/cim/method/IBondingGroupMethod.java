package com.fa.cim.method;

import java.util.List;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/13          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/13 15:23
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IBondingGroupMethod {
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/13 15:28
     * @param objCommon
     * @param equipmentID
     * @param controlJobID
     * @param bondingMapInfoFlag -
     * @return com.fa.cim.dto.Outputs.ObjBondingGroupInfoByEqpGetDROut
     */
    Outputs.ObjBondingGroupInfoByEqpGetDROut bondingGroupInfoByEqpGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, boolean bondingMapInfoFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/3/13 18:20
     * @param objCommon -
     * @param bondingGroupID -
     * @param bondingMapInfoFlag -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.bean.extension.Outputs.ObjBondingGroupInfoGetDROut>
     */
    Outputs.ObjBondingGroupInfoGetDROut bondingGroupInfoGetDR(Infos.ObjCommon objCommon, String bondingGroupID, Boolean bondingMapInfoFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/10/9 16:10
     * @param objCommon
     * @param bondingGroupID
     * @param bondingGroupState
     * @param equipmentID
     * @param controlJobID -
     * @return void
     */
    void bondingGroupStateUpdateDR(Infos.ObjCommon objCommon, String bondingGroupID, String bondingGroupState, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID);


    /**
     * Update the Bonding Group and related child tables.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/17 13:52
     */
    void bondingGroupInfoUpdateDR(Infos.ObjCommon objCommon, String action, Infos.BondingGroupInfo strBondingGroupInfo);

    /**
     * This function inquires information of specified bonding group.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/22 13:39
     */
    List<Infos.BondingGroupInfo> bondingGroupListGetDR(Infos.ObjCommon objCommon, Params.BondingGroupListInqInParams bondingGroupListInqInParams);


    /**
     * description: Check the load purpose type when wafer bonding
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - objCommon
     * @param equipmentId - equipmentId
     * @param portId - portId
     * @param cassetteId  - carrier
     * @author YJ
     * @date 2021/1/12 0012 14:35
     */
	void portWaferBondingCheck(Infos.ObjCommon objCommon, ObjectIdentifier portId, ObjectIdentifier equipmentId,
			ObjectIdentifier cassetteId);

    /**
     * description: get port wafer bonding type ,  top or base
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common
     * @param portId      - port id
     * @param equipmentId - eqp id
     * @return top or base
     * @author YJ
     * @date 2021/1/13 0013 16:43
     */
    String portWaferBodingTypeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier portId, ObjectIdentifier equipmentId);
}