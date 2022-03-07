package com.fa.cim.controller.interfaces.bank;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 16:11
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IBankInqController {
    /**
     * description:
     * The method use to define the BankListInq controller.
     * transaction ID: OBNKQ001
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/3/20        ********            Bear         create file
     *
     * @author: Bear
     * @date: 2018/3/20 10:18
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response bankListInq(Params.BankListInqParams bankListInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/30 15:07
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response materialPrepareCancelInfoInq(Params.MaterialPrepareCancelInfoInqParams materialPrepareCancelInfoInqParams);
}