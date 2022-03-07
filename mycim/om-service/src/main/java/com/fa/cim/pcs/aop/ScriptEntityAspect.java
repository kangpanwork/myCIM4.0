package com.fa.cim.pcs.aop;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.newcore.exceptions.CoreFrameworkException;
import com.fa.cim.pcs.annotations.PcsAPI;
import com.fa.cim.pcs.engine.ScriptThreadHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Slf4j
public class ScriptEntityAspect {

    private static final String COMMAND_NOT_EXC = "Phase is not support for current pcs api [ PHASE:%s ] [ API:%s ]";

    /**
     * mapping class which has annotation of {@link com.fa.cim.pcs.annotations.PcsEntity}
     */
    @Pointcut("@within(com.fa.cim.pcs.annotations.PcsEntity)")
    public void hasAnnotationOfPcsEntityForClass() {
    }

    /**
     * mapping method which has annotation of {@link com.fa.cim.pcs.annotations.PcsAPI}
     */
    @Pointcut("@annotation(com.fa.cim.pcs.annotations.PcsAPI)")
    public void hasAnnotationOfPcsAPIForMethod() {
    }

    /**
     * this point join is validating the methods that annotated with {@link PcsAPI}, which is to indicate if there is a
     * limitation for proceeding the method. the method would only be effective in the available phases the mismatched
     * execuction would be simply ignored and produce a warning message in logging.
     *
     * @param point the point join
     * @param pcsAPI {@link PcsAPI} to fetch the available phases
     * @return the result of the point join proceeding
     * @author Yuri
     */
    @Around("hasAnnotationOfPcsEntityForClass() && hasAnnotationOfPcsAPIForMethod() && @annotation(pcsAPI)")
    public Object validatePhasePoint (ProceedingJoinPoint point, PcsAPI pcsAPI) {
        String phase = ScriptThreadHolder.getPhase();
        if (CimStringUtils.equalsIn(phase, extractAvailablePhases(pcsAPI))) {
            try {
                return point.proceed();
            } catch (ServiceException | CoreFrameworkException e){
                log.error(e.getMessage(),e);
                throw e;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new ServiceException(throwable.getMessage());
            }
        }
        log.warn(String.format(COMMAND_NOT_EXC, phase, point.getSignature().getName()));
        return null;
    }

    /**
     * extract the available phases from the provided {@link PcsAPI} as an array of strings
     *
     * @param pcsAPI {@link PcsAPI} to fetch the available phases
     * @return an array of available phase of the command
     */
    private String[] extractAvailablePhases(PcsAPI pcsAPI) {
        PcsAPI.Scope[] value = pcsAPI.value();
        String[] values = new String[value.length];
        for (int i = 0; i < value.length; i++) {
            values[i] = value[i].getValue();
        }
        return values;
    }
}
