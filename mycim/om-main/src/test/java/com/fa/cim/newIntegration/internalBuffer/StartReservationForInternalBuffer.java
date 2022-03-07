package com.fa.cim.newIntegration.internalBuffer;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.internalBuffer.scase.StartReservationForInternalBufferCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/26        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/11/26 10:38
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class StartReservationForInternalBuffer {
    @Autowired
    private StartReservationForInternalBufferCase startReservationForInternalBufferCase;

    /**     
     * description:
     *    1.[DIS4-1-27]If both of the flags show False, there should be no empty carrier in the Start Lot Reservation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4 
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_SingleLot() {
        startReservationForInternalBufferCase.startReservation_SingleLot();
    }

    /**     
     * description:
     *    1.[DIS4-1-2]Reserve two lots at one time
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4 
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_MutipleLotMutiplePort() {
        startReservationForInternalBufferCase.startReservation_MutipleLotMutiplePort();
    }

    /**     
     * description:
     *    1.[DIS4-1-5]In one Carrier, 25 wafers were all prepared, but only a few of them were STB.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4 
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_MutipleLotPerCarrier() {
        startReservationForInternalBufferCase.startReservation_MutipleLotPerCarrier();
    }

    /**     
     * description:
     *    1.[DIS4-1-6]In one Carrier, 25 wafers were all prepared, but only a few of them were STB.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_ReserveDuplicated() {
        startReservationForInternalBufferCase.startReservation_ReserveDuplicated();
    }

    /**
     * description:
     *    1.[DIS4-1-7]Reserve two different lots one by one using the same port
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_MutipleLotSinglePort() {
        //[DIS4-1-7]Reserve two different lots one by one using the same port
        startReservationForInternalBufferCase.startReservation_MutipleLotSinglePort();
    }

    /**
     * description:
     *    1.[DIS4-1-8]Reserve a process lot carrier to a port whose purpose type is "Empty Cassette"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_WithEmptyCassette() {
        startReservationForInternalBufferCase.startReservation_WithEmptyCassette();
    }


    /**
     * description:
     *    1.[DIS4-1-9]Reserve a carrier with Carrier Type "FOUP" to a port whose compatible carrier cagetory does not include "FOUP"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_CassetteCagetoryNotMatched() {
        startReservationForInternalBufferCase.startReservation_CassetteCagetoryNotMatched();
    }

    /**
     * description:
     *    1.[DIS4-1-11]Reserve carrier to port which is not under LoadReq or LoadAvail status
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_PortStatusAbnormal() {
        startReservationForInternalBufferCase.startReservation_PortStatusAbnormal();
    }


    /**     
     * description:
     *    1.[DIS4-1-15]Reserve a lot whose Lot Hold State is OnHold
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_LotOnHold() {
        startReservationForInternalBufferCase.startReservation_LotOnHold();
    }


    /**
     * description:
     *    1.[DIS4-1-16]Reserve a lot which has process hold record
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_HasProcessHoldRecord() {
        startReservationForInternalBufferCase.startReservation_HasProcessHoldRecord();
    }

    /**
     * description:
     *    1.[DIS4-1-17]Reserve a lot whose Lot Process State is not Waiting
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_NotWaiting() {
        startReservationForInternalBufferCase.startReservation_NotWaiting();
    }

    /**
     * description:
     *    1.[DIS4-1-18]Reserve a lot whose Lot Inventory State is inBank
     *    2.[DIS4-1-19]Reserve a lot whose Lot Inventory State is NonProBank
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_LotVentoryState_InBank() {
        startReservationForInternalBufferCase.startReservation_LotVentoryState_InBank();
    }

    /**
     * description:
     *    1.[DIS4-1-21]Total number of wafers in each carrier are not between the MaxWaferSize and MinWaferSize of equipment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_NotMatchWaferSize() {
        startReservationForInternalBufferCase.startReservation_NotMatchWaferSize();
    }

    /**
     * description:
     *    1.[DIS4-1-22]When monitorCreationFlag of equipment is True, Reserve carrier with another loaded empty carrier
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_MonitorCreationFlagTrue_WithEmptyCassette() {
        startReservationForInternalBufferCase.startReservation_MonitorCreationFlagTrue_WithEmptyCassette();
    }

    /**
     * description:
     *    1.[DIS4-1-23]When monitorCreationFlag of equipment is True, Reserve carrier without another loaded empty carrier
     *    2.[DIS4-1-24]When carrierExchangeFlag of equipment is True, Reserve 1 carrier with ProcessLot or ProcessMonitorLot without other loaded empty carrier.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_MonitorCreationFlagTrue_WithOutEmptyCassette() {
        startReservationForInternalBufferCase.startReservation_MonitorCreationFlagTrue_WithOutEmptyCassette();
    }

    
    /*     
     * description:
     *   1)[DIS4-1-25]When carrierExchangeFlag of equipment is True, Reserve 2 carriers with ProcessLot or ProcessMonitorLot with another 1 loaded empty carrier
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/19
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_CarrierExchangeFlagTrue_TwoLotOneEmptyCassette() {
        startReservationForInternalBufferCase.startReservation_CarrierExchangeFlagTrue_TwoLotOneEmptyCassette();
    }

    /**
     * description:
     *    1.[DIS4-1-29]Reserve a carrier with scrapped wafers inside
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/4
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startReservation_WithScrappedWafers() {
        startReservationForInternalBufferCase.startReservation_WithScrappedWafers();
    }
}