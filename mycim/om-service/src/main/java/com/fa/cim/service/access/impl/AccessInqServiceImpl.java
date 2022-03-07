package com.fa.cim.service.access.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.runtime.person.CimPersonDO;
import com.fa.cim.frameworks.common.TaskContextHolder;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IPersonMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.department.IDepartmentInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
 * @date: 2020/9/9 14:23
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class AccessInqServiceImpl implements IAccessInqService {
    @Autowired
    private IUtilsComp utilsComp;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IDepartmentInqService departmentInqService;

    @Override
    public Results.LoginCheckInqResult sxLoginCheckInq(Infos.ObjCommon objCommon, Params.LoginCheckInqParams params) {
        Results.LoginCheckInqResult loginCheckInqResult = new Results.LoginCheckInqResult();

        //【step1】check the privilege
        log.debug("【step1】check the privilege");
        Results.LoginCheckInqResult result = personMethod.personLogOnCheck(objCommon, params.getUser(), params.getSubSystemID(), params.getCategoryID());

        // set SubSystemFuncList
        loginCheckInqResult.setSubSystemFuncList(result.getSubSystemFuncList());

        if (CimBooleanUtils.isTrue(params.getProductRequestFlag())) {
            //【step2】get allow product list
            log.debug("【step2】get allow product list");

            Outputs.ObjPersonAllowProductListGetOut retCode = personMethod.personAllowProductListGet(objCommon, params.getUser().getUserID());

            loginCheckInqResult.setProductIDList(retCode.getProductIDList());
        }

        if (CimBooleanUtils.isTrue(params.getRecipeRequestFlag())) {
            //【step3】get allow machine recipe list
            log.debug("【step3】get allow machine recipe list");
            Outputs.ObjPersonAllowMachineRecipeListGetOut retCode = personMethod.personAllowMachineRecipeListGet(objCommon, params.getUser().getUserID());
            loginCheckInqResult.setMachineRecipeIDList(retCode.getMachineRecipeIDList());
        }
        return loginCheckInqResult;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @since 2018/10/15 10:39
     * @param objCommon -
     * @param params -
     */
    @Override
    public void sxAccessControlCheckInq(Infos.ObjCommon objCommon, Params.AccessControlCheckInqParams params) {
        if (StandardProperties.SP_PRIVILEGE_CHECK_BY_DR_FLAG.isTrue()) {
            //【step1】check the privilege by dr
            log.debug("【step1】check the privilege by dr");
            personMethod.personPrivilegeCheckDR(objCommon, objCommon.getUser(), params.getEquipmentID(), params.getStockerID(), params.getProductIDList(), params.getRouteIDList(), params.getLotIDLists(), params.getMachineRecipeIDList());

            // 【step 2】 check department and section
            log.debug("【step1】check department and section");
            this.departmentAndSectionCheckInq(objCommon, params);

               /* Long forceLDAPFlag = 0L;

            //【joseph】we don't use auto server to check privilege. so we skip ths code
            result = authServerMethod.authSvrSendAuthenticate(objCommon, params.getUser(), forceLDAPFlag);
            if (!Validations.isSuccess(result)) {
                log.error("authSvrSendAuthenticate() != ok");
            }*/
        } else {
            //【step2】check the privilege by fw
            //【joseph】we can just check the privilege by dr is ok.
            log.debug("todo - 【step2】check the privilege by fw");
        }
        TaskContextHolder.setObjCommon(objCommon);
    }

    /**
     * description: hold release check
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon - common
     * @param params    - hold release params
     * @author YJ
     * @date 2021/1/21 0021 16:44
     */
    private void departmentAndSectionCheckInq(Infos.ObjCommon objCommon, Params.AccessControlCheckInqParams params) {
        // 【step 1】check tx
        String transactionID = objCommon.getTransactionID();

        if (
            /*
             * hold release check
             */
                CimStringUtils.equals(TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue(), transactionID) ||
                CimStringUtils.equals(TransactionIDEnum.FUTURE_HOLD_CANCEL_REQ.getValue(), transactionID) ||
                CimStringUtils.equals(TransactionIDEnum.DEPARTMENT_AUTHORITY_HOLD_DEPARTMENT_CHANGE_REQ.getValue(), transactionID) ||
                CimStringUtils.equals(TransactionIDEnum.DEPARTMENT_AUTHORITY_FUTURE_HOLD_DEPARTMENT_CHANGE_REQ.getValue(), transactionID) ||
                CimStringUtils.equals(TransactionIDEnum.DEPARTMENT_AUTHORITY_PROCESS_HOLD_DEPARTMENT_CHANGE_REQ.getValue(), transactionID)
        ) {
            // 【step 1.1】 conversion check hold params
            List<Params.DepartmentCheckReasonCodeParams> departmentSectionCheckCodes = params.getDepartmentSectionCheckCodes();
            if (CollectionUtils.isEmpty(departmentSectionCheckCodes)) {
                return;
            }
            departmentSectionCheckCodes.parallelStream().forEach(departmentCheckReasonCodeParams -> {
                departmentInqService.sxDepartmentAndSectionReasonCodeCheckInq(objCommon, departmentCheckReasonCodeParams);
            });
        } else if (
            /*
             * eqp state change
             */
                CimStringUtils.equals(TransactionIDEnum.EQP_STATUS_CHANGE_REQ.getValue(), transactionID)
        ) {
            // eqp state change 的权限验证修改到 EquipmentMethod.checkEquipmentStateTransition() 中, 所以应删除本验证
            // departmentInqService.sxDepartmentAndSectionEqpStateCheckInq(objCommon, params.getEquipmentID());
        }

    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @since 2019/7/3 10:39
     * @param transactionID -
     * @param user -
     * @param accessControlCheckInqParams -
     * @return com.fa.cim.dto.Infos.ObjCommon
     */
    @Override
    public Infos.ObjCommon checkPrivilegeAndGetObjCommon(String transactionID, User user, Params.AccessControlCheckInqParams accessControlCheckInqParams) {
        Validations.check(null == user, "the user info is null...");

        log.debug("【step1】get schedule from calendar");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.debug("【step2】call txAccessControlCheckInq(...)");
//        AccessInqServiceImpl accessInqService = (AccessInqServiceImpl) AopContext.currentProxy();
//        accessInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParams);
        this.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);
        TaskContextHolder.setObjCommon(objCommon);
        return objCommon;
    }

    @Override
    public List<Results.BasicUserInfoInqResult> sxAllUserInfoInq(){
        List<Results.BasicUserInfoInqResult> allUserInfoInqResults = new ArrayList<>();
        String sql = "select * from fruser";
        List<CimPersonDO> personDOList = cimJpaRepository.query(sql, CimPersonDO.class);
        if (!CimArrayUtils.isEmpty(personDOList)){
            for (CimPersonDO cimPersonDO : personDOList){
                Results.BasicUserInfoInqResult basicUserInfoInqResult = new Results.BasicUserInfoInqResult();
                allUserInfoInqResults.add(basicUserInfoInqResult);
                basicUserInfoInqResult.setUserID(cimPersonDO.getUserID());
                basicUserInfoInqResult.setUserName(cimPersonDO.getUserFullID());
                basicUserInfoInqResult.setDepartmentName(basicUserInfoInqResult.getDepartmentName());
                basicUserInfoInqResult.setDepartmentNumber(basicUserInfoInqResult.getDepartmentNumber());
                basicUserInfoInqResult.setTelephoneNumber(basicUserInfoInqResult.getTelephoneNumber());
                basicUserInfoInqResult.setMailAddress(basicUserInfoInqResult.getMailAddress());
            }
        }
        return allUserInfoInqResults;
    }

    @Override
    public Results.BasicUserInfoInqResult sxBasicUserInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier userID) {
        return personMethod.personFillInTxPLQ013DR(objCommon,userID);
    }



}
