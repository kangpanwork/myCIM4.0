package com.fa.cim.controller.season;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.season.ISeasoningController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.season.ISeasoningService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * @exception
 * @author ho
 * @date 2020/5/27 10:40
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = ISeasoningController.class, confirmableKey = "SeasoningConfirm", cancellableKey = "SeasoningCancel")
@RequestMapping("/season")
//@Listenable
public class SeasoningController implements ISeasoningController {

    @Autowired
    private ISeasoningService seasoningService;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IAccessInqService accessInqService;

    @ResponseBody
    @RequestMapping(value = "/season_plan_create/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_PLAN_CREATE_REQ)
    public Response seasonPlanCreateReq(@RequestBody Params.SeasonPlanPrams seasonPlanPrams) {
        String transactionID = TransactionIDEnum.SEASON_PLAN_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(seasonPlanPrams.getSeason().getEqpID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, seasonPlanPrams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        seasoningService.sxSeasonPlanCreateReq(objCommon, seasonPlanPrams.getSeason(),seasonPlanPrams.getClaimMemo());
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/season_plan_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_PLAN_MODIFY_REQ)
    public Response seasonPlanModifyReq(@RequestBody Params.SeasonPlanPrams seasonPlanPrams) {
        String transactionID = TransactionIDEnum.SEASON_PLAN_MODIFY_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(seasonPlanPrams.getSeason().getEqpID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, seasonPlanPrams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        seasoningService.sxUpdateSeasonPlan(objCommon, CimArrayUtils.generateList(seasonPlanPrams.getSeason()));
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/season_plan_delete/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_PLAN_DELETE_REQ)
    public Response seasonPlanDeleteReq(@RequestBody Params.SeasonPlanPrams seasonPlanPrams) {
        String transactionID = TransactionIDEnum.SEASON_PLAN_MODIFY_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(seasonPlanPrams.getSeason().getEqpID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, seasonPlanPrams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        seasoningService.sxSeasonPlanDeleteReq(objCommon, seasonPlanPrams.getSeason(),seasonPlanPrams.getClaimMemo());
        return Response.createSucc(transactionID, null);
    }


    @ResponseBody
    @RequestMapping(value = "/season_plans_delete/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_PLANS_DELETE_REQ)
    public Response seasonPlansDeleteReq(@RequestBody Params.SeasonPlansPrams seasonPlansPrams) {
        String transactionID = TransactionIDEnum.SEASON_PLANS_DELETE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        ObjectIdentifier equipmentID=null;
        List<Infos.Season> seasons = seasonPlansPrams.getSeasons();
        Validations.check(CimArrayUtils.isEmpty(seasons),retCodeConfigEx.getSeasonNoExist());
        for (Infos.Season season : seasons) {
            Validations.check(equipmentID!=null&&
                    equipmentID!=season.getEqpID(),retCodeConfigEx.getEquipmentUnmatch(),season.getEqpID().getValue());
            equipmentID=season.getEqpID();
            Validations.check(equipmentID==null,retCodeConfigEx.getEquipmentRequired());
        }

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, seasonPlansPrams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        seasoningService.sxSeasonPlansDeleteReq(objCommon, seasonPlansPrams.getSeasons(),seasonPlansPrams.getClaimMemo());
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/season_recipe_group_create/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_RECIPE_GROUP_CREATE_REQ)
    public Response seasonRecipeGroupCreateReq(@RequestBody Params.RecipeGroupPrams recipeGroupPrams) {
        String transactionID = TransactionIDEnum.SEASON_RECIPE_GROUP_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> recipeIDList = recipeGroupPrams.getRecipeGroup().getRecipeInfos();
        accessControlCheckInqParams.setMachineRecipeIDList(recipeIDList);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, recipeGroupPrams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        seasoningService.sxSeasonRecipeGroupCreateReq(objCommon, recipeGroupPrams.getRecipeGroup(), recipeGroupPrams.getClaimMemo());
        return Response.createSucc(transactionID, recipeGroupPrams.getRecipeGroup());
    }

    @ResponseBody
    @RequestMapping(value = "/season_recipe_group_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_RECIPE_GROUP_MODIFY_REQ)
    public Response seasonRecipeGroupModifyReq(@RequestBody Params.RecipeGroupPrams recipeGroupPrams) {
        String transactionID = TransactionIDEnum.SEASON_RECIPE_GROUP_MODIFY_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> recipeIDList = recipeGroupPrams.getRecipeGroup().getRecipeInfos();
        accessControlCheckInqParams.setMachineRecipeIDList(recipeIDList);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, recipeGroupPrams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        seasoningService.sxSeasonRecipeGroupModifyReq(objCommon, recipeGroupPrams.getRecipeGroup(), recipeGroupPrams.getClaimMemo());
        return Response.createSucc(transactionID, recipeGroupPrams.getRecipeGroup());
    }

    @ResponseBody
    @RequestMapping(value = "/season_recipe_group_delete/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_RECIPE_GROUP_DELETE_REQ)
    public Response seasonRecipeGroupDeleteReq(@RequestBody Params.RecipeGroupPrams recipeGroupPrams) {
        String transactionID = TransactionIDEnum.SEASON_RECIPE_GROUP_DELETE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> recipeIDList = recipeGroupPrams.getRecipeGroup().getRecipeInfos();
        accessControlCheckInqParams.setMachineRecipeIDList(recipeIDList);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, recipeGroupPrams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        seasoningService.sxSeasonRecipeGroupDeleteReq(objCommon, recipeGroupPrams.getRecipeGroup(), recipeGroupPrams.getClaimMemo());
        return Response.createSucc(transactionID, recipeGroupPrams.getRecipeGroup());
    }

    @ResponseBody
    @RequestMapping(value = "/season_job_abort/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_JOB_ABORT_REQ)
    public Response seasonJobAbortReq(@RequestBody Params.SeasonJobPrams seasonJobPrams) {
        String transactionID = TransactionIDEnum.SEASON_JOB_ABORT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(seasonJobPrams.getSeasonJob().getEqpID());
        if (ObjectIdentifier.isNotEmptyWithValue(seasonJobPrams.getSeasonJob().getSeasonLotID())) {
            String[] split = ObjectIdentifier.fetchValue(seasonJobPrams.getSeasonJob().getSeasonLotID()).split(BizConstant.SEASON_SEPARATOR);
            List<String> lotIDStrings = CimArrayUtils.generateList(split);
            List<ObjectIdentifier> lotIDs = lotIDStrings.stream().map(ele -> ObjectIdentifier.buildWithValue(ele)).collect(Collectors.toList());
            accessControlCheckInqParams.setLotIDLists(lotIDs);
        }
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, seasonJobPrams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        seasoningService.sxUpdateSeasonJob(objCommon, seasonJobPrams.getSeasonJob(), BizConstant.SEASONJOB_STATUS_ABORTED,seasonJobPrams.getClaimMemo());
        return Response.createSucc(transactionID, seasonJobPrams.getSeasonJob());
    }

    @ResponseBody
    @RequestMapping(value = "/season_priority/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_PRIORITY_REQ)
    public Response seasonPriorityReq(@RequestBody Params.SeasonPriorityParams seasonPriorityParams) {
        String transactionID = TransactionIDEnum.SEASON_PRIORITY_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Validations.check(CimArrayUtils.isEmpty(seasonPriorityParams.getSeasonList()),retCodeConfig.getInvalidParameter());
        accessControlCheckInqParams.setEquipmentID(seasonPriorityParams.getSeasonList().get(0).getEqpID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, seasonPriorityParams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        seasoningService.sxUpdateSeasonPlan(objCommon, seasonPriorityParams.getSeasonList());
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/season_lot_move_in_reserve/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_LOT_MOVE_IN_RESERVE_REQ)
    public Response seasonLotMoveInReserveReq(@RequestBody Params.SeasonLotMoveInReserveReqParams seasonLotMoveInReserveReqParams) {
        String transactionID = TransactionIDEnum.SEASON_LOT_MOVE_IN_RESERVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(seasonLotMoveInReserveReqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                seasonLotMoveInReserveReqParams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        Results.SeasonLotMoveInReserveReqResult result = seasoningService.sxSeasonLotMoveInReserveReq(objCommon, seasonLotMoveInReserveReqParams);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/season_move_in_reserve/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASONMOVE_IN_RESERVE_REQ)
    public Response seasonMoveInReserveReq(@RequestBody Params.SeasonMoveInReserveReqParams seasonMoveInReserveReqParams) {
        String transactionID = TransactionIDEnum.SEASONMOVE_IN_RESERVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(seasonMoveInReserveReqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                seasonMoveInReserveReqParams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        Results.SeasonLotMoveInReserveReqResult result = seasoningService.sxSeasonMoveInReserveReq(objCommon, seasonMoveInReserveReqParams);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/move_in_reserve/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_MOVE_IN_RESERVE_REQ)
    public Response moveInReserveReq(@RequestBody Params.MoveInReserveReqForSeasonParams moveInReserveReqForSeasonParams) {
        String transactionID = TransactionIDEnum.SEASON_MOVE_IN_RESERVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Validations.check(moveInReserveReqForSeasonParams.getSeasonMoveinReserveReqParams()==null, retCodeConfig.getInvalidInputParam());
        accessControlCheckInqParams.setEquipmentID(moveInReserveReqForSeasonParams.getSeasonMoveinReserveReqParams().getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                moveInReserveReqForSeasonParams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        Results.SeasonLotMoveInReserveReqResult result = seasoningService.sxMoveInReserveReq(objCommon, moveInReserveReqForSeasonParams);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/move_in_reserve_for_iB/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_MOVE_IN_RESERVE_FOR_IB_REQ)
    public Response moveInReserveForIBReq(@RequestBody Params.MoveInReserveForIBReqForSeasonParams moveInReserveReqForSeasonParams) {
        String transactionID = TransactionIDEnum.SEASON_MOVE_IN_RESERVE_FOR_IB_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(moveInReserveReqForSeasonParams.getSeasonMoveInReserveForIBReqParams().getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                moveInReserveReqForSeasonParams.getUser(), accessControlCheckInqParams);

        //step3 - seasoningService
        Results.SeasonLotMoveInReserveReqResult result = seasoningService.sxMoveInReserveForIBReq(objCommon, moveInReserveReqForSeasonParams);
        return Response.createSucc(transactionID, result);
    }
}
