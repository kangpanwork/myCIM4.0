package com.fa.cim.service.department;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.newcore.dto.person.UserGroupAccessControlInfo;

import java.util.List;

/**
 * description: department inq service
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/1/20 0020        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/1/20 0020 13:49
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDepartmentInqService {

    /**
     * description: find department and section infos
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon common params
     * @param departmentSectionParams department section params
     * @return department and section
     * @author YJ
     * @date 2021/1/20 0020 14:02
     */
    List<UserGroupAccessControlInfo.DepartmentInfo> sxDepartmentSectionInfoInq(Infos.ObjCommon objCommon, Params.DepartmentSectionParams departmentSectionParams);

    /**
     * description: find reason code by department and section
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon             - objCommon
     * @param reasonCodeQueryParams - reason code query params
     * @return List<Reason code></>
     * @author YJ
     * @date 2021/1/20 0020 15:37
     */
    List<Infos.ReasonCodeAttributes> sxReasonCodeByDepartmentAndSectionInq(Infos.ObjCommon objCommon, Params.ReasonCodeQueryParams reasonCodeQueryParams);

    /**
     * description: department and section check ,  reason code
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                       - common
     * @param departmentCheckReasonCodeParams - department and section and reason code
     * @author YJ
     * @date 2021/1/21 0021 15:41
     */
    void sxDepartmentAndSectionReasonCodeCheckInq(Infos.ObjCommon objCommon, Params.DepartmentCheckReasonCodeParams departmentCheckReasonCodeParams);

    /**
     * description: eqp state check
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common
     * @param equipmentID - equipmentID
     * @author YJ
     * @date 2021/1/21 0021 17:02
     */
    void sxDepartmentAndSectionEqpStateCheckInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);
}