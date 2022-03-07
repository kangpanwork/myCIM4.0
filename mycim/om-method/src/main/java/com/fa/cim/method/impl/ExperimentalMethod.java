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
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.product.CimLotFamily;
import com.fa.cim.newcore.bo.product.CimPlannedSplitJob;
import com.fa.cim.newcore.bo.product.CimWafer;
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
import java.util.Optional;

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
public class ExperimentalMethod implements IExperimentalMethod {

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
    public void experimentalLotInfoCheck(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber, List<Infos.ExperimentalLotDetailInfo> strExperimentalLotDetailInfoSeq) {

        log.info(" experimental_lotInfo_Check");
        //【step0】 Initialize
        com.fa.cim.newcore.bo.pd.CimProcessFlow aPF = null;
        com.fa.cim.newcore.bo.pd.CimProcessFlow aSplitPF = null;
        //【step1】lot_wafersStatusList_GetDR
        List<Infos.WaferListInLotFamilyInfo> lotWaferStatusListDROut = lotMethod.lotWafersStatusListGetDR(objCommon, lotFamilyID);

        Validations.check(CimObjectUtils.isEmpty(lotWaferStatusListDROut), retCodeConfig.getNotFoundWafer());

        List<Infos.WaferListInLotFamilyInfo> familyWaferList = lotWaferStatusListDROut;
        int familyWaferLen = CimArrayUtils.getSize(familyWaferList);
        int targetRtLen = CimArrayUtils.getSize(strExperimentalLotDetailInfoSeq);
        for (int routeCnt = 0; routeCnt < targetRtLen; routeCnt++) {
            int routeWaferLen = CimArrayUtils.getSize(strExperimentalLotDetailInfoSeq.get(routeCnt).getWaferIDs());
            for(int waferCnt = 0;waferCnt < routeWaferLen;waferCnt++){
                int familyWaferCnt = 0;
                for (familyWaferCnt = 0;familyWaferCnt < familyWaferLen;familyWaferCnt++ ){
                    if (CimStringUtils.equals(strExperimentalLotDetailInfoSeq.get(routeCnt).getWaferIDs().get(waferCnt).getValue(),
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
        com.fa.cim.newcore.bo.pd.CimProcessDefinition aPD = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class, originalRouteID);
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
            Validations.check(!CimStringUtils.equals(splitOperationNumber,originalOperationNumber), retCodeConfig.getPsmOpeInvalid(),originalOperationNumber,splitOperationNumber);

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
        List<Infos.ExperimentalLotInfo> strDefList = this.experimentalLotListGetDR(objCommon, ObjectIdentifier.fetchValue(lotFamilyID), "", "", originalRouteID.getValue(), "", true, true);
        //Loop of target PSM's branch route
        for (int i = 0; i < targetRtLen; i++) {
            log.info("Branch route of target PSM",i,strExperimentalLotDetailInfoSeq.get(i).getSubRouteID().getValue());
            if( CimStringUtils.length(endPoint) < 1){
                endPoint = strExperimentalLotDetailInfoSeq.get(i).getMergeOperationNumber();
            }
            log.info("Start {} and end point {}of target PSM",startPoint,endPoint);
            //Checks branch route's setting
            if(CimBooleanUtils.isTrue(strExperimentalLotDetailInfoSeq.get(i).getDynamicFlag())){
                //【step5】process_checkForDynamicRoute
                Outputs.ObjProcessCheckForDynamicRouteOut processCheckForDynamicRouteOut = processMethod.processCheckForDynamicRoute(objCommon, strExperimentalLotDetailInfoSeq.get(i).getSubRouteID());
                if (!CimStringUtils.equals(processCheckForDynamicRouteOut.getProcessDefinitionType(),BizConstant.SP_MAINPDTYPE_BRANCH)){
                    log.info("The route type of branch route has to be 'Branch'.");
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidPDType(),
                            processCheckForDynamicRouteOut.getProcessDefinitionType(),strExperimentalLotDetailInfoSeq.get(i).getSubRouteID().getValue()));

                }else if (CimBooleanUtils.isFalse(processCheckForDynamicRouteOut.getDynamicRouteFlag())){
                    log.info("Branch route has to be the dynamic one.");
                    throw new ServiceException(retCodeConfig.getNotDynamicRoute());
                }else {
                    log.info("Branch route is a dynamic branch route.");
                }
            }else {
                int getBranchRtCnt_forSptOpe = 0;
                for (getBranchRtCnt_forSptOpe = 0; getBranchRtCnt_forSptOpe < getBranchRtLen_forSptOpe; getBranchRtCnt_forSptOpe++) {
                    if (ObjectIdentifier.equalsWithValue(strExperimentalLotDetailInfoSeq.get(i).getSubRouteID(), strSptOpeInfo.getConnectedRouteList().get(getBranchRtCnt_forSptOpe).getRouteID())){
                        log.info("Branch route is related to split operation.");
                        break;
                    }
                }
                if (getBranchRtCnt_forSptOpe == getBranchRtLen_forSptOpe){
                    log.info("Branch route has to be related to split operation.");
                    throw new ServiceException(retCodeConfig.getNotFoundExperimentalLotSubroute());
                }else if (!CimStringUtils.equals(strExperimentalLotDetailInfoSeq.get(i).getReturnOperationNumber(),strSptOpeInfo.getConnectedRouteList().get(getBranchRtCnt_forSptOpe).getReturnOperationNumber())){
                    log.info("Return operation has to be same as the setting of SM.");
                    throw new ServiceException(new OmCode(retCodeConfig.getPsmOpeInvalid(),strSptOpeInfo.getConnectedRouteList().get(getBranchRtCnt_forSptOpe).getReturnOperationNumber(),strExperimentalLotDetailInfoSeq.get(i).getReturnOperationNumber()));
                }else {
                    log.info("Return operation is same as the setting of SM.");
                }
            }
            //Checks whether merge operation is on split route
            //【step6】process_operationListInRoute_GetDR
            List<Infos.OperationInfo> processOperationListInRouteGetDROutForMrgOpe = processMethod.processOperationListInRouteGetDR(objCommon, splitRouteID, strExperimentalLotDetailInfoSeq.get(i).getMergeOperationNumber(), "");
            if (CimObjectUtils.isEmpty(processOperationListInRouteGetDROutForMrgOpe)){
                throw new ServiceException(retCodeConfig.getInvalidExperimentalLotMergePoint());
            }
            //Checks the relation between merge operation and split operation
            Boolean toSplit =false;
            if (aSplitPF == null){
                toSplit = aPF.isAfterOperationNumberForMain(null, strExperimentalLotDetailInfoSeq.get(i).getMergeOperationNumber(), splitOperationNumber);
            }else{
                toSplit = aSplitPF.isAfterOperationNumberForMain(null,strExperimentalLotDetailInfoSeq.get(i).getMergeOperationNumber(),splitOperationNumber);
            }
            Validations.check(CimBooleanUtils.isFalse(toSplit), retCodeConfig.getInvalidExperimentalLotMergePoint());

            //Checks whether return operation is on split route
            //【step7】process_operationListInRoute_GetDR
            List<Infos.OperationInfo> processOperationListInRouteGetDRForRtnOpe = processMethod.processOperationListInRouteGetDR(objCommon, splitRouteID, strExperimentalLotDetailInfoSeq.get(i).getReturnOperationNumber(), "");
            if (CimObjectUtils.isEmpty(processOperationListInRouteGetDRForRtnOpe)){
                log.info("Return operation has to be on split route.");
                throw new ServiceException(new OmCode(retCodeConfig.getNotFoundRouteOpe(),
                        splitRouteID.getValue(),strExperimentalLotDetailInfoSeq.get(i).getReturnOperationNumber()));
            }
            //Checks the relation between merge operation and return operation
            Boolean toReturn = false;
            if (aSplitPF == null){
                toReturn =  aPF.isAfterOperationNumberForMain(null,strExperimentalLotDetailInfoSeq.get(i).getReturnOperationNumber(),strExperimentalLotDetailInfoSeq.get(i).getMergeOperationNumber());
            }else {
                toReturn = aSplitPF.isAfterOperationNumberForMain(null, strExperimentalLotDetailInfoSeq.get(i).getReturnOperationNumber(),strExperimentalLotDetailInfoSeq.get(i).getMergeOperationNumber());
            }

            Validations.check( CimBooleanUtils.isTrue(toReturn), retCodeConfig.getInvalidExperimentalLotMergePoint());

            //Checks futurehold and schedulechange between merge operation and split operation
            int waferLen = 0;
            waferLen = CimArrayUtils.getSize(strExperimentalLotDetailInfoSeq.get(i).getWaferIDs());
            Validations.check(waferLen < 1, retCodeConfig.getNotFoundWafer());

            //Get Lot from Wafer
            //【step8】wafer_lot_Get
            ObjectIdentifier waferLotGetOut = waferMethod.waferLotGet(objCommon, strExperimentalLotDetailInfoSeq.get(i).getWaferIDs().get(0));
            log.info("Check LotID",waferLotGetOut.getValue());
            Boolean isAfterFlag = false;
            Boolean returnCheckFlag = true;
            if (aSplitPF == null){
                isAfterFlag = aPF.isAfterOperationNumberForMain(null, strExperimentalLotDetailInfoSeq.get(i).getReturnOperationNumber(),splitOperationNumber);
            }else {
                isAfterFlag = aSplitPF.isAfterOperationNumberForMain(null, strExperimentalLotDetailInfoSeq.get(i).getReturnOperationNumber(),splitOperationNumber);
            }
            if (CimBooleanUtils.isFalse(isAfterFlag)){
                log.info("returnOperationNumber is before splitOperationNumber");
                returnCheckFlag = false;
            }
            com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, waferLotGetOut);
            Validations.check(CimObjectUtils.isEmpty(aLot),retCodeConfig.getNotFoundLot());

            com.fa.cim.newcore.bo.pd.CimProcessDefinition aSplitPD = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class, splitRouteID);
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
                if (CimStringUtils.equals(processOperationListForRouteOut.get(opeCnt).getOperationNumber(), strExperimentalLotDetailInfoSeq.get(i).getMergeOperationNumber())){
                    log.info("FutureHold Check End!",processOperationListForRouteOut.get(opeCnt).getOperationNumber(),strExperimentalLotDetailInfoSeq.get(i).getMergeOperationNumber());
                    break;
                }
                if (CimStringUtils.equals(processOperationListForRouteOut.get(opeCnt).getOperationNumber(), strExperimentalLotDetailInfoSeq.get(i).getReturnOperationNumber())){
                    log.info("CIMFWStrCmp(strProcess_operationListForRoute_out.strOperationNameAttributes[opeCnt].operationNumber, strExperimentalLotDetailInfoSeq[m].returnOperationNumber) == 0");
                    returnCheckFlag = false;
                }
                List<ProductDTO.FutureHoldRecord> aFutureHoldRecordSequence = new ArrayList<>();
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
                int anotherRtlen = CimArrayUtils.getSize(strDefList.get(j).getStrExperimentalLotDetailInfoSeq());
                for (int k = 0; k < anotherRtlen; k++) {
                    log.info("Branch route of another PSM",j,k,strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getSubRouteID().getValue());
                    //Checks whether all repetitive wafers are specified by the beginning PSM
                    if (CimStringUtils.equals(startPoint, anotherStartPoint) &&
                            !ObjectIdentifier.equalsWithValue(splitRouteID, strDefList.get(j).getSplitRouteID())){
                        //The list of wafers which is specified by the repetitive PSM and the beginning PSM
                        List<ObjectIdentifier> repetitiveWaferList = new ArrayList<>();
                        List<ObjectIdentifier> beginningWaferList = new ArrayList<>();
                        //Target PSM is the one that is defined repetitively
                        if (ObjectIdentifier.equalsWithValue(splitRouteID, strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getSubRouteID())){
                            log.info("Target PSM has repetition with another PSM.");
                            repetitiveWaferList = strExperimentalLotDetailInfoSeq.get(i).getWaferIDs();
                            beginningWaferList = strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getWaferIDs();
                        } else if (ObjectIdentifier.equalsWithValue(strExperimentalLotDetailInfoSeq.get(i).getSubRouteID(), strDefList.get(j).getSplitRouteID())){
                            //Target PSM is the one that is defined as the beginning
                            log.info("Another PSM has repetition with target PSM.");
                            repetitiveWaferList = strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getWaferIDs();
                            beginningWaferList = strExperimentalLotDetailInfoSeq.get(i).getWaferIDs();
                            addLevel1PSMFlag = true;
                        }else {
                            //Target PSM is neither the repetitive PSM nor the beginning PSM
                            log.info("Target PSM and another PSM don't have repetition to each other.");
                            int xWaferLen = CimArrayUtils.getSize(strExperimentalLotDetailInfoSeq.get(i).getWaferIDs());
                            int yWaferLen = CimArrayUtils.getSize(strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getWaferIDs());
                            for (int x = 0;x < xWaferLen; x++){
                                for (int y = 0; y < yWaferLen; y++) {
                                    Validations.check(CimStringUtils.equals(strExperimentalLotDetailInfoSeq.get(i).getWaferIDs().get(x).getValue(), strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getWaferIDs().get(y).getValue()), retCodeConfig.getPsmWaferInvalid());
                                }
                            }
                            accordFlag = true;
                            continue;
                        }
                        int accordCnt = 0;
                        int repWafLen = CimArrayUtils.getSize(repetitiveWaferList);
                        int begWafLen = CimArrayUtils.getSize(beginningWaferList);
                        for (int repWafCnt = 0; repWafCnt < repWafLen; repWafCnt++) {
                            log.info("The wafer of the repetitive PSM",begWafLen,repWafCnt,repetitiveWaferList.get(repWafCnt).getValue());
                            for (int begWafCnt = 0; begWafCnt < begWafLen; begWafCnt++) {
                                log.info("The wafer of the beginning PSM",begWafLen,begWafCnt,beginningWaferList.get(begWafCnt).getValue());
                                if (CimStringUtils.equals(repetitiveWaferList.get(repWafCnt).getValue(), beginningWaferList.get(begWafCnt).getValue())){
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
                        anotherEndPoint = strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getMergeOperationNumber();
                    }
                    log.info("Start and end point of another PSM",anotherStartPoint,anotherEndPoint);
                    //Loop of target PSM's wafers
                    int targetWfLen = CimArrayUtils.getSize(strExperimentalLotDetailInfoSeq.get(i).getWaferIDs());
                    for (int n = 0; n < targetWfLen; n++) {
                        log.info("Wafer of target PSM",i,n,strExperimentalLotDetailInfoSeq.get(i).getWaferIDs().get(n).getValue());
                        //Loop of another PSM's wafers
                        int anotherWfLen = CimArrayUtils.getSize(strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getWaferIDs());
                        for (int m = 0; m < anotherWfLen; m++) {
                            log.info("Wafer of another PSM",j,k,m,strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getWaferIDs().get(m).getValue());
                            //Skips the check for PSM, to which the wafer concerned is not set
                            if (!CimStringUtils.equals(strExperimentalLotDetailInfoSeq.get(i).getWaferIDs().get(n).getValue(),
                                    strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getWaferIDs().get(m).getValue())){
                                log.info("The wafer doesn't overlap, so consistency between definitions is not checked.");
                                log.info(strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getWaferIDs().get(m).getValue());
                                continue;
                            }
                            if (aSplitPF != null
                                    && CimStringUtils.equals(splitRouteID.getValue(), strDefList.get(j).getSplitRouteID().getValue())
                                    && CimStringUtils.equals(originalOperationNumber, strDefList.get(j).getOriginalOperationNumber())){
                                Boolean  checkSplitSplit = false;
                                Boolean  checkSplitMerge = false;
                                Boolean  checkMergeSplit = false;
                                checkSplitSplit = aSplitPF.isAfterOperationNumberForMain(null,strDefList.get(j).getSplitOperationNumber(),splitOperationNumber);
                                checkSplitMerge = aSplitPF.isAfterOperationNumberForMain(null,strDefList.get(j).getSplitOperationNumber(),strExperimentalLotDetailInfoSeq.get(i).getMergeOperationNumber());
                                checkMergeSplit = aSplitPF.isAfterOperationNumberForMain(null,splitOperationNumber,strDefList.get(j).getStrExperimentalLotDetailInfoSeq().get(k).getMergeOperationNumber());
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
    public String  experimentalLotInfoUpdate(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber, Boolean actionEMail, Boolean actionHold, String testMemo, Boolean execFlag
            , String actionTimeStamp, String modifyTimeStamp, String modifyUserID, List<Infos.ExperimentalLotDetailInfo> strExperimentalLotDetailInfoSeq,List<String> originalPsmKeys,String modifyPsmKey,String removePsmKey, Boolean actionSeparateHold, Boolean actionCombineHold) {

        //【step1】Init
        //【step2】Insert or Update the specified indications to DB.
        int lenDetailInfoSeq = CimArrayUtils.getSize(strExperimentalLotDetailInfoSeq);
        log.info("lenDetailInfoSeq",lenDetailInfoSeq);
        ProductDTO.PlannedSplitJobInfo aPlannedSplitJobInfo = new ProductDTO.PlannedSplitJobInfo();

        //【step3】Edit the contents of DB.
        if (!ObjectIdentifier.isEmptyWithRefKey(lotFamilyID)){
            log.info("lotFamilyID.stringifiedObjectReference is not blank");
            aPlannedSplitJobInfo.setLotFamilyID(lotFamilyID);
        }else{
            log.info("lotFamilyID.stringifiedObjectReference is blank");
            CimLotFamily lotFamily = baseCoreFactory.getBO(CimLotFamily.class, lotFamilyID);
            Validations.check(CimObjectUtils.isEmpty(lotFamily),retCodeConfig.getNotFoundLotFamily());
            ObjectIdentifier aLotFamilyOI = ObjectIdentifier.build(lotFamily.getIdentifier(),
                    lotFamily.getPrimaryKey());
            aPlannedSplitJobInfo.setLotFamilyID(aLotFamilyOI);
        }

        if (!ObjectIdentifier.isEmptyWithRefKey(splitRouteID)){
            log.info("splitRouteID.stringifiedObjectReference is not blank");
            aPlannedSplitJobInfo.setSplitRouteID(splitRouteID);
        }else {
            log.info("splitRouteID.stringifiedObjectReference is blank");
            CimProcessDefinition aSplitRoute = baseCoreFactory.getBO(CimProcessDefinition.class, splitRouteID);
            Validations.check(CimObjectUtils.isEmpty(aSplitRoute),retCodeConfig.getNotFoundProcessDefinition());
            ObjectIdentifier aSplitRouteOI = ObjectIdentifier.build(aSplitRoute.getIdentifier(),
                    aSplitRoute.getPrimaryKey());
            aPlannedSplitJobInfo.setSplitRouteID(aSplitRouteOI);
        }
        aPlannedSplitJobInfo.setSplitOperationNumber(splitOperationNumber);

        if (!ObjectIdentifier.isEmptyWithRefKey(originalRouteID)){
            log.info("originalRouteID.stringifiedObjectReference is not blank");
            aPlannedSplitJobInfo.setOriginalRouteID(originalRouteID);
        }else {
            log.info("originalRouteID.stringifiedObjectReference is blank");
            CimProcessDefinition anOriginalRoute = baseCoreFactory.getBO(CimProcessDefinition.class, originalRouteID);
            Validations.check(CimObjectUtils.isEmpty(anOriginalRoute),retCodeConfig.getNotFoundProcessDefinition());
            ObjectIdentifier anOriginalRouteOI = ObjectIdentifier.build(anOriginalRoute.getIdentifier(),
                    anOriginalRoute.getPrimaryKey());
            aPlannedSplitJobInfo.setOriginalRouteID(anOriginalRouteOI);
        }
        aPlannedSplitJobInfo.setOriginalOperationNumber(originalOperationNumber);
        aPlannedSplitJobInfo.setActionEMail(actionEMail);
        aPlannedSplitJobInfo.setActionHold(actionHold);
        //2020/09/01   add Input param actionSeparateHold/actionCombineHold support for auto separate and combine  - jerry
        aPlannedSplitJobInfo.setActionSeparateHold(actionSeparateHold);
        aPlannedSplitJobInfo.setActionCombineHold(actionCombineHold);
        aPlannedSplitJobInfo.setTestMemo(testMemo);
        aPlannedSplitJobInfo.setExecutedFlag(execFlag);
        aPlannedSplitJobInfo.setExecutedTimeStamp(CimDateUtils.convertToOrInitialTime(actionTimeStamp));
        aPlannedSplitJobInfo.setLastClaimedTimeStamp(CimDateUtils.convertToOrInitialTime(modifyTimeStamp));
        CimPerson aModifyUser = baseCoreFactory.getBO(CimPerson.class, new ObjectIdentifier(modifyUserID));
        Validations.check(CimObjectUtils.isEmpty(aModifyUser),retCodeConfig.getNotFoundPerson());
        ObjectIdentifier aModifyUserOI = ObjectIdentifier.build(aModifyUser.getIdentifier(),
                aModifyUser.getPrimaryKey());
        aPlannedSplitJobInfo.setModifier(aModifyUserOI);
        List<ProductDTO.PlannedSplitJobInfoDetail> plannedSplitJobInfoDetails = new ArrayList<>();
        for (int i = 0; i < lenDetailInfoSeq; i++) {
            ProductDTO.PlannedSplitJobInfoDetail plannedSplitJobInfoDetail = new ProductDTO.PlannedSplitJobInfoDetail();
            if (!ObjectIdentifier.isEmptyWithRefKey(strExperimentalLotDetailInfoSeq.get(i).getSubRouteID())){
                log.info("subRouteID.stringifiedObjectReference is not blank");
                plannedSplitJobInfoDetail.setSubRouteID(strExperimentalLotDetailInfoSeq.get(i).getSubRouteID());
            }else {
                log.info("subRouteID.stringifiedObjectReference is blank");
                CimProcessDefinition anSubRoute = baseCoreFactory.getBO(CimProcessDefinition.class, strExperimentalLotDetailInfoSeq.get(i).getSubRouteID());
                Validations.check(CimObjectUtils.isEmpty(anSubRoute),retCodeConfig.getNotFoundProcessDefinition());
                ObjectIdentifier anSubRouteOI = ObjectIdentifier.build(anSubRoute.getIdentifier(),
                        anSubRoute.getPrimaryKey());
                plannedSplitJobInfoDetail.setSubRouteID(anSubRouteOI);
            }
            plannedSplitJobInfoDetail.setReturnOperationNumber(strExperimentalLotDetailInfoSeq.get(i).getReturnOperationNumber());
            plannedSplitJobInfoDetail.setMergeOperationNumber(strExperimentalLotDetailInfoSeq.get(i).getMergeOperationNumber());
            plannedSplitJobInfoDetail.setMemo(strExperimentalLotDetailInfoSeq.get(i).getMemo());

            int waferLen = CimArrayUtils.getSize(strExperimentalLotDetailInfoSeq.get(i).getWaferIDs());
            List<ObjectIdentifier> wafers = new ArrayList<>();
            ObjectIdentifier objectIdentifier = new ObjectIdentifier();
            for (int waferCnt = 0; waferCnt < waferLen; waferCnt++) {
                log.info("waferCnt/waferLen",waferCnt,waferLen);
                if (!ObjectIdentifier.isEmptyWithRefKey(strExperimentalLotDetailInfoSeq.get(i).getWaferIDs().get(waferCnt))){
                    log.info("waferID.stringifiedObjectReference is not blank");
                    objectIdentifier = strExperimentalLotDetailInfoSeq.get(i).getWaferIDs().get(waferCnt);
                }else {
                    log.info("waferID.stringifiedObjectReference is blank");
                    CimWafer aWafer = baseCoreFactory.getBO(CimWafer.class, strExperimentalLotDetailInfoSeq.get(i).getWaferIDs().get(waferCnt));
                    Validations.check(CimObjectUtils.isEmpty(aWafer),retCodeConfig.getNotFoundWafer());
                    objectIdentifier = ObjectIdentifier.build(aWafer.getIdentifier(), aWafer.getPrimaryKey());
                }
                wafers.add(objectIdentifier);
            }
            plannedSplitJobInfoDetail.setWafers(wafers);
            plannedSplitJobInfoDetail.setDynamicFlag(strExperimentalLotDetailInfoSeq.get(i).getDynamicFlag());
            plannedSplitJobInfoDetail.setExecutedFlag(strExperimentalLotDetailInfoSeq.get(i).getExecFlag());
            plannedSplitJobInfoDetail.setExecutedTimeStamp(CimDateUtils.convertToOrInitialTime(strExperimentalLotDetailInfoSeq.get(i).getActionTimeStamp()));
            plannedSplitJobInfoDetails.add(plannedSplitJobInfoDetail);
        }
        aPlannedSplitJobInfo.setPlannedSplitJobInfoDetails(plannedSplitJobInfoDetails);
        //Find Experimental Lot Info
        CimPlannedSplitJob aPlannedSplitJob = productManager.findPlannedSplitJobFor(lotFamilyID.getValue(), splitRouteID.getValue(), splitOperationNumber, originalRouteID.getValue(), originalOperationNumber);
        Boolean createFlag = false;
        Boolean modifyFlag = false;
        Boolean removeFlag = false;
        if (aPlannedSplitJob == null){
            // When the record does not exist, the record should be created.
            createFlag = true;
            aPlannedSplitJob = productManager.createPlannedSplitJob(aPlannedSplitJobInfo);
        }else {
            //When the record exists, the record should be updated.
            if (CimArrayUtils.getSize(plannedSplitJobInfoDetails) == CimArrayUtils.getSize(aPlannedSplitJob.getPlannedSplitJobInfoDetails())){
                //modify request updateReq
                modifyFlag = true;
            }else {
                if (CimStringUtils.isNotEmpty(removePsmKey)){
                    removeFlag = true;
                }
            }
            aPlannedSplitJob.setPlannedSplitJobInfo(aPlannedSplitJobInfo);
        }

        //runCard check start
        log.info("Psm update createFlag: {}",createFlag);
        log.info("Psm update modifyFlag: {}",modifyFlag);
        log.info("Psm update removeFlag: {}",removeFlag);
        ObjectIdentifier originalLotID = null;
        Boolean foundOneLotFlag = true;
        log.info("Setting new psmKey info by input param");
        runCardMethod.setPsmKeyFromPsmInfo(aPlannedSplitJobInfo);
        log.info("Find psm info exsit runCard setting");
        for (ProductDTO.PlannedSplitJobInfoDetail plannedSplitJobInfoDetail : aPlannedSplitJobInfo.getPlannedSplitJobInfoDetails()) {
            List<ObjectIdentifier> wafers = plannedSplitJobInfoDetail.getWafers();
            for (ObjectIdentifier wafer : wafers) {
                ObjectIdentifier lotID = waferMethod.waferLotGet(objCommon, wafer);
                if (null == originalLotID){
                    originalLotID = lotID;
                }
                if (!ObjectIdentifier.equalsWithValue(originalLotID, lotID)){
                    foundOneLotFlag = false;
                    break;
                }
            }
        }
        if (ObjectIdentifier.isNotEmptyWithValue(originalLotID) && CimBooleanUtils.isTrue(foundOneLotFlag)){
            //Check runCard and update runCard info
            log.info("Check runCard and update runCard info by lotID: {}",originalLotID);
            Infos.RunCardInfo runCardInfo = runCardMethod.getRunCardFromLotID(originalLotID);
            if (null != runCardInfo){
                log.info("RunCard info exsit");
                List<Infos.RunCardPsmInfo> runCardPsmInfos = new ArrayList<>();
                if (CimBooleanUtils.isTrue(createFlag)){
                    //Psm update create action, runCard should create a new psmJobID
                    log.info("Psm update create action, runCard should create a new psmJobID");
                }else {
                    if (CimBooleanUtils.isTrue(modifyFlag)){
                        log.info("Psm update modify action");
                        if (CimStringUtils.isNotEmpty(modifyPsmKey)){
                            List<Object[]> psmDocList = cimJpaRepository.query("SELECT * FROM RUNCARD_PSM_DOC WHERE PSM_KEY = ?1", modifyPsmKey);
                            //Check the psm setting doc or not
                            log.info("Check the psm setting doc or not");
                            Validations.check(CimArrayUtils.isNotEmpty(psmDocList),retCodeConfigEx.getCannotModifyPsm());
                        }
                        //Delete from RUNCARD_PSM and RUNCARD_PSM_DOC by changePsm key
                        log.info("Delete from RUNCARD_PSM and RUNCARD_PSM_DOC by changePsm key");
                        if (!CimArrayUtils.isEmpty(originalPsmKeys)) {
                            for (String originalPsmKey : originalPsmKeys) {
                                runCardMethod.removeRunCardPsmDocFromPsmKey(objCommon, originalPsmKey, null, removeFlag, runCardInfo.getRunCardID());
                            }
                        }
                    }else if (CimBooleanUtils.isTrue(removeFlag)){
                        log.info("Psm update delete action");
                        //When the record exists, runCard should update one psmJobID with a new psmKey or modify a psmKey
                        log.info("When the record exists, runCard should update one psmJobID with a new psmKey or modify a psmKey");
                        //Must have changePsmKey from input param
                        log.info("Must have changePsmKey from input param");
                        Validations.check(CimArrayUtils.isEmpty(originalPsmKeys),retCodeConfig.getInvalidInputParam());
                        //Delete from RUNCARD_PSM and RUNCARD_PSM_DOC by changePsmkey
                        log.info("Delete from RUNCARD_PSM and RUNCARD_PSM_DOC by changePsm key");
                        for (String originalPsmKey : originalPsmKeys) {
                            if (CimStringUtils.isNotEmpty(removePsmKey)){
                                runCardMethod.removeRunCardPsmDocFromPsmKey(objCommon,originalPsmKey,removePsmKey,removeFlag,runCardInfo.getRunCardID());
                            }
                        }
                    }else {
                        //Normal delete from RUNCARD_PSM by changePsmkey
                        log.info("Normal delete from RUNCARD_PSM by changePsmkey");
                        for (String originalPsmKey : originalPsmKeys) {
                            runCardMethod.removeRunCardPsmDocFromPsmKey(objCommon,originalPsmKey,null,removeFlag,runCardInfo.getRunCardID());
                        }
                    }
                }
                for (ProductDTO.PlannedSplitJobInfoDetail plannedSplitJobInfoDetail : aPlannedSplitJobInfo.getPlannedSplitJobInfoDetails()) {
                    Infos.RunCardPsmInfo psmInfo = new Infos.RunCardPsmInfo();
                    String psmJobID = aPlannedSplitJob.getIdentifier();
                    psmInfo.setPsmDocInfos(new ArrayList<>());//no setting doc
                    psmInfo.setUpdateTime(CimDateUtils.getCurrentDateTimeWithDefault());
                    psmInfo.setCreateTime(CimDateUtils.getCurrentDateTimeWithDefault());
                    psmInfo.setPsmJobID(psmJobID);
                    psmInfo.setPsmKey(plannedSplitJobInfoDetail.getPsmKey());
                    runCardPsmInfos.add(psmInfo);
                }
                runCardInfo.setRunCardPsmDocInfos(runCardPsmInfos);
                runCardInfo.setUpdateTime(CimDateUtils.getCurrentDateTimeWithDefault());
                runCardMethod.updateRunCardInfo(objCommon,runCardInfo);
            }else {
                log.info("RunCard Info is NULL");
            }
        }
        //run Card check end
        return aPlannedSplitJob.getIdentifier();
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
    public void experimentalLotStateCheck(Infos.ObjCommon objCommon, ObjectIdentifier lotId) {

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
    public Outputs.ObjExperimentalLotInfoGetOut experimentalLotInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotId) {
        //Initialize
        Outputs.ObjExperimentalLotInfoGetOut out = new Outputs.ObjExperimentalLotInfoGetOut();

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
        com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainPD = aLot.getMainProcessDefinition();
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
                com.fa.cim.newcore.bo.pd.CimProcessDefinition anOrigMainPD = aBackupPo.getMainProcessDefinition();
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
        com.fa.cim.newcore.bo.product.CimPlannedSplitJob aPosPlannedSplitJobObject = productManager.findPlannedSplitJobFor(lotFamily, splitRoute, splitOpeNo, origRoute, origOpeNo);
        //------------------------------------------------------------------------//
        // PPT_SET_MSG_RC_KEY3
        // "Experimental Lot [%s] Object is not found. RouteID [%s], splitOperationNumber [%s]"
        //      %1:lotID.identifier, %2:routeID.identifier, %3:splitOperationNumber
        //------------------------------------------------------------------------//
        Validations.check(null == aPosPlannedSplitJobObject, retCodeConfig.getNotFoundExperimentalLotObj());//getNotFoundExperimentalLotObj()

        //Get Experimental Lot Info
        ProductDTO.PlannedSplitJobInfo aPosPlannedSplitJobInfo = aPosPlannedSplitJobObject.getPlannedSplitJobInfo();
        //Set output buffer
        Infos.ExperimentalLotInfo strInfo = new Infos.ExperimentalLotInfo();
        //add psmJobID for history
        strInfo.setPsmJobID(aPosPlannedSplitJobObject.getIdentifier());
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
        int nLotDetailInfoLen = CimArrayUtils.getSize(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails());
        log.info("nLotDetailInfoLen,{}",nLotDetailInfoLen);
        // Initialization and logging
        List<Long> operationSequenceNumberSeq = new ArrayList<>();
        log.info("###### Before sorting. ");
        for (int i = 0; i < nLotDetailInfoLen; i++) {
            operationSequenceNumberSeq.add(i,0L);
            log.info("##### Number,{}",i);
            log.info("##### branch route,{}",aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getSubRouteID().getValue());
            log.info("#####mergeOperationNumber,{}",aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getMergeOperationNumber());
        }
        Boolean noSortFlag = false;
        List<Infos.ExperimentalLotDetailInfo> strExperimentalLotDetailInfoSeq = new ArrayList<>();
        strInfo.setStrExperimentalLotDetailInfoSeq(strExperimentalLotDetailInfoSeq);
        for (int i = 0; i < nLotDetailInfoLen; i++) {
            /***********************************************************************/
            /*  Sort PSM detail information order by mergeOperationNumber.         */
            /*  ** If mergeOperationNumber is the same, then not change an order.  */
            /***********************************************************************/
            Inputs.ProcessOperationSequenceGetDRIn processOperationSequenceGetDRIn = new Inputs.ProcessOperationSequenceGetDRIn();
            processOperationSequenceGetDRIn.setRouteId(strInfo.getSplitRouteID());
            processOperationSequenceGetDRIn.setOperationNumber(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getMergeOperationNumber());
            try {
                Outputs.ObjProcessOperationSequenceGetDROut processOperationSequenceGetDROut = processMethod.processOperationSequenceGetDR(objCommon, processOperationSequenceGetDRIn);

                long tryingNum = processOperationSequenceGetDROut.getOperationSequenceNumber();
                Infos.ExperimentalLotDetailInfo tryingStruct = new Infos.ExperimentalLotDetailInfo();
                tryingStruct.setSubRouteID(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getSubRouteID());
                tryingStruct.setReturnOperationNumber(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getReturnOperationNumber());
                tryingStruct.setMergeOperationNumber(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getMergeOperationNumber());
                tryingStruct.setWaferIDs(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getWafers());
                tryingStruct.setMemo(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getMemo());
                tryingStruct.setDynamicFlag(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).isDynamicFlag());
                tryingStruct.setExecFlag(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).isExecutedFlag());
                tryingStruct.setActionTimeStamp(CimDateUtils.convertToSpecString(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getExecutedTimeStamp()));
                strExperimentalLotDetailInfoSeq.add(tryingStruct);
                for (int j = 0; j < nLotDetailInfoLen; j++) {
                    if (operationSequenceNumberSeq.get(j) < tryingNum) {
                        log.info("####, {} < {}", operationSequenceNumberSeq.get(j), tryingNum);
                        //Set operationSequence
                        Long tmpNum = operationSequenceNumberSeq.get(j);
                        operationSequenceNumberSeq.set(j, tryingNum);
                        tryingNum = tmpNum;
                        //set structure
                        Infos.ExperimentalLotDetailInfo tmpStruct = strExperimentalLotDetailInfoSeq.get(j);
                        strInfo.getStrExperimentalLotDetailInfoSeq().set(j,tryingStruct);
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
            List<Infos.ExperimentalLotDetailInfo> strExperimentalLotDetailInfoSeq1 = new ArrayList<>();
            strInfo.setStrExperimentalLotDetailInfoSeq(strExperimentalLotDetailInfoSeq1);
            for (int i = 0; i < nLotDetailInfoLen; i++) {
                Infos.ExperimentalLotDetailInfo experimentalLotDetailInfo = new Infos.ExperimentalLotDetailInfo();
                strExperimentalLotDetailInfoSeq1.add(i,experimentalLotDetailInfo);
                experimentalLotDetailInfo.setSubRouteID(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getSubRouteID());
                experimentalLotDetailInfo.setReturnOperationNumber(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getReturnOperationNumber());
                experimentalLotDetailInfo.setMergeOperationNumber(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getMergeOperationNumber());
                experimentalLotDetailInfo.setWaferIDs(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getWafers());
                experimentalLotDetailInfo.setMemo(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getMemo());
                experimentalLotDetailInfo.setDynamicFlag(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).isDynamicFlag());
                experimentalLotDetailInfo.setExecFlag(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).isExecutedFlag());
                experimentalLotDetailInfo.setActionTimeStamp(CimDateUtils.convertToSpecString(aPosPlannedSplitJobInfo.getPlannedSplitJobInfoDetails().get(i).getExecutedTimeStamp()));
            }
        }
        out.setStrExperimentalLotInfo(strInfo);
        //logging after sortin
        log.info("After sorting. ");
        int resultLen = CimArrayUtils.getSize(out.getStrExperimentalLotInfo().getStrExperimentalLotDetailInfoSeq());
        for (int i = 0; i < resultLen; i++) {
            log.info("##### Number, {}",i);
            log.info("##### branch route,  {}",out.getStrExperimentalLotInfo().getStrExperimentalLotDetailInfoSeq().get(i).getSubRouteID().getValue());
            log.info("##### mergeOperationNumber, {}",out.getStrExperimentalLotInfo().getStrExperimentalLotDetailInfoSeq().get(i).getMergeOperationNumber());
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
    public Outputs.ObjExperimentalLotActualInfoCreateOut experimentalLotActualInfoCreate(Infos.ObjCommon objCommon, ObjectIdentifier lotId, Infos.ExperimentalLotInfo strExperimentalLotInfo) {
        //init
        Outputs.ObjExperimentalLotActualInfoCreateOut out = new Outputs.ObjExperimentalLotActualInfoCreateOut();

        //Get Wafers of Lot Family
        List<Infos.WaferListInLotFamilyInfo> lotWaferStatusListDROut = lotMethod.lotWafersStatusListGetDR(objCommon, strExperimentalLotInfo.getLotFamilyID());

        List<Infos.WaferListInLotFamilyInfo> waferList = lotWaferStatusListDROut;
        int familyWaferLen = CimArrayUtils.getSize(waferList);
        //Check which wafers are specified as target
        //----- Actual Data -------//
        List<Infos.ExperimentalLotDetailInfo> strActualDataList = new ArrayList<>();
        //----- Defined Data -------//
        List<Infos.ExperimentalLotDetailInfo> strDefinedDataList = strExperimentalLotInfo.getStrExperimentalLotDetailInfoSeq();
        //----- History Data -------//
        Infos.ExperimentalLotDetailResultInfo strHistoryData = new Infos.ExperimentalLotDetailResultInfo();

        int subRouteCnt = 0;
        int subRouteLen = CimArrayUtils.getSize(strDefinedDataList);

        //History Data
        List<Infos.ExperimentalLotDetail> strExperimentalLotDetailSeq = new ArrayList<>();
        strHistoryData.setStrExperimentalLotDetailSeq(strExperimentalLotDetailSeq);

        for (int i = 0; i < subRouteLen; i++) {
            //History Data i
            Infos.ExperimentalLotDetail experimentalLotDetail = new Infos.ExperimentalLotDetail();
            strExperimentalLotDetailSeq.add(i,experimentalLotDetail);

            //Actual Data routeCnt
            Infos.ExperimentalLotDetailInfo experimentalLotDetailInfoActual = new Infos.ExperimentalLotDetailInfo();
            strActualDataList.add(subRouteCnt,experimentalLotDetailInfoActual);

            if (CimBooleanUtils.isTrue(strDefinedDataList.get(i).getExecFlag())){
                continue;
            }
            log.info("Check the sub route's wafers.{},{},{}",i,strDefinedDataList.get(i).getSubRouteID().getValue());
            int waferCnt = 0;
            int waferLen = CimArrayUtils.getSize(strDefinedDataList.get(i).getWaferIDs());

            int waferLackCnt = 0;
            int waferOmitCnt = 0;
            //History Data i
            List<Infos.ExperimentalLotWafer> strExperimentalLotWaferSeq = new ArrayList<>();
            experimentalLotDetail.setStrExperimentalLotWaferSeq(strExperimentalLotWaferSeq);

            //Actual Data
            List<ObjectIdentifier> waferIDs = new ArrayList<>();
            experimentalLotDetailInfoActual.setWaferIDs(waferIDs);
            for (int j = 0; j < waferLen; j++) {
                //History Data
                Infos.ExperimentalLotWafer experimentalLotWafer = new Infos.ExperimentalLotWafer();
                strExperimentalLotWaferSeq.add(j,experimentalLotWafer);

                int k = 0;
                for (k = 0; k < familyWaferLen; k++) {
                    if (CimStringUtils.equals(strDefinedDataList.get(i).getWaferIDs().get(j).getValue(), waferList.get(k).getWaferID().getValue())){
                        log.info("Check the wafer's condition.{}",i,j,strDefinedDataList.get(i).getWaferIDs().get(j).getValue());
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
            experimentalLotDetail.setSubRouteID(strDefinedDataList.get(i).getSubRouteID());
            experimentalLotDetail.setReturnOperationNumber(strDefinedDataList.get(i).getReturnOperationNumber());
            experimentalLotDetail.setMergeOperationNumber(strDefinedDataList.get(i).getMergeOperationNumber());
            experimentalLotDetail.setMemo(strDefinedDataList.get(i).getMemo());

            // No sub route data is set if some target wafer is lack

            if (waferLackCnt + waferOmitCnt == waferLen){
                log.info("All wafers of the sub route are not actual target.");
                continue;
            }else if ( waferLackCnt > 0 ){
                log.info("Some wafers of the sub route are not actual target.");
                throw new ServiceException(retCodeConfig.getPsmExecutionFail());
            }

            //Actual Data : Sub Route
            experimentalLotDetailInfoActual.setSubRouteID(strDefinedDataList.get(i).getSubRouteID());
            experimentalLotDetailInfoActual.setReturnOperationNumber(strDefinedDataList.get(i).getReturnOperationNumber());
            experimentalLotDetailInfoActual.setMergeOperationNumber(strDefinedDataList.get(i).getMergeOperationNumber());
            experimentalLotDetailInfoActual.setMemo(strDefinedDataList.get(i).getMemo());
            experimentalLotDetailInfoActual.setDynamicFlag(strDefinedDataList.get(i).getDynamicFlag());
            subRouteCnt++;
        }
        log.info("The number of sub route which is actual target.{}",subRouteCnt);
        //History Data : Base
        if(subRouteCnt == 0){
            strActualDataList = new ArrayList<>();
            strExperimentalLotDetailSeq = new ArrayList<>();
            strHistoryData.setStrExperimentalLotDetailSeq(strExperimentalLotDetailSeq);
        }else {
            //strActualDataList.length( subRouteCnt );
        }
        strHistoryData.setUserID(objCommon.getUser().getUserID());
        strHistoryData.setLotFamilyID(strExperimentalLotInfo.getLotFamilyID());
        strHistoryData.setSplitRouteID(strExperimentalLotInfo.getSplitRouteID());
        strHistoryData.setSplitOperationNumber(strExperimentalLotInfo.getSplitOperationNumber());
        strHistoryData.setOriginalRouteID(strExperimentalLotInfo.getOriginalRouteID());
        strHistoryData.setOriginalOperationNumber(strExperimentalLotInfo.getOriginalOperationNumber());
        strHistoryData.setActionEMail(strExperimentalLotInfo.getActionEMail());
        strHistoryData.setActionHold(strExperimentalLotInfo.getActionHold());

        //Output Data
        out.setLotFamilyId(strExperimentalLotInfo.getLotFamilyID());
        out.setSplitRouteId(strExperimentalLotInfo.getSplitRouteID());
        out.setSplitOperationNumber(strExperimentalLotInfo.getSplitOperationNumber());
        out.setOriginalRouteId(strExperimentalLotInfo.getOriginalRouteID());
        out.setOriginalOperationNumber(strExperimentalLotInfo.getOriginalOperationNumber());
        out.setActionEmail(strExperimentalLotInfo.getActionEMail());
        out.setActionHold(strExperimentalLotInfo.getActionHold());
        out.setActionSeparateHold(strExperimentalLotInfo.getActionSeparateHold());
        out.setActionCombineHold(strExperimentalLotInfo.getActionCombineHold());
        out.setTestMemo(strExperimentalLotInfo.getTestMemo());
        out.setStrExperimentalLotDetailInfoSeq(strActualDataList);
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
    public String experimentalLotInfoDelete(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber,String originalPsmKey,String runCardID) {

        //【step1】Find Experimental Lot Info When the record does not exist, the record is not deleted.
        com.fa.cim.newcore.bo.product.CimPlannedSplitJob aPosPlannedSplitJob = productManager.findPlannedSplitJobFor(lotFamilyID.getValue(), splitRouteID.getValue(), splitOperationNumber, originalRouteID.getValue(), originalOperationNumber);

        //runCard add data
        String psmID = aPosPlannedSplitJob.getIdentifier();
        List<ProductDTO.PlannedSplitJobInfoDetail> plannedSplitJobInfoDetails = aPosPlannedSplitJob.getPlannedSplitJobInfoDetails();
        //runCard add end

        if (CimObjectUtils.isEmpty(aPosPlannedSplitJob)){
            throw new ServiceException(retCodeConfig.getNotFoundExperimentalLotData());
        }

        //【step2】When the record exists, the record should be deleted.
        productManager.removePlannedSplitJobFor(lotFamilyID.getValue(),splitRouteID.getValue(),splitOperationNumber,originalRouteID.getValue(),originalOperationNumber);

        //check run card start
        if (CimStringUtils.isNotEmpty(runCardID)){
            log.info("Get runCard Info: {}",runCardID);
            Infos.RunCardInfo runCardInfo = runCardMethod.getRunCardInfo(objCommon, runCardID);
            if (null != runCardInfo){
                Validations.check(CimStringUtils.isEmpty(originalPsmKey),retCodeConfig.getInvalidInputParam());
                runCardMethod.removePsmDocRunCardInfo(objCommon,psmID,originalPsmKey,runCardInfo.getRunCardID());
            }
        }
        //Card check end
        return psmID;
    }

    @Override
    public List<Infos.ExperimentalLotInfo> experimentalLotListGetDR(Infos.ObjCommon strObjCommonIn, String lotFamily, String splitRoute, String splitOperationNumber, String originalRoute, String originalOperationNumber, Boolean execCheckFlag, Boolean detailRequireFlag) {

        // 【step1】:Initialize
        String hFRPLSPLITdTheTableMarker2;
        List<Infos.ExperimentalLotInfo> result = new ArrayList<>();
        Boolean runCardFlag = false;

        //【step2】:Get PSM Definition
        String HV_TMPBUFFER = "";
        String HV_BUFFER =
                "SELECT ID, LOTFAMILY_ID, " +
                        "   LOTFAMILY_RKEY, " +
                "           SPLIT_MAIN_PROCESS_ID, " +
                        "   SPLIT_MAIN_PROCESS_RKEY, " +
                        "   SPLIT_OPE_NO," +
                        "   ORIG_MAIN_PROCESS_ID, " +
                "           ORIG_MAIN_PROCESS_RKEY, " +
                        "   ORIG_OPE_NO, " +
                        "   MAIL_ACTION, " +
                        "   HOLD_ACTION, " +
                        "   TEST_MEMO, " +
                "           EXECUTED_FLAG, " +
                        "   EXECUTED_TIME, " +
                        "   LAST_MODIFY_TIME, " +
                        "   LAST_MODIFY_USER_ID," +
                        "   LAST_MODIFY_USER_RKEY," +
                        "   SEPARATE_ACTION," +
                        "   COMBINE_ACTION, " +
                        "   PSM_JOB_ID " +
                        "   FROM OMPSM ";
        if (CimStringUtils.isNotEmpty(lotFamily)) {
            HV_TMPBUFFER = String.format("WHERE LOTFAMILY_ID = '%s' ", lotFamily);
            HV_BUFFER += HV_TMPBUFFER;
        }
        if (CimStringUtils.isNotEmpty(splitRoute)) {
            if (HV_TMPBUFFER.length() > 0) {
                HV_TMPBUFFER = "";
                HV_TMPBUFFER = String.format("AND   SPLIT_MAIN_PROCESS_ID = '%s' ", splitRoute);
            } else {
                HV_TMPBUFFER = String.format("WHERE SPLIT_MAIN_PROCESS_ID = '%s' ", splitRoute);
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
                HV_TMPBUFFER = String.format("AND   ORIG_MAIN_PROCESS_ID = '%s' ", originalRoute);
            } else {
                HV_TMPBUFFER = String.format("WHERE ORIG_MAIN_PROCESS_ID = '%s' ", originalRoute);
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
        List<Infos.ExperimentalLotInfo> strDefList = new ArrayList<>();
        for (Object[] cimPlannedSplitDO : cimPlannedSplitDOList) {
            Infos.ExperimentalLotInfo experimentalLotInfo = new Infos.ExperimentalLotInfo();
            experimentalLotInfo.setLotFamilyID(ObjectIdentifier.build(CimObjectUtils.toString(cimPlannedSplitDO[1]), CimObjectUtils.toString(cimPlannedSplitDO[2])));
            experimentalLotInfo.setSplitRouteID(ObjectIdentifier.build(CimObjectUtils.toString(cimPlannedSplitDO[3]), CimObjectUtils.toString(cimPlannedSplitDO[4])));
            experimentalLotInfo.setSplitOperationNumber(CimObjectUtils.toString(cimPlannedSplitDO[5]));
            experimentalLotInfo.setOriginalRouteID(ObjectIdentifier.build(CimObjectUtils.toString(cimPlannedSplitDO[6]), CimObjectUtils.toString(cimPlannedSplitDO[7])));
            experimentalLotInfo.setOriginalOperationNumber(CimObjectUtils.toString(cimPlannedSplitDO[8]));
            experimentalLotInfo.setActionEMail(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitDO[9])));
            experimentalLotInfo.setActionHold(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitDO[10])));
            //add auto separate and combine  - jerry
            experimentalLotInfo.setActionSeparateHold(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitDO[17])));
            experimentalLotInfo.setActionCombineHold(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitDO[18])));
            experimentalLotInfo.setTestMemo(CimObjectUtils.toString(cimPlannedSplitDO[11]));
            experimentalLotInfo.setExecFlag(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitDO[12])));
            experimentalLotInfo.setActionTimeStamp(CimDateUtils.convertToSpecString((Timestamp) (cimPlannedSplitDO[13])));
            experimentalLotInfo.setModifyTimeStamp(CimDateUtils.convertToSpecString((Timestamp) (cimPlannedSplitDO[14])));
            experimentalLotInfo.setModifyUserID(ObjectIdentifier.build(CimObjectUtils.toString(cimPlannedSplitDO[15]), CimObjectUtils.toString(cimPlannedSplitDO[16])));
            experimentalLotInfo.setPsmJobID(CimObjectUtils.toString(cimPlannedSplitDO[19]));
            List<Infos.ExperimentalLotDetailInfo> strBranchRouteList = new ArrayList<>();
            experimentalLotInfo.setStrExperimentalLotDetailInfoSeq(strBranchRouteList);
            strDefList.add(experimentalLotInfo);
            if (CimBooleanUtils.isFalse(detailRequireFlag)) {
                continue;
            }
            String hFRPLSPLITdheSystemKey = CimObjectUtils.toString(cimPlannedSplitDO[0]);//ID

            //【step5】 Get Branch Route
            String sqlRoute = " SELECT  IDX_NO, " +
                    "                   SUB_PROCESS_ID, " +
                    "                   SUB_PROCESS_RKEY, " +
                    "                   RETURN_OPE_NO, " +
                    "                   MERGE_OPE_NO, MEMO, " +
                    "                   DYNAMIC_FLAG, " +
                    "                   EXECUTED_FLAG, " +
                    "                   EXECUTED_TIME " +
                    "           FROM    OMPSM_SUBPROC " +
                    "           WHERE   REFKEY = ? " +
                    "           ORDER BY IDX_NO";

            List<Object[]> cimPlannedSplitJobSubRouteDOList = cimJpaRepository.query(sqlRoute, hFRPLSPLITdheSystemKey);
            //【step6】Branch Route Data
            for (Object[] cimPlannedSplitJobSubRouteDO : cimPlannedSplitJobSubRouteDOList) {
                Infos.ExperimentalLotDetailInfo experimentalLotDetailInfo = new Infos.ExperimentalLotDetailInfo();
                experimentalLotDetailInfo.setSubRouteID(ObjectIdentifier.build(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[1]), CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[2])));
                experimentalLotDetailInfo.setReturnOperationNumber(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[3]));
                experimentalLotDetailInfo.setMergeOperationNumber(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[4]));
                experimentalLotDetailInfo.setMemo(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[5]));
                experimentalLotDetailInfo.setDynamicFlag(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[6])));
                experimentalLotDetailInfo.setExecFlag(CimBooleanUtils.getBoolean(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[7])));
                experimentalLotDetailInfo.setActionTimeStamp(CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[8]));

                //【step7】Get Wafer
                String subrtSeqNo = CimObjectUtils.toString(cimPlannedSplitJobSubRouteDO[0]);
                String hFRPLSPLITdTheTableMarker = "";
                hFRPLSPLITdTheTableMarker += subrtSeqNo;
                hFRPLSPLITdTheTableMarker2 = subrtSeqNo;

                //【step8】DECLATE : OMPSM_SUBPROC_WFR);
                String sqlWF = "SELECT  WAFER_ID, WAFER_RKEY" +
                        "       FROM    OMPSM_SUBPROC_WFR" +
                        "       WHERE   REFKEY = ?1" +
                        "       AND     LINK_MARKER = ?2" +
                        "       AND     (LINK_MARKER = ?3 " +
                        "                   OR " +
                        "                   LINK_MARKER = ?4) " +
                        "       ORDER BY IDX_NO";
                List<Object[]> cimPlannedSplitJobSubRouteWaferDOS = cimJpaRepository.query(sqlWF, hFRPLSPLITdheSystemKey,hFRPLSPLITdTheTableMarker,hFRPLSPLITdTheTableMarker,hFRPLSPLITdTheTableMarker2);

                //【step9】Wafer Data
                List<ObjectIdentifier> waferList = new ArrayList<>();
                for (Object[] cimPlannedSplitJobSubRouteWaferDO : cimPlannedSplitJobSubRouteWaferDOS) {
                    ObjectIdentifier objectIdentifier = new ObjectIdentifier();
                    objectIdentifier.setValue(CimObjectUtils.toString(cimPlannedSplitJobSubRouteWaferDO[0]));
                    objectIdentifier.setReferenceKey(CimObjectUtils.toString(cimPlannedSplitJobSubRouteWaferDO[1]));
                    waferList.add(objectIdentifier);
                }
                experimentalLotDetailInfo.setWaferIDs(waferList);
                strBranchRouteList.add(experimentalLotDetailInfo);
            }
        }
        result = strDefList;
        //run card check start
        //Check run card from wafer - lot - runRard
        log.info("Check run card from wafer - lot - runRard");
        for (Infos.ExperimentalLotInfo experimentalLotInfo : result) {
            if (CimArrayUtils.isNotEmpty(experimentalLotInfo.getStrExperimentalLotDetailInfoSeq())){
                for (Infos.ExperimentalLotDetailInfo experimentalLotDetailInfo : experimentalLotInfo.getStrExperimentalLotDetailInfoSeq()) {
                    if (CimArrayUtils.isNotEmpty(experimentalLotDetailInfo.getWaferIDs())){
                        Infos.RunCardInfo splitRunCardInfo = runCardMethod.getRunCardInfoByPsm(strObjCommonIn,experimentalLotInfo.getPsmJobID());
                        if (null != splitRunCardInfo){
                            runCardFlag = true;
                            Optional.ofNullable(splitRunCardInfo.getRunCardPsmDocInfos()).ifPresent(runCardPsmInfos -> {
                                runCardPsmInfos.forEach(runCardPsmInfo -> {
                                    log.info("Get runCard psmKey to approve the runCard psm operation");
                                    Infos.RunCardPsmKeyInfo runCardPsmKeyInfo = runCardMethod.getRunCardPsmKeyInfo(experimentalLotInfo.getSplitOperationNumber(),experimentalLotDetailInfo.getSubRouteID(),experimentalLotDetailInfo.getWaferIDs(),runCardPsmInfo.getPsmKey());
                                    if (null != runCardPsmKeyInfo){
                                        List<String> wafers = new ArrayList<>();
                                        experimentalLotDetailInfo.getWaferIDs().forEach(x -> wafers.add(x.getValue()));
                                        if (runCardPsmKeyInfo.getWaferList().equals(wafers)){
                                            experimentalLotDetailInfo.setPsmKey(runCardPsmInfo.getPsmKey());
                                        }
                                    }
                                });
                            });
                        }
                    }
                }
            }
            experimentalLotInfo.setRunCardFlag(runCardFlag);
        }
        //run card check end
        return result;
    }


}