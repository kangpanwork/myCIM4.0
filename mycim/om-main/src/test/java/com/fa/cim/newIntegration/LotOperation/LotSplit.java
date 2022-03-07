package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.LotSplitCase;
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
 * 2019/9/11       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/11 17:46
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class LotSplit {

    @Autowired
    private LotSplitCase lotSplitCase;

    
    /**     
     * description:
     *   1)[WIP-8-1-1]Split one lot to two lots without merge point
     *   2)[WIP-8-1-9]Split to sub route when not on branch step
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author HuangHao
     * @date 2019/12/11
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithoutMergePoint(){
        lotSplitCase.splitWithoutMergePoint();
    }

    /**
     * description:
     *   1)[WIP-8-1-2]Split one lot to two lots with merge point selected
     *   2)[WIP-8-1-9]Split to sub route when not on branch step
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/11
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithMergePoint(){
        lotSplitCase.splitWithMergePoint();
    }

    /**
     * description:
     *   1)[WIP-8-1-3]Split lot after it was splited already
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/11
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithSplitedAlready(){
        lotSplitCase.splitWithSplitedAlready();
    }


    /**
     * description:
     *   1)[WIP-8-1-4]OnHold lot to do lot split
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/11
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithHoldLot(){
        lotSplitCase.splitWithHoldRelease();
        lotSplitCase.splitWithOutHoldRelease();
    }

    /**
     * description:
     *   1)[WIP-8-1-5]Split vendor lot which is just received
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/11
     * @param  -
     * @return void
     */

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitVendorLotWithNotPrepared(){
        lotSplitCase.splitVendorLotWithNotPrepared();
    }

    /**
     * description:
     *   1)[WIP-8-1-6]Split vendor lot which is prepared
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/11
     * @param  -
     * @return void
     */

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitVendorLotWithPrepared(){
        lotSplitCase.splitVendorLotWithPrepared();
    }


    /**
     * description:
     *   1)[WIP-8-1-8]split to sub route when  on branch step(split to sub route cannot split to rework route ,only branch route)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/11
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitToSubRoute(){
        lotSplitCase.splitToSubRoute();
    }

    /**
     * description:
     *   1)[WIP8-1-10]split with EI status
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/11
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithEIStatus(){
        lotSplitCase.splitWithEIStatus();
    }

    /**
     * description:
     *   1)[WIP8-1-11]split with processing status
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/13
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithProcessingStatus(){
        lotSplitCase.splitWithProcessingStatus();
    }

    /**     
     * description:
     *   1)[WIP8-1-12]Split the same lot second time after merged
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/13
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithSecondTime(){
        lotSplitCase.splitWithSecondTime();
    }

    /**     
     * description:
     *   1)[WIP8-1-13]Lot to do split with future hold registrated
     *   2)[WIP8-1-19]Set Future hold for Lot and then split and The child Lot will inherit the data status of the father Lot
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/13
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithFutureHoldRegistrated(){
        lotSplitCase.splitWithFutureHoldRegistrated();
    }

    /**
     * description:
     *   1)[WIP8-1-15]Lot to do split with lot note
     *   2)[WIP8-1-20]Add notes to Lot and then split. The child Lot will inherit the notes from his father Lot
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/13
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithLotNote(){
        lotSplitCase.splitWithLotNote();
    }

    /**
     * description:
     *   1)[WIP8-1-16]Lot to do split with lot comment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/13
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithLotComment(){
        lotSplitCase.splitWithLotComment();
    }


    /**
     * description:
     *   1)[WIP8-1-18]Lot to do split with monitor group(as monitored lot)
     *   2)[WIP8-1-23]Monitor Group and * If the parent lot is a monitored lot, the monitor group is inherited to the child lot.
     *   3)[WIP8-1-24]Monitor Group and  But if the parent lot is a monitoring lot, the monitor group is not inherited to the child lot.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/13
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithMonitorGroup(){
        lotSplitCase.splitWithMonitorGroup();
    }


    /**
     * description:
     *   1)[WIP8-1-21]After Lot Operation Notes, perform the split Operation to check the Lot Operation Notes of the child batch
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/16
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithLotOperationNote(){
        lotSplitCase.splitWithLotOperationNote();
    }


    /**
     * description:
     *   1)[WIP8-1-29]After split, the child Lot inherits the data of Control Job just like the father Lot
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/16
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void splitWithControlJob(){
        lotSplitCase.splitWithControlJob();
    }
}