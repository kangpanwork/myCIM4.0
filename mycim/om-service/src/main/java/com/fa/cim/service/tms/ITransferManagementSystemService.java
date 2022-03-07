package com.fa.cim.service.tms;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:42
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ITransferManagementSystemService {

    Results.CarrierReserveCancelReqResult sxCarrierReserveCancelReq(Infos.ObjCommon objCommon, List<Infos.ReserveCancelLotCarrier> reserveCancelLotCarriers, String claimMemo);

    Results.CarrierReserveReqResult sxCarrierReserveReq(Infos.ObjCommon objCommonIn, Params.CarrierReserveReqParam params);

    void sxCarrierTransferJobEndRpt(Infos.ObjCommon objCommon, Params.CarrierTransferJobEndRptParams carrierTransferJobEndRptParams);

    Results.CarrierTransferStatusChangeRptResult sxCarrierTransferStatusChangeRpt(Infos.ObjCommon objCommonIn, Params.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams);

    void sxLotCassetteXferDeleteReq(Infos.ObjCommon objCommon, Params.CarrierTransferJobDeleteReqParam params);

    Results.CarrierTransferReqResult sxCarrierTransferForIBReq(Infos.ObjCommon objCommonIn,
                                                               ObjectIdentifier equipmentID) ;

    Results.CarrierTransferReqResult sxCarrierTransferReq(Infos.ObjCommon objCommonIn,
                                                          ObjectIdentifier equipmentID);

    Results.CarrierTransferReqResult sxFMCCarrierTransferReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID) ;


    Results.SingleCarrierTransferReqResult sxSingleCarrierTransferReq(Infos.ObjCommon objCommonIn,
                                                                      Params.SingleCarrierTransferReqParam params) ;

    void sxMultipleCarrierTransferReq(Infos.ObjCommon objCommon, Boolean rerouteFlag,String transportType,List<Infos.CarrierXferReq> strCarrierXferReq) ;

    Results.StockerInventoryUploadReqResult sxStockerInventoryUploadReq(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, Infos.ShelfPosition shelfPosition, String claimMemo);


    Results.StockerInventoryRptResult sxStockerInventoryRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, Infos.ShelfPosition shelfPosition, List<Infos.InventoryLotInfo> inventoryLotInfos, String claimMemo);

    ObjectIdentifier sxStockerStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier stockerStatusCode, String claimMemo);

    void sxNPWCarrierReserveForIBReq(Infos.ObjCommon objCommon, Params.NPWCarrierReserveForIBReqParams params);

    void sxNPWCarrierReserveReq(Infos.ObjCommon objCommon, Params.NPWCarrierReserveReqParams params);

    void sxNPWCarrierReserveCancelForIBReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroup, List<Infos.NPWXferCassette> strNPWXferCassette, Boolean notifyToTCSFlag, String claimMemo);

    void sxNPWCarrierReserveCancelReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, List<Infos.NPWXferCassette> strNPWXferCassette, Boolean notifyToTCSFlag, String claimMemo);

    Results.LotCarrierTOTIReqResult sxLotCarrierTOTIReq(Infos.ObjCommon objCommon, Params.LotCarrierTOTIReqParam params,String claimMemo) ;

    void sxMultipleDurableTransferReq(Infos.ObjCommon objCommon, Boolean rerouteFlag,String transportType,List<Infos.DurableXferReq> strDurableXferReq,String durableCategory) ;

    void sxSingleDurableTransferReq(Infos.ObjCommon objCommonIn, Params.SingleDurableTransferReqParam params,String durableCategory) ;


    Results.CarrierTransferReqResult sxDmsTransferReq(Infos.ObjCommon objCommonIn,
                                                                 ObjectIdentifier equipmentID);

    Results.CarrierTransferReqResult sxDmsTransferForIBReq(Infos.ObjCommon objCommonIn,
                                                                      ObjectIdentifier equipmentID);

}
