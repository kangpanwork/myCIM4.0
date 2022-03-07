package com.fa.cim.controller.season;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.season.ISeasoningInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.newcore.dto.rcpgrp.RecipeGroup;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.season.ISeasoningInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author ho
 * @exception
 * @date 2020/5/27 13:29
 */
@Slf4j
@RestController
@RequestMapping("/season")
//@Listenable
public class SeasoningInqController implements ISeasoningInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private ISeasoningInqService seasoningInqService;

    @ResponseBody
    @RequestMapping(value = "/machine_season_plan/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MACHINE_SEASON_PLAN_INQ)
    public Response machineSeasonPlanInq(@RequestBody Params.IdentifierParams machineParams) {
        String txId = TransactionIDEnum.MACHINE_SEASON_PLAN_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(machineParams.getIdentifier());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, machineParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        List<Infos.Season> result = seasoningInqService.sxMachineSeasonPlanInq(objCommon, machineParams.getIdentifier());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/all_season_product/inq", method = RequestMethod.POST)
    @Override
    public Response allSeasonProductInq(@RequestBody Params.IdentifierParams machineParams) {
        String txId = TransactionIDEnum.MACHINE_SEASON_PLAN_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(machineParams.getIdentifier());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, machineParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        List<String> result = seasoningInqService.sxAllSeasonProductInq(objCommon, machineParams.getIdentifier());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/all_season_product_recipe/inq", method = RequestMethod.POST)
    @Override
    public Response allSeasonProductRecipeInq(@RequestBody Params.IdentifierParams machineParams) {
        String txId = TransactionIDEnum.MACHINE_SEASON_PLAN_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(machineParams.getIdentifier());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, machineParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        List<String> result = seasoningInqService.sxAllSeasonProductRecipeInq(objCommon, machineParams.getIdentifier());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/season_plan_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MACHINE_SEASON_PLAN_INQ)
    public Response seasonPlanInfoInq(@RequestBody Params.IdentifierParams seasonParams) {
        String txId = TransactionIDEnum.MACHINE_SEASON_PLAN_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, seasonParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        Infos.Season result = seasoningInqService.sxSeasonPlanInfoInq(objCommon, seasonParams.getIdentifier());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_group_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RECIPE_GROUP_INFO_INQ)
    public Response recipeGroupInfoInq(@RequestBody Params.IdentifierParams recipeGroupParams) {
        String txId = TransactionIDEnum.MACHINE_SEASON_PLAN_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, recipeGroupParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        RecipeGroup.Info result = seasoningInqService.sxRrecipeGroupInfoInq(objCommon, recipeGroupParams.getIdentifier());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/machine_season_job/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MACHINE_SEASON_JOB_INQ)
    public Response machineSeasonJobInq(@RequestBody Params.IdentifierParams machineParams) {
        String txId = TransactionIDEnum.MACHINE_SEASON_PLAN_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(machineParams.getIdentifier());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, machineParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        List<Infos.SeasonJob> result = seasoningInqService.sxMachineSeasonJobInq(objCommon, machineParams.getIdentifier());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/season_job_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_JOB_INFO_INQ)
    public Response seasonJobInfoInq(@RequestBody Params.IdentifierParams seasonJobParams) {
        String txId = TransactionIDEnum.SEASON_JOB_INFO_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, seasonJobParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        Infos.SeasonJob result = seasoningInqService.sxSeasonJobInfoInq(objCommon, seasonJobParams.getIdentifier());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/season_types/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_TYPES_INQ)
    public Response seasonTypesInq(@RequestBody Params.UserParams userParams) {
        String txId = TransactionIDEnum.SEASON_TYPES_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, userParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        return Response.createSucc(txId, new String[]{
                BizConstant.SEASON_TYPE_IDLE,
                BizConstant.SEASON_TYPE_RECIPEIDLE,
//                BizConstant.SEASON_TYPE_INTERVAL,
                BizConstant.SEASON_TYPE_PM,
                BizConstant.SEASON_TYPE_RECIPEGROUP
        });
    }

    @ResponseBody
    @RequestMapping(value = "/all_season_status/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ALL_SEASON_STATUS_INQ)
    public Response allSeasonStatusInq(@RequestBody Params.UserParams userParams) {
        String txId = TransactionIDEnum.ALL_SEASON_STATUS_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, userParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        return Response.createSucc(txId, new String[]{
                BizConstant.SEASON_STATUS_ACTIVE,
                BizConstant.SEASON_STATUS_INACTIVE
        });
    }

    @ResponseBody
    @RequestMapping(value = "/all_season_job_status/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ALL_SEASON_JOB_STATUS_INQ)
    public Response allSeasonJobStatusInq(@RequestBody Params.UserParams userParams) {
        String txId = TransactionIDEnum.ALL_SEASON_JOB_STATUS_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, userParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        return Response.createSucc(txId, new String[]{
                BizConstant.SEASONJOB_STATUS_REQUESTED,
                BizConstant.SEASONJOB_STATUS_RESERVED,
                BizConstant.SEASONJOB_STATUS_EXECUTING,
                BizConstant.SEASONJOB_STATUS_COMPLETED,
                BizConstant.SEASONJOB_STATUS_ABORTED
        });
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_group/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RECIPE_GROUP_INQ)
    public Response recipeGroupInq(@RequestBody Params.UserParams userParams) {
        String txId = TransactionIDEnum.RECIPE_GROUP_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, userParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        List<RecipeGroup.Info> result = seasoningInqService.sxRecipeGroupInq(objCommon);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/season_what_next/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_WHAT_NEXT_INQ)
    public Response seasonWhatNextInq(@RequestBody Params.SeasonWhatNextLotListParams seasonWhatNextLotListParams) {
        String txId = TransactionIDEnum.SEASON_WHAT_NEXT_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(seasonWhatNextLotListParams.getWhatNextLotListParams().getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, seasonWhatNextLotListParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        Results.SeasonWhatNextLotListResult result = seasoningInqService.sxSeasonWhatNextInq(objCommon,seasonWhatNextLotListParams);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/season_lots_move_in_reserve_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_LOTS_MOVE_IN_RESERVE_INFO_INQ)
    public Response seasonLotsMoveInReserveInfoInq(@RequestBody Params.SeasonLotsMoveInReserveInfoInqParams seasonLotsMoveInReserveInfoInqParams) {
        String txId = TransactionIDEnum.SEASON_LOTS_MOVE_IN_RESERVE_INFO_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(seasonLotsMoveInReserveInfoInqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, seasonLotsMoveInReserveInfoInqParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        Results.SeasonLotsMoveInReserveInfoInqResult result = seasoningInqService.sxSeasonLotsMoveInReserveInfoInq(objCommon,seasonLotsMoveInReserveInfoInqParams);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/season_lots_move_in_reserve_info_for_ib/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SEASON_LOTS_MOVE_IN_RESERVE_INFO_FOR_IB_INQ)
    public Response seasonLotsMoveInReserveInfoForIBInq(@RequestBody Params.SeasonLotsMoveInReserveInfoForIBInqParams seasonLotsMoveInReserveInfoForIBInqParams) {
        String txId = TransactionIDEnum.SEASON_LOTS_MOVE_IN_RESERVE_INFO_FOR_IB_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(seasonLotsMoveInReserveInfoForIBInqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, seasonLotsMoveInReserveInfoForIBInqParams.getUser(), accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        Results.SeasonLotsMoveInReserveInfoInqResult result = seasoningInqService.sxSeasonLotsMoveInReserveInfoForIBInq(objCommon,seasonLotsMoveInReserveInfoForIBInqParams);

        return Response.createSucc(txId, result);
    }
}