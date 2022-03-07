package com.fa.cim.newIntegration.internalBuffer;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.internalBuffer.scase.LoadForInternalBufferCase;
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
 * 2019/12/5        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/5 10:44
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class LoadForInternalBuffer {
    @Autowired
    LoadForInternalBufferCase loadForInternalBufferCase;

    /**     
     * description:
     *   1.[EQP6-1-1]Select the reserved Port for Loading
     *   2.[EQP6-1-3]Select the Shelf Information for Loading
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/6
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void load_WithStartReservation() {
        loadForInternalBufferCase.load_WithStartReservation();
    }

    /**     
     * description:
     *   1.[EQP6-1-2]Selected Port is not the reserved one
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/6
     * @param  -
     * @return void
     */

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void load_WithStartReservation_PortNotMatch() {
        loadForInternalBufferCase.load_WithStartReservation_PortNotMatch();
    }
    /**     
     * description:
     *   1.[EQP6-1-4]Manual Loading without StartLotsReservation in advance
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/5 
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void load_NoStartReservation() {
        loadForInternalBufferCase.load_NoStartReservation();
    }

    /**
     * description:
     *   1.[EQP6-1-6]Manual Loading a carrier whose transfer status is "EI"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/5
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void load_CassetteXferStatusIsEIStatus() {
        loadForInternalBufferCase.load_CassetteXferStatusIsEIStatus();
    }

    /**
     * description:
     *   1.[EQP6-1-7]Manual Loading a carrier which is not supposed to be processed in current selected equipment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/5
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void load_EquipmentNotSupported() {
        loadForInternalBufferCase.load_EquipmentNotSupported();
    }

    /**
     * description:
     *   1.[EQP6-1-8]Load a reserved process lot carrier to a port whose purpose type is "Process Monitor Lot"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/5
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void load_ProcessMonitorLotAfterReservated() {
        loadForInternalBufferCase.load_ProcessMonitorLotAfterReservated();
    }

    /**
     * description:
     *   1.[EQP6-1-9]Load a carrier with Carrier Type "FOUP" to a port whose compatible Carrier Type does not include"Foup"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/5
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void load_CassetteCategoryNotSupport() {
        loadForInternalBufferCase.load_CassetteCategoryNotSupport();
    }

    /**
     * description:
     *   1.[EQP6-1-15]Equipment's status should be available before Move In
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/5
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void load_EquipmentNotAvailable() {
        loadForInternalBufferCase.load_EquipmentNotAvailable();
    }

    /**
     * description:
     *   1.[EQP6-1-20]Load carrier which Carrier’s Transfer State is ManualIn
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/5
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void load_CassetteXferStatusIsMI() {
        loadForInternalBufferCase.load_CassetteXferStatusIsMI();
    }

    /**
     * description:
     *   1.[EQP6-1-24]Load carrier which Carrier’s status is NotAvailable
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/5
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void load_CassetteStatusNotAvailable() {
        loadForInternalBufferCase.load_CassetteStatusNotAvailable();
    }

    /**
     * description:
     *   1.[EQP6-1-25]Load carrier which Carrier’s status is INUSE
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/5
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void load_CassetteStatusIsInUse() {
        loadForInternalBufferCase.load_CassetteStatusIsInUse();
    }
}