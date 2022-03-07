package com.fa.cim.tms.method;

import com.fa.cim.tms.pojo.Infos;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/14                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/14 17:57
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IQueneTransferJobMethod {

    void xferJobEventQuePut(Infos.ObjCommon objCommon, String operationCategory, List<Infos.TransferJobInfo> transferJobInfoList);

}
