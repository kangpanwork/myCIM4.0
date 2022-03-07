package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.nonruntime.contamination.CimContaminationOpeDo;
import com.fa.cim.entity.nonruntime.contamination.CimContaminationSorterDo;
import com.fa.cim.entity.runtime.eqp.CimEquipmentContaminationDO;
import com.fa.cim.entity.runtime.eqp.CimEquipmentDO;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.entity.runtime.lot.CimLotMaterialContainerDO;
import com.fa.cim.entity.runtime.po.CimProcessOperationDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IBondingGroupMethod;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.method.IContaminationMethod;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.*;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.dto.recipe.RecipeDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.prcssdfn.ProcessOperationSpecification;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.SorterType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * description: contamination method
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/25 0025        ********             Bear               create file
 *
 * @author: YJ
 * @date: 2020/11/25 0025 10:18
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class ContaminationMethod implements IContaminationMethod {

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private LotMethod lotMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Override
    public void lotCheckContaminationLevelAndPrFlagStepIn(List<ObjectIdentifier> lotIds, ObjectIdentifier eqpId, String actionCode) {
        lotIds.forEach(lotId -> lotCheckContaminationLevelAndPrFlagStepIn(lotId, eqpId, actionCode));
    }

    @Override
    public void lotCheckContaminationLevelStepOut(Infos.ObjCommon objCommon, ObjectIdentifier lotId) {
        // 【step 1】 check contamination level
        String holdType = lotCheckContaminationLevelState(lotId);

        //check if need  to change the carrier's category
        if (StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOn()) {
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotId);
            String requiredCassetteCategory = Optional.ofNullable(aLot.getRequiredCassetteCategory()).orElse("");
            if (CimStringUtils.isNotEmpty(requiredCassetteCategory) && !CimStringUtils.equals("None", requiredCassetteCategory)) {
                CimLotMaterialContainerDO cimLotMaterialContainerExample = new CimLotMaterialContainerDO();
                cimLotMaterialContainerExample.setSequenceNumber(0);
                cimLotMaterialContainerExample.setReferenceKey(ObjectIdentifier.fetchReferenceKey(aLot.getLotID()));
                CimLotMaterialContainerDO lotMaterialContainers = cimJpaRepository.findOne(Example.of(cimLotMaterialContainerExample)).orElse(null);
                if (null != lotMaterialContainers) {
                    CimCassette aCast = baseCoreFactory.getBO(CimCassette.class, lotMaterialContainers.getMaterialContainerObj());
                    if (requiredCassetteCategory.contains("2")) {
                        //A2B mode
                        String[] split = requiredCassetteCategory.split("2", 2);
                        if (CimStringUtils.isEmpty(split[0]) || CimStringUtils.isEmpty(split[1])) {
                            Validations.check(retCodeConfigEx.getReqCastCategoryNotAllowed(), requiredCassetteCategory);
                        }
                        requiredCassetteCategory = split[0];
                    }
                    boolean categoryChange = this.contaminationOperationMatchCheck(aCast.getCassetteCategory(), requiredCassetteCategory);
                    if (categoryChange) {
                        //update the carrier's category to new category
                        aCast.setCassetteCategory(requiredCassetteCategory);
                        holdType = null;
                    }
                }
            }
        }

        if (CimStringUtils.isNotEmpty(holdType)) {
            // 【step 2】lot hold
            lotContaminationHold(objCommon, lotId, holdType);
        }

    }

    @Override
    public boolean lotCheckContaminationLevelForHold(Infos.ObjCommon objCommon, ObjectIdentifier lotId) {
        // 【step 1】 check contamination level
        String holdType = lotCheckContaminationLevelState(lotId);

        if (CimStringUtils.isNotEmpty(holdType)) {
            // 【step 2】lot hold
            lotContaminationHold(objCommon, lotId, holdType);
        }
        return CimStringUtils.isNotEmpty(holdType);
    }

    /**
     * description: contamination hold
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon - common
     * @param lotId     - lot id
     * @author YJ
     * @date 2020/11/27 0027 17:25
     */
    private void lotContaminationHold(Infos.ObjCommon objCommon, ObjectIdentifier lotId, String holdType) {
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldType(BizConstant.SP_OPERATIONCATEGORY_LOTHOLD);
        lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(holdType));
        lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
        lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);

        CimLot lotBo = baseCoreFactory.getBO(CimLot.class, lotId);
        CimProcessDefinition mainProcessDefinition = lotBo.getMainProcessDefinition();
        if (Objects.nonNull(mainProcessDefinition)) {
            lotHoldReq.setRouteID(ObjectIdentifier.build(mainProcessDefinition.getIdentifier(), lotBo.getMainProcessDefinition().getPrimaryKey()));
        }
        lotHoldReq.setOperationNumber(lotBo.getOperationNumber());
        lotHoldReq.setRelatedLotID(lotId);

        List<Infos.LotHoldReq> holdReqList = Lists.newArrayList();
        holdReqList.add(lotHoldReq);
        List<Infos.HoldHistory> holdHistories = lotMethod.lotHold(objCommon, lotId, holdReqList);
        // 【step 3】 make history
        Inputs.LotHoldEventMakeParams lotHoldEventMakeParams = new Inputs.LotHoldEventMakeParams();
        lotHoldEventMakeParams.setHoldHistoryList(holdHistories);
        lotHoldEventMakeParams.setLotID(lotId);
        lotHoldEventMakeParams.setTransactionID(TransactionIDEnum.HOLD_LOT_REQ.getValue());
        eventMethod.lotHoldEventMake(objCommon, lotHoldEventMakeParams);
    }

    /**
     * description: check contamination level
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotId - lot id
     * @return whether match
     * @author YJ
     * @date 2020/11/27 0027 17:23
     */
    private String lotCheckContaminationLevelState(ObjectIdentifier lotId) {
        CimLot cimLotBO = baseCoreFactory.getBO(CimLot.class, lotId);
        // 【step 1】 get current step(pos) contamination level and lot contamination level
        log.info("lotCheckContaminationLevelState->info: {} get current step(pos) contamination level and lot contamination level!", lotId);
        CimProcessFlowContext processFlowContext = cimLotBO.getProcessFlowContext();
        if (CimObjectUtils.isEmpty(processFlowContext)) {
            log.info("lotCheckContaminationLevelState->info: the processFlowContext is empty!");
            return null;
        }
        CimProcessOperation currentProcessOperation = processFlowContext.getCurrentProcessOperation();
        if (CimObjectUtils.isEmpty(currentProcessOperation)) {
            log.info("lotCheckContaminationLevelState->info: the currentProcessOperation is empty!");
            return null;
        }
        CimProcessOperationSpecification processOperationSpecification = currentProcessOperation.getProcessOperationSpecification();
        if (Objects.isNull(processOperationSpecification)) {
            log.info("lotCheckContaminationLevelState->info: the processOperationSpecification is empty!");
            return null;
        }
        String contaminationInLevel = Optional.ofNullable(processOperationSpecification.getContaminationInLevel()).orElse("");
        if (contaminationInLevel.contains(".")) {
            String[] split = contaminationInLevel.split("\\.", 2);
            contaminationInLevel = split[1];
        }
        String contaminationLevel = Optional.ofNullable(cimLotBO.getContaminationLevel()).orElse("");
        // 【step 1-2】 check contamination whether match
        String requiredCassetteCategory = Optional.ofNullable(cimLotBO.getRequiredCassetteCategory()).orElse("");
        if (CimStringUtils.isNotEmpty(requiredCassetteCategory) && !CimStringUtils.equals("None", requiredCassetteCategory)) {
            String currentCarrierCategory = cimLotBO.getCurrentCarrierCategory();
            if (CimStringUtils.isNotEmpty(currentCarrierCategory)) {
                if (StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOn()) {
                    //qiandao mode
                    if (requiredCassetteCategory.contains("2")) {
                        //A2B mode
                        String[] split = requiredCassetteCategory.split("2", 2);
                        if (CimStringUtils.isEmpty(split[0]) || CimStringUtils.isEmpty(split[1])) {
                            Validations.check(retCodeConfigEx.getReqCastCategoryNotAllowed(), requiredCassetteCategory);
                        }
                        requiredCassetteCategory = split[0];
                        if (!CimStringUtils.equals(currentCarrierCategory, requiredCassetteCategory)) {
                            return BizConstant.SP_REASON_CARRIER_CATEGORY_HOLD;
                        }
                    } else {
                        //normal mode
                        if (!CimStringUtils.equals(currentCarrierCategory, requiredCassetteCategory)) {
                            //check if match the carrier change table
                            CimContaminationOpeDo example = new CimContaminationOpeDo();
                            example.setCurrentCarrierCategory(currentCarrierCategory);
                            example.setDesnationCarrierCategory(requiredCassetteCategory);
                            List<CimContaminationOpeDo> all = cimJpaRepository.findAll(Example.of(example));
                            if (!CimArrayUtils.isNotEmpty(all)) {
                                return BizConstant.SP_REASON_CARRIER_CATEGORY_HOLD;
                            }
                        }
                    }
                } else {
                    if (!CimStringUtils.equals(currentCarrierCategory, requiredCassetteCategory)) {
                        return BizConstant.SP_REASON_CARRIER_CATEGORY_HOLD;
                    }
                }
            }
        }
        if (CimStringUtils.isNotEmpty(contaminationInLevel)
                && CimStringUtils.isNotEmpty(contaminationLevel)
                && !CimStringUtils.equals(contaminationInLevel, contaminationLevel)) {
            return BizConstant.SP_REASON_CONTAMINATION_HOLD;
        }
        return null;
    }

    @Override
    public void lotCheckContaminationLevelAndPrFlagStepOut(Infos.ObjCommon objCommon, ObjectIdentifier lotId) {
        lotCheckContaminationLevelAndPrFlagStepOut(objCommon, lotId, null);
    }

    @Override
    public void lotCheckContaminationLevelAndPrFlagStepOut(Infos.ObjCommon objCommon, ObjectIdentifier lotId, ObjectIdentifier eqpId) {
        //update the lot's required carrier category
        this.lotReqCastCategorySetForPP(lotId);
        // 【step 1】 check contamination level
        String holdType = lotCheckContaminationLevelState(lotId);
        if (CimStringUtils.isNotEmpty(holdType)) {
            lotContaminationHold(objCommon, lotId, holdType);
        }
        // 【step 2】 find eqp id
        eqpId = lotFirstEqpId(lotId);
        // 【step 3】 get current step pr action , lot pr flag , eqp pr control
        if (ObjectIdentifier.isEmpty(eqpId)) {
            log.info("lotCheckContaminationLevelAndPrFlagStepOut: the eqpId is empty!");
            return;
        }

        CimLot cimLotBO = baseCoreFactory.getBO(CimLot.class, lotId);

        CimMachine cimMachineBO = baseCoreFactory.getBO(CimMachine.class, eqpId);
        String prControl = cimMachineBO.getPrControl();
        Integer prFlag = cimLotBO.getPrFlag();
        // 【step 4】pr flag is yes and eqp the pr control is avoid
        boolean isPrFlagMatch = CimNumberUtils.eq(prFlag, 1)
                && CimStringUtils.equals(BizConstant.EQP_PR_CONTROL_AVOID, prControl);
        if (isPrFlagMatch) {
            lotContaminationHold(objCommon, lotId, BizConstant.SP_REASON_PR_HOLD);
        }
    }

    @Override
    public void lotCheckContaminationLevelAndPrFlagStepOut(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotIds, ObjectIdentifier eqpId) {
//        lotIds.forEach(lotId -> lotCheckContaminationLevelAndPrFlagStepOut(objCommon, lotId, eqpId));
    }

    @Override
    public void lotCheckContaminationLevelStepOut(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotIds) {
        lotIds.forEach(lotId -> lotCheckContaminationLevelStepOut(objCommon, lotId));
    }

    @Override
    public void lotContaminationLevelAndPrFlagSet(ObjectIdentifier lotId) {
        // 【step 1】 get current step(pos) contamination level and lot contamination level
        if (ObjectIdentifier.isEmpty(lotId)) {
            log.info("lotContaminationLevelAndPrFlagSet->info: the lotId is empty!");
            return;
        }
        CimLot cimLotBO = baseCoreFactory.getBO(CimLot.class, lotId);

        CimProcessFlowContext processFlowContext = cimLotBO.getProcessFlowContext();
        if (CimObjectUtils.isEmpty(processFlowContext)) {
            log.info("lotContaminationLevelAndPrFlagSet->info: the processFlowContext is empty!");
            return;
        }
        CimProcessOperation currentProcessOperation = processFlowContext.getCurrentProcessOperation();
        if (CimObjectUtils.isEmpty(currentProcessOperation)) {
            log.info("lotContaminationLevelAndPrFlagSet->info: the currentProcessOperation is empty!");
            return;
        }
        CimProcessOperationSpecification processOperationSpecification = currentProcessOperation.getProcessOperationSpecification();
        // not find pos , step 1 ，not check
        if (Objects.isNull(processOperationSpecification)) {
            if (log.isInfoEnabled()) {
                log.info("lotContaminationLevelAndPrFlagSet->info: the processOperationSpecification is empty!");
            }
            return;
        }

        // 【step 2】 set contamination level and pr flag
        String contaminationOutLevel = processOperationSpecification.getContaminationOutLevel();
        if (CimStringUtils.isNotEmpty(contaminationOutLevel)) {
            if (contaminationOutLevel.contains(".")) {
                String[] split = contaminationOutLevel.split("\\.");
                contaminationOutLevel = split[1];
            }
            cimLotBO.setContaminationLevel(contaminationOutLevel);
        }
        String prAction = processOperationSpecification.getPrAction();
        if (CimStringUtils.equals(BizConstant.EQP_PR_ACTION_SET, prAction)) {
            cimLotBO.setPrFlag(1);
        } else if (CimStringUtils.equals(BizConstant.EQP_PR_ACTION_REMOVE, prAction)) {
            cimLotBO.setPrFlag(2);
        }
    }

    @Override
    public void lotReqCastCategorySetForPP(ObjectIdentifier lotId) {
        // 【step 1】 get current step(pos) contamination level and lot contamination level
        if (ObjectIdentifier.isEmpty(lotId)) {
            log.info("lotContaminationLevelAndPrFlagSet->info: the lotId is empty!");
            return;
        }
        CimLot cimLotBO = baseCoreFactory.getBO(CimLot.class, lotId);

        CimProcessFlowContext processFlowContext = cimLotBO.getProcessFlowContext();
        if (CimObjectUtils.isEmpty(processFlowContext)) {
            log.info("lotContaminationLevelAndPrFlagSet->info: the processFlowContext is empty!");
            return;
        }
        CimProcessOperation currentProcessOperation = processFlowContext.getCurrentProcessOperation();
        if (CimObjectUtils.isEmpty(currentProcessOperation)) {
            log.info("lotContaminationLevelAndPrFlagSet->info: the currentProcessOperation is empty!");
            return;
        }
        CimProcessOperationSpecification processOperationSpecification = currentProcessOperation.getProcessOperationSpecification();
        // not find pos , step 1 ，not check
        if (Objects.isNull(processOperationSpecification)) {
            if (log.isInfoEnabled()) {
                log.info("lotContaminationLevelAndPrFlagSet->info: the processOperationSpecification is empty!");
            }
            return;
        }
        cimLotBO.setRequiredCassetteCategory(processOperationSpecification.getRequiredCassetteCategory());

    }

    @Override
    public void carrierProductUsageTypeCheck(ObjectIdentifier productID, ObjectIdentifier lotID, ObjectIdentifier CastID) {
        CimProductSpecification aProduct;
        if (ObjectIdentifier.isNotEmpty(productID)) {
            aProduct = baseCoreFactory.getBO(CimProductSpecification.class, productID);
        } else {
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
            Validations.check(CimObjectUtils.isEmpty(aLot), retCodeConfig.getNotFoundLot(), lotID);
            ObjectIdentifier productSpecificationID = aLot.getProductSpecificationID();
            aProduct = baseCoreFactory.getBO(CimProductSpecification.class, productSpecificationID);
        }
        Validations.check(CimObjectUtils.isEmpty(aProduct), retCodeConfig.getNotFoundProductSpec());

        CimCassette aCast = baseCoreFactory.getBO(CimCassette.class, CastID);
        Validations.check(CimObjectUtils.isEmpty(aCast), retCodeConfig.getNotFoundCassette(), CastID);

        //get product usage type
        String productUsage = aProduct.getProductUsage();
        //get cast usage type
        String castUsage = aCast.getProductUsage();
        Validations.check(CimStringUtils.isNotEmpty(productUsage) && CimStringUtils.isNotEmpty(castUsage)
                && !CimStringUtils.equals(productUsage, castUsage), retCodeConfigEx.getCarrierUsageNotMatch());
    }

    @Override
    public void stbCheck(ObjectIdentifier cassetteID, ObjectIdentifier productRequestID) {
        String reqCastCategory = getCastReq(productRequestID.getValue());
        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
        String cassetteCategory = aCassette.getCassetteCategory();
        boolean chk_modeOn = StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOn();
        if (chk_modeOn) {
            if (cassetteCategory.contains("2")) {
                Validations.check(retCodeConfigEx.getDestinationCarrierCategoryNotMatch(), reqCastCategory);
            }
        }
        if (CimStringUtils.isNotEmpty(reqCastCategory)) {
            if (!CimStringUtils.equals(reqCastCategory, cassetteCategory)) {
                if (chk_modeOn) {
                    if (reqCastCategory.contains("2")) {
                        String[] split = reqCastCategory.split("2", 2);
                        if (CimStringUtils.isEmpty(split[0]) || CimStringUtils.isEmpty(split[1])) {
                            Validations.check(retCodeConfigEx.getReqCastCategoryNotAllowed(), reqCastCategory);
                        }
                        reqCastCategory = split[0];
                    }
                }
                Validations.check(!CimStringUtils.equals(reqCastCategory, cassetteCategory), retCodeConfig.getCassetteCategoryMismatch());
            }
        }
    }

    @Override
    public void lotPartialReworkCancelContaminationCheck(Infos.ObjCommon objCommon, ObjectIdentifier parentLotId, ObjectIdentifier childLotId) {
        // 【step 1】 check parent and child the contamination level and pr flag
        lotCheckContaminationLevelAndPrFlagMatchError(objCommon, parentLotId, childLotId);
        // 【step 3】 child check next contamination level
        lotCheckContaminationLevelStepOut(objCommon, childLotId);
    }

    @Override
    public void lotCheckContaminationLevelAndPrFlagMatchError(Infos.ObjCommon objCommon, ObjectIdentifier parentLotId, ObjectIdentifier childLotId) {
        // 【step 1】 get parent and child lot details
        CimLot parentLot = baseCoreFactory.getBO(CimLot.class, parentLotId);
        Validations.check(parentLot == null, retCodeConfig.getNotFoundLot(), parentLotId);
        CimLot childLot = baseCoreFactory.getBO(CimLot.class, childLotId);
        Validations.check(childLot == null, retCodeConfig.getNotFoundLot(), childLotId);

        // 【step 2】 check contamination level match
        assert parentLot != null;
        assert childLot != null;
        Validations.check(!CimStringUtils.equals(parentLot.getContaminationLevel(), childLot.getContaminationLevel()),
                retCodeConfigEx.getContaminationChildNotMatchParentState());

        // o = null  1 = YES , 2 = NO
        Integer parentPrFlag = parentLot.getPrFlag();
        parentPrFlag = Objects.isNull(parentPrFlag) || CimNumberUtils.eq(parentPrFlag, 0) ? 2 : parentPrFlag;

        Integer childPrFlag = childLot.getPrFlag();
        childPrFlag = Objects.isNull(childPrFlag) || CimNumberUtils.eq(childPrFlag, 0) ? 2 : childPrFlag;

        Validations.check(!parentPrFlag.equals(childPrFlag), retCodeConfigEx.getContaminationChildNotMatchParentState());
    }

    @Override
    public void inheritContaminationFlagFromParentLot(ObjectIdentifier parentLotId, ObjectIdentifier childLotId) {
        //get contamination level and pr flag
        Validations.check(ObjectIdentifier.isEmpty(parentLotId), retCodeConfig.getInvalidParameter());
        Validations.check(ObjectIdentifier.isEmpty(childLotId), retCodeConfig.getInvalidParameter());
        CimLot parentLot = baseCoreFactory.getBO(CimLot.class, parentLotId);
        String parentContaminationLevel = parentLot.getContaminationLevel();
        Integer parentPrFlag = parentLot.getPrFlag();
        //get child lot
        CimLot childLot = baseCoreFactory.getBO(CimLot.class, childLotId);
        childLot.setContaminationLevel(parentContaminationLevel);
        childLot.setPrFlag(parentPrFlag);
    }

    @Override
    public void lotWaferSlotMapChangeCheck(Infos.ObjCommon objCommon, Infos.WaferTransfer waferTransfer) {
        if (CimObjectUtils.isEmpty(waferTransfer)) {
            return;
        }
        CimCassette cimCassetteDO = baseCoreFactory.getBO(CimCassette.class, waferTransfer.getDestinationCassetteID());
        if (CimObjectUtils.isEmpty(cimCassetteDO)) {
            return;
        }
        // 【step 1】 query lot by wafer id
        ObjectIdentifier waferID = waferTransfer.getWaferID();
        if (ObjectIdentifier.isEmpty(waferID)) {
            return;
        }
        CimWafer cimWaferBO = baseCoreFactory.getBO(CimWafer.class, waferID);
        CimLot lot = (CimLot) cimWaferBO.getLot();

        //check if the target carrier match the source lot's product 's usage type
        CimProductSpecification productSpecification = lot.getProductSpecification();
        String productUsage = productSpecification.getProductUsage();
        String casstteUsage = cimCassetteDO.getProductUsage();
        Validations.check(CimStringUtils.isNotEmpty(productUsage) && CimStringUtils.isNotEmpty(casstteUsage)
                && !CimStringUtils.equals(productUsage, casstteUsage), retCodeConfigEx.getCarrierUsageNotMatch());
        // 【step 2】 query lots by target carrier id
        if (ObjectIdentifier.isEmpty(waferTransfer.getDestinationCassetteID())) {
            return;
        }

        List<ObjectIdentifier> lotIDList = Lists.newArrayList();
        try {
            Infos.LotListInCassetteInfo lotListInCassetteInfo = cassetteMethod.cassetteGetLotList(objCommon, waferTransfer.getDestinationCassetteID());
            lotIDList = lotListInCassetteInfo.getLotIDList();
        } catch (ServiceException ex) {
            if (!Validations.isEquals(ex.getCode(), retCodeConfig.getNotFoundLot())) {
                throw ex;
            }
        }

        // 【step 3】 check lots contamination level match
        lotIDList.forEach(lotId -> {
            CimLot lotDO = baseCoreFactory.getBO(CimLot.class, lotId);
            Validations.check(!CimStringUtils.equals(lot.getContaminationLevel(), lotDO.getContaminationLevel()), retCodeConfigEx.getContaminationWaferChangeState());
        });

        // 【step 3】 check lots pr flag match
        Integer sourcePR = lot.getPrFlag();
        if (CimNumberUtils.eq(sourcePR, 0) || CimNumberUtils.eq(sourcePR, 2)) {
            //source has no pr, destina cast should not have pr lot
            for (ObjectIdentifier lotID : lotIDList) {
                CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
                Integer prFlag = aLot.getPrFlag();
                if (CimNumberUtils.eq(prFlag, 1)) {
                    Validations.check(retCodeConfigEx.getContaminationWaferChangeState());
                }
            }
        } else {
            //source has pr, destina cast should not have clean lot
            for (ObjectIdentifier lotID : lotIDList) {
                CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
                Integer prFlag = aLot.getPrFlag();
                if (CimNumberUtils.eq(prFlag, 0) || CimNumberUtils.eq(prFlag, 2)) {
                    Validations.check(retCodeConfigEx.getContaminationWaferChangeState());
                }
            }
        }

        /*//check if the new carrier category match the step's requirement
        if (Objects.isNull(lot.getProcessOperation())) return;
        CimProcessOperationSpecification processOperationSpecification = lot.getProcessOperation().getProcessOperationSpecification();
        if (Objects.isNull(processOperationSpecification)) {
            return;
        }
        String requiredCassetteCategory = processOperationSpecification.getRequiredCassetteCategory();
        if (CimStringUtils.isEmpty(requiredCassetteCategory)) {
            return;
        }
        Validations.check(!CimStringUtils.equals(requiredCassetteCategory, cimCassetteDO.getCassetteCategory()), retCodeConfigEx.getNewCarrierCategoryNotMatch());*/

    }

    @Override
    public void lotOffRouteSorterCheckForQiandao(Infos.ObjCommon objCommon, Infos.WaferTransfer waferTransfer) {
        ObjectIdentifier destinationCassetteID = waferTransfer.getDestinationCassetteID();
        ObjectIdentifier originalCassetteID = waferTransfer.getOriginalCassetteID();
        if (ObjectIdentifier.isEmpty(destinationCassetteID)) {
            return;
        }
        CimCassette desCast = baseCoreFactory.getBO(CimCassette.class, destinationCassetteID);
        CimCassette souCast = baseCoreFactory.getBO(CimCassette.class, originalCassetteID);
        Validations.check(!CimStringUtils.equals(souCast.getCassetteCategory(), desCast.getCassetteCategory()), retCodeConfigEx.getDestinationCarrierCategoryNotMatch());
    }

    @Override
    public void lotWaferCarrierExchangeChangeCheck(Infos.ObjCommon objCommon, Infos.WaferTransfer waferTransfer, String actionCode) {

        if (CimObjectUtils.isEmpty(waferTransfer) || CimStringUtils.equals(actionCode, SorterType.Action.WaferStart.getValue())) {
            return;
        }
        CimCassette cimCassetteDO = baseCoreFactory.getBO(CimCassette.class, waferTransfer.getDestinationCassetteID());
        if (CimObjectUtils.isEmpty(cimCassetteDO)) {
            return;
        }
        // 【step 1】 query lot by wafer id
        ObjectIdentifier waferID = waferTransfer.getWaferID();
        CimWafer cimWaferBO = baseCoreFactory.getBO(CimWafer.class, waferID);
        CimLot aLot = (CimLot) cimWaferBO.getLot();

        //check if the target carrier match the source lot's product 's usage type
        CimProductSpecification productSpecification = aLot.getProductSpecification();
        String productUsage = productSpecification.getProductUsage();
        String casstteUsage = cimCassetteDO.getProductUsage();
        Validations.check(CimStringUtils.isNotEmpty(productUsage) && CimStringUtils.isNotEmpty(casstteUsage)
                && !CimStringUtils.equals(productUsage, casstteUsage), retCodeConfigEx.getCarrierUsageNotMatch());

        // 【step 2】 query lots by target carrier id
        if (ObjectIdentifier.isEmpty(waferTransfer.getDestinationCassetteID())) {
            return;
        }

        List<ObjectIdentifier> lotIDList = Lists.newArrayList();
        try {
            Infos.LotListInCassetteInfo lotListInCassetteInfo = cassetteMethod.cassetteGetLotList(objCommon, waferTransfer.getDestinationCassetteID());
            lotIDList = lotListInCassetteInfo.getLotIDList();
        } catch (ServiceException ex) {
            if (!Validations.isEquals(ex.getCode(), retCodeConfig.getNotFoundLot())) {
                throw ex;
            }
        }

        // 【step 3】 check lots contamination level match
        lotIDList.forEach(lotId -> {
            CimLot lotDO = baseCoreFactory.getBO(CimLot.class, lotId);
            Validations.check(!CimStringUtils.equals(aLot.getContaminationLevel(), lotDO.getContaminationLevel()), retCodeConfigEx.getContaminationWaferChangeState());
        });

        //check if the new carrier category match the next step's requirement
        //find next pos
        CimProcessOperation processOperation = aLot.getProcessOperation();
        if (CimObjectUtils.isEmpty(processOperation)) {
            return;
        }
        String cassetteCategory = cimCassetteDO.getCassetteCategory();
        if (!(CimStringUtils.equals(actionCode, SorterType.Action.LotTransfer.getValue()))
                && !(CimStringUtils.equals(actionCode, SorterType.Action.Combine.getValue()))
                && !(CimStringUtils.equals(actionCode, SorterType.Action.Separate.getValue()))
                && !(CimStringUtils.equals(actionCode, SorterType.Action.WaferEnd.getValue()))
                && CimStringUtils.isNotEmpty(actionCode)) {
            //no wafer transfer, check current step's operation carrier category
            CimProcessOperationSpecification processOperationSpecification = processOperation.getProcessOperationSpecification();
            if (CimObjectUtils.isEmpty(processOperationSpecification)) {
                return;
            }
            String requiredCassetteCategory = processOperationSpecification.getRequiredCassetteCategory();
            if (CimStringUtils.isNotEmpty(requiredCassetteCategory)) {
                Validations.check(!CimStringUtils.equals(requiredCassetteCategory, cassetteCategory), retCodeConfigEx.getDestinationCarrierCategoryNotMatch());
            }
        } else {
            //wafer transfer, check next step's operation carrier category
            CimProcessOperationSpecification currentPOS = processOperation.getModuleProcessOperationSpecification();
            if (CimObjectUtils.isEmpty(currentPOS)) {
                if (log.isInfoEnabled()) {
                    log.info("lotContaminationLevelAndPrFlagSet->info: the processOperationSpecification is empty!");
                }
                return;
            }
            ProcessOperationSpecification processOperationSpecification = aLot.getProcessFlow().nextProcessOperationSpecificationOnDefaultPathFrom(currentPOS);
            if (CimObjectUtils.isEmpty(processOperationSpecification)) {
                return;
            }
            if (!CimObjectUtils.isEmpty(processOperationSpecification)) {
                CimProcessOperationSpecification nextPOS = (CimProcessOperationSpecification) processOperationSpecification;
                String requiredCassetteCategory = nextPOS.getRequiredCassetteCategory();
                if (CimStringUtils.isNotEmpty(requiredCassetteCategory) && !CimStringUtils.equals(requiredCassetteCategory, "None")) {
                    Validations.check(!CimStringUtils.equals(requiredCassetteCategory, cassetteCategory), retCodeConfigEx.getDestinationCarrierCategoryNotMatch());
                }
            }
        }
    }

    @Override
    public void carrierExchangeCheckQiandaoMode(Infos.ObjCommon objCommon, ObjectIdentifier castID, ObjectIdentifier LotId, String actionCode) {
        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, castID);
        if (CimObjectUtils.isEmpty(aCassette) || CimStringUtils.equals(actionCode, SorterType.Action.WaferStart.getValue())) {
            return;
        }
        // 【step 1】 query lot by wafer id/lot id
        CimLot aLot;
        CimWafer aWafer = baseCoreFactory.getBO(CimWafer.class, LotId);
        if (!CimObjectUtils.isEmpty(aWafer)) {
            aLot = (CimLot) aWafer.getLot();
        } else {
            aLot = baseCoreFactory.getBO(CimLot.class, LotId);
        }

        List<ObjectIdentifier> lotIDList = Lists.newArrayList();
        try {
            Infos.LotListInCassetteInfo lotListInCassetteInfo = cassetteMethod.cassetteGetLotList(objCommon, castID);
            lotIDList = lotListInCassetteInfo.getLotIDList();
        } catch (ServiceException ex) {
            if (!Validations.isEquals(ex.getCode(), retCodeConfig.getNotFoundLot())) {
                throw ex;
            }
        }

        // 【step 3】 check lots contamination level match
        lotIDList.forEach(lotId -> {
            CimLot lotDO = baseCoreFactory.getBO(CimLot.class, lotId);
            Validations.check(!CimStringUtils.equals(aLot.getContaminationLevel(), lotDO.getContaminationLevel()), retCodeConfigEx.getContaminationWaferChangeState());
        });

        //check if the lot and carrier match the operation carrier category limit
        String requiredCassetteCategory = aLot.getRequiredCassetteCategory();
        //get lot's carrier category
        String currentCarrierCategory = aLot.getCurrentCarrierCategory();
        //get operation carrier category
        String destinationCassetteCategory = aCassette.getCassetteCategory();
        if (!CimStringUtils.equals(actionCode, SorterType.Action.LotTransfer.getValue())
                && !CimStringUtils.equals(actionCode, SorterType.Action.Combine.getValue())
                && !CimStringUtils.equals(actionCode, SorterType.Action.Separate.getValue())
                && !(CimStringUtils.equals(actionCode, SorterType.Action.WaferEnd.getValue()))
                && CimStringUtils.isNotEmpty(actionCode)) {
            //no wafer transfer
            Validations.check(!CimStringUtils.isEmpty(requiredCassetteCategory)
                    && !CimStringUtils.equals(destinationCassetteCategory, requiredCassetteCategory),
                    retCodeConfigEx.getDestinationCarrierCategoryNotMatch());
        } else {
            //wafer transfer,AA2BB MODE
            Validations.check(!CimStringUtils.equals(currentCarrierCategory + "2" + destinationCassetteCategory,
                    requiredCassetteCategory), retCodeConfigEx.getDestinationCarrierCategoryNotMatch());
        }

        //check usageType
        //check if the target carrier match the source lot's product 's usage type
        CimProductSpecification productSpecification = aLot.getProductSpecification();
        String productUsage = productSpecification.getProductUsage();
        String casstteUsage = aCassette.getProductUsage();
        Validations.check(CimStringUtils.isNotEmpty(productUsage) && CimStringUtils.isNotEmpty(casstteUsage)
                && !CimStringUtils.equals(productUsage, casstteUsage), retCodeConfigEx.getCarrierUsageNotMatch());

    }

    @Override
    public void contaminationLvlCheckAmongLots(List<Params.ContaminationAllLotCheckParams> checkParamsList) {
        if (CimArrayUtils.getSize(checkParamsList) == 1) {
            return;
        }
        List<CimLot> lotBoList = new ArrayList<>();
        for (Params.ContaminationAllLotCheckParams checkParams : checkParamsList) {
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, checkParams.getLotID());
            lotBoList.add(aLot);
            if (CimStringUtils.isNotEmpty(aLot.getContaminationLevel())) {
                checkParams.setContaminationIn(aLot.getContaminationLevel());
            }
            if (!checkParams.isMoveInFlag()){
                checkParams.setContaminationOut(aLot.getContaminationLevel());
            }else {
                CimProcessOperation processOperation = aLot.getProcessOperation();
                if (CimObjectUtils.isEmpty(processOperation)) {
                    log.info("lotContaminationLevelAndPrFlagSet->info: the currentProcessOperation is empty!");
                    continue;
                }
                CimProcessOperationSpecification processOperationSpecification = processOperation.getProcessOperationSpecification();
                if (CimObjectUtils.isEmpty(processOperationSpecification)) {
                    log.info("lotContaminationLevelAndPrFlagSet->info: the currentProcessOperationSpecification is empty!");
                    continue;
                }
                String contaminationOutLevel = processOperationSpecification.getContaminationOutLevel();
                if (CimStringUtils.isNotEmpty(contaminationOutLevel)) {
                    if (contaminationOutLevel.contains(".")) {
                        String[] split = contaminationOutLevel.split("\\.");
                        contaminationOutLevel = split[1];
                    }
                    checkParams.setContaminationOut(contaminationOutLevel);
                }
            }
        }

        for (Params.ContaminationAllLotCheckParams checkParams : checkParamsList) {
            String contaminationIn = checkParams.getContaminationIn();
            String contaminationOut = checkParams.getContaminationOut();
            for (Params.ContaminationAllLotCheckParams targetParams : checkParamsList){
                String contaminationIn1 = targetParams.getContaminationIn();
                String contaminationOut1 = targetParams.getContaminationOut();
                if (!(CimStringUtils.isEmpty(contaminationIn1) && CimStringUtils.isEmpty(contaminationIn))) {
                    Validations.check(!CimStringUtils.equals(contaminationIn1, contaminationIn),
                            retCodeConfigEx.getContaminationLevelMismatch());
                }
                if (!(CimStringUtils.isEmpty(contaminationOut) && CimStringUtils.isEmpty(contaminationOut1))) {
                    Validations.check(!CimStringUtils.equals(contaminationOut, contaminationOut1),
                            retCodeConfigEx.getContaminationLevelMismatch());
                }
            }
        }
    }

    @Override
    public void normalMoveInWaferBondingUsageCheck(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotList) {
        if (CimArrayUtils.isEmpty(lotList) || lotList.size() < 2) {
            return;
        }
        for (ObjectIdentifier lotId : lotList) {
            String strLotBondingGroupIDGetDROut = lotMethod.lotBondingGroupIDGetDR(objCommon, lotId);
            if (CimStringUtils.isEmpty(strLotBondingGroupIDGetDROut)) {
                continue;
            }
            Outputs.ObjBondingGroupInfoGetDROut objBondingGroupInfoGetDROut = bondingGroupMethod.bondingGroupInfoGetDR(objCommon, strLotBondingGroupIDGetDROut, true);
            Infos.BondingGroupInfo bondingGroupInfo = objBondingGroupInfoGetDROut.getBondingGroupInfo();
            List<Infos.BondingMapInfo> bondingMapInfoList = bondingGroupInfo.getBondingMapInfoList();
            ObjectIdentifier planTopLotID = ObjectIdentifier.emptyIdentifier();
            ObjectIdentifier baseLotID = ObjectIdentifier.emptyIdentifier();
            for (Infos.BondingMapInfo bondingMapInfo : bondingMapInfoList) {
                if (ObjectIdentifier.equals(planTopLotID, bondingMapInfo.getPlanTopLotID()) &&
                        ObjectIdentifier.equals(baseLotID, bondingMapInfo.getBaseLotID())) {
                    //same bonding group, continue
                    continue;
                } else {
                    planTopLotID = bondingMapInfo.getPlanTopLotID();
                    baseLotID = bondingMapInfo.getBaseLotID();
                }
                if (ObjectIdentifier.equals(lotId, planTopLotID)) {
                    for (ObjectIdentifier targetLot : lotList) {
                        if (ObjectIdentifier.equals(targetLot, baseLotID)) {
                            //both base/top lot will move in,check usage type
                            this.carrierProductUsageTypeCheck(ObjectIdentifier.emptyIdentifier(), planTopLotID,
                                    bondingMapInfo.getBaseCarrierID());
                        }
                    }
                } else if (ObjectIdentifier.equals(lotId, baseLotID)) {
                    for (ObjectIdentifier targetLot : lotList) {
                        if (ObjectIdentifier.equals(targetLot, planTopLotID)) {
                            //both base/top lot will move in,check usage type
                            this.carrierProductUsageTypeCheck(ObjectIdentifier.emptyIdentifier(), planTopLotID,
                                    bondingMapInfo.getBaseCarrierID());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void contaminationSorterCheck(Info.ComponentJob componentJob, String productRequestID, ObjectIdentifier equipmentID, ObjectIdentifier controlJJobId) {
        String actionCode = componentJob.getActionCode();
        List<Info.WaferSorterSlotMap> waferSorterSlotMapList = componentJob.getWaferList();
        String reqCastCategory = "";
        if (CimStringUtils.equals(actionCode, SorterType.Action.WaferStart.getValue())) {
            //wafer start, check the fisrt step's req of the flow
            reqCastCategory = getCastReq(productRequestID);
        }
        for (Info.WaferSorterSlotMap waferSorterSlotMap : waferSorterSlotMapList) {
            ObjectIdentifier lotID = waferSorterSlotMap.getLotID();
            if (ObjectIdentifier.isEmpty(lotID)) {
                //no lot , no check
                continue;
            }
            //check eqp contamination
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
            String contaminationLevel = aLot.getContaminationLevel();
            if (CimStringUtils.isNotEmpty(contaminationLevel)) {
                List<CimEquipmentContaminationDO> eqpContaminationByEqp = this.getEqpContaminationByEqp(equipmentID);
                boolean matchFlag = false;
                boolean emptyFlag = true;
                for (CimEquipmentContaminationDO equipmentContamination : eqpContaminationByEqp) {
                    emptyFlag = false;
                    String eqpContaminationLevel = equipmentContamination.getContaminationLevel();
                    if (CimStringUtils.equals(eqpContaminationLevel, contaminationLevel)) {
                        matchFlag = true;
                        break;
                    }
                }
                Validations.check(!matchFlag && !emptyFlag, retCodeConfigEx.getContaminationLevelMatchState());
            }
            ObjectIdentifier destinationCassetteID = componentJob.getDestinationCassetteID();
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, destinationCassetteID);
            if (CimObjectUtils.isEmpty(aCassette)) {
                continue;
            }
            if (!(CimStringUtils.equals(actionCode, SorterType.Action.LotTransfer.getValue()))
                    && !(CimStringUtils.equals(actionCode, SorterType.Action.WaferStart.getValue()))
                    && !(CimStringUtils.equals(actionCode, SorterType.Action.Combine.getValue()))
                    && !(CimStringUtils.equals(actionCode, SorterType.Action.Separate.getValue()))
                    && !(CimStringUtils.equals(actionCode, SorterType.Action.WaferEnd.getValue()))) {
                //no wafer transfer, no check
                continue;
            }
            String cassetteCategory = aCassette.getCassetteCategory();
            if (CimStringUtils.isNotEmpty(reqCastCategory) && !CimStringUtils.equals(reqCastCategory, "None")) {
                Validations.check(!CimStringUtils.equals(reqCastCategory, cassetteCategory), retCodeConfigEx.getDestinationCarrierCategoryNotMatch());
                continue;
            }
            if (!ObjectIdentifier.isEmpty(controlJJobId)) {
                //onRoute sorter, check next step req cast category
                //find next pos
                CimProcessOperationSpecification currentPOS = aLot.getProcessOperation().getProcessOperationSpecification();
                if (CimObjectUtils.isEmpty(currentPOS)) {
                    if (log.isInfoEnabled()) {
                        log.info("lotContaminationLevelAndPrFlagSet->info: the processOperationSpecification is empty!");
                    }
                    continue;
                }
                ProcessOperationSpecification processOperationSpecification = aLot.getProcessFlow().nextProcessOperationSpecificationOnDefaultPathFrom(currentPOS);
                if (!CimObjectUtils.isEmpty(processOperationSpecification)) {
                    CimProcessOperationSpecification nextPOS = (CimProcessOperationSpecification) processOperationSpecification;
                    String requiredCassetteCategory = nextPOS.getRequiredCassetteCategory();
                    if (CimStringUtils.isNotEmpty(requiredCassetteCategory) && !CimStringUtils.equals(requiredCassetteCategory, "None")) {
                        Validations.check(!CimStringUtils.equals(requiredCassetteCategory, cassetteCategory), retCodeConfigEx.getDestinationCarrierCategoryNotMatch());
                    }
                }
            } else {
                //off Route sorter, check current step req cast category
                String requiredCassetteCategory = aLot.getRequiredCassetteCategory();
                if (CimStringUtils.isNotEmpty(requiredCassetteCategory) && !CimStringUtils.equals("None", requiredCassetteCategory)) {
                    Validations.check(!CimStringUtils.equals(requiredCassetteCategory, cassetteCategory), retCodeConfigEx.getDestinationCarrierCategoryNotMatch());
                }
            }
        }
    }

    @Override
    public void contaminationSorterCheckForQiandao(Info.ComponentJob componentJob, String productRequestID, ObjectIdentifier eqID) {
        String actionCode = componentJob.getActionCode();
        List<Info.WaferSorterSlotMap> waferSorterSlotMapList = componentJob.getWaferList();
        String reqCastCategory = "";
        if (CimStringUtils.equals(actionCode, SorterType.Action.WaferStart.getValue())) {
            //wafer start, check the fisrt step's req of the flow
            reqCastCategory = getCastReq(productRequestID);
        }
        for (Info.WaferSorterSlotMap waferSorterSlotMap : waferSorterSlotMapList) {
            ObjectIdentifier lotID = waferSorterSlotMap.getLotID();
            if (ObjectIdentifier.isEmpty(lotID)) {
                //no lot , no check
                continue;
            }
            //check eqp contamination
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
            String contaminationLevel = aLot.getContaminationLevel();
            if (CimStringUtils.isNotEmpty(contaminationLevel)) {
                List<CimEquipmentContaminationDO> eqpContaminationByEqp = this.getEqpContaminationByEqp(eqID);
                boolean matchFlag = false;
                boolean emptyFlag = true;
                for (CimEquipmentContaminationDO equipmentContamination : eqpContaminationByEqp) {
                    emptyFlag = false;
                    String eqpContaminationLevel = equipmentContamination.getContaminationLevel();
                    if (CimStringUtils.equals(eqpContaminationLevel, contaminationLevel)) {
                        matchFlag = true;
                        break;
                    }
                }
                Validations.check(!matchFlag && !emptyFlag, retCodeConfigEx.getContaminationLevelMatchState());
            }

            if (!(CimStringUtils.equals(actionCode, SorterType.Action.LotTransfer.getValue()))
                    && !(CimStringUtils.equals(actionCode, SorterType.Action.WaferStart.getValue()))
                    && !(CimStringUtils.equals(actionCode, SorterType.Action.Combine.getValue()))
                    && !(CimStringUtils.equals(actionCode, SorterType.Action.Separate.getValue()))
                    && !(CimStringUtils.equals(actionCode, SorterType.Action.WaferEnd.getValue()))) {
                //no wafer transfer, no check
                continue;
            }
            ObjectIdentifier destinationCassetteID = componentJob.getDestinationCassetteID();
            CimCassette destinationCassette = baseCoreFactory.getBO(CimCassette.class, destinationCassetteID);
            if (CimObjectUtils.isEmpty(destinationCassette)) {
                continue;
            }
            String destinationCategory = destinationCassette.getCassetteCategory();
            //stb sorter check
            if (CimStringUtils.isNotEmpty(reqCastCategory) && !CimStringUtils.equals(reqCastCategory, "None")) {
                Validations.check(!CimStringUtils.equals(reqCastCategory, destinationCategory), retCodeConfigEx.getDestinationCarrierCategoryNotMatch());
                continue;
            }
            //other sorter check ,follow the sorter table
            CimContaminationSorterDo example = new CimContaminationSorterDo();
            example.setEqpID(ObjectIdentifier.fetchValue(eqID));
            ObjectIdentifier originalCassetteID = componentJob.getOriginalCassetteID();
            CimCassette originalCassette = baseCoreFactory.getBO(CimCassette.class, originalCassetteID);
            if (CimObjectUtils.isEmpty(originalCassette)) {
                continue;
            }
            example.setSourceCarrierCategory(originalCassette.getCassetteCategory());
            List<CimContaminationSorterDo> contaminationSorterList = cimJpaRepository.findAll(Example.of(example));
            Validations.check(CimArrayUtils.isEmpty(contaminationSorterList), retCodeConfigEx.getDestinationCarrierCategoryNotMatch());
            boolean matchflag = false;
            for (CimContaminationSorterDo contaminationSorterDo : contaminationSorterList) {
                String desnationCarrierCategory = contaminationSorterDo.getDesnationCarrierCategory();
                if (CimStringUtils.isEmpty(desnationCarrierCategory) || CimStringUtils.equals(desnationCarrierCategory, destinationCategory)) {
                    matchflag = true;
                    break;
                }
            }
            Validations.check(!matchflag, retCodeConfigEx.getDestinationCarrierCategoryNotMatch());
        }
    }

    private String getCastReq(String productRequestID) {
        String reqCastCategory;
        String sql = "SELECT TMP.MODULE_NO,\n" +
                "       TMP.STEP_NO,\n" +
                "       TMP.ID,\n" +
                "       TMP.MAIN_OPE_PF\n" +
                "     FROM (SELECT PF_PD.ROUTE_NO         AS MODULE_NO,\n" +
                "             POS.OPE_NO             AS STEP_NO,\n" +
                "             POS.ID,\n" +
                "             PF_PD.IDX_NO           AS MAIN_SEQNO,\n" +
                "             PF_POS.IDX_NO          AS MODULE_SEQNO,\n" +
                "             MAINPD.ACTIVE_PRF_RKEY AS MAIN_OPE_PF\n" +
                "      FROM OMPRORDER PRO_ORDER\n" +
                "               LEFT JOIN\n" +
                "           OMPRP MAINPD\n" +
                "           ON\n" +
                "               PRO_ORDER.MAIN_PROCESS_ID = MAINPD.PRP_ID\n" +
                "               LEFT JOIN\n" +
                "           OMPRF MAIN_MODPF\n" +
                "           ON\n" +
                "                   MAIN_MODPF.ID =\n" +
                "                   CASE\n" +
                "                       WHEN MAINPD.ACTIVE_MROUTE_PRF_RKEY IS NOT NULL\n" +
                "                           THEN MAINPD.ACTIVE_MROUTE_PRF_RKEY\n" +
                "                       ELSE\n" +
                "                           (SELECT r.ACTIVE_MROUTE_PRF_RKEY\n" +
                "                            FROM OMPRP r\n" +
                "                            WHERE r.id = MAINPD.ACTIVE_VER_RKEY)\n" +
                "                       END\n" +
                "               LEFT JOIN\n" +
                "           OMPRF_ROUTESEQ PF_PD\n" +
                "           ON\n" +
                "               MAIN_MODPF.ID = PF_PD.REFKEY\n" +
                "               LEFT JOIN\n" +
                "           OMPRP MODULEPD\n" +
                "           ON\n" +
                "               PF_PD.ROUTE_RKEY = MODULEPD.ID\n" +
                "               LEFT JOIN\n" +
                "           OMPRF MOUDLEPF\n" +
                "           ON\n" +
                "               MODULEPD.ACTIVE_PRF_RKEY = MOUDLEPF.ID\n" +
                "               LEFT JOIN\n" +
                "           OMPRF_PRSSSEQ PF_POS\n" +
                "           ON\n" +
                "               MOUDLEPF.ID = PF_POS.REFKEY\n" +
                "               LEFT JOIN\n" +
                "           OMPRSS POS\n" +
                "           ON\n" +
                "               PF_POS.PRSS_RKEY = POS.ID\n" +
                "      WHERE PRO_ORDER.PROD_ORDER_ID = ?1 \n" +
                "      ORDER BY PF_PD.IDX_NO,\n" +
                "               PF_POS.IDX_NO) TMP\n" +
                "WHERE ROWNUM = 1 ";


        Object[] resultQuery = cimJpaRepository.queryOne(sql, productRequestID);
        Validations.check(CimObjectUtils.isEmpty(resultQuery), retCodeConfig.getInvalidParameter());
        String opeNum = CimObjectUtils.toString(resultQuery[0]) + "." + CimObjectUtils.toString(resultQuery[1]);
        String PF_OBJ = CimObjectUtils.toString(resultQuery[3]);
        CimProcessFlow aPF = baseCoreFactory.getBO(CimProcessFlow.class, PF_OBJ);
        CimProcessOperationSpecification processOperationSpecificationOnDefault = aPF.findProcessOperationSpecificationOnDefault(opeNum);
        if (CimObjectUtils.isEmpty(processOperationSpecificationOnDefault)) {
            return null;
        }
        reqCastCategory = processOperationSpecificationOnDefault.getRequiredCassetteCategory();
        return reqCastCategory;
    }

    @Override
    public void carrierCategoryCheckAmongLotAndCarrier(ObjectIdentifier castID, ObjectIdentifier LotId) {
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, LotId);
        String requiredCassetteCategory = aLot.getRequiredCassetteCategory();
        if (CimStringUtils.isEmpty(requiredCassetteCategory) && !CimStringUtils.equals("None", requiredCassetteCategory)) {
            return;
        }
        CimCassette aCast = baseCoreFactory.getBO(CimCassette.class, castID);
        if (requiredCassetteCategory.contains("2")) {
            String[] split = requiredCassetteCategory.split("2", 2);
            if (CimStringUtils.isEmpty(split[0]) || CimStringUtils.isEmpty(split[1])) {
                Validations.check(retCodeConfigEx.getReqCastCategoryNotAllowed(), requiredCassetteCategory);
            }
            requiredCassetteCategory = split[0];
        }
        Validations.check(!CimStringUtils.equals(requiredCassetteCategory, aCast.getCassetteCategory()), new OmCode(retCodeConfigEx.getMismatchDestCastCategory()
                , castID.getValue(), aCast.getCassetteCategory(), requiredCassetteCategory, requiredCassetteCategory));
    }

    @Override
    public List<CimEquipmentContaminationDO> getEqpContaminationByEqp(ObjectIdentifier eqpID) {
        CimEquipmentContaminationDO example = new CimEquipmentContaminationDO();
        CimMachine machineBO = baseCoreFactory.getBO(CimMachine.class, eqpID);
        example.setReferenceKey(machineBO.getPrimaryKey());
        List<CimEquipmentContaminationDO> all = cimJpaRepository.findAll(Example.of(example));
        all.removeIf(next -> CimStringUtils.isEmpty(next.getContaminationLevel()));
        for (CimEquipmentContaminationDO equipmentContaminationDO : all) {
            String contaminationLevel = equipmentContaminationDO.getContaminationLevel();
            if (CimStringUtils.isNotEmpty(contaminationLevel)) {
                if (contaminationLevel.contains(".")) {
                    String[] split = contaminationLevel.split("\\.");
                    contaminationLevel = split[1];
                }
            }
            equipmentContaminationDO.setContaminationLevel(contaminationLevel);
        }
        return all;
    }

    @Override
    public boolean contaminationExchangeMatchCheck(ObjectIdentifier lotID, String sourceCarrierCategory, String destCarrierCategory) {
        String change = sourceCarrierCategory;
        if (!CimStringUtils.equals(sourceCarrierCategory, destCarrierCategory)) {
            change = sourceCarrierCategory + "2" + destCarrierCategory;
        }
        CimLot lotBO = baseCoreFactory.getBO(CimLot.class, lotID);
        String requiredCassetteCategory = lotBO.getRequiredCassetteCategory();
        return CimStringUtils.equals(requiredCassetteCategory, change);
    }

    @Override
    public boolean contaminationSorterMatchCheck(String EqpID, String sourceCate, String desCate) {
        CimContaminationSorterDo example = new CimContaminationSorterDo();
        example.setEqpID(EqpID);
        example.setSourceCarrierCategory(sourceCate);
        List<CimContaminationSorterDo> sorterDoList = cimJpaRepository.findAll(Example.of(example));
        sorterDoList.removeIf(sorterDo -> CimStringUtils.isNotEmpty(sorterDo.getDesnationCarrierCategory()) &&
                !CimStringUtils.equals(sorterDo.getDesnationCarrierCategory(), desCate));
        return CimArrayUtils.isNotEmpty(sorterDoList);
    }

    @Override
    public boolean contaminationOperationMatchCheck(String currentCate, String desCate) {
        CimContaminationOpeDo example = new CimContaminationOpeDo();
        example.setCurrentCarrierCategory(currentCate);
        example.setDesnationCarrierCategory(desCate);
        List<CimContaminationOpeDo> opeDos = cimJpaRepository.findAll(Example.of(example));
        return CimArrayUtils.isNotEmpty(opeDos);
    }

    @Override
    public void recipeContaminationCheck(ObjectIdentifier aLogicalRecipe, ObjectIdentifier aMachineRecipe, ObjectIdentifier LotID, ObjectIdentifier eqpID) {
        if (ObjectIdentifier.isEmpty(aLogicalRecipe) || ObjectIdentifier.isEmpty(aMachineRecipe)) {
            return;
        }
        CimLogicalRecipe aRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, aLogicalRecipe);
        List<RecipeDTO.DefaultRecipeSetting> defaultRecipeSettings = aRecipe.getDefaultRecipeSettings();
        List<RecipeDTO.ProcessResourceState> processResourceStates = new ArrayList<>();
        for (RecipeDTO.DefaultRecipeSetting defaultRecipeSetting : defaultRecipeSettings) {
            if (CimStringUtils.equals(ObjectIdentifier.fetchValue(defaultRecipeSetting.getRecipe()), aMachineRecipe.getValue())) {
                processResourceStates = defaultRecipeSetting.getProcessResourceStates();
            }
        }
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, LotID);
        String contaminationLevel = aLot.getContaminationLevel();
        if (CimStringUtils.isEmpty(contaminationLevel)) {
            return;
        }
        List<CimEquipmentContaminationDO> eqpContaminationByEqp = this.getEqpContaminationByEqp(eqpID);
        boolean allMatchFlag = true;
        for (RecipeDTO.ProcessResourceState processResourceState : processResourceStates) {
            if (processResourceState.getState()) {
                //chamber is on, check the contamination flag
                boolean matchFlag = false;
                boolean emptyFlag = true;
                for (CimEquipmentContaminationDO equipmentContamination : eqpContaminationByEqp) {
                    if (CimStringUtils.equals(equipmentContamination.getChamberID(), processResourceState.getProcessResourceName())) {
                        //chamber ID match,check the contamination flag
                        if (CimStringUtils.isNotEmpty(equipmentContamination.getContaminationLevel())) {
                            emptyFlag = false;
                            if (CimStringUtils.equals(equipmentContamination.getContaminationLevel(), contaminationLevel)) {
                                matchFlag = true;
                                break;
                            }
                        }
                    }
                }
                if (!matchFlag && !emptyFlag) {
                    allMatchFlag = false;
                    break;
                }
            }
        }
        if (!allMatchFlag) {
            Validations.check(retCodeConfigEx.getContaminationLevelMatchState());
        }
    }

    @Override
    public void lotContaminationUpdate(Params.LotContaminationParams params, Infos.ObjCommon objCommon) {
        // 【step 1】 update contamination level and pr flag
        ObjectIdentifier lotId = params.getLotId();
        Validations.check(ObjectIdentifier.isEmpty(lotId), retCodeConfig.getInvalidParameter());
        CimLot cimLotBO = baseCoreFactory.getBO(CimLot.class, params.getLotId());
        cimLotBO.setContaminationLevel(params.getContaminationLevel());
        cimLotBO.setPrFlag(params.getPrFlag());
        cimLotBO.setLastClaimedTimeStamp(CimDateUtils.getCurrentTimeStamp());
        User user = objCommon.getUser();
        CimPerson cimPerson = baseCoreFactory.getBO(CimPerson.class, user.getUserID());
        cimLotBO.setLastClaimedPerson(cimPerson);
    }

    @Override
    public void lotCheckContaminationLevelAndPrFlagStepIn(ObjectIdentifier lotId, ObjectIdentifier eqpId, String actionCode) {
        CimLot cimLotBO = baseCoreFactory.getBO(CimLot.class, lotId);
        // Vendor lot not check
        String lotType = cimLotBO.getLotBaseInfo().getLotType();
        if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_VENDORLOT, lotType)) {
            return;
        }

        // 【step 1】 get current step(pos) contamination level and lot contamination level
        CimProcessOperation currentProcessOperation = cimLotBO.getProcessFlowContext().getCurrentProcessOperation();
        String contaminationLevel = Optional.ofNullable(cimLotBO.getContaminationLevel()).orElse("");
        CimProcessOperationSpecification processOperationSpecification = null;
        if (!Objects.isNull(currentProcessOperation)) {
            processOperationSpecification = currentProcessOperation.getProcessOperationSpecification();
        }
        if (!Objects.isNull(processOperationSpecification)) {
            String contaminationInLevel = Optional.ofNullable(processOperationSpecification.getContaminationInLevel()).orElse("");
            if (contaminationInLevel.contains(".")) {
                String[] split = contaminationInLevel.split("\\.", 2);
                contaminationInLevel = split[1];
            }

            // 【step 1-2】 check contamination whether match
            Validations.check(CimStringUtils.isNotEmpty(contaminationInLevel)
                            && CimStringUtils.isNotEmpty(contaminationLevel)
                            && !CimStringUtils.equals(contaminationInLevel, contaminationLevel)
                    , retCodeConfigEx.getContaminationLevelMatchState());
        }

        //check eqp contamination
        if (!CimStringUtils.isEmpty(contaminationLevel)) {
            List<CimEquipmentContaminationDO> eqpContaminationByEqp = this.getEqpContaminationByEqp(eqpId);
            boolean matchFlag = false;
            boolean emptyFlag = true;
            for (CimEquipmentContaminationDO equipmentContamination : eqpContaminationByEqp) {
                emptyFlag = false;
                String eqpContaminationLevel = equipmentContamination.getContaminationLevel();
                if (CimStringUtils.equals(eqpContaminationLevel, contaminationLevel)) {
                    matchFlag = true;
                    break;
                }
            }
            Validations.check(!matchFlag && !emptyFlag, retCodeConfigEx.getContaminationLevelMatchState());
        }

        // get current step pr action , lot pr flag , eqp pr control
        CimMachine cimMachineBO = baseCoreFactory.getBO(CimMachine.class, eqpId);
        String prControl = cimMachineBO.getPrControl();
        Integer prFlag = cimLotBO.getPrFlag();
        // 【step 2-2】pr flag is yes and eqp the pr control is avoid
        Validations.check(1 == prFlag && CimStringUtils.equals(BizConstant.EQP_PR_CONTROL_AVOID, prControl),
                retCodeConfigEx.getContaminationPrFlagMatchState());
    }

    @Override
    public void lotSorterCJCreateCheck(ObjectIdentifier lotId, ObjectIdentifier eqpId, String actionCode) {
        CimMachine cimMachineBO = baseCoreFactory.getBO(CimMachine.class, eqpId);
        CimLot cimLotBO = baseCoreFactory.getBO(CimLot.class, lotId);
        //check carrier category
        lotSorterCheck(actionCode, cimMachineBO, cimLotBO);
    }

    private void lotSorterCheck(String actionCode, CimMachine cimMachineBO, CimLot cimLotBO) {
        Boolean cassetteChangeRequired = cimMachineBO.isCassetteChangeRequired();
        String equipmentCategory = cimMachineBO.getCategory();
        String requiredCassetteCategory = cimLotBO.getRequiredCassetteCategory();
        if (CimStringUtils.isNotEmpty(requiredCassetteCategory)) {
            if (StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOn()) {
                if (log.isInfoEnabled()){
                    log.info("OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE : " + StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.getValue());
                }
                //CDA mode ,might be AA2BB
                if (CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_WAFERSORTER, equipmentCategory)) {
                    if (log.isInfoEnabled()){
                        log.info("Equipment Category : " + equipmentCategory);
                    }
                    //sorter eqp,no need check exchange flag
                    if (CimStringUtils.equals(actionCode, SorterType.Action.LotTransfer.getValue())
                            || CimStringUtils.equals(actionCode, SorterType.Action.WaferEnd.getValue())) {
                        if (log.isInfoEnabled()){
                            log.info("actionCode : " + actionCode);
                        }
                        //online  wafer transfer
                        if (!requiredCassetteCategory.contains("2")) {
                            Validations.check(retCodeConfigEx.getReqCastCategoryNotAllowed(), requiredCassetteCategory);
                        }
                        String[] split = requiredCassetteCategory.split("2", 2);
                        if (CimStringUtils.isEmpty(split[0]) || CimStringUtils.isEmpty(split[1])) {
                            Validations.check(retCodeConfigEx.getReqCastCategoryNotAllowed(), requiredCassetteCategory);
                        }
                        requiredCassetteCategory = split[0];
                        if (log.isInfoEnabled()){
                            log.info("required Cassette Category : " + requiredCassetteCategory);
                        }
                    }
                } else {
                    if (requiredCassetteCategory.contains("2") && cassetteChangeRequired) {
                        //carrier exchange step
                        //A2B mode
                        String[] split = requiredCassetteCategory.split("2", 2);
                        if (CimStringUtils.isEmpty(split[0]) || CimStringUtils.isEmpty(split[1])) {
                            Validations.check(retCodeConfigEx.getReqCastCategoryNotAllowed(), requiredCassetteCategory);
                        }
                        requiredCassetteCategory = split[0];
                        if (log.isInfoEnabled()){
                            log.info("required Cassette Category : " + requiredCassetteCategory);
                        }
                    }
                }
            }
            String currentCarrierCategory = cimLotBO.getCurrentCarrierCategory();
            Validations.check(!CimStringUtils.equals(requiredCassetteCategory, currentCarrierCategory),
                    retCodeConfigEx.getNotMatchOpeCastCategory(), currentCarrierCategory, requiredCassetteCategory);
        }
    }

    @Override
    public void lotCheckPrFlagStepIn(ObjectIdentifier lotId, ObjectIdentifier eqpId) {
        // 【step 2】 get current step pr action , lot pr flag , eqp pr control
        if (ObjectIdentifier.isEmpty(eqpId)) {
            log.info("lotCheckPrFlagStepIn: the eqp id is empty!");
            return;
        }
        CimLot cimLotBO = baseCoreFactory.getBO(CimLot.class, lotId);
        CimMachine cimMachineBO = baseCoreFactory.getBO(CimMachine.class, eqpId);
        String prControl = cimMachineBO.getPrControl();
        Integer prFlag = cimLotBO.getPrFlag();
        // 【step 2-2】pr flag is yes and eqp the pr control is avoid
        Validations.check(CimNumberUtils.eq(prFlag, 1)
                && CimStringUtils.equals(BizConstant.EQP_PR_CONTROL_AVOID, prControl), retCodeConfigEx.getContaminationPrFlagMatchState());
    }

    @Override
    public void lotCheckPrFlagStepIn(ObjectIdentifier lotId) {
        // 【step 1】 get lot by id
        if (ObjectIdentifier.isEmpty(lotId)) {
            log.info("lotCheckPrFlagStepIn: the lot id is empty!");
            return;
        }
        ObjectIdentifier eqpId = lotFirstEqpId(lotId);
        // 【step 2】 check pr flag
        lotCheckPrFlagStepIn(lotId, eqpId);
    }


    /**
     * description: find lot first eqp by lot id
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotId - lot id
     * @return eqp id
     * @author YJ
     * @date 2020/11/27 0027 17:29
     */
    public ObjectIdentifier lotFirstEqpId(ObjectIdentifier lotId) {
        CimLotDO lot = new CimLotDO();
        lot.setId(lotId.getReferenceKey());
        lot = cimJpaRepository.findOne(Example.of(lot)).orElse(null);
        if (CimObjectUtils.isEmpty(lot)) {
            return null;
        }

        // 【step 2】 find first eqp
        ObjectIdentifier eqpId = null;
        if (CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_FINISHED, lot.getLotState())
                || CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_SHIPPED, lot.getLotState())
                || !CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, lot.getLotInventoryState())) {
            return null;
        } else {
            if (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lot.getLotProcessState())) {
                CimProcessOperationDO processOperation = cimJpaRepository.queryOne("SELECT * FROM OMPROPE WHERE ID = ?1",
                        CimProcessOperationDO.class, lot.getProcessOperationObj());
                if (null != processOperation) {
                    eqpId = new ObjectIdentifier(processOperation.getAssignEquipmentID(), processOperation.getAssignEquipmentObj());
                    CimEquipmentDO equipment = cimJpaRepository.queryOne("SELECT * FROM OMEQP WHERE EQP_ID = ?1",
                            CimEquipmentDO.class, processOperation.getAssignEquipmentID());
                    Validations.check(null == equipment, retCodeConfig.getNotFoundEqp());
                }
            } else if (CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, lot.getLotHoldState())) {
                String sql = "SELECT OMLOT_HOLDEQP.EQP_ID,\n" +
                        "  OMLOT_HOLDEQP.EQP_RKEY,\n" +
                        "  OMEQP.DESCRIPTION\n" +
                        "  FROM   OMLOT_HOLDEQP, OMEQP\n" +
                        "  WHERE  OMLOT_HOLDEQP.REFKEY = ?1\n" +
                        "    AND  OMEQP.EQP_ID = OMLOT_HOLDEQP.EQP_ID";
                List<Object[]> lotHoldEquipments = cimJpaRepository.query(sql, lot.getId());
                if (CimArrayUtils.isNotEmpty(lotHoldEquipments)) {
                    Object[] lotEquipment = lotHoldEquipments.get(0);
                    eqpId = new ObjectIdentifier((String) lotEquipment[0], (String) lotEquipment[1]);
                }
            } else if (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_WAITING, lot.getLotProcessState())) {
                String sql1 = "SELECT OMLOT_EQP.EQP_ID,\n" +
                        "        OMLOT_EQP.EQP_RKEY,\n" +
                        "        OMEQP.DESCRIPTION\n" +
                        " FROM   OMLOT_EQP, OMEQP\n" +
                        " WHERE  OMLOT_EQP.REFKEY = ?1\n" +
                        " AND    OMEQP.EQP_ID = OMLOT_EQP.EQP_ID";
                List<Object[]> lotEquipments = cimJpaRepository.query(sql1, lot.getId());
                if (!CollectionUtils.isEmpty(lotEquipments)) {
                    Object[] lotEquipment = lotEquipments.get(0);
                    eqpId = new ObjectIdentifier((String) lotEquipment[0], (String) lotEquipment[1]);
                }
            }
        }
        return eqpId;
    }
}
