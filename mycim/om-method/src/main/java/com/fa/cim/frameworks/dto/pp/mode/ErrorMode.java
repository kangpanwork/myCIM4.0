package com.fa.cim.frameworks.dto.pp.mode;


import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTransactionManager;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * determine how the error would be handled when any error occured
 *
 * @author Yuri
 */
public enum ErrorMode {

    /**
     * rollback current transaction and skip the task to a task is not chained
     */
    Rollback_Skip(ErrorAction.ROLLBACK, ChainAction.SKIP),

    /**
     * rollback current transaction and continue to next task
     */
    Rollback_Continue(ErrorAction.ROLLBACK, ChainAction.CONTINUE),

    /**
     * rollback current transaction and stop further task execution
     */
    Rollback_Break(ErrorAction.ROLLBACK, ChainAction.BREAK),

    /**
     * apply compensate logic according to the executor and skip the task to a task is not chained
     */
    Compensate_Skip(ErrorAction.COMPENSATE, ChainAction.SKIP),

    /**
     * apply compensate logic according to the executor and continue to next task
     */
    Compensate_Continue(ErrorAction.COMPENSATE, ChainAction.CONTINUE),

    /**
     * apply compensate logic according to the executor and stop further task execution
     */
    Compensate_Break(ErrorAction.COMPENSATE, ChainAction.BREAK),

    /**
     * ignore any error and skip the task to a task is not chained
     */
    Ignore_Skip(ErrorAction.IGNORE, ChainAction.SKIP),

    /**
     * ignore any error and continue to next task
     */
    Ignore_Continue(ErrorAction.IGNORE, ChainAction.CONTINUE),

    /**
     * ignore any error and stop further task execution
     */
    Ignore_Break(ErrorAction.IGNORE, ChainAction.BREAK),
    ;

    private final ErrorAction errorAction;
    private final ChainAction chainAction;

    ErrorMode(ErrorAction errorAction, ChainAction chainAction) {
        this.errorAction = errorAction;
        this.chainAction = chainAction;
    }

    public void handleError(PostProcessTransactionManager txContext, PostProcessTask mainFunction) {
        errorAction.doErrorProcess(txContext, mainFunction);
    }

    public void filterList(List<PostProcessTask> taskQueue, PostProcessTask task) {
        chainAction.filterList(taskQueue, task);
    }

    /**
     * the action to handle the error
     */
    private enum ErrorAction {

        /**
         * rollback current transaction
         */
        ROLLBACK((transactionManager, task) -> {
            if (task.getPhase() == ExecutePhase.CHAINED) {
                return;
            }
            transactionManager.doRollback();
        }),

        /**
         * apply compensate logic base on the executor
         */
        COMPENSATE((transactionManager, task) -> {
            if (task.getPhase() == ExecutePhase.CHAINED) {
                return;
            }
            task.compensate();
        }),

        /**
         * do nothing when error occured
         */
        IGNORE((transactionManager, task) -> {}); // do nothing

        /**
         * do the error action
         *
         * @param transactionManager the post process transaction manager
         * @param task the error task
         */
        public void doErrorProcess (PostProcessTransactionManager transactionManager, PostProcessTask task) {
            consumer.accept(transactionManager, task);
        }

        private BiConsumer<PostProcessTransactionManager, PostProcessTask> consumer;

        ErrorAction(BiConsumer<PostProcessTransactionManager, PostProcessTask> consumer) {
            this.consumer = consumer;
        }
    }

    /**
     * the post error action on how the rest of the tasks in the chained would be executed or not
     */
    private enum ChainAction {

        /**
         * conintue to next task in the chain
         */
        CONTINUE((list, curTask) -> {}), // do nothing

        /**
         * skip the to the next chained tasks
         */
        SKIP((list, curTask) -> {
            boolean isEqp = curTask.getDefinition().getEntityType() == EntityType.Equipment;
            Iterator<PostProcessTask> ite = list.iterator();
            while (ite.hasNext()) {
                PostProcessTask next = ite.next();
                boolean isNotSameEntity = isEqp ?
                        !next.getEquipmentID().equals(curTask.getEquipmentID()) :
                        !next.getEntityID().equals(curTask.getEntityID());
                if (isNotSameEntity && !next.getDefinition().isChained()) {
                    break;
                } else {
                    ite.remove();
                }
            }
        }),

        /**
         * forcefully break all executing of the task
         */
        BREAK((list, curTask) -> list.clear()),
        ;

        /**
         * remove the tasks that would not be executed from the list
         *
         * @param taskQueue the tasks that await executing
         * @param task the error task
         */
        public void filterList(List<PostProcessTask> taskQueue, PostProcessTask task) {
            function.accept(taskQueue, task);
        }

        private BiConsumer<List<PostProcessTask>, PostProcessTask> function;

        ChainAction(BiConsumer<List<PostProcessTask>, PostProcessTask> function) {
            this.function = function;
        }
    }
}
