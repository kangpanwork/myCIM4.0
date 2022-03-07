package com.fa.cim.controller.mfg;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.controller.interfaces.mfg.IMfgInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.mfg.MfgInfoExportParams;
import com.fa.cim.mfg.MfgInfoParams;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.constraint.IConstraintInqService;
import com.fa.cim.service.einfo.IElectronicInformationInqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
@RequestMapping("/mfg")
@Listenable
public class MfgInqController implements IMfgInqController {
    @Autowired
    private IConstraintInqService constraintInqService;
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IElectronicInformationInqService electronicInformationInqService;

    @ResponseBody
    @RequestMapping(value = "/mfg_restrict_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(names = "OCONQ001_EX") //OCONQ001接口联调时 constraint和mfg冲突
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
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), accessControlCheckInqParams.getUser(), accessControlCheckInqParams);

        //临时把两种constraint组合
        List<Infos.ConstraintEqpDetailInfo> entityInhibitDetailInfos = new ArrayList<>();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = mfgRestrictListInqParams.getEntityInhibitDetailAttributes();
        List<Infos.EntityIdentifier> entities = entityInhibitDetailAttributes.getEntities();
        boolean tool = false;
        ObjectIdentifier eqpID = new ObjectIdentifier();
        if (!CimArrayUtils.isEmpty(entities)){
            Infos.EntityIdentifier identifier = entities.get(0);
            String className = identifier.getClassName();
            if (CimStringUtils.equals(className, BizConstant.SP_INHIBITCLASSID_EQUIPMENT)){
                tool = true;
                eqpID = identifier.getObjectID();
            }
        }
        if (tool){
            entityInhibitDetailInfos = constraintInqService.sxConstraintListByEqpInq(objCommon, eqpID, mfgRestrictListInqParams.getFunctionRule());
        }else {
            entityInhibitDetailInfos = constraintInqService.sxMfgRestrictListInq_110(mfgRestrictListInqParams, objCommon);
        }

        //Step-2:sxMfgRestrictListInq;
        log.debug("【Step-4】call-sxMfgRestrictListInq(...)");

        return Response.createSucc(transactionID.getValue(), CimPageUtils.convertListToPage(entityInhibitDetailInfos, mfgRestrictListInqParams.getSearchCondition().getPage(), mfgRestrictListInqParams.getSearchCondition().getSize()));
    }

    @ResponseBody
    @RequestMapping(value = "/mfg_restrict_list_by_eqp/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_LIST_BY_EQP_INQ)
    public Response mfgRestrictListByEqpInq(@RequestBody Params.MfgRestrictListByEqpInqParams mfgRestrictListByEqpInqParams) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ENTITY_INHIBIT_LIST_BY_EQP_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-1:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessControlCheckInqParams.setUser(mfgRestrictListByEqpInqParams.getUser());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), accessControlCheckInqParams.getUser(), accessControlCheckInqParams);


        //Step-2:sxMfgRestrictListInq;
        log.debug("【Step-4】call-sxMfgRestrictListInq(...)");
        Results.MfgRestrictListByEqpInqResult result = constraintInqService.sxMfgRestrictListByEqpInq(mfgRestrictListByEqpInqParams, objCommon);

        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/sub_lot_type_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SUB_LOT_TYPE_ID_LIST_INQ)
    public Response subLotTypeIdListInq(@RequestBody Params.SubLotTypeListInqParams subLotTypeListInqParams) {
        final TransactionIDEnum transactionId = TransactionIDEnum.SUB_LOT_TYPE_ID_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionId.getValue());

        //step3 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId.getValue(), subLotTypeListInqParams.getUser(), accessControlCheckInqParams);

        //step 4: call txSubLotTypeListInq;
        Results.SubLotTypeListInqResult result = electronicInformationInqService.sxSubLotTypeListInq(objCommon, subLotTypeListInqParams);
        Response response = Response.createSucc(transactionId.getValue(), result);

        //step-5:judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_time_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_RECIPE_TIME_INQ)
    public Response recipeTimeLimitListInq(@RequestBody Params.RecipeTimeInqParams params) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ENTITY_INHIBIT_RECIPE_TIME_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-1:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setUser(params.getUser());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), accessControlCheckInqParams.getUser(), accessControlCheckInqParams);


        //Step-2:sxMfgRestrictListInq;
        log.debug("【Step-4】call-recipeTimeLimitListIn(...)");
        Results.RecipeTimeInqResult result = constraintInqService.recipeTimeLimitListInq(params, objCommon);

        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/mfg_info_export/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_EXPORT_INQ)
    public void mfgInfoExportInq(@RequestBody MfgInfoExportParams mfgInfoExportParams, HttpServletResponse httpServletResponse) throws IOException {
        String txId = TransactionIDEnum.ENTITY_INHIBIT_EXPORT_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        //Step1 - calendar_GetCurrentTimeDR
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, mfgInfoExportParams.getUser(), accessControlCheckInqParams);
        List<MfgInfoParams> mfgInfoParamsList = constraintInqService.sxConstraintInfoExportInq(objCommon, mfgInfoExportParams.getConstraintEqpDetailInfos());
        httpServletResponse.setCharacterEncoding("UTF-8");
        ExportParams entity = new ExportParams();
        entity.setType(ExcelType.XSSF);
        Workbook workbook = ExcelExportUtil.exportExcel(entity, MfgInfoParams.class, mfgInfoParamsList);
        workbook.write(httpServletResponse.getOutputStream());
    }
}