package com.fa.cim.controller.processmonitor;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.processMonitor.IProcessMonitorController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.automonitor.IAutoMonitorService;
import com.fa.cim.service.processmonitor.IProcessMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 * 2019/9/25        ######              Neko                Refactoring
 *
 * @author Nyx
 * @since 2019/7/30 16:45
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IProcessMonitorController.class, confirmableKey = "ProcessMonitorConfirm", cancellableKey = "ProcessMonitorCancel")
@RequestMapping("/pmon")
@Listenable
public class ProcessMonitorController implements IProcessMonitorController {
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private PostController postController;

    @Autowired
    private IProcessMonitorService processMonitorService;

    @Autowired
    private IAutoMonitorService autoMonitorService;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @ResponseBody
    @RequestMapping(value = "/monitor_batch_delete/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CANCEL_RELATION_MONITOR_PROD_LOTS_REQ)
    public Response monitorBatchDeleteReq(@RequestBody Params.MonitorBatchDeleteReqParams monitorBatchDeleteReqParams) {
        String txId = TransactionIDEnum.CANCEL_RELATION_MONITOR_PROD_LOTS_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        // step 2 check privilege
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(monitorBatchDeleteReqParams.getMonitorLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, monitorBatchDeleteReqParams.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.MonitorBatchDeleteReqResult tmpResult = processMonitorService.sxMonitorBatchDeleteReq(objCommon, monitorBatchDeleteReqParams.getMonitorLotID());

        return Response.createSucc(txId, tmpResult);
    }

    @ResponseBody
    @RequestMapping(value = "/monitor_batch_create/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MAKE_RELATION_MONITOR_PROD_LOTS_REQ)
    public Response monitorBatchCreateReq(@RequestBody Params.MonitorBatchCreateReqParams monitorBatchCreateReqParams) {
        String txId = TransactionIDEnum.MAKE_RELATION_MONITOR_PROD_LOTS_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  check privilege
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(monitorBatchCreateReqParams.getMonitorLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, monitorBatchCreateReqParams.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        processMonitorService.sxMonitorBatchCreateReq(objCommon, monitorBatchCreateReqParams);

        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/old/auto_create_monitor_for_in_process_lot/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.MONITOR_LOT_STB_AFTER_PROCESS_REQ)
    public Response autoCreateMonitorForInProcessLotReqOld(@RequestBody Params.AutoCreateMonitorForInProcessLotReqParams autoCreateMonitorForInProcessLotReqParams) {
        String txId = TransactionIDEnum.MONITOR_LOT_STB_AFTER_PROCESS_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(autoCreateMonitorForInProcessLotReqParams.getProductLotIDs());
        accessControlCheckInqParams.setEquipmentID(autoCreateMonitorForInProcessLotReqParams.getProcessEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, autoCreateMonitorForInProcessLotReqParams.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        // bug-1504 check the spurceLotID is not empty
        for (Infos.NewWaferAttributes newWaferAttributes : autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList()) {
            if (ObjectIdentifier.isEmpty(newWaferAttributes.getSourceLotID())) {
                return Response.createError(retCodeConfig.getInvalidParameter(), TransactionIDEnum.MONITOR_LOT_STB_AFTER_PROCESS_REQ.getValue());
            }
        }
        Results.AutoCreateMonitorForInProcessLotReqResult tmpResult = processMonitorService.sxAutoCreateMonitorForInProcessLotReq(objCommon, autoCreateMonitorForInProcessLotReqParams);

        Params.PostTaskRegisterReqParams postTaskRegisterReqParamsForChild = new Params.PostTaskRegisterReqParams(txId, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, Arrays.asList(tmpResult.getMonitorLotID()), null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParamsForChild);

         //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(txId, tmpResult);
    }

    @ResponseBody
    @RequestMapping(value = "/auto_create_monitor_for_in_process_lot/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.MONITOR_LOT_STB_AFTER_PROCESS_REQ)
    @EnablePostProcess
    public Response autoCreateMonitorForInProcessLotReq(@RequestBody Params.AutoCreateMonitorForInProcessLotReqParams params) {
        String txId = TransactionIDEnum.MONITOR_LOT_STB_AFTER_PROCESS_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(params.getProductLotIDs());
        accessControlCheckInqParams.setEquipmentID(params.getProcessEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId,
                params.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        // bug-1504 check the spurceLotID is not empty
        for (Infos.NewWaferAttributes newWaferAttributes :
                params.getStrNewLotAttributes().getNewWaferAttributesList()) {
            if (ObjectIdentifier.isEmpty(newWaferAttributes.getSourceLotID())) {
                return Response.createError(retCodeConfig.getInvalidParameter(),
                        TransactionIDEnum.MONITOR_LOT_STB_AFTER_PROCESS_REQ.getValue());
            }
        }
        Results.AutoCreateMonitorForInProcessLotReqResult result = processMonitorService.sxAutoCreateMonitorForInProcessLotReq
                (objCommon, params);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(txId, result);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

}
