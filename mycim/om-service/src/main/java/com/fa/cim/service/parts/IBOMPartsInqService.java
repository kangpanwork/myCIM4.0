package com.fa.cim.service.parts;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8          ********            lightyh                create file
 *
 * @author: lightyh
 * @date: 2020/9/8 16:31
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IBOMPartsInqService {

    Results.BOMPartsDefinitionInqResult sxBOMPartsDefinitionInq(Infos.ObjCommon objCommon, Params.BOMPartsDefinitionInqInParams bomPartsDefinitionInqInParams);

    List<Infos.LotListAttributes> sxBOMPartsLotListForProcessInq(Infos.ObjCommon objCommon, Params.BOMPartsLotListForProcessInqInParams bomPartsLotListForProcessInqInParams);

}