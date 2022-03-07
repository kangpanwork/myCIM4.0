package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.AutoDispatchControlCase;
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
 * 2019/12/19          ********            lightyh                create file
 *
 * @author: light
 * @date: 2019/12/19 12:36
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class LotAutoDispatchControl {

    @Autowired
    private AutoDispatchControlCase autoDispatchControlCase;

    /**
     * description: DIS-5-5-1 Normal case of Adding a Auto Dispatch Control which process flow is current process flow
     *              DIS-5-5-2 Adding a Auto Dispatch Control which process flow is different from current process flow
     *              DIS-5-5-3 Adding a Auto Dispatch Control which Operation NO is current
     *              DIS-5-5-4 Adding a Auto Dispatch Control which Operation NO is not current
     *              DIS-5-5-5 Adding a Auto Dispatch Control which Single Trigger flag is "Single"
     *              DIS-5-5-6 Adding a Auto Dispatch Control which Single Trigger flag is "Multiple"
     *              DIS-5-5-10 Delete a Auto Dispatch Control record
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/19 14:03
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void addingAutoDispatchControlThenCheckTheAutoDispatchDisabledFlag(){
        autoDispatchControlCase.addingAutoDispatchControlThenCheckTheAutoDispatchDisabledFlag();
    }

    /**
     * description: DIS-5-5-7 Adding a Auto Dispatch Control which lot state is FINISHED
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/19 15:37
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void addingAutoDispatchControlWhichLotStateIsFINISHED(){
        autoDispatchControlCase.addingAutoDispatchControlWhichLotStateIsFINISHED();
    }

    /**
     * description: DIS-5-5-8 Adding a Auto Dispatch Control which Lot Post Process State is in post-processing
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/19 16:02
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void addingAutoDispatchControlWhichLotPostProcessStateIsInPostProcessing(){
        // todo
        autoDispatchControlCase.addingAutoDispatchControlWhichLotPostProcessStateIsInPostProcessing();
    }

    /**
     * description: DIS-5-5-9
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/19 16:15
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateAutoDispatchControlChangeSingleTriggerFlagFromMultipleToSingle(){
        autoDispatchControlCase.updateAutoDispatchControlChangeSingleTriggerFlagFromMultipleToSingle();
    }

    /**
     * description:DIS-5-5-11 Execute Auto Dispatch Control which Single Trigger flag is "Multiple"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/23 16:13
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void executeAutoDispatchControlWhichSingleTriggerFlagIsMultiple(){
        autoDispatchControlCase.executeAutoDispatchControlWhichSingleTriggerFlagIsMultiple();
    }

    /**
     * description:DIS-5-5-12 Execute Auto Dispatch Control which Single Trigger flag is "Single"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/23 17:27
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void executeAutoDispatchControlWhichSingleTriggerFlagIsSingle(){
        autoDispatchControlCase.executeAutoDispatchControlWhichSingleTriggerFlagIsSingle();
    }

    /**
     * description:DIS-5-5-13 Execute Auto Dispatch Control which Load carrier to port without reserve
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/25 10:11
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void executeAutoDispatchControlWhichLoadCarrierToPortWithoutReserve(){
        autoDispatchControlCase.executeAutoDispatchControlWhichLoadCarrierToPortWithoutReserve();
    }

    /**
     * description:DIS-5-5-14 Execute Auto Dispatch Control which start reservation without change carrier's xfer status
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/25 10:37
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void executeAutoDispatchControlWhichStartReservationWithoutChangeCarrierXferStatus(){
        autoDispatchControlCase.executeAutoDispatchControlWhichStartReservationWithoutChangeCarrierXferStatus();
    }

    /**
     * description:DIS-5-5-15 Execute Auto Dispatch Control which load lot to port without change carrier's xfer status
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/25 10:47
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void executeAutoDispatchControlWhichLoadLotToPortWithoutChangeCarrierXferStatus(){
        autoDispatchControlCase.executeAutoDispatchControlWhichLoadLotToPortWithoutChangeCarrierXferStatus();
    }

    /**
     * description:DIS-5-5-16 Execute Auto Dispatch Control which load port state should be “Loadcomp”
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/25 10:56
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void executeAutoDispatchControlWhichLoadPortStateShouldBeLoadcomp(){
        autoDispatchControlCase.executeAutoDispatchControlWhichLoadPortStateShouldBeLoadcomp();
    }

    /**
     * description: DIS-5-5-18 Lot Information Inquiry
     *              DIS-5-5-19 What’s Next List Inquiry
     *              DIS-5-5-20 Lot List Inquiry
     *              DIS-5-5-21 Cassette Information Inquiry
     *              DIS-5-5-22 Lot Auto Dispatch Control Information Inquiry
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/19 16:38
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void autoDispatchInquiry(){
        autoDispatchControlCase.autoDispatchInquiry();
    }

    /**
     * description: DIS-5-5-23 Auto dispath control in splitted lots
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/25 11:08
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void autoDispathControlInSplittedLots(){
        autoDispatchControlCase.autoDispathControlInSplittedLots();
    }


}