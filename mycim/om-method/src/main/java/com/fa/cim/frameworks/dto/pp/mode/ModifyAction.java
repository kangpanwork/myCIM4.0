package com.fa.cim.frameworks.dto.pp.mode;

/**
 * modify action regarding the post process task
 *
 * @author Yuri
 */
public enum ModifyAction {

    /**
     * remove all tasks sharing the same task Id
     */
    RemoveTask_byTaskId,

    /**
     * remove all tasks sharing the same task Id and entity Id
     */
    RemoveTask_Top,

    /**
     * remove all tasks in the executing chain
     */
    RemoveTask_byChain,

    /**
     * remove all tasks with task status as "Completed" or "Skipped"
     */
    RemoveTask_Completed,

    /**
     * pass the task without executing it
     */
    ExecuteTask_passThrough,

    /**
     * execute the task but arbitrarily deem the execute result as success
     */
    ExecuteTask_force,

    /**
     * re-execute the task normally
     */
    ExecuteTask_retry,

    /**
     * modify the task by adding additional information
     */
    ModifyTask_addDetails,

    /**
     * modify the task by removing additional information
     */
    ModifyTask_removeDetails,

    /**
     * modify the task by updating the transaction memo
     */
    ModifyTask_updateTrxMemo,

}
