package com.fa.cim.service.cda.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.method.IObjectLockMethod;
import com.fa.cim.method.IObjectMethod;
import com.fa.cim.newcore.bo.CimBO;
import com.fa.cim.service.cda.ICustomerDefinedAttributeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * description:
 * <p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 18:37
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class CustomerDefinedAttributeServiceImpl implements ICustomerDefinedAttributeService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Override
    public List<Results.UserDataUpdateResult> sxCDAValueUpdateReq(Infos.ObjCommon objCommon, Params.CDAValueUpdateReqParams params) {
        List<Results.UserDataUpdateResult> userDataUpdateResultList = new ArrayList<>();

        String actionCode = params.getStrCDAValueUpdateReqInParm().getActionCode();
        if (!CimStringUtils.equals(BizConstant.SP_USERDATAUPDATEREQ_ACTIONCODE_REGIST, actionCode)
                && !CimStringUtils.equals(BizConstant.SP_USERDATAUPDATERESULT_ACTIONCODE_DELETE, actionCode)) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterWithMsg(), params.getStrCDAValueUpdateReqInParm().getActionCode()));

        }

        Inputs.ObjObjectGetIn objObjectGetIn = new Inputs.ObjObjectGetIn();
        objObjectGetIn.setStringifiedObjectReference(params.getStrCDAValueUpdateReqInParm().getStringifiedObjectReference());
        objObjectGetIn.setClassName(params.getStrCDAValueUpdateReqInParm().getClassName());
        objObjectGetIn.setStrHashedInfoSeq(params.getStrCDAValueUpdateReqInParm().getStrHashedInfoSeq());

        //step1 - object_Get
        CimBO object = objectMethod.objectGet(objCommon, objObjectGetIn);
        ObjectIdentifier objectID = new ObjectIdentifier("", object.getPrimaryKey());
        //step1 - object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(objectID);
        objLockModeIn.setClassName(params.getStrCDAValueUpdateReqInParm().getClassName());
        objLockModeIn.setFunctionCategory(TransactionIDEnum.USER_DATA_UPDATE_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(true);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        String className = objLockModeOut.getClassName();
        objectID = objLockModeOut.getObjectID();

        int udataLen = params.getStrCDAValueUpdateReqInParm().getStrUserDataSeq().size();

        List<Infos.UserDataAction> strUserDataActionSeq = new ArrayList<>();
        List<Infos.UserData> strUserDataSeq = new ArrayList<>();
        List<String> keySeq = new ArrayList<>();

        for (int udataIdx = 0; udataIdx < udataLen; udataIdx++) {
            Infos.UserData userData = params.getStrCDAValueUpdateReqInParm().getStrUserDataSeq().get(udataIdx);
            if (CimStringUtils.isEmpty(userData.getName())) {
                throw new ServiceException(retCodeConfigEx.getBlankInputParameter());

            }
            if (CimStringUtils.isEmpty(userData.getOriginator())) {
                throw new ServiceException(retCodeConfigEx.getBlankInputParameter());

            }

            if (CimStringUtils.equals(BizConstant.SP_USERDATA_ORIG_SM, userData.getOriginator())) {
                if (CimStringUtils.equals(BizConstant.SP_USERDATAUPDATEREQ_ACTIONCODE_REGIST, actionCode)) {
                    throw new ServiceException(retCodeConfig.getCanNotRegisteredMDSData());

                } else if (CimStringUtils.equals(BizConstant.SP_USERDATAUPDATEREQ_ACTIONCODE_DELETE, actionCode)) {
                    userDataUpdateResultList.clear();
                    throw new ServiceException(retCodeConfig.getCanNotDeleteMDSData());

                }
            }
            Results.UserDataUpdateResult userDataUpdateResult = new Results.UserDataUpdateResult();
            userDataUpdateResultList.add(userDataUpdateResult);
            userDataUpdateResult.setName(userData.getName());
            userDataUpdateResult.setOriginator(userData.getOriginator());
            userDataUpdateResult.setActionCode("");

            Infos.UserDataAction userDataAction = new Infos.UserDataAction();
            strUserDataActionSeq.add(userDataAction);
            userDataAction.setUserDataName(userData.getName());
            userDataAction.setOriginator(userData.getOriginator());
            userDataAction.setActionCode("");
            userDataAction.setFromType("");
            userDataAction.setFromValue("");
            userDataAction.setToType("");
            userDataAction.setToValue("");

            Infos.CDAValueInqInParm strCDAValueInqInParm = new Infos.CDAValueInqInParm();
            strCDAValueInqInParm.setUserDataName(userData.getName());
            strCDAValueInqInParm.setUserDataOriginator(userData.getOriginator());

            //step3 - object_userData_Get__101
            List<Infos.UserData> objectUserData;
            try {
                objectUserData = objectMethod.objectUserDataGet(objCommon, strCDAValueInqInParm, object);
                keySeq.add(params.getStrCDAValueUpdateReqInParm().getStrUserDataSeq().get(udataIdx).getName());
                Boolean existSMFlag = false;
                List<Infos.UserData> userDataList = objectUserData;
                if (CimArrayUtils.isNotEmpty(userDataList)) {
                    for (Infos.UserData tmpUserData : userDataList) {
                        if (CimStringUtils.equals(BizConstant.SP_USERDATA_ORIG_SM, tmpUserData.getOriginator())) {
                            existSMFlag = true;
                            break;
                        }
                    }
                }
                if (CimStringUtils.equals(BizConstant.SP_USERDATAUPDATEREQ_ACTIONCODE_REGIST, actionCode)) {
                    if (CimBooleanUtils.isTrue(existSMFlag)) {
                        userDataUpdateResultList.clear();
                        throw new ServiceException(retCodeConfig.getCanNotModMDSData());

                    }
                    userDataAction.setActionCode(BizConstant.SP_USERDATAUPDATERESULT_ACTIONCODE_UPDATE);
                    userDataUpdateResult.setActionCode(BizConstant.SP_USERDATAUPDATERESULT_ACTIONCODE_UPDATE);
                } else if (CimStringUtils.equals(BizConstant.SP_USERDATAUPDATEREQ_ACTIONCODE_DELETE, actionCode)) {
                    if (CimBooleanUtils.isTrue(existSMFlag)) {
                        userDataUpdateResultList.clear();
                        throw new ServiceException(retCodeConfig.getCanNotDeleteMDSData());

                    }
                    userDataUpdateResult.setActionCode(BizConstant.SP_USERDATAUPDATERESULT_ACTIONCODE_DELETE);
                    userDataAction.setActionCode(BizConstant.SP_USERDATAUPDATERESULT_ACTIONCODE_DELETE);
                }
                userDataAction.setFromValue(objectUserData.get(0).getValue());
                userDataAction.setFromType(objectUserData.get(0).getType());
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundUData(), e.getCode())) {
                    if (CimStringUtils.equals(BizConstant.SP_USERDATAUPDATEREQ_ACTIONCODE_REGIST, actionCode)) {
                        userDataUpdateResult.setActionCode(BizConstant.SP_USERDATAUPDATERESULT_ACTIONCODE_INSERT);
                        userDataAction.setActionCode(BizConstant.SP_USERDATAUPDATERESULT_ACTIONCODE_INSERT);
                    } else if (CimStringUtils.equals(BizConstant.SP_USERDATAUPDATEREQ_ACTIONCODE_DELETE, actionCode)) {
                        userDataUpdateResult.setActionCode(BizConstant.SP_USERDATAUPDATERESULT_ACTIONCODE_SKIP);
                        continue;
                    }
                } else {
                    userDataUpdateResultList.clear();
                    throw e;
                }
            }
            strUserDataSeq.add(params.getStrCDAValueUpdateReqInParm().getStrUserDataSeq().get(udataIdx));
            if (CimStringUtils.equals(BizConstant.SP_USERDATAUPDATEREQ_ACTIONCODE_REGIST, actionCode)) {
                userDataAction.setToValue(params.getStrCDAValueUpdateReqInParm().getStrUserDataSeq().get(udataIdx).getValue());
                userDataAction.setToType(params.getStrCDAValueUpdateReqInParm().getStrUserDataSeq().get(udataIdx).getType());
            } else if (CimStringUtils.equals(BizConstant.SP_USERDATAUPDATEREQ_ACTIONCODE_DELETE, actionCode)) {
                //anEventCount++;
            }
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Object Lock Process                                                 */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        if (!Objects.equals(lockMode, BizConstant.SP_EQP_LOCK_MODE_WRITE)
                && objLockModeOut.getRequiredLockForMainObject() != BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE) {
            if (!CimArrayUtils.isEmpty(keySeq)) {
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(objectID,
                        BaseStaticMethod.convertClassNameToOmsClassName(className),
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_USERDEFINEDDATA,
                        (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, keySeq));
            }
        } else {
            /*--------------------------------*/
            /*   Lock objects to be updated   */
            /*--------------------------------*/
            objectLockMethod.objectLock(objCommon, CimObjectUtils.cast(object.getClass().getInterfaces()[0]), objectID);
        }
        if (CimArrayUtils.isNotEmpty(strUserDataSeq)) {
            for (Infos.UserData userData : strUserDataSeq) {
                if (CimStringUtils.equals(BizConstant.SP_USERDATAUPDATEREQ_ACTIONCODE_REGIST, actionCode)) {

                    //step6 - object_userData_Set
                    try {
                        objectMethod.objectUserDataSet(objCommon, userData, object);
                    } catch (ServiceException e) {
                        userDataUpdateResultList.clear();
                        throw e;
                    }
                } else if (CimStringUtils.equals(BizConstant.SP_USERDATAUPDATEREQ_ACTIONCODE_DELETE, actionCode)) {

                    //step7 - object_userData_Remove
                    try {
                        objectMethod.objectUserDataRemove(objCommon, userData, object);
                    } catch (ServiceException e) {
                        userDataUpdateResultList.clear();
                        throw e;
                    }
                }
            }
        }
        if (CimArrayUtils.isNotEmpty(strUserDataActionSeq)) {
            String aClassName = params.getStrCDAValueUpdateReqInParm().getClassName();
            List<Infos.HashedInfo> strHashedInfoSeq = params.getStrCDAValueUpdateReqInParm().getStrHashedInfoSeq();

            if (params.getStrCDAValueUpdateReqInParm().getStringifiedObjectReference() != null && params.getStrCDAValueUpdateReqInParm().getStringifiedObjectReference().length() > 0) {

                //step8 -  object_classIDInfo_GetDR // Get class ID information
                Outputs.ObjectClassIDInfoGetDROut getClassIDInfos = objectMethod.ObjectClassIDInfoGetDR(objCommon, object);

                aClassName = getClassIDInfos.getClassName();
                strHashedInfoSeq = getClassIDInfos.getStrHashedInfoSeq();
            }

            //step9 - object_userData_ChangeEvent_Make
            eventMethod.objectUserDataChangeEventMake(objCommon, params.getStrCDAValueUpdateReqInParm().getStringifiedObjectReference(), aClassName, strHashedInfoSeq, strUserDataActionSeq, "");
        }
        return userDataUpdateResultList;
    }
}
