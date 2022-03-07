package com.fa.cim.service.bond.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.method.*;
import com.fa.cim.service.bond.IBondInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 17:23
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class BondInqServiceImpl implements IBondInqService {
    @Autowired
    private IBondingFlowMethod bondingFlowMethod;
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;


    @Autowired
    private IBondingLotMethod bondingLotMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ILotMethod lotMethod;


    @Autowired
    private IProcessMethod processMethod;


    @Autowired
    private ILotInBondingFlowMethod lotInBondingFlowMethod;


    /**
     * This function obtains the relation map between Base Wafer and Top Wafer based on input.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/21 16:30
     */
    public List<Infos.StackedWaferInfo> sxStackedWaferListInq(Infos.ObjCommon objCommon, Params.StackedWaferListInqInParams stackedWaferListInqInParams) {
        Validations.check(null == stackedWaferListInqInParams, retCodeConfig.getInvalidParameter());
        log.info("InParam [lotID]: " + ObjectIdentifier.fetchValue(stackedWaferListInqInParams.getLotID()));
        log.info("InParam [cassetteID]: " + ObjectIdentifier.fetchValue(stackedWaferListInqInParams.getCassetteID()));
        log.info("InParam [baseLotSearchFlag]: " + stackedWaferListInqInParams.getBaseLotSearchFlag());

        List<Infos.StackedWaferInfo> retVal;

        boolean fromCassette;
        if (!ObjectIdentifier.isEmptyWithValue(stackedWaferListInqInParams.getLotID())) {
            fromCassette = false;
        } else if (!ObjectIdentifier.isEmptyWithValue(stackedWaferListInqInParams.getCassetteID())) {
            fromCassette = true;
        } else {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }

        //------------------------------------------------------
        // Get stacked wafer information
        //------------------------------------------------------
        Inputs.ObjLotStackedWaferInfoGetDRIn lotStackedWaferInfoGetDRIn = new Inputs.ObjLotStackedWaferInfoGetDRIn();

        if (fromCassette) {
            log.info("fromCassette = TRUE");
            lotStackedWaferInfoGetDRIn.setBaseCarrierID(stackedWaferListInqInParams.getCassetteID());
            Outputs.ObjLotStackedWaferInfoGetDROut objLotStackedWaferInfoGetDROut = lotMethod.lotStackedWaferInfoGetDR(objCommon, lotStackedWaferInfoGetDRIn);
            retVal = null == objLotStackedWaferInfoGetDROut ? null : objLotStackedWaferInfoGetDROut.getStrStackedWaferInfoSeq();
        } else {
            lotStackedWaferInfoGetDRIn.setBaseLotIDSeq(Collections.singletonList(stackedWaferListInqInParams.getLotID()));
            Outputs.ObjLotStackedWaferInfoGetDROut objLotStackedWaferInfoGetDROut = lotMethod.lotStackedWaferInfoGetDR(objCommon, lotStackedWaferInfoGetDRIn);

            if (stackedWaferListInqInParams.getBaseLotSearchFlag()) {
                log.info("baseLotSearchFlag = TRUE");
                Set<ObjectIdentifier> topLotIDSeq = new HashSet<>();
                topLotIDSeq.add(stackedWaferListInqInParams.getLotID());
                if (!CimObjectUtils.isEmpty(objLotStackedWaferInfoGetDROut)) {
                    Optional.ofNullable(objLotStackedWaferInfoGetDROut.getStrStackedWaferInfoSeq()).ifPresent(list -> list.forEach(data -> {
                        topLotIDSeq.add(data.getTopLotID());
                    }));
                }
                lotStackedWaferInfoGetDRIn.setBaseLotIDSeq(null);
                lotStackedWaferInfoGetDRIn.setTopLotIDSeq(new ArrayList<>(topLotIDSeq));
                objLotStackedWaferInfoGetDROut = lotMethod.lotStackedWaferInfoGetDR(objCommon, lotStackedWaferInfoGetDRIn);

                if (null != objLotStackedWaferInfoGetDROut) {
                    if (topLotIDSeq.size() > 0 && CimArrayUtils.getSize(objLotStackedWaferInfoGetDROut.getBaseLotIDSeq()) > 0) {
                        lotStackedWaferInfoGetDRIn.setBaseLotIDSeq(objLotStackedWaferInfoGetDROut.getBaseLotIDSeq());
                        lotStackedWaferInfoGetDRIn.setTopLotIDSeq(new ArrayList<>(topLotIDSeq));
                        try {
                            objLotStackedWaferInfoGetDROut = lotMethod.lotStackedWaferInfoGetDR(objCommon, lotStackedWaferInfoGetDRIn);
                        } catch (ServiceException e) {
                            if (!Validations.isEquals(e.getCode(), retCodeConfigEx.getNotFoundStackedWafer())) {
                                throw e;
                            }
                        }
                    }
                }
            }

            //------------------------------------------------------
            // Remove Scrapped Wafers
            //------------------------------------------------------
            if (null == objLotStackedWaferInfoGetDROut) return null;
            List<Infos.StackedWaferInfo> stackedWaferInfos = new ArrayList<>();
            Optional.of(objLotStackedWaferInfoGetDROut).ifPresent(out -> out.getStrStackedWaferInfoSeq().forEach(data -> {
                if (!CimStringUtils.equals(data.getState(), BizConstant.SP_SCRAPSTATE_SCRAP)
                        && !CimStringUtils.equals(data.getState(), BizConstant.SP_SCRAPSTATE_GARBAGE)) {
                    stackedWaferInfos.add(data);
                }
            }));
            retVal = new ArrayList<>(stackedWaferInfos);
        }
        return retVal;
    }
    /*@Autowired
    public LotListInBondingFlowInqService(RetCodeConfig retCodeConfig,
                                          IEquipmentMethod equipmentMethod,
                                          ILotInBondingFlowMethod lotInBondingFlowMethod,
                                          IBondingFlowMethod bondingFlowMethod) {
        this.retCodeConfig = retCodeConfig;
        this.equipmentMethod = equipmentMethod;
        this.lotInBondingFlowMethod = lotInBondingFlowMethod;
        this.bondingFlowMethod = bondingFlowMethod;
    }*/

    public List<Infos.LotInBondingFlowInfo> sxLotListInBondingFlowInq(Infos.ObjCommon objCommon,
                                                                      Params.LotListInBondingFlowInqInParams params) {
        ObjectIdentifier targetEquipmentID = ObjectIdentifier.emptyIdentifier();
        ObjectIdentifier baseLotID = ObjectIdentifier.emptyIdentifier();
        List<Infos.HashedInfo> strSearchConditionSeq = params.getStrSearchConditionSeq();
        int nLen = CimArrayUtils.getSize(strSearchConditionSeq);
        if (CimArrayUtils.isNotEmpty(strSearchConditionSeq)) {
            for (Infos.HashedInfo hashedInfo : strSearchConditionSeq) {
                String hashKey = hashedInfo.getHashKey();
                String hashData = hashedInfo.getHashData();
                if (CimStringUtils.equals(hashKey, BizConstant.SP_HASHKEY_TARGETEQUIPMENTID)) {
                    targetEquipmentID.setValue(hashData);
                } else if (CimStringUtils.equals(hashKey, BizConstant.SP_HASHKEY_LOTID)) {
                    baseLotID.setValue(hashData);
                } else {
                    Validations.check(!CimStringUtils.equalsIn(hashKey,
                            BizConstant.SP_HASHKEY_SECTIONNAME,
                            BizConstant.SP_HASHKEY_PRODUCTID,
                            BizConstant.SP_HASHKEY_LOTTYPE,
                            BizConstant.SP_HASHKEY_SUBLOTTYPE),
                            retCodeConfig.getInvalidParameter());
                }
            }
        }

        boolean isTargetEquipment = CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(targetEquipmentID));
        boolean isBasedLot = CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(baseLotID));
        Validations.check(!isTargetEquipment && !isBasedLot, retCodeConfig.getInvalidParameter());
        Validations.check(isTargetEquipment && isBasedLot, retCodeConfig.getInvalidSearchCondition());
        Validations.check(isBasedLot && nLen > 1, retCodeConfig.getInvalidSearchCondition());

        if (isTargetEquipment) {
            equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, targetEquipmentID);
        }

        /*------------------------------------------------------------------*/
        /*   bSiviewFlag is FALSE ( RTD I/F is used )                       */
        /*------------------------------------------------------------------*/
        if (isTargetEquipment && !params.isBSiviewFlag()) {
            /*------------------------*/
            /*   Call RTD Interface   */
            /*------------------------*/
            //@TODO - no RTD implementation
            List<Infos.LotInBondingFlowInfo> lotInBondingFlowInfos = lotInBondingFlowMethod.lotsInBondingFlowRTDInterfaceReq(objCommon,
                    BizConstant.SP_RTD_FUNCTION_CODE_LOTSINBONDINGFLOW, targetEquipmentID);

            /*------------------------------------------------------------------*/
            /*   call lotsInBondingFlow_Info_GetDR                              */
            /*------------------------------------------------------------------*/
            return lotInBondingFlowMethod.lotsInBondingFlowLotInfoGetDR(objCommon, strSearchConditionSeq, lotInBondingFlowInfos);
            /*------------------------------------------------------------------*/
            /*   bSiviewFlag is FALSE ( Siview Logic is used )                  */
            /*------------------------------------------------------------------*/
        } else {
            /*------------------------------------------------------------------*/
            /*   call bondingFlow_lotList_GetDR                                 */
            /*------------------------------------------------------------------*/
            return bondingFlowMethod.bondingFlowLotListGetDR(objCommon, strSearchConditionSeq);
        }
    }

    /*@Autowired
    public EqpCandidateForBondingInqService(RetCodeConfig retCodeConfig, ILotMethod lotMethod, IBondingGroupMethod bondingGroupMethod,
                                            IProcessMethod processMethod, IEquipmentMethod equipmentMethod) {
        this.retCodeConfig = retCodeConfig;
        this.lotMethod = lotMethod;
        this.bondingGroupMethod = bondingGroupMethod;
        this.processMethod = processMethod;
        this.equipmentMethod = equipmentMethod;
    }*/

    public List<Infos.AreaEqp> sxEqpCandidateForBondingInq(Infos.ObjCommon objCommon, Params.EqpCandidateForBondingInqInParams eqpCandidateForBondingInqInParams) {
        Validations.check(null == objCommon || null == eqpCandidateForBondingInqInParams, retCodeConfig.getInvalidInputParam());
        List<String> bondingGroupIDSeq = eqpCandidateForBondingInqInParams.getBondingGroupIDSeq();

        AtomicBoolean firstTime = new AtomicBoolean(true);
        Set<ObjectIdentifier> tmpEqpSeq = new HashSet<>();
        Optional.ofNullable(bondingGroupIDSeq).ifPresent(bondingGroupIDs -> {
            for (String bondingGroupID : bondingGroupIDs) {
                //------------------------------
                // bondingGroup_info_GetDR
                //------------------------------
                Outputs.ObjBondingGroupInfoGetDROut bondingGroupInfoGetDROut = bondingGroupMethod.bondingGroupInfoGetDR(objCommon, bondingGroupID, true);
                Optional.ofNullable(bondingGroupInfoGetDROut).flatMap(objBondingGroupInfoGetDROut -> Optional.ofNullable(objBondingGroupInfoGetDROut.getBondingLotIDList())).ifPresent(lotIDs -> {
                    for (ObjectIdentifier lotID : lotIDs) {
                        //---------------------------------------
                        // Get Target Operation information
                        //---------------------------------------
                        Outputs.ObjLotBondingOperationInfoGetDROut lotBondingOperationInfoGetDROut = lotMethod.lotBondingOperationInfoGetDR(objCommon, lotID);
                        if (null == lotBondingOperationInfoGetDROut) continue;

                        //----------------------------------------------------
                        // Get FPC Information of the Lot at Target Operation
                        //----------------------------------------------------
                        Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo = lotMethod.lotFPCdispatchEqpInfoGet(objCommon, lotID,
                                lotBondingOperationInfoGetDROut.getTargetRouteID(),
                                lotBondingOperationInfoGetDROut.getTargetOperationNumber());

                        //***********************************************/
                        //*    Call process_dispatchEquipments_GetDR    */
                        //***********************************************/
                        List<ObjectIdentifier> equipmentIDs = processMethod.processDispatchEquipmentsGetDR(objCommon,
                                lotBondingOperationInfoGetDROut.getProductID(),
                                lotBondingOperationInfoGetDROut.getTargetOperationID());

                        if (null == fpcDispatchEqpInfo) continue;
                        if (firstTime.get()) {
                            log.info("For First Time");
                            firstTime.set(false);
                            tmpEqpSeq.addAll(fpcDispatchEqpInfo.getDispatchEqpIDs());
                            if (!fpcDispatchEqpInfo.getRestrictEqpFlag()) {
                                tmpEqpSeq.addAll(equipmentIDs);
                            }
                        } else if (tmpEqpSeq.size() == 0) {
                            break;
                        } else {
                            for (ObjectIdentifier tmpEqp : tmpEqpSeq) {
                                boolean eqpFound = false;
                                for (ObjectIdentifier dispatchEqpID : fpcDispatchEqpInfo.getDispatchEqpIDs()) {
                                    if (ObjectIdentifier.equalsWithValue(tmpEqp, dispatchEqpID)) {
                                        eqpFound = true;
                                        break;
                                    }
                                }
                                if (eqpFound) continue;
                                if (!fpcDispatchEqpInfo.getRestrictEqpFlag()) {
                                    for (ObjectIdentifier equipmentID : equipmentIDs) {
                                        if (ObjectIdentifier.equalsWithValue(tmpEqp, equipmentID)) {
                                            eqpFound = true;
                                            break;
                                        }
                                    }
                                    if (eqpFound) continue;
                                }
                                tmpEqpSeq.remove(tmpEqp);
                            }
                        }
                    }
                });
            }
        });

        // call equipment_listInfo_GetDR__160
        List<Infos.AreaEqp> retVal = new ArrayList<>();
        Optional.of(tmpEqpSeq).ifPresent(eqps -> eqps.forEach(eqp -> {
            Infos.EquipmentListInfoGetDRIn equipmentListInfoGetDRIn = new Infos.EquipmentListInfoGetDRIn();
            equipmentListInfoGetDRIn.setEquipmentID(eqp);
            equipmentListInfoGetDRIn.setWhiteDefSearchCriteria(BizConstant.SP_WHITEDEF_SEARCHCRITERIA_ALL);
            Infos.EquipmentListInfoGetDROut equipmentListInfoGetDROut = equipmentMethod.equipmentListInfoGetDR(objCommon, equipmentListInfoGetDRIn);
            Optional.ofNullable(equipmentListInfoGetDROut).ifPresent(out -> {
                if (CimArrayUtils.getSize(out.getStrAreaEqp()) > 0 && CimStringUtils.equals(out.getStrAreaEqp().get(0).getEquipmentCategory(), BizConstant.SP_MC_CATEGORY_WAFERBONDING)) {
                    retVal.add(out.getStrAreaEqp().get(0));
                }
            });
        }));
        return retVal;
    }
    /*@Autowired
    public BondingLotListInqService(IEquipmentMethod equipmentMethod,
                                    IBondingLotMethod bondingLotMethod,
                                    RetCodeConfigEx retCodeConfigEx) {
        this.equipmentMethod = equipmentMethod;
        this.bondingLotMethod = bondingLotMethod;
        this.retCodeConfigEx = retCodeConfigEx;
    }*/

    public List<Infos.BondingLotAttributes> sxBondingLotListInq(Infos.ObjCommon objCommon,
                                                                Params.BondingLotListInqInParams bondingLotListInqInParams) {
        ObjectIdentifier targetEquipmentID = bondingLotListInqInParams.getTargetEquipmentID();
        if (!CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(targetEquipmentID))) {
            equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, targetEquipmentID);
        }

        /*------------------------------------------------------------------------*/
        /*   call bondingLotList_FillInTxPCQ039DR                                 */
        /*------------------------------------------------------------------------*/
        try {
            return bondingLotMethod.bondingLotListFillInTxPCQ039DR(objCommon, bondingLotListInqInParams);
        } catch (ServiceException se) {
            if (retCodeConfigEx.getNoWipLot().getCode() != se.getCode()) {
                throw se;
            }
        }
        return Collections.emptyList();
    }

    public List<Infos.BondingGroupInfo> sxBondingGroupListInq(Infos.ObjCommon objCommon, Params.BondingGroupListInqInParams bondingGroupListInqInParams) {
        Validations.check(null == bondingGroupListInqInParams, retCodeConfig.getInvalidParameter());
        ObjectIdentifier targetEquipmentID = bondingGroupListInqInParams.getTargetEquipmentID();
        if (!ObjectIdentifier.isEmptyWithValue(targetEquipmentID)) {
            log.info("Check Transaction ID and equipment Category combination.");
            equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, targetEquipmentID);
        } else {
            if (CimStringUtils.isEmpty(bondingGroupListInqInParams.getBondingGroupID())
                    && CimStringUtils.isEmpty(bondingGroupListInqInParams.getBondingGroupState())
                    && ObjectIdentifier.isEmptyWithValue(bondingGroupListInqInParams.getControlJobID())
                    && ObjectIdentifier.isEmptyWithValue(bondingGroupListInqInParams.getUpdateUserID())
                    && ObjectIdentifier.isEmptyWithValue(bondingGroupListInqInParams.getLotID())
                    && ObjectIdentifier.isEmptyWithValue(bondingGroupListInqInParams.getBaseProductID())
                    && ObjectIdentifier.isEmptyWithValue(bondingGroupListInqInParams.getTopProductID())) {
                log.error("No search parameter specified.");
                throw new ServiceException(retCodeConfig.getInvalidSearchCondition());
            }
        }
        /*----------------------------------------------------------------*/
        /*   call bondingGroup_list_GetDR                                 */
        /*----------------------------------------------------------------*/
        return bondingGroupMethod.bondingGroupListGetDR(objCommon, bondingGroupListInqInParams);
    }

    public List<String> sxBondingFlowSectionListInq(Infos.ObjCommon objCommon) {
        return bondingFlowMethod.bondingFlowBondingFlowNameGetDR(objCommon);
    }
}
