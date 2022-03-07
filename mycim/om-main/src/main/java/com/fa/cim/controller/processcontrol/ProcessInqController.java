package com.fa.cim.controller.processcontrol;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.processControl.IProcessInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.processcontrol.IProcessControlInqService;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 15:55
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/pctrl")
@Listenable
public class ProcessInqController implements IProcessInqController {
    @Autowired
    private IProcessControlInqService processControlInqService;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private CimJpaRepository cimJpaRepository;

    @ResponseBody
    @RequestMapping(value = "/future_hold_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FUTURE_HOLD_LIST_BY_KEY)
    public Response futureHoldListInq(@RequestBody Params.FutureHoldListInqParams params) {

        //init params
        final String transactionID = TransactionIDEnum.FUTURE_HOLD_LIST_BY_KEY.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        Infos.FutureHoldSearchKey futureHoldSearchKey = params.getFutureHoldSearchKey();
        Integer count = params.getCount();
        SearchCondition searchCondition = params.getSearchCondition();
        Validations.check(ObjectUtils.isEmpty(futureHoldSearchKey) || ObjectUtils.isEmpty(count) || ObjectUtils.isEmpty(searchCondition), "the parameter is null");

        //【step1】get schedule from calendar

        //【step2】 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(futureHoldSearchKey.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxFutureHoldListInq(...)
        Page<Infos.FutureHoldListAttributes> result = null;
        try {
            result = processControlInqService.sxFutureHoldListInq(objCommon, futureHoldSearchKey, count, searchCondition);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundFtholdEntW(), e.getCode())) {
                //ok
            } else {
                throw e;
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/npw_usage_state_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_CTRL_STATUS_INQ)
    public Response npwUsageStateSelectionInq(@RequestBody Params.UserParams userParams) {
        //【step0】check input params
        Validations.check(null == userParams || null == userParams.getUser(), "the user info is null");

        TransactionIDEnum transactionID = TransactionIDEnum.LOT_CTRL_STATUS_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //【step1】get schedule from calendar
        //【step2】call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), userParams.getUser(), new Params.AccessControlCheckInqParams(true));

        //【step3】call sxNPWUsageStateSelectionInq(...)
        List<Infos.LotCtrlStatus> result = processControlInqService.sxNPWUsageStateSelectionInq(objCommon);
        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/qtime_definition_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.Qrest_Time_Candidate_Inq)
    public Response qtimeDefinitionSelectionInq(@RequestBody Params.QtimeDefinitionSelectionInqParam qtimeDefinitionSelectionInqParam) {
        String txId = TransactionIDEnum.Qrest_Time_Candidate_Inq.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step1 - calendar_GetCurrentTimeDR get schedule from calendar

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, qtimeDefinitionSelectionInqParam.getUser(), accessControlCheckInqParams);

        //step3 - txQtimeDefinitionSelectionInq__180()
        List<Infos.QrestTimeInfo> result = processControlInqService.sxQtimeDefinitionSelectionInq(objCommon, qtimeDefinitionSelectionInqParam);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/qtime_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.QREST_TIME_LIST_INQ)
    public Response qtimeListInq(@RequestBody Params.QtimeListInqParams qtimeListInqParams) {
        //Step-0:Initialize Parameters;
        String transactionID = TransactionIDEnum.QREST_TIME_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step-1:Incoming Log Put;
        log.info(" Incoming = {}", transactionID);

        //Step-2:txCalendar_GetCurrentTimeDR;
        log.debug("【Step-2】txCalendar_GetCurrentTimeDR(...)");

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(qtimeListInqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, qtimeListInqParams.getUser(), accessControlCheckInqParams);

        //Step-4:txQtimeListInq;
        log.debug("【Step-4】call-txQtimeListInq(...)");
        Infos.QtimePageListInqInfo qtimeListPageInqInfo = new Infos.QtimePageListInqInfo();
        qtimeListPageInqInfo.setLotID(qtimeListInqParams.getLotID());
        qtimeListPageInqInfo.setWaferID(qtimeListInqParams.getWaferID());
        qtimeListPageInqInfo.setQTimeType(qtimeListInqParams.getQTimeType());
        qtimeListPageInqInfo.setActiveQTime(qtimeListInqParams.getActiveQTime());
        qtimeListPageInqInfo.setSearchCondition(qtimeListInqParams.getSearchCondition());
        qtimeListPageInqInfo.setType(qtimeListInqParams.getType());

        Page<Outputs.QrestLotInfo> result = null;
        try {
            result = processControlInqService.sxQtimeListInq(objCommon, qtimeListPageInqInfo);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
            } else {
                throw e;
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_future_pctrl_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_FUTURE_ACTION_LIST_INQ)
    public Response lotFuturePctrlListInq(@RequestBody Params.LotFuturePctrlListInqParams lotFuturePctrlListInqParams) {

        final String transactionID = TransactionIDEnum.LOT_FUTURE_ACTION_LIST_INQ.getValue();
        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar

        RetCode<Object> strAccessControlCheckInqResult;
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        Infos.LotFuturePctrlListInqInParm strLotFuturePctrlListInqInParm = lotFuturePctrlListInqParams.getStrLotFuturePctrlListInqInParm();
        lotIDs.add(strLotFuturePctrlListInqInParm.getLotID());

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        // step2 - txAccessControlCheckInq
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, lotFuturePctrlListInqParams.getUser(), accessControlCheckInqParams);


        // step3 - txLotFuturePctrlListInq
        Results.LotFuturePctrlListInqResult lotFuturePctrlListInqResult = processControlInqService.sxLotFuturePctrlListInq(objCommon, strLotFuturePctrlListInqInParm);
        return Response.createSucc(transactionID, lotFuturePctrlListInqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/qtime_expired_lot_list_with_action_pending/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EXPIRED_QREST_TIME_LOT_LIST_INQ)
    public Response qtimeExpiredLotListWithActionPendingInq(@RequestBody Params.QtimeExpiredLotListWithActionPendingInqInParm strQtimeExpiredLotListWithActionPendingInqInParm) {
        String txId = TransactionIDEnum.EXPIRED_QREST_TIME_LOT_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, strQtimeExpiredLotListWithActionPendingInqInParm.getUser(), accessControlCheckInqParams);

        // step3 - txQtimeExpiredLotListWithActionPendingInq
        List<ObjectIdentifier> rc = processControlInqService.sxQtimeExpiredLotListWithActionPendingInq(objCommon, strQtimeExpiredLotListWithActionPendingInqInParm);

        return Response.createSucc(txId, rc);
    }

    @ResponseBody
    @RequestMapping(value = "/process_hold_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PROCESS_HOLD_LIST_INQ)
    public Response processHoldListInq(@RequestBody Params.ProcessHoldListInqParams params) {
        final String txId = TransactionIDEnum.PROCESS_HOLD_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();


        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step3 - txProcessHoldListInq
        List<Infos.ProcHoldListAttributes> result = null;
        try {
            result = processControlInqService.sxProcessHoldListInq(objCommon, params);
        } catch (ServiceException ex) {
            String returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
            if (!(retCodeConfig.getNotFoundEntryW().getCode() == ex.getCode() && CimStringUtils.equals(returnOK, "1")))
                throw ex;

            if (retCodeConfig.getNotFoundEntryW().getCode() == ex.getCode()) {
                if (retCodeConfig.getNotFoundEntryW().getCode() != ex.getCode()) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/future_rework_list/inq", method = RequestMethod.POST)
    @Override
    public Response futureReworkListInq(@RequestBody Params.FutureReworkListInqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.FUTURE_REWORK_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        ObjectIdentifier lotID = params.getLotID();
        accessControlCheckInqParams.setLotIDLists(ImmutableList.of(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxFutureReworkListInq(...)
        List<Infos.FutureReworkInfo> futureReworkInfos = new ArrayList<>();
        try {
            futureReworkInfos = processControlInqService.sxFutureReworkListInq(objCommon, lotID);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfigEx.getFtrwkNotFound(), e.getCode())) {
                throw e;
            }
        }
        return Response.createSucc(transactionID, futureReworkInfos);
    }

    @ResponseBody
    @RequestMapping(value = "/mm_list/inq", method = RequestMethod.POST)
    public Response mmQTimeInqControllerInq(@RequestBody Params.MMQTimeParams mmqTimeParams) {
        RetCode<List<Infos.MMQTIME>> result = new RetCode<>();
        result.setTransactionID("OPRCQ008");
        result.setReturnCode(retCodeConfig.getSucc());

        String lotID = mmqTimeParams.getLotID();

        String sql = "SELECT a.QTIME_TYPE,a.PROD_ID,a.TRIGGER_OPE_NO,a.TARGET_OPE_NO,b.LOT_ID,MAX(b.TRIGGER_TIME) TRIGGER_time,\n" +
                "MIN(b.TARGET_TIME) min_target_time, MAX(b.TARGET_TIME) max_target_time, MAX(MAX_FLAG) MAX_FLAG\n" +
                "FROM MM_QREST a,MM_QTIME b WHERE a.ID=b.QREST_OBJ GROUP BY a.PROD_ID,a.TRIGGER_OPE_NO,a.TARGET_OPE_NO,b.LOT_ID,a.QTIME_TYPE";

        List<Object[]> objs = cimJpaRepository.query(sql);
        List<Infos.MMQTIME> mmqtimes = new ArrayList<>();
        result.setObject(mmqtimes);
        objs.stream().filter(objects -> {
            if (CimStringUtils.isEmpty(lotID)) {
                return true;
            }
            return objects[5] == lotID;
        }).forEach(objects -> {
            Infos.MMQTIME mmqtime = new Infos.MMQTIME();
            mmqtimes.add(mmqtime);
            mmqtime.setQtimeType(CimObjectUtils.toString(objects[0]));
            mmqtime.setPosID(CimObjectUtils.toString(objects[1]));
            mmqtime.setTriggerOperationNumber(CimObjectUtils.toString(objects[2]));
            mmqtime.setTargetOperationNumber(CimObjectUtils.toString(objects[3]));
            mmqtime.setLotID(CimObjectUtils.toString(objects[4]));
            mmqtime.setTriggerTime((Timestamp) objects[5]);
            mmqtime.setMinTargetTime((Timestamp) objects[6]);
            mmqtime.setMaxTargetTime((Timestamp) objects[7]);
            mmqtime.setMaxFlag(CimBooleanUtils.convert(objects[8]));
            long mms = mmqtime.getMinTargetTime().getTime();
            long cms = System.currentTimeMillis(),
                    rms, h, m, s;
            String pre;
            rms = mms - cms;
            if (rms > 0) {
                pre = "+";
            } else {
                pre = "-";
                rms = -rms;
            }
            rms = rms / 1000;
            s = rms % 60;
            rms = rms / 60;
            m = rms % 60;
            h = rms / 60;
            mmqtime.setMinRemainTime(pre + h + ":" + m + ":" + s);
            mms = mmqtime.getMaxTargetTime().getTime();
            rms = mms - cms;
            if (rms > 0) {
                pre = "+";
            } else {
                pre = "-";
                rms = -rms;
            }
            rms = rms / 1000;
            s = rms % 60;
            rms = rms / 60;
            m = rms % 60;
            h = rms / 60;
            mmqtime.setMaxRemainTime(pre + h + ":" + m + ":" + s);
            if (mmqtime.getMinTargetTime().getTime() == mmqtime.getMaxTargetTime().getTime()) {
                if (CimBooleanUtils.isTrue(mmqtime.getMaxFlag())) {
                    mmqtime.setMinTargetTime(null);
                    mmqtime.setMinRemainTime(null);
                } else {
                    mmqtime.setMaxTargetTime(null);
                    mmqtime.setMaxRemainTime(null);
                }
            }
        });
        return Response.create(result);
    }
}
