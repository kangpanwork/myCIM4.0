package com.fa.cim.factory;

import com.fa.cim.common.utils.SpringContextUtil;
import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.method.IAuthServerMethod;
import com.fa.cim.method.ILotMethod;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MethodFactory {
    private static final String METHOD_PACKAGE_NAME = "com.fa.cim.method";
    private static Map<Class<?>, Object> methodPools = new HashMap<>();

    public MethodFactory() {
        initMethodFactory();
    }

    @SuppressWarnings("unchecked")
    private void initMethodFactory() {
        Reflections reflections = new Reflections(METHOD_PACKAGE_NAME);
        Set<Class<?>> methodClasses = reflections.getTypesAnnotatedWith(OmMethod.class);
        for (Class<?> methodClass: methodClasses) {
          /*  try {
                methodPools.put(methodClass, methodClass.newInstance());
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
    public <T>  T getMethodInstance(Class<T> tClass) {
        for (Map.Entry<Class<?>, Object> entry: methodPools.entrySet()) {
            if (tClass.isAssignableFrom(entry.getKey())) {
                return (T)entry.getValue();
            }
        }
        return null;
    }
}
