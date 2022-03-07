package com.fa.cim.frameworks.pprocess.manager;


/**
 * the transaction manager when post process requires an independent transactional management
 *
 * @author Yuri
 */
public interface PostProcessTransactionManager {

    /**
     * commit current transaction
     */
    void doCommit();

    /**
     * rollback current transaction
     */
    void doRollback();

    /**
     * begin a new transaction
     */
    void begin();

    /**
     * clear current transaction
     */
    void close();

}
