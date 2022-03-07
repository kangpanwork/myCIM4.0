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
public class ControlJobStatusChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private ControlJobStatusChangeHistoryService controlJobStatusChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param controlJobStatusChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 10:57
     */
    public Response controlJobCreateStatusChangeEventRecord(Infos.ControlJobStatusChangeEventRecord controlJobStatusChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Response iRc = returnOK();
        log.info("HistoryWatchDogServer::CreateControlJobStatusChangeEventRecord Function" );
        iRc = createFHCJSCHS( controlJobStatusChangeEventRecord, userDataSets );
        if (!isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateControlJobStatusChangeEventRecord Function" );
            return( iRc );
        }

        iRc = returnOK();
        iRc = createFHCJSCHS_LOTS( controlJobStatusChangeEventRecord, userDataSets );
        if (!isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateControlJobStatusChangeEventRecord Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::CreateControlJobStatusChangeEventRecord Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param controlJobStatusChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 10:58
     */
    public Response createFHCJSCHS(Infos.ControlJobStatusChangeEventRecord controlJobStatusChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::createFHCJSCHS Function" );

        Infos.Ohcjschs       fhcjschs;
        Response iRc = returnOK();
        fhcjschs=new Infos.Ohcjschs();
        log.info("ctrljob_id       : ", controlJobStatusChangeEventRecord.getCtrlJob()                    );
        log.info("ctrljob_state    : ", controlJobStatusChangeEventRecord.getCtrlJobState()               );
        log.info("eqp_id           : ", controlJobStatusChangeEventRecord.getEqpID()                      );
        log.info("claim_time       : ", controlJobStatusChangeEventRecord.getEventCommon().getEventTimeStamp() );
        fhcjschs.setCtrljob_id(        controlJobStatusChangeEventRecord.getCtrlJob()                            );
        fhcjschs.setCtrljob_state(     controlJobStatusChangeEventRecord.getCtrlJobState()                       );
        fhcjschs.setEqp_id(            controlJobStatusChangeEventRecord.getEqpID()                              );
        fhcjschs.setEqp_descripstion(  controlJobStatusChangeEventRecord.getEqpDescription()                     );
        fhcjschs.setClaim_time(        controlJobStatusChangeEventRecord.getEventCommon().getEventTimeStamp()         );
        fhcjschs.setClaim_shop_date  ( controlJobStatusChangeEventRecord.getEventCommon().getEventShopDate());
        fhcjschs.setClaim_user_id(     controlJobStatusChangeEventRecord.getEventCommon().getUserID()                 );
        fhcjschs.setClaim_memo(        controlJobStatusChangeEventRecord.getEventCommon().getEventMemo()              );
        fhcjschs.setEvent_create_time( controlJobStatusChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = controlJobStatusChangeHistoryService.insertControlJobStatusChangeHistory( fhcjschs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHCJSCHS(): InsertControlJobStatusChangeHistory SQL Error Occured" );
            log.info("HistoryWatchDogServer::createFHCJSCHS Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::createFHCJSCHS Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param controlJobStatusChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 11:03
     */
    public Response createFHCJSCHS_LOTS(Infos.ControlJobStatusChangeEventRecord controlJobStatusChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::createFHCJSCHS_LOTS Function" );

        Infos.OhcjschsLots   fhcjschs_lots;
        Response iRc   = returnOK();

        int          i     = 0;

        int          count = 0;
        count = length(controlJobStatusChangeEventRecord.getLots());
        log.info("lot info count   : ", count                                                         );
        log.info("ctrljob_id       : ", controlJobStatusChangeEventRecord.getCtrlJob()                    );
        log.info("claim_time       : ", controlJobStatusChangeEventRecord.getEventCommon().getEventTimeStamp() );
        for( i = 0 ; i < count ; i++  ) {
            fhcjschs_lots=new Infos.OhcjschsLots();
            fhcjschs_lots.setCtrljob_id(      controlJobStatusChangeEventRecord.getCtrlJob()                    );
            fhcjschs_lots.setLot_id(          controlJobStatusChangeEventRecord.getLots().get(i).getLotID()              );
            fhcjschs_lots.setCast_id(         controlJobStatusChangeEventRecord.getLots().get(i).getCastID()             );
            fhcjschs_lots.setLot_type(        controlJobStatusChangeEventRecord.getLots().get(i).getLotType()            );
            fhcjschs_lots.setSub_lot_type(    controlJobStatusChangeEventRecord.getLots().get(i).getSubLotType()         );
            fhcjschs_lots.setProdspec_id(     controlJobStatusChangeEventRecord.getLots().get(i).getProdSpecID()         );
            fhcjschs_lots.setMainpd_id(       controlJobStatusChangeEventRecord.getLots().get(i).getMainPDID()           );
            fhcjschs_lots.setOpe_no(          controlJobStatusChangeEventRecord.getLots().get(i).getOpeNo()              );
            fhcjschs_lots.setPd_id(           controlJobStatusChangeEventRecord.getLots().get(i).getPdID()               );
            fhcjschs_lots.setOpe_pass_count ( controlJobStatusChangeEventRecord.getLots().get(i).getOpePassCount()==null?null:
                    controlJobStatusChangeEventRecord.getLots().get(i).getOpePassCount().intValue());
            fhcjschs_lots.setPd_name(         controlJobStatusChangeEventRecord.getLots().get(i).getPdName()             );
            fhcjschs_lots.setClaim_time(      controlJobStatusChangeEventRecord.getEventCommon().getEventTimeStamp() );
            iRc = controlJobStatusChangeHistoryService.insertControlJobStatusChangeLotsHistory( fhcjschs_lots );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHCJSCHS_LOTS(): InsertControlJobStatusChangeLotsHistory SQL Error Occured" );
                log.info("HistoryWatchDogServer::createFHCJSCHS_LOTS Function" );
                return( iRc );
            }
        }

        log.info("HistoryWatchDogServer::createFHCJSCHS_LOTS Function" );
        return(returnOK());
    }

}
