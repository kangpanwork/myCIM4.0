package com.fa.cim.service.ocap.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.IOcapMethod;
import com.fa.cim.service.ocap.IOcapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/1/21          ********            lightyh                create file
 *
 * @author: hd
 * @date: 2021/1/21 17:31
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class OcapService implements IOcapService {

    @Autowired
    private IOcapMethod ocapMethod;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public void ocapUpdateRpt(Infos.ObjCommon objCommon, Params.OcapReqParams params) {
        //step1 - check input param
        if (log.isDebugEnabled()) {
            log.debug("step1 - check input param");
        }
        Validations.check(CimStringUtils.isEmpty(params.getOcapNo())
                        || ObjectIdentifier.isEmptyWithValue(params.getLotID())
                        || ObjectIdentifier.isEmptyWithValue(params.getEquipmentID())
                        || ObjectIdentifier.isEmptyWithValue(params.getRecipeID())
                        || CimStringUtils.isEmpty(params.getWaferList()),
                retCodeConfig.getInvalidParameter());
        //step2 - confirm addMeasure and reMeasure can not handle at the same time
        if (log.isDebugEnabled()) {
            log.debug("step2 - confirm addMeasure and reMeasure can not handle at the same time");
        }
        Validations.check(CimBooleanUtils.isTrue(params.getAddMeasureFlag())
                        && CimBooleanUtils.isTrue(params.getReMeasureFlag()),
                retCodeConfigEx.getOcapRemeasureAndAddmeasureConflict());

        //step5 - call ocapUpdateRpt
        if (log.isDebugEnabled()) {
            log.debug("step3 - call ocapUpdateRpt");
        }
        ocapMethod.ocapUpdateRpt(objCommon, params);
    }
}