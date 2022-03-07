package com.fa.cim.frameworks.dto.pp.mode;


import java.util.function.BiFunction;


/**
 * declare on the executor to determine which phase could be configured
 * WARNING: if executor configured at a wrong phase would result in the executing will not follow the configure sequence
 *
 * @author Yuri
 */
public enum AvailablePhase {

    /**
     * the executor could be configured at any phases of the execution
     */
    ALL((phase, chainedFlag) -> phase),

    /**
     * the executor could only be configured at post phase of the execution
     */
    POST((phase, chainedFlag) -> ExecutePhase.POST),

    /**
     * the executor could only be configured at chained or joined phase of the execution
     */
    CHAINED_OR_JOINED((phase, chainedFlag) -> chainedFlag ? ExecutePhase.CHAINED : ExecutePhase.JOINED);

    /**
     * override the decision by local function
     *
     * @param phase the post process task phase decision
     * @param chained if the task is still chained to main
     * @return the execute phase of the task
     */
    public ExecutePhase override(ExecutePhase phase, boolean chained) {
        return this.function.apply(phase, chained);
    }

    private final BiFunction<ExecutePhase, Boolean, ExecutePhase> function;

    AvailablePhase(BiFunction<ExecutePhase, Boolean, ExecutePhase> function) {
        this.function = function;
    }
}
