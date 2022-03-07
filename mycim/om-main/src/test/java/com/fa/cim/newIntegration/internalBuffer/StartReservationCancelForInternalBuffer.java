package com.fa.cim.newIntegration.internalBuffer;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.internalBuffer.scase.StartReservationCancelForInternalBufferCase;
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
 * @date: 2019/11/26 16:17
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class StartReservationCancelForInternalBuffer {
    @Autowired
    private StartReservationCancelForInternalBufferCase StartReservationCancelForInternalBufferCase;

    /**     
     * description:
     *   1)[DIS4-1-41]Start Reserve Cancel for Equipment
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
    public void startReservationCancel_Norm() {
        StartReservationCancelForInternalBufferCase.startReservationCancel_SingleLot();
    }

    /**     
     * description:
     *   2)[DIS4-1-42]Move in Reserve Cancel for Equipment after Loading
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
    public void startReservationCancel_AfterLoading() {
        StartReservationCancelForInternalBufferCase.startReservationCancel_AfterLoading();
    }
}