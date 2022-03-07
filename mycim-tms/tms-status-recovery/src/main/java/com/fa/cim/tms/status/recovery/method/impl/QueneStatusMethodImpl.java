package com.fa.cim.tms.status.recovery.method.impl;

import com.fa.cim.tms.status.recovery.entity.FxqstatusEntity;
import com.fa.cim.tms.status.recovery.entity.NonRuntimeEntity;
import com.fa.cim.tms.status.recovery.method.IQueneStatusMethod;
import com.fa.cim.tms.status.recovery.pojo.Infos;
import com.fa.cim.tms.status.recovery.pojo.ObjectIdentifier;
import com.fa.cim.tms.status.recovery.support.CustomizeSupport;
import com.fa.cim.tms.status.recovery.utils.ArrayUtils;
import com.fa.cim.tms.status.recovery.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/3 9:50
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Component
public class QueneStatusMethodImpl implements IQueneStatusMethod {

    @Autowired
    private CustomizeSupport customizeSupport;

    @Override
    public List<Infos.StatQueGetData> statQueGet(Infos.ObjCommon objCommon) {
        return customizeSupport.findAll(FxqstatusEntity.class).stream().map(entity -> {
            Infos.StatQueGetData info = new Infos.StatQueGetData();
            info.setTimeStamp(DateUtils.convertToSpecString(entity.getTimestamp()));
            info.setStockerID(ObjectIdentifier.buildWithValue(entity.getStockerID()));
            info.setStockerStatus(ObjectIdentifier.buildWithValue(entity.getStockerStatus()));
            return info;
        }).collect(Collectors.toList());
    }

    @Override
    public void statQueDel(Infos.ObjCommon objCommon, Infos.StatQueGetData statQueGetData) {
        try {
            FxqstatusEntity fxqstatusEntityExam = new FxqstatusEntity();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh24:mi:ss.ff");
            fxqstatusEntityExam.setTimestamp(new Timestamp(sdf.parse(statQueGetData.getTimeStamp()).getTime()));
            fxqstatusEntityExam.setStockerID(ObjectIdentifier.fetchValue(statQueGetData.getStockerID()));
            customizeSupport.removeNonRuntimeEntityForExample(fxqstatusEntityExam);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private <S extends NonRuntimeEntity> void removeEntitys(String querySql, Class<S> clz, Object... objects) {
        List<S> objs = customizeSupport.query(querySql, clz, objects);
        if (ArrayUtils.isNotEmpty(objs)) {
            objs.forEach(x -> customizeSupport.removeNonRuntimeEntity(x));
        }
    }
}
