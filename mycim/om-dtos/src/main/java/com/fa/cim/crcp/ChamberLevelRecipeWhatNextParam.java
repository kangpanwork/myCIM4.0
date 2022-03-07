package com.fa.cim.crcp;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * description: what next查询lot，
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/9/16          ********              YJ               create file
 *
 * @author: YJ
 * @date: 2021/9/16 14:36
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChamberLevelRecipeWhatNextParam {

    /**
     * 设备ID
     */
    private ObjectIdentifier equipmentId;

    /**
     * what next 查询出来的lot
     */
    private List<Infos.WhatNextAttributes> whatNextAttributesList;
}
