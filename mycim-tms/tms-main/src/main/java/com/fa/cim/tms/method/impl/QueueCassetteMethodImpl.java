package com.fa.cim.tms.method.impl;

import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.tms.common.enums.EnvCodeEnum;
import com.fa.cim.tms.entity.FxqcastEntity;
import com.fa.cim.tms.entity.NonRuntimeEntity;
import com.fa.cim.tms.method.IEnvMethod;
import com.fa.cim.tms.method.IQueueCassetteMethod;
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
 * @date: 2020/10/14 17:53
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class QueueCassetteMethodImpl implements IQueueCassetteMethod {

    @Autowired
    private CustomizeSupport customizeSupport;

    @Autowired
    private IEnvMethod envMethod;




    @Override
    public Long carrierQueCheck(Infos.ObjCommon objCommon, ObjectIdentifier carrierID) {
        FxqcastEntity example = new FxqcastEntity();
        example.setCarrierID(ObjectIdentifier.fetchValue(carrierID));
        return customizeSupport.count(example);
    }

    @Override
    public void carrierQuePut(Infos.ObjCommon objCommon, Timestamp timestamp, ObjectIdentifier carrierID, String jobID, String carrierJobID, String eventType, String eventStatus, ObjectIdentifier machineID, ObjectIdentifier portID, String xferStatus, Boolean updateFlag,Boolean tmsFlag) {
        if (BooleanUtils.isTrue(tmsFlag)) {
            //TMS Fucntion carrierQuePut
            log.info("TMS Fucntion carrierQuePut");
            String errorRet = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM06_ERROR_RETURN.getValue());//"ON";
            log.info("evn OM_TM06_ERROR_RETURN: {}", errorRet);
            if (Constant.TM_ON.equals(errorRet) &&
                    Constant.TM_EVENTTYPE_M6.equals(eventStatus)) {
                log.info("errorRet=ON && eventType=M6");
                log.info("This event is not stored!");
                return;
            }//origin anotation
        }else {
            //RTMS Fucntion carrierQuePut
            log.info("RTMS Fucntion carrierQuePut");
        }
        String updateByEvent = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_UPDATE_BY_EVENT.getValue());//"ON"
        log.info("evn OM_QUE_UPDATE_BY_EVENT: {}", updateByEvent);
        if (BooleanUtils.isTrue(updateFlag) && Constant.TM_ON.equals(updateByEvent)) {
            log.info("delete OTQCARRIER by carrierID");
            FxqcastEntity fxqcastEntityExam = new FxqcastEntity();
            fxqcastEntityExam.setCarrierID(ObjectIdentifier.fetchValue(carrierID));
            customizeSupport.removeNonRuntimeEntityForExample(fxqcastEntityExam);
        }
        if (Constant.TM_ON.equals(updateByEvent)) {
            log.info("delete OTQCARRIER by carrierID and eventType");
            FxqcastEntity fxqcastEntityExam = new FxqcastEntity();
            fxqcastEntityExam.setCarrierID(ObjectIdentifier.fetchValue(carrierID));
            fxqcastEntityExam.setEventType(eventType);
            customizeSupport.removeNonRuntimeEntityForExample(fxqcastEntityExam);
        }
        FxqcastEntity fxqcastEntity = new FxqcastEntity();
        fxqcastEntity.setTimestamp(timestamp);
        fxqcastEntity.setCarrierID(ObjectIdentifier.fetchValue(carrierID));
        fxqcastEntity.setCarrierJobID(carrierJobID);
        fxqcastEntity.setEventStatus(eventStatus);
        fxqcastEntity.setEventType(eventType);
        fxqcastEntity.setJobID(jobID);
        fxqcastEntity.setMachineID(ObjectIdentifier.fetchValue(machineID));
        fxqcastEntity.setPortID(ObjectIdentifier.fetchValue(portID));
        fxqcastEntity.setTransferStatus(xferStatus);
        customizeSupport.saveNonRuntimeEntity(fxqcastEntity);
    }


    private <S extends NonRuntimeEntity> void removeEntitys(String querySql, Class<S> clz, Object... objects) {
        List<S> objs = customizeSupport.query(querySql, clz, objects);
        if (ArrayUtils.isNotEmpty(objs)) {
            objs.forEach(x -> customizeSupport.removeNonRuntimeEntity(x));
        }
    }
}
