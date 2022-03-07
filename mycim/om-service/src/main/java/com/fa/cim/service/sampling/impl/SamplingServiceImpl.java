package com.fa.cim.service.sampling.impl;

import cn.hutool.core.util.StrUtil;
import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.LotSamplingRuleCheckParam;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.processflow.CimPFPosListDO;
import com.fa.cim.feign.ISamplingFeign;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.newcore.bo.pd.*;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.prcssdfn.ProcessFlow;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.sampling.ISamplingService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.lang.Nullable;

import java.util.*;

@OmService
@Slf4j
public class SamplingServiceImpl implements ISamplingService {

    @Autowired
    private ISamplingFeign samplingFeign;

    @Autowired
    private ILotService lotService;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    @Qualifier("ProcessDefinitionManagerCore")
    private ProcessDefinitionManager processDefinitionManager;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Override
    public void sxLotSamplingCheckThenSkipReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String transactionId, String samplingExecute) {
        try {
            // this tx whether trigger sampling
            if (!triggerSamplingCheckTx(transactionId)) {
                return;
            }

            // 【step 1】 call lot sampling service
            LotSamplingRuleCheckParam lotSamplingRuleCheckParam = new LotSamplingRuleCheckParam();
            lotSamplingRuleCheckParam.setSamplingExecute(samplingExecute);
            // set user
            User user = objCommon.getUser();
            User userParam = new User();
            BeanUtils.copyProperties(user, userParam);

            userParam.setFunctionID(TransactionIDEnum.LOT_SAMPLING_RULE_CHECK.getValue());
            lotSamplingRuleCheckParam.setUser(userParam);

            // set main process flow pd id
            CimLot lotBo = baseCoreFactory.getBO(CimLot.class, lotID);
            lotSamplingRuleCheckParam.setProcessFlowObj(lotBo.getMainProcessDefinition().getPrimaryKey());

            // set product pd id
            lotSamplingRuleCheckParam.setProductObj(lotBo.getProductSpecificationID().getReferenceKey());

            // set lot info basic
            LotSamplingRuleCheckParam.LotInfoParam lotInfoParam = new LotSamplingRuleCheckParam.LotInfoParam();
            lotInfoParam.setLotObj(lotBo.getLotID().getReferenceKey());
            lotInfoParam.setLotId(lotBo.getLotID().getValue());
            lotInfoParam.setLotPriority(lotBo.getPriorityClass());
            lotInfoParam.setLotType(lotBo.getLotType());
            lotInfoParam.setLotSubType(lotBo.getSubLotType());
            lotSamplingRuleCheckParam.setLotInfoParam(lotInfoParam);

            // set operation number
            CimProcessFlowContext processFlowContext = lotBo.getProcessFlowContext();
            List<ProcessDTO.BackupOperation> backupOperations = processFlowContext.allBackupOperations();
            Collections.reverse(backupOperations);
            boolean firstRoll = true;
            StringBuilder backStep = new StringBuilder();
            StringBuilder processFlow = new StringBuilder();

            for (int i = 0; i < backupOperations.size(); i++) {
                ProcessDTO.BackupOperation backupOperation = backupOperations.get(i);
                if (!firstRoll) {
                    backStep.append("->");
                    processFlow.append("->");
                }
                String processOperation = backupOperation.getProcessOperation();
                CimProcessOperation cimProcessOperationBO = baseCoreFactory.getBO(CimProcessOperation.class, processOperation);

                // because lot in branch flow , get main process flow
                if (CimNumberUtils.eq(0, i)) {
                    String mainFlowPdObj = cimProcessOperationBO.getMainProcessDefinition().getPrimaryKey();
                    lotSamplingRuleCheckParam.setProcessFlowObj(mainFlowPdObj);
                }

                String operationNumber = cimProcessOperationBO.getOperationNumber();
                backStep.append(operationNumber);
                processFlow.append(cimProcessOperationBO.getMainProcessDefinition().getIdentifier());

                firstRoll = false;
            }

            if (CimStringUtils.isEmpty(backStep.toString())) {
                backStep.append(lotBo.getOperationNumber());
                processFlow.append(lotBo.getMainProcessDefinition().getIdentifier());
            } else {
                backStep.append("->").append(lotBo.getOperationNumber());
                processFlow.append("->").append(lotBo.getMainProcessDefinition().getIdentifier());
            }

            lotSamplingRuleCheckParam.setOperationNumber(backStep.toString());
            lotSamplingRuleCheckParam.setProcessFlowRelation(processFlow.toString());
            CimProcessOperation processOperation = lotBo.getProcessOperation();
            if (Objects.nonNull(processOperation)) {
                CimProcessDefinition processDefinition = processOperation.getProcessDefinition();
                if (Objects.nonNull(processDefinition)) {
                    if (StrUtil.equals(processDefinition.getProcessDefinitionType(), BizConstant.SP_OPEPDTYPE_PROCESS)) {
                        if (log.isInfoEnabled()) {
                            log.info("lot process : step type is process , not execute Sampling");
                        }
                        return;
                    }
                }
            }
            // check lot whether hold
            lotSamplingRuleCheckParam.setHoldState(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD, lotBo.getLotHoldState()));
            log.info("lot hold ...... , record sampling hold");

            // set call sampling , set function id
            lotSamplingRuleCheckParam.setCallFunctionId(transactionId);

            Response response = samplingFeign.lotSamplingCheck(lotSamplingRuleCheckParam);
            if (!CimNumberUtils.eq(OmCode.SUCCESS_CODE, response.getCode())) {
                // if response error
                log.error("lotSamplingCheck() -> error:{}", response);
                return;
            }

            // 【step 2】 check response body , whether skip
            Boolean isSkip = (Boolean) response.getBody();
            if (!isSkip) {
                return;
            }

            // 【step 3】 skip
            OperationInfo operationInfo = this.getOperationInfoForOpeNumber(null, true, lotID);
            Validations.check(null == operationInfo, "Cannot find specified step.");

            // get needed parameters of locate to.
            log.info("get needed parameters of skip.");
            Params.SkipReqParams params = new Params.SkipReqParams();
            params.setCurrentOperationNumber(operationInfo.getCurrentOpeNumber());
            if (operationInfo.getJumpDirection()) {
                log.info("--> skip by forward.");
                params.setLocateDirection(true);
            } else {
                log.info("--> skip by backward.");
                params.setLocateDirection(false);
            }
            params.setClaimMemo(String.format("[LotSampling Action] Skip from %s to %s",
                    operationInfo.getCurrentOpeNumber(), operationInfo.getTargetOperationNumber()));
            params.setCurrentRouteID(operationInfo.getCurrentRootID());
            params.setLotID(ObjectIdentifier.build(lotBo.getIdentifier(), lotBo.getPrimaryKey()));

            params.setOperationID(operationInfo.getTargetOperationID());
            params.setOperationNumber(operationInfo.getTargetOperationNumber());
            params.setProcessRef(operationInfo.getProcessRef());
            params.setRouteID(operationInfo.getCurrentRootID());
            params.setSeqno(-1);
            params.setSequenceNumber(0);
            params.setUser(userParam);

            log.trace("call SkipReqService::sxSkipReq(...)");
            // update obj common
            Infos.ObjCommon duplicate = objCommon.duplicate();
            duplicate.setTransactionID(TransactionIDEnum.LOT_SAMPLING_RULE_CHECK.getValue());
            duplicate.setUser(userParam);
            lotService.sxSkipReq(duplicate, params);
            log.info("LOT sampling ::skip() exit.");

            // if tx is hold release then tx is skip TX ID
            if (CimStringUtils.equals(transactionId, TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue())) {
                transactionId = TransactionIDEnum.OPE_LOCATE_REQ.getValue();
            }

            // check
            sxLotSamplingCheckThenSkipReq(objCommon, lotID, transactionId, samplingExecute);
        } catch (Exception e) {
            log.error("call lot sampling service error: {}", e.getMessage());
        }

    }

    /**
     * Get target operation info by specified operation number.
     *
     * @param opeNumber             operation number. If Operation Number is null, return next operation info by current step.
     * @param isSameOperationNumber as a flag to judge whether Operation Number is same.
     * @return {@link OperationInfo}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/4 14:41
     */
    private OperationInfo getOperationInfoForOpeNumber(@Nullable String opeNumber, boolean isSameOperationNumber, ObjectIdentifier lotID) {
        CimLot bizObject = baseCoreFactory.getBO(CimLot.class, lotID);
        // step1: get Process Flow(Main_Ope Level).
        ProcessFlow processFlow = bizObject.getProcessFlow();
        log.trace(String.format("(1) get Process Flow(Main_Ope Level).[%s]", processFlow.getPrimaryKey()));

        // step2: get current operation number.
        String currentOpeNumber = bizObject.getOperationNumber();
        if (isSameOperationNumber) {
            Validations.check(CimStringUtils.equals(currentOpeNumber, opeNumber), "Specified Operation Number error.");
        }
        log.trace(String.format("(2) get current operation number.[%s]", currentOpeNumber));

        // step3: get PFX info of current lot.
        CimProcessFlowContext pfx = bizObject.getProcessFlowContext();
        Validations.check(null == pfx, String.format("The Lot[%s]'s PFX is null.", bizObject.getIdentifier()));
        log.trace(String.format("(3) get PFX info for current lot.[%s]", pfx.getPrimaryKey()));

        // step4: get Main PF.
        CimProcessFlow mainPF = pfx.getMainProcessFlow();
        Validations.check(null == mainPF, "Main PF is null.");
        log.trace(String.format("(4) get Main PF.[%s]", mainPF.getPrimaryKey()));

        // step5: get Main PD.
        CimProcessDefinition currentRoot = bizObject.getMainProcessDefinition();
        log.trace(String.format("(5) get Main PD.[%s]", currentRoot.getIdentifier()));

        // step6: get Process Definition(Module PF) of Main PF.
        List<ProcessDTO.ProcessDefinitionInfo> processDefinitions = mainPF.getProcessDefinitions();
        log.trace("(6) get Process Definition (Module PF) of Main PF.");

        log.trace("Process Definition (Module PF) size : " + CimArrayUtils.getSize(processDefinitions));

        // step7: get all operation info of current lot.
        log.trace("(7) get all operation info of current lot.");
        List<OperationInfo> operationInfos = new ArrayList<>();
        Optional.ofNullable(processDefinitions).ifPresent(list -> list.forEach(data -> {
            String moduleNumber = data.getNumber();
            // step7.1: get module PD info.
            CimProcessDefinition modulePD = processDefinitionManager.findModuleProcessDefinitionNamed(ObjectIdentifier.fetchValue(data.getProcessDefinition()));
            Validations.check(null == modulePD, "Module PD is null.");
            log.trace(String.format("(7.1) get module PD info.[%s]", modulePD.getIdentifier()));
            String pdLevel = modulePD.getProcessDefinitionLevel();
            // step7.2: get Active PF of the module PD.
            Validations.check(!CimStringUtils.equals(pdLevel, BizConstant.SP_PD_FLOWLEVEL_MODULE),
                    "This PD Level is not " + BizConstant.SP_PD_FLOWLEVEL_MODULE);
            CimProcessFlow activeProcessFlow = modulePD.getActiveProcessFlow();
            Validations.check(null == activeProcessFlow, "Active Process Flow is null.");
            log.trace(String.format("(7.2) get Active PF of the module PD.[%s]", activeProcessFlow.getPrimaryKey()));

            // step7.3: get operation number and pos info.
            log.trace("(7.3) get operation number and pos info.");

            CimPFPosListDO example = new CimPFPosListDO();
            example.setReferenceKey(activeProcessFlow.getPrimaryKey());
            List<CimPFPosListDO> posListDOS = cimJpaRepository.findAll(Example.of(example));
            posListDOS.sort(Comparator.comparing(CimPFPosListDO::getSequenceNumber));

            Optional.of(posListDOS).ifPresent(posList -> posList.forEach(pos -> {
                Validations.check(CimStringUtils.isEmpty(pos.getDKey()), "Can't find operation number.");
                String operationNumber = String.format("%s.%s", moduleNumber, pos.getDKey());

                // get module POS info.
                CimProcessOperationSpecification modulePOS = baseCoreFactory.getBO(CimProcessOperationSpecification.class,
                        pos.getProcessOperationSpecificationsObj());
                Validations.check(null == modulePOS, "Module POS is null.");
                CimProcessDefinition operationPD = modulePOS.getProcessDefinition();
                Validations.check(null == operationPD, "Operation PD is null.");

                OperationInfo operationInfo = new OperationInfo();
                operationInfo.setTargetOperationNumber(operationNumber);
                operationInfo.setTargetOperationID(ObjectIdentifier.build(operationPD.getIdentifier(), operationPD.getPrimaryKey()));
                operationInfo.setCurrentRootID(ObjectIdentifier.build(currentRoot.getIdentifier(), currentRoot.getPrimaryKey()));
                operationInfo.setCurrentOpeNumber(currentOpeNumber);

                Infos.ProcessRef processRef = new Infos.ProcessRef();
                operationInfo.setProcessRef(processRef);
                processRef.setMainProcessFlow(mainPF.getPrimaryKey());
                processRef.setModuleNumber(moduleNumber);
                processRef.setModulePOS(modulePOS.getPrimaryKey());
                processRef.setModuleProcessFlow(activeProcessFlow.getPrimaryKey());
                processRef.setProcessFlow(processFlow.getPrimaryKey());
                processRef.setProcessOperationSpecification("*");
                processRef.setSiInfo(null);
                operationInfos.add(operationInfo);
            }));
        }));

        int operationListSize = CimArrayUtils.getSize(operationInfos);
        log.trace("Operation List[size : " + operationListSize + "]");

        // step8: get next operation of current step and check jump direction.
        log.trace("(8) get next operation of current step and check jump direction.");
        OperationInfo nextOperationInfo = null;
        int currentIndex = -1;
        int toIndex;
        for (int i = 0; i < operationListSize; i++) {
            if (CimStringUtils.equals(currentOpeNumber, operationInfos.get(i).getTargetOperationNumber())) {
                currentIndex = i;
                if (CimStringUtils.isEmpty(opeNumber)) {
                    log.trace("(8.1) Get the next step information of the current step");
                    Validations.check(currentIndex + 1 >= operationListSize,
                            "The current step is already the last step, so you cannot do skip operations");
                    nextOperationInfo = operationInfos.get(i + 1);
                }
                break;
            }
        }

        if (CimStringUtils.isNotEmpty(opeNumber)) {
            log.trace("(8.1) Get specified step information");
            for (int j = 0; j < operationListSize; j++) {
                if (CimStringUtils.equals(opeNumber, operationInfos.get(j).getTargetOperationNumber())) {
                    toIndex = j;
                    operationInfos.get(j).setJumpDirection(toIndex > currentIndex);
                    nextOperationInfo = operationInfos.get(j);
                    break;
                }
            }
        }

        // step9: return operation info.
        log.trace("(9) return operation info.");
        return nextOperationInfo;
    }


    /**
     * description: this tx whether trigger sampling
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param transactionId - call sampling of tx id , not tx of post process
     * @return contains tx
     * @author YJ
     * @date 2020/12/26 0026 14:56
     */
    private boolean triggerSamplingCheckTx(String transactionId) {
        return CimStringUtils.equals(transactionId, TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.OPE_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.FORCE__OPE_COMP_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.FORCE_OPE_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.GATE_PASS_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.OPE_LOCATE_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.FORCE_OPE_LOCATE_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.REWORK_WHOLE_LOT_CANCEL_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.REWORK_PARTIAL_WAFER_LOT_CANCEL_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.SUBROUT_BRABCH_CANCEL_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.NON_PRO_BANK_OUT_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.BRANCH_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.SUB_ROUTE_BRANCH_REQ.getValue())
                || CimStringUtils.equals(transactionId, TransactionIDEnum.STB_RELEASED_LOT_REQ.getValue())
                ;
    }


    @Getter
    @Setter
    static class OperationInfo {
        private String targetOperationNumber;
        private ObjectIdentifier targetOperationID;
        private Infos.ProcessRef processRef;
        private ObjectIdentifier currentRootID;
        private String currentOpeNumber;
        private Boolean jumpDirection;  // support skip. true: forward  false: backward

        OperationInfo() {
            this.jumpDirection = true;
        }
    }

}
