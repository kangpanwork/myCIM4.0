package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.Constant.TransactionConstant.*;
import static com.fa.cim.utils.BaseUtils.isOk;
import static com.fa.cim.utils.BaseUtils.returnOK;
import static com.fa.cim.utils.StringUtils.variableStrCmp;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/6/25 16:31
 */
@Repository
//@Transactional(rollbackFor = Exception.class)
public class LotChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private LotHoldHistoryService lotHoldHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/25 16:35
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createLotChangeEventRecord(Infos.LotChangeEventRecord lotChangeEventRecord,
                                               List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs=new Infos.Ohopehs();
        Infos.Frlot lotData=new Infos.Frlot();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String custProdID=new Params.String();
        Params.String subLotType=new Params.String();
        Params.String lotType=new Params.String();
        Response                 iRc = returnOK();

        iRc = tableMethod.getFRLOT( lotChangeEventRecord.getLotID(), lotData );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        if( variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(),OPLNW004_ID) == 0 ) {
            subLotType.setValue(lotData.getSubLotType ());
            lotType.setValue(lotData.getLotType ());
            lotData.setSubLotType(subLotType .getValue());
            lotData.setLotType(lotType .getValue());
            lotData.setCustomerID(lotChangeEventRecord.getCustomerID ());
            if(variableStrCmp( lotData.getCustomerID(), "") != 0 ) {
                iRc = tableMethod.getFRCUSTPROD2( lotChangeEventRecord.getCustomerID(),
                        lotChangeEventRecord.getProductID(),
                        custProdID );
                if( !isOk(iRc) ) {
                    return( iRc );
                }

            }
        }else{
            iRc = tableMethod.getFRPRODSPEC( lotChangeEventRecord.getProductID(), prodGrpID, prodType );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPRODGRP(  lotChangeEventRecord.getProductID(), techID );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            if(variableStrCmp( lotData.getCustomerID(), "") != 0 ) {
                iRc = tableMethod.getFRCUSTPROD(  lotChangeEventRecord.getLotID(),
                        lotChangeEventRecord.getProductID(),
                        custProdID );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        fhopehs.setLot_id(lotChangeEventRecord.getLotID());
        fhopehs.setOpe_pass_count (0);
        fhopehs.setClaim_time(lotChangeEventRecord.getEventCommon().getEventTimeStamp ());
        fhopehs.setClaim_shop_date (lotChangeEventRecord.getEventCommon().getEventShopDate());
        fhopehs.setClaim_user_id(lotChangeEventRecord.getEventCommon().getUserID ());
        fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );

        fhopehs.setSub_lot_type(lotData.getSubLotType ());
        fhopehs.setLot_type(lotData.getLotType ());

        if( variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(),OPLNW004_ID) == 0 ) {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_ORDERCHANGE );
            fhopehs.setExt_priority (lotChangeEventRecord.getExternalPriority()==null?
                    null:lotChangeEventRecord.getExternalPriority().intValue());
            fhopehs.setCustomer_id(lotChangeEventRecord.getCustomerID ());
        }else if( variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(),OPLNW007_ID) == 0 ) {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_REQUEUE );
            fhopehs.setProd_type(prodType.getValue() );
            fhopehs.setExt_priority (0);
            fhopehs.setPriority_class (lotChangeEventRecord.getPriorityClass()==null?
                    null:lotChangeEventRecord.getPriorityClass().intValue());
            fhopehs.setProdspec_id(lotChangeEventRecord.getProductID ());
            fhopehs.setProdgrp_id(prodGrpID.getValue());
            fhopehs.setTech_id(techID.getValue() );
            fhopehs.setCustomer_id(lotData.getCustomerID ());
        }else{
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_SCHEDULECHANGE );
            fhopehs.setProd_type(prodType.getValue() );
            fhopehs.setExt_priority (0);
            fhopehs.setPriority_class (lotChangeEventRecord.getPriorityClass()==null?
                    null:lotChangeEventRecord.getPriorityClass().intValue());
            fhopehs.setProdspec_id(lotChangeEventRecord.getProductID ());
            fhopehs.setProdgrp_id(prodGrpID.getValue());
            fhopehs.setTech_id(techID .getValue());
            fhopehs.setCustomer_id(lotData.getCustomerID ());
        }
        if(variableStrCmp( fhopehs.getCustomer_id(), "" ) != 0 ) {
            fhopehs.setCustprod_id(custProdID .getValue());
        }
        if( variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(),OPLNW004_ID) == 0 )
        {
            fhopehs.setOrder_no(lotChangeEventRecord.getOrderNumber ());
        }
        fhopehs.setReticle_count      (0);
        fhopehs.setFixture_count      (0);
        fhopehs.setRparm_count        (0);
        fhopehs.setInit_hold_flag     (0);
        fhopehs.setLast_hldrel_flag   (0);
        fhopehs.setHold_time("1901-01-01-00.00.00.000000" ) ;
        fhopehs.setPrev_pass_count    (0);
        fhopehs.setRework_count       (0);
        fhopehs.setOrg_wafer_qty      (0);
        fhopehs.setCur_wafer_qty      (0);
        fhopehs.setProd_wafer_qty     (0);
        fhopehs.setCntl_wafer_qty     (0);
        fhopehs.setClaim_prod_qty     (0);
        fhopehs.setClaim_cntl_qty     (0);
        fhopehs.setTotal_good_unit    (0);
        fhopehs.setTotal_fail_unit    (0);
        if( variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(),OPLNW004_ID) == 0 )
        {
            fhopehs.setLot_owner_id(lotChangeEventRecord.getLotOwnerID ());
            fhopehs.setPlan_end_time("1901-01-01-00.00.00.000000" ) ;
        }else{
            fhopehs.setLot_owner_id(lotData.getLotOwner ());
            fhopehs.setPlan_end_time(lotChangeEventRecord.getPlanCompTime ()) ;
            if( variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(),OPLNW006_ID) == 0
                    || variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(),OPLNW007_ID) == 0
                    || variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(),OPLNW012_ID) == 0)
            {
                if( variableStrCmp( lotChangeEventRecord.getPlanCompTime(), "" ) ==0
                        || lotChangeEventRecord.getPlanCompTime ()== null) {
                    fhopehs.setPlan_end_time("1901-01-01-00.00.00.000000" ) ;
                }
            }
        }
        fhopehs.setWfrhs_time("1901-01-01-00.00.00.000000" ) ;
        fhopehs.setClaim_memo(lotChangeEventRecord.getEventCommon().getEventMemo ());

        if( variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(),OPLNW006_ID) == 0
                || variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(),OPLNW007_ID) == 0
                || variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(),OPLNW012_ID) == 0)
        {
            fhopehs.setPrev_prodspec_id(lotChangeEventRecord.getPreviousProductID());
        }
        else
        {
            fhopehs.setPrev_prodspec_id("" );
        }

        fhopehs.setCriteria_flag      (CRITERIA_NA);

        if( variableStrCmp( lotChangeEventRecord.getEventCommon().getTransactionID(), OPLNW012_ID ) == 0) {
            fhopehs.setReason_code(SP_REASON_RSRV ) ;
        }

        fhopehs.setEvent_create_time(lotChangeEventRecord.getEventCommon().getEventCreationTimeStamp ());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        return(returnOK());
    }

}
