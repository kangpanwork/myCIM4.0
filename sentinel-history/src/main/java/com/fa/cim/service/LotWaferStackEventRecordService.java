package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.utils.BaseUtils.*;
import static com.fa.cim.utils.StringUtils.variableStrCmp;
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
 * @date 2019/7/25 11:29
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotWaferStackEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private WaferSortJobHistoryService waferSortJobHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferStackEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/25 15:02
     */
    public Response createLotWaferStackEventRecord(Infos.LotWaferStackEventRecord lotWaferStackEventRecord , List<Infos.UserDataSet> userDataSets ) {
        Response iRc = returnOK();
        log.info("HistoryWatchDogServer::CreateLotWaferStackEventRecord Function" );
        iRc = createFHOPEHS_TxWaferBondReq( lotWaferStackEventRecord, userDataSets );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateLotWaferStackEventRecord Function" );
            return ( iRc );
        }
        iRc = createFHWLTHS_TxWaferBondReq( lotWaferStackEventRecord, userDataSets );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateLotWaferStackEventRecord Function" );
            return ( iRc );
        }

        log.info("HistoryWatchDogServer::CreateLotWaferStackEventRecord Function" );
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferStackEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/25 15:03
     */
    public Response createFHOPEHS_TxWaferBondReq(
            Infos.LotWaferStackEventRecord lotWaferStackEventRecord,
            List<Infos.UserDataSet>          userDataSets ) {
        Infos.Ohopehs        fhopehs;
        Infos.Frpd           pdData;
        Infos.Frlot          lotData;
        Infos.Frpos          Data;
        Params.String castCategory = new Params.String();
        Params.String productGrpID = new Params.String();
        Params.String prodType = new Params.String();
        Params.String techID = new Params.String();
        Params.String custprodID = new Params.String();
        Params.String stageGrpID = new Params.String();
        Params.String equipmentName = new Params.String();
        Params.String areaID = new Params.String();
        Params.String locationID = new Params.String();
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
        lotData=new Infos.Frlot();
        iRc = tableMethod.getFRLOT( lotWaferStackEventRecord.getLotData().getLotID(), lotData );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
            return iRc;
        }
        iRc = tableMethod.getFRCAST( lotWaferStackEventRecord.getLotData().getCassetteID(), castCategory );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
            return iRc;
        }
        pdData=new Infos.Frpd();
        iRc = tableMethod.getFRPD( lotWaferStackEventRecord.getLotData().getOperationID(), pdData );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
            return iRc;
        }
        iRc = tableMethod.getFRPRODSPEC( lotWaferStackEventRecord.getLotData().getProductID(), productGrpID, prodType );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
            return iRc;
        }
        iRc = tableMethod.getFRPRODGRP( lotWaferStackEventRecord.getLotData().getProductID(), techID );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
            return iRc;
        }
        iRc = tableMethod.getFRCUSTPROD( lotWaferStackEventRecord.getLotData().getLotID(),
                lotWaferStackEventRecord.getLotData().getProductID(), custprodID );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
            return iRc;
        }
        Data=new Infos.Frpos();
        iRc = tableMethod.getFRPOS( lotWaferStackEventRecord.getLotData().getObjrefPOS(),
                lotWaferStackEventRecord.getLotData().getOperationNumber(),
                lotWaferStackEventRecord.getLotData().getObjrefMainPF(), Data );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
            return iRc;
        }
        Data.setOperationNO(   lotWaferStackEventRecord.getLotData().getOperationNumber() );
        Data.setPdID(          lotWaferStackEventRecord.getLotData().getOperationID()     );
        iRc = tableMethod.getFRSTAGE( Data.getStageID(), stageGrpID );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
            return iRc;
        }
        iRc = tableMethod.getFREQP( lotWaferStackEventRecord.getEquipmentID(), areaID, equipmentName );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
            return iRc;
        }
        iRc = tableMethod.getFRAREA( areaID, locationID );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
            return iRc;
        }
        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id(             lotWaferStackEventRecord.getLotData().getLotID() );
        fhopehs.setLot_type(           lotWaferStackEventRecord.getLotData().getLotType() );
        fhopehs.setSub_lot_type(       lotData.getSubLotType() );
        fhopehs.setCast_id(            lotWaferStackEventRecord.getLotData().getCassetteID() );
        fhopehs.setCast_category(      castCategory.getValue() );
        fhopehs.setMainpd_id(          lotWaferStackEventRecord.getLotData().getRouteID() );
        fhopehs.setOpe_no(             lotWaferStackEventRecord.getLotData().getOperationNumber() );
        fhopehs.setPd_id(              lotWaferStackEventRecord.getLotData().getOperationID() );
        fhopehs.setOpe_pass_count    ( lotWaferStackEventRecord.getLotData().getOperationPassCount());
        fhopehs.setPd_name(            pdData.getOperationName() );
        fhopehs.setClaim_time(         lotWaferStackEventRecord.getEventCommon().getEventTimeStamp() );
        fhopehs.setClaim_shop_date   ( lotWaferStackEventRecord.getEventCommon().getEventShopDate());
        fhopehs.setClaim_user_id(      lotWaferStackEventRecord.getEventCommon().getUserID() );
        fhopehs.setMove_type(          SP_MOVEMENTTYPE_NONMOVE );
        if ( variableStrCmp( lotWaferStackEventRecord.getBondingCategory(), SP_LOT_BONDINGCATEGORY_BASE ) == 0 ) {
            fhopehs.setOpe_category(   SP_OPERATIONCATEGORY_WAFERSTACKINGBASE );
        }
        else if ( variableStrCmp( lotWaferStackEventRecord.getBondingCategory(), SP_LOT_BONDINGCATEGORY_BASECANCEL ) == 0 ) {
            fhopehs.setOpe_category(   SP_OPERATIONCATEGORY_WAFERSTACKINGBASECANCEL );
        }
        else if ( variableStrCmp( lotWaferStackEventRecord.getBondingCategory(), SP_LOT_BONDINGCATEGORY_TOP ) == 0 ) {
            fhopehs.setOpe_category(   SP_OPERATIONCATEGORY_WAFERSTACKINGTOP );
        }
        else if ( variableStrCmp( lotWaferStackEventRecord.getBondingCategory(), SP_LOT_BONDINGCATEGORY_TOPCANCEL ) == 0 ) {
            fhopehs.setOpe_category(   SP_OPERATIONCATEGORY_WAFERSTACKINGTOPCANCEL );
        }
        else
        {
            fhopehs.setOpe_category(   SP_OPERATIONCATEGORY_WAFERSTACKING );
        }
        fhopehs.setProd_type(          prodType.getValue() );
        fhopehs.setTest_type(          pdData.getTestType() );
        fhopehs.setMfg_layer(          lotData.getMfgLayer() );
        fhopehs.setExt_priority      ( lotData.getPriority());
        fhopehs.setPriority_class    ( lotData.getPriorityClass());
        fhopehs.setProdspec_id(        lotWaferStackEventRecord.getLotData().getProductID() );
        fhopehs.setProdgrp_id(         productGrpID.getValue()  );
        fhopehs.setTech_id(            techID.getValue() );
        fhopehs.setCustomer_id(        lotData.getCustomerID() );
        fhopehs.setCustprod_id(        custprodID.getValue() );
        fhopehs.setOrder_no(           lotData.getOrderNO() );
        fhopehs.setStage_id(           Data.getStageID() );
        fhopehs.setStagegrp_id(        stageGrpID.getValue() );
        fhopehs.setPhoto_layer(        Data.getPhotoLayer() );
        fhopehs.setLocation_id(        locationID.getValue() );
        fhopehs.setArea_id(            areaID.getValue() );
        fhopehs.setEqp_id(             lotWaferStackEventRecord.getEquipmentID() );
        fhopehs.setEqp_name(           equipmentName.getValue() );
        fhopehs.setOpe_mode(           "" );
        fhopehs.setLc_recipe_id(       "" );
        fhopehs.setRecipe_id(          "" );
        fhopehs.setPh_recipe_id(       "" );
        fhopehs.setReticle_count     ( 0);
        fhopehs.setFixture_count     ( 0);
        fhopehs.setRparm_count       ( 0);
        fhopehs.setInit_hold_flag    ( 0);
        fhopehs.setLast_hldrel_flag  ( 0);
        fhopehs.setHold_state(         lotWaferStackEventRecord.getLotData().getHoldState() );
        fhopehs.setHold_time(          SP_TIMESTAMP_NIL_OBJECT_STRING );
        fhopehs.setHold_shop_date    ( 0D);
        fhopehs.setHold_user_id(       "" );
        fhopehs.setHold_type(          "" );
        fhopehs.setHold_reason_code(   "" );
        fhopehs.setHold_reason_desc(   "" );
        fhopehs.setReason_code(        "" );
        fhopehs.setReason_description( "" );
        fhopehs.setBank_id(            lotWaferStackEventRecord.getLotData().getBankID() );
        fhopehs.setPrev_mainpd_id(     "" );
        fhopehs.setPrev_ope_no(        "" );
        fhopehs.setPrev_pd_id(         "" );
        fhopehs.setPrev_pass_count   ( 0);
        fhopehs.setPrev_pd_name(       "" );
        fhopehs.setPrev_photo_layer(   "" );
        fhopehs.setPrev_stage_id(      "" );
        fhopehs.setPrev_stagegrp_id(   "" );
        fhopehs.setFlowbatch_id(       "" );
        fhopehs.setCtrl_job(           lotWaferStackEventRecord.getControlJobID() );
        fhopehs.setRework_count      ( 0);
        fhopehs.setOrg_wafer_qty     ( lotWaferStackEventRecord.getLotData().getOriginalWaferQuantity());
        fhopehs.setCur_wafer_qty     ( lotWaferStackEventRecord.getLotData().getCurrentWaferQuantity());
        fhopehs.setProd_wafer_qty    ( lotWaferStackEventRecord.getLotData().getProductWaferQuantity());
        fhopehs.setCntl_wafer_qty    ( lotWaferStackEventRecord.getLotData().getControlWaferQuantity());
        fhopehs.setClaim_prod_qty    ( lotWaferStackEventRecord.getLotData().getProductWaferQuantity());
        fhopehs.setClaim_cntl_qty    ( lotWaferStackEventRecord.getLotData().getControlWaferQuantity());
        fhopehs.setTotal_good_unit   ( 0);
        fhopehs.setTotal_fail_unit   ( 0);
        fhopehs.setLot_owner_id(       lotData.getLotOwner() );
        fhopehs.setPlan_end_time(      lotData.getPlanEndTime() );
        fhopehs.setWfrhs_time(         lotWaferStackEventRecord.getLotData().getWaferHistoryTimeStamp() );
        fhopehs.setCriteria_flag     ( CRITERIA_NA);
        fhopehs.setClaim_memo(         lotWaferStackEventRecord.getEventCommon().getEventMemo() );
        fhopehs.setRparm_change_type(  "" );
        fhopehs.setEvent_create_time(  lotWaferStackEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhopehs.setHold_ope_no(        "" );
        fhopehs.setHold_reason_ope_no( "" );
        fhopehs.setOriginalFabID(      "" );
        fhopehs.setDestinationFabID(   "" );
        fhopehs.setRelatedLotID(       lotWaferStackEventRecord.getRelatedLotID() );
        fhopehs.setBondingGroupID(     lotWaferStackEventRecord.getBondingGroupID() );
        fhopehs.setPd_type(            pdData.getPd_type());
        fhopehs.setPrev_pd_type(       "");
        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq(): InsertLotOperationHistory SQL Error Occurred" );

            log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
            return iRc;
        }

        log.info("HistoryWatchDogServer::createFHOPEHS_TxWaferBondReq Function" );
        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferStackEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/25 15:08
     */
    public Response createFHWLTHS_TxWaferBondReq(Infos.LotWaferStackEventRecord lotWaferStackEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths      fhwlths;
        Infos.Frwafer      resultData;
        Infos.Frwafer      origWaferData;
        Params.String cur_cast_category = new Params.String();
        Params.String prv_cast_category = new Params.String();
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::createFHWLTHS_TxWaferBondReq Function" );
        int wfrLen = length(lotWaferStackEventRecord.getWafers());
        for ( int wfrCnt = 0; wfrCnt < wfrLen; wfrCnt++ ) {
            iRc = tableMethod.getFRCAST( lotWaferStackEventRecord.getWafers().get(wfrCnt).getDestinationCassetteID(), cur_cast_category );
            if ( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_TxWaferBondReq Function" );
                return iRc;
            }
            iRc = tableMethod.getFRCAST( lotWaferStackEventRecord.getWafers().get(wfrCnt).getOriginalCassetteID(), prv_cast_category );
            if ( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_TxWaferBondReq Function" );
                return iRc;
            }
            resultData=new Infos.Frwafer();
            iRc = tableMethod.getFRWAFER( lotWaferStackEventRecord.getWafers().get(wfrCnt).getWaferID(), resultData );
            if ( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_TxWaferBondReq Function" );
                return iRc;
            }
            origWaferData=new Infos.Frwafer();
            iRc = tableMethod.getFRWAFER( lotWaferStackEventRecord.getWafers().get(wfrCnt).getOriginalWaferID(), origWaferData );
            if ( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_TxWaferBondReq Function" );
                return iRc;
            }
            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id(             lotWaferStackEventRecord.getWafers().get(wfrCnt).getWaferID() );
            fhwlths.setCur_lot_id(           lotWaferStackEventRecord.getLotData().getLotID() );
            fhwlths.setCur_cast_id(          lotWaferStackEventRecord.getWafers().get(wfrCnt).getDestinationCassetteID() );
            fhwlths.setCur_cast_category(    cur_cast_category.getValue() );
            fhwlths.setCur_cast_slot_no    ( convertI(lotWaferStackEventRecord.getWafers().get(wfrCnt).getDestinationSlotNumber()));
            fhwlths.setClaim_user_id(        lotWaferStackEventRecord.getEventCommon().getUserID() );
            fhwlths.setClaim_time(           lotWaferStackEventRecord.getEventCommon().getEventTimeStamp() );
            fhwlths.setClaim_shop_date     ( lotWaferStackEventRecord.getEventCommon().getEventShopDate());
            if ( variableStrCmp( lotWaferStackEventRecord.getBondingCategory(), SP_LOT_BONDINGCATEGORY_BASE ) == 0 ) {
                fhwlths.setOpe_category(     SP_OPERATIONCATEGORY_WAFERSTACKINGBASE );
                fhwlths.setExist_flag(       "Y" );
            }
            else if ( variableStrCmp( lotWaferStackEventRecord.getBondingCategory(), SP_LOT_BONDINGCATEGORY_BASECANCEL ) == 0 ) {
                fhwlths.setOpe_category(     SP_OPERATIONCATEGORY_WAFERSTACKINGBASECANCEL );
                fhwlths.setExist_flag(       "Y" );
            }
            else if ( variableStrCmp( lotWaferStackEventRecord.getBondingCategory(), SP_LOT_BONDINGCATEGORY_TOP ) == 0 ) {
                fhwlths.setOpe_category(     SP_OPERATIONCATEGORY_WAFERSTACKINGTOP );
                fhwlths.setExist_flag(       "N" );
            }
            else if ( variableStrCmp( lotWaferStackEventRecord.getBondingCategory(), SP_LOT_BONDINGCATEGORY_TOPCANCEL ) == 0 ) {
                fhwlths.setOpe_category(     SP_OPERATIONCATEGORY_WAFERSTACKINGTOPCANCEL );
                fhwlths.setExist_flag(       "Y" );
            }
            else
            {
                fhwlths.setOpe_category(     SP_OPERATIONCATEGORY_WAFERSTACKING );
                fhwlths.setExist_flag(       "Y" );
            }
            fhwlths.setApply_wafer_flag(     "Y" );
            fhwlths.setProdspec_id(          lotWaferStackEventRecord.getLotData().getProductID() );
            fhwlths.setGood_unit_count     ( resultData.getGoodDiceQty());
            fhwlths.setRepair_unit_count   ( resultData.getRepairedDiceQty());
            fhwlths.setFail_unit_count     ( resultData.getBadDiceQty());
            if (Objects.equals(lotWaferStackEventRecord.getWafers().get(wfrCnt).getControlWaferFlag(), TRUE)) {
                fhwlths.setControl_wafer      ( true);
            }
            else
            {
                fhwlths.setControl_wafer      ( false);
            }
            fhwlths.setPrev_cast_slot_no      ( 0);
            fhwlths.setPrev_lot_id(          lotWaferStackEventRecord.getLotData().getLotID() );
            fhwlths.setPrev_cast_id(         lotWaferStackEventRecord.getWafers().get(wfrCnt).getOriginalCassetteID() );
            fhwlths.setPrev_cast_category(   prv_cast_category.getValue() );
            fhwlths.setPrev_cast_slot_no   ( convertI(lotWaferStackEventRecord.getWafers().get(wfrCnt).getOriginalSlotNumber()));
            fhwlths.setReason_code(          "" );
            fhwlths.setReason_description(   "" );
            fhwlths.setOrg_wafer_id(         lotWaferStackEventRecord.getWafers().get(wfrCnt).getOriginalWaferID() );
            fhwlths.setOrg_prodspec_id(      origWaferData.getProductID() );
            fhwlths.setAlias_wafer_name(     lotWaferStackEventRecord.getWafers().get(wfrCnt).getAliasWaferName() );
            fhwlths.setEvent_create_time(    lotWaferStackEventRecord.getEventCommon().getEventCreationTimeStamp() );
            fhwlths.setEquipmentID(          lotWaferStackEventRecord.getEquipmentID() );
            fhwlths.setSorterJobID(          "" );
            fhwlths.setComponentJobID(       "" );
            fhwlths.setOrg_alias_wafer_name( lotWaferStackEventRecord.getWafers().get(wfrCnt).getOriginalAliasWaferName() );
            fhwlths.setRelated_wafer_id(     lotWaferStackEventRecord.getWafers().get(wfrCnt).getRelatedWaferID() );
            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if ( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_TxWaferBondReq(): InsertLotWaferHistory SQL Error Occurred" );

                log.info("HistoryWatchDogServer::createFHWLTHS_TxWaferBondReq Function" );
                return iRc;
            }
        }

        log.info("HistoryWatchDogServer::createFHWLTHS_TxWaferBondReq Function" );

        return returnOK();
    }

}
