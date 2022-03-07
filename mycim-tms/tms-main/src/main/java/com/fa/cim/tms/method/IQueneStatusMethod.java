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
 * @date: 2020/10/14 17:55
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IQueneStatusMethod {

    Long statQueCheck(Infos.ObjCommon objCommon, ObjectIdentifier stockerID);

    void statQuePut(Infos.ObjCommon objCommon, Timestamp timestamp, ObjectIdentifier stockerID, String sotckerStatusCode, Boolean updateFlag);
}
