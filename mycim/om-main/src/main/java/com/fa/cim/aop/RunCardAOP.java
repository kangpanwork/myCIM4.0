package com.fa.cim.aop;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/7/13        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/7/13 13:17
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Aspect
public class RunCardAOP {

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Pointcut("execution(public * com.fa.cim.controller.doc.DynamicOperationController.*(..)) " +
            "|| execution(public * com.fa.cim.controller.psm.PlannedSplitMergeController.*(..))" +
            "|| execution(public * com.fa.cim.controller.runcard.RunCardController.*(..))")
    public void point() {
    }

    @Before(value = "point()")
    public void doBefore(JoinPoint joinPoint) {

        String methodName = joinPoint.getSignature().getName(); //获取方法名
        boolean runCardUIFlag = methodName.substring(0, 7).equalsIgnoreCase("runCard"); //通过方法名前缀'runCard'判断是norm界面还是runCard界面
        boolean runCardFlag = StandardProperties.OM_DISABLE_RUNCARD.isTrue();

        try {
            if (runCardUIFlag) { //如果是runCard界面
                //如果是runCard界面但是runCard功能被禁用，则抛出runCard禁用异常。
                Validations.check(runCardFlag, retCodeConfigEx.getDisableRuncard(), "RunCard has been disabled");

            } else { //如果是norm页面
                //如果是norm界面但是runCard被启用，则抛出runCard启用异常，
                Validations.check(!runCardFlag, retCodeConfigEx.getDisableRuncard(), "RunCard has been enabled");

                String name = joinPoint.getSignature().getDeclaringTypeName();
                String[] split = name.split("\\.");
                String type = split[split.length - 2];

                // 验证norm页面的doc或者psm是否被禁用，如果被禁用，抛出异常
                Validations.check(type.equals("doc") && StandardProperties.OM_DISABLE_DOC.isTrue(), retCodeConfigEx.getDisableDoc());
                Validations.check(type.equals("psm") && StandardProperties.OM_DISABLE_PSM.isTrue(), retCodeConfigEx.getDisablePsm());
            }
        } catch (ServiceException ex) {
            // 获取切入的 Method 上的 txID
            MethodSignature joinPointObject = (MethodSignature) joinPoint.getSignature();
            CimMapping cimMapping = joinPointObject.getMethod().getAnnotation(CimMapping.class);
            ex.setTransactionID(cimMapping.value()[0].getValue());
            throw ex;
        }
    }
}