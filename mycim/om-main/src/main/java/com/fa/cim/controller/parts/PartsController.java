package com.fa.cim.controller.parts;

import com.fa.cim.controller.interfaces.bond.IBondController;
import com.fa.cim.controller.interfaces.parts.IPartsController;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>PartsController .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/21 12:49         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/21 12:49
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IBondController.class, confirmableKey = "PartsConfirm", cancellableKey = "PartsCancel")
@Listenable
@RestController
@RequestMapping("/parts")
public class PartsController implements IPartsController {
}
