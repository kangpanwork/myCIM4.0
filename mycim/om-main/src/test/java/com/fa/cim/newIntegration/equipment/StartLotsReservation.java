package com.fa.cim.newIntegration.equipment;

import com.fa.cim.MycimApplication;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.newIntegration.equipment.scase.StartLotsReservationCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/4          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/9/4 16:16
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class StartLotsReservation {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private StartLotsReservationCase startLotsReservationCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveInReserveReq_singleCassette() {
        startLotsReservationCase.startLotsReservation();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveInReserveReq_twoLotsAtOneTime() {
        startLotsReservationCase.startReserve_twoLotsAtOneTime();
    }

    /**
     *
     * description: DIS4-1-5  Reserve a carrier which is already reserved to an equipment
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/2 14:34
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void reserveAlreadyReserved() {
        startLotsReservationCase.reserveAlreadyReserved();
    }

    /**
     *
     * description: DIS4-1-9  Reserve carriers to different port groups
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/2 17:37
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void reserveDifferentPortGroup() {
        startLotsReservationCase.reserveDifferentPortGroup();
    }

    /**
     *
     * description: DIS4-1-8  Reserve a carrier with Carrier Type "FOUP" to a port whose compatible carrier cagetory does not include "FOUP"
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/5 11:29
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void reserveFoupToNotIncludeFoupPort() {
        startLotsReservationCase.reserveFoupToNotIncludeFoupPort();
    }

    /**
     *
     * description: DIS4-1-10 Reserve carrier to port which is not under LoadReq or LoadAvail status
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/5 13:29
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void reserveCarrierToPortWhichIsNotUnderLoadReqOrLoadAvailStatus() {
        startLotsReservationCase.reserveCarrierToPortWhichIsNotUnderLoadReqOrLoadAvailStatus();
    }

    /**
     *
     * description: DIS4-1-14 Reserve a lot whose Lot Hold State is OnHold
     *              DIS4-1-18 Reserve a lot which has process hold record
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/6 10:27
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void ReserveALotWhoseLotHoldStateIsOnHold() {
        startLotsReservationCase.ReserveALotWhoseLotHoldStateIsOnHold();
    }

    /**
     *
     * description: DIS4-1-15 Reserve a lot whose Lot Process State is not Waiting
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/6 10:27
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void ReserveALotWhoseLotProcessStateIsNotWaiting() {
        startLotsReservationCase.ReserveALotWhoseLotProcessStateIsNotWaiting();
    }

    /**
     *
     * description: DIS4-1-21 When monitorCreationFlag of equipment is True, Reserve carrier with another loaded empty carrier
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/6 10:27
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void whenMonitorCreationFlagOfEquipmentIsTrueReserveCarrierWithAnotherLoadedEmptyCarrier() {
        startLotsReservationCase.whenMonitorCreationFlagOfEquipmentIsTrueReserveCarrierWithAnotherLoadedEmptyCarrier();
    }

    /**
     *
     * description: DIS4-1-22  When monitorCreationFlag of equipment is True, Reserve carrier without another loaded empty carrier
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/9 11:26
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void whenMonitorCreationFlagOfEquipmentIsTrueReserveCarrierWithoutAnotherLoadedEmptyCarrier() {
        try {
            startLotsReservationCase.whenMonitorCreationFlagOfEquipmentIsTrueReserveCarrierWithoutAnotherLoadedEmptyCarrier();
            Assert.isTrue(false,"0000437E:Specified empty cassette count 0 is wrong. Correct count is 1.");
        } catch (ServiceException ex){
            Assert.isTrue(Validations.isEquals(ex.getCode(),retCodeConfig.getInvalidEmptyCount()),ex.getMessage());
        }
    }

    /**
     *
     * description: DIS4-1-27  Reserve a carrier with scrapped wafers inside
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return void
     * @exception
     * @author HO
     * @date 2019/12/9 11:26
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void reserveACarrierWithScrappedWafersInside() {
        try {
            startLotsReservationCase.reserveACarrierWithScrappedWafersInside();
            Assert.isTrue(false,"error message :Logical Scrap Wafers are found. Need Wafer Sorter Action");
        } catch (ServiceException ex){
            Assert.isTrue(Validations.isEquals(ex.getCode(),retCodeConfig.getFoundScrap()),ex.getCode()+ex.getMessage());
        }
    }

}