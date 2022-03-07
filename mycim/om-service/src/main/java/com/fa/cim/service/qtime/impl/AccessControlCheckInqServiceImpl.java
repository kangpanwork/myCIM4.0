package com.fa.cim.service.qtime.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.IPersonMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.qtime.IAccessControlCheckInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/15        ********             Bear               create file
 *
 * @author Bear
 * @since 2018/10/15 10:02
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class AccessControlCheckInqServiceImpl implements IAccessControlCheckInqService {

    @Autowired
    private IUtilsComp utilsComp;

    @Autowired
    private IPersonMethod personMethod;

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
    //@Transactional(rollbackFor = Exception.class)
    @Override
    public void sxAccessControlCheckInq(Infos.ObjCommon objCommon, Params.AccessControlCheckInqParams params) {
        String privilegeByDRFlag = StandardProperties.SP_PRIVILEGE_CHECK_BY_DR_FLAG.getValue();
        if (CimStringUtils.equals(privilegeByDRFlag, "1")) {
            //【step1】check the privilege by dr
            log.debug("【step1】check the privilege by dr");
            personMethod.personPrivilegeCheckDR(objCommon, objCommon.getUser(), params.getEquipmentID(), params.getStockerID()
                    , params.getProductIDList(), params.getRouteIDList(), params.getLotIDLists(), params.getMachineRecipeIDList());
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

        log.info("【step1】get schedule from calendar");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step2】call txAccessControlCheckInq(...)");
        this.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        return objCommon;
    }
}