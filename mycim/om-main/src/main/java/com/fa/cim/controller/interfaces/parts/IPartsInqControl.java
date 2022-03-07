package com.fa.cim.controller.interfaces.parts;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * <p>IPartsInqControl .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/21 11:21
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface IPartsInqControl {

    /**
     * This function obtains BOM (Parts) Information which is defined for specified Product.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 17:17
     */
    Response bomPartsInq(Params.BOMPartsDefinitionInqInParams bomPartsDefinitionInqInParams);

    /**
     * This function obtains Parts Lot List for specified product operation.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/20 17:19
     */
    Response bomPartsForLotListInq(Params.BOMPartsLotListForProcessInqInParams bomPartsLotListForProcessInqInParams);
}
