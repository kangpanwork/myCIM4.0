package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.BaseStaticMethod;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.ICimComp;
import com.fa.cim.method.IUtilsComp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@OmMethod
@Slf4j
public class UtilsCompImpl implements IUtilsComp {

    @Autowired
    private ICimComp cimComp;

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public Infos.ObjCommon setObjCommon(TransactionIDEnum transactionIDEnum, User user) {
        return this.setObjCommon(transactionIDEnum.getValue(), user);
    }

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public Infos.ObjCommon setObjCommon(String transactionID, User user) {
        Infos.ObjCommon objCommon = getPPTObjCommonInInstance(transactionID, user);
        log.info("setObjCommon(): ReportTimeStamp is " + objCommon.getTimeStamp().getReportTimeStamp());
        /* [bear-20200304]note schedule plan, because of the mds data can't do release now.
        Infos.TimeStamp cimTimeStamp = cimComp.getSchedule(BaseStaticMethod.getTimeStampInString(objCommon.getTimeStamp().getReportTimeStamp()));
        objCommon.setTimeStamp(cimTimeStamp);
         */
        return objCommon;
    }

    private Infos.ObjCommon getPPTObjCommonInInstance(String transactionID, User requestUserID) {
        Infos.ObjCommon objCommon = new Infos.ObjCommon();
        objCommon.setTransactionID(transactionID);
        objCommon.setUser(requestUserID);
        Infos.TimeStamp timeStamp = new Infos.TimeStamp();
        timeStamp.setReportTimeStamp(new Timestamp(System.currentTimeMillis()));
        objCommon.setTimeStamp(timeStamp);
        return objCommon;
    }
}