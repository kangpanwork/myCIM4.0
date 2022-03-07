package com.fa.cim.tms.event.recovery.method;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/2 13:09
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEnvMethod {

    String getEnvFromOmsDB(String envID);
}
