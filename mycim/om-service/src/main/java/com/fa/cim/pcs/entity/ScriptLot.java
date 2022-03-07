package com.fa.cim.pcs.entity;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.processflow.CimPFPosListDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.newcore.bo.code.CimCode;
import com.fa.cim.newcore.bo.factory.CimBank;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.*;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.prodspec.CimProductGroup;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.prodspec.CimTechnology;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.prcssdfn.ProcessFlow;
import com.fa.cim.pcs.annotations.PcsAPI;
import com.fa.cim.pcs.annotations.PcsEntity;
import com.fa.cim.pcs.engine.ScriptThreadHolder;
import com.fa.cim.service.einfo.IElectronicInformationService;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.lang.Nullable;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@PcsEntity
@Slf4j
public class ScriptLot extends ScriptEntity<CimLot> {

    @Autowired
    private ILotService lotService;

    @Autowired
    private IProcessControlService processControlService;

    @Autowired
    private IElectronicInformationService electronicInformationService;

    @Autowired
    private ILotInqService lotInqService;

    @Autowired
    @Qualifier("ProcessDefinitionManagerCore")
    private ProcessDefinitionManager processDefinitionManager;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    public ScriptLot(CimLot bizObject) {
        super(bizObject);
    }

    /**
     * Gets current operation number for Lot.
     *
     * @return current operation number
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 11:26
     */
    public String operationNumber() {
        return bizObject.getOperationNumber();
    }

    /**
     * Gets the Lot identifier.
     *
     * @return lot identifier
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 11:25
     */
    public String lotId() {
        return bizObject.getIdentifier();
    }

    /**
     * Gets representative state of the Lot.
     *
     * @return state
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 11:22
     */
    public String state() {
        return bizObject.getState();
    }

    /**
     * Gets all wafers in the Lot.
     *
     * @return array of {@link ScriptWafer}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 11:17
     */
    public ScriptWafer[] wafers() {
        List<ProductDTO.WaferInfo> allWaferInfo = bizObject.getAllWaferInfo();
        ScriptWafer[] scriptWafers = new ScriptWafer[allWaferInfo.size()];
        int i = 0;
        for (ProductDTO.WaferInfo waferInfo : allWaferInfo) {
            scriptWafers[i++] = factory.wafer(ObjectIdentifier.fetchValue(waferInfo.getWaferID()));
        }
        return scriptWafers;
    }

    /**
     * Gets product specification for Lot.
     *
     * @return {@link ScriptProduct}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 11:16
     */
    public ScriptProduct product() {
        CimProductSpecification product = bizObject.getProductSpecification();
        Validations.check(null == product, String.format("Cannot found Product Specification of Lot[%s].", lotId()));
        return factory.generateScriptEntity(ScriptProduct.class, product);
    }

    /**
     * Gets product identifier for Lot.
     *
     * @return product identifier
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 11:19
     */
    public String productId() {
        CimProductSpecification product = bizObject.getProductSpecification();
        Validations.check(null == product, retCodeConfig.getNotFoundLot());
        return product.getIdentifier();
    }

    /**
     * Gets current hold state for Lot.
     *
     * @return hold state
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 11:14
     */
    public String holdState() {
        return bizObject.getLotHoldState();
    }

    /**
     * Gets current process state for Lot.
     *
     * @return process state
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 11:11
     */
    public String processState() {
        return bizObject.getLotProcessState();
    }

    /**
     * Gets current process operation for Lot.
     *
     * @return {@link ScriptOperation}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 11:30
     */
    public ScriptOperation currentOperation() {
        CimProcessOperation processOperation = bizObject.getProcessOperation();
        Validations.check(null == processOperation, String.format("Cannot found Current Process Operation of the Lot[%s].", lotId()));
        return factory.generateScriptEntity(ScriptOperation.class, processOperation);
    }

    /**
     * Gets completed process operation for Lot.
     *
     * @return {@link ScriptOperation}
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 14:48
     */
    public ScriptOperation completedOperation() {
        CimProcessOperation previousProcessOperation = bizObject.getPreviousProcessOperation();
        return factory.generateScriptEntity(ScriptOperation.class, previousProcessOperation);
    }

    /**
     * Gets completed process operation number for Lot.
     *
     * @return operation number
     * @version 1.0
     * @author ZQI
     * @date 2020/6/8 11:18
     */
    public String completedOperationNumber() {
        return null == completedOperation() ? null : completedOperation().operationNum();
    }

    /**
     * Gets current Route for lot.
     *
     * @return {@link ScriptRoute}
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 14:37
     */
    public ScriptRoute currentRoute() {
        CimProcessOperation po = bizObject.getProcessOperation();
        CimProcessDefinition modulePD = po.getModuleProcessDefinition();
        Validations.check(null == modulePD, String.format("Cannot found the Current Route of Lot[%s].", lotId()));
        return factory.generateScriptEntity(ScriptRoute.class, modulePD);
    }

    /**
     * Gets current route identifier for lot.
     *
     * @return route identifier
     * @version 1.0
     * @author ZQI
     * @date 2020/6/8 11:19
     */
    public String currentRouteId() {
        return null == currentRoute() ? null : currentRoute().routeId();
    }

    /**
     * Gets completed route for lot.
     *
     * @return {@link ScriptRoute}
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 14:46
     */
    public ScriptRoute completedRoute() {
        CimProcessDefinition modulePD = processDefinitionManager.findModuleProcessDefinitionNamed(this.completedOperation().routeId());
        Validations.check(null == modulePD, String.format("Cannot found the Completed Route of Lot[%s].", lotId()));
        return factory.generateScriptEntity(ScriptRoute.class, modulePD);
    }

    /**
     * Gets completed route identifier for lot.
     *
     * @return route identifier
     * @version 1.0
     * @author ZQI
     * @date 2020/6/8 11:21
     */
    public String completedRouteId() {
        return null == completedRoute() ? null : completedRoute().routeId();
    }

    /**
     * Gets completed process for lot.
     *
     * @return {@link ScriptProcess}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 11:30
     */
    public ScriptProcess completedProcess() {
        CimProcessDefinition mainPD = processDefinitionManager.findMainProcessDefinitionNamed(this.completedOperation().processId());
        Validations.check(null == mainPD, String.format("Cannot found the Completed Process of Lot[%s].", lotId()));
        return factory.generateScriptEntity(ScriptProcess.class, mainPD);
    }

    /**
     * Gets completed process identifier for lot.
     *
     * @return process identifier
     * @version 1.0
     * @author ZQI
     * @date 2020/6/8 11:23
     */
    public String completedProcessId() {
        return null == completedProcess() ? null : completedProcess().processId();
    }

    /**
     * Gets current process for lot.
     *
     * @return {@link ScriptProcess}
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 14:50
     */
    public ScriptProcess currentProcess() {
        CimProcessDefinition mainPD = bizObject.getMainProcessDefinition();
        Validations.check(null == mainPD, String.format("Cannot found the Current Process of Lot[%s].", lotId()));
        return factory.generateScriptEntity(ScriptProcess.class, mainPD);
    }

    /**
     * Gets current process identifier for lot.
     *
     * @return process identifier
     * @version 1.0
     * @author ZQI
     * @date 2020/6/8 11:24
     */
    public String currentProcessId() {
        return null == currentProcess() ? null : currentProcess().processId();
    }

    /**
     * Gets the owner for Lot.
     *
     * @return {@link ScriptUser}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 12:35
     */
    public ScriptUser owner() {
        CimPerson owner = bizObject.getLotOwner();
        Validations.check(null == owner, String.format("Cannot found the Owner of Lot[%s].", lotId()));
        return factory.generateScriptEntity(ScriptUser.class, owner);
    }

    /**
     * Gets the owner identifier for Lot.
     *
     * @return owner identifier
     * @version 1.0
     * @author ZQI
     * @date 2020/6/8 11:25
     */
    public String ownerId() {
        return null == owner() ? null : owner().userId();
    }

    /**
     * Gets carrier that the Lot is in.
     *
     * @return {@link ScriptCarrier}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 14:27
     */
    public ScriptCarrier carrier() {
        List<MaterialContainer> materialContainers = bizObject.materialContainers();
        Validations.check(CimArrayUtils.isEmpty(materialContainers), String.format("Cannot found carrier of Lot[%s].", lotId()));
        return factory.carrier(materialContainers.get(0).getIdentifier());
    }

    /**
     * Gets the carrier identifier for lot.
     *
     * @return carrier identifier
     * @version 1.0
     * @author ZQI
     * @date 2020/6/8 11:26
     */
    public String carrierId() {
        return null == carrier() ? null : carrier().carrierId();
    }

    /**
     * Get finished state of the Lot.
     *
     * @return finished state
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 14:56
     */
    public String finishedState() {
        return bizObject.getLotFinishedState();
    }

    /**
     * Get Hold types of the Lot.
     *
     * @return array of Hold Type
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 15:45
     */
    public String[] holdTypes() {
        List<ProductDTO.HoldRecord> holdRecords = bizObject.allHoldRecords();
        if (CimArrayUtils.isEmpty(holdRecords)) {
            return null;
        }
        String[] retVal = new String[holdRecords.size()];
        int count = 0;
        for (ProductDTO.HoldRecord record : holdRecords) {
            retVal[count++] = record.getHoldType();
        }
        return retVal;
    }

    /**
     * Get inventory state of the Lot.
     *
     * @return inventory state
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 15:47
     */
    public String inventoryState() {
        return bizObject.getLotInventoryState();
    }

    /**
     * Gets the order number of Lot.
     *
     * @return order number
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 16:37
     */
    public String orderNum() {
        return bizObject.getOrderNumber();
    }

    /**
     * Gets User who claimed for the lot at last.
     *
     * @return {@link ScriptUser}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 17:37
     */
    public ScriptUser lastUser() {
        CimPerson person = bizObject.getLastClaimedPerson();
        return factory.generateScriptEntity(ScriptUser.class, person);
    }

    /**
     * Gets the last user identifier for lot.
     *
     * @return last user identifier
     * @version 1.0
     * @author ZQI
     * @date 2020/6/8 11:27
     */
    public String lastUserId() {
        return null == lastUser() ? null : lastUser().userId();
    }

    /**
     * Get the timestamp that claimed for the lot at last.
     *
     * @return time
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 17:39
     */
    public Timestamp lastTimestamp() {
        return bizObject.getLastClaimedTimeStamp();
    }

    /**
     * Get priority of the Lot.
     *
     * @return priority
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 17:41
     */
    public int priorityClass() {
        return bizObject.getPriorityClass();
    }

    /**
     * Get product group id of the Lot.
     *
     * @return product group
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 17:43
     */
    public String productGroupId() {
        CimProductSpecification productSpecification = bizObject.getProductSpecification();
        Validations.check(null == productSpecification,
                String.format("Cannot found ProductSpecification of Lot[%s].", lotId()));
        CimProductGroup productGroup = productSpecification.getProductGroup();
        Validations.check(null == productGroup, "Cannot found ProductGroup");
        return productGroup.getIdentifier();
    }

    /**
     * Gets technology for lot.
     *
     * @return {@link ScriptTechnology}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:28
     */
    public ScriptTechnology technology() {
        CimProductSpecification productSpecification = bizObject.getProductSpecification();
        Validations.check(null == productSpecification,
                String.format("Cannot found ProductSpecification of Lot[%s].", lotId()));
        CimProductGroup productGroup = productSpecification.getProductGroup();
        Validations.check(null == productGroup, "Cannot found ProductGroup");
        CimTechnology technology = productGroup.getTechnology();
        return factory.generateScriptEntity(ScriptTechnology.class, technology);
    }

    /**
     * Gets the technology identifier for lot.
     *
     * @return technology identifier
     * @version 1.0
     * @author ZQI
     * @date 2020/6/8 11:28
     */
    public String technologyId() {
        return null == technology() ? null : technology().technologyId();
    }

    /**
     * Get type of the Lot.
     *
     * @return lot type
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:31
     */
    public String lotType() {
        return bizObject.getLotType();
    }

    /**
     * Get wafer quantity of the Lot.
     *
     * @return wafer quantity
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:33
     */
    public int waferQuantity() {
        return bizObject.getQuantity();
    }

    /**
     * Get the bank info for the Lot.
     *
     * @return {@link ScriptBank}
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 14:09
     */
    public ScriptBank bank() {
        CimBank bank = bizObject.getBank();
        Validations.check(null == bank, String.format("Cannot find the Bank of Lot[%s].", lotId()));
        return factory.generateScriptEntity(ScriptBank.class, bank);
    }

    /**
     * Get identifier of the Bank that current Lot in.
     *
     * @return bank id
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 14:25
     */
    public String bankId() {
        CimBank bank = bizObject.getBank();
        Validations.check(null == bank, String.format("Cannot find the Bank of Lot[%s].", lotId()));
        return bank.getIdentifier();
    }

    /**
     * Get all process job positions.
     *
     * @return positions
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 15:59
     * @todo
     */
    public Integer[] processJobPositions() {
        return null;
    }

    /**
     * Get all process wafers.
     *
     * @return array of {@link ScriptWafer}
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 15:59
     * @todo
     */
    public ScriptWafer[] processedWafers() {
        return null;
    }

    /**
     * Commit Due Date yyMMdd of the lot.
     *
     * @return Date
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 17:54
     * @todo need to confirm
     */
    public String dueDate() {
        Timestamp time = bizObject.getPlannedCompletionDateTime();
        LocalDateTime date = LocalDateTime.parse(time.toString(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        return date.toString();
    }

    /**
     * Gets the control job identifier for lot when "Processing".
     *
     * @return control job identifier
     * @version 1.0
     * @author ZQI
     * @date 2020/6/12 10:02
     */
    public String controlJobId() {
        CimControlJob controlJob = bizObject.getControlJob();
        return null == controlJob ? null : controlJob.getIdentifier();
    }


    //*****************************************************************************************************************/
    //************************************  Actions of the Lot are as follows *****************************************/
    //*****************************************************************************************************************/

    /**
     * Skip to the specified step to processing.
     *
     * @param operationNumber specified operation number for locate
     * @version 1.0
     * @author ZQI
     * @date 2019/12/3 12:48
     */
    @PcsAPI(PcsAPI.Scope.PRE_1)
    public void skipTo(String operationNumber) {
        log.info("PCS::skipTo() enter.");
        Infos.ObjCommon objCommon = ScriptThreadHolder.getObjCommon();
        Params.SkipReqParams skipToParams = this.getSkipToParams(operationNumber, objCommon);

        log.trace("call SkipReqService::sxSkipReq(...)");
        lotService.sxSkipReq(objCommon, skipToParams);
        log.info("PCS::skipTo() exit.");
    }

    /**
     * Ignore the current step, then skip to the next step.
     *
     * @version 1.0
     * @author ZQI
     * @date 2019/12/3 10:25
     */
    @PcsAPI(PcsAPI.Scope.PRE_1)
    public void skip() {
        log.info("PCS::skip() enter.");
        Infos.ObjCommon objCommon = ScriptThreadHolder.getObjCommon();
        Params.SkipReqParams skipToParams = this.getSkipToParams(null, objCommon);

        log.trace("call SkipReqService::sxSkipReq(...)");
        lotService.sxSkipReq(objCommon, skipToParams);
        log.info("PCS::skip() exit.");
    }

    /**
     * This class is used to encapsulate the relevant information of the target step.
     *
     * @author ZQI
     * @version 1.0
     * @date 2019/12/4 15:21
     */
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

    /**
     * Get the parameters required for Skip.
     *
     * @param opeNumber need to skip Operation Number.If Operation Number is null, jump from current step to next step by default.
     * @param objCommon objCommon
     * @version 1.0
     * @author ZQI
     * @date 2019/12/3 18:07
     */
    private Params.SkipReqParams getSkipToParams(@Nullable String opeNumber, Infos.ObjCommon objCommon) {
        Validations.check(null == objCommon, "ObjCommon is null.");
        if (CimStringUtils.isEmpty(opeNumber)) {
            log.trace("Operation Number is null.");
        } else {
            log.trace("Operation Number is not-null.");
        }
        OperationInfo operationInfo = this.getOperationInfoForOpeNumber(opeNumber, true);
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
        params.setClaimMemo(String.format("[PCS Action] Skip from %s to %s",
                operationInfo.getCurrentOpeNumber(), operationInfo.getTargetOperationNumber()));
        params.setCurrentRouteID(operationInfo.getCurrentRootID());
        params.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));

        params.setOperationID(operationInfo.getTargetOperationID());
        params.setOperationNumber(operationInfo.getTargetOperationNumber());
        params.setProcessRef(operationInfo.getProcessRef());
        params.setRouteID(operationInfo.getCurrentRootID());
        params.setSeqno(-1);
        params.setSequenceNumber(0);
        params.setUser(objCommon.getUser());
        return params;
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
    private OperationInfo getOperationInfoForOpeNumber(@Nullable String opeNumber, boolean isSameOperationNumber) {
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

            Optional.ofNullable(posListDOS).ifPresent(posList -> posList.forEach(pos -> {
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
     * @param code    the reason code used to hold the lot
     * @param memo    the memo description of the lot
     * @param rspMark responsible operation mark
     * @author Yuri
     */
    @PcsAPI(PcsAPI.Scope.PRE_1)
    public void hold(String code, String memo, String rspMark) {
        log.trace("PCS::hold() enter.");
        Infos.ObjCommon objCommon = ScriptThreadHolder.getObjCommon();
        ObjectIdentifier lotId = ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey());
        List<Infos.LotHoldReq> holdReqList = new ArrayList<>(1);
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
        lotHoldReq.setHoldReasonCodeID(ObjectIdentifier.buildWithValue(code));
        lotHoldReq.setClaimMemo("[PCS Action] " + memo);
        lotHoldReq.setResponsibleOperationMark(rspMark);
        lotHoldReq.setOperationNumber(bizObject.getOperationNumber());
        lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
        CimProcessDefinition route = bizObject.getMainProcessDefinition();
        lotHoldReq.setRouteID(ObjectIdentifier.build(route.getIdentifier(), route.getPrimaryKey()));
        holdReqList.add(lotHoldReq);

        log.trace("call HoldLotReqService::sxHoldLotReq(...)");
        lotService.sxHoldLotReq(objCommon, lotId, holdReqList);
        log.trace("PCS::hold() exit.");
    }

    /**
     * FutureHold is a command to register future hold to lot
     *
     * @param operationNumber operation number
     * @param code            future hold reason code
     * @param memo            claim memo
     * @version 1.0
     * @author ZQI
     * @date 2019/12/4 13:40
     */
    @PcsAPI(PcsAPI.Scope.PRE_1)
    public void futureHold(String operationNumber, String code, String memo) {
        log.info("PCS::futureHold() enter.");
        Infos.ObjCommon objCommon = ScriptThreadHolder.getObjCommon();
        Validations.check(CimStringUtils.isEmpty(operationNumber), "Specified Operation Number is null.");
        Validations.check(CimStringUtils.isEmpty(code), "Future Hold must be specified ReasonCode.");

        Params.FutureHoldReqParams params = new Params.FutureHoldReqParams();
        params.setClaimMemo("[PCS Action] " + memo);
        params.setHoldType(BizConstant.SP_HOLDTYPE_FUTUREHOLD);
        params.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));
        params.setOperationNumber(operationNumber);
        params.setPostFlag(false);
        params.setReasonCodeID(ObjectIdentifier.buildWithValue(code));
        params.setRelatedLotID(null);

        CimProcessDefinition root = bizObject.getMainProcessDefinition();
        Validations.check(null == root, "Current lot's Root is null.");
        params.setRouteID(ObjectIdentifier.build(root.getIdentifier(), root.getPrimaryKey()));

        params.setSingleTriggerFlag(true);
        params.setUser(objCommon.getUser());

        log.trace("call FutureHoldReqService::sxFutureHoldReq(...)");
        processControlService.sxFutureHoldReq(objCommon, params);
        log.info("PCS::futureHold() exit.");
    }

    /**
     * This function is used when need to recording some note of specified operation.
     *
     * @param operationNumber specified operation number
     * @param title           title
     * @param contents        contents
     * @version 1.0
     * @author ZQI
     * @date 2019/12/4 14:03
     */
    @PcsAPI({PcsAPI.Scope.POST, PcsAPI.Scope.PRE_1})
    public void setOpeNote(String operationNumber, String title, String contents) {
        log.info("PCS::setOpeNote() enter.");
        Infos.ObjCommon objCommon = ScriptThreadHolder.getObjCommon();
        Validations.check(CimStringUtils.isEmpty(operationNumber), "Specified Operation Number is null.");
        Validations.check(CimStringUtils.isEmpty(title), "This title of Operation Note is null.");
        Validations.check(CimStringUtils.isEmpty(contents), "This contents of Operation Note is null");

        Params.LotOperationNoteInfoRegisterReqParams params = new Params.LotOperationNoteInfoRegisterReqParams();
        params.setClaimMemo("Recording by PCS function.");
        params.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));
        params.setLotOperationNoteDescription(contents);
        params.setLotOperationNoteTitle(title);

        OperationInfo operationInfo = this.getOperationInfoForOpeNumber(operationNumber, false);
        Validations.check(null == operationInfo,
                String.format("Cannot found operation info of specified Operation Number[%s].", operationNumber));
        params.setRouteID(operationInfo.getCurrentRootID());

        params.setOperationNumber(operationNumber);
        params.setOperationID(operationInfo.getTargetOperationID());
        params.setUser(objCommon.getUser());

        electronicInformationService.sxLotOperationNoteInfoRegisterReq(params, objCommon);
        log.info("PCS::setOpeNote() exit.");
    }

    /**
     * This function is used to register sub-lot type for a lot.
     *
     * @param subLotType new sub-lot type
     * @version 1.0
     * @author ZQI
     * @date 2019/12/4 15:37
     */
    @PcsAPI({PcsAPI.Scope.POST, PcsAPI.Scope.PRE_1})
    public void setSubLotType(String subLotType) {
        log.info("PCS::setSubLotType() enter.");
        bizObject.setSubLotType(subLotType);
        log.info("PCS::setSubLotType() exit.");
    }

    /**
     * This function can command the lot move to main route operation where main route and sub route join.
     *
     * <p> Jump to return branch route(joining route) specified step.
     *
     * @version 1.0
     * @author ZQI
     * @date 2019/12/4 17:19
     */
    @PcsAPI(PcsAPI.Scope.PRE_1)
    public void returnFromSubRoute() {
        log.info("PCS::returnFromSubRoute enter.");
        //---------------------------- case -------------------------------------------------------------------------//
        //                                                                                                           //
        //    1000.0100   0200    0300     0400     0500     0600    mainRoute                       index 0         //
        // -------O--------O--------O--------O--------O--------O--------------->                                     //
        //                 |                          |                                                              //
        //                 O---->---O--->----O--->----O              branch01.01                     index 1         //
        //             1000.0100  0200     0300     0400                                                             //
        //                          |        |                                                                       //
        //                          O--->----O                                                                       //
        //                      1000.0100  0200                      branch02.01                     index 2         //
        //                                                                                                           //
        // if current route is "index 0"                                                                             //
        //     throw exception("Current Root is main Route, Cannot execute returnFromSubRoute() method.")            //
        // if current route is "index 1"                                                                             //
        //     return mainRoute.1000.0500 from branch01.01.*                                                         //
        // if current route is  "index 2"                                                                            //
        //     return branch01.01.1000.0300 form branch02.01.*                                                       //
        //-----------------------------------------------------------------------------------------------------------//
        CimProcessFlowContext processFlowContext = bizObject.getProcessFlowContext();
        Validations.check(null == processFlowContext,
                String.format("Current PFX of Lot[%s] is null.", bizObject.getIdentifier()));

        List<ProcessDTO.ReturnOperation> returnOperations = processFlowContext.allReturnOperations();
        Validations.check(CimArrayUtils.isEmpty(returnOperations),
                "Current Route is main Route, Cannot execute returnFromSubRoute() method.");

        // get jump need's parameters.
        Infos.ObjCommon objCommon = ScriptThreadHolder.getObjCommon();
        Params.SkipReqParams params = new Params.SkipReqParams();
        params.setClaimMemo("Return to previous operation route by PCS function.");
        params.setCurrentOperationNumber(bizObject.getOperationNumber());

        CimProcessDefinition currentRoute = bizObject.getMainProcessDefinition();
        Validations.check(null == currentRoute,
                String.format("Current Root of Lot[%s] is null.", bizObject.getIdentifier()));
        params.setCurrentRouteID(ObjectIdentifier.build(currentRoute.getIdentifier(), currentRoute.getPrimaryKey()));

        params.setLocateDirection(true);
        params.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));

        // find current operation PD
        CimProcessOperation currentPO = bizObject.getProcessOperation();
        Validations.check(null == currentPO,
                String.format("Cannot found Current PO of Lot[%s].", bizObject.getIdentifier()));

        CimProcessDefinition currentOperationPD = currentPO.getProcessDefinition();
        Validations.check(null == currentOperationPD,
                String.format("Cannot found Operation PD of Lot[%s]", bizObject.getIdentifier()));

        params.setOperationID(ObjectIdentifier.build(currentOperationPD.getIdentifier(), currentOperationPD.getPrimaryKey()));

        // find target info
        ProcessDTO.ReturnOperation returnOperation = returnOperations.get(0);
        String processFlowObj = returnOperation.getProcessFlow();
        CimProcessFlow processFlow = baseCoreFactory.getBO(CimProcessFlow.class, processFlowObj);
        Validations.check(null == processFlow, "Process Flow is null.");
        CimProcessDefinition targetRoute = processFlow.getRootProcessDefinition();
        Validations.check(null == targetRoute, "The Return Route is null.");
        params.setRouteID(ObjectIdentifier.build(targetRoute.getIdentifier(), targetRoute.getPrimaryKey()));
        params.setOperationNumber(returnOperation.getOperationNumber());

        CimProcessFlow targetModulePF = baseCoreFactory.getBO(CimProcessFlow.class, returnOperation.getModuleProcessFlow());
        Validations.check(null == targetModulePF, "Return Module PF is null");
        String moduleOpeNo = BaseStaticMethod.convertOpeNoToModuleOpeNo(returnOperation.getOperationNumber());
        CimProcessOperationSpecification targetModulePOS = targetModulePF.findProcessOperationSpecificationOnDefault(moduleOpeNo);
        Validations.check(null == targetModulePOS, "Return Module POS is null.");

        Infos.ProcessRef processRef = new Infos.ProcessRef();
        processRef.setMainProcessFlow(returnOperation.getMainProcessFlow());
        processRef.setModuleNumber(BaseStaticMethod.convertOpeNoToModuleNo(returnOperation.getOperationNumber()));
        processRef.setModulePOS(targetModulePOS.getPrimaryKey());
        processRef.setModuleProcessFlow(returnOperation.getModuleProcessFlow());
        processRef.setProcessFlow(returnOperation.getProcessFlow());
        processRef.setProcessOperationSpecification("*");
        processRef.setSiInfo(null);
        params.setProcessRef(processRef);

        params.setSeqno(-1);
        params.setSequenceNumber(0);
        params.setUser(objCommon.getUser());

        log.trace("call SkipReqService::sxSkipReq(...)");
        lotService.sxSkipReq(objCommon, params);
        log.info("PCS::returnFromSubRoute exit.");
    }

    /**
     * AllRework is a command to rework the whole lot to rework-route.
     *
     * <p> Specified rework route must be defined in MDS.
     *
     * <p> Rework Route ID should exist and it should be connected to
     * the previous operation of Lot’s current operation.
     *
     * @param reworkRouteID need to rework's routeID
     * @param code          reason code
     * @param claimMemo     claim memo
     * @version 1.0
     * @author ZQI
     * @date 2019/12/5 10:25
     */
    @PcsAPI(PcsAPI.Scope.PRE_1)
    public void allRework(String reworkRouteID, String code, String claimMemo) {
        log.info("PCS::allRework() enter.");
        Validations.check(CimStringUtils.isEmpty(reworkRouteID), "Rework Route is null. Please check input parameter.");
        Infos.ObjCommon objCommon = ScriptThreadHolder.getObjCommon();

        Params.ReworkWholeLotReqParams params = new Params.ReworkWholeLotReqParams();
        params.setClaimMemo("[PCS Action] " + claimMemo);
        params.setCurrentOperationNumber(bizObject.getOperationNumber());

        CimProcessDefinition currentRoot = bizObject.getMainProcessDefinition();
        Validations.check(null == currentRoot, "Current Root is null.");
        params.setCurrentRouteID(ObjectIdentifier.build(currentRoot.getIdentifier(), currentRoot.getPrimaryKey()));
        params.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));
        params.setReasonCodeID(ObjectIdentifier.buildWithValue(code));
        params.setUser(objCommon.getUser());

        //-------------------------------------//
        //           find branch info          //
        //-------------------------------------//
        log.trace("find branch info on current step. (Defined on previous step).");
        Params.MultiPathListInqParams multiPathListInqParams = new Params.MultiPathListInqParams();
        multiPathListInqParams.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));
        multiPathListInqParams.setRouteType(BizConstant.SP_MAINPDTYPE_REWORK);

        log.trace("call MultiPathListInqService::sxMultiPathListInq(...)");
        List<Infos.ConnectedRouteList> connectedRouteLists = lotInqService.sxMultiPathListInq(multiPathListInqParams, objCommon);
        boolean isExist = false;
        String returnOperationNumber = null;
        ObjectIdentifier subRoute = null;
        if (CimArrayUtils.isNotEmpty(connectedRouteLists)) {
            for (Infos.ConnectedRouteList routeList : connectedRouteLists) {
                if (CimStringUtils.equals(routeList.getRouteID().getValue(), reworkRouteID)) {
                    isExist = true;
                    returnOperationNumber = routeList.getReturnOperationNumber();
                    subRoute = routeList.getRouteID();
                    break;
                }
            }
        }

        Validations.check(!isExist,
                String.format("This Rework-Route[%s] not connected to the the previous operation of Lot’s current operation.",
                        reworkRouteID));
        params.setReturnOperationNumber(returnOperationNumber);
        params.setSubRouteID(subRoute);

        log.trace("call ReworkWholeLotReqService::sxReworkWholeLotReq(...)");
        lotService.sxReworkWholeLotReq(objCommon, params);
        log.info("PCS::allRework() exit.");
    }


    /**
     * Branch is a command for automatic moving the lot to specified sub-route that
     * is defined as sub-route connection for the operation in MDS.
     *
     * @param subRouteID need to branch's sub-route
     * @version 1.0
     * @author ZQI
     * @date 2019/12/9 10:34
     */
    @PcsAPI(PcsAPI.Scope.PRE_1)
    public void branch(String subRouteID) {
        log.info("PCS::branch() enter.");
        Validations.check(CimStringUtils.isEmpty(subRouteID), "Specified branch sub-route is null.");
        Infos.ObjCommon objCommon = ScriptThreadHolder.getObjCommon();

        // check the specified sub-route is exist.
        Params.MultiPathListInqParams multiPathListInqParams = new Params.MultiPathListInqParams();
        multiPathListInqParams.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));
        multiPathListInqParams.setUser(objCommon.getUser());
        log.trace("call MultiPathListInqService::sxMultiPathListInq(...)");
        List<Infos.ConnectedRouteList> connectedRouteLists = lotInqService.sxMultiPathListInq(multiPathListInqParams, objCommon);
        // get the defined branch info.
        boolean isExist = false;
        String returnOperationNumber = null;
        ObjectIdentifier branchRouteID = null;
        if (CimArrayUtils.isNotEmpty(connectedRouteLists)) {
            for (Infos.ConnectedRouteList connectedRoute : connectedRouteLists) {
                if (CimStringUtils.equals(connectedRoute.getRouteID().getValue(), subRouteID)) {
                    isExist = true;
                    branchRouteID = connectedRoute.getRouteID();
                    returnOperationNumber = connectedRoute.getReturnOperationNumber();
                    break;
                }
            }
        }
        Validations.check(!isExist,
                String.format("This Branch-Route[%s] not connected to the the previous operation of Lot’s current operation.",
                        subRouteID));

        Params.SubRouteBranchReqParams params = new Params.SubRouteBranchReqParams();
        params.setClaimMemo("Branch by PCS function.");
        params.setCurrentOperationNumber(bizObject.getOperationNumber());
        CimProcessDefinition currentRoot = bizObject.getMainProcessDefinition();
        Validations.check(null == currentRoot, String.format("Current Root of the Lot[%s] is null.", bizObject.getIdentifier()));
        params.setCurrentRouteID(ObjectIdentifier.build(currentRoot.getIdentifier(), currentRoot.getPrimaryKey()));
        params.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));
        params.setReturnOperationNumber(returnOperationNumber);
        params.setSubRouteID(branchRouteID);
        params.setUser(objCommon.getUser());

        log.trace("call SubRouteBranchReqService::sxSubRouteBranchReqService(...)");
        lotService.sxSubRouteBranchReqService(objCommon, params);
        log.info("PCS::branch() exit.");
    }

    /**
     * BranchWithOpeNo is a command for automatically moving the lot to specified sub-route with
     * return operation number that is defined as sub-route connection for the operation in MDS.
     *
     * <p> Sub Route ID should exist and should be a STRING variable or STRING constant.
     * It should be connected to the previous operation of Lot’s current operation.
     *
     * <p> The return operation number can be over-written by a difference one.
     * When the connected branch route settings are the followings:
     * <p> Branch Route ID = "SubRoute.01"
     * <p> Return operation number = "1000.0500"
     *
     * <p> Case 1. Over write return operation number, Lot will return to 1000.0700.
     * <p> branchWithOpeNo("SubRoute.01", "1000.0700", "Claim Memo")
     *
     * <p> Case 2. If return operation number is not specified, this case is same as “Branch”. Lot will return to 1000.0500.
     * <p> branchWithOpeNo("SubRoute.01", "", "Claim Memo")
     *
     * @param subRouteID  need to branch 's sub-route
     * @param returnOpeNo return main operation's number
     * @param memo        claim memo
     * @version 1.0
     * @author ZQI
     * @date 2019/12/5 18:25
     */
    @PcsAPI(PcsAPI.Scope.PRE_1)
    public void branchWithOpeNo(String subRouteID, String returnOpeNo, String memo) {
        log.info("PCS::branchWithOpeNo() enter.");
        Validations.check(CimStringUtils.isEmpty(subRouteID), "Specified branch sub-route is null.");
        Infos.ObjCommon objCommon = ScriptThreadHolder.getObjCommon();

        if (CimStringUtils.isEmpty(returnOpeNo)) {
            this.branch(subRouteID);
            return;
        }

        // check the specified sub-route is exist.
        Params.MultiPathListInqParams multiPathListInqParams = new Params.MultiPathListInqParams();
        multiPathListInqParams.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));
        log.trace("call MultiPathListInqService::sxMultiPathListInq(...)");
        List<Infos.ConnectedRouteList> connectedRouteLists = lotInqService.sxMultiPathListInq(multiPathListInqParams, objCommon);
        // get the defined branch info.
        boolean isExist = false;
        ObjectIdentifier branchRouteID = null;
        if (CimArrayUtils.isNotEmpty(connectedRouteLists)) {
            for (Infos.ConnectedRouteList connectedRoute : connectedRouteLists) {
                if (CimStringUtils.equals(connectedRoute.getRouteID().getValue(), subRouteID)) {
                    branchRouteID = connectedRoute.getRouteID();
                    isExist = true;
                    break;
                }
            }
        }
        Validations.check(!isExist,
                String.format("This Branch-Route[%s] not connected to the the previous operation of Lot’s current operation.",
                        subRouteID));

        Params.SubRouteBranchReqParams params = new Params.SubRouteBranchReqParams();
        params.setClaimMemo("Branch by PCS function.");
        params.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));
        params.setCurrentOperationNumber(bizObject.getOperationNumber());

        CimProcessDefinition currentRoot = bizObject.getMainProcessDefinition();
        Validations.check(null == currentRoot, String.format("Current Root of the Lot[%s] is null.", bizObject.getIdentifier()));
        params.setCurrentRouteID(ObjectIdentifier.build(currentRoot.getIdentifier(), currentRoot.getPrimaryKey()));

        params.setSubRouteID(branchRouteID);
        params.setReturnOperationNumber(returnOpeNo);
        params.setUser(objCommon.getUser());
        params.setClaimMemo("[PCS Action] " + memo);

        log.trace("call SubRouteBranchReqService::sxSubRouteBranchReqService(...)");
        lotService.sxSubRouteBranchReqService(objCommon, params);
        log.info("PCS::branchWithOpeNo() exit.");
    }

    /**
     * AllReworkWithOpeNo is a command to rework with return operation number on the whole lot to rework-route.
     *
     * <p>Rework Route ID should exist and should be a STRING variable or STRING constant.
     * It should be connected to the previous operation of Lot’s current operation.
     * And Reason Code ID should be registered by reason code category “Rework”.
     *
     * <p>The return operation number can be over-written by a difference one.
     * When the connected rework route settings are the followings,
     * <p>Rework Route ID = “RwrkRoute.01”
     * <p>Return operation number = “1000.0500”
     *
     * <p>Case 1. Over write return operation number, Lot will return to 1000.0700.
     * <p>allReworkWithOpeNo("RwrkRoute.01", "1000.0700", "Reason Code ID", "Claim Memo")
     *
     * <p>Case 2. If return operation number is not specified, this case is same as “AllRework”. Lot will return to 1000.0500.
     * <p>allReworkWithOpeNo("RwrkRoute.01", "", "ReasonCod", "Claim Memo")
     *
     * @param reworkRouteID need to rework's routeID
     * @param returnOpeNo   return operation number
     * @param code          reason code
     * @param memo          claim memo
     * @version 1.0
     * @author ZQI
     * @date 2019/12/5 18:31
     */
    @PcsAPI(PcsAPI.Scope.PRE_1)
    public void allReworkWithOpeNo(String reworkRouteID, String returnOpeNo, String code, String memo) {
        log.info("PCS::allReworkWithOpeNo() enter.");
        Validations.check(CimStringUtils.isEmpty(reworkRouteID), "Rework Route is null. Please check input parameter.");
        Infos.ObjCommon objCommon = ScriptThreadHolder.getObjCommon();

        //-------------------------------//
        // if returnNo is null.
        if (CimStringUtils.isEmpty(returnOpeNo)) {
            this.allRework(reworkRouteID, code, memo);
            return;
        }

        Params.ReworkWholeLotReqParams params = new Params.ReworkWholeLotReqParams();
        params.setClaimMemo("[PCS Action] " + memo);
        params.setCurrentOperationNumber(bizObject.getOperationNumber());

        CimProcessDefinition currentRoot = bizObject.getMainProcessDefinition();
        Validations.check(null == currentRoot, "Current Root is null.");
        params.setCurrentRouteID(ObjectIdentifier.build(currentRoot.getIdentifier(), currentRoot.getPrimaryKey()));
        params.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));
        params.setUser(objCommon.getUser());

        // check the code is belong to "Rework" category.
        CimCode cimCode = baseCoreFactory.getBOByIdentifier(CimCode.class, code);
        Validations.check(null == cimCode, "Cannot find specified code");
        Validations.check(!CimStringUtils.equals(cimCode.getCategory(), BizConstant.SP_OPERATIONCATEGORY_REWORK),
                "Specified code doesn't  belong to Rework category.");
        params.setReasonCodeID(ObjectIdentifier.build(cimCode.getIdentifier(), cimCode.getPrimaryKey()));

        // check the routeID is exist.
        log.trace("find branch info on current step. (Defined on previous step).");
        Params.MultiPathListInqParams multiPathListInqParams = new Params.MultiPathListInqParams();
        multiPathListInqParams.setRouteType(BizConstant.SP_MAINPDTYPE_REWORK);
        multiPathListInqParams.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));

        log.trace("call MultiPathListInqService::sxMultiPathListInq(...)");
        List<Infos.ConnectedRouteList> connectedRouteLists = lotInqService.sxMultiPathListInq(multiPathListInqParams, objCommon);
        boolean isExist = false;
        ObjectIdentifier subRoute = null;
        if (CimArrayUtils.isNotEmpty(connectedRouteLists)) {
            for (Infos.ConnectedRouteList routeList : connectedRouteLists) {
                if (CimStringUtils.equals(routeList.getRouteID().getValue(), reworkRouteID)) {
                    isExist = true;
                    subRoute = routeList.getRouteID();
                    break;
                }
            }
        }

        Validations.check(!isExist,
                String.format("This Rework-Route[%s] not connected to the the previous operation of Lot’s current operation.", reworkRouteID));
        params.setReturnOperationNumber(returnOpeNo);
        params.setSubRouteID(subRoute);

        log.trace("call ReworkWholeLotReqService::sxReworkWholeLotReq(...)");
        lotService.sxReworkWholeLotReq(objCommon, params);
        log.info("PCS::allReworkWithOpeNo() exit.");
    }

    /**
     * EnhancedFutureHold is a command to register future hold with single/multiple trigger flag and Pre/Post flag to lot.
     *
     * @param opeNo             future hold operation number
     * @param code              reason code
     * @param memo              claim memo
     * @param postFlag          post flag
     * @param singleTriggerFlag single trigger flag
     * @version 1.0
     * @author ZQI
     * @date 2019/12/5 18:34
     */
    @PcsAPI(PcsAPI.Scope.PRE_1)
    public void enhancedFutureHold(String opeNo, String code, String memo, Boolean postFlag, Boolean singleTriggerFlag) {
        log.info("PCS::enhancedFutureHold() enter.");
        Infos.ObjCommon objCommon = ScriptThreadHolder.getObjCommon();
        Validations.check(CimStringUtils.isEmpty(opeNo), "Specified Operation Number is null.");
        Validations.check(CimStringUtils.isEmpty(code), "Future Hold must be specified ReasonCode.");

        Params.FutureHoldReqParams params = new Params.FutureHoldReqParams();
        params.setClaimMemo("[PCS Action] " + memo);
        params.setHoldType(BizConstant.SP_HOLDTYPE_FUTUREHOLD);
        params.setLotID(ObjectIdentifier.build(bizObject.getIdentifier(), bizObject.getPrimaryKey()));
        params.setOperationNumber(opeNo);
        params.setPostFlag(postFlag);
        params.setReasonCodeID(ObjectIdentifier.buildWithValue(code));
        params.setRelatedLotID(null);
        CimProcessDefinition root = bizObject.getMainProcessDefinition();
        Validations.check(null == root, "Current lot's Root is null.");
        params.setRouteID(ObjectIdentifier.build(root.getIdentifier(), root.getPrimaryKey()));
        params.setSingleTriggerFlag(singleTriggerFlag);
        params.setUser(objCommon.getUser());

        // future Hold
        log.trace("call FutureHoldReqService::sxFutureHoldReq(...)");
        processControlService.sxFutureHoldReq(objCommon, params);
        log.info("PCS::enhancedFutureHold() exit.");
    }

    /**
     * SetEquipment is a command to specify usable equipment at the operation.
     *
     * <p> Usually an operation is defined such that the process can be performed by multiple equipments.
     * Using this command, usable equipment can be restricted only to those associated ones.
     *
     * <p> Plural equipments can be specified by this command, but they should be one of the equipments
     * that are originally associated to the operation in Process definition.
     *
     * @param equipments equipments
     * @version 1.0
     * @author ZQI
     * @date 2020/1/3 11:10
     */
    public void setEquipment(String... equipments) {
        Validations.check(null == equipments, "The specified equip cannot be null.");
        Validations.check(equipments.length < 1, "The specified equip cannot be null.");
        List<CimMachine> queuedMachines = bizObject.getQueuedMachines();
        boolean isExist = false;
        String tempEqp = null;
        for (String equipment : equipments) {
            for (CimMachine queuedMachine : queuedMachines) {
                if (CimStringUtils.equals(equipment, queuedMachine.getIdentifier())) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                tempEqp = equipment;
                break;
            }
        }
        Validations.check(!isExist, String.format("The specified EQP[%s] is not exist next process.", tempEqp));

        Optional.of(Arrays.asList(equipments)).ifPresent(list -> {
            List<CimMachine> machines = new ArrayList<>();
            for (String equipmentID : list) {
                CimMachine machine = baseCoreFactory.getBOByIdentifier(CimMachine.class, equipmentID);
                machines.add(machine);
            }
            Optional.of(machines).filter(data -> data.size() > 0).ifPresent(data -> bizObject.setQueuedMachines(data));
        });
    }


    @PcsAPI(PcsAPI.Scope.POST)
    public void lotFlip(){
        CimLot cimLot = this.bizObject;
        log.info("Flip lot :" + cimLot.getIdentifier());
        cimLot.getAllWaferInfo().parallelStream()
                .map(ProductDTO.WaferInfo::getWaferID)
                .map(id -> baseCoreFactory.getBO(CimWafer.class, id))
                .filter(wafer -> !CimArrayUtils.isEmpty(wafer.allStackedWafers()))
                .forEach(wafer -> {
                    List<ProductDTO.StackedWafer> sortStackedWafers = wafer.allStackedWafers().stream()
                            .sorted(Comparator.comparing(ProductDTO.StackedWafer::getStackedTimeStamp).reversed())
                            .collect(Collectors.toList());
                    ProductDTO.StackedWafer lastestOne = sortStackedWafers.get(0);
                    lastestOne.setPreviousAliasWaferName(wafer.getAliasWaferName());
                    for (ProductDTO.StackedWafer stackedWafer : sortStackedWafers) {
                        int materialOffset = CimNumberUtils.intValue(stackedWafer.getMaterialOffset());
                        int newMaterialOffset = -materialOffset;
                        stackedWafer.setMaterialOffset(CimNumberUtils.longValue(newMaterialOffset));
                        wafer.removeStackedWafer(stackedWafer);
                        wafer.addStackedWafer(stackedWafer);
                    }
                    wafer.setAliasWaferName(lastestOne.getTopAliasWaferName());
                });
    }
}
