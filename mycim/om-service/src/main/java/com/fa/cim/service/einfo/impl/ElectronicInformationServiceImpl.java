package com.fa.cim.service.einfo.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.method.*;
import com.fa.cim.service.einfo.IElectronicInformationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * description:
 * <p>ElectronicInformationService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/8/008   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/8/008 16:24
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class ElectronicInformationServiceImpl implements IElectronicInformationService {

    @Autowired
    private IFactoryNoteMethod factoryNoteMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IEquipmentAreaMethod equipmentAreaMethod;

    @Override
    public void sxEboardInfoSetReq(Infos.ObjCommon objCommon, String noticeTitle, String noticeDescription) {
        factoryNoteMethod.factoryNoteMake(objCommon, noticeTitle, noticeDescription);
    }

    @Override
    public ObjectIdentifier sxLotMemoAddReq(Infos.ObjCommon objCommon, Params.LotMemoAddReqParams params) {
        ObjectIdentifier lotID = params.getLotID();

        //-----------------------------------------------------------
        // Check lot interFabXferState
        //-----------------------------------------------------------
        String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
        Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, interFabXferState), retCodeConfig.getInterfabInvalidLotXferstateForReq());

        /*-----------------------------------------------------------*/
        /*   Check PosLot, put PosLotNote                            */
        /*-----------------------------------------------------------*/
        String lotNoteTitle = params.getLotNoteTitle();
        String lotNoteDescription = params.getLotNoteDescription();
        Validations.check(CimObjectUtils.isEmpty(lotNoteTitle) || CimObjectUtils.isEmpty(lotNoteDescription), retCodeConfig.getInvalidInputParam());
        ObjectIdentifier lotNoteMakeLotID = lotMethod.lotNoteMake(objCommon, lotID, lotNoteTitle, lotNoteDescription);

        /*------------------------------------------------------------------------*/
        /*   Make Event Record                                                    */
        /*------------------------------------------------------------------------*/
        // objectNoteEvent_Make
        Inputs.ObjectNoteEventMakeParams objectNoteEventMakeParams = new Inputs.ObjectNoteEventMakeParams();
        objectNoteEventMakeParams.setObjectID(lotID);
        objectNoteEventMakeParams.setNoteType(BizConstant.SP_NOTETYPE_LOTNOTE);
        objectNoteEventMakeParams.setAction(BizConstant.SP_NOTEACTION_CREATE);
        objectNoteEventMakeParams.setNoteTitle(lotNoteTitle);
        objectNoteEventMakeParams.setNoteContents(lotNoteDescription);
        objectNoteEventMakeParams.setOwnerID(objCommon.getUser().getUserID());
        objectNoteEventMakeParams.setTransactionID(objCommon.getTransactionID());
        objectNoteEventMakeParams.setClaimMemo(params.getClaimMemo());
        eventMethod.objectNoteEventMake(objCommon, objectNoteEventMakeParams);

        return lotNoteMakeLotID;
    }

    @Override
    public ObjectIdentifier sxLotOperationNoteInfoRegisterReq(Params.LotOperationNoteInfoRegisterReqParams params, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxLotOperationNoteInfoRegisterReq()");

        //Check PosLot, get PosProcessOperation and PosLotNote ;

        String objLotInterFabTransferStateGetOut = lotMethod.lotInterFabXferStateGet(objCommon, params.getLotID());

        if (CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, objLotInterFabTransferStateGetOut)) {
            log.info(" #### lot InterFabXfer State == 'Transferring'. This request is rejected. ");
            throw new ServiceException(new OmCode(retCodeConfig.getInterfabInvalidLotXferstateForReq(), params.getLotID().getValue(), objLotInterFabTransferStateGetOut));
        }
        //Check PosLot, put PosLotOperationNote;
        ObjectIdentifier newLotOpeNote = lotMethod.lotOperationNoteMake(objCommon, params);

        // Make Event Record
        Inputs.ObjectNoteEventMakeParams objectNoteEventMakeParams = new Inputs.ObjectNoteEventMakeParams();
        objectNoteEventMakeParams.setObjectID(params.getLotID());
        objectNoteEventMakeParams.setNoteType(BizConstant.SP_NOTETYPE_LOTOPENOTE);
        objectNoteEventMakeParams.setAction(BizConstant.SP_NOTEACTION_CREATE);
        objectNoteEventMakeParams.setRouteID(params.getRouteID());
        objectNoteEventMakeParams.setOperationID(params.getOperationID());
        objectNoteEventMakeParams.setNoteTitle(params.getLotOperationNoteTitle());
        objectNoteEventMakeParams.setNoteContents(params.getLotOperationNoteDescription());
        objectNoteEventMakeParams.setOwnerID(objCommon.getUser().getUserID());
        objectNoteEventMakeParams.setTransactionID(objCommon.getTransactionID());
        objectNoteEventMakeParams.setClaimMemo(params.getClaimMemo());

        eventMethod.objectNoteEventMake(objCommon, objectNoteEventMakeParams);

        log.info("【Method Exit】sxLotOperationNoteInfoRegisterReq()");

        return newLotOpeNote;
    }

    @Override
    public void sxEqpBoardWorkZoneBindingReq(Infos.ObjCommon objCommon, Params.EqpBoardWorkZoneBindingParams eqpBoardWorkZoneBindingParams) {
        //step1 check basic params
        log.info("sxEqpBoardWorkZoneBindingReq()->info: check params");
        String category = eqpBoardWorkZoneBindingParams.getCategory();
        Validations.check(CimStringUtils.isEmpty(category), retCodeConfig.getInvalidParameter());

        List<ObjectIdentifier> eqpIds = eqpBoardWorkZoneBindingParams.getEqpIds();
        Validations.check(CollectionUtils.isEmpty(eqpIds), retCodeConfig.getInvalidParameter());

        Validations.check(!CimStringUtils.equals(category, BizConstant.EQP_WORK_ZONE_CATEGORY) &&
                        !CimStringUtils.equals(category, BizConstant.EQP_WORK_USER_CATEGORY)
                , retCodeConfig.getInvalidParameter());

        Validations.check(CimStringUtils.equals(category, BizConstant.EQP_WORK_ZONE_CATEGORY) &&
                CimStringUtils.isEmpty(eqpBoardWorkZoneBindingParams.getZone()), retCodeConfig.getInvalidParameter());

        // step2 conversion params
        log.info("sxEqpBoardWorkZoneBindingReq()->info: conversion params");
        if (CimStringUtils.equals(category, BizConstant.EQP_WORK_USER_CATEGORY)) {
            eqpBoardWorkZoneBindingParams.setZone(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        }

        //step3 method : insert eqp area
        log.info("sxEqpBoardWorkZoneBindingReq()->info: call eqp area for update");
        equipmentAreaMethod.eqpBoardWorkZoneBinding(objCommon, eqpBoardWorkZoneBindingParams);

    }

    @Override
    public void sxEqpAreaCancelReq(Infos.ObjCommon objCommon, Params.EqpAreaCancelParams eqpAreaCancelParams) {
        //step1 check basic params
        log.info("sxEqpAreaCancelReq()->info: check params");
        // category
        String category = eqpAreaCancelParams.getCategory();
        Validations.check(CimStringUtils.isEmpty(category), retCodeConfig.getInvalidParameter());

        // zone type
        Validations.check(!CimStringUtils.equals(category, BizConstant.EQP_WORK_ZONE_CATEGORY) &&
                        !CimStringUtils.equals(category, BizConstant.EQP_WORK_USER_CATEGORY)
                , retCodeConfig.getInvalidParameter());

        // category is work zone && zone can`t be null
        Validations.check(CimStringUtils.equals(category, BizConstant.EQP_WORK_ZONE_CATEGORY) &&
                CimStringUtils.isEmpty(eqpAreaCancelParams.getZone()), retCodeConfig.getInvalidParameter());

        // clear all
        Boolean clearAll = eqpAreaCancelParams.getClearAll();
        Validations.check(Objects.isNull(clearAll), retCodeConfig.getInvalidParameter());

        // clear all is false && eqp id can`t be null
        Validations.check(!clearAll && ObjectIdentifier.isEmpty(eqpAreaCancelParams.getEqpId()), retCodeConfig.getInvalidParameter());

        // step2 conversion params
        log.info("sxEqpAreaCancelReq()->info: conversion params");
        if (CimStringUtils.equals(category, BizConstant.EQP_WORK_USER_CATEGORY)) {
            eqpAreaCancelParams.setZone(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        }

        //step3 eqpAreaCancel
        log.info("sxEqpAreaCancelReq()->info: call eqpAreaCancel");
        equipmentAreaMethod.eqpAreaCancel(objCommon, eqpAreaCancelParams);
    }

    @Override
    public void sxEqpAreaMoveReq(Infos.ObjCommon objCommon, Params.EqpAreaMoveParams eqpAreaMoveParams) {
        //step1 check basic params
        log.info("sxEqpAreaMoveReq()->info: check params");

        String category = eqpAreaMoveParams.getCategory();
        Validations.check(CimStringUtils.isEmpty(category), retCodeConfig.getInvalidParameter());

        // zone type
        Validations.check(!CimStringUtils.equals(category, BizConstant.EQP_WORK_ZONE_CATEGORY) &&
                        !CimStringUtils.equals(category, BizConstant.EQP_WORK_USER_CATEGORY)
                , retCodeConfig.getInvalidParameter());

        // category is work zone && zone can`t be null
        Validations.check(CimStringUtils.equals(category, BizConstant.EQP_WORK_ZONE_CATEGORY) &&
                CimStringUtils.isEmpty(eqpAreaMoveParams.getZone()), retCodeConfig.getInvalidParameter());

        //eqp id`s can`t be null
        List<ObjectIdentifier> eqpIds = eqpAreaMoveParams.getEqpIds();
        if (CollectionUtils.isEmpty(eqpIds)) {
            if (log.isDebugEnabled()) {
                log.debug("sxEqpAreaMoveReq()-> debug : equipment ids is empty!");
            }
            return;
        }

        // step2 conversion params
        log.info("sxEqpAreaMoveReq()->info: conversion params");
        if (CimStringUtils.equals(category, BizConstant.EQP_WORK_USER_CATEGORY)) {
            eqpAreaMoveParams.setZone(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        }

        //step3 call eqpAreaMove
        log.info("sxEqpAreaMoveReq()->info: call eqpAreaMove");
        equipmentAreaMethod.eqpAreaMove(objCommon, eqpAreaMoveParams);
    }
}
