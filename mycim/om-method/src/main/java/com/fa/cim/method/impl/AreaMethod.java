package com.fa.cim.method.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.entity.runtime.area.CimAreaDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IAreaMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/24       ********             lightyh             create file
 *
 * @author lightyh
 * @since 2019/9/24 10:46
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class AreaMethod implements IAreaMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Override
    public List<Infos.WorkArea> areaFillInTxTRQ014DR(Infos.ObjCommon objCommon) {
        List<CimAreaDO> areas = cimJpaRepository.query("select BAY_ID, ID, DESCRIPTION from OMBAY where BAY_CATEGORY = ?1", CimAreaDO.class, BizConstant.SP_AREACATEGORY_WORKAREA);
        Validations.check(CimObjectUtils.isEmpty(areas), retCodeConfig.getNotFoundEntry());
        return areas.stream().map(x -> new Infos.WorkArea(new ObjectIdentifier(x.getAreaID(), x.getId()), x.getDescription())).collect(Collectors.toList());
    }

    @Override
    public List<ObjectIdentifier> areaGetByLocationID(Infos.ObjCommon objCommon , String locationID){
        if (CimObjectUtils.isEmpty(locationID)){
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }
        List<CimAreaDO> areas = cimJpaRepository.query("select BAY_ID, ID FROM OMBAY where FAB_ID = ?1",
                CimAreaDO.class, locationID);
        Validations.check(CimObjectUtils.isEmpty(areas), retCodeConfig.getNotFoundEntry());
        List<ObjectIdentifier> workAreaIDList = new ArrayList<>();
        for (CimAreaDO areaDO : areas){
            workAreaIDList.add(ObjectIdentifier.build(areaDO.getAreaID(),areaDO.getIdentifier()));
        }
        return workAreaIDList;
    }

}