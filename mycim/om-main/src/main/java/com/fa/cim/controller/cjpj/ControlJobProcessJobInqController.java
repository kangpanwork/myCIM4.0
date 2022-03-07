package com.fa.cim.controller.cjpj;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.controlJobProcessJob.IControlJobProcessJobInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.cjpj.IControlJobProcessJobInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/7/31 10:38
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/cjpj")
@Listenable
public class ControlJobProcessJobInqController implements IControlJobProcessJobInqController {

    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IControlJobProcessJobInqService controlJobProcessJobInqService;

    @ResponseBody
    @RequestMapping(value = "/cjpj_online_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CIPJ_INFO_INQ)
    public Response CJPJOnlineInfoInq(@RequestBody Params.CJPJOnlineInfoInqInParams params) {
        String transcationID = TransactionIDEnum.CIPJ_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transcationID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transcationID, params.getUser(),
                accessControlCheckInqParams);

        /*-------------------------------*/
        /*   Main Process                */
        /*-------------------------------*/
        Results.CJPJOnlineInfoInqResult tmpResult =
                controlJobProcessJobInqService.sxCJPJOnlineInfoInq(objCommon, params);

        return Response.createSucc(transcationID, tmpResult);
    }

    @ResponseBody
    @RequestMapping(value = "/cjpj_progress_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CIPJ_PROGRESS_INQ)
    public Response CJPJProgressInfoInq(@RequestBody Params.CJPJProgressInfoInqParams params) {
        String transcationID = TransactionIDEnum.CIPJ_PROGRESS_INQ.getValue();
        ThreadContextHolder.setTransactionId(transcationID);

        //step1 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transcationID, params.getUser(), accessControlCheckInqParams);

        /*-------------------------------*/
        /*   Main Process                */
        /*-------------------------------*/
        Results.CJPJProgressInfoInqResult tmpResult = controlJobProcessJobInqService.sxCJPJProgressInfoInq(objCommon, params);

        return Response.createSucc(transcationID, tmpResult);
    }
}