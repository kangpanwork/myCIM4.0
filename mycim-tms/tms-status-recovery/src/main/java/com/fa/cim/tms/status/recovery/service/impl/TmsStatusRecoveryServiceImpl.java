package com.fa.cim.tms.status.recovery.service.impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.status.recovery.config.MsgRetCodeConfig;
import com.fa.cim.tms.status.recovery.dto.Results;
import com.fa.cim.tms.status.recovery.dto.User;
import com.fa.cim.tms.status.recovery.enums.TransactionIDEnum;
import com.fa.cim.tms.status.recovery.manager.IOMSManager;
import com.fa.cim.tms.status.recovery.method.IQueneStatusMethod;
import com.fa.cim.tms.status.recovery.method.IUtilsComp;
import com.fa.cim.tms.status.recovery.pojo.Infos;
import com.fa.cim.tms.status.recovery.pojo.ObjectIdentifier;
import com.fa.cim.tms.status.recovery.service.ITmsStatusRecoveryService;
import com.fa.cim.tms.status.recovery.utils.ArrayUtils;
import com.fa.cim.tms.status.recovery.utils.BooleanUtils;
import com.fa.cim.tms.status.recovery.utils.Validations;
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
 * 2020/11/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/3 9:37
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TmsStatusRecoveryServiceImpl implements ITmsStatusRecoveryService {

    @Value("${tms.user.id}")
    private String userID;
    @Value("${tms.user.password}")
    private String password;

    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private IQueneStatusMethod queneStatusMethod;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;


    @Override
    public void tmsStatusRecoveryReq() {
        User user = new User();
        user.setUserID(ObjectIdentifier.buildWithValue(userID));
        user.setPassword(password);
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.NULL, user);
        //-------------------------------------
        //  Get Queue Data
        //-------------------------------------
        log.info("【step1】 - queneStatusMethod.statQueGet");
        List<Infos.StatQueGetData> statQueGetDataList = queneStatusMethod.statQueGet(objCommon);

        //-------------------------------------
        //  Call OMS method for recovery
        //-------------------------------------
        if (ArrayUtils.isNotEmpty(statQueGetDataList)) {
            for (Infos.StatQueGetData statQueGetData : statQueGetDataList) {
                Results.E10StatusReportResult result = null;
                log.info("【step2】 - omsManager.sendStockerStatusChangeRpt");
                Boolean successFlag = true;
                try {
                    result = omsManager.sendStockerStatusChangeRpt(objCommon,
                            statQueGetData.getStockerID(),
                            statQueGetData.getStockerStatus(),
                            null);
                } catch (ServiceException e) {
                    successFlag = false;
                    if (Validations.isEquals(msgRetCodeConfig.getMsgOmsNoResponse(), e.getCode())) {
                        log.info("【step3】 - omsManager.sendStockerStatusChangeRpt");
                        //-------------------------------------
                        //  Re-try same data
                        //-------------------------------------
                        result = omsManager.sendStockerStatusChangeRpt(objCommon,
                                statQueGetData.getStockerID(),
                                statQueGetData.getStockerStatus(),
                                null);
                        successFlag = true;
                    }
                }
                //-------------------------------------
                //  Delete Queue Data
                //-------------------------------------
                if (BooleanUtils.isTrue(successFlag)) {
                    log.info("【step4】 - omsManager.sendStockerStatusChangeRpt");
                    queneStatusMethod.statQueDel(objCommon, statQueGetData);
                }
            }
        }
    }
}
