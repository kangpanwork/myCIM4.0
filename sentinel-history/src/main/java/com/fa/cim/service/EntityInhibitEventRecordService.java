package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
 * @exception
 * @author Ho
 * @date 2019/7/4 13:30
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class EntityInhibitEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private EntityInhibitHistoryService entityInhibitHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param entityInhibitEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 14:26
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createEntityInhibitEventRecord( Infos.EntityInhibitEventRecord entityInhibitEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Response iRc = returnOK();
        if (log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::CreateEntityInhibitEventRecord Function");
        }

        iRc = createFHENINHS( entityInhibitEventRecord, userDataSets );
        if (!isOk(iRc) ) {
            if(log.isDebugEnabled()){
                log.debug("HistoryWatchDogServer::CreateEntityInhibitEventRecord Function");
            }
            return ( iRc );
        }
        iRc = createFHENINHS_ENTITY( entityInhibitEventRecord, userDataSets );
        if (!isOk(iRc) ) {
            if(log.isDebugEnabled()){
                log.debug("HistoryWatchDogServer::CreateEntityInhibitEventRecord Function");
            }
            return ( iRc );
        }
        iRc = createFHENINHS_EXPENTITY( entityInhibitEventRecord, userDataSets );
        if (!isOk(iRc) ) {
            if(log.isDebugEnabled()){
                log.debug("HistoryWatchDogServer::CreateExpEntityInhibitEventRecord Function");
            }
            return ( iRc );
        }
        iRc = createFHENINHS_SUBLOT( entityInhibitEventRecord, userDataSets );
        if (!isOk(iRc) ) {
            if(log.isDebugEnabled()){
                log.debug("HistoryWatchDogServer::CreateEntityInhibitEventRecord Function");
            }
            return ( iRc );
        }
        iRc = createFHENINHS_RSNINFO( entityInhibitEventRecord, userDataSets );
        if (!isOk(iRc) ) {
            if(log.isDebugEnabled()){
                log.debug("HistoryWatchDogServer::CreateEntityInhibitEventRecord Function");
            }
            return ( iRc );
        }
        iRc = createFHENINHS_RSNINFO_SPC( entityInhibitEventRecord, userDataSets );
        if (!isOk(iRc) ) {
            if(log.isDebugEnabled()){
                log.debug("HistoryWatchDogServer::CreateEntityInhibitEventRecord Function");
            }
            return ( iRc );
        }
        iRc = createFHENINHS_EXPLOT( entityInhibitEventRecord, userDataSets );
        if (!isOk(iRc) ) {
            if(log.isDebugEnabled()){
                log.debug("HistoryWatchDogServer::CreateEntityInhibitEventRecord Function");
            }
            return ( iRc );
        }
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::CreateEntityInhibitEventRecord Function");
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
     * @param entityInhibitEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 14:29
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createFHENINHS( Infos.EntityInhibitEventRecord entityInhibitEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Oheninhs fheninhs = new Infos.Oheninhs();
        Response iRc = returnOK();
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS Function");
        }
        fheninhs = new Infos.Oheninhs();
        fheninhs.setInhibit_id(entityInhibitEventRecord.getInhibitID() );
        if( variableStrCmp( entityInhibitEventRecord.getEventCommon().getTransactionID() , OCONW001_ID) == 0 ) {
            fheninhs.setInhibit_type("R" );
        }else if( variableStrCmp( entityInhibitEventRecord.getEventCommon().getTransactionID() , OCONW003_ID) == 0 ){
            fheninhs.setInhibit_type("E" );
        }else if( variableStrCmp( entityInhibitEventRecord.getEventCommon().getTransactionID() , OCONW004_ID) == 0 ){
            fheninhs.setInhibit_type("D" );
        }else if( variableStrCmp( entityInhibitEventRecord.getEventCommon().getTransactionID() , OEQPW006_ID) == 0 ||
                variableStrCmp( entityInhibitEventRecord.getEventCommon().getTransactionID() , OEQPW008_ID) == 0 ||
                variableStrCmp( entityInhibitEventRecord.getEventCommon().getTransactionID() , OEQPW014_ID) == 0 ||
                variableStrCmp( entityInhibitEventRecord.getEventCommon().getTransactionID() , OEQPW023_ID) == 0 ) {
            fheninhs.setInhibit_type("U");
        } else if (variableStrCmp(entityInhibitEventRecord.getEventCommon().getTransactionID() , OCONW007_ID) == 0) {
            fheninhs.setInhibit_type("M");
        }else{
            fheninhs.setInhibit_type("C" );
        }
        fheninhs.setHistoryID(entityInhibitEventRecord.getHistoryID());
        fheninhs.setStart_time(entityInhibitEventRecord.getStartTimeStamp() );
        fheninhs.setEnd_time(entityInhibitEventRecord.getEndTimeStamp() );
        fheninhs.setReason_code(entityInhibitEventRecord.getReasonCode() );
        fheninhs.setReason_desc(entityInhibitEventRecord.getReasonDesc() ) ;
        fheninhs.setDescription(entityInhibitEventRecord.getDescription() ) ;
        fheninhs.setClaim_time(entityInhibitEventRecord.getEventCommon().getEventTimeStamp() );
        fheninhs.setClaim_shop_date(entityInhibitEventRecord.getEventCommon().getEventShopDate() );
        fheninhs.setClaim_user_id(entityInhibitEventRecord.getEventCommon().getUserID() );
        fheninhs.setClaim_memo(entityInhibitEventRecord.getEventCommon().getEventMemo() );
        fheninhs.setEvent_create_time(entityInhibitEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fheninhs.setApplied_context(entityInhibitEventRecord.getAppliedContext() );
        fheninhs.setFunction_rule(entityInhibitEventRecord.getFunctionRule());
        fheninhs.setSpecific_tool(entityInhibitEventRecord.getSpecificTool());
        fheninhs.setClaim_memo(entityInhibitEventRecord.getMemo());
        iRc = entityInhibitHistoryService.insertEntityInhibitHistory( fheninhs );
        if( !isOk(iRc) ) {
            if(log.isDebugEnabled()){
                log.debug("HistoryWatchDogServer::createFHENINHS(): InsertEntityInhibitHistory SQL Error Occured");
                log.debug("HistoryWatchDogServer::createFHENINHS Function");
            }
            return( iRc );
        }
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS Function");
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
     * @param entityInhibitEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 14:38
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createFHENINHS_ENTITY( Infos.EntityInhibitEventRecord entityInhibitEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Oheninhs_entity fheninhs_entity = new Infos.Oheninhs_entity();
        int i;
        Response iRc = returnOK();
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_ENTITY Function");
        }
        for( i = 0; i < length(entityInhibitEventRecord.getEntities()); i++ ) {
            fheninhs_entity = new Infos.Oheninhs_entity();
            fheninhs_entity.setRefrenceKey(entityInhibitEventRecord.getEntities().get(i).getRefrenceKey());
            fheninhs_entity.setInhibit_id(entityInhibitEventRecord.getInhibitID() );
            fheninhs_entity.setClaim_time(entityInhibitEventRecord.getEventCommon().getEventTimeStamp() );
            fheninhs_entity.setClass_name(entityInhibitEventRecord.getEntities().get(i).getClassName() );
            fheninhs_entity.setEntity_id(entityInhibitEventRecord.getEntities().get(i).getObjectId().getIdentifier() );
            fheninhs_entity.setAttrib(entityInhibitEventRecord.getEntities().get(i).getAttrib() );
            iRc = entityInhibitHistoryService.insertEntityInhibitHistory_entity( fheninhs_entity );
            if( !isOk(iRc) ) {
                if(log.isDebugEnabled()){
                    log.debug("HistoryWatchDogServer::createFHENINHS(): InsertEntityInhibitHistory_entity SQL Error Occured");
                    log.debug("HistoryWatchDogServer::createFHENINHS_ENTITY Function");
                }
                return( iRc );
            }
        }
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_ENTITY Function");
        }
        return(returnOK());
    }
    /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/6/9 14:50                       AOKI              Create
    * @author AOKI
    * @date 2021/6/9 14:50
    * @param entityInhibitEventRecord
    * @param userDataSets
    * @return com.fa.cim.dto.Response
    */
    public Response createFHENINHS_EXPENTITY( Infos.EntityInhibitEventRecord entityInhibitEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Oheninhs_entity fheninhs_entity = new Infos.Oheninhs_entity();
        int i;
        Response iRc = returnOK();
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_ENTITY Function");
        }
        for( i = 0; i < length(entityInhibitEventRecord.getExpEntities()); i++ ) {
            fheninhs_entity = new Infos.Oheninhs_entity();
            fheninhs_entity.setRefrenceKey(entityInhibitEventRecord.getExpEntities().get(i).getRefrenceKey());
            fheninhs_entity.setInhibit_id(entityInhibitEventRecord.getInhibitID() );
            fheninhs_entity.setClaim_time(entityInhibitEventRecord.getEventCommon().getEventTimeStamp() );
            fheninhs_entity.setClass_name(entityInhibitEventRecord.getExpEntities().get(i).getClassName() );
            fheninhs_entity.setEntity_id(entityInhibitEventRecord.getExpEntities().get(i).getObjectId().getIdentifier() );
            fheninhs_entity.setAttrib(entityInhibitEventRecord.getExpEntities().get(i).getAttrib() );
            iRc = entityInhibitHistoryService.insertExpEntityInhibitHistory_entity( fheninhs_entity );
            if( !isOk(iRc) ) {
                if(log.isDebugEnabled()){
                    log.debug("HistoryWatchDogServer::createFHENINHS(): InsertExpEntityInhibitHistory_entity SQL Error Occured");
                    log.debug("HistoryWatchDogServer::createFHENINHS_EXPENTITY Function");
                }
                return( iRc );
            }
        }
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_EXPENTITY Function");
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
     * @param entityInhibitEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 14:44
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createFHENINHS_SUBLOT( Infos.EntityInhibitEventRecord entityInhibitEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Oheninhs_sublot fheninhs_sublot = new Infos.Oheninhs_sublot();
        int i;
        Response iRc = returnOK();
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_SUBLOT Function");
        }
        for( i = 0; i < length(entityInhibitEventRecord.getSublottypes()); i++ ) {
            fheninhs_sublot = new Infos.Oheninhs_sublot();
            fheninhs_sublot.setInhibit_id(entityInhibitEventRecord.getInhibitID() );
            fheninhs_sublot.setClaim_time(entityInhibitEventRecord.getEventCommon().getEventTimeStamp() );
            fheninhs_sublot.setSub_lot_type(entityInhibitEventRecord.getSublottypes().get(i));
            iRc = entityInhibitHistoryService.insertEntityInhibitHistory_sublot( fheninhs_sublot );
            if( !isOk(iRc) ) {
                if(log.isDebugEnabled()){
                    log.debug("HistoryWatchDogServer::createFHENINHS_SUBLOT(): InsertEntityInhibitHistory_sublot SQL Error Occured");
                    log.debug("HistoryWatchDogServer::createFHENINHS_SUBLOT Function");
                }
                return( iRc );
            }
        }
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_SUBLOT Function");
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
     * @param entityInhibitEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 14:49
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createFHENINHS_RSNINFO( Infos.EntityInhibitEventRecord entityInhibitEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Oheninhs_rsninfo fheninhs_rsninfo = new Infos.Oheninhs_rsninfo();
        int i;
        Response iRc = returnOK();
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_RSNINFO Function");
        }
        for( i = 0; i < length(entityInhibitEventRecord.getReasonDetailInfos()); i++ ) {
            fheninhs_rsninfo = new Infos.Oheninhs_rsninfo();
            fheninhs_rsninfo.setInhibit_id(entityInhibitEventRecord.getInhibitID() );
            fheninhs_rsninfo.setClaim_time(entityInhibitEventRecord.getEventCommon().getEventTimeStamp() );
            fheninhs_rsninfo.setLot_id(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedLotID() );
            fheninhs_rsninfo.setCtrljob_id(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedControlJobID() );
            fheninhs_rsninfo.setFab_id(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedFabID() );
            fheninhs_rsninfo.setMainpd_id(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedRouteID() );
            fheninhs_rsninfo.setPd_id(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedProcessDefinitionID() );
            fheninhs_rsninfo.setOpe_no(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedOperationNumber() );
            fheninhs_rsninfo.setPass_count(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedOperationPassCount() );
            iRc = entityInhibitHistoryService.insertEntityInhibitHistory_rsninfo( fheninhs_rsninfo );
            if( !isOk(iRc) ) {
                if(log.isDebugEnabled()){
                    log.debug("HistoryWatchDogServer::createFHENINHS_RSNINFO(): InsertEntityInhibitHistory_entity SQL Error Occured");
                    log.debug("HistoryWatchDogServer::createFHENINHS_RSNINFO Function");
                }
                return( iRc );
            }
        }
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_RSNINFO Function");
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
     * @param entityInhibitEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 14:56
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createFHENINHS_RSNINFO_SPC( Infos.EntityInhibitEventRecord entityInhibitEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Oheninhs_rsninfo_spc fheninhs_rsninfo_spc = new Infos.Oheninhs_rsninfo_spc();
        int i,j;
        Response iRc = returnOK();
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_RSNINFO_SPC Function");
        }
        for( i = 0; i < length(entityInhibitEventRecord.getReasonDetailInfos()); i++ ) {
            for( j = 0; j < length(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedSpcChartInfos()); j++ ) {
                fheninhs_rsninfo_spc = new Infos.Oheninhs_rsninfo_spc();
                fheninhs_rsninfo_spc.setInhibit_id(entityInhibitEventRecord.getInhibitID() );
                fheninhs_rsninfo_spc.setClaim_time(entityInhibitEventRecord.getEventCommon().getEventTimeStamp() );
                fheninhs_rsninfo_spc.setDc_type(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedSpcChartInfos().
                        get(j).getRelatedSpcDcType() );
                fheninhs_rsninfo_spc.setChart_grp_id(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedSpcChartInfos().
                        get(j).getRelatedSpcChartGroupID() );
                fheninhs_rsninfo_spc.setChart_id(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedSpcChartInfos().
                        get(j).getRelatedSpcChartID() );
                fheninhs_rsninfo_spc.setChart_type(entityInhibitEventRecord.getReasonDetailInfos().get(i).getRelatedSpcChartInfos().
                        get(j).getRelatedSpcChartType() );
                iRc = entityInhibitHistoryService.insertEntityInhibitHistory_rsninfo_spc( fheninhs_rsninfo_spc );
                if( !isOk(iRc) ) {
                    if(log.isDebugEnabled()){
                        log.debug("HistoryWatchDogServer::createFHENINHS_RSNINFO_SPC(): InsertEntityInhibitHistory_entity SQL Error Occured");
                        log.debug("HistoryWatchDogServer::createFHENINHS_RSNINFO_SPC Function");
                    }
                    return( iRc );
                }
            }
        }
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_RSNINFO_SPC Function");
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
     * @param entityInhibitEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 15:02
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createFHENINHS_EXPLOT( Infos.EntityInhibitEventRecord entityInhibitEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Oheninhs_explot fheninhs_explot = new Infos.Oheninhs_explot();
        int i;
        Response iRc = returnOK();
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_EXPLOT Function");
        }
        for( i = 0; i < length(entityInhibitEventRecord.getExceptionLots()); i++ ) {
            fheninhs_explot = new Infos.Oheninhs_explot();
            fheninhs_explot.setInhibit_id(entityInhibitEventRecord.getInhibitID() );
            fheninhs_explot.setLot_id(entityInhibitEventRecord.getExceptionLots().get(i).getLotID().getIdentifier() );
            fheninhs_explot.setClaim_time(entityInhibitEventRecord.getEventCommon().getEventTimeStamp() );
            fheninhs_explot.setSingle_trig_flag(entityInhibitEventRecord.getExceptionLots().get(i).getSingleTriggerFlag());
            iRc = entityInhibitHistoryService.insertEntityInhibitHistory_explot( fheninhs_explot );
            if( !isOk(iRc) ) {
                if(log.isDebugEnabled()){
                    log.debug("HistoryWatchDogServer::createFHENINHS_EXPLOT(): InsertEntityInhibitHistory_entity SQL Error Occured");
                    log.debug("HistoryWatchDogServer::createFHENINHS_EXPLOT Function");
                }
                return( iRc );
            }
        }
        if(log.isDebugEnabled()){
            log.debug("HistoryWatchDogServer::createFHENINHS_EXPLOT Function");
        }
        return(returnOK());
    }

}
