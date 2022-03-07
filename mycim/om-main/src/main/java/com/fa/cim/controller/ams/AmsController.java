package com.fa.cim.controller.ams;

import com.fa.cim.controller.interfaces.accessManagement.IAccessController;
import com.fa.cim.controller.interfaces.ams.IAmsController;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:
 * <p>AmsController .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/7/27/027   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/7/27/027 17:36
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IAccessController.class, confirmableKey = "AmsConfirm", cancellableKey = "AmsCancel")
@RequestMapping("/ams")
@Listenable
public class AmsController implements IAmsController {
}
