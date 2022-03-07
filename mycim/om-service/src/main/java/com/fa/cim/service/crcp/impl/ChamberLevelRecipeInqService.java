package com.fa.cim.service.crcp.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.crcp.ChamberLevelRecipeReserveParam;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.IChamberLevelRecipeMethod;
import com.fa.cim.service.crcp.IChamberLevelRecipeInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

/**
 * description: chamber level recipe service inq
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/9/26          ********              YJ               create file
 *
 * @author: YJ
 * @date: 2021/9/26 15:15
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class ChamberLevelRecipeInqService implements IChamberLevelRecipeInqService {

    @Autowired
    private IChamberLevelRecipeMethod chamberLevelRecipeMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public List<Infos.StartCassette> sxChamberLevelRecipeDesignatedInq(
            Infos.ObjCommon objCommon, ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam) {
        // step1. check param
        Validations.check(Objects.isNull(chamberLevelRecipeReserveParam), retCodeConfig.getInvalidInputParam());

        ObjectIdentifier equipmentId = chamberLevelRecipeReserveParam.getEquipmentId();
        Validations.check(ObjectIdentifier.isEmpty(equipmentId), retCodeConfig.getNotFoundEqp());

        return chamberLevelRecipeMethod.chamberLevelRecipeMoveQueryRpt(objCommon, chamberLevelRecipeReserveParam);
    }
}
