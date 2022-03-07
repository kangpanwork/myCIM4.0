package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ISorterNewMethod;
import com.fa.cim.service.equipment.IEquipmentProcessOperation;
import com.fa.cim.service.lot.ILotProcessOperationService;
import com.fa.cim.service.newSorter.ISortNewService;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.SorterType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>DurableLagTimeExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 15:39    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 15:39
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class SortActionRptExecutor implements PostProcessExecutor {

    private final IEquipmentProcessOperation equipmentProcessOperation;
    private final ILotProcessOperationService processOperationService;
    private final ISorterNewMethod sorterNewMethod;
    private final ISortNewService sortNewService;


    @Autowired
    public SortActionRptExecutor(IEquipmentProcessOperation equipmentProcessOperation,
                                 ILotProcessOperationService processOperationService,
                                 ISorterNewMethod sorterNewMethod, ISortNewService sortNewService) {
        this.equipmentProcessOperation = equipmentProcessOperation;
        this.processOperationService = processOperationService;
        this.sorterNewMethod = sorterNewMethod;
        this.sortNewService = sortNewService;
    }


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        if (log.isInfoEnabled()) {
            log.info("SortActionRptExecutor start");
        }
        Infos.ObjCommon objCommon = param.getObjCommon();
        objCommon.setTransactionID(TransactionIDEnum.SORT_ACTION_RPT.getValue());

        final ObjectIdentifier sortJobID = ObjectIdentifier.buildWithValue(param.getDetailValue("sorterJobId"));
        //-----------------------
        // 获取jobList,目前一个sort设备只有一个sortJob
        //-----------------------
        com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.
                Params.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRIn.setSorterJob(sortJobID);
        List<Info.SortJobListAttributes> jobListGetDROut = sorterNewMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
        int srtJobLen = CimArrayUtils.getSize(jobListGetDROut);
        if (srtJobLen > 0) {
            Info.SortJobListAttributes sortJobAttributes = jobListGetDROut.get(0);
            int compStatusLen = 0;
            int compoJobLen = CimArrayUtils.getSize(sortJobAttributes.getSorterComponentJobListAttributesList());
            String actionCode=null;
            for (Info.SorterComponentJobListAttributes componentJob : sortJobAttributes.getSorterComponentJobListAttributesList()) {
                if (CimStringUtils.equals(SorterType.Status.Completed.getValue(),componentJob.getComponentSorterJobStatus())) {
                    actionCode=componentJob.getActionCode();
                    compStatusLen++;
                } else {
                    break;
                }
            }
            //----------------------------------------------------------
            //  Delete sorter job
            //----------------------------------------------------------
            if (compStatusLen == compoJobLen) {
                if (log.isDebugEnabled()) {
                    log.debug("compStatusLen == compoJobLen");
                }

                Info.PostActDRIn postActDrIn =new Info.PostActDRIn();
                postActDrIn.setActionCode(actionCode);
                postActDrIn.setSortJobID(sortJobAttributes.getSorterJobID());
                postActDrIn.setOpeMemo("");
                if (log.isDebugEnabled()) {
                    log.debug("sxPostAct");
                }
                sortNewService.sxPostAct(objCommon,postActDrIn);
                com.fa.cim.sorter.Params.SJCancelReqParm sjCancelReqParm = new com.fa.cim.sorter.Params.SJCancelReqParm();
                sjCancelReqParm.setUser(objCommon.getUser());
                sjCancelReqParm.setNotifyEAPFlag(false);
                sjCancelReqParm.setJobID(sortJobAttributes.getSorterJobID());
                sjCancelReqParm.setJobType(SorterType.JobType.SorterJob.getValue());
                if (log.isDebugEnabled()) {
                    log.debug("sxSJCancelReq");
                }
                sortNewService.sxSJCancelReq(objCommon, sjCancelReqParm, true);
            }
        }
        return PostProcessTask.success();
    }

}
