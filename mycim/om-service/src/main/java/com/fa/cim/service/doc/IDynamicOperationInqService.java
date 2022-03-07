package com.fa.cim.service.doc;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * description:
 * This file use to define the IDynamicOperationInqService interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 17:08
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDynamicOperationInqService {

    Results.DOCLotInfoInqResult sxDOCLotInfoInq(Infos.ObjCommon objCommon, Params.DOCLotInfoInqParams params);

    Results.DOCStepListInProcessFlowInqResult sxDOCStepListInProcessFlowInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    Infos.FPCInfo sxCopyFromInq(Infos.ObjCommon objCommon, Infos.FPCInfo fpcInfo, ObjectIdentifier productID, ObjectIdentifier lotID);

    Infos.RouteInfo sxProcessFlowOpeListWithNestInq(Infos.ObjCommon objCommon, Params.ProcessFlowOpeListWithNestInqParam param);
}
