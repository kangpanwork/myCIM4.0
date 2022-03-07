package com.fa.cim.service.constraint.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IConstraintMethod;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.mfg.MfgInfoParams;
import com.fa.cim.service.constraint.IConstraintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
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
 * @date: 2020/9/9 10:46
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class IConstraintServiceImpl implements IConstraintService {

    @Autowired
    private IEventMethod eventMethod;
    @Autowired
    private IConstraintMethod constraintMethod;
    @Autowired
    private RetCodeConfig retCodeConfig;


    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param mfgRestrictReqParams
     * @param objCommon              objCommon
     * @return com.fa.cim.dto.result.MfgRestrictListInqResult
     * @author Sun
     * @date 2018/10/11
     */

    public Infos.EntityInhibitDetailInfo sxMfgRestrictReq(Params.MfgRestrictReqParams mfgRestrictReqParams, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxMfgRestrictReq()");

        //Step-1:call entityInhibitCheckValidity; 无需检查，检查内容已不符合constraint最新需求
        //entityInhibitMethod.entityInhibitCheckValidity(objCommon, mfgRestrictReqParams.getEntityInhibitDetailAttributes());


        //Step-2:call entityInhibitRegistrationReq;
//        Infos.EntityInhibitDetailInfo registrationResult = entityInhibitMethod.entityInhibitRegistrationReq(objCommon, mfgRestrictReqParams);
        Infos.EntityInhibitDetailInfo registrationResult = constraintMethod.constraintRegistrationReq(objCommon, mfgRestrictReqParams.getEntityInhibitDetailAttributes(), mfgRestrictReqParams.getClaimMemo());

        //Step-3:call entityInhibitEventMake;
        Inputs.EntityInhibitEventMakeParams entityInhibitEventMakeParams = new Inputs.EntityInhibitEventMakeParams();
        entityInhibitEventMakeParams.setClaimMemo(mfgRestrictReqParams.getClaimMemo());
//        entityInhibitEventMakeParams.setTransactionID(TransactionIDEnum.ENTITY_INHIBIT_REQ.getValue());
        entityInhibitEventMakeParams.setTransactionID(objCommon.getTransactionID());
        entityInhibitEventMakeParams.setEntityInhibitDetailInfo(registrationResult);
        entityInhibitEventMakeParams.setReasonCode(new ObjectIdentifier(registrationResult.getEntityInhibitDetailAttributes().getReasonCode()));
        eventMethod.entityInhibitEventMake(objCommon, entityInhibitEventMakeParams);

        log.info("【Method Exit】sxMfgRestrictReq()");

        return registrationResult;
    }

    @Override
    public Infos.MfgMMResponse sxMfgRestrictReq_110(Params.MfgRestrictReq_110Params params, Infos.ObjCommon objCommon) {
        Infos.MfgMMResponse mfgMMResponse = new Infos.MfgMMResponse();
        List<Infos.FailMfgRequest> failMfgRequestList = new ArrayList<>();
        mfgMMResponse.setFailMfgRequestList(failMfgRequestList);
        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = params.getEntityInhibitDetailAttributeList();
        for (Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes : entityInhibitDetailAttributeList){
            Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
            mfgRestrictReqParams.setClaimMemo(params.getClaimMemo());
            mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
            mfgRestrictReqParams.setUser(params.getUser());
            try {
                sxMfgRestrictReq(mfgRestrictReqParams,objCommon);
            }catch (ServiceException e){
                Infos.FailMfgRequest failMfgRequest = new Infos.FailMfgRequest();
                failMfgRequest.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
                failMfgRequest.setReasonCode(e.getCode());
                failMfgRequest.setMessageText(e.getMessage());
                failMfgRequestList.add(failMfgRequest);
            }
        }

        return mfgMMResponse;
    }

    @Override
    public void sxMfgRecipeTimeLimitSet(Params.RecipeTimeSetParams params, Infos.ObjCommon objCommon) {
        constraintMethod.recipeTimeLimitSet(params);
    }

    @Override
    public void sxMfgRecipeTimeLimitDelete(Params.RecipeTimeCancelParams params, Infos.ObjCommon objCommon) {
        constraintMethod.recipeTimeLimitDelete(params);
    }

    @Override
    public void sxMfgRecipeUseSet(Params.RecipeUseSetParams params) {
        constraintMethod.recipeTimeUseSet(params);
    }

    @Override
    public void sxMfgRecipeUseCheck(Infos.ObjCommon objCommon) {
        constraintMethod.recipeTimeUseCheck(objCommon);
    }

    @Override
    public Infos.ConstraintEqpDetailInfo sxConstraintEqpAddReq(Infos.ObjCommon objCommon, Infos.ConstraintDetailAttributes entityInhibitDetailAttributes, String claimMemo) {
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        mfgRestrictReqParams.setUser(objCommon.getUser());
        mfgRestrictReqParams.setClaimMemo(claimMemo);
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(makeEntityInhibitDetailAttributes(entityInhibitDetailAttributes));

        //add restraint
        Infos.EntityInhibitDetailInfo entityInhibitDetailInfo = sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

        Infos.ConstraintEqpDetailInfo constraintEqpDetailInfo = new Infos.ConstraintEqpDetailInfo();
        constraintEqpDetailInfo.setEntityInhibitID(entityInhibitDetailInfo.getEntityInhibitID());
        constraintEqpDetailInfo.setEntityInhibitDetailAttributes(makeConstraintEqpDetailAttributes(entityInhibitDetailInfo.getEntityInhibitDetailAttributes()));
        return constraintEqpDetailInfo;
    }

    @Override
    public Infos.ConstraintEqpDetailInfo sxConstraintEqpModifyReq(Infos.ObjCommon objCommon, Infos.ConstraintEqpDetailInfo constraintEqpDetailInfo, String claimMemo) {
        //没有modify接口，临时用此方法实现效果
        //先删除
        sxConstraintEqpCancelReq(objCommon, Collections.singletonList(constraintEqpDetailInfo), claimMemo);
        //后添加
        return sxConstraintEqpAddReq(objCommon, constraintEqpDetailInfo.getEntityInhibitDetailAttributes(), claimMemo);
    }

    @Override
    public void sxConstraintEqpCancelReq(Infos.ObjCommon objCommon, List<Infos.ConstraintEqpDetailInfo> constraintEqpDetailInfos, String claimMemo) {
        Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams = new Params.MfgRestrictCancelReqParams();

        List<Infos.EntityInhibitDetailInfo> entityInhibitDetailInfos = new ArrayList<>();
        for (Infos.ConstraintEqpDetailInfo constraintEqpDetailInfo : constraintEqpDetailInfos) {
            Infos.EntityInhibitDetailInfo entityInhibitDetailInfo = new Infos.EntityInhibitDetailInfo();
            entityInhibitDetailInfo.setEntityInhibitID(constraintEqpDetailInfo.getEntityInhibitID());
            if (null != constraintEqpDetailInfo.getEntityInhibitDetailAttributes()) {
                entityInhibitDetailInfo.setEntityInhibitDetailAttributes(makeEntityInhibitDetailAttributes(constraintEqpDetailInfo.getEntityInhibitDetailAttributes()));
            }
            entityInhibitDetailInfos.add(entityInhibitDetailInfo);
        }
        mfgRestrictCancelReqParams.setEntityInhibitions(entityInhibitDetailInfos);
        mfgRestrictCancelReqParams.setClaimMemo(claimMemo);
        sxMfgRestrictCancelReq(mfgRestrictCancelReqParams, objCommon);
    }

    @Override
    public List<Infos.EntityInhibitDetailAttributes> sxConstraintInfoImportReq(Infos.ObjCommon objCommon, MultipartFile multipartFile) {
        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributesList = new ArrayList<>();
        // 【step 1】 conversion import vo (export and import the same)
        ImportParams params = new ImportParams();
        List<MfgInfoParams> mfgInfoParamsList = new ArrayList<>();
        try {
            mfgInfoParamsList = ExcelImportUtil.importExcel(multipartFile.getInputStream(),
                    MfgInfoParams.class, params);
        } catch (Exception e) {
            log.error("ConstraintInfoImportReq ->error:{}", e.getMessage());
            Validations.check(new OmCode(retCodeConfig.getInvalidInputParam()));
        }
        entityInhibitDetailAttributesList = constraintMethod.convertMfgExcel(mfgInfoParamsList);

        return entityInhibitDetailAttributesList;
    }

    private Infos.EntityInhibitDetailAttributes makeEntityInhibitDetailAttributes(Infos.ConstraintDetailAttributes constraintDetailAttributes) {
        Infos.EntityInhibitDetailAttributes attributes = new Infos.EntityInhibitDetailAttributes();
        attributes.setEntities(constraintDetailAttributes.getEntities());
        attributes.setExceptionEntities(constraintDetailAttributes.getExceptionEntities());
        attributes.setSubLotTypes(constraintDetailAttributes.getSubLotTypes());
        attributes.setMemo(constraintDetailAttributes.getMemo());
        attributes.setFunctionRule(constraintDetailAttributes.getFunctionRule());
        attributes.setStartTimeStamp(constraintDetailAttributes.getStartTimeStamp());
        attributes.setEndTimeStamp(constraintDetailAttributes.getEndTimeStamp());
        attributes.setClaimedTimeStamp(constraintDetailAttributes.getClaimedTimeStamp());
        attributes.setReasonCode(constraintDetailAttributes.getReasonCode());
        attributes.setOwnerID(constraintDetailAttributes.getOwnerID());
        attributes.setSpecTool(true);
        return attributes;
    }

    private Infos.ConstraintDetailAttributes makeConstraintEqpDetailAttributes(Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes) {
        Infos.ConstraintDetailAttributes attributes = new Infos.ConstraintDetailAttributes();
        attributes.setEntities(entityInhibitDetailAttributes.getEntities());
        attributes.setExceptionEntities(entityInhibitDetailAttributes.getExceptionEntities());
        attributes.setSubLotTypes(entityInhibitDetailAttributes.getSubLotTypes());
        attributes.setMemo(entityInhibitDetailAttributes.getMemo());
        attributes.setFunctionRule(entityInhibitDetailAttributes.getFunctionRule());
        attributes.setStartTimeStamp(entityInhibitDetailAttributes.getStartTimeStamp());
        attributes.setEndTimeStamp(entityInhibitDetailAttributes.getEndTimeStamp());
        attributes.setClaimedTimeStamp(entityInhibitDetailAttributes.getClaimedTimeStamp());
        attributes.setOwnerID(entityInhibitDetailAttributes.getOwnerID());
        return attributes;
    }


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
//        Page<Infos.EntityInhibitDetailInfo> entityInhibitAttributesGetDR =entityInhibitMethod.entityInhibitAttributesGetDR(mfgRestrictListInqParams,objCommon);
        List<Infos.EntityInhibitDetailInfo> entityInhibitAttributesGetDR = constraintMethod.constraintAttributesGetDR(objCommon, mfgRestrictListInqParams.getEntityInhibitDetailAttributes(), mfgRestrictListInqParams.getEntityInhibitReasonDetailInfoFlag());
        log.info("【Method Exit】sxMfgRestrictListInq()");
        /*Page page = PageUtils.convertListToPage(entityInhibitAttributesGetDR,
                mfgRestrictListInqParams.getSearchCondition().getPage(),
                mfgRestrictListInqParams.getSearchCondition().getSize());*/
        Results.NewMfgRestrictListInqResult mfgRestrictListInqResult = new Results.NewMfgRestrictListInqResult();
        mfgRestrictListInqResult.setStrEntityInhibitions(new PageImpl<>(entityInhibitAttributesGetDR));
        return mfgRestrictListInqResult;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 11/27/2018 6:00 PM
     * @param params -
     * @param objCommon -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    public void sxMfgRestrictExclusionLotReq(Params.MfgRestrictExclusionLotReqParams params, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxMfgRestrictExclusionLotReq()");

        //【Step1】checking validity of entity inhibit exception lot;
        constraintMethod.constraintExceptionLotCheckValidity(params.getEntityInhibitExceptionLots(), objCommon);

        //【Step2】creating new entity inhibit exception lot;
        List<Infos.EntityInhibitDetailInfo> registrationReqRetCode = constraintMethod.constraintExceptionLotRegistrationReq(objCommon, params.getEntityInhibitExceptionLots(), params.getClaimMemo());

        //【Step3】making event record;
        for (Infos.EntityInhibitDetailInfo item : registrationReqRetCode) {
            Inputs.EntityInhibitEventMakeParams entityInhibitEventMakeParams = new Inputs.EntityInhibitEventMakeParams();
            entityInhibitEventMakeParams.setTransactionID(params.getUser().getFunctionID());
            entityInhibitEventMakeParams.setClaimMemo(params.getClaimMemo());
            entityInhibitEventMakeParams.setEntityInhibitDetailInfo(item);

            eventMethod.entityInhibitEventMake(objCommon, entityInhibitEventMakeParams);

        }

        log.info("【Method Exit】sxMfgRestrictExclusionLotReq()");
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
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 11/28/2018 4:23 PM
     * @param cancelParams -
     * @param objCommon -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    public void sxMfgRestrictExclusionLotCancelReq(Params.MfgRestrictExclusionLotReqParams cancelParams, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxMfgRestrictExclusionLotCancelReq()");

        //【Step1】cancelling exception lot requests;
        List<Infos.EntityInhibitDetailInfo> cancelReqRetCode = constraintMethod.constraintExceptionLotCancelReq(cancelParams.getEntityInhibitExceptionLots(),objCommon);


        //【Step2】making event record;
        for (Infos.EntityInhibitDetailInfo item : cancelReqRetCode) {
            Inputs.EntityInhibitEventMakeParams entityInhibitEventMakeParams = new Inputs.EntityInhibitEventMakeParams();
            entityInhibitEventMakeParams.setTransactionID(cancelParams.getUser().getFunctionID());
            entityInhibitEventMakeParams.setClaimMemo(cancelParams.getClaimMemo());
            entityInhibitEventMakeParams.setEntityInhibitDetailInfo(item);

            eventMethod.entityInhibitEventMake(objCommon, entityInhibitEventMakeParams);

        }

        log.info("【Method Exit】sxMfgRestrictExclusionLotCancelReq()");
    }
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param mfgRestrictCancelReqParams
     * @param objCommon        objCommon
     * @return com.fa.cim.dto.result.MfgRestrictListInqResult
     * @author Sun
     * @date 2018/10/11
     */

    public void sxMfgRestrictCancelReq(Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxMfgRestrictCancelReq()");

        //Step-1:call entityInhibit_CancelReq__150;
//        List<Infos.EntityInhibitDetailInfo> strMfgRestrictCancelReqOut = entityInhibitMethod.mfgRestrictCancelReq(objCommon, mfgRestrictCancelReqParams.getEntityInhibitions());
        List<Infos.EntityInhibitDetailInfo> strMfgRestrictCancelReqOut = constraintMethod.constraintCancelReq(objCommon, mfgRestrictCancelReqParams.getEntityInhibitions());
        //Step-2:call entityInhibitEvent_Make__150;

        //Step-2:call entityInhibitEvent_Make__101;
        int len = CimArrayUtils.getSize(strMfgRestrictCancelReqOut);
        for (int i = 0; i < len; i++) {
            if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.ENTITY_INHIBIT_MODIFY_REQ.getValue())){
                //modify,no need make enevt
                continue;
            }
            Inputs.EntityInhibitEventMakeParams strEntityInhibitEventMakeIn = new Inputs.EntityInhibitEventMakeParams();
//            strEntityInhibitEventMakeIn.setTransactionID(TransactionIDEnum.ENTITY_INHIBIT_CANCEL_REQ.toString());
            strEntityInhibitEventMakeIn.setTransactionID(objCommon.getTransactionID());
            strEntityInhibitEventMakeIn.setEntityInhibitDetailInfo(strMfgRestrictCancelReqOut.get(i));

            // Reason Detail Information is not stored in History table
            ObjectIdentifier reasonCode = mfgRestrictCancelReqParams.getReasonCode();
            strEntityInhibitEventMakeIn.setReasonCode(!ObjectIdentifier.isEmpty(reasonCode) ? reasonCode : new ObjectIdentifier("EXPD"));
            strEntityInhibitEventMakeIn.setClaimMemo(mfgRestrictCancelReqParams.getClaimMemo());

            eventMethod.entityInhibitEventMake(objCommon, strEntityInhibitEventMakeIn);

        }


        log.info("【Method Exit】sxMfgRestrictCancelReq()");
    }
}
