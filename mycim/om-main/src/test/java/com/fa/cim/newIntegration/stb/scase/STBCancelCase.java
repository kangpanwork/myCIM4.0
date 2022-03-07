package com.fa.cim.newIntegration.stb.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.LotOperation.scase.LotHoldCase;
import com.fa.cim.newIntegration.tcase.StbTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Neko
 * @date 2019/9/11 15:30
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class STBCancelCase {
    @Autowired
    private STBCase stbCase;

    @Autowired
    private StbTestCase stbTestCase;

    @Autowired
    private LotHoldCase lotHoldCase;

    public Response STBCancel_NormalCase() {
        //【step 1】STB one lot and get the lot ID.
        Response response = stbCase.STB_PreparedLot();
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) response.getBody();
        ObjectIdentifier lotID = waferLotStartReqResult.getLotID();
        log.info("【step 1】: new lot has been STBed");

        // step 2: STB Cancel the Lot
        log.info("【step 2】: Start to STB Cancel");
        return stbTestCase.stbLotCancelReqCase(lotID);
    }


    public Response STBCancel_OnHoldLot() {
        //【step 1】STB one lot
        Response response = stbCase.STB_PreparedLot();
        Results.WaferLotStartReqResult body = (Results.WaferLotStartReqResult) response.getBody();
        ObjectIdentifier lotID = body.getLotID();
        log.info("【step 1】: new lot has been STBed, lotID :"+lotID);

        //【step2】lotHold
        final String reasonCode = "SOHL";
        final String reasonableOperation = "C";
        response = lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);
        Validations.isSuccessWithException(response);
        log.info("【step 2】: lot is now On Hold");

        // 【step 3】: STB Cancel the Lot
        try{
            log.info("【step 3】: Start to STB Cancel the OnHold Lot");
            return stbTestCase.stbLotCancelReqCase(lotID);
        }catch (ServiceException ex){
            log.info(String.format(ex.getMessage(),lotID.getValue(),"HoldLot"));
            return null;
        }
    }

    public Response STBCancel_ProcessingLot() {
     /* TODO This case requires a lot condition which is Operation Start
           This will done later.

        //【step 1】STB one lot
      */
        Response response = stbCase.STB_PreparedLot();
        Results.WaferLotStartReqResult body = (Results.WaferLotStartReqResult) response.getBody();
        ObjectIdentifier lotID = body.getLotID();
        log.info("【step 1】: new lot has been STBed, lotID :"+lotID);

        //【step 2】above Lot Operation Start

        // 【step 3】: STB Cancel the Lot
      /*   try{
            log.info("【step 3】: Start to STB Cancel the OnHold Lot");
            return stbTestCase.stbLotCancelReqCase(lotID);
        }catch (ServiceException ex){
            log.info(ex.getMessage());
            return null;
        }*/
      return null;
    }
}
