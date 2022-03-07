package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.nonruntime.eqparea.CimEqpAreaDO;

import java.util.List;

/**
 * description: eqp area method
 * This file use to define the IEquipmentAreaMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/2/19 0019        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/2/19 0019 19:42
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEquipmentAreaMethod {


    /**
     * description: eqp search for setting eqp board
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                         - common
     * @param eqpSearchForSettingEqpBoardParams - eqp search params
     * @return result eqp info
     * @author YJ
     * @date 2021/2/19 0019 11:32
     */
    List<Results.EqpSearchForSettingEqpBoardResult> eqpSearchForSettingEqpBoard(Infos.ObjCommon objCommon, Params.EqpSearchForSettingEqpBoardParams eqpSearchForSettingEqpBoardParams);

    /**
     * description: eqp area board list
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon              - objCommon
     * @param eqpAreaBoardListParams - eqpAreaBoardListParams
     * @return rest
     * @author YJ
     * @date 2021/2/19 0019 11:04
     */
    List<Results.EqpAreaBoardListResult> eqpAreaBoardList(Infos.ObjCommon objCommon, Params.EqpAreaBoardListParams eqpAreaBoardListParams);

    /**
     * description: eqp work zone list
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpWorkZoneListParams - eqpWorkZoneListParams
     * @return rest
     * @author YJ
     * @date 2021/2/19 0019 11:04
     */
    List<String> eqpWorkZoneList(Infos.ObjCommon objCommon, Params.EqpWorkZoneListParams eqpWorkZoneListParams);

    /**
     * description: eqp board work zone binding req
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpBoardWorkZoneBindingParams - eqpBoardWorkZoneBindingParams
     * @author YJ
     * @date 2021/2/19 0019 19:23
     */
    void eqpBoardWorkZoneBinding(Infos.ObjCommon objCommon, Params.EqpBoardWorkZoneBindingParams eqpBoardWorkZoneBindingParams);

    /**
     * description: eqp area list by category and zone , lock
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon - objCommon
     * @param category  - category
     * @param zone      - zone
     * @return eqp area
     * @author YJ
     * @date 2021/2/19 0019 20:13
     */
    List<CimEqpAreaDO> eqpAreaLockListByCategoryAndZone(Infos.ObjCommon objCommon, String category, String zone);

    /**
     * description: eqp area cancel req
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon           - objCommon
     * @param eqpAreaCancelParams - eqpAreaCancelParams
     * @author YJ
     * @date 2021/2/20 0020 10:13
     */
    void eqpAreaCancel(Infos.ObjCommon objCommon, Params.EqpAreaCancelParams eqpAreaCancelParams);

    /**
     * description: eqp area move params
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpAreaMoveParams - eqp area move params
     * @author YJ
     * @date 2021/2/20 0020 14:58
     */
    void eqpAreaMove(Infos.ObjCommon objCommon, Params.EqpAreaMoveParams eqpAreaMoveParams);
}