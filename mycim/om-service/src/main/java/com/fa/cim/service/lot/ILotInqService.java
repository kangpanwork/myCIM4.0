package com.fa.cim.service.lot;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 15:27
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ILotInqService {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/6/26         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/6/26 17:41
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.LotHoldListAttributes> sxHoldLotListInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/19         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/19 11:05
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.AliasWaferName> sxWaferAliasInfoInq(Infos.ObjCommon objCommon, Params.WaferAliasInfoInqParams params) ;

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-11-23                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-11-23 16:18
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.WaferListInLotFamilyInfo> sxWaferListInLotFamilyInq(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID) ;

    /**
     * description:
     * The method use to define the MultiPathListInq controller.
     * transaction ID: OLOTQ013
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/10        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/10 10:16
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.ConnectedRouteList> sxMultiPathListInq(Params.MultiPathListInqParams multiPathListInqParams, Infos.ObjCommon objCommon);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/6        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/11/6 15:24
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Page<Infos.DynamicRouteList> sxDynamicPathListInq(Infos.ObjCommon objCommon, Params.DynamicPathListInqParams params) ;

    /**
     * description:
     * The method use to define the LotFamilyInq Controller.
     * transaction ID: OLOTQ004
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/10        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/10 10:16
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.LotFamilyInqResult sxLotFamilyInq(Params.LotFamilyInqParams lotFamilyInqParams, Infos.ObjCommon objCommon);

    /**
     * description:
     * <p>LotFuturePctrlDetailInfoInq .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/4        ********             Scott               create file
     *
     * @author: Scott
     * @date: 2018/12/4 13:37
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.LotFuturePctrlDetailInfoInqResult sxLotFuturePctrlDetailInfoInq(Infos.ObjCommon objCommon,
                                                                            Params.FutureActionDetailInfoInqParams futureActionDetailInfoInqParams) ;

    /**
     * description:
     * The method use to define the LotInfoByWaferInqController.
     * transaction ID: OLOTQ005
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/15       ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/15 11:26
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    ObjectIdentifier sxLotInfoByWaferInq(Params.LotInfoByWaferInqParams lotInfoByWaferInqParams, Infos.ObjCommon objCommon);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/3         OLOTQ001              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/5/3 15:21
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.LotInfoInqResult sxLotInfoInq(Infos.ObjCommon objCommon, Params.LotInfoInqParams lotInfoInqParams) ;

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/30        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/5/30 13:14
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.LotListByCarrierInqResult sxLotListByCarrierInq(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    /**
     * description:
     * The method use to define the LotListByCJInqController.
     * transaction ID: OLOTQ007
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/22        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/22 17:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.ControlJobInfo> sxLotListByCJInq(Params.LotListByCJInqParams lotListByCJInqParams, Infos.ObjCommon objCommon);

    /**
     * description: filter lot for Move Bank Screen, only display 'InBank' Lot
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/8        ********            Bear         create file
     *
     * @author: Bear
     * @date: 2018/4/8 10:19
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Page<Infos.LotListAttributes> sxLotListInq(Params.LotListInqParams lotListInqParams, Infos.ObjCommon objCommon);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/8/27         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/8/27 10:34
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.LotOperationSelectionInqResult sxLotOperationSelectionInq(Infos.ObjCommon objCommon, Params.LotOperationSelectionInqParams params) ;

    /**
     * description:
     * The method use to define the AllMfgLayerListInqController.
     * transaction ID: OLOTQ022
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/23        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/23 15:40
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.SubMfgLayerAttributes> sxAllMfgLayerListInq(Infos.ObjCommon objCommon);

    /**
     * description:
     * The method use to define the AllProcessStepListInqController.
     * transaction ID: OCONQ003
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/24        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/24 11:26
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<ObjectIdentifier> sxAllProcessStepListInq(Infos.ObjCommon objCommon) ;

    /**
     * description:
     * The method use to define the MainProcessFlowListInqController.
     * transaction ID: OLOTQ026
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/24        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/24 11:26
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.RouteIndexInformation> sxMainProcessFlowListInq(Infos.ObjCommon objCommon, Params.MainProcessFlowListInqParams params) ;

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/25       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/25 14:22
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.OperationNameAttributes> sxProcessFlowOperationListForLotInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * The method use to define the ProcessFlowOperationListInqController.
     * transaction ID: ODRBQ013
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/16        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/16 15:38
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.OperationNameAttributes> sxProcessFlowOperationListInq(Infos.ObjCommon objCommon,
                                                                      Params.ProcessFlowOperationListInqParams processFlowOperationListInqParams);

    Results.LotsMoveInReserveInfoInqResult sxLotsMoveInReserveInfoInq(Infos.ObjCommon objCommon,
                                                                      Params.LotsMoveInReserveInfoInqParams lotsMoveInReserveInfoInqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/11/30/030 16:02
     */
    List<ObjectIdentifier> getAllLotType();

}
