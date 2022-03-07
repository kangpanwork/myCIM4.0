package com.fa.cim.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.fa.cim.intercept.DefaultInterceptor;
import com.fa.cim.intercept.PrivilegeInterceptor;
import com.fa.cim.intercept.ScheduleInterceptor;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * WebConfiguration .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/28        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/6/28 12:36
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return
     * @author PlayBoy
     * @date 2018/6/29
     */
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 允许cookies跨域
        config.addAllowedHeader("*");   // 允许访问的头信息,*表示全部
        config.addAllowedOrigin("*");   // 允许向该服务器提交请求的URI，*表示全部允许。。这里尽量限制来源域，比如http://xxxx:8080 ,以降低安全风险。。
        config.setMaxAge(18000L);       // 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
        config.addAllowedMethod("*");   // 允许提交请求的方法，*表示全部允许，也可以单独设置GET、PUT等
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * description:
     * Interceptors configure
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param registry registry
     * @author PlayBoy
     * @date 2018/6/29
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(scheduleInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(privilegeInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(defaultInterceptor()).addPathPatterns("/**");
    }

    /**
     * description: get defaultInterceptor
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author PlayBoy
     * @date 2018/8/6
     */
    @Bean
    public DefaultInterceptor defaultInterceptor() {
        return new DefaultInterceptor();
    }

    /**
     * description:get ScheduleInterceptor
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return ScheduleInterceptor
     * @author PlayBoy
     * @date 2018/8/6
     */
    @Bean
    public ScheduleInterceptor scheduleInterceptor() {
        return new ScheduleInterceptor();
    }

    /**
     * description: get PrivilegeInterceptor
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return PrivilegeInterceptor
     * @author PlayBoy
     * @date 2018/8/6
     */
    @Bean
    public PrivilegeInterceptor privilegeInterceptor() {
        return new PrivilegeInterceptor();
    }

    /**
     * description:Change springBoot default jackson to fastjson
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return org.springframework.boot.autoconfigure.web.HttpMessageConverters
     * @author Nyx
     * @date 2019/1/18 15:11
     */
    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.DisableCircularReferenceDetect);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        //set Json encode is utf-8
        List<MediaType> fastMedisTypes = new ArrayList<>();
        fastMedisTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastConverter.setSupportedMediaTypes(fastMedisTypes);
        return new HttpMessageConverters(fastConverter);
    }
}
