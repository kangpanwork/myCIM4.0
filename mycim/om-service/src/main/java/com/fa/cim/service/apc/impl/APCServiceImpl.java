package com.fa.cim.service.apc.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.IAPCMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.service.apc.IAPCInqService;
import com.fa.cim.service.apc.IAPCService;
import com.fa.cim.service.constraint.IConstraintInqService;
import com.fa.cim.service.constraint.IConstraintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 13:05
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class APCServiceImpl implements IAPCService {

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IAPCMethod apcMethod;

    @Autowired
    private IConstraintInqService constraintInqService;

    @Autowired
    private IConstraintService constraintService;

    @Autowired
    private IAPCInqService apcInqService;

    public void sxAPCInterfaceOpsReq(Infos.ObjCommon objCommon, String operation, Infos.APCIf apcIf){
        //----------------------
        //   check operation
        //----------------------
        if (!CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_APPROVEX)
                && !CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_APPROVE)
                && !CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_REJECT)
                && !CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_NEW)
                && !CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_UPDATE)
                && !CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_DELETE)){
            throw new ServiceException(retCodeConfigEx.getApcInvalidBuildTimeOperation());
        }
        Infos.APCIf tmpAPCIF = apcIf;
        if (tmpAPCIF == null){
            tmpAPCIF = new Infos.APCIf();
        }
        if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_APPROVEX)){
            tmpAPCIF.setApprovedTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            tmpAPCIF.setRegisteredTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        }
        if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_APPROVE)){
            tmpAPCIF.setApprovedTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        }
        if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_NEW)){
            tmpAPCIF.setRegisteredTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            tmpAPCIF.setApprovedUserID(new ObjectIdentifier(""));
            tmpAPCIF.setApprovedTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
        }
        //-------------------------------------------
        // Call APCIF_point_InsertDR
        // when operation is "new" or "approvex"
        //-------------------------------------------
        if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_NEW)
                || CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_APPROVEX)){
            apcMethod.APCIFPointInsertDR(objCommon, operation, tmpAPCIF);
        }
        //-----------------------------------------------------
        // Call APCIF_point_UpdateDR
        // when operaion is "update" or "approve" or "reject"
        //-----------------------------------------------------
        if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_UPDATE)
                || CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_APPROVE)
                || CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_REJECT)){
            apcMethod.APCIFPointUpdateDR(objCommon, operation, tmpAPCIF);
        }
        //-------------------------------------------
        // Call APCIF_point_DeleteDR
        // when operation is "delete"
        //-------------------------------------------
        if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_DELETE)){
            apcMethod.APCIFPointDeleteDR(objCommon, operation, tmpAPCIF);
        }

        //---------------------------------------------
        // Entity Inhibit
        // when operation is "new" or "reject".
        //---------------------------------------------
        if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_NEW)
                || CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_REJECT)){
            //--------------------------------
            // Inhibit Enable Check
            // OM_CONSTRAINT_APC_INTERFACE_MODIFY
            // 0 --> Inhibit disable
            // 1 --> inhibit enable
            //--------------------------------

            int APCInhibitFlag = StandardProperties.OM_CONSTRAINT_APC_INTERFACE_MODIFY.getIntValue();
            log.info("Entity Inhibit Check 0(disable)/1(enable) --> {}", APCInhibitFlag);
            if (APCInhibitFlag == 1){
                User requestUserID = new User();
                Infos.EntityInhibitDetailAttributes entityInhibitions = new Infos.EntityInhibitDetailAttributes();
                requestUserID.setUserID(objCommon.getUser().getUserID());
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                entityInhibitions.setEntities(entities);
                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                entities.add(entityIdentifier);
                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                entityIdentifier.setObjectID(tmpAPCIF.getEquipmentID());
                entityIdentifier.setAttribution(tmpAPCIF.getAPCSystemName());
                entityInhibitions.setStartTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                entityInhibitions.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                entityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCBUILDTIME);
                entityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                entityInhibitions.setClaimedTimeStamp("");
                String claimMemo = "APC Bulid Time Registration.";
                Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                mfgRestrictReqParams.setClaimMemo(claimMemo);
                mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitions);
                mfgRestrictReqParams.setUser(requestUserID);
                try {
//                    constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                    //Step5 - txMfgRestrictReq__110
                    Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                    List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                    entityInhibitDetailAttributeList.add(entityInhibitions);
                    mfgRestrictReq_110Params.setClaimMemo(claimMemo);
                    mfgRestrictReq_110Params.setUser(objCommon.getUser());
                    mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                    constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                } catch (ServiceException e){
                    if (!Validations.isEquals(retCodeConfig.getDuplicateInhibit(), e.getCode())){
                        throw e;
                    }
                }
            }
        }
        //---------------------------------------------
        // Entity Inhibit Cancel
        // when operation is "approve" or "delete".
        //---------------------------------------------
        if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_APPROVE)
                || CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_DELETE)){
            List<Infos.APCIf> apcIfList = apcInqService.sxAPCInterfaceListInq(objCommon, tmpAPCIF.getEquipmentID());
            boolean inhibitCancelFlag = true;
            int count = CimArrayUtils.getSize(apcIfList);
            for (int i = 0; i < count; i++){
                Infos.APCIf apcIf1 = apcIfList.get(i);
                if (apcIf != null && !CimStringUtils.equals(apcIf1.getAPCSystemName(), apcIf.getAPCSystemName())
                        && !CimStringUtils.equals(apcIf1.getAPCConfigStatus(), BizConstant.SP_APC_CONFIG_STATE_APPROVED)) {
                    inhibitCancelFlag = false;
                    break;
                }
            }
            /*----------------------------*/
            /*   Make Inhibit Data List   */
            /*----------------------------*/
            log.info("Make Inhibit Data List");
            if (inhibitCancelFlag){
                log.info("inhibitCancelFlag == TRUE");
                Infos.EntityInhibitDetailAttributes searchCondition = new Infos.EntityInhibitDetailAttributes();
                /*----------------------*/
                /*   Get Inhibit List   */
                /*----------------------*/
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                searchCondition.setEntities(entities);
                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                entities.add(entityIdentifier);
                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                entityIdentifier.setAttribution("");
                log.info("call txEntityInhibitListInq");
                boolean entityInhibitReasonDetailInfoFlag = false;
                Params.MfgRestrictListInqParams mfgRestrictListInqParams = new Params.MfgRestrictListInqParams();
                SearchCondition condition = new SearchCondition();
                condition.setPage(0);
                condition.setSize(999);
                mfgRestrictListInqParams.setSearchCondition(condition);
                mfgRestrictListInqParams.setEntityInhibitDetailAttributes(searchCondition);
                mfgRestrictListInqParams.setEntityInhibitReasonDetailInfoFlag(entityInhibitReasonDetailInfoFlag);
                Results.NewMfgRestrictListInqResult newMfgRestrictListInqResult = constraintInqService.sxMfgRestrictListInq(mfgRestrictListInqParams, objCommon);
                List<Infos.EntityInhibitDetailInfo> strEntityInhibitions = newMfgRestrictListInqResult.getStrEntityInhibitions().getContent();
                int inhibitCnt = 0;
                List<Infos.EntityInhibitDetailInfo> expiredEntityInhibitions = new ArrayList<>();
                count = CimArrayUtils.getSize(strEntityInhibitions);
                for (int i = 0; i < count; i++){
                    /*----------------------------------*/
                    /*   Set expiredEntityInhibitions   */
                    /*----------------------------------*/
                    Infos.EntityInhibitDetailInfo entityInhibitDetailInfo = strEntityInhibitions.get(i);
                    if (CimStringUtils.equals(entityInhibitDetailInfo.getEntityInhibitDetailAttributes().getReasonCode(), BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCBUILDTIME)){
                        List<Infos.EntityIdentifier> tmpEntities = entityInhibitDetailInfo.getEntityInhibitDetailAttributes().getEntities();
                        for (int j = 0; j < tmpEntities.size(); j++){
                            if (ObjectIdentifier.equalsWithValue(entities.get(j).getObjectID(), tmpAPCIF.getEquipmentID())){
                                expiredEntityInhibitions.add(entityInhibitDetailInfo);
                            }
                        }
                    }
                }
                if (CimArrayUtils.getSize(expiredEntityInhibitions) > 0){
                    ObjectIdentifier reason = new ObjectIdentifier(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCBUILDTIME_END);
                    String claimMemo = "APC Build Time Approval.";
                    log.info("call txEntityInhibitCancelReq");
                    Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams = new Params.MfgRestrictCancelReqParams();
                    mfgRestrictCancelReqParams.setEntityInhibitions(expiredEntityInhibitions);
                    mfgRestrictCancelReqParams.setClaimMemo(claimMemo);
                    mfgRestrictCancelReqParams.setReasonCode(reason);
                    constraintService.sxMfgRestrictCancelReq(mfgRestrictCancelReqParams, objCommon);
                }
            }
        }
    }
}
