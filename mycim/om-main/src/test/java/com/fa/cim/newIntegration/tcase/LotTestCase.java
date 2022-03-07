package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.controller.interfaces.lot.ILotInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/9          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/12/9 18:24
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotTestCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;
    @Autowired
    private ILotInqController lotInqController;

    public List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInqCase(ObjectIdentifier lotFamilyID) {
        Params.WaferListInLotFamilyInqParams waferListInLotFamilyInqParams = new Params.WaferListInLotFamilyInqParams();
        waferListInLotFamilyInqParams.setUser(testCommonData.getUSER());
        waferListInLotFamilyInqParams.setLotFamilyID(lotFamilyID);
        return (List<Infos.WaferListInLotFamilyInfo>) lotInqController.waferListInLotFamilyInq(waferListInLotFamilyInqParams).getBody();
    }

    public Results.HoldLotListInqResult HoldLotListInqCase(ObjectIdentifier lotID){
        Params.HoldLotListInqParams params = new Params.HoldLotListInqParams();
        params.setUser(testCommonData.getUSER());
        params.setLotID(lotID);
        params.setSearchCondition(new SearchCondition());
        return  (Results.HoldLotListInqResult)lotInqController.HoldLotListInq(params).getBody();
    }
}