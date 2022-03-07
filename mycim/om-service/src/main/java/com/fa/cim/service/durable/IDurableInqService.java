package com.fa.cim.service.durable;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * description:
 * This file use to define the IDurableInqService interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 17:06
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDurableInqService {

    Results.WhatNextDurableListInqResult sxDrbWhatNextListInq(Infos.ObjCommon strObjCommonIn, Params.WhatNextDurableListInqInParam strWhatNextDurableListInqInParam);

    Results.DurableStatusSelectionInqResult sxDurableStatusSelectionInq(Infos.ObjCommon objCommon, ObjectIdentifier durableID);

    List<Infos.CandidateDurableSubStatusDetail> sxDurableSubStatusSelectionInq(Infos.ObjCommon objCommon, Params.DurableSubStatusSelectionInqParams params);

    List<Infos.ConnectedRouteList> sxConnectedDurableRouteListInq(Infos.ObjCommon objCommon, Infos.ConnectedDurableRouteListInqInParam param);

    List<Infos.DurableControlJobListInfo> sxDrbControlJobListInq(Infos.ObjCommon objCommon, Params.DurableControlJobListInqParams params);

    List<Infos.DurableHoldListAttributes> sxDrbHoldListInq(Infos.ObjCommon objCommon, Params.DurableHoldListInqInParam params);

    Results.DurablesInfoForOpeStartInqResult sxDrbInfoForMoveInInq(Infos.ObjCommon objCommon, Params.DurablesInfoForOpeStartInqInParam paramIn);

    Results.DurablesInfoForStartReservationInqResult sxDrbInfoForMoveInReserveInq(Infos.ObjCommon objCommon, Params.DurablesInfoForStartReservationInqInParam param);

    List<Infos.DurableOperationHisInfo> sxDrbStepHistoryInq(Infos.ObjCommon strObjCommonIn, Params.DurableOperationHistoryInqInParam strDurableOperationHistoryInqInParam);

    Results.DurableOperationListFromHistoryInqResult sxDrbStepListFromHistoryInq(Infos.ObjCommon strObjCommonIn, Params.DurableOperationListFromHistoryInqInParam strDurableOperationListFromHistoryInqInParam);

    Results.DurableOperationListInqResult sxDrbStepListInq(Infos.ObjCommon objCommon, Params.DurableOperationListInqInParam durableOperationListInqInParam);

    List<Infos.OperationNameAttributes> sxProcessFlowOperationListForDrbInq(Infos.ObjCommon strObjCommonIn, Params.RouteOperationListForDurableInqInParam strRouteOperationListForDurableInqInParam);

    Results.DurableOperationStartReqResult sxDrbMoveInReq(Infos.ObjCommon objCommon, Params.DurableOperationStartReqInParam param);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return RetCode<List < ObjectIdentifier>>
     * @author Sun
     * @date 2018/10/16
     */
    List<ObjectIdentifier> sxAllReticleGroupListInq(Infos.ObjCommon objCommon);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return RetCode<List < ObjectIdentifier>>
     * @author Sun
     * @date 2018/10/16
     */
    List<ObjectIdentifier> sxAllReticleListInq(Infos.ObjCommon objCommon);

    List<Infos.AvailableCarrierOut> sxAvailableCarrierListForLotStartInq(Infos.ObjCommon objCommon);

    Results.ReticlePodDetailInfoInqResult sxReticlePodDetailInfoInq(Infos.ObjCommon objCommon, Params.ReticlePodDetailInfoInqParams params);

    List<Infos.ReticlePodListInfo> sxReticlePodListInq(Infos.ObjCommon objCommon, Params.ReticlePodListInqParams params);

    Page<Infos.ReticlePodListInfo> sxPageReticlePodListInq(Infos.ObjCommon objCommon, Params.ReticlePodListInqParams params);

    List<Infos.DurableAttribute> sxReticlePodListWithBasicInfoInq(Infos.ObjCommon objCommon, List<ObjectIdentifier> reticlePodIDList);

    Results.ReticleStocInfoInqResult sxReticleStocInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    List<Infos.DurableAttribute> sxCarrierBasicInfoInq(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDSeq);

    Results.ReticleDetailInfoInqResult sxReticleDetailInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, boolean durableOperationInfoFlag, boolean durableWipOperationInfoFlag);

    Results.ReticleListInqResult sxReticleListInq(Infos.ObjCommon objCommon, Params.ReticleListInqParams reticleListInqParams);

    /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/26 11:05                     Aoki                Create
    *
    * @author Aoki
    * @date 2021/3/26 11:05
    * @param objCommon
     * @param reticleListInqParams
    * @return com.fa.cim.dto.Results.PageReticleListInqResult
    */
    Results.PageReticleListInqResult sxPageReticleListInq(Infos.ObjCommon objCommon, Params.ReticleListInqParams reticleListInqParams);

    Results.CandidateDurableJobStatusDetail sxDurableJobStatusSelectionInq(Infos.ObjCommon objCommon, Params.DurableJobStatusSelectionInqParams params);

    /**
     * description: 用于查询earack in/out 的carrier和reticlepod
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/20 15:20                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/10/20 15:20
     * @param objCommon
     * @param stockerID -
     * @return com.fa.cim.dto.Results.ErackPodInfoInqResult
     */
    Results.ErackPodInfoInqResult sxErackPodInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    /**
     * description: This function will retrieve reticle pod stocker information.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/9 10:48
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.ReticlePodStockerInfoInqResult sxReticlePodStockerInfoInq(Infos.ObjCommon objCommon, Params.ReticlePodStockerInfoInqParams params);

    /**
     * description: This function retieve the basic information for a bare reticle stocker.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/20 16:49
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.BareReticleStockerInfoInqResult sxBareReticleStockerInfoInq(Infos.ObjCommon objCommon, Params.BareReticleStockerInfoInqParams params);

    /**
     * description: Select bank from durable where bank in can be performed
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/19 11:34                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/19 11:34
     * @param objCommon
     * @param params -
     * @return com.fa.cim.dto.Results.BankListInqResult
     */
    Results.BankListInqResult sxDrbBankInListInq(Infos.ObjCommon objCommon, Params.BankListInqParams params);

}
