package com.fa.cim.newIntegration.flowbatch;

import com.fa.cim.MycimApplication;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.newIntegration.flowbatch.scase.FlowBatchCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicReference;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/13       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/11/13 12:54
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class FlowBatch {

    @Autowired
    private FlowBatchCase flowBatchCase;

    /**
     * description: testcase doc :include DIS3-1-2, DIS3-1-8，DIS3-1-13，DIS3-1-14，DIS3-1-15，DIS3-1-16,
     * DIS3-1-17,DIS3-1-18,DIS3-1-19
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/29 16:06
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void flowBatch_HappyPath(){
        flowBatchCase.flowBatch_HappyPath();
    }

    /**
     * description: testcase doc :DIS3-1-12
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/29 16:07
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void autoFlowBatch_HappyPath(){
        flowBatchCase.autoFlowBatch_HappyPath();
    }

    /**
     * description: testcase doc :DIS3-1-3
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/29 16:08
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void flowBatch_BatchSize(){
        flowBatchCase.flowBatch_BatchSize();
    }

    /**
     * description: testcase doc :DIS3-1-5
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/29 16:08
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void flowBatch_SomeBatchesExist(){
        flowBatchCase.flowBatch_SomeBatchesExist();
    }

    /**
     * description:testcase doc :DIS3-1-9, DIS3-1-33
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/29 17:53
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void flowBatch_FirstTargetEquipment_MoveOut(){
        AtomicReference<ObjectIdentifier> tempFlowBatchID = new AtomicReference<>();
        flowBatchCase.flowBatch_FirstTargetEquipment_MoveOut(tempFlowBatchID);
    }

    /**
     * description:testcase doc :DIS3-1-10, DIS3-1-6，DIS3-1-31
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/2 13:16
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void flowBatch_LastTargetEquipment_MoveOut(){
        flowBatchCase.flowBatch_LastTargetEquipment_MoveOut();
    }

    /**
     * description:testcase doc :DIS3-1-20,DIS3-1-23
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/2 17:41
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void flowBatch_LocateToOutside_LeftLotSize_BetweenMaxAndMin(){
        flowBatchCase.flowBatch_LocateToOutside_LeftLotSize_BetweenMaxAndMin();
    }

    /**
     * description:testcase doc :DIS3-1-21, DIS3-1-22
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/2 17:48
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void flowBatch_LocateToOutside_LeftLotSize_LessThanMin(){
        flowBatchCase.flowBatch_LocateToOutside_LeftLotSize_LessThanMin();
    }

    /**
     * description:testcase doc :DIS3-1-24，DIS3-1-26
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/3 13:53
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void two_floatingBatch_to_consist_ofOneBatch(){
        flowBatchCase.two_floatingBatch_to_consist_ofOneBatch();
    }

    /**
     * description:testcase doc :DIS3-1-25
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/3 16:08
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void someOfRelated_FlowBatchInhibited(){
        flowBatchCase.someOfRelated_FlowBatchInhibited();
    }

    /**
     * description:testcase doc :DIS3-1-28
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/4 9:16
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void flowBatched_ReworkBranch(){
        flowBatchCase.flowBatched_ReworkBranch();
    }

    /**
     * description:testcase doc :DIS3-1-34
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/4 14:02
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void flowBatch_Reserve_TwoLots_In_TargetEquipment(){
        flowBatchCase.flowBatch_Reserve_TwoLots_In_TargetEquipment();
    }

    /**
     * description:testcase doc :DIS3-1-37,DIS3-1-38,DIS3-1-39,DIS3-1-40
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/4 14:30
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void flowBatch_ChangeMaxCount(){
        flowBatchCase.flowBatch_ChangeMaxCount();
    }

    /**
     * description:DIS3-1-41 Batch infor search in lot information page which lot has no batch information
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/23 12:07
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void batchInforSearchInLotInformationPageWhichLotHasNoBatchInformation(){
        flowBatchCase.batchInforSearchInLotInformationPageWhichLotHasNoBatchInformation();
    }

    /**
     * description:DIS3-1-42 Batch infor search in lot information page which lot exist batch ID
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/23 11:00
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void batchInforSearchInLotInformationPageWhichLotExistBatchID(){
        flowBatchCase.batchInforSearchInLotInformationPageWhichLotExistBatchID();
    }

    /**
     * description: DIS3-1-43 Flow batch lost lots list in MISC
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/20 15:43
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void flowBatchLostLotsList(){
        flowBatchCase.flowBatchLostLotsList();
    }

    /**
     * description:DIS3-1-45 Auto flow  batch when some of lots has been splited
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/23 10:09
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void autoFlowBatchWhenSomeOfLotsHasBeenSplited(){
        flowBatchCase.autoFlowBatchWhenSomeOfLotsHasBeenSplited();
    }

}