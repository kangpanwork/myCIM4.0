package com.fa.cim.newIntegration.processMonitor.scase;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.dispatch.DispatchController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.controller.edc.EngineerDataCollectionController;
import com.fa.cim.controller.edc.EngineerDataCollectionInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.LotOperation.scase.LotHoldCase;
import com.fa.cim.newIntegration.bank.scase.VendorLotPrepareCase;
import com.fa.cim.newIntegration.bank.scase.VendorLotReceiveCase;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.internalBuffer.scase.LoadForInternalBufferCase;
import com.fa.cim.newIntegration.internalBuffer.scase.MoveInForInternalBufferCase;
import com.fa.cim.newIntegration.internalBuffer.scase.MoveOutForInternalBufferCase;
import com.fa.cim.newIntegration.internalBuffer.scase.MoveToSelfCase;
import com.fa.cim.newIntegration.stb.scase.NPWLotCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.*;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.utils.GenerateVendorlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/27                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/11/27 15:07
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class ProcessMonitorCase {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private DispatchController dispatchController;

    @Autowired
    private DurableInqController durableInqController;

    @Autowired
    private BankTestCase bankTestCase;

    @Autowired
    private VendorLotPrepareCase vendorLotPrepareCase;

    @Autowired
    private NPWLotCase npwLotCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private ProcessMonitorTestCase processMonitorTestCase;

    @Autowired
    private VendorLotReceiveCase vendorLotReceiveCase;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private StbTestCase stbTestCase;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private LotHoldCase lotHoldCase;

    @Autowired
    private LotSplitTestCase lotSplitTestCase;

    @Autowired
    private LotMergeTestCase lotMergeTestCase;

    @Autowired
    private OperationSkipTestCase operationSkipTestCase;

    @Autowired
    private EquipmentTestCase equipmentTestCase;

    @Autowired
    private EngineerDataCollectionController dataCollectionController;

    @Autowired
    private EngineerDataCollectionInqController dataCollectionInqController;

    @Autowired
    private MoveToSelfCase moveToSelfCase;

    @Autowired
    private LoadForInternalBufferCase loadForInternalBufferCase;

    @Autowired
    private MoveInForInternalBufferCase moveInForInternalBufferCase;

    @Autowired
    private MoveOutForInternalBufferCase moveOutForInternalBufferCase;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    private static String PROCESS_MONITOR_NORMALEQP = "1SRT01";

    private static String PROCESS_MONITOR_SPECILEQP = "1THK01";

    private static String PROCESS_MONITOR_BUFFEREQP = "1FHI01";

    private static String PROCESS_MONITOR_STB_AFTER_EQP = "1FHI02_NORM";

    private static String LOAD_PORT_ONE = "P1";

    private static String LOAD_PORT_TWO = "P2";


    public void Put_MonitorLot_Grouping_Into_MonitorLot() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier firstMonitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】NPW stb on second monitor lot of one wafer
        Response response2 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier secondMonitorLotID = (ObjectIdentifier) response2.getBody();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(firstMonitorLotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】lot info search for first monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(secondMonitorLotID);
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step6】get monitor grouping  for first monitor lot
        ObjectIdentifier monitorLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step7】get lot_list_in_cassette for second monitor lot
        ObjectIdentifier secondCassetteID = secondLotInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(secondCassetteID).getBody();
        secondMonitorLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step8】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,secondMonitorLotID);
    }

    public void Bind_MonitorLot_To_The_ProductLot() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        Response response2 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();
        ObjectIdentifier monitorLotID2 = (ObjectIdentifier) response2.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】lot info search for first monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step6】get monitor grouping  for first monitor lot
        monitorLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step7】get lot_list_in_cassette for second monitor lot
        ObjectIdentifier secondCassetteID = secondLotInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(secondCassetteID).getBody();
        productLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step8】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,productLotID);

        //【step9】grouping the same product lot to another monitor lot
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID2,productLotID);
    }

    public void One_MonitorLot_Binds_MultipleLots() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】search products to stb, the product count is 10, and Product count is 10.

        //【step3-1】make default setting
        String bankID1 = "BNK-0S";
        String sourceProductID1 = "RAW-2000.01";
        String productID1 = "PRODUCT0.01";


        //【step3-2】make material prepared
        String vendorLotID1 = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID1,vendorLotID1,sourceProductID1, 100);
        Long productCount1 = 10L;

        //【step3-3】make stb to generate first Lot
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID1,productCount1);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();
        Results.WaferLotStartReqResult body = (Results.WaferLotStartReqResult) stbTestCase.waferLotStartReqCase(lotID, vendorLotID1, productCount1).getBody();

        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(body.getLotID());
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier cassetteID = lotInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step3-4】make stb to generate second Lot use same cassetteID
        Infos.ProdReqListAttribute prodReqListAttribute2 = stbTestCase.productOrderReleasedListInqCase(productID1, productCount1);
        ObjectIdentifier lotID2 = prodReqListAttribute2.getLotID();
        stbTestCase.waferLotStartReqCase(lotID2, cassetteID, vendorLotID1, productCount1);

        //【step4】lot info search for first monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】get monitor grouping  for  monitor lot
        monitorLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step6】get lot_list_in_cassette for product lot and get the first
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(cassetteID).getBody();
        ObjectIdentifier firstLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step7】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,firstLotID);
    }

    public void One_ProcessLot_CanNot_Have_MoreThan_One_Grouping() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier firstMonitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】NPW stb on second monitor lot of one wafer
        Response response2 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier secondMonitorLotID = (ObjectIdentifier) response2.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for product lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】lot info search for firstMonitorLot
        lotIDs = new ArrayList<>();
        lotIDs.add(firstMonitorLotID);
        Results.LotInfoInqResult firstMonitorLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step6】lot info search for secondMonitorLot
        lotIDs = new ArrayList<>();
        lotIDs.add(secondMonitorLotID);
        Results.LotInfoInqResult secondMonitorLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step7】get monitor grouping  for product lot
        productLotID = productLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step8】get lot_list_in_cassette for first monitor lot
        ObjectIdentifier firstMonitorCassetteID = firstMonitorLotInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(firstMonitorCassetteID).getBody();
        firstMonitorLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step9】get lot_list_in_cassette for second monitor lot
        ObjectIdentifier secondMonitorCassetteID = secondMonitorLotInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult1 = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(secondMonitorCassetteID).getBody();
        secondMonitorLotID = lotListByCarrierInqResult1.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step10】confirm monitor grouping first time
        processMonitorTestCase.monitorBatchCreateReqCase(productLotID,firstMonitorLotID);

        //【step11】confirm monitor grouping second time
        try {
            processMonitorTestCase.monitorBatchCreateReqCase(productLotID,secondMonitorLotID);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getAlreadyExistMonitorGroup(),e.getCode())){
                 throw e;
            }
        }
    }

    public void One_MonitoringLot_Can_Only_Have_One_Grouping() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】search products to stb, the product count is 10, and Product count is 10.

        //【step3-1】make default setting
        String bankID1 = "BNK-0S";
        String sourceProductID1 = "RAW-2000.01";
        String productID1 = "PRODUCT0.01";


        //【step3-2】make material receive
        String vendorLotID1 = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID1,vendorLotID1,sourceProductID1, 100);
        Long productCount1 = 10L;

        //【step3-3】make stb to generate first Lot
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID1,productCount1);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();
        Results.WaferLotStartReqResult body = (Results.WaferLotStartReqResult) stbTestCase.waferLotStartReqCase(lotID, vendorLotID1, productCount1).getBody();

        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(body.getLotID());
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier cassetteID = lotInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step3-4】make stb to generate second Lot use same cassetteID
        Infos.ProdReqListAttribute prodReqListAttribute2 = stbTestCase.productOrderReleasedListInqCase(productID1, productCount1);
        ObjectIdentifier lotID2 = prodReqListAttribute2.getLotID();
        stbTestCase.waferLotStartReqCase(lotID2, cassetteID, vendorLotID1, productCount1);

        //【step4】lot info search for first monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】get monitor grouping  for  monitor lot
        monitorLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step6】get lot_list_in_cassette for product lot and get the first lotID and second lotID
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(cassetteID).getBody();
        ObjectIdentifier firstLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);
        ObjectIdentifier secondLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(1);

        //【step7】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,firstLotID);

        //【step8】confirm monitor grouping
        try {
            processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,secondLotID);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getAlreadyExistMonitorGroup(),e.getCode())){
                throw e;
            }
        }
    }

    public void LotA_Bound_LotB_And_LotB_Bound_LotC() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier firstMonitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】NPW stb on first monitor lot of one wafer
        Response response2 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier secondMonitorLotID = (ObjectIdentifier) response2.getBody();

        //【step3】NPW stb on first monitor lot of one wafer
        Response response3 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier thirdMonitorLotID = (ObjectIdentifier) response3.getBody();

        //【step4】lot info search for second monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(firstMonitorLotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】lot info search for first monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(secondMonitorLotID);
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】lot info search for third monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(thirdMonitorLotID);
        Results.LotInfoInqResult thirdLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step6】get monitor grouping  for first monitor lot
        firstMonitorLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step7】get lot_list_in_cassette for second monitor lot
        ObjectIdentifier secondCassetteID = secondLotInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(secondCassetteID).getBody();
        secondMonitorLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step8】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(firstMonitorLotID,secondMonitorLotID);

        //【step9】get lot_list_in_cassette for third monitor lot
        ObjectIdentifier thirdCassetteID = thirdLotInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult1 = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(thirdCassetteID).getBody();
        thirdMonitorLotID = lotListByCarrierInqResult1.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step10】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(secondMonitorLotID,thirdMonitorLotID);
    }

    public void GroupingCancel_OneMonitorLot_Grouping_OneMonitorLot() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier firstMonitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】NPW stb on second monitor lot of one wafer
        Response response2 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier secondMonitorLotID = (ObjectIdentifier) response2.getBody();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(firstMonitorLotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】lot info search for first monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(secondMonitorLotID);
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step6】get monitor grouping  for first monitor lot
        ObjectIdentifier monitorLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step7】get lot_list_in_cassette for second monitor lot
        ObjectIdentifier secondCassetteID = secondLotInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(secondCassetteID).getBody();
        secondMonitorLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step8】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,secondMonitorLotID);

        //【step9】get monitor_prod_lots_relation list
        List<Infos.MonitorGroups> monitorProdLotsRelation = processMonitorTestCase.getMonitorProdLotsRelationCase(firstMonitorLotID, firstLotInfoCase.getLotListInCassetteInfo().getCassetteID());
        if (CimArrayUtils.getSize(monitorProdLotsRelation) > 0){
            Infos.MonitorGroups monitorGroups = monitorProdLotsRelation.get(0);
            ObjectIdentifier cancelMonitorLotID = monitorGroups.getMonitorLotID();
            //【step10】monitorBatchDeleteReq
            processMonitorTestCase.monitorBatchDeleteReqCase(cancelMonitorLotID);
        }
    }

    public void GroupingCancel_OneMonitorLot_Grouping_OneProcessLot() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for product lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】lot info search for firstMonitorLot
        lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();


        //【step6】get monitor grouping  for product lot
        productLotID = productLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step8】get lot_list_in_cassette for  monitor lot
        ObjectIdentifier monitorCassetteID = monitorLotInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(monitorCassetteID).getBody();
        monitorLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step9】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,productLotID);

        //【step10】get monitor_prod_lots_relation list
        List<Infos.MonitorGroups> monitorProdLotsRelation = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID, monitorCassetteID);
        if (CimArrayUtils.getSize(monitorProdLotsRelation) > 0){
            Infos.MonitorGroups monitorGroups = monitorProdLotsRelation.get(0);
            ObjectIdentifier cancelMonitorLotID = monitorGroups.getMonitorLotID();
            //【step10】monitorBatchDeleteReq
            processMonitorTestCase.monitorBatchDeleteReqCase(cancelMonitorLotID);
        }
    }

    public void GroupingCancel_OneMonitorLot_Grouping_MultipleProcessLots() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】search products to stb, the product count is 10, and Product count is 10.

        //【step3-1】make default setting
        String bankID1 = "BNK-0S";
        String sourceProductID1 = "RAW-2000.01";
        String productID1 = "PRODUCT0.01";


        //【step3-2】make material receive
        String vendorLotID1 = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID1,vendorLotID1,sourceProductID1, 100);
        Long productCount1 = 10L;

        //【step3-3】make stb to generate first Lot
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID1,productCount1);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();
        Results.WaferLotStartReqResult body = (Results.WaferLotStartReqResult) stbTestCase.waferLotStartReqCase(lotID, vendorLotID1, productCount1).getBody();

        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(body.getLotID());
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier cassetteID = lotInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step3-4】make stb to generate second Lot use same cassetteID
        Infos.ProdReqListAttribute prodReqListAttribute2 = stbTestCase.productOrderReleasedListInqCase(productID1, productCount1);
        ObjectIdentifier lotID2 = prodReqListAttribute2.getLotID();
        stbTestCase.waferLotStartReqCase(lotID2, cassetteID, vendorLotID1, productCount1);

        //【step4】lot info search for first monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】get monitor grouping  for  monitor lot
        monitorLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step6】get lot_list_in_cassette for product lot and get the first and second
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(cassetteID).getBody();
        ObjectIdentifier firstLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);
        ObjectIdentifier secondLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(1);

        //【step7】confirm monitor grouping
        lotIDs = new ArrayList<>();
        lotIDs.add(firstLotID);
        lotIDs.add(secondLotID);
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,lotIDs);

        //【step10】get monitor_prod_lots_relation list
        List<Infos.MonitorGroups> monitorProdLotsRelation = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID, firstLotInfoCase.getLotListInCassetteInfo().getCassetteID());
        if (CimArrayUtils.getSize(monitorProdLotsRelation) > 0){
            Infos.MonitorGroups monitorGroups = monitorProdLotsRelation.get(0);
            ObjectIdentifier cancelMonitorLotID = monitorGroups.getMonitorLotID();
            //【step10】monitorBatchDeleteReq
            processMonitorTestCase.monitorBatchDeleteReqCase(cancelMonitorLotID);
        }
    }

    public void GroupingCancel_OneProcessLot_Can_Have_MoreThanOne_Grouping() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】search products to stb, the product count is 10, and Product count is 10.

        //【step3-1】make default setting
        String bankID1 = "BNK-0S";
        String sourceProductID1 = "RAW-2000.01";
        String productID1 = "PRODUCT0.01";


        //【step3-2】make material receive
        String vendorLotID1 = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID1,vendorLotID1,sourceProductID1, 100);
        Long productCount1 = 10L;

        //【step3-3】make stb to generate first Lot
        Infos.ProdReqListAttribute prodReqListAttribute = stbTestCase.productOrderReleasedListInqCase(productID1,productCount1);
        ObjectIdentifier lotID = prodReqListAttribute.getLotID();
        Results.WaferLotStartReqResult body = (Results.WaferLotStartReqResult) stbTestCase.waferLotStartReqCase(lotID, vendorLotID1, productCount1).getBody();
        lotID = body.getLotID();

        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier cassetteID = lotInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step3-4】make stb to generate second Lot use same cassetteID
        Infos.ProdReqListAttribute prodReqListAttribute2 = stbTestCase.productOrderReleasedListInqCase(productID1, productCount1);
        ObjectIdentifier lotID2 = prodReqListAttribute2.getLotID();
        Results.WaferLotStartReqResult body2 = (Results.WaferLotStartReqResult)stbTestCase.waferLotStartReqCase(lotID2, cassetteID, vendorLotID1, productCount1).getBody();
        lotID2 = body2.getLotID();

        //【step4】lot info search for first monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】get monitor grouping  for  monitor lotID and CassetteID
        monitorLotID = monitorLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();
        ObjectIdentifier monitorCassetteID = monitorLotInfoCase.getLotListInCassetteInfo().getCassetteID();


        //【step6】get lot_list_in_cassette for product lot and get the first and second
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(monitorCassetteID).getBody();
        monitorLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step97】confirm monitor grouping for first product lot with monitor lot
        processMonitorTestCase.monitorBatchCreateReqCase(lotID,monitorLotID);

        //【step8】confirm monitor grouping for first product lot with same monitor lot
        processMonitorTestCase.monitorBatchCreateReqCase(lotID2,monitorLotID);

        //【step9】get monitor_prod_lots_relation list
        List<Infos.MonitorGroups> monitorProdLotsRelation = processMonitorTestCase.getMonitorProdLotsRelationCase(lotID,cassetteID);
        if (CimArrayUtils.getSize(monitorProdLotsRelation) > 0){
            Infos.MonitorGroups monitorGroups = monitorProdLotsRelation.get(0);
            ObjectIdentifier cancelMonitorLotID = monitorGroups.getMonitorLotID();
            //【step10】monitorBatchDeleteReq first
            processMonitorTestCase.monitorBatchDeleteReqCase(cancelMonitorLotID);
        }

        //【step11】get monitor_prod_lots_relation list
        List<Infos.MonitorGroups> monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(lotID2,cassetteID);
        if (CimArrayUtils.getSize(monitorProdLotsRelation1) > 0){
            Infos.MonitorGroups monitorGroups = monitorProdLotsRelation1.get(0);
            ObjectIdentifier cancelMonitorLotID = monitorGroups.getMonitorLotID();
            //【step12】monitorBatchDeleteReq second
            processMonitorTestCase.monitorBatchDeleteReqCase(cancelMonitorLotID);
        }
    }

    public void GroupingCancel_A_GroupingB_And_BGroupingC() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier firstMonitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】NPW stb on first monitor lot of one wafer
        Response response2 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier secondMonitorLotID = (ObjectIdentifier) response2.getBody();

        //【step3】NPW stb on first monitor lot of one wafer
        Response response3 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier thirdMonitorLotID = (ObjectIdentifier) response3.getBody();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(firstMonitorLotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】lot info search for second monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(secondMonitorLotID);
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】lot info search for third monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(thirdMonitorLotID);
        Results.LotInfoInqResult thirdLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step6】get monitor grouping  for first monitor lot
        firstMonitorLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step7】get lot_list_in_cassette for second monitor lot
        ObjectIdentifier secondCassetteID = secondLotInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(secondCassetteID).getBody();
        secondMonitorLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step8】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(firstMonitorLotID,secondMonitorLotID);

        //【step9】get lot_list_in_cassette for third monitor lot
        ObjectIdentifier thirdCassetteID = thirdLotInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult1 = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(thirdCassetteID).getBody();
        thirdMonitorLotID = lotListByCarrierInqResult1.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step10】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(secondMonitorLotID,thirdMonitorLotID);

        //【step11】 get monitor_prod_lots_relation list
        List<Infos.MonitorGroups> monitorProdLotsRelation = processMonitorTestCase.getMonitorProdLotsRelationCase(firstMonitorLotID,firstLotInfoCase.getLotListInCassetteInfo().getCassetteID());
        if (CimArrayUtils.getSize(monitorProdLotsRelation) > 0){
            Infos.MonitorGroups monitorGroups = monitorProdLotsRelation.get(0);
            ObjectIdentifier cancelMonitorLotID = monitorGroups.getMonitorLotID();
            //【step12】monitorBatchDeleteReq first
            processMonitorTestCase.monitorBatchDeleteReqCase(cancelMonitorLotID);
        }

        //【step13】get monitor_prod_lots_relation list
        List<Infos.MonitorGroups> monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(secondMonitorLotID,secondCassetteID);
        if (CimArrayUtils.getSize(monitorProdLotsRelation1) > 0){
            Infos.MonitorGroups monitorGroups = monitorProdLotsRelation1.get(0);
            ObjectIdentifier cancelMonitorLotID = monitorGroups.getMonitorLotID();
            //【step14】monitorBatchDeleteReq second
            processMonitorTestCase.monitorBatchDeleteReqCase(cancelMonitorLotID);
        }
    }

    public void LotHold_For_MonitorGrouping() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 1;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot hold the product lot
        final String reasonCode = "SOHL";
        final String reasonableOperation = "C";
        lotHoldCase.lotHold(productLotID.getValue(),reasonCode,reasonableOperation);

        //【step5】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step6】lot info search for product lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step7】get lot_list_in_cassette for second monitor lot
        ObjectIdentifier productCassetteID = productLotInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(productCassetteID).getBody();
        productLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step8】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,productLotID);
    }

    public void MonitorLot_Did_Split_First_AndThen_Merge() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step4】split monitor lot on current route
        List<Infos.LotWaferAttributes> lotWaferAttributesList = monitorInfoCase.getLotInfoList().get(0).getLotWaferAttributesList();

        //【step5】lot split
        List<Infos.LotWaferAttributes> subLotWaferAttributesList = new ArrayList<>();
        int size = new Random().nextInt(lotWaferAttributesList.size()) + 1;
        for (int i = 0; i < size; i++) {
            subLotWaferAttributesList.add(lotWaferAttributesList.get(i));
        }
        List<ObjectIdentifier> childWaferIDs = subLotWaferAttributesList.stream().map(Infos.LotWaferAttributes::getWaferID).collect(Collectors.toList());
        ObjectIdentifier childMonitorLotID = (ObjectIdentifier) lotSplitTestCase.splitLotReq(childWaferIDs, false, false,
                "", new ObjectIdentifier(), monitorLotID, new ObjectIdentifier()).getBody();

        //【step6】lot info
        lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        lotIDs.add(childMonitorLotID);
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        List<ObjectIdentifier> lotIDInCassetteList = lotInfoInqResult2.getLotListInCassetteInfo().getLotIDList();
        Assert.isTrue(lotIDInCassetteList.size() == 2 ,"the lot count in cassette must be 2");

        //【step7】get lot_list_in_cassette for monitor lot parent
        ObjectIdentifier monitorCassetteID = monitorInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(monitorCassetteID).getBody();
        childMonitorLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(1);
        monitorLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step8】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step9】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,productLotID);

        //【step10】merge confirm
        lotMergeTestCase.mergeLotReq(monitorLotID, childMonitorLotID);

        //【step11】lot info
        lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult lotInfoInqResult3 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        List<ObjectIdentifier> lotIDInCassetteList3 = lotInfoInqResult3.getLotListInCassetteInfo().getLotIDList();
        Assert.isTrue(lotIDInCassetteList3.size() == 1 ,"the lot count in cassette must be 1 after merge");
    }

    public void Grouped_MonitorLot_ShouldNot_Skip() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】lot info search for product lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step6】get monitor grouping  for first monitor lot
        monitorLotID = monitorInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step7】get lot_list_in_cassette for product lot
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(productCassetteID).getBody();
        productLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step8】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,productLotID);

        //【step9】skip for monitor lot
        Infos.LotInfo lotInfo = monitorInfoCase.getLotInfoList().get(0);

        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(monitorLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes1 = operationNameAttributesList1.get(operationNameAttributesList1.size()/2);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(monitorLotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        try {
            operationSkipTestCase.operationSkip(skipReqParams);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getAlreadyExistMonitorGroup(),e.getCode())){
                throw e;
            }
        }
    }

    public void Grouped_ProcessLot_Can_Skip_If_NotOnHold() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】lot info search for product lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step6】get monitor grouping  for first monitor lot
        monitorLotID = monitorInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotID();

        //【step7】get lot_list_in_cassette for product lot
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(productCassetteID).getBody();
        productLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);

        //【step8】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,productLotID);

        //【step9】skip for product lot
        Infos.LotInfo lotInfo = productInfoCase.getLotInfoList().get(0);

        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(productLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes1 = operationNameAttributesList1.get(3);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(productLotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);

        operationSkipTestCase.operationSkip(skipReqParams);
    }

    public void Grouped_MonitorLot_Split_And_TheChildLot_DoesNot_Have_TheGroupInfo_But_TheMotherLot_Have_TheGroupInfo() {

        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step5】split monitor lot on current route
        List<Infos.LotWaferAttributes> lotWaferAttributesList = monitorInfoCase.getLotInfoList().get(0).getLotWaferAttributesList();

        //【step6】lot split
        List<Infos.LotWaferAttributes> subLotWaferAttributesList = new ArrayList<>();
        int size = new Random().nextInt(lotWaferAttributesList.size()) + 1;
        for (int i = 0; i < size; i++) {
            subLotWaferAttributesList.add(lotWaferAttributesList.get(i));
        }
        List<ObjectIdentifier> childWaferIDs = subLotWaferAttributesList.stream().map(Infos.LotWaferAttributes::getWaferID).collect(Collectors.toList());
        ObjectIdentifier childMonitorLotID = (ObjectIdentifier) lotSplitTestCase.splitLotReq(childWaferIDs, false, false,
                "", new ObjectIdentifier(), monitorLotID, new ObjectIdentifier()).getBody();

        //【step7】lot info
        lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        lotIDs.add(childMonitorLotID);
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        List<ObjectIdentifier> lotIDInCassetteList = lotInfoInqResult2.getLotListInCassetteInfo().getLotIDList();
        Assert.isTrue(lotIDInCassetteList.size() == 2 ,"the lot count in cassette must be 2");

        //【step8】get lot_list_in_cassette for monitor lot parent
        ObjectIdentifier monitorCassetteID = monitorInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(monitorCassetteID).getBody();
        childMonitorLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(0);
        monitorLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().get(1);

        //【step9】confirm monitor grouping
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,productLotID);

        //【step10】get monitor_prod_lots_relation list for parent monitor lot
        List<Infos.MonitorGroups> monitorProdLotsRelationParent = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelationParent) > 0),"parent monitor lot monitor group info must not be null");

        //【step11】lot info search for child monitor lot
        lotIDs = new ArrayList<>();
        lotIDs.add(childMonitorLotID);
        Results.LotInfoInqResult childMonitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier childCassetteID = childMonitorInfoCase.getLotListInCassetteInfo().getCassetteID();

        List<Infos.MonitorGroups> monitorProdLotsRelationChild = processMonitorTestCase.getMonitorProdLotsRelationCase(childMonitorLotID,childCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelationChild) == 0),"child monitor lot monitor group info must be null");
    }

    public void Load_Two_ProductionLots_WithOut_Reserve_And_One_LoadPurpose_As_ProcessLot_And_AnotherOne_LoadPurpose_As_ProcessMonitorLot_And_Check_ProcessLot_OnHold() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier monitorCassetteID = monitorInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step5】lot info search for product lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step6】monitor lot skip to 1000.0200
        String operationNum1 = "1000.0200";
        Infos.LotInfo lotInfo = monitorInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(monitorLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,operationNum1);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(monitorLotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step7】product lot skip to 2000.0200
        String operationNum2 = "2000.0200";
        Infos.LotInfo lotInfo2 = productInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(productLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes2 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList2,operationNum2);

        Params.SkipReqParams skipReqParams2 = new Params.SkipReqParams();
        skipReqParams2.setCurrentOperationNumber(lotInfo2.getLotOperationInfo().getOperationNumber());
        skipReqParams2.setCurrentRouteID(lotInfo2.getLotOperationInfo().getRouteID());
        skipReqParams2.setLocateDirection(true);
        skipReqParams2.setLotID(productLotID);
        skipReqParams2.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams2.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams2.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams2.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams2.setSeqno(-1);
        skipReqParams2.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams2);

        //【step8】 step to eqp 1SRT01
        //【step9】einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP));

        //【step10】load purpose productLot
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP),productCassetteID,new ObjectIdentifier(LOAD_PORT_ONE) , purposeList.get(0));

        //【step11】load purpose monitorLot
        purposeList = new ArrayList<>();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSMONITORLOT);
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP),monitorCassetteID,new ObjectIdentifier(LOAD_PORT_TWO) , purposeList.get(0));

        //【step12】move in the product lot and monitor lot
        // 【step12-1】get lotsInfoForOpeStart info
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(productCassetteID);
        cassetteIDs.add(monitorCassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP), cassetteIDs).getBody();
        // 【step12-2】move in
        Results.MoveInReqResult moveInResult = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList()).getBody();

        //【step13-1】move out the two lot
        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInResult.getControlJobID());
        opeComWithDataReqParams.setUser(lotSplitTestCase.getUser());
        opeComWithDataReqParams.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP));
        equipmentTestCase.moveOut(opeComWithDataReqParams);

        //【step14】return the lot info check grouping info
        List<Infos.MonitorGroups> monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation1) > 0),"monitor lot grouping failed");

        List<Infos.MonitorGroups> monitorProdLotsRelation2 = processMonitorTestCase.getMonitorProdLotsRelationCase(productLotID,productCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation2) > 0),"monitor lot grouping failed");

        Boolean compareFlag1 = false;
        Boolean compareFlag2 = false;
        if (CimStringUtils.equals(monitorProdLotsRelation1.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag1 = true;
        }
        if (CimStringUtils.equals(monitorProdLotsRelation2.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag2 = true;
        }
        Validations.assertCheck(compareFlag1 && compareFlag2,"monitor lot and product lot does not have grouping relation");

        //【step15】check process product lot is onHold
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus,BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the product lot must be onHold");
    }

    public void Load_Two_ProductionLots_WithOut_Reserve_And_One_LoadPurpose_As_ProcessLot_And_AnotherOne_LoadPurpose_As_ProcessMonitorLot_And_Check_ProcessLot_OnHold_For_InternalBuffer(){
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 25;


        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        String productbankID = "BNK-0S";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(productbankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier productLotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier monitorCassetteID = monitorInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step5】lot info search for product lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step6】monitor lot skip to 1000.0200
        String operationNum1 = "1000.0200";
        Infos.LotInfo lotInfo = monitorInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(monitorLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,operationNum1);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(monitorLotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        ObjectIdentifier equipmentID = new ObjectIdentifier(PROCESS_MONITOR_BUFFEREQP);
        equipmentTestCase.changeOperationModeToOffLine1(equipmentID, BizConstant.SP_MC_CATEGORY_INTERNALBUFFER);

        CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        cimMachine.setMultipleRecipeCapability("Single Recipe");
        cimMachine.makeCassetteChangeRequiredOff();
        cimMachine.makeMonitorCreationOff();
        //【step8】load purpose productLot

        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(productCassetteID);
        loadForInternalBufferInfo.setEquipmentID(equipmentID);
        loadForInternalBufferInfo.setLoadPurposeType(BizConstant.SP_BUFFERCATEGORY_PROCESSLOT);
        loadForInternalBufferInfo.setLotID(productLotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier(LOAD_PORT_ONE));
        loadForInternalBufferInfo.setNeedStartReserved(false);
        loadForInternalBufferCase.load(loadForInternalBufferInfo);

        //【step10】move to self
        TestInfos.MoveToSelfInfo productMoveToSelfInfo = new TestInfos.MoveToSelfInfo(productCassetteID, new ObjectIdentifier(LOAD_PORT_ONE), new ObjectIdentifier(PROCESS_MONITOR_BUFFEREQP));
        moveToSelfCase.movetoSelf(productMoveToSelfInfo);

        //【step9】load purpose monitorLot
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo1 = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo1.setCassetteID(monitorCassetteID);
        loadForInternalBufferInfo1.setEquipmentID(equipmentID);
        loadForInternalBufferInfo1.setLoadPurposeType(BizConstant.SP_BUFFERCATEGORY_PROCESSMONITORLOT);
        loadForInternalBufferInfo1.setLotID(monitorLotID);
        loadForInternalBufferInfo1.setPortID(new ObjectIdentifier(LOAD_PORT_TWO));
        loadForInternalBufferInfo1.setNeedStartReserved(false);
        loadForInternalBufferCase.load(loadForInternalBufferInfo1);

        TestInfos.MoveToSelfInfo monitorMoveToSelfInfo = new TestInfos.MoveToSelfInfo(monitorCassetteID, new ObjectIdentifier(LOAD_PORT_TWO), new ObjectIdentifier(PROCESS_MONITOR_BUFFEREQP));
        moveToSelfCase.movetoSelf(monitorMoveToSelfInfo);

//        ObjectIdentifier emptyCarrier = bankTestCase.getEmptyCassette();
//        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo2 = new TestInfos.LoadForInternalBufferInfo();
//        loadForInternalBufferInfo2.setCassetteID(emptyCarrier);
//        loadForInternalBufferInfo2.setEquipmentID(equipmentID);
//        loadForInternalBufferInfo2.setLoadPurposeType(BizConstant.SP_BUFFERCATEGORY_EMPTYCASSETTE);
//        loadForInternalBufferInfo2.setPortID(new ObjectIdentifier(LOAD_PORT_ONE));
//        loadForInternalBufferInfo2.setNeedStartReserved(false);
//        loadForInternalBufferCase.load(loadForInternalBufferInfo2);
//
//        TestInfos.MoveToSelfInfo emptyMoveToSelfInfo = new TestInfos.MoveToSelfInfo(emptyCarrier, new ObjectIdentifier(LOAD_PORT_ONE), new ObjectIdentifier(PROCESS_MONITOR_BUFFEREQP));
//        moveToSelfCase.movetoSelf(emptyMoveToSelfInfo);


        //【step11】move in
        TestInfos.MoveInForInternalBufferInfo moveInForInternalBufferInfo = new TestInfos.MoveInForInternalBufferInfo();
        moveInForInternalBufferInfo.setEquipmentID(equipmentID);
        moveInForInternalBufferInfo.setProcessJobPauseFlag(false); // when not do start reserve, it's false
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        cassetteIDList.add(productCassetteID);
        cassetteIDList.add(monitorCassetteID);
     //   cassetteIDList.add(emptyCarrier);
        moveInForInternalBufferInfo.setCassetteIDList(cassetteIDList);
        moveInForInternalBufferCase.moveIn(moveInForInternalBufferInfo);

        //【step12】check the lot info screem have control job or not.
        ObjectIdentifier controlJobID = testUtils.getControlJobIDByLotID(productLotID);
        Validations.check(null == controlJobID, "null == controlJobID");



        //【step13】move out
        Params.MoveOutForIBReqParams params = new Params.MoveOutForIBReqParams();
        params.setUser(testUtils.getUser());
        params.setEquipmentID(equipmentID);
        params.setControlJobID(controlJobID);
        params.setSpcResultRequiredFlag(false);
        moveOutForInternalBufferCase.moveOut(params);


        //【step14】return the lot info check grouping info
        List<Infos.MonitorGroups> monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation1) > 0),"monitor lot grouping failed");

        List<Infos.MonitorGroups> monitorProdLotsRelation2 = processMonitorTestCase.getMonitorProdLotsRelationCase(productLotID,productCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation2) > 0),"monitor lot grouping failed");

        Boolean compareFlag1 = false;
        Boolean compareFlag2 = false;
        if (CimStringUtils.equals(monitorProdLotsRelation1.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag1 = true;
        }
        if (CimStringUtils.equals(monitorProdLotsRelation2.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag2 = true;
        }
        Validations.assertCheck(compareFlag1 && compareFlag2,"monitor lot and product lot does not have grouping relation");

        //【step15】check process product lot is onHold
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus,BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the product lot must be onHold");
    }

    public void Reserve_Tow_ProductionLots_And_One_LoadPurpose_As_ProcessLot_And_AnotherOne_LoadPurpose_As_ProcessMonitorLot() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier monitorCassetteID = monitorInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step5】lot info search for product lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step6】monitor lot skip to 1000.0200
        String operationNum1 = "1000.0200";
        Infos.LotInfo lotInfo = monitorInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(monitorLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,operationNum1);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(monitorLotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step7】product lot skip to 2000.0200
        String operationNum2 = "2000.0200";
        Infos.LotInfo lotInfo2 = productInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(productLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes2 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList2,operationNum2);

        Params.SkipReqParams skipReqParams2 = new Params.SkipReqParams();
        skipReqParams2.setCurrentOperationNumber(lotInfo2.getLotOperationInfo().getOperationNumber());
        skipReqParams2.setCurrentRouteID(lotInfo2.getLotOperationInfo().getRouteID());
        skipReqParams2.setLocateDirection(true);
        skipReqParams2.setLotID(productLotID);
        skipReqParams2.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams2.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams2.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams2.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams2.setSeqno(-1);
        skipReqParams2.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams2);

        //【step8】 step to eqp 1SRT01
        //【step9】einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP));

        //【step10】 start reserve the two lot
        //【step10 -1】dispatch/what_next_lot_list/inq
        Results.WhatNextLotListResult whatNextLotListResult = (Results.WhatNextLotListResult) equipmentTestCase.whatNextInqCase(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP)).getBody();
        List<Infos.WhatNextAttributes> whatNextAttributesContent = (List<Infos.WhatNextAttributes> ) whatNextLotListResult.getWhatNextAttributesPage().getContent();
        Infos.WhatNextAttributes whatNextAttributesMonitor = whatNextAttributesContent.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getLotID(), monitorLotID)).findFirst().orElse(null);
        Infos.WhatNextAttributes whatNextAttributesProduct = whatNextAttributesContent.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getLotID(), productLotID)).findFirst().orElse(null);
        if (whatNextAttributesMonitor == null || whatNextAttributesProduct == null) {
            return;
        }

        //【step10-2】 einfo/lot_list_in_cassette/inq
        Results.LotListByCarrierInqResult lotListByCarrierInqResultMonitor = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(whatNextAttributesMonitor.getCassetteID()).getBody();
        Results.LotListByCarrierInqResult lotListByCarrierInqResultProduct = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(whatNextAttributesProduct.getCassetteID()).getBody();

        //【step10-3】einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP));

        //【step10-4】 einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResultMonitor = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotListByCarrierInqResultMonitor.getLotListInCassetteInfo().getLotIDList()).getBody();
        Results.LotInfoInqResult lotInfoInqResultProduct = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotListByCarrierInqResultProduct.getLotListInCassetteInfo().getLotIDList()).getBody();

        List<Results.LotInfoInqResult> lotInfoInqResultList = new ArrayList<>();
        lotInfoInqResultList.add(lotInfoInqResultMonitor);
        lotInfoInqResultList.add(lotInfoInqResultProduct);

        //【step10-5】dispatch/lots_info_for_start_reservation/inq
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult = (Results.LotsMoveInReserveInfoInqResult) equipmentTestCase.lotsMoveInReserveInfoInqCase(lotInfoInqResultList,new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP)).getBody();

        //【step10-6】start reserve for two lot
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSMONITORLOT);
        equipmentTestCase.moveInReserveReqForMonitorLotAndProductLotCase(lotsMoveInReserveInfoInqResult,monitorCassetteID,productCassetteID, purposeList,new ObjectIdentifier(LOAD_PORT_ONE),new ObjectIdentifier(LOAD_PORT_TWO));

        Results.EqpInfoInqResult infoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP)).getBody();

        //【step11】load purpose monitorLot
        purposeList = new ArrayList<>();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSMONITORLOT);
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP),monitorCassetteID,new ObjectIdentifier(LOAD_PORT_ONE) , purposeList.get(0));

        //【step11-1】load purpose productLot
        purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP),productCassetteID,new ObjectIdentifier(LOAD_PORT_TWO) , purposeList.get(0));

        //【step12】move in the product lot and monitor lot
        // 【step12-1】get lotsInfoForOpeStart info
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(productCassetteID);
        cassetteIDs.add(monitorCassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP), cassetteIDs).getBody();
        // 【step12-2】move in
        Results.MoveInReqResult moveInResult = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList()).getBody();

        //【step13-1】move out the two lot
        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInResult.getControlJobID());
        opeComWithDataReqParams.setUser(lotSplitTestCase.getUser());
        opeComWithDataReqParams.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP));
        equipmentTestCase.moveOut(opeComWithDataReqParams);

        //【step14】return the lot info check grouping info
        List<Infos.MonitorGroups> monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation1) > 0),"monitor lot grouping failed");

        List<Infos.MonitorGroups> monitorProdLotsRelation2 = processMonitorTestCase.getMonitorProdLotsRelationCase(productLotID,productCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation2) > 0),"monitor lot grouping failed");

        Boolean compareFlag1 = false;
        Boolean compareFlag2 = false;
        if (CimStringUtils.equals(monitorProdLotsRelation1.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag1 = true;
        }
        if (CimStringUtils.equals(monitorProdLotsRelation2.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag2 = true;
        }
        Validations.assertCheck(compareFlag1 && compareFlag2,"monitor lot and product lot does not have grouping relation");
    }

    public void ManuelGrouping_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OK() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier monitorCassetteID = monitorInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step5】lot info search for product lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step6】monitor lot skip to 1000.0300
        String operationNum1 = "1000.0300";
        Infos.LotInfo lotInfo = monitorInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(monitorLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,operationNum1);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(monitorLotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step7】monitor grouping with product lot on 1000.0300
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,productLotID);

        //【step8】step to eqp 1THK01
        electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));

        //【step9】load the monitor lot
        List<String> purposeList = new ArrayList<>();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSLOT);
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP),monitorCassetteID,new ObjectIdentifier(LOAD_PORT_ONE) , purposeList.get(0));

        //【step10】move in the monitor lot
        // 【step10-1】get lotsInfoForOpeStart info
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(monitorCassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP), cassetteIDs).getBody();
        // 【step10-2】move in
        Results.MoveInReqResult moveInResult = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList()).getBody();

        //【step11】spec check in limit
        //【step11-1】data collection
        Params.EDCDataItemWithTransitDataInqParams edcDataItemWithTransitDataInqParams=new Params.EDCDataItemWithTransitDataInqParams();
        edcDataItemWithTransitDataInqParams.setControlJobID(moveInResult.getControlJobID());
        edcDataItemWithTransitDataInqParams.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));
        edcDataItemWithTransitDataInqParams.setUser(lotSplitTestCase.getUser());
        Results.EDCDataItemWithTransitDataInqResult edcDataItemWithTransitDataInqResult= (Results.EDCDataItemWithTransitDataInqResult) dataCollectionInqController.edcDataItemWithTransitDataInq(edcDataItemWithTransitDataInqParams).getBody();

        // 【step11-2】. spec check
        Params.SpecCheckReqParams specCheckReqParams=new Params.SpecCheckReqParams();
        specCheckReqParams.setControlJobID(edcDataItemWithTransitDataInqResult.getControlJobID());
        specCheckReqParams.setEquipmentID(edcDataItemWithTransitDataInqResult.getEquipmentID());
        specCheckReqParams.setUser(lotSplitTestCase.getUser());
        List<Infos.StartCassette> startCassetteList = edcDataItemWithTransitDataInqResult.getStartCassetteList();
        Infos.LotInCassette lotInCassette = startCassetteList.get(0).getLotInCassetteList().get(0);
        List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
        String[] dataValues={"10.0","10.0","10.0"};
        int i=0;
        for (Infos.DataCollectionItemInfo dataCollectionItemInfo : lotInCassette.getStartRecipe().getDcDefList().get(0).getDcItems()) {
            if (!"Mean".equalsIgnoreCase(dataCollectionItemInfo.getCalculationType())){
                dataCollectionItemInfo.setDataValue(dataValues[i++]);
                dataCollectionItemInfo.setWaferID(lotWaferList.get(i).getWaferID());
            }
        }
        specCheckReqParams.setStartCassetteList(startCassetteList);
        Results.SpecCheckReqResult specCheckReqResult= (Results.SpecCheckReqResult) dataCollectionController.specCheckReq(specCheckReqParams).getBody();

        //【step12-1】move out monitor lot
        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInResult.getControlJobID());
        opeComWithDataReqParams.setUser(lotSplitTestCase.getUser());
        opeComWithDataReqParams.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));
        equipmentTestCase.moveOut(opeComWithDataReqParams);

        //【step13】return the lot info check grouping info
        List<Infos.MonitorGroups> monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.assertCheck((CimArrayUtils.getSize(monitorProdLotsRelation1) == 0),"monitor lot grouping must null");

        List<Infos.MonitorGroups> monitorProdLotsRelation2 = processMonitorTestCase.getMonitorProdLotsRelationCase(productLotID,productCassetteID);
        Validations.assertCheck((CimArrayUtils.getSize(monitorProdLotsRelation2) == 0),"monitor lot grouping must null");

        //【step14】entityInhibit check
        Results.EqpInfoInqResult infoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP)).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(infoInqResult.getConstraintList()) == 0,"the eqp must not be inhibit");
    }

    public void ManuelGrouping_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OverLimit() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier monitorCassetteID = monitorInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step5】lot info search for product lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step6】monitor lot skip to 1000.0300
        String operationNum1 = "1000.0300";
        Infos.LotInfo lotInfo = monitorInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(monitorLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,operationNum1);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(monitorLotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step7】monitor grouping with product lot on 1000.0300
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,productLotID);

        //【step8】step to eqp 1THK01
        electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));

        //【step9】load the monitor lot
        List<String> purposeList = new ArrayList<>();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSLOT);
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP),monitorCassetteID,new ObjectIdentifier(LOAD_PORT_ONE) , purposeList.get(0));

        //【step10】move in the monitor lot
        // 【step10-1】get lotsInfoForOpeStart info
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(monitorCassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP), cassetteIDs).getBody();
        // 【step10-2】move in
        Results.MoveInReqResult moveInResult = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList()).getBody();

        //【step11】spec check over limit
        //【step11-1】data collection
        Params.EDCDataItemWithTransitDataInqParams edcDataItemWithTransitDataInqParams=new Params.EDCDataItemWithTransitDataInqParams();
        edcDataItemWithTransitDataInqParams.setControlJobID(moveInResult.getControlJobID());
        edcDataItemWithTransitDataInqParams.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));
        edcDataItemWithTransitDataInqParams.setUser(lotSplitTestCase.getUser());
        Results.EDCDataItemWithTransitDataInqResult edcDataItemWithTransitDataInqResult= (Results.EDCDataItemWithTransitDataInqResult) dataCollectionInqController.edcDataItemWithTransitDataInq(edcDataItemWithTransitDataInqParams).getBody();

        // 【step11-2】. spec check
        Params.SpecCheckReqParams specCheckReqParams=new Params.SpecCheckReqParams();
        specCheckReqParams.setControlJobID(edcDataItemWithTransitDataInqResult.getControlJobID());
        specCheckReqParams.setEquipmentID(edcDataItemWithTransitDataInqResult.getEquipmentID());
        specCheckReqParams.setUser(lotSplitTestCase.getUser());
        List<Infos.StartCassette> startCassetteList = edcDataItemWithTransitDataInqResult.getStartCassetteList();
        Infos.LotInCassette lotInCassette = startCassetteList.get(0).getLotInCassetteList().get(0);
        List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
        String[] dataValues={"1000.01","1000.01","1000.01"};
        int i=0;
        for (Infos.DataCollectionItemInfo dataCollectionItemInfo : lotInCassette.getStartRecipe().getDcDefList().get(0).getDcItems()) {
            if (!"Mean".equalsIgnoreCase(dataCollectionItemInfo.getCalculationType())){
                dataCollectionItemInfo.setDataValue(dataValues[i++]);
                dataCollectionItemInfo.setWaferID(lotWaferList.get(i).getWaferID());
            }
        }
        specCheckReqParams.setStartCassetteList(startCassetteList);
        Results.SpecCheckReqResult specCheckReqResult= (Results.SpecCheckReqResult) dataCollectionController.specCheckReq(specCheckReqParams).getBody();

        //【step12-1】move out monitor lot
        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInResult.getControlJobID());
        opeComWithDataReqParams.setUser(lotSplitTestCase.getUser());
        opeComWithDataReqParams.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));
        equipmentTestCase.moveOut(opeComWithDataReqParams);

        //【step13】return the lot info check grouping info
        List<Infos.MonitorGroups> monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.assertCheck((CimArrayUtils.getSize(monitorProdLotsRelation1) == 0),"monitor lot grouping must null");

        List<Infos.MonitorGroups> monitorProdLotsRelation2 = processMonitorTestCase.getMonitorProdLotsRelationCase(productLotID,productCassetteID);
        Validations.assertCheck((CimArrayUtils.getSize(monitorProdLotsRelation2) == 0),"monitor lot grouping must null");

        //【step14】entityInhibit check
        Results.EqpInfoInqResult infoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP)).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(infoInqResult.getConstraintList()) == 0,"the eqp must not be inhibit");

        //【step15】check monitorLot and productLot lotHoldState
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus,BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the product lot must be onHold");

        lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult productInfoCaseLast2 = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String lotStatus2 = productInfoCaseLast2.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus2,BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the product lot must be onHold");
    }

    public void AutoGrouping_By_EqpMoveOut_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OK() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier monitorCassetteID = monitorInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step5】lot info search for product lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step6】monitor lot skip to 1000.0200
        String operationNum1 = "1000.0200";
        Infos.LotInfo lotInfo = monitorInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(monitorLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,operationNum1);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(monitorLotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step7】product lot skip to 2000.0200
        String operationNum2 = "2000.0200";
        Infos.LotInfo lotInfo2 = productInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(productLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes2 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList2,operationNum2);

        Params.SkipReqParams skipReqParams2 = new Params.SkipReqParams();
        skipReqParams2.setCurrentOperationNumber(lotInfo2.getLotOperationInfo().getOperationNumber());
        skipReqParams2.setCurrentRouteID(lotInfo2.getLotOperationInfo().getRouteID());
        skipReqParams2.setLocateDirection(true);
        skipReqParams2.setLotID(productLotID);
        skipReqParams2.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams2.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams2.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams2.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams2.setSeqno(-1);
        skipReqParams2.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams2);

        //【step8】 step to eqp 1SRT01
        //【step9】einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP));

        //【step10】load purpose productLot
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP),productCassetteID,new ObjectIdentifier(LOAD_PORT_ONE) , purposeList.get(0));

        //【step11】load purpose monitorLot
        purposeList = new ArrayList<>();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSMONITORLOT);
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP),monitorCassetteID,new ObjectIdentifier(LOAD_PORT_TWO) , purposeList.get(0));

        //【step12】move in the product lot and monitor lot
        // 【step12-1】get lotsInfoForOpeStart info
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(productCassetteID);
        cassetteIDs.add(monitorCassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP), cassetteIDs).getBody();
        // 【step12-2】move in
        Results.MoveInReqResult moveInResult = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList()).getBody();

        //【step13-1】move out the two lot and unload
        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInResult.getControlJobID());
        opeComWithDataReqParams.setUser(lotSplitTestCase.getUser());
        opeComWithDataReqParams.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP));
        equipmentTestCase.moveOut(opeComWithDataReqParams);

        //【step13-2】unload monitor lot and product lot
        purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.uncarrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP), productCassetteID,new ObjectIdentifier(LOAD_PORT_ONE), purposeList.get(0));

        purposeList = new ArrayList<>();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSMONITORLOT);
        equipmentTestCase.uncarrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP), monitorCassetteID,new ObjectIdentifier(LOAD_PORT_TWO), purposeList.get(0));

        //【step14】return the lot info check grouping info
        List<Infos.MonitorGroups> monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation1) > 0),"monitor lot grouping failed");

        List<Infos.MonitorGroups> monitorProdLotsRelation2 = processMonitorTestCase.getMonitorProdLotsRelationCase(productLotID,productCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation2) > 0),"monitor lot grouping failed");

        Boolean compareFlag1 = false;
        Boolean compareFlag2 = false;
        if (CimStringUtils.equals(monitorProdLotsRelation1.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag1 = true;
        }
        if (CimStringUtils.equals(monitorProdLotsRelation2.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag2 = true;
        }
        Validations.assertCheck(compareFlag1 && compareFlag2,"monitor lot and product lot does not have grouping relation");

        //【step15】check process product lot is onHold
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus,BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the product lot must be onHold");

        //【step16】step to special eqp 1THK01 for monitor lot
        electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));

        //【step17】load monitor lot for process lot
        purposeList = new ArrayList<>();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSLOT);
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP),monitorCassetteID,new ObjectIdentifier(LOAD_PORT_ONE) , purposeList.get(0));

        //【step18】move in the monitor lot
        // 【step18-1】get lotsInfoForOpeStart info
        cassetteIDs = new ArrayList<>();
        cassetteIDs.add(monitorCassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult1 = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP), cassetteIDs).getBody();
        // 【step18-2】move in
        Results.MoveInReqResult moveInResult1 = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult1.getControlJobID(), lotsMoveInInfoInqResult1.getEquipmentID(), lotsMoveInInfoInqResult1.getPortGroupID(), false, lotsMoveInInfoInqResult1.getStartCassetteList()).getBody();

        //【step19】spec check in limit
        //【step19-1】data collection
        Params.EDCDataItemWithTransitDataInqParams edcDataItemWithTransitDataInqParams=new Params.EDCDataItemWithTransitDataInqParams();
        edcDataItemWithTransitDataInqParams.setControlJobID(moveInResult1.getControlJobID());
        edcDataItemWithTransitDataInqParams.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));
        edcDataItemWithTransitDataInqParams.setUser(lotSplitTestCase.getUser());
        Results.EDCDataItemWithTransitDataInqResult edcDataItemWithTransitDataInqResult= (Results.EDCDataItemWithTransitDataInqResult) dataCollectionInqController.edcDataItemWithTransitDataInq(edcDataItemWithTransitDataInqParams).getBody();

        // 【step19-2】. spec check
        Params.SpecCheckReqParams specCheckReqParams=new Params.SpecCheckReqParams();
        specCheckReqParams.setControlJobID(edcDataItemWithTransitDataInqResult.getControlJobID());
        specCheckReqParams.setEquipmentID(edcDataItemWithTransitDataInqResult.getEquipmentID());
        specCheckReqParams.setUser(lotSplitTestCase.getUser());
        List<Infos.StartCassette> startCassetteList = edcDataItemWithTransitDataInqResult.getStartCassetteList();
        Infos.LotInCassette lotInCassette = startCassetteList.get(0).getLotInCassetteList().get(0);
        List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
        String[] dataValues={"10.0","10.0","10.0"};
        int i=0;
        for (Infos.DataCollectionItemInfo dataCollectionItemInfo : lotInCassette.getStartRecipe().getDcDefList().get(0).getDcItems()) {
            if (!"Mean".equalsIgnoreCase(dataCollectionItemInfo.getCalculationType())){
                dataCollectionItemInfo.setDataValue(dataValues[i++]);
                dataCollectionItemInfo.setWaferID(lotWaferList.get(i).getWaferID());
            }
        }
        specCheckReqParams.setStartCassetteList(startCassetteList);
        Results.SpecCheckReqResult specCheckReqResult= (Results.SpecCheckReqResult) dataCollectionController.specCheckReq(specCheckReqParams).getBody();

        //【step20】move out monitor lot
        Params.OpeComWithDataReqParams opeComWithDataReqParams1 = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams1.setControlJobID(moveInResult1.getControlJobID());
        opeComWithDataReqParams1.setUser(lotSplitTestCase.getUser());
        opeComWithDataReqParams1.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));
        equipmentTestCase.moveOut(opeComWithDataReqParams1);

        //【step21】return the lot info check grouping info
        monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.assertCheck((CimArrayUtils.getSize(monitorProdLotsRelation1) == 0),"monitor lot grouping must null");

        monitorProdLotsRelation2 = processMonitorTestCase.getMonitorProdLotsRelationCase(productLotID,productCassetteID);
        Validations.assertCheck((CimArrayUtils.getSize(monitorProdLotsRelation2) == 0),"monitor lot grouping must null");

        //【step22】entityInhibit check
        Results.EqpInfoInqResult infoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP)).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(infoInqResult.getConstraintList()) == 0,"the first eqp must have inhibit");

        Results.EqpInfoInqResult infoInqResult1 = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP)).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(infoInqResult1.getConstraintList()) == 0,"the second eqp must have inhibit");

        //【step23】check product lot lotHoldState
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(!CimStringUtils.equals(lotStatus,BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the product lot holdLot must be release");

    }

    public void AutoGrouping_By_EqpMoveOut_MonitorLot_And_ProductLot_Then_Load_MonitorLot_To_Eqp_OnSpecCheck_OverLimit() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb product lot of 25 wafer
        ObjectIdentifier productLotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier monitorCassetteID = monitorInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step5】lot info search for product lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step6】monitor lot skip to 1000.0200
        String operationNum1 = "1000.0200";
        Infos.LotInfo lotInfo = monitorInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(monitorLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,operationNum1);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(monitorLotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step7】product lot skip to 2000.0200
        String operationNum2 = "2000.0200";
        Infos.LotInfo lotInfo2 = productInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(productLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes2 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList2,operationNum2);

        Params.SkipReqParams skipReqParams2 = new Params.SkipReqParams();
        skipReqParams2.setCurrentOperationNumber(lotInfo2.getLotOperationInfo().getOperationNumber());
        skipReqParams2.setCurrentRouteID(lotInfo2.getLotOperationInfo().getRouteID());
        skipReqParams2.setLocateDirection(true);
        skipReqParams2.setLotID(productLotID);
        skipReqParams2.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams2.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams2.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams2.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams2.setSeqno(-1);
        skipReqParams2.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams2);

        //【step8】 step to eqp 1SRT01
        //【step9】einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP));

        //【step10】load purpose productLot
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP),productCassetteID,new ObjectIdentifier(LOAD_PORT_ONE) , purposeList.get(0));

        //【step11】load purpose monitorLot
        purposeList = new ArrayList<>();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSMONITORLOT);
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP),monitorCassetteID,new ObjectIdentifier(LOAD_PORT_TWO) , purposeList.get(0));

        //【step12】move in the product lot and monitor lot
        // 【step12-1】get lotsInfoForOpeStart info
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(productCassetteID);
        cassetteIDs.add(monitorCassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP), cassetteIDs).getBody();
        // 【step12-2】move in
        Results.MoveInReqResult moveInResult = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList()).getBody();

        //【step13-1】move out the two lot
        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInResult.getControlJobID());
        opeComWithDataReqParams.setUser(lotSplitTestCase.getUser());
        opeComWithDataReqParams.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP));
        equipmentTestCase.moveOut(opeComWithDataReqParams);

        //【step13-2】unload monitor lot and product lot
        purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.uncarrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP), productCassetteID,new ObjectIdentifier(LOAD_PORT_ONE), purposeList.get(0));

        purposeList = new ArrayList<>();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSMONITORLOT);
        equipmentTestCase.uncarrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP), monitorCassetteID,new ObjectIdentifier(LOAD_PORT_TWO), purposeList.get(0));


        //【step14】return the lot info check grouping info
        List<Infos.MonitorGroups> monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation1) > 0),"monitor lot grouping failed");

        List<Infos.MonitorGroups> monitorProdLotsRelation2 = processMonitorTestCase.getMonitorProdLotsRelationCase(productLotID,productCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation2) > 0),"monitor lot grouping failed");

        Boolean compareFlag1 = false;
        Boolean compareFlag2 = false;
        if (CimStringUtils.equals(monitorProdLotsRelation1.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag1 = true;
        }
        if (CimStringUtils.equals(monitorProdLotsRelation2.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag2 = true;
        }
        Validations.assertCheck(compareFlag1 && compareFlag2,"monitor lot and product lot does not have grouping relation");

        //【step15】check process product lot is onHold
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus,BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the product lot must be onHold");

        //【step16】step to special eqp 1THK01 for monitor lot
        electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));

        //【step17】load monitor lot for process lot
        purposeList = new ArrayList<>();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSLOT);
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP),monitorCassetteID,new ObjectIdentifier(LOAD_PORT_ONE) , purposeList.get(0));

        //【step18】move in the monitor lot
        // 【step18-1】get lotsInfoForOpeStart info
        cassetteIDs = new ArrayList<>();
        cassetteIDs.add(monitorCassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult1 = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP), cassetteIDs).getBody();
        // 【step18-2】move in
        Results.MoveInReqResult moveInResult1 = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult1.getControlJobID(), lotsMoveInInfoInqResult1.getEquipmentID(), lotsMoveInInfoInqResult1.getPortGroupID(), false, lotsMoveInInfoInqResult1.getStartCassetteList()).getBody();

        //【step19】spec check in limit
        //【step19-1】data collection
        Params.EDCDataItemWithTransitDataInqParams edcDataItemWithTransitDataInqParams=new Params.EDCDataItemWithTransitDataInqParams();
        edcDataItemWithTransitDataInqParams.setControlJobID(moveInResult1.getControlJobID());
        edcDataItemWithTransitDataInqParams.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));
        edcDataItemWithTransitDataInqParams.setUser(lotSplitTestCase.getUser());
        Results.EDCDataItemWithTransitDataInqResult edcDataItemWithTransitDataInqResult= (Results.EDCDataItemWithTransitDataInqResult) dataCollectionInqController.edcDataItemWithTransitDataInq(edcDataItemWithTransitDataInqParams).getBody();

        // 【step19-2】. spec check over the limit
        Params.SpecCheckReqParams specCheckReqParams=new Params.SpecCheckReqParams();
        specCheckReqParams.setControlJobID(edcDataItemWithTransitDataInqResult.getControlJobID());
        specCheckReqParams.setEquipmentID(edcDataItemWithTransitDataInqResult.getEquipmentID());
        specCheckReqParams.setUser(lotSplitTestCase.getUser());
        List<Infos.StartCassette> startCassetteList = edcDataItemWithTransitDataInqResult.getStartCassetteList();
        Infos.LotInCassette lotInCassette = startCassetteList.get(0).getLotInCassetteList().get(0);
        List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
        String[] dataValues={"1000.01","1000.01","1000.01"};
        int i=0;
        for (Infos.DataCollectionItemInfo dataCollectionItemInfo : lotInCassette.getStartRecipe().getDcDefList().get(0).getDcItems()) {
            if (!"Mean".equalsIgnoreCase(dataCollectionItemInfo.getCalculationType())){
                dataCollectionItemInfo.setDataValue(dataValues[i++]);
                dataCollectionItemInfo.setWaferID(lotWaferList.get(i).getWaferID());
            }
        }
        specCheckReqParams.setStartCassetteList(startCassetteList);
        Results.SpecCheckReqResult specCheckReqResult= (Results.SpecCheckReqResult) dataCollectionController.specCheckReq(specCheckReqParams).getBody();

        //【step20】move out monitor lot
        Params.OpeComWithDataReqParams opeComWithDataReqParams1 = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams1.setControlJobID(moveInResult1.getControlJobID());
        opeComWithDataReqParams1.setUser(lotSplitTestCase.getUser());
        opeComWithDataReqParams1.setEquipmentID(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP));
        equipmentTestCase.moveOut(opeComWithDataReqParams1);

        //【step21】return the lot info check grouping info
        monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.assertCheck((CimArrayUtils.getSize(monitorProdLotsRelation1) == 0),"monitor lot grouping must null");

        monitorProdLotsRelation2 = processMonitorTestCase.getMonitorProdLotsRelationCase(productLotID,productCassetteID);
        Validations.assertCheck((CimArrayUtils.getSize(monitorProdLotsRelation2) == 0),"monitor lot grouping must null");

        //【step22】entityInhibit check
        Results.EqpInfoInqResult infoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_NORMALEQP)).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(infoInqResult.getConstraintList()) > 0,"the first eqp must have inhibit");

        Results.EqpInfoInqResult infoInqResult1 = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PROCESS_MONITOR_SPECILEQP)).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(infoInqResult1.getConstraintList()) == 0,"the second eqp must not have inhibit");

        //【step23】check product lot lotHoldState
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus,BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the product lot must be onHold");

        //【step24】check monitor lot lotHoldState
        lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus,BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the monitor lot must be onHold");
    }

    public void MonitorLot_And_ProcessLot_AutoGrouping_After_MoveIn_With_MoveIn_Reserve() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb a product lot with 25 wafer and skip to 2000.0200
        String productBankID = "BNK-0S";
        String productID = "PRODUCT0.01";
        String operationNum = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(productBankID, sourceProductID, productID);
        ObjectIdentifier productLotID = testUtils.stbAndSkip(stbInfo,operationNum);

        //【step4】skip monitor lot to 1000.0200
        String operationNum1 = "1000.0200";
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Infos.LotInfo lotInfo = monitorInfoCase.getLotInfoList().get(0);
        ObjectIdentifier monitorCassetteID = monitorInfoCase.getLotListInCassetteInfo().getCassetteID();
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(monitorLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,operationNum1);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(monitorLotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step5】start reserve the two lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step5 -1】dispatch/what_next_lot_list/inq
        ObjectIdentifier equipmentID = new ObjectIdentifier(PROCESS_MONITOR_BUFFEREQP);
        Results.WhatNextLotListResult whatNextLotListResult = (Results.WhatNextLotListResult) equipmentTestCase.whatNextInqCase(equipmentID).getBody();
        List<Infos.WhatNextAttributes> whatNextAttributesContent = (List<Infos.WhatNextAttributes> ) whatNextLotListResult.getWhatNextAttributesPage().getContent();
        Infos.WhatNextAttributes whatNextAttributesMonitor = whatNextAttributesContent.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getLotID(), monitorLotID)).findFirst().orElse(null);
        Infos.WhatNextAttributes whatNextAttributesProduct = whatNextAttributesContent.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getLotID(), productLotID)).findFirst().orElse(null);
        if (whatNextAttributesMonitor == null || whatNextAttributesProduct == null) {
            return;
        }


        //【step5-2】 einfo/lot_list_in_cassette/inq
        Results.LotListByCarrierInqResult lotListByCarrierInqResultMonitor = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(whatNextAttributesMonitor.getCassetteID()).getBody();
        Results.LotListByCarrierInqResult lotListByCarrierInqResultProduct = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(whatNextAttributesProduct.getCassetteID()).getBody();


        //【step5-3】 einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResultMonitor = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotListByCarrierInqResultMonitor.getLotListInCassetteInfo().getLotIDList()).getBody();
        Results.LotInfoInqResult lotInfoInqResultProduct = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotListByCarrierInqResultProduct.getLotListInCassetteInfo().getLotIDList()).getBody();

        List<Results.LotInfoInqResult> lotInfoInqResultList = new ArrayList<>();
        lotInfoInqResultList.add(lotInfoInqResultMonitor);
        lotInfoInqResultList.add(lotInfoInqResultProduct);

        //【step5-4】dispatch/lots_info_for_start_reservation/inq
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult = (Results.LotsMoveInReserveInfoInqResult) equipmentTestCase.lotsMoveInReserveInfoForIBInqCase(lotInfoInqResultList,equipmentID).getBody();

        //【step5-5】start reserve for two lot and on empty carrier
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_PROCESSMONITORLOT);
        purposeList.add(BizConstant.SP_BUFFERCATEGORY_EMPTYCASSETTE);
        equipmentTestCase.changeOperationModeToOffLine1(equipmentID, BizConstant.SP_MC_CATEGORY_INTERNALBUFFER);
        ObjectIdentifier emptyCarrier = bankTestCase.getEmptyCassette();
        equipmentTestCase.moveInReserveReqForMonitorLotAndProductLotForInternalBufferCase(lotsMoveInReserveInfoInqResult,monitorCassetteID,productCassetteID, purposeList,new ObjectIdentifier(LOAD_PORT_ONE),new ObjectIdentifier(LOAD_PORT_TWO), emptyCarrier);


        //【step6】load the two lot and load emptyCarrier
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(monitorCassetteID);
        loadForInternalBufferInfo.setEquipmentID(equipmentID);
        loadForInternalBufferInfo.setLoadPurposeType(BizConstant.SP_BUFFERCATEGORY_PROCESSMONITORLOT);
        loadForInternalBufferInfo.setLotID(monitorLotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier(LOAD_PORT_ONE));
        loadForInternalBufferInfo.setNeedStartReserved(false);
        loadForInternalBufferCase.load(loadForInternalBufferInfo);

        //【step7】move to self
        TestInfos.MoveToSelfInfo moveToSelfInfo = new TestInfos.MoveToSelfInfo(monitorCassetteID, new ObjectIdentifier(LOAD_PORT_ONE), equipmentID);
        moveToSelfCase.movetoSelf(moveToSelfInfo);

        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo1 = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo1.setCassetteID(productCassetteID);
        loadForInternalBufferInfo1.setEquipmentID(equipmentID);
        loadForInternalBufferInfo1.setLoadPurposeType(BizConstant.SP_BUFFERCATEGORY_PROCESSLOT);
        loadForInternalBufferInfo1.setLotID(productLotID);
        loadForInternalBufferInfo1.setPortID(new ObjectIdentifier(LOAD_PORT_TWO));
        loadForInternalBufferInfo1.setNeedStartReserved(false);
        loadForInternalBufferCase.load(loadForInternalBufferInfo1);

        TestInfos.MoveToSelfInfo moveToSelfInfo1 = new TestInfos.MoveToSelfInfo(productCassetteID, new ObjectIdentifier(LOAD_PORT_TWO), equipmentID);
        moveToSelfCase.movetoSelf(moveToSelfInfo1);

        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo2 = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo2.setCassetteID(emptyCarrier);
        loadForInternalBufferInfo2.setEquipmentID(equipmentID);
        loadForInternalBufferInfo2.setLoadPurposeType(BizConstant.SP_BUFFERCATEGORY_EMPTYCASSETTE);
        loadForInternalBufferInfo2.setPortID(new ObjectIdentifier(LOAD_PORT_ONE));
        loadForInternalBufferInfo2.setNeedStartReserved(false);
        loadForInternalBufferCase.load(loadForInternalBufferInfo2);

        //【step7】move to self
        TestInfos.MoveToSelfInfo moveToSelfInfo2 = new TestInfos.MoveToSelfInfo(emptyCarrier, new ObjectIdentifier(LOAD_PORT_ONE), equipmentID);
        moveToSelfCase.movetoSelf(moveToSelfInfo2);

        //【step8】move in
        TestInfos.MoveInForInternalBufferInfo moveInForInternalBufferInfo = new TestInfos.MoveInForInternalBufferInfo();
        moveInForInternalBufferInfo.setEquipmentID(equipmentID);
        moveInForInternalBufferInfo.setProcessJobPauseFlag(false); // when not do start reserve, it's false
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        cassetteIDList.add(monitorCassetteID);
        cassetteIDList.add(productCassetteID);
        cassetteIDList.add(emptyCarrier);
        moveInForInternalBufferInfo.setCassetteIDList(cassetteIDList);
        moveInForInternalBufferCase.moveIn(moveInForInternalBufferInfo);

        //【step9】check monitor grouping
        List<Infos.MonitorGroups> monitorProdLotsRelation1 = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation1) > 0),"monitor lot grouping failed");

        List<Infos.MonitorGroups> monitorProdLotsRelation2 = processMonitorTestCase.getMonitorProdLotsRelationCase(productLotID,productCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelation2) > 0),"monitor lot grouping failed");

        Boolean compareFlag1 = false;
        Boolean compareFlag2 = false;
        if (CimStringUtils.equals(monitorProdLotsRelation1.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag1 = true;
        }
        if (CimStringUtils.equals(monitorProdLotsRelation2.get(0).getStrMonitoredLots().get(0).getCassetteID(),productCassetteID)){
            compareFlag2 = true;
        }
        Validations.assertCheck(compareFlag1 && compareFlag2,"monitor lot and product lot does not have grouping relation");
    }

    public void Grouped_Monitor_Lot_To_Split() {
        //【step0】init parameter
        String bankID = "BK-CTRL";
        String sourceProductID = "RAW-2000.01";
        String productCategory = "Process Monitor";
        Integer waferCount = 10;

        //【step1】make receive
        String vendorLotID =GenerateVendorlot.getVendorLot();
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】NPW stb on first monitor lot of one wafer
        Response response1 = npwLotCase.NPWLot_ProcessMonitorLotSTBBeforeProcess(vendorLotID,bankID, sourceProductID, productCategory, waferCount);
        ObjectIdentifier monitorLotID = (ObjectIdentifier) response1.getBody();

        //【step3】stb a product lot
        String productBankID = "BNK-0S";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(productBankID, sourceProductID, productID);
        ObjectIdentifier productLotID = stbCase.STB_Normal(stbInfo);

        //【step4】lot info search for first monitor lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(monitorLotID);
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier monitorCassetteID = monitorInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step5】lot info search for product lot
        lotIDs = new ArrayList<>();
        lotIDs.add(productLotID);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step6】monitor grouping a monitor lot and product lot
        processMonitorTestCase.monitorBatchCreateReqCase(monitorLotID,productLotID);

        //【step7】split the monitor lot at current operation
        List<Infos.LotWaferAttributes> lotWaferAttributesList = monitorInfoCase.getLotInfoList().get(0).getLotWaferAttributesList();
        List<Infos.LotWaferAttributes> subLotWaferAttributesList = new ArrayList<>();
        int size = new Random().nextInt(lotWaferAttributesList.size()) + 1;
        for (int i = 0; i < size; i++) {
            subLotWaferAttributesList.add(lotWaferAttributesList.get(i));
        }
        List<ObjectIdentifier> childWaferIDs = subLotWaferAttributesList.stream().map(Infos.LotWaferAttributes::getWaferID).collect(Collectors.toList());
        ObjectIdentifier childMonitorLotID = (ObjectIdentifier) lotSplitTestCase.splitLotReq(childWaferIDs, false, false,
                "", new ObjectIdentifier(), monitorLotID, new ObjectIdentifier()).getBody();

        //【step8】check monitor relation for parent monitor lot
        List<Infos.MonitorGroups> monitorProdLotsRelationParent = processMonitorTestCase.getMonitorProdLotsRelationCase(monitorLotID,monitorCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelationParent) > 0),"parent monitor lot monitor group info must not be null");

        //【step9】check monitor relation for child monitor lot
        Results.LotInfoInqResult childMonitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(childMonitorLotID)).getBody();
        ObjectIdentifier childCassetteID = childMonitorInfoCase.getLotListInCassetteInfo().getCassetteID();

        List<Infos.MonitorGroups> monitorProdLotsRelationChild = processMonitorTestCase.getMonitorProdLotsRelationCase(childMonitorLotID,childCassetteID);
        Validations.check(!(CimArrayUtils.getSize(monitorProdLotsRelationChild) == 0),"child monitor lot monitor group info must be null");
    }

    public void Monitor_Grouping_STB_After_Processing_In_Normal_EQP_And_Then_Use_Monitor_Lot_Processing_Measurement_Step() {
        //【step1】stb a product lot and skip to 2000.0200
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOperationNum = "2000.0200";
        ObjectIdentifier productLotID = testUtils.stbAndSkip(stbInfo, skipOperationNum);
        Results.LotInfoInqResult productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(productLotID)).getBody();
        ObjectIdentifier productCassetteID = productInfoCase.getLotListInCassetteInfo().getCassetteID();

        //【step2】vendor prepare a monitor lot product id RAW-2000.01
        String bkControlBankID = "BK-CTRL";
        //【step2-1】make receive
        String vendorLotID = "VL011002.00";
        //【step2-2】get one empty cassette
        ObjectIdentifier monitorCassetteID = bankTestCase.getEmptyCassette();
        //【step2-3】make prepared a monitor lot ID
        Results.MaterialPrepareReqResult response = (Results.MaterialPrepareReqResult)vendorLotPrepareCase.VendorLotPrepare(bkControlBankID, vendorLotID, sourceProductID, monitorCassetteID, 2,false,"Process Monitor","Process Monitor").getBody();
        ObjectIdentifier monitorLotID = response.getLotID();

        ObjectIdentifier equipmentID = new ObjectIdentifier(PROCESS_MONITOR_STB_AFTER_EQP);
        //【step3】step to normal eqp 1FHI02_NORM and start reserve product cassette and empty a cassette
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult= (Results.LotsMoveInReserveInfoInqResult) equipmentTestCase.lotsMoveInReserveInfoInqCase(productInfoCase,equipmentID).getBody();

        Params.CarrierListInqParams carrierListInqParams= JSONObject.parseObject("{\"maxRetrieveCount\":10,\"cassetteID\":{},\"bankID\":{},\"emptyFlag\":true,\"searchCondition\":{\"size\":10,\"page\":1,\"conditions\":[]},\"user\":{\"userID\":{\"value\":\"ADMIN\",\"referenceKey\":\"\"},\"password\":\"b51fa595e692d53739b69131cdc73440\",\"functionID\":\"OBNKW001\"}}", Params.CarrierListInqParams.class);
        Results.CarrierListInq170Result carrierListInq170Result= (Results.CarrierListInq170Result) durableInqController.carrierListInq(carrierListInqParams).getBody();

        Infos.StartCassette startCassette=new Infos.StartCassette();
        ObjectIdentifier emptyCassette = carrierListInq170Result.getFoundCassette().getContent().get(0).getCassetteID();
        startCassette.setCassetteID(emptyCassette);
        startCassette.setLoadPurposeType("Empty Cassette");
        startCassette.setLotInCassetteList(new ArrayList<>());

        Results.EqpInfoInqResult eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();

        List<Infos.StartCassette> startCassetteList=lotsMoveInReserveInfoInqResult.getStrStartCassette();

        List<ObjectIdentifier> portIDs=eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().stream().map(
                eqpPortStatus -> eqpPortStatus.getPortID()).collect(Collectors.toList());

        startCassetteList.get(0).setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);

        startCassetteList.add(startCassette);

        for (int i = 0; i < startCassetteList.size(); i++) {
            startCassette=startCassetteList.get(i);
            startCassette.setLoadSequenceNumber(i+1L);
            startCassette.setLoadPortID(portIDs.get(i));
            startCassette.setUnloadPortID(portIDs.get(i));
        }

        Params.MoveInReserveReqParams moveInReserveReqParams=new Params.MoveInReserveReqParams();
        moveInReserveReqParams.setEquipmentID(equipmentID);
        moveInReserveReqParams.setPortGroupID("PG1");
        moveInReserveReqParams.setUser(testUtils.getUser());
        moveInReserveReqParams.setStartCassetteList(startCassetteList);
        dispatchController.moveInReserveReq(moveInReserveReqParams);

        //【step4】eqp info
        eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();
        Results.LotListByCarrierInqResult listInCassetteInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(monitorCassetteID).getBody();
        //【step5】npw reserve the monitor lot to equipment
        Results.LotInfoInqResult monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(monitorLotID)).getBody();
        List<Infos.StartCassette> monitorStartCassette =  this.getNpwReserveStartCassette(eqpInfoInqResult,listInCassetteInqResult,monitorInfoCase);

        Params.NPWCarrierReserveReqParams npwReserveParam = new Params.NPWCarrierReserveReqParams();
        npwReserveParam.setControlJobID(ObjectIdentifier.build("",""));
        npwReserveParam.setEquipmentID(equipmentID);
        npwReserveParam.setPortGroupID("PG2");
        npwReserveParam.setStartCassetteList(monitorStartCassette);
        processMonitorTestCase.npwCarrierReserveReq(npwReserveParam);

        //【step6】load the productCassette P1, emptyCassete P2 and monitorCassette P3
        equipmentTestCase.carrierLoadingRpt(equipmentID, productCassetteID, new ObjectIdentifier("P1"), BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);
        equipmentTestCase.carrierLoadingRpt(equipmentID, emptyCassette, new ObjectIdentifier("P2"), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE);
        equipmentTestCase.carrierLoadingRpt(equipmentID, monitorCassetteID, new ObjectIdentifier("P3"), BizConstant.SP_LOADPURPOSETYPE_WAITINGMONITORLOT);

        //【step7】move in the product and empty cassette
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(equipmentID, Arrays.asList(productCassetteID,emptyCassette)).getBody();
        //【step7-1】move in
        Results.MoveInReqResult moveInResult = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList()).getBody();

        //【step8】move out
        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInResult.getControlJobID());
        opeComWithDataReqParams.setUser(testUtils.getUser());
        opeComWithDataReqParams.setEquipmentID(equipmentID);
        try {
            equipmentTestCase.moveOut(opeComWithDataReqParams);
            Assert.isTrue(false,"test error");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfigEx.getMonitorCreatReqd(),e.getCode()),"test fail");
        }

        //【step9】can't  move so stb after
        productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(productLotID)).getBody();
        Results.SubLotTypeListInqResult subLotTypeListInqResult = (Results.SubLotTypeListInqResult) processMonitorTestCase.subLotTypeIdListInq(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT).getBody();

        Results.SourceLotListInqResult sourceLotListInqResult = (Results.SourceLotListInqResult) processMonitorTestCase.sourceLotListInq(productInfoCase.getLotInfoList().get(0).getLotRecipeInfo().getProcessMonitorProductID()).getBody();
        Validations.assertCheck(CimArrayUtils.isNotEmpty(sourceLotListInqResult.getSourceLotList().getContent()),"the monitorLot product id is not RAW-2000.01");
        Infos.SourceLot sourceLotSelect = sourceLotListInqResult.getSourceLotList().getContent().stream().filter(sourceLot -> CimObjectUtils.equalsWithValue(sourceLot.getLotID(), monitorLotID)).findFirst().get();

        //【step10】select the reserve empty cassette to stb after process
        Params.AutoCreateMonitorForInProcessLotReqParams params = new Params.AutoCreateMonitorForInProcessLotReqParams();
        params.setProcessEquipmentID(equipmentID);
        params.setProductLotIDs(Arrays.asList(productLotID));
        params.setStbLotSubLotType(subLotTypeListInqResult.getStrLotTypes().get(0).getStrSubLotTypes().get(0).getSubLotType());
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();

        newLotAttributes.setCassetteID(emptyCassette);
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);

        monitorInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(monitorLotID)).getBody();
        List<Infos.LotWaferAttributes> lotWaferAttributesList = monitorInfoCase.getLotInfoList().get(0).getLotWaferAttributesList();
        for (Infos.LotWaferAttributes lotWaferAttributes : lotWaferAttributesList) {
            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
            newWaferAttributes.setNewWaferID(ObjectIdentifier.build("",""));
            newWaferAttributes.setNewLotID(ObjectIdentifier.build("",""));
            newWaferAttributes.setNewSlotNumber(lotWaferAttributes.getSlotNumber());
            newWaferAttributes.setSourceLotID(monitorLotID);
            newWaferAttributes.setSourceWaferID(lotWaferAttributes.getWaferID());
            newWaferAttributesList.add(newWaferAttributes);
        }
        params.setStrNewLotAttributes(newLotAttributes);
        Results.AutoCreateMonitorForInProcessLotReqResult stbAfterResult = (Results.AutoCreateMonitorForInProcessLotReqResult) processMonitorTestCase.autoCreateMonitorForInProcessLotReq(params).getBody();
        ObjectIdentifier emptyLotID = stbAfterResult.getMonitorLotID();

        //【step11】 after stb process move out success
        opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInResult.getControlJobID());
        opeComWithDataReqParams.setUser(testUtils.getUser());
        opeComWithDataReqParams.setEquipmentID(equipmentID);
        equipmentTestCase.moveOut(opeComWithDataReqParams);

        //【step12】check product lot and empty lot have grouping relation and empty lot have 2 wafer
        productInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(productLotID)).getBody();
        Results.LotInfoInqResult emptyInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(emptyLotID)).getBody();

        Validations.assertCheck(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD,productInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus()),"parent lot must be onHold");
        Validations.assertCheck(CimArrayUtils.getSize(emptyInfoCase.getLotInfoList().get(0).getLotWaferAttributesList()) == 2,"empty cassette have original process monitor wafer size");
        List<Infos.MonitorGroups> productProdLotsRelation = processMonitorTestCase.getMonitorProdLotsRelationCase(productLotID,productCassetteID);
        List<Infos.MonitorGroups> emptyProdLotsRelation = processMonitorTestCase.getMonitorProdLotsRelationCase(emptyLotID,emptyCassette);

        Validations.assertCheck(CimStringUtils.equals(productProdLotsRelation.get(0).getMonitorGroupID(),emptyProdLotsRelation.get(0).getMonitorGroupID()),"monitor relation must have same monitor group id");
    }

    private List<Infos.StartCassette> getNpwReserveStartCassette(Results.EqpInfoInqResult eqpInfoInqResult, Results.LotListByCarrierInqResult listInCassetteInqResult, Results.LotInfoInqResult monitorInfoCase) {
        List<Infos.StartCassette> startCassetteList = new ArrayList<>();
        Infos.StartCassette startCassette = new Infos.StartCassette();

        startCassette.setCassetteID(listInCassetteInqResult.getLotListInCassetteInfo().getCassetteID());
        Infos.EqpPortStatus eqpPortStatusP3 = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().stream().filter(eqpPortStatus -> eqpPortStatus.getPortID().getValue().equals("P3")).findFirst().orElse(null);
        if (eqpPortStatusP3 != null) {
            startCassette.setLoadPortID(eqpPortStatusP3.getPortID());
            startCassette.setUnloadPortID(eqpPortStatusP3.getPortID());
        }
        startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_WAITINGMONITORLOT);
        startCassette.setLoadSequenceNumber(1L);

        List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
        startCassette.setLotInCassetteList(lotInCassetteList);
        for (ObjectIdentifier lotID : listInCassetteInqResult.getLotListInCassetteInfo().getLotIDList()) {
            Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
            lotInCassette.setLotID(lotID);
            lotInCassette.setMoveInFlag(true);
            lotInCassette.setMonitorLotFlag(false);
            lotInCassette.setLotType(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT);
            lotInCassette.setSubLotType(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT);
            lotInCassette.setProductID(monitorInfoCase.getLotInfoList().get(0).getLotProductInfo().getProductID());
            lotInCassette.setRecipeParameterChangeType("");

            lotInCassette.setStartRecipe(new Infos.StartRecipe());
            lotInCassette.setStartOperationInfo(new Infos.StartOperationInfo());

            List<Infos.LotWafer> lotWaferList = new ArrayList<>();
            listInCassetteInqResult.getWaferMapInCassetteInfoList().forEach(waferMapInCassetteInfo -> {
                Infos.LotWafer lotWafer = new Infos.LotWafer();
                lotWafer.setControlWaferFlag(false);
                lotWafer.setParameterUpdateFlag(false);
                lotWafer.setProcessJobExecFlag(true);
                lotWafer.setWaferID(waferMapInCassetteInfo.getWaferID());
                lotWafer.setStartRecipeParameterList(new ArrayList<>());
                lotWafer.setSlotNumber(waferMapInCassetteInfo.getSlotNumber().longValue());
                lotWaferList.add(lotWafer);
            });

            lotInCassette.setLotWaferList(lotWaferList);
            lotInCassetteList.add(lotInCassette);
        }
        startCassetteList.add(startCassette);
        return startCassetteList;
    }
}
