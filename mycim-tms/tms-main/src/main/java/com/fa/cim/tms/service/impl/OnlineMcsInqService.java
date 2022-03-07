package com.fa.cim.tms.service.impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.tms.common.enums.EnvCodeEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IMCSManager;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.method.IEnvMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.service.IE10StatusRptService;
import com.fa.cim.tms.service.IOnlineMcsInqService;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 13:55
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class OnlineMcsInqService implements IOnlineMcsInqService {

    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private IMCSManager mcsManager;
    @Autowired
    private IE10StatusRptService e10StatusRptService;
    @Autowired
    private IEnvMethod envMethod;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    public void sxOnlineMcsInq(Infos.ObjCommon objCommon) {
        String inventoryReqFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_INVENTORY_FLAG.getValue());//"ON"
        log.info("env OM_INVENTORY_FLAG: {}", inventoryReqFlag);
        if (!Constant.TM_ON.equals(inventoryReqFlag)) {
            log.info("inventoryReqFlag != ON");
            return;
        }
        String stockerType = Constant.TM_STOCKER_TYPE_AUTO;
        log.info("【step1】- omsManager.sendStockerListInq");
        Results.StockerListInqResult stockerListInqResult = omsManager.sendStockerListInq(objCommon, stockerType,false);
        if (CimArrayUtils.isNotEmpty(stockerListInqResult.getStrStockerInfo())) {
            for (Results.StockerInfo stockerInfo : stockerListInqResult.getStrStockerInfo()) {
                ObjectIdentifier machineID = stockerInfo.getStockerID();
                log.info("【step2】- mcsManager.sendStockerDetailInfoInq");
                Results.StockerDetailInfoInqResult stockerDetailInfoInqResult = null;
                Params.StockerDetailInfoInqParmas stockerDetailInfoInqParmas = new Params.StockerDetailInfoInqParmas();
                stockerDetailInfoInqParmas.setMachineID(machineID);
                try {
                    stockerDetailInfoInqResult = mcsManager.sendStockerDetailInfoInq(objCommon, stockerDetailInfoInqParmas);
                } catch (ServiceException e) {
                    //Integration error
                    if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
                    continue;
                }
                /* Stocker Status Report to OMS */
                Params.E10StatusReportParmas e10StatusReportParmas = new Params.E10StatusReportParmas();
                e10StatusReportParmas.setMachineID(stockerDetailInfoInqResult.getMachineID());
                e10StatusReportParmas.setE10Status(stockerDetailInfoInqResult.getE10Status());
                log.info("【step3】- e10StatusRptService.sxE10StatusRpt");
                try {
                    Results.E10StatusReportResult e10StatusReportResult = e10StatusRptService.sxE10StatusRpt(objCommon, e10StatusReportParmas);
                } catch (ServiceException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        log.info("【step4】- mcsManager.sendStockerDetailInfoInq");
        Results.StockerForAutoTransferInqResult stockerForAutoTransferInqResult = omsManager.sendStockerForAutoTransferInq(objCommon);
        if (CimArrayUtils.isNotEmpty(stockerForAutoTransferInqResult.getStrAvailableStocker())) {
            for (Results.AvailableStocker availableStocker : stockerForAutoTransferInqResult.getStrAvailableStocker()) {
                Params.UploadInventoryReqParmas amhsUploadInventoryReqParmas = new Params.UploadInventoryReqParmas();
                amhsUploadInventoryReqParmas.setMachineID(availableStocker.getStockerID());
                log.info("【step5】- mcsManager.sendUploadInventoryReq");
                Results.UploadInventoryReqResult amhsUploadInventoryReqResult = null;
                try {
                    amhsUploadInventoryReqResult = mcsManager.sendUploadInventoryReq(objCommon, amhsUploadInventoryReqParmas);
                } catch (ServiceException e) {
                    //Integration error
                    if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
                    continue;
                }
                /*******************/
                /*   Data Copy     */
                /*******************/
                List<Infos.InventoryLotInfo> strInventoryLotInfo = new ArrayList<Infos.InventoryLotInfo>();
                ObjectIdentifier stockerID = availableStocker.getStockerID();
                if (CimArrayUtils.isNotEmpty(amhsUploadInventoryReqResult.getUploadInventoryReqResults())) {
                    for (Results.UploadInventoryReq uploadInventoryReqResult : amhsUploadInventoryReqResult.getUploadInventoryReqResults()) {
                        Infos.InventoryLotInfo inventoryLotInfo = new Infos.InventoryLotInfo();
                        inventoryLotInfo.setCassetteID(uploadInventoryReqResult.getCarrierID());
                        strInventoryLotInfo.add(inventoryLotInfo);
                    }
                }
                log.info("【step6】- omsManager.sendStockerInventoryRpt");
                Results.StockerInventoryRptResult stockerInventoryRptResult = omsManager.sendStockerInventoryRpt(objCommon, stockerID, strInventoryLotInfo, "");
            }
        }
    }

    @Override
    public void sxRtmsOnlineMcsInq(Infos.ObjCommon objCommon) {
        String inventoryReqFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_INVENTORY_FLAG.getValue());//"ON"
        log.info("env OM_INVENTORY_FLAG: {}", inventoryReqFlag);
        if (!Constant.TM_ON.equals(inventoryReqFlag)) {
            log.info("inventoryReqFlag != ON");
            return;
        }
        //==============================================================
        // Call Stocker List Inq to mmserver (Only for ReticlePod type)
        // Call Inventory Upload Request to MCS
        //==============================================================
        String stockerType = Constant.TM_STOCKER_TYPE_RETICLEPOD;
        Boolean availFlag = false;
        log.info("【step1】- omsManager.sendStockerListInq");
        Results.StockerListInqResult stockerListInqResult = omsManager.sendStockerListInq(objCommon, stockerType,false);

        //==========================================
        // Call Detail Info Inq to MCS
        // Call E10 Status Report to OMS based on info from MCS
        //==========================================
        if (CimArrayUtils.isNotEmpty(stockerListInqResult.getStrStockerInfo())) {
            for (Results.StockerInfo stockerInfo : stockerListInqResult.getStrStockerInfo()) {
                ObjectIdentifier machineID = stockerInfo.getStockerID();
                //==========================================
                // Call Detail Info Inq to MCS
                //==========================================
                log.info("【step2】- mcsManager.sendStockerDetailInfoInq");
                Results.StockerDetailInfoInqResult stockerDetailInfoInqResult = null;
                Params.StockerDetailInfoInqParmas stockerDetailInfoInqParmas = new Params.StockerDetailInfoInqParmas();
                stockerDetailInfoInqParmas.setMachineID(machineID);
                try {
                    stockerDetailInfoInqResult = mcsManager.sendStockerDetailInfoInq(objCommon, stockerDetailInfoInqParmas);
                } catch (ServiceException e) {
                    //Integration error
                    if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
                    continue;
                }
                //======================================================
                // Call E10 Status Report to OMS based on info from MCS
                //======================================================
                Params.E10StatusReportParmas e10StatusReportParmas = new Params.E10StatusReportParmas();
                e10StatusReportParmas.setMachineID(stockerDetailInfoInqResult.getMachineID());
                e10StatusReportParmas.setE10Status(stockerDetailInfoInqResult.getE10Status());
                log.info("【step3】- e10StatusRptService.sxE10StatusRpt");
                try {
                    Results.E10StatusReportResult e10StatusReportResult = e10StatusRptService.sxE10StatusRpt(objCommon, e10StatusReportParmas);
                } catch (ServiceException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        //==========================================
        // Get Available Stocker list from OMS
        //==========================================
        availFlag = true;
        log.info("【step4】- mcsManager.sendStockerListInq");
        Results.StockerListInqResult stockerListInqResult1 = omsManager.sendStockerListInq(objCommon,stockerType,availFlag);
        //===========================================================
        // Call Inventory Upload request to MCS for availble stokers
        // Call Inventory Report to OMS based on info from MCS
        //============================================================
        if (CimArrayUtils.isNotEmpty(stockerListInqResult1.getStrStockerInfo())) {
            for (Results.StockerInfo availableStocker : stockerListInqResult1.getStrStockerInfo()) {
                Params.UploadInventoryReqParmas amhsUploadInventoryReqParmas = new Params.UploadInventoryReqParmas();
                amhsUploadInventoryReqParmas.setMachineID(availableStocker.getStockerID());
                //==========================================
                // call Inventory Upload request to MCS
                //==========================================
                log.info("【step5】- mcsManager.sendUploadInventoryReq");
                Results.UploadInventoryReqResult amhsUploadInventoryReqResult = null;
                try {
                    amhsUploadInventoryReqResult = mcsManager.sendUploadInventoryReq(objCommon, amhsUploadInventoryReqParmas);
                } catch (ServiceException e) {
                    //Integration error
                    if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
                    continue;
                }
                /*******************/
                /*   Data Copy     */
                /*******************/
                List<Infos.InventoryReticlePodInfo> strInventoryReticlePodInfo = new ArrayList<>();
                ObjectIdentifier stockerID = availableStocker.getStockerID();
                if (CimArrayUtils.isNotEmpty(amhsUploadInventoryReqResult.getUploadInventoryReqResults())) {
                    for (Results.UploadInventoryReq uploadInventoryReqResult : amhsUploadInventoryReqResult.getUploadInventoryReqResults()) {
                        Infos.InventoryReticlePodInfo inventoryLotInfo = new Infos.InventoryReticlePodInfo();
                        inventoryLotInfo.setReticlePodID(uploadInventoryReqResult.getCarrierID());
                        strInventoryReticlePodInfo.add(inventoryLotInfo);
                    }
                }
                log.info("【step6】- omsManager.sendReticlePodInventoryRpt");
                Results.ReticlePodInventoryRptResult reticlePodInventoryRptResult = omsManager.sendReticlePodInventoryRpt(objCommon, stockerID, strInventoryReticlePodInfo, null);
            }
        }
    }
}
