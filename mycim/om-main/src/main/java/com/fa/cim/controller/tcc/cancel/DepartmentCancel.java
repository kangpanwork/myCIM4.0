package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.controller.interfaces.department.IDepartmentController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description: tcc department Cancel
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/1/20 0020        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/1/20 0020 11:20
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("DepartmentCancel")
@Transactional(rollbackFor = Exception.class)
public class DepartmentCancel implements IDepartmentController {
}