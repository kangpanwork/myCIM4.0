package com.fa.cim.frameworks.pprocess.api.annotations;

import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * declare as a post process task executor
 *
 * @author Yuri
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Component
public @interface PostProcessTaskHandler {

    /**
     * The value may indicate a suggestion for a logical component name,
     * to be turned into a Spring bean in case of an autodetected component.
     *
     * @return the suggested component name, if any (or empty String otherwise)
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * the value will determine which phase this executor could be configured to
     *
     * @return the available phase
     */
    AvailablePhase available() default AvailablePhase.ALL;

    /**
     * the executing of the task executor is requiring the lot to be moved to next step based on the
     * pendingMoveNext status.
     *
     * @return if it is true, requires to check the pending move next status
     */
    boolean isNextOperationRequired() default false;

}
