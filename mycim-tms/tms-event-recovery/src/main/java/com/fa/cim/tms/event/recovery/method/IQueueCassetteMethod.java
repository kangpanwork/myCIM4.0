package com.fa.cim.tms.event.recovery.method;

import com.fa.cim.tms.event.recovery.pojo.Infos;

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
public interface IQueueCassetteMethod {

    List<Infos.CarrierQueGetData> carrierQueGet(Infos.ObjCommon objCommon);

    void carrierQueDel(Infos.ObjCommon objCommon, Infos.CarrierQueGetData carrierQueGetData);
}
