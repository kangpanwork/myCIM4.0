package com.fa.cim.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description:
 * use to check privilege.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/8/1        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/8/1 16:03
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Privilege {
    /**
     * description:
     * this is authorities
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    String[] value() default {};

    /**
     * description:
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    String[] authorities() default {};

    /**
     * description:
     * this is role
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    String[] roles() default {};
}
