package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.plannedSplitMerge.IPlannedSplitMergeController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 17:18
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("PlannedSplitMergeCancel")
@Transactional(rollbackFor = Exception.class)
public class PlannedSplitMergeCancel implements IPlannedSplitMergeController {
    @Override
    public Response psmLotRemoveReq(Params.PSMLotRemoveReqParams params) {
        return null;
    }

    @Override
    public Response psmLotActionReq(Params.PSMLotActionReqParams params) {
        return null;
    }

    @Override
    public Response psmLotInfoSetReq(Params.PSMLotInfoSetReqParams params) {
        return null;
    }
}