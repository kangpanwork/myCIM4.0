package com.fa.cim.service.processcontrol.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IMinQTimeMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.method.IQTimeMethod;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.service.processcontrol.IProcessControlInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 17:16
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class ProcessControlInqService implements IProcessControlInqService {
    @Autowired
    private ILotMethod lotMethod;
    @Autowired
    private IMinQTimeMethod minQTimeMethod;
    @Autowired
    private IQTimeMethod qTimeMethod;
    @Autowired
    private IProcessMethod processMethod;
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Override
    public Page<Infos.FutureHoldListAttributes> sxFutureHoldListInq(Infos.ObjCommon objCommon, Infos.FutureHoldSearchKey futureHoldSearchKey, Integer count, SearchCondition searchCondition) {
        List<Infos.FutureHoldListAttributes> out = lotMethod.lotFutureHoldListbyKeyDR(objCommon, futureHoldSearchKey, count);
        searchCondition = CimObjectUtils.isEmpty(searchCondition) ? new SearchCondition() : searchCondition;
        return CimPageUtils.convertListToPage(out, searchCondition.getPage(), searchCondition.getSize());
    }

    @Override
    public List<Infos.LotCtrlStatus> sxNPWUsageStateSelectionInq(Infos.ObjCommon objCommon) {
        return lotMethod.lotFillInTxPCQ017DR(objCommon);
    }

    @Override
    public List<Infos.QrestTimeInfo> sxQtimeDefinitionSelectionInq(Infos.ObjCommon objCommon, Params.QtimeDefinitionSelectionInqParam qtimeDefinitionSelectionInqParam) {
        log.info("【Method Entry】 sxQtimeDefinitionSelectionInq");

        Inputs.QtimeDefinitionSelectionInqIn qtimeDefinitionSelectionInqIn = new Inputs.QtimeDefinitionSelectionInqIn();
        qtimeDefinitionSelectionInqIn.setLotID(qtimeDefinitionSelectionInqParam.getQtimeDefinitionSelectionInqIn().getLotID());
        qtimeDefinitionSelectionInqIn.setRouteID(qtimeDefinitionSelectionInqParam.getQtimeDefinitionSelectionInqIn().getRouteID());
        qtimeDefinitionSelectionInqIn.setOperationNumber(qtimeDefinitionSelectionInqParam.getQtimeDefinitionSelectionInqIn().getOperationNumber());
        qtimeDefinitionSelectionInqIn.setBranchInfo(qtimeDefinitionSelectionInqParam.getQtimeDefinitionSelectionInqIn().getBranchInfo());

        //step1 - qtime_candidateList_Get__180
        List<Infos.QrestTimeInfo>  objQTimeCandidateListGetOut = qTimeMethod.qtimeCandidateListGet(objCommon, qtimeDefinitionSelectionInqIn);
        //------------------------------------------------------
        //  Set output result
        //------------------------------------------------------
        log.info("【Method Exit】sxQtimeDefinitionSelectionInq");
        return objQTimeCandidateListGetOut;

    }

    @Override
    public List<Outputs.QrestLotInfo> sxQtimeListInq(Infos.ObjCommon objCommon,
                                                     Infos.QtimeListInqInfo qtimeListInqInfo) {
        List<Outputs.QrestLotInfo> out;
        if (CimStringUtils.isNotEmpty(qtimeListInqInfo.getType())
                && CimStringUtils.equalsIgnoreCase("min", qtimeListInqInfo.getType().trim())) {
            out = minQTimeMethod.getRestrictionsByLot(objCommon, qtimeListInqInfo.getLotID());
        } else {
            if (ObjectIdentifier.isNotEmptyWithValue(qtimeListInqInfo.getLotID())
                    || ObjectIdentifier.isNotEmptyWithValue(qtimeListInqInfo.getWaferID())) {
                //--------------------------------------------------------
                // Get Q-Time restriction action information for one lot
                //--------------------------------------------------------
                out = qTimeMethod.qTimeLotInfoGetDR(objCommon, qtimeListInqInfo);
            } else {
                //---------------------------------------------------
                // Get Q-Time restriction information for every lot
                //---------------------------------------------------
                out = qTimeMethod.qTimeLotListGetDR(objCommon, qtimeListInqInfo.getQTimeType());
            }
        }
        //---------------------------------------------------
        // Get lot information for all lots
        //---------------------------------------------------
        Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
        lotInfoInqFlag.setLotBasicInfoFlag(true);
        lotInfoInqFlag.setLotControlUseInfoFlag(false);
        lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
        lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
        lotInfoInqFlag.setLotOperationInfoFlag(true);
        lotInfoInqFlag.setLotOrderInfoFlag(false);
        lotInfoInqFlag.setLotControlJobInfoFlag(false);
        lotInfoInqFlag.setLotProductInfoFlag(false);
        lotInfoInqFlag.setLotRecipeInfoFlag(false);
        lotInfoInqFlag.setLotLocationInfoFlag(true);
        lotInfoInqFlag.setLotWipOperationInfoFlag(false);
        lotInfoInqFlag.setLotBackupInfoFlag(false);
        if (CimArrayUtils.isNotEmpty(out)) {
            out.forEach(info -> {
                Infos.LotInfo lotDetail = lotMethod.lotDBInfoGetDR(objCommon, lotInfoInqFlag, info.getLotID());
                info.setCassetteID(lotDetail.getLotLocationInfo().getCassetteID());
                info.setLotStatus(lotDetail.getLotBasicInfo().getLotStatus());
                info.setStockerID(lotDetail.getLotLocationInfo().getStockerID());
                info.setTransferStatus(lotDetail.getLotLocationInfo().getTransferStatus());
                info.setRouteID(lotDetail.getLotOperationInfo().getRouteID());
                info.setOperationID(lotDetail.getLotOperationInfo().getOperationID());
                info.setOperationNumber(lotDetail.getLotOperationInfo().getOperationNumber());
                info.setEquipmentID(lotDetail.getLotLocationInfo().getEquipmentID());
            });
        }
        return out;
    }

    @Override
    public Page<Outputs.QrestLotInfo> sxQtimeListInq(Infos.ObjCommon objCommon,
                                                     Infos.QtimePageListInqInfo qtimePageListInqInfo) {
        Page<Outputs.QrestLotInfo> out;
        if (CimStringUtils.isNotEmpty(qtimePageListInqInfo.getType())
                && CimStringUtils.equalsIgnoreCase("min", qtimePageListInqInfo.getType().trim())) {
            out = minQTimeMethod.getRestrictionsByPage(objCommon, qtimePageListInqInfo);
        } else {
            if (ObjectIdentifier.isNotEmptyWithValue(qtimePageListInqInfo.getLotID())
                    || ObjectIdentifier.isNotEmptyWithValue(qtimePageListInqInfo.getWaferID())) {
                //--------------------------------------------------------
                // Get Q-Time restriction action information for one lot
                //--------------------------------------------------------
                out = qTimeMethod.qTimeLotInfoGetDR(objCommon, qtimePageListInqInfo);
            } else {
                //---------------------------------------------------
                // Get Q-Time restriction information for every lot
                //---------------------------------------------------
                out = qTimeMethod.qTimeLotListGetDR(objCommon, qtimePageListInqInfo.getQTimeType(),
                        qtimePageListInqInfo.getSearchCondition());
            }
        }
        //---------------------------------------------------
        // Get lot information for all lots
        //---------------------------------------------------
        Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
        lotInfoInqFlag.setLotBasicInfoFlag(true);
        lotInfoInqFlag.setLotControlUseInfoFlag(false);
        lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
        lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
        lotInfoInqFlag.setLotOperationInfoFlag(true);
        lotInfoInqFlag.setLotOrderInfoFlag(false);
        lotInfoInqFlag.setLotControlJobInfoFlag(false);
        lotInfoInqFlag.setLotProductInfoFlag(false);
        lotInfoInqFlag.setLotRecipeInfoFlag(false);
        lotInfoInqFlag.setLotLocationInfoFlag(true);
        lotInfoInqFlag.setLotWipOperationInfoFlag(false);
        lotInfoInqFlag.setLotBackupInfoFlag(false);
        List<Outputs.QrestLotInfo> pageContent = out.getContent();
        for (Outputs.QrestLotInfo qrestLotInfo : pageContent) {
            Infos.LotInfo objLotDetailInfoGetDROut = lotMethod.lotDBInfoGetDR(objCommon, lotInfoInqFlag,
                    qrestLotInfo.getLotID());
            qrestLotInfo.setCassetteID(objLotDetailInfoGetDROut.getLotLocationInfo().getCassetteID());
            qrestLotInfo.setLotStatus(objLotDetailInfoGetDROut.getLotBasicInfo().getLotStatus());
            qrestLotInfo.setStockerID(objLotDetailInfoGetDROut.getLotLocationInfo().getStockerID());
            qrestLotInfo.setTransferStatus(objLotDetailInfoGetDROut.getLotLocationInfo().getTransferStatus());
            qrestLotInfo.setRouteID(objLotDetailInfoGetDROut.getLotOperationInfo().getRouteID());
            qrestLotInfo.setOperationID(objLotDetailInfoGetDROut.getLotOperationInfo().getOperationID());
            qrestLotInfo.setOperationNumber(objLotDetailInfoGetDROut.getLotOperationInfo().getOperationNumber());
            qrestLotInfo.setEquipmentID(objLotDetailInfoGetDROut.getLotLocationInfo().getEquipmentID());
        }
        return out;
    }

    @Override
    public Results.LotFuturePctrlListInqResult sxLotFuturePctrlListInq(Infos.ObjCommon objCommon, Infos.LotFuturePctrlListInqInParm strLotFuturePctrlListInqInParm){
        Results.LotFuturePctrlListInqResult strLotFuturePctrlListInqResult=new Results.LotFuturePctrlListInqResult();

        Inputs.ObjProcessOperationListForLotIn strProcessOperationListForLotIn=new Inputs.ObjProcessOperationListForLotIn();
        strProcessOperationListForLotIn.setSearchDirectionFlag(true);
        strProcessOperationListForLotIn.setPosSearchFlag(false);
        strProcessOperationListForLotIn.setSearchCount(strLotFuturePctrlListInqInParm.getSearchCount());
        strProcessOperationListForLotIn.setSearchOperationNumber("");
        strProcessOperationListForLotIn.setCurrentFlag(true);
        strProcessOperationListForLotIn.setLotID(strLotFuturePctrlListInqInParm.getLotID());

        // step1 - process_OperationListForLot__160
        List<Infos.OperationNameAttributes> attrSeq = processMethod.processOperationListForLot(objCommon, strProcessOperationListForLotIn);

        // step2 - lot_futureActionInfo_GetDR
        List<Infos.OperationFutureActionAttributes> strLotFutureActionInfoGetDROut = lotMethod.lotFutureActionInfoGetDR(objCommon, attrSeq, strLotFuturePctrlListInqInParm.getStrRequestActionFlagSeq(), strLotFuturePctrlListInqInParm.getLotID());


        strLotFuturePctrlListInqResult.setLotID(strLotFuturePctrlListInqInParm.getLotID());
        strLotFuturePctrlListInqResult.setStrOperationFutureActionAttributes(strLotFutureActionInfoGetDROut);

        return strLotFuturePctrlListInqResult;
    }

    @Override
    public List<ObjectIdentifier> sxQtimeExpiredLotListWithActionPendingInq(Infos.ObjCommon strObjCommonIn, Params.QtimeExpiredLotListWithActionPendingInqInParm strQtimeExpiredLotListWithActionPendingInqInParm) {
        Infos.ExpiredQrestTimeLotListGetDROut strExpiredQrestTimeLotListGetDROut;
        Infos.ExpiredQrestTimeLotListGetDRIn strExpiredQrestTimeLotListGetDRIn=new Infos.ExpiredQrestTimeLotListGetDRIn();
        strExpiredQrestTimeLotListGetDRIn.setMaxRetrieveCount(strQtimeExpiredLotListWithActionPendingInqInParm.getMaxRetrieveCount());
        // step1 - expiredQrestTime_lotList_GetDR
        strExpiredQrestTimeLotListGetDROut = qTimeMethod.expiredQrestTimeLotListGetDR(strObjCommonIn, strExpiredQrestTimeLotListGetDRIn );
        return strExpiredQrestTimeLotListGetDROut.getLotIDs();
    }

    @Override
    public List<Infos.ProcHoldListAttributes> sxProcessHoldListInq(Infos.ObjCommon objCommon, Params.ProcessHoldListInqParams params) {
        return processMethod.processHoldHoldListGetDR(objCommon, params.getSearchKey(), new ObjectIdentifier(), params.getCount(), true, true);
    }

    @Override
    public List<Infos.FutureReworkInfo> sxFutureReworkListInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        //--------------------------------------------------
        // Trace and check input parameter
        //--------------------------------------------------
        if (!ObjectIdentifier.isEmpty(lotID)) {
            log.info("The input parameter lotID {}", lotID);
        } else {
            log.info("The input parameter lotID is not specified.");
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        //--------------------------------------------------
        // Get the list of future rework request
        //--------------------------------------------------
        List<Infos.FutureReworkInfo> futureReworkInfos = lotMethod.lotFutureReworkListGetDR(objCommon, lotID, null, null);
        Validations.check(CimArrayUtils.getSize(futureReworkInfos) < 1, retCodeConfigEx.getFtrwkNotFound());
        log.info("Some future rework requests are found.");

        return futureReworkInfos;
    }
}
