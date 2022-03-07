package com.fa.cim.controller.fam;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.fam.IFAMController;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.fam.IFAMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * description:
 * <p>
 * change history:
 * date defect# person comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/4/25 ******** zh create file
 *
 * @author: zh
 * @date: 2021/4/25 12:44
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@RequestMapping("/fam")
@Listenable
public class FAMController implements IFAMController {

    @Autowired
    private IFAMService famService;

    @ResponseBody
    @RequestMapping(value = "/sj_history_status_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SORT_JOB_HISTORY_STATUA_CHANGE_REQ)
    public Response sortJobHistoryStatusChangeReq(@RequestBody Params.SortJobHistoryParams sortJobHistoryParams) {
        //【step0】init params
//        final String transactionID = TransactionIDEnum.SORT_JOB_HISTORY_INQ.getValue();
//        ThreadContextHolder.setTransactionId(transactionID);
//
//        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
//        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, sortJobHistoryParams.getUser(), accessControlCheckInqParams);

        Boolean hasChangeCategory = famService.sxSortJobHistoryStatusChangeReq(sortJobHistoryParams);
        return Response.createSucc(hasChangeCategory);
    }
}