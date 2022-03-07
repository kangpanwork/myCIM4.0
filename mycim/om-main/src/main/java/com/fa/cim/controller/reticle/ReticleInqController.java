package com.fa.cim.controller.reticle;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimPageUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.reticle.IReticleInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.rtms.ReticleUpdateParamsInfo;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.reticle.IReticleInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rtl")
@Listenable
public class ReticleInqController implements IReticleInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IReticleInqService reticleInqService;

    @ResponseBody
    @PostMapping(value = "/hold_reticle_list/inq")
    @CimMapping(TransactionIDEnum.HOLD_RETICLE_LIST)
    @Override
    public Response holdReticleListInq(@RequestBody Params.ReticleHoldListInqParams params) {
        log.info(String.valueOf(params));
        //【step0】init params
        final String transactionID = TransactionIDEnum.HOLD_RETICLE_LIST.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step0】check input params
        ObjectIdentifier reticleID = params.getReticleID();
        SearchCondition searchCondition = params.getSearchCondition();
        Validations.check(ObjectIdentifier.isEmpty(reticleID) || null == searchCondition, "the parameter is null");

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(reticleID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxHoldLotListInq(...)
        List<Infos.ReticleHoldListAttributes> reticleHoldListAttributes = null;
        try {
            reticleHoldListAttributes = reticleInqService.sxHoldReticleListInq(objCommon, reticleID);
        } catch (ServiceException ex) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEntry(), ex.getCode())) {
                Validations.check(ObjectIdentifier.isEmpty(reticleID), retCodeConfig.getNotFoundEntry());
            }
        }
        Results.ReticleHoldListInqResult reticleHoldListInqResult = new Results.ReticleHoldListInqResult();
        if (!CimObjectUtils.isEmpty(reticleHoldListAttributes)) {
            reticleHoldListInqResult.setReticleHoldListAttributes(CimPageUtils.convertListToPage(reticleHoldListAttributes, params.getSearchCondition().getPage(), params.getSearchCondition().getSize()));
        }
        Response succ = Response.createSucc(transactionID, reticleHoldListInqResult);
        log.info(String.valueOf(succ));
        return succ;
    }

    @ResponseBody
    @PostMapping(value = "/reticle_update_params/inq")
    @CimMapping(TransactionIDEnum.RETICLE_UPDATE_PARAMS_INQ)
    @Override
    public Response reticleUpdateParamsInq(@RequestBody Params.reticleUpdateParamsInqParams params) {
        //【step1】call txAccessControlCheckInq(...)
        final String transactionID = TransactionIDEnum.RETICLE_UPDATE_PARAMS_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        ReticleUpdateParamsInfo reticleUpdateParamsInfo = reticleInqService.sxReticleUpdateParamsInq(objCommon);

        return Response.createSucc(transactionID, reticleUpdateParamsInfo);
    }
}
