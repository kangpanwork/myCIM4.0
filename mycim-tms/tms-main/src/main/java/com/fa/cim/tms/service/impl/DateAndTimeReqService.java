package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IDateAndTimeReqService;
import com.fa.cim.tms.utils.DateUtils;
import org.springframework.stereotype.Service;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 13:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
public class DateAndTimeReqService implements IDateAndTimeReqService {

    public Results.DateAndTimeReqResult sxDateAndTimeReq(Infos.ObjCommon objCommon) {

        Results.DateAndTimeReqResult result = new Results.DateAndTimeReqResult();
        result.setSystemTime(DateUtils.getCurrentTimeStamp().toString());
        return result;
    }
}
