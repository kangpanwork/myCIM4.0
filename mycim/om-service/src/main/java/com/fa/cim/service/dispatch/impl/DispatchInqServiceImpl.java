package com.fa.cim.service.dispatch.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.method.impl.equipment.EquipmentWhatNextMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.dispatch.IDispatchInqService;
import com.fa.cim.service.system.ISystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 18:37
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class DispatchInqServiceImpl implements IDispatchInqService {

    @Autowired
    private IAutoDispatchControlMethod autoDispatchControlMethod;

    @Autowired
    private IBankMethod bankMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IRouteMethod routeMethod;

    @Autowired
    private IOperationMethod operationMethod;

    @Autowired
    private IVirtualOperationMethod virtualOperationMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IStartCassetteMethod startCassetteMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private ISystemService systemService;

    @Autowired
    private IAPCMethod apcMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private EquipmentWhatNextMethod equipmentWhatNextMethod;

    @Override
    public Results.WhatNextLotListResult sxWhatNextLotListInfo(Infos.ObjCommon objCommon, Params.WhatNextLotListParams whatNextLotListParams) {
        Inputs.ObjEquipmentLotsWhatNextDRIn in = new Inputs.ObjEquipmentLotsWhatNextDRIn();
        in.setEquipmentID(whatNextLotListParams.getEquipmentID());
        if (null != whatNextLotListParams.getSelectCriteria()) {
            in.setSelectCriteria(whatNextLotListParams.getSelectCriteria().getValue());
        }
        return equipmentWhatNextMethod.equipmentLotsWhatNextDR(in, objCommon);
    }

    @Override
    public Results.AutoDispatchConfigInqResult sxAutoDispatchConfigInq(Infos.ObjCommon objCommon, Params.AutoDispatchConfigInqParams autoDispatchConfigInqParams) {
        Results.AutoDispatchConfigInqResult autoDispatchConfigInqResult = new Results.AutoDispatchConfigInqResult();
        // Step1 - autoDispatchControl_info_GetDR
        Inputs.ObjAutoDispatchControlInfoGetDRIn objAutoDispatchControlInfoGetDRIn = new Inputs.ObjAutoDispatchControlInfoGetDRIn();
        objAutoDispatchControlInfoGetDRIn.setLotID(autoDispatchConfigInqParams.getLotID());
        objAutoDispatchControlInfoGetDRIn.setRouteID(autoDispatchConfigInqParams.getRouteID());
        objAutoDispatchControlInfoGetDRIn.setOperationNumber(autoDispatchConfigInqParams.getOperationNumber());
        List<Infos.LotAutoDispatchControlInfo> autoDispatchControlInfo = autoDispatchControlMethod.autoDispatchControlInfoGetDR(objCommon, objAutoDispatchControlInfoGetDRIn);
        autoDispatchConfigInqResult.setLotAutoDispatchControlInfoList(autoDispatchControlInfo);
        return autoDispatchConfigInqResult;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params    -
     * @return com.fa.cim.dto.RetCode<Results.WhatNextNPWStandbyLotInqResult>
     * @author Sun
     * @date 10/29/2018 10:33 AM
     */
    @Override
    public List<Infos.WhatNextStandbyAttributes> sxWhatNextNPWStandbyLotInq(Infos.ObjCommon objCommon, Params.WhatNextNPWStandbyLotInqParams params) {
        RetCode<Results.WhatNextNPWStandbyLotInqResult> result = new RetCode<>();
        log.info("【Method Entry】sxWhatNextNPWStandbyLotInq()");

        //【Step-1】Set Inquired Data
        return bankMethod.bankGetLotListByQueryDR(objCommon, params.getBankID());
    }

    @Override
    public Results.VirtualOperationWipListInqResult sxVirtualOperationWipListInq(Infos.ObjCommon objCommon, Params.VirtualOperationWipListInqParams params) {
        Results.VirtualOperationWipListInqResult virtualOperationWipListInqResult = new Results.VirtualOperationWipListInqResult();
        ObjectIdentifier routeID = params.getRouteID();
        String operationNumber = params.getOperationNumber();
        ObjectIdentifier operationID = params.getOperationID();
        String selectCriteria = params.getSelectCriteria();
        log.info("【step1】check in-paramter");
        Validations.check((!ObjectIdentifier.isEmptyWithValue(routeID) && CimStringUtils.isEmpty(operationNumber))
                        || (ObjectIdentifier.isEmptyWithValue(routeID) && !CimStringUtils.isEmpty(operationNumber))
                        || (ObjectIdentifier.isEmptyWithValue(routeID) && CimStringUtils.isEmpty(operationNumber) && ObjectIdentifier.isEmptyWithValue(operationID)),
                retCodeConfig.getInvalidInputParam());

        Validations.check(!CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_ALL, selectCriteria)
                        && !CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED, selectCriteria)
                        && !CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_HOLD, selectCriteria),
                retCodeConfig.getInvalidInputParam());

        //check PDType for virtual operaion
        log.debug("check PDType for virtual operaion");
        ObjectIdentifier checkOperationID = null;
        if (!ObjectIdentifier.isEmptyWithValue(routeID) && !CimStringUtils.isEmpty(operationNumber)) {
            //【step2】call getRouteOperationOperationID
            log.info("Get OperationID from RouteID and OperaionNumber");
            checkOperationID = routeMethod.routeOperationOperationIDGet(objCommon, routeID, operationNumber);
        } else {
            checkOperationID = operationID;
        }
        //【step3】get operation process definition type
        log.debug("【step3】get operation process definition type");
        Outputs.ObjOperationPdTypeGetOut objOperationPdTypeGetOut = operationMethod.operationPdTypeGet(objCommon, checkOperationID);
        virtualOperationWipListInqResult.setOperationID(checkOperationID);
        virtualOperationWipListInqResult.setOperationName(objOperationPdTypeGetOut.getOperationName());
        Validations.check(!CimStringUtils.equals(BizConstant.SP_OPEPDTYPE_VIRTUAL, objOperationPdTypeGetOut.getPdType()),
                new OmCode(retCodeConfig.getInvalidPDType(), objOperationPdTypeGetOut.getPdType(), ObjectIdentifier.fetchValue(checkOperationID)));

        //【step4】get lot list for virtual operation
        log.debug("【step4】get lot list for virtual operation");
        Inputs.ObjVirtualOperationLotsGetDRIn in = new Inputs.ObjVirtualOperationLotsGetDRIn();
        in.setRouteID(routeID);
        in.setOperationNumber(operationNumber);
        in.setOperationID(operationID);
        in.setSelectCriteria(selectCriteria);
        List<Infos.VirtualOperationLot> virtualOperationLotList = virtualOperationMethod.virtualOperationLotsGetDR(objCommon, in);
        virtualOperationWipListInqResult.setVirtualOperationLotList(virtualOperationLotList);
        //【step5】get inprocessing lot list for virtual operation
        log.debug("【step5】get inprocessing lot list for virtual operation");
        if (!CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_HOLD, selectCriteria)) {
            Inputs.ObjVirtualOperationInprocessingLotsGetDRIn inprocessingLotsGetDRIn = new Inputs.ObjVirtualOperationInprocessingLotsGetDRIn();
            inprocessingLotsGetDRIn.setRouteID(routeID);
            inprocessingLotsGetDRIn.setOperationNumber(operationNumber);
            inprocessingLotsGetDRIn.setOperationID(operationID);
            List<Infos.VirtualOperationInprocessingLot> virtualOperationInprocessingLotList = virtualOperationMethod.virtualOperationInprocessingLotsGetDR(objCommon, inprocessingLotsGetDRIn);
            virtualOperationWipListInqResult.setVirtualOperationInprocessingLotList(virtualOperationInprocessingLotList);
        }
        return virtualOperationWipListInqResult;
    }

    @Override
    public Results.LotsMoveInReserveInfoInqResult sxLotsMoveInReserveInfoForIBInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassettes) {
        RetCode<Results.LotsMoveInReserveInfoInqResult> result = new RetCode<>();

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Check Process                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("Check Transaction ID and eqp Category combination.");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        //----------------------------------------
        // Change processJobExecFlag to True.
        //----------------------------------------
        //-------------------------------------------------------
        // When RC_SMPL_SLOTMAP_CONFLICT_WARN is returned,
        // return RC_SMPL_SLOTMAP_CONFLICT_WARN at the last of TX process.
        //-------------------------------------------------------
        log.info("Set processJobExecFlag based on wafer sampling setting.");
        boolean slotmapConflictWarnFlag = false;
        Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteOut = null;
        try {
            startCassetteOut = startCassetteMethod.startCassetteProcessJobExecFlagSet(objCommon, startCassettes, equipmentID);

        } catch (ServiceException ex) {
            OmCode cassetteOut = new OmCode(ex.getCode(), ex.getMessage());
            if (!cassetteOut.equals(retCodeConfig.getSmplSlotmapConflictWarn())) {
                throw ex;
            }
            slotmapConflictWarnFlag = true;
        }


        //-------------------------------------------------------
        // create return message or e-mail text.
        //-------------------------------------------------------
        // txAlertMessageRpt
        StringBuilder smplMessage = new StringBuilder();
        if (!CimObjectUtils.isEmpty(startCassetteOut)) {
            List<Infos.ObjSamplingMessageAttribute> samplingMessageList = startCassetteOut.getSamplingMessage();
            if (!CimObjectUtils.isEmpty(samplingMessageList)) {
                for (Infos.ObjSamplingMessageAttribute objSamplingMessageAttribute : samplingMessageList) {
                    if (objSamplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_WARN_MAIL) {
                        if (smplMessage.length() > 0) {
                            smplMessage.append(",");
                        }
                        smplMessage.append(objSamplingMessageAttribute.getMessageText());
                    } else if (objSamplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_IGNORED_MAIL) {
                        Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                        alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                        alertMessageRptParams.setSystemMessageText(objSamplingMessageAttribute.getMessageText());
                        alertMessageRptParams.setNotifyFlag(true);
                        alertMessageRptParams.setLotID(objSamplingMessageAttribute.getLotID());
                        alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                        systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                    }
                }
            }
        }

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Main Process                                                        */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*-------------------------------------------*/
        /*   Get Information for Start Reservation   */
        /*-------------------------------------------*/
        log.info("Get Information for Start Reservation");
        Outputs.ObjProcessStartReserveInformationGetBaseInfoForClientOut clientOut =
                processMethod.processStartReserveInformationGetBaseInfoForClient(objCommon, equipmentID, startCassettes);
        Results.LotsMoveInReserveInfoInqResult resultObj = new Results.LotsMoveInReserveInfoInqResult();
        resultObj.setEquipmentID(clientOut.getEquipmentID());
        resultObj.setStrStartCassette(clientOut.getStartCassetteList());
        String fpcAdaptationFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getValue();
        if (CimStringUtils.equals("1", fpcAdaptationFlag)) {
            List<Infos.StartCassette> exchangeFPCStartCassetteInfo = fpcMethod.fpcStartCassetteInfoExchange(objCommon, BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO
                    , resultObj.getEquipmentID(), resultObj.getStrStartCassette());
            resultObj.setStrStartCassette(exchangeFPCStartCassetteInfo);
        } else {
            log.info("DOC Adopt Flag is OFF.");
        }

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   APC I/F                                                             */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        // APC I/F
        boolean apcInterfaceFlag = apcMethod.apcInterfaceFlagCheck(objCommon, resultObj.getStrStartCassette());
        if (apcInterfaceFlag) {
            /*--------------------------------------------------*/
            /*                                                  */
            /*   Get Equipment's Start Reserved ControlJob ID   */
            /*                                                  */
            /*--------------------------------------------------*/
            List<Infos.StartReservedControlJobInfo> startReservedControlJobInfos = equipmentMethod.equipmentReservedControlJobIDGet(objCommon, equipmentID);
            /*-------------------------------------------------*/
            /*                                                 */
            /*   Get Cassette's Start Reserved ControlJob ID   */
            /*                                                 */
            /*-------------------------------------------------*/
            ObjectIdentifier saveControlJobID = null;
            List<Infos.StartCassette> tmpStartCassette = resultObj.getStrStartCassette();
            int nIMax = CimArrayUtils.getSize(tmpStartCassette);
            for (int i = 0; i < nIMax; i++) {
                ObjectIdentifier controlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, tmpStartCassette.get(i).getCassetteID());
                boolean findFlag = false;
                int nJMax = CimArrayUtils.getSize(startReservedControlJobInfos);
                int j;
                for (j = 0; j < nJMax; j++) {
                    if (ObjectIdentifier.equalsWithValue(startReservedControlJobInfos.get(j).getControlJobID(), controlJobID)) {
                        findFlag = true;
                        break;
                    }
                }
                if (!ObjectIdentifier.isEmptyWithValue(controlJobID)) {
                    saveControlJobID = controlJobID;
                    Validations.check(!findFlag, retCodeConfig.getNotResvedPortgrp());
                } else {
                    if (findFlag) {
                        throw new ServiceException(new OmCode(retCodeConfig.getAlreadyReservedPortGroup(),
                                startReservedControlJobInfos.get(j).getPortGroupID(), ObjectIdentifier.fetchValue(startReservedControlJobInfos.get(j).getControlJobID())));
                    }
                }
            }
            /*---------------------*/  // This implementation is not good from
            /*   Get PortGroupID   */  // responce point of view. In the near
            /*---------------------*/  // future, this logic must be replaced.
            ObjectIdentifier savePortGroupID = new ObjectIdentifier();
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
            List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
            int EPSLength = CimArrayUtils.getSize(eqpPortStatuses);
            for (int i = 0; i < EPSLength; i++) {
                if (ObjectIdentifier.equalsWithValue(eqpPortStatuses.get(i).getLoadedCassetteID(), tmpStartCassette.get(0).getCassetteID()) && saveControlJobID != null) {
                    saveControlJobID.setValue(eqpPortInfo.getEqpPortStatuses().get(i).getPortGroup());
                    break;
                }
            }
            /*-------------------------------------------------*/
            /*   Get from APCInterface AdjustRecipeParameter   */
            /*-------------------------------------------------*/
            List<Infos.StartCassette> apcStartCassettes = apcMethod.APCMgrSendRecipeParamInq(objCommon, equipmentID, savePortGroupID.getValue(), saveControlJobID, tmpStartCassette,BizConstant.SP_OPERATION_STARTRESERVATION);
            resultObj.setStrStartCassette(apcStartCassettes);
        }

        /*-----------------------*/
        /*   Set out structure   */
        /*-----------------------*/
        Validations.check(slotmapConflictWarnFlag, new OmCode(retCodeConfig.getSmplSlotmapConflictWarn(), smplMessage.toString()));
        return resultObj;
    }

    @Override
    public Results.EqpFullAutoConfigListInqResult sxEqpFullAutoConfigListInq(Infos.ObjCommon objCommon, Params.EqpFullAutoConfigListInqInParm eqpFullAutoConfigListInqInParm) {

        Results.EqpFullAutoConfigListInqResult eqpFullAutoConfigListInqResult = new Results.EqpFullAutoConfigListInqResult();

        //step1 - Get OSEQAUTODISPATCHSET
        List<ObjectIdentifier> eqpID = eqpFullAutoConfigListInqInParm.getEquipmentIDs();

        /*------------*/
        /*   Return   */
        /*------------*/
        List<Infos.EqpAuto3SettingInfo> eqpAuto3SettingInfos = equipmentMethod.equipmentAuto3DispatchSettingListGetDR(objCommon, eqpID);

        eqpFullAutoConfigListInqResult.setStrEqpAuto3SettingInfoSeq(eqpAuto3SettingInfos);

        return eqpFullAutoConfigListInqResult;
    }
}
