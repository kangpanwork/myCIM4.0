package com.fa.cim.factory;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.utils.SpringContextUtil;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/18        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/9/18 10:38
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
public class ServiceFactory {
    private static final String SERVICE_PACKAGE_NAME = "com.fa.cim.service";
    private static Map<Class<?>, Object> servicePools = new HashMap<>();

    public ServiceFactory() {
        initServiceFactory();
    }

    @SuppressWarnings("unchecked")
    private void initServiceFactory() {
        Reflections reflections = new Reflections(SERVICE_PACKAGE_NAME);
        Set<Class<?>> serviceClasses = reflections.getTypesAnnotatedWith(OmService.class);
        for (Class<?> serviceClass: serviceClasses) {
           /* try {
                servicePools.put(serviceClass, serviceClass.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }*/
        }
    }

    /**     
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * @author Bear
      * @date 2019/9/19 13:41
      * @param tClass -
      * @return T
      */
    public <T>  T getServiceInstance(Class<T> tClass) {
        for (Map.Entry<Class<?>, Object> entry: servicePools.entrySet()) {
            if (tClass.isAssignableFrom(entry.getKey())) {
                return (T)entry.getValue();
            }
        }
        return null;
    }
}
