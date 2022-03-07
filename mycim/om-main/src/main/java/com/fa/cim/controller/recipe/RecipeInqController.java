package com.fa.cim.controller.recipe;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.recipe.IRecipeInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.recipe.IRecipeInqService;
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
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 16:19
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/rcp")
@Listenable
public class RecipeInqController implements IRecipeInqController {

    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IRecipeInqService recipeInqService;

    @ResponseBody
    @RequestMapping(value = "/uploaded_recipe_id_list_by_eqp/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MACHINE_RECIPE_LIST_INQ)
    public Response uploadedRecipeIdListByEqpInq(@RequestBody Params.UploadedRecipeIdListByEqpInqParams params) {
        final String transactionID = TransactionIDEnum.MACHINE_RECIPE_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        ObjectIdentifier equipmentID = params.getEquipmentID();

        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("call sxUploadedRecipeIdListByEqpInq");
        return Response.createSucc(transactionID, recipeInqService.sxUploadedRecipeIdListByEqpInq(objCommon, equipmentID));
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_directory/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RECIPE_DIRECTORY_INQ)
    public Response recipeDirectoryInq(@RequestBody Params.RecipeDirectoryInqParam params) {
        final String transactionID = TransactionIDEnum.RECIPE_DIRECTORY_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        ObjectIdentifier equipmentID = params.getEquipmentID();

        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("call sxRecipeDirectoryInq");
        return Response.createSucc(transactionID, recipeInqService.sxRecipeDirectoryInq(objCommon, equipmentID));
    }

    @ResponseBody
    @RequestMapping(value = "/all_recipe_id_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MACHINE_RECIPE_ID_LIST_INQ)
    public Response allRecipeIdListInq(@RequestBody Params.UserParams allRecipeIdListInqParams) {
        //Step-0:Initialize Parameters;
        final String transactionID = TransactionIDEnum.MACHINE_RECIPE_ID_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, allRecipeIdListInqParams.getUser(), accessControlCheckInqParams);

        //Step-4:txAllRecipeIdListInq;
        log.debug("【Step-4】call-txAllRecipeIdListInq(...)");
        return Response.createSucc(transactionID, recipeInqService.sxAllRecipeIdListInq(objCommon));
    }
}