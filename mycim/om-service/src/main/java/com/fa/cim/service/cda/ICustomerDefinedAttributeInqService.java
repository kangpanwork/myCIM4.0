package com.fa.cim.service.cda;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * This file use to define the ICustomerDefinedAttributeInqService interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 17:02
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ICustomerDefinedAttributeInqService {

    Results.CDAInfoInqResult sxCDAInfoInq(Infos.ObjCommon objCommon, Params.CDAInfoInqParams params);

    List<Infos.UserData> sxCDAValueInq(Infos.ObjCommon objCommon, Params.CDAValueInqParams params);

}
