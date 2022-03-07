package com.fa.cim.controller.fmc;

import com.fa.cim.controller.interfaces.fmc.IFMCInqController;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>FMCInqController .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/21 11:02         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/21 11:02
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@Listenable
@RestController
@RequestMapping("/fmc")
public class FMCInqController implements IFMCInqController {
}
