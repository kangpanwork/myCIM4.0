package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.controller.interfaces.parts.IPartsController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>PartsCancel .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/21 12:50         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/21 12:50
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Service("PartsCancel")
@Transactional(rollbackFor = Exception.class)
public class PartsCancel implements IPartsController {
}
