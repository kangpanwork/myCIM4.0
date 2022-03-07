package com.fa.cim.crcp;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * description: 进行tool capability / tool constraint / chamber state 查询参数
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/9/15          ********              YJ               create file
 *
 * @author: YJ
 * @date: 2021/9/15 11:09
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class ChamberLevelRecipeReserveParam {


    /**
     * user
     */
    private User user;

    /**
     * 设备ID
     */
    private ObjectIdentifier equipmentId;


    /**
     * 加工carrier信息
     */
    private List<Infos.StartCassette> startCassettes;

    /**
     * <LogicRecipe,LogicRecipe></>
     */
    private Map<String,String> multipleChamberStatus;

}
