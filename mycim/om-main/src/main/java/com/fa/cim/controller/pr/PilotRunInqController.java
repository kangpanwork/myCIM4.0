package com.fa.cim.controller.pr;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.pr.IPilotRunInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.pr.Results;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.pr.IPilotRunInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@RequestMapping("/pr")
@Listenable
public class PilotRunInqController implements IPilotRunInqController {

    @Autowired
    private IPilotRunInqService pilotRunInqService;

    @Autowired
    private IAccessInqService accessInqService;


    @Override
    @ResponseBody
    @RequestMapping(value = "/pr_job_info/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PILOT_RUN_JOB_INFO)
    public Response pilotRunJobInfo(@Validated @RequestBody Params.PilotRunJobInfoParams params) {
        final String transactionID = TransactionIDEnum.PILOT_RUN_JOB_INFO.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID, pilotRunInqService.sxPilotJobInfo(objCommon, params));
    }


    @Override
    @ResponseBody
    @RequestMapping(value = "/pr_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PILOT_RUN_LIST)
    public Response pilotRunList(@Validated @RequestBody Params.PilotRunInqParams params) {
        final String transactionID = TransactionIDEnum.PILOT_RUN_LIST.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        return Response.createSucc(transactionID, pilotRunInqService.sxPilotRunList(objCommon, params));
    }


    @Override
    @ResponseBody
    @RequestMapping(value = "/pr_job_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PILOT_JOB_LIST)
    public Response pilotJobListInq(@Validated @RequestBody com.fa.cim.pr.Params.PilotJobInfoParams params) {
        final String transactionID = TransactionIDEnum.PILOT_JOB_LIST.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<Results.JobInfo> results = pilotRunInqService.sxPilotJobListInq(objCommon, params);

        return Response.createSucc(transactionID, results);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/pr_recipe_group_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.GET_PILOT_RUN_RECIPE_GROUP)
    public Response pilotRecipeGroupListInq(@Validated @RequestBody com.fa.cim.pr.Params.PilotRunRecipeGroupParams params) {
        final String transactionID = TransactionIDEnum.GET_PILOT_RUN_RECIPE_GROUP.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process
        /*   check User permission                                                               */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        List<Results.PilotRunRecipeGroupResults> pilotRunRecipeGroupResults = pilotRunInqService.sxPilotRunRecipeGroupList(objCommon, params);
        return Response.createSucc(transactionID, pilotRunRecipeGroupResults);

    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/pr_recipe_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.GET_PILOT_RECIPE_LIST)
    public Response pilotRecipeListInq(@Validated @RequestBody com.fa.cim.pr.Params.EquipmentParams params) {
        final String transactionID = TransactionIDEnum.GET_PILOT_RECIPE_LIST.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process
        /*   check User permission                                                               */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        Results.RecipeResults recipeResults = pilotRunInqService.sxPilotRunRecipeList(objCommon, params);
        return Response.createSucc(transactionID, recipeResults);

    }

}
