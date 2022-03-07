package com.fa.cim.tms.service.impl;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
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
import com.fa.cim.tms.service.IOnlineHostInqService;
import com.fa.cim.tms.utils.ArrayUtils;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
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
 * @date: 2020/10/20 14:54
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class OnlineHostInqService implements IOnlineHostInqService {

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

    public void sxOnlineHostInq(Infos.ObjCommon objCommon, Params.OnlineHostInqParam onlineHostInqParam) {
        log.info("txXmPriorityChangeReq Request Json" + JSON.toJSONString(onlineHostInqParam));
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendOnlineHostInq");
        mcsManager.sendOnlineHostInq(objCommon, onlineHostInqParam);

        Infos.ObjCommon strObjCommonIn2 = new Infos.ObjCommon();
        strObjCommonIn2.setTimeStamp(new Infos.TimeStamp());
        strObjCommonIn2.getTimeStamp().setReportTimeStamp(new Timestamp(System.currentTimeMillis()));
        strObjCommonIn2.setTransactionID("OM11");

        // TODO: 2020/10/20 set user by objcome and no set it force by TMS system
        strObjCommonIn2.setUser(objCommon.getUser());
        String inventoryReqFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_INVENTORY_TM_FLAG.getValue());//"ON"
        log.info("env OM_INVENTORY_TM_FLAG: {}", inventoryReqFlag);
        if (Constant.TM_ON.equals(inventoryReqFlag)) {
            String stockerType = Constant.TM_STOCKER_TYPE_AUTO;
            log.info("【step2】 - omsManager.sendStockerListInq");
            Results.StockerListInqResult stockerListInqResult = omsManager.sendStockerListInq(strObjCommonIn2, stockerType,false);
            if (null != stockerListInqResult && ArrayUtils.isNotEmpty(stockerListInqResult.getStrStockerInfo())) {
                for (Results.StockerInfo stockerInfo : stockerListInqResult.getStrStockerInfo()) {
                    ObjectIdentifier machineID = stockerInfo.getStockerID();
                    log.info("【step3】 - mcsManager.sendStockerDetailInfoInq");
                    Params.StockerDetailInfoInqParmas stockerDetailInfoInqParmas = new Params.StockerDetailInfoInqParmas();
                    stockerDetailInfoInqParmas.setMachineID(machineID);
                    Results.StockerDetailInfoInqResult stockerDetailInfoInqResult = mcsManager.sendStockerDetailInfoInq(strObjCommonIn2, stockerDetailInfoInqParmas);
                    /* Stocker Status Report to MM */
                    Params.E10StatusReportParmas e10StatusReport = new Params.E10StatusReportParmas();
                    e10StatusReport.setMachineID(stockerDetailInfoInqResult.getMachineID());
                    e10StatusReport.setE10Status(stockerDetailInfoInqResult.getE10Status());
                    log.info("【step4】 - e10StatusRptService.sxE10StatusRpt");
                    try {
                        Results.E10StatusReportResult e10StatusReportResult = e10StatusRptService.sxE10StatusRpt(strObjCommonIn2, e10StatusReport);
                    } catch (Exception e) {
                        //doNothing
                        e.printStackTrace();
                    }
                }
            }
            log.info("【step5】 - omsManager.sendStockerForAutoTransferInq");
            Results.StockerForAutoTransferInqResult stockerForAutoTransferInqResult = omsManager.sendStockerForAutoTransferInq(objCommon);
            if (null != stockerForAutoTransferInqResult && ArrayUtils.isNotEmpty(stockerForAutoTransferInqResult.getStrAvailableStocker())) {
                for (Results.AvailableStocker availableStocker : stockerForAutoTransferInqResult.getStrAvailableStocker()) {
                    Params.UploadInventoryReqParmas uploadInventoryReqParmas = new Params.UploadInventoryReqParmas();
                    uploadInventoryReqParmas.setMachineID(availableStocker.getStockerID());
                    Results.UploadInventoryReqResult uploadInventoryReqResult = null;
                    log.info("【step6】 - mcsManager.sendUploadInventoryReq");
                    try {
                        uploadInventoryReqResult = mcsManager.sendUploadInventoryReq(objCommon, uploadInventoryReqParmas);
                    } catch (ServiceException e) {
                        //Integration error
                        if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
                        continue;
                    }
                    /*******************/
                    /*   Data Copy     */
                    /*******************/
                    List<Infos.InventoryLotInfo> strInventoryLotInfo = new ArrayList<>();
                    if (null != uploadInventoryReqResult && ArrayUtils.isNotEmpty(uploadInventoryReqResult.getUploadInventoryReqResults())) {
                        for (Results.UploadInventoryReq inventoryReqResult : uploadInventoryReqResult.getUploadInventoryReqResults()) {
                            Infos.InventoryLotInfo inventoryLotInfo = new Infos.InventoryLotInfo();
                            inventoryLotInfo.setCassetteID(inventoryReqResult.getCarrierID());
                            strInventoryLotInfo.add(inventoryLotInfo);
                        }
                        log.info("【step7】 - omsManager.sendStockerInventoryRpt");
                        Results.StockerInventoryRptResult stockerInventoryRptResult = omsManager.sendStockerInventoryRpt(
                                objCommon, availableStocker.getStockerID(), strInventoryLotInfo, ""
                        );
                    }
                }
            }
        }
    }

    @Override
    public void sxRtmsOnlineHostInq(Infos.ObjCommon objCommon, Params.OnlineHostInqParam onlineHostInqParam) {
        log.info("txXmPriorityChangeReq Request Json" + JSON.toJSONString(onlineHostInqParam));
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendOnlineHostInq");
        mcsManager.sendOnlineHostInq(objCommon, onlineHostInqParam);

        String inventoryReqFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_INVENTORY_RTM_FLAG.getValue());//"ON"
        log.info("env OM_INVENTORY_RTM_FLAG: {}", inventoryReqFlag);
        if (Constant.TM_ON.equals(inventoryReqFlag)) {
            log.info("inventoryReqFlag = ON");
            //===========================================================
            //   Get stocker ID list (of Reticle Pod type stocker)
            //===========================================================
            String stockerType = Constant.TM_STOCKER_TYPE_RETICLEPOD;
            Boolean availFlag = false;
            log.info("【step2】 - omsManager.sendStockerListInq");
            Results.StockerListInqResult stockerListInqResult = omsManager.sendStockerListInq(objCommon, stockerType,availFlag);
            if (null != stockerListInqResult && ArrayUtils.isNotEmpty(stockerListInqResult.getStrStockerInfo())) {
                for (Results.StockerInfo stockerInfo : stockerListInqResult.getStrStockerInfo()) {
                    ObjectIdentifier machineID = stockerInfo.getStockerID();
                    //===========================================================
                    //   Inquire stocker status to MCS
                    //===========================================================
                    log.info("【step3】 - mcsManager.sendStockerDetailInfoInq");
                    Params.StockerDetailInfoInqParmas stockerDetailInfoInqParmas = new Params.StockerDetailInfoInqParmas();
                    stockerDetailInfoInqParmas.setMachineID(machineID);
                    Results.StockerDetailInfoInqResult stockerDetailInfoInqResult = mcsManager.sendStockerDetailInfoInq(objCommon, stockerDetailInfoInqParmas);
                    /* Stocker Status Report to MM */
                    Params.E10StatusReportParmas e10StatusReport = new Params.E10StatusReportParmas();
                    e10StatusReport.setMachineID(stockerDetailInfoInqResult.getMachineID());
                    e10StatusReport.setE10Status(stockerDetailInfoInqResult.getE10Status());
                    //===========================================================
                    //   Send E10 status report to oms based on info from MCS
                    //===========================================================
                    log.info("【step4】 - e10StatusRptService.sxE10StatusRpt");
                    try {
                        Results.E10StatusReportResult e10StatusReportResult = e10StatusRptService.sxE10StatusRpt(objCommon, e10StatusReport);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //doNoting
                    }
                }
            }
            //===========================================================
            //   Get available stocker list
            //===========================================================
            availFlag = true;
            log.info("【step5】 - omsManager.sendStockerListInq");
            Results.StockerListInqResult stockerListInqResult1 = omsManager.sendStockerListInq(objCommon, stockerType, availFlag);
            if (null != stockerListInqResult1 && ArrayUtils.isNotEmpty(stockerListInqResult1.getStrStockerInfo())) {
                for (Results.StockerInfo availableStocker : stockerListInqResult1.getStrStockerInfo()) {
                    //===========================================================
                    //   Request inventory upload to Mcs based on stocker list from mmserver
                    //===========================================================
                    Params.UploadInventoryReqParmas uploadInventoryReqParmas = new Params.UploadInventoryReqParmas();
                    uploadInventoryReqParmas.setMachineID(availableStocker.getStockerID());
                    Results.UploadInventoryReqResult uploadInventoryReqResult = null;
                    log.info("【step6】 - mcsManager.sendUploadInventoryReq");
                    try {
                        uploadInventoryReqResult = mcsManager.sendUploadInventoryReq(objCommon, uploadInventoryReqParmas);
                    } catch (ServiceException e) {
                        //Integration error
                        if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
                        continue;
                    }
                    /*******************/
                    /*   Data Copy     */
                    /*******************/
                    ObjectIdentifier stockerID = availableStocker.getStockerID();
                    List<Infos.InventoryReticlePodInfo> strInventoryReticlePodInfo = new ArrayList<>();
                    if (null != uploadInventoryReqResult && ArrayUtils.isNotEmpty(uploadInventoryReqResult.getUploadInventoryReqResults())){
                        for (Results.UploadInventoryReq inventoryReqResult : uploadInventoryReqResult.getUploadInventoryReqResults()) {
                            Infos.InventoryReticlePodInfo inventoryReticlePodInfo = new Infos.InventoryReticlePodInfo();
                            inventoryReticlePodInfo.setReticlePodID(inventoryReqResult.getCarrierID());
                            strInventoryReticlePodInfo.add(inventoryReticlePodInfo);
                        }
                    }
                    //===========================================================
                    //   Inventory report to Oms based on Mcs report
                    //===========================================================
                    log.info("【step7】 - omsManager.sendReticlePodInventoryRpt");
                    Results.ReticlePodInventoryRptResult reticlePodInventoryRptResult = omsManager.sendReticlePodInventoryRpt(objCommon,
                            stockerID,
                            strInventoryReticlePodInfo,
                            null);
                }
            }
        }
    }
}
