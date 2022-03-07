package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.BranchCase;
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
 * 2019/12/12          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/12/12 15:42
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class Branch {

    @Autowired
    private BranchCase branchCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void branchFromProductionRouteToBranchRoute() {
        branchCase.branchFromProductionRouteToBranchRoute();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void branchFromBranchRouteToBranchRoute() {
        branchCase.branchFromBranchRouteToBranchRoute();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void branchAOnholdLotWithHoldRelease() {
        branchCase.branchAOnholdLotWithHoldRelease();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void branchFromProductionFutureRouteToBranchRouteOutOfTheJumpStep() {
        branchCase.branchFromProductionFutureRouteToBranchRouteOutOfTheJumpStep();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void branchFromProductionFutureRouteToBranchRouteInTheJumpStep_exception() {
        branchCase.branchFromProductionFutureRouteToBranchRouteInTheJumpStep_exception();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void branchCancelAtTheFirstStepFromBranchRoute() {
        branchCase.branchCancelAtTheFirstStepFromBranchRoute();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void branchCancelAtTheFirstStepFromBranchRouteMoveIn_exception() {
        branchCase.branchCancelAtTheFirstStepFromBranchRouteMoveIn_exception();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void branchCancelNotAtTheFirstStepFromBranchRoute_exception() {
        branchCase.branchCancelNotAtTheFirstStepFromBranchRoute_exception();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void branchCancelWhenAFutureHoldInBranchRoute_exception() {
        branchCase.branchCancelWhenAFutureHoldInBranchRoute_exception();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void branchCancelFromProductionFutureRouteToBranchRoute() {
        branchCase.branchCancelFromProductionFutureRouteToBranchRoute();
    }
}