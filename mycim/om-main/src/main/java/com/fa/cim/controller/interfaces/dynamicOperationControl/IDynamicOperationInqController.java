package com.fa.cim.controller.interfaces.dynamicOperationControl;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 15:36
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDynamicOperationInqController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/18       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2019/3/18 10:37
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response DOCLotInfoInq(Params.DOCLotInfoInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/18       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2019/3/18 10:37
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response DOCStepListInProcessFlowInq(Params.DOCStepListInProcessFlowInqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/13                             Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/13 14:15
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response recipeIdListForDOCInq(Params.RecipeIdListForDOCInqParams params);

    /**
     * description:
     * <p>This function returns information of all Operation IDs and nested route informations on the specified Route ID.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/14                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/12/14 10:52
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response processFlowOpeListWithNestInq(Params.ProcessFlowOpeListWithNestInqParam param);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/1 17:05                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/7/1 17:05
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response copyFromInq(Params.CopyFromInqParams params);
}