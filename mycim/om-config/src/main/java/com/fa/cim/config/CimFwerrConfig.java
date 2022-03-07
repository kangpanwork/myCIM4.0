package com.fa.cim.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * description:
 *
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/4/23        ********            Bear               create file
 *
 * @author: Bear
 * @date: 2018/4/23 10:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@PropertySource("classpath:cim-fwerr.properties")
@ConfigurationProperties(prefix="env")
@ToString
@Setter
@Getter
public class CimFwerrConfig {

    /************************************************ Fields (1 - 200) ************************************************/
    private int errorInvalidIdentifier;

}
