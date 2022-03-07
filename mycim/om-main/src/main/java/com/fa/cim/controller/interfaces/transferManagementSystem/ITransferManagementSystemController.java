package com.fa.cim.controller.interfaces.transferManagementSystem;

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
 * @date: 2019/7/30 14:54
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ITransferManagementSystemController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-11-12                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-11-12 17:38
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierReserveCancelReq(Params.LotCassetteReserveCancelParams params);

    /**
     * description:
     * <p>CarrierReserveReqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/12        ********             Yuri               create file
     *
     * @author: Yuri
     * @date: 2018/11/12 15:52
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierReserveReq(Params.CarrierReserveReqParam params);

    /**
     * description:
     * <p>CarrierTransferJobEndRptController .<br/></p>
     * * change history:
     * date             defect#             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * 2019/1/31        ********             apple               create file
     *
     * @author: apple
     * @date: 2019/1/31 16:32
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierTransferJobEndRpt(Params.CarrierTransferJobEndRptParams carrierTransferJobEndRptParams);

    /**
     * description: TxCarrierTransferStatusChangeRpt
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/31 15:20:41
     */
    Response carrierTransferStatusChangeRpt(Params.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams);

    /**
     * description: This function deletes TransferJob of a specified Carrier.
     * This function requests TMS to delete TransferJob of a specified Carrier and deletes the TransferJob.
     * With the deletion of TransferJob, if a specified Cassette has been reserved to transfer, the reservation is cancelled and if it has been dispatched, DispatchState is changed to Required.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 9:54
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierTransferJobDeleteReq(Params.CarrierTransferJobDeleteReqParam params);

    /**
     * description:
     * <p>CarrierTransferReqForInternalBufferController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/9        ********             Yuri               create file
     *
     * @author: Yuri
     * @date: 2019/5/9 14:41
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierTransferForIBReq(Params.CastDeliveryReqParam castDeliveryReqParam);

    /**
     * description:
     * <p>CarrierTransferReqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/9        ********             Yuri               create file
     *
     * @author: Yuri
     * @date: 2019/5/9 14:32
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierTransferReq(Params.CastDeliveryReqParam castDeliveryReqParam);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/5/25                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/5/25 15:30
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response fmcCarrierTransferReq(Params.CastDeliveryReqParam castDeliveryReqParam);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/21 10:45
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response singleCarrierTransferReq(Params.SingleCarrierTransferReqParam params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/21 10:45
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response multipleCarrierTransferReq(Params.MultipleCarrierTransferReqParam params);

    /**
     * @author apple
     * @date: 2019-2-01 10:44
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response durableTransferJobStatusRpt(Params.DurableTransferJobStatusRptParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-10-16                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-10-16 17:59
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response stockerInventoryUploadReq(Params.StockerInventoryUploadReqParam params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-10-16                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-10-16 17:59
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response stockerInventoryRpt(Params.StockerInventoryRptParam params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-10-26                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-10-26 15:35
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response stockerStatusChangeRpt(Params.StockerStatusChangeRptParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/9          ********            Nyx                create file
     *
     * @author: Nyx
     * @date: 2019/7/9 16:28
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response npwCarrierReserveForIBReq(Params.NPWCarrierReserveForIBReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/9          ********            Nyx                create file
     *
     * @author: Nyx
     * @date: 2019/7/9 16:28
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response npwCarrierReserveReq(Params.NPWCarrierReserveReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/11/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/11/5 15:06
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response npwCarrierReserveCancelForIBReq(Params.NPWCarrierReserveCancelReqParm params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params
     * @return com.fa.cim.common.support.Response
     * @author Jerry
     * @date 2019/8/6 18:16
     */
    Response npwCarrierReserveCancelReq(Params.NPWCarrierReserveCancelReqParm params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/9/16                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/9/16 9:52
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response dmsTransferReq(Params.CastDeliveryReqParam params);
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/9/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/9/22 17:17
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response dmsTransferForIBReq(Params.CastDeliveryReqParam params);
}