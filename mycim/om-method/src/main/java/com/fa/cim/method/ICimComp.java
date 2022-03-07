package com.fa.cim.method;

import com.fa.cim.dto.Infos;

/**
 * description:
 * This file use to define the ILotMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/6/21 10:29
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

public interface ICimComp {

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/15                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/15 10:38
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Infos.ConfigInfo configurationInformationGetDR(Infos.ObjCommon objCommon, String equipmentId, String value);

}
