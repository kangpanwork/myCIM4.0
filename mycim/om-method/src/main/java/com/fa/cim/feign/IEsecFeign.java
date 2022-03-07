package com.fa.cim.feign;

import com.fa.cim.common.support.Response;
import com.fa.cim.crcp.ChamberLevelRecipeQueryParam;
import com.fa.cim.crcp.DisabledChamberQueryParam;
import com.fa.cim.layoutrecipe.LayoutRecipeParams;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description: ESEC Feign
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/9/15          ********              YJ               create file
 *
 * @author: YJ
 * @date: 2021/9/15 10:33
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@FeignClient(value = "oms-esec-service")
public interface IEsecFeign {

    /**
     * description:  获取Chamber level Recipe
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/15 10:36                        YJ                Create
     *
     * @author YJ
     * @date 2021/9/15 10:36
     * @param chamberLevelRecipeQueryParam  -  chamber level recipe 查询参数
     * @return result<ChamberLevelRecipe>
     */
    @PostMapping("/esec/prc/chamber_level_recipe_query/rpt")
    Response chamberLevelRecipeMoveQueryRpt(@RequestBody ChamberLevelRecipeQueryParam chamberLevelRecipeQueryParam);

    /**
     * description:  查询disabled chamber list
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/16 15:09                        YJ                Create
     *
     * @param disabledChamberQueryParam - 禁用chamber 查询参数
     * @return <MachineRecipeId,List<Chamber>>
     * @author YJ
     * @date 2021/9/16 15:09
     */
    @PostMapping("/esec/prc/disabled_chamber_list/rpt")
    Response disabledChamberListRpt(@RequestBody DisabledChamberQueryParam disabledChamberQueryParam);


    /**
     * description: convert the layout recipe
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param convertEquipmentFurnaceRecipeParam - convert params
     * @return The result of the convert
     * @author YJ
     * @date 2021/3/3 0003 18:26
     */
    @PostMapping("/esec/lyrcp/equipment_furnace_recipe_convert/rpt")
    Response equipmentFurnaceRecipeConvertRpt(@RequestBody LayoutRecipeParams.ConvertEquipmentFurnaceRecipeParams
                                                      convertEquipmentFurnaceRecipeParam);




}
