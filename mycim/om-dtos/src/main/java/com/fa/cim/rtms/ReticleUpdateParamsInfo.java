package com.fa.cim.rtms;

import lombok.Data;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/8/20        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/8/20 14:49
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class ReticleUpdateParamsInfo {

    private List<String> departmentList;

    private List<String> FabList;

    private List<String> DocCategoryList;

    private List<String> userList;

}