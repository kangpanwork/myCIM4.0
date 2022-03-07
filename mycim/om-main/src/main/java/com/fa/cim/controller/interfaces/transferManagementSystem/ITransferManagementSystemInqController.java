package com.fa.cim.controller.interfaces.transferManagementSystem;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 15:14
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ITransferManagementSystemInqController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/25       ********              lightyh             create file
     *
     * @author: lightyh
     * @date: 2019/6/25 16:51
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response whereNextStockerInq(Params.WhereNextStockerInqParams whereNextStockerInqParams);

    /**
     * description: This function inquires TMServer about Transport Job information of Carrier and returns it.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/18                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/18 11:17
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierTransferJobDetailInfoInq(Params.CarrierTransferJobDetailInfoInqParam params);

    /**
     * description:This function inquires TMServer about Transport Job Status information of Carrier and returns it.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/17                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/17 14:46
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierTransferJobInfoInq(Params.CarrierTransferJobInfoInqParam params);

    /**
     * description:TxStockerInfoInq
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-18                           ho             create file
     * <p>
     * return:
     *
     * @author ho
     * @date: 2018-09-18 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response stockerInfoInq(Params.StockerInfoInqInParams stockerInfoInqInParams);

    /**
     * description:TxStockerListInq
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-18                           ho             create file
     * <p>
     * return:
     *
     * @author ho
     * @date: 2018-09-18 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response stockerListInq(Params.StockerListInqInParams stockerListInqInParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param params
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/3/17 13:26
     */
    Response cxWhereNextOHBCarrierInq(@RequestBody Params.WhereNextOHBCarrierInqInParm params);

    /**
     * description:TxAllEqpForAutoTransferInq
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-18                           ho             create file
     * <p>
     * return:
     *
     * @author ho
     * @date: 2018-09-18 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response allEqpForAutoTransferInq(Params.AllAvailableEqpParams allAvailableEqpParams);

    /**
     * description:TxEqpForAutoTransferInq
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-18                           ho             create file
     * <p>
     * return:
     *
     * @author ho
     * @date: 2018-09-18 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpForAutoTransferInq(Params.EqpForAutoTransferInqParams eqpForAutoTransferInqParams);

    /**
     * description:TxStockerForAutoTransferInq
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-18                           ho             create file
     * <p>
     * return:
     *
     * @author ho
     * @date: 2018-09-18 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response stockerForAutoTransferInq(Params.StockerForAutoTransferInqParams stockerForAutoTransferInqParams);

    /**
     * description:DurableWhereNextStockerInq
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/14                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/14 14:41
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response durableWhereNextStockerInq(@RequestBody Params.DurableWhereNextStockerInqParams params);
}