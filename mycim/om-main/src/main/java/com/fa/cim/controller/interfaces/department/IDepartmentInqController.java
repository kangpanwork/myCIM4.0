package com.fa.cim.controller.interfaces.department;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description: department in query controller interface
 * This file use to define the IDepartmentInqController interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/1/20 0020        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/1/20 0020 11:22
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDepartmentInqController {


    /**
     * description: find department and section infos
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param departmentSectionParams params
     * @return department and section
     * @author YJ
     * @date 2021/1/20 0020 14:02
     */
    Response departmentSectionInfoInq(Params.DepartmentSectionParams departmentSectionParams);

    /**
     * description: find reason code by department and section
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param reasonCodeQueryParams - params
     * @return List<Reason code></>
     * @author YJ
     * @date 2021/1/20 0020 15:37
     */
    Response reasonCodeByDepartmentAndSectionInq(Params.ReasonCodeQueryParams reasonCodeQueryParams);
}