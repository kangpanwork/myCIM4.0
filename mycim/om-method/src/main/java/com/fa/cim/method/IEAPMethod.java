package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import com.fa.cim.remote.IEAPRemoteManager;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/18                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/12/18 20:12
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEAPMethod {

    IEAPRemoteManager eapRemoteManager(Infos.ObjCommon objCommon,
                                       User user,
                                       ObjectIdentifier equipmentID,
                                       ObjectIdentifier stockerID,
                                       Boolean onlineModeCheck);
}
