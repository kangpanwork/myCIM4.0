package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.service.ICarrierInfoInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 13:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class CarrierInfoInqService implements ICarrierInfoInqService {
    @Autowired
    private IOMSManager iomsManager;

    public Results.CarrierInfoInqResult sxCarrierInfoInq(Infos.ObjCommon objCommon, Params.CarrierInfoInqParam carrierInfoInqParam) {
        Results.CarrierInfoInqResult result = new Results.CarrierInfoInqResult();
        ObjectIdentifier cassetteID = carrierInfoInqParam.getCarrierID();
        Results.CarrierDetailInfoInqResult cassetteStatusInqResult = cassetteStatusInqResult = iomsManager.sendCassetteStatusInq(objCommon, cassetteID);
        result.setZoneType(cassetteStatusInqResult.getCassetteStatusInfo().getZoneType());
        result.setCarrierID(cassetteStatusInqResult.getCassetteID());
        return result;

    }

    @Override
    public Results.CarrierInfoInqResult sxRtmsCarrierInfoInq(Infos.ObjCommon objCommon, Params.CarrierInfoInqParam carrierInfoInqParam) {
        Results.CarrierInfoInqResult result = new Results.CarrierInfoInqResult();
        ObjectIdentifier reticlePodID = carrierInfoInqParam.getCarrierID();
        Results.ReticlePodStatusInqResult reticlePodStatusInqResult = iomsManager.sendReticlePodStatusInq(objCommon, reticlePodID);
        result.setCarrierID(reticlePodStatusInqResult.getReticlePodID());
        return result;
    }
}
