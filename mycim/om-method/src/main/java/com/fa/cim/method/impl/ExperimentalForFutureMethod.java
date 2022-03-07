package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/22          ********            light                create file
 *
 * @author: light
 * @date: 2019/11/22 13:16
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class ExperimentalForFutureMethod implements IExperimentalForFutureMethod{

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IScheduleChangeReservationMethod scheduleChangeReservationMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private ProductManager productManager;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IRunCardMethod runCardMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/26                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/26 10:45
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public void experimentalFutureLotInfoCheck(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber, List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strExperimentalFutureLotDetailInfoSeq) {

        log.info(" experimental_lotInfo_Check");
        //【step0】 Initialize
        com.fa.cim.newcore.bo.pd.CimProcessFlow aPF = null;
        com.fa.cim.newcore.bo.pd.CimProcessFlow aSplitPF = null;
        //【step1】lot_wafersStatusList_GetDR
        List<Infos.WaferListInLotFamilyInfo> lotWaferStatusListDROut = lotMethod.lotWafersStatusListGetDR(objCommon, lotFamilyID);

        Validations.check(CimObjectUtils.isEmpty(lotWaferStatusListDROut), retCodeConfig.getNotFoundWafer());

        List<Infos.WaferListInLotFamilyInfo> familyWaferList = lotWaferStatusListDROut;
        int familyWaferLen = CimArrayUtils.getSize(familyWaferList);
        int targetRtLen = CimArrayUtils.getSize(strExperimentalFutureLotDetailInfoSeq);
        for (int routeCnt = 0; routeCnt < targetRtLen; routeCnt++) {
            int routeWaferLen = CimArrayUtils.getSize(strExperimentalFutureLotDetailInfoSeq.get(routeCnt).getWaferIDs());
            for(int waferCnt = 0;waferCnt < routeWaferLen;waferCnt++){
                int familyWaferCnt = 0;
                for (familyWaferCnt = 0;familyWaferCnt < familyWaferLen;familyWaferCnt++ ){
                    if (CimStringUtils.equals(strExperimentalFutureLotDetailInfoSeq.get(routeCnt).getWaferIDs().get(waferCnt).getWaferID().getValue(),
                            familyWaferList.get(familyWaferCnt).getWaferID().getValue())){

                        if (CimStringUtils.equals(familyWaferList.get(familyWaferCnt).getWaferID().getReferenceKey(), "CHECKED")){
                            log.info("The wafer is specified more than once.",familyWaferList.get(familyWaferCnt).getWaferID().getValue());
                            throw new ServiceException(retCodeConfig.getInvalidInputWafer());

                        }else{
                            familyWaferList.get(familyWaferCnt).getWaferID().setReferenceKey("CHECKED");
                            break;
                        }
                    }
                }
                Validations.check(familyWaferCnt == familyWaferLen, retCodeConfig.getInvalidInputWafer());

            }
        }
        //Gets process flow of original route
        CimProcessDefinition aPD = baseCoreFactory.getBO(CimProcessDefinition.class, originalRouteID);
        Validations.check(CimObjectUtils.isEmpty(aPD),retCodeConfig.getNotFoundProcessDefinition());

        aPF = aPD.getActiveMainProcessFlow();
        Validations.check(aPF == null, retCodeConfig.getNotFoundProcessFlow());

        if (CimBooleanUtils.isTrue(aPF.isNewlyCreated())){
            aPF = aPD.getPreviousActiveMainProcessFlow();

            Validations.check( aPF == null, retCodeConfig.getNotFoundProcessFlow());

        }
        //Gets start/end point of target PSM
        String startPoint = "";
        String endPoint = "";
        if (!ObjectIdentifier.equalsWithValue(splitRouteID, originalRouteID)){
            log.info("Split operation of target PSM is set on sub/rework route.");
            startPoint = originalOperationNumber;
            //===== Checks whether original operation is on original route =======//
            //【step2】process_operationListInRoute_GetDR
            List<Infos.OperationInfo> processOperationListInRouteGetDROut = processMethod.processOperationListInRouteGetDR(objCommon, originalRouteID, originalOperationNumber, BizConstant.SP_MAINPDTYPE_REWORK);
            if (CimArrayUtils.getSize(processOperationListInRouteGetDROut) < 1){
                log.info("Original operation has to be on original route.");
                throw new ServiceException(new OmCode(retCodeConfig.getNotFoundRouteOpe(),
                        originalRouteID.getValue(),originalOperationNumber));
            }
            Infos.OperationInfo strOrgOpeInfo_forTarget = processOperationListInRouteGetDROut.get(0);
            int getRouteCnt_forTarget = 0;
            int getRouteLen_forTarget = CimArrayUtils.getSize(strOrgOpeInfo_forTarget.getConnectedRouteList());
            for (getRouteCnt_forTarget = 0; getRouteCnt_forTarget < getRouteLen_forTarget ; getRouteCnt_forTarget++) {
                if (ObjectIdentifier.equalsWithValue(splitRouteID, strOrgOpeInfo_forTarget.getConnectedRouteList().get(getRouteCnt_forTarget).getRouteID())){
                    endPoint = strOrgOpeInfo_forTarget.getConnectedRouteList().get(getRouteCnt_forTarget).getReturnOperationNumber();
                    break;
                }
            }

            Validations.check(getRouteCnt_forTarget == getRouteLen_forTarget, retCodeConfig.getNotFoundReturnOpe());

            //Gets process flow of split route
            CimProcessDefinition aSplitPD = baseCoreFactory.getBO(CimProcessDefinition.class, splitRouteID);
            Validations.check(CimObjectUtils.isEmpty(aSplitPD),retCodeConfig.getNotFoundProcessDefinition());

            aSplitPF = aSplitPD.getActiveMainProcessFlow();
            Validations.check(null == aSplitPF, retCodeConfig.getNotFoundProcessFlow());

            if (CimBooleanUtils.isTrue(aSplitPF.isNewlyCreated())){
                aSplitPF = aSplitPD.getPreviousActiveProcessFlow();
                Validations.check(aSplitPF == null, retCodeConfig.getNotFoundProcessFlow());
            }
        }else {
            log.info("Split operation of target PSM is set on main route.");
            //Checks whether split operation and original operation are same
            Validations.check(!CimStringUtils.equals(splitOperationNumber,originalOperationNumber), retCodeConfigEx.getFsmOpeInvalid(),originalOperationNumber,splitOperationNumber);

            startPoint = splitOperationNumber;
        }
        //Checks whether split operation is on split route
        //【step3】process_operationListInRoute_GetDR

        List<Infos.OperationInfo> processOperationListInRouteGetDROutForSptOpe = processMethod.processOperationListInRouteGetDR(objCommon, splitRouteID, splitOperationNumber, BizConstant.SP_MAINPDTYPE_BRANCH);
        Validations.check(CimArrayUtils.isEmpty(processOperationListInRouteGetDROutForSptOpe), new OmCode(retCodeConfig.getNotFoundRouteOpe(), ObjectIdentifier.fetchValue(splitRouteID), splitOperationNumber));
        Infos.OperationInfo strSptOpeInfo = processOperationListInRouteGetDROutForSptOpe.get(0);
        int getBranchRtLen_forSptOpe = CimArrayUtils.getSize(strSptOpeInfo.getConnectedRouteList());
        // Gets list of Lot Family's PSM definitions
        //【step4】experimental_lotList_GetDR
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo> strDefList = this.experimentalFutureLotListGetDR(objCommon, ObjectIdentifier.fetchValue(lotFamilyID), "", "", originalRouteID.getValue(), "", true, true);
        //Loop of target PSM's branch route
        for (int i = 0; i < targetRtLen; i++) {
            // log.info("Branch route of target PSM",i,strExperimentalFutureLotDetailInfoSeq.get(i).getSubRouteID().getValue());
            if( CimStringUtils.length(endPoint) < 1){
                endPoint = strExperimentalFutureLotDetailInfoSeq.get(i).getMergeOperationNumber();
            }
            log.info("Start {} and end point {}of target PSM",startPoint,endPoint);
            //Checks branch route's setting
            if(CimBooleanUtils.isTrue(strExperimentalFutureLotDetailInfoSeq.get(i).getDynamicFlag())){
                //【step5】process_checkForDynamicRoute
                Outputs.ObjProcessCheckForDynamicRouteOut processCheckForDynamicRouteOut = processMethod.processCheckForDynamicRoute(objCommon, strExperimentalFutureLotDetailInfoSeq.get(i).getRouteID());
                if (!CimStringUtils.equals(processCheckForDynamicRouteOut.getProcessDefinitionType(),BizConstant.SP_MAINPDTYPE_BRANCH)){
                    log.info("The route type of branch route has to be 'Branch'.");
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidPDType(),
                            processCheckForDynamicRouteOut.getProcessDefinitionType(),strExperimentalFutureLotDetailInfoSeq.get(i).getRouteID().getValue()));

                }else if (CimBooleanUtils.isFalse(processCheckForDynamicRouteOut.getDynamicRouteFlag())){
                    log.info("Branch route has to be the dynamic one.");
                    throw new ServiceException(retCodeConfig.getNotDynamicRoute());
                }else {
                    log.info("Branch route is a dynamic branch route.");
                }
            }else {
                int getBranchRtCnt_forSptOpe = 0;
                for (getBranchRtCnt_forSptOpe = 0; getBranchRtCnt_forSptOpe < getBranchRtLen_forSptOpe; getBranchRtCnt_forSptOpe++) {
                    if (ObjectIdentifier.equalsWithValue(strExperimentalFutureLotDetailInfoSeq.get(i).getRouteID(), strSptOpeInfo.getConnectedRouteList().get(getBranchRtCnt_forSptOpe).getRouteID())){
                        log.info("Branch route is related to split operation.");
                        break;
                    }
                }
                if (getBranchRtCnt_forSptOpe == getBranchRtLen_forSptOpe){
                    log.info("Branch route has to be related to split operation.");
                    // task-3988 The sub-process parameter can be null if the main process is batched
                    // task-3988 this is a main flow if SubRouteID and ReturnOperationNumber is blank
                    if (ObjectIdentifier.isNotEmpty(strExperimentalFutureLotDetailInfoSeq.get(i).getRouteID()) && CimStringUtils.isNotEmpty(strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber())){
                        throw new ServiceException(retCodeConfig.getNotFoundExperimentalLotSubroute());
                    }
                }else if (!CimStringUtils.equals(strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber(),strSptOpeInfo.getConnectedRouteList().get(getBranchRtCnt_forSptOpe).getReturnOperationNumber())){
                    log.info("Return operation has to be same as the setting of SM.");
                    // task-3988 this is a main flow if SubRouteID and ReturnOperationNumber is blank
                    if (ObjectIdentifier.isNotEmpty(strExperimentalFutureLotDetailInfoSeq.get(i).getRouteID()) && CimStringUtils.isNotEmpty(strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber())) {
                        throw new ServiceException(new OmCode(retCodeConfig.getPsmOpeInvalid(), strSptOpeInfo.getConnectedRouteList().get(getBranchRtCnt_forSptOpe).getReturnOperationNumber(), strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber()));
                    }
                }else {
                    log.info("Return operation is same as the setting of SM.");
                }
            }
            //Checks whether merge operation is on split route
            //【step6】process_operationListInRoute_GetDR
            List<Infos.OperationInfo> processOperationListInRouteGetDROutForMrgOpe = processMethod.processOperationListInRouteGetDR(objCommon, splitRouteID, strExperimentalFutureLotDetailInfoSeq.get(i).getMergeOperationNumber(), "");
            if (CimObjectUtils.isEmpty(processOperationListInRouteGetDROutForMrgOpe)){
                throw new ServiceException(retCodeConfig.getInvalidExperimentalLotMergePoint());
            }
            //Checks the relation between merge operation and split operation
            Boolean toSplit =false;
            if (aSplitPF == null){
                toSplit = aPF.isAfterOperationNumberForMain(null, strExperimentalFutureLotDetailInfoSeq.get(i).getMergeOperationNumber(), splitOperationNumber);
            }else{
                toSplit = aSplitPF.isAfterOperationNumberForMain(null,strExperimentalFutureLotDetailInfoSeq.get(i).getMergeOperationNumber(),splitOperationNumber);
            }
            Validations.check(CimBooleanUtils.isFalse(toSplit), retCodeConfig.getInvalidExperimentalLotMergePoint());

            //Checks whether return operation is on split route
            //【step7】process_operationListInRoute_GetDR
            List<Infos.OperationInfo> processOperationListInRouteGetDRForRtnOpe = processMethod.processOperationListInRouteGetDR(objCommon, splitRouteID, strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber(), "");
            if (CimObjectUtils.isEmpty(processOperationListInRouteGetDRForRtnOpe)){
                log.info("Return operation has to be on split route.");
                throw new ServiceException(new OmCode(retCodeConfig.getNotFoundRouteOpe(),
                        splitRouteID.getValue(),strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber()));
            }
            //Checks the relation between merge operation and return operation

            // task-3988 this is a main flow if SubRouteID and ReturnOperationNumber is blank
            Boolean toReturn = false;
            if (ObjectIdentifier.isNotEmpty(strExperimentalFutureLotDetailInfoSeq.get(i).getRouteID()) && CimStringUtils.isNotEmpty(strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber())){
                if (aSplitPF == null){
                    toReturn =  aPF.isAfterOperationNumberForMain(null,strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber(),strExperimentalFutureLotDetailInfoSeq.get(i).getMergeOperationNumber());
                }else {
                    toReturn = aSplitPF.isAfterOperationNumberForMain(null, strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber(),strExperimentalFutureLotDetailInfoSeq.get(i).getMergeOperationNumber());
                }
            }

            Validations.check( CimBooleanUtils.isTrue(toReturn), retCodeConfig.getInvalidExperimentalLotMergePoint());

            //Checks futurehold and schedulechange between merge operation and split operation
            int waferLen = 0;
            waferLen = CimArrayUtils.getSize(strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs());
            Validations.check(waferLen < 1, retCodeConfig.getNotFoundWafer());

            //Get Lot from Wafer
            //【step8】wafer_lot_Get

            //task-3899 The structure of the waferlist has been changed
            ObjectIdentifier waferLotGetOut = waferMethod.waferLotGet(objCommon, strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs().get(0).getWaferID());
            log.info("Check LotID",waferLotGetOut.getValue());
            Boolean isAfterFlag = false;
            Boolean returnCheckFlag = true;
            if (aSplitPF == null){
                isAfterFlag = aPF.isAfterOperationNumberForMain(null, strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber(),splitOperationNumber);
            }else {
                isAfterFlag = aSplitPF.isAfterOperationNumberForMain(null, strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber(),splitOperationNumber);
            }
            if (CimBooleanUtils.isFalse(isAfterFlag)){
                log.info("returnOperationNumber is before splitOperationNumber");
                returnCheckFlag = false;
            }
            com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, waferLotGetOut);
            Validations.check(CimObjectUtils.isEmpty(aLot),retCodeConfig.getNotFoundLot());

            CimProcessDefinition aSplitPD = baseCoreFactory.getBO(CimProcessDefinition.class, splitRouteID);
            Validations.check(CimObjectUtils.isEmpty(aSplitPD),retCodeConfig.getNotFoundProcessDefinition());
            //【step9】 process_operationListForRoute__160
            Inputs.ProcessOperationListForRoute processOperationListForRouteIn = new Inputs.ProcessOperationListForRoute();
            processOperationListForRouteIn.setRouteID(splitRouteID);
            processOperationListForRouteIn.setOperationID(new ObjectIdentifier(""));
            processOperationListForRouteIn.setOperationNumber("");
            processOperationListForRouteIn.setPdType("");
            processOperationListForRouteIn.setSearchCount(-1);
            List<Infos.OperationNameAttributes> processOperationListForRouteOut = processMethod.processOperationListForRoute(objCommon, processOperationListForRouteIn);
            int opeLen = 0;
            int opeCnt = 0;
            int checkCnt = 0;
            Boolean checkStartFalg = false;
            opeLen = processOperationListForRouteOut.size();
            log.info("opeLen=",opeLen);
            for (opeCnt = 0; opeCnt < opeLen; opeCnt++) {
                if (CimStringUtils.equals(processOperationListForRouteOut.get(opeCnt).getOperationNumber(), splitOperationNumber)){
                    checkStartFalg = true;
                }
                if (CimBooleanUtils.isFalse(checkStartFalg)){
                    continue;
                }
                //Checks futurehold between merge operation and split operation
                log.info("Check OperatoinNumber",processOperationListForRouteOut.get(opeCnt).getOperationNumber());
                if (CimStringUtils.equals(processOperationListForRouteOut.get(opeCnt).getOperationNumber(), strExperimentalFutureLotDetailInfoSeq.get(i).getMergeOperationNumber())){
                    log.info("FutureHold Check End!",processOperationListForRouteOut.get(opeCnt).getOperationNumber(),strExperimentalFutureLotDetailInfoSeq.get(i).getMergeOperationNumber());
                    break;
                }
                if (CimStringUtils.equals(processOperationListForRouteOut.get(opeCnt).getOperationNumber(), strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber())){
                    log.info("CIMFWStrCmp(strProcess_operationListForRoute_out.strOperationNameAttributes[opeCnt].operationNumber, strExperimentalLotDetailInfoSeq[m].returnOperationNumber) == 0");
                    returnCheckFlag = false;
                }
                List<ProductDTO.FutureHoldRecord> aFutureHoldRecordSequence;
                if (checkCnt == 0){
                    aFutureHoldRecordSequence = aLot.findFutureHoldRecordsFor(aSplitPD, processOperationListForRouteOut.get(opeCnt).getOperationNumber());
                    int futureHoldLen = 0;
                    futureHoldLen = CimArrayUtils.getSize(aFutureHoldRecordSequence);
                    log.info("futureHoldLen",futureHoldLen);
                    Boolean postFutureHoldFlag = false;
                    for (int futureHoldCnt = 0;futureHoldCnt < futureHoldLen; futureHoldCnt++){
                        if (CimBooleanUtils.isTrue(aFutureHoldRecordSequence.get(futureHoldCnt).isPostFlag())){
                            log.info("postFlag == TRUE",futureHoldCnt);
                            postFutureHoldFlag = true;
                        }
                    }
                    if (CimBooleanUtils.isTrue(postFutureHoldFlag) && CimBooleanUtils.isTrue(returnCheckFlag)) {
                        throw new ServiceException(new OmCode(retCodeConfig.getNotFutureholdInSplit(),aLot.getLotID().getValue(),aFutureHoldRecordSequence.get(0).getMainProcessDefinition().getValue(),aFutureHoldRecordSequence.get(0).getOperationNumber()));
                    }
                }else {
                    aFutureHoldRecordSequence = aLot.findFutureHoldRecordsFor(aSplitPD,processOperationListForRouteOut.get(opeCnt).getOperationNumber());
                    if (CimArrayUtils.getSize(aFutureHoldRecordSequence) != 0){
                        log.info("aFutureHoldRecords->length() != 0");
                        if (CimBooleanUtils.isTrue(returnCheckFlag)){
                            throw new ServiceException(new OmCode(retCodeConfig.getNotFutureholdInSplit(),aLot.getLotID().getValue(),aFutureHoldRecordSequence.get(0).getMainProcessDefinition().getValue(),aFutureHoldRecordSequence.get(0).getOperationNumber()));
                        }
                        int futureHoldLen = 0;
                        futureHoldLen = CimArrayUtils.getSize(aFutureHoldRecordSequence);
                        for (int futureHoldCnt = 0; futureHoldCnt < futureHoldLen;futureHoldCnt++){
                            if (CimStringUtils.equals(aFutureHoldRecordSequence.get(futureHoldCnt).getHoldType(), BizConstant.SP_HOLDTYPE_MERGEHOLD) &&
                                    CimStringUtils.equals(aFutureHoldRecordSequence.get(futureHoldCnt).getHoldType(), BizConstant.SP_HOLDTYPE_REWORKHOLD)){
                                Lot aParentLot = aLot.mostRecentlySplitFrom();
                                if (null == aParentLot){
                                    log.info("aParentLot is NIL");
                                    continue;
                                }
                                //if this lot is child lot, that is, it will be merged into parentLot, return error
                                String aParentLotID = aParentLot.getIdentifier();
                                Validations.check(CimStringUtils.equals(aParentLotID, aFutureHoldRecordSequence.get(futureHoldCnt).getRelatedLot().getValue()),
                                        retCodeConfig.getMergedBeforeMergepoint(),
                                        waferLotGetOut,
                                        aFutureHoldRecordSequence.get(futureHoldCnt).getRelatedLot(),
                                        splitRouteID,
                                        processOperationListForRouteOut.get(opeCnt).getOperationName());

                            }
                        }
                    }
                }
                // Checks schedulechage between merge operation and split operation
                //【step10】 schdlChangeReservation_CheckForActionDR__110
                log.info("Call schdlChangeReservation_CheckForActionDR");
                Inputs.ObjSchdlChangeReservationCheckForActionDRIn objSchdlChangeReservationCheckForActionDRIn = new Inputs.ObjSchdlChangeReservationCheckForActionDRIn();
                objSchdlChangeReservationCheckForActionDRIn.setLotID(waferLotGetOut);
                objSchdlChangeReservationCheckForActionDRIn.setRouteID(splitRouteID.getValue());
                objSchdlChangeReservationCheckForActionDRIn.setOperationNumber(processOperationListForRouteOut.get(opeCnt).getOperationNumber());
                Outputs.ObjSchdlChangeReservationCheckForActionDROut objSchdlChangeReservationCheckForActionDROut = scheduleChangeReservationMethod.schdlChangeReservationCheckForActionDR(objCommon, objSchdlChangeReservationCheckForActionDRIn);
                Validations.check(CimBooleanUtils.isTrue(objSchdlChangeReservationCheckForActionDROut.isExistFlag()), retCodeConfig.getSchdresvExistedFutureOperation(), waferLotGetOut);
                checkCnt ++;
            }
            // Loop of another PSM list
            int anotherDefLen = CimArrayUtils.getSize(strDefList);
            for (int j = 0; j < anotherDefLen; j++) {
                //Skips the check for target PSM itself
                if (ObjectIdentifier.equalsWithValue(splitRouteID, strDefList.get(j).getSplitRouteID()) &&
                        CimStringUtils.equals(splitOperationNumber, strDefList.get(j).getSplitOperationNumber()) &&
                        CimStringUtils.equals(originalOperationNumber, strDefList.get(j).getOriginalOperationNumber())){
                    log.info("Target PSM already exist, it is updated.");
                    continue;
                }
                //Gets start/end point of PSM
                String anotherStartPoint = "";
                String anotherEndPoint = "";
                if (!ObjectIdentifier.equalsWithValue(strDefList.get(j).getSplitRouteID(), strDefList.get(j).getOriginalRouteID())){
                    log.info("Split operation of another PSM is set on sub/rework route.");
                    anotherStartPoint = strDefList.get(j).getOriginalOperationNumber();
                    //【step】process_operationListInRoute_GetDR
                    List<Infos.OperationInfo> processOperationListInRouteGetDROut = processMethod.processOperationListInRouteGetDR(objCommon, strDefList.get(j).getOriginalRouteID(), strDefList.get(j).getOriginalOperationNumber(), BizConstant.SP_MAINPDTYPE_REWORK);
                    if (CimArrayUtils.getSize(processOperationListInRouteGetDROut)< 1){
                        log.info("Original operation has to be on original route.");
                        throw new ServiceException(new OmCode(retCodeConfig.getNotFoundRouteOpe(), strDefList.get(j).getOriginalRouteID().getValue(), strDefList.get(i).getOriginalOperationNumber()));
                    }
                    Infos.OperationInfo strOrgOpeInfoForAnother = processOperationListInRouteGetDROut.get(0);
                    int getRouteCnt_forAnother = 0;
                    int getRouteLen_forAnother = CimArrayUtils.getSize(strOrgOpeInfoForAnother.getConnectedRouteList());
                    for (getRouteCnt_forAnother = 0; getRouteCnt_forAnother < getRouteLen_forAnother; getRouteCnt_forAnother++) {
                        if (ObjectIdentifier.equalsWithValue(strDefList.get(j).getSplitRouteID(), strOrgOpeInfoForAnother.getConnectedRouteList().get(getRouteCnt_forAnother).getRouteID())){
                            anotherEndPoint = strOrgOpeInfoForAnother.getConnectedRouteList().get(getRouteCnt_forAnother).getReturnOperationNumber();
                            break;
                        }
                    }
                    if (getRouteCnt_forAnother == getRouteLen_forAnother){
                        log.info("Return operation of another PSM's split route is not found.");
                        continue;
                    }
                }else {
                    log.info("Split operation of another PSM is set on main route.");
                    anotherStartPoint =strDefList.get(j).getSplitOperationNumber();
                }
                Boolean accordFlag = false;
                Boolean addLevel1PSMFlag = false;
                //Loop of another PSM's branch route
                int anotherRtlen = CimArrayUtils.getSize(strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq());
                for (int k = 0; k < anotherRtlen; k++) {
                    log.info("Branch route of another PSM",j,k,strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getRouteID().getValue());
                    //Checks whether all repetitive wafers are specified by the beginning PSM
                    if (CimStringUtils.equals(startPoint, anotherStartPoint) &&
                            !ObjectIdentifier.equalsWithValue(splitRouteID, strDefList.get(j).getSplitRouteID())){
                        //The list of wafers which is specified by the repetitive PSM and the beginning PSM
                        List<com.fa.cim.fsm.Infos.Wafer> repetitiveWaferList = new ArrayList<>();
                        List<com.fa.cim.fsm.Infos.Wafer> beginningWaferList = new ArrayList<>();
                        //Target PSM is the one that is defined repetitively
                        if (ObjectIdentifier.equalsWithValue(splitRouteID, strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getRouteID())){
                            log.info("Target PSM has repetition with another PSM.");
                            repetitiveWaferList = strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs();
                            beginningWaferList = strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getWaferIDs();
                        } else if (ObjectIdentifier.equalsWithValue(strExperimentalFutureLotDetailInfoSeq.get(i).getRouteID(), strDefList.get(j).getSplitRouteID())){
                            //Target PSM is the one that is defined as the beginning
                            log.info("Another PSM has repetition with target PSM.");
                            repetitiveWaferList = strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getWaferIDs();
                            beginningWaferList = strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs();
                            addLevel1PSMFlag = true;
                        }else {
                            //Target PSM is neither the repetitive PSM nor the beginning PSM
                            log.info("Target PSM and another PSM don't have repetition to each other.");
                            int xWaferLen = CimArrayUtils.getSize(strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs());
                            int yWaferLen = CimArrayUtils.getSize(strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getWaferIDs());
                            for (int x = 0;x < xWaferLen; x++){
                                for (int y = 0; y < yWaferLen; y++) {
                                    Validations.check(CimStringUtils.equals(strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs().get(x).getWaferID().getValue(), strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getWaferIDs().get(y).getWaferID().getValue()), retCodeConfig.getPsmWaferInvalid());
                                }
                            }
                            accordFlag = true;
                            continue;
                        }
                        int accordCnt = 0;
                        int repWafLen = CimArrayUtils.getSize(repetitiveWaferList);
                        int begWafLen = CimArrayUtils.getSize(beginningWaferList);
                        for (int repWafCnt = 0; repWafCnt < repWafLen; repWafCnt++) {
                            log.info("The wafer of the repetitive PSM",begWafLen,repWafCnt,repetitiveWaferList.get(repWafCnt).getWaferID().getValue());
                            for (int begWafCnt = 0; begWafCnt < begWafLen; begWafCnt++) {
                                log.info("The wafer of the beginning PSM",begWafLen,begWafCnt,beginningWaferList.get(begWafCnt).getWaferID().getValue());
                                if (CimStringUtils.equals(repetitiveWaferList.get(repWafCnt).getWaferID().getValue(), beginningWaferList.get(begWafCnt).getWaferID().getValue())){
                                    log.info("The wafer is specified repetitively.");
                                    accordCnt++;
                                    break;
                                }
                            }
                        }
                        if( accordCnt == 0){
                            log.info("Any wafers of the repetitive PSM is not specified by the beginning PSM.");
                        }else if (accordCnt != repWafLen){
                            log.info("Only a part of the repetitive PSM's wafer is not specified by the beginning PSM.");
                            throw new ServiceException(retCodeConfig.getPsmWaferInvalid());
                        }else {
                            log.info("All wafers of the repetitive PSM is specified by the beginning PSM.");
                            accordFlag = true;
                        }
                        continue;
                    }
                    accordFlag = true;
                    if (CimObjectUtils.isEmpty(anotherEndPoint)){
                        anotherEndPoint = strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getMergeOperationNumber();
                    }
                    log.info("Start and end point of another PSM",anotherStartPoint,anotherEndPoint);
                    //Loop of target PSM's wafers
                    int targetWfLen = CimArrayUtils.getSize(strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs());
                    for (int n = 0; n < targetWfLen; n++) {
                        log.info("Wafer of target PSM",i,n,strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs().get(n).getWaferID().getValue());
                        //Loop of another PSM's wafers
                        int anotherWfLen = CimArrayUtils.getSize(strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getWaferIDs());
                        for (int m = 0; m < anotherWfLen; m++) {
                            log.info("Wafer of another PSM",j,k,m,strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getWaferIDs().get(m).getWaferID().getValue());
                            //Skips the check for PSM, to which the wafer concerned is not set
                            if (!CimStringUtils.equals(strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs().get(n).getWaferID().getValue(),
                                    strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getWaferIDs().get(m).getWaferID().getValue())){
                                log.info("The wafer doesn't overlap, so consistency between definitions is not checked.");
                                log.info(strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getWaferIDs().get(m).getWaferID().getValue());
                                continue;
                            }
                            if (aSplitPF != null
                                    && CimStringUtils.equals(splitRouteID.getValue(), strDefList.get(j).getSplitRouteID().getValue())
                                    && CimStringUtils.equals(originalOperationNumber, strDefList.get(j).getOriginalOperationNumber())){
                                Boolean  checkSplitSplit = false;
                                Boolean  checkSplitMerge = false;
                                Boolean  checkMergeSplit = false;
                                checkSplitSplit = aSplitPF.isAfterOperationNumberForMain(null,strDefList.get(j).getSplitOperationNumber(),splitOperationNumber);
                                checkSplitMerge = aSplitPF.isAfterOperationNumberForMain(null,strDefList.get(j).getSplitOperationNumber(),strExperimentalFutureLotDetailInfoSeq.get(i).getMergeOperationNumber());
                                checkMergeSplit = aSplitPF.isAfterOperationNumberForMain(null,splitOperationNumber,strDefList.get(j).getStrExperimentalFutureLotDetailInfoSeq().get(k).getMergeOperationNumber());
                                Validations.check(( CimBooleanUtils.isFalse(checkSplitSplit)
                                        || CimBooleanUtils.isFalse(checkSplitMerge))
                                        && CimBooleanUtils.isFalse(checkMergeSplit), retCodeConfig.getInvalidExperimentalLotMergePoint());

                            }else {
                                Boolean  checkStartStart = false;
                                Boolean  checkStartEnd   = false;
                                Boolean  checkEndStart   = false;
                                checkStartStart = aPF.isAfterOperationNumberForMain(null,anotherStartPoint,startPoint);
                                checkStartEnd = aPF.isAfterOperationNumberForMain(null,anotherStartPoint,endPoint);
                                checkEndStart = aPF.isAfterOperationNumberForMain(null,startPoint,anotherEndPoint);
                                Validations.check((CimBooleanUtils.isFalse(checkStartStart)
                                        || CimBooleanUtils.isFalse(checkStartEnd))
                                        && CimBooleanUtils.isFalse(checkEndStart),retCodeConfig.getInvalidExperimentalLotMergePoint());

                            }
                            log.info("The consistency was checked.");
                            break;
                        }
                    }
                }
                Validations.check(CimBooleanUtils.isFalse(accordFlag) && CimBooleanUtils.isFalse(addLevel1PSMFlag), retCodeConfig.getPsmWaferInvalid());

            }
        }

    }
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/26                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/26 15:57
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public String  experimentalFutureLotInfoUpdate(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber, Boolean actionEMail, Boolean actionHold, String testMemo, Boolean execFlag
            , String actionTimeStamp, String modifyTimeStamp, String modifyUserID, List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strExperimentalFutureLotDetailInfoSeq,Boolean actionSeparateHold, Boolean actionCombineHold) {

        //【step1】Init
        //【step2】Insert or Update the specified indications to DB.
        int lenDetailInfoSeq = CimArrayUtils.getSize(strExperimentalFutureLotDetailInfoSeq);
        log.info("lenDetailInfoSeq", lenDetailInfoSeq);
        ProductDTO.FutureSplitJobInfo aFutureSplitJobInfo = new ProductDTO.FutureSplitJobInfo();

        //【step3】Edit the contents of DB.
        if (!ObjectIdentifier.isEmptyWithRefKey(lotFamilyID)) {
            log.info("lotFamilyID.stringifiedObjectReference is not blank");
            aFutureSplitJobInfo.setLotFamilyID(lotFamilyID);
        } else {
            log.info("lotFamilyID.stringifiedObjectReference is blank");
            com.fa.cim.newcore.bo.product.CimLotFamily lotFamily = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLotFamily.class, lotFamilyID);
            Validations.check(CimObjectUtils.isEmpty(lotFamily), retCodeConfig.getNotFoundLotFamily());
            ObjectIdentifier aLotFamilyOI = ObjectIdentifier.build(lotFamily.getIdentifier(),
                    lotFamily.getPrimaryKey());
            aFutureSplitJobInfo.setLotFamilyID(aLotFamilyOI);
        }

        if (!ObjectIdentifier.isEmptyWithRefKey(splitRouteID)) {
            log.info("splitRouteID.stringifiedObjectReference is not blank");
            aFutureSplitJobInfo.setSplitRouteID(splitRouteID);
        } else {
            log.info("splitRouteID.stringifiedObjectReference is blank");
            CimProcessDefinition aSplitRoute = baseCoreFactory.getBO(CimProcessDefinition.class, splitRouteID);
            Validations.check(CimObjectUtils.isEmpty(aSplitRoute), retCodeConfig.getNotFoundProcessDefinition());
            ObjectIdentifier aSplitRouteOI = ObjectIdentifier.build(aSplitRoute.getIdentifier(),
                    aSplitRoute.getPrimaryKey());
            aFutureSplitJobInfo.setSplitRouteID(aSplitRouteOI);
        }
        aFutureSplitJobInfo.setSplitOperationNumber(splitOperationNumber);

        if (!ObjectIdentifier.isEmptyWithRefKey(originalRouteID)) {
            log.info("originalRouteID.stringifiedObjectReference is not blank");
            aFutureSplitJobInfo.setOriginalRouteID(originalRouteID);
        } else {
            log.info("originalRouteID.stringifiedObjectReference is blank");
            CimProcessDefinition anOriginalRoute = baseCoreFactory.getBO(CimProcessDefinition.class, originalRouteID);
            Validations.check(CimObjectUtils.isEmpty(anOriginalRoute), retCodeConfig.getNotFoundProcessDefinition());
            ObjectIdentifier anOriginalRouteOI = ObjectIdentifier.build(anOriginalRoute.getIdentifier(),
                    anOriginalRoute.getPrimaryKey());
            aFutureSplitJobInfo.setOriginalRouteID(anOriginalRouteOI);
        }
        aFutureSplitJobInfo.setOriginalOperationNumber(originalOperationNumber);
        aFutureSplitJobInfo.setActionEMail(actionEMail);
        aFutureSplitJobInfo.setActionHold(actionHold);
        //2020/09/01   add Input param actionSeparateHold/actionCombineHold support for auto separate and combine  - jerry
        aFutureSplitJobInfo.setActionSeparateHold(actionSeparateHold);
        aFutureSplitJobInfo.setActionCombineHold(actionCombineHold);
        aFutureSplitJobInfo.setTestMemo(testMemo);
        aFutureSplitJobInfo.setExecutedFlag(execFlag);
        aFutureSplitJobInfo.setExecutedTimeStamp(CimDateUtils.convertToOrInitialTime(actionTimeStamp));
        aFutureSplitJobInfo.setLastClaimedTimeStamp(CimDateUtils.convertToOrInitialTime(modifyTimeStamp));
        com.fa.cim.newcore.bo.person.CimPerson aModifyUser = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, new ObjectIdentifier(modifyUserID));
        Validations.check(CimObjectUtils.isEmpty(aModifyUser), retCodeConfig.getNotFoundPerson());
        ObjectIdentifier aModifyUserOI = ObjectIdentifier.build(aModifyUser.getIdentifier(),
                aModifyUser.getPrimaryKey());
        aFutureSplitJobInfo.setModifier(aModifyUserOI);
        List<ProductDTO.FutureSplitJobInfoDetail> futureSplitJobInfoDetails = new ArrayList<>();
        for (int i = 0; i < lenDetailInfoSeq; i++) {
            ProductDTO.FutureSplitJobInfoDetail futureSplitJobInfoDetail = new ProductDTO.FutureSplitJobInfoDetail();
            if (!ObjectIdentifier.isEmptyWithRefKey(strExperimentalFutureLotDetailInfoSeq.get(i).getRouteID())) {
                log.info("subRouteID.stringifiedObjectReference is not blank");
                futureSplitJobInfoDetail.setRouteID(strExperimentalFutureLotDetailInfoSeq.get(i).getRouteID());
            } else {
                log.info("subRouteID.stringifiedObjectReference is blank");
                // task-3988 if SubRouteID and ReturnOperationNumber is blank,needn`t to validation it`s a main flow
                if (CimStringUtils.isNotEmpty(strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber()) && ObjectIdentifier.isNotEmpty(strExperimentalFutureLotDetailInfoSeq.get(i).getRouteID())){
                    CimProcessDefinition anSubRoute = baseCoreFactory.getBO(CimProcessDefinition.class, strExperimentalFutureLotDetailInfoSeq.get(i).getRouteID());
                    Validations.check(CimObjectUtils.isEmpty(anSubRoute), retCodeConfig.getNotFoundProcessDefinition());
                    ObjectIdentifier anSubRouteOI = ObjectIdentifier.build(anSubRoute.getIdentifier(),
                            anSubRoute.getPrimaryKey());
                    futureSplitJobInfoDetail.setRouteID(anSubRouteOI);
                }
            }
            futureSplitJobInfoDetail.setReturnOperationNumber(strExperimentalFutureLotDetailInfoSeq.get(i).getReturnOperationNumber());
            futureSplitJobInfoDetail.setMergeOperationNumber(strExperimentalFutureLotDetailInfoSeq.get(i).getMergeOperationNumber());
            futureSplitJobInfoDetail.setMemo(strExperimentalFutureLotDetailInfoSeq.get(i).getMemo());

            int waferLen = CimArrayUtils.getSize(strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs());
            List<ProductDTO.Wafer> wafers = new ArrayList<>();
            ObjectIdentifier objectIdentifier = new ObjectIdentifier();
            for (int waferCnt = 0; waferCnt < waferLen; waferCnt++) {
                log.info("waferCnt/waferLen", waferCnt, waferLen);
                if (!ObjectIdentifier.isEmptyWithRefKey(strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs().get(waferCnt).getWaferID())) {
                    log.info("waferID.stringifiedObjectReference is not blank");
                    objectIdentifier = strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs().get(waferCnt).getWaferID();
                } else {
                    log.info("waferID.stringifiedObjectReference is blank");
                    com.fa.cim.newcore.bo.product.CimWafer aWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs().get(i).getWaferID());
                    Validations.check(CimObjectUtils.isEmpty(aWafer), retCodeConfig.getNotFoundWafer());
                    objectIdentifier = ObjectIdentifier.build(aWafer.getIdentifier(), aWafer.getPrimaryKey());
                }
                ProductDTO.Wafer wafer = new ProductDTO.Wafer();
                String groupID = null;
                if (!CimStringUtils.isEmpty(strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs().get(waferCnt).getGroupNo())){
                    groupID = strExperimentalFutureLotDetailInfoSeq.get(i).getWaferIDs().get(waferCnt).getGroupNo();
                }
                wafer.setGroupNo(groupID);
                wafer.setWaferID(objectIdentifier);
                wafers.add(wafer);
            }
            futureSplitJobInfoDetail.setWafers(wafers);
            futureSplitJobInfoDetail.setDynamicFlag(strExperimentalFutureLotDetailInfoSeq.get(i).getDynamicFlag());
            futureSplitJobInfoDetail.setExecutedFlag(strExperimentalFutureLotDetailInfoSeq.get(i).getExecFlag());
            futureSplitJobInfoDetail.setExecutedTimeStamp(CimDateUtils.convertToOrInitialTime(strExperimentalFutureLotDetailInfoSeq.get(i).getActionTimeStamp()));
            futureSplitJobInfoDetails.add(futureSplitJobInfoDetail);
        }
        aFutureSplitJobInfo.setFutureSplitJobInfoDetails(futureSplitJobInfoDetails);
        //Find Experimental Lot Info
        com.fa.cim.newcore.bo.product.CimFutureSplitJob aFutureSplitJob = productManager.findFutureSplitJobFor(lotFamilyID.getValue(), splitRouteID.getValue(), splitOperationNumber, originalRouteID.getValue(), originalOperationNumber);
        Boolean createFlag = false;
        Boolean modifyFlag = false;
        Boolean removeFlag = false;
        if (aFutureSplitJob == null) {
            // When the record does not exist, the record should be created.
            createFlag = true;
            aFutureSplitJob = productManager.createFutureSplitJob(aFutureSplitJobInfo);
        } else {
            //When the record exists, the record should be updated.
            if (CimArrayUtils.getSize(futureSplitJobInfoDetails) == CimArrayUtils.getSize(aFutureSplitJob.getFutureSplitJobInfoDetails())) {
                //modify request updateReq
                modifyFlag = true;
            } else {
//                if (StringUtils.isNotEmpty(removePsmKey)) {
                    removeFlag = true;
//                }
            }
            aFutureSplitJob.setFutureSplitJobInfo(aFutureSplitJobInfo);
        }
        return aFutureSplitJob.getIdentifier();
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/30                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/30 13:28
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public void experimentalFutureLotStateCheck(Infos.ObjCommon objCommon, ObjectIdentifier lotId) {

        //Initialize
        log.info("in para lotID,{}",lotId);

        //Get Lot Object
        com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotId);
        Validations.check(CimObjectUtils.isEmpty(aLot),retCodeConfig.getNotFoundLot());
        //Get holdState
        String holdState = null;
        holdState = aLot.getLotHoldState();

        //Get processState
        String processState = null;
        processState = aLot.getLotProcessState();

        // Get inventoryState
        String inventoryState = null;
        inventoryState = aLot.getLotInventoryState();

        //Get Backup State
        Boolean backupProcessingFlag = false;
        backupProcessingFlag = aLot.isBackupProcessingFlagOn();

        //Check status
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD, holdState),
                retCodeConfig.getInvalidLotHoldStat(),lotId,holdState );

        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_WAITING, processState),
                retCodeConfig.getInvalidLotProcstat(),lotId,processState);

        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, inventoryState),
                retCodeConfig.getInvalidLotInventoryStat(),lotId,inventoryState);

        Validations.check(CimBooleanUtils.isTrue(backupProcessingFlag), retCodeConfig.getLotOnBackupOperation());

    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/30                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/30 14:03
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public com.fa.cim.fsm.Outputs.ObjExperimentalFutureLotInfoGetOut experimentalFutureLotInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotId) {
        //Initialize
        com.fa.cim.fsm.Outputs.ObjExperimentalFutureLotInfoGetOut out = new com.fa.cim.fsm.Outputs.ObjExperimentalFutureLotInfoGetOut();

        //Get key items of PSM definition
        String lotFamily = null;
        String splitRoute = null;
        String splitOpeNo = null;
        String origRoute = null;
        String origOpeNo = null;
        com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotId);
        Validations.check(CimObjectUtils.isEmpty(aLot),retCodeConfig.getNotFoundLot());
        // Lot Family
        com.fa.cim.newcore.bo.product.CimLotFamily aLotFamily = aLot.getLotFamily();
        Validations.check(CimObjectUtils.isEmpty(aLotFamily), retCodeConfig.getNotFoundLotFamily());

        lotFamily = aLotFamily.getIdentifier();

        //Split Route
        CimProcessDefinition aMainPD = aLot.getMainProcessDefinition();
        Validations.check(aMainPD == null, retCodeConfig.getNotFoundMainRoute());

        splitRoute = aMainPD.getIdentifier();

        //Split Operation Number
        splitOpeNo = aLot.getOperationNumber();

        //Original Route & Operation Number
        com.fa.cim.newcore.bo.pd.CimProcessFlowContext aPFX = aLot.getProcessFlowContext();
        Validations.check(aPFX == null, retCodeConfig.getNotFoundPfx());

        List<ProcessDTO.BackupOperation> backupPOList = aPFX.allBackupOperations();
        if (CimArrayUtils.getSize(backupPOList) > 1){
            log.info("The lot is branched/reworked more than twice, so PSM is not defined.");
            throw new ServiceException(retCodeConfig.getNotFoundExperimentalLotData());
        }else if (CimArrayUtils.getSize(backupPOList) > 0){
            if (CimStringUtils.length(backupPOList.get(0).getReworkOutKey()) > 0){
                log.info("The lot is reworked once.");
                //===== Divide reworkOutKey to RouteID and OpeNo =======//
                //reworkOutKey = "MainPD ID" + '.' + "MainPD Version" + '.' + "Module No" + '.' + "Module OpeNo"
                String reworkOutKeyTmp = null;
                reworkOutKeyTmp = backupPOList.get(0).getReworkOutKey();
                int periodCnt = 0;
                int index = CimStringUtils.length(reworkOutKeyTmp) - 1;
                for (index = CimStringUtils.length(reworkOutKeyTmp) - 1; index >= 0; index--){
                    char[] chars = reworkOutKeyTmp.toCharArray();
                    if (chars[index] == '.'){
                        periodCnt++;
                    }
                    if (periodCnt == 2){
                        break;
                    }
                }
                if (periodCnt == 2){
                    index++;       //rewind one char for last refPos--
                    index = '\0'; //replace '.' to '\0'
                    index++;       //point next char
                    log.info("  string reworkOutKeyTmp {}", reworkOutKeyTmp);
                    log.info("  string index {}", index);

                    origRoute = reworkOutKeyTmp;
                    origOpeNo = String.valueOf(index);
                }else{
                    log.info("The original route and ope cannot be created from reworkOutKey. {}", reworkOutKeyTmp);
                    throw new ServiceException(retCodeConfig.getNotFoundExperimentalLotData());

                }
            }else{
                log.info( "The lot is branched once.");
                com.fa.cim.newcore.bo.pd.CimProcessOperation aBackupPo = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessOperation.class, backupPOList.get(0).getProcessOperation());
                if (null == aBackupPo) {
                    log.info("not found processOperation");
                    throw new ServiceException(retCodeConfig.getNotFoundProcessOperation());
                }
                CimProcessDefinition anOrigMainPD = aBackupPo.getMainProcessDefinition();
                if (null == anOrigMainPD) {
                    log.info("Original Route is not found.");
                    throw new ServiceException(retCodeConfig.getNotFoundProcessDefinition());

                }

                origRoute = anOrigMainPD.getIdentifier();
                origOpeNo = aBackupPo.getOperationNumber();
            }
        }else{
            log.info("The lot is not branched/reworked.");
            origRoute = splitRoute;
            origOpeNo = splitOpeNo;
        }
        log.info("The key items of PSM definition");
        // PPT_DISPLAY_RESPONSE_TIME();
        com.fa.cim.newcore.bo.product.CimFutureSplitJob aPosPlannedSplitJobObject = productManager.findFutureSplitJobFor(lotFamily, splitRoute, splitOpeNo, origRoute, origOpeNo);
        //------------------------------------------------------------------------//
        // PPT_SET_MSG_RC_KEY3
        // "Experimental Lot [%s] Object is not found. RouteID [%s], splitOperationNumber [%s]"
        //      %1:lotID.identifier, %2:routeID.identifier, %3:splitOperationNumber
        //------------------------------------------------------------------------//
        Validations.check(null == aPosPlannedSplitJobObject, retCodeConfig.getNotFoundExperimentalLotObj());//getNotFoundExperimentalLotObj()

        //Get Experimental Lot Info
        ProductDTO.FutureSplitJobInfo aPosPlannedSplitJobInfo = aPosPlannedSplitJobObject.getFutureSplitJobInfo();
        //Set output buffer
        com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo strInfo = new com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo();
        strInfo.setLotFamilyID(aPosPlannedSplitJobInfo.getLotFamilyID());
        strInfo.setSplitRouteID(aPosPlannedSplitJobInfo.getSplitRouteID());
        strInfo.setSplitOperationNumber(aPosPlannedSplitJobInfo.getSplitOperationNumber());
        strInfo.setOriginalRouteID(aPosPlannedSplitJobInfo.getOriginalRouteID());
        strInfo.setOriginalOperationNumber(aPosPlannedSplitJobInfo.getOriginalOperationNumber());
        strInfo.setActionEMail(aPosPlannedSplitJobInfo.isActionEMail());
        strInfo.setActionHold(aPosPlannedSplitJobInfo.isActionHold());
        //add auto separate and combine  - jerry
        strInfo.setActionSeparateHold(aPosPlannedSplitJobInfo.getActionSeparateHold());
        strInfo.setActionCombineHold(aPosPlannedSplitJobInfo.getActionCombineHold());
        strInfo.setTestMemo(aPosPlannedSplitJobInfo.getTestMemo());
        strInfo.setExecFlag(aPosPlannedSplitJobInfo.isExecutedFlag());
        strInfo.setActionTimeStamp(CimDateUtils.convertToSpecString(aPosPlannedSplitJobInfo.getExecutedTimeStamp()));
        strInfo.setModifyTimeStamp(CimDateUtils.convertToSpecString(aPosPlannedSplitJobInfo.getLastClaimedTimeStamp()));
        strInfo.setModifyUserID(aPosPlannedSplitJobInfo.getModifier());
        int nLotDetailInfoLen = CimArrayUtils.getSize(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails());
        log.info("nLotDetailInfoLen,{}",nLotDetailInfoLen);
        // Initialization and logging
        List<Long> operationSequenceNumberSeq = new ArrayList<>();
        log.info("###### Before sorting. ");
        for (int i = 0; i < nLotDetailInfoLen; i++) {
            operationSequenceNumberSeq.add(i,0L);
            log.info("##### Number,{}",i);
            log.info("##### branch route,{}",aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getRouteID().getValue());
            log.info("#####mergeOperationNumber,{}",aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getMergeOperationNumber());
        }
        Boolean noSortFlag = false;
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strExperimentalFutureLotDetailInfoSeq = new ArrayList<>();
        strInfo.setStrExperimentalFutureLotDetailInfoSeq(strExperimentalFutureLotDetailInfoSeq);
        for (int i = 0; i < nLotDetailInfoLen; i++) {
            /***********************************************************************/
            /*  Sort PSM detail information order by mergeOperationNumber.         */
            /*  ** If mergeOperationNumber is the same, then not change an order.  */
            /***********************************************************************/
            Inputs.ProcessOperationSequenceGetDRIn processOperationSequenceGetDRIn = new Inputs.ProcessOperationSequenceGetDRIn();
            processOperationSequenceGetDRIn.setRouteId(strInfo.getSplitRouteID());
            processOperationSequenceGetDRIn.setOperationNumber(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getMergeOperationNumber());
            try {
                Outputs.ObjProcessOperationSequenceGetDROut processOperationSequenceGetDROut = processMethod.processOperationSequenceGetDR(objCommon, processOperationSequenceGetDRIn);

                long tryingNum = processOperationSequenceGetDROut.getOperationSequenceNumber();
                com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo tryingStruct = new com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo();
                tryingStruct.setRouteID(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getRouteID());
                tryingStruct.setReturnOperationNumber(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getReturnOperationNumber());
                tryingStruct.setMergeOperationNumber(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getMergeOperationNumber());
                // task-3988 this function not has been definde ,for now just comment it out

                final List<ProductDTO.Wafer> wafers = aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getWafers();
                List<com.fa.cim.fsm.Infos.Wafer> waferIDs = new ArrayList<>();
                if(CimArrayUtils.isNotEmpty(wafers)) {
                    for (ProductDTO.Wafer wafer : wafers) {
                        com.fa.cim.fsm.Infos.Wafer waf = new com.fa.cim.fsm.Infos.Wafer();
                        waf.setGroupNo(wafer.getGroupNo());
                        waf.setWaferID(wafer.getWaferID());
                        waf.setSlotNumber(wafer.getSlotNumber());
                        waferIDs.add(waf);
                    }
                }
                tryingStruct.setWaferIDs(waferIDs);
                tryingStruct.setMemo(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getMemo());
                tryingStruct.setDynamicFlag(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).isDynamicFlag());
                tryingStruct.setExecFlag(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).isExecutedFlag());
                tryingStruct.setActionTimeStamp(CimDateUtils.convertToSpecString(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getExecutedTimeStamp()));
                strExperimentalFutureLotDetailInfoSeq.add(tryingStruct);
                for (int j = 0; j < nLotDetailInfoLen; j++) {
                    if (operationSequenceNumberSeq.get(j) < tryingNum) {
                        log.info("####, {} < {}", operationSequenceNumberSeq.get(j), tryingNum);
                        //Set operationSequence
                        Long tmpNum = operationSequenceNumberSeq.get(j);
                        operationSequenceNumberSeq.set(j, tryingNum);
                        tryingNum = tmpNum;
                        //set structure
                        com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo tmpStruct = strExperimentalFutureLotDetailInfoSeq.get(j);
                        strInfo.getStrExperimentalFutureLotDetailInfoSeq().set(j,tryingStruct);
                        tryingStruct = tmpStruct;
                    }
                }
            }catch (ServiceException ex){

                log.info("process_operationSequence_GetDR != RC_OK ");
                log.info("Omit Sort logic.");
                noSortFlag = true;
                break;

            }
        }
        // Check noSortFlag
        if (CimBooleanUtils.isTrue(noSortFlag)){
            List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strExperimentalFutureLotDetailInfoSeq1 = new ArrayList<>();
            strInfo.setStrExperimentalFutureLotDetailInfoSeq(strExperimentalFutureLotDetailInfoSeq1);
            for (int i = 0; i < nLotDetailInfoLen; i++) {
                com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo experimentalFutureLotDetailInfo = new com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo();
                strExperimentalFutureLotDetailInfoSeq1.add(i,experimentalFutureLotDetailInfo);
                experimentalFutureLotDetailInfo.setRouteID(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getRouteID());
                experimentalFutureLotDetailInfo.setReturnOperationNumber(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getReturnOperationNumber());
                experimentalFutureLotDetailInfo.setMergeOperationNumber(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getMergeOperationNumber());
                // task-3988 this function not has been definde ,for now just comment it out

                final List<ProductDTO.Wafer> wafers = aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getWafers();
                List<com.fa.cim.fsm.Infos.Wafer> waferIDs = new ArrayList<>();
                if(CimArrayUtils.isNotEmpty(wafers)) {
                    for (ProductDTO.Wafer wafer : wafers) {
                        com.fa.cim.fsm.Infos.Wafer waf = new com.fa.cim.fsm.Infos.Wafer();
                        waf.setGroupNo(wafer.getGroupNo());
                        waf.setWaferID(wafer.getWaferID());
                        waf.setSlotNumber(wafer.getSlotNumber());
                        waferIDs.add(waf);
                    }
                }
                experimentalFutureLotDetailInfo.setWaferIDs(waferIDs);
                experimentalFutureLotDetailInfo.setMemo(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getMemo());
                experimentalFutureLotDetailInfo.setDynamicFlag(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).isDynamicFlag());
                experimentalFutureLotDetailInfo.setExecFlag(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).isExecutedFlag());
                experimentalFutureLotDetailInfo.setActionTimeStamp(CimDateUtils.convertToSpecString(aPosPlannedSplitJobInfo.getFutureSplitJobInfoDetails().get(i).getExecutedTimeStamp()));
            }
        }
        out.setStrExperimentalFutureLotInfo(strInfo);
        //logging after sortin
        log.info("After sorting. ");
        int resultLen = CimArrayUtils.getSize(out.getStrExperimentalFutureLotInfo().getStrExperimentalFutureLotDetailInfoSeq());
        for (int i = 0; i < resultLen; i++) {
            log.info("##### Number, {}",i);
            log.info("##### branch route,  {}",out.getStrExperimentalFutureLotInfo().getStrExperimentalFutureLotDetailInfoSeq().get(i).getRouteID().getValue());
            log.info("##### mergeOperationNumber, {}",out.getStrExperimentalFutureLotInfo().getStrExperimentalFutureLotDetailInfoSeq().get(i).getMergeOperationNumber());
        }
        //Return to Caller
        return out;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/5 15:18
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public com.fa.cim.fsm.Outputs.ObjExperimentalFutureLotActualInfoCreateOut experimentalFutureLotActualInfoCreate(Infos.ObjCommon objCommon, ObjectIdentifier lotId, com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo strExperimentalFutureLotInfo) {
        //init
        com.fa.cim.fsm.Outputs.ObjExperimentalFutureLotActualInfoCreateOut out = new com.fa.cim.fsm.Outputs.ObjExperimentalFutureLotActualInfoCreateOut();

        //Get Wafers of Lot Family
        List<Infos.WaferListInLotFamilyInfo> lotWaferStatusListDROut = lotMethod.lotWafersStatusListGetDR(objCommon, strExperimentalFutureLotInfo.getLotFamilyID());

        List<Infos.WaferListInLotFamilyInfo> waferList = lotWaferStatusListDROut;
        int familyWaferLen = CimArrayUtils.getSize(waferList);
        //Check which wafers are specified as target
        //----- Actual Data -------//
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strActualDataList = new ArrayList<>();
        //----- Defined Data -------//
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strDefinedDataList = strExperimentalFutureLotInfo.getStrExperimentalFutureLotDetailInfoSeq();
        //----- History Data -------//
        com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailResultInfo strHistoryData = new com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailResultInfo();

        int subRouteCnt = 0;
        int subRouteLen = CimArrayUtils.getSize(strDefinedDataList);

        //History Data
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetail> strExperimentalLotDetailSeq = new ArrayList<>();
        strHistoryData.setStrExperimentalFutureLotDetailSeq(strExperimentalLotDetailSeq);

        for (int i = 0; i < subRouteLen; i++) {
            //History Data i
            com.fa.cim.fsm.Infos.ExperimentalFutureLotDetail experimentalLotDetail = new com.fa.cim.fsm.Infos.ExperimentalFutureLotDetail();
            strExperimentalLotDetailSeq.add(i,experimentalLotDetail);

            //Actual Data routeCnt
            com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo experimentalFutureLotDetailInfoActual = new com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo();
            strActualDataList.add(subRouteCnt,experimentalFutureLotDetailInfoActual);

            if (CimBooleanUtils.isTrue(strDefinedDataList.get(i).getExecFlag())){
                continue;
            }
            log.info("Check the sub route's wafers.{},{},{}",i,strDefinedDataList.get(i).getRouteID().getValue());
            int waferCnt = 0;
            int waferLen = CimArrayUtils.getSize(strDefinedDataList.get(i).getWaferIDs());

            int waferLackCnt = 0;
            int waferOmitCnt = 0;
            //History Data i
            List<com.fa.cim.fsm.Infos.ExperimentalFutureLotWafer> strExperimentalLotWaferSeq = new ArrayList<>();
            experimentalLotDetail.setStrExperimentalLotWaferSeq(strExperimentalLotWaferSeq);

            //Actual Data
            List<com.fa.cim.fsm.Infos.Wafer> waferIDs = new ArrayList<com.fa.cim.fsm.Infos.Wafer>();
            experimentalFutureLotDetailInfoActual.setWaferIDs(waferIDs);
            for (int j = 0; j < waferLen; j++) {
                //History Data
                com.fa.cim.fsm.Infos.ExperimentalFutureLotWafer experimentalLotWafer = new com.fa.cim.fsm.Infos.ExperimentalFutureLotWafer();
                strExperimentalLotWaferSeq.add(j,experimentalLotWafer);

                //  task-3988 the method of setting wafer has not been definde ,in order to ensure that the error is not changed,if the method is defined it will be overridden
                int k = 0;
                for (k = 0; k < familyWaferLen; k++) {
                    if (CimStringUtils.equals(strDefinedDataList.get(i).getWaferIDs().get(j).getWaferID().getValue(), waferList.get(k).getWaferID().getValue())){
                        log.info("Check the wafer's condition.{}",i,j,strDefinedDataList.get(i).getWaferIDs().get(j).getWaferID().getValue());
                        if (CimBooleanUtils.isTrue(waferList.get(k).getScrapFlag())){
                            k = familyWaferLen;
                        }else if (!CimStringUtils.equals(lotId.getValue(), waferList.get(k).getLotID().getValue())){
                            log.info("The wafer relates to another lot.");
                            waferLackCnt++;
                        }else {
                            log.info("The wafer relates to the lot.");
                        }
                        break;
                    }
                }
                //History Data : Wafer
                experimentalLotWafer.setWaferId(strDefinedDataList.get(i).getWaferIDs().get(j));
                if (k == familyWaferLen){
                    log.info("The wafer is scrapped or had been deleted.");
                    experimentalLotWafer.setStatus("NG");
                    waferOmitCnt++;
                    continue;
                }else {
                    experimentalLotWafer.setStatus("OK");
                }
                // No wafer data is set if some target wafer is lack.
                if(waferLackCnt > 0){
                    continue;
                }
                log.info("The wafer is actual target.");
                //Actual Data : Wafer
                waferIDs.add(waferCnt,strDefinedDataList.get(i).getWaferIDs().get(j));
                waferCnt ++;
            }
            //History Data : Sub Route
            experimentalLotDetail.setRouteID(strDefinedDataList.get(i).getRouteID());
            experimentalLotDetail.setReturnOperationNumber(strDefinedDataList.get(i).getReturnOperationNumber());
            experimentalLotDetail.setMergeOperationNumber(strDefinedDataList.get(i).getMergeOperationNumber());
            experimentalLotDetail.setMemo(strDefinedDataList.get(i).getMemo());

            // No sub route data is set if some target wafer is lack

//            if (waferLackCnt + waferOmitCnt == waferLen){
//                log.info("All wafers of the sub route are not actual target.");
//                continue;
//            }else if ( waferLackCnt > 0 ){
//                log.info("Some wafers of the sub route are not actual target.");
//                throw new ServiceException(retCodeConfigEx.getFsmExecutionFail());
//            }
            // bug-6908 Throws an exception when the number of wafers is inconsistent
            if (waferLackCnt > 0 || (waferLackCnt+waferOmitCnt) == waferLen){
                log.info("Some wafers of the sub route are not actual target.");
                throw new ServiceException(retCodeConfigEx.getFsmExecutionFail());
            }

            //Actual Data : Sub Route
            experimentalFutureLotDetailInfoActual.setRouteID(strDefinedDataList.get(i).getRouteID());
            experimentalFutureLotDetailInfoActual.setReturnOperationNumber(strDefinedDataList.get(i).getReturnOperationNumber());
            experimentalFutureLotDetailInfoActual.setMergeOperationNumber(strDefinedDataList.get(i).getMergeOperationNumber());
            experimentalFutureLotDetailInfoActual.setMemo(strDefinedDataList.get(i).getMemo());
            experimentalFutureLotDetailInfoActual.setDynamicFlag(strDefinedDataList.get(i).getDynamicFlag());
            subRouteCnt++;
        }
        log.info("The number of sub route which is actual target.{}",subRouteCnt);
        //History Data : Base
        if(subRouteCnt == 0){
            strActualDataList = new ArrayList<>();
            strExperimentalLotDetailSeq = new ArrayList<>();
            strHistoryData.setStrExperimentalFutureLotDetailSeq(strExperimentalLotDetailSeq);
        }else {
            //strActualDataList.length( subRouteCnt );
        }
        strHistoryData.setUserID(objCommon.getUser().getUserID());
        strHistoryData.setLotFamilyID(strExperimentalFutureLotInfo.getLotFamilyID());
        strHistoryData.setSplitRouteID(strExperimentalFutureLotInfo.getSplitRouteID());
        strHistoryData.setSplitOperationNumber(strExperimentalFutureLotInfo.getSplitOperationNumber());
        strHistoryData.setOriginalRouteID(strExperimentalFutureLotInfo.getOriginalRouteID());
        strHistoryData.setOriginalOperationNumber(strExperimentalFutureLotInfo.getOriginalOperationNumber());
        strHistoryData.setActionEMail(strExperimentalFutureLotInfo.getActionEMail());
        strHistoryData.setActionHold(strExperimentalFutureLotInfo.getActionHold());

        //Output Data
        out.setLotFamilyId(strExperimentalFutureLotInfo.getLotFamilyID());
        out.setSplitRouteId(strExperimentalFutureLotInfo.getSplitRouteID());
        out.setSplitOperationNumber(strExperimentalFutureLotInfo.getSplitOperationNumber());
        out.setOriginalRouteId(strExperimentalFutureLotInfo.getOriginalRouteID());
        out.setOriginalOperationNumber(strExperimentalFutureLotInfo.getOriginalOperationNumber());
        out.setActionEmail(strExperimentalFutureLotInfo.getActionEMail());
        out.setActionHold(strExperimentalFutureLotInfo.getActionHold());
        out.setActionSeparateHold(strExperimentalFutureLotInfo.getActionSeparateHold());
        out.setActionCombineHold(strExperimentalFutureLotInfo.getActionCombineHold());
        out.setTestMemo(strExperimentalFutureLotInfo.getTestMemo());
        out.setStrExperimentalFutureLotDetailInfoSeq(strActualDataList);
        out.setStrExperimentalLotDetailResultInfo(strHistoryData);

        return out;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/22 下午 1:29
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public String experimentalFutureLotInfoDelete(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber) {

        //【step1】Find Experimental Lot Info When the record does not exist, the record is not deleted.
        com.fa.cim.newcore.bo.product.CimFutureSplitJob aPosFutureSplitJob = productManager.findFutureSplitJobFor(lotFamilyID.getValue(), splitRouteID.getValue(), splitOperationNumber, originalRouteID.getValue(), originalOperationNumber);

        //runCard add data
        String psmID = aPosFutureSplitJob.getIdentifier();
        //runCard add end

        if (CimObjectUtils.isEmpty(aPosFutureSplitJob)){
            throw new ServiceException(retCodeConfig.getNotFoundExperimentalLotData());
        }

        //【step2】When the record exists, the record should be deleted.
        productManager.removeFutureSplitJobFor(lotFamilyID.getValue(),splitRouteID.getValue(),splitOperationNumber,originalRouteID.getValue(),originalOperationNumber);
        return psmID;
    }

    @Override
    public List<com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo> experimentalFutureLotListGetDR(Infos.ObjCommon strObjCommonIn, String lotFamily, String splitRoute, String splitOperationNumber, String originalRoute, String originalOperationNumber, Boolean execCheckFlag, Boolean detailRequireFlag) {

        // 【step1】:Initialize
        String hFRPLSPLITdTheTableMarker2;
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo> result = new ArrayList<>();
        Boolean runCardFlag = false;

        //【step2】:Get PSM Definition
        String HV_BUFFER = "";
        String HV_TMPBUFFER = "";
        HV_BUFFER += "SELECT ID, LOTFAMILY_ID, LOTFAMILY_RKEY, " +
                "SPLIT_PROCESS_ID, SPLIT_PROCESS_RKEY, SPLIT_OPE_NO,ORIG_PROCESS_ID, " +
                "ORIG_PROCESS_RKEY, ORIG_OPE_NO, MAIL_ACTION, HOLD_ACTION, TEST_MEMO, " +
                "EXECUTED_FLAG, EXECUTED_TIME, UPDATE_TIME, MODIFY_USER_ID,MODIFY_USER_RKEY,SEPARATE_ACTION,COMBINE_ACTION FROM OMFSM ";
        if (CimStringUtils.isNotEmpty(lotFamily)) {
            HV_TMPBUFFER = String.format("WHERE LOTFAMILY_ID = '%s' ", lotFamily);
            HV_BUFFER += HV_TMPBUFFER;
        }
        if (CimStringUtils.isNotEmpty(splitRoute)) {
            if (HV_TMPBUFFER.length() > 0) {
                HV_TMPBUFFER = "";
                HV_TMPBUFFER = String.format("AND   SPLIT_PROCESS_ID = '%s' ", splitRoute);
            } else {
                HV_TMPBUFFER = String.format("WHERE SPLIT_PROCESS_ID = '%s' ", splitRoute);
            }
            HV_BUFFER += HV_TMPBUFFER;
        }
        if (CimStringUtils.isNotEmpty(splitOperationNumber)) {
            if (HV_TMPBUFFER.length() > 0) {
                HV_TMPBUFFER = "";
                HV_TMPBUFFER = String.format("AND   SPLIT_OPE_NO = '%s' ", splitOperationNumber);
            } else {
                HV_TMPBUFFER = String.format("WHERE SPLIT_OPE_NO = '%s' ", splitOperationNumber);
            }
            HV_BUFFER += HV_TMPBUFFER;
        }
        if (CimStringUtils.isNotEmpty(originalRoute)) {
            if (HV_TMPBUFFER.length() > 0) {
                HV_TMPBUFFER = "";
                HV_TMPBUFFER = String.format("AND   ORIG_PROCESS_ID = '%s' ", originalRoute);
            } else {
                HV_TMPBUFFER = String.format("WHERE ORIG_PROCESS_ID = '%s' ", originalRoute);
            }
            HV_BUFFER += HV_TMPBUFFER;
        }
        if (CimStringUtils.isNotEmpty(originalOperationNumber)) {
            if (HV_TMPBUFFER.length() > 0) {
                HV_TMPBUFFER = "";
                HV_TMPBUFFER = String.format("AND   ORIG_OPE_NO = '%s' ", originalOperationNumber);
            } else {
                HV_TMPBUFFER = String.format("WHERE ORIG_OPE_NO = '%s' ", originalOperationNumber);
            }
            HV_BUFFER += HV_TMPBUFFER;
        }
        if (true == execCheckFlag) {
            if (HV_TMPBUFFER.length() > 0) {
                HV_BUFFER += "AND   EXECUTED_FLAG = 0 ";
            } else {
                HV_BUFFER += "WHERE EXECUTED_FLAG = 0 ";
            }
        }

        //【step3】SQL PREPARE
        List<Object[]> cimPlannedSplitDOList = cimJpaRepository.query(HV_BUFFER);

        //【step4】PSM Definitions
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo> strDefList = new ArrayList<>();
        for (Object[] cimPlannedSplitDO : cimPlannedSplitDOList) {
            com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo experimentalFutureLotInfo = new com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo();
            experimentalFutureLotInfo.setLotFamilyID(ObjectIdentifier.build(CimObjectUtils.toString(cimPlannedSplitDO[1]), CimObjectUtils.toString(cimPlannedSplitDO[2])));
            experimentalFutureLotInfo.setSplitRouteID(ObjectIdentifier.build(CimObjectUtils.toString(cimPlannedSplitDO[3]), CimObjectUtils.toString(cimPlannedSplitDO[4])));
            experimentalFutureLotInfo.setSplitOperationNumber(CimObjectUtils.toString(cimPlannedSplitDO[5]));
            experimentalFutureLotInfo.setOriginalRouteID(ObjectIdentifier.build(CimObjectUtils.toString(cimPlannedSplitDO[6]), CimObjectUtils.toString(cimPlannedSplitDO[7])));
            experimentalFutureLotInfo.setOriginalOperationNumber(CimObjectUtils.toString(cimPlannedSplitDO[8]));
            experimentalFutureLotInfo.setActionEMail(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitDO[9])));
            experimentalFutureLotInfo.setActionHold(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitDO[10])));
            //add auto separate and combine  - jerry
            experimentalFutureLotInfo.setActionSeparateHold(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitDO[17])));
            experimentalFutureLotInfo.setActionCombineHold(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitDO[18])));
            experimentalFutureLotInfo.setTestMemo(CimObjectUtils.toString(cimPlannedSplitDO[11]));
            experimentalFutureLotInfo.setExecFlag(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitDO[12])));
            experimentalFutureLotInfo.setActionTimeStamp(CimDateUtils.convertToSpecString((Timestamp) (cimPlannedSplitDO[13])));
            experimentalFutureLotInfo.setModifyTimeStamp(CimDateUtils.convertToSpecString((Timestamp) (cimPlannedSplitDO[14])));
            experimentalFutureLotInfo.setModifyUserID(ObjectIdentifier.build(CimObjectUtils.toString(cimPlannedSplitDO[15]), CimObjectUtils.toString(cimPlannedSplitDO[16])));
            List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strBranchRouteList = new ArrayList<>();
            experimentalFutureLotInfo.setStrExperimentalFutureLotDetailInfoSeq(strBranchRouteList);
            strDefList.add(experimentalFutureLotInfo);
            if (CimBooleanUtils.isFalse(detailRequireFlag)) {
                continue;
            }
            String hFRPLSPLITdheSystemKey = CimObjectUtils.toString(cimPlannedSplitDO[0]);//ID

            //【step5】 Get Branch Route
            String sqlRoute = "SELECT IDX_NO, PROCESS_ID, PROCESS_RKEY, RETURN_OPE_NO, MERGE_OPE_NO, MEMO, DYNAMIC_FLAG, EXECUTED_FLAG, EXECUTED_TIME FROM OMFSM_RT WHERE  REFKEY = ? ORDER BY IDX_NO";

            List<Object[]> cimFutureSplitJobSubRouteDOList = cimJpaRepository.query(sqlRoute, hFRPLSPLITdheSystemKey);
            //【step6】Branch Route Data
            for (Object[] cimPlannedSplitJobSubRouteDO : cimFutureSplitJobSubRouteDOList) {
                com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo experimentalFutureLotDetailInfo = new com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo();
                experimentalFutureLotDetailInfo.setRouteID(ObjectIdentifier.build(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[1]), CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[2])));
                experimentalFutureLotDetailInfo.setReturnOperationNumber(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[3]));
                experimentalFutureLotDetailInfo.setMergeOperationNumber(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[4]));
                experimentalFutureLotDetailInfo.setMemo(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[5]));
                experimentalFutureLotDetailInfo.setDynamicFlag(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[6])));
                experimentalFutureLotDetailInfo.setExecFlag(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[7])));
                experimentalFutureLotDetailInfo.setActionTimeStamp(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[8]));

                //【step7】Get Wafer
                String subrtSeqNo = CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[0]);
                String hFRPLSPLITdTheTableMarker = "";
                hFRPLSPLITdTheTableMarker += subrtSeqNo;
                hFRPLSPLITdTheTableMarker2 = subrtSeqNo;

                //【step8】DECLATE : OMPSM_SUBPROC_WFR
                String sqlWF = "SELECT WAFER_ID, WAFER_RKEY,WAFER_GROUP" +
                        " FROM  OMFSM_RT_WF" +
                        " WHERE  REFKEY = ?1" +
                        " AND LINK_MARKER = ?2" +
                        " AND    (LINK_MARKER = ?3 OR LINK_MARKER = ?4) ORDER BY IDX_NO";
                List<Object[]> cimFutureSplitJobSubRouteWaferDOS = cimJpaRepository.query(sqlWF, hFRPLSPLITdheSystemKey,hFRPLSPLITdTheTableMarker,hFRPLSPLITdTheTableMarker,hFRPLSPLITdTheTableMarker2);

                //【step9】Wafer Data

                //task-3988 The structure of the waferlist has been changed for testing purposes in task 3988
                ArrayList<com.fa.cim.fsm.Infos.Wafer> wafers = new ArrayList<>();
                for (Object[] cimFutureSplitJobSubRouteWaferDO : cimFutureSplitJobSubRouteWaferDOS) {
                    ObjectIdentifier objectIdentifier = new ObjectIdentifier();
                    objectIdentifier.setValue(CimObjectUtils.toString(cimFutureSplitJobSubRouteWaferDO[0]));
                    objectIdentifier.setReferenceKey(CimObjectUtils.toString(cimFutureSplitJobSubRouteWaferDO[1]));
                    com.fa.cim.fsm.Infos.Wafer wafer = new com.fa.cim.fsm.Infos.Wafer();
                    wafer.setWaferID(objectIdentifier);
                    wafer.setGroupNo(CimObjectUtils.toString(cimFutureSplitJobSubRouteWaferDO[2]));
                    wafers.add(wafer);
                }
                experimentalFutureLotDetailInfo.setWaferIDs(wafers);
                strBranchRouteList.add(experimentalFutureLotDetailInfo);
            }
        }
        result = strDefList;
        //run card check start
        log.info("Check run card from wafer - lot - runRard");
        return result;
    }


}