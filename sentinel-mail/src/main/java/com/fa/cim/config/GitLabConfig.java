package com.fa.cim.config;

import lombok.Getter;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/5/19          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/5/19 0:04
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Configuration
public class GitLabConfig {

    @Value("${spring.profiles.active}")
    @Getter
    private String profiles;

    @Value("${spring.config.git.uri}")
    private String uri;

    @Value("${spring.config.git.username}")
    private String userName;

    @Value("${spring.config.git.password}")
    private String password;

    @Bean
    public GitLabApi getGitLabApi() throws GitLabApiException{
        return GitLabApi.oauth2Login(uri, userName, password);
    }
}