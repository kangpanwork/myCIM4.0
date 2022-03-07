package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.pr.Info;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/3/3        ********            Aoki                create file
 *
 * @author: Aoki
 * @date: 2021/3/3 10:23
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IRecipeGroupEventMethod {

    /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/4 16:57                     Aoki                Create
    *
    * @author Aoki
    * @date 2021/3/4 16:57
    * @param objCommon
    * @param txId
    * @param testMemo
    * @param strExperimentalGroupInfo
    * @return void
    */
    void experimentalRecipeGroupRegistEventMake(Infos.ObjCommon objCommon, String txId, String testMemo, Info.ExperimentalGroupInfo strExperimentalGroupInfo);
}
