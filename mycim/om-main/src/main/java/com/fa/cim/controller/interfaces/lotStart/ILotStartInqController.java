package com.fa.cim.controller.interfaces.lotStart;

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
 * @date: 2019/7/30 13:54
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ILotStartInqController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Panda
     * @date: 2018/5/15
     */
    Response productOrderInq(Params.ProductOrderInqParams requestInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/17       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/17 11:17
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response productIdListInq(Params.ProductIdListInqInParams params);

    /**
     * description:This function relates ProductOrderReleasedListInq.
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Panda
     * @date: 2018/5/3
     */
    Response productOrderReleasedListInq(Params.ProductOrderReleasedListInqParams productOrderReleasedListInqParams);

    /**
     * description:This function relates ProductOrderReleasedListInq.
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author ho
     * @date: 2018/5/3
     */
    Response productRelatedSourceProductInq(@RequestBody Params.ProductRelatedSourceProductInqParams productOrderReleasedListInqParams);

    /**
     * description:This function relates SourceLotListInqController.
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Panda
     * @date: 2018/5/4
     */
    Response sourceLotListInq(Params.SourceLotListInqParams sourceLotListInqParams);

    /**
     * description:This function relates WaferLotStartCancelInfoInqController.
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Panda
     * @date: 2018/5/16
     */
    Response WaferLotStartCancelInfoInq(Params.WaferLotStartCancelInfoInqParams waferLotStartCancelInfoInqParams);

    /**
     * description:
     * <p>This function returns productgroup ID List.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/4                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/12/4 21:49
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response allProductGroupListInq(Params.AllProductGroupListInq param);
}