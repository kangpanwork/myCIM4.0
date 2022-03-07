package com.fa.cim.tms.method.impl;

import com.fa.cim.tms.entity.FxqstatusEntity;
import com.fa.cim.tms.entity.NonRuntimeEntity;
import com.fa.cim.tms.method.IQueneStatusMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.support.CustomizeSupport;
import com.fa.cim.tms.utils.ArrayUtils;
import com.fa.cim.tms.utils.BooleanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/14                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/14 17:55
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class QueneStatusMethodImpl implements IQueneStatusMethod {
    @Autowired
    private CustomizeSupport customizeSupport;


    @Override
    public Long statQueCheck(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        FxqstatusEntity fxqstatusEntityExam = new FxqstatusEntity();
        fxqstatusEntityExam.setStockerID(ObjectIdentifier.fetchValue(stockerID));
        return customizeSupport.count(fxqstatusEntityExam);
    }

    @Override
    public void statQuePut(Infos.ObjCommon objCommon, Timestamp timestamp, ObjectIdentifier stockerID, String sotckerStatusCode, Boolean updateFlag) {
        if (BooleanUtils.isTrue(updateFlag)) {
            log.info("delete the old record by stockerID: {}", ObjectIdentifier.fetchValue(stockerID));
            FxqstatusEntity fxqstatusEntityExam = new FxqstatusEntity();
            fxqstatusEntityExam.setStockerID(ObjectIdentifier.fetchValue(stockerID));
            customizeSupport.removeNonRuntimeEntityForExample(fxqstatusEntityExam);
        }
        FxqstatusEntity fxqstatusEntity = new FxqstatusEntity();
        fxqstatusEntity.setTimestamp(timestamp);
        fxqstatusEntity.setStockerID(ObjectIdentifier.fetchValue(stockerID));
        fxqstatusEntity.setStockerStatus(sotckerStatusCode);
        customizeSupport.saveNonRuntimeEntity(fxqstatusEntity);
    }

}
