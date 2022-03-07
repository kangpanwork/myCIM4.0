package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/22          ********            light                create file
 *
 * @author: light
 * @date: 2019/11/22 10:21
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IInterFabMethod {

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/27                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/27 10:09
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjInterFabXferPlanListGetDROut interFabXferPlanListGetDR(Infos.ObjCommon objCommon, Inputs.ObjInterFabXferPlanListGetDRIn objInterFabXferPlanListGetDRIn);

}