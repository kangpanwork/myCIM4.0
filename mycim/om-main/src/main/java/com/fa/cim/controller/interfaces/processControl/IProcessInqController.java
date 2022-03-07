package com.fa.cim.controller.interfaces.processControl;

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
 * @date: 2019/7/30 15:52
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProcessInqController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/24       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/24 10:14
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response futureHoldListInq(Params.FutureHoldListInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/8       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/11/8 17:13
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response npwUsageStateSelectionInq(Params.UserParams userParams);

    /**
     * description:TxQtimeDefinitionSelectionInq__180
     * <p></p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/29                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/10/29 18:09
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response qtimeDefinitionSelectionInq(Params.QtimeDefinitionSelectionInqParam qtimeDefinitionSelectionInqParam);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 11/13/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 11/13/2018 3:22 PM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response qtimeListInq(Params.QtimeListInqParams qtimeListInqParams);

    /**
     * description: TxLotFuturePctrlListInq
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/12/4 13:58:06
     */
    Response lotFuturePctrlListInq(Params.LotFuturePctrlListInqParams lotFuturePctrlListInqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @throws
     * @author Ho
     * @date 2019/4/22 18:44
     */
    Response qtimeExpiredLotListWithActionPendingInq(Params.QtimeExpiredLotListWithActionPendingInqInParm strQtimeExpiredLotListWithActionPendingInqInParm);

    /**
     * description:
     * <p>This function returns List of ProcessHold that fulfill specified conditions.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/2                            Wind                create file
     *
     * @author: Wind
     * @date: 2019/1/2 16:44
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response processHoldListInq(Params.ProcessHoldListInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/24 13:06
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response futureReworkListInq(Params.FutureReworkListInqParams params);
}