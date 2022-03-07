package com.fa.cim.newIntegration.internalBuffer;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.internalBuffer.scase.BufferAllocationCase;
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
public class BufferAllocation {
    @Autowired
    private BufferAllocationCase bufferAllocationCase;

    /**     
     * description:
     *   1)[EQP-11-3-1]In the case that use specifies actual buffer resources dynamically in MM.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/18
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void addAllBufferSize() {
        bufferAllocationCase.addAllBufferSize();
    }
    
    /**     
     * description:
     *   1)[EQP-11-3-2]In the case that there isn’t enough buffer resource for process lot to execute a batch-process.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/18
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void addProcessLotBufferSize() {
        bufferAllocationCase.addProcessLotBufferSize();
    }

    /**
     * description:
     *   1)[EQP-11-3-3]In the case that there isn’t enough buffer resource for empty cassette to unload the completed lots and there are some not-used buffer resources for Process Lot.
     *   2)[EQP-11-3-4]In the case that there is a maintenance work for actual buffer resources. Dynamic allocated actual buffers are returned to “Any Process Lot” not to be dispatched.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/18
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void reduceProcessLotBufferSize() {
        bufferAllocationCase.reduceProcessLotBufferSize();
    }
}