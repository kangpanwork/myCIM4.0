package com.fa.cim.controller.pr;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.pr.IPilotRunController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.pr.IPilotRunService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.hibernate.boot.model.source.spi.PluralAttributeElementSourceManyToAny;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/17          ********            jerry              create file
 *
 * @author: Jerry
 * @date: 2020/12/17 14:41
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IPilotRunController.class, confirmableKey = "PilotRunConfirm", cancellableKey = "PilotRunCancel")
@RequestMapping("/pr")
@Listenable
public class PilotRunController implements IPilotRunController {

    @Autowired
    private IAccessInqService accessInqService;


    @Autowired
    private IPilotRunService pilotRunService;


    @ResponseBody
    @RequestMapping(value = "/pr_status_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PILOT_RUN_STATUS_CHANGE)
    public Response pilotRunStatusChange(@Validated @RequestBody Params.PilotRunStatusChangeParams params) {
        final String txId = TransactionIDEnum.PILOT_RUN_STATUS_CHANGE.getValue();
        ThreadContextHolder.setTransactionId(txId);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        pilotRunService.sxPilotRunStatusChange(objCommon, params);
        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/create_pr_plan/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CREATE_PILOT_RUN_PLAN)
    public Response createPilotRunPlan(@Validated @RequestBody Params.CreatePilotRunParams params) {
        final String txId = TransactionIDEnum.CREATE_PILOT_RUN_PLAN.getValue();
        ThreadContextHolder.setTransactionId(txId);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getTagEquipment());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        pilotRunService.sxCreatePilotRunPlan(objCommon, params);
        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/create_pr_job/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CREATE_PILOT_RUN_JOB)
    public Response createPilotRunJob(@Validated @RequestBody Params.CreatePilotRunJobParams params) {
        final String txId = TransactionIDEnum.CREATE_PILOT_RUN_JOB.getValue();
        ThreadContextHolder.setTransactionId(txId);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        pilotRunService.sxCreatePilotRunJob(objCommon, params);
        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/delete_pr_plan/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PILOT_RUN_CANCEL)
    public Response pilotRunCancel(@Validated @RequestBody Params.PilotRunCancelParams params) {
        final String txId = TransactionIDEnum.PILOT_RUN_CANCEL.getValue();
        ThreadContextHolder.setTransactionId(txId);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        pilotRunService.sxPilotRunCancel(objCommon, params);
        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/create_recipe_info/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CREATE_RECIPE_INFO)
    public Response createRecipeinfo(Params.CreateRecipeInfo params) {

        return null;
    }
    /*---------------------------------------------------------------------------*/
    /* PILOT RUN  RECIPE GROUP                                                              */
    /*---------------------------------------------------------------------------*/
    @ResponseBody
    @RequestMapping(value = "/create_recipe_group/req",method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CREATE_PILOT_RUN_RECIPE_GROUP)
    public Response createPilotRunRecipeGroupReq(@Validated @RequestBody com.fa.cim.pr.Params.CreatePilotRunRecipeGroupParams params){
        final String txId = TransactionIDEnum.CREATE_PILOT_RUN_RECIPE_GROUP.getValue();
        ThreadContextHolder.setTransactionId(txId);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process
        /*   check User permission                                                               */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        pilotRunService.sxCreatePilotRunRecipeGroup(objCommon,params);
        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/delete_recipe_group/req",method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DELETE_PILOT_RUN_RECIPE_GROUP)
    public Response deletePilotRunRecipeGroupReq(@Validated @RequestBody com.fa.cim.pr.Params.DeletePilotRunRecipeGroupParams params){
        final String txId = TransactionIDEnum.DELETE_PILOT_RUN_RECIPE_GROUP.getValue();
        ThreadContextHolder.setTransactionId(txId);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process
        /*   check User permission                                                               */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        pilotRunService.sxDeletePilotRunRecipeGroup(objCommon,params);
        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/update_recipe_group/req",method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.UPDATE_PILOT_RUN_RECIPE_GROUP)
    public Response updatePilotRunRecipeGroupReq(@Validated @RequestBody com.fa.cim.pr.Params.UpdatePilotRunRecipeGroupParams params){
        final String txId = TransactionIDEnum.UPDATE_PILOT_RUN_RECIPE_GROUP.getValue();
        ThreadContextHolder.setTransactionId(txId);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process
        /*   check User permission                                                               */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        pilotRunService.sxUpdatePilotRunRecipeGroup(objCommon,params);
        return Response.createSucc(txId);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/create_recipe_job_info/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.CREATE_RECIPE_JOB)
    public Response createRecipeJob(@Validated @RequestBody com.fa.cim.pr.Params.CreatePilotJobInfoParams param) {
        final String txId = TransactionIDEnum.CREATE_RECIPE_JOB.getValue();
        ThreadContextHolder.setTransactionId(txId);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, param.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        pilotRunService.sxCreateRecipeJob(objCommon,param);
        return Response.createSucc(txId);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/delete_recipe_job_info/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.DELETE_RECIPE_JOB)
    public Response deleteRecipeJob(@Validated @RequestBody com.fa.cim.pr.Params.DeletePilotJobInfoParams param) {
        final String txId = TransactionIDEnum.DELETE_RECIPE_JOB.getValue();
        ThreadContextHolder.setTransactionId(txId);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, param.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        pilotRunService.sxDeleteRecipeJob(objCommon,param);
        return Response.createSucc(txId);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/recipe_job_bind_lot/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RECIPE_JOB_BIND_LOT)
    public Response recipeJobBindLot(@Validated @RequestBody com.fa.cim.pr.Params.RecipeJobBindLotParams param) {
        final String txId = TransactionIDEnum.RECIPE_JOB_BIND_LOT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, param.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        pilotRunService.sxRecipeJobBindLot(objCommon,param);
        return Response.createSucc(txId);
    }
}
