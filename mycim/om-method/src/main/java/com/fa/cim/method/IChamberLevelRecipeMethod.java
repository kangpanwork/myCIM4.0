package com.fa.cim.method;

import com.fa.cim.crcp.ChamberLevelRecipeReserveParam;
import com.fa.cim.crcp.ChamberLevelRecipeWhatNextParam;
import com.fa.cim.dto.Infos;

import java.util.List;

/**
 * description: chamber level recipe method
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/9/15          ********              YJ               create file
 *
 * @author: YJ
 * @date: 2021/9/15 10:37
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IChamberLevelRecipeMethod {


    /**
     * description:  获取当前加工使用的chamber level recipe
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/15 10:37                        YJ                Create
     *
     * @param objCommon                      - common param
     * @param chamberLevelRecipeReserveParam - 进行验证的参数
     * @return chamber level recipe
     * @author YJ
     * @date 2021/9/15 10:37
     */
    List<Infos.StartCassette> chamberLevelRecipeMoveQueryRpt(Infos.ObjCommon objCommon,
                                                             ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam);

    /**
     * description:  what next 判断chamber是否全部被禁用
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/16 14:37                        YJ                Create
     *
     * @param objCommon                       - common
     * @param chamberLevelRecipeWhatNextParam - param check disabled chamber list
     * @return WhatNextAttributes
     * @author YJ
     * @date 2021/9/16 14:37
     */
    List<Infos.WhatNextAttributes> whatNextAttributesListChamberCheckRpt(
            Infos.ObjCommon objCommon, ChamberLevelRecipeWhatNextParam chamberLevelRecipeWhatNextParam);
}
