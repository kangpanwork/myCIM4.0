package com.fa.cim.controller.processmonitor;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.processMonitor.IProcessMonitorInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.processmonitor.IProcessMonitorInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
 * @since 2019/7/30 16:51
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/pmon")
@Listenable
public class ProcessMonitorInqController implements IProcessMonitorInqController {
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IProcessMonitorInqService processMonitorInqService;

    @ResponseBody
    @RequestMapping(value = "/monitor_batch_relation/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MONITOR_PROD_LOTS_RELATION_INQ)
    public Response monitorBatchRelationInq(@RequestBody Params.MonitorBatchRelationInqParams monitorBatchRelationInqParams) {
        final String transactionID = TransactionIDEnum.MONITOR_PROD_LOTS_RELATION_INQ.getValue();
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(monitorBatchRelationInqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, monitorBatchRelationInqParams.getUser(), accessControlCheckInqParams);

        /*-------------------------------*/
        /*   Main Process                */
        /*-------------------------------*/
        List<Infos.MonitorGroups> tmpResult = processMonitorInqService.sxMonitorBatchRelationInq(objCommon, monitorBatchRelationInqParams);
        return Response.createSucc(transactionID, tmpResult);
    }
}