package com.fa.cim.tms.service;

import com.fa.cim.tms.pojo.Infos;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/12                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/12 15:13
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IOnlineMcsInqService {

    void sxOnlineMcsInq(Infos.ObjCommon objCommon);

    void sxRtmsOnlineMcsInq(Infos.ObjCommon objCommon);
}
