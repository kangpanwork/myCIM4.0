package com.fa.cim.service.constraint.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimPageUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IConstraintMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.mfg.MfgInfoParams;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.service.constraint.IConstraintInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/9        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/9 10:56
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class IConstraintInqServiceImpl implements IConstraintInqService {

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IEquipmentMethod equipmentMethod;
    @Autowired
    private IConstraintMethod constraintMethod;

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param mfgRestrictListInqParams mfgRestrictListInqParams
     * @param objCommon        objCommon
     * @return com.fa.cim.dto.result.MfgRestrictListInqResult
     * @author Sun
     * @date 2018/09/29
     */

    public Results.NewMfgRestrictListInqResult sxMfgRestrictListInq(Params.MfgRestrictListInqParams mfgRestrictListInqParams, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxMfgRestrictListInq()");

        //Step-1:call entityInhibitAttributesGetDR;
//        Page<Infos.EntityInhibitDetailInfo> entityInhibitAttributesGetDR = entityInhibitMethod.entityInhibitAttributesGetDR(mfgRestrictListInqParams,objCommon);
        List<Infos.EntityInhibitDetailInfo> entityInhibitAttributesGetDR = constraintMethod.constraintAttributesGetDR(objCommon, mfgRestrictListInqParams.getEntityInhibitDetailAttributes(), mfgRestrictListInqParams.getEntityInhibitReasonDetailInfoFlag());
        log.info("【Method Exit】sxMfgRestrictListInq()");
        /*Page page = PageUtils.convertListToPage(entityInhibitAttributesGetDR,
                mfgRestrictListInqParams.getSearchCondition().getPage(),
                mfgRestrictListInqParams.getSearchCondition().getSize());*/
        Results.NewMfgRestrictListInqResult mfgRestrictListInqResult = new Results.NewMfgRestrictListInqResult();
        mfgRestrictListInqResult.setStrEntityInhibitions(new PageImpl<>(entityInhibitAttributesGetDR));
        mfgRestrictListInqResult.setStrEntityInhibitions(CimPageUtils.convertListToPage(entityInhibitAttributesGetDR,mfgRestrictListInqParams.getSearchCondition().getPage(),mfgRestrictListInqParams.getSearchCondition().getSize()));
        return mfgRestrictListInqResult;
    }

    @Override
    public List<Infos.ConstraintEqpDetailInfo> sxMfgRestrictListInq_110(Params.MfgRestrictListInqParams mfgRestrictListInqParams, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxMfgRestrictListInq()");
        //Step-1:call entityInhibitAttributesGetDR;
        return constraintMethod.constraintAttributeListGetDR(mfgRestrictListInqParams,objCommon);
    }

    @Override
    public Results.MfgRestrictListByEqpInqResult sxMfgRestrictListByEqpInq(Params.MfgRestrictListByEqpInqParams mfgRestrictListByEqpInqParams, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxMfgRestrictByEqpListInq()");
        String eqpID = mfgRestrictListByEqpInqParams.getEqpID();
        ObjectIdentifier bayID = mfgRestrictListByEqpInqParams.getBayID();
        Validations.check(ObjectIdentifier.isEmpty(bayID) && CimStringUtils.isEmpty(eqpID), retCodeConfig.getInvalidInputParam());
        List<Infos.ConstraintEqpDetailInfo> entityInhibitAttributesGetDR = new ArrayList<>();
        Validations.check(!ObjectIdentifier.isEmpty(bayID) && !CimStringUtils.isEmpty(eqpID), retCodeConfig.getInvalidInputParam());
        //Step-1:call entityInhibitAttributesGetDR;
        if (CimStringUtils.isNotEmpty(eqpID)){
            entityInhibitAttributesGetDR = constraintMethod.constraintAttributesGetByEqpDR(mfgRestrictListByEqpInqParams,objCommon);
        }else {
            Infos.EquipmentListInfoGetDRIn strEquipmentListInfoGetDRIn = new Infos.EquipmentListInfoGetDRIn();
            strEquipmentListInfoGetDRIn.setWorkArea(bayID);
            strEquipmentListInfoGetDRIn.setEquipmentID(ObjectIdentifier.emptyIdentifier());
            Infos.EquipmentListInfoGetDROut equipmentListInfoGetDROut = equipmentMethod.equipmentListInfoGetDR(objCommon, strEquipmentListInfoGetDRIn);
            List<Infos.AreaEqp> strAreaEqp = equipmentListInfoGetDROut.getStrAreaEqp();
            entityInhibitAttributesGetDR = constraintMethod.constraintAttributesGetByEqpListDR(strAreaEqp, objCommon);
        }
        log.info("【Method Exit】sxMfgRestrictByEqpListInq()");
        Results.MfgRestrictListByEqpInqResult mfgRestrictListByEqpInqResult = new Results.MfgRestrictListByEqpInqResult();
        mfgRestrictListByEqpInqResult.setStrEntityInhibitions(entityInhibitAttributesGetDR);
        return mfgRestrictListByEqpInqResult;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 11/27/2018 10:03 AM
     * @param params -
     * @param objCommon -
     * @return com.fa.cim.dto.RetCode<Results.MfgRestrictListInqResult>
     */
    public List<Infos.EntityInhibitDetailInfo> sxMfgRestrictExclusionLotListInq(Params.MfgRestrictExclusionLotListInqParams params, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxMfgRestrictExclusionLotListInq()");

        //Step-1:call entityInhibitExceptionLot_attributes_GetDR;
        Inputs.ObjEntityInhibitExceptionLotAttributesIn inParams = new Inputs.ObjEntityInhibitExceptionLotAttributesIn();
        inParams.setEntityIdentifierList(params.getEntities());
        Infos.EntityInhibitExceptionLotInfo exceptionLotInfo = new Infos.EntityInhibitExceptionLotInfo();
        exceptionLotInfo.setLotID(params.getLotID());
        exceptionLotInfo.setClaimUserID(params.getOwnerID());
        exceptionLotInfo.setUsedFlag(false);
        exceptionLotInfo.setSingleTriggerFlag(false);
        inParams.setExceptionLotInfo(exceptionLotInfo);
        List<Infos.EntityInhibitDetailInfo>  result = constraintMethod.constraintExceptionLotAttributesGetDR(objCommon, inParams.getEntityIdentifierList(), inParams.getExceptionLotInfo());

        log.info("【Method Exit】sxMfgRestrictExclusionLotListInq()");

        return result;
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params
     * @param objCommon
     * @return RetCode<Results.ProcessDefinitionIndexListInqResult>
     * @author Sun
     * @date 2018/10/24
     */
    public List<Infos.ProcessDefinitionIndexList> sxRouteListInq(Params.RouteListInqParams params, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxRouteListInq()");
        List<Infos.ProcessDefinitionIndexList> processDefinitionIndexListList = processMethod.processDefinitionIDList(objCommon,params);
        log.info("【Method Exit】sxRouteListInq()");
        return processDefinitionIndexListList;
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params -
     * @return com.fa.cim.dto.RetCode<List<Results.StageListInqResult>>
     * @author Sun
     * @since 2018/10/24
     */
    @Override
    public Results.StageListInqResult sxStageListInq(Infos.ObjCommon objCommon, Params.StageListInqParams params) {
        log.info("【Method Entry】sxStageListInq()");

        //Inquiry list of stage;
        Results.StageListInqResult resultRetCode = processMethod.processStageIDGetDR(objCommon,params);

        log.info("【Method Exit】sxStageListInq()");
        return resultRetCode;
    }

    @Override
    public Results.RecipeTimeInqResult recipeTimeLimitListInq(Params.RecipeTimeInqParams recipeTimeInqParams, Infos.ObjCommon objCommon) {
        Results.RecipeTimeInqResult soitecRecipeTimeInqResult = new Results.RecipeTimeInqResult();
        List<Infos.RecipeTime> soitecRecipeTimes = constraintMethod.recipeTimeLimitListQuery(recipeTimeInqParams);
        soitecRecipeTimeInqResult.setRecipeTimeList(soitecRecipeTimes);
        return soitecRecipeTimeInqResult;
    }

    @Override
    public List<Infos.ConstraintEqpDetailInfo> sxConstraintListByEqpInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String functionRule) {
        return constraintMethod.constraintListByEqp(objCommon, equipmentID, functionRule, true);
    }

    @Override
    public List<MfgInfoParams> sxConstraintInfoExportInq(Infos.ObjCommon objCommon,List<Infos.ConstraintEqpDetailInfo> constraintEqpDetailInfos) {
        List<MfgInfoParams> mfgInfoParamsList = new ArrayList<>();
        for (Infos.ConstraintEqpDetailInfo constraintEqpDetailInfo : constraintEqpDetailInfos){
            MfgInfoParams mfgInfoParams = new MfgInfoParams();
            mfgInfoParamsList.add(mfgInfoParams);
            mfgInfoParams.setConstraintID(constraintEqpDetailInfo.getEntityInhibitID().getValue());
            Infos.ConstraintDetailAttributes entityInhibitDetailAttributes = constraintEqpDetailInfo.getEntityInhibitDetailAttributes();
            mfgInfoParams.setEndTime(entityInhibitDetailAttributes.getEndTimeStamp());
            mfgInfoParams.setStartTime(entityInhibitDetailAttributes.getStartTimeStamp());
            mfgInfoParams.setClaimTime(entityInhibitDetailAttributes.getClaimedTimeStamp());
            mfgInfoParams.setRuleFunction(entityInhibitDetailAttributes.getFunctionRule());
            mfgInfoParams.setReasonCode(entityInhibitDetailAttributes.getReasonCode());
            mfgInfoParams.setOwner(entityInhibitDetailAttributes.getOwnerID().getValue());
            String memo = entityInhibitDetailAttributes.getMemo();
            if (CimStringUtils.isEmpty(memo)){
                mfgInfoParams.setMemo("");
            }else {
                mfgInfoParams.setMemo(memo);
            }
            mfgInfoParams.setConstraintType(entityInhibitDetailAttributes.getConstraintType());
            List<String> subLotTypes = entityInhibitDetailAttributes.getSubLotTypes();
            if (CimArrayUtils.isEmpty(subLotTypes)){
                mfgInfoParams.setSubLotType("*");
            }else {
                StringBuilder sb = new StringBuilder();
                Iterator<String> iterator = subLotTypes.iterator();
                while (iterator.hasNext()){
                    String sub = iterator.next();
                    if (iterator.hasNext()){
                        sb.append(sub);
                        sb.append(",");
                    }else {
                        sb.append(sub);
                    }
                }
                mfgInfoParams.setSubLotType(sb.toString());
            }
            List<Infos.EntityIdentifier> exceptionEntities = entityInhibitDetailAttributes.getExceptionEntities();
            if (CimArrayUtils.isNotEmpty(exceptionEntities)){
                for (Infos.EntityIdentifier exceptionEntity : exceptionEntities){
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_LOT,exceptionEntity.getClassName())){
                        mfgInfoParams.setExLotID(exceptionEntity.getObjectID().getValue());
                    }else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_PRODUCT,exceptionEntity.getClassName())){
                        mfgInfoParams.setExProduct(exceptionEntity.getObjectID().getValue());
                    }else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_ROUTE,exceptionEntity.getClassName())){
                        mfgInfoParams.setExMainPF(exceptionEntity.getObjectID().getValue());
                    }
                }
            }
            List<Infos.EntityIdentifier> entities = entityInhibitDetailAttributes.getEntities();
            if (CimArrayUtils.isNotEmpty(entities)){
                for (Infos.EntityIdentifier entity : entities){
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_EQUIPMENT,entity.getClassName())){
                        mfgInfoParams.setEquipment(entity.getObjectID().getValue());
                    }
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_PRODUCT,entity.getClassName())){
                        mfgInfoParams.setProduct(entity.getObjectID().getValue());
                    }
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_ROUTE,entity.getClassName())){
                        mfgInfoParams.setMainPF(entity.getObjectID().getValue());
                    }
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_MODULEPD,entity.getClassName())){
                        mfgInfoParams.setRoute(entity.getObjectID().getValue());
                    }
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_PROCESS,entity.getClassName())){
                        mfgInfoParams.setStep(entity.getObjectID().getValue());
                    }
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_OPERATION,entity.getClassName())){
                        mfgInfoParams.setMainPF(entity.getObjectID().getValue());
                        mfgInfoParams.setOperation(entity.getAttribution());
                    }
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE,entity.getClassName())){
                        mfgInfoParams.setRecipe(entity.getObjectID().getValue());
                    }
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_RETICLE,entity.getClassName())){
                        mfgInfoParams.setReticle(entity.getObjectID().getValue());
                    }
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_RETICLEGROUP,entity.getClassName())){
                        mfgInfoParams.setReticleGrp(entity.getObjectID().getValue());
                    }
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_CHAMBER,entity.getClassName())){
                        mfgInfoParams.setEquipment(entity.getObjectID().getValue());
                        mfgInfoParams.setChamber(entity.getAttribution());
                    }
                }
            }
        }
        return mfgInfoParamsList;
    }

    @Override
    public Page<Infos.ConstraintHistoryDetailInfo> sxConstraintHistoryListInq(Infos.ObjCommon objCommon, String functionRule, Boolean specificTool, SearchCondition searchCondition) {
        // TODO: 2021/6/11 if need user information
        String userID = objCommon.getUser().getUserID().getValue();
        return constraintMethod.constrintHistoryGet(userID,functionRule,specificTool,searchCondition);
    }
}
