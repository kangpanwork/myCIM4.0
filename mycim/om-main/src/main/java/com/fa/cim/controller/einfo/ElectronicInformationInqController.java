package com.fa.cim.controller.einfo;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ErrorCode;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.electronicInformation.IElectronicInformationInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.runtime.lottype.CimLotTypeDO;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.lmg.LotMonitorGroupParams;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.repository.standard.eqp.EquipmentDao;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.bank.IBankService;
import com.fa.cim.service.einfo.IElectronicInformationInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 11:26
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/einfo")
@Listenable
public class ElectronicInformationInqController implements IElectronicInformationInqController {

    @Autowired
    private IElectronicInformationInqService electronicInformationInqService;
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IBankService bankService;
    @Autowired
    private EquipmentDao equipmentDao;

    @ResponseBody
    @RequestMapping(value = "/eboard_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.BULLETIN_BOARD_INFO_INQ)
    public Response eboardInfoInq(@RequestBody Params.EboardInfoInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.BULLETIN_BOARD_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        //【step2】call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        //【step3】call sxEboardInfoInq(...)
        Results.EboardInfoInqResult result = electronicInformationInqService.sxEboardInfoInq(objCommon, params.getUser());
        return Response.createSucc(TransactionIDEnum.BULLETIN_BOARD_INFO_INQ.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_operation_manual/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.OPE_GUIDE_INFO_INQ)
    public Response eqpOperationManualInq(@RequestBody Params.OpeGuideInq opeGuideInqParams) {

        //init params
        final String transactionID = TransactionIDEnum.OPE_GUIDE_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(opeGuideInqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, opeGuideInqParams.getUser(), accessControlCheckInqParams);

        //step3 - txBayListInq
        Results.OpeGuideInqResult result = electronicInformationInqService.sxEqpOperationManualInq(objCommon, opeGuideInqParams.getEquipmentID());

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/history_information/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.HISTORY_INFORMATION_INQ)
    public Response historyInformationInq(@RequestBody Params.HistoryInformationInqParams strHistoryInformationInqInParm) {

        String fromTimeStamp = strHistoryInformationInqInParm.getFromTimeStamp();
        String toTimeStamp = strHistoryInformationInqInParm.getToTimeStamp();
        String historyCategory = strHistoryInformationInqInParm.getHistoryCategory();
        List<Infos.TargetTableInfo> strTargetTableInfoSeq = strHistoryInformationInqInParm.getStrTargetTableInfoSeq();
        Long maxRecordCount = strHistoryInformationInqInParm.getMaxRecordCount();

        Results.HistoryInformationInqResult retVal;

        List<Infos.EventParameter> strEventParameter = new ArrayList<>();

        String txID = TransactionIDEnum.HISTORY_INFORMATION_INQ.getValue();
        ThreadContextHolder.setTransactionId(txID);

        int nLen = CimArrayUtils.getSize(strEventParameter);

        Infos.EventParameter eventParameter = new Infos.EventParameter();
        strEventParameter.add(eventParameter);
        eventParameter.setParameterName(("FROM_TIME_STAMP"));
        eventParameter.setParameterValue((fromTimeStamp));
        eventParameter = new Infos.EventParameter();
        strEventParameter.add(eventParameter);
        eventParameter.setParameterName("TO_TIME_STAMP");
        eventParameter.setParameterValue(toTimeStamp);
        eventParameter = new Infos.EventParameter();
        strEventParameter.add(eventParameter);
        eventParameter.setParameterName("HISTORY_CATEGORY");
        eventParameter.setParameterValue(historyCategory);

        StringBuffer hashedInfoBuf = new StringBuffer();
        ObjectIdentifier equipmentID = new ObjectIdentifier();
        for (int count = 0; count < CimArrayUtils.getSize(strTargetTableInfoSeq); count++) {
            Infos.TargetTableInfo strTargetTableInfo = strTargetTableInfoSeq.get(count);
            List<Infos.HashedInfo> strHashedInfoSeq = strTargetTableInfo.getStrHashedInfoSeq();
            for (int i = 0; i < CimArrayUtils.getSize(strHashedInfoSeq); i++) {
                Infos.HashedInfo strHashedInfo = strHashedInfoSeq.get(i);
                if (i > 0) {
                    hashedInfoBuf.append(",");
                }
                hashedInfoBuf.append("[");
                hashedInfoBuf.append("HASH_KEY:").append(strHashedInfo.getHashKey()).append(",");
                hashedInfoBuf.append("HASH_DATA:").append(strHashedInfo.getHashData());
                hashedInfoBuf.append("]");

                if (CimStringUtils.equals(strHashedInfo.getHashKey(), BizConstant.SP_HISTORYCOLUMNNAME_EQUIPMENTID)) {
                    equipmentID.setValue(strHashedInfo.getHashData());
                }
            }
        }
        eventParameter = new Infos.EventParameter();
        strEventParameter.add(eventParameter);
        eventParameter.setParameterName("SEARCH_CONDITION");
        eventParameter.setParameterValue(hashedInfoBuf.toString());

        eventParameter = new Infos.EventParameter();
        strEventParameter.add(eventParameter);
        eventParameter.setParameterName(("MAX_RECORD_COUNT"));
        eventParameter.setParameterValue(CimObjectUtils.toString(maxRecordCount));


        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);

        // step2 - txAccessControlCheckInq
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txID, strHistoryInformationInqInParm.getUser(), accessControlCheckInqParams);


        // step3 - txHistoryInformationInq
        retVal = electronicInformationInqService.sxHistoryInformationInq(objCommon,
                strHistoryInformationInqInParm);

        return Response.createSucc(txID, retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_annotation/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_COMMENT_INFO_INQ)
    public Response lotAnnotationInq(@RequestBody Params.LotAnnotationInqParams params) {

        //init params
        final String transactionID = TransactionIDEnum.LOT_COMMENT_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        ObjectIdentifier lotID = params.getLotID();
        Validations.check(org.springframework.util.ObjectUtils.isEmpty(lotID), "lotID is null...");


        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxLotAnnotationInq(...)
        Results.LotAnnotationInqResult result = null;
        try {
            result = electronicInformationInqService.sxLotAnnotationInq(objCommon, lotID);
        } catch (ServiceException ex) {
            if (!Validations.isEquals(ex.getCode(), retCodeConfig.getSomeLotComtDataError())) {
                throw ex;
            }
        }

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_memo_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_NOTE_INFO_INQ)
    public Response lotMemoInfoInq(@RequestBody Params.LotMemoInfoInqParams params) {

        //init params
        final String transactionID = TransactionIDEnum.LOT_NOTE_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        ObjectIdentifier lotID = params.getLotID();
        Validations.check(org.springframework.util.ObjectUtils.isEmpty(lotID), "lotID is null...");


        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxLotMemoInfoInq(...)
        Results.LotMemoInfoInqResult result = electronicInformationInqService.sxLotMemoInfoInq(objCommon, lotID);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_operation_selection_from_history/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_OPERATION_LIST_FROM_HISTORY_INQ)
    public Response lotOperationSelectionFromHistoryInq(@RequestBody Params.LotOperationSelectionFromHistoryInqParams params) {
        //======Pre Process======
        String txId = TransactionIDEnum.LOT_OPERATION_LIST_FROM_HISTORY_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        privilegeCheckParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), privilegeCheckParams);

        //Step3 - txLotOperationSelectionFromHistoryInq
        Results.LotOperationSelectionFromHistoryInqResult result = electronicInformationInqService.sxLotOperationSelectionFromHistoryInq(objCommon, params);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_ope_memo_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_OPERATION_NOTE_INFO_INQ)
    public Response lotOpeMemoInfoInq(@RequestBody Params.LotOpeMemoInfoInqParams lotOpeMemoInfoInqParams) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.LOT_OPERATION_NOTE_INFO_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(lotOpeMemoInfoInqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), lotOpeMemoInfoInqParams.getUser(), accessControlCheckInqParams);

        //Step-4:txLotOpeMemoInfoInq;
        log.debug("【Step-4】call-txLotOpeMemoInfoInq(...)");
        Results.LotOpeMemoInfoInqResult result = electronicInformationInqService.sxLotOpeMemoInfoInq(lotOpeMemoInfoInqParams, objCommon);

        // Step-5:Post Process(Generate Output Results/event log put/Set Transaction ID);
        Response response = Response.createSucc(transactionID.getValue(), result);

        //whether roll back;
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/lot_ope_memo_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_OPERATION_NOTE_LIST_INQ)
    public Response lotOpeMemoListInq(@RequestBody Params.LotOpeMemoListInqParams params) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.LOT_OPERATION_NOTE_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), params.getUser(), accessControlCheckInqParams);

        //Step-4:txLotOpeMemoListInq;
        log.debug("【Step-4】call-txLotOpeMemoListInq(...)");
        Results.LotOpeMemoListInqResult result = electronicInformationInqService.sxLotOpeMemoListInq(params, objCommon);

        // Step-5:Post Process(Generate Output Results/event log put/Set Transaction ID);

        Response response = Response.createSucc(transactionID.getValue(), result);

        //whether roll back;
        Validations.isSuccessWithException(response);

        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/lot_operation_history/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.OPERATION_HISTORY_INQ)
    public Response lotOperationHistoryInq(@RequestBody Params.LotOperationHistoryInqParams params) {
        //======Pre Process======
        String txId = TransactionIDEnum.OPERATION_HISTORY_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        privilegeCheckParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), privilegeCheckParams);

        //Step3 - txLotOperationHistoryInq__160
        Results.LotOperationHistoryInqResult retVal = null;
        try {
            SearchCondition searchCondition = params.getSearchCondition();
            retVal = electronicInformationInqService.sxLotOperationHistoryInq(objCommon, params.getLotID(), params.getRouteID(), params.getOperationID(), params.getOperationNumber(), params.getOperationPass(), params.getOperationCategory(), params.getPinPointFlag(),params.getFromTimeStamp(),params.getToTimeStamp());
            if (searchCondition!=null&&retVal!=null&&retVal.getStrOperationHisInfo()!=null){
                retVal.setStrOperationHisInfoPage(CimPageUtils.convertListToPage(retVal.getStrOperationHisInfo(),searchCondition.getPage(),searchCondition.getSize()));
            }
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getSomeopehisDataError(), e.getCode())) {
                //do nothing
            } else {
                throw e;
            }
        }

        return Response.createSucc(txId, retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/sub_lot_type_id_list_ex/inq", method = RequestMethod.POST)
    @CimMapping(names = "OCONQ005_EX") //This interface does not have a TXID. Using SUB_LOT_TYPE_ID_LIST_INQ TXID results in a duplicate TX, so it is changed to a unique TXID
    public Response subLotTypeIdListEx(@RequestBody Params.SubLotTypeIDListExInqParams params) {
        final TransactionIDEnum transactionId = TransactionIDEnum.SUB_LOT_TYPE_ID_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionId.getValue());

        //step3 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId.getValue(), params.getUser(), accessControlCheckInqParams);

        List<Infos.LotTypeInfo> result = new ArrayList<>();

        List<CimLotTypeDO> lotTypeDOList = bankService.getLotType();
        int lotTypesCount = CimArrayUtils.getSize(lotTypeDOList);
        for (int i = 0; i < lotTypesCount; i++) {
            CimLotTypeDO lotTypeDO = lotTypeDOList.get(i);
            if (lotTypeDO != null) {
                Params.SubLotTypeListInqParams parameters = new Params.SubLotTypeListInqParams();
                parameters.setUser(params.getUser());
                parameters.setLotType(lotTypeDO.getLotType());
                Results.SubLotTypeListInqResult subLotTypeResponse = electronicInformationInqService.sxSubLotTypeListInq(objCommon, parameters);
                int tmpListCount = CimArrayUtils.getSize(subLotTypeResponse.getStrLotTypes());
                if (tmpListCount > 0) {
                    result.addAll(subLotTypeResponse.getStrLotTypes());
                }
            }
        }
        Response response = Response.createSucc(transactionId.getValue(), result);

        //step-5:judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_top10/inq", method = RequestMethod.POST)
    @CimMapping(names = "OEQPQ003_EQPTOP10") //This interface does not have a TXID. Using EQP_LIST_INQ TXID results in a duplicate TX, so it is changed to a unique TXID
    public Response eqpTop10(@RequestBody Params.EqpReqParams eqpReqParams) {

        String value = ObjectIdentifier.fetchValue(eqpReqParams.getEquipmentID());

        if (value == null) value = "";

        List<Infos.EqpTopInfo> data = new ArrayList<>();

        Response response = Response.createSucc(TransactionIDEnum.EQP_LIST_INQ.getValue(), data);
        List<Object[]> equipmentIDs = equipmentDao.findEquipmentIDs("%" + value.trim() + "%");
        if (CimArrayUtils.isEmpty(equipmentIDs)) {
            return response;
        }
        List<String> specialControls = null;
        String _specialControl,_temp,temp = null;
        for (Object[] item : equipmentIDs) {
            specialControls=new ArrayList<>();
            _temp = (String) item[0];
            _specialControl = (String) item[3];
            if (CimStringUtils.equals(temp,_temp)){
                if (CimStringUtils.isNotEmpty(_specialControl)){
                    specialControls.add(_specialControl);
                }
                continue;
            }
            Infos.EqpTopInfo eqpTopInfo = new Infos.EqpTopInfo();
            if (CimStringUtils.isNotEmpty(_specialControl)){
                specialControls.add(_specialControl);
            }
            eqpTopInfo.setSpecialControls(specialControls);
            ObjectIdentifier dataItem = ObjectIdentifier.build((String) item[0], (String) item[1]);
            eqpTopInfo.setEquipmentID(dataItem);
            eqpTopInfo.setEquipmentCategory((String)item[2]);
            data.add(eqpTopInfo);
            temp=_temp;
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/port_info/Inq", method = RequestMethod.POST)
    @CimMapping(names = "OEQPQ001_PORTINFOINQ") //This interface does not have a TXID. Using EQP_INFO_INQ TXID results in a duplicate TX, so it is changed to a unique TXID
    public Response portInfoInq(@RequestBody Params.PortInfoInqParams portInfoInqParams) {
        //step 1 - Pre Process
        User user = portInfoInqParams.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), TransactionIDEnum.EQP_INFO_INQ.getValue());
        }

        //step 2 - Main Process
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.EQP_INFO_INQ, user);

        //step 3 - Post Process
        return Response.createSucc(TransactionIDEnum.EQP_INFO_INQ.getValue(), electronicInformationInqService.sxPortInfoInq(objCommon, portInfoInqParams));
    }

    @ResponseBody
    @RequestMapping(value = "/wafer_scrapped_history/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SCRAP_HISTORY_INQ)
    public Response waferScrappedHistoryInq(@RequestBody Params.WaferScrappedHistoryInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.SCRAP_HISTORY_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        ObjectIdentifier lotID = params.getLotID();
        Validations.check(org.springframework.util.ObjectUtils.isEmpty(lotID), "lotID is null...");

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxWaferScrappedHistoryInq(...)
        List<Infos.ScrapHistories> result = null;
        String returnOK = null;
        try {
            result = electronicInformationInqService.sxWaferScrappedHistoryInq(objCommon, lotID, params.getCassetteID());
            returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundScrList(), e.getCode()) && 1 == CimNumberUtils.intValue(returnOK)) {
            }
        }
        return Response.createSucc(transactionID, CimObjectUtils.isEmpty(result) ? Collections.emptyList() : result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_alarm_hist/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_ALARM_HISTORY_INQ)
    public Response EqpAlarmHistInq(@RequestBody Params.EqpAlarmHistInqParams params) {
        String txId = TransactionIDEnum.EQP_ALARM_HISTORY_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setEquipmentID(params.getObjectID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), privilegeCheckParams);

        //Step3 - txEqpBufferInfoInq
        List<Infos.EquipmentAlarm> result = electronicInformationInqService.sxEqpAlarmHistInq(objCommon, params);
        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/edc_history/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.COLLECTED_DATA_HISTORY_INQ)
    public Response edcHistoryInq(@RequestBody Params.EDCHistoryInqParams params) {
        //Pre Process;
        String txId = TransactionIDEnum.COLLECTED_DATA_HISTORY_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), privilegeCheckParams);

        //Step3 - txEDCDataItemListByKeyInq
        Results.EDCHistoryInqResult result = null;
        try {
            result = electronicInformationInqService.sxEDCHistoryInq(objCommon, params);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundCdata(), e.getCode()) ||
                    Validations.isEquals(retCodeConfig.getCdataDeleted(), e.getCode()) ||
                    Validations.isEquals(retCodeConfig.getDefinedDcSpecInfo(), e.getCode())) {
                //ok
            } else {
                throw e;
            }
        }


        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_search_for_setting_eqp_board/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_SEARCH_FOR_SETTING_EQPBOARD_INQ)
    public Response eqpSearchForSettingEqpBoardInq(@RequestBody Params.EqpSearchForSettingEqpBoardParams eqpSearchForSettingEqpBoardParams) {
        // step1 set current tx id
        String txId = TransactionIDEnum.EQP_SEARCH_FOR_SETTING_EQPBOARD_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, eqpSearchForSettingEqpBoardParams.getUser(), privilegeCheckParams);

        //step3 - eqp search
        List<Results.EqpSearchForSettingEqpBoardResult> eqpSearchForSettingEqpBoardResults
                = electronicInformationInqService.sxEqpSearchForSettingEqpBoardInq(objCommon, eqpSearchForSettingEqpBoardParams);

        return Response.createSucc(txId, eqpSearchForSettingEqpBoardResults);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_area_board_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_AREA_BOARD_LIST_INQ)
    public Response eqpAreaBoardListInq(@RequestBody Params.EqpAreaBoardListParams eqpAreaBoardListParams) {

        // step1 set current tx id
        String txId = TransactionIDEnum.EQP_AREA_BOARD_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, eqpAreaBoardListParams.getUser(), privilegeCheckParams);

        //step3 - eqp search
        List<Results.EqpAreaBoardListResult> result
                = electronicInformationInqService.sxEqpAreaBoardListInq(objCommon, eqpAreaBoardListParams);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_work_zone_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_WORK_ZONE_LIST_INQ)
    public Response eqpWorkZoneListInq(@RequestBody Params.EqpWorkZoneListParams eqpWorkZoneListParams) {
        // step1 set current tx id
        String txId = TransactionIDEnum.EQP_WORK_ZONE_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, eqpWorkZoneListParams.getUser(), privilegeCheckParams);

        //step3 - eqp work zone search
        List<String> workZones = electronicInformationInqService.sxEqpWorkZoneListInq(objCommon, eqpWorkZoneListParams);

        return Response.createSucc(txId, workZones);
    }

    @ResponseBody
    @RequestMapping(value = "/monitor_lot_edc_history/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MON_LOT_COLLECTED_DATA_HISTORY_INQ)
    public Response monitorLotDataCollectionInq(
            @RequestBody LotMonitorGroupParams.MonitorDataCollectionParams monitorDataCollectionParams) {
        // step1 set current tx id
        String txId = TransactionIDEnum.MON_LOT_COLLECTED_DATA_HISTORY_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        // Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId,
                monitorDataCollectionParams.getUser(), privilegeCheckParams);

        return Response.createSucc(txId,
                electronicInformationInqService.sxMonitorLotDataCollectionInq(objCommon, monitorDataCollectionParams));
    }

}
