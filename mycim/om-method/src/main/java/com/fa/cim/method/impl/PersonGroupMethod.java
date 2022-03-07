package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.persongroup.CimPersonGroupDO;
import com.fa.cim.entity.runtime.persongroup.CimPersonGroupDepartmentDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IPersonGroupMethod;
import com.fa.cim.newcore.bo.code.CimMachineState;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.dto.person.UserGroupAccessControlInfo;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * description: person group method impl
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/1/20 0020        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/1/20 0020 14:28
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class PersonGroupMethod implements IPersonGroupMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public List<UserGroupAccessControlInfo.DepartmentInfo> departmentSectionInfoGet(Infos.ObjCommon objCommon, Params.DepartmentSectionParams departmentSectionParams) {
        // 【step 1】 find user group
        String queryDepartmentAndSectionSql =
                "SELECT ud.* FROM OMUSERGRP_DEPT ud INNER JOIN OMUSERGRP ug ON ud.REFKEY = ug.ID " +
                        " INNER JOIN OMUSERGRP_ACCUSRGRP ugp ON ug.ID = ugp.REFKEY " +
                        "WHERE ugp.PURPOSE = ?1";
        String purpose = CimStringUtils.isEmpty(departmentSectionParams.getPurpose())
                ? UserGroupAccessControlInfo.Purpose.ReasonCode.name() : departmentSectionParams.getPurpose();

        List<CimPersonGroupDepartmentDO> resultGroup = cimJpaRepository
                .query(queryDepartmentAndSectionSql, CimPersonGroupDepartmentDO.class,
                        purpose);

        // 【step 2】 conversion user group to department and section
        Map<String, List<CimPersonGroupDepartmentDO>> departmentMap = resultGroup.parallelStream()
                .collect(Collectors.groupingBy(CimPersonGroupDepartmentDO::getDepartmentId));

        return departmentMap.keySet().stream().map(k -> {
            List<CimPersonGroupDepartmentDO> cimPersonGroupDOS = departmentMap.get(k);
            // department code
            UserGroupAccessControlInfo.DepartmentInfo departmentInfo = new UserGroupAccessControlInfo.DepartmentInfo();
            departmentInfo.setDepartmentCode(k);
            // section code
            List<UserGroupAccessControlInfo.SectionInfo> sectionInfos = cimPersonGroupDOS.parallelStream()
                    .map(CimPersonGroupDepartmentDO::getSectionId)
                    .distinct()
                    .map(section -> new UserGroupAccessControlInfo.SectionInfo().setSectionCode(section))
                    .collect(Collectors.toList());
            departmentInfo.setSectionInfos(sectionInfos);
            return departmentInfo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> groupIdByDepartmentAndSectionGet(Params.ReasonCodeQueryParams reasonCodeQueryParams) {
        // 【step 1】 find group id
        String queryGroupIdSql = "SELECT ug.ID FROM OMUSERGRP ug " +
                "left join OMUSERGRP_DEPT ud on ud.REFKEY = ug.id " +
                "INNER JOIN OMUSERGRP_ACCUSRGRP gp ON ug.ID = gp.REFKEY and gp.PURPOSE = ?1 " +
                "where ud.DEPARTMENT_ID = ?2 AND ud.SECTION_ID = ?3";
        List<CimPersonGroupDO> resultGroup = cimJpaRepository
                .query(queryGroupIdSql, CimPersonGroupDO.class,
                        UserGroupAccessControlInfo.Purpose.ReasonCode.name(),
                        reasonCodeQueryParams.getDepartment(),
                        reasonCodeQueryParams.getSection());

        // 【step 2】 conversion ids
        return resultGroup.parallelStream().map(CimPersonGroupDO::getId).collect(Collectors.toList());
    }

    @Override
    public Long departmentAndSectionReasonCodeCheck(Infos.ObjCommon objCommon, Params.DepartmentCheckReasonCodeParams departmentCheckReasonCodeParams) {
        // 【step 1】 query count , by user , depart , section
        String queryCountSql = "SELECT count( m.code_id ) " +
                "FROM" +
                " OMUSER u" +
                " INNER JOIN OMUSER_USERGRP ug ON u.id = ug.REFKEY" +
                " INNER JOIN OMUSERGRP g ON ug.USER_GRP_ID = g.USER_GRP_ID" +
                " INNER JOIN OMUSERGRP_ACCUSRGRP gp ON g.ID = gp.REFKEY and gp.PURPOSE = ?1" +
                " INNER JOIN OMUSERGRP_DEPT ud ON ud.REFKEY = g.ID" +
                " INNER JOIN OMCODE_ACCUSRGRP ca ON g.id = ca.USERGRP_RKEY" +
                " INNER JOIN OMCODE m ON ca.REFKEY = m.id " +
                " WHERE" +
                " m.ACCUSRGRP = 1" +
                " AND u.USER_ID = ?2 " +
                " AND ud.DEPARTMENT_ID = ?3 " +
                " AND ud.SECTION_ID = ?4 " +
                " AND m.CODE_ID = ?5";

        User user = objCommon.getUser();
        return cimJpaRepository.count(
                queryCountSql,
                UserGroupAccessControlInfo.Purpose.ReasonCode.name(),
                user.getUserID().getValue(),
                departmentCheckReasonCodeParams.getDepartment(),
                departmentCheckReasonCodeParams.getSection(),
                departmentCheckReasonCodeParams.getHoldReasonCodeId().getValue()
        );
    }

    @Override
    public Long departmentAndSectionEqpStateCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        // 【step 1】 find eqp state
        CimMachine eqpBo = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        String eqpStateId = "";
        if (Objects.isNull(eqpBo)) {
            CimStorageMachine cimStorageMachineBO = baseCoreFactory.getBO(CimStorageMachine.class, equipmentID);
            if (Objects.nonNull(cimStorageMachineBO)) {
                CimMachineState currentMachineState = cimStorageMachineBO.getCurrentMachineState();
                if (Objects.nonNull(currentMachineState)) {
                    eqpStateId = currentMachineState.getIdentifier();
                }
            }
        } else {
            CimMachineState currentMachineState = eqpBo.getCurrentMachineState();
            if (Objects.nonNull(currentMachineState)) {
                eqpStateId = currentMachineState.getIdentifier();
            }
        }
        Validations.check(CimStringUtils.isEmpty(eqpStateId), retCodeConfig.getNotFoundEqp());


        // 【step 2】 query count , by user , depart , section
        String queryCountSql = "SELECT count( m.EQP_STATE_ID ) " +
                " FROM " +
                " OMUSER u" +
                " INNER JOIN OMUSER_USERGRP ug ON u.id = ug.REFKEY" +
                " INNER JOIN OMUSERGRP g ON ug.USER_GRP_ID = g.USER_GRP_ID" +
                " INNER JOIN OMUSERGRP_ACCUSRGRP gp ON g.ID = gp.REFKEY and gp.PURPOSE = ?1" +
                " INNER JOIN OMEQPST_ACCUSRGRP ea ON g.id = ea.USERGRP_RKEY" +
                " INNER JOIN OMEQPST m ON ea.REFKEY = m.id " +
                " WHERE" +
                " u.USER_ID = ?2 " +
                " AND m.EQP_STATE_ID = ?3";

        User user = objCommon.getUser();
        return cimJpaRepository.count(
                queryCountSql,
                UserGroupAccessControlInfo.Purpose.EquipmentState.name(),
                user.getUserID().getValue(),
                eqpStateId
        );
    }
}