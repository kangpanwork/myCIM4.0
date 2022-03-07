package com.fa.cim.controller.department;

import com.fa.cim.controller.interfaces.department.IDepartmentController;
import com.fa.cim.controller.interfaces.dispatch.IDispatchController;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description: department req controller
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/1/20 0020        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/1/20 0020 11:18
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IDispatchController.class, confirmableKey = "DepartmentConfirm", cancellableKey = "DepartmentCancel")
@RequestMapping("/department")
@Listenable
public class DepartmentController implements IDepartmentController {
}