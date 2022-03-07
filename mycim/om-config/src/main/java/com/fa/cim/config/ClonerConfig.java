package com.fa.cim.config;

import com.rits.cloning.Cloner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/3/19          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/3/19 9:31
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Configuration
public class ClonerConfig {
    @Bean
    public Cloner getCloner(){
        Cloner cloner = new Cloner();
        cloner.setDumpClonedClasses(false);
        return cloner;
    }
}