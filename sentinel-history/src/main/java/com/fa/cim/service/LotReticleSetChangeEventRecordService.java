package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fa.cim.utils.BaseUtils.isOk;
import static com.fa.cim.utils.BaseUtils.returnOK;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/7/11 10:37
 */
@Slf4j
@Repository
//@Transactional(rollbackFor = Exception.class)
public class LotReticleSetChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private LotReticleSetChangeHistoryService lotReticleSetChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotReticleSetChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/11 11:19
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createLotReticleSetChangeEventRecord( Infos.LotReticleSetChangeEventRecord lotReticleSetChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateLotReticleSetChangeEventRecord Function");
        Infos.Ohrsths        fhrsths= new Infos.Ohrsths();
        Response iRc = returnOK();
        fhrsths = new Infos.Ohrsths();
        fhrsths.setLot_id(lotReticleSetChangeEventRecord.getLotData().getLotID() );
        fhrsths.setRtclset_id(lotReticleSetChangeEventRecord.getReticleSetID() );
        fhrsths.setProdspec_id(lotReticleSetChangeEventRecord.getLotData().getProductID() );
        fhrsths.setLot_type(lotReticleSetChangeEventRecord.getLotData().getLotType() );
        fhrsths.setLot_status(lotReticleSetChangeEventRecord.getLotData().getLotStatus() );
        fhrsths.setCast_id(lotReticleSetChangeEventRecord.getLotData().getCassetteID() );
        fhrsths.setCustomer_id(lotReticleSetChangeEventRecord.getLotData().getCustomerID() );
        fhrsths.setPriority_class(lotReticleSetChangeEventRecord.getLotData().getPriorityClass()==null?
                null:lotReticleSetChangeEventRecord.getLotData().getPriorityClass().intValue());
        fhrsths.setClaim_mainpd_id(lotReticleSetChangeEventRecord.getLotData().getRouteID() );
        fhrsths.setClaim_ope_no(lotReticleSetChangeEventRecord.getLotData().getOperationNumber() );
        fhrsths.setClaim_pd_id(lotReticleSetChangeEventRecord.getLotData().getOperationID() );
        fhrsths.setClaim_pass_count(lotReticleSetChangeEventRecord.getLotData().getOperationPassCount());
        fhrsths.setClaim_time(lotReticleSetChangeEventRecord.getEventCommon().getEventTimeStamp() );
        fhrsths.setClaim_shop_date(lotReticleSetChangeEventRecord.getEventCommon().getEventShopDate() );
        fhrsths.setClaim_user_id(lotReticleSetChangeEventRecord.getEventCommon().getUserID() );
        fhrsths.setEvent_create_time(lotReticleSetChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = lotReticleSetChangeHistoryService.insertLotReticleSetChangeHistory( fhrsths );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateLotReticleSetChangeEventRecord(): InsertLotReticleSetChangeHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateLotReticleSetChangeEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateLotReticleSetChangeEventRecord Function");
        return(returnOK());
    }

}
