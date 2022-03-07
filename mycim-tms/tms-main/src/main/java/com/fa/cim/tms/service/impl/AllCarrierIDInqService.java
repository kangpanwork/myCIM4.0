package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.method.ICassetteMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.service.IAllCarrierIDInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
@Slf4j
@Service
public class AllCarrierIDInqService implements IAllCarrierIDInqService {
    @Autowired
    private ICassetteMethod cassetteMethod;

    public Results.AllCarrierIDInquiryResult sxAllCarrierIDInq(Infos.ObjCommon objCommon, Params.AllCarrierIDInquiryParam allCarrierIDInquiryParam) {
        Results.AllCarrierIDInquiryResult result = new Results.AllCarrierIDInquiryResult();
        List<ObjectIdentifier> cassetteAllDRList = cassetteMethod.cassetteAllDR(objCommon);
        List<ObjectIdentifier> castList = new ArrayList<>();
        Optional.ofNullable(cassetteAllDRList).ifPresent(castList::addAll);
        result.setCarrierSequenceData(castList);
        return result;
    }

    @Override
    public Results.AllCarrierIDInquiryResult sxRtmsAllCarrierIDInq(Infos.ObjCommon objCommon, Params.AllCarrierIDInquiryParam allCarrierIDInquiryParam) {
        Results.AllCarrierIDInquiryResult result = new Results.AllCarrierIDInquiryResult();
        List<ObjectIdentifier> cassetteAllDRList = cassetteMethod.rtmsCassetteAllDR(objCommon);
        List<ObjectIdentifier> castList = new ArrayList<>();
        Optional.ofNullable(cassetteAllDRList).ifPresent(castList::addAll);
        result.setCarrierSequenceData(castList);
        return result;
    }
}
