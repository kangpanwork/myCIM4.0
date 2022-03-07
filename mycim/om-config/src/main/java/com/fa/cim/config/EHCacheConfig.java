package com.fa.cim.config;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * description:
 * <p>EHCacheConfig .</p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/2/11        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2019/2/11 17:21
 * @copyright: 2019, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
@Configuration
@Slf4j
@EnableCaching
public class EHCacheConfig {
    @Bean
    public EhCacheCacheManager ehCacheCacheManager() {
        CacheManager myCache = CacheManager.getCacheManager("myCache");
        if (myCache == null){
            myCache = CacheManager.create();
        }
        return new EhCacheCacheManager(myCache);
    }
}
