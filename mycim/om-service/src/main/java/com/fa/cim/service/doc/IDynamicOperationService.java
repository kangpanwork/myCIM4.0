package com.fa.cim.service.doc;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * This file use to define the IDynamicOperationService interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 17:08
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDynamicOperationService {

    void sxDOCLotActionReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    Results.DOCLotInfoSetReqResult sxDOCLotInfoSetReq(Infos.ObjCommon objCommon, List<Infos.FPCInfoAction> strFPCInfoActionList, String claimMemo, String runCardID);

    Results.DOCLotRemoveReqResult sxDOCLotRemoveReq(Infos.ObjCommon objCommon, Params.DOCLotRemoveReqParams strDOCLotRemoveReqInParm);

}
