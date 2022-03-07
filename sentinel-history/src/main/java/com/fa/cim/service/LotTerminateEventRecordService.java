package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.Constant.TransactionConstant.OLOTW038_ID;
import static com.fa.cim.Constant.TransactionConstant.OLOTW039_ID;
import static com.fa.cim.utils.BaseUtils.isOk;
import static com.fa.cim.utils.BaseUtils.returnOK;

/**
 * description:
 * CimLotTerminateEvent
 * change history:
 * date         defect#         person      comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 21-7-10       ********        Grant       create file
 *
 * @author: Grant
 * @date: 21-7-10 15:13
 * @copyright: 2021, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotTerminateEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private LotTerminateHistoryService lotTerminateHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    public Response createEventRecord(Infos.LotTerminateEventRecord eventRecord) {
        if (eventRecord == null)
            return returnOK();

        Response res;
        if (OLOTW038_ID.equals(eventRecord.getEventCommon().getTransactionID())
                || OLOTW039_ID.equals(eventRecord.getEventCommon().getTransactionID())) {
            res = createOhopehsTxTerminate(eventRecord);
            if (!isOk(res)) {
                return res;
            }
            res = createFhscrhs(eventRecord);
            if (!isOk(res)) {
                return res;
            }
        }

        return returnOK();
    }

    private Response createOhopehsTxTerminate(Infos.LotTerminateEventRecord eventRecord) {
        Infos.Ohopehs ohopehs;
        Infos.Frpd resultDataPd;
        Infos.Frlot resultDataLot;
        Infos.Frpos resultDataPos = new Infos.Frpos();
        Params.String castCategory = new Params.String();
        Params.String productGrpID = new Params.String();
        Params.String prodType = new Params.String();
        Params.String techID = new Params.String();
        Params.String custprodID = new Params.String();
        Params.String stageGrpID = new Params.String();
        Params.Param<Long> productWaferQuantity = new Params.Param<>(0L);
        Params.Param<Long> controlWaferQuantity = new Params.Param<>(0L);
        resultDataLot = new Infos.Frlot();
        Response res;

        res = tableMethod.getFRLOT(eventRecord.getLotData().getLotID(), resultDataLot);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRCAST(eventRecord.getLotData().getCassetteID(), castCategory);
        if (!isOk(res)) {
            return res;
        }
        resultDataPd = new Infos.Frpd();
        res = tableMethod.getFRPD(eventRecord.getLotData().getOperationID(), resultDataPd);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRPRODSPEC(eventRecord.getLotData().getProductID(), productGrpID, prodType);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRPRODGRP(eventRecord.getLotData().getProductID(), techID);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRCUSTPROD(eventRecord.getLotData().getLotID(),
                eventRecord.getLotData().getProductID(), custprodID);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRPOS(eventRecord.getLotData().getObjrefPOS(),
                eventRecord.getLotData().getOperationNumber(),
                eventRecord.getLotData().getObjrefMainPF(),
                resultDataPos);
        if (!isOk(res)) {
            return res;
        }
        resultDataPos.setOperationNO(eventRecord.getLotData().getOperationNumber());
        resultDataPos.setPdID(eventRecord.getLotData().getOperationID());
        res = tableMethod.getFRSTAGE(resultDataPos.getStageID(), stageGrpID);
        if (!isOk(res)) {
            return res;
        }
        ohopehs = new Infos.Ohopehs();
        ohopehs.setLot_id(eventRecord.getLotData().getLotID());
        ohopehs.setLot_type(eventRecord.getLotData().getLotType());
        ohopehs.setSub_lot_type(resultDataLot.getSubLotType());
        ohopehs.setCast_id(eventRecord.getLotData().getCassetteID());
        ohopehs.setCast_category(castCategory.getValue());
        ohopehs.setMainpd_id(eventRecord.getLotData().getRouteID());
        ohopehs.setOpe_no(eventRecord.getLotData().getOperationNumber());
        ohopehs.setPd_id(eventRecord.getLotData().getOperationID());
        ohopehs.setOpe_pass_count(eventRecord.getLotData().getOperationPassCount());
        ohopehs.setPd_name(resultDataPd.getOperationName());
        ohopehs.setClaim_time(eventRecord.getEventCommon().getEventTimeStamp());
        ohopehs.setClaim_shop_date(eventRecord.getEventCommon().getEventShopDate());
        ohopehs.setClaim_user_id(eventRecord.getEventCommon().getUserID());
        ohopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE);
        if (OLOTW038_ID.equals(eventRecord.getEventCommon().getTransactionID())) {
            ohopehs.setOpe_category(SP_OPERATIONCATEGORY_LOTTERMINATE);
        } else {
            ohopehs.setOpe_category(SP_OPERATIONCATEGORY_LOTTERMINATECANCEL);
        }
        ohopehs.setProd_type(prodType.getValue());
        ohopehs.setTest_type(resultDataPd.getTestType());
        ohopehs.setMfg_layer(resultDataLot.getMfgLayer());
        ohopehs.setExt_priority(resultDataLot.getPriority());
        ohopehs.setPriority_class(resultDataLot.getPriorityClass());
        ohopehs.setProdspec_id(eventRecord.getLotData().getProductID());
        ohopehs.setProdgrp_id(productGrpID.getValue());
        ohopehs.setTech_id(techID.getValue());
        ohopehs.setCustomer_id(resultDataLot.getCustomerID());
        ohopehs.setCustprod_id(custprodID.getValue());
        ohopehs.setOrder_no(resultDataLot.getOrderNO());
        ohopehs.setStage_id(resultDataPos.getStageID());
        ohopehs.setStagegrp_id(stageGrpID.getValue());
        ohopehs.setPhoto_layer(resultDataPos.getPhotoLayer());
        ohopehs.setReticle_count(0);
        ohopehs.setFixture_count(0);
        ohopehs.setRparm_count(0);
        ohopehs.setInit_hold_flag(0);
        ohopehs.setLast_hldrel_flag(0);
        ohopehs.setHold_state(eventRecord.getLotData().getHoldState());
        ohopehs.setHold_time("1901-01-01-00.00.00.000000");
        ohopehs.setBank_id(eventRecord.getLotData().getBankID());
        ohopehs.setPrev_pass_count(0);
        ohopehs.setRework_count(0);
        ohopehs.setOrg_wafer_qty(eventRecord.getLotData().getOriginalWaferQuantity());
        ohopehs.setCur_wafer_qty(eventRecord.getLotData().getCurrentWaferQuantity());
        ohopehs.setProd_wafer_qty(eventRecord.getLotData().getProductWaferQuantity());
        ohopehs.setCntl_wafer_qty(eventRecord.getLotData().getControlWaferQuantity());
        ohopehs.setClaim_prod_qty(productWaferQuantity.getValue() == null ? null :
                productWaferQuantity.getValue().intValue());
        ohopehs.setClaim_cntl_qty(controlWaferQuantity.getValue() == null ? null :
                controlWaferQuantity.getValue().intValue());
        ohopehs.setTotal_good_unit(0);
        ohopehs.setTotal_fail_unit(0);
        ohopehs.setLot_owner_id(resultDataLot.getLotOwner());
        ohopehs.setPlan_end_time(resultDataLot.getPlanEndTime());
        ohopehs.setWfrhs_time(eventRecord.getLotData().getWaferHistoryTimeStamp());
        ohopehs.setClaim_memo(eventRecord.getEventCommon().getEventMemo());
        ohopehs.setCriteria_flag(CRITERIA_NA);
        ohopehs.setEvent_create_time(eventRecord.getEventCommon().getEventCreationTimeStamp());
        ohopehs.setPd_type(resultDataPd.getPd_type());
        res = lotOperationHistoryService.insertLotOperationHistory(ohopehs);
        if (!isOk(res)) {
            return res;
        }
        return returnOK();
    }

    private Response createFhscrhs(Infos.LotTerminateEventRecord eventRecord) {
        Infos.Ohtrmhs fhtrmhs;
        Infos.Ohtrmhs fhtrmhs2 = new Infos.Ohtrmhs();
        Infos.Frlot lotData;
        Infos.Frpd pdData;
        Infos.Frpos data;
        Params.String codeDescription = new Params.String();
        Params.String prodGrpID = new Params.String();
        Params.String prodType = new Params.String();
        Params.String techID = new Params.String();
        Params.String custProdID = new Params.String();
        Params.String eqpID = new Params.String();
        Params.String eqpName = new Params.String();
        Params.String areaID = new Params.String();
        Params.String stageGrpID = new Params.String();
        Params.String stageGrpID_reason = new Params.String();
        Params.String locationID = new Params.String();
        Params.String castCategory = new Params.String();
        Response res;

        lotData = new Infos.Frlot();
        res = tableMethod.getFRLOT(eventRecord.getLotData().getLotID(), lotData);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRPRODSPEC(eventRecord.getLotData().getProductID(), prodGrpID, prodType);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRPRODGRP(eventRecord.getLotData().getProductID(), techID);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRCAST(eventRecord.getLotData().getCassetteID(), castCategory);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRCUSTPROD(eventRecord.getLotData().getLotID(), eventRecord.getLotData().getProductID(),
                custProdID);
        if (!isOk(res)) {
            return res;
        }
        pdData = new Infos.Frpd();
        res = tableMethod.getFRPD(eventRecord.getLotData().getOperationID(), pdData);
        if (!isOk(res)) {
            return res;
        }
        data = new Infos.Frpos();
        res = tableMethod.getFRPOS(eventRecord.getLotData().getObjrefPOS(),
                eventRecord.getLotData().getOperationNumber(),
                eventRecord.getLotData().getObjrefMainPF(),
                data);
        if (!isOk(res)) {
            return res;
        }

        data.setOperationNO(eventRecord.getLotData().getOperationNumber());
        data.setPdID(eventRecord.getLotData().getOperationID());
        res = tableMethod.getFREQP(eqpID.getValue(), areaID, eqpName);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRAREA(areaID, locationID);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRSTAGE(data.getStageID(), stageGrpID);
        if (!isOk(res)) {
            return res;
        }
        res = tableMethod.getFRCODE(eventRecord.getReasonCode(), codeDescription);
        if (!isOk(res)) {
            return res;
        }
        fhtrmhs = new Infos.Ohtrmhs();
        fhtrmhs.setLotId(eventRecord.getLotData().getLotID());
        fhtrmhs.setReasonCode(eventRecord.getReasonCode().getIdentifier());
        fhtrmhs.setReasonDescription(codeDescription.getValue());
        fhtrmhs.setProdspecId(eventRecord.getLotData().getProductID());
        fhtrmhs.setLotOwnerId(lotData.getLotOwner());
        fhtrmhs.setProdgrpId(prodGrpID.getValue());
        fhtrmhs.setTechId(techID.getValue());
        fhtrmhs.setCustprodId(custProdID.getValue());
        fhtrmhs.setOrderNo(lotData.getOrderNO());
        fhtrmhs.setCustomerId(lotData.getCustomerID());
        fhtrmhs.setControlWafer(0);
        fhtrmhs.setLotType(eventRecord.getLotData().getLotType());
        fhtrmhs.setCastId(eventRecord.getLotData().getCassetteID());
        fhtrmhs.setCastCategory(castCategory.getValue());
        fhtrmhs.setProdType(prodType.getValue());
        fhtrmhs.setClaimMainpdId(eventRecord.getLotData().getRouteID());
        fhtrmhs.setClaimOpeNo(eventRecord.getLotData().getOperationNumber());
        fhtrmhs.setClaimPdId(eventRecord.getLotData().getOperationID());

        fhtrmhs.setClaimPassCount(eventRecord.getLotData().getOperationPassCount());
        fhtrmhs.setClaimOpeName(pdData.getOperationName());
        fhtrmhs.setClaimTestType(pdData.getTestType());
        fhtrmhs.setClaimTime(eventRecord.getEventCommon().getEventTimeStamp());

        fhtrmhs.setClaimShopDate(eventRecord.getEventCommon().getEventShopDate());
        fhtrmhs.setClaimUserId(eventRecord.getEventCommon().getUserID());
        fhtrmhs.setClaimStageId(data.getStageID());
        fhtrmhs.setClaimStagegrpId(stageGrpID.getValue());
        fhtrmhs.setClaimPhotoLayer(data.getPhotoLayer());
        fhtrmhs.setClaimDepartment(pdData.getDepartment());
        fhtrmhs.setClaimBankId(eventRecord.getLotData().getBankID());
        if (OLOTW038_ID.equals(eventRecord.getEventCommon().getTransactionID())) {
            fhtrmhs.setReasonLotId(eventRecord.getLotData().getLotID());
            fhtrmhs.setReasonStagegrpId(stageGrpID_reason.getValue());
            fhtrmhs.setReasonLocationId(locationID.getValue());
            fhtrmhs.setReasonAreaId(areaID.getValue());
            fhtrmhs.setReasonEqpId(eqpID.getValue());
            fhtrmhs.setReasonEqpName(eqpName.getValue());
            fhtrmhs.setTerminateType("LT");
        } else {
            fhtrmhs.setReasonLotId(fhtrmhs2.getReasonLotId());
            fhtrmhs.setReasonMainpdId(fhtrmhs2.getReasonMainpdId());
            fhtrmhs.setReasonOpeNo(fhtrmhs2.getReasonOpeNo());
            fhtrmhs.setReasonPdId(fhtrmhs2.getReasonPdId());
            fhtrmhs.setReasonPassCount(fhtrmhs2.getReasonPassCount());
            fhtrmhs.setReasonOpeName(fhtrmhs2.getReasonOpeName());
            fhtrmhs.setReasonTestType(fhtrmhs2.getReasonTestType());
            fhtrmhs.setReasonStageId(fhtrmhs2.getReasonStageId());
            fhtrmhs.setReasonStagegrpId(fhtrmhs2.getReasonStagegrpId());
            fhtrmhs.setReasonPhotoLayer(fhtrmhs2.getReasonPhotoLayer());
            fhtrmhs.setReasonDepartment(fhtrmhs2.getReasonDepartment());
            fhtrmhs.setReasonLocationId(fhtrmhs2.getReasonLocationId());
            fhtrmhs.setReasonAreaId(fhtrmhs2.getReasonAreaId());
            fhtrmhs.setReasonEqpId(fhtrmhs2.getReasonEqpId());
            fhtrmhs.setReasonEqpName(fhtrmhs2.getReasonEqpName());
            fhtrmhs.setTerminateType("LC");
        }
        fhtrmhs.setClaimMemo(eventRecord.getEventCommon().getEventMemo());
        fhtrmhs.setEventCreateTime(eventRecord.getEventCommon().getEventCreationTimeStamp());
        res = lotTerminateHistoryService.insertLotTerminateHistory(fhtrmhs);
        if (!isOk(res)) {
            return res;
        }

        return returnOK();
    }

}
