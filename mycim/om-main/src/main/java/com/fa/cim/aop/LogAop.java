//package com.fa.cim.aop;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.fa.cim.common.utils.CimStringUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.AfterThrowing;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import java.lang.reflect.Method;
//import java.util.function.Function;
//
//
///**
// * description:
// * <p></p>
// * change history:
// * date             defect             person             comments
// * ---------------------------------------------------------------------------------------------------------------------
// *
// * @author Ho
// * @date 2019/6/28 10:29
// */
//@Component
//@RefreshScope
//@Aspect
//@Slf4j
//public class LogAop {
//
//    private static String[] packages;
//
//    private static ThreadLocal<String> threadLocalBefore=new ThreadLocal<>();
//
//    private static ThreadLocal<String> threadLocalThrowing=new ThreadLocal<>();
//
//    private static ThreadLocal<String> threadLocalAfter=new ThreadLocal<>();
//
//
//    @Before("com.fa.cim.aop.Pointers.aopLog()")
//    public void before(JoinPoint joinPoint) {
//        if (!check(joinPoint,threadLocalBefore)){
//            return;
//        }
//        try {
//            logging(joinPoint, joinPoint.getArgs(),"In");
//            threadLocalBefore.set(joinPoint.getSignature().getName());
//        } catch (Exception ex){}
//    }
//
//    @AfterThrowing(value = "com.fa.cim.aop.Pointers.aopLog()",throwing = "ex")
//    public void throwing(JoinPoint joinPoint,Throwable ex) {
//        if (!check(joinPoint,threadLocalThrowing)){
//            return;
//        }
//        try {
//            logging(joinPoint, ex.getMessage(),"Error");
//            threadLocalThrowing.set(joinPoint.getSignature().getName());
//        } catch (Exception e){}
//    }
//
//    @AfterReturning(value = "com.fa.cim.aop.Pointers.aopLog()",returning = "returnValue")
//    public void after(JoinPoint joinPoint,Object returnValue){
//        if (!check(joinPoint,threadLocalAfter)){
//            return;
//        }
//        try {
//            logging(joinPoint,returnValue,"Out");
//            threadLocalAfter.set(joinPoint.getSignature().getName());
//        } catch (Exception ex){}
//    }
//
//    @Value("${log.scans}")
//    public void setPackages(String packages){
//        if (CimStringUtils.isEmpty(packages)){
//            LogAop.packages=null;
//        }else {
//            LogAop.packages=packages.split(",");
//        }
//    }
//
//    private void logging(JoinPoint joinPoint, Object object,String point){
//        try {
//            logging(joinPoint,object,JSONObject::toJSONString,point);
//        } catch (Exception ex){
//            logging(joinPoint,object,JSONArray::toJSONString,point);
//        }
//    }
//
//    private void logging(JoinPoint joinPoint, Object object, Function func,String point){
//        Logger logger = LoggerFactory.getLogger(String.format("%s.%s", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName()));
//        //String msg= (String) func.apply(new Object[]{object,SerializerFeature.SkipTransientField, SerializerFeature.IgnoreErrorGetter});
//        String msg = (String) func.apply(new Object[]{object});
//
//        Class<?> targetCls = joinPoint.getTarget().getClass();
//        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
//        String requestValue = null;
//        try {
//            Method targetMethod = targetCls.getDeclaredMethod(ms.getName(), ms.getParameterTypes());
//            RequestMapping requestMapping = targetMethod.getAnnotation(RequestMapping.class);
//            if (requestMapping != null && requestMapping.value().length >=1) {
//                requestValue = requestMapping.value()[0];
//            }
//            // 获取类上面的注解
//            requestMapping = targetCls.getAnnotation(RequestMapping.class);
//            if (requestMapping != null && requestMapping.value().length >=1) {
//                requestValue = String.format("%s%s", requestMapping.value()[0], requestValue);
//            }
//        } catch (NoSuchMethodException e) {}
//        logger.info("|{}|{}|{}",requestValue, point, msg);
//    }
//
//    public boolean check(JoinPoint joinPoint,ThreadLocal<String> threadLocal){
//        if (packages==null||packages.length==0){
//            return false;
//        }
//        if (joinPoint.getSignature().getName().equals(threadLocal.get())){
//            threadLocal.remove();
//            return false;
//        }
//        for (String aPackage : packages) {
//            if (joinPoint.getTarget()!=null &&joinPoint.getTarget().toString().startsWith(aPackage)){
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public void unThreadLocalBefore(){
//        threadLocalBefore.remove();
//    }
//
//    public void unThreadLocalThrowing(){
//        threadLocalThrowing.remove();
//    }
//
//    public void unThreadLocalAfter(){
//        threadLocalAfter.remove();
//    }
//
//}
