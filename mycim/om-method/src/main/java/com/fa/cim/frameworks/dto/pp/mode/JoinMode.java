package com.fa.cim.frameworks.dto.pp.mode;


/**
 * determine the join mode of the executing
 *
 * @author Yuri
 */
public enum  JoinMode {

    /**
     * the executor will be executed in the post phase only in case of all successful execution of previous phases
     */
    NORMAL,

    /**
     * the executor will be executed regardless of the execution result of previous phases
     */
    FINALLY,

    /**
     * the executor will be executed in either chained or joined phase by either the task is chained to the main process
     */
    JOINED

}
