package com.fa.cim.service.probe.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IDurableMethod;
import com.fa.cim.method.IFixtureMethod;
import com.fa.cim.method.IStockerMethod;
import com.fa.cim.method.impl.DurableMethod;
import com.fa.cim.service.probe.IProbeInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 25708
 * @version 6.0.0
 * @date 2020/11/5
 **/
@OmService
@Slf4j
public class ProbeInqServiceImpl implements IProbeInqService {

    @Autowired
    private IFixtureMethod fixtureMethod;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private IStockerMethod stockerMethod;

    @Override
    public Results.FixtureListInqResult sxProbeListInq(Infos.ObjCommon objCommon,
                                                       Params.ProbeListInqParams probeListInqParams) {
        Results.FixtureListInqResult strFixtureListInqResult = fixtureMethod.fixtureFillInTxPDQ001DR(
                objCommon, probeListInqParams.getEquipmentID(), probeListInqParams.getLotID(),
                probeListInqParams.getFixtureID(), probeListInqParams.getFixturePartNumber(),
                probeListInqParams.getFixtureGroupID(), probeListInqParams.getFixtureCategoryID(),
                probeListInqParams.getFixtureStatus(), probeListInqParams.getMaxRetrieveCount());

        return strFixtureListInqResult;
    }

    @Override
    public Results.FixtureIDListInqResult sxProbeIDListInq(Infos.ObjCommon objCommon,
                                                           Params.ProbeIDListInqParams probeIDListInqParams) {

        List<ObjectIdentifier> durableIDList = durableMethod.durableIDGetDR(objCommon,
                                                                                        BizConstant.SP_DURABLECAT_FIXTURE);
        Results.FixtureIDListInqResult resultFixtureIDListInqResult = new Results.FixtureIDListInqResult();
       /*List<String> valueList =   durableIDList.stream().map(i->i.getValue()).collect(Collectors.toList());
        for(String value:valueList){
            resultFixtureIDListInqResult.getObjectIdentifiers().add(ObjectIdentifier.buildWithValue(value));
        }*/
       resultFixtureIDListInqResult.setObjectIdentifiers(durableIDList);
        return resultFixtureIDListInqResult;
    }

    @Override
    public Results.FixtureStatusInqResult sxProbeStatusInq(Infos.ObjCommon objCommon,ObjectIdentifier objectIdentifier) {
        Results.FixtureStatusInqResult fixtureStatusInqResult = fixtureMethod.fixtureFillInTxPDQ002DR(objCommon, objectIdentifier);
        return fixtureStatusInqResult;
    }

    @Override
    public Results.FixtureStockerInfoInqResult sxProbeStockerInfoInq(Infos.ObjCommon objCommon,
                                                                     ObjectIdentifier objectIdentifier) {
        Results.FixtureStockerInfoInqResult fixtureStockerInfoInqResult = stockerMethod.stockerFillInTxPDQ003DR(objCommon,objectIdentifier);
        return fixtureStockerInfoInqResult;
    }

    @Override
    public Results.FixtureGroupIDListInqResult sxProbeGroupIDListInq(Infos.ObjCommon objCommon) {
        Results.FixtureGroupIDListInqResult fixtureGroupIDListInqResult = new Results.FixtureGroupIDListInqResult();
        List<ObjectIdentifier> objectIdentifiers = durableMethod.durableCapabilityIDGetDR(objCommon,BizConstant.SP_DURABLECAT_FIXTURE);
        fixtureGroupIDListInqResult.setObjectIdentifiers(objectIdentifiers);
        return fixtureGroupIDListInqResult;
    }
}
