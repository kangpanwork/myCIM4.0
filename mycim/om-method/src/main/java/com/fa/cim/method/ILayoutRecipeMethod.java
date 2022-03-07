package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.layoutrecipe.LayoutRecipeParams;

import java.util.List;

/**
 * description: layout recipe recipe method
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/3/2 0002        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/3/2 0002 19:23
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ILayoutRecipeMethod {
    /**
     * description: convert the layout recipe
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                           - common
     * @param convertEquipmentFurnaceRecipeParams - convert params
     * @return The result of the convert
     * @author YJ
     * @date 2021/3/3 0003 18:26
     */
    List<Infos.StartCassette> equipmentFurnaceRecipeConvert(Infos.ObjCommon objCommon,
                                                            LayoutRecipeParams.ConvertEquipmentFurnaceRecipeParams
                                                                    convertEquipmentFurnaceRecipeParams);


    /**
     * description:  在eqp 进行 load 的时候， 验证当前carrier中的顺序 是否与furnace recipe specific control 设置的顺序符合。
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/7/22 0022 15:16                        YJ                Create
     *
     * @param objCommon   - common
     * @param cassetteId  - carrier Id 当前load的carrier
     * @param equipmentId - eqp port info
     * @author YJ
     * @date 2021/7/22 0022 15:16
     */
    void controlJobIsCarrierLotFurnaceSpecificControl(Infos.ObjCommon objCommon, ObjectIdentifier cassetteId,
                                                      ObjectIdentifier equipmentId);
}