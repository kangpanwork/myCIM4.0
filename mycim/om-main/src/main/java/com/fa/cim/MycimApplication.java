package com.fa.cim;

import com.fa.cim.support.OmRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/3/20        ********            Bear         create file
 *
 * @author: Bear
 * @date: 2018/3/20 10:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

//ByteTCC:使用文件存储log时, 不需要配置mongodb
@SpringBootApplication
//@EnableAspectJAutoProxy(exposeProxy = true)
@EnableAsync
@EnableJpaRepositories(repositoryFactoryBeanClass = OmRepositoryFactoryBean.class)
@EnableDiscoveryClient
@EnableFeignClients
@ServletComponentScan
@EnableScheduling
public class MycimApplication {

    public static void main(String[] args) {
        SpringApplication.run(MycimApplication.class, args);
    }
}
