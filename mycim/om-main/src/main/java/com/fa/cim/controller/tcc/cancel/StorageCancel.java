package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.controller.interfaces.storage.IStorageTest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/25        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/25 17:21
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("StorageCancel")
@Transactional(rollbackFor = Exception.class)
public class StorageCancel implements IStorageTest {
    @Override
    public void storageTest() {}

    @Override
    public void sampleTx() {}
}