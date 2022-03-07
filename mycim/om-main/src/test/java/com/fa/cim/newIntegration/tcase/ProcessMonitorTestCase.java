package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.controller.interfaces.mfg.IMfgInqController;
import com.fa.cim.controller.interfaces.transferManagementSystem.ITransferManagementSystemController;
import com.fa.cim.controller.lotstart.LotStartInqController;
import com.fa.cim.controller.processmonitor.ProcessMonitorController;
import com.fa.cim.controller.processmonitor.ProcessMonitorInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/27                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/11/27 15:44
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class ProcessMonitorTestCase {

    @Autowired
    private ProcessMonitorController processMonitorController;

    @Autowired
    private ProcessMonitorInqController processMonitorInqController;

    @Autowired
    private LotStartInqController lotStartInqController;

    @Autowired
    private IMfgInqController mfgInqController;

    @Autowired
    private ITransferManagementSystemController transferManagementSystemController;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }


    public void monitorBatchCreateReqCase(ObjectIdentifier firstLotID, ObjectIdentifier secondLotID) {
        Params.MonitorBatchCreateReqParams params = new Params.MonitorBatchCreateReqParams();
        params.setMonitorLotID(firstLotID);
        params.setUser(getUser());
        List<Infos.MonRelatedProdLots> list = new ArrayList<>();
        Infos.MonRelatedProdLots monRelatedProdLots = new Infos.MonRelatedProdLots();
        monRelatedProdLots.setProductLotID(secondLotID);
        monRelatedProdLots.setOperationNumber("");
        monRelatedProdLots.setRouteID(new ObjectIdentifier());
        monRelatedProdLots.setSiInfo("");
        list.add(monRelatedProdLots);
        params.setStrMonRelatedProdLots(list);
        processMonitorController.monitorBatchCreateReq(params);
    }

    public void monitorBatchCreateReqCase(ObjectIdentifier firstLotID, List<ObjectIdentifier> secondLotIDs) {
        Params.MonitorBatchCreateReqParams params = new Params.MonitorBatchCreateReqParams();
        params.setMonitorLotID(firstLotID);
        params.setUser(getUser());
        List<Infos.MonRelatedProdLots> list = new ArrayList<>();
        if (CimArrayUtils.getSize(secondLotIDs) > 0){
            for (ObjectIdentifier secondLotID : secondLotIDs) {
                Infos.MonRelatedProdLots monRelatedProdLots = new Infos.MonRelatedProdLots();
                monRelatedProdLots.setProductLotID(secondLotID);
                monRelatedProdLots.setOperationNumber("");
                monRelatedProdLots.setRouteID(new ObjectIdentifier());
                monRelatedProdLots.setSiInfo("");
                list.add(monRelatedProdLots);
            }
        }
        params.setStrMonRelatedProdLots(list);
        processMonitorController.monitorBatchCreateReq(params);
    }

    public List<Infos.MonitorGroups> getMonitorProdLotsRelationCase(ObjectIdentifier lotID,ObjectIdentifier cassetteID) {
        Params.MonitorBatchRelationInqParams params = new Params.MonitorBatchRelationInqParams();
        params.setUser(getUser());
        params.setCassetteID(cassetteID);
        params.setLotID(lotID);
        List<Infos.MonitorGroups> result = (List<Infos.MonitorGroups>) processMonitorInqController.monitorBatchRelationInq(params).getBody();
        return result;
    }

    public void monitorBatchDeleteReqCase(ObjectIdentifier cancelMonitorLotID) {
        Params.MonitorBatchDeleteReqParams params = new Params.MonitorBatchDeleteReqParams();
        params.setUser(getUser());
        params.setMonitorLotID(cancelMonitorLotID);
        processMonitorController.monitorBatchDeleteReq(params);
    }

    public Response npwCarrierReserveReq(Params.NPWCarrierReserveReqParams params){
        params.setOpeMemo("");
        params.setUser(getUser());
        return transferManagementSystemController.npwCarrierReserveReq(params);
    }

    public Response subLotTypeIdListInq(String lotType){
        Params.SubLotTypeListInqParams params = new Params.SubLotTypeListInqParams();
        params.setLotType(lotType);
        params.setUser(getUser());
        return mfgInqController.subLotTypeIdListInq(params);
    }

    public Response sourceLotListInq(ObjectIdentifier productID){
        Params.SourceLotListInqParams params = new Params.SourceLotListInqParams();
        params.setProductID(productID);
        params.setUser(getUser());
        return lotStartInqController.sourceLotListInq(params);
    }

    public Response autoCreateMonitorForInProcessLotReq(Params.AutoCreateMonitorForInProcessLotReqParams params){
        params.setClaimMemo("");
        params.setUser(getUser());
        return processMonitorController.autoCreateMonitorForInProcessLotReq(params);
    }
}
