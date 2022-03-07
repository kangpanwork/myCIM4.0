package com.fa.cim.service.lotstart;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

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
public interface ILotStartInqService {
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
    Outputs.ObjProductRequestGetDetailOut sxProductOrderInq(Infos.ObjCommon objCommon, Params.ProductOrderInqParams requestInqParams);

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
    List<Infos.ProductIDListAttributes> sxProductIdListInq(Infos.ObjCommon objCommon, Params.ProductIdListInqInParams params) ;

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
    List<ObjectIdentifier> sxProductRelatedSourceProductInq(Infos.ObjCommon objCommon, Params.ProductRelatedSourceProductInqParams productOrderReleasedListInqParams) ;
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
    Results.ProductOrderReleasedListInqResult sxProductOrderReleasedListInq(Infos.ObjCommon objCommon, Params.ProductOrderReleasedListInqParams productOrderReleasedListInqParams) ;


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
    Results.SourceLotListInqResult sxSourceLotListInq(Infos.ObjCommon objCommon, Params.SourceLotListInqParams sourceLotListInqParams) ;

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
    Results.WaferLotStartCancelInfoInqResult sxWaferLotStartCancelInfoInq(Infos.ObjCommon objCommon, Params.WaferLotStartCancelInfoInqParams waferLotStartCancelInfoInqParams) ;


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
    List<Infos.ProductGroupIDListAttributes> sxAllProductGroupListInq(Infos.ObjCommon objCommon, Params.AllProductGroupListInq param);
}