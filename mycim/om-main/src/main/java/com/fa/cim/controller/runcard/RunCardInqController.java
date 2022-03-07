package com.fa.cim.controller.runcard;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.runCard.IRunCardInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.runcard.IRunCardInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/12                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/12 15:17
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/rc")
@Listenable
public class RunCardInqController implements IRunCardInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IRunCardInqService runCardInqService;

    @ResponseBody
    @RequestMapping(value = "/run_card_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RC_LIST_INQ)
    public Response runCardListInq(@RequestBody Params.RunCardListInqParams params) {
        String txId = TransactionIDEnum.RC_LIST_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step1 - checkPrivilegeAndGetObjCommon
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(params.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step2 - sxRunCardListInq
        Page<List<Infos.RunCardInfo>> result = runCardInqService.sxRunCardListInq(objCommon, params);

        return Response.createSucc(txId, result);
    }

}
