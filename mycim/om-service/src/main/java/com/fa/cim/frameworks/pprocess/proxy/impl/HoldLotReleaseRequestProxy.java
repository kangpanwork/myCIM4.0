package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.executor.TaskExecutors;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.ProxyUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HoldLotReleaseRequestProxy extends DefaultPostProcessProxy {

    private final GenericCoreFactory genericCoreFactory;

    @Autowired
    public HoldLotReleaseRequestProxy(PostProcessPlanManager planManager, GenericCoreFactory genericCoreFactory) {
        super(planManager);
        this.genericCoreFactory = genericCoreFactory;
    }

    @Override
    public List<PostProcessTask> modify(List<PostProcessTask> tasks) {
        return tasks.stream()
                // when the lot is not pending move next as the post process task triggered, do not perform pre future hold
                // for this lot
                .filter(task -> {
                    if (TaskExecutors.FutureHoldPre.getExecutorType()
                            .isAssignableFrom(task.getDefinition().getExecutor().getClass())) {
                        Boolean pendingMoveNext = genericCoreFactory.getBO(CimLot.class, task.getEntityID())
                                .isPendingMoveNext();
                        if (log.isTraceEnabled()) {
                            log.trace("Lot<{}> pendingMoveNextFlag == {}", task.getEntityID(), pendingMoveNext);
                        }
                        if (log.isDebugEnabled() && !pendingMoveNext) {
                            log.debug("Remove FutureHoldPreExecutor for Lot<{}>", task.getEntityID());
                        }
                        return pendingMoveNext;
                    }
                    return true;
                })
                .filter(task -> {
                    final Class<?> taskClass = ProxyUtils.getUserClass(task.getDefinition().getExecutor());
                    if (TaskExecutors.ProcessHold.getExecutorType().isAssignableFrom(taskClass)) {
                        boolean pendingMoveNext = genericCoreFactory.getBO(CimLot.class, task.getEntityID())
                                .isPendingMoveNext();
                        if (log.isDebugEnabled() && !pendingMoveNext) {
                            log.debug("Remove ProcessHoldExecutor for Lot<{}>", task.getEntityID());
                        }
                        return pendingMoveNext;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}
