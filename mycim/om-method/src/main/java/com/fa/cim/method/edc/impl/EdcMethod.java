package com.fa.cim.method.edc.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.BaseStaticMethod;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.edc.IEdcMethod;
import com.fa.cim.newcore.bo.dc.CimDataCollectionSpecification;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.dc.EDCDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@OmMethod
public class EdcMethod implements IEdcMethod {

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public void edcTempDataSet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier controlJobID,
                               List<Infos.DataCollectionInfo> edcData) {
        if (CimArrayUtils.isEmpty(edcData)) {
            return;
        }
        //Get lot Object
        CimLot lot = baseCoreFactory.getBO(CimLot.class, lotID);
        //Get Process Operation Object
        //Current PO or Previous PO ?
        Boolean checkConditionForPOOut = lotMethod.lotCheckConditionForPO(objCommon, lotID);
        CimProcessOperation po = CimBooleanUtils.isTrue(checkConditionForPOOut) ?
                lot.getProcessOperation() :
                lot.getPreviousProcessOperation();
        Validations.check(po == null, retCodeConfig.getNotFoundProcessOperation());

        List<ProcessDTO.DataCollectionInfo> dcDataList = edcData.stream()
                .map(this::createEdcDataFrom)
                .collect(Collectors.toList());
        po.setDataCollectionInfo(dcDataList);
    }

    private ProcessDTO.DataCollectionInfo createEdcDataFrom(Infos.DataCollectionInfo dcDef) {
        ProcessDTO.DataCollectionInfo dataCollectionInfo = new ProcessDTO.DataCollectionInfo();
        dataCollectionInfo.setDataCollectionDefinitionID(dcDef.getDataCollectionDefinitionID());
        dataCollectionInfo.setDescription(dcDef.getDescription());
        dataCollectionInfo.setDataCollectionType(dcDef.getDataCollectionType());
        dataCollectionInfo.setCalculationRequiredFlag(dcDef.getCalculationRequiredFlag());
        dataCollectionInfo.setSpecCheckRequiredFlag(dcDef.getSpecCheckRequiredFlag());
        dataCollectionInfo.setDataCollectionSpecificationID(dcDef.getDataCollectionSpecificationID());
        dataCollectionInfo.setDcSpecDescription(dcDef.getDcSpecDescription());
        dataCollectionInfo.setPreviousDataCollectionDefinitionID(dcDef.getPreviousDataCollectionDefinitionID());
        dataCollectionInfo.setPreviousOperationID(dcDef.getPreviousOperationID());
        dataCollectionInfo.setPreviousOperationNumber(dcDef.getPreviousOperationNumber());
        dataCollectionInfo.setDcItems(dcDef.getDcItems().stream()
                .map(this::createEdcItemDataFrom)
                .collect(Collectors.toList()));
        dataCollectionInfo.setDcSpecs(dcDef.getDcSpecs().stream()
                .map(this::createEdcSpecDataFrom)
                .collect(Collectors.toList()));
        return dataCollectionInfo;
    }

    private ProcessDTO.DataCollectionSpecInfo createEdcSpecDataFrom(Infos.DataCollectionSpecInfo dcSpec) {
        ProcessDTO.DataCollectionSpecInfo dcSpecInfo = new ProcessDTO.DataCollectionSpecInfo();
        dcSpecInfo.setDataItemName(dcSpec.getDataItemName());
        dcSpecInfo.setScreenLimitUpperRequired(dcSpec.getScreenLimitUpperRequired());
        dcSpecInfo.setScreenLimitUpper(dcSpec.getScreenLimitUpper());
        dcSpecInfo.setActionCodesUscrn(dcSpec.getActionCodesUscrn());
        dcSpecInfo.setScreenLimitLowerRequired(dcSpec.getScreenLimitLowerRequired());
        dcSpecInfo.setScreenLimitLower(dcSpec.getScreenLimitLower());
        dcSpecInfo.setActionCodesLscrn(dcSpec.getActionCodesLscrn());
        dcSpecInfo.setSpecLimitUpperRequired(dcSpec.getSpecLimitUpperRequired());
        dcSpecInfo.setSpecLimitUpper(dcSpec.getSpecLimitUpper());
        dcSpecInfo.setActionCodesUsl(dcSpec.getActionCodesUsl());
        dcSpecInfo.setSpecLimitLowerRequired(dcSpec.getSpecLimitLowerRequired());
        dcSpecInfo.setSpecLimitLower(dcSpec.getSpecLimitLower());
        dcSpecInfo.setActionCodesLsl(dcSpec.getActionCodesLsl());
        dcSpecInfo.setControlLimitUpperRequired(dcSpec.getControlLimitUpperRequired());
        dcSpecInfo.setControlLimitUpper(dcSpec.getControlLimitUpper());
        dcSpecInfo.setActionCodesUcl(dcSpec.getActionCodesUcl());
        dcSpecInfo.setControlLimitLowerRequired(dcSpec.getControlLimitLowerRequired());
        dcSpecInfo.setControlLimitLower(dcSpec.getControlLimitLower());
        dcSpecInfo.setActionCodesLcl(dcSpec.getActionCodesLcl());
        dcSpecInfo.setTarget(dcSpec.getTarget());
        dcSpecInfo.setTag(dcSpec.getTag());
        dcSpecInfo.setDcSpecGroup(BizConstant.EMPTY);
        return dcSpecInfo;
    }

    private ProcessDTO.DataCollectionItemInfo createEdcItemDataFrom(Infos.DataCollectionItemInfo dcItem) {
        ProcessDTO.DataCollectionItemInfo dataCollectionItemInfo = new ProcessDTO.DataCollectionItemInfo();
        dataCollectionItemInfo.setDataCollectionItemName(dcItem.getDataCollectionItemName());
        dataCollectionItemInfo.setDataCollectionMode(dcItem.getDataCollectionMode());
        dataCollectionItemInfo.setDataCollectionUnit(dcItem.getDataCollectionUnit());
        dataCollectionItemInfo.setDataType(dcItem.getDataType());
        dataCollectionItemInfo.setItemType(dcItem.getItemType());
        dataCollectionItemInfo.setMeasurementType(dcItem.getMeasurementType());
        dataCollectionItemInfo.setWaferID(dcItem.getWaferID());
        dataCollectionItemInfo.setWaferPosition(dcItem.getWaferPosition());
        dataCollectionItemInfo.setSitePosition(dcItem.getSitePosition());
        dataCollectionItemInfo.setHistoryRequiredFlag(dcItem.getHistoryRequiredFlag());
        dataCollectionItemInfo.setCalculationType(dcItem.getCalculationType());
        dataCollectionItemInfo.setCalculationExpression(dcItem.getCalculationExpression());
        dataCollectionItemInfo.setDataValue(dcItem.getDataValue());
        dataCollectionItemInfo.setTargetValue(dcItem.getTargetValue());
        dataCollectionItemInfo.setSpecCheckResult(dcItem.getSpecCheckResult());
        dataCollectionItemInfo.setActionCodes(dcItem.getActionCodes());
        return dataCollectionItemInfo;
    }


    @Override
    public Infos.StartRecipe lotStartRecipeInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier controlJobID) {
        CimLot lot = baseCoreFactory.getBO(CimLot.class, lotID);

        Outputs.ObjLotCheckConditionForPOByControlJobOut condition = lotMethod.lotCheckConditionForPOByControlJob(objCommon,
                lotID, controlJobID);

        CimProcessOperation aPosPO = CimBooleanUtils.isTrue(condition.getCurrentPOFlag()) ?
                lot.getProcessOperation() :
                lot.getPreviousProcessOperation();

        Validations.check(null == aPosPO, new OmCode(retCodeConfig.getNotFoundProcessOperation(),
                "", ObjectIdentifier.fetchValue(lotID)));

        ProcessDTO.ActualStartInformationForPO actualStartInfo = aPosPO.getActualStartInfo(true);

        Infos.StartRecipe startRecipe = new Infos.StartRecipe();
        startRecipe.setLogicalRecipeID(actualStartInfo.getAssignedLogicalRecipe());
        startRecipe.setMachineRecipeID(actualStartInfo.getAssignedMachineRecipe());
        startRecipe.setPhysicalRecipeID(actualStartInfo.getAssignedPhysicalRecipe());
        startRecipe.setDataCollectionFlag(actualStartInfo.getAssignedDataCollectionFlag());

        startRecipe.setStartReticleList(actualStartInfo.getAssignedReticles().stream().map(dto -> {
            Infos.StartReticleInfo startReticle = new Infos.StartReticleInfo();
            startReticle.setSequenceNumber(dto.getSequenceNumber());
            startReticle.setReticleID(dto.getReticleID());
            return startReticle;
        }).collect(Collectors.toList()));

        startRecipe.setStartFixtureList(actualStartInfo.getAssignedFixtures().stream().map(dto -> {
            Infos.StartFixtureInfo startFixture = new Infos.StartFixtureInfo();
            startFixture.setFixtureCategory(dto.getFixtureCategory());
            startFixture.setFixtureID(dto.getFixtureID());
            return startFixture;
        }).collect(Collectors.toList()));

        boolean clearRcpParamSetFlag = actualStartInfo.getAssignedRecipeParameterSets().stream()
                .mapToInt(param -> param.getRecipeParameterList().size())
                .sum() == 1;
        if(clearRcpParamSetFlag) {
            actualStartInfo.setAssignedRecipeParameterSets(Collections.emptyList());
        }

        if (CimBooleanUtils.isTrue(actualStartInfo.getAssignedDataCollectionFlag())) {
            startRecipe.setDcDefList(actualStartInfo.getAssignedDataCollections().stream()
                    .map(this::getEdcInfoFrom)
                    .collect(Collectors.toList()));
        } else {
            startRecipe.setDcDefList(Collections.emptyList());
        }
        return startRecipe;
    }

    private Infos.DataCollectionInfo getEdcInfoFrom(ProcessDTO.DataCollectionInfo edcInfo) {
        Infos.DataCollectionInfo dcDef = new Infos.DataCollectionInfo();
        dcDef.setDataCollectionDefinitionID(edcInfo.getDataCollectionDefinitionID());
        dcDef.setDescription(edcInfo.getDescription());
        dcDef.setDataCollectionType(edcInfo.getDataCollectionType());
        dcDef.setCalculationRequiredFlag(edcInfo.getCalculationRequiredFlag());
        dcDef.setSpecCheckRequiredFlag(edcInfo.getSpecCheckRequiredFlag());
        dcDef.setDataCollectionSpecificationID(edcInfo.getDataCollectionSpecificationID());
        dcDef.setDcSpecDescription(edcInfo.getDcSpecDescription());
        dcDef.setPreviousDataCollectionDefinitionID(edcInfo.getPreviousDataCollectionDefinitionID());
        dcDef.setPreviousOperationID(edcInfo.getPreviousOperationID());
        dcDef.setPreviousOperationNumber(edcInfo.getPreviousOperationNumber());
        dcDef.setDcItems(edcInfo.getDcItems().stream()
                .map(this::createEdcItemInfoFrom)
                .collect(Collectors.toList()));
        dcDef.setDcSpecs(edcInfo.getDcSpecs().stream().
                map(this::createEdcSpecInfoFrom)
                .collect(Collectors.toList()));
        return dcDef;
    }

    private Infos.DataCollectionItemInfo createEdcItemInfoFrom(ProcessDTO.DataCollectionItemInfo edcItemInfo) {
        Infos.DataCollectionItemInfo dcItem = new Infos.DataCollectionItemInfo();
        dcItem.setDataCollectionItemName(edcItemInfo.getDataCollectionItemName());
        dcItem.setDataCollectionMode(edcItemInfo.getDataCollectionMode());
        dcItem.setDataCollectionUnit(edcItemInfo.getDataCollectionUnit());
        dcItem.setDataType(edcItemInfo.getDataType());
        dcItem.setItemType(edcItemInfo.getItemType());
        dcItem.setMeasurementType(edcItemInfo.getMeasurementType());
        dcItem.setWaferID(edcItemInfo.getWaferID());
        dcItem.setWaferPosition(edcItemInfo.getWaferPosition());
        dcItem.setSitePosition(edcItemInfo.getSitePosition());
        dcItem.setHistoryRequiredFlag(edcItemInfo.getHistoryRequiredFlag());
        dcItem.setCalculationType(edcItemInfo.getCalculationType());
        dcItem.setCalculationExpression(edcItemInfo.getCalculationExpression());
        dcItem.setDataValue(edcItemInfo.getDataValue());
        dcItem.setTargetValue(edcItemInfo.getTargetValue());
        dcItem.setSpecCheckResult(edcItemInfo.getSpecCheckResult());
        return dcItem;
    }

    private Infos.DataCollectionSpecInfo createEdcSpecInfoFrom(ProcessDTO.DataCollectionSpecInfo edcSpecInfo) {
        Infos.DataCollectionSpecInfo dcSpec = new Infos.DataCollectionSpecInfo();
        dcSpec.setDataItemName(edcSpecInfo.getDataItemName());
        dcSpec.setScreenLimitLowerRequired(edcSpecInfo.getScreenLimitLowerRequired());
        dcSpec.setScreenLimitUpperRequired(edcSpecInfo.getScreenLimitUpperRequired());
        dcSpec.setScreenLimitUpper(edcSpecInfo.getScreenLimitUpper());
        dcSpec.setActionCodesUscrn(edcSpecInfo.getActionCodesUscrn());
        dcSpec.setSpecLimitLowerRequired(edcSpecInfo.getSpecLimitLowerRequired());
        dcSpec.setScreenLimitLower(edcSpecInfo.getScreenLimitLower());
        dcSpec.setActionCodesLscrn(edcSpecInfo.getActionCodesLscrn());
        dcSpec.setSpecLimitUpperRequired(edcSpecInfo.getSpecLimitUpperRequired());
        dcSpec.setSpecLimitUpper(edcSpecInfo.getSpecLimitUpper());
        dcSpec.setActionCodesUsl(edcSpecInfo.getActionCodesUsl());
        dcSpec.setSpecLimitLowerRequired(edcSpecInfo.getSpecLimitLowerRequired());
        dcSpec.setSpecLimitLower(edcSpecInfo.getSpecLimitLower());
        dcSpec.setActionCodesLsl(edcSpecInfo.getActionCodesLsl());
        dcSpec.setControlLimitUpperRequired(edcSpecInfo.getControlLimitUpperRequired());
        dcSpec.setControlLimitUpper(edcSpecInfo.getControlLimitUpper());
        dcSpec.setActionCodesUcl(edcSpecInfo.getActionCodesUcl());
        dcSpec.setControlLimitLowerRequired(edcSpecInfo.getControlLimitLowerRequired());
        dcSpec.setControlLimitLower(edcSpecInfo.getControlLimitLower());
        dcSpec.setActionCodesLcl(edcSpecInfo.getActionCodesLcl());
        dcSpec.setTarget(edcSpecInfo.getTarget());
        dcSpec.setTag(edcSpecInfo.getTag());
        return dcSpec;
    }

    @Override
    public List<Infos.DataCollectionInfo> edcSpecDataSet(Infos.ObjCommon objCommon, ObjectIdentifier lotID,
                                                         ObjectIdentifier controlJobID, List<Infos.DataCollectionInfo> edcDataInfo) {
        if (CimArrayUtils.isEmpty(edcDataInfo)) {
            return Collections.emptyList();
        }
        boolean fpcAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.isTrue();

        //Get lot Object
        CimLot lot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(lot == null, new OmCode(retCodeConfig.getNotFoundLot(),
                ObjectIdentifier.fetchValue(lotID)));

        Boolean checkConditionForPOOut = lotMethod.lotCheckConditionForPO(objCommon, lotID);
        //Get Process Operation Object
        //Current PO or Previous PO ?
        CimProcessOperation po = CimBooleanUtils.isTrue(checkConditionForPOOut) ?
                lot.getProcessOperation() :
                lot.getPreviousProcessOperation();

        Validations.check(po == null, new OmCode(retCodeConfig.getNotFoundProcessOperation(), ""));
        ProcessDTO.ActualStartInformationForPO actualStartInfo = po.getActualStartInfo(true);

        if (CimBooleanUtils.isFalse(actualStartInfo.getAssignedDataCollectionFlag())) {
            return edcDataInfo;
        }

        List<ProcessDTO.DataCollectionInfo> dcDataList = actualStartInfo.getAssignedDataCollections();
        boolean fpcDCSpecAvailable = fpcAdoptFlag &&
                !CimArrayUtils.isEmpty(dcDataList) &&
                !CimArrayUtils.isEmpty(dcDataList.get(0).getDcSpecs());

//        if (fpcDCSpecAvailable) {
//
//        } else {
//            edcDataInfo.stream().filter(edcData -> ObjectIdentifier.isNotEmpty(edcData.getDataCollectionDefinitionID()))
//                    .map(edcInfo -> {
//                        //Get DCSpec Object
//                        CimDataCollectionSpecification dcSpec = baseCoreFactory.getBO(CimDataCollectionSpecification.class,
//                                edcInfo.getDataCollectionSpecificationID());
//                        Validations.check(dcSpec == null,retCodeConfig.getNotFoundDcspec());
//                        List<EDCDTO.DCItemSpecification> dcItemSpecList = dcSpec.allDCSpecs();
//                        List<ProcessDTO.DataCollectionSpecInfo> saveData = dcItemSpecList.stream()
//                                .map(edcDataCollected -> {
//                                    ProcessDTO.DataCollectionSpecInfo dcSpecInfo = new ProcessDTO.DataCollectionSpecInfo();
//                                    dcSpecInfo.setDataItemName(edcDataCollected.getDataItemName());
//                                    dcSpecInfo.setScreenLimitUpperRequired(edcDataCollected.getScreenLimitUpperRequired());
//                                    dcSpecInfo.setScreenLimitUpper(edcDataCollected.getScreenLimitUpper());
//                                    dcSpecInfo.setActionCodesUscrn(edcDataCollected.getActionCodesUscrn());
//                                    dcSpecInfo.setScreenLimitLowerRequired(edcDataCollected.getScreenLimitLowerRequired());
//                                    dcSpecInfo.setScreenLimitLower(edcDataCollected.getScreenLimitLower());
//                                    dcSpecInfo.setActionCodesLscrn(edcDataCollected.getActionCodesLscrn());
//                                    dcSpecInfo.setSpecLimitUpperRequired(edcDataCollected.getSpecLimitUpperRequired());
//                                    dcSpecInfo.setSpecLimitUpper(edcDataCollected.getSpecLimitUpper());
//                                    dcSpecInfo.setActionCodesUsl(edcDataCollected.getActionCodesUsl());
//                                    dcSpecInfo.setSpecLimitLowerRequired(edcDataCollected.getSpecLimitLowerRequired());
//                                    dcSpecInfo.setSpecLimitLower(edcDataCollected.getSpecLimitLower());
//                                    dcSpecInfo.setActionCodesLsl(edcDataCollected.getActionCodesLsl());
//                                    dcSpecInfo.setControlLimitUpperRequired(edcDataCollected.getControlLimitUpperRequired());
//                                    dcSpecInfo.setControlLimitUpper(edcDataCollected.getControlLimitUpper());
//                                    dcSpecInfo.setActionCodesUcl(edcDataCollected.getActionCodesUcl());
//                                    dcSpecInfo.setControlLimitLowerRequired(edcDataCollected.getControlLimitLowerRequired());
//                                    dcSpecInfo.setControlLimitLower(edcDataCollected.getControlLimitLower());
//                                    dcSpecInfo.setActionCodesLcl(edcDataCollected.getActionCodesLcl());
//                                    dcSpecInfo.setTarget(edcDataCollected.getTarget());
//                                    dcSpecInfo.setTag(edcDataCollected.getTag());
//                                    dcSpecInfo.setDcSpecGroup(edcDataCollected.getDcSpecGroup());
//                                    return dcSpecInfo;
//                                }).collect(Collectors.toList());
//                        saveData.stream().filter()
//                    })
//        }
        return edcDataInfo;
    }
}
