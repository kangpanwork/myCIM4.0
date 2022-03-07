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
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.service.ICarrierStatusRptService;
import com.fa.cim.tms.utils.BooleanUtils;
import com.fa.cim.tms.utils.StringUtils;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class CarrierStatusRptService implements ICarrierStatusRptService {

    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private IEnvMethod envMethod;
    @Autowired
    private ICassetteMethod cassetteMethod;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    public Results.CarrierStatusReportResult sxCarrierStatusReport(Infos.ObjCommon objCommon, Params.CarrierStatusReportParam carrierStatusReportParam) {
        Results.CarrierStatusReportResult result = new Results.CarrierStatusReportResult();

        Boolean castTxNoSendWarning = false;
        String reportFlagTM2 = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM02_MO_REPORT_FLAG.getValue());
        String reportCarrierStatusTM2 = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM02_MO_REPORT_CARRIER_STATUS.getValue());
        log.info("evn OM_TM02_MO_REPORT_FLAG: {}", reportFlagTM2);
        log.info("evn OM_TM02_MO_REPORT_CARRIER_STATUS: {}", reportCarrierStatusTM2);
        if (StringUtils.equals(reportCarrierStatusTM2, null)) {
            reportCarrierStatusTM2 = "0";
        }
        //===========================================================================
        // If OM_TM02_MO_REPORT_FLAG = ON following logic is enabled.
        //
        //   If carrier is in stocker and
        //   if reported carrier status is equal to OM_TM02_MO_REPORT_CARRIER_STATUS
        //   then carrier's transfer status becomes "MO"
        //===========================================================================
        if (StringUtils.equals(reportFlagTM2, "ON")) {
            //=================================================================
            //  Send Location Report to OMS
            //=================================================================
            //=================================================================
            //  Get current reticle pod location
            //=================================================================
            log.info("【step1】 - cassetteXfstatGetDR");
            Results.DRcassetteXfstatResult dRcassetteXfstatResult = cassetteMethod.cassetteXfstatGetDR(objCommon, carrierStatusReportParam.getCarrierID());
            if (StringUtils.isNotEmpty(dRcassetteXfstatResult.getStkID())) {
                log.info("stkID is not Null");
                if (Constant.TM_TRANSSTATE_STATIONIN.equals(dRcassetteXfstatResult.getTransferStatus()) ||
                        Constant.TM_TRANSSTATE_BAYIN.equals(dRcassetteXfstatResult.getTransferStatus()) ||
                        Constant.TM_TRANSSTATE_MANUALIN.equals(dRcassetteXfstatResult.getTransferStatus()) ||
                        Constant.TM_TRANSSTATE_ABNORMALIN.equals(dRcassetteXfstatResult.getTransferStatus()) ||
                        Constant.TM_TRANSSTATE_INTERMEDIATEIN.equals(dRcassetteXfstatResult.getTransferStatus())) {
                    log.info("Cassette Transfer Status shows it is inStocker !");
                    if (StringUtils.equals(carrierStatusReportParam.getCarrierStatus(), reportCarrierStatusTM2)) {
                        ObjectIdentifier carrierID = carrierStatusReportParam.getCarrierID();
                        ObjectIdentifier machineID = ObjectIdentifier.buildWithValue(dRcassetteXfstatResult.getStkID());
                        log.info("【step2】 - sendCarrierTransferStatusChangeRpt");
                        try {
                            Results.CarrierTransferStatusChangeRptResult carrierTransferStatusChangeRptResult = omsManager.sendCarrierTransferStatusChangeRpt(objCommon,
                                    carrierID,
                                    Constant.TM_TRANSSTATE_MANUALOUT,
                                    false,
                                    machineID,
                                    null,
                                    Constant.TM_STRING_DEFAULT,
                                    "",
                                    carrierStatusReportParam.getJobID(),
                                    carrierStatusReportParam.getCarrierJobID());
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
            }
        }
        Validations.check(BooleanUtils.isTrue(castTxNoSendWarning),result,msgRetCodeConfig.getMsgOmsCastTxNoSend());
        return result;
    }

    @Override
    public Results.CarrierStatusReportResult sxRtmsCarrierStatusReport(Infos.ObjCommon objCommon, Params.CarrierStatusReportParam carrierStatusReportParam) {
        Results.CarrierStatusReportResult result = new Results.CarrierStatusReportResult();

        //=================================================================
        //  Send Location Report to OMS
        //=================================================================
        //=================================================================
        //  Get current reticle pod location
        //=================================================================
        log.info("【step1】 - cassetteMethod.reticlePodXferStatGetDR");
        Infos.ReticlePodXferStat reticlePodXferStat = cassetteMethod.reticlePodXferStatGetDR(objCommon,carrierStatusReportParam.getCarrierID());
        log.info("transferStatus: {}",reticlePodXferStat.getTransferStatus());
        log.info("eqpID: {}",reticlePodXferStat.getEqpID());
        log.info("stkID: {}",reticlePodXferStat.getStkID());

        if (StringUtils.isNotEmpty(reticlePodXferStat.getStkID())){
            log.info("stkID is not null");
            if (Constant.TM_TRANSSTATE_STATIONIN.equals(reticlePodXferStat.getTransferStatus())
                || Constant.TM_TRANSSTATE_BAYIN.equals(reticlePodXferStat.getTransferStatus())
                || Constant.TM_TRANSSTATE_MANUALIN.equals(reticlePodXferStat.getTransferStatus())
                || Constant.TM_TRANSSTATE_ABNORMALIN.equals(reticlePodXferStat.getTransferStatus())
                || Constant.TM_TRANSSTATE_INTERMEDIATEIN.equals(reticlePodXferStat.getTransferStatus())){
                log.info("Reticle Pod Transfer Status shows it is in Stocker !");
                if (Constant.TM_VALUE_ZERO.equals(carrierStatusReportParam.getCarrierStatus())){
                    log.info("Input Carrier Status =='0'");
                    ObjectIdentifier reticlePodID = carrierStatusReportParam.getCarrierID();
                    ObjectIdentifier machineID = ObjectIdentifier.buildWithValue(reticlePodXferStat.getStkID());

                    log.info("【step2】 - omsManager.sendRSPXferStatusChangeRpt");
                    Results.RSPXferStatusChangeRptResult rspXferStatusChangeRptResult = omsManager.sendRSPXferStatusChangeRpt(objCommon,
                            reticlePodID,
                            Constant.TM_TRANSSTATE_MANUALOUT,
                            false,
                            machineID,
                            null);
                }
            }
        }
        return result;
    }
}
