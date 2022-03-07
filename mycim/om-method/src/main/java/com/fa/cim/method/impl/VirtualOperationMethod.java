package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.runtime.cassette.CimCassetteDO;
import com.fa.cim.entity.runtime.code.CimCodeDO;
import com.fa.cim.entity.runtime.controljob.CimControlJobDO;
import com.fa.cim.entity.runtime.logicalrecipe.CimLogicalRecipeDO;
import com.fa.cim.entity.runtime.logicalrecipe.CimLogicalRecipeDSetDO;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.entity.runtime.lot.CimLotToNoteDO;
import com.fa.cim.entity.runtime.lotcomment.CimLotCommentDO;
import com.fa.cim.entity.runtime.lotopenote.CimLotOpeNoteDO;
import com.fa.cim.entity.runtime.mrecipe.CimMachineRecipeDO;
import com.fa.cim.entity.runtime.mrecipe.CimMachineRecipeEqpDO;
import com.fa.cim.entity.runtime.pfx.CimProcessFlowContextPODO;
import com.fa.cim.entity.runtime.pfx.CimProcessFlowContextReturnDO;
import com.fa.cim.entity.runtime.po.CimProcessOperationDO;
import com.fa.cim.entity.runtime.pos.CimProcessOperationSpecificationDO;
import com.fa.cim.entity.runtime.processdefinition.*;
import com.fa.cim.entity.runtime.processflow.CimPFDefinitionListDO;
import com.fa.cim.entity.runtime.processflow.CimPFPosListDO;
import com.fa.cim.entity.runtime.processflow.CimProcessFlowDO;
import com.fa.cim.entity.runtime.productgroup.CimProductGroupDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.globalfunc.CimFrameWorkGlobals;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.restrict.RestrictionManager;
import com.fa.cim.newcore.dto.restriction.Constrain;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * VirtualOperationMethod .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/8/10        ********             PlayBoy               create file
 * 2019/9/23        ######              Neko                Refactor: change retCode to exception
 *
 * @author PlayBoy
 * @since 2018/8/10 11:16
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class VirtualOperationMethod implements IVirtualOperationMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IOperationMethod operationMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IRouteMethod routeMethod;
    
    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IAutoDispatchControlMethod autoDispatchControlMethod;

    @Autowired
    private SorterNewMethod sorterMethod;

    @Autowired
    private RestrictionManager entityInhibitManager;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private CimFrameWorkGlobals cimFrameWorkGlobals;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IConstraintMethod constraintMethod;

    @Autowired
    private IMinQTimeMethod minQTimeMethod;

    @Override
    public Boolean virtualOperationCheckByStartCassette(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList) {

        //Check StartCassette Operation
        Validations.check(CimArrayUtils.isEmpty(startCassetteList),"Empty CassetteList");

        boolean virtualOperationFlag = false;
        startCassetteLoop:
        for (Infos.StartCassette startCassette : startCassetteList) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (CimArrayUtils.isEmpty(lotInCassetteList)) {
                continue;
            }
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                if (!CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {
                    continue;
                }
                //operation_pdType_Get
                Outputs.ObjOperationPdTypeGetOut operationPdTypeOut = operationMethod.operationPdTypeGet(objCommon, lotInCassette.getStartOperationInfo().getOperationID());
                String pdType = operationPdTypeOut.getPdType();
                if (CimStringUtils.equals(pdType, BizConstant.SP_OPEPDTYPE_VIRTUAL)
                        && ObjectIdentifier.isEmptyWithValue(startCassette.getLoadPortID())) {
                    virtualOperationFlag = true;
                    break startCassetteLoop;
                }
            }
        }

        return virtualOperationFlag;
    }

    @Override
    public List<Infos.VirtualOperationLot> virtualOperationLotsGetDR(Infos.ObjCommon objCommon, Inputs.ObjVirtualOperationLotsGetDRIn in) {
        ObjectIdentifier routeID = in.getRouteID();
        String operationNumber = in.getOperationNumber();
        ObjectIdentifier operationID = in.getOperationID();
        String selectCriteria = in.getSelectCriteria();

        int aRowIndex = 0;
        List<String> subLotTypeList = new ArrayList<>();
        List<String> reticleSetList = new ArrayList<>();

        List<Boolean> fpcAppliedList = new ArrayList<>();
        List<Boolean> lotHasAssignedMachineRecipeList = new ArrayList<>();

        List<Infos.VirtualOperationLot> virtualOperationLotList = new ArrayList<>();
        List<Inputs.ObjEntityInhibiteffectiveForLotGetDRIn> tmpEntityInhibitEffectiveForLotGetDRList = new ArrayList<>();

        boolean bondingEqpFlag = false;
        //【step1】search lot by conditions
        log.debug("【step1】search lot by conditions");

        boolean adoptFPCInfo = StandardProperties.OM_DOC_ENABLE_FLAG.isTrue();
        int searchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getIntValue();
        List<CimLotDO> cimLotDOList = new ArrayList<>();
        if (!ObjectIdentifier.isEmptyWithValue(routeID) && !CimStringUtils.isEmpty(operationNumber)) {
            String sql = "SELECT * FROM OMLOT WHERE MAIN_PROCESS_ID = ? AND OPE_NO = ? AND LOT_STATE = ? AND LOT_INV_STATE = ? AND ";
            String state = null;
            if (!CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_HOLD, selectCriteria)) {
                sql = sql +  "LOT_PROCESS_STATE = ?";
                state = BizConstant.SP_LOT_PROCSTATE_WAITING;
            } else {
                sql = sql +  "LOT_HOLD_STATE = ?" ;
                state = CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD;
            }
            cimLotDOList = cimJpaRepository.query(sql, CimLotDO.class, ObjectIdentifier.fetchValue(routeID), operationNumber,
                    CIMStateConst.CIM_LOT_STATE_ACTIVE, BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, state);

        } else if (!ObjectIdentifier.isEmptyWithValue(operationID)) {
            String sql = "SELECT OMLOT.* FROM OMLOT, OMPROPE WHERE OMLOT.PROPE_RKEY = OMPROPE.ID AND OMPROPE.STEP_ID =? \n";
            if (!CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_HOLD, selectCriteria)) {
                sql = sql + " AND LOT_PROCESS_STATE = ? AND LOT_STATE = ? AND LOT_INV_STATE = ?";
                cimLotDOList = cimJpaRepository.query(sql, CimLotDO.class, operationID.getValue(), BizConstant.SP_LOT_PROCSTATE_WAITING,
                        CIMStateConst.CIM_LOT_STATE_ACTIVE, BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR);
            } else {
                sql = sql + " AND LOT_HOLD_STATE = ? AND LOT_STATE = ? AND LOT_INV_STATE = ?";
                cimLotDOList = cimJpaRepository.query(sql, CimLotDO.class, ObjectIdentifier.fetchValue(operationID),
                        CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, CIMStateConst.CIM_LOT_STATE_ACTIVE, BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR);
            }
        }

        for (CimLotDO cimLotDO : cimLotDOList) {
            //【step2】lot finished status check
            log.debug("【step2】lot finished status check");
            if (CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_EMPTIED, cimLotDO.getLotFinishedState())) {
                continue;
            }
            //【step3】when lot is in post process, it's not included in return structure
            log.debug("【step3】when lot is in post process, it's not included in return structure");
            if (CimBooleanUtils.isTrue(cimLotDO.getPostProcessFlag()) && CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED, selectCriteria)) {
                log.debug("lot is in PostProcess and selectCriteria is SAVL");
                continue;
            }
            //【step4】When Lot is hold, it is not included in return structure
            log.debug("【step4】When Lot is hold, it is not included in return structure");
            if (CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED, selectCriteria)
                    && CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, cimLotDO.getLotHoldState())) {
                continue;
            }
            String objLotBondingGroupIDGetDROut = null;
            Boolean isLotInBondingFlow = !CimStringUtils.isEmpty(cimLotDO.getBondFlowName());
            if (isLotInBondingFlow || bondingEqpFlag) {
                log.debug("lot is in a bonding flow section or equipment is WaferBonding.");
                //【step5】get bonding group id
                log.debug("【step5】get bonding group id");
                ObjectIdentifier lotID = new ObjectIdentifier(cimLotDO.getLotID(), cimLotDO.getId());
                objLotBondingGroupIDGetDROut = lotMethod.lotBondingGroupIDGetDR(objCommon, lotID);
                if (null == objLotBondingGroupIDGetDROut) {
                    continue;
                }
                if (CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED, selectCriteria)) {
                    log.debug("SelectCriteria is SAVL");
                    if (CimStringUtils.isEmpty(objLotBondingGroupIDGetDROut)) {
                        //---------------------------------------------------------------------------------
                        //  When the Lot does not belong to any Bonding Group,
                        //  it is not included in return structure if it is in Bonding Flow Section.
                        //---------------------------------------------------------------------------------
                        if (!bondingEqpFlag) {
                            log.debug("Lot is in a Bonding Flow Section: {}", cimLotDO.getBondFlowName());
                            continue;
                        }
                    } else {
                        log.debug("lot belongs to a bonding group: {}", objLotBondingGroupIDGetDROut);
                        //---------------------------------------------------------------------------------
                        //  When the Lot belongs to a Bonding Group,
                        //  it is not included in return structure if the Bonding Group has Error State.
                        //---------------------------------------------------------------------------------
                        //【step6】get bonding group info
                        log.debug("【step】get bonding group info");
                        Outputs.ObjBondingGroupInfoGetDROut objBondingGroupInfoGetDROut = bondingGroupMethod.bondingGroupInfoGetDR(objCommon, objLotBondingGroupIDGetDROut, false);
                        if (null == objBondingGroupInfoGetDROut
                                || null == objBondingGroupInfoGetDROut.getBondingGroupInfo()
                                || CimStringUtils.equals(BizConstant.SP_BONDINGGROUPSTATE_ERROR, objBondingGroupInfoGetDROut.getBondingGroupInfo().getBondingGroupState())) {
                            log.error("Bonding Group Status is null or SP_BondingGroupState_Error");
                            continue;
                        }
                    }
                }
            }

            //【step7】get auto dispatch control information
            log.debug("【step7】get auto dispatch control information");
            Boolean autoDispatchDisableFlag = false;
            Inputs.ObjAutoDispatchControlInfoGetDRIn objAutoDispatchControlInfoGetDRIn = new Inputs.ObjAutoDispatchControlInfoGetDRIn();
            objAutoDispatchControlInfoGetDRIn.setLotID(new ObjectIdentifier(cimLotDO.getLotID(), cimLotDO.getId()));

            List<Infos.LotAutoDispatchControlInfo> objAutoDispatchControlInfoGetDROut = autoDispatchControlMethod.autoDispatchControlInfoGetDR(objCommon, objAutoDispatchControlInfoGetDRIn);

            int lotAutoDispatchControlInfoListSize = CimArrayUtils.getSize(objAutoDispatchControlInfoGetDROut);
            for (int i = 0; i < lotAutoDispatchControlInfoListSize; i++) {
                Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo = objAutoDispatchControlInfoGetDROut.get(i);
                if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), cimLotDO.getRouteID())
                        && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), cimLotDO.getOperationNumber())) {
                    autoDispatchDisableFlag = true;
                    break;
                } else if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), cimLotDO.getRouteID())
                        && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), "*")) {
                    autoDispatchDisableFlag = true;
                    break;
                } else if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), "*")
                        && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), "*")) {
                    autoDispatchDisableFlag = true;
                    break;
                }
            }
            lotHasAssignedMachineRecipeList.add(true);
            String status = null;
            if (CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK, cimLotDO.getLotInventoryState())) {
                status = (CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_EMPTIED, cimLotDO.getLotFinishedState())
                        || CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED, cimLotDO.getLotFinishedState()))
                        ? cimLotDO.getLotFinishedState() : cimLotDO.getLotInventoryState();
            } else {
                if (CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, cimLotDO.getLotHoldState())) {
                    status = (CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_EMPTIED, cimLotDO.getLotFinishedState())
                            || CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED, cimLotDO.getLotFinishedState()))
                            ? cimLotDO.getLotFinishedState() : cimLotDO.getLotHoldState();
                } else {
                    if (CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_LOTCREATED, cimLotDO.getLotState())
                            || CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_RELEASED, cimLotDO.getLotState())
                            || CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_SHIPPED, cimLotDO.getLotState())) {
                        status = cimLotDO.getLotState();
                    } else if (CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_FINISHED, cimLotDO.getLotState())) {
                        status = cimLotDO.getLotFinishedState();
                    } else if (CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, cimLotDO.getLotState())) {
                        status = cimLotDO.getLotProcessState();
                    }
                }
            }
            //【step8】set lot-eqp info on FRLOT_HOLDEQP, if LOT_HOLD_STATE is OnHold
            log.debug("【step8】set lot-eqp info on OMLOT_HOLDEQP, if LOT_HOLD_STATE is OnHold");
            List<Infos.LotEquipmentList> lotEquipmentListList = new ArrayList<>();
            if (CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, cimLotDO.getLotHoldState())) {
                String lotHoldEqpSql = "SELECT OMLOT_HOLDEQP.EQP_ID,  OMLOT_HOLDEQP.EQP_RKEY, OMEQP.DESCRIPTION FROM OMLOT_HOLDEQP, OMEQP "
                        + " WHERE OMLOT_HOLDEQP.REFKEY = ? "
                        + " AND OMEQP.EQP_ID = OMLOT_HOLDEQP.EQP_ID";
                List<Object[]> objectList = cimJpaRepository.query(lotHoldEqpSql, cimLotDO.getId());
                if (!CimArrayUtils.isEmpty(objectList)) {
                    log.debug("OMLOT_HOLDEQP : record found...");
                    for (int i = 0; i < CimArrayUtils.getSize(objectList); i++) {
                        Object[] tmp = objectList.get(i);
                        Infos.LotEquipmentList lotEquipmentList = new Infos.LotEquipmentList();
                        lotEquipmentList.setEquipmentID(new ObjectIdentifier((String) tmp[0], (String) tmp[1]));
                        lotEquipmentList.setEquipmentName((String)tmp[2]);
                        lotEquipmentListList.add(lotEquipmentList);
                    }
                } else {
                    log.debug("OMLOT_HOLDEQP : record not found...");
                    String tempSql = "SELECT OMLOT_EQP.EQP_ID,  OMLOT_EQP.EQP_RKEY, OMEQP.DESCRIPTION FROM OMLOT_EQP, OMEQP "
                            + " WHERE OMLOT_EQP.REFKEY = ? "
                            + " AND OMEQP.EQP_ID = OMLOT_EQP.EQP_ID";
                    List<Object[]> tempList = cimJpaRepository.query(tempSql, cimLotDO.getId());
                    int size  = CimArrayUtils.getSize(tempList);
                    for (int i = 0; i < size; i++) {
                        Object[] tmp = tempList.get(i);
                        Infos.LotEquipmentList lotEquipmentList = new Infos.LotEquipmentList();
                        lotEquipmentList.setEquipmentID(new ObjectIdentifier((String) tmp[0], (String) tmp[1]));
                        lotEquipmentList.setEquipmentName((String)tmp[2]);
                        lotEquipmentListList.add(lotEquipmentList);
                    }
                }
            } else if (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_WAITING, cimLotDO.getLotProcessState())) {
                //【step9】set the queued machines as the next equipments. if lot state is wating.
                log.debug("【step9】set the queued machines as the next equipments. if lot state is wating.");
                String tempSql = "SELECT OMLOT_EQP.EQP_ID,  OMLOT_EQP.EQP_RKEY, OMEQP.DESCRIPTION FROM OMLOT_EQP, OMEQP "
                        + " WHERE OMLOT_EQP.REFKEY = ? "
                        + " AND OMEQP.EQP_ID = OMLOT_EQP.EQP_ID";
                List<Object[]> tempList = cimJpaRepository.query(tempSql, cimLotDO.getId());
                int size  = CimArrayUtils.getSize(tempList);
                for (int i = 0; i < size; i++) {
                    Object[] tmp = tempList.get(i);
                    Infos.LotEquipmentList lotEquipmentList = new Infos.LotEquipmentList();
                    lotEquipmentList.setEquipmentID(new ObjectIdentifier((String) tmp[0], (String) tmp[1]));
                    lotEquipmentList.setEquipmentName((String)tmp[2]);
                    lotEquipmentListList.add(lotEquipmentList);
                }
            } else {
                log.debug("set eqp info on other case.");

                //【bear】yes, it's just set null object in List.
                // virtualOperation_lots_GetDR.dr (line:765 - 770)
                Infos.LotEquipmentList lotEquipmentList = new Infos.LotEquipmentList();
                lotEquipmentListList.add(lotEquipmentList);
            }

            Infos.VirtualOperationLot virtualOperationLot = new Infos.VirtualOperationLot();
            virtualOperationLot.setOperableFlagForCurrentMachineStateFlag(true);
            virtualOperationLot.setLotID(new ObjectIdentifier(cimLotDO.getLotID(), cimLotDO.getId()));
            virtualOperationLot.setLotStatus(status);
            virtualOperationLot.setLotType(cimLotDO.getLotType());
            virtualOperationLot.setFlowBatchID(new ObjectIdentifier(cimLotDO.getFlowBatchID(), cimLotDO.getFlowBatchObj()));
            virtualOperationLot.setProductID(new ObjectIdentifier(cimLotDO.getProductSpecificationID(), cimLotDO.getProductSpecificationObj()));
            virtualOperationLot.setRouteID(new ObjectIdentifier(cimLotDO.getRouteID(), cimLotDO.getRouteObj()));
            virtualOperationLot.setOperationNumber(cimLotDO.getOperationNumber());
            virtualOperationLot.setTotalWaferCount(CimNumberUtils.longValue(cimLotDO.getWaferCount()));
            virtualOperationLot.setLastClaimedTimeStamp(CimDateUtils.getTimestampAsString(cimLotDO.getLastClamiedTimeStamp()));
            virtualOperationLot.setStateChangeTimeStamp(CimDateUtils.getTimestampAsString(cimLotDO.getStateChangeTime()));
            virtualOperationLot.setQueuedTimeStamp(CimDateUtils.getTimestampAsString(cimLotDO.getQueuedTimeStap()));
            virtualOperationLot.setDueTimeStamp(CimDateUtils.getTimestampAsString(cimLotDO.getPlanEndTimeStamp()));
            virtualOperationLot.setInventoryChangeTimeStamp(CimDateUtils.getTimestampAsString(cimLotDO.getInvChangeTimeStamp()));
            virtualOperationLot.setProcessHoldFlag(false);
            virtualOperationLot.setTotalGoodDieCount(0L);
            virtualOperationLot.setRequiredCassetteCategory(cimLotDO.getRequiredCassetteCategory());
            virtualOperationLot.setInPostProcessFlagOfLotFlag(cimLotDO.getPostProcessFlag());
            virtualOperationLot.setBondingFlowSectionName(cimLotDO.getBondFlowName());
            if (null != objLotBondingGroupIDGetDROut) {
                virtualOperationLot.setBondingGroupID(objLotBondingGroupIDGetDROut);
            }
            virtualOperationLot.setMonitorOperationFlag(false);
            virtualOperationLot.setStartSeqNo(0L);
            virtualOperationLot.setAutoDispatchDisableFlag(autoDispatchDisableFlag);
            virtualOperationLot.setReticleExistFlag(false);
            virtualOperationLot.setLotEquipmentListList(lotEquipmentListList);
            virtualOperationLot.setPriorityClass(CimObjectUtils.toString(cimLotDO.getPriorityClass()));
            Infos.LotNoteFlagInfo lotNoteFlagInfo = new Infos.LotNoteFlagInfo();
            lotNoteFlagInfo.setLotCommentFlag(false);
            virtualOperationLot.setLotNoteFlagInfo(lotNoteFlagInfo);

            if (!CimStringUtils.isEmpty(cimLotDO.getCommentID())) {
                log.debug("OMLOT.COMMENT_OBJ is not null");
                String commentID = cimLotDO.getCommentID();
                String thekeyPos = commentID.substring(commentID.indexOf("#") + 1);
                CimLotCommentDO cimLotCommentDOExample = new CimLotCommentDO();
                cimLotCommentDOExample.setId(thekeyPos);
                CimLotCommentDO cimLotCommentDO = cimJpaRepository.findOne(Example.of(cimLotCommentDOExample)).orElse(null);
                if (null != cimLotCommentDO
                        && !CimStringUtils.isEmpty(cimLotCommentDO.getNoteContents())
                        && !CimStringUtils.equals(BizConstant.SP_LOTCOMMENT_CONTENTS_NULLSTRING, cimLotCommentDO.getNoteContents())) {
                    log.debug("OMLOTCOMMENT.NOTE_CONTENTS is not null!");
                    lotNoteFlagInfo.setLotCommentFlag(true);
                }
            }

            //virtualOperation_lots_GetDR.dr (line:864 - 887)
            log.debug("set lot_note flag");
            String lotNoteSql = "SELECT * FROM OMLOT_MEMO WHERE OMLOT_MEMO.REFKEY = ? ";
            List lotNodeList = cimJpaRepository.query(lotNoteSql, CimLotToNoteDO.class, cimLotDO.getId());
            Boolean lotNoteFlag = !CimArrayUtils.isEmpty(lotNodeList);
            lotNoteFlagInfo.setLotNoteFlag(lotNoteFlag);

            //virtualOperation_lots_GetDR.dr (line:888 - 911)
            log.debug("set lot_operation_flag");
            String lotOperationNoteSql = "SELECT * FROM OMLOTOPEMEMO WHERE LOT_ID = ? " +
                    " AND MAIN_PROCESS_ID = ? AND OPE_NO = ? ";
            List lotOperationNoteList = cimJpaRepository.query(lotOperationNoteSql, CimLotOpeNoteDO.class, cimLotDO.getLotID(), cimLotDO.getRouteID(), cimLotDO.getOperationNumber());
            Boolean lotOperationNoteFlag = !CimArrayUtils.isEmpty(lotOperationNoteList);
            lotNoteFlagInfo.setLotOperationNoteFlag(lotOperationNoteFlag);

            subLotTypeList.add(cimLotDO.getSubLotType());
            reticleSetList.add(cimLotDO.getReticleSetID());

            Outputs.ObjLotEffectiveFPCInfoGetOut lotEffectiveFPCInfoGetOut = null;
            if (CimBooleanUtils.isTrue(adoptFPCInfo)) {
                ObjectIdentifier dummyID = new ObjectIdentifier();
                try {
                    lotEffectiveFPCInfoGetOut = lotMethod.lotEffectiveFPCInfoGet(objCommon, BizConstant.SP_FPC_EXCHANGETYPE_ALL, dummyID, virtualOperationLot.getLotID());
                } catch (ServiceException e) {
                    continue;
                }
                if (CimBooleanUtils.isTrue(lotEffectiveFPCInfoGetOut.isEquipmentActionRequiredFlag())
                        || CimBooleanUtils.isTrue(lotEffectiveFPCInfoGetOut.isMachineRecipeActionRequiredFlag())
                        || CimBooleanUtils.isTrue(lotEffectiveFPCInfoGetOut.isRecipeParameterActionRequiredFlag())
                        || CimBooleanUtils.isTrue(lotEffectiveFPCInfoGetOut.isDcDefActionRequiredFlag())
                        || CimBooleanUtils.isTrue(lotEffectiveFPCInfoGetOut.isDcSpecActionRequiredFlag())
                        || CimBooleanUtils.isTrue(lotEffectiveFPCInfoGetOut.isReticleActionRequiredFlag())) {
                    fpcAppliedList.add(true);
                } else {
                    fpcAppliedList.add(false);
                }
            } else {
                lotEffectiveFPCInfoGetOut = new Outputs.ObjLotEffectiveFPCInfoGetOut();
                lotEffectiveFPCInfoGetOut.setEquipmentActionRequiredFlag(false);
                lotEffectiveFPCInfoGetOut.setMachineRecipeActionRequiredFlag(false);
                lotEffectiveFPCInfoGetOut.setRecipeParameterActionRequiredFlag(false);
                lotEffectiveFPCInfoGetOut.setDcDefActionRequiredFlag(false);
                lotEffectiveFPCInfoGetOut.setDcSpecActionRequiredFlag(false);
                lotEffectiveFPCInfoGetOut.setReticleActionRequiredFlag(false);
                fpcAppliedList.add(false);
            }

            //【step10】select cassette information
            log.debug("【step10】select cassette information");
            String sql1 = "SELECT OMCARRIER.*\n" +
                    "  FROM OMCARRIER, OMCARRIER_LOT\n" +
                    " WHERE OMCARRIER_LOT.LOT_ID = ?\n" +
                    "   AND OMCARRIER.ID = OMCARRIER_LOT.REFKEY";
            List<CimCassetteDO> cimCassetteDOList = cimJpaRepository.query(sql1, CimCassetteDO.class, cimLotDO.getLotID());
            int cassetteSize = CimArrayUtils.getSize(cimCassetteDOList);
            for (int i = 0; i < cassetteSize; i++) {
                CimCassetteDO cassetteDO = cimCassetteDOList.get(i);
                if (CimBooleanUtils.isTrue(cassetteDO.getPostProcessingFlag())) {
                    log.debug("cassette {} related to Lot {} is in PostProcess,", cassetteDO.getCassetteID(), cimLotDO.getLotID());
                    if (CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED, selectCriteria)) {
                        log.debug("selectCriteria {} is CanBeProcessed or Auto3.", selectCriteria);
                        continue;
                    }
                }
                virtualOperationLot.setCassetteID(new ObjectIdentifier(cassetteDO.getCassetteID(), cassetteDO.getId()));
                virtualOperationLot.setTransferStatus(cassetteDO.getTransferState());
                virtualOperationLot.setTransferReserveUserID(new ObjectIdentifier(cassetteDO.getReserveUserID(), cassetteDO.getReserveUserObj()));
                virtualOperationLot.setMultiLotType(cassetteDO.getMultiLotType());
                virtualOperationLot.setCassetteCategory(cassetteDO.getCassetteCategory());
                virtualOperationLot.setInPostProcessFlagOfCassetteFlag(cassetteDO.getPostProcessingFlag());
                virtualOperationLot.setEquipmentID(null);
                virtualOperationLot.setStockerID(null);
                if (!CimStringUtils.isEmpty(cassetteDO.getTransferState()) && CimStringUtils.equals("E", cassetteDO.getTransferState().substring(0,1))) {
                    virtualOperationLot.setEquipmentID(new ObjectIdentifier(cassetteDO.getEquipmentID(), cassetteDO.getEquipmentObj()));
                } else {
                    virtualOperationLot.setStockerID(new ObjectIdentifier(cassetteDO.getEquipmentID(), cassetteDO.getEquipmentObj()));
                }
                virtualOperationLot.setOperableFlagForMultiRecipeCapabilityFlag(true);
            }

            //【step11】select CTRLJOB information
            log.debug("【step11】select CTRLJOB information");
            CimControlJobDO cjExample = new CimControlJobDO();
            cjExample.setId(cimLotDO.getControlJobObj());
            String sql;
            CimControlJobDO cimControlJobDO = cimJpaRepository.findOne(Example.of(cjExample)).orElse(null);
            if (null != cimControlJobDO) {
                virtualOperationLot.setControlJob(new ObjectIdentifier(cimControlJobDO.getControlJobID(), cimControlJobDO.getId()));
                virtualOperationLot.setProcessReserveEquipmentID(new ObjectIdentifier(cimControlJobDO.getEquipmentID(), cimControlJobDO.getEquipmentObject()));
                virtualOperationLot.setProcessReserveUserID(new ObjectIdentifier(cimControlJobDO.getOwnerID(), cimControlJobDO.getOwnerObject()));
            }
            if (CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED, selectCriteria)) {
                if (cimControlJobDO != null && !CimObjectUtils.isEmpty(cimControlJobDO.getControlJobID())) {
                    log.debug("controlJob is not null!");
                    continue;
                }
            }
            //【step12】set sorter information
            log.debug("【step12】set sorter information");
            Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new Params.ObjSorterJobListGetDRIn();
            objSorterJobListGetDRIn.setCarrierID(virtualOperationLot.getCassetteID());
            objSorterJobListGetDRIn.setLotID(virtualOperationLot.getLotID());
            List<Info.SortJobListAttributes> objSorterJobListGetDROut = sorterMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
            if (CimArrayUtils.isEmpty(objSorterJobListGetDROut)) {
                virtualOperationLot.setSorterJobExistFlag(false);
            } else {
                if (CimStringUtils.equals(BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED, selectCriteria)) {
                    log.debug("sorterJob exists");
                    continue;
                }
                virtualOperationLot.setSorterJobExistFlag(true);
            }

            //【step13】select PO information
            log.debug("【step13】select PO information");

            String poSql = "SELECT * FROM OMPROPE WHERE OMPROPE.ID = ? ";
            CimProcessOperationDO cimProcessOperationDO = cimJpaRepository.queryOne(poSql, CimProcessOperationDO.class, cimLotDO.getProcessOperationObj());
            Validations.check(null == cimProcessOperationDO, "the process operation is null.");
            if (CimStringUtils.isEmpty(cimProcessOperationDO.getAssignRecipeID())) {
                lotHasAssignedMachineRecipeList.set(aRowIndex, false);
            }
            //if current module PF is inactive, search current module POS and PD_ID from an active module PF
            log.debug("if current module PF is inactive, search current module POS and PD_ID from an active module PF");

            String pfSql = "SELECT * FROM OMPRF WHERE ID = ?";
            CimProcessFlowDO cimProcessFlowDO = cimJpaRepository.queryOne(pfSql, CimProcessFlowDO.class, cimProcessOperationDO.getModuleProcessFlowObj());
            Validations.check(null == cimProcessFlowDO, "the process flow is null.");

            if (!CimBooleanUtils.isFalse(cimProcessFlowDO.getState())) {
                log.debug("current module pf is active...");
                // do nothing
            }
            virtualOperationLot.setOperationID(new ObjectIdentifier(cimProcessOperationDO.getProcessDefinitionID(), cimProcessOperationDO.getProcessDefinitionObj()));
            virtualOperationLot.setPlanStartTimeStamp(CimDateUtils.getTimestampAsString(cimProcessOperationDO.getPlanStartTime()));
            virtualOperationLot.setPlanEndTimeStamp(CimDateUtils.getTimestampAsString(cimProcessOperationDO.getPlanEndTime()));
            virtualOperationLot.setPlannedEquipmentID(new ObjectIdentifier(cimProcessOperationDO.getPlanEuipmentID(), cimProcessOperationDO.getPlanEuipmentObj()));

            //【step14】select previous po information
            log.debug("【step14】select previous po information");
            String pfxSql = "SELECT OMPRFCX_PROPESEQ.* FROM OMPRFCX_PROPESEQ, OMPRFCX WHERE  OMPRFCX_PROPESEQ.PROPE_RKEY = OMPRFCX.CUR_PROPE_RKEY AND " +
                    " OMPRFCX_PROPESEQ.REFKEY = OMPRFCX.ID AND OMPRFCX.ID = ?" ;

            CimProcessFlowContextPODO cimProcessFlowContextPODO = cimJpaRepository.queryOne(pfxSql, CimProcessFlowContextPODO.class, cimLotDO.getProcessFlowContextObj());
            Validations.check(null == cimProcessFlowContextPODO, "cimProcessFlowContextPODO is null.");
            Integer sequenceNumber = cimProcessFlowContextPODO.getSequenceNumber() - 1;

            String previousPoSql = "SELECT OMPROPE.* FROM OMPROPE, OMPRFCX_PROPESEQ WHERE OMPRFCX_PROPESEQ.REFKEY = ? " +
                    " AND OMPRFCX_PROPESEQ.IDX_NO = ? AND OMPROPE.ID = OMPRFCX_PROPESEQ.PROPE_RKEY";
            CimProcessOperationDO previousPo = cimJpaRepository.queryOne(previousPoSql, CimProcessOperationDO.class, cimLotDO.getProcessFlowContextObj(), sequenceNumber);
            if (null != previousPo) {
                virtualOperationLot.setPreOperationCompTImeStamp(CimDateUtils.getTimestampAsString(previousPo.getActualEndTime()));
            }

            // calculate internal priority
            log.debug("calculate internal priority");
            Double remainCycleTime = cimProcessOperationDO.getRemainCycleTime();
            Timestamp plannedCompDataTime = cimLotDO.getPlanEndTimeStamp();
            Long plannedCompDuration = CimDateUtils.substractTimeStamp(plannedCompDataTime.getTime(), System.currentTimeMillis());
            if ((null == remainCycleTime) || (0L == remainCycleTime)) {
                virtualOperationLot.setInternalPriority("0");
            } else {
                Double internalPriority = plannedCompDuration / (remainCycleTime * 60 * 1000);
                log.debug("internalPriority: {}", internalPriority);
                if (plannedCompDataTime.before(new Timestamp(System.currentTimeMillis()))) {
                    virtualOperationLot.setInternalPriority(Double.toString(0 - internalPriority));
                } else {
                    virtualOperationLot.setInternalPriority(Double.toString(internalPriority));
                }
            }

            //【step15】select POS information (get photo_layer)
            log.debug("【step15】select POS information (get photo_layer)");
            sql = "SELECT * FROM OMPRF_PRSSSEQ WHERE REFKEY = ? AND LINK_KEY = ? ";
            CimPFPosListDO cimPFPosListDO = cimJpaRepository.queryOne(sql, CimPFPosListDO.class, cimProcessOperationDO.getProcessFlowObj(), cimProcessOperationDO.getOperationNumber() );
            String poslistposObj = cimProcessOperationDO.getModuleProcessOperationSpecificationsObj();
            if (cimPFPosListDO != null){
                poslistposObj = cimPFPosListDO.getProcessOperationSpecificationsObj();
            }
            //【step16】select Module POS information (get mandatory_flag)
            log.debug("【step16】select Module POS information (get mandatory_flag)");
            sql = String.format("SELECT COMPULSORY_FLAG\n" +
                    "                     FROM   OMPRSS\n" +
                    "                     WHERE  OMPRSS.ID = '%s'", poslistposObj);
            CimProcessOperationSpecificationDO cimProcessOperationSpecificationDO = cimJpaRepository.queryOne(sql, CimProcessOperationSpecificationDO.class);
            if (null != cimProcessOperationSpecificationDO) {
                virtualOperationLot.setMandatoryOperationFlag(cimProcessOperationSpecificationDO.getMandatoryFlag());
            }
            //【step17】select Main PF information (get stage_id, stage_obj, current seqno in mainPF)
            log.debug("【step17】select Main PF information (get stage_id, stage_obj, current seqno in mainPF)");
            sql = "SELECT * FROM OMPRF_ROUTESEQ WHERE REFKEY = ? AND LINK_KEY = ?";
            CimPFDefinitionListDO cimPFDefinitionListDO = cimJpaRepository.queryOne(sql, CimPFDefinitionListDO.class, cimProcessOperationDO.getMainProcessFlowObj(), cimProcessOperationDO.getModuleNumber());
            if (null != cimPFDefinitionListDO) {
                virtualOperationLot.setStageID(new ObjectIdentifier(cimPFDefinitionListDO.getStageID(), cimPFDefinitionListDO.getStageObj()));
            }

            //【step18】check lot's routeID and operation No And productID
            log.debug("【step18】check lot's routeID and operation No And productID");
            Boolean entityInhibtInfoCollectedFlag = false;
            Boolean nextOperationInfoRetrievedFlag = false;
            Boolean bondingInfoRetrievedFlag = false;

            int virtualOperationLotListSize = CimArrayUtils.getSize(virtualOperationLotList);
            for (int i = 0; i < virtualOperationLotListSize - 1; i++) {
                Infos.VirtualOperationLot tmpVirtualOperationLot = virtualOperationLotList.get(i);
                if (ObjectIdentifier.isNotEmptyWithValue(tmpVirtualOperationLot.getControlJob())
                        || ObjectIdentifier.isNotEmptyWithValue(virtualOperationLot.getControlJob())) {
                    log.debug("Exist Control Job, Continue !");
                    continue;
                }

                if (ObjectIdentifier.equalsWithValue(tmpVirtualOperationLot.getRouteID(), virtualOperationLot.getRouteID())
                        && CimStringUtils.equals(tmpVirtualOperationLot.getOperationNumber(), virtualOperationLot.getOperationNumber())
                        && ObjectIdentifier.equalsWithValue(tmpVirtualOperationLot.getProductID(), virtualOperationLot.getProductID())
                        && ObjectIdentifier.equalsWithValue(tmpVirtualOperationLot.getOperationID(), virtualOperationLot.getProductID())) {
                    log.debug("same type lot found");
                    if (CimBooleanUtils.isFalse(nextOperationInfoRetrievedFlag)) {
                        virtualOperationLot.setNext2EquipmentID(tmpVirtualOperationLot.getNext2EquipmentID());
                        virtualOperationLot.setNext2LogicalRecipeID(tmpVirtualOperationLot.getNext2LogicalRecipeID());
                        virtualOperationLot.setNext2requiredCassetteCategory(tmpVirtualOperationLot.getNext2requiredCassetteCategory());
                        nextOperationInfoRetrievedFlag = true;
                    }

                    if (CimBooleanUtils.isFalse(bondingInfoRetrievedFlag)) {
                        tmpVirtualOperationLot.setBondingCateGory(tmpVirtualOperationLot.getBondingCateGory());
                        tmpVirtualOperationLot.setTopProductID(tmpVirtualOperationLot.getTopProductID());
                        bondingInfoRetrievedFlag = true;
                    }

                    // copy entityInhibit record for accelerate
                    log.debug("copy entityInhibit record for accelerate");
                    String tmpSubLotType = subLotTypeList.get(i);
                    String subLotType = cimLotDO.getSubLotType();      // the subLotTpye is at the end of subLotTypeSeq
                    String tmpReticleSet = reticleSetList.get(i);
                    String reticleSet = cimLotDO.getReticleSetID();    // the reticleSet is at the end of reticleSetList
                    if (CimStringUtils.equals(tmpSubLotType, subLotType)
                            && CimStringUtils.equals(tmpReticleSet, reticleSet)
                            && CimBooleanUtils.isFalse(fpcAppliedList.get(i)
                            && CimBooleanUtils.isFalse(fpcAppliedList.get(virtualOperationLotListSize)))) {
                        log.debug("subLotType & Reticle Matching");
                        log.debug("found same entity inhibit information");

                        Inputs.ObjEntityInhibiteffectiveForLotGetDRIn inhibiteffectiveForLotGetDRIn = new Inputs.ObjEntityInhibiteffectiveForLotGetDRIn();
                        inhibiteffectiveForLotGetDRIn.setLotID(virtualOperationLot.getLotID());
                        inhibiteffectiveForLotGetDRIn.setStrEntityInhibitInfos(tmpEntityInhibitEffectiveForLotGetDRList.get(i).getStrEntityInhibitInfos());
                        List<Infos.EntityInhibitInfo>  strEntityInhibitEffectiveForLotGetDROut = constraintMethod.constraintEffectiveForLotGetDR(objCommon, inhibiteffectiveForLotGetDRIn.getStrEntityInhibitInfos(), inhibiteffectiveForLotGetDRIn.getLotID());

                        int inhibitSize = CimArrayUtils.getSize(strEntityInhibitEffectiveForLotGetDROut);
                        List<Infos.EntityInhibitAttributes> entityInhibitAttributesList = new ArrayList<>();
                        for (int j = 0; j <inhibitSize; j++) {
                            Infos.EntityInhibitInfo entityInhibitInfo = strEntityInhibitEffectiveForLotGetDROut.get(j);
                            entityInhibitAttributesList.add(entityInhibitInfo.getEntityInhibitAttributes());
                        }
                        virtualOperationLot.setEntityInhibitAttributesList(entityInhibitAttributesList);
                        entityInhibtInfoCollectedFlag = true;
                        break;
                    }
                }
            }

            //【step19】select next to eqp information
            log.debug("【step19】select next to eqp information");
            CimProductSpecificationDO cimProductSpecificationDO = null;
            String pdObj = null;
            String cassettCategory = null;
            if (CimBooleanUtils.isFalse(nextOperationInfoRetrievedFlag)) {
                log.debug("nextOperationInfoRetrievedFlag = false");
                Inputs.ObjProcessOperationProcessRefListForLotIn objProcessOperationProcessRefListForLotIn = new Inputs.ObjProcessOperationProcessRefListForLotIn();
                ObjectIdentifier searchRouteDummy = new ObjectIdentifier();
                objProcessOperationProcessRefListForLotIn.setSearchDirection(true);
                objProcessOperationProcessRefListForLotIn.setPosSearchFlag(true);
                objProcessOperationProcessRefListForLotIn.setSearchCount(1);
                objProcessOperationProcessRefListForLotIn.setSearchRouteID(searchRouteDummy);
                objProcessOperationProcessRefListForLotIn.setSearchOperationNumber("");
                objProcessOperationProcessRefListForLotIn.setCurrentFlag(false);
                objProcessOperationProcessRefListForLotIn.setLotID(new ObjectIdentifier(cimLotDO.getLotID(), cimLotDO.getId()));
                List<Infos.OperationProcessRefListAttributes> processOperationProcessRefListForLotRetCode= null;
                try {
                    processOperationProcessRefListForLotRetCode = processMethod.processOperationProcessRefListForLot(objCommon, objProcessOperationProcessRefListForLotIn);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getSomeopelistDataError(), e.getCode())){
                        throw e;
                    }
                }
                String strMainPos = cimProcessOperationDO.getModuleProcessOperationSpecificationsObj();
                if (!CimArrayUtils.isEmpty(processOperationProcessRefListForLotRetCode)){
                    strMainPos = processOperationProcessRefListForLotRetCode.get(0).getProcessRef().getProcessOperationSpecification();
                }
                sql = String.format("SELECT *\n" +
                        "                         FROM   OMPRSS\n" +
                        "                         WHERE  OMPRSS.ID = '%s'", strMainPos);
                CimProcessOperationSpecificationDO mainPos  = cimJpaRepository.queryOne(sql, CimProcessOperationSpecificationDO.class);
                if (null == mainPos) {
                    log.debug("MainPOS is not found.");
                } else {
                    cassettCategory = mainPos.getRequiredCassetteCategory();
                }
                String strModulePos = cimProcessOperationDO.getModuleProcessOperationSpecificationsObj();;
                if (!CimArrayUtils.isEmpty(processOperationProcessRefListForLotRetCode)){
                    strModulePos = processOperationProcessRefListForLotRetCode.get(0).getProcessRef().getModulePOS();
                }
                sql = String.format("SELECT *\n" +
                        "                         FROM   OMPRSS\n" +
                        "                         WHERE  OMPRSS.ID = '%s'", strModulePos);
                CimProcessOperationSpecificationDO modulePos = cimJpaRepository.queryOne(sql, CimProcessOperationSpecificationDO.class);
                if (null == modulePos) {
                    log.debug("modulePos is not found.");
                    Validations.check(true,"modulePos is not found");
                } else {
                    pdObj = modulePos.getProcessDefinitionObj();
                }

                sql = "SELECT OMPRP_EQPSETPRD.* FROM OMPRP_EQPSETPRD WHERE OMPRP_EQPSETPRD.PROD_ID = ? AND OMPRP_EQPSETPRD.REFKEY = ? ";
                CimPDSpecEqpDO cimPDSpecEqpDO = null;
                cimPDSpecEqpDO = cimJpaRepository.queryOne(sql, CimPDSpecEqpDO.class, cimLotDO.getProductSpecificationID(), modulePos.getProcessDefinitionObj());

                //provide specific eqp by product group and technology
                log.debug("provide specific eqp by product group and technology");
                Boolean specificEqpFlag = false;
                Boolean specificEqpByProduct = false;
                Boolean specificEqpByProductGroup = false;
                Boolean specificEqpByTechnology = false;

                //【step20】search specific eqp by product group
                log.debug("【step20】search specific eqp by product group");
                CimPDSpecEqpPgrpDO cimPDSpecEqpPgrpDO = null;
                CimProductGroupDO cimProductGroupDO = null;
                CimPDSpecEqpTechDO cimPDSpecEqpTechDO = null;
                if (null == cimPDSpecEqpDO) {
                    //Search Logic of OMPRODINFO by Product Spec ID for get Product Group
                    log.debug("Search Logic of OMPRODINFO by Product Spec ID for get Product Group");
                    //line:1893 - 2019
                    sql = "SELECT OMPRODINFO.* FROM OMPRODINFO WHERE OMPRODINFO.PROD_ID = ? AND OMPRODINFO.ID = ? ";
                    cimProductSpecificationDO = cimJpaRepository.queryOne(sql, CimProductSpecificationDO.class, cimLotDO.getProductSpecificationID(), cimLotDO.getProductSpecificationObj());

                    // search logic of FRPD_SPECEQPPGRP by product group
                    log.debug("search logic of OMPRP_EQPSETPRODFMLY by product group");
                    sql = "SELECT OMPRP_EQPSETPRODFMLY.*  FROM OMPRP_EQPSETPRODFMLY WHERE OMPRP_EQPSETPRODFMLY.PRODFMLY_ID = ? AND OMPRP_EQPSETPRODFMLY.REFKEY = ?" ;
                    cimPDSpecEqpPgrpDO = cimJpaRepository.queryOne(sql, CimPDSpecEqpPgrpDO.class, cimProductSpecificationDO.getProductGroupID(), modulePos.getProcessDefinitionObj());

                    // search specific eqp by technology
                    log.debug("search specific eqp by technology");
                    if (null == cimPDSpecEqpPgrpDO) {
                        // Search Logic of OMPRODFMLY by Product Group ID for get Technology
                        log.debug("Search Logic of OMPRODFMLY by Product Group ID for get Technology");
                        log.debug("start search specific eqp by technology");
                        sql = "SELECT OMPRODFMLY.* FROM OMPRODFMLY WHERE OMPRODFMLY.PRODFMLY_ID = ? AND OMPRODFMLY.ID = ?";
                        cimProductGroupDO = cimJpaRepository.queryOne(sql, CimProductGroupDO.class,  cimProductSpecificationDO.getProductGroupID(), cimProductSpecificationDO.getProductGroupObj());

                        // search logic of FRPD_SPECEQPTECH by Technology
                        log.debug("search logic of OMPRP_EQPSETTECH by Technology");

                        sql = "SELECT OMPRP_EQPSETTECH.* FROM OMPRP_EQPSETTECH WHERE  OMPRP_EQPSETTECH.TECH_ID = ? AND OMPRP_EQPSETTECH.REFKEY = ?";
                        cimPDSpecEqpTechDO = cimJpaRepository.queryOne(sql, CimPDSpecEqpTechDO.class, cimProductGroupDO.getTechnologyID(), modulePos.getProcessDefinitionObj());
                        if (null != cimPDSpecEqpTechDO) {
                            specificEqpFlag = true;
                            specificEqpByTechnology = true;
                        }
                    } else {
                        log.debug("specific eqp by product group");
                        specificEqpFlag = true;
                        specificEqpByProductGroup = true;
                    }
                } else {
                    log.debug("specific eqp by product");
                    specificEqpFlag = true;
                    specificEqpByProduct = true;
                }

                if (CimBooleanUtils.isTrue(specificEqpFlag)) {
                    String tempNextToEqp = null;
                    Integer tempNextToEqpOrder = null;
                    if (CimBooleanUtils.isTrue(specificEqpByProduct)) {
                        tempNextToEqp = "PosProcessDefinition_SpecificMachines_machines";
                        tempNextToEqpOrder = CimObjectUtils.isEmpty(cimPDSpecEqpDO) ? null : cimPDSpecEqpDO.getSequenceNumber();
                    } else if (CimBooleanUtils.isTrue(specificEqpByProductGroup)) {
                        tempNextToEqp = "PosProcessDefinition_SpecificMachinesByProductGroup_machines";
                        tempNextToEqpOrder = CimObjectUtils.isEmpty(cimPDSpecEqpPgrpDO) ? null : cimPDSpecEqpPgrpDO.getSequenceNumber();
                    } else if (CimBooleanUtils.isTrue(specificEqpByTechnology)) {
                        tempNextToEqp = "PosProcessDefinition_SpecificMachinesByTechnology_machines";
                        tempNextToEqpOrder = cimPDSpecEqpTechDO.getSequenceNumber();
                    }

                    // judgement of specific eqp function of product, product group or technology
                    log.debug("judgement of specific eqp function of product, product group or technology");
                    List<Object[]> objects = null;
                    if (CimBooleanUtils.isTrue(specificEqpByProduct)) {
                        sql = " SELECT OMPRP_EQPSETPRD_ASGN.EQP_ID,OMPRP_EQPSETPRD_ASGN.EQP_RKEY FROM OMPRP_EQPSETPRD_ASGN WHERE OMPRP_EQPSETPRD_ASGN.REFKEY = ? " +
                                "AND (OMPRP_EQPSETPRD_ASGN.LINK_MARKER = ? OR OMPRP_EQPSETPRD_ASGN.LINK_MARKER = ?) ";
                        objects = cimJpaRepository.query(sql, modulePos.getProcessDefinitionObj(), tempNextToEqp, tempNextToEqpOrder);
                        Validations.check(null == objects, "the cimPDSpecEqpEqpDOList is null.");
                    } else if (CimBooleanUtils.isTrue(specificEqpByProductGroup)) {
                        sql = " SELECT OMPRP_EQPSETPRODFMLY_ASGN.EQP_ID,OMPRP_EQPSETPRD_ASGN.EQP_RKEY FROM OMPRP_EQPSETPRODFMLY_ASGN WHERE OMPRP_EQPSETPRODFMLY_ASGN.REFKEY = ? " +
                                "AND (OMPRP_EQPSETPRODFMLY_ASGN.LINK_MARKER = ? OR OMPRP_EQPSETPRODFMLY_ASGN.LINK_MARKER = ?) ";
                        objects = cimJpaRepository.query(sql, modulePos.getProcessDefinitionObj(), tempNextToEqp, tempNextToEqpOrder);
                        Validations.check(null == objects, "the cimPDSpecEqpPgrpEqpDO is null.");
                    } else if (CimBooleanUtils.isTrue(specificEqpByTechnology)) {
                        sql = " SELECT OMPRP_EQPSETTECH_ASGN.EQP_ID,OMPRP_EQPSETPRD_ASGN.EQP_RKEY FROM OMPRP_EQPSETTECH_ASGN WHERE OMPRP_EQPSETTECH_ASGN.REFKEY = ? " +
                                "AND (OMPRP_EQPSETTECH_ASGN.LINK_MARKER = ? OR OMPRP_EQPSETTECH_ASGN.LINK_MARKER = ?) ";
                        objects = cimJpaRepository.query(sql, modulePos.getProcessDefinitionObj(), tempNextToEqp, tempNextToEqpOrder);
                        Validations.check(null == objects, "the cimPDSpecEqpTechEqpDO is null.");
                    }

                    for (int i = 0; i < CimArrayUtils.getSize(objects); i++) {
                        Boolean logicalRecipeFoundFlag = false;
                        String tmpSpecEqpID = (String)objects.get(i)[0];
                        String tmpSpecEqpObj = (String)objects.get(i)[1];

                        ObjectIdentifier logicalRecipeID = null;
                        sql = "SELECT OMPRP_LRPRD.* FROM OMPRP_LRPRD WHERE OMPRP_LRPRD.REFKEY = ? AND OMPRP_LRPRD.PROD_ID = ?";
                        CimPDLcRecipeDO cimPDLcRecipeDO = cimJpaRepository.queryOne(sql, CimPDLcRecipeDO.class, modulePos.getProcessDefinitionObj(), cimLotDO.getProductSpecificationID());
                        if (null != cimPDLcRecipeDO && !CimStringUtils.isEmpty(cimPDLcRecipeDO.getRecipeID())) {
                            logicalRecipeFoundFlag = true;
                            logicalRecipeID = new ObjectIdentifier(cimPDLcRecipeDO.getRecipeID(), cimPDLcRecipeDO.getRecipeObj());
                        } else {
                            log.debug("logical recipe is not found.");
                        }

                        // search logical recipe by product group
                        if (CimBooleanUtils.isFalse(logicalRecipeFoundFlag)) {
                            sql = String.format("SELECT PRODFMLY_ID\n" +
                                    "                                          FROM OMPRODINFO\n" +
                                    "                                          WHERE PROD_ID = '%s'", cimLotDO.getProductSpecificationID());
                            CimProductSpecificationDO cimProductSpecificationDO1 = cimJpaRepository.queryOne(sql, CimProductSpecificationDO.class);
                            if (null == cimProductSpecificationDO1) {
                                log.debug("OMPRODINFO is NOT_FOUND");
                            } else {
                                sql = "SELECT * FROM OMPRP_LRPRODFMLY WHERE REFKEY = ? AND PRODFMLY_ID = ?";
                                CimPDLcRecipeProductGroupDO cimPDLcRecipeProductGroupDO = cimJpaRepository.queryOne(sql, CimPDLcRecipeProductGroupDO.class, modulePos.getProcessDefinitionObj(), cimProductSpecificationDO1.getProductGroupID());
                                if (null != cimPDLcRecipeProductGroupDO && !CimStringUtils.isEmpty(cimPDLcRecipeProductGroupDO.getRecipeID())) {
                                    logicalRecipeFoundFlag = true;
                                    logicalRecipeID = new ObjectIdentifier(cimPDLcRecipeProductGroupDO.getRecipeID(), cimPDLcRecipeProductGroupDO.getRecipeObj());
                                }
                            }
                        }

                        // search logical recipe by technology
                        log.debug("search logical recipe by technology");
                        if (CimBooleanUtils.isFalse(logicalRecipeFoundFlag)) {
                            sql = String.format("SELECT TECH_ID\n" +
                                    "            FROM   OMPRODFMLY\n" +
                                    "            WHERE  PRODFMLY_ID = '%s'", cimProductSpecificationDO.getProductGroupID());
                            CimProductGroupDO cimProductGroupDO1 = cimJpaRepository.queryOne(sql, CimProductGroupDO.class);
                            if (null == cimProductGroupDO1) {
                                log.debug("OMPRODFMLY is not found.");
                            } else {
                                sql = "SELECT * FROM OMPRP_LRTECH WHERE REFKEY = ? AND TECH_ID = ?";
                                CimPDLcRecipeTechDO cimPDLcRecipeTechDO = cimJpaRepository.queryOne(sql, CimPDLcRecipeTechDO.class, modulePos.getProcessDefinitionObj(), cimProductGroupDO1.getTechnologyID());
                                if (null != cimPDLcRecipeTechDO && !CimStringUtils.isEmpty(cimPDLcRecipeTechDO.getRecipeID())) {
                                    logicalRecipeFoundFlag = true;
                                    logicalRecipeID = new ObjectIdentifier(cimPDLcRecipeTechDO.getRecipeID(), cimPDLcRecipeTechDO.getRecipeObj());
                                }
                            }
                        }

                        // search defalut logical recipe
                        log.debug("search defalut logical recipe");
                        if (CimBooleanUtils.isFalse(logicalRecipeFoundFlag)) {
                            sql = String.format("SELECT LRCP_ID,\n" +
                                    "                                                 LRCP_RKEY\n" +
                                    "                                          FROM   OMPRP\n" +
                                    "                                          WHERE  ID = '%s'", modulePos.getProcessDefinitionObj());
                            CimProcessDefinitionDO cimProcessDefinitionDO = cimJpaRepository.queryOne(sql, CimProcessDefinitionDO.class);
                            if (null != cimProcessDefinitionDO && !CimStringUtils.isEmpty(cimProcessDefinitionDO.getRecipeID())) {
                                logicalRecipeFoundFlag = true;
                                logicalRecipeID = new ObjectIdentifier(cimProcessDefinitionDO.getRecipeID(), cimProcessDefinitionDO.getRecipeObj());
                            } else {
                                log.debug("OMPRP is not found.");
                            }
                        }
                        virtualOperationLot.setNext2EquipmentID(new ObjectIdentifier(tmpSpecEqpID, tmpSpecEqpObj));
                        virtualOperationLot.setNext2LogicalRecipeID(logicalRecipeID);
                        virtualOperationLot.setNext2requiredCassetteCategory(CimObjectUtils.isEmpty(mainPos) ? null : mainPos.getRequiredCassetteCategory());
                        break;
                    }
                } else  {
                    log.debug("OMPRP_EQPSETPRD is NOT_FOUND");
                }
            }


            // line：2579 - 3026
            if (CimBooleanUtils.isFalse(nextOperationInfoRetrievedFlag)) {

                boolean logicalRecipeFoundFlag = false;
                sql = "SELECT OMPRP_LRPRD.* FROM OMPRP_LRPRD WHERE OMPRP_LRPRD.REFKEY = ? AND OMPRP_LRPRD.PROD_ID = ?";
                CimPDLcRecipeDO cimPDLcRecipeDO = cimJpaRepository.queryOne(sql, CimPDLcRecipeDO.class, pdObj, cimLotDO.getProductSpecificationID());
                String logicalRecipeID = null;
                String logicalRecipeObj = null;

                if (null != cimPDLcRecipeDO && CimStringUtils.isNotEmpty(logicalRecipeID)) {
                    logicalRecipeID = cimPDLcRecipeDO.getRecipeID();
                    logicalRecipeObj = cimPDLcRecipeDO.getId();
                    logicalRecipeFoundFlag = true;
                }

                // search logical recipe by product group (line:2627 - 2677)
                if (CimBooleanUtils.isFalse(logicalRecipeFoundFlag)) {
                    CimProductSpecification bo = baseCoreFactory.getBO(CimProductSpecification.class, new ObjectIdentifier(cimLotDO.getProductSpecificationID()));
                    if (null != bo) {
                        sql = "select * from OMPRP_LRPRODFMLY where REFKEY = ? and PRODFMLY_ID = ?";
                        CimPDLcRecipeProductGroupDO cimPDLcRecipeProductGroupDO = cimJpaRepository.queryOne(sql, CimPDLcRecipeProductGroupDO.class, pdObj, cimLotDO.getProductSpecificationID());
                        if (null != cimPDLcRecipeProductGroupDO) {
                            logicalRecipeID = cimPDLcRecipeProductGroupDO.getRecipeID();
                            logicalRecipeObj = cimPDLcRecipeProductGroupDO.getRecipeObj();
                            logicalRecipeFoundFlag = true;
                        }
                    }
                }

                // search logical recipe by Technology (line:2678 - 2728)
                if (CimBooleanUtils.isFalse(logicalRecipeFoundFlag)) {
                    sql = "select * from OMPRODFMLY where PRODFMLY_ID = ?";
                    CimProductGroupDO cimProductGroupDO = cimJpaRepository.queryOne(sql, CimProductGroupDO.class, cimLotDO.getProductSpecificationID());
                    if (null != cimProductGroupDO) {
                        sql = "select * from OMPRP_LRTECH where REFKEY = ? and TECH_ID = ?";
                        CimPDLcRecipeTechDO cimPDLcRecipeTechDO = cimJpaRepository.queryOne(sql, CimPDLcRecipeTechDO.class, pdObj, cimProductGroupDO.getTechnologyID());
                        if (null != cimPDLcRecipeTechDO) {
                            logicalRecipeID = cimPDLcRecipeTechDO.getRecipeID();
                            logicalRecipeObj = cimPDLcRecipeTechDO.getRecipeObj();
                            logicalRecipeFoundFlag = true;
                        }
                    }
                }

                // search default logical recipe (line:2729 - 2754)
                if (CimBooleanUtils.isFalse(logicalRecipeFoundFlag)) {
                    sql = "select * from OMPRP where ID = ?";
                    CimProcessDefinitionDO cimProcessDefinitionDO = cimJpaRepository.queryOne(sql, CimProcessDefinitionDO.class, pdObj);
                    if (null != cimProcessDefinitionDO) {
                        logicalRecipeID = cimProcessDefinitionDO.getRecipeID();
                        logicalRecipeObj = cimProcessDefinitionDO.getRecipeObj();
                        logicalRecipeFoundFlag = true;
                    }
                }

                if (CimStringUtils.isNotEmpty(logicalRecipeID)) {
                    String version = BaseStaticMethod.extractVersionFromID(logicalRecipeID);
                    if (CimStringUtils.equals(BizConstant.SP_ACTIVE_VERSION, version)) {
                        sql = "SELECT B.* FROM  OMLRCP A, OMLRCP B WHERE A.LRCP_ID = ? AND B.LRCP_ID = A.ACTIVE_VER";
                        CimLogicalRecipeDO cimLogicalRecipeDO = cimJpaRepository.queryOne(sql, CimLogicalRecipeDO.class, logicalRecipeID);
                        if (null != cimLogicalRecipeDO) {
                            logicalRecipeID = cimLogicalRecipeDO.getLogicalRecipeID();
                            logicalRecipeObj = cimLogicalRecipeDO.getId();
                        }
                    }
                }

                // line:2781 - 3025
                if (CimStringUtils.isNotEmpty(logicalRecipeObj)) {
                    sql = "SELECT OMPRP_RESTRICTEQP.* FROM OMPRP_RESTRICTEQP WHERE OMPRP_RESTRICTEQP.REFKEY = ? ORDER BY OMPRP_RESTRICTEQP.IDX_NO";
                    List<CimPDEqpDO> pdEqpList = cimJpaRepository.query(sql, CimPDEqpDO.class, pdObj);
                    boolean nextEquipmentFoundFlag = false;
                    if (CimArrayUtils.isEmpty(pdEqpList)) {
                        // get eqp from logicalrecipe (machineRecipe's eqp)
                        CimLogicalRecipe bo = baseCoreFactory.getBO(CimLogicalRecipe.class, new ObjectIdentifier(logicalRecipeID));
                        Validations.check(null == bo, new OmCode(retCodeConfig.getNotFoundLogicalRecipe(), logicalRecipeID));

                        sql = "select * from OMLRCP_DFLT where REFKEY = ?";
                        List<CimLogicalRecipeDSetDO> logicalRecipeDSetList = cimJpaRepository.query(sql, CimLogicalRecipeDSetDO.class, bo.getPrimaryKey());
                        if (CimArrayUtils.isNotEmpty(logicalRecipeDSetList)) {
                            for (CimLogicalRecipeDSetDO cimLogicalRecipeDSetDO : logicalRecipeDSetList) {
                                String machineRecipeObj = cimLogicalRecipeDSetDO.getRecipeObj();
                                sql = "select * from OMRCP_EQP where REFKEY = ?";
                                List<CimMachineRecipeEqpDO> cimMachineRecipeEqpDOList = cimJpaRepository.query(sql, CimMachineRecipeEqpDO.class, machineRecipeObj);
                                if (CimArrayUtils.isNotEmpty(cimMachineRecipeEqpDOList)) {
                                    virtualOperationLot.setNext2EquipmentID(new ObjectIdentifier(cimMachineRecipeEqpDOList.get(0).getEquipmentID(), cimMachineRecipeEqpDOList.get(0).getEquipmentObj()));
                                    virtualOperationLot.setNext2LogicalRecipeID(new ObjectIdentifier(logicalRecipeID, logicalRecipeObj));
                                    virtualOperationLot.setNext2requiredCassetteCategory(cassettCategory);
                                    break;
                                }
                            }
                        }

                    } else {
                        // line: 2966 - 3013
                        for (CimPDEqpDO cimPDEqpDO : pdEqpList){
                            sql = "select OMRCP.* FROM OMLRCP_DFLT,OMRCP,OMRCP_EQP WHERE OMLRCP_DFLT.REFKEY = ?" +
                                    " AND OMLRCP_DFLT.RECIPE_RKEY = OMRCP.RECIPE_RKEY AND OMRCP.ID = OMRCP_EQP.REFKEY AND OMRCP_EQP.EQP_ID = ?";
                            List<CimMachineRecipeDO> query = cimJpaRepository.query(sql, CimMachineRecipeDO.class, logicalRecipeObj, cimPDEqpDO.getEquipmentID());
                            if (CimArrayUtils.isNotEmpty(query)) {
                                virtualOperationLot.setNext2EquipmentID(new ObjectIdentifier(cimPDEqpDO.getEquipmentID(), cimPDEqpDO.getEquipmentObj()));;
                                virtualOperationLot.setNext2LogicalRecipeID(new ObjectIdentifier(logicalRecipeID, logicalRecipeObj));
                                virtualOperationLot.setNext2requiredCassetteCategory(cassettCategory);
                            }
                        }
                    }
                }
            }



            //【stp24】get Q-Time Info For Lot
            // Max Q-Time
            List<Infos.LotQtimeInfo> objLotQtimeGetForRouteDROut = lotMethod.lotQtimeGetForRouteDR(objCommon, virtualOperationLot.getLotID(),
                    cimProcessOperationDO.getMainProcessFlowObj(), cimProcessOperationDO.getModuleProcessFlowObj(), cimProcessOperationDO.getOperationNumber());
            virtualOperationLot.setLotQtimeInfoList(objLotQtimeGetForRouteDROut);

            // Min Q-Time
            List<Infos.LotQtimeInfo> minQTimeInfos = minQTimeMethod.getRestrictInProcessArea(
                    virtualOperationLot.getLotID(), cimProcessOperationDO.getMainProcessFlowObj());
            virtualOperationLot.setMinQTimeInfos(minQTimeInfos);

            //【step25】select QTime restriction for Lot on branch route.
            if (log.isDebugEnabled())
                log.debug("【step25】select QTime restriction for Lot on branch route.");

            sql = "SELECT * FROM OMPRFCX_RTNSEQ WHERE REFKEY = ?";
            List<CimProcessFlowContextReturnDO> cimProcessFlowContextReturnDOList = cimJpaRepository.query(sql, CimProcessFlowContextReturnDO.class, cimLotDO.getProcessFlowContextObj());

            int cimProcessFlowContextReturnDOSize = CimArrayUtils.getSize(cimProcessFlowContextReturnDOList);
            for (int i = 0; i < cimProcessFlowContextReturnDOSize; i++) {
                CimProcessFlowContextReturnDO cimProcessFlowContextReturnDO = cimProcessFlowContextReturnDOList.get(i);
                String tmpMainPFObj = cimProcessFlowContextReturnDO.getMainProcessFlowObj();
                String tmpModulePFObj = cimProcessFlowContextReturnDO.getModuleProcessFlowObj();
                String tmpOperationNumber = cimProcessFlowContextReturnDO.getOperationNumber();
                objLotQtimeGetForRouteDROut = lotMethod.lotQtimeGetForRouteDR(objCommon, virtualOperationLot.getLotID(), tmpMainPFObj, tmpModulePFObj, tmpOperationNumber);

                int tmpSize = CimArrayUtils.getSize(objLotQtimeGetForRouteDROut);
                for (int j = 0; j < tmpSize; j++) {
                    Infos.LotQtimeInfo tmpLotQtimInfo = objLotQtimeGetForRouteDROut.get(j);
                    if (!CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, tmpLotQtimInfo.getQrestrictionTargetTimeStamp())
                            || !CimStringUtils.isEmpty(tmpLotQtimInfo.getQrestrictionTargetTimeStamp())) {
                        virtualOperationLot.getLotQtimeInfoList().add(tmpLotQtimInfo);
                    }
                }

                minQTimeInfos.addAll(minQTimeMethod.getRestrictInProcessArea(
                        virtualOperationLot.getLotID(), tmpMainPFObj));
            }

            virtualOperationLot.setQtimeFlag(CimArrayUtils.isNotEmpty(virtualOperationLot.getLotQtimeInfoList()));
            virtualOperationLot.setMinQTimeFlag(CimArrayUtils.isNotEmpty(minQTimeInfos));

            //【step26】select PD information
            log.debug("【step26】select PD information");
            Boolean logicalRecipeFoundFlag = false;
            ObjectIdentifier logicalRecipeID = null;
            String inspecitionType = null;
            sql = "SELECT OMPRP.PRP_ID, OMPRP_LRPRD.LRCP_ID, OMPRP_LRPRD.LRCP_RKEY FROM OMPRP, OMPRP_LRPRD WHERE " +
                    " OMPRP.ID = ? AND OMPRP_LRPRD.REFKEY = ? AND OMPRP_LRPRD.PROD_ID = ? ";
            Object[] objects = cimJpaRepository.queryOne(sql, cimProcessOperationDO.getProcessDefinitionObj(), cimProcessOperationDO.getProcessDefinitionObj(), cimLotDO.getProductSpecificationID());

            if (null == objects || 0 == objects.length || !CimStringUtils.isEmpty((String) objects[1])) {
                log.debug("OMPRP, OMPRP_LRPRD is NOT_FOUND");
            } else {
                logicalRecipeFoundFlag = true;
                inspecitionType = (String) objects[0]; // PRP_ID 代替
                logicalRecipeID = new ObjectIdentifier((String)objects[1], (String)objects[2]);
            }

            //【step27】select logical recipe by product group
            log.debug("【step27】select logical recipe by product group");
            // search logical recipe by product group
            if (CimBooleanUtils.isFalse(logicalRecipeFoundFlag)) {
                sql = String.format("SELECT PRODFMLY_ID\n" +
                        "                          FROM OMPRODINFO\n" +
                        "                          WHERE PROD_ID = '%s'", cimLotDO.getProductSpecificationID());
                CimProductSpecificationDO cimProductSpecificationDO1 = cimJpaRepository.queryOne(sql, CimProductSpecificationDO.class);
                if (null == cimProductSpecificationDO1) {
                    log.debug("OMPRODINFO is NOT_FOUND");
                } else {
                    sql = "SELECT OMPRP.PRP_ID, OMPRP_LRPRODFMLY.LRCP_ID, OMPRP_LRPRODFMLY.LRCP_RKEY FROM OMPRP, OMPRP_LRPRODFMLY " +
                            "WHERE OMPRP.ID = ? AND OMPRP_LRPRODFMLY.REFKEY = ? AND OMPRP_LRPRODFMLY.PRODFMLY_ID = ?";
                    objects = cimJpaRepository.queryOne(sql, cimProcessOperationDO.getProcessDefinitionObj(),
                            cimProcessOperationDO.getProcessDefinitionObj(),  cimProductSpecificationDO1.getProductGroupID());
                    if (null != objects && 0 != objects.length && !CimStringUtils.isEmpty((String) objects[1])) {
                        logicalRecipeFoundFlag = true;
                        inspecitionType = (String) objects[0];  // PRP_ID 代替
                        logicalRecipeID = new ObjectIdentifier((String)objects[1], (String)objects[2]);
                    }
                }
            }

            //【step28】search logical recipe by technology
            log.debug("search logical recipe by technology");
            if (CimBooleanUtils.isFalse(logicalRecipeFoundFlag)) {
                sql = String.format("SELECT TECH_ID \n" +
                         "           FROM   OMPRODFMLY \n" +
                         "           WHERE  PRODFMLY_ID = '%s'", cimProductSpecificationDO.getProductGroupID());
                CimProductGroupDO cimProductGroupDO1 = cimJpaRepository.queryOne(sql, CimProductGroupDO.class);
                if (null == cimProductGroupDO1) {
                    log.debug("OMPRODFMLY is not found.");
                } else {
                    sql = "SELECT OMPRP.PRP_ID, OMPRP_LRTECH.LRCP_ID, OMPRP_LRTECH.LRCP_RKEY FROM OMPRP, OMPRP_LRTECH " +
                            "WHERE OMPRP.ID = ? AND OMPRP_LRTECH.REFKEY = ? AND OMPRP_LRTECH.TECH_ID = ?";
                    objects = cimJpaRepository.queryOne(sql, cimProcessOperationDO.getProcessDefinitionObj(),
                            cimProcessOperationDO.getProcessDefinitionObj(), cimProductGroupDO1.getTechnologyID());
                    if (null != objects && 0 != objects.length && !CimStringUtils.isEmpty((String) objects[1])) {
                        logicalRecipeFoundFlag = true;
                        inspecitionType = (String) objects[0]; // PRP_ID 代替
                        logicalRecipeID = new ObjectIdentifier((String)objects[1], (String)objects[2]);
                    }
                }
            }

            //【step29】search defalut logical recipe
            log.debug("search defalut logical recipe");
            if (CimBooleanUtils.isFalse(logicalRecipeFoundFlag)) {
                sql = String.format("SELECT OMPRP.PRP_ID,\n" +  // PRP_ID 代替
                        "                                 LRCP_ID,\n" +
                        "                                 LRCP_RKEY\n" +
                        "                          FROM   OMPRP\n" +
                        "                          WHERE  ID = '%s'", cimProcessOperationDO.getProcessDefinitionObj());
                CimProcessDefinitionDO cimProcessDefinitionDO = cimJpaRepository.queryOne(sql, CimProcessDefinitionDO.class);
                if (null != cimProcessDefinitionDO && !CimStringUtils.isEmpty(cimProcessDefinitionDO.getRecipeID())) {
                    logicalRecipeFoundFlag = true;
                    logicalRecipeID = new ObjectIdentifier(cimProcessDefinitionDO.getRecipeID(), cimProcessDefinitionDO.getRecipeObj());
                    inspecitionType = cimProcessDefinitionDO.getInspectionType();
                } else {
                    log.debug("OMPRP is not found.");
                }
            }
            virtualOperationLot.setInspectionType(inspecitionType);
            if (!CimStringUtils.isEmpty(cimProcessOperationDO.getControlJobObj())) {
                virtualOperationLot.setLogicalRecipeID(new ObjectIdentifier(cimProcessOperationDO.getAssignLogicalRecipeID(), cimProcessOperationDO.getAssignLogicalRecipeObj()));
            } else {
                virtualOperationLot.setLogicalRecipeID(logicalRecipeID);
            }

            if (CimBooleanUtils.isFalse(bondingInfoRetrievedFlag)) {
                ObjectIdentifier bondingTargetOpeartionID = null;
                if (CimBooleanUtils.isTrue(bondingEqpFlag)) {
                    bondingTargetOpeartionID = virtualOperationLot.getOperationID();
                } else if (CimBooleanUtils.isTrue(isLotInBondingFlow)) {
                    // get target operation of bonding flow
                    log.debug("get target operation of bonding flow");
                    // lot_bondingOperationInfo_GetDR
                    // Line:3465 - 3481
                    Outputs.ObjLotBondingOperationInfoGetDROut objLotBondingOperationInfoGetDROut = lotMethod.lotBondingOperationInfoGetDR(objCommon, virtualOperationLot.getLotID());
                    bondingTargetOpeartionID = objLotBondingOperationInfoGetDROut.getTargetOperationID();
                }
                if (ObjectIdentifier.isNotEmptyWithValue(bondingTargetOpeartionID)) {
                    // check if the lot is on bonding operaion
                    log.debug("check if the lot is on bonding operaion");
                    // process_BOMPartsInfo_GetDR
                    List<Infos.BOMPartsInfo> bomPartsInfos = null;
                    try {
                        bomPartsInfos = processMethod.processBOMPartsInfoGetDR(objCommon, virtualOperationLot.getProductID(), bondingTargetOpeartionID);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getBomNotDefined(), e.getCode())
                                && !Validations.isEquals(retCodeConfigEx.getPartsNotDefinedForProcess(), e.getCode())){
                            throw e;
                        }
                    }
                    if (CimArrayUtils.isEmpty(bomPartsInfos)){
                        virtualOperationLot.setBondingCateGory(BizConstant.SP_LOT_BONDINGCATEGORY_TOP);
                    } else {
                        virtualOperationLot.setBondingCateGory(BizConstant.SP_LOT_BONDINGCATEGORY_BASE);
                        virtualOperationLot.setTopProductID(bomPartsInfos.get(0).getPartID());
                    }
                }
            }

            // find referable Lot for performance improvement.
            log.debug("find referable Lot for performance improvement.");
            Boolean infoCollectedFlag = false;
            // Eqp doesn't have Chamber or all Chambers' status are not conditional available.
            // We don't have to consider subLotType.
            //  List<Boolean> lotHasAssignedMachineRecipeList = new ArrayList<>();
            //   List<Infos.VirtualOperationLot> virtualOperationLotList = new ArrayList<>();
            for (int k = 0; k < aRowIndex; k++){
                Infos.VirtualOperationLot tmpVirtualOperationLot = virtualOperationLotList.get(k);
                boolean lotHasAssignedMachineRecipe = lotHasAssignedMachineRecipeList.get(k);
                if (!ObjectIdentifier.isEmptyWithValue(tmpVirtualOperationLot.getControlJob()) && lotHasAssignedMachineRecipe){
                    if ((searchCondition == 0
                            && ObjectIdentifier.equalsWithValue(tmpVirtualOperationLot.getLogicalRecipeID(), virtualOperationLot.getLogicalRecipeID())
                            && !fpcAppliedList.get(k) && !fpcAppliedList.get(aRowIndex))
                            || (searchCondition == 1
                            && ObjectIdentifier.equalsWithValue(tmpVirtualOperationLot.getLogicalRecipeID(), virtualOperationLot.getLogicalRecipeID())
                            && !fpcAppliedList.get(k) && !fpcAppliedList.get(aRowIndex) && CimStringUtils.equals(subLotTypeList.get(k), subLotTypeList.get(aRowIndex)))){
                        //------------------------------------------------------------------------------------
                        //  Referable Lot is found.
                        //  Recipe related information should be set from PO.ASSIGNEDxxxx .
                        //  recipeAvailableFlag should always be TRUE.
                        //------------------------------------------------------------------------------------
                        virtualOperationLot.setProcessMonitorProductID(tmpVirtualOperationLot.getProcessMonitorProductID());
                        virtualOperationLot.setTestTypeID(tmpVirtualOperationLot.getTestTypeID());
                        virtualOperationLot.setRecipeAvailableFlag(true);
                        virtualOperationLot.setMachineRecipeID(new ObjectIdentifier(cimProcessOperationDO.getAssignRecipeID(), cimProcessOperationDO.getAssignRecipeObj()));
                        virtualOperationLot.setPhysicalRecipeID(cimProcessOperationDO.getAssignPhysicalRecipeID());
                        infoCollectedFlag = true;
                        break;
                    }
                } else {
                    if ((searchCondition == 0
                            && ObjectIdentifier.equalsWithValue(tmpVirtualOperationLot.getLogicalRecipeID(), virtualOperationLot.getLogicalRecipeID())
                            && ObjectIdentifier.isEmptyWithValue(tmpVirtualOperationLot.getControlJob())
                            && !lotHasAssignedMachineRecipeList.get(k)
                            && !fpcAppliedList.get(k)
                            && !fpcAppliedList.get(aRowIndex))
                            || (searchCondition == 1
                            && ObjectIdentifier.equalsWithValue(tmpVirtualOperationLot.getLogicalRecipeID(), virtualOperationLot.getLogicalRecipeID())
                            && ObjectIdentifier.isEmptyWithValue(tmpVirtualOperationLot.getControlJob())
                            && !lotHasAssignedMachineRecipeList.get(k)
                            && !fpcAppliedList.get(k)
                            && !fpcAppliedList.get(aRowIndex)
                            && ObjectIdentifier.equalsWithValue(tmpVirtualOperationLot.getProductID(), virtualOperationLot.getProductID())
                            && CimStringUtils.equals(subLotTypeList.get(k), subLotTypeList.get(aRowIndex)))){
                        //------------------------------------------------------------------------------------
                        //  Referable Lot is found.
                        //  All information can be set from referable Lot.
                        //------------------------------------------------------------------------------------
                        virtualOperationLot.setProcessMonitorProductID(virtualOperationLotList.get(k).getProcessMonitorProductID());
                        virtualOperationLot.setTestTypeID(virtualOperationLotList.get(k).getTestTypeID());
                        virtualOperationLot.setMachineRecipeID(virtualOperationLotList.get(k).getMachineRecipeID());
                        virtualOperationLot.setPhysicalRecipeID(virtualOperationLotList.get(k).getPhysicalRecipeID());
                        virtualOperationLot.setRecipeAvailableFlag(virtualOperationLotList.get(k).getRecipeAvailableFlag());
                        infoCollectedFlag = true;
                        break;
                    }
                }
            }
            if (!infoCollectedFlag){
                //-----------------------------------
                //  Referable Lot is not found.
                //-----------------------------------
                // if use FPCInfo, get machineRecipe and so on.
                boolean  FPCLogicalRecipeFound = false;
                boolean  FPCMachineRecipeFound = false;
                ObjectIdentifier processMonitorProductID = null;
                ObjectIdentifier testTypeID = null;
                ObjectIdentifier machineRecipeID = null;
                String physicalRecipeID = null;
                if (lotEffectiveFPCInfoGetOut.isMachineRecipeActionRequiredFlag()){
                    // get some items from OMLRCP
                    sql = String.format("SELECT OMLRCP.MON_PROD_ID,\n" +
                            "                             OMLRCP.MON_PROD_RKEY,\n" +
                            "                             OMLRCP.TEST_TYPE_ID,\n" +
                            "                             OMLRCP.TEST_TYPE_RKEY\n" +
                            "                        FROM OMLRCP\n" +
                            "                       WHERE OMLRCP.LRCP_ID  = '%s'\n" +
                            "                         AND OMLRCP.LRCP_RKEY = '%s'",
                            virtualOperationLot.getLogicalRecipeID().getValue(), virtualOperationLot.getLogicalRecipeID().getReferenceKey());
                    CimLogicalRecipeDO cimLogicalRecipeDO = cimJpaRepository.queryOne(sql, CimLogicalRecipeDO.class);
                    processMonitorProductID = new ObjectIdentifier(cimLogicalRecipeDO.getMonitorProductSpecificationID(), cimLogicalRecipeDO.getMonitorProductSpecificationObj());
                    testTypeID = new ObjectIdentifier(cimLogicalRecipeDO.getTestTypeID(), cimLogicalRecipeDO.getTestTypeObj());
                    if (cimLogicalRecipeDO != null){
                        FPCLogicalRecipeFound = true;
                    }
                    // for active version.
                    // The selection of active version isn't approved when FPC information is defined..
                    // But, if it is selected,
                    // we'll improve, so that the machine recipe which the active version is pointing out is taken, for keeping out of transaction failure.
                    machineRecipeID = lotEffectiveFPCInfoGetOut.getFpcInfo().getMachineRecipeID();
                    if (!ObjectIdentifier.isEmptyWithValue(machineRecipeID)){
                        String version = cimFrameWorkGlobals.extractVersionFromID(machineRecipeID.getValue());
                        if (CimStringUtils.equals(version, BizConstant.SP_ACTIVE_VERSION)){
                            sql = String.format("SELECT B.RECIPE_ID, B.ID\n" +
                                    "                                      FROM  OMRCP A, OMRCP B\n" +
                                    "                                      WHERE A.RECIPE_ID = '%s' AND\n" +
                                    "                                            B.RECIPE_ID = A.ACTIVE_ID", machineRecipeID.getValue());
                            CimMachineRecipeDO cimMachineRecipeDO = cimJpaRepository.queryOne(sql, CimMachineRecipeDO.class);
                            if (cimMachineRecipeDO != null){
                                machineRecipeID = new ObjectIdentifier(cimMachineRecipeDO.getRecipeID(), cimMachineRecipeDO.getId());
                            }
                        }
                    }
                    // search physical recipe from machine recipe.
                    if (!ObjectIdentifier.isEmptyWithValue(machineRecipeID)){
                        sql = String.format("SELECT PHY_RECIPE_ID\n" +
                                "                            FROM OMRCP\n" +
                                "                           WHERE RECIPE_ID  = '%s'\n" +
                                "                             AND RECIPE_RKEY = '%s'",
                                machineRecipeID.getValue(), machineRecipeID.getReferenceKey());
                        CimMachineRecipeDO cimMachineRecipeDO = cimJpaRepository.queryOne(sql, CimMachineRecipeDO.class);
                        physicalRecipeID = cimMachineRecipeDO.getPhysicalRecipeID();
                        if (cimMachineRecipeDO != null){
                            FPCMachineRecipeFound = true;
                        }
                    }
                }
                if (!lotEffectiveFPCInfoGetOut.isMachineRecipeActionRequiredFlag()){
                    //-------------------------------------------------------------
                    // select Logical and Machine and Physical Recipe information
                    //-------------------------------------------------------------
                    sql = String.format(" SELECT OMLRCP.MON_PROD_ID,\n" +
                            "                                    OMLRCP.MON_PROD_RKEY,\n" +
                            "                                    OMLRCP.TEST_TYPE_ID,\n" +
                            "                                    OMLRCP.TEST_TYPE_RKEY,\n" +
                            "                                    OMRCP.RECIPE_ID,\n" +
                            "                                    OMRCP.ID,\n" +
                            "                                    OMRCP.PHY_RECIPE_ID,\n" +
                            "                                    OMLRCP_DFLT.IDX_NO\n" +
                            "                             FROM   OMLRCP,\n" +
                            "                                    OMLRCP_DFLT,\n" +
                            "                                    OMRCP\n" +
                            "                             WHERE  OMLRCP.ID = '%s' AND\n" +
                            "                                    OMLRCP_DFLT.REFKEY = '%s' AND\n" +
                            "                                    OMLRCP_DFLT.RECIPE_RKEY = OMRCP.ID",
                            virtualOperationLot.getLogicalRecipeID().getReferenceKey(), virtualOperationLot.getLogicalRecipeID().getReferenceKey());
                    Object[] objects2 = cimJpaRepository.queryOne(sql);
                    processMonitorProductID = new ObjectIdentifier((String) objects2[0], (String) objects2[1]);
                    testTypeID = new ObjectIdentifier((String) objects2[2], (String) objects2[3]);
                    machineRecipeID = new ObjectIdentifier((String) objects2[4], (String) objects2[5]);
                    physicalRecipeID = (String) objects2[6];
                    if (!ObjectIdentifier.isEmptyWithValue(machineRecipeID)){
                        String version = cimFrameWorkGlobals.extractVersionFromID(machineRecipeID.getValue());
                        if (CimStringUtils.equals(version, BizConstant.SP_ACTIVE_VERSION)){
                            sql = String.format("SELECT B.RECIPE_ID, B.ID\n" +
                                    "                                      FROM  OMRCP A, OMRCP B\n" +
                                    "                                      WHERE A.RECIPE_ID = '%s' AND\n" +
                                    "                                            B.RECIPE_ID = A.ACTIVE_ID", machineRecipeID.getValue());
                            CimMachineRecipeDO cimMachineRecipeDO = cimJpaRepository.queryOne(sql, CimMachineRecipeDO.class);
                            if (cimMachineRecipeDO != null){
                                machineRecipeID = new ObjectIdentifier(cimMachineRecipeDO.getRecipeID(), cimMachineRecipeDO.getId());
                            }
                        }
                    }
                    virtualOperationLot.setRecipeAvailableFlag(true);
                    virtualOperationLot.setProcessMonitorProductID(processMonitorProductID);
                    virtualOperationLot.setTestTypeID(testTypeID);
                    //---------------------
                    //Lot Reservation Check
                    //---------------------
                    if (!CimStringUtils.isEmpty(cimLotDO.getControlJobObj())){
                        virtualOperationLot.setMachineRecipeID(new ObjectIdentifier(cimProcessOperationDO.getAssignRecipeID(), cimProcessOperationDO.getAssignRecipeObj()));
                        virtualOperationLot.setPhysicalRecipeID(cimProcessOperationDO.getAssignPhysicalRecipeID());
                    } else {
                        //------------------------------------------------------------------------------------------------------
                        //  Even if the Lot does not have relation with any logical recipe, "Recipe Available Flag" is "Yes".
                        //  There is no problem in Auto-3 mode, but this flag should be "No" in case of other mode.
                        //  Therefore, we changed to turn the flag "No" by condition.
                        //
                        //   1. Logical Recipe(Machine Recipe) is not found.
                        //   2. And WhatNext mode is not "Auto-3".
                        //   3. And the Lot does not have control job.
                        //------------------------------------------------------------------------------------------------------
                        if (ObjectIdentifier.isEmptyWithValue(machineRecipeID)){
                            virtualOperationLot.setRecipeAvailableFlag(false);
                        }
                        virtualOperationLot.setMachineRecipeID(machineRecipeID);
                        virtualOperationLot.setPhysicalRecipeID((String)objects2[6]);
                    }
                } else {
                    if (FPCLogicalRecipeFound && FPCMachineRecipeFound){
                        log.info("Logical/Machine recipe found.");
                        virtualOperationLot.setProcessMonitorProductID(processMonitorProductID);
                        virtualOperationLot.setTestTypeID(testTypeID);
                        if (!CimStringUtils.isEmpty(cimLotDO.getControlJobObj())){
                            virtualOperationLot.setRecipeAvailableFlag(true);
                            virtualOperationLot.setMachineRecipeID(new ObjectIdentifier(cimProcessOperationDO.getAssignRecipeID(), cimProcessOperationDO.getAssignRecipeObj()));
                            virtualOperationLot.setPhysicalRecipeID(cimProcessOperationDO.getAssignPhysicalRecipeID());
                        } else {
                            // recipe availability is treated as same as Dynamic Recipe Change
                            virtualOperationLot.setRecipeAvailableFlag(true);
                            virtualOperationLot.setMachineRecipeID(machineRecipeID);
                            virtualOperationLot.setPhysicalRecipeID(physicalRecipeID);
                        }
                    } else {
                        if (!CimStringUtils.isEmpty(cimLotDO.getControlJobObj())){
                            virtualOperationLot.setRecipeAvailableFlag(true);
                            virtualOperationLot.setMachineRecipeID(new ObjectIdentifier(cimProcessOperationDO.getAssignRecipeID(), cimProcessOperationDO.getAssignRecipeObj()));
                            virtualOperationLot.setPhysicalRecipeID(cimProcessOperationDO.getAssignPhysicalRecipeID());
                        } else {
                            virtualOperationLot.setRecipeAvailableFlag(true);
                            virtualOperationLot.setMachineRecipeID(new ObjectIdentifier("*"));
                        }
                    }
                }
            }
            if (CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)){
                if (!virtualOperationLot.getRecipeAvailableFlag()){
                    continue;
                }
            }
            //【step31】get entity inhibit information for lot
            log.debug("【step31】get entity inhibit information for lot");
            List<Infos.EntityInhibitInfo> entityInhibitInfoList = new ArrayList<>();
            if (CimBooleanUtils.isFalse(entityInhibtInfoCollectedFlag)) {
                log.debug("entity inhibt info is not collected before.");
                List<Constrain.EntityIdentifier> entityIdentifierList = new ArrayList<>();
                if (ObjectIdentifier.isNotEmptyWithValue(virtualOperationLot.getProductID())) {
                    Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_PRODUCT);
                    entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(virtualOperationLot.getProductID()));
                    entityIdentifier.setAttrib("");
                    entityIdentifierList.add(entityIdentifier);
                }

                if (ObjectIdentifier.isNotEmptyWithValue(virtualOperationLot.getRouteID())) {
                    Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                    entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(virtualOperationLot.getRouteID()));
                    entityIdentifier.setAttrib("");
                    entityIdentifierList.add(entityIdentifier);
                }
                if (ObjectIdentifier.isNotEmptyWithValue(virtualOperationLot.getOperationID())){
                    Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                    entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(virtualOperationLot.getOperationID()));
                    entityIdentifier.setAttrib("");
                    entityIdentifierList.add(entityIdentifier);
                }

                if (!CimStringUtils.isEmpty(cimProcessOperationDO.getModuleProcessDefinitionID())) {
                    Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MODULEPD);
                    entityIdentifier.setObjectId(cimProcessOperationDO.getModuleProcessDefinitionID());
                    entityIdentifier.setAttrib("");
                    entityIdentifierList.add(entityIdentifier);
                }

                if (ObjectIdentifier.isNotEmptyWithValue(virtualOperationLot.getMachineRecipeID())
                        && !ObjectIdentifier.equalsWithValue(virtualOperationLot.getMachineRecipeID(), "*")) {
                    Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                    entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(virtualOperationLot.getMachineRecipeID()));
                    entityIdentifier.setAttrib("");
                    entityIdentifierList.add(entityIdentifier);
                }

                if (ObjectIdentifier.isNotEmptyWithValue(virtualOperationLot.getStageID())) {
                    Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_STAGE);
                    entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(virtualOperationLot.getStageID()));
                    entityIdentifier.setAttrib("");
                    entityIdentifierList.add(entityIdentifier);
                }
                List<String> subLotTypes = new ArrayList<>();
                subLotTypes.add(subLotTypeList.get(subLotTypeList.size() - 1));
                List<Constrain.EntityInhibitRecord> inhibitRecordList = entityInhibitManager.allEntityInhibitRecordsForLotEntities(entityIdentifierList, subLotTypes);

                int inhibitRecordListSize = CimArrayUtils.getSize(inhibitRecordList);
                for (int i = 0; i < inhibitRecordListSize; i++) {
                    Constrain.EntityInhibitRecord entityInhibitRecord = inhibitRecordList.get(i);

                    Infos.EntityInhibitInfo entityInhibitInfo = new Infos.EntityInhibitInfo();
                    entityInhibitInfo.setEntityInhibitID(new ObjectIdentifier(entityInhibitRecord.getId(), entityInhibitRecord.getReferenceKey()));
                    Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                    List<Constrain.EntityIdentifier> entits = entityInhibitRecord.getEntities();
                    if(!CimObjectUtils.isEmpty(entits)){
                        List<Infos.EntityIdentifier> ens = new ArrayList<>();
                        for (Constrain.EntityIdentifier entit : entits) {
                            Infos.EntityIdentifier en = new Infos.EntityIdentifier();
                            en.setClassName(entit.getClassName());
                            en.setObjectID(new ObjectIdentifier(entit.getObjectId()));
                            en.setAttribution(entit.getAttrib());
                            ens.add(en);
                        }
                        entityInhibitAttributes.setEntities(ens);
                    }
                    entityInhibitAttributes.setSubLotTypes(entityInhibitRecord.getSubLotTypes());
                    entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(entityInhibitRecord.getStartTimeStamp()));
                    entityInhibitAttributes.setEndTimeStamp(CimDateUtils.getTimestampAsString(entityInhibitRecord.getEndTimeStamp()));
                    entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(entityInhibitRecord.getChangedTimeStamp()));
                    entityInhibitAttributes.setReasonCode(ObjectIdentifier.fetchValue(entityInhibitRecord.getReasonCode()));
                    entityInhibitAttributes.setOwnerID(entityInhibitRecord.getOwner());
                    entityInhibitAttributes.setMemo(entityInhibitRecord.getClaimMemo());
                    sql = String.format("SELECT DESCRIPTION\n" +
                            "                             FROM   OMCODE\n" +
                            "                             WHERE  OMCODE.CODETYPE_ID = '%s' AND\n" +
                            "                                    OMCODE.CODE_ID = '%s'",
                            BizConstant.SP_REASONCAT_ENTITYINHIBIT, entityInhibitAttributes.getReasonCode());
                    CimCodeDO cimCodeDO = cimJpaRepository.queryOne(sql, CimCodeDO.class);
                    if (null != cimCodeDO) {
                        entityInhibitAttributes.setReasonDesc(cimCodeDO.getDescription());
                    }
                    entityInhibitInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                    entityInhibitInfoList.add(entityInhibitInfo);
                }
                Inputs.ObjEntityInhibiteffectiveForLotGetDRIn objEntityInhibiteffectiveForLotGetDRIn = new Inputs.ObjEntityInhibiteffectiveForLotGetDRIn();
                objEntityInhibiteffectiveForLotGetDRIn.setLotID(virtualOperationLot.getLotID());
                objEntityInhibiteffectiveForLotGetDRIn.setStrEntityInhibitInfos(entityInhibitInfoList);
                tmpEntityInhibitEffectiveForLotGetDRList.add(objEntityInhibiteffectiveForLotGetDRIn);
                if (!CimArrayUtils.isEmpty(entityInhibitInfoList)) {
                    objEntityInhibiteffectiveForLotGetDRIn.setLotID(virtualOperationLot.getLotID());
                    objEntityInhibiteffectiveForLotGetDRIn.setStrEntityInhibitInfos(entityInhibitInfoList);
                    entityInhibitInfoList = constraintMethod.constraintEffectiveForLotGetDR(objCommon, objEntityInhibiteffectiveForLotGetDRIn.getStrEntityInhibitInfos(), objEntityInhibiteffectiveForLotGetDRIn.getLotID());
                }

                int entityInhibitInfoListSize = CimArrayUtils.getSize(entityInhibitInfoList);
                List<Infos.EntityInhibitAttributes> entityInhibitAttributesList = new ArrayList<>();
                for (int i = 0; i < entityInhibitInfoListSize; i++) {
                    entityInhibitAttributesList.add(entityInhibitInfoList.get(i).getEntityInhibitAttributes());
                }
                virtualOperationLot.setEntityInhibitAttributesList(entityInhibitAttributesList);
            }
            if (!CimArrayUtils.isEmpty(entityInhibitInfoList)){
                if (CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)){
                    log.info("found entity inhibition, but ignore this lot because of CanBeProcessed mode");
                    continue;
                }
            }
            virtualOperationLotList.add(virtualOperationLot);
            aRowIndex ++;
        }

        return virtualOperationLotList;
    }

    @Override
    public List<Infos.VirtualOperationInprocessingLot> virtualOperationInprocessingLotsGetDR(Infos.ObjCommon objCommon, Inputs.ObjVirtualOperationInprocessingLotsGetDRIn in) {
        List<Infos.VirtualOperationInprocessingLot> virtualOperationInprocessingLotList = new ArrayList<>();
        //---------------------------------------------
        //  Set input parameters into local variable
        //---------------------------------------------
        ObjectIdentifier routeID = in.getRouteID();
        String operationNumber = in.getOperationNumber();
        ObjectIdentifier operationID = in.getOperationID();
        int aRowIndex = 0;
        String sql = null;
        if (!ObjectIdentifier.isEmptyWithValue(routeID) && !CimStringUtils.isEmpty(operationNumber)){
            sql = String.format("SELECT LOT_ID, ID, MAIN_PROCESS_ID, " +
                    "MAIN_PROCESS_RKEY, OPE_NO, CJ_RKEY " +
                    "FROM OMLOT " +
                    "WHERE LOT_PROCESS_STATE = '%s' " +
                    "AND MAIN_PROCESS_ID = '%s' " +
                    "AND OPE_NO = '%s'",
                    BizConstant.SP_LOT_PROCSTATE_PROCESSING, routeID.getValue(), operationNumber);
        } else if (!ObjectIdentifier.isEmptyWithValue(operationID)){
            sql = String.format("SELECT OMLOT.LOT_ID, OMLOT.ID, OMLOT.MAIN_PROCESS_ID," +
                    " OMLOT.MAIN_PROCESS_RKEY, OMLOT.OPE_NO, OMLOT.CJ_RKEY" +
                    " FROM OMLOT, OMPROPE WHERE OMLOT.PROPE_RKEY = OMPROPE.ID " +
                    " AND  OMLOT.LOT_PROCESS_STATE = '%s' " +
                    " AND  OMPROPE.STEP_ID = '%s'", BizConstant.SP_LOT_PROCSTATE_PROCESSING, operationID.getValue());
        }
        //-------------------------------------------
        // Judge and Convert SQL with Escape Sequence
        //-------------------------------------------
        List<CimLotDO> cimLotDOList = cimJpaRepository.query(sql, CimLotDO.class);
        if (!CimArrayUtils.isEmpty(cimLotDOList)){
            for (CimLotDO cimLotDO : cimLotDOList){
                Infos.VirtualOperationInprocessingLot tmpVirtualOperationInprocessingLot = new Infos.VirtualOperationInprocessingLot();
                virtualOperationInprocessingLotList.add(tmpVirtualOperationInprocessingLot);
                tmpVirtualOperationInprocessingLot.setLotID(new ObjectIdentifier(cimLotDO.getLotID(), cimLotDO.getId()));
                tmpVirtualOperationInprocessingLot.setRouteID(new ObjectIdentifier(cimLotDO.getRouteID(), cimLotDO.getRouteObj()));
                tmpVirtualOperationInprocessingLot.setOperationNumber(cimLotDO.getOperationNumber());
                //-----------------------------------------------------
                //  Get OperationID from RouteID and OperaionNumber
                //-----------------------------------------------------
                ObjectIdentifier routeOperationID = routeMethod.routeOperationOperationIDGet(objCommon, tmpVirtualOperationInprocessingLot.getRouteID(), tmpVirtualOperationInprocessingLot.getOperationNumber());
                tmpVirtualOperationInprocessingLot.setOperationID(routeOperationID);
                //---------------------------------------
                // select cassette information
                //---------------------------------------
                sql = String.format("SELECT OMCARRIER.CARRIER_ID,\n" +
                        "                            OMCARRIER.CARRIER_RKEY\n" +
                        "                     FROM   OMCARRIER,\n" +
                        "                            OMCARRIER_LOT\n" +
                        "                     WHERE  OMCARRIER_LOT.LOT_ID = '%s' AND\n" +
                        "                            OMCARRIER.ID = OMCARRIER_LOT.REFKEY", cimLotDO.getLotID());
                CimCassetteDO cimCassetteDO = cimJpaRepository.queryOne(sql, CimCassetteDO.class);
                if (cimCassetteDO != null){
                    tmpVirtualOperationInprocessingLot.setCassetteID(new ObjectIdentifier(cimCassetteDO.getCassetteID(), cimCassetteDO.getId()));
                }
                //-------------------------------------
                // select CTRLJOB information
                //-------------------------------------
                CimControlJobDO controlJobExample = new CimControlJobDO();
                controlJobExample.setId(cimLotDO.getControlJobObj());
                CimControlJobDO cimControlJobDO = cimJpaRepository.findOne(Example.of(controlJobExample)).orElse(null);
                if (cimControlJobDO != null){
                    tmpVirtualOperationInprocessingLot.setControlJobID(new ObjectIdentifier(cimControlJobDO.getControlJobID(), cimControlJobDO.getId()));
                    tmpVirtualOperationInprocessingLot.setEquipmentID(new ObjectIdentifier(cimControlJobDO.getEquipmentID(), cimControlJobDO.getEquipmentObject()));
                }
            }
        }

        return virtualOperationInprocessingLotList;
    }

}
