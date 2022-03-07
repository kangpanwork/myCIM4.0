package com.fa.cim.controller.constraint;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimPageUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.constraint.IConstraintInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.constraint.IConstraintInqService;
import com.fa.cim.service.einfo.IElectronicInformationInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/7/31 10:02
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/constraint")
@Listenable
public class ConstraintInqController implements IConstraintInqController {
    @Autowired
    private IConstraintInqService constraintInqService;
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IElectronicInformationInqService electronicInformationInqService;

    @ResponseBody
    @RequestMapping(value = "/mfg_restrict_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_LIST_INQ)
    public Response mfgRestrictListInq(@RequestBody Params.MfgRestrictListInqParams mfgRestrictListInqParams) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ENTITY_INHIBIT_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-1:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessControlCheckInqParams.setUser(mfgRestrictListInqParams.getUser());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(),
                accessControlCheckInqParams.getUser(), accessControlCheckInqParams);


        //Step-2:sxMfgRestrictListInq;
        log.debug("【Step-4】call-sxMfgRestrictListInq(...)");
        Results.NewMfgRestrictListInqResult result = constraintInqService
                .sxMfgRestrictListInq(mfgRestrictListInqParams, objCommon);

        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/mfg_restrict_exclusion_lot_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_EXCEPTION_LOT_LIST_INQ)
    public Response mfgRestrictExclusionLotListInq(@RequestBody Params.MfgRestrictExclusionLotListInqParams params) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ENTITY_INHIBIT_EXCEPTION_LOT_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-1:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(),
                params.getUser(), accessControlCheckInqParams);


        //Step-2:sxMfgRestrictExclusionLotListInq;
        log.debug("【Step-4】call-sxMfgRestrictExclusionLotListInq(...)");
        List<Infos.EntityInhibitDetailInfo> result = constraintInqService
                .sxMfgRestrictExclusionLotListInq(params, objCommon);

        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/route_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MODULE_PROCESS_DEFINITION_ID_LIST_INQ)
    public Response moduleAllProcessStepListInq(@RequestBody Params.RouteListInqParams params) {
        //Step-0:Initialize Parameters;
        final String transactionID = TransactionIDEnum.MODULE_PROCESS_DEFINITION_ID_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        //Step-1:Incoming Log Put;
        log.info(" Incoming = {}", transactionID);
        //Step-2:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                user, accessControlCheckInqParams);
        //Step-3:txRouteListInq;
        log.debug("【Step-4】call-txRouteListInq(...)");
        List<Infos.ProcessDefinitionIndexList> result = constraintInqService.sxRouteListInq(params, objCommon);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/stage_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.STAGE_ID_LIST_INQ)
    public Response stageListInq(@RequestBody Params.StageListInqParams params) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.STAGE_ID_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-1:Incoming Log Put;
        log.info(" Incoming = {}", transactionID.getValue());

        //Step-2:txAccessControlCheckInq;
        log.debug("【Step-2】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params
                .AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(),
                params.getUser(), accessControlCheckInqParams);

        //Step-3:txStageListInq;
        log.debug("【Step-3】call-txStageListInq(...)");
        Results.StageListInqResult result = constraintInqService.sxStageListInq(objCommon, params);

        // Step-4:Post Process(Generate Output Results/event log put/Set Transaction ID);
        Response response = Response.createSucc(transactionID.getValue(), result);

        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/constraint_list_by_eqp/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CONSTRAINT_LIST_BY_EQP_INQ)
    public Response constraintListByEqpInq(@RequestBody Params.ConstraintListByEqpInqParams params) {
        String transactionID = TransactionIDEnum.CONSTRAINT_LIST_BY_EQP_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step3 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params
                .AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        List<Infos.ConstraintEqpDetailInfo> entityInhibitDetailInfos = constraintInqService
                .sxConstraintListByEqpInq(objCommon, params.getEquipmentID(), params.getFunctionRule());
        return Response.createSucc(transactionID, CimPageUtils.convertListToPage(entityInhibitDetailInfos,
                params.getSearchCondition().getPage(), params.getSearchCondition().getSize()));
    }
    
    /**       
    * description:  inquire constraint history for tool constraint which constraints with BLIST and WLIST
    * change history:  
    * date             defect             person             comments  
    * ---------------------------------------------------------------------------------------------------------------------  
    * 2021/6/11 14:08                       AOKI              Create
    * @author AOKI  
    * @date 2021/6/11 14:08  
    * @param params  
    * @return com.fa.cim.common.support.Response  
    */
    @ResponseBody
    @RequestMapping(value = "/constraint_history_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CONSTRAINT_HISOTORY_LIST_INQ)
    public Response constraintHistoryListInq(@RequestBody Params.ConstraintHistoryListInqParams params) {
        String transactionID = TransactionIDEnum.CONSTRAINT_HISOTORY_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step3 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params
                .AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService
                .checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        Page<Infos.ConstraintHistoryDetailInfo> constraintHistoryDetailInfos = constraintInqService
                .sxConstraintHistoryListInq(objCommon, params.getFunctionRule(),
                        params.getSpecificTool(),params.getSearchCondition());

        return Response.createSucc(transactionID, constraintHistoryDetailInfos);
    }
}