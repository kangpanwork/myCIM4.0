package com.fa.cim.aop;

import org.aspectj.lang.annotation.Pointcut;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/6/28 10:25
 */
public class Pointers {

    @Pointcut("execution(* com.fa.cim..*.*(..))")
    public void aopLog(){}

}
