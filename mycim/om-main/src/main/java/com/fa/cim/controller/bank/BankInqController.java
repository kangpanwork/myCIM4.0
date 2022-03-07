package com.fa.cim.controller.bank;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.interfaces.bank.IBankInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.bank.IBankInqService;
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
 * @author Nyx
 * @since 2019/7/31 16:12
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/bank")
@Listenable
public class BankInqController implements IBankInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IBankInqService bankInqService;

    @ResponseBody
    @RequestMapping(value = "/bank_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.BANK_LIST_INQ)
    public Response bankListInq(@RequestBody Params.BankListInqParams bankListInqParams) {
        //init params
        log.debug("【step0】init params");
        final String transactionID = TransactionIDEnum.BANK_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        String inqBank = bankListInqParams.getInqBank();
        User user = bankListInqParams.getUser();

        //check input params
        log.debug("【step1】check input params");
        Validations.check(null == inqBank, "the inqBank is null...");
        Validations.check(!inqBank.equals("N") && !inqBank.equals("B") && !inqBank.equals("P"), "the inqBank parameter is invalid...");

        // check privilege
        log.debug("【step2】check privilege");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        // call txBankListInq(...)
        log.debug("【step3】call BankListInq service");
        Results.BankListInqResult bankListInqResult = bankInqService.sxBankListInq(objCommon, bankListInqParams);
        return Response.createSucc(transactionID, bankListInqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/material_prepare_cancel_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_PREPARATION_CANCEL_INFO_INQ)
    public Response materialPrepareCancelInfoInq(@RequestBody Params.MaterialPrepareCancelInfoInqParams materialPrepareCancelInfoInqParams) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.LOT_PREPARATION_CANCEL_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(materialPrepareCancelInfoInqParams.getPreparationCancelledLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, materialPrepareCancelInfoInqParams.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.MaterialPrepareCancelInfoInqResult tmpResult = bankInqService.sxMaterialPrepareCancelInfoInq(objCommon, materialPrepareCancelInfoInqParams);
        return Response.createSucc(transactionID, tmpResult);
    }
}