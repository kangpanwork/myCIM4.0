package com.fa.cim.controller.qtime;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.dto.*;
import com.fa.cim.method.IQTimeMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.post.IPostService;
import com.fa.cim.service.qtime.IQtimeActionReqService;
import com.fa.cim.service.qtime.IQtimeListInqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * description: 提供给qTime-Sentinel调用
 * <p>
 * change history:
 * date             defect#             person             comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/9/3         ********             ZH                 create file
 *
 * @author: ZH
 * @date: 2021/9/3 3:03 下午
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved
 */
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Slf4j
@RequestMapping("/qtaction")
public class QtimeActionReqController {

    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IQtimeListInqService qtimeListInqService;
    @Autowired
    private IQTimeMethod qTimeMethod;
    @Autowired
    private IQtimeActionReqService qtimeActionReqService;
    @Autowired
    private IPostService postService;
    @Autowired
    private QTimeController postController;

    @RequestMapping(value = "/qtime_do_action/req", method = RequestMethod.POST)
    public Response qtimeActionReq(@RequestBody Params.QtimeActionReqParam qtimeActionReqParam) {
        String txId = TransactionIDEnum.QREST_TIME_ACTION_EXEC_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = qtimeActionReqParam.getUser();
        Asserts.check(null != user, "the check request is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.QREST_TIME_ACTION_EXEC_REQ, user);
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(qtimeActionReqParam.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Infos.QtimeListInqInfo qtimeListInqInfo = new Infos.QtimeListInqInfo();
        qtimeListInqInfo.setLotID(qtimeActionReqParam.getLotID());
        qtimeListInqInfo.setActiveQTime(true);
        List<Outputs.QrestLotInfo> tmpResult = qtimeListInqService.sxQtimeListInq(objCommon, qtimeListInqInfo);
        List<Infos.QtimeInfo> qtimeInfos = new ArrayList<>();
        Optional.ofNullable(tmpResult).orElse(new ArrayList<>()).forEach(qrestLotInfo -> qtimeInfos.addAll(qrestLotInfo.getStrQtimeInfo()));
        List<Outputs.QrestTimeAction> lotQtimeInfoSortByActionResult = qTimeMethod.lotQtimeInfoSortByAction(objCommon, qtimeActionReqParam.getLotID(), qtimeInfos);
        Integer actionLen = lotQtimeInfoSortByActionResult.size();
        List<String> postProcessRegistKey = new ArrayList<>();
        String qtimeActionReqObject = null;
        for (int i = 0; i < actionLen; i++) {
            Inputs.QtimeActionReqIn in = new Inputs.QtimeActionReqIn();
            in.setLotID(qtimeActionReqParam.getLotID());
            in.setLotStatus(tmpResult.get(0).getLotStatus());
            in.setRouteID(tmpResult.get(0).getRouteID());
            in.setOperationNumber(tmpResult.get(0).getOperationNumber());
            in.setStrQrestTimeAction(lotQtimeInfoSortByActionResult.get(i));
            RetCode<String> qtimeActionReq = qtimeActionReqService.sxQtimeActionReq(objCommon, in);
            qtimeActionReqObject = qtimeActionReq.getObject();

            if (CimStringUtils.equals(qtimeActionReqObject, "1")) {
                log.info("Post-Processing is required.");
                //-------------------------------------
                // Post-Processing Registration Section
                //-------------------------------------
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
                postTaskRegisterReqParams.setSequenceNumber(-1);
                postTaskRegisterReqParams.setPostProcessRegistrationParm(new Infos.PostProcessRegistrationParam(null, null, lotIDLists, null));
                Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                if (!CimObjectUtils.isEmpty(postTaskRegisterReqResult.getDKey())) {
                    //-------------------------------
                    // Sequence length Check
                    //-------------------------------
                    postProcessRegistKey.add(postTaskRegisterReqResult.getDKey());
                } else {
                    // Nothing to do.
                }
            } else {
                log.info("Post-Processing is not required.");
            }
        }
        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        for (String key : postProcessRegistKey) {
            postController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(user, key, 1, 0, null, ""));
        }

        Response response = Response.createSucc(txId, qtimeActionReqObject);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }


}