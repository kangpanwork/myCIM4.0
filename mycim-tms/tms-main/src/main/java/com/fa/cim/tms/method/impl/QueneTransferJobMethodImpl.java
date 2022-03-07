package com.fa.cim.tms.method.impl;

import com.fa.cim.tms.entity.FxqxferjobEntity;
import com.fa.cim.tms.method.IQueneTransferJobMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.support.CustomizeSupport;
import com.fa.cim.tms.utils.BooleanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/14                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/14 17:57
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class QueneTransferJobMethodImpl implements IQueneTransferJobMethod {

    @Autowired
    private CustomizeSupport customizeSupport;

    @Override
    public void xferJobEventQuePut(Infos.ObjCommon objCommon, String operationCategory, List<Infos.TransferJobInfo> transferJobInfoList) {

        Optional.ofNullable(transferJobInfoList).ifPresent(list -> list.forEach(data -> {
            FxqxferjobEntity fxqxferjobEntity = new FxqxferjobEntity();

            fxqxferjobEntity.setOpeCategory(operationCategory);
            fxqxferjobEntity.setTimestamp(objCommon.getTimeStamp().getReportTimeStamp());
            fxqxferjobEntity.setClaimUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));

            fxqxferjobEntity.setCarrierID(ObjectIdentifier.fetchValue(data.getCarrierID()));
            fxqxferjobEntity.setJobID(data.getJobID());
            fxqxferjobEntity.setCarrierJobID(data.getCarrierJobID());
            fxqxferjobEntity.setTransportType(data.getTransportType());
            fxqxferjobEntity.setZoneType(data.getZoneType());
            fxqxferjobEntity.setFromMachineID(ObjectIdentifier.fetchValue(data.getFromMachineID()));
            fxqxferjobEntity.setFromPortID(ObjectIdentifier.fetchValue(data.getFromPortID()));
            fxqxferjobEntity.setToStockerGroup(data.getToStockerGroup());
            fxqxferjobEntity.setToMachineID(ObjectIdentifier.fetchValue(data.getToMachineID()));
            fxqxferjobEntity.setToPortID(ObjectIdentifier.fetchValue(data.getToPortID()));
            fxqxferjobEntity.setExpectedStrtTime(data.getExpectedStartTime());
            fxqxferjobEntity.setExpectedEndTime(data.getExpectedEndTime());
            fxqxferjobEntity.setEstimateStrtTime(data.getEstimatedStartTime());
            fxqxferjobEntity.setEstimateEndTime(data.getEstimatedEndTime());
            fxqxferjobEntity.setPriority(data.getPriority());
            fxqxferjobEntity.setJobStatus(data.getJobStatus());
            fxqxferjobEntity.setCarrierJobStatus(data.getCarrierJobStatus());

            fxqxferjobEntity.setN2PurgeFlag(null == BooleanUtils.convertBooleanToLong(data.getN2PurgeFlag()) ? null : BooleanUtils.convertBooleanToLong(data.getN2PurgeFlag()).intValue());
            fxqxferjobEntity.setMandatoryFlag(null == BooleanUtils.convertBooleanToLong(data.getN2PurgeFlag()) ? null : BooleanUtils.convertBooleanToLong(data.getN2PurgeFlag()).intValue());
            customizeSupport.saveNonRuntimeEntity(fxqxferjobEntity);
        }));
    }

}
