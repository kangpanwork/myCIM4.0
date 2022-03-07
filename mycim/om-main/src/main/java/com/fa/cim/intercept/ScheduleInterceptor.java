package com.fa.cim.intercept;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.annotation.Schedule;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.RequestJsonUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.method.IUtilsComp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * description:
 * ScheduleInterceptor .
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
public class ScheduleInterceptor implements HandlerInterceptor {

    public static final String POST_USER_KEY = "user";
    public static final String GET_USER_KEY = "userID";

    @Autowired
    private IUtilsComp utilsCompService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("ScheduleInterceptor: enter preHandle()");
        // 将handler强转为HandlerMethod
        // Spring boot 2.0 对静态资源也进行了拦截
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 从方法处理器中获取出要调用的方法
            Method method = handlerMethod.getMethod();
            Schedule schedule = method.getAnnotation(Schedule.class);
            if (schedule == null) {
                schedule = method.getDeclaringClass().getAnnotation(Schedule.class);
            }
            if (schedule != null) {
                CimHttpServletRequestWrapper cimRequest = new CimHttpServletRequestWrapper(request);
                String requestType = cimRequest.getMethod();
                User user = new User();
                if (RequestJsonUtils.POST.equals(requestType)) {
                    String requestJson = RequestJsonUtils.getRequestJsonString(cimRequest);
                    JSONObject userJson = JSON.parseObject(requestJson).getJSONObject(POST_USER_KEY);
                    //put json data to bean
                    copyJson2Bean(userJson, user);
                }
                if (RequestJsonUtils.GET.equals(requestType)) {
                    String userID = cimRequest.getParameter(GET_USER_KEY);
                    user.setUserID(new ObjectIdentifier(userID));
                }
                if (ObjectIdentifier.isEmpty(user.getUserID())) {
                    return false;
                }
                try {
                    //TODO: 待优化成checkSchedule，把检查和获取ObjCommon分开
                    utilsCompService.setObjCommon(schedule.value(), user);
                } catch (Exception e) {
                    log.error("have no schedule for current process", e);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * description:
     * put jsonObject value to POJO bean
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param sourceJson sourceJson
     * @param target     target
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @author PlayBoy
     * @date 2018/8/2
     */
    private void copyJson2Bean(JSONObject sourceJson, Object target) throws InvocationTargetException, IllegalAccessException {
        for (Map.Entry<String, Object> entry : sourceJson.entrySet()) {
            String property = entry.getKey();
            Object value = entry.getValue();
            PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(target.getClass());
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                Method writeMethod = propertyDescriptor.getWriteMethod();
                if (writeMethod == null) {
                    continue;
                }
                String propertyName = propertyDescriptor.getName();
                if (propertyName.equals(property)) {
                    writeMethod.invoke(target, value);
                    break;
                }
            }
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.debug("ScheduleInterceptor: enter postHandle()");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.debug("ScheduleInterceptor: enter afterCompletion()");
    }
}
