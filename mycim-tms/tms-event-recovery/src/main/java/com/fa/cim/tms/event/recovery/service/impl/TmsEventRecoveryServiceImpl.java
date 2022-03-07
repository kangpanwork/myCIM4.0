package com.fa.cim.tms.event.recovery.service.impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.event.recovery.config.MsgRetCodeConfig;
import com.fa.cim.tms.event.recovery.dto.User;
import com.fa.cim.tms.event.recovery.enums.TransactionIDEnum;
import com.fa.cim.tms.event.recovery.method.IQueneTransferJobMethod;
import com.fa.cim.tms.event.recovery.method.IQueueCassetteMethod;
import com.fa.cim.tms.event.recovery.method.IUtilsComp;
import com.fa.cim.tms.event.recovery.pojo.Infos;
import com.fa.cim.tms.event.recovery.pojo.ObjectIdentifier;
import com.fa.cim.tms.event.recovery.service.ICarrierEventService;
import com.fa.cim.tms.event.recovery.service.ITmsEventRecoveryService;
import com.fa.cim.tms.event.recovery.service.ITransferEventService;
import com.fa.cim.tms.event.recovery.utils.ArrayUtils;
import com.fa.cim.tms.event.recovery.utils.BooleanUtils;
import com.fa.cim.tms.event.recovery.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/2 13:23
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TmsEventRecoveryServiceImpl implements ITmsEventRecoveryService {

    @Value("${tms.user.id}")
    private String userID;

    @Value("${tms.user.password}")
    private String password;

    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IQueueCassetteMethod queueCassetteMethod;
    @Autowired
    private ICarrierEventService carrierEventService;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;
    @Autowired
    private IQueneTransferJobMethod queneTransferJobMethod;
    @Autowired
    private ITransferEventService transferEventService;


    @Override
    public void tmsEventRecoveryReq() {
        User user = new User();
        user.setUserID(ObjectIdentifier.buildWithValue(userID));
        user.setPassword(password);
        user.setFunctionID("M0/M1/M3/M6");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.TM16, user);

        //-------------------------------------
        //  Get Queue Data
        //-------------------------------------
        log.info("【step1】 - queueCassetteMethod.carrierQueGet");
        List<Infos.CarrierQueGetData> carrierQueGetDataList = queueCassetteMethod.carrierQueGet(objCommon);

        //-------------------------------------
        //  Call OMS method for recovery
        //-------------------------------------
        log.info("【step2】 - carrierEventService.tmsCarrierEventRetry");
        if (ArrayUtils.isNotEmpty(carrierQueGetDataList)) {
            for (Infos.CarrierQueGetData carrierQueGetData : carrierQueGetDataList) {
                ObjectIdentifier carrierID = carrierQueGetData.getCarrierID();
                log.info("carrierID: {}", ObjectIdentifier.fetchValue(carrierID));
                Boolean sucessFlag = true;
                try {
                    carrierEventService.tmsCarrierEventRetry(objCommon, carrierQueGetData);
                } catch (ServiceException e) {
                    sucessFlag = false;
                    if (Validations.isEquals(msgRetCodeConfig.getMsgOmsNoResponse(), e.getCode())) {
                        //-------------------------------------
                        //  Re-try same data
                        //-------------------------------------
                        log.info("【step3】 - carrierEventService.tmsCarrierEventRetry");
                        carrierEventService.tmsCarrierEventRetry(objCommon, carrierQueGetData);
                        sucessFlag = true;
                    }
                }
                //-------------------------------------
                //  Delete Queue Data
                //-------------------------------------
                if (BooleanUtils.isTrue(sucessFlag)) {
                    log.info("【step4】 - queueCassetteMethod.carrierQueDel");
                    queueCassetteMethod.carrierQueDel(objCommon, carrierQueGetData);
                }
            }
        }
        //-------------------------------------
        //  Get XferStatus Event Queue Data
        //-------------------------------------
        log.info("【step5】 - queneTransferJobMethod.xferJobEventQueGet");
        List<Infos.XferJobEventQueData> xferJobEventQueDataList = queneTransferJobMethod.xferJobEventQueGet(objCommon);


        //-------------------------------------
        //  Call OMS method for recovery
        //-------------------------------------
        if (ArrayUtils.isNotEmpty(xferJobEventQueDataList)) {
            for (Infos.XferJobEventQueData xferJobEventQueData : xferJobEventQueDataList) {
                log.info("【step6】 - transferEventService.tmsXferJobEventRetry");
                Boolean sucessFlag = true;
                try {
                    transferEventService.tmsXferJobEventRetry(objCommon, xferJobEventQueData);
                } catch (ServiceException e) {
                    sucessFlag = false;
                    if (Validations.isEquals(msgRetCodeConfig.getMsgOmsNoResponse(), e.getCode())) {
                        //-------------------------------------
                        //  Re-try same data
                        //-------------------------------------
                        log.info("【step7】 - transferEventService.tmsXferJobEventRetry");
                        transferEventService.tmsXferJobEventRetry(objCommon, xferJobEventQueData);
                        sucessFlag = true;
                    }
                }
                //-------------------------------------
                //  Delete Queue Data
                //-------------------------------------
                if (BooleanUtils.isTrue(sucessFlag)) {
                    log.info("【step8】 - queneTransferJobMethod.xferJobEventQueDel");
                    queneTransferJobMethod.xferJobEventQueDel(objCommon, xferJobEventQueData);
                }
            }
        }
    }

    @Override
    public void rtmsEventRecoveryReq() {
        User user = new User();
        user.setUserID(ObjectIdentifier.buildWithValue(userID));
        user.setPassword(password);
        user.setFunctionID("M0/M1/M3/M6");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon("", user);

        //-------------------------------------
        //  Get Queue Data
        //-------------------------------------
        log.info("【step1】 - queueCassetteMethod.carrierQueGet");
        List<Infos.CarrierQueGetData> carrierQueGetDataList = queueCassetteMethod.carrierQueGet(objCommon);

        //-------------------------------------
        //  Call OMS method for recovery
        //-------------------------------------
        log.info("【step2】 - carrierEventService.rtmsCarrierEventRetry");
        if (ArrayUtils.isNotEmpty(carrierQueGetDataList)) {
            for (Infos.CarrierQueGetData carrierQueGetData : carrierQueGetDataList) {
                ObjectIdentifier carrierID = carrierQueGetData.getCarrierID();
                log.info("carrierID: {}", ObjectIdentifier.fetchValue(carrierID));
                Boolean sucessFlag = true;
                try {
                    carrierEventService.rtmsCarrierEventRetry(objCommon, carrierQueGetData);
                } catch (ServiceException e) {
                    sucessFlag = false;
                    if (Validations.isEquals(msgRetCodeConfig.getMsgOmsNoResponse(), e.getCode())) {
                        //-------------------------------------
                        //  Re-try same data
                        //-------------------------------------
                        log.info("【step3】 - carrierEventService.rtmsCarrierEventRetry");
                        carrierEventService.rtmsCarrierEventRetry(objCommon, carrierQueGetData);
                        sucessFlag = true;
                    }
                }
                //-------------------------------------
                //  Delete Queue Data
                //-------------------------------------
                if (BooleanUtils.isTrue(sucessFlag)) {
                    log.info("【step4】 - queueCassetteMethod.carrierQueDel");
                    queueCassetteMethod.carrierQueDel(objCommon, carrierQueGetData);
                }
            }
        }
        //-------------------------------------
        //  Get XferStatus Event Queue Data
        //-------------------------------------
        log.info("【step5】 - queneTransferJobMethod.xferJobEventQueGet");
        List<Infos.XferJobEventQueData> xferJobEventQueDataList = queneTransferJobMethod.xferJobEventQueGet(objCommon);


        //-------------------------------------
        //  Call OMS method for recovery
        //-------------------------------------
        if (ArrayUtils.isNotEmpty(xferJobEventQueDataList)) {
            for (Infos.XferJobEventQueData xferJobEventQueData : xferJobEventQueDataList) {
                log.info("【step6】 - transferEventService.rtmsXferJobEventRetry");
                Boolean sucessFlag = true;
                try {
                    transferEventService.rtmsXferJobEventRetry(objCommon, xferJobEventQueData);
                } catch (ServiceException e) {
                    sucessFlag = false;
                    if (Validations.isEquals(msgRetCodeConfig.getMsgOmsNoResponse(), e.getCode())) {
                        //-------------------------------------
                        //  Re-try same data
                        //-------------------------------------
                        log.info("【step7】 - transferEventService.rtmsXferJobEventRetry");
                        transferEventService.rtmsXferJobEventRetry(objCommon, xferJobEventQueData);
                        sucessFlag = true;
                    }
                }
                //-------------------------------------
                //  Delete Queue Data
                //-------------------------------------
                if (BooleanUtils.isTrue(sucessFlag)) {
                    log.info("【step8】 - queneTransferJobMethod.xferJobEventQueDel");
                    queneTransferJobMethod.xferJobEventQueDel(objCommon, xferJobEventQueData);
                }
            }
        }
    }
}
