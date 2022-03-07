package com.fa.cim.controller.tcc.confirm;

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
 * @date: 2019/12/25 17:20
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("StorageConfirm")
@Transactional(rollbackFor = Exception.class)
public class StorageConfirm implements IStorageTest {
    @Override
    public void storageTest() { }

    @Override
    public void sampleTx() {}
}