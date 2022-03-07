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
public interface ITransferManagementSystemInqService {

    Results.WhereNextStockerInqResult sxWhereNextStockerInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier cassetteID);

    Results.LotCassetteXferJobDetailResult sxCarrierTransferJobDetailInfoInq(Infos.ObjCommon objCommon, Params.CarrierTransferJobDetailInfoInqParam params) ;

    Results.CarrierTransferJobInfoInqResult sxCarrierTransferJobInfoInq(Infos.ObjCommon objCommon, Params.CarrierTransferJobInfoInqParam params);

    Results.StockerInfoInqResult sxStockerInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier machineID, boolean detailFlag);

    Results.StockerListInqResult sxStockerListInq(Infos.ObjCommon objCommon, String stockerType, boolean availFlag);

    ObjectIdentifier sxWhereNextOHBCarrierInq (Infos.ObjCommon strObjCommonIn,
                                               Params.WhereNextOHBCarrierInqInParm strWhereNextOHBCarrierInqInParm) ;

    List<ObjectIdentifier> sxAllEqpForAutoTransferInq(Infos.ObjCommon objCommon);


    List<ObjectIdentifier> sxEqpForAutoTransferInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier lotID, ObjectIdentifier cassetteID, ObjectIdentifier durableID);


    List<Infos.AvailableStocker> sxStockerForAutoTransferInq(Infos.ObjCommon objCommon);


    Results.DurableWhereNextStockerInqResult sxDurableWhereNextStockerInq(Infos.ObjCommon objCommon, ObjectIdentifier durableID);



}
