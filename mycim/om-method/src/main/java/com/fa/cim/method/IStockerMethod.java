package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * This file use to define the IStockerMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/16        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/7/16 18:01
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IStockerMethod {
    
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/15 10:21
     * @param objCommon
     * @param stockerID -
     * @return com.fa.cim.dto.Outputs.ObjStockerTypeGetDROut
     */
    Outputs.ObjStockerTypeGetDROut stockerTypeGet(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/27 17:11
     * @param objCommon
     * @param stockerID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Results.StockerInfoInqResult>
     */
    Results.StockerInfoInqResult stockerBaseInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param stockerType
     * @param availFlag
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.StockerListInqResult>
     * @author Ho
     * @date 2018/10/9 10:58:16
     */
    Results.StockerListInqResult stockerListInfoGetDR(Infos.ObjCommon objCommon, String stockerType, boolean availFlag);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.StockerForAutoTransferInqResult>
     * @author Ho
     * @date 2018/10/9 16:10:32
     */
    List<Infos.AvailableStocker> stockerFillInTxLGQ002DR(Infos.ObjCommon objCommon);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param stockerId
     * @return Boolean
     * @author Paladin
     * @date 2018/10/17 16:10:32
     */
    Boolean stockerInventoryStateGet(Infos.ObjCommon objCommon, ObjectIdentifier stockerId);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param stockerID
     * @param inventoryRequestedFlag
     * @return RetCode
     * @author Paladin
     * @date 2018/10/17 16:10:32
     */
    void stockerInventoryStateChange(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, boolean inventoryRequestedFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/14 14:17
     * @param objCommon
     * @param stockerID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Results.ReticleStocInfoInqResult>
     */
    Results.ReticleStocInfoInqResult stockerFillInTxPDQ006DR(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/27 14:25
     * @param objCommon
     * @param stockerInfo
     * @param lotID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjStockerUTSPriorityOrderGetByLotAvailabilityOut>
     */
    List<Infos.EqpStockerStatus> stockerUTSPriorityOrderGetByLotAvailability(Infos.ObjCommon objCommon, List<Infos.EqpStockerStatus> stockerInfo, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/27 14:35
     * @param objCommon
     * @param stockerInfo
     * @param lotID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjStockerPriorityOrderGetByLotAvailabilityOut>
     */
    List<Infos.EqpStockerStatus> stockerPriorityOrderGetByLotAvailability(Infos.ObjCommon objCommon, List<Infos.EqpStockerStatus> stockerInfo, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/6/27 23:58
     * @param objCommon
     * @param stockerID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjStockerUTSVacantSpaceCheckDROut>
     */
    Outputs.ObjStockerUTSVacantSpaceCheckDROut stockerUTSVacantSpaceCheckDR(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/9                          Wind
     * @param objCommon
     * @param stockerID
     * @return RetCode<Outputs.ObjStockerTypeGetDROut>
     * @author Wind
     * @date 2018/11/9 15:43
     */
    Outputs.ObjStockerTypeGetDROut stockerTypeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param stockerID
     * @return java.util.List<com.fa.cim.dto.Infos.StockerEqp>
     * @exception
     * @author ho
     * @date 2020/3/17 14:40
     */
    List<Infos.StockerEqp> stockerUTSRelatedEquipmentListGetDR ( Infos.ObjCommon strObjCommonIn, ObjectIdentifier stockerID);

    void stockerReticlePodUnload(
            Infos.ObjCommon strObjCommonIn,
            ObjectIdentifier stockerID,
            ObjectIdentifier resourceID,
            ObjectIdentifier reticlePodID,
            String claimMemo, Infos.ShelfPosition shelfPosition);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param stockerID
     * @return java.util.List<com.fa.cim.dto.Infos.ReticlePodPortInfo>
     * @exception
     * @author ho
     * @date 2020/3/20 12:40
     */
    public List<Infos.ReticlePodPortInfo> stockerReticlePodPortInfoGetDR(Infos.ObjCommon strObjCommonIn, ObjectIdentifier stockerID );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strStockerLoadLotDeleteIn
     * @return void
     * @exception
     * @author ho
     * @date 2020/3/20 12:55
     */
    public void stockerLoadLotDelete(Infos.ObjCommon strObjCommonIn, Infos.StockerLoadLotDeleteIn strStockerLoadLotDeleteIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strStockerOnlineModeGetIn
     * @return java.lang.String
     * @exception
     * @author ho
     * @date 2020/3/20 13:27
     */
    public String stockerOnlineModeGet(Infos.ObjCommon strObjCommonIn, ObjectIdentifier  strStockerOnlineModeGetIn );


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4 15:00                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/11/4 15:00
     * @param strObjCommonIn
     * @param stockerID -
     * @return java.util.List<com.fa.cim.dto.Infos.ReticlePodInfoInStocker>
     */

    public List<Infos.ReticlePodInfoInStocker> stockerStoredReticlePodGetDR(Infos.ObjCommon strObjCommonIn, ObjectIdentifier stockerID);

    void stockerReticlePodPortAccessModeChange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier reticlePodPortID, String newAccessMode);


    void stockerStatusCheckForReticleRetrieve(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier reticlePodPortID,ObjectIdentifier  reticlePodID, List<Infos.MoveReticles> strMoveReticlesSeq);

    List<Infos.StoredReticle> stockerStoredReticleGetDR(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/6 16:57
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void stockerReticlePodPortStateChange(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier portID, String portStatus);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/11 17:04
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void stockerReticlePodPortDispatchStateChange(Infos.ObjCommon strObjCommonIn, Inputs.StockerReticlePodPortDispatchStateChangeIn strStockerReticlePodPortDispatchStateChangeIn);

    /**
     * description:retrieve stored reticle in equipment list this is direct sql function.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/17 14:25                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/17 14:25
     * @param objCommon
     * @param stockerID
     * @param resourceID
     * @param reticlePodID
     * @param claimMemo -
     * @return void
     */
    void stockerReticlePodLoad(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier resourceID, ObjectIdentifier reticlePodID, String claimMemo);
    
    /**     
     * description:This method adds RSP for a RSP port on stocker.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/17 17:34                     Nyx                Create
     *       
     * @author Nyx
     * @date 2020/11/17 17:34
     * @param objCommon
     * @param stockerID
     * @param reticlePodPortID
     * @param loadedReticlePodID -
     * @return void
     */
    void stockerLoadLotAdd(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier reticlePodPortID, ObjectIdentifier loadedReticlePodID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/20 18:43
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void stockerOnlineModeChange(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, String onlineMode);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param stockerID
     * @return java.lang.String
     * @exception
     * @author Jerry_Huang
     * @date 2020/11/10
     */
    Results.FixtureStockerInfoInqResult stockerFillInTxPDQ003DR(Infos.ObjCommon strObjCommonIn, ObjectIdentifier stockerID);
}
