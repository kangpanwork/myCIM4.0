package com.fa.cim.service.department.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.ICodeMethod;
import com.fa.cim.method.IPersonGroupMethod;
import com.fa.cim.newcore.dto.person.UserGroupAccessControlInfo;
import com.fa.cim.service.department.IDepartmentInqService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * description: department inq service impl
 * <p>
 * change history: date defect# person comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/1/20 0020 ******** YJ create file
 *
 * @author: YJ
 * @date: 2021/1/20 0020 13:49
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class DepartmentInqServiceImpl implements IDepartmentInqService {

    @Autowired
    private ICodeMethod codeMethod;

    @Autowired
    private IPersonGroupMethod personGroupMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public List<UserGroupAccessControlInfo.DepartmentInfo> sxDepartmentSectionInfoInq(Infos.ObjCommon objCommon, Params.DepartmentSectionParams departmentSectionParams) {
        return personGroupMethod.departmentSectionInfoGet(objCommon, departmentSectionParams);
    }

    @Override
    public List<Infos.ReasonCodeAttributes> sxReasonCodeByDepartmentAndSectionInq(Infos.ObjCommon objCommon, Params.ReasonCodeQueryParams reasonCodeQueryParams) {
        // 【step 1】 find group by department and section
        log.info("sxReasonCodeByDepartmentAndSectionInq -> find group by department and section");
        List<String> groupIds = personGroupMethod.groupIdByDepartmentAndSectionGet(reasonCodeQueryParams);
        if (CollectionUtils.isEmpty(groupIds)) {
            return Lists.newArrayList();
        }
        reasonCodeQueryParams.setGroupIds(groupIds);
        // 【step 2】 find reason code
        log.info("sxReasonCodeByDepartmentAndSectionInq -> find reason code");
        return codeMethod.codeByGroupIdsGet(objCommon, reasonCodeQueryParams);
    }

    @Override
    public void sxDepartmentAndSectionReasonCodeCheckInq(Infos.ObjCommon objCommon, Params.DepartmentCheckReasonCodeParams departmentCheckReasonCodeParams) {
        // 【step 1】 check department and section, If it doesn't exist, it doesn't check
        log.info("sxDepartmentAndSectionReasonCodeCheckInq -> check params");
        String department = departmentCheckReasonCodeParams.getDepartment();
        String section = departmentCheckReasonCodeParams.getSection();
        if (CimStringUtils.isEmpty(department) || CimStringUtils.isEmpty(section)) {
            return;
        }
        Validations.check(ObjectIdentifier.isEmpty(departmentCheckReasonCodeParams.getHoldReasonCodeId()), retCodeConfig.getInvalidInputParam());

        // 【step end】 check ！  user whether have reason code authority
        log.info("departmentAndSectionReasonCodeCheck -> check department and section");
        Long count = personGroupMethod.departmentAndSectionReasonCodeCheck(objCommon, departmentCheckReasonCodeParams);
        Validations.check(CimNumberUtils.eq(count.intValue(), 0),
                retCodeConfigEx.getDepartmentHoldNotAuthority(),
                department, section, ObjectIdentifier.fetchValue(departmentCheckReasonCodeParams.getHoldReasonCodeId()));
    }

    @Override
    public void sxDepartmentAndSectionEqpStateCheckInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        // 【step 1】 check eqp Id
        log.info("sxDepartmentAndSectionEqpStateCheckInq -> check eqp ID");
        Validations.check(ObjectIdentifier.isEmpty(equipmentID), retCodeConfig.getInvalidInputParam());

        // 【step 2】 check ! user whether have eqp state authority
        log.info("departmentAndSectionEqpStateCheck -> check department and section");
        Long count = personGroupMethod.departmentAndSectionEqpStateCheck(objCommon, equipmentID);
        Validations.check(CimNumberUtils.eq(count.intValue(), 0), retCodeConfigEx.getDepartmentEqpStateNotAuthority());
    }
}