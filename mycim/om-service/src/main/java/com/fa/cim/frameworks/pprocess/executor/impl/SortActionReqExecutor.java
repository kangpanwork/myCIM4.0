package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ISorterNewMethod;
import com.fa.cim.service.equipment.IEquipmentInqService;
import com.fa.cim.service.equipment.IEquipmentService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.newSorter.ISortNewService;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.SorterType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
public class SortActionReqExecutor implements PostProcessExecutor {

    private final ILotService lotService;
    private final ISorterNewMethod sorterNewMethod;


    @Autowired
    public SortActionReqExecutor(ILotService lotService, ISorterNewMethod sorterNewMethod) {
        this.lotService = lotService;
        this.sorterNewMethod = sorterNewMethod;
    }


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        if (log.isInfoEnabled()) {
            log.info("SortActionReqExecutor start");
        }
        Infos.ObjCommon objCommon = param.getObjCommon();
        //在moveIn方法中为了有moveIn的操作日志将transactionID OSRTW015---> OEQPW005
        //为了以防后续的业务执行将OEQPW005替换成OSRTW015
        objCommon.setTransactionID(TransactionIDEnum.SORT_ACTION_REQ.getValue());
        final ObjectIdentifier sortJobID = ObjectIdentifier.buildWithValue(param.getDetailValue("sorterJobId"));

        //-----------------------
        // 获取jobList
        //-----------------------
        com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.
                Params.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRIn.setSorterJob(sortJobID);
        List<Info.SortJobListAttributes> jobListGetDROut = sorterNewMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
        int srtJobLen = CimArrayUtils.getSize(jobListGetDROut);
        Info.SorterComponentJobListAttributes componentJobListAttributes = null;
        Info.SortJobListAttributes sortJobAttributes ;
        if (srtJobLen > 0) {
            sortJobAttributes = jobListGetDROut.get(0);
            for (Info.SorterComponentJobListAttributes attributes : sortJobAttributes.getSorterComponentJobListAttributesList()) {
                if (attributes.getComponentSorterJobStatus().equals(SorterType.Status.Executing.getValue())) {
                    componentJobListAttributes = attributes;
                    break;
                }
            }
            if (null != componentJobListAttributes) {
                String actionCode = componentJobListAttributes.getActionCode();
                if (CimStringUtils.equals(SorterType.Action.Separate.getValue(), actionCode)) {
                    if (log.isDebugEnabled()) log.debug("actionCode=Separate--> 1:1");
                    List<Info.WaferSorterSlotMap> waferList = componentJobListAttributes.getWaferSorterSlotMapList();
                    if (CimArrayUtils.isNotEmpty(waferList)) {
                        com.fa.cim.dto.Params.SplitLotReqParams splitLotReqParams = new com.fa.cim.dto.Params.SplitLotReqParams();
                        List<ObjectIdentifier> waferIDs = new ArrayList<>();
                        splitLotReqParams.setParentLotID(waferList.get(0).getLotID());
                        for (Info.WaferSorterSlotMap slotMap : waferList) {
                            waferIDs.add(slotMap.getWaferID());
                        }
                        splitLotReqParams.setChildWaferIDs(waferIDs);
                        splitLotReqParams.setBranchingRouteSpecifyFlag(false);
                        splitLotReqParams.setFutureMergeFlag(false);
                        Results.SplitLotReqResult splitLotReqResult = lotService.sxSplitLotReq(objCommon, splitLotReqParams);
                        if (log.isDebugEnabled()) {
                            log.debug("update postAct and slotMap");
                        }
                        //进行分批过后，需要将子lot的ID存到slotMap表和postAct表
                        sorterNewMethod.updatePostActAndSoltMapLot(sortJobID, splitLotReqResult.getChildLotID());
                    }
                }
            }
        }
        return PostProcessTask.success();

    }

}
