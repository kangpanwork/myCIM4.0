package com.fa.cim.newIntegration.bank.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.dto.Infos;
import com.fa.cim.newIntegration.tcase.BankTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/2       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/2 11:15
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class BankShipCase {
    @Autowired
    private BankTestCase bankTestCase;

    public Response bankShip() {
        //【step1】select Bank
        final String bankID = "BNK-0S";
        final String inqBank = "B";
        Response response = bankTestCase.bankListInqCase(bankID, inqBank);
        Validations.isSuccessWithException(response);
        //【step2】get lots which in bank
        response = bankTestCase.lotInfoByLotListInqCase(bankID);
        Validations.isSuccessWithException(response);
        Page<Infos.LotListAttributes> lotListInqResult = (Page<Infos.LotListAttributes>) response.getBody();
        // 随机选择状态为COMPLETED的LOT
        List<Infos.LotListAttributes> lotListAttributesList = lotListInqResult.getContent();
        List<Infos.LotListAttributes> lotListAttributesCompletedList = lotListAttributesList.stream().filter(lotListAttributes -> lotListAttributes.getLotStatus().equals("COMPLETED")).collect(Collectors.toList());
        List<ObjectIdentifier> lotListAttributes = new ArrayList<>();
        lotListAttributes.add(lotListAttributesCompletedList.get(new Random().nextInt(lotListAttributesCompletedList.size())).getLotID());
        //【step3】ship in
        return bankTestCase.bankShipCase(new ObjectIdentifier(bankID), lotListAttributes);
    }
}