package com.fa.cim.service.tms.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.idp.tms.api.TmsService;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.method.IDurableMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IStockerMethod;
import com.fa.cim.service.dispatch.IDispatchInqService;
import com.fa.cim.service.tms.ITransferManagementSystemInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 17:00
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class TransferManagementSystemInqServiceImpl implements ITransferManagementSystemInqService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private TmsService tmsService;

    @Autowired
    private IStockerMethod stockerMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IDispatchInqService dispatchInqService;

    @Autowired
    private IDurableMethod durableMethod;

    @Override
    public Results.WhereNextStockerInqResult sxWhereNextStockerInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier cassetteID){

        Results.WhereNextStockerInqResult whereNextStockerInqResult =new Results.WhereNextStockerInqResult();

        // step1 - cassette_destinationInfo_GetForSLM
        log.info("step1 - cassette_destinationInfo_GetForSLM");
        Outputs.CassetteDestinationInfoGetForSLMOut destinationInfoGetForSLMResult = null;
        try{
            destinationInfoGetForSLMResult = cassetteMethod.cassetteDestinationInfoGetForSLM(objCommon, cassetteID, lotID);
            BeanUtils.copyProperties(destinationInfoGetForSLMResult.getDestinationOrder(),whereNextStockerInqResult);
        }catch (ServiceException e) {
            if(!Validations.isEquals(retCodeConfig.getNotReservedCastSLM(), e.getCode())) {
                throw e;
            } else {
                log.info("step2 - cassette_destinationInfo_Get");
                Outputs.CassetteDestinationInfoGetOut cassetteDestinationInfoGetResult = cassetteMethod.cassetteDestinationInfoGet(objCommon, cassetteID, lotID);
                BeanUtils.copyProperties(cassetteDestinationInfoGetResult.getDestinationOrder(),whereNextStockerInqResult);
            }
        }
        return whereNextStockerInqResult;
    }


    @Override
    public Results.LotCassetteXferJobDetailResult sxCarrierTransferJobDetailInfoInq(Infos.ObjCommon objCommon, Params.CarrierTransferJobDetailInfoInqParam params) {

        //init
        Results.LotCassetteXferJobDetailResult out = new Results.LotCassetteXferJobDetailResult();
        Boolean detailFlag = params.getDetailFlag();
        String functionID = params.getFunctionID();
        ObjectIdentifier inquiryKey = params.getInquiryKey();
        String inquiryType = params.getInquiryType();

        /*----------------------------*/
        /*   Set data                 */
        /*----------------------------*/
        log.info("Set data");
        Infos.TransportJobInq transportJobInq = new Infos.TransportJobInq();
        transportJobInq.setInquiryType(inquiryType);
        if (CimStringUtils.equals(inquiryType,"C")){
            transportJobInq.setCarrierID(inquiryKey);
        }else if (CimStringUtils.equals(inquiryType,"T")){
            transportJobInq.setToMachineID(inquiryKey);
        }else if (CimStringUtils.equals(inquiryType,"F")){
            transportJobInq.setFromMachineID(inquiryKey);
        }else {
            log.info("*** unkown inquiryType:{}",inquiryType);
        }

        /*-------------------------------------------------------*/
        /*   Request to TMS to Inquire Transfer Job Information   */
        /*-------------------------------------------------------*/
        log.info("Request to TMS to Inquire Transfer Job Information");
        //【step-1】: TMSMgr_SendTransportJobInq
        Inputs.SendTransportJobInqIn in = new Inputs.SendTransportJobInqIn();
        in.setStrObjCommonIn(objCommon);
        in.setUser(objCommon.getUser());
        in.setTransportJobInq(transportJobInq);
        in.setFunctionID(functionID);
        Outputs.SendTransportJobInqOut sendTransportJobInqOutRetCode = tmsService.transportJobInq(in);


        /*----------------------------*/
        /*   Set Returned Structure   */
        /*----------------------------*/
        int nILen = CimArrayUtils.getSize(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData());
        List<Results.JobResult> strJobResult = new ArrayList<>();
        out.setStrJobResult(strJobResult);
        for (int i = 0; i < nILen; i++) {
            Results.JobResult jobResult = new Results.JobResult();
            strJobResult.add(i,jobResult);
            jobResult.setJobID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getJobID());
            jobResult.setJobStatus(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getJobStatus());
            jobResult.setTransportType(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getTransportType());
            int nJLen = CimArrayUtils.getSize(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo());
            List<Infos.CarrierJobResult> strCarrierJobResult = new ArrayList<>();
            jobResult.setStrCarrierJobResult(strCarrierJobResult);
            for (int j = 0; j < nJLen; j++) {
                Infos.CarrierJobResult carrierJobResult = new Infos.CarrierJobResult();
                strCarrierJobResult.add(j,carrierJobResult);
                carrierJobResult.setCarrierJobID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getCarrierJobID());
                carrierJobResult.setCarrierJobStatus(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getCarrierJobStatus());
                carrierJobResult.setCarrierID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getCarrierID());
                carrierJobResult.setZoneType(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getZoneType());
                carrierJobResult.setN2PurgeFlag(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).isN2PurgeFlag());
                carrierJobResult.setFromMachineID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getFromMachineID());
                carrierJobResult.setFromPortID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getFromPortID());
                carrierJobResult.setToStockerGroup("");
                carrierJobResult.setToMachine(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getToMachineID());
                carrierJobResult.setToPortID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getToPortID());
                carrierJobResult.setExpectedStartTime(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getExpectedStartTime());
                carrierJobResult.setExpectedEndTime(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getExpectedEndTime());
                carrierJobResult.setMandatoryFlag(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).isMandatoryFlag());
                carrierJobResult.setPriority(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getPriority());
                carrierJobResult.setEstimatedStartTime(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getEstimatedStartTime());
                carrierJobResult.setEstimatedEndTime(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getEstimatedEndTime());
                carrierJobResult.setSiInfo(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getSiInfo());
            }
            jobResult.setSiInfo(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getSiInfo());
        }
        out.setSiInfo(sendTransportJobInqOutRetCode.getSiInfo());
        return out;
    }

    @Override
    public Results.CarrierTransferJobInfoInqResult sxCarrierTransferJobInfoInq(Infos.ObjCommon objCommon, Params.CarrierTransferJobInfoInqParam params) {

        //init
        Results.CarrierTransferJobInfoInqResult out = new Results.CarrierTransferJobInfoInqResult();
        Boolean detailFlag = params.getDetailFlag();
        ObjectIdentifier inquiryKey = params.getInquiryKey();
        String inquiryType = params.getInquiryType();

        /*----------------------------*/
        /*   Set data                 */
        /*----------------------------*/
        log.info("Set data");
        Infos.TransportJobInq transportJobInq = new Infos.TransportJobInq();
        transportJobInq.setInquiryType(inquiryType);
        if (CimStringUtils.equals(inquiryType,"C")){
            transportJobInq.setCarrierID(inquiryKey);
        }else if (CimStringUtils.equals(inquiryType,"T")){
            transportJobInq.setToMachineID(inquiryKey);
        }else if (CimStringUtils.equals(inquiryType,"F")){
            transportJobInq.setFromMachineID(inquiryKey);
        }else {
            log.info("unkown inquiryType:{}",inquiryType);
        }
        /*-------------------------------------------------------*/
        /*   Request to TM to Inquire Transfer Job Information   */
        /*-------------------------------------------------------*/
        log.info("Request to TMS to Inquire Transfer Job Information");
        //【step-1】: XMSMgr_SendTransportJobInq
        Inputs.SendTransportJobInqIn in = new Inputs.SendTransportJobInqIn();
        in.setStrObjCommonIn(objCommon);
        in.setTransportJobInq(transportJobInq);
        in.setFunctionID("");
        in.setUser(objCommon.getUser());
        Outputs.SendTransportJobInqOut sendTransportJobInqOutRetCode = tmsService.transportJobInq(in);

        /*----------------------------*/
        /*   Set Returned Structure   */
        /*----------------------------*/
        log.info("Set Returned Structure");
        int nILen = CimArrayUtils.getSize(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData());
        List<Results.JobResult> strJobResult = new ArrayList<>();
        out.setStrJobResult(strJobResult);
        for (int i = 0; i < nILen; i++) {
            Results.JobResult jobResult = new Results.JobResult();
            strJobResult.add(i,jobResult);
            jobResult.setJobID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getJobID());
            jobResult.setJobStatus(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getJobStatus());
            jobResult.setTransportType(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getTransportType());
            int nJLen = CimArrayUtils.getSize(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo());
            List<Infos.CarrierJobResult> strCarrierJobResult = new ArrayList<>();
            jobResult.setStrCarrierJobResult(strCarrierJobResult);
            for (int j = 0; j < nJLen; j++) {
                Infos.CarrierJobResult carrierJobResult = new Infos.CarrierJobResult();
                strCarrierJobResult.add(j,carrierJobResult);
                carrierJobResult.setCarrierJobID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getCarrierJobID());
                carrierJobResult.setCarrierJobStatus(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getCarrierJobStatus());
                carrierJobResult.setCarrierID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getCarrierID());
                carrierJobResult.setZoneType(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getZoneType());
                carrierJobResult.setN2PurgeFlag(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).isN2PurgeFlag());
                carrierJobResult.setFromMachineID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getFromMachineID());
                carrierJobResult.setFromPortID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getFromPortID());
                carrierJobResult.setToStockerGroup("");
                carrierJobResult.setToMachine(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getToMachineID());
                carrierJobResult.setToPortID(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getToPortID());
                carrierJobResult.setExpectedStartTime(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getExpectedStartTime());
                carrierJobResult.setExpectedEndTime(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getExpectedEndTime());
                carrierJobResult.setMandatoryFlag(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).isMandatoryFlag());
                carrierJobResult.setPriority(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getPriority());
                carrierJobResult.setEstimatedStartTime(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getEstimatedStartTime());
                carrierJobResult.setEstimatedEndTime(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getEstimatedEndTime());
                carrierJobResult.setSiInfo(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getCarrierJobInqInfo().get(j).getSiInfo());
            }
            jobResult.setSiInfo(sendTransportJobInqOutRetCode.getStrTransportJobInqResult().getJobInqData().get(i).getSiInfo());
        }
        out.setSiInfo(sendTransportJobInqOutRetCode.getSiInfo());
        return out;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param machineID
     * @param detailFlag
     * @return
     * @author Ho
     * @date 2018/9/26 14:18:06
     */
    @Override
    public Results.StockerInfoInqResult sxStockerInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier machineID, boolean detailFlag) {

        Results.StockerInfoInqResult stockerInfoInqResult=new Results.StockerInfoInqResult();

        // step1 - stocker_type_GetDR
        Outputs.ObjStockerTypeGetDROut stockerTypeResult = stockerMethod.stockerTypeGet(objCommon, machineID);

        if(CimArrayUtils.binarySearch(new String[]{
                BizConstant.SP_STOCKER_TYPE_INTERM,
                BizConstant.SP_STOCKER_TYPE_RETICLE,
                BizConstant.SP_STOCKER_TYPE_INTERBAY,
                BizConstant.SP_STOCKER_TYPE_INTRABAY,
                BizConstant.SP_STOCKER_TYPE_RETICLEPOD,
                BizConstant.SP_STOCKER_TYPE_BARERETICLE,
                BizConstant.SP_STOCKER_TYPE_RETICLESHELF
        },stockerTypeResult.getStockerType())){
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidStockerType(), stockerTypeResult.getStockerType()));
        }

        // step2 - stocker_baseInfo_Get__090
        Results.StockerInfoInqResult stockerBaseInfoRetCode = stockerMethod.stockerBaseInfoGet(objCommon, machineID);

        if(CimArrayUtils.binarySearch(new String[]{
                BizConstant.SP_STOCKER_TYPE_SHELF,
                BizConstant.SP_STOCKER_TYPE_AUTO,
                BizConstant.SP_STOCKER_TYPE_INTERM,
        },stockerTypeResult.getStockerType())){
            // step3 - cassette_FillInTxLGQ004DR
            Results.StockerInfoInqResult fillInTxLGQ004DRResult = cassetteMethod.cassetteFillInTxLGQ004DR(objCommon, machineID);
            if(fillInTxLGQ004DRResult != null){
                stockerInfoInqResult.setStrCarrierInStocker(fillInTxLGQ004DRResult.getStrCarrierInStocker());
            }
            if(detailFlag){
                // step4 - call TMSMgr_SendStockerDetailInfoInq

                Outputs.SendStockerDetailInfoInqOut strXMSMgrSendStockerDetailInfoInqOut;
                Inputs.StockerDetailInfoInq stockerDetailInfoInq = new Inputs.StockerDetailInfoInq();
                stockerDetailInfoInq.setMachineID( machineID);
                strXMSMgrSendStockerDetailInfoInqOut = tmsService.stockerDetailInfoInq(objCommon,
                        objCommon.getUser(), stockerDetailInfoInq);

                stockerInfoInqResult.setStockerID        ( stockerBaseInfoRetCode.getStockerID());
                stockerInfoInqResult.setStockerType      ( stockerBaseInfoRetCode.getStockerType());
                stockerInfoInqResult.setDescription      ( stockerBaseInfoRetCode.getDescription());

                stockerInfoInqResult.setE10Status        ( strXMSMgrSendStockerDetailInfoInqOut.getStrStockerDetailInfoInqResult().getE10Status());
                stockerInfoInqResult.setResourceInfoData ( strXMSMgrSendStockerDetailInfoInqOut.getStrStockerDetailInfoInqResult().getResourceInfoData());
                stockerInfoInqResult.setZoneInfoData     ( strXMSMgrSendStockerDetailInfoInqOut.getStrStockerDetailInfoInqResult().getZoneInfoData());
                stockerInfoInqResult.setSiInfo           ( strXMSMgrSendStockerDetailInfoInqOut.getStrStockerDetailInfoInqResult().getSiInfo());
            }else{
                stockerInfoInqResult.setResourceInfoData(stockerBaseInfoRetCode.getResourceInfoData());
                stockerInfoInqResult.setStockerID(stockerBaseInfoRetCode.getStockerID());
                stockerInfoInqResult.setStockerType(stockerBaseInfoRetCode.getStockerType());
                stockerInfoInqResult.setDescription(stockerBaseInfoRetCode.getDescription());
                stockerInfoInqResult.setE10Status(stockerBaseInfoRetCode.getE10Status());
                stockerInfoInqResult.setStockerStatusCode(stockerBaseInfoRetCode.getStockerStatusCode());
                stockerInfoInqResult.setStatusName(stockerBaseInfoRetCode.getStatusName());
                stockerInfoInqResult.setStatusDescription(stockerBaseInfoRetCode.getStatusDescription());
                stockerInfoInqResult.setStatusChangeTimeStamp(stockerBaseInfoRetCode.getStatusChangeTimeStamp());
                stockerInfoInqResult.setActualE10Status(stockerBaseInfoRetCode.getActualE10Status());
                stockerInfoInqResult.setActualStatusCode(stockerBaseInfoRetCode.getActualStatusCode());
                stockerInfoInqResult.setActualStatusName(stockerBaseInfoRetCode.getActualStatusName());
                stockerInfoInqResult.setActualStatusDescription(stockerBaseInfoRetCode.getActualStatusDescription());
                stockerInfoInqResult.setActualStatusChangeTimeStamp(stockerBaseInfoRetCode.getActualStatusChangeTimeStamp());
            }

            stockerInfoInqResult.setUtsFlag(stockerBaseInfoRetCode.getUtsFlag());
            stockerInfoInqResult.setMaxUTSCapacity(stockerBaseInfoRetCode.getMaxUTSCapacity());

        }

        return stockerInfoInqResult;
    }

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
     * @return
     * @author Ho
     * @date 2018/9/26 14:18:06
     */
    @Override
    public Results.StockerListInqResult sxStockerListInq(Infos.ObjCommon objCommon, String stockerType, boolean availFlag) {

        // step1 - stocker_FillInTxLGQ003DR
        return stockerMethod.stockerListInfoGetDR(objCommon,stockerType,availFlag);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strWhereNextOHBCarrierInqInParm
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @exception
     * @author ho
     * @date 2020/3/17 13:37
     */
    @Override
    public ObjectIdentifier sxWhereNextOHBCarrierInq (Infos.ObjCommon strObjCommonIn, Params.WhereNextOHBCarrierInqInParm strWhereNextOHBCarrierInqInParm) {
        /*---------------------------------------------*/
        /*  Get carrier UTS information                */
        /*---------------------------------------------*/
        ObjectIdentifier strCassetteUTSInfoGetOut ,stockerID = null;
        // step-1 cassette_UTSInfo_Get

        try {
            stockerID=strCassetteUTSInfoGetOut = cassetteMethod.cassetteUTSInfoGet(
                    strObjCommonIn,
                    strWhereNextOHBCarrierInqInParm.getCassetteID() );
        } catch (ServiceException ex){
            if (!Validations.isEquals(ex.getCode(),retCodeConfigEx.getCarrierNotInUts())){
                throw ex;
            }
            /*------------------------------------------------*/
            /*  Check whether a carrier is transfering to UTS */
            /*------------------------------------------------*/
            Infos.CarrierJobResult strCassetteTransferJobRecordGetDROut;
            try {
                strCassetteTransferJobRecordGetDROut = cassetteMethod.cassetteTransferJobRecordGetDR(
                        strObjCommonIn,
                        strWhereNextOHBCarrierInqInParm.getCassetteID() );
            } catch (ServiceException ex1){
                if( Validations.isEquals(ex1.getCode(),retCodeConfig.getCarrierNotTransfering()) ) {
                    log.info("The Carrier is not transfering. Return...");
                    Validations.check(true,retCodeConfigEx.getCarrierNotInUts());
                }
                throw ex1;
            }

            Outputs.ObjStockerTypeGetDROut strStockerTypeGetDROut;
            try {
                strStockerTypeGetDROut = stockerMethod.stockerTypeGetDR(
                        strObjCommonIn,
                        strCassetteTransferJobRecordGetDROut.getToMachine() );
            } catch (ServiceException ex1){
                if (Validations.isEquals(ex1.getCode(),retCodeConfig.getUndefinedStockerType())){
                    Validations.check(true,retCodeConfigEx.getCarrierNotInUts());
                }
                throw ex1;
            }

            if(strStockerTypeGetDROut.isUtsFlag()) {
                stockerID = strCassetteTransferJobRecordGetDROut.getToMachine() ;
            } else {
                log.info( "The Stocker is not UTS. Return...");
                Validations.check(true,retCodeConfigEx.getCarrierNotInUts());
            }
        }

        log.info("UTS ID = {}", stockerID.getValue() );
        /*---------------------------------------------*/
        /*  Get related equipment list of the UTS      */
        /*---------------------------------------------*/
        List<Infos.StockerEqp> strStockerUTSRelatedEquipmentListGetDROut,strStockerEqpSeq ;
        // step-2 stockerUTS_relatedEquipmentList_GetDR
        strStockerEqpSeq=strStockerUTSRelatedEquipmentListGetDROut = stockerMethod.stockerUTSRelatedEquipmentListGetDR(
                strObjCommonIn,
                stockerID );

        int relatedEqpLen = CimArrayUtils.getSize(strStockerEqpSeq);

        if (relatedEqpLen == 0) {
            Validations.check(true,retCodeConfig.getNotFoundAvailStk());
        }

        /*-------------------------------*/
        /*  Get Lot list in carrier      */
        /*-------------------------------*/
        Infos.LotListInCassetteInfo strCassetteLotListGetDROut,strLotListInCassetteInfo;
        strLotListInCassetteInfo=strCassetteLotListGetDROut = cassetteMethod.cassetteLotListGetDR(
                strObjCommonIn,
                strWhereNextOHBCarrierInqInParm.getCassetteID() );

        /*---------------------------*/
        /*  Check carrier status     */
        /*---------------------------*/
        List<ObjectIdentifier> lotIDs;
        lotIDs = strCassetteLotListGetDROut.getLotIDList();
        int lotLen = CimArrayUtils.getSize(lotIDs);
        Boolean remainCurrentUTSFlag = FALSE;
        Boolean RTDInterfaceOn = TRUE;

        /***************************************************************************/
        /*  Check Lot availability for equipment.                                  */
        /*  If the cassette has following situation, then check will not be done.  */
        /*   1. Carrier includes scrap wafer.                                      */
        /*   2. Carrier status is not available.                                   */
        /*   3. Carrier is empty.                                                  */
        /*   4. All Lots are Held (or BankIn)                                      */
        /***************************************************************************/
        ObjectIdentifier strWhereNextOHBCarrierInqResult = null;
        if ( CimBooleanUtils.isTrue(strWhereNextOHBCarrierInqInParm.getWhatNextInqFlag()) &&
                0 < lotLen ) {
            /*------------------------------------------------------------------------*/
            /*  Check at least one Lot in input carrier is WIP Lot of the equipment   */
            /*------------------------------------------------------------------------*/
            for (int i = 0 ; i < relatedEqpLen ; i++) {
                if (CimBooleanUtils.isTrue(strStockerUTSRelatedEquipmentListGetDROut.get(i).getAvailableStateFlag())) {
                    Results.WhatNextLotListResult strWhatNextInqResult;

                    Outputs.ObjCassetteDeliveryRTDInterfaceReqOut strCassetteDeliveryRTDInterfaceReqOut;
                    try {
                        strCassetteDeliveryRTDInterfaceReqOut = cassetteMethod.cassetteDeliveryRTDInterfaceReq(
                                strObjCommonIn,
                                BizConstant.SP_RTD_FUNCTION_CODE_WHATNEXT,
                                strStockerUTSRelatedEquipmentListGetDROut.get(i).getEquipmentID() );
                        strWhatNextInqResult=strCassetteDeliveryRTDInterfaceReqOut.getStrWhatNextInqResult();
                    } catch (ServiceException ex){
                        log.info( "Faild RTD Interface -----> Call Normal Function (txWhatNextInq)");
                        /*-------------------------------------------------*/
                        /*   Call Normal Function (txWhatNextInq)   */
                        /*-------------------------------------------------*/

                        Params.WhatNextLotListParams whatNextLotListParams=new Params.WhatNextLotListParams();
                        whatNextLotListParams.setEquipmentID(strStockerUTSRelatedEquipmentListGetDROut.get(i).getEquipmentID());
                        whatNextLotListParams.setSelectCriteria(ObjectIdentifier.buildWithValue(BizConstant.SP_DP_SELECTCRITERIA_AUTO3) );
                        // step - txWhatNextInq

                        try {
                            strWhatNextInqResult = dispatchInqService.sxWhatNextLotListInfo(
                                    strObjCommonIn,
                                    whatNextLotListParams );
                        } catch (ServiceException ex1){
                            if (!Validations.isEquals(ex1.getCode(),retCodeConfigEx.getNoWipLot())){
                                throw ex1;
                            }
                            continue;
                        }
                    }

                    int whatNextLotLen = CimArrayUtils.getSize(strWhatNextInqResult.getStrWhatNextAttributes());
                    for ( int j = 0 ; j < whatNextLotLen ; j++ ) {
                        if (CimStringUtils.equals(strWhatNextInqResult.getStrWhatNextAttributes().get(j).getCassetteID().getValue(),
                                strWhereNextOHBCarrierInqInParm.getCassetteID().getValue()) ) {
                            remainCurrentUTSFlag = TRUE;
                            strWhereNextOHBCarrierInqResult = stockerID;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(remainCurrentUTSFlag)) {
                        break;
                    }
                }
            }
        }

        if (CimBooleanUtils.isFalse(remainCurrentUTSFlag) ) {
            Boolean availableStockerExistFlag = FALSE;
            Boolean foundPhysicalStockerFlag = FALSE;
            ObjectIdentifier firstStocker = null;
            for (int i = 0 ; i < relatedEqpLen ; i++) {
                /*-------------------------------------------------*/
                /*   Call equipment related stocker information    */
                /*-------------------------------------------------*/

                Infos.EqpStockerInfo strEquipmentStockerInfoGetOut;
                // step equipment_stockerInfo_Get
                strEquipmentStockerInfoGetOut = equipmentMethod.equipmentStockerInfoGetDR(
                        strObjCommonIn,
                        strStockerUTSRelatedEquipmentListGetDROut.get(i).getEquipmentID() );

                Infos.EqpStockerInfo equipmentStockerInfo;
                equipmentStockerInfo = strEquipmentStockerInfoGetOut;
                int stkLen = CimArrayUtils.getSize(equipmentStockerInfo.getEqpStockerStatusList());

                for ( int j = 0 ; j < stkLen ; j++ ) {
                    /*---------------------------------*/
                    /*   Check stocker availability    */
                    /*---------------------------------*/
                    Boolean strMachineStatAvailabilityCheckOut;
                    if( 0 < lotLen ) {
                        /*----------------------------------------------------------------------------*/
                        /*   Check conditional availability for all Lots in Cassette.                 */
                        /*   If any one good Lot is found in Cassette, then it is judged available.   */
                        /*----------------------------------------------------------------------------*/
                        for( int lotCnt = 0 ; lotCnt < lotLen ; lotCnt++ ) {
                            // step machineState_availability_Check
                            strMachineStatAvailabilityCheckOut = equipmentMethod.machineStateAvailabilityCheck(
                                    strObjCommonIn,
                                    equipmentStockerInfo.getEqpStockerStatusList().get(j).getStockerStatus(),
                                    lotIDs.get(lotCnt) );

                            if ( CimBooleanUtils.isTrue(strMachineStatAvailabilityCheckOut) ) {
                                strWhereNextOHBCarrierInqResult = equipmentStockerInfo.getEqpStockerStatusList().get(j).getStockerID();
                                availableStockerExistFlag = TRUE;
                                break;
                            }
                        }
                    } else {
                        ObjectIdentifier dummyLot=null;
                        // step machineState_availability_Check
                        strMachineStatAvailabilityCheckOut = equipmentMethod.machineStateAvailabilityCheck(
                                strObjCommonIn,
                                equipmentStockerInfo.getEqpStockerStatusList().get(j).getStockerStatus(),
                                dummyLot );

                        if ( CimBooleanUtils.isTrue(strMachineStatAvailabilityCheckOut) ) {
                            strWhereNextOHBCarrierInqResult = equipmentStockerInfo.getEqpStockerStatusList().get(j).getStockerID();
                            availableStockerExistFlag = TRUE;
                            break;
                        }
                    }

                    if ( CimBooleanUtils.isTrue(availableStockerExistFlag) ){
                        break;
                    }

                    /*--------------------------------------------------------*/
                    /*  Keep first stocker for all stocker status down case   */
                    /*--------------------------------------------------------*/
                    if ( CimBooleanUtils.isFalse(foundPhysicalStockerFlag) ) {
                        firstStocker = equipmentStockerInfo.getEqpStockerStatusList().get(j).getStockerID();
                        foundPhysicalStockerFlag = TRUE ;
                    }
                }

                if ( CimBooleanUtils.isFalse(availableStockerExistFlag) ){
                    break;
                }
            }

            if ( CimBooleanUtils.isFalse(availableStockerExistFlag) ) {
                /*---------------------------------------------------------------*/
                /*   Return error if any stocker could not found physically.     */
                /*---------------------------------------------------------------*/
                if( CimBooleanUtils.isFalse(foundPhysicalStockerFlag) ) {
                    Validations.check(true,retCodeConfig.getNotFoundAvailStk());
                } else {
                    /*-----------------------------------------------------------------------------*/
                    /*   Set first priority Stocker if all Stockers are not available logically.   */
                    /*-----------------------------------------------------------------------------*/
                    strWhereNextOHBCarrierInqResult = firstStocker ;
                }
            }
        }

        /*------------------------------------------------------------------------*/
        /*   Return                                                               */
        /*------------------------------------------------------------------------*/
        return strWhereNextOHBCarrierInqResult;
    }

    @Override
    public List<ObjectIdentifier> sxAllEqpForAutoTransferInq(Infos.ObjCommon objCommon) {
        // step1 - availableEquipment_GetByModeAndStatusDR
        return equipmentMethod.availableEquipmentGetByModeAndStatusDR(objCommon);
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param lotID
     * @param cassetteID
     * @param durableID
     * @return com.fa.cim.dto.RetCode<Results.EqpForAutoTransferInqResult>
     * @author Ho
     * @date 2018/10/11 14:11:37
     */
    @Override
    public List<ObjectIdentifier> sxEqpForAutoTransferInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier lotID, ObjectIdentifier cassetteID, ObjectIdentifier durableID) {

        // step1 - availableEquipment_GetForDeliveryReqDR
        return equipmentMethod.availableEquipmentGetForDeliveryReqDR(objCommon, equipmentID, lotID, cassetteID, durableID);
    }

    @Override
    public List<Infos.AvailableStocker> sxStockerForAutoTransferInq(Infos.ObjCommon objCommon) {

        // step1 - stocker_FillInTxLGQ002DR
        return stockerMethod.stockerFillInTxLGQ002DR(objCommon);
    }

    @Override
    public Results.DurableWhereNextStockerInqResult sxDurableWhereNextStockerInq(Infos.ObjCommon objCommon, ObjectIdentifier durableID) {
        //【step1】 - durableDestinationInfoGet
        return durableMethod.durableDestinationInfoGet(objCommon, durableID);
    }
}
