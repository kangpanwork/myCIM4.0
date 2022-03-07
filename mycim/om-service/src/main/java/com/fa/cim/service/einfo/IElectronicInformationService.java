package com.fa.cim.service.einfo;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>ElectronicInformationService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/8/008   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/8/008 16:21
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IElectronicInformationService {

    void sxEboardInfoSetReq(Infos.ObjCommon objCommon, String noticeTitle, String noticeDescription);

    ObjectIdentifier sxLotMemoAddReq(Infos.ObjCommon objCommon, Params.LotMemoAddReqParams params);

    ObjectIdentifier sxLotOperationNoteInfoRegisterReq(Params.LotOperationNoteInfoRegisterReqParams params, Infos.ObjCommon objCommon);

    /**
     * description: eqp board work zone binding req
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpBoardWorkZoneBindingParams - eqpBoardWorkZoneBindingParams
     * @author YJ
     * @date 2021/2/19 0019 19:23
     */
    void sxEqpBoardWorkZoneBindingReq(Infos.ObjCommon objCommon, Params.EqpBoardWorkZoneBindingParams eqpBoardWorkZoneBindingParams);

    /**
     * description: eqp area cancel req
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon           - objCommon
     * @param eqpAreaCancelParams - eqpAreaCancelParams
     * @author YJ
     * @date 2021/2/20 0020 10:13
     */
    void sxEqpAreaCancelReq(Infos.ObjCommon objCommon, Params.EqpAreaCancelParams eqpAreaCancelParams);

    /**
     * description: eqp area move params
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpAreaMoveParams - eqp area move params
     * @author YJ
     * @date 2021/2/20 0020 14:58
     */
    void sxEqpAreaMoveReq(Infos.ObjCommon objCommon, Params.EqpAreaMoveParams eqpAreaMoveParams);
}
