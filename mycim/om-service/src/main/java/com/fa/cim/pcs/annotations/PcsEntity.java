package com.fa.cim.pcs.annotations;

import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@Scope("prototype")
public @interface PcsEntity {

    @AliasFor(annotation = Component.class)
    String value() default "";

}
