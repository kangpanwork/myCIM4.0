package com.fa.cim.service.dispatch;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * This file use to define the IDispatchService interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 16:50
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDispatchInqService {

    Results.AutoDispatchConfigInqResult sxAutoDispatchConfigInq(Infos.ObjCommon objCommon, Params.AutoDispatchConfigInqParams autoDispatchConfigInqParams);

    List<Infos.WhatNextStandbyAttributes> sxWhatNextNPWStandbyLotInq(Infos.ObjCommon objCommon, Params.WhatNextNPWStandbyLotInqParams params);

    Results.WhatNextLotListResult sxWhatNextLotListInfo(Infos.ObjCommon objCommon, Params.WhatNextLotListParams whatNextLotListParams) ;

    Results.VirtualOperationWipListInqResult sxVirtualOperationWipListInq(Infos.ObjCommon objCommon, Params.VirtualOperationWipListInqParams params);

    Results.LotsMoveInReserveInfoInqResult sxLotsMoveInReserveInfoForIBInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassettes);

    Results.EqpFullAutoConfigListInqResult sxEqpFullAutoConfigListInq(Infos.ObjCommon objCommon, Params.EqpFullAutoConfigListInqInParm eqpFullAutoConfigListInqInParm);
}
