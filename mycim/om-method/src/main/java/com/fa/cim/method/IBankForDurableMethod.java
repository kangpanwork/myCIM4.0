package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;

import java.util.List;

/**
 * description:
 * <p>IBankForDurableMethod .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/6/17/017   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/6/17/017 15:14
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IBankForDurableMethod {

    void bankCheckStartBankForDurableRouteDR(Infos.ObjCommon objCommonIn, ObjectIdentifier bankID);

    void durableBankIn(Infos.ObjCommon objCommonIn, Boolean onRouteFlag, String durableCategory, ObjectIdentifier durableID, ObjectIdentifier bankID);

    void durableBankInCancel(Infos.ObjCommon objCommonIn, String durableCategory, ObjectIdentifier durableID);

    void durableBankMove(Infos.ObjCommon objCommonIn, String durableCategory, ObjectIdentifier durableID, ObjectIdentifier bankID);

    /**
     * Check Durable is InBank to StartBank of specified route.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/23 13:00
     */
    void bankDurableSTBCheck(Infos.ObjCommon objCommon, ObjectIdentifier routeID, String durableCategory, List<ObjectIdentifier> durables);
}
