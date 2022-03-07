package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;

import java.util.List;

/**
 * description:
 * This file use to define the IAuthServerMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/15        ********             Bear               create file
 *
 * @author Bear
 * @since 2018/10/15 11:21
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAreaMethod {
    /**
     * @param objCommon -
     * @return List<Infos.WorkArea>
     * @author ho
     */
    List<Infos.WorkArea> areaFillInTxTRQ014DR(Infos.ObjCommon objCommon);

    List<ObjectIdentifier> areaGetByLocationID(Infos.ObjCommon objCommon , String locationID);
}
