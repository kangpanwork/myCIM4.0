package com.fa.cim.newIntegration.internalBuffer;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.internalBuffer.scase.UnloadForInternalBufferCase;
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
 * 2019/12/6        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/6 16:38
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class UnloadForInternalBuffer {
    @Autowired
    private UnloadForInternalBufferCase unloadForInternalBufferCase;

    /**
     * description:
     *    1.[EQP6-1-37]Manual Unloading a carrier which just did "carrier out" operation
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
    public void unload_AfterMoveOut() {
        unloadForInternalBufferCase.unload_AfterMoveOut();
    }


    /**     
     * description:
     *   1.[EQP6-1-38]Manual Unloading a carrier which is just loaded and has not be moved to shelf
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
    public void unload_BeforeMoveOut() {
        unloadForInternalBufferCase.unload_BeforeMoveOut();
    }

}