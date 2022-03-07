package com.fa.cim.tms.event.recovery.service;

import com.fa.cim.tms.event.recovery.pojo.Infos;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/2 14:26
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ICarrierEventService {

    void tmsCarrierEventRetry(Infos.ObjCommon objCommon, Infos.CarrierQueGetData carrierQueGetData);

    void rtmsCarrierEventRetry(Infos.ObjCommon objCommon, Infos.CarrierQueGetData carrierQueGetData);
}
