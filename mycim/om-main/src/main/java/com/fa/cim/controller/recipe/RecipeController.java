package com.fa.cim.controller.recipe;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.recipe.IRecipeController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.IControlJobMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.recipe.IRecipeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/4          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/4 14:04
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IRecipeController.class, confirmableKey = "RecipeConfirm", cancellableKey = "RecipeCancel")
@RequestMapping("/rcp")
@Listenable
public class RecipeController implements IRecipeController {

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IRecipeService recipeService;

    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IControlJobMethod controlJobMethod;

    @ResponseBody
    @RequestMapping(value = "/recipe_download/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RECIPE_DOWNLOAD_REQ)
    public Response recipeDownloadReq(@RequestBody Params.RecipeDownloadReqParams params) {
        final String transactionID = TransactionIDEnum.RECIPE_DOWNLOAD_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setMachineRecipeIDList(Arrays.asList(params.getMachineRecipeID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("call sxRecipeDownloadReq");
        recipeService.sxRecipeDownloadReq(objCommon, params);
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_delete_in_file/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RECIPE_FILE_DELETION_REQ)
    public Response recipeDeleteInFileReq(@RequestBody Params.RecipeDeleteInFileReqParams params) {
        final String transactionID = TransactionIDEnum.RECIPE_FILE_DELETION_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setMachineRecipeIDList(Arrays.asList(params.getMachineRecipeID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("call sxRecipeDeleteInFileReq");
        recipeService.sxRecipeDeleteInFileReq(objCommon, params);
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_compare/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RECIPE_CONFIRMATION_REQ)
    public Response recipeCompareReq(@RequestBody Params.RecipeCompareReqParams params) {
        final String transactionID = TransactionIDEnum.RECIPE_CONFIRMATION_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setMachineRecipeIDList(Arrays.asList(params.getMachineRecipeID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("call sxRecipeCompareReq");
        try {
            recipeService.sxRecipeCompareReq(objCommon, params);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfigEx.getTcsMMTapPPConfirmError(), e.getCode())) {
                throw new ServiceException(retCodeConfigEx.getRecipeConfirmError(), "");
            }
            throw e;
        }
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_upload/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RECIPE_UPLOAD_REQ)
    public Response recipeUploadReq(@RequestBody Params.RecipeUploadReqParams params) {
        final String transactionID = TransactionIDEnum.RECIPE_UPLOAD_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        ObjectIdentifier equipmentID = params.getEquipmentID();

        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setMachineRecipeIDList(Collections.singletonList(params.getMachineRecipeID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("call sxRecipeUploadReq");
        recipeService.sxRecipeUploadReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_delete/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RECIPE_DELETION_REQ)
    public Response recipeDeleteReq(@RequestBody Params.RecipeDeleteReqParams params) {
        final String transactionID = TransactionIDEnum.RECIPE_DELETION_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        ObjectIdentifier equipmentID = params.getEquipmentID();

        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setMachineRecipeIDList(Collections.singletonList(params.getMachineRecipeID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("call sxRecipeDeleteReq");
        recipeService.sxRecipeDeleteReq(objCommon, params);
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_param_adjust_on_active_pj/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RECIPE_PARAMETER_ADJUST_IN_PROCESSING_REQ)
    public Response RecipeParamAdjustOnActivePJReq(@RequestBody Params.RecipeParamAdjustOnActivePJReqParam params) {
        log.info("RecipeParamAdjustOnActivePJReqParam : {}", params);
        final String transactionID = TransactionIDEnum.RECIPE_PARAMETER_ADJUST_IN_PROCESSING_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        Asserts.check(null != user, "the check request is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);
        //step2 -  call txAccessControlCheckInq(...)
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue) && CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1")) {
            //Step2 - controlJob_lotIDList_GetDR
            log.info("Call controlJob_lotIDList_GetDR");
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        recipeService.sxRecipeParamAdjustOnActivePJReq(objCommon, params);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_param_adjust/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RECIPE_PARAMETER_ADJUST_REQ)
    public Response RecipeParamAdjustReq(@RequestBody Params.RecipeParamAdjustReqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.RECIPE_PARAMETER_ADJUST_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call tx
        recipeService.sxRecipeParamAdjustReq(objCommon, params);

        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_param_adjust/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RECIPE_PARAMETER_ADJUST_RPT)
    public Response recipeParamAdjustRpt(@RequestBody Params.RecipeParamAdjustRptParams params) {
        final String transactionID = TransactionIDEnum.RECIPE_PARAMETER_ADJUST_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();

        Validations.check(null == user, "the user info is null...");

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("call txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1"))) {
            log.info("call controlJob_lotIDList_GetDR(...)");
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        log.info("call txRecipeParamAdjustRpt(...)");
        recipeService.sxRecipeParamAdjustRpt(objCommon, params);
        return Response.createSucc(transactionID);
    }
}
