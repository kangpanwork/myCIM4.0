package com.fa.cim.aop;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.function.Function;


/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/6/28 10:29
 */
@Component
@RefreshScope
@Aspect
public class LogAop {

    private static String[] packages;

    private static ThreadLocal<String> threadLocalBefore=new ThreadLocal<>();

    private static ThreadLocal<String> threadLocalThrowing=new ThreadLocal<>();

    private static ThreadLocal<String> threadLocalAfter=new ThreadLocal<>();


    @Before("com.fa.cim.aop.Pointers.aopLog()")
    public void before(JoinPoint joinPoint) {
        if (!check(joinPoint,threadLocalBefore)){
            return;
        }
        logging(joinPoint, joinPoint.getArgs(),"In");
        threadLocalBefore.set(joinPoint.getSignature().getName());
    }

    @AfterThrowing(value = "com.fa.cim.aop.Pointers.aopLog()",throwing = "ex")
    public void throwing(JoinPoint joinPoint,Throwable ex) {
        if (!check(joinPoint,threadLocalThrowing)){
            return;
        }
        logging(joinPoint, ex.getMessage(),"Error");
        threadLocalThrowing.set(joinPoint.getSignature().getName());
    }

    @AfterReturning(value = "com.fa.cim.aop.Pointers.aopLog()",returning = "returnValue")
    public void after(JoinPoint joinPoint,Object returnValue){
        if (!check(joinPoint,threadLocalAfter)){
            return;
        }
        logging(joinPoint,returnValue,"Out");
        threadLocalAfter.set(joinPoint.getSignature().getName());
    }

    @Value("${log.scans}")
    public void setPackages(String packages){
        if (StringUtils.isEmpty(packages)){
            LogAop.packages=null;
        }else {
            LogAop.packages=packages.split(",");
        }
    }

    private void logging(JoinPoint joinPoint, Object object,String point){
        try {
            logging(joinPoint,object,JSONObject::toJSONString,point);
        } catch (Exception ex){
            logging(joinPoint,object,JSONArray::toJSONString,point);
        }
    }

    private void logging(JoinPoint joinPoint, Object object, Function func,String point){
        Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getName());
        String msg= (String) func.apply(new Object[]{object,SerializerFeature.SkipTransientField,
        SerializerFeature.IgnoreErrorGetter});
        logger.info("Parameters[{}] = {}",point,msg);
    }

    public boolean check(JoinPoint joinPoint,ThreadLocal<String> threadLocal){
        if (packages==null||packages.length==0){
            return false;
        }
        if (joinPoint.getSignature().getName().equals(threadLocal.get())){
            threadLocal.remove();
            return false;
        }
        for (String aPackage : packages) {
            if (joinPoint.getTarget()!=null
                    &&joinPoint.getTarget().toString().startsWith(aPackage)){
                return true;
            }
        }
        return false;
    }

}
