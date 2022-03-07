package com.fa.cim.controller.department;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.department.IDepartmentInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.department.IDepartmentInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * description: department in query controller
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/1/20 0020        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/1/20 0020 11:18
 * @copyright: 2021, FA Soft
 * ware (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/department")
@Listenable
public class DepartmentInqController implements IDepartmentInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IDepartmentInqService departmentInqService;

    @ResponseBody
    @RequestMapping(value = "/dept_section_info/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.DEPARTMENT_AUTHORITY_SECTION_INQ)
    @Override
    public Response departmentSectionInfoInq(@RequestBody Params.DepartmentSectionParams departmentSectionParams) {
        final String transactionID = TransactionIDEnum.DEPARTMENT_AUTHORITY_SECTION_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, departmentSectionParams.getUser(), accessControlCheckInqParams);
        return Response.createSucc(transactionID, departmentInqService.sxDepartmentSectionInfoInq(objCommon, departmentSectionParams));
    }

    @ResponseBody
    @RequestMapping(value = "/reason_code_by_department/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.DEPARTMENT_AUTHORITY_REASON_CODE_INQ)
    @Override
    public Response reasonCodeByDepartmentAndSectionInq(@RequestBody Params.ReasonCodeQueryParams reasonCodeQueryParams) {
        final String transactionID = TransactionIDEnum.DEPARTMENT_AUTHORITY_REASON_CODE_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, reasonCodeQueryParams.getUser(), accessControlCheckInqParams);
        return Response.createSucc(transactionID, departmentInqService.sxReasonCodeByDepartmentAndSectionInq(objCommon, reasonCodeQueryParams));
    }

}