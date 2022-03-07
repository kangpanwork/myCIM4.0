package com.fa.cim.tms.status.recovery.method;

import com.fa.cim.tms.status.recovery.pojo.Infos;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/14                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/14 17:55
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IQueneStatusMethod {

    List<Infos.StatQueGetData> statQueGet(Infos.ObjCommon objCommon);

    void statQueDel(Infos.ObjCommon objCommon, Infos.StatQueGetData statQueGetData);
}
