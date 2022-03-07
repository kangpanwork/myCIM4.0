package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.controller.interfaces.parts.IPartsController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>PartsConfirm .
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
@Service("PartsConfirm")
@Transactional(rollbackFor = Exception.class)
public class PartsConfirm implements IPartsController {
}
