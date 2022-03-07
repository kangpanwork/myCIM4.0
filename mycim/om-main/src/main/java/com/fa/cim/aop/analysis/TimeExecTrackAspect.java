package com.fa.cim.aop.analysis;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.newcore.framework.annotation.biz.Core;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

@Component
@Aspect
@Slf4j
public class TimeExecTrackAspect {

    @Around(value = "(execution(* com.fa.cim.controller..*.*(..)) " +
            "|| execution(* com.fa.cim.service..*.*(..)) " +
            "|| execution(* com.fa.cim.method.impl..*.*(..)) " +
            "|| execution(* com.fa.cim.newcore.impl.bo..*.*(..)) " +
            "|| execution(* com.fa.cim.newcore.impl.standard..*.*(..))) && !execution(* com.fa.cim.newcore.impl.bo.env..*.*(..))")
    public Object timeTracking(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        // 获取 OM_TIME_TRACK_FOR_METHOD_EXEC 环境变量
        String omTimeExecTrackEnv = StandardProperties.OM_TIME_TRACK_FOR_TX_EXEC.isReady() ? StandardProperties.OM_TIME_TRACK_FOR_TX_EXEC.getValue() : "";
        if (isNotEmpty(omTimeExecTrackEnv)) {

            // Get the target class level
            final Class<?> targetClass = joinPoint.getTarget().getClass();
            final boolean isController = targetClass.isAnnotationPresent(Controller.class) || targetClass.isAnnotationPresent(RestController.class);
            final boolean isService = targetClass.isAnnotationPresent(OmService.class);
            final boolean isMethod = targetClass.isAnnotationPresent(OmMethod.class);
            final boolean isCore = targetClass.isAnnotationPresent(Core.class);

            String trackLevel = "Non_Level";
            if (isController) {
                trackLevel = "controller";
            } else if (isService) {
                trackLevel = "service";
            } else if (isMethod) {
                trackLevel = "method";
            } else if (isCore) {
                trackLevel = "core";
            }

            // 获取 OM_TIME_TRACK_LEVEL 环境变量
            String omTimeTrackLevelEnv = StandardProperties.OM_TIME_TRACK_LEVEL.getValue();
            boolean isTrackingLevelCorrect = false;
            if (isNotEmpty(omTimeTrackLevelEnv) && omTimeTrackLevelEnv.contains(trackLevel)) {
                isTrackingLevelCorrect = true;
            }

            String transactionId = ThreadContextHolder.getTransactionId();
            boolean isContainsTx = omTimeExecTrackEnv.contains(transactionId);

            // 获取目标方法上的注解
            final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            if (isController) {
                final Method targetMethod = targetClass.getMethod(signature.getName(), signature.getParameterTypes());
                final CimMapping cimMapping = targetMethod.getAnnotation(CimMapping.class);
                if (null != cimMapping) {
                    final TransactionIDEnum[] transactionIDEnums = cimMapping.value();
                    for (TransactionIDEnum transactionIDEnum : transactionIDEnums) {
                        if (isNotEmpty(transactionIDEnum.getValue()) && omTimeExecTrackEnv.contains(transactionIDEnum.getValue())) {
                            isContainsTx = true;
                            transactionId = transactionIDEnum.getValue();
                            break;
                        }
                    }
                    if (!isContainsTx) {
                        final String[] txID_Str = cimMapping.names();
                        for (String txID : txID_Str) {
                            if (isNotEmpty(txID) && omTimeExecTrackEnv.contains(txID)) {
                                isContainsTx = true;
                                transactionId = txID;
                                break;
                            }
                        }
                    }
                }
            }

            // 记录日志
            if (isContainsTx && isTrackingLevelCorrect) {
                StopWatch stopWatch = new StopWatch();
                final String methodName = signature.getName();

                stopWatch.start(targetClass.getName() + "." + methodName);

                // 执行方法
                result = joinPoint.proceed(joinPoint.getArgs());

                stopWatch.stop();
                //log.info("<<<TimeExecTracking>>> time: {}", stopWatch.prettyPrint());
                log.info("\n<<<TimeExecTracking>>> txId: {}, requestTime: {}, threadId: {}, level: {}, methodName: {}.{}, time(ms): {}\n",
                        transactionId,
                        ThreadContextHolder.getRequestTime(),
                        Thread.currentThread().getId(),
                        trackLevel,
                        targetClass.getName(),
                        methodName,
                        stopWatch.getTotalTimeMillis());
            }
            //  do nothing
            else {
                result = joinPoint.proceed(joinPoint.getArgs());
            }
        }
        // do nothing
        else {
            result = joinPoint.proceed(joinPoint.getArgs());
        }
        return result;
    }

    private static boolean isEmpty(String str) {
        return str != null && !"".equals(str.trim()) && !"null".equals(str.trim());
    }

    private static boolean isNotEmpty(String str) {
        return isEmpty(str);
    }

}
