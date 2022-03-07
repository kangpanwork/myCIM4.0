package com.fa.cim.tms.event.recovery.method.impl;

import com.fa.cim.tms.event.recovery.method.IEnvMethod;
import com.fa.cim.tms.event.recovery.support.CustomizeSupport;
import com.fa.cim.tms.event.recovery.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/2 13:10
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
public class EnvMethodImpl implements IEnvMethod {
    @Autowired
    private CustomizeSupport customizeSupport;

    public String getEnvFromOmsDB(String envID) {
        List<Object[]> results = customizeSupport.query("SELECT ENV_VALUE FROM OMENV WHERE ENV_ID = ?", envID.trim());
        return Optional.ofNullable(results).map(list -> StringUtils.toString(list.get(0)[0])).orElse(null);
    }
}
