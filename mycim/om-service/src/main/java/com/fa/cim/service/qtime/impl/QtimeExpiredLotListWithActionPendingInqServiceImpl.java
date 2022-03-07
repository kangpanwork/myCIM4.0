package com.fa.cim.service.qtime.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.IQTimeMethod;
import com.fa.cim.service.qtime.IQtimeExpiredLotListWithActionPendingInqService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/7        ********             Jerry               create file
 *
 * @author: Jerry
 * @date: 2018/11/7 15:32
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@OmService
public class QtimeExpiredLotListWithActionPendingInqServiceImpl implements IQtimeExpiredLotListWithActionPendingInqService {

    @Autowired
    private IQTimeMethod qTimeMethod;

    @Override
    public List<ObjectIdentifier> sxQtimeExpiredLotListWithActionPendingInq(Infos.ObjCommon strObjCommonIn, Params.QtimeExpiredLotListWithActionPendingInqInParm strQtimeExpiredLotListWithActionPendingInqInParm) {
        Infos.ExpiredQrestTimeLotListGetDROut strExpiredQrestTimeLotListGetDROut;
        Infos.ExpiredQrestTimeLotListGetDRIn strExpiredQrestTimeLotListGetDRIn=new Infos.ExpiredQrestTimeLotListGetDRIn();
        strExpiredQrestTimeLotListGetDRIn.setMaxRetrieveCount(strQtimeExpiredLotListWithActionPendingInqInParm.getMaxRetrieveCount());
        // step1 - expiredQrestTime_lotList_GetDR
        strExpiredQrestTimeLotListGetDROut = qTimeMethod.expiredQrestTimeLotListGetDR(strObjCommonIn, strExpiredQrestTimeLotListGetDRIn );
        return strExpiredQrestTimeLotListGetDROut.getLotIDs();
    }

}