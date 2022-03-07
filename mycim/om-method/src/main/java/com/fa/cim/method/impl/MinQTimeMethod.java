package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.runtime.qtime.CimMinQTimeDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IMinQTimeMethod;
import com.fa.cim.newcore.bo.globalfunc.CimFrameWorkGlobals;
import com.fa.cim.newcore.bo.pd.*;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.qtime.CimMinQTimeRestriction;
import com.fa.cim.newcore.bo.qtime.MinQTimeRestrictionManager;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.fa.cim.common.constant.BizConstant.*;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * description:
 * MinQTimeMethod
 * change history:
 * date         defect#         person      comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 21-6-10       ********        Grant       create file
 *
 * @author: Grant
 * @date: 21-6-10 16:19
 * @copyright: 2021, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class MinQTimeMethod implements IMinQTimeMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;
    @Autowired
    private GenericCoreFactory genericCoreFactory;
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private MinQTimeRestrictionManager restrictionManager;
    @Autowired
    private LotMethod lotMethod;
    @Autowired
    private CimFrameWorkGlobals cimFrameWorkGlobals;

    private final static String FORMATTER = "yyyy-MM-dd HH:mm:ss";

    /**
     * 查询Min Q-Time运行时数据的基础SQL语句
     */
    private static final String MINQT_RESTRICT_SELECT = "SELECT ID, TIMER_TYPE, LOT_ID, LOT_RKEY, WAFER_ID, " +
            "WAFER_RKEY, INITIAL_QTIME_INFO, PRP_LEVEL, TRIGGER_PRP_ID, TRIGGER_PRP_RKEY, TRIGGER_OPE_NO, " +
            "TRIGGER_TIME, TARGET_PRP_ID, TARGET_PRP_RKEY, TARGET_OPE_NO, TARGET_TIME, ENTITY_MGR FROM OMMINQT ";

    @Override
    public void checkAndSetRestrictions(Infos.ObjCommon objCommon, List<Infos.StartCassette> cassettes) {
        if (CimArrayUtils.isEmpty(cassettes)) {
            return;
        }

        // Loop for StartCassette
        cassettes.stream().filter(cassette -> CimArrayUtils.isNotEmpty(cassette.getLotInCassetteList()))
                .forEach(cassette -> cassette.getLotInCassetteList().stream().filter(lot ->
                        CimBooleanUtils.isTrue(lot.getMoveInFlag())).forEach(lotIn -> {
                            // Loop for LotInCassette
                            checkAndSetRestrictions(objCommon, lotIn.getLotID());
                        }));
    }

    @Override
    public void checkAndSetRestrictions(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        // 通过ID获取Lot, 并且判断不能为空
        CimLot lot = getLot(lotID);
        // 通过Lot获取process operation, 并且判断不能为空
        CimProcessOperation processOpe = lot.getProcessOperation();
        Validations.check(processOpe == null, retCodeConfig.getNotFoundProcessOperation());

        // 获取当前process definition的ID, 并且判断不能为空
        CimProcessDefinition currentRoute = processOpe.getMainProcessDefinition();
        Validations.check(currentRoute == null, retCodeConfig.getNotFoundMainRoute());
        ObjectIdentifier curProcessID = ObjectIdentifier.build(
             currentRoute.getIdentifier(), currentRoute.getPrimaryKey());

        // get the process operation number
        String currentOpeNo = processOpe.getOperationNumber();

        // get product specification of the Lot, and it can't be empty
        CimProductSpecification productSpec = lot.getProductSpecification();
        Validations.check(productSpec == null, retCodeConfig.getNotFoundProductSpec());

        // get process flow type;
        String processFlowType = currentRoute.getProcessFlowType();
        if (CimStringUtils.equals(BizConstant.SP_FLOWTYPE_SUB, processFlowType)) {
            // exec Min Q-Time Target Operation Replace
            checkTargetOpeReplace(objCommon, lot, productSpec);
        }

        // find all min q-time restrictions
        List<ProcessDTO.TimeRestrictionSpecification> restrictions = processOpe.findMinQTimeRestrictions(productSpec);
        if (CimArrayUtils.isNotEmpty(restrictions)) {
            final Timestamp currentTime = objCommon.getTimeStamp().getReportTimeStamp();
            restrictions.stream().filter(restrict ->
                    restrict.getDefaultTimeRS() != null &&
                            CimStringUtils.equals(SP_QTIMETYPE_BYLOT, restrict.getDefaultTimeRS().getQTimeType()) &&
                            restrict.getDefaultTimeRS().getExpiredTimeDuration() != null &&
                            restrict.getDefaultTimeRS().getExpiredTimeDuration() > 0)
                    .forEach(restrict -> {
                        CimMinQTimeRestriction restriction = findRestriction(lot.getPrimaryKey(),
                                curProcessID.getValue(), currentOpeNo, curProcessID.getValue(),
                                restrict.getDefaultTimeRS().getTargetOperationNumber());
                        if (restriction != null) {
                            restriction.setTriggerTime(currentTime);
                            restriction.setTargetTime(CimDateUtils.convertToOrInitialTime(
                                    currentTime.toLocalDateTime().plusMinutes(restrict.getDefaultTimeRS()
                                            .getExpiredTimeDuration().longValue())
                                            .format(DateTimeFormatter.ofPattern(FORMATTER))));
                        } else {
                            // Create a CimMinQTimeRestriction
                            restriction = restrictionManager.createRestriction();
                            Validations.check(restriction == null, new OmCode(
                                    retCodeConfig.getNotFoundSystemObj(), "CimMinQTimeRestriction"));

                            ProcessDTO.QTimeRestrictionInfo info = new ProcessDTO.QTimeRestrictionInfo();
                            info.setQTimeType(restrict.getDefaultTimeRS().getQTimeType());
                            info.setLotID(lot.getLotID());
                            info.setWaferID(ObjectIdentifier.emptyIdentifier());
                            info.setProcessDefinitionLevel(restrict.getDefaultTimeRS().getProcessDefinitionLevel());
                            info.setTriggerMainProcessDefinition(curProcessID);
                            info.setTriggerOperationNumber(currentOpeNo);
                            info.setTriggerTimeStamp(currentTime.toString());
                            info.setTargetMainProcessDefinition(curProcessID);
                            info.setTargetOperationNumber(restrict.getDefaultTimeRS().getTargetOperationNumber());
                            info.setTargetTimeStamp(currentTime.toLocalDateTime()
                                    .plusMinutes(restrict.getDefaultTimeRS()
                                            .getExpiredTimeDuration().longValue())
                                    .format(DateTimeFormatter.ofPattern(FORMATTER)));
                            info.setOriginalQTime(CimArrayUtils.mergeStringIntoTokens(Lists.newArrayList(
                                    curProcessID.getValue(), currentOpeNo, curProcessID.getValue(),
                                    restrict.getDefaultTimeRS().getTargetOperationNumber()),
                                    BizConstant.SP_KEY_SEPARATOR_DOT));
                            // add a min q-time runtime data
                            restriction.setRestrictionInfo(info);
                        }
                    });
        }

        if (CimStringUtils.equals(BizConstant.SP_FLOWTYPE_SUB, processFlowType)) {
            // exec Min Q-Time Trigger Operation Replace
            checkTriggerOpeReplace(objCommon, lot, productSpec);
        }
    }

    @Override
    public void checkTargetOpeReplace(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        // 通过ID获取Lot, 并且判断不能为空
        CimLot lot = getLot(lotID);

        // get product specification of the Lot, and it can't be empty
        CimProductSpecification productSpec = lot.getProductSpecification();
        Validations.check(productSpec == null, retCodeConfig.getNotFoundProductSpec());

        checkTargetOpeReplace(objCommon, lot, productSpec);
    }

    private void checkTargetOpeReplace(Infos.ObjCommon objCommon, CimLot lot, CimProductSpecification productSpec) {
        List<CimMinQTimeRestriction> restrictions = findRestrictions(lot.getPrimaryKey());
        if (CimArrayUtils.isEmpty(restrictions)) {
            return;
        }

        CimProcessFlowContext processFlow = lot.getProcessFlowContext();
        Validations.check(processFlow == null, retCodeConfig.getNotFoundPfx(),
                nullToEmpty(lot.getProcessFlowContextObj()));
        if (processFlow == null) {
            return;
        }

        // 标识当前Process Operation是否发生改变(true.没变 false.已变)
        boolean currentPOFlag = true;
        if (CimStringUtils.equalsIn(objCommon.getTransactionID(),
                "OEQPW006", "OEQPW008", "OEQPW014", "OEQPW023", "OEQPW012", "OEQPW024")) {
            boolean strLotCheckConditionForPOOut = lotMethod.lotCheckConditionForPO(objCommon, lot.getLotID());
            if (!strLotCheckConditionForPOOut) {
                currentPOFlag = false;
            }
        }

        CimProcessOperation processOpe;
        if (currentPOFlag) {
            processOpe = lot.getProcessOperation();
        } else {
            processOpe = lot.getPreviousProcessOperation();
        }
        Validations.check(processOpe == null, retCodeConfig.getNotFoundOperation());

        CimProcessDefinition currentRoute = processOpe.getMainProcessDefinition();
        Validations.check(currentRoute == null, retCodeConfig.getNotFoundMainRoute());
        String currentRouteID = currentRoute.getIdentifier();

        List<ProcessDTO.BranchInfo> branchInfos = processFlow.allBranchInfos();
        List<ProcessDTO.ReturnInfo> returnInfos = processFlow.allReturnInfos();

        boolean firstOperationFlag = false;
        if (currentPOFlag) {
            firstOperationFlag = processFlow.isFirstOperationForProcessFlowOnCurrentRoute();
        } else {
            String previousBranchInfo = ThreadContextHolder.getThreadSpecificDataString(
                    SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);
            String previousReturnInfo = ThreadContextHolder.getThreadSpecificDataString(
                    SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);

            if (CimStringUtils.isNotEmpty(previousBranchInfo)) {
                List<String> previousTokens = BaseStaticMethod.splitStringIntoTokens(previousBranchInfo,
                        "\\" + SP_KEY_SEPARATOR_DOT);
                if (CimArrayUtils.getSize(previousTokens) == 4) {
                    String[] strings = new String[2];
                    strings[0] = previousTokens.get(0);
                    strings[1] = previousTokens.get(1);
                    String previousBranchRouteIdentifier = CimArrayUtils.mergeStringIntoTokens(
                            CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);
                    strings[0] = previousTokens.get(2);
                    strings[1] = previousTokens.get(3);
                    String previousBranchOpeNo = CimArrayUtils.mergeStringIntoTokens(
                            CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);

                    CimProcessDefinition aPosPD = genericCoreFactory.getBO(CimProcessDefinition.class,
                            ObjectIdentifier.buildWithValue(previousBranchRouteIdentifier));

                    ProcessDTO.BranchInfo branchInfo = new ProcessDTO.BranchInfo();
                    branchInfo.getRouteID().setValue(previousBranchRouteIdentifier);
                    branchInfo.getRouteID().setReferenceKey(aPosPD.getPrimaryKey());
                    branchInfo.setOperationNumber(previousBranchOpeNo);
                    branchInfo.setProcessOperation("");
                    branchInfo.setReworkOutKey("");

                    String previousReworkOutKey = ThreadContextHolder.getThreadSpecificDataString(
                            SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);
                    if (CimStringUtils.isNotEmpty(previousReworkOutKey)) {
                        branchInfo.setReworkOutKey(previousReworkOutKey);
                    }

                    branchInfos.add(branchInfo);
                }
            }

            if (CimStringUtils.isNotEmpty(previousReturnInfo)) {
                List<String> previousTokens = BaseStaticMethod.splitStringIntoTokens(previousReturnInfo,
                        "\\" + SP_KEY_SEPARATOR_DOT);
                if (CimArrayUtils.getSize(previousTokens) == 4) {
                    String[] strings = new String[2];
                    strings[0] = previousTokens.get(0);
                    strings[1] = previousTokens.get(1);
                    String previousReturnRouteIdentifier = CimArrayUtils.mergeStringIntoTokens(
                            CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);
                    strings[0] = previousTokens.get(2);
                    strings[1] = previousTokens.get(3);
                    String previousReturnOpeNo = CimArrayUtils.mergeStringIntoTokens(
                            CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);

                    CimProcessDefinition aPosPD = genericCoreFactory.getBO(CimProcessDefinition.class,
                            ObjectIdentifier.buildWithValue(previousReturnRouteIdentifier));

                    ProcessDTO.ReturnInfo returnInfo = new ProcessDTO.ReturnInfo();
                    returnInfo.getRouteID().setValue(previousReturnRouteIdentifier);
                    returnInfo.getRouteID().setReferenceKey(aPosPD.getPrimaryKey());
                    returnInfo.setOperationNumber(previousReturnOpeNo);
                    returnInfo.setProcessFlow("");
                    returnInfo.setMainProcessFlow("");
                    returnInfo.setModuleProcessFlow("");

                    returnInfos.add(returnInfo);
                }
            }

            CimProcessFlow mainPF = processOpe.getMainProcessFlow();
            Validations.check(mainPF == null, retCodeConfig.getNotFoundProcessFlow());

            String moduleNumber = processOpe.getModuleNumber();

            CimProcessFlow modulePF = processOpe.getModuleProcessFlow();
            Validations.check(modulePF == null, retCodeConfig.getNotFoundProcessFlow());

            CimProcessOperationSpecification modulePOS = processOpe.getModuleProcessOperationSpecification();
            Validations.check(modulePOS == null, retCodeConfig.getNotFoundPos());

            AtomicReference<CimProcessFlow> outMainProcessFlow = new AtomicReference<>(),
                    outModuleProcessFlow = new AtomicReference<>();
            AtomicReference<String> chrOutModuleNumber = new AtomicReference<>();
            CimProcessOperationSpecification prevPOS = mainPF.getPreviousProcessOperationSpecificationFor(moduleNumber,
                    modulePF, modulePOS, outMainProcessFlow, chrOutModuleNumber, outModuleProcessFlow);
            if (prevPOS == null) {
                firstOperationFlag = true;
            }
        }

        boolean finalFirstOperationFlag = firstOperationFlag;
        final Timestamp currentTime = objCommon.getTimeStamp().getReportTimeStamp();
        restrictions.forEach(restrict -> {
            ProcessDTO.QTimeRestrictionInfo info = restrict.getRestrictionInfo();
            Validations.check(info == null || CimStringUtils.isEmpty(info.getTriggerOperationNumber()),
                    retCodeConfig.getNotFoundQtime());

            ProcessDTO.ReplaceTimeRestrictionSpecification replaceTime = processOpe
                    .findMinQTimeTimeRestrictionForReplace(info, productSpec, branchInfos, returnInfos, currentRouteID);
            if (replaceTime == null) {
                return;
            }

            if (CimStringUtils.equals(replaceTime.getControl(), SP_QRESTTIME_SPECIFICCONTROL_RETRIGGER)) {
                if (finalFirstOperationFlag) {
                    Timestamp triggerTime = CimDateUtils.convertToOrInitialTime(info.getTriggerTimeStamp());

                    info.setTriggerTimeStamp(currentTime.toString());
                    info.setTargetTimeStamp(CimDateUtils.getTimestampAsString(new Timestamp(currentTime.getTime() +
                            Math.abs(triggerTime.getTime() - CimDateUtils.convertToOrInitialTime(info.getTargetTimeStamp())
                                    .getTime()))));

                    String targetTimeStamp = getTargetTimeByActions(replaceTime.getActions(), currentTime);
                    if (CimStringUtils.isNotEmpty(targetTimeStamp)) {
                        info.setTargetTimeStamp(targetTimeStamp);
                    }

                    restrict.setRestrictionInfo(info);
                } else {
                    if (log.isInfoEnabled()) {
                        log.info("Not First Operation, RestrictId({}), CurrentRouteID({})",
                                restrict.getPrimaryKey(), currentRouteID);
                    }
                }
            } else if (CimStringUtils.equals(replaceTime.getControl(), SP_QRESTTIME_SPECIFICCONTROL_DELETE)) {
                restrictionManager.removeRestriction(restrict);
            } else if (CimStringUtils.isNotEmpty(replaceTime.getTargetOperationNumber())) {
                CimProcessDefinition processDef = genericCoreFactory.getBO(CimProcessDefinition.class,
                        replaceTime.getTargetRouteID());
                String targetTimeStamp = getTargetTimeByActions(replaceTime.getActions(), currentTime);
                restrict.replaceTarget(processDef, replaceTime.getTargetOperationNumber(),
                        CimStringUtils.isEmpty(targetTimeStamp) ? null : CimDateUtils.convertTo(targetTimeStamp));

                // TODO 实现事件日志记录
            }
        });
    }

    private String getTargetTimeByActions(List<ProcessDTO.TimeRestrictionAction> actions, Timestamp currentTime) {
        if (CimArrayUtils.isEmpty(actions)) {
            return null;
        }
        String targetTimeStamp = null;
        for (ProcessDTO.TimeRestrictionAction action : actions) {
            if (SP_DISPATCH_PRECEDE_NOT_FOUND * 1.0 == action.getExpiredTimeDuration()) {
                continue;
            }
            String targetTimeForAction = currentTime.toLocalDateTime()
                    .plusMinutes(action.getExpiredTimeDuration().longValue())
                    .format(DateTimeFormatter.ofPattern(FORMATTER));
            if (CimStringUtils.isEmpty(targetTimeStamp)) {
                targetTimeStamp = targetTimeForAction;
            } else if (CimDateUtils.compare(targetTimeStamp, targetTimeForAction) > 0) {
                targetTimeStamp = targetTimeForAction;
            }
        }
        return targetTimeStamp;
    }

    @Override
    public void checkTriggerOpeReplace(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        // 通过ID获取Lot, 并且判断不能为空
        CimLot lot = getLot(lotID);

        // get product specification of the Lot, and it can't be empty
        CimProductSpecification productSpec = lot.getProductSpecification();
        Validations.check(productSpec == null, retCodeConfig.getNotFoundProductSpec());

        checkTriggerOpeReplace(objCommon, lot, productSpec);
    }

    private void checkTriggerOpeReplace(Infos.ObjCommon objCommon, CimLot lot, CimProductSpecification productSpec) {
        CimProcessFlowContext processFlow = lot.getProcessFlowContext();
        Validations.check(processFlow == null, retCodeConfig.getNotFoundPfx(),
                nullToEmpty(lot.getProcessFlowContextObj()));
        if (processFlow == null) {
            return;
        }

        // 标识当前Process Operation是否发生改变(true.没变 false.已变)
        boolean currentPOFlag = true;
        if (CimStringUtils.equalsIn(objCommon.getTransactionID(),
                "OEQPW006", "OEQPW008", "OEQPW014", "OEQPW023", "OEQPW012", "OEQPW024")) {
            boolean strLotCheckConditionForPOOut = lotMethod.lotCheckConditionForPO(objCommon, lot.getLotID());
            if (!strLotCheckConditionForPOOut) {
                currentPOFlag = false;
            }
        }

        CimProcessOperation processOpe;
        if (currentPOFlag) {
            processOpe = lot.getProcessOperation();
        } else {
            processOpe = lot.getPreviousProcessOperation();
        }
        Validations.check(processOpe == null, retCodeConfig.getNotFoundOperation());

        CimProcessDefinition currentRoute = processOpe.getMainProcessDefinition();
        Validations.check(currentRoute == null, retCodeConfig.getNotFoundMainRoute());
        String currentRouteID = currentRoute.getIdentifier();

        String currentOpeNo = processOpe.getOperationNumber();

        List<ProcessDTO.BranchInfo> branchInfos = processFlow.allBranchInfos();
        List<ProcessDTO.ReturnInfo> returnInfos = processFlow.allReturnInfos();

        if (!currentPOFlag) {
            String previousBranchInfo = ThreadContextHolder.getThreadSpecificDataString(
                    SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);
            String previousReturnInfo = ThreadContextHolder.getThreadSpecificDataString(
                    SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);

            if (CimStringUtils.isNotEmpty(previousBranchInfo)) {
                List<String> previousTokens = BaseStaticMethod.splitStringIntoTokens(previousBranchInfo,
                        "\\" + SP_KEY_SEPARATOR_DOT);
                if (CimArrayUtils.getSize(previousTokens) == 4) {
                    String[] strings = new String[2];
                    strings[0] = previousTokens.get(0);
                    strings[1] = previousTokens.get(1);
                    String previousBranchRouteIdentifier = CimArrayUtils.mergeStringIntoTokens(
                            CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);
                    strings[0] = previousTokens.get(2);
                    strings[1] = previousTokens.get(3);
                    String previousBranchOpeNo = CimArrayUtils.mergeStringIntoTokens(
                            CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);

                    CimProcessDefinition aPosPD = genericCoreFactory.getBO(CimProcessDefinition.class,
                            ObjectIdentifier.buildWithValue(previousBranchRouteIdentifier));

                    ProcessDTO.BranchInfo branchInfo = new ProcessDTO.BranchInfo();
                    branchInfo.getRouteID().setValue(previousBranchRouteIdentifier);
                    branchInfo.getRouteID().setReferenceKey(aPosPD.getPrimaryKey());
                    branchInfo.setOperationNumber(previousBranchOpeNo);
                    branchInfo.setProcessOperation("");
                    branchInfo.setReworkOutKey("");

                    String previousReworkOutKey = ThreadContextHolder.getThreadSpecificDataString(
                            SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);
                    if (CimStringUtils.isNotEmpty(previousReworkOutKey)) {
                        branchInfo.setReworkOutKey(previousReworkOutKey);
                    }

                    branchInfos.add(branchInfo);
                }
            }

            if (CimStringUtils.isNotEmpty(previousReturnInfo)) {
                List<String> previousTokens = BaseStaticMethod.splitStringIntoTokens(previousReturnInfo,
                        "\\" + SP_KEY_SEPARATOR_DOT);
                if (CimArrayUtils.getSize(previousTokens) == 4) {
                    String[] strings = new String[2];
                    strings[0] = previousTokens.get(0);
                    strings[1] = previousTokens.get(1);
                    String previousReturnRouteIdentifier = CimArrayUtils.mergeStringIntoTokens(
                            CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);
                    strings[0] = previousTokens.get(2);
                    strings[1] = previousTokens.get(3);
                    String previousReturnOpeNo = CimArrayUtils.mergeStringIntoTokens(
                            CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);

                    CimProcessDefinition aPosPD = genericCoreFactory.getBO(CimProcessDefinition.class,
                            ObjectIdentifier.buildWithValue(previousReturnRouteIdentifier));

                    ProcessDTO.ReturnInfo returnInfo = new ProcessDTO.ReturnInfo();
                    returnInfo.getRouteID().setValue(previousReturnRouteIdentifier);
                    returnInfo.getRouteID().setReferenceKey(aPosPD.getPrimaryKey());
                    returnInfo.setOperationNumber(previousReturnOpeNo);
                    returnInfo.setProcessFlow("");
                    returnInfo.setMainProcessFlow("");
                    returnInfo.setModuleProcessFlow("");

                    returnInfos.add(returnInfo);
                }
            }
        }

        final Timestamp currentTime = objCommon.getTimeStamp().getReportTimeStamp();
        // Get all Q-Time definitions to be replaced for Trigger Operation
        List<ProcessDTO.ReplaceTimeRestrictionSpecification> replaceTimes = processOpe
                .findMinQTimeRestrictionsForReplaceTrigger(productSpec, branchInfos, returnInfos, currentRouteID,
                        currentOpeNo);
        if (CimArrayUtils.isNotEmpty(replaceTimes)) {
            replaceTimes.stream().filter(replaceTime ->
                    CimStringUtils.equals(replaceTime.getQTimeType(), BizConstant.SP_QTIMETYPE_BYLOT))
                    .forEach(time -> {
                        // Check if there is duplicate Q-Time timer
                        CimMinQTimeRestriction restriction = findRestrictByOriginalQTime(lot.getPrimaryKey(),
                                time.getOriginalQTime());
                        if (restriction != null) {
                            return;
                        }

                        // Create QTime timer object
                        restriction = restrictionManager.createRestriction();
                        Validations.check(restriction == null, new OmCode(
                                retCodeConfig.getNotFoundSystemObj(), "CimMinQTimeRestriction"));

                        ProcessDTO.QTimeRestrictionInfo info = new ProcessDTO.QTimeRestrictionInfo();
                        info.setQTimeType(time.getQTimeType());
                        info.setLotID(lot.getLotID());
                        info.setWaferID(ObjectIdentifier.emptyIdentifier());
                        info.setProcessDefinitionLevel(time.getProcessDefinitionLevel());
                        info.setTriggerMainProcessDefinition(ObjectIdentifier.build(
                                currentRoute.getIdentifier(), currentRoute.getPrimaryKey()));
                        info.setTriggerOperationNumber(currentOpeNo);
                        info.setTriggerTimeStamp(currentTime.toString());
                        info.setTargetMainProcessDefinition(time.getTargetRouteID());
                        info.setTargetOperationNumber(time.getTargetOperationNumber());
                        info.setOriginalQTime(time.getOriginalQTime());

                        // "expiredTimeDuration=0" means "dispatch precede" is not specified on SMS.
                        // Then, set default time stamp(1901-01-01.....)
                        String targetTimeStamp = getTargetTimeByActions(time.getActions(), currentTime);
                        info.setTargetTimeStamp(targetTimeStamp);

                        restriction.setRestrictionInfo(info);

                        // TODO 实现事件日志记录
                    });
        }

        // Find MinQTime Restrictions for target lot
        List<CimMinQTimeRestriction> restrictions = findRestrictions(lot.getPrimaryKey());
        if (CimArrayUtils.isEmpty(restrictions)) {
            return;
        }
        restrictions.forEach(restrict -> {
            ProcessDTO.QTimeRestrictionInfo info = restrict.getRestrictionInfo();
            if (info == null || CimStringUtils.isEmpty(info.getTriggerOperationNumber())) {
                throw new ServiceException(retCodeConfig.getNotFoundQtime());
            } else if (!CimStringUtils.equals(info.getControl(), BizConstant.SP_QRESTTIME_SPECIFICCONTROL_RETRIGGER)) {
                return;
            }

            info.setTriggerMainProcessDefinition(ObjectIdentifier.build(
                    currentRoute.getIdentifier(), currentRoute.getPrimaryKey()));
            info.setTriggerOperationNumber(currentOpeNo);
            restrict.setRestrictionInfo(info);

            // TODO 实现事件日志记录
        });
    }

    @Override
    public void checkIsRejectByRestriction(Infos.ObjCommon objCommon, List<Infos.StartCassette> cassettes) {
        if (CimArrayUtils.isEmpty(cassettes)) {
            return;
        }

        // Loop for StartCassette
        cassettes.stream().filter(cassette -> CimArrayUtils.isNotEmpty(cassette.getLotInCassetteList()) &&
                !CimStringUtils.equals(SP_LOADPURPOSETYPE_EMPTYCASSETTE, cassette.getLoadPurposeType()))
                .forEach(cassette -> cassette.getLotInCassetteList().stream().filter(lot ->
                        CimBooleanUtils.isTrue(lot.getMoveInFlag())).forEach(lotIn -> {
                            // Loop for LotInCassette
                            checkIsRejectByRestriction(objCommon, lotIn.getLotID());
                        }));
    }

    @Override
    public void checkIsRejectByRestriction(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        // 通过ID获取Lot, 并且判断不能为空
        CimLot lot = getLot(lotID);
        // 通过Lot获取process operation, 并且判断不能为空
        CimProcessOperation processOpe = lot.getProcessOperation();
        if (processOpe == null) {
            return;
        }
        // 获取当前process definition的ID, 并且判断不能为空
        ObjectIdentifier curProcessID = processOpe.getMainProcessID();
        if (curProcessID == null) {
            return;
        }

        List<CimMinQTimeRestriction> restricts = findRestrictions(lot.getPrimaryKey(),
                curProcessID.getValue(), processOpe.getOperationNumber());
        if (CimArrayUtils.isEmpty(restricts)) {
            return;
        }

        final Timestamp currentTime = objCommon.getTimeStamp().getReportTimeStamp();

        // 只要有且存在任意一条还未结束的最小时间限制计数, 就拒绝Lot Reserve/Move In
        restricts.forEach(restrict -> {
            if (restrict.getTargetTime().after(currentTime)) {
                throw new ServiceException(retCodeConfigEx.getMinQTimeLimitedToReject());
            }

            restrictionManager.removeRestriction(restrict);
        });
    }

    @Override
    public void lotSplit(ObjectIdentifier parentLotID, ObjectIdentifier childLotID) {
        checkParams(parentLotID, childLotID);

        // 通过ID获取子批Lot, 并且判断不能为空
        CimLot lot = getLot(childLotID);
        // 获取当前process definition的ID, 并且判断不能为空
        String curProcessID = getCurProcessID(lot);

        List<CimMinQTimeRestriction> restricts = findRestrictions(ObjectIdentifier.fetchReferenceKey(parentLotID),
                curProcessID);
        if (CimArrayUtils.isEmpty(restricts)) {
            return;
        }

        // 子批Lot继承母批Lot的所有Min Q-Time的运行时时间限制数据
        restricts.forEach(restrict -> {
            CimMinQTimeRestriction restriction = restrictionManager.createRestriction();
            if (restriction == null) {
                return;
            }

            ProcessDTO.QTimeRestrictionInfo info = new ProcessDTO.QTimeRestrictionInfo();
            info.setQTimeType(restrict.getTimerType());
            info.setLotID(childLotID);
            info.setWaferID(ObjectIdentifier.emptyIdentifier());
            info.setProcessDefinitionLevel(restrict.getProcessDefinitionLevel());
            info.setTriggerMainProcessDefinition(restrict.getTriggerMainProcessDefinitionID());
            info.setTriggerOperationNumber(restrict.getTriggerOpeNo());
            info.setTriggerTimeStamp(restrict.getTriggerTime().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern(FORMATTER)));
            info.setTargetMainProcessDefinition(restrict.getTargetMainProcessDefinitionID());
            info.setTargetOperationNumber(restrict.getTargetOpeNo());
            info.setTargetTimeStamp(restrict.getTargetTime().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern(FORMATTER)));
            info.setOriginalQTime(restrict.getOriginalQTime());
            restriction.setRestrictionInfo(info);
        });
    }

    @Override
    public void lotMerge(ObjectIdentifier parentLotID, ObjectIdentifier childLotID) {
        checkParams(parentLotID, childLotID);

        // 通过ID获取母批Lot, 并且判断不能为空
        CimLot parentLot = getLot(parentLotID);
        // 获取当前母Lot的process definition的ID, 并且判断不能为空
        String curParentProcessID = getCurProcessID(parentLot);
        // 通过ID获取子批Lot, 并且判断不能为空
        CimLot childLot = getLot(childLotID);
        // 获取当前子Lot的process definition的ID, 并且判断不能为空
        String curChildProcessID = getCurProcessID(childLot);

        // 获取母批Lot当前工步的所有Min Q-Time运行时数据
        List<CimMinQTimeRestriction> parentRestricts = findRestrictions(
                ObjectIdentifier.fetchReferenceKey(parentLot.getLotID()), curParentProcessID);
        // 获取子批Lot当前工步的所有Min Q-Time运行时数据
        List<CimMinQTimeRestriction> childRestricts = findRestrictions(
                ObjectIdentifier.fetchReferenceKey(childLot.getLotID()), curChildProcessID);

        List<CimMinQTimeRestriction> needDeletes = Lists.newArrayList();

        if (CimArrayUtils.isNotEmpty(parentRestricts) && CimArrayUtils.isNotEmpty(childRestricts)) {
            // 1.子/母Lot都有Min Q-Time时, 系统根据子母批Min Q-Time剩余时间的长短, 选择继承剩余时间最长的Min Q-Time;
            boolean existInParent;
            for (CimMinQTimeRestriction childRestrict : childRestricts) {
                existInParent = false;
                for (CimMinQTimeRestriction parentRestrict : parentRestricts) {
                    // 子/母Lot的Min Q-Time区域相同(trigger operation 和 target operation相同)
                    if (ObjectIdentifier.equalsWithValue(childRestrict.getTriggerMainProcessDefinitionID(),
                            parentRestrict.getTriggerMainProcessDefinitionID()) &&
                            CimStringUtils.equals(childRestrict.getTriggerOpeNo(), parentRestrict.getTriggerOpeNo()) &&
                            ObjectIdentifier.equalsWithValue(childRestrict.getTargetMainProcessDefinitionID(),
                                    parentRestrict.getTargetMainProcessDefinitionID()) &&
                            CimStringUtils.equals(childRestrict.getTargetOpeNo(), parentRestrict.getTargetOpeNo())) {
                        if (childRestrict.getTargetTime().after(parentRestrict.getTargetTime())) {
                            parentRestrict.setTargetTime(childRestrict.getTargetTime());
                        }
                        existInParent = true;
                    }
                }
                if (existInParent) {
                    needDeletes.add(childRestrict);
                } else {
                    childRestrict.setLotID(parentLot.getLotID());
                }
            }
        } else if (CimArrayUtils.isEmpty(parentRestricts) && CimArrayUtils.isNotEmpty(childRestricts)) {
            // 2.当子Lot有Min Q-Time, 母Lot没有, Merge后, 母Lot继承子Lot的Min Q-Time
            childRestricts.forEach(restrict -> restrict.setLotID(parentLot.getLotID()));
        }

        // 3.当母Lot有Min Q-Time, 子Lot没有, Merge后, 母Lot的Min Q-Time保持不变(数据已满足, 无逻辑处理)
        // 4.子/母Lot都没有Min Q-Time时, 正常Merge(无Min Q-Time数据需要处理处理)

        // Merge成功后, 删除多余的Min Q-Time运行时数据
        if (CimArrayUtils.isNotEmpty(needDeletes)) {
            needDeletes.forEach(restrict -> restrictionManager.removeRestriction(restrict));
        }
    }

    @Override
    public void clearInvalid() {
        Timestamp currentTime = CimDateUtils.convertToOrInitialTime(new Timestamp(System.currentTimeMillis())
                .toLocalDateTime().minusMinutes(5).format(DateTimeFormatter.ofPattern(FORMATTER)));
        List<CimMinQTimeRestriction> restrictions;
        int size = 1000;
        do {
            restrictions = findRestrictions(currentTime, size);
            if (CimArrayUtils.isEmpty(restrictions)) {
                break;
            }

            restrictions.forEach(restrict -> restrictionManager.removeRestriction(restrict));

            if (restrictions.size() < size) {
                break;
            }
        } while (true);
    }

    @Override
    public boolean checkIsInRestriction(ObjectIdentifier lotID, String mainProcessFlow, String processFlowContextObj) {
        // 在主Process上先检查指定Lot是否有在时间限制区域范围内
        List<Infos.LotQtimeInfo> infos = getRestrictInProcessArea(lotID, mainProcessFlow);
        if (CimArrayUtils.isNotEmpty(infos)) {
            return true;
        }

        // 如果主Process上没有, 再在Branch Route上检查指定Lot是否在时间限制区域范围内
        return cimJpaRepository.queryOneColumn(
                "SELECT MROUTE_PRF_RKEY FROM OMPRFCX_RTNSEQ WHERE REFKEY = ?",
                processFlowContextObj).stream().anyMatch(obj ->
                CimArrayUtils.isNotEmpty(getRestrictInProcessArea(lotID, CimObjectUtils.toString(obj))));
    }

    @Override
    public List<Infos.LotQtimeInfo> getRestrictInProcessArea(ObjectIdentifier lotID, String mainProcessFlow) {
        List<Infos.LotQtimeInfo> result = Lists.newArrayList();

        String pfxObj = CimObjectUtils.toString(cimJpaRepository.queryOneColumnAndUnique(
                "SELECT PRFCX_RKEY FROM OMLOT WHERE LOT_ID = ?", lotID.getValue()));
        if (CimStringUtils.isEmpty(pfxObj)) {
            return result;
        }

        CimProcessFlowContext processFlowContext = genericCoreFactory.getBO(CimProcessFlowContext.class, pfxObj);
        if (processFlowContext == null) {
            return result;
        }

        String pfMainPDID = CimObjectUtils.toString(cimJpaRepository.queryOneColumnAndUnique(
                "SELECT PRP_ID FROM OMPRF WHERE ID = ?", mainProcessFlow));
        if (CimStringUtils.isEmpty(pfMainPDID)) {
            return result;
        }

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        // 获取指定Lot在时间限制区域内的所有时间限制
        cimJpaRepository.queryOneColumn(
                "SELECT ID FROM OMMINQT WHERE LOT_ID = ? AND TRIGGER_PRP_ID = ? ORDER BY TARGET_TIME",
                lotID.getValue(), pfMainPDID).forEach(obj -> {
            CimMinQTimeRestriction restrict = genericCoreFactory.getBO(CimMinQTimeRestriction.class,
                    CimObjectUtils.toString(obj));
            if (restrict == null || restrict.getTargetTime().before(currentTime) ||
                    !processFlowContext.isInProcessArea(restrict.getTriggerMainProcessDefinition(),
                            restrict.getTriggerOpeNo(), restrict.getTargetMainProcessDefinition(),
                            restrict.getTargetOpeNo(), false)) {
                return;
            }

            ProcessDTO.QTimeRestrictionInfo info = restrict.getRestrictionInfo();
            Infos.LotQtimeInfo qTimeInfo = new Infos.LotQtimeInfo();
            qTimeInfo.setQrestrictionTriggerRouteID(info.getTriggerMainProcessDefinition());
            qTimeInfo.setQrestrictionTriggerOperationNumber(nullToEmpty(info.getTriggerOperationNumber()));
            qTimeInfo.setQrestrictionTriggerTimeStamp(info.getTriggerTimeStamp());
            qTimeInfo.setQrestrictionTargetRouteID(info.getTargetMainProcessDefinition());
            qTimeInfo.setQrestrictionTargetOperationNumber(nullToEmpty(info.getTargetOperationNumber()));
            qTimeInfo.setQrestrictionTargetTimeStamp(info.getTargetTimeStamp());
            if (CimStringUtils.isNotEmpty(info.getTargetTimeStamp()) &&
                    !BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING.equals(info.getTargetTimeStamp())) {
                qTimeInfo.setQrestrictionRemainTime(CimDateUtils.convertToHHmmss(
                        currentTime, Timestamp.valueOf(info.getTargetTimeStamp())));
            } else {
                qTimeInfo.setQrestrictionRemainTime("-");
            }
            result.add(qTimeInfo);
        });

        return result;
    }

    @Override
    public List<Outputs.QrestLotInfo> getRestrictionsByLot(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        List<Outputs.QrestLotInfo> result = Lists.newArrayList();

        final Timestamp currentTime = objCommon.getTimeStamp().getReportTimeStamp();

        // 获取运行时数据中有效的Min Q-Time时间限制数据
        StringBuilder builder = new StringBuilder(MINQT_RESTRICT_SELECT);
        if (ObjectIdentifier.isNotEmptyWithValue(lotID)) {
            builder.append(" WHERE LOT_ID = '").append(lotID.getValue()).append("'");
        }
        builder.append(" ORDER BY TARGET_TIME");
        cimJpaRepository.query(builder.toString(), CimMinQTimeDO.class).stream().filter(restrict ->
                restrict.getTargetTime().after(currentTime)).forEach(restrict ->
                getQRestLotInfo(result, restrict, currentTime));

        return result;
    }

    @Override
    public Page<Outputs.QrestLotInfo> getRestrictionsByPage(Infos.ObjCommon objCommon,
                                                            Infos.QtimePageListInqInfo params) {
        List<Outputs.QrestLotInfo> result = Lists.newArrayList();

        final Timestamp currentTime = objCommon.getTimeStamp().getReportTimeStamp();

        StringBuilder builder = new StringBuilder(MINQT_RESTRICT_SELECT);
        if (ObjectIdentifier.isNotEmptyWithValue(params.getLotID())) {
            builder.append(" WHERE LOT_ID = '").append(params.getLotID().getValue()).append("'");
        }
        builder.append(" ORDER BY TARGET_TIME");
        Page<CimMinQTimeDO> data = cimJpaRepository.query(builder.toString(), CimMinQTimeDO.class,
                params.getSearchCondition());
        data.stream().filter(restrict -> restrict.getTargetTime().after(currentTime)).forEach(restrict ->
                getQRestLotInfo(result, restrict, currentTime));
        return new PageImpl<>(result, data.getPageable(), data.getTotalElements());
    }

    private void getQRestLotInfo(List<Outputs.QrestLotInfo> result, CimMinQTimeDO restrict, Timestamp currentTime) {
        Outputs.QrestLotInfo info = new Outputs.QrestLotInfo();
        info.setLotID(ObjectIdentifier.build(restrict.getLotID(), restrict.getLotObj()));
        Infos.QtimeInfo time = new Infos.QtimeInfo();
        time.setQTimeType(restrict.getTimerType());
        time.setRestrictionTriggerRouteID(ObjectIdentifier.build(
                restrict.getTriggerProcessDefinitionID(), restrict.getTriggerProcessDefinitionObj()));
        time.setRestrictionTriggerOperationNumber(restrict.getTriggerOpeNo());
        time.setRestrictionTriggerTimeStamp(restrict.getTriggerTime().toString());
        time.setRestrictionTargetRouteID(ObjectIdentifier.build(
                restrict.getTargetProcessDefinitionID(), restrict.getTargetProcessDefinitionObj()));
        time.setRestrictionTargetOperationNumber(restrict.getTargetOpeNo());
        time.setRestrictionTargetTimeStamp(restrict.getTargetTime().toString());
        time.setRestrictionRemainTime(CimDateUtils.convertToHHmmss(currentTime, restrict.getTargetTime()));
        info.setStrQtimeInfo(Lists.newArrayList(time));
        result.add(info);
    }

    private CimLot getLot(ObjectIdentifier lotID) {
        CimLot lot = genericCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(lot == null, retCodeConfig.getNotFoundLot());
        return lot;
    }

    private void checkParams(ObjectIdentifier parentLotID, ObjectIdentifier childLotID) {
        if (ObjectIdentifier.isEmpty(parentLotID) || ObjectIdentifier.isEmpty(childLotID) ||
                CimStringUtils.equals(parentLotID.getReferenceKey(), childLotID.getReferenceKey())) {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
    }

    private String getCurProcessID(CimLot lot) {
        // 通过Lot获取process operation, 并且判断不能为空
        CimProcessOperation processOpe = lot.getProcessOperation();
        Validations.check(processOpe == null, retCodeConfig.getNotFoundProcessOperation());

        // 获取当前process definition的ID, 并且判断不能为空
        ObjectIdentifier curProcessID = processOpe.getMainProcessID();
        Validations.check(curProcessID == null, retCodeConfig.getNotFoundProcessOperation());

        return nullToEmpty(curProcessID.getValue());
    }

    /**
     * 查找指定Lot在指定工步的指定条件Min Q-Time限制运行时数据
     * @param lotId Lot的ID
     * @param triggerPRPID Trigger Operation 的ID
     * @param triggerOpeNo Trigger Operation 的Operation Number
     * @param targetPRPID Target Operation的ID
     * @param targetOpeNo Target Operation的Operation Number
     * @return {@link CimMinQTimeRestriction}
     */
    private CimMinQTimeRestriction findRestriction(String lotId, String triggerPRPID, String triggerOpeNo,
                                                   String targetPRPID, String targetOpeNo) {
        final String findRestrictByLotAndOperation = MINQT_RESTRICT_SELECT + "WHERE LOT_RKEY = ? AND " +
                "TRIGGER_PRP_ID = ? AND TRIGGER_OPE_NO = ? AND TARGET_PRP_ID = ? AND TARGET_OPE_NO = ? AND rownum < 2";
        return genericCoreFactory.getBOByCustom(CimMinQTimeRestriction.class, findRestrictByLotAndOperation,
                lotId, triggerPRPID, triggerOpeNo, targetPRPID, targetOpeNo);
    }

    /**
     * 查找指定Lot在指定Operation的Operation Number时的所有Min Q-Time运行时限制数据
     * @param lotId Lot的ID
     * @param targetPRPID 目标站点的ID
     * @param targetOpeNo 目标站点的Operation Number
     * @return {@link CimMinQTimeRestriction} 返回符合条件的运行时限制数据集合
     */
    private List<CimMinQTimeRestriction> findRestrictions(String lotId, String targetPRPID, String targetOpeNo) {
        final String findRestrictByLotAndTarget = MINQT_RESTRICT_SELECT + "WHERE LOT_RKEY = ? AND " +
                "TARGET_PRP_ID = ? AND TARGET_OPE_NO = ?";
        return genericCoreFactory.getBOListByCustom(CimMinQTimeRestriction.class, findRestrictByLotAndTarget,
                lotId, targetPRPID, targetOpeNo);
    }

    /**
     * 查找指定Lot的所有Min Q-Time运行时限制数据
     * @param lotId Lot的ID
     * @return {@link CimMinQTimeRestriction} 返回符合条件的运行时限制数据集合
     */
    private List<CimMinQTimeRestriction> findRestrictions(String lotId, String triggerPRPID) {
        final String findRestrictByLotAndTrigger = MINQT_RESTRICT_SELECT + "WHERE LOT_RKEY = ? AND TRIGGER_PRP_ID = ?";
        return genericCoreFactory.getBOListByCustom(CimMinQTimeRestriction.class, findRestrictByLotAndTrigger,
                lotId, triggerPRPID);
    }

    /**
     * 查找指定Lot的所有Min Q-Time运行时限制数据
     * @param lotId Lot的ID
     * @return {@link CimMinQTimeRestriction} 返回符合条件的运行时限制数据集合
     */
    private List<CimMinQTimeRestriction> findRestrictions(String lotId) {
        final String findRestrictByLot = MINQT_RESTRICT_SELECT + "WHERE LOT_RKEY = ?";
        return genericCoreFactory.getBOListByCustom(CimMinQTimeRestriction.class, findRestrictByLot, lotId);
    }

    /**
     * 查询到达目标Operation的时间小于指定时间的所有Min Q-Time运行时限制数据
     * @param targetTime 设定的判断阀值时间
     * @param size 查询的数量
     * @return {@link CimMinQTimeRestriction} 返回符合条件的运行时限制数据集合
     */
    private List<CimMinQTimeRestriction> findRestrictions(Timestamp targetTime, int size) {
        final String findRestrictOfInvalid = MINQT_RESTRICT_SELECT + "WHERE TARGET_TIME < ? AND rownum < ?";
        return genericCoreFactory.getBOListByCustom(CimMinQTimeRestriction.class, findRestrictOfInvalid,
                targetTime, size);
    }

    /**
     * 查找指定Lot在指定初始数据时的Min Q-Time限制运行时数据
     * @param lotId Lot的ID
     * @param originalQTime 初始数据
     * @return {@link CimMinQTimeRestriction} 返回符合条件的运行时限制数据集合
     */
    private CimMinQTimeRestriction findRestrictByOriginalQTime(String lotId, String originalQTime) {
        final String findRestrictByLotAndOperation = MINQT_RESTRICT_SELECT + "WHERE LOT_RKEY = ? AND " +
                "INITIAL_QTIME_INFO = ? AND rownum < 2";
        return genericCoreFactory.getBOByCustom(CimMinQTimeRestriction.class, findRestrictByLotAndOperation,
                lotId, originalQTime);
    }

}
