package com.fa.cim.newIntegration.durable.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.tms.TransferManagementSystemController;
import com.fa.cim.controller.tms.TransferManagementSystemInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.common.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description:
 * <p>CarrierStockInOutCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/2/25/025   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/2/25/025 12:40
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class CarrierStockInOutCase {
    @Autowired
    private TestUtils testUtils;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private TransferManagementSystemInqController transferManagementSystemInqController;

    @Autowired
    private TransferManagementSystemController transferManagementSystemController;

    @Autowired
    private RetCodeConfig retCodeConfig;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public ObjectIdentifier emptyID = ObjectIdentifier.build(null,null);
    public ObjectIdentifier carrierID = new ObjectIdentifier("CRUP0001","FRCAST.81057531304862583");
    public ObjectIdentifier stockerID = new ObjectIdentifier("SHELF01","OMSTOCKER.29978739629766351");

    public void DRB2_4_1_Stockin(){
        //【Step 1】stock in
        this.stockMove("HI");
        //【Step 2】Check the result
        Results.StockerInfoInqResult stockerInfo = this.getStockerInfo();
        List<Infos.CarrierInStocker> strCarrierInStocker = stockerInfo.getStrCarrierInStocker();
        if (!CimStringUtils.equals(strCarrierInStocker.get(0).getCassetteID().getValue(),"CRUP0001")){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void DRB2_4_2_CarrierNoTransferStatusStockin(){
        //【Step 1】stock in
        Params.CarrierTransferStatusChangeRptParams params = new Params.CarrierTransferStatusChangeRptParams();
        params.setUser(getUser());
        params.setMachineID(stockerID);
        params.setCarrierID(ObjectIdentifier.buildWithValue("CRUP0540"));
        params.setManualInFlag(false);
        params.setXferStatus("HI");
        params.setPortID(emptyID);
        params.setZoneID("");
        params.setShelfType("");
        params.setTransferStatusChangeTimeStamp(null);
        params.setClaimMemo("");
        transferManagementSystemController.carrierTransferStatusChangeRpt(params);
        //【Step 2】Check the result
        Results.StockerInfoInqResult stockerInfo = this.getStockerInfo();
        List<Infos.CarrierInStocker> strCarrierInStocker = stockerInfo.getStrCarrierInStocker();
        if (!CimStringUtils.equals(strCarrierInStocker.get(0).getCassetteID().getValue(),"CRUP0540")){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void DRB2_4_3_CarrierMIStatusStockin(){
        //【Step 1】stock in
        Params.CarrierTransferStatusChangeRptParams params = new Params.CarrierTransferStatusChangeRptParams();
        params.setUser(getUser());
        params.setMachineID(stockerID);
        params.setCarrierID(ObjectIdentifier.buildWithValue("CRUP0070"));
        params.setManualInFlag(false);
        params.setXferStatus("HI");
        params.setPortID(emptyID);
        params.setZoneID("");
        params.setShelfType("");
        params.setTransferStatusChangeTimeStamp(null);
        params.setClaimMemo("");
        transferManagementSystemController.carrierTransferStatusChangeRpt(params);
        //【Step 2】Check the result
        Results.StockerInfoInqResult stockerInfo = this.getStockerInfo();
        List<Infos.CarrierInStocker> strCarrierInStocker = stockerInfo.getStrCarrierInStocker();
        if (!CimStringUtils.equals(strCarrierInStocker.get(0).getCassetteID().getValue(),"CRUP0070")){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void DRB2_4_4_CarrierMoStatusStockin(){
        //【Step 1】stock in
        Params.CarrierTransferStatusChangeRptParams params = new Params.CarrierTransferStatusChangeRptParams();
        params.setUser(getUser());
        params.setMachineID(stockerID);
        params.setCarrierID(ObjectIdentifier.buildWithValue("CRUP0041"));
        params.setManualInFlag(false);
        params.setXferStatus("HI");
        params.setPortID(emptyID);
        params.setZoneID("");
        params.setShelfType("");
        params.setTransferStatusChangeTimeStamp(null);
        params.setClaimMemo("");
        transferManagementSystemController.carrierTransferStatusChangeRpt(params);
        //【Step 2】Check the result
        Results.StockerInfoInqResult stockerInfo = this.getStockerInfo();
        List<Infos.CarrierInStocker> strCarrierInStocker = stockerInfo.getStrCarrierInStocker();
        if (!CimStringUtils.equals(strCarrierInStocker.get(0).getCassetteID().getValue(),"CRUP0041")){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void DRB2_4_5_CarrierEOStatusStockin(){
        //【Step 1】stock in
        Params.CarrierTransferStatusChangeRptParams params = new Params.CarrierTransferStatusChangeRptParams();
        params.setUser(getUser());
        params.setMachineID(stockerID);
        params.setCarrierID(ObjectIdentifier.buildWithValue("CRUP0067"));
        params.setManualInFlag(false);
        params.setXferStatus("HI");
        params.setPortID(emptyID);
        params.setZoneID("");
        params.setShelfType("");
        params.setTransferStatusChangeTimeStamp(null);
        params.setClaimMemo("");
        transferManagementSystemController.carrierTransferStatusChangeRpt(params);
        //【Step 2】Check the result
        Results.StockerInfoInqResult stockerInfo = this.getStockerInfo();
        List<Infos.CarrierInStocker> strCarrierInStocker = stockerInfo.getStrCarrierInStocker();
        if (!CimStringUtils.equals(strCarrierInStocker.get(0).getCassetteID().getValue(),"CRUP0067")){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void DRB2_4_6_CarrierEIStatusStockin(){
        //【Step 1】stock in
        Params.CarrierTransferStatusChangeRptParams params = new Params.CarrierTransferStatusChangeRptParams();
        params.setUser(getUser());
        params.setMachineID(stockerID);
        params.setCarrierID(ObjectIdentifier.buildWithValue("CRUP0104"));
        params.setManualInFlag(false);
        params.setXferStatus("HI");
        params.setPortID(emptyID);
        params.setZoneID("");
        params.setShelfType("");
        params.setTransferStatusChangeTimeStamp(null);
        params.setClaimMemo("");
        try {
            transferManagementSystemController.carrierTransferStatusChangeRpt(params);
        }catch (ServiceException e){
            if (e.getCode() != 905){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
    }

    public void DRB2_4_7_StockOut(){
        //【Step 1】stock in
        this.stockMove("HI");
        //【Step 2】stock out
        this.stockMove("HO");
        //【Step 3】Check the result
        Results.StockerInfoInqResult stockerInfo = this.getStockerInfo();
        List<Infos.CarrierInStocker> strCarrierInStocker = stockerInfo.getStrCarrierInStocker();
    }

    public Results.StockerInfoInqResult getStockerInfo(){
        Params.StockerInfoInqInParams params = new Params.StockerInfoInqInParams();
        params.setUser(getUser());
        params.setDetailFlag(false);
        params.setMachineID(stockerID);
        Response response = transferManagementSystemInqController.stockerInfoInq(params);
        Results.StockerInfoInqResult stockerInfoInqResult = (Results.StockerInfoInqResult) response.getBody();
        return stockerInfoInqResult;
    }

    public void stockMove(String moveDirection){
        Params.CarrierTransferStatusChangeRptParams params = new Params.CarrierTransferStatusChangeRptParams();
        params.setUser(getUser());
        params.setMachineID(stockerID);
        params.setCarrierID(carrierID);
        params.setManualInFlag(false);
        params.setXferStatus(moveDirection);
        params.setPortID(emptyID);
        params.setZoneID("");
        params.setShelfType("");
        params.setTransferStatusChangeTimeStamp(null);
        params.setClaimMemo("");
        transferManagementSystemController.carrierTransferStatusChangeRpt(params);
    }
}
