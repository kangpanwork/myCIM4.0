package com.fa.cim.service.cda;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * This file use to define the ICustomerDefinedAttributeService interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 17:01
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ICustomerDefinedAttributeService {

    List<Results.UserDataUpdateResult> sxCDAValueUpdateReq(Infos.ObjCommon objCommon, Params.CDAValueUpdateReqParams params);

}
