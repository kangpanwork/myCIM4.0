package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ErrorCode;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.entity.nonruntime.arsh.CimRALDO;
import com.fa.cim.entity.nonruntime.arsh.CimRELDO;
import com.fa.cim.entity.nonruntime.arsh.CimRTCLEventDO;
import com.fa.cim.entity.runtime.area.CimAreaDO;
import com.fa.cim.entity.runtime.code.CimCodeDO;
import com.fa.cim.entity.runtime.durable.*;
import com.fa.cim.entity.runtime.durablegroup.CimDurableGroupDO;
import com.fa.cim.entity.runtime.durablepfx.CimDurablePFXDurablePOListDO;
import com.fa.cim.entity.runtime.durablepo.CimDurableProcessOperationDO;
import com.fa.cim.entity.runtime.durablesubstate.CimDurableSubStateDO;
import com.fa.cim.entity.runtime.eqp.CimEquipmentDO;
import com.fa.cim.entity.runtime.person.CimPersonDO;
import com.fa.cim.entity.runtime.pos.CimProcessOperationSpecificationDO;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.entity.runtime.processflow.CimPFPosListDO;
import com.fa.cim.entity.runtime.processflow.CimProcessFlowDO;
import com.fa.cim.entity.runtime.reticlepod.CimReticlePodDO;
import com.fa.cim.entity.runtime.reticlepod.CimReticlePodToReticleDO;
import com.fa.cim.entity.runtime.wafer.CimWaferDO;
import com.fa.cim.entitysuper.NonRuntimeEntity;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.code.CimCategory;
import com.fa.cim.newcore.bo.code.CimCode;
import com.fa.cim.newcore.bo.code.CodeManager;
import com.fa.cim.newcore.bo.dispatch.DispatchingManager;
import com.fa.cim.newcore.bo.durable.*;
import com.fa.cim.newcore.bo.factory.CimArea;
import com.fa.cim.newcore.bo.factory.CimBank;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimReticlePodPortResource;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.machine.MachineManager;
import com.fa.cim.newcore.bo.pd.CimDurableProcessOperation;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlow;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.person.PersonManager;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.durable.DurableDTO;
import com.fa.cim.newcore.exceptions.IllegalParameterException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.drblmngm.ProcessDurable;
import com.fa.cim.newcore.standard.fctrycmp.Area;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.newcore.standard.mtrlmngm.Material;
import com.fa.cim.remote.IRTMSRemoteManager;
import com.fa.cim.rtms.ReticleRTMSMoveInAndReserveReqParams;
import com.fa.cim.rtms.ReticleRTMSMoveOutAndCancelReqParams;
import com.fa.cim.rtms.ReticleUpdateParamsInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.fa.cim.constant.HistoryInfoConstant.ReticleConstant.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p><br/></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2018/10/17 10:43:06
 */
@Slf4j
@OmMethod
public class ReticleMethod implements IReticleMethod {

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IStockerMethod stockerMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private DurableManager durableManager;

    @Autowired
    private PersonManager personManager;

    @Autowired
    private MachineManager machineManager;

    @Autowired
    private IAreaMethod areaMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IConstraintMethod constraintMethod;

    @Autowired
    @Qualifier("DispatchingManagerCore")
    private DispatchingManager dispatchingManager;

    @Autowired
    @Qualifier("CodeManagerCore")
    private CodeManager codeManager;

    @Autowired
    private ICodeMethod codeMethod;

    @Autowired
    private IRTMSRemoteManager irtmsRemoteManager;

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/25       ****                ZQI               delete the Drblsubst entity.add the DurableSubState entity.
     *
     * @param objCommon
     * @param reticleListInqParams
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.ReticleListInqResult>
     * @author Ho
     * @date 2018/10/17 10:54:39
     */
    @Override
    public Results.ReticleListInqResult reticleListGetDR(Infos.ObjCommon objCommon, Params.ReticleListInqParams reticleListInqParams) {
        Results.ReticleListInqResult strReticleListInqResult = new Results.ReticleListInqResult();
        int searchCondition = 0;
        String searchCondition_var = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if (!CimStringUtils.isEmpty(searchCondition_var)) {
            searchCondition = Integer.parseInt(searchCondition_var);
        }
        long fetchLimitCount = 0;
        String reticlePartNumber, reticleStatus, FPCCategory, whiteDefSearchCriteria, flowStatus, durableFlowState, durableState;
        ObjectIdentifier equipmentID, lotID, reticleID, reticleGroupID, bankID, durableSubStatus;
        long maxRetrieveCount = 0;
        Boolean availableForDurableFlag = false;
        List<Object[]> availableForDurableFlagList = new ArrayList<>();
        equipmentID = reticleListInqParams.getEquipmentID();
        lotID = reticleListInqParams.getLotID();
        reticleID = reticleListInqParams.getReticleID();
        reticlePartNumber = reticleListInqParams.getReticlePartNumber();
        reticleGroupID = reticleListInqParams.getReticleGroupID();
        reticleStatus = reticleListInqParams.getReticleStatus();
        maxRetrieveCount = reticleListInqParams.getMaxRetrieveCount();
        FPCCategory = reticleListInqParams.getFPCCategory();
        whiteDefSearchCriteria = reticleListInqParams.getWhiteDefSearchCriteria();
        bankID = reticleListInqParams.getBankID();
        durableSubStatus = reticleListInqParams.getDurableSubStatus();
        flowStatus = reticleListInqParams.getFlowStatus();
        if (maxRetrieveCount <= 0 ||
                maxRetrieveCount > StandardProperties.OM_RETICLE_LIST_MAX_LEN_FOR_RETICLE_LIST_INQ.getIntValue()) {
            fetchLimitCount = StandardProperties.OM_RETICLE_LIST_MAX_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
        } else {
            fetchLimitCount = maxRetrieveCount;
        }
        Boolean hFRDRBLWHITE_FLAG = false;
        String hFRDRBLFPC_CATEGORY = "";
        Boolean bWhiteFlag = false, bSearchAllFlag = false;
        if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_WHITE)) {
            bWhiteFlag = true;
        } else if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_NONWHITE)) {

        } else if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_ALL)) {
            bSearchAllFlag = true;
        } else {
            Validations.check(true, retCodeConfig.getInvalidParameter());
        }
        if (ObjectIdentifier.isEmptyWithValue(equipmentID) && (ObjectIdentifier.isNotEmptyWithValue(lotID))) {
            com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
            com.fa.cim.newcore.bo.pd.CimProcessOperation aProcessOperation = aLot.getProcessOperation();
            Validations.check(aProcessOperation == null, retCodeConfig.getNoNeedReticle());
            CimProductSpecification aProductSpecification = aLot.getProductSpecification();
            com.fa.cim.newcore.bo.recipe.CimLogicalRecipe aLogicalRecipe = aProcessOperation.findLogicalRecipeFor(aProductSpecification);
            Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicRecipe());
            List<CimProcessDurableCapability> aProcessDurableCapabilities = aLot.getReticleGroupsFor("*");
            int i, nReticlePDCs = aProcessDurableCapabilities.size();
            Validations.check(nReticlePDCs == 0, retCodeConfig.getNoNeedReticle());
            Infos.FoundReticle[] strFoundReticle;
            int j, l, count = 0;
            int t_count = StandardProperties.OM_RETICLE_LIST_INITIAL_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
            strFoundReticle = new Infos.FoundReticle[t_count];
            for (i = 0; i < nReticlePDCs; i++) {
                CimProcessDurableCapability aProcessDurableCapability = aProcessDurableCapabilities.get(i);
                List<CimProcessDurable> strProcessDurables = aProcessDurableCapability.allAssignedProcessDurables();
                ObjectIdentifier aReticleGroupID = new ObjectIdentifier();
                aReticleGroupID.setValue(aProcessDurableCapability.getIdentifier());
                aReticleGroupID.setReferenceKey(aProcessDurableCapability.getPrimaryKey());
                int PDLen = strProcessDurables.size();
                if ((count + PDLen) >= t_count) {
                    int tmp_len = StandardProperties.OM_RETICLE_LIST_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
                    if ((count + PDLen) > (t_count + tmp_len)) {
                        int tmp_len2 = (count + PDLen - t_count);
                        t_count = t_count + ((tmp_len2 + tmp_len - 1) / tmp_len) * tmp_len;
                    } else {
                        t_count = t_count + tmp_len;
                    }
                    strFoundReticle = Arrays.copyOf(strFoundReticle, t_count);
                }
                for (j = 0; j < PDLen; j++) {
                    CimProcessDurable strProcessDurable = strProcessDurables.get(j);
                    String aReticleIdentifier = strProcessDurable.getIdentifier();
                    for (l = 0; l < count; l++) {
                        if (CimStringUtils.equals(aReticleIdentifier, strFoundReticle[l].getReticleID().getValue())) {
                            break;
                        }
                    }
                    if (l < count) {
                        continue;
                    }
                    boolean whiteDefFlag = strProcessDurable.isWhiteFlagOn();
                    if (!bSearchAllFlag && bWhiteFlag) {
                        if (!whiteDefFlag) {
                            continue;
                        }
                    } else if (!bSearchAllFlag && !bWhiteFlag) {
                        if (whiteDefFlag) {
                            continue;
                        }
                    }
                    String fpcCategory = strProcessDurable.getFPCCategory();
                    if (!CimStringUtils.equals(fpcCategory, FPCCategory) &&
                            !CimStringUtils.isEmpty(fpcCategory)) {
                        continue;
                    }
                    strFoundReticle[count] = new Infos.FoundReticle();
                    strFoundReticle[count].setWhiteDefFlag(whiteDefFlag);
                    strFoundReticle[count].setFpcCategory(fpcCategory);
                    ObjectIdentifier _reticleID = new ObjectIdentifier();
                    _reticleID.setValue(aReticleIdentifier);
                    strFoundReticle[count].setReticleID(_reticleID);
                    _reticleID.setReferenceKey(strProcessDurable.getPrimaryKey());
                    strFoundReticle[count].setDescription(strProcessDurable.getProcessDurableDescription());
                    strFoundReticle[count].setReticleGroupID(aReticleGroupID);
                    aReticleGroupID.setReferenceKey(strProcessDurable.getPrimaryKey());
                    strFoundReticle[count].setReticleGroupSequenceNumber(BizConstant.SP_RETICLE_DEFAULTGROUPSEQUENCENUMBER);
                    strFoundReticle[count].setReticlePartNumber(strProcessDurable.getPartNumber());
                    strFoundReticle[count].setReticleSerialNumber(strProcessDurable.getSerialNumber());
                    Infos.ReticleStatusInfo reticleStatusInfo = new Infos.ReticleStatusInfo();
                    reticleStatusInfo.setReticleStatus(strProcessDurable.getDurableState());
                    strFoundReticle[count].setReticleStatusInfo(reticleStatusInfo);
                    reticleStatusInfo.setTransferStatus(strProcessDurable.getTransportState());
                    Machine aMachine = strProcessDurable.currentAssignedMachine();
                    if (CimStringUtils.equals(reticleStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                            || CimStringUtils.equals(reticleStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
                        ObjectIdentifier _equipmentID = new ObjectIdentifier();
                        reticleStatusInfo.setEquipmentID(_equipmentID);
                        if (aMachine != null) {
                            _equipmentID.setValue(aMachine.getIdentifier());
                            _equipmentID.setReferenceKey(aMachine.getPrimaryKey());
                        }
                    } else {
                        ObjectIdentifier _stockerID = new ObjectIdentifier();
                        reticleStatusInfo.setStockerID(_stockerID);
                        if (aMachine != null) {
                            _stockerID.setValue(aMachine.getIdentifier());
                            _stockerID.setReferenceKey(aMachine.getPrimaryKey());
                        }
                    }
                    reticleStatusInfo.setLastClaimedTimeStamp(strProcessDurable.getLastClaimedTimeStamp());
                    reticleStatusInfo.setLastClaimedPerson(new ObjectIdentifier(strProcessDurable.getLastClaimedPersonID()));
                    MaterialContainer aMtrlCntnr = strProcessDurable.getMaterialContainer();
                    CimReticlePod aReticlePod = (CimReticlePod) aMtrlCntnr;
                    ObjectIdentifier _reticlePodID = new ObjectIdentifier();
                    reticleStatusInfo.setReticlePodID(_reticlePodID);
                    if (aReticlePod != null) {
                        _reticlePodID.setValue(aReticlePod.getIdentifier());
                        _reticlePodID.setReferenceKey(aReticlePod.getPrimaryKey());
                    }
                    CimDurableControlJob aDurableCJ = strProcessDurable.getDurableControlJob();
                    ObjectIdentifier _aDurableCJ = new ObjectIdentifier();
                    if (aDurableCJ != null) {
                        _aDurableCJ.setValue(aDurableCJ.getIdentifier());
                        _aDurableCJ.setReferenceKey(aDurableCJ.getPrimaryKey());
                    }
                    reticleStatusInfo.setDurableControlJobID(_aDurableCJ);
                    CimDurableProcessFlowContext aDurablePFX = strProcessDurable.getDurableProcessFlowContext();
                    if (aDurablePFX != null) {
                        reticleStatusInfo.setDurableSTBFlag(true);
                    } else {
                        reticleStatusInfo.setDurableSTBFlag(false);
                    }
                    availableForDurableFlag = false;
                    ObjectIdentifier _aDurableSubState = new ObjectIdentifier();
                    CimDurableSubState aDurableSubState = strProcessDurable.getDurableSubState();
                    if (aDurableSubState != null) {
                        reticleStatusInfo.setDurableSubStatus(_aDurableSubState);
                        _aDurableSubState.setValue(aDurableSubState.getIdentifier());
                        _aDurableSubState.setReferenceKey(aDurableSubState.getPrimaryKey());
                        availableForDurableFlag = aDurableSubState.isDurableProcessAvailable();
                    }
                    reticleStatusInfo.setAvailableForDurableFlag(availableForDurableFlag);
                    String reservePerson = strProcessDurable.getReservePersonID();
                    Infos.HashedInfo[] strDurableStatusList = new Infos.HashedInfo[7];
                    strDurableStatusList[0] = new Infos.HashedInfo();
                    strDurableStatusList[0].setHashKey("durable Flow State");
                    strDurableStatusList[0].setHashData("");// TODO: (*strProcessDurables)[j]->getDurableFlowState();
                    strDurableStatusList[1] = new Infos.HashedInfo();
                    strDurableStatusList[1].setHashKey("durable State");
                    strDurableStatusList[1].setHashData("");//TODO:hashData = (*strProcessDurables)[j]->getDurableOnRouteState();
                    strDurableStatusList[2] = new Infos.HashedInfo();
                    strDurableStatusList[2].setHashKey("durable Production State");
                    strDurableStatusList[2].setHashData(strProcessDurable.getDurableProductionState());
                    strDurableStatusList[3] = new Infos.HashedInfo();
                    strDurableStatusList[3].setHashKey("durable Hold State");
                    strDurableStatusList[3].setHashData(strProcessDurable.getDurableHoldState());
                    strDurableStatusList[4] = new Infos.HashedInfo();
                    strDurableStatusList[4].setHashKey("durable Finished State");
                    strDurableStatusList[4].setHashData(strProcessDurable.getDurableFinishedState());
                    strDurableStatusList[5] = new Infos.HashedInfo();
                    strDurableStatusList[5].setHashKey("durable Process State");
                    strDurableStatusList[5].setHashData(strProcessDurable.getDurableProcessState());
                    strDurableStatusList[6] = new Infos.HashedInfo();
                    strDurableStatusList[6].setHashKey("durable Inventory State");
                    strDurableStatusList[6].setHashData(strProcessDurable.getDurableInventoryState());
                    reticleStatusInfo.setStrDurableStatusList(Arrays.asList(strDurableStatusList));
                    reticleStatusInfo.setInPostProcessFlagOfReticle(strProcessDurable.isPostProcessFlagOn());
                    ObjectIdentifier _aBank = new ObjectIdentifier();
                    CimBank aBank = strProcessDurable.getBank();
                    strFoundReticle[count].setBankID(_aBank);
                    if (aBank != null) {
                        _aBank.setValue(aBank.getIdentifier());
                        _aBank.setReferenceKey(aBank.getPrimaryKey());
                    }
                    strFoundReticle[count].setDueTimeStamp(null);
                    ObjectIdentifier _aMainPD = new ObjectIdentifier();
                    com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainPD = strProcessDurable.getMainProcessDefinition();
                    strFoundReticle[count].setRouteID(_aMainPD);
                    if (aMainPD != null) {
                        _aMainPD.setValue(aMainPD.getIdentifier());
                        _aMainPD.setReferenceKey(aMainPD.getPrimaryKey());
                    }
                    strFoundReticle[count].setBankInRequiredFlag(strProcessDurable.isBankInRequired());
                    List<DurableDTO.PosDurableHoldRecord> strHoldRecords = strProcessDurable.allHoldRecords();
                    int holdLen = CimArrayUtils.getSize(strHoldRecords);
                    if (holdLen == 1) {
                        ObjectIdentifier _holdReasonCodeID = strHoldRecords.get(0).getReasonCode();
                        strFoundReticle[count].setHoldReasonCodeID(_holdReasonCodeID);
                    } else if (holdLen > 1) {
                        String holdReasonCode = strHoldRecords.get(0).getReasonCode().getValue() + BizConstant.SP_DEFAULT_CHAR;
                        ObjectIdentifier _holdReasonCodeID = new ObjectIdentifier();
                        _holdReasonCodeID.setValue(holdReasonCode);
                        _holdReasonCodeID.setReferenceKey("");
                        strFoundReticle[count].setHoldReasonCodeID(_holdReasonCodeID);
                    }
                    count++;
                }
            }
            strFoundReticle = Arrays.copyOf(strFoundReticle, count);
            Validations.check(strFoundReticle.length <= 0, new OmCode(retCodeConfig.getNotFoundReticle(), "*****"));
            strReticleListInqResult.setStrFoundReticle(Arrays.asList(strFoundReticle));
            return strReticleListInqResult;
        } else if (ObjectIdentifier.isNotEmptyWithValue(equipmentID) && ObjectIdentifier.isNotEmptyWithValue(lotID)) {
            com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
            CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
            Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
            com.fa.cim.newcore.bo.pd.CimProcessOperation aProcessOperation = aLot.getProcessOperation();
            Validations.check(aProcessOperation == null, retCodeConfig.getNoNeedReticle());
            CimProductSpecification aProductSpecification = aLot.getProductSpecification();
            com.fa.cim.newcore.bo.recipe.CimLogicalRecipe aLogicalRecipe = aProcessOperation.findLogicalRecipeFor(aProductSpecification);
            Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicRecipe());
            String subLotType = aLot.getSubLotType();
            CimMachineRecipe aMachineRecipe = null;
            if (searchCondition == 1) {
                aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLot, aMachine);
            } else {
                aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, subLotType);
            }
            /*Validations.check(aMachineRecipe == null, retCodeConfig.getNoNeedReticle());*/
            String anEqpID = aMachine.getIdentifier();
            List<CimProcessDurableCapability> aProcessDurableCapabilitySeq = aLot.getReticleGroupsFor(anEqpID);
            int j, k, l, PDCLen, PDLen, count = 0;
            Infos.FoundReticle[] strFoundReticle = null;
            PDCLen = aProcessDurableCapabilitySeq.size();
            Validations.check(PDCLen == 0, retCodeConfig.getNoNeedReticle());
            int t_count = StandardProperties.OM_RETICLE_LIST_INITIAL_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
            Machine aMachineInner = null;
            strFoundReticle = new Infos.FoundReticle[t_count];
            for (j = 0; j < PDCLen; j++) {
                CimProcessDurableCapability aProcessDurableCapability = aProcessDurableCapabilitySeq.get(j);
                List<CimProcessDurable> tempAssignedProcessDurablesSeq = aProcessDurableCapability.allAssignedProcessDurables();
                List<CimProcessDurable> allAssignedProcessDurablesSeq = new ArrayList<>();
                for (CimProcessDurable cimProcessDurable : tempAssignedProcessDurablesSeq) {
                    Machine machine = cimProcessDurable.currentAssignedMachine();
                    if (!CimObjectUtils.isEmpty(machine)) {
                        String temEqpID = machine.getIdentifier();
                        if (CimStringUtils.equals(temEqpID, equipmentID.getValue())) {
                            allAssignedProcessDurablesSeq.add(cimProcessDurable);
                        }
                    }
                }
                PDLen = allAssignedProcessDurablesSeq.size();
                if ((count + PDLen) >= t_count) {
                    int tmp_len = StandardProperties.OM_RETICLE_LIST_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
                    if ((count + PDLen) > (t_count + tmp_len)) {
                        int tmp_len2 = (count + PDLen - t_count);
                        t_count = t_count + ((tmp_len2 + tmp_len - 1) / tmp_len) * tmp_len;
                    } else {
                        t_count = t_count + tmp_len;
                    }
                    strFoundReticle = Arrays.copyOf(strFoundReticle, t_count);
                }
                ObjectIdentifier aReticleGroupID = new ObjectIdentifier();
                aReticleGroupID.setValue(aProcessDurableCapability.getIdentifier());
                aReticleGroupID.setReferenceKey(aProcessDurableCapability.getPrimaryKey());
                String aReticleIdentifier = null;
                for (k = 0; k < PDLen; k++) {
                    aReticleIdentifier = allAssignedProcessDurablesSeq.get(k).getIdentifier();
                    for (l = 0; l < count; l++) {
                        if (CimStringUtils.equals(aReticleIdentifier, strFoundReticle[l].getReticleID().getValue())) {
                            break;
                        }
                    }
                    if (l < count) {
                        continue;
                    }
                    boolean whiteDefFlag = allAssignedProcessDurablesSeq.get(k).isWhiteFlagOn();
                    if (!bSearchAllFlag && bWhiteFlag) {
                        if (!whiteDefFlag) {
                            continue;
                        }
                    } else if (!bSearchAllFlag && !bWhiteFlag) {
                        if (whiteDefFlag) {
                            continue;
                        }
                    }
                    String fpcCategory = allAssignedProcessDurablesSeq.get(k).getFPCCategory();
                    if (CimStringUtils.equals(fpcCategory, FPCCategory) &&
                            !CimStringUtils.isEmpty(fpcCategory)) {
                        continue;
                    }
                    strFoundReticle[count] = new Infos.FoundReticle();
                    strFoundReticle[count].setWhiteDefFlag(whiteDefFlag);
                    strFoundReticle[count].setFpcCategory(fpcCategory);
                    ObjectIdentifier _reticleID = new ObjectIdentifier();
                    _reticleID.setValue(aReticleIdentifier);
                    _reticleID.setReferenceKey(allAssignedProcessDurablesSeq.get(k).getPrimaryKey());
                    strFoundReticle[count].setReticleID(_reticleID);
                    strFoundReticle[count].setDescription(allAssignedProcessDurablesSeq.get(k).getProcessDurableDescription());
                    strFoundReticle[count].setReticleGroupID(aReticleGroupID);
                    strFoundReticle[count].setReticleGroupSequenceNumber(j);
                    strFoundReticle[count].setReticleLocation(allAssignedProcessDurablesSeq.get(k).getReticleLocation());
                    strFoundReticle[count].setReticlePartNumber(allAssignedProcessDurablesSeq.get(k).getPartNumber());
                    strFoundReticle[count].setReticleSerialNumber(allAssignedProcessDurablesSeq.get(k).getSerialNumber());
                    Infos.ReticleStatusInfo reticleStatusInfo = new Infos.ReticleStatusInfo();
                    strFoundReticle[count].setReticleStatusInfo(reticleStatusInfo);
                    reticleStatusInfo.setReticleStatus(allAssignedProcessDurablesSeq.get(k).getDurableState());
                    reticleStatusInfo.setTransferStatus(allAssignedProcessDurablesSeq.get(k).getTransportState());
                    aMachineInner = allAssignedProcessDurablesSeq.get(k).currentAssignedMachine();
                    ObjectIdentifier machineIDToFill = new ObjectIdentifier();
                    if (aMachineInner != null) {
                        machineIDToFill.setValue(aMachineInner.getIdentifier());
                        machineIDToFill.setReferenceKey(aMachineInner.getPrimaryKey());
                    }
                    if (CimStringUtils.equals(reticleStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                            || CimStringUtils.equals(reticleStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
                        reticleStatusInfo.setEquipmentID(machineIDToFill);
                    } else {
                        reticleStatusInfo.setStockerID(machineIDToFill);
                    }
                    reticleStatusInfo.setLastClaimedTimeStamp(allAssignedProcessDurablesSeq.get(k).getLastClaimedTimeStamp());
                    reticleStatusInfo.setLastClaimedPerson(new ObjectIdentifier(allAssignedProcessDurablesSeq.get(k).getLastClaimedPersonID()));
                    ObjectIdentifier _materialContainer = new ObjectIdentifier();
                    MaterialContainer aMtrlCntnr = allAssignedProcessDurablesSeq.get(k).getMaterialContainer();
                    CimReticlePod aReticlePod = (CimReticlePod) aMtrlCntnr;
                    reticleStatusInfo.setReticlePodID(_materialContainer);
                    if (aReticlePod != null) {
                        _materialContainer.setValue(aReticlePod.getIdentifier());
                        _materialContainer.setReferenceKey(aReticlePod.getPrimaryKey());
                    }
                    ObjectIdentifier _aDurableCJ = new ObjectIdentifier();
                    CimDurableControlJob aDurableCJ = allAssignedProcessDurablesSeq.get(k).getDurableControlJob();
                    reticleStatusInfo.setDurableControlJobID(_aDurableCJ);
                    if (aDurableCJ != null) {
                        _aDurableCJ = new ObjectIdentifier(aDurableCJ.getIdentifier(), aDurableCJ.getPrimaryKey());
                    }
                    CimDurableProcessFlowContext aDurablePFX = allAssignedProcessDurablesSeq.get(k).getDurableProcessFlowContext();
                    if (aDurablePFX != null) {
                        reticleStatusInfo.setDurableSTBFlag(true);
                    } else {
                        reticleStatusInfo.setDurableSTBFlag(false);
                    }
                    availableForDurableFlag = false;
                    ObjectIdentifier _aDurableSubState = new ObjectIdentifier();
                    CimDurableSubState aDurableSubState = allAssignedProcessDurablesSeq.get(k).getDurableSubState();
                    if (aDurableSubState != null) {
                        reticleStatusInfo.setDurableSubStatus(_aDurableSubState);
                        _aDurableSubState.setValue(aDurableSubState.getIdentifier());
                        _aDurableSubState.setReferenceKey(aDurableSubState.getPrimaryKey());
                        availableForDurableFlag = aDurableSubState.isDurableProcessAvailable();
                    }
                    reticleStatusInfo.setAvailableForDurableFlag(availableForDurableFlag);
                    /*--------------------------------*/
                    /*  Get Reticle reserve person    */
                    /*--------------------------------*/
                    String reservePerson = allAssignedProcessDurablesSeq.get(k).getReservePersonID();
                    Infos.HashedInfo[] strDurableStatusList = new Infos.HashedInfo[7];
                    strDurableStatusList[0] = new Infos.HashedInfo();
                    strDurableStatusList[0].setHashKey("durable Flow State");
                    strDurableStatusList[0].setHashData("");// TODO: (*strProcessDurables)[j]->getDurableFlowState();
                    strDurableStatusList[1] = new Infos.HashedInfo();
                    strDurableStatusList[1].setHashKey("durable State");
                    strDurableStatusList[1].setHashData("");//TODO:hashData = (*strProcessDurables)[j]->getDurableOnRouteState();
                    strDurableStatusList[2] = new Infos.HashedInfo();
                    strDurableStatusList[2].setHashKey("durable Production State");
                    strDurableStatusList[2].setHashData(allAssignedProcessDurablesSeq.get(k).getDurableProductionState());
                    strDurableStatusList[3] = new Infos.HashedInfo();
                    strDurableStatusList[3].setHashKey("durable Hold State");
                    strDurableStatusList[3].setHashData(allAssignedProcessDurablesSeq.get(k).getDurableHoldState());
                    strDurableStatusList[4] = new Infos.HashedInfo();
                    strDurableStatusList[4].setHashKey("durable Finished State");
                    strDurableStatusList[4].setHashData(allAssignedProcessDurablesSeq.get(k).getDurableFinishedState());
                    strDurableStatusList[5] = new Infos.HashedInfo();
                    strDurableStatusList[5].setHashKey("durable Process State");
                    strDurableStatusList[5].setHashData(allAssignedProcessDurablesSeq.get(k).getDurableProcessState());
                    strDurableStatusList[6] = new Infos.HashedInfo();
                    strDurableStatusList[6].setHashKey("durable Inventory State");
                    strDurableStatusList[6].setHashData(allAssignedProcessDurablesSeq.get(k).getDurableInventoryState());
                    reticleStatusInfo.setStrDurableStatusList(Arrays.asList(strDurableStatusList));
                    reticleStatusInfo.setInPostProcessFlagOfReticle(allAssignedProcessDurablesSeq.get(k).isPostProcessFlagOn());
                    //--- Set objectIdentifier bankID; ------------------------------------//
                    ObjectIdentifier _aBank = new ObjectIdentifier();
                    CimBank aBank = allAssignedProcessDurablesSeq.get(k).getBank();
                    strFoundReticle[count].setBankID(_aBank);
                    if (aBank != null) {
                        _aBank.setValue(aBank.getIdentifier());
                        _aBank.setReferenceKey(aBank.getPrimaryKey());
                    }
                    strFoundReticle[count].setDueTimeStamp(null);
                    ObjectIdentifier _aMainPD = new ObjectIdentifier();
                    com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainPD = allAssignedProcessDurablesSeq.get(k).getMainProcessDefinition();
                    strFoundReticle[count].setRouteID(_aMainPD);
                    if (aMainPD != null) {
                        _aMainPD.setValue(aMainPD.getIdentifier());
                        _aMainPD.setReferenceKey(aMainPD.getPrimaryKey());
                    }
                    strFoundReticle[count].setBankInRequiredFlag(allAssignedProcessDurablesSeq.get(k).isBankInRequired());
                    List<DurableDTO.PosDurableHoldRecord> strHoldRecords = allAssignedProcessDurablesSeq.get(k).allHoldRecords();
                    int holdLen = strHoldRecords.size();
                    if (holdLen == 1) {
                        strFoundReticle[count].setHoldReasonCodeID(strHoldRecords.get(0).getReasonCode());
                    } else if (holdLen > 1) {
                        String holdReasonCode = strHoldRecords.get(0).getReasonCode().getValue() + BizConstant.SP_DEFAULT_CHAR;
                        ObjectIdentifier _holdReasonCodeID = new ObjectIdentifier();
                        _holdReasonCodeID.setValue(holdReasonCode);
                        strFoundReticle[count].setHoldReasonCodeID(_holdReasonCodeID);
                    }
                    count++;
                }
            }
            strFoundReticle = Arrays.copyOf(strFoundReticle, count);
            Validations.check(strFoundReticle.length <= 0, new OmCode(retCodeConfig.getNotFoundReticle(), "*****"));
            strReticleListInqResult.setStrFoundReticle(Arrays.asList(strFoundReticle));
            return strReticleListInqResult;
        }
        String HV_BUFFER = "SELECT\n" +
                "	OMPDRBL.PDRBL_ID,\n" +
                "	OMPDRBL.ID,\n" +
                "	OMPDRBL.DESCRIPTION,\n" +
                "	OMPDRBL.DRBL_PART_NO,\n" +
                "	OMPDRBL.DRBL_SERIAL_NO,\n" +
                "	OMPDRBL.PDRBL_STATE,\n" +
                "	OMPDRBL.XFER_STATE,\n" +
                "	OMPDRBL.EQP_ID,\n" +
                "	OMPDRBL.EQP_RKEY,\n" +
                "	OMPDRBL.LAST_TRX_TIME,\n" +
                "	OMPDRBL.LAST_TRX_USER_ID,\n" +
                "	OMPDRBL.LAST_TRX_USER_RKEY,\n" +
                "	OMPDRBL.MTRL_CONT_ID,\n" +
                "	OMPDRBL.MTRL_CONT_RKEY,\n" +
                "	OMPDRBL.TEMP_MODE_FLAG,\n" +
                "	OMPDRBL.DOC_CATEGORY,\n" +
                "	OMPDRBL.LAST_STORED_TIME,\n" +
                "	OMPDRBL.LAST_USED_TIME,\n" +
                "	OMPDRBL.DCJ_ID,\n" +
                "	OMPDRBL.DCJ_RKEY,\n" +
                "	OMPDRBL.DRBL_PRFCX_RKEY,\n" +
                "	OMPDRBL.RSV_USER_ID,\n" +
                "	OMPDRBL.DRBL_PRODUCTION_STATE,\n" +
                "	OMPDRBL.DRBL_HOLD_STATE,\n" +
                "	OMPDRBL.DRBL_FINISHED_STATE,\n" +
                "	OMPDRBL.DRBL_PROCESS_STATE,\n" +
                "	OMPDRBL.DRBL_INV_STATE,\n" +
                "	OMPDRBL.BANK_ID,\n" +
                "	OMPDRBL.BANK_RKEY,\n" +
                "	OMPDRBL.MAIN_PROCESS_ID,\n" +
                "	OMPDRBL.MAIN_PROCESS_RKEY,\n" +
                "	OMPDRBL.OPE_NO,\n" +
                "	OMPDRBL.BANK_IN_REQD,\n" +
                "	OMPDRBL.PP_FLAG,\n" +
                "	OMPDRBL.PDRBL_SUB_STATE_ID,\n" +
                "	OMPDRBL.PDRBL_SUB_STATE_RKEY,\n" +
                "	OMPDRBL_PDRBLGRP.PDRBL_GRP_ID,\n" +
                "	OMPDRBL_PDRBLGRP.ID as ID_GRP,\n" +
                "	OMPDRBL.RETICLE_LOCATION\n" +
                "FROM\n" +
                "	OMPDRBL,\n" +
                "	OMPDRBL_PDRBLGRP\n" +
                "WHERE\n" +
                "	OMPDRBL.ID = OMPDRBL_PDRBLGRP.REFKEY\n" +
                "AND OMPDRBL.PDRBL_CATEGORY = 'Reticle'";
        List<Object> args = new ArrayList<>();
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            HV_BUFFER += " AND EQP_ID = ? ";
            args.add(equipmentID.getValue());
        }
        if (!CimStringUtils.isEmpty(reticlePartNumber)) {
            HV_BUFFER += " AND DRBL_PART_NO = ? ";
            args.add(reticlePartNumber);
        }
        if (!ObjectIdentifier.isEmpty(reticleID)) {
            HV_BUFFER += " AND PDRBL_ID LIKE ? ";
            args.add(String.format("%s%%", reticleID.getValue()));
        }
        if (!ObjectIdentifier.isEmptyWithValue(reticleGroupID)) {
            HV_BUFFER += " AND OMPDRBL_PDRBLGRP.PDRBL_GRP_ID = ? ";
            args.add(reticleGroupID.getValue());
        }
        if (!CimStringUtils.isEmpty(reticleStatus)) {
            HV_BUFFER += " AND PDRBL_STATE = ? ";
            args.add(reticleStatus);
        }
        if (bWhiteFlag) {
            HV_BUFFER += "AND TEMP_MODE_FLAG = 1 ";
        } else if (!bWhiteFlag && !bSearchAllFlag) {
            HV_BUFFER += "AND TEMP_MODE_FLAG = 0 ";
        }
        if (!CimStringUtils.isEmpty(FPCCategory)) {
            HV_BUFFER += " AND (DOC_CATEGORY = ? OR DOC_CATEGORY = '') ";
            args.add(FPCCategory);
        }
        if (!ObjectIdentifier.isEmpty(bankID)) {
            HV_BUFFER += " AND BANK_ID = ? ";
            args.add(bankID.getValue());
        }
        if (!ObjectIdentifier.isEmpty(durableSubStatus)) {
            HV_BUFFER += " AND PDRBL_SUB_STATE_ID = ? ";
            args.add(durableSubStatus.getValue());
        }
        if (!CimStringUtils.isEmpty(flowStatus)) {
            if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_DURABLE_SCRAPPED)) {
                HV_BUFFER += " AND MAIN_PROCESS_ID IS NOT NULL AND PDRBL_STATE = ? ";
                args.add(flowStatus);
            } else if (CimStringUtils.equals(flowStatus, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
                HV_BUFFER += " AND MAIN_PROCESS_ID IS NOT NULL AND DRBL_HOLD_STATE = ? ";
                args.add(flowStatus);
                HV_BUFFER += " AND PDRBL_STATE <> ? ";
                args.add(CIMStateConst.CIM_DURABLE_SCRAPPED);
            } else if (CimStringUtils.equals(flowStatus, BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED)) {
                HV_BUFFER += " AND MAIN_PROCESS_ID IS NOT NULL AND DRBL_FINISHED_STATE = ? ";
                args.add(flowStatus);
                HV_BUFFER += " AND PDRBL_STATE <> ? ";
                args.add(CIMStateConst.CIM_DURABLE_SCRAPPED);
                HV_BUFFER += " AND DRBL_HOLD_STATE <> ? ";
                args.add(BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD);
            } else if (CimStringUtils.equals(flowStatus, BizConstant.SP_DURABLE_PROCSTATE_WAITING)
                    || CimStringUtils.equals(flowStatus, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING)) {
                HV_BUFFER += " AND MAIN_PROCESS_ID IS NOT NULL AND DRBL_PROCESS_STATE = ? ";
                args.add(flowStatus);
                HV_BUFFER += " AND PDRBL_STATE <> ? ";
                args.add(CIMStateConst.CIM_DURABLE_SCRAPPED);
                HV_BUFFER += " AND DRBL_HOLD_STATE <> ? ";
                args.add(BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD);
                HV_BUFFER += " AND DRBL_FINISHED_STATE <> ? ";
                args.add(BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED);
            }
        }
        boolean bConvertFlag = false;
        String originalSQL = HV_BUFFER;
        List<Object[]> objectsList = cimJpaRepository.query(HV_BUFFER, args.toArray());
        int t_len = StandardProperties.OM_RETICLE_LIST_INITIAL_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
        int count = 0;
        List<Infos.FoundReticle> strFoundReticle = new ArrayList<>();
        for (Object[] objects : objectsList) {
            Object hFRDRBLDRBL_ID = objects[0],
                    hFRDRBLDRBL_OBJ = objects[1],
                    hFRDRBLDESCRIPTION = objects[2],
                    hFRDRBLDRBL_PART_NO = objects[3],
                    hFRDRBLDRBL_SERIAL_NO = objects[4],
                    hFRDRBLDRBL_STATE = objects[5],
                    hFRDRBLTRANS_STATE = objects[6],
                    hFRDRBLEQP_ID = objects[7],
                    hFRDRBLEQP_OBJ = objects[8],
                    hFRDRBLCLAIM_TIME = objects[9],
                    hFRDRBLCLAIM_USER_ID = objects[10],
                    hFRDRBLCLAIM_USER_OBJ = objects[11],
                    hFRDRBLMTRLCONTNR_ID = objects[12],
                    hFRDRBLMTRLCONTNR_OBJ = objects[13];
            hFRDRBLWHITE_FLAG = CimBooleanUtils.convert(objects[14]);
            hFRDRBLFPC_CATEGORY = (String) objects[15];
            Object hFRDRBLSTORE_TIME = objects[16],
                    hFRDRBLLAST_USED_TIME = objects[17],
                    hFRDRBLDCTRLJOB_ID = objects[18],
                    hFRDRBLDCTRLJOB_OBJ = objects[19],
                    hFRDRBLDRBLPFX_OBJ = objects[20],
                    hFRDRBLRESRV_USER_ID = objects[21],
                    hFRDRBLDRBL_PRODCTN_STATE = objects[22],
                    hFRDRBLDRBL_HOLD_STATE = objects[23],
                    hFRDRBLDRBL_FINISHED_STATE = objects[24],
                    hFRDRBLDRBL_PROCESS_STATE = objects[25],
                    hFRDRBLDRBL_INV_STATE = objects[26],
                    hFRDRBLBANK_ID = objects[27],
                    hFRDRBLBANK_OBJ = objects[28],
                    hFRDRBLMAINPD_ID = objects[29],
                    hFRDRBLMAINPD_OBJ = objects[30],
                    hFRDRBLOPE_NO = objects[31],
                    hFRDRBLBANK_IN_REQ = objects[32],
                    hFRDRBLPOST_PROCESS_FLAG = objects[33],
                    hFRDRBLDRBLSUBSTATE_ID = objects[34],
                    hFRDRBLDRBLSUBSTATE_OBJ = objects[35],
                    hFRDRBLGRPDRBLGRP_ID = objects[36],
                    hFRDRBLGRPDRBLGRP_OBJ = objects[37],
                    reticleLocation = objects[38];


            if (count >= t_len) {
                t_len = t_len + StandardProperties.OM_RETICLE_LIST_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
            }
            Infos.FoundReticle foundReticle = new Infos.FoundReticle();
            strFoundReticle.add(foundReticle);
//            strFoundReticle[count] = new Infos.FoundReticle();
            ObjectIdentifier _reticleID = new ObjectIdentifier();
            foundReticle.setReticleID(_reticleID);
            _reticleID.setValue((String) hFRDRBLDRBL_ID);
            _reticleID.setReferenceKey((String) hFRDRBLDRBL_OBJ);
            foundReticle.setDescription((String) hFRDRBLDESCRIPTION);
            ObjectIdentifier _reticleGroupID = new ObjectIdentifier();
            foundReticle.setReticleGroupID(_reticleGroupID);
            _reticleGroupID.setValue((String) hFRDRBLGRPDRBLGRP_ID);
            _reticleGroupID.setReferenceKey((String) hFRDRBLGRPDRBLGRP_OBJ);
            foundReticle.setReticleGroupSequenceNumber(BizConstant.SP_RETICLE_DEFAULTGROUPSEQUENCENUMBER);
            foundReticle.setReticlePartNumber((String) hFRDRBLDRBL_PART_NO);
            foundReticle.setReticleLocation((String) reticleLocation);
            foundReticle.setReticleSerialNumber((String) hFRDRBLDRBL_SERIAL_NO);
            Infos.ReticleStatusInfo reticleStatusInfo = new Infos.ReticleStatusInfo();
            foundReticle.setReticleStatusInfo(reticleStatusInfo);
            reticleStatusInfo.setReticleStatus((String) hFRDRBLDRBL_STATE);
            reticleStatusInfo.setTransferStatus((String) hFRDRBLTRANS_STATE);
            ObjectIdentifier _reticlePodID = new ObjectIdentifier();
            reticleStatusInfo.setReticlePodID(_reticlePodID);
            _reticlePodID.setValue((String) hFRDRBLMTRLCONTNR_ID);
            _reticlePodID.setReferenceKey((String) hFRDRBLMTRLCONTNR_OBJ);
            if (CimStringUtils.equals((String) hFRDRBLTRANS_STATE, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                    || CimStringUtils.equals((String) hFRDRBLTRANS_STATE, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
                ObjectIdentifier _equipmentID = new ObjectIdentifier();
                reticleStatusInfo.setEquipmentID(_equipmentID);
                _equipmentID.setValue((String) hFRDRBLEQP_ID);
                _equipmentID.setReferenceKey((String) hFRDRBLEQP_OBJ);
            } else {
                ObjectIdentifier _equipmentID = new ObjectIdentifier();
                reticleStatusInfo.setStockerID(_equipmentID);
                _equipmentID.setValue((String) hFRDRBLEQP_ID);
                _equipmentID.setReferenceKey((String) hFRDRBLEQP_OBJ);
            }
            reticleStatusInfo.setLastClaimedTimeStamp((Timestamp) hFRDRBLCLAIM_TIME);
            reticleStatusInfo.setLastClaimedPerson(ObjectIdentifier.build((String) hFRDRBLCLAIM_USER_ID, (String) hFRDRBLCLAIM_USER_OBJ));
            ObjectIdentifier _durableControlJobID = new ObjectIdentifier();
            reticleStatusInfo.setDurableControlJobID(_durableControlJobID);
            _durableControlJobID.setValue((String) hFRDRBLDCTRLJOB_ID);
            _durableControlJobID.setReferenceKey((String) hFRDRBLDCTRLJOB_OBJ);
            if (!CimObjectUtils.isEmpty(hFRDRBLDRBLPFX_OBJ)) {
                reticleStatusInfo.setDurableSTBFlag(true);
            } else {
                reticleStatusInfo.setDurableSTBFlag(false);
            }
            ObjectIdentifier _durableSubStatus = new ObjectIdentifier();
            reticleStatusInfo.setDurableSubStatus(_durableSubStatus);
            _durableSubStatus.setValue((String) hFRDRBLDRBLSUBSTATE_ID);
            _durableSubStatus.setReferenceKey((String) hFRDRBLDRBLSUBSTATE_OBJ);
            availableForDurableFlag = false;
            if (!CimStringUtils.isEmpty((String) hFRDRBLDRBLSUBSTATE_ID)) {
                Pair<Boolean, Object> pair = find(availableForDurableFlagList, hFRDRBLDRBLSUBSTATE_ID, availableForDurableFlag);
                if (!pair.getFirst()) {
                    CimDurableSubState cimDurableSubState = baseCoreFactory.getBO(CimDurableSubState.class, new ObjectIdentifier((String) hFRDRBLDRBLSUBSTATE_ID));
                    Boolean hFRDRBLSUBSTPROC_AVLBL_FLG = cimDurableSubState.isDurableProcessAvailable();
                    availableForDurableFlag = hFRDRBLSUBSTPROC_AVLBL_FLG;
                    availableForDurableFlagList.add(new Object[]{hFRDRBLDRBLSUBSTATE_ID, availableForDurableFlag});
                }
            }
            reticleStatusInfo.setAvailableForDurableFlag(availableForDurableFlag);
            reticleStatusInfo.setInPostProcessFlagOfReticle(CimBooleanUtils.convert(hFRDRBLPOST_PROCESS_FLAG));
            Infos.HashedInfo[] strDurableStatusList = new Infos.HashedInfo[7];
            durableFlowState = "";
            if (!CimObjectUtils.isEmpty(hFRDRBLMAINPD_ID)) {
                if (CimStringUtils.equals((String) hFRDRBLDRBL_STATE, CIMStateConst.CIM_DURABLE_SCRAPPED)) {
                    durableFlowState = (String) hFRDRBLDRBL_STATE;
                } else if (CimStringUtils.equals((String) hFRDRBLDRBL_HOLD_STATE, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
                    durableFlowState = (String) hFRDRBLDRBL_HOLD_STATE;
                } else if (CimStringUtils.equals((String) hFRDRBLDRBL_FINISHED_STATE, BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED)) {
                    durableFlowState = (String) hFRDRBLDRBL_FINISHED_STATE;
                } else {
                    durableFlowState = (String) hFRDRBLDRBL_PROCESS_STATE;
                }
            }
            strDurableStatusList[0] = new Infos.HashedInfo();
            strDurableStatusList[0].setHashKey("durable Flow State");
            strDurableStatusList[0].setHashData(durableFlowState);
            strDurableStatusList[1] = new Infos.HashedInfo();
            strDurableStatusList[1].setHashKey("durable State");
            durableState = "";
            if (!CimObjectUtils.isEmpty(hFRDRBLMAINPD_ID)) {
                if (CimStringUtils.equals((String) hFRDRBLDRBL_STATE, CIMStateConst.CIM_DURABLE_SCRAPPED)
                        || CimStringUtils.equals((String) hFRDRBLDRBL_FINISHED_STATE, BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED)) {
                    durableState = BizConstant.SP_DURABLE_ONROUTESTATE_FINISHED;
                } else {
                    durableState = BizConstant.SP_DURABLE_ONROUTESTATE_ACTIVE;
                }
            }
            strDurableStatusList[1].setHashData(durableState);
            strDurableStatusList[2] = new Infos.HashedInfo();
            strDurableStatusList[2].setHashKey("durable Production State");
            strDurableStatusList[2].setHashData((String) hFRDRBLDRBL_PRODCTN_STATE);
            strDurableStatusList[3] = new Infos.HashedInfo();
            strDurableStatusList[3].setHashKey("durable Hold State");
            strDurableStatusList[3].setHashData((String) hFRDRBLDRBL_HOLD_STATE);
            strDurableStatusList[4] = new Infos.HashedInfo();
            strDurableStatusList[4].setHashKey("durable Finished State");
            strDurableStatusList[4].setHashData((String) hFRDRBLDRBL_FINISHED_STATE);
            strDurableStatusList[5] = new Infos.HashedInfo();
            strDurableStatusList[5].setHashKey("durable Process State");
            strDurableStatusList[5].setHashData((String) hFRDRBLDRBL_PROCESS_STATE);
            strDurableStatusList[6] = new Infos.HashedInfo();
            strDurableStatusList[6].setHashKey("durable Inventory State");
            strDurableStatusList[6].setHashData((String) hFRDRBLDRBL_INV_STATE);
            reticleStatusInfo.setStrDurableStatusList(CimArrayUtils.generateList(strDurableStatusList));
            ObjectIdentifier _aBank = new ObjectIdentifier();
            _aBank.setValue((String) hFRDRBLBANK_ID);
            _aBank.setReferenceKey((String) hFRDRBLBANK_OBJ);
            foundReticle.setBankID(_aBank);
            foundReticle.setDueTimeStamp(null);
            if (!CimStringUtils.isEmpty((String) hFRDRBLMAINPD_ID)) {
                String hFRPDSTART_BANK_ID,
                        hFRPDSTART_BANK_OBJ,
                        hFRPDPD_LEVEL;
                hFRPDPD_LEVEL = BizConstant.SP_PD_FLOWLEVEL_MAIN;
                String sql = String.format("SELECT START_BANK_ID,\n" +
                        "                                START_BANK_RKEY\n" +
                        "                           FROM OMPRP\n" +
                        "                          WHERE PRP_ID = '%s' AND PRP_LEVEL = '%s'", (String) hFRDRBLMAINPD_ID, hFRPDPD_LEVEL);
                CimProcessDefinitionDO processDefinition = cimJpaRepository.queryOne(sql, CimProcessDefinitionDO.class);
                Validations.check(processDefinition == null, retCodeConfig.getSystemError());
                hFRPDSTART_BANK_ID = processDefinition.getStartBankID();
                hFRPDSTART_BANK_OBJ = processDefinition.getStartBankObj();
                ObjectIdentifier _startBankID = new ObjectIdentifier();
                foundReticle.setStartBankID(_startBankID);
                _startBankID.setValue(hFRPDSTART_BANK_ID);
                _startBankID.setReferenceKey(hFRPDSTART_BANK_OBJ);
            }
            //--- Set objectIdentifier routeID; ----------------------------------//
            ObjectIdentifier _routeID = new ObjectIdentifier();
            _routeID.setValue((String) hFRDRBLMAINPD_ID);
            _routeID.setReferenceKey((String) hFRDRBLMAINPD_OBJ);
            foundReticle.setRouteID(_routeID);
            //--- Set string operationNumber; ------------------------------------//
            foundReticle.setOperationNumber((String) hFRDRBLOPE_NO);
            //--- Set boolean bankInRequiredFlag; --------------------------------//
            foundReticle.setBankInRequiredFlag(CimBooleanUtils.convert(hFRDRBLBANK_IN_REQ));
            /***************************/
            /* Get durable hold record */
            /***************************/
            ObjectIdentifier _durableID = new ObjectIdentifier();
            _durableID.setValue((String) hFRDRBLDRBL_ID);
            _durableID.setReferenceKey((String) hFRDRBLDRBL_ID);
            // step1 - durable_holdRecord_GetDR
            List<Infos.DurableHoldRecord> strDurable_holdRecord_GetDR_out = null;
            try {
                strDurable_holdRecord_GetDR_out = durableMethod.durableHoldRecordGetDR(objCommon, _durableID, BizConstant.SP_DURABLECAT_RETICLE);
            } catch (ServiceException e) {
                break;
            }
            //--- Set objectIdentifier holdReasonCodeID; -------------------------//
            int holdLen = strDurable_holdRecord_GetDR_out.size();
            if (holdLen == 1) {
                ObjectIdentifier _holdReasonCodeID = new ObjectIdentifier();
                _holdReasonCodeID.setValue(strDurable_holdRecord_GetDR_out.get(0).getHoldReasonCodeID().getValue());
                _holdReasonCodeID.setReferenceKey("");
                foundReticle.setHoldReasonCodeID(_holdReasonCodeID);
            } else if (holdLen > 1) {
                String holdReasonCode = strDurable_holdRecord_GetDR_out.get(0).getHoldReasonCodeID().getValue();
                holdReasonCode += BizConstant.SP_DEFAULT_CHAR;
                ObjectIdentifier _holdReasonCodeID = new ObjectIdentifier();
                _holdReasonCodeID.setValue(holdReasonCode);
                _holdReasonCodeID.setReferenceKey("");
                foundReticle.setHoldReasonCodeID(_holdReasonCodeID);
            }
            foundReticle.setWhiteDefFlag(hFRDRBLWHITE_FLAG);
            foundReticle.setFpcCategory(hFRDRBLFPC_CATEGORY);
            foundReticle.setStoreTimeStamp((Timestamp) hFRDRBLSTORE_TIME);
            foundReticle.setLastUsedTimeStamp((Timestamp) hFRDRBLLAST_USED_TIME);
            count++;
            if (count >= fetchLimitCount) {
                break;
            }
        }
        Validations.check(count == 0, new OmCode(retCodeConfig.getNotFoundReticle(), "*****"));
        if (CimArrayUtils.getSize(strFoundReticle) > 0) {
            strReticleListInqResult.setStrFoundReticle(strFoundReticle);
        }
        return strReticleListInqResult;
    }

    /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/26 10:21                     Aoki                Create
    *
    * @author Aoki
    * @date 2021/3/26 10:21
    * @param objCommon
     * @param reticleListInqParams
    * @return com.fa.cim.dto.Results.ReticleListInqResult
    */
    @Override
    public Results.PageReticleListInqResult reticleListGetDRForPage(Infos.ObjCommon objCommon, Params.ReticleListInqParams reticleListInqParams) {
        Results.PageReticleListInqResult strReticleListInqResult = new Results.PageReticleListInqResult();
        int searchCondition = 0;
        String searchCondition_var = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if (!CimStringUtils.isEmpty(searchCondition_var)) {
            searchCondition = Integer.parseInt(searchCondition_var);
        }
        long fetchLimitCount = 0;
        String reticlePartNumber, reticleStatus, FPCCategory, whiteDefSearchCriteria, flowStatus, durableFlowState, durableState;
        ObjectIdentifier equipmentID, lotID, reticleID, reticleGroupID, bankID, durableSubStatus;
        long maxRetrieveCount = 0;
        Boolean availableForDurableFlag = false;
        List<Object[]> availableForDurableFlagList = new ArrayList<>();
        equipmentID = reticleListInqParams.getEquipmentID();
        lotID = reticleListInqParams.getLotID();
        reticleID = reticleListInqParams.getReticleID();
        reticlePartNumber = reticleListInqParams.getReticlePartNumber();
        reticleGroupID = reticleListInqParams.getReticleGroupID();
        reticleStatus = reticleListInqParams.getReticleStatus();
        maxRetrieveCount = reticleListInqParams.getMaxRetrieveCount();
        FPCCategory = reticleListInqParams.getFPCCategory();
        whiteDefSearchCriteria = reticleListInqParams.getWhiteDefSearchCriteria();
        bankID = reticleListInqParams.getBankID();
        durableSubStatus = reticleListInqParams.getDurableSubStatus();
        flowStatus = reticleListInqParams.getFlowStatus();
        if (maxRetrieveCount <= 0 ||
                maxRetrieveCount > StandardProperties.OM_RETICLE_LIST_MAX_LEN_FOR_RETICLE_LIST_INQ.getIntValue()) {
            fetchLimitCount = StandardProperties.OM_RETICLE_LIST_MAX_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
        } else {
            fetchLimitCount = maxRetrieveCount;
        }
        Boolean hFRDRBLWHITE_FLAG = false;
        String hFRDRBLFPC_CATEGORY = "";
        Boolean bWhiteFlag = false, bSearchAllFlag = false;
        if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_WHITE)) {
            bWhiteFlag = true;
        } else if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_NONWHITE)) {

        } else if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_ALL)) {
            bSearchAllFlag = true;
        } else {
            Validations.check(true, retCodeConfig.getInvalidParameter());
        }
        if (ObjectIdentifier.isEmptyWithValue(equipmentID) && (ObjectIdentifier.isNotEmptyWithValue(lotID))) {
            com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
            com.fa.cim.newcore.bo.pd.CimProcessOperation aProcessOperation = aLot.getProcessOperation();
            Validations.check(aProcessOperation == null, retCodeConfig.getNoNeedReticle());
            CimProductSpecification aProductSpecification = aLot.getProductSpecification();
            com.fa.cim.newcore.bo.recipe.CimLogicalRecipe aLogicalRecipe = aProcessOperation.findLogicalRecipeFor(aProductSpecification);
            Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicRecipe());
            List<CimProcessDurableCapability> aProcessDurableCapabilities = aLot.getReticleGroupsFor("*");
            int i, nReticlePDCs = aProcessDurableCapabilities.size();
            Validations.check(nReticlePDCs == 0, retCodeConfig.getNoNeedReticle());
            Infos.FoundReticle[] strFoundReticle;
            int j, l, count = 0;
            int t_count = StandardProperties.OM_RETICLE_LIST_INITIAL_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
            strFoundReticle = new Infos.FoundReticle[t_count];
            for (i = 0; i < nReticlePDCs; i++) {
                CimProcessDurableCapability aProcessDurableCapability = aProcessDurableCapabilities.get(i);
                List<CimProcessDurable> strProcessDurables = aProcessDurableCapability.allAssignedProcessDurables();
                ObjectIdentifier aReticleGroupID = new ObjectIdentifier();
                aReticleGroupID.setValue(aProcessDurableCapability.getIdentifier());
                aReticleGroupID.setReferenceKey(aProcessDurableCapability.getPrimaryKey());
                int PDLen = strProcessDurables.size();
                if ((count + PDLen) >= t_count) {
                    int tmp_len = StandardProperties.OM_RETICLE_LIST_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
                    if ((count + PDLen) > (t_count + tmp_len)) {
                        int tmp_len2 = (count + PDLen - t_count);
                        t_count = t_count + ((tmp_len2 + tmp_len - 1) / tmp_len) * tmp_len;
                    } else {
                        t_count = t_count + tmp_len;
                    }
                    strFoundReticle = Arrays.copyOf(strFoundReticle, t_count);
                }
                for (j = 0; j < PDLen; j++) {
                    CimProcessDurable strProcessDurable = strProcessDurables.get(j);
                    String aReticleIdentifier = strProcessDurable.getIdentifier();
                    for (l = 0; l < count; l++) {
                        if (CimStringUtils.equals(aReticleIdentifier, strFoundReticle[l].getReticleID().getValue())) {
                            break;
                        }
                    }
                    if (l < count) {
                        continue;
                    }
                    boolean whiteDefFlag = strProcessDurable.isWhiteFlagOn();
                    if (!bSearchAllFlag && bWhiteFlag) {
                        if (!whiteDefFlag) {
                            continue;
                        }
                    } else if (!bSearchAllFlag && !bWhiteFlag) {
                        if (whiteDefFlag) {
                            continue;
                        }
                    }
                    String fpcCategory = strProcessDurable.getFPCCategory();
                    if (!CimStringUtils.equals(fpcCategory, FPCCategory) &&
                            !CimStringUtils.isEmpty(fpcCategory)) {
                        continue;
                    }
                    strFoundReticle[count] = new Infos.FoundReticle();
                    strFoundReticle[count].setWhiteDefFlag(whiteDefFlag);
                    strFoundReticle[count].setFpcCategory(fpcCategory);
                    ObjectIdentifier _reticleID = new ObjectIdentifier();
                    _reticleID.setValue(aReticleIdentifier);
                    strFoundReticle[count].setReticleID(_reticleID);
                    _reticleID.setReferenceKey(strProcessDurable.getPrimaryKey());
                    strFoundReticle[count].setDescription(strProcessDurable.getProcessDurableDescription());
                    strFoundReticle[count].setReticleGroupID(aReticleGroupID);
                    aReticleGroupID.setReferenceKey(strProcessDurable.getPrimaryKey());
                    strFoundReticle[count].setReticleGroupSequenceNumber(BizConstant.SP_RETICLE_DEFAULTGROUPSEQUENCENUMBER);
                    strFoundReticle[count].setReticlePartNumber(strProcessDurable.getPartNumber());
                    strFoundReticle[count].setReticleSerialNumber(strProcessDurable.getSerialNumber());
                    Infos.ReticleStatusInfo reticleStatusInfo = new Infos.ReticleStatusInfo();
                    reticleStatusInfo.setReticleStatus(strProcessDurable.getDurableState());
                    strFoundReticle[count].setReticleStatusInfo(reticleStatusInfo);
                    reticleStatusInfo.setTransferStatus(strProcessDurable.getTransportState());
                    Machine aMachine = strProcessDurable.currentAssignedMachine();
                    if (CimStringUtils.equals(reticleStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                            || CimStringUtils.equals(reticleStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
                        ObjectIdentifier _equipmentID = new ObjectIdentifier();
                        reticleStatusInfo.setEquipmentID(_equipmentID);
                        if (aMachine != null) {
                            _equipmentID.setValue(aMachine.getIdentifier());
                            _equipmentID.setReferenceKey(aMachine.getPrimaryKey());
                        }
                    } else {
                        ObjectIdentifier _stockerID = new ObjectIdentifier();
                        reticleStatusInfo.setStockerID(_stockerID);
                        if (aMachine != null) {
                            _stockerID.setValue(aMachine.getIdentifier());
                            _stockerID.setReferenceKey(aMachine.getPrimaryKey());
                        }
                    }
                    reticleStatusInfo.setLastClaimedTimeStamp(strProcessDurable.getLastClaimedTimeStamp());
                    reticleStatusInfo.setLastClaimedPerson(new ObjectIdentifier(strProcessDurable.getLastClaimedPersonID()));
                    MaterialContainer aMtrlCntnr = strProcessDurable.getMaterialContainer();
                    CimReticlePod aReticlePod = (CimReticlePod) aMtrlCntnr;
                    ObjectIdentifier _reticlePodID = new ObjectIdentifier();
                    reticleStatusInfo.setReticlePodID(_reticlePodID);
                    if (aReticlePod != null) {
                        _reticlePodID.setValue(aReticlePod.getIdentifier());
                        _reticlePodID.setReferenceKey(aReticlePod.getPrimaryKey());
                    }
                    CimDurableControlJob aDurableCJ = strProcessDurable.getDurableControlJob();
                    ObjectIdentifier _aDurableCJ = new ObjectIdentifier();
                    if (aDurableCJ != null) {
                        _aDurableCJ.setValue(aDurableCJ.getIdentifier());
                        _aDurableCJ.setReferenceKey(aDurableCJ.getPrimaryKey());
                    }
                    reticleStatusInfo.setDurableControlJobID(_aDurableCJ);
                    CimDurableProcessFlowContext aDurablePFX = strProcessDurable.getDurableProcessFlowContext();
                    if (aDurablePFX != null) {
                        reticleStatusInfo.setDurableSTBFlag(true);
                    } else {
                        reticleStatusInfo.setDurableSTBFlag(false);
                    }
                    availableForDurableFlag = false;
                    ObjectIdentifier _aDurableSubState = new ObjectIdentifier();
                    CimDurableSubState aDurableSubState = strProcessDurable.getDurableSubState();
                    if (aDurableSubState != null) {
                        reticleStatusInfo.setDurableSubStatus(_aDurableSubState);
                        _aDurableSubState.setValue(aDurableSubState.getIdentifier());
                        _aDurableSubState.setReferenceKey(aDurableSubState.getPrimaryKey());
                        availableForDurableFlag = aDurableSubState.isDurableProcessAvailable();
                    }
                    reticleStatusInfo.setAvailableForDurableFlag(availableForDurableFlag);
                    String reservePerson = strProcessDurable.getReservePersonID();
                    Infos.HashedInfo[] strDurableStatusList = new Infos.HashedInfo[7];
                    strDurableStatusList[0] = new Infos.HashedInfo();
                    strDurableStatusList[0].setHashKey("durable Flow State");
                    strDurableStatusList[0].setHashData("");// TODO: (*strProcessDurables)[j]->getDurableFlowState();
                    strDurableStatusList[1] = new Infos.HashedInfo();
                    strDurableStatusList[1].setHashKey("durable State");
                    strDurableStatusList[1].setHashData("");//TODO:hashData = (*strProcessDurables)[j]->getDurableOnRouteState();
                    strDurableStatusList[2] = new Infos.HashedInfo();
                    strDurableStatusList[2].setHashKey("durable Production State");
                    strDurableStatusList[2].setHashData(strProcessDurable.getDurableProductionState());
                    strDurableStatusList[3] = new Infos.HashedInfo();
                    strDurableStatusList[3].setHashKey("durable Hold State");
                    strDurableStatusList[3].setHashData(strProcessDurable.getDurableHoldState());
                    strDurableStatusList[4] = new Infos.HashedInfo();
                    strDurableStatusList[4].setHashKey("durable Finished State");
                    strDurableStatusList[4].setHashData(strProcessDurable.getDurableFinishedState());
                    strDurableStatusList[5] = new Infos.HashedInfo();
                    strDurableStatusList[5].setHashKey("durable Process State");
                    strDurableStatusList[5].setHashData(strProcessDurable.getDurableProcessState());
                    strDurableStatusList[6] = new Infos.HashedInfo();
                    strDurableStatusList[6].setHashKey("durable Inventory State");
                    strDurableStatusList[6].setHashData(strProcessDurable.getDurableInventoryState());
                    reticleStatusInfo.setStrDurableStatusList(Arrays.asList(strDurableStatusList));
                    reticleStatusInfo.setInPostProcessFlagOfReticle(strProcessDurable.isPostProcessFlagOn());
                    ObjectIdentifier _aBank = new ObjectIdentifier();
                    CimBank aBank = strProcessDurable.getBank();
                    strFoundReticle[count].setBankID(_aBank);
                    if (aBank != null) {
                        _aBank.setValue(aBank.getIdentifier());
                        _aBank.setReferenceKey(aBank.getPrimaryKey());
                    }
                    strFoundReticle[count].setDueTimeStamp(null);
                    ObjectIdentifier _aMainPD = new ObjectIdentifier();
                    com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainPD = strProcessDurable.getMainProcessDefinition();
                    strFoundReticle[count].setRouteID(_aMainPD);
                    if (aMainPD != null) {
                        _aMainPD.setValue(aMainPD.getIdentifier());
                        _aMainPD.setReferenceKey(aMainPD.getPrimaryKey());
                    }
                    strFoundReticle[count].setBankInRequiredFlag(strProcessDurable.isBankInRequired());
                    List<DurableDTO.PosDurableHoldRecord> strHoldRecords = strProcessDurable.allHoldRecords();
                    int holdLen = CimArrayUtils.getSize(strHoldRecords);
                    if (holdLen == 1) {
                        ObjectIdentifier _holdReasonCodeID = strHoldRecords.get(0).getReasonCode();
                        strFoundReticle[count].setHoldReasonCodeID(_holdReasonCodeID);
                    } else if (holdLen > 1) {
                        String holdReasonCode = strHoldRecords.get(0).getReasonCode().getValue() + BizConstant.SP_DEFAULT_CHAR;
                        ObjectIdentifier _holdReasonCodeID = new ObjectIdentifier();
                        _holdReasonCodeID.setValue(holdReasonCode);
                        _holdReasonCodeID.setReferenceKey("");
                        strFoundReticle[count].setHoldReasonCodeID(_holdReasonCodeID);
                    }
                    count++;
                }
            }
            strFoundReticle = Arrays.copyOf(strFoundReticle, count);
            Validations.check(strFoundReticle.length <= 0, new OmCode(retCodeConfig.getNotFoundReticle(), "*****"));
            strReticleListInqResult.setStrFoundReticlelList(CimPageUtils.convertListToPage(Arrays.asList(strFoundReticle),reticleListInqParams.getSearchCondition().getPage(),reticleListInqParams.getSearchCondition().getSize()));
            return strReticleListInqResult;
        } else if (ObjectIdentifier.isNotEmptyWithValue(equipmentID) && ObjectIdentifier.isNotEmptyWithValue(lotID)) {
            com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
            CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
            Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
            com.fa.cim.newcore.bo.pd.CimProcessOperation aProcessOperation = aLot.getProcessOperation();
            Validations.check(aProcessOperation == null, retCodeConfig.getNoNeedReticle());
            CimProductSpecification aProductSpecification = aLot.getProductSpecification();
            com.fa.cim.newcore.bo.recipe.CimLogicalRecipe aLogicalRecipe = aProcessOperation.findLogicalRecipeFor(aProductSpecification);
            Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicRecipe());
            String subLotType = aLot.getSubLotType();
            CimMachineRecipe aMachineRecipe = null;
            if (searchCondition == 1) {
                aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLot, aMachine);
            } else {
                aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, subLotType);
            }
            /*Validations.check(aMachineRecipe == null, retCodeConfig.getNoNeedReticle());*/
            String anEqpID = aMachine.getIdentifier();
            List<CimProcessDurableCapability> aProcessDurableCapabilitySeq = aLot.getReticleGroupsFor(anEqpID);
            int j, k, l, PDCLen, PDLen, count = 0;
            Infos.FoundReticle[] strFoundReticle = null;
            PDCLen = aProcessDurableCapabilitySeq.size();
            Validations.check(PDCLen == 0, retCodeConfig.getNoNeedReticle());
            int t_count = StandardProperties.OM_RETICLE_LIST_INITIAL_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
            Machine aMachineInner = null;
            strFoundReticle = new Infos.FoundReticle[t_count];
            for (j = 0; j < PDCLen; j++) {
                CimProcessDurableCapability aProcessDurableCapability = aProcessDurableCapabilitySeq.get(j);
                List<CimProcessDurable> tempAssignedProcessDurablesSeq = aProcessDurableCapability.allAssignedProcessDurables();
                List<CimProcessDurable> allAssignedProcessDurablesSeq = new ArrayList<>();
                for (CimProcessDurable cimProcessDurable : tempAssignedProcessDurablesSeq) {
                    Machine machine = cimProcessDurable.currentAssignedMachine();
                    if (!CimObjectUtils.isEmpty(machine)) {
                        String temEqpID = machine.getIdentifier();
                        if (CimStringUtils.equals(temEqpID, equipmentID.getValue())) {
                            allAssignedProcessDurablesSeq.add(cimProcessDurable);
                        }
                    }
                }
                PDLen = allAssignedProcessDurablesSeq.size();
                if ((count + PDLen) >= t_count) {
                    int tmp_len = StandardProperties.OM_RETICLE_LIST_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
                    if ((count + PDLen) > (t_count + tmp_len)) {
                        int tmp_len2 = (count + PDLen - t_count);
                        t_count = t_count + ((tmp_len2 + tmp_len - 1) / tmp_len) * tmp_len;
                    } else {
                        t_count = t_count + tmp_len;
                    }
                    strFoundReticle = Arrays.copyOf(strFoundReticle, t_count);
                }
                ObjectIdentifier aReticleGroupID = new ObjectIdentifier();
                aReticleGroupID.setValue(aProcessDurableCapability.getIdentifier());
                aReticleGroupID.setReferenceKey(aProcessDurableCapability.getPrimaryKey());
                String aReticleIdentifier = null;
                for (k = 0; k < PDLen; k++) {
                    aReticleIdentifier = allAssignedProcessDurablesSeq.get(k).getIdentifier();
                    for (l = 0; l < count; l++) {
                        if (CimStringUtils.equals(aReticleIdentifier, strFoundReticle[l].getReticleID().getValue())) {
                            break;
                        }
                    }
                    if (l < count) {
                        continue;
                    }
                    boolean whiteDefFlag = allAssignedProcessDurablesSeq.get(k).isWhiteFlagOn();
                    if (!bSearchAllFlag && bWhiteFlag) {
                        if (!whiteDefFlag) {
                            continue;
                        }
                    } else if (!bSearchAllFlag && !bWhiteFlag) {
                        if (whiteDefFlag) {
                            continue;
                        }
                    }
                    String fpcCategory = allAssignedProcessDurablesSeq.get(k).getFPCCategory();
                    if (CimStringUtils.equals(fpcCategory, FPCCategory) &&
                            !CimStringUtils.isEmpty(fpcCategory)) {
                        continue;
                    }
                    strFoundReticle[count] = new Infos.FoundReticle();
                    strFoundReticle[count].setWhiteDefFlag(whiteDefFlag);
                    strFoundReticle[count].setFpcCategory(fpcCategory);
                    ObjectIdentifier _reticleID = new ObjectIdentifier();
                    _reticleID.setValue(aReticleIdentifier);
                    _reticleID.setReferenceKey(allAssignedProcessDurablesSeq.get(k).getPrimaryKey());
                    strFoundReticle[count].setReticleID(_reticleID);
                    strFoundReticle[count].setDescription(allAssignedProcessDurablesSeq.get(k).getProcessDurableDescription());
                    strFoundReticle[count].setReticleGroupID(aReticleGroupID);
                    strFoundReticle[count].setReticleGroupSequenceNumber(j);
                    strFoundReticle[count].setReticleLocation(allAssignedProcessDurablesSeq.get(k).getReticleLocation());
                    strFoundReticle[count].setReticlePartNumber(allAssignedProcessDurablesSeq.get(k).getPartNumber());
                    strFoundReticle[count].setReticleSerialNumber(allAssignedProcessDurablesSeq.get(k).getSerialNumber());
                    Infos.ReticleStatusInfo reticleStatusInfo = new Infos.ReticleStatusInfo();
                    strFoundReticle[count].setReticleStatusInfo(reticleStatusInfo);
                    reticleStatusInfo.setReticleStatus(allAssignedProcessDurablesSeq.get(k).getDurableState());
                    reticleStatusInfo.setTransferStatus(allAssignedProcessDurablesSeq.get(k).getTransportState());
                    aMachineInner = allAssignedProcessDurablesSeq.get(k).currentAssignedMachine();
                    ObjectIdentifier machineIDToFill = new ObjectIdentifier();
                    if (aMachineInner != null) {
                        machineIDToFill.setValue(aMachineInner.getIdentifier());
                        machineIDToFill.setReferenceKey(aMachineInner.getPrimaryKey());
                    }
                    if (CimStringUtils.equals(reticleStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                            || CimStringUtils.equals(reticleStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
                        reticleStatusInfo.setEquipmentID(machineIDToFill);
                    } else {
                        reticleStatusInfo.setStockerID(machineIDToFill);
                    }
                    reticleStatusInfo.setLastClaimedTimeStamp(allAssignedProcessDurablesSeq.get(k).getLastClaimedTimeStamp());
                    reticleStatusInfo.setLastClaimedPerson(new ObjectIdentifier(allAssignedProcessDurablesSeq.get(k).getLastClaimedPersonID()));
                    ObjectIdentifier _materialContainer = new ObjectIdentifier();
                    MaterialContainer aMtrlCntnr = allAssignedProcessDurablesSeq.get(k).getMaterialContainer();
                    CimReticlePod aReticlePod = (CimReticlePod) aMtrlCntnr;
                    reticleStatusInfo.setReticlePodID(_materialContainer);
                    if (aReticlePod != null) {
                        _materialContainer.setValue(aReticlePod.getIdentifier());
                        _materialContainer.setReferenceKey(aReticlePod.getPrimaryKey());
                    }
                    ObjectIdentifier _aDurableCJ = new ObjectIdentifier();
                    CimDurableControlJob aDurableCJ = allAssignedProcessDurablesSeq.get(k).getDurableControlJob();
                    reticleStatusInfo.setDurableControlJobID(_aDurableCJ);
                    if (aDurableCJ != null) {
                        _aDurableCJ = new ObjectIdentifier(aDurableCJ.getIdentifier(), aDurableCJ.getPrimaryKey());
                    }
                    CimDurableProcessFlowContext aDurablePFX = allAssignedProcessDurablesSeq.get(k).getDurableProcessFlowContext();
                    if (aDurablePFX != null) {
                        reticleStatusInfo.setDurableSTBFlag(true);
                    } else {
                        reticleStatusInfo.setDurableSTBFlag(false);
                    }
                    availableForDurableFlag = false;
                    ObjectIdentifier _aDurableSubState = new ObjectIdentifier();
                    CimDurableSubState aDurableSubState = allAssignedProcessDurablesSeq.get(k).getDurableSubState();
                    if (aDurableSubState != null) {
                        reticleStatusInfo.setDurableSubStatus(_aDurableSubState);
                        _aDurableSubState.setValue(aDurableSubState.getIdentifier());
                        _aDurableSubState.setReferenceKey(aDurableSubState.getPrimaryKey());
                        availableForDurableFlag = aDurableSubState.isDurableProcessAvailable();
                    }
                    reticleStatusInfo.setAvailableForDurableFlag(availableForDurableFlag);
                    /*--------------------------------*/
                    /*  Get Reticle reserve person    */
                    /*--------------------------------*/
                    String reservePerson = allAssignedProcessDurablesSeq.get(k).getReservePersonID();
                    Infos.HashedInfo[] strDurableStatusList = new Infos.HashedInfo[7];
                    strDurableStatusList[0] = new Infos.HashedInfo();
                    strDurableStatusList[0].setHashKey("durable Flow State");
                    strDurableStatusList[0].setHashData("");// TODO: (*strProcessDurables)[j]->getDurableFlowState();
                    strDurableStatusList[1] = new Infos.HashedInfo();
                    strDurableStatusList[1].setHashKey("durable State");
                    strDurableStatusList[1].setHashData("");//TODO:hashData = (*strProcessDurables)[j]->getDurableOnRouteState();
                    strDurableStatusList[2] = new Infos.HashedInfo();
                    strDurableStatusList[2].setHashKey("durable Production State");
                    strDurableStatusList[2].setHashData(allAssignedProcessDurablesSeq.get(k).getDurableProductionState());
                    strDurableStatusList[3] = new Infos.HashedInfo();
                    strDurableStatusList[3].setHashKey("durable Hold State");
                    strDurableStatusList[3].setHashData(allAssignedProcessDurablesSeq.get(k).getDurableHoldState());
                    strDurableStatusList[4] = new Infos.HashedInfo();
                    strDurableStatusList[4].setHashKey("durable Finished State");
                    strDurableStatusList[4].setHashData(allAssignedProcessDurablesSeq.get(k).getDurableFinishedState());
                    strDurableStatusList[5] = new Infos.HashedInfo();
                    strDurableStatusList[5].setHashKey("durable Process State");
                    strDurableStatusList[5].setHashData(allAssignedProcessDurablesSeq.get(k).getDurableProcessState());
                    strDurableStatusList[6] = new Infos.HashedInfo();
                    strDurableStatusList[6].setHashKey("durable Inventory State");
                    strDurableStatusList[6].setHashData(allAssignedProcessDurablesSeq.get(k).getDurableInventoryState());
                    reticleStatusInfo.setStrDurableStatusList(Arrays.asList(strDurableStatusList));
                    reticleStatusInfo.setInPostProcessFlagOfReticle(allAssignedProcessDurablesSeq.get(k).isPostProcessFlagOn());
                    //--- Set objectIdentifier bankID; ------------------------------------//
                    ObjectIdentifier _aBank = new ObjectIdentifier();
                    CimBank aBank = allAssignedProcessDurablesSeq.get(k).getBank();
                    strFoundReticle[count].setBankID(_aBank);
                    if (aBank != null) {
                        _aBank.setValue(aBank.getIdentifier());
                        _aBank.setReferenceKey(aBank.getPrimaryKey());
                    }
                    strFoundReticle[count].setDueTimeStamp(null);
                    ObjectIdentifier _aMainPD = new ObjectIdentifier();
                    com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainPD = allAssignedProcessDurablesSeq.get(k).getMainProcessDefinition();
                    strFoundReticle[count].setRouteID(_aMainPD);
                    if (aMainPD != null) {
                        _aMainPD.setValue(aMainPD.getIdentifier());
                        _aMainPD.setReferenceKey(aMainPD.getPrimaryKey());
                    }
                    strFoundReticle[count].setBankInRequiredFlag(allAssignedProcessDurablesSeq.get(k).isBankInRequired());
                    List<DurableDTO.PosDurableHoldRecord> strHoldRecords = allAssignedProcessDurablesSeq.get(k).allHoldRecords();
                    int holdLen = strHoldRecords.size();
                    if (holdLen == 1) {
                        strFoundReticle[count].setHoldReasonCodeID(strHoldRecords.get(0).getReasonCode());
                    } else if (holdLen > 1) {
                        String holdReasonCode = strHoldRecords.get(0).getReasonCode().getValue() + BizConstant.SP_DEFAULT_CHAR;
                        ObjectIdentifier _holdReasonCodeID = new ObjectIdentifier();
                        _holdReasonCodeID.setValue(holdReasonCode);
                        strFoundReticle[count].setHoldReasonCodeID(_holdReasonCodeID);
                    }
                    count++;
                }
            }
            strFoundReticle = Arrays.copyOf(strFoundReticle, count);
            Validations.check(strFoundReticle.length <= 0, new OmCode(retCodeConfig.getNotFoundReticle(), "*****"));
            strReticleListInqResult.setStrFoundReticlelList(CimPageUtils.convertListToPage(Arrays.asList(strFoundReticle),reticleListInqParams.getSearchCondition().getPage(),reticleListInqParams.getSearchCondition().getSize()));
            return strReticleListInqResult;
        }
        String HV_BUFFER = "SELECT\n" +
                "	OMPDRBL.PDRBL_ID,\n" +
                "	OMPDRBL.ID,\n" +
                "	OMPDRBL.DESCRIPTION,\n" +
                "	OMPDRBL.DRBL_PART_NO,\n" +
                "	OMPDRBL.DRBL_SERIAL_NO,\n" +
                "	OMPDRBL.PDRBL_STATE,\n" +
                "	OMPDRBL.XFER_STATE,\n" +
                "	OMPDRBL.EQP_ID,\n" +
                "	OMPDRBL.EQP_RKEY,\n" +
                "	OMPDRBL.LAST_TRX_TIME,\n" +
                "	OMPDRBL.LAST_TRX_USER_ID,\n" +
                "	OMPDRBL.LAST_TRX_USER_RKEY,\n" +
                "	OMPDRBL.MTRL_CONT_ID,\n" +
                "	OMPDRBL.MTRL_CONT_RKEY,\n" +
                "	OMPDRBL.TEMP_MODE_FLAG,\n" +
                "	OMPDRBL.DOC_CATEGORY,\n" +
                "	OMPDRBL.LAST_STORED_TIME,\n" +
                "	OMPDRBL.LAST_USED_TIME,\n" +
                "	OMPDRBL.DCJ_ID,\n" +
                "	OMPDRBL.DCJ_RKEY,\n" +
                "	OMPDRBL.DRBL_PRFCX_RKEY,\n" +
                "	OMPDRBL.RSV_USER_ID,\n" +
                "	OMPDRBL.DRBL_PRODUCTION_STATE,\n" +
                "	OMPDRBL.DRBL_HOLD_STATE,\n" +
                "	OMPDRBL.DRBL_FINISHED_STATE,\n" +
                "	OMPDRBL.DRBL_PROCESS_STATE,\n" +
                "	OMPDRBL.DRBL_INV_STATE,\n" +
                "	OMPDRBL.BANK_ID,\n" +
                "	OMPDRBL.BANK_RKEY,\n" +
                "	OMPDRBL.MAIN_PROCESS_ID,\n" +
                "	OMPDRBL.MAIN_PROCESS_RKEY,\n" +
                "	OMPDRBL.OPE_NO,\n" +
                "	OMPDRBL.BANK_IN_REQD,\n" +
                "	OMPDRBL.PP_FLAG,\n" +
                "	OMPDRBL.PDRBL_SUB_STATE_ID,\n" +
                "	OMPDRBL.PDRBL_SUB_STATE_RKEY,\n" +
                "	OMPDRBL_PDRBLGRP.PDRBL_GRP_ID,\n" +
                "	OMPDRBL_PDRBLGRP.ID as ID_GRP,\n" +
                "	OMPDRBL.RETICLE_LOCATION\n" +
                "FROM\n" +
                "	OMPDRBL,\n" +
                "	OMPDRBL_PDRBLGRP\n" +
                "WHERE\n" +
                "	OMPDRBL.ID = OMPDRBL_PDRBLGRP.REFKEY\n" +
                "AND OMPDRBL.PDRBL_CATEGORY = 'Reticle'";
        List<Object> args = new ArrayList<>();
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            HV_BUFFER += " AND EQP_ID = ? ";
            args.add(equipmentID.getValue());
        }
        if (!CimStringUtils.isEmpty(reticlePartNumber)) {
            HV_BUFFER += " AND DRBL_PART_NO = ? ";
            args.add(reticlePartNumber);
        }
        if (!ObjectIdentifier.isEmpty(reticleID)) {
            HV_BUFFER += " AND PDRBL_ID LIKE ? ";
            args.add(String.format("%s%%", reticleID.getValue()));
        }
        if (!ObjectIdentifier.isEmptyWithValue(reticleGroupID)) {
            HV_BUFFER += " AND OMPDRBL_PDRBLGRP.PDRBL_GRP_ID = ? ";
            args.add(reticleGroupID.getValue());
        }
        if (!CimStringUtils.isEmpty(reticleStatus)) {
            HV_BUFFER += " AND PDRBL_STATE = ? ";
            args.add(reticleStatus);
        }
        if (bWhiteFlag) {
            HV_BUFFER += "AND TEMP_MODE_FLAG = 1 ";
        } else if (!bWhiteFlag && !bSearchAllFlag) {
            HV_BUFFER += "AND TEMP_MODE_FLAG = 0 ";
        }
        if (!CimStringUtils.isEmpty(FPCCategory)) {
            HV_BUFFER += " AND (DOC_CATEGORY = ? OR DOC_CATEGORY = '') ";
            args.add(FPCCategory);
        }
        if (!ObjectIdentifier.isEmpty(bankID)) {
            HV_BUFFER += " AND BANK_ID = ? ";
            args.add(bankID.getValue());
        }
        if (!ObjectIdentifier.isEmpty(durableSubStatus)) {
            HV_BUFFER += " AND PDRBL_SUB_STATE_ID = ? ";
            args.add(durableSubStatus.getValue());
        }
        if (!CimStringUtils.isEmpty(flowStatus)) {
            if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_DURABLE_SCRAPPED)) {
                HV_BUFFER += " AND MAIN_PROCESS_ID <> '' AND PDRBL_STATE = ? ";
                args.add(flowStatus);
            } else if (CimStringUtils.equals(flowStatus, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
                HV_BUFFER += " AND MAIN_PROCESS_ID <> '' AND DRBL_HOLD_STATE = ? ";
                args.add(flowStatus);
                HV_BUFFER += " AND PDRBL_STATE <> ? ";
                args.add(CIMStateConst.CIM_DURABLE_SCRAPPED);
            } else if (CimStringUtils.equals(flowStatus, BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED)) {
                HV_BUFFER += " AND MAIN_PROCESS_ID <> '' AND DRBL_FINISHED_STATE = ? ";
                args.add(flowStatus);
                HV_BUFFER += " AND PDRBL_STATE <> ? ";
                args.add(CIMStateConst.CIM_DURABLE_SCRAPPED);
                HV_BUFFER += " AND DRBL_HOLD_STATE <> ? ";
                args.add(BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD);
            } else if (CimStringUtils.equals(flowStatus, BizConstant.SP_DURABLE_PROCSTATE_WAITING)
                    || CimStringUtils.equals(flowStatus, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING)) {
                HV_BUFFER += " AND MAIN_PROCESS_ID <> '' AND DRBL_PROCESS_STATE = ? ";
                args.add(flowStatus);
                HV_BUFFER += " AND PDRBL_STATE <> ? ";
                args.add(CIMStateConst.CIM_DURABLE_SCRAPPED);
                HV_BUFFER += " AND DRBL_HOLD_STATE <> ? ";
                args.add(BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD);
                HV_BUFFER += " AND DRBL_FINISHED_STATE <> ? ";
                args.add(BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED);
            }
        }
        boolean bConvertFlag = false;
        String originalSQL = HV_BUFFER;
        Page<Object[]> pageinfo = cimJpaRepository.query(HV_BUFFER,reticleListInqParams.getSearchCondition(),args.toArray(new String[args.size()]));
        List<Object[]> objectsList = pageinfo.getContent();
        int t_len = StandardProperties.OM_RETICLE_LIST_INITIAL_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
        int count = 0;
        List<Infos.FoundReticle> strFoundReticle = new ArrayList<>();
        for (Object[] objects : objectsList) {
            Object hFRDRBLDRBL_ID = objects[0],
                    hFRDRBLDRBL_OBJ = objects[1],
                    hFRDRBLDESCRIPTION = objects[2],
                    hFRDRBLDRBL_PART_NO = objects[3],
                    hFRDRBLDRBL_SERIAL_NO = objects[4],
                    hFRDRBLDRBL_STATE = objects[5],
                    hFRDRBLTRANS_STATE = objects[6],
                    hFRDRBLEQP_ID = objects[7],
                    hFRDRBLEQP_OBJ = objects[8],
                    hFRDRBLCLAIM_TIME = objects[9],
                    hFRDRBLCLAIM_USER_ID = objects[10],
                    hFRDRBLCLAIM_USER_OBJ = objects[11],
                    hFRDRBLMTRLCONTNR_ID = objects[12],
                    hFRDRBLMTRLCONTNR_OBJ = objects[13];
            hFRDRBLWHITE_FLAG = CimBooleanUtils.convert(objects[14]);
            hFRDRBLFPC_CATEGORY = (String) objects[15];
            Object hFRDRBLSTORE_TIME = objects[16],
                    hFRDRBLLAST_USED_TIME = objects[17],
                    hFRDRBLDCTRLJOB_ID = objects[18],
                    hFRDRBLDCTRLJOB_OBJ = objects[19],
                    hFRDRBLDRBLPFX_OBJ = objects[20],
                    hFRDRBLRESRV_USER_ID = objects[21],
                    hFRDRBLDRBL_PRODCTN_STATE = objects[22],
                    hFRDRBLDRBL_HOLD_STATE = objects[23],
                    hFRDRBLDRBL_FINISHED_STATE = objects[24],
                    hFRDRBLDRBL_PROCESS_STATE = objects[25],
                    hFRDRBLDRBL_INV_STATE = objects[26],
                    hFRDRBLBANK_ID = objects[27],
                    hFRDRBLBANK_OBJ = objects[28],
                    hFRDRBLMAINPD_ID = objects[29],
                    hFRDRBLMAINPD_OBJ = objects[30],
                    hFRDRBLOPE_NO = objects[31],
                    hFRDRBLBANK_IN_REQ = objects[32],
                    hFRDRBLPOST_PROCESS_FLAG = objects[33],
                    hFRDRBLDRBLSUBSTATE_ID = objects[34],
                    hFRDRBLDRBLSUBSTATE_OBJ = objects[35],
                    hFRDRBLGRPDRBLGRP_ID = objects[36],
                    hFRDRBLGRPDRBLGRP_OBJ = objects[37],
                    reticleLocation = objects[38];


            if (count >= t_len) {
                t_len = t_len + StandardProperties.OM_RETICLE_LIST_LEN_FOR_RETICLE_LIST_INQ.getIntValue();
            }
            Infos.FoundReticle foundReticle = new Infos.FoundReticle();
            strFoundReticle.add(foundReticle);
//            strFoundReticle[count] = new Infos.FoundReticle();
            ObjectIdentifier _reticleID = new ObjectIdentifier();
            foundReticle.setReticleID(_reticleID);
            _reticleID.setValue((String) hFRDRBLDRBL_ID);
            _reticleID.setReferenceKey((String) hFRDRBLDRBL_OBJ);
            foundReticle.setDescription((String) hFRDRBLDESCRIPTION);
            ObjectIdentifier _reticleGroupID = new ObjectIdentifier();
            foundReticle.setReticleGroupID(_reticleGroupID);
            _reticleGroupID.setValue((String) hFRDRBLGRPDRBLGRP_ID);
            _reticleGroupID.setReferenceKey((String) hFRDRBLGRPDRBLGRP_OBJ);
            foundReticle.setReticleGroupSequenceNumber(BizConstant.SP_RETICLE_DEFAULTGROUPSEQUENCENUMBER);
            foundReticle.setReticlePartNumber((String) hFRDRBLDRBL_PART_NO);
            foundReticle.setReticleLocation((String) reticleLocation);
            foundReticle.setReticleSerialNumber((String) hFRDRBLDRBL_SERIAL_NO);
            Infos.ReticleStatusInfo reticleStatusInfo = new Infos.ReticleStatusInfo();
            foundReticle.setReticleStatusInfo(reticleStatusInfo);
            reticleStatusInfo.setReticleStatus((String) hFRDRBLDRBL_STATE);
            reticleStatusInfo.setTransferStatus((String) hFRDRBLTRANS_STATE);
            ObjectIdentifier _reticlePodID = new ObjectIdentifier();
            reticleStatusInfo.setReticlePodID(_reticlePodID);
            _reticlePodID.setValue((String) hFRDRBLMTRLCONTNR_ID);
            _reticlePodID.setReferenceKey((String) hFRDRBLMTRLCONTNR_OBJ);
            if (CimStringUtils.equals((String) hFRDRBLTRANS_STATE, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                    || CimStringUtils.equals((String) hFRDRBLTRANS_STATE, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
                ObjectIdentifier _equipmentID = new ObjectIdentifier();
                reticleStatusInfo.setEquipmentID(_equipmentID);
                _equipmentID.setValue((String) hFRDRBLEQP_ID);
                _equipmentID.setReferenceKey((String) hFRDRBLEQP_OBJ);
            } else {
                ObjectIdentifier _equipmentID = new ObjectIdentifier();
                reticleStatusInfo.setStockerID(_equipmentID);
                _equipmentID.setValue((String) hFRDRBLEQP_ID);
                _equipmentID.setReferenceKey((String) hFRDRBLEQP_OBJ);
            }
            reticleStatusInfo.setLastClaimedTimeStamp((Timestamp) hFRDRBLCLAIM_TIME);
            reticleStatusInfo.setLastClaimedPerson(ObjectIdentifier.build((String) hFRDRBLCLAIM_USER_ID, (String) hFRDRBLCLAIM_USER_OBJ));
            ObjectIdentifier _durableControlJobID = new ObjectIdentifier();
            reticleStatusInfo.setDurableControlJobID(_durableControlJobID);
            _durableControlJobID.setValue((String) hFRDRBLDCTRLJOB_ID);
            _durableControlJobID.setReferenceKey((String) hFRDRBLDCTRLJOB_OBJ);
            if (!CimObjectUtils.isEmpty(hFRDRBLDRBLPFX_OBJ)) {
                reticleStatusInfo.setDurableSTBFlag(true);
            } else {
                reticleStatusInfo.setDurableSTBFlag(false);
            }
            ObjectIdentifier _durableSubStatus = new ObjectIdentifier();
            reticleStatusInfo.setDurableSubStatus(_durableSubStatus);
            _durableSubStatus.setValue((String) hFRDRBLDRBLSUBSTATE_ID);
            _durableSubStatus.setReferenceKey((String) hFRDRBLDRBLSUBSTATE_OBJ);
            availableForDurableFlag = false;
            if (!CimStringUtils.isEmpty((String) hFRDRBLDRBLSUBSTATE_ID)) {
                Pair<Boolean, Object> pair = find(availableForDurableFlagList, hFRDRBLDRBLSUBSTATE_ID, availableForDurableFlag);
                if (!pair.getFirst()) {
                    CimDurableSubState cimDurableSubState = baseCoreFactory.getBO(CimDurableSubState.class, new ObjectIdentifier((String) hFRDRBLDRBLSUBSTATE_ID));
                    Boolean hFRDRBLSUBSTPROC_AVLBL_FLG = cimDurableSubState.isDurableProcessAvailable();
                    availableForDurableFlag = hFRDRBLSUBSTPROC_AVLBL_FLG;
                    availableForDurableFlagList.add(new Object[]{hFRDRBLDRBLSUBSTATE_ID, availableForDurableFlag});
                }
            }
            reticleStatusInfo.setAvailableForDurableFlag(availableForDurableFlag);
            reticleStatusInfo.setInPostProcessFlagOfReticle(CimBooleanUtils.convert(hFRDRBLPOST_PROCESS_FLAG));
            Infos.HashedInfo[] strDurableStatusList = new Infos.HashedInfo[7];
            durableFlowState = "";
            if (!CimObjectUtils.isEmpty(hFRDRBLMAINPD_ID)) {
                if (CimStringUtils.equals((String) hFRDRBLDRBL_STATE, CIMStateConst.CIM_DURABLE_SCRAPPED)) {
                    durableFlowState = (String) hFRDRBLDRBL_STATE;
                } else if (CimStringUtils.equals((String) hFRDRBLDRBL_HOLD_STATE, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
                    durableFlowState = (String) hFRDRBLDRBL_HOLD_STATE;
                } else if (CimStringUtils.equals((String) hFRDRBLDRBL_FINISHED_STATE, BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED)) {
                    durableFlowState = (String) hFRDRBLDRBL_FINISHED_STATE;
                } else {
                    durableFlowState = (String) hFRDRBLDRBL_PROCESS_STATE;
                }
            }
            strDurableStatusList[0] = new Infos.HashedInfo();
            strDurableStatusList[0].setHashKey("durable Flow State");
            strDurableStatusList[0].setHashData(durableFlowState);
            strDurableStatusList[1] = new Infos.HashedInfo();
            strDurableStatusList[1].setHashKey("durable State");
            durableState = "";
            if (!CimObjectUtils.isEmpty(hFRDRBLMAINPD_ID)) {
                if (CimStringUtils.equals((String) hFRDRBLDRBL_STATE, CIMStateConst.CIM_DURABLE_SCRAPPED)
                        || CimStringUtils.equals((String) hFRDRBLDRBL_FINISHED_STATE, BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED)) {
                    durableState = BizConstant.SP_DURABLE_ONROUTESTATE_FINISHED;
                } else {
                    durableState = BizConstant.SP_DURABLE_ONROUTESTATE_ACTIVE;
                }
            }
            strDurableStatusList[1].setHashData(durableState);
            strDurableStatusList[2] = new Infos.HashedInfo();
            strDurableStatusList[2].setHashKey("durable Production State");
            strDurableStatusList[2].setHashData((String) hFRDRBLDRBL_PRODCTN_STATE);
            strDurableStatusList[3] = new Infos.HashedInfo();
            strDurableStatusList[3].setHashKey("durable Hold State");
            strDurableStatusList[3].setHashData((String) hFRDRBLDRBL_HOLD_STATE);
            strDurableStatusList[4] = new Infos.HashedInfo();
            strDurableStatusList[4].setHashKey("durable Finished State");
            strDurableStatusList[4].setHashData((String) hFRDRBLDRBL_FINISHED_STATE);
            strDurableStatusList[5] = new Infos.HashedInfo();
            strDurableStatusList[5].setHashKey("durable Process State");
            strDurableStatusList[5].setHashData((String) hFRDRBLDRBL_PROCESS_STATE);
            strDurableStatusList[6] = new Infos.HashedInfo();
            strDurableStatusList[6].setHashKey("durable Inventory State");
            strDurableStatusList[6].setHashData((String) hFRDRBLDRBL_INV_STATE);
            reticleStatusInfo.setStrDurableStatusList(CimArrayUtils.generateList(strDurableStatusList));
            ObjectIdentifier _aBank = new ObjectIdentifier();
            _aBank.setValue((String) hFRDRBLBANK_ID);
            _aBank.setReferenceKey((String) hFRDRBLBANK_OBJ);
            foundReticle.setBankID(_aBank);
            foundReticle.setDueTimeStamp(null);
            if (!CimStringUtils.isEmpty((String) hFRDRBLMAINPD_ID)) {
                String hFRPDSTART_BANK_ID,
                        hFRPDSTART_BANK_OBJ,
                        hFRPDPD_LEVEL;
                hFRPDPD_LEVEL = BizConstant.SP_PD_FLOWLEVEL_MAIN;
                String sql = String.format("SELECT START_BANK_ID,\n" +
                        "                                START_BANK_RKEY\n" +
                        "                           FROM OMPRP\n" +
                        "                          WHERE PRP_ID = '%s' AND PRP_LEVEL = '%s'", (String) hFRDRBLMAINPD_ID, hFRPDPD_LEVEL);
                CimProcessDefinitionDO processDefinition = cimJpaRepository.queryOne(sql, CimProcessDefinitionDO.class);
                Validations.check(processDefinition == null, retCodeConfig.getSystemError());
                hFRPDSTART_BANK_ID = processDefinition.getStartBankID();
                hFRPDSTART_BANK_OBJ = processDefinition.getStartBankObj();
                ObjectIdentifier _startBankID = new ObjectIdentifier();
                foundReticle.setStartBankID(_startBankID);
                _startBankID.setValue(hFRPDSTART_BANK_ID);
                _startBankID.setReferenceKey(hFRPDSTART_BANK_OBJ);
            }
            //--- Set objectIdentifier routeID; ----------------------------------//
            ObjectIdentifier _routeID = new ObjectIdentifier();
            _routeID.setValue((String) hFRDRBLMAINPD_ID);
            _routeID.setReferenceKey((String) hFRDRBLMAINPD_OBJ);
            foundReticle.setRouteID(_routeID);
            //--- Set string operationNumber; ------------------------------------//
            foundReticle.setOperationNumber((String) hFRDRBLOPE_NO);
            //--- Set boolean bankInRequiredFlag; --------------------------------//
            foundReticle.setBankInRequiredFlag(CimBooleanUtils.convert(hFRDRBLBANK_IN_REQ));
            /***************************/
            /* Get durable hold record */
            /***************************/
            ObjectIdentifier _durableID = new ObjectIdentifier();
            _durableID.setValue((String) hFRDRBLDRBL_ID);
            _durableID.setReferenceKey((String) hFRDRBLDRBL_ID);
            // step1 - durable_holdRecord_GetDR
            List<Infos.DurableHoldRecord> strDurable_holdRecord_GetDR_out = null;
            try {
                strDurable_holdRecord_GetDR_out = durableMethod.durableHoldRecordGetDR(objCommon, _durableID, BizConstant.SP_DURABLECAT_RETICLE);
            } catch (ServiceException e) {
                break;
            }
            //--- Set objectIdentifier holdReasonCodeID; -------------------------//
            int holdLen = strDurable_holdRecord_GetDR_out.size();
            if (holdLen == 1) {
                ObjectIdentifier _holdReasonCodeID = new ObjectIdentifier();
                _holdReasonCodeID.setValue(strDurable_holdRecord_GetDR_out.get(0).getHoldReasonCodeID().getValue());
                _holdReasonCodeID.setReferenceKey("");
                foundReticle.setHoldReasonCodeID(_holdReasonCodeID);
            } else if (holdLen > 1) {
                String holdReasonCode = strDurable_holdRecord_GetDR_out.get(0).getHoldReasonCodeID().getValue();
                holdReasonCode += BizConstant.SP_DEFAULT_CHAR;
                ObjectIdentifier _holdReasonCodeID = new ObjectIdentifier();
                _holdReasonCodeID.setValue(holdReasonCode);
                _holdReasonCodeID.setReferenceKey("");
                foundReticle.setHoldReasonCodeID(_holdReasonCodeID);
            }
            foundReticle.setWhiteDefFlag(hFRDRBLWHITE_FLAG);
            foundReticle.setFpcCategory(hFRDRBLFPC_CATEGORY);
            foundReticle.setStoreTimeStamp((Timestamp) hFRDRBLSTORE_TIME);
            foundReticle.setLastUsedTimeStamp((Timestamp) hFRDRBLLAST_USED_TIME);
            count++;
            if (count >= fetchLimitCount) {
                break;
            }
        }
        Validations.check(count == 0, new OmCode(retCodeConfig.getNotFoundReticle(), "*****"));
        if (CimArrayUtils.getSize(strFoundReticle) > 0) {
            strReticleListInqResult.setStrFoundReticlelList(new PageImpl<>(strFoundReticle,pageinfo.getPageable(),pageinfo.getTotalElements()));
        }
        return strReticleListInqResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param reticleID
     * @param reticlePodID
     * @param toEquipmentID
     * @return RetCode
     * @author Ho
     * @date 2018/10/31 17:49:10
     */
    @Override
    public List<Infos.ReticleDispatchJob> reticleDispatchJobCheckExistenceDR(Infos.ObjCommon strObjCommonIn, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID, ObjectIdentifier toEquipmentID) {
        StringBuilder HV_BUFFER = new StringBuilder(
                "SELECT\n" +
                "	STOCKER_ID,\n" +
                "	REQ_TIME,\n" +
                "	RTCL_ID,\n" +
                "	RTCLPOD_ID,\n" +
                "	RDJ_ID,\n" +
                "	CUR_EQP_ID,\n" +
                "	CUR_EQP_CATEGORY,\n" +
                "	DEST_EQP_ID,\n" +
                "	DEST_EQP_CATEGORY,\n" +
                "	PRIORITY,\n" +
                "	REQ_USER_ID,\n" +
                "	JOB_STATUS,\n" +
                "	JOB_STATUS_CHG_TIME\n" +
                "FROM\n" +
                "	OSRTCLACTLIST ");

        List<Object> sqlParams = new ArrayList<>();

        boolean firstCondition = true;
        if (ObjectIdentifier.isNotEmptyWithValue(reticleID)) {

            HV_BUFFER.append(" WHERE ");
            firstCondition = false;

//            if (firstCondition) {
//                HV_BUFFER.append(" WHERE ");
//                firstCondition = false;
//            } else {
//                HV_BUFFER.append(" OR ");
//            }

            HV_BUFFER.append("RTCL_ID = ?");
            sqlParams.add(reticleID.getValue());

        }

        if (ObjectIdentifier.isNotEmptyWithValue(reticlePodID)) {
            if (firstCondition) {
                HV_BUFFER.append(" WHERE ");
                firstCondition = false;
            } else {
                HV_BUFFER.append(" OR ");
            }

            HV_BUFFER.append("RTCLPOD_ID = ?");
            sqlParams.add(reticlePodID.getValue());
        }

        if (ObjectIdentifier.isNotEmptyWithValue(toEquipmentID)) {
            if (firstCondition) {
                HV_BUFFER.append(" WHERE ");
                firstCondition = false;
            } else {
                HV_BUFFER.append(" OR ");
            }

            HV_BUFFER.append("DEST_EQP_ID = ?");
            sqlParams.add(toEquipmentID.getValue());
        }
        Validations.check(firstCondition, retCodeConfig.getInvalidParameter());

        List<Object[]> FSRAL2 = cimJpaRepository.query(HV_BUFFER.toString(), sqlParams.toArray());
        List<Infos.ReticleDispatchJob> strReticleDispatchJobList = new ArrayList<>();
        for (Object[] FSRAL : FSRAL2) {
            Object hFSRAL_STATION_ID = FSRAL[0],
                    hFSRAL_REQ_TIME = FSRAL[1],
                    hFSRAL_RTCL_ID = FSRAL[2],
                    hFSRAL_RTCLPOD_ID = FSRAL[3],
                    hFSRAL_RDJ_ID = FSRAL[4],
                    hFSRAL_FROM_EQP_ID = FSRAL[5],
                    hFSRAL_FROM_EQP_CATEGORY = FSRAL[6],
                    hFSRAL_TO_EQP_ID = FSRAL[7],
                    hFSRAL_TO_EQP_CATEGORY = FSRAL[8],
                    hFSRAL_PRIORITY = FSRAL[9],
                    hFSRAL_REQ_USER_ID = FSRAL[10],
                    hFSRAL_JOB_STATUS = FSRAL[11],
                    hFSRAL_JOB_STAT_CHG_TIME = FSRAL[12];
            Infos.ReticleDispatchJob strReticleDispatchJob = new Infos.ReticleDispatchJob();
            strReticleDispatchJobList.add(strReticleDispatchJob);
            strReticleDispatchJob.setDispatchStationID((String) hFSRAL_STATION_ID);
            strReticleDispatchJob.setRequestedTimestamp(CimDateUtils.convertToSpecString((Timestamp) hFSRAL_REQ_TIME));
            strReticleDispatchJob.setReticleID(new ObjectIdentifier((String) hFSRAL_RTCL_ID));
            strReticleDispatchJob.setReticlePodID(new ObjectIdentifier((String) hFSRAL_RTCLPOD_ID));
            strReticleDispatchJob.setReticleDispatchJobID((String) hFSRAL_RDJ_ID);
            strReticleDispatchJob.setFromEquipmentID(new ObjectIdentifier((String) hFSRAL_FROM_EQP_ID));
            strReticleDispatchJob.setFromEquipmentCategory((String) hFSRAL_FROM_EQP_CATEGORY);
            strReticleDispatchJob.setToEquipmentID(new ObjectIdentifier((String) hFSRAL_TO_EQP_ID));
            strReticleDispatchJob.setToEquipmentCategory((String) hFSRAL_TO_EQP_CATEGORY);
            strReticleDispatchJob.setPriority(((BigDecimal) hFSRAL_PRIORITY).longValue());
            strReticleDispatchJob.setRequestUserID(new ObjectIdentifier((String) hFSRAL_REQ_USER_ID));
            strReticleDispatchJob.setJobStatus((String) hFSRAL_JOB_STATUS);
            strReticleDispatchJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString((Timestamp) hFSRAL_JOB_STAT_CHG_TIME));
        }
        if (CimArrayUtils.isNotEmpty(strReticleDispatchJobList)) {
            Validations.check(true,
                    strReticleDispatchJobList,
                    retCodeConfig.getFoundInRdj(),
                    ObjectIdentifier.fetchValue(reticleID),
                    ObjectIdentifier.fetchValue(reticlePodID),
                    ObjectIdentifier.fetchValue(toEquipmentID));
        }
        return strReticleDispatchJobList;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param stockerID
     * @param equipmentID
     * @param strXferReticle
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.ReticleChangeTransportState>
     * @author Ho
     * @date 2018/10/29 14:07:00
     */
    @Override
    public Infos.ReticleChangeTransportState reticleChangeTransportState(Infos.ObjCommon strObjCommonIn, ObjectIdentifier stockerID, ObjectIdentifier equipmentID, List<Infos.XferReticle> strXferReticle) {

        Infos.ReticleChangeTransportState strReticleChangeTransportStateOut = new Infos.ReticleChangeTransportState();
        CimMachine machine = null;
        CimStorageMachine stocker = null;

        strReticleChangeTransportStateOut.setStockerID(stockerID);
        strReticleChangeTransportStateOut.setEquipmentID(equipmentID);
        strReticleChangeTransportStateOut.setStrXferReticle(strXferReticle);

        boolean firstEquipment;
        Boolean firstStocker;
        int len = CimArrayUtils.getSize(strXferReticle);
        firstEquipment = true;
        firstStocker = true;

        Timestamp recordTimeStamp;

        for (int i = 0; i < len; i++) {
            Infos.XferReticle xferReticle = strXferReticle.get(i);

            CimProcessDurable reticle = baseCoreFactory.getBO(CimProcessDurable.class, xferReticle.getReticleID());
            Timestamp xferStatChgTimeStamp = reticle.getTransferStatusChangedTimeStamp();

            if (xferReticle.getTransferStatusChangeTimeStamp() == null) {
                recordTimeStamp = strObjCommonIn.getTimeStamp().getReportTimeStamp();
            } else {
                recordTimeStamp = xferReticle.getTransferStatusChangeTimeStamp();
            }

            if (CimDateUtils.compare(recordTimeStamp, xferStatChgTimeStamp) < 0) {
                continue;
            }

            reticle.setTransferStatusChangedTimeStamp(recordTimeStamp);

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                reticle.makeEquipmentIn();
                this.reticleLocationTrxUpdate(strObjCommonIn,reticle,BizConstant.SP_RETICLE_EQUIPMENT);
                reticle.setReticleLocation(BizConstant.SP_RETICLE_EQUIPMENT);
                CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_EQP);
                reticle.setDurableSubState(aDurableSubState);
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {

                // step1 - durable_durableControlJobID_Get
                ObjectIdentifier strDurableDurableControlJobIDGetOut = durableMethod.durableDurableControlJobIDGet(strObjCommonIn, xferReticle.getReticleID(), BizConstant.SP_DURABLECAT_RETICLE);

                if (!ObjectIdentifier.isEmpty(strDurableDurableControlJobIDGetOut)) {
                    // step2 - durableControlJob_status_Get
                    Infos.DurableControlJobStatusGet strDurableControlJob_status_Get_out = durableMethod.durableControlJobStatusGet(strObjCommonIn, strDurableDurableControlJobIDGetOut);

                    Validations.check(!CimStringUtils.equals(strDurableControlJob_status_Get_out.getDurableControlJobStatus(), CIMStateConst.SP_CONTROL_JOB_STATUS_CREATED) &&
                            !CimStringUtils.equals(strDurableControlJob_status_Get_out.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE), retCodeConfig.getInvalidDcjstatus());
                }
                reticle.makeEquipmentOut();
                this.reticleLocationTrxUpdate(strObjCommonIn,reticle,BizConstant.SP_RETICLE_MASK_ROOM);
                reticle.setReticleLocation(BizConstant.SP_RETICLE_MASK_ROOM);
                CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_IDLE);
                reticle.setDurableSubState(aDurableSubState);
            }
            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_STATIONIN)) {
                String reticleLocation = reticle.getReticleLocation();
                Validations.check(!CimStringUtils.equals(reticleLocation, BizConstant.SP_RETICLE_MASK_ROOM), new OmCode(retCodeConfigEx.getInvalidReticleLocation(), reticle.getIdentifier(), reticleLocation));
                this.reticleLocationTrxUpdate(strObjCommonIn,reticle,BizConstant.SP_RETICLE_STOCKER_ROOM);
                reticle.setReticleLocation(BizConstant.SP_RETICLE_STOCKER_ROOM);
                reticle.makeStationIn();
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_STATIONOUT)) {
                reticle.makeStationOut();
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_BAYIN)) {
                reticle.makeBayIn();
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_BAYOUT)) {
                reticle.makeBayOut();
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_MANUALIN)) {
                reticle.makeManualIn();
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_MANUALOUT)) {
                reticle.makeManualOut();
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_ABNORMALIN)) {
                reticle.makeAbnormalIn();
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_ABNORMALOUT)) {
                reticle.makeAbnormalOut();
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_SHELFIN)) {
                CimDurableSubState durableSubState = reticle.getDurableSubState();
                String identifier = durableSubState.getIdentifier();
                String transportState = reticle.getTransportState();
                Validations.check(CimStringUtils.equals(transportState, BizConstant.SP_TRANSSTATE_SHELFIN),new OmCode(retCodeConfig.getInvalidReticleXferStat(),reticle.getIdentifier(),transportState));
                Validations.check(!CimStringUtils.equals(identifier, BizConstant.SP_DRBL_SUBSTATE_IDLE)&&!CimStringUtils.equals(identifier, BizConstant.SP_DRBL_SUBSTATE_EQP), new OmCode(retCodeConfigEx.getInvalidDurableStat(), reticle.getIdentifier()));
                this.reticleLocationTrxUpdate(strObjCommonIn,reticle,BizConstant.SP_RETICLE_STOCKER_ROOM);
                reticle.setReticleLocation(BizConstant.SP_RETICLE_STOCKER_ROOM);
                CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_IDLE);
                reticle.setDurableSubState(aDurableSubState);
                reticle.makeShelfIn();
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_SHELFOUT)) {
                String transportState = reticle.getTransportState();
                Validations.check(CimStringUtils.equals(transportState, BizConstant.SP_TRANSSTATE_SHELFOUT), new OmCode(retCodeConfig.getInvalidReticleXferStat(),reticle.getIdentifier(),transportState));
                reticle.makeShelfOut();
                this.reticleLocationTrxUpdate(strObjCommonIn,reticle,BizConstant.SP_RETICLE_MASK_ROOM);
                reticle.setReticleLocation(BizConstant.SP_RETICLE_MASK_ROOM);
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_INTERMEDIATEIN)) {
                reticle.makeIntermediateIn();
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT)) {
                reticle.makeIntermediateOut();
            }

            if (CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN) ||
                    CimStringUtils.equals(xferReticle.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
                if (firstEquipment) {
                    machine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
                    firstEquipment = false;
                }
                reticle.assignToMachine(machine);
            } else {
                if (firstStocker) {
                    stocker = baseCoreFactory.getBO(com.fa.cim.newcore.bo.machine.CimStorageMachine.class, stockerID);
                    firstStocker = false;
                }
                reticle.assignToMachine(stocker);
            }
            reticle.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());
            CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, strObjCommonIn.getUser().getUserID());
            reticle.setLastClaimedPerson(aPerson);
        }
        return strReticleChangeTransportStateOut;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param reticleID
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/10/26 16:01:06
     */
    @Override
    public void reticleUsageInfoReset(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {

        CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);

        aReticle.setTimesUsed(0L);

        aReticle.setDurationUsed(0d);

        aReticle.setLastMaintenanceTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());

        aReticle.setLastMaintenancePerson(aPerson);

        aReticle.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

        aReticle.setLastClaimedPerson(aPerson);
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param reticleID
     * @param durableOperationInfoFlag
     * @param durableWipOperationInfoFlag
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.ReticleDetailInfoInqResult>
     * @author Ho
     * @date 2018/10/22 13:23:11
     */
    @Override
    public Results.ReticleDetailInfoInqResult reticleDetailInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Boolean durableOperationInfoFlag, Boolean durableWipOperationInfoFlag) {

        Results.ReticleDetailInfoInqResult strReticleDetailInfoInqResult = new Results.ReticleDetailInfoInqResult();

        strReticleDetailInfoInqResult.setReticleID(reticleID);

        CimDurableDO cimDurableExample = new CimDurableDO();
        cimDurableExample.setDurableId(ObjectIdentifier.fetchValue(reticleID));
        cimDurableExample.setDurableCategory("Reticle");
        CimDurableDO durable = cimJpaRepository.findOne(Example.of(cimDurableExample)).orElse(null);
        Validations.check(CimObjectUtils.isEmpty(durable), retCodeConfig.getNotFoundReticle(), reticleID);
        Infos.ReticleBrInfo reticleBRInfo = new Infos.ReticleBrInfo();
        reticleBRInfo.setDescription(durable.getDescription());
        reticleBRInfo.setReticlePartNumber(durable.getDurablePartNumber());
        reticleBRInfo.setReticleSerialNumber(durable.getDurableSerialNumber());
        reticleBRInfo.setSupplierName(durable.getVendorName());
        reticleBRInfo.setUsageCheckFlag(durable.getUsageCheckRequired());
        strReticleDetailInfoInqResult.setReticleBRInfo(reticleBRInfo);

        Infos.ReticleStatusInfo reticleStatusInfo = new Infos.ReticleStatusInfo();
        reticleStatusInfo.setReticleStatus(durable.getDurableState());
        reticleStatusInfo.setTransferStatus(durable.getTransferState());
        reticleStatusInfo.setReticleLocation(durable.getReticleLocation());
        reticleStatusInfo.setInspectionType(durable.getInspectionType());

        ObjectIdentifier transferReserveUserID = new ObjectIdentifier();
        transferReserveUserID.setValue(durable.getReserveUserID());
        reticleStatusInfo.setTransferReserveUserID(transferReserveUserID);
        strReticleDetailInfoInqResult.setReticleStatusInfo(reticleStatusInfo);

        if (CimStringUtils.equals(durable.getTransferState(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                || CimStringUtils.equals(durable.getTransferState(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
            reticleStatusInfo.setEquipmentID(ObjectIdentifier.build(durable.getEquipmentID(), durable.getEquipmentObj()));
        } else {
            reticleStatusInfo.setStockerID(ObjectIdentifier.build(durable.getEquipmentID(), durable.getEquipmentObj()));
        }

        reticleStatusInfo.setLastClaimedTimeStamp(durable.getClaimTime());
        reticleStatusInfo.setLastClaimedPerson(ObjectIdentifier.build(durable.getClaimUserID(), durable.getClaimUserObj()));

        reticleStatusInfo.setDurableControlJobID(ObjectIdentifier.build(durable.getDurableControlJobID(), durable.getDurableControlJobObj()));

        if (!CimStringUtils.isEmpty(durable.getDurableProcessFlowContextObj())) {
            reticleStatusInfo.setDurableSTBFlag(true);
        } else {
            reticleStatusInfo.setDurableSTBFlag(false);
        }

        List<Infos.HashedInfo> strDurableStatusList = new ArrayList<>(7);

        String durableFlowState = "";
        if (!CimStringUtils.isEmpty(durable.getRouteID())) {
            if (CimStringUtils.equals(durable.getDurableState(), BizConstant.CIMFW_DURABLE_SCRAPPED)) {
                durableFlowState = durable.getDurableState();
            } else if (CimStringUtils.equals(durable.getDurableHoldState(), BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
                durableFlowState = durable.getDurableHoldState();
            } else if (CimStringUtils.equals(durable.getDurableFinishedState(), BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED)) {
                durableFlowState = durable.getDurableFinishedState();
            } else {
                durableFlowState = durable.getDurableProcessState();
            }
        }

        Infos.HashedInfo strDurableStatus = new Infos.HashedInfo();
        strDurableStatus.setHashKey("Durable Flow State");
        strDurableStatus.setHashData(durableFlowState);
        strDurableStatusList.add(strDurableStatus);

        String durableState = "";
        if (!CimStringUtils.isEmpty(durable.getRouteID())) {
            if (CimStringUtils.equals(durable.getDurableState(), CIMStateConst.CIM_DURABLE_SCRAPPED)
                    || CimStringUtils.equals(durable.getDurableFinishedState(), BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED)) {
                durableState = BizConstant.SP_DURABLE_ONROUTESTATE_FINISHED;
            } else {
                durableState = BizConstant.SP_DURABLE_ONROUTESTATE_ACTIVE;
            }
        }

        strDurableStatus = new Infos.HashedInfo();
        strDurableStatus.setHashKey("Durable State");
        strDurableStatus.setHashData(durableState);
        strDurableStatusList.add(strDurableStatus);

        strDurableStatus = new Infos.HashedInfo();
        strDurableStatus.setHashKey("Durable Production State");
        strDurableStatus.setHashData(durable.getDurableProductionState());
        strDurableStatusList.add(strDurableStatus);


        strDurableStatus = new Infos.HashedInfo();
        strDurableStatus.setHashKey("Durable Hold State");
        strDurableStatus.setHashData(durable.getDurableHoldState());
        strDurableStatusList.add(strDurableStatus);

        strDurableStatus = new Infos.HashedInfo();
        strDurableStatus.setHashKey("Durable Finished State");
        strDurableStatus.setHashData(durable.getDurableFinishedState());
        strDurableStatusList.add(strDurableStatus);

        strDurableStatus = new Infos.HashedInfo();
        strDurableStatus.setHashKey("Durable Process State");
        strDurableStatus.setHashData(durable.getDurableProcessState());
        strDurableStatusList.add(strDurableStatus);

        strDurableStatus = new Infos.HashedInfo();
        strDurableStatus.setHashKey("Durable Inventory State");
        strDurableStatus.setHashData(durable.getDurableInventoryState());
        strDurableStatusList.add(strDurableStatus);
        reticleStatusInfo.setStrDurableStatusList(strDurableStatusList);

        reticleStatusInfo.setDurableSubStatus(ObjectIdentifier.build(durable.getDurableSubStateID(), durable.getDurableSubStateObj()));

        if (!CimStringUtils.isEmpty(durable.getDurableSubStateID())) {
            ObjectIdentifier drblSubStateID = new ObjectIdentifier(durable.getDurableSubStateID(), durable.getDurableSubStateObj());
            CimDurableSubStateDO cimDurableSubStateExam = new CimDurableSubStateDO();
            cimDurableSubStateExam.setDurableSubStateID(ObjectIdentifier.fetchValue(drblSubStateID));
            CimDurableSubStateDO durableSubState = cimJpaRepository.findOne(Example.of(cimDurableSubStateExam)).orElse(null);
            reticleStatusInfo.setDurableSubStatusDescription(durableSubState.getDescription());
            reticleStatusInfo.setAvailableForDurableFlag(durableSubState.getConditionAvailableLotFlag());
        }

        if (durable.getProcessLagTime() != null) {
            reticleStatusInfo.setProcessLagTime(durable.getProcessLagTime());
        } else {
            reticleStatusInfo.setProcessLagTime(null);
        }

        Infos.DurableOperationInfo strDurableOperationInfo = new Infos.DurableOperationInfo();
        Infos.DurableWipOperationInfo strDurableWipOperationInfo = new Infos.DurableWipOperationInfo();

        strDurableOperationInfo.setDueTimeStamp(null);
        strDurableOperationInfo.setBankID(ObjectIdentifier.build(durable.getBankID(), durable.getBankObj()));

        String hFRDRBLPOPO_OBJ = null;
        String pdDepartment = null;
        Double pdSTDPROCTIME = null;
        String PFPDListStageID = null;
        String PFPDListStageObj = null;
        if (CimBooleanUtils.isTrue(durableOperationInfoFlag)) {
            ObjectIdentifier activePdID = null;

            CimDurableProcessOperationDO durableProcessOperation = cimJpaRepository.queryOne("SELECT * FROM OMDRBLPROPE WHERE ID = ?1", CimDurableProcessOperationDO.class, durable.getDurableProcessOperation());
            if (null != durableProcessOperation) {
                hFRDRBLPOPO_OBJ = durableProcessOperation.getProcessOperationName();
                activePdID = ObjectIdentifier.build(durableProcessOperation.getProcessDefinitionID(), durableProcessOperation.getProcessDefinitionObj());

                // Get seqno of current module POS in current module PF
                CimPFPosListDO pfPosListDO = cimJpaRepository.queryOne("SELECT * FROM OMPRF_PRSSSEQ WHERE REFKEY = ?1 AND LINK_KEY = ?2", CimPFPosListDO.class, durableProcessOperation.getModuleProcessFlowObj(), durableProcessOperation.getModuleOperationNumber());

                // Check state of current module PF
                CimProcessFlowDO processFlowDO = cimJpaRepository.queryOne("SELECT * FROM OMPRF WHERE ID = ?1", CimProcessFlowDO.class, durableProcessOperation.getModuleProcessFlowObj());

                String hFRPFMAINPD_ID = processFlowDO.getMainProcessDefinitionID();

                String hFRPFPD_LEVEL = BizConstant.SP_PD_FLOWLEVEL_MODULE;

                String sql1 = "SELECT\n" +
                        "	OMPRF.ID,\n" +
                        "	OMPRF_PRSSSEQ.IDX_NO,\n" +
                        "	OMPRF_PRSSSEQ.PRSS_RKEY\n" +
                        "FROM\n" +
                        "	OMPRF,\n" +
                        "	OMPRF_PRSSSEQ\n" +
                        "WHERE\n" +
                        "	OMPRF.PRP_ID = ?1\n" +
                        "AND OMPRF.ACTIVE_FLAG = 1\n" +
                        "AND OMPRF.ID = OMPRF_PRSSSEQ.REFKEY\n" +
                        "AND OMPRF_PRSSSEQ.LINK_KEY =  ?2\n" +
                        "AND OMPRF.PRP_LEVEL = ?3";

                List<Object[]> allObject = cimJpaRepository.query(sql1, hFRPFMAINPD_ID, durableProcessOperation.getModuleOperationNumber(), hFRPFPD_LEVEL);


                CimProcessDefinitionDO processDefinitionDO = cimJpaRepository.queryOne("SELECT * FROM OMPRP WHERE PRP_ID = ?1", CimProcessDefinitionDO.class, activePdID);
                pdDepartment = processDefinitionDO.getDepartment();
                pdSTDPROCTIME = processDefinitionDO.getStandardProcessTIme();
                String sql2 = "SELECT\n" +
                        "	OMPRF.PRP_ID,\n" +
                        "	OMPRF_ROUTESEQ.IDX_NO,\n" +
                        "	OMPRF_ROUTESEQ.STAGE_ID,\n" +
                        "	OMPRF_ROUTESEQ.STAGE_RKEY\n" +
                        "FROM\n" +
                        "	OMPRF,\n" +
                        "	OMPRF_ROUTESEQ\n" +
                        "WHERE\n" +
                        "	OMPRF.ID = ?1\n" +
                        "AND OMPRF_ROUTESEQ.REFKEY = OMPRF.ID\n" +
                        "AND OMPRF_ROUTESEQ.LINK_KEY = ?2";
                List<Object[]> objectsList = cimJpaRepository.query(sql2, durableProcessOperation.getMainProcessFlowObj(), durableProcessOperation.getModuleOperationNumber());
                PFPDListStageID = objectsList.get(0)[2].toString();
                PFPDListStageObj = objectsList.get(0)[3].toString();

            }
            strDurableOperationInfo.setRouteID(ObjectIdentifier.build(durable.getRouteID(), durable.getRouteObj()));


            String hFRPDPDLEVEL = BizConstant.SP_PD_FLOWLEVEL_MAIN;

            CimProcessDefinitionDO processDefinitionDO = cimJpaRepository.queryOne("SELECT * FROM OMPRP WHERE PRP_ID = ?1 AND PRP_LEVEL = ?2", CimProcessDefinitionDO.class, durable.getRouteID(), hFRPDPDLEVEL);
            if (processDefinitionDO != null) {
                strDurableOperationInfo.setStartBankID(ObjectIdentifier.build(processDefinitionDO.getStartBankID(), processDefinitionDO.getStartBankObj()));
            }
            strDurableOperationInfo.setOperationNumber(durable.getOperationNumber());

            if (!CimStringUtils.equals(durable.getDurableInventoryState(), BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR)) {

            } else if (CimStringUtils.equals(durable.getDurableProcessState(), BizConstant.SP_DURABLE_PROCSTATE_PROCESSING)) {
                List<Infos.LotEquipmentList> strEquipmentList = new ArrayList<>(1);
                Infos.LotEquipmentList strEquipment = new Infos.LotEquipmentList();

                strEquipment.setEquipmentID(ObjectIdentifier.build(durableProcessOperation.getAssignEquipmentID(), durableProcessOperation.getAssignEquipmentObj()));
                CimEquipmentDO cimEquipmentDO = cimJpaRepository.queryOne("SELECT * FROM OMEQP WHERE ID = ?1", CimEquipmentDO.class, durableProcessOperation.getAssignEquipmentObj());
                Validations.check(cimEquipmentDO == null, new OmCode(retCodeConfig.getNotFoundEqp(), strEquipment.getEquipmentID().getValue()));
                strEquipment.setEquipmentName(cimEquipmentDO.getDispatchID());
                strEquipmentList.add(strEquipment);
                strDurableOperationInfo.setStrEquipmentList(strEquipmentList);

            } else if (CimStringUtils.equals(durable.getDurableHoldState(), BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
                String sql = "SELECT\n" +
                        "	FRDRBL_HOLDEQP.EQP_ID,\n" +
                        "	FRDRBL_HOLDEQP.EQP_OBJ,\n" +
                        "	OMEQP.DESCRIPTION\n" +
                        "FROM\n" +
                        "	FRDRBL_HOLDEQP,\n" +
                        "	OMEQP\n" +
                        "WHERE\n" +
                        "	FRDRBL_HOLDEQP.REFKEY = ?\n" +
                        "AND OMEQP.EQP_ID = FRDRBL_HOLDEQP.EQP_ID";

                List<Object[]> allObjectList = cimJpaRepository.query(sql, durable.getId());

                List<Infos.LotEquipmentList> strEquipmentList = new ArrayList<>();
                for (Object[] objects : allObjectList) {
                    Object hFRDRBL_HOLDEQPEQP_ID = objects[0],
                            hFRDRBL_HOLDEQPEQP_OBJ = objects[1],
                            hFREQPDESCRIPTION = objects[2];

                    Infos.LotEquipmentList strEquipment = new Infos.LotEquipmentList();
                    strEquipment.setEquipmentID(ObjectIdentifier.build(hFRDRBL_HOLDEQPEQP_ID.toString(), hFRDRBL_HOLDEQPEQP_OBJ.toString()));
                    strEquipment.setEquipmentName(hFREQPDESCRIPTION.toString());
                    strEquipmentList.add(strEquipment);
                }
                strDurableOperationInfo.setStrEquipmentList(strEquipmentList);


                if (CimArrayUtils.isEmpty(strEquipmentList)) {
                    sql = "SELECT\n" +
                            "	OMPDRBL_EQP.EQP_ID,\n" +
                            "	OMPDRBL_EQP.EQP_RKEY,\n" +
                            "	OMEQP.DESCRIPTION\n" +
                            "FROM\n" +
                            "	OMPDRBL_EQP,\n" +
                            "	OMEQP\n" +
                            "WHERE\n" +
                            "	OMPDRBL_EQP.REFKEY = ?\n" +
                            "AND OMEQP.EQP_ID = OMPDRBL_EQP.EQP_ID";

                    allObjectList = cimJpaRepository.query(sql, durable.getId());

                    strEquipmentList = new ArrayList<>();

                    for (Object[] objects : allObjectList) {
                        Object hFRDRBL_HOLDEQPEQP_ID = objects[0],
                                hFRDRBL_HOLDEQPEQP_OBJ = objects[1],
                                hFREQPDESCRIPTION = objects[2];

                        Infos.LotEquipmentList strEquipment = new Infos.LotEquipmentList();
                        strEquipmentList.add(strEquipment);
                        strEquipment.setEquipmentID(ObjectIdentifier.build((String) hFRDRBL_HOLDEQPEQP_ID, (String) hFRDRBL_HOLDEQPEQP_OBJ));
                        strEquipment.setEquipmentName((String) hFREQPDESCRIPTION);
                    }
                    strDurableOperationInfo.setStrEquipmentList(strEquipmentList);
                }
            } else if (CimStringUtils.equals(durable.getDurableProcessState(), BizConstant.SP_DURABLE_PROCSTATE_WAITING)) {

                String sql = "SELECT\n" +
                        "	OMPDRBL_EQP.EQP_ID,\n" +
                        "	OMPDRBL_EQP.EQP_RKEY,\n" +
                        "	OMEQP.DESCRIPTION\n" +
                        "FROM\n" +
                        "	OMPDRBL_EQP,\n" +
                        "	OMEQP\n" +
                        "WHERE\n" +
                        "	OMPDRBL_EQP.REFKEY = ?\n" +
                        "AND OMEQP.EQP_ID = OMPDRBL_EQP.EQP_ID";
                List<Object[]> allObjectList = cimJpaRepository.query(sql, durable.getId());

                List<Infos.LotEquipmentList> strEquipmentList = new ArrayList<>();

                for (Object[] objects : allObjectList) {
                    Object hFRDRBL_HOLDEQPEQP_ID = objects[0],
                            hFRDRBL_HOLDEQPEQP_OBJ = objects[1],
                            hFREQPDESCRIPTION = objects[2];

                    Infos.LotEquipmentList strEquipment = new Infos.LotEquipmentList();
                    strEquipmentList.add(strEquipment);

                    strEquipment.setEquipmentID(ObjectIdentifier.build((String) hFRDRBL_HOLDEQPEQP_ID, (String) hFRDRBL_HOLDEQPEQP_OBJ));
                    strEquipment.setEquipmentName((String) hFREQPDESCRIPTION);
                }
                strDurableOperationInfo.setStrEquipmentList(strEquipmentList);

            }

            if (durable.getQueuedTime() != null) {
                strDurableOperationInfo.setQueuedTimeStamp(durable.getQueuedTime());
            } else {
                strDurableOperationInfo.setQueuedTimeStamp(null);
            }

            if (null != durableProcessOperation) {
                CimPFPosListDO POSLISTPOS_OBJ = cimJpaRepository.queryOne("SELECT * FROM OMPRF_PRSSSEQ WHERE REFKEY = ?1 AND LINK_KEY = ?2", CimPFPosListDO.class, durableProcessOperation.getProcessFlowObj(), durableProcessOperation.getOperationNumber());
                CimProcessOperationSpecificationDO processOperationSpecification = cimJpaRepository.queryOne("SELECT * FROM OMPRSS WHERE ID = ?1", CimProcessOperationSpecificationDO.class, durableProcessOperation.getModuleProcessOperationSpecificationsObj());


                //--- Set objectIdentifier operationID;-----------------------//
                strDurableOperationInfo.setOperationID(activePdID);

                //--- Set string operationName;-------------------------------//
                strDurableOperationInfo.setOperationName(durableProcessOperation.getOperationName());

                //--- Set string department;----------------------------------//
                strDurableOperationInfo.setDepartment(pdDepartment);

                //--- Set string standardProcessTime;-------------------------//
                strDurableOperationInfo.setStandardProcessTime(pdSTDPROCTIME);

                //--- Set boolean mandatoryOperationFlag;---------------------//
                strDurableOperationInfo.setMandatoryOperationFlag(processOperationSpecification.getMandatoryFlag());

                //--- Set objectIdentifier stageID;---------------------------//
                strDurableOperationInfo.setStageID(ObjectIdentifier.build(PFPDListStageID, PFPDListStageObj));

                //--- Set long reworkCount;-----------------------------------//
                strDurableOperationInfo.setReworkCount(0l);

                String key = "";
                String DRBLRWKCNTdkey = "";

                ObjectIdentifier dummyID = new ObjectIdentifier();
                Params.ProcessOperationListForDurableDRParams strProcess_OperationListForDurableDR_in = new Params.ProcessOperationListForDurableDRParams();
                strProcess_OperationListForDurableDR_in.setDurableCategory(BizConstant.SP_DURABLECAT_RETICLE);
                strProcess_OperationListForDurableDR_in.setDurableID(reticleID);
                strProcess_OperationListForDurableDR_in.setSearchDirection(false);
                strProcess_OperationListForDurableDR_in.setPosSearchFlag(true);
                strProcess_OperationListForDurableDR_in.setCurrentFlag(false);
                strProcess_OperationListForDurableDR_in.setSearchCount(1);
                strProcess_OperationListForDurableDR_in.setSearchRouteID(dummyID);
                strProcess_OperationListForDurableDR_in.setSearchOperationNumber("");

                // step1 - process_OperationListForDurableDR
                List<Infos.DurableOperationNameAttributes> strProcess_OperationListForDurableDR_out = processMethod.processOperationListForDurableDR(objCommon, strProcess_OperationListForDurableDR_in);

                List<Infos.DurableOperationNameAttributes> strOperationNameAttributes = strProcess_OperationListForDurableDR_out;

                if (!CimArrayUtils.isEmpty(strOperationNameAttributes)) {

                    Infos.DurableOperationNameAttributes strOperationNameAttribute = strOperationNameAttributes.get(0);
                    boolean firstOperationFlag = false;
                    if (!ObjectIdentifier.equalsWithValue(durable.getRouteID(), strOperationNameAttribute.getRouteID())) {
                        hFRPDPDLEVEL = BizConstant.SP_PD_FLOWLEVEL_MAIN;
                        String sql6 = "SELECT\n" +
                                "	PRF_TYPE\n" +
                                "FROM\n" +
                                "	OMPRP\n" +
                                "WHERE\n" +
                                "	PRP_ID = ?1\n" +
                                "AND PRP_LEVEL = ?2";

                        CimProcessDefinitionDO processDefinitionDO1 = cimJpaRepository.queryOne("SELECT * FROM OMPRP WHERE PRP_ID = ?1 AND PRP_LEVEL = ?2", CimProcessDefinitionDO.class, durable.getRouteID(), hFRPDPDLEVEL);

                        if (processDefinitionDO1 != null) {
                            if (CimStringUtils.equals(processDefinitionDO1.getFlowType(), BizConstant.SP_FLOWTYPE_SUB)) {
                                firstOperationFlag = true;
                            }
                        }
                    }

                    if (!ObjectIdentifier.isEmpty(strOperationNameAttribute.getRouteID())
                            && !CimStringUtils.isEmpty(strOperationNameAttribute.getOperationNumber())
                            && !firstOperationFlag) {
                        key = strOperationNameAttribute.getRouteID().getValue();
                        key += BizConstant.SP_POSEQPMONITOR_SEPARATOR_CHAR;
                        key += strOperationNameAttribute.getOperationNumber();

                        DRBLRWKCNTdkey = key;
                    }
                }

                if (!CimStringUtils.isEmpty(DRBLRWKCNTdkey)) {

                    CimDurableReworkCountDO cimDurableReworkCountExample = new CimDurableReworkCountDO();
                    cimDurableReworkCountExample.setLinkKey(DRBLRWKCNTdkey);
                    cimDurableReworkCountExample.setReferenceKey(durable.getId());
                    CimDurableReworkCountDO durableReworkCountDO = cimJpaRepository.findOne(Example.of(cimDurableReworkCountExample)).orElse(null);

                    if (null != durableReworkCountDO) {
                        strDurableOperationInfo.setReworkCount(durableReworkCountDO.getReworkCount().longValue());
                    }
                }

                //--- Set objectIdentifier logicalRecipeID;-----------------------------------//
                if (!CimStringUtils.isEmpty(durable.getDurableControlJobID())) {
                    strDurableOperationInfo.setLogicalRecipeID(ObjectIdentifier.build(durableProcessOperation.getAssignLogicalRecipeID(), durableProcessOperation.getAssignLogicalRecipeObj()));
                } else {
                    // step2 - process_defaultLogicalRecipe_GetDR
                    ObjectIdentifier strProcessDefaultLogicalRecipeGetDROut = processMethod.processDefaultLogicalRecipeGetDR(objCommon, activePdID);
                    strDurableOperationInfo.setLogicalRecipeID(strProcessDefaultLogicalRecipeGetDROut);
                }
            } else {
                strDurableOperationInfo.setMandatoryOperationFlag(false);
                strDurableOperationInfo.setReworkCount(0l);
            }
        }

        if (CimBooleanUtils.isTrue(durableWipOperationInfoFlag)) {
            if (CimBooleanUtils.isFalse(durable.getRespOperationFlag())) {
                strDurableWipOperationInfo.setResponsibleRouteID(strDurableOperationInfo.getRouteID());
                strDurableWipOperationInfo.setResponsibleOperationID(strDurableOperationInfo.getOperationID());
                strDurableWipOperationInfo.setResponsibleOperationNumber(strDurableOperationInfo.getOperationNumber());
                strDurableWipOperationInfo.setResponsibleOperationName(strDurableOperationInfo.getOperationName());
            } else {
                CimDurablePFXDurablePOListDO durablePFXDurablePOList = cimJpaRepository.queryOne("SELECT * FROM OMDRBLPRFCX_PROPESEQ WHERE PROPE_RKEY = ?1", CimDurablePFXDurablePOListDO.class, hFRDRBLPOPO_OBJ);
                if (durablePFXDurablePOList != null) {
                    Integer sequenceNumber = durablePFXDurablePOList.getSequenceNumber();
                    if (sequenceNumber > 0) {

                        sequenceNumber--;
                        CimDurablePFXDurablePOListDO durablePFXDurablePOList1 = cimJpaRepository.queryOne("SELECT * FROM OMDRBLPRFCX_PROPESEQ WHERE PROPE_RKEY = ?1 AND IDX_NO = ?2", CimDurablePFXDurablePOListDO.class, hFRDRBLPOPO_OBJ);
                        if (durablePFXDurablePOList1 != null) {

                            CimDurableProcessOperationDO drblpolistpo = cimJpaRepository.queryOne("SELECT * FROM OMDRBLPROPE WHERE ID = ?", CimDurableProcessOperationDO.class, durablePFXDurablePOList1.getProcessOperationObj());
                            if (drblpolistpo != null) {
                                strDurableWipOperationInfo.setResponsibleRouteID(ObjectIdentifier.build(drblpolistpo.getMainProcessDefinitionID(), drblpolistpo.getMainProcessDefinitionObj()));
                                strDurableWipOperationInfo.setResponsibleOperationID(ObjectIdentifier.build(drblpolistpo.getProcessDefinitionID(), drblpolistpo.getProcessDefinitionObj()));
                                strDurableWipOperationInfo.setResponsibleOperationNumber(drblpolistpo.getOperationNumber());
                                strDurableWipOperationInfo.setResponsibleOperationName(drblpolistpo.getOperationName());
                            }
                        }
                    }
                }
            }
        }

        strReticleDetailInfoInqResult.setStrDurableOperationInfo(strDurableOperationInfo);
        strReticleDetailInfoInqResult.setStrDurableWipOperationInfo(strDurableWipOperationInfo);

        Timestamp reportTimeStamp = objCommon.getTimeStamp().getReportTimeStamp();
        Timestamp aTmp = durable.getMaintainTime();

        Long aDuration = CimDateUtils.substractTimeStamp(reportTimeStamp.getTime(), aTmp.getTime());

        Long runTime = aDuration / 1000;
        long runTimeStampValue = runTime / 60;

        Infos.ReticlePmInfo reticlePMInfo = new Infos.ReticlePmInfo();
        strReticleDetailInfoInqResult.setReticlePMInfo(reticlePMInfo);
        reticlePMInfo.setRunTimeStamp(runTimeStampValue);


        Long maximumRunTimeStampValue;
        Double DurationLimit;
        if (durable.getDurationLimit() == -1) {
            DurationLimit = 0D;
        } else {
            DurationLimit = durable.getDurationLimit() / 60 / 1000;
        }

        maximumRunTimeStampValue = DurationLimit.longValue();
        reticlePMInfo.setMaximumRunTimeStamp(maximumRunTimeStampValue);

        reticlePMInfo.setOperationStartCount(durable.getTimesUsed().intValue());
        reticlePMInfo.setMaximumOperationStartCount((int) CimNumberUtils.longValue(durable.getTimesUsedLimit()));
        reticlePMInfo.setLastMaintenanceTimeStamp(durable.getMaintainTime());
        reticlePMInfo.setLastMaintenancePerson(ObjectIdentifier.build(durable.getMaintainUserID(), durable.getMaintainUserObj()));

        reticlePMInfo.setIntervalBetweenPM(durable.getIntervalBetweenPM());

        aDuration = CimDateUtils.substractTimeStamp(reportTimeStamp.getTime(), durable.getMaintainTime().getTime());
        Long remainSeconds = aDuration / 1000;
        Long minutes = remainSeconds / 60;

        reticlePMInfo.setPassageTimeFromLastPM(minutes);

        // step3 - reticle_reservationInfo_GetDR
        Infos.ReticleAdditionalAttribute strReticleReservationInfoGetDROut = this.reticleReservationInfoGetDR(objCommon, reticleID);
        strReticleDetailInfoInqResult.setStrReticleAdditionalAttribute(strReticleReservationInfoGetDROut);

        CimReticlePodToReticleDO cimReticlePodToReticleExample = new CimReticlePodToReticleDO();
        cimReticlePodToReticleExample.setReticleID(durable.getDurableId());
        CimReticlePodToReticleDO reticlePodToReticleDO = cimJpaRepository.findOne(Example.of(cimReticlePodToReticleExample)).orElse(null);

        if (reticlePodToReticleDO != null) {
            CimReticlePodDO cimReticlePodExample = new CimReticlePodDO();
            cimReticlePodExample.setId(reticlePodToReticleDO.getReferenceKey());
            CimReticlePodDO reticlePodDO = cimJpaRepository.findOne(Example.of(cimReticlePodExample)).orElse(null);
            if (!CimObjectUtils.isEmpty(reticlePodDO)) {
                reticleStatusInfo.setReticlePodID(ObjectIdentifier.build(reticlePodDO.getReticlePodID(), reticlePodDO.getId()));
                reticleStatusInfo.setShelfPosition(new Infos.ShelfPosition(reticlePodDO.getShelfPositionX(), reticlePodDO.getShelfPositionY(), reticlePodDO.getShelfPositionZ()));
            }
        }
        CimDurableDurableGroupDO cimDurableDurableGroupExample = new CimDurableDurableGroupDO();
        cimDurableDurableGroupExample.setReferenceKey(durable.getId());
        CimDurableDurableGroupDO drblgrp = cimJpaRepository.findOne(Example.of(cimDurableDurableGroupExample)).orElse(null);

        if (drblgrp != null) {
            reticleBRInfo.setReticleGroupID(ObjectIdentifier.build(drblgrp.getDurableGroupId(), drblgrp.getDurableGroupObj()));
            CimDurableGroupDO cimDurableGroupExample = new CimDurableGroupDO();
            cimDurableGroupExample.setDurableGroupId(drblgrp.getDurableGroupId());
            CimDurableGroupDO durableGroupDO = cimJpaRepository.findOne(Example.of(cimDurableGroupExample)).orElse(null);
            if (durableGroupDO != null) {
                reticleBRInfo.setReticleGroupDescription(durableGroupDO.getDescription());
            }
        }


        Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
        List<Infos.EntityIdentifier> entities = new ArrayList<>(2);
        Infos.EntityIdentifier entity = new Infos.EntityIdentifier();
        entity.setClassName(BizConstant.SP_INHIBITCLASSID_RETICLE);
        if (ObjectIdentifier.isEmpty(reticleID)) {
            Validations.check(retCodeConfig.getNotFoundReticle());
        }
        entity.setObjectID(reticleID);
        entity.setAttribution("");
        entities.add(entity);

        entity = new Infos.EntityIdentifier();
        entity.setClassName(BizConstant.SP_INHIBITCLASSID_RETICLEGROUP);
        entity.setObjectID(reticleBRInfo.getReticleGroupID());
        entity.setAttribution("");
        entities.add(entity);
        entityInhibitAttributes.setEntities(entities);


        // step4 - entityInhibit_CheckForEntities
        Infos.EntityInhibitCheckForEntitiesOut strEntityInhibit_CheckForEntities_out = constraintMethod.constraintCheckForEntities(objCommon, entityInhibitAttributes);

        List<Infos.EntityInhibitInfo> entityInhibitInfo = strEntityInhibit_CheckForEntities_out.getEntityInhibitInfo();
        int n = entityInhibitInfo.size();
        List<Infos.EntityInhibitAttributes> entityInhibitions = new ArrayList<>();
        strReticleDetailInfoInqResult.setEntityInhibitions(entityInhibitions);
        for (int i = 0; i < n; i++) {
            entityInhibitions.add(entityInhibitInfo.get(i).getEntityInhibitAttributes());
        }

        return strReticleDetailInfoInqResult;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param reticleID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.ReticleAdditionalAttribute>
     * @author Ho
     * @date 2018/10/25 11:20:43
     */
    @Override
    public Infos.ReticleAdditionalAttribute reticleReservationInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        String reticleId = ObjectIdentifier.fetchValue(reticleID);
        CimDurableDO cimDurableExmaple = new CimDurableDO();
        cimDurableExmaple.setDurableId(reticleId);
        CimDurableDO durableDO = cimJpaRepository.findOne(Example.of(cimDurableExmaple)).orElse(null);
        Validations.check(durableDO == null, new OmCode(retCodeConfig.getNotFoundReticle(), reticleId));

        Infos.ReticleAdditionalAttribute strReticleAdditionalAttribute = new Infos.ReticleAdditionalAttribute();

        strReticleAdditionalAttribute.setLastUsedTime(durableDO.getLastUsedTime());
        strReticleAdditionalAttribute.setTransferDestinationEqpID(ObjectIdentifier.build(durableDO.getTransferDestinationEquipmentId(), durableDO.getTransferDestinationEquipmentObj()));
        strReticleAdditionalAttribute.setTransferDestinationStockerID(ObjectIdentifier.build(durableDO.getTransferDestinationStockerId(), durableDO.getTransferDestinationStockerObj()));
        strReticleAdditionalAttribute.setReserveUserID(ObjectIdentifier.build(durableDO.getReserveUserID(), durableDO.getReserveUserObj()));
        strReticleAdditionalAttribute.setTransferReserveUserID(ObjectIdentifier.build(durableDO.getTransferReservedUserId(), durableDO.getTransferReservedUserObj()));
        strReticleAdditionalAttribute.setTransferReserveTimestamp(durableDO.getTransferReservedTime());
        strReticleAdditionalAttribute.setTransferReserveReticlePodID(ObjectIdentifier.build(durableDO.getTransferReservedReticlePodId(), durableDO.getTransferReservedReticlePodObj()));

        CimDurableReservedControlJobDO cimDurableReservedControlJobExample = new CimDurableReservedControlJobDO();
        cimDurableReservedControlJobExample.setReferenceKey(durableDO.getId());
        List<CimDurableReservedControlJobDO> durableReservedControlJobDOS = cimJpaRepository.findAll(Example.of(cimDurableReservedControlJobExample));

        strReticleAdditionalAttribute.setReservedControlJobs(durableReservedControlJobDOS.stream()
                .map(data -> ObjectIdentifier.build(data.getControlJobID(), data.getControlJobObj()))
                .collect(Collectors.toList()));

        return strReticleAdditionalAttribute;
    }


    @Override
    public void reticleStateCheck170(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartReticleInfo> startReticleList, ObjectIdentifier lotID) {
        /*----------------------------------*/
        /*   Get and Check Reticle Status   */
        /*----------------------------------*/
        int srLength = CimArrayUtils.getSize(startReticleList);
        Validations.check(0 == srLength, retCodeConfig.getNotAvailableReticle());

        List<String> subLotTypeList = new ArrayList<>();
        if (!ObjectIdentifier.isEmpty(lotID)) {
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
            log.info(" Get lot's SubLotType : {}", aLot.getSubLotType());
            subLotTypeList.add(aLot.getSubLotType());
        }
        for (int i = 0; i < srLength; i++) {
            Infos.StartReticleInfo startReticleInfo = startReticleList.get(i);
            /*------------------------*/
            /*   Get Reticle Object   */
            /*------------------------*/
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, startReticleInfo.getReticleID());
            /*---------------------------------*/
            /*   Check Reticle State           */
            /*---------------------------------*/
            Boolean availableFlag = aReticle.isLotProcessAvailable(subLotTypeList);
            Validations.check(!availableFlag, new OmCode(retCodeConfig.getDurableNotAvailableStateForLotProcess(), ObjectIdentifier.fetchValue(startReticleInfo.getReticleID())));

            /*--------------------------------------*/
            /*   Get and Check Reticle Xfer State   */
            /*--------------------------------------*/
            Machine aMachine = aReticle.currentAssignedMachine();
            Validations.check(null == aMachine, retCodeConfig.getNotFoundMachine());

            boolean tmpB = ObjectIdentifier.equalsWithValue(equipmentID, aMachine.getIdentifier())
                    && CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, aReticle.getTransportState());
            Validations.check(!tmpB, new OmCode(retCodeConfig.getInvalidReticleStat()
                    , ObjectIdentifier.fetchValue(startReticleInfo.getReticleID()), aReticle.getTransportState()));
        }
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param reticleID
     * @param reticleStatus
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/10/22 16:38:20
     */
    @Override
    public void reticleStateChange(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String reticleStatus) {

        boolean Flag = false;
        CimProcessDurable Reticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        if (CimStringUtils.equals(reticleStatus, CIMStateConst.CIM_DURABLE_AVAILABLE)) {
            Flag = Reticle.isAvailable();
            if (Flag == TRUE) {
                String msgTxt = String.format(retCodeConfig.getSameReticleStat().getMessage(), Reticle.getIdentifier(), Reticle.getDurableState());
                OmCode cimCode = new OmCode(retCodeConfig.getSameReticleStat().getCode(), msgTxt);
                Validations.check(true, cimCode);
            } else {
                Reticle.makeAvailable();
            }
        } else if (CimStringUtils.equals(reticleStatus, CIMStateConst.CIM_DURABLE_INUSE)) {
            Flag = Reticle.isInUse();
            if (Flag) {
                String msgTxt = String.format(retCodeConfig.getSameReticleStat().getMessage(), Reticle.getIdentifier(), Reticle.getDurableState());
                OmCode cimCode = new OmCode(retCodeConfig.getSameReticleStat().getCode(), msgTxt);
                Validations.check(true, cimCode);
            } else {
                Reticle.makeInUse();
            }
        } else if (CimStringUtils.equals(reticleStatus, CIMStateConst.CIM_DURABLE_NOTAVAILABLE)) {
            Flag = Reticle.isNotAvailable();
            if (Flag) {
                String msgTxt = String.format(retCodeConfig.getSameReticleStat().getMessage(), Reticle.getIdentifier(), Reticle.getDurableState());
                OmCode cimCode = new OmCode(retCodeConfig.getSameReticleStat().getCode(), msgTxt);
                Validations.check(true, cimCode);
            } else {
                Reticle.makeNotAvailable();
            }
        } else if (CimStringUtils.equals(reticleStatus, CIMStateConst.CIM_DURABLE_SCRAPPED)) {
            Flag = Reticle.isScrapped();
            if (Flag) {
                String msgTxt = String.format(retCodeConfig.getSameReticleStat().getMessage(), Reticle.getIdentifier(), Reticle.getDurableState());
                OmCode cimCode = new OmCode(retCodeConfig.getSameReticleStat().getCode(), msgTxt);
                Validations.check(true, cimCode);
            } else {
                Reticle.makeScrapped();
            }
        } else {
            Validations.check(true, retCodeConfig.getInvalidReticleStat());
        }
        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Reticle.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        Reticle.setLastClaimedPerson(aPerson);
        Reticle.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        Reticle.setStateChangedPerson(aPerson);

    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param reticleID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.ReticleStatusChangeRptResult>
     * @author Ho
     * @date 2018/10/22 16:43:13
     */
    @Override
    public Results.ReticleStatusChangeRptResult reticleFillInTxPDR005(Infos.ObjCommon strObjCommonIn, ObjectIdentifier reticleID) {

        CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);

        Results.ReticleStatusChangeRptResult strReticleStatusChangeRptResult = new Results.ReticleStatusChangeRptResult();
        strReticleStatusChangeRptResult.setReticleID(ObjectIdentifier.build(reticleID.getValue(), aReticle.getIdentifier()));


        Infos.ReticleBrInfo reticleBRInfo = new Infos.ReticleBrInfo();
        strReticleStatusChangeRptResult.setReticleBRInfo(reticleBRInfo);
        reticleBRInfo.setDescription(aReticle.getProcessDurableDescription());

        reticleBRInfo.setReticlePartNumber(aReticle.getPartNumber());

        reticleBRInfo.setReticleSerialNumber(aReticle.getSerialNumber());

        reticleBRInfo.setSupplierName(aReticle.getVendorName());

        reticleBRInfo.setUsageCheckFlag(aReticle.isUsageCheckRequired());

        List<CimProcessDurableCapability> seq = aReticle.allAssignedProcessDurableCapabilities();

        if (!CimArrayUtils.isEmpty(seq)) {
            reticleBRInfo.setReticleGroupID(ObjectIdentifier.build(seq.get(0).getIdentifier(), seq.get(0).getPrimaryKey()));

            reticleBRInfo.setReticleGroupDescription(seq.get(0).getProcessDurableCapabilityDescription());
        }

        Infos.ReticleStatusInfo reticleStatusInfo = new Infos.ReticleStatusInfo();
        strReticleStatusChangeRptResult.setReticleStatusInfo(reticleStatusInfo);

        reticleStatusInfo.setReticleStatus(aReticle.getDurableState());

        reticleStatusInfo.setTransferStatus(aReticle.getTransportState());

        Machine aMachine = aReticle.currentAssignedMachine();


        if (aMachine != null) {
            if (CimStringUtils.equals(reticleStatusInfo.getTransferStatus(), CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN) ||
                    CimStringUtils.equals(reticleStatusInfo.getTransferStatus(), CIMStateConst.SP_TRANS_STATE_EQUIPMENT_OUT)) {
                reticleStatusInfo.setEquipmentID(ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
            } else {
                reticleStatusInfo.setStockerID(ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
            }
        }

        reticleStatusInfo.setLastClaimedTimeStamp(aReticle.getLastClaimedTimeStamp());
        // aReticle.getClaimUserObj() 添加
        reticleStatusInfo.setLastClaimedPerson(new ObjectIdentifier(aReticle.getLastClaimedPersonID()));

        Double duration = aReticle.durationUsed();

        Double durationLimit = aReticle.getDurationLimit();

        Double min = CimNumberUtils.longValue(duration) / (60.0 * 1000);

        Infos.ReticlePmInfo reticlePMInfo = new Infos.ReticlePmInfo();

        strReticleStatusChangeRptResult.setReticlePMInfo(reticlePMInfo);

        reticlePMInfo.setRunTimeStamp(min.longValue());

        min = CimNumberUtils.longValue(durationLimit) / (60.0 * 1000);

        reticlePMInfo.setMaximumRunTimeStamp(min.longValue());

        reticlePMInfo.setOperationStartCount(aReticle.timesUsed().intValue());

        reticlePMInfo.setMaximumOperationStartCount(aReticle.getTimesUsedLimit().intValue());
        // aReticle.getMaintenanceUserObj() 添加
        reticlePMInfo.setLastMaintenancePerson(ObjectIdentifier.buildWithValue(aReticle.getLastMaintenancePersonID()));

        reticlePMInfo.setLastMaintenanceTimeStamp(aReticle.getLastMaintenanceTimeStamp());

        return strReticleStatusChangeRptResult;

    }

    @Override
    public void reticlePodTimeStampSet(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {
        CimReticlePod cimReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);
        Validations.check(cimReticlePod == null, retCodeConfig.getNotFoundReticlePod());
        com.fa.cim.newcore.bo.person.CimPerson cimPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(cimPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));

        cimReticlePod.setLastMaintenanceTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        cimReticlePod.setLastMaintenancePerson(cimPerson);
    }

    @Override
    public Infos.DurableAttribute reticlePodBaseInfoGetDr(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {
        Infos.DurableAttribute durableAttribute = new Infos.DurableAttribute();
        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);
        durableAttribute.setDurableID(reticlePodID);
        durableAttribute.setDescription(aReticlePod.getDescription());
        durableAttribute.setCategory(aReticlePod.getCategory().getIdentifier());
        durableAttribute.setUsageCheckFlag(false);
        durableAttribute.setMaximumRunTime("0.0");
        durableAttribute.setMaximumOperationStartCount(0.0);
        durableAttribute.setIntervalBetweenPM(aReticlePod.getIntervalBetweenPM().intValue());
        durableAttribute.setCapacity(aReticlePod.getCapacity());
        durableAttribute.setNominalSize(0);
        durableAttribute.setContents(BizConstant.EMPTY);
        durableAttribute.setInstanceName(aReticlePod.getInstanceName());

        return durableAttribute;

    }

    @Override
    public List<Infos.UserData> reticlePodUserDataInfoGetDr(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {
        List<Infos.UserData> userDataList = new ArrayList<>();

        String sql = "SELECT B.NAME, B.DATA_TYPE, B.VALUE, B.SOURCE " +
                "FROM OMRTCLPOD A, OMRTCLPOD_CDA B " +
                "WHERE A.RTCLPOD_ID = ?1 " +
                "AND A.ID = B.REFKEY " +
                "ORDER BY B.IDX_NO";
        List<Object[]> objects = cimJpaRepository.query(sql, reticlePodID.getValue());
        if (CimArrayUtils.isEmpty(objects)) {
            return userDataList;
        }
        for (Object[] reticlePod : objects) {
            String name = reticlePod[0] == null ? null : (String) reticlePod[0];
            String type = reticlePod[1] == null ? null : (String) reticlePod[1];
            String value = reticlePod[2] == null ? null : (String) reticlePod[2];
            String originator = reticlePod[3] == null ? null : (String) reticlePod[3];
            Infos.UserData userData = new Infos.UserData();
            userData.setName(name);
            userData.setType(type);
            userData.setValue(value);
            userData.setOriginator(originator);
            userDataList.add(userData);
        }
        return userDataList;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/6                          Wind
     *
     * @param objCommon
     * @param params
     * @return RetCode<List < Infos.ReticlePodListInfo>>
     * @author Wind
     * @date 2018/11/6 13:37
     */
    @Override
    public List<Infos.ReticlePodListInfo> reticlePodFillInTxPDQ012DR(Infos.ObjCommon objCommon, Params.ReticlePodListInqParams params) {
        List<Infos.ReticlePodListInfo> reticlePodListInfoList = new ArrayList<>();

        Map<String, Integer> availableForDurableFlagList = new HashMap<>();

        Integer availableForDurableFlag;
        String durableFlowState;
        String durableState;
        long fetchLimitCount = 0L;
        if (params.getMaxRetrieveCount() <= 0 || params.getMaxRetrieveCount() > Long.parseLong(BizConstant.SEARCH_COUNT_MAX)) {
            fetchLimitCount = Long.parseLong(BizConstant.SEARCH_COUNT_MAX);
        } else {
            fetchLimitCount = params.getMaxRetrieveCount();
        }
        StringBuffer hvBUFFER = new StringBuffer();
        String hvTMPBUFFER = new String();
        hvBUFFER.append(
                "SELECT " +
                "           RTCLPOD_ID," +
                "           ID," +
                "           DESCRIPTION, " +
                "           RTCLPOD_TYPE_ID, " +
                "           RTCLPOD_STATE," +
                "           XFER_STATE," +
                "           EQP_ID," +
                "           EQP_RKEY," +
                "           STK_ID," +
                "           STK_RKEY, " +
                "           RTCLPOD_USED_CAP," +
                "           LAST_TRX_TIME, " +
                "           LAST_TRX_USER_ID, " +
                "           LAST_TRX_USER_RKEY, " +
                "           PM_INTERVAL_TIME, " +
                "           MAINT_TIME, " +
                "           MAINT_USER_ID," +
                "           MAINT_USER_RKEY, " +
                "           DCJ_ID, " +
                "           DCJ_RKEY, " +
                "           DRBL_PRFCX_RKEY, " +
                "           DRBL_PRODUCTION_STATE, " +
                "           DRBL_HOLD_STATE, " +
                "           DRBL_FINISHED_STATE, " +
                "           DRBL_PROCESS_STATE, " +
                "           DRBL_INV_STATE, " +
                "           BANK_ID, " +
                "           BANK_RKEY, " +
                "           MAIN_PROCESS_ID, " +
                "           MAIN_PROCESS_RKEY, " +
                "           OPE_NO, " +
                "           BANK_IN_REQD, " +
                "           PP_FLAG, " +
                "           RTCLPOD_SUB_STATE_ID, " +
                "           RTCLPOD_SUB_STATE_RKEY, " +
                "           RTCLPOD_CAP, " +
                "           FAB_INSTANCE, " +
                "           CUR_SITE_FLAG, " +
                "           BCKUP_STATE " +
                "FROM       OMRTCLPOD");

        boolean bFirstCondition = true;
        /*-------------------------*/
        /*   Set Where Condition   */
        /*-------------------------*/
        if (CimStringUtils.isNotEmpty(params.getReticlePodID().getValue())) {
            /*----------------------------*/
            /*   reticlePodID Condition   */
            /*----------------------------*/
            bFirstCondition = false;
            hvBUFFER.append(" WHERE");

            //step1 - reticlePod_whereCondition_Make
            String whereConditionMake = this.reticlePodWhereConditionMake(objCommon, params.getReticlePodID().getValue());

            hvBUFFER.append(String.format(" RTCLPOD_ID %s ", whereConditionMake));
        }
        if (CimStringUtils.isNotEmpty(params.getReticlePodCategory())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }

            //step2 - reticlePod_whereCondition_Make
            String whereConditionMake = this.reticlePodWhereConditionMake(objCommon, params.getReticlePodCategory());

            hvBUFFER.append(String.format(" RTCLPOD_TYPE_ID %s ", whereConditionMake));
        }
        if (CimStringUtils.isNotEmpty(params.getReticlePodStatus())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }

            //step3 - reticlePod_whereCondition_Make
            String whereConditionMake = this.reticlePodWhereConditionMake(objCommon, params.getReticlePodStatus());

            hvBUFFER.append(String.format(" RTCLPOD_STATE %s ", whereConditionMake));
        }
        if (CimStringUtils.isNotEmpty(params.getEquipmentID().getValue())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }
            hvBUFFER.append(String.format(" EQP_ID = '%s' ", params.getEquipmentID().getValue()));
        }
        if (CimStringUtils.isNotEmpty(params.getStockerID().getValue())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }
            hvBUFFER.append(String.format(" EQP_ID = '%s' ", params.getStockerID().getValue()));
        }
        if (params.isEmptyFlag()) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }
            hvBUFFER.append(" RTCLPOD_USED_CAP = 0 ");
        }
        if (CimStringUtils.isNotEmpty(ObjectIdentifier.isEmpty(params.getBankID()) ? null : params.getBankID().getValue())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }
            hvBUFFER.append(String.format(" BANK_ID = '%s' ", params.getBankID().getValue()));
        }
        if (CimStringUtils.isNotEmpty(params.getDurableSubStatus().getValue())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }
            hvBUFFER.append(String.format(" RTCLPOD_SUB_STATE_ID = '%s' ", params.getDurableSubStatus().getValue()));
        }

        String flowStatus = params.getFlowStatus();
        if (CimStringUtils.isNotEmpty(flowStatus)) {
            boolean durableStatusFlag = true;
            if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_DURABLE_SCRAPPED)) {
                hvTMPBUFFER = String.format(" MAIN_PROCESS_ID <> '' AND DRBL_STATE = '%s' ", flowStatus);
            } else if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                hvTMPBUFFER = String.format(" MAIN_PROCESS_ID <> '' AND DRBL_HOLD_STATE = '%s' ", flowStatus);
            } else if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED)) {
                hvTMPBUFFER = String.format(" MAIN_PROCESS_ID <> '' AND DRBL_FINISHED_STATE = '%s' ", flowStatus);
            } else if (CimStringUtils.equals(flowStatus, CIMStateConst.SP_DURABLE_PROCESS_STATE_WAITING) ||
                    CimStringUtils.equals(flowStatus, CIMStateConst.CIM_PRODUCT_ACTIVITY_STATE_PROCESSING)) {
                hvTMPBUFFER = String.format(" MAIN_PROCESS_ID <> '' AND DRBL_PROCESS_STATE = '%s' ", flowStatus);
            } else {
                durableStatusFlag = false;
            }
            if (durableStatusFlag) {
                if (bFirstCondition) {
                    bFirstCondition = false;
                    hvBUFFER.append(" WHERE");
                } else {
                    hvBUFFER.append(" AND");
                }
                hvBUFFER.append(hvTMPBUFFER);
                if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                    hvBUFFER.append(String.format(" AND DRBL_STATE <> '%s' ", CIMStateConst.CIM_DURABLE_SCRAPPED));
                } else if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED)) {
                    hvBUFFER.append(String.format(" AND DRBL_STATE <> '%s' ", CIMStateConst.CIM_DURABLE_SCRAPPED));
                    hvBUFFER.append(String.format(" AND DRBL_HOLD_STATE <> '%s' ", CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD));
                } else if (CimStringUtils.equals(flowStatus, CIMStateConst.SP_DURABLE_PROCESS_STATE_WAITING) ||
                        CimStringUtils.equals(flowStatus, CIMStateConst.CIM_PRODUCT_ACTIVITY_STATE_PROCESSING)) {
                    hvBUFFER.append(String.format(" AND DRBL_STATE <> '%s' ", CIMStateConst.CIM_DURABLE_SCRAPPED));
                    hvBUFFER.append(String.format(" AND DRBL_HOLD_STATE <> '%s' ", CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD));
                    hvBUFFER.append(String.format(" AND DRBL_FINISHED_STATE <> '%s' ", CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED));
                }
            }
        }
        hvBUFFER.append(" ORDER BY RTCLPOD_ID");
        //-------------------------------------------
        // Judge and Convert SQL with Escape Sequence
        //-------------------------------------------
        String originalSQL = hvBUFFER.toString();
        /*-----------------*/
        /*   SQL PREPARE   */
        /*-----------------*/
        List<Object[]> reticlePodList = cimJpaRepository.query(originalSQL);
        int count = 0;
        if (CimArrayUtils.isNotEmpty(reticlePodList)) {
            for (Object[] objects : reticlePodList) {
                Infos.ReticlePodListInfo reticlePodListInfo = new Infos.ReticlePodListInfo();

                ObjectIdentifier aReticlePodID = new ObjectIdentifier();
                Infos.ReticlePodBrInfo reticlePodBRInfo = new Infos.ReticlePodBrInfo();
                Infos.ReticlePodStatusInfo reticlePodStatusInfo = new Infos.ReticlePodStatusInfo();

                //赋值
                reticlePodListInfo.setReticlePodID(aReticlePodID);
                reticlePodListInfo.setReticlePodBRInfo(reticlePodBRInfo);
                reticlePodListInfo.setReticlePodStatusInfo(reticlePodStatusInfo);


                aReticlePodID.setValue(CimObjectUtils.toString(objects[0]));
                aReticlePodID.setReferenceKey(CimObjectUtils.toString(objects[1]));

                reticlePodBRInfo.setDescription(CimObjectUtils.toString(objects[2]));
                reticlePodBRInfo.setReticlePodCategory(CimObjectUtils.toString(objects[3]));
                reticlePodBRInfo.setCapacity(CimNumberUtils.longValue(CimObjectUtils.toString(objects[35])));

                reticlePodStatusInfo.setReticlePodStatus(CimObjectUtils.toString(objects[4]));
                reticlePodStatusInfo.setTransferStatus(CimObjectUtils.toString(objects[5]));

                // can't use STK_ID, STK_OBJ
                if (!CimObjectUtils.isEmpty(objects[6])) {
                    if (CimStringUtils.equals(CimObjectUtils.toString(objects[5]), CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN) ||
                            CimStringUtils.equals(CimObjectUtils.toString(objects[5]), CIMStateConst.SP_TRANS_STATE_EQUIPMENT_OUT)) {
                        reticlePodStatusInfo.setEquipmentID(ObjectIdentifier.build(CimObjectUtils.toString(objects[6]), CimObjectUtils.toString(objects[7])));
                    } else {
                        reticlePodStatusInfo.setStockerID(ObjectIdentifier.build(CimObjectUtils.toString(objects[6]), CimObjectUtils.toString(objects[7])));
                    }
                }
                if (Integer.valueOf(String.valueOf(objects[10])) > 0) {
                    reticlePodStatusInfo.setEmptyFlag(false);
                } else {
                    reticlePodStatusInfo.setEmptyFlag(true);
                }
                reticlePodStatusInfo.setLastClaimedTimeStamp(CimObjectUtils.toString(objects[11]));
                reticlePodStatusInfo.setLastClaimedPerson(ObjectIdentifier.build(CimObjectUtils.toString(objects[12]), CimObjectUtils.toString(objects[13])));

                reticlePodStatusInfo.setDurableControlJobID(ObjectIdentifier.build(CimObjectUtils.toString(objects[18]), CimObjectUtils.toString(objects[19])));
                if (!CimObjectUtils.isEmpty(objects[20])) {
                    reticlePodStatusInfo.setDurableSTBFlag(true);
                } else {
                    reticlePodStatusInfo.setDurableSTBFlag(false);
                }
                reticlePodStatusInfo.setDurableSubStatus(ObjectIdentifier.build(CimObjectUtils.toString(objects[33]), CimObjectUtils.toString(objects[34])));
                //--- Set availableForDurableFlag; ------------------------------------//
                availableForDurableFlag = 0;
                if (!CimObjectUtils.isEmpty(objects[33])) {
                    if (!availableForDurableFlagList.containsKey(CimObjectUtils.toString(objects[33]))) {
                        String sql1 = "SELECT PROC_AVAIL_FLAG\n" +
                                "FROM OMDRBLST\n" +
                                "WHERE DRBL_SUBSTATE_ID = ?1 ";
                        Object distinct = cimJpaRepository.queryOneColumnAndUnique(sql1, CimObjectUtils.toString(objects[33]));
                        availableForDurableFlag = CimBooleanUtils.isTrue(!CimObjectUtils.isEmpty(distinct) && (CimObjectUtils.equals(distinct.toString(), 1))) ? 1 : 0;
                        availableForDurableFlagList.put(CimObjectUtils.toString(objects[33]), availableForDurableFlag);
                    }
                }
                reticlePodStatusInfo.setAvailableForDurableFlag(availableForDurableFlag == 1);
                reticlePodStatusInfo.setInPostProcessFlagOfReticlePod(Boolean.parseBoolean(CimObjectUtils.toString(objects[32])));
                //--- Set pptHashedInfoSequence strDurableStatusList; ----------------------//
                List<Infos.HashedInfo> strDurableStatusList = new ArrayList<>();
                durableFlowState = "";
                if (!CimObjectUtils.isEmpty(objects[28])) {
                    if (CimStringUtils.equals(CimObjectUtils.toString(objects[4]), CIMStateConst.CIM_DURABLE_SCRAPPED)) {
                        durableFlowState = CimObjectUtils.toString(objects[4]);
                    } else if (CimStringUtils.equals(CimObjectUtils.toString(objects[22]), CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                        durableFlowState = CimObjectUtils.toString(objects[22]);
                    } else if (CimStringUtils.equals(CimObjectUtils.toString(objects[23]), CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED)) {
                        durableFlowState = CimObjectUtils.toString(objects[23]);
                    } else {
                        durableFlowState = CimObjectUtils.toString(objects[24]);
                    }
                }
                Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData(durableFlowState);
                hashedInfo.setHashKey("durable Flow State");
                strDurableStatusList.add(hashedInfo);

                durableState = "";
                if (!CimObjectUtils.isEmpty(objects[28])) {
                    if (CimStringUtils.equals(CimObjectUtils.toString(objects[4]), CIMStateConst.CIM_DURABLE_SCRAPPED) ||
                            CimStringUtils.equals(CimObjectUtils.toString(objects[23]), CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED)) {
                        durableState = CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED;
                    } else {
                        durableState = CIMStateConst.CIM_PRODUCT_PRODUCT_STATE_ACTIVE;
                    }
                }
                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData(durableState);
                hashedInfo.setHashKey("durable State");
                strDurableStatusList.add(hashedInfo);

                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData((CimObjectUtils.isEmpty(objects[21])) ? null : objects[21].toString());
                hashedInfo.setHashKey("durable Production State");
                strDurableStatusList.add(hashedInfo);

                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData((CimObjectUtils.isEmpty(objects[22])) ? null : objects[22].toString());
                hashedInfo.setHashKey("durable Hold State");
                strDurableStatusList.add(hashedInfo);

                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData((CimObjectUtils.isEmpty(objects[23])) ? null : objects[23].toString());
                hashedInfo.setHashKey("durable Finished State");
                strDurableStatusList.add(hashedInfo);

                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData((CimObjectUtils.isEmpty(objects[24])) ? null : objects[24].toString());
                hashedInfo.setHashKey("durable Process State");
                strDurableStatusList.add(hashedInfo);

                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData((CimObjectUtils.isEmpty(objects[25])) ? null : objects[25].toString());
                hashedInfo.setHashKey("durable Inventory State");
                strDurableStatusList.add(hashedInfo);

                reticlePodStatusInfo.setStrDurableStatusList(strDurableStatusList);

                //--- Set objectIdentifier bankID; ----------------------------------------//
                reticlePodListInfo.setBankID(ObjectIdentifier.build(CimObjectUtils.toString(objects[26]), CimObjectUtils.toString(objects[27])));
                reticlePodListInfo.setDueTimeStamp("");

                if (!CimObjectUtils.isEmpty(objects[28])) {
                    String sql2 = "SELECT START_BANK_ID,\n" +
                            "START_BANK_RKEY\n" +
                            "FROM OMPRP\n" +
                            "WHERE PRP_ID = ?1 \n" +
                            "AND PRP_LEVEL = ?2";
                    List<Object[]> START_BANKS = cimJpaRepository.query(sql2, CimObjectUtils.toString(objects[28]), BizConstant.SP_PD_FLOWLEVEL_MAIN);
                    Object[] START_BANK = START_BANKS.get(0);
                    reticlePodListInfo.setStartBankID(ObjectIdentifier.build(CimObjectUtils.isEmpty(START_BANK) ? null : (String) START_BANK[0], CimObjectUtils.isEmpty(START_BANK) ? null : (String) START_BANK[1]));
                }
                reticlePodListInfo.setRouteID(ObjectIdentifier.build(CimObjectUtils.toString(objects[28]), CimObjectUtils.toString(objects[29])));
                reticlePodListInfo.setOperationNumber(CimObjectUtils.toString(objects[30]));
                reticlePodListInfo.setBankInRequiredFlag((!CimObjectUtils.isEmpty(objects[31])) && Integer.parseInt((CimObjectUtils.toString(objects[31]))) == 1);

                /***************************/
                /* Get durable hold record */
                /***************************/
                Inputs.ObjDurableHoldRecordGetDRIn objDurableHoldRecordGetDRIn = new Inputs.ObjDurableHoldRecordGetDRIn();
                objDurableHoldRecordGetDRIn.setDurableCategory(CIMStateConst.SP_DURABLE_CAT_RETICLE_POD);
                objDurableHoldRecordGetDRIn.setDurableID(ObjectIdentifier.build(CimObjectUtils.toString(objects[0]), CimObjectUtils.toString(objects[1])));

                //step4 - durable_holdRecord_GetDR
                List<Infos.DurableHoldRecord> objDurableHoldRecordGetDROut = null;
                try {
                    objDurableHoldRecordGetDROut = durableMethod.durableHoldRecordGetDR(objCommon, objDurableHoldRecordGetDRIn.getDurableID(), objDurableHoldRecordGetDRIn.getDurableCategory());
                } catch (ServiceException e) {
                    break;
                }

                //--- Set objectIdentifier holdReasonCodeID; -------------------------//
                int holdLen = objDurableHoldRecordGetDROut.size();
                if (holdLen == 1) {
                    reticlePodListInfo.setHoldReasonCodeID(ObjectIdentifier.build(ObjectIdentifier.isEmpty(objDurableHoldRecordGetDROut.get(0).getHoldReasonCodeID()) ? null : objDurableHoldRecordGetDROut.get(0).getHoldReasonCodeID().getValue(), ""));
                } else if (holdLen > 1) {
                    StringBuffer holdReasonCode = new StringBuffer();
                    holdReasonCode.append(ObjectIdentifier.isEmpty(objDurableHoldRecordGetDROut.get(0).getHoldReasonCodeID()) ? null : objDurableHoldRecordGetDROut.get(0).getHoldReasonCodeID().getValue());
                    holdReasonCode.append(BizConstant.SP_DEFAULT_CHAR);
                    reticlePodListInfo.setHoldReasonCodeID(ObjectIdentifier.build(holdReasonCode.toString(), ""));
                }
                /*-------------------------------------*/
                /*   Get Reticle List for reticlepod   */
                /*-------------------------------------*/

                //step5 - reticlePod_reticleList_GetDR
                Outputs.ObjReticlePodReticleListGetDROut objReticlePodReticleListGetDROut = this.reticlePodReticleListGetDR(objCommon, aReticlePodID);
                List<Long> slotPosition = objReticlePodReticleListGetDROut.getSlotPosition();
                List<ObjectIdentifier> reticleIDs = objReticlePodReticleListGetDROut.getReticleID();
                boolean reticleIDFlag = false;
                boolean reticleGroupIDFlag = false;
                boolean reticlePartNumberFlag = false;
                List<Infos.ContainedReticleInfo> containedReticleInfos = new ArrayList<>();
                reticlePodStatusInfo.setStrContainedReticleInfo(containedReticleInfos);
                if (!CimObjectUtils.isEmpty(reticleIDs)) {
                    for (int i = 0; i < reticleIDs.size(); i++) {
                        if (CimStringUtils.isEmpty(reticleIDs.get(i).getValue())) {
                            continue;
                        }
                        /*--------------------------------------*/
                        /*   Check Input Condition (reticleID)  */
                        /*--------------------------------------*/
                        if (CimStringUtils.isNotEmpty(params.getReticleID().getValue())) {

                            //step6 - reticlePod_wildCardString_Check
                            Outputs.ObjReticlePodWildCardStringCheckOut objReticlePodWildCardStringCheckOut = this.reticlePodWildCardStringCheck(objCommon, params.getReticleID().getValue(), reticleIDs.get(i).getValue());
                            if (objReticlePodWildCardStringCheckOut.isCheckFlag()) {
                                reticleIDFlag = true;
                            }
                        } else {
                            reticleIDFlag = true;
                        }
                        /*---------------------------------*/
                        /*   Get reticle transfer status   */
                        /*---------------------------------*/
                        Inputs.ObjReticleDetailInfoGetDRIn objReticleDetailInfoGetDRIn = new Inputs.ObjReticleDetailInfoGetDRIn();
                        objReticleDetailInfoGetDRIn.setReticleID(reticleIDs.get(i));
                        objReticleDetailInfoGetDRIn.setDurableOperationInfoFlag(false);
                        objReticleDetailInfoGetDRIn.setDurableWipOperationInfoFlag(false);

                        //step7 - reticle_detailInfo_GetDR__170
                        Results.ReticleDetailInfoInqResult reticleDetailInfoInqResult = reticleDetailInfoGetDR(objCommon, objReticleDetailInfoGetDRIn.getReticleID(), objReticleDetailInfoGetDRIn.isDurableOperationInfoFlag(), objReticleDetailInfoGetDRIn.isDurableWipOperationInfoFlag());
                        Infos.ReticleBrInfo tmpReticleBRInfo = reticleDetailInfoInqResult.getReticleBRInfo();
                        /*-------------------------------------------*/
                        /*   Check Input Condition (reticleGroupID)  */
                        /*-------------------------------------------*/
                        if (!ObjectIdentifier.isEmpty(params.getReticleGroupID()) && CimStringUtils.isNotEmpty(params.getReticleGroupID().getValue())) {

                            //step8 - reticlePod_wildCardString_Check
                            Outputs.ObjReticlePodWildCardStringCheckOut objReticlePodWildCardStringCheckOut = this.reticlePodWildCardStringCheck(objCommon, params.getReticleGroupID().getValue(), tmpReticleBRInfo.getReticleGroupID().getValue());
                            if (objReticlePodWildCardStringCheckOut.isCheckFlag()) {
                                reticleGroupIDFlag = true;
                            }
                        } else {
                            reticleGroupIDFlag = true;
                        }
                        /*----------------------------------------------*/
                        /*   Check Input Condition (reticlePartNumber)  */
                        /*----------------------------------------------*/
                        if (CimStringUtils.isNotEmpty(params.getReticlePartNumber())) {

                            //step9 - reticlePod_wildCardString_Check
                            Outputs.ObjReticlePodWildCardStringCheckOut objReticlePodWildCardStringCheckOut = this.reticlePodWildCardStringCheck(objCommon, params.getReticlePartNumber(), tmpReticleBRInfo.getReticlePartNumber());
                            if (objReticlePodWildCardStringCheckOut.isCheckFlag()) {
                                reticlePartNumberFlag = true;
                            }
                        } else {
                            reticlePartNumberFlag = true;
                        }
                        Infos.ContainedReticleInfo containedReticleInfo = new Infos.ContainedReticleInfo();
                        containedReticleInfo.setSlotNo(slotPosition.get(i));
                        containedReticleInfo.setReticleID(reticleIDs.get(i));
                        containedReticleInfo.setStrReticleBrInfo(tmpReticleBRInfo);
                        containedReticleInfos.add(containedReticleInfo);
                    }
                }
                // Non Check Condition is Pick Up.
                if (CimStringUtils.isEmpty(params.getReticleID().getValue()) && CimStringUtils.isEmpty(params.getReticleGroupID().getValue()) && CimStringUtils.isEmpty(params.getReticlePartNumber())) {
                    reticleIDFlag = true;
                    reticleGroupIDFlag = true;
                    reticlePartNumberFlag = true;
                }
                if (!reticleIDFlag || !reticleGroupIDFlag || !reticlePartNumberFlag) {
                    continue;
                }
                /*---------------------*/
                /* pptReticlePodPmInfo */
                /*---------------------*/
                Timestamp aTimeStamp = Timestamp.valueOf(objects[15].toString());
                Duration aDuration = Duration.between(CimObjectUtils.isEmpty(aTimeStamp.toLocalDateTime()) ? null : aTimeStamp.toLocalDateTime(), CimObjectUtils.isEmpty(objCommon.getTimeStamp().getReportTimeStamp()) ? null : objCommon.getTimeStamp().getReportTimeStamp().toLocalDateTime());

                long minutes = aDuration.toMinutes();
                Infos.ReticlePodPmInfo reticlePodPMInfo = new Infos.ReticlePodPmInfo();
                reticlePodPMInfo.setPassageTimeFromLastPM(minutes);
                reticlePodPMInfo.setIntervalBetweenPM(Long.parseLong(CimObjectUtils.toString(objects[14])));
                reticlePodPMInfo.setLastMaintenanceTimeStamp(CimObjectUtils.toString(objects[15]));
                reticlePodPMInfo.setLastMaintenancePerson(ObjectIdentifier.build(CimObjectUtils.toString(objects[16]), CimObjectUtils.toString(objects[17])));
                reticlePodListInfo.setReticlePodPMInfo(reticlePodPMInfo);

                Infos.DurableLocationInfo reticlePodLocationInfo = new Infos.DurableLocationInfo();
                reticlePodLocationInfo.setInstanceName(CimObjectUtils.toString(objects[36]));
                reticlePodLocationInfo.setCurrentLocationFlag(Boolean.parseBoolean(CimObjectUtils.toString(objects[37])));
                reticlePodLocationInfo.setBackupState(CimObjectUtils.toString(objects[38]));
                reticlePodListInfo.setReticlePodLocationInfo(reticlePodLocationInfo);
                /*----------------------*/
                /* Set output Parameter */
                /*----------------------*/
                reticlePodListInfoList.add(reticlePodListInfo);
                count++;
                if (count >= fetchLimitCount) {
                    break;
                }
            }
        }
        return reticlePodListInfoList;
    }

    @Override
    public Page<Infos.ReticlePodListInfo> pageReticlePodList(Infos.ObjCommon objCommon, Params.ReticlePodListInqParams params) {
        List<Infos.ReticlePodListInfo> reticlePodListInfoList = new ArrayList<>();

        Map<String, Integer> availableForDurableFlagList = new HashMap<>();

        Integer availableForDurableFlag;
        String durableFlowState;
        String durableState;
        long fetchLimitCount = 0L;
        if (params.getMaxRetrieveCount() <= 0 || params.getMaxRetrieveCount() > Long.parseLong(BizConstant.SEARCH_COUNT_MAX)) {
            fetchLimitCount = Long.parseLong(BizConstant.SEARCH_COUNT_MAX);
        } else {
            fetchLimitCount = params.getMaxRetrieveCount();
        }
        StringBuffer hvBUFFER = new StringBuffer();
        String hvTMPBUFFER = new String();
        hvBUFFER.append(
                "SELECT " +
                "           RTCLPOD_ID," +
                "           ID," +
                        "   DESCRIPTION, " +
                "           RTCLPOD_TYPE_ID, " +
                "           RTCLPOD_STATE, " +
                "           XFER_STATE, " +
                "           EQP_ID, " +
                "           EQP_RKEY, " +
                "           STK_ID, " +
                "           STK_RKEY, " +
                "           RTCLPOD_USED_CAP, " +
                "           LAST_TRX_TIME, " +
                "           LAST_TRX_USER_ID, " +
                "           LAST_TRX_USER_RKEY, " +
                "           PM_INTERVAL_TIME, " +
                "           MAINT_TIME, " +
                "           MAINT_USER_ID, " +
                "           MAINT_USER_RKEY, " +
                "           DCJ_ID, " +
                "           DCJ_RKEY, " +
                "           DRBL_PRFCX_RKEY, " +
                "           DRBL_PRODUCTION_STATE, " +
                "           DRBL_HOLD_STATE, " +
                "           DRBL_FINISHED_STATE, " +
                "           DRBL_PROCESS_STATE, " +
                "           DRBL_INV_STATE, " +
                "           BANK_ID, " +
                "           BANK_RKEY, " +
                "           MAIN_PROCESS_ID, " +
                "           MAIN_PROCESS_RKEY, " +
                "           OPE_NO, " +
                "           BANK_IN_REQD, " +
                "           PP_FLAG, " +
                "           RTCLPOD_SUB_STATE_ID, " +
                "           RTCLPOD_SUB_STATE_RKEY, " +
                "           RTCLPOD_CAP, " +
                "           FAB_INSTANCE, " +
                "           CUR_SITE_FLAG, " +
                "           BCKUP_STATE " +
                "FROM       OMRTCLPOD");

        boolean bFirstCondition = true;
        /*-------------------------*/
        /*   Set Where Condition   */
        /*-------------------------*/
        if (CimStringUtils.isNotEmpty(params.getReticlePodID().getValue())) {
            /*----------------------------*/
            /*   reticlePodID Condition   */
            /*----------------------------*/
            bFirstCondition = false;
            hvBUFFER.append(" WHERE");

            //step1 - reticlePod_whereCondition_Make
            String whereConditionMake = this.reticlePodWhereConditionMake(objCommon, params.getReticlePodID().getValue());

            hvBUFFER.append(String.format(" RTCLPOD_ID %s ", whereConditionMake));
        }
        if (CimStringUtils.isNotEmpty(params.getReticlePodCategory())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }

            //step2 - reticlePod_whereCondition_Make
            String whereConditionMake = this.reticlePodWhereConditionMake(objCommon, params.getReticlePodCategory());

            hvBUFFER.append(String.format(" RTCLPOD_TYPE_ID %s ", whereConditionMake));
        }
        if (CimStringUtils.isNotEmpty(params.getReticlePodStatus())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }

            //step3 - reticlePod_whereCondition_Make
            String whereConditionMake = this.reticlePodWhereConditionMake(objCommon, params.getReticlePodStatus());

            hvBUFFER.append(String.format(" RTCLPOD_STATE %s ", whereConditionMake));
        }
        if (CimStringUtils.isNotEmpty(params.getEquipmentID().getValue())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }
            hvBUFFER.append(String.format(" EQP_ID = '%s' ", params.getEquipmentID().getValue()));
        }
        if (CimStringUtils.isNotEmpty(params.getStockerID().getValue())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }
            hvBUFFER.append(String.format(" EQP_ID = '%s' ", params.getStockerID().getValue()));
        }
        if (params.isEmptyFlag()) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }
            hvBUFFER.append(" RTCLPOD_USED_CAP = 0 ");
        }
        if (CimStringUtils.isNotEmpty(ObjectIdentifier.isEmpty(params.getBankID()) ? null : params.getBankID().getValue())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }
            hvBUFFER.append(String.format(" BANK_ID = '%s' ", params.getBankID().getValue()));
        }
        if (CimStringUtils.isNotEmpty(params.getDurableSubStatus().getValue())) {
            if (bFirstCondition) {
                bFirstCondition = false;
                hvBUFFER.append(" WHERE");
            } else {
                hvBUFFER.append(" AND");
            }
            hvBUFFER.append(String.format(" RTCLPOD_SUB_STATE_ID = '%s' ", params.getDurableSubStatus().getValue()));
        }

        String flowStatus = params.getFlowStatus();
        if (CimStringUtils.isNotEmpty(flowStatus)) {
            boolean durableStatusFlag = true;
            if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_DURABLE_SCRAPPED)) {
                hvTMPBUFFER = String.format(" MAIN_PROCESS_ID <> '' AND DRBL_STATE = '%s' ", flowStatus);
            } else if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                hvTMPBUFFER = String.format(" MAIN_PROCESS_ID <> '' AND DRBL_HOLD_STATE = '%s' ", flowStatus);
            } else if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED)) {
                hvTMPBUFFER = String.format(" MAIN_PROCESS_ID <> '' AND DRBL_FINISHED_STATE = '%s' ", flowStatus);
            } else if (CimStringUtils.equals(flowStatus, CIMStateConst.SP_DURABLE_PROCESS_STATE_WAITING) ||
                    CimStringUtils.equals(flowStatus, CIMStateConst.CIM_PRODUCT_ACTIVITY_STATE_PROCESSING)) {
                hvTMPBUFFER = String.format(" MAINPD_ID <> '' AND DRBL_PROCESS_STATE = '%s' ", flowStatus);
            } else {
                durableStatusFlag = false;
            }
            if (durableStatusFlag) {
                if (bFirstCondition) {
                    bFirstCondition = false;
                    hvBUFFER.append(" WHERE");
                } else {
                    hvBUFFER.append(" AND");
                }
                hvBUFFER.append(hvTMPBUFFER);
                if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                    hvBUFFER.append(String.format(" AND DRBL_STATE <> '%s' ", CIMStateConst.CIM_DURABLE_SCRAPPED));
                } else if (CimStringUtils.equals(flowStatus, CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED)) {
                    hvBUFFER.append(String.format(" AND DRBL_STATE <> '%s' ", CIMStateConst.CIM_DURABLE_SCRAPPED));
                    hvBUFFER.append(String.format(" AND DRBL_HOLD_STATE <> '%s' ", CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD));
                } else if (CimStringUtils.equals(flowStatus, CIMStateConst.SP_DURABLE_PROCESS_STATE_WAITING) ||
                        CimStringUtils.equals(flowStatus, CIMStateConst.CIM_PRODUCT_ACTIVITY_STATE_PROCESSING)) {
                    hvBUFFER.append(String.format(" AND DRBL_STATE <> '%s' ", CIMStateConst.CIM_DURABLE_SCRAPPED));
                    hvBUFFER.append(String.format(" AND DRBL_HOLD_STATE <> '%s' ", CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD));
                    hvBUFFER.append(String.format(" AND DRBL_FINISHED_STATE <> '%s' ", CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED));
                }
            }
        }
        hvBUFFER.append(" ORDER BY RTCLPOD_ID");
        //-------------------------------------------
        // Judge and Convert SQL with Escape Sequence
        //-------------------------------------------
        String originalSQL = hvBUFFER.toString();
        /*-----------------*/
        /*   SQL PREPARE   */
        /*-----------------*/
        Page<Object[]> pageInfo = cimJpaRepository.query(originalSQL,params.getSearchCondition());
        List<Object[]> reticlePodList = pageInfo.getContent();
        int count = 0;
        if (CimArrayUtils.isNotEmpty(reticlePodList)) {
            for (Object[] objects : reticlePodList) {
                Infos.ReticlePodListInfo reticlePodListInfo = new Infos.ReticlePodListInfo();

                ObjectIdentifier aReticlePodID = new ObjectIdentifier();
                Infos.ReticlePodBrInfo reticlePodBRInfo = new Infos.ReticlePodBrInfo();
                Infos.ReticlePodStatusInfo reticlePodStatusInfo = new Infos.ReticlePodStatusInfo();

                //赋值
                reticlePodListInfo.setReticlePodID(aReticlePodID);
                reticlePodListInfo.setReticlePodBRInfo(reticlePodBRInfo);
                reticlePodListInfo.setReticlePodStatusInfo(reticlePodStatusInfo);


                aReticlePodID.setValue(CimObjectUtils.toString(objects[0]));
                aReticlePodID.setReferenceKey(CimObjectUtils.toString(objects[1]));

                reticlePodBRInfo.setDescription(CimObjectUtils.toString(objects[2]));
                reticlePodBRInfo.setReticlePodCategory(CimObjectUtils.toString(objects[3]));
                reticlePodBRInfo.setCapacity(CimNumberUtils.longValue(CimObjectUtils.toString(objects[35])));

                reticlePodStatusInfo.setReticlePodStatus(CimObjectUtils.toString(objects[4]));
                reticlePodStatusInfo.setTransferStatus(CimObjectUtils.toString(objects[5]));

                // can't use STK_ID, STK_OBJ
                if (!CimObjectUtils.isEmpty(objects[6])) {
                    if (CimStringUtils.equals(CimObjectUtils.toString(objects[5]), CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN) ||
                            CimStringUtils.equals(CimObjectUtils.toString(objects[5]), CIMStateConst.SP_TRANS_STATE_EQUIPMENT_OUT)) {
                        reticlePodStatusInfo.setEquipmentID(ObjectIdentifier.build(CimObjectUtils.toString(objects[6]), CimObjectUtils.toString(objects[7])));
                    } else {
                        reticlePodStatusInfo.setStockerID(ObjectIdentifier.build(CimObjectUtils.toString(objects[6]), CimObjectUtils.toString(objects[7])));
                    }
                }
                if (Integer.valueOf(String.valueOf(objects[10])) > 0) {
                    reticlePodStatusInfo.setEmptyFlag(false);
                } else {
                    reticlePodStatusInfo.setEmptyFlag(true);
                }
                reticlePodStatusInfo.setLastClaimedTimeStamp(CimObjectUtils.toString(objects[11]));
                reticlePodStatusInfo.setLastClaimedPerson(ObjectIdentifier.build(CimObjectUtils.toString(objects[12]), CimObjectUtils.toString(objects[13])));

                reticlePodStatusInfo.setDurableControlJobID(ObjectIdentifier.build(CimObjectUtils.toString(objects[18]), CimObjectUtils.toString(objects[19])));
                if (!CimObjectUtils.isEmpty(objects[20])) {
                    reticlePodStatusInfo.setDurableSTBFlag(true);
                } else {
                    reticlePodStatusInfo.setDurableSTBFlag(false);
                }
                reticlePodStatusInfo.setDurableSubStatus(ObjectIdentifier.build(CimObjectUtils.toString(objects[33]), CimObjectUtils.toString(objects[34])));
                //--- Set availableForDurableFlag; ------------------------------------//
                availableForDurableFlag = 0;
                if (!CimObjectUtils.isEmpty(objects[33])) {
                    if (!availableForDurableFlagList.containsKey(CimObjectUtils.toString(objects[33]))) {
                        String sql1 = "SELECT PROC_AVAIL_FLAG\n" +
                                "FROM OMDRBLST\n" +
                                "WHERE DRBL_SUBSTATE_ID = ?1 ";
                        Object distinct = cimJpaRepository.queryOneColumnAndUnique(sql1, CimObjectUtils.toString(objects[33]));
                        availableForDurableFlag = CimBooleanUtils.isTrue(!CimObjectUtils.isEmpty(distinct) && (CimObjectUtils.equals(distinct.toString(), 1))) ? 1 : 0;
                        availableForDurableFlagList.put(CimObjectUtils.toString(objects[33]), availableForDurableFlag);
                    }
                }
                reticlePodStatusInfo.setAvailableForDurableFlag(availableForDurableFlag == 1);
                reticlePodStatusInfo.setInPostProcessFlagOfReticlePod(Boolean.parseBoolean(CimObjectUtils.toString(objects[32])));
                //--- Set pptHashedInfoSequence strDurableStatusList; ----------------------//
                List<Infos.HashedInfo> strDurableStatusList = new ArrayList<>();
                durableFlowState = "";
                if (!CimObjectUtils.isEmpty(objects[28])) {
                    if (CimStringUtils.equals(CimObjectUtils.toString(objects[4]), CIMStateConst.CIM_DURABLE_SCRAPPED)) {
                        durableFlowState = CimObjectUtils.toString(objects[4]);
                    } else if (CimStringUtils.equals(CimObjectUtils.toString(objects[22]), CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                        durableFlowState = CimObjectUtils.toString(objects[22]);
                    } else if (CimStringUtils.equals(CimObjectUtils.toString(objects[23]), CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED)) {
                        durableFlowState = CimObjectUtils.toString(objects[23]);
                    } else {
                        durableFlowState = CimObjectUtils.toString(objects[24]);
                    }
                }
                Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData(durableFlowState);
                hashedInfo.setHashKey("durable Flow State");
                strDurableStatusList.add(hashedInfo);

                durableState = "";
                if (!CimObjectUtils.isEmpty(objects[28])) {
                    if (CimStringUtils.equals(CimObjectUtils.toString(objects[4]), CIMStateConst.CIM_DURABLE_SCRAPPED) ||
                            CimStringUtils.equals(CimObjectUtils.toString(objects[23]), CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED)) {
                        durableState = CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED;
                    } else {
                        durableState = CIMStateConst.CIM_PRODUCT_PRODUCT_STATE_ACTIVE;
                    }
                }
                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData(durableState);
                hashedInfo.setHashKey("durable State");
                strDurableStatusList.add(hashedInfo);

                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData((CimObjectUtils.isEmpty(objects[21])) ? null : objects[21].toString());
                hashedInfo.setHashKey("durable Production State");
                strDurableStatusList.add(hashedInfo);

                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData((CimObjectUtils.isEmpty(objects[22])) ? null : objects[22].toString());
                hashedInfo.setHashKey("durable Hold State");
                strDurableStatusList.add(hashedInfo);

                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData((CimObjectUtils.isEmpty(objects[23])) ? null : objects[23].toString());
                hashedInfo.setHashKey("durable Finished State");
                strDurableStatusList.add(hashedInfo);

                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData((CimObjectUtils.isEmpty(objects[24])) ? null : objects[24].toString());
                hashedInfo.setHashKey("durable Process State");
                strDurableStatusList.add(hashedInfo);

                hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashData((CimObjectUtils.isEmpty(objects[25])) ? null : objects[25].toString());
                hashedInfo.setHashKey("durable Inventory State");
                strDurableStatusList.add(hashedInfo);

                reticlePodStatusInfo.setStrDurableStatusList(strDurableStatusList);

                //--- Set objectIdentifier bankID; ----------------------------------------//
                reticlePodListInfo.setBankID(ObjectIdentifier.build(CimObjectUtils.toString(objects[26]), CimObjectUtils.toString(objects[27])));
                reticlePodListInfo.setDueTimeStamp("");

                if (!CimObjectUtils.isEmpty(objects[28])) {
                    String sql2 = "SELECT START_BANK_ID,\n" +
                            "START_BANK_RKEY\n" +
                            "FROM OMPRP\n" +
                            "WHERE PRP_ID = ?1 \n" +
                            "AND PRP_LEVEL = ?2";
                    List<Object[]> START_BANKS = cimJpaRepository.query(sql2, CimObjectUtils.toString(objects[28]), BizConstant.SP_PD_FLOWLEVEL_MAIN);
                    Object[] START_BANK = START_BANKS.get(0);
                    reticlePodListInfo.setStartBankID(ObjectIdentifier.build(CimObjectUtils.isEmpty(START_BANK) ? null : (String) START_BANK[0], CimObjectUtils.isEmpty(START_BANK) ? null : (String) START_BANK[1]));
                }
                reticlePodListInfo.setRouteID(ObjectIdentifier.build(CimObjectUtils.toString(objects[28]), CimObjectUtils.toString(objects[29])));
                reticlePodListInfo.setOperationNumber(CimObjectUtils.toString(objects[30]));
                reticlePodListInfo.setBankInRequiredFlag((!CimObjectUtils.isEmpty(objects[31])) && Integer.parseInt((CimObjectUtils.toString(objects[31]))) == 1);

                /***************************/
                /* Get durable hold record */
                /***************************/
                Inputs.ObjDurableHoldRecordGetDRIn objDurableHoldRecordGetDRIn = new Inputs.ObjDurableHoldRecordGetDRIn();
                objDurableHoldRecordGetDRIn.setDurableCategory(CIMStateConst.SP_DURABLE_CAT_RETICLE_POD);
                objDurableHoldRecordGetDRIn.setDurableID(ObjectIdentifier.build(CimObjectUtils.toString(objects[0]), CimObjectUtils.toString(objects[1])));

                //step4 - durable_holdRecord_GetDR
                List<Infos.DurableHoldRecord> objDurableHoldRecordGetDROut = null;
                try {
                    objDurableHoldRecordGetDROut = durableMethod.durableHoldRecordGetDR(objCommon, objDurableHoldRecordGetDRIn.getDurableID(), objDurableHoldRecordGetDRIn.getDurableCategory());
                } catch (ServiceException e) {
                    break;
                }

                //--- Set objectIdentifier holdReasonCodeID; -------------------------//
                int holdLen = objDurableHoldRecordGetDROut.size();
                if (holdLen == 1) {
                    reticlePodListInfo.setHoldReasonCodeID(ObjectIdentifier.build(ObjectIdentifier.isEmpty(objDurableHoldRecordGetDROut.get(0).getHoldReasonCodeID()) ? null : objDurableHoldRecordGetDROut.get(0).getHoldReasonCodeID().getValue(), ""));
                } else if (holdLen > 1) {
                    StringBuffer holdReasonCode = new StringBuffer();
                    holdReasonCode.append(ObjectIdentifier.isEmpty(objDurableHoldRecordGetDROut.get(0).getHoldReasonCodeID()) ? null : objDurableHoldRecordGetDROut.get(0).getHoldReasonCodeID().getValue());
                    holdReasonCode.append(BizConstant.SP_DEFAULT_CHAR);
                    reticlePodListInfo.setHoldReasonCodeID(ObjectIdentifier.build(holdReasonCode.toString(), ""));
                }
                /*-------------------------------------*/
                /*   Get Reticle List for reticlepod   */
                /*-------------------------------------*/

                //step5 - reticlePod_reticleList_GetDR
                Outputs.ObjReticlePodReticleListGetDROut objReticlePodReticleListGetDROut = this.reticlePodReticleListGetDR(objCommon, aReticlePodID);
                List<Long> slotPosition = objReticlePodReticleListGetDROut.getSlotPosition();
                List<ObjectIdentifier> reticleIDs = objReticlePodReticleListGetDROut.getReticleID();
                boolean reticleIDFlag = false;
                boolean reticleGroupIDFlag = false;
                boolean reticlePartNumberFlag = false;
                List<Infos.ContainedReticleInfo> containedReticleInfos = new ArrayList<>();
                reticlePodStatusInfo.setStrContainedReticleInfo(containedReticleInfos);
                if (!CimObjectUtils.isEmpty(reticleIDs)) {
                    for (int i = 0; i < reticleIDs.size(); i++) {
                        if (CimStringUtils.isEmpty(reticleIDs.get(i).getValue())) {
                            continue;
                        }
                        /*--------------------------------------*/
                        /*   Check Input Condition (reticleID)  */
                        /*--------------------------------------*/
                        if (CimStringUtils.isNotEmpty(params.getReticleID().getValue())) {

                            //step6 - reticlePod_wildCardString_Check
                            Outputs.ObjReticlePodWildCardStringCheckOut objReticlePodWildCardStringCheckOut = this.reticlePodWildCardStringCheck(objCommon, params.getReticleID().getValue(), reticleIDs.get(i).getValue());
                            if (objReticlePodWildCardStringCheckOut.isCheckFlag()) {
                                reticleIDFlag = true;
                            }
                        } else {
                            reticleIDFlag = true;
                        }
                        /*---------------------------------*/
                        /*   Get reticle transfer status   */
                        /*---------------------------------*/
                        Inputs.ObjReticleDetailInfoGetDRIn objReticleDetailInfoGetDRIn = new Inputs.ObjReticleDetailInfoGetDRIn();
                        objReticleDetailInfoGetDRIn.setReticleID(reticleIDs.get(i));
                        objReticleDetailInfoGetDRIn.setDurableOperationInfoFlag(false);
                        objReticleDetailInfoGetDRIn.setDurableWipOperationInfoFlag(false);

                        //step7 - reticle_detailInfo_GetDR__170
                        Results.ReticleDetailInfoInqResult reticleDetailInfoInqResult = reticleDetailInfoGetDR(objCommon, objReticleDetailInfoGetDRIn.getReticleID(), objReticleDetailInfoGetDRIn.isDurableOperationInfoFlag(), objReticleDetailInfoGetDRIn.isDurableWipOperationInfoFlag());
                        Infos.ReticleBrInfo tmpReticleBRInfo = reticleDetailInfoInqResult.getReticleBRInfo();
                        /*-------------------------------------------*/
                        /*   Check Input Condition (reticleGroupID)  */
                        /*-------------------------------------------*/
                        if (!ObjectIdentifier.isEmpty(params.getReticleGroupID()) && CimStringUtils.isNotEmpty(params.getReticleGroupID().getValue())) {

                            //step8 - reticlePod_wildCardString_Check
                            Outputs.ObjReticlePodWildCardStringCheckOut objReticlePodWildCardStringCheckOut = this.reticlePodWildCardStringCheck(objCommon, params.getReticleGroupID().getValue(), tmpReticleBRInfo.getReticleGroupID().getValue());
                            if (objReticlePodWildCardStringCheckOut.isCheckFlag()) {
                                reticleGroupIDFlag = true;
                            }
                        } else {
                            reticleGroupIDFlag = true;
                        }
                        /*----------------------------------------------*/
                        /*   Check Input Condition (reticlePartNumber)  */
                        /*----------------------------------------------*/
                        if (CimStringUtils.isNotEmpty(params.getReticlePartNumber())) {

                            //step9 - reticlePod_wildCardString_Check
                            Outputs.ObjReticlePodWildCardStringCheckOut objReticlePodWildCardStringCheckOut = this.reticlePodWildCardStringCheck(objCommon, params.getReticlePartNumber(), tmpReticleBRInfo.getReticlePartNumber());
                            if (objReticlePodWildCardStringCheckOut.isCheckFlag()) {
                                reticlePartNumberFlag = true;
                            }
                        } else {
                            reticlePartNumberFlag = true;
                        }
                        Infos.ContainedReticleInfo containedReticleInfo = new Infos.ContainedReticleInfo();
                        containedReticleInfo.setSlotNo(slotPosition.get(i));
                        containedReticleInfo.setReticleID(reticleIDs.get(i));
                        containedReticleInfo.setStrReticleBrInfo(tmpReticleBRInfo);
                        containedReticleInfos.add(containedReticleInfo);
                    }
                }
                // Non Check Condition is Pick Up.
                if (CimStringUtils.isEmpty(params.getReticleID().getValue()) && CimStringUtils.isEmpty(params.getReticleGroupID().getValue()) && CimStringUtils.isEmpty(params.getReticlePartNumber())) {
                    reticleIDFlag = true;
                    reticleGroupIDFlag = true;
                    reticlePartNumberFlag = true;
                }
                if (!reticleIDFlag || !reticleGroupIDFlag || !reticlePartNumberFlag) {
                    continue;
                }
                /*---------------------*/
                /* pptReticlePodPmInfo */
                /*---------------------*/
                Timestamp aTimeStamp = Timestamp.valueOf(objects[15].toString());
                Duration aDuration = Duration.between(CimObjectUtils.isEmpty(aTimeStamp.toLocalDateTime()) ? null : aTimeStamp.toLocalDateTime(), CimObjectUtils.isEmpty(objCommon.getTimeStamp().getReportTimeStamp()) ? null : objCommon.getTimeStamp().getReportTimeStamp().toLocalDateTime());

                long minutes = aDuration.toMinutes();
                Infos.ReticlePodPmInfo reticlePodPMInfo = new Infos.ReticlePodPmInfo();
                reticlePodPMInfo.setPassageTimeFromLastPM(minutes);
                reticlePodPMInfo.setIntervalBetweenPM(Long.parseLong(CimObjectUtils.toString(objects[14])));
                reticlePodPMInfo.setLastMaintenanceTimeStamp(CimObjectUtils.toString(objects[15]));
                reticlePodPMInfo.setLastMaintenancePerson(ObjectIdentifier.build(CimObjectUtils.toString(objects[16]), CimObjectUtils.toString(objects[17])));
                reticlePodListInfo.setReticlePodPMInfo(reticlePodPMInfo);

                Infos.DurableLocationInfo reticlePodLocationInfo = new Infos.DurableLocationInfo();
                reticlePodLocationInfo.setInstanceName(CimObjectUtils.toString(objects[36]));
                reticlePodLocationInfo.setCurrentLocationFlag(Boolean.parseBoolean(CimObjectUtils.toString(objects[37])));
                reticlePodLocationInfo.setBackupState(CimObjectUtils.toString(objects[38]));
                reticlePodListInfo.setReticlePodLocationInfo(reticlePodLocationInfo);
                /*----------------------*/
                /* Set output Parameter */
                /*----------------------*/
                reticlePodListInfoList.add(reticlePodListInfo);
                count++;
                if (count >= fetchLimitCount) {
                    break;
                }
            }
        }
        return new PageImpl<>(reticlePodListInfoList,pageInfo.getPageable(),pageInfo.getTotalElements());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/7                            Wind
     *
     * @param objCommon
     * @param params
     * @return RetCode<Outputs.ObjReticlePodFillInTxPDQ013DROut>
     * @author Wind
     * @date 2018/11/7 18:10
     */
    @Override
    public Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DR(Infos.ObjCommon objCommon, Params.ReticlePodDetailInfoInqParams params) {
        Outputs.ObjReticlePodFillInTxPDQ013DROut objReticlePodFillInTxPDQ013DROut = new Outputs.ObjReticlePodFillInTxPDQ013DROut();

        Infos.ReticlePodBrInfo reticlePodBRInfo = new Infos.ReticlePodBrInfo();
        Infos.ReticlePodStatusInfo reticlePodStatusInfo = new Infos.ReticlePodStatusInfo();
        List<Infos.ContainedReticleInfo> strContainedReticleInfo = new ArrayList<>();
        reticlePodStatusInfo.setStrContainedReticleInfo(strContainedReticleInfo);
        Infos.ReticlePodPmInfo reticlePodPMInfo = new Infos.ReticlePodPmInfo();
        Infos.DurableLocationInfo reticlePodLocationInfo = new Infos.DurableLocationInfo();
        Infos.DurableOperationInfo strDurableOperationInfo = new Infos.DurableOperationInfo();
        Infos.DurableWipOperationInfo strDurableWipOperationInfo = new Infos.DurableWipOperationInfo();

        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, params.getReticlePodID());
        com.fa.cim.newcore.bo.pd.CimProcessDefinition aProcessDefinition = baseCoreFactory.newBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class);

        if (aReticlePod != null) {
            /*---------------------*/
            /* pptReticlePodBrInfo */
            /*---------------------*/
            reticlePodBRInfo.setDescription(aReticlePod.getDescription());
            CimCode category = aReticlePod.getCategory();
            reticlePodBRInfo.setReticlePodCategory((category == null) ? null : category.getIdentifier());
            reticlePodBRInfo.setCapacity(aReticlePod.getCapacity());

            /*-------------------------*/
            /* pptReticlePodStatusInfo */
            /*-------------------------*/
            reticlePodStatusInfo.setReticlePodStatus(aReticlePod.getDurableState());
            reticlePodStatusInfo.setTransferStatus(aReticlePod.getTransferStatus());
            CimPerson cimPerson = aReticlePod.getTransferReserveUser();
            reticlePodStatusInfo.setTransferReserveUserID(null == cimPerson ? null :
                    ObjectIdentifier.build(cimPerson.getIdentifier(), cimPerson.getPrimaryKey()));

            // can't use STK_ID, STK_OBJ
            if (!CimObjectUtils.isEmpty(aReticlePod.currentAssignedMachine())) {
                if (CimStringUtils.equals(aReticlePod.getTransferStatus(), CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN) ||
                        CimStringUtils.equals(aReticlePod.getTransferStatus(), CIMStateConst.SP_TRANS_STATE_EQUIPMENT_OUT)) {
                    reticlePodStatusInfo.setEquipmentID(ObjectIdentifier.build(aReticlePod.currentAssignedMachine().getIdentifier(), aReticlePod.currentAssignedMachine().getPrimaryKey()));
                } else {
                    reticlePodStatusInfo.setStockerID(ObjectIdentifier.build(aReticlePod.currentAssignedMachine().getIdentifier(), aReticlePod.currentAssignedMachine().getPrimaryKey()));
                    reticlePodStatusInfo.setShelfPosition(new Infos.ShelfPosition(aReticlePod.getShelfPositionX(), aReticlePod.getShelfPositionY(), aReticlePod.getShelfPositionZ()));
                }
            }
            if (aReticlePod.getUsedCapacity() > 0) {
                reticlePodStatusInfo.setEmptyFlag(false);
            } else {
                reticlePodStatusInfo.setEmptyFlag(true);
            }
            //reticlePodStatusInfo.setLatestOperatedTimestamp(aReticlePod.getLatestOperatedTimestamp().toString());
            if (null != aReticlePod.getLastClaimedPerson()) {
                reticlePodStatusInfo.setLastClaimedPerson(ObjectIdentifier.build(aReticlePod.getLastClaimedPerson().getIdentifier(), aReticlePod.getLastClaimedPerson().getPrimaryKey()));
            }
            if (!CimObjectUtils.isEmpty(aReticlePod.getDurableControlJob())) {
                reticlePodStatusInfo.setDurableControlJobID(ObjectIdentifier.build(aReticlePod.getDurableControlJob().getIdentifier(), aReticlePod.getDurableControlJob().getPrimaryKey()));
            }

            if (!CimObjectUtils.isEmpty(aReticlePod.getDurableProcessFlowContext())) {
                reticlePodStatusInfo.setDurableSTBFlag(true);
            } else {
                reticlePodStatusInfo.setDurableSTBFlag(false);
            }
            //--- Set pptHashedInfoSequence strDurableStatusList; ----------------------//
            List<Infos.HashedInfo> strDurableStatusList = new ArrayList<>();
            String durableFlowState = "";
            if (!CimObjectUtils.isEmpty(aReticlePod.getMainProcessDefinition())) {
                if (CimStringUtils.equals(aReticlePod.getDurableState(), CIMStateConst.CIM_DURABLE_SCRAPPED)) {
                    durableFlowState = aReticlePod.getDurableState();
                } else if (CimStringUtils.equals(aReticlePod.getDurableHoldState(), CIMStateConst.CIM_PRRQ_PROD_STATE_ONHOLD)) {
                    durableFlowState = aReticlePod.getDurableHoldState();
                } else if (CimStringUtils.equals(aReticlePod.getDurableFinishedState(), CIMStateConst.CIM_PRRQ_PROD_STATE_COMPLETED)) {
                    durableFlowState = aReticlePod.getDurableFinishedState();
                } else {
                    durableFlowState = aReticlePod.getDurableProcessState();
                }
            }
            Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
            hashedInfo.setHashKey("durable Flow State");
            hashedInfo.setHashData(durableFlowState);
            strDurableStatusList.add(hashedInfo);

            //strDurableStatusList.get(0).setHashKey("durable Flow State");
            //strDurableStatusList.get(0).setHashData(durableFlowState);

            String durableState = "";
            if (!CimObjectUtils.isEmpty(aReticlePod.getMainProcessDefinition())) {
                if (CimStringUtils.equals(aReticlePod.getDurableState(), CIMStateConst.CIM_DURABLE_SCRAPPED) ||
                        CimStringUtils.equals(aReticlePod.getDurableFinishedState(), CIMStateConst.SP_DURABLE_FINISHED_STATE_COMPLETED)) {
                    durableState = CIMStateConst.SP_DURABLE_ON_ROUTE_STATE_FINISHED;
                } else {
                    durableState = CIMStateConst.SP_DURABLE_ON_ROUTE_STATE_ACTIVE;
                }
            }
            hashedInfo = new Infos.HashedInfo();
            hashedInfo.setHashKey("durable State");
            hashedInfo.setHashData(durableState);
            strDurableStatusList.add(hashedInfo);

            //strDurableStatusList.get(1).setHashKey("durable State");
            //strDurableStatusList.get(1).setHashData(durableState);

            hashedInfo = new Infos.HashedInfo();
            hashedInfo.setHashKey("durable Production State");
            hashedInfo.setHashData(aReticlePod.getDurableProductionState());
            strDurableStatusList.add(hashedInfo);

            //strDurableStatusList.get(2).setHashKey("durable Production State");
            //strDurableStatusList.get(2).setHashData(reticlePod0.getDurableProductionState());

            hashedInfo = new Infos.HashedInfo();
            hashedInfo.setHashKey("durable Hold State");
            hashedInfo.setHashData(aReticlePod.getDurableHoldState());
            strDurableStatusList.add(hashedInfo);

            //strDurableStatusList.get(3).setHashKey("durable Hold State");
            //strDurableStatusList.get(3).setHashData(reticlePod0.getDurableHoldState());

            hashedInfo = new Infos.HashedInfo();
            hashedInfo.setHashKey("durable Finished State");
            hashedInfo.setHashData(aReticlePod.getDurableFinishedState());
            strDurableStatusList.add(hashedInfo);

            //strDurableStatusList.get(4).setHashKey("durable Finished State");
            //strDurableStatusList.get(4).setHashData(reticlePod0.getDurableFinishedState());

            hashedInfo = new Infos.HashedInfo();
            hashedInfo.setHashKey("durable Process State");
            hashedInfo.setHashData(aReticlePod.getDurableProcessState());
            strDurableStatusList.add(hashedInfo);

            //strDurableStatusList.get(5).setHashKey("durable Process State");
            //strDurableStatusList.get(5).setHashData(reticlePod0.getDurableProcessState());

            hashedInfo = new Infos.HashedInfo();
            hashedInfo.setHashKey("durable Inventory State");
            hashedInfo.setHashData(aReticlePod.getDurableInventoryState());
            strDurableStatusList.add(hashedInfo);

            //strDurableStatusList.get(6).setHashKey("durable Inventory State");
            //strDurableStatusList.get(6).setHashData(reticlePod0.getDurableInventoryState());

            reticlePodStatusInfo.setStrDurableStatusList(strDurableStatusList);

            reticlePodStatusInfo.setDurableSubStatus((aReticlePod.getDurableSubState() == null) ? null : ObjectIdentifier.build(aReticlePod.getDurableSubState().getIdentifier(), aReticlePod.getDurableSubState().getPrimaryKey()));

            if (!CimObjectUtils.isEmpty(aReticlePod.getDurableSubState())) {
                CimDurableSubState aDurableSubState = aReticlePod.getDurableSubState();
                reticlePodStatusInfo.setDurableSubStatusName(CimObjectUtils.isEmpty(aDurableSubState) ? null : aDurableSubState.getDurableSubStateName());
                reticlePodStatusInfo.setDurableSubStatusDescription(aDurableSubState.getDescription());
                reticlePodStatusInfo.setAvailableForDurableFlag(aDurableSubState.isDurableProcessAvailable());
            }
            //--- Set string processLagTime; ------------------------------------//
            Timestamp processLagTime = aReticlePod.getProcessLagTime();
            if (!CimObjectUtils.isEmpty(processLagTime)) {
                reticlePodStatusInfo.setProcessLagTime(aReticlePod.getProcessLagTime().toString());
            } else {
                reticlePodStatusInfo.setProcessLagTime("");
            }
            //--------------------------------------------------------------------------------------------//
            // set DurableOperationInfo & DurableWipOperationInfo                                         //
            //--------------------------------------------------------------------------------------------//
            String hFRDRBLPOMODPOSOBJ = null;
            String hFRDRBLPOPOOBJ = null;

            strDurableOperationInfo.setDueTimeStamp("");
            CimBank bank = aReticlePod.getBank();
            strDurableOperationInfo.setBankID(null == bank ? null : bank.getBankID());
            if (params.isDurableOperationInfoFlag()) {
                ObjectIdentifier active_pdID = new ObjectIdentifier();
                String currentModulePFSystemkey;
                int currentPoslistSeqno = 0;
                CimDurableProcessOperation aDurableProcessOperation = aReticlePod.getDurableProcessOperation();
                hFRDRBLPOPOOBJ = CimObjectUtils.isEmpty(aDurableProcessOperation) ? null : aDurableProcessOperation.getProcessDefinition().getPrimaryKey();
                Object[] pfAndPfPosListByPfIdAndDkey = null;
                if (!CimObjectUtils.isEmpty(aDurableProcessOperation)) {
                    active_pdID.setValue(aDurableProcessOperation.getProcessDefinition().getIdentifier());
                    active_pdID.setReferenceKey(aDurableProcessOperation.getProcessDefinition().getPrimaryKey());
                    //------------------------------------------//
                    //  Get systemkey of current module PF      //
                    //------------------------------------------//
                    String dTheSystemKeyModulePF = CimStringUtils.strRrChr(aDurableProcessOperation.getModuleProcessFlow().getPrimaryKey(), CIMStateConst.SP_Charater_Pound);
                    if (CimStringUtils.isNotEmpty(dTheSystemKeyModulePF)) {
                        dTheSystemKeyModulePF = dTheSystemKeyModulePF.substring(1, dTheSystemKeyModulePF.length());
                    }
                    currentModulePFSystemkey = dTheSystemKeyModulePF;
                    //------------------------------------------------------------//
                    //  Get seqno of current module POS in current module PF      //
                    //------------------------------------------------------------//
                    String sql1 = "SELECT IDX_NO\n" +
                            "FROM OMPRF_PRSSSEQ\n" +
                            "WHERE REFKEY = ?1 \n" +
                            "AND LINK_KEY    = ?2 ";
                    List<Object[]> queryD_SEQNO = cimJpaRepository.query(sql1, currentModulePFSystemkey, aDurableProcessOperation.getModuleOperationNumber());
                    Object[] D_SEQNO = queryD_SEQNO.get(0);
                    if (!CimObjectUtils.isEmpty(D_SEQNO)) {
                        currentPoslistSeqno = (Integer) D_SEQNO[0];
                    }
                    //----------------------------------------//
                    //  Check state of current module PF      //
                    //----------------------------------------//
                    CimProcessFlow aProcessFlow = baseCoreFactory.getBO(CimProcessFlow.class, currentModulePFSystemkey);
                    String sql5 = "SELECT\n" +
                            "PRP_ID,\n" +
                            "ACTIVE_FLAG,\n" +
                            "FROM\n" +
                            "OMPRF\n" +
                            "WHERE\n" +
                            "ID = ?1 ";
                    List<Object> PF_Informations = cimJpaRepository.query(sql5, Object.class, currentModulePFSystemkey);
                    String hFRPFMAINPD_ID = PF_Informations.get(0).toString();
                    //----------------------------------------//
                    //  Get info from active module PF        //
                    //----------------------------------------//
                    String sql2 = "SELECT OMPRF.ID,\n" +
                            "OMPRF_PRSSSEQ.IDX_NO,\n" +
                            "OMPRF_PRSSSEQ.PRSS_RKEY\n" +
                            "FROM OMPRF, OMPRF_PRSSSEQ\n" +
                            "WHERE OMPRF.PRP_ID    = ?1 \n" +
                            "AND OMPRF.ACTIVE_FLAG          = 1\n" +
                            "AND OMPRF.ID = OMPRF_PRSSSEQ.REFKEY\n" +
                            "AND OMPRF_PRSSSEQ.LINK_KEY  = ?2 \n" +
                            "AND OMPRF.PRP_LEVEL       = ?3 ";
                    List<Object[]> objects = cimJpaRepository.query(sql2, hFRPFMAINPD_ID, aDurableProcessOperation.getModuleOperationNumber(), CIMStateConst.SP_PD_FLOWLEVEL_MODULE);
                    Object[] object = objects.get(0);
                    if (!CimObjectUtils.isEmpty(object)) {
                        hFRDRBLPOMODPOSOBJ = object[2].toString();
                    }
                    String dTheSystemKeyPD = CimStringUtils.strRrChr(active_pdID.getReferenceKey(), CIMStateConst.SP_Charater_Pound);
                    if (CimStringUtils.isNotEmpty(dTheSystemKeyPD)) {
                        dTheSystemKeyPD = dTheSystemKeyPD.substring(1, dTheSystemKeyPD.length());
                    }
                    //---------------------------------------//
                    //  Get information from active PD       //
                    //---------------------------------------//
                    aProcessDefinition = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class, dTheSystemKeyPD);
                    //---------------------------------------//
                    //  Get systmekey of current main PF     //
                    //---------------------------------------//
                    String dTheSystemKeyMainPF = CimStringUtils.strRrChr(aDurableProcessOperation.getMainProcessFlow().getPrimaryKey(), CIMStateConst.SP_Charater_Pound);
                    if (CimStringUtils.isNotEmpty(dTheSystemKeyMainPF)) {
                        dTheSystemKeyMainPF = dTheSystemKeyMainPF.substring(1, dTheSystemKeyMainPF.length());
                    }
                    //---------------------------------------------------------------//
                    //  Get seqno & stage of current module PD in current main PF    //
                    //---------------------------------------------------------------//
                    String sql3 = "SELECT OMPRF.PRP_ID,\n" +
                            "OMPRF_ROUTESEQ.IDX_NO,\n" +
                            "OMPRF_ROUTESEQ.STAGE_ID,\n" +
                            "OMPRF_ROUTESEQ.STAGE_RKEY\n" +
                            "FROM OMPRF, OMPRF_ROUTESEQ\n" +
                            "WHERE OMPRF.ID          = ?1 \n" +
                            "AND OMPRF_ROUTESEQ.REFKEY = OMPRF.ID\n" +
                            "AND OMPRF_ROUTESEQ.LINK_KEY  = ?2 ";
                    List<Object[]> querys = cimJpaRepository.query(sql3, dTheSystemKeyMainPF, aDurableProcessOperation.getModuleNumber());
                    pfAndPfPosListByPfIdAndDkey = querys.get(0);
                }
                //--- Set objectIdentifier routeID; ----------------------------------------//
                if (aReticlePod.getMainProcessDefinition() != null) {
                    strDurableOperationInfo.setRouteID(ObjectIdentifier.build(aReticlePod.getMainProcessDefinition().getIdentifier(), aReticlePod.getMainProcessDefinition().getPrimaryKey()));
                    //--- Set objectIdentifier startBankID; ------------------------------------//
                    String sql4 = "SELECT START_BANK_ID,\n" +
                            "START_BANK_RKEY\n" +
                            "FROM OMPRP\n" +
                            "WHERE PRP_ID = ?1 \n" +
                            "AND PRP_LEVEL = ?2 ";
                    List<Object[]> Start_Banks = cimJpaRepository.query(sql4, aReticlePod.getMainProcessDefinition().getIdentifier(), CIMStateConst.SP_PD_FLOWLEVEL_MAIN);
                    Object[] Start_Bank = Start_Banks.get(0);
                    strDurableOperationInfo.setStartBankID(ObjectIdentifier.build(CimObjectUtils.isEmpty(Start_Bank) ? null : (String) Start_Bank[0], CimObjectUtils.isEmpty(Start_Bank) ? null : (String) Start_Bank[1]));
                }


                //--- Set string operationNumber; ------------------------------------//
                strDurableOperationInfo.setOperationNumber(CimObjectUtils.isEmpty(aReticlePod) ? null : aReticlePod.getOperationNumber());
                if (CimStringUtils.equals(aReticlePod.getDurableInventoryState(), CIMStateConst.CIM_LOT_INVENTORY_STATE_ONFLOOR)) {
                    strDurableOperationInfo.getStrEquipmentList().get(0).setEquipmentID(ObjectIdentifier.build("", ""));
                    strDurableOperationInfo.getStrEquipmentList().get(0).setEquipmentName("");
                } else if (!CimStringUtils.equals(aReticlePod.getDurableProcessState(), CIMStateConst.SP_DURABLE_PROCESS_STATE_PROCESSING)) {
                    if (!CimObjectUtils.isEmpty(aDurableProcessOperation)) {
                        strDurableOperationInfo.getStrEquipmentList().get(0).setEquipmentID(ObjectIdentifier.build(aDurableProcessOperation.getAssignedMachine().getIdentifier(), aDurableProcessOperation.getAssignedMachine().getPrimaryKey()));
                        CimEquipmentDO equipment = cimJpaRepository.queryOne("SELECT DESCRIPTION FROM OMEQP WHERE ID = ?1", CimEquipmentDO.class, aDurableProcessOperation.getAssignedMachine().getPrimaryKey());

                        strDurableOperationInfo.getStrEquipmentList().get(0).setEquipmentName(equipment.getDescription());
                    }
                } else if (!CimStringUtils.equals(aReticlePod.getDurableHoldState(), CIMStateConst.SP_DURABLE_HOLD_STATE_ON_HOLD)) {
                    String sql5 = "SELECT OMRTCLPOD_HOLDEQP.EQP_ID,\n" +
                            "OMRTCLPOD_HOLDEQP.EQP_RKEY,\n" +
                            "OMEQP.DESCRIPTION\n" +
                            "FROM   OMRTCLPOD_HOLDEQP, OMEQP\n" +
                            "WHERE  OMRTCLPOD_HOLDEQP.REFKEY = ?1 \n" +
                            "AND  OMEQP.EQP_ID = OMRTCLPOD_HOLDEQP.EQP_ID";
                    List<Object[]> holdEqpAndEqpList = cimJpaRepository.query(sql5, aReticlePod.getPrimaryKey());

                    int count = 0;
                    for (Object[] objects : holdEqpAndEqpList) {
                        strDurableOperationInfo.getStrEquipmentList().get(count).setEquipmentID(ObjectIdentifier.build(objects[0].toString(), objects[1].toString()));
                        strDurableOperationInfo.getStrEquipmentList().get(count).setEquipmentName(objects[2].toString());
                        count++;
                    }
                    if (count == 0) {
                        String sql6 = "SELECT OMRTCLPOD_EQP.EQP_ID,\n" +
                                "OMRTCLPOD_EQP.EQP_RKEY,\n" +
                                "OMEQP.DESCRIPTION\n" +
                                "FROM OMRTCLPOD_EQP, OMEQP\n" +
                                "WHERE OMRTCLPOD_EQP.REFKEY = ? \n" +
                                "AND OMEQP.EQP_ID = OMRTCLPOD_EQP.EQP_ID ";
                        List<Object[]> reticlePodAndEqpList = cimJpaRepository.query(sql6, aReticlePod.getPrimaryKey());
                        Validations.check(CimArrayUtils.isEmpty(reticlePodAndEqpList), new ErrorCode("list is null !"));
                        for (Object[] objects : reticlePodAndEqpList) {
                            strDurableOperationInfo.getStrEquipmentList().get(count).setEquipmentID(ObjectIdentifier.build(objects[0].toString(), objects[1].toString()));
                            strDurableOperationInfo.getStrEquipmentList().get(count).setEquipmentName(objects[2].toString());
                            count++;
                        }
                    }
                } else if (!CimStringUtils.equals(aReticlePod.getDurableProcessState(), CIMStateConst.SP_DURABLE_PROCESS_STATE_WAITING)) {

                    String sql6 = "SELECT OMRTCLPOD_EQP.EQP_ID,\n" +
                            "OMRTCLPOD_EQP.EQP_RKEY,\n" +
                            "OMEQP.DESCRIPTION\n" +
                            "FROM OMRTCLPOD_EQP, OMEQP\n" +
                            "WHERE OMRTCLPOD_EQP.REFKEY = ? \n" +
                            "AND OMEQP.EQP_ID = OMRTCLPOD_EQP.EQP_ID ";
                    List<Object[]> reticlePodAndEqpList = cimJpaRepository.query(sql6, aReticlePod.getPrimaryKey());

                    int count = 0;
                    for (Object[] objects : reticlePodAndEqpList) {
                        strDurableOperationInfo.getStrEquipmentList().get(count).setEquipmentID(ObjectIdentifier.build(objects[0].toString(), objects[1].toString()));
                        strDurableOperationInfo.getStrEquipmentList().get(count).setEquipmentName(objects[2].toString());
                        count++;
                    }
                } else {
                    strDurableOperationInfo.getStrEquipmentList().get(0).setEquipmentID(ObjectIdentifier.emptyIdentifier());
                    strDurableOperationInfo.getStrEquipmentList().get(0).setEquipmentName("");
                }
                Timestamp queuedTimeStamp = aReticlePod.getProcessLagTime();
                if (!CimObjectUtils.isEmpty(queuedTimeStamp)) {
                    strDurableOperationInfo.setQueuedTimeStamp(aReticlePod.getProcessLagTime());
                } else {
                    strDurableOperationInfo.setQueuedTimeStamp(null);
                }
                if (!CimObjectUtils.isEmpty(aDurableProcessOperation)) {
                    String dTheSystemKeyPos = CimStringUtils.strRrChr(aDurableProcessOperation.getModuleProcessFlow().getPrimaryKey(), CIMStateConst.SP_Charater_Pound);
                    if (CimStringUtils.isNotEmpty(dTheSystemKeyPos)) {
                        dTheSystemKeyPos = dTheSystemKeyPos.substring(1, dTheSystemKeyPos.length());
                    }
                    String sql7 = "SELECT PRSS_RKEY\n" +
                            "FROM OMPRF_PRSSSEQ\n" +
                            "WHERE REFKEY = ?1 \n" +
                            "AND LINK_KEY    = ?2 ";
                    List<Object[]> processFlowPosList = cimJpaRepository.query(sql7, dTheSystemKeyPos, aDurableProcessOperation.getOperationNumber());
                    //--- Get process information from module POS

                    String dTheSystemKeyModulePos = CimStringUtils.strRrChr(hFRDRBLPOMODPOSOBJ, CIMStateConst.SP_Charater_Pound);
                    if (CimStringUtils.isNotEmpty(dTheSystemKeyModulePos)) {
                        dTheSystemKeyModulePos = dTheSystemKeyModulePos.substring(1, dTheSystemKeyModulePos.length());
                    }
                    com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification aProcessOperationSpecification = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification.class, dTheSystemKeyModulePos);
                    strDurableOperationInfo.setOperationID(active_pdID);
                    strDurableOperationInfo.setOperationName(aDurableProcessOperation.getOperationName());
                    strDurableOperationInfo.setDepartment(aProcessDefinition.getResponsibleDepartment());
                    strDurableOperationInfo.setStandardProcessTime(aProcessDefinition.getStandardProcessTime());
                    strDurableOperationInfo.setMandatoryOperationFlag(aProcessOperationSpecification.isMandatoryOperation());
                    strDurableOperationInfo.setStageID(ObjectIdentifier.build(pfAndPfPosListByPfIdAndDkey[2].toString(), pfAndPfPosListByPfIdAndDkey[3].toString()));
                    strDurableOperationInfo.setReworkCount(0L);

                    StringBuffer key = new StringBuffer();
                    ObjectIdentifier dummyID = new ObjectIdentifier();
                    Params.ProcessOperationListForDurableDRParams strProcessOperationListForDurableDRIn = new Params.ProcessOperationListForDurableDRParams();

                    strProcessOperationListForDurableDRIn.setDurableCategory(CIMStateConst.SP_DURABLE_CAT_RETICLE_POD);
                    strProcessOperationListForDurableDRIn.setDurableID(params.getReticlePodID());
                    strProcessOperationListForDurableDRIn.setSearchDirection(false);
                    strProcessOperationListForDurableDRIn.setPosSearchFlag(true);
                    strProcessOperationListForDurableDRIn.setCurrentFlag(false);
                    strProcessOperationListForDurableDRIn.setSearchCount(1);
                    strProcessOperationListForDurableDRIn.setSearchRouteID(dummyID);
                    strProcessOperationListForDurableDRIn.setSearchOperationNumber("");

                    //Step1 - process_OperationListForDurableDR()
                    List<Infos.DurableOperationNameAttributes> processOperationListForDurableDR = processMethod.processOperationListForDurableDR(objCommon, strProcessOperationListForDurableDRIn);
                    if (true) {
                        if (CimArrayUtils.isNotEmpty(processOperationListForDurableDR)) {
                            boolean firstOperationFlag = false;
                            if (CimStringUtils.equals(aReticlePod.getMainProcessDefinition().getIdentifier(), processOperationListForDurableDR.get(0).getRouteID().getValue())) {
                                String sql8 = "SELECT PRF_TYPE\n" +
                                        "FROM OMPRP\n" +
                                        "WHERE PRP_ID  = ?1 \n" +
                                        "AND PRP_LEVEL = ?2";
                                List<Object[]> FLOW_TYPES = cimJpaRepository.query(sql8, aReticlePod.getMainProcessDefinition().getIdentifier(), CIMStateConst.SP_PD_FLOWLEVEL_MAIN);
                                Object[] FLOW_TYPE = FLOW_TYPES.get(0);
                                if (!CimObjectUtils.isEmpty(FLOW_TYPE)) {
                                    if (!CimStringUtils.equals((String) FLOW_TYPE[0], CIMStateConst.SP_FLOWTYPE_SUB)) {
                                        firstOperationFlag = true;
                                    }
                                }
                            }
                            if (CimStringUtils.isNotEmpty(processOperationListForDurableDR.get(0).getRouteID().getValue()) &&
                                    CimStringUtils.isNotEmpty(processOperationListForDurableDR.get(0).getOperationNumber()) &&
                                    !firstOperationFlag) {
                                if (!CimObjectUtils.isEmpty(key)) {
                                    key = null;
                                }
                                key.append(processOperationListForDurableDR.get(0).getRouteID().getValue());
                                key.append(CIMStateConst.SEPARATOR_CHAR);
                                key.append(processOperationListForDurableDR.get(0).getOperationNumber());
                            }
                        }
                    }
                    /*-------------------------------------*/
                    /*   Get reworkCount                   */
                    /*-------------------------------------*/
                    // Generate key string.
                    if (key != null) {
                        //Get wafer Rework count.
                        String sql9 = "SELECT REWORK_COUNT\n" +
                                "FROM OMRTCLPOD_RWKCNT\n" +
                                "WHERE LINK_KEY = ?1 \n" +
                                "AND REFKEY = ?2 ";
                        List<Object[]> REWORK_COUNTS = cimJpaRepository.query(sql9, key.toString(), aReticlePod.getPrimaryKey());
                        Object[] REWORK_COUNT = REWORK_COUNTS.get(0);
                        if (!CimObjectUtils.isEmpty(REWORK_COUNT)) {
                            strDurableOperationInfo.setReworkCount(Long.parseLong((String) REWORK_COUNT[0]));
                        }
                    }
                    //--- Set objectIdentifier logicalRecipeID;-----------------------------------//
                    if (CimStringUtils.isNotEmpty(aReticlePod.getDurableControlJob().getIdentifier())) {
                        strDurableOperationInfo.setLogicalRecipeID(aDurableProcessOperation.findLogicalRecipeFor(null).getLogicalRecipeID());
                    } else {
                        ObjectIdentifier operationID = active_pdID;

                        //Step2 - process_defaultLogicalRecipe_GetDR
                        try {
                            ObjectIdentifier processDefaultLogicalRecipeGetDR = processMethod.processDefaultLogicalRecipeGetDR(objCommon, operationID);
                            strDurableOperationInfo.setLogicalRecipeID(processDefaultLogicalRecipeGetDR);
                        } catch (ServiceException ex) {
                            OmCode omCode = new OmCode(ex.getCode(), ex.getMessage());
                            if (Validations.isEquals(retCodeConfig.getNotFoundProcessDefinition(), omCode) &&
                                    Validations.isEquals(retCodeConfig.getNotFoundLogicalRecipe(), omCode)) {
                                throw ex;
                            }
                        }
                    }
                } else {
                    strDurableOperationInfo.setMandatoryOperationFlag(false);
                    strDurableOperationInfo.setReworkCount(0L);
                }
            }
            if (params.isDurableWipOperationInfoFlag()) {
                if (!aReticlePod.isResponsibleOperation()) {
                    strDurableWipOperationInfo.setResponsibleRouteID(strDurableOperationInfo.getRouteID());
                    strDurableWipOperationInfo.setResponsibleOperationID(strDurableOperationInfo.getOperationID());
                    strDurableWipOperationInfo.setResponsibleOperationNumber(strDurableOperationInfo.getOperationNumber());
                    strDurableWipOperationInfo.setResponsibleOperationName(strDurableOperationInfo.getOperationName());
                } else {
                    //-----------------------------------------------------------------------------------//
                    // Get Previous Process Operation                                                    //
                    //-----------------------------------------------------------------------------------//
                    String sql = "SELECT ID,\n" +
                            "IDX_NO\n" +
                            "FROM OMDRBLPRFCX_PROPESEQ\n" +
                            "WHERE PROPE_RKEY = ?";
                    List<Object[]> query = cimJpaRepository.query(sql, hFRDRBLPOPOOBJ);
                    if (!CimObjectUtils.isEmpty(query)) {
                        CimDurableProcessOperation aDurableProcessOperation = baseCoreFactory.getBO(CimDurableProcessOperation.class, hFRDRBLPOPOOBJ);
                        if (!CimObjectUtils.isEmpty(aDurableProcessOperation)) {
                            CimProcessDefinition mainProcessDefinition = aDurableProcessOperation.getMainProcessDefinition();
                            if (null != mainProcessDefinition) {
                                strDurableWipOperationInfo.setResponsibleRouteID(ObjectIdentifier.build(mainProcessDefinition.getIdentifier(), mainProcessDefinition.getPrimaryKey()));
                            }
                            strDurableWipOperationInfo.setResponsibleOperationID(ObjectIdentifier.build(aDurableProcessOperation.getProcessDefinition().getIdentifier(), aDurableProcessOperation.getProcessDefinition().getPrimaryKey()));
                            strDurableWipOperationInfo.setResponsibleOperationNumber(aDurableProcessOperation.getOperationNumber());
                            strDurableWipOperationInfo.setResponsibleOperationName(aDurableProcessOperation.getOperationName());
                        }
                    }
                }
            }

            //Step3 - reticlePod_reticleList_GetDR
            Outputs.ObjReticlePodReticleListGetDROut objReticlePodReticleListGetDROut = reticlePodReticleListGetDR(objCommon, params.getReticlePodID());
            List<Long> slotPosition = objReticlePodReticleListGetDROut.getSlotPosition();
            List<ObjectIdentifier> reticleID = objReticlePodReticleListGetDROut.getReticleID();
            int lenReticle = CimObjectUtils.isEmpty(reticleID) ? 0 : reticleID.size();
            int count = 0;
            for (int i = 0; i < lenReticle; i++) {
                if (CimStringUtils.isEmpty(reticleID.get(i).getValue())) {
                    continue;
                }
                Inputs.ObjReticleDetailInfoGetDRIn objReticleDetailInfoGetDRIn = new Inputs.ObjReticleDetailInfoGetDRIn();
                objReticleDetailInfoGetDRIn.setReticleID(reticleID.get(i));
                objReticleDetailInfoGetDRIn.setDurableWipOperationInfoFlag(false);
                objReticleDetailInfoGetDRIn.setDurableOperationInfoFlag(false);


                //Step4 - reticle_detailInfo_GetDR__170
                Results.ReticleDetailInfoInqResult reticleDetailInfoInqResult = reticleDetailInfoGetDR(objCommon, objReticleDetailInfoGetDRIn.getReticleID(), objReticleDetailInfoGetDRIn.isDurableOperationInfoFlag(), objReticleDetailInfoGetDRIn.isDurableWipOperationInfoFlag());
                Outputs.ObjReticleDetailInfoGetDROut objReticleDetailInfoGetDROut = new Outputs.ObjReticleDetailInfoGetDROut();
                objReticleDetailInfoGetDROut.setStrReticleDetailInfoInqResult(reticleDetailInfoInqResult);

                Infos.ContainedReticleInfo containedReticleInfo = new Infos.ContainedReticleInfo();
                containedReticleInfo.setSlotNo(slotPosition.get(i));
                containedReticleInfo.setReticleID(reticleID.get(i));
                containedReticleInfo.setStrReticleBrInfo(objReticleDetailInfoGetDROut.getStrReticleDetailInfoInqResult().getReticleBRInfo());
                strContainedReticleInfo.add(containedReticleInfo);
            }
            reticlePodStatusInfo.setStrContainedReticleInfo(strContainedReticleInfo);
            /*---------------------*/
            /* pptReticlePodPmInfo */
            /*---------------------*/
            Duration duration = Duration.between(aReticlePod.getLastMaintenanceTimeStamp() == null ? null : aReticlePod.getLastMaintenanceTimeStamp().toLocalDateTime(),
                    objCommon.getTimeStamp().getReportTimeStamp() == null ? null : objCommon.getTimeStamp().getReportTimeStamp().toLocalDateTime());

            long minutes = duration.toMinutes();
            reticlePodPMInfo.setPassageTimeFromLastPM(minutes);
            reticlePodPMInfo.setIntervalBetweenPM(aReticlePod.getIntervalBetweenPM());
            reticlePodPMInfo.setLastMaintenanceTimeStamp(aReticlePod.getLastMaintenanceTimeStamp().toString());
            if (!CimObjectUtils.isEmpty(aReticlePod.getLastMaintenancePerson())) {
                reticlePodPMInfo.setLastMaintenancePerson(ObjectIdentifier.build(aReticlePod.getLastMaintenancePerson().getIdentifier(), aReticlePod.getLastMaintenancePerson().getPrimaryKey()));
            }

            reticlePodLocationInfo.setInstanceName(aReticlePod.getInstanceName());
            reticlePodLocationInfo.setCurrentLocationFlag(Boolean.parseBoolean(aReticlePod.isCurrentLocationFlagOn().toString()));
            reticlePodLocationInfo.setBackupState(aReticlePod.getBackupState());

            objReticlePodFillInTxPDQ013DROut.setReticlePodID(params.getReticlePodID());
            objReticlePodFillInTxPDQ013DROut.setReticlePodBRInfo(reticlePodBRInfo);
            objReticlePodFillInTxPDQ013DROut.setReticlePodStatusInfo(reticlePodStatusInfo);
            objReticlePodFillInTxPDQ013DROut.setReticlePodPMInfo(reticlePodPMInfo);
            objReticlePodFillInTxPDQ013DROut.setReticlePodLocationInfo(reticlePodLocationInfo);
            objReticlePodFillInTxPDQ013DROut.setStrDurableOperationInfo(strDurableOperationInfo);
            objReticlePodFillInTxPDQ013DROut.setStrDurableWipOperationInfo(strDurableWipOperationInfo);
        }

        return objReticlePodFillInTxPDQ013DROut;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/7                           Wind
     *
     * @param objCommon
     * @param reticlePodID
     * @return RetCode<Infos.ReticlePodAdditionalAttribute>
     * @author Wind
     * @date 2018/11/7 18:19
     */
    @Override
    public Infos.ReticlePodAdditionalAttribute reticlePodReservedReticleInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {
        Infos.ReticlePodAdditionalAttribute reticlePodAdditionalAttribute = new Infos.ReticlePodAdditionalAttribute();
        List<Infos.ContainedReticleInfo> strReservedReticleInfo = new ArrayList<>();
        reticlePodAdditionalAttribute.setStrReservedReticleInfo(strReservedReticleInfo);

        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);
        Validations.check(CimObjectUtils.isEmpty(aReticlePod), "not find any reticlepod !");
        if (!CimObjectUtils.isEmpty(aReticlePod.getTransferDestinationEquipment())) {
            reticlePodAdditionalAttribute.setTransferDestEquipmentID(ObjectIdentifier.build(aReticlePod.getTransferDestinationEquipment().getIdentifier(), aReticlePod.getTransferDestinationEquipment().getPrimaryKey()));
            reticlePodAdditionalAttribute.setTransferReservedFlag(true);
        } else if (!CimObjectUtils.isEmpty(aReticlePod.getTransferDestinationStocker())) {
            reticlePodAdditionalAttribute.setTransferDestEquipmentID(ObjectIdentifier.build(aReticlePod.getTransferDestinationStocker().getIdentifier(), aReticlePod.getTransferDestinationStocker().getPrimaryKey()));
            reticlePodAdditionalAttribute.setTransferReservedFlag(true);
        } else {
            reticlePodAdditionalAttribute.setTransferReservedFlag(false);
        }
        //=========================================================================
        // Get resreved reticle info for this reticle pod slot
        //=========================================================================
        String sql = "SELECT RSV_RTCL_ID FROM OMRTCLPOD_RSVRTCL WHERE REFKEY = ?";
        List<Object[]> reticlePodsReservedReticleList = cimJpaRepository.query(sql, aReticlePod.getIdentifier());
        int currCount = 0;
        if (CimArrayUtils.isNotEmpty(reticlePodsReservedReticleList)) {
            for (Object[] reticlePodsReservedReticle : reticlePodsReservedReticleList) {

                Inputs.ObjReticleDetailInfoGetDRIn objReticleDetailInfoGetDRIn = new Inputs.ObjReticleDetailInfoGetDRIn();

                objReticleDetailInfoGetDRIn.setReticleID(ObjectIdentifier.buildWithValue((String) reticlePodsReservedReticle[0]));
                objReticleDetailInfoGetDRIn.setDurableOperationInfoFlag(false);
                objReticleDetailInfoGetDRIn.setDurableWipOperationInfoFlag(false);

                //reticle_detailInfo_GetDR__170
                Results.ReticleDetailInfoInqResult reticleDetailInfoInqResult = reticleDetailInfoGetDR(objCommon, objReticleDetailInfoGetDRIn.getReticleID(), objReticleDetailInfoGetDRIn.isDurableOperationInfoFlag(), objReticleDetailInfoGetDRIn.isDurableWipOperationInfoFlag());
                Infos.ContainedReticleInfo containedReticleInfo = new Infos.ContainedReticleInfo();
                containedReticleInfo.setSlotNo(currCount + 1L);
                containedReticleInfo.setStrReticleBrInfo(reticleDetailInfoInqResult.getReticleBRInfo());
                containedReticleInfo.setReticleID(reticleDetailInfoInqResult.getReticleID());
                strReservedReticleInfo.add(containedReticleInfo);
                currCount++;
            }
        }
        //----------------------------------------------------------
        // Normal end
        //----------------------------------------------------------

        return reticlePodAdditionalAttribute;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/8                          Wind
     *
     * @param objCommon
     * @param reticlePodID
     * @return RetCode<Outputs.ObjReticlePodReticleListGetDROut>
     * @author Wind
     * @date 2018/11/8 19:50
     */
    @Override
    public Outputs.ObjReticlePodReticleListGetDROut reticlePodReticleListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {
        Outputs.ObjReticlePodReticleListGetDROut objReticlePodReticleListGetDROut = new Outputs.ObjReticlePodReticleListGetDROut();

        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);
        /*--------------------------*/
        /*   Prepare Cursor Table   */
        /*--------------------------*/
        String sql = "SELECT  IDX_NO,\n" +
                "RTCL_ID,\n" +
                "RTCL_RKEY\n" +
                "FROM    OMRTCLPOD_RTCL\n" +
                "WHERE   REFKEY = ?1";
        List<Object[]> infos = cimJpaRepository.query(sql, aReticlePod.getPrimaryKey());
        /*------------------------*/
        /*   Fetch Cursor Table   */
        /*------------------------*/
        List<Long> slotPosition = new ArrayList<>();
        List<ObjectIdentifier> reticleID = new ArrayList<>();

        if (CimArrayUtils.isNotEmpty(infos)) {
            for (Object[] aReticle : infos) {
                if (CimObjectUtils.isEmpty(aReticle[1])) {
                    continue;
                }
                slotPosition.add(Long.parseLong(aReticle[0].toString()));
                reticleID.add(ObjectIdentifier.build((String) aReticle[1], (String) aReticle[2]));
            }
        }
        objReticlePodReticleListGetDROut.setSlotPosition(slotPosition);
        objReticlePodReticleListGetDROut.setReticleID(reticleID);

        return objReticlePodReticleListGetDROut;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/9                          Wind
     *
     * @param objCommon
     * @param stockerID
     * @return RetCode<Object>
     * @author Wind
     * @date 2018/11/9 16:04
     */
    @Override
    public void objectLock(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        //TODO-NOTIMPL:
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/9                          Wind
     *
     * @param objCommon
     * @param strObjectLockModeGetIn
     * @return RetCode<Outputs.ObjectLockModeGetOut>
     * @author Wind
     * @date 2018/11/9 16:22
     */
    @Override
    public Outputs.ObjectLockModeGetOut objectLockModeGet(Infos.ObjCommon objCommon, Inputs.ObjectLockModeGetIn strObjectLockModeGetIn) {
        //TODO-NOTIMPL:
        return null;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/9                          Wind
     *
     * @param objCommon
     * @param stockerID
     * @param equipmentID
     * @param transferStatus
     * @param transferStatusChangeTimeStamp
     * @param claimMemo
     * @return RetCode<Object>
     * @author Wind
     * @date 2018/11/9 16:37
     */
    @Override
    public void reticlePodTransferStateChange(Infos.ObjCommon objCommon,
                                              ObjectIdentifier stockerID,
                                              ObjectIdentifier equipmentID,
                                              ObjectIdentifier reticlePodID,
                                              String transferStatus,
                                              String transferStatusChangeTimeStamp,
                                              String claimMemo, Infos.ShelfPosition shelfPosition) {

        // Initialize rc

        /*------------------------*/
        /*   Get Machine Object   */
        /*------------------------*/
        CimMachine aMachine = baseCoreFactory.newBO(CimMachine.class);

        if (ObjectIdentifier.isNotEmptyWithValue(equipmentID)) {
            aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        }
        /*------------------------*/
        /*   Get stocker Object   */
        /*------------------------*/
        com.fa.cim.newcore.bo.machine.CimStorageMachine aStocker = baseCoreFactory.newBO(com.fa.cim.newcore.bo.machine.CimStorageMachine.class);

        if (ObjectIdentifier.isNotEmptyWithValue(stockerID)) {
            aStocker = baseCoreFactory.getBO(com.fa.cim.newcore.bo.machine.CimStorageMachine.class, stockerID);
        }
        /*----------------------------*/
        /*   Get Reticle Pod Object   */
        /*----------------------------*/
        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);
        Validations.check(CimObjectUtils.isEmpty(aReticlePod), new OmCode(retCodeConfig.getNotFoundReticlePod(), reticlePodID.getValue()));

        String location;
        /*------------------------------------*/
        /*   Check In-Parm's Validity Check   */
        /*------------------------------------*/
        if (CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_OUT)) {
            Validations.check(aMachine == null, retCodeConfig.getInvalidDataCombinAtion());
            location = equipmentID.getValue();
        } else {
            Validations.check(aStocker == null, retCodeConfig.getInvalidDataCombinAtion());
            location = stockerID.getValue() + CimObjectUtils.toString(shelfPosition); //for e-rack history, add by Nyx
        }
        if (CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_STATION_IN) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_STATION_OUT) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_BAY_IN) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_BAY_OUT) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_MANUAL_IN) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_MANUAL_OUT) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_OUT) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_SHELF_IN) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_SHELF_OUT) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_INTERMEDIATE_IN) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_INTERMEDIATE_OUT) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_ABNORMAL_IN) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_ABNORMAL_OUT)) {

        } else {
            Validations.check(true, retCodeConfig.getInvalidDataCombinAtion());
        }
        //=========================================================================
        // Get reserved destination (target) eqp
        //=========================================================================
        CimMachine aPosMachine = baseCoreFactory.newBO(CimMachine.class);

        String destinationMachine = null;
        if (!CimObjectUtils.isEmpty(aPosMachine)) {
            destinationMachine = aPosMachine.getIdentifier();
        }
        //=========================================================================
        // Clear reserved destination (target) machine if necessary
        //=========================================================================
        CimStorageMachine aPosStorageMachine = aReticlePod.getTransferDestinationStocker();
        String destinationStocker = null;
        if (!CimObjectUtils.isEmpty(aPosStorageMachine)) {
            destinationStocker = aPosStorageMachine.getIdentifier();
        }

        //=========================================================================
        // Clear reserved destination machine if necessary
        //=========================================================================
        if (ObjectIdentifier.equalsWithValue(equipmentID, destinationMachine)) {
            CimMachine aNilMachine = baseCoreFactory.newBO(CimMachine.class);
            CimPerson aNilPerson = baseCoreFactory.newBO(CimPerson.class);
            aReticlePod.setTransferDestinationEquipment(aNilMachine);
            aReticlePod.setTransferReserveUser(aNilPerson);
        } else if (ObjectIdentifier.equalsWithValue(stockerID, destinationStocker)) {
            CimStorageMachine aNilStorageMachine = baseCoreFactory.newBO(CimStorageMachine.class);
            CimPerson aNilPerson = baseCoreFactory.newBO(CimPerson.class);
            aReticlePod.setTransferDestinationStocker(aNilStorageMachine);
            aReticlePod.setTransferReserveUser(aNilPerson);
        }
        /*---------------------------------------*/
        /*   Check cassette's XferChgTimeStamp   */
        /*---------------------------------------*/
        String xferStatChgTimeStamp = aReticlePod.getTransferStatusChangedTimeStamp().toString();
        String recordTimeStamp;

        if (CimStringUtils.isEmpty(transferStatusChangeTimeStamp)) {
            recordTimeStamp = objCommon.getTimeStamp().getReportTimeStamp().toString();
        } else {
            recordTimeStamp = transferStatusChangeTimeStamp;
        }

        if (CimDateUtils.compare(recordTimeStamp, xferStatChgTimeStamp) < 0) {
            /*---------------------------------------------*/
            /*   D4100081  Create durable Change Event     */
            /*---------------------------------------------*/
            // durableXferStatusChangeEvent_Make()
            Inputs.DurableXferStatusChangeEventMakeParams params = new Inputs.DurableXferStatusChangeEventMakeParams();
            params.setTransactionID("");
            params.setDurableID(reticlePodID);
            params.setDurableType(BizConstant.SP_DURABLECAT_RETICLEPOD);
            params.setActionCode(BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE);
            params.setDurableStatus("");
            params.setXferStatus(transferStatus);
            params.setTransferStatusChangeTimeStamp(recordTimeStamp);
            params.setLocation(location);
            params.setClaimMemo(claimMemo);
            eventMethod.durableXferStatusChangeEventMake(objCommon, params);
        }
        aReticlePod.setLastClaimedTimeStamp(Timestamp.valueOf(recordTimeStamp));

        /*-----------------------------------------*/
        /*   Update reticlepod's Transfer Status   */
        /*-----------------------------------------*/
        aReticlePod.setTransferStatus(transferStatus);

        /*---------------------------*/
        /*   Set AssignedToMachine   */
        /*---------------------------*/
        if (CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN) ||
                CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_OUT)) {
            aReticlePod.assignToMachine(aMachine);
            aReticlePod.setShelfPosition(0, 0, 0); //for e-rack, add by nyx
        } else {
            aReticlePod.assignToMachine(aStocker);
            if (CimObjectUtils.isEmpty(shelfPosition)) {
                aReticlePod.setShelfPosition(0, 0, 0); //for e-rack, add by nyx
            } else {
                aReticlePod.setShelfPosition(shelfPosition.getShelfPositionX(), shelfPosition.getShelfPositionY(), shelfPosition.getShelfPositionZ()); //for e-rack, add by nyx
            }
        }
        /*---------------------------------------------*/
        /*   D4100081  Create durable Change Event     */
        /*---------------------------------------------*/
        Inputs.DurableXferStatusChangeEventMakeParams params = new Inputs.DurableXferStatusChangeEventMakeParams();
        params.setTransactionID("");
        params.setDurableID(reticlePodID);
        params.setDurableType(BizConstant.SP_DURABLECAT_RETICLEPOD);
        params.setActionCode(BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE);
        params.setDurableStatus("");
        params.setXferStatus(transferStatus);
        params.setTransferStatusChangeTimeStamp(recordTimeStamp);
        params.setLocation(location);
        params.setClaimMemo(claimMemo);
        eventMethod.durableXferStatusChangeEventMake(objCommon, params);

        /*-------------------------------------*/
        /*   Get Reticle List for reticlepod   */
        /*-------------------------------------*/
        Outputs.ObjReticlePodReticleListGetDROut objReticlePodReticleListGetDR = this.reticlePodReticleListGetDR(objCommon, reticlePodID);
        if (null != objReticlePodReticleListGetDR && CimArrayUtils.isNotEmpty(objReticlePodReticleListGetDR.getReticleID())){
            for (ObjectIdentifier reticleID : objReticlePodReticleListGetDR.getReticleID()) {
                if (ObjectIdentifier.isEmptyWithValue(reticleID)) {
                    continue;
                }
                /*------------------------*/
                /*   Get Reticle Object   */
                /*------------------------*/
                CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
                /*--------------------------------------*/
                /*   Get Reticle's Transfer Status   */
                /*--------------------------------------*/
                String strReticleTransferStatus = CimObjectUtils.isEmpty(aReticle) ? null : aReticle.getTransportState();
                /*--------------------------------------*/
                /*   Update Reticle's Transfer Status   */
                /*--------------------------------------*/
                aReticle.setTransportState(transferStatus);
                /*---------------------------*/
                /*   Set AssignedToMachine   */
                /*---------------------------*/
                if (CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN) ||
                        CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_OUT)) {
                    aReticle.assignToMachine(aMachine);
                    if(CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN)){
                        CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_EQP);
                        aReticle.setDurableSubState(aDurableSubState);
                    }else {
                        CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_IDLE);
                        aReticle.setDurableSubState(aDurableSubState);
                    }
                } else {
                    aReticle.assignToMachine(aStocker);
                }

                /*-----------------------------------------*/
                /*   Set TimeStamp and LastClaimedPerson   */
                /*-----------------------------------------*/
                aReticle.setTransferStatusChangedTimeStamp(Timestamp.valueOf(recordTimeStamp));

                aReticle.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

                com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());

                aReticle.setLastClaimedPerson(aPerson);
                /*-----------------------------------*/
                /*   Create Durable Change Event     */
                /*-----------------------------------*/
                Inputs.DurableXferStatusChangeEventMakeParams durableXferStatusChangeEventMakeParams = new Inputs.DurableXferStatusChangeEventMakeParams();
                durableXferStatusChangeEventMakeParams.setTransactionID("");
                durableXferStatusChangeEventMakeParams.setDurableID(reticleID);
                durableXferStatusChangeEventMakeParams.setDurableType(BizConstant.SP_DURABLECAT_RETICLE);
                durableXferStatusChangeEventMakeParams.setActionCode(BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE);
                durableXferStatusChangeEventMakeParams.setDurableStatus("");
                durableXferStatusChangeEventMakeParams.setXferStatus(transferStatus);
                durableXferStatusChangeEventMakeParams.setTransferStatusChangeTimeStamp(recordTimeStamp);
                durableXferStatusChangeEventMakeParams.setLocation(location);
                durableXferStatusChangeEventMakeParams.setClaimMemo(claimMemo);
                eventMethod.durableXferStatusChangeEventMake(objCommon, durableXferStatusChangeEventMakeParams);

                /*-----------------------------------*/
                /*   Newly add Xfer Change Event     */
                /*-----------------------------------*/
                Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                this.reticleEventInfoSet(reticleOperationEventMakeParams, reticleID);
                reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_XFERCHG);
                reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
                reticleOperationEventMakeParams.setEqpID(ObjectIdentifier.fetchValue(equipmentID));
                reticleOperationEventMakeParams.setStockerID(ObjectIdentifier.fetchValue(stockerID));
                eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);

                String strReticleState = aReticle.getDurableState();

                if (CimStringUtils.equals(strReticleState, CIMStateConst.CIM_DURABLE_SCRAPPED) ||
                        CimStringUtils.equals(strReticleState, CIMStateConst.CIM_DURABLE_NOTAVAILABLE) ||
                        CimStringUtils.equals(strReticleState, CIMStateConst.CIM_DURABLE_AVAILABLE)) {
                    //Reticle Status not Change...
                } else if (CimStringUtils.equals(strReticleTransferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN) &&
                        !CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN)) {

                    reticleStateChange(objCommon, reticleID, CIMStateConst.CIM_DURABLE_AVAILABLE);
                    /*---------------------------------------------*/
                    /*   D4100081  Create durable Change Event     */
                    /*---------------------------------------------*/
                    eventMethod.durableChangeEventMake(objCommon, TransactionIDEnum.RETICLE_POD_XFER_STAUES_CHANGE_RPT.getValue(),
                            reticleID, BizConstant.SP_DURABLECAT_RETICLE,
                            BizConstant.SP_DURABLEEVENT_ACTION_STATECHANGE, "");
                }
            }
        }

        aReticlePod.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        aReticlePod.setLastClaimedPerson(aPerson);
    }

    @Override
    public void reticleMaterialContainerChange(Infos.ObjCommon objCommon, Infos.ReticleSortInfo strReticleSortInfo, boolean bAddFlag, boolean bRemoveFlag) {

        //-----------------------------------
        // Retrieve request user's object reference
        //-----------------------------------
        log.info("Retrieve request user's object reference");
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), "*****"));

        //-----------------------------------
        // retrieve object reference of reticlePod
        //-----------------------------------
        log.info("retrieve object reference of reticlePod");

        boolean reticlePodFound = false;
        boolean reticleFound = false;
        CimReticlePod aNewReticlePodObj = baseCoreFactory.getBO(CimReticlePod.class, strReticleSortInfo.getDestinationReticlePodID());
        CimReticlePod aOldReticlePodObj = baseCoreFactory.getBO(CimReticlePod.class, strReticleSortInfo.getOriginalReticlePodID());
        CimProcessDurable aPosReticleObj = baseCoreFactory.getBO(CimProcessDurable.class, strReticleSortInfo.getReticleID());
        //----------------------------------
        // Check reticlePod Status
        //----------------------------------
        log.info("Check reticlePod Status");

        if (aNewReticlePodObj != null) {
            log.info("aNewReticlePodObj is not nil");

            boolean bReticlePodAvailable = aNewReticlePodObj.isAvailable();
            boolean bReticlePodInUse = aNewReticlePodObj.isAvailable();
            Validations.check(!bReticlePodAvailable && !bReticlePodInUse, new OmCode(retCodeConfig.getInvalidReticlepodStat(), "*****", aNewReticlePodObj.getIdentifier()));
        }

        //-----------------------------------
        // cancel relation between old reticlePod and reticle
        // By using this method, both of reticlePod-reticle relation
        // is canceled.
        //-----------------------------------
        log.info("cancel relation between old reticlePod and reticle");

        if (bRemoveFlag) {
            if (aOldReticlePodObj != null) {
                log.info("aOldReticlePodObj is not nil");
                Material tmpMaterial = aOldReticlePodObj.removeMaterialFromPosition(strReticleSortInfo.getOriginalSlotNumber());
            }
        }

        //-----------------------------------
        // Set reticle position in reticlePod
        // By using this method, both of reticlePod-reticle relation
        // is canceled.
        //-----------------------------------
        if (bAddFlag) {
            log.info("aPosReticleObj is not nil");

            if (aNewReticlePodObj != null) {
                log.info("aNewReticlePodObj is not nil");
                try {
                    aNewReticlePodObj.addMaterialAtPosition(aPosReticleObj, strReticleSortInfo.getDestinationSlotNumber());
                } catch (ServiceException se) {
                    if (se.getMessage().equals("SemPositionalContainer::addMaterialAtPosition(),PositionOccupiedSignal...")) {
                        Validations.check(true, new OmCode(retCodeConfig.getReticlepodSlotNotBlank(), String.valueOf(strReticleSortInfo.getDestinationSlotNumber()), strReticleSortInfo.getDestinationReticlePodID().getValue()));
                    }
                    throw se;
                }
            }
        }

        log.info("# set reticle last claimed timestamp");
        //--------------------------------------------------------------------
        // set reticle and retclePod, last claimed timestamp, last claimed person
        //--------------------------------------------------------------------

        // set for reticle
        if (aPosReticleObj != null) {
            log.info("aPosReticleObj is not nil");
            // set reticle last claimed timestamp
            aPosReticleObj.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            // set reticle last claimed person
            aPosReticleObj.setLastClaimedPerson(aPerson);
        }

        // set for original reticlePod
        if (aOldReticlePodObj != null) {
            log.info("aOldReticlePodObj is not nil");
            // set old reticePod last claimed timestamp
            aOldReticlePodObj.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            // set old reticlePod last claimed person
            aOldReticlePodObj.setLastClaimedPerson(aPerson);
        }

        // set for destination reticlePod
        if (aNewReticlePodObj != null) {
            log.info("aNewReticlePodObj is not nil");
            // set new reticlePod last claimed timestamp
            aNewReticlePodObj.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            // set new reticlePod last claimed person
            aNewReticlePodObj.setLastClaimedPerson(aPerson);
        }
    }

    @Override
    public void reticlePodTransferStateCheck(Infos.ObjCommon objCommon, ObjectIdentifier fromReticlePodID, ObjectIdentifier toReticlePodID, boolean fromReticlePodFlag, boolean toReticlePodFlag) {

        Machine aFromMachine, aToMachine;
        String strFromTransferStatus, strToTransferStatus, strFromMachineID = null, strToMachineID = null;

        if (fromReticlePodFlag) {
            log.info("Get and Check fromReticlePodID");

            CimReticlePod aFromReticlePod = baseCoreFactory.getBO(CimReticlePod.class, fromReticlePodID);
            Validations.check(aFromReticlePod == null, retCodeConfig.getNotFoundReticlePod());

            strFromTransferStatus = aFromReticlePod.getTransferStatus();
            if (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) ||
                    CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_SHELFIN)) {
                log.info("EquipmentIn or ShelfIn");

                aFromMachine = aFromReticlePod.currentAssignedMachine();
                Validations.check(aFromMachine == null, new OmCode(retCodeConfig.getReticleNotInTheEqp(), fromReticlePodID.getValue()));

                strFromMachineID = aFromMachine.getIdentifier();
            }
        } else {
            log.info("Get and Check fromReticleID");

            CimProcessDurable aFromReticle = baseCoreFactory.getBO(CimProcessDurable.class, fromReticlePodID);
            Validations.check(aFromReticle == null, retCodeConfig.getNotFoundReticlePod());

            strFromTransferStatus = aFromReticle.getTransportState();
            if (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) ||
                    CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_SHELFIN)) {
                log.info("EquipmentIn or ShelfIn");
                aFromMachine = aFromReticle.currentAssignedMachine();
                Validations.check(aFromMachine == null, new OmCode(retCodeConfig.getReticleNotInTheEqp(), fromReticlePodID.getValue()));

                strFromMachineID = aFromMachine.getIdentifier();
            }
        }

        if (toReticlePodFlag) {
            log.info("Get and Check toReticlePodID");

            CimReticlePod aToReticlePod = baseCoreFactory.getBO(CimReticlePod.class, toReticlePodID);
            Validations.check(aToReticlePod == null, retCodeConfig.getNotFoundReticlePod());

            strToTransferStatus = aToReticlePod.getTransferStatus();
            if (CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) ||
                    CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_SHELFIN)) {
                log.info("EquipmentIn or ShelfIn");

                aToMachine = aToReticlePod.currentAssignedMachine();
                Validations.check(aToMachine == null, new OmCode(retCodeConfig.getReticleNotInTheEqp(), toReticlePodID.getValue()));

                strToMachineID = aToMachine.getIdentifier();
            }
        } else {
            log.info("Get and Check toReticleID");

            CimProcessDurable aToReticle = baseCoreFactory.getBO(CimProcessDurable.class, toReticlePodID);
            Validations.check(aToReticle == null, retCodeConfig.getNotFoundReticle());

            strToTransferStatus = aToReticle.getTransportState();
            if (CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) ||
                    CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_SHELFIN)) {
                log.info("EquipmentIn or ShelfIn");
                aToMachine = aToReticle.currentAssignedMachine();
                Validations.check(aToMachine == null, new OmCode(retCodeConfig.getReticleNotInTheEqp(), toReticlePodID.getValue()));

                strToMachineID = aToMachine.getIdentifier();
            }
        }

        boolean bTransferStatusError = false;
        if (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
            log.info("EquipmentIn");
            if (!CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN))
                bTransferStatusError = TRUE;
            if (!CimStringUtils.equals(strFromMachineID, strToMachineID))
                bTransferStatusError = TRUE;
        } else if ((CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_ABNORMALOUT)) ||
                (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) ||
                (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_SHELFOUT)) ||
                (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_MANUALOUT)) ||
                (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_STATIONOUT)) ||
                (CimStringUtils.equals(strFromTransferStatus, BizConstant.HYPHEN))) {
            log.info("AbnormalOut, EquipmentOut, ShelfOut, ManualOut, StationOut");
            if ((!CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_ABNORMALOUT)) &&
                    (!CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) &&
                    (!CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_SHELFOUT)) &&
                    (!CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_MANUALOUT)) &&
                    (!CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_STATIONOUT)) &&
                    (!CimStringUtils.equals(strToTransferStatus, BizConstant.HYPHEN)))
                bTransferStatusError = TRUE;
        } else if (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_SHELFIN)) {
            log.info("ShelfIn");
            if (!CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_SHELFIN))
                bTransferStatusError = TRUE;
            if (!CimStringUtils.equals(strFromMachineID, strToMachineID))
                bTransferStatusError = TRUE;
        } else if ((CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_STATIONIN)) ||
                (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_BAYIN)) ||
                (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_MANUALIN)) ||
                (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_ABNORMALIN))) {
            log.info("StationIn, BayIn, ManualIn, AbnormalIn");
            if ((!CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_STATIONIN)) &&
                    (!CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_BAYIN)) &&
                    (!CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_MANUALIN)) &&
                    (!CimStringUtils.equals(strToTransferStatus, BizConstant.SP_TRANSSTATE_ABNORMALIN)))
                bTransferStatusError = TRUE;
            if (!CimStringUtils.equals(strFromMachineID, strToMachineID))
                bTransferStatusError = TRUE;
        } else if (CimStringUtils.equals(strFromTransferStatus, BizConstant.SP_TRANSSTATE_BAYOUT)) {
            log.info("BayOut");
            bTransferStatusError = TRUE;
        } else {
            log.info("other strFromTransferStatus {}", strFromTransferStatus);
            bTransferStatusError = TRUE;
        }

        Validations.check(bTransferStatusError, toReticlePodFlag ? new OmCode(retCodeConfig.getInvalidReticlepodXferStat(), toReticlePodID.getValue(), strToTransferStatus)
                : new OmCode(retCodeConfig.getInvalidReticleXferStat(), toReticlePodID.getValue(), strToTransferStatus));
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param list
     * @param key
     * @param value
     * @return boolean
     * @author Ho
     * @date 2018/10/18 18:25:02
     */
    private Pair<Boolean, Object> find(List<Object[]> list, Object key, Object value) {

        for (Object[] item : list) {
            if (key != null && key.equals(item[0])) {
                value = item[1];
                return Pair.of(true, value);
            }
        }
        return Pair.of(false, value);
    }

    @Override
    public Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut reticlePodPortResourceCurrentAccessModeGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier reticlePodPortID) {
        Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut objReticlePodPortResourceCurrentAccessModeGetOut = new Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut();
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        CimReticlePodPortResource aReticlePodPortResource = baseCoreFactory.getBO(CimReticlePodPortResource.class, reticlePodPortID);
        //-------------------------------------
        // Set output structure
        //-------------------------------------
        objReticlePodPortResourceCurrentAccessModeGetOut.setEquipmentID(new ObjectIdentifier(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
        objReticlePodPortResourceCurrentAccessModeGetOut.setReticlePodPortID(new ObjectIdentifier(aReticlePodPortResource.getIdentifier(), aReticlePodPortResource.getPrimaryKey()));
        //-------------------------------------------
        // Get accessMode of ReticlePodPortResource
        //-------------------------------------------
        String accessMode = aReticlePodPortResource.getAccessMode();
        //-------------------------------------
        // Set output structure
        //-------------------------------------
        objReticlePodPortResourceCurrentAccessModeGetOut.setAccessMode(accessMode);
        return objReticlePodPortResourceCurrentAccessModeGetOut;
    }

    @Override
    public String reticlePodWhereConditionMake(Infos.ObjCommon objCommon, String conditionString) {
        boolean bWildCard = false;
        long lenCond = conditionString.length();
        String pTmpCondition;
        String pWhereCondition;

        /*---------------------*/
        /*   Check Wild Card   */
        /*---------------------*/
        if (conditionString.indexOf("%") != -1) {
            bWildCard = true;
        }
        /*---------------------------*/
        /*   Make Wild Card String   */
        /*---------------------------*/
        if (bWildCard) {
            pTmpCondition = conditionString;
            pWhereCondition = String.format("LIKE '%s'", pTmpCondition);
        } else {
            pTmpCondition = conditionString;
            pWhereCondition = String.format("= '%s'", pTmpCondition);
        }

        return pWhereCondition;
    }

    @Override
    public Outputs.ObjReticlePodWildCardStringCheckOut reticlePodWildCardStringCheck(Infos.ObjCommon objCommon, String conditionString, String dataString) {
        Outputs.ObjReticlePodWildCardStringCheckOut objReticlePodWildCardStringCheckOut = new Outputs.ObjReticlePodWildCardStringCheckOut();
        boolean bCheckFlag = false;
        boolean bWildCard = false;
        int lenCond = conditionString.length();
        String pTmpCondition;
        int i;

        /*---------------------*/
        /*   Check Wild Card   */
        /*---------------------*/
        if (conditionString.indexOf("%") != -1) {
            bWildCard = true;
        }
        /*---------------------------*/
        /*   Make Wild Card String   */
        /*---------------------------*/
        if (bWildCard) {
            int condStartIdx = 0;
            String dataStartPtr = dataString;
            boolean bAbortFlag = false;
            for (i = 0; i <= lenCond; i++) {
                if (conditionString.indexOf("%") == i || conditionString.indexOf("\0") == i) {
                    int lenBuf = i - condStartIdx;
                    pTmpCondition = (conditionString + condStartIdx).substring(0, lenBuf);
                    StringBuffer stringBuffer = new StringBuffer(pTmpCondition);
                    pTmpCondition = stringBuffer.replace(lenBuf, lenBuf, "\0").toString();
                    /*------------------*/
                    /* Check Top String */
                    /*------------------*/
                    if (condStartIdx == 0) {
                        condStartIdx = i + 1;
                        if (lenBuf != 0) {
                            if (!CimStringUtils.equals(dataStartPtr.substring(0, lenBuf), pTmpCondition.substring(0, lenBuf))) {
                                bAbortFlag = true;
                                bCheckFlag = false;
                                break;
                            }
                        }
                    } else {
                        condStartIdx = i + 1;
                        if (dataStartPtr.indexOf(pTmpCondition) != -1) {
                            dataStartPtr = pTmpCondition;
                        } else {
                            dataStartPtr = null;
                        }
                        if (dataStartPtr == null) {
                            bAbortFlag = true;
                            bCheckFlag = false;
                            break;
                        }
                        /*---------------------*/
                        /* Check Buttom String */
                        /*---------------------*/
                        if (conditionString.indexOf("\0") == i) {
                            if (lenBuf != 0) {
                                if (!CimStringUtils.equals(dataStartPtr, pTmpCondition)) {
                                    bAbortFlag = true;
                                    bCheckFlag = false;
                                    break;
                                }
                            }
                        }
                    }
                    dataStartPtr = dataStartPtr + lenBuf;
                }
            }
            if (CimBooleanUtils.isFalse(bAbortFlag)) {
                bCheckFlag = true;
            }
        } else {
            if (CimStringUtils.equals(conditionString, dataString)) {
                bCheckFlag = true;
            } else {
                bCheckFlag = false;
            }
        }
        objReticlePodWildCardStringCheckOut.setCheckFlag(bCheckFlag);
        return objReticlePodWildCardStringCheckOut;
    }

    @Override
    public void reticleUsageCountIncrement(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {

        CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        Validations.check(null == aReticle, new OmCode(retCodeConfig.getNotFoundReticle(), reticleID.getValue()));
        aReticle.incrementTimesUsed();
    }

    /**
     * @param objCommon objCommon
     * @param reticleID reticleID
     * @return
     * @author ho
     */
    @Override
    public void reticleUsageCountDecrement(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        /*-------------------*/
        /*   Get timesUsed   */
        /*-------------------*/
        Long timesUsed = aReticle.timesUsed();
        if (timesUsed > 0) {
            /*-------------------*/
            /*   theTimesUsed--  */
            /*-------------------*/
            aReticle.decrementTimesUsed();
        }
    }

    @Override
    public Outputs.ObjReticleUsageLimitationCheckOut reticleUsageLimitationCheck(Infos.ObjCommon objCommon, ObjectIdentifier reticleId) {
        Outputs.ObjReticleUsageLimitationCheckOut resultObject = new Outputs.ObjReticleUsageLimitationCheckOut();

        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        resultObject.setUsageLimitOverFlag(false);
        resultObject.setRunTimeOverFlag(false);
        resultObject.setStartCountOverFlag(false);
        resultObject.setPmTimeOverFlag(false);

        CimProcessDurable reticle = baseCoreFactory
                .getBO(CimProcessDurable.class, reticleId);
        Validations.check(reticle == null, new OmCode(retCodeConfig.getNotFoundReticle(), reticleId.getValue()));
        resultObject.setReticleId(reticleId);

        /*--------------------------*/
        /*   Get Usage Check Flag   */
        /*--------------------------*/
        boolean usageCheckRequiredFlag = reticle.isUsageCheckRequired();
        if (!usageCheckRequiredFlag) {
            return resultObject;
        }

        /*--------------------------*/
        /*   Get Usage Limitation   */
        /*--------------------------*/
        resultObject.setMaxRunTime((long) (reticle.getDurationLimit() / (60 * 1000)));
        resultObject.setStartCount(reticle.timesUsed());
        resultObject.setMaxStartCount(reticle.getTimesUsedLimit());
        resultObject.setIntervalBetweenPM(reticle.getIntervalBetweenPM());

        if (resultObject.getMaxRunTime() == 0 && resultObject.getMaxStartCount() == 0 && resultObject.getIntervalBetweenPM() == 0) {
            return resultObject;
        }

        /*-------------------------------------*/
        /*   Calcurate Run Time from Last PM   */
        /*-------------------------------------*/
        resultObject.setRunTime(0);
        resultObject.setPassageTimeFromLastPM(reticle.getLastMaintenanceTimeStamp().getTime() / (1000 * 60));       //minutes

        /*--------------------------*/
        /*   Set Flag Information   */
        /*--------------------------*/
        if (resultObject.getStartCount() >= resultObject.getMaxStartCount()) {
            resultObject.setUsageLimitOverFlag(true);
            resultObject.setStartCountOverFlag(true);
        }

        if (resultObject.getPassageTimeFromLastPM() >= resultObject.getIntervalBetweenPM()) {
            resultObject.setUsageLimitOverFlag(true);
            resultObject.setPmTimeOverFlag(true);
        }

        /*-------------------------*/
        /*   Create Message Text   */
        /*-------------------------*/
        if (resultObject.isUsageLimitOverFlag()) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<<< Reticle Usage Limitation Over >>>")
                    .append("\n    Reticle ID            : ").append(ObjectIdentifier.fetchValue(reticleId))
                    .append("\n    Start Count           : ").append(resultObject.getStartCount())
                    .append("\n    Max Start Count       : ").append(resultObject.getMaxStartCount())
                    .append("\n    Passage from PM (min) : ").append(resultObject.getPassageTimeFromLastPM())
                    .append("\n    Interval for PM (min) : ").append(resultObject.getIntervalBetweenPM());
            resultObject.setMessageText(stringBuffer.toString());
        }

        return resultObject;
    }

    @Override
    public void reticleLastUsedTimeSet(ObjectIdentifier reticleId, Timestamp lastUsedTime) {
        CimProcessDurable reticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleId);
        Validations.check(reticle == null, new OmCode(retCodeConfig.getNotFoundReticle(), reticleId.getValue()));
        reticle.setLastUsedTimeStamp(lastUsedTime);
    }

    @Override
    public void reticleLastUsedTimeStampUpdate(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassettes) {
        startCassettes.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                .forEach(lotInCassette -> {
                    /*-------------------------------*/
                    /*   Get Used Reticles for lot   */
                    /*-------------------------------*/
                    List<Infos.StartReticle> startReticles =
                            processMethod.processAssignedReticleGet(objCommon, lotInCassette.getLotID());
                    startReticles.forEach(startReticle -> {
                        Outputs.ObjReticleUsageLimitationCheckOut usageLimitationCheckOut =
                                this.reticleUsageLimitationCheck(objCommon, startReticle.getReticleID());

                        if (usageLimitationCheckOut.isUsageLimitOverFlag()) {
                            log.info("strFixture_usageLimitation_Check_out.usageLimitOverFlag == TRUE");
                            /*-------------------------*/
                            /*   Call System Message   */
                            /*-------------------------*/
                            //TODO: txAlertMessageRpt
                        }

                        /*---------------------------------------------*/
                        /*  Set last used time stamp for used reticle  */
                        /*---------------------------------------------*/
                        //reticle_lastUsedTime_Set
                        Timestamp reportTime = objCommon.getTimeStamp() == null ? CimDateUtils.getCurrentTimeStamp() :
                                objCommon.getTimeStamp().getReportTimeStamp();
                        reticleLastUsedTimeSet(startReticle.getReticleID(), reportTime);
                    });
                });
    }

    @Override
    public void reticlePodStatusChange(Infos.ObjCommon objCommon, String reticlePodStatus, ObjectIdentifier reticlePodID) {
        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);
        Boolean flag = false;
        Map<String, Runnable> reticlePodStatusMapping = new HashMap<>();

        reticlePodStatusMapping.put(BizConstant.CIMFW_DURABLE_AVAILABLE, () -> {
            Validations.check(aReticlePod.isAvailable(), retCodeConfig.getSameReticleStat());
            aReticlePod.makeAvailable();
        });
        reticlePodStatusMapping.put(BizConstant.CIMFW_DURABLE_INUSE, () -> {
            Validations.check(aReticlePod.isInUse(), retCodeConfig.getSameReticleStat());
            aReticlePod.makeInUse();
        });
        reticlePodStatusMapping.put(BizConstant.CIMFW_DURABLE_NOTAVAILABLE, () -> {
            Validations.check(aReticlePod.isNotAvailable(), retCodeConfig.getSameReticleStat());
            aReticlePod.makeNotAvailable();
        });
        reticlePodStatusMapping.put(BizConstant.CIMFW_DURABLE_SCRAPPED, () -> {
            Validations.check(aReticlePod.isScrapped(), retCodeConfig.getSameReticleStat());
            aReticlePod.makeScrapped();
        });

        if (reticlePodStatusMapping.containsKey(reticlePodStatus)) {
            Runnable processBody = reticlePodStatusMapping.get(reticlePodStatus);
            processBody.run();
        } else {
            Validations.check(true, retCodeConfig.getInvalidReticleStat());
        }
        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));

        aReticlePod.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aReticlePod.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aReticlePod.setLastClaimedPerson(aPerson);
        aReticlePod.setStateChangedPerson(aPerson);
    }

    @Override
    public void justInOutReticleTransferInfoVerify(Infos.ObjCommon objCommon, String moveDirection, ObjectIdentifier reticlePodID, List<Infos.MoveReticles> moveReticlesList) {
        //Check moveDirection
        boolean isChecked = !BizConstant.SP_MOVEDIRECTION_JUSTIN.equals(moveDirection) && !BizConstant.SP_MOVEDIRECTION_JUSTOUT.equals(moveDirection);
        Validations.check(isChecked, retCodeConfig.getInvalidInputParam());

        //Check Condition of reticlePod
        CimReticlePod anInputReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);
        Validations.check(anInputReticlePod == null, retCodeConfig.getNotFoundReticlePod());

        Validations.check(CimArrayUtils.isEmpty(moveReticlesList), retCodeConfig.getInvalidInputParam());

        for (int i = 0; i < moveReticlesList.size(); i++) {
            Infos.MoveReticles moveReticles = moveReticlesList.get(i);

            //Check Condition of reticle
            Validations.check(CimStringUtils.isEmpty(moveReticles.getReticleID().getValue()), retCodeConfig.getReticleIdPod());

            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, moveReticles.getReticleID());
            Validations.check(aReticle == null, retCodeConfig.getNotFoundReticle());

            //Check Condition of slotNumber
            int positionTotal = anInputReticlePod.getPositionTotal();

            Validations.check(moveReticles.getSlotNumber() > positionTotal || moveReticles.getSlotNumber() < 0, new OmCode(retCodeConfig.getInvalidReticlePodPosition(), moveReticles.getReticleID().getValue(), moveReticles.getSlotNumber().toString()));

            if (BizConstant.SP_MOVEDIRECTION_JUSTIN.equals(moveDirection)) {
                //Check reticle or reticlePod transferState
                this.reticlePodTransferStateCheck(objCommon, reticlePodID, moveReticles.getReticleID(), true, false);

                //Check reticle's slot is empty or not
                MaterialContainer aMtrlCntnr = aReticle.getMaterialContainer();

                CimReticlePod curReticlePod = (CimReticlePod) aMtrlCntnr;

                Validations.check(curReticlePod != null, new OmCode(retCodeConfig.getSameReticlepodStat(), moveReticlesList.get(i).getReticleID().getValue(), (curReticlePod == null) ? null : curReticlePod.getIdentifier()));

                List<Integer> nEmptyPositions = anInputReticlePod.emptyPositions();

                boolean bSlotCheckFlag = false;
                if (!CimObjectUtils.isEmpty(nEmptyPositions)) {
                    for (Integer nEmptySlotNumber : nEmptyPositions) {
                        if (Objects.equals(moveReticlesList.get(i).getSlotNumber(), nEmptySlotNumber)) {
                            bSlotCheckFlag = true;
                            break;
                        }
                    }
                }
                Validations.check(!bSlotCheckFlag, new OmCode(retCodeConfig.getNotEmptyReticlePodPosition(), reticlePodID.getValue(), moveReticlesList.get(i).getSlotNumber().toString()));
            } else if (BizConstant.SP_MOVEDIRECTION_JUSTOUT.equals(moveDirection)) {

                List<Material> aMaterialList = anInputReticlePod.containedMaterial();

                boolean bReticleCheckFlag = false;
                if (!CimObjectUtils.isEmpty(aMaterialList)) {
                    for (Material material : aMaterialList) {
                        if (ObjectIdentifier.equalsWithValue(moveReticlesList.get(i).getReticleID(), material.getIdentifier())) {
                            bReticleCheckFlag = true;
                            break;
                        }
                    }
                }
                Validations.check(!bReticleCheckFlag, new OmCode(retCodeConfig.getInvalidReticlePodPosition(), moveReticlesList.get(i).getReticleID().getValue(), moveReticlesList.get(i).getSlotNumber().toString()));

                Material aMaterialTemp = anInputReticlePod.contentsOfPosition(moveReticlesList.get(i).getSlotNumber());

                if (aMaterialTemp != null) {
                    Validations.check(!CimStringUtils.equals(moveReticlesList.get(i).getReticleID().getValue(), aMaterialTemp.getIdentifier()),
                            new OmCode(retCodeConfig.getInvalidReticlePodPosition(), moveReticlesList.get(i).getReticleID().getValue(), moveReticlesList.get(i).getSlotNumber().toString()));
                } else {
                    Validations.check(true, new OmCode(retCodeConfig.getInvalidReticlePodPosition(), moveReticlesList.get(i).getReticleID().getValue(), moveReticlesList.get(i).getSlotNumber().toString()));
                }
            }
        }
    }

    @Override
    public void reticleMaterialContainerJustIn(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, ObjectIdentifier reticleID, int slotNumber) {
        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));

        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);

        CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        Validations.check(aReticle == null, retCodeConfig.getNotFoundReticle());

        if (aReticlePod != null) {
            boolean reticlePodAvailable = aReticlePod.isAvailable();
            boolean reticlePodInUse = aReticlePod.isInUse();
            Validations.check(!reticlePodAvailable && !reticlePodInUse, new OmCode(retCodeConfig.getInvalidReticlepodStat(), aReticlePod.getDurableState(), reticlePodID.getValue()));
        }

        if (aReticlePod != null) {
            aReticlePod.addMaterialAtPosition(aReticle, slotNumber);
        }

        if (aReticle != null) {
            aReticle.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aReticle.setLastClaimedPerson(aPerson);
        }

        if (aReticlePod != null) {
            aReticlePod.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aReticlePod.setLastClaimedPerson(aPerson);
        }

    }

    @Override
    public void reticleMaterialContainerJustOut(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, ObjectIdentifier reticleID, int slotNumber) {

        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));

        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);

        CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        Validations.check(aReticle == null, retCodeConfig.getNotFoundReticle());

        if (aReticlePod != null) {
            boolean reticlePodAvailable = aReticlePod.isAvailable();
            boolean reticlePodInUse = aReticlePod.isInUse();
            Validations.check(!reticlePodAvailable && !reticlePodInUse, retCodeConfig.getInvalidReticleStat());
        }

        if (aReticlePod != null) {
            aReticlePod.removeMaterialFromPosition(slotNumber);
        }

        if (aReticle != null) {
            aReticle.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aReticle.setLastClaimedPerson(aPerson);
        }

        if (aReticlePod != null) {
            aReticlePod.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aReticlePod.setLastClaimedPerson(aPerson);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param reticlePodID
     * @return com.fa.cim.dto.Infos.ReticlePodCurrentMachineGetOut
     * @throws
     * @author ho
     * @date 2020/3/19 13:54
     */
    @Override
    public Infos.ReticlePodCurrentMachineGetOut reticlePodCurrentMachineGet(Infos.ObjCommon strObjCommonIn, ObjectIdentifier reticlePodID) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/

        // --------------------------------------------------------------
        // Get currentMachineID. (current assign Machine for reticlePodID)
        // --------------------------------------------------------------
        Boolean reticlePodFound = FALSE;
        CimReticlePod aReticlePod;
        aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);

        Validations.check(aReticlePod == null, retCodeConfig.getNotFoundReticlePod(), reticlePodID.getValue());

        reticlePodFound = TRUE;
        Machine aMachine;
        aMachine = aReticlePod.currentAssignedMachine();

        ObjectIdentifier currentMachineID = new ObjectIdentifier();
        ObjectIdentifier currentReticlePodPortID = null;

        if (aMachine != null) {
            String strMachineID;
            strMachineID = aMachine.getIdentifier();

            currentMachineID.setValue(strMachineID);

            // --------------------------------------------------------------
            // Get currentReticlePodPortID
            // --------------------------------------------------------------
            Outputs.ObjMachineTypeGetOut strMachinetypeGetout;

            // machine_type_Get
            strMachinetypeGetout = equipmentMethod.machineTypeGet(strObjCommonIn, currentMachineID);

            List<Infos.ReticlePodPortInfo> tmpReticlePodPortInfoSeq = null;
            if (!strMachinetypeGetout.isBStorageMachineFlag()) {
                Outputs.ObjEquipmentReticlePodPortInfoGetDROut strEquipmentreticlePodPortInfoGetDRout;

                // equipment_reticlePodPortInfo_GetDR
                strEquipmentreticlePodPortInfoGetDRout = equipmentMethod.equipmentReticlePodPortInfoGetDR(
                        strObjCommonIn,
                        currentMachineID);

                tmpReticlePodPortInfoSeq = strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList();
            } else {
                if (!CimStringUtils.equals(strMachinetypeGetout.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD) &&
                        !CimStringUtils.equals(strMachinetypeGetout.getStockerType(), BizConstant.SP_STOCKER_TYPE_INTERBAY) &&
                        !CimStringUtils.equals(strMachinetypeGetout.getStockerType(), BizConstant.SP_STOCKER_TYPE_INTRABAY)) {
                    List<Infos.ReticlePodPortInfo> strStockerreticlePodPortInfoGetDRout;

                    // stocker_reticlePodPortInfo_GetDR
                    strStockerreticlePodPortInfoGetDRout = stockerMethod.stockerReticlePodPortInfoGetDR(
                            strObjCommonIn,
                            currentMachineID);

                    tmpReticlePodPortInfoSeq = strStockerreticlePodPortInfoGetDRout;
                }
            }

            // Search reticlePodPortID
            int k;
            int nKLen = CimArrayUtils.getSize(tmpReticlePodPortInfoSeq);
            for (k = 0; k < nKLen; k++) {
                if (CimStringUtils.equals(tmpReticlePodPortInfoSeq.get(k).getLoadedReticlePodID().getValue(),
                        reticlePodID.getValue())) {
                    currentReticlePodPortID = tmpReticlePodPortInfoSeq.get(k).getReticlePodPortID();
                    break;
                }
            }
        }

        // Set Return Structure
        Infos.ReticlePodCurrentMachineGetOut strReticlePodcurrentMachineGetout = new Infos.ReticlePodCurrentMachineGetOut();
        strReticlePodcurrentMachineGetout.setCurrentMachineID(currentMachineID);
        strReticlePodcurrentMachineGetout.setCurrentReticlePodPortID(currentReticlePodPortID);

        /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/
        return strReticlePodcurrentMachineGetout;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param stockerID
     * @param strInventoryReticlePodInfo
     * @return java.util.List<com.fa.cim.dto.Infos.InventoriedReticlePodInfo>
     * @throws
     * @author ho
     * @date 2020/3/20 10:40
     */
    public List<Results.InventoriedReticlePodInfo> reticlePodPositionUpdateByStockerInventoryDR(
            Infos.ObjCommon strObjCommonIn,
            ObjectIdentifier stockerID,
            List<Infos.InventoryReticlePodInfo> strInventoryReticlePodInfo,
            Infos.ShelfPosition shelfPosition) {
        int i, j;

        int txSeqLen = CimArrayUtils.getSize(strInventoryReticlePodInfo);
        int setSeqLen = 0;
        int errorCount = 0;

        List<Results.InventoriedReticlePodInfo> strReticlePodPositionUpdateByStockerInventoryDROut;

        strReticlePodPositionUpdateByStockerInventoryDROut = new ArrayList<>(setSeqLen);


        for (i = 0; i < txSeqLen; i++) {
        }

        CimStorageMachine aStocker;
        aStocker = baseCoreFactory.getBO(CimStorageMachine.class,
                stockerID);

        CimPerson aPerson;
        aPerson = baseCoreFactory.getBO(CimPerson.class,
                strObjCommonIn.getUser().getUserID());

        /*-------------------------------------------------*/
        /*   Prepare Cursor Table to get PPT Information   */
        /*-------------------------------------------------*/
        String HV_BUFFER = "SELECT RTCLPOD_ID, ID FROM OMRTCLPOD";
        int paramLen = CimStringUtils.length(stockerID.getValue()) + CimStringUtils.length(BizConstant.SP_TRANSSTATE_MANUALIN) +
                CimStringUtils.length(BizConstant.SP_TRANSSTATE_STATIONIN) + CimStringUtils.length(BizConstant.SP_TRANSSTATE_BAYIN) +
                CimStringUtils.length(BizConstant.SP_TRANSSTATE_SHELFIN) + CimStringUtils.length(BizConstant.SP_TRANSSTATE_ABNORMALIN);
        if (2048 <= paramLen + 60) {
            Validations.check(true, retCodeConfig.getError());
        }
        // !!! CAUTION !!!
        //   The number of character which is used in format part of the following sprintf is 60. (except for %s)
        //   If the format is changed, the number has to be changed at the above if-sentence.
        String HV_TMPBUFFER = String.format(" WHERE EQP_ID = '%s' AND XFER_STATE in ( '%s', '%s', '%s', '%s', '%s' )",
                stockerID.getValue(),
                BizConstant.SP_TRANSSTATE_MANUALIN,
                BizConstant.SP_TRANSSTATE_STATIONIN,
                BizConstant.SP_TRANSSTATE_BAYIN,
                BizConstant.SP_TRANSSTATE_SHELFIN,
                BizConstant.SP_TRANSSTATE_ABNORMALIN);

        HV_BUFFER += HV_TMPBUFFER;

        List<Object[]> RTCLPOD2 = cimJpaRepository.query(HV_BUFFER);

        /*------------------------*/
        /*   Fetch Cursor Table   */
        /*------------------------*/
        List<Results.InventoriedReticlePodInfo> pptRtclSeq;
        int pptSeqLen = 0;
        int t_count = 1000;
        pptRtclSeq = new ArrayList<>(t_count);

        for (Object[] RTCLPOD : RTCLPOD2) {
            String hFRRTCLPODRTCLPOD_ID = CimObjectUtils.toString(RTCLPOD[0]);
            String hFRRTCLPODRTCLPOD_OBJ = CimObjectUtils.toString(RTCLPOD[1]);

            if (pptSeqLen >= t_count) {

                t_count = t_count + 500;
            }

            pptRtclSeq.add(new Results.InventoriedReticlePodInfo());
            pptRtclSeq.get(pptSeqLen).setReticlePodID(ObjectIdentifier.build(hFRRTCLPODRTCLPOD_ID, hFRRTCLPODRTCLPOD_OBJ));
            pptSeqLen++;
        }

        /*-------------------------------------*/
        /*   Change to AbnormalOut Procedure   */
        /*-------------------------------------*/
        for (i = 0; i < pptSeqLen; i++) {
            Boolean foundFlag = FALSE;

            for (j = 0; j < txSeqLen; j++) {

                if (CimStringUtils.equals(strInventoryReticlePodInfo.get(j).getReticlePodID().getValue(), pptRtclSeq.get(i).getReticlePodID().getValue())) {
                    foundFlag = TRUE;
                    break;
                }
            }

            if (Objects.equals(foundFlag, TRUE)) {
                // Found reticlePod for DB
                continue;
            }

            // Not Found reticlePod for DB

            /*-------------------------------------------------------*/
            /*   Change Transfer Status for reticlePod and reticle   */
            /*-------------------------------------------------------*/
            ObjectIdentifier dummyID = null;

            // reticlePod_transferState_Change__160
            reticlePodTransferStateChange(
                    strObjCommonIn,
                    stockerID,
                    dummyID,
                    pptRtclSeq.get(i).getReticlePodID(),
                    BizConstant.SP_TRANSSTATE_ABNORMALOUT,
                    CimDateUtils.getTimestampAsString(strObjCommonIn.getTimeStamp().getReportTimeStamp()),
                    "", shelfPosition);
        }

        /*-------------------------------------*/
        /*   Change to ManualIn Procedure      */
        /*-------------------------------------*/
        t_count = 1000;
        List<Results.InventoriedReticlePodInfo> strInventoriedReticlePodInfo = new ArrayList<>(t_count);

        for (i = 0; i < txSeqLen; i++) {
            Boolean foundFlag = FALSE;

            for (j = 0; j < pptSeqLen; j++) {

                if (CimStringUtils.equals(strInventoryReticlePodInfo.get(i).getReticlePodID().getValue(), pptRtclSeq.get(j).getReticlePodID().getValue())) {
                    foundFlag = TRUE;
                    break;
                }
            }

            /*-------------------------------------*/
            /*   Get Reticle List for ReticlePod   */
            /*-------------------------------------*/
            Outputs.ObjReticlePodReticleListGetDROut strReticlePodreticleListGetDRout = null;
            //reticlePod_reticleList_GetDR
            try {
                strReticlePodreticleListGetDRout = reticlePodReticleListGetDR(
                        strObjCommonIn,
                        strInventoryReticlePodInfo.get(i).getReticlePodID());
            } catch (ServiceException ex) {
                // Illegal reticlePodID specify
                // Error is Not Found for DB
                foundFlag = FALSE;
            }

            if (Objects.equals(foundFlag, TRUE)) {
                // Found reticlePod for DB
                if (setSeqLen >= t_count) {
                    t_count = t_count + 500;
                }

                strInventoriedReticlePodInfo.add(new Results.InventoriedReticlePodInfo());
                strInventoriedReticlePodInfo.get(setSeqLen).setReticlePodID(strInventoryReticlePodInfo.get(i).getReticlePodID());
                strInventoriedReticlePodInfo.get(setSeqLen).setReturnCode(retCodeConfig.getSucc().toString());
                // Copy Reticle List
                strInventoriedReticlePodInfo.get(setSeqLen).setReticleID(strReticlePodreticleListGetDRout.getReticleID());
                setSeqLen++;
                continue;
            }

            // Not Found reticlePod for DB

            /*------------------------*/
            /*   Get Reticle Object   */
            /*------------------------*/
            Boolean reticlePodFound = FALSE;

            CimReticlePod aReticlePodObj;
            aReticlePodObj = baseCoreFactory.getBO(CimReticlePod.class, strInventoryReticlePodInfo.get(i).getReticlePodID());
            reticlePodFound = aReticlePodObj != null;

            if (Objects.equals(reticlePodFound, FALSE)) {

                if (setSeqLen >= t_count) {

                    t_count = t_count + 500;
                }

                strInventoriedReticlePodInfo.add(new Results.InventoriedReticlePodInfo());
                strInventoriedReticlePodInfo.get(setSeqLen).setReticlePodID(strInventoryReticlePodInfo.get(i).getReticlePodID());
                strInventoriedReticlePodInfo.get(setSeqLen).setReturnCode(retCodeConfig.getNotFoundReticlePod().toString());
                // Copy Reticle List
                if (null != strReticlePodreticleListGetDRout) {
                    strInventoriedReticlePodInfo.get(setSeqLen).setReticleID(strReticlePodreticleListGetDRout.getReticleID());
                }
                setSeqLen++;
                errorCount++;
                continue;
            }

            /*-------------------------------------------------------*/
            /*   Change Transfer Status for reticlePod and reticle   */
            /*-------------------------------------------------------*/
            ObjectIdentifier dummyID = null;

            // reticlePod_transferState_Change__160
            reticlePodTransferStateChange(
                    strObjCommonIn,
                    stockerID,
                    dummyID,
                    strInventoryReticlePodInfo.get(i).getReticlePodID(),
                    BizConstant.SP_TRANSSTATE_MANUALIN,
                    CimDateUtils.getTimestampAsString(strObjCommonIn.getTimeStamp().getReportTimeStamp()),
                    "", shelfPosition);

            if (setSeqLen >= t_count) {

                t_count = t_count + 500;
            }

            strInventoriedReticlePodInfo.add(new Results.InventoriedReticlePodInfo());
            strInventoriedReticlePodInfo.get(setSeqLen).setReticlePodID(strInventoryReticlePodInfo.get(i).getReticlePodID());
            strInventoriedReticlePodInfo.get(setSeqLen).setReturnCode(retCodeConfig.getSucc().toString());
            // Copy Reticle List
            if (null != strReticlePodreticleListGetDRout) {
                strInventoriedReticlePodInfo.get(setSeqLen).setReticleID(strReticlePodreticleListGetDRout.getReticleID());
            }
            setSeqLen++;
        }
        strReticlePodPositionUpdateByStockerInventoryDROut = strInventoriedReticlePodInfo;

        /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/
        if (errorCount == 0) {
        } else if (setSeqLen == errorCount) {
            Validations.check(true, retCodeConfigEx.getAllrtclinvDataError());
        } else {
            Validations.check(true, retCodeConfigEx.getSomertclinvDataError());
        }

        return (strReticlePodPositionUpdateByStockerInventoryDROut);
    }

    @Override
    public List<Infos.ReticleDispatchJob> reticleDispatchJobListGetDR(Infos.ObjCommon objCommon, String reticleDispatchJobID) {
        List<Infos.ReticleDispatchJob> result = new ArrayList<>();
        /*--------------------------------------------*/
        /*   Get reticle list into output structure   */
        /*--------------------------------------------*/
        String sql = "SELECT  STOCKER_ID, " +
                "REQ_TIME, " +
                "RTCL_ID, " +
                "RTCLPOD_ID, " +
                "RDJ_ID, " +
                "CUR_EQP_ID, " +
                "CUR_EQP_CATEGORY, " +
                "DEST_EQP_ID, " +
                "DEST_EQP_CATEGORY, " +
                "PRIORITY, " +
                "REQ_USER_ID, " +
                "JOB_STATUS, " +
                "JOB_STATUS_CHG_TIME " +
                "FROM  OSRTCLACTLIST ";
        if (CimStringUtils.isNotEmpty(reticleDispatchJobID)) {
            sql += String.format(" WHERE RDJ_ID = '%s' ", reticleDispatchJobID);
        }
        sql += " ORDER BY RDJ_ID";
        List<Object[]> queryList = cimJpaRepository.query(sql);
        Optional.of(queryList).ifPresent(list -> list.forEach(data -> {
            Infos.ReticleDispatchJob job = new Infos.ReticleDispatchJob();
            job.setDispatchStationID(CimObjectUtils.toString(data[0]));
            job.setRequestedTimestamp(CimObjectUtils.toString(data[1]));
            job.setReticleID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[2])));
            job.setReticlePodID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[3])));
            job.setReticleDispatchJobID(CimObjectUtils.toString(data[4]));
            job.setFromEquipmentID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[5])));
            job.setFromEquipmentCategory(CimObjectUtils.toString(data[6]));
            job.setToEquipmentID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[7])));
            job.setToEquipmentCategory(CimObjectUtils.toString(data[8]));
            job.setPriority(((BigDecimal) data[9]).longValue());
            job.setRequestUserID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[10])));
            job.setJobStatus(CimObjectUtils.toString(data[11]));
            job.setJobStatusChangeTimestamp(CimObjectUtils.toString(data[12]));
            result.add(job);
        }));
        return result;
    }

    @Override
    public List<Infos.ReticleComponentJob> reticleComponentJobListGetDR(Infos.ObjCommon objCommon, String reticleDispatchJobID) {
        /*--------------------------------------------*/
        /*   Get reticle list into output structure   */
        /*--------------------------------------------*/
        CimRELDO example = new CimRELDO();
        if (CimStringUtils.isNotEmpty(reticleDispatchJobID)) {
            example.setRdjId(reticleDispatchJobID);
        }
        return cimJpaRepository.findAll(Example.of(example)).stream()
                .sorted(Comparator.comparing(CimRELDO::getRdjId))
                .sorted(Comparator.comparing(CimRELDO::getRcjId))
                .map(data -> {
                    Infos.ReticleComponentJob job = new Infos.ReticleComponentJob();
                    job.setRequestedTimestamp(String.valueOf(data.getReqTime()));
                    job.setPriority(CimNumberUtils.longValue(data.getPriority()));
                    job.setReticleDispatchJobID(data.getRdjId());
                    job.setReticleComponentJobID(data.getRcjId());
                    job.setReticleDispatchJobRequestUserID(ObjectIdentifier.buildWithValue(data.getReqUserId()));
                    job.setJobSeq(CimNumberUtils.longValue(data.getJobSeq()));
                    job.setReticlePodID(ObjectIdentifier.buildWithValue(data.getRtclpodId()));
                    job.setSlotNo(CimNumberUtils.longValue(data.getSlotNo()));
                    job.setReticleID(ObjectIdentifier.buildWithValue(data.getRtclId()));
                    job.setJobName(data.getJobName());
                    job.setToEquipmentID(ObjectIdentifier.buildWithValue(data.getToEqpId()));
                    job.setToReticlePodPortID(ObjectIdentifier.buildWithValue(data.getToPortId()));
                    job.setToEquipmentCategory(data.getToEqpCategory());
                    job.setFromEquipmentID(ObjectIdentifier.buildWithValue(data.getFromEqpId()));
                    job.setFromReticlePodPortID(ObjectIdentifier.buildWithValue(data.getFromPortId()));
                    job.setFromEquipmentCategory(data.getFromEqpCategory());
                    job.setJobStatus(data.getJobStatus());
                    job.setJobStatusChangeTimestamp(String.valueOf(data.getJobStatChgTime()));
                    return job;
                }).collect(Collectors.toList());
    }

    @Override
    public Outputs.ReticleDispatchJobCreateOut reticleDispatchJobCreate(Infos.ObjCommon objCommon, String reticleDispatchJobID, ObjectIdentifier rDJRequestUserID, Long priority, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID, ObjectIdentifier toMachineID) {
        Outputs.ReticleDispatchJobCreateOut result = new Outputs.ReticleDispatchJobCreateOut();
        //---------------------------------------------
        //  Check for Reticle condition
        //---------------------------------------------
        log.debug("Check for Reticle condition");
        ObjectIdentifier reticleMachineID = null;
        String reticleMachineType = null;
        String reticleState = null;

        log.debug("call reticle_availability_CheckForXfer() to get reticle info for Xfer.");
        log.debug("step1 - reticleMethod.reticleAvailabilityCheckForXfer");
        Outputs.ReticleAvailabilityCheckForXferOut reticleAvailabilityCheckForXferOut = this.reticleAvailabilityCheckForXfer(objCommon, reticleID);

        reticleMachineID = reticleAvailabilityCheckForXferOut.getMachineID();
        reticleMachineType = reticleAvailabilityCheckForXferOut.getMachineType();
        reticleState = reticleAvailabilityCheckForXferOut.getDurableState();

        if (log.isDebugEnabled()){
            log.debug("reticleMachineID   = {}", ObjectIdentifier.fetchValue(reticleMachineID));
            log.debug("reticleMachineType = {}", reticleMachineType);
            log.debug("reticleState       = {}", reticleState);
        }

        //--------------------------------------------------------
        //  If reticle is in RSP, that RSP should be specified
        //--------------------------------------------------------
        if (ObjectIdentifier.isNotEmptyWithValue(reticleAvailabilityCheckForXferOut.getReticlePodID())
                && ObjectIdentifier.isNotEmptyWithValue(reticlePodID)
                && !ObjectIdentifier.equalsWithValue(reticleAvailabilityCheckForXferOut.getReticlePodID(), reticlePodID)) {
            log.error("Current reticlePodID     = {}", ObjectIdentifier.fetchValue(reticleAvailabilityCheckForXferOut.getReticlePodID()));
            log.error("Specified reticlePodID   = {}", ObjectIdentifier.fetchValue(reticlePodID));
            Validations.check(true, retCodeConfig.getReticleIsInReticlepod(), ObjectIdentifier.fetchValue(reticleID), ObjectIdentifier.fetchValue(reticleAvailabilityCheckForXferOut.getReticlePodID()));
        }

        Infos.ReticleDispatchJob strReticleDispatchJob = new Infos.ReticleDispatchJob();
        result.setStrReticleDispatchJob(strReticleDispatchJob);
        strReticleDispatchJob.setFromEquipmentID(reticleMachineID);
        strReticleDispatchJob.setFromEquipmentCategory(reticleMachineType);

        if (ObjectIdentifier.equalsWithValue(toMachineID, reticleMachineID)) {
            log.error("toMachineID is same as reticleMachineID. {}", ObjectIdentifier.fetchValue(toMachineID));
            Validations.check(true, retCodeConfigEx.getInvalidRdjRecord(), reticleDispatchJobID);
        }

        ObjectIdentifier xferReticlePodID = null;
        if (ObjectIdentifier.isNotEmptyWithValue(reticlePodID)) {
            if (log.isDebugEnabled())
                log.debug("ReticlePodID {} specified.", ObjectIdentifier.fetchValue(reticlePodID));
            xferReticlePodID = reticlePodID;
        }

        ObjectIdentifier fromMachineID = reticleMachineID;
        String fromMachienType = reticleMachineType;
        if (log.isDebugEnabled()){
            log.debug("fromMachineID   = {}", ObjectIdentifier.fetchValue(fromMachineID));
            log.debug("fromMachineType = {}", fromMachienType);
        }

        //---------------------------------------------
        //  Check for ReticlePod condition
        //---------------------------------------------
        log.debug("Check for ReticlePod condition");
        if (ObjectIdentifier.isNotEmptyWithValue(xferReticlePodID)) {
            if (log.isDebugEnabled())
                log.debug("xferReticlePodID {} is specified. ", ObjectIdentifier.fetchValue(xferReticlePodID));
            log.debug("step2 - reticleMethod.reticlePodAvailabilityCheckForXfer");
            Outputs.ReticlePodAvailabilityCheckForXferOut reticlePodAvailabilityCheckForXferOut = this.reticlePodAvailabilityCheckForXfer(objCommon, xferReticlePodID, reticleDispatchJobID);

            ObjectIdentifier reticlePodMachineID = reticlePodAvailabilityCheckForXferOut.getMachineID();
            String reticlePodMachineType = reticlePodAvailabilityCheckForXferOut.getMachineType();

            if (log.isDebugEnabled()){
                log.info("reticlePodMachineID   = {}", reticlePodMachineID);
                log.info("reticlePodMachineType = {}", reticlePodMachineType);
            }

            if (CimBooleanUtils.isFalse(reticlePodAvailabilityCheckForXferOut.getEmptyFlag())) {
                log.debug("reticlePodAvailabilityCheckForXferOut.emptyFlag == FALSE, Reticle exists in ReticlePod.");
                Boolean checkFlag = false;
                if (CimArrayUtils.isNotEmpty(reticlePodAvailabilityCheckForXferOut.getReticleList())) {
                    for (ObjectIdentifier reticle : reticlePodAvailabilityCheckForXferOut.getReticleList()) {
                        log.debug("check loop of ReticleIDs in xfer reticlePod");
                        if (log.isDebugEnabled()){
                            log.info("reticleID (in xferReticlePod) = {}", ObjectIdentifier.fetchValue(reticlePodAvailabilityCheckForXferOut.getReticleList().get(0)));
                            log.info("xfer reticleID                = {}", ObjectIdentifier.fetchValue(reticleID));
                        }
                        if (ObjectIdentifier.equalsWithValue(reticlePodAvailabilityCheckForXferOut.getReticleList().get(0), reticleID)) {
                            if (log.isDebugEnabled())
                                log.info("Reticle is already in xfer reticlePod. {},{}", ObjectIdentifier.fetchValue(reticleID), ObjectIdentifier.fetchValue(xferReticlePodID));
                            checkFlag = true;
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isFalse(checkFlag)) {
                    log.error("Other reticle is already in selected reticlePod.{}", ObjectIdentifier.fetchValue(xferReticlePodID));
                    Validations.check(true, retCodeConfigEx.getRspNotEmpty(), ObjectIdentifier.fetchValue(xferReticlePodID));
                }
            } else {
                log.debug("ReticlePod is empty. check xfer reticle exists in ReticlePodStocker.");
                log.debug("step3 - reticleMethod.reticleDetailInfoGetDR");
                Results.ReticleDetailInfoInqResult reticleDetailInfoInqResult = this.reticleDetailInfoGetDR(objCommon, reticleID, false, false);
                if (log.isDebugEnabled())
                    log.debug("reticle is at {},{}",
                        null == reticleDetailInfoInqResult.getReticleStatusInfo() ? null : ObjectIdentifier.fetchValue(reticleDetailInfoInqResult.getReticleStatusInfo().getStockerID()),
                        null == reticleDetailInfoInqResult.getReticleStatusInfo() ? null : ObjectIdentifier.fetchValue(reticleDetailInfoInqResult.getReticleStatusInfo().getEquipmentID()));
                if (null != reticleDetailInfoInqResult.getReticleStatusInfo()) {
                    Infos.ReticleStatusInfo reticleStatusInfo = reticleDetailInfoInqResult.getReticleStatusInfo();
                    if (ObjectIdentifier.isNotEmptyWithValue(reticleStatusInfo.getStockerID())) {
                        log.debug("reticle is in stocker");
                        log.debug("step4 - stockerMethod.stockerTypeGetDR");
                        Outputs.ObjStockerTypeGetDROut stockerTypeGetDROut = stockerMethod.stockerTypeGetDR(objCommon, reticleStatusInfo.getStockerID());
                        if (log.isDebugEnabled())
                            log.debug("stockerType by reticle relate stocker = {}", stockerTypeGetDROut.getStockerType());
                        if (CimStringUtils.equals(stockerTypeGetDROut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                            log.error("stockerType == 'ReticlePod', Reticle is in ReticlePodStocker with another ReticlePod.");
                            Validations.check(true, retCodeConfigEx.getRtclNotInReclPod(),
                                    ObjectIdentifier.fetchValue(reticleID),
                                    ObjectIdentifier.fetchValue(xferReticlePodID),
                                    "");
                        }
                    }
                }
            }
            //---------------------------------------------
            //  Check for fromMachine condition
            //---------------------------------------------
            log.debug("Check for fromMachine condition");
            String transferStatus = reticlePodAvailabilityCheckForXferOut.getTransferStatus();
            ObjectIdentifier podPortID = reticlePodAvailabilityCheckForXferOut.getPortID();
            String podPortStatus = reticlePodAvailabilityCheckForXferOut.getPortStatus();
            String portAccessMode = reticlePodAvailabilityCheckForXferOut.getPortAccessMode();

            if (log.isDebugEnabled()){
                log.debug("transferStatus = {}", transferStatus);
                log.debug("podPortID      = {}", podPortID);
                log.debug("podPortStatus  = {}", podPortStatus);
                log.debug("portAccessMode = {}", portAccessMode);
            }

            if (!CimStringUtils.equals(fromMachienType, BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.debug("fromMachineType == 'Equipment' or 'BareReticle', check ReticlePod is loaded on");
                if (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_STATIONIN)
                        || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    if (log.isDebugEnabled())
                        log.debug("reticlePod xferStatus is 'SI' or 'EI', check accessMode {}", portAccessMode);
                    if (!CimStringUtils.equals(portAccessMode, BizConstant.SP_RETICLEPODPORT_ACCESSMODE_AUTO)) {
                        log.error("accessMode is not 'Auto', return!");
                        Validations.check(true, retCodeConfigEx.getEqpPortAccessMode(),
                                ObjectIdentifier.fetchValue(fromMachineID),
                                ObjectIdentifier.fetchValue(podPortID),
                                portAccessMode
                        );
                    } else {
                        log.debug("reticlePod is not loaded. check existance of 'Auto' PSPPort.");
                        //BareReticleStocker
                        if (CimStringUtils.equals(fromMachienType, BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                            log.debug("check about 'fromMachine' BareReticleStocker.");
                            log.debug("step5 - stockerMethod.stockerReticlePodPortInfoGetDR");
                            List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, fromMachineID);
                            Boolean checkFlag = false;
                            if (CimArrayUtils.isNotEmpty(reticlePodPortInfos)) {
                                for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfos) {
                                    if (CimStringUtils.equals(reticlePodPortInfo.getAccessMode(), BizConstant.SP_RETICLEPODPORT_ACCESSMODE_AUTO)) {
                                        log.debug("AccessMode_Auto port is exist!");
                                        checkFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (CimBooleanUtils.isFalse(checkFlag)) {
                                log.error("AccessMode_Auto port is not found.");
                                Validations.check(true, retCodeConfigEx.getNotFoundSuitableRsp());
                            }
                        }//Equipment
                        else if (CimStringUtils.equals(fromMachienType, BizConstant.SP_MACHINE_TYPE_EQP)) {
                            log.debug("check about 'fromMachine' Equipment.");
                            log.debug("step6 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
                            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, fromMachineID);
                            Boolean checkFlag = false;
                            if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                                for (Infos.ReticlePodPortInfo reticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                                    if (CimStringUtils.equals(reticlePodPortInfo.getAccessMode(), BizConstant.SP_RETICLEPODPORT_ACCESSMODE_AUTO)) {
                                        log.debug("AccessMode_Auto port is exist!");
                                        checkFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (CimBooleanUtils.isFalse(checkFlag)) {
                                log.error("AccessMode_Auto port is not found.");
                                Validations.check(true, retCodeConfigEx.getNotFoundSuitableRsp());
                            }
                        }
                    }
                }
            } else {
                log.debug("fromMachineType == 'ReticlePod', no need to check RSP port.");
            }
        } else {
            log.debug("xferReticlePodID is not specified. check existance of 'Auto' PSPPort.");
            //BareReticleStocker
            if (CimStringUtils.equals(fromMachienType, BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.debug("check about 'fromMachine' BareReticleStocker.");
                log.debug("step7 - stockerMethod.stockerReticlePodPortInfoGetDR");
                List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, fromMachineID);
                Boolean checkFlag = false;
                if (CimArrayUtils.isNotEmpty(reticlePodPortInfos)) {
                    for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfos) {
                        if (CimStringUtils.equals(reticlePodPortInfo.getAccessMode(), BizConstant.SP_RETICLEPODPORT_ACCESSMODE_AUTO)) {
                            log.debug("AccessMode_Auto port is exist!");
                            checkFlag = true;
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isFalse(checkFlag)) {
                    log.error("AccessMode_Auto port is not found.");
                    Validations.check(true, retCodeConfigEx.getNotFoundSuitableRsp());
                }
            }//Equipment
            else if (CimStringUtils.equals(fromMachienType, BizConstant.SP_MACHINE_TYPE_EQP)) {
                log.debug("check about 'fromMachine' Equipment.");
                log.debug("step8 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
                Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, fromMachineID);
                Boolean checkFlag = false;
                if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                    for (Infos.ReticlePodPortInfo reticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                        if (CimStringUtils.equals(reticlePodPortInfo.getAccessMode(), BizConstant.SP_RETICLEPODPORT_ACCESSMODE_AUTO)) {
                            log.debug("AccessMode_Auto port is exist!");
                            checkFlag = true;
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isTrue(checkFlag)) {
                    log.error("AccessMode_Auto port is not found.");
                    Validations.check(true, retCodeConfigEx.getNotFoundSuitableRsp());
                }
            }
        }
        log.debug("check fromMachine condition for Xfer.");
        log.debug("step9 - equipmentMethod.machineStateAvailabilityCheckForReticlePodXfer");
        String machineType = equipmentMethod.machineStateAvailabilityCheckForReticlePodXfer(objCommon,
                fromMachineID);

        //---------------------------------------------
        //  Check for toMachine condition
        //---------------------------------------------
        log.debug("Check for toMachine condition");
        log.debug("step10 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, toMachineID);

        String toMachineType = null;
        if (CimBooleanUtils.isTrue(machineTypeGetOut.isBStorageMachineFlag())) {
            if (log.isDebugEnabled())
                log.debug("toMachine is Stocker. set toMachineType {}", machineTypeGetOut.getStockerType());
            toMachineType = machineTypeGetOut.getStockerType();
        } else {
            if (log.isDebugEnabled())
                log.debug("toMachine is Equipment. set toMachineType {}", BizConstant.SP_MACHINE_TYPE_EQP);
            toMachineType = BizConstant.SP_MACHINE_TYPE_EQP;
        }
        if (log.isDebugEnabled())
            log.debug("toMachineType = {}", toMachineType);

        //BareReticleStocker
        if (CimStringUtils.equals(fromMachienType, BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("check about 'fromMachine' BareReticleStocker.");
            log.debug("step11 - stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, fromMachineID);
            Boolean checkFlag = false;
            if (CimArrayUtils.isNotEmpty(reticlePodPortInfos)) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfos) {
                    if (CimStringUtils.equals(reticlePodPortInfo.getAccessMode(), BizConstant.SP_RETICLEPODPORT_ACCESSMODE_AUTO)) {
                        log.debug("AccessMode_Auto port is exist!");
                        checkFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(checkFlag)) {
                log.debug("AccessMode_Auto port is not found.");
                Validations.check(true, retCodeConfigEx.getNotFoundSuitableRsp());
            }
        }//Equipment
        else if (CimStringUtils.equals(fromMachienType, BizConstant.SP_MACHINE_TYPE_EQP)) {
            log.debug("check about 'fromMachine' Equipment.");
            log.debug("step12 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, fromMachineID);
            Boolean checkFlag = false;
            if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                    if (CimStringUtils.equals(reticlePodPortInfo.getAccessMode(), BizConstant.SP_RETICLEPODPORT_ACCESSMODE_AUTO)) {
                        log.debug("AccessMode_Auto port is exist!");
                        checkFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(checkFlag)) {
                log.error("AccessMode_Auto port is not found.");
                Validations.check(true, retCodeConfigEx.getNotFoundSuitableRsp());
            }
        }

        log.debug("step13 - equipmentMethod.machineStateAvailabilityCheckForReticlePodXfer");
        String machineTypeTemp = equipmentMethod.machineStateAvailabilityCheckForReticlePodXfer(objCommon, toMachineID);

        //---------------------------------------------
        //  Make RDJ info
        //---------------------------------------------
        log.debug("Make RDJ info");
        Infos.ReticleDispatchJob tmpRDJ = new Infos.ReticleDispatchJob();
        tmpRDJ.setDispatchStationID("");
        tmpRDJ.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        tmpRDJ.setReticleID(reticleID);
        tmpRDJ.setReticlePodID(xferReticlePodID);
        tmpRDJ.setReticleDispatchJobID(reticleDispatchJobID);
        tmpRDJ.setFromEquipmentID(fromMachineID);
        tmpRDJ.setFromEquipmentCategory(fromMachienType);
        tmpRDJ.setToEquipmentID(toMachineID);
        tmpRDJ.setToEquipmentCategory(toMachineType);
        tmpRDJ.setPriority(1L);
        tmpRDJ.setRequestUserID(rDJRequestUserID);

        if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.ASYNC_RETICLE_XFER_JOB_CREATE_REQ.getValue())) {
            tmpRDJ.setJobStatus(BizConstant.SP_RDJ_STATUS_CREATED);
        } else {
            tmpRDJ.setJobStatus(BizConstant.SP_RDJ_STATUS_WAITTOEXECUTE);
        }

        tmpRDJ.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        result.setStrReticleDispatchJob(tmpRDJ);

        //---------------------------------------------
        //  Make EventRecord
        //---------------------------------------------
        ObjectIdentifier dummyID = ObjectIdentifier.buildWithValue("");
        Infos.ReticleEventRecord reticleEventRecord = new Infos.ReticleEventRecord();
        reticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        reticleEventRecord.setReticleDispatchJobID(reticleDispatchJobID);
        reticleEventRecord.setReticleComponentJobID("");
        reticleEventRecord.setReticlePodStockerID(dummyID);
        reticleEventRecord.setBareReticleStockerID(dummyID);
        reticleEventRecord.setResourceID(dummyID);
        reticleEventRecord.setEquipmentID(dummyID);
        reticleEventRecord.setRSPPortID(dummyID);
        reticleEventRecord.setReticlePodID(dummyID);
        reticleEventRecord.setRSPPortEvent("");
        reticleEventRecord.setReticleID(dummyID);
        reticleEventRecord.setReticleJobEvent("");

        result.setStrReticleEventRecord(reticleEventRecord);
        return result;
    }

    @Override
    public Outputs.ReticleAvailabilityCheckForXferOut reticleAvailabilityCheckForXfer(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        Outputs.ReticleAvailabilityCheckForXferOut result = new Outputs.ReticleAvailabilityCheckForXferOut();
        /*-----------------------------*/
        /*   Get reticle information   */
        /*-----------------------------*/
        log.info("step1- reticleMethod.reticleDetailInfoGetDR");
        Results.ReticleDetailInfoInqResult reticleDetailInfoInqResult = this.reticleDetailInfoGetDR(objCommon, reticleID, false, false);

        ObjectIdentifier machineID = null;
        if (null != reticleDetailInfoInqResult && null != reticleDetailInfoInqResult.getReticleStatusInfo()) {
            Infos.ReticleStatusInfo reticleStatusInfo = reticleDetailInfoInqResult.getReticleStatusInfo();
            if (ObjectIdentifier.isNotEmptyWithValue(reticleStatusInfo.getEquipmentID())) {
                machineID = reticleStatusInfo.getEquipmentID();
                log.info("Reticle is on EQP: {}", ObjectIdentifier.fetchValue(machineID));
            } else if (ObjectIdentifier.isNotEmptyWithValue(reticleStatusInfo.getStockerID())) {
                machineID = reticleStatusInfo.getStockerID();
                log.info("Reticle is on STK: {}", ObjectIdentifier.fetchValue(machineID));
            } else {
                log.info("Reticle is not found on machine");
                Validations.check(true, retCodeConfigEx.getUnKnownReticlePosition());
            }
            String transferStatus = reticleStatusInfo.getTransferStatus();
            result.setMachineID(machineID);
            result.setTransferStatus(transferStatus);
            result.setReticlePodID(reticleStatusInfo.getReticlePodID());

            /*--------------------------------*/
            /*   Check machine availability   */
            /*--------------------------------*/
            String machineStateAvailabilityCheckForReticlePodXferOut = null;
            log.info("step2- equipmentMethod.machineStateAvailabilityCheckForReticlePodXfer");
            try {
                machineStateAvailabilityCheckForReticlePodXferOut = equipmentMethod.machineStateAvailabilityCheckForReticlePodXfer(objCommon,
                        machineID);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                    Validations.check(true, retCodeConfig.getReticleNotInTheEqp(), ObjectIdentifier.fetchValue(reticleID));
                } else {
                    throw e;
                }
            }
            result.setMachineType(machineStateAvailabilityCheckForReticlePodXferOut);
            /*-----------------------------------*/
            /*   Check reticle transfer status   */
            /*-----------------------------------*/
            if (!CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_SHELFIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_INTERMEDIATEIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_ABNORMALIN, transferStatus)) {
                log.info("TransferStatus {} of reticle is NOT *I.", transferStatus);
                Validations.check(true, retCodeConfig.getInvalidTransferState());
            }

            /*-----------------------------------*/
            /*   Check reticle RCJ reservation   */
            /*-----------------------------------*/
            ObjectIdentifier dummy = ObjectIdentifier.buildWithValue("");
            log.info("step3 - reticleMethod.reticleComponentJobCheckExistenceDR");
            List<Infos.ReticleComponentJob> reticleComponentJobList = this.reticleComponentJobCheckExistenceDR(objCommon, reticleID, dummy, dummy, dummy, dummy, dummy);

            CimProcessDurable aReticle = null;
            if (ObjectIdentifier.isEmptyWithRefKey(reticleID)) {
                Validations.check(ObjectIdentifier.isEmptyWithValue(reticleID), retCodeConfig.getNotFoundReticle());
                ProcessDurable aTempDurable = durableManager.findProcessDurableNamed(reticleID.getValue());
                aReticle = (CimProcessDurable) aTempDurable;
            } else {
                aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID.getReferenceKey());
            }
            Validations.check(null == aReticle, retCodeConfig.getNotFoundReticle());

            /*--------------------------------*/
            /*   Check reticle Reservation   */
            /*-------------------------------*/
            Boolean reticleReserved = aReticle.isReserved();
            if (CimBooleanUtils.isTrue(reticleReserved)) {
                log.info("This reticle {} is reserved.", ObjectIdentifier.fetchValue(reticleID));
                Validations.check(true, retCodeConfigEx.getAlreadyReservedReticle());
            }
            /*-----------------------*/
            /*   Check Control job   */
            /*-----------------------*/
            List<CimControlJob> theReticleControlJobList = aReticle.allReservedControlJobs();
            if (CimArrayUtils.isNotEmpty(theReticleControlJobList)) {
                //--------------------------------------------------
                //   Get OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS
                //--------------------------------------------------
                String retrieveReticleDuringLotProcFlag = StandardProperties.OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS.getValue();
                log.info("Env OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS {}", retrieveReticleDuringLotProcFlag);
                if (CimStringUtils.equals(BizConstant.VALUE_ONE, retrieveReticleDuringLotProcFlag)) {
                    log.info("Check if reticle has reserve control job");
                    for (CimControlJob reticleControlJob : theReticleControlJobList) {
                        if (null == reticleControlJob) {
                            continue;
                        }
                        //--------------------------------------------------
                        //  Get control job status
                        //--------------------------------------------------
                        String controlJobStatus = reticleControlJob.controlJobStatusGet();
                        if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBSTATUS_CREATED, controlJobStatus)) {
                            log.info("controlJob status is Created");
                            Validations.check(true, retCodeConfigEx.getReticleHasControlJob(), ObjectIdentifier.fetchValue(reticleID));
                        }

                    }
                } else {
                    log.info("Reticle has control job.", CimArrayUtils.getSize(theReticleControlJobList));
                    Validations.check(retCodeConfigEx.getReticleHasControlJob(), ObjectIdentifier.fetchValue(reticleID));
                }
            }
            String strReticleState = aReticle.getDurableState();
            result.setDurableState(strReticleState);
        }
        return result;
    }

    @Override
    public List<Infos.ReticleComponentJob> reticleComponentJobCheckExistenceDR(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID, ObjectIdentifier fromMachineID, ObjectIdentifier fromPortID, ObjectIdentifier toMachineID, ObjectIdentifier toPortID) {
        List<Infos.ReticleComponentJob> result = new ArrayList<>();
        String sql = "SELECT REQ_TIME," +
                "PRIORITY," +
                "RDJ_ID," +
                "RCJ_ID," +
                "REQ_USER_ID," +
                "JOB_SEQ," +
                "RTCL_ID," +
                "RTCLPOD_ID," +
                "SLOT_NO," +
                "JOB_NAME," +
                "DEST_EQP_ID," +
                "DEST_EQP_CATEGORY," +
                "DEST_PORT_ID," +
                "CUR_EQP_ID," +
                "CUR_EQP_CATEGORY," +
                "CUR_PORT_ID," +
                "JOB_STATUS," +
                "JOB_STATUS_CHG_TIME " +
                "FROM  OSRTCLEXELIST " +
                "WHERE JOB_STATUS != 'Completed' AND (";

        boolean firstCondition = true, rpsFlag = false;
        String sqlTemp;
        if (ObjectIdentifier.isNotEmptyWithValue(reticleID)) {
            if (CimBooleanUtils.isTrue(firstCondition)) {
                firstCondition = false;
            } else {
                sql += " OR ";
            }
            sqlTemp = String.format("RTCL_ID = '%s'", ObjectIdentifier.fetchValue(reticleID));
            sql += sqlTemp;
        }
        if (ObjectIdentifier.isNotEmptyWithValue(reticlePodID)) {
            if (CimBooleanUtils.isTrue(firstCondition)) {
                firstCondition = false;
            } else {
                sql += " OR ";
            }
            sqlTemp = String.format("RTCLPOD_ID = '%s'", ObjectIdentifier.fetchValue(reticlePodID));
            sql += sqlTemp;
        }
        String stockerType = null;
        if (ObjectIdentifier.isNotEmptyWithValue(fromMachineID)) {
            log.debug("step1 - stockerMethod.stockerTypeGet");
            try {
                Outputs.ObjStockerTypeGetDROut stockerTypeGetDROut = stockerMethod.stockerTypeGetDR(objCommon, fromMachineID);
                log.debug("fromMachine is stocker.");
                stockerType = stockerTypeGetDROut.getStockerType();
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getUndefinedStockerType(), e.getCode())) {
                    log.debug("fromMachine is not stocker.");
                    stockerType = "";
                } else {
                    throw e;
                }
            }
            if (!CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_RETICLEPOD, stockerType)) {
                log.debug("fromMachine is not reticle pod stocker");
                if (ObjectIdentifier.isNotEmptyWithValue(fromPortID)) {
                    if (CimBooleanUtils.isTrue(firstCondition)) {
                        firstCondition = false;
                    } else {
                        sql += " OR ";
                    }
                    sqlTemp = String.format("(CUR_EQP_ID = '%s' AND CUR_PORT_ID = '%s' ) OR (DEST_EQP_ID = '%s' AND DEST_PORT_ID = '%s')",
                            ObjectIdentifier.fetchValue(fromMachineID),
                            ObjectIdentifier.fetchValue(fromPortID),
                            ObjectIdentifier.fetchValue(fromMachineID),
                            ObjectIdentifier.fetchValue(fromPortID));
                    sql += sqlTemp;
                } else {
                    //Invalid Parameter
                    log.error("Specified pair fromMachineID {} and fromPortID {} is invalid", ObjectIdentifier.fetchValue(fromMachineID), ObjectIdentifier.fetchValue(fromPortID));
                    Validations.check(true, retCodeConfig.getInvalidParameter());
                }
            } else {
                log.debug("No check because of Reticle Pod Stocker");
                rpsFlag = true;
            }
        } else {
            log.debug("fromMachineID is nil");
            Validations.check(ObjectIdentifier.isNotEmptyWithValue(fromPortID), retCodeConfig.getInvalidParameter());
        }
        if (ObjectIdentifier.isNotEmptyWithValue(toMachineID)) {
            log.debug("step2 - stockerMethod.stockerTypeGet");
            try {
                Outputs.ObjStockerTypeGetDROut stockerTypeGetDROut = stockerMethod.stockerTypeGetDR(objCommon, toMachineID);
                log.debug("toMachine is stocker.");
                stockerType = stockerTypeGetDROut.getStockerType();
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getUndefinedStockerType(), e.getCode())) {
                    log.debug("toMachine is not stocker.");
                    stockerType = "";
                } else {
                    throw e;
                }
            }
            if (!CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_RETICLEPOD, stockerType)) {
                log.debug("toMachine is not reticle pod stocker");
                if (ObjectIdentifier.isNotEmptyWithValue(toPortID)) {
                    if (CimBooleanUtils.isTrue(firstCondition)) {
                        firstCondition = false;
                    } else {
                        sql += " OR ";
                    }
                    sqlTemp = String.format("(CUR_EQP_ID = '%s' AND CUR_PORT_ID = '%s' ) OR (DEST_EQP_ID = '%s' AND DEST_PORT_ID = '%s')",
                            ObjectIdentifier.fetchValue(toMachineID),
                            ObjectIdentifier.fetchValue(toPortID),
                            ObjectIdentifier.fetchValue(toMachineID),
                            ObjectIdentifier.fetchValue(toPortID));
                    sql += sqlTemp;
                } else {
                    // Invalid Parameter
                    log.error("Specified the pair toMachineID {} and toPortID {} is invalid", ObjectIdentifier.fetchValue(toMachineID), ObjectIdentifier.fetchValue(toPortID));
                    Validations.check(true, retCodeConfig.getInvalidParameter());
                }
            } else {
                log.debug("No check because of Reticle Pod Stocker");
                rpsFlag = true;
            }
        } else {
            log.debug("toMachine is nil");
            Validations.check(ObjectIdentifier.isNotEmptyWithValue(toPortID), retCodeConfig.getInvalidParameter());
        }
        if (CimBooleanUtils.isTrue(firstCondition)) {
            if (CimBooleanUtils.isTrue(rpsFlag)) {
                log.debug("Reticle Pod {} Stocker {} is not checked. {} {}", ObjectIdentifier.fetchValue(toPortID), ObjectIdentifier.fetchValue(toMachineID));
                return result;
            } else {
                // Invalid Parameter
                log.error("Specified parameters are invalid. All parameter is NULL.");
                Validations.check(true, retCodeConfig.getInvalidParameter());
            }
        }
        sql += ")";
        List<Object[]> queryList = cimJpaRepository.query(sql);
        if (CimArrayUtils.isNotEmpty(queryList)) {
            for (Object[] data : queryList) {
                Infos.ReticleComponentJob componentJob = new Infos.ReticleComponentJob();
                componentJob.setRequestedTimestamp(CimObjectUtils.toString(data[0]));
                componentJob.setPriority(Long.valueOf(CimObjectUtils.toString(data[1])));
                componentJob.setReticleDispatchJobID(CimObjectUtils.toString(data[2]));
                componentJob.setReticleComponentJobID(CimObjectUtils.toString(data[3]));
                componentJob.setReticleDispatchJobRequestUserID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[4])));
                componentJob.setJobSeq(Long.valueOf(CimObjectUtils.toString(data[5])));
                componentJob.setReticleID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[6])));
                componentJob.setReticlePodID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[7])));
                componentJob.setSlotNo(Long.valueOf(CimObjectUtils.toString(data[8])));
                componentJob.setJobName(CimObjectUtils.toString(data[9]));
                componentJob.setToEquipmentID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[10])));
                componentJob.setToEquipmentCategory(CimObjectUtils.toString(data[11]));
                componentJob.setToReticlePodPortID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[12])));
                componentJob.setFromEquipmentID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[13])));
                componentJob.setFromEquipmentCategory(CimObjectUtils.toString(data[14]));
                componentJob.setFromReticlePodPortID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(data[15])));
                componentJob.setJobStatus(CimObjectUtils.toString(data[16]));
                componentJob.setJobStatusChangeTimestamp(CimObjectUtils.toString(data[17]));
                result.add(componentJob);
            }
        } else {
            return result;
        }
        if (CimArrayUtils.isNotEmpty(result)) {
            StringBuilder sb = new StringBuilder();
            if (ObjectIdentifier.isNotEmptyWithValue(reticleID)) {
                sb.append("ReticleID [")
                        .append(reticleID.getValue())
                        .append("]");
            }
            if (ObjectIdentifier.isNotEmptyWithValue(reticlePodID)) {
                if (CimStringUtils.isNotEmpty(sb.toString())) {
                    sb.append(", ");
                }
                sb.append("ReticlePodID [")
                        .append(reticlePodID.getValue())
                        .append("]");
            }
            if (ObjectIdentifier.isNotEmptyWithValue(fromMachineID)) {
                if (CimStringUtils.isNotEmpty(sb.toString())) {
                    sb.append(", ");
                }
                sb.append("FromMachineID [")
                        .append(fromMachineID.getValue())
                        .append("]");
            }
            if (ObjectIdentifier.isNotEmptyWithValue(fromPortID)) {
                if (CimStringUtils.isNotEmpty(sb.toString())) {
                    sb.append(", ");
                }
                sb.append("FromPortID [")
                        .append(fromPortID.getValue())
                        .append("]");
            }
            if (ObjectIdentifier.isNotEmptyWithValue(toMachineID)) {
                if (CimStringUtils.isNotEmpty(sb.toString())) {
                    sb.append(", ");
                }
                sb.append("ToMachineID [")
                        .append(toMachineID.getValue())
                        .append("]");
            }
            if (ObjectIdentifier.isNotEmptyWithValue(toPortID)) {
                if (CimStringUtils.isNotEmpty(sb.toString())) {
                    sb.append(", ");
                }
                sb.append("ToPortID [")
                        .append(toPortID.getValue())
                        .append("]");
            }
            Validations.check(true,result,retCodeConfigEx.getFoundInRcj(), sb.toString());
        }
        return result;
    }

    @Override
    public Outputs.ReticlePodAvailabilityCheckForXferOut reticlePodAvailabilityCheckForXfer(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, String reticleDispatchJobID) {
        Outputs.ReticlePodAvailabilityCheckForXferOut result = new Outputs.ReticlePodAvailabilityCheckForXferOut();
        /*--------------------------------*/
        /*   Get reticlePod information   */
        /*--------------------------------*/
        boolean durableOperationInfoFlag = false;
        boolean durableWipOperationInfoFlag = false;

        Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
        reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(durableOperationInfoFlag);
        reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(durableWipOperationInfoFlag);
        reticlePodDetailInfoInqParams.setReticlePodID(reticlePodID);
        log.debug("step1 - reticleMethod.reticlePodFillInTxPDQ013DR");
        Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DROut = this.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);

        if (null != reticlePodFillInTxPDQ013DROut && null != reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo()) {
            if (log.isDebugEnabled())
                log.debug("transferStatus: {}", reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus());
            String transferStatus = reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus();
            result.setTransferStatus(transferStatus);

            ObjectIdentifier machineID = null;
            Infos.ReticlePodStatusInfo reticlePodStatusInfo = reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo();
            if (ObjectIdentifier.isNotEmptyWithValue(reticlePodStatusInfo.getEquipmentID())) {
                log.debug("Reticle Pod is on EQP");
                machineID = reticlePodStatusInfo.getEquipmentID();
            } else {
                log.debug("Reticle Pod is not on EQP");
                machineID = reticlePodStatusInfo.getStockerID();
            }

            result.setMachineID(machineID);
            result.setEmptyFlag(reticlePodStatusInfo.isEmptyFlag());

            /*---------------------------------------*/
            /*   Build reticlePod slot information   */
            /*---------------------------------------*/
            int reticleCount = 0;
            int emptyCount = 0;

            List<ObjectIdentifier> reticleList = new ArrayList<>();
            List<Long> emptySlotNoList = new ArrayList<>();
            result.setEmptySlotNoList(emptySlotNoList);
            result.setReticleList(reticleList);

            if (CimArrayUtils.isNotEmpty(reticlePodStatusInfo.getStrContainedReticleInfo())) {
                for (Infos.ContainedReticleInfo reticleInfo : reticlePodStatusInfo.getStrContainedReticleInfo()) {
                    if (ObjectIdentifier.isEmptyWithValue(reticleInfo.getReticleID())) {
                        if (log.isDebugEnabled())
                            log.debug("Slot {} is empty", reticleInfo.getSlotNo());
                        emptySlotNoList.add(reticleInfo.getSlotNo());
                        emptyCount++;
                    } else {
                        if (log.isDebugEnabled())
                            log.debug("{} Slot {} is not empty", ObjectIdentifier.fetchValue(reticleInfo.getReticleID()), reticleInfo.getSlotNo());
                        reticleList.add(reticleInfo.getReticleID());
                        reticleCount++;
                    }
                }
            }
            /*---------------------------------------------------------------------*/
            /*   Get machineType and check machine status where reticlePod is on   */
            /*---------------------------------------------------------------------*/
            String machineType = null;
            log.debug("step2 - equipmentMethod.machineStateAvailabilityCheckForReticlePodXfer");
            try {
                machineType = equipmentMethod.machineStateAvailabilityCheckForReticlePodXfer(objCommon, machineID);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                    Validations.check(true, retCodeConfigEx.getReticlePodNotInTheEqp(), ObjectIdentifier.fetchValue(reticlePodID));
                } else {
                    throw e;
                }
            }
            result.setMachineType(machineType);

            if (!CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_SHELFIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_INTERMEDIATEIN, transferStatus)
                    && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_ABNORMALIN, transferStatus)) {
                log.error("TransferStatus {} of reticle is NOT *I.", transferStatus);
                Validations.check(true, retCodeConfig.getInvalidTransferState());
            }

            /*------------------------------*/
            /*   Check RDJ for reticlePod   */
            /*------------------------------*/
            ObjectIdentifier dummy = ObjectIdentifier.buildWithValue("");
            List<Infos.ReticleDispatchJob> reticleDispatchJobList = null;
            log.debug("step3 - reticleMethod.reticleDispatchJobCheckExistenceDR");
            try {
                reticleDispatchJobList = this.reticleDispatchJobCheckExistenceDR(objCommon,
                        dummy,
                        reticlePodID,
                        dummy);
            } catch (ServiceException e) {
                reticleDispatchJobList = e.getData(List.class);
                if (CimArrayUtils.getSize(reticleDispatchJobList) != 1) {
                    log.error("notInCheckForRDJ not ok");
                    throw e;
                } else if (!CimStringUtils.equals(reticleDispatchJobList.get(0).getReticleDispatchJobID(), reticleDispatchJobID)) {
                    log.error("RDJ is not match");
                    throw e;
                } else {
                    log.debug("Reserved by RDJ, but that is current RDJ. ok. {}", reticleDispatchJobID);
                }
            }

            //Check reticlePod status
            CimReticlePod aReticlePod = null;
            if (ObjectIdentifier.isEmptyWithRefKey(reticlePodID)) {
                Validations.check(ObjectIdentifier.isEmptyWithValue(reticlePodID), retCodeConfig.getNotFoundReticlePod());
                aReticlePod = durableManager.findReticlePodNamed(reticlePodID.getValue());
            } else {
                aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID.getReferenceKey());
            }
            Validations.check(null == aReticlePod, retCodeConfig.getNotFoundReticlePod());

            Boolean reticlePodIsAvailable = aReticlePod.isAvailable();
            Boolean reticlePodIsInUse = aReticlePod.isInUse();
            Boolean reticlePodIsNotAvailable = aReticlePod.isNotAvailable();

            if (CimBooleanUtils.isFalse(reticlePodIsAvailable)
                    && CimBooleanUtils.isFalse(reticlePodIsInUse)
                    && CimBooleanUtils.isFalse(reticlePodIsNotAvailable)) {
                log.error("ReticlePod state cannot for use.");
                Validations.check(true, retCodeConfigEx.getCannotUseRsp());
            }

            //Get RSP Port information
            result.setPortID(dummy);
            if (CimStringUtils.equals(result.getMachineType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                if (log.isDebugEnabled())
                    log.debug("{} Machine Type is RSP Stocker.", ObjectIdentifier.fetchValue(machineID));
                result.setPortStatus(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ);
                result.setPortAccessMode(BizConstant.SP_RETICLEPODPORT_ACCESSMODE_AUTO);
            } else {
                log.debug("Machine Type is NOT RSP Stocker");
                List<Infos.ReticlePodPortInfo> tmpReticlePodPortInfoList = null;
                if (CimStringUtils.equals(result.getMachineType(), BizConstant.SP_MACHINE_TYPE_EQP)) {
                    if (log.isDebugEnabled()){
                        log.debug("{} Machine Type is EQP.", ObjectIdentifier.fetchValue(machineID));
                    }
                    log.debug("step4 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
                    Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, machineID);
                    tmpReticlePodPortInfoList = equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
                } else if (CimStringUtils.equals(result.getMachineType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                    if (log.isDebugEnabled()){
                        log.debug("{} Machine Type is BRS. ", ObjectIdentifier.fetchValue(machineID));
                    }
                    log.debug("step5 - stockerMethod.stockerReticlePodPortInfoGetDR");
                    tmpReticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, machineID);
                } else {
                    log.error("{} Invalid Stocker Type.", ObjectIdentifier.fetchValue(machineID));
                    Validations.check(true, retCodeConfigEx.getStkTypeDifferent(), result.getMachineType());
                }
                Boolean checkFalg = false;
                if (CimArrayUtils.isNotEmpty(tmpReticlePodPortInfoList)) {
                    for (Infos.ReticlePodPortInfo reticlePodPortInfo : tmpReticlePodPortInfoList) {
                        if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                            if (log.isDebugEnabled())
                                log.debug("ReticlePod found on RSPPort {}", ObjectIdentifier.fetchValue(reticlePodPortInfo.getReticlePodPortID()));
                            result.setPortID(reticlePodPortInfo.getReticlePodPortID());
                            result.setPortStatus(reticlePodPortInfo.getPortStatus());
                            result.setPortAccessMode(reticlePodPortInfo.getAccessMode());
                            checkFalg = true;
                            break;
                        }
                    }
                }
                log.debug("Specified RSP is not on machine's RSP Port.");
                Validations.check(CimBooleanUtils.isFalse(checkFalg), retCodeConfigEx.getRspNotLoaded(), ObjectIdentifier.fetchValue(reticlePodID), ObjectIdentifier.fetchValue(machineID));

                if (!CimStringUtils.equals(result.getPortAccessMode(), BizConstant.SP_RETICLEPODPORT_ACCESSMODE_AUTO)) {
                    log.error("RSP Port's access mode is not Auto.");
                    Validations.check(true, retCodeConfigEx.getEqpPortAccessMode(),
                            ObjectIdentifier.fetchValue(machineID),
                            ObjectIdentifier.fetchValue(result.getPortID()),
                            result.getPortAccessMode());
                }
                if (!CimStringUtils.equals(result.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP)
                        && !CimStringUtils.equals(result.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)) {
                    log.error("RSP Port's status is not LoadComp or UnloadReq.");
                    Validations.check(true, retCodeConfigEx.getInvalidMachinePortState(),
                            ObjectIdentifier.fetchValue(machineID),
                            ObjectIdentifier.fetchValue(result.getPortID()),
                            result.getPortStatus());
                }
            }
        }
        return result;
    }

    @Override
    public void reticleDispatchJobInsertDR(Infos.ObjCommon objCommon, List<Infos.ReticleDispatchJob> strReticleDispatchJobList) {
        Optional.ofNullable(strReticleDispatchJobList).ifPresent(list -> list.forEach(data -> {
            CimRALDO cimRALDO = new CimRALDO();
            cimRALDO.setStationID(data.getDispatchStationID());
            cimRALDO.setReqTime(CimDateUtils.convertToOrInitialTime(data.getRequestedTimestamp()));
            cimRALDO.setRtclID(ObjectIdentifier.fetchValue(data.getReticleID()));
            cimRALDO.setRtclPodID(ObjectIdentifier.fetchValue(data.getReticlePodID()));
            cimRALDO.setRdjID(data.getReticleDispatchJobID());
            cimRALDO.setFromEqpID(ObjectIdentifier.fetchValue(data.getFromEquipmentID()));
            cimRALDO.setFromEqpCategory(data.getFromEquipmentCategory());
            cimRALDO.setToEqpID(ObjectIdentifier.fetchValue(data.getToEquipmentID()));
            cimRALDO.setToEqpCategory(data.getToEquipmentCategory());
            cimRALDO.setPriority(data.getPriority().intValue());
            cimRALDO.setReqUserID(ObjectIdentifier.fetchValue(data.getRequestUserID()));
            cimRALDO.setJobStatus(data.getJobStatus());
            cimRALDO.setJobStatChgTime(CimDateUtils.convertToOrInitialTime(data.getJobStatusChangeTimestamp()));
            cimRALDO.setStatusWarnedFlag(false);//0
            cimJpaRepository.save(cimRALDO);
        }));
    }

    @Override
    public void reticleReticlePodReserve(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID, ObjectIdentifier toMachineID) {

        /*-------------------------------------*/
        /*   Get object reference of reticle   */
        /*-------------------------------------*/
        log.debug("get ReticleID (aReticle)");
        CimProcessDurable aReticle = null;
        if (ObjectIdentifier.isEmptyWithRefKey(reticleID)) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(reticleID), retCodeConfig.getNotFoundReticle());
            ProcessDurable aTmpDurable = durableManager.findProcessDurableNamed(reticleID.getValue());
            aReticle = (CimProcessDurable) aTmpDurable;
        } else {
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID.getReferenceKey());
        }
        Validations.check(null == aReticle, retCodeConfig.getNotFoundReticle());

        log.debug("get reticlePodID (aPosReticlePod)");
        CimReticlePod aPosReticlePod = null;
        if (ObjectIdentifier.isEmptyWithRefKey(reticlePodID)) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(reticlePodID), retCodeConfig.getNotFoundReticlePod());
            aPosReticlePod = durableManager.findReticlePodNamed(reticlePodID.getValue());
        } else {
            aPosReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID.getReferenceKey());
        }
        Validations.check(null == aPosReticlePod, retCodeConfig.getNotFoundReticlePod());

        log.debug("get objCommon.User.userID (aPerson)");
        CimPerson aPerson = null;
        ObjectIdentifier personID = objCommon.getUser().getUserID();
        if (ObjectIdentifier.isEmptyWithRefKey(personID)) {
            aPerson = personManager.findPersonNamed(ObjectIdentifier.fetchValue(personID));
        } else {
            aPerson = baseCoreFactory.getBO(CimPerson.class, personID.getReferenceKey());
        }
        Validations.check(null == aPerson, retCodeConfig.getNotFoundPerson());

        /*--------------------------------------------------*/
        /*   Get object reference of equipment or stocker   */
        /*--------------------------------------------------*/
        CimMachine aPosMachine = null;
        CimStorageMachine aPosStorageMachine = null;
        if (ObjectIdentifier.isNotEmptyWithValue(toMachineID)) {
            /*----------------------*/
            /*   Get machine type   */
            /*----------------------*/
            Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, toMachineID);
            if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
                log.debug("bStorageMachineFlag == FALSE");
                if (ObjectIdentifier.isEmptyWithRefKey(toMachineID)) {
                    aPosMachine = machineManager.findMachineNamed(ObjectIdentifier.fetchValue(toMachineID));
                } else {
                    aPosMachine = baseCoreFactory.getBO(CimMachine.class, toMachineID.getReferenceKey());
                }
                Validations.check(null == aPosMachine, retCodeConfig.getNotFoundEqp());
            } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)
                    || CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.debug("stockerType = BRS || stockerType = RPS");
                if (ObjectIdentifier.isEmptyWithRefKey(toMachineID)) {
                    aPosStorageMachine = machineManager.findStorageMachineNamed(ObjectIdentifier.fetchValue(toMachineID));
                } else {
                    aPosStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, toMachineID.getReferenceKey());
                }
                Validations.check(null == aPosStorageMachine, retCodeConfig.getNotFoundStocker());
            } else {
                log.debug("in-para toMachineID {} is not equipment or bareReticleStocker", ObjectIdentifier.fetchValue(toMachineID));
                Validations.check(true, retCodeConfig.getInvalidParameter());
            }
            if (null == aReticle) {
                log.debug("aReticle is null");
                Validations.check(true, retCodeConfig.getNotFoundReticle());
            }
            /*------------------------------------------------*/
            /*   Set reserve related information to reticle   */
            /*------------------------------------------------*/
            if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
                log.debug("bStorageMachineFlag == FALSE");
                aReticle.setTransferDestinationEquipment(aPosMachine);
            } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.debug("stockerType = BRS");
                aReticle.setTransferDestinationStocker(aPosStorageMachine);
            }
        }
        aReticle.setTransferReservedReticlePod(aPosReticlePod);
        aReticle.setTransferReserveUser(aPerson);
        aReticle.setTransferReservedTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
    }

    @Override
    public void reticleEventQueuePutDR(Infos.ObjCommon objCommon, Infos.ReticleEventRecord strReticleEventRecord) {
        CimRTCLEventDO eventDO = new CimRTCLEventDO();
        eventDO.setEventTime(CimDateUtils.convertToOrInitialTime(strReticleEventRecord.getEventTime()));
        eventDO.setRdjID(strReticleEventRecord.getReticleDispatchJobID());
        eventDO.setRcjID(strReticleEventRecord.getReticleComponentJobID());
        eventDO.setRspstkID(ObjectIdentifier.fetchValue(strReticleEventRecord.getReticlePodStockerID()));
        eventDO.setBrstkID(ObjectIdentifier.fetchValue(strReticleEventRecord.getBareReticleStockerID()));
        eventDO.setRscID(ObjectIdentifier.fetchValue(strReticleEventRecord.getResourceID()));
        eventDO.setEqpID(ObjectIdentifier.fetchValue(strReticleEventRecord.getEquipmentID()));
        eventDO.setPortID(ObjectIdentifier.fetchValue(strReticleEventRecord.getRSPPortID()));
        eventDO.setRtclPodID(ObjectIdentifier.fetchValue(strReticleEventRecord.getReticlePodID()));
        eventDO.setRtclPodEvent(strReticleEventRecord.getRSPPortEvent());
        eventDO.setRtclID(ObjectIdentifier.fetchValue(strReticleEventRecord.getReticleID()));
        eventDO.setRtclJob(strReticleEventRecord.getReticleJobEvent());
        cimJpaRepository.save(eventDO);
    }

    @Override
    public void reticleReservationCheck(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        log.info("in para reticleID : {}", ObjectIdentifier.fetchValue(reticleID));

        CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        Boolean reticleReserved = aReticle.isReserved();

        Validations.check(reticleReserved, retCodeConfigEx.getAlreadyReservedReticle());

        List<CimControlJob> aReticleControlJobList = aReticle.allReservedControlJobs();

        int rtclCtrlJobCount = CimArrayUtils.getSize(aReticleControlJobList);
        if (rtclCtrlJobCount > 0) {
            //--------------------------------------------------
            //   Get OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS
            //--------------------------------------------------
            String environmentVariableValueStr = StandardProperties.OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS.getValue();
            int retrieveReticleDuringLotProcFlag = CimStringUtils.isEmpty(environmentVariableValueStr) ? 0 : Integer.parseInt(environmentVariableValueStr);
            log.info("Env OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS : {}", retrieveReticleDuringLotProcFlag);
            if (1 == retrieveReticleDuringLotProcFlag) {
                log.info("Check if reticle has reserve control job");
                for (int rtclCtrlJobIndex = 0; rtclCtrlJobIndex < rtclCtrlJobCount; rtclCtrlJobIndex++) {
                    log.info("rtclCtrlJobIndex : {}", rtclCtrlJobIndex);

                    if (null == aReticleControlJobList.get(rtclCtrlJobIndex)) {
                        log.info("nil object");
                        continue;
                    }

                    //--------------------------------------------------
                    //  Get control job status
                    //--------------------------------------------------
                    String controlJobStatus = ObjectIdentifier.fetchValue(aReticleControlJobList.get(rtclCtrlJobIndex).getControlJobID());
                    Validations.check(CimStringUtils.equals(controlJobStatus, BizConstant.SP_CONTROLJOBSTATUS_CREATED), retCodeConfigEx.getRtclHasCtrljob(), reticleID.getValue());
                }
            } else {
                log.info("Reticle has control job.: {}", rtclCtrlJobCount);
                Validations.check(retCodeConfigEx.getRtclHasCtrljob(), reticleID.getValue());
            }
        }

    }

    @Override
    public void reticlePodVacantSlotPositionCheck(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, List<Infos.MoveReticles> moveReticlesList) {

        /****************************/
        /*  Get Reticle Pod Object  */
        /****************************/
        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);

        /**************************************/
        /*  Get Reticle Pod all slot Numbers  */
        /**************************************/
        List<Integer> slotNumberList = aReticlePod.getPositions();

        int nSlotCount = CimArrayUtils.getSize(slotNumberList);

        /*********************************/
        /*  Check empty slot existenct   */
        /*********************************/
        List<Boolean> slotIsEmptyList = new ArrayList<>();
        int i = 0;
        for (i = 0; i < nSlotCount; i++) {
            slotIsEmptyList.add(true);
        }

        Outputs.ObjReticlePodReticleListGetDROut strReticlePod_reticleList_GetDR_out = this.reticlePodReticleListGetDR(objCommon, reticlePodID);

        int retSlotCount = CimArrayUtils.getSize(strReticlePod_reticleList_GetDR_out.getSlotPosition());
        int retRtclCount = CimArrayUtils.getSize(strReticlePod_reticleList_GetDR_out.getReticleID());
        for (i = 0; i < retSlotCount; i++) {
            if (i < retRtclCount) {
                if (ObjectIdentifier.isNotEmpty(strReticlePod_reticleList_GetDR_out.getReticleID().get(i))) {
                    int z = 0;
                    for (int j = 0; j < nSlotCount; j++) {
                        if (slotNumberList.get(j).intValue() == strReticlePod_reticleList_GetDR_out.getSlotPosition().get(i)) {
                            log.info("slot is not empty : {}", slotNumberList.get(j));
                            slotIsEmptyList.add(false);
                            z++;
                            break;
                        }
                    }
                    Validations.check(z == nSlotCount, retCodeConfigEx.getNotFoundSlot());
                }
            }
        }

        // check reticle destination slot of reticlePod is available(empty) or not.
        for (i = 0; i < CimArrayUtils.getSize(moveReticlesList); i++) {
            Boolean slotFound = false;
            for (int j = 0; j < nSlotCount; j++) {
                if (Objects.equals(moveReticlesList.get(i).getSlotNumber(), slotNumberList.get(j))) {
                    slotFound = true;
                    if (slotIsEmptyList.get(j)) {
                        slotIsEmptyList.set(j, false);
                    } else {
                        Validations.check(retCodeConfig.getNotEmptyReticlePodPosition(), reticlePodID.getValue(), moveReticlesList.get(i).getSlotNumber());
                    }
                }
            }
            Validations.check(!slotFound, retCodeConfigEx.getNotFoundSlot());
        }
    }

    @Override
    public void reticleReticlePodReserveCancel(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Boolean bCheckToMachineFlag) {
        /*-------------------------------------*/
        /*   Get object reference of reticle   */
        /*-------------------------------------*/
        CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);

        /*---------------------------------*/
        /*   Reset reticle's destination   */
        /*---------------------------------*/
        Validations.check(null == aReticle, retCodeConfig.getNotFoundReticle());

        Boolean bReserveCancelFlag = false;
        if (bCheckToMachineFlag) {
            CimMachine aPosMachine = aReticle.getTransferDestinationEquipment();
            CimStorageMachine aPosStorageMachine = aReticle.getTransferDestinationStocker();
            if (null == aPosMachine && null == aPosStorageMachine) {
                bReserveCancelFlag = true;
            }
        } else {
            aReticle.setTransferDestinationEquipment(null);
            aReticle.setTransferDestinationStocker(null);
            bReserveCancelFlag = true;
        }

        if (bReserveCancelFlag) {
            aReticle.setTransferReservedReticlePod(null);
            aReticle.setTransferReservedReticlePodSlotNumber(0l);
            aReticle.setTransferReserveUser(null);
            aReticle.setTransferReservedTimeStamp(null);
        }
    }

    @Override
    public String reticlePodTransferStateGetDR(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {
        Object[] objects = cimJpaRepository.queryOne("SELECT XFER_STATE FROM OMRTCLPOD WHERE RTCLPOD_ID = ?1",
                ObjectIdentifier.fetchValue(reticlePodID));
        return objects == null ? "" : (String) objects[0];
    }

    @Override
    public void reticlePodAvailabilityCheck(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {

        log.info("in para reticlePodID {}", reticlePodID);

        /****************************/
        /*  Get Reticle Pod Object  */
        /****************************/
        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);
        Validations.check(aReticlePod == null, retCodeConfig.getNotFoundReticlePod());
        Validations.check(CimBooleanUtils.isFalse(aReticlePod.isAvailable()) && CimBooleanUtils.isFalse(aReticlePod.isInUse()), retCodeConfigEx.getCannotUseRsp());
    }

    @Override
    public void reticleJobStatusUpdateByRequestDR(Infos.ObjCommon objCommon, String reticleDispatchJobID, String reticleComponentJobID, boolean jobSuccessFlag) {
        String fsralJobStatus, fsrelJobStatus;
        if (jobSuccessFlag) {
            log.info("TRUE == jobSuccessFlag");
            fsralJobStatus = BizConstant.SP_RDJ_STATUS_EXECUTING;
            fsrelJobStatus = BizConstant.SP_RCJ_STATUS_EXECUTING;
        } else {
            log.info("TRUE != jobSuccessFlag");
            fsralJobStatus = BizConstant.SP_RDJ_STATUS_ERROR;
            fsrelJobStatus = BizConstant.SP_RCJ_STATUS_ERROR;
        }

        CimRALDO cimRALExam = new CimRALDO();
        cimRALExam.setRdjID(reticleDispatchJobID);
        cimJpaRepository.findOne(Example.of(cimRALExam)).ifPresent(cimRAL -> {
            cimRAL.setJobStatus(fsralJobStatus);
            cimRAL.setJobStatChgTime(objCommon.getTimeStamp().getReportTimeStamp());
            cimJpaRepository.save(cimRAL);
        });

        CimRELDO cimRELExam = new CimRELDO();
        cimRELExam.setRcjId(reticleComponentJobID);
        cimJpaRepository.findOne(Example.of(cimRELExam)).ifPresent(cimREL -> {
            cimREL.setJobStatus(fsrelJobStatus);
            cimREL.setJobStatChgTime(objCommon.getTimeStamp().getReportTimeStamp());
            cimJpaRepository.save(cimREL);
        });
    }

    @Override
    public void reticleDispatchJobUpdateDR(Infos.ObjCommon objCommon, Infos.ReticleDispatchJob strReticleDispatchJob) {
        CimRALDO cimRALExam = new CimRALDO();
        cimRALExam.setRdjID(strReticleDispatchJob.getReticleDispatchJobID());
        List<CimRALDO> resultList = cimJpaRepository.findAll(Example.of(cimRALExam));
        if (CimArrayUtils.isNotEmpty(resultList)) {
            resultList.forEach(entity -> {
                entity.setStationID(strReticleDispatchJob.getDispatchStationID());
                entity.setReqTime(CimDateUtils.convertToOrInitialTime(strReticleDispatchJob.getRequestedTimestamp()));
                entity.setRtclID(ObjectIdentifier.fetchValue(strReticleDispatchJob.getReticleID()));
                entity.setRtclPodID(ObjectIdentifier.fetchValue(strReticleDispatchJob.getReticlePodID()));
                entity.setRdjID(strReticleDispatchJob.getReticleDispatchJobID());
                entity.setFromEqpID(ObjectIdentifier.fetchValue(strReticleDispatchJob.getFromEquipmentID()));
                entity.setFromEqpCategory(strReticleDispatchJob.getFromEquipmentCategory());
                entity.setToEqpID(ObjectIdentifier.fetchValue(strReticleDispatchJob.getToEquipmentID()));
                entity.setToEqpCategory(strReticleDispatchJob.getToEquipmentCategory());
                entity.setPriority(strReticleDispatchJob.getPriority().intValue());
                entity.setReqUserID(ObjectIdentifier.fetchValue(strReticleDispatchJob.getRequestUserID()));
                entity.setJobStatus(strReticleDispatchJob.getJobStatus());
                entity.setJobStatChgTime(CimDateUtils.convertToOrInitialTime(strReticleDispatchJob.getRequestedTimestamp()));
                cimJpaRepository.save(entity);
            });
        } else {
            Validations.check(true, retCodeConfigEx.getRdjNotFound());
        }
    }

    @Override
    public List<Infos.ReticleDispatchJob> reticleDispatchJobListGetForUpdateDR(Infos.ObjCommon objCommon, String reticleDispatchJobID) {
        /*--------------------------------------------*/
        /*   Get reticle list into output structure   */
        /*--------------------------------------------*/
        CimRALDO cimRALExam = new CimRALDO();
        if (CimStringUtils.isNotEmpty(reticleDispatchJobID)) {
            cimRALExam.setRdjID(reticleDispatchJobID);
        }
        return cimJpaRepository.findAll(Example.of(cimRALExam)).stream().map(data -> {
            Infos.ReticleDispatchJob job = new Infos.ReticleDispatchJob();
            job.setDispatchStationID(data.getStationID());
            job.setRequestedTimestamp(String.valueOf(data.getReqTime()));
            job.setReticleID(ObjectIdentifier.buildWithValue(data.getRtclID()));
            job.setReticlePodID(ObjectIdentifier.buildWithValue(data.getRtclPodID()));
            job.setReticleDispatchJobID(String.valueOf(data.getRdjID()));
            job.setFromEquipmentID(ObjectIdentifier.buildWithValue(data.getFromEqpID()));
            job.setFromEquipmentCategory(data.getFromEqpCategory());
            job.setToEquipmentID(ObjectIdentifier.buildWithValue(data.getToEqpID()));
            job.setToEquipmentCategory(data.getToEqpCategory());
            job.setPriority(data.getPriority().longValue());
            job.setRequestUserID(ObjectIdentifier.buildWithValue(data.getReqUserID()));
            job.setJobStatus(data.getJobStatus());
            job.setJobStatusChangeTimestamp(String.valueOf(data.getJobStatChgTime()));
            return job;
        }).collect(Collectors.toList());
    }

    @Override
    public Outputs.ReticleReticlePodGetForXferOut reticleReticlePodGetForXfer(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        Outputs.ReticleReticlePodGetForXferOut result = new Outputs.ReticleReticlePodGetForXferOut();
        ObjectIdentifier dummyID = null;
        log.info("in para reticleID: {}", ObjectIdentifier.fetchValue(reticleID));

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.info("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        int usePodLen = 0;
        result.setReticleID(reticleID);
        //=============================================================================================
        // if reticle is in FSRAL and it have relation of reticlePod, just that pod will be replied.
        //=============================================================================================
        log.info("step1- reticleMethod.reticleDispatchJobListGetDR");
        List<Infos.ReticleDispatchJob> reticleDispatchJobList = this.reticleDispatchJobListGetDR(objCommon, "");
        if (CimArrayUtils.isNotEmpty(reticleDispatchJobList)) {
            for (Infos.ReticleDispatchJob reticleDispatchJob : reticleDispatchJobList) {
                if (ObjectIdentifier.equalsWithValue(reticleDispatchJob.getReticleID(), reticleID)
                        && ObjectIdentifier.isNotEmptyWithValue(reticleDispatchJob.getReticlePodID())) {
                    log.info("Found reticlePod in OSRTCLACTLIST {}", ObjectIdentifier.fetchValue(reticleDispatchJob.getReticlePodID()));
                    log.info("step2- reticleMethod.reticlePodCandidateInfoGet");
                    List<Infos.CandidateReticlePod> candidateReticlePodList = this.reticlePodCandidateInfoGet(objCommon, reticleDispatchJob.getReticlePodID(), false);
                    result.setStrCandidateReticlePods(candidateReticlePodList);
                    Validations.check(CimArrayUtils.isEmpty(result.getStrCandidateReticlePods()), retCodeConfigEx.getRdjIncomplete());
                    return result;
                }
            }
        }
        //=========================================================================
        // get reticle info
        //=========================================================================
        log.info("step3- reticleMethod.reticleDetailInfoGetDR");
        Results.ReticleDetailInfoInqResult reticleDetailInfoInqResult = this.reticleDetailInfoGetDR(objCommon,
                reticleID,
                false,
                false);

        //=========================================================================
        // if reticle is in reticle pod, just that reticle pod will be replied.
        //=========================================================================
        if (null != reticleDetailInfoInqResult && null != reticleDetailInfoInqResult.getReticleStatusInfo()) {
            Infos.ReticleStatusInfo reticleStatusInfo = reticleDetailInfoInqResult.getReticleStatusInfo();
            if (ObjectIdentifier.isNotEmptyWithValue(reticleStatusInfo.getReticlePodID())) {
                log.info("reticle is in reticlePod.");
                log.info("reticlePodID {}", ObjectIdentifier.fetchValue(reticleStatusInfo.getReticlePodID()));
                Infos.CandidateReticlePod candidateReticlePod = new Infos.CandidateReticlePod();
                result.setStrCandidateReticlePods(Arrays.asList(candidateReticlePod));
                candidateReticlePod.setReticlePodID(reticleStatusInfo.getReticlePodID());
                if (CimArrayUtils.isNotEmpty(result.getStrCandidateReticlePods())) {
                    return result;
                }
            }

            ObjectIdentifier machineID = null;
            //=========================================================================
            // If reticle is in equipment,
            //=========================================================================
            if (ObjectIdentifier.isNotEmptyWithValue(reticleStatusInfo.getEquipmentID())) {
                machineID = reticleStatusInfo.getEquipmentID();
                log.info("equipmentID not null");
                log.info("equipmentID {}", ObjectIdentifier.fetchValue(reticleStatusInfo.getEquipmentID()));

                //=========================================================================
                // Get reticle pod on equipment
                //=========================================================================
                log.info("step4- equipmentMethod.equipmentReticlePodPortInfoGetDR");
                Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, reticleStatusInfo.getEquipmentID());

                //=========================================================================
                // check that equipment has non-reserved empty reticle pod on port or not.
                //=========================================================================
                if (null != equipmentReticlePodPortInfoGetDROut && null != equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                    for (Infos.ReticlePodPortInfo reticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                        if (ObjectIdentifier.isNotEmptyWithValue(reticlePodPortInfo.getLoadedReticlePodID())
                                && CimStringUtils.equals(reticlePodPortInfo.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                                && CimStringUtils.equals(reticlePodPortInfo.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP)) {
                            log.info("Found reticlePod in same equipment. reticlePodPort is OnlineMode and LoadComp status.");
                            log.info("step5- reticleMethod.reticlePodCandidateInfoGet");
                            List<Infos.CandidateReticlePod> candidateReticlePodList = this.reticlePodCandidateInfoGet(objCommon, reticlePodPortInfo.getLoadedReticlePodID(), true);
                            result.setStrCandidateReticlePods(candidateReticlePodList);
                            if (CimArrayUtils.isNotEmpty(result.getStrCandidateReticlePods())) {
                                return result;
                            }
                        }
                    }
                }
            }
            //=========================================================================
            // If reticle is in stocker (must be bare reticle stocker)
            //=========================================================================
            if (ObjectIdentifier.isNotEmptyWithValue(reticleStatusInfo.getStockerID())) {
                machineID = reticleStatusInfo.getStockerID();
                log.info("step6- equipmentMethod.machineTypeGet");
                Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, reticleStatusInfo.getStockerID());
                if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                    log.info("step7- stockerMethod.stockerReticlePodPortInfoGetDR");
                    List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, machineTypeGetOut.getStockerID());
                    //=========================================================================
                    // check that equipment has non-reserved empty reticle pod on port or not.
                    //=========================================================================
                    if (CimArrayUtils.isNotEmpty(reticlePodPortInfoList)) {
                        for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfoList) {
                            if (ObjectIdentifier.isNotEmptyWithValue(reticlePodPortInfo.getLoadedReticlePodID())
                                    && CimStringUtils.equals(reticlePodPortInfo.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                                    && CimStringUtils.equals(reticlePodPortInfo.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP)) {
                                log.info("Found reticlePod in same equipment. reticlePodPort is OnlineMode and LoadComp status.");
                                log.info("step8- reticleMethod.reticlePodCandidateInfoGet");
                                List<Infos.CandidateReticlePod> candidateReticlePodList = this.reticlePodCandidateInfoGet(objCommon, reticlePodPortInfo.getLoadedReticlePodID(), true);
                                result.setStrCandidateReticlePods(candidateReticlePodList);
                                if (CimArrayUtils.isNotEmpty(result.getStrCandidateReticlePods())) {
                                    return result;
                                }
                            }
                        }
                    }
                }
            }
            //======================================================
            // If reicle is in Equipment. check associate stocker
            //======================================================
            if (ObjectIdentifier.isNotEmptyWithValue(reticleStatusInfo.getEquipmentID())) {
                //================================================
                // check stocker which has relation in equipment
                //================================================
                log.info("step9- equipmentMethod.equipmentStockerInfoGetDR");
                Infos.EqpStockerInfo eqpStockerInfo = equipmentMethod.equipmentStockerInfoGetDR(objCommon, reticleStatusInfo.getEquipmentID());
                if (null != eqpStockerInfo && CimArrayUtils.isNotEmpty(eqpStockerInfo.getEqpStockerStatusList())) {
                    for (Infos.EqpStockerStatus eqpStockerStatus : eqpStockerInfo.getEqpStockerStatusList()) {
                        if (CimStringUtils.equals(eqpStockerStatus.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                            log.info("This reticlePodStocker {} associate to the equipment.", ObjectIdentifier.fetchValue(eqpStockerStatus.getStockerID()));
                            log.info("step10- equipmentMethod.equipmentCheckAvail");
                            try {
                                equipmentMethod.equipmentCheckAvail(objCommon, eqpStockerStatus.getStockerID());
                            } catch (ServiceException e) {
                                if (Validations.isEquals(retCodeConfig.getEquipmentNotAvailableStat(), e.getCode())) {
                                    log.info("This reticlePodStocker {} is not Available state.", ObjectIdentifier.fetchValue(eqpStockerStatus.getStockerID()));
                                } else {
                                    throw e;
                                }
                            }
                            //get reticlePod list in the reticlePodStocker
                            Params.ReticlePodListInqParams reticlePodListInqParams = new Params.ReticlePodListInqParams();
                            reticlePodListInqParams.setUser(objCommon.getUser());
                            reticlePodListInqParams.setStockerID(eqpStockerStatus.getStockerID());
                            reticlePodListInqParams.setEmptyFlag(true);
                            reticlePodListInqParams.setMaxRetrieveCount(0);
                            List<Infos.ReticlePodListInfo> reticlePodListInfoList = new ArrayList<>();
                            log.info("step13- reticleMethod.reticlePodFillInTxPDQ012DR");
                            try {
                                reticlePodListInfoList = this.reticlePodFillInTxPDQ012DR(objCommon, reticlePodListInqParams);
                            } catch (ServiceException e) {
                                if (Validations.isEquals(retCodeConfig.getNotFoundReticlePod(), e.getCode())) {
                                    continue;
                                } else {
                                    throw e;
                                }
                            }
                            if (CimArrayUtils.isNotEmpty(reticlePodListInfoList)) {
                                for (Infos.ReticlePodListInfo reticlePodListInfo : reticlePodListInfoList) {
                                    log.info("Found retilcePod in reticlePodStocker.");
                                    ObjectIdentifier reticlePodID = reticlePodListInfo.getReticlePodID();
                                    log.info("step14- reticleMethod.reticlePodCandidateInfoGet");
                                    List<Infos.CandidateReticlePod> candidateReticlePodList = this.reticlePodCandidateInfoGet(objCommon, reticlePodID, true);
                                    result.setStrCandidateReticlePods(candidateReticlePodList);
                                    if (CimArrayUtils.isNotEmpty(result.getStrCandidateReticlePods())) {
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (ObjectIdentifier.isEmptyWithValue(machineID)) {
                log.info("This reticle {} current status is invalid.", ObjectIdentifier.fetchValue(reticleID));
                Validations.check(true, retCodeConfig.getInvalidReticleStat(), ObjectIdentifier.fetchValue(reticleID), reticleStatusInfo.getTransferStatus());
            }
            //=============================================================================================
            // Get workArea. If reticlePodStocker exist in the workArea, serch reticlePod in the stocker.
            //=============================================================================================
            else {
                log.info("step15- equipmentMethod.machineTypeGet");
                Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, machineID);

                Infos.EquipmentListInfoGetDRIn equipmentListInfoGetDRIn = new Infos.EquipmentListInfoGetDRIn();
                equipmentListInfoGetDRIn.setWorkArea(machineTypeGetOut.getAreaID());
                log.info("step16- equipmentMethod.equipmentListInfoGetDR");
                Infos.EquipmentListInfoGetDROut equipmentListInfoGetDROut = equipmentMethod.equipmentListInfoGetDR(objCommon, equipmentListInfoGetDRIn);

                if (null != equipmentListInfoGetDROut) {
                    if (CimArrayUtils.isNotEmpty(equipmentListInfoGetDROut.getStrAreaStocker())) {
                        for (Infos.AreaStocker strAreaStocker : equipmentListInfoGetDROut.getStrAreaStocker()) {
                            if (CimStringUtils.equals(strAreaStocker.getStockerID(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                                log.info("The reticlePodStocker {} which it same workArea of reticle", ObjectIdentifier.fetchValue(strAreaStocker.getStockerID()));
                                log.info("step17- equipmentMethod.equipmentCheckAvail");
                                try {
                                    equipmentMethod.equipmentCheckAvail(objCommon, strAreaStocker.getStockerID());
                                } catch (ServiceException e) {
                                    if (Validations.isEquals(retCodeConfig.getEquipmentNotAvailableStat(), e.getCode())) {
                                        continue;
                                    } else {
                                        throw e;
                                    }
                                }
                                Params.ReticlePodListInqParams reticlePodListInqParams = new Params.ReticlePodListInqParams();
                                reticlePodListInqParams.setUser(objCommon.getUser());
                                reticlePodListInqParams.setStockerID(strAreaStocker.getStockerID());
                                reticlePodListInqParams.setEmptyFlag(true);
                                reticlePodListInqParams.setMaxRetrieveCount(0);

                                List<Infos.ReticlePodListInfo> reticlePodListInfoList = null;
                                log.info("step17- reticleMethod.reticlePodFillInTxPDQ012DR");
                                try {
                                    reticlePodListInfoList = this.reticlePodFillInTxPDQ012DR(objCommon, reticlePodListInqParams);
                                } catch (ServiceException e) {
                                    if (Validations.isEquals(retCodeConfig.getNotFoundReticlePod(), e.getCode())) {
                                        continue;
                                    } else {
                                        throw e;
                                    }
                                }
                                if (CimArrayUtils.isNotEmpty(reticlePodListInfoList)) {
                                    for (Infos.ReticlePodListInfo reticlePodListInfo : reticlePodListInfoList) {
                                        ObjectIdentifier reticlePodID = reticlePodListInfo.getReticlePodID();
                                        log.info("Found retilcePod {} in reticlePodStocker.", ObjectIdentifier.fetchValue(reticlePodID));
                                        log.info("step17- reticleMethod.reticlePodCandidateInfoGet");
                                        List<Infos.CandidateReticlePod> candidateReticlePodList = this.reticlePodCandidateInfoGet(objCommon, reticlePodID, true);
                                        result.setStrCandidateReticlePods(candidateReticlePodList);
                                        if (CimArrayUtils.isNotEmpty(result.getStrCandidateReticlePods())) {
                                            return result;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //============================================================================================
                    // If no empty pod is listed by above selectionlogic, search other RSP stocker same location
                    //============================================================================================
                    Boolean isStorageBool = false;
                    Machine aMachine = null;
                    if (ObjectIdentifier.isEmptyWithRefKey(machineID)) {
                        Validations.check(ObjectIdentifier.isEmptyWithValue(machineID), retCodeConfig.getNotFoundEqp(), "*****");
                        aMachine = machineManager.findMachineNamed(machineID.getValue());
                        if (null == aMachine) {
                            aMachine = machineManager.findStorageMachineNamed(ObjectIdentifier.fetchValue(machineID));
                            Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(machineID));
                        }
                        isStorageBool = aMachine.isStorageMachine();
                    } else {
                        aMachine = baseCoreFactory.getBO(Machine.class, machineID.getReferenceKey());
                        Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(machineID));
                        isStorageBool = aMachine.isStorageMachine();
                    }
                    CimArea aPosArea = null;
                    if (CimBooleanUtils.isTrue(isStorageBool)) {
                        log.info("TRUE == isStorageBool");
                        CimStorageMachine aPosStorageMacine = (CimStorageMachine) aMachine;
                        Validations.check(null == aPosStorageMacine, retCodeConfigEx.getUnexpectedNilObject());
                        aPosArea = aPosStorageMacine.getWorkArea();
                    } else {
                        CimMachine aPosMachine = (CimMachine) aMachine;
                        Validations.check(null == aPosMachine, retCodeConfigEx.getUnexpectedNilObject());
                        aPosArea = aPosMachine.getWorkArea();
                    }
                    if (null == aPosArea) {
                        log.info("aPosArea is null");
                        Validations.check(true, retCodeConfigEx.getNotFoundWorkarea(), "*****");
                    }
                    Area anArea = aPosArea.getSuperArea();
                    Validations.check(null == anArea, retCodeConfigEx.getNotFoundArea(), "*****");

                    CimArea aLocation = (CimArea) anArea;
                    ObjectIdentifier locationID = ObjectIdentifier.buildWithValue(aLocation.getIdentifier());
                    log.info("location is {}", ObjectIdentifier.fetchValue(locationID));
                    log.info("step18- areaMethod.areaGetByLocationID");
                    List<ObjectIdentifier> areaGetByLocationIDs = areaMethod.areaGetByLocationID(objCommon, ObjectIdentifier.fetchValue(locationID));

                    if (CimArrayUtils.isNotEmpty(areaGetByLocationIDs)) {
                        for (ObjectIdentifier workAreaID : areaGetByLocationIDs) {
                            Infos.EquipmentListInfoGetDRIn equipmentListInfoGetDRInput = new Infos.EquipmentListInfoGetDRIn();
                            equipmentListInfoGetDRInput.setWorkArea(workAreaID);
                            log.info("step19- equipmentMethod.equipmentListInfoGetDR");
                            Infos.EquipmentListInfoGetDROut equipmentListInfoGetDROutOther = equipmentMethod.equipmentListInfoGetDR(objCommon, equipmentListInfoGetDRInput);
                            if (null != equipmentListInfoGetDROutOther && CimArrayUtils.isEmpty(equipmentListInfoGetDROutOther.getStrAreaStocker())) {
                                log.info("Not found stocker in this workArea {}", ObjectIdentifier.fetchValue(workAreaID));
                                continue;
                            }
                            if (ObjectIdentifier.equalsWithValue(machineTypeGetOut.getAreaID(), workAreaID)) {
                                log.info("This workArea {} is current workArea {}. this workArea skip.", ObjectIdentifier.fetchValue(machineTypeGetOut.getAreaID()),
                                        ObjectIdentifier.fetchValue(workAreaID));
                                continue;
                            }
                            if (null != equipmentListInfoGetDROutOther && CimArrayUtils.isNotEmpty(equipmentListInfoGetDROutOther.getStrAreaStocker())) {
                                for (Infos.AreaStocker strAreaStocker : equipmentListInfoGetDROutOther.getStrAreaStocker()) {
                                    if (CimStringUtils.equals(strAreaStocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                                        log.info("The reticlePodStocker {} which it same location of reticle", ObjectIdentifier.fetchValue(strAreaStocker.getStockerID()));
                                        log.info("step20- equipmentMethod.equipmentCheckAvail");
                                        try {
                                            equipmentMethod.equipmentCheckAvail(objCommon, strAreaStocker.getStockerID());
                                        } catch (ServiceException e) {
                                            if (Validations.isEquals(retCodeConfig.getEquipmentNotAvailableStat(), e.getCode())) {
                                                log.info("This reticlePodStocker {} is not Available state.", ObjectIdentifier.fetchValue(strAreaStocker.getStockerID()));
                                                continue;
                                            } else {
                                                throw e;
                                            }
                                        }
                                        //get reticlePod list in the reticlePodStocker
                                        Params.ReticlePodListInqParams reticlePodListInqParams = new Params.ReticlePodListInqParams();
                                        reticlePodListInqParams.setUser(objCommon.getUser());
                                        reticlePodListInqParams.setStockerID(strAreaStocker.getStockerID());
                                        reticlePodListInqParams.setEmptyFlag(true);
                                        reticlePodListInqParams.setMaxRetrieveCount(0);

                                        List<Infos.ReticlePodListInfo> reticlePodListInfoList = null;
                                        log.info("step21- reticleMethod.reticlePodFillInTxPDQ012DR");
                                        try {
                                            reticlePodListInfoList = this.reticlePodFillInTxPDQ012DR(objCommon, reticlePodListInqParams);
                                        } catch (ServiceException e) {
                                            if (Validations.isEquals(retCodeConfig.getNotFoundReticlePod(), e.getCode())) {
                                                continue;
                                            } else {
                                                throw e;
                                            }
                                        }
                                        if (CimArrayUtils.isNotEmpty(reticlePodListInfoList)) {
                                            for (Infos.ReticlePodListInfo reticlePodListInfo : reticlePodListInfoList) {
                                                ObjectIdentifier reticlePodID = reticlePodListInfo.getReticlePodID();
                                                log.info("Found retilcePod {} in reticlePodStocker.", ObjectIdentifier.fetchValue(reticlePodID));
                                                log.info("step22- reticleMethod.reticlePodCandidateInfoGet");
                                                List<Infos.CandidateReticlePod> candidateReticlePodList = this.reticlePodCandidateInfoGet(objCommon, reticlePodID, true);
                                                result.setStrCandidateReticlePods(candidateReticlePodList);
                                                if (CimArrayUtils.isNotEmpty(result.getStrCandidateReticlePods())) {
                                                    return result;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //===========================================================================================
                    // If no empty pod is listed by above selectionlogic, search other equipment reticlePodPort
                    //===========================================================================================
                    if (CimArrayUtils.isNotEmpty(equipmentListInfoGetDROut.getStrAreaEqp())) {
                        for (Infos.AreaEqp strAreaEqp : equipmentListInfoGetDROut.getStrAreaEqp()) {
                            log.info("The equipment {} which it same workArea of reticle", ObjectIdentifier.fetchValue(strAreaEqp.getEquipmentID()));
                            log.info("Check ReticleUseFlag");
                            log.info("step23- equipmentMethod.equipmentBRInfoGetDR");
                            Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, strAreaEqp.getEquipmentID());
                            if (null != eqpBrInfo) {
                                if (CimBooleanUtils.isFalse(eqpBrInfo.isReticleUseFlag())) {
                                    log.info("This equipment {} is not use reticle.", ObjectIdentifier.fetchValue(strAreaEqp.getEquipmentID()));
                                    continue;
                                }
                                //get reticlePod in other equipment
                                log.info("step24- equipmentMethod.equipmentReticlePodPortInfoGetDR");
                                Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, strAreaEqp.getEquipmentID());
                                if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                                    for (Infos.ReticlePodPortInfo strReticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                                        if (ObjectIdentifier.isNotEmptyWithValue(strReticlePodPortInfo.getLoadedReticlePodID())
                                                && CimStringUtils.equals(strReticlePodPortInfo.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                                                && CimStringUtils.equals(strReticlePodPortInfo.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP)) {
                                            log.info("Found retilcePod {} in ReticlePodStocker.", ObjectIdentifier.fetchValue(strReticlePodPortInfo.getLoadedReticlePodID()));
                                            log.info("step25- reticleMethod.reticlePodCandidateInfoGet");
                                            List<Infos.CandidateReticlePod> candidateReticlePodList = this.reticlePodCandidateInfoGet(objCommon, strReticlePodPortInfo.getLoadedReticlePodID(), true);
                                            result.setStrCandidateReticlePods(candidateReticlePodList);
                                            if (CimArrayUtils.isNotEmpty(result.getStrCandidateReticlePods())) {
                                                return result;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //================================================================================================================
                    // If no empty pod is listed by above selectionlogic, search other equipment reticlePodPort in the same location
                    //================================================================================================================
                    if (CimArrayUtils.isNotEmpty(areaGetByLocationIDs)) {
                        for (ObjectIdentifier workAreaID : areaGetByLocationIDs) {
                            Infos.EquipmentListInfoGetDRIn equipmentListInfoGetDRInput = new Infos.EquipmentListInfoGetDRIn();
                            equipmentListInfoGetDRInput.setWorkArea(workAreaID);
                            log.info("step26- equipmentMethod.equipmentListInfoGetDR");
                            Infos.EquipmentListInfoGetDROut equipmentListInfoGetDROutOther = equipmentMethod.equipmentListInfoGetDR(objCommon, equipmentListInfoGetDRInput);
                            if (null != equipmentListInfoGetDROutOther) {
                                if (CimArrayUtils.isEmpty(equipmentListInfoGetDROutOther.getStrAreaEqp())) {
                                    log.info("Not found equipment in this workArea {}", ObjectIdentifier.fetchValue(workAreaID));
                                    continue;
                                }
                                if (ObjectIdentifier.equalsWithValue(machineTypeGetOut.getAreaID(), workAreaID)) {
                                    log.info("This workArea {} is current workArea {}. this workArea skip.", ObjectIdentifier.fetchValue(machineTypeGetOut.getAreaID()), ObjectIdentifier.fetchValue(workAreaID));
                                    continue;
                                }
                                for (Infos.AreaEqp strAreaEqp : equipmentListInfoGetDROutOther.getStrAreaEqp()) {
                                    log.info("The equipment {} which it same location of reticle", ObjectIdentifier.fetchValue(strAreaEqp.getEquipmentID()));
                                    log.info("Check ReticleUseFlag");
                                    log.info("step27- equipmentMethod.equipmentBRInfoGetDR");
                                    Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, strAreaEqp.getEquipmentID());
                                    if (null != eqpBrInfo) {
                                        if (CimBooleanUtils.isFalse(eqpBrInfo.isReticleUseFlag())) {
                                            log.info("This equipment {} is not use reticle.", ObjectIdentifier.fetchValue(strAreaEqp.getEquipmentID()));
                                            continue;
                                        }
                                        //get reticlePod in other equipment
                                        log.info("step28- equipmentMethod.equipmentReticlePodPortInfoGetDR");
                                        Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, strAreaEqp.getEquipmentID());
                                        if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                                            for (Infos.ReticlePodPortInfo strReticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                                                if (ObjectIdentifier.isNotEmptyWithValue(strReticlePodPortInfo.getLoadedReticlePodID())
                                                        && CimStringUtils.equals(strReticlePodPortInfo.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                                                        && CimStringUtils.equals(strReticlePodPortInfo.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP)) {
                                                    log.info("Found retilcePod {} in ReticlePodStocker.", ObjectIdentifier.fetchValue(strReticlePodPortInfo.getLoadedReticlePodID()));
                                                    log.info("step29- reticleMethod.reticlePodCandidateInfoGet");
                                                    List<Infos.CandidateReticlePod> candidateReticlePodList = this.reticlePodCandidateInfoGet(objCommon, strReticlePodPortInfo.getLoadedReticlePodID(), true);
                                                    result.setStrCandidateReticlePods(candidateReticlePodList);
                                                    if (CimArrayUtils.isNotEmpty(result.getStrCandidateReticlePods())) {
                                                        return result;
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
        }
        return result;
    }

    @Override
    public List<Infos.CandidateReticlePod> reticlePodCandidateInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, Boolean reservedCheckFlag) {
        List<Infos.CandidateReticlePod> result = new ArrayList<>();
        log.info("in para reticlePodID: {}", ObjectIdentifier.fetchValue(reticlePodID));

        Boolean candidateCheckFlag = false;
        //----------------------------------------------
        // check reticlePodID
        //----------------------------------------------
        if (ObjectIdentifier.isNotEmptyWithValue(reticlePodID)) {
            log.info("reticlePodID != null");
            if (CimBooleanUtils.isTrue(candidateCheckFlag)) {
                log.info("check transfer reserved");
                log.info("step1 - reticleMethod.reticlePodReservedReticleInfoGetDR");
                Infos.ReticlePodAdditionalAttribute reticlePodAdditionalAttributeOut = this.reticlePodReservedReticleInfoGetDR(objCommon, reticlePodID);
                if (CimBooleanUtils.isTrue(reticlePodAdditionalAttributeOut.isTransferReservedFlag())) {
                    log.info("reticlePodAdditionalAttributeOut.isTransferReservedFlag() is TRUE");
                    candidateCheckFlag = false;
                }
                if (CimBooleanUtils.isTrue(candidateCheckFlag)) {
                    log.info("transferRequest check");
                    log.info("step2 - reticleMethod.reticleDispatchJobListGetDR");
                    List<Infos.ReticleDispatchJob> reticleDispatchJobList = this.reticleDispatchJobListGetDR(objCommon, "");
                    if (CimArrayUtils.isNotEmpty(reticleDispatchJobList)) {
                        for (Infos.ReticleDispatchJob reticleDispatchJob : reticleDispatchJobList) {
                            if (ObjectIdentifier.equalsWithValue(reticleDispatchJob.getReticlePodID(), reticlePodID)) {
                                log.info("Reticle Pod Found!");
                                candidateCheckFlag = false;
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            log.info("reticlePodID is null");
            candidateCheckFlag = false;
        }

        Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DROut = null;
        String reticlePodStatus = null;
        String reticlePodXferStatus = null;

        if (CimBooleanUtils.isTrue(candidateCheckFlag)) {
            log.info("candidateCheckFlag == TRUE");
            //----------------------------------------------
            // Get reticlePodIDStatus
            //----------------------------------------------
            Boolean durableOperationInfoFlag = false;
            Boolean durableWipOperationInfoFlag = false;

            Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
            reticlePodDetailInfoInqParams.setReticlePodID(reticlePodID);
            reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(durableWipOperationInfoFlag);
            reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(durableOperationInfoFlag);
            log.info("step3 - reticleMethod.reticlePodFillInTxPDQ013DR");
            reticlePodFillInTxPDQ013DROut = this.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);

            if (null != reticlePodFillInTxPDQ013DROut && null != reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo()) {
                Infos.ReticlePodStatusInfo reticlePodStatusInfo = reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo();
                reticlePodStatus = reticlePodStatusInfo.getReticlePodStatus();
                reticlePodXferStatus = reticlePodStatusInfo.getTransferStatus();
            }

            if (CimBooleanUtils.isTrue(reservedCheckFlag)) {
                log.info("check reticlePodStatus {}", reticlePodStatus);
                if (!CimStringUtils.equals(reticlePodStatus, CIMStateConst.CIM_DURABLE_AVAILABLE)
                        && !CimStringUtils.equals(reticlePodStatus, CIMStateConst.CIM_DURABLE_INUSE)) {
                    log.info("Status != Available and InUse");
                    candidateCheckFlag = false;
                }
                log.info("check reticlePodXferStatus {}", reticlePodXferStatus);
                if (CimStringUtils.equals(reticlePodXferStatus, BizConstant.SP_TRANSSTATE_STATIONOUT)
                        || CimStringUtils.equals(reticlePodXferStatus, BizConstant.SP_TRANSSTATE_BAYOUT)
                        || CimStringUtils.equals(reticlePodXferStatus, BizConstant.SP_TRANSSTATE_MANUALOUT)
                        || CimStringUtils.equals(reticlePodXferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)
                        || CimStringUtils.equals(reticlePodXferStatus, BizConstant.SP_TRANSSTATE_SHELFOUT)
                        || CimStringUtils.equals(reticlePodXferStatus, BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT)
                        || CimStringUtils.equals(reticlePodXferStatus, BizConstant.SP_TRANSSTATE_ABNORMALOUT)
                        || CimStringUtils.equals(reticlePodXferStatus, BizConstant.SP_UNDEFINED_STATE)) {
                    log.info("XferStatus == *O");
                    candidateCheckFlag = false;
                }
            }
        }

        if (CimBooleanUtils.isTrue(candidateCheckFlag)) {
            log.info("candidateCheckFlag == TRUE");
            ObjectIdentifier curReticlePodStokerID = null;
            ObjectIdentifier curBareReticleStokerID = null;
            ObjectIdentifier curEquipmentID = null;

            if (null != reticlePodFillInTxPDQ013DROut && null != reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo()) {
                Infos.ReticlePodStatusInfo reticlePodStatusInfo = reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo();
                if (ObjectIdentifier.isNotEmptyWithValue(reticlePodStatusInfo.getEquipmentID())) {
                    log.info("equipmentID {}", ObjectIdentifier.fetchValue(reticlePodStatusInfo.getEquipmentID()));
                    curEquipmentID = reticlePodStatusInfo.getEquipmentID();
                } else if (ObjectIdentifier.isNotEmptyWithValue(reticlePodStatusInfo.getStockerID())) {
                    log.info("stockerID {}", ObjectIdentifier.fetchValue(reticlePodStatusInfo.getStockerID()));
                    log.info("step4 - equipmentMethod.machineTypeGet");
                    Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, reticlePodStatusInfo.getStockerID());
                    log.info("stockerType {}", machineTypeGetOut.getStockerType());

                    if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                        log.info("stockerType == SP_STOCKER_TYPE_RETICLEPOD");
                        curReticlePodStokerID = reticlePodStatusInfo.getStockerID();
                    } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                        log.info("stockerType == SP_STOCKER_TYPE_BARERETICLE");
                        curBareReticleStokerID = reticlePodStatusInfo.getStockerID();
                    }
                } else {
                    log.info("equipmentID, stockerID is null");
                }
                log.info("reticlePodID                {}", ObjectIdentifier.fetchValue(reticlePodID));
                log.info("reticlePodStatus            {}", reticlePodStatus);
                log.info("reticlePodTransferStatus    {}", reticlePodXferStatus);
                log.info("currentReticlePodStockerID  {}", ObjectIdentifier.fetchValue(curReticlePodStokerID));
                log.info("currentBareReticleStockerID {}", ObjectIdentifier.fetchValue(curBareReticleStokerID));
                log.info("currentEquipmentID          {}", ObjectIdentifier.fetchValue(curEquipmentID));

                //----------------------------------------------
                // Prepare return structure
                //----------------------------------------------
                if (CimArrayUtils.isNotEmpty(reticlePodStatusInfo.getStrContainedReticleInfo())) {
                    log.info("this reticlePod have some reticle. not select this pod.");
                } else {
                    Infos.CandidateReticlePod candidateReticlePod = new Infos.CandidateReticlePod();
                    result.add(candidateReticlePod);
                    candidateReticlePod.setReticlePodID(reticlePodID);
                    candidateReticlePod.setSlotNumber(1L);
                    candidateReticlePod.setReticlePodStatus(reticlePodStatus);
                    candidateReticlePod.setReticlePodTransferStatus(reticlePodXferStatus);
                    candidateReticlePod.setCurrentReticlePodStockerID(curReticlePodStokerID);
                    candidateReticlePod.setCurrentBareReticleStockerID(curBareReticleStokerID);
                    candidateReticlePod.setCurrentEquipmentID(curEquipmentID);
                }
            }
        }
        return result;
    }

    @Override
    public Outputs.ReticleComponentJobCreateOut reticleComponentJobCreate(Infos.ObjCommon objCommon, String reticleDispatchJobID, ObjectIdentifier RDJRequestUserID, Long priority, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID, ObjectIdentifier toMachineID) {
        Outputs.ReticleComponentJobCreateOut result = new Outputs.ReticleComponentJobCreateOut();

        Boolean reticleXferRequired = false;
        if (ObjectIdentifier.isNotEmptyWithValue(reticleID)) {
            log.info("Reticle {} specified", ObjectIdentifier.fetchValue(reticleID));
            reticleXferRequired = true;
        }
        ObjectIdentifier reticleMachineID = null;
        String reticleMachineType = null;
        Infos.ReticleDispatchJob strReticleDispatchJob = new Infos.ReticleDispatchJob();
        Infos.ReticleXferJob strReticleXferJob = new Infos.ReticleXferJob();
        result.setStrReticleDispatchJob(strReticleDispatchJob);
        result.setStrReticleXferJob(strReticleXferJob);

        if (CimBooleanUtils.isTrue(reticleXferRequired)) {
            log.info("TRUE == reticleXferRequired");
            log.info("step1 - reticleMethod.reticleAvailabilityCheckForXfer");
            Outputs.ReticleAvailabilityCheckForXferOut reticleAvailabilityCheckForXferOut = this.reticleAvailabilityCheckForXfer(objCommon, reticleID);
            reticleMachineID = reticleAvailabilityCheckForXferOut.getMachineID();
            reticleMachineType = reticleAvailabilityCheckForXferOut.getMachineType();
            strReticleDispatchJob.setFromEquipmentID(reticleMachineID);
            strReticleDispatchJob.setFromEquipmentCategory(reticleMachineType);
            strReticleXferJob.setFromEquipmentID(reticleMachineID);

            if (ObjectIdentifier.equalsWithValue(toMachineID, reticleMachineID)) {
                log.info("toMachineID {} is same as reticleMachineID.", ObjectIdentifier.fetchValue(toMachineID));
                Validations.check(true, retCodeConfigEx.getInvalidRdjRecord(), reticleDispatchJobID);
            }
        }
        log.info("step2 - reticleMethod.reticlePodAvailabilityCheckForXfer");
        Outputs.ReticlePodAvailabilityCheckForXferOut reticlePodAvailabilityCheckForXferOut = this.reticlePodAvailabilityCheckForXfer(objCommon, reticlePodID, reticleDispatchJobID);
        ObjectIdentifier reticlePodMachineID = reticlePodAvailabilityCheckForXferOut.getMachineID();
        String reticlePodMachineType = reticlePodAvailabilityCheckForXferOut.getMachineType();

        if (CimBooleanUtils.isFalse(reticleXferRequired)) {
            log.info("FALSE == reticleXferRequired");
            strReticleDispatchJob.setFromEquipmentID(reticlePodMachineID);
            strReticleDispatchJob.setFromEquipmentCategory(reticlePodMachineType);
            strReticleXferJob.setFromEquipmentID(reticlePodMachineID);
        }

        Boolean reticleRetrieveFlag = false;
        if (CimBooleanUtils.isTrue(reticleXferRequired)) {
            log.info("Reticle retrieve is required except reticle is already in reticlePod.");
            reticleRetrieveFlag = true;
        }
        if (CimBooleanUtils.isFalse(reticlePodAvailabilityCheckForXferOut.getEmptyFlag())) {
            log.info("reticlePodAvailabilityCheckForXferOut.getEmptyFlag() is FLASE");
            if (CimBooleanUtils.isTrue(reticleXferRequired)) {
                log.info("TRUE == reticleXferRequired");
                if (CimArrayUtils.isNotEmpty(reticlePodAvailabilityCheckForXferOut.getReticleList())) {
                    int i = 0;
                    for (i = 0; i < CimArrayUtils.getSize(reticlePodAvailabilityCheckForXferOut.getReticleList()); i++) {
                        if (ObjectIdentifier.equalsWithValue(reticlePodAvailabilityCheckForXferOut.getReticleList().get(0), reticleID)) {
                            log.info("Reticle {} is already in xfer reticlePod.", ObjectIdentifier.fetchValue(reticleID));
                            break;
                        }
                    }
                    if (i == CimArrayUtils.getSize(reticlePodAvailabilityCheckForXferOut.getReticleList())) {
                        log.info("Other reticle is already in selected reticlePod {}.", ObjectIdentifier.fetchValue(reticlePodID));
                        Validations.check(true, retCodeConfigEx.getRspNotEmpty(), ObjectIdentifier.fetchValue(reticlePodID));
                    } else {
                        log.info("Reticle retrieve is not necessary.");
                        reticleRetrieveFlag = false;
                    }
                }
            } else {
                log.info("Empty pod {} xfer specified, but this pod is not empty.", ObjectIdentifier.fetchValue(reticlePodID));
                Validations.check(true, retCodeConfigEx.getInvalidRdjRecord(), reticleDispatchJobID);
            }
        }
        ObjectIdentifier podPortID = reticlePodAvailabilityCheckForXferOut.getPortID();
        String podPortStatus = reticlePodAvailabilityCheckForXferOut.getPortStatus();

        ObjectIdentifier fromMachineID = null;
        String fromMachineType = null;

        if (CimBooleanUtils.isTrue(reticleXferRequired)) {
            log.info("TRUE == reticleXferRequired");
            fromMachineID = reticleMachineID;
            fromMachineType = reticleMachineType;
        } else {
            log.info("TRUE != reticleXferRequired");
            fromMachineID = reticlePodMachineID;
            fromMachineType = reticlePodMachineType;
        }

        if (ObjectIdentifier.equalsWithValue(fromMachineID, toMachineID)) {
            log.info("Source and Destination machine {} is same.", ObjectIdentifier.fetchValue(fromMachineID));
            Validations.check(true, retCodeConfigEx.getInvalidRdjRecord(), reticleDispatchJobID);
        }

        Boolean emptyPodXferForRetrieveFlag = false;
        ObjectIdentifier fromPortID = null;
        String fromPortStatus = null;

        if (!ObjectIdentifier.equalsWithValue(fromMachineID, reticlePodMachineID)) {
            log.info("Empty reticlePod xfer for retrieve required.");
            emptyPodXferForRetrieveFlag = true;

            if (!CimStringUtils.equals(fromMachineType, BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.info("step3 - reticleMethod.reticlePodPortAvailabilityCheckForReticlePodXfer");
                List<ObjectIdentifier> availablePortList = this.reticlePodPortAvailabilityCheckForReticlePodXfer(objCommon, fromMachineID);
                if (CimArrayUtils.isEmpty(availablePortList)) {
                    log.info("reticlePodPortAvailabilityCheckForReticlePodXfer() returns strange reply.");
                    log.info("RSP Port for Retrieve not found on source machine {}.", ObjectIdentifier.fetchValue(fromMachineID));
                    Validations.check(true, retCodeConfigEx.getAvailRspPortNotFound(), ObjectIdentifier.fetchValue(fromMachineID));
                } else {
                    fromPortID = availablePortList.get(0);
                    fromPortStatus = BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP;
                }
            } else {
                log.info("Reticle {} is in RSPStocker {} and not in reticlePod...", ObjectIdentifier.fetchValue(reticleID), ObjectIdentifier.fetchValue(fromMachineID));
                Validations.check(true, retCodeConfigEx.getRtclInRspStkWithNoRsp(), ObjectIdentifier.fetchValue(reticleID), ObjectIdentifier.fetchValue(fromMachineID));
            }
        } else {
            log.info("Empty pod xfer for retrieve is not necessary.");
            emptyPodXferForRetrieveFlag = false;
            fromPortID = podPortID;
            fromPortStatus = podPortStatus;
        }
        if (CimBooleanUtils.isTrue(reticleRetrieveFlag)) {
            log.info("TRUE == reticleRetrieveFlag");
            if (CimStringUtils.equals(fromMachineType, BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.info("Reticle {} is in RSPStocker {} and not in reticlePod...", ObjectIdentifier.fetchValue(reticleID), ObjectIdentifier.fetchValue(fromMachineID));
                Validations.check(true, retCodeConfigEx.getRtclInRspStkWithNoRsp(), ObjectIdentifier.fetchValue(reticleID), ObjectIdentifier.fetchValue(fromMachineID));
            } else {
                log.info("Now check portStatus {} for retrieve.", fromPortStatus);
                if (!CimStringUtils.equals(fromPortStatus, BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP)) {
                    log.info("PortStatus is invalid for retrieve.");
                    Validations.check(true, retCodeConfigEx.getInvalidRspPortStatus(), ObjectIdentifier.fetchValue(fromPortID), fromPortStatus);
                }
            }
        } else {
            log.info("Now check portStatus {} for xfer.", fromPortStatus);
            if (!CimStringUtils.equals(fromPortStatus, BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP)
                    && !CimStringUtils.equals(fromPortStatus, BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)) {
                log.info("PortStatus is invalid for xfer.");
                Validations.check(true, retCodeConfigEx.getInvalidRspPortStatus(), ObjectIdentifier.fetchValue(fromPortID), fromPortStatus);
            }
        }
        log.info("step4 - equipmentMethod.machineStateAvailabilityCheckForReticlePodXfer");
        String toMachineType = equipmentMethod.machineStateAvailabilityCheckForReticlePodXfer(objCommon, toMachineID);

        ObjectIdentifier toPortID = null;
        String toPortStatus = null;

        if (CimStringUtils.equals(toMachineType, BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
            log.info("Destination machine {} is RSP Stocker.", ObjectIdentifier.fetchValue(toMachineID));
        } else {
            log.info("step5 - reticleMethod.reticlePodPortAvailabilityCheckForReticlePodXfer");
            List<ObjectIdentifier> availablePortList = this.reticlePodPortAvailabilityCheckForReticlePodXfer(objCommon, toMachineID);
            if (CimArrayUtils.isEmpty(availablePortList)) {
                log.info("reticlePodPortAvailabilityCheckForReticlePodXfer() returns strange reply.");
                log.info("RSP Port for xfer not found on destination machine {}.", ObjectIdentifier.fetchValue(toMachineID));
                Validations.check(true, retCodeConfigEx.getAvailRspPortNotFound(), ObjectIdentifier.fetchValue(toMachineID));
            } else {
                toPortID = availablePortList.get(0);
                toPortStatus = BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ;
            }
            if (CimBooleanUtils.isTrue(reticleXferRequired)) {
                log.info("TRUE == reticleXferRequired");
                log.info("step6 - equipmentMethod.machineCapacityCheckForReticleStore");
                equipmentMethod.machineCapacityCheckForReticleStore(objCommon, toMachineID);
            }
        }
        //tmpRCJList.length(SP_MAX_RCJLEN_FROM_ONE_RDJ);
        List<Infos.ReticleComponentJob> tmpRCJList = new ArrayList<>();
        ObjectIdentifier dummyID = ObjectIdentifier.buildWithValue("");

        //step1
        if (CimBooleanUtils.isTrue(emptyPodXferForRetrieveFlag)
                && CimStringUtils.equals(podPortStatus, BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP)) {
            log.info("emptyPodXferForRetrieveFlag == TRUE && podPortStatus = LoadComp");
            // TODO: 2020/11/10 confirm timeStamp_GetDR method
//            Timestamp currentTimeStamp = DateUtils.getCurrentTimeStamp();
            Infos.ReticleComponentJob job = new Infos.ReticleComponentJob();
            job.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            job.setPriority(priority);
            job.setReticleDispatchJobID(reticleDispatchJobID);
            job.setReticleComponentJobID(this.timeStampGetDR());
            job.setReticleDispatchJobRequestUserID(RDJRequestUserID);
            job.setJobSeq(CimNumberUtils.longValue(CimArrayUtils.getSize(tmpRCJList) + 1));
            job.setReticlePodID(reticlePodID);
            job.setSlotNo(0L);
            job.setReticleID(dummyID);
            job.setJobName(BizConstant.SP_RCJ_JOBNAME_UNCLAMP);
            job.setToEquipmentID(reticlePodMachineID);
            job.setToReticlePodPortID(podPortID);
            job.setToEquipmentCategory(reticlePodMachineType);
            job.setFromEquipmentID(reticlePodMachineID);
            job.setFromReticlePodPortID(podPortID);
            job.setFromEquipmentCategory(reticlePodMachineType);
            job.setJobStatus(BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE);
            job.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            tmpRCJList.add(job);
        }
        //step2
        if (CimBooleanUtils.isTrue(emptyPodXferForRetrieveFlag)) {
            log.info("emptyPodXferForRetrieveFlag == TRUE");
            // TODO: 2020/11/10 confirm timeStamp_GetDR method
//            Timestamp currentTimeStamp = DateUtils.getCurrentTimeStamp();
            Infos.ReticleComponentJob job = new Infos.ReticleComponentJob();
            job.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            job.setPriority(priority);
            job.setReticleDispatchJobID(reticleDispatchJobID);
            job.setReticleComponentJobID(this.timeStampGetDR());
            job.setReticleDispatchJobRequestUserID(RDJRequestUserID);
            job.setJobSeq(CimNumberUtils.longValue(CimArrayUtils.getSize(tmpRCJList) + 1));
            job.setReticlePodID(reticlePodID);
            job.setSlotNo(0L);
            job.setReticleID(dummyID);
            job.setJobName(BizConstant.SP_RCJ_JOBNAME_XFER);
            job.setToEquipmentID(fromMachineID);
            job.setToReticlePodPortID(fromPortID);
            job.setToEquipmentCategory(fromMachineType);
            job.setFromEquipmentID(reticlePodMachineID);
            job.setFromReticlePodPortID(podPortID);
            job.setFromEquipmentCategory(reticlePodMachineType);
            job.setJobStatus(BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE);
            job.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            tmpRCJList.add(job);
        }
        //step3
        if (CimBooleanUtils.isTrue(reticleRetrieveFlag)) {
            log.info("reticleRetrieveFlag == TRUE");
            // TODO: 2020/11/10 confirm timeStamp_GetDR method
//            Timestamp currentTimeStamp = DateUtils.getCurrentTimeStamp();
            Infos.ReticleComponentJob job = new Infos.ReticleComponentJob();
            job.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            job.setPriority(priority);
            job.setReticleDispatchJobID(reticleDispatchJobID);
            job.setReticleComponentJobID(this.timeStampGetDR());
            job.setReticleDispatchJobRequestUserID(RDJRequestUserID);
            job.setJobSeq(CimNumberUtils.longValue(CimArrayUtils.getSize(tmpRCJList) + 1));
            job.setReticlePodID(reticlePodID);
            job.setSlotNo(1L);
            job.setReticleID(reticleID);
            job.setJobName(BizConstant.SP_RCJ_JOBNAME_RETRIEVE);
            job.setToEquipmentID(fromMachineID);
            job.setToReticlePodPortID(fromPortID);
            job.setToEquipmentCategory(fromMachineType);
            job.setFromEquipmentID(fromMachineID);
            job.setFromReticlePodPortID(fromPortID);
            job.setFromEquipmentCategory(fromMachineType);
            job.setJobStatus(BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE);
            job.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            tmpRCJList.add(job);
        }
        //step4
        if (CimBooleanUtils.isTrue(emptyPodXferForRetrieveFlag)
                || CimBooleanUtils.isTrue(reticleRetrieveFlag)) {
            log.info("emptyPodXferForRetrieveFlag is TRUE || reticleRetrieveFlag is TRUE");
            // TODO: 2020/11/10 confirm timeStamp_GetDR method
//            Timestamp currentTimeStamp = DateUtils.getCurrentTimeStamp();
            Infos.ReticleComponentJob job = new Infos.ReticleComponentJob();
            job.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            job.setPriority(priority);
            job.setReticleDispatchJobID(reticleDispatchJobID);
            job.setReticleComponentJobID(this.timeStampGetDR());
            job.setReticleDispatchJobRequestUserID(RDJRequestUserID);
            job.setJobSeq(CimNumberUtils.longValue(CimArrayUtils.getSize(tmpRCJList) + 1));
            job.setReticlePodID(reticlePodID);
            job.setJobName(BizConstant.SP_RCJ_JOBNAME_UNCLAMP);
            job.setToEquipmentID(fromMachineID);
            job.setToReticlePodPortID(fromPortID);
            job.setToEquipmentCategory(fromMachineType);
            job.setFromEquipmentID(fromMachineID);
            job.setFromReticlePodPortID(fromPortID);
            job.setFromEquipmentCategory(fromMachineType);
            job.setJobStatus(BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE);
            job.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

            if (CimBooleanUtils.isTrue(reticleXferRequired)) {
                job.setSlotNo(1L);
                job.setReticleID(reticleID);
            } else {
                job.setSlotNo(0L);
                job.setReticleID(dummyID);
            }
            tmpRCJList.add(job);
        }
        //step5
        // TODO: 2020/11/10 confirm timeStamp_GetDR method
//        Timestamp currentTimeStamp = DateUtils.getCurrentTimeStamp();
        Infos.ReticleComponentJob job = new Infos.ReticleComponentJob();
        job.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        job.setPriority(priority);
        job.setReticleDispatchJobID(reticleDispatchJobID);
        job.setReticleComponentJobID(this.timeStampGetDR());
        job.setReticleDispatchJobRequestUserID(RDJRequestUserID);
        job.setJobSeq(CimNumberUtils.longValue(CimArrayUtils.getSize(tmpRCJList) + 1));
        job.setReticlePodID(reticlePodID);
        job.setJobName(BizConstant.SP_RCJ_JOBNAME_XFER);
        job.setToEquipmentID(toMachineID);
        job.setToReticlePodPortID(toPortID);
        job.setToEquipmentCategory(toMachineType);
        job.setFromEquipmentID(fromMachineID);
        job.setFromReticlePodPortID(fromPortID);
        job.setFromEquipmentCategory(fromMachineType);
        job.setJobStatus(BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE);
        job.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

        if (CimBooleanUtils.isTrue(reticleXferRequired)) {
            job.setSlotNo(1L);
            job.setReticleID(reticleID);
        } else {
            job.setSlotNo(0L);
            job.setReticleID(dummyID);
        }
        tmpRCJList.add(job);

        //step6
        if (CimBooleanUtils.isTrue(reticleXferRequired)
                && !CimStringUtils.equals(toMachineType, BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
            log.info("reticleXferRequired  == TRUE, toMachineType != SP_STOCKER_TYPE_RETICLEPOD");
            // TODO: 2020/11/10 confirm timeStamp_GetDR method
//            Timestamp currentTimeStampOut = DateUtils.getCurrentTimeStamp();
            Infos.ReticleComponentJob componentJob = new Infos.ReticleComponentJob();
            componentJob.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            componentJob.setPriority(priority);
            componentJob.setReticleDispatchJobID(reticleDispatchJobID);
            componentJob.setReticleComponentJobID(this.timeStampGetDR());
            componentJob.setReticleDispatchJobRequestUserID(RDJRequestUserID);
            componentJob.setJobSeq(CimNumberUtils.longValue(CimArrayUtils.getSize(tmpRCJList) + 1));
            componentJob.setReticlePodID(reticlePodID);
            componentJob.setSlotNo(1L);
            componentJob.setReticleID(reticleID);
            componentJob.setJobName(BizConstant.SP_RCJ_JOBNAME_STORE);
            componentJob.setToEquipmentID(toMachineID);
            componentJob.setToReticlePodPortID(toPortID);
            componentJob.setToEquipmentCategory(toMachineType);
            componentJob.setFromEquipmentID(toMachineID);
            componentJob.setFromReticlePodPortID(toPortID);
            componentJob.setFromEquipmentCategory(toMachineType);
            componentJob.setJobStatus(BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE);
            componentJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            tmpRCJList.add(componentJob);
        }
        //return RCJ List
        result.setStrReticleComponentJobList(tmpRCJList);

        //return RDJ
        Infos.ReticleDispatchJob tmpRDJ = new Infos.ReticleDispatchJob();
        tmpRDJ.setDispatchStationID("");
        tmpRDJ.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        tmpRDJ.setReticleID(reticleID);
        tmpRDJ.setReticlePodID(reticlePodID);
        tmpRDJ.setReticleDispatchJobID(reticleDispatchJobID);
        tmpRDJ.setFromEquipmentID(fromMachineID);
        tmpRDJ.setFromEquipmentCategory(fromMachineType);
        tmpRDJ.setToEquipmentID(toMachineID);
        tmpRDJ.setToEquipmentCategory(toMachineType);
        tmpRDJ.setPriority(1L);
        tmpRDJ.setRequestUserID(RDJRequestUserID);

        if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.ASYNC_RETICLE_XFER_JOB_CREATE_REQ.getValue())) {//from TxAsyncReticleXferJobCreateReq
            // TODO: 2020/11/11 TXPDC703 from TxAsyncReticleXferJobCreateReq but TxAsyncReticleXferJobCreateReq is TXPDC037
            tmpRDJ.setJobStatus(BizConstant.SP_RDJ_STATUS_CREATED);
        } else {
            tmpRDJ.setJobStatus(BizConstant.SP_RDJ_STATUS_WAITTOEXECUTE);
        }

        tmpRDJ.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        result.setStrReticleDispatchJob(tmpRDJ);

        //return Xfer Job
        Infos.ReticleXferJob tmpReticleXferJob = new Infos.ReticleXferJob();
        tmpReticleXferJob.setFromEquipmentID(fromMachineID);
        tmpReticleXferJob.setFromPortID(fromPortID);
        tmpReticleXferJob.setToEquipmentID(toMachineID);
        tmpReticleXferJob.setToPortID(toPortID);
        tmpReticleXferJob.setReticlePodID(reticlePodID);
        tmpReticleXferJob.setReticleID(reticleID);
        result.setStrReticleXferJob(tmpReticleXferJob);

        //retrurn EventRecord
        Infos.ReticleEventRecord tmpRecticleEventRecord = new Infos.ReticleEventRecord();
        tmpRecticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        tmpRecticleEventRecord.setReticleDispatchJobID(reticleDispatchJobID);
        tmpRecticleEventRecord.setReticleComponentJobID(tmpRCJList.get(0).getReticleComponentJobID());
        tmpRecticleEventRecord.setReticlePodStockerID(dummyID);
        tmpRecticleEventRecord.setBareReticleStockerID(dummyID);
        tmpRecticleEventRecord.setResourceID(dummyID);
        tmpRecticleEventRecord.setEquipmentID(dummyID);
        tmpRecticleEventRecord.setRSPPortID(dummyID);
        tmpRecticleEventRecord.setReticlePodID(dummyID);
        tmpRecticleEventRecord.setRSPPortEvent("");
        tmpRecticleEventRecord.setReticleID(dummyID);
        tmpRecticleEventRecord.setReticleJobEvent("");
        result.setStrReticleEventRecord(tmpRecticleEventRecord);
        return result;
    }

    @Override
    public List<ObjectIdentifier> reticlePodPortAvailabilityCheckForReticlePodXfer(Infos.ObjCommon objCommon, ObjectIdentifier machineID) {
        List<ObjectIdentifier> reuslt = new ArrayList<>();
        log.info("step1 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, machineID);
        if (CimBooleanUtils.isTrue(machineTypeGetOut.isBStorageMachineFlag())
                && !CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.info("Invalid stocker {} specified. stocker ID", ObjectIdentifier.fetchValue(machineID));
            Validations.check(true, retCodeConfigEx.getStkTypeDifferent(), machineTypeGetOut.getStockerType());
        }
        List<Infos.ReticlePodPortInfo> tmpReticlePodPortInfoList = new ArrayList<>();
        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.info("The machineID is EQP {}.", ObjectIdentifier.fetchValue(machineID));
            log.info("step2 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, machineID);
            if (null != equipmentReticlePodPortInfoGetDROut) {
                tmpReticlePodPortInfoList = equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
            }
        } else {
            log.info("This machine {} is BR Stocker.", ObjectIdentifier.fetchValue(machineID));
            log.info("step3 - stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, machineID);
            tmpReticlePodPortInfoList = reticlePodPortInfoList;
        }

        List<ObjectIdentifier> tmpAvailablePortList = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(tmpReticlePodPortInfoList)) {
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : tmpReticlePodPortInfoList) {
                if (CimStringUtils.equals(reticlePodPortInfo.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                        && CimStringUtils.equals(reticlePodPortInfo.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)
                        && ObjectIdentifier.isEmptyWithValue(reticlePodPortInfo.getLoadedReticlePodID())
                        && ObjectIdentifier.isEmptyWithValue(reticlePodPortInfo.getReservedReticlePodID())
                        && CimStringUtils.equals(reticlePodPortInfo.getTransferReserveStatus(), BizConstant.SP_RETICLEPODPORT_NOTRESERVED)
                        && CimStringUtils.equals(reticlePodPortInfo.getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED)) {
                    log.info("Available Port Found!");

                    List<Infos.ReticleComponentJob> reticleComponentJobList = null;
                    log.info("step4 - reticleMethod.reticleComponentJobCheckExistenceDR");
                    try {
                        reticleComponentJobList = this.reticleComponentJobCheckExistenceDR(objCommon,
                                null,
                                null,
                                machineID,
                                reticlePodPortInfo.getReticlePodPortID(),
                                null,
                                null);
                    } catch (ServiceException e) {
                        log.info("This RSPPort {} is reserved by RCJ", ObjectIdentifier.fetchValue(reticlePodPortInfo.getReticlePodPortID()));
                        Boolean ignoreRCJ = false;
                        reticleComponentJobList = e.getData(List.class);
                        if (Validations.isEquals(retCodeConfigEx.getFoundInRcj(), e.getCode())) {
                            if (CimArrayUtils.isNotEmpty(reticleComponentJobList)) {
                                for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobList) {
                                    if (!CimStringUtils.equals(reticleComponentJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_XFER)
                                            || CimStringUtils.equals(reticleComponentJob.getJobStatus(), BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE)
                                            || !ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromEquipmentID(), machineID)
                                            || !ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromReticlePodPortID(), reticlePodPortInfo.getReticlePodPortID())) {
                                        log.info("This RCJ is not ignorable.");
                                        ignoreRCJ = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (CimBooleanUtils.isFalse(ignoreRCJ)) {
                            log.info("ignoreRCJ == FALSE");
                            continue;
                        }
                    }
                    tmpAvailablePortList.add(reticlePodPortInfo.getReticlePodPortID());
                }
            }
        }
        reuslt = tmpAvailablePortList;
        if (CimArrayUtils.isEmpty(reuslt)) {
            log.info("Available port is not found for this machine {}.", ObjectIdentifier.fetchValue(machineID));
            Validations.check(true, retCodeConfigEx.getAvailRspPortNotFound(), ObjectIdentifier.fetchValue(machineID));
        }
        return reuslt;
    }

    @Override
    public void reticleComponentJobInsertDR(Infos.ObjCommon objCommon, List<Infos.ReticleComponentJob> strReticleComponentJobList) {
        Optional.ofNullable(strReticleComponentJobList).ifPresent(list -> list.forEach(data -> {
            CimRELDO entity = new CimRELDO();
            entity.setReqTime(CimDateUtils.convertToOrInitialTime(data.getRequestedTimestamp()));
            entity.setPriority(data.getPriority().intValue());
            entity.setRdjId(data.getReticleDispatchJobID());
            entity.setRcjId(data.getReticleComponentJobID());
            entity.setReqUserId(ObjectIdentifier.fetchValue(data.getReticleDispatchJobRequestUserID()));
            entity.setJobSeq(data.getJobSeq().intValue());
            entity.setRtclId(ObjectIdentifier.fetchValue(data.getReticleID()));
            entity.setRtclpodId(ObjectIdentifier.fetchValue(data.getReticlePodID()));
            entity.setSlotNo(data.getSlotNo().intValue());
            entity.setJobName(data.getJobName());
            entity.setToEqpId(ObjectIdentifier.fetchValue(data.getToEquipmentID()));
            entity.setToEqpCategory(data.getToEquipmentCategory());
            entity.setToPortId(ObjectIdentifier.fetchValue(data.getToReticlePodPortID()));
            entity.setFromEqpId(ObjectIdentifier.fetchValue(data.getFromEquipmentID()));
            entity.setFromEqpCategory(data.getFromEquipmentCategory());
            entity.setFromPortId(ObjectIdentifier.fetchValue(data.getFromReticlePodPortID()));
            entity.setJobStatus(data.getJobStatus());
            entity.setJobStatChgTime(CimDateUtils.convertToOrInitialTime(data.getJobStatusChangeTimestamp()));
            cimJpaRepository.save(entity);
        }));
    }

    @Override
    public void reticleDispatchJobDeleteDR(Infos.ObjCommon objCommon, String reticleDispatchJobID) {
        //=========================================================================
        // Delete reticle dispatch job from FSRAL
        //=========================================================================
        Validations.check(CimStringUtils.isEmpty(reticleDispatchJobID), retCodeConfig.getInvalidParameter());
        CimRALDO example = new CimRALDO();
        example.setRdjID(reticleDispatchJobID);
        cimJpaRepository.delete(Example.of(example));
    }

    @Override
    public void reticleComponentJobDeleteDR(Infos.ObjCommon objCommon, String reticleDispatchJobID) {
        //=========================================================================
        // Delete reticle Component job from FSREL
        //=========================================================================
        Validations.check(CimStringUtils.isEmpty(reticleDispatchJobID), retCodeConfig.getInvalidParameter());
        CimRELDO example = new CimRELDO();
        example.setRdjId(reticleDispatchJobID);
        cimJpaRepository.delete(Example.of(example));
    }

    @Override
    public void reticleComponentJobUpdateDR(Infos.ObjCommon objCommon, Infos.ReticleComponentJob strReticleComponentJob) {
        CimRELDO cimRELExam = new CimRELDO();
        cimRELExam.setRdjId(strReticleComponentJob.getReticleDispatchJobID());
        cimRELExam.setRcjId(strReticleComponentJob.getReticleComponentJobID());
        List<CimRELDO> queryList = cimJpaRepository.findAll(Example.of(cimRELExam));
        Validations.check(CimArrayUtils.isEmpty(queryList), retCodeConfigEx.getRcjNotFound());
        queryList.forEach(entity -> {
            entity.setReqTime(CimDateUtils.convertToOrInitialTime(strReticleComponentJob.getRequestedTimestamp()));
            entity.setPriority(strReticleComponentJob.getPriority().intValue());
            entity.setRdjId(strReticleComponentJob.getReticleDispatchJobID());
            entity.setRcjId(strReticleComponentJob.getReticleComponentJobID());
            entity.setReqUserId(ObjectIdentifier.fetchValue(strReticleComponentJob.getReticleDispatchJobRequestUserID()));
            entity.setJobSeq(strReticleComponentJob.getJobSeq().intValue());
            entity.setRtclId(ObjectIdentifier.fetchValue(strReticleComponentJob.getReticleID()));
            entity.setRtclpodId(ObjectIdentifier.fetchValue(strReticleComponentJob.getReticlePodID()));
            entity.setSlotNo(strReticleComponentJob.getSlotNo().intValue());
            entity.setJobName(strReticleComponentJob.getJobName());
            entity.setToEqpId(ObjectIdentifier.fetchValue(strReticleComponentJob.getToEquipmentID()));
            entity.setToEqpCategory(strReticleComponentJob.getToEquipmentCategory());
            entity.setToPortId(ObjectIdentifier.fetchValue(strReticleComponentJob.getToReticlePodPortID()));
            entity.setFromEqpId(ObjectIdentifier.fetchValue(strReticleComponentJob.getFromEquipmentID()));
            entity.setFromEqpCategory(strReticleComponentJob.getFromEquipmentCategory());
            entity.setFromPortId(ObjectIdentifier.fetchValue(strReticleComponentJob.getFromReticlePodPortID()));
            entity.setJobStatus(strReticleComponentJob.getJobStatus());
            entity.setJobStatChgTime(CimDateUtils.convertToOrInitialTime(strReticleComponentJob.getJobStatusChangeTimestamp()));
            cimJpaRepository.save(entity);
        });
    }

    @Override
    public void reticlePodTransferReserveCancel(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {
        //------------------------------------
        // Get reticle pod object reference
        //------------------------------------
        CimReticlePod aPosReticlePod = null;
        if (ObjectIdentifier.isEmptyWithRefKey(reticlePodID)) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(reticlePodID), retCodeConfig.getNotFoundReticlePod(), "*****");
            aPosReticlePod = durableManager.findReticlePodNamed(reticlePodID.getValue());
        } else {
            aPosReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID.getReferenceKey());
        }
        Validations.check(null == aPosReticlePod, retCodeConfig.getNotFoundReticlePod(), ObjectIdentifier.fetchValue(reticlePodID));

        //----------------------------------------
        // Update reticle pod reserve information
        //----------------------------------------
        CimPerson aPerson = null;
        ObjectIdentifier userID = objCommon.getUser().getUserID();
        if (ObjectIdentifier.isEmptyWithRefKey(userID)) {
            aPerson = personManager.findPersonNamed(ObjectIdentifier.fetchValue(userID));
        } else {
            aPerson = baseCoreFactory.getBO(CimPerson.class, userID.getReferenceKey());
        }
        Validations.check(null == aPerson, retCodeConfig.getNotFoundPerson(), ObjectIdentifier.fetchValue(userID));

        aPosReticlePod.setLastClaimedPerson(aPerson);
        aPosReticlePod.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aPosReticlePod.setTransferDestinationEquipment(null);
        aPosReticlePod.setTransferDestinationStocker(null);
        aPosReticlePod.setTransferReserveUser(null);
        aPosReticlePod.setTransferReservedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
    }

    @Override
    public Outputs.ReticleReservationDetailInfoGetOut reticleReservationDetailInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Outputs.ReticleReservationDetailInfoGetOut out = new Outputs.ReticleReservationDetailInfoGetOut();
        //Localization
        log.info("in para reticleID : {}", reticleID);

        /*-------------------------*/
        /*  Get Reticle object     */
        /*-------------------------*/
        CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);

        /*----------------------------------*/
        /*  Get Reticle reservation flag    */
        /*----------------------------------*/
        Boolean reticleReserved = false;
        out.setReservedFlag(aReticle.isReserved());

        /*--------------------------------*/
        /*  Get Reticle reserve person    */
        /*--------------------------------*/
        out.setReservePerson(aReticle.getReservePersonID());

        /*--------------------------------------------------*/
        /*  Get Reticle current assigned machine if exist   */
        /*--------------------------------------------------*/
        Machine aMachine = aReticle.currentAssignedMachine();

        if (null != aMachine) {
            out.setCurrentAssignedMachineID(ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
        }

        /*--------------------------------------------------------*/
        /*  Get Reticle transfer destination equipment if exist   */
        /*--------------------------------------------------------*/
        CimMachine aPosMachine = aReticle.getTransferDestinationEquipment();

        if (null != aPosMachine) {
            out.setTransferDestinationEquipmentID(ObjectIdentifier.build(aPosMachine.getIdentifier(), aPosMachine.getPrimaryKey()));
        }

        /*------------------------------------------------------*/
        /*  Get Reticle transfer destination stocker if exist   */
        /*------------------------------------------------------*/
        CimStorageMachine aPosStorageMachine = aReticle.getTransferDestinationStocker();

        if (null != aPosStorageMachine) {
            out.setTransferDestinationStockerID(ObjectIdentifier.build(aPosStorageMachine.getIdentifier(), aPosStorageMachine.getPrimaryKey()));
        }

        /*-----------------------------------------------*/
        /*  Get Reticle transfer reserve user if exist   */
        /*-----------------------------------------------*/
        CimPerson aPosPerson = aReticle.getTransferReserveUser();


        if (null != aPosPerson) {
            out.setTransferReserveUserID(ObjectIdentifier.build(aPosPerson.getIdentifier(), aPosPerson.getPrimaryKey()));
        }

        /*-------------------------------------------------------*/
        /*  Get Reticle transfer reserved reticle Pod if exist   */
        /*-------------------------------------------------------*/
        CimReticlePod aPosReticlePod = aReticle.getTransferReservedReticlePod();


        if (null != aPosReticlePod) {
            out.setTransferReservedReticlePodID(ObjectIdentifier.build(aPosReticlePod.getIdentifier(), aPosReticlePod.getPrimaryKey()));
        }

        /*-------------------------------------------------------*/
        /*  Get Reticle transfer reserved Time Stamp if exist    */
        /*-------------------------------------------------------*/

        out.setTransferReservedTimeStamp(aReticle.getTransferReservedTimeStamp().toString());


        /*--------------------------------------------------------------------*/
        /*  Get Reticle transfer reserved reticle pod slot number if exist    */
        /*--------------------------------------------------------------------*/

        out.setTransferReservedReticlePodSlotNumber(aReticle.getTransferReservedReticlePodSlotNumber());


        /*---------------------------------------------------------*/
        /*  Get Reticle transfer reserved control jobs if exist    */
        /*---------------------------------------------------------*/
        Outputs.ReticleControlJobInfoGetOut reticlecontrolJobInfoGet = this.reticlecontrolJobInfoGet(objCommon, reticleID);
        out.setStrControlJobAttributeInfoSeq(reticlecontrolJobInfoGet.getStrControlJobAttributeInfoSeq());
        return out;
    }

    @Override
    public void reticleStoreTimeSet(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String toString) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/

        //Localization

        /*-------------------------*/
        /*  Get Reticle object     */
        /*-------------------------*/
        CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        //【TODO】【TODO - NOTIMPL】- 方法没实现
        //aReticle.setStoreTimeStamp(storeTimeStamp);
    }

    @Override
    public Outputs.ReticleControlJobInfoGetOut reticlecontrolJobInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Outputs.ReticleControlJobInfoGetOut reticleControlJobInfoGetOut = new Outputs.ReticleControlJobInfoGetOut();

        CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);

        List<CimControlJob> aReticleControlJobList = aReticle.allReservedControlJobs();

        int rtclCtrlJobCount = CimArrayUtils.getSize(aReticleControlJobList);
        List<Outputs.ControlJobAttributeInfo> controlJobAttributeInfos = new ArrayList<>();
        if (rtclCtrlJobCount > 0) {
            for (int i = 0; i < rtclCtrlJobCount; i++) {
                Outputs.ControlJobAttributeInfo controlJobAttributeInfo = new Outputs.ControlJobAttributeInfo();

                ObjectIdentifier controlJobID = ObjectIdentifier.build(aReticleControlJobList.get(i).getIdentifier(), aReticleControlJobList.get(i).getPrimaryKey());
                /*********************************/
                /*  Get control Job information  */
                /*********************************/
                controlJobAttributeInfos.add(controlJobMethod.controlJobAttributeInfoGet(objCommon, controlJobID));
            }
        }
        reticleControlJobInfoGetOut.setStrControlJobAttributeInfoSeq(controlJobAttributeInfos);
        return reticleControlJobInfoGetOut;
    }

    @Override
    public Outputs.ReticleArhsJobCreateOut reticleArhsJobCreate(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Integer slotNumber, ObjectIdentifier reticlePodID, ObjectIdentifier toMachineID, ObjectIdentifier toPortID, ObjectIdentifier fromMachineID, ObjectIdentifier fromPortID, String jobName) {
        Outputs.ReticleArhsJobCreateOut reuslt = new Outputs.ReticleArhsJobCreateOut();
        ObjectIdentifier dummyID = ObjectIdentifier.buildWithValue("");
        /*-----------------*/
        /*   Check FSRAL   */
        /*-----------------*/
        log.info("step1 - reticleMethod.reticleDispatchJobCheckExistenceDR");
        this.reticleDispatchJobCheckExistenceDR(objCommon,
                reticleID,
                reticlePodID,
                dummyID);

        /*-----------------*/
        /*   Check FSREL   */
        /*-----------------*/
        log.info("step2 - reticleMethod.reticleComponentJobCheckExistenceDR");
        this.reticleComponentJobCheckExistenceDR(objCommon,
                reticleID,
                reticlePodID,
                fromMachineID,
                fromPortID,
                toMachineID,
                toPortID);
        /*----------------------------------*/
        /*   Get from/to machine categoty   */
        /*----------------------------------*/
        log.info("step3 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut fromMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, fromMachineID);

        log.info("step4 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut toMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, toMachineID);

        String fromMachineCategory = null;
        if (CimBooleanUtils.isFalse(fromMachineTypeGetOut.isBStorageMachineFlag())) {
            log.info("fromMachineTypeGetOut.bStorageMachineFlag == FALSE");
            fromMachineCategory = BizConstant.SP_MACHINE_TYPE_EQP;
        } else {
            log.info("bStorageMachineFlag == TRUE");
            if (CimStringUtils.equals(fromMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.info("stockerType = BRS");
                fromMachineCategory = BizConstant.SP_STOCKER_TYPE_BARERETICLE;
            } else if (CimStringUtils.equals(fromMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.info("stockerType = RPS");
                fromMachineCategory = BizConstant.SP_STOCKER_TYPE_RETICLEPOD;
            }
        }

        String toMachineCategory = null;
        if (CimBooleanUtils.isFalse(toMachineTypeGetOut.isBStorageMachineFlag())) {
            log.info("toMachineTypeGetOut.bStorageMachineFlag == FALSE");
            toMachineCategory = BizConstant.SP_MACHINE_TYPE_EQP;
        } else {
            if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.info("stockerType = BRS");
                toMachineCategory = BizConstant.SP_STOCKER_TYPE_BARERETICLE;
            } else if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.info("stockerType = RPS");
                toMachineCategory = BizConstant.SP_STOCKER_TYPE_RETICLEPOD;
            }
        }

        /*---------------------*/
        /*   Insert to FSRAL   */
        /*---------------------*/
        // TODO: 2020/11/12  timeStamp_GetDR
        Infos.ReticleDispatchJob reticleDispatchJob = new Infos.ReticleDispatchJob();
        List<Infos.ReticleDispatchJob> strReticleDispatchJobList = Arrays.asList(reticleDispatchJob);
        reticleDispatchJob.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        reticleDispatchJob.setReticleID(reticleID);
        reticleDispatchJob.setReticlePodID(reticlePodID);
        reticleDispatchJob.setReticleDispatchJobID(this.timeStampGetDR());//timeStamp_GetDR out todo confrim it
        reticleDispatchJob.setFromEquipmentID(fromMachineID);
        reticleDispatchJob.setFromEquipmentCategory(fromMachineCategory);
        reticleDispatchJob.setToEquipmentID(toMachineID);
        reticleDispatchJob.setToEquipmentCategory(toMachineCategory);
        reticleDispatchJob.setPriority(1L);
        reticleDispatchJob.setRequestUserID(objCommon.getUser().getUserID());
        reticleDispatchJob.setJobStatus(BizConstant.SP_RDJ_STATUS_WAITTOEXECUTE);
        reticleDispatchJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

        log.info("step5 - reticleMethod.reticleDispatchJobInsertDR");
        this.reticleDispatchJobInsertDR(objCommon, strReticleDispatchJobList);

        /*---------------------*/
        /*   Insert to FSREL   */
        /*---------------------*/
        // TODO: 2020/11/12  timeStamp_GetDR
        Infos.ReticleComponentJob reticleComponentJob = new Infos.ReticleComponentJob();
        List<Infos.ReticleComponentJob> strReticleComponentJobList = Arrays.asList(reticleComponentJob);
        reticleComponentJob.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        reticleComponentJob.setPriority(1L);
        reticleComponentJob.setReticleDispatchJobID(strReticleDispatchJobList.get(0).getReticleDispatchJobID());
        reticleComponentJob.setReticleComponentJobID(this.timeStampGetDR());//timeStamp_GetDR out todo confrim it
        reticleComponentJob.setReticleDispatchJobRequestUserID(strReticleDispatchJobList.get(0).getRequestUserID());
        reticleComponentJob.setJobSeq(1L);
        reticleComponentJob.setReticlePodID(reticlePodID);
        reticleComponentJob.setSlotNo(CimNumberUtils.longValue(slotNumber));
        reticleComponentJob.setReticleID(reticleID);
        reticleComponentJob.setJobName(jobName);
        reticleComponentJob.setToEquipmentID(toMachineID);
        reticleComponentJob.setToReticlePodPortID(toPortID);
        reticleComponentJob.setToEquipmentCategory(toMachineCategory);
        reticleComponentJob.setFromEquipmentID(fromMachineID);
        reticleComponentJob.setFromReticlePodPortID(fromPortID);
        reticleComponentJob.setFromEquipmentCategory(fromMachineCategory);
        reticleComponentJob.setJobStatus(BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE);
        reticleComponentJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

        log.info("step6 - reticleMethod.reticleComponentJobInsertDR");
        this.reticleComponentJobInsertDR(objCommon, strReticleComponentJobList);
        //Set out structure
        log.info("step7 - reticleMethod.setReticleDispatchJobID");
        reuslt.setReticleDispatchJobID(strReticleDispatchJobList.get(0).getReticleDispatchJobID());

        log.info("step8 - reticleMethod.setReticleComponentJobID");
        reuslt.setReticleComponentJobID(strReticleComponentJobList.get(0).getReticleComponentJobID());
        return reuslt;
    }

    public String timeStampGetDR(){
        return  CimDateUtils.getCurrentDateTimeByPattern("yyyy-MM-dd-HH.mm.ss.SSS") + (100 + new Random().nextInt(900));
    }

    @Override
    public void reticleUsageCheckByRTMS(Infos.ObjCommon objCommon, String action, List<String> reticlelist, ObjectIdentifier lotID, ObjectIdentifier eqpID) {
        ReticleRTMSMoveInAndReserveReqParams reserveReqParams = new ReticleRTMSMoveInAndReserveReqParams();
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, eqpID);
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        if (!aMachine.isReticleRequired() || CimStringUtils.isEmpty(aLot.getProcessOperation().getPhotoLayer())){
            // no need reticle , return
            return;
        }
        if (CimStringUtils.equals(action, BizConstant.RTMS_RETICLE_CHECK_ACTION_MOVE_IN)
                && ObjectIdentifier.isNotEmpty(aLot.getControlJobID())) {
            reserveReqParams.setHasReserve(true);
        } else {
            reserveReqParams.setHasReserve(false);
        }
        reserveReqParams.setReticles(reticlelist);
        reserveReqParams.setUser(objCommon.getUser());
        CimWaferDO example = new CimWaferDO();
        example.setLotID(lotID.getValue());
        List<CimWaferDO> waferDOS = cimJpaRepository.findAll(Example.of(example));
        Validations.check(CimArrayUtils.isEmpty(waferDOS), retCodeConfig.getInvalidInputParam());
        reserveReqParams.setWaferCount(waferDOS.size());
        reserveReqParams.setEquipmentId(eqpID.getValue());
        reserveReqParams.setLotType(aLot.getLotType());
        reserveReqParams.setLotId(aLot.getLotID().getValue());
        reserveReqParams.setOpeCategory(action);
        reserveReqParams.setOpeNo(aLot.getOperationNumber());
        CimProcessOperation processOperation = aLot.getProcessOperation();
        if (!CimObjectUtils.isEmpty(processOperation)){
            Long passCount = processOperation.getPassCount();
            reserveReqParams.setOpePassCount(CimNumberUtils.intValue(passCount));
        }
        reserveReqParams.setProcessId(aLot.getProcessFlow().getName());
        reserveReqParams.setProductId(aLot.getProductSpecificationID().getValue());
        reserveReqParams.setTrxTime(objCommon.getTimeStamp().getReportTimeStamp());
        irtmsRemoteManager.requestRTMSMoveInAndReserveReq(reserveReqParams);
    }

    @Override
    public void reticleOpeCancelCompleteRptRTMS(Infos.ObjCommon objCommon, String action, ObjectIdentifier lotID) {
        ReticleRTMSMoveOutAndCancelReqParams params = new ReticleRTMSMoveOutAndCancelReqParams();
        params.setLotId(lotID.getValue());
        params.setOpeCategory(action);
        params.setUser(objCommon.getUser());
        CimWaferDO example = new CimWaferDO();
        example.setLotID(lotID.getValue());
        List<CimWaferDO> waferDOS = cimJpaRepository.findAll(Example.of(example));
        Validations.check(CimArrayUtils.isEmpty(waferDOS), retCodeConfig.getInvalidInputParam());
        params.setWaferCount(waferDOS.size());
        irtmsRemoteManager.requestRTMSMoveOutAndCancelReq(params);
    }

    @Override
    public void reticleDispatchJobDeleteByStatusDR(Infos.ObjCommon objCommon, String reticleDispatchJobID) {
        Validations.check(CimStringUtils.isEmpty(reticleDispatchJobID), retCodeConfig.getInvalidParameter());
        String jobStatusCreated = BizConstant.SP_RDJ_STATUS_CREATED;
        String jobStatusWaitToRelease = BizConstant.SP_RDJ_STATUS_WAITTORELEASE;
        this.removeEntitys("SELECT * FROM OSRTCLACTLIST WHERE RDJ_ID = ?1 AND (JOB_STATUS = ?2 OR JOB_STATUS = ?3)",
                CimRALDO.class, reticleDispatchJobID, jobStatusCreated, jobStatusWaitToRelease);
    }

    @Override
    public Outputs.ReticleComponentJobGetByJobNameDROut reticleComponentJobGetByJobNameDR(Infos.ObjCommon objCommon, String jobName, ObjectIdentifier reticleID, ObjectIdentifier reticlePodID) {
        Outputs.ReticleComponentJobGetByJobNameDROut out = new Outputs.ReticleComponentJobGetByJobNameDROut();

        /*--------------------------------------------*/
        /*   Find RCJ Record                          */
        /*--------------------------------------------*/
        StringBuffer hvBuffer = new StringBuffer("SELECT * FROM OSRTCLEXELIST");

        /*--------------------------------------------*/
        /*   Check parameter                          */
        /*--------------------------------------------*/
        if (CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_STORE)
                || CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_RETRIEVE)
                || CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_UNCLAMP)
                || CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_XFER)
        ) {
            hvBuffer.append(" WHERE ");
            hvBuffer.append(String.format("JOB_NAME = '%s'", jobName));
        } else {
            // Invalid Parameter
            log.error("Specified the jobName is invalid {}", jobName);
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        boolean firstCondition = TRUE;
        if (!ObjectIdentifier.isEmpty(reticleID)) {
            if (firstCondition) firstCondition = FALSE;

            hvBuffer.append(" AND ");
            hvBuffer.append(String.format("RTCL_ID = '%s'", ObjectIdentifier.fetchValue(reticleID)));
        }

        if (!ObjectIdentifier.isEmpty(reticlePodID)) {
            if (firstCondition) {
                firstCondition = FALSE;
                hvBuffer.append(" AND ");
                hvBuffer.append(String.format("RTCLPOD_ID = '%s'", ObjectIdentifier.fetchValue(reticlePodID)));
            }
        }

        hvBuffer.append(String.format(" AND JOB_STATUS != '%s'", BizConstant.SP_RCJ_STATUS_COMPLETED));

        //Check SQL validity
        hvBuffer.append(" ORDER BY JOB_SEQ");
        if (firstCondition) {
            // Invalid Parameter
            log.error("Specified parameters are invalid. Both reticleID and reticlePodID are NULL.");
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        if (log.isDebugEnabled())
            log.debug("Generated SQL sentence = {}", hvBuffer);

        Infos.ReticleComponentJob reticleComponentJob = null;
        List<CimRELDO> rels = cimJpaRepository.query(hvBuffer.toString(), CimRELDO.class);
        if (CimArrayUtils.isNotEmpty(rels)) {
            CimRELDO cimRELDO = rels.get(0);
            reticleComponentJob = new Infos.ReticleComponentJob();
            reticleComponentJob.setRequestedTimestamp(CimDateUtils.getTimestampAsString(cimRELDO.getReqTime()));
            reticleComponentJob.setPriority(cimRELDO.getPriority().longValue());
            reticleComponentJob.setReticleDispatchJobID(cimRELDO.getRdjId());
            reticleComponentJob.setReticleComponentJobID(cimRELDO.getRcjId());
            reticleComponentJob.setReticleDispatchJobRequestUserID(ObjectIdentifier.buildWithValue(cimRELDO.getReqUserId()));
            reticleComponentJob.setJobSeq(CimNumberUtils.longValue(cimRELDO.getJobSeq()));
            reticleComponentJob.setReticlePodID(ObjectIdentifier.buildWithValue(cimRELDO.getRtclpodId()));
            reticleComponentJob.setSlotNo(CimNumberUtils.longValue(cimRELDO.getSlotNo()));
            reticleComponentJob.setReticleID(ObjectIdentifier.buildWithValue((cimRELDO.getRtclId())));
            reticleComponentJob.setJobName(cimRELDO.getJobName());
            reticleComponentJob.setToEquipmentID(ObjectIdentifier.buildWithValue((cimRELDO.getToEqpId())));
            reticleComponentJob.setToReticlePodPortID(ObjectIdentifier.buildWithValue((cimRELDO.getToPortId())));
            reticleComponentJob.setToEquipmentCategory(cimRELDO.getToEqpCategory());
            reticleComponentJob.setFromEquipmentID(ObjectIdentifier.buildWithValue((cimRELDO.getFromEqpId())));
            reticleComponentJob.setFromReticlePodPortID(ObjectIdentifier.buildWithValue((cimRELDO.getFromPortId())));
            reticleComponentJob.setFromEquipmentCategory(cimRELDO.getFromEqpCategory());
            reticleComponentJob.setJobStatus(cimRELDO.getJobStatus());
            reticleComponentJob.setJobStatusChangeTimestamp(CimDateUtils.getTimestampAsString(cimRELDO.getJobStatChgTime()));
        }
        out.setStrReticleComponentJob(reticleComponentJob);

        /*--------------------------------------------*/
        /*   Get Max SEQ_NO in the RDJ_ID             */
        /*--------------------------------------------*/
        if (reticleComponentJob != null) {
            out.setJobCountInRDJ(cimJpaRepository.count("SELECT MAX(JOB_SEQ) FROM OSRTCLEXELIST WHERE RDJ_ID=?1", reticleComponentJob.getReticleDispatchJobID()));
        }
        return out;
    }

    @Override
    public void reticleJobStatusUpdateByReportDR(Infos.ObjCommon objCommon, String reticleComponentJobID, boolean jobSuccessFlag) {

        if (CimObjectUtils.isEmpty(reticleComponentJobID)) {
            return;
        }

        ;
        List<Infos.ReticleComponentJob> strReticleComponentJobList = cimJpaRepository
                .query("SELECT RDJ_ID, JOB_SEQ, JOB_STATUS FROM OSRTCLEXELIST WHERE RCJ_ID = ?1",
                CimRELDO.class, reticleComponentJobID).stream().map(rel -> {
            Infos.ReticleComponentJob reticleComponentJob = new Infos.ReticleComponentJob();
            reticleComponentJob.setReticleDispatchJobID(rel.getRdjId());
            reticleComponentJob.setJobSeq(rel.getJobSeq().longValue());
            reticleComponentJob.setJobStatus(rel.getJobStatus());
            return reticleComponentJob;
        }).collect(Collectors.toList());
        int jobCount = strReticleComponentJobList.size();
        if (log.isDebugEnabled())
            log.debug("found RCJ count {}", jobCount);

        boolean updateToComplete = jobSuccessFlag;
        if (jobCount != 1) {
            log.debug("jobCount != 1");
            updateToComplete = FALSE;
        }

        for (Infos.ReticleComponentJob reticleComponentJob : strReticleComponentJobList) {
            String hFsralJobStatus = null, hFsrelJobStatus = null;
            String reticleDispatchJobID = reticleComponentJob.getReticleDispatchJobID();
            Long targetJobSeq = reticleComponentJob.getJobSeq();
            String targetRCJStatus = reticleComponentJob.getJobStatus();

            boolean updateRequiredFlag = FALSE;
            boolean deleteRequiredFlag = FALSE;

            if (CimStringUtils.equals(targetRCJStatus, BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE)) {
                log.debug("targetRCJStatus, RCJ_STATUS_WAITTOEXECUTE == 0");
                if (updateToComplete) {
                    log.error("This report is invalid. RCJ status is 'WaitToExecute'");
                    Validations.check(retCodeConfigEx.getRcjIncomplete());
                } else {
                    if (log.isDebugEnabled())
                        log.debug("RCJ status is 'WaitToExecute'. It cannot be completed. {}", reticleComponentJobID);
                    hFsralJobStatus = BizConstant.SP_RDJ_STATUS_ERROR;
                    hFsrelJobStatus = BizConstant.SP_RCJ_STATUS_ERROR;
                    updateRequiredFlag = TRUE;
                }
            } else if (updateToComplete == TRUE &&
                    (CimStringUtils.equals(targetRCJStatus, BizConstant.SP_RCJ_STATUS_EXECUTING) ||
                            CimStringUtils.equals(targetRCJStatus, BizConstant.SP_RCJ_STATUS_ERROR))) {
                if (log.isDebugEnabled())
                    log.debug("RCJ is completed. {} {}", reticleComponentJobID, targetRCJStatus);

                long hFsrelJobSeqInd = cimJpaRepository.count("SELECT MAX(JOB_SEQ) FROM OSRTCLEXELIST WHERE RDJ_ID = ?1", reticleDispatchJobID);
                if (hFsrelJobSeqInd == targetJobSeq) {
                    if (log.isDebugEnabled())
                        log.debug("And it is last RCJ in RDJ. {}", reticleDispatchJobID);
                    deleteRequiredFlag = TRUE;
                } else {
                    if (log.isDebugEnabled())
                        log.debug("And update status of RDJ. {}", reticleDispatchJobID);
                    hFsralJobStatus = BizConstant.SP_RDJ_STATUS_WAITTOEXECUTE;
                    hFsrelJobStatus = BizConstant.SP_RCJ_STATUS_COMPLETED;
                    updateRequiredFlag = TRUE;
                }
            } else if (updateToComplete == FALSE &&
                    CimStringUtils.equals(targetRCJStatus, BizConstant.SP_RCJ_STATUS_EXECUTING)) {
                if (log.isDebugEnabled())
                    log.debug("Error occured in RCJ. {}", reticleComponentJobID);
                hFsralJobStatus = BizConstant.SP_RDJ_STATUS_ERROR;
                hFsrelJobStatus = BizConstant.SP_RCJ_STATUS_ERROR;
                updateRequiredFlag = TRUE;
            }

            if (updateRequiredFlag) {
                log.debug("Update required. Now update OSRTCLACTLIST and OSRTCLEXELIST.");
                CimRALDO cimRALExam = new CimRALDO();
                cimRALExam.setRdjID(reticleDispatchJobID);
                String _hFsralJobStatus = hFsralJobStatus;
                cimJpaRepository.findOne(Example.of(cimRALExam)).ifPresent(data -> {
                    data.setJobStatus(_hFsralJobStatus);
                    data.setJobStatChgTime(objCommon.getTimeStamp().getReportTimeStamp());
                    cimJpaRepository.save(data);
                });

                CimRELDO cimRELExam = new CimRELDO();
                cimRELExam.setRcjId(reticleComponentJobID);
                String _hFsrelJobStatus = hFsrelJobStatus;
                cimJpaRepository.findOne(Example.of(cimRELExam)).ifPresent(cimRELDO -> {
                    cimRELDO.setJobStatus(_hFsrelJobStatus);
                    cimRELDO.setJobStatChgTime(objCommon.getTimeStamp().getReportTimeStamp());
                    cimJpaRepository.save(cimRELDO);
                });
            }

            if (deleteRequiredFlag) {
                log.debug("Delete required. Now delete record from OSRTCLACTLIST and OSRTCLEXELIST.");
                CimRALDO cimRALExam = new CimRALDO();
                cimRALExam.setRdjID(reticleDispatchJobID);
                cimJpaRepository.delete(Example.of(cimRALExam));

                CimRELDO cimRELExam = new CimRELDO();
                cimRELExam.setRdjId(reticleDispatchJobID);
                cimJpaRepository.delete(Example.of(cimRELExam));
            }
        }
    }

    @Override
    public void reticlePodTransferReserve(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, ObjectIdentifier reticlePodID) {
        /*--------------------------------------*/
        /*   Get reticle pod object reference   */
        /*--------------------------------------*/
        CimReticlePod aPosReticlePod = null;
        if (ObjectIdentifier.isEmptyWithRefKey(reticlePodID)) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(reticlePodID), retCodeConfig.getNotFoundReticlePod(), "*****");
            aPosReticlePod = durableManager.findReticlePodNamed(reticlePodID.getValue());
        } else {
            aPosReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID.getReferenceKey());
        }
        Validations.check(null == aPosReticlePod, retCodeConfig.getNotFoundReticlePod(), ObjectIdentifier.fetchValue(reticlePodID));
        /*-----------------------------------------------*/
        /*   Check destination is equipment or stocker   */
        /*-----------------------------------------------*/
        log.info("step1 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, equipmentID);
        /*--------------------------------------------*/
        /*   Update reticle pod reserve information   */
        /*--------------------------------------------*/
        CimPerson aPerson = null;
        ObjectIdentifier personID = objCommon.getUser().getUserID();
        if (ObjectIdentifier.isEmptyWithRefKey(personID)) {
            aPerson = personManager.findPersonNamed(ObjectIdentifier.fetchValue(personID));
        } else {
            aPerson = baseCoreFactory.getBO(CimPerson.class, personID.getReferenceKey());
        }
        Validations.check(null == aPerson, retCodeConfig.getNotFoundPerson(), ObjectIdentifier.fetchValue(personID));

        log.info("aPosReticlePod is not null. and call two methods");
        aPosReticlePod.setLastClaimedPerson(aPerson);
        aPosReticlePod.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.info("bStorageMachineFlag == FLASE");
            CimMachine aPosMachine = null;
            if (ObjectIdentifier.isEmptyWithRefKey(equipmentID)) {
                Validations.check(ObjectIdentifier.isEmptyWithValue(equipmentID), retCodeConfig.getNotFoundEqp(), "*****");
                aPosMachine = machineManager.findMachineNamed(equipmentID.getValue());
            } else {
                aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID.getReferenceKey());
            }
            Validations.check(null == aPosMachine, retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(equipmentID));
            aPosReticlePod.setTransferDestinationEquipment(aPosMachine);
        } else {
            CimStorageMachine aPosStorageMachine = null;
            if (ObjectIdentifier.isEmptyWithRefKey(machineTypeGetOut.getStockerID())) {
                Validations.check(ObjectIdentifier.isEmptyWithValue(machineTypeGetOut.getStockerID()), retCodeConfig.getNotFoundStocker(), "******");
                aPosStorageMachine = machineManager.findStorageMachineNamed(machineTypeGetOut.getStockerID().getValue());
            } else {
                aPosStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, machineTypeGetOut.getStockerID().getReferenceKey());
            }
            Validations.check(null == aPosStorageMachine, retCodeConfig.getNotFoundStocker(), ObjectIdentifier.fetchValue(machineTypeGetOut.getStockerID()));
            aPosReticlePod.setTransferDestinationStocker(aPosStorageMachine);
            aPosReticlePod.setTransferReserveUser(aPerson);
            aPosReticlePod.setTransferReservedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        }
    }

    @Override
    public String reticlePodTransferStateGet(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {
        CimReticlePod reticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);
        Validations.check(reticlePod == null, retCodeConfig.getNotFoundReticlePod(), reticlePod);
        return reticlePod.getTransferStatus();
    }

    @Override
    public void reticlePodTransferReservationCheck(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {
        log.debug("in para reticlePodID {}", ObjectIdentifier.fetchValue(reticlePodID));

        CimReticlePod aPosReticlePod = null;
        if (ObjectIdentifier.isEmptyWithRefKey(reticlePodID)) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(reticlePodID), retCodeConfig.getNotFoundReticlePod(), "******");
            aPosReticlePod = durableManager.findReticlePodNamed(reticlePodID.getValue());
        } else {
            aPosReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID.getReferenceKey());
        }
        Validations.check(null == aPosReticlePod, retCodeConfig.getNotFoundReticlePod(), ObjectIdentifier.fetchValue(reticlePodID));

        CimMachine aPosMachine = aPosReticlePod.getTransferDestinationEquipment();
        if (null != aPosMachine) {
            log.debug("ReticlePod has transfer reserve to equipment.");
            Validations.check(true, retCodeConfigEx.getRtclPodResrved(), ObjectIdentifier.fetchValue(reticlePodID));
        }

        CimStorageMachine aPosStorageMachine = aPosReticlePod.getTransferDestinationStocker();
        if (null != aPosStorageMachine) {
            log.debug("ReticlePod has transfer reserve to BR stocker.");
            Validations.check(true, retCodeConfigEx.getRtclPodResrved(), ObjectIdentifier.fetchValue(reticlePodID));
        }
    }

    @Override
    public void reticleStateCheckForAction(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String action) {
        CimProcessDurable durableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        Validations.check(CimObjectUtils.isEmpty(durableBO),retCodeConfig.getNotFoundReticle());
        String durableState = durableBO.getDurableState();
        CimDurableSubState durableSubState = durableBO.getDurableSubState();
        String subStateId = durableSubState.getIdentifier();
        String reticleLocation = durableBO.getReticleLocation();
        if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_INSPECTION_REQUEST)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_IDLE),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_INSPECTION_REQUEST_CANCEL)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_WIATINSP),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_INSPECTION_IN)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_WIATINSP),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_INSPECTION_OUT)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_INSPECTION),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_CONFIRM_MASK_QUALITY)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_WAIT_RELEASE),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
            // Check Reticle Location
            Validations.check(!CimStringUtils.equals(reticleLocation, BizConstant.SP_RETICLE_QUALITY_CHECK),
                    new OmCode(retCodeConfigEx.getInvalidReticleLocation(), durableBO.getIdentifier(), reticleLocation));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_HOLD)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_IDLE) &&
                            !CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_HOLD) &&
                            !CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_REPAIR) &&
                            !CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_INSPECTION),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_HOLD_RELEASE)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_HOLD),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
            // Check Reticle Location
            Validations.check(!CimStringUtils.equals(reticleLocation, BizConstant.SP_RETICLE_MASK_ROOM),
                    new OmCode(retCodeConfigEx.getInvalidReticleLocation(), durableBO.getIdentifier(), reticleLocation));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_REQUEST_REPAIR)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_HOLD) &&
                            !CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_EQP),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_TERMINATE)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_HOLD),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
            // Check Reticle Location
            Validations.check(!CimStringUtils.equals(reticleLocation, BizConstant.SP_RETICLE_MASK_ROOM),
                    new OmCode(retCodeConfigEx.getInvalidReticleLocation(), durableBO.getIdentifier(), reticleLocation));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_TERMINATE_CANCEL)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_TERMINATE),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
            Validations.check(!CimStringUtils.equals(reticleLocation, BizConstant.SP_RETICLE_WAREHOUSE),
                    new OmCode(retCodeConfigEx.getInvalidReticleLocation(), durableBO.getIdentifier(), reticleLocation));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_REPAIR_IN)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_WAIT_REPAIR),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_REPAIR_OUT)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_REPAIR),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_SCRAP)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_HOLD),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
            // Check Reticle Location
            Validations.check(!CimStringUtils.equals(reticleLocation, BizConstant.SP_RETICLE_MASK_ROOM),
                    new OmCode(retCodeConfigEx.getInvalidReticleLocation(), durableBO.getIdentifier(), reticleLocation));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_SCRAP_CANCEL)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_SCRAP),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
            // Check Reticle Location
            Validations.check(!CimStringUtils.equals(reticleLocation, BizConstant.SP_RETICLE_MASK_ROOM),
                    new OmCode(retCodeConfigEx.getInvalidReticleLocation(), durableBO.getIdentifier(), reticleLocation));
        }else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_XFERCHG)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_IDLE) &&
                            !CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_EQP),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_JUST_IN)||
                CimStringUtils.equals(action, BizConstant.SP_RETICLE_JUST_OUT)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_IDLE) &&
                            !CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_EQP),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_SCAN_REQUEST)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_IDLE)
                    && !CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_HOLD),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
            // Check Reticle Location
            Validations.check(!CimStringUtils.equals(reticleLocation, BizConstant.SP_RETICLE_MASK_ROOM),
                    new OmCode(retCodeConfigEx.getInvalidReticleLocation(), durableBO.getIdentifier(), reticleLocation));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_SCAN_COMPLETE)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_WAITSCAN),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
            // Check Reticle Location
            Validations.check(!CimStringUtils.equals(reticleLocation, BizConstant.SP_RETICLE_MASK_ROOM),
                    new OmCode(retCodeConfigEx.getInvalidReticleLocation(), durableBO.getIdentifier(), reticleLocation));
        } else if (CimStringUtils.equals(action, BizConstant.SP_RETICLE_INSPECTION_TYPE_CHANGE)) {
            Validations.check(!CimStringUtils.equals(subStateId, BizConstant.SP_DRBL_SUBSTATE_WIATINSP),
                    new OmCode(retCodeConfig.getInvalidReticleStat(), reticleID.getValue(), durableState + "." + subStateId));
            // Check Reticle Location
            Validations.check(!CimStringUtils.equals(reticleLocation, BizConstant.SP_RETICLE_MASK_ROOM),
                    new OmCode(retCodeConfigEx.getInvalidReticleLocation(), durableBO.getIdentifier(), reticleLocation));
        }
    }

    @Override
    public void reticleInspectionRequest(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String inspectionType) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_MASK_ROOM);
        processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_MASK_ROOM);
        CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_WIATINSP);
        processDurableBO.setDurableSubState(aDurableSubState);
        CimCategory categoryNamed = codeManager.findCategoryNamed(BizConstant.SP_CATEGORY_INSPECTIONTYPE);
        List<CimCode> cimCodes = codeManager.allCodesFor(categoryNamed);
        boolean matchFlag = false;
        for (CimCode cimCode : cimCodes){
            if (CimStringUtils.equals(cimCode.getIdentifier(),inspectionType)){
                matchFlag = true;
                break;
            }
        }
        Validations.check(!matchFlag,retCodeConfig.getInvalidInputParam());
        processDurableBO.setInspectionType(inspectionType);
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);
    }

    @Override
    public void reticleInspectionIn(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_MASK_ROOM);
        processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_MASK_ROOM);
        CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_INSPECTION);
        processDurableBO.setDurableSubState(aDurableSubState);
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);
    }

    @Override
    public void confirmMaskQuality(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_MASK_ROOM);
        processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_MASK_ROOM);
        CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_IDLE);
        processDurableBO.setDurableSubState(aDurableSubState);
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);
    }

    @Override
    public void changeRecticleState(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String action) {
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        CimDurableSubState durableSubState = null;
        boolean changeFlag = true;
        if (BizConstant.SP_RETICLE_HOLD.equals(action)) {
            //  hold
            durableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_HOLD);
            Boolean onHold = processDurableBO.isOnHold();
            if (!onHold){
                processDurableBO.makeOnHold();
            }
        } else if (BizConstant.SP_RETICLE_HOLD_RELEASE.equals(action)) {
            // hold release
            durableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_IDLE);
            List<DurableDTO.PosDurableHoldRecord> posDurableHoldRecords = processDurableBO.allHoldRecords();
            if (CimArrayUtils.isEmpty(posDurableHoldRecords)){
                processDurableBO.makeNotOnHold();
            }else {
                changeFlag = false;
            }
        } else if (BizConstant.SP_RETICLE_TERMINATE.equals(action)) {
            //  terminate
            durableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_TERMINATE);
            this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_WAREHOUSE);
            processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_WAREHOUSE);
        } else if (BizConstant.SP_RETICLE_TERMINATE_CANCEL.equals(action)) {
            // Terminate Cancel
            durableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_HOLD);
            this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_MASK_ROOM);
            processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_MASK_ROOM);
        } else if (BizConstant.SP_RETICLE_SCRAP.equals(action)) {
            // Scrap
            durableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_SCRAP);
            this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_MASK_ROOM);
            processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_MASK_ROOM);
        } else if (BizConstant.SP_RETICLE_SCRAP_CANCEL.equals(action)) {
            // Scrap Cancel
            durableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_HOLD);
            this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_MASK_ROOM);
            processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_MASK_ROOM);
        }
        if (durableSubState != null && changeFlag) {
            processDurableBO.setDurableSubState(durableSubState);
        }
    }

    @Override
    public void reticleHold(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleHoldReqParams params) {
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        Boolean onHold = processDurableBO.isOnHold();
        Boolean bReasonLotLockFlag = false;
        Boolean aChangeStateFlag = false;
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        List<Infos.ReticleHoldReq> holdReqList = params.getHoldReqList();
        String lotHoldEqpUpdateFlag = StandardProperties.OM_EQP_HOLD_UPDATE_MODE.getValue();
        Validations.check(CimArrayUtils.isEmpty(holdReqList), retCodeConfig.getInvalidParameter());
        if ("-1".equals(lotHoldEqpUpdateFlag)) {
            for (int i = 0; i < holdReqList.size(); i++) {
                if (CimStringUtils.equals(BizConstant.SP_REASON_LOTLOCK, holdReqList.get(i).getHoldReasonCodeID().getValue())) {
                    bReasonLotLockFlag = true;
                    break;
                }
            }
        }
        if ("1".equals(lotHoldEqpUpdateFlag) || !bReasonLotLockFlag) {
            dispatchingManager.addToDurableHoldQueue(processDurableBO.getDurableCategory(), reticleID);
        }

        if (!onHold) {
            processDurableBO.makeOnHold();
            processDurableBO.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            processDurableBO.setLastClaimedPerson(aPerson);
            aChangeStateFlag = true;
        }

        if ("1".equals(lotHoldEqpUpdateFlag) || !bReasonLotLockFlag) {
            dispatchingManager.removeFromDurableHoldQueue(processDurableBO.getDurableCategory(), reticleID);
        }

        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);

        for (int i = 0; i < CimArrayUtils.getSize(holdReqList); i++) {
            DurableDTO.PosDurableHoldRecord holdRecord = new DurableDTO.PosDurableHoldRecord();
            Infos.ReticleHoldReq reticleHoldReq = holdReqList.get(i);
            CimDurableHoldRecordDO example = new CimDurableHoldRecordDO();
            example.setRelatedDurableID(reticleID.getValue());
            example.setRelatedDurableObj(reticleID.getReferenceKey());
            example.setHoldReasonID(reticleHoldReq.getHoldReasonCodeID().getValue());
            example.setHoldType(BizConstant.SP_HOLDTYPE_RETICLEHOLD);
            List<CimDurableHoldRecordDO> holdRecordDOS = cimJpaRepository.findAll(Example.of(example));
            Validations.check(CimArrayUtils.isNotEmpty(holdRecordDOS),retCodeConfig.getExistSameHold());

            holdRecord.setHoldType(BizConstant.SP_HOLDTYPE_RETICLEHOLD);
            holdRecord.setReasonCode(reticleHoldReq.getHoldReasonCodeID());

            holdRecord.setHoldPerson(objCommon.getUser().getUserID());
            holdRecord.setHoldTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
            holdRecord.setRelatedDurable(reticleID);
            holdRecord.setResponsibleOperationName(reticleHoldReq.getResponsibleOperationMark());
            holdRecord.setHoldClaimMemo(reticleHoldReq.getClaimMemo());
            processDurableBO.addHoldRecord(holdRecord);
        }
    }

    @Override
    public void reticleHoldRelease(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleHoldReleaseReqParams params) {
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        List<Infos.ReticleHoldReq> holdReqList = params.getHoldReqList();
        ObjectIdentifier releaseReasonCodeID = params.getReleaseReasonCodeID();
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        if (ObjectIdentifier.isEmpty(releaseReasonCodeID)){
            //old mode
            processDurableBO.removeAllHoldRecords();
            processDurableBO.setLastClaimedPerson(aPerson);
            processDurableBO.setLastUsedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            return;
        }
        if (CimArrayUtils.isEmpty(holdReqList)){
            //release all hold for RTMS
            processDurableBO.removeAllHoldRecords();
            processDurableBO.setLastClaimedPerson(aPerson);
            processDurableBO.setLastUsedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        }else {
            for (Infos.ReticleHoldReq reticleHoldReq: holdReqList) {
                if (CimStringUtils.equals(reticleHoldReq.getHoldType(), BizConstant.SP_HOLDTYPE_RETICLEHOLD)) {
                    DurableDTO.PosDurableHoldRecord holdRecord = new DurableDTO.PosDurableHoldRecord();
                    holdRecord.setHoldType(BizConstant.SP_HOLDTYPE_RETICLEHOLD);
                    holdRecord.setReasonCode(reticleHoldReq.getHoldReasonCodeID());

                    holdRecord.setHoldPerson(objCommon.getUser().getUserID());
                    holdRecord.setHoldTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                    holdRecord.setRelatedDurable(reticleID);
                    holdRecord.setResponsibleOperationName(reticleHoldReq.getResponsibleOperationMark());
                    holdRecord.setHoldClaimMemo(reticleHoldReq.getClaimMemo());
                    processDurableBO.removeHoldRecord(holdRecord);
                    processDurableBO.setLastClaimedPerson(aPerson);
                    processDurableBO.setLastUsedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                }
            }
        }
    }

    @Override
    public List<Infos.ReticleHoldListAttributes> findHoldReticleListInq(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        Validations.check(processDurableBO == null, new OmCode(retCodeConfig.getNotFoundLot(), reticleID.getValue()));
        List<DurableDTO.PosDurableHoldRecord> holdRecords = processDurableBO.allHoldRecords();
        int hrLen = CimArrayUtils.getSize(holdRecords);
        if (hrLen == 0) {
            throw new ServiceException(retCodeConfig.getNotFoundEntry());
        }
        List<Infos.ReticleHoldListAttributes> lotHoldListAttributesList = new ArrayList<>();
        CimCategory reticleHoldCategory = codeManager.findCategoryNamed(BizConstant.SP_HOLDTYPE_RETICLEHOLD);
        Validations.check(reticleHoldCategory == null, retCodeConfig.getNotFoundCategory());
        CimCategory reticleHoldReleaseCategory = codeManager.findCategoryNamed(BizConstant.SP_HOLDTYPE_RELEASE_RETICLE_HOLD);
        Validations.check(reticleHoldReleaseCategory == null, retCodeConfig.getNotFoundCategory());
        for (int i = 0; i < hrLen; i++) {
            DurableDTO.PosDurableHoldRecord holdRecord = holdRecords.get(i);
            Infos.ReticleHoldListAttributes reticleHoldListAttributes = new Infos.ReticleHoldListAttributes();
            lotHoldListAttributesList.add(reticleHoldListAttributes);

            reticleHoldListAttributes.setHoldType(holdRecord.getHoldType());
            ObjectIdentifier reasonCode = holdRecord.getReasonCode();
            reticleHoldListAttributes.setReasonCodeID(reasonCode);
            reticleHoldListAttributes.setRelatedLotID(holdRecord.getRelatedDurable());
            CimCode aReasonCode = null;
            if (CimStringUtils.equals(reticleHoldListAttributes.getHoldType(), BizConstant.SP_REASONCAT_DURABLEHOLD)) {
                aReasonCode = codeMethod.convertCodeIDToCodeOr(reticleHoldCategory, reasonCode);
            } else if (CimStringUtils.equals(reticleHoldListAttributes.getHoldType(), BizConstant.SP_REASONCAT_DURABLEHOLDRELEASE)) {
                aReasonCode = codeMethod.convertCodeIDToCodeOr(reticleHoldReleaseCategory, reasonCode);
            } else {
                Validations.check(aReasonCode == null, retCodeConfig.getNotFoundCode());
            }
            if (null != aReasonCode) {
                reticleHoldListAttributes.setCodeDescription(aReasonCode.getDescription());
            }
            reticleHoldListAttributes.setUserID(holdRecord.getHoldPerson());
            com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, holdRecord.getHoldPerson());
            if (aPerson != null) {
                reticleHoldListAttributes.setUserName(aPerson.getFullName());
            }
            reticleHoldListAttributes.setHoldTimeStamp(holdRecord.getHoldTimeStamp());
            reticleHoldListAttributes.setResponsibleOperationMark(holdRecord.getResponsibleOperationName());
            reticleHoldListAttributes.setClaimMemo(holdRecord.getHoldClaimMemo());
        }
        return lotHoldListAttributesList;
    }

    @Override
    public ReticleUpdateParamsInfo reticleUpdateParamsGet(Infos.ObjCommon objCommon) {
        ReticleUpdateParamsInfo reticleUpdateParamsInfo = new ReticleUpdateParamsInfo();
        CimCodeDO example = new CimCodeDO();
        //get department
        example.setCategoryID(BizConstant.SP_CATEGORY_DEPARTMENT);
        List<CimCodeDO> departmentCodeList = cimJpaRepository.findAll(Example.of(example));
        List<String> departmentList = new ArrayList<>();
        for (CimCodeDO cimCodeDO : departmentCodeList){
            departmentList.add(cimCodeDO.getCodeID());
        }
        //get doc
        example.setCategoryID("DOC");
        List<CimCodeDO> DocCodeList = cimJpaRepository.findAll(Example.of(example));
        List<String> DocList = new ArrayList<>();
        for (CimCodeDO cimCodeDO : DocCodeList){
            DocList.add(cimCodeDO.getCodeID());
        }
        //get fab
        CimAreaDO fabExample = new CimAreaDO();
        fabExample.setAreaCategory(BizConstant.SP_AREACATEGORY_LOCATION);
        List<CimAreaDO> fabDOList = cimJpaRepository.findAll(Example.of(fabExample));
        List<String> fabList = new ArrayList<>();
        for (CimAreaDO cimAreaDO : fabDOList){
            fabList.add(cimAreaDO.getAreaID());
        }
        //get user
        String sql = "SELECT * FROM OMUSER";
        List<CimPersonDO> personDOS = cimJpaRepository.query(sql, CimPersonDO.class);
        List<String> userList = new ArrayList<>();
        for (CimPersonDO cimPersonDO : personDOS){
            userList.add(cimPersonDO.getUserID());
        }
        reticleUpdateParamsInfo.setDepartmentList(departmentList);
        reticleUpdateParamsInfo.setFabList(fabList);
        reticleUpdateParamsInfo.setDocCategoryList(DocList);
        reticleUpdateParamsInfo.setUserList(userList);
        return reticleUpdateParamsInfo;
    }

    @Override
    public void reticleTerminate(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleTerminateReqParams params) {
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        List<Infos.ReticleReasonReq> holdReqList = params.getTerminateReqList();
        Validations.check(CimArrayUtils.isEmpty(holdReqList), retCodeConfig.getInvalidParameter());
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);

        for (int i = 0; i < CimArrayUtils.getSize(holdReqList); i++) {
            DurableDTO.PosDurableHoldRecord holdRecord = new DurableDTO.PosDurableHoldRecord();
            Infos.ReticleReasonReq reticleReasonReq = holdReqList.get(i);
            holdRecord.setHoldType(BizConstant.SP_HOLDTYPE_RETICLE_TERMINATE);
            holdRecord.setReasonCode(reticleReasonReq.getHoldReasonCodeID());
            holdRecord.setHoldPerson(objCommon.getUser().getUserID());
            holdRecord.setHoldTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
            holdRecord.setRelatedDurable(reticleID);
            holdRecord.setResponsibleOperationName(reticleReasonReq.getResponsibleOperationMark());
            holdRecord.setHoldClaimMemo(reticleReasonReq.getClaimMemo());
            processDurableBO.addHoldRecord(holdRecord);
        }
    }

    @Override
    public void reticleTerminateCancel(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleTerminateReqParams params) {
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        List<DurableDTO.PosDurableHoldRecord> posDurableHoldRecords = processDurableBO.allHoldRecords();
        for (DurableDTO.PosDurableHoldRecord posDurableHoldRecord : posDurableHoldRecords) {
            if (CimStringUtils.equals(posDurableHoldRecord.getHoldType(), BizConstant.SP_HOLDTYPE_RETICLE_TERMINATE)) {
                processDurableBO.removeHoldRecord(posDurableHoldRecord);
            }
        }
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        processDurableBO.setLastClaimedPerson(aPerson);
        processDurableBO.setLastUsedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
    }

    @Override
    public void reticleScrap(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleScrapReqParams params) {
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        List<Infos.ReticleReasonReq> holdReqList = params.getReqList();
        Validations.check(CimArrayUtils.isEmpty(holdReqList), retCodeConfig.getInvalidParameter());
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);

        for (int i = 0; i < CimArrayUtils.getSize(holdReqList); i++) {
            DurableDTO.PosDurableHoldRecord holdRecord = new DurableDTO.PosDurableHoldRecord();
            Infos.ReticleReasonReq reticleReasonReq = holdReqList.get(i);
            holdRecord.setHoldType(BizConstant.SP_HOLDTYPE_RETICLE_SCRAP);
            holdRecord.setReasonCode(reticleReasonReq.getHoldReasonCodeID());

            holdRecord.setHoldPerson(objCommon.getUser().getUserID());
            holdRecord.setHoldTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
            holdRecord.setRelatedDurable(reticleID);
            holdRecord.setResponsibleOperationName(reticleReasonReq.getResponsibleOperationMark());
            holdRecord.setHoldClaimMemo(reticleReasonReq.getClaimMemo());
            processDurableBO.addHoldRecord(holdRecord);
        }
    }

    @Override
    public void reticleScrapCancel(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Params.ReticleScrapReqParams params) {
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        List<DurableDTO.PosDurableHoldRecord> posDurableHoldRecords = processDurableBO.allHoldRecords();
        for (DurableDTO.PosDurableHoldRecord posDurableHoldRecord : posDurableHoldRecords) {
            if (CimStringUtils.equals(posDurableHoldRecord.getHoldType(), BizConstant.SP_HOLDTYPE_RETICLE_SCRAP)) {
                processDurableBO.removeHoldRecord(posDurableHoldRecord);
            }
        }
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        processDurableBO.setLastClaimedPerson(aPerson);
        processDurableBO.setLastUsedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
    }

    @Override
    public void reticleScanRequest(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class,
                BizConstant.SP_DRBL_SUBSTATE_WAITSCAN);
        try {
            processDurableBO.setDurableSubState(aDurableSubState);
        } catch (IllegalParameterException e) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidReticleStat(),
                    reticleID,processDurableBO.getDurableState()+"."+processDurableBO.getDurableSubState().getIdentifier()));
        }
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);
    }

    @Override
    public void reticleScanCompleteSuccess(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, ObjectIdentifier releaseCodeId) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class,
                BizConstant.SP_DRBL_SUBSTATE_IDLE);
        processDurableBO.setDurableSubState(aDurableSubState);
        processDurableBO.removeAllHoldRecords();
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);
    }

    @Override
    public void reticleScanCompleteFail(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, Infos.ReticleReasonReq reticleHoldReason) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class,
                BizConstant.SP_DRBL_SUBSTATE_HOLD);
        processDurableBO.setDurableSubState(aDurableSubState);
        List<DurableDTO.PosDurableHoldRecord> posDurableHoldRecords = processDurableBO.allHoldRecords();
        Validations.check(CimArrayUtils.isEmpty(posDurableHoldRecords) &&
                        (CimObjectUtils.isEmpty(reticleHoldReason) || ObjectIdentifier.isEmpty(reticleHoldReason.getHoldReasonCodeID())),
                retCodeConfig.getInvalidInputParam());
        if(!CimObjectUtils.isEmpty(reticleHoldReason)){
            //add hold record
            DurableDTO.PosDurableHoldRecord holdRecord = new DurableDTO.PosDurableHoldRecord();
            CimDurableHoldRecordDO example = new CimDurableHoldRecordDO();
            example.setRelatedDurableObj(reticleID.getReferenceKey());
            example.setRelatedDurableID(reticleID.getValue());
            example.setHoldReasonID(reticleHoldReason.getHoldReasonCodeID().getValue());
            example.setHoldType(BizConstant.SP_HOLDTYPE_RETICLEHOLD);
            List<CimDurableHoldRecordDO> holdRecordDOS = cimJpaRepository.findAll(Example.of(example));
            Validations.check(CimArrayUtils.isNotEmpty(holdRecordDOS),retCodeConfig.getExistSameHold());

            holdRecord.setHoldType(BizConstant.SP_HOLDTYPE_RETICLEHOLD);
            holdRecord.setReasonCode(reticleHoldReason.getHoldReasonCodeID());
            holdRecord.setHoldPerson(objCommon.getUser().getUserID());
            holdRecord.setHoldTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
            holdRecord.setRelatedDurable(reticleID);
            holdRecord.setResponsibleOperationName(reticleHoldReason.getResponsibleOperationMark());
            holdRecord.setHoldClaimMemo(reticleHoldReason.getClaimMemo());
            processDurableBO.addHoldRecord(holdRecord);
        }
        boolean bOnHoldFlag = processDurableBO.isOnHold();
        if (CimBooleanUtils.isFalse(bOnHoldFlag)) {
            processDurableBO.makeOnHold();
            processDurableBO.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        }
        processDurableBO.setLastClaimedPerson(aPerson);
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
    }

    @Override
    public void reticleInspectionTypeChange(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String inspectionType) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        processDurableBO.setInspectionType(inspectionType);
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);
    }

    @Override
    public Infos.ReticleHistoryGetDROut reticleHistoryGetDR(Infos.ObjCommon objCommon, Infos.ReticleHistoryGetDRIn reticleHistoryGetDRIn) {

        String HV_BUFFER;
        String HV_WHERETIMESTAMP;
        String HV_TMPBUFFER;

        String claimTime;
        String historyName;
        String reticleType;
        String operationCategory;
        String reticleStatus;
        String reticleSubStatus;
        String reticleGrade;
        String reticleLocation;
        String reticlePodID;
        String inspectionType;
        String eqpID;
        String stockerID;
        String reasonCode;
        String transactionID;
        String xferStatus;
        String claimUser;
        String claimMemo;


        HV_TMPBUFFER = "";
        HV_WHERETIMESTAMP = "";
        HV_BUFFER = "";

        Long extend_len = 500L;
        Long t_len = extend_len;
        Long recordInfCount = 0L;
        Long recordValCount = 0L;

        List<Infos.TableRecordInfo> strTableRecordInfoSeq = new ArrayList<>();

        List<Infos.TableRecordValue> strTableRecordValueSeq = new ArrayList<>();
        long totalSize = 0;

        String fromTimeStamp = reticleHistoryGetDRIn.getFromTimeStamp();
        String toTimeStamp = reticleHistoryGetDRIn.getToTimeStamp();
        Long maxRecordCount = reticleHistoryGetDRIn.getMaxRecordCount();
        SearchCondition searchCondition = reticleHistoryGetDRIn.getSearchCondition();
        List<Infos.TargetTableInfo> strTargetTableInfoSeq = reticleHistoryGetDRIn.getStrTargetTableInfoSeq();
        Validations.check(CimArrayUtils.getSize(strTargetTableInfoSeq) == 0, retCodeConfig.getInvalidParameterWithMsg());

        int FHDRCHS_index = -1;
        int OHRTLHS_index = -1;


        boolean issueFlag = false;

        for (int paramIndex = 0; paramIndex < CimArrayUtils.getSize(strTargetTableInfoSeq); paramIndex++) {

            Infos.TargetTableInfo strTargetTableInfo = strTargetTableInfoSeq.get(paramIndex);
            if (OHRTLHS_index == -1 && CimStringUtils.equals(BizConstant.SP_HISTORYTABLENAME_OHRTLHS, strTargetTableInfo.getTableName())) {
                OHRTLHS_index = paramIndex;
                recordInfCount = SET_RECORD_INFO(BizConstant.SP_HISTORYTABLENAME_OHRTLHS, selectItemOHRTLHS, strTableRecordInfoSeq, recordInfCount);
            } else if (OHRTLHS_index == -1 && CimStringUtils.equals(BizConstant.SP_HISTORYTABLENAME_OHRTLHS_ISSUE, strTargetTableInfo.getTableName())) {
                OHRTLHS_index = paramIndex;
                recordInfCount = SET_RECORD_INFO(BizConstant.SP_HISTORYTABLENAME_OHRTLHS, selectItemOHRTLHS, strTableRecordInfoSeq, recordInfCount);
                issueFlag = true;
            }else if (FHDRCHS_index == -1 && CimStringUtils.equals(BizConstant.SP_HISTORYTABLENAME_FHDRCHS, strTargetTableInfo.getTableName())) {
                FHDRCHS_index = paramIndex;
                recordInfCount = SET_RECORD_INFO(BizConstant.SP_HISTORYTABLENAME_FHDRCHS, selectItemFHDRCHS, strTableRecordInfoSeq, recordInfCount);
            } else {
                Validations.check(true, retCodeConfig.getInvalidParameterWithMsg());
            }
        }

        Infos.ReticleHistoryGetDROut strReticleHistoryGetDROut = new Infos.ReticleHistoryGetDROut();
        strReticleHistoryGetDROut.setStrTableRecordInfoSeq(strTableRecordInfoSeq);

        if (CimStringUtils.length(fromTimeStamp) > 0) {
            HV_TMPBUFFER = String.format(" WHERE TRX_TIME >= TO_TIMESTAMP('%s','yyyy-MM-dd HH24:mi:ss')", fromTimeStamp);
            HV_WHERETIMESTAMP = HV_TMPBUFFER;
        }
        if (CimStringUtils.length(toTimeStamp) > 0) {
            if (CimStringUtils.length(HV_WHERETIMESTAMP) == 0) {
                HV_WHERETIMESTAMP = " WHERE";
            } else {
                HV_WHERETIMESTAMP += " AND";
            }
            HV_TMPBUFFER = String.format(" TRX_TIME <= TO_TIMESTAMP('%s','yyyy-MM-dd HH24:mi:ss')", toTimeStamp);
            HV_WHERETIMESTAMP += HV_TMPBUFFER;
        }


        Long selectCnt = 0L;

        Object[] resultObject = GET_SELECT_SQL(BizConstant.SP_HISTORYTABLENAME_OHRTLHS, OHRTLHS_index, selectItemOHRTLHS, paramKeyOHRTLHS, strTargetTableInfoSeq, selectCnt, HV_BUFFER, HV_TMPBUFFER, HV_WHERETIMESTAMP);
        Validations.check(!Validations.isSuccess((OmCode) resultObject[4]), (OmCode) resultObject[4]);

        selectCnt = (Long) resultObject[0];
        HV_BUFFER = (String) resultObject[1];
        HV_TMPBUFFER = (String) resultObject[2];
        HV_WHERETIMESTAMP = (String) resultObject[3];

        resultObject = GET_SELECT_SQL(BizConstant.SP_HISTORYTABLENAME_FHDRCHS, FHDRCHS_index, selectItemFHDRCHS, paramKeyFHDRCHS, strTargetTableInfoSeq, selectCnt, HV_BUFFER, HV_TMPBUFFER, HV_WHERETIMESTAMP);
        Validations.check(!Validations.isSuccess((OmCode) resultObject[4]), (OmCode) resultObject[4]);

        selectCnt = (Long) resultObject[0];
        HV_BUFFER = (String) resultObject[1];
        HV_TMPBUFFER = (String) resultObject[2];
        HV_WHERETIMESTAMP = (String) resultObject[3];

        if ((CimStringUtils.length(toTimeStamp) > 0 && CimStringUtils.length(fromTimeStamp) == 0)
                || (CimStringUtils.length(toTimeStamp) == 0 && CimStringUtils.length(fromTimeStamp) == 0)) {
            HV_BUFFER += " ORDER BY TRX_TIME DESC";
        } else {
            HV_BUFFER += " ORDER BY TRX_TIME ASC";
        }

        Boolean bConvertFlag = false;
        String originalSQL = "";
        originalSQL = HV_BUFFER;

        List<Object[]> DURHISGET_C1 = null;
        if (searchCondition != null) {
            Page<Object[]> queryPage = cimJpaRepository.query(HV_BUFFER, searchCondition);
            totalSize = queryPage.getTotalElements();
            DURHISGET_C1 = queryPage.getContent();
        } else {
            DURHISGET_C1 = cimJpaRepository.query(HV_BUFFER);
        }


        Long entValMaxCount = StandardProperties.OM_DRBL_MAX_COUNT_FOR_HIST_INQ.getLongValue();

        Long limitCount = 0L;
        if (maxRecordCount < 1 || maxRecordCount > entValMaxCount) {
            limitCount = 100L;
        } else {
            limitCount = maxRecordCount;
        }

        if (DURHISGET_C1 != null) {
            for (Object[] obj : DURHISGET_C1) {
                claimTime = "";
                historyName = "";
                reticleType = "";
                operationCategory = "";
                reticleStatus = "";
                reticleSubStatus = "";
                reticleGrade = "";
                reticleLocation = "";
                reticlePodID = "";
                inspectionType = "";
                eqpID = "";
                stockerID = "";
                reasonCode = "";
                transactionID = "";
                xferStatus = "";
                claimMemo = "";
                claimUser = "";


                claimTime = CimObjectUtils.toString(obj[0]);
                historyName = CimObjectUtils.toString(obj[1]);
                reticleType = CimObjectUtils.toString(obj[2]);
                operationCategory = CimObjectUtils.toString(obj[3]);
                inspectionType = CimObjectUtils.toString(obj[4]);
                xferStatus = CimObjectUtils.toString(obj[5]);
                reticleStatus = CimObjectUtils.toString(obj[6]);
                reticleSubStatus = CimObjectUtils.toString(obj[7]);
                reticleGrade = CimObjectUtils.toString(obj[8]);
                reticleLocation = CimObjectUtils.toString(obj[9]);
                reticlePodID = CimObjectUtils.toString(obj[10]);
                eqpID = CimObjectUtils.toString(obj[11]);
                stockerID = CimObjectUtils.toString(obj[12]);
                reasonCode = CimObjectUtils.toString(obj[13]);
                transactionID = CimObjectUtils.toString(obj[14]);
                claimMemo = CimObjectUtils.toString(obj[15]);
                claimUser = CimObjectUtils.toString(obj[16]);


                if (recordValCount >= t_len) {
                    t_len += extend_len;
                }

                recordValCount = SET_RECORD_VALUE(BizConstant.SP_HISTORYTABLENAME_FHDRCHS, selectItemFHDRCHS, historyName, strTableRecordValueSeq, CimNumberUtils.intValue(recordValCount),
                        claimTime,
                        reticleType,
                        operationCategory,
                        inspectionType,
                        xferStatus,
                        reticleStatus,
                        reticleSubStatus,
                        reticleGrade,
                        reticleLocation,
                        reticlePodID,
                        eqpID,
                        stockerID,
                        reasonCode,
                        transactionID,
                        claimMemo,
                        claimUser);
                recordValCount = SET_RECORD_VALUE(BizConstant.SP_HISTORYTABLENAME_OHRTLHS, selectItemOHRTLHS, historyName, strTableRecordValueSeq, CimNumberUtils.intValue(recordValCount),
                        claimTime,
                        reticleType,
                        operationCategory,
                        inspectionType,
                        xferStatus,
                        reticleStatus,
                        reticleSubStatus,
                        reticleGrade,
                        reticleLocation,
                        reticlePodID,
                        eqpID,
                        stockerID,
                        reasonCode,
                        transactionID,
                        claimMemo,
                        claimUser);

                if (searchCondition == null) {
                    if (recordValCount > limitCount) {
                        recordValCount = limitCount;
                        break;
                    }
                }
            }
        }

        ArrayList<String> issueList = Lists.newArrayList(BizConstant.SP_RETICLE_INSPECTION_OUT_FAIL, BizConstant.SP_RETICLE_HOLD, BizConstant.SP_RETICLE_HOLD_RELEASE, BizConstant.SP_RETICLE_TERMINATE,
                BizConstant.SP_RETICLE_TERMINATE_CANCEL, BizConstant.SP_RETICLE_SCRAP, BizConstant.SP_RETICLE_SCRAP_CANCEL);
        if (issueFlag){
            Iterator<Infos.TableRecordValue> iterator = strTableRecordValueSeq.iterator();
            while (iterator.hasNext()){
                Infos.TableRecordValue tableRecordValue = iterator.next();
                List<Object> columnValues = tableRecordValue.getColumnValues();
                int size = columnValues.size();
                if (size == 17){
                    String operation = (String) columnValues.get(3);
                    if (CimStringUtils.isNotEmpty(operation)){
                        if (!CimStringUtils.equalsIn(operation,issueList)){
                            iterator.remove();
                        }
                    }
                }
            }
        }

        strReticleHistoryGetDROut.setStrTableRecordValueSeq(strTableRecordValueSeq);
        if (searchCondition != null) {
            strReticleHistoryGetDROut.setStrTableRecordValuePage(
                    CimPageUtils.convertListToPage(strTableRecordValueSeq, searchCondition.getPage(), searchCondition.getSize(), totalSize));
        }

        return strReticleHistoryGetDROut;
    }

    @Override
    public void reticleEventInfoSet(Inputs.ReticleOperationEventMakeParams params, ObjectIdentifier reticleID) {
        CimProcessDurable aReticle = baseCoreFactory.getBOByIdentifier(CimProcessDurable.class, reticleID.getValue());
        params.setReticleID(reticleID.getValue());
        params.setReticleObj(reticleID.getReferenceKey());
        params.setReticleLocation(aReticle.getReticleLocation());
        params.setReticleStatus(aReticle.getDurableState());
        params.setReticleSubStatus(aReticle.getDurableSubState().getIdentifier());
        params.setTransferStatus(aReticle.getTransferStatus());
        params.setInspectionType(aReticle.getInspectionType());
    }

    private Long SET_RECORD_INFO(String theTblName, String[][] theSelectItemDefinition, List<Infos.TableRecordInfo> strTableRecordInfoSeq, Long recordInfCount) {
        int columnCnt = 0;
        Infos.TableRecordInfo strTableRecordInfo = new Infos.TableRecordInfo();
        if (CimArrayUtils.getSize(strTableRecordInfoSeq) > recordInfCount) {
            strTableRecordInfo = strTableRecordInfoSeq.get(recordInfCount.intValue());
        } else {
            strTableRecordInfoSeq.add(strTableRecordInfo);
        }
        strTableRecordInfo.setTableName(theTblName);
        List<String> columnNames = new ArrayList<>();
        strTableRecordInfo.setColumnNames(columnNames);

        for (int defIndex = 0; defIndex < SELECT_ITEM_MAX; defIndex++) {
            if (theSelectItemDefinition[defIndex][1] != null) {
                columnNames.add(theSelectItemDefinition[defIndex][1]);
                columnCnt++;
            }
        }

        recordInfCount++;
        return recordInfCount;
    }

    public Long SET_RECORD_VALUE(String theTblName, String[][] theSelectItemDefinition,
                                 String hHISTORY_NAME, List<Infos.TableRecordValue> strTableRecordValueSeq, Integer recordValCount,
                                 String claimTime,
                                 String reticleType,
                                 String operationCategory,
                                 String inspectionType,
                                 String xferStatus,
                                 String reticleStatus,
                                 String reticleSubStatus,
                                 String reticleGrade,
                                 String reticleLocation,
                                 String reticlePodID,
                                 String eqpID,
                                 String stockerID,
                                 String reasonCode,
                                 String transactionID,
                                 String claimMemo,
                                 String claimUser) {
        if (CimStringUtils.equals(theTblName, hHISTORY_NAME)) {
            StringBuffer stringBuffer = new StringBuffer();
            int defIndex = 0;
            int columnCnt = 0;
            List<Object> columnValues = new ArrayList<>();
            Infos.TableRecordValue strTableRecordValue = new Infos.TableRecordValue();
            strTableRecordValueSeq.add(strTableRecordValue);
            strTableRecordValue.setColumnValues(columnValues);

            if (theSelectItemDefinition[defIndex++][1] != null) {
                strTableRecordValue.setReportTimeStamp(claimTime);
                columnValues.add(claimTime);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                strTableRecordValue.setTableName(hHISTORY_NAME);
                columnValues.add(hHISTORY_NAME);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(reticleType);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(operationCategory);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(inspectionType);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(xferStatus);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(reticleStatus);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(reticleSubStatus);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(reticleGrade);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(reticleLocation);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(reticlePodID);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(eqpID);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(stockerID);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(reasonCode);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(transactionID);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(claimMemo);
                columnCnt++;
            }

            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(claimUser);
                columnCnt++;
            }

            recordValCount++;
        }
        return recordValCount.longValue();
    }

    private Object[] GET_SELECT_SQL(String theTblName, Integer theInParamTblIndex, String[][] theSelectItemDefinition, String[][] theParamKeyDefinition, List<Infos.TargetTableInfo> strTargetTableInfoSeq, Long selectCnt, String HV_BUFFER, String HV_TMPBUFFER, String HV_WHERETIMESTAMP) {
        if (theInParamTblIndex != -1) {
            if (selectCnt == 0) {
                HV_BUFFER = "SELECT ";
            } else {
                HV_BUFFER += " UNION ALL SELECT ";
            }
            selectCnt++;

            StringBuilder HV_BUFFERBuilder = new StringBuilder(HV_BUFFER);
            for (int defIndex = 0; defIndex < SELECT_ITEM_MAX; defIndex++) {
                if (defIndex > 0) {
                    HV_BUFFERBuilder.append(",");
                }
                HV_BUFFERBuilder.append(theSelectItemDefinition[defIndex][0]);
            }
            HV_BUFFER = HV_BUFFERBuilder.toString();
            HV_TMPBUFFER = String.format(" FROM %s ", theTblName);
            HV_BUFFER += HV_TMPBUFFER;

            boolean bFirstCondition = true;

            if (CimStringUtils.length(HV_WHERETIMESTAMP) > 0) {
                bFirstCondition = false;
                HV_BUFFER += HV_WHERETIMESTAMP;
            }

            boolean bExistMandatoryKey = false;
            List<Infos.HashedInfo> strHashedInfoSeq = strTargetTableInfoSeq.get(theInParamTblIndex).getStrHashedInfoSeq();
            StringBuilder HV_BUFFERBuilder1 = new StringBuilder(HV_BUFFER);
            for (int paramIndex = 0; paramIndex < CimArrayUtils.getSize(strHashedInfoSeq); paramIndex++) {
                boolean bExistKey = false;
                int defIndex = 0;
                while (theParamKeyDefinition[defIndex][0] != null) {
                    if (CimStringUtils.equals(theParamKeyDefinition[defIndex][0], strHashedInfoSeq.get(paramIndex).getHashKey())) {
                        if (bFirstCondition) {
                            bFirstCondition = false;
                            HV_BUFFERBuilder1.append(" WHERE ");
                        } else {
                            HV_BUFFERBuilder1.append(" AND ");
                        }
                        HV_BUFFERBuilder1.append(theParamKeyDefinition[defIndex][1]);
                        HV_TMPBUFFER = String.format(" = '%s' ", (strHashedInfoSeq.get(paramIndex).getHashData()));
                        HV_BUFFERBuilder1.append(HV_TMPBUFFER);
                        bExistKey = true;

                        if (CimStringUtils.equalsIn(strHashedInfoSeq.get(paramIndex).getHashKey(), Arrays.asList(MANDATORY_HASH_KEY))) {
                            bExistMandatoryKey = true;
                        }
                        break;
                    }
                    defIndex++;
                }
                if (!bExistKey) {
                    return new Object[]{selectCnt, HV_BUFFERBuilder1.toString(), HV_TMPBUFFER, HV_WHERETIMESTAMP, retCodeConfig.getInvalidParameterWithMsg()};
                }
            }
            HV_BUFFER = HV_BUFFERBuilder1.toString();
            if (!bExistMandatoryKey) {
                return new Object[]{selectCnt, HV_BUFFER, HV_TMPBUFFER, HV_WHERETIMESTAMP, retCodeConfig.getInvalidParameterWithMsg()};
            }
        }
        return new Object[]{selectCnt, HV_BUFFER, HV_TMPBUFFER, HV_WHERETIMESTAMP, retCodeConfig.getSucc()};
    }


    @Override
    public String reticleInspectionOut(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, boolean operationFlag, String claimMemo) {
        User user = objCommon.getUser();
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, user.getUserID());
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        String inspectionType = processDurableBO.getInspectionType();
        this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_MASK_ROOM);
        processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_MASK_ROOM);
        CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_IDLE);
        processDurableBO.setDurableSubState(aDurableSubState);
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);
        processDurableBO.setInspectionType(null);
        return inspectionType;
    }

    @Override
    public boolean reticleRequestRepair(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        CimDurableSubState durableSubState = processDurableBO.getDurableSubState();
        String subStateIdentifier = durableSubState.getIdentifier();
        boolean eqpOutFlag = false;
        if (CimStringUtils.equals(subStateIdentifier, BizConstant.SP_DRBL_SUBSTATE_EQP)) {
            //reticle in eqp, eqp out first
            eqpOutFlag = true;
            // step1 - durable_durableControlJobID_Get
            ObjectIdentifier strDurableDurableControlJobIDGetOut = durableMethod.durableDurableControlJobIDGet(objCommon, reticleID, BizConstant.SP_DURABLECAT_RETICLE);

            if (!ObjectIdentifier.isEmpty(strDurableDurableControlJobIDGetOut)) {
                // step2 - durableControlJob_status_Get
                Infos.DurableControlJobStatusGet strDurableControlJob_status_Get_out = durableMethod.durableControlJobStatusGet(objCommon, strDurableDurableControlJobIDGetOut);

                Validations.check(!CimStringUtils.equals(strDurableControlJob_status_Get_out.getDurableControlJobStatus(), CIMStateConst.SP_CONTROL_JOB_STATUS_CREATED) &&
                        !CimStringUtils.equals(strDurableControlJob_status_Get_out.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE), retCodeConfig.getInvalidDcjstatus());
            }
            processDurableBO.makeEquipmentOut();
            this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_QUALITY_CHECK);
            processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_QUALITY_CHECK);
            CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_WAIT_REPAIR);
            CimDurableSubState tmpDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_IDLE);
            processDurableBO.setDurableSubState(tmpDurableSubState);
            processDurableBO.setDurableSubState(aDurableSubState);
        } else if (CimStringUtils.equals(subStateIdentifier, BizConstant.SP_DRBL_SUBSTATE_HOLD)) {
            this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_QUALITY_CHECK);
            processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_QUALITY_CHECK);
            CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_WAIT_REPAIR);
            processDurableBO.setDurableSubState(aDurableSubState);
        }
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);
        return eqpOutFlag;
    }

    @Override
    public void reticleRepairIn(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_QUALITY_CHECK);
        processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_QUALITY_CHECK);
        CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_REPAIR);
        processDurableBO.setDurableSubState(aDurableSubState);
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);
    }

    @Override
    public void reticleLocationTrxUpdate(Infos.ObjCommon objCommon, CimProcessDurable processDurableBO, String location){
        String currentLocation = processDurableBO.getReticleLocation();
        if (CimStringUtils.isEmpty(currentLocation)){
            return;
        }
        if (!CimStringUtils.equals(currentLocation,location)){
            //location changed
            processDurableBO.setLastLocation(currentLocation);
            processDurableBO.setLastLocationChangeTime(objCommon.getTimeStamp().getReportTimeStamp());
        }
        processDurableBO.setLastTrxID(objCommon.getTransactionID());
    }

    @Override
    public void reticleRepairOut(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String claimMemo) {
        User user = objCommon.getUser();
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, user.getUserID());
        //set the reticle to wait qc
        CimProcessDurable processDurableBO = baseCoreFactory.getBO(CimProcessDurable.class, reticleID);
        processDurableBO.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        processDurableBO.setLastClaimedPerson(aPerson);
        CimDurableSubState durableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, BizConstant.SP_DRBL_SUBSTATE_WAIT_RELEASE);
        processDurableBO.setDurableSubState(durableSubState);
        this.reticleLocationTrxUpdate(objCommon,processDurableBO,BizConstant.SP_RETICLE_QUALITY_CHECK);
        processDurableBO.setReticleLocation(BizConstant.SP_RETICLE_QUALITY_CHECK);
    }

    private <S extends NonRuntimeEntity> void removeEntitys(String querySql, Class<S> clz, Object... objects) {
        List<S> objs = cimJpaRepository.query(querySql, clz, objects);
        if (CimArrayUtils.isNotEmpty(objs)) {
            objs.forEach(x -> cimJpaRepository.delete(x));
        }
    }
}
