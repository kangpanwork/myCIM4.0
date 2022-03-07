package com.fa.cim.service.crcp;

import com.fa.cim.crcp.ChamberLevelRecipeReserveParam;
import com.fa.cim.dto.Infos;

import java.util.List;

/**
 * description: chamber level recipe inq service
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/9/26          ********              YJ               create file
 *
 * @author: YJ
 * @date: 2021/9/26 15:14
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IChamberLevelRecipeInqService {

    /**
     * description:  chamber level recipe 预览
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/26 15:33                        YJ                Create
     *
     * @param chamberLevelRecipeReserveParam - chamber level recipe reserve param
     * @return start cassette
     * @author YJ
     * @date 2021/9/26 15:33
     */
    List<Infos.StartCassette> sxChamberLevelRecipeDesignatedInq(
            Infos.ObjCommon objCommon, ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam);
}
