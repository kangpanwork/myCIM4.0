package com.fa.cim.frameworks.pprocess.executor;


import com.fa.cim.frameworks.pprocess.api.definition.NonExecutor;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.frameworks.pprocess.executor.impl.*;
import lombok.Getter;

/**
 * The collections of all active {@link PostProcessExecutor} implements
 *
 * @author Yuri
 */
public enum TaskExecutors {

    /**
     * CollectedData / SPC Check / Spec Check executor
     */
    CollectedDataAction(EDCActionExecutor.class),

    /**
     * CollectedData action by process job
     */
    CollectedDataActionByPJ(EDCActionByPJExecutor.class),

    /**
     * APC I/F, then perform Product Disposition which is requested by APC
     * todo
     */
    APCDisposition(NonExecutor.class),

    /**
     * Q-Time executor
     */
    QTime(QTimeActionExecutor.class),

    /**
     * Process Lag Time
     */
    ProcessLagTime(LagTimeActionExecutor.class),

    /**
     * Future Hold (Post)
     */
    FutureHoldPost(FutureHoldPostExecutor.class),

    /**
     * If the lot is Monitoring Lot, and if it’s operation is monitor process,
     * system holds the lots which are grouped by monitor group in this post process.
     */
    MonitoredLotHold(MonitorLotHoldExecutor.class),

    /**
     * Schedule Change Reservation
     * todo
     */
    SCHExec(SchdlChangeExecutor.class),

    /**
     * Future Rework
     */
    FutureRework(FutureReworkExecutor.class),

    /**
     * Inter Fab Transfer to another FAB
     * todo
     */
    InterFabXfer(NonExecutor.class),

    /**
     * Pre1 Script executor
     */
    Script(PreOnePcsExecutor.class),

    /**
     * Future Hold (Pre)
     */
    FutureHoldPre(FutureHoldPreExecutor.class),

    /**
     * Apply Process Hold to lot
     */
    ProcessHold(ProcessHoldExecutor.class),

    /**
     * Auto Bank In
     */
    AutoBankIn(AutoBankInExecutor.class),

    /**
     * Planned Split/Merge
     */
    PlannedSplit(PSMActionExecutor.class),

    /**
     * DOC (Dynamic Process Operation Change)
     */
    DOCExec(DOCActionExecutor.class),

    /**
     * Put UTS Event
     * todo
     */
    UTSQueuePut(NonExecutor.class),

    /**
     * Put Auto-3 Dispatch Request Event for WD
     */
    MessageQueuePut(MessageQueueExecutor.class),

    /**
     * Put Auto-3 Dispatch Request Event for WD
     */
    MessageQueuePutForLotRecovery(MessageQueueExecutor.class),

    /**
     * Put Lot Event for External System . If lot event is put (this step is performed), external system MUST notify
     * the the end of external system process by calling OMS transaction “PostEndForExtRpt”
     * todo
     */
    ExternalPostProcessExecReq(NonExecutor.class),

    /**
     * Update run wafer count for processed equipment after operation start or operation start cancel
     */
    RunWaferInfoUpdate(RunWaferInfoUpdateExecutor.class),

    /**
     * Remove Equipment Monitor lot from Equipment Monitor job when the lot goes out from Equipment Monitor job section
     */
    AutoMonitorJobLotRemove(AutoMonitorJobLotRemoveExecutor.class),

    /**
     * If Spec/SPC violation is detected for the lot, perform defined actions at Equipment Monitor job “Fail”.
     */
    AutoMonitorEval(AutoMonitorEvalExecutor.class),

    /**
     * Wafer stacking
     */
    WaferStacking(WaferStackingExecutor.class),

    /**
     * Process operation deletion queue put action
     */
    PODelQueuePut(OperationDelQueuePutExecutor.class),

    /**
     * Partial move out lot hold
     */
    PartialCompLotHold(PartialCompLotHoldExecutor.class),

    /**
     * Auto dispatch control
     */
    AutoDispatchControl(AutoDispatchControlExecutor.class),

    /**
     * Parallel execution finalize
     */
    ParallelExecFinalize(ParallelExecFinalizeExecutor.class),

    /**
     * Auto monitor used count up
     */
    AutoMonitorUsedCountUp(AutoMonitorUsedCountUpExecutor.class),

    /**
     * Durable pcs
     * todo
     */
    DScript(NonExecutor.class),

    /**
     * Durable auto bank in
     * todo
     */
    DAutoBankIn(NonExecutor.class),

    /**
     * Durable process lag time
     */
    DProcessLagTime(DurableLagTimeExecutor.class),

    /**
     * Lot sampling
     */
    LotSampling(LotSamplingExecutor.class),

    /**
     * NPW lot skip
     */
    NPWLotSkip(NPWLotSkipExecutor.class),

    /**
     * Engineer Data Collection information set executor
     */
    EDCInformationSet(EDCInformationSetExecutor.class),

    /**
     * Move the lot to next step, ONLY applicable in JOINED or CHAINED phase
     */
    LotProcessMove(LotProcessMoveExecutor.class),


    /**
     * Move the lot to next step, ONLY applicable in JOINED or CHAINED phase, ONLY ForceMoveOut
     */
    LotForceProcessMove(LotForceProcessMoveExecutor.class),

    /**
     * Cancel the lot's pending to move next step status
     */
    LotProcessMoveCancel(LotProcessMoveCancelExecutor.class),

    /**
     * Check the lot's contamination level after move out, and hold the lot if it doesn't match
     */
    ContaminationOpeCheck(ContaminationOpeCheckExecutor.class),

    /**
     * Cancel the lot's pending to move next step status
     */
    OcapHoldAfterSPC(OcapHoldAfterSPCExecutor.class),


    /**
     * SortActionReq Post processing according to different actions
     */
    SortActionReq(SortActionReqExecutor.class),

    /**
     * SortActionRpt  Processing after completion of all processing tasks
     */
    SortActionRpt(SortActionRptExecutor.class),

    ;



    @Getter
    private final Class<? extends PostProcessExecutor> executorType;

    TaskExecutors(Class<? extends PostProcessExecutor> executorType) {
        this.executorType = executorType;
    }
}
