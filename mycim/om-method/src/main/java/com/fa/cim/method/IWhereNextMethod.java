package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;

/**
 * description:
 * <p>IWhereNextTransferEqp .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/8        ********             Yuri               create file
 * 2019/9/23        ######              Neko                Refactor: change retCode to exception
 *
 * @author Yuri
 * @since 2018/11/8 13:24
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IWhereNextMethod {


    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn
     * @param equipmentID
     * @param equipmentTargetPortPickupOut
     * @return RetCode<Outputs.WhereNextTransferEqpOut>
     * @author Yuri
     * @date 2018/11/8 13:31:17
     */
    Outputs.WhereNextTransferEqpOut whereNextTransferEqp(Infos.ObjCommon objCommonIn,
                                                         ObjectIdentifier equipmentID,
                                                         Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickupOut);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/9/14                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/9/14 17:53
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.DurableWhereNextTransferEqpOut durableWhereNextTransferEqp(Infos.ObjCommon objCommonIn,
                                                         ObjectIdentifier equipmentID,
                                                         Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickupOut);

}
