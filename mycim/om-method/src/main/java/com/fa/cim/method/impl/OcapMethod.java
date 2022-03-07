package com.fa.cim.method.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.nonruntime.CimOcapDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IOcapMethod;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.remote.IOCAPRemoteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>AMSMethod .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2021/1/22   ********     Decade     create file
 *
 * @author: hd
 * @date: 2021/1/22 17:45
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class OcapMethod implements IOcapMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IOCAPRemoteManager ocapRemoteManager;

    @Autowired
    private ILotMethod lotMethod;

    public void ocapUpdateRpt(Infos.ObjCommon objCommon, Params.OcapReqParams params) {
        CimOcapDO cimOcapDo = null;
        // step -1 search ocap information by lot ID
        if (log.isDebugEnabled()) {
            log.debug("step - 1 search ocap information by lot ID ");
        }
        if (CimStringUtils.isNotEmpty(params.getOcapNo())
                && ObjectIdentifier.isNotEmptyWithValue(params.getLotID())) {
            CimLot lot = baseCoreFactory.getBO(CimLot.class, params.getLotID());
            Validations.check(null == lot, retCodeConfig.getNotFoundLot(),
                    ObjectIdentifier.fetchValue(params.getLotID()));
            CimOcapDO example = new CimOcapDO();
            example.setLotID(lot.getIdentifier());
            example.setOcapNo(params.getOcapNo());
            cimOcapDo = cimJpaRepository.findOne(Example.of(example)).orElse(null);
        }

        if (null == cimOcapDo) {
            if (log.isDebugEnabled()) {
                log.debug("ocap info is not exsit. return");
            }
            return;
        }

        // step -2 delete ocap information
        if (log.isDebugEnabled()) {
            log.debug("step - 2 delete ocap information");
        }
        if (CimBooleanUtils.isTrue(params.getRemoveFlag())) {
            if (log.isDebugEnabled()) {
                log.debug("removeFlag is {}", params.getReMeasureFlag());
                log.debug("step3 - delete ocap info");
            }
            cimJpaRepository.delete(cimOcapDo);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("reMeasureFlag is {}", params.getReMeasureFlag());
                log.debug("addMeasureFlag is {}", params.getAddMeasureFlag());
            }
            if (CimBooleanUtils.isTrue(params.getReMeasureFlag())
                    && CimBooleanUtils.isFalse(params.getAddMeasureFlag())) {
                //step -3 update ocap reMeasure info
                if (log.isDebugEnabled()) {
                    log.debug("step3 - update ocap reMeasure info");
                }
                cimOcapDo.setReMeasureFlag(true);
            } else if (CimBooleanUtils.isTrue(params.getAddMeasureFlag())) {
                //step -4 update ocap addMeasure info
                if (log.isDebugEnabled()) {
                    log.debug("step3 - update ocap addMeasure info");
                }
                cimOcapDo.setAddMeasureFlag(true);
            }
            if (ObjectIdentifier.isNotEmptyWithValue(params.getEquipmentID())) {
                CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, params.getEquipmentID());
                Validations.check(null == equipment, retCodeConfig.getNotFoundEquipment(),
                        ObjectIdentifier.fetchValue(params.getEquipmentID()));
                cimOcapDo.setEquimentID(equipment.getIdentifier());
                cimOcapDo.setEquimentObj(equipment.getPrimaryKey());
            }
            if (ObjectIdentifier.isNotEmptyWithValue(params.getRecipeID())) {
                CimMachineRecipe recipe = baseCoreFactory.getBO(CimMachineRecipe.class, params.getRecipeID());
                Validations.check(null == recipe, retCodeConfig.getNotFoundMachineRecipe());
                cimOcapDo.setRecipeID(recipe.getIdentifier());
                cimOcapDo.setRecipeObj(recipe.getPrimaryKey());
            }
            cimOcapDo.setWaferList(params.getWaferList());
            cimOcapDo.setUpdateTime(objCommon.getTimeStamp().getReportTimeStamp());
            cimJpaRepository.save(cimOcapDo);
        }
    }

    @Override
    public void ocapInfomationSave(ObjectIdentifier lotId, String ocapNo, ObjectIdentifier equimentId) {
        if (ObjectIdentifier.isNotEmptyWithValue(lotId) && CimStringUtils.isNotEmpty(ocapNo)) {
            //check return lotID
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotId);
            Validations.check(null == aLot, retCodeConfig.getNotFoundLot(),
                    ObjectIdentifier.fetchValue(lotId));
            CimOcapDO example = new CimOcapDO();
            example.setLotID(ObjectIdentifier.fetchValue(lotId));
            example.setOcapNo(ocapNo);
            if (null == cimJpaRepository.findOne(Example.of(example)).orElse(null)) {
                if (log.isDebugEnabled()) {
                    log.debug("ocap info need create");
                }
                CimOcapDO entity = new CimOcapDO();
                if (ObjectIdentifier.isNotEmptyWithValue(lotId)) {
                    entity.setLotID(aLot.getIdentifier());
                    entity.setLotObj(aLot.getPrimaryKey());
                }
                entity.setOcapNo(ocapNo);
                if (ObjectIdentifier.isNotEmptyWithValue(equimentId)) {
                    entity.setEquimentID(ObjectIdentifier.fetchValue(equimentId));
                    entity.setEquimentObj(ObjectIdentifier.fetchReferenceKey(equimentId));
                }
                entity.setCreateTime(CimDateUtils.getCurrentTimeStamp());
                cimJpaRepository.save(entity);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("ocap info exist. do nothing");
                }
            }
        }
    }

    @Override
    public Infos.OcapInfo ocapInformationGetByLotID(ObjectIdentifier lotId) {
        CimOcapDO example = new CimOcapDO();
        example.setLotID(ObjectIdentifier.fetchValue(lotId));
        return Optional.ofNullable(cimJpaRepository.findOne(Example.of(example)).orElse(null)).map(cimOcapDo -> {
            Infos.OcapInfo ocapInfo = new Infos.OcapInfo();
            ocapInfo.setOcapNo(cimOcapDo.getOcapNo());
            ocapInfo.setEquipmentID(ObjectIdentifier.build(cimOcapDo.getEquimentID(), cimOcapDo.getEquimentObj()));
            ocapInfo.setLotID(ObjectIdentifier.build(cimOcapDo.getLotID(), cimOcapDo.getLotObj()));
            ocapInfo.setRemoveFlag(cimOcapDo.getRemoveFlag());
            ocapInfo.setAddMeasureFlag(cimOcapDo.getAddMeasureFlag());
            ocapInfo.setReMeasureFlag(cimOcapDo.getReMeasureFlag());
            ocapInfo.setWaferList(cimOcapDo.getWaferList());
            ocapInfo.setRecipeID(ObjectIdentifier.build(cimOcapDo.getRecipeID(), cimOcapDo.getRecipeObj()));
            return ocapInfo;
        }).orElse(null);
    }

    @Override
    public List<Infos.StartCassette> ocapCheckEquipmentAndSamplingAndRecipeExchange(ObjectIdentifier equipmentID,
                                                       List<Infos.StartCassette> startCassetteList) {
        List<Infos.StartCassette> result = startCassetteList;
        if (CimArrayUtils.isNotEmpty(result)) {
            for (Infos.StartCassette startCassette : startCassetteList) {
                if (CimArrayUtils.isNotEmpty(startCassette.getLotInCassetteList())) {
                    for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                        if (ObjectIdentifier.isNotEmptyWithValue(lotInCassette.getLotID())) {
                            CimOcapDO example = new CimOcapDO();
                            example.setLotID(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                            CimOcapDO cimOcapDo = cimJpaRepository.findOne(Example.of(example)).orElse(null);
                            if (cimOcapDo == null) continue;
                            // if current lot have ocap info ,make sure lot specify equipment is correct
                            if (log.isDebugEnabled()) {
                                log.debug("step1 - if current lot have ocap info ," +
                                        "make sure lot specify equipment is correct");
                            }
                            Validations.check(!ObjectIdentifier.equalsWithValue(equipmentID, cimOcapDo.getEquimentID()),
                                    retCodeConfigEx.getOcapSpecifiedEquipmentError(),
                                    cimOcapDo.getEquimentID());
                            if (log.isDebugEnabled()) {
                                log.debug("step2 - if current lot have ocap info, " +
                                        "make sure lot specify recipe is correct");
                            }
                            if (!ObjectIdentifier.equalsWithValue(cimOcapDo.getRecipeID(),
                                    lotInCassette.getStartRecipe().getMachineRecipeID())){
                                if (log.isDebugEnabled()){
                                    log.debug("specify recipe [%s] need exchange to ocap specified recipe [%s]",
                                            ObjectIdentifier.fetchValue(lotInCassette.getStartRecipe().
                                                    getMachineRecipeID()),
                                            cimOcapDo.getRecipeID());
                                }
                                lotInCassette.getStartRecipe().setMachineRecipeID(ObjectIdentifier.build(
                                        cimOcapDo.getRecipeID(),
                                        cimOcapDo.getRecipeObj()));
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("addMeasureFlag is {}", cimOcapDo.getAddMeasureFlag());
                            }
                            //check addMeasure ocap,waferList can not be empty.
                            if (log.isDebugEnabled()) {
                                log.debug("step3 - check addMeasure ocap,waferList can not be empty.");
                            }
                            Validations.check(CimStringUtils.isEmpty(cimOcapDo.getWaferList()),
                                    retCodeConfigEx.getOcapMeasureWaferListIsEmpty());

                            if (CimBooleanUtils.isTrue(cimOcapDo.getAddMeasureFlag())
                                    || CimBooleanUtils.isTrue(cimOcapDo.getReMeasureFlag())) {
                                String waferListStr = cimOcapDo.getWaferList();
                                //check reMeasure & addMeasure ocap,waferList can not be empty.
                                if (log.isDebugEnabled()) {
                                    log.debug("step4 - check reMeasure & addMeasure ocap,waferList is correct");
                                }
                                //fiter sampling wafer
                                List<String> processExecWaferList = lotInCassette.getLotWaferList().parallelStream()
                                        .filter(lotWafer -> CimBooleanUtils.isTrue(lotWafer.getProcessJobExecFlag()))
                                        .map(lotWafer -> ObjectIdentifier.fetchValue(lotWafer.getWaferID()))
                                        .collect(Collectors.toList());
                                if (CimArrayUtils.isNotEmpty(processExecWaferList)) {
                                    List<String> ocapWaferList = CimArrayUtils.
                                            convertStringList(waferListStr.split(BizConstant.SEPARATOR_COMMA));
                                    //check addMeasure ocap ,waferList should be same as sampling
                                    //waferList from reserve information.
                                    if (log.isDebugEnabled()) {
                                        log.debug("check smapling info ,waferList need exchange to ocap specify");
                                    }
                                    if (ocapWaferList.size() != processExecWaferList.size()
                                            || !ocapWaferList.containsAll(processExecWaferList)){
                                        if (log.isDebugEnabled()){
                                            log.debug("specify waferList need exchange " +
                                                    "to ocap specified waferList [%s]",waferListStr);
                                        }
                                        for (Infos.LotWafer lotWafer : lotInCassette.getLotWaferList()) {
                                            lotWafer.setProcessJobExecFlag(false);
                                            for (String ocapLotWafer : ocapWaferList) {
                                                if (ObjectIdentifier.equalsWithValue(lotWafer.getWaferID(),
                                                        ocapLotWafer)){
                                                    lotWafer.setProcessJobExecFlag(true);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void ocapHoldActionAfterSPCCheckByPostTaskReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        //step1 - check lotID exist
        if (log.isDebugEnabled()) {
            log.debug("step1 - check lotID exist");
        }
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(null == aLot, retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(lotID));
        //step2 - check and get lot OcapInfo
        if (log.isDebugEnabled()) {
            log.debug("step2 - check and get lot OcapInfo");
        }
        boolean handleCompleteFlag = false;
        String ocapType = BizConstant.EMPTY;
        Infos.OcapInfo ocapLotInfo = this.ocapInformationGetByLotID(lotID);
        if (null != ocapLotInfo && CimStringUtils.isNotEmpty(ocapLotInfo.getOcapNo())) {
            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
            lotHoldReq.setHoldType(BizConstant.HOLDTYPE_OCAPHOLD);
            lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            lotHoldReq.setRouteID(ObjectIdentifier.build(aLot.getMainProcessDefinition().getIdentifier(),
                    aLot.getMainProcessDefinition().getPrimaryKey()));
            lotHoldReq.setOperationNumber(aLot.getOperationNumber());
            if (CimBooleanUtils.isFalse(ocapLotInfo.getAddMeasureFlag())
                    && CimBooleanUtils.isTrue(ocapLotInfo.getReMeasureFlag())) {
                //step3 - tigger ocap remeasure hold
                if (log.isDebugEnabled()) {
                    log.debug("step3 - ocap remeasure trigger need onhold the lot");
                }
                lotHoldReq.setHoldReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.OCAP_RE_MEASURE_HOLD_LOT));
                if (log.isDebugEnabled()) {
                    log.debug("OCAP remeasure need call ocap reach the note");
                }
                ocapType = BizConstant.OCAP_RE_MEASURE;
                handleCompleteFlag = true;
            } else if (CimBooleanUtils.isTrue(ocapLotInfo.getAddMeasureFlag())) {
                //step3 - tigger ocap addmeasure hold
                if (log.isDebugEnabled()) {
                    log.debug("step3 - ocap addmeasure trigger need onhold the lot");
                }
                lotHoldReq.setHoldReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.OCAP_ADD_MEASURE_HOLD_LOT));
                if (log.isDebugEnabled()) {
                    log.debug("ocap addmeasure need call ocap reach the note");
                }
                ocapType = BizConstant.OCAP_ADD_MEASURE;
                handleCompleteFlag = true;
            } else {
                //step3 - first ocap trigger ocap hold
                if (log.isDebugEnabled()) {
                    log.debug("step3 - first ocap trigger ocap hold");
                }
                lotHoldReq.setHoldReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.OCAP_HOLD_LOT));
            }
            lotMethod.lotHold(objCommon, aLot.getLotID(), Collections.singletonList(lotHoldReq));
        } else {
            if (log.isDebugEnabled()) {
                log.debug("lot don't exist ocapInfo, do nothing");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("handleCompleteFlag: {}", handleCompleteFlag);
            log.debug("ocapType: {}", ocapType);
        }
        if (handleCompleteFlag && CimStringUtils.isNotEmpty(ocapType)) {
            if (log.isDebugEnabled()) {
                log.debug("step4 - need call ocap to complete the note");
            }
            Inputs.OcapInput ocapInput = new Inputs.OcapInput();
            ocapInput.setCaseNo(ocapLotInfo.getOcapNo());
            ocapInput.setReqUser(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
            ocapInput.setReqSystem(BizConstant.SP_SUBSYSTEMID_MM);
            Map<String, String> map = new HashMap<>();
            map.put(BizConstant.OCAP_TYPE_KEY, ocapType);
            ocapInput.setParams(map);
            ocapInput.setRemakr(new StringBuilder(ocapType).
                    append(BizConstant.OCAP_COMPLETE).toString());
            try {
                ocapRemoteManager.tcHandleCompleteMessageReq(ocapInput);
            } catch (CimIntegrationException e) {
                Validations.check(true, new OmCode((int) e.getCode(), e.getMessage()));
            }
        }
    }
}
