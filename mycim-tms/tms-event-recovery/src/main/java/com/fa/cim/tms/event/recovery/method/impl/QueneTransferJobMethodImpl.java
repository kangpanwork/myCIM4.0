package com.fa.cim.tms.event.recovery.method.impl;

import com.fa.cim.tms.event.recovery.entity.FxqxferjobEntity;
import com.fa.cim.tms.event.recovery.entity.NonRuntimeEntity;
import com.fa.cim.tms.event.recovery.method.IQueneTransferJobMethod;
import com.fa.cim.tms.event.recovery.pojo.Infos;
import com.fa.cim.tms.event.recovery.pojo.ObjectIdentifier;
import com.fa.cim.tms.event.recovery.support.CustomizeSupport;
import com.fa.cim.tms.event.recovery.utils.ArrayUtils;
import com.fa.cim.tms.event.recovery.utils.BooleanUtils;
import com.fa.cim.tms.event.recovery.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
 * @date: 2020/11/2 13:20
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Component
public class QueneTransferJobMethodImpl implements IQueneTransferJobMethod {
    @Autowired
    private CustomizeSupport customizeSupport;

    @Override
    public List<Infos.XferJobEventQueData> xferJobEventQueGet(Infos.ObjCommon objCommon) {
        return customizeSupport.findAll(FxqxferjobEntity.class).stream()
                .sorted(Comparator.comparing(FxqxferjobEntity::getTimestamp))
                .map(entity -> {
                    Infos.XferJobEventQueData data = new Infos.XferJobEventQueData();
                    data.setOperationCategory(entity.getOpeCategory());
                    data.setCarrierID(ObjectIdentifier.buildWithValue(entity.getCarrierID()));
                    data.setTransportType(entity.getTransportType());
                    data.setZoneType(entity.getZoneType());
                    data.setFromMachineID(ObjectIdentifier.buildWithValue(entity.getFromMachineID()));
                    data.setFromPortID(ObjectIdentifier.buildWithValue(entity.getFromPortID()));
                    data.setToStockerGroup(entity.getToStockerGroup());
                    data.setToMachineID(ObjectIdentifier.buildWithValue(entity.getToMachineID()));
                    data.setToPortID(ObjectIdentifier.buildWithValue(entity.getToPortID()));
                    data.setExpectedStartTime(entity.getExpectedStrtTime());
                    data.setExpectedEndTime(entity.getExpectedEndTime());
                    data.setEstimatedStartTime(entity.getEstimateStrtTime());
                    data.setEstimatedEndTime(entity.getEstimateEndTime());
                    data.setPriority(entity.getPriority());
                    data.setJobStatus(entity.getJobStatus());
                    data.setCarrierJobStatus(entity.getCarrierJobStatus());
                    data.setTimestamp(DateUtils.convertToSpecString(entity.getTimestamp()));
                    data.setClaimUserID(entity.getClaimUserID());
                    data.setN2PurgeFlag(BooleanUtils.convert(entity.getN2PurgeFlag()));
                    data.setMandatoryFlag(BooleanUtils.convert(entity.getMandatoryFlag()));
                    return data;
                }).collect(Collectors.toList());
    }

    @Override
    public void xferJobEventQueDel(Infos.ObjCommon objCommon, Infos.XferJobEventQueData xferJobEventQueData) {
        try {
            FxqxferjobEntity fxqxferjobEntityExam = new FxqxferjobEntity();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh24:mi:ss.ff");
            fxqxferjobEntityExam.setTimestamp(new Timestamp(sdf.parse(xferJobEventQueData.getTimestamp()).getTime()));
            fxqxferjobEntityExam.setOpeCategory(xferJobEventQueData.getOperationCategory());
            fxqxferjobEntityExam.setCarrierID(ObjectIdentifier.fetchValue(xferJobEventQueData.getCarrierID()));
            customizeSupport.removeNonRuntimeEntityForExample(fxqxferjobEntityExam);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
