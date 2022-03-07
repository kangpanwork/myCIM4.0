package com.fa.cim.tms.event.recovery.method.impl;

import com.fa.cim.tms.event.recovery.entity.FxqcastEntity;
import com.fa.cim.tms.event.recovery.entity.NonRuntimeEntity;
import com.fa.cim.tms.event.recovery.method.IQueueCassetteMethod;
import com.fa.cim.tms.event.recovery.pojo.Infos;
import com.fa.cim.tms.event.recovery.pojo.ObjectIdentifier;
import com.fa.cim.tms.event.recovery.support.CustomizeSupport;
import com.fa.cim.tms.event.recovery.utils.ArrayUtils;
import com.fa.cim.tms.event.recovery.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/2 13:19
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Component
public class QueueCassetteMethodImpl implements IQueueCassetteMethod {

    @Autowired
    private CustomizeSupport customizeSupport;

    @Override
    public List<Infos.CarrierQueGetData> carrierQueGet(Infos.ObjCommon objCommon) {
        return customizeSupport.findAll(FxqcastEntity.class).stream()
                .sorted(Comparator.comparing(FxqcastEntity::getCarrierID))
                .sorted(Comparator.comparing(FxqcastEntity::getTimestamp))
                .map(entity -> {
                    Infos.CarrierQueGetData carrierQueGetData = new Infos.CarrierQueGetData();
                    carrierQueGetData.setTimeStamp(DateUtils.convertToSpecString(entity.getTimestamp()));
                    carrierQueGetData.setCarrierID(ObjectIdentifier.buildWithValue(entity.getCarrierID()));
                    carrierQueGetData.setJobID(entity.getJobID());
                    carrierQueGetData.setCarrierJobID(entity.getCarrierJobID());
                    carrierQueGetData.setEventType(entity.getEventType());
                    carrierQueGetData.setEventStatus(entity.getEventStatus());
                    carrierQueGetData.setMachineID(ObjectIdentifier.buildWithValue(entity.getMachineID()));
                    carrierQueGetData.setPortID(ObjectIdentifier.buildWithValue(entity.getPortID()));
                    carrierQueGetData.setTransferStatus(entity.getTransferStatus());
                    return carrierQueGetData;
                }).collect(Collectors.toList());
    }

    @Override
    public void carrierQueDel(Infos.ObjCommon objCommon, Infos.CarrierQueGetData carrierQueGetData) {
        this.removeEntitys("SELECT * FROM OTQCARRIER WHERE CREATE_TIME = TO_TIMESTAMP(?1,'yyyy-mm-dd hh24:mi:ss.ff') AND CARRIER_ID = ?2",
                FxqcastEntity.class, carrierQueGetData.getTimeStamp(), ObjectIdentifier.fetchValue(carrierQueGetData.getCarrierID()));
    }

    private <S extends NonRuntimeEntity> void removeEntitys(String querySql, Class<S> clz, Object... objects) {
        List<S> objs = customizeSupport.query(querySql, clz, objects);
        if (ArrayUtils.isNotEmpty(objs)) {
            objs.forEach(x -> customizeSupport.removeNonRuntimeEntity(x));
        }
    }
}
