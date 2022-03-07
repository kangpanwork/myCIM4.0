package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fa.cim.Constant.ConfigConstant.HsWtDgMgWaferRparmDifFlag;
import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.Constant.TransactionConstant.*;
import static com.fa.cim.utils.BaseUtils.*;
import static com.fa.cim.utils.StringUtils.variableStrCmp;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/25 11:08:05
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotOperationEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationStartEventRecord
     * @param userDataSets
     * @return void
     * @author Ho
     * @date 2019/2/25 14:36:57
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createLotOperationStartEventRecord(Infos.LotOperationStartEventRecord lotOperationStartEventRecord, List<Infos.UserDataSet> userDataSets){
        Response response=new Response();
        response.setCode(0);

        Infos.EventData eventCommon = lotOperationStartEventRecord.getEventCommon();

        String transactionID = eventCommon.getTransactionID();

        if(( variableStrCmp(transactionID,OEQPW005_ID) == 0 ) ||
                ( variableStrCmp(transactionID,OEQPW004_ID) == 0 ) ||
                ( variableStrCmp(transactionID,OEQPW009_ID) == 0 ) ||
                ( variableStrCmp(transactionID,OEQPW010_ID) == 0 ) ||
                ( variableStrCmp(transactionID,OEQPW012_ID) == 0 ) ||
                ( variableStrCmp(transactionID,OEQPW024_ID) == 0 ) ) {
            Response iRc = createFHOPEHSTxMoveInReq(lotOperationStartEventRecord, userDataSets);
            if ( !isOk(iRc) ) {
                return iRc;
            }
        }

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationCompleteEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 17:09:07
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createLotOperationCompleteEventRecord (Infos.LotOperationCompleteEventRecord lotOperationCompleteEventRecord, List<Infos.UserDataSet> userDataSets) {
        Response iRc=new Response();
        iRc.setCode(0);
        if((variableStrCmp(lotOperationCompleteEventRecord.getEventCommon().getTransactionID(),OEQPW006_ID) == 0 ) ||
                (variableStrCmp(lotOperationCompleteEventRecord.getEventCommon().getTransactionID(),OEQPW008_ID) == 0 ) ||
                (variableStrCmp(lotOperationCompleteEventRecord.getEventCommon().getTransactionID(),OEQPW014_ID) == 0 ) ||
                (variableStrCmp(lotOperationCompleteEventRecord.getEventCommon().getTransactionID(),OEQPW023_ID) == 0 ) ||
                (variableStrCmp(lotOperationCompleteEventRecord.getEventCommon().getTransactionID(),OEQPW012_ID) == 0 ) ||
                (variableStrCmp(lotOperationCompleteEventRecord.getEventCommon().getTransactionID(),OEQPW024_ID) == 0 )) {
            iRc = createFHOPEHSTxMoveOutReq( lotOperationCompleteEventRecord, userDataSets );
            if ( !isOk(iRc) ) {
                return(iRc);
            }
        }

        iRc.setCode(0);

        return iRc;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 17:15:34
     */
    public Response createFHOPEHSTxMoveOutReq(Infos.LotOperationCompleteEventRecord lotOperationMoveEventRecord, List<Infos.UserDataSet> userDataSets) {
        Infos.Ohopehs fhopehs=new Infos.Ohopehs();
        Infos.OhopehsReticle fhopehs_reticle=new Infos.OhopehsReticle();
        Infos.OhopehsFixture fhopehs_fixture=new Infos.OhopehsFixture();
        Infos.OhopehsRparm fhopehs_rparm=new Infos.OhopehsRparm();
        Infos.OhopehsRparmWafer fhopehs_rparm_wafer=new Infos.OhopehsRparmWafer();
        Infos.OhopehsPasscnt fhopehs_passcnt=new Infos.OhopehsPasscnt();
        Infos.Frpd resultData_pd=new Infos.Frpd();
        Infos.Frpd resultData_pd_prev=new Infos.Frpd();
        Infos.Frlot resultData_lot=new Infos.Frlot();
        Infos.Frpos resultData_pos=new Infos.Frpos();
        Infos.Frpos resultData_pos_prev=new Infos.Frpos();
        Infos.Frlrcp resultData_lrcp=new Infos.Frlrcp();
        String castCategory = null;
        String productGrpID = null;
        String prodType     = null;
        String techID       = null;
        String custprodID   = null;
        String stageGrpID   = null;
        String prev_stageGrpID = null;
        String areaID     =null;
        String eqpName    =null;
        String locationID =null;
        int i;
        int j, k;
        Response iRc=new Response();
        iRc.setCode(0);

        iRc = tableMethod.getFRLOT(lotOperationMoveEventRecord.getLotData().getLotID(),resultData_lot);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCAST( lotOperationMoveEventRecord.getLotData().getCassetteID() , castCategory );
        castCategory=get(iRc);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPD( lotOperationMoveEventRecord.getLotData().getOperationID() , resultData_pd ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRLRCP( lotOperationMoveEventRecord.getLogicalRecipeID() , resultData_lrcp );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPD( lotOperationMoveEventRecord.getPreviousOperationID() , resultData_pd_prev ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( lotOperationMoveEventRecord.getLotData().getProductID() ,productGrpID , prodType );
        productGrpID=get(iRc,0);
        prodType=get(iRc,1);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( lotOperationMoveEventRecord.getLotData().getProductID() , techID );
        techID=get(iRc);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotOperationMoveEventRecord.getLotData().getLotID(),lotOperationMoveEventRecord.getLotData().getProductID() , custprodID );
        custprodID=get(iRc);
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRPOS( lotOperationMoveEventRecord.getPreviousObjrefModulePOS(),
                lotOperationMoveEventRecord.getPreviousOperationNumber(),
                lotOperationMoveEventRecord.getPreviousObjrefMainPF(),
                resultData_pos_prev );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        resultData_pos_prev.setOperationNO(lotOperationMoveEventRecord.getPreviousOperationNumber());
        resultData_pos_prev.setPdID(lotOperationMoveEventRecord.getPreviousOperationID());

        iRc = tableMethod.getFRPOS( lotOperationMoveEventRecord.getLotData().getObjrefPOS() ,
                lotOperationMoveEventRecord.getLotData().getOperationNumber(),
                lotOperationMoveEventRecord.getLotData().getObjrefMainPF(),
                resultData_pos ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        resultData_pos.setOperationNO(lotOperationMoveEventRecord.getLotData().getOperationNumber());
        resultData_pos.setPdID(lotOperationMoveEventRecord.getLotData().getOperationID());

        Params.Param<String> stageGrpIDParam=new Params.Param<>();
        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID() , stageGrpIDParam ) ;
        stageGrpID=get(stageGrpIDParam);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        Params.Param<String> prev_stageGrpIDParam=new Params.Param<>();
        iRc = tableMethod.getFRSTAGE( resultData_pos_prev.getStageID() , prev_stageGrpIDParam ) ;
        prev_stageGrpID=get(prev_stageGrpIDParam);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFREQP( lotOperationMoveEventRecord.getEquipmentID() , areaID , eqpName  );
        areaID=get(iRc,0);
        eqpName=get(iRc,1);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRAREA( areaID ,locationID ) ;
        locationID=get(iRc);
        if( !isOk(iRc) ) {
            return( iRc );
        }

        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
        fhopehs.setLot_type(lotOperationMoveEventRecord.getLotData().getLotType());
        fhopehs.setSub_lot_type(resultData_lot.getSubLotType());
        fhopehs.setCast_id(lotOperationMoveEventRecord.getLotData().getCassetteID());
        fhopehs.setCast_category(castCategory);
        fhopehs.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
        fhopehs.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
        fhopehs.setPd_id(lotOperationMoveEventRecord.getLotData().getOperationID());
        fhopehs.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
        fhopehs.setPd_name(resultData_pd.getOperationName());
        fhopehs.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
        fhopehs.setClaim_shop_date(lotOperationMoveEventRecord.getEventCommon().getEventShopDate());
        fhopehs.setClaim_user_id(lotOperationMoveEventRecord.getEventCommon().getUserID());
        if( variableStrCmp ( resultData_pos.getStageID() , resultData_pos_prev.getStageID() ) != 0 ) {
            fhopehs.setMove_type(SP_MOVEMENTTYPE_MOVEFORWARDSTAGE);
        } else {
            fhopehs.setMove_type(SP_MOVEMENTTYPE_MOVEFORWARDOPERATION);
        }
        if(( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW014_ID) == 0 ) ||
                ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW023_ID) == 0 )  ) {                                                                                                   //D4100080
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_FORCECOMP);
        } else if (( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW012_ID) == 0 ) ||
                ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW024_ID) == 0 )  ) {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_OPECOMPPARTIAL);
        } else {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONCOMPLETE);
        }
        fhopehs.setProd_type(prodType);
        fhopehs.setTest_type(resultData_lrcp.getTest_type());
        fhopehs.setMfg_layer(resultData_lot.getMfgLayer());
        fhopehs.setExt_priority(resultData_lot.getPriority());
        fhopehs.setPriority_class(resultData_lot.getPriorityClass());
        fhopehs.setProdspec_id(lotOperationMoveEventRecord.getLotData().getProductID());
        fhopehs.setProdgrp_id(productGrpID);
        fhopehs.setTech_id(techID);
        fhopehs.setCustomer_id(resultData_lot.getCustomerID());
        fhopehs.setCustprod_id(custprodID);
        fhopehs.setOrder_no(resultData_lot.getOrderNO());
        fhopehs.setStage_id(resultData_pos.getStageID());
        fhopehs.setStagegrp_id(stageGrpID);
        fhopehs.setPhoto_layer(resultData_pos.getPhotoLayer());
        fhopehs.setLocation_id(locationID);
        fhopehs.setArea_id(areaID);
        fhopehs.setEqp_id(lotOperationMoveEventRecord.getEquipmentID());
        fhopehs.setEqp_name(eqpName);
        fhopehs.setOpe_mode(lotOperationMoveEventRecord.getOperationMode());
        fhopehs.setLc_recipe_id(lotOperationMoveEventRecord.getLogicalRecipeID());
        fhopehs.setRecipe_id(lotOperationMoveEventRecord.getMachineRecipeID());
        fhopehs.setPh_recipe_id(lotOperationMoveEventRecord.getPhysicalRecipeID());
        fhopehs.setReticle_count(length(lotOperationMoveEventRecord.getReticleIDs()));
        fhopehs.setFixture_count(length(lotOperationMoveEventRecord.getFixtureIDs()));
        fhopehs.setRparm_count(length(lotOperationMoveEventRecord.getRecipeParameters()));
        fhopehs.setInit_hold_flag(0);
        fhopehs.setLast_hldrel_flag(0);
        fhopehs.setHold_state(lotOperationMoveEventRecord.getLotData().getHoldState());
        fhopehs.setHold_time("1901-01-01-00.00.00.000000");
        fhopehs.setPrev_mainpd_id(lotOperationMoveEventRecord.getPreviousRouteID());
        fhopehs.setPrev_ope_no(lotOperationMoveEventRecord.getPreviousOperationNumber());
        fhopehs.setPrev_pd_id(lotOperationMoveEventRecord.getPreviousOperationID());
        fhopehs.setPrev_pass_count(lotOperationMoveEventRecord.getPreviousOperationPassCount());
        fhopehs.setPrev_pd_name(resultData_pd_prev.getOperationName());
        fhopehs.setPrev_photo_layer(resultData_pos_prev.getPhotoLayer());
        fhopehs.setPrev_stage_id(resultData_pos_prev.getStageID());
        fhopehs.setPrev_stagegrp_id(prev_stageGrpID);
        fhopehs.setFlowbatch_id(lotOperationMoveEventRecord.getBatchID());
        fhopehs.setCtrl_job(lotOperationMoveEventRecord.getControlJobID());
        fhopehs.setRework_count(0);
        fhopehs.setOrg_wafer_qty(lotOperationMoveEventRecord.getLotData().getOriginalWaferQuantity());
        fhopehs.setCur_wafer_qty(lotOperationMoveEventRecord.getLotData().getCurrentWaferQuantity());
        fhopehs.setProd_wafer_qty(lotOperationMoveEventRecord.getLotData().getProductWaferQuantity());
        fhopehs.setCntl_wafer_qty(lotOperationMoveEventRecord.getLotData().getControlWaferQuantity());
        fhopehs.setClaim_prod_qty(lotOperationMoveEventRecord.getLotData().getProductWaferQuantity());
        fhopehs.setClaim_cntl_qty(lotOperationMoveEventRecord.getLotData().getControlWaferQuantity());
        fhopehs.setTotal_good_unit(0);
        fhopehs.setTotal_fail_unit(0);
        fhopehs.setLot_owner_id(resultData_lot.getLotOwner());
        fhopehs.setPlan_end_time(resultData_lot.getPlanEndTime());
        fhopehs.setWfrhs_time(lotOperationMoveEventRecord.getLotData().getWaferHistoryTimeStamp());
        fhopehs.setClaim_memo(lotOperationMoveEventRecord.getEventCommon().getEventMemo());
        fhopehs.setCriteria_flag(convert(lotOperationMoveEventRecord.getTestCriteriaResult()));
        if( length(lotOperationMoveEventRecord.getRecipeParameters()) > 0 ) {
            fhopehs.setRparm_change_type(SP_RPARM_CHANGETYPE_BYLOT);
        }
        if( length(lotOperationMoveEventRecord.getWaferLevelRecipe()) > 0 ) {
            fhopehs.setRparm_change_type(SP_RPARM_CHANGETYPE_BYWAFER);
        }

        fhopehs.setEvent_create_time(lotOperationMoveEventRecord.getEventCommon().getEventCreationTimeStamp());
        fhopehs.setPd_type(resultData_pd.getPd_type());
        fhopehs.setPrev_pd_type(resultData_pd_prev.getPd_type());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        if( length(lotOperationMoveEventRecord.getReticleIDs()) > 0 ) {
            for( i = 0; i < length(lotOperationMoveEventRecord.getReticleIDs()); i++ ) {
                fhopehs_reticle=new Infos.OhopehsReticle();
                fhopehs_reticle.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                fhopehs_reticle.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                fhopehs_reticle.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                fhopehs_reticle.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                fhopehs_reticle.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                if(( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW014_ID) == 0 ) ||
                        ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW023_ID) == 0 )  ) {                                                                                                   //D4100080
                    fhopehs_reticle.setOpe_category(SP_OPERATIONCATEGORY_FORCECOMP);
                } else if(( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW012_ID) == 0 ) ||
                        ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW024_ID) == 0 )  ) {
                    fhopehs_reticle.setOpe_category(SP_OPERATIONCATEGORY_OPECOMPPARTIAL);
                } else {
                    fhopehs_reticle.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONCOMPLETE);
                }
                fhopehs_reticle.setReticle_id(lotOperationMoveEventRecord.getReticleIDs().get(i));

                iRc = lotOperationHistoryService.insertLotOperationReticleHistory( fhopehs_reticle );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }
        if( length(lotOperationMoveEventRecord.getFixtureIDs()) > 0 ) {
            for( i = 0; i < length(lotOperationMoveEventRecord.getFixtureIDs()); i++ ) {
                fhopehs_fixture=new Infos.OhopehsFixture();
                fhopehs_fixture.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                fhopehs_fixture.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                fhopehs_fixture.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                fhopehs_fixture.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                fhopehs_fixture.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                if(( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW014_ID) == 0 ) ||
                        ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW023_ID) == 0 )  ) {
                    fhopehs_fixture.setOpe_category(SP_OPERATIONCATEGORY_FORCECOMP);
                } else if(( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW012_ID) == 0 ) ||
                        ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW024_ID) == 0 )  ) {
                    fhopehs_fixture.setOpe_category(SP_OPERATIONCATEGORY_OPECOMPPARTIAL);
                } else {
                    fhopehs_fixture.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONCOMPLETE);
                }
                fhopehs_fixture.setFixture_id(lotOperationMoveEventRecord.getFixtureIDs().get(i));

                iRc = lotOperationHistoryService.insertLotOperationFixtureHistory( fhopehs_fixture );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }
        if( length(lotOperationMoveEventRecord.getRecipeParameters()) > 0 ) {
            for( i = 0; i < length(lotOperationMoveEventRecord.getRecipeParameters()); i++ ) {
                fhopehs_rparm=new Infos.OhopehsRparm();
                fhopehs_rparm.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                fhopehs_rparm.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                fhopehs_rparm.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                fhopehs_rparm.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                fhopehs_rparm.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                if(( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW014_ID) == 0 ) ||
                        ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW023_ID) == 0 )  ) {
                    fhopehs_rparm.setOpe_category(SP_OPERATIONCATEGORY_FORCECOMP);
                } else if(( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW012_ID) == 0 ) ||
                        ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW024_ID) == 0 )  ) {
                    fhopehs_rparm.setOpe_category(SP_OPERATIONCATEGORY_OPECOMPPARTIAL);
                } else {
                    fhopehs_rparm.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONCOMPLETE);
                }
                fhopehs_rparm.setRparm_name(lotOperationMoveEventRecord.getRecipeParameters().get(i).getParameterName());
                fhopehs_rparm.setRparm_value(lotOperationMoveEventRecord.getRecipeParameters().get(i).getParameterValue());

                iRc = lotOperationHistoryService.insertLotOperationRparmHistory( fhopehs_rparm );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        if( length(lotOperationMoveEventRecord.getWaferLevelRecipe()) > 0 ) {
            for( i = 0; i < length(lotOperationMoveEventRecord.getWaferLevelRecipe()); i++ ) {
                if( length(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters()) > 0 ) {
                    for( j = 0; j < length(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters()); j++ ) {
                        if( HsWtDgMgWaferRparmDifFlag == 0 ) {
                            fhopehs_rparm_wafer=new Infos.OhopehsRparmWafer();
                            fhopehs_rparm_wafer.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                            fhopehs_rparm_wafer.setWafer_id(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferID());
                            fhopehs_rparm_wafer.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                            fhopehs_rparm_wafer.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                            fhopehs_rparm_wafer.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                            fhopehs_rparm_wafer.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                            if( ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW014_ID ) == 0 ) ||
                                    ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW023_ID ) == 0 ) ) {
                                fhopehs_rparm_wafer.setOpe_category(SP_OPERATIONCATEGORY_FORCECOMP);
                            } else if(( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW012_ID) == 0 ) ||
                                    ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW024_ID) == 0 )  ) {
                                fhopehs_rparm_wafer.setOpe_category(SP_OPERATIONCATEGORY_OPECOMPPARTIAL);
                            } else {
                                fhopehs_rparm_wafer.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONCOMPLETE);
                            }
                            fhopehs_rparm_wafer.setMachine_recipe_id(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getMachineRecipeID());
                            fhopehs_rparm_wafer.setRparm_name(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterName());
                            fhopehs_rparm_wafer.setRparm_value(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterValue());

                            iRc = lotOperationHistoryService.insertLotOperationRparmWaferHistory( fhopehs_rparm_wafer );
                            if( !isOk(iRc) ) {
                                return( iRc );
                            }
                        } else {
                            if( length(lotOperationMoveEventRecord.getRecipeParameters()) > 0 ) {
                                for( k = 0; k < length(lotOperationMoveEventRecord.getRecipeParameters()); k++ ) {
                                    if( variableStrCmp( lotOperationMoveEventRecord.getRecipeParameters().get(k).getParameterName(),
                                            lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterName()) == 0 &&
                                            variableStrCmp( lotOperationMoveEventRecord.getRecipeParameters().get(k).getParameterValue(),
                                                    lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterValue()) != 0 ) {
                                        fhopehs_rparm_wafer=new Infos.OhopehsRparmWafer();
                                        fhopehs_rparm_wafer.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                                        fhopehs_rparm_wafer.setWafer_id(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferID());
                                        fhopehs_rparm_wafer.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                                        fhopehs_rparm_wafer.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                                        fhopehs_rparm_wafer.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                                        fhopehs_rparm_wafer.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                                        if( ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW014_ID ) == 0 ) ||
                                                ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW023_ID ) == 0 ) ) {
                                            fhopehs_rparm_wafer.setOpe_category(SP_OPERATIONCATEGORY_FORCECOMP);
                                        } else if(( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW012_ID) == 0 ) ||
                                                ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW024_ID) == 0 )  ) {
                                            fhopehs_rparm_wafer.setOpe_category(SP_OPERATIONCATEGORY_OPECOMPPARTIAL);
                                        } else {
                                            fhopehs_rparm_wafer.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONCOMPLETE);
                                        }
                                        fhopehs_rparm_wafer.setMachine_recipe_id(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getMachineRecipeID());
                                        fhopehs_rparm_wafer.setRparm_name(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterName());
                                        fhopehs_rparm_wafer.setRparm_value(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterValue());

                                        iRc = lotOperationHistoryService.insertLotOperationRparmWaferHistory( fhopehs_rparm_wafer );
                                        if( !isOk(iRc) ) {
                                            return( iRc );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if( length(lotOperationMoveEventRecord.getProcessWafers()) > 0 ) {
            for( i = 0; i < length(lotOperationMoveEventRecord.getProcessWafers()); i++ ) {
                fhopehs_passcnt=new Infos.OhopehsPasscnt();
                fhopehs_passcnt.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                if( lotOperationMoveEventRecord.getProcessWafers().get(i).getCurrentOperationFlag() ) {
                    fhopehs_passcnt.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                    fhopehs_passcnt.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                    fhopehs_passcnt.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                } else {
                    fhopehs_passcnt.setMainpd_id(lotOperationMoveEventRecord.getPreviousRouteID());
                    fhopehs_passcnt.setOpe_no(lotOperationMoveEventRecord.getPreviousOperationNumber());
                    fhopehs_passcnt.setOpe_pass_count(lotOperationMoveEventRecord.getPreviousOperationPassCount());
                }
                fhopehs_passcnt.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                fhopehs_passcnt.setMove_type(fhopehs.getMove_type());
                fhopehs_passcnt.setOpe_category(fhopehs.getOpe_category());
                fhopehs_passcnt.setWafer_id(lotOperationMoveEventRecord.getProcessWafers().get(i).getWaferID());
                fhopehs_passcnt.setPass_count(lotOperationMoveEventRecord.getProcessWafers().get(i).getPassCount());

                iRc = lotOperationHistoryService.insertLotOperationPasscntHistory( fhopehs_passcnt );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        iRc.setCode(0);

        return iRc;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/25 15:38:24
     */
    public Response createFHOPEHSTxMoveInReq(Infos.LotOperationStartEventRecord lotOperationMoveEventRecord, List<Infos.UserDataSet> userDataSets) {
        Infos.Ohopehs fhopehs;
        Infos.Frpd resultData_pd=new Infos.Frpd();
        Infos.Frlot resultData_lot=new Infos.Frlot();
        Infos.Frpos resultData_pos=new Infos.Frpos();
        String castCategory=null;
        String productGrpID=null;
        String prodType=null;
        String techID =null;
        String custprodID=null;
        String stageGrpID=null;
        String areaID=null;
        String eqpName=null;
        String locationID = null;
        Integer i;
        Integer j, k;

        Response iRc=new Response();
        iRc.setCode(0);

        Infos.OhopehsReticle fhopehs_reticle    ;
        Infos.OhopehsFixture fhopehs_fixture    ;
        Infos.OhopehsRparm fhopehs_rparm      ;
        Infos.OhopehsRparmWafer fhopehs_rparm_wafer;
        Infos.OhopehsWafersampling fhopehs_wafersampling;

        iRc = tableMethod.getFRCAST( lotOperationMoveEventRecord.getLotData().getCassetteID() , castCategory ) ;
        castCategory=convert(iRc.getBody());
        if( !isOk(iRc) ) {
            return iRc;
        }
        iRc = tableMethod.getFRPD( lotOperationMoveEventRecord.getLotData().getOperationID() , resultData_pd );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( lotOperationMoveEventRecord.getLotData().getProductID() ,productGrpID , prodType );
        productGrpID= get(iRc,0);
        prodType= get(iRc,1);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRLOT( lotOperationMoveEventRecord.getLotData().getLotID() , resultData_lot ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( lotOperationMoveEventRecord.getLotData().getProductID() , techID );
        techID=get(iRc);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotOperationMoveEventRecord.getLotData().getLotID(),
                lotOperationMoveEventRecord.getLotData().getProductID() , custprodID );
        custprodID=get(iRc);
        if( !isOk(iRc) ) {
            return( iRc );
        }


        iRc = tableMethod.getFRPOS( lotOperationMoveEventRecord.getLotData().getObjrefPOS() ,
                lotOperationMoveEventRecord.getLotData().getOperationNumber(),
                lotOperationMoveEventRecord.getLotData().getObjrefMainPF(),
                resultData_pos ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        resultData_pos.setOperationNO(lotOperationMoveEventRecord.getLotData().getOperationNumber());
        resultData_pos.setPdID(lotOperationMoveEventRecord.getLotData().getOperationID());

        Params.Param<String> stageGrpIDParam=new Params.Param<>();
        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID() , stageGrpIDParam ) ;
        stageGrpID=get(stageGrpIDParam);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFREQP( lotOperationMoveEventRecord.getEquipmentID() , areaID , eqpName  );
        areaID=get(iRc,0);
        eqpName=get(iRc,1);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRAREA( areaID ,locationID ) ;
        locationID=get(iRc);
        if( !isOk(iRc) ) {
            return( iRc );
        }

        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
        fhopehs.setLot_type(lotOperationMoveEventRecord.getLotData().getLotType());
        fhopehs.setSub_lot_type(resultData_lot.getSubLotType());
        fhopehs.setCast_id(lotOperationMoveEventRecord.getLotData().getCassetteID());
        fhopehs.setCast_category(castCategory);
        fhopehs.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
        fhopehs.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
        fhopehs.setPd_id(lotOperationMoveEventRecord.getLotData().getOperationID());
        fhopehs.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
        fhopehs.setPd_name(resultData_pd.getOperationName());
        fhopehs.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
        fhopehs.setClaim_shop_date(lotOperationMoveEventRecord.getEventCommon().getEventShopDate());
        fhopehs.setClaim_user_id(lotOperationMoveEventRecord.getEventCommon().getUserID());
        fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE);

        if( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW005_ID ) == 0 ||
                variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW004_ID ) == 0 ) {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONSTART);
        }else if(( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW012_ID) == 0 ) ||
                ( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OEQPW024_ID) == 0 )  ) {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_OPESTARTCANCELPARTIAL);
        }else{
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONSTARTCANCEL);
        }
        fhopehs.setProd_type(prodType);
        fhopehs.setTest_type(resultData_pd.getTestType());
        fhopehs.setMfg_layer(resultData_lot.getMfgLayer());
        fhopehs.setExt_priority(resultData_lot.getPriority());
        fhopehs.setPriority_class(resultData_lot.getPriorityClass());
        fhopehs.setProdspec_id(lotOperationMoveEventRecord.getLotData().getProductID());
        fhopehs.setProdgrp_id(productGrpID);
        fhopehs.setTech_id(techID);
        fhopehs.setCustomer_id(resultData_lot.getCustomerID());
        fhopehs.setCustprod_id(custprodID);
        fhopehs.setOrder_no(resultData_lot.getOrderNO());
        fhopehs.setStage_id(resultData_pos.getStageID());
        fhopehs.setStagegrp_id(stageGrpID);
        fhopehs.setPhoto_layer(resultData_pos.getPhotoLayer());
        fhopehs.setLocation_id(locationID);
        fhopehs.setArea_id(areaID);
        fhopehs.setEqp_id(lotOperationMoveEventRecord.getEquipmentID());
        fhopehs.setEqp_name(eqpName);
        fhopehs.setOpe_mode(lotOperationMoveEventRecord.getOperationMode());
        fhopehs.setLc_recipe_id(lotOperationMoveEventRecord.getLogicalRecipeID());
        fhopehs.setRecipe_id(lotOperationMoveEventRecord.getMachineRecipeID());
        fhopehs.setPh_recipe_id(lotOperationMoveEventRecord.getPhysicalRecipeID());
        fhopehs.setReticle_count(length(lotOperationMoveEventRecord.getReticleIDs()));
        fhopehs.setFixture_count(length(lotOperationMoveEventRecord.getFixtureIDs()));
        fhopehs.setRparm_count(length(lotOperationMoveEventRecord.getRecipeParameters()));
        fhopehs.setInit_hold_flag(0);
        fhopehs.setLast_hldrel_flag(0);
        fhopehs.setHold_state(lotOperationMoveEventRecord.getLotData().getHoldState());
        fhopehs.setHold_time("1901-01-01-00.00.00.000000");
        fhopehs.setPrev_pass_count(0);
        fhopehs.setFlowbatch_id(lotOperationMoveEventRecord.getBatchID());
        fhopehs.setCtrl_job(lotOperationMoveEventRecord.getControlJobID());
        fhopehs.setRework_count(0);
        fhopehs.setOrg_wafer_qty(lotOperationMoveEventRecord.getLotData().getOriginalWaferQuantity());
        fhopehs.setCur_wafer_qty(lotOperationMoveEventRecord.getLotData().getCurrentWaferQuantity());
        fhopehs.setProd_wafer_qty(lotOperationMoveEventRecord.getLotData().getProductWaferQuantity());
        fhopehs.setCntl_wafer_qty(lotOperationMoveEventRecord.getLotData().getControlWaferQuantity());
        fhopehs.setClaim_prod_qty(lotOperationMoveEventRecord.getLotData().getProductWaferQuantity());
        fhopehs.setClaim_cntl_qty(lotOperationMoveEventRecord.getLotData().getControlWaferQuantity());
        fhopehs.setTotal_good_unit(0);
        fhopehs.setTotal_fail_unit(0);
        fhopehs.setLot_owner_id(resultData_lot.getLotOwner());
        fhopehs.setPlan_end_time(resultData_lot.getPlanEndTime());
        fhopehs.setWfrhs_time(lotOperationMoveEventRecord.getLotData().getWaferHistoryTimeStamp());
        fhopehs.setClaim_memo(lotOperationMoveEventRecord.getEventCommon().getEventMemo());
        fhopehs.setPd_type(resultData_pd.getPd_type());

        fhopehs.setCriteria_flag(CRITERIA_NA);

        if( length(lotOperationMoveEventRecord.getRecipeParameters()) > 0 ) {
            fhopehs.setRparm_change_type(SP_RPARM_CHANGETYPE_BYLOT);
        } if( length(lotOperationMoveEventRecord.getWaferLevelRecipe()) > 0 ) {
            fhopehs.setRparm_change_type(SP_RPARM_CHANGETYPE_BYWAFER);
        }

        fhopehs.setEvent_create_time(lotOperationMoveEventRecord.getEventCommon().getEventCreationTimeStamp());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        if( length(lotOperationMoveEventRecord.getReticleIDs()) > 0 ) {
            List<String> reticleIDs = lotOperationMoveEventRecord.getReticleIDs();
            for( i = 0; i < length(reticleIDs) ; i++ ) {
                fhopehs_reticle=new Infos.OhopehsReticle();
                fhopehs_reticle.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                fhopehs_reticle.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                fhopehs_reticle.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                fhopehs_reticle.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                fhopehs_reticle.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                fhopehs_reticle.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONSTART);
                fhopehs_reticle.setReticle_id(reticleIDs.get(i));

                iRc = lotOperationHistoryService.insertLotOperationReticleHistory( fhopehs_reticle );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }
        if( length(lotOperationMoveEventRecord.getFixtureIDs()) > 0 ) {
            for( i = 0; i < length(lotOperationMoveEventRecord.getFixtureIDs()); i++ ) {
                fhopehs_fixture=new Infos.OhopehsFixture();
                fhopehs_fixture.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                fhopehs_fixture.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                fhopehs_fixture.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                fhopehs_fixture.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                fhopehs_fixture.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                fhopehs_fixture.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONSTART);
                fhopehs_fixture.setFixture_id(lotOperationMoveEventRecord.getFixtureIDs().get(i));

                iRc = lotOperationHistoryService.insertLotOperationFixtureHistory( fhopehs_fixture );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }
        if( length(lotOperationMoveEventRecord.getRecipeParameters()) > 0 ) {
            for( i = 0; i < length(lotOperationMoveEventRecord.getRecipeParameters()); i++ ) {
                fhopehs_rparm=new Infos.OhopehsRparm();
                fhopehs_rparm.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                fhopehs_rparm.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                fhopehs_rparm.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                fhopehs_rparm.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                fhopehs_rparm.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                fhopehs_rparm.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONSTART);
                fhopehs_rparm.setRparm_name(lotOperationMoveEventRecord.getRecipeParameters().get(i).getParameterName());
                fhopehs_rparm.setRparm_value(lotOperationMoveEventRecord.getRecipeParameters().get(i).getParameterValue());

                iRc = lotOperationHistoryService.insertLotOperationRparmHistory( fhopehs_rparm );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        if( length(lotOperationMoveEventRecord.getWaferLevelRecipe()) > 0 ) {
            for( i = 0; i < length(lotOperationMoveEventRecord.getWaferLevelRecipe()); i++ ) {
                if( length(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters()) > 0 ) {
                    for( j = 0; j < length(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters()); j++ ) {
                        if( HsWtDgMgWaferRparmDifFlag == 0 ) {
                            fhopehs_rparm_wafer=new Infos.OhopehsRparmWafer();
                            fhopehs_rparm_wafer.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                            fhopehs_rparm_wafer.setWafer_id(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferID());
                            fhopehs_rparm_wafer.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                            fhopehs_rparm_wafer.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                            fhopehs_rparm_wafer.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                            fhopehs_rparm_wafer.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                            fhopehs_rparm_wafer.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONSTART);
                            fhopehs_rparm_wafer.setMachine_recipe_id(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getMachineRecipeID());
                            fhopehs_rparm_wafer.setRparm_name(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterName());
                            fhopehs_rparm_wafer.setRparm_value(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterValue());

                            iRc = lotOperationHistoryService.insertLotOperationRparmWaferHistory( fhopehs_rparm_wafer );
                            if( !isOk(iRc) ) {
                                return( iRc );
                            }
                        } else {
                            if( length(lotOperationMoveEventRecord.getRecipeParameters()) > 0 ) {
                                for( k = 0; k < length(lotOperationMoveEventRecord.getRecipeParameters()); k++ ) {
                                    if( variableStrCmp( lotOperationMoveEventRecord.getRecipeParameters().get(k).getParameterName(),
                                            lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterName() ) == 0 &&
                                            variableStrCmp( lotOperationMoveEventRecord.getRecipeParameters().get(k).getParameterValue(),
                                                    lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterValue() ) != 0 ) {
                                        fhopehs_rparm_wafer=new Infos.OhopehsRparmWafer();
                                        fhopehs_rparm_wafer.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                                        fhopehs_rparm_wafer.setWafer_id(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferID());
                                        fhopehs_rparm_wafer.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                                        fhopehs_rparm_wafer.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                                        fhopehs_rparm_wafer.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                                        fhopehs_rparm_wafer.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                                        fhopehs_rparm_wafer.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONSTART);
                                        fhopehs_rparm_wafer.setMachine_recipe_id(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getMachineRecipeID());
                                        fhopehs_rparm_wafer.setRparm_name(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterName());
                                        fhopehs_rparm_wafer.setRparm_value(lotOperationMoveEventRecord.getWaferLevelRecipe().get(i).getWaferRecipeParameters().get(j).getParameterValue());

                                        iRc = lotOperationHistoryService.insertLotOperationRparmWaferHistory( fhopehs_rparm_wafer );
                                        if( !isOk(iRc) ) {
                                            return( iRc );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if( length(lotOperationMoveEventRecord.getSamplingWafers()) > 0 ) {
            for( i = 0; i < length(lotOperationMoveEventRecord.getSamplingWafers()); i++ ) {
                fhopehs_wafersampling=new Infos.OhopehsWafersampling();
                fhopehs_wafersampling.setLot_id(lotOperationMoveEventRecord.getLotData().getLotID());
                fhopehs_wafersampling.setMainpd_id(lotOperationMoveEventRecord.getLotData().getRouteID());
                fhopehs_wafersampling.setOpe_no(lotOperationMoveEventRecord.getLotData().getOperationNumber());
                fhopehs_wafersampling.setOpe_pass_count(lotOperationMoveEventRecord.getLotData().getOperationPassCount());
                fhopehs_wafersampling.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp());
                fhopehs_wafersampling.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONSTART);
                fhopehs_wafersampling.setSmpl_wafer_id(lotOperationMoveEventRecord.getSamplingWafers().get(i));

                iRc = lotOperationHistoryService.insertLotOperationWaferSamplingHistory( fhopehs_wafersampling );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        iRc.setCode(0);

        return iRc;
    }

}
