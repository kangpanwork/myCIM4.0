package com.fa.cim.pcs.entity.utils;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * This class provides some special methods common to scripts.
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 13:28
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Component
@Slf4j
public class System {

    /**
     * Executes an external program command.
     *
     * @param path   shell script path
     * @param params parameters
     * @return the first line that echo into the input stream
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 12:16
     */
    public String call(String path, @Nullable String... params) {
        Validations.check(CimStringUtils.isEmpty(path), "The path of script command is null. Please check and try again.");
        try {
            StringBuilder cmd = new StringBuilder(path);
            Optional.ofNullable(params).ifPresent(array -> {
                for (String param : array) {
                    cmd.append(" ").append(param);
                }
            });
            Process exec = Runtime.getRuntime().exec(cmd.toString());
            return new BufferedReader(new InputStreamReader(exec.getInputStream())).readLine();
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
