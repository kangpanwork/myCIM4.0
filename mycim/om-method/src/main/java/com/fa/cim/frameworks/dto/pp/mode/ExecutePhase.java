package com.fa.cim.frameworks.dto.pp.mode;

import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * indicate the phase of the post process task's executing
 *
 * @author Yuri
 */
public enum ExecutePhase {

    /**
     * execute the task in chain phase
     */
    CHAINED,

    /**
     * execute the task in joined phase
     */
    JOINED,

    /**
     * execute the task in post phase
     */
    POST;

    /**
     * determine the execute phase according to the specifications of the task
     *
     * @param task the post process task
     * @param chainedToMain if the task is out of the chained phase already
     * @return the actual phase of the task
     */
    public static ExecutePhase getPhase(PostProcessTask task, AtomicBoolean chainedToMain) {
        chainedToMain.set(task.getDefinition().isChained() && chainedToMain.get());
        boolean chained = chainedToMain.get();
        PostProcessExecutor executor = task.getDefinition().getExecutor();
        JoinMode joinMode = task.getDefinition().getJoinMode();

        // the arbitrary execute phase
        ExecutePhase phase = joinMode != JoinMode.JOINED ? POST : (chained ? CHAINED : JOINED);

        // get the available phase attribute from the annotation, if not found use ALL instead
        return Optional.ofNullable(executor.getClass().getAnnotation(PostProcessTaskHandler.class))
                .map(PostProcessTaskHandler::available).orElse(AvailablePhase.ALL)
                // override the phase by the available phase attribute from the annotation
                .override(phase, chained);
    }

}
