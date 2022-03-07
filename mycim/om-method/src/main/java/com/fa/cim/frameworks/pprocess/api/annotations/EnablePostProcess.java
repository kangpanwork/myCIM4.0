package com.fa.cim.frameworks.pprocess.api.annotations;



import com.fa.cim.frameworks.pprocess.api.definition.PostProcessPlanProxy;

import java.lang.annotation.*;

/**
 * declare on the method to enable the post process
 *
 * @author Yuri
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnablePostProcess {

    /**
     * the post process proxy responsible for registering, modifying and executing
     *
     * @return the bean name of the proxy
     */
    Class<? extends PostProcessPlanProxy> proxy() default PostProcessPlanProxy.class;

}
