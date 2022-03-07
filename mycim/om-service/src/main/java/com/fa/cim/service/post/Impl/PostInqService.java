package com.fa.cim.service.post.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.IObjectMethod;
import com.fa.cim.method.IPostProcessMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.post.IPostInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 16:50
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class PostInqService implements IPostInqService {
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IObjectMethod objectMethod;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private IPostProcessMethod postProcessMethod;

    @Override
    public Results.CimObjectListInqResult sxCimObjectListInq(Infos.ObjCommon objCommon, Infos.CimObjectListInqInParm cimObjectListInqInParm) {
        ObjectIdentifier objectID = cimObjectListInqInParm.getObjectID();
        String className = cimObjectListInqInParm.getClassName();
        List<Infos.AdditionalSearchCondition> strAdditionalSearchConditionSeq = cimObjectListInqInParm.getStrAdditionalSearchConditionSeq();
        //----------------------------------------------------------------
        //
        //  Pre Process
        //
        //----------------------------------------------------------------
        //  -------------------------------------------------------
        //  className is mandatory
        //  -------------------------------------------------------
        Validations.check(CimStringUtils.isEmpty(className), new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "className is mandatory"));

        //  -------------------------------------------------------
        //  className should be one of followings
        //      ProductSpec/ProductGroup/Technology
        //  -------------------------------------------------------
        if (!CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSPRODUCTSPECIFICATION) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSPRODUCTGROUP) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSTECHNOLOGY) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMAINPROCESSDEFINITION) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMESSAGEDEFINITION) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMACHINERECIPE) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMODULEPROCESSDEFINITION) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSRETICLEGROUP) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMACHINE) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSCASSETTE) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSAREA) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSLOT) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSBANK) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSSTORAGEMACHINE) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSRETICLE) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSDURABLESUBSTATE) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSRETICLEPOD) &&
                !CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSCUSTOMER)) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "className is invalid"));
        }
        //  -------------------------------------------------------
        //  objectID is mandatory
        //  -------------------------------------------------------
        Validations.check(ObjectIdentifier.isEmptyWithValue(objectID), new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "objectID is mandatory"));

        //----------------------------------------------------------------------
        //
        //  Main Process
        //
        //----------------------------------------------------------------------
        //----------------------------------------------------------------------
        //  Delete filter
        //----------------------------------------------------------------------
        Inputs.ObjObjectIDListGetDRIn objObjectIDListGetDRIn = new Inputs.ObjObjectIDListGetDRIn();
        objObjectIDListGetDRIn.setClassName(className);
        objObjectIDListGetDRIn.setObjectID(objectID);
        objObjectIDListGetDRIn.setStrAdditionalSearchConditionSeq(strAdditionalSearchConditionSeq);

        List<Infos.ObjectIDList> objectOut = null;
        if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMAINPROCESSDEFINITION) ||
                CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMODULEPROCESSDEFINITION) ||
                CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION)) {
            objectOut = objectMethod.objectIDListGetForProcessDefinitionDR(objCommon, objObjectIDListGetDRIn);

        } else if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSLOT)) {
            objectOut = objectMethod.objectIDListGetForLotDR(objCommon, objObjectIDListGetDRIn);

        } else if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMACHINE) ||
                CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSSTORAGEMACHINE)) {
            objectOut = objectMethod.objectIDListGetForEquipmentDR(objCommon, objObjectIDListGetDRIn);

        } else if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSAREA) ||
                CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSBANK)) {
            objectOut = objectMethod.objectIDListGetForAreaBankDR(objCommon, objObjectIDListGetDRIn);

        } else if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSCASSETTE) ||
                CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSRETICLE) ||
                CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSRETICLEPOD)) {
            objectOut = objectMethod.objectIDListGetForDurableDR(objCommon, objObjectIDListGetDRIn);

        } else if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSDURABLESUBSTATE)) {
            objectOut = objectMethod.objectIDListGetForDurableSubStateDR(objCommon, objObjectIDListGetDRIn);

        } else {
            List<Infos.ObjectIDList> objectIDs = new ArrayList<>();
            Infos.objObjectIDListGetDR objObjectIDListGetDR = new Infos.objObjectIDListGetDR();
            objObjectIDListGetDR.setClassName(className);
            objObjectIDListGetDR.setObjectID(objectID);
            objObjectIDListGetDR.setStrAdditionalSearchConditionSeq(strAdditionalSearchConditionSeq);
            Outputs.ObjectIDList objectIDLists = objectMethod.objectIDListGetDR(objCommon, objObjectIDListGetDR);
            Outputs.ObjectIDList objectIDList = objectIDLists;

            if(!CimObjectUtils.isEmpty(objectIDList) && !CimObjectUtils.isEmpty(objectIDList.getObjectIDInformationList())){
                List<Infos.ObjectIDInformation> objectIDInformationList = objectIDList.getObjectIDInformationList();
                objectIDInformationList.forEach(x-> objectIDs.add(new Infos.ObjectIDList(x.getObjectID(), x.getDescription())));
                objectIDs.sort((x,y) -> x.getObjectID().getValue().compareTo(y.getObjectID().getValue()));
            }

            objectOut = objectIDs;
        }

        Results.CimObjectListInqResult cimObjectListInqResult = new Results.CimObjectListInqResult();
        cimObjectListInqResult.setStrObjectIDListSeq(objectOut);
        return cimObjectListInqResult;
    }

    @Override
    public Results.PostActionPageListInqResult sxPostActionListInq(Infos.ObjCommon objCommon, Params.PostActionListInqParams params) {
        Results.PostActionPageListInqResult result = new Results.PostActionPageListInqResult();
        boolean additionalInfoFlag = params.isAdditionalInfoFlag();
        String key = params.getKey();
        //------------------------------------------------------------------------------------
        // Check:
        //  If additionalInfoFlag is true, Key should be specified (for performance issue)
        //------------------------------------------------------------------------------------
        Validations.check(additionalInfoFlag && CimObjectUtils.isEmpty(key), retCodeConfigEx.getPostprocKeyRequired());

        int ppChainMode = StandardProperties.OM_PP_CHAIN_FLAG.getIntValue();
        //-------------------------------------------------------
        //  Call postProcessQueue_ListDR
        //-------------------------------------------------------
        log.info("call postProcessQueue_ListDR__100() ");
        Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn = new Inputs.PostProcessQueueListDRIn();
        postProcessQueueListDRIn.setKey(key);
        postProcessQueueListDRIn.setSeqNo(params.getSeqNo());
        postProcessQueueListDRIn.setWatchDogName(params.getWatchdogName());
        postProcessQueueListDRIn.setPostProcId(params.getPostProcID());
        postProcessQueueListDRIn.setSyncFlag(params.getSyncFlag());
        postProcessQueueListDRIn.setTxId(params.getTxID());
        postProcessQueueListDRIn.setTargetType(params.getTargetType());
        postProcessQueueListDRIn.setStrPostProcessTargetObject(params.getPostProcessTargetObject());
        postProcessQueueListDRIn.setStatus(params.getStatus());
        postProcessQueueListDRIn.setPassedTime(params.getPassedTime());
        postProcessQueueListDRIn.setClaimUserId(params.getClaimUserID());
        postProcessQueueListDRIn.setStartCreateTimeStamp(params.getStartCreateTimeStamp());
        postProcessQueueListDRIn.setEndCreateTieStamp(params.getEndCreateTimeStamp());
        postProcessQueueListDRIn.setStartUpdateTimeStamp(params.getStartUpdateTimeStamp());
        postProcessQueueListDRIn.setEndUpdateTimeStamp(params.getEndUpdateTimeStamp());
        postProcessQueueListDRIn.setMaxCount(params.getMaxCount());
        postProcessQueueListDRIn.setCommittedReadFlag(params.isAdditionalInfoFlag());
        Outputs.ObjPostProcessPageQueListDROut objPostProcessPageQueListDROut = postProcessMethod.postProcessQueueListDR(objCommon, postProcessQueueListDRIn,params.getSearchCondition());
        Page<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq = objPostProcessPageQueListDROut.getStrActionInfoSeq();
        result.setPostProcessActionInfos(strPostProcessActionInfoSeq);
        if (additionalInfoFlag) {
            log.info("Call postProcessAdditionalInfoGetDR");
            List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfos = postProcessMethod.postProcessAdditionalInfoGetDR(objCommon, key, params.getSeqNo());
            if (!CimObjectUtils.isEmpty(postProcessAdditionalInfos)) {
                List<Infos.PostProcessAdditionalInfo> strTmpPostProcessAdditionalInfoSeq = new ArrayList<>();
                //========================================================================
                // Remove addtional information that is not selected by search condition
                //========================================================================
                for (Infos.PostProcessAdditionalInfo postProcessAdditionalInfo : postProcessAdditionalInfos) {
                    for (Infos.PostProcessActionInfo postProcessActionInfo : strPostProcessActionInfoSeq) {
                        if (CimStringUtils.equals(postProcessActionInfo.getDKey(), key)) {
                            if (CimStringUtils.equals(postProcessActionInfo.getDKey(), postProcessAdditionalInfo.getDKey()) &&
                                    postProcessActionInfo.getSequenceNumber().equals(postProcessAdditionalInfo.getSequenceNumber())) {
                                strTmpPostProcessAdditionalInfoSeq.add(postProcessAdditionalInfo);
                                break;
                            }
                        }
                    }
                }
                result.setPostProcessAdditionalInfos(new PageImpl<>(strTmpPostProcessAdditionalInfoSeq,strPostProcessActionInfoSeq.getPageable(),strPostProcessActionInfoSeq.getTotalElements()));
            } else {
                log.info("No additional information for this key");
            }
        }
        //When called from TxPostActionListInq__130, get the triggerKey for each fetched records.
        else if ((1 == ppChainMode) && (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.POST_PROCESS_ACTION_LIST_INQ.getValue()))) {
            log.info("transactionID=OPOSQ003, called from TxPostActionListInq__130");
            String theKey = "";
            Page<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfosRes = result.getPostProcessAdditionalInfos();
            if (!CimObjectUtils.isEmpty(postProcessAdditionalInfosRes)) {
                for (Infos.PostProcessAdditionalInfo postProcessAdditionalInfo : postProcessAdditionalInfosRes) {
                    if (CimStringUtils.equals(theKey, postProcessAdditionalInfo.getDKey())) {
                        continue;
                    }

                    //keep the current key
                    theKey = postProcessAdditionalInfo.getDKey();
                    List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfos = postProcessMethod.postProcessAdditionalInfoGetDR(objCommon, theKey, 0);
                    List<Infos.PostProcessAdditionalInfo> strTmpPostProcessAdditionalInfoSeq = new ArrayList<>();
                    for (Infos.PostProcessAdditionalInfo processAdditionalInfo : postProcessAdditionalInfos) {
                        if (CimStringUtils.equals(processAdditionalInfo.getName(), BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY)) {
                            strTmpPostProcessAdditionalInfoSeq.add(processAdditionalInfo);
                        }
                    }
                    result.setPostProcessAdditionalInfos(new PageImpl<>(strTmpPostProcessAdditionalInfoSeq,strPostProcessActionInfoSeq.getPageable(),strPostProcessActionInfoSeq.getTotalElements()));
                }
            }
        }
        return result;
    }

    @Override
    public List<Infos.ExternalPostProcessFilterInfo> sxPostFilterListForExtInq(Infos.ObjCommon objCommon, Infos.PostFilterListForExtInqInParm parm) {

        ObjectIdentifier objectID = parm.getObjectID();
        String objectType = parm.getObjectType();
        ObjectIdentifier userID = parm.getUserID();
        log.info("in-parm objectType  {}", objectType);
        log.info("in-parm objectID    {}", objectID);
        log.info("in-parm userID      {}", userID);

        //----------------------------------------------------------------
        //  Pre Process
        //----------------------------------------------------------------
        Validations.check(ObjectIdentifier.isEmpty(objectID), retCodeConfig.getInvalidParameterWithMsg(), "objectID is mandatory");

        //----------------------------------------------------------------------
        //  Get filters
        //----------------------------------------------------------------------
        return postProcessMethod.postProcessFilterGetDR(objCommon, objectType, objectID, userID);
    }
}