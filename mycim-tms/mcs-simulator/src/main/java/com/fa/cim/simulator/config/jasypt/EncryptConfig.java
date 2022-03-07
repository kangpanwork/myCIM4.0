package com.fa.cim.simulator.config.jasypt;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import com.ulisesbocchio.jasyptspringboot.configuration.EnableEncryptablePropertiesBeanFactoryPostProcessor;
import com.ulisesbocchio.jasyptspringboot.configuration.EncryptablePropertyResolverConfiguration;
import org.jasypt.commons.CommonUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.salt.RandomSaltGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * description: enc ypt
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/5/26 0021        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/5/26 0021 13:07
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Configuration
@Import({EncryptablePropertyResolverConfiguration.class})
public class EncryptConfig {

    public EncryptConfig() {
    }

    /**
     * 初始化encrypt properties 读取
     *
     * @param environment env
     * @param converter   EncryptablePropertySourceConverter
     * @return post process
     */
    @Bean
    public static EnableEncryptablePropertiesBeanFactoryPostProcessor enableEncryptablePropertiesBeanFactoryPostProcessor
    (ConfigurableEnvironment environment, EncryptablePropertySourceConverter converter) {
        return new EnableEncryptablePropertiesBeanFactoryPostProcessor(environment, converter);
    }

    /**
     * description: init encrypt
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return string encrypt
     * @author YJ
     * @date 2021/6/1 0001 11:31
     */
    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("fa-software");
        // 加密算法
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        // 密钥迭代次数
        config.setKeyObtentionIterations("1000");
        // 设置加密器池的大小被创建。
        config.setPoolSize("1");
        // 加密方式
        config.setProviderName("SunJCE");
        // 盐值生成策略器
        config.setSaltGeneratorClassName(RandomSaltGenerator.class.getName());
        // 初始化向量生成器
        config.setIvGeneratorClassName(RandomIvGenerator.class.getName());
        config.setStringOutputType(CommonUtils.STRING_OUTPUT_TYPE_BASE64);
        encryptor.setConfig(config);
        return encryptor;
    }
}