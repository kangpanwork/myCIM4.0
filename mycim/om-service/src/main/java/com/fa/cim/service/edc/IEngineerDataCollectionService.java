package com.fa.cim.service.edc;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>IEngineerDataCollectionService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/8/008   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/8/008 16:30
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEngineerDataCollectionService {

    Results.EDCWithSpecCheckActionReqResult sxEDCWithSpecCheckActionReq(Infos.ObjCommon strObjCommonIn, Infos.EDCWithSpecCheckActionReqInParm strEDCWithSpecCheckActionReqInParm, String claimMemo);

    Results.SpecCheckReqResult sxSpecCheckReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> startCassetteList);

    ObjectIdentifier sxEDCTransitDataRpt(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, ObjectIdentifier controlJobID, List<Infos.StartCassette> startCassetteList);

    Results.SPCCheckReqResult sxSPCCheckReq(Infos.ObjCommon objCommon, Params.SPCCheckReqParams spcCheckReqParams);

    void sxSPCDoActionReq(Infos.ObjCommon objCommon, List<Infos.BankMove> bankMoveList, List<Infos.MailSend> mailSendList, List<Infos.ReworkBranch> reworkBranchList);

    Results.EDCWithSpecCheckActionByPJReqResult sxEDCWithSpecCheckActionByPJReq(Infos.ObjCommon objCommon, Params.EDCWithSpecCheckActionByPJReqParams params);

    Results.EDCByPJRptResult sxEDCByPJRpt(Infos.ObjCommon objCommon, Params.EDCByPJRptInParms params);

    void sxDChubDataSendCompleteRpt(Infos.ObjCommon strObjCommonIn, Params.DChubDataSendCompleteRptInParam strDChubDataSendCompleteRptInParam);

    void sxEDCDataUpdateForLotReq(Infos.ObjCommon strObjCommonIn, Params.EDCDataUpdateForLotReqInParm strEDCDataUpdateForLotReqInParm, String claimMemo);

    /**
     * This function performs SPEC Check and SPC Check, and performs action to the entities specified
     * based on the check result.
     *
     * <p> The actions are Entity Constraint and Hold.
     *
     * <p> If the target Lot belongs to Monitor Group and it is the representative Lot,the Monitor Group
     * and Hold of Monitored Lot are released according to a setup of the previous operation of Lot.
     *
     * <p> And then, if the monitored Lot's operation which moved by Hold release is measurement,
     * measurement operation is passed.
     *
     * <p> At last, PO's collected data information is stored with data conversion.
     *
     * @param objCommon  objCommon
     * @param params params
     * @return move out information
     */
    Results.CollectedDataActionByPostProcReqResult sxEDCWithSpecCheckActionByPostTaskReq(Infos.ObjCommon objCommon,
                                                                                         Params.EDCWithSpecCheckActionByPostTaskReqParams params);

    /**
     * EDC information write in run time stage at PostProcess function for improve transaction performance as following.
     * <ui>
     * <li>MoveInReserveReq</li>
     * <li>MoveInReserveForIBReq</li>
     * <li>MoveInReserveForTOTIReq</li>
     * <li>MoveInReq</li>
     * <li>MoveInForIBReq</li>
     * <li>SLMStartLotsReservationReq</li>
     * </ui>
     *
     * @param objCommon objCommon
     * @param controlJobID controlJobID
     * @param lotID lotID
     * @author zqi
     */
    void sxEDCInformationSetByPostProcReq(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, ObjectIdentifier lotID);

    /**
     * EDC information write in run time stage at PostProcess function for improve transaction performance as following.
     * <ui>
     * <li>MoveInReserveReq</li>
     * <li>MoveInReserveForIBReq</li>
     * <li>MoveInReserveForTOTIReq</li>
     * <li>MoveInReq</li>
     * <li>MoveInForIBReq</li>
     * <li>SLMStartLotsReservationReq</li>
     * </ui>
     *
     * @param objCommon objCommon
     * @param equipmentID equipmentID
     * @param lotID lotID
     * @author zqi
     */
    void sxEDCInformationSetByPostProcReq_2(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier lotID);
}
