package com.fa.cim.controller.interfaces.bank;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/26          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/26 14:18
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IBankController {
    /**
     * description:This function moves the specified lot from a current bank to the selected bank logically.
     * This function is used in the following cases.
     * 1) Moving a lot to the start bank of the next manufacturing layer to prepare for STB.
     * The moved lot could be used as source lot for a new lot to STB.
     * 2) Moving a lot to Non Production bank such as "Engineer Use bank".
     * The bank of specified Lots will be changed to the selected bank.
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/4        D6000025               Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/4/4 14:25
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response bankMoveReq(Params.BankMoveReqParams bankMoveReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/19        *******              jerry              create file
     *
     * @author: jerry
     * @date: 2018/4/19 15:55
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response holdLotInBankReq(Params.HoldLotInBankReqParams holdLotInBankReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/25        *******              jerry              create file
     *
     * @author: jerry
     * @date: 2018/4/25 16:31
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response holdLotReleaseInBankReq(Params.HoldLotReleaseInBankReqParams holdLotReleaseInBankReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/22        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/5/22 17:22
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response materialPrepareCancelReq(Params.MaterialPrepareCancelReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/22        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/5/22 17:22
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotPreparationCancelExReq(Params.LotPreparationCancelExReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/16         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/16 10:26
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response nonProdBankStoreReq(Params.NonProdBankStoreReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/18         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/18 10:29
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response nonProdBankReleaseReq(Params.NonProdBankReleaseReqParams params);

    /**
     * description:This function cancels a request to ship lot.
     * This time, the lot Status (Shipped) is returned to the state that is was in before shipping (Completed) and the lot is stored in the specified bank.
     * However, you cannot cancel a request to ship the lot that has been over the reserve period.
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/11        OBNKW012             Nyx              create file
     *
     * @author: Nyx
     * @date: 2018/4/11 11:28
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response unshipReq(Params.BankMoveReqParams unshipReqParams);

    /**
     * description:This function requests to ship the specified lot.
     * By this function, the condition of lot which finished all process is changed to "shipped".
     * At this time, the lot Status (Completed) is changed to the state that has been already shipped (Shipped).
     * The shipped lot, after it passes the reserve period, you can delete it by other function (lotmaint).
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/4        D6000025               Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/4/4 14:25
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response shipReq(Params.BankMoveReqParams shipReqParams);

    /**
     * description:
     * This function relates VenderLot to Carrier.
     * The VenderLot related to the specified Carrier is used as the Infos.SourceLot for STB (Start To Build) and the Dummy lot.
     * Also, the lot related to the Carrier remains in a bank.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/4        ********            Bear         create file
     *
     * @author: Bear
     * @date: 2018/4/4 14:53
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response materialPrepareReq(Params.MaterialPrepareReqParams materialPrepareReqParams);

    /**
     * description:
     * The method use to define the MaterialReceiveAndPrepareReqController.
     * transaction ID: OBNKW004
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/18        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/18 09:58
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response materialReceiveAndPrepareReq(Params.MaterialReceiveAndPrepareReqParams materialReceiveAndPrepareReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/11         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/11 10:38
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response bankInCancelReq(Params.BankInCancelReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/10         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/10 13:57
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response bankInReq(Params.BankInReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/8        ********            Bear         create file
     *
     * @author: Bear
     * @date: 2018/4/8 13:13
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response vendorLotReceiveReq(Params.VendorLotReceiveParams vendorLotReceiveParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/4        *******             jerry               create file
     *
     * @author: jerry
     * @date: 2018/4/4 11:11
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response vendorLotReturnReq(Params.VendorLotReturnParams vendorLotReturnParams);
}