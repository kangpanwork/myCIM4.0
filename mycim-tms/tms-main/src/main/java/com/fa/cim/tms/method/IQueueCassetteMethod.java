package com.fa.cim.tms.method;

import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;

import java.sql.Timestamp;

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


    Long carrierQueCheck(Infos.ObjCommon objCommon, ObjectIdentifier carrierID);

    void carrierQuePut(Infos.ObjCommon objCommon, Timestamp timestamp, ObjectIdentifier carrierID, String jobID, String carrierJobID, String eventType, String eventStatus, ObjectIdentifier machineID, ObjectIdentifier portID, String xferStatus, Boolean updateFlag,Boolean tmsFlag);
}
