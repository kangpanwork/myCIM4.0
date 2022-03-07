package com.fa.cim.newIntegration.stb.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.newIntegration.bank.scase.VendorLotReceiveCase;
import com.fa.cim.newIntegration.tcase.StbTestCase;
import com.fa.cim.utils.GenerateVendorlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Neko
 * @date 2019/9/20 9:50
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class NPWLotCase {


    @Autowired
    private VendorLotReceiveCase vendorLotReceiveCase;

    @Autowired
    private StbTestCase stbTestCase;

    public Response NPWLot_ProcessMonitorLotSTBBeforeProcess() {
        //【step0】make default setting
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 25;
        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);
        //【step2】npw stb
        return this.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID,sourceProductID,productCategory,waferCount);
    }

    public Response NPWLot_ProcessMonitorLotSTBBeforeProcess(String vendorLotID,String bankID, String sourceProductID, String productCategory,Integer waferCount) {

        //【step1】search one product to stb
        List<Infos.ProductIDListAttributes> productIDListAttributes = stbTestCase.productIdListInqCase(productCategory);
        Infos.ProductIDListAttributes productIDAttribute = productIDListAttributes.get(0);

        //【step2】get SubLot Type and Source Product IDs and productID
        String subLotType = productIDAttribute.getCandidateSubLotTypes().get(0);
        String sourceProductIDs = CimObjectUtils.getObjectValue(productIDAttribute.getSourceProductID().get(0));
        ObjectIdentifier productID = productIDAttribute.getProductID();

        //【step3】make stb
        return stbTestCase.npwLotStartReqCase(subLotType, vendorLotID, productID,waferCount);
    }
}
