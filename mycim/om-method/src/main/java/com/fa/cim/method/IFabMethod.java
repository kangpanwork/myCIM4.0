package com.fa.cim.method;

import com.fa.cim.dto.Infos;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/24          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/2/24 17:00
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IFabMethod {

    List<Infos.InterFabDestinationInfo> fabInfoGetDR(Infos.ObjCommon objCommon, String fabID);
}