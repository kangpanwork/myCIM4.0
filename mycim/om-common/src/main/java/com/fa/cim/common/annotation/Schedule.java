package com.fa.cim.common.annotation;

import com.fa.cim.common.constant.TransactionIDEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description:
 * use to check calendar.
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
public @interface Schedule {
    /**
     * description:
     * this is function id
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    TransactionIDEnum value();
}
