package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.newcore.dto.person.UserGroupAccessControlInfo;

import java.util.List;

/**
 * description: user group method
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
public interface IPersonGroupMethod {

    /**
     * description: find department and section infos
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon common params
     * @return department and section
     * @author YJ
     * @date 2021/1/20 0020 14:02
     */
    List<UserGroupAccessControlInfo.DepartmentInfo> departmentSectionInfoGet(Infos.ObjCommon objCommon, Params.DepartmentSectionParams departmentSectionParams);

    /**
     * description:  find group id and section infos
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2021/1/20 0020 15:51
     * @param reasonCodeQueryParams - department and section
     * @return id
     */
    List<String> groupIdByDepartmentAndSectionGet(Params.ReasonCodeQueryParams reasonCodeQueryParams);

    /**
     * description: department and section check ,  reason code
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2021/1/21 0021 15:41
     * @param objCommon - common
     * @param departmentCheckReasonCodeParams - department and section and reason code
     */
    Long departmentAndSectionReasonCodeCheck(Infos.ObjCommon objCommon, Params.DepartmentCheckReasonCodeParams departmentCheckReasonCodeParams);

    /**
     * description: department and section check ,  eqp state
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2021/1/21 0021 15:41
     * @param objCommon - common
     * @param equipmentID - eqp id
     */
    Long departmentAndSectionEqpStateCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);
}