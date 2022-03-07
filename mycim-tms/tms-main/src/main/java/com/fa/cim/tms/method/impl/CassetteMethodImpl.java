package com.fa.cim.tms.method.impl;

import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.method.ICassetteMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.support.CustomizeSupport;
import com.fa.cim.tms.utils.ArrayUtils;
import com.fa.cim.tms.utils.StringUtils;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/14                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/14 16:59
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class CassetteMethodImpl implements ICassetteMethod {

    @Autowired
    private CustomizeSupport customizeSupport;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    @Override
    public List<ObjectIdentifier> cassetteAllDR(Infos.ObjCommon objCommon) {
        List<Object> queryList = customizeSupport.oneResultListQuery("SELECT CARRIER_ID FROM OMCARRIER");
        Validations.check(ArrayUtils.isEmpty(queryList), msgRetCodeConfig.getMsgRecordNotFound());
        return queryList.stream().map(data -> ObjectIdentifier.buildWithValue(StringUtils.toString(data))).collect(Collectors.toList());
    }


    @Override
    public Results.DRcassetteXfstatResult cassetteXfstatGetDR(Infos.ObjCommon objCommon, ObjectIdentifier carrierID) {
        Results.DRcassetteXfstatResult result = new Results.DRcassetteXfstatResult();
        List<Object[]> queryResult = customizeSupport.query("SELECT TRANS_STATE,EQP_ID FROM FRCAST WHERE CAST_ID = ?1", ObjectIdentifier.fetchValue(carrierID));
        if (ArrayUtils.isNotEmpty(queryResult)) {
            String castEqpID = StringUtils.toString(queryResult.get(0)[1]);
            String transferStatus = StringUtils.toString(queryResult.get(0)[0]);
            long totalCount = customizeSupport.count("SELECT COUNT(*) FROM OMSTOCKER WHERE STOCKER_ID = ?1", castEqpID);
            result.setTransferStatus(transferStatus);
            if (totalCount > 0) {
                result.setStkID(castEqpID);
            } else {
                result.setEqpID(castEqpID);
            }
        } else {
            Validations.check(true, msgRetCodeConfig.getMsgRecordNotFound());
        }
        return result;
    }

    @Override
    public List<ObjectIdentifier> rtmsCassetteAllDR(Infos.ObjCommon objCommon) {
        List<Object> queryList = customizeSupport.oneResultListQuery("SELECT RTCLPOD_ID FROM OMRTCLPOD");
        Validations.check(ArrayUtils.isEmpty(queryList), msgRetCodeConfig.getMsgRecordNotFound());
        return queryList.stream().map(data -> ObjectIdentifier.buildWithValue(StringUtils.toString(data))).collect(Collectors.toList());
    }

    @Override
    public Infos.CarrierCurrentLocationGetDR carrierCurrentLocationGetDR(Infos.ObjCommon objCommon, ObjectIdentifier carrierID) {
        Infos.CarrierCurrentLocationGetDR result = new Infos.CarrierCurrentLocationGetDR();
        //=========================================================================
        // Get reticle list into output structure
        //=========================================================================
        Object[] queryResult = customizeSupport.queryOne("SELECT XFER_STATE, EQP_ID FROM OMRTCLPOD WHERE RTCLPOD_ID = ?1",
                ObjectIdentifier.fetchValue(carrierID));
        if (null != queryResult){
            result.setCarrierID(carrierID);
            result.setMachineID(ObjectIdentifier.buildWithValue(StringUtils.toString(queryResult[1])));
            result.setTransferStatus(StringUtils.toString(queryResult[0]));
        }
        return result;
    }

    @Override
    public Infos.ReticlePodXferStat reticlePodXferStatGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {
        Infos.ReticlePodXferStat result = new Infos.ReticlePodXferStat();
        Object[] queryResult = customizeSupport.queryOne("select XFER_STATE,EQP_ID,STK_ID from OMRTCLPOD where RTCLPOD_ID = ?1", ObjectIdentifier.fetchValue(reticlePodID));
        Validations.check(null == queryResult,msgRetCodeConfig.getMsgRecordNotFound());

        String transferState = StringUtils.toString(queryResult[0]);
        String eqpID = StringUtils.toString(queryResult[1]);
        String stkID = StringUtils.toString(queryResult[2]);

        long totalCount = customizeSupport.count("SELECT COUNT(*) FROM OMSTOCKER WHERE STOCKER_ID = ?1", eqpID);
        result.setTransferStatus(transferState);
        if (totalCount > 0){
            result.setStkID(eqpID);
        }else {
            result.setEqpID(eqpID);
        }
        return result;
    }
}
