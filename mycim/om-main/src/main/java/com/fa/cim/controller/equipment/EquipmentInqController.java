package com.fa.cim.controller.equipment;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.equipment.IEquipmentInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.eqp.IBFurnaceEQPBatchInfo;
import com.fa.cim.eqp.IBFurnaceEQPBatchInfoInqParams;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.equipment.IEquipmentInqService;
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
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 11:07
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/eqp")
@Listenable
public class EquipmentInqController implements IEquipmentInqController {
    @Autowired
    private IEquipmentInqService equipmentInqService;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private RetCodeConfig retCodeConfig;

    @ResponseBody
    @RequestMapping(value = "/chamber_status_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CANDIDATE_CHAMBER_STATUS_INQ)
    public Response chamberStatusSelectionInq(@RequestBody Params.ChamberStatusSelectionInqParams params) {
        log.debug("eqpStatusSelectionInq(): enter eqpStatusSelectionInq");
        String txId = TransactionIDEnum.CANDIDATE_CHAMBER_STATUS_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams =
                new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(),
                accessControlCheckInqParams);

        //step3 - txChamberStatusSelectionInq
        List<Infos.CandidateChamberStatusInfo> result = equipmentInqService.sxChamberStatusSelectionInq(
                objCommon, params.getEquipmentID());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/equipment_mode_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CANDIDATE_EQUIPMENT_INQ)
    public Response equipmentModeSelectionInq(@RequestBody Params.EquipmentModeSelectionInqParams params) {
        String txId = TransactionIDEnum.CANDIDATE_EQUIPMENT_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step2 - txChamberStatusSelectionInq
        Results.EquipmentModeSelectionInqResult result = equipmentInqService.sxEquipmentModeSelectionInq(objCommon, params.getEquipmentID(), params.getModeChangeType(), params.getPortID());
        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_status_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CANDIDATE_EQP_STATUS_INQ)
    public Response eqpStatusSelectionInq(@RequestBody Params.EqpStatusSelectionInqParams params) {
        log.debug("eqpStatusSelectionInq(): enter eqpStatusSelectionInq");
        String txId = TransactionIDEnum.CANDIDATE_EQP_STATUS_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams =
                new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(),
                accessControlCheckInqParams);

        //Step3 - txEqpStatusSelectionInq
        Results.EqpStatusSelectionInqResult eqpStatusSelectionInqResult = equipmentInqService.sxEqpStatusSelectionInq(
                objCommon, params.getEquipmentID(), params.getAllInquiryFlag());

        return Response.createSucc(txId, eqpStatusSelectionInqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_eap_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_DETAIL_INFO_INQ)
    public Response eqpEAPInfoInq(@RequestBody Params.EqpEAPInfoInqParams params) {

        if (log.isErrorEnabled()){
            log.debug("step1 - init param");
        }
        String txId = TransactionIDEnum.EQP_DETAIL_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step2 - call txAccessControlCheckInq(...)
        if (log.isDebugEnabled()){
            log.debug("step2 - call checkPrivilegeAndGetObjCommon(...)");
        }
        Params.AccessControlCheckInqParams accessParam = new Params.AccessControlCheckInqParams(true);
        accessParam.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessParam);

        //step3 - sxEqpEAPInfoInq(...)
        if (log.isDebugEnabled()){
            log.debug("step3 - sxEapEAPInfoInq");
        }
        Results.EqpEAPInfoInqResult result = equipmentInqService.sxEqpEAPInfoInq(objCommon, params.getEquipmentID());
        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_memo_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_NOTE_INQ)
    public Response eqpMemoInfoInq(@RequestBody Params.EqpMemoInfoInqParams eqpMemoInfoInqParams) {
        String transcationID = TransactionIDEnum.EQP_NOTE_INQ.getValue();
        ThreadContextHolder.setTransactionId(transcationID);


        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transcationID, eqpMemoInfoInqParams.getUser(), accessControlCheckInqParams);

        //step3 - txEqpMemoInfoInq
        Results.EqpMemoInfoInqResult result = null;
        String returnOK = null;
        try {
            result = equipmentInqService.sxEqpMemoInfoInq(objCommon, eqpMemoInfoInqParams.getEquipmentID());
            returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNoEqpNote(), e.getCode()) && CimStringUtils.equals(returnOK, retCodeConfig.getWarn().toString())) {
                //do nothing
            }
            if (Validations.isEquals(retCodeConfig.getNoEqpNote(), e.getCode())) {
                return Response.createSuccWithOmCode(transcationID, new OmCode(retCodeConfig.getSucc().getCode(), retCodeConfig.getNoEqpNote().getMessage()), result);
            } else if (Validations.isEquals(retCodeConfig.getSomeEqpNoteDataError(), e.getCode())) {
                return Response.createSuccWithOmCode(transcationID, new OmCode(retCodeConfig.getSucc().getCode(), retCodeConfig.getSomeEqpNoteDataError().getMessage()), result);
            } else {
                throw e;
            }

        }
        return Response.createSucc(transcationID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_recipe_parameter_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_RECIPE_PARAMETER_LIST_INQ)
    public Response eqpRecipeParameterListInq(@RequestBody Params.EqpRecipeParameterListInq params) {
        String txId = TransactionIDEnum.EQP_RECIPE_PARAMETER_LIST_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> machineRecipeIDs = new ArrayList<>();
        if (!ObjectIdentifier.isEmpty(params.getLotID())) {
            lotIDs.add(params.getLotID());
        }
        if (!ObjectIdentifier.isEmpty(params.getMachineRecipeID())) {
            machineRecipeIDs.add(params.getMachineRecipeID());
        }

        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setEquipmentID(params.getEquipmentID());
        privilegeCheckParams.setLotIDLists(lotIDs);
        privilegeCheckParams.setMachineRecipeIDList(machineRecipeIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), privilegeCheckParams);

        //step3 - txEqpRecipeParameterListInq
        Results.EqpRecipeParameterListInqResult result = equipmentInqService.sxEqpRecipeParameterListInq(objCommon, params);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_recipe_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_RELATED_RECIPELID_LIST_INQ)
    public Response eqpRecipeSelectionInq(@RequestBody Params.EqpRecipeSelectionInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.EQP_RELATED_RECIPELID_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        ObjectIdentifier equipmentID = params.getEquipmentID();
        Validations.check(org.springframework.util.ObjectUtils.isEmpty(equipmentID), "equipmentID is null...");

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxCodeSelectionInq(...)
        Results.EqpRecipeSelectionInqResult result = equipmentInqService.sxEqpRecipeSelectionInq(objCommon, equipmentID);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/lots_move_in_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOTS_INFO_FOR_OPERATION_START_INQ)
    public Response LotsMoveInInfoInq(@RequestBody Params.LotsMoveInInfoInqParams lotsMoveInInfoInqParams) {

        //init params
        final String transactionID = TransactionIDEnum.LOTS_INFO_FOR_OPERATION_START_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(lotsMoveInInfoInqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, lotsMoveInInfoInqParams.getUser(), accessControlCheckInqParams);


        //step 3
        Results.LotsMoveInInfoInqResult result = equipmentInqService.sxLotsMoveInInfoInq(objCommon, lotsMoveInInfoInqParams);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/lots_move_in_info_for_ib/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_INFO_FOR_OPE_START_FOR_INTERNAL_BUFFER_INQ)
    public Response LotsMoveInInfoForIBInq(@RequestBody Params.LotsMoveInInfoForIBInqParams params) {

        //【step0】init params
        final String transactionID = TransactionIDEnum.LOT_INFO_FOR_OPE_START_FOR_INTERNAL_BUFFER_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        ObjectIdentifier equipmentID = params.getEquipmentID();
        List<ObjectIdentifier> cassetteID = params.getCassetteIDs();
        Validations.check(ObjectIdentifier.isEmpty(equipmentID) || CimArrayUtils.isEmpty(cassetteID), "the parameter is null...");

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //step 3 Main Process
        Results.LotsMoveInInfoInqResult result = equipmentInqService.sxLotsMoveInInfoForIBInq(objCommon, params);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_info_for_ib/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_INFO_FOR_INTERNAL_BUFFER_INQ)
    public Response eqpInfoForIBInq(@RequestBody Params.EqpInfoForIBInqParams params) {
        String transactionID = TransactionIDEnum.EQP_INFO_FOR_INTERNAL_BUFFER_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), privilegeCheckParams);

        //Step3 - txEqpInfoForIBInq__120
        Results.EqpInfoForIBInqResult result = equipmentInqService.sxEqpInfoForIBInq(objCommon, params);


        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_INFO_INQ)
    public Response eqpInfoInq(@RequestBody Params.EqpInfoInqParams eqpInfoInqParams) {
        String transactionID = TransactionIDEnum.EQP_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setEquipmentID(eqpInfoInqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, eqpInfoInqParams.getUser(), privilegeCheckParams);

        //step 2 - Main Process
        return Response.createSucc(transactionID, equipmentInqService.sxEqpInfoInq(objCommon, eqpInfoInqParams));
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_list_by_step/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_LIST_BY_OPERATION_INQ)
    public Response eqpListByStepInq(@RequestBody Params.EqpListByStepInqParm parm) {

        final String transactionID = TransactionIDEnum.EQP_LIST_BY_OPERATION_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, parm.getUser(), accessControlCheckInqParams);


        //step3 - txEqpListByStepInq
        List<Infos.AreaEqp> result = equipmentInqService.sxEqpListByStepInq(objCommon, parm);


        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_list_by_bay/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_LIST_INQ)
    public Response eqpListByBayInq(@RequestBody Params.EqpListByBayInqInParm eqpListByBayInqInParm) {

        String transactionID = TransactionIDEnum.EQP_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, eqpListByBayInqInParm.getUser(), accessControlCheckInqParams);


        //step3 - txEqpListByBayInq
        Results.EqpListByBayInqResult result = equipmentInqService.sxEqpListByBayInq(objCommon, eqpListByBayInqInParm);
        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/all_eqp_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_ID_LIST_INQ)
    public Response allEqpListByBayInq(@RequestBody Params.EquipmentIDListParams params) {
        String transactionID = TransactionIDEnum.EQP_ID_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), privilegeCheckParams);

        //Step3 - txAllEqpListByBayInq
        List<ObjectIdentifier> result = equipmentInqService.sxAllEqpListByBayInq(objCommon);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/bay_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WORK_AREA_LIST_INQ)
    public Response bayListInq(@RequestBody Params.BayListInqParams bayListInqParams) {

        String transactionID = TransactionIDEnum.WORK_AREA_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, bayListInqParams.getUser(), accessControlCheckInqParams);

        //step3 - txBayListInq
        List<Infos.WorkArea> result = null;
        try {
            result = equipmentInqService.sxBayListInq(objCommon, bayListInqParams.getUser().getUserID());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {

            }
        }
        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_buffer_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_BUFFER_RESOURCE_INFO_INQ)
    public Response eqpBufferInfoInq(@RequestBody Params.EqpBufferInfoInqInParm params) {
        String txId = TransactionIDEnum.EQP_BUFFER_RESOURCE_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), privilegeCheckParams);

        //Step3 - txEqpBufferInfoInq
        Results.EqpBufferInfoInqResult result = equipmentInqService.sxEqpBufferInfoInq(objCommon, params);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/spc_check_info/inq", method = RequestMethod.POST)
    @Override
    public Response spcCheckInfoInq(@RequestBody Params.SpcCheckInfoInqParams spcCheckInfoInqParams) {
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessInqService.checkPrivilegeAndGetObjCommon(null, spcCheckInfoInqParams.getUser(), accessControlCheckInqParams);
        Results.SpcCheckInfoInqResult spcCheckInfoInqResult = equipmentInqService.spcCheckInfoInq(spcCheckInfoInqParams);
        return Response.createSucc(spcCheckInfoInqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/general_eqp_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.COMMON_EQP_INFO_INQ)
    public Response generalEqpInfoInq(@RequestBody Params.CommonEqpInfoParam params) {

        //init params
        final String transactionID = TransactionIDEnum.COMMON_EQP_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //step3 - txGeneralEqpInfoInq
        Results.GeneralEqpInfoInqResult result = equipmentInqService.sxGeneralEqpInfoInq(objCommon, params);

        //step4 - Post Process
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/ib_furnace_eqp_batch_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.IB_FURNACE_EQP_BATCH_INFO)
    public Response IBFurnaceEQPBatchInfoInq(@RequestBody IBFurnaceEQPBatchInfoInqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.IB_FURNACE_EQP_BATCH_INFO.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEqpID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //step3 - txGeneralEqpInfoInq
        IBFurnaceEQPBatchInfo ibFurnaceEQPBatchInfo = equipmentInqService.sxIBFurnaceEQPBatchInfoInq(objCommon, params);

        //step4 - Post Process
        return Response.createSucc(transactionID, ibFurnaceEQPBatchInfo);
    }
}
