package com.fa.cim.newIntegration.internalBuffer;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.internalBuffer.scase.CarrierOutForInternalBufferCase;
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
 * 2019/12/9        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/9 14:41
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class CarrierOutForInternalBuffer {
    @Autowired
    private CarrierOutForInternalBufferCase carrierOutForInternalBufferCase;
    
    
    /**     
     * description:
     *   1.[EQP1-1-2](Internal_Buffer_Equipment_(Furnace_Type)_Management) Move To Shelf & Carrier out without Operation start and Operation Complete
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/9
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void carrierOut_WithOutMoveIn() {
        carrierOutForInternalBufferCase.carrierOut_WithOutMoveIn();
    }
    
    /**     
     * description:
     *   1.[EQP1-1-3](Internal_Buffer_Equipment_(Furnace_Type)_Management) Move To Shelf & Carrier out after reserve cancel
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/9
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void carrierOut_AfterStartReservationCancel() {
        carrierOutForInternalBufferCase.carrierOut_AfterStartReservationCancel();
    }

    /**     
     * description:
     *   1.[EQP11-1-1](Internal_Buffer_Equipment_(Furnace_Type)_Management) Move To Shelf & Carrier out after Opreation start cancel
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/9
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void carrierOut_AfterMoveInCancel() {
        carrierOutForInternalBufferCase.carrierOut_AfterMoveInCancel();
    }
}