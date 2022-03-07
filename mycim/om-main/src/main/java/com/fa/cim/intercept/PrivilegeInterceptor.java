package com.fa.cim.intercept;

import com.fa.cim.common.annotation.Privilege;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * description:
 * PrivilegeInterceptor .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/8/1        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/8/1 15:54
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
public class PrivilegeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("PrivilegeInterceptor: enter preHandle()");
        // 将handler强转为HandlerMethod
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 从方法处理器中获取出要调用的方法
            Method method = handlerMethod.getMethod();
            Privilege privilege = method.getAnnotation(Privilege.class);
            if (privilege == null) {
                privilege = method.getDeclaringClass().getAnnotation(Privilege.class);
            }
            boolean isRequiredCheck = privilege != null;
            if (isRequiredCheck) {
                //TODO: call txAccessControlCheckInq(...)
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.debug("PrivilegeInterceptor: enter postHandle()");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.debug("PrivilegeInterceptor: enter afterCompletion()");
    }
}
