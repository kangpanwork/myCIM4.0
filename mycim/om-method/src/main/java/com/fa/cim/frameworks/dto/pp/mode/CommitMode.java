package com.fa.cim.frameworks.dto.pp.mode;


import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTransactionManager;

import java.util.function.BiFunction;

/**
 * determine when the transaction will be commited
 *
 * @author Yuri
 */
public enum CommitMode {

    /**
     * commit current trasaction, then begin a new transaction for task execution
     */
    Commit_Before((transactionManager, task) -> {
        transactionManager.doCommit();
        transactionManager.begin();
        return task.execute();
    }),

    /**
     * use current transaction for task execution, then commit the changes right after
     */
    Commit_After((transactionManager, task) -> {
        transactionManager.begin();
        PostProcessTask.Result result = task.execute();
        if (result.isSuccess()) {
            transactionManager.doCommit();
        }
        return result;
    }),

    /**
     * use current transaction for task execution, but do not do any commit
     */
    No_Commit((transactionManager, task) -> {
        transactionManager.begin();
        return task.execute();
    }),
    ;

    /**
     * proceed the task with transaction
     *
     * @param transactionManager the post process transaction manager
     * @param task the task to proceed
     * @return the proceed result
     */
    public PostProcessTask.Result apply(PostProcessTransactionManager transactionManager, PostProcessTask task) {
        return consumer.apply(transactionManager, task);
    }

    private final BiFunction<PostProcessTransactionManager, PostProcessTask, PostProcessTask.Result> consumer;

    CommitMode(BiFunction<PostProcessTransactionManager, PostProcessTask, PostProcessTask.Result> consumer) {
        this.consumer = consumer;
    }
}
