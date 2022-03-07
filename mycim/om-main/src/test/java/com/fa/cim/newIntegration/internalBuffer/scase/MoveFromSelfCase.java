package com.fa.cim.newIntegration.internalBuffer.scase;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.dto.Params;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/6        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/6 17:00
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class MoveFromSelfCase {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private EquipmentController equipmentController;

    public Response moveFromSelf(Params.CarrierMoveFromIBRptParams params) {
        return equipmentController.carrierMoveFromIBRpt(params);
    }
}