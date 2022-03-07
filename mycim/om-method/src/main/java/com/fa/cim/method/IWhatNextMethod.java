package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;

import java.util.List;


public interface IWhatNextMethod {

    /**
     * description:
     * change history:    This object is make strStartCassette from in-param's pptWhatNextLotList for DeliveryReq.
     1. make strStartCassette from in-param's pptWhatNextLotList.
     2. check strStartCassette on each check's process.
     3. focus strStartCassette with Equipment's multiRecipeCapability and Cassette's multiLotType.
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/5 15:01
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.StartCassette> whatNextLotListToStartCassetteForDeliveryReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, List<Infos.PortGroup> strPortGroupSeq, Results.WhatNextLotListResult strWhatNextInqResult);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/17 16:40
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.StartCassette> whatNextLotListToStartCassetteForTakeOutInDeliveryReq(Infos.ObjCommon objCommon, Inputs.ObjWhatNextLotListToStartCassetteForTakeOutInDeliveryReqIn input);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/23                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/23 9:49
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.StartCassette> whatNextLotListToStartCassetteForDeliveryForInternalBufferReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, Outputs.EquipmentTargetPortPickupOut strEquipmentTargetPortPickupOut, Results.WhatNextLotListResult strWhatNextLotListForInternalBufferInqResult, boolean bEqpInternalBufferInfo, List<Infos.EqpInternalBufferInfo> strEqpInternalBufferInfoSeq);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/5/26                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/5/26 13:00
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.StartCassette> whatNextLotListToStartCassetteForSLMDeliveryReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, List<Infos.PortGroup> strPortGroupList, Results.WhatNextLotListResult whatNextInqResult);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/9/15                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/9/15 12:50
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.StartDurable> whatNextDurableListToStartDurableForDeliveryReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, List<Infos.PortGroup> strPortGroupSeq, Results.WhatNextDurableListInqResult durableWhatNextInqResult);


    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/9/23                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/9/23 11:29
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.StartDurable> whatNextDurableListToStartDurableForDeliveryForInternalBufferReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, Outputs.EquipmentTargetPortPickupOut strEquipmentTargetPortPickupOut, Results.WhatNextDurableListInqResult strWhatNextDurableListForInternalBufferInqResult, boolean bEqpInternalBufferInfo, List<Infos.EqpInternalBufferInfo> strEqpInternalBufferInfoSeq);


}
