package com.fa.cim.frameworks.pprocess.api.annotations;

import java.lang.annotation.*;

/**
 * preferably annotated on the service method, to indicate the method might might cause the operation change within the
 * method during the post process invocation. Each time such methods invocated would be counted and if the count exceed
 * the value set by {@link com.fa.cim.newcore.impl.bo.env.StandardProperties}.OM_PP_MAX_OPE_COUNT
 *
 * @author Yuri
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValitileOperation {
}
