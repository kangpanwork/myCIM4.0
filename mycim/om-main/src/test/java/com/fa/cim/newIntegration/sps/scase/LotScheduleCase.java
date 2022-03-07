package com.fa.cim.newIntegration.sps.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.entity.runtime.lotcomment.CimLotCommentDO;
import com.fa.cim.entity.runtime.productrequest.CimProductRequestDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationDO;
import com.fa.cim.entity.runtime.wafer.CimWaferDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.BankTestCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import com.fa.cim.newIntegration.tcase.LotScheduleTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/16                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/12/16 14:12
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class LotScheduleCase {

    @Autowired
    private LotScheduleTestCase lotScheduleTestCase;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private BankTestCase bankTestCase;

    public void Lot_Creation_By_Volume_Schedule_Daily() {
        final String productIDKey = "PRODUCT0.01";
        this.Lot_Creation_By_Volume_Schedule_Daily(productIDKey);
    }

    public ObjectIdentifier Lot_Creation_By_Volume_Schedule_Daily(String productIDKey) {
        //【step0】choice product id and search
        Params.NewProdOrderCreateReqParams params = new Params.NewProdOrderCreateReqParams();

        //【step1】get product id and Scheduling Type
        CimProductSpecificationDO productSpecification = cimJpaRepository.queryOne("SELECT ID FROM OMPRODINFO WHERE PROD_ID = ?1", CimProductSpecificationDO.class, productIDKey);
        ObjectIdentifier productID = ObjectIdentifier.build(productIDKey,productSpecification.getId());
        String schedulingType = productSpecification.getLotGenerationType();
        ObjectIdentifier routeID = ObjectIdentifier.build(productSpecification.getMainProcessDefinitionID(),productSpecification.getMainProcessDefinitionObj());

        //【step2】input parameter on SPS screen
        final String subLotType = "Normal";
        final String customerCode = "FA";
        final String lotType = "Production";
        final String lotOwner = "ADMIN";
        final String schedulingMode = "Forward";
        final Integer productQuantity = 25;

        //【step3】set start and finish date and start PQ and releaseLotScheduleList
        AtomicReference<String> startTime = new AtomicReference<>();
        AtomicReference<String> finishTime = new AtomicReference<>();
        List<Infos.ReleaseLotSchedule> releaseLotScheduleList = this.getReleaseLotScheduleListByDailyForProduct0_01(startTime,finishTime);

        //【step4】get lotID available
        final ObjectIdentifier lotCreateID = ObjectIdentifier.build("NP001111.00A","");

        //【step5】create product lot
        List<Infos.ReleaseLotAttributes> releaseLotAttributesList = new ArrayList<>();
        params.setReleaseLotAttributesList(releaseLotAttributesList);
        Infos.ReleaseLotAttributes releaseLotAttributes = new Infos.ReleaseLotAttributes();

        releaseLotAttributes.setLotID(lotCreateID);
        releaseLotAttributes.setProductID(productID);
        releaseLotAttributes.setCustomerCode(customerCode);
        releaseLotAttributes.setManufacturingOrderNumber("");
        releaseLotAttributes.setLotOwner(lotOwner);
        releaseLotAttributes.setLotType(lotType);
        releaseLotAttributes.setSubLotType(subLotType);
        releaseLotAttributes.setRouteID(routeID);
        releaseLotAttributes.setLotGenerationType(schedulingType);
        releaseLotAttributes.setSchedulingMode(schedulingMode);

        releaseLotAttributes.setLotIDGenerationMode("Manual");
        releaseLotAttributes.setProductDefinitionMode("By Product Quantity");
        releaseLotAttributes.setPriorityClass("1");
        releaseLotAttributes.setExternalPriority("199999");

        releaseLotAttributes.setPlannedStartTime(startTime.get());
        releaseLotAttributes.setPlannedFinishTime(finishTime.get());

        releaseLotAttributes.setLotComment("");
        releaseLotAttributes.setProductQuantity(productQuantity);
        releaseLotAttributes.setDirectedSourceLotList(new ArrayList<>());

        releaseLotAttributes.setReleaseLotScheduleList(releaseLotScheduleList);
        releaseLotAttributesList.add(releaseLotAttributes);
        Results.NewProdOrderCreateReqResult newProdOrderCreateReqResult = (Results.NewProdOrderCreateReqResult) lotScheduleTestCase.newProdOrderCreateReq(params).getBody();
        ObjectIdentifier newCreateID = newProdOrderCreateReqResult.getReleasedLotReturnList().get(0).getLotID();

        //【step6】om stb release lot check the schedule lot exist
        Boolean findFlag = false;
        Results.ProductOrderReleasedListInqResult releaseList = (Results.ProductOrderReleasedListInqResult) lotScheduleTestCase.getReleasedLotList(productID.getValue(), subLotType).getBody();
        List<Infos.ProdReqListAttribute> reqListAttributes = releaseList.getProductReqListAttributePage().getContent();
        for (Infos.ProdReqListAttribute reqListAttribute : reqListAttributes) {
            if (CimObjectUtils.equalsWithValue(reqListAttribute.getLotID(),lotCreateID)){
                findFlag = true;
                break;
            }
        }
        Validations.assertCheck(findFlag,"test fail");
        return newCreateID;
    }

    public void Lot_Creation_By_Source_Lot_Inheriting() {
        final String productIDKey = "PRODUCT1.01";
        this.Lot_Creation_By_Source_Lot_Inheriting(productIDKey);
    }
    public ObjectIdentifier Lot_Creation_By_Source_Lot_Inheriting(String productIDKey){
        //【step1】choice product id and search
        Params.NewProdOrderCreateReqParams params = new Params.NewProdOrderCreateReqParams();

        //【step2】get Scheduling Lot Selection
        List<TestInfos.SchedulingLotSelection> schedulingLotSelections = this.getSchedulingLotSelectionList();

        //【step2-1】select a lot id in scheduling lot selection,select a product0.01 to do schedule product1.01
        ObjectIdentifier selectLotID = null;
        Boolean findLotFlag = false;
        if (CimArrayUtils.isNotEmpty(schedulingLotSelections)){
            //【step2-2】check selectLotID if exist or used
            for (TestInfos.SchedulingLotSelection schedulingLotSelection : schedulingLotSelections) {
                findLotFlag = this.checkSelectLotIDIfExist(schedulingLotSelection.getLotID());
                if (CimBooleanUtils.isTrue(findLotFlag)){
                    selectLotID = ObjectIdentifier.build(schedulingLotSelection.getLotID(),"");
                    break;
                }
            }
        }

        //【step3】get product id and Scheduling Type
        CimProductSpecificationDO productSpecification = cimJpaRepository.queryOne("SELECT ID FROM OMPRODINFO WHERE PROD_ID = ?1", CimProductSpecificationDO.class, productIDKey);
        ObjectIdentifier productID = ObjectIdentifier.build(productIDKey,productSpecification.getId());
        String schedulingType = productSpecification.getLotGenerationType();
        ObjectIdentifier routeID = ObjectIdentifier.build(productSpecification.getMainProcessDefinitionID(),productSpecification.getMainProcessDefinitionObj());

        //【step4】input parameter on SPS screen
        final String subLotType = "Normal";
        final String customerCode = "FA";
        final String lotType = "Production";
        final String lotOwner = "ADMIN";
        final String schedulingMode = "Forward";
        final Integer productQuantity = 25;

        //【step5】set start and finish date and start PQ and releaseLotScheduleList
        AtomicReference<String> startTime = new AtomicReference<>();
        AtomicReference<String> finishTime = new AtomicReference<>();
        List<Infos.ReleaseLotSchedule> releaseLotScheduleList = this.getReleaseLotScheduleListByDailyForProduct1_01(startTime,finishTime);

        //【step6】get directedSourceLotList
        List<Infos.DirectedSourceLot> directedSourceLotList = this.getDirectedSourceLotList(selectLotID);


        //【step7】create product lot
        List<Infos.ReleaseLotAttributes> releaseLotAttributesList = new ArrayList<>();
        params.setReleaseLotAttributesList(releaseLotAttributesList);
        Infos.ReleaseLotAttributes releaseLotAttributes = new Infos.ReleaseLotAttributes();

        releaseLotAttributes.setLotID(selectLotID);
        releaseLotAttributes.setProductID(productID);
        releaseLotAttributes.setCustomerCode(customerCode);
        releaseLotAttributes.setManufacturingOrderNumber("");
        releaseLotAttributes.setLotOwner(lotOwner);
        releaseLotAttributes.setLotType(lotType);
        releaseLotAttributes.setSubLotType(subLotType);
        releaseLotAttributes.setRouteID(routeID);
        releaseLotAttributes.setLotGenerationType(schedulingType);
        releaseLotAttributes.setSchedulingMode(schedulingMode);

        releaseLotAttributes.setLotIDGenerationMode("Manual");
        releaseLotAttributes.setProductDefinitionMode("By Product Quantity");
        releaseLotAttributes.setPriorityClass("1");
        releaseLotAttributes.setExternalPriority("199999");

        releaseLotAttributes.setPlannedStartTime(startTime.get());
        releaseLotAttributes.setPlannedFinishTime(finishTime.get());

        releaseLotAttributes.setLotComment("");
        releaseLotAttributes.setProductQuantity(productQuantity);
        releaseLotAttributes.setDirectedSourceLotList(directedSourceLotList);

        releaseLotAttributes.setReleaseLotScheduleList(releaseLotScheduleList);
        releaseLotAttributesList.add(releaseLotAttributes);
        Results.NewProdOrderCreateReqResult newProdOrderCreateReqResult = (Results.NewProdOrderCreateReqResult) lotScheduleTestCase.newProdOrderCreateReq(params).getBody();
        ObjectIdentifier newCreateLotID = newProdOrderCreateReqResult.getReleasedLotReturnList().get(0).getLotID();

        //【step8】om stb release lot check the schedule lot exist
        Boolean findFlag = false;
        Results.ProductOrderReleasedListInqResult releaseList = (Results.ProductOrderReleasedListInqResult) lotScheduleTestCase.getReleasedLotList(productID.getValue(), subLotType).getBody();
        List<Infos.ProdReqListAttribute> reqListAttributes = releaseList.getProductReqListAttributePage().getContent();
        for (Infos.ProdReqListAttribute reqListAttribute : reqListAttributes) {
            if (CimObjectUtils.equalsWithValue(reqListAttribute.getLotID(),selectLotID)){
                findFlag = true;
                break;
            }
        }
        Validations.assertCheck(findFlag,"test fail");
        return newCreateLotID;
    }

    private List<Infos.DirectedSourceLot> getDirectedSourceLotList(ObjectIdentifier selectLotID) {
        if (null == selectLotID){
            return null;
        }
        List<Infos.DirectedSourceLot> list = new ArrayList<>();
        Infos.DirectedSourceLot directedSourceLot = new Infos.DirectedSourceLot();
        directedSourceLot.setSourceLotID(selectLotID);
        directedSourceLot.setReserve(null);
        List<Infos.SourceProduct> sourceProducts = new ArrayList<>();
        directedSourceLot.setStrSourceProduct(sourceProducts);
        List<CimWaferDO> cimWaferDOS = cimJpaRepository.query("SELECT WAFER_ID, CONTROL_WAFER FROM FRWAFER WHERE LOT_ID= ?1 AND \n" +
                "SCRAP_STATE<>'Scrap' AND STB_ALLOCATED=0 ORDER BY POSITION", CimWaferDO.class, selectLotID.getValue());
        if (CimArrayUtils.isNotEmpty(cimWaferDOS)){
            for (CimWaferDO cimWaferDO : cimWaferDOS) {
                Infos.SourceProduct sourceProduct = new Infos.SourceProduct();
                sourceProduct.setSourceProduct(cimWaferDO.getWaferID());
                sourceProduct.setReserve(null);
                sourceProducts.add(sourceProduct);
            }
        }
        list.add(directedSourceLot);
        return list;
    }

    private Boolean checkSelectLotIDIfExist(String lotID) {
        if (null == lotID){
            return false;
        }
        List<CimWaferDO> cimWaferDOS = cimJpaRepository.query("SELECT WAFER_ID, CONTROL_WAFER FROM FRWAFER WHERE LOT_ID= ?1 AND \n" +
                "SCRAP_STATE<>'Scrap' AND STB_ALLOCATED=0 ORDER BY POSITION", CimWaferDO.class, lotID);
        return CimArrayUtils.isNotEmpty(cimWaferDOS);
    }

    private List<TestInfos.SchedulingLotSelection> getSchedulingLotSelectionList() {
        String sql = "SELECT LOT.LOT_ID, LOT.PRODSPEC_ID, PRD.MFG_LAYER, COD.DESCRIPTION, LOT.CUSTOMER_ID, LOT.LOT_OWNER_ID, \n" +
                " LOT.ORDER_NO, LOT.LOT_STATE, LOT.LOT_FINISHED_STATE, PO.MAINPD_ID, PO.OPE_NO, PO.PD_ID, LOT.COMPLETION_TIME,\n" +
                " LOT.PLAN_END_TIME, PO.REMAIN_CYCLE_TIME, LOT.QTY, LOT.CNTL_QTY \n" +
                " FROM OMPRODINFO_SRC PRD_SRC, FRLOT LOT,\n" +
                "  OMPRODINFO PRD, OMPRODINFO PRD2, OMCODE COD, OMPROPE PO, FRPD PD, OMPRODINFO_PRP PRD_MPD \n" +
                "  WHERE PRD2.PROD_ID = 'PRODUCT1.01' \n" +
                "  AND PRD_SRC.REFKEY = PRD2.ID \n" +
                "  AND LOT.PROD_ID = PRD_SRC.SRC_PROD_ID\n" +
                "  AND PRD.PROD_ID = PRD_SRC.SRC_PROD_ID \n" +
                "  AND COD.CODETYPE_ID = 'Mfg Layer' \n" +
                "  AND COD.CODE_ID = PRD.MFG_LAYER AND LOT.NOT_ALLOC_QTY > 0 AND PRD2.ID = PRD_MPD.REFKEY\n" +
                "     AND PRD_MPD.ORDER_TYPE = '*' AND LOT.PO_OBJ = PO.ID AND PO.PD_ID = PD.PD_ID\n" +
                "     AND PD.PD_LEVEL = 'Operation' AND ( LOT.LOT_STATE = 'ACTIVE' OR\n" +
                "     ( LOT.LOT_STATE = 'FINISHED' AND LOT.LOT_FINISHED_STATE = 'COMPLETED') ) ORDER BY LOT.LOT_ID";
        List<Object[]> queryResult = cimJpaRepository.query(sql);
        List<TestInfos.SchedulingLotSelection> lotSelectionList = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(queryResult)){
            for (Object[] objects : queryResult) {
                TestInfos.SchedulingLotSelection schedulingLotSelection = new TestInfos.SchedulingLotSelection();
                schedulingLotSelection.setLotID(String.valueOf(objects[0]));
                schedulingLotSelection.setProductID(String.valueOf(objects[1]));
                schedulingLotSelection.setMfgLayer(String.valueOf(objects[2]));
                schedulingLotSelection.setDescription(String.valueOf(objects[3]));
                schedulingLotSelection.setCustomerID(String.valueOf(objects[4]));
                schedulingLotSelection.setLotOwnerID(String.valueOf(objects[5]));
                schedulingLotSelection.setOrderNumber(String.valueOf(objects[6]));
                schedulingLotSelection.setLotState(String.valueOf(objects[7]));
                schedulingLotSelection.setLotFinishState(String.valueOf(objects[8]));
                schedulingLotSelection.setMainPDID(String.valueOf(objects[9]));
                schedulingLotSelection.setOpeNo(String.valueOf(objects[10]));
                schedulingLotSelection.setPdID(String.valueOf(objects[11]));
                schedulingLotSelection.setCompletionTime(CimDateUtils.convertToOrInitialTime(String.valueOf(objects[12])));
                schedulingLotSelection.setPlanEndTime(CimDateUtils.convertToOrInitialTime(String.valueOf(objects[13])));
                schedulingLotSelection.setRemainCycleTime(CimDateUtils.convertToOrInitialTime(String.valueOf(objects[14])));
                schedulingLotSelection.setQty(Integer.valueOf(String.valueOf(objects[15])));
                schedulingLotSelection.setCntlQty(Integer.valueOf(String.valueOf(objects[16])));
                lotSelectionList.add(schedulingLotSelection);
            }
        }
        return lotSelectionList;
    }

    private List<Infos.ReleaseLotSchedule> getReleaseLotScheduleListByDailyForProduct1_01(AtomicReference<String> startDate, AtomicReference<String> finishDate) {
        //【step】get current date next date and am 8
        List<Infos.ReleaseLotSchedule> result = new ArrayList<>();
        AtomicReference<String> startDateSet = getStartDate(startDate);
        List<String> operationNumList = Arrays.asList("5000.0100", "5000.0200","5000.0300","6000.0100","7000.0100","7000.0200","7000.0300","7000.0400",
                "7000.0500","7000.0600","8000.0100","8000.0200","8000.0300","8000.0400","8000.0500","9000.0100","9000.0200","9000.0300","9900.0100","9900.0200");
        for (int i = 0; i < CimArrayUtils.getSize(operationNumList); i++) {
            Infos.ReleaseLotSchedule releaseLotSchedule = new Infos.ReleaseLotSchedule();
            releaseLotSchedule.setPlannedStartTime(startDate.get());
            finishDate = getFinishTime(startDate,finishDate,12);
            releaseLotSchedule.setEquipmentID(null);
            releaseLotSchedule.setOperationNumber(operationNumList.get(i));
            releaseLotSchedule.setPlannedFinishTime(finishDate.get());
            releaseLotSchedule.setReserve(null);
            result.add(releaseLotSchedule);
        }
        startDate.set(startDateSet.get());
        return result;
    }


    private List<Infos.ReleaseLotSchedule> getReleaseLotScheduleListByDailyForProduct0_01(AtomicReference<String> startDate, AtomicReference<String> finishDate){
        //【step】get current date next date and am 8
        List<Infos.ReleaseLotSchedule> result = new ArrayList<>();
        AtomicReference<String> startDateSet = getStartDate(startDate);
        List<String> operationNumList = Arrays.asList("1000.0100", "1000.0200","2000.0100","2000.0200","2000.0300","2000.0350","2000.0400","2000.0500","3000.0100",
                "3000.0200","3000.0300","4000.0100","4000.0150","4000.0160","4000.0170","4000.0200","4000.0300","4000.0400","4000.0500","4000.0600","4000.0700","4000.0800","5000.0100");
        for (int i = 0; i < CimArrayUtils.getSize(operationNumList); i++) {
            Infos.ReleaseLotSchedule releaseLotSchedule = new Infos.ReleaseLotSchedule();
            releaseLotSchedule.setPlannedStartTime(startDate.get());
            if (CimStringUtils.equals("2000.0500",operationNumList.get(i))) {
                finishDate = getFinishTime(startDate, finishDate,9);
            }else if (CimStringUtils.equals("5000.0100",operationNumList.get(i))){
                finishDate = getFinishTime(startDate,finishDate,12);
            }else if ("2000.0500".compareTo(operationNumList.get(i)) > 0){
                finishDate = getFinishTime(startDate,finishDate,12);
            }else {
                finishDate = getFinishTime(startDate,finishDate,15);
            }

            releaseLotSchedule.setEquipmentID(null);
            releaseLotSchedule.setOperationNumber(operationNumList.get(i));
            releaseLotSchedule.setPlannedFinishTime(finishDate.get());
            releaseLotSchedule.setReserve(null);
            result.add(releaseLotSchedule);
        }
        startDate.set(startDateSet.get());
        return result;
    }

    private AtomicReference<String> getFinishTime(AtomicReference<String> startDate,AtomicReference<String> finishState,Integer intervalTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long time = CimDateUtils.convertToOrInitialTime(startDate.get()).getTime();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(time));
        calendar.add(calendar.MINUTE,intervalTime);
        String finishTime = sdf.format(calendar.getTime());
        startDate.set(finishTime);
        finishState.set(finishTime);
        return finishState;
    }

    private AtomicReference<String> getStartDate(AtomicReference<String>startDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = new GregorianCalendar();
        Date dateNow = new Date();
        dateNow.setHours(8);
        dateNow.setSeconds(00);
        dateNow.setMinutes(00);
        calendar.setTime(dateNow);
        calendar.add(calendar.DATE,1);
        String dateNextDay = sdf.format(calendar.getTime());
        startDate.set(dateNextDay);
        return new AtomicReference<>(dateNextDay);
    }

    public void Lot_Creation_By_Source_Lot_Next() {
        final String productIDKey = "PRODUCT1.01";
        this.Lot_Creation_By_Source_Lot_Next(productIDKey);
    }

    public ObjectIdentifier Lot_Creation_By_Source_Lot_Next(String productIDKey){
        //【step1】choice product id and search
        Params.NewProdOrderCreateReqParams params = new Params.NewProdOrderCreateReqParams();

        //【step2】get Scheduling Lot Selection
        List<TestInfos.SchedulingLotSelection> schedulingLotSelections = this.getSchedulingLotSelectionList();

        //【step2-1】select a lot id in scheduling lot selection,select a product0.01 to do schedule product1.01
        ObjectIdentifier selectLotID = null;
        Boolean findLotFlag = false;
        if (CimArrayUtils.isNotEmpty(schedulingLotSelections)){
            //【step2-2】check selectLotID if exist or used
            for (TestInfos.SchedulingLotSelection schedulingLotSelection : schedulingLotSelections) {
                findLotFlag = this.checkSelectLotIDIfExist(schedulingLotSelection.getLotID());
                if (CimBooleanUtils.isTrue(findLotFlag)){
                    selectLotID = ObjectIdentifier.build(schedulingLotSelection.getLotID(),"");
                    break;
                }
            }
        }

        //【step3】get product id and Scheduling Type
        CimProductSpecificationDO productSpecification = cimJpaRepository.queryOne("SELECT ID FROM OMPRODINFO WHERE PROD_ID = ?1", CimProductSpecificationDO.class, productIDKey);
        ObjectIdentifier productID = ObjectIdentifier.build(productIDKey,productSpecification.getId());
        String schedulingType = productSpecification.getLotGenerationType();
        ObjectIdentifier routeID = ObjectIdentifier.build(productSpecification.getMainProcessDefinitionID(),productSpecification.getMainProcessDefinitionObj());

        //【step4】input parameter on SPS screen
        final String subLotType = "Normal";
        final String customerCode = "FA";
        final String lotType = "Production";
        final String lotOwner = "ADMIN";
        final String schedulingMode = "Forward";
        final Integer productQuantity = 25;

        //【step5】set start and finish date and start PQ and releaseLotScheduleList
        AtomicReference<String> startTime = new AtomicReference<>();
        AtomicReference<String> finishTime = new AtomicReference<>();
        List<Infos.ReleaseLotSchedule> releaseLotScheduleList = this.getReleaseLotScheduleListByDailyForProduct1_01(startTime,finishTime);

        //【step6】get directedSourceLotList
        List<Infos.DirectedSourceLot> directedSourceLotList = this.getDirectedSourceLotList(selectLotID);

        //【step6-1】get next new lotID
        ObjectIdentifier newLotID = ObjectIdentifier.build("NP001111.00A","");


        //【step7】create product lot
        List<Infos.ReleaseLotAttributes> releaseLotAttributesList = new ArrayList<>();
        params.setReleaseLotAttributesList(releaseLotAttributesList);
        Infos.ReleaseLotAttributes releaseLotAttributes = new Infos.ReleaseLotAttributes();

        releaseLotAttributes.setLotID(newLotID);
        releaseLotAttributes.setProductID(productID);
        releaseLotAttributes.setCustomerCode(customerCode);
        releaseLotAttributes.setManufacturingOrderNumber("");
        releaseLotAttributes.setLotOwner(lotOwner);
        releaseLotAttributes.setLotType(lotType);
        releaseLotAttributes.setSubLotType(subLotType);
        releaseLotAttributes.setRouteID(routeID);
        releaseLotAttributes.setLotGenerationType(schedulingType);
        releaseLotAttributes.setSchedulingMode(schedulingMode);

        releaseLotAttributes.setLotIDGenerationMode("Manual");
        releaseLotAttributes.setProductDefinitionMode("By Product Quantity");
        releaseLotAttributes.setPriorityClass("1");
        releaseLotAttributes.setExternalPriority("199999");

        releaseLotAttributes.setPlannedStartTime(startTime.get());
        releaseLotAttributes.setPlannedFinishTime(finishTime.get());

        releaseLotAttributes.setLotComment("");
        releaseLotAttributes.setProductQuantity(productQuantity);
        releaseLotAttributes.setDirectedSourceLotList(directedSourceLotList);

        releaseLotAttributes.setReleaseLotScheduleList(releaseLotScheduleList);
        releaseLotAttributesList.add(releaseLotAttributes);
        Results.NewProdOrderCreateReqResult releaseReqResult = (Results.NewProdOrderCreateReqResult) lotScheduleTestCase.newProdOrderCreateReq(params).getBody();
        ObjectIdentifier newCreateLotID = releaseReqResult.getReleasedLotReturnList().get(0).getLotID();

        //【step8】om stb release lot check the schedule lot exist
        Boolean findFlag = false;
        Results.ProductOrderReleasedListInqResult releaseList = (Results.ProductOrderReleasedListInqResult) lotScheduleTestCase.getReleasedLotList(productID.getValue(), subLotType).getBody();
        List<Infos.ProdReqListAttribute> reqListAttributes = releaseList.getProductReqListAttributePage().getContent();
        for (Infos.ProdReqListAttribute reqListAttribute : reqListAttributes) {
            if (CimObjectUtils.equalsWithValue(reqListAttribute.getLotID(),newLotID)){
                findFlag = true;
                break;
            }
        }
        Validations.assertCheck(findFlag,"test fail");
        return newCreateLotID;
    }
    public void Lot_Change() {

        final String productIDKey = "PRODUCT0.01";

        //【step1】get product id and Scheduling Type
        CimProductSpecificationDO productSpecification = cimJpaRepository.queryOne("SELECT ID FROM OMPRODINFO WHERE PROD_ID = ?1", CimProductSpecificationDO.class, productIDKey);
        ObjectIdentifier productID = ObjectIdentifier.build(productIDKey,productSpecification.getId());
        String schedulingType = productSpecification.getLotGenerationType();
        ObjectIdentifier routeID = ObjectIdentifier.build(productSpecification.getMainProcessDefinitionID(),productSpecification.getMainProcessDefinitionObj());
        String manufacturingLayer = productSpecification.getManufacturingLayer();


        //【step2】stb a product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID.getValue());
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step3】get lot selection list
        String lotStatus = "WIP";
        String selectProcessFlowID = "12REFIN.01";
        String selectProduct = "DEV-12REFAA001.01";
        String selectSubType = "Special";
        String selectOperationNum = "2000.0400";
        this.lotSchdlChangeByParameter(routeID,lotID,lotStatus,productID,manufacturingLayer,
                true,true,true,selectProcessFlowID,selectProduct,selectSubType,selectOperationNum);

        //【step4】om check the change if occur
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        String newSubLotType = lotInfo.getLotBasicInfo().getSubLotType();
        ObjectIdentifier newProductID = lotInfo.getLotProductInfo().getProductID();
        ObjectIdentifier newRouteID = lotInfo.getLotOperationInfo().getRouteID();
        String newOperationNumber = lotInfo.getLotOperationInfo().getOperationNumber();
        Boolean checkFlag = false;
        if (CimStringUtils.equals(newProductID.getValue(),selectProduct)
                && CimStringUtils.equals(newRouteID.getValue(),selectProcessFlowID)
                && CimStringUtils.equals(newSubLotType,selectSubType)
                && CimStringUtils.equals(newOperationNumber,selectOperationNum)){
            checkFlag = true;
        }
        Validations.assertCheck(checkFlag,"test error");
    }

    public void lotSchdlChangeByParameter(ObjectIdentifier routeID ,ObjectIdentifier lotID,String lotStatus,ObjectIdentifier productID,String manufacturingLayer,Boolean selectProductIDFlag,Boolean selectProcessFlowFlag,Boolean selectSubTypeFlag,String selectProcessFlowID,String selectProduct,String selectSubType,String selectOperaionNum) {
        //【step1】check lot status and get parameter
        List lotSelectionListByLotStatus = this.getLotSelectionListByLotStatus(lotStatus,productID,manufacturingLayer);
        List<TestInfos.WIPInfos> lotSelectionListByLotStatus1 = new ArrayList<>();
        List<TestInfos.BankInInfos> lotSelectionListByLotStatus2 = new ArrayList<>();
        List<TestInfos.PreLotStartInfos> lotSelectionListByLotStatus3 = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(lotSelectionListByLotStatus)){
            if (lotSelectionListByLotStatus.get(0) instanceof TestInfos.WIPInfos){
                lotSelectionListByLotStatus1 = (List<TestInfos.WIPInfos>)lotSelectionListByLotStatus;
            }else if (lotSelectionListByLotStatus.get(0) instanceof TestInfos.BankInInfos){
                lotSelectionListByLotStatus2 = (List<TestInfos.BankInInfos>) lotSelectionListByLotStatus;
            }else if (lotSelectionListByLotStatus.get(0) instanceof TestInfos.PreLotStartInfos){
                lotSelectionListByLotStatus3 = (List<TestInfos.PreLotStartInfos>) lotSelectionListByLotStatus;
            }
        }

        //【step2】get lotSelectionListByLotStatus and select sbt lot
        Boolean findFlag = false;
        String selectLotID = null;
        String shedulingMode = null;
        Integer priorityClass = null;
        String subLotType = null;
        String lotType = null;
        for (TestInfos.WIPInfos wipInfos : lotSelectionListByLotStatus1) {
            if (CimStringUtils.equals(lotID.getValue(),wipInfos.getLotID())){
                findFlag = true;
                selectLotID = wipInfos.getLotID();
                shedulingMode = wipInfos.getScheduleMode();
                priorityClass = wipInfos.getPriorityClass();
                subLotType = wipInfos.getSubLotType();
                lotType = wipInfos.getLotType();
                break;
            }
        }
        for (TestInfos.BankInInfos bankInInfos : lotSelectionListByLotStatus2) {
            if (CimStringUtils.equals(lotID.getValue(),bankInInfos.getLotID())){
                findFlag = true;
                selectLotID = bankInInfos.getLotID();
                shedulingMode = bankInInfos.getScheduleMode();
                priorityClass = bankInInfos.getPriorityClass();
                subLotType = bankInInfos.getSubLotType();
                lotType = bankInInfos.getLotType();
                break;
            }
        }
        for (TestInfos.PreLotStartInfos preLotStartInfos : lotSelectionListByLotStatus3) {
            if (CimStringUtils.equals(lotID.getValue(),preLotStartInfos.getProdReqID())){
                findFlag = true;
                selectLotID = preLotStartInfos.getProdReqID();
                shedulingMode = preLotStartInfos.getScheduleMode();
                priorityClass = preLotStartInfos.getPriorityClass();
                subLotType = preLotStartInfos.getSubLotType();
                lotType = preLotStartInfos.getLotType();
                break;
            }
        }
        Validations.assertCheck(findFlag,"test fail");

        //【step3】get schedule change param
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        String originalOperationNumber = lotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getOperationNumber();
        ObjectIdentifier selectProductID = productID;
        if (CimBooleanUtils.isTrue(selectProductIDFlag)){
            selectProductID  = this.selectProductID(manufacturingLayer,selectProcessFlowID,selectProduct);
        }
        Map<String,String> selectProcessFlow = null;
        if (CimBooleanUtils.isTrue(selectProcessFlowFlag) && selectProductID != null){
            selectProcessFlow = this.getProcessFlowID(selectProductID.getValue());
        }
        String selectSubLotType = subLotType;
        if (CimBooleanUtils.isTrue(selectSubTypeFlag)){
            selectSubLotType = this.getSubLotType(lotType,selectSubType);
        }

        //【step4】schedule change request
        Params.LotScheduleChangeReqParams params = new Params.LotScheduleChangeReqParams();
        List<Infos.ReScheduledLotAttributes> reScheduledLotAttributesList = new ArrayList<>();
        Infos.ReScheduledLotAttributes reScheduledLotAttributes = new Infos.ReScheduledLotAttributes();
        reScheduledLotAttributesList.add(reScheduledLotAttributes);

        reScheduledLotAttributes.setLotID(ObjectIdentifier.buildWithValue(selectLotID));
        reScheduledLotAttributes.setProductID(selectProductID);
        reScheduledLotAttributes.setOriginalRouteID(routeID.getValue());
        if (null == selectProcessFlow || selectProcessFlow.isEmpty()){
            reScheduledLotAttributes.setRouteID(routeID);
            reScheduledLotAttributes.setCurrentOperationNumber(originalOperationNumber);
        }else {
            for (String key : selectProcessFlow.keySet()) {
                if (CimStringUtils.equals(selectOperaionNum,key)){
                    reScheduledLotAttributes.setRouteID(new ObjectIdentifier(selectProcessFlow.get(key)));
                    reScheduledLotAttributes.setCurrentOperationNumber(key);
                    break;
                }
            }
        }
        reScheduledLotAttributes.setOriginalOperationNumber(originalOperationNumber);
        reScheduledLotAttributes.setSubLotType(selectSubLotType);
        reScheduledLotAttributes.setShedulingMode(shedulingMode);
        reScheduledLotAttributes.setPriorityClass(priorityClass.toString());

        //【step4-1】change time
        AtomicReference<String> startTime = new AtomicReference<>();
        AtomicReference<String> finishTime = new AtomicReference<>();
        List<Infos.ReleaseLotSchedule> releaseLotScheduleList = this.getReleaseLotScheduleListByDailyForProduct0_01(startTime,finishTime);

        reScheduledLotAttributes.setPlannedStartTime(startTime.get());
        reScheduledLotAttributes.setPlannedFinishTime(finishTime.get());
        List<Infos.ChangedLotSchedule> changedLotSchedules = new ArrayList<>();
        releaseLotScheduleList.forEach(x -> {
            Infos.ChangedLotSchedule changedLotSchedule = new Infos.ChangedLotSchedule();
            changedLotSchedule.setEquipmentID(x.getEquipmentID());
            changedLotSchedule.setOperationNumber(x.getOperationNumber());
            changedLotSchedule.setPlannedStartTime(x.getPlannedStartTime());
            changedLotSchedule.setPlannedFinishTime(x.getPlannedFinishTime());
            changedLotSchedules.add(changedLotSchedule);
        });
        reScheduledLotAttributes.setChangedLotScheduleList(changedLotSchedules);

        //【step5】lot schedule change request
        params.setReScheduledLotAttributesList(reScheduledLotAttributesList);
        lotScheduleTestCase.lotPlanChangeReq(params);
    }

    private String getSubLotType(String lotType,String selectSubLotType) {
        String sql = "SELECT LOTTYP_SUB.SUB_LOT_TYPE FROM FRLOTTYPE LOTTYP,FRLOTTYPE_SUBTYPE LOTTYP_SUB \n" +
                     "WHERE LOTTYP.LOTTYPE_ID = ?1 AND LOTTYP_SUB.REFKEY = LOTTYP.ID ORDER BY LOTTYP_SUB.SUB_LOT_TYPE";
        List<Object[]> query = cimJpaRepository.query(sql, lotType);
        if (CimArrayUtils.isNotEmpty(query)){
            for (Object[] objects : query) {
                if (CimStringUtils.equals(selectSubLotType,String.valueOf(objects[0]))){
                    return selectSubLotType;
                }
            }
        }
        return null;
    }

    private Map<String,String> getProcessFlowID(String selectProductID) {
        Map<String,String> result = new HashMap<>();
        String sql = "SELECT CASE WHEN substr(PRDSPC_MPD.MAIN_PROCESS_ID,-3,3)='.##' THEN\n" +
                "    (SELECT FRPD.ACTIVE_ID FROM FRPD FRPD WHERE FRPD.PD_ID=PRDSPC_MPD.MAIN_PROCESS_ID AND\n" +
                "    FRPD.PD_LEVEL='Main') ELSE PRDSPC_MPD.MAIN_PROCESS_ID END AS MAINPD_ID,PRDSPC_MPD.ORDER_TYPE\n" +
                "    FROM OMPRODINFO PRDSPC, OMPRODINFO_PRP PRDSPC_MPD WHERE PRDSPC.PROD_ID = ?1\n" +
                "    AND PRDSPC.ID = PRDSPC_MPD.REFKEY ORDER BY PRDSPC_MPD.MAIN_PROCESS_ID, PRDSPC_MPD.ORDER_TYPE ";
        List<Object[]> queryResult = cimJpaRepository.query(sql, selectProductID);
        String processFlowId = null;
        if (CimArrayUtils.isNotEmpty(queryResult)){
            for (Object[] objects : queryResult) {
                processFlowId = String.valueOf(objects[0]);
            }
        }
        String sql1 = "SELECT PF_POS.OPE_NO, PD.OPE_NAME, PF_POS.d_SeqNo FROM FVPF_POS_SCH PF_POS, FRPD PD WHERE PF_POS.MAINPD_ID= ?1 AND PF_POS.PD_KEY=PD.ID ORDER BY PF_POS.D_SEQNO";
        if (CimStringUtils.isNotEmpty(processFlowId)){
            List<Object[]> queryResult1 = cimJpaRepository.query(sql1, processFlowId);
            if (CimArrayUtils.isNotEmpty(queryResult1)){
                for (Object[] objects : queryResult1) {
                    result.put(String.valueOf(objects[0]),processFlowId);
                }
            }
        }
        return result;
    }

    private ObjectIdentifier selectProductID(String manufacturingLayer,String selectFlow,String selectProductID) {
        String sql = "SELECT DISTINCT PRD.PROD_ID, CASE WHEN substr(PRD.MAIN_PROCESS_ID,-3,3)='.##' THEN\n" +
                "    (SELECT FRPD.ACTIVE_ID FROM FRPD FRPD WHERE FRPD.PD_ID=PRD_MPD.MAIN_PROCESS_ID AND FRPD.PD_LEVEL='Main') ELSE\n" +
                "    PRD_MPD.MAIN_PROCESS_ID END MPD,PRD.DESCRIPTION, PRD_MPD.ORDER_TYPE FROM OMPRODINFO PRD, OMPRODINFO_PRP PRD_MPD,\n" +
                "    OMUSER USR, OMUSER_USERGRP USR_USRG, OMUSERGRP UGRP WHERE PRD.MFG_LAYER = ?1\n" +
                "    AND USR.USER_ID = 'ADMIN' AND PRD.STATE = 'Complete' AND PRD.SPS_OWNER_GRP = UGRP.USER_GRP_ID\n" +
                "    AND UGRP.USER_GRP_TYPE = 'Scheduler' AND ((USR_USRG.REFKEY = USR.ID AND UGRP.USER_GRP_ID = USR_USRG.USER_GRP_ID) )\n" +
                "    AND PRD.ID = PRD_MPD.REFKEY ORDER BY PRD.PROD_ID ";
        List<Object[]> query = cimJpaRepository.query(sql, manufacturingLayer);
        String productID = null;
        String processFlow = null;
        if (CimArrayUtils.isNotEmpty(query)){
            for (Object[] objects : query) {
                productID = String.valueOf(objects[0]);
                processFlow = String.valueOf(objects[1]);
                if (CimStringUtils.equals(selectProductID,productID) && CimStringUtils.equals(selectFlow,processFlow)){
                    return new ObjectIdentifier(String.valueOf(objects[0]));
                }
            }
        }
        return null;
    }

    private List getLotSelectionListByLotStatus(String lotStatus, ObjectIdentifier productID, String manufacturingLayer) {
        List result = new ArrayList();
        String sql = null;
        switch (lotStatus){
            case "WIP":{
                sql = "SELECT \n" +
                        "LOT.LOT_ID, \n" +
                        "PO.MAINPD_ID, \n" +
                        "PO.OPE_NO, \n" +
                        "LOT.SCHEDULE_MODE, \n" +
                        "PO.PLAN_START_TIME, \n" +
                        "LOT.PLAN_END_TIME,\n" +
                        "LOT.PRIORITY_CLASS,\n" +
                        "LOT.CUSTOMER_ID, \n" +
                        "LOT.LOT_OWNER_ID, \n" +
                        "LOT.ORDER_NO, \n" +
                        "LOT.LOT_TYPE, \n" +
                        "LOT.LOT_PROCESS_STATE, \n" +
                        "PD.FLOW_TYPE, \n" +
                        "LOT.ORDER_TYPE, \n" +
                        "LOT.SUB_LOT_TYPE, \n" +
                        "LOT.CTRLJOB_ID, \n" +
                        "CASE WHEN (SELECT COUNT(*) FROM OSLOTPLANCHGRSV SCH \n" +
                        "WHERE SCH.OBJECT_ID=LOT.LOT_ID \n" +
                        "AND SCH.LOTINFO_CHANGE_FLAG=1) > 0 \n" +
                        "THEN 1 \n" +
                        "ELSE 0 END as LOTINFO_CHANGE_FLAG \n" +
                        "FROM \n" +
                        "FRLOT LOT, \n" +
                        "OMPROPE PO, \n" +
                        "FRPD PD \n" +
                        "WHERE \n" +
                        "LOT.MFG_LAYER = ?1 AND \n" +
                        "LOT.PRODSPEC_ID = ?2 AND \n" +
                        "LOT.LOT_STATE = 'ACTIVE' AND \n" +
                        "PO.ID=LOT.PO_OBJ AND\n" +
                        "PD.PD_LEVEL = 'Main' AND \n" +
                        "PD.PD_ID = PO.MAINPD_ID \n" +
                        "ORDER BY \n" +
                        "LOT.LOT_ID ";
                List<Object[]> queryResult = cimJpaRepository.query(sql, manufacturingLayer, productID.getValue());
                if (CimArrayUtils.isNotEmpty(queryResult)){
                    for (Object[] objects : queryResult) {
                        TestInfos.WIPInfos wipInfos = new TestInfos.WIPInfos();
                        wipInfos.setLotID(String.valueOf(objects[0]));
                        wipInfos.setMainPDID(String.valueOf(objects[1]));
                        wipInfos.setOpeNo(String.valueOf(objects[2]));
                        wipInfos.setScheduleMode(String.valueOf(objects[3]));
                        wipInfos.setPlanStartTime(CimDateUtils.convertToOrInitialTime(String.valueOf(objects[4])));
                        wipInfos.setPlanEndTime(CimDateUtils.convertToOrInitialTime(String.valueOf(objects[5])));
                        wipInfos.setPriorityClass(Integer.valueOf(String.valueOf(objects[6])));
                        wipInfos.setCustomerID(String.valueOf(objects[7]));
                        wipInfos.setLotOwnerID(String.valueOf(objects[8]));
                        wipInfos.setOrderNo(String.valueOf(objects[9]));
                        wipInfos.setLotType(String.valueOf(objects[10]));
                        wipInfos.setLotProcessState(String.valueOf(objects[11]));
                        wipInfos.setFlowType(String.valueOf(objects[12]));
                        wipInfos.setOrderType(String.valueOf(objects[13]));
                        wipInfos.setSubLotType(String.valueOf(objects[14]));
                        wipInfos.setControlJobID(String.valueOf(objects[15]));
                        wipInfos.setLotInfoChangeFlag(String.valueOf(objects[16]));
                        result.add(wipInfos);
                    }
                }
                break;
            }
            case "BankIn":{
                sql = "SELECT \n" +
                        "LOT.LOT_ID, \n" +
                        "PO.MAINPD_ID, \n" +
                        "PO.OPE_NO, \n" +
                        "LOT.SCHEDULE_MODE, \n" +
                        "LOT.RELEASED_TIME, \n" +
                        "LOT.COMPLETION_TIME, \n" +
                        "LOT.PRIORITY_CLASS, \n" +
                        "LOT.CUSTOMER_ID,\n" +
                        "LOT.LOT_OWNER_ID, \n" +
                        "LOT.ORDER_NO, \n" +
                        "LOT.ORDER_TYPE, \n" +
                        "LOT.LOT_TYPE, \n" +
                        "LOT.SUB_LOT_TYPE \n" +
                        "FROM \n" +
                        "FRLOT LOT, \n" +
                        "OMPROPE PO \n" +
                        "WHERE\n" +
                        "LOT.PRODSPEC_ID = ?1 AND \n" +
                        "LOT.LOT_STATE = 'FINISHED' AND \n" +
                        "LOT.LOT_FINISHED_STATE = 'COMPLETED' AND\n" +
                        "LOT.MFG_LAYER = ?2 AND\n" +
                        "PO.ID=LOT.PO_OBJ \n" +
                        "ORDER BY \n" +
                        "LOT.LOT_ID";
                List<Object[]> queryResult = cimJpaRepository.query(sql, productID.getValue(),manufacturingLayer);
                if (CimArrayUtils.isNotEmpty(queryResult)){
                    TestInfos.BankInInfos bankInInfos = new TestInfos.BankInInfos();
                    if (CimArrayUtils.isNotEmpty(queryResult)){
                        for (Object[] objects : queryResult) {
                            bankInInfos.setLotID(String.valueOf(objects[0]));
                            bankInInfos.setMainPDID(String.valueOf(objects[1]));
                            bankInInfos.setOpeNo(String.valueOf(objects[2]));
                            bankInInfos.setScheduleMode(String.valueOf(objects[3]));
                            bankInInfos.setReleasedTime(CimDateUtils.convertToOrInitialTime(String.valueOf(objects[4])));
                            bankInInfos.setCompletionTime(CimDateUtils.convertToOrInitialTime(String.valueOf(objects[5])));
                            bankInInfos.setPriorityClass(Integer.valueOf(String.valueOf(objects[6])));
                            bankInInfos.setCustomerID(String.valueOf(objects[7]));
                            bankInInfos.setLotOwnerID(String.valueOf(objects[8]));
                            bankInInfos.setOrderNo(String.valueOf(objects[9]));
                            bankInInfos.setOrderType(String.valueOf(objects[10]));
                            bankInInfos.setLotType(String.valueOf(objects[11]));
                            bankInInfos.setSubLotType(String.valueOf(objects[12]));
                            result.add(bankInInfos);
                        }
                    }
                }
                break;
            }
            case "PreLotStart":{
                sql = "SELECT \n" +
                        "PRDREQ.PROD_ORDER_ID, \n" +
                        "PRDREQ.MAIN_PROCESS_ID, \n" +
                        "PRDREQ.SCHEDULE_TYPE, \n" +
                        "PRDREQ.PLAN_RELEASE_DATE, \n" +
                        "PRDREQ.CUST_DELIVER_DATE, \n" +
                        "PRDREQ.LOT_PRIORITY, \n" +
                        "PRDREQ.CUSTOMER_ID,\n" +
                        "PRDREQ.LOT_OWNER_ID,\n" +
                        "PRDREQ.MFG_ORDER_NO, \n" +
                        "PRDREQ.PO_COMMENT, \n" +
                        "PRDREQ.LOT_TYPE, \n" +
                        "PRDREQ.ORDER_TYPE, \n" +
                        "PRDREQ.SUB_LOT_TYPE \n" +
                        "FROM \n" +
                        "OMPRORDER PRDREQ \n" +
                        "WHERE \n" +
                        "PRDREQ.MFG_LAYER = ?1 AND \n" +
                        "PRDREQ.PROD_ID = ?2 AND \n" +
                        "PRDREQ.PO_PROD_STATE = 'NOTINRELEASE' \n" +
                        "ORDER BY \n" +
                        "PRDREQ.PROD_ORDER_ID ";
                List<Object[]> queryResult = cimJpaRepository.query(sql, manufacturingLayer, productID.getValue());
                if (CimArrayUtils.isNotEmpty(queryResult)){
                    for (Object[] objects : queryResult) {
                        TestInfos.PreLotStartInfos preLotStartInfos = new TestInfos.PreLotStartInfos();
                        preLotStartInfos.setProdReqID(String.valueOf(objects[0]));
                        preLotStartInfos.setMainPDID(String.valueOf(objects[1]));
                        preLotStartInfos.setScheduleMode(String.valueOf(objects[2]));
                        preLotStartInfos.setPlanReleaseTime(CimDateUtils.convertToOrInitialTime(String.valueOf(objects[3])));
                        preLotStartInfos.setDeliveryTime(CimDateUtils.convertToOrInitialTime(String.valueOf(objects[4])));
                        preLotStartInfos.setPriorityClass(Integer.valueOf(String.valueOf(objects[5])));
                        preLotStartInfos.setCustomerID(String.valueOf(objects[6]));
                        preLotStartInfos.setLotOwnerID(String.valueOf(objects[7]));
                        preLotStartInfos.setOrderNo(String.valueOf(objects[8]));
                        preLotStartInfos.setLotComment(String.valueOf(objects[9]));
                        preLotStartInfos.setLotType(String.valueOf(objects[10]));
                        preLotStartInfos.setOrderType(String.valueOf(objects[11]));
                        preLotStartInfos.setSubLotType(String.valueOf(objects[12]));
                        result.add(preLotStartInfos);
                    }
                }
                break;
            }
        }
        return result;
    }

    public void Pre_Lot_Start_Cancel() {
        //【1】create a lot by volume
        final String productID = "PRODUCT0.01";
        ObjectIdentifier createNewLotID = this.Lot_Creation_By_Volume_Schedule_Daily(productID);

        //【step3】check the release list
        Boolean findFlag = false;
        Results.ProductOrderReleasedListInqResult releaseList = (Results.ProductOrderReleasedListInqResult) lotScheduleTestCase.getReleasedLotList(productID, "Normal").getBody();
        List<Infos.ProdReqListAttribute> reqListAttributes = releaseList.getProductReqListAttributePage().getContent();
        for (Infos.ProdReqListAttribute reqListAttribute : reqListAttributes) {
            if (CimObjectUtils.equalsWithValue(reqListAttribute.getLotID(),createNewLotID)){
                findFlag = true;
                break;
            }
        }
        Validations.assertCheck(!findFlag,"test fail");
    }

    public void Pre_Lot_Start_Cancel_By_Source_Lot() {
        //【step1】choice product id and search
        Params.NewProdOrderCreateReqParams params = new Params.NewProdOrderCreateReqParams();
        final String productIDKey = "PRODUCT1.01";

        //【step2】get Scheduling Lot Selection
        List<TestInfos.SchedulingLotSelection> schedulingLotSelections = this.getSchedulingLotSelectionList();

        //【step2-1】select a lot id in scheduling lot selection,select a product0.01 to do schedule product1.01
        ObjectIdentifier selectLotID = null;
        Boolean findLotFlag = false;
        if (CimArrayUtils.isNotEmpty(schedulingLotSelections)){
            //【step2-2】check selectLotID if exist or used
            for (TestInfos.SchedulingLotSelection schedulingLotSelection : schedulingLotSelections) {
                findLotFlag = this.checkSelectLotIDIfExist(schedulingLotSelection.getLotID());
                if (CimBooleanUtils.isTrue(findLotFlag)){
                    selectLotID = ObjectIdentifier.build(schedulingLotSelection.getLotID(),"");
                    break;
                }
            }
        }

        //【step3】get product id and Scheduling Type
        CimProductSpecificationDO productSpecification = cimJpaRepository.queryOne("SELECT ID FROM OMPRODINFO WHERE PROD_ID = ?1", CimProductSpecificationDO.class, productIDKey);
        ObjectIdentifier productID = ObjectIdentifier.build(productIDKey,productSpecification.getId());
        String schedulingType = productSpecification.getLotGenerationType();
        ObjectIdentifier routeID = ObjectIdentifier.build(productSpecification.getMainProcessDefinitionID(),productSpecification.getMainProcessDefinitionObj());

        //【step4】input parameter on SPS screen
        final String subLotType = "Normal";
        final String customerCode = "FA";
        final String lotType = "Production";
        final String lotOwner = "ADMIN";
        final String schedulingMode = "Forward";
        final Integer productQuantity = 25;

        //【step5】set start and finish date and start PQ and releaseLotScheduleList
        AtomicReference<String> startTime = new AtomicReference<>();
        AtomicReference<String> finishTime = new AtomicReference<>();
        List<Infos.ReleaseLotSchedule> releaseLotScheduleList = this.getReleaseLotScheduleListByDailyForProduct1_01(startTime,finishTime);

        //【step6】get directedSourceLotList
        List<Infos.DirectedSourceLot> directedSourceLotList = this.getDirectedSourceLotList(selectLotID);

        //【step6-1】get next new lotID
        ObjectIdentifier newLotID = ObjectIdentifier.build("NP001111.00A","");


        //【step7】create product lot
        List<Infos.ReleaseLotAttributes> releaseLotAttributesList = new ArrayList<>();
        params.setReleaseLotAttributesList(releaseLotAttributesList);
        Infos.ReleaseLotAttributes releaseLotAttributes = new Infos.ReleaseLotAttributes();

        releaseLotAttributes.setLotID(newLotID);
        releaseLotAttributes.setProductID(productID);
        releaseLotAttributes.setCustomerCode(customerCode);
        releaseLotAttributes.setManufacturingOrderNumber("");
        releaseLotAttributes.setLotOwner(lotOwner);
        releaseLotAttributes.setLotType(lotType);
        releaseLotAttributes.setSubLotType(subLotType);
        releaseLotAttributes.setRouteID(routeID);
        releaseLotAttributes.setLotGenerationType(schedulingType);
        releaseLotAttributes.setSchedulingMode(schedulingMode);

        releaseLotAttributes.setLotIDGenerationMode("Manual");
        releaseLotAttributes.setProductDefinitionMode("By Product Quantity");
        releaseLotAttributes.setPriorityClass("1");
        releaseLotAttributes.setExternalPriority("199999");

        releaseLotAttributes.setPlannedStartTime(startTime.get());
        releaseLotAttributes.setPlannedFinishTime(finishTime.get());

        releaseLotAttributes.setLotComment("");
        releaseLotAttributes.setProductQuantity(productQuantity);
        releaseLotAttributes.setDirectedSourceLotList(directedSourceLotList);

        releaseLotAttributes.setReleaseLotScheduleList(releaseLotScheduleList);
        releaseLotAttributesList.add(releaseLotAttributes);
        Results.NewProdOrderCreateReqResult newProdOrderCreateReqResult = (Results.NewProdOrderCreateReqResult)lotScheduleTestCase.newProdOrderCreateReq(params).getBody();
        ObjectIdentifier newCreateLotID = newProdOrderCreateReqResult.getReleasedLotReturnList().get(0).getLotID();

        //【step8】om stb release lot check the schedule lot exist
        Boolean findFlag = false;
        Results.ProductOrderReleasedListInqResult releaseList = (Results.ProductOrderReleasedListInqResult) lotScheduleTestCase.getReleasedLotList(productID.getValue(), subLotType).getBody();
        List<Infos.ProdReqListAttribute> reqListAttributes = releaseList.getProductReqListAttributePage().getContent();
        for (Infos.ProdReqListAttribute reqListAttribute : reqListAttributes) {
            if (CimObjectUtils.equalsWithValue(reqListAttribute.getLotID(),newCreateLotID)){
                findFlag = true;
                break;
            }
        }
        Validations.assertCheck(findFlag,"test fail");

        //【step9】sps create cancel the new create lot
        findFlag = false;
        Params.NewProdOrderCancelReqParams cancelReqParams = new Params.NewProdOrderCancelReqParams();
        cancelReqParams.setLotIDs(Arrays.asList(newCreateLotID));
        lotScheduleTestCase.newProdOrderCancelReq(cancelReqParams);
        releaseList = (Results.ProductOrderReleasedListInqResult) lotScheduleTestCase.getReleasedLotList(productID.getValue(), subLotType).getBody();
        reqListAttributes = releaseList.getProductReqListAttributePage().getContent();
        for (Infos.ProdReqListAttribute reqListAttribute : reqListAttributes) {
            if (CimObjectUtils.equalsWithValue(reqListAttribute.getLotID(),newCreateLotID)){
                findFlag = true;
                break;
            }
        }
        Validations.assertCheck(!findFlag,"test fail");
    }

    public void Lot_Change_For_Pre_LotStart() {
        //【step1】create a product 0.01 lot pre stb
        final String productID = "PRODUCT0.01";
        ObjectIdentifier newCreateLotID = this.Lot_Creation_By_Volume_Schedule_Daily(productID);

        CimProductSpecificationDO productSpecification = cimJpaRepository.queryOne("SELECT ID FROM OMPRODINFO WHERE PROD_ID = ?1", CimProductSpecificationDO.class, productID);
        String lotGenerationType = productSpecification.getLotGenerationType();
        String layer = productSpecification.getManufacturingLayer();

        //【step7】lot change new lot update
        CimProductRequestDO productRequestDO = cimJpaRepository.queryOne("SELECT * FROM OMPRORDER WHERE LOT_PLAN_ID = ?1", CimProductRequestDO.class, newCreateLotID.getValue());
        ObjectIdentifier routeID = ObjectIdentifier.build(productRequestDO.getMainProcessDefinitionID(),productRequestDO.getMainProcessDefinitionObj());
        String lotStatus = "PreLotStart";
        String selectProcessFlowID = "12REFIN.01";
        String selectProduct = "DEV-12REFAA001.01";
        this.lotSchdlChangeByParameter(routeID,newCreateLotID,lotStatus,ObjectIdentifier.buildWithValue(productID),lotGenerationType,layer,true,true,selectProcessFlowID,selectProduct);

        //【step8】check change
        List lotSelectionListByLotStatus = this.getLotSelectionListByLotStatus(lotStatus,ObjectIdentifier.buildWithValue(selectProduct),layer);
        List<TestInfos.PreLotStartInfos> lotSelectionListByLotStatus1 = new ArrayList<>();
        if (lotSelectionListByLotStatus.get(0) instanceof TestInfos.PreLotStartInfos){
            lotSelectionListByLotStatus1 = (List<TestInfos.PreLotStartInfos>) lotSelectionListByLotStatus;
        }
        TestInfos.PreLotStartInfos lotStartInfos = lotSelectionListByLotStatus1.stream().filter(preLotStartInfos -> CimStringUtils.equals(preLotStartInfos.getProdReqID(), newCreateLotID.getValue())).findFirst().get();
        String newProcessFlowID = lotStartInfos.getMainPDID();
        productRequestDO = cimJpaRepository.queryOne("SELECT * FROM OMPRORDER WHERE LOT_PLAN_ID = ?1", CimProductRequestDO.class, newCreateLotID.getValue());
        String newProductID = productRequestDO.getProductSpecificationID();
        Validations.assertCheck(CimStringUtils.equals(selectProcessFlowID,newProcessFlowID) && CimStringUtils.equals(newProductID,selectProduct),"test fail");
    }

    private void lotSchdlChangeByParameter(ObjectIdentifier routeID, ObjectIdentifier lotID, String lotStatus, ObjectIdentifier productID, String lotGenerationType, String manufacturingLayer, Boolean selectProductIDFlag, Boolean selectProcessFlowFlag, String selectProcessFlowID, String selectProduct) {
        //【step1】check lot status and get parameter
        List lotSelectionListByLotStatus = this.getLotSelectionListByLotStatus(lotStatus,productID,manufacturingLayer);
        List<TestInfos.WIPInfos> lotSelectionListByLotStatus1 = new ArrayList<>();
        List<TestInfos.BankInInfos> lotSelectionListByLotStatus2 = new ArrayList<>();
        List<TestInfos.PreLotStartInfos> lotSelectionListByLotStatus3 = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(lotSelectionListByLotStatus)){
            if (lotSelectionListByLotStatus.get(0) instanceof TestInfos.WIPInfos){
                lotSelectionListByLotStatus1 = (List<TestInfos.WIPInfos>)lotSelectionListByLotStatus;
            }else if (lotSelectionListByLotStatus.get(0) instanceof TestInfos.BankInInfos){
                lotSelectionListByLotStatus2 = (List<TestInfos.BankInInfos>) lotSelectionListByLotStatus;
            }else if (lotSelectionListByLotStatus.get(0) instanceof TestInfos.PreLotStartInfos){
                lotSelectionListByLotStatus3 = (List<TestInfos.PreLotStartInfos>) lotSelectionListByLotStatus;
            }
        }

        //【step2】get lotSelectionListByLotStatus and select sbt lot
        Boolean findFlag = false;
        String selectLotID = null;
        String shedulingMode = null;
        Integer priorityClass = null;
        String customerID = null;
        String lotOwnerID = null;
        String subLotType = null;
        for (TestInfos.WIPInfos wipInfos : lotSelectionListByLotStatus1) {
            if (CimStringUtils.equals(lotID.getValue(),wipInfos.getLotID())){
                findFlag = true;
                selectLotID = wipInfos.getLotID();
                shedulingMode = wipInfos.getScheduleMode();
                priorityClass = wipInfos.getPriorityClass();
                customerID = wipInfos.getCustomerID();
                lotOwnerID = wipInfos.getLotOwnerID();
                subLotType = wipInfos.getSubLotType();
                break;
            }
        }
        for (TestInfos.BankInInfos bankInInfos : lotSelectionListByLotStatus2) {
            if (CimStringUtils.equals(lotID.getValue(),bankInInfos.getLotID())){
                findFlag = true;
                selectLotID = bankInInfos.getLotID();
                shedulingMode = bankInInfos.getScheduleMode();
                priorityClass = bankInInfos.getPriorityClass();
                customerID = bankInInfos.getCustomerID();
                lotOwnerID = bankInInfos.getLotOwnerID();
                subLotType = bankInInfos.getSubLotType();
                break;
            }
        }
        for (TestInfos.PreLotStartInfos preLotStartInfos : lotSelectionListByLotStatus3) {
            if (CimStringUtils.equals(lotID.getValue(),preLotStartInfos.getProdReqID())){
                findFlag = true;
                selectLotID = preLotStartInfos.getProdReqID();
                shedulingMode = preLotStartInfos.getScheduleMode();
                priorityClass = preLotStartInfos.getPriorityClass();
                customerID = preLotStartInfos.getCustomerID();
                lotOwnerID = preLotStartInfos.getLotOwnerID();
                subLotType = preLotStartInfos.getSubLotType();
                break;
            }
        }
        Validations.assertCheck(findFlag,"test fail");

        //【step3】get schedule change param
        ObjectIdentifier selectProductID = productID;
        if (CimBooleanUtils.isTrue(selectProductIDFlag)){
            selectProductID  = this.selectProductID(manufacturingLayer,selectProcessFlowID,selectProduct);
        }
        Map<String,String> selectProcessFlow = null;
        if (CimBooleanUtils.isTrue(selectProcessFlowFlag) && selectProductID != null){
            selectProcessFlow = this.getProcessFlowID(selectProductID.getValue());
        }

        //【step4】schedule change request
        Params.NewProdOrderModifyReqParams params = new Params.NewProdOrderModifyReqParams();
        List<Infos.UpdateLotAttributes> updateLotAttributesList = new ArrayList<>();
        Infos.UpdateLotAttributes updateLotAttributes = new Infos.UpdateLotAttributes();
        updateLotAttributesList.add(updateLotAttributes);

        updateLotAttributes.setLotID(ObjectIdentifier.buildWithValue(selectLotID));
        updateLotAttributes.setProductID(selectProductID);
        updateLotAttributes.setCustomerCode(customerID);
        updateLotAttributes.setManufacturingOrderNumber("");
        updateLotAttributes.setLotOwner("dream");//change owner id
        updateLotAttributes.setSubLotType(subLotType);
        updateLotAttributes.setProductQuantity(25);

        if (null == selectProcessFlow || selectProcessFlow.isEmpty()){
            updateLotAttributes.setRouteID(routeID);
        }else {
            for (String key : selectProcessFlow.keySet()) {
                updateLotAttributes.setRouteID(new ObjectIdentifier(selectProcessFlow.get(key)));
            }
        }
        updateLotAttributes.setLotGenerationType(lotGenerationType);
        updateLotAttributes.setSchedulingMode(shedulingMode);
        updateLotAttributes.setProductDefinitionMode("By Product Quantity");
        updateLotAttributes.setPriorityClass(priorityClass.toString());
        updateLotAttributes.setExternalPriority("199999");
        updateLotAttributes.setLotComment("1234");//change comment
        updateLotAttributes.setDirectedSourceLotList(new ArrayList<>());

        //【step4-1】change time
        AtomicReference<String> startTime = new AtomicReference<>();
        AtomicReference<String> finishTime = new AtomicReference<>();
        List<Infos.ReleaseLotSchedule> releaseLotScheduleList = this.getReleaseLotScheduleListByDailyForProduct0_01(startTime,finishTime);

        updateLotAttributes.setPlannedStartTime(startTime.get());
        updateLotAttributes.setPlannedFinishTime(finishTime.get());
        List<Infos.ReleaseLotSchedule> updateLotSchedule = new ArrayList<>();
        releaseLotScheduleList.forEach(x -> {
            Infos.ReleaseLotSchedule changedLotSchedule = new Infos.ReleaseLotSchedule();
            changedLotSchedule.setEquipmentID(x.getEquipmentID());
            changedLotSchedule.setOperationNumber(x.getOperationNumber());
            changedLotSchedule.setPlannedStartTime(x.getPlannedStartTime());
            changedLotSchedule.setPlannedFinishTime(x.getPlannedFinishTime());
            changedLotSchedule.setReserve(null);
            updateLotSchedule.add(changedLotSchedule);
        });
        updateLotAttributes.setUpdateLotScheduleList(updateLotSchedule);

        //【step5】lot schedule change request
        params.setUpdateLotAttributesList(updateLotAttributesList);
        lotScheduleTestCase.newProdOrderModifyReq(params);
    }

    public void Lot_Change_For_Bank_In() {
        //【step1】stb and skip to 5000.0100
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String skipOperationNum = "5000.0100";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOperationNum);

        //【step2】bank in the product lot
        bankTestCase.bankInCase(Arrays.asList(lotID));

        //【step3】step3 get sps change bank in
        String lotStatus = "BankIn";
        List lotSelectionListByLotStatus = this.getLotSelectionListByLotStatus(lotStatus,ObjectIdentifier.buildWithValue(productID),"A");
        List<TestInfos.BankInInfos> lotSelectionListByLotStatus1 = new ArrayList<>();
        if (lotSelectionListByLotStatus.get(0) instanceof TestInfos.BankInInfos){
            lotSelectionListByLotStatus1 = (List<TestInfos.BankInInfos>) lotSelectionListByLotStatus;
        }
        TestInfos.BankInInfos lotStartInfos = lotSelectionListByLotStatus1.stream().filter(bankInInfos -> CimStringUtils.equals(bankInInfos.getLotID(), lotID.getValue())).findFirst().get();

        //【step4】lot change
        Params.ProdOrderChangeReqParams params = new Params.ProdOrderChangeReqParams();
        List<Infos.ChangedLotAttributes> changedLotAttributesList = new ArrayList<>();
        params.setStrChangedLotAttributes(changedLotAttributesList);
        Infos.ChangedLotAttributes changedLotAttributes = new Infos.ChangedLotAttributes();

        changedLotAttributes.setLotID(lotID);
        String selectCustomerCode = "IBM";
        String selectOwner = "dream";
        String selectComment = "123123";
        String selectSubLotType = "Special";
        changedLotAttributes.setCustomerCode(selectCustomerCode);
        changedLotAttributes.setManufacturingOrderNumber("");
        changedLotAttributes.setLotOwner(ObjectIdentifier.buildWithValue(selectOwner));
        changedLotAttributes.setLotComment(selectComment);
        changedLotAttributes.setSubLotType(selectSubLotType);

        changedLotAttributesList.add(changedLotAttributes);
        lotScheduleTestCase.prodOrderChangeReq(params);
        CimLotDO cimLotDO = cimJpaRepository.queryOne("SELECT * FROM FRLOT WHERE LOT_ID = ?1", CimLotDO.class, lotID.getValue());
        CimLotCommentDO cimLotCommentDO = cimJpaRepository.queryOne("SELECT * FROM FRLOTCOMMENT WHERE LOT_ID = ?1", CimLotCommentDO.class, lotID.getValue());

        String newCustomerCode = cimLotDO.getCustomerID();
        String newOwner = cimLotDO.getLotOwnerID();
        String newComment = cimLotCommentDO.getNoteContents();
        String newSubLotType = cimLotDO.getSubLotType();
        Boolean checkFlag = false;
        if (CimStringUtils.equals(newCustomerCode,selectCustomerCode)
            && CimStringUtils.equals(newOwner,selectOwner)
            && CimStringUtils.equals(newComment,selectComment)
            && CimStringUtils.equals(newSubLotType,selectSubLotType)){
            checkFlag = true;
        }
        Validations.assertCheck(checkFlag,"test fail");
    }
}
