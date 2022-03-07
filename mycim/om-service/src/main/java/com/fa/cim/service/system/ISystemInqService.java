package com.fa.cim.service.system;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISystemInqService {

    List<Infos.CodeInfo> sxCodeSelectionInq(Infos.ObjCommon objCommon, ObjectIdentifier category);

    Results.OMSEnvInfoInqResult sxOMSEnvInfoInq(Infos.ObjCommon objCommon) ;

    List<Infos.ReasonCodeAttributes> sxReasonCodeListByCategoryInq(Infos.ObjCommon objCommon, String codeCategory) ;

}
