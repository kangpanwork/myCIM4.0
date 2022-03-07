package com.fa.cim.service.lot.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.method.impl.SorterNewMethod;
import com.fa.cim.newcore.dto.product.DispatchReadinessState;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.system.ISystemService;
import com.fa.cim.sorter.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8          ********            ho                create file
 *
 * @author: ho
 * @date: 2020/9/8 17:36
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@OmService
@Slf4j
public class LotInqServiceImpl implements ILotInqService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private ILotFamilyMethod lotFamilyMethod;

    @Autowired
    private IAutoDispatchControlMethod autoDispatchControlMethod;
    @Autowired
    private IExperimentalMethod experimentalMethod;
    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private SorterNewMethod sorterMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IProductMethod productMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private ICategoryMethod categoryMethod;

    @Autowired
    private IAPCMethod apcMethod;

    @Autowired
    private IStartCassetteMethod startCassetteMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ISystemService systemService;

    public List<Infos.LotHoldListAttributes> sxHoldLotListInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        Validations.check(ObjectIdentifier.isEmpty(lotID), retCodeConfig.getNotFoundLot());
        return lotMethod.lotFillInTxTRQ005DR(objCommon, lotID);
    }

    public List<Infos.AliasWaferName> sxWaferAliasInfoInq(Infos.ObjCommon objCommon, Params.WaferAliasInfoInqParams params) {

        List<ObjectIdentifier> waferIDSeq = params.getWaferIDSeq();
        log.trace("!org.springframework.util.ObjectUtils.isEmpty(waferIDSeq) : {}",!org.springframework.util.ObjectUtils.isEmpty(waferIDSeq));
        if (!org.springframework.util.ObjectUtils.isEmpty(waferIDSeq)) {
            return waferMethod.waferAliasNameGetDR(objCommon, params.getWaferIDSeq());
        }
        return null;
    }

    public List<Infos.WaferListInLotFamilyInfo> sxWaferListInLotFamilyInq(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID) {
        Validations.check(CimStringUtils.isEmpty(lotFamilyID.getValue()), retCodeConfig.getInvalidParameter(), objCommon.getTransactionID());

        //Step1 - lot_wafersStatusList_GetDR
        log.debug("get lot wafer status list");
        return lotMethod.lotWafersStatusListGetDR(objCommon, lotFamilyID);
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param multiPathListInqParams
     * @param objCommon
     * @return com.fa.cim.dto.result.MultiPathListInqResult
     * @author Sun
     * @date 2018/10/10
     */
    public List<Infos.ConnectedRouteList> sxMultiPathListInq(Params.MultiPathListInqParams multiPathListInqParams, Infos.ObjCommon objCommon) {
        log.debug("【Method Entry】txMultiPathListInq()");
        //Step-1:call process_connectedRouteList;
        List<Infos.ConnectedRouteList> connectedRouteListList = processMethod.processConnectedRouteList(objCommon, multiPathListInqParams.getRouteType(), multiPathListInqParams.getLotID());
        log.debug("【Method Exit】txMultiPathListInq()");
        return connectedRouteListList;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params    -
     * @return com.fa.cim.dto.RetCode<Results.DynamicPathListInqResult>
     * @author Bear
     * @date 2018/11/6 16:34
     */
    public Page<Infos.DynamicRouteList> sxDynamicPathListInq(Infos.ObjCommon objCommon, Params.DynamicPathListInqParams params) {
        log.debug("get process dynamoc route list");
        return processMethod.processDynamicRouteListDR(objCommon, params);
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotFamilyInqParams
     * @param objCommon
     * @return com.fa.cim.dto.result.LotFamilyInqResult
     * @author Sun
     * @date 2018/10/10
     */

    public Results.LotFamilyInqResult sxLotFamilyInq(Params.LotFamilyInqParams lotFamilyInqParams, Infos.ObjCommon objCommon) {
        log.debug("【Method Entry】sxLotFamilyInq");

        //Step-1:call lotFamily_FillInTxTRQ007DR;
        return lotFamilyMethod.lotFamilyFillInTxTRQ007DR(objCommon, lotFamilyInqParams.getLotID());
    }

    public Results.LotFuturePctrlDetailInfoInqResult sxLotFuturePctrlDetailInfoInq(Infos.ObjCommon objCommon, Params.FutureActionDetailInfoInqParams futureActionDetailInfoInqParams) {

        Results.LotFuturePctrlDetailInfoInqResult out = new Results.LotFuturePctrlDetailInfoInqResult();
        // step1 Get future actions for the operation
        log.debug("[step0] Get future actions for the operation");
        Infos.OperationFutureActionAttributes operationFutureActionAttributes = futureActionDetailInfoInqParams.getOperationFutureActionAttributes();
        List<Infos.HashedInfo> futureActionFlagSeq = operationFutureActionAttributes.getStrFutureActionFlagSeq();
        ObjectIdentifier lotID = futureActionDetailInfoInqParams.getLotID();
        for (Infos.HashedInfo hashedInfo : futureActionFlagSeq) {
            // step1 process_futureQrestTimeInfo_GetDR__180
            log.debug("[step1] process_futureQrestTimeInfo_GetDR__180");
            String hashKey = hashedInfo.getHashKey();
            String hashData = hashedInfo.getHashData();
            log.trace("hashKey : {}",hashKey);
            if (CimStringUtils.equals(BizConstant.SP_HASHKEY_FUTUREQTIMEFLAG, hashKey)) {
                log.trace("hashData : {}",hashData);
                if (CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData)) {
                    Outputs.ObjProcessFutureQrestTimeInfoGetDROut futureQrestTimeInfoGetDOut = processMethod.processFutureQrestTimeInfoGetDR(objCommon, futureActionDetailInfoInqParams);
                    out.setFutureQtimeInfoList(futureQrestTimeInfoGetDOut.getFutureQtimeInfoList());
                }
            }
            // step2 lot_futureHoldListbyKeyDR
            log.debug("[step2] lot_futureHoldListbyKeyDR");
            log.trace("StringUtils.equals(BizConstant.SP_HASHKEY_FUTUREHOLDFLAG, hashKey) : {}", CimStringUtils.equals(BizConstant.SP_HASHKEY_FUTUREHOLDFLAG, hashKey));
            if (CimStringUtils.equals(BizConstant.SP_HASHKEY_FUTUREHOLDFLAG, hashKey)) {
                log.trace("StringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData) : {}", CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData));
                if (CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData)) {
                    Infos.FutureHoldSearchKey futureHoldSearchKey = new Infos.FutureHoldSearchKey();
                    futureHoldSearchKey.setLotID(lotID);
                    futureHoldSearchKey.setRouteID(operationFutureActionAttributes.getRouteID());
                    futureHoldSearchKey.setOperationNumber(operationFutureActionAttributes.getOperationNumber());
                    List<Infos.FutureHoldListAttributes> futureHoldListByKeyGetDOut = lotMethod.lotFutureHoldListbyKeyDR(objCommon, futureHoldSearchKey, 10);

                    out.setFutureHoldListAttributes(futureHoldListByKeyGetDOut);
                }
            }
            // step3 lot_futureReworkList_GetDR
            log.debug("[step3] lot_futureReworkList_GetDR");
            log.trace("StringUtils.equals(BizConstant.SP_HASHKEY_FUTUREREWORKFLAG, hashKey) : {}", CimStringUtils.equals(BizConstant.SP_HASHKEY_FUTUREREWORKFLAG, hashKey));
            if (CimStringUtils.equals(BizConstant.SP_HASHKEY_FUTUREREWORKFLAG, hashKey)) {
                log.trace("StringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData) : {}", CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData));
                if (CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData)) {
                    Outputs.lotFutureReworkListGetDROut futureReworkListGetDROut = lotMethod.lotFutureReworkListGetDR(objCommon, futureActionDetailInfoInqParams);
                    log.trace("futureReworkListGetDROut != null && futureReworkListGetDROut.getFutureReworkDetailInfoList() != null && futureReworkListGetDROut.getFutureReworkDetailInfoList().size() > 0 : {}",
                            futureReworkListGetDROut != null && futureReworkListGetDROut.getFutureReworkDetailInfoList() != null && futureReworkListGetDROut.getFutureReworkDetailInfoList().size() > 0);

                    if (futureReworkListGetDROut != null && futureReworkListGetDROut.getFutureReworkDetailInfoList() != null && futureReworkListGetDROut.getFutureReworkDetailInfoList().size() > 0) {
                        out.setFutureReworkDetailInfoListSeq(futureReworkListGetDROut.getFutureReworkDetailInfoList().get(0).getFutureReworkDetailInfoList());
                    }
                }
            }
            // step4 autoDispatchControl_info_GetDR
            log.debug("[step4] autoDispatchControl_info_GetDR");
            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_HASHKEY_ADCFLAG, hashKey) : {}", CimStringUtils.equals(BizConstant.SP_HASHKEY_ADCFLAG, hashKey));
            if (CimStringUtils.equals(BizConstant.SP_HASHKEY_ADCFLAG, hashKey)) {
                log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_HASHDATA_FLAG_1, hashData) : {}", CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData));
                if (CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData)) {
                    Inputs.ObjAutoDispatchControlInfoGetDRIn in = new Inputs.ObjAutoDispatchControlInfoGetDRIn();
                    in.setLotID(lotID);
                    List<Infos.LotAutoDispatchControlInfo> lotAutoDispatchControlInfos = autoDispatchControlMethod.autoDispatchControlInfoGetDR(objCommon, in);

                    boolean bFound;
                    List<Infos.LotAutoDispatchControlInfo> strLotAutoDispatchControlInfoSeq = new ArrayList<>();
                    for (Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo : lotAutoDispatchControlInfos) {
                        bFound = false;
                        ObjectIdentifier routeID = lotAutoDispatchControlInfo.getRouteID();
                        String operationNumber = lotAutoDispatchControlInfo.getOperationNumber();
                        log.trace("ObjectUtils.equalsWithValue(routeID, BizConstant.SP_ADCSETTING_ASTERISK) && ObjectUtils.equalsWithValue(operationNumber, BizConstant.SP_ADCSETTING_ASTERISK) : {}",
                                ObjectIdentifier.equalsWithValue(routeID, BizConstant.SP_ADCSETTING_ASTERISK) && CimStringUtils.equals(operationNumber, BizConstant.SP_ADCSETTING_ASTERISK));
                        log.trace("!ObjectUtils.equalsWithValue(routeID, BizConstant.SP_ADCSETTING_ASTERISK) && ObjectUtils.equalsWithValue(operationNumber, BizConstant.SP_ADCSETTING_ASTERISK) : {}",
                                !ObjectIdentifier.equalsWithValue(routeID, BizConstant.SP_ADCSETTING_ASTERISK) && CimStringUtils.equals(operationNumber, BizConstant.SP_ADCSETTING_ASTERISK));
                        log.trace("!ObjectUtils.equalsWithValue(routeID, BizConstant.SP_ADCSETTING_ASTERISK) && !ObjectUtils.equalsWithValue(operationNumber, BizConstant.SP_ADCSETTING_ASTERISK) : {}",
                                !ObjectIdentifier.equalsWithValue(routeID, BizConstant.SP_ADCSETTING_ASTERISK) && !CimStringUtils.equals(operationNumber, BizConstant.SP_ADCSETTING_ASTERISK));

                        if (ObjectIdentifier.equalsWithValue(routeID, BizConstant.SP_ADCSETTING_ASTERISK) && CimStringUtils.equals(operationNumber, BizConstant.SP_ADCSETTING_ASTERISK)) {
                            log.debug("routeID=Any operationNumber=Any");
                            log.debug("Match record found. {} {}", routeID, operationNumber);
                            bFound = true;
                        } else if (!ObjectIdentifier.equalsWithValue(routeID, BizConstant.SP_ADCSETTING_ASTERISK) && CimStringUtils.equals(operationNumber, BizConstant.SP_ADCSETTING_ASTERISK)) {
                            log.debug("Check existence of routeID");
                            log.trace("ObjectUtils.equalsWithValue(operationFutureActionAttributes.getRouteID(), routeID) : {}",
                                    ObjectIdentifier.equalsWithValue(operationFutureActionAttributes.getRouteID(), routeID));

                            if (ObjectIdentifier.equalsWithValue(operationFutureActionAttributes.getRouteID(), routeID)) {
                                log.debug("Match record found. {} {}", routeID, operationNumber);
                                bFound = true;
                            }
                        } else if (!ObjectIdentifier.equalsWithValue(routeID, BizConstant.SP_ADCSETTING_ASTERISK) && !CimStringUtils.equals(operationNumber, BizConstant.SP_ADCSETTING_ASTERISK)) {
                            log.debug("Check existence of routeID and operationNumber");
                            log.trace("ObjectUtils.equalsWithValue(operationFutureActionAttributes.getRouteID(), routeID)\n" +
                                    "                                    && ObjectUtils.equalsWithValue(operationFutureActionAttributes.getOperationNumber(), operationNumber) : {}",
                                    ObjectIdentifier.equalsWithValue(operationFutureActionAttributes.getRouteID(), routeID)
                                            && CimStringUtils.equals(operationFutureActionAttributes.getOperationNumber(), operationNumber));

                            if (ObjectIdentifier.equalsWithValue(operationFutureActionAttributes.getRouteID(), routeID)
                                    && CimStringUtils.equals(operationFutureActionAttributes.getOperationNumber(), operationNumber)) {
                                log.debug("Match record found. {} {}", routeID, operationNumber);
                                bFound = true;
                            }
                        }

                        log.trace("bFound : {}",bFound);
                        if (bFound) {
                            strLotAutoDispatchControlInfoSeq.add(lotAutoDispatchControlInfo);
                        }
                    }
                    out.setLotAutoDispatchControlInfoSeq(strLotAutoDispatchControlInfoSeq);
                }
            }

            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_HASHKEY_PSMFLAG, hashKey) : {}", CimStringUtils.equals(BizConstant.SP_HASHKEY_PSMFLAG, hashKey));
            if (CimStringUtils.equals(BizConstant.SP_HASHKEY_PSMFLAG, hashKey)) {
                log.debug("hashKey == PSMFlag");
                log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_HASHDATA_FLAG_1, hashData) : {}", CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData));
                if (CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData)) {
                    log.debug("hashData == 1");
                    //------------------------------------------------------------------------
                    // Get lot family
                    //------------------------------------------------------------------------
                    log.debug("Get lot family");
                    String lotFamily;
                    Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
                    lotInfoInqFlag.setLotBasicInfoFlag(true);
                    Infos.LotInfo lotInfo = lotMethod.lotDBInfoGetDR(objCommon, lotInfoInqFlag, lotID);
                    lotFamily = ObjectIdentifier.fetchValue(lotInfo.getLotBasicInfo().getFamilyLotID());
                    List<Infos.ExperimentalLotInfo> experimentalLotInfos = experimentalMethod.experimentalLotListGetDR(objCommon, lotFamily,
                            ObjectIdentifier.fetchValue(operationFutureActionAttributes.getRouteID()),
                            operationFutureActionAttributes.getOperationNumber(),
                            operationFutureActionAttributes.getOriginalMainPDID(),
                            operationFutureActionAttributes.getOriginalOpeNo(), true, true);
                    log.trace("!ObjectUtils.isEmpty(experimentalLotInfos) : {}",!CimObjectUtils.isEmpty(experimentalLotInfos));
                    if (!CimObjectUtils.isEmpty(experimentalLotInfos)) {
                        out.setExperimentalLotInfo(experimentalLotInfos);
                    }
                }
            }

            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_HASHKEY_FPCFLAG, hashKey) : {}", CimStringUtils.equals(BizConstant.SP_HASHKEY_FPCFLAG, hashKey));
            if (CimStringUtils.equals(BizConstant.SP_HASHKEY_FPCFLAG, hashKey)) {
                log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_HASHDATA_FLAG_1, hashData) : {}", CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData));
                if (CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData)) {
                    log.info("hashData == 1");
                    Inputs.ObjFPCInfoGetDRIn in = new Inputs.ObjFPCInfoGetDRIn();
                    in.setLotID(lotID);
                    in.setMainPDID(operationFutureActionAttributes.getRouteID());
                    in.setMainOperNo(operationFutureActionAttributes.getOperationNumber());
                    in.setOrgMainPDID(new ObjectIdentifier(operationFutureActionAttributes.getOriginalMainPDID()));
                    in.setOrgOperNo(operationFutureActionAttributes.getOriginalOpeNo());
                    in.setSubMainPDID(new ObjectIdentifier(operationFutureActionAttributes.getSubOrigMainPDID()));
                    in.setSubOperNo(operationFutureActionAttributes.getSubOrigOpeNo());
                    in.setWaferIDInfoGetFlag(true);
                    in.setRecipeParmInfoGetFlag(true);
                    in.setReticleInfoGetFlag(true);
                    in.setDcSpecItemInfoGetFlag(true);
                    List<Infos.FPCInfo> fpcInfos = fpcMethod.fpcInfoGetDR(objCommon, in);
                    out.setFPCInfoList(fpcInfos);
                }
            }

            log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_HASHKEY_SCRIPTFLAG, hashKey) : {}", CimStringUtils.equals(BizConstant.SP_HASHKEY_SCRIPTFLAG, hashKey));
            if (CimStringUtils.equals(BizConstant.SP_HASHKEY_SCRIPTFLAG, hashKey)) {
                log.trace("ObjectUtils.equalsWithValue(BizConstant.SP_HASHDATA_FLAG_1, hashData) : {}", CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData));
                if (CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData)) {
                    log.debug("hashData == 1");
                    Infos.ScriptInfo scriptInfo = processMethod.processScriptInfoGetDR(objCommon,
                            operationFutureActionAttributes.getRouteID(),
                            operationFutureActionAttributes.getOperationID(),
                            operationFutureActionAttributes.getOperationNumber(),
                            operationFutureActionAttributes.getMainPOS(),
                            operationFutureActionAttributes.getModulePOS());
                    out.setScriptInfoList(Collections.singletonList(scriptInfo));
                }
            }
            // step9 lot_DBInfo_GetDR__160 step10 processHold_holdList_GetDR
            log.debug("get process hold list");
            log.trace("StringUtils.equals(BizConstant.SP_HASHKEY_PROCESSHOLDFLAG, hashKey) : {}", CimStringUtils.equals(BizConstant.SP_HASHKEY_PROCESSHOLDFLAG, hashKey));
            if (CimStringUtils.equals(BizConstant.SP_HASHKEY_PROCESSHOLDFLAG, hashKey)) {
                log.trace("StringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData) : {}", CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData));
                if (CimStringUtils.equals(BizConstant.SP_HASHDATA_FLAG_1, hashData)) {
                    Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
                    lotInfoInqFlag.setLotBasicInfoFlag(false);
                    lotInfoInqFlag.setLotControlUseInfoFlag(false);
                    lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
                    lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
                    lotInfoInqFlag.setLotOperationInfoFlag(false);
                    lotInfoInqFlag.setLotOrderInfoFlag(false);
                    lotInfoInqFlag.setLotControlJobInfoFlag(false);
                    lotInfoInqFlag.setLotProductInfoFlag(true);
                    lotInfoInqFlag.setLotRecipeInfoFlag(false);
                    lotInfoInqFlag.setLotLocationInfoFlag(false);
                    lotInfoInqFlag.setLotWipOperationInfoFlag(false);
                    lotInfoInqFlag.setLotWaferAttributesFlag(false);
                    lotInfoInqFlag.setLotBackupInfoFlag(false);
                    log.debug("get lot Detail info");
                    Infos.LotInfo objLotDetailInfoGetDROut = lotMethod.lotDBInfoGetDR(objCommon, lotInfoInqFlag, lotID);

                    Infos.ProcessHoldSearchKey processHoldSearchKey = new Infos.ProcessHoldSearchKey();
                    ObjectIdentifier reasonCodeID = null;
                    processHoldSearchKey.setOperationNumber(operationFutureActionAttributes.getOperationNumber());
                    processHoldSearchKey.setRouteID(operationFutureActionAttributes.getRouteID());
                    //------------------------------------
                    //     Get Process Hold Requests
                    //------------------------------------
                    log.debug("Get Process Hold Requests");
                    List<Infos.ProcHoldListAttributes> retCode = processMethod.processHoldHoldListGetDR(objCommon, processHoldSearchKey, reasonCodeID, 0L, false, false);
                    List<Infos.ProcHoldListAttributes> processDefinitionProcessHoldListAttributesList = retCode;
                    List<Infos.ProcHoldListAttributes> tmpProcHoldListAttributesList = new ArrayList<Infos.ProcHoldListAttributes>();
                    for (Infos.ProcHoldListAttributes processDefinitionProcessHold : processDefinitionProcessHoldListAttributesList) {
                        log.trace("!StringUtils.isEmpty(processDefinitionProcessHold.getProductID()) : {}",!ObjectIdentifier.isEmpty(processDefinitionProcessHold.getProductID()));
                        if (!ObjectIdentifier.isEmpty(processDefinitionProcessHold.getProductID())) {
                            log.debug("lot's ProductID:%s", objLotDetailInfoGetDROut.getLotProductInfo().getProductID().getValue());
                            log.debug("ProcessHold ProductID:%s", processDefinitionProcessHold.getProductID());
                            log.trace("StringUtils.equals(objLotDetailInfoGetDROut.getLotProductInfo().getProductID().getValue(),\n" +
                                    "                                    processDefinitionProcessHold.getProductID().getValue()) : {}",
                                    CimStringUtils.equals(objLotDetailInfoGetDROut.getLotProductInfo().getProductID().getValue(),
                                            processDefinitionProcessHold.getProductID().getValue()));

                            if (CimStringUtils.equals(objLotDetailInfoGetDROut.getLotProductInfo().getProductID().getValue(),
                                    processDefinitionProcessHold.getProductID().getValue())) {
                                log.debug("ProductID is same:%s,%s", objLotDetailInfoGetDROut.getLotProductInfo().getProductID().getValue(), processDefinitionProcessHold.getProductID().getValue());
                                tmpProcHoldListAttributesList.add(processDefinitionProcessHold);
                            } else {
                                tmpProcHoldListAttributesList.add(processDefinitionProcessHold);
                            }
                        }
                        out.setProcHoldListAttributes(tmpProcHoldListAttributesList);
                    }
                }
            }
        }
        out.setLotID(lotID);
        return out;
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotInfoByWaferInqParams
     * @param objCommon
     * @return com.fa.cim.dto.result.LotFamilyInqResult
     * @author Sun
     * @date 2018/10/10
     */

    public ObjectIdentifier sxLotInfoByWaferInq(Params.LotInfoByWaferInqParams lotInfoByWaferInqParams, Infos.ObjCommon objCommon) {
        log.debug("get lot wafer");
        return waferMethod.waferLotGet(objCommon, lotInfoByWaferInqParams.getWaferID());
    }

    public Results.LotInfoInqResult sxLotInfoInq(Infos.ObjCommon objCommon, Params.LotInfoInqParams lotInfoInqParams) {

        log.debug("【step0】 Get lot status information for all lotIDs of in-parameter ");
        Results.LotInfoInqResult lotInfoInqObj = new Results.LotInfoInqResult();
        List<Infos.LotInfo> lotInfos = new ArrayList<>();
        int lotLen = CimArrayUtils.getSize(lotInfoInqParams.getLotIDs());

        log.debug("【step1】Get lot status information for all lotIDs of in-parameter ");
        for (int i = 0; i < lotLen; i++) {
            ObjectIdentifier lotID = lotInfoInqParams.getLotIDs().get(i);

            Infos.LotInfo lotInfo = lotMethod.lotDetailInfoGetDR(objCommon, lotInfoInqParams.getLotInfoInqFlag(), lotID);
            Infos.LotBasicInfo lotBasicInfo = lotInfo.getLotBasicInfo();
            log.trace("lotInfoInqParams.getLotInfoInqFlag().getLotBasicInfoFlag() : {}",lotInfoInqParams.getLotInfoInqFlag().getLotBasicInfoFlag());
            if (lotInfoInqParams.getLotInfoInqFlag().getLotBasicInfoFlag()) {
                log.debug("Get InterFab Transfer State");
                String strLotInterFabXferStateGetOut = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
                log.debug("Get InterFab Transfer State {}",strLotInterFabXferStateGetOut);

                lotBasicInfo.setInterFabXferState(strLotInterFabXferStateGetOut);
                lotBasicInfo.setSorterJobExistFlag(false);

                log.debug("Get sorter information for the lot.");
                com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn in = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
                log.debug("Get sorter information for the lot {}",in);
                in.setLotID(lotID);
                List<Info.SortJobListAttributes> objSorterJobListGetDROutRetCode = sorterMethod.sorterJobListGetDR(objCommon, in);

                log.trace("ArrayUtils.isNotEmpty(objSorterJobListGetDROutRetCode) : {}", CimArrayUtils.isNotEmpty(objSorterJobListGetDROutRetCode));
                if (CimArrayUtils.isNotEmpty(objSorterJobListGetDROutRetCode)) {
                    lotBasicInfo.setSorterJobExistFlag(true);
                }

                log.debug("Get Bonding Group ID");
                String strLotBondingGroupIDGetDROut = lotMethod.lotBondingGroupIDGetDR(objCommon, lotID);
                log.debug("Get Bonding Group ID {}",strLotBondingGroupIDGetDROut);
                lotBasicInfo.setBondingGroupID(strLotBondingGroupIDGetDROut);

                log.debug("Get Auto Dispatch Control Information");
                Inputs.ObjAutoDispatchControlInfoGetDRIn objAutoDispatchControlInfoGetDRIn = new Inputs.ObjAutoDispatchControlInfoGetDRIn();
                log.debug("Get Auto Dispatch Control Information {}",objAutoDispatchControlInfoGetDRIn);
                objAutoDispatchControlInfoGetDRIn.setLotID(lotID);
                List<Infos.LotAutoDispatchControlInfo> autoDispatchControlInfo = autoDispatchControlMethod.autoDispatchControlInfoGetDR(objCommon, objAutoDispatchControlInfoGetDRIn);

                log.debug("Set Auto Dispatch Control Flag");
                log.trace("ArrayUtils.isNotEmpty(autoDispatchControlInfo) : {}", CimArrayUtils.isNotEmpty(autoDispatchControlInfo));
                if (CimArrayUtils.isNotEmpty(autoDispatchControlInfo)) {
                    lotBasicInfo.setAutoDispatchControlFlag(true);
                } else {
                    lotBasicInfo.setAutoDispatchControlFlag(false);
                }

                log.debug("Get Auto Monitor Job Information");
                Infos.EqpMonitorJobLotInfo eqpMonitorJob = lotMethod.lotEqpMonitorJobGet(objCommon, lotID);
                log.trace("!ObjectUtils.isEmpty(eqpMonitorJob.getEqpMonitorJobID()) : {}",!ObjectIdentifier.isEmpty(eqpMonitorJob.getEqpMonitorJobID()));
                if (!ObjectIdentifier.isEmpty(eqpMonitorJob.getEqpMonitorJobID())) {
                    Infos.EqpMonitorID strEqpMonitorID = new Infos.EqpMonitorID();
                    strEqpMonitorID.setEquipmentID(eqpMonitorJob.getEquipmentID());
                    strEqpMonitorID.setChamberID(eqpMonitorJob.getChamberID());
                    strEqpMonitorID.setEqpMonitorID(eqpMonitorJob.getEqpMonitorID());

                    strEqpMonitorID.setEqpMonitorJobID(eqpMonitorJob.getEqpMonitorJobID());
                    lotBasicInfo.setEqpMonitorID(strEqpMonitorID);
                }
            }

            log.debug("Get InPostProcessFlag of lot");
            Outputs.ObjLotInPostProcessFlagOut processFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);
            log.debug("Get InPostProcessFlag of lot {}",processFlagGetOut);
            lotBasicInfo.setInPostProcessFlagOfCassette(processFlagGetOut.getInPostProcessFlagOfCassette());
            lotBasicInfo.setInPostProcessFlagOfLot(processFlagGetOut.getInPostProcessFlagOfLot());

            if(log.isDebugEnabled()) {
                log.debug("Get dispatchReadiness of lot");
            }
            DispatchReadinessState readinessState = lotMethod.lotDispatchReadinessGet(objCommon, lotID);
            if (log.isDebugEnabled()) {
                log.debug("Get dispatchReadiness of lot {}", readinessState.name());
            }
            lotBasicInfo.setDispatchReadiness(readinessState.name());

            lotInfo.setLotBasicInfo(lotBasicInfo);

            log.trace("lotInfoInqParams.getLotInfoInqFlag().getLotProductInfoFlag() : {}",lotInfoInqParams.getLotInfoInqFlag().getLotProductInfoFlag());
            if (lotInfoInqParams.getLotInfoInqFlag().getLotProductInfoFlag()) {
                Infos.LotProductInfo lotProductInfo = lotInfo.getLotProductInfo();
                log.debug("Get product BOM information");
                ObjectIdentifier productID = lotProductInfo.getProductID();
                log.debug("Get product BOM information {}",productID);
                Outputs.ObjProductBOMInfoGetOut bomOut = null;
                try {
                    bomOut = productMethod.productBOMInfoGet(objCommon, productID);
                } catch (ServiceException e) {
                    log.trace("Validations.isEquals(retCodeConfig.getBomNotDefined(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getBomNotDefined(), e.getCode()));
                    if (Validations.isEquals(retCodeConfig.getBomNotDefined(), e.getCode())) {

                    } else {
                        throw e;
                    }
                }
                lotProductInfo.setBomID(CimObjectUtils.isEmpty(bomOut) ? null : bomOut.getBomID());
                lotInfo.setLotProductInfo(lotProductInfo);
            }

            lotInfos.add(lotInfo);
        }

        Infos.LotListInCassetteInfo strLotListInCassetteInfo = new Infos.LotListInCassetteInfo();
        List<Infos.WaferMapInCassetteInfo> strWaferMapInCassetteInfo = null;
        log.trace("lotLen == 1 : {}",lotLen == 1);
        log.trace("lotLen > 1 : {}",lotLen > 1);
        if (lotLen == 1) {
            ObjectIdentifier cassetteListOut = null;
            try {
                cassetteListOut = lotMethod.lotCassetteListGetDR(objCommon, lotInfoInqParams.getLotIDs().get(0));
            } catch (ServiceException e) {
                log.trace("Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode()));
                if (Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {

                } else {
                    throw e;
                }
            }

            log.trace("!ObjectUtils.isEmpty(cassetteListOut) : {}",!ObjectIdentifier.isEmpty(cassetteListOut));
            if (!ObjectIdentifier.isEmpty(cassetteListOut)) {
                if (lotInfoInqParams.getLotInfoInqFlag().getLotListInCassetteInfoFlag()) {
                    Infos.LotListInCassetteInfo cassetteLotListOut = null;
                    try {
                        cassetteLotListOut = cassetteMethod.cassetteLotIDListGetDR(objCommon, cassetteListOut);
                    } catch (ServiceException e) {
                        log.trace("Validations.isEquals(retCodeConfig.getNotFoundLot(), e.getCode()) || Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode()) : {}",
                                Validations.isEquals(retCodeConfig.getNotFoundLot(), e.getCode()) || Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode()));

                        if (Validations.isEquals(retCodeConfig.getNotFoundLot(), e.getCode()) || Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {

                        } else {
                            throw e;
                        }
                    }
                    strLotListInCassetteInfo = cassetteLotListOut;

                }
                log.trace("lotInfoInqParams.getLotInfoInqFlag().getLotWaferMapInCassetteInfoFlag() : {}",lotInfoInqParams.getLotInfoInqFlag().getLotWaferMapInCassetteInfoFlag());
                if (lotInfoInqParams.getLotInfoInqFlag().getLotWaferMapInCassetteInfoFlag()) {
                    List<Infos.WaferMapInCassetteInfo> cassetteGetWaferMapOutRetCode = null;
                    try {
                        cassetteGetWaferMapOutRetCode = cassetteMethod.cassetteGetWaferMapDR(objCommon, cassetteListOut);
                    } catch (ServiceException e) {
                        log.trace("Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode()));
                        if (Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {

                        } else {
                            throw e;
                        }
                    }
                    strWaferMapInCassetteInfo = cassetteGetWaferMapOutRetCode;
                }
            }

        } else if (lotLen > 1) {
            ObjectIdentifier strLotCassetteSameCheckDROut = lotMethod.lotCassetteSameCheckDR(objCommon, lotInfoInqParams.getLotIDs());

            log.trace("!ObjectUtils.isEmpty(strLotCassetteSameCheckDROut) : {}",!ObjectIdentifier.isEmpty(strLotCassetteSameCheckDROut));
            if (!ObjectIdentifier.isEmpty(strLotCassetteSameCheckDROut)) {
                log.trace("otInfoInqParams.getLotInfoInqFlag().getLotListInCassetteInfoFlag() : {}",lotInfoInqParams.getLotInfoInqFlag().getLotListInCassetteInfoFlag());
                if (lotInfoInqParams.getLotInfoInqFlag().getLotListInCassetteInfoFlag()) {
                    Infos.LotListInCassetteInfo cassetteLotListGetResult = null;
                    try {
                        cassetteLotListGetResult = cassetteMethod.cassetteLotIDListGetDR(objCommon, strLotCassetteSameCheckDROut);
                    } catch (ServiceException e) {
                        log.trace("Validations.isEquals(retCodeConfig.getNotFoundLot(), e.getCode()) || Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode()) : {}",
                                Validations.isEquals(retCodeConfig.getNotFoundLot(), e.getCode()) || Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode()));

                        if (Validations.isEquals(retCodeConfig.getNotFoundLot(), e.getCode()) || Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {

                        } else {
                            throw e;
                        }
                    }
                    strLotListInCassetteInfo = cassetteLotListGetResult;
                }

                log.trace("lotInfoInqParams.getLotInfoInqFlag().getLotWaferMapInCassetteInfoFlag() : {}",lotInfoInqParams.getLotInfoInqFlag().getLotWaferMapInCassetteInfoFlag());
                if (lotInfoInqParams.getLotInfoInqFlag().getLotWaferMapInCassetteInfoFlag()) {
                    List<Infos.WaferMapInCassetteInfo> objCassetteGetWaferMapOutRetCode = null;
                    try {
                        objCassetteGetWaferMapOutRetCode = cassetteMethod.cassetteGetWaferMapDR(objCommon, strLotCassetteSameCheckDROut);
                    } catch (ServiceException e) {
                        log.trace("Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode()) : {}",Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode()));
                        if (Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {

                        } else {
                            throw e;
                        }

                    }
                    strWaferMapInCassetteInfo = objCassetteGetWaferMapOutRetCode;
                }
            }
        }

        lotInfoInqObj.setLotListInCassetteInfo(strLotListInCassetteInfo);
        lotInfoInqObj.setWaferMapInCassetteInfoList(strWaferMapInCassetteInfo);
        lotInfoInqObj.setLotInfoList(lotInfos);
        return lotInfoInqObj;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param cassetteID -
     * @param objCommon  -
     * @return com.fa.cim.dto.result.RetCode<LotListByCarrierInqResult>
     * @author Bear
     * @since 2018/5/30
     */
    public Results.LotListByCarrierInqResult sxLotListByCarrierInq(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        // check params
        //【step1】get cassette associated information
        log.debug("【step1】get cassette associated information by call cassetteGetLotList()");
        Infos.LotListInCassetteInfo objCassetteGetLotListOut = new Infos.LotListInCassetteInfo();
        try {
            objCassetteGetLotListOut = cassetteMethod.cassetteGetLotList(objCommon, cassetteID);
        } catch (ServiceException e) {
            log.trace("!Validations.isEquals(retCodeConfig.getNotFoundLot(), e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getNotFoundLot(), e.getCode()));
            if (!Validations.isEquals(retCodeConfig.getNotFoundLot(), e.getCode())) {
                throw e;
            }
            objCassetteGetLotListOut = (Infos.LotListInCassetteInfo) e.getData();
        }
        log.trace("null != objCassetteGetLotListOut && ArrayUtils.isNotEmpty(objCassetteGetLotListOut.getLotIDList()) : {}",null != objCassetteGetLotListOut && CimArrayUtils.isNotEmpty(objCassetteGetLotListOut.getLotIDList()));
        if (null != objCassetteGetLotListOut && CimArrayUtils.isNotEmpty(objCassetteGetLotListOut.getLotIDList())) {
            objCassetteGetLotListOut.getLotIDList().sort(Comparator.comparing(ObjectIdentifier::getValue));
        }

        //【step2】get cassette associated information
        log.debug("【step2】get cassette associated information by call cassetteGetWaferMapDR()");
        List<Infos.WaferMapInCassetteInfo> objCassetteGetWaferMapOut = cassetteMethod.cassetteGetWaferMapDR(objCommon, cassetteID);
        Results.LotListByCarrierInqResult obj = new Results.LotListByCarrierInqResult();
        obj.setLotListInCassetteInfo(objCassetteGetLotListOut);
        log.trace("null != objCassetteGetWaferMapOut : {}",null != objCassetteGetWaferMapOut);
        if (null != objCassetteGetWaferMapOut) {
            obj.setWaferMapInCassetteInfoList(objCassetteGetWaferMapOut);
        }
        return obj;
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotListByCJInqParams
     * @param objCommon
     * @return RetCode<Results.LotListByCJInqResult>
     * @author Sun
     * @date 2018/10/22
     */

    public List<Infos.ControlJobInfo> sxLotListByCJInq(Params.LotListByCJInqParams lotListByCJInqParams, Infos.ObjCommon objCommon) {
        log.debug("【Method Entry】sxLotListByCJInq()");
        int nLen = CimArrayUtils.getSize(lotListByCJInqParams.getControlJobIDs());
        List<Infos.ControlJobInfo> controlJobInfoList = new ArrayList<>();
        for (int i = 0; i < nLen; i++) {
            // Get cassette associated information wihch are contained in controlJob ;
            log.debug("Get cassette associated information wihch are contained in controlJob");
            List<Infos.ControlJobCassette> controlJobCassetteList = controlJobMethod.controlJobContainedLotGet(objCommon, lotListByCJInqParams.getControlJobIDs().get(i));
            //Set out structure;
            log.debug("Set out structure;");
            Infos.ControlJobInfo controlJobInfo = new Infos.ControlJobInfo();
            controlJobInfo.setControlJobCassetteList(controlJobCassetteList);
            controlJobInfoList.add(controlJobInfo);
        }
        log.debug("【Method Exit】sxLotListByCJInq()");
        return controlJobInfoList;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * 2018/12/25       Bug-148           Sun                filter lot for Move Bank Screen, only display 'InBank' Lot;
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param lotListInqParams -
     * @param objCommon        -
     * @return Page<Infos.LotListAttributes>
     * @author Sun
     * @since 12/25/2018 2:00 PM
     */
    public Page<Infos.LotListAttributes> sxLotListInq(Params.LotListInqParams lotListInqParams, Infos.ObjCommon objCommon) {
        //  Gets list out of all Lot
        log.debug("Gets list out of all Lot");
        SearchCondition searchCondition = lotListInqParams.getSearchCondition();

        log.trace("lot delete flag is {}",lotListInqParams.getDeleteLotFlag());
        log.trace("BooleanUtils.isFalse(lotListInqParams.getDeleteLotFlag()) : {}", CimBooleanUtils.isFalse(lotListInqParams.getDeleteLotFlag()));
        if (CimBooleanUtils.isFalse(lotListInqParams.getDeleteLotFlag())) {
            return lotMethod.lotListGetDR(objCommon, lotListInqParams, searchCondition);
        } else {
            log.debug("get list out of deletable lot");
            return lotMethod.lotListForDeletionGetDR(objCommon, new Inputs.ObjLotListForDeletionGetDRIn(lotListInqParams), searchCondition);
        }
    }

    public Results.LotOperationSelectionInqResult sxLotOperationSelectionInq(Infos.ObjCommon objCommon, Params.LotOperationSelectionInqParams params) {

        Inputs.ObjProcessOperationListForLotIn in = new Inputs.ObjProcessOperationListForLotIn();
        in.setSearchDirectionFlag(params.isSearchDirection());
        in.setPosSearchFlag(params.isPosSearchFlag());
        in.setSearchCount(params.getSearchCount());
        in.setCurrentFlag(params.isCurrentFlag());
        in.setLotID(params.getLotID());
        List<Infos.OperationNameAttributes> out = null;
        try {
            out = processMethod.processOperationListForLot(objCommon, in);
        } catch (ServiceException ex) {
            log.trace("!Validations.isEquals(retCodeConfig.getSucc(), ex.getCode() : {}",
                    !Validations.isEquals(retCodeConfig.getSucc(), ex.getCode()));
            if (!Validations.isEquals(retCodeConfig.getSucc(), ex.getCode())
                    && !Validations.isEquals(retCodeConfig.getSomeopelistDataError(), ex.getCode())
                    && !Validations.isEquals(retCodeConfig.getInvalidLcData(), ex.getCode())
                    && !Validations.isEquals(retCodeConfig.getFutureholdReservedUntilJoinpoint(), ex.getCode())
                    && !Validations.isEquals(retCodeConfig.getCannotSetLcSkipToLastOpe(), ex.getCode())) {
                throw ex;
            }
        }

        SearchCondition searchCondition = params.getSearchCondition();
        log.trace("ObjectUtils.isEmpty(searchCondition) : {}", CimObjectUtils.isEmpty(searchCondition));
        if (CimObjectUtils.isEmpty(searchCondition)) {
            searchCondition = new SearchCondition();
            searchCondition.setSize(params.getSearchCount());
        }
        Results.LotOperationSelectionInqResult result = new Results.LotOperationSelectionInqResult();
        result.setLotID(params.getLotID());
        result.setOperationNameAttributesAttributes(CimPageUtils.convertListToPage(
                out, searchCondition.getPage(), searchCondition.getSize()));
        return result;
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @return Results.LotOpeMemoListInqResult
     * @author Sun
     * @since 2018/10/23
     */

    public List<Infos.SubMfgLayerAttributes> sxAllMfgLayerListInq(Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxAllMfgLayerListInq()");

        //Check PosLot, get PosProcessOperation and PosLotNote ;
        log.debug("Check PosLot, get PosProcessOperation and PosLotNote");
        List<Infos.SubMfgLayerAttributes> resultRetCode = categoryMethod.categoryFillInTxTRQ010DR(objCommon);

        log.debug("【Method Exit】sxAllMfgLayerListInq()");

        return resultRetCode;
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return com.fa.cim.dto.RetCode<List < com.fa.cim.ObjectIdentifier>>
     * @author Sun
     * @date 2018/10/24
     */

    public List<ObjectIdentifier> sxAllProcessStepListInq(Infos.ObjCommon objCommon) {
        log.debug("get process definition ");
        return processMethod.processDefinitionProcessDefinitionIDGetDR(objCommon);
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param params
     * @return com.fa.cim.dto.RetCode<List < Results.MainProcessFlowListInqResult>>
     * @author Sun
     * @date 2018/10/24
     */

    public List<Infos.RouteIndexInformation> sxMainProcessFlowListInq(Infos.ObjCommon objCommon, Params.MainProcessFlowListInqParams params) {
        log.debug("【Method Entry】sxMainProcessFlowListInq()");
        //Main index search ;
        List<Infos.RouteIndexInformation> routeIndexInformationList = processMethod.processRouteList(objCommon, params);
        log.debug("【Method Exit】sxMainProcessFlowListInq()");
        return routeIndexInformationList;
    }

    public List<Infos.OperationNameAttributes> sxProcessFlowOperationListForLotInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        /*---------------------*/
        /*   Check lot Exist   */
        /*---------------------*/
        log.debug("Check Lot Exist");
        String lotStateGet = lotMethod.lotStateGet(objCommon, lotID);

        /*----------------------------------------*/
        /*   Get backward operation info of lot   */
        /*----------------------------------------*/
        log.debug("call LotOperationSelectionInq()   (Backward)");
        Results.LotOperationSelectionInqResult backwardOperationOut = sxLotOperationSelectionInq(objCommon, new Params.LotOperationSelectionInqParams(false, true, 9999, false, lotID));


        /*---------------------------------------*/
        /*   Get forward operation info of lot   */
        /*---------------------------------------*/
        log.debug("call LotOperationSelectionInq()   (Forward)");
        Results.LotOperationSelectionInqResult forwardOperationOut = sxLotOperationSelectionInq(objCommon, new Params.LotOperationSelectionInqParams(true, true, 9999, true, lotID));


        /*------------------------*/
        /*   Get RouteID of lot   */
        /*------------------------*/
        log.debug("Get RouteID of lot");
        ObjectIdentifier routeID = lotMethod.lotRouteIdGet(objCommon, lotID);

        List<Infos.OperationNameAttributes> operationNameAttributesList = new ArrayList<>();
        /*-----------------------*/
        /*   Set Output Struct   */
        /*-----------------------*/
        log.debug("Set Output Struct");
        Page<Infos.OperationNameAttributes> backwardPage = backwardOperationOut.getOperationNameAttributesAttributes();
        log.trace("!ObjectUtils.isEmpty(backwardPage) : {}",!CimObjectUtils.isEmpty(backwardPage));
        if (!CimObjectUtils.isEmpty(backwardPage)) {
            List<Infos.OperationNameAttributes> backwardList = backwardPage.getContent();
            int backwardListCount = CimArrayUtils.getSize(backwardList);
            log.trace("backwardListCount > 0 : {}",backwardListCount > 0);
            if (backwardListCount > 0) {
                for (int i = backwardListCount - 1; i >= 0; i--) {
                    Infos.OperationNameAttributes operationNameAttributes = backwardList.get(i);
                    log.trace("ObjectUtils.equalsWithValue(operationNameAttributes.getRouteID(), routeID) : {}", ObjectIdentifier.equalsWithValue(operationNameAttributes.getRouteID(), routeID));
                    if (ObjectIdentifier.equalsWithValue(operationNameAttributes.getRouteID(), routeID)) {
                        operationNameAttributesList.add(operationNameAttributes);
                    }
                }
            }
        }
        Page<Infos.OperationNameAttributes> forwardPage = forwardOperationOut.getOperationNameAttributesAttributes();
        log.trace("!ObjectUtils.isEmpty(forwardPage) : {}",!CimObjectUtils.isEmpty(forwardPage));
        if (!CimObjectUtils.isEmpty(forwardPage)) {
            List<Infos.OperationNameAttributes> forwardList = forwardPage.getContent();
            forwardList.forEach(operationNameAttributes -> {
                log.trace("ObjectUtils.equalsWithValue(operationNameAttributes.getRouteID(), routeID) : {}", ObjectIdentifier.equalsWithValue(operationNameAttributes.getRouteID(), routeID));
                if (ObjectIdentifier.equalsWithValue(operationNameAttributes.getRouteID(), routeID)) {
                    operationNameAttributesList.add(operationNameAttributes);
                }
            });
        }

        return operationNameAttributesList;
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param processFlowOperationListInqParams
     * @return RetCode<List < Infos.OperationNameAttributes160>>
     * @author Sun
     * @date 2018/10/16
     */

    public List<Infos.OperationNameAttributes> sxProcessFlowOperationListInq(Infos.ObjCommon objCommon, Params.ProcessFlowOperationListInqParams processFlowOperationListInqParams) {
        log.debug("【Method Entry】sxProcessFlowOperationListInq()");
        // Converts input subrouteID if it has the version ##  ;
        log.debug("Converts input subrouteID if it has the version");
        ObjectIdentifier aRouteID = processMethod.processActiveIDGet(objCommon, processFlowOperationListInqParams.getRouteID());
        /*-----------------------------------------------------------*/
        /*   Use P.O., P.O.S., P.D.                                  */
        /*-----------------------------------------------------------*/
        Inputs.ProcessOperationListForRoute strProcessOperationListForRouteIn = new Inputs.ProcessOperationListForRoute();
        strProcessOperationListForRouteIn.setRouteID(aRouteID);
        strProcessOperationListForRouteIn.setOperationID(processFlowOperationListInqParams.getOperationID());
        strProcessOperationListForRouteIn.setOperationNumber(processFlowOperationListInqParams.getOperationNumber());
        strProcessOperationListForRouteIn.setPdType(processFlowOperationListInqParams.getPdType());
        strProcessOperationListForRouteIn.setSearchCount(processFlowOperationListInqParams.getSearchCount());
        List<Infos.OperationNameAttributes> operationNameAttributesList = processMethod.processOperationListForRoute(objCommon, strProcessOperationListForRouteIn);
        log.debug("【Method Exit】sxProcessFlowOperationListInq()");
        return operationNameAttributesList;
    }

    @Override
    public Results.LotsMoveInReserveInfoInqResult sxLotsMoveInReserveInfoInq(Infos.ObjCommon objCommon, Params.LotsMoveInReserveInfoInqParams lotsMoveInReserveInfoInqParams) {
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult = new Results.LotsMoveInReserveInfoInqResult();
        Validations.check(0 >= lotsMoveInReserveInfoInqParams.getStartCassettes().size(), retCodeConfig.getInvalidParameter());
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Object Lock Process                                                 */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Check Process                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.debug("Object Lock Process and Check Process");
        log.debug("step1 - equipment categoryVsTxID CheckCombination");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, lotsMoveInReserveInfoInqParams.getEquipmentID());
        //----------------------------------------
        // Change processJobExecFlag to True.
        //----------------------------------------
        log.debug("Change processJobExecFlag to True.");
        lotsMoveInReserveInfoInqResult.setStrStartCassette(lotsMoveInReserveInfoInqParams.getStartCassettes());
        Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteOut = null;
        boolean slotmapConflictWarnFlag = false;
        try {
            startCassetteOut = startCassetteMethod.startCassetteProcessJobExecFlagSet(objCommon, lotsMoveInReserveInfoInqParams.getStartCassettes(), lotsMoveInReserveInfoInqParams.getEquipmentID());
        } catch (ServiceException e) {
            log.trace("!Validations.isEquals(retCodeConfig.getSmplSlotmapConflictWarn(), e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getSmplSlotmapConflictWarn(), e.getCode()));
            if (!Validations.isEquals(retCodeConfig.getSmplSlotmapConflictWarn(), e.getCode())) {
                throw e;
            } else {
                startCassetteOut = e.getData(Outputs.ObjStartCassetteProcessJobExecFlagSetOut.class);
                slotmapConflictWarnFlag = true;
            }
        }
        //-------------------------------------------------------
        // create return message or e-mail text.
        //-------------------------------------------------------
        log.debug("step3 - create return message or e-mail text.");
        StringBuffer smplMessageSb = new StringBuffer();
        List<Infos.ObjSamplingMessageAttribute> samplingMessageList = startCassetteOut.getSamplingMessage();
        int pcount = 0;
        int samplingMessageLen = CimArrayUtils.getSize(samplingMessageList);
        log.trace("samplingMessageLen > 0 : {}",samplingMessageLen > 0);
        if (samplingMessageLen > 0) {
            for (Infos.ObjSamplingMessageAttribute objSamplingMessageAttribute : samplingMessageList) {
                log.trace("objSamplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_WARN_MAIL : {}",objSamplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_WARN_MAIL);
                log.trace("objSamplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_IGNORED_MAIL : {}",objSamplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_IGNORED_MAIL);
                if (objSamplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_WARN_MAIL) {
                    // create return message;
                    log.debug("create return message;");
                    log.trace("pcount > 0 && pcount < samplingMessageLen - 1 : {}",pcount > 0 && pcount < samplingMessageLen - 1);
                    if (pcount > 0 && pcount < samplingMessageLen - 1) {
                        smplMessageSb.append(",");
                    }
                    smplMessageSb.append(objSamplingMessageAttribute.getMessageText());
                    pcount++;
                } else if (objSamplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_IGNORED_MAIL) {
                    Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                    alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                    alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                    alertMessageRptParams.setSystemMessageText(objSamplingMessageAttribute.getMessageText());
                    alertMessageRptParams.setNotifyFlag(true);
                    alertMessageRptParams.setLotID(objSamplingMessageAttribute.getLotID());
                    alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                    systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                }
            }
        }

        lotsMoveInReserveInfoInqResult.setStrStartCassette(startCassetteOut.getStartCassettes());
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Main Process                                                        */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*-------------------------------------------*/
        /*   Get Information for Start Reservation   */
        /*-------------------------------------------*/
        log.debug("[step-4] Get Information for Start Reservation");
        Outputs.ObjProcessStartReserveInformationGetBaseInfoForClientOut out = processMethod.processStartReserveInformationGetBaseInfoForClient(objCommon, lotsMoveInReserveInfoInqParams.getEquipmentID(), lotsMoveInReserveInfoInqResult.getStrStartCassette());
        lotsMoveInReserveInfoInqResult.setEquipmentID(out.getEquipmentID());
        lotsMoveInReserveInfoInqResult.setStrStartCassette(out.getStartCassetteList());
        log.info("step5 - fpcStartCassetteInfoExchange");
        String tmpFPCAdoptFlagStr = StandardProperties.OM_DOC_ENABLE_FLAG.getValue();
        log.trace("1 == BaseStaticMethod.parseToInteger(tmpFPCAdoptFlagStr) : {}",1 == CimNumberUtils.intValue(tmpFPCAdoptFlagStr));
        if (1 == CimNumberUtils.intValue(tmpFPCAdoptFlagStr)) {
            List<Infos.StartCassette> exchangeFPCStartCassetteInfo = fpcMethod.fpcStartCassetteInfoExchange(objCommon, BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO, lotsMoveInReserveInfoInqResult.getEquipmentID(), lotsMoveInReserveInfoInqResult.getStrStartCassette());
            lotsMoveInReserveInfoInqResult.setStrStartCassette(exchangeFPCStartCassetteInfo);
        } else {
            log.debug("DOC Adopt Flag is OFF.");
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   APC I/F                                                             */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*------------------------------------------------------*/
        /*   Get Info for StartReservation use APC I/F or not   */
        /*------------------------------------------------------*/
        log.debug("step5 - Get Info for StartReservation use APC I/F or not");
        boolean apcInterFaceFlag = apcMethod.apcInterfaceFlagCheck(objCommon, lotsMoveInReserveInfoInqResult.getStrStartCassette());
        log.trace("apcInterFaceFlag : {}",apcInterFaceFlag);
        if (apcInterFaceFlag) {
            /*--------------------------------------------------*/
            /*                                                  */
            /*   Get Equipment's Start Reserved ControlJob ID   */
            /*                                                  */
            /*--------------------------------------------------*/
            log.debug("step6 -Get Equipment's Start Reserved ControlJob ID");
            List<Infos.StartReservedControlJobInfo> startReservedControlJobInfoList = equipmentMethod.equipmentReservedControlJobIDGet(objCommon, lotsMoveInReserveInfoInqParams.getEquipmentID());
            /*-------------------------------------------------*/
            /*                                                 */
            /*   Get Cassette's Start Reserved ControlJob ID   */
            /*                                                 */
            /*-------------------------------------------------*/
            log.debug("Get Cassette's Start Reserved ControlJob ID");
            ObjectIdentifier saveControlJobID = new ObjectIdentifier();
            List<Infos.StartCassette> strStartCassette = lotsMoveInReserveInfoInqResult.getStrStartCassette();
            int nIMax = CimArrayUtils.getSize(strStartCassette);
            for (int i = 0; i < nIMax; i++) {
                log.debug("step7 - cassette controlJobID Get");
                ObjectIdentifier controlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, strStartCassette.get(i).getCassetteID());
                boolean findFlag = false;
                int nJMax = CimArrayUtils.getSize(startReservedControlJobInfoList);
                int j = 0;
                for (j = 0; j < nJMax; j++) {
                    log.trace("ObjectUtils.equalsWithValue(startReservedControlJobInfoList.get(j).getControlJobID(), controlJobID) : {}", ObjectIdentifier.equalsWithValue(startReservedControlJobInfoList.get(j).getControlJobID(), controlJobID));
                    if (ObjectIdentifier.equalsWithValue(startReservedControlJobInfoList.get(j).getControlJobID(), controlJobID)) {
                        findFlag = true;
                        break;
                    }
                }
                log.trace("!ObjectUtils.isEmptyWithValue(controlJobID) : {}",!ObjectIdentifier.isEmptyWithValue(controlJobID));
                if (!ObjectIdentifier.isEmptyWithValue(controlJobID)) {
                    saveControlJobID = controlJobID;
                    Validations.check(!findFlag, new OmCode(retCodeConfig.getNotResvedPortgrp()));
                } else {
                    log.trace("findFlag : {}",findFlag);
                    if (findFlag) {
                        throw new ServiceException(new OmCode(retCodeConfig.getAlreadyReservedPortGroup(), startReservedControlJobInfoList.get(j).getPortGroupID(), ObjectIdentifier.fetchValue(startReservedControlJobInfoList.get(j).getControlJobID())));
                    }
                }
            }
            /*---------------------*/  // This implementation is not good from
            /*   Get PortGroupID   */  // responce point of view. In the near
            /*---------------------*/  // future, this logic must be replaced.
            log.debug("step8 - equipment portInfo Get");
            ObjectIdentifier savePortGroupID = new ObjectIdentifier();
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, lotsMoveInReserveInfoInqParams.getEquipmentID());
            List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
            int epsLength = CimArrayUtils.getSize(eqpPortStatuses);
            for (int i = 0; i < epsLength; i++) {
                String epsIdent = ObjectIdentifier.fetchValue(eqpPortStatuses.get(i).getLoadedCassetteID());
                String inCIDIdent = ObjectIdentifier.fetchValue(strStartCassette.get(0).getCassetteID());
                log.trace("StringUtils.equals(epsIdent, inCIDIdent) : {}", CimStringUtils.equals(epsIdent, inCIDIdent));
                if (CimStringUtils.equals(epsIdent, inCIDIdent)) {
                    savePortGroupID.setValue(eqpPortStatuses.get(i).getPortGroup());
                    break;
                }
            }
            /*-------------------------------------------------*/
            /*   Get from APCInterface AdjustRecipeParameter   */
            /*-------------------------------------------------*/
            // step9 - APCMgr_SendRecipeParamInq
            log.debug("Get from APCInterface AdjustRecipeParameter");
            List<Infos.StartCassette> apcStartCassettes = apcMethod.APCMgrSendRecipeParamInq(objCommon, lotsMoveInReserveInfoInqParams.getEquipmentID(), savePortGroupID.getValue(), saveControlJobID, lotsMoveInReserveInfoInqParams.getStartCassettes(),BizConstant.SP_OPERATION_STARTRESERVATION);
            lotsMoveInReserveInfoInqResult.setStrStartCassette(apcStartCassettes);
        }
        Validations.check(slotmapConflictWarnFlag, new OmCode(retCodeConfig.getSmplSlotmapConflictWarn(), smplMessageSb.toString()));
        return lotsMoveInReserveInfoInqResult;
    }

    @Override
    public List<ObjectIdentifier> getAllLotType() {
        List<ObjectIdentifier> objectIdentifiers = lotMethod.allLotTypeGet();
        return objectIdentifiers;
    }
}
