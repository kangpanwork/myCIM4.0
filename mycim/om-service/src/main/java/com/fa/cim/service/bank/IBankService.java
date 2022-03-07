package com.fa.cim.service.bank;


import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.runtime.lottype.CimLotTypeDO;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 13:59
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IBankService {

    /**
     * description: If the Lot state satisfies the requirement, then the Lot and Bank associations are modified
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/17 11:19                     ZH                Create
     *
     * @param objCommon
     * @param bankMoveReqParams -
     * @return java.util.List<com.fa.cim.dto.Infos.ReturnCodeInfo>
     * @author ZH
     * @date 2021/3/17 11:19
     */
    List<Infos.ReturnCodeInfo> sxBankMoveReq(Infos.ObjCommon objCommon, Params.BankMoveReqParams bankMoveReqParams);

    /**
     * description: If the Lot state satisfies the requirement, change the state of Lot to ONHOLD, and add the Hold Record
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/17 13:50                     ZH                Create
     *
     * @param objCommon
     * @param i
     * @param holdLotInBankReqParams -
     * @return com.fa.cim.dto.Results.HoldLotInBankReqResult
     * @author ZH
     * @date 2021/3/17 13:50
     */
    Results.HoldLotInBankReqResult sxHoldLotInBankReqReq(Infos.ObjCommon objCommon, Integer i, Params.HoldLotInBankReqParams holdLotInBankReqParams);

    /**
     * description: If the Lot state satisfies the requirement, change the state of Lot to NOTONHOLD, and remove the Hold Record
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/17 14:03                     ZH                Create
     *
     * @param objCommon
     * @param i
     * @param holdLotReleaseInBankReqParams -
     * @return java.util.List<com.fa.cim.dto.Infos.HoldHistory>
     * @author ZH
     * @date 2021/3/17 14:03
     */
    List<Infos.HoldHistory> sxHoldLotReleaseInBankReq(Infos.ObjCommon objCommon, Integer i, Params.HoldLotReleaseInBankReqParams holdLotReleaseInBankReqParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/18 18:43                     Aoki                Create
     *
     * @param objCommon
     * @param materialPrepareCancelReqParams
     * @return com.fa.cim.dto.Results.MaterialPrepareCancelReqResult
     * @author Aoki
     * @date 2021/3/18 18:43
     */
    Results.MaterialPrepareCancelReqResult sxMaterialPrepareCancelReq(Infos.ObjCommon objCommon, Params.MaterialPrepareCancelReqParams materialPrepareCancelReqParams);

    /**
     * description: In normal case, wafers from vendor lot in bank will be picked up and inserted into an empty carrier.
     * At that time, these vendor lots will be used as the source for control wafer lots as well as production lots.
     * Under the multiple lots per carrier environment to support 300mm process,
     * vendor lot in bank can be inserted into any carrier regardless it is empty or not up to the carrier’s capacity
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/18 18:39                     Aoki                Create
     *
     * @param objCommon
     * @param materialPrepareReqParams
     * @return com.fa.cim.dto.Results.MaterialPrepareReqResult
     * @author Aoki
     * @date 2021/3/18 18:39
     */
    Results.MaterialPrepareReqResult sxMaterialPrepareReq(Infos.ObjCommon objCommon, Params.MaterialPrepareReqParams materialPrepareReqParams);

    List<CimLotTypeDO> getLotType();

    ObjectIdentifier sxMaterialReceiveAndPrepareReq(Params.MaterialReceiveAndPrepareReqParams materialReceiveAndPrepareReqParams, Infos.ObjCommon objCommon);

    /**
     * description: Lot’s inventory state is changed from “NonProBankIn” to “OnFloor”, and the responsible department is changed from “Production Control” to “Manufacturing”.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/18 19:29                     QSZ                Create
     *
     * @author QSZ
     * @date 2021/3/18 19:29
     * @param objCommon
     * @param lotID
     * @param holdCheckFlag -
     * @return void
     */
    void sxNonProdBankReleaseReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, boolean holdCheckFlag);

    /**     
     * description: The Non-Pro Bank-In function provides that “WIP” lot, which inventory state is “OnFloor” is moved to “Non-Pro Bank”.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/18 19:40                     QSZ                Create
     *       
     * @author QSZ
     * @date 2021/3/18 19:40
     * @param objCommon
     * @param params -
     * @return void
     */
    void sxNonProdBankStoreReq(Infos.ObjCommon objCommon, Params.NonProdBankStoreReqParams params);

    List<Infos.ReturnCodeInfo> sxShipReq(Infos.ObjCommon objCommon, Params.BankMoveReqParams shipReqParams);

    List<Infos.ReturnCodeInfo> sxUnshipReq(Infos.ObjCommon objCommon, Params.BankMoveReqParams unshipReqParams);

    /**
     * description: 1.Create Material (vendor) Lot information from received vendor lot data
     * 2.Lot-id will be assigned automatically by system. (Refer to Appendix - LotID Naming Rule)
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/18 18:40                     Aoki                Create
     *
     * @param objCommon
     * @param vendorLotReceiveParams
     * @return com.fa.cim.dto.Results.VendorLotReceiveReqResult
     * @author Aoki
     * @date 2021/3/18 18:40
     */
    Results.VendorLotReceiveReqResult sxVendorLotReceiveReq(Infos.ObjCommon objCommon, Params.VendorLotReceiveParams vendorLotReceiveParams);

    List<String> getProductID(String str);

    List<String> getRaw(String str);

    /**
     * description: 1.The quantity of the selected lot will be decreased by the returned quantity.
     * 2.If the quantity becomes zero, the Finished State of the lot will be changed ro emptied.
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/18 18:41                     Aoki                Create
     *
     * @param objCommon
     * @param vendorLotReturnParams
     * @return com.fa.cim.dto.Results.VendorLotReturnReqResult
     * @author Aoki
     * @date 2021/3/18 18:41
     */
    Results.VendorLotReturnReqResult sxVendorLotReturnReq(Infos.ObjCommon objCommon, Params.VendorLotReturnParams vendorLotReturnParams);

    /**
     * description: When a Bank-In lot is needed to move back to the previous process flow because of a mistake, retest and/or some reasons, the Bank-In Cancel function can be used to
     * cancel the Bank In.
     *
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/19 16:46                     QSZ                Create
     *
     * @author QSZ
     * @date 2021/3/19 16:46
     * @param objCommon
     * @param lotID
     * @param claimMemo -
     * @return void
     */
    void sxBankInCancelReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String claimMemo);

    /**
     * description: When a lot completes all processed in a route then this BankIn claim is executed for the lot,  The lot will be moved into a Bank that is managed by Production Control.
     *
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/19 16:38                     QSZ                Create
     *
     * @author QSZ
     * @date 2021/3/19 16:38
     * @param objCommon
     * @param seqIndex
     * @param lotIDs
     * @param claimMemo -
     * @return java.util.List<com.fa.cim.dto.Infos.BankInLotResult>
     */
    List<Infos.BankInLotResult> sxBankInReq(Infos.ObjCommon objCommon, int seqIndex, List<ObjectIdentifier> lotIDs, String claimMemo);

    /**
     * proc auto bank-in in post process phase
     *
     * @param objCommon objComm
     * @param lotID lotID
     * @param claimMemo claimMemo
     * @return if the lot is auto bank-in required
     */
    boolean sxBankInByPostProcessReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String claimMemo);
}
