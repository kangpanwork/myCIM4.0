package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

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
 * @date 2019/5/31 16:14
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotHoldEventRecordService {

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
     * @param lotHoldEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/31 16:27
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createLotHoldEventRecord(Infos.LotHoldEventRecord lotHoldEventRecord, List<Infos.UserDataSet> userDataSets){
        Response iRc = returnOK();

        if( variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OBNKW009_ID ) == 0 ||
                variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OBNKW010_ID ) == 0 ) {
            iRc =  createFHOPEHS_TxHoldLotInBankReq(lotHoldEventRecord,userDataSets);
            if( !isOk(iRc) ) {
                return ( iRc );
            }
        }else{
            iRc =  createFHOPEHS_TxHoldLotReq(lotHoldEventRecord,userDataSets);
            if( !isOk(iRc) ) {
                return ( iRc );
            }
        }

        return ( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotHoldEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/31 16:48
     */
    private Response createFHOPEHS_TxHoldLotReq(Infos.LotHoldEventRecord lotHoldEventRecord, List<Infos.UserDataSet> userDataSets) {
        Infos.Frpd resultDataPd_prev;
        Infos.Frpd resultDataPd_cur;
        Infos.Frlot lotData;
        Infos.Frpos resultDataPos_prev;
        Infos.Frpos resultDataPos_cur;
        Infos.Ohopehs fhopehs;

        String custProdID;
        String prodGrpID;
        String prodType;
        String techID;
        Params.String stockerID=new Params.String();
        Params.String areaID=new Params.String();
        Params.String description=new Params.String();
        String castCategory = null;
        String prev_stageGrpID;
        String cur_stageGrpID;
        Params.String codeCategory;
        Params.String releaseCodeCategory=new Params.String();
        Timestamp shopDate;
        String locationID;
        int                 i;
        Response                 iRc = returnOK();
        String workTimeStamp;

        lotData=new Infos.Frlot();
        iRc = tableMethod.getFRLOT(      lotHoldEventRecord.getLotData().getLotID(),      lotData );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        prodGrpID=null;
        prodType=null;
        iRc = tableMethod.getFRPRODSPEC( lotHoldEventRecord.getLotData().getProductID(),  prodGrpID,prodType );
        prodGrpID=get(iRc,0);
        prodType=get(iRc,1);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        techID=null;
        iRc = tableMethod.getFRPRODGRP(  lotHoldEventRecord.getLotData().getProductID(),  techID );
        techID=get(iRc);
        if( !isOk(iRc)) {
            return( iRc );
        }
        iRc = tableMethod.getFRBANK(     lotHoldEventRecord.getLotData().getBankID(),     stockerID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRSTK(      stockerID,areaID,                       description );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCAST(     lotHoldEventRecord.getLotData().getCassetteID(), castCategory );
        castCategory=get(iRc);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        locationID=null;
        iRc = tableMethod.getFRAREA(     areaID.getValue(),                                 locationID);
        locationID=get(iRc);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        custProdID=null;
        iRc = tableMethod.getFRCUSTPROD( lotHoldEventRecord.getLotData().getLotID(),      lotHoldEventRecord.getLotData().getProductID(), custProdID );
        custProdID=get(iRc);
        if( !isOk(iRc) ) {
            return( iRc );
        }

        for( i=0; i < length(lotHoldEventRecord.getHoldRecords()); i++) {
            shopDate=new Timestamp(0);
            iRc = tableMethod.getFRCALENDAR(lotHoldEventRecord.getHoldRecords().get(i).getHoldTimeStamp(),shopDate);
            if( !isOk(iRc) ) {
                return( iRc );
            }

            resultDataPos_prev=new Infos.Frpos();
            iRc = tableMethod.getFRPOS( lotHoldEventRecord.getPreviousObjrefPOS(),
                    lotHoldEventRecord.getPreviousOperationNumber(),
                    lotHoldEventRecord.getPreviousObjrefMainPF(),
                    resultDataPos_prev );

            if( !isOk(iRc) ) {
                return( iRc );
            }
            resultDataPos_prev.setOperationNO   (lotHoldEventRecord.getPreviousOperationNumber()) ;
            resultDataPos_prev.setPdID          (lotHoldEventRecord.getPreviousOperationID    ()) ;

            resultDataPos_cur=new Infos.Frpos();
            iRc = tableMethod.getFRPOS( lotHoldEventRecord.getLotData().getObjrefPOS(),
                    lotHoldEventRecord.getLotData().getOperationNumber(),
                    lotHoldEventRecord.getLotData().getObjrefMainPF(),
                    resultDataPos_cur );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            resultDataPos_cur.setOperationNO   (lotHoldEventRecord.getLotData().getOperationNumber()) ;
            resultDataPos_cur.setPdID          (lotHoldEventRecord.getLotData().getOperationID    ()) ;

            resultDataPd_prev=new Infos.Frpd();
            iRc = tableMethod.getFRPD( lotHoldEventRecord.getPreviousOperationID(),resultDataPd_prev );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            Params.Param<String> prev_stageGrpIDParam=new Params.Param();
            iRc = tableMethod.getFRSTAGE( resultDataPos_prev.getStageID(),prev_stageGrpIDParam);
            prev_stageGrpID=get(prev_stageGrpIDParam);
            if( !isOk(iRc) ) {
                return( iRc );
            }

            resultDataPd_cur=new Infos.Frpd();
            iRc = tableMethod.getFRPD( lotHoldEventRecord.getLotData().getOperationID(), resultDataPd_cur );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            Params.Param cur_stageGrpIDParam=new Params.Param();
            iRc = tableMethod.getFRSTAGE( resultDataPos_cur.getStageID(),cur_stageGrpIDParam );
            cur_stageGrpID=get(cur_stageGrpIDParam);
            if( !isOk(iRc) )
            {
                return( iRc );
            }
            codeCategory=new Params.String();
            iRc = tableMethod.getFRCODE(  lotHoldEventRecord.getHoldRecords().get(i).getHoldReasonCodeID(),codeCategory);
            if( !isOk(iRc) ) {
                return( iRc );
            }
            if( variableStrCmp ( lotHoldEventRecord.getEventCommon().getTransactionID (), OLOTW001_ID ) != 0 &&
                    variableStrCmp ( lotHoldEventRecord.getEventCommon().getTransactionID (), OEQPW020_ID ) != 0 ) {
                releaseCodeCategory=new Params.String();
                iRc = tableMethod.getFRCODE(  lotHoldEventRecord.getReleaseReasonCodeID (), releaseCodeCategory );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
            fhopehs=new Infos.Ohopehs();
            fhopehs.setLot_id(lotHoldEventRecord.getLotData().getLotID   ());
            fhopehs.setLot_type(lotHoldEventRecord.getLotData().getLotType ());
            fhopehs.setSub_lot_type(lotData.getSubLotType() );
            fhopehs.setCast_id(lotHoldEventRecord.getLotData().getCassetteID ());
            fhopehs.setCast_category(castCategory );

            if( (Objects.equals(lotHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationExistFlag(), FALSE) &&
                    Objects.equals(lotHoldEventRecord.getHoldRecords().get(i).getMovementFlag(), TRUE)) ||
                    (Objects.equals(lotHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationExistFlag(), TRUE) &&
                            Objects.equals(lotHoldEventRecord.getHoldRecords().get(i).getMovementFlag(), FALSE)) ) {
                fhopehs.setMainpd_id(lotHoldEventRecord.getPreviousRouteID ());
                fhopehs.setOpe_no(lotHoldEventRecord.getPreviousOperationNumber ());
                fhopehs.setPd_id (lotHoldEventRecord.getPreviousOperationID ());
                fhopehs.setOpe_pass_count     (lotHoldEventRecord.getPreviousOperationPassCount ()==null?null:
                        lotHoldEventRecord.getPreviousOperationPassCount ().intValue());
                fhopehs.setPd_name(resultDataPd_prev.getOperationName());
                fhopehs.setPd_type(resultDataPd_prev.getPd_type());
            } else {
                fhopehs.setMainpd_id(lotHoldEventRecord.getLotData().getRouteID ());
                fhopehs.setOpe_no (lotHoldEventRecord.getLotData().getOperationNumber ());
                fhopehs.setPd_id(lotHoldEventRecord.getLotData().getOperationID ());
                fhopehs.setOpe_pass_count     (lotHoldEventRecord.getLotData().getOperationPassCount());
                fhopehs.setPd_name(resultDataPd_cur.getOperationName());
                fhopehs.setPd_type(resultDataPd_cur.getPd_type());
            }

            fhopehs.setClaim_time (lotHoldEventRecord.getEventCommon().getEventTimeStamp ());
            fhopehs.setClaim_shop_date    (lotHoldEventRecord.getEventCommon().getEventShopDate   ());
            fhopehs.setClaim_user_id(lotHoldEventRecord.getEventCommon().getUserID ());

            if(variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OLOTW001_ID )==0 ||
                    variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OEQPW020_ID )==0) {
                if( !isTrue(lotHoldEventRecord.getHoldRecords().get(i).getMovementFlag ())) {
                    fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
                } else {
                    if( variableStrCmp( resultDataPos_prev.getStageID(), resultDataPos_cur.getStageID())!= 0 ) {
                        fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE );
                    } else {
                        fhopehs.setMove_type(SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION );
                    }
                }
            } else if(variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OLOTW002_ID )==0) {
                if( !isTrue(lotHoldEventRecord.getHoldRecords().get(i).getMovementFlag ()) ) {
                    fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
                } else {
                    if( variableStrCmp( resultDataPos_prev.getStageID(), resultDataPos_cur.getStageID())!= 0 ) {
                        fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEFORWARDSTAGE );
                    } else {
                        fhopehs.setMove_type(SP_MOVEMENTTYPE_MOVEFORWARDOPERATION );
                    }
                }
            } else {
                fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
            }

            if( variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (),OLOTW001_ID ) == 0 )
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_LOTHOLD );
            }
            else if(variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OEQPW020_ID )==0)
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_RUNNINGHOLD );
            }
            else if(variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OEQPW014_ID )==0)
            {
                continue;
            }
            else
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_LOTHOLDRELEASE );
            }

            fhopehs.setProd_type(prodType );
            fhopehs.setMfg_layer(lotData.getMfgLayer ());
            fhopehs.setExt_priority      (lotData.getPriority());
            fhopehs.setPriority_class    (lotData.getPriorityClass());
            fhopehs.setProdspec_id (lotHoldEventRecord.getLotData().getProductID ());
            fhopehs.setProdgrp_id (prodGrpID );
            fhopehs.setTech_id(techID );
            fhopehs.setCustomer_id(lotData.getCustomerID ());
            fhopehs.setCustprod_id(custProdID );
            fhopehs.setOrder_no(lotData.getOrderNO ());

            if( (!isTrue(lotHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationExistFlag ())&&
                    isTrue(lotHoldEventRecord.getHoldRecords().get(i).getMovementFlag ())) ||
                    (isTrue(lotHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationExistFlag ())&&
                            !isTrue(lotHoldEventRecord.getHoldRecords().get(i).getMovementFlag ())) )  {
                fhopehs.setTest_type (resultDataPd_prev.getTestType ());
                fhopehs.setStage_id(resultDataPos_prev.getStageID ());
                fhopehs.setPhoto_layer(resultDataPos_prev.getPhotoLayer ());
                fhopehs.setStagegrp_id(prev_stageGrpID );
            } else {
                fhopehs.setTest_type (resultDataPd_cur.getTestType ());
                fhopehs.setStage_id(resultDataPos_cur.getStageID ());
                fhopehs.setPhoto_layer(resultDataPos_cur.getPhotoLayer ());
                fhopehs.setStagegrp_id(cur_stageGrpID );
            }
            fhopehs.setReticle_count      (0);
            fhopehs.setFixture_count      (0);
            fhopehs.setRparm_count        (0);

            if( (variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OLOTW001_ID    )==0 ) ||
                    (variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OEQPW020_ID    )==0 ) ) {
                fhopehs.setInit_hold_flag     (convert(lotHoldEventRecord.getHoldRecords().get(i).getChangeStateFlag()));
                fhopehs.setLast_hldrel_flag   (0);
            }else{
                fhopehs.setInit_hold_flag     (0);
                fhopehs.setLast_hldrel_flag   (convert(lotHoldEventRecord.getHoldRecords().get(i).getChangeStateFlag()));
            }

            fhopehs.setHold_state(lotHoldEventRecord.getLotData().getHoldState ());
            fhopehs.setHold_type(lotHoldEventRecord.getHoldRecords().get(i).getHoldType ());
            fhopehs.setHold_reason_code(lotHoldEventRecord.getHoldRecords().get(i).getHoldReasonCodeID().getIdentifier ());
            fhopehs.setHold_reason_desc(codeCategory.getValue());

            if( (variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OLOTW001_ID    )==0 ) ||
                    (variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OEQPW020_ID    )==0 ) ) {
                workTimeStamp="";
                workTimeStamp = lotHoldEventRecord.getEventCommon().getEventTimeStamp();
                fhopehs.setHold_time(workTimeStamp);
                fhopehs.setHold_shop_date     (lotHoldEventRecord.getEventCommon().getEventShopDate());
                fhopehs.setHold_user_id (lotHoldEventRecord.getHoldRecords().get(i).getHoldUserID());
                fhopehs.setReason_code(lotHoldEventRecord.getHoldRecords().get(i).getHoldReasonCodeID().getIdentifier());
                fhopehs.setReason_description (codeCategory.getValue());
                fhopehs.setClaim_memo (lotHoldEventRecord.getHoldRecords().get(i).getHoldClaimMemo());
            }else{
                workTimeStamp = lotHoldEventRecord.getHoldRecords().get(i).getHoldTimeStamp();
                fhopehs.setHold_time(workTimeStamp);
                fhopehs.setHold_shop_date   (convertD(shopDate.getTime()));
                fhopehs.setHold_user_id (lotHoldEventRecord.getHoldRecords().get(i).getHoldUserID());
                fhopehs.setReason_code(lotHoldEventRecord.getReleaseReasonCodeID().getIdentifier());
                fhopehs.setReason_description (releaseCodeCategory.getValue());
                fhopehs.setClaim_memo (lotHoldEventRecord.getEventCommon().getEventMemo());
            }

            if( (isTrue(lotHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationExistFlag ())&&
                    !isTrue(lotHoldEventRecord.getHoldRecords().get(i).getMovementFlag ())) ||
                    (isTrue(lotHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationExistFlag ())&&
                            isTrue(lotHoldEventRecord.getHoldRecords().get(i).getMovementFlag ())) ) {
                fhopehs.setPrev_mainpd_id(lotHoldEventRecord.getPreviousRouteID ());
                fhopehs.setPrev_ope_no (lotHoldEventRecord.getPreviousOperationNumber ());
                fhopehs.setPrev_pd_id  (lotHoldEventRecord.getPreviousOperationID ());
                fhopehs.setPrev_pass_count   (lotHoldEventRecord.getPreviousOperationPassCount()==null?
                        null:lotHoldEventRecord.getPreviousOperationPassCount().intValue());
                fhopehs.setPrev_pd_name(resultDataPd_prev.getOperationName ());
                fhopehs.setPrev_photo_layer(resultDataPos_prev.getPhotoLayer ());
                fhopehs.setPrev_stage_id(resultDataPos_prev.getStageID ());
                fhopehs.setPrev_stagegrp_id (prev_stageGrpID);
                fhopehs.setPrev_pd_type(resultDataPd_prev.getPd_type());
            } else {
                fhopehs.setPrev_mainpd_id (lotHoldEventRecord.getLotData().getRouteID ());
                fhopehs.setPrev_ope_no  (lotHoldEventRecord.getLotData().getOperationNumber ());
                fhopehs.setPrev_pd_id (lotHoldEventRecord.getLotData().getOperationID ());
                fhopehs.setPrev_pass_count   (lotHoldEventRecord.getLotData().getOperationPassCount());
                fhopehs.setPrev_pd_name(resultDataPd_cur.getOperationName ());
                fhopehs.setPrev_photo_layer(resultDataPos_cur.getPhotoLayer ());
                fhopehs.setPrev_stage_id (resultDataPos_cur.getStageID ());
                fhopehs.setPrev_stagegrp_id (cur_stageGrpID);
                fhopehs.setPrev_pd_type(resultDataPd_cur.getPd_type());
            }
            fhopehs.setRework_count       (0);
            fhopehs.setOrg_wafer_qty      (lotHoldEventRecord.getLotData().getOriginalWaferQuantity());
            fhopehs.setCur_wafer_qty      (lotHoldEventRecord.getLotData().getCurrentWaferQuantity());
            fhopehs.setProd_wafer_qty     (lotHoldEventRecord.getLotData().getProductWaferQuantity());
            fhopehs.setCntl_wafer_qty     (lotHoldEventRecord.getLotData().getControlWaferQuantity());
            fhopehs.setClaim_prod_qty     (lotHoldEventRecord.getLotData().getProductWaferQuantity());
            fhopehs.setClaim_cntl_qty     (lotHoldEventRecord.getLotData().getControlWaferQuantity());
            fhopehs.setTotal_good_unit    (0);
            fhopehs.setTotal_fail_unit    (0);
            fhopehs.setLot_owner_id(lotData.getLotOwner());
            fhopehs.setPlan_end_time(lotData.getPlanEndTime());
            fhopehs.setWfrhs_time (lotHoldEventRecord.getLotData().getWaferHistoryTimeStamp());

            fhopehs.setCriteria_flag      (CRITERIA_NA);
            fhopehs.setEvent_create_time(lotHoldEventRecord.getEventCommon().getEventCreationTimeStamp ());

            fhopehs.setHold_ope_no(lotHoldEventRecord.getLotData().getOperationNumber ());
            if( isTrue(lotHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationFlag ())||
                    0 == variableStrCmp(lotHoldEventRecord.getReleaseReasonCodeID().getIdentifier(), HOLD_REASON_CODE_RUNNING_HOLD_RELEASE ) ) {
                fhopehs.setHold_reason_ope_no(lotHoldEventRecord.getPreviousOperationNumber ());
            }
            else
            {
                fhopehs.setHold_reason_ope_no(lotHoldEventRecord.getLotData().getOperationNumber ());
            }

            if (!CollectionUtils.isEmpty(lotHoldEventRecord.getHoldRecords()) &&
                    !org.springframework.util.StringUtils.isEmpty(lotHoldEventRecord.getHoldRecords().get(i).getDepartmentNamePlate())) {
                String departmentNamePlate = lotHoldEventRecord.getHoldRecords().get(i).getDepartmentNamePlate();
                fhopehs.setDpt_name_plate(departmentNamePlate);
            }

            iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );

            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        return ( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotHoldEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/31 16:46
     */
    private Response createFHOPEHS_TxHoldLotInBankReq(Infos.LotHoldEventRecord lotHoldEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Frpd resultDataPd_prev=new Infos.Frpd();
        Infos.Frpd resultDataPd_cur=new Infos.Frpd();
        Infos.Frlot lotData=new Infos.Frlot();
        Infos.Frpos resultDataPos_prev=new Infos.Frpos();
        Infos.Frpos resultDataPos_cur=new Infos.Frpos();
        Infos.Ohopehs fhopehs;

        Params.String custProdID=new Params.String();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String stockerID=new Params.String();
        Params.String areaID=new Params.String();
        Params.String description=new Params.String();
        Params.String castCategory=new Params.String();
        Params.String stageGrpID=new Params.String();
        Params.String codeCategory=new Params.String();
        Params.String releaseCodeCategory=new Params.String();
        Timestamp              shopDate=new Timestamp(0);
        Params.String locationID=new Params.String();
        int                 i;
        Response                 iRc = returnOK();
        String workTimeStamp=null;

        iRc = tableMethod.getFRLOT(      lotHoldEventRecord.getLotData().getLotID(),      lotData );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( lotHoldEventRecord.getLotData().getProductID(),  prodGrpID,prodType );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP(  lotHoldEventRecord.getLotData().getProductID(),  techID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRBANK(     lotHoldEventRecord.getLotData().getBankID(),     stockerID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRSTK(      stockerID,areaID,                       description );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCAST(     lotHoldEventRecord.getLotData().getCassetteID(), castCategory );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRAREA(     areaID,                                 locationID);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotHoldEventRecord.getLotData().getLotID(),      lotHoldEventRecord.getLotData().getProductID(),
                custProdID );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        for( i=0; i < length(lotHoldEventRecord.getHoldRecords()) ; i++) {
            iRc = tableMethod.getFRCALENDAR(lotHoldEventRecord.getHoldRecords().get(i).getHoldTimeStamp(),shopDate);
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPOS( lotHoldEventRecord.getPreviousObjrefPOS(),
                    lotHoldEventRecord.getPreviousOperationNumber(),
                    lotHoldEventRecord.getPreviousObjrefMainPF(),
                    resultDataPos_prev );

            if( !isOk(iRc) ) {
                return( iRc );
            }
            resultDataPos_prev.setOperationNO   (lotHoldEventRecord.getPreviousOperationNumber()) ;
            resultDataPos_prev.setPdID          (lotHoldEventRecord.getPreviousOperationID    ()) ;

            iRc = tableMethod.getFRPOS( lotHoldEventRecord.getLotData().getObjrefPOS(),
                    lotHoldEventRecord.getLotData().getOperationNumber(),
                    lotHoldEventRecord.getLotData().getObjrefMainPF(),
                    resultDataPos_cur );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            resultDataPos_cur.setOperationNO   (lotHoldEventRecord.getLotData().getOperationNumber()) ;
            resultDataPos_cur.setPdID          (lotHoldEventRecord.getLotData().getOperationID    ()) ;

            if ( isTrue(lotHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationFlag ()) ) {
                iRc = tableMethod.getFRPD( lotHoldEventRecord.getPreviousOperationID(),resultDataPd_prev );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
                iRc = tableMethod.getFRSTAGE( resultDataPos_prev.getStageID(),stageGrpID);
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }else{

                iRc = tableMethod.getFRPD( lotHoldEventRecord.getLotData().getOperationID(), resultDataPd_cur );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
                iRc = tableMethod.getFRSTAGE( resultDataPos_cur.getStageID(),stageGrpID );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
            iRc = tableMethod.getFRCODE(  lotHoldEventRecord.getHoldRecords().get(i).getHoldReasonCodeID(),codeCategory);
            if( !isOk(iRc) ) {
                return( iRc );
            }
            if( variableStrCmp ( lotHoldEventRecord.getEventCommon().getTransactionID (), OBNKW009_ID ) != 0 ) {
                iRc = tableMethod.getFRCODE(  lotHoldEventRecord.getReleaseReasonCodeID (), releaseCodeCategory );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }

            fhopehs=new Infos.Ohopehs();
            fhopehs.setLot_id(lotHoldEventRecord.getLotData().getLotID   ());
            fhopehs.setLot_type(lotHoldEventRecord.getLotData().getLotType ());
            fhopehs.setSub_lot_type(lotData.getSubLotType ());
            fhopehs.setCast_id(lotHoldEventRecord.getLotData().getCassetteID ());
            fhopehs.setCast_category(castCategory.getValue() );
            fhopehs.setOpe_pass_count     (0);
            fhopehs.setClaim_time (lotHoldEventRecord.getEventCommon().getEventTimeStamp ());
            fhopehs.setClaim_shop_date    (lotHoldEventRecord.getEventCommon().getEventShopDate   ());
            fhopehs.setClaim_user_id(lotHoldEventRecord.getEventCommon().getUserID ());

            fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE);

            if( variableStrCmp( lotHoldEventRecord.getEventCommon().getTransactionID (), OBNKW009_ID ) == 0 )
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_BANKHOLD );
            }else{
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_BANKHOLDRELEASE);
            }

            fhopehs.setProd_type(prodType.getValue() );
            fhopehs.setMfg_layer(lotData.getMfgLayer ());
            fhopehs.setExt_priority      (lotData.getPriority());
            fhopehs.setPriority_class    (lotData.getPriorityClass());
            fhopehs.setProdspec_id (lotHoldEventRecord.getLotData().getProductID ());
            fhopehs.setProdgrp_id (prodGrpID.getValue() );
            fhopehs.setTech_id(techID.getValue() );
            fhopehs.setCustomer_id(lotData.getCustomerID ());
            fhopehs.setCustprod_id(custProdID.getValue() );
            fhopehs.setOrder_no(lotData.getOrderNO ());
            fhopehs.setLocation_id(locationID.getValue());
            fhopehs.setArea_id (areaID.getValue());
            fhopehs.setReticle_count      (0);
            fhopehs.setFixture_count      (0);
            fhopehs.setRparm_count        (0);

            if( variableStrCmp(lotHoldEventRecord .getEventCommon().getTransactionID (), OBNKW009_ID ) == 0 ) {
                fhopehs.setInit_hold_flag     (1);
                fhopehs.setLast_hldrel_flag   (0);
            }else{
                fhopehs.setInit_hold_flag     (0);
                fhopehs.setLast_hldrel_flag   (1);
            }

            fhopehs.setHold_state(lotHoldEventRecord.getLotData().getHoldState ());
            fhopehs.setHold_type(lotHoldEventRecord.getHoldRecords().get(i).getHoldType ());
            fhopehs.setHold_reason_code(lotHoldEventRecord.getHoldRecords().get(i).getHoldReasonCodeID().getIdentifier ());
            fhopehs.setHold_reason_desc(codeCategory.getValue());

            if( variableStrCmp(lotHoldEventRecord.getEventCommon().getTransactionID (), OBNKW009_ID ) == 0  ) {

                workTimeStamp = lotHoldEventRecord.getEventCommon().getEventTimeStamp();
                fhopehs.setHold_time(workTimeStamp);
                fhopehs.setHold_shop_date     (lotHoldEventRecord.getEventCommon().getEventShopDate());
                fhopehs.setHold_user_id (lotHoldEventRecord.getHoldRecords().get(i).getHoldUserID());
                fhopehs.setReason_code(lotHoldEventRecord.getHoldRecords().get(i).getHoldReasonCodeID().getIdentifier ());
                fhopehs.setReason_description (codeCategory.getValue());
            }else{

                workTimeStamp = lotHoldEventRecord.getHoldRecords().get(i).getHoldTimeStamp();
                fhopehs.setHold_time(workTimeStamp);
                fhopehs.setHold_shop_date   (convertD(shopDate.getTime()));
                fhopehs.setHold_user_id (lotHoldEventRecord.getHoldRecords().get(i).getHoldUserID());
                fhopehs.setReason_code(lotHoldEventRecord.getReleaseReasonCodeID().getIdentifier());
                fhopehs.setReason_description (releaseCodeCategory.getValue());
            }

            fhopehs.setBank_id (lotHoldEventRecord.getLotData().getBankID());
            fhopehs.setPrev_pass_count    (0);
            fhopehs.setRework_count       (0);
            fhopehs.setOrg_wafer_qty      (lotHoldEventRecord.getLotData().getOriginalWaferQuantity());
            fhopehs.setCur_wafer_qty      (lotHoldEventRecord.getLotData().getCurrentWaferQuantity());
            fhopehs.setProd_wafer_qty     (lotHoldEventRecord.getLotData().getProductWaferQuantity());
            fhopehs.setCntl_wafer_qty     (lotHoldEventRecord.getLotData().getControlWaferQuantity());
            fhopehs.setClaim_prod_qty     (lotHoldEventRecord.getLotData().getProductWaferQuantity());
            fhopehs.setClaim_cntl_qty     (lotHoldEventRecord.getLotData().getControlWaferQuantity());
            fhopehs.setTotal_good_unit    (0);
            fhopehs.setTotal_fail_unit    (0);
            fhopehs.setLot_owner_id(lotData.getLotOwner());
            fhopehs.setPlan_end_time(lotData.getPlanEndTime());
            fhopehs.setWfrhs_time (lotHoldEventRecord.getLotData().getWaferHistoryTimeStamp());
            fhopehs.setClaim_memo (lotHoldEventRecord.getEventCommon().getEventMemo());

            fhopehs.setCriteria_flag      (CRITERIA_NA);
            fhopehs.setEvent_create_time(lotHoldEventRecord.getEventCommon().getEventCreationTimeStamp ());

            iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );

            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        return ( returnOK() );
    }

}
