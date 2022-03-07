package com.fa.cim.frameworks.pprocess.manager.impl;

import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTransactionManager;
import com.fa.cim.newcore.impl.factory.GenericCorePool;
import com.fa.cim.newcore.lock.executor.ObjectLockExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Optional;

@Component
public class PostProcessTransactionManagerImpl implements PostProcessTransactionManager {

    private final PlatformTransactionManager txManager;
    private final ObjectLockExecutor lockExecutor;

    private final static ThreadLocal<TransactionStatus> LOCAL_TRANSACTION_STATUS = new ThreadLocal<>();

    @Autowired
    public PostProcessTransactionManagerImpl(PlatformTransactionManager txManager, ObjectLockExecutor lockExecutor) {
        this.txManager = txManager;
        this.lockExecutor = lockExecutor;
    }

    @Override
    public void doCommit() {
        currentTransactionStatus().filter(status -> !status.isCompleted()).ifPresent(txManager::commit);
        lockExecutor.forceUnlockAll();
        GenericCorePool.clear();
    }

    @Override
    public void doRollback() {
        currentTransactionStatus().filter(status -> !status.isCompleted()).ifPresent(txManager::rollback);
        lockExecutor.forceUnlockAll();
        GenericCorePool.clear();
    }

    @Override
    public void begin() {
        currentTransactionStatus().filter(status -> !status.isCompleted()).orElseGet(() -> {
            TransactionStatus transaction = txManager.getTransaction(new DefaultTransactionDefinition());
            LOCAL_TRANSACTION_STATUS.set(transaction);
            return transaction;
        });
    }

    @Override
    public void close() {
        ThreadContextHolder.clearThreadSpecificDataString();
        LOCAL_TRANSACTION_STATUS.remove();
    }

    private static Optional<TransactionStatus> currentTransactionStatus() {
        return Optional.ofNullable(LOCAL_TRANSACTION_STATUS.get());
    }
}
