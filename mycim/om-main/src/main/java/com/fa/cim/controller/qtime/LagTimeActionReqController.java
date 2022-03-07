package com.fa.cim.controller.qtime;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.interfaces.durable.IDurableController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.qtime.IDrbLagTimeActionReqService;
import com.fa.cim.service.qtime.ILagTimeActionReqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
@RequestMapping("/lag")
@Listenable
public class LagTimeActionReqController {
    @Autowired
    private IUtilsComp utilsComp;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private ILagTimeActionReqService lagTimeActionReqService;

    @Autowired
    private QTimeController postController;

    @Autowired
    private IDrbLagTimeActionReqService drbLagTimeActionReqService;

    @ResponseBody
    @RequestMapping(value="/lag_time_action/req",method = RequestMethod.POST)
    @EnablePostProcess
    public Response lagTimeActionReq(@RequestBody Params.LagTimeActionReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.PROCESS_LAG_TIME_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();

        //check input params
        Validations.check(null == user, "the user info is null...");

        //【step1】get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.PROCESS_LAG_TIME_UPDATE_REQ, user);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);
        //【step3】main process
        lagTimeActionReqService.sxProcessLagTimeUpdate(objCommon, params);
        return Response.createSucc(transactionID);

    }

    @ResponseBody
    @RequestMapping(value="/alpha/lag_time_action/req",method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PROCESS_LAG_TIME_UPDATE_REQ)
    @EnablePostProcess
    public Response lagTimeActionReqAlpha(@RequestBody Params.LagTimeActionReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.PROCESS_LAG_TIME_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();

        //check input params
        Validations.check(null == user, "the user info is null...");

        //【step1】get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.PROCESS_LAG_TIME_UPDATE_REQ, user);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //【step3】main process
        lagTimeActionReqService.sxProcessLagTimeUpdate(objCommon, params);

        return Response.createSucc(transactionID);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @PostMapping(value = "/drb_lag_time_time_action/req")
    public Response drbLagTimeActionReq(@RequestBody Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        //Initialising strObjCommonIn's first two parameters
        Infos.ObjCommon objCommonIn;
        String transactionID = TransactionIDEnum.DURABLE_PROCESS_LAG_TIME_UPDATE.getValue();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        objCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, durableProcessLagTimeUpdateReqInParm.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurableProcessLagTimeUpdateReqResult durableProcessLagTimeUpdateReqResul= drbLagTimeActionReqService.sxDrbLagTimeActionReq(objCommonIn, durableProcessLagTimeUpdateReqInParm.getUser(),durableProcessLagTimeUpdateReqInParm);


        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        //todo  txDurablePostProcessActionRegistReq
        //-----------------------------------
        //   Post Process Execution
        //-----------------------------------
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
        //todo  postTaskRegisterReqParams.setPostProcessRegistrationParm();
        postTaskRegisterReqParams.setClaimMemo("");
        // postTaskRegisterReqParams.setKey();
        postTaskRegisterReqParams.setUser(objCommonIn.getUser());
        // postTaskRegisterReqParams.setPatternID();
        //postTaskRegisterReqParams.setTransactionID();
        Infos.PostProcessRegistrationParam strPostProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
        log.info("eqpReserveForFlowBatchReq(): Registration of Post-Processng");
        postTaskRegisterReqParams.setTransactionID(transactionID);
        postTaskRegisterReqParams.setSequenceNumber(-1);
        postTaskRegisterReqParams.setPostProcessRegistrationParm(strPostProcessRegistrationParm);
        postController.postTaskRegisterReq(postTaskRegisterReqParams);
        return Response.createSucc(transactionID, durableProcessLagTimeUpdateReqResul);
    }
}
