package com.fa.cim.newIntegration.durable.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.durable.DurableController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.controller.tms.TransferManagementSystemController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>DurableTransferStateCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/4/004   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/4/004 17:07
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class DurableTransferStateCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private DurableController durableController;

    @Autowired
    private DurableInqController durableInqController;

    @Autowired
    private TransferManagementSystemController systemController;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public ObjectIdentifier emptyID = ObjectIdentifier.build(null,null);

    public void ChangeXferStateToMO(){
        //【STEP1】STB
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        //【step2】MO
        this.durableMOMI(cassetteID,"MO");
    }

    public void ChangeXferStateToMI(){
        //【STEP1】STB
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        //【step2】MI
        this.durableMOMI(cassetteID,"MI");
    }

    public void durableMOMI(ObjectIdentifier carrierID,String XferStatus){
        Params.CarrierTransferStatusChangeRptParams params = new Params.CarrierTransferStatusChangeRptParams();
        params.setUser(getUser());
        params.setCarrierID(carrierID);
        params.setXferStatus(XferStatus);
        if (CimStringUtils.equals(XferStatus,"MO")){
            params.setManualInFlag(false);
        }else if (CimStringUtils.equals(XferStatus,"MI")){
            params.setManualInFlag(true);
        }
        params.setMachineID(ObjectIdentifier.build("STK0102","OMSTOCKER.67761921777089503"));
        params.setPortID(emptyID);
        params.setZoneID("");
        params.setShelfType("");
        params.setTransferStatusChangeTimeStamp(null);
        params.setClaimMemo("");
        systemController.carrierTransferStatusChangeRpt(params);
    }
}
