package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.*;

import java.util.List;


/**
 * description:
 * This file use to define the IEquipmentMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/6/21 10:30
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProductMethod {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/9/20 12:47
     * @param objCommon -
     * @param productRequestID -
     * @return com.fa.cim.dto.Outputs.ObjProductRequestGetDetailOut
     */
    Outputs.ObjProductRequestGetDetailOut productRequestGetDetail(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/6/7 16:29
     * @param objCommon
     * @param productID -
     * @return com.fa.cim.pojo.Outputs.ObjProductBOMInfoGetOut
     */
    Outputs.ObjProductBOMInfoGetOut productBOMInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier productID);

    Results.ProductOrderReleasedListInqResult productRequestGetListDR(Infos.ObjCommon objCommon, Params.ProductOrderReleasedListInqParams productOrderReleasedListInqParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author ho
     * @date 2018/6/7 16:29
     * @param objCommon
     * @param productID -
     * @return com.fa.cim.pojo.Outputs.ObjProductBOMInfoGetOut
     */
    List<ObjectIdentifier> sourceProductGet(Infos.ObjCommon objCommon, ObjectIdentifier productID);

    List<String> productIDGetBySourceProductID(Infos.ObjCommon objCommon,Params.ProductOrderReleasedListInqParams productOrderReleasedListInqParams);

    /**
     * description:
     * <p>productRequest_CheckForRelease</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/9/19 15:08
     * @param objCommon
     * @param bankID
     * @param sourceLotID
     * @param productCount
     * @param lotType
     * @param subLotType -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjProductRequestForVendorLotReleaseOut>
     */
    Outputs.ObjProductRequestForVendorLotReleaseOut productRequestForVendorLotRelease(Infos.ObjCommon objCommon
            , ObjectIdentifier bankID, ObjectIdentifier sourceLotID, int productCount, String lotType, String subLotType);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/21 21:45
     * @param objCommon
     * @param releaseLotAttributes -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjProductRequestReleaseOut>
     */
    Outputs.ObjProductRequestReleaseOut productRequestRelease(Infos.ObjCommon objCommon, Infos.ReleaseLotAttributes releaseLotAttributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/29 14:49
     * @param objCommon
     * @param in -
     * @return com.fa.cim.dto.Outputs.ObjProductRequestReleaseBySTBCancelOut
     */
    Outputs.ObjProductRequestReleaseBySTBCancelOut productRequestReleaseBySTBCancel(Infos.ObjCommon objCommon, Inputs.ObjProductRequestReleaseBySTBCancelIn in);

    /**
     * description:
     * <p>
     *     method:productRequest_CheckForRelease
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/9/29 16:02
     * @param objCommon -
     * @param releaseLotAttributes -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */

    void productRequestCheckForRelease(Infos.ObjCommon objCommon, Infos.ReleaseLotAttributes releaseLotAttributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/11/28 14:02
     * @param objCommon -
     * @param updateLotAttributes -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     */
    ObjectIdentifier productRequestUpdate(Infos.ObjCommon objCommon, Infos.UpdateLotAttributes updateLotAttributes);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 2018/10/15 14:17
     * @param objCommon -
     * @param lotID -
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     */
    String productRequestProductionStateGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 2018/10/17 15:46
     * @param objCommon -
     * @param lotID -
     * @return com.fa.cim.dto.RetCode
     */
    void productRequestReleaseCancel(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @since 2018/10/17 15:11
     * @param objCommon -
     * @param params -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.ProductIDListAttributes>>
     */
    List<Infos.ProductIDListAttributes> productSpecificationFillInTxPCQ015DR180(Infos.ObjCommon objCommon, Params.ProductIdListInqInParams params);
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2018/11/14 14:28
     * @param objCommon
     * @param productID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     */
    ObjectIdentifier productSpecificationStartBankGet(Infos.ObjCommon objCommon, ObjectIdentifier productID);
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2018/10/29 16:15
     * @param objCommon
     * @param productID
     * @param waferCount
     * @param lotType
     * @param subLotType -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     */
    ObjectIdentifier productRequestForControlLotRelease(Infos.ObjCommon objCommon, ObjectIdentifier productID, Integer waferCount, String lotType, String subLotType);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/12 17:53
     * @param objCommon
     * @param productID -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void productExistenceCheck(Infos.ObjCommon objCommon, ObjectIdentifier productID);

    /**
     * This method gets product route information.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/22 15:44
     */
    ObjectIdentifier productRouteInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier productID);
}
