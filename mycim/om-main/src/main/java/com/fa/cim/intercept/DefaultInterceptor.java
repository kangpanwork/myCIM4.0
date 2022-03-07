package com.fa.cim.intercept;

import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.frameworks.common.TaskContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * description:
 * DefaultInterceptor
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/28        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/6/28 12:40
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
public class DefaultInterceptor implements HandlerInterceptor {

    /**
     * description:
     * Call before request processing (before Controller method call)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param request
     * @param response
     * @param handler
     * @return boolean
     * @author PlayBoy
     * @date 2018/6/29
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("DefaultInterceptor: enter preHandle()");
        //default return true to pass
        return true;
    }

    /**
     * description:
     * Called after request processing, but before view is rendered (after Controller method call)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @return void
     * @author PlayBoy
     * @date 2018/6/29
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.debug("DefaultInterceptor: enter postHandle()");
        //每次请求结束后，做一次clear操作
        ThreadContextHolder.clearHolder();
        TaskContextHolder.clear();
    }

    /**
     * description:
     * Called after the end of the entire request, that is,
     * after the DispatcherServlet renders the corresponding view (mainly for resource cleanup)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @author PlayBoy
     * @date 2018/6/29
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.debug("DefaultInterceptor: enter afterCompletion()");
    }
}
