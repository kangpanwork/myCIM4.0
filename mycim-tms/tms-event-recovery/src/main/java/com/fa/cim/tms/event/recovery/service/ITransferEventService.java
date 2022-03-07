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
 * @date: 2020/11/2 16:24
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ITransferEventService {

    void tmsXferJobEventRetry(Infos.ObjCommon objCommon, Infos.XferJobEventQueData xferJobEventQueData);

    void rtmsXferJobEventRetry(Infos.ObjCommon objCommon, Infos.XferJobEventQueData xferJobEventQueData);
}
