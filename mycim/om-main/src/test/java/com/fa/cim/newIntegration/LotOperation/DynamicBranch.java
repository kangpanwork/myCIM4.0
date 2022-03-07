package com.fa.cim.newIntegration.LotOperation;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.LotOperation.scase.DynamicBranchCase;
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
 * 2019/12/20          ********            lightyh                create file
 *
 * @author: light
 * @date: 2019/12/20 10:32
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class DynamicBranch {

    @Autowired
    private DynamicBranchCase dynamicBranchCase;

    /**
     * description: 1 Dynanmic Branch from Production route to Branch route and return point is current step
     *              2 Dynamic Branch from Branch route to dynamic  Branch route
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/20 10:39
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void dynamicBranchNormalCase(){
        dynamicBranchCase.dynamicBranchNormalCase();
    }

    /**
     * description: 3 Branch from Production route to Branch route (a future hold in production route) out of the jump step
     *              4 Branch from Production route to Branch route (a future hold in production route) in the jump step
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/20 13:16
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void dynamicBranchFutureHoldInTheProductionRoute(){
        dynamicBranchCase.dynamicBranchFutureHoldInTheProductionRoute();
    }

    /**
     * description: 5 if there have a future hold in the sub route,lot can not branch cancle
     *              6 if there have a future hold in the sub route,lot can not skip the future step
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/12/20 13:28
     * @param  -
     * @return void
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void dynamicBranchFutureHoldInSubRoute(){
        dynamicBranchCase.dynamicBranchFutureHoldInSubRoute();
    }

}