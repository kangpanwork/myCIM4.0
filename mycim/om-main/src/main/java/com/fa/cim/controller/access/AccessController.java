package com.fa.cim.controller.access;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.accessManagement.IAccessController;
import com.fa.cim.dto.*;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.method.IOwnerMethod;
import com.fa.cim.method.IPersonMethod;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.bo.CimBO;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/7/30 13:41
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IAccessController.class, confirmableKey = "AccessConfirm", cancellableKey = "AccessCancel")
@RequestMapping("/access")
@Listenable
public class AccessController implements IAccessController {

    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private com.fa.cim.service.access.IAccessInqService accessInqService;

    @Autowired
    private IOwnerMethod ownerMethod;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private IAccessService accessService;
    @Autowired
    private IPersonMethod personMethod;
    @Autowired
    private IEventMethod eventMethod;

    @ResponseBody
    @RequestMapping(value = "/owner_change/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.OWNER_CHANGE_REQ)
    @Override
    public Response ownerChangeReq(@RequestBody Params.OwnerChangeReqParams params) {

        ObjectIdentifier fromOwnerID = params.getStrOwnerChangeReqInParm().getFromOwnerID();
        ObjectIdentifier toOwnerID = params.getStrOwnerChangeReqInParm().getToOwnerID();
        String targetClassName = params.getStrOwnerChangeReqInParm().getTargetClassName();
        String targetTableName = params.getStrOwnerChangeReqInParm().getTargetTableName();
        String targetColumnName = params.getStrOwnerChangeReqInParm().getTargetColumnName();
        List<Infos.HashedInfo> strHashedInfoSeq = params.getStrOwnerChangeReqInParm().getStrHashedInfoSeq();

        // Pre Process
        //【step0】init params
        final TransactionIDEnum transactionId = TransactionIDEnum.OWNER_CHANGE_REQ;
        User user = params.getUser();

        //【step1】calendar_GetCurrentTimeDR
        log.debug("【Step-1】txCalendar_GetCurrentTimeDR(...)");
        //【step2】 txAccessControlCheckInq
        log.debug("【Step-2】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId.getValue(), user, accessControlCheckInqParams);

        //【step3】 - person_existence_Check
        log.debug("【Step-3】person_existence_Check(...)");
        String personExistenceCheckResult = personMethod.personExistenceCheck(objCommon, toOwnerID);
        //【step4】 - ownerChange_definition_GetDR
        log.debug("【Step-4】ownerChange_definition_GetDR(...)");
        Outputs.ObjOwnerChangeDefinitionGetDROut ownerChangeDefinitionGetDrResult = ownerMethod.ownerChangeDefinitionGetDR(objCommon);
        List<Infos.OwnerChangeDefinition> strOwnerChangeDefinitionSeq = ownerChangeDefinitionGetDrResult.getStrOwnerChangeDefinitionSeq();
        List<Infos.OwnerChangeDefObjDefinition> strOwnerChangeDefObjDefinitionSeq = ownerChangeDefinitionGetDrResult.getStrOwnerChangeDefObjDefinitionSeq();

        Infos.OwnerChangeReqInParm aOwnerChangeReqInParm = params.getStrOwnerChangeReqInParm();
        aOwnerChangeReqInParm.getToOwnerID().setValue(personExistenceCheckResult);
        //---------------------------------------------
        // Check input parameter
        //---------------------------------------------
        // Check targetClassName
        if (CimStringUtils.isNotEmpty(targetClassName)) {
            Boolean existFlag = false;
            for (int index = 0; index < CimArrayUtils.getSize(ownerChangeDefinitionGetDrResult.getAllClassNames()); index++) {
                if (CimStringUtils.equals(targetClassName, ownerChangeDefinitionGetDrResult.getAllClassNames().get(index))) {
                    existFlag = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(existFlag)) {
                log.error("Invalid targetClassName Parameter,{}", targetClassName);
                return Response.createError(retCodeConfig.getInvalidParameterWithMsg(), transactionId.getValue(), "Input class name is invalid.");
            }
        }
        // Check targetTableName
        if (CimStringUtils.isNotEmpty(targetTableName)) {
            Boolean existFlag = false;
            for (int index = 0; index < CimArrayUtils.getSize(ownerChangeDefinitionGetDrResult.getAllTableNames()); index++) {
                if (CimStringUtils.equals(targetTableName, ownerChangeDefinitionGetDrResult.getAllTableNames().get(index))) {
                    existFlag = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(existFlag)) {
                log.error("Invalid targetTableName Parameter,{}", targetTableName);
                return Response.createError(retCodeConfig.getInvalidParameterWithMsg(), transactionId.getValue(), "Input table name is invalid.");
            }
        }
        // Check targetTableName and targetColumnName
        if (CimStringUtils.isNotEmpty(targetTableName) && CimStringUtils.isNotEmpty(targetColumnName)) {
            String keyString = new String(targetTableName);
            keyString += BizConstant.SP_KEY_SEPARATOR_DOT;
            keyString += targetColumnName;
            Boolean existFlag = false;
            for (int index = 0; index < CimArrayUtils.getSize(ownerChangeDefinitionGetDrResult.getAllTableAndColumNames()); index++) {
                if (CimStringUtils.equals(keyString, ownerChangeDefinitionGetDrResult.getAllTableAndColumNames().get(index))) {
                    existFlag = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(existFlag)) {
                log.error("Invalid targetTableName and targetColumnName Parameter,{}", keyString);
                return Response.createError(retCodeConfig.getInvalidParameterWithMsg(), transactionId.getValue(), "Input table name and column name is mismatch.");
            }
        }
        // Check targetClassName and targetTableName
        if (CimStringUtils.isNotEmpty(targetClassName) && CimStringUtils.isNotEmpty(targetTableName)) {
            String keyString = new String(targetClassName);
            keyString += BizConstant.SP_KEY_SEPARATOR_DOT;
            keyString += targetTableName;
            Boolean existFlag = false;
            for (int index = 0; index < CimArrayUtils.getSize(ownerChangeDefinitionGetDrResult.getAllClassAndTableNames()); index++) {
                if (CimStringUtils.equals(keyString, ownerChangeDefinitionGetDrResult.getAllClassAndTableNames().get(index))) {
                    existFlag = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(existFlag)) {
                log.error("Invalid targetClassName and targetTableName Parameter,{}", keyString);
                return Response.createError(retCodeConfig.getInvalidParameterWithMsg(), transactionId.getValue(), "Input class name and table name is mismatch.");
            }
        }
        if (CimStringUtils.isNotEmpty(targetColumnName) && CimStringUtils.isEmpty(targetTableName)) {
            log.error("Table name is not specified");
            return Response.createError(retCodeConfig.getInvalidParameterWithMsg(), transactionId.getValue(), "Table name is not specified. It is needed when column name is specified.");
        }
        if (CimArrayUtils.getSize(strHashedInfoSeq) != 0 && CimStringUtils.isEmpty(targetClassName)) {
            log.error("Class name is not specified");
            return Response.createError(retCodeConfig.getInvalidParameterWithMsg(), transactionId.getValue(), "Class name is not specified. It is needed when hashed info is specified.");
        }
        int failExtendLen = 50;
        int extendLen = 1000;
        int failLen = failExtendLen;
        int histLen = extendLen;
        int failCnt = 0;
        int histCnt = 0;
        List<Infos.OwnerChangeErrorInfo> strOwnerChangeErrorInfoSeq = new ArrayList<>();
        List<Infos.OwnerChangeObject> strOwnerChangeObjectSeq = new ArrayList<>();
        //=========================================================================
        // Update FW Object
        //=========================================================================
        int commitMax = StandardProperties.OM_OWNER_CHG_MAX_COMMIT_COUNT.getIntValue();
        int commitCnt = 0;
        Boolean commitMaxOverFlag = false;
        for (int i = 0; i < CimArrayUtils.getSize(strOwnerChangeDefinitionSeq); i++) {
            Infos.OwnerChangeDefinition strOwnerChangeDefinition = strOwnerChangeDefinitionSeq.get(i);
            if (CimStringUtils.isNotEmpty(targetClassName) && !CimStringUtils.equals(targetClassName, strOwnerChangeDefinition.getClassName())) {
                continue;
            }
            //=========================================================================
            // Get object reference list
            //=========================================================================
            //【step5】 - ownerChange_objectList_Get
            log.debug("【step5】ownerChange_objectList_Get(...)");
            Inputs.ObjOwnerChangeObjectListGetIn in = new Inputs.ObjOwnerChangeObjectListGetIn();
            in.setOwnerID(fromOwnerID);
            in.setTargetClassName(targetClassName);
            in.setTargetTableName(targetTableName);
            in.setTargetColumnName(targetColumnName);
            in.setStrHashedInfoSeq(strHashedInfoSeq);
            in.setStrOwnerChangeDefinition(strOwnerChangeDefinition);
            List<CimBO> ownerChangeObjectListGetResult = ownerMethod.ownerChangeObjectListGet(objCommon, in);
            List<CimBO> aBoSeq = ownerChangeObjectListGetResult;

            //=========================================================================
            // Update Owner information
            //=========================================================================
            for (int j = 0; j < CimArrayUtils.getSize(aBoSeq); j++) {
                //---------------------------------------------
                // Update Object Owner information
                //---------------------------------------------
                List<Infos.OwnerChangeObject> aOwnerChangeObjectSeq = new ArrayList<>();
                //【step6】 - txOwnerChangeReq
                log.debug("【step6】 txOwnerChangeReq(...)");
                Infos.OwnerChangeDefObjDefinition dummyOwnerChangeDefObjDefinition = new Infos.OwnerChangeDefObjDefinition();
                Results.OwnerChangeReqResult ownerChangeReqResult = null;
                try {
                    ownerChangeReqResult = accessService.sxOwnerChangeReq(aOwnerChangeObjectSeq,
                            objCommon,
                            aOwnerChangeReqInParm,
                            strOwnerChangeDefinition,
                            dummyOwnerChangeDefObjDefinition,
                            params.getClaimMemo(),
                            aBoSeq.get(j));
                } catch (ServiceException e) {
                    List<Infos.OwnerChangeErrorInfo> ownerChangeErrorInfoList = e.getData(List.class);
                    //TX_ROLLBACK ( txOwnerChangeReq );
                    if (CimArrayUtils.getSize(ownerChangeErrorInfoList) > 0) {
                        strOwnerChangeErrorInfoSeq.add(failCnt, ownerChangeErrorInfoList.get(0));
                        failCnt++;
                    }
                    continue;
                }
                if (commitMax > 0 && (commitCnt + 1) > commitMax) {
                    //TX_ROLLBACK ( txOwnerChangeReq );
                    commitMaxOverFlag = true;
                    break;
                }
                //TX_COMMIT( txOwnerChangeReq );
                commitCnt++;
                if (CimArrayUtils.getSize(aOwnerChangeObjectSeq) > 0) {
                    strOwnerChangeObjectSeq.add(histCnt, aOwnerChangeObjectSeq.get(0));
                    histCnt++;
                }
            }
            if (CimBooleanUtils.isTrue(commitMaxOverFlag)) {
                break;
            }
        }
        //=========================================================================
        // Update user definition table
        //=========================================================================
        if (CimBooleanUtils.isFalse(commitMaxOverFlag)) {
            Boolean beginFlag = false;
            for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeDefObjDefinitionSeq); k++) {
                Infos.OwnerChangeDefObjDefinition strOwnerChangeDefObjDefinition = strOwnerChangeDefObjDefinitionSeq.get(k);
                if (CimStringUtils.isNotEmpty(targetTableName) && !CimStringUtils.equals(targetTableName, strOwnerChangeDefObjDefinition.getTableName())) {
                    continue;
                }
                if (CimStringUtils.isNotEmpty(targetColumnName) && !CimStringUtils.equals(targetColumnName, strOwnerChangeDefObjDefinition.getColumnName())) {
                    continue;
                }
                //---------------------------------------------
                // Update outside of FW table Owner information
                //---------------------------------------------
                List<Infos.OwnerChangeObject> aOwnerChangeObjectSeq = new ArrayList<>();
                Infos.OwnerChangeDefinition dummyOwnerChangeDefinition = new Infos.OwnerChangeDefinition();
                if (CimBooleanUtils.isFalse(beginFlag)) {
                    beginFlag = true;
                }
                //【step7】 - txOwnerChangeReq
                log.debug("【step7】 txOwnerChangeReq(...)");
                Results.OwnerChangeReqResult ownerChangeReqResult1 = null;
                try {
                    ownerChangeReqResult1 = accessService.sxOwnerChangeReq(aOwnerChangeObjectSeq,
                            objCommon,
                            aOwnerChangeReqInParm,
                            dummyOwnerChangeDefinition,
                            strOwnerChangeDefObjDefinition,
                            params.getClaimMemo(),
                            null);
                } catch (ServiceException e) {
                    beginFlag = false;
                    List<Infos.OwnerChangeErrorInfo> ownerChangeErrorInfoList = e.getData(List.class);
                    if (CimArrayUtils.getSize(ownerChangeErrorInfoList) > 0) {
                        strOwnerChangeErrorInfoSeq.add(failCnt, ownerChangeErrorInfoList.get(0));
                        failCnt++;
                    }
                    continue;
                }

                if (strOwnerChangeDefObjDefinition.getCommitFlag() == 1) {
                    // Data was updated
                    if (CimArrayUtils.getSize(aOwnerChangeObjectSeq) > 0) {
                        if (commitMax > 0 && (commitCnt + 1) > commitMax) {
                            //TX_ROLLBACK ( txOwnerChangeReq );
                            beginFlag = false;
                            commitMaxOverFlag = true;
                            break;
                        }
                        //TX_COMMIT( txOwnerChangeReq );
                        commitCnt++;
                        beginFlag = false;
                    }
                    // Data was not updated <- Commit count does not update
                    else {
                        //TX_COMMIT( txOwnerChangeReq );
                        beginFlag = true;
                    }
                }
                for (int m = 0; m < CimArrayUtils.getSize(aOwnerChangeObjectSeq); m++) {
                    Boolean existFlag = false;
                    for (int n = 0; n < CimArrayUtils.getSize(strOwnerChangeObjectSeq); n++) {
                        if (CimStringUtils.equals(aOwnerChangeObjectSeq.get(m).getObjectName(), strOwnerChangeObjectSeq.get(n).getObjectName())
                                && CimStringUtils.equals(aOwnerChangeObjectSeq.get(m).getHashedInfo(), strOwnerChangeObjectSeq.get(n).getHashedInfo())) {
                            existFlag = true;
                            break;
                        }
                    }// loop of m
                    if (CimBooleanUtils.isFalse(existFlag)) {
                        if (histCnt > histLen) {
                            histLen = extendLen;
                        }
                        strOwnerChangeObjectSeq.add(histCnt, aOwnerChangeObjectSeq.get(m));
                        histCnt++;
                    }
                }
            }
            if (CimBooleanUtils.isTrue(beginFlag)) {
                //TX_COMMIT( txOwnerChangeReq );
                commitCnt++;
            }
        }
        if (CimArrayUtils.getSize(strOwnerChangeObjectSeq) > 0) {
            //=========================================================================
            // Make history event
            //=========================================================================
            //【step8】 - ownerChangeEvent_Make
            log.debug("【step8】 ownerChangeEvent_Make(...)");
            Inputs.ObjOwnerChangeEventMakeIn input = new Inputs.ObjOwnerChangeEventMakeIn();
            input.setFromOwnerID(fromOwnerID);
            input.setToOwnerID(toOwnerID);
            input.setStrOwnerChangeObjectSeq(strOwnerChangeObjectSeq);
            input.setClaimMemo(params.getClaimMemo());
            try {
                eventMethod.ownerChangeEventMake(objCommon, input);
            } catch (ServiceException e) {
                if (CimBooleanUtils.isTrue(commitMaxOverFlag)) {
                    return Response.createError(new OmCode(retCodeConfigEx.getCommitMaxCountOver()), transactionId.getValue());
                } else {
                    return Response.create(e);
                }
            }
        }
        if (CimBooleanUtils.isTrue(commitMaxOverFlag)) {
            return Response.create(retCodeConfigEx.getCommitMaxCountOver().getCode(), transactionId.getValue(), retCodeConfigEx.getCommitMaxCountOver().getMessage(), strOwnerChangeErrorInfoSeq);
        } else if (failCnt == 0) {
            return Response.create(retCodeConfig.getSucc().getCode(), transactionId.getValue(), null, strOwnerChangeErrorInfoSeq);
        } else {
            return Response.create(retCodeConfigEx.getSomeDataFailed().getCode(), transactionId.getValue(), retCodeConfigEx.getSomeDataFailed().getMessage(), strOwnerChangeErrorInfoSeq);
        }
    }
}