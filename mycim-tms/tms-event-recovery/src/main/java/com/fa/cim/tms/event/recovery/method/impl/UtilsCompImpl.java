package com.fa.cim.tms.event.recovery.method.impl;

import com.fa.cim.tms.event.recovery.dto.User;
import com.fa.cim.tms.event.recovery.enums.TransactionIDEnum;
import com.fa.cim.tms.event.recovery.method.IUtilsComp;
import com.fa.cim.tms.event.recovery.pojo.Infos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/4/28        *******              jerry              create file
 *
 * @author: jerry
 * @date: 2018/4/28 17:52
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class UtilsCompImpl implements IUtilsComp {

    @Override
    public Infos.ObjCommon setObjCommon(TransactionIDEnum transactionIDEnum, User user) {
        return this.setObjCommon(transactionIDEnum.getValue(), user);
    }

    @Override
    public Infos.ObjCommon setObjCommon(String transactionID, User user) {
        Infos.ObjCommon objCommon = new Infos.ObjCommon();
        objCommon.setTransactionID(transactionID);
        objCommon.setUser(user);
        Infos.TimeStamp timeStamp = new Infos.TimeStamp();
        timeStamp.setReportTimeStamp(new Timestamp(System.currentTimeMillis()));
        objCommon.setTimeStamp(timeStamp);
        return objCommon;
    }
}