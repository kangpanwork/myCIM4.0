package com.fa.cim.service.newSorter.Impl;


import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.IEAPMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.ISorterNewMethod;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.remote.IEAPRemoteManager;
import com.fa.cim.service.newSorter.ISortNewInqService;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.Params;
import com.fa.cim.sorter.Results;
import com.fa.cim.sorter.SorterType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:59
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class SortNewInqServiceImpl implements ISortNewInqService {
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ISorterNewMethod sorterMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IEAPMethod eapMethod;

    @Autowired
    private ISorterNewMethod sorterNewMethod;


    @Override
    public Info.SortJobInfo sorterActionInq(Infos.ObjCommon objCommon, Params.SorterActionInqParams params,String jotStatus) {
        Validations.check(ObjectIdentifier.isEmpty(params.getEquipmentID())
                        && ObjectIdentifier.isEmpty(params.getCassetteID()),
                retCodeConfig.getInvalidInputParam());
        if (!SorterType.Status.Created.getValue().equals(jotStatus)
                && !SorterType.Status.Executing.getValue().equals(jotStatus)) {
            Validations.check(retCodeConfigEx.getInvalidSorterJobStatus());
        }
        return sorterMethod.sorterActionInq(objCommon, params,jotStatus);
    }

    @Override
    public Results.OnlineSorterActionSelectionInqResult sxOnlineSorterActionSelectionInq(Infos.ObjCommon objCommon, Params.OnlineSorterActionSelectionInqParams params) {
        Results.OnlineSorterActionSelectionInqResult out = new Results.OnlineSorterActionSelectionInqResult();
        ObjectIdentifier equipmentID = params.getEquipmentID();

        if (log.isDebugEnabled()) {
            log.debug("Check Transaction ID and equipment Category combination.");
        }
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);
        if (log.isDebugEnabled()) {
            log.debug("Check And Read Action code");
        }
        List<Info.SortActionAttributes> waferSorterActionListSelectDROut = sorterMethod.waferSorterActionListSelectDR(objCommon, equipmentID);

        out.setStrWaferSorterActionListSequence(waferSorterActionListSelectDROut);
        out.setEquipmentID(equipmentID);
        return out;
    }

    @Override
    public List<Info.SortJobListAttributes> sxSJListInq(Infos.ObjCommon objCommon, Params.SJListInqParams params) {
        Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new Params.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRIn.setEquipmentID(params.getEquipmentID());
        objSorterJobListGetDRIn.setCarrierID(params.getCarrierID());
        objSorterJobListGetDRIn.setCreateUser(params.getCreateUser());
        objSorterJobListGetDRIn.setLotID(params.getLotID());
        objSorterJobListGetDRIn.setSorterJob(params.getSorterJobID());
        objSorterJobListGetDRIn.setActionCode(params.getActionCode());
        return sorterMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
    }

    @Override
    public Page<Results.SortJobHistoryResult> getSortJobHistory(Infos.ObjCommon objCommon, Params.SortJobHistoryParams params) {
        Validations.check(StringUtils.isEmpty(params.getEquipmentID()), retCodeConfigEx.getEqpIdParamNull());
        return sorterMethod.getSortJobHistory(objCommon,params);

    }

    @Override
    public Info.CarrierAndLotHistory sxCarrierAndLotHisInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        if (log.isDebugEnabled()) {
            log.debug("query lot information");
        }
        Validations.check(ObjectIdentifier.isEmpty(equipmentID),retCodeConfigEx.getEqpIdParamNull());
        List<ObjectIdentifier> lotIDs = sorterMethod.getLotIDByEquipment(objCommon, equipmentID);
        List<ObjectIdentifier> carrierIDs = sorterMethod.getCarrierIDByEquipment(objCommon, equipmentID);
        Info.CarrierAndLotHistory carrierAndLotHistory = new Info.CarrierAndLotHistory();
        carrierAndLotHistory.setLotIDs(lotIDs);
        carrierAndLotHistory.setCarrierIDs(carrierIDs);
        return carrierAndLotHistory;
    }
    @Override
    public void scanbarCodeInq(Infos.ObjCommon objCommon, com.fa.cim.sorter.Params.ScanBarCodeInqParams params) {
        Validations.check(ObjectIdentifier.isEmpty(params.getVendorLotID())
                || ObjectIdentifier.isEmpty(params.getEquipmentID())
                || ObjectIdentifier.isEmpty(params.getCarrierID()), retCodeConfig.getInvalidInputParam());
        Params.SorterActionInqParams actionInqParams = new Params.SorterActionInqParams();
        actionInqParams.setCassetteID(params.getCarrierID());
        actionInqParams.setEquipmentID(params.getEquipmentID());
        Info.SortJobInfo sortJobInfo = this.sorterActionInq(objCommon, actionInqParams, SorterType.Status.Created.getValue());
        sortJobInfo.setUser(params.getUser());
        Info.ComponentJob componentJob = sortJobInfo.getComponentJob();
        Validations.check(null == componentJob, retCodeConfigEx.getNoProcessingTask());
        Validations.check(!componentJob.getActionCode().equals(SorterType.Action.WaferStart.getValue()),
                retCodeConfigEx.getSorterActionCodeError());
        if (componentJob.getActionCode().equals(SorterType.Action.WaferStart.getValue())) {
            ObjectIdentifier lotID = componentJob.getWaferList().get(0).getLotID();
            Validations.check(!ObjectIdentifier.equalsWithValue(lotID, params.getVendorLotID()),
                    retCodeConfigEx.getInvalidInputVendorlotID());
            sortJobInfo.setFosbActionStartMethodFlag(false);
            this.sendEap(objCommon, sortJobInfo);
        }
    }

    private void sendEap(Infos.ObjCommon objCommon, Info.SortJobInfo jobInfo) {
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        Long sleepTimeValue ;
        Long retryCountValue ;

        if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            sleepTimeValue = CimNumberUtils.longValue(tmpSleepTimeValue);
        }

        if (0 == CimStringUtils.length(tmpRetryCountValue)) {
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            retryCountValue = CimNumberUtils.longValue(tmpRetryCountValue);
        }

        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            /*--------------------------*/
            /*    Send Request to EAP   */
            /*--------------------------*/
            IEAPRemoteManager eapRemoteManager = eapMethod
                    .eapRemoteManager(objCommon, objCommon.getUser(), jobInfo.getEquipmentID(), null, true);
            if (null == eapRemoteManager) {
                break;
            }
            try {
                eapRemoteManager.sendSortActionStartReq(jobInfo);
                break;
            } catch (CimIntegrationException ex) {
                if (Validations.isEquals((int) ex.getCode(), retCodeConfig.getExtServiceBindFail())
                        || Validations.isEquals((int) ex.getCode(), retCodeConfig.getExtServiceNilObj())
                        || Validations.isEquals((int) ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    if (retryNum != retryCountValue) {
                        try {
                            Thread.sleep(sleepTimeValue);
                            continue;
                        } catch (InterruptedException e) {
                            ex.addSuppressed(e);
                            Thread.currentThread().interrupt();
                            throw ex;
                        }
                    } else {
                        Validations.check(true, retCodeConfig.getTcsNoResponse());
                    }
                } else {
                    Validations.check(true, new OmCode((int) ex.getCode(), ex.getMessage()));
                }
            }
        }
    }



}
