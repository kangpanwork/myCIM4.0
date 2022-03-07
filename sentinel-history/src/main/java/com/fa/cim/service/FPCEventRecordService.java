package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fa.cim.utils.BaseUtils.*;

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
public class FPCEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private FPCHistoryService fpcHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param FPCEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 10:18
     */
    public Response createFPCEventRecord(Infos.FPCEventRecord FPCEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateFPCEventRecord Function" );

        Infos.Ohfpchs               fhfpchs;
        Infos.OhfpchsWafer         fhfpchs_wafer;
        Infos.OhfpchsWaferRparm   fhfpchs_wafer_rparm;
        Infos.OhfpchsDcspecs       fhfpchs_dcspecs;
        Infos.OhfpchsRtcl          fhfpchs_rtcl;
        Infos.OhfpchsCorope        fhfpchs_corope;

        Response iRc = returnOK();
        fhfpchs=new Infos.Ohfpchs();
        fhfpchs_wafer=new Infos.OhfpchsWafer();
        fhfpchs_wafer_rparm=new Infos.OhfpchsWaferRparm();
        fhfpchs_dcspecs=new Infos.OhfpchsDcspecs();
        fhfpchs_rtcl=new Infos.OhfpchsRtcl();
        fhfpchs_corope=new Infos.OhfpchsCorope();
        log.info("action_code  : "+ FPCEventRecord.getAction()                  );
        log.info("FPCID        : "+ FPCEventRecord.getFPCID()                   );
        log.info("lotFamilyID  : "+ FPCEventRecord.getLotFamilyID()             );
        log.info("mainPDID     : "+ FPCEventRecord.getMainPDID()                );
        log.info("mainOperNo   : "+ FPCEventRecord.getOperationNumber()         );
        log.info("orgMainPDID  : "+ FPCEventRecord.getOriginalMainPDID()        );
        log.info("orgOperNo    : "+ FPCEventRecord.getOriginalOperationNumber() );
        log.info("subMainPDID  : "+ FPCEventRecord.getSubMainPDID()             );
        log.info("subOperNo    : "+ FPCEventRecord.getSubOperationNumber()      );
        log.info("pdID         : "+ FPCEventRecord.getPdID()                    );
        log.info("FPCType      : "+ FPCEventRecord.getFPCType()                 );
        log.info("FPCGroup     : "+ FPCEventRecord.getFPCGroupNo()              );
        log.info("update_time  : "+ FPCEventRecord.getUpdateTime()              );
        log.info("create_time  : "+ FPCEventRecord.getCreateTime()              );
        //add runCardID for history
        log.info("runcard_id  : "+ FPCEventRecord.getRunCardID()                );
        fhfpchs.setAction                    ( FPCEventRecord.getAction()                    );
        fhfpchs.setFPCID                     ( FPCEventRecord.getFPCID()                     );
        fhfpchs.setLotFamilyID               ( FPCEventRecord.getLotFamilyID()               );
        fhfpchs.setMainPDID                  ( FPCEventRecord.getMainPDID()                  );
        fhfpchs.setOperationNumber           ( FPCEventRecord.getOperationNumber()           );
        fhfpchs.setOriginalMainPDID          ( FPCEventRecord.getOriginalMainPDID()          );
        fhfpchs.setOriginalOperationNumber   ( FPCEventRecord.getOriginalOperationNumber()   );
        fhfpchs.setSubMainPDID               ( FPCEventRecord.getSubMainPDID()               );
        fhfpchs.setSubOperationNumber        ( FPCEventRecord.getSubOperationNumber()        );
        fhfpchs.setFPCGroupNo                ( FPCEventRecord.getFPCGroupNo()                );
        fhfpchs.setFPCType                   ( FPCEventRecord.getFPCType()                   );
        fhfpchs.setMergeMainPDID             ( FPCEventRecord.getMergeMainPDID()             );
        fhfpchs.setMergeOperationNumber      ( FPCEventRecord.getMergeOperationNumber()      );
        fhfpchs.setFPCCategory               ( FPCEventRecord.getFPCCategory()               );
        fhfpchs.setPdID                      ( FPCEventRecord.getPdID()                      );
        fhfpchs.setPdType                    ( FPCEventRecord.getPdType()                    );
        fhfpchs.setCorrespondingOperNo       ( FPCEventRecord.getCorrespondingOperNo()       );
        fhfpchs.setSkipFlag                  ( convertI(FPCEventRecord.getSkipFlag())                   );
        fhfpchs.setRestrictEqpFlag           ( convertI(FPCEventRecord.getRestrictEqpFlag())            );
        fhfpchs.setEquipmentID               ( FPCEventRecord.getEquipmentID()               );
        fhfpchs.setMachineRecipeID           ( FPCEventRecord.getMachineRecipeID()           );
        fhfpchs.setDcDefID                   ( FPCEventRecord.getDcDefID()                   );
        fhfpchs.setDcSpecID                  ( FPCEventRecord.getDcSpecID()                  );
        fhfpchs.setRecipeParameterChangeType ( FPCEventRecord.getRecipeParameterChangeType() );
        fhfpchs.setDescription               ( FPCEventRecord.getDescription()               );
        fhfpchs.setSendEmailFlag             ( convertI(FPCEventRecord.getSendEmailFlag())              );
        fhfpchs.setHoldLotFlag               ( convertI(FPCEventRecord.getHoldLotFlag())                );
        fhfpchs.setCreateTime                ( FPCEventRecord.getCreateTime()                );
        fhfpchs.setUpdateTime                ( FPCEventRecord.getUpdateTime()                );
        fhfpchs.setClaim_time                ( FPCEventRecord.getEventCommon().getEventTimeStamp());
        fhfpchs.setClaim_user_id             ( FPCEventRecord.getEventCommon().getUserID()        );
        fhfpchs.setClaim_memo                ( FPCEventRecord.getEventCommon().getEventMemo()     );
        fhfpchs.setEvent_create_time         ( FPCEventRecord.getEventCommon().getEventCreationTimeStamp());
        //add runCardID for history
        fhfpchs.setRunCardID                 ( FPCEventRecord.getRunCardID());
        iRc = fpcHistoryService.insertFPCHistory_FHFPCHS( fhfpchs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFPCEventRecord(): InsertFPCHistory_FHFPCHS SQL Error Occured" );
            log.info("HistoryWatchDogServer::CreateFPCEventRecord Function" );
            return( iRc );
        }
        int waferLen = length(FPCEventRecord.getWafers());

        for( int waferCnt = 0; waferCnt < waferLen; waferCnt++ ) {
            fhfpchs_wafer=new Infos.OhfpchsWafer();
            //add runCardID for history
            fhfpchs_wafer.setRunCardID(  FPCEventRecord.getRunCardID());
            fhfpchs_wafer.setFPCID   (   FPCEventRecord.getFPCID()                   );
            fhfpchs_wafer.setWafer_id(   FPCEventRecord.getWafers().get(waferCnt).getWaferID());
            fhfpchs_wafer.setClaim_time( FPCEventRecord.getEventCommon().getEventTimeStamp());
            iRc = fpcHistoryService.insertFPCHistory_FHFPCHS_WAFER( fhfpchs_wafer );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFPCEventRecord(): InsertFPCHistory_FHFPCHS_WAFER SQL Error Occured" );
                log.info("HistoryWatchDogServer::CreateFPCEventRecord Function" );
                return( iRc );
            }
            int rcpParmLen = length(FPCEventRecord.getWafers().get(waferCnt).getRecipeParameters());

            for( int rcpParmCnt = 0; rcpParmCnt < rcpParmLen; rcpParmCnt++ ) {
                fhfpchs_wafer_rparm=new Infos.OhfpchsWaferRparm();
                //add runCardID for history
                fhfpchs_wafer_rparm.setRunCardID(FPCEventRecord.getRunCardID());
                fhfpchs_wafer_rparm.setFPCID                    (  FPCEventRecord.getFPCID()           );
                fhfpchs_wafer_rparm.setWafer_id                 (  FPCEventRecord.getWafers().get(waferCnt).getWaferID());
                fhfpchs_wafer_rparm.setSeq_no                    ( convertI(FPCEventRecord.getWafers().get(waferCnt).getRecipeParameters().get(rcpParmCnt).getSeq_No()));
                fhfpchs_wafer_rparm.setParameterName            (  FPCEventRecord.getWafers().get(waferCnt).getRecipeParameters().get(rcpParmCnt).getParameterName());
                fhfpchs_wafer_rparm.setParameterUnit            (  FPCEventRecord.getWafers().get(waferCnt).getRecipeParameters().get(rcpParmCnt).getParameterUnit()       );
                fhfpchs_wafer_rparm.setParameterDataType        (  FPCEventRecord.getWafers().get(waferCnt).getRecipeParameters().get(rcpParmCnt).getParameterDataType()   );
                fhfpchs_wafer_rparm.setParameterLowerLimit      (  FPCEventRecord.getWafers().get(waferCnt).getRecipeParameters().get(rcpParmCnt).getParameterLowerLimit() );
                fhfpchs_wafer_rparm.setParameterUpperLimit      (  FPCEventRecord.getWafers().get(waferCnt).getRecipeParameters().get(rcpParmCnt).getParameterUpperLimit() );
                fhfpchs_wafer_rparm.setParameterValue           (  FPCEventRecord.getWafers().get(waferCnt).getRecipeParameters().get(rcpParmCnt).getParameterValue());
                fhfpchs_wafer_rparm.setUseCurrentSettingValueFlag( convertI(FPCEventRecord.getWafers().get(waferCnt).getRecipeParameters().get(rcpParmCnt).getUseCurrentSettingValueFlag()));
                fhfpchs_wafer_rparm.setParameterTargetValue     (  FPCEventRecord.getWafers().get(waferCnt).getRecipeParameters().get(rcpParmCnt).getParameterTargetValue());
                fhfpchs_wafer_rparm.setParameterTag             (  FPCEventRecord.getWafers().get(waferCnt).getRecipeParameters().get(rcpParmCnt).getParameterTag());
                fhfpchs_wafer_rparm.setClaim_time               (  FPCEventRecord.getEventCommon().getEventTimeStamp());
                iRc = fpcHistoryService.insertFPCHistory_FHFPCHS_WAFER_RPARM( fhfpchs_wafer_rparm );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::createFPCEventRecord(): InsertFPCHistory_FHFPCHS_WAFER_RPARM SQL Error Occured" );
                    log.info("HistoryWatchDogServer::CreateFPCEventRecord Function" );
                    return( iRc );
                }
            }
        }
        int dcSpecItemLen = length(FPCEventRecord.getDcSpecItems());

        for( int dcSpecItemCnt = 0; dcSpecItemCnt < dcSpecItemLen ; dcSpecItemCnt++ ) {
            fhfpchs_dcspecs=new Infos.OhfpchsDcspecs();
            //add runCardID for history
            fhfpchs_dcspecs.setRunCardID(FPCEventRecord.getRunCardID());
            fhfpchs_dcspecs.setFPCID                 ( FPCEventRecord.getFPCID()                                                );
            fhfpchs_dcspecs.setDcItemName            ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getDcItemName()                );
            fhfpchs_dcspecs.setScreenUpperRequired   ( convertI(FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getScreenUpperRequired()));
            fhfpchs_dcspecs.setScreenUpperLimit      ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getScreenUpperLimit());
            fhfpchs_dcspecs.setScreenUpperActions    ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getScreenUpperActions()        );
            fhfpchs_dcspecs.setScreenLowerRequired   ( convertI(FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getScreenLowerRequired()));
            fhfpchs_dcspecs.setScreenLowerLimit      ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getScreenLowerLimit());
            fhfpchs_dcspecs.setScreenLowerActions    ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getScreenLowerActions()        );
            fhfpchs_dcspecs.setSpecUpperRequired     ( convertI(FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getSpecUpperRequired()));
            fhfpchs_dcspecs.setSpecUpperLimit        ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getSpecUpperLimit());
            fhfpchs_dcspecs.setSpecUpperActions      ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getSpecUpperActions()          );
            fhfpchs_dcspecs.setSpecLowerRequired     ( convertI(FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getSpecLowerRequired()));
            fhfpchs_dcspecs.setSpecLowerLimit        ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getSpecLowerLimit());
            fhfpchs_dcspecs.setSpecLowerActions      ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getSpecLowerActions()          );
            fhfpchs_dcspecs.setControlUpperRequired  ( convertI(FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getControlUpperRequired()));
            fhfpchs_dcspecs.setControlUpeerLimit     ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getControlUpeerLimit());
            fhfpchs_dcspecs.setControlUpperActions   ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getControlUpperActions()       );
            fhfpchs_dcspecs.setControlLowerRequired  ( convertI(FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getControlLowerRequired()));
            fhfpchs_dcspecs.setControlLowerLimit     ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getControlLowerLimit());
            fhfpchs_dcspecs.setControlLowerActions   ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getControlLowerActions()       );
            fhfpchs_dcspecs.setDcItemTargetValue     ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getDcItemTargetValue());
            fhfpchs_dcspecs.setDcItemTag             ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getDcItemTag()                 );
            fhfpchs_dcspecs.setDcSpecGroup           ( FPCEventRecord.getDcSpecItems().get(dcSpecItemCnt).getDcSpecGroup()               );
            fhfpchs_dcspecs.setClaim_time            ( FPCEventRecord.getEventCommon().getEventTimeStamp()                           );
            iRc = fpcHistoryService.insertFPCHistory_FHFPCHS_DCSPECS( fhfpchs_dcspecs );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFPCEventRecord(): InsertFPCHistory_FHFPCHS_DCSPECS SQL Error Occured" );
                log.info("HistoryWatchDogServer::CreateFPCEventRecord Function" );
                return( iRc );
            }

        }
        int rtclLen = length(FPCEventRecord.getReticles());

        for( int rtclCnt = 0; rtclCnt < rtclLen; rtclCnt++ ) {
            fhfpchs_rtcl=new Infos.OhfpchsRtcl();
            //add runCardID for history
            fhfpchs_rtcl.setRunCardID(FPCEventRecord.getRunCardID());
            fhfpchs_rtcl.setFPCID          ( FPCEventRecord.getFPCID()                           );
            fhfpchs_rtcl.setSeq_no         ( convertI(FPCEventRecord.getReticles().get(rtclCnt).getSeq_No()));
            fhfpchs_rtcl.setReticleID      ( FPCEventRecord.getReticles().get(rtclCnt).getReticleID()     );
            fhfpchs_rtcl.setReticleGroupID ( FPCEventRecord.getReticles().get(rtclCnt).getReticleGroupID());
            fhfpchs_rtcl.setClaim_time     ( FPCEventRecord.getEventCommon().getEventTimeStamp()      );
            iRc = fpcHistoryService.insertFPCHistory_FHFPCHS_RTCL( fhfpchs_rtcl );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFPCEventRecord(): InsertFPCHistory_FHFPCHS_RTCL SQL Error Occured" );
                log.info("HistoryWatchDogServer::CreateFPCEventRecord Function" );
                return( iRc );
            }
        }
        int corrOpeLen = length(FPCEventRecord.getCorrespondingOperations());
        for( int corrOpeCnt = 0; corrOpeCnt < corrOpeLen; corrOpeCnt++ ) {
            fhfpchs_corope=new Infos.OhfpchsCorope();
            //add runCardID for history
            fhfpchs_corope.setRunCardID(FPCEventRecord.getRunCardID());
            fhfpchs_corope.setFPCID                    ( FPCEventRecord.getFPCID()                           );
            fhfpchs_corope.setSeq_no                   ( corrOpeCnt);
            fhfpchs_corope.setCorrespondingOpeNo       ( FPCEventRecord.getCorrespondingOperations().get(corrOpeCnt).getCorrespondingOperationNumber()     );
            fhfpchs_corope.setDcSpecGroup              ( FPCEventRecord.getCorrespondingOperations().get(corrOpeCnt).getDcSpecGroup());
            fhfpchs_corope.setClaim_time               ( FPCEventRecord.getEventCommon().getEventTimeStamp()      );
            iRc = fpcHistoryService.insertFPCHistory_FHFPCHS_COROPE( fhfpchs_corope );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFPCEventRecord(): InsertFPCHistory_FHFPCHS_COROPE SQL Error Occured" );
                log.info("HistoryWatchDogServer::CreateFPCEventRecord Function" );
                return( iRc );
            }
        }
        log.info("HistoryWatchDogServer::CreateFPCEventRecord Function" );
        return( returnOK() );
    }

}
