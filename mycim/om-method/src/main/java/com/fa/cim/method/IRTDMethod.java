package com.fa.cim.method;

import com.fa.cim.dto.Infos;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/10/14       ********             lightyh             create file
 *
 * @author lightyh
 * @since 2019/10/14 16:56
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IRTDMethod {

    public Infos.RTDDataGetDROut rtdDataGetDR(
            Infos.ObjCommon        strObjCommonIn,
            String                 functionCode,
            String                 dispatchStationID );

}