package com.fa.cim.frameworks.pprocess.manager;

import com.fa.cim.frameworks.dto.pp.PostProcessParam;

public interface PostProcessExecuteManager {

    /**
     * execute the tasks in chained phase
     *
     * @param param tasks
     */
    void executeChained(PostProcessParam.Execute param);

    /**
     * execute the tasks in joined phase
     *
     * @param param tasks
     * @return if the execution is failed
     */
    boolean executeJoined(PostProcessParam.Execute param);

    /**
     * execute the tasks in post phase
     *
     * @param param tasks
     */
    void executePost(PostProcessParam.Execute param);

}
