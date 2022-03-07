package com.fa.cim.locktest;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.controller.interfaces.dispatch.IDispatchController;
import com.fa.cim.dto.Params;
import com.fa.cim.method.IObjectLockMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/3/18          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/3/18 16:03
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class LockInfoTest {

    @Autowired
    private IObjectLockMethod objectLockMethod;


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW) //REQUIRES_NEW 新建事务，如果当前存在事务，把当前事务挂起。事务结束 释放锁
    public void objectLockForEquipmentResourceTest(){
        Thread thread = Thread.currentThread();
        long id = thread.getId();
        log.info(String.format(">>>>>>> Thread ID %d start >>>>>>> ", id));

        objectLockMethod.objectLockForEquipmentResource(null, new ObjectIdentifier("HW1CDS09"), new ObjectIdentifier("P1"), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
        log.info(String.format(">>>>>>> Thread ID %d has been locked  >>>>>>> ", id));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        log.info(String.format(">>>>>>> Thread ID %d has been unlocked  >>>>>>> ", id));
    }
}