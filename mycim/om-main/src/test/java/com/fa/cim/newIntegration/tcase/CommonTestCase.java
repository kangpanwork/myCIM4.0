package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.controller.durable.DurableController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.controller.interfaces.transferManagementSystem.ITransferManagementSystemInqController;
import com.fa.cim.controller.lot.LotInqController;
import com.fa.cim.controller.system.SystemInqController;
import com.fa.cim.controller.tms.TransferManagementSystemController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/3          ********            lightyh                create file
 *
 * @author: light
 * @date: 2019/12/3 17:44
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class CommonTestCase {

    @Autowired
    private SystemInqController systemInqController;

    @Autowired
    private LotInqController processFlowInqController;

    @Autowired
    private TransferManagementSystemController transferManagementSystemController;

    @Autowired
    private ITransferManagementSystemInqController transferManagementSystemInqController;

    @Autowired
    private DurableController durableController;

    @Autowired
    private DurableInqController durableInqController;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        return user;
    }

    public List<Infos.ReasonCodeAttributes> getReasonCodeListByCategoryInq(String codeCategory) {
        Params.ReasonCodeListByCategoryInqParams reasonCodeListByCategoryInqParams = new Params.ReasonCodeListByCategoryInqParams();
        reasonCodeListByCategoryInqParams.setCodeCategory(codeCategory);
        reasonCodeListByCategoryInqParams.setUser(getUser());
        List<Infos.ReasonCodeAttributes> reasonCodeAttributesList = (List<Infos.ReasonCodeAttributes>) systemInqController.reasonCodeListByCategoryInq(reasonCodeListByCategoryInqParams).getBody();
        return reasonCodeAttributesList;
    }

    public List<Infos.ConnectedRouteList> getConnectedRouteList(ObjectIdentifier lotID, ObjectIdentifier cassetteID, String routeType) {
        Params.MultiPathListInqParams multiPathListInqParams = new Params.MultiPathListInqParams();
        multiPathListInqParams.setUser(getUser());
        multiPathListInqParams.setCassetteID(cassetteID);
        multiPathListInqParams.setLotID(lotID);
        multiPathListInqParams.setRouteType(routeType);
        return (List<Infos.ConnectedRouteList>) processFlowInqController.multiPathListInq(multiPathListInqParams).getBody();
    }

    public Response lotCassetteXferStatusChange(ObjectIdentifier cassette, String transferStatus) {
        Infos.StockerInfo stocker = this.getStocker();
        Params.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams = new Params.CarrierTransferStatusChangeRptParams();
        carrierTransferStatusChangeRptParams.setUser(getUser());
        carrierTransferStatusChangeRptParams.setMachineID(getStocker().getStockerID());
        carrierTransferStatusChangeRptParams.setCarrierID(cassette);
        carrierTransferStatusChangeRptParams.setMachineID(stocker.getStockerID());
        boolean manualInFlag = true;
        if (transferStatus.equals("MO")) {
            manualInFlag = false;
        }
        carrierTransferStatusChangeRptParams.setManualInFlag(manualInFlag);
        carrierTransferStatusChangeRptParams.setXferStatus(transferStatus);
        return transferManagementSystemController.carrierTransferStatusChangeRpt(carrierTransferStatusChangeRptParams);
    }

    public List<Infos.StockerInfo> getStockerInfoList() {
        Params.StockerListInqInParams params = new Params.StockerListInqInParams();
        params.setUser(getUser());
        params.setAvailFlag(true);
        params.setStockerType("Auto");
        Response response = transferManagementSystemInqController.stockerListInq(params);
        return ((Results.StockerListInqResult) response.getBody()).getStrStockerInfo();
    }

    public Infos.StockerInfo getStocker() {
        //【step1】get stocker list
        List<Infos.StockerInfo> stockerInfoList = this.getStockerInfoList();
        Infos.StockerInfo stockerInfo = stockerInfoList.get(0);
        for (Infos.StockerInfo tmpStockerInfo : stockerInfoList) {
            if (CimObjectUtils.equalsWithValue(tmpStockerInfo.getStockerID(), "STK0101")) {
                stockerInfo = tmpStockerInfo;
                break;
            }
        }
        return stockerInfo;
    }

    public void changeDurableStatus(ObjectIdentifier durableID, String status, String durableCategory) {
        String durableStatus = null;
        ObjectIdentifier durableSubStatus = null;
        if (durableCategory.equals("Cassette")) {
            Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult = this.carrierDetailInfoInq(durableID);
            durableStatus = carrierDetailInfoInqResult.getCassetteStatusInfo().getCassetteStatus();
            durableSubStatus = carrierDetailInfoInqResult.getCassetteStatusInfo().getDurableSubStatus();
        } else if (durableCategory.equals("Reticle")) {
            Results.ReticleDetailInfoInqResult reticleDetailInfoInqResult = this.reticleDetailInfoInq(durableID);
            durableStatus = reticleDetailInfoInqResult.getReticleStatusInfo().getReticleStatus();
            durableSubStatus = reticleDetailInfoInqResult.getReticleStatusInfo().getDurableSubStatus();
        }
        assert durableStatus != null;
        this.changeDurableStatusImp(durableID, durableCategory, status, durableStatus, durableSubStatus);

    }

    private void changeDurableStatusImp(ObjectIdentifier durableID, String durableCategory, String status, String durableStatus, ObjectIdentifier durableSubStatus) {
        if (!durableStatus.equals(status)) {
            List<Infos.CandidateDurableSubStatusDetail> candidateDurableSubStatusDetailList = this.durableSubStatusSelectionInq(durableID, durableCategory, false);
            Infos.CandidateDurableSubStatusDetail candidateDurableSubStatusDetailSelected = null;
            for (Infos.CandidateDurableSubStatusDetail candidateDurableSubStatusDetail : candidateDurableSubStatusDetailList) {
                if (candidateDurableSubStatusDetail.getDurableStatus().equals(status)) {
                    candidateDurableSubStatusDetailSelected = candidateDurableSubStatusDetail;
                    break;
                }
            }
            if (candidateDurableSubStatusDetailSelected == null) {
                this.durableStatusChangeReq(durableCategory, candidateDurableSubStatusDetailList.get(0).getDurableStatus(), candidateDurableSubStatusDetailList.get(0).getCandidateDurableSubStatuses().get(0).getDurableSubStatus(),
                        durableStatus, durableSubStatus, durableID);
                this.changeDurableStatus(durableID, status, durableCategory);
            } else {
                this.durableStatusChangeReq(durableCategory, status, candidateDurableSubStatusDetailSelected.getCandidateDurableSubStatuses().get(0).getDurableSubStatus(),
                        durableStatus, durableSubStatus, durableID);
            }
        }
    }

    public Results.CarrierDetailInfoInqResult carrierDetailInfoInq(ObjectIdentifier cassetteID) {
        Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams = new Params.CarrierDetailInfoInqParams();
        carrierDetailInfoInqParams.setCassetteID(cassetteID);
        carrierDetailInfoInqParams.setDurableOperationInfoFlag(true);
        carrierDetailInfoInqParams.setDurableWipOperationInfoFlag(true);
        carrierDetailInfoInqParams.setUser(getUser());
        return (Results.CarrierDetailInfoInqResult) durableInqController.carrierDetailInfoInq(carrierDetailInfoInqParams).getBody();
    }

    public List<Infos.CandidateDurableSubStatusDetail> durableSubStatusSelectionInq(ObjectIdentifier cassetteID, String durableCategory, boolean allInquiryFlag) {
        Params.DurableSubStatusSelectionInqParams durableSubStatusSelectionInqParams = new Params.DurableSubStatusSelectionInqParams();
        durableSubStatusSelectionInqParams.setAllInquiryFlag(allInquiryFlag);
        durableSubStatusSelectionInqParams.setDurableCategory(durableCategory);
        durableSubStatusSelectionInqParams.setDurableID(cassetteID);
        durableSubStatusSelectionInqParams.setUser(getUser());
        return (List<Infos.CandidateDurableSubStatusDetail>) durableInqController.durableSubStatusSelectionInq(durableSubStatusSelectionInqParams).getBody();
    }

    public Results.ReticleDetailInfoInqResult getReticleInfomation(ObjectIdentifier reticleID) {
        Params.ReticleDetailInfoInqParams reticleDetailInfoInqParams = new Params.ReticleDetailInfoInqParams();
        reticleDetailInfoInqParams.setUser(getUser());
        reticleDetailInfoInqParams.setDurableOperationInfoFlag(true);
        reticleDetailInfoInqParams.setDurableWipOperationInfoFlag(true);
        reticleDetailInfoInqParams.setReticleID(reticleID);
        return (Results.ReticleDetailInfoInqResult) durableInqController.reticleDetailInfoInq(reticleDetailInfoInqParams).getBody();
    }

    public Results.ReticleDetailInfoInqResult reticleDetailInfoInq(ObjectIdentifier reticleID) {
        Params.ReticleDetailInfoInqParams reticleDetailInfoInqParams = new Params.ReticleDetailInfoInqParams();
        reticleDetailInfoInqParams.setDurableWipOperationInfoFlag(true);
        reticleDetailInfoInqParams.setDurableOperationInfoFlag(true);
        reticleDetailInfoInqParams.setReticleID(reticleID);
        reticleDetailInfoInqParams.setUser(getUser());
        return (Results.ReticleDetailInfoInqResult) durableInqController.reticleDetailInfoInq(reticleDetailInfoInqParams).getBody();
    }

    public void durableStatusChangeReq(String durableCategory, String toStatus, ObjectIdentifier toDurableSubStatus, String fromStatus, ObjectIdentifier fromDurablSubStatus, ObjectIdentifier durableID) {
        Params.MultiDurableStatusChangeReqParams multiDurableStatusChangeReqParams = new Params.MultiDurableStatusChangeReqParams();
        multiDurableStatusChangeReqParams.setUser(getUser());
        Infos.MultiDurableStatusChangeReqInParm multiDurableStatusChangeReqInParm = new Infos.MultiDurableStatusChangeReqInParm();
        multiDurableStatusChangeReqParams.setParm(multiDurableStatusChangeReqInParm);
        multiDurableStatusChangeReqInParm.setDurableCategory(durableCategory);
        multiDurableStatusChangeReqInParm.setDurableStatus(toStatus);
        multiDurableStatusChangeReqInParm.setDurableSubStatus(toDurableSubStatus);
        List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos = new ArrayList<>();
        multiDurableStatusChangeReqInParm.setStatusChangeDurableInfos(statusChangeDurableInfos);
        Infos.StatusChangeDurableInfo statusChangeDurableInfo = new Infos.StatusChangeDurableInfo();
        statusChangeDurableInfos.add(statusChangeDurableInfo);
        statusChangeDurableInfo.setDurableID(durableID);
        statusChangeDurableInfo.setDurableStatus(fromStatus);
        statusChangeDurableInfo.setDurableSubStatus(fromDurablSubStatus);
        durableController.multiDurableStatusChangeReq(multiDurableStatusChangeReqParams);
    }

    public List<Infos.StoredReticle> reticleEquipmentIn(ObjectIdentifier reticleGroup, ObjectIdentifier equipmentID, int needEquipmentInCount) {
        Results.ReticleListInqResult reticleListInqResult = this.getReticleList(reticleGroup);
        List<Infos.FoundReticle> strFoundReticle = reticleListInqResult.getStrFoundReticle();
        List<Infos.StoredReticle> storedReticleList = new ArrayList<>();
        for (Infos.FoundReticle foundReticle : strFoundReticle) {
            if (CimObjectUtils.isEmptyWithValue(foundReticle.getReticleStatusInfo().getEquipmentID())
                    && (foundReticle.getReticleStatusInfo().getTransferStatus().equals("EO") || foundReticle.getReticleStatusInfo().getTransferStatus().equals("-"))) {
                Infos.StoredReticle storedReticle = new Infos.StoredReticle();
                storedReticle.setReticleGroupID(reticleGroup);
                storedReticle.setReticleID(foundReticle.getReticleID());
                storedReticle.setStatus(foundReticle.getReticleStatusInfo().getReticleStatus());
                storedReticleList.add(storedReticle);
                if (storedReticleList.size() == needEquipmentInCount) {
                    break;
                }
            }
        }
        List<Infos.XferReticle> xferReticleList = new ArrayList<>();
        for (Infos.StoredReticle storedReticle : storedReticleList) {
            Infos.XferReticle xferReticle = new Infos.XferReticle();
            xferReticleList.add(xferReticle);
            xferReticle.setReticleID(storedReticle.getReticleID());
            xferReticle.setTransferStatus("EI");
        }
        this.reticleTransferStatusChangeRpt(equipmentID, xferReticleList);
        return storedReticleList;
    }

    public Results.ReticleListInqResult getReticleList(ObjectIdentifier reticleGroup) {
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setReticleGroupID(reticleGroup);
        reticleListInqParams.setWhiteDefSearchCriteria("All");
        return (Results.ReticleListInqResult) durableInqController.reticleListInq(reticleListInqParams).getBody();
    }

    public void reticleTransferStatusChangeRpt(ObjectIdentifier equipmentID, List<Infos.XferReticle> xferReticleList) {
        Params.ReticleTransferStatusChangeRptParams reticleTransferStatusChangeRptParams = new Params.ReticleTransferStatusChangeRptParams();
        reticleTransferStatusChangeRptParams.setUser(getUser());
        reticleTransferStatusChangeRptParams.setEquipmentID(equipmentID);
        reticleTransferStatusChangeRptParams.setStrXferReticle(xferReticleList);
        durableController.reticleTransferStatusChangeRpt(reticleTransferStatusChangeRptParams);
    }

    public Response dynamicPathListInqCase(String processDefinitionType, String routeIDKey) {
        Params.DynamicPathListInqParams params = new Params.DynamicPathListInqParams();
        params.setActiveVersionFlag(false);
        params.setProcessDefinitionType(processDefinitionType);
        params.setUser(getUser());
        params.setRouteIDKey(routeIDKey);
        return processFlowInqController.dynamicPathListInq(params);
    }

    public Response processFlowOperationListInq(ObjectIdentifier routeID) {
        Params.ProcessFlowOperationListInqParams params = new Params.ProcessFlowOperationListInqParams();
        params.setUser(getUser());
        params.setRouteID(routeID);
        params.setSearchCount(0);
        return processFlowInqController.processFlowOperationListInq(params);
    }

    public List<Infos.RouteIndexInformation> mainProcessFlowListInq(){
        Params.MainProcessFlowListInqParams mainProcessFlowListInqParams = new Params.MainProcessFlowListInqParams();
        mainProcessFlowListInqParams.setUser(getUser());
        mainProcessFlowListInqParams.setActiveShowFlag(false);
        return (List<Infos.RouteIndexInformation>) processFlowInqController.mainProcessFlowListInq(mainProcessFlowListInqParams).getBody();

    }
}