package com.fa.cim.newIntegration.internalBuffer;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.internalBuffer.scase.MoveInCancelForInternalBufferCase;
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
 * @date: 2019/12/9 17:47
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class MoveInCancelForInternalBuffer {
    @Autowired
    private MoveInCancelForInternalBufferCase moveInCancelForInternalBufferCase;

    /**
     * description:
     *   1)[EQP11-3-1]Move in Cancel a reserved lot
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
    public void moveInCancel_WithStartReservation() {
        moveInCancelForInternalBufferCase.moveInCancel_WithStartReservation();
    }

    /**
     * description:
     *   1)[EQP11-3-2]Move out a lot which is not reserved
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
    public void moveInCancel_WithOutStartReservation() {
        moveInCancelForInternalBufferCase.moveInCancel_WithOutStartReservation();
    }
}