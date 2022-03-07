package com.fa.cim.frameworks.pprocess.api.definition;


import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;

import java.util.List;
import java.util.stream.Collectors;

/**
 * the post process proxy API
 *
 * @author Yuri
 */
public interface PostProcessPlanProxy<P, R> {

    /**
     * get the plan of post process tasks for later executing
     *
     * @param param register parameters
     * @return a list of tasks
     */
    List<PostProcessTask> plan(PostProcessParam.PlanTask<P, R> param);

    /**
     * modify the post process tasks for later executing
     *
     * @param tasks a list of tasks
     * @return a list of tasks
     */
    default List<PostProcessTask> modify(List<PostProcessTask> tasks) {
        return tasks;
    }

}
