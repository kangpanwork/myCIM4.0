package com.fa.cim.controller.post;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.post.IPostInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.post.IPostInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 9:49
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/post")
@Listenable
public class PostInqController implements IPostInqController {
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IPostInqService postInqService;
    @Autowired
    private IAccessInqService accessInqService;

    @ResponseBody
    @RequestMapping(value = "/cim_object_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.OBJECTID_LIST_INQ)
    public Response cimObjectListInq(@RequestBody Params.CimObjectListInqInParms params) {
        //init params
        final String transactionID = TransactionIDEnum.OBJECTID_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        //【step1】get schedule from calendar
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxWaferScrappedHistoryInq(...)
        Results.CimObjectListInqResult result = postInqService.sxCimObjectListInq(objCommon, params.getCimObjectListInqInParm());
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/post_action_list/inq", method = RequestMethod.POST)
    @Override
    public Response postActionListInq(@RequestBody Params.PostActionListInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.POST_PROCESS_ACTION_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        //【step1】get schedule from calendar
        //【step2】call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(),
                new Params.AccessControlCheckInqParams(true));

        //【step3】call sxWaferScrappedHistoryInq(...)
        Results.PostActionPageListInqResult result = null;
        try {
            result = postInqService.sxPostActionListInq(objCommon, params);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                throw e;
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/post_filter_list_for_ext/inq", method = RequestMethod.POST)
    @Override
    public Response postFilterListForExtInq(@RequestBody Params.PostFilterListForExtInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.EXTERNAL_POST_PROCESS_FILTER_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        //【step1】get schedule from calendar
        //【step2】call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        //【step3】call sxWaferScrappedHistoryInq(...)
        return Response.createSucc(transactionID, postInqService.sxPostFilterListForExtInq(objCommon, params.getPostFilterListForExtInqInParm()));
    }
}