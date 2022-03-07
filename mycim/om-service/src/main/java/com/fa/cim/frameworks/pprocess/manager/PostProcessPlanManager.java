package com.fa.cim.frameworks.pprocess.manager;

import com.fa.cim.frameworks.dto.pp.PostProcessTaskPlan;

import java.util.List;

/**
 * this manager is responsible for getting the post process plan according to a transaction id and intepreted the plan into
 * a actual task list
 *
 * @author Yuri
 */
public interface PostProcessPlanManager {

    /**
     * plan the post process definition by according to the transaction id
     *
     * @param transactionId the transaction id
     * @return a post process plan
     */
    PostProcessTaskPlan findPostProcessPlan(String transactionId);

}
