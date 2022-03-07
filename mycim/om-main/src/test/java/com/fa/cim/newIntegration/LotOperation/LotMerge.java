package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.LotMergeCase;
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
 * 2019/9/12       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/12 9:46
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class LotMerge {

    @Autowired
    private LotMergeCase lotMergeCase;

    /**     
     * description:
     *   1)[WIP-8-2-1]Merge two lots without merge point selected
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
    public void mergeWithoutMergePoint(){
        lotMergeCase.mergeWithoutMergePoint();
    }

    /**
     * description:
     *   1)[WIP-8-2-2]Merge two lots with merge point selected
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
    public void mergeWithMergePoint(){
        lotMergeCase.mergeWithMergePoint();
    }

    /**
     * description:
     *   1)[WIP-8-2-3]Merge two lots not at merge point
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
    public void mergeIsNotMergePoint(){
        lotMergeCase.mergeIsNotMergePoint();
    }

    /**
     * description:
     *   1)[WIP-8-2-4]Merge with target lot is onhold
     *   2)[WIP-8-2-6]Merge with parent & child lot are both on hold  with the same hold reason code
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
    public void mergeWithParentLotOnHold(){
        lotMergeCase.mergeWithParentLotOnHold();
    }


    /**
     * description:
     *   1)[WIP-8-2-5]Merge with child lot is onhold
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
    public void mergeWithChildLotOnHold(){
        lotMergeCase.mergeWithChildLotOnHold();
    }


    /**
     * description:
     *   1)[WIP-8-2-7]Merge with parent & child lot are both on hold  with different hold reason code
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
    public void mergeWithDifferentHoldReson(){
        lotMergeCase.mergeWithDifferentHoldReson();
    }

    /**
     * description:
     *   1)[WIP-8-2-8]Merge vendor lot which is just received
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
    public void mergeWithVendorLotJustReceived(){
        lotMergeCase.mergeWithVendorLotJustReceived();
    }

    /**
     * description:
     *   1)[WIP-8-2-9]Merge vendor lot which is prepared
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
    public void mergeWithVendorLotHasPrepared(){
        lotMergeCase.mergeWithVendorLotHasPrepared();
    }

    /**     
     * description:
     *   1)[WIP-8-2-11]Merge lot with putting a child lot to sub route
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithChildLotOnSubRoute(){
        lotMergeCase.mergeWithChildLotOnSubRoute();
    }

    /**
     * description:
     *   1)[WIP-8-2-12]Merge with EI status
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithEIStatus(){
        lotMergeCase.mergeWithEIStatus();
    }

    /**     
     * description:
     *   1)[WIP-8-2-13]Merge with processing status
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithProcessingStatus(){
        lotMergeCase.mergeWithProcessingStatus();
    }

    /**
     * description:
     *   1)[WIP-8-2-14]Merge Lot 3 to lot 2 (merge GrandChildLot to other child lot)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithChildLotWithGrandChildLot(){
        lotMergeCase.mergeWithChildLotWithGrandChildLot();
    }

    /**
     * description:
     *   1)[WIP-8-2-15]Merge Lot 3 to lot 0
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithLotWithGrandChildLot(){
        lotMergeCase.mergeWithLotWithGrandChildLot();
    }

    /**
     * description:
     *   1)[WIP-8-2-16]Merge Lot 1 to lot 2
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithChildLotWithChildLot(){
        lotMergeCase.mergeWithChildLotWithChildLot();
    }

    /*
     * description:
     *   1)[WIP-8-2-17]Merge with parent & child lot are both have future hold record  with the same  reason code（same  hold step )
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithFutureHoldWithSameHoldStep(){
        lotMergeCase.mergeWithFutureHoldWithSameHoldStep();
    }

    /**
     * description:
     *   1)[WIP-8-2-18]Merge with parent & child lot are both have future hold record  with the same  reason code（ different hold step )
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithFutureHoldWithDifferentHoldStep(){
        lotMergeCase.mergeWithFutureHoldWithDifferentHoldStep();
    }

    /**
     * description:
     *   1)[WIP-8-2-19]Merge with parent & child lot are both have future hold record  with different reason code
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithFutureHoldWithDifferentReasonCode(){
        lotMergeCase.mergeWithFutureHoldWithDifferentReasonCode();
    }

    /**
     * description:
     *   1)[WIP-8-2-20]Add notes to Lot and then split. The child Lot will inherit the notes from his father Lot and  The Merge operation is then performed
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithLotNotes(){
        lotMergeCase.mergeWithLotNotes();
    }

    /**
     * description:
     *   1)[WIP-8-2-21]After Lot Operation Notes, perform the split Operation to check the Lot Operation Notes of the child batch and The Merge operation is then performed
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithLotOperationNotes(){
        lotMergeCase.mergeWithLotOperationNotes();
    }

    /**
     * description:
     *   1)[WIP-8-2-23]Monitor Group and * If the parent lot is a monitored lot, the monitor group is inherited to the child lot and The Merge operation is then performed
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithBothHaveMonitorGroupInfo(){
        lotMergeCase.mergeWithBothHaveMonitorGroupInfo();
    }

    /**
     * description:
     *   1)Monitor Group and  But if the parent lot is a monitoring lot, the monitor group is not inherited to the child lot and The Merge operation is then performed
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithParentLotHasMonitorGroupInfo(){
        lotMergeCase.mergeWithParentLotHasMonitorGroupInfo();
    }


    /**
     * description:
     *   1)[WIP-8-2-31]Lot to do merge with lot comment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/12/17
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void mergeWithLotComment(){
        lotMergeCase.mergeWithLotComment();
    }
}