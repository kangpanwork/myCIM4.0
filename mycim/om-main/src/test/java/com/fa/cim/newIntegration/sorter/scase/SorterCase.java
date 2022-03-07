package com.fa.cim.newIntegration.sorter.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.controller.newSorter.SortNewInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.sorter.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/6/30         ********             ZH                 create file
 *
 * @author: ZH
 * @date: 2021/6/30 3:45 下午
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved
 */
@Service
@Slf4j
public class SorterCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private SortNewInqController sortNewInqController;

    public Response actionSelect() {
        String equipmentID = "ASTR201";
        Params.OnlineSorterActionSelectionInqParams onlineSorterActionSelectionInqParams = new Params.OnlineSorterActionSelectionInqParams();
        onlineSorterActionSelectionInqParams.setUser(testCommonData.getUSER());
        onlineSorterActionSelectionInqParams.setEquipmentID(ObjectIdentifier.buildWithValue(equipmentID));
        Response response = sortNewInqController.onlineSorterActionSelectionInq(onlineSorterActionSelectionInqParams);
        return response;
    }

}