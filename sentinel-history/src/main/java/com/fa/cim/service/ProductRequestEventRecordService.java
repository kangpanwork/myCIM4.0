package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.Constant.TransactionConstant.*;
import static com.fa.cim.utils.BaseUtils.*;
import static com.fa.cim.utils.StringUtils.variableStrCmp;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/6/24 15:12
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class ProductRequestEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private ProductRequestHistoryService productRequestHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param productRequestEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/24 15:17
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createProductRequestEventRecord(Infos.ProductRequestEventRecord productRequestEventRecord
            , List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohprqhs fhprqhs=new Infos.Ohprqhs();
        Infos.Frprodreq prodReqData=new Infos.Frprodreq();
        Infos.Frpd pdData=new Infos.Frpd();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String custProdID=new Params.String();
        Response                 iRc = returnOK();

        log.info("get others Data From FrameWork Database");

        iRc = tableMethod.getFRPRODSPEC( productRequestEventRecord.getProductID(), prodGrpID, prodType );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( productRequestEventRecord.getProductID(), techID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD2( productRequestEventRecord.getCustomerID(),
                productRequestEventRecord.getProductID(),
                custProdID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODREQ( productRequestEventRecord.getLotID(), prodReqData );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        if( (variableStrCmp( productRequestEventRecord.getEventCommon().getTransactionID(),OPLNW001_ID) != 0) ||
                (variableStrCmp( productRequestEventRecord.getEventCommon().getTransactionID(),OPLNW002_ID) != 0) ||
                (variableStrCmp( productRequestEventRecord.getEventCommon().getTransactionID(),OPLNW003_ID) != 0)   ) {
            iRc = tableMethod.getFRPD( productRequestEventRecord.getRouteID(), pdData );
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        log.info("createProductRequestHistory");

        fhprqhs=new Infos.Ohprqhs();

        fhprqhs.setLot_id(productRequestEventRecord.getLotID ());
        fhprqhs.setLot_type(productRequestEventRecord.getLotType());
        fhprqhs.setSub_lot_type(productRequestEventRecord.getSubLotType());
        fhprqhs.setMainpd_id(productRequestEventRecord.getRouteID());
        fhprqhs.setClaim_time(productRequestEventRecord.getEventCommon().getEventTimeStamp());
        fhprqhs.setClaim_shop_date (productRequestEventRecord.getEventCommon().getEventShopDate());
        fhprqhs.setClaim_user_id(productRequestEventRecord.getEventCommon().getUserID());

        if( variableStrCmp( productRequestEventRecord.getEventCommon().getTransactionID (), OPLNW001_ID) == 0 ) {
            fhprqhs.setOpe_category(SP_OPERATIONCATEGORY_RELEASE);
        }
        else if( variableStrCmp( productRequestEventRecord.getEventCommon().getTransactionID (), OPLNW003_ID) == 0 ) {
            fhprqhs.setOpe_category(SP_OPERATIONCATEGORY_UPDATE);
        }else{
            fhprqhs.setOpe_category(SP_OPERATIONCATEGORY_RELEASECANCEL);
        }

        fhprqhs.setProd_type(prodType.getValue());
        fhprqhs.setProd_qty        (productRequestEventRecord.getProductQuantity()==null?
                null:productRequestEventRecord.getProductQuantity().intValue());
        fhprqhs.setMfg_layer(prodReqData.getMfgLayer());
        fhprqhs.setPlan_start_time(productRequestEventRecord.getPlanStartTime());
        fhprqhs.setPlan_end_time(productRequestEventRecord.getPlanCompTime());
        fhprqhs.setLot_gen_type(productRequestEventRecord.getLotGenerationType());
        fhprqhs.setLot_sch_mode(productRequestEventRecord.getLotScheduleMode());
        fhprqhs.setLot_gen_mode(productRequestEventRecord.getLotIDGenerationMode());
        fhprqhs.setProd_def_mode(productRequestEventRecord.getProductDefinitionMode());
        fhprqhs.setExt_priority    (productRequestEventRecord.getExternalPriority()==null?
                null:productRequestEventRecord.getExternalPriority().intValue());
        fhprqhs.setPriority_class  (productRequestEventRecord.getPriorityClass()==null?
                null:productRequestEventRecord.getPriorityClass().intValue());
        fhprqhs.setProdspec_id(productRequestEventRecord.getProductID());
        fhprqhs.setLot_owner_id(productRequestEventRecord.getLotOwnerID());
        fhprqhs.setProdgrp_id(prodGrpID.getValue());
        fhprqhs.setTech_id(techID.getValue());
        fhprqhs.setCustprod_id(custProdID.getValue() );
        fhprqhs.setOrder_no(productRequestEventRecord.getOrderNumber());
        fhprqhs.setCustomer_id(productRequestEventRecord.getCustomerID());

        if( ( variableStrCmp( productRequestEventRecord.getEventCommon().getTransactionID (), OPLNW001_ID) == 0 )
                || ( variableStrCmp( productRequestEventRecord.getEventCommon().getTransactionID (), OPLNW003_ID) == 0 ) ) {
            fhprqhs.setStart_bank_id(prodReqData.getStartBankID());
        }
        else{
            fhprqhs.setStart_bank_id(pdData.getStartBankID());
        }

        fhprqhs.setLot_comment(productRequestEventRecord.getLotComment());
        fhprqhs.setClaim_memo(productRequestEventRecord.getEventCommon().getEventMemo());
        fhprqhs.setEvent_create_time(productRequestEventRecord.getEventCommon().getEventCreationTimeStamp ());

        log.info("Insert ProductRequestHistory (OHPRORDER)");

        iRc = productRequestHistoryService.insertProductRequestHistory(fhprqhs);
        if( !isOk(iRc) ) {
            log.error("HistoryWatchDogServer::CreateProductRequestEventRecord(): InsertHistory SQL Error");
            return( iRc );
        }

        return(returnOK());

    }

}
