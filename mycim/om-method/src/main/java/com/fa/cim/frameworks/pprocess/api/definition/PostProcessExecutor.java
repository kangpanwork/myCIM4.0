package com.fa.cim.frameworks.pprocess.api.definition;


import com.fa.cim.frameworks.dto.pp.PostProcessTask;

/**
 * the post process executor API
 *
 * @author Yuri
 */
public interface PostProcessExecutor {

    /**
     * the post process executor name, preferably same at the executor bean name
     *
     * @return name
     */
    default String getExecutorId() {
        return this.getClass().getSimpleName();
    }

    /**
     * define the compensation logic when the executing process failed
     *
     * @param param the necessary params for task handling
     */
    default void doCompensate(PostProcessTask.Param param) {
    }

    /**
     * main body of the post process task executing
     *
     * @param param the necessary params for task handling
     * @return the executing result
     */
    PostProcessTask.Result doExecute(PostProcessTask.Param param);

}
