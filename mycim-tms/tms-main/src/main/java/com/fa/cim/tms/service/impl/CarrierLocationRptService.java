package com.fa.cim.tms.service.impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.tms.common.enums.EnvCodeEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.method.ICassetteMethod;
import com.fa.cim.tms.method.IEnvMethod;
import com.fa.cim.tms.method.IEquipmentMethod;
import com.fa.cim.tms.method.ITransferJobMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.service.ICarrierLocationRptService;
import com.fa.cim.tms.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 13:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class CarrierLocationRptService implements ICarrierLocationRptService {

    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private IEnvMethod envMethod;
    @Autowired
    private ITransferJobMethod transferJobMethod;
    @Autowired
    private IEquipmentMethod equipmentMethod;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;
    @Autowired
    private ICassetteMethod cassetteMethod;

    public Results.CarrierLocationReportResult sxCarrierLocationRpt(Infos.ObjCommon objCommon, Params.CarrierLocationReportParmas carrierLocationReportParmas) {
        String reticleXferFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_RETICLE_LOGIC_AVAILABLE.getValue());//"NO"
        if (StringUtils.isEmpty(reticleXferFlag)) {
            reticleXferFlag = Constant.TM_YES;
        }
        Boolean castTxNoSendWarning = false;
        Results.CarrierLocationReportResult result = new Results.CarrierLocationReportResult();
        /*-----------------------------------------------------------*/
        /*  Send Location Report to OMS                              */
        /*  If SP_RETICLE_XFER_AVAILABLE != "NO" (by D5100250) and   */
        /*  heading byte is equal to 'R',then it is Reticle event    */
        /*  In Reticle Case,carrierID=PodID(8byte)+ReticleID(20byte) */
        /*-----------------------------------------------------------*/
        Params.CarrierLocationReportParmas tempCarrierLocationReport = new Params.CarrierLocationReportParmas();
        BeanUtils.copyProperties(carrierLocationReportParmas,tempCarrierLocationReport);

        if (Constant.TM_YES.equals(reticleXferFlag) && !ObjectUtils.isEmptyWithValue(tempCarrierLocationReport.getCarrierID()) && Constant.TM_STRING_R.equals(tempCarrierLocationReport.getCarrierID().getValue().substring(0, 1))) {
            log.info("carrierID[0] = 'R'");
            /*** ReticlePod****/
            Infos.XferReticlePod xferReticlePod = new Infos.XferReticlePod();
            String reticlePodIDOnly = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_RETICLE_POD_ONLY.getValue());
            log.info("evn OM_RETICLE_POD_ONLY: {}", reticlePodIDOnly);

            if (Constant.TM_YES.equals(reticlePodIDOnly)) {
                if (StringUtils.length(tempCarrierLocationReport.getCarrierID().getValue()) < 65) {
                    xferReticlePod.setReticlePodID(ObjectIdentifier.buildWithValue(ObjectIdentifier.fetchValue(tempCarrierLocationReport.getCarrierID())));
                }
            } else {
                xferReticlePod.setReticlePodID(ObjectIdentifier.buildWithValue(tempCarrierLocationReport.getCarrierID().getValue().substring(0, 8)));
            }
            List<Infos.XferReticlePod> xferReticlePodList = Collections.singletonList(xferReticlePod);

            ObjectIdentifier stockerID = tempCarrierLocationReport.getCurrMachineID();
            log.info("stockerID: {}", ObjectIdentifier.fetchValue(stockerID));
            log.info("reticlePodID: {}", ObjectIdentifier.fetchValue(xferReticlePodList.get(0).getReticlePodID()));

            log.info("【step1】- sendReticlePodTransferStatusChangeRpt");
            Results.ReticlePodTransferStatusChangeRptResult reticlePodTransferStatusChangeRptResult = omsManager.sendReticlePodTransferStatusChangeRpt(objCommon, stockerID,
                    null, xferReticlePodList);
        } else {
            log.info("carrierID[0] != 'R'");
            /*-----------------------------------------------------------*/
            /*   get Transfer job from Transfer Table by Carrier         */
            /*-----------------------------------------------------------*/
            Infos.XferLot xferLot = new Infos.XferLot();
            xferLot.setTransferStatus(tempCarrierLocationReport.getStatus());
            List<Infos.XferLot> strXferLot = Collections.singletonList(xferLot);
            log.info("【step2】- transferJobGet");
            List<Infos.TransferJobInfo> transferJobInfoList = null;
            try {
                transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                        Constant.TM_INQUIRY_TYPE_BY_CARRIER,
                        Collections.singletonList(tempCarrierLocationReport.getCarrierID()),
                        null,
                        null,
                        null);
            } catch (ServiceException e) {
                //do nothing
            }
            if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
                /* Old transefer request is for Equipment transfer */
                if (ObjectUtils.isNotEmptyWithValue(transferJobInfoList.get(0).getToPortID())) {
                    log.info("toPortID != NULL && toPortID != BLANK");
                    log.info("toPortID: {}", ObjectUtils.getObjectValue(transferJobInfoList.get(0).getToPortID()));
                    log.info("【step3】- checkEqpTransfer");
                    ObjectIdentifier toMachineID = transferJobInfoList.get(0).getToMachineID();
                    Boolean eqpFlag = true;
                    try {
                        equipmentMethod.checkEqpTransfer(objCommon, toMachineID,true);
                    } catch (ServiceException e) {
                        if (Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                            eqpFlag = false;
                            log.info("checkEqpTransfer == RC_RECORD_NOT_FOUND");
                            if (ObjectUtils.equalsWithValue(tempCarrierLocationReport.getCurrMachineID(), transferJobInfoList.get(0).getToMachineID())
                                    && ObjectUtils.equalsWithValue(tempCarrierLocationReport.getCurrPortID(), transferJobInfoList.get(0).getToPortID())) {
                                log.info("transferStatus: {}", "MO");
                                strXferLot.get(0).setTransferStatus(Constant.TM_TRANSSTATE_MANUALOUT);
                            }
                        }
                    }
                    if (BooleanUtils.isTrue(eqpFlag)) {
                        strXferLot.get(0).setTransferStatus(tempCarrierLocationReport.getStatus());
                        log.info("transferStatus: {}", tempCarrierLocationReport.getStatus());
                    }
                }
            }
            log.info("【step4】- checkEqpTransfer");
            Boolean checkFlag = true;
            ObjectIdentifier currMachineID = tempCarrierLocationReport.getCurrMachineID();
            try {
                equipmentMethod.checkEqpTransfer(objCommon, currMachineID,true);
            } catch (ServiceException e) {
                if (Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                    log.info("checkEqpTransfer Location isn't EQP.");
                    checkFlag = false;
                }
            }
            if (BooleanUtils.isTrue(checkFlag)) {
                log.info("checkEqpTransfer Location is EQP.");
                result.setJobID(tempCarrierLocationReport.getJobID());
                result.setCarrierJobID(tempCarrierLocationReport.getCarrierJobID());
                result.setCarrierID(tempCarrierLocationReport.getCarrierID());
                return result;
            } else {
                log.info("【step4】- sendCarrierTransferStatusChangeRpt");
                ObjectIdentifier carrierID = tempCarrierLocationReport.getCarrierID();
                Boolean manualInFlag = false;
                ObjectIdentifier machineID = tempCarrierLocationReport.getCurrMachineID();
                ObjectIdentifier portID = tempCarrierLocationReport.getCurrPortID();
                String zoneID = tempCarrierLocationReport.getCurrZoneID();
                String shelfType = tempCarrierLocationReport.getCurrZoneID();
                Results.CarrierTransferStatusChangeRptResult strPPTMgrSendCarrierTransferStatusChangeRptout = null;
                try {
                    strPPTMgrSendCarrierTransferStatusChangeRptout = omsManager.sendCarrierTransferStatusChangeRpt(
                            objCommon, carrierID, strXferLot.get(0).getTransferStatus(), manualInFlag, machineID, portID,
                            zoneID, shelfType, tempCarrierLocationReport.getJobID(), tempCarrierLocationReport.getCarrierJobID()
                    );
                } catch (ServiceException e) {
                    if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode())) {
                        log.error("oms cast no send or shutdown");
                        castTxNoSendWarning = true;
                    }else {
                        throw e;
                    }
                }
            }
        }
        result.setJobID(tempCarrierLocationReport.getJobID());
        result.setCarrierJobID(tempCarrierLocationReport.getCarrierJobID());
        result.setCarrierID(tempCarrierLocationReport.getCarrierID());

        Validations.check(BooleanUtils.isTrue(castTxNoSendWarning),result,msgRetCodeConfig.getMsgOmsCastTxNoSend());
        return result;
    }

    @Override
    public Results.CarrierLocationReportResult sxRtmsCarrierLocationRpt(Infos.ObjCommon objCommon, Params.CarrierLocationReportParmas carrierLocationReportParmas) {
        Results.CarrierLocationReportResult result = new Results.CarrierLocationReportResult();
        //=================================================================
        //  Send Location Report to OMS
        //=================================================================
        ObjectIdentifier reticlePodID = carrierLocationReportParmas.getCarrierID();
        String xferStatus = null;
        Boolean manualInFlag = false;
        ObjectIdentifier machineID = carrierLocationReportParmas.getCurrMachineID();
        ObjectIdentifier portID = carrierLocationReportParmas.getCurrPortID();

        log.info("【step1】 - equipmentMethod.machineTypeGetDR");
        Infos.MachineTypeGetDR machineTypeGetDR = equipmentMethod.machineTypeGetDR(objCommon,carrierLocationReportParmas.getCurrMachineID());

        // Set default value as reported status
        xferStatus = carrierLocationReportParmas.getStatus();
        if (null != machineTypeGetDR && BooleanUtils.isTrue(machineTypeGetDR.getBStorageMachineFlag())){
            log.info("bStorageMachineFlag is TRUE");
            log.info("【step2】 - cassetteMethod.carrierCurrentLocationGetDR");
            Infos.CarrierCurrentLocationGetDR carrierCurrentLocationGetDR = cassetteMethod.carrierCurrentLocationGetDR(objCommon,carrierLocationReportParmas.getCarrierID());
            log.info("Stocker Type: {}",machineTypeGetDR.getStockerType());

            if (Constant.TM_STOCKER_TYPE_RETICLEPOD.equals(machineTypeGetDR.getStockerType())){
                log.info("stockerType == ReticlePod");
                if (ObjectUtils.isNotEmptyWithValue(carrierLocationReportParmas.getCurrPortID())){
                    log.info("currPortID: {}",ObjectIdentifier.fetchValue(carrierLocationReportParmas.getCurrPortID()));

                    Boolean okFlag = true;
                    List<Infos.TransferJobInfo> transferJobInfoList = null;
                    log.info("【step3】 - transferJobMethod.transferJobGet");
                    try {
                        transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                                Constant.TM_INQUIRY_TYPE_BY_CARRIER,
                                Collections.singletonList(carrierLocationReportParmas.getCarrierID()),
                                null,
                                null,
                                null);
                    } catch (ServiceException e) {
                        okFlag = false;
                        log.error("transferJobMethod.transferJobGet meet some error");
                    }
                    if (BooleanUtils.isTrue(okFlag)){
                        if (ObjectUtils.equalsWithValue(carrierLocationReportParmas.getCurrMachineID(),transferJobInfoList.get(0).getToMachineID())
                                && ObjectUtils.equalsWithValue(carrierLocationReportParmas.getCurrPortID(),transferJobInfoList.get(0).getToPortID())){
                            log.info("xferStatus is MO");
                            xferStatus = Constant.TM_TRANSSTATE_MANUALOUT;
                        }
                    }
                    if (ObjectUtils.equalsWithValue(carrierLocationReportParmas.getCurrPortID(),envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_ABNORMALSTOCK_MANUALPORTID.getValue()))){
                        log.info("xferStatus is MO");
                        xferStatus = Constant.TM_TRANSSTATE_MANUALOUT;
                    }
                }else if (Constant.TM_TRANSSTATE_BAYIN.equals(carrierCurrentLocationGetDR.getTransferStatus())
                    || Constant.TM_TRANSSTATE_BAYOUT.equals(carrierCurrentLocationGetDR.getTransferStatus())){
                    log.info("xferStatus is BI");
                    xferStatus = Constant.TM_TRANSSTATE_BAYIN;
                }else if (Constant.TM_TRANSSTATE_STATIONIN.equals(carrierCurrentLocationGetDR.getTransferStatus())
                        || Constant.TM_TRANSSTATE_STATIONOUT.equals(carrierCurrentLocationGetDR.getTransferStatus())){
                    log.info("xferStatus is SI");
                    xferStatus = Constant.TM_TRANSSTATE_STATIONIN;
                }else {
                    log.info("xferStatus is MI");
                    xferStatus = Constant.TM_TRANSSTATE_MANUALIN;
                }
            }else if (Constant.TM_STOCKER_TYPE_BARERETICLE.equals(machineTypeGetDR.getStockerType())){
                log.info("xferStatus is SO");
                xferStatus = Constant.TM_TRANSSTATE_STATIONOUT;
            }else if (Constant.TM_STOCKER_TYPE_INTERBAY.equals(machineTypeGetDR.getStockerType())){
                log.info("xferStatus is BO");
                xferStatus = Constant.TM_TRANSSTATE_BAYOUT;
            }else if (Constant.TM_STOCKER_TYPE_INTRABAY.equals(machineTypeGetDR.getStockerType())){
                log.info("xferStatus is SO");
                xferStatus = Constant.TM_TRANSSTATE_STATIONOUT;
            }else if (Constant.TM_STOCKER_TYPE_RETICLE.equals(machineTypeGetDR.getStockerType())){
                log.info("xferStatus is HI");
                xferStatus = Constant.TM_TRANSSTATE_SHELFIN;
            }else if (Constant.TM_STOCKER_TYPE_RETICLESHELF.equals(machineTypeGetDR.getStockerType())){
                log.info("xferStatus is HI");
                xferStatus = Constant.TM_TRANSSTATE_SHELFIN;
            }else if (Constant.TM_STOCKER_TYPE_SHELF.equals(machineTypeGetDR.getStockerType())){
                log.info("xferStatus is HI");
                xferStatus = Constant.TM_TRANSSTATE_SHELFIN;
            }
        }else {
            log.info("bStorageMachineFlag != TRUE");
            log.info("xferStatus is HI");
            xferStatus = Constant.TM_TRANSSTATE_SHELFIN;
        }

        log.info("【step4】 - omsManager.sendRSPXferStatusChangeRpt");
        Results.RSPXferStatusChangeRptResult rspXferStatusChangeRptResult = omsManager.sendRSPXferStatusChangeRpt(objCommon,
                reticlePodID,
                xferStatus,
                manualInFlag,
                machineID,
                portID);

        result.setJobID(carrierLocationReportParmas.getJobID());
        result.setCarrierJobID(carrierLocationReportParmas.getCarrierJobID());
        result.setCarrierID(carrierLocationReportParmas.getCarrierID());
        return result;
    }
}