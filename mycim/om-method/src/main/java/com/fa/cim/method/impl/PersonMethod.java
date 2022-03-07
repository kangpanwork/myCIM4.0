package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.EnvConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.nonruntime.CimUserDefinedAttributeInfoDO;
import com.fa.cim.entity.runtime.person.CimPersonDO;
import com.fa.cim.entity.runtime.persongroup.CimPersonGroupDO;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.entity.runtime.productgroup.CimProductGroupDO;
import com.fa.cim.entity.runtime.productgroup.CimProductGroupToUserGroupDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IPersonMethod;
import com.fa.cim.method.IRouteMethod;
import com.fa.cim.method.ITransMethod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.person.PersonDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/11        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/10/11 15:20
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class PersonMethod  implements IPersonMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    public ITransMethod transMethod;

    @Autowired
    public IRouteMethod routeMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Override
    public Results.LoginCheckInqResult personLogOnCheck(Infos.ObjCommon objCommon, User user, String subSystemID, String categoryID) {
        Results.LoginCheckInqResult loginCheckInqResult = new Results.LoginCheckInqResult();

        // get environment object
        log.debug("get environment object");
        String env = StandardProperties.OM_ACCESS_USER_POLICY.getValue();
        boolean userPrivilegePolicyFlag = !CimStringUtils.isEmpty(env) && (CimStringUtils.equals(env, "1"));
        CimPerson cimPersonBO = baseCoreFactory.getBO(CimPerson.class, user.getUserID());
        Validations.check(cimPersonBO == null, new OmCode(retCodeConfig.getNotFoundPerson(), user.getUserID().getValue()));
        if (!CimStringUtils.isEmpty(subSystemID)) {
            log.debug("subSystemID != null");
            List<Infos.FuncList> subSystemFuncList = new ArrayList<>();
            Infos.FuncList funcList = new Infos.FuncList();
            funcList.setSubSystemID(subSystemID);
            subSystemFuncList.add(funcList);
            List<PersonDTO.PrivilegeInfo> functionList = !userPrivilegePolicyFlag ? cimPersonBO.allOperablePrivilegesFor(subSystemID, categoryID)
                    : cimPersonBO.allPrivilegesFor(subSystemID, categoryID);
            Optional<List<PersonDTO.PrivilegeInfo>> privilegeInfosOptinal = Optional.ofNullable(functionList);
            List<Infos.FuncID> funcIDList = new ArrayList<>();
            privilegeInfosOptinal.ifPresent(list -> list.forEach(x -> {
                Infos.FuncID funcID = new Infos.FuncID();
                funcID.setCategoryID(x.getCategory());
                funcID.setFunctionID(x.getPrivilegeID());
                funcID.setPermission(x.getPermission());
                funcIDList.add(funcID);
            }));
            funcList.setFuncIDList(funcIDList);
            loginCheckInqResult.setSubSystemFuncList(subSystemFuncList);
        } else {
            log.debug("Input Sub System ID is NULL");
            log.debug("Now call PosPerson->allOperableSubSystems()");
            Optional<List<String>> subSystemIDList=Optional.ofNullable(cimPersonBO.allOperableSubSystems());

            List<Infos.FuncList> subSystemFuncList = new ArrayList<>();
            subSystemIDList.ifPresent(list -> list.forEach(x -> {
                log.debug("Sub System ID: {}", x);
                Infos.FuncList funcList = new Infos.FuncList();
                funcList.setSubSystemID(x);

                List<PersonDTO.PrivilegeInfo> functionList = !userPrivilegePolicyFlag ? cimPersonBO.allOperablePrivilegesFor(x, null)
                        : cimPersonBO.allPrivilegesFor(x, null);
                Optional<List<PersonDTO.PrivilegeInfo>> privilegeInfosOptinal = Optional.ofNullable(functionList);
                List<Infos.FuncID> funcIDList = new ArrayList<>();
                privilegeInfosOptinal.ifPresent(list2 -> list2.forEach(privilegeInfo -> {
                    Infos.FuncID funcID = new Infos.FuncID();
                    funcID.setCategoryID(privilegeInfo.getCategory());
                    funcID.setFunctionID(privilegeInfo.getPrivilegeID());
                    funcID.setPermission(privilegeInfo.getPermission());
                    funcIDList.add(funcID);
                }));
                funcList.setFuncIDList(funcIDList);
                subSystemFuncList.add(funcList);
            }));
            loginCheckInqResult.setSubSystemFuncList(subSystemFuncList);
        }
        return loginCheckInqResult;
    }

    @Override
    public Outputs.ObjPersonAllowProductListGetOut personAllowProductListGet(Infos.ObjCommon objCommon, ObjectIdentifier userID) {
        Outputs.ObjPersonAllowProductListGetOut objPersonAllowProductListGetOut = new Outputs.ObjPersonAllowProductListGetOut();

        //【step1】get userGroupID by userID
        log.debug("【step1】get userGroupID by userID");
        List<ObjectIdentifier> userGroupIDList = this.personUserGroupListGetDR(objCommon, userID);

        // Add "*" User Group for Allow All User Group
        userGroupIDList.add(new ObjectIdentifier("*"));

        //【step2】get productGroupID by userGroupID
        log.debug("【step2】get productGroupID by userGroupID");
        Outputs.ObjPersonProductGroupListGetOut productGroupListGetOutRetCode = this.personProductGroupListGetDR(objCommon, userGroupIDList);

        //【step3】get product id by productGroupID
        log.debug("【step3】get product id by productGroupID");
        Outputs.ObjPersonProductListGetOut productListGetOutRetCode = this.personProductListGetDR(objCommon, productGroupListGetOutRetCode.getProductGroupIDList());
        objPersonAllowProductListGetOut.setProductIDList(productListGetOutRetCode.getProductIDList());
        return objPersonAllowProductListGetOut;
    }

    @Override
    public List<ObjectIdentifier> personUserGroupListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier userID) {

        //Get UserIDKey By UserID
        CimPerson person = baseCoreFactory.getBO(CimPerson.class, userID);
        Validations.check(null == person, new OmCode(retCodeConfig.getNotFoundPerson(), ObjectIdentifier.fetchValue(userID)));
        String personID = person.getPrimaryKey();
        //Get UserGrpID By UserIDKey
        List<ObjectIdentifier> userGroupIDList = new ArrayList<>();
        String sql = "SELECT USER_GRP_ID FROM OMUSER_USERGRP WHERE REFKEY = ?";
        List<Object> query = cimJpaRepository.queryOneColumn(sql, personID);
        if (CimArrayUtils.getSize(query) > 0){
            for (Object objects :  query) {
                ObjectIdentifier userGroupID = new ObjectIdentifier();
                userGroupID.setValue(String.valueOf(objects));
                userGroupIDList.add(userGroupID);
            }
        }

        String sql1 = "SELECT UG.USER_GRP_ID FROM OMUSERGRP UG, OMUSERGRP_ORG UC, OMUSER US WHERE US.USER_ID=? " +
                "AND UC.ORG_ID = US.ORG_ID AND UG.ID = UC.REFKEY";
        List<Object> query1 = cimJpaRepository.queryOneColumn(sql1, userID.getValue());
        if (CimArrayUtils.getSize(query1)> 0){
            for (Object objects : query1) {
                ObjectIdentifier userGroupID = new ObjectIdentifier();
                userGroupID.setValue(String.valueOf(objects));
                userGroupIDList.add(userGroupID);
            }
        }
        return userGroupIDList;
    }

    @Override
    public Outputs.ObjPersonProductGroupListGetOut personProductGroupListGetDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> userGroupIDList) {
        Outputs.ObjPersonProductGroupListGetOut objPersonProductGroupListGetOut = new Outputs.ObjPersonProductGroupListGetOut();
        List<ObjectIdentifier> productGroupIDs = new ArrayList<>();
        /*-------------------------------*/
        /* Get ProductGrpID By UserGrpID */
        /*-------------------------------*/
        int nLen = CimArrayUtils.getSize(userGroupIDList);
        for (int i = 0; i < nLen; i++){
            ObjectIdentifier userGroupID = userGroupIDList.get(i);
            /*-------------------------------*/
            /* Get UserGrpIDKey By UserGrpID */
            /*-------------------------------*/
            String sql = "SELECT  REFKEY\n" +
                    "     FROM    OMPRODFMLY_USERGRP\n" +
                    "     WHERE   USERGRP_ID = ?";
            List<CimProductGroupToUserGroupDO> cimProductGroupToUserGroupDOList = cimJpaRepository.query(sql, CimProductGroupToUserGroupDO.class, new Object[]{userGroupID.getValue()});
            if (!CimArrayUtils.isEmpty(cimProductGroupToUserGroupDOList)){
                for (CimProductGroupToUserGroupDO cimProductGroupToUserGroupDO : cimProductGroupToUserGroupDOList){
                    /*----------------------------------*/
                    /* Get ProductGrpID By UserGrpIDKey */
                    /*----------------------------------*/
                    sql = "SELECT   PRODFMLY_ID \n" +
                          "FROM     OMPRODFMLY \n" +
                          "WHERE    ID = ? ";
                    List<CimProductGroupDO> cimPersonGroupDOList = cimJpaRepository.query(sql, CimProductGroupDO.class, new Object[]{cimProductGroupToUserGroupDO.getReferenceKey()});
                    if (!CimArrayUtils.isEmpty(cimPersonGroupDOList)){
                        for (CimProductGroupDO cimProductGroupDO : cimPersonGroupDOList){
                            productGroupIDs.add(new ObjectIdentifier(cimProductGroupDO.getProductGroupID()));
                        }
                    }
                }
            }
        }
        objPersonProductGroupListGetOut.setProductGroupIDList(productGroupIDs);
        return objPersonProductGroupListGetOut;
    }

    @Override
    public Outputs.ObjPersonProductListGetOut personProductListGetDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> productGroupIDList) {
        List<ObjectIdentifier> strProductIDList = new ArrayList<>();
        int count = 0;
        int nLen = CimArrayUtils.getSize(productGroupIDList);
        for (int i = 0; i < nLen; i++){
            ObjectIdentifier productGroupID = productGroupIDList.get(i);
            /*-------------------------------*/
            /* Get ProductID By ProductGrpID */
            /*-------------------------------*/
            String sql = "select   PROD_ID\n" +
                    "                from     OMPRODINFO\n" +
                    "                where    PRODFMLY_ID = ?";
            List<CimProductSpecificationDO> cimProductSpecificationDOList = cimJpaRepository.query(sql, CimProductSpecificationDO.class, productGroupID.getValue());
            boolean sameFlag = false;
            if (!CimArrayUtils.isEmpty(cimProductSpecificationDOList)){
                for (CimProductSpecificationDO cimProductSpecificationDO : cimProductSpecificationDOList){
                    for (int j = 0; j < count; j++){
                        if (ObjectIdentifier.equalsWithValue(cimProductSpecificationDO.getProductSpecID(), strProductIDList.get(j))){
                            sameFlag = true;
                            break;
                        }
                    }
                    if (!sameFlag){
                        strProductIDList.add(new ObjectIdentifier(cimProductSpecificationDO.getProductSpecID()));
                        count++;
                    }
                    sameFlag = false;
                }
            }
        }
        Outputs.ObjPersonProductListGetOut out = new Outputs.ObjPersonProductListGetOut();
        out.setProductIDList(strProductIDList);
        return out;
    }

    @Override
    public Outputs.ObjPersonAllowMachineRecipeListGetOut personAllowMachineRecipeListGet(Infos.ObjCommon objCommon, ObjectIdentifier userID) {
        Outputs.ObjPersonAllowMachineRecipeListGetOut result = null;

        //【step1】get userProductID list by userID
        List<ObjectIdentifier> userGroupIDList = this.personUserGroupListGetDR(objCommon, userID);

        //  add "*" User Group for Allow All User Group
        log.debug("add \"*\" user group for allow all user group");
        userGroupIDList.add(new ObjectIdentifier("*"));

        //【step2】get machineRecipeID list by userGroupID List
        result = this.personMachineRecipeListGetDR(objCommon, userGroupIDList);
        return result;
    }

    @Override
    public Outputs.ObjPersonAllowMachineRecipeListGetOut personMachineRecipeListGetDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> userGroupIDList) {
        Outputs.ObjPersonAllowMachineRecipeListGetOut out = new Outputs.ObjPersonAllowMachineRecipeListGetOut();

        Optional<List<ObjectIdentifier>> userGroupIDListOpt = Optional.ofNullable(userGroupIDList);
        List<String> strUserGroupIDList = new ArrayList<>();
        userGroupIDListOpt.ifPresent(list -> list.forEach(userGroupID -> {
            strUserGroupIDList.add(userGroupID.getValue());
        }));

        int UserGroupSize = CimArrayUtils.getSize(strUserGroupIDList);
        StringJoiner s = new StringJoiner("','", "'", "'");
        for (int i=0; i < UserGroupSize;i++){
            s.add(strUserGroupIDList.get(i));
        }
        String sql="select RECIPE_ID from OMRCP c where c.ID in (select b.REFKEY from OMRCP_USERGRP b where b.USERGRP_ID in ("+s.toString()+"))";
        List<Object> machineRecipeIDLisOpt=null;
        if(CimArrayUtils.isNotEmpty(userGroupIDList)){
            machineRecipeIDLisOpt = cimJpaRepository.queryOneColumn(sql);
        }
        List<ObjectIdentifier> machineRecipeIDList = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(machineRecipeIDLisOpt)) {
            for (Object objects :  machineRecipeIDLisOpt){
                ObjectIdentifier machineRecipeID = new ObjectIdentifier();
                machineRecipeID.setValue(String.valueOf(objects));
                machineRecipeIDList.add(machineRecipeID);
            }
        }
        out.setMachineRecipeIDList(machineRecipeIDList);
        return out;
    }

    @Override
    public void personPrivilegeCheckDR(Infos.ObjCommon objCommon, User user, ObjectIdentifier equipmentID, ObjectIdentifier stockerID, List<ObjectIdentifier> productIDList, List<ObjectIdentifier> routeIDList, List<ObjectIdentifier> lotIDList, List<ObjectIdentifier> machineRecipeIDList) {
        //【step1】check authserver mode
        log.debug("【step1】check authserver mode");
        String authAvailableFlag = StandardProperties.SP_AUTH_AUTHSERVER_AVAILABLE.getValue();

        if (EnvConst.ifEquals("1", authAvailableFlag)) {
            log.debug("authserver mode");
            if (CimStringUtils.isEmpty(user.getNewPassword())) {
                /*【bear】don't neet to change password */
                ObjectIdentifier userID = objCommon.getUser().getUserID();   // [in-param]user = objCommon.getUser()
                //【step1-1】check user existence in siview
                log.debug("【step1-1】check user existence in siview");
                CimPerson cimPersonBO = baseCoreFactory.getBO(CimPerson.class,userID.getValue());
                if (null == cimPersonBO) {
                    log.error("not found person");
                    Validations.check(new OmCode(retCodeConfig.getNotFoundPerson(), userID.getValue()));
                }
                //add user info to objCommon
                objCommon.getUser().setUserID(new ObjectIdentifier(cimPersonBO.getIdentifier(),cimPersonBO.getPrimaryKey()));
                //【step1-2】call request to authserver
                //【joseph】we don't do authserver check ,so we skip this source code.
                log.debug("【step1-2】call request to authserver");
            } else {
                // new password update
                log.debug("new password update");
                log.error("function disable...");
                Validations.check(retCodeConfig.getFunctionDisable());
            }
        } else {
            log.debug("non-authserver mode");
            //【step2】pass word check
            log.debug("【step2】pass word check");
            if (CimStringUtils.isEmpty(user.getNewPassword())) {
                CimPerson cimPersonBO = baseCoreFactory.getBO(CimPerson.class,user.getUserID());
                if (null == cimPersonBO) {
                    log.error("not found person by userID: {}", user.getUserID().getValue());
                    Validations.check(true, new OmCode(retCodeConfig.getNotFoundPerson(), user.getUserID().getValue()));
                }
                //add user info into objCommon
                objCommon.getUser().setUserID(new ObjectIdentifier(cimPersonBO.getIdentifier(),cimPersonBO.getPrimaryKey()));
                int expiredPeriod = CimNumberUtils.intValue(cimPersonBO.getExpiredPeriod());

                String password = user.getPassword();
                String dbPassword = cimPersonBO.getPassword();

                boolean passwordMatchSucc = CimStringUtils.equals(password, dbPassword);
                if (!passwordMatchSucc) {
                    log.error("invalid password");
                    Validations.check(retCodeConfig.getInvalidPassword());
                }

                double dataInterval = BaseStaticMethod.getDataInterval(cimPersonBO.getPasswordChangeTimeStamp(), new Timestamp(System.currentTimeMillis()));
                dataInterval = (dataInterval < 0 ? -dataInterval : dataInterval);
                if ((0 != expiredPeriod) && (dataInterval >= expiredPeriod)) {
                    log.error("password expired");
                    Validations.check(retCodeConfig.getPasswordExpired());
                }
            } else {
                // new password update
                log.debug("new password update");
                String strMaxPasswordLen = StandardProperties.SP_DO_COLUMN_MAX_LENGTH_FRUSER_PASSWORD.getValue();
                long maxPasswordLen = CimNumberUtils.longValue(strMaxPasswordLen);
                maxPasswordLen = (maxPasswordLen == 0 || maxPasswordLen > 64) ? 64 : maxPasswordLen;

                if (maxPasswordLen < user.getNewPassword().length()) {
                    log.error("new password length over");
                    throw new ServiceException(new OmCode(retCodeConfig.getNewPasswordLengthOver(), strMaxPasswordLen));
                }

                // check for query server or not
                log.debug("check for query server or not");

                // change new password
                // 【bear】theAuthenticationManager->changePassword()
                this.changePassword(user.getUserID().getValue(), user.getPassword(), user.getNewPassword());
            }
        }

        //【step4】person privilege check
        log.debug("【step4】person privilege check");
        ObjectIdentifier userID = user.getUserID();
        boolean matchFlag = CimStringUtils.equals(BizConstant.SP_PPT_SVC_MGR_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_QTIME_WATCH_DOG_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_LTIME_WATCH_DOG_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_PPWATCH_DOG_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_POST_PROC_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_UTSWATCHDOG_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_UTSEVENTWATCHDOG_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_SORTERWATCHDOG_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_TIME_CONSTRAINT_WATCH_DOG_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_SPC_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_TCS_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_XMS_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_RXM_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_SAR_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_DCSC_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_RTD_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_AMS_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_ADM_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_SENTINEL, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_CARRIER_OUT_PERSON,userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_AM_SENTINEL_PERSON, userID.getValue())
                || CimStringUtils.equals(BizConstant.SP_ARHS_PERSON, userID.getValue());
        if (!matchFlag) {
            CimPerson cimPersonBO = baseCoreFactory.getBO(CimPerson.class,userID);
            if (null == cimPersonBO) {
                log.error("not found person");
                Validations.check(new OmCode(retCodeConfig.getNotFoundPerson(), userID.getValue()));
            }

            //【step5】eqp privilege check
            log.debug("【step5】eqp privilege check");
            if (!ObjectIdentifier.isEmptyWithValue(equipmentID)) {
                boolean boolFlag = false;
                log.debug("eqp id is specified, {}", equipmentID.getValue());
                //【step5-1】search OMBAY_EQP by inpara EquipmentID
                log.debug("【step5-1】search OMBAY_EQP by inpara EquipmentID");
                String sql1="SELECT COUNT(OMBAY_EQP.ID) FROM OMBAY_EQP " +
                        "WHERE OMBAY_EQP.EQP_ID = ?1 " +
                        "AND OMBAY_EQP.REFKEY IN ( " +
                        "SELECT OMBAY.ID " +
                        "FROM  OMUSER_BAYGRP, OMBAYGRP, OMBAYGRP_BAY, OMBAY " +
                        "WHERE OMUSER_BAYGRP.REFKEY = ?2 " +
                        "AND OMBAYGRP.ID = OMUSER_BAYGRP.BAY_GRP_RKEY " +
                        "AND OMBAYGRP_BAY.REFKEY = OMBAYGRP.ID AND OMBAY.BAY_ID = OMBAYGRP_BAY.BAY_ID)" +
                        "AND OMBAY_EQP.EQP_ID NOT IN (" +
                        "SELECT OMBAYGRP_PROHBTEQP.PROHBT_EQP_ID " +
                        "FROM  OMUSER_BAYGRP, OMBAYGRP, OMBAYGRP_PROHBTEQP " +
                        "WHERE OMUSER_BAYGRP.REFKEY = ?3 " +
                        "AND OMBAYGRP.ID = OMUSER_BAYGRP.BAY_GRP_RKEY " +
                        "AND OMBAYGRP_PROHBTEQP.REFKEY = OMBAYGRP.ID)";
                Long count = cimJpaRepository.count(sql1,equipmentID.getValue(), cimPersonBO.getPrimaryKey(),cimPersonBO.getPrimaryKey());;
                if (count > 0) {
                    boolFlag = true;
                    log.debug("this eqp is authorized, {}", equipmentID.getValue());
                } else if (count == 0) {
                    //【step5-2】search OMBAY_STOCKER as STK_ID is inpara EquipmentID
                    log.debug("【step5-2】search OMBAY_STOCKER as STK_ID is inpara EquipmentID");
                    String sql2="SELECT COUNT(OMBAY_STOCKER.ID) FROM OMUSER_BAYGRP, OMBAYGRP, OMBAYGRP_BAY, OMBAY, OMBAY_STOCKER " +
                            "WHERE OMUSER_BAYGRP.REFKEY = ?1 AND OMBAYGRP.ID = OMUSER_BAYGRP.BAY_GRP_RKEY " +
                            "AND OMBAYGRP_BAY.REFKEY = OMBAYGRP.ID AND OMBAY.BAY_ID = OMBAYGRP_BAY.BAY_ID " +
                            "AND OMBAY_STOCKER.REFKEY = OMBAY.ID AND OMBAY_STOCKER.STOCKER_ID = ?2";
                    count = cimJpaRepository.count(sql2,cimPersonBO.getPrimaryKey(),equipmentID.getValue());
                    if (count > 0) {
                        boolFlag = true;
                        log.debug("this eqp(stocker) is authorized, {}", equipmentID.getValue());
                    } else if (count == 0) {
                        //【step5-3】search OMBAY_OHB as STOCKER_ID is inpara EquipmentID
                        log.debug("【step5-3】search OMBAY_OHB as STK_ID is inpara EquipmentID");
                        String sql3 = "SELECT COUNT(OMBAY_OHB.ID) FROM  OMUSER_BAYGRP, OMBAYGRP, OMBAYGRP_BAY, OMBAY, OMBAY_OHB " +
                                "WHERE OMUSER_BAYGRP.REFKEY = ?1 AND  OMBAYGRP.ID = OMUSER_BAYGRP.BAY_GRP_RKEY " +
                                "AND OMBAYGRP_BAY.REFKEY = OMBAYGRP.ID AND  OMBAY.BAY_ID = OMBAYGRP_BAY.BAY_ID " +
                                "AND OMBAY_OHB.REFKEY = OMBAY.ID AND  OMBAY_OHB.STOCKER_ID = ?2";
                        count = cimJpaRepository.count(sql3,cimPersonBO.getPrimaryKey(),equipmentID.getValue());
                        if (count > 0){
                            boolFlag = true;
                            log.debug("this eqp(stocker) is authorized, {}", equipmentID.getValue());
                        }else if (count == 0){
                            String sql4 = "SELECT COUNT(OMBAY_FMCOHB.ID) FROM  OMUSER_BAYGRP, OMBAYGRP, OMBAYGRP_BAY, OMBAY, OMBAY_FMCOHB " +
                                    "WHERE OMUSER_BAYGRP.REFKEY = ?1 AND  OMBAYGRP.ID = OMUSER_BAYGRP.BAY_GRP_RKEY " +
                                    "AND OMBAYGRP_BAY.REFKEY = OMBAYGRP.ID AND  OMBAY.BAY_ID = OMBAYGRP_BAY.BAY_ID " +
                                    "AND OMBAY_FMCOHB.REFKEY = OMBAY.ID AND  OMBAY_FMCOHB.STOCKER_ID = ?2";
                            count = cimJpaRepository.count(sql4,cimPersonBO.getPrimaryKey(),equipmentID.getValue());
                            if (count > 0){
                                boolFlag = true;
                                log.debug("this eqp(stocker) is authorized, {}", equipmentID.getValue());
                            }else if (count == 0){
                                //【step5-4】Search OMBAY_EQP
                                log.debug("【step5-4】Search OMBAY_EQP");
                                CimMachine cimMachineBO = baseCoreFactory.getBO(CimMachine.class,equipmentID);
                                if (null != cimMachineBO) {
                                    boolFlag = false;
                                    log.debug("specified eqp is in OMEQP but not authorized. equipmentID: {}", equipmentID.getValue());
                                } else {
                                    //【step5-5】Search OMBAY_STOCKER
                                    CimStorageMachine cimStorageMachineBO = baseCoreFactory.getBO(CimStorageMachine.class,equipmentID);
                                    if (null == cimStorageMachineBO) {
                                        log.debug("not found the stocker, stockerID: {}", equipmentID.getValue());
                                        Validations.check(true, retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(equipmentID));
                                    }else {
                                        boolFlag = false;
                                        log.debug("specified eqp is in OMSTOCKER but not authorized. equipmentID: {}", equipmentID.getValue());
                                    }
                                }
                            }
                        }
                    }
                }

                if (!boolFlag) {
                    log.error("this eqp or stk is not authorized, equipmentID: {}", equipmentID.getValue());
                    Validations.check(retCodeConfig.getNotAuthEqp());
                }
            }

            //【step6】stocker eqp privilege check -- line: 752
            log.debug("【step6】stocker eqp privilege check");
            if (!ObjectIdentifier.isEmptyWithValue(stockerID)) {
                log.debug("stockerID is specified, the stockerID: {}", stockerID.getValue());
                String sql5= "SELECT COUNT(OMBAY_STOCKER.ID) FROM OMUSER_BAYGRP, OMBAYGRP, OMBAYGRP_BAY, OMBAY, OMBAY_STOCKER " +
                        "WHERE OMUSER_BAYGRP.REFKEY = ?1 AND OMBAYGRP.ID = OMUSER_BAYGRP.BAY_GRP_RKEY " +
                        "AND OMBAYGRP_BAY.REFKEY = OMBAYGRP.ID AND OMBAY.BAY_ID = OMBAYGRP_BAY.BAY_ID " +
                        "AND OMBAY_STOCKER.REFKEY = OMBAY.ID AND OMBAY_STOCKER.STOCKER_ID = ?2";
                Long count = cimJpaRepository.count(sql5, cimPersonBO.getPrimaryKey(),stockerID.getValue());;
                if (count == 0) {
                    //search OMBAY_OHB as STOCKER_ID is inpara EquipmentID
                    String sql6 = "SELECT COUNT(OMBAY_OHB.ID) FROM  OMUSER_BAYGRP, OMBAYGRP, OMBAYGRP_BAY, OMBAY, OMBAY_OHB " +
                            "WHERE OMUSER_BAYGRP.REFKEY = ?1 AND  OMBAYGRP.ID = OMUSER_BAYGRP.BAY_GRP_RKEY " +
                            "AND OMBAYGRP_BAY.REFKEY = OMBAYGRP.ID AND  OMBAY.BAY_ID = OMBAYGRP_BAY.BAY_ID " +
                            "AND OMBAY_OHB.REFKEY = OMBAY.ID AND  OMBAY_OHB.STOCKER_ID = ?2";
                    count = cimJpaRepository.count(sql6,cimPersonBO.getPrimaryKey(),stockerID.getValue());
                }
                if (count == 0) {
                    //search OMBAY_FMCOHB as STK_ID is inpara EquipmentID
                    String sql7 = "SELECT COUNT(OMBAY_FMCOHB.ID) FROM  OMUSER_BAYGRP, OMBAYGRP, OMBAYGRP_BAY, OMBAY, OMBAY_FMCOHB " +
                            "WHERE OMUSER_BAYGRP.REFKEY = ?1 AND  OMBAYGRP.ID = OMUSER_BAYGRP.BAY_GRP_RKEY " +
                            "AND OMBAYGRP_BAY.REFKEY = OMBAYGRP.ID AND  OMBAY.BAY_ID = OMBAYGRP_BAY.BAY_ID " +
                            "AND OMBAY_FMCOHB.REFKEY = OMBAY.ID AND  OMBAY_FMCOHB.STOCKER_ID = ?2";
                    count = cimJpaRepository.count(sql7, cimPersonBO.getPrimaryKey(), stockerID.getValue());
                }
                if (count > 0){
                    //Normal
                    log.info("This Stocker {} is authorized", ObjectIdentifier.fetchValue(stockerID));
                }else if (count == 0) {
                    CimStorageMachine cimStorageMachineBO = baseCoreFactory.getBO(CimStorageMachine.class,stockerID);
                    if (null == cimStorageMachineBO) {
                        log.error("not found the stocker, stockerID: {}", stockerID.getValue());
                        Validations.check(retCodeConfig.getNotFoundStocker());
                    }else {
                        log.error("specified stocker is in OMSTOCKER but not authorized, {}", ObjectIdentifier.fetchValue(equipmentID)); //【bear】yes, it's equipmentID but stockerID.
                        Validations.check(retCodeConfig.getNotAuthStocker());
                    }
                }
            }

            //【step7】function privilege check
            log.debug("【step7】function privilege check");
            if (!CimStringUtils.isEmpty(user.getFunctionID())) {
                log.debug("user.functionID is specified, {}", user.getFunctionID());
                String userId = cimPersonBO.getPrimaryKey();
                String functionID = user.getFunctionID();
                String subSystemID = BizConstant.SP_SUBSYSTEMID_MM;
                String permission = BizConstant.SP_MM_PERMISSION_ACCESS;
                String sql4 = "SELECT COUNT(OMACCESSGRP_ACCESS.ID) FROM OMUSER_ACCESSGRP, OMACCESSGRP, OMACCESSGRP_ACCESS " +
                        "WHERE OMUSER_ACCESSGRP.REFKEY = ?1 AND OMACCESSGRP.ID = OMUSER_ACCESSGRP.ACCESS_GRP_RKEY " +
                        "AND OMACCESSGRP.SERVICES_ID = ?2 AND OMACCESSGRP_ACCESS.REFKEY = OMACCESSGRP.ID " +
                        "AND OMACCESSGRP_ACCESS.LINK_KEY = ?3 AND OMACCESSGRP_ACCESS.PERMISSION = ?4";
                Long count = cimJpaRepository.count(sql4 ,userId, subSystemID, functionID, permission);
                if (count > 0) {
                    log.debug("the function is authorized, {}", functionID);
                } else {
                    log.error("the function is not authorized, {}", functionID);
                    Validations.check(retCodeConfig.getNotAuthFunc());
                }
            } else {
                throw new ServiceException(retCodeConfigEx.getNotFilledFunc());
            }
            //【step8】check transaction access control
            log.debug("【step8】check transaction access control");
            Outputs.ObjTransAccessControlCheckOut objTransAccessControlCheckOut = transMethod.transAccessControlCheckDR(objCommon);
            // check for query server allowed Tx or not.
            log.debug("check for query server allowed Tx or not.");

            //【step9】product status check
            log.debug("【step9】product status check");
            if (CimBooleanUtils.isTrue(objTransAccessControlCheckOut.getProductState())) {
                log.debug("controlCheckOutRetCode.object.productState = true");
                //【step9-1】check by productID
                log.debug("【step9-1】check by productID");
                int size = CimArrayUtils.getSize(productIDList);
                for (int i = 0; i < size; i++) {
                    ObjectIdentifier productID = productIDList.get(i);
                    if (CimStringUtils.isEmpty(productID.getValue())) {
                        continue;
                    }
                    this.personCheckProductPrivilegeDR(objCommon, user.getUserID(), productID);
                }
            }

            //【step9-2】check by routeID
            log.debug("【step9-2】check by routeID");
            int routeIDSize = CimArrayUtils.getSize(routeIDList);
            for (int i = 0; i < routeIDSize; i++) {
                ObjectIdentifier routeID = routeIDList.get(i);
                log.debug("for loop routeID: {}", routeID);
                if (!ObjectIdentifier.isEmptyWithValue(routeID)){
                    String sql = "SELECT PRF_TYPE\n" +
                            "                            FROM   OMPRP\n" +
                            "                            WHERE  PRP_ID    = ? AND\n" +
                            "                                   PRP_LEVEL = ?";
                    CimProcessDefinitionDO processDefinition = cimJpaRepository.queryOne(sql, CimProcessDefinitionDO.class, routeID.getValue(), BizConstant.SP_PD_FLOWLEVEL_MAIN);
                    if (null == processDefinition) {
                        log.error(String.format("not found route, the routeID:%s, pdLevel:%s", routeID, "Main"));
                        Validations.check(new OmCode(retCodeConfig.getNotFoundRoute(), routeID.getValue()));
                    }
                    if (!EnvConst.ifEquals(BizConstant.SP_FLOWTYPE_MAIN, processDefinition.getFlowType())) {
                        log.debug("FLOW_TYPE is not Main");
                        continue;
                    }

                    //【step9-2-1】get product list by routeID
                    log.debug("【step9-2-1】get product list by routeID");
                    List<ObjectIdentifier> routeProductIDList = routeMethod.routeProductListGetDR(objCommon, routeID);

                    int routeProductIDCount = CimArrayUtils.getSize(routeProductIDList);
                    log.debug("product count: {} for this route: {}", routeProductIDCount, routeID);
                    boolean routeAuthed = false;
                    if (0 == routeProductIDCount) {
                        log.debug("product for this route is not exist, no check.");
                        routeAuthed = true;
                    } else {
                        for (int j = 0; j < routeProductIDCount; j++) {
                            ObjectIdentifier routeProductID = routeProductIDList.get(j);
                            try {
                                this.personCheckProductPrivilegeDR(objCommon, user.getUserID(), routeProductID);
                            } catch (ServiceException ex) {
                                if (Validations.isEquals(retCodeConfig.getNotAuthProduct(), ex.getCode())) {
                                    throw ex;
                                }
                            }
                            log.debug("check ok, routeProductID: {}", routeProductID);
                            routeAuthed = true;
                            break;
                        }
                    }

                    if (!routeAuthed) {
                        log.debug("The products for this route is not authenticated. the routeID: {}", routeID);
                        throw new ServiceException(new OmCode(retCodeConfig.getNotAuthRoute(), ObjectIdentifier.fetchValue(routeID)));
                    }
                }
            }
            //【step9-3】check by lotID
            log.debug("【step9-3】check by lotID");
            int lotIDSize = CimArrayUtils.getSize(lotIDList);
            for (int j = 0; j < lotIDSize; j++) {
                ObjectIdentifier lotID = lotIDList.get(j);
                if (!ObjectIdentifier.isEmptyWithValue(lotID)){
                    CimLot cimLotBO = baseCoreFactory.getBO(CimLot.class,lotID);
                    if (null == cimLotBO) {
                        log.error("not found lot, the lotID: {}", lotID.getValue());
                        Validations.check(true, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
                    }
                    ObjectIdentifier lotProductID = cimLotBO.getProductSpecificationID();
                    try {
                        this.personCheckProductPrivilegeDR(objCommon, user.getUserID(), lotProductID);
                    } catch (ServiceException ex) {
                        if (Validations.isEquals(retCodeConfig.getNotAuthProduct(), ex.getCode())) {
                            log.error("product for this lot is not authenticated, the lotID:{}", lotID.getValue());
                            throw new ServiceException(new OmCode(retCodeConfig.getNotAuthLot(), lotID.getValue()));
                        }
                        throw ex;
                    }
                }
            }
            //【step9-4】check by machineRecipeID
            log.debug("【step9-4】check by machineRecipeID");
            if (CimBooleanUtils.isTrue(objTransAccessControlCheckOut.getMachineRecipeState())) {
                int machineRecipeIDSize = CimArrayUtils.getSize(machineRecipeIDList);
                for (int i = 0; i < machineRecipeIDSize; i++) {
                    ObjectIdentifier machineRecipeID = machineRecipeIDList.get(i);
                    if(!ObjectIdentifier.isEmptyWithValue(machineRecipeID)){
                        // get allow machine recipe list
                        log.debug("get allow machine recipe list");

                        Outputs.ObjPersonAllowMachineRecipeListGetOut machineRecipeListGetOutRetCode = this.personAllowMachineRecipeListGet(objCommon, user.getUserID());
                        boolean findFlag = false;
                        int size = CimArrayUtils.getSize(machineRecipeListGetOutRetCode.getMachineRecipeIDList());
                        for (int j = 0; j < size; j++) {
                            ObjectIdentifier machineRecipeIDTmp = machineRecipeListGetOutRetCode.getMachineRecipeIDList().get(j);
                            if (ObjectIdentifier.equalsWithValue(machineRecipeID, machineRecipeIDTmp)) {
                                findFlag = true;
                                break;
                            }
                        }
                        Validations.check(!findFlag, new OmCode(retCodeConfig.getNotAuthMachineRecipe(), ObjectIdentifier.fetchValue(machineRecipeID)));
                    }
                }
            }
        } else {
            //【step9-5】check transaction access control
            log.debug("【step9-5】check transaction access control");
        }
    }

    @Override
    public void changePassword(String userID, String password, String newPassword) {
        CimPerson cimPersonBO = baseCoreFactory.getBO(CimPerson.class,ObjectIdentifier.buildWithValue(userID));
        Validations.check(null == cimPersonBO, new OmCode(retCodeConfig.getNotFoundPerson(), userID));

        Validations.check(!CimStringUtils.equals(password, cimPersonBO.getPassword()), retCodeConfig.getInvalidPassword());
        cimPersonBO.setPassword(CimEncodeUtils.md5Encode(newPassword));
        cimPersonBO.setPasswordChangeTimeStamp(new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public void personCheckProductPrivilegeDR(Infos.ObjCommon objCommon, ObjectIdentifier userID, ObjectIdentifier productID) {
        //【step1】check product is open(user unlimited) product or not.
        log.debug("【step1】check product is open(user unlimited) product or not. productID: {}", productID);
        boolean privilegeFlag = false;
        String sqlBase = "SELECT PU.USERGRP_ID\n" +
                "              FROM OMPRODFMLY PG,\n" +
                "                   OMPRODFMLY_USERGRP PU,\n" +
                "                   OMPRODINFO PS\n" +
                "             WHERE PG.PRODFMLY_ID     =  PS.PRODFMLY_ID\n" +
                "               AND PG.ID =  PU.REFKEY\n" +
                "               AND PS.PROD_ID    = ? ";
        List<Object> queryList = cimJpaRepository.queryOneColumn(sqlBase, ObjectIdentifier.fetchValue(productID));
        String productGroupID = null;
        if (CimArrayUtils.isNotEmpty(queryList)){
            for (Object o : queryList) {
                productGroupID = null == o ? null : String.valueOf(o);
                if (CimStringUtils.equals(productGroupID, "*")) {
                    privilegeFlag = true;
                    break;
                }
            }
        }

        //【step2】check this close (user limited) product is available for user or not.
        log.debug("【step2】check this close (user limited) product is available for user or not.");
        if (CimBooleanUtils.isFalse(privilegeFlag)) {
            log.debug("product is close product. productID: {}", productID);
            String sql=  "SELECT UG.* FROM OMUSER US, OMUSER_USERGRP UG, OMPRODFMLY PG, OMPRODFMLY_USERGRP PU, OMPRODINFO PS " +
                    "WHERE UG.USER_GRP_ID = PU.USERGRP_ID AND PG.PRODFMLY_ID = PS.PRODFMLY_ID AND PG.ID = PU.REFKEY " +
                    "AND PS.PROD_ID = ?1 AND US.ID = UG.REFKEY AND US.USER_ID = ?2";
            List<Object[]> personToPersonGroupList = null;
            if (CimStringUtils.isNotEmpty(productID.getValue()) && CimStringUtils.isNotEmpty(userID.getValue())){
                personToPersonGroupList = cimJpaRepository.query(sql,productID.getValue(), userID.getValue());
            }
            if (!CimArrayUtils.isEmpty(personToPersonGroupList)) {
                log.debug("this product is privileged with userGroup, productID: {}, userID: {}", productID.getValue(), userID.getValue());
                privilegeFlag = true;
            }
        }

        //【step3】check with companyGroup - productgroup relation.
        if (CimBooleanUtils.isFalse(privilegeFlag)) {
            log.debug("product is close product and not privilged with userGroup. productID: {}", productID.getValue());
            String sql = "SELECT PU.* FROM OMPRODFMLY PG, OMPRODFMLY_USERGRP PU, OMPRODINFO PS, OMUSERGRP UG, OMUSERGRP_ORG UC, OMUSER US " +
                    "WHERE US.USER_ID = ?1 AND UC.ORG_ID = US.ORG_ID AND UG.ID = UC.REFKEY AND PU.USERGRP_ID = UG.USER_GRP_ID " +
                    "AND PG.ID = PU.REFKEY AND PS.PRODFMLY_ID = PG.PRODFMLY_ID AND PS.PROD_ID = ?2";
            List<Object[]> productGroupToUserGroup1 = cimJpaRepository.query(sql, userID.getValue(),productID.getValue());
            if (!CimArrayUtils.isEmpty(productGroupToUserGroup1)) {
                log.debug("this product is privileged with companyGroup.");
                privilegeFlag = true;
            } else {
                log.debug("this product is not authenticated for the user with companyGroup");
            }
        }
        Validations.check(CimBooleanUtils.isFalse(privilegeFlag), new OmCode(retCodeConfig.getNotAuthProduct(), ObjectIdentifier.fetchValue(productID)));
    }

    @Override
    public Results.BasicUserInfoInqResult personFillInTxPLQ013DR(Infos.ObjCommon objCommon, ObjectIdentifier userID) {
        Results.BasicUserInfoInqResult basicUserInfoInqResult = new Results.BasicUserInfoInqResult();
        com.fa.cim.newcore.bo.person.CimPerson cimPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, userID);
        Validations.check(cimPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), userID.getValue()));
        basicUserInfoInqResult.setUserName(cimPerson.getFullName());
        basicUserInfoInqResult.setDepartmentNumber(cimPerson.getDepartment());
        basicUserInfoInqResult.setMailAddress(cimPerson.getEMailAddress());
        basicUserInfoInqResult.setTelephoneNumber(cimPerson.getPhoneNumber());
        basicUserInfoInqResult.setPassword(cimPerson.getPassword());
        if (!CimStringUtils.isEmpty(basicUserInfoInqResult.getDepartmentNumber())){
            String sql = String.format("SELECT\n" +
                    "                DESCRIPTION\n" +
                    "            FROM\n" +
                    "                OMCODE\n" +
                    "            WHERE \n" +
                    "                CODE_ID     = '%s'  and\n" +
                    "                CODETYPE_ID = '%s'", basicUserInfoInqResult.getDepartmentNumber(), BizConstant.SP_CATEGORY_DEPARTMENT);
            com.fa.cim.newcore.bo.code.CimCode cimCode = baseCoreFactory.getBOByCustom(com.fa.cim.newcore.bo.code.CimCode.class, sql);
            if (cimCode != null){
                basicUserInfoInqResult.setDepartmentName(cimCode.getDescription());
            } else {
                basicUserInfoInqResult.setDepartmentName("*");
            }
        } else {
            basicUserInfoInqResult.setDepartmentName("*");
        }
        return basicUserInfoInqResult;
    }

    @Override
    public Outputs.ObjUserDefinedAttributeInfoGetDROut userDefinedAttributeInfoGetDR(Infos.ObjCommon objCommon, String classID) {
        CimUserDefinedAttributeInfoDO example = new CimUserDefinedAttributeInfoDO();
        example.setClassID(classID);
        List<Infos.UserDefinedData> userDefinedData = cimJpaRepository.findAll(Example.of(example)).stream().map(data -> {
            Infos.UserDefinedData info = new Infos.UserDefinedData();
            info.setClassID(data.getClassID());
            info.setSeqNo(data.getSeqNo());
            info.setName(data.getName());
            info.setType(data.getType());
            info.setDescription(data.getDescription());
            info.setInitialValue(data.getInitialValue());
            return info;
        }).collect(Collectors.toList());
        Outputs.ObjUserDefinedAttributeInfoGetDROut out = new Outputs.ObjUserDefinedAttributeInfoGetDROut();
        out.setStrUserDefinedDataSeq(userDefinedData);

        return out;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param classID
     * @return com.fa.cim.dto.RetCode<java.util.List < Infos.UserDefinedData>>
     * @author Ho
     * @date 2019/2/19 14:41:28
     */
    @Override
    public List<Infos.UserDefinedData> getUserDefinedAttributeDR(Infos.ObjCommon objCommon, String classID) {
        CimUserDefinedAttributeInfoDO example = new CimUserDefinedAttributeInfoDO();
        if (CimStringUtils.isNotEmpty(classID)) {
            example.setClassID(classID);
        }
        List<Infos.UserDefinedData> strUserDefinedDataSeq = cimJpaRepository.findAll(Example.of(example)).stream()
                .sorted(Comparator.comparing(CimUserDefinedAttributeInfoDO::getClassID))
                .sorted(Comparator.comparing(CimUserDefinedAttributeInfoDO::getSeqNo))
                .map(data -> {
                    Infos.UserDefinedData info = new Infos.UserDefinedData();
                    info.setClassID(data.getClassID());
                    info.setSeqNo(data.getSeqNo());
                    info.setName(data.getName());
                    info.setType(data.getType());
                    info.setDescription(data.getDescription());
                    info.setInitialValue(data.getInitialValue());
                    return info;
                }).collect(Collectors.toList());

        RetCode<List<Infos.UserDefinedData>> result = new RetCode<>();
        result.setObject(strUserDefinedDataSeq);
        result.setReturnCode(retCodeConfig.getSucc());
        return result.getObject();
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 11:08
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public String personExistenceCheck(Infos.ObjCommon objCommon, ObjectIdentifier userId) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, userId);
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), userId.getValue()));
        return aPerson.getPrimaryKey();
    }

    @Override
    public List<Infos.UsersForOMS> users() {
        List<Infos.UsersForOMS> users = new ArrayList<>();
        List<CimPersonDO> persons = cimJpaRepository.query("SELECT distinct f.ID, f.USER_ID, f.USER_NAME, f.EMAIL_ID, f.TEL_CONTACT_NO " +
                "FROM OMUSER f, OMUSER_USERGRP fu, OMUSERGRP f2 WHERE fu.REFKEY = f.ID AND fu.USER_GRP_ID = f2.USER_GRP_ID AND f2.USER_GRP_TYPE = 'OMS'", CimPersonDO.class);
        if(CimArrayUtils.isNotEmpty(persons)){
            for (CimPersonDO person : persons) {
                Infos.UsersForOMS usersForOMS = new Infos.UsersForOMS();
                usersForOMS.setId(person.getId());
                usersForOMS.setUsername(person.getUserID());
                usersForOMS.setRealName(person.getUserFullID());
                usersForOMS.setMail(person.getEmailAddress());
                usersForOMS.setPhone(person.getPhoneNO());
                users.add(usersForOMS);
            }
        }
        return users;
    }

    @Override
    public List<Infos.UserGroupForOMS> userGroups() {
        List<Infos.UserGroupForOMS> userGroups = new ArrayList<>();
        List<CimPersonGroupDO> personGroups = cimJpaRepository.query("SELECT ID, USERGRP_ID, USERGRP_TYPE FROM OMUSERGRP f WHERE USER_GRP_TYPE = 'OMS'", CimPersonGroupDO.class);
        if (CimArrayUtils.isNotEmpty(personGroups)) {
            for (CimPersonGroupDO personGroup : personGroups) {
                Infos.UserGroupForOMS userList = new Infos.UserGroupForOMS();
                String userGroupID = personGroup.getUserGroupID();
                userList.setId(personGroup.getId());
                userList.setName(userGroupID);
                List<Object[]> userIDs = cimJpaRepository.query("SELECT f.ID FROM OMUSER f, OMUSER_USERGRP fu WHERE fu.REFKEY = f.ID AND fu.USER_GRP_ID = ?1", userGroupID);
                if (CimArrayUtils.isNotEmpty(userIDs)) {
                    userList.setUserIds(userIDs.stream().map(x -> (String) x[0]).collect(Collectors.toList()));
                }
                userGroups.add(userList);
            }
        }
        return userGroups;
    }
}