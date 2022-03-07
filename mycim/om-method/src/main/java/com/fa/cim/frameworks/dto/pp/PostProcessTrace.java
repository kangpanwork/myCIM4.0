package com.fa.cim.frameworks.dto.pp;

import lombok.Getter;

/**
 * for tracing the execution result of the post process execution chain
 *
 * @author Yuri
 */
@Getter
public class PostProcessTrace {

    private final PostProcessTask task;
    private final PostProcessTask.Result result;

    public PostProcessTrace(PostProcessTask task, PostProcessTask.Result result) {
        this.task = task;
        this.result = result;
    }
}
