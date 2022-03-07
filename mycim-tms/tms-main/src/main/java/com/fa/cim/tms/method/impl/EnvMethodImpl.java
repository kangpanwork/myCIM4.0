package com.fa.cim.tms.method.impl;

import com.fa.cim.tms.method.IEnvMethod;
import com.fa.cim.tms.support.CustomizeSupport;
import com.fa.cim.tms.utils.ArrayUtils;
import com.fa.cim.tms.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/14                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/14 17:34
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class EnvMethodImpl implements IEnvMethod {

    @Autowired
    private CustomizeSupport customizeSupport;

    public String getEnvFromOmsDB(String envID) {
        List<Object[]> results = customizeSupport.query("SELECT ENV_VALUE FROM OMENV WHERE ENV_ID = ?", envID.trim());
        if (ArrayUtils.isNotEmpty(results)){
            return StringUtils.toString(results.get(0)[0]);
        }else {
            return null;
        }
    }
}
