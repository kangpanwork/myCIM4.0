package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
 * @date 2019/6/6 16:48
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotWaferMoveEventRecordService {

    private int HsWtDgMgRetryInterval;
    private int HsWtDgMgOldCategoryUseFlag;
    private int HsWtDgMgWaferRparmDifFlag;
    private int HsWtDgMgPosMESEnableFlag;
    private int HsWtDgMgREMOVEEVENTIMMEDIATELY;

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private LotReworkHistoryService lotReworkHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:01
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createLotWaferMoveEventRecord(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Response     iRc = returnOK();

        if(( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW040_ID) == 0) ||
                ( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW034_ID) == 0)) {
            iRc = createFHOPEHS_DestinationLot_Txtrc048(lotWaferMoveEventRecord, userDataSets) ;
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }else if(( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW033_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW019_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW021_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXTRC045_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW020_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW028_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC015_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW017_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW018_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC017_ID ) == 0 ) ) {
            iRc = createFHOPEHS_DestinationLot_Txtrc023(lotWaferMoveEventRecord, userDataSets) ;
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }else {
            iRc = createFHOPEHS_DestinationLot_Txtrc001(lotWaferMoveEventRecord, userDataSets) ;
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        if(( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW040_ID) == 0 )   ||
                ( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW034_ID) == 0 )) {
            iRc = createFHOPEHS_SourceLot_Txtrc048(lotWaferMoveEventRecord, userDataSets);
            if( !isOk(iRc) ) {
                return( iRc );
            }
        } else if(( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OBNKW003_ID) == 0 ) ||
                ( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OBNKW004_ID) == 0 )) {
            iRc = createFHOPEHS_SourceLot_Txbkc012(lotWaferMoveEventRecord, userDataSets);
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }else if(( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW020_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXTRC044_ID ) == 0 ) ||
                variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW032_ID ) == 0  ||
                variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW028_ID ) == 0  ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC059_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW017_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW018_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW019_ID ) == 0 )) {
            iRc = createFHOPEHS_SourceLot_Txtrc019(lotWaferMoveEventRecord, userDataSets);
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }else{
            iRc = createFHOPEHS_SourceLot_Txtrc023(lotWaferMoveEventRecord, userDataSets);
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }


        if( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW040_ID) == 0
                || variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW034_ID) == 0 ) {
            if( length(lotWaferMoveEventRecord.getCurrentWafers()) != 0 ) {
                iRc = createFHWLTHS_DestinationLot_Txtrc048(lotWaferMoveEventRecord, userDataSets);
                if( !isOk(iRc) ) {
                    return( iRc );
                }

                if( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW040_ID) == 0 ) {
                    iRc = createFHWLTHS_SourceLot_Txtrc048(lotWaferMoveEventRecord, userDataSets);  // 3100031
                    if( !isOk(iRc) ) {
                        return( iRc );
                    }
                }
            }
        } else {

            if(variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC015_ID ) == 0 ) {
                iRc = createFHWLTHS_DestinationLot_Txpcc015(lotWaferMoveEventRecord, userDataSets);
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }else if((variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW004_ID ) == 0 )||
                    (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OPMNW003_ID ) == 0 )||
                    (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC014_ID ) == 0 )||
                    (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC017_ID ) == 0 )||
                    (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW001_ID ) == 0 ) ) {
                iRc = createFHWLTHS_DestinationLot_Txtrc001(lotWaferMoveEventRecord, userDataSets);
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }else if( ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW002_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW005_ID ) == 0 ) ) {
                iRc = createFHWLTHS_DestinationLot_Txtrc083( lotWaferMoveEventRecord, userDataSets );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }else {
                iRc = createFHWLTHS_DestinationLot_Txtrc019(lotWaferMoveEventRecord, userDataSets);
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }


            if( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW033_ID) != 0 &&
                    variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),TXTRC045_ID) != 0 &&
                    variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW021_ID) != 0    ) {

                if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC015_ID ) == 0 ){
                    iRc = createFHWLTHS_SourceLot_Txpcc015(lotWaferMoveEventRecord, userDataSets);
                    if( !isOk(iRc) ) {
                        return( iRc );
                    }
                } else if( ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW002_ID ) == 0 ) ||
                        ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW005_ID ) == 0 ) ) {
                    iRc = createFHWLTHS_SourceLot_Txtrc083( lotWaferMoveEventRecord, userDataSets );
                    if( !isOk(iRc) ) {
                        return( iRc );
                    }
                } else{
                    iRc = createFHWLTHS_SourceLot_Txtrc019(lotWaferMoveEventRecord, userDataSets);
                    if( !isOk(iRc) ) {
                        return( iRc );
                    }
                }
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:14
     */
    public Response createFHOPEHS_DestinationLot_Txtrc048(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs=new Infos.Ohopehs();
        Infos.Frpd pdData=new Infos.Frpd();
        Infos.Frlot lotData=new Infos.Frlot();
        Infos.Frpos posData=new Infos.Frpos();
        Params.String castCategory=new Params.String();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String custProdID=new Params.String();
        Params.String stageGrpID=new Params.String();
        int productionWaferQuantity = 0;
        int controlWaferQuantity    = 0;
        int             i;
        int             j;
        Response             iRc = returnOK();


        iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(), castCategory );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC(lotWaferMoveEventRecord.getDestinationLotData().getProductID(), prodGrpID, prodType );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRLOT(lotWaferMoveEventRecord.getDestinationLotData().getLotID(), lotData);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP(lotWaferMoveEventRecord.getDestinationLotData().getProductID(), techID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotWaferMoveEventRecord.getDestinationLotData().getLotID(),
                lotWaferMoveEventRecord.getDestinationLotData().getProductID(),
                custProdID);
        if( !isOk(iRc) ) {
            return( iRc );
        }

        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id(lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
        fhopehs.setLot_type(lotWaferMoveEventRecord.getDestinationLotData().getLotType ());
        fhopehs.setSub_lot_type(lotData.getSubLotType ());
        fhopehs.setCast_id(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID());
        fhopehs.setCast_category(castCategory.getValue() );
        fhopehs.setOpe_pass_count (0 );
        fhopehs.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
        fhopehs.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate ());
        fhopehs.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
        fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );

        if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW040_ID ) == 0 ) {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_SPLIT );
        }
        else if(variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW034_ID ) == 0 ) {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MERGE );
        }
        else
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MONITORIN);
        }

        fhopehs.setProd_type(prodType.getValue() );
        fhopehs.setMfg_layer(lotData.getMfgLayer ());
        fhopehs.setExt_priority   (lotData.getPriority ());
        fhopehs.setPriority_class (lotData.getPriorityClass ());
        fhopehs.setProdspec_id(lotWaferMoveEventRecord.getDestinationLotData().getProductID());
        fhopehs.setProdgrp_id(prodGrpID.getValue());
        fhopehs.setTech_id(techID.getValue() );
        fhopehs.setCustomer_id(lotData.getCustomerID ());
        fhopehs.setCustprod_id(custProdID.getValue() );
        fhopehs.setOrder_no(lotData.getOrderNO());
        fhopehs.setHold_state(lotWaferMoveEventRecord.getDestinationLotData().getHoldState());
        fhopehs.setHold_time("1901-01-01-00.00.00.000000" );
        fhopehs.setBank_id(lotWaferMoveEventRecord.getDestinationLotData().getBankID ());
        fhopehs.setPrev_pass_count (0);
        fhopehs.setRework_count    (0);
        fhopehs.setOrg_wafer_qty   (lotWaferMoveEventRecord.getDestinationLotData().getOriginalWaferQuantity ());
        fhopehs.setCur_wafer_qty   (lotWaferMoveEventRecord.getDestinationLotData().getCurrentWaferQuantity ());
        fhopehs.setProd_wafer_qty  (lotWaferMoveEventRecord.getDestinationLotData().getProductWaferQuantity());
        fhopehs.setCntl_wafer_qty  (lotWaferMoveEventRecord.getDestinationLotData().getControlWaferQuantity());
        fhopehs.setClaim_prod_qty  (lotWaferMoveEventRecord.getDestinationLotData().getProductWaferQuantity());
        fhopehs.setClaim_cntl_qty  (lotWaferMoveEventRecord.getDestinationLotData().getControlWaferQuantity());

        if(variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW034_ID ) == 0 ) {
            for( i = 0; i < length(lotWaferMoveEventRecord.getSourceLots()) ; i++ ) {
                for( j = 0; j < length(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers()) ; j++ ) {
                    if(!isTrue(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getControlWaferFlag ())) {
                        productionWaferQuantity++;
                    }else{
                        controlWaferQuantity++;
                    }
                }
            }
            fhopehs.setClaim_prod_qty  (productionWaferQuantity);
            fhopehs.setClaim_cntl_qty  (controlWaferQuantity);
        }
        else{
            fhopehs.setClaim_prod_qty  (lotWaferMoveEventRecord.getDestinationLotData().getProductWaferQuantity());
            fhopehs.setClaim_cntl_qty  (lotWaferMoveEventRecord.getDestinationLotData().getControlWaferQuantity());
        }
        fhopehs.setTotal_good_unit (0);
        fhopehs.setTotal_fail_unit (0);
        fhopehs.setLot_owner_id(lotData.getLotOwner ());
        fhopehs.setPlan_end_time(lotData.getPlanEndTime ());
        fhopehs.setWfrhs_time(lotWaferMoveEventRecord.getDestinationLotData().getWaferHistoryTimeStamp());
        fhopehs.setClaim_memo(lotWaferMoveEventRecord.getEventCommon().getEventMemo ());
        fhopehs.setCriteria_flag   (CRITERIA_NA);
        fhopehs.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:17
     */
    public Response createFHOPEHS_DestinationLot_Txtrc023(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs=new Infos.Ohopehs();
        Infos.Frpd pdData=new Infos.Frpd();
        Infos.Frlot lotData=new Infos.Frlot();
        Infos.Frpos posData=new Infos.Frpos();

        Infos.Frpd pdDataSrc=new Infos.Frpd();
        Infos.Frlot lotDataSrc=new Infos.Frlot();
        Infos.Frpos posDataSrc=new Infos.Frpos();

        Params.String castCategory=new Params.String();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String custProdID=new Params.String();
        Params.String stageGrpID=new Params.String();
        Params.String stageGrpIDSrc=new Params.String();
        int    productionWaferQuantity = 0;
        int    controlWaferQuantity    = 0;
        int             i;
        int             j;
        Response             iRc = returnOK();


        iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(), castCategory );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPD(lotWaferMoveEventRecord.getDestinationLotData().getOperationID(), pdData );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC(lotWaferMoveEventRecord.getDestinationLotData().getProductID(), prodGrpID, prodType );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRLOT(lotWaferMoveEventRecord.getDestinationLotData().getLotID(), lotData);
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP(lotWaferMoveEventRecord.getDestinationLotData().getProductID(), techID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotWaferMoveEventRecord.getDestinationLotData().getLotID(),
                lotWaferMoveEventRecord.getDestinationLotData().getProductID(),
                custProdID);
        if( !isOk(iRc) ) {
            return( iRc );
        }

        tableMethod.getFRPOS(lotWaferMoveEventRecord.getDestinationLotData().getObjrefPOS(),
                lotWaferMoveEventRecord.getDestinationLotData().getOperationNumber(),
                lotWaferMoveEventRecord.getDestinationLotData().getObjrefMainPF(),
                posData) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        posData.setOperationNO   (lotWaferMoveEventRecord.getDestinationLotData().getOperationNumber()) ;
        posData.setPdID          (lotWaferMoveEventRecord.getDestinationLotData().getOperationID    ()) ;


        iRc = tableMethod.getFRSTAGE(posData.getStageID(), stageGrpID );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRPD(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationID(), pdDataSrc );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRLOT(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getLotID(), lotDataSrc);
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRPOS(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getObjrefPOS(),
                lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationNumber(),
                lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getObjrefMainPF(),
                posDataSrc) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        posDataSrc.setOperationNO   (lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationNumber()) ;
        posDataSrc.setPdID          (lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationID    ()) ;

        iRc = tableMethod.getFRSTAGE(posDataSrc.getStageID(), stageGrpIDSrc );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id(lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
        fhopehs.setLot_type(lotWaferMoveEventRecord.getDestinationLotData().getLotType ());
        fhopehs.setSub_lot_type(lotData.getSubLotType ());
        fhopehs.setCast_id(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID());
        fhopehs.setCast_category(castCategory.getValue() );

        if(( length(lotWaferMoveEventRecord.getSourceLots())> 0 ) &&
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW017_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW018_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW019_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW020_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW028_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW040_ID ) == 0 )  )
        {
            fhopehs.setMainpd_id(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getRouteID ());
            fhopehs.setOpe_no(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationNumber ());
            fhopehs.setPd_id(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationID ());
            fhopehs.setOpe_pass_count (lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationPassCount());
            fhopehs.setPd_type(pdDataSrc.getPd_type ());
        } else {
            fhopehs.setMainpd_id(lotWaferMoveEventRecord.getDestinationLotData().getRouteID ());
            fhopehs.setOpe_no(lotWaferMoveEventRecord.getDestinationLotData().getOperationNumber ());
            fhopehs.setPd_id(lotWaferMoveEventRecord.getDestinationLotData().getOperationID ());
            fhopehs.setOpe_pass_count (lotWaferMoveEventRecord.getDestinationLotData().getOperationPassCount());
            fhopehs.setPd_type(pdData.getPd_type ());
        }

        fhopehs.setPd_name(pdData.getOperationName ());
        fhopehs.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
        fhopehs.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate ());
        fhopehs.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
        fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );

        if(( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW020_ID ) == 0 )||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW017_ID ) == 0 )||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW018_ID ) == 0 )||
                variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW028_ID ) == 0 ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW019_ID ) == 0 )   ) {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_SPLIT );
        } else if((variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW033_ID ) == 0 ) ||
                (variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW021_ID ) == 0 )    ) {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MERGE );
        }
        else if( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXTRC045_ID ) == 0 )
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_PILOTMERGE );
        }
        else if(variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC015_ID ) == 0 )
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MONITOROUT);
        }
        else
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MONITORIN);
        }

        fhopehs.setProd_type(prodType .getValue());

        if(( length(lotWaferMoveEventRecord.getSourceLots()) > 0 ) &&
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW017_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW018_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW019_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW020_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW028_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW040_ID ) == 0 )  ) {
            fhopehs.setTest_type(pdDataSrc.getTestType()) ;
            fhopehs.setMfg_layer(lotDataSrc.getMfgLayer ());
            fhopehs.setStage_id(posDataSrc.getStageID ());
            fhopehs.setStagegrp_id(stageGrpIDSrc.getValue() );
            fhopehs.setPhoto_layer(posDataSrc.getPhotoLayer());
            fhopehs.setBank_id(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getBankID ());
        } else {
            fhopehs.setTest_type(pdData.getTestType()) ;
            fhopehs.setMfg_layer(lotData.getMfgLayer ());
            fhopehs.setStage_id(posData.getStageID ());
            fhopehs.setStagegrp_id(stageGrpID .getValue());
            fhopehs.setPhoto_layer(posData.getPhotoLayer());
            fhopehs.setBank_id(lotWaferMoveEventRecord.getDestinationLotData().getBankID ());
        }

        fhopehs.setExt_priority   (lotData.getPriority ());
        fhopehs.setPriority_class (lotData.getPriorityClass ());
        fhopehs.setProdspec_id(lotWaferMoveEventRecord.getDestinationLotData().getProductID());
        fhopehs.setProdgrp_id(prodGrpID.getValue());
        fhopehs.setTech_id(techID.getValue() );
        fhopehs.setCustomer_id(lotData.getCustomerID ());
        fhopehs.setCustprod_id(custProdID.getValue() );
        fhopehs.setOrder_no(lotData.getOrderNO());
        fhopehs.setHold_state(lotWaferMoveEventRecord.getDestinationLotData().getHoldState());
        fhopehs.setHold_time("1901-01-01-00.00.00.000000" );

        fhopehs.setPrev_pass_count (0);
        fhopehs.setRework_count    (0);
        fhopehs.setOrg_wafer_qty   (lotWaferMoveEventRecord.getDestinationLotData().getOriginalWaferQuantity ());
        fhopehs.setCur_wafer_qty   (lotWaferMoveEventRecord.getDestinationLotData().getCurrentWaferQuantity ());
        fhopehs.setProd_wafer_qty  (lotWaferMoveEventRecord.getDestinationLotData().getProductWaferQuantity());
        fhopehs.setCntl_wafer_qty  (lotWaferMoveEventRecord.getDestinationLotData().getControlWaferQuantity());
        fhopehs.setClaim_prod_qty  (lotWaferMoveEventRecord.getDestinationLotData().getProductWaferQuantity());
        fhopehs.setClaim_cntl_qty  (lotWaferMoveEventRecord.getDestinationLotData().getControlWaferQuantity());

        for( i = 0; i < length(lotWaferMoveEventRecord.getSourceLots()); i++ ) {
            for( j = 0; j < length(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers()); j++ ) {
                if(isTrue(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getControlWaferFlag ())!= TRUE ) {
                    productionWaferQuantity++;
                }else{
                    controlWaferQuantity++;
                }
            }
        }

        fhopehs.setClaim_prod_qty  (productionWaferQuantity);
        fhopehs.setClaim_cntl_qty  (controlWaferQuantity);
        fhopehs.setTotal_good_unit (0);
        fhopehs.setTotal_fail_unit (0);
        fhopehs.setLot_owner_id(lotData.getLotOwner ());
        fhopehs.setPlan_end_time(lotData.getPlanEndTime ());
        fhopehs.setWfrhs_time(lotWaferMoveEventRecord.getDestinationLotData().getWaferHistoryTimeStamp());
        fhopehs.setClaim_memo(lotWaferMoveEventRecord.getEventCommon().getEventMemo ());
        fhopehs.setCriteria_flag   (CRITERIA_NA);
        fhopehs.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:19
     */
    public Response createFHOPEHS_DestinationLot_Txtrc001(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs=new Infos.Ohopehs();
        Infos.Frpd pdData=new Infos.Frpd();
        Infos.Frlot lotData=new Infos.Frlot();
        Infos.Frpos posData=new Infos.Frpos();

        Infos.Frpd pdDataSrc=new Infos.Frpd();
        Infos.Frlot lotDataSrc=new Infos.Frlot();
        Infos.Frpos posDataSrc=new Infos.Frpos();

        Params.String castCategory=new Params.String();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String custProdID=new Params.String();
        Params.String stageGrpID=new Params.String();
        Params.String stageGrpIDSrc=new Params.String();
        int             i;
        int             j;
        Response             iRc = returnOK();


        iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(), castCategory );
        if( !isOk(iRc)) {
            return( iRc );
        }
        iRc = tableMethod.getFRPD(lotWaferMoveEventRecord.getDestinationLotData().getOperationID(), pdData );
        if( !isOk(iRc)) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC(lotWaferMoveEventRecord.getDestinationLotData().getProductID(), prodGrpID, prodType );
        if( !isOk(iRc)) {
            return( iRc );
        }
        iRc = tableMethod.getFRLOT(lotWaferMoveEventRecord.getDestinationLotData().getLotID(), lotData);
        if( !isOk(iRc)) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP(lotWaferMoveEventRecord.getDestinationLotData().getProductID(), techID );
        if( !isOk(iRc)) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotWaferMoveEventRecord.getDestinationLotData().getLotID(),
                lotWaferMoveEventRecord.getDestinationLotData().getProductID(),
                custProdID);
        if( !isOk(iRc) ) {
            return( iRc );
        }


        iRc = tableMethod.getFRPOS(lotWaferMoveEventRecord.getDestinationLotData().getObjrefPOS(),
                lotWaferMoveEventRecord.getDestinationLotData().getOperationNumber(),
                lotWaferMoveEventRecord.getDestinationLotData().getObjrefMainPF(),
                posData) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        posData.setOperationNO   (lotWaferMoveEventRecord.getDestinationLotData().getOperationNumber()) ;
        posData.setPdID          (lotWaferMoveEventRecord.getDestinationLotData().getOperationID    ()) ;

        iRc = tableMethod.getFRSTAGE(posData.getStageID(), stageGrpID );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRPD(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationID(), pdDataSrc );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRLOT(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getLotID(), lotDataSrc);
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRPOS(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getObjrefPOS(),
                lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationNumber(),
                lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getObjrefMainPF(),
                posDataSrc) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        posDataSrc.setOperationNO   (lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationNumber()) ;
        posDataSrc.setPdID          (lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationID    ()) ;

        iRc = tableMethod.getFRSTAGE(posDataSrc.getStageID(), stageGrpIDSrc );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id(lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
        fhopehs.setLot_type(lotWaferMoveEventRecord.getDestinationLotData().getLotType ());
        fhopehs.setSub_lot_type(lotData.getSubLotType ());
        fhopehs.setCast_id(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID());
        fhopehs.setCast_category(castCategory.getValue() );

        if(( length(lotWaferMoveEventRecord.getSourceLots()) > 0 ) &&
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW017_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW018_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW019_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW020_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW032_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC059_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW040_ID ) == 0 )  ) {
            fhopehs.setMainpd_id(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getRouteID ());
            fhopehs.setOpe_no(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationNumber ());
            fhopehs.setPd_id(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationID ());
            fhopehs.setOpe_pass_count (lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getOperationPassCount());
            fhopehs.setPd_type(pdDataSrc.getPd_type());
        } else {
            fhopehs.setMainpd_id(lotWaferMoveEventRecord.getDestinationLotData().getRouteID ());
            fhopehs.setOpe_no(lotWaferMoveEventRecord.getDestinationLotData().getOperationNumber ());
            fhopehs.setPd_id(lotWaferMoveEventRecord.getDestinationLotData().getOperationID ());
            fhopehs.setOpe_pass_count (lotWaferMoveEventRecord.getDestinationLotData().getOperationPassCount());
            fhopehs.setPd_type(pdData.getPd_type());
        }

        fhopehs.setPd_name(pdData.getOperationName ());
        fhopehs.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
        fhopehs.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate ());
        fhopehs.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());

        if(( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXTRC044_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW032_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC059_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW003_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW005_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW004_ID ) == 0 )  ) {
            fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
        }
        else if( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW002_ID ) == 0 )
        {
            fhopehs.setMove_type(SP_MOVEMENTTYPE_STBCANCEL );
        }else
        {
            fhopehs.setMove_type(SP_MOVEMENTTYPE_STB );
        }

        if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ||
                variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC059_ID ) == 0 ||
                variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW032_ID ) == 0   )
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_SPLIT );
        }
        else if( variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXTRC044_ID ) == 0 )
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_PILOTSPLIT );
        }
        else if((variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW001_ID ) == 0 ) ||
                (variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW004_ID ) == 0 ) ||
                (variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OPMNW003_ID ) == 0 ) ||
                (variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC014_ID ) == 0 ) )
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_STB );
        }
        else if((variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW003_ID ) == 0 ) ||
                (variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW004_ID ) == 0 ) )
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_VENDORLOTPREPARATION );
        }
        else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW002_ID ) == 0 )
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_STBCANCELLED );
        }
        else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW005_ID ) == 0 )
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_PREPARECANCELLED );
        }
        else
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MONITORIN);
        }

        fhopehs.setProd_type(prodType.getValue() );

        if(( length(lotWaferMoveEventRecord.getSourceLots()) > 0 ) &&
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW017_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW018_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW019_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW020_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW032_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC059_ID ) == 0 ) ||
                ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW040_ID ) == 0 )  )
        {
            fhopehs.setTest_type(pdDataSrc.getTestType()) ;
            fhopehs.setMfg_layer(lotDataSrc.getMfgLayer ());
            fhopehs.setStage_id(posDataSrc.getStageID ());
            fhopehs.setStagegrp_id(stageGrpIDSrc.getValue() );
            fhopehs.setPhoto_layer(posDataSrc.getPhotoLayer());
            fhopehs.setBank_id(lotWaferMoveEventRecord.getSourceLots().get(0).getSourceLotData().getBankID ());
        }
        else
        {
            fhopehs.setTest_type(pdData.getTestType()) ;
            fhopehs.setMfg_layer(lotData.getMfgLayer ());
            fhopehs.setStage_id(posData.getStageID ());
            fhopehs.setStagegrp_id(stageGrpID .getValue());
            fhopehs.setPhoto_layer(posData.getPhotoLayer());
            fhopehs.setBank_id(lotWaferMoveEventRecord.getDestinationLotData().getBankID ());
        }

        fhopehs.setExt_priority   (lotData.getPriority ());
        fhopehs.setPriority_class (lotData.getPriorityClass ());
        fhopehs.setProdspec_id(lotWaferMoveEventRecord.getDestinationLotData().getProductID());
        fhopehs.setProdgrp_id(prodGrpID.getValue());
        fhopehs.setTech_id(techID.getValue() );
        fhopehs.setCustomer_id(lotData.getCustomerID ());
        fhopehs.setCustprod_id(custProdID.getValue() );
        fhopehs.setOrder_no(lotData.getOrderNO());
        fhopehs.setHold_state(lotWaferMoveEventRecord.getDestinationLotData().getHoldState());
        fhopehs.setHold_time("1901-01-01-00.00.00.000000" );

        fhopehs.setPrev_pass_count (0);
        fhopehs.setRework_count    (0);
        fhopehs.setOrg_wafer_qty   (lotWaferMoveEventRecord.getDestinationLotData().getOriginalWaferQuantity ());
        fhopehs.setCur_wafer_qty   (lotWaferMoveEventRecord.getDestinationLotData().getCurrentWaferQuantity ());
        fhopehs.setProd_wafer_qty  (lotWaferMoveEventRecord.getDestinationLotData().getProductWaferQuantity());
        fhopehs.setCntl_wafer_qty  (lotWaferMoveEventRecord.getDestinationLotData().getControlWaferQuantity());
        fhopehs.setClaim_prod_qty  (lotWaferMoveEventRecord.getDestinationLotData().getProductWaferQuantity());
        fhopehs.setClaim_cntl_qty  (lotWaferMoveEventRecord.getDestinationLotData().getControlWaferQuantity());
        fhopehs.setClaim_prod_qty  (lotWaferMoveEventRecord.getDestinationLotData().getProductWaferQuantity());
        fhopehs.setClaim_cntl_qty  (lotWaferMoveEventRecord.getDestinationLotData().getControlWaferQuantity());
        fhopehs.setTotal_good_unit (0);
        fhopehs.setTotal_fail_unit (0);
        fhopehs.setLot_owner_id(lotData.getLotOwner ());
        fhopehs.setPlan_end_time(lotData.getPlanEndTime ());
        fhopehs.setWfrhs_time(lotWaferMoveEventRecord.getDestinationLotData().getWaferHistoryTimeStamp());
        fhopehs.setClaim_memo(lotWaferMoveEventRecord.getEventCommon().getEventMemo ());
        fhopehs.setCriteria_flag   (CRITERIA_NA);
        fhopehs.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }


        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:22
     */
    public Response createFHOPEHS_SourceLot_Txtrc048(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs=new Infos.Ohopehs();
        Infos.Frpd pdData=new Infos.Frpd();
        Infos.Frlot lotData=new Infos.Frlot();
        Infos.Frpos posData=new Infos.Frpos();
        Params.String castCategory=new Params.String();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String custProdID=new Params.String();
        Params.String stageGrpID=new Params.String();

        int       productionWaferQuantity;
        int       controlWaferQuantity ;
        int                 i,j;
        Response                 iRc = returnOK();


        for(i = 0; i < length(lotWaferMoveEventRecord.getSourceLots()); i++) {
            productionWaferQuantity = 0 ;
            controlWaferQuantity  = 0;

            iRc =tableMethod.getFRLOT(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID(), lotData);
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc =tableMethod.getFRCAST(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID(), castCategory );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc =tableMethod.getFRPRODSPEC(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(), prodGrpID, prodType );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc =tableMethod.getFRPRODGRP(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(), techID );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRCUSTPROD(  lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID(),
                    lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(),
                    custProdID);
            if( !isOk(iRc) ) {
                return( iRc );
            }

            fhopehs=new Infos.Ohopehs();
            fhopehs.setLot_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID ()) ;
            fhopehs.setLot_type(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotType ()) ;
            fhopehs.setSub_lot_type(lotData.getSubLotType ()) ;
            fhopehs.setCast_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID ());
            fhopehs.setCast_category(castCategory.getValue() );
            fhopehs.setOpe_pass_count (0 );
            fhopehs.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
            fhopehs.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());
            fhopehs.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
            fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );

            if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW040_ID ) == 0 ) {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_SPLIT );
            }
            else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW034_ID ) == 0 ) {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MERGE ) ;
            }
            else
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MONITOROUT );
            }

            fhopehs.setProd_type(prodType .getValue());
            fhopehs.setMfg_layer(lotData.getMfgLayer ());
            fhopehs.setExt_priority (lotData.getPriority ());
            fhopehs.setPriority_class (lotData.getPriorityClass());
            fhopehs.setProdspec_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID ());
            fhopehs.setProdgrp_id(prodGrpID .getValue());
            fhopehs.setTech_id(techID.getValue() );
            fhopehs.setCustomer_id(lotData.getCustomerID ());
            fhopehs.setCustprod_id(custProdID .getValue());
            fhopehs.setOrder_no(lotData.getOrderNO ());
            fhopehs.setHold_state(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getHoldState ());
            fhopehs.setHold_time("1901-01-01-00.00.00.000000" );
            fhopehs.setBank_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getBankID ());
            fhopehs.setOrg_wafer_qty  (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOriginalWaferQuantity());
            fhopehs.setCur_wafer_qty  (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCurrentWaferQuantity());
            fhopehs.setProd_wafer_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductWaferQuantity());
            fhopehs.setCntl_wafer_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getControlWaferQuantity());
            fhopehs.setClaim_prod_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductWaferQuantity());
            fhopehs.setClaim_cntl_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getControlWaferQuantity());

            if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW040_ID ) == 0 ) {
                fhopehs.setClaim_prod_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductWaferQuantity());
                fhopehs.setClaim_cntl_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getControlWaferQuantity());
            }
            else{
                for( j = 0; j < length(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers()); j++ ) {
                    if(isTrue(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getControlWaferFlag ())!= TRUE ) {
                        productionWaferQuantity++;
                    }else{
                        controlWaferQuantity++;
                    }
                }
                fhopehs.setClaim_prod_qty     (productionWaferQuantity);
                fhopehs.setClaim_cntl_qty     (controlWaferQuantity);
            }

            fhopehs.setTotal_good_unit    (0);
            fhopehs.setTotal_fail_unit    (0);
            fhopehs.setLot_owner_id(lotData.getLotOwner ());
            fhopehs.setPlan_end_time(lotData.getPlanEndTime ());
            fhopehs.setWfrhs_time(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getWaferHistoryTimeStamp ());
            fhopehs.setClaim_memo(lotWaferMoveEventRecord.getEventCommon().getEventMemo ());
            fhopehs.setCriteria_flag      (CRITERIA_NA);
            fhopehs.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

            iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:24
     */
    public Response createFHOPEHS_SourceLot_Txbkc012(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs=new Infos.Ohopehs();
        Infos.Frpd pdData=new Infos.Frpd();
        Infos.Frlot lotData=new Infos.Frlot();
        Infos.Frpos posData=new Infos.Frpos();
        Params.String castCategory=new Params.String();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String custProdID=new Params.String();
        Params.String stageGrpID=new Params.String();

        int       productionWaferQuantity;
        int       controlWaferQuantity ;
        int                 i,j;
        Response                 iRc = returnOK();


        for(i = 0; i < length(lotWaferMoveEventRecord.getSourceLots()); i++) {
            productionWaferQuantity = 0 ;
            controlWaferQuantity  = 0;

            iRc = tableMethod.getFRLOT(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID(), lotData);
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID(), castCategory );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPD(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationID(), pdData );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPRODSPEC(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(), prodGrpID, prodType );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPRODGRP(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(), techID );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRCUSTPROD(  lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID(),
                    lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(),
                    custProdID);
            if( !isOk(iRc) ) {
                return( iRc );
            }

            iRc = tableMethod.getFRPOS( lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getObjrefPOS (),
                    lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationNumber(),
                    lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getObjrefMainPF(),
                    posData ) ;
            if( !isOk(iRc) ) {
                return( iRc );
            }

            posData.setOperationNO     (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationNumber ());
            posData.setPdID            (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationID     ());

            iRc = tableMethod.getFRSTAGE(posData.getStageID(), stageGrpID );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            fhopehs=new Infos.Ohopehs();
            fhopehs.setLot_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID ()) ;
            fhopehs.setLot_type(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotType ()) ;
            fhopehs.setSub_lot_type(lotData.getSubLotType ()) ;
            fhopehs.setCast_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID ());
            fhopehs.setCast_category(castCategory.getValue() );
            fhopehs.setMainpd_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getRouteID ());
            fhopehs.setOpe_no(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationNumber ());
            fhopehs.setPd_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationID ()) ;
            fhopehs.setOpe_pass_count(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationPassCount ());
            fhopehs.setPd_name(pdData.getOperationName ());
            fhopehs.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
            fhopehs.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());
            fhopehs.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
            fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_VENDORWAFEROUT );
            fhopehs.setProd_type(prodType.getValue() );
            fhopehs.setTest_type(pdData.getTestType());
            fhopehs.setMfg_layer(lotData.getMfgLayer ());
            fhopehs.setExt_priority (lotData.getPriority ());
            fhopehs.setPriority_class (lotData.getPriorityClass());
            fhopehs.setProdspec_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID ());
            fhopehs.setProdgrp_id(prodGrpID .getValue());
            fhopehs.setTech_id(techID.getValue() );
            fhopehs.setCustomer_id(lotData.getCustomerID ());
            fhopehs.setCustprod_id(custProdID.getValue() );
            fhopehs.setOrder_no(lotData.getOrderNO ());
            fhopehs.setStage_id(posData.getStageID ());
            fhopehs.setStagegrp_id(stageGrpID .getValue());
            fhopehs.setPhoto_layer(posData.getPhotoLayer());
            fhopehs.setHold_state(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getHoldState ());
            fhopehs.setHold_time("1901-01-01-00.00.00.000000" );
            fhopehs.setBank_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getBankID ());
            fhopehs.setOrg_wafer_qty  (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOriginalWaferQuantity());
            fhopehs.setCur_wafer_qty  (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCurrentWaferQuantity());
            fhopehs.setProd_wafer_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductWaferQuantity());
            fhopehs.setCntl_wafer_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getControlWaferQuantity());
            fhopehs.setClaim_prod_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductWaferQuantity());
            fhopehs.setClaim_cntl_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getControlWaferQuantity());
            fhopehs.setPd_type(pdData.getPd_type ());

            for( j = 0; j < length(lotWaferMoveEventRecord.getCurrentWafers()); j++ ) {
                if(isTrue(lotWaferMoveEventRecord.getCurrentWafers().get(j).getControlWaferFlag ())!= TRUE ) {
                    productionWaferQuantity++;
                }else{
                    controlWaferQuantity++;
                }
            }

            fhopehs.setClaim_prod_qty     (productionWaferQuantity);
            fhopehs.setClaim_cntl_qty     (controlWaferQuantity);
            fhopehs.setTotal_good_unit    (0);
            fhopehs.setTotal_fail_unit    (0);
            fhopehs.setLot_owner_id(lotData.getLotOwner ());
            fhopehs.setPlan_end_time(lotData.getPlanEndTime ());
            fhopehs.setWfrhs_time(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getWaferHistoryTimeStamp ());
            fhopehs.setClaim_memo(lotWaferMoveEventRecord.getEventCommon().getEventMemo ());
            fhopehs.setCriteria_flag      (CRITERIA_NA);
            fhopehs.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

            iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:26
     */
    public Response createFHOPEHS_SourceLot_Txtrc019(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs=new Infos.Ohopehs();
        Infos.Frpd pdData=new Infos.Frpd();
        Infos.Frlot lotData=new Infos.Frlot();
        Infos.Frpos posData=new Infos.Frpos();
        Params.String castCategory=new Params.String();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String custProdID=new Params.String();
        Params.String stageGrpID=new Params.String();

        int                 i,j;
        Response                 iRc = returnOK();


        for(i = 0; i < length(lotWaferMoveEventRecord.getSourceLots()); i++) {

            iRc = tableMethod.getFRLOT(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID(), lotData);
            if( !isOk(iRc) ) {
                return( iRc );
            }

            iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID(), castCategory );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPD(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationID(), pdData );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPRODSPEC(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(), prodGrpID, prodType );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPRODGRP(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(), techID );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRCUSTPROD(  lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID(),
                    lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(),
                    custProdID);
            if( !isOk(iRc) ) {
                return( iRc );
            }


            iRc = tableMethod.getFRPOS( lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getObjrefPOS (),
                    lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationNumber(),
                    lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getObjrefMainPF(),
                    posData ) ;
            if( !isOk(iRc) ) {
                return( iRc );
            }

            posData.setOperationNO     (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationNumber ());
            posData.setPdID            (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationID     ());



            iRc = tableMethod.getFRSTAGE(posData.getStageID(), stageGrpID );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            fhopehs=new Infos.Ohopehs();
            fhopehs.setLot_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID ()) ;
            fhopehs.setLot_type(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotType ()) ;
            fhopehs.setSub_lot_type(lotData.getSubLotType ()) ;                    //DCR9900230
            fhopehs.setCast_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID ());
            fhopehs.setCast_category(castCategory.getValue() );
            fhopehs.setMainpd_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getRouteID ());
            fhopehs.setOpe_no(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationNumber ());
            fhopehs.setPd_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationID ()) ;
            fhopehs.setOpe_pass_count(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationPassCount ());
            fhopehs.setPd_name(pdData.getOperationName ());
            fhopehs.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
            fhopehs.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());
            fhopehs.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
            fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );

            if(( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW020_ID ) == 0 ) ||
                    variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW032_ID ) == 0  ||
                    variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW028_ID ) == 0  ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC059_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW017_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW018_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW019_ID) == 0 )    )
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_SPLIT );
            }
            else if(  variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),TXTRC044_ID ) == 0 )
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_PILOTSPLIT );
            }
            else
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MONITOROUT );
            }

            fhopehs.setProd_type(prodType.getValue() );
            fhopehs.setTest_type(pdData.getTestType());
            fhopehs.setMfg_layer(lotData.getMfgLayer ());
            fhopehs.setExt_priority (lotData.getPriority ());
            fhopehs.setPriority_class (lotData.getPriorityClass());
            fhopehs.setProdspec_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID ());
            fhopehs.setProdgrp_id(prodGrpID.getValue() );
            fhopehs.setTech_id(techID.getValue() );
            fhopehs.setCustomer_id(lotData.getCustomerID ());
            fhopehs.setCustprod_id(custProdID.getValue() );
            fhopehs.setOrder_no(lotData.getOrderNO ());
            fhopehs.setStage_id(posData.getStageID ());
            fhopehs.setStagegrp_id(stageGrpID.getValue() );
            fhopehs.setPhoto_layer(posData.getPhotoLayer());
            fhopehs.setHold_state(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getHoldState ());
            fhopehs.setHold_time("1901-01-01-00.00.00.000000" );
            fhopehs.setBank_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getBankID ());
            fhopehs.setOrg_wafer_qty  (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOriginalWaferQuantity());
            fhopehs.setCur_wafer_qty  (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCurrentWaferQuantity());
            fhopehs.setProd_wafer_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductWaferQuantity());
            fhopehs.setCntl_wafer_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getControlWaferQuantity());
            fhopehs.setClaim_prod_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductWaferQuantity());
            fhopehs.setClaim_cntl_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getControlWaferQuantity());
            fhopehs.setClaim_prod_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductWaferQuantity());
            fhopehs.setClaim_cntl_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getControlWaferQuantity());
            fhopehs.setTotal_good_unit    (0);
            fhopehs.setTotal_fail_unit    (0);
            fhopehs.setLot_owner_id(lotData.getLotOwner ());
            fhopehs.setPlan_end_time(lotData.getPlanEndTime ());
            fhopehs.setWfrhs_time(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getWaferHistoryTimeStamp ());
            fhopehs.setClaim_memo(lotWaferMoveEventRecord.getEventCommon().getEventMemo ());
            fhopehs.setCriteria_flag      (CRITERIA_NA);
            fhopehs.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());
            fhopehs.setPd_type(pdData.getPd_type ());

            iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:28
     */
    public Response createFHOPEHS_SourceLot_Txtrc023(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ){
        Infos.Ohopehs fhopehs=new Infos.Ohopehs();
        Infos.Frpd pdData=new Infos.Frpd();
        Infos.Frlot lotData=new Infos.Frlot();
        Infos.Frpos posData=new Infos.Frpos();
        Params.String castCategory=new Params.String();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String custProdID=new Params.String();
        Params.String stageGrpID=new Params.String();

        int         productionWaferQuantity;
        int         controlWaferQuantity ;
        int                 i,j;
        Response                 iRc = returnOK();


        for(i = 0; i < length(lotWaferMoveEventRecord.getSourceLots()); i++) {
            productionWaferQuantity = 0 ;
            controlWaferQuantity  = 0;

            iRc = tableMethod.getFRLOT(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID(), lotData);
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID(), castCategory );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPD(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationID(), pdData );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPRODSPEC(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(), prodGrpID, prodType );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPRODGRP(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(), techID );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRCUSTPROD(  lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID(),
                    lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID(),
                    custProdID);
            if( !isOk(iRc) ) {
                return( iRc );
            }


            iRc = tableMethod.getFRPOS( lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getObjrefPOS (),
                    lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationNumber(),
                    lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getObjrefMainPF(),
                    posData ) ;
            if( !isOk(iRc) ) {
                return( iRc );
            }

            posData.setOperationNO     (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationNumber ());
            posData.setPdID            (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationID     ());


            iRc = tableMethod.getFRSTAGE(posData.getStageID(), stageGrpID );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            fhopehs=new Infos.Ohopehs();
            fhopehs.setLot_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID ()) ;
            fhopehs.setLot_type(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotType ()) ;
            fhopehs.setSub_lot_type(lotData.getSubLotType ()) ;
            fhopehs.setCast_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID ());
            fhopehs.setCast_category(castCategory.getValue() );
            fhopehs.setMainpd_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getRouteID ());
            fhopehs.setOpe_no(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationNumber ());
            fhopehs.setPd_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationID ()) ;
            fhopehs.setOpe_pass_count(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOperationPassCount ());
            fhopehs.setPd_name(pdData.getOperationName ());
            fhopehs.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
            fhopehs.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());
            fhopehs.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());

            if(( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW033_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW021_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXTRC045_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC015_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC014_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW005_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC017_ID ) == 0 )   )
            {
                fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
            }
            else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW002_ID ) == 0 )
            {
                fhopehs.setMove_type(SP_MOVEMENTTYPE_STBCANCEL );
            }else
            {
                fhopehs.setMove_type(SP_MOVEMENTTYPE_STB ) ;
            }

            if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW030_ID) == 0 )
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_SPLIT );
            }
            else if(( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW033_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLOTW021_ID ) == 0 ) )
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MERGE ) ;
            }
            else if(  variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(),TXTRC045_ID ) == 0 )
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_PILOTMERGE );
            }
            else if(( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW001_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW004_ID ) == 0 )    )
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_STBUSED );
            }
            else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW002_ID ) == 0 )
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_STBCANCEL);
            }
            else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW005_ID ) == 0 )
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_PREPARECANCEL);
            }
            else if(variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC015_ID ) == 0 )
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MONITORIN);
            }
            else
            {
                fhopehs.setOpe_category(SP_OPERATIONCATEGORY_MONITOROUT );
            }

            fhopehs.setProd_type(prodType.getValue() );
            fhopehs.setTest_type(pdData.getTestType());
            fhopehs.setMfg_layer(lotData.getMfgLayer ());
            fhopehs.setExt_priority (lotData.getPriority ());
            fhopehs.setPriority_class (lotData.getPriorityClass());
            fhopehs.setProdspec_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID ());
            fhopehs.setProdgrp_id(prodGrpID.getValue() );
            fhopehs.setTech_id(techID.getValue() );
            fhopehs.setCustomer_id(lotData.getCustomerID ());
            fhopehs.setCustprod_id(custProdID.getValue() );
            fhopehs.setOrder_no(lotData.getOrderNO ());
            fhopehs.setStage_id(posData.getStageID ());
            fhopehs.setStagegrp_id(stageGrpID.getValue() );
            fhopehs.setPhoto_layer(posData.getPhotoLayer());
            fhopehs.setHold_state(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getHoldState ());
            fhopehs.setHold_time("1901-01-01-00.00.00.000000" );
            fhopehs.setBank_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getBankID ());
            fhopehs.setOrg_wafer_qty  (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getOriginalWaferQuantity());
            fhopehs.setCur_wafer_qty  (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCurrentWaferQuantity());
            fhopehs.setProd_wafer_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductWaferQuantity());
            fhopehs.setCntl_wafer_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getControlWaferQuantity());
            fhopehs.setClaim_prod_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductWaferQuantity());
            fhopehs.setClaim_cntl_qty (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getControlWaferQuantity());

            for( j = 0; j < length(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers()); j++ ) {
                if(isTrue(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getControlWaferFlag ())!= TRUE ) {
                    productionWaferQuantity++;
                }else{
                    controlWaferQuantity++;
                }
            }

            fhopehs.setClaim_prod_qty     (productionWaferQuantity);
            fhopehs.setClaim_cntl_qty     (controlWaferQuantity);
            fhopehs.setTotal_good_unit    (0);
            fhopehs.setTotal_fail_unit    (0);
            fhopehs.setLot_owner_id(lotData.getLotOwner ());
            fhopehs.setPlan_end_time(lotData.getPlanEndTime ());
            fhopehs.setWfrhs_time(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getWaferHistoryTimeStamp ());
            fhopehs.setClaim_memo(lotWaferMoveEventRecord.getEventCommon().getEventMemo ());
            fhopehs.setCriteria_flag      (CRITERIA_NA);
            fhopehs.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());
            fhopehs.setPd_type(pdData.getPd_type ());

            iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:31
     */
    public Response createFHWLTHS_DestinationLot_Txtrc048(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths fhwlths=new Infos.Ohwlths();
        Infos.Frwafer waferData=new Infos.Frwafer();
        Params.String castCategory=new Params.String();
        Boolean             check_flag ;
        int             i,j,z;
        Response             iRc = returnOK();

        for(i = 0 ; i < length(lotWaferMoveEventRecord.getCurrentWafers()); i++) {
            fhwlths=new Infos.Ohwlths();

            iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(), castCategory );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRWAFER(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID(), waferData );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID ());
            fhwlths.setCur_lot_id(lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
            fhwlths.setCur_cast_id(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID ());
            fhwlths.setCur_cast_category(castCategory.getValue() );
            fhwlths.setCur_cast_slot_no (lotWaferMoveEventRecord.getCurrentWafers().get(i).getSlotNumber()==null?null:
                    lotWaferMoveEventRecord.getCurrentWafers().get(i).getSlotNumber().intValue());
            fhwlths.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
            fhwlths.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
            fhwlths.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());

            if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW040_ID) == 0 )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_SPLIT );
            }
            else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW034_ID ) == 0 )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MERGE );
            }
            else
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MONITORIN );
            }

            fhwlths.setProdspec_id(lotWaferMoveEventRecord.getDestinationLotData().getProductID ());
            fhwlths.setGood_unit_count        (0);
            fhwlths.setRepair_unit_count      (0);
            fhwlths.setFail_unit_count        (0);
            fhwlths.setExist_flag("Y" );

            if( isTrue(lotWaferMoveEventRecord.getCurrentWafers().get(i).getControlWaferFlag ())== TRUE) {
                fhwlths.setControl_wafer (TRUE);
            }else{
                fhwlths.setControl_wafer (FALSE);
            }

            check_flag = FALSE ;
            for( j = 0; j < length(lotWaferMoveEventRecord.getSourceLots()); j++) {
                for(z = 0; z < length(lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers()) ; z++) {
                    if( variableStrCmp(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID(),
                            lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getWaferID ()) == 0 )
                    {
                        iRc = tableMethod.getFRCAST( lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getCassetteID(),
                                castCategory);
                        if( !isOk(iRc) ) {
                            return( iRc );
                        }
                        fhwlths.setPrev_lot_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getLotID ());
                        fhwlths.setPrev_cast_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getCassetteID ()) ;
                        fhwlths.setPrev_cast_category( castCategory .getValue());
                        fhwlths.setPrev_cast_slot_no (
                                lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getOriginalSlotNumber()==null?null:
                                        lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getOriginalSlotNumber().intValue());
                        fhwlths.setOrg_wafer_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getWaferID ());
                        fhwlths.setOrg_prodspec_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getProductID ());
                        fhwlths.setApply_wafer_flag("Y" );
                        check_flag = TRUE;
                        break;
                    }
                }
                if(Objects.equals(check_flag, TRUE))
                {
                    break;
                }
            }
            if(isTrue(check_flag )== FALSE ) {
                fhwlths.setPrev_cast_slot_no  (0);
                fhwlths.setApply_wafer_flag("N" );
            }

            fhwlths.setAlias_wafer_name(waferData.getAlias_wafer_name ());
            fhwlths.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc)) {
                return( iRc );
            }
        }

        for(i = 0 ; i < length(lotWaferMoveEventRecord.getSourceWafers()); i++) {
            fhwlths=new Infos.Ohwlths();
            iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(), castCategory );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRWAFER(lotWaferMoveEventRecord.getSourceWafers().get(i).getWaferID(), waferData );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id(lotWaferMoveEventRecord.getSourceWafers().get(i).getWaferID ());
            fhwlths.setCur_lot_id(lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
            fhwlths.setCur_cast_id(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID ());
            fhwlths.setCur_cast_category(castCategory.getValue() );
            fhwlths.setCur_cast_slot_no (lotWaferMoveEventRecord.getSourceWafers().get(i).getSlotNumber()==null?null:
                    lotWaferMoveEventRecord.getSourceWafers().get(i).getSlotNumber().intValue());
            fhwlths.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
            fhwlths.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
            fhwlths.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());

            if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW040_ID) == 0 )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_SPLIT );
            }
            else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW034_ID ) == 0 )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MERGE );
            }
            else
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MONITORIN );
            }

            fhwlths.setProdspec_id(lotWaferMoveEventRecord.getDestinationLotData().getProductID ());
            fhwlths.setGood_unit_count        (0);
            fhwlths.setRepair_unit_count      (0);
            fhwlths.setFail_unit_count        (0);
            fhwlths.setExist_flag("Y" );

            if( isTrue(lotWaferMoveEventRecord.getSourceWafers().get(i).getControlWaferFlag ())== TRUE)
            {
                fhwlths.setControl_wafer (TRUE);
            }else{
                fhwlths.setControl_wafer (FALSE);
            }

            fhwlths.setPrev_cast_slot_no  (0);
            fhwlths.setApply_wafer_flag("N" );

            fhwlths.setAlias_wafer_name(waferData.getAlias_wafer_name ());
            fhwlths.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:33
     */
    public Response createFHWLTHS_SourceLot_Txtrc048(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths fhwlths=new Infos.Ohwlths();
        Infos.Frwafer waferData=new Infos.Frwafer();
        Params.String castCategory=new Params.String();
        int             i,j,z;
        Boolean  check_flag;
        Response             iRc = returnOK();

        for(i = 0 ; i < length(lotWaferMoveEventRecord.getSourceLots()); i++) {
            for(j = 0 ; j < length(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers()); j++) {
                fhwlths=new Infos.Ohwlths();
                iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID(), castCategory );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
                iRc = tableMethod.getFRWAFER(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getWaferID(), waferData );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
                fhwlths.setWafer_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getWaferID ());
                fhwlths.setCur_lot_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID ());
                fhwlths.setCur_cast_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID ());
                fhwlths.setCur_cast_category(castCategory.getValue() );
                fhwlths.setCur_cast_slot_no (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getOriginalSlotNumber ()==null?null:
                        lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getOriginalSlotNumber ().intValue());
                fhwlths.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
                fhwlths.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
                fhwlths.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());

                if(variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW040_ID ) == 0 )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_SPLIT);
                }
                else if(variableStrCmp(  lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW034_ID ) == 0 )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MERGE );
                }
                else
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MONITOROUT );
                }

                fhwlths.setApply_wafer_flag("N" );
                fhwlths.setPrev_cast_slot_no (0);
                fhwlths.setProdspec_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID ());
                fhwlths.setGood_unit_count        (0);
                fhwlths.setRepair_unit_count      (0);
                fhwlths.setFail_unit_count        (0);
                fhwlths.setExist_flag("Y" );

                if( isTrue(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getControlWaferFlag ())== TRUE) {
                    fhwlths.setControl_wafer      (true);
                }else{
                    fhwlths.setControl_wafer      (false);
                }

                fhwlths.setAlias_wafer_name(waferData.getAlias_wafer_name ());
                fhwlths.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

                iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:35
     */
    public Response createFHWLTHS_DestinationLot_Txpcc015(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths fhwlths=new Infos.Ohwlths();
        Infos.Frwafer waferData=new Infos.Frwafer();
        Params.String castCategory=new Params.String();
        int             check_flag ;
        int             i,j,z;
        Response             iRc = returnOK();


        for(i = 0 ; i < length(lotWaferMoveEventRecord.getCurrentWafers()); i++) {
            fhwlths=new Infos.Ohwlths();

            iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(), castCategory );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRWAFER(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID(), waferData );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID ());
            fhwlths.setCur_lot_id(lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
            fhwlths.setCur_cast_id(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID ());
            fhwlths.setCur_cast_category(castCategory.getValue() );
            fhwlths.setCur_cast_slot_no (lotWaferMoveEventRecord.getCurrentWafers().get(i).getSlotNumber()==null?
                    null:lotWaferMoveEventRecord.getCurrentWafers().get(i).getSlotNumber().intValue());
            fhwlths.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
            fhwlths.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
            fhwlths.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());
            fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MONITOROUT);
            fhwlths.setProdspec_id(lotWaferMoveEventRecord.getDestinationLotData().getProductID ());
            fhwlths.setGood_unit_count        (waferData.getGoodDiceQty());
            fhwlths.setRepair_unit_count      (waferData.getRepairedDiceQty());
            fhwlths.setFail_unit_count        (waferData.getBadDiceQty());
            fhwlths.setExist_flag("Y" );

            if( isTrue(lotWaferMoveEventRecord.getCurrentWafers().get(i).getControlWaferFlag ())== TRUE) {
                fhwlths.setControl_wafer (TRUE);
            }else{
                fhwlths.setControl_wafer (FALSE);
            }

            fhwlths.setApply_wafer_flag("N" );
            fhwlths.setAlias_wafer_name(waferData.getAlias_wafer_name ());
            fhwlths.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:38
     */
    public Response createFHWLTHS_DestinationLot_Txtrc001(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths fhwlths=new Infos.Ohwlths();
        Infos.Frwafer waferData=new Infos.Frwafer();
        Params.String castCategory=new Params.String();
        Boolean             check_flag ;
        int             i,j,z;
        Response             iRc = returnOK();


        for(i = 0 ; i < length(lotWaferMoveEventRecord.getCurrentWafers()); i++) {
            fhwlths=new Infos.Ohwlths();

            iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(), castCategory );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRWAFER(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID(), waferData );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID ());
            fhwlths.setCur_lot_id(lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
            fhwlths.setCur_cast_id(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID ());
            fhwlths.setCur_cast_category(castCategory .getValue());
            fhwlths.setCur_cast_slot_no (lotWaferMoveEventRecord.getCurrentWafers().get(i).getSlotNumber()==null?
                    null:lotWaferMoveEventRecord.getCurrentWafers().get(i).getSlotNumber().intValue());
            fhwlths.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
            fhwlths.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
            fhwlths.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());

            if(( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW001_ID ) == 0 )||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW004_ID ) == 0 )||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OPMNW003_ID ) == 0 )||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC014_ID ) == 0 )   )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_STB );
            }
            else
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MONITORIN );
            }

            fhwlths.setProdspec_id(lotWaferMoveEventRecord.getDestinationLotData().getProductID ());
            fhwlths.setGood_unit_count        (waferData.getGoodDiceQty());
            fhwlths.setRepair_unit_count      (waferData.getRepairedDiceQty());
            fhwlths.setFail_unit_count        (waferData.getBadDiceQty());
            fhwlths.setExist_flag("Y" );

            if( isTrue(lotWaferMoveEventRecord.getCurrentWafers().get(i).getControlWaferFlag ())== TRUE)
            {
                fhwlths.setControl_wafer (TRUE);
            }else{
                fhwlths.setControl_wafer (FALSE);
            }

            check_flag = FALSE ;
            for( j = 0; j < length(lotWaferMoveEventRecord.getSourceLots()) ; j++) {
                for(z = 0; z < length(lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers()) ; z++) {
                    if( ( variableStrCmp(lotWaferMoveEventRecord.getCurrentWafers().get(i).getOriginalWaferID(),
                            lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getWaferID ()) == 0 ) ||
                            ( variableStrCmp(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID(),
                                    lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getWaferID ()) == 0 )) {

                        iRc = tableMethod.getFRCAST( lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getCassetteID(),
                                castCategory);
                        if( !isOk(iRc) ) {
                            return( iRc );
                        }
                        fhwlths.setPrev_lot_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getLotID ());
                        fhwlths.setPrev_cast_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getCassetteID ()) ;
                        fhwlths.setPrev_cast_category(castCategory .getValue());
                        fhwlths.setPrev_cast_slot_no (
                                lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getOriginalSlotNumber()==null?
                                null:
                                        lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getOriginalSlotNumber().intValue());
                        fhwlths.setOrg_wafer_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getWaferID ());
                        fhwlths.setOrg_prodspec_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getProductID ());
                        fhwlths.setApply_wafer_flag("Y" );
                        check_flag = TRUE;
                        break;
                    }
                }
                if(Objects.equals(check_flag, TRUE))
                {
                    break;
                }
            }
            if(isTrue(check_flag) == FALSE ) {
                fhwlths.setPrev_cast_slot_no  (0);
                fhwlths.setApply_wafer_flag("N" );
            }

            fhwlths.setAlias_wafer_name(waferData.getAlias_wafer_name ());
            fhwlths.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:40
     */
    public Response createFHWLTHS_DestinationLot_Txtrc083(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths fhwlths=new Infos.Ohwlths();
        Infos.Frwafer waferData=new Infos.Frwafer();
        Params.String castCategory=new Params.String();
        int             i,j,z;
        Response             iRc = returnOK();


        for(i = 0 ; i < length(lotWaferMoveEventRecord.getCurrentWafers()); i++) {
            fhwlths=new Infos.Ohwlths();

            iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(), castCategory );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRWAFER(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID(), waferData );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID ());
            fhwlths.setCur_lot_id(lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
            fhwlths.setCur_cast_id(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID ());
            fhwlths.setCur_cast_category(castCategory.getValue() );
            fhwlths.setCur_cast_slot_no (lotWaferMoveEventRecord.getCurrentWafers().get(i).getSlotNumber()==null?null:
                    lotWaferMoveEventRecord.getCurrentWafers().get(i).getSlotNumber().intValue());
            fhwlths.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
            fhwlths.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
            fhwlths.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());

            if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW002_ID ) == 0 ) {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_STBCANCELLED );
            }
            else
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_PREPARECANCELLED );
            }

            fhwlths.setApply_wafer_flag("N" );
            fhwlths.setPrev_cast_slot_no (0);
            fhwlths.setProdspec_id(lotWaferMoveEventRecord.getDestinationLotData().getProductID ());
            fhwlths.setGood_unit_count        (waferData.getGoodDiceQty());
            fhwlths.setRepair_unit_count      (waferData.getRepairedDiceQty());
            fhwlths.setFail_unit_count        (waferData.getBadDiceQty());
            fhwlths.setExist_flag("Y" );

            if( isTrue(lotWaferMoveEventRecord.getCurrentWafers().get(i).getControlWaferFlag ())== TRUE)
            {
                fhwlths.setControl_wafer (true);
            }else{
                fhwlths.setControl_wafer (false);
            }

            fhwlths.setAlias_wafer_name(waferData.getAlias_wafer_name ());
            fhwlths.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:41
     */
    public Response createFHWLTHS_DestinationLot_Txtrc019(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths fhwlths=new Infos.Ohwlths();
        Infos.Frwafer waferData=new Infos.Frwafer();
        Params.String castCategory=new Params.String();
        Boolean             check_flag ;
        int             i,j,z;
        Response             iRc = returnOK();


        for(i = 0 ; i < length(lotWaferMoveEventRecord.getCurrentWafers()); i++) {
            fhwlths=new Infos.Ohwlths();

            iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(), castCategory );
            if(!isOk(iRc) )
            {
                return( iRc );
            }
            iRc = tableMethod.getFRWAFER(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID(), waferData );
            if( !isOk(iRc))
            {
                return( iRc );
            }

            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID ());
            fhwlths.setCur_lot_id(lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
            fhwlths.setCur_cast_id(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID ());
            fhwlths.setCur_cast_category(castCategory.getValue() );
            fhwlths.setCur_cast_slot_no (lotWaferMoveEventRecord.getCurrentWafers().get(i).getSlotNumber()==null?null:
                    lotWaferMoveEventRecord.getCurrentWafers().get(i).getSlotNumber().intValue());
            fhwlths.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
            fhwlths.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
            fhwlths.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());

            if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ||
                    variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC059_ID ) == 0 ||
                    variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW032_ID ) == 0   )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_SPLIT );
            }
            else if(  variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXTRC044_ID ) == 0 )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_PILOTSPLIT );
            }
            else if(  variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXTRC045_ID ) == 0 )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_PILOTMERGE );
            }
            else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW011_ID ) == 0 )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_REWORK );
            }
//P5000014 add start
            else if((variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW019_ID ) == 0 ) ||
                    (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW017_ID ) == 0 ) ||
                    (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW020_ID ) == 0 ) ||
                    variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW028_ID ) == 0   ||
                    (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW018_ID ) == 0 )    )
            {
                if( HsWtDgMgOldCategoryUseFlag == 0 )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_REWORK);
                }
                else
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_SPLIT);
                }
            }
            else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW033_ID ) == 0 )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MERGE );
            }
            else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW021_ID ) == 0 )
            {
                if( HsWtDgMgOldCategoryUseFlag == 0 )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_REWORKCANCEL);
                }
                else
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MERGE);
                }
            }
            else if(( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW003_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW004_ID ) == 0 ) )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_VENDORLOTPREPARATION );
            }
            else
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MONITORIN );
            }

            fhwlths.setProdspec_id(lotWaferMoveEventRecord.getDestinationLotData().getProductID ());
            fhwlths.setGood_unit_count        (waferData.getGoodDiceQty());
            fhwlths.setRepair_unit_count      (waferData.getRepairedDiceQty());
            fhwlths.setFail_unit_count        (waferData.getBadDiceQty());
            fhwlths.setExist_flag("Y" );

            if( isTrue(lotWaferMoveEventRecord.getCurrentWafers().get(i).getControlWaferFlag ())== TRUE) {
                fhwlths.setControl_wafer (TRUE);
            }else{
                fhwlths.setControl_wafer (FALSE);
            }

            check_flag = FALSE ;
            for( j = 0; j < length(lotWaferMoveEventRecord.getSourceLots()); j++) {
                for(z = 0; z < length(lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers()); z++) {
                    if( ( variableStrCmp(lotWaferMoveEventRecord.getCurrentWafers().get(i).getOriginalWaferID(),
                            lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getWaferID ()) == 0 ) ||
                            ( variableStrCmp(lotWaferMoveEventRecord.getCurrentWafers().get(i).getWaferID(),
                                    lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getWaferID ()) == 0 ) ) {
                        iRc = tableMethod.getFRCAST( lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getCassetteID(),
                                castCategory);
                        if( !isOk(iRc) ) {
                            return( iRc );
                        }
                        fhwlths.setPrev_lot_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getLotID ());
                        fhwlths.setPrev_cast_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getCassetteID ()) ;
                        fhwlths.setPrev_cast_category(castCategory.getValue() );
                        fhwlths.setPrev_cast_slot_no (
                                lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getOriginalSlotNumber()==null?
                                null:
                                        lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getOriginalSlotNumber().intValue());
                        fhwlths.setOrg_wafer_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getCurrentWafers().get(z).getWaferID ());
                        fhwlths.setOrg_prodspec_id(
                                lotWaferMoveEventRecord.getSourceLots().get(j).getSourceLotData().getProductID ());
                        fhwlths.setApply_wafer_flag("Y" );
                        check_flag = TRUE;
                        break;
                    }
                }
                if(Objects.equals(check_flag, TRUE))
                {
                    break;
                }
            }
            if(isTrue(check_flag )== FALSE ) {
                fhwlths.setPrev_cast_slot_no  (0);
                fhwlths.setApply_wafer_flag("N" );
            }

            fhwlths.setAlias_wafer_name(waferData.getAlias_wafer_name ());
            fhwlths.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        for(i = 0 ; i < length(lotWaferMoveEventRecord.getSourceWafers()); i++) {
            fhwlths=new Infos.Ohwlths();

            iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(), castCategory );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRWAFER(lotWaferMoveEventRecord.getSourceWafers().get(i).getWaferID(), waferData );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id(lotWaferMoveEventRecord.getSourceWafers().get(i).getWaferID ());
            fhwlths.setCur_lot_id(lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
            fhwlths.setCur_cast_id(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID ());
            fhwlths.setCur_cast_category(castCategory.getValue() );
            fhwlths.setCur_cast_slot_no (lotWaferMoveEventRecord.getSourceWafers().get(i).getSlotNumber()==null?
                    null:
                    lotWaferMoveEventRecord.getSourceWafers().get(i).getSlotNumber().intValue());
            fhwlths.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
            fhwlths.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
            fhwlths.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());

            if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ||
                    variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC059_ID ) == 0 ||
                    variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW032_ID ) == 0   )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_SPLIT );
            }
            else if((variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW019_ID ) == 0 ) ||
                    (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW017_ID ) == 0 ) ||
                    (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW020_ID ) == 0 ) ||
                    variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW028_ID ) == 0   ||
                    (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW018_ID ) == 0 )    )
            {
                if( HsWtDgMgOldCategoryUseFlag == 0 )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_REWORK);
                }
                else
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_SPLIT);
                }
            }
            else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW033_ID ) == 0 )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MERGE );
            }
            else if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW021_ID ) == 0 )
            {
                if( HsWtDgMgOldCategoryUseFlag == 0 )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_REWORKCANCEL);
                }
                else
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MERGE);
                }
            }
            else if(( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW003_ID ) == 0 ) ||
                    ( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW004_ID ) == 0 ) )
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_VENDORLOTPREPARATION );
            }
            else
            {
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MONITORIN );
            }

            fhwlths.setProdspec_id(lotWaferMoveEventRecord.getDestinationLotData().getProductID ());
            fhwlths.setGood_unit_count        (waferData.getGoodDiceQty());
            fhwlths.setRepair_unit_count      (waferData.getRepairedDiceQty());
            fhwlths.setFail_unit_count        (waferData.getBadDiceQty());
            fhwlths.setExist_flag("Y" );

            if( isTrue(lotWaferMoveEventRecord.getSourceWafers().get(i).getControlWaferFlag ())== TRUE) {
                fhwlths.setControl_wafer (TRUE);
            }else{
                fhwlths.setControl_wafer (FALSE);
            }

            fhwlths.setPrev_cast_slot_no  (0);
            fhwlths.setApply_wafer_flag("N" );

            fhwlths.setAlias_wafer_name(waferData.getAlias_wafer_name ());
            fhwlths.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:45
     */
    public Response createFHWLTHS_SourceLot_Txpcc015(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths fhwlths=new Infos.Ohwlths();
        Infos.Frwafer waferData=new Infos.Frwafer();
        Params.String castCategory=new Params.String();
        int             i,j,z;
        Boolean  check_flag;
        Response             iRc = returnOK();


        for(i = 0 ; i < length(lotWaferMoveEventRecord.getSourceLots()); i++) {
            for(j = 0 ; j < length(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers()); j++) {
                fhwlths=new Infos.Ohwlths();

                iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID(), castCategory );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
                iRc = tableMethod.getFRWAFER(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getWaferID(), waferData );
                if( !isOk(iRc) ) {
                    return( iRc );
                }

                fhwlths.setWafer_id(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getWaferID ());
                fhwlths.setCur_lot_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID ());
                fhwlths.setCur_cast_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID ());
                fhwlths.setCur_cast_category(castCategory.getValue() );
                fhwlths.setCur_cast_slot_no (lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getOriginalSlotNumber ()==null?
                        null:
                        lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getOriginalSlotNumber ().intValue());
                fhwlths.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
                fhwlths.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
                fhwlths.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());
                fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MONITORIN);

                check_flag = FALSE ;
                for(z = 0; z < length(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers()); z++) {
                    if( variableStrCmp(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getWaferID(),
                            lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(z).getWaferID ()) == 0 ) {
                        fhwlths.setApply_wafer_flag("Y" );
                        iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(), castCategory);
                        if( !isOk(iRc) ) {
                            return( iRc );
                        }
                        fhwlths.setPrev_lot_id(
                                lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
                        fhwlths.setPrev_cast_id(
                                lotWaferMoveEventRecord.getDestinationLotData().getCassetteID());
                        fhwlths.setPrev_cast_category(castCategory .getValue());
                        fhwlths.setPrev_cast_slot_no (0);
                        fhwlths.setOrg_prodspec_id(
                                lotWaferMoveEventRecord.getDestinationLotData().getProductID ());
                        check_flag = TRUE;
                    }
                }
                if(isTrue(check_flag )== FALSE ) {
                    fhwlths.setApply_wafer_flag("N" );
                    fhwlths.setPrev_cast_slot_no  (0);
                }

                fhwlths.setProdspec_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID ());
                fhwlths.setGood_unit_count        (waferData.getGoodDiceQty());
                fhwlths.setRepair_unit_count      (waferData.getRepairedDiceQty());
                fhwlths.setFail_unit_count        (waferData.getBadDiceQty());
                fhwlths.setExist_flag("Y" );

                if( isTrue(lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getControlWaferFlag ())== TRUE) {
                    fhwlths.setControl_wafer      (true);
                }else{
                    fhwlths.setControl_wafer      (false);
                }

                fhwlths.setAlias_wafer_name(waferData.getAlias_wafer_name ());
                fhwlths.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

                iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:48
     */
    public Response createFHWLTHS_SourceLot_Txtrc083(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths fhwlths=new Infos.Ohwlths();
        Infos.Frwafer waferData=new Infos.Frwafer();
        Params.String castCategory=new Params.String();
        int             i,j,z;
        Boolean  check_flag;
        Response iRc = returnOK();


        for(i = 0 ; i < length(lotWaferMoveEventRecord.getSourceLots()); i++) {
            for(j = 0 ; j < length(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers()); j++) {
                fhwlths=new Infos.Ohwlths();

                iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID(), castCategory );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
                iRc = tableMethod.getFRWAFER(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getWaferID(), waferData );
                if( !isOk(iRc) ) {
                    return( iRc );
                }

                fhwlths.setWafer_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getWaferID ());
                fhwlths.setCur_lot_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID ());
                fhwlths.setCur_cast_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID ());
                fhwlths.setCur_cast_category(castCategory.getValue() );
                fhwlths.setCur_cast_slot_no (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j)
                        .getOriginalSlotNumber ()==null?null:
                        lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getOriginalSlotNumber ().intValue());
                fhwlths.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
                fhwlths.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
                fhwlths.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());

                if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLSTW002_ID ) == 0 ) {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_STBCANCEL );
                }
                else
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_PREPARECANCEL );
                }

                fhwlths.setProdspec_id(lotWaferMoveEventRecord.getDestinationLotData().getProductID ());
                fhwlths.setGood_unit_count        (waferData.getGoodDiceQty());
                fhwlths.setRepair_unit_count      (waferData.getRepairedDiceQty());
                fhwlths.setFail_unit_count        (waferData.getBadDiceQty());
                fhwlths.setExist_flag("Y" );

                if(isTrue(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getControlWaferFlag ())== TRUE) {
                    fhwlths.setControl_wafer      (true);
                }else{
                    fhwlths.setControl_wafer      (false);
                }

                check_flag = FALSE ;
                for( z = 0; z < length(lotWaferMoveEventRecord.getCurrentWafers()); z++) {
                    if( ( variableStrCmp( lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getWaferID(),
                            lotWaferMoveEventRecord.getCurrentWafers().get(z).getOriginalWaferID ()) == 0 ) ||
                            ( variableStrCmp( lotWaferMoveEventRecord.getSourceLots().get(i).getCurrentWafers().get(j).getWaferID(),
                                    lotWaferMoveEventRecord.getCurrentWafers().get(z).getWaferID ()) == 0 ) )
                    {
                        iRc = tableMethod.getFRCAST( lotWaferMoveEventRecord.getDestinationLotData().getCassetteID(),
                                castCategory);
                        if( !isOk(iRc) ) {
                            return( iRc );
                        }
                        fhwlths.setPrev_lot_id(
                                lotWaferMoveEventRecord.getDestinationLotData().getLotID ());
                        fhwlths.setPrev_cast_id(
                                lotWaferMoveEventRecord.getDestinationLotData().getCassetteID ()) ;
                        fhwlths.setPrev_cast_category(castCategory.getValue() );
                        fhwlths.setPrev_cast_slot_no (
                                lotWaferMoveEventRecord.getCurrentWafers().get(z).getSlotNumber()
                        ==null?null:lotWaferMoveEventRecord.getCurrentWafers().get(z).getSlotNumber().intValue());
                        fhwlths.setOrg_wafer_id(
                                lotWaferMoveEventRecord.getCurrentWafers().get(z).getWaferID ());
                        fhwlths.setOrg_prodspec_id(
                                lotWaferMoveEventRecord.getDestinationLotData().getProductID ());
                        fhwlths.setApply_wafer_flag("Y" );
                        check_flag = TRUE;
                        break;
                    }
                    if(Objects.equals(check_flag, TRUE))
                    {
                        break;
                    }
                }
                if(Objects.equals(check_flag, FALSE)) {
                    fhwlths.setPrev_cast_slot_no  (0);
                    fhwlths.setApply_wafer_flag("N" );
                }

                fhwlths.setAlias_wafer_name(waferData.getAlias_wafer_name ());
                fhwlths.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

                iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/6 17:49
     */
    public Response createFHWLTHS_SourceLot_Txtrc019(Infos.LotWaferMoveEventRecord lotWaferMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths fhwlths=new Infos.Ohwlths();
        Infos.Frwafer waferData=new Infos.Frwafer();
        Params.String castCategory=new Params.String();
        int             i,j,z;
        Boolean  check_flag;
        Response iRc = returnOK();

        for(i = 0 ; i < length(lotWaferMoveEventRecord.getSourceLots()); i++)
        {
            for(j = 0 ; j < length(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers()); j++) {
                fhwlths=new Infos.Ohwlths();

                iRc = tableMethod.getFRCAST(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID(), castCategory );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
                iRc = tableMethod.getFRWAFER(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getWaferID(), waferData );
                if( !isOk(iRc) ) {
                    return( iRc );
                }

                fhwlths.setWafer_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getWaferID ());
                fhwlths.setCur_lot_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getLotID ());
                fhwlths.setCur_cast_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getCassetteID ());
                fhwlths.setCur_cast_category(castCategory.getValue() );
                fhwlths.setCur_cast_slot_no (lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getOriginalSlotNumber ()
                ==null?null:lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getOriginalSlotNumber ().intValue());
                fhwlths.setClaim_user_id(lotWaferMoveEventRecord.getEventCommon().getUserID ());
                fhwlths.setClaim_time(lotWaferMoveEventRecord.getEventCommon().getEventTimeStamp ());
                fhwlths.setClaim_shop_date (lotWaferMoveEventRecord.getEventCommon().getEventShopDate());

                if( variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ||
                        variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXPCC059_ID ) == 0 ||
                        variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW032_ID ) == 0   )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_SPLIT);
                }
                else if((variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW019_ID ) == 0 ) ||
                        (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW017_ID ) == 0 ) ||
                        (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW020_ID ) == 0 ) ||
                        variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW028_ID ) == 0   ||
                        (variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW018_ID ) == 0 )    )
                {
                    if( HsWtDgMgOldCategoryUseFlag == 0 )
                    {
                        fhwlths.setOpe_category(SP_OPERATIONCATEGORY_REWORK);
                    }
                    else
                    {
                        fhwlths.setOpe_category(SP_OPERATIONCATEGORY_SPLIT);
                    }
                }
                else if(  variableStrCmp(  lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXTRC044_ID ) == 0 )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_PILOTSPLIT );
                }
                else if(  variableStrCmp( lotWaferMoveEventRecord.getEventCommon().getTransactionID(), TXTRC045_ID ) == 0 )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_PILOTMERGE );
                }
                else if((variableStrCmp(  lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW033_ID ) == 0 ) ||
                        (variableStrCmp(  lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OLOTW021_ID ) == 0 )    )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MERGE );
                }
                else if((variableStrCmp(  lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLSTW001_ID ) == 0 ) ||
                        (variableStrCmp(  lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OLSTW004_ID ) == 0 ) ||
                        (variableStrCmp(  lotWaferMoveEventRecord.getEventCommon().getTransactionID(),OPMNW003_ID ) == 0 )    )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_STBUSED );
                }
                else if((variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW003_ID ) == 0 ) ||
                        (variableStrCmp(lotWaferMoveEventRecord.getEventCommon().getTransactionID(), OBNKW004_ID ) == 0 ) )
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_VENDORWAFEROUT );
                }
                else
                {
                    fhwlths.setOpe_category(SP_OPERATIONCATEGORY_MONITOROUT );
                }

                fhwlths.setApply_wafer_flag("N" );
                fhwlths.setPrev_cast_slot_no (0);
                fhwlths.setProdspec_id(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceLotData().getProductID ());
                fhwlths.setGood_unit_count        (waferData.getGoodDiceQty());
                fhwlths.setRepair_unit_count      (waferData.getRepairedDiceQty());
                fhwlths.setFail_unit_count        (waferData.getBadDiceQty());
                fhwlths.setExist_flag("Y" );

                if( isTrue(lotWaferMoveEventRecord.getSourceLots().get(i).getSourceWafers().get(j).getControlWaferFlag ())== TRUE)  {
                    fhwlths.setControl_wafer      (true);
                }else{
                    fhwlths.setControl_wafer      (false);
                }

                fhwlths.setAlias_wafer_name(waferData.getAlias_wafer_name ());
                fhwlths.setEvent_create_time(lotWaferMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

                iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        return(returnOK());
    }

}
