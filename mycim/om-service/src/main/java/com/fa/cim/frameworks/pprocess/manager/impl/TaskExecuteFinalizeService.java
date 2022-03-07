package com.fa.cim.frameworks.pprocess.manager.impl;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessContext;
import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.mode.ExecutePhase;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTraceManager;
import com.fa.cim.method.IPostProcesssTaskMethod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.product.DispatchReadinessState;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import com.fa.cim.service.pprocess.PostProcessHoldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskExecuteFinalizeService {

    private final IPostProcesssTaskMethod taskMethod;
    private final PostProcessTraceManager traceManager;
    private final PostProcessHoldService holdService;
    private final GenericCoreFactory genericCoreFactory;

    @Autowired
    public TaskExecuteFinalizeService(IPostProcesssTaskMethod taskMethod,
                                      PostProcessTraceManager traceManager,
                                      PostProcessHoldService holdService,
                                      GenericCoreFactory genericCoreFactory) {
        this.taskMethod = taskMethod;
        this.traceManager = traceManager;
        this.holdService = holdService;
        this.genericCoreFactory = genericCoreFactory;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void doFinalize(PostProcessExecuteManagerImpl.TaskExecuteAction action) {
        PostProcessParam.Execute param = action.getParam();
        PostProcessContext context = traceManager.findPostProcessContextForTaskId(param.getTaskId());
        Infos.ObjCommon objCommon = param.getObjCommon();
        // step 2:  update executed tasks' status
        if (param.getPhase() != ExecutePhase.CHAINED) {
            action.getExecutedTasks().forEach(task -> taskMethod.updateTaskStatus(objCommon, task));
        }
        // step 3:  perform post action
        action.getFineLotIDs().stream()
                // perform fine lot action
                // change the fine lots' readiness status
                .map(lotID -> genericCoreFactory.getBO(CimLot.class, lotID))
                .filter(cimLot -> cimLot.getDispatchReadiness() != DispatchReadinessState.Error)
                .peek(cimLot ->{
                    cimLot.setDispatchReadiness(param.getNextStatus());
                    if (context.isLockHoldEnabled() && cimLot.isDispatchReady()) {
                        ObjectIdentifier lotID = ObjectIdentifier.build(cimLot.getIdentifier(), cimLot.getPrimaryKey());
                        Infos.LotHoldReq lotHoldReq = context.getLotHoldRecords().get(ObjectIdentifier.fetchValue(lotID));
                        holdService.sxReleaseLockHoldOnLot(objCommon, lotID, lotHoldReq);
                    }
                })
                .forEach(action.getFineLotAction());
        action.getErrorLotIDs().stream()
                // perform error lot action
                .peek(action.getErrorLotAction())
                // change error lots' readiness status to error
                .map(lotID -> genericCoreFactory.getBO(CimLot.class, lotID))
                .forEach(CimLot::makeDispatchError);
        action.getPostAction().accept(action);
    }
}
