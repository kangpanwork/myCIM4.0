package com.fa.cim.newIntegration.common;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.durable.DurableController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import com.fa.cim.newIntegration.tcase.OperationSkipTestCase;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/5        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/5 9:51
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class TestUtils {
    @Autowired
    private STBCase stbCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private OperationSkipTestCase operationSkipTestCase;

    @Autowired
    private DurableInqController durableInqController;

    @Autowired
    private DurableController durableController;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN" ));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        return user;
    }

    public ObjectIdentifier stbAndSkip(TestInfos.StbInfo stbInfo, String operationNumber) {
        //【step1】stb one product lot
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】skip to operation number
        return this.skip(lotID, operationNumber);
    }

    public ObjectIdentifier skip(ObjectIdentifier lotID, String operationNumber) {
        if (CimStringUtils.isEmpty(operationNumber)) {
            return lotID;
        }

        //【step1】lot info
        Results.LotInfoInqResult lotInfoInqResult = this.getLotInfoByLotID(lotID);
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);

        //【step2】get lot operation
        String currentOperationNumber = lotInfo.getLotOperationInfo().getOperationNumber();
        boolean searchDirection = isLaterThan(operationNumber, currentOperationNumber);

        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, searchDirection).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Predicate<Infos.OperationNameAttributes> predicate = t -> CimStringUtils.equals(operationNumber, t.getOperationNumber());
        Infos.OperationNameAttributes operationNameAttributes = operationNameAttributesList1.stream().filter(predicate).findFirst().orElse(null);

        //【step4】skip
        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(currentOperationNumber);
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(searchDirection);
        skipReqParams.setLotID(lotID);
        if (operationNameAttributes != null) {
            skipReqParams.setOperationID(operationNameAttributes.getOperationID());
            skipReqParams.setOperationNumber(operationNameAttributes.getOperationNumber());
            skipReqParams.setProcessRef(operationNameAttributes.getProcessRef());
            skipReqParams.setRouteID(operationNameAttributes.getRouteID());
        }
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        Response response = operationSkipTestCase.operationSkip(skipReqParams);
        Validations.isSuccessWithException(response);
        return lotID;
    }

    public ObjectIdentifier findEmptyCassette(String cassetteCategory, String cassetteStatus) {
        Params.CarrierListInqParams carrierListInqParams = new Params.CarrierListInqParams();
        carrierListInqParams.setEmptyFlag(true);
        carrierListInqParams.setCassetteStatus(cassetteStatus);
        carrierListInqParams.setCassetteCategory(cassetteCategory);
        carrierListInqParams.setMaxRetrieveCount(300);
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setSize(10);
        searchCondition.setPage(1);
        carrierListInqParams.setSearchCondition(searchCondition);
        carrierListInqParams.setUser(this.getUser());
        Response response = durableInqController.carrierListInq(carrierListInqParams);
        Validations.isSuccessWithException(response);
        Results.CarrierListInq170Result carrierListInq170Result = (Results.CarrierListInq170Result) response.getBody();
        Predicate<Infos.FoundCassette> predicate = x -> CimObjectUtils.isEmptyWithValue(x.getEquipmentID());
        Optional<Infos.FoundCassette> first = carrierListInq170Result.getFoundCassette().getContent().stream().filter(predicate).findAny();
        if (first.isPresent()) {
            return first.get().getCassetteID();
        }
        return null;
    }

    public ObjectIdentifier getCassetteIDByLotID(ObjectIdentifier lotID) {
        //【step1】lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = this.getLotInfoByLotID(lotID);
        Infos.LotListInCassetteInfo lotListInCassetteInfo = lotInfoInqResult.getLotListInCassetteInfo();

        if (null == lotListInCassetteInfo) {
            //the lot is not in cassette
            return null;
        }
        return lotListInCassetteInfo.getCassetteID();
    }

    public ObjectIdentifier getControlJobIDByLotID(ObjectIdentifier lotID) {
        //【step1】lot info
        Results.LotInfoInqResult lotInfoInqResult = this.getLotInfoByLotID(lotID);
        List<Infos.LotInfo> lotInfoList = lotInfoInqResult.getLotInfoList();
        if (null == lotInfoList) {
            //the lot is not in cassette
            return null;
        }
        for (Infos.LotInfo lotInfo: lotInfoList) {
            if (CimObjectUtils.equalsWithValue(lotID, lotInfo.getLotBasicInfo().getLotID())) {
                return lotInfo.getLotControlJobInfo().getControlJobID();
            }
        }
        return null;
    }

    public Results.LotInfoInqResult getLotInfoByLotID(ObjectIdentifier lotID) {
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        return (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
    }


    public boolean isLaterThan(String operationNumber, String currentOperationNumber) {
        int opeNumber = Integer.parseInt(CimStringUtils.replace(operationNumber, ".", ""));
        int currentOpeNumber = Integer.parseInt(CimStringUtils.replace(currentOperationNumber, ".", ""));
        return opeNumber > currentOpeNumber;
    }

    public void changeCassetteStatus(ObjectIdentifier cassetteID, String cassetteStatus, ObjectIdentifier cassetteSubStatus) {
        /* if the cassetteSubStatus is null, then get by randomly */

        //【step2-1】drb/cassette_status/inq
        Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams = new Params.CarrierDetailInfoInqParams();
        carrierDetailInfoInqParams.setUser(this.getUser());
        carrierDetailInfoInqParams.setCassetteID(cassetteID);
        carrierDetailInfoInqParams.setDurableOperationInfoFlag(true);
        carrierDetailInfoInqParams.setDurableWipOperationInfoFlag(true);
        Results.CarrierDetailInfoInqResult body1 = (Results.CarrierDetailInfoInqResult) durableInqController.carrierDetailInfoInq(carrierDetailInfoInqParams).getBody();
        String currentCassetteStatus = body1.getCassetteStatusInfo().getCassetteStatus();
        ObjectIdentifier currentDurableSubStatus = body1.getCassetteStatusInfo().getDurableSubStatus();

        //【step2-2】drb/candidate_durable_sub_status/inq
        Params.DurableSubStatusSelectionInqParams durableSubStatusSelectionInqParams = new Params.DurableSubStatusSelectionInqParams();
        durableSubStatusSelectionInqParams.setUser(this.getUser());
        durableSubStatusSelectionInqParams.setAllInquiryFlag(false);
        durableSubStatusSelectionInqParams.setDurableCategory("Cassette");
        durableSubStatusSelectionInqParams.setDurableID(cassetteID);
        List<Infos.CandidateDurableSubStatusDetail> body = (List<Infos.CandidateDurableSubStatusDetail>) durableInqController.durableSubStatusSelectionInq(durableSubStatusSelectionInqParams).getBody();
        Predicate<Infos.CandidateDurableSubStatusDetail> predicate = p -> CimStringUtils.equals(p.getDurableStatus(), cassetteStatus);
        if ( CimObjectUtils.isEmpty(cassetteSubStatus)) {
            Infos.CandidateDurableSubStatusDetail candidateDurableSubStatusDetail = body.stream().filter(predicate).findFirst().orElse(null);
            if (candidateDurableSubStatusDetail != null) {
                cassetteSubStatus = candidateDurableSubStatusDetail.getCandidateDurableSubStatuses().get(0).getDurableSubStatus();
            }
        }

        //【stepp2-3】drb/durable_status_multi_change/req
        Params.MultiDurableStatusChangeReqParams multiDurableStatusChangeReqParams = new Params.MultiDurableStatusChangeReqParams();
        multiDurableStatusChangeReqParams.setUser(this.getUser());
        Infos.MultiDurableStatusChangeReqInParm parm = new Infos.MultiDurableStatusChangeReqInParm();
        parm.setDurableCategory("Cassette");
        parm.setDurableStatus(cassetteStatus);
        parm.setDurableSubStatus(cassetteSubStatus);
        List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos = new ArrayList<>();
        Infos.StatusChangeDurableInfo statusChangeDurableInfo = new Infos.StatusChangeDurableInfo();
        statusChangeDurableInfo.setDurableID(cassetteID);
        statusChangeDurableInfo.setDurableStatus(currentCassetteStatus);
        statusChangeDurableInfo.setDurableSubStatus(currentDurableSubStatus);
        statusChangeDurableInfos.add(statusChangeDurableInfo);
        parm.setStatusChangeDurableInfos(statusChangeDurableInfos);
        multiDurableStatusChangeReqParams.setParm(parm);
        durableController.multiDurableStatusChangeReq(multiDurableStatusChangeReqParams);
    }

}